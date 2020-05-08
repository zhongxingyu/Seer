 package org.loadtest;
 
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Tool to run test load specified by script using several threads.
  * Main entry point.
  */
@SuppressWarnings({"WeakerAccess"})
 public class LoadTestTool {
     private final Options options;
     private ScheduledExecutorService executor;
     private ScheduledExecutorService printExecutor;
     private ScriptRunner scriptRunner;
 
     public LoadTestTool(Options options) {
         this.options = options;
     }
 
     public synchronized void start(int nThreads) {
         if (executor != null) {
             return;
         }
         scriptRunner = new ScriptRunner(this);
         scriptRunner.init();
         executor = Executors.newScheduledThreadPool(nThreads);
         for (int i = 0; i < nThreads; i++) {
             long initDelay = (long)(1000L * options.delay * i / nThreads);
             long delay = (long)(1000L * options.delay);
             if (initDelay <= 0) initDelay = 0;
             if (delay <= 0) delay = 1;
             executor.scheduleWithFixedDelay(scriptRunner,
                     initDelay,
                     delay,
                     TimeUnit.MILLISECONDS);
         }
 
         printExecutor = Executors.newSingleThreadScheduledExecutor();
         printExecutor.scheduleWithFixedDelay(new StatsPrinter(),
                 500L,
                 1000L,
                 TimeUnit.MILLISECONDS);
     }
 
     public synchronized void stop() {
         if (executor == null) {
             return;
         }
         executor.shutdownNow();
         executor = null;
         printExecutor.shutdownNow();
         printExecutor = null;
         scriptRunner = null;
     }
 
     public boolean isStarted() {
         return executor != null;
     }
 
     public static void main(String[] args) throws IOException {
         LoadTestTool loadtest = new LoadTestTool(new Options().parse(args));
         Scanner scanner = new Scanner(System.in);
         int nThreads = loadtest.getOptions().getConcurrentThreads();
         try {
             Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
             do {
                 System.out.println("Starting with " + nThreads + " threads (press q[ENTER] - to quit, [ENTER] - to stop)");
                 loadtest.start(nThreads);
                 if (scanner.nextLine().equals("q")) {
                     break;
                 }
                 System.out.println("Stopping (press q[ENTER] - to quit, [ENTER] - to start)");
                 loadtest.stop();
 
                 int increment = loadtest.getOptions().getConcurrentThreadsIncrement();
                 if (increment > 0) {
                     nThreads += increment;
                 }
             } while (!scanner.nextLine().equals("q"));
         } catch (Throwable thr) {
             System.out.println("Error happened: " + thr);
         }
         System.exit(0);
     }
 
     public synchronized ScriptRunner getScriptRunner() {
         return scriptRunner;
     }
 
     public Options getOptions() {
         return options;
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public static class Options {
         @Option(name="-n", usage="Number of runs to perform", aliases = "--runs-number")
         private int requests = -1;
 
 
         @Option(name="-c", usage="Number of concurrent threads", aliases = "--threads")
         private int concurrentThreads = 1;
 
         @Option(name="-ci", usage="Number of concurrent threads to increment after stop/start (use [ENTER])", aliases = "--threads-increment")
         private int concurrentThreadsIncrement = 0;
 
         @Option(name="-h", usage="Display usage information", aliases="--help")
         private boolean displayUsage = false;
 
         @Option(name="-d", usage="Seconds to delay between requests", aliases="--delay")
         private double delay = 0;
 
         @Option(name="-s", usage="Number of the slowest queries to show in statistics(by default 24)", aliases="--show-slows")
         private int slowQueriesToShow = 24;
 
         @Option(name="-e", usage="Evaluate groovy script", aliases="--eval")
         private final List<String> scriptTexts = new ArrayList<String>();
 
         @Option(name="-ie", usage="Evaluate init script to initialize globals", aliases="--init-eval")
         private final List<String> initScriptTexts = new ArrayList<String>();
 
         @Option(name="-if", usage="Runs init script to initialize globals", aliases="--init-file")
         private final List<File> initScripts = new ArrayList<File>();
 
         @Argument
         private final List<File> scripts = new ArrayList<File>();
 
         public Options() {
         }
 
         public Options parse(String[] args) {
             CmdLineParser parser = new CmdLineParser(this);
             try {
                 parser.parseArgument(args);
                 if ((scripts.isEmpty() && scriptTexts.isEmpty()) || displayUsage) {
                     usage(parser, new CmdLineException(parser, "specify one or more scripts"));
                 }
                 if (concurrentThreads <= 1) {
                     concurrentThreads = 1;
                 }
             } catch (CmdLineException e) {
                 usage(parser, e);
             }
             return this;
         }
 
         private void usage(CmdLineParser parser, CmdLineException e) {
             System.err.println(e.getMessage());
             System.err.println("java -jar loadtest.jar [options...] scripts...");
             parser.printUsage(System.err);
             System.exit(1);
         }
 
         public int getRequests() {
             return requests;
         }
 
         public void setRequests(int requests) {
             this.requests = requests;
         }
 
         public int getConcurrentThreads() {
             return concurrentThreads;
         }
 
         public void setConcurrentThreads(int concurrentThreads) {
             this.concurrentThreads = concurrentThreads;
         }
 
         public int getConcurrentThreadsIncrement() {
             return concurrentThreadsIncrement;
         }
 
         public void setConcurrentThreadsIncrement(int concurrentThreadsIncrement) {
             this.concurrentThreadsIncrement = concurrentThreadsIncrement;
         }
 
         public boolean isDisplayUsage() {
             return displayUsage;
         }
 
         public void setDisplayUsage(boolean displayUsage) {
             this.displayUsage = displayUsage;
         }
 
         public double getDelay() {
             return delay;
         }
 
         public void setDelay(double delay) {
             this.delay = delay;
         }
 
         public int getSlowQueriesToShow() {
             return slowQueriesToShow;
         }
 
         public void setSlowQueriesToShow(int slowQueriesToShow) {
             this.slowQueriesToShow = slowQueriesToShow;
         }
 
         public List<String> getScriptTexts() {
             return scriptTexts;
         }
 
         public List<File> getScripts() {
             return scripts;
         }
 
         public List<String> getInitScriptTexts() {
             return initScriptTexts;
         }
 
         public List<File> getInitScripts() {
             return initScripts;
         }
     }
 
     private class StatsPrinter implements Runnable {
         public void run() {
             ScriptRunner runner = getScriptRunner();
             if (runner != null) {
                 runner.getStats().report(options.slowQueriesToShow);
             }
         }
     }
 
 }
