 import java.io.IOException;
 import java.util.*;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 
 public class MapleJuiceListener implements Runnable {
 
 	private Machine m;
 	public static int task_id;
 	public static HashMap<Integer , HashMap<String, Process>> task_map; 
 	public void start()
 	{
 		Thread server_thread = new Thread(this);
 		
 		server_thread.start();
 	}
 	public MapleJuiceListener(Machine machine) {
 		task_map = new HashMap<Integer, HashMap<String, Process>>();
 		m = machine;
 		task_id = 0;
 	}
 	@SuppressWarnings("unchecked")
 	public void processMapleCommand(String mapleExe, Vector<String> filesToProcess, String outputFilePrefix) {
 		@SuppressWarnings("rawtypes")		
 		ArrayList[] nodeFileList = new ArrayList[m.memberList.size()];
 		for (int i = 0 ; i < m.memberList.size(); i++) {
 			nodeFileList[i] = new ArrayList<String>();
 
 		}
 		int i = 0;
 		task_id++;
 		for (String fileName : filesToProcess) {
 			nodeFileList[i].add(fileName);
 			i = (i + 1) % m.memberList.size();
 
 		}
 		for (int j = 0 ; ((j < m.memberList.size()) && (nodeFileList[j].size()>0)); j++) {
 			MapleAction temp = new MapleAction();
 			temp.mapleTaskId = task_id;
 			temp.machineId = j + 1;
 			temp.mapleExe = mapleExe;
 			temp.inputFileInfo = nodeFileList[j];
 			temp.outputFilePrefix = outputFilePrefix;
 			MapleJuicePayload mj_payload = new MapleJuicePayload("MapleTask");
 			mj_payload.setByteArray(temp);
 			mj_payload.sendMapleJuicePacket(m.memberList.elementAt(j), false);
 		}
 		
 		TaskStatus status = new TaskStatus();
 		status.taskId = task_id;
 		status.messageType = new String("get");
 		//Monitor the progress on the node every 10 seconds
 		for (int j = 0 ; ((j < m.memberList.size()) && (nodeFileList[j].size()>0)); j++) {
 			 
 			MapleJuicePayload mj_payload = new MapleJuicePayload("TaskStatus");
 			
 			mj_payload.setByteArray(status);
 			Socket sendSocket = mj_payload.sendMapleJuicePacket(m.memberList.elementAt(j), true);
 			mj_payload.receiveMapleJuicePacket(sendSocket);
 			try {
 				Thread.sleep(10 * 1000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		
 
 
	}
 
 
 	public void processJuiceCommand(String sdfsExe, int num_reducers, String outputsdfsFileName, String outputFilePrefix) {
 	}
 	
 	
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		ServerSocket servsock = null;
 		try {
 			servsock = new ServerSocket(Machine.MAPLE_JUICE_PORT);
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		while(true){
 
 			try{
 
 
 				System.out.println("Waiting...");
 
 				Socket sock = servsock.accept();
 				System.out.println("Accepted connection : " + sock);
 
 
 
 				MapleJuiceThread mj_thread = new MapleJuiceThread(sock, m);
 				mj_thread.start();
 				// sendfile
 
 
 
 			}catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 
 	}
 
 }
