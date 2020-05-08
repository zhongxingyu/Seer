 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) 2009 Osaka Gas Information System Research Institute Co., Ltd. 
  * All rights reserved.  http://www.ogis-ri.co.jp/
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE file.
  */
 
 package org.mule.transport.ssh;
 
 import net.sf.commons.ssh.PublicKeyAuthenticationOptions;
 
 import org.mule.api.endpoint.OutboundEndpoint;
 import org.mule.tck.FunctionalTestCase;
 
 /**
  * TODO write document
  */
 public class SshNamespaceHandlerTestCase extends FunctionalTestCase
 {
     protected String getConfigResources()
     {
         return "ssh-namespace-config.xml";
         
     }
 
 	public void testSshConfig() throws Exception
     {
         SshConnector c = (SshConnector) muleContext.getRegistry().lookupConnector("sshConnector");
         
         assertNotNull(c);
         
         assertTrue(c.isConnected());
         
         assertTrue(c.isStarted());
         
        assertEquals("localhost", c.getHost());
         
         assertEquals(22, c.getPort());
         
         assertEquals(3000, c.getKexTimeout());
         
         assertEquals(PublicKeyAuthenticationOptions.class, 
         		c.getAuthOptions().getClass());
        
        //assertEquals("user", ((PublicKeyAuthenticationOptions)c.getAuthOptions()).login);
 
         
         OutboundEndpoint endpoint = (OutboundEndpoint) muleContext.getRegistry().lookupObject("ssh-out");
         
         assertEquals(Boolean.TRUE.toString(), endpoint.getProperty("useSudo"));
         
         //valid to replace from 'ssh="SEND"' to 'address="ssh://ssh.out"'
         assertEquals("ssh.out", endpoint.getEndpointURI().getAddress());
         
        assertEquals("sudopwd", endpoint.getProperty("sudoPassword"));
        
        assertEquals(4000, endpoint.getResponseTimeout());
         
         assertEquals(true, endpoint.isSynchronous());
         
     }
     
 }
