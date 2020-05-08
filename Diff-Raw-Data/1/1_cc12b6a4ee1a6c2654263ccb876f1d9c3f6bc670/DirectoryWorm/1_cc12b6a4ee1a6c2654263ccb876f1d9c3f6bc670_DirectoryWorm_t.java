 package ru.sbertech;
 
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.*;
 import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
 
 /**
  * Реализация сканнера.
  * @author Dmitry Dobrynin
  *         Date: 10.11.11 time: 23:03
  */
 public class DirectoryWorm implements Scanner {
     private OutputStream output;
     private ParameterParser[] parsers;
     private List<File> roots = new LinkedList<File>();
     private CompositeFilter fileFilter = new CompositeFilter();
     private CompositeFilter directoryFilter = new CompositeFilter();
     // создать пул потоков по количеству процессоров/ядер
     private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
     private boolean started;
     private Set<Appender> workingAppenders = new CopyOnWriteArraySet<Appender>();
     private SortedSet<Appender> closedAppenders = new ConcurrentSkipListSet<Appender>();
     private Timer timer = new Timer(true);
 
     public DirectoryWorm(OutputStream stream, ParameterParser... parsers) {
         this.output = stream;
         this.parsers = parsers;
     }
 
     public DirectoryWorm(File output, ParameterParser... parsers) throws FileNotFoundException {
         this(new FileOutputStream(output), parsers);
     }
 
     public DirectoryWorm(String output, ParameterParser... parsers) throws FileNotFoundException {
         this(new File(output), parsers);
     }
 
     public DirectoryWorm configure(String[] parameters) {
         for (String parameter : parameters) if (!parseParameter(parameter))
             throw new IllegalArgumentException(format("Could not parse parameter: %s!", parameter));
         return this;
     }
 
     protected boolean parseParameter(String parameter) {
         for (ParameterParser parser : parsers) if (parser.parse(parameter, this)) return true;
         return false;
     }
 
     public void directory(File directory) {
         if (started) scanDirectory(directory); else roots.add(directory); // отложенный старт
     }
 
     protected void scanDirectory(File directory) {
         if (directoryFilter.accept(directory)) {
             Appender appender = new FileAppender(directory, fileFilter, this);
             workingAppenders.add(appender);
             executor.execute(new DirectoryBrowser(directory, this, appender));
         }
     }
 
     public void closed(Appender appender) {
         workingAppenders.remove(appender);
         closedAppenders.add(appender);
         if (workingAppenders.isEmpty()) finish();
     }
 
     public void directoryFilter(FileFilter filter) { directoryFilter.addFilter(filter); }
 
     public void fileFilter(FileFilter filter) { fileFilter.addFilter(filter); }
 
     public void scan() {
         started = true;
         for (File root : roots) scanDirectory(root);
         timer.schedule(new Indicator(), 0, 1000);
     }
 
     protected void finish() {
         for (Appender a : closedAppenders)
             try { a.dump(output); }
             catch (Exception e) {
                 System.out.println("Appender failed to dump its results");
                 e.printStackTrace();
             }
 
         System.out.println("\nScanning has been completed");
         executor.shutdown();
         timer.cancel();
         try { output.close(); } catch (IOException ignored) {}
     }
 
     private static class Indicator extends TimerTask {
         public void run() {
             if (currentTimeMillis() % 60000  == 0) System.out.print('|');
             else if (currentTimeMillis() % 6000 == 0) System.out.print('.');
         }
     }
 }
