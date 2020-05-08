 package org.loadtest;
 
 import groovy.lang.Binding;
 import groovy.lang.GroovyCodeSource;
 import groovy.lang.GroovyRuntimeException;
 import groovy.lang.GroovyShell;
 import org.codehaus.groovy.control.CompilationFailedException;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Random;
 
 /**
  * Runs scripts in environment produced by "Classes.groovy" and three bindings:
  *  GLOBAL - global variables interchanged
  *  STATS - statistics object,
  *  RANDOM - random object
  */
 @SuppressWarnings({"WeakerAccess"})
 public class ScriptRunner implements Runnable {
     private final LoadTestTool loadTool;
     private final Stats stats = new Stats();
    private Object globals;
     private final Random random = new Random();
     private final static ThreadLocal<GroovyShell> LOCAL_SHELL = new ThreadLocal<GroovyShell>();
 
     public ScriptRunner(LoadTestTool loadTool) {
         this.loadTool = loadTool;
     }
 
     public void init() {
         run(new GroovyShellRunner() {
             public void run(GroovyShell shell) throws IOException {
                 for (String script : loadTool.getOptions().getInitScriptTexts()) {
                     shell.evaluate(script);
                 }
 
                 for (File script : loadTool.getOptions().getInitScripts()) {
                     shell.evaluate(script);
                 }
             }});
     }
 
     public void run() {
         run(new GroovyShellRunner() {
             public void run(GroovyShell shell) throws IOException {
                 for (String script : loadTool.getOptions().getScriptTexts()) {
                     shell.evaluate(script);
                 }
 
                 for (File script : loadTool.getOptions().getScripts()) {
                     shell.evaluate(script);
                 }
             }});
     }
 
     public void run(GroovyShellRunner runner) {
         synchronized (stats) {
             if (loadTool.getOptions().getRequests() != -1
                     && stats.getRuns() >= loadTool.getOptions().getRequests()) {
                 stats.report(Integer.MAX_VALUE);
                 System.out.println("Finished (press q[ENTER] - to quit, [ENTER][ENTER] - to restart)");
                 loadTool.stop();
                 return;
             }
             stats.addRun();
         }
         Binding binding = new Binding();
         binding.setVariable("STATS", stats);
 
         GroovyShell shell = new GroovyShell(binding);
         try {
             LOCAL_SHELL.set(shell);
 
             shell.evaluate(new GroovyCodeSource(ScriptRunner.class.getResource("Classes.groovy")));
 
            synchronized (this) {
                if (globals == null) {
                    globals = shell.evaluate("return new Globals()");
                }
            }
            binding.setProperty("GLOBALS", globals);

             runner.run(shell);
 
         } catch (CompilationFailedException e) {
             stats.addError(e);
             errorCase(e);
         } catch(GroovyRuntimeException e) {
             stats.addError(e);
             errorCase(e);
         } catch(Throwable e) {
             stats.addError(e);
         } finally {
             LOCAL_SHELL.remove();
         }
     }
 
     private interface GroovyShellRunner {
         void run(GroovyShell shell) throws IOException;
     }
 
     private void errorCase(Exception e) {
         if (loadTool.isStarted()) {
             e.printStackTrace();
             loadTool.stop();
         }
     }
 
     public Stats getStats() {
         return stats;
     }
 
     public static GroovyShell getGroovyShell() {
         return LOCAL_SHELL.get();
     }
 }
