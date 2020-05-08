 import java.io.*;
 import java.net.*;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Enumeration;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.concurrent.*;
 
 //TODO doe error handling on client imput
 
 public class Manager {
     private static final boolean DEBUG = true;
     private static final String usage = "Usage: bindingName schedulerHost schedulerTCPPort";
     private String bindingName;
     private String schedHost;
     private int schedTP;
     private String regHost;
     private int regPort;
     private Socket schedsock;
     private PrintWriter schedout;
     private BufferedReader schedin;
     private ExecutorService TaskEServ = Executors.newCachedThreadPool();
     private ConcurrentHashMap<String,User> Users = new ConcurrentHashMap<String,User>();
     private ConcurrentHashMap<Integer,Double> Prices = new ConcurrentHashMap<Integer,Double>();
     private ConcurrentHashMap<Integer,MTask> Tasks = new ConcurrentHashMap<Integer,MTask>();
 
     
     public Manager(String bn, String sh, int tp){
         bindingName = bn;
         schedHost = sh;
         schedTP = tp;
     }
     
     public void inputListen(){
         InputListener i = new InputListener();
         i.start();        
     }
     
     private void exitRoutine(){
         try {
             schedsock.close();
             //TODO
         } catch (IOException e) {
             if(DEBUG){e.printStackTrace();}
         }
     }
     
     private void exitRoutineFail(){
         try {
             schedsock.close();
             //TODO
         } catch (IOException e) {
             if(DEBUG){e.printStackTrace();}
         }
     }
     
     private void setupRMI(){
        if (System.getSecurityManager() == null) {
             System.setSecurityManager(new SecurityManager());
        }
         
         try {
             Loginable l = (Loginable) UnicastRemoteObject.exportObject(new LoginHandler(), 0);
             Registry r = LocateRegistry.createRegistry(regPort);
             r.rebind(bindingName, l);
             System.out.println("Bound login to"+bindingName);
         } catch (RemoteException e) {
             // TODO Auto-generated catch block
             if(DEBUG){e.printStackTrace();}
         }
     }
     
     private void schedConnect(){
         try {
             schedsock = new Socket(schedHost, schedTP);
             schedout = new PrintWriter(schedsock.getOutputStream(), true);
             schedin = new BufferedReader(new InputStreamReader(schedsock.getInputStream()));
         } catch (UnknownHostException e) {
             System.out.print("Login: Unknown Host, check server name and port.\n");
             if(DEBUG){e.printStackTrace();}
             System.exit(1);
         } catch (IOException e) {
             System.out.print("Login: Could not get I/O for "+schedHost+" \n");
             if(DEBUG){e.printStackTrace();}
             System.exit(1);
         }
     }
     
     private void readProperties() throws FileNotFoundException{
         readRegistry();
         readUsers();
     }
     
     private void readRegistry() throws FileNotFoundException{
         InputStream in = null;
         in = ClassLoader.getSystemResourceAsStream("registry.properties");
         if(in != null){
             java.util.Properties registry = new java.util.Properties();
             try {
                 registry.load(in);
                 Set<String> registryprops = registry.stringPropertyNames();
                 
                 for (String prop : registryprops){
                     String p = registry.getProperty(prop);
                     if(prop.contentEquals("registry.host")){
                         regHost = p;
                     }
                     if(prop.contentEquals("registry.port")){
                         regPort = Integer.parseInt(p);
                     }
                 }
                 
             } catch (IOException e) {
                 System.out.print("Could not read from registry.properties. Exiting.\n");
                 exitRoutineFail();
                 if(DEBUG){e.printStackTrace();}
             }
             catch(NumberFormatException e){
                 System.out.print("Your registry.proerties file is malformed. Port is not an integer.\n");
                 if(DEBUG){e.printStackTrace();}
             }
         }
         else{
             throw new FileNotFoundException();
         }
     }
     
     private void readUsers() throws FileNotFoundException{
         InputStream in = null;
         in = ClassLoader.getSystemResourceAsStream("user.properties");
         if(in != null){
             java.util.Properties users = new java.util.Properties();
             try {
                 users.load(in);
                 Set<String> userNames = users.stringPropertyNames();
                 String name = "";
                 String pw = "";
                 
                 for (String userName : userNames){
                     String attribute = users.getProperty(userName);
                     String user[] = userName.split(".");
                     if(user.length == 1){
                         name = userName;
                         pw = attribute;
                     }
                     else{
                         if(user[1].contentEquals("admin")){
                             if(attribute.contentEquals("true")){
                                 Users.put(name, new Admin(name, pw));
                             }
                         }
                         if(user[1].contentEquals("credits")){
                             Users.put(name, new User(name, pw, Integer.parseInt(attribute)));
                         }
                     }
                 }
                 
             } catch (IOException e) {
                 System.out.print("Could not read from user.properties. Exiting.\n");
                 exitRoutineFail();
                 if(DEBUG){e.printStackTrace();}
             }
             catch(NumberFormatException e){
                 System.out.print("Your user.proerties file is malformed, A credit value contains a non integer value.\n");
                 if(DEBUG){e.printStackTrace();}
             }
         }
         else{
             throw new FileNotFoundException();
         }
     }
 
     public static void main(String[] args) {
         if(args.length != 3){
             System.out.print(usage);
             System.exit(1);
         }
         
         try {
             Manager m = new Manager(args[0], args[1], Integer.parseInt(args[2]));
             
             m.readProperties();
             m.inputListen();
             m.schedConnect();
             m.setupRMI();
             
             
         } catch (IOException e) {
             System.out.println("Ran in to an I/O Problem. Most likely Some config file is missing.");
             if(DEBUG){e.printStackTrace();}
             return;
         }
           catch(NumberFormatException e){
             System.out.print(usage + "All number values must be integers.\n");
             if(DEBUG){e.printStackTrace();}
             System.exit(1);
         }
     }
     
     
     private class InputListener extends Thread{
         public void run(){
             BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
             String userin;
             
             try {
                 while((userin = stdin.readLine()) != null){
                     if(userin.contentEquals("!users")){
                         Enumeration<User> u = Users.elements();
                         int i = 1;
                         while(u.hasMoreElements()){
                             User a = u.nextElement();
                             System.out.println(i + a.toString());
                             i++;
                         }
                        
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
     
     private class User{
         private String name = "";
         private String password = ""; //this is inherently unsafe in production use encryption
         private int low = 0;
         private int middle = 0;
         private int high = 0;
         private volatile int credits = 0;
         private Callbackable callback = null;
         
         public User(String n, String pw, int c){
             name = n;
             password = pw;
             credits = c;
         }
         public User(String n, String pw){
             name = n;
             password = pw;
         }
         
         public String toString(){
             if(callback != null){
                 return(name+" (online) LOW: "+low+", MIDDLE: "+middle+", HIGH: "+high+"\n");
             }
             return(name+" (offline) LOW: "+low+", MIDDLE: "+middle+", HIGH: "+high+"\n");
         }
         
         public boolean verify(String pw){
             if(pw.contentEquals(password)){
                 return true;
             }
             return false;
         }
     }
     private class Admin extends User{
         
         public Admin(String n, String pw){
             super(n,pw);
         }
         
         public String toString(){
             if(super.callback != null){
                 return(super.name+" (online)");
             }
             return(super.name+" (offline)");
         }
         
 
     }
     
     private class Remote implements Companyable{
         private String name ="";
         private Callbackable cb = Users.get(name).callback;
         
         public Remote(String n){
             name = n;
         }
 
         public boolean buyCredits(int amount) throws RemoteException {
             Users.get(name).credits += amount;
             return true;
         }
 
         public void executeTask(int id, String execln) throws RemoteException {
             MTask t = Tasks.get(Integer.valueOf(id));
             if(t == null){
                 cb.sendMessage("No Task with id: "+id+" known.");
                 return;
             }
             if(t.owner.contentEquals(name)){
                 t.execln = execln;
                 TaskEServ.execute(new TaskExecutor(t, cb));
             }
                 return;
         }
 
         public int getCredits() throws RemoteException {
             return Users.get(name).credits;
         }
 
         public void getOutputOf(int id) throws RemoteException {
             // TODO Auto-generated method stub
             
         }
 
         public void getTaskInfo(int id) throws RemoteException {
             // TODO Auto-generated method stub
             
         }
 
         public void logout() throws RemoteException {
             // TODO Auto-generated method stub
         }
 
         public boolean prepareTask(Task t) throws RemoteException {
             MTask mt = new MTask(t);
             mt.status = TASKSTATE.prepared;
             Tasks.put(Integer.valueOf(mt.id), mt);
             return true;
         }
     }
     
     private class RAdmin implements Adminable{
         private String name ="";
         
         public RAdmin(String n){
             name = n;
         }
 
         public Set<Entry<Integer, Double>> getPrices() throws RemoteException {
             return Prices.entrySet();
         }
 
         public void logout() throws RemoteException {
             User me = Users.get(name);
             me.callback.sendMessage("Thanks for using our services. Bye.");
             me.callback = null;
             
         }
 
         public void setPrice(int step, double discount) throws RemoteException {
             if(Prices.containsKey(Integer.valueOf(step))){
                 Prices.replace(Integer.valueOf(step), Double.valueOf(discount));
             }
             else{
                 Prices.put(Integer.valueOf(step), Double.valueOf(discount));
             }
             return;           
         }
         
     }
     
     private class LoginHandler implements Loginable{
 
         public Comunicatable login(String uname, String password, Callbackable cb)
                 throws RemoteException {
             User u;
             if((u = Users.get(uname)) != null){
                 if(u.verify(password) && u.callback == null){
                     u.callback = cb;
                     cb.sendMessage("Logged in.");
                     if(u instanceof Admin){
                         return (Comunicatable) UnicastRemoteObject.exportObject(new RAdmin(uname));
                     }
                     return (Comunicatable) UnicastRemoteObject.exportObject(new Remote(uname));
                 }
             }
             cb.sendMessage("Wrong username or Password or account already in use.");
             return null;
         }
         
     }
     
     private class TaskExecutor implements Runnable{
         MTask m;
         Callbackable cb;
         
         public TaskExecutor(MTask mt, Callbackable c){
             m = mt;
             cb = c;
         }
 
         public void run() {
             try{
             schedout.println("!requestEngine "+m.id+" "+m.ttype.toString());
             String rcv = schedin.readLine();//TODO will i get the next or the one concerning me?
             //TODO assign fail and win
             }
             catch(IOException e){
                 if(DEBUG){e.printStackTrace();}
             }
             
             
             //Start talking to GTE
             Socket tsock = null;
             PrintWriter tout = null;
             DataOutputStream dout = null;
             BufferedReader tin = null;
             
             
             try {
                 tsock = new Socket(m.taskEngine,m.port);
                 dout = new DataOutputStream(tsock.getOutputStream());
                 tout = new PrintWriter(tsock.getOutputStream());
             } catch (UnknownHostException e) {
                 System.out.print("The Host Task Engine "+m.taskEngine+" is unknown. Can not connect.\n");
                 if(DEBUG){e.printStackTrace();}
                 return;
             } catch (IOException e) {
                 System.out.print("Sorry encounterd a problem in opening the outgoing Task Engine socket.\n");
                 if(DEBUG){e.printStackTrace();}
             }
             
             try {
                 tin = new BufferedReader(new InputStreamReader(tsock.getInputStream()));
             } catch (IOException e) {
                 System.out.print("Could not listen for replay from Task Engine.\n");
                 if(DEBUG){e.printStackTrace();}
                 return;
             }
         
             //Transmit the command string string.
             //BEWARE THIS IS UNVALIDATE USER INPUT!!!        
             tout.println(m.execln);
             tout.println(m.id);
             tout.println(m.tname);
             tout.println(m.ttype.toString());
             tout.println(m.flength);
             tout.flush();
             try {
                 tin.readLine(); //this is for sync; a Send will be received maybe useful for later implementations
             } catch (IOException e1) {
                 if(DEBUG){e1.printStackTrace();}
             }
             
 
             try {
                 byte[] ba = m.binary;
                 dout.write(ba,0,ba.length);
                 dout.flush();
             }
                 catch (IOException e) {
                 System.out.print("There was a problem with the remote connection, could not send file.\n");
                 if(DEBUG){e.printStackTrace();}
                 return;
             }
                 
                 
             try {
                 String in;
                 if((in = tin.readLine()).contains("execution started")){
                     m.status = TASKSTATE.executing;
                     m.start = System.currentTimeMillis();
                 }
                 else{
                     m.status = TASKSTATE.prepared;
                     cb.sendMessage("Currently no engine available, try again later.");
                     return;
                 }
 
                 while((in = tin.readLine())!= null){
                     m.output.concat(in);
                 }
                 m.status = TASKSTATE.finished;
                 m.finish = System.currentTimeMillis();
                 cb.sendMessage("Execution of Task "+m.id+" finished.");
                 
                 //TODO reduce credits of company
                 
                 tout.close();
                 dout.close();
                 tin.close();
                 tsock.close();
                 return;
                 
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 if(DEBUG){e.printStackTrace();}
             }
 
         }
         
         
         
     }
 
 }
