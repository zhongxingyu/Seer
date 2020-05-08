 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.httpclient.ConnectTimeoutException;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethodBase;
 import org.apache.commons.httpclient.methods.OptionsMethod;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.service.Descriptor;
 import org.xins.common.service.TargetDescriptor;
 import org.xins.common.xml.ElementBuilder;
 
 /**
  * Checks all the links in the given <code>descriptor</code>s list and builds
  * a <code>FunctionResult</code>. It connects to each link in 
  * {@link TargetDescriptor}s in {@link Descriptor}s list using a 
  * {@link URLChecker} and calculates the total links count and 
  * total links failures. The returned {@link FunctionResult} contains 
  * information about total links checked, failures and details.
  * 
  * The following example uses a {@link CheckLinks} object to get the 
  * {@link FunctionResult}.
  * 
  * <pre>
  * FunctionResult result = CheckLinks.checkLinks(descriptorList);
  * 
  * // Returns parameters
  * result.getParameters(); 
  * </pre>
  *  
  * @version $Revision$ $Date$
  * @author Tauseef Rehman (<a href="mailto:tauseef.rehman@nl.wanadoo.com">tauseef.rehman@nl.wanadoo.com</a>)
  */
 class CheckLinks {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
    
    /**
     * Checks all the links in <code>TargetDescriptor</code>s inside the 
     * <code>Descriptor</code> list and builds a <code>FunctionResult</code>.
     * First gets all the {@link TargetDescriptor}s from the 
     * {@link Descriptor}s list then creates {@link URLChecker} threads with 
     * {@link TargetDescriptor}s and runs them. When all the threads have
     * finished execution, the {@link FunctionResult} is built and returned.
     * The returned {@link FunctionResult} contains all the links which were 
     * checked with their results.
     *
     * @param descriptors
     *    the list of {@link Descriptor}s defined in the runtime properties, 
     *    cannot be <code>null</code>.
     *
     * @return
     *    the constructed {@link FunctionResult} object, never 
     *    <code>null</code>.
     * 
     * @throws IllegalArgumentException
     *    if <code>descriptors == null</code>.
     */
    public static FunctionResult checkLinks(List descriptors) 
    throws IllegalArgumentException {
 
 		// Check preconditions
       MandatoryArgumentChecker.check("descriptors", descriptors);
       
       // Get all the targets from the descriptor list
       List targetDescriptors = getTargetDescriptors(descriptors);
 
       // Create the thread for each target and run them
       List threads = createAndRunUrlCheckThreads(targetDescriptors);
       
       // Get the biggest timeout from all the targets
       int timeout = getBiggestTimeout(targetDescriptors);
 
       // Wait till all the threads finish their execution or timedout.
       waitTillThreadsRunning(threads, timeout);
 
       // Start building the result
       FunctionResult builder = new FunctionResult();
       int errorCount = addCheckElements(builder, threads);
       builder.param("linkCount", String.valueOf(threads.size()));
       builder.param("errorCount", String.valueOf(errorCount));
       
       return builder;
    }
 
    
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
    
    /**
     * Creates a new <code>CheckLinks</code> object.
     */
    private CheckLinks() {
       // empty
    }
    
    
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------   
    
    /**
     * Creates a list of <code>TargetDescriptor</code>s from the
     * given <code>Descriptor</code>s list. Each {@link Descriptor} in the 
     * list contains a list of {@link TargetDescriptor}s, which are added to 
     * the returning list.
     * 
     * @param descriptors
     *    the list of {@link Descriptor}s, cannot be <code>null</code>.
     *
     * @return
     *    the constructed {@link TargetDescriptor}s list, never 
     *    <code>null</code>.
     * 
     * @throws IllegalArgumentException
     *    if <code>descriptors == null</code>.
     */
    private static List getTargetDescriptors(List descriptors)
    throws IllegalArgumentException {
 
 		// Check preconditions
       MandatoryArgumentChecker.check("descriptors", descriptors);
       
       Iterator descriptorIterator = descriptors.iterator();
       List     targetDescriptors  = new ArrayList();
     
       // Each decriptor in the list contains target descriptors, so 
       // iterate over descriptors and get all the target descriptors, then
       // iterate over each target descriptor and get the individual 
       // target descriptors.
       while (descriptorIterator.hasNext()) {
          Descriptor descriptor = (Descriptor)descriptorIterator.next();
          
          // Get the iterator on target descriptor
          Iterator targetIterator = descriptor.iterateTargets();
          while (targetIterator.hasNext()) {
             TargetDescriptor targetDescriptor = 
                (TargetDescriptor)targetIterator.next();
             
             // Add all the target descriptors in a list
             targetDescriptors.add(targetDescriptor);
          }
       }
       
       return targetDescriptors;
    }
    
    /**
     * Creates and runs a thread for each <code>TargetDescriptor</code> in the
     * given list. Each {@link TargetDescriptor} in the list contains a URL. A 
     * {@link URLChecker} thread is created for each {@link TargetDescriptor},
     * which tries to connect to the URL provided in the 
     * {@link TargetDescriptor}. Each thread is then added to a list which is 
     * returned.
     *
     * @param targetDescriptors
     *    the list of {@link TargetDescriptor}s which needs to be checked,
     *    cannot be <code>null</code>.
     *
     * @return
     *    the constructed {@link URLChecker}s list, never <code>null</code>.
     * 
     * @throws IllegalArgumentException
     *    if <code>targetDescriptors == null</code>.
     */
    private static List createAndRunUrlCheckThreads(List targetDescriptors)
    throws IllegalArgumentException {
 
 		// Check preconditions
       MandatoryArgumentChecker.check("targetDescriptors", targetDescriptors);
       
       List     threads = new ArrayList();
       Iterator targets = targetDescriptors.iterator();
       
       // Iterate over all target descriptors 
       while (targets.hasNext()) {
          TargetDescriptor target = (TargetDescriptor) targets.next();
          
          // Create a thread for the target descriptor
          URLChecker urlThread = new URLChecker(target);
          
          // Start the thread with target descriptor 
          urlThread.start();
          
          // Store the thread just started in a list
          threads.add(urlThread);
       }
       
       return threads;
    }
    
    /**
     * Returns the biggest timeout of all the URLs defined in
     * <code>TargetDescriptor</code>s list. Each {@link TargetDescriptor} in 
     * the list has total timeout. The biggest of all of them is returned. 
     * This timeout is then used to setup the timeouts of the 
     * {@link URLChecker} threads.
     *
     * @param targetDescriptors
     *    the list of {@link TargetDescriptor}s, cannot be <code>null</code>.
     *
     * @return
     *    the biggest timeout from the list, or <code>-1</code> if none of the
 	 *    target descriptors defines a time-out.
     * 
     * @throws IllegalArgumentException
     *    if <code>targetDescriptors == null</code>.
     */
    private static int getBiggestTimeout(List targetDescriptors)
    throws IllegalArgumentException {
 
 		// Check preconditions
       MandatoryArgumentChecker.check("targetDescriptors", targetDescriptors);
       
       Iterator targets        = targetDescriptors.iterator();
       int      biggestTimeout = -1;
       
       // Iterate over all target descriptors 
       while (targets.hasNext()) {
          TargetDescriptor target = (TargetDescriptor) targets.next();
          
          // Try to get the biggest time out of all the target descriptors
          if (biggestTimeout < target.getTotalTimeOut()) {
             biggestTimeout = target.getTotalTimeOut();
          }
       }
       
       return biggestTimeout;
    }
    
    /**
     * Sets up the timeout for each thread and waits till each thread finishes
     * execution. The timeout is the biggest timeout of all the URLs in 
     * {@link TargetDescriptor}s. Timeout for every next thread also considers
     * the time which is already spent and that time is subtracted from the
     * timout for the current thread.
     *
     * @param threads
     *    the list of {@link URLChecker} threads, cannot be <code>null</code>.
     * 
     * @param timeout
     *    the timeout for {@link URLChecker} threads.
     * 
     * @throws IllegalArgumentException
     *    if <code>threads == null</code>.
     */
    private static void waitTillThreadsRunning(List threads, int timeout)
    throws IllegalArgumentException {
 
 		// Check preconditions
       MandatoryArgumentChecker.check("threads", threads);
       
       Iterator threadIterator = threads.iterator();
       long threadTimeout      = timeout;
       
       // Storing the time approximately when the first thread was started
       long startTime = System.currentTimeMillis();
       try {
 
          // Iterate over all the threads
          while (threadIterator.hasNext()) {
             URLChecker urlThread = (URLChecker) threadIterator.next();
             urlThread.join(threadTimeout);
             
             // If the previous thread was setup with a certain timeout
             // the next thread should be setup with a timeout subtracted
             // by the time which is already passed.
             long endTime = System.currentTimeMillis();
             long timePassed = endTime - startTime;
            threadTimeout = threadTimeout - timePassed;
             
             // If the timeout becomes negative, it means that the total time
             // out interval has passed now we do not need to setup timeout 
             // for threads and they all should have finished execution by now.
             if (threadTimeout < 0) {
                return;
             }
          }
       } catch (InterruptedException exception) {
 
          // The exception is thrown when another thread has interrupted 
          // the current thread. This should never happen so it should log
          // a programming error and throw a Programming exception.
          throw Utils.logProgrammingError(exception);
       }
    }
 
    /**
     * Builds the <code>FunctionResult</code> for all the URLs checked. It 
     * iterates over the list of all {@link URLChecker} threads and gets the
     * information like the total time each thread took to execute and the 
     * result of the execution. The information is added in an 
     * {@link ElementBuilder} object using which {@link Element} is created
     * which then is added to the passed {@link FunctionResult}.
     *
     * @param builder
     *    the {@link FunctionResult} where the result is added, cannot be 
     *    <code>null</code>.
     * 
     * @param threads
     *    the list of {@link URLChecker} threads, cannot be <code>null</code>.
     * 
     * @return
     *    the total number of URLs without success.
     * 
     * @throws IllegalArgumentException
     *    if <code>builder == null || threads == null</code>.
     * 
     */
    private static int addCheckElements(FunctionResult builder, List threads)
    throws IllegalArgumentException {
 
 		// Check preconditions
       MandatoryArgumentChecker.check("builder", builder, "threads", threads);
 
       Iterator threadIterator = threads.iterator();
       int      errorCount     = 0;
       
       // Iterate over the threads of target descriptors and create the 
       // check element.
       while (threadIterator.hasNext()) {
          URLChecker urlThread = (URLChecker) threadIterator.next();
          ElementBuilder check = new ElementBuilder("check");
          check.setAttribute("url", urlThread.getURL());
          check.setAttribute("duration", 
             new Long(urlThread.getDuration()).toString());
          check.setAttribute("result", getResult(urlThread));
          builder.add(check.createElement());
          
          if (!urlThread.getSuccess()) {
             errorCount ++;
          }
       }
       
       return errorCount;
    }
    
    /**
     * Returns the value for the result parameter which is added in the 
     * <code>FunctionBuilder</code>. The value of the result depends on the
     * success or failure of the passed {@link URLChecker} thread. If the 
     * {@link URLChecker} thread gives a success, the status code of the 
     * {@link URLChecker} thread is used to create the value for result 
     * parameter, otherwise the exception in the {@link URLChecker} thread 
     * determines the value for the result parameter.
     *
     * @param urlThread
     *    the {@link URLChecker} thread for which the result value is to 
     *    detemined, cannot be <code>null</code>.
     *
     * @return 
     *    the result message, never <code>null</code>.
     * 
     * @throws IllegalArgumentException
     *    if <code>urlThread == null || urlThread.hasRun() == false</code>.
     */
    private static String getResult(URLChecker urlThread)
    throws IllegalArgumentException {
 
 		// Check preconditions
       MandatoryArgumentChecker.check("urlThread", urlThread);
 		if (! urlThread.hasRun()) {
 			throw new IllegalArgumentException("urlThread().hasRun() == false");
 		}
 
       if (urlThread.getSuccess()) {
          return "Success";
       } else {
          return getResult(urlThread.getException());
       }
    }
    
    /**
     * Returns the value for the result parameter which is added in the 
     * <code>FunctionBuilder</code> when the <code>URLChecker<code> thread 
     * failed to connect the URL. The value for the result parameter depends
     * on the exception occured in the {@link URLChecker} thread. The 
     * exception is passed to this method. Based on the type of exception, an
     * appropriate value is returned.
     *
     * @param exception
     *    the {@link Throwable} exception occured in the {@link URLChecker}
     *    thread, cannot be <code>null</code>.
     *
     * @return 
     *    the result message, never <code>null</code>.
     * 
     * @throws IllegalArgumentException
     *    if <code>exception == null</code>.
     */
    private static String getResult(Throwable exception)
    throws IllegalArgumentException {
 
 		// Check preconditions
       MandatoryArgumentChecker.check("exception", exception);
       
       if (exception instanceof UnknownHostException) {
          return "UnknownHost";
       } else if (exception instanceof ConnectException) {
          return "ConnectionRefusal";
       } else if (exception instanceof ConnectTimeoutException) {
          return "ConnectionTimeout";
          
       // SocketTimeoutException is not available in older jdk version
       // we need to check for the exact name
       } else if (exception.getClass().getName().equals(
                                          "java.net.SocketTimeoutException")) {
          return "SocketTimeout";
       } else if (exception instanceof IOException) {
          return "OtherIOError";
       } else {
          return "OtherFailure";
       }
    }
    
    
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
    
    /**
     * Tries to connect to a URL provided in the 
     * <code>TargetDescriptor</code>. Runs as a separate thread. The URL is
     * connected by sending a request associated with an HTTP 
     * <code>OPTIONS</code> method. Also calculates the total time to 
     * connect to the provided URL.
     *  
     * <p>The following example uses a {@link CheckLinks} object to get the 
     * {@link FunctionResult}.
     * 
     * <blockquote><pre>TargetDescriptor target = new TargetDescriptor();
     * target.setURL("www.hotmail.com");
     * 
     * URLChecker urlThread = new URLChecker(target);
     * urlThread.start();
     * 
     * String URL = urlThread.getURL();
     * int duration = urlThread.getDuration();
     * boolean success = urlThread.getSuccess();
     * if (!success) {
     *    exception = urlThread.getException();
     * }</pre></blockquote>
     * 
     * @version $Revision$ $Date$
     * @author Tauseef Rehman (<a href="mailto:tauseef.rehman@nl.wanadoo.com">tauseef.rehman@nl.wanadoo.com</a>)
     */
    private static final class URLChecker extends Thread {
 
       //-------------------------------------------------------------------------
       // Class fields
       //-------------------------------------------------------------------------
       
       //-------------------------------------------------------------------------
       // Class functions
       //-------------------------------------------------------------------------     
            
       //-------------------------------------------------------------------------
       // Constructors
       //-------------------------------------------------------------------------
       
       /**
        * Constructs a new <code>URLChecker</code> for the specified target 
        * descriptor.
        *
        * @param targetDescriptor
        *    the {@link TargetDescriptor}, whose URL needs to be checked, 
        *    cannot be <code>null</code>.
        * 
        * @throws IllegalArgumentException
        *    if <code>targetDescriptor == null</code>.
        */
       public URLChecker(TargetDescriptor targetDescriptor)
       throws IllegalArgumentException {
            
 			// Check preconditions
          MandatoryArgumentChecker.check("targetDescriptor", targetDescriptor);
 
 			// Initialize fields
          _targetDescriptor = targetDescriptor;
          _url              = targetDescriptor.getURL();
 			_duration         = -1;
 			_statusCode       = -1;
 
 			// Check postconditions
 			if (_url == null) {
 				throw Utils.logProgrammingError("_url == null");
 			}
       }
 
 
       //-------------------------------------------------------------------------
       // Fields
       //-------------------------------------------------------------------------
       
       /**
        * The target descriptor for which the URL needs to be checked. Never
 		 * <code>null</code>.
        */
       private TargetDescriptor _targetDescriptor;
       
       /**
        * The URL to be checked. Never <code>null</code>.
        */
       private String _url;
       
       /**
        * The exception thrown when accessing the URL. Can be
 		 * <code>null</code> if the <code>URLChecker</code> has not run yet, or
 		 * if there was no error.
        */
       private Throwable _exception;
       
       /**
        * The result of the URL check. Is <code>true</code> if the
 		 * <code>URLChecker</code> has run and was successful. If either of
 		 * these conditions is not met, then <code>false</code>.
        */
       private boolean _success;
       
       /**
        * The time taken to check the URL. Initially <code>-1</code>.
        */
       private long _duration;
       
       /**
        * The status code returned when the URL was called. Initially
 		 * <code>-1</code>, when the <code>URLChecker</code> was not run yet.
        */
       private int _statusCode;
 
       
       //-------------------------------------------------------------------------
       // Methods
       //-------------------------------------------------------------------------
 
       /**
        * Runs this thread. It tries to connect to the URL provided in the 
        * {@link TargetDescriptor}. The URL is connected by sending a request 
        * associated with an HTTP <code>OPTIONS</code> method. It also
        * calculates the total time to connect to the provided URL and saves
        * the exception in case an exception occurs.
 		 * 
 		 * @throws IllegalStateException
 		 *    if this <code>URLChecker</code> has already run.
        */
       public void run() throws IllegalStateException {
 
 			// Check preconditions
 			if (hasRun()) {
 				throw new IllegalStateException("This URLChecker has already run.");
 			}
 
 			// Register current time, to compute total duration later
          long startTime = System.currentTimeMillis();
 
          try {
             HttpClient client = new HttpClient();
             client.getParams().setConnectionManagerTimeout(
                                _targetDescriptor.getConnectionTimeOut());
             client.getParams().setSoTimeout(
                               _targetDescriptor.getSocketTimeOut());
             
             client.getHttpConnectionManager().getParams(
                               ).setConnectionTimeout(
                               _targetDescriptor.getConnectionTimeOut());
             
             HttpMethodBase optionsMethod = 
                new OptionsMethod(_targetDescriptor.getURL());
             _statusCode = client.executeMethod(optionsMethod);
             optionsMethod.releaseConnection();
             _success = true;
          } catch (Throwable exception) {
             _exception = exception;
             _success = false;
          }
          _duration = System.currentTimeMillis() - startTime;
       }
 
 		/**
 		 * Checks if this <code>URLChecker</code> has already run.
 		 *
 		 * @return
 		 *    <code>true</code> if this <code>URLChecker</code> has already run,
 		 *    or <code>false</code> otherwise.
 		 */
 		boolean hasRun() {
 			return (_duration >= 0);
 		}
       
 		/**
 		 * Checks if this <code>URLChecker</code> has already run and if not,
 		 * throws an exception
 		 *
 		 * @throws IllegalStateException
 		 *    if this <code>URLChecker</code> has not run yet.
 		 */
 		private void assertHasRun() throws IllegalStateException {
 			if (!hasRun()) {
 				throw new IllegalStateException("This URLChecker has not run yet.");
 			}
 		}
       
       /**
        * Returns the total time it took to connect to the URL.
        *
        * @return 
        *    the total duration in milliseconds, never <code>null</code>.
        *
        * @throws IllegalStateException 
 		 *    if this <code>URLChecker</code> has not run yet.
        */
       public long getDuration() throws IllegalStateException {
 			assertHasRun();
          return _duration;
       }
       
       /**
        * Returns the flag indicating if the URL was connected successfully.
        *
        * @return 
        *    the success flag, never <code>null</code>.
        *
        * @throws IllegalStateException 
 		 *    if this <code>URLChecker</code> has not run yet.
        */
       public boolean getSuccess() throws IllegalStateException {
 			assertHasRun();
          return _success;
       }
       
       /**
        * Return the status code of the method execution.
        *
        * @return 
        *    the status code, never <code>null</code>.
        *
        * @throws IllegalStateException 
 		 *    if this <code>URLChecker</code> has not run yet.
        */
       public int getStatusCode() throws IllegalStateException {
 			assertHasRun();
          return _statusCode;
       }
       
       /**
        * Returns the URL which was connected.
        *
        * @return 
        *    the URL, never <code>null</code>.
        *
        * @throws IllegalStateException 
 		 *    if this <code>URLChecker</code> has not run yet.
        */
       public String getURL() throws IllegalStateException {
 			assertHasRun();
          return _url;
       }
       
       /**
        * Returns the exception thrown while trying to connect to the URL.
        *
        * @return 
        *    the exception, never <code>null</code>.
        *
        * @throws IllegalStateException 
 		 *    if this <code>URLChecker</code> has not run yet.
        */
       public Throwable getException() throws IllegalStateException {
 			assertHasRun();
          return _exception;
       }
    }
 }
