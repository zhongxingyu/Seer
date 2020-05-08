 /*
 * JBoss, Home of Professional Open Source
 * Copyright $today.year Red Hat Inc. and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 
 package org.jboss.test.jgroups.cassandra.test;
 
 import java.net.URL;
 import java.util.List;
 
 import org.jboss.jgroups.cassandra.spi.CassandraSPI;
 import org.jboss.test.jgroups.cassandra.support.ExposedPing;
 import org.jgroups.Address;
 import org.jgroups.JChannel;
 import org.jgroups.Message;
 import org.jgroups.ReceiverAdapter;
 import org.jgroups.protocols.PingData;
 import org.jgroups.util.UUID;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Simple smoke test case.
  *
  * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
  */
 public abstract class AbstractSmokeTest extends AbstractCassandraTest
 {
    protected static String JGROUPS = "jgroups";
    protected static String CLUSTER = "clusters";
 
    protected abstract CassandraSPI createSPI();
 
    @Before
    public void create()
    {
       if (isCassandraRunning())
       {
          CassandraSPI spi = createSPI();
          spi.createKeyspace(JGROUPS);
          spi.createColumnFamily(JGROUPS, CLUSTER);
       }
    }
 
    @After
    public void destroy()
    {
       if (isCassandraRunning())
       {
          CassandraSPI spi = createSPI();
          spi.dropColumnFamily(JGROUPS, CLUSTER);
          spi.dropKeyspace(JGROUPS);
       }
    }
 
    protected abstract ExposedPing getPing();
 
    @Test
    public void testBasicOps() throws Exception
    {
       if (isCassandraRunning() == false)
          return;
 
       // mock data
       Address address = UUID.randomUUID();
       PingData data = new PingData(address, null, true);
 
       ExposedPing ping = getPing();
       ping.init();
       try
       {
          ping.writeToFile(data, CLUSTER);
          try
          {
             testReadAll(ping, data, CLUSTER);
          }
          finally
          {
             ping.remove(CLUSTER, address);
          }
 
          List<PingData> empty = ping.readAll(CLUSTER);
          Assert.assertNotNull(empty);
          Assert.assertTrue(empty.isEmpty());
       }
       finally
       {
          ping.destroy();
       }
    }
 
    @Test
    public void testMultipleClusters() throws Exception
    {
      if (isCassandraRunning() == false)
         return;

       String clusterName = "test2";
 
       CassandraSPI spi = createSPI();
       spi.createColumnFamily(JGROUPS, clusterName);
       try
       {
          // mock data
          Address address1 = UUID.randomUUID();
          Address address2 = UUID.randomUUID();
          PingData data1 = new PingData(address1, null, true);
          PingData data2 = new PingData(address2, null, false);
 
          ExposedPing ping = getPing();
          ping.init();
          try
          {
             ping.writeToFile(data1, CLUSTER);
             try
             {
                ping.writeToFile(data2, clusterName);
                try
                {
                   testReadAll(ping, data2, clusterName);
                   testReadAll(ping, data1, CLUSTER);
                }
                finally
                {
                   ping.remove(clusterName, address2);
                }
             }
             finally
             {
                ping.remove(CLUSTER, address1);
             }
 
             List<PingData> empty = ping.readAll(CLUSTER);
             Assert.assertNotNull(empty);
             Assert.assertTrue(empty.isEmpty());
 
             empty = ping.readAll(clusterName);
             Assert.assertNotNull(empty);
             Assert.assertTrue(empty.isEmpty());
          }
          finally
          {
             ping.destroy();
          }
       }
       finally
       {
          spi.dropColumnFamily(JGROUPS, clusterName);
       }
    }
 
    protected void testReadAll(ExposedPing ping, PingData data, String clusterName)
    {
       List<PingData> datas = ping.readAll(clusterName);
       Assert.assertNotNull(datas);
       Assert.assertEquals(1, datas.size());
       PingData pd = datas.get(0);
       Assert.assertEquals(data, pd);
    }
 
    @Test
    public void testMockRun() throws Exception
    {
       if (isCassandraRunning() == false)
          return;
 
       URL url = getResource("etc/test-run.xml");
       Assert.assertNotNull(url);
 
       JChannel channel = new JChannel(url);
       channel.setReceiver(new ReceiverAdapter()
       {
          public void receive(Message msg)
          {
             System.out.println("received msg from " + msg.getSrc() + ": " + msg.getObject());
          }
       });
       channel.connect("MyCluster");
       try
       {
          channel.send(new Message(null, null, "hello world"));
       }
       finally
       {
          channel.close();
       }
    }
 }
