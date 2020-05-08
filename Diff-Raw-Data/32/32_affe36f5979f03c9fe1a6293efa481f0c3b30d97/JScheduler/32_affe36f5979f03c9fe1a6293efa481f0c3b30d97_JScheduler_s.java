 package scheduler;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 public class JScheduler {
     private SchedulerHeap sh; 
     private Parser p;
     private int [] stats;
     
     public JScheduler(String filename) {
             try {
             this.sh = new SchedulerHeap();
                 this.p = new Parser(filename);
             } catch (Exception e) {
                     System.out.println(e.getMessage());
             }     
             this.stats = new int [3];
     }
     
     public JScheduler() {
             this.sh = new SchedulerHeap();
             this.stats = new int [3];
     }
 
     public JScheduler(InputStream in) {
             this.sh = new SchedulerHeap();
             this.p = new Parser(in);
             this.stats = new int [3];
     }
 
     public SchedulerHeap getSh() {
         return this.sh;
     }
     
     public Parser getParser() {
         return this.p;
     }
 
     /* Public so it could be accessed from Parser.java */
     public static void help() {
             System.out.println ("# ");
             System.out.println ("# Usage:");
             System.out.println ("# java -classpath src/ src.scheduler.JScheduler [-f <file_with_orders>]");
             System.out.println ("# ");
     }
     
     private void writeStats() {
             System.out.println("##### Stats ######");
             System.out.println("# Pushed jobs: " + this.stats[0]);
             System.out.println("# Popped jobs: " + this.stats[1]);
             System.out.println("# Changed jobs: " + this.stats[2]);
             System.out.println("##################");
     }
 
     public static void main(String [] args) throws IOException {
             
             JScheduler scheduler = null;
 
             if (args.length == 0) { // no arguments - reading from System.in
                 System.out.println("# Reading from Standard Input");
                 scheduler = new JScheduler(System.in);
             } else if (args.length == 1) {
                 System.out.println("# Error! Too less parameters!");
                 help();
                 return;
             } else if (!args[0].equals("-f")) {
                 System.out.println("# Error! Unknown parameter (required -f)!");
                 help();
                 return;
             } else if (args[0].equals("-f")) 
                 scheduler = new JScheduler(args[1]);
             
             String s;
             int operation = 0;
             //System.out.println(scheduler.sh.isEmpty());
 
             while ((s = scheduler.getParser().getBr().readLine()) != null && !s.equals ("")) {
                     operation = scheduler.getParser().getNextOrder(s);
                     if (operation == 1) {
                         //System.out.println("Adding");
                         int [] tmp = scheduler.getParser().getCache();
                         scheduler.getSh().add(new Job(tmp[0], tmp[1]));
                         scheduler.stats[0]++;
                     } else if (operation == 2) {
                         //System.out.println("Removing");
                        scheduler.getSh().remove();
                         scheduler.stats[1]++;
                     } else if (operation == 3) {
                         //System.out.println("Changing");
                         int [] tmp = scheduler.getParser().getCache();
                         scheduler.getSh().changePriority(tmp[0], tmp[1]);
                         scheduler.stats[2]++;
                     }
             }
             scheduler.getSh().writeHeap();
             scheduler.writeStats();
     }
 }
