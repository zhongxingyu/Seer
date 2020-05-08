 /**
  * Sahara Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
 * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
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
  * @date 5th October 2009
  *
  * Changelog:
  * - 05/10/2009 - mdiponio - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.main;
 
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRig;
 import au.edu.uts.eng.remotelabs.rigclient.server.EmbeddedJettyServer;
 import au.edu.uts.eng.remotelabs.rigclient.server.IServer;
 import au.edu.uts.eng.remotelabs.rigclient.status.StatusUpdater;
 import au.edu.uts.eng.remotelabs.rigclient.type.RigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Startup and shutdown class. 
  * Shutdown error codes:
  * <ul>
  *  <li>0 - exit normal.</li>
  *  <li>1 - unhandled exception.</li>
  *  <li>2 - rig type class cannot be loaded and resolved.</li>
  *  <li>3 - rig client server cannot be started.</li>
  * </ul>
  */
 public class RigClient
 {
     /** Listening server. */
     private final IServer server;
     
     /** Status updater. */
     private Thread statusThread;
     
     /** Shutdown flag. */
     private static boolean shutdown;
     
     /** Logger. */
     private final ILogger logger;
     
     /** Configuration. */
     private final IConfig config;
     
     /**
      * Constructor.
      */
     public RigClient()
     {
         this.logger = LoggerFactory.getLoggerInstance();
         this.config = ConfigFactory.getInstance();
         this.server = new EmbeddedJettyServer();
         
     }
     
     /**
      * Runs the program forever, until notified to shutdown.
      */
     public void runProgram()
     {
         try
         {
             this.logger.priority("Rig client is starting up...");
             if (this.config.getProperty("Rig_Name") == null)
             {
                 this.logger.fatal("Unable to load the rig's name. Unrecoverable, please check configuration to " +
                         "ensure a valid rig name is specified ('Rig_Name' property).");
                 System.exit(2);
             }
             this.logger.priority("Rig name: " + this.config.getProperty("Rig_Name"));
             
             if (this.config.getProperty("Rig_Type") == null)
             {
                 this.logger.fatal("Unable to load the rig's type. Unrecoverable, please check configuration to " +
                         "ensure a valid rig type is specified ('Rig_Type' property).");
                 System.exit(2);
             }
             this.logger.priority("Rig type: " + this.config.getProperty("Rig_Type"));
 
             /* ------------------------------------------------------------------
              * ---- 1. Get the rig type class and start the exerciser tests. ----
              * ----------------------------------------------------------------*/
             final IRig rig = RigFactory.getRigInstance();
             if (rig == null)
             {
                 this.logger.fatal("Unable to load rig type class. Unrecoverable, please check configuration to" +
                         " ensure a valid rig type class is specified.");
                 System.exit(2);
             }
             rig.startTests();
             
             /* ------------------------------------------------------------------
              * ---- 2. If the server isn't running, start it up. ----------------
              * ----------------------------------------------------------------*/
             if (!this.server.startListening())
             {
                 this.logger.fatal("Unable to start the rig client server. Unrecoverable, please check configuration," +
                 		" to ensure a valid port number and the operating system process list to ensure no other" +
                 		" rig clients are running.");
                 System.exit(3);
             }
             
             while (!this.server.isListening())
             {
                 Thread.sleep(1000);
             }
             
             /* ------------------------------------------------------------------
              * ---- 3. Start the registration and status notification service. --
              * ----------------------------------------------------------------*/
             try
             {
                 StatusUpdater statusUpdater = new StatusUpdater(this.server.getAddress()[0]);
                 this.statusThread = new Thread(statusUpdater);
                 this.statusThread.start();
             }
             catch (Exception ex)
             {
                 this.logger.fatal("Unable to start the status updater because " + ex.getMessage() + '.');
                 System.exit(4);
             }
             
             /* ------------------------------------------------------------------
              * ---- 4. Enter the run loop and wait for shutdown. ----------------
              * ----------------------------------------------------------------*/
             while (!RigClient.shutdown)
             {
                 try
                 {                    
                     Thread.sleep(5000);
                 }
                 catch (InterruptedException e)
                 {
                     break;
                 }
             }
 
             /* ------------------------------------------------------------------
              * ---- 5. Cleanup and shutdown all services. -----------------------
              * ----------------------------------------------------------------*/
             this.logger.priority("Received shut down request, shutting down...");
             /* Purge all sessions (if running). */
             if (rig.isSessionActive())
             {
                 rig.revoke();
             }
             
             /* Shutdown registration and status update service. 30 seconds 
              * *should* be enough time to remove rig registration, but 
              * we can't wait any longer because services have an expectation
              * shutting down quickly.  */ 
             this.statusThread.interrupt();
             this.statusThread.join(30000);
             
             /* Stop exerciser tests. */
             rig.stopTests();
             
             /* Stop server. */
             this.server.stopListening();
         } 
         catch (Throwable thr)
         {
             this.logger.fatal("Unhandled exception " + thr.getClass().getSimpleName() + " with error message " +
                     thr.getMessage() + ". This is a proverbial blue screen of death, so please file a bug report.");
             RigClientDefines.reportBug("Unhandled exception which popped the stack.", thr);
             System.exit(1);
         }
         
         /* Sorry to see you go... */
         System.exit(0);
     }
     
     /**
      * Starts the rig client (called through JNI).
      */
     public static void start()
     {
         RigClient.shutdown = false;
         final RigClient rigClient = new RigClient();
         rigClient.runProgram();
     }
     
     /**
      * Notifies the rig client to shutdown (called through JNI).
      */
     public static void stop()
     {
         RigClient.shutdown = true;
     }
 }
