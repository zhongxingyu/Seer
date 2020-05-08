 // Copyright (C) 2000 Paco Gomez
 // Copyright (C) 2000, 2001, 2002, 2003, 2004 Philip Aston
 // Copyright (C) 2004 Bertrand Ave
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.engine.agent;
 
 import java.io.File;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import net.grinder.common.Logger;
 import net.grinder.common.GrinderBuild;
 import net.grinder.common.GrinderException;
 import net.grinder.common.GrinderProperties;
 import net.grinder.communication.ClientReceiver;
 import net.grinder.communication.CommunicationDefaults;
 import net.grinder.communication.CommunicationException;
 import net.grinder.communication.ConnectionType;
 import net.grinder.communication.Connector;
 import net.grinder.communication.FanOutStreamSender;
 import net.grinder.communication.InitialiseGrinderMessage;
 import net.grinder.communication.Message;
 import net.grinder.communication.MessagePump;
 import net.grinder.communication.Receiver;
 import net.grinder.communication.Sender;
 import net.grinder.communication.StreamSender;
 import net.grinder.communication.TeeSender;
 import net.grinder.engine.common.ConsoleListener;
 import net.grinder.engine.common.EngineException;
 import net.grinder.engine.process.GrinderProcess;
 import net.grinder.util.thread.Kernel;
 
 
 /**
  * This is the entry point of The Grinder agent process.
  *
  * @author Paco Gomez
  * @author Philip Aston
  * @author Bertrand Ave
  * @version $Revision$
  */
 public final class Agent {
 
   private final File m_alternateFile;
 
   private final Logger m_logger =
     new AgentLogger(new PrintWriter(System.out), new PrintWriter(System.err));
 
   /**
    * Constructor.
    */
   public Agent() {
     this(null);
   }
 
   /**
    * Constructor.
    *
    * @param alternateFile Alternative properties file.
    */
   public Agent(File alternateFile) {
     m_alternateFile = alternateFile;
   }
 
   /**
    * Run the Grinder agent process.
    *
    * @throws GrinderException If an error occurs.
    * @throws InterruptedException If the calling thread is
    * interrupted whilst waiting.
    */
   public void run() throws GrinderException, InterruptedException {
 
     final Timer timer = new Timer(true);
     boolean startImmediately = false;
 
     while (true) {
       m_logger.output("The Grinder version " +
                       GrinderBuild.getVersionString());
 
       final GrinderProperties properties =
         new GrinderProperties(m_alternateFile);
 
       final Object eventSynchronisation = new Object();
       final FanOutStreamSender fanOutStreamSender = new FanOutStreamSender(3);
       final ConsoleListener consoleListener =
         new ConsoleListener(eventSynchronisation, m_logger);
 
       Receiver receiver = null;
 
       if (properties.getBoolean("grinder.useConsole", true)) {
         final Connector connector =
           new Connector(
             properties.getProperty("grinder.consoleHost",
                                    CommunicationDefaults.CONSOLE_HOST),
             properties.getInt("grinder.consolePort",
                               CommunicationDefaults.CONSOLE_PORT),
             ConnectionType.CONTROL);
 
         try {
           receiver = ClientReceiver.connect(connector);
 
           // Ordering of the TeeSender is important so the child
           // processes get the signals before our console listener.
           final Sender sender =
             new TeeSender(fanOutStreamSender, consoleListener.getSender());
 
           new MessagePump(receiver, sender, 1);
         }
         catch (CommunicationException e) {
           m_logger.error(
             e.getMessage() + ", proceeding without the console; set " +
             "grinder.useConsole=false to disable this warning.");
         }
       }
 
       final ProcessLauncher processLauncher =
         new ProcessLauncher(properties, fanOutStreamSender,
                            new InitialiseGrinderMessage(receiver != null),
                            eventSynchronisation);
 
       if (!startImmediately && receiver != null) {
         m_logger.output("waiting for console signal");
         consoleListener.waitForMessage();
       }
 
       if (receiver == null ||
           startImmediately ||
           consoleListener.received(ConsoleListener.START)) {
 
         final int processIncrement =
           properties.getInt("grinder.processIncrement", 0);
 
         if (processIncrement > 0) {
           final boolean moreProcessesToStart =
             processLauncher.startSomeProcesses(
               properties.getInt("grinder.initialProcesses", processIncrement));
 
           if (moreProcessesToStart) {
             final int incrementInterval =
               properties.getInt("grinder.processIncrementInterval", 60000);
 
             final RampUpTimerTask rampUpTimerTask =
               new RampUpTimerTask(processLauncher, processIncrement);
 
             timer.scheduleAtFixedRate(
               rampUpTimerTask, incrementInterval, incrementInterval);
           }
         }
         else {
           processLauncher.startAllProcesses();
         }
 
         // Wait for a termination event.
         synchronized (eventSynchronisation) {
           final long maximumShutdownTime = 20000;
           long consoleSignalTime = -1;
 
           while (!processLauncher.allFinished()) {
 
             if (consoleListener.checkForMessage(ConsoleListener.ANY ^
                                                 ConsoleListener.START)) {
               processLauncher.dontStartAnyMore();
               consoleSignalTime = System.currentTimeMillis();
             }
 
             if (consoleSignalTime >= 0 &&
                 System.currentTimeMillis() - consoleSignalTime >
                 maximumShutdownTime) {
 
               m_logger.output("forcibly terminating unresponsive processes");
               processLauncher.destroy();
             }
 
             eventSynchronisation.wait(maximumShutdownTime);
           }
         }
       }
 
       if (receiver == null) {
         break;
       }
       else {
         if (!consoleListener.received(ConsoleListener.ANY)) {
           // We've got here naturally, without a console signal.
           m_logger.output("finished, waiting for console signal");
 
           consoleListener.waitForMessage();
         }
 
         receiver.shutdown();
 
         if (consoleListener.received(ConsoleListener.START)) {
           startImmediately = true;
         }
         else if (consoleListener.received(ConsoleListener.STOP |
                                           ConsoleListener.SHUTDOWN)) {
           break;
         }
         else {
           // ConsoleListener.RESET or natural death.
           startImmediately = false;
         }
       }
     }
 
     m_logger.output("finished");
   }
 
   private class ProcessLauncher {
 
     private final Kernel m_kernel = new Kernel(1);
     private final FanOutStreamSender m_fanOutStreamSender;
     private final Message m_initialisationMessage;
     private final Object m_notifyOnFinish;
 
     private final String[] m_commandArray;
     private final int m_grinderIDIndex;
     private final String m_hostIDString;
 
     /**
      * Fixed size array with a slot for all potential processes.
      * Synchronise on m_processes before accessing entries. If an
      * entry is null and its index is less than m_nextProcessIndex,
      * the process has finished or the ProcessLauncher has been
      * shutdown.
      */
     private final ChildProcess[] m_processes;
 
     /**
      * The next process to start. Only increases.
      */
     private int m_nextProcessIndex = 0;
 
     public ProcessLauncher(GrinderProperties properties,
                           FanOutStreamSender fanOutStreamSender,
                           InitialiseGrinderMessage initialisationMessage,
                           Object notifyOnFinish) {
 
       m_fanOutStreamSender = fanOutStreamSender;
       m_initialisationMessage = initialisationMessage;
       m_notifyOnFinish = notifyOnFinish;
 
       m_processes =
         new ChildProcess[properties.getInt("grinder.processes", 1)];
 
       final List command = new ArrayList();
       command.add(properties.getProperty("grinder.jvm", "java"));
 
       final String jvmArguments =
         properties.getProperty("grinder.jvm.arguments");
 
       if (jvmArguments != null) {
         // Really should allow whitespace to be escaped/quoted.
         final StringTokenizer tokenizer = new StringTokenizer(jvmArguments);
 
         while (tokenizer.hasMoreTokens()) {
           command.add(tokenizer.nextToken());
         }
       }
 
       // Pass through any "grinder" system properties.
       final Iterator systemProperties =
         System.getProperties().entrySet().iterator();
 
       while (systemProperties.hasNext()) {
         final Map.Entry entry = (Map.Entry)systemProperties.next();
         final String key = (String)entry.getKey();
         final String value = (String)entry.getValue();
 
         if (key.startsWith("grinder.")) {
           command.add("-D" + key + "=" + value);
         }
       }
 
       final String additionalClasspath =
         properties.getProperty("grinder.jvm.classpath", null);
 
       final String classpath =
         (additionalClasspath != null ?
          additionalClasspath + File.pathSeparatorChar : "") +
         System.getProperty("java.class.path");
 
       if (classpath.length() > 0) {
         command.add("-classpath");
         command.add(classpath);
       }
 
       command.add(GrinderProcess.class.getName());
 
       m_hostIDString = properties.getProperty("grinder.hostID", getHostName());
 
       m_grinderIDIndex = command.size();
       command.add("");    // Place holder for grinder ID.
 
       if (m_alternateFile != null) {
         command.add(m_alternateFile.getPath());
       }
 
       m_commandArray = (String[])command.toArray(new String[0]);
 
       m_commandArray[m_grinderIDIndex] = "<grinderID>";
 
       final StringBuffer buffer = new StringBuffer(m_commandArray.length * 10);
       buffer.append("Worker process command line:");
 
       for (int j = 0; j < m_commandArray.length; ++j) {
         buffer.append(" ");
         buffer.append(m_commandArray[j]);
       }
 
       m_logger.output(buffer.toString());
     }
 
     public void startAllProcesses() {
       startSomeProcesses(m_processes.length - m_nextProcessIndex);
     }
 
     public boolean startSomeProcesses(int numberOfProcesses) {
 
       final int numberToStart =
         Math.min(numberOfProcesses, m_processes.length - m_nextProcessIndex);
 
       for (int i = 0; i < numberToStart; ++i) {
         final int processIndex = m_nextProcessIndex;
 
         final String grinderID = m_hostIDString + "-" + processIndex;
         m_commandArray[m_grinderIDIndex] = grinderID;
 
         try {
           final ChildProcess process =
             new ChildProcess(m_commandArray, System.out, System.err);
 
           final OutputStream processStdin = process.getStdinStream();
 
           new StreamSender(processStdin).send(m_initialisationMessage);
           m_fanOutStreamSender.add(processStdin);
 
           synchronized(m_processes) {
             m_processes[processIndex] = process;
           }
         }
         catch (EngineException e) {
           m_logger.error("Failed to create process");
           e.printStackTrace(m_logger.getErrorLogWriter());
           return false;
         }
         catch (CommunicationException e) {
           m_logger.error("Failed to communicate with process");
           e.printStackTrace(m_logger.getErrorLogWriter());
           return false;
         }
 
         try {
           m_kernel.execute(new ReapTask(processIndex));
         }
         catch (Kernel.ShutdownException e) {
           m_logger.error("Kernel unexpectedly shutdown");
           e.printStackTrace(m_logger.getErrorLogWriter());
           return false;
         }
 
         ++m_nextProcessIndex;
 
         final StringBuffer buffer = new StringBuffer();
         m_logger.output("process " + grinderID + " started");
       }
 
       return m_processes.length > m_nextProcessIndex;
     }
 
     private final class ReapTask implements Runnable {
 
       private final int m_processIndex;
 
       public ReapTask(int processIndex) {
         m_processIndex = processIndex;
       }
 
       public void run() {
         try {
           final ChildProcess process;
 
           synchronized (m_processes) {
             process = m_processes[m_processIndex];
           }
 
           if (process != null) {
             process.waitFor();
 
             synchronized (m_processes) {
               m_processes[m_processIndex] = null;
             }
           }
         }
         catch (InterruptedException e) {
           // Really an assertion failure. Can't use m_logger here
           // because its not thread safe.
           e.printStackTrace();
         }
         catch (EngineException e) {
           // Really an assertion failure. Can't use m_logger here
           // because its not thread safe.
           e.printStackTrace();
         }
 
         if (allFinished()) {
           synchronized (m_notifyOnFinish) {
             m_notifyOnFinish.notifyAll();
           }
         }
       }
     }
 
     public boolean allFinished() {
      if (m_nextProcessIndex < m_processes.length) {
        return false;
      }

      synchronized(m_processes) {
        for (int i = 0; i < m_processes.length; i++) {
          if (m_processes[i] != null) {
            return false;
           }
         }
       }
 
      return true;
     }
 
     public void dontStartAnyMore() {
       m_nextProcessIndex = m_processes.length;
     }
     
     public void destroy() {
       dontStartAnyMore();
 
       synchronized(m_processes) {
         for (int i = 0; i < m_processes.length; i++) {
           if (m_processes[i] != null) {
             m_processes[i].destroy();
           }
         }
       }
     }
 
     private String getHostName() {
       try {
         return InetAddress.getLocalHost().getHostName();
       }
       catch (UnknownHostException e) {
         return "UNNAMED HOST";
       }
     }
   }
 
   private class RampUpTimerTask extends TimerTask {
 
     private final ProcessLauncher m_processLauncher;
     private final int m_processIncrement;
     private boolean m_rampUpFinished = false;
 
     public RampUpTimerTask(ProcessLauncher processLauncher,
                            int processIncrement) {
       m_processLauncher = processLauncher;
       m_processIncrement = processIncrement;
     }
 
     public void run () {
       final boolean moreProcessesToStart =
         m_processLauncher.startSomeProcesses(m_processIncrement);
 
       if (!moreProcessesToStart) {
         super.cancel();
 
         synchronized (this) {
           m_rampUpFinished = true;
           notifyAll();
         }
       }
     }
   }
 }
