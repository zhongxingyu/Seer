 package in.uglyhunk.jvm.mon;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.FileWriter;
 import java.net.InetAddress;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class FileOutputFormat implements OutputFormat {
 
     @Override
     public void writeOutput(String output) throws Exception{
 
         if (LogRotator.getCSVLogFlag()) {
             csvPrintWriter.close();
            CSV2JS.convert(csvFileName);
             createCSVFile(LogRotator.getNewTimeStamp());
             LogRotator.setCSVLogFlag(false);
         }
 
         String[] vmCounters = output.split(";");
         for(String countersPerVM : vmCounters)
             csvPrintWriter.println(countersPerVM);
     }
 
     public static void createCSVFile(String timestamp) throws Exception {
         
         String hostname = InetAddress.getLocalHost().getHostName();
         csvFileName = hostname + "_" + timestamp + ".csv";
         String csvFileLoc = Main.getJVMONLogDir() +  File.separator + csvFileName;
         
         csvPrintWriter = new PrintWriter(new FileWriter(csvFileLoc), true);
         String message = "CSV file name - " + csvFileName;
         logger.log(Level.INFO, message);
         
         csvPrintWriter.println(header);
     }
     
     public static String getCSVFileName(){
         return csvFileName;
     }
 
     private static String csvFileName;
     private static final Logger logger = Main.getLogger();
     private static PrintWriter csvPrintWriter = null;
     private static String header = "timestamp,proc_id," + 
                                     "used_heap(MB),comm_heap(MB),used_non-heap(MB),comm_non-heap(MB)," +
                                     "curr_loaded_classes,tot_loaded_classes,tot_unloaded_classes," + 
                                     "daemon_thrd_count,peak_thrd_count,current_thrd_count,total_started_thrd_count," + 
                                     "tot_compilation_time(ms),";
           
 }
