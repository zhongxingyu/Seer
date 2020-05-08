 
 import java.util.*;
 import java.util.ArrayList;
 import java.lang.*;
 import java.io.File;
 import java.io.*;
 
 public class Clock {
 
     private static boolean DEBUG = true; // for sterling algor.
     private boolean DEBUG_OUT = false;    // for internal use, comment to false
     private int missPenalty = 0,
             dirtyPagePenalty = 0, pageSize = 0,
             vaBits = 0, paBits = 0, frameCount = 0,
             numOfProcesses = 0;
     private String referenceFile;
     private ArrayList<Process> processList = new ArrayList<Process>();
     private Process running = null;
     private PageTable pageTable = null;
     private boolean[] bitmap;
     private ArrayList<PageTable.Page> clockStruct;
 
     public static void main(String... arg) {
         Clock clock = new Clock();
         clock.run();
     }
 
     public Clock() {
         readSettings("MemoryManagement.txt");
         bitmap = new boolean[frameCount];
         clockStruct = new ArrayList<PageTable.Page>(frameCount);
         for (int i = 0; i < bitmap.length; ++i) {
             bitmap[i] = false; //false is free
         }
         pageTable = new PageTable(pageSize);
         readReference(referenceFile);
     }
 
     public void run() {
         System.err.println();
         int memoryCycle = 1;
 
         System.out.println("References file: " + referenceFile
                          + "\nPage size: " + pageSize
                          + "\nVA size: " + vaBits
                          + "\nPA size: " + paBits
                          + "\nMiss penalty: " + missPenalty
                          + "\nDirty page penalty: " + dirtyPagePenalty
                          + "\nDebug: " + DEBUG
                          + "\nFrame Count: " + frameCount
                          + "\n"
                          + "\nRunning Clock\n========");
 
         while (processList.size() > 0) {
             while (!checkIfReady(processList.get(0))) {
                 /* set top process to the next process and
                 push the top to the end of the queue */
                 Process top = processList.get(0);
                 processList.remove(0); // Take O(N)
                 processList.add(top);
             } // if there's penalty on all processes this will generate an infinite loop
 
             running = processList.get(0); // this has to change ! update Note to self
             processList.remove(0); // Take O(N)
 
             System.out.println("Running " + running.getPid());
             while (running.topRef()!=null && checkRefIfValid(running.topRef())) {
                 int tmpBurst = running.getBurst();
                 while (tmpBurst > 0) {
                     --tmpBurst;
                     for (int i = 0; i < processList.size(); ++i) {
                         Process tmp = processList.get(i);
                         tmp.decPenaltyTime(); // nvr goes below 0
                         processList.set(i, tmp);
                     }
                 }
                 if(DEBUG){
                     System.out.print("Clock: ");
                     for (int i = 0; i < clockStruct.size(); ++i) {
                         System.out.print(i + " ");
                     }
                     System.out.print("Free frames: ");
                     for (int i = 0; i < bitmap.length; ++i) {
                         System.out.print((!bitmap[i]) ? i + " " : "");
                     }
                     System.out.println();
                 }
                 
                 System.out.print("R/W: " + (( running.topRef().getReadOrWrite() )?"R":"W")
                        + "; VA: " + running.topRef().getAddress()/pageSize 
                         + "; Offset: " + running.topRef().getAddress()%pageSize
                         + "; ");
                 System.out.println();
                 memoryCycle++;
                 running.popRef();
             }
             if(running.topRef()==null)break;
             int freePageIndex = nextFreePage();
             PageTable.Page tmpPage = pageTable.new Page(running.topRef().getAddress() >> 6);
             if (freePageIndex >= 0) {
                 clockStruct.add(tmpPage);
                 running.setPenaltyTime(missPenalty);
             } else {
                 boolean isDirty = false;
                 // this means loop around finding unref'ed. pages.
                 for (int i = 0; i < clockStruct.size(); ++i) {
                     //check if ref is = 0
                     if (clockStruct.get(i).referenced == false) {
                         clockStruct.set(i, tmpPage);
                         isDirty = true;
                         running.setPenaltyTime(missPenalty);
                         break;
                     }
                 }
                 if(isDirty){
                       clockStruct.set(0, tmpPage);
                       running.setPenaltyTime(missPenalty+dirtyPagePenalty);
                 }
             }
             running.popRef();// we no longer need this.
 
             // move to the back of the queue
             processList.add(running);
         }
     }
 
     public int nextFreePage() {
         for (int i = 0; i < bitmap.length; ++i) {
             if (bitmap[i] == false) {
                 return i;
             }
         }
         return -1;
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
                     tmpPid = scan.nextInt(); // pid
                     tmpBurst = scan.nextInt(); // burst
                     tmpNumOfRefs = scan.nextInt();  // number of references
                     if (DEBUG_OUT) {
                         System.out.println("\npid: " + tmpPid +
                                 "\nburst: " + tmpBurst +
                                 "\nnum of refs: " + tmpNumOfRefs);
                     }
 
                     ArrayList<Reference> refs = new ArrayList<Reference>();
                     while (scan.hasNextLine()) {
                         String line = scan.nextLine();
                         if (line.isEmpty()) {
                             continue; // this means is an empty line, ignore.
                         }
                         if (DEBUG_OUT) {
                             System.out.println("\nline: " + line +
                                     "\nsizeOfLine: " + line.length()); // output for debug
 
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
             System.out.println("Exception in readReference(), in clock class ");
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
                 if (DEBUG_OUT) {
                     System.out.println("in readSetting()\nargnument: " + arg + "\nvalue: " + value);
                 }
                 setValue(arg, value);
             }
         } catch (java.io.FileNotFoundException e) {
             System.out.println("Exception in readSetting(), in clock class ");
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
 
     boolean checkIfReady(Process process) { // true if no penalty
         return (process.getPenaltyTime() == 0);
     }
 
     boolean checkRefIfValid(Reference ref) {
         int VA = ref.getAddress();
         int index = VA /pageSize ;
 
         if(DEBUG_OUT){
             System.out.println("checking for out of bound index is:"+index
                     +"\nVirtual Address: "+VA
                     );
 
         }
         return pageTable.getPageAtIndex(index).valid;
     }
 }
