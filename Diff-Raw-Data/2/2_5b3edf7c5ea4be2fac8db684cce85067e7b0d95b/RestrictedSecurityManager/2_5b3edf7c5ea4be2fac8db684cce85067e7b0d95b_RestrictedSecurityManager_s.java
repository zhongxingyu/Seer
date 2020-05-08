 package org.rsbot.security;
 
 import java.io.File;
 import java.io.FileDescriptor;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.security.Permission;
 import java.util.ArrayList;
 
 import org.rsbot.Application;
 import org.rsbot.gui.BotGUI;
 import org.rsbot.service.ScriptDeliveryNetwork;
 import org.rsbot.util.AccountStore;
 import org.rsbot.util.GlobalConfiguration;
 
 /**
  * @author Paris
  */
 public class RestrictedSecurityManager extends SecurityManager {
 	private String getCallingClass() {
 		final String prefix = Application.class.getPackage().getName() + ".";
 		for (final StackTraceElement s : Thread.currentThread().getStackTrace()) {
 			final String name = s.getClassName();
 			if (name.startsWith(prefix) && !name.equals(RestrictedSecurityManager.class.getName())) {
 				return name;
 			}
 		}
 		return "";
 	}
 
 	private boolean isCallerScript() {
 		return Thread.currentThread().getName().startsWith("Script-");
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
 
 		whitelist.add("shadowscripting.org"); // iDungeon
 		whitelist.add("shadowscripting.wordpress.com"); // iDungeon
 		whitelist.add(".glorb.nl"); // SXForce - Swamp Lizzy Paid, Snake Killah
 		whitelist.add("scripts.johnkeech.com"); // MrSneaky - SneakyFarmerPro
 		whitelist.add("myrsdatabase.x10.mx"); // gravemindx - BPestControl, GhoulKiller
		whitelist.add("hedealer.site11.com"); // XscripterzX - PiratePlanker, DealerTanner
 		whitelist.add("elyzianpirate.web44.net"); // XscripterzX (see above)
 		whitelist.add(".wikia.com"); // common assets and images
 		whitelist.add("jtryba.com"); // jtryba - autoCook, monkR8per
 		whitelist.add("tehgamer.info"); // TehGamer - iMiner
 		whitelist.add("www.universalscripts.org"); // Fletch To 99 - UFletch
 		whitelist.add("www.dunkscripts.freeiz.com"); // Dunnkers
 
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
 		checkFilePath(file);
 		super.checkDelete(file);
 	}
 
 	@Override
 	public void checkExec(final String cmd) {
 		final String calling = getCallingClass();
 		if (calling.equals(ScriptDeliveryNetwork.class.getName()) || calling.equals(BotGUI.class.getName())) {
 			super.checkExec(cmd);
 		} else {
 			throw new SecurityException();
 		}
 	}
 
 	@Override
 	public void checkExit(final int status) {
 		final String calling = getCallingClass();
 		if (calling.equals(BotGUI.class.getName())) {
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
 		checkFilePath(file);
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
 		throw new SecurityException();
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
 		checkFilePath(file);
 		super.checkWrite(file);
 	}
 
 	private void checkFilePath(String path) {
 		path = new File(path).getAbsolutePath();
 		if (isCallerScript()) {
 			if (!path.startsWith(GlobalConfiguration.Paths.getScriptCacheDirectory())) {
 				boolean fail = true;
 				if (!GlobalConfiguration.RUNNING_FROM_JAR) {
 					// allow project resource directory if not running from JAR (i.e. in eclipse)
 					String check = new File(GlobalConfiguration.Paths.ROOT).getAbsolutePath();
 					try {
 						check = new File(check).getCanonicalPath();
 					} catch (final IOException ignored) {
 					}
 					fail = !path.startsWith(check);
 				} else {
 					final String check = new File(GlobalConfiguration.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getAbsolutePath();
 					fail = !path.equals(check);
 				}
 				// allow screenshots directory
 				if (path.startsWith(GlobalConfiguration.Paths.getScreenshotsDirectory())) {
 					fail = false;
 				}
 				if (path.startsWith(GlobalConfiguration.Paths.getScriptsDirectory())) {
 					fail = false;
 				}
 				if (path.equals(GlobalConfiguration.Paths.getWebCache())) {
 					fail = false;
 				}
 				if (path.contains("jre6\\bin") || path.contains("jre/lib")) {
 					fail = false;
 				}
 				if (fail) {
 					throw new SecurityException();
 				}
 			}
 		}
 		if (path.equalsIgnoreCase(new File(GlobalConfiguration.Paths.getAccountsFile()).getAbsolutePath())) {
 			for (final StackTraceElement s : Thread.currentThread().getStackTrace()) {
 				final String name = s.getClassName();
 				if (name.equals(AccountStore.class.getName())) {
 					return;
 				}
 			}
 			throw new SecurityException();
 		}
 	}
 }
