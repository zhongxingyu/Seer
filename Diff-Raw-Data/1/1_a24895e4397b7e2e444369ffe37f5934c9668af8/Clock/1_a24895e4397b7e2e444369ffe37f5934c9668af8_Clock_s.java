 
 import java.util.*;
 import java.util.ArrayList;
 import java.lang.*;
 import java.io.File;
 import java.io.*;
 
 public class Clock {
 
   private static boolean DEBUG = true; // for sterling algor.
   private boolean DEBUG_OUT = true;    // for internal use, comment to false
   
   private int missPenalty = 0,
     dirtyPagePenalty = 0, pageSize = 0,
     vaBits = 0, paBits = 0, frameCount = 0,
     numOfProcesses = 0;
   private String referenceFile;
   private ArrayList<Process> processList = new ArrayList<Process>();
   private Process running = null;
   private PageTable pageTable = null;
 
   public static void main(String... arg) {
     Clock clock = new Clock();
     clock.run();                
   }
 
   public Clock() {
     readSettings("MemoryManagement.txt");
     pageTable = new PageTable(pageSize);
     readReference(referenceFile);
   }
 
   public void run() {
     while (processList.size() > 0) {
       running = processList.get(0);
       processList.remove(0); // Take O(N)
       int tmpBurst = running.getBurst();
 
       while (tmpBurst > 0) {
         --tmpBurst;
       }
 
       // page faults
 
 
       if (running.getNumOfRef() > 0) {
         running.decNumOfRef();
       } else {
         continue;
       }
 
       // move to the back of the queue
       processList.add(running);
     }
   }
 
   private void readReference(String filename) {
     /*
      *# of processes
      * process list
      * pid
      * burst, Defined as the number of memory references
      between I/O requests.
      * # of references for the process
      list of references for the process.
      Each reference indicates whether it was a
      read or a write
     */
     try {
       Scanner scan = new Scanner(new File(filename));
 
       if (scan.hasNextLine()) {
         numOfProcesses = scan.nextInt();
       } else {
         System.out.println("Error: Nothing to read. Reference File.\n");
       }
 
       for (int i = 0; i < numOfProcesses; ++i) {
         if (scan.hasNextLine()) {
           int tmpPid, tmpBurst, tmpNumOfRefs;
          //*************************************** problem here, we are not ignoring whitespaces. ! java !
           tmpPid = scan.nextInt(); // pid
           tmpBurst = scan.nextInt(); // burst
           tmpNumOfRefs = scan.nextInt();  // number of references
           if(DEBUG_OUT){
             System.out.println("\npid: "+tmpPid+
                                "\nburst: "+tmpBurst+
                                "\nnum of refs: "+ tmpNumOfRefs
                                );
           }
 
           ArrayList<Reference> refs = new ArrayList<Reference>();
           while (scan.hasNextLine()) {
             String line = scan.nextLine();
             if(line.isEmpty()) continue;
             if(DEBUG_OUT){
               System.out.println("\nline: "+line+
                                  "\nsizeOfLine: "+line.length()
                                  ); // output for debug 
             
             }
             Scanner scanLine = new Scanner(line);
             Reference reference = new Reference(scanLine.nextInt(), // address
                                                 (scanLine.hasNext("R")) // read or write
                                                 );
             refs.add(reference);
             
           }
           processList.add(new Process(tmpPid, tmpBurst, tmpNumOfRefs, refs));
         }
       }
     } catch (java.io.FileNotFoundException e) {
       System.out.println("Exception in readReference(), in clock class " );
       e.printStackTrace();
     }
   }
 
   private void readSettings(String filename) {
     /*referenceFile=references.txt
       missPenalty=1
       dirtyPagePenalty=0
       pageSize=1024
       VAbits=16
       PAbits=13
       frameCount=5
       debug=true
     */
     try {
       Scanner scan = new Scanner(new File(filename));
       String line, value, arg;
       while (scan.hasNextLine()) {
         line = scan.nextLine();
         int indexEquals = line.indexOf("=");
         arg = line.substring(0, indexEquals);
         value = line.substring(indexEquals + 1);
         if(DEBUG_OUT){
           System.out.println("in readSetting()\nargnument: "+arg+"\nvalue: "+value);
         }
         setValue(arg, value);
       }
     } catch (java.io.FileNotFoundException e) {
       System.out.println("Exception in readSetting(), in clock class " );
       e.printStackTrace();
     }
   }
 
   private void setValue(String arg, String value) {
     if (arg.equals("missPenalty")) {
       missPenalty = Integer.valueOf(value);
     } else if (arg.equals("debug")) {
       DEBUG = Boolean.valueOf(value);
     } else if (arg.equals("referenceFile")) {
       referenceFile = value;
     } else if (arg.equals("dirtyPagePenalty")) {
       dirtyPagePenalty = Integer.valueOf(value);
     } else if (arg.equals("pageSize")) {
       pageSize = Integer.valueOf(value);
     } else if (arg.equals("VAbits")) {
       vaBits = Integer.valueOf(value);
     } else if (arg.equals("PAbits")) {
       paBits = Integer.valueOf(value);
     } else if (arg.equals("frameCount")) {
       frameCount = Integer.valueOf(value);
     } else {
       System.out.println("Error: Argument not found! \nArgument:" + arg + "\nValue: " + value);
     }
   }
 }
