 /**
  *
  * Copyright 2013 the original author or authors.
  * Copyright 2013, 2014 Sorcersoft.com S.A.
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
 
 package sorcer.ex2.requestor;
 
 import java.rmi.RMISecurityManager;
 import java.util.logging.Logger;
 
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.junit.runner.RunWith;
 import sorcer.core.SorcerEnv;
 import sorcer.core.context.ServiceContext;
 import sorcer.core.exertion.NetTask;
 import sorcer.core.requestor.ServiceRequestor;
 import sorcer.core.signature.NetSignature;
 import sorcer.junit.*;
 import sorcer.service.Context;
 import sorcer.service.Exertion;
 import sorcer.service.Task;
 import sorcer.util.Log;
 import sorcer.util.Sorcer;
 
 @RunWith(SorcerRunner.class)
 @Category(SorcerClient.class)
@ExportCodebase({
        "org.sorcersoft.sorcer:sorcer-api",
        "org.sorcersoft.sorcer:ex2-rdl",
        "org.sorcersoft.sorcer:ex2-api"
})
 @SorcerServiceConfiguration({
         ":ex2-cfg1",
         ":ex2-cfg2",
         ":ex2-cfg3"
 })
 public class WorkerTaskTest {
 
 	private static Logger logger = Log.getTestLog();
 
     @Test
 	public void workerTask() throws Exception {
 		System.setSecurityManager(new RMISecurityManager());
 		// initialize system properties
 		Sorcer.getEnvProperties();
         ServiceRequestor.prepareCodebase();
 
 		// get the queried provider name from the command line
 		String pn = "Worker1";
 
 		logger.info("Provider name: " + pn);
 
 		Exertion exertion = getExertion(pn);
 		Exertion result = exertion.exert(null);
 		logger.info("Output context: \n" + result.getContext());
         ExertionErrors.check(result.getExceptions());
 	}
 
 	private Exertion getExertion(String pn) throws Exception {
 		String hostname = SorcerEnv.getHostName();
 
         if (pn!=null) pn = SorcerEnv.getSuffixedName(pn);
         logger.info("Suffixed Provider name: " + pn);
 
         Context context = new ServiceContext("work");
         context.putValue("requstor/name", hostname);
         context.putValue("requestor/operand/1", 4);
         context.putValue("requestor/operand/2", 4);
         context.putValue("to/provider/name", pn);
         context.putValue("requestor/work", Works.work2);
 
 
         NetSignature signature = new NetSignature("doWork",
 				sorcer.ex2.provider.Worker.class, pn);
 
 		Task task = new NetTask("work", signature, context);
 
 		return task;
 	}
 }
