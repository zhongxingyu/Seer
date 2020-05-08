 /*
  * Copyright (c) 2013, Tripwire, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *  o Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  *  o Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.jmxdatamart.Extractor;
 
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Properties;
 import javax.management.AttributeNotFoundException;
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.MBeanException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 import org.jmxdatamart.JMXTestServer.TestBean;
 import org.jmxdatamart.common.*;
 
 public class Main {
 
   public static void main(String[] args) throws Exception {
         System.out.println("extract");
         
         // values
         int expected = 42;
         
         //Create new test MBean
         TestBean tb = new TestBean();
         tb.setA(new Integer(expected));
         MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
         String mbName = "org.jmxdatamart.JMXTestServer:type=TestBean";
         ObjectName mbeanName = new ObjectName(mbName);
         mbs.registerMBean(tb, mbeanName);
         
         //Create test MBean's MBeanData
         Attribute a = new Attribute("A", "Alpha", DataType.INT);
         MBeanData mbd = new MBeanData(mbName, "testMBean",
                                         Collections.singletonList(a), true);
 
         //Init MBeanExtract
         MBeanExtract instance = new MBeanExtract(mbd, mbs);
         Map result = instance.extract();
         
         //test MBean to embbed DB
         Settings s = new Settings();
         s.setBeans(Collections.singletonList((BeanData)mbd));
         s.setFolderLocation("HyperSQL/");
         s.setPollingRate(2);
         s.setUrl("service:jmx:rmi:///jndi/rmi://:9999/jmxrmi");
         Properties props = new Properties();
         props.put("username", "sa");
         props.put("password", "whatever");
 
         Bean2DB bd = new Bean2DB();
         String dbname = bd.generateMBeanDB(s);
 
         HypersqlHandler hsql = new HypersqlHandler();
         Connection conn= hsql.connectDatabase(dbname,props);
         bd.export2DB(conn,mbd,result);
        ResultSet rs = conn.createStatement().executeQuery("select count(*) from org_jmxdatamart_JMXTestServer__type___TestBean");
         rs.next();
         System.out.println(rs.getInt(1));
         rs.close();
         hsql.shutdownDatabase(conn);
         hsql.disconnectDatabase(rs,null,null,conn);
 
         // TODO review the generated test code and remove the default call to fail.
         //fail("The test case is a prototype.");
   }
 }
