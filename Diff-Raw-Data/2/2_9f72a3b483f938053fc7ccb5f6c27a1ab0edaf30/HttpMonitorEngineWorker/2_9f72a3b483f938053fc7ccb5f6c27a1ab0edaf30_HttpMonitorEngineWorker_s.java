 package com.yogocodes.httpmonitor.core;
 
 import java.io.IOException;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class HttpMonitorEngineWorker implements Runnable {
 
 	private final static Logger LOG = LoggerFactory.getLogger(HttpMonitorEngineWorker.class);
 	private MonitorTarget target;
 
 	@Override
 	public void run() {
 		final HttpMonitorEngine engineInstance = HttpMonitorEngineFactory.getEngineInstance();
 
 		final HttpClient httpClient = new HttpClient();
 		final MonitorResultSummarizer summarizer = MonitorResultSummarizerFactory.getInstance();
 
 		final MonitorLogWriter writer = new MonitorLogWriter();
 		while (engineInstance.isRunning()) {
 			final GetMethod method = new GetMethod(target.toString());
 
 			try {
 				final long start = System.currentTimeMillis();
 				// FIXME add support for statuscode
 				final int statusCode = httpClient.executeMethod(method);
 				int numOfBytes = 0;
 				if (statusCode == 200) {
 					final byte[] response = method.getResponseBody();
 					numOfBytes = response.length;
 				}
 				final long end = System.currentTimeMillis();
 
 				final MonitorResult result = new MonitorResult();
 				result.setUrl(target.toString());
 				result.setTime((end - start));
 				result.setStatusCode(statusCode);
 				result.setNumberOfBytes(numOfBytes);
 				result.setExecuteTime(start);
 				summarizer.addResult(result);
 				writer.writeCSVLog(result);
 
 			} catch (final HttpException e) {
 				LOG.error("failed to create request to target:" + e.getMessage(), e);
 			} catch (final IOException e) {
 				LOG.error("failed to create request to target:" + e.getMessage(), e);
 			}
 
 			try {
 				Thread.sleep(1000);
 			} catch (final InterruptedException e) {
				LOG.error("the thread sleep was interrupted, interruptting current one");
 				Thread.currentThread().interrupt();
 			}
 
 		}
 
 	}
 
 	public void setValues(final MonitorTarget target) {
 		this.target = target;
 	}
 
 }
