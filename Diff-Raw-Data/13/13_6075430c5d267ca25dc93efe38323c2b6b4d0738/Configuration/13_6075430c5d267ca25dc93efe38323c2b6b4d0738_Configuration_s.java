 package net.sayon.dovor;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Date;
 import java.util.Properties;
 import java.util.Scanner;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 public class Configuration {
 	private static final String VERSION = "Junthor";
 	private static final String CLIENT = "Dovor";
 	private static final long KEEP_ALIVE_INTERVAL = 120;
 	private static final Logger log;
 	private String configFile;
 	private Properties prop;
 	private String torchatId;
 
 	static {
 		log = Logger.getLogger(Configuration.class.getName());
 		log.setParent(Logger.getLogger(Dovor.class.getName()));
 		log.setLevel(Logger.getLogger(Dovor.class.getName()).getLevel());
 	}
 
 	public Configuration() {
 		this.prop = new Properties();
 	}
 
 	public void load(InputStream is) throws IOException {
 		prop.load(is);
 	}
 
 	public void load(String string) throws IOException {
 		load(new FileInputStream(string)); // TODO create if not exist
 		this.configFile = string;
 	}
 
 	/**
 	 * Writes config to the file config was last loaded from
 	 * 
 	 * @throws IOException
 	 */
 	public void save() throws IOException {
 		save(configFile);
 	}
 
 	/**
 	 * Writes config to the file specified in output
 	 * 
 	 * @param location
 	 *            the file to write to
 	 * @throws IOException
 	 */
 	public void save(String location) throws IOException {
 		save(new FileOutputStream(location));
 	}
 
 	/**
 	 * Writes config to output and closes stream
 	 * 
 	 * Equivalent to save(os, true)
 	 * 
 	 * @param os
 	 * @throws IOException
 	 */
 	public void save(OutputStream os) throws IOException {
 		save(os, true);
 	}
 
 	/**
 	 * Writes config to output
 	 * 
 	 * @param os
 	 * @param close
 	 *            if true the stream is closed after write
 	 * @throws IOException
 	 */
 	public void save(OutputStream os, boolean close) throws IOException {
 		prop.store(os, new Date().toString());
 		os.flush();
 	}
 
 	public String getBuddyListLocation() {
 		return (String) prop.get("buddy_list");
 	}
 
 	public Properties getProperties() {
 		return prop;
 	}
 
 	public String getVersion() {
 		return VERSION;
 	}
 
 	public String getClient() {
 		return CLIENT;
 	}
 
 	public String getProfileName() {
 		return (String) prop.get("profile_name");
 	}
 
 	public String getProfileText() {
 		return (String) prop.get("profile_text");
 	}
 
 	public int getSocksPort() {
 		return Integer.parseInt(prop.getProperty("socks_port"));
 	}
 
 	public int getLocalPort() {
 		return Integer.parseInt(prop.getProperty("local_port"));
 	}
 
 	public long getKeepAliveInterval() {
 		return KEEP_ALIVE_INTERVAL * 1000; // TODO make configurable
 	}
 
 	public void loadTorId() {
 		String hiddenService = prop.getProperty("hidden_service").replaceAll("\\\\", "/");
 		if (hiddenService.contains("$")) {
 			for (String s : hiddenService.split("/")) {
 				if (s.startsWith("$")) {
 					hiddenService = hiddenService.replaceAll(Pattern.quote(s), System.getenv(s.substring(1)));
 				}
 			}
 		}
 		if (hiddenService.length() == 16 && !hiddenService.contains("/")
 				&& !hiddenService.contains("\\")) { // this var likely directly contains the torid
 			this.torchatId = hiddenService;
 		} else if (hiddenService.length() == 22 && !hiddenService.contains("/")
 				&& !hiddenService.contains("\\")) { // this var likely directly contains the torid with .onion appended
 			this.torchatId = hiddenService.substring(0, 16);
 		} else {
 			File f = new File(hiddenService);
 			if (f.isDirectory()) { // we assume this is the folder containing hostname
 				f = new File(hiddenService, "hostname");
 			}
 			if (f.isFile()) { // we assume that this is hostname
 				try {
 					this.torchatId = new Scanner(f).nextLine().substring(0, 16);
 				} catch (FileNotFoundException e) {
 					this.torchatId = null;
 				}
 			}
 		}
 
 		if (this.torchatId == null) {
 			log.severe("Could not acquire torchatId.");
			throw new RuntimeException("Could not acquire torchatId,.");
 		}
 	}
 
 	public String getTorchatId() {
 		return torchatId;
 	}
 
 }
