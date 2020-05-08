 package MapReduceObjects;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 import Config.Configuration;
 
 public class MapReduce440 {
 	
 	private boolean isRunning = true;
 	private int port;
 	private int backlog;
 	private int heartbeatPort;
 	private int heartbeatBacklog;
 	private MapReduceListener mrl = null;
 
 	public MapReduce440(int port, int backlog, int heartbeatPort, int heartbeatBacklog) {
 		this.port = port;
 		this.backlog = backlog;
 		this.heartbeatPort = heartbeatPort;
 		this.heartbeatBacklog = heartbeatBacklog;
 	}
 	/** receiveCommands()
 	 * 
 	 * Runs the command prompt
 	 * @throws Exception
 	 */
 	public void receiveCommands() throws Exception {
 		String result = "";
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		mrl = new MapReduceListener(port, backlog, heartbeatPort, heartbeatBacklog, this);
 		mrl.start();
 		
 		while(isRunning) {
 			System.out.print("==> ");
 			result = br.readLine();
 			parseCommand(result);
 		}
 	}
 	
 	public void parseCommand(String command) {
 		//Split command based on space
 		String[] words = command.split(" ");
 
 		//First word is the process/command
 		String com = words[0];
 
 		//Remaining words are process arguments
 		String[] args = new String[words.length - 1];
 
 		for (int i = 1; i < words.length; i++) {
 			args[i-1] = words[i];
 		}
 
 		if (com.equals("start") && args.length == 1) {
 			//First connect to master, then let them know what's up.
 			String configPath = args[0];
 			JobRunner440 jr = new JobRunner440(configPath);
 			Configuration config = jr.getConfig();
 			String[] masterInfo = config.getMasterLocation().split(":");
 			String host = masterInfo[0];
 			int port = Integer.parseInt(masterInfo[1]);
 			Socket jobInfo = null;
 			ObjectOutputStream details = null;
 			try {
 				jobInfo = new Socket(host, port);
 				details = new ObjectOutputStream(jobInfo.getOutputStream());
 				details.writeObject("master");
 				details.writeObject(configPath);
 				details.close();
 				jobInfo.close();
 			} catch (UnknownHostException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else if (com.equals("monitor") && words.length == 1) {
 			if (mrl != null) mrl.printJobs();
 		} else if (com.equals("stop") && words.length == 1) {
			//STOP CODE
 		} else {
 			System.out.println("Command not " + com + " recognized.");
 		}
 	}
 	
 	public void jobFinished(String jobName) {
 		System.out.println("Job " + jobName + " finished.");
 	}
 }
