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
 package sorcer.arithmetic.requestor;
 
 import org.junit.Test;
 import sorcer.core.SorcerEnv;
 import sorcer.core.context.ServiceContext;
 import sorcer.core.exertion.NetTask;
 import sorcer.core.signature.NetSignature;
 import sorcer.service.Context;
 import sorcer.service.Exerter;
 import sorcer.service.Job;
 import sorcer.service.Task;
 
 
 import java.rmi.RMISecurityManager;
 import java.util.logging.Logger;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * @author Mike Sobolewski
  */
 @SuppressWarnings({ "rawtypes" })
 public class ArithmeticExerterTest {
 
 	private final static Logger logger = Logger
 			.getLogger(ArithmeticExerterTest.class.getName());
 
 	static {
         System.setProperty("java.security.policy", System.getenv("SORCER_HOME")
                 + "/configs/sorcer.policy");
         System.setSecurityManager(new RMISecurityManager());
         SorcerEnv.setCodeBaseByArtifacts(new String[]{
                 "org.sorcersoft.sorcer:sos-platform",
                 "org.sorcersoft.sorcer:ex6-api" });
         System.out.println("CLASSPATH :" + System.getProperty("java.class.path"));
 	}
 	
 	@Test
 	public void exertExerter() throws Exception {
 		Job exertion = NetArithmeticReqTest.getJobComposition();
 		Task task = new NetTask("exert", new NetSignature("exert",
 				Exerter.class),
 				new ServiceContext(exertion));
 		Task result = (Task) task.exert();
 		// logger.info("result: " + result);
 		// logger.info("return value: " + result.getReturnValue());
 	
		Context out = (Context) result.getContext();
 //		logger.info("out context: " + out);
		logger.info("1job1task/subtract/result/value: "
 				+ out.getValue(
						"1job1task/subtract/result/value"));
		assertEquals(
				out.getValue("1job1task/subtract/result/value"),
				400.0);
 	}
 }
