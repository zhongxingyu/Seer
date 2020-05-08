 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.io.*;
 
 
 //Payload for sending Juice Action Payload to worker nodes
 public class JuiceAction extends GenericPayload implements Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 2L;
 	int                 juiceTaskId;
 	int                 machineId;
 	String              juiceExe;
 	ArrayList<String> 	juiceInputFileList;
 	String              juiceOutputFile; 
 	//@Override
 	/*public JuiceleAction(Machine machine) {
 		m = machine;
 	}*/
 	@Override
 	public void printContents()
 	{
 		System.out.println(juiceTaskId);
 	}
 	public void processJuiceActionPayload(Machine machine) {
 		HashMap<String, Process> temp = new HashMap<String, Process>();
 
 		for (String juiceInputFile : juiceInputFileList) {
 			temp.put(juiceInputFile, (Process)null);
 		}
 		synchronized (MapleJuiceListener.task_map) {
 			MapleJuiceListener.task_map.put(new Integer(juiceTaskId), new HashMap<String, Process>(temp));
 		}
 		//TODO : Mayur Get the exe and the files from SDFS
 		if (!machine.myFileList.contains(juiceExe)) {
 			machine.FileReplicator.sendSDFSGetMessage(juiceExe);
 		}
 		for (String juiceInputFile : juiceInputFileList) {
 			if (!machine.myFileList.contains(juiceInputFile)) {
 				machine.FileReplicator.sendSDFSGetMessage(juiceInputFile);
 			}
 			//machine.FileReplicator.sendSDFSGetMessage(fileInfo);
 		}
 		//TODO : Synchronization
 
 		int i =0;
 		HashMap<String, Process> processList = new HashMap<String, Process>();
		System.out.println("********************************* Starting Maple Task *********************************************");
 		for (String juiceInputFile : juiceInputFileList) {
 
 			Process juiceProcess;
 			try {
 				i++;
 				juiceProcess = Runtime.getRuntime().exec("java -jar " + juiceExe + " " + juiceInputFile + " " + juiceTaskId);
 				//MapleJuiceListener.task_map.get(juiceTaskId).remove(juiceInputFile);
 				synchronized (MapleJuiceListener.task_map) {
 					MapleJuiceListener.task_map.get(juiceTaskId).put(juiceInputFile, juiceProcess);
 				}
 				processList.put(juiceInputFile, juiceProcess);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			if ((i % 50) == 0) {
 				for (String juiceFile  : processList.keySet()) {
 					try {
 						Process process = processList.get(juiceFile);
 
 						process.waitFor();
 						int result = process.exitValue();
 						WriteLog.writelog(machine.myName, "Juice Task  " + juiceFile + "exited with code " + result);
 
 						if(result == 0) { //If process exited successfully
 
 						}else {
 							//Do nothing.
 						}
 
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				processList.clear();
 			}
 
 		}
 
 
 		//TODO : the task_map will contain the IDs of the juice task they executed. Now we don't need them. 
 		//Needs to be removed
 
 		int index = 0;
 		for (String juiceInputFile  : processList.keySet()) {
 			try {
 				Process process = processList.get(juiceInputFile);
 				index++;
 				process.waitFor();
 				int result = process.exitValue();
 				WriteLog.writelog(machine.myName, "Juice Task  " + juiceInputFile + "exited with code " + result);
 
 				if(result == 0) { //If process exited successfully
 
 				}else {
 					//Do nothing.
 				}
 
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}

 		String path = ".";
 		File folder = new File(path);
 		File[] listOfFiles = folder.listFiles();
 		Pattern pattern = Pattern.compile("juice_inter_" + juiceTaskId);
 
 		for(File file : listOfFiles) {
 			Matcher matcher = pattern.matcher(file.getName());						
 			if(matcher.find()) {
 
 				try {
 					WriteLog.writelog(machine.myName, "Sending PUT msg for file " + file.getName());
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				machine.FileReplicator.sendSDFSPutMessage(file.getName(), juiceOutputFile, true);
 				matcher.reset();
 				try {
 					Thread.sleep(50);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				continue;
 			}
 		}
 		/*File directory = new File("./");  
 
 		File[] toBeDeleted = directory.listFiles(new FileFilter() {  
 			public boolean accept(File theFile) {  
 				if (theFile.isFile()) {  
 					return theFile.getName().startsWith("juice_inter"); 
 				}  
 				return false;  
 			}  
 		});  
 
 		System.out.println(Arrays.toString(toBeDeleted));  
 		for(File deletableFile:toBeDeleted){  
 			deletableFile.delete();  
		}  
 		synchronized (MapleJuiceListener.task_map) {
 			MapleJuiceListener.task_map.remove(juiceTaskId);
		}*/
 
 	}
 }
