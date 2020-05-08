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
 
 import org.junit.Ignore;
 import org.junit.Test;
 import sorcer.arithmetic.provider.Adder;
 import sorcer.arithmetic.provider.Multiplier;
 import sorcer.arithmetic.provider.Subtractor;
 import sorcer.core.SorcerEnv;
 import sorcer.core.context.PositionalContext;
 import sorcer.core.exertion.NetJob;
 import sorcer.core.exertion.NetTask;
 import sorcer.core.signature.NetSignature;
 import sorcer.service.Context;
 import sorcer.service.Direction;
 import sorcer.service.Job;
 import sorcer.service.Signature;
 import sorcer.service.Task;
 
 
 import java.rmi.RMISecurityManager;
 import java.util.logging.Logger;
 
 import static org.junit.Assert.assertEquals;
 import static sorcer.co.operator.from;
 import static sorcer.eo.operator.*;
 
 /**
  * @author Mike Sobolewski
  */
 @SuppressWarnings({ "rawtypes" })
 public class NetArithmeticReqTest {
 
 	private final static Logger logger = Logger
 			.getLogger(NetArithmeticReqTest.class.getName());
 
 	static {
         System.setProperty("java.security.policy", System.getenv("SORCER_HOME")
                 + "/configs/sorcer.policy");
         System.setSecurityManager(new RMISecurityManager());
         SorcerEnv.setCodeBaseByArtifacts(new String[]{
                 "org.sorcersoft.sorcer:sos-platform",
                 "org.sorcersoft.sorcer:ex6-api"});
        //System.setProperty("java.protocol.handler.pkgs", "net.jini.url|sorcer.util.bdb.sos|org.rioproject.url");
        //System.setProperty("java.rmi.server.RMIClassLoaderSpi", "org.rioproject.rmi.ResolvingLoader");
         System.out.println("CLASSPATH :" + System.getProperty("java.class.path"));
     }
 
     @Ignore
     @Test
     public void batchTaskTest() throws Exception {
         // batch for the composition f1(f2(f3((x1, x2), f4(x1, x2)), f5(x1, x2))
         // shared context with named paths
         Task batch3 = task("batch3",
                 type(sig("multiply", Multiplier.class, result("subtract/x1", Direction.IN)), Signature.PRE),
                 type(sig("add", Adder.class, result("subtract/x2", Direction.IN)), Signature.PRE),
                 sig("subtract", Subtractor.class, result("result/y", from("subtract/x1", "subtract/x2"))),
                 context(in("multiply/x1", 10.0), in("multiply/x2", 50.0),
                         in("add/x1", 20.0), in("add/x2", 80.0)));
 
         batch3 = exert(batch3);
         //logger.info("task result/y: " + get(batch3, "result/y"));
         assertEquals("Wrong value for 400.0", 400.0, get(batch3, "result/y"));
     }
 
     @Ignore
     @Test
     public void batchPrefixedTaskTest() throws Exception {
         // batch for the composition f1(f2(f3((x1, x2), f4(x1, x2)), f5(x1, x2))
         // shared context with prefixed paths
         Task batch3 = task("batch3",
                 type(sig("multiply#op1", Multiplier.class, result("op3/x1", Direction.IN)), Signature.PRE),
                 type(sig("add#op2", Adder.class, result("op3/x2", Direction.IN)), Signature.PRE),
                 sig("subtract", Subtractor.class, result("result/y", from("op3/x1", "op3/x2"))),
                 context(in("op1/x1", 10.0), in("op1/x2", 50.0),
                         in("op2/x1", 20.0), in("op2/x2", 80.0)));
 
         batch3 = exert(batch3);
         //logger.info("task result/y: " + get(batch3, "result/y"));
         assertEquals("Wrong value for 400.0", 400.0, get(batch3, "result/y"));
     }
 
     @Test
     public void exertTaskJob() throws Exception {
         Job job = getTaskedNetJob();
         NetJob result = (NetJob)job.exert();
         logger.info("result context: "  + result.getComponentContext("3tasks/subtract"));
         logger.info("job context: " + result.getJobContext());
 		assertEquals(400.0, result.getValue("3tasks/subtract/result/value"));
     }
 
 	@Test
 	public void exertJobComposition() throws Exception {
 		Job job = getJobComposition();
 		Job result = (NetJob) job.exert();
 		logger.info("result context: " + result.getComponentContext("3tasks/subtract"));
 		logger.info("job context: " + result.getJobContext());
 		assertEquals(400.0, result.getValue("1job1task/subtract/result/value"));
 	}
 
 	public static Job getJobComposition() throws Exception {
 		Task task1 = getAddTask();
 		Task task2 = getMultiplyTask();
 		Task task3 = getSubtractTask();
 
 		Job internal = new NetJob("2tasks");
 		internal.addExertion(task2);
 		internal.addExertion(task1);
 		
 		Job job = new NetJob("1job1task");
 		job.addExertion(internal);
 		job.addExertion(task3);
 		
 		// make the result of second task as the first argument of task
 		// three
 		task2.getContext().connect("out/value", "arg1/value", task3.getContext());
 		// make the result of the first task as the second argument of task
 		// three
 		task1.getContext().connect("out/value", "arg2/value", task3.getContext());
 		
 		return job;
 	}
 	
 	public static Job getTaskedNetJob() throws Exception {
 		Task task1 = getAddTask();
 		Task task2 = getMultiplyTask();
 		Task task3 = getSubtractTask();
 
 		Job job = new NetJob("3tasks");
 		job.addExertion(task1);
 		job.addExertion(task2);
 		job.addExertion(task3);
 		
 		// make the result of second task as the first argument of task
 		// three
 		task2.getContext().connect("out/value", "arg1/value", task3.getContext());
 		// make the result of the first task as the second argument of task
 		// three
 		task1.getContext().connect("out/value", "arg2/value", task3.getContext());
 		
 		return job;
 	}
 	private static Task getAddTask() throws Exception {
 		Context context = new PositionalContext("add");
 		context.putInValue("arg1/value", 20.0);
 		context.putInValue("arg2/value", 80.0);
 		// We know that the output is gonna be placed in this path
 		context.putOutValue("out/value", 0);
 		NetSignature method = new NetSignature("add", Adder.class);
 		Task task = new NetTask("add", method);
 		task.setContext(context);
 		return task;
 	}
 
 	private static Task getMultiplyTask() throws Exception {
 		Context context = new PositionalContext("multiply");
 		context.putInValue("arg1/value", 10.0);
 		context.putInValue("arg2/value", 50.0);
 		// We know that the output is gonna be placed in this path
 		context.putOutValue("out/value", 0);
 		NetSignature method = new NetSignature("multiply", Multiplier.class);
 		Task task = new NetTask("multiply", method);
 		task.setContext(context);
 		return task;
 	}
 
 	private static Task getSubtractTask() throws Exception {
 		PositionalContext context = new PositionalContext("subtract");
 		// We want to stick in the result of multiply in here
 		context.putInValueAt("arg1/value", 0.0, 1);
 		// We want to stick in the result of add in here
 		context.putInValueAt("arg2/value", 0.0, 2);
 		NetSignature method = new NetSignature("subtract", Subtractor.class);
 		Task task = new NetTask("subtract",
 				"processing results from two previous tasks", method);
 		task.setContext(context);
 		return task;
 	}
 }
