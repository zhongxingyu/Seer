 package org.whired.ghostclient.updater;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.security.MessageDigest;
 import java.util.Enumeration;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import javax.swing.SwingUtilities;
 
 import org.whired.ghost.constants.Vars;
 import org.whired.ghostclient.io.HttpClient;
 import org.whired.ghostclient.updater.ui.UpdaterFrame;
 
 /**
  * Downloads and launches the newest version of GHOST
  * 
  * @author Whired
  */
 public class Launcher implements Runnable {
 
 	/**
 	 * The name of the jar package
 	 */
 	private static final String JAR_PACKAGE_NAME = "ghostclient.jar";
 	/**
 	 * The name of the module package
 	 */
 	private static final String MODULE_PACKAGE_NAME = "modules.zip";
 	/**
 	 * Where the packages are hosted
 	 */
 	private static final String REMOTE_CODEBASE = "https://raw.github.com/Whired/jghost-portable/master/dist/dev/"; // Dev
 																								// branch
 	/**
 	 * Where the packages are saved
 	 */
 	private static final String LOCAL_CODEBASE = Vars.getLocalCodebase();
 	/**
 	 * The entry point of the main application
 	 */
 	private final String ENTRY_POINT = "org.whired.ghostclient.Main";
 	/**
 	 * The GUI to display output on
 	 */
 	private final UpdaterFrame form;
 
 	/**
 	 * Creates a new launcher for the given form
 	 * 
 	 * @param form the form to display output to
 	 */
 	public Launcher(UpdaterFrame form) {
 		this.form = form;
 	}
 
 	@Override
 	public void run() {
 		form.log("Grabbing file version..");
 		String remoteHash;
 		try {
 			remoteHash = getRemoteHash(REMOTE_CODEBASE + JAR_PACKAGE_NAME);
 		}
 		catch (IOException e) {
 			form.log("Unable to check hash.");
 			e.printStackTrace();
 			launchGhost();
 			return;
 		}
 		String localHash = getLocalHash(LOCAL_CODEBASE + JAR_PACKAGE_NAME);
 		boolean match = localHash != null && remoteHash.toLowerCase().equals(localHash.toLowerCase());
 		while (!match) {
 			form.log("Hash mismatch.");
 			form.log("Downloading new version..");
 			try {
 				HttpClient.saveToDisk(LOCAL_CODEBASE + JAR_PACKAGE_NAME, REMOTE_CODEBASE + JAR_PACKAGE_NAME);
 			}
 			catch (Throwable t) {
 				form.log("Unable to download new codebase.");
 				t.printStackTrace();
 				launchGhost();
 				return;
 			}
 			form.log("Newest version downloaded.");
 			form.log("Checking sanity..");
 			localHash = getLocalHash(LOCAL_CODEBASE + JAR_PACKAGE_NAME);
 			match = remoteHash.toLowerCase().equals(localHash.toLowerCase());
 			break;
 		}
 		form.log("Hashes match, GHOST is up to date!");
 		if (form.downloadModules()) {
 			form.log("Downloading modules..");
 			File zipped;
 			try {
 				zipped = HttpClient.saveToDisk(LOCAL_CODEBASE + MODULE_PACKAGE_NAME, REMOTE_CODEBASE + MODULE_PACKAGE_NAME);
 			}
 			catch (Throwable t) {
 				form.log("Unable to download modules.");
 				t.printStackTrace();
 				launchGhost();
 				return;
 			}
 			form.log("Unpacking modules..");
 			File f = new File(LOCAL_CODEBASE + "modules" + Vars.FS);
 			if (!f.exists()) {
 				f.mkdirs();
 			}
 			try {
 				ZipFile zipFile = new ZipFile(zipped);
 				Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
 				while (enumeration.hasMoreElements()) {
 					ZipEntry zipEntry = enumeration.nextElement();
					BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));
					int size;
					byte[] buffer = new byte[2048];
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(zipEntry.getName()), buffer.length);
					while ((size = bis.read(buffer, 0, buffer.length)) != -1) {
						bos.write(buffer, 0, size);
 					}
					bos.flush();
					bos.close();
					bis.close();
 				}
 				zipped.delete();
 			}
 			catch (Throwable t) {
 				form.log("Unable to unpack modules.");
 				t.printStackTrace();
 				launchGhost();
 				return;
 			}
 		}
 		form.log("Update fully successful.");
 		launchGhost();
 	}
 
 	private void launchGhost() {
 		try {
 			form.log("Launching GHOST..");
 			ProcessBuilder pb = new ProcessBuilder("java", "-classpath", LOCAL_CODEBASE + JAR_PACKAGE_NAME, ENTRY_POINT);
 			pb.start();
 			form.log("Exiting..");
 			try {
 				Thread.sleep(8000);
 			}
 			catch (InterruptedException ex) {
 			}
 			System.exit(0);
 		}
 		catch (IOException e) {
 			form.log("Unable to launch GHOST: " + e.toString());
 		}
 	}
 
 	/**
 	 * Gets the hash from the file at the specified url
 	 * 
 	 * @param url the url of the file to get the hash of
 	 * @return the hash that was read
 	 */
 	private String getRemoteHash(String url) throws IOException {
 		BufferedReader br = new BufferedReader(new InputStreamReader(HttpClient.getStream(url + ".MD5")));
 		StringBuilder sb = new StringBuilder();
 		String line;
 		while ((line = br.readLine()) != null) {
 			sb.append(line);
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Gets the hash from the file at the given path
 	 * 
 	 * @param path the path to the file
 	 * @return the hash of the file
 	 */
 	private String getLocalHash(String path) {
 		BufferedInputStream bis = null;
 		try {
 			File file = new File(path);
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			int bytesRead;
 			byte[] buffer = new byte[1024];
 			bis = new BufferedInputStream(new FileInputStream(file));
 			while ((bytesRead = bis.read(buffer)) != -1) {
 				md.update(buffer, 0, bytesRead);
 			}
 			bis.close();
 			return toHexString(md.digest());
 		}
 		catch (Exception ex) {
 		} // Swallow irrelevant exceptions
 		finally {
 			try {
 				if (bis != null) {
 					bis.close();
 				}
 			}
 			catch (IOException ex) {
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Converts an array of bytes to a hexidecimal string
 	 * 
 	 * @param bytes the bytes to convert
 	 * @return the resulting string
 	 */
 	public static String toHexString(byte[] bytes) {
 		char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
 		char[] hexChars = new char[bytes.length * 2];
 		int v;
 		for (int j = 0; j < bytes.length; j++) {
 			v = bytes[j] & 0xFF;
 			hexChars[j * 2] = hexArray[v / 16];
 			hexChars[j * 2 + 1] = hexArray[v % 16];
 		}
 		return new String(hexChars);
 	}
 
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				final UpdaterFrame form = new UpdaterFrame();
 				form.setOnLaunch(new Runnable() {
 					@Override
 					public void run() {
 						new Thread(new Launcher(form)).start();
 					}
 				});
 			}
 		});
 	}
 }
