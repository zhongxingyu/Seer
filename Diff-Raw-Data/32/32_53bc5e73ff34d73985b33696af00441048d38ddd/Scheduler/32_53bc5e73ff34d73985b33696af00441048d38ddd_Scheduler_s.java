 /**
  * 
  * @author Eduard Thamm
  * @matnr 0525087
  * @brief A Scheduling server handling TaskEngins and Clients
  * @detail This Server manages TEs by receiving their isAlive messages and querying them for data if necessary as well as suspending them if they are not needed.
  *  It also manages company data for accounting and billing purposes. 
  * This program was not written with security in mind 
  * !!!DO NOT USE IN PRODUCTIVE ENVIROMENT!!!
  */
 
 
 import java.io.*;
 import java.net.*;
 import java.util.Enumeration;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.*;
 
 
 public class Scheduler extends AbstractServer {
     
     private int uPort;
     private int minT;
     private int maxT;
     private int tout;
     private int checkP;
     private static final int BUFSIZE = 1024;
     private static final int MAXCOEF = 1024;
     private Timer etime;
     private DatagramSocket uSock = null;
     private final static String usage = "Usage: Scheduler tcpPort udpPort min max tomeout checkPeriod\n";
     private ConcurrentHashMap<String,GTEntry> GTs = new ConcurrentHashMap<String,GTEntry>();
     private ConcurrentHashMap<String,Company> Companies = new ConcurrentHashMap<String,Company>();
     private ExecutorService contE = Executors.newCachedThreadPool();
     private Controller c = null;
     private static final boolean DEBUG = false;
     
     public Scheduler(int tcpPort, int udpPort, int min, int max, int timeout, int checkPeriod){
         Tport = tcpPort;
         uPort = udpPort;
         minT = min;
         maxT = max;
         tout = timeout;
         checkP = checkPeriod;
     }
     
     private void control() {
         c = new Controller();
         c.start();        
     }
     
     public void inputListen(){
         InputListener i = new InputListener();
         i.start();        
     }
     
     public void tcpListen() throws IOException {
         TCPListener l = new TCPListener();
         l.start();
         
     }
     
     public void readCompanies() throws FileNotFoundException{
         InputStream in = null;
         in = ClassLoader.getSystemResourceAsStream("company.properties");
         if(in != null){
             java.util.Properties companies = new java.util.Properties();
             try {
                 companies.load(in);
                 Set<String> companyNames = companies.stringPropertyNames();
                 
                 for (String companyName : companyNames){
                     String password = companies.getProperty(companyName);
                     Company c = new Company();
                     c.name = companyName;
                     c.password = password;
                     Companies.put(c.name, c);
                 }
                 
             } catch (IOException e) {
                 System.out.print("Could not read from company.properties. Exiting.\n");
                 exitRoutineFail();
                 if(DEBUG){e.printStackTrace();}
             }
         }
         else{
             throw new FileNotFoundException();
         }
     }
     
     private GTEntry schedule(String t){//String is load.
         int load = 0;
         int coef = MAXCOEF;
         GTEntry gc = null;
         GTEntry g = null;
         
         if(t.contentEquals("LOW")){
             load = 33;
         }
         if(t.contentEquals("MIDDLE")){
             load = 66;
         }
         if(t.contentEquals("HIGH")){
             load = 100;
         }
         if(load == 0 || GTs.isEmpty()){
             return g;
         }
         
         updateLoads();
 
         Enumeration<GTEntry> ge = GTs.elements();
         while(ge.hasMoreElements()){
             gc = ge.nextElement();
             if((gc.load+load) < 101 && (gc.maxE -gc.minE) < coef){
                 g = gc;
                 coef = gc.maxE -gc.minE;
             }
         }
         g.load = g.load+load;
         return g;
     }
     
     private void efficencyCheck(){
         etime = new Timer(true);
         etime.scheduleAtFixedRate(new ECheck(), checkP, checkP);
     }
     
     private void updateLoads(){
         Enumeration<GTEntry> ge = GTs.elements();
         ExecutorService es = Executors.newCachedThreadPool();
         GTEntry g;
         
         while(ge.hasMoreElements()){
             g = ge.nextElement();
             try {
                 es.execute(new LWorker(new Socket(g.ip,g.tcp),g));
             } catch (UnknownHostException e) {
                 if(DEBUG){e.printStackTrace();}
             } catch (IOException e) {
                 if(DEBUG){e.printStackTrace();}
             }
             //maybe do err msgs
         }
         return;
 
     }
     
     
     public boolean loggedIn(String ip){
         Enumeration<Company> ce = Companies.elements();
         while(ce.hasMoreElements()){
         	Company c = ce.nextElement();
             if(c.via.contains(ip) && c.line == COMPANYCONNECT.online){
                 return true;
             }
         }
         return false;
         //client dies will be trouble already asked
     }
     
     
     public void exitRoutine(){
         contE.shutdownNow();
         if(uSock != null){uSock.close();}
         etime.cancel();
         cancelGETimer();
         logoutCompanies();
         super.exitRoutine();
     }
     
     public void exitRoutineFail(){
         contE.shutdownNow();
         if(uSock != null){uSock.close();}
         etime.cancel();
         logoutCompanies();
         super.exitRoutineFail();
     }
     
     private void cancelGETimer(){
     	if (!GTs.isEmpty()) {
     		Enumeration<GTEntry> gi = GTs.elements();
             while(gi.hasMoreElements()) {
             	GTEntry g = gi.nextElement();
             	g.stopTimer();
             }	
         }
     }
     
     private void logoutCompanies(){
         Enumeration<Company> ce = Companies.elements();
         while(ce.hasMoreElements()){
             Company c = ce.nextElement();
             if(c.line == COMPANYCONNECT.online){
                 c.line = COMPANYCONNECT.offline;
             }
         }
         for(Socket s : CSocks){
             try {
                 s.close();
             } catch (IOException e) {
                 if(DEBUG){e.printStackTrace();}
             }
         }
     }
 
     
     public static void main(String[] args) {
         
         if(args.length != 6){
             System.out.print(usage);
             System.exit(1);
         }
         
         try {
             Scheduler sched = new Scheduler(Integer.parseInt(args[0]),Integer.parseInt(args[1]),Integer.parseInt(args[2]),Integer.parseInt(args[3]),Integer.parseInt(args[4]),Integer.parseInt(args[5]));
             
             sched.readCompanies();
             sched.inputListen();
             sched.control();
             sched.tcpListen();
             sched.efficencyCheck();
             
             
         } catch (IOException e) {
             System.out.println("Ran in to an I/O Problem. Most likely Some config file is missing.");
             if(DEBUG){e.printStackTrace();}
             return;
         }
           catch(NumberFormatException e){
             System.out.print(usage + "All values must be integers.\n");
             if(DEBUG){e.printStackTrace();}
             System.exit(1);
         }
 
     }
     
 
     // Client handling is done in worker.
     
 
 
     private class Worker extends AbstractServer.Worker{
         public Worker(Socket s) {
             super(s);
         }
         public void run(){
             try{
                 PrintWriter out = new PrintWriter(Csock.getOutputStream());
                 BufferedReader in = new BufferedReader(new InputStreamReader(Csock.getInputStream()));
                 
                 String input, output;
                 
                 while((input = in.readLine()) != null){
                     output = processInput(input,Csock.getInetAddress().toString().substring(1));
                     out.println(output);
                     out.flush();
                 }
                 in.close();
                 out.close();
                 Csock.close();
                 return;
             }
             catch(IOException e){
                 if(DEBUG){e.printStackTrace();}
             }
         }
 /*
  *  Preconditions: Client ensures that in !requestEngine only HIGH,MIDDLE,LOW are allowed on in[2], Companies not null
  *  Postconditions:
  */
         private String processInput(String input, String ip) {
             String[] in = input.split(" ");
             //what if client dies set a timeout? answer bad luck have to wait for shed reboot
             if(!in[0].contains("!login") && !loggedIn(ip)){
                 return "Please log in first.";
             }
 
 
             if(in[0].contentEquals("!requestEngine")){
                 GTEntry g = schedule(in[2]);
                 if(g == null){
                     return "Not enough capacity. Try again later.";
                 }
                 Enumeration<Company> ce = Companies.elements();
                 while(ce.hasMoreElements()){
                 	Company c = ce.nextElement();
                     if(c.via.contentEquals(ip)){
                         if(in[2].contains("HIGH")){
                             c.high++;
                         }
                         if(in[2].contains("MIDDLE")){
                             c.middle++;
                         }
                         if(in[2].contains("LOW")){
                             c.low++;
                         }
                     }
                 }
                 return "Assigned engine: "+g.ip+" "+g.tcp+" to task "+in[1];
                 
                 
             }
             if(in[0].contentEquals("!login")){
                 Enumeration<Company> ce = Companies.elements();
                 while(ce.hasMoreElements()){
                 	Company c = ce.nextElement();
                     if(in[1].contentEquals(c.name) && in[2].contentEquals(c.password) && c.line != COMPANYCONNECT.online){
                         c.via = ip;
                         c.line = COMPANYCONNECT.online;
                         return "Successfully logged in.";
                     }  
                 }
                 return"Wrong company or password.";
                 
             }
             if(in[0].contentEquals("!logout")){
                 Enumeration<Company> ce = Companies.elements();
                 while(ce.hasMoreElements()){
                 	Company c = ce.nextElement();
                     if(c.via.contains(ip)){
                         c.line = COMPANYCONNECT.offline;
                         c.via = null;
                         break;
                     }
                 }
                 return "Successfully logged out.";   
             }
             
             
             return "Unrecognised message send !login,!logout, or !requestEngine."; //would not do this in production gives away to much info.
         }
         
     }
         
     
     
     
     
     private class LWorker extends AbstractServer.Worker{
         GTEntry g;
         
         LWorker(Socket s, GTEntry gt){
             super(s);
             g = gt;
         }
         
         public void run(){
             try {
                 PrintWriter sout = new PrintWriter(Csock.getOutputStream(), true);
                 BufferedReader sin = new BufferedReader(new InputStreamReader(Csock.getInputStream()));
                 
                 sout.println("!Load");
                 sout.flush();
                 g.load = Integer.parseInt(sin.readLine());
                 
                 sin.close();
                 sout.close();
                 Csock.close();
                 return;
                 
             } 
               catch (IOException e) {
                 if(DEBUG){e.printStackTrace();}
                 return;
               }
               catch (NumberFormatException e){
                   System.out.println("Recieved a non number value from TE" + g.ip);
                   if(DEBUG){e.printStackTrace();}
                   return;
               }
         }
     }
    
     
     //udp controlling of GTEs is done here
     
     private class Controller extends Thread{
         byte[] buf = new byte[BUFSIZE];
         private DatagramPacket in = new DatagramPacket(buf, buf.length);
         
         /*
          * Preconditions: none
          * Postconditions: a controller listens on the designated port and instantiates a worker for each new incoming GTE, or send msgs to tes
          */
         public void run(){
             try {
                 uSock = new DatagramSocket(uPort);
                 
             } catch (SocketException e) {
                 System.out.print("Unable to listen on UDP "+uPort+"\n");
                 if(DEBUG){e.printStackTrace();}
                 return;
             } 
             
             while(true){
                 try {
                     uSock.receive(in);
                     contE.execute(new CWorker(in));
                 } 
                 catch (IOException e) {
                     if(DEBUG){e.printStackTrace();}
                     return;    
                 }
             }
         }
         
 
     }
     
     private class CWorker implements Runnable{
         //One CWorker manages on GTE
         private DatagramPacket in = null;
         public CWorker(DatagramPacket in) {
             this.in = in;
         }
         
         public CWorker(){
         	
         }
         
         public void run(){
             if(in == null){
                 System.out.println("Something with handing the Datagram to the worker went wrong. He recieved NULL.");
                 return;
             }
             try{
             String inString = new String(in.getData(), 0, in.getLength());
             String rcv[] = inString.split(" ");
         	if (!GTs.isEmpty()) {
         		Enumeration<GTEntry> gi = GTs.elements();
                 while(gi.hasMoreElements()) {
                 	GTEntry g = gi.nextElement();
                     if (g.ip.contains(in.getAddress().toString().substring(1)) && g.tcp == Integer.parseInt(rcv[0])) {
                         if (g.status != GTSTATUS.suspended) {//ignore isAlives of suspended engines
                             try{
                             g.resetTimer();
                             g.status = GTSTATUS.online;
                             }
                             catch(NullPointerException e){
                                 if(DEBUG){e.printStackTrace();}
                             }                            
                             return;
                         } else {
                             return;// mutliple ges now supported but trouble with isAlives (runns in normal problems only in debug)
                         }
                     }
                 }
             }
             //engine unknown make new
             GTEntry g = new GTEntry(in.getAddress().toString().substring(1),Integer.parseInt(rcv[0]), in.getPort(), GTSTATUS.online, Integer.parseInt(rcv[1]), Integer.parseInt(rcv[2]), 0);
             GTs.put(g.ip+g.tcp, g);
             g.startTimer();
             } catch(NumberFormatException e){
                 if(DEBUG){System.out.print("An isAlive from a new TaskEngine is malformated. IP: "+in.getAddress().toString().substring(1)+" \n");}
                 if(DEBUG){e.printStackTrace();}
             }
             
         }
         
         public void sendToTaskEngine(GTEntry g, String msg){
             byte[]  buf = new byte[BUFSIZE];
             buf = msg.getBytes();
             try {
               DatagramPacket p = new DatagramPacket(buf, buf.length,InetAddress.getByName(g.ip),g.udp);
               uSock.send(p);
           } catch (UnknownHostException e) {
               System.out.print("Could not send message to "+g.ip+" on UDP "+g.udp+" Host Unknown.\n");
               if(DEBUG){e.printStackTrace();}
           } catch (IOException e) {
               System.out.print("Could not send message to "+g.ip+" on UDP "+g.udp+" I/O Error.\n");
               if(DEBUG){e.printStackTrace();}
           }
           }
         
     }
     
     
     //Handling user input
     
     private class InputListener extends Thread{
         public void run(){
             BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
             String userin;
             
             try {
                 while((userin = stdin.readLine()) != null){
                     if(userin.contentEquals("!engines")){
                         int i = 1;
                         Enumeration<GTEntry> gi = GTs.elements();
                         while(gi.hasMoreElements()) {
                         	GTEntry g = gi.nextElement();
                             System.out.print(i+". "+g.toString());
                             i++;
                         }                    
                         userin = "";
                     }
                     if(userin.contentEquals("!companies")){
                         int i = 1;
                         Enumeration<Company> ce = Companies.elements();
                         while(ce.hasMoreElements()){
                         	Company c = ce.nextElement();
                             System.out.print(i+". "+c.toString());
                             i++;
                         }
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
     private class GTEntry{
         private String ip = "";
         private int tcp = 0;
         private int udp = 0;
        private GTSTATUS status;
         private int minE = 0;
         private int maxE = 0;
         public volatile int load = 0;
        private boolean isAlive = true;
         Timer time;
         
         public GTEntry(String ip, int tcp, int udp, GTSTATUS status, int minE, int maxE , int load){
             this.ip = ip;
             this.tcp = tcp;
             this.udp = udp;
             this.status = status;
             this.minE = minE;
             this.maxE = maxE;
             this.load = load;            
         }
         
         public void startTimer(){
             time = new Timer(true);
             time.scheduleAtFixedRate(new Timeout(this), 0, tout);        
         }
         
         
         
         public void stopTimer(){
             time.cancel();
             time.purge();
         }
         
         public void resetTimer(){
         	isAlive = true;
         }
         
         public String toString(){
             return("IP: "+ip+", TCP: "+tcp+", UDP: "+udp+", "+status.toString()+", Energy Signature: min "+minE+", max "+maxE+", Load: "+load+"%\n");
         }
         
         private class Timeout extends TimerTask{
         	GTEntry g = null;
         	public Timeout(GTEntry g){
         		this.g = g;
         	}
 
             public void run() {
                 if(g.isAlive){
                 	g.isAlive = false;
                 	return;
                 }
                 else{
                 	g.status=GTSTATUS.offline;
                 }
                 return;
             }
             
         }
     }
     
     private class Company{
         private String via = "";
         private String name = "";
         private String password = ""; //this is inherently unsafe in production use encryption
         private COMPANYCONNECT line = COMPANYCONNECT.offline;
         private int low = 0;
         private int middle = 0;
         private int high = 0;
         
         public String toString(){
             return(name+" ("+line.toString()+") LOW: "+low+", MIDDLE: "+middle+", HIGH: "+high+"\n");
         }
     }
     
     private class ECheck extends TimerTask{
         
         public ECheck(){
         }
 
         public void run() {
             int highUsers = 0;
             int emptyRunners = 0;
             int gtsUp = 0;
             Enumeration<GTEntry> gi = GTs.elements();
             while(gi.hasMoreElements()) {
             	GTEntry g = gi.nextElement();
                 if(g.status == GTSTATUS.online){
                     gtsUp++;
                     if(g.load == 0){
                         emptyRunners++;
                     }
                     if(g.load > 65){
                         highUsers++;
                     }
                 }
             }
             if(!GTs.isEmpty() && (highUsers == gtsUp && gtsUp < maxT && gtsUp < GTs.size()) || (gtsUp < minT && gtsUp < GTs.size())){
                 // no engine <66 load up and less than max engines active and inactive engines exist
                 // active smaller min and suspended available
                 GTEntry minEngine = GTs.elements().nextElement();//the first element
                 gi = GTs.elements();
                 while(gi.hasMoreElements()) {
                 	GTEntry g = gi.nextElement();
                     if(minEngine.minE < g.minE && g.status == GTSTATUS.suspended){
                         minEngine = g;
                     }
                 }
                 activate(minEngine);
                 //worst case first engine and that is offline well s happens
             }
             if(emptyRunners > 1 && gtsUp > minT){
                 GTEntry maxEngine = GTs.elements().nextElement();
                 gi = GTs.elements();
                 while(gi.hasMoreElements()) {
                 	GTEntry g = gi.nextElement();
                     if(maxEngine.maxE > g.maxE && g.status == GTSTATUS.online){
                         maxEngine = g;
                     }
                 }
                 suspend(maxEngine);
             }
             
         }
         
         private void suspend(GTEntry g){
             (new CWorker()).sendToTaskEngine(g, "!suspend");
             GTs.get(g.ip+g.tcp).status = GTSTATUS.suspended;
             g.stopTimer();
         }
         
         private void activate(GTEntry g){
             (new CWorker()).sendToTaskEngine(g, "!wakeUp");
             GTs.get(g.ip+g.tcp).status = GTSTATUS.offline; // will change to online once the first is alive is received cautious approach don't know if machine will respond
             
         }
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
