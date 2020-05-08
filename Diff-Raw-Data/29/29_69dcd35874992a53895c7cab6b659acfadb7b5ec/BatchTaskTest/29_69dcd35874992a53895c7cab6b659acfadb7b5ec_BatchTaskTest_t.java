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
 package junit.sorcer.core.exertion;
 
 import static org.junit.Assert.assertEquals;
 import static sorcer.co.operator.from;
 import static sorcer.eo.operator.context;
 import static sorcer.eo.operator.exert;
 import static sorcer.eo.operator.get;
 import static sorcer.eo.operator.in;
 import static sorcer.eo.operator.result;
 import static sorcer.eo.operator.sig;
 import static sorcer.eo.operator.task;
 import static sorcer.eo.operator.type;
 
 import java.rmi.RMISecurityManager;
 
 import junit.sorcer.core.provider.AdderImpl;
 import junit.sorcer.core.provider.MultiplierImpl;
 import junit.sorcer.core.provider.SubtractorImpl;
 
 import org.junit.Test;
 
 import sorcer.core.SorcerEnv;
 import sorcer.service.Signature;
 import sorcer.service.Direction;
 import sorcer.service.Task;
 
 
 /**
  * @author Mike Sobolewski
  */
 public class BatchTaskTest {
 	static {
        System.setProperty("java.rmi.server.useCodebaseOnly", "false");
        System.setProperty("java.protocol.handler.pkgs", "net.jini.url|sorcer.util.bdb|org.rioproject.url");
        System.setProperty("java.security.policy", System.getenv("SORCER_HOME") + "/configs/sorcer.policy");
        System.setSecurityManager(new RMISecurityManager());
        System.setProperty("java.util.logging.config.file",
                System.getenv("SORCER_HOME") + "/configs/sorcer.logging");
        SorcerEnv.debug = true;
        SorcerEnv.setCodeBaseByArtifacts(new String[]{
                 "org.sorcersoft.sorcer:ju-arithmetic-api",
                "org.sorcersoft.sorcer:sorcer-api"});
 	}
 
 	@Test
 	public void batchTask3Test() throws Exception {
 		// batch for the composition f1(f2(f3((x1, x2), f4(x1, x2)), f5(x1, x2))
 		// shared dataContext with named paths
 		Task batch3 = task("batch3",
 				type(sig("multiply", MultiplierImpl.class, result("subtract/x1", Direction.IN)), Signature.PRE),
 				type(sig("add", AdderImpl.class, result("subtract/x2", Direction.IN)), Signature.PRE),
 				sig("subtract", SubtractorImpl.class, result("result/y", from("subtract/x1", "subtract/x2"))),
 				context(in("multiply/x1", 10.0), in("multiply/x2", 50.0), 
 						in("add/x1", 20.0), in("add/x2", 80.0)));
 		
 		batch3 = exert(batch3);
 		//logger.info("task result/y: " + get(batch3, "result/y"));
 		assertEquals("Wrong value for 400.0", 400.0, get(batch3, "result/y"));
 	}
 	
 	
 	@Test
 	public void batchTask4Test() throws Exception {
 		// batch for the composition f1(f2(f3((x1, x2), f4(x1, x2)), f5(x1, x2))
 		// shared dataContext with prefixed paths
 		Task batch3 = task("batch3",
 				type(sig("multiply#op1", MultiplierImpl.class, result("op3/x1", Direction.IN)), Signature.PRE),
 				type(sig("add#op2", AdderImpl.class, result("op3/x2", Direction.IN)), Signature.PRE),
 				sig("subtract", SubtractorImpl.class, result("result/y", from("op3/x1", "op3/x2"))),
 				context(in("op1/x1", 10.0), in("op1/x2", 50.0), 
 						in("op2/x1", 20.0), in("op2/x2", 80.0)));
 		
 		batch3 = exert(batch3);
 		//logger.info("task result/y: " + get(batch3, "result/y"));
 		assertEquals("Wrong value for 400.0", 400.0, get(batch3, "result/y"));
 	}
 }
 	
