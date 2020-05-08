 package org.jenkinsci.plugins.remote_terminal_access.ssh;
 
 import hudson.AbortException;
 import hudson.util.StreamTaskListener;
 import org.apache.sshd.common.PtyMode;
 import org.apache.sshd.server.Environment;
 import org.apache.sshd.server.Signal;
 import org.apache.sshd.server.SignalListener;
 import org.jenkinsci.main.modules.sshd.AsynchronousCommand;
 import org.jenkinsci.main.modules.sshd.SshCommandFactory.CommandLine;
 import org.jenkinsci.plugins.remote_terminal_access.ProcessWithPtyLauncher;
 import org.kohsuke.ajaxterm.ProcessWithPty;
 import org.kohsuke.args4j.CmdLineException;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.reflect.Field;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 public abstract class AbstractRemoteSshCommand extends AsynchronousCommand {
     protected ProcessWithPty proc;
 
     public AbstractRemoteSshCommand(CommandLine cmdLine) {
         super(cmdLine);
     }
 
     /**
      * Pass along the signal from the client to the child process.
      */
     @Override
     public void start(final Environment env) throws IOException {
         env.addSignalListener(new SignalListener() {
             public void signal(Signal signal) {
                 try {
                     if (signal==Signal.WINCH) {
                         int w = Integer.parseInt(env.getEnv().get(Environment.ENV_COLUMNS));
                         int h = Integer.parseInt(env.getEnv().get(Environment.ENV_LINES));
                         proc.setWindowSize(w,h);
                     } else
                         proc.kill(signal.getNumeric());
                 } catch (IOException e) {
                     LOGGER.log(Level.WARNING, "Failed to send signal to " + proc, e);
                 }
             }
         });
         super.start(env);
     }
 
     /**
      * When the terminal is allocated, I discovered that the linux SSH client (at least)
      * sends the terminal mode ONLCR to 1, and expects us to send back CR+LF instead of just NL.
      *
      * If I understand pty correctly, I should be able to send a command to set this off,
      * but I can't find such a command in terminfo, so I'm just working around by sending CR+LF.
      *
      * This unfortunately requires reflection as PrintStream doesn't make it modifiable.
      */
     protected PrintStream getErrorPrintStream() {
         PrintStream ps = new PrintStream(getErrorStream());
         try {
             Field $textOut = ps.getClass().getDeclaredField("textOut");
             $textOut.setAccessible(true);
             BufferedWriter bw = (BufferedWriter)$textOut.get(ps);
             Field $lineSeparator = BufferedWriter.class.getDeclaredField("lineSeparator");
             $lineSeparator.setAccessible(true);
 
             String lineSeparator = null;
             Integer onlcr = getEnvironment().getPtyModes().get(PtyMode.ONLCR);
             if (onlcr!=null && onlcr==1)
                 lineSeparator = "\r\n";
             // other output mode??
             if (lineSeparator!=null)
                 $lineSeparator.set(bw,lineSeparator);
 
             return ps;
         } catch (NoSuchFieldException e) {
             // if this ugly reflection fails, proceed anyway
             return ps;
         } catch (IllegalAccessException e) {
             return ps;
         }
     }
 
     protected int die(String msg) {
         PrintStream s = getErrorPrintStream();
         s.println(msg);
         s.flush();
         return 1;
     }
 
     /**
      * Runs the specified commands and
      * pumps the stdin/stdout/stderr between {@link #proc} and the SSH client.
      *
      * @return
      *      When the child process exits, this method returns with its exit code.
      */
     protected int run(List<String> args) throws IOException, InterruptedException {
         Environment env = getEnvironment();
         String term = env.getEnv().get(Environment.ENV_TERM);
         String w = env.getEnv().get(Environment.ENV_COLUMNS);
         String h = env.getEnv().get(Environment.ENV_LINES);
         //            if (w == null || h == null || term == null)
         //                return die("No tty. Please run ssh with the -t option");
 
         // probably pty was requested. this is a somewhat weaker way of testing this
         // TODO: patch mina to remember handlePty call
 
         ProcessWithPtyLauncher pb = new ProcessWithPtyLauncher();
         if (args.isEmpty())
             pb.shell();
         else
             pb.commands(args);
         pb.envs(env.getEnv());
         configure(pb);
         proc = pb.launch(term);
 
         if (w!=null && h!=null)
             proc.setWindowSize(Integer.parseInt(w),Integer.parseInt(h));
 
        FlushStreamCopyThread t1 = new FlushStreamCopyThread(getCmdLine() + " stdout pump", proc.getInputStream(), getOutputStream(), true);
         FlushStreamCopyThread t2 = new FlushStreamCopyThread(getCmdLine() + " stdin pump", getInputStream(), proc.getOutputStream(), true);
         t1.start();
         t2.start();
 
         // stderr doesn't exist if there's pty
         FlushStreamCopyThread t3=null;
         if (term==null) {
             t3 = new FlushStreamCopyThread(getCmdLine() + " stderr pump", proc.getErrorStream(), getErrorStream(), false); // we might need stderr to send our own error
             t3.start();
         }
 
         try {
             int exit = proc.waitFor();
             proc = null;
             return exit;
         } finally {
             try {// on abnormal termination, kill the process
                 if (proc!=null) {
                     proc.kill(9);
                     proc.destroy();
                 }
                 proc = null;
             } catch (IOException e) {
                 LOGGER.log(Level.WARNING, "Failed to send signal to " + proc, e);
             }
             t1.join();
             if (t3!=null)   t3.join();
 //                t2.join(); - client normally doesn't close stdin, so we don't wait for that
         }
     }
 
     /**
      * Configures how/where the process is launched.
      */
     protected abstract void configure(ProcessWithPtyLauncher pb) throws IOException, InterruptedException;
 
     private static final Logger LOGGER = Logger.getLogger(AbstractRemoteSshCommand.class.getName());
 
     @Override
     protected int run() {
         try {
             List<String> args = getCmdLine().subList(1, getCmdLine().size());
             return main(args);
         } catch (AbortException e) {
             return die(e.getMessage());
         } catch (CmdLineException e) {
             return die(e.getMessage());
         } catch (Exception e) {// this catch block becomes redundant with sshd-module 1.5
             PrintStream out = getErrorPrintStream();
             e.printStackTrace(out);
             out.flush();
             return 1;
         }
     }
 
     protected abstract int main(List<String> args) throws IOException, InterruptedException, CmdLineException;
 }
