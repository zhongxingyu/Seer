 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 18th January 2010
  *
  * Changelog:
  * - 18/01/2010 - mdiponio - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.action.test;
 
 import java.util.Random;
 
 import au.edu.uts.eng.remotelabs.rigclient.rig.ITestAction;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Abstract test action class which uses a run flag to implement the 
  * <code>ITestAction.run</code> contract which specifies a persistent
  * thread never terminating for the life of the rig client. The following
  * flags may be modified to change its behavior:   
  * <ul>	
  *  <li><code>isPeriodic</code> - Specifies if the test interval is 
  *  periodic (<code>true</code>), so runs every <code>runInterval</code> 
  *  seconds or if it runs at a random time between 0 seconds and the 
  *  <code>runInterval</code> seconds (<code>false</code>). Generally 
  *  tests which result in <em>visible</em> effects (i.e. something moves) 
  *  should not be periodic to mimic real lab use (not everything lab
  *  runs at the same time). The default is for tests to be periodic. </li>
  *  <li><code>isSetIntervalHonoured</code> - Specifies if the test 
  *  interval is changed when the <code>setInterval</code> method is 
  *  invoked. The default is to honour the test interval.</li>
  *  <li><code>runInterval - The time between each test run in seconds. The 
  *  default is 60 seconds.</li>
  * </ul>
  * <strong>NOTE:</strong> The abstract methods declared in this class
 * do not provide any indication of the success of their invocation. The 
  * relative success or failure of them should be indicated by calls
  * to <code>getStatus</code> and <code>getReason</code>.
  */
 public abstract class AbstractTestAction implements ITestAction
 {
     /** Flag to specify if the test is in run mode or wait mode. */
     protected boolean runTest;
     
     /** The maximum amount of seconds between test runs. */
     protected int runInterval = 60;
     
     /** Whether the test is periodic or aperiodic. */
     protected boolean isPeriodic = true;
     
     /** Whether a call to setInterval will actually cause the test run 
      *  interval to be changed (i.e. if the specified test interval is 
      *  honoured). The default is to honour the specified test interval. */
     protected boolean isSetIntervalHonoured = true;
     
     /** Random number generator. */
     protected final Random randomNumGen;
     
     /** Logger. */
     protected final ILogger logger;
     
     /** Configuration. */
     protected final IConfig config;
     
     public AbstractTestAction()
     {
         this.logger = LoggerFactory.getLoggerInstance();
         this.config = ConfigFactory.getInstance();
         
         this.randomNumGen = new Random();
     }
     
     @Override
     public void run()
     {
         int sleepCount;
         
         /* Run setup. */
         this.setUp();
         
         try
         {
             while (!Thread.interrupted())
             {
                 /* Instead of one long sleep, this is a series of short sleeps, so 
                  * if the test run interval is shortened past the existing sleep interval,
                  * the existing sleep time is reduced to within the new test run
                  * interval. */
                 if (this.isPeriodic)
                 {
                     sleepCount = this.runInterval;
                 }
                 else
                 {
                     sleepCount = this.randomNumGen.nextInt(this.runInterval + 1);
                 }
                 while (sleepCount > 0)
                 {
                     if (this.runInterval < sleepCount) // Occurs when the test interval time is reduced to 
                     {                                  // by a call to setInterval.
                         sleepCount = this.randomNumGen.nextInt(this.runInterval + 1);
                     }
                     Thread.sleep(1000);
                     sleepCount--;
                 }
                 
                 if (this.runTest)
                 {
                     this.doTest();
                 }
             }
         }
         catch (InterruptedException ex)
         {
             this.logger.info("Exerciser test " + this.getActionType() + " interrupted, shutting down.");
         }
         
         this.tearDown();
     }
     
     /**
      * Setup method that runs once before the first execution of the 
      * <code>doTest</code> method.
      */
     public abstract void setUp();
     
     /**
      * This method is the actual test method. This method is invoked at each 
      * test interval when in run test mode. Whatever occurs within this method,
      * calls to <code>getStatus</code> and <code>getReason</code> should
      * reflect the results of this method.
      * <p />
      * If this method is long in duration (more than a few seconds), the 
      * <code>runTest</code> flag should be regularly polled to ensure the test
      * stops when it is instructed to stop (so when the flag is set to false,
      * this method should return).
      */
     public abstract void doTest();
     
     /**
      * Cleanup method that is called when the test is shutdown.
      */
     public abstract void tearDown();
     
     @Override
     public void setInterval(int interval)
     {
         if (this.isSetIntervalHonoured)
         {
             this.logger.info("Changing the test interval for " + this.getActionType() + " to " + interval + ".");
             this.runInterval = interval;
         }
         else
         {
             this.logger.info("Ignoring request to change test interval for " + this.getActionType() + " to "
                     + interval + ". Keeping test interval at " + this.runInterval + ".");
         }
     }
 
     @Override
     public void startTest()
     {
         this.logger.debug("Starting " + this.getActionType() + " exerciser test.");
         this.runTest = true;
     }
 
     @Override
     public void stopTest()
     {
         this.logger.debug("Stopping " + this.getActionType() + " exerciser test.");
         this.runTest = false;
     }
 
     @Override
     public String getFailureReason()
     {
         /* This method is redundant in <code>ITestAction</code implementations
          * as the error reason method is <code>getReason</code>. */
         return null;
     }
 }
