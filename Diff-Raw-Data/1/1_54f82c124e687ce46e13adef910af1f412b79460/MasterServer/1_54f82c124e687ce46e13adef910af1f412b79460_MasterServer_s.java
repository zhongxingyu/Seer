 import java.rmi.*;
 import java.rmi.server.*;
 import java.rmi.registry.*;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 import java.util.concurrent.*;
 
 final class Node implements Serializable
 {
     String name;
     Status status;
     NodeFileServerInterface server;
     boolean isConnected;
     boolean beenCleaned;
     InetAddress address;
     int cores;
     ArrayList<FilePartition> files;
 }
 
 public class MasterServer extends UnicastRemoteObject implements MasterFileServerInterface
 {
     private static Registry rmiRegistry;
 
     //Info from Config file
     private static final String configFileName = Config.configFileName;
     private static int registryPort;
     private static String masterServerRegistryKey;
     private static final String serverName = "MasterServer";
 
     //Instance variables
     // DFS information
     private ConcurrentHashMap<String, Node> nodes;
     private List<DistributedFile> fileList;
 
     // MapReduce information
     private int currentJid;
     private int currentTid;
     private int currentNodeId;
     private Queue<Job> jobs;
     private LinkedList<Object[]> tasks; 
     private ConcurrentMap<Integer,Integer> jobMapsDone; //jid, maps done
     private ConcurrentMap<Integer,Integer> jobReducesDone;
     private ConcurrentMap<Integer,List<Node>> jobMapNodeList;
     private ConcurrentMap<Integer,HashMap<String,Node>> jobReduceNodeList;
     private Scheduler scheduler;
     private Heartbeater heartbeater;
     private boolean isRunning;
 
     public MasterServer() throws RemoteException
     {
         System.setProperty("sun.rmi.transport.tcp.responseTimeout", "5000");
         this.nodes = new ConcurrentHashMap<String, Node>();
         this.jobs = new LinkedList<Job>();
         this.tasks = new LinkedList<Object[]>();
         this.jobMapsDone = new ConcurrentHashMap<Integer,Integer>();
         this.jobReducesDone = new ConcurrentHashMap<Integer,Integer>();
         this.jobMapNodeList = new ConcurrentHashMap<Integer,List<Node>>();
         this.jobReduceNodeList = new ConcurrentHashMap<Integer,HashMap<String,Node>>();
         this.fileList = new LinkedList<DistributedFile>();
 
         this.isRunning = true;
         scheduler = new Scheduler();
         scheduler.start();
         heartbeater = new Heartbeater();
         heartbeater.start();
 
         parseFile(configFileName);
         this.currentJid = 0;
         this.currentNodeId = 0;
     }
 
     public class Heartbeater extends Thread {
 
         public Heartbeater() {
             System.setProperty("sun.rmi.transport.tcp.responseTimeout", "5000");
             System.out.println("Heartbeater started");
         }
         
         public void run() {
             while(isRunning) {
                 Enumeration<Node> enumerate = nodes.elements();
                 // Iterate through all nodes to check if connected
                 while (enumerate.hasMoreElements()) {
                     Node each = enumerate.nextElement();
                     try {
                         if (each.server != null) {
                             each.status = each.server.getStatus();
                             each.isConnected = true;
                         }
                         else
                             each.isConnected = false;
                     } catch (RemoteException e) {
                         // This is thrown when a node cannot be reached
                         each.isConnected = false;
                     }
 
                 }
                 try{
                     Thread.sleep(10);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
             System.out.println("HeartBeater Stopped");
         }
     }   
 
     
     public DistributedFile findDistributedFile(String name) {
         for (DistributedFile d : fileList) {
             if (d.getFileName().equals(name))
                 return d;
         }
         System.out.println("File not found");
         return null;
     }
     
     public void newJob(Job j) {
         try {
             System.out.println("Recieved Job " + j);
             int jid = currentJid++;
             int tid = 0;
             DistributedFile d = findDistributedFile(j.getInput());
             j.setDFile(d);
             
             j.setJid(jid);
             jobs.add(j);
             jobMapsDone.put(jid,0); 
             jobReducesDone.put(jid,0);
             jobMapNodeList.put(jid,new LinkedList<Node>());
             jobReduceNodeList.put(jid,new HashMap<String,Node>());
             List<FilePartition[]> blocks = j.getDFile().getBlocks();
             for(Object[] fps : blocks) {
                 MapTask m = new MapTask(tid++,jid,null,j); 
                 tasks.add(new Object[]{fps,m}); //TODO: change Scheduler
             }
             System.out.println("Finished Adding Map Tasks");
         } catch(Exception e) {
             e.printStackTrace();
         }
     }
 
     public void scheduleReducers(Job j) {
         ReduceTask r;
         int tid = 0;
         System.out.println("Scheduling Reducers for jid " + j.getJid());
         
         ///////////////// Use localDir //////////////////
         String localDir = Config.getLocalDirectory();
         for(int i = 0; i < j.getTotalReduces(); i++) {
             r = new ReduceTask(i,j.getJid(),null,j,localDir + j.getJid() + "part" + i); 
             r.setNodeList(jobMapNodeList.get(j.getJid()));
             r.setNodeId(i);
             tasks.add(new Object[]{null,r});
         }
         System.out.println("Finished Adding Reduce Tasks");
     }
     
 
     public void scheduleFinalReduce(Job j) { //compile all reduce outputs to local afs
         BufferedReader in;
         PrintWriter out = null;
         String line;
         char[] b;
         File f;
         System.out.println("Compiling all Reduces!");
   
         String localDir = Config.getLocalDirectory(); 
         try{
             out = new PrintWriter(new BufferedWriter(new FileWriter(j.getOutput(),true))); //final output file
             for(int i = 0; i < j.getTotalReduces(); i++) {
                 String fileName = localDir + j.getJid() + "part" + i; //file we are trying to get
                 f = new File(j.getJid() + "part" + i); //file that we download to locally
                 Node node = jobReduceNodeList.get(j.getJid()).get(fileName); //location of file we are trying to get
                 
                 FileIO.download(node.server,new File(fileName),f);//dowload file to local file
                 in = new BufferedReader(new FileReader(f));
                 b = new char[(int)f.length()];
                 in.read(b,0,b.length);//read local file
                 out.print(b);//appends read chars to output file
                 in.close(); //close the local file and delete it
                 f.delete();
             }
             
         } catch (Exception e) {
             e.printStackTrace(System.out);
         } finally {
             out.close();
         }
         System.out.println("DONE");
     }
                 
     // finished maptask t from node name, if all tasks are done, schedule
     // reducers
     public void finishedMap(MapTask t,String name) throws RemoteException{
         //System.out.println("Finished Map" + t.getTaskId() +" on node " + name);
         Job j = t.getJob();
         int maps = jobMapsDone.get(j.getJid()) + 1;
         jobMapsDone.put(j.getJid(),maps);
         if (!jobMapNodeList.get(j.getJid()).contains(nodes.get(name)))
             jobMapNodeList.get(j.getJid()).add(nodes.get(name));
         if (maps >= j.getTotalMaps()) {
             scheduleReducers(j); //TODO: start reducing as maps finish
             //jobMapsDone.remove(j.getJid());
         }
     }
     public void finishedReduce(ReduceTask t,String name) throws RemoteException{
         //System.out.println("Finished Reduce" + t.getTaskId()); 
         Job j = t.getJob();
         int reduces = jobReducesDone.get(j.getJid()) + 1;
         jobReducesDone.put(j.getJid(),reduces);
         jobReduceNodeList.get(j.getJid()).put(t.getOutputFile(),nodes.get(name));
         if(reduces >= j.getTotalReduces()){
             scheduleFinalReduce(j);
             //jobReducesDone.remove(j.getJid());
         }
     }
 
     public class Scheduler extends Thread {
         public LinkedList<Node> nodeQueue;
         
         public Scheduler() {
             System.out.println("Scheduler started");
             nodeQueue = new LinkedList<Node>(nodes.values());
         }
         
         public void scheduleMap(FilePartition[] fps, MapTask m) throws RemoteException{
             List<Node> ns = new ArrayList<Node>();
             
             for(int i = 0; i < fps.length; i++) {
                 ns.add(fps[i].getLocation());
             }
             
             for(Node n : nodeQueue) {
                 if(ns.contains(n) && !n.server.isFull()) {
                     m.setPartition(fps[ns.indexOf(n)]);
                     n.server.scheduleTask(m);
                     
                     nodeQueue.remove(n);
                     nodeQueue.add(n); 
                     tasks.removeFirst();
                     return;
                 }
             }
             System.out.println("Unable to Schedule Task " + m);
            System.exit(0);
         }
         public void scheduleReduce(ReduceTask t) throws RemoteException {
             Node n = nodeQueue.element(); 
             if(n.server.isFull())
                 nodeQueue.add(nodeQueue.removeFirst());
             else {
                 n.server.scheduleTask(t); 
                 
                 nodeQueue.remove(n);
                 tasks.removeFirst();
                 nodeQueue.add(n);
             }
         }
         public void run() {
             while(isRunning) {
                 try{
                     while(nodeQueue.size() > 0 && tasks.size() > 0) {
                         Object[] objs = tasks.getFirst();
                         Task t = (Task)objs[1]; 
                         if (t instanceof MapTask) 
                             scheduleMap((FilePartition[])objs[0],(MapTask)t);
                         else 
                             scheduleReduce((ReduceTask)t);
                     }
                 } catch(Exception e) {
                     e.printStackTrace(System.out);
                 }
             }
             System.out.println("Scheduler Stopped");
         }
     }   
         
             
     // This parses constants in the format
     // key=value from fileConfig.txt
     private void parseFile(String filename)
     {
         Properties prop = new Properties();
         try{
             prop.load(new FileInputStream(configFileName));
         } catch (FileNotFoundException e) {
             System.out.println("No config file found named: " + configFileName);
             prop = Config.generateConfigFile(); 
         } catch (IOException e)
         {
             e.printStackTrace(System.out);
         }
 
         //Check and assure file is formatted correctly
         if (! Config.checkConfigFile())
         {
             System.out.println("Invalid file config");
             return;
         }
         try{
             //Load in all config properties
             this.registryPort = Integer.parseInt(prop.getProperty("REGISTRY_PORT"));
             this.masterServerRegistryKey = prop.getProperty("MASTER_SERVER_REGISTRY_KEY");
 
             ArrayList<String> addresses = Config.getNodeAddresses();
             Iterator<String> iter = addresses.iterator();
             while (iter.hasNext())
                 addNode(iter.next());
 
         } catch (NumberFormatException e) {
             System.out.println("Incorrectly formatted number " + e.getMessage());
         }
         return;
     }
 
     // Initializes and adds a new Node object to Master's list of nodes
     private void addNode(String address)
     {
         InetAddress newAddress = null;
         try{
             newAddress = InetAddress.getByName(address);
         } catch(Exception e) {
             e.printStackTrace(System.out);
         }
         Node newNode = new Node();
         newNode.name = address;
         newNode.address = newAddress;
         newNode.server = null;
         newNode.isConnected = false;
         newNode.beenCleaned = false;
         Status newStat = new Status();
         newNode.status = newStat;
         newNode.files = new ArrayList<FilePartition>();
         this.nodes.put(address, newNode);
     }
 
     // This allows the server to be reached by any nodes or users
     public void start() throws RemoteException
     {
         // Get the old registry, or create a new one
         try{
             rmiRegistry = LocateRegistry.getRegistry(registryPort);
             System.out.println(Arrays.toString(rmiRegistry.list()));
             System.out.println("Registry server found");
         } catch (RemoteException e) {
             rmiRegistry = LocateRegistry.createRegistry(registryPort);
             System.out.println("Registry server created");
         }
 
         // Bind the new server
         try {
             rmiRegistry.bind(masterServerRegistryKey, this);
         } catch (Exception e) {
             e.printStackTrace();
         }
         // Clean all nodes
         for (Node eachNode : nodes.values().toArray(new Node[0])) {
             System.out.println("Cleaning " + eachNode.name);
             eachNode.server.cleanLocalDirectory();
         }
     }
 
     // This allows a user to stop the server
     public void stop() throws RemoteException
     {
         System.out.println("Stopping master");
         //This should probably do other things before ending
         //the registry
         try{
             stopNodes();
             System.out.println("Unbinding");
             //rmiRegistry.unbind(masterServerRegistryKey);
             System.out.println("Unexporting self");
             unexportObject(this, true);
             System.out.println("Unexporting Registry");
             unexportObject(rmiRegistry, true);
             System.out.println("Server stopped");
             this.isRunning = false;
 
         } catch (Exception e)
         {
             System.out.println("Hit exception");
             e.printStackTrace();
         }
         System.exit(0);
     }
   
     private void stopNodes() throws RemoteException
     {
         Enumeration<Node> enumerate = this.nodes.elements();
         do {
             System.out.println("Stopping a node");
             Node each = enumerate.nextElement();
             NodeFileServerInterface eachServer = each.server;
             if (eachServer != null) {
                 System.out.println(System.getProperty("sun.rmi.transport.tcp.responseTimeout"));
                 System.out.println("Sending a stop message");
                 try {
                     eachServer.stop();
                 } catch (RemoteException e) {
                     System.out.println(each.name + " could not be reached to stop");
                 }
                 System.out.println("Sent a stop message");
             }
         } while (enumerate.hasMoreElements());
         System.out.println("Done stopping nodes");
         
     }
 
     public void register(NodeFileServerInterface server, String address, int cores)
     {
         System.out.println("Client connected");
         Node foundNode = this.nodes.get(address);
         if (foundNode == null){
             System.out.println("Unrecognized client" + address + " ; ignoring");
             return;
         }
         if (foundNode.isConnected)
         {
             boolean check = (server == foundNode.server);
             System.out.println(address + " checked in and the check is " + check);
         }
         else
         {
             System.out.println(address + " is now connected");
             foundNode.isConnected = true;
             foundNode.server = server;
             foundNode.cores = cores;
             try{
                 if (!foundNode.beenCleaned)
                     foundNode.server.cleanLocalDirectory();
             } catch (RemoteException e) {
                 System.out.println("Unable to clean " + foundNode.name);
             }
             scheduler.nodeQueue.add(foundNode);
         }
     }
    
     //Adds a new file to the distributed file system
     //if host is either null or 'this' it must be local to thismaster
     public void addNewFile(String filename, FileServerInterface host) throws RemoteException
     {
         //Distribute the file among nodes
         System.out.println("Adding file " + filename + " from host " + host);
         //Determine if file name is already taken
         for (DistributedFile eachFile : fileList)
         {
             if (filename.equals(eachFile.getFileName())) {
                 System.out.println("File already exists with that name.");
                 return;
             }
         }
         //Download the file from the remote host
         File newFile = new File(filename);
         if ((host == null) || (host == this))
             System.out.println("File is already local");
         else {
             System.out.println("File " + filename + " is at remote host; downloading");
             try{
 
                 FileIO.download(host, newFile, newFile);
                 System.out.println("Downloaded " + filename + " from remote host");
             } catch(IOException e) {
                 System.out.println("Failed to download");
                 e.printStackTrace();
             }
         }
         // Partition the files and send it to nodes
         this.partitionFile(newFile);
         System.out.println(filename + " has been added to the DFS");
 
         return;
     }
 
     private void partitionFile(File originalFile)
     {
         DistributedFile dfile = null;
         System.out.println("Making " + originalFile.getName() + 
             " a distributed file...");
         try{
             dfile = new DistributedFile(originalFile);
             //Add nodes to all of the parts of dfile
             dfile = allocateFile(dfile);
             //Send relevant partitions to all nodes
             commit(dfile);
             //Remove local Copy
             File localDir = new File(Config.getLocalDirectory());
             localDir.mkdirs();
             for (File f:localDir.listFiles()) f.delete();
         } catch (Exception e) {
             System.out.println("Error");
             e.printStackTrace(System.out);
         }
         return;
     }
 
     private DistributedFile allocateFile(DistributedFile dfile)
     {
         ListIterator<FilePartition[]> iter = dfile.getBlocks().listIterator();
         // Maps between nodes and size of replicas added to this
         // Node on this Distributed File
         Map<Node, Integer> tempSize = new HashMap<Node, Integer>();
         // TODO: THIS SHOULD BE DYNAMIC
         while (iter.hasNext()) {
             FilePartition[] block = iter.next();
             Set<Node> placedNodes = new HashSet<Node>();
             // Place each partition replica on a node
             for (int i = 0; i < block.length; i++)
             {
                 FilePartition eachPartition = block[i];
                 // Place on node with fewest files
                 Node optimalNode = null;
                 int optSize = Integer.MAX_VALUE;
                 // Looks through all nodes to find optimal location to place
                 Enumeration<Node> nodeEnum = this.nodes.elements();
                 while (nodeEnum.hasMoreElements())
                 {
                     Node eachNode = nodeEnum.nextElement();
 
                     // Determines viable nodes for this replica
                     if (eachNode.isConnected && (!placedNodes.contains(eachNode)))
                     {
                         Integer tempNodeSize = tempSize.get(eachNode);
                         int thisSize = eachNode.files.size() + 
                             ((tempNodeSize == null) ? 0 : tempNodeSize);
                         if (thisSize < optSize) {
                             //This is the new optimal node
                             optSize = thisSize;
                             optimalNode = eachNode;
                         }
                     }
                 }
                 // This can be null if the replication factor
                 // is higher than the numberof online nodes
                 if (optimalNode != null)
                     placedNodes.add(optimalNode);
                 block[i].setLocation(optimalNode);
                 // Update the TempSize for the chosen node
                 Integer oldsize = tempSize.get(optimalNode);
                 Integer newsize = ((oldsize == null) ? 0 : oldsize)
                     + block[i].getSize();
                 tempSize.put(optimalNode, newsize);
             }
         }
         return dfile;
     }
 
     // This actually makes the network requests to inform the nodes what
     // files they should have based on this server's information
     private void commit(DistributedFile dfile) throws IOException
     {
         ListIterator<FilePartition[]> iter = dfile.getBlocks().listIterator();
         while (iter.hasNext()) {
             FilePartition[] eachBlock = iter.next();
             for (FilePartition eachPartition : eachBlock)
             {
                 Node destination = eachPartition.getLocation();
                 if (destination == null) {
                     System.out.println("File " + eachPartition.getFileName() + 
                         " has null location");
                     continue;
                 }
                 destination.files.add(eachPartition);
                 FileServerInterface server = destination.server;
                 File partitionFile = new File(eachPartition.getFileName());
                 FileIO.upload(server, partitionFile, partitionFile);
             }
         }
         this.fileList.add(dfile);
         System.out.println("File Commited");
     }
 
     public String monitorFiles() throws RemoteException
     {
         String s = "\n###### Files ######\n";
         Iterator<DistributedFile> iter = this.fileList.listIterator();
         if (!iter.hasNext())
             s = s.concat("\t No files");
         while (iter.hasNext()) {
             DistributedFile dfile = iter.next();
             s = s.concat("\n\t" + dfile.getFileName());
         }
         s = s.concat("\n\n###### End Files ######\n");
         return s;
     }
     
     public String monitorNodes() throws RemoteException
     {
         String s = "\n###### Nodes ######\n";
         Enumeration<Node> en = this.nodes.elements();
         while (en.hasMoreElements()) {
             Node n = en.nextElement();
             s = s.concat("\n\t" + n.name);
             s = s.concat("\n\t\tStatus: " + 
                 (n.isConnected ? "Connected" : "Not Connected"));
             s = s.concat("\n\t\tCores: " + n.cores + "; " 
                 + n.status.mapSlots + " Mappers and "
                 + n.status.reduceSlots +"Reducers");
             s = s.concat("\n\t\tNumber of File Partitions: " + n.files.size());
             s = s.concat("\n\t\tNumber of Tasks: " + n.status.tasks.size());
 
         }
         s = s.concat("\n\n###### End Nodes ######\n");
         return s;
     }
 
     //Prints out a status report of the whole system
     public String monitorAll() throws RemoteException
     {
         String s = "###### STATUS REPORT ######\n";
         Enumeration<Node> enumerate = this.nodes.elements();
         do {
             Node each = enumerate.nextElement();
             s = s.concat("\nReport for: " + each.name);
             s = s.concat(each.isConnected ? "\n\tConnected" : "\n\tNot Connected");
             s = s.concat("\n\tInetAddress: " + each.address);
 
             ListIterator<FilePartition> iter = each.files.listIterator();
             s = s.concat(iter.hasNext() ? "\n\tFiles are:\n" : "\n\tNo Files");
             while (iter.hasNext()) {
                 FilePartition fp = iter.next();
                 s = s + "\t\t" + fp.getFileName() + " part " + fp.getIndex() 
                     + " size: " + fp.getSize() + "\n";
             }
             if (each.status.tasks != null) {
                 Iterator<Task> taskIter = each.status.tasks.iterator();
                 s = s.concat(taskIter.hasNext() ? 
                     "\n\tTasks are:\n" : "\n\tNo Tasks");
                 while (taskIter.hasNext()) {
                     Task eachTask = taskIter.next();
                     Job j = eachTask.getJob();
                     s = s + "\t\tTask for " + j.getInput() + 
                         "; Task " + eachTask.getTaskId();
                 }
             }
         } while (enumerate.hasMoreElements());
         s = s.concat("\n\n######  END  REPORT  ######");
         return s;
     }
 
     public OutputStream getOutputStream(File f) throws IOException {
         return new RMIOutputStream(new RMIOutputStreamImpl(new 
                                         FileOutputStream(f)));
     }
     public InputStream getInputStream(File f) throws IOException {
         return new RMIInputStream(new RMIInputStreamImpl(new 
                                         FileInputStream(f)));
     }
     
     public static void main(String[] args) throws Exception
     {
         MasterServer server = new MasterServer();
         // 10 sec rmi timeout
         System.setProperty("sun.rmi.transport.tcp.responseTimeout", "5000");
         System.out.println(System.getProperty("sun.rmi.transport.tcp.responseTimeout"));
         server.start();
     }
 }
 
