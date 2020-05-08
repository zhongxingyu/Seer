 
 import java.io.OutputStream;
 
 
 /**
  * @author E. Javier Figueroa 
  * COP5615 Spring 2011 
  * University of Florida
  * 
  */
 public class Exec implements Runnable {
 	private String host;
 	private String server;
 	private int port;
 	private int id;
 
 	public int getId() {
 		return id;
 	}
 
 	public Exec(String host, String server, int port, int id) {
 		this.host = host;
 		this.server = server;
 		this.port = port;
 		this.id = id;
 	}
 
 	@Override
 	public void run() {
 		String path = System.getProperty("user.dir");
 		Log.write("Starting Remote Process: Client ID: " + this.id + " on "
 				+ this.host);
 		String[] command = {
				"ssh",
				"-i",
				"id_rsa",
				"figueroa@" + this.host,
				"~/.ssh/id_rsa",
//				"javier.figueroa@" + this.host,
				"cd " + path + " ; java start "
//				"cd " + path + " ; java -jar start.jar "
 			+ this.server + " "	+ this.port + " " + this.id };
 
 		
 		try {
 			Runtime rt = Runtime.getRuntime();
 			Process pr = rt.exec(command);
 
 			Log.write("Status: SUCCESS! Parameters Passed: " + this.server	+ " " + this.port + " " + this.id);
 
 			OutputStream stdin = pr.getOutputStream();
 			String carriageReturn = "\n";
 			stdin.write(carriageReturn.getBytes());
 			stdin.flush();
 			stdin.close();
 
 			pr.waitFor();
 			int exitValue = pr.exitValue();
 			if (exitValue != 0) {
 				Server.decreaseWorkers();
 			}
 		} catch (Exception e) {
 			Log.write("Warning: Remote Process could not start for Client ID: "	+ this.id + ", host: " + this.host);
 			Server.decreaseWorkers();
 			e.printStackTrace();
 		} 
 	}
 }
