 package failure.detector;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.LinkedHashMap;
 
 
 public class FailureDetectorThread extends Thread {
 	private int serverId;
 	private int serverPort;
 	private LinkedHashMap<Integer, Integer> peers;
 		
 	public FailureDetectorThread(int id, String failureSettingFile) throws NumberFormatException, IOException {
 	    // read server setting file
 	    // java.net.URL path = ClassLoader.getSystemResource(failureSettingFile);	
 	    FileReader fr = new FileReader(failureSettingFile);//path.getFile());
 	    BufferedReader br = new BufferedReader (fr);
 	    String line;

 	    peers = new LinkedHashMap<Integer, Integer>();
 	    while ((line = br.readLine()) != null){
 	        String[] serverSetting = line.split(",");
 	        int i = 0;
 	        if(Integer.parseInt(serverSetting[i]) == serverId){
 	            serverPort = Integer.parseInt(serverSetting[++i]);
 	            peers.put(Integer.parseInt(serverSetting[++i]), Integer.parseInt(serverSetting[++i]));
 	            peers.put(Integer.parseInt(serverSetting[++i]), Integer.parseInt(serverSetting[++i]));
 	            peers.put(Integer.parseInt(serverSetting[++i]), Integer.parseInt(serverSetting[++i]));
 	            break;
 	        }
 	    }
 	}
 	
 	public void run() {
 		ReceivePingThread receiveThread = new ReceivePingThread(serverId, serverPort, null);
 		SendPingThread sendThread = new SendPingThread(serverId, peers);
 		
 		receiveThread.start();
 		sendThread.start();
 		
 		try {
 			receiveThread.join();
 			sendThread.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 	}
 }
