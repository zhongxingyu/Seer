 package diskbiggest;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.NavigableSet;
 import java.util.TreeSet;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * User: Oleksiy Pylypenko
  * Date: 10/10/12
  * Time: 11:49 AM
  */
 public class DiskBiggest {
 
     static String sizeShortcut(long size, boolean progress) {
         if (progress) {
             if (size < 1024) {
                 return String.format("%dB", size);
             } else if (size < 1024L * 1024) {
                 return String.format("%.2fKiB", size / 1024.0);
             } else if (size < 1024L * 1024 * 1024) {
                 return String.format("%.4fMiB", size / (1024.0 * 1024.0));
             } else if (size < 1024L * 1024 * 1024 * 1024) {
                 return String.format("%.6fGiB", size / (1024.0 * 1024.0 * 1024.0));
             } else {
                 return String.format("%.8fTiB", size / (1024.0 * 1024.0 * 1024.0 * 1024.0));
             }
         } else {
             if (size < 1024) {
                 return String.format("%dB", size);
             } else if (size < 1024L * 1024) {
                 return String.format("%.3fKiB", size / 1024.0);
             } else if (size < 1024L * 1024 * 1024) {
                 return String.format("%.3fMiB", size / (1024.0 * 1024.0));
             } else if (size < 1024L * 1024 * 1024 * 1024) {
                 return String.format("%.3fGiB", size / (1024.0 * 1024.0 * 1024.0));
             } else {
                 return String.format("%.3fTiB", size / (1024.0 * 1024.0 * 1024.0 * 1024.0));
             }
         }
     }
 
     static class FilePath implements Comparable<FilePath> {
         private long size;
         private String filename;
 
         public FilePath(String filename, long size) {
             this.filename = filename;
             this.size = size;
         }
 
         public int compareTo(FilePath o) {
             return -Long.valueOf(size).compareTo(o.size);
         }
 
         @Override
         public String toString() {
             return DiskBiggest.sizeShortcut(size, false) + " " + filename;
         }
     }
 
     private NavigableSet<FilePath> set = new TreeSet<FilePath>();
     private final int limit;
 
     public DiskBiggest(int limit) {
         this.limit = limit;
     }
 
     class Progress implements Runnable {
         private AtomicLong totalSize = new AtomicLong();
         private AtomicLong totalDirs = new AtomicLong();
 
         private Thread thread;
 
         @Override
         public void run() {
             try {
                 while (true) {
                     report(false);
                     Thread.sleep(100);
                 }
             } catch (InterruptedException e) {
                 // quit
             }
         }
 
         private void report(boolean ln) {
             String msg = "Total scanned: " + sizeShortcut(totalSize.longValue(), true) +
                     " dirs: " + totalDirs.longValue() + "        ";
             if (ln) {
                 System.err.println(msg);
             } else {
                 System.err.print(msg + "\r");
             }
         }
 
         public void addBytes(long length) {
             totalSize.addAndGet(length);
         }
 
         public void addDir(long count) {
             totalDirs.addAndGet(count);
         }
 
         public void start() {
             thread = new Thread(this);
             thread.start();
         }
 
         public void stop() {
             thread.interrupt();
             try {
                 thread.join(500);
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
             }
             report(true);
         }
     }
 
     private Progress progress = new Progress();
 
     public long iterate(final File filename) {
         final String base = filename.getPath() + File.separator;
         class Recursion {
             long go(File currentFile) {
                 progress.addDir(1);
 
                 long size = 0;
                 File[] files = currentFile.listFiles();
                 if (files != null) {
                     for (File f : files) {
                         if (f.isFile()) {
                             progress.addBytes(f.length());
                             size += f.length();
                         } else if (f.isDirectory()) {
                             size += go(f);
                         }
                     }
                 }
 
                 String path = currentFile.getPath();
                 if (path.startsWith(base)) {
                     path = path.substring(base.length());
                 }
 
                 enqueue(new FilePath(path, size));
                 return size;
             }
         }
         return new Recursion().go(filename);
     }
 
     private synchronized void enqueue(FilePath filePath) {
         if (set.size() >= limit) {
             set.pollLast();
         }
         set.add(filePath);
     }
 
     public static void main(String[] args) {
         if (args.length == 0) {
             System.out.println("java -jar diskbiggest.jar PATH [N=30]");
             System.exit(1);
         }
 
         int n = 30;
         if (args.length >= 2) {
             n = Integer.parseInt(args[1]);
         }
         File root = new File(args[0]);
         try {
             root = root.getCanonicalFile();
         } catch (IOException e) {
             root = root.getAbsoluteFile();
         }
        System.out.println("Top " + n + " file and directories in " + root);
         DiskBiggest diskBiggest = new DiskBiggest(n);
         diskBiggest.outputOnShutdown();
         diskBiggest.progress.start();
         diskBiggest.iterate(root);
         diskBiggest.progress.stop();
         // output is done in shutdown hook
     }
 
     private void outputOnShutdown() {
         Thread hook = new Thread(new Runnable() {
             @Override
             public void run() {
                 System.err.println();
                 output();
             }
         });
         Runtime.getRuntime().addShutdownHook(hook);
     }
 
     public synchronized void output() {
         for (FilePath path : set) {
             System.out.println(path);
         }
     }
 }
