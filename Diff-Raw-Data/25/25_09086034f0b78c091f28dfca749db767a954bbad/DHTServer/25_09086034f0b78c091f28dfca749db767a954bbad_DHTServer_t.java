 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.rmi.Naming;
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.util.Hashtable;
 
 import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;
 
 import distributed.hash.table.DistributedHashTable;
 
 public class DHTServer {
 	
     private static int mServerCount;
 	private static int[] sPortMap;
 	private static int[] sPeertMap;
 	private static int serverId;
 	public static void main(String[] args) {
 
 	    GetOpt getopt = new GetOpt(args, "i:");
 		serverId = -1;
 		try {
 			int c;
 			while ((c = getopt.getNextOption()) != -1) {
 			    switch(c) {
 			    case 'i':
 			    	serverId = Integer.parseInt(getopt.getOptionArg());
 			        break;
 			    }
 			}
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		
 		try{
 			java.net.URL path = ClassLoader.getSystemResource("serverSetting.txt");	
 			FileReader fr = new FileReader (path.getFile());
 	        BufferedReader br = new BufferedReader (fr);
 	        String line;
 	        try {
 				String[] portMap = br.readLine().split(",");
 				mServerCount = portMap.length;
 				sPortMap = new int[mServerCount];
 				for(int i = 0; i < mServerCount; i++){
 					sPortMap[i] = Integer.parseInt(portMap[i]);
 				}
 				while ((line = br.readLine()) != null){
 					String[] machineIds = line.split(",");				
 					if(Integer.parseInt(machineIds[0]) == serverId){
 						int idCount = machineIds.length;
 						sPeertMap = new int[idCount - 1];
 						for(int i = 0; i < idCount-1; i++){
 							sPeertMap[i] = Integer.parseInt(machineIds[i+1]);
 						}
 						break;
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 				System.exit(-1);
 			}
 		} catch (FileNotFoundException e2) {
 			e2.printStackTrace();
 			System.exit(-1);
 		}
 		
 		if (System.getSecurityManager() == null) {
             System.setSecurityManager(new RMISecurityManager());
             System.out.println("Security manager installed.");
         } else {
             System.out.println("Security manager already exists.");
         }
 		int serverPort = sPortMap[serverId-1];
 		try { //special exception handler for registry creation
 			LocateRegistry.createRegistry(serverPort); 
 			System.out.println("java RMI registry created on port: " + serverPort);
 		} catch (RemoteException e) {
 		    //do nothing, error means registry already exists
 			System.out.println("java RMI registry already exists.");
 		}
 		Hashtable<Integer, Integer> fingerTable = new Hashtable<Integer, Integer>();
 		for(int i = 0; i < sPeertMap.length; i++)
 			fingerTable.put(sPeertMap[i], sPortMap[(sPeertMap[i]-1) % mServerCount]);
 		try{
 			DistributedHashTable dhtServer = new DistributedHashTable(serverId, fingerTable);
 			Naming.rebind("//localhost:"+serverPort+"/DistributedHashTable", dhtServer);
             System.out.println("Distributed Hash server on machine: " + serverId + " is running.");
 		}catch(Exception e){
 			System.out.println("dhtServer: " + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 }
