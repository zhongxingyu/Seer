 package org.openengsb.labs.endtoend.karaf.shell;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.openengsb.labs.endtoend.karaf.CommandTimeoutException;
 import org.openengsb.labs.endtoend.karaf.output.KarafPromptRecognizer;
 import org.openengsb.labs.endtoend.karaf.output.OutputHandler;
 import org.openengsb.labs.endtoend.util.TimeoutableProcess;
 
 public class KarafClientShell extends AbstractKarafShell implements RemoteShell {
     private final File startCmd;
     private PrintWriter pw;
     private TimeoutableProcess process;
     private OutputHandler outputHandler;
 
     boolean tryAgain = true; // Helper for login(...).
 
     public KarafClientShell(final File startCmd) {
         this.startCmd = startCmd;
     }
 
     public void login(final String applicationName, final String host, final Integer port, final String user,
            final String pass, final Long timeout, final TimeUnit timeUnit) throws CommandTimeoutException {
 
         Long timeoutNanos = timeUnit.toNanos(timeout);
 
         // Try to start client process. If karaf ssh port not yet ready, try again until success or timeout.
         while (tryAgain) {
             Long startNanoTime = System.nanoTime();
 
             tryAgain = false;
 
             KarafClientShell.this.startClient(applicationName, host, port, user, pass);
 
             Thread workerThread = new Thread(new Runnable() {
                 @Override
                 public void run() {
                     try {
                        KarafClientShell.this.process.waitFor();
                        Thread.sleep(50);
                        // Try it again!
                        tryAgain = true;
                        KarafClientShell.this.killClient();
                     } catch (InterruptedException e) {
                     }
                 }
             });
             workerThread.start();
 
             try {
                 this.outputHandler.waitForPrompt(timeoutNanos, TimeUnit.NANOSECONDS);
                 if (!tryAgain) {
                     // Client running and prompt found.
                     workerThread.interrupt();
                     return;
                 }
 
                 // Reduce timeout for next attempt.
                 timeoutNanos -= System.nanoTime() - startNanoTime;
 
             } catch (TimeoutException e) {
                 killClient();
                 throw new CommandTimeoutException("login", e);
             }
         }
     }
 
     private void startClient(String applicationName, String host, Integer port, String user, String pass) {
         this.startCmd.setExecutable(true);
         ProcessBuilder processBuilder = new ProcessBuilder(this.startCmd.getAbsolutePath(), "-a", port.toString(),
                 "-h", host, "-u", user);
         processBuilder.environment().put("KARAF_OPTS", "-Djline.terminal=off");
 
         try {
             this.process = new TimeoutableProcess(processBuilder.start());
         } catch (IOException e) {
             throw new IllegalStateException("Could not start client.", e);
         }
 
         this.pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream())));
         if (!pass.isEmpty()) {
             this.pw.println(pass);
             this.pw.flush();
         }
         this.outputHandler = new OutputHandler(new InputStreamReader(this.process.getInputStream()),
                 new KarafPromptRecognizer(user, applicationName));
     }
 
     private void killClient() {
         try {
             stopClient(0L, TimeUnit.SECONDS);
         } catch (TimeoutException e) {
             // Expected.
         }
     }
 
     private void stopClient(Long timeout, TimeUnit timeUnit) throws TimeoutException {
         this.pw.close();
         this.outputHandler.shutdown();
         try {
             this.process.waitFor(timeout, timeUnit);
         } catch (TimeoutException e) {
             this.process.destroy();
             throw e;
         }
     }
 
     @Override
     public void logout(Long timeout, TimeUnit timeUnit) throws CommandTimeoutException {
         this.pw.println("logout");
         this.pw.flush();
         try {
             stopClient(timeout, timeUnit);
         } catch (TimeoutException e) {
             throw new CommandTimeoutException("logout", e);
         }
     }
 
     @Override
     protected OutputHandler getOutputHandler() {
         return outputHandler;
     }

     @Override
     protected PrintWriter getPrintWriter() {
         return pw;
     }
 }
