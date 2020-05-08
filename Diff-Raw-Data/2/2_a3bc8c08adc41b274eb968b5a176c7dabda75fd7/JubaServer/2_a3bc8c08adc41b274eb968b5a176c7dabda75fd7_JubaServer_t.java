 package us.jubat.testutil;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 public enum JubaServer {
 	classifier, recommender, regression, stat, graph, anomaly;
 
 	public static final int BASEPORT = Integer.parseInt(System
 			.getProperty("jubatus.baseport"));
 
 	private Process process;
 	private StdoutReader stdout_reader;
 
 	public void start(String config) throws IOException, InterruptedException {
 		String[] command = new String[] { //
 		this.getCommandName(), //
 				"--rpc-port", String.valueOf(this.getPort()), //
 				"--configpath", config, //
 				"--thread", "100", //
 				"--tmpdir", "." //
 		};
 
 		ProcessBuilder pb = new ProcessBuilder(command);
 		pb.redirectErrorStream(true);
 		process = pb.start();
 		stdout_reader = new StdoutReader(process.getInputStream());
 		stdout_reader.setDaemon(true);
 		stdout_reader.start();
 
 		// sleep　1 second.
 		Thread.sleep(1000);
 	}
 
 	public void stop() throws IOException, InterruptedException {
 		stdout_reader.dump();
 		process.getInputStream().close();
 		process.getErrorStream().close();
 		process.getOutputStream().close();
 		process.destroy();
 		process.waitFor();
 	}
 
 	private class StdoutReader extends Thread {
 		private BufferedReader br;
 		private List<String> stdout = new ArrayList<String>();
 
 		public StdoutReader(InputStream is) {
 			br = new BufferedReader(new InputStreamReader(is));
 		}
 
 		@Override
 		public void run() {
 			try {
 				try {
 					String line;
 					while ((line = br.readLine()) != null) {
 						stdout.add(line);
 					}
 				} finally {
 					br.close();
 				}
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		// for debug
 		public void dump() {
 			for (String line : stdout) {
 				System.out.println(line);
 			}
 		}
 	}
 
 	public String getCommandName() {
 		return "juba" + this.name();
 	}
 
 	public String getHost() {
 		return "127.0.0.1";
 	}
 
 	public int getPort() {
 		return BASEPORT + this.ordinal();
 	}
 
 	private String getConfigFileName() {
		return "/config_" + this.name() + ".json";
 	}
 
 	public String getConfigPath() {
 		return getClass().getResource(getConfigFileName()).getPath();
 	}
 
 	public String getConfigData() throws IOException {
 		BufferedReader br = new BufferedReader(new InputStreamReader(getClass()
 				.getResourceAsStream(getConfigFileName())));
 		StringBuilder sb = new StringBuilder();
 
 		String line;
 		while ((line = br.readLine()) != null) {
 			sb.append(line);
 			sb.append('\n');
 		}
 		br.close();
 
 		return sb.toString();
 	}
 }
