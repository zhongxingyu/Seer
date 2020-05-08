 package chronos;
 
 import java.awt.Color;
 
 /**
  * Global variables are stored here
  * http://en.wikipedia.org/wiki/Singleton_pattern
  */
 public class Singleton {
 	/**
 	 * Other constants
 	 */
 	public final static String APP_NAME = "Chronos - FUCK YEAH";
 	public final static Color LIGHT_RED = new Color(0xFF8F95);
 	public final static Color LIGHT_GREEN = new Color(0xCBFF7C);
 	public final static Color BACKGROUND = new Color(0xFFFFFF);
	public final static Color[] COLORARRAY = { Color.pink, new Color(0xFAFA9E), new Color(0xCCFF99), new Color(0x66FFB3), new Color(0x99FFFF), new Color(0x99DDFF), new Color(0xCCCCFF), new Color(0xFF99FF) };
	public final static Color HOVER_COLOR = Color.red;
 
 	/**
 	 * Singleton variables
 	 */
 	private static Singleton instance;
 
 	private boolean logEnabled;
 	private int port = 25000;
 	private String hostname = "localhost";
 	private String username;
 	private boolean networkEnabled = false;
 	private boolean loginEnabled = false;
 
 	// Current logged in user
 	private Person person;
 
 	private Singleton() {
 	}
 
 	public static Singleton getInstance() {
 		if (instance == null)
 			instance = new Singleton();
 		return instance;
 	}
 
 	public static void log(String str) {
 		if (Singleton.getInstance().logEnabled())
 			System.out.println(str);
 	}
 
 	public void enableLog() {
 		this.logEnabled = true;
 	}
 
 	public void disableLog() {
 		this.logEnabled = false;
 	}
 
 	public boolean logEnabled() {
 		return logEnabled;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 	public String getHostname() {
 		return hostname;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsersname(String username) {
 		this.username = username;
 	}
 
 	public void enableNetwork() {
 		networkEnabled = true;
 	}
 
 	public boolean networkEnabled() {
 		return networkEnabled;
 	}
 
 	public void enableLogin() {
 		loginEnabled = true;
 	}
 
 	public boolean loginEnabled() {
 		return loginEnabled;
 	}
 
 	public void setSelf(Person person) {
 		this.person = person;
 	}
 
 	public Person getSelf() {
 		return person;
 	}
 }
