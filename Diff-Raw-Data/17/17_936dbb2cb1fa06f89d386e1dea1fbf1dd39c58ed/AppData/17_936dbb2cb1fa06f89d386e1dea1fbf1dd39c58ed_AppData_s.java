 package gui;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.JarURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Enumeration;
 import java.util.Scanner;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import tape.Tape;
 
 public class AppData {
 
 	/**
 	 * The name of the application.
 	 */
 	public static final String APP_NAME = "TuringBrain IDE";
 	/**
 	 * The version of the application.
 	 */
 	public static final String APP_VERSION = "0.8.5";
 	/**
 	 * The authors of the application.
 	 */
 	public static final String APP_AUTHORS = "Im Rahmen des Teamprojekts 2011 entstanden.\n\nInstitut für Programmierung\nund Reaktive Systeme\n\nBetreuer: Matthias Hagner\n\nVanessa Baier,\n Nils Breyer,\n Phillipp Neumann,\n Sven Schuster,\n David Wille";
 
 
 	static File appDataDirectory;
 	static File examplesDirectory;
 	static File tapeStylesDirectory;
 	
 	public static void init() {
 		//init system specific paths
 		if (System.getProperties().getProperty("os.name").equals("Linux")) {
 			AppData.appDataDirectory = new File(System.getProperty("user.home") + File.separator + "." + AppData.APP_NAME);
 		}
 		else if (System.getProperties().getProperty("os.name").equals("Mac OS X")) {
 			AppData.appDataDirectory = new File(System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support" + File.separator + AppData.APP_NAME);
 
 		}
 		else {
 			AppData.appDataDirectory = new File(System.getProperty("user.home") + File.separator + AppData.APP_NAME);
 		}
 		AppData.examplesDirectory = new File(appDataDirectory.getAbsolutePath() + File.separator + "examples");
 		AppData.tapeStylesDirectory = new File(appDataDirectory.getAbsolutePath() + File.separator + "tape" + File.separator + "styles");
 
 		try {
 			String installedVersion = AppData.getInstalledVersion();
 			if (!AppData.APP_VERSION.equals(installedVersion)) {
 				System.out.println("App data is outdated.");
 				try {
 					System.out.println("Updating app data  (" + installedVersion + "->" + AppData.APP_VERSION + ")...");
 					AppData.update();
 					System.out.println("Updating app data  (" + installedVersion + "->" + AppData.APP_VERSION + ") finished.");
 				} catch (Exception e1) {
 					ErrorDialog.showError("Failed to update application data (" + installedVersion + "->" + AppData.APP_VERSION + "). The application might not run correctly.", e1);
 				}			
 			}
 		}
 		catch (Exception e) {
 			System.out.println("App data not installed.");
 			try {
 				System.out.println("Installing app data  (n/a->" + AppData.APP_VERSION + ")...");
 				AppData.install();
 				System.out.println("Installing app data  (n/a->" + AppData.APP_VERSION + ") finished.");
 			} catch (Exception e1) {
 				ErrorDialog.showError("Failed to install application data. The application might not run correctly.", e1);
 			}
 		}
 	}
 
 	private static String getInstalledVersion() throws FileNotFoundException {
 		File versionFile = new File(appDataDirectory.getAbsolutePath() + File.separator + "Version");
 		Scanner scanner = new Scanner(new FileInputStream(versionFile), "UTF-8");
 		String version = scanner.nextLine();
 		scanner.close();
 
 		return version;
 	}
 
 	private static void install() throws IOException {
 		if (AppData.appDataDirectory.exists()) {
 			AppData.deleteDirectoryRecursively(AppData.appDataDirectory);
 		}
 		AppData.appDataDirectory.mkdirs();
 		AppData.examplesDirectory.mkdirs();
 		AppData.tapeStylesDirectory.mkdirs();
 
 		copyResourcesRecursively(Tape.class.getResource("images/styles"), new File(AppData.appDataDirectory.getAbsolutePath() + File.separator + "tape"));
 	}
 
 	private static void update() throws IOException{
 		//TODO: implement
 	}
 
 
 	private static void deleteDirectoryRecursively(File f) throws IOException {
 		if (f.isDirectory()) {
 			for (File c : f.listFiles()) {
 				System.out.println("Deleting " + c.getAbsolutePath() + "...");
 				c.delete();
 			}
 
 		}
 		System.out.println("Deleting " + f.getAbsolutePath() + "...");
 		f.delete();
 	}
 
 	private static boolean copyFilesRecusively(final File toCopy,
 			final File destDir) {
 		assert destDir.isDirectory();
 
 		if (!toCopy.isDirectory()) {
 			try {
 				return copyStream(new FileInputStream(toCopy), new FileOutputStream(new File(destDir, toCopy.getName())));
 			} catch (final FileNotFoundException e) {
 				e.printStackTrace();
 			}
 			return false;
 		} else {
 			final File newDestDir = new File(destDir, toCopy.getName());
 			System.out.println("Installing '" + newDestDir.getAbsolutePath() + "'...");
 
 			if (!newDestDir.exists() && !newDestDir.mkdir()) {
 				return false;
 			}
 			for (final File child : toCopy.listFiles()) {
 				if (!copyFilesRecusively(child, newDestDir)) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	private static boolean copyJarResourcesRecursively(final File destDir, final JarURLConnection jarConnection) throws IOException {
 		final JarFile jarFile = jarConnection.getJarFile();
 
 		for (final Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();) {
 			final JarEntry entry = e.nextElement();
 			if (entry.getName().startsWith(jarConnection.getEntryName())) {
 				String filename = entry.getName();
 				if (entry.getName().startsWith(jarConnection.getEntryName())) {
 					filename = filename.substring(jarConnection.getEntryName().length());
 				}
 
 				final File f = new File(destDir, filename);
 				if (!entry.isDirectory()) {
 					final InputStream entryInputStream = jarFile.getInputStream(entry);
 					if(!copyStream(entryInputStream, f)){
 						return false;
 					}
 					entryInputStream.close();
 				} else {
 					System.out.println("Installing '" + f.getAbsolutePath() + "'...");
 					if (!ensureDirectoryExists(f)) {
 						throw new IOException("Could not create directory: "
 								+ f.getAbsolutePath());
 					}
 				}
 			}
 		}
 		return true;
 	}
 
 	private static boolean copyResourcesRecursively(final URL originUrl, final File destination) {
 		try {
 			final URLConnection urlConnection = originUrl.openConnection();
 			if (urlConnection instanceof JarURLConnection) {
 				return copyJarResourcesRecursively(destination,
 						(JarURLConnection) urlConnection);
 			} else {
 				return copyFilesRecusively(new File(originUrl.getPath()),
 						destination);
 			}
 		} catch (final IOException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	private static boolean copyStream(final InputStream is, final File f) {
 		try {
 			return copyStream(is, new FileOutputStream(f));
 		} catch (final FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	private static boolean copyStream(final InputStream is, final OutputStream os) {
 		try {
 			final byte[] buf = new byte[1024];
 
 			int len = 0;
 			while ((len = is.read(buf)) > 0) {
 				os.write(buf, 0, len);
 			}
 			is.close();
 			os.close();
 			return true;
 		} catch (final IOException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	private static boolean ensureDirectoryExists(final File f) {
 		return f.exists() || f.mkdir();
 	}
 }
 
 
