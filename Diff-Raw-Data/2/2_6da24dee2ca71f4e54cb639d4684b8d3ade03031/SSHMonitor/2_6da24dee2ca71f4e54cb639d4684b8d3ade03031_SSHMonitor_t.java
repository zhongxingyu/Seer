 package aic.monitor;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 
 public class SSHMonitor {
 	private String username;
 	private String ip;
 	private Process process;
 	private BufferedReader input;
 	private OutputStreamWriter output;
 
 	public SSHMonitor(String username, String ip) throws IOException {
 		super();
 		this.username = username;
 		this.ip = ip;
 		openConnection();
 	}
 
 	public void openConnection() throws IOException {
 		if (process == null) {
 			ProcessBuilder pb = new ProcessBuilder("ssh", username + "@" + ip);
 			process = pb.start();
 			input = new BufferedReader(new InputStreamReader(
 					process.getInputStream()));
 			output = new OutputStreamWriter(process.getOutputStream());
 			skipOutput();
 		}
 	}
 	
 	public void skipOutput() throws IOException{
 		input.read(new char[1024]);
 	}
 
 	public void closeConnection() throws IOException {
 		if (process != null) {
 			executeCommand("exit");
 			output.close();
 			input.close();
 			process.destroy();
 			process = null;
 		}
 	}
 	
 	public BufferedReader executeCommand(String cmd) throws IOException{
 		//read everything before starting a new command
 		while(input.ready()){
 			skipOutput();
 		}
 		output.write(cmd + "\n");
 		output.flush();
 		return input;
 	}
 
 	public long getFreeMemory() throws IOException {
 		BufferedReader input = executeCommand("free -b | grep 'Mem:'");
 		return Long.parseLong(input.readLine().split("\\s+")[3]);
 	}
 
 	public float getLoadAvg() throws IOException {
 		BufferedReader input = executeCommand("cat /proc/loadavg");
 		String loadString = input.readLine();
 		//The load string looks something like this: "0.79 0.83 0.95 1/600 25019"
 		//We will take the second value from this list of floats (which is the load average from the last few minutes)
		String[] tokens = loadString.split("\\s+");
 		return Float.parseFloat(tokens[1]);
 	}
 
 	public float getCpuUsage() throws IOException {
 		// to install mpstat: sudo apt-get install sysstat
 		BufferedReader input = executeCommand("mpstat 1 1 | grep Average:");
 		return 100 - Float.parseFloat(input.readLine().split("\\s+")[10]);
 	}
 
 	public void restartMongoDb() throws IOException {
 		// to install mpstat: sudo apt-get install sysstat
 		executeCommand("sudo /etc/init.d/mongodb restart");
 		skipOutput();
 	}
 
 	public static void main(String[] args) throws NumberFormatException,
 			IOException {
 		SSHMonitor m = new SSHMonitor("ubuntu", "10.99.0.98");
 		System.out.println(m.getLoadAvg());
 //		System.out.println(m.getFreeMemory());
 //		System.out.println(m.getFreeMemory());
 //		System.out.println(m.getFreeMemory());
 		System.out.println(m.getCpuUsage());
 		m.closeConnection();
 	}
 }
