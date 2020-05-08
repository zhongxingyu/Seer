 /**
  * @author Eduard Thamm
  * @matnr 0525087
  * @brief Client program for DSLab.
  * @detail Simple Client for the DsLab performs various activities like logging in. requesting
  * resources and some things more. This program was not written with security in mind 
  * !!!DO NOT USE IN PRODUCTIVE ENVIROMENT!!!
  */
 
 import java.io.*;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.*;
 
 public class Client implements Callbackable{
     
     private String mancomp;
     private int port;
     private String sname;
     private File tdir;
     private ExecutorService e = Executors.newCachedThreadPool();
     private static final boolean DEBUG = false;
     private Callbackable cb;
     private Adminable admin = null;
     private Companyable comp = null;
 
     /*
      * Preconditions: srv, p not null
      * Postconditions: new Client is created server and port are set
      */
     public Client(String sn, String dir){
         sname = sn;
         tdir = new File(dir);
         if (!tdir.exists()){
             System.out.print(tdir.getName()+"does not exists.\n");
             System.exit(1);
         }
         if(!tdir.isDirectory()){
             System.out.print(tdir.getName()+"is not a directory.\n");
             System.exit(1);
         }
     }
     
     
     private void readRegProp() throws FileNotFoundException{
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
                         mancomp = p;
                     }
                     if(prop.contentEquals("registry.port")){
                         port = Integer.parseInt(p);
                     }
                 }
                 
             } catch (IOException e) {
                 System.out.print("Could not read from registry.properties. Exiting.\n");
                 exit();
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
     
     public void createStub(){
         try {
             cb = (Callbackable) UnicastRemoteObject.exportObject(this, 0);
         } catch (RemoteException e) {
             if(DEBUG){e.printStackTrace();}
             System.out.println("Could not export Callback. Exiting...");
             exit();
             return;
         }
     }
     
     public void sendMessage(String msg){
         System.out.println(msg);
     }
     
     
     /*
      * Preconditions: none
      * Postconditions: program execution started
      */
     public void run() throws IOException{
         BufferedReader stdin = new BufferedReader( new InputStreamReader(System.in));
         String userin;
         boolean exitFlag = false;
         
         while(!exitFlag && (userin = stdin.readLine()) != null){
             String usercmd[] = userin.split("\"");
             String usersp[] = usercmd[0].split(" ");
             if(usercmd.length > 1){
                 String tmp[] = new String[3];
                 tmp[0] = usersp[0];
                 tmp[1] = usersp[1];
                 tmp[2] = usercmd[1]; 
                 usersp = tmp;
             }
             try{
                 exitFlag = checkAction(usersp);
             }
             catch(NumberFormatException e){
                 System.out.print("You entered a non integer value. Please enter an Integer value.\n");
             }
         }
         return;
     
     }
     
     /*
      * Preconditions: user input received
      * Postconditions: user input processed and handling function called accordingly
      */
     private boolean checkAction(String[] in) throws NumberFormatException, IOException{
         //both
         if(in[0].contentEquals("!login")){
             if(in.length != 3){
                 System.out.print("Invalid parameters. Usage: !login username password.\n");
                 return false;
             }
             Comunicatable b = login(in[1],in[2]);
             if(b instanceof Adminable){
                 admin = (Adminable) b;
             }
             else{
                 comp = (Companyable) b;
             }
             return false; 
         }
         if(in[0].contentEquals("!logout")){
             logout();
             return false; 
         }
         //admin
         if(in[0].contentEquals("!getPricingCurve")){
             getPcurve();
             return false; 
         }
         if(in[0].contentEquals("!setPriceStep")){
             if(in.length != 3){
                System.out.print("Invalid parameters. Usage: !stePriceStep taskCount percent.\n");
                return false;
             }
             setPstep(Integer.parseInt(in[1]),Double.parseDouble(in[2]));
            return false; 
         }
         //company
         if(in[0].contentEquals("!list")){
             list();
             return false; 
         }
         if(in[0].contentEquals("!credits")){
             credits();
             return false; 
         }
         if(in[0].contentEquals("!buy")){
             if(in.length != 2){
                 System.out.print("Invalid parameters. Usage: !buy amount.\n");
                 return false;
             }
             buyC(Integer.parseInt(in[1]));
             return false; 
         }
         if(in[0].contentEquals("!prepare")){
             if(in.length != 3){
                System.out.print("Invalid parameters. Usage: !prepare taskname tasktype.\n");
                return false;
             }
             prepare(in[1],in[2]);
            return false; 
         }
         if(in[0].contentEquals("!executeTask")){
             if(in.length != 3){
                 System.out.print("Invalid parameters. Usage: !executeTask taskid startscript.\n");
                 return false;
             } 
             executeTask(Integer.parseInt(in[1]),in[2]);
            return false; 
         }
         if(in[0].contentEquals("!info")){
             if(in.length != 2){
                 System.out.print("Invalid parameters. Usage: !info taskid.\n");
                 return false;
             }
             info(Integer.parseInt(in[1]));
             return false; 
         }
         if(in[0].contentEquals("!getOutput")){
             if(in.length != 2){
                 System.out.print("Invalid parameters. Usage: !getOutput taskid.\n");
                 return false;
             }
             getOut(Integer.parseInt(in[1]));
             return false; 
         }
         //exit
         if(in[0].contentEquals("!exit")){
             exit(); 
             return true;
         }
         else{
             System.out.print("Command not recognised.\n");
             return false;
         }
     }
     
     
     private void getOut(int parseInt) throws RemoteException {
         if(comp == null && admin == null){System.out.println("Your not logged in!");}
         if(comp == null){System.out.println("Your not a Company!");}
         comp.getOutputOf(parseInt);
     }
 
 
     private void buyC(int parseInt) throws RemoteException {
         if(comp == null && admin == null){System.out.println("Your not logged in!");}
         if(comp == null){System.out.println("Your not a Company!");}
         if(comp.buyCredits(parseInt)){
             System.out.println("You have bought "+parseInt+" Credits. Your balance now is: " +comp.getCredits());
         }
     }
 
 
     private void credits() throws RemoteException {
         if(comp == null && admin == null){System.out.println("Your not logged in!");}
         if(comp == null){System.out.println("Your not a Company!");}
         System.out.println("You have: "+comp.getCredits());
         
     }
 
 
     private void setPstep(int i, double j) throws RemoteException {
         if(admin == null){System.out.println("Your not an Admin!");}
         admin.setPrice(i,j);
     }
 
 
     private void getPcurve() throws RemoteException {
         if(admin == null){System.out.println("Your not an Admin!");}
         Set<Map.Entry<Integer,Double>> ps = admin.getPrices();
         System.out.println("Task count | Discount");
         for(Map.Entry<Integer,Double> e : ps){
             System.out.println(e.getKey()+" | "+e.getValue() +"%");
             
         }
         
     }
 
     
     /*
      * Preconditions: user, pass not null
      * Postconditions: Connection is build, reading and writing lines are opened. User is logged in to server, or error is thrown.
      */
     private Comunicatable login(String user, String pass){
        /*if (System.getSecurityManager() == null) {
             System.setSecurityManager(new SecurityManager());
        } ***see manager*** */
         try {
             if(admin != null || comp != null){System.out.println("Already logged in.");}
             Registry r = LocateRegistry.getRegistry(mancomp, port);
             Loginable l = (Loginable) r.lookup(sname);
             return l.login(user, pass, cb);
         }
         catch(RemoteException e){
             if(DEBUG){e.printStackTrace();}
             return null;
         } catch (NotBoundException e) {
             if(DEBUG){e.printStackTrace();}
             return null;
         }
             
     }
     
     /*
      * Preconditions: logged in
      * Postconditions: logged out
      */
     private void logout() throws RemoteException{
         if(admin != null){
             admin.logout();
             admin = null;
             return;
         }
         if(comp != null){
             comp.logout();
             comp = null;
             return;
         }
         System.out.println("Need to login first!");
         return;
     }
     
     
     // Task life cycle
     
     
     /*
      * Preconditions: none
      * Postconditions: new task with unique taskid prepared
      */
     private void prepare(String task, String type) throws IOException{
         if(comp == null && admin == null){System.out.println("Your not logged in!");}
         if(comp == null){System.out.println("Your not a Company!");}
         TASKTYPE typ = null;
         if(type.contentEquals("LOW")){
             typ = TASKTYPE.LOW;
             
         }
         else{
             if(type.contentEquals("MIDDLE")){
                 typ = TASKTYPE.MIDDLE;
             }
             else{
                 if(type.contentEquals("HIGH")){
                     typ = TASKTYPE.HIGH;
                 }
         
                 else{
                     System.out.print("Invalid type. Use [LOW|MIDDLE|HIGH].\n");
                     return;
                 }
             }
         }
         File f = new File(tdir.getAbsolutePath()+File.separator+task);
         if(!f.exists()){
             System.out.print("No such file exists: "+f.getAbsolutePath()+"\n");
             return;
         }
         byte[] ba = new byte[(int) f.length()];  
         FileInputStream fis = new FileInputStream(f);
         BufferedInputStream bis = new BufferedInputStream(fis);
         bis.read(ba,0,ba.length);
         
         Task t = new Task(task, typ, ba.length, ba);
         comp.prepareTask(t);
         
     }
     
     
     /*
      * Preconditions: logged in, taskEngine assigned to task, task file still exists
      * Postconditions: task starts executing
      */
     private void executeTask(int id, String script) throws RemoteException{
         if(comp == null){System.out.println("Your not a Company!");}
         comp.executeTask(id, script);
     }
     
     
     // Local functions
     
     
     /*
      * Preconditions: none
      * Postconditions: printed all files in task directory to stdout
      */
     private void list(){
         if(comp == null){System.out.println("Your not a Company!");}
         String cont[] = tdir.list();
         int i = 0;
         while(i < cont.length){
             System.out.print(cont[i]+"\n");
             i++;
        }
     }
 
     /*
      * Preconditions: none
      * Postconditions: Task info printed to std out
      */
     private void info(int id) throws RemoteException{
         if(comp == null && admin == null){System.out.println("Your not logged in!");}
         if(comp == null){System.out.println("Your not a Company!");}
         comp.getTaskInfo(id);
         
     }
     
     /*
      * Preconditions: none
      * Postconditions: all open handles are released, program terminates
      */
     private void exit() {
         System.out.print("Exiting on request. Good Bye!\n");
         try {
             logout();
         } catch (RemoteException e1) {
             if(DEBUG){e1.printStackTrace();}
         }
         e.shutdownNow();
 
     }
 
     //  Nested Classes and Main
 
     
 
     
     public static void main (String args[]){
         
         final String usage = "DSLab Client usage: java Client.java managementComponent taskdir";
         Client c;
         
         if(args.length != 2){
             System.out.print(usage);
             System.exit(1); //return value
         }
         
         try{
             c = new Client(args[0], args[1]);
             c.readRegProp();// catch fnf
             c.run();
         } catch(NumberFormatException e){
             System.out.print("Second argument must be an Integer value.\n");
             System.exit(1);//return value
         } catch (IOException e) {
             if(DEBUG){e.printStackTrace();}
         }
         
     }
 }
 
