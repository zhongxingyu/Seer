 import java.rmi.*;
 import java.rmi.server.*;
 import java.rmi.registry.*;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 import java.util.concurrent.*;
 
 final class Node
 {
     String name;
     NodeFileServerInterface server;
     boolean isConnected;
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
     private volatile Hashtable<String, Node> nodes;
     private int currentJid;
     private int currentTid;
     public Queue<Job> jobs;
     public Queue<Task> tasks; 
     private Queue<Node> nodeQueue;
     public ConcurrentMap<Integer,Integer> jobMapsDone;
     public ConcurrentMap<Integer,Integer> jobReducesDone;
     public ConcurrentMap<Integer,HashMap<String, List<String>>> jobKvs;
     private Scheduler scheduler;
     private boolean isRunning;
 
     public MasterServer() throws RemoteException
     {
         System.setProperty("sun.rmi.transport.tcp.responseTimeout", "5000");
         this.nodes = new Hashtable<String, Node>();
         this.jobs = new LinkedList<Job>();
         this.tasks = new LinkedList<Task>();
         this.jobMapsDone = new ConcurrentHashMap<Integer,Integer>();
         this.jobReducesDone = new ConcurrentHashMap<Integer,Integer>();
         this.jobKvs = new ConcurrentHashMap<Integer,HashMap<String, List<String>>>();
         nodeQueue = new LinkedList<Node>(nodes.values());
         this.isRunning = true;
         scheduler = new Scheduler();
         scheduler.start();
 
         parseFile(configFileName);
         this.currentJid = 0;
     }
 
     public void putAll(HashMap<String, List<String>> partial, Job j) {
 	HashMap<String, List<String>> kvs = jobKvs.get(j.getJid());
 	if(kvs == null)
 	    kvs = new HashMap<String, List<String>>();
 	Iterator it = partial.keySet().iterator();
         while (it.hasNext()) {
             String key = (String)it.next();
             List<String> value = kvs.get(key);
 	    if (value == null) 
 		value = new LinkedList<String>();
 	    value.addAll(partial.get(key));
             kvs.put(key,value);
             it.remove(); 
 	}
 	jobKvs.put(j.getJid(),kvs);
     }
 
     public int count(String filename) throws IOException {
 	InputStream is = new BufferedInputStream(new FileInputStream(filename));
 	try {
 	    byte[] c = new byte[1024];
 	    int count = 0;
 	    int readChars = 0;
 	    boolean empty = true;
 	    while ((readChars = is.read(c)) != -1) {
 		empty = false;
 		for (int i = 0; i < readChars; ++i) {
 		    if (c[i] == '\n') {
 			++count;
 		    }
 		}
 	    }
 	    return (count == 0 && !empty) ? 1 : count;
 	} finally {
 	    is.close();
 	} 
     }
     
     public void newJob(Job j) {
 	try {
 	    System.out.println("Recieved Job " + j);
 	    int jid = currentJid++;
 	    j.setJid(jid);
 	    jobs.add(j);
 	    jobMapsDone.put(jid,0); 
 	    jobReducesDone.put(jid,0);
 	    jobKvs.put(jid,new HashMap<String,List<String>>());
 	    List<String> inputs = j.getInput();
	    for(int i = 0; i < inputs.size(); i++) { //TODO: variable mapTasks
 		String name = inputs.get(i);
 		FilePartition f = new FilePartition(name,0,count(name));
 		MapTask m = new MapTask(i,jid,f,j,"out" + i); 
 		tasks.add(m);
 	    }
 	    System.out.println("Finished Adding Map Tasks");
 	    System.out.println(tasks);
 	} catch(Exception e) {
 	    e.printStackTrace();
 	}
     }
 
     public void scheduleReducers(Job j) {
 	TreeMap<String, List<String>> sorted = new TreeMap<String, List<String>>(jobKvs.get(j.getJid()));
 	int i;
 	Task r;
 	int tid = 0;
 	int partitionSize = sorted.size() / j.getTotalReduces(); //TODO: better splitting of reduce tasks
 	Object[] objs = sorted.keySet().toArray();
 	String[] keys = Arrays.copyOf(objs,objs.length,String[].class);
 	for(i = 0; i < j.getTotalReduces() - 1; i = i + partitionSize) {
 	    r = new ReduceTask(tid,j.getJid(),sorted.subMap(keys[i],keys[i+partitionSize]),j,j.getJid() + "part" + tid); 
 	    tasks.add(r);
 	    tid++;
 	}
 	r = new ReduceTask(tid,j.getJid(),sorted.subMap(keys[i],keys[keys.length - 1]),j,j.getJid() + "part" + tid); 
 	tasks.add(r);
     }
 
     public void scheduleFinalReduce(Job j) {
 	FileInputStream in;
 	RandomAccessFile out;
 	String line;
 	byte[] b;
 	File f;
 	try{
 	    out = new RandomAccessFile(j.getOutput(),"rws");
 	    for(int i = 0; i < 2; i++) {
 		f = new File(j.getJid() + "part" + i);
 		in = new FileInputStream(f);
 	        b = new byte[(int)f.length()];
 		in.read(b);
 		out.write(b);
 		in.close();
 	    }
 	    out.close();
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
     }
 		
     public void finishedMap(Task t, HashMap<String, List<String>> partialKvs) throws RemoteException{
 	Job j = t.getJob();
 	putAll(partialKvs,j);
 	System.out.println(jobMapsDone);
 	System.out.println(j);
 	int maps = jobMapsDone.get(j.getJid()) + 1;
 	jobMapsDone.put(j.getJid(),maps); 
 	if (maps >= j.getTotalMaps()) {
 	    scheduleReducers(j); //TODO: start reducing as maps finish
 	    //jobMapsDone.remove(j.getJid());
 	}
     }
     public void finishedReduce(Task t) throws RemoteException{
 	Job j = t.getJob();
 	int reduces = jobReducesDone.get(j.getJid()) + 1;
 	jobReducesDone.put(j.getJid(),reduces);
 	if(reduces >= j.getTotalReduces()){
 	    scheduleFinalReduce(j);
 	    //jobReducesDone.remove(j.getJid());
 	}
     }
 
     public class Scheduler extends Thread {
 
 	public Scheduler() {
 	    System.out.println("Scheduler started");
 	}
 
 	public void run() {
 	    while(isRunning) {
 		try{
 		    while(nodeQueue.size() > 0 && tasks.size() > 0) {
 			System.out.println("Running");
 			if(nodeQueue.element().server.isFull()) {
 			    nodeQueue.add(nodeQueue.remove());
 			} else {
			    Node n = nodeQueue.element();
 			    System.out.println("Starting task on node " + n);
 			    Task t = tasks.remove();
 			    n.server.scheduleTask(t); //TODO: schedule based on node location
 			    System.out.println("Scheduled Task");
 			    nodeQueue.add(n);
 			}
 		    }
 		} catch(Exception e) {
 		    e.printStackTrace();
 		}
 	    }
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
             e.printStackTrace();
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
 
     private void addNode(String address)
     {
         InetAddress newAddress = null;
         try{
             newAddress = InetAddress.getByName(address);
         } catch(Exception e) {
             e.printStackTrace();
         }
         Node newNode = new Node();
         newNode.name = address;
         newNode.address = newAddress;
         newNode.server = null;
         newNode.isConnected = false;
         newNode.files = new ArrayList<FilePartition>();
         this.nodes.put(address, newNode);
     }
 
     // This allows the server to be reached by any nodes or users
     public void start() throws RemoteException
     {
         // Get the old registry, or create a new one
         try{
             rmiRegistry = LocateRegistry.getRegistry(registryPort);
             rmiRegistry.list();
             System.out.println("Registry server found");
         } catch (RemoteException e) {
             rmiRegistry = LocateRegistry.createRegistry(registryPort);
             System.out.println("Registry server created");
         }
     
         // Attempt to stop a previous instance of the server
         // TODO: This doesn't seem to work. 
         try{        
             MasterFileServerInterface oldServer = 
              (MasterFileServerInterface)rmiRegistry.lookup(masterServerRegistryKey);
             oldServer.stop();
             System.out.println("Stopped old server");
         } catch (NotBoundException|RemoteException e) {
             System.out.println("No old server found on old registry");
         }
 
         // Bind the new server
         try {
             rmiRegistry.bind(masterServerRegistryKey, this);
         } catch (Exception e) {
             e.printStackTrace();
         }
         System.out.println("Registry port is: " + registryPort);
         System.out.println("masterServerRegistryKey is: " + masterServerRegistryKey);
 	
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
 
     public void register(NodeFileServerInterface server, String address)
     {
         System.out.println("Client connected");
         Node foundNode = this.nodes.get(address);
         if (foundNode == null){
             System.out.println("Unrecognized client; ignoring");
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
 	    nodeQueue.add(foundNode);
         }
     }
    
     //Adds a new file to the distributed file system
     //if host is either null or 'this' it must be local to thismaster
     public void addNewFile(String filename, FileServerInterface host) throws RemoteException
     {
         //Distribute the file among nodes
         System.out.println("adding file " + filename + " from host " + host);
         //Download the file from the remote host
         File newFile = new File(filename);
         if ((host == null) || (host == this))
             System.out.println("File is already local");
         else {
             System.out.println("File " + filename + " is at remote host; downloading");
             try{
 
                 FileIO.download(host, newFile, newFile);
             } catch(IOException e) {
                 System.out.println("Failed to download");
                 e.printStackTrace();
             }
         }
         System.out.println("Downloaded " + filename + " from remote host");
         // Partition the files and send it to nodes
         this.partitionFile(newFile);
         System.out.println("file added");
 
         return;
     }
 
     private void partitionFile(File originalFile)
     {
         DistributedFile dfile = null;
         System.out.println("Distributifying file");
         try{
             dfile = new DistributedFile(originalFile);
             System.out.println("File is chunked");
             System.out.println("blocks: " + dfile.getBlocks());
             //Add nodes to all of the parts of dfile
             dfile = allocateFile(dfile);
             //Send relevant partitions to all nodes
             commit(dfile);
         } catch (Exception e) {
             System.out.println("Error");
             e.printStackTrace(System.out);
         }
         return;
     }
 
     private DistributedFile allocateFile(DistributedFile dfile)
     {
         ListIterator<FilePartition[]> iter = dfile.getBlocks().listIterator();
         Enumeration<Node> nodeEnum = this.nodes.elements();
         // TODO: THIS SHOULD BE DYNAMIC
         Node singleNode = nodeEnum.nextElement();
         while (iter.hasNext()) {
             FilePartition[] block = iter.next();
             // TODO: THIS SHOULD BE DYNAMIC
             System.out.println("File" + block[0].getFileName() + " location set");
             block[0].setLocation(singleNode);
             System.out.println("Location now" + block[0].getLocation());
         }
         return dfile;
     }
 
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
             s = s.concat("\n\tFiles are:\n");
             ListIterator<FilePartition> iter = each.files.listIterator();
             while (iter.hasNext()) {
                 FilePartition fp = iter.next();
                 s = s + "\n\t\t" + fp.getFileName() + " part " + fp.getIndex() 
                     + " size: " + fp.getSize() + "\n";
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
 
