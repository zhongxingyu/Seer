 package org.rsbot.security;
 
 import org.rsbot.Application;
 import org.rsbot.Configuration;
 import org.rsbot.Configuration.OperatingSystem;
 import org.rsbot.bot.RSLoader;
 import org.rsbot.gui.BotGUI;
 import org.rsbot.script.AccountStore;
 import org.rsbot.script.provider.ScriptDeliveryNetwork;
 import org.rsbot.util.UpdateChecker;
 import org.rsbot.util.io.JavaCompiler;
 
 import sun.font.FontManager;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileDescriptor;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.security.Permission;
 import java.util.ArrayList;
 
 /**
  * @author Paris
  */
 public class RestrictedSecurityManager extends SecurityManager {
 	private String getCallingClass() {
 		final String prefix = Application.class.getPackage().getName() + ".";
 		for (final Class<?> c : getClassContext()) {
 			final String name = c.getName();
 			if (name.startsWith(prefix) && !name.equals(RestrictedSecurityManager.class.getName())) {
 				return name;
 			}
 		}
 		return "";
 	}
 
 	public boolean isCallerScript() {
 		return Thread.currentThread().getName().startsWith("Script-") || getCallingClass().startsWith("org.rsbot.script.Script");
 	}
 
 	public ArrayList<String> getAllowedHosts() {
 		final ArrayList<String> whitelist = new ArrayList<String>(32);
 
 		// NOTE: if whitelist item starts with a dot "." then it is checked at the end of the host
 		whitelist.add(".imageshack.us");
 		whitelist.add(".tinypic.com");
 		whitelist.add(".photobucket.com");
 		whitelist.add(".imgur.com");
 		whitelist.add(".deviantart.net");
 		whitelist.add(".powerbot.org");
 		whitelist.add(".runescape.com");
 		whitelist.add(".ipcounter.de");
 
 		whitelist.add("shadowscripting.org"); // iDungeon
 		whitelist.add("shadowscripting.wordpress.com"); // iDungeon
 		whitelist.add(".glorb.nl"); // SXForce - Swamp Lizzy Paid, Snake Killah
 		whitelist.add("scripts.johnkeech.com"); // MrSneaky - SneakyFarmerPro
 		whitelist.add("myrsdatabase.x10.mx"); // gravemindx - BPestControl, GhoulKiller
 		whitelist.add("thedealer.site11.com"); // XscripterzX - PiratePlanker, DealerTanner
 		whitelist.add("elyzianpirate.web44.net"); // XscripterzX (see above)
 		whitelist.add(".wikia.com"); // common assets and images
 		whitelist.add("jtryba.com"); // jtryba - autoCook, monkR8per
 		whitelist.add("tehgamer.info"); // TehGamer - iMiner
 		whitelist.add("www.universalscripts.org"); // Fletch To 99 - UFletch
 		whitelist.add("www.dunkscripts.freeiz.com"); // Dunnkers
 		whitelist.add("www.dlolpics.com"); // DlolPics
 		whitelist.add(".logikmedia.co"); // countvidal
 		whitelist.add("letthesmokeout.com"); // MrByte
 		whitelist.add("zaszmedia.com"); // zasz - Frost Dragons Pro, Enchanter Pro, Jars Pro
 		whitelist.add("pumyscript.orgfree.com"); // Pumy - Ape Atoll Chinner, PumyDungxFarm, PumyArtisansWorkshop
 		whitelist.add("noneevr2.r00t.la"); // noneevr2 - TakeBury
 		whitelist.add("testscriptsecurity.host22.com");//Marneus901 - Runite miner
 		whitelist.add("massacrescripting.net");//ShizZznit - Aviansie Massacre.
 		whitelist.add(".ownagebots.com"); //Ownageful/Aut0r's scripts - OwnageGDK, OwnageBDK, OwnageFDK
 		whitelist.add("vassdascripts.comuf.com");//Dandan Boy - ?
 		whitelist.add("doout.net84.net");
 		whitelist.add("doout5.webs.com");
 		whitelist.add("terrabubble.netai.net");
 		whitelist.add("terrabubble.webs.com");
 		whitelist.add("aaimister.webs.com");
 		whitelist.add("xscriptx.atwebpages.com");
 
 		return whitelist;
 	}
 
 	@Override
 	public void checkAccept(final String host, final int port) {
 		throw new SecurityException();
 	}
 
 	@Override
 	public void checkConnect(final String host, final int port) {
 		if (host.equalsIgnoreCase("localhost") || host.startsWith("127.") || host.startsWith("192.168.") || host.startsWith("10.")) {
 			throw new SecurityException();
 		}
 
 		// ports other than HTTP (80), HTTPS (443) and unknown (-1) are automatically denied
 		if (!(port == -1 || port == 80 || port == 443)) {
 			throw new SecurityException();
 		}
 
 		if (isCallerScript()) {
 			boolean allowed = false;
 			if (isIpAddress(host)) {
 				// NOTE: loophole in whitelist - temporarily allowed for round robin DNS without reverse host set
 				allowed = true;
 			} else {
 				for (final String check : getAllowedHosts()) {
 					if (check.startsWith(".")) {
 						if (host.endsWith(check) || check.equals("." + host)) {
 							allowed = true;
 						}
 					} else if (host.equals(check)) {
 						allowed = true;
 					}
 					if (allowed == true) {
 						break;
 					}
 				}
 			}
 
 			if (!allowed) {
 				throw new SecurityException();
 			}
 		}
 
 		super.checkConnect(host, port);
 	}
 
 	private boolean isIpAddress(final String check) {
 		final int l = check.length();
 		if (l < 7 || l > 15) {
 			return false;
 		}
 		final String[] parts = check.split("\\.", 4);
 		if (parts.length != 4) {
 			return false;
 		}
 		for (int i = 0; i < 4; i++) {
 			final int n = Integer.parseInt(parts[i]);
 			if (n < 0 || n > 255) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public void checkConnect(final String host, final int port, final Object context) {
 		checkConnect(host, port);
 	}
 
 	@Override
 	public void checkCreateClassLoader() {
 		super.checkCreateClassLoader();
 	}
 
 	@Override
 	public void checkDelete(final String file) {
 		checkFilePath(file, false);
 		super.checkDelete(file);
 	}
 
 	@Override
 	public void checkExec(final String cmd) {
 		final String calling = getCallingClass();
 		for (final Class<?> c : new Class<?>[]{ScriptDeliveryNetwork.class, BotGUI.class, UpdateChecker.class, JavaCompiler.class}) {
 			if (calling.startsWith(c.getName())) {
 				super.checkExec(cmd);
 				return;
 			}
 		}
 		throw new SecurityException();
 	}
 
 	@Override
 	public void checkExit(final int status) {
 		final String calling = getCallingClass();
 		if (calling.equals(BotGUI.class.getName()) || calling.equals(Application.class.getName())) {
 			super.checkExit(status);
 		} else {
 			throw new SecurityException();
 		}
 	}
 
 	@Override
 	public void checkLink(final String lib) {
 		super.checkLink(lib);
 	}
 
 	@Override
 	public void checkListen(final int port) {
 		throw new SecurityException();
 	}
 
 	@Override
 	public void checkMemberAccess(final Class<?> clazz, final int which) {
 		super.checkMemberAccess(clazz, which);
 	}
 
 	@Override
 	public void checkMulticast(final InetAddress maddr) {
 		throw new SecurityException();
 	}
 
 	@Override
 	public void checkMulticast(final InetAddress maddr, final byte ttl) {
 		throw new SecurityException();
 	}
 
 	@Override
 	public void checkPackageAccess(final String pkg) {
 		super.checkPackageAccess(pkg);
 	}
 
 	@Override
 	public void checkPackageDefinition(final String pkg) {
 		super.checkPackageDefinition(pkg);
 	}
 
 	@Override
 	public void checkPermission(final Permission perm) {
 		if (perm instanceof RuntimePermission) {
 			if (perm.getName().equals("setSecurityManager")) {
 				throw new SecurityException();
 			}
 		}
 		// super.checkPermission(perm);
 	}
 
 	@Override
 	public void checkPermission(final Permission perm, final Object context) {
 		checkPermission(perm);
 	}
 
 	@Override
 	public void checkPrintJobAccess() {
 		throw new SecurityException();
 	}
 
 	@Override
 	public void checkPropertiesAccess() {
 		super.checkPropertiesAccess();
 	}
 
 	@Override
 	public void checkPropertyAccess(final String key) {
 		super.checkPropertyAccess(key);
 	}
 
 	@Override
 	public void checkRead(final FileDescriptor fd) {
 		super.checkRead(fd);
 	}
 
 	@Override
 	public void checkRead(final String file) {
 		checkFilePath(file, true);
 		super.checkRead(file);
 	}
 
 	@Override
 	public void checkRead(final String file, final Object context) {
 		checkRead(file);
 	}
 
 	@Override
 	public void checkSecurityAccess(final String target) {
 		super.checkSecurityAccess(target);
 	}
 
 	@Override
 	public void checkSetFactory() {
 		super.checkSetFactory();
 	}
 
 	@Override
 	public void checkSystemClipboardAccess() {
 		if (isCallerScript()) {
 			throw new SecurityException();
 		}
 	}
 
 	@Override
 	public boolean checkTopLevelWindow(final Object window) {
 		return super.checkTopLevelWindow(window);
 	}
 
 	@Override
 	public void checkWrite(final FileDescriptor fd) {
 		super.checkWrite(fd);
 	}
 
 	@Override
 	public void checkWrite(final String file) {
 		if (file.contains("hosts"))
 		System.out.println(getCallingClass());
 		checkFilePath(file, false);
 		super.checkWrite(file);
 	}
 
 	private void checkFilePath(String path, final boolean readOnly) {
 		path = new File(path).getAbsolutePath();
 		if (isCallerScript()) {
 			if (!path.startsWith(Configuration.Paths.getScriptCacheDirectory())) {
 				boolean fail = true;
 				if (!Configuration.RUNNING_FROM_JAR) {
 					// allow project resource directory if not running from JAR (i.e. in eclipse)
 					String check = new File(Configuration.Paths.ROOT).getAbsolutePath();
 					try {
 						check = new File(check).getCanonicalPath();
 					} catch (final IOException ignored) {
 					}
 					fail = !path.startsWith(check);
 				} else {
 					if (readOnly && path.equals(Configuration.Paths.getRunningJarPath())) {
 						fail = false;
 					}
 				}
 				for (final String prefix : new String[]{Configuration.Paths.getScreenshotsDirectory(),
 						Configuration.Paths.getScriptsDirectory(), Configuration.Paths.getWebDatabase()}) {
 					if (path.startsWith(prefix)) {
 						fail = false;
 						break;
 					}
 				}
 				final String jre = System.getProperty("java.home");
 				if (readOnly && jre != null && !jre.isEmpty() && path.startsWith(jre)) {
 					fail = false;
 				}
 				if (fail && readOnly) {
 					for (final String font : FontManager.getFontPath(true).split("\\Q" + File.pathSeparator + "\\E")) {
 						if (path.startsWith(font)) {
 							fail = false;
 							break;
 						}
 					}
 				}
 				if (Configuration.getCurrentOperatingSystem() == OperatingSystem.WINDOWS) {
 					final String sysroot = System.getenv("SystemRoot");
 					if (sysroot != null && sysroot.length() > 0 && path.startsWith(sysroot)) {
 						fail = !readOnly;
 						if (getCallingClass().startsWith(RSLoader.class.getName())) {
 							fail = false;
 						}
 					}
 				}
 				if (fail) {
 					throw new SecurityException();
 				}
 			}
 		}
 		if (path.equalsIgnoreCase(new File(Configuration.Paths.getAccountsFile()).getAbsolutePath())) {
 			for (final StackTraceElement s : Thread.currentThread().getStackTrace()) {
 				final String name = s.getClassName();
 				if (name.equals(AccountStore.class.getName())) {
 					return;
 				}
 			}
 			throw new SecurityException();
 		}
 	}
 
 	public static void fixHosts() {
 		if (Configuration.getCurrentOperatingSystem() != OperatingSystem.WINDOWS) {
 			return;
 		}
 		final File hosts = new File(System.getenv("SystemRoot") + "\\System32\\drivers\\etc\\hosts");
 		if (!hosts.exists()) {
 			return;
 		}
 		try {
 			final StringBuilder modified = new StringBuilder((int) hosts.length());
 			final BufferedReader reader = new BufferedReader(new FileReader(hosts));
			boolean infected = false;
 			String line;
 			while ((line = reader.readLine()) != null) {
 				if (line.contains(Configuration.Paths.URLs.HOST)) {
					infected = true;
 					continue;
 				}
 				modified.append(line);
 				modified.append("\r\n");
 			}
 			reader.close();
			if (infected) {
				final BufferedWriter writer = new BufferedWriter(new FileWriter(hosts));
				writer.append(modified.toString());
				writer.close();
			}
 		} catch (final IOException ignored) {
 		}
 	}
 }
