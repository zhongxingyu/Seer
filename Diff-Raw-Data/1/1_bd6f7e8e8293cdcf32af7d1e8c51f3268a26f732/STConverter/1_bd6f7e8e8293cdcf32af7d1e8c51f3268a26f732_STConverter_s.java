 package uk.ac.ebi.sampletab;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.apache.commons.io.FileUtils;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 
 public class STConverter {
     static final String SAMPLETAB = "sampletab.toload.txt";
     
     static int exitcode = 0;
 
     static class InputFiles {
         File dir;
         File sampletab;
     }
 
     static final String usage = "java -jar STConverter.jar <input file/dir> [ ... <input file/dir> ]";
 
     static private BlockingQueue<InputFiles> infiles;
     static private STConverterOptions options;
 
     static Log stdout = new PrintStreamLog(System.out, false);
 
     public static void main(String[] args) {
         options = new STConverterOptions();
         CmdLineParser parser = new CmdLineParser(options);
 
         try {
             parser.parseArgument(args);
         } catch (CmdLineException e) {
             System.err.println(e.getMessage());
             System.err.println(usage);
             parser.printUsage(System.err);
             System.exit(1);
             return;
         }
 
         if (options.getDirs() == null || options.getDirs().size() == 0) {
             System.err.println(usage);
             parser.printUsage(System.err);
             System.exit(1);
             return;
         }
 
         infiles = new LinkedBlockingQueue<InputFiles>();
 
         // Set<String> processedDirs = new HashSet<String>();
         //
         // for(String outf : options.getDirs())
         // {
         // File in = new File(outf);
         //
         // if( ! in.exists() )
         // {
         // System.err.println("Input directory '" + outf + "' doesn't exist");
         // System.exit(1);
         // }
         // else if( ! in.isDirectory() )
         // {
         // System.err.println("'" + outf + "' is not a directory");
         // System.exit(1);
         // }
         //
         // collectInput( in, infiles, processedDirs, 0 ) ;
         //
         // }
         //
         // if(infiles.size() == 0)
         // {
         // System.err.println("No files to process");
         // return;
         // }
 
         File outDir = null;
 
         if (options.getOutputDir() != null) {
             if (options.getDirs().size() != 1) {
                 System.err.println("Only one input directory is allowed if output directory specified");
                 System.exit(1);
                 return;
             }
 
             outDir = new File(options.getOutputDir());
 
             if (outDir.isFile()) {
                 System.err.println("Output path should point to some directory");
                 System.exit(1);
                 return;
             }
 
             if (!outDir.exists() && !outDir.mkdirs()) {
                 System.err.println("Can't create output direcory");
                 System.exit(1);
                 return;
             }
         }
         // else
         // outDir = new File( options.getDirs().get(0) );
 
         // if(options.getOutDir() == null)
         // {
         // System.err.println("Output directory is not specified");
         // return;
         // }
 
         // final File outDir = new File(options.getOutDir());
 
         // if(outDir.isFile())
         // {
         // System.err.println("Output path should point to a directory");
         // return;
         // }
         //
         // if(!outDir.exists() && !outDir.mkdirs())
         // {
         // System.err.println("Can't create output directory");
         // return;
         // }
 
         final Log log;
         final Log failedLog;
 
         try {
             if (options.getLogFileName() != null)
                 log = new PrintStreamLog(new PrintStream(new File(options.getLogFileName())), true);
             else
                 log = new PrintStreamLog(System.err, true);
         } catch (IOException e1) {
             System.err.println("Can't create log file: " + options.getLogFileName());
             System.exit(1);
             return;
         }
 
         try {
             if (options.getFailedFileName() != null)
                 failedLog = new PrintStreamLog(new PrintStream(new File(options.getFailedFileName())), false);
             else
                 failedLog = new NullLog();
         } catch (IOException e1) {
             System.err.println("Can't create failed log file: " + options.getFailedFileName());
             System.exit(1);
             return;
         }
 
         if (!options.isRecursive()) {
             List<String> subdirs = options.getDirs();
             Collections.sort(subdirs);
             for (String outf : subdirs) {
                 File in = new File(outf);
 
                 if (!in.exists()) {
                     System.err.println("Input file/directory '" + outf + "' doesn't exist");
                 } else if (in.isDirectory()) {
                     System.err.println("'" + outf + "' is a directory");
                 }
 
                 InputFiles inp = new InputFiles();
 
                 inp.sampletab = in;
                 inp.dir = in.getParentFile();
 
                 infiles.add(inp);
             }
 
             if (infiles.size() == 0) {
                 System.err.println("No files to process");
                 System.exit(1);
                 return;
             }
 
             infiles.add(new InputFiles());
 
             new ConverterTask(outDir != null ? new File(options.getDirs().get(0)) : null, outDir, "Main", log,
                     failedLog).run();
            return;
         } else {
             int nTheads = Runtime.getRuntime().availableProcessors();
 
             log.write("Starting " + nTheads + " threads");
 
             ExecutorService exec = Executors.newFixedThreadPool(nTheads + 1);
 
             exec.execute(new InputCollectionTask());
 
             File bDir = outDir != null ? new File(options.getDirs().get(0)) : null;
 
             for (int i = 1; i <= nTheads; i++)
                 exec.execute(new ConverterTask(bDir, outDir, "Thr" + i, log, failedLog));
 
             try {
                 exec.shutdown();
 
                 exec.awaitTermination(72, TimeUnit.HOURS);
             } catch (InterruptedException e) {
             }
         }
 
         log.shutdown();
         failedLog.shutdown();
         System.exit(exitcode);
     }
 
     static class InputCollectionTask implements Runnable {
 
         @Override
         public void run() {
             Set<String> processedDirs = new HashSet<String>();
 
             for (String outf : options.getDirs()) {
                 File in = new File(outf);
 
                 if (!in.exists()) {
                     System.err.println("Input directory '" + outf + "' doesn't exist. Skipping");
                     continue;
                 } else if (!in.isDirectory()) {
                     System.err.println("'" + outf + "' is not a directory. Skipping");
                     continue;
                 }
 
                 collectInput(in, infiles, processedDirs, 0);
 
             }
 
             while (true) {
                 try {
                     infiles.put(new InputFiles());
                     return;
                 } catch (InterruptedException e) {
                 }
             }
 
         }
 
     }
 
     static class ConverterTask implements Runnable {
         private String threadName;
 
         private Log log;
         private Log failedLog;
 
         File baseDir;
         File outDir;
 
         private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
 
         public ConverterTask(File bDir, File oDir, String threadName, Log log, Log failedLog) {
             super();
             this.threadName = threadName;
             this.log = log;
             this.failedLog = failedLog;
 
             baseDir = bDir;
             outDir = oDir;
         }
 
         @Override
         public void run() {
             Thread.currentThread().setName(threadName);
 
             InputFiles f;
             while (true) {
                 try {
                     f = infiles.take();
                 } catch (InterruptedException e1) {
                     continue;
                 }
 
                 if (f.dir == null) {
                     while (true) {
                         try {
                             infiles.put(f);
                             return;
                         } catch (InterruptedException e) {
                         }
                     }
 
                 }
 
                 String dirName = " (" + f.dir.getName() + ")";
 
                 try {
 
                     Submission s = null;
 
                     long time = System.currentTimeMillis();
 
                     File subOutDir = null;
 
                     if (options.isRecursive()) {
 
                         if (outDir != null) {
                             String relPath = f.dir.getAbsolutePath().substring(baseDir.getAbsolutePath().length());
 
                             if (relPath.startsWith("/"))
                                 relPath = relPath.substring(1);
 
                             subOutDir = new File(outDir, relPath + "/age");
 
                         } else
                             subOutDir = new File(f.dir, "age");
                     } else {
                         if (outDir != null)
                             subOutDir = outDir;
                         else
                             subOutDir = f.dir;
                     }
 
                     if (!subOutDir.isDirectory() && !subOutDir.mkdirs()) {
                         log.write("ERROR: Can't create output directory: " + subOutDir.getAbsolutePath() + dirName);
                         exitcode = 1;
                         return;
                     }
 
                     File ageFile = new File(subOutDir, f.dir.getName() + ".age.txt");
 
                     if (options.isUpdate() && ageFile.exists() && ageFile.lastModified() >= f.sampletab.lastModified()) {
                         log.write("File '" + ageFile + "' is up-to-date" + dirName);
                         continue;
                     }
 
                     // System.out.println("Parsing file: " + f);
                     log.write("Parsing file: " + f.sampletab.getAbsolutePath());
 
                     try {
                         s = STParser4.readST(f.sampletab);
                     } catch (Exception e) {
                         failedLog.write(f.dir.getAbsolutePath());
 
                         log.write("ERROR: File parsing error: " + e.getMessage() + dirName);
                         log.printStackTrace(e);
                         exitcode = 1;
                         continue;
                     }
 
                     String sbmId = s.getAnnotation(Definitions.SUBMISSIONIDENTIFIER).getValue();
 
                     if (sbmId == null) {
                         log.write("ERROR: Can't retrieve submission identifier" + dirName);
                         failedLog.write(f.dir.getAbsolutePath());
                         exitcode = 1;
                         continue;
                     }
 
                     log.write("Parsing success. " + (System.currentTimeMillis() - time) + "ms" + dirName);
 
                     log.write("Converting to AGE-TAB" + dirName);
                     time = System.currentTimeMillis();
 
                     OutputStream fos = new BufferedOutputStream(new FileOutputStream(ageFile));
 
                     ATWriter.writeAgeTab(s, fos);
 
                     fos.close();
 
                     log.write("Converting success. " + (System.currentTimeMillis() - time) + "ms" + dirName);
 
                     FileUtils.copyFile(f.sampletab, new File(subOutDir, "source.sampletab.txt"));
 
                     // FileUtil.linkOrCopyFile(f.sampletab, new File(subOutDir, "source.sampletab.txt"));
 
                     // PrintWriter stOut = new PrintWriter(new File(subOutDir, "source.sampletab.txt"), "UTF-8");
                     // stOut.write(stContent);
                     // stOut.close();
 
                     File idFile = new File(subOutDir, ".id");
                     PrintWriter metaOut = new PrintWriter(idFile, "UTF-8");
                     metaOut.print(sbmId);
                     metaOut.close();
 
                     idFile = new File(subOutDir, ".id." + ageFile.getName());
                     metaOut = new PrintWriter(idFile, "UTF-8");
                     metaOut.print(sbmId + ":module1");
                     metaOut.close();
 
                     String descr = s.getAnnotation(Definitions.SUBMISSIONDESCRIPTION).getValue();
 
                     if (descr == null)
                         descr = "Submission " + sbmId;
 
                     File descFile = new File(subOutDir, ".description");
                     metaOut = new PrintWriter(descFile, "UTF-8");
                     metaOut.print(descr);
                     metaOut.close();
 
                     File modDescFile = new File(subOutDir, ".description." + ageFile.getName());
                     metaOut = new PrintWriter(modDescFile, "UTF-8");
                     metaOut.print("Data module for submisson '" + sbmId + "'. Converted from Sample-Tab at "
                             + dateFormat.format(new Date()));
                     metaOut.close();
 
                     modDescFile = new File(subOutDir, ".description." + f.sampletab.getName());
                     metaOut = new PrintWriter(modDescFile, "UTF-8");
                     metaOut.print("Sample-Tab file'. Last mofified at "
                             + dateFormat.format(new Date(f.sampletab.lastModified())));
                     metaOut.close();
 
                     stdout.write(f.dir.getAbsolutePath());
                 } catch (IOException e) {
                     log.write("ERROR: IOException. " + e.getMessage() + dirName);
                     failedLog.write(f.sampletab.getAbsolutePath());
                     exitcode = 1;
                     log.printStackTrace(e);
                 } catch (Exception e) {
                     log.write("ERROR: Unknown Exception. " + e.getClass().getName() + " " + e.getMessage() + dirName);
                     failedLog.write(f.sampletab.getAbsolutePath());
                     exitcode = 1;
                     log.printStackTrace(e);
                 }
             }
         }
     }
 
     static long collectInput(File in, BlockingQueue<InputFiles> infiles, Set<String> processedDirs, long prcssd) {
         File[] files = in.listFiles();
 
         for (File f : files) {
 
             if (f.isDirectory() && !processedDirs.contains(f.getAbsolutePath()) && options.isRecursive()) {
                 // System.out.println(f.getAbsolutePath());
                 prcssd++;
 
                 prcssd = collectInput(f, infiles, processedDirs, prcssd);
                 processedDirs.add(f.getAbsolutePath());
 
                 if (prcssd % 1000 == 0)
                     System.out.println(String.valueOf(prcssd) + " directories scanned");
             } else if (options.getStFileName().equals(f.getName()) && f.isFile()) {
                 InputFiles ifls = new InputFiles();
 
                 ifls.dir = in;
                 ifls.sampletab = f;
 
                 while (true) {
                     try {
                         infiles.put(ifls);
                         break;
                     } catch (InterruptedException e) {
                     }
                 }
 
                 // System.out.println( in.getAbsolutePath()+" selected");
             }
 
         }
 
         return prcssd;
     }
 
     static class NullLog implements Log {
         @Override
         public void shutdown() {
         }
 
         @Override
         public void write(String msg) {
         }
 
         @Override
         public void printStackTrace(Exception e) {
         }
     }
 
     static class PrintStreamLog implements Log {
         private PrintStream log;
         private Lock lock = new ReentrantLock();
 
         private boolean showThreads;
 
         PrintStreamLog(PrintStream l, boolean th) {
             log = l;
             showThreads = th;
         }
 
         public void shutdown() {
             log.close();
         }
 
         public void write(String msg) {
             lock.lock();
 
             try {
                 if (showThreads)
                     log.print("[" + Thread.currentThread().getName() + "] ");
 
                 log.println(msg);
             } finally {
                 lock.unlock();
             }
         }
 
         @Override
         public void printStackTrace(Exception e) {
             lock.lock();
 
             try {
                 e.printStackTrace(log);
             } finally {
                 lock.unlock();
             }
         }
 
     }
 
 }
