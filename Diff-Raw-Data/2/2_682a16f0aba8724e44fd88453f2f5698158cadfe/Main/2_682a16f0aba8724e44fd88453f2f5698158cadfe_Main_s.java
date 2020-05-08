 package org.spoutshafter.client;
 
 import java.applet.Applet;
 import java.awt.Frame;
 import java.io.ByteArrayInputStream;
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Map;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 import javax.swing.JOptionPane;
 import java.security.MessageDigest;
 import org.spoutshafter.client.proxy.MineProxy;
 import org.spoutshafter.client.util.Resources;
 import org.spoutshafter.client.util.SimpleRequest;
 import org.spoutshafter.client.util.Streams;
 
 public class Main extends Applet {
 	private static final long serialVersionUID = 1L;
 	
 	protected static float VERSION = 1.4f;
 	
	protected static String launcherDownloadURL = "http://ci.getspout.org/job/Spoutcraft%20Launcher/promotion/latest/Recommended/artifact/target/launcher-dev-SNAPSHOT.jar";
 	protected static String normalLauncherFilename = "spoutcraft.jar";
 	protected static String hackedLauncherFilename = "spoutcraft_modified.jar";
 	
 	protected static String MANIFEST_TEXT = "Manifest-Version: 1.2\nCreated-By: 1.6.0_22 (Sun Microsystems Inc.)\nMain-Class: org.spoutcraft.launcher.Main\n";
 	
 	/* Added For MineshafterSquared */
 	protected static String authServer = Resources.loadString("auth").trim();
 	protected static String mineshaftersquaredPath;
 	protected static String gamePath;
 	protected static String versionPath;
 	
 	
 	public void init() {
 		Main.main(new String[0]);
 	}
 	
 	public static void main(String[] args) {
 		try {
 			// Get Update Info
 			String[] gamePaths = getGameFilePaths();
 			gamePath = gamePaths[0];
 			mineshaftersquaredPath = gamePaths[2];
 			
 			// check to make sure mineshaftersquaredPath exists if not create it
 			File msFilePath = new File(mineshaftersquaredPath);
 			if(!msFilePath.exists())
 			{
 				msFilePath.mkdir();
 			}
 			
 			// set minecraft downloads to Mineshafter Squared dir
 			Main.normalLauncherFilename = mineshaftersquaredPath + Main.normalLauncherFilename;
 			Main.hackedLauncherFilename = mineshaftersquaredPath + Main.hackedLauncherFilename;
 			
 			// updateInfo string for use with the open mineshaftersquared auth server is "http://" + authServer + "/update.php?name=client&build=" + buildNumber
 			String updateInfo = new String(SimpleRequest.get("http://mineshafter.tr0l.it/version/Spoutshafter-Squared"));
 			String launcherMD5 = new String(SimpleRequest.get("http://mineshafter.tr0l.it/version/Spoutcraft-Launcher"));
 			
 			// make sure updateInfo is 0 if it is empty
 			if(updateInfo.isEmpty()) {
 				updateInfo = "0";
 			}
 			
 			// parse out updateInfo into an integer
 			float version;
 			try {
 				version = Float.parseFloat(updateInfo);
 			} 
 			catch(Exception e) {
 				version = 0;
 			}
 			
 			// Print Proxy Version Numbers to Console
 			System.out.println("Current proxy version: " + VERSION);
 			System.out.println("Gotten proxy version: " + version);
 			
 			if(VERSION < version) {
 				JOptionPane.showMessageDialog(null, "A new version of Spoutshafter Squared is available at http://mineshafter.tr0l.it\nGo get it.", "Update Available", JOptionPane.PLAIN_MESSAGE);
 				System.exit(0);
 			}
             
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			BufferedInputStream in = new BufferedInputStream(new FileInputStream(normalLauncherFilename));
 			
 			if(new File(normalLauncherFilename).exists()) {
 				int theByte = 0;
 				while ((theByte = in.read()) != -1) {
 					md.update((byte) theByte);
 				}
 				in.close();
 				
 				byte[] theDigest = md.digest();
 				StringBuffer sb = new StringBuffer();
 				for (byte b : theDigest) {
 					sb.append(Integer.toHexString((int) (b & 0xff)));
 				}
 				
 				if(!sb.toString().equals(launcherMD5)) {
 					new File(normalLauncherFilename).delete();
 				}
 			}
 			
 		} 
 		catch(Exception e) {
 			// if errors
 			System.out.println("Error while updating:");
 			e.printStackTrace();
 			/* System.exit(1); */
 		}
 		
 		try {
 			MineProxy proxy = new MineProxy(VERSION, authServer); // create proxy
 			proxy.start(); // launch proxy
 			int proxyPort = proxy.getPort();
 			
 			System.setProperty("http.proxyHost", "127.0.0.1");
 			System.setProperty("http.proxyPort", Integer.toString(proxyPort));
 			
 			//System.setProperty("https.proxyHost", "127.0.0.1");
 			//System.setProperty("https.proxyPort", Integer.toString(proxyPort));
 			
 			// Make sure we have a fresh launcher every time
 			File hackedFile = new File(hackedLauncherFilename);
 			if(hackedFile.exists()){ 
 				hackedFile.delete();
 			}
 			
 			// start the game launcher
 			startLauncher(args);
 			
 		} catch(Exception e) {
 			System.out.println("Something bad happened:");
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	public static void startLauncher(String[] args) {
 		try {
 			// if hacked game exists
 			if(new File(hackedLauncherFilename).exists()) {
 				URL u = new File(hackedLauncherFilename).toURI().toURL();
 				URLClassLoader cl = new URLClassLoader(new URL[]{u});
 				
 				@SuppressWarnings("unchecked")
 				Class<Frame> launcherFrame = (Class<Frame>) cl.loadClass("org.spoutcraft.launcher.Main");
 				
 				String[] nargs;
 				try{
 					nargs = new String[args.length - 1];
 					System.arraycopy(args, 1, nargs, 0, nargs.length); // Transfer the arguments from the process call so that the launcher gets them
 				} catch(Exception e){
 					nargs = new String[0];
 				}
 				
 				Method main = launcherFrame.getMethod("main", new Class[]{ String[].class });
 				main.invoke(launcherFrame, new Object[]{ nargs });
 			}
 			// if the normal game exists
 			else if(new File(normalLauncherFilename).exists()) {
 				editLauncher();
 				startLauncher(args);
 			}
 			// 
 			else {
 				try{
 					byte[] data = SimpleRequest.get(launcherDownloadURL);
 					OutputStream out = new FileOutputStream(normalLauncherFilename);
 					out.write(data);
 					out.flush();
 					out.close();
 					startLauncher(args);
 					
 				} catch(Exception ex) {
 					System.out.println("Error downloading launcher:");
 					ex.printStackTrace();
 					return;
 				}
 			}
 		} catch(Exception e1) {
 			System.out.println("Error starting launcher:");
 			e1.printStackTrace();
 		}
 	}
 	
 	public static void editLauncher() {
 		try {
 			ZipInputStream in = new ZipInputStream(new FileInputStream(normalLauncherFilename));
 			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(hackedLauncherFilename));
 			ZipEntry entry;
 			String n;
 			InputStream dataSource;
 			while((entry = in.getNextEntry()) != null) {
 				n = entry.getName();
 				if(n.contains(".svn") || n.equals("META-INF/MOJANG_C.SF") || n.equals("META-INF/MOJANG_C.DSA") || n.equals("net/minecraft/minecraft.key")) continue;
 				out.putNextEntry(entry);
 				if(n.equals("META-INF/MANIFEST.MF")) dataSource = new ByteArrayInputStream(MANIFEST_TEXT.getBytes());
 				else if(n.equals("org/spoutcraft/launcher/PlatformUtils.class")) dataSource = Resources.load("org/spoutcraft/launcher/PlatformUtils.class");
 				else if(n.equals("org/spoutcraft/launcher/Main.class")) dataSource = Resources.load("Main.class");
 				else if(n.equals("org/spoutcraft/launcher/gui/OptionDialog.class")) dataSource = Resources.load("OptionDialog.class");
 				else dataSource = in;
 				Streams.pipeStreams(dataSource, out);
 				out.flush();
 			}
 			in.close();
 			out.close();
 		} catch(Exception e) {
 			System.out.println("Editing launcher failed:");
 			e.printStackTrace();
 		}
 	}
 	
 	private static String[] getGameFilePaths(){
 		String[] paths = new String[3];
 		
 		String os = System.getProperty("os.name").toLowerCase();
         Map<String, String> enviornment = System.getenv();
         String basePath;
         if (os.contains("windows")) {
         	basePath = enviornment.get("APPDATA");
             paths[0] = basePath + "\\.minecraft\\bin";
             paths[1] = paths[0] + "\\version";
             paths[2] = basePath + "\\.spoutshaftersquared\\";
         } else if (os.contains("mac")) {
         	basePath = "/Users/" + enviornment.get("USER") + "/Library/Application Support";
         	paths[0] = basePath + "/minecraft/bin";
         	paths[1] = paths[0] + "/version";
         	paths[2] = basePath + "/spoutshaftersquared/";
         } else if(os.contains("linux")){
         	basePath = enviornment.get("HOME");
         	paths[0] = basePath+ "/.minecraft/bin";
         	paths[1] = paths[0] + "/version";
         	paths[2] = basePath + "/.spoutshaftersquared/";
         }
         
         return paths;
 	}
 	
 	private static void recursiveDelete(File root){
         if(root.isDirectory()){
             for(File file : root.listFiles()){
             	recursiveDelete(file);
             }
             root.delete();
         } else {
             root.delete();
         }
     }
 }
