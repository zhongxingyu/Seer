 // Copyright (C) 2004 - 2008 Philip Aston
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
 // COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.engine.agent;
 
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import junit.framework.TestCase;
 import net.grinder.common.Logger;
 import net.grinder.common.LoggerStubFactory;
 import net.grinder.engine.agent.AgentIdentityImplementation.WorkerIdentityImplementation;
 import net.grinder.engine.common.EngineException;
 import net.grinder.messages.console.WorkerIdentity;
 import net.grinder.testutility.AssertUtilities;
 import net.grinder.testutility.CallData;
 import net.grinder.testutility.RedirectStandardStreams;
 import net.grinder.util.thread.Condition;
 import net.grinder.util.thread.Executor;
 
 
 /**
  *  Unit tests for <code>WorkerLauncher</code>.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestWorkerLauncher extends TestCase {
 
   private static final String s_testClasspath =
     System.getProperty("java.class.path");
 
   public void testConstructor() throws Exception {
     final WorkerLauncher workerLauncher1 =
       new WorkerLauncher(0, null, null, null);
 
     assertTrue(workerLauncher1.allFinished());
     workerLauncher1.shutdown();
 
     final WorkerLauncher workerLauncher2 =
       new WorkerLauncher(10, null, null, null);
 
     assertFalse(workerLauncher2.allFinished());
 
     workerLauncher2.destroyAllWorkers();
     workerLauncher2.shutdown();
   }
 
   public void testStartSomeProcesses() throws Exception {
 
     final LoggerStubFactory loggerStubFactory = new LoggerStubFactory();
     final Logger logger = loggerStubFactory.getLogger();
     final MyCondition condition = new MyCondition();
     final MyWorkerFactory myProcessFactory = new MyWorkerFactory();
 
     final WorkerLauncher workerLauncher =
       new WorkerLauncher(5, myProcessFactory, condition, logger);
 
     condition.waitFor(workerLauncher);
     assertFalse(condition.isFinished());
 
     assertEquals(0, myProcessFactory.getNumberOfProcesses());
 
     workerLauncher.startSomeWorkers(1);
 
     assertEquals(1, myProcessFactory.getNumberOfProcesses());
 
     assertFalse(workerLauncher.allFinished());
     assertEquals(System.out, myProcessFactory.getLastOutputStream());
     assertEquals(System.err, myProcessFactory.getLastErrorStream());
 
     assertEquals(1, myProcessFactory.getChildProcesses().size());
     final Worker childProcess =
       (Worker)myProcessFactory.getChildProcesses().get(0);
 
     final CallData call =
       loggerStubFactory.assertSuccess("output", String.class);
     final String s = (String)call.getParameters()[0];
     AssertUtilities.assertContains(s, childProcess.getIdentity().getName());
     loggerStubFactory.assertNoMoreCalls();
 
     workerLauncher.startSomeWorkers(10);
     assertEquals(5, myProcessFactory.getNumberOfProcesses());
 
     loggerStubFactory.assertSuccess("output", new Class[] { String.class });
     loggerStubFactory.assertSuccess("output", new Class[] { String.class });
     loggerStubFactory.assertSuccess("output", new Class[] { String.class });
     loggerStubFactory.assertSuccess("output", new Class[] { String.class });
     loggerStubFactory.assertNoMoreCalls();
 
     assertEquals(5, myProcessFactory.getChildProcesses().size());
 
     assertFalse(workerLauncher.allFinished());
 
     final Worker[] processes =
       (Worker[])
       myProcessFactory.getChildProcesses().toArray(new Worker[0]);
 
     sendTerminationMessage(processes[0]);
     sendTerminationMessage(processes[2]);
 
     assertFalse(workerLauncher.allFinished());
     assertFalse(condition.isFinished());
 
     sendTerminationMessage(processes[1]);
     sendTerminationMessage(processes[3]);
     sendTerminationMessage(processes[4]);
 
     // Can't be bothered to add another layer of synchronisation, just
     // spin.
     while (!condition.isFinished()) {
       Thread.sleep(20);
     }
 
     assertTrue(workerLauncher.allFinished());
     workerLauncher.shutdown();
   }
 
   private void sendTerminationMessage(Worker process) {
     final PrintWriter processStdin =
       new PrintWriter(process.getCommunicationStream());
 
     processStdin.print("Foo\n");
     processStdin.flush();
   }
 
   public void testStartAllProcesses() throws Exception {
 
     final LoggerStubFactory loggerStubFactory = new LoggerStubFactory();
     final Logger logger = loggerStubFactory.getLogger();
     final MyCondition condition = new MyCondition();
     final MyWorkerFactory myProcessFactory = new MyWorkerFactory();
 
     final WorkerLauncher workerLauncher =
       new WorkerLauncher(9, myProcessFactory, condition, logger);
 
     condition.waitFor(workerLauncher);
     assertFalse(condition.isFinished());
 
     assertEquals(0, myProcessFactory.getNumberOfProcesses());
 
     workerLauncher.startAllWorkers();
 
     assertEquals(9, myProcessFactory.getNumberOfProcesses());
 
     assertFalse(workerLauncher.allFinished());
     assertEquals(System.out, myProcessFactory.getLastOutputStream());
     assertEquals(System.err, myProcessFactory.getLastErrorStream());
 
     assertEquals(9, myProcessFactory.getChildProcesses().size());
 
     final Worker[] processes =
       (Worker[])
       myProcessFactory.getChildProcesses().toArray(new Worker[0]);
 
     sendTerminationMessage(processes[0]);
     sendTerminationMessage(processes[6]);
     sendTerminationMessage(processes[5]);
     sendTerminationMessage(processes[2]);
     sendTerminationMessage(processes[7]);
 
     assertFalse(workerLauncher.allFinished());
     assertFalse(condition.isFinished());
 
     sendTerminationMessage(processes[8]);
     sendTerminationMessage(processes[1]);
     sendTerminationMessage(processes[3]);
     sendTerminationMessage(processes[4]);
 
     // Can't be bothered to add another layer of synchronisation, just
     // spin.
     while (!condition.isFinished()) {
       Thread.sleep(20);
     }
 
     assertTrue(workerLauncher.allFinished());
     workerLauncher.shutdown();
   }
 
   public void testBadExecutor() throws Exception {
 
     final LoggerStubFactory loggerStubFactory = new LoggerStubFactory();
     final Logger logger = loggerStubFactory.getLogger();
     final MyCondition condition = new MyCondition();
     // Can't use a ProcessWorker because we can't interrupt a thread
     // blocking in Process.waitFor().
     final NullWorkerFactory myProcessFactory = new NullWorkerFactory();
     final Executor executor = new Executor(1);
 
     final WorkerLauncher workerLauncher =
       new WorkerLauncher(executor, 9, myProcessFactory, condition, logger);
 
     final boolean result1 = workerLauncher.startSomeWorkers(1);
     assertTrue(result1);
     loggerStubFactory.assertOutputMessageContains("started");
     loggerStubFactory.assertNoMoreCalls();
 
     while (myProcessFactory.getNumberOfLiveProcesses() != 1) {
       Thread.sleep(20);
     }
 
     executor.forceShutdown();
     assertFalse(condition.isFinished());
     while (myProcessFactory.getNumberOfDestroyedProcesses() != 1) {
       Thread.sleep(20);
     }
 
     final boolean result2 = workerLauncher.startSomeWorkers(1);
     assertFalse(result2);
     loggerStubFactory.assertErrorMessageContains("shutdown");
     loggerStubFactory.assertSuccess("getErrorLogWriter");
     loggerStubFactory.assertNoMoreCalls();
 
     assertEquals(2, myProcessFactory.getNumberOfDestroyedProcesses());
   }
 
   public void testDestroyAllProcesses() throws Exception {
 
     final LoggerStubFactory loggerStubFactory = new LoggerStubFactory();
     final Logger logger = loggerStubFactory.getLogger();
     final MyCondition condition = new MyCondition();
     final MyWorkerFactory myProcessFactory = new MyWorkerFactory();
 
     final WorkerLauncher workerLauncher =
       new WorkerLauncher(4, myProcessFactory, condition, logger);
 
     condition.waitFor(workerLauncher);
     assertFalse(condition.isFinished());
 
     assertEquals(0, myProcessFactory.getNumberOfProcesses());
 
     final RedirectStandardStreams redirectStreams =
       new RedirectStandardStreams() {
         protected void runWithRedirectedStreams() throws Exception {
           workerLauncher.startAllWorkers();
         }
       };
 
     redirectStreams.run();
 
     assertEquals(4, myProcessFactory.getNumberOfProcesses());
 
     assertFalse(workerLauncher.allFinished());
     assertEquals(4, myProcessFactory.getChildProcesses().size());
 
     final Worker[] processes =
       (Worker[])
       myProcessFactory.getChildProcesses().toArray(new Worker[0]);
 
     sendTerminationMessage(processes[1]);
     sendTerminationMessage(processes[3]);
 
     assertFalse(workerLauncher.allFinished());
     assertFalse(condition.isFinished());
 
     workerLauncher.destroyAllWorkers();
 
     // Can't be bothered to add another layer of synchronisation, just
     // spin.
     while (!condition.isFinished()) {
       Thread.sleep(20);
     }
 
     assertTrue(workerLauncher.allFinished());
     workerLauncher.shutdown();
   }
 
   private static class MyCondition extends Condition {
     private boolean m_finished;
 
     public synchronized void waitFor(final WorkerLauncher workerLauncher) {
 
       m_finished = false;
 
       new Thread() {
         public void run() {
           try {
             synchronized (MyCondition.this) {
               while (!workerLauncher.allFinished()) {
                 MyCondition.this.wait();
               }
             }
 
             m_finished = true;
           }
           catch (InterruptedException e) {
           }
         }
       }.start();
     }
 
     public boolean isFinished() {
       return m_finished;
     }
   }
 
   private static class MyWorkerFactory implements WorkerFactory {
 
     private int m_numberOfProcesses = 0;
     private OutputStream m_lastOutputStream;
     private OutputStream m_lastErrorStream;
     private ArrayList m_childProcesses = new ArrayList();
     private StubAgentIdentity m_agentIdentity =
       new StubAgentIdentity("process");
 
     public Worker create(OutputStream outputStream, OutputStream errorStream)
       throws EngineException {
 
       m_lastOutputStream = outputStream;
       m_lastErrorStream = errorStream;
 
       final String[] commandArray = {
         "java",
         "-classpath",
         s_testClasspath,
         EchoClass.class.getName(),
       };
 
       final Worker childProcess =
         new ProcessWorker(m_agentIdentity.createWorkerIdentity(),
                           commandArray,
                           outputStream,
                           errorStream);
       ++m_numberOfProcesses;
       m_childProcesses.add(childProcess);
 
       return childProcess;
     }
 
     public int getNumberOfProcesses() {
       return m_numberOfProcesses;
     }
 
     public OutputStream getLastOutputStream() {
       return m_lastOutputStream;
     }
 
     public OutputStream getLastErrorStream() {
       return m_lastErrorStream;
     }
 
     public ArrayList getChildProcesses() {
       return m_childProcesses;
     }
   }
 
   private static class NullWorkerFactory implements WorkerFactory {
     private StubAgentIdentity m_agentIdentity =
       new StubAgentIdentity("process");
     private int m_numberOfDestroyedProcesses = 0;
     private int m_numberOfLiveProcesses = 0;
 
     public Worker create(OutputStream outputStream, OutputStream errorStream)
       throws EngineException {
 
       return new NullWorker(m_agentIdentity.createWorkerIdentity());
     }
 
     public synchronized int getNumberOfLiveProcesses() {
       return m_numberOfLiveProcesses;
     }
 
     public synchronized int getNumberOfDestroyedProcesses() {
       return m_numberOfDestroyedProcesses;
     }
 
     private class NullWorker implements Worker {
       private WorkerIdentity m_workerIdentity;
 
       public NullWorker(WorkerIdentityImplementation workerIdentity) {
         m_workerIdentity = workerIdentity;
       }
 
       public void destroy() {
         synchronized (NullWorkerFactory.this) {
           ++m_numberOfDestroyedProcesses;
         }
       }
 
       public OutputStream getCommunicationStream() {
         return null;
       }
 
       public WorkerIdentity getIdentity() {
         return m_workerIdentity;
       }
 
       public int waitFor() {
         synchronized (NullWorkerFactory.this) {
           ++m_numberOfLiveProcesses;
         }
 
         final Condition condition = new Condition();
 
         synchronized (condition) {
           condition.waitNoInterrruptException();
         }
 
         return 0;
       }
     }
   }
 }
