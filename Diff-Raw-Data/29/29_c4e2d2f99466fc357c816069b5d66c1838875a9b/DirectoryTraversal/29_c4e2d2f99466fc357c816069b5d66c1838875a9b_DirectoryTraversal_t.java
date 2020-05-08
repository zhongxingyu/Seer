 /* DirectoryTraversal.java */
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.io.PrintWriter;
 
 public class DirectoryTraversal {
 
   private int defaultNumThreads = 5;
   private int defaultExecutionTime = 1000;
 
   private ArrayList<File> files; // Needs to be synchronized
   private int numThreads;
   private long executionTime;
   private long startTime;
  private HashMap<String, Integer> counts; // Needs to be synchronized
   private MySQLDatabase mysql;
   private Runnable run;
  private int exited = 0; // Needs to be synchronized?

  // Locks
   private final static Object filesLock = new Object();
   private final static Object countsLock = new Object();
   private final static Object exitedLock = new Object();
 
   /**
    *  DirectoryTraversal() constructor.
    *  @param filepath the file to be traversed.
    *  @param numThreads the number of threads to be used.
    *  @param executionTime the execution time
    **/
   public DirectoryTraversal(String filepath, String numThreads, String executionTime) {
     files = new ArrayList<File>();
     files.add(new File(filepath));
     this.numThreads = Integer.parseInt(numThreads);
     if (this.numThreads <= 0) {
       System.out.println("Error: Invalid Number of Threads");
       System.out.println("Using " + defaultNumThreads + " Threads Instead");
       this.numThreads = defaultNumThreads;
     }
     this.executionTime = Long.parseLong(executionTime);
     if (this.executionTime <= 0) {
       System.out.println("Error: Invalid Program Execution Time");
       System.out.println("Using " + defaultExecutionTime + " ms Instead");
       this.executionTime = defaultExecutionTime;
     }
     counts = new HashMap<String, Integer>();
     setupDatabase();
     setupRunnable();
    startTime = System.currentTimeMillis();
   }
 
   private void setupDatabase() {
     mysql = new MySQLDatabase();
     mysql.connectToDatabase();
     mysql.setup();
   }
 
   private void setupRunnable() {
     run = new Runnable() {
       public void run() {
        //System.out.println("Thread " + Thread.currentThread().getId() + " Started and running");
         while ((System.currentTimeMillis() - startTime) < executionTime) {
           try {
             File file = null;
             synchronized(filesLock) {
               if (files.size() != 0) {
                 file = files.remove(0);
               }
             }
             if (file == null) {
             } else if (file.isDirectory()) {
               String[] filenames = file.list();
              synchronized(filesLock) {
                for (String filename : filenames) {
                  files.add(new File(file, filename));
                }
               }
             } else if (file.isFile()) {
               String[] path = file.toString().split("/");
               String key = path[path.length - 1];
               String sql = "INSERT INTO FILES values(now(), \"" + key + "\", \"" + path[1] + "\", " + Thread.currentThread().getId() + ")";
               mysql.addEntry(sql);
               synchronized(countsLock) {
                 if (counts.containsKey(key)) {
                   int count = counts.get(key);
                   count++;
                   counts.put(key, count);
                 } else {
                   counts.put(key, 1);
                 }
               }
             }
           } catch (Exception e) {
            System.out.println(e);
           }
         }
         synchronized(exitedLock) {
           exited++;
         }
         System.out.println("Thread " + Thread.currentThread().getId() + " Finished");
       }
     };
   }
 
   /**
    *  closeConnection() close the connection to the mysql database.
    **/
   public void closeConnection() {
     mysql.closeConnection();
   }
 
   /**
    *  printCounts() print to stdout the counts of all files in the format:
    *  filename - count.
    **/
   public void printCounts() {
     Object[] count = counts.keySet().toArray();
     for (Object c : count) {
       System.out.println(c + " - " + counts.get(c));
     }
   }
 
   /**
    *  writeCounts() write to a file output.txt the counts of all the files
    *  in the format: filename - count.
    **/
   public void writeCounts() {
     Object[] count = counts.keySet().toArray();
     try {
       PrintWriter output = new PrintWriter("counts.txt", "UTF-8");
       for (Object c : count) {
         output.println(c + " - " + counts.get(c));
       }
       output.flush();
       output.close();
       System.out.println("Successfully Wrote Filename Counts to output.txt");
     } catch (Exception e) {
       System.out.println(e);
     }
   }
 
   /**
    *  traverse() using multiple threads, traverse the directory structure and
    *  print the counts to stdout and write them to a file output.txt.
    **/
   public void traverse() {
     for (int i = 0; i < numThreads; i++) {
       Thread t = new Thread(run);
       System.out.println("Starting Thread " + t.getId());
       t.start();
     }
     while (exited != numThreads) {
       try {
         Thread.sleep(1000);
       } catch (InterruptedException e) {
         System.out.println(e);
       }
     }
     printCounts();
     writeCounts();
   }
 
   /**
    *  java DirectoryTraversal <path> <numThreads> <executionTime>
    **/
   public static void main(String[] args) {
     if (args.length != 3) {
       System.out.println("Usage: java DirectoryTraversal <path> <numThreads> <executionTime>");
       return;
     }
     System.out.println("Begin Traversing Directory: " + args[0] + " using " + args[1] + " threads");
     DirectoryTraversal d = new DirectoryTraversal(args[0], args[1], args[2]);
     d.traverse();
     d.closeConnection();
     System.out.println("Finished Traversing Directory: " + args[0]);
   }
 }
