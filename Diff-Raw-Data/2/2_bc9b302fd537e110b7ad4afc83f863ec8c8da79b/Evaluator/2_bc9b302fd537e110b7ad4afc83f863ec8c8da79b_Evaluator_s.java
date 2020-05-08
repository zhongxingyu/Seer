 package de.prob.web;
 
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.be4.classicalb.core.parser.exceptions.BException;
 import de.be4.classicalb.core.parser.exceptions.BParseException;
 import de.prob.ProBException;
 import de.prob.animator.domainobjects.EvaluationResult;
 import de.prob.statespace.StateSpace;
 
 public class Evaluator {
 	private static final int TIMEOUT = 3;
 	private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;
 	private static final String TIMEOUT_MESSAGE = "Could not complete calculation within the time constraints. ("
 			+ TIMEOUT + " " + TIMEOUT_UNIT + ")";
 
 	private final static Logger logger = LoggerFactory
 			.getLogger(Evaluator.class);
 
 	public final StateSpace space;
 	private volatile boolean busy;
 
 	public Evaluator(StateSpace space) {
 		this.space = space;
 	}
 
 	public String eval(final String formula) throws BParseException {
 		ExecutorService executor = Executors.newSingleThreadExecutor();
 		Future<String> future = executor.submit(new Callable<String>() {
 			@Override
 			public String call() throws Exception {
 				try {
 					List<EvaluationResult> evaluate = space.evaluate(formula);
 					EvaluationResult first = evaluate.get(0);
 					return first.toString();
 				} catch (ProBException e) {
 					return "ERROR: " + e.getMessage();
 				}
 			}
 		});
 
 		try {
 			this.busy = true;
 			String value = future.get(TIMEOUT, TIMEOUT_UNIT);
 			logger.trace("Result {} ", value);
 			return value;
 		} catch (TimeoutException e) {
 			space.sendInterrupt();
 			logger.debug("Timeout while calculating '{}'.", formula);
 			return TIMEOUT_MESSAGE;
 		} catch (InterruptedException e) {
 			logger.error("Interrupt Exception ", e);
 			return "ERROR: " + e.getMessage();
 		} catch (ExecutionException e) {
 			Throwable cause = e.getCause();
 			if (cause instanceof BException) {
 				throw (BParseException) cause.getCause();
 			}
 			return "EXECUTION ERROR";
 		} finally {
 			executor.shutdownNow();
 			this.busy = false;
 		}
 	}
 
 	public boolean isBusy() {
 		return busy;
 	}
 
 }
