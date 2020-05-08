 package com.example.sorcer;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import sorcer.core.requestor.ServiceRequestor;
import sorcer.service.ContextException;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.SignatureException;
import sorcer.service.Task;
 
 import java.rmi.RMISecurityManager;
 
 import static sorcer.eo.operator.*;
 
 public class SampleServiceTester extends ServiceRequestor {
 
	private static Logger logger = LoggerFactory.getLogger(SampleServiceTester.class);
 
 	public Exertion getExertion(String... args) throws ExertionException,
                 ContextException, SignatureException {
 		System.setSecurityManager(new RMISecurityManager());
 		logger.info("Starting SampleServiceTester");
 
 		Task t1 = task("hello", sig("sayHelloWorld", SampleService.class),
 				context("Hello", in(path("in", "value"), "TESTER"), out(path("out", "value"), null)));
 
 		logger.info("Task t1 prepared: " + t1);
 		Exertion out = exert(t1);
 
 		logger.info("Got result: " + get(out, "out/value"));
 		logger.info("----------------------------------------------------------------");
 		logger.info("Task t1 trace: " + trace(out));
         return out;
     }
 }
