 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Iterables;
 
 import uk.ac.ebi.fgpt.sampletab.utils.FileRecursiveIterable;
 import uk.ac.ebi.fgpt.sampletab.utils.FileGlobIterable;
 
 public abstract class AbstractInfileDriver<T extends Runnable> extends AbstractDriver {
 
     @Argument(required=true, index=0, metaVar="INPUT", usage = "input filenames or globs")
     protected List<String> inputFilenames;
 
     @Option(name = "--threads", aliases = { "-t" }, usage = "number of additional threads")
     protected int threads = 0;
        
     @Option(name = "--recursive", aliases = { "-r" }, usage="recusively match filename?")
     protected boolean recursive = false;
     
     @Option(name = "--startpath", aliases = { "-s" }, usage="starting path for matching")
     protected List<File> startpaths = null;
     
     private Logger log = LoggerFactory.getLogger(getClass());
         
     protected abstract T getNewTask(File inputFile);
     
     protected void preProcess() {
         //do nothing
         //override in subclass
     }
     
     protected void postProcess() {
         //do nothing
         //override in subclass
     }
     
     
     protected void doMain(String[] args) {
         super.doMain(args);
         
 
         Iterable<File> inputFiles = null;
         for (String inputFilename : inputFilenames) {
             if (recursive){
                 if (startpaths == null) {
                     log.info("Looking recursively for input files named "+inputFilename);
                     if (inputFiles == null) {
                         inputFiles = new FileRecursiveIterable(inputFilename, null);
                     } else {
                         inputFiles = Iterables.concat(inputFiles, new FileRecursiveIterable(inputFilename, null));
                     }
                     
                 } else {
                     for (File startpath : startpaths) {
                         log.info("Looking recursively for input files named "+inputFilename+" from "+startpath);
                         if (inputFiles == null) {
                             inputFiles = new FileRecursiveIterable(inputFilename, startpath);
                         } else {
                             inputFiles = Iterables.concat(inputFiles, new FileRecursiveIterable(inputFilename, startpath));
                         }
                     }
                 }
             } else {
                 log.info("Looking for input files in glob "+inputFilename);   
                 if (inputFiles == null) {
                     inputFiles = new FileGlobIterable(inputFilename);
                 } else {
                     inputFiles = Iterables.concat(inputFiles,new FileGlobIterable(inputFilename));
                 }
             }
         }
 
        ExecutorService pool = null;
        if (threads > 0) {
            pool = Executors.newFixedThreadPool(threads);
        }
 
         preProcess();
         
         for (File inputFile : inputFiles) {
             Runnable t = getNewTask(inputFile);
            if (pool != null) {
                 pool.execute(t);
             } else {
                 t.run();
             }
         }
         
         // run the pool and then close it afterwards
         // must synchronize on the pool object
        if (pool != null) {
            synchronized (pool) {
                pool.shutdown();
                try {
                    // allow 24h to execute. Rather too much, but meh
                    pool.awaitTermination(1, TimeUnit.DAYS);
                } catch (InterruptedException e) {
                    log.error("Interupted awaiting thread pool termination", e);
                }
             }
         }
         
         postProcess();
         
         log.info("Finished reading");
     }
     
 }
