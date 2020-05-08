 import java.io.*;
 import java.lang.reflect.*;
 import java.net.InetAddress;
 import java.util.StringTokenizer;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 /**
  * ProcessManager is a process management tool which support Migratable process and load
  * re-distribution (job balancing).
  * 
  * @author Guanyu Wang
  * */ 
 public class ProcessManager {
 
   private static RunningProcessTable process_table;
   private static SlaveTable slave_table;
   private static Thread listenthread;
   private static ProcessBalancer pb;
   private static SlaveNotifier sn;
  private static int localport;
  private static int masterport;
 
   private static String mode = new String();
   private static String masterhostname = new String();
   private static String localhostname;
   
   public static void LocalPs() {
     if (process_table.size() < 1) {
       System.out.println("no running processes");
       System.out.flush();
       return;
     }
     for (MigratableProcess t : process_table.keySet()) {
       System.out.println(process_table.get(t));
       System.out.flush();
     }
   }
   
   public static void GlobalPs() {
     if (process_table.size() < 1) {
       System.out.println("no running processes");
       System.out.flush();
     }
     else{
       System.out.println("local processes:");
     }
     for (MigratableProcess t : process_table.keySet()) {
       System.out.println(process_table.get(t));
       System.out.flush();
     }
     System.out.println("global processes:");
     if(mode == "Slave"){
       
       Serializer ser = new Serializer();
       byte[] instruction = new byte[1];
       instruction[0] = Byte.valueOf("1");
       
       byte[] content = (localhostname + " " + localport).getBytes();
       System.out.println("send gps request to: "  + masterhostname + masterport);
       ByteSender bsender = new ByteSender(masterhostname, masterport, instruction, content);
       bsender.run();
       bsender.close();
     }
     else{
       for(String slavehost: slave_table.keySet()){
         System.out.println("Slave name and port number: " + slavehost);
         RunningProcessTable temprpt = slave_table.get(slavehost.split(" "));
         if(temprpt.size() < 1){
           System.out.println("no running processes");
         }
         else{
           for(MigratableProcess mp: temprpt.keySet()){
             System.out.println(temprpt.get(mp));
           }
         }
       }
     }
   }
 
   
 
   public static void Runprocess(String command) throws ClassNotFoundException, Exception {
     
     try{
       ProcessRunner pr = new ProcessRunner(command, process_table);
       Thread t = new Thread(pr);
       t.start();
     }catch (InvocationTargetException e) {
       System.out.println("");
       System.out.println("Invalid arguments for input command.");
     } catch (Exception e) {
       e.printStackTrace();
     }
      
   }
 
   private static void Parser(String command) throws Exception {
     StringTokenizer stoken;
     String cmd;
     stoken = new StringTokenizer(command);
     if (stoken.hasMoreTokens()) {
       cmd = stoken.nextToken();
       if (cmd.equals("ps") && !stoken.hasMoreElements()) {
         LocalPs();
         
       } 
       else if (cmd.equals("gps") && !stoken.hasMoreElements()) {
         GlobalPs();
       }else if (cmd.equals("quit") && !stoken.hasMoreElements()) {
         System.exit(0);
       } else {
         Runprocess(command);
       }
 
     } else {
       System.out.println("");
       System.out.println("Please input command.");
       return;
     }
   }
    
   
   private static void ArgParser(String args[]){
     if(args.length == 0 ){
       mode = "Master";
       localport = 4444;
     }
     if(args.length % 2 != 0){
       System.out.println("Invalid arguments for ProcessManager.");
      System.out.println("Usage: no args or ”-p <local port> (for master)” ”-c <hostname>” ”-mp <master port>” ”-sp <s port>” ");
       System.exit(0);
     }
     for(int i = 0; i < args.length; ++i){
       if(args[i].equals("-c")){
         ++i;
         mode = "Slave";
        localport = 4444;
         masterhostname = args[i];
       }
       else if(args[i].equals("-p")){
         ++i;
         mode = "Master";
         localport = Integer.parseInt(args[i]);
       }
       else if(args[i].equals("-mp")){
         ++i;
         mode = "Slave";
         masterport = Integer.parseInt(args[i]);
       }
       else if(args[i].equals("-sp")){
         ++i;
         mode = "Slave";
         localport = Integer.parseInt(args[i]);
       }
       else {
         System.out.println("Invalid arguments for ProcessManager.");
        System.out.println("Usage: no args or ”-c <hostname>” ”-mp <master port>” ”-sp <s port>” ");
         System.exit(0);
       }
     }
   }
   
 
   public static void main(String args[]) throws Exception {
 
     ArgParser(args);
     
     String commandline;
     BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
     process_table = new RunningProcessTable();
     slave_table = new SlaveTable();
     
     CommListener cmmMM = new CommListener(mode, localport, process_table, slave_table);
     listenthread = new Thread(cmmMM);
     listenthread.start();
     
     InetAddress addr = InetAddress.getLocalHost();
     localhostname = addr.getCanonicalHostName();
     
     if(mode == "Master"){
       pb = new ProcessBalancer(slave_table, process_table);
       ScheduledExecutorService schExec = Executors.newScheduledThreadPool(8);
       ScheduledFuture<?> schFuture = schExec.scheduleWithFixedDelay(pb, 0, 5, TimeUnit.SECONDS);
     }
     else{
       String[] localhost = new String[2];
       localhost[0] = localhostname;
       localhost[1] = Integer.toString(localport);
       sn = new SlaveNotifier(masterhostname, masterport, localhost, process_table);
       ScheduledExecutorService schExec = Executors.newScheduledThreadPool(8);
       ScheduledFuture<?> schFuture = schExec.scheduleWithFixedDelay(sn, 0, 5, TimeUnit.SECONDS);
     }
     
     System.out.println("Localhostname: " + localhostname);
     while (true) {
       System.out.print("==> ");
       commandline = in.readLine();
       Parser(commandline);
     }
   }
 }
