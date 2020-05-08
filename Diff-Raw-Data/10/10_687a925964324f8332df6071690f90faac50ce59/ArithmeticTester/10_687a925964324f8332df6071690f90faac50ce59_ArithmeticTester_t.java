 package sorcer.arithmetic.requestor;
 
 import static sorcer.eo.operator.context;
 import static sorcer.eo.operator.control;
 import static sorcer.eo.operator.exert;
 import static sorcer.eo.operator.exertion;
 import static sorcer.eo.operator.get;
 import static sorcer.eo.operator.in;
 import static sorcer.eo.operator.input;
 import static sorcer.eo.operator.job;
 import static sorcer.eo.operator.jobContext;
 import static sorcer.eo.operator.name;
 import static sorcer.eo.operator.out;
 import static sorcer.eo.operator.output;
 import static sorcer.eo.operator.path;
 import static sorcer.eo.operator.pipe;
 import static sorcer.eo.operator.sig;
 import static sorcer.eo.operator.strategy;
 import static sorcer.eo.operator.task;
 import static sorcer.eo.operator.trace;
 
 import java.rmi.RMISecurityManager;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
//import java.util.logging.Logger;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import sorcer.arithmetic.provider.Adder;
 import sorcer.arithmetic.provider.Multiplier;
 import sorcer.arithmetic.provider.RemoteAdder;
 import sorcer.arithmetic.provider.Subtractor;
 import sorcer.core.SorcerConstants;
 import sorcer.core.requestor.ServiceRequestor;
 import sorcer.service.Accessor;
 import sorcer.service.ContextException;
 import sorcer.service.Exerter;
 import sorcer.service.Exertion;
 import sorcer.service.ExertionCallable;
 import sorcer.service.ExertionException;
 import sorcer.service.Job;
 import sorcer.service.SignatureException;
 import sorcer.service.Strategy.Access;
 import sorcer.service.Strategy.Flow;
 import sorcer.service.Strategy.Monitor;
 import sorcer.service.Strategy.Provision;
 import sorcer.service.Strategy.Wait;
 import sorcer.service.Task;
 
 /**
  * Testing parameter passing between tasks within the same service job. Two
  * numbers are added by the first task, then two numbers are multiplied by the
  * second one. The results of the first task and the second task are passed on
  * to the third task that subtracts the result of task two from the result of
  * task one. The {@link sorcer.core.context.PositionalContext} is used for requestor's
  * data in this test.
  * 
  * @see ArithmeticTester
  * @author Mike Sobolewski
  */
 
 public class ArithmeticTester implements SorcerConstants {
 
	private static Logger logger = LoggerFactory.getLogger(ArithmeticTester.class.getName());
 	
 	public static void main(String[] args) throws Exception {
 		System.setSecurityManager(new RMISecurityManager());
         // Resolve codebase from requestor.webster.codebase sysproperty specified in ant script to webster urls
         ServiceRequestor.prepareCodebase();
         //
 		logger.info("running: " + args[0]);
 		Exertion result = null;
 		ArithmeticTester tester = new ArithmeticTester();
 		if (args[0].equals("f5"))
 			result = tester.f5();
 		if (args[0].equals("f5m"))
 			result = tester.f5m();
 		else if (args[0].equals("f5inh"))
 			result = tester.f5inh();
 		else if (args[0].equals("f5a"))
 			result = tester.f5a();
 		else if (args[0].equals("f5pull"))
 			result = tester.f5pull();
 		else if (args[0].equals("f1"))
 			result = tester.f1();
 		else if (args[0].equals("f1a"))
 			result = tester.f1a();
 		else if (args[0].equals("f1b"))
 			result = tester.f1b();
 		else if (args[0].equals("f1c"))
 			result = tester.f1c();
 		else if (args[0].equals("f1PARpull"))
 			result = tester.f1PARpull();
 		else if (args[0].equals("f1SEQpull"))
 			result = tester.f1SEQpull();
 		else if (args[0].equals("f5xS"))
 			result = tester.f5xS(args[1]);
 		else if (args[0].equals("f5xP"))
 			result = tester.f5xP(args[1], args[2]);
 		else if (args[0].equals("f5exerter"))
 			result = tester.f5exerter();
 		
 //		logger.info(">>>>>>>>>>>>> exceptions: " + exceptions(result));
 //		logger.info(">>>>>>>>>>>>> result context: " + context(result));
 	}
 	
 	// two level composition
 	private Exertion f1a() throws Exception {
 		String arg = "arg", result = "result";
 		String x1 = "x1", x2 = "x2", y = "y";
 
 		Task f3 = task("f3", sig("multiply", Multiplier.class), 
 				   context("multiply", in(path(arg, x1), 10.0), in(path(arg, x2), 50.0),
 				      out(path(result, y), null)));
 		
 		Task f4 = task("f4", sig("add", Adder.class), 
 		   context("add", in(path(arg, x1), 20.0), in(path(arg, x2), 80.0),
 		      out(path(result, y), null)));
 		
 		Task f5 = task("f5", sig("subtract", Subtractor.class), 
 				   context("subtract", in(path(arg, x1), null), in(path(arg, x2), null),
 				      out(path(result, y), null)));
 
 		// Service Composition f1(f2(f3((x1, x2), f4(x1, x2)), f5(x1, x2))
 		//Job f1= job("f1", job("f2", f4, f5, strategy(Flow.PARALLEL, Access.PULL)), f3,
 		Job f1= job("f1", job("f2", f3, f4), f5, strategy(Provision.NO),
 		   pipe(out(f3, path(result, y)), in(f5, path(arg, x1))),
 		   pipe(out(f4, path(result, y)), in(f5, path(arg, x2))));
 
 		Exertion out = exert(f1);
 		if (out != null) {
 			logger.info("job f1 context: " + jobContext(out));
 			logger.info("job f1/f5/result/y: " + get(out, "f1/f5/result/y"));
 		} else {
 			logger.info("job execution failed");
 		}
 		
 		return out;
 	}
 	
 	// two level composition
 	private Exertion f1() throws Exception {
 		
 		Task f4 = task("f4", sig("multiply", Multiplier.class),
 				context("multiply", input("arg/x1", 10.0d), input("arg/x2", 50.0d),
 						out("result/y1", null)));
 
 		Task f5 = task("f5", sig("add", Adder.class),
 				context("add", input("arg/x3", 20.0d), input("arg/x4", 80.0d),
 						output("result/y2", null)));
 
 		Task f3 = task("f3", sig("subtract", Subtractor.class),
 				context("subtract", input("arg/x5", null), input("arg/x6", null),
 						output("result/y3", null)));
 
 		//job("f1", job("f2", f4, f5), f3,		
 		//job("f1", job("f2", f4, f5, strategy(Flow.PAR, Access.PULL)), f3,
 		Job f1 = job("f1", job("f2", f4, f5), f3, strategy(Provision.NO),
 				pipe(out(f4, "result/y1"), in(f3, "arg/x5")),
 				pipe(out(f5, "result/y2"), in(f3, "arg/x6")));
 
 		Exertion out = exert(f1);
 		if (out != null) {
 			logger.info("job f1 context: " + jobContext(out));
 			logger.info("job f1/f3/result/y3: " + get(out, "f1/f3/result/y3"));
 		} else {
 			logger.info("job execution failed");
 		}
 		
 		return out;
 	}
 	
 	// one level composition
 	private Exertion f1b() throws Exception {
 		String arg = "arg", result = "result";
 		String x1 = "x1", x2 = "x2", y = "y";
 
 		Task f3 = task("f3", sig("subtract", Subtractor.class), 
 		   context("subtract", in(path(arg, x1), null), in(path(arg, x2), null),
 		      out(path(result, y), null)));
 		
 		Task f4 = task("f4", sig("multiply", Multiplier.class), 
 				   context("multiply", in(path(arg, x1), 10.0), in(path(arg, x2), 50.0),
 				      out(path(result, y), null)));
 		
 		Task f5 = task("f5", sig("add", Adder.class), 
 		   context("add", in(path(arg, x1), 20.0), in(path(arg, x2), 80.0),
 		      out(path(result, y), null)));
 
 		// Service Composition f1(f4(x1, x2), f5(x1, x2), f3(x1, x2))
 		Job f1= job("f1", f4, f5, f3,
 		   pipe(out(f4, path(result, y)), in(f3, path(arg, x1))),
 		   pipe(out(f5, path(result, y)), in(f3, path(arg, x2))));
 
 		Exertion out = exert(f1);
 		logger.info("job f1 context: " + jobContext(out));
 		logger.info("job f1 f1/f3/result/y: " + get(out, path("f1", "f3", result, y)));
 		
 		return out;
 	}
 	
 	// job composed of job and task with PUSH/SEQ strategy
 	private Exertion f1c() throws Exception {
 		
 		Task f4 = task("f4", sig("multiply", Multiplier.class), 
 				context("multiply", in(path("arg/x1"), 10.0), in(path("arg/x2"), 50.0),
 						out(path("result/y1"), null)));
 
 		Task f5 = task("f5", sig("add", Adder.class), 
 				context("add", in(path("arg/x3"), 20.0), in(path("arg/x4"), 80.0),
 						out(path("result/y2"), null)));
 
 		Task f3 = task("f3", sig("subtract", Subtractor.class), 
 				context("subtract", in(path("arg/x5"), null), in(path("arg/x6"), null),
 						out(path("result/y3"), null)));
 
 		// Service Composition f1(f2(x1, x2), f3(x1, x2))
 		// Service Composition f2(f4(x1, x2), f5(x1, x2))
 		//Job f1= job("f1", job("f2", f4, f5, strategy(Flow.PAR, Access.PULL)), f3,
 		Job f1= job("f1", job("f2", f4, f5), f3,
 				pipe(out(f4, path("result/y1")), in(f3, path("arg/x5"))),
 				pipe(out(f5, path("result/y2")), in(f3, path("arg/x6"))));
 
 		Exertion out = exert(f1);
 
 		logger.info("job f1 context: " + jobContext(out));
 		logger.info("job f1 f3/result/y3: " + get(out, path("f1/f3/result/y3")));
 
 		return out;
 	}
 	
 	// composition with mixed flow/access strategy
 	private Exertion f1PARpull() throws Exception {
 		
 		Task f4 = task("f4", sig("multiply", Multiplier.class), 
 				context("multiply", in(path("arg/x1"), 10.0), in(path("arg/x2"), 50.0),
 						out(path("result/y1"), null)), Access.PULL);
 
 		Task f5 = task("f5", sig("add", Adder.class), 
 				context("add", in(path("arg/x3"), 20.0), in(path("arg/x4"), 80.0),
 						out(path("result/y2"), null)), Access.PULL);
 
 		Task f3 = task("f3", sig("subtract", Subtractor.class), 
 				context("subtract", in(path("arg/x5"), null), in(path("arg/x6"), null),
 						out(path("result/y3"), null)));
 
 		// Service Composition f1(f2(x1, x2), f3(x1, x2))
 		// Service Composition f2(f4(x1, x2), f5(x1, x2))
 		//Job f1= job("f1", job("f2", f4, f5, strategy(Flow.PAR, Access.PULL)), f3,
 		Job f1= job("f1", job("f2", f4, f5, strategy(Access.PULL, Flow.PAR)), f3,
 				pipe(out(f4, path("result/y1")), in(f3, path("arg/x5"))),
 				pipe(out(f5, path("result/y2")), in(f3, path("arg/x6"))));
 
 		long start = System.currentTimeMillis();
 		Exertion out = exert(f1);
 		long end = System.currentTimeMillis();
 		System.out.println("Execution time: " + (end-start) + " ms.");
 		
 		logger.info("out name: " + name(out));
 		logger.info("job f1 context: " + context(out));
 		logger.info("job f1 job context: " + jobContext(out));
 		logger.info("job f1 f3/result/y3: " + get(out, path("f1/f3/result/y3")));
 		logger.info("task f4 trace: " + trace(exertion(out, "f1/f2/f4")));
 		logger.info("task f5 trace: " + trace(exertion(out, "f1/f2/f5")));
 		logger.info("task f3 trace: " +  trace(exertion(out, "f1/f3")));
 		logger.info("task f2 trace: " +  trace(exertion(out, "f1/f2")));
 		logger.info("task f1 trace: " +  trace(out));
 		return out;
 	}
 	
 private Exertion f1SEQpull() throws Exception {
 		
 		Task f4 = task("f4", sig("multiply", Multiplier.class), 
 				context("multiply", in(path("arg/x1"), 10.0), in(path("arg/x2"), 50.0),
 						out(path("result/y1"), null)), Access.PULL);
 
 		Task f5 = task("f5", sig("add", Adder.class), 
 				context("add", in(path("arg/x3"), 20.0), in(path("arg/x4"), 80.0),
 						out(path("result/y2"), null)), Access.PULL);
 
 		Task f3 = task("f3", sig("subtract", Subtractor.class), 
 				context("subtract", in(path("arg/x5"), null), in(path("arg/x6"), null),
 						out(path("result/y3"), null)));
 
 		// Service Composition f1(f2(x1, x2), f3(x1, x2))
 		// Service Composition f2(f4(x1, x2), f5(x1, x2))
 		//Job f1= job("f1", job("f2", f4, f5, strategy(Flow.PAR, Access.PULL)), f3,
 		Job f1= job("f1", job("f2", f4, f5, strategy(Access.PULL, Flow.SEQ)), f3,
 				pipe(out(f4, path("result/y1")), in(f3, path("arg/x5"))),
 				pipe(out(f5, path("result/y2")), in(f3, path("arg/x6"))));
 		
 		long start = System.currentTimeMillis();
 		Exertion out = exert(f1);
 		long end = System.currentTimeMillis();
 		System.out.println("Execution time: " + (end-start) + " ms.");
 		
 		logger.info("out name: " + name(out));
 		logger.info("job f1 context: " + context(out));
 		logger.info("job f1 job context: " + jobContext(out));
 		logger.info("job f1 f3/result/y3: " + get(out, path("f1/f3/result/y3")));
 		logger.info("task f4 trace: " + trace(exertion(out, "f1/f2/f4")));
 		logger.info("task f5 trace: " + trace(exertion(out, "f1/f2/f5")));
 		logger.info("task f3 trace: " +  trace(exertion(out, "f1/f3")));
 		logger.info("task f2 trace: " +  trace(exertion(out, "f1/f2")));
 		logger.info("task f1 trace: " +  trace(out));
 		return out;
 	}
 
 	private Exertion f5() throws Exception {
 		Task f5 = task(
 				"f5",
 				sig("add", Adder.class),
 				context("add", in("arg/x1", 20.0),
 						in("arg/x2", 80.0), out("result/y", null)),
 				strategy(Monitor.NO, Wait.YES, Provision.YES));
 		
 		Exertion out = null;
 		long start = System.currentTimeMillis();
 		out = exert(f5);
 		long end = System.currentTimeMillis();
 		System.out.println("Execution time: " + (end-start) + " ms.");
 		logger.info("task f5 context: " + context(out));
 		logger.info("task f5 result/y: " + get(context(out), "result/y"));
 
 		return out;
 	}
 
 	private Exertion f5exerter() throws Exception {
 		Task f5 = task(
 				"f5",
 				sig("add", Adder.class),
 				context("add", in("arg/x1", 20.0),
 						in("arg/x2", 80.0), out("result/y", null)),
 				strategy(Monitor.NO, Wait.YES));
 		
 		Exertion out = null;
 		long start = System.currentTimeMillis();
 		Exerter exerter = Accessor.getService(Exerter.class);
 		logger.info("got exerter: " + exerter);
 
 		out = exerter.exert(f5);
 		long end = System.currentTimeMillis();
 		
 		if (out.getExceptions().size() > 0) {
 			System.out.println("Execeptins: " + out.getExceptions());
 			return out;
 		}
 			
 		System.out.println("Execution time by exerter: " + (end-start) + " ms.");
 		logger.info("task f5 context: " + context(out));
 		logger.info("task f5 result/y: " + get(context(out), "result/y"));
 
 		return out;
 	}
 	
 	private Exertion f5m() throws Exception {
 		Task f5 = task(
 				"f5",
 				sig("add", Adder.class),
 				context("add", in("arg/x1", 20.0),
 						in("arg/x2", 80.0), out("result/y", null)),
 				strategy(Monitor.YES, Wait.YES));
 		
 		Exertion out = null;
 		long start = System.currentTimeMillis();
 		out = exert(f5);
 		long end = System.currentTimeMillis();
 		System.out.println("Execution time: " + (end-start) + " ms.");
 		logger.info("task f5 context: " + context(out));
 		logger.info("task f5 result/y: " + get(context(out), "result/y"));
 
 		return out;
 	}
 
 	
 	private Exertion f5inh() throws Exception {
 		
 		Task f5 = task(
 				"f5",
 				sig("add", RemoteAdder.class),
 				context("add", in("arg/x1", 20.0),
 						in("arg/x2", 80.0), out("result/y", null)),
 				strategy(Monitor.YES, Wait.YES));
 		
 		Exertion out = null;
 		long start = System.currentTimeMillis();
 		out = exert(f5);
 		long end = System.currentTimeMillis();
 		System.out.println("Execution time: " + (end-start) + " ms.");
 		logger.info("task f5 context: " + context(out));
 		logger.info("task f5 result/y: " + get(context(out), "result/y"));
 
 		return out;
 	}
 
 	private Exertion f5pull() throws Exception {
 
 		Task f5 = task(
 				"f5",
 				sig("add", Adder.class),
 				context("add", in("arg/x1", 20.0),
 						in("arg/x2", 80.0), out("result/y", null)),
 				strategy(Access.PULL, Wait.YES));
 		
 		Exertion out = null;
 		long start = System.currentTimeMillis();
 		out = exert(f5);
 		long end = System.currentTimeMillis();
 		System.out.println("Execution time: " + (end-start) + " ms.");
 		logger.info("task f5 context: " + context(out));
 		logger.info("task f5 control: " + control(out));
 		logger.info("task f5 result/y: " + get(context(out), "result/y"));
 
 		return out;
 	}
 	
 	private Exertion f5a() throws Exception {
 
 		Task f5 = task(
 				"f5",
 				sig("add", Adder.class),
 				context("add", in("arg/x1", 20.0),
 						in("arg/x2", 80.0), out("result/y", null)),
 				strategy(Monitor.YES, Wait.NO));
 		
 		logger.info("task f5 control context: " + control(f5));
 		
 		Exertion out = null;
 		long start = System.currentTimeMillis();
 		out = exert(f5);
 		long end = System.currentTimeMillis();
 		System.out.println("Execution time: " + (end-start) + " ms.");
 		logger.info("task f5 context: " + context(out));
 		logger.info("task f5 result/y: " + get(context(out), "result/y"));
 
 		return out;
 	}
     // SET Provisioning to false!!!
 	private Exertion f5xS(String repeat) throws Exception {
 		int to = new Integer(repeat);
 		
 		Task f5 = task("f5", sig("add", Adder.class), 
 		   context("add", in("arg/x1", 20.0), in("arg/x2", 80.0),
 		      out("result/y", null),
 		      strategy(Access.PULL, Wait.YES, Provision.FALSE)));
         Job f1= job("f1", strategy(Access.PULL, Provision.FALSE, Flow.SEQ), f5);
 
 		Exertion out = null;
 		for (int i = 0; i < to; i++) {
             f5 = task("f5-" + i, sig("add", Adder.class),
                     context("f5-" +i, in("arg/x1", 20.0), in("arg/x2", 80.0),
                             out("result/y", null),
                             strategy(Access.PULL, Wait.YES, Provision.FALSE)));
             f5.setAccess(Access.PULL);
 			//f5.setName("f5-" + i);
 			//f5.getContext().setName("f5-" + i);
             f1.addExertion(f5);
 			//out = exert(f5);
 			System.out.println("out context: " + name(f5) + "\n" + context(out));
 		}
         long start = System.currentTimeMillis();
         out = exert(f1);
         for (Exertion inExt : out.getExertions())
             System.out.println("out context: " + name(inExt) + "\n" + context(inExt));
 
         //System.out.println("job context: " + name(f1) + "\n" + context(f1));
         long end = System.currentTimeMillis();
 		System.out.println("Execution time: " + (end-start) + " ms.");
 		return out;
 	}
 	
 	private Task getTask() throws ExertionException, SignatureException,
 			ContextException {
 		
 		Task f5 = task(
 				"f5",
 				sig("add", Adder.class),
 				context("add", in("arg/x1", 20.0),
 						in("arg/x2", 80.0), out("result/y", null)));
 		return f5;
 	}
 
     // SET Provisioning to false!!!
 	private Exertion f5xP(String poolSizeStr, String size) throws Exception {
 		int poolSize = new Integer(size);
 		int tally = new Integer(size);
 		Task task = null;
 		ExertionCallable ec = null;
 		long start = System.currentTimeMillis();
 		List<Future<Exertion>> fList = new ArrayList<Future<Exertion>>(tally);
 		ExecutorService pool = Executors.newFixedThreadPool(poolSize);
 		for (int i = 0; i < tally; i++) {
 			task = getTask();
 			task.getControlContext().setAccessType(Access.PULL);
             task.getControlContext().setProvisionable(false);
 			task.setName("f5-" + i);
 			ec = new ExertionCallable(task);
 			logger.info("exertion submit: " + task.getName());
 			Future<Exertion> future = pool.submit(ec);
 			fList.add(future);
 		}
 		pool.shutdown();
 		for (int i = 0; i < tally; i++) {
 			logger.info("got future value for: " + fList.get(i).get().getName());
 		}
 		long end = System.currentTimeMillis();
 		System.out.println("Execution time: " + (end - start) + " ms.");
 		logger.info("got last context #" + tally + "\n" + fList.get(tally-1).get().getContext());
 		logger.info("run in parallel: " + tally);
 		return fList.get(tally-1).get();
 	}
 	
 }
