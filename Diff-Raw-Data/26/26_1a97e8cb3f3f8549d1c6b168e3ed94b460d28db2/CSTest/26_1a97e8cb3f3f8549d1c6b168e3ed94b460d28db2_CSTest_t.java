 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2009, Red Hat, Inc., and others contributors as indicated
  * by the @authors tag. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.jboss.blacktie.jatmibroker.xatmi;
 
 import java.io.*;
 import junit.framework.TestCase;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 
 public class CSTest extends TestCase
 {
 	private static final Logger log = LogManager.getLogger(CSTest.class);
 	private static boolean DISABLED = false;	// disable tests until the C program is built correctly
 
 	// Avoid having to start up and tear down a server for each test
 	// WARNING TODO NTESTS must match the actual number of tests
 	// Better would be to define
 	// public static Test suite() and have the final test destroy the server
 	static final int NTESTS = 16;	// Number of tests 
 	private static int testCnt = 0;
 
 	private static TestProcess server;
 	private TestProcess client;
 	private String CS_EXE;
 	private String REPORT_DIR;
 	private String[] ENV_ARRAY;
 
 	public void tearDown() {
 		if (DISABLED) return;
 		log.debug("tearDown " + (testCnt + 1) + " out of " + NTESTS);
 		if (++testCnt == NTESTS) {
 			try {
 				log.info("destroying server process");
				server.interrupt();
 				server.getProcess().destroy();
 			} catch(Throwable e) {
 			}
 		}
 	}
 
 	public void setUp() {
 		if (DISABLED) return;
 		REPORT_DIR = System.getProperty("TEST_REPORTS_DIR", ".");
 		CS_EXE = System.getProperty("CLIENT_SERVER_EXE", "./cs");
 
 		if (server == null) {
 			log.info("CS Tests: SETUP REPORT_DIR=" + REPORT_DIR + " CLIENT_SERVER_EXE=" + CS_EXE);
 
 			ENV_ARRAY = new String[4];
 
 			ENV_ARRAY[0] = "LD_LIBRARY_PATH=" + System.getProperty("LD_LIBRARY_PATH");
 			ENV_ARRAY[1] = "BLACKTIE_CONFIGURATION_DIR=" + System.getProperty("BLACKTIE_CONFIGURATION_DIR");
 			ENV_ARRAY[2] = "BLACKTIE_SCHEMA_DIR=" + System.getProperty("BLACKTIE_SCHEMA_DIR");
 			ENV_ARRAY[3] = "JBOSSAS_IP_ADDR=" + System.getProperty("JBOSSAS_IP_ADDR");
 
 			for (int i = 0; i < 4; i++)
 				log.debug(ENV_ARRAY[i]);
 
 			try {
 				server = startServer(ENV_ARRAY);
 				Thread.sleep(3000);
 			} catch (IOException e) {
 				System.err.printf("Server io exception: " + e);
 			} catch (InterruptedException e) {
 				System.err.printf("Server interrupted: " + e);
 			}
 		}
 	}
 
 	// XsdValidator is not thread safe
 	public void test_211() { runTest("211"); }
 	// tpcall incorrectly returns TPNOTIME whenever the TPNOBLOCK or TPNOTIME flags are specified
 	public void test_2120() { runTest("2120"); }
 	// Similarly specifying TPNOBLOCK means that if a blocking condition does exist then the caller
 	// should get the error TPEBLOCK
 	public void test_2121() { runTest("2121"); }
 	// tpcall should return TPEINVAL if the service name is invalid
 	public void test_213() { runTest("213"); }
 	// TPSIGRSTRT flag isn't supported on tpcall	
 	public void test_214() { runTest("214"); }
 	// tpcall failure with multiple threads
 	public void test_215() { runTest("215"); }
 	//tp bufs should morph if they're the wrong type
 	public void test_2160() { runTest("2160"); }
 	// passing the wrong return buffer type with TPNOCHANGE
 	public void test_2161() { runTest("2161"); }
 	// make sure tpurcode works
 	public void test_217() { runTest("217"); }
 	// tpalloc with X_C_TYPE and non-zero len: tperrno=%d (spec says size is optional)
 	public void test_9001() { runTest("9001"); }
 	// sanity check
 	public void test_0() { runTest("0"); }
 	// tell the server to set a flag on tpreturn (should generate TPESVCERR)
 	public void test_1() { runTest("1"); }
 	// set flag on tpreturn should fail
 	public void test_2() { runTest("2"); }
 	// telling the service to not tpreturn should generate an error
 	public void test_3() { runTest("3"); }
 	// telling service to call tpreturn outside service routine should have no effect
 	public void test_4() { runTest("4"); }
 	// tpreturn outside service routing
 	public void test_5() { runTest("5"); }
 
 	void runTest(String name) {
 		if (DISABLED) return;
 		try {
 			log.debug("waiting for test " + name);
 			TestProcess client = startClient(name, true, ENV_ARRAY);
 			int res = client.exitValue();
 
 			log.info("test " + name + (res == 0 ? " passed " : " failed ") + res);
 			assertTrue(res == 0);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	TestProcess startClient(String testname, boolean waitFor, String[] envp) throws IOException, InterruptedException {
 		FileOutputStream ostream = new FileOutputStream(REPORT_DIR + "/test-" + testname + "-out.txt");
 		FileOutputStream estream = new FileOutputStream(REPORT_DIR + "/test-" + testname + "-err.txt");
 		TestProcess client = new TestProcess(ostream, estream, "client: ", CS_EXE + " " + testname, envp);
 		Thread thread = new Thread(client);
 
 		thread.start();
 		if (waitFor) {
 			log.debug("startClient: waiting to join with client thread ...");
 			thread.join();
 			log.debug("startClient: joined - waiting for client process to exit");
 			client.getProcess().waitFor();
			log.debug("startClient: done");
 		} else {
 		}
 
 		return client;
 	}
 
 	TestProcess startServer(String[] envp) throws IOException {
 		FileOutputStream ostream = new FileOutputStream(REPORT_DIR + "/server-out.txt");
 		FileOutputStream estream = new FileOutputStream(REPORT_DIR + "/server-err.txt");
 		TestProcess server = new TestProcess(ostream, estream, "server: ", CS_EXE + " -c linux -i 1", envp);
 		Thread thread = new Thread(server);
 		thread.start();
 
 		return server;
 	}
 
 	public static void main(String args[]) throws IOException {
 		new CSTest().runTests();
 	}
 
 	void runTests() {
 		setUp();
 
 //		test_211(); test_2120(); test_2121(); test_213(); test_214(); test_215();
 //		test_2160(); test_2161(); test_217(); test_9001();
 //		test_0(); test_1(); test_2(); test_3(); test_4(); test_5(); 
 		server.getProcess().destroy();
 	}
 
 	class TestProcess implements Runnable {
 		private String prefix;
 		private String command;
 		private Process proc;
 		private FileOutputStream ostream;
 		private FileOutputStream estream;
 		private String[] envp;
		private Thread thread;
 
 		TestProcess(FileOutputStream ostream, FileOutputStream estream, String prefix, String command, String[] envp) {
 			this.ostream = ostream;
 			this.estream = estream;
 			this.prefix = prefix;
 			this.command = command;
 			this.envp = envp;
 		}
 
 		Process getProcess() { return proc; }
 		int exitValue() { return proc.exitValue(); }
		void interrupt() { if (thread != null) thread.interrupt(); }
 
 		public void run() {
			thread = Thread.currentThread();

 			try {
 				Runtime rt = Runtime.getRuntime();
 				proc = rt.exec(command, envp);
 
 				InputStream is = proc.getInputStream();
 				InputStream es = proc.getErrorStream();
 				byte[] buf = new byte[1024];
 				int len;
 
 				while ((len = is.read(buf)) > 0)
 					ostream.write(buf, 0, len);
 
 				while ((len = es.read(buf)) > 0)
 					estream.write(buf, 0, len);
 			} catch (IOException e) {
				if (!thread.interrupted())
					log.info(command + ": IO error on stream write: " + e + " test count=" + testCnt);
			}

			try {
				ostream.close();
				estream.close();
			} catch (IOException e) {
 			}
 		}
 	}
 }
