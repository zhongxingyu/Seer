 package net.mms_projects.copyit.app;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import net.mms_projects.copy_it.ApplicationLock;
 import net.mms_projects.copy_it.ApplicationLock.LockException;
 import net.mms_projects.copyit.FileStreamBuilder;
 import net.mms_projects.copyit.PathBuilder;
 import net.mms_projects.copyit.Settings;
 import net.mms_projects.copyit.ui.AbstractUi;
 import net.mms_projects.copyit.ui.SwtGui;
 import net.mms_projects.utils.OSValidator;
 
 import org.apache.commons.io.FileUtils;
 import org.freedesktop.Notifications;
 import org.freedesktop.dbus.DBusConnection;
 import org.freedesktop.dbus.UInt32;
 import org.freedesktop.dbus.Variant;
 import org.freedesktop.dbus.exceptions.DBusException;
 
 public class CopyItDesktop extends CopyIt {
 
 	protected Settings settings;
 
 	static public DBusConnection dbusConnection;
 
 	static private boolean nativeLoadingInitialized;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		CopyItDesktop app = new CopyItDesktop();
 		app.run();
 	}
 
 	public static String getVersion() {
 		String version = "";
 		if (CopyItDesktop.class.getPackage().getSpecificationVersion() != null) {
 			version += CopyItDesktop.class.getPackage()
 					.getSpecificationVersion();
 		} else {
 			version += "0.0.1";
 		}
 
 		if (CopyItDesktop.getBuildNumber() != 0) {
 			version += "-" + CopyItDesktop.getBuildNumber();
 		}
 		return version;
 	}
 
 	public static int getBuildNumber() {
 		try {
 			return Integer.parseInt(CopyItDesktop.class.getPackage()
 					.getImplementationVersion());
 		} catch (NumberFormatException e) {
 			return 0;
 		}
 	}
 
 	public void run() {
 		this.settings = new Settings();
 		try {
 			this.settings.setFileStreamBuilder(new StreamBuilder());
 		} catch (IOException e) {
 			e.printStackTrace();
 
 			System.exit(1);
 		}
 		this.settings.loadProperties();
 
 		if (OSValidator.isUnix()) {
 			this.exportResource("libunix-java.so");
 			try {
 				CopyItDesktop.dbusConnection = DBusConnection
 						.getConnection(DBusConnection.SESSION);
 			} catch (DBusException e1) {
 				// TODO Auto-generated catch block
 				System.out
 						.println("Ahh could not connect to D-Bus. All kinds of explosions n'stuff. Fix it!");
 				e1.printStackTrace();
 				System.exit(1);
 			}
 		}
 
 		final ApplicationLock appLock = new ApplicationLock(
 				PathBuilder.getConfigDirectory());
 		if (appLock.isRunning()) {
 			String message = "An instance is already running. ";
 
 			if (OSValidator.isUnix()) {
 				try {
 					Notifications notify = CopyItDesktop.dbusConnection
 							.getRemoteObject("org.freedesktop.Notifications",
 									"/org/freedesktop/Notifications",
 									Notifications.class);
 					Map<String, Variant<Byte>> hints = new HashMap<String, Variant<Byte>>();
 					hints.put("urgency", new Variant<Byte>((byte) 2));
 					notify.Notify("CopyIt", new UInt32(0), "", "CopyIt",
 							message, new LinkedList<String>(), hints, -1);
 				} catch (DBusException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 
 			System.out.println(message);
 			System.exit(0);
 		} else {
 			try {
 				appLock.lock();
 			} catch (LockException e) {
 				e.printStackTrace();
 
 				System.exit(1);
 			}
 
 			Runtime.getRuntime().addShutdownHook(new Thread() {
 				@Override
 				public void run() {
 					appLock.unlock();
 				}
 			});
 		}
 
 		AbstractUi ui = new SwtGui(this.settings);
 		ui.open();
 
 		this.settings.saveProperties();
 	}
 
 	class StreamBuilder extends FileStreamBuilder {
 
 		private File settingsFile;
 
 		public StreamBuilder() throws IOException {
 			this.settingsFile = new File(PathBuilder.getConfigDirectory(),
 					"options.properties");
 			if (!this.settingsFile.exists()) {
 				System.out.println("No settings file. Creating it.");
 				this.settingsFile.createNewFile();
 			}
 		}
 
 		@Override
 		public FileInputStream getInputStream() throws IOException {
 			return new FileInputStream(this.settingsFile);
 		}
 
 		@Override
 		public FileOutputStream getOutputStream() throws IOException {
 			return new FileOutputStream(this.settingsFile);
 		}
 
 	}
 
 	public static File exportResource(String resource) {
 		if (!CopyItDesktop.nativeLoadingInitialized) {
 			System.setProperty(
 					"java.library.path",
 					System.getProperty("java.library.path")
 							+ System.getProperty("path.separator")
 							+ PathBuilder.getCacheDirectory().getAbsolutePath());
 			System.out.println(System.getProperty("java.library.path"));
 
 			Field fieldSysPath = null;
 			try {
 				fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
 				fieldSysPath.setAccessible(true);
 				fieldSysPath.set(null, null);
 			} catch (SecurityException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 			} catch (NoSuchFieldException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			CopyItDesktop.nativeLoadingInitialized = true;
 		}
 		System.out.println("Exporting resource " + resource);
 		URL inputUrl = CopyItDesktop.class.getResource("/" + resource);
 		File dest = new File(PathBuilder.getCacheDirectory(), resource);
 		if (inputUrl == null) {
 			System.out
 					.println("No input resource available while exporting resource "
 							+ resource + ". " + "Ignoring it.");
 			return null;
 		}
 		try {
 			FileUtils.copyURLToFile(inputUrl, dest);
 		} catch (IOException e1) {
 			System.out.println("Could not copy " + resource
 					+ ". This might cause issues.");
 			e1.printStackTrace();
 		}
 		return dest;
 	}
 
 }
