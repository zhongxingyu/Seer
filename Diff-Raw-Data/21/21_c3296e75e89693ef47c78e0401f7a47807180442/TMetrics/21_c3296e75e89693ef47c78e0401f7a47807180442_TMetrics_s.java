 package com.github.dreadslicer.tekkitrestrict;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class TMetrics {
 	public TMetrics(final JavaPlugin plugin, boolean showWarnings){
 		this.plugin = plugin;
 		this.showWarnings = showWarnings;
 	}
 
	private static final String BASE_URL = "http://localhost/";
 	private static final String REPORT_URL = "tekkitrestrict.php";
 	private static final int PING_INTERVAL = 15;
	public boolean debug = true;
 
 	public int uid = 0;
 	private int savedId = 0;
 	private boolean first = true;
 	private boolean logged = false;
 	private final JavaPlugin plugin;
	private int taskId = 0;
 	private boolean showWarnings;
 
 	public boolean start(){
 		if (!tekkitrestrict.useTMetrics){
 			stop();
 			return false;
 		}
 		
 		if (taskId >= 0) {
             return true;
         }
 		taskId = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
             public void run() {
                 try {
                     postPlugin();
                 } catch (TMetricsException e) {
                     if (showWarnings || debug) {
                         Bukkit.getLogger().warning("[TMetrics] " + e.getMessage());
                     }
                 }
             }
         }, 0, PING_INTERVAL * 1200);
 
         return true;
 	}
 	
 	public void stop(){
 		if (taskId > 0) {
             plugin.getServer().getScheduler().cancelTask(taskId);
             taskId = -1;
         }
 	}
 
 	private void postPlugin() throws TMetricsException {
 		if (plugin == null) throw new TMetricsException("The developer forgot to initiate the metrics. Please report this bug if you see this message.");
 		File file = new File(plugin.getDataFolder()+File.separator+"uid");
 		if (uid == 0){
 			if (file.exists()) savedId = uid = readUIDFile(file);
 		}
 
 		int playersOnline = Bukkit.getServer().getOnlinePlayers().length;
 		String params = "";
 		if (uid == 0 || first){
 			PluginDescriptionFile description = plugin.getDescription();
 			int onlineMode = Bukkit.getServer().getOnlineMode() ? 1 : 0;
 			String pluginVersion = description.getVersion();
 			String serverVersion = Bukkit.getVersion();
 	
 			String osname = System.getProperty("os.name");
 			String osarch = System.getProperty("os.arch");
 			String osversion = System.getProperty("os.version");
 			String java_version = System.getProperty("java.version");
 			int coreCount = Runtime.getRuntime().availableProcessors();
 			long memory = Runtime.getRuntime().maxMemory();
 			if (memory == Long.MAX_VALUE) memory = Runtime.getRuntime().totalMemory();
 			memory = Math.round(memory/(1024*1024));
 	
 			// normalize os arch .. amd64 -> x86_64
 			if (osarch.equals("amd64")) {
 				osarch = "x86_64";
 			}
 			int arch = 0;
 			if (osarch.equals("x86_64")) arch = 1;
 	
 			params =  "id="+uid+"&"
 					+ "pver="+pluginVersion+"&"
 					+ "sver="+serverVersion+"&"
 					+ "online="+onlineMode+"&"
 					+ "players="+playersOnline+"&"
 					+ "osname="+osname+"&"
 					+ "osarch="+arch+"&"
 					+ "osver="+osversion+"&"
 					+ "jver="+java_version+"&"
 					+ "cores="+coreCount+"&"
 					+ "memory="+memory;
 		} else {
 			params =  "id="+uid+"&"
 					+ "players="+playersOnline;
 		}
 		//String request = "http://metrics.taico.nl/tekkitrestrict.php";
 		String request = BASE_URL + REPORT_URL;
 		if (first){
 			request += "?first=1";
 			first = false;
 		}
 		if (debug){
 			Bukkit.getLogger().info("[TMetrics] Prepared request for URL: "+request);
 			Bukkit.getLogger().info("[TMetrics] Prepered params: "+params);
 		}
 		URL url; 
 		HttpURLConnection connection = null;
 		try {
 			url = new URL(request);
 			connection = (HttpURLConnection) url.openConnection();           
 			connection.setDoOutput(true);
 			connection.setDoInput(true);
 			connection.setInstanceFollowRedirects(false); 
 			connection.setRequestMethod("POST");
 			connection.setRequestProperty("User-Agent", "TMetrics/1");
 			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
 			connection.setRequestProperty("charset", "utf-8");
 			connection.setRequestProperty("Content-Length", "" + Integer.toString(params.getBytes().length));
 			connection.setUseCaches(false);
 
 			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
 			wr.writeBytes(params);
 			wr.flush();
 			wr.close();
 
 			int responseCode = connection.getResponseCode();
 
 			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 			ArrayList<String> response = new ArrayList<String>();
 			String inputLine;
 			while ((inputLine = in.readLine()) != null) {
 				response.add(inputLine);
 			}
 
 			in.close();
 			connection.disconnect();
 
 			if (responseCode != 200){
 				throw new TMetricsException("Unable to send statistics! Response code: "+responseCode);
 			} else if (response.size() == 0){
 				throw new TMetricsException("An error occured while trying to send statistics: No response from server.");
 			}
 
 			for (String s : response){
 				if (s == null || s.equals("")) continue;
 				if (s.contains("id=")){
 					try {
 						uid = Integer.parseInt(s.replace("id=", ""));
 					} catch (NumberFormatException ex) {
 						if (showWarnings || debug) Bukkit.getLogger().warning("[TMetrics] Invalid response from server!");
 					}
 				} else if (!s.contains("OK:")){
 					if (showWarnings || debug) Bukkit.getLogger().warning("[TMetrics] Statistics server responds: " + s);
 				} else if (debug && s.contains("OK: ")) {
 					Bukkit.getLogger().info("[TMetrics] Server responds: "+s);
 				}
 			}
 
 			if (savedId != uid || savedId == 0) generateUIDFile(file);
 		} catch (Exception ex){
 			if (connection != null){
 				connection.disconnect();
 			}
 			if (!logged){
 				logged = true;
 				if (debug) ex.printStackTrace();
 				throw new TMetricsException("An error occured while trying to send statistics: "+ex.getMessage()+"\n"+"This error will only be logged once.");
 			}
 		}
 	}
 
 	private void generateUIDFile(File file) {
 		Writer writer = null;
 
 		try {
 			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
 			writer.write(""+uid);
 			writer.close();
 		} catch (IOException ex){
 			if (showWarnings) Bukkit.getLogger().warning("[TMetrics] Unable to write UID to file!");
 		} finally {
 			try {if (writer != null) writer.close();} catch (IOException ex) {}
 		}
 	}
 
 	private int readUIDFile(File file){
 		BufferedReader input = null;
 		try {
 			input = new BufferedReader(new FileReader(file));
 		} catch (FileNotFoundException e2) {
 			return 0;
 		}
 
 		String line = "0";
 		try {
 			line = input.readLine();
 		} catch (IOException ex) {
 			if (showWarnings) Bukkit.getLogger().warning("[TMetrics] The UID file for TekkitRestrict cannot be read!");
 		}
 
 		try {
 			input.close();
 		} catch (IOException e) {
 			if (showWarnings) Bukkit.getLogger().warning("[TMetrics] Unknown error occured when reading UID for TekkitRestrict!");
 		}
 
 		try {
 			return Integer.parseInt(line);
 		} catch (NumberFormatException ex){
 			if (showWarnings) Bukkit.getLogger().warning("[TMetrics] The UID file for TekkitRestrict is malformed!");
 		}
 
 		return 0;
 	}
 }
