 package org.rsbot.security;
 
 import java.io.File;
 import java.io.FileDescriptor;
 import java.net.InetAddress;
 import java.security.Permission;
 import java.util.ArrayList;
 
 import org.rsbot.Application;
 import org.rsbot.gui.BotGUI;
 import org.rsbot.script.Script;
 import org.rsbot.service.ScriptDeliveryNetwork;
 import org.rsbot.util.AccountStore;
 import org.rsbot.util.GlobalConfiguration;
 
 /**
  * @author Paris
  */
 public class RestrictedSecurityManager extends SecurityManager {
 	private String getCallingClass() {
 		final String prefix = Application.class.getPackage().getName() + ".";
 		for (StackTraceElement s : Thread.currentThread().getStackTrace()) {
 			final String name = s.getClassName();
 			if (name.startsWith(prefix) && !name.equals(RestrictedSecurityManager.class.getName())) {
 				return name;
 			}
 		}
 		return "";
 	}
 
 	private boolean isCallerScript() {
 		final StackTraceElement[] s = Thread.currentThread().getStackTrace();
 		for (int i = s.length - 1; i > -1; i--) {
 			if (s[i].getClassName().startsWith(Script.class.getName()))
 				return true;
 		}
 		return false;
 	}
 
 	public void checkAccept(String host, int port) {
 		throw new SecurityException();
 	}
 
 	public void checkConnect(String host, int port) {
 		if (host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1"))
 			throw new SecurityException();
 
 		// ports other than HTTP (80), HTTPS (443) and unknown (-1) are automatically denied
 		if (!(port == -1 || port == 80 || port == 443)) {
 			throw new SecurityException();
 		}
 
 		if (isCallerScript()) {
 			ArrayList<String> whitelist = new ArrayList<String>();
 
 			// NOTE: if whitelist item starts with a dot "." then it is checked at the end of the host
 			whitelist.add(".imageshack.us");
 			whitelist.add(".tinypic.com");
 			whitelist.add(".photobucket.com");
 			whitelist.add(".imgur.com");
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
 
 			// connecting to a raw IP address blocked because a fake reverse DNS is easy to set
 			if (isIpAddress(host))
 				throw new SecurityException();
 
 			boolean allowed = false;
 
 			for (String check : whitelist) {
 				if (check.startsWith(".")) {
 					if (host.endsWith(check) || check.equals("." + host))
 						allowed = true;
 				} else if (host.equals(check)) {
 					allowed = true;
 				}
				if (allowed = true)
 					break;
 			}
 
 			if (!allowed) {
 				throw new SecurityException();
 			}
 		}
 
 		super.checkConnect(host, port);
 	}
 
 	private boolean isIpAddress(String check) {
 		final int l = check.length();
 		if (l < 7 || l > 15) {
 			return false;
 		}
 		String[] parts = check.split("\\.", 4);
 		if (parts.length != 4) {
 			return false;
 		}
 		for (int i = 0; i < 4; i++) {
 			int n = Integer.parseInt(parts[i]);
 			if (n < 0 || n > 255) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public void checkConnect(String host, int port, Object context) {
 		checkConnect(host, port);
 	}
 
 	public void checkCreateClassLoader() {
 		super.checkCreateClassLoader();
 	}
 
 	public void checkDelete(String file) {
 		checkFilePath(file);
 		super.checkDelete(file);
 	}
 
 	public void checkExec(String cmd) {
 		final String calling = getCallingClass();
 		if (calling.equals(ScriptDeliveryNetwork.class.getName()) || calling.equals(BotGUI.class.getName())) {
 			super.checkExec(cmd);
 		} else {
 			throw new SecurityException();
 		}
 	}
 
 	public void checkExit(int status) {
 		final String calling = getCallingClass();
 		if (calling.equals(BotGUI.class.getName())) {
 			super.checkExit(status);
 		} else {
 			throw new SecurityException();
 		}
 	}
 
 	public void checkLink(String lib) {
 		super.checkLink(lib);
 	}
 
 	public void checkListen(int port) {
 		throw new SecurityException();
 	}
 
 	public void checkMemberAccess(Class<?> clazz, int which) {
 		super.checkMemberAccess(clazz, which);
 	}
 
 	public void checkMulticast(InetAddress maddr) {
 		throw new SecurityException();
 	}
 
 	public void checkMulticast(InetAddress maddr, byte ttl) {
 		throw new SecurityException();
 	}
 
 	public void checkPackageAccess(String pkg) {
 		super.checkPackageAccess(pkg);
 	}
 
 	public void checkPackageDefinition(String pkg) {
 		super.checkPackageDefinition(pkg);
 	}
 
 	public void checkPermission(Permission perm) {
 		//super.checkPermission(perm);
 	}
 
 	public void checkPermission(Permission perm, Object context) {
 		//super.checkPermission(perm, context);
 	}
 
 	public void checkPrintJobAccess() {
 		throw new SecurityException();
 	}
 
 	public void checkPropertiesAccess() {
 		super.checkPropertiesAccess();
 	}
 
 	public void checkPropertyAccess(String key) {
 		super.checkPropertyAccess(key);
 	}
 
 	public void checkRead(FileDescriptor fd) {
 		if (isCallerScript()) {
 			throw new SecurityException();
 		}
 		super.checkRead(fd);
 	}
 
 	public void checkRead(String file) {
 		checkSuperFilePath(file);
 		super.checkRead(file);
 	}
 
 	public void checkRead(String file, Object context) {
 		checkRead(file);
 	}
 
 	public void checkSecurityAccess(String target) {
 		super.checkSecurityAccess(target);
 	}
 
 	public void checkSetFactory() {
 		super.checkSetFactory();
 	}
 
 	public void checkSystemClipboardAccess() {
 		throw new SecurityException();
 	}
 
 	public boolean checkTopLevelWindow(Object window) {
 		return super.checkTopLevelWindow(window);
 	}
 
 	public void checkWrite(FileDescriptor fd) {
 		if (isCallerScript()) {
 			throw new SecurityException();
 		}
 		super.checkWrite(fd);
 	}
 
 	public void checkWrite(String file) {
 		checkFilePath(file);
 		super.checkWrite(file);
 	}
 
 	private void checkSuperFilePath(String path) {
 		path = new File(path).getAbsolutePath();
 		if (path.equalsIgnoreCase(new File(GlobalConfiguration.Paths.getAccountsFile()).getAbsolutePath())) {
 			for (StackTraceElement s : Thread.currentThread().getStackTrace()) {
 				final String name = s.getClassName();
 				if (name.equals(AccountStore.class.getName()))
 					return;
 			}
 			throw new SecurityException();
 		}
 	}
 
 	private void checkFilePath(String path) {
 		checkSuperFilePath(path);
 		path = new File(path).getAbsolutePath();
 		if (isCallerScript()) {
 			if (!path.startsWith(GlobalConfiguration.Paths.getScriptCacheDirectory()))
 				throw new SecurityException();
 		}
 	}
 }
