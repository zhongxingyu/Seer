 /**
  *
  * Copyright 2013 the original author or authors.
  * Copyright 2013 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sorcer.ex1.requestor;
 
 import org.junit.Test;
 import sorcer.core.SorcerConstants;
 import sorcer.core.context.ServiceContext;
 import sorcer.core.exertion.NetTask;
 import sorcer.core.exertion.ObjectTask;
 import sorcer.core.signature.NetSignature;
 import sorcer.core.signature.ObjectSignature;
 import sorcer.ex1.bean.WhoIsItBean1;
 import sorcer.ex1.provider.WhoIsItProvider1;
 import sorcer.service.Context;
 import sorcer.service.Exertion;
 import sorcer.service.Signature;
 import sorcer.service.Signature.Type;
 import sorcer.service.Task;
 import sorcer.util.Sorcer;
 
 import java.net.InetAddress;
 import java.rmi.RMISecurityManager;
 import java.util.logging.Logger;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * @author Mike Sobolewski
  */
 @SuppressWarnings({ "rawtypes", "unchecked" })
 public class WhoIsItTest implements SorcerConstants {
 
 	private final static Logger logger = Logger
 			.getLogger(WhoIsItTest.class.getName());
 
 	static {
         System.setProperty("java.security.policy", System.getenv("SORCER_HOME")
                 + "/configs/sorcer.policy");
         System.setSecurityManager(new RMISecurityManager());
         Sorcer.setCodeBaseByArtifacts(new String[]{
                 "org.sorcersoft.sorcer:sos-platform",
                "org.sorcersoft.sorcer:ex1-prv",
                 "org.sorcersoft.sorcer:ex1-api"});
         System.out.println("CLASSPATH :" + System.getProperty("java.class.path"));
 	}
 
     @Test
     public void helloObjectTask() throws Exception {
         InetAddress inetAddress = InetAddress.getLocalHost();
         String hostname = inetAddress.getHostName();
         String ipAddress = inetAddress.getHostAddress();
 
         Context context = new ServiceContext("Who Is It?");
         context.putValue("requestor/message", "Hello Objects!");
         context.putValue("requestor/hostname", hostname);
 
         ObjectSignature signature = new ObjectSignature("getHostName",
                 WhoIsItBean1.class);
 
         Task task = new ObjectTask("Who Is It?", signature, context);
         Exertion result = task.exert();
         logger.info("task context: " + result.getContext());
         assertEquals(result.getContext().getValue("provider/hostname"), hostname);
         assertEquals(result.getContext().getValue("provider/address"), ipAddress);
     }
 
     @Test
     public void helloNetworkTask() throws Exception {
         // using requestor/provider message types
 
         InetAddress inetAddress = InetAddress.getLocalHost();
         String hostname = inetAddress.getHostName();
         String ipAddress = inetAddress.getHostAddress();
         String providerName = null;
 
         Context context = new ServiceContext("Who Is It?");
         context.putValue("requestor/message", new RequestorMessage("Hello Network!"));
         context.putValue("requestor/hostname", hostname);
 
         NetSignature signature = new NetSignature("getHostName",
                 sorcer.ex1.WhoIsIt.class, providerName);
 
         Task task = new NetTask("Who Is It?", signature, context);
         Exertion result = task.exert();
         logger.info("task context: " + result.getContext());
         assertEquals(result.getContext().getValue("provider/hostname"), hostname);
        assertEquals(result.getContext().getValue("provider/address"), ipAddress);
     }
 
 	@Test
 	public void execBatchTask() throws Exception {
         InetAddress inetAddress = InetAddress.getLocalHost();
         String hostname = inetAddress.getHostName();
         String ipAddress = inetAddress.getHostAddress();
 
         Context context = new ServiceContext("Who Is It?");
         context.putValue("requestor/message", new RequestorMessage("Hello Objects!"));
         context.putValue("requestor/hostname", hostname);
         context.putValue("requestor/address", ipAddress);
 
         Signature signature1 = new ObjectSignature("getHostAddress", WhoIsItProvider1.class);
         Signature signature2 = new ObjectSignature("getHostName", WhoIsItProvider1.class);
         Signature signature3 = new ObjectSignature("getCanonicalHostName", WhoIsItProvider1.class);
         Signature signature4 = new ObjectSignature("getTimestamp", WhoIsItProvider1.class);
 
         Task task = new ObjectTask("Who Is It?", signature1, signature2, signature3, signature4);
         task.setContext(context);
 
         Exertion result = task.exert();
         logger.info("task context: " + result.getContext());
         assertEquals(result.getContext().getValue("provider/hostname"), hostname);
         assertEquals(result.getContext().getValue("provider/address"), ipAddress);
     }
 
     @Test
     public void exertBatchTask() throws Exception {
         InetAddress inetAddress = InetAddress.getLocalHost();
         String hostname = inetAddress.getHostName();
         String ipAddress = inetAddress.getHostAddress();
         String providername = "*";
 
         Context context = new ServiceContext("Who Is It?");
         context.putValue("requestor/message", new RequestorMessage("Hello Network!"));
         context.putValue("requestor/hostname", hostname);
         context.putValue("requestor/address", ipAddress);
 
         Signature signature1 = new NetSignature("getHostAddress",
                 sorcer.ex1.WhoIsIt.class, providername, Type.PRE);
         Signature signature2 = new NetSignature("getHostName",
                 sorcer.ex1.WhoIsIt.class, providername, Type.SRV);
         Signature signature3 = new NetSignature("getCanonicalHostName",
                 sorcer.ex1.WhoIsIt.class, providername, Type.POST);
         Signature signature4 = new NetSignature("getTimestamp",
                 sorcer.ex1.WhoIsIt.class, providername, Type.POST);
 
         Task task = new NetTask("Who Is It?",
                 new Signature[] { signature1, signature2, signature3, signature4 },
                 context);
 
         Exertion result = task.exert();
         assertEquals(result.getContext().getValue("provider/hostname"), hostname);
         assertEquals(result.getContext().getValue("provider/address"), inetAddress.getHostAddress());
     }
 
 }
