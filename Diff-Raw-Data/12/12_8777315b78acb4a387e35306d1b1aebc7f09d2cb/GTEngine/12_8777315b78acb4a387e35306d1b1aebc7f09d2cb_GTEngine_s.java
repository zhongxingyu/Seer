 
 
 import java.io.*;
 import java.net.*;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * 
  * @author Eduard Thamm 0525087
  *
  */
 public class GTEngine extends AbstractServer {
     
     private String schedIP;
     private int schedUPort;
     private File tdir;
     private int isAl;
     private int minC;
     private int maxC;
     private boolean terminate = false;
     private volatile int load = 0;//Load needs to be threadsafe volatile should suffice according to JLS 17.4.3 and 17.7
     private final static String usage = "Usage GTEngine tcpPort schedulerHost schedulerUDPPort alivePeriod minComsumption maxConsumption taskDir";
     private final static boolean DEBUG = true;
     private DatagramSocket uSock = null;
     private Timer time = new Timer();;
     
     public GTEngine(int tp, String sched, int udp, int ia, int min, int max, String td){
         Tport = tp;
         schedIP = sched;
         schedUPort = udp;
         isAl = ia;
         minC = min;
         maxC = max;
         tdir = new File(td);
         if (!tdir.exists()){
             System.out.print(tdir.getName()+" does not exists. Creating....\n");
             if(!tdir.mkdir()){
                 System.out.print("Can not create "+tdir.getName()+". Exiting.\n");
                 exitRoutine();
             }
             
         }
         if(!tdir.isDirectory()){
             System.out.print(tdir.getName()+"is not a directory.\n");
             return;
         }        
     }
     
     private void inputListen(){
         InputListener i = new InputListener();
         i.start();        
     }
     
     private boolean UDPConnect(){
         try {
             uSock = new DatagramSocket();
             uSock.connect(InetAddress.getByName(schedIP), schedUPort);
             return true;
         } catch (SocketException e) {
             System.out.println("Could not bind to UDP Port exiting.");
             if(DEBUG){e.printStackTrace();}
             return false;
         } catch (UnknownHostException e) {
             System.out.println("Could not find "+schedIP+" exiting.");
             if(DEBUG){e.printStackTrace();}
             exitRoutine();
             return false;
         }
     }
     
     
     private void UDPListen(){
         UDPWorker u = new UDPWorker ();
         u.start();
     }
     
     /*
      * Preconditions: timer exists
      * Postconditions: isAlive timer is set at fixed rate
      */
     private void startIsAlive(){
         time.scheduleAtFixedRate(new IASender(), isAl, isAl);
     }
     
     /*
      * Preconditions: timer exists
      * Postconditions: isAlive timer is canceled and recreated
      */
     private void stopIsAlive(){
         time.cancel();
         time = new Timer();
     }
     
     
     /*
      * Preconditions: n is in TASKTYPE
      * Postconditions: returns true and updates load of engine if this results in load <=100%, false otherwise
      */
     private boolean upLoad(String n){
         if(n.contentEquals("HIGH")){
             load += 100;
             if(load<101){
                 return true;
             }
             load -= 100;
             return false;
         }
         if(n.contentEquals("MIDDLE")){
             load += 66;
             if(load<101){
                 return true;
             }
             load -= 66;
             return false;
         }
         if(n.contentEquals("LOW")){
             load += 33;
             if(load<101){
                 return true;
             }
             load -= 33;
             return false;
         }
         return false;
     }
     
     
     /*
      * Preconditions: n is in TASKTYPE
      * Postconditions: returns true and updates load of engine if this results in load >=0%, false otherwise
      */
     private boolean downLoad(String n){
         if(n.contentEquals("HIGH")){
             load -= 100;
             if(load>-1){
                 return true;
             }
             load += 100;
             return false;
         }
         if(n.contentEquals("MIDDLE")){
             load -= 66;
             if(load>-1){
                 return true;
             }
             load += 66;
             return false;
         }
         if(n.contentEquals("LOW")){
             load -= 33;
             if(load>-1){
                 return true;
             }
             load += 33;
             return false;
         }
         return false;
     }
     
     public void exitRoutine(){
         terminate = true;
         time.cancel();
         uSock.close();
         super.exitRoutine();
         return;
     }
     
     public void exitRoutineFail(){
         terminate = true;
         time.cancel();
         uSock.close();
         super.exitRoutineFail();
         return;
     }
     
     
     
     
     public static void main(String[] args) {
         if(args.length != 7){
             System.out.println(usage);
             System.exit(1);
         }
         
         try{
             GTEngine gt = new GTEngine(Integer.parseInt(args[0]),args[1],Integer.parseInt(args[2]),Integer.parseInt(args[3]),Integer.parseInt(args[4]),Integer.parseInt(args[5]),args[6]);
             if(!gt.UDPConnect()){
                 return;
             }
             gt.inputListen();
             gt.UDPListen();
             gt.tcpListen();
             gt.startIsAlive();
             
             
         }
         catch(NumberFormatException e){
             if(DEBUG){e.printStackTrace();}
             System.out.println("All input numbers must be Integers.");
             System.exit(1);
         } catch (IOException e) {
             
             if(DEBUG){e.printStackTrace();}
         }
         
     }
     
     
     private class Worker extends AbstractServer.Worker{
         
         
         public Worker(Socket s) {
             super(s);
         }
         //CAUTION THIS RUNS UNVALIDATED USER INPUT AND FILES DO NOT USE IN PRODUCTION!!!
         public void run(){
             String execln;
             String tid;
             String ttype;
             String tname;
             long flength;
             
             try {
                 BufferedReader textin = new BufferedReader(new InputStreamReader(Csock.getInputStream()));
                 DataInputStream datain= new DataInputStream(Csock.getInputStream());
                 PrintWriter toCl = new PrintWriter(Csock.getOutputStream());
                 
                 //Receive all parameters. 
                 execln = textin.readLine();
                 if(execln.contentEquals("!Load")){//handle load requests
                     PrintWriter toSc = new PrintWriter(Csock.getOutputStream());
                     toSc.println(load);
                     toSc.flush();
                     Csock.close();
                     return;
                 }
                 tid = textin.readLine();
                 tname = textin.readLine();
                 ttype = textin.readLine();
                 flength = Long.parseLong(textin.readLine());
                 
                 //Find free filename and create the file 
                 int num = 0;
                 while(new File(tdir.getAbsolutePath()+tname+num).exists()){
                     num++;
                 }
                 File f = new File(tdir.getAbsolutePath()+tname+num);
                 f.createNewFile();
                 
                 //Receive the file
                 byte[] ba = new byte[(int) flength];
                 FileOutputStream fos = new FileOutputStream(f);
                 BufferedOutputStream bos = new BufferedOutputStream(fos);
                 toCl.println("Send");
                 toCl.flush();
                 datain.read(ba, 0, ba.length);
                 
                 bos.write(ba);
                 bos.flush();
                 bos.close();
                 fos.close();
                 
                 
                 
                 if(!upLoad(ttype)){
                     toCl.println(tid+" Not enough capacity. Try again later.");
                 }
                 else{
                     //Replace name in cmd string.
                     String rpl = tname+num;
                     execln.replace(tname.substring(2), rpl);//TODO This does not replace!!!
                     //fork and pipe stdout to sock
                    Process p = Runtime.getRuntime().exec(execln);
                     BufferedReader pin = new BufferedReader(
                             new InputStreamReader(p.getInputStream()));
                     
                     String in;
                     while ((in = pin.readLine()) != null) {
                         toCl.println("Task " + tid + ": " + in);
                     }
                    //TODO why do I only get error output
                     //clean up
                     pin.close();
                     p.destroy();
                     downLoad(ttype);
                 }
                 toCl.println("Finished Task "+tid);
                 toCl.flush();
                 toCl.close();
                 Csock.close();
                 f.delete();
                 return;
                 
                 
             } catch (IOException e) {
                 System.out.println("Could not read from Socket");
                 if(DEBUG){e.printStackTrace();}
                 return;
             }
         }
     }
 
     private class InputListener extends Thread{
         public void run(){
             BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
             String userin;
             
             try{
                 while((userin = stdin.readLine()) != null){
                     if(userin.contentEquals("!load")){
                         System.out.println("Current load: " + load);
                         userin = "";
                     }
                     if(userin.contentEquals("!exit")){
                         System.out.println("Exiting on request. Bye.");
                         exitRoutine();
                         return;
                     }
                     if(!userin.contentEquals("")){
                         System.out.print("Unknown command.\n");
                     }
                 }
             } catch (IOException e) {
                 System.out.print("Could not read from stdin.\n");
                 if(DEBUG){e.printStackTrace();}
                 return;
             }
 
         
         }
     }
     /*
      * Preconditions: only one UDPWorker active at a time
      * Postconditions: Manages activate suspend msgs
      */
     private class UDPWorker extends Thread{
         
         public UDPWorker() {
         }
         
         public void run(){
             DatagramPacket in = new DatagramPacket(new byte[1024], 0);
 
             while(!terminate){
                 try {
                     uSock.receive(in);
                     if(in != null){
                         if(in.getData().toString().contains("!suspend")){
                             stopIsAlive();
                         }
                         if(in.getData().toString().contains("!wakeUp")){
                             startIsAlive();
                         }
                     }
                 } catch (IOException e) {
                     if(DEBUG){e.printStackTrace();}
                     System.out.println("Error recieving UDP Massages from Scheduler I/O.");
                 }
                 
             }
         }
         
     }
     
     private class IASender extends TimerTask{
         public void run() {
             String IAMsg = Tport+" "+minC+" "+maxC;
             byte[] ba = IAMsg.getBytes();
             DatagramPacket p;
             try {
                 p = new DatagramPacket(ba,0,ba.length,InetAddress.getByName(schedIP),schedUPort);
                 uSock.send(p);
             } catch (UnknownHostException e1) {
                 System.out.println("Could not send isAlive. HostUnknown");
                 if(DEBUG){e1.printStackTrace();}
                 return;
             } catch (IOException e) {
                 System.out.println("Could not send isAlive. I/O");
                 if(DEBUG){e.printStackTrace();}
                 return;
             }
 
             
         }
         
     }
 
     public void tcpListen() throws IOException {
         TCPListener l = new TCPListener();
         l.start();
         
     }
     
     private class TCPListener extends Thread{
         public void run(){
         try {
             Ssock = new ServerSocket(Tport);
         } catch (IOException e) {
             System.out.print("Could not listen on port: " + Tport + "\n");
             if(DEBUG){e.printStackTrace();}
             exitRoutineFail();
         }
         
         while(true){
             try {
                 abservexe.execute(new Worker(Ssock.accept()));
             } catch (IOException e) {
                 if(DEBUG){e.printStackTrace();}
                 return;
             }
         }
     }
 }
 
 }
