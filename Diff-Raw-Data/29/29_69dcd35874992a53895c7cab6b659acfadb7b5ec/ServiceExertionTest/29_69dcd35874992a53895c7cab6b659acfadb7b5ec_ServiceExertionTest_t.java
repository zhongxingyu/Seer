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
 import static sorcer.co.operator.list;
 import static sorcer.eo.operator.context;
 import static sorcer.eo.operator.cxt;
 import static sorcer.eo.operator.exert;
 import static sorcer.eo.operator.exertion;
 import static sorcer.eo.operator.exertions;
 import static sorcer.eo.operator.get;
 import static sorcer.eo.operator.in;
 import static sorcer.eo.operator.job;
 import static sorcer.eo.operator.jobContext;
 import static sorcer.eo.operator.name;
 import static sorcer.eo.operator.names;
 import static sorcer.eo.operator.out;
 import static sorcer.eo.operator.path;
 import static sorcer.eo.operator.pipe;
 import static sorcer.eo.operator.sig;
 import static sorcer.eo.operator.task;
 import static sorcer.eo.operator.xrt;
 
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.util.logging.Logger;
 
 import junit.sorcer.core.provider.AdderImpl;
 import junit.sorcer.core.provider.MultiplierImpl;
 import junit.sorcer.core.provider.SubtractorImpl;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import sorcer.core.SorcerEnv;
 import sorcer.core.provider.jobber.ServiceJobber;
 import sorcer.service.Context;
 import sorcer.service.ContextException;
 import sorcer.service.EvaluationException;
 import sorcer.service.Exertion;
 import sorcer.service.ExertionException;
 import sorcer.service.Job;
 import sorcer.service.Task;
 
 
 /**
  * @author Mike Sobolewski
  */
 
 public class ServiceExertionTest {
 	private final static Logger logger = Logger
 			.getLogger(ServiceExertionTest.class.getName());
 
 	private Exertion eTask, eJob;
 	// to avoid spelling errors in test cases define instance variables
 	private String arg = "arg", result = "result";
 	private String x1 = "x1", x2 = "x2", y = "y";
 	
 	static {
        System.setProperty("java.rmi.server.useCodebaseOnly", "false");
        System.setProperty("java.protocol.handler.pkgs", "net.jini.url|sorcer.util.bdb|org.rioproject.url");
        System.setProperty("java.security.policy", System.getenv("SORCER_HOME") + "/configs/sorcer.policy");
        System.setSecurityManager(new RMISecurityManager());
 		SorcerEnv.debug = true;
         SorcerEnv.setCodeBaseByArtifacts(new String[]{
                 "org.sorcersoft.sorcer:ju-arithmetic-api",
                 "org.sorcersoft.sorcer:sorcer-api"});
 	}
 	
 	@Before
 	public void setUp() throws Exception {
 		// create an exertions
 		eTask = createTask();	
 		eJob = createJob();
 	}
 	
 	@Test
 	public void exertTaskTest() throws ExertionException, ContextException {
 		eTask = exert(eTask);
 
 		// exert and them get the value from task's dataContext
 		//logger.info("eTask value @ result/y = " + get(exert(eTask), path(result, y)));
 		assertEquals(100.0, get(eTask, path(result, y)));
 		
 		//logger.info("eTask value @ arg/x1 = " + exert(eTask, path("arg/x1")));
 		assertEquals(20.0, get(eTask, path("arg/x1")));
 
 		//logger.info("eTask value @  arg/x2 = " + exert(eTask, "arg/x2"));
 		assertEquals(80.0, get(eTask, "arg/x2"));
 	}
 	
 	@Test
 	public void exertJobTest() throws ExertionException, ContextException {
 		// just get value from job's dataContext
 		logger.info("eJob value @  t3/arg/x2 = " + get(eJob, "j1/t3/arg/x2"));
 		assertEquals(Context.none, get(eJob, "/j1/t3/arg/x2") );
 		
 		// exert and then get the value from job's dataContext
 		eJob = exert(eJob);
 		logger.info("eJob: " + eJob);
 
 		logger.info("eJob jobContext: " + jobContext(eJob));
 		//logger.info("eJob value @  j2/t5/arg/x1 = " + get(eJob, "j2/t5/arg/x1"));
 		assertEquals(20.0, get(eJob, "/j1/j2/t5/arg/x1"));
 			
 		//logger.info("eJob value @ j2/t4/arg/x1 = " + exert(eJob, path("j1/j2/t4/arg/x1")));
 		assertEquals(10.0, get(eJob, "/j1/j2/t4/arg/x1"));
 
 		//logger.info("eJob value @  j1/j2/t5/arg/x2 = " + exert(eJob, "j1/j2/t5/arg/x2"));
 		assertEquals(80.0, get(eJob, "/j1/j2/t5/arg/x2"));
 		
 		//logger.info("eJob value @  j2/t5/arg/x1 = " + exert(eJob, "j2/t5/arg/x1"));
 		assertEquals(20.0, get(eJob, "/j1/j2/t5/arg/x1"));
 		
 		//logger.info("eJob value @  j2/t4/arg/x2 = " + exert(eJob, "j2/t4/arg/x2"));
 		assertEquals(50.0, get(eJob, "/j1/j2/t4/arg/x2"));
 			
 		logger.info("job dataContext: " + jobContext(eJob));
 		logger.info("value at j1/t3/result/y: " + get(eJob, "j1/t3/result/y"));
 		logger.info("value at t3, result/y: " + get(eJob, "t3", "result/y"));
 
 		// absolute path
 		assertEquals("Wrong value for 400.0", 400.0, get(eJob, "/j1/t3/result/y"));
 		//local t3 path
 		assertEquals("Wrong value for 400.0", 400.0, get(eJob, "t3", "result/y"));
 	}
 
 	@Test
 	public void accessingComponentExertionsTest() throws EvaluationException,
 			RemoteException, ExertionException {
 		//logger.info("eJob exertions: " + names(exertions(eJob)));
 		assertEquals(names(exertions(eJob)), list("t4", "t5", "j2", "t3", "j1"));
 		
 		//logger.info("t4 exertion: " + exertion(eJob, "t4"));
 		assertEquals(name(exertion(eJob, "j1/j2/t4")), "t4");
 		
 		//logger.info("j2 exertion: " + exertion(eJob, "j2"));
 		assertEquals(name(exertion(eJob, "j1/j2")), "j2");
 		
 		//logger.info("j2 exertion names: " + names(exertions(exertion(eJob, "j2"))));
 		assertEquals(names(exertions(exertion(eJob, "j1/j2"))), list("t4", "t5", "j2"));
 	}
 
 	// a simple task
 	private Exertion createTask() throws Exception {
 		
 //		Task task = task("t1", sig("add", Adder.class), 
 //		   dataContext("add", in(path(arg, x1), 20.0), in(path(arg, x2), 80.0),
 //		      out(path(result, y), null)));
 
 		return task("t1", sig("add", AdderImpl.class),
 				   context("add", in(path(arg, x1), 20.0), in(path(arg, x2), 80.0),
 				      out(path(result, y), null)));
 	}
 	
 	// two level job composition
 	private Exertion createJob() throws Exception {
 		Task t3 = task("t3", sig("subtract", SubtractorImpl.class),
 				context("subtract", in(path(arg, x1), null), in(path(arg, x2), null),
 						out(path(result, y), null)));
 
 		Task t4 = task("t4", sig("multiply", MultiplierImpl.class), 
 				context("multiply", in(path(arg, x1), 10.0), in(path(arg, x2), 50.0),
 						out(path(result, y), null)));
 
 		Task t5 = task("t5", sig("add", AdderImpl.class), 
 				context("add", in(path(arg, x1), 20.0), in(path(arg, x2), 80.0),
 						out(path(result, y), null)));
 
 		// Service Composition j1(j2(t4(x1, x2), t5(x1, x2)), t3(x1, x2))
 		//Job j1= job("j1", job("j2", t4, t5, strategy(Flow.PARALLEL, Access.PULL)), t3,
 		return job("j1", sig("execute", ServiceJobber.class),
 					job("j2", sig("execute", ServiceJobber.class), t4, t5),
 					t3,
 					pipe(out(t4, path(result, y)), in(t3, path(arg, x1))),
 					pipe(out(t5, path(result, y)), in(t3, path(arg, x2))));
 	}
 	
 	@Test
 	public void exertXrtTest() throws Exception {
 		Exertion xrt = createXrt();
 		logger.info("job dataContext " + ((Job)xrt).getJobContext());
 		
 		logger.info("xrt value @  t3/arg/x1 = " + get(xrt, "t3/arg/x1"));
 		logger.info("xrt value @  t3/arg/x2 = " + get(xrt, "t3/arg/x2"));
 		logger.info("xrt value @  t3/result/y = " + get(xrt, "t3/result/y"));
 
 		//assertTrue("Wrong xrt value for " + Context.Value.NULL, get(srv, "t3/arg/x2").equals(Context.Value.NULL));
 	}
 	
 	// two level job composition
 	@SuppressWarnings("unchecked")
 	private Exertion createXrt() throws Exception {
 		// using the data dataContext in jobs
 		Task t3 = xrt("t3", sig("subtract", SubtractorImpl.class), 
 				cxt("subtract", in("arg/x1", null), in("arg/x2", null),
 						out("result/y", null)));
 
 		Task t4 = xrt("t4", sig("multiply", MultiplierImpl.class), 
 				cxt("multiply", in("super/arg/x1"), in("arg/x2", 50.0),
 						out("result/y", null)));
 
 		Task t5 = xrt("t5", sig("add", AdderImpl.class), 
 				cxt("add", in("arg/x1", 20.0), in("arg/x2", 80.0),
 						out("result/y", null)));
 
 		// Service Composition j1(j2(t4(x1, x2), t5(x1, x2)), t3(x1, x2))
 		//Job j1= job("j1", job("j2", t4, t5, strategy(Flow.PARALLEL, Access.PULL)), t3,
 		return xrt("j1", sig("execute", ServiceJobber.class),
 					cxt(in("arg/x1", 10.0), out("job/result")),
 				xrt("j2", sig("execute", ServiceJobber.class), t4, t5),
 				t3,
 				pipe(out(t4, "result/y"), in(t3, "arg/x1")),
 				pipe(out(t5, "result/y"), in(t3, "arg/x2")));
 	}
 }
