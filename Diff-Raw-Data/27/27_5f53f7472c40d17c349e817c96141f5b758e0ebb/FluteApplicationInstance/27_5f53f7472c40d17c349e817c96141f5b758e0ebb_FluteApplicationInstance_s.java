 /*******************************************************************************
  * Copyright (c) 2007 Robert Fraser
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package descent.internal.unittest.flute;
 
 import java.io.IOException;
 
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.IStreamListener;
 import org.eclipse.debug.core.Launch;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.debug.core.model.IStreamMonitor;
 import org.eclipse.debug.core.model.IStreamsProxy;
 
 /**
  * Interfaces with the streams of a fluted application to do cool stuff
  * like run a command & get the result of it.
  * 
  * @author Robert Fraser
  */
 public class FluteApplicationInstance
 {
 	public static final String FLUTE_VERSION = "flute 0.1";
 	
 	/**
 	 * Sets the timeout for commands. Particularly important when running
 	 * tests that could take a while.
 	 * 
 	 * @param timeout the timeout, in milliseconds
 	 */
 	public void setTimeout(long timeout)
 	{
 		fTimeout = timeout;
 	}
 	
 	/**
 	 * Initializes the flute application. Should be called only once
 	 * and before any calls to other methods.
 	 */
 	public void init()
 	{
 		assert(fState instanceof StartingUp);
 		try
 		{
 			beforeWaitStateReturn();
 			waitStateReturn();
 			assert(((StartingUp) fState).hasCorrectVersion);
 		}
 		finally
 		{
 			setState(fWaitingState);
 		}
 	}
 	
 	/**
 	 * Runs a single test and returns the result, or null if the test
 	 * timed out.
 	 * 
 	 * @param signature the signature of the test
 	 * @return          the result of running the test or null if the
 	 *                  test timed out
 	 */
 	public FluteTestResult runTest(String signature) throws IOException
 	{
 		try
 		{
 			setState(new RunningOneTest(this));
 			
 			beforeWaitStateReturn();
 			fProxy.write("r " + signature + "\n"); //$NON-NLS-1$
 			waitStateReturn();
 			
 			return ((RunningOneTest) fState).getResult();	
 		}
 		finally
 		{
 			setState(fWaitingState);
 		}
 	}
 	
 	/**
 	 * Terminates the application. No other calls to this instance should
 	 * be made after this one.
 	 */
 	public void terminate() throws IOException
 	{
 		fProxy.write("x\n");
 	}
 	
 	private class FluteStreamListener implements IStreamListener
 	{	
 		private final StringBuilder fStreamBuffer = new StringBuilder();
 		
		@Override
 		public void streamAppended(String text, IStreamMonitor monitor)
 		{
 			try {
 				fStreamBuffer.append(text);
 				
 				if (fStreamBuffer.toString().trim().equals(AWAITING_INPUT)) {
 					interpret(AWAITING_INPUT);
 					fStreamBuffer.setLength(0);
 				}
 				
 				int indexOfLine = fStreamBuffer.indexOf("\n"); //$NON-NLS-1$
 				if (indexOfLine == -1) return;
 				
 				text = fStreamBuffer.toString();
 				
 				int lastIndexOfLine = 0;
 				
 				while(indexOfLine != -1) {
 					String line = fStreamBuffer.substring(lastIndexOfLine, indexOfLine);
 					interpret(line);
 					
 					lastIndexOfLine = indexOfLine + 1;
 					indexOfLine = fStreamBuffer.indexOf("\n", lastIndexOfLine); //$NON-NLS-1$
 				}
 				
 				fStreamBuffer.delete(0, lastIndexOfLine);
 				
 				String remainder = fStreamBuffer.toString().trim();
 				
 				if (remainder.equals(AWAITING_INPUT)) {
 					interpret(AWAITING_INPUT);
 					fStreamBuffer.setLength(0);
 					
 				// Special case, may happen
 				} else if (remainder.endsWith(AWAITING_INPUT)) {
 					int index = remainder.length() - AWAITING_INPUT.length();
 					interpret(remainder.substring(0, index));
 					interpret(AWAITING_INPUT);
 					fStreamBuffer.setLength(0);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	static final String AWAITING_INPUT = "(flute)";
 	
 	private long fTimeout = 10000;
 	private final Object fWaitLock = new Object();
 	private volatile boolean fWaitLockUsed;
 	
 	private IState fState;
 	private IState fWaitingState = new Waiting(this);
 	
 	private final IProcess fProcess;
 	private final IStreamsProxy fProxy;
 	
 	public FluteApplicationInstance(IProcess process)
 	{
 		fProcess = process;
 		fProxy = fProcess.getStreamsProxy();
 		fProxy.getOutputStreamMonitor().addListener(
 				new FluteStreamListener());
 		setState(new StartingUp(this));
 	}
 	
 	void beforeWaitStateReturn() {
 		fWaitLockUsed = false;
 	}
 	
 	void waitStateReturn() {
 		try {
 			synchronized (fWaitLock) {
 				if (!fWaitLockUsed) {
 					fWaitLock.wait(fTimeout);
 				}
 			}
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	void notifyStateReturn() {
 		fWaitLockUsed = true;
 		synchronized (fWaitLock) {
 			fWaitLock.notify();
 		}		
 	}
 	
 	void setState(IState state)
 	{
 		fState = state;
 	}
 	
 	void interpret(String text) throws IOException
 	{
 		fState.interpret(text.trim());
 	}
 	
 	// Note: this is just for ad-hoc internal testing & should be removed
 	// from the final version
 	public static void adHocTest() throws Exception
 	{
 		Process javaProc = (new ProcessBuilder(new String[] {
 				"C:/Users/xycos/workspace/descent.unittest/testdata/" +
 					"src/test.exe"
 			})).start();
 		IProcess eclipseProc = DebugPlugin.newProcess(new Launch(null,
 				ILaunchManager.RUN_MODE, null), javaProc, "test");
 		FluteApplicationInstance instance = new FluteApplicationInstance(
 				eclipseProc);
 		
 		FluteTestResult res;
 		
 		instance.init();
 		
 		res = instance.runTest("sample.module1.0");
 		System.out.println("sample.module1.0 " + res.getResultType());
 		
 		res = instance.runTest("sample.module1.1");
 		System.out.println("sample.module1.1 " + res.getResultType());
 		
 		res = instance.runTest("sample.module1.2");
 		System.out.println("sample.module1.2 " + res.getResultType());
 		
 		res = instance.runTest("sample.module1.Bar.0");
 		System.out.println("sample.module1.Bar.0 " + res.getResultType());
 		
 		instance.terminate();
 		
 		Thread.sleep(1000); // Let it finish up whatever it's doing
 		try
 		{
 			System.out.println("App finished with exit code: " +
 					javaProc.exitValue());
 		}
 		catch(IllegalThreadStateException e)
 		{
 			System.out.println("Had to manually kill the process...");
 			javaProc.destroy();
 		}
 	}
 }
