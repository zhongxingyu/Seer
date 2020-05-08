 /* 
  * Copyright 2008-2009 the original author or authors.
  * The contents of this file are subject to the Mozilla Public License
  * Version 1.1 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  */
  
 package com.mtgi.analytics.test;
 
 import static java.io.File.separator;
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.mtgi.analytics.jmx.StatisticsMBean;
 
 /**
  * Performs some instrumented tests to verify that behavior tracking doesn't
  * interfere too much with application performance.
  */
 public abstract class AbstractPerformanceTestCase {
 
 	private static final int TEST_ITERATIONS = 10;
 	private Log log = LogFactory.getLog(getClass());
 
 	private int testLoop;
 	private int testThreads;
 	private long averageOverhead;
 	private long maxOverhead;
 	private long expectedBasis;
 	
 	/**
 	 * @param testThreads the number of normal priority threads to concurrently execute test runnables.
 	 * @param testLoop the number of times each test thread invokes the test runnable before exiting
 	 * @param expectedBasis <span>the benchmark CPU time against which <code>averageOverhead</code> was established.
 	 * 	                    <code>averageOverhead</code> and <code>maxOverhead</code> will be scaled by the ratio of this time to
 	 * 						actual measured time during control runs, to account for the processing power available on the system that
 	 * 						is actually running the test.</span>
 	 * @param averageOverhead <span>the average overhead in CPU nanoseconds expected for each measurement taken.  this figure is scaled
 	 * 						  according to <code>expectedBasis</code> before being compared to test run times.</span>
 	 * @param maxOverhead <span>the maximum overhead in CPU nanoseconds allowed for each measurement taken.  this figure is scaled
 	 * 					  according to <code>expectedBasis</code> before being compared to test run times.</span>
 	 */
 	protected AbstractPerformanceTestCase(int testThreads, int testLoop, long expectedBasis, long averageOverhead, long maxOverhead) {
 		this.testThreads = testThreads;
 		this.testLoop = testLoop;
 		this.averageOverhead = averageOverhead;
 		this.maxOverhead = maxOverhead;
		this.expectedBasis = expectedBasis;
 	}
 	
 	protected void testPerformance(TestCase basisJob, TestCase testJob) throws Throwable {
 
 		//cache serialized form to save time on repeated test iterations
 		byte[] controlData = serializeJob(new TestProcess(testThreads, testLoop, basisJob));
 		byte[] testData = serializeJob(new TestProcess(testThreads, testLoop, testJob));
		StatisticsMBean controlStats = new StatisticsMBean(),  testStats = new StatisticsMBean();
 		
 		ServerSocket listener = new ServerSocket(0);
 		try {
 			int port = listener.getLocalPort();
 			
 			ProcessBuilder pb = new ProcessBuilder(
 					System.getProperty("java.home") + separator + "/bin/java",
 					"-server",
 					"-cp",
 					System.getProperty("java.class.path"),
 					TestProcess.class.getName(),
 					String.valueOf(port));
 			pb.redirectErrorStream(false);
 			
 			//run several iterations of the test, alternating between instrumented and not
 			//to absorb the affects of garbage collection.
 			for (int i = 0; i < TEST_ITERATIONS; ++i) {
 				System.out.println("iteration " + i);
 				//switch the order of test / control runs during iteration to reduce any
 				//bias that order might cause
 				if (i % 2 == 0) {
 					runTest(listener, pb, testStats, testData);
 					runTest(listener, pb, controlStats, controlData);
 				} else {
 					runTest(listener, pb, controlStats, controlData);
 					runTest(listener, pb, testStats, testData);
 				} 
 				assertEquals("basis and test have same sample size after iteration " + i, controlStats.getCount(), testStats.getCount());
 			}
 		} finally {
 			listener.close();
 		}
 			
 		double basisNanos = controlStats.getAverageTime();
		double cpuCoefficient = basisNanos / expectedBasis;
 		
 		double expectedAverage = cpuCoefficient * averageOverhead;
 		double expectedMax = cpuCoefficient * maxOverhead;
 		
 		System.out.println("control:\n" + controlStats);
 		System.out.println("test:\n" + testStats);
 		System.out.println("CPU Coefficient: " + cpuCoefficient);
 		
 		//compute the overhead as the difference between instrumented and uninstrumented
 		//runs.  we want the per-event overhead to be less than .5 ms.
 		double delta = testStats.getAverageTime() - basisNanos;
 		//deltaWorst, my favorite sausage.  mmmmmm, dellltttaaaWwwwooorrsstt.
 		double deltaWorst = testStats.getMaxTime() - controlStats.getMaxTime();
 
 		System.out.println("Average overhead: " + delta);
 		System.out.println("Worst case overhead: " + deltaWorst);
 		
 		assertTrue("Average overhead per method cannot exceed " + expectedAverage + "ns [ " + delta + " ]",
 					 delta <= expectedAverage);
 		
 		assertTrue("Worst case per method overhead cannot exceed " + expectedMax + "ns [ " + deltaWorst + " ]",
 					deltaWorst <= expectedMax);
 	}
 	
 	private void runTest(ServerSocket listener, ProcessBuilder launcher, StatisticsMBean stats, byte[] jobData) throws IOException, InterruptedException {
 		Process proc = launcher.start();
 		
 		//connect stdout and stderr to parent process streams
 		if (log.isDebugEnabled()) {
 			new PipeThread(proc.getInputStream(), System.out).start();
 			new PipeThread(proc.getErrorStream(), System.err).start();
 		} else {
 			NullOutput sink = new NullOutput();
 			new PipeThread(proc.getInputStream(), sink).start();
 			new PipeThread(proc.getErrorStream(), sink).start();
 		}
 
 		//wait for the incoming connection from the child process.
 		Socket sock = listener.accept();
 		try {
 			//connection established, send the job.
 			OutputStream os = sock.getOutputStream();
 			os.write(jobData);
 			os.flush();
 
 			//read measurements back.
 			DataInputStream dis = new DataInputStream(sock.getInputStream());
 			for (long measure = dis.readLong(); measure >= 0; measure = dis.readLong())
 				stats.add(measure);
 			
 			//send ack byte to tell the child proc its ok to hang up.
 			os.write(1);
 			os.flush();
 			
 		} finally {
 			sock.close();
 		}
 
 		assertEquals("Child process ended successfully", 0, proc.waitFor());
 	}
 	
 	private byte[] serializeJob(TestProcess job) throws IOException {
 		ByteArrayOutputStream buf = new ByteArrayOutputStream();
 		ObjectOutputStream oos = new ObjectOutputStream(buf);
 		oos.writeObject(job);
 		oos.flush();
 		return buf.toByteArray();
 	}
 	
 	private static class NullOutput extends OutputStream {
 		@Override
 		public void write(byte[] b, int off, int len) {}
 		@Override
 		public void write(byte[] b) {}
 		@Override
 		public void write(int b) {}
 	}
 	
 	private static class PipeThread extends Thread {
 
 		private InputStream in;
 		private OutputStream out;
 		
 		protected PipeThread(InputStream in, OutputStream out) {
 			super("Piped IO");
 			this.in = in;
 			this.out = out;
 		}
 		
 		public void run() {
 			//just drain child output.
 			byte[] buf = new byte[256];
 			try {
 				for (int b = in.read(buf); b >= 0; b = in.read(buf))
 					if (b == 0)
 						Thread.sleep(100);
 					else
 						out.write(buf, 0, b);
 			} catch (Exception ioe) {
 				ioe.printStackTrace(System.err);
 			}
 		}
 	}
 }
