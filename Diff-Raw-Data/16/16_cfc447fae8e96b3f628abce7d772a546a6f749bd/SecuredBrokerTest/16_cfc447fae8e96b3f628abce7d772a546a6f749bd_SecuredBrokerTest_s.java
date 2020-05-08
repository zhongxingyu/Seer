 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.jbi.security;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jbi.messaging.InOnly;
 import javax.security.auth.Subject;
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 import javax.security.auth.callback.UnsupportedCallbackException;
 import javax.security.auth.login.LoginContext;
 
 import junit.framework.TestCase;
 
 import org.apache.servicemix.client.DefaultServiceMixClient;
 import org.apache.servicemix.client.ServiceMixClient;
 import org.apache.servicemix.jbi.container.JBIContainer;
 import org.apache.servicemix.jbi.jaxp.StringSource;
 import org.apache.servicemix.jbi.security.acl.AuthorizationMap;
 import org.apache.servicemix.jbi.security.acl.impl.AuthorizationEntry;
 import org.apache.servicemix.jbi.security.acl.impl.DefaultAuthorizationMap;
 import org.apache.servicemix.tck.ReceiverComponent;
 
 public class SecuredBrokerTest extends TestCase {
 
     static {
         String path = System.getProperty("java.security.auth.login.config");
         if (path == null) {
             URL resource = PropertiesLoginModuleTest.class.getResource("login.properties");
             if (resource != null) {
                 path = new File(resource.getFile()).getAbsolutePath();
                 System.setProperty("java.security.auth.login.config", path);
             }
         }
         System.out.println("Path to login config: " + path);
     }
 
     protected JBIContainer jbi;
     protected ReceiverComponent receiver;
     
     protected void setUp() throws Exception {
         jbi = new JBIContainer();
         jbi.setUseMBeanServer(false);
         jbi.setEmbedded(true);
         List entries = new ArrayList();
        entries.add(new AuthorizationEntry(ReceiverComponent.SERVICE, null, "programmers"));
         AuthorizationMap map = new DefaultAuthorizationMap(entries);
         SecuredBroker broker = new SecuredBroker(map);
         jbi.setBroker(broker);
         jbi.init();
         
         receiver = new ReceiverComponent();
         jbi.activateComponent(receiver, "receiver");
 
         jbi.start();
     }
     
     protected void tearDown() throws Exception {
         jbi.shutDown();
     }
     
     protected Subject login(final String username, final String password) throws Exception {
         LoginContext context = new LoginContext("servicemix-domain", new CallbackHandler() {
             public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                 for (int i = 0; i < callbacks.length; i++) {
                     if (callbacks[i] instanceof NameCallback) {
                         ((NameCallback) callbacks[i]).setName(username);
                     } else if (callbacks[i] instanceof PasswordCallback) {
                         ((PasswordCallback) callbacks[i]).setPassword(password.toCharArray());
                     } else {
                         throw new UnsupportedCallbackException(callbacks[i]);
                     }
                 }
             }
         });
         context.login();
         return context.getSubject();
     }
     
     public void testOk() throws Exception {
         Subject subject = login("first", "secret");
         ServiceMixClient client = new DefaultServiceMixClient(jbi);
         InOnly me = client.createInOnlyExchange();
         me.setService(ReceiverComponent.SERVICE);
         me.getInMessage().setSecuritySubject(subject);
         me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
         client.sendSync(me);
         
         receiver.getMessageList().assertMessagesReceived(1);
     }
     
     public void testNOk() throws Exception {
         Subject subject = login("second", "password");
         ServiceMixClient client = new DefaultServiceMixClient(jbi);
         InOnly me = client.createInOnlyExchange();
         me.setService(ReceiverComponent.SERVICE);
         me.getInMessage().setSecuritySubject(subject);
         me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
         try {
             client.sendSync(me);
             fail("Should have thrown a SecurityException");
         } catch (SecurityException e) {
             // ok
         }
     }
     
 }
