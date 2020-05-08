 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008, Red Hat, Inc., and others contributors as indicated
  * by the @authors tag. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.jboss.blacktie.jatmibroker.xatmi;
 
 import junit.framework.TestCase;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.jboss.blacktie.jatmibroker.core.conf.ConfigurationException;
 import org.jboss.blacktie.jatmibroker.core.server.AtmiBrokerServer;
 
 public class TestTPCall extends TestCase {
 	private static final Logger log = LogManager.getLogger(TestTPCall.class);
 	private AtmiBrokerServer server;
 	private Connection connection;
 
 	public void setUp() throws ConnectionException, ConfigurationException {
 		this.server = new AtmiBrokerServer("standalone-server", null);
 
 		ConnectionFactory connectionFactory = ConnectionFactory
 				.getConnectionFactory();
 		connection = connectionFactory.getConnection();
 	}
 
 	public void tearDown() throws ConnectionException, ConfigurationException {
 		connection.close();
 		server.close();
 	}
 
 	public void test_tpcall_x_octet() throws ConnectionException {
 		log.info("test_tpcall_x_octet");
 		this.server.tpadvertise("TestOne", TestTPCallServiceXOctet.class
 				.getName());
 
 		String toSend = "test_tpcall_x_octet";
 		int sendlen = toSend.length() + 1;
 		int rcvlen = sendlen;
 		Buffer sendbuf = new Buffer("X_OCTET", null);
 		sendbuf.setData(toSend.getBytes());
 
 		Response rcvbuf = connection.tpcall("TestOne", sendbuf, sendlen, 0);
 		assertTrue(rcvbuf != null);
 		assertTrue(rcvbuf.getBuffer() != null);
 		assertTrue(rcvbuf.getBuffer().getData() != null);
 		assertTrue(new String(rcvbuf.getBuffer().getData())
 				.equals("tpcall_x_octet"));
 	}
 }
