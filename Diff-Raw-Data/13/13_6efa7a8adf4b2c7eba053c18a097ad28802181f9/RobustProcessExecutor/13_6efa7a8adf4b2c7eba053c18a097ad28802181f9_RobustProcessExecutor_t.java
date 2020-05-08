 package upsilon;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.joda.time.Duration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import upsilon.dataStructures.ResultKarma;
 import upsilon.dataStructures.StructureService;
 import upsilon.util.GlobalConstants;
 import upsilon.util.Util;
 
 public class RobustProcessExecutor implements Callable<Integer> {
 
 	private final Logger log = LoggerFactory.getLogger("RPE");
 	private Process p;
 	private Future<Integer> future;
 	private final Duration timeout;
 	private final StructureService service;
 
 	private final String[] executableAndArguments;
 
 	public static final ExecutorService monitoringThreadPool = Executors.newFixedThreadPool(6, Util.getThreadFactory("RPE monitor"));
 	public static final ExecutorService executingThreadPool = Executors.newFixedThreadPool(6, Util.getThreadFactory("RPE executor"));
 
 	public RobustProcessExecutor(final StructureService service) {
 		this.service = service;
 		this.timeout = service.getTimeout();
 		this.executableAndArguments = service.getCommand().getFinalCommandLinePieces(service);
 	}
 
 	@Override
 	public Integer call() throws IllegalThreadStateException, InterruptedException {
 		this.p.waitFor();

 		return this.p.exitValue();
 	}
 
 	public void destroy() {
 		// On some old JDKs, processes and their streams can be set
 		// to NULL before this method is called, hence the many NULL
 		// checks to do our best to clean up after ourselves, rather
 		// than throwing and leaking file handles.
 		try {
 			if (this.p == null) {
 				return;
 			}
 
 			if (this.p.getInputStream() != null) {
 				this.p.getInputStream().close();
 			}
 
 			if (this.p.getErrorStream() != null) {
 				this.p.getErrorStream().close();
 			}
 
 			if (this.p.getOutputStream() != null) {
 				this.p.getOutputStream().close();
 			}
 		} catch (final IOException e) {
 			this.log.error("Could not close a stream associated with a process, this instance of Upsilon will probably leak file handles!", e);
 		}
 
 		this.p.destroy();
 		this.p = null;
 		this.future = null;
 	}
 
	private void exec() throws IOException {
 		this.log.debug("Executing " + this.service.getIdentifier() + ": " + Arrays.toString(this.executableAndArguments));
 
 		this.p = Runtime.getRuntime().exec(this.executableAndArguments);
 	}
 
 	public void execAsync() {
 		if (this.service.getDependancy() != null) {
 			if (this.service.getDependancy().getResult() != ResultKarma.GOOD) {
 				final String msg = "Skipped, because of parent failure: " + this.service.getDependancy().getIdentifier();
 
 				this.service.addResult(ResultKarma.SKIPPED, msg);
 				this.log.info(msg);
 
 				return;
 			}
 		}
 
 		final Runnable monitoringThread = new Runnable() {
 			@Override
 			public void run() {
 				try {
 					RobustProcessExecutor.this.exec();
 
 					final String output = RobustProcessExecutor.this.getOutput();
 
 					if (!output.isEmpty()) {
						RobustProcessExecutor.this.log.debug("Output {}: " + output, new Object[] { RobustProcessExecutor.this.service.getIdentifier() });
 					}
 
 					switch (RobustProcessExecutor.this.getReturn()) {
 					case 0:
 						RobustProcessExecutor.this.service.addResult(ResultKarma.GOOD, output);
 						break;
 					case 1:
 						RobustProcessExecutor.this.service.addResult(ResultKarma.WARNING, output);
 						break;
 					case 2:
 						RobustProcessExecutor.this.service.addResult(ResultKarma.BAD, output);
 						break;
 					default:
 						RobustProcessExecutor.this.service.addResult(ResultKarma.UNKNOWN, output);
 					}
 				} catch (final IOException | ExecutionException e) {
 					RobustProcessExecutor.this.service.addResult(ResultKarma.BAD, "Java Exception (" + e.getClass().getSimpleName() + ") occoured: " + e.toString());
 				} catch (InterruptedException | TimeoutException e) {
 					RobustProcessExecutor.this.log.warn("Timeout exceeded while waiting for service check to complete: " + RobustProcessExecutor.this.service.getIdentifier());
 
 					RobustProcessExecutor.this.service.addResult(ResultKarma.TIMEOUT, "Timeout of " + Math.max(GlobalConstants.DEF_TIMEOUT.getStandardSeconds(), RobustProcessExecutor.this.service.getTimeout().getStandardSeconds()) + " exceeded");
 				} finally {
 					RobustProcessExecutor.this.destroy();
 				}
 
 				RobustProcessExecutor.this.log.info("Executed service check: " + RobustProcessExecutor.this.service.getIdentifier() + " = " + RobustProcessExecutor.this.service.getResult() + " (" + RobustProcessExecutor.this.service.getResultConsequtiveCount() + " consecutive). Delay until next check " + RobustProcessExecutor.this.service.getFlexiTimer().getCurrentDelay() + " , which is " + RobustProcessExecutor.this.service.getSecondsRemaining() + " seconds from now");
 			}
 		};
 
 		RobustProcessExecutor.executingThreadPool.execute(monitoringThread);
 	}
 
 	private String getOutput() {
 		String output = "";
 		final String errorStream = this.outputStreamToString(this.p.getErrorStream());
 
 		if (!errorStream.isEmpty()) {
 			output = "STDERROR: " + errorStream;
 		}
 
 		output += this.outputStreamToString(this.p.getInputStream());
 
 		return output;
 	}
 
 	private int getReturn() throws InterruptedException, ExecutionException, TimeoutException {
 		if (RobustProcessExecutor.monitoringThreadPool.isShutdown() || RobustProcessExecutor.monitoringThreadPool.isTerminated()) {
 			this.log.warn("Service check cannot be executed, because the threadpool has been shutdown. Assuming exit status of 0.");
 			return 0;
 		}
 
 		this.future = RobustProcessExecutor.monitoringThreadPool.submit(this);
 
 		final long timeout = Math.max(this.timeout.getStandardSeconds(), GlobalConstants.DEF_TIMEOUT.getStandardSeconds());
 
 		return this.future.get(timeout, TimeUnit.SECONDS);
 	}
 
 	private String outputStreamToString(final InputStream os) {
		BufferedReader br = new BufferedReader(new InputStreamReader(os, Charset.forName("UTF-8")));
 		final StringBuilder sb = new StringBuilder();
 		String s;
 
 		try {
 			while ((s = br.readLine()) != null) {
 				sb.append(s + "\n");
 			}
 
 			br.close();
 			br = null;
 		} catch (final IOException e) {
 			e.printStackTrace();
 		}
 
 		return sb.toString().trim();
 	}
 }
