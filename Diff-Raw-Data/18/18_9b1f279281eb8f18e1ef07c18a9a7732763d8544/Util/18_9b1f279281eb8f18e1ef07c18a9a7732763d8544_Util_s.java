 package cmu.ds.mr.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 public class Util {
 
   public static final long TIME_INTERVAL_MONITOR = 1000;
 
   public static final long TIME_INTERVAL_HEARTBEAT = 1000;
 
   public static final String BLOCK_SIZE = "fs.block.size";
 
   public static final String LOCAL_ROOT_DIR = "fs.local.root.dir";
 
   public static final String NUM_MAP_TASK = "mapred.map.tasks";
 
   public static final String NUM_RED_TASK = "mapred.red.tasks";
 
   public static final String JOB_NAME = "mapred.job.name";
 
   public static final String NUM_TASK_MAX = "mapred.tasktracker.tasks.maximum";
 
   public static final String SERVICE_NAME = "JobSubmissionProtocol";
 
   public static final String SERVICE_NAME_INTERTRACKER = "InterTrackerProtocol";
 
   public static final String CONFIG_PATH = "./conf/mapred.conf";
 
   public static final int MAX_TRY = 3;
 
   public static String stringifyException(Throwable e) {
     StringWriter stm = new StringWriter();
     PrintWriter wrt = new PrintWriter(stm);
     e.printStackTrace(wrt);
     wrt.close();
     return stm.toString();
   }
 
   public static Object newInstance(Class<?> theClass) throws RuntimeException,
           InstantiationException, IllegalAccessException, InvocationTargetException,
           NoSuchMethodException {
     Constructor<?> constructor = theClass.getConstructor();
     return constructor.newInstance();
   }
 
   /**
    * rm -r
    * 
    * Adopted from http://www.mkyong.com/java/how-to-delete-directory-in-java/
    * */
   public static void delete(File file) throws IOException {
 
     if (file.isDirectory()) {
 
       // directory is empty, then delete it
       if (file.list().length == 0) {
 
         file.delete();
         System.out.println("Directory is deleted : " + file.getAbsolutePath());
 
       } else {
 
         // list all the directory contents
         String files[] = file.list();
 
         for (String temp : files) {
           // construct the file structure
           File fileDelete = new File(file, temp);
 
           // recursive delete
           delete(fileDelete);
         }
 
         // check the directory again, if empty then delete it
         if (file.list().length == 0) {
           file.delete();
           System.out.println("Directory is deleted : " + file.getAbsolutePath());
         }
       }
 
     } else {
       // if file, then delete it
       file.delete();
       System.out.println("File is deleted : " + file.getAbsolutePath());
     }
   }
 
 }
