 package watch;
 
 import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
 import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
 import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.file.WatchEvent;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * Class used for notification services
  */
 public class MyFSWatcher {
 
 	// ArrayList of NFS mountpoints
 	private HashMap<String,String> mounted;
 	private LocalWatcher lw;
 	private DistributedWatcher dw;
 
 	/**
 	 * Default Subscriber Constructor
 	 */
 	public MyFSWatcher() {
 		mounted = new HashMap<String,String>();
 		lw = null;
 		dw = null;
 		setMountPoints();
 	}
 
 	/**
 	 * Populates mountedDirectories array with mount point locations
 	 */
 	private void setMountPoints() {
 
 		String os = System.getProperty("os.name").toLowerCase();
 		String searchString = null;
 
 		if (os.contains("mac")) {
 			searchString = "(nfs";
 		} else if ((os.contains("nix") || (os.contains("linux")))) {
 			searchString = "type nfs";
 		} else {
 			System.err.println("ERROR: Unknown OS");
 			return;
 		}
 
 		String line;
 		try {
 			Runtime r = Runtime.getRuntime();
 			Process p = r.exec("mount");
 			p.waitFor();
 			BufferedReader b = new BufferedReader(new InputStreamReader(p
 					.getInputStream()));
 			ArrayList<String> mount_info = new ArrayList<String>();
 
 			while ((line = b.readLine()) != null) {
 				mount_info.add(line);
 			}
 
 			for (int i = 0; i < mount_info.size(); i++) {
 				if (mount_info.get(i).contains(searchString)) {
 					ArrayList<String> splitLine = new ArrayList<String>(Arrays
 							.asList(mount_info.get(i).split(" ")));
 					
 					String hostName = splitLine.get(0).split(":")[0];
 					System.out.println("Hostname: "+hostName);
 					
 					int indexOfPath = splitLine.indexOf("on");
 					String mountPoint = splitLine.get(++indexOfPath);
 					mounted.put(mountPoint, hostName);
 				}
 			}
 		} catch (Exception e) {
 			System.out
 					.println("Error running `mount` command; Only Linux and OSX are currently supported");
 			System.exit(-1);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param dirName
 	 * @return 0 if local file is registered; 1 if distributed file is
 	 *         registered Doesn't actually return though
 	 */
 	public int subscribe(String dirName, WatchEvent.Kind<?>... subscriptionTypes) {
 		boolean durable = false;
 		
 		Iterator<Map.Entry<String, String>> iter = mounted.entrySet().iterator();
 		String tempdir;
 		String temphost;
 		
 		while (iter.hasNext()) {
 	        Map.Entry pairs = iter.next();
 	        if(dirName.startsWith((String)pairs.getKey())){
 	        	tempdir = (String)pairs.getKey();
 	        	temphost = (String)pairs.getValue();
 	        	System.out.println("NFS subscription for: "+pairs.getValue());
	        	nfssubscribe(temphost, dirName, durable, subscriptionTypes);
 	        	return 1;
 	        }
 	    }
 
 		System.out.println("Local Directory Subscription for: " + dirName);
 		localsubscribe(dirName, durable, subscriptionTypes);
 		return 0;
 	}
 
 	/*
 	 * Method for subscribing to a directory that is distributed
 	 */
 	private int nfssubscribe(String hostName, String dirName, boolean durable,
 			WatchEvent.Kind<?>... subscriptionTypes) {
 		if (dw == null) {
 			try {
 				dw = new DistributedWatcher(durable);
 			} catch (IOException e) {
 				e.printStackTrace();
 				return -1;
 			}
 		}
 		try {
 			dw.register(dirName, subscriptionTypes);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return -1;
 		}
 		return 0;
 	}
 
 	/*
 	 * Method for subscribing to a directory that is local
 	 */
 	private int localsubscribe(String dirName, boolean durable,
 			WatchEvent.Kind<?>... subscriptionTypes) {
 		if (this.lw == null) {
 			try {
 				this.lw = new LocalWatcher();
 				System.out.println("Created local watcher");
 			} catch (IOException e) {
 				e.printStackTrace();
 				return -1;
 			}
 		}
 		try {
 			this.lw.register(dirName);
 			System.out.println("Finished registering: " + dirName);
 		} catch (IOException e) {
 			System.err.println("Could not register; " + dirName);
 			return -1;
 		}
 		return 0;
 	}
 
 	public ArrayList<SerializableFileEvent> pollEvent()
 			throws InterruptedException {
 		ArrayList<SerializableFileEvent> temp;
 		ArrayList<SerializableFileEvent> messages = new ArrayList<SerializableFileEvent>();
 
 		if (dw != null) {
 			temp = dw.pollEvents();
 			if (temp != null) {
 				messages.addAll(temp);
 			}
 			
 		}
 		if (lw != null) {
 			temp = lw.pollEvents();
 			if (temp != null) {
 				messages.addAll(temp);
 			}
 		}
 
 		return messages;
 	}
 }
