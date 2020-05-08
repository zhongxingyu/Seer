 package nl.groep5.xchange;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 import nl.groep5.xchange.communication.Communicator;
 
 public class Settings implements Serializable {
 
 	private static final long serialVersionUID = 3297557095244201638L;
 
 	private static String localIp;
 	private static int incomingConnectionPort = 9000;
 	private static String settingsFileLocation = "xchange/settings.txt";
 	private static String sharedFolder = "xchange/shared/";
 	private static int blockSize = 64 * 1024;
 	private static String tmpExtension = ".!xch";
 	private static String infoExtension = ".!info";
 	private static String infoFolder = "xchange/INFO/";
 
 	private String routerIP;
 	private static int routerPort = 7000;
 	private String storageServerIP;
 	private static int storageServerPort = 9002;
 	private String nameServerIP;
 	private static int nameServerPort = 9001;
 
 	private static Settings instance;
 
 	public static final boolean DEBUG = true;
 
	private static final String SPLIT_CHAR = "|";
 
 	public String getNameServerIp() {
 		return nameServerIP;
 	}
 
 	public String getStorageServerIp() {
 		return storageServerIP;
 	}
 
 	public String getRouterIp() {
 		return routerIP;
 	}
 
 	public static int getNameServerPort() {
 		return nameServerPort;
 	}
 
 	public static String getLocalIp() {
 		return localIp;
 	}
 
 	public static void setLocalIp(String localIp) {
 		Settings.localIp = localIp;
 	}
 
 	public static int getIncomingConnectionPort() {
 		return incomingConnectionPort;
 	}
 
 	public static String getSharedFolder() {
 		return sharedFolder;
 	}
 
 	public static int getBlockSize() {
 		return blockSize;
 	}
 
 	public static String getTmpExtension() {
 		return tmpExtension;
 	}
 
 	public static String getInfoFolder() {
 		return infoFolder;
 	}
 
 	public static String getInfoExtension() {
 		return infoExtension;
 	}
 
 	public void setNameServerIp(String newNameServerIP) {
 		nameServerIP = newNameServerIP;
 	}
 
 	public void setStorageServerIp(String newStorageServerIP) {
 		storageServerIP = newStorageServerIP;
 	}
 
 	public void setRouterIp(String newRouterIP) {
 		routerIP = newRouterIP;
 	}
 
 	public static Settings getInstance() {
 		if (instance == null)
 			instance = new Settings();
 
 		return instance;
 	}
 
 	private static String getSettingFileLocation() {
 		return settingsFileLocation;
 	}
 
 	public void save() throws IOException {
 		FileOutputStream fileOut = new FileOutputStream(
 				Settings.getSettingFileLocation());
 		ObjectOutputStream out = new ObjectOutputStream(fileOut);
 		out.writeObject(getInstance());
 		out.close();
 		fileOut.close();
 	}
 
 	public void load() throws IOException, ClassNotFoundException {
 		FileInputStream fileIn = new FileInputStream(
 				Settings.getSettingFileLocation());
 		ObjectInputStream in = new ObjectInputStream(fileIn);
 		instance = (Settings) in.readObject();
 		in.close();
 		fileIn.close();
 	}
 
 	public boolean validate() {
 		Communicator.resetConnections();
 		if (Communicator.signUpToNameServer()
 				&& Communicator.testStorageServer()
 				&& Communicator.setRouterSettings()) {
 			return true;
 		}
 
 		return false;
 	}
 
 	public static int getRouterPort() {
 		return routerPort;
 	}
 
 	public static int getStorageServerPort() {
 		return storageServerPort;
 	}
 
 	public static String getSplitChar() {
 		return SPLIT_CHAR;
 	}
 }
