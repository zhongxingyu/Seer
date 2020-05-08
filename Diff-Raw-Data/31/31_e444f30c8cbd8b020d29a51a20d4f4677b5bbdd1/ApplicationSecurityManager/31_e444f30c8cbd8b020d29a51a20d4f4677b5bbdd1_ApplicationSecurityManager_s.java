 package edu.teco.dnd.module;
 
 import java.awt.AWTPermission;
 import java.io.FilePermission;
 import java.io.SerializablePermission;
 import java.lang.management.ManagementPermission;
 import java.lang.reflect.ReflectPermission;
 import java.net.NetPermission;
 import java.net.SocketPermission;
 import java.nio.file.LinkPermission;
 import java.security.BasicPermission;
 import java.security.Permission;
 import java.security.SecurityPermission;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.PropertyPermission;
import java.util.HashSet;
 import java.util.logging.LoggingPermission;
 
 import javax.security.auth.AuthPermission;
 import javax.sound.sampled.AudioPermission;
 import javax.xml.ws.WebServicePermission;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 /**
  * the security manager designed to limit assess rights of functionBlocks.
  * 
  * @author Marvin Marx
  * 
  */
 public class ApplicationSecurityManager extends SecurityManager {
 
 	private static final Logger LOGGER = LogManager.getLogger(ApplicationSecurityManager.class);
 
 	/**
 	 * Classes after passing through which code is considered insecure. Class names can be partial (missing beginning
 	 * and/or end).
 	 */
 	private static final Collection<String> insecureClasses = new LinkedList<String>();
 
 	/**
 	 * Methods the code can call after which execution is considered privileged again.<br>
 	 * Syntax: <br>
 	 * String[0]=fully qualified classname ; <br>
 	 * String[1] = method name.
 	 * 
 	 */
 	private static final Collection<String[]> securedMethods = new LinkedList<String[]>();
 
 	static {
 		insecureClasses.add("edu.teco.dnd.module.BlockRunner");
 		securedMethods.add(new String[] { "edu.teco.dnd.module.Application", "sendValue" });
 	}
 
 	public ApplicationSecurityManager() {
 		super();
 	}
 
 	/**
 	 * check if we are being called from within privileged code (meaning not a functionBlocks init) or doUpdate(). if a
 	 * known secured function is called after that permissions are still granted.)
 	 * 
 	 * @return true if we are inside an application, and not within other context.
 	 */
 	private boolean isPrivilegedCode() {
 		boolean isPrivileged = true;
 		Thread currentThread = Thread.currentThread();
 		if (currentThread == null) {
 			throw new SecurityException();
 			// Thread group is dead, which is strange in this context.
 		}
 
		for (StackTraceElement ste : currentThread.getStackTrace()) {
 
 			for (String str : insecureClasses) {
 				if (ste.getClassName().contains(str)) {
					// We are inside a FunctionBlocks doUpdate() or init()
					// == the stack contains BlockRunner somewhere, which marks that we are in user code.
 					isPrivileged = false;
 					break;
 				}
 			}
 			for (String[] str : securedMethods) {
 				if (ste.getClassName().equals(str[0]) && ste.getMethodName().equals(str[1])) {
 					// Code assumed safe again;
 					isPrivileged = true;
 					break;
 				}
 			}
		} //Next ste;
 		return isPrivileged;
 	}
 
 	@Override
 	public void checkPermission(Permission perm) {
 		LOGGER.entry(perm);
 		if (isPrivilegedCode()) {
 			LOGGER.trace("Allowing privileged code to do {}.", perm);
 			return;
 		}
 
 		LOGGER.trace("permission {} requested by app.", perm);
 		boolean doAllow = false;
 		if (perm instanceof SocketPermission) {
 		} else if (perm instanceof FilePermission) {
 		} else if (perm instanceof BasicPermission) {
 			BasicPermission bPerm = (BasicPermission) perm;
 			if (bPerm instanceof AudioPermission) {
 				doAllow = true;
 			} else if (bPerm instanceof WebServicePermission) {
 				doAllow = true;
 			} else if (bPerm instanceof AWTPermission) {
 				if (bPerm.getName().equals("accessSystemTray")) {
 					doAllow = true;
 				} else if (bPerm.getName().equals("fullScreenExclusive")) {
 					doAllow = true;
 				} else if (bPerm.getName().equals("fullScreenExclusive")) {
 					doAllow = true;
 				} else if (bPerm.getName().equals("setWindowAlwaysOnTop")) {
 					doAllow = true;
 				} else if (bPerm.getName().equals("watchMousePointer")) {
 					doAllow = true;
 				} else {
 					// Deny
 				}
 			} else if (bPerm instanceof NetPermission) {
 				if (bPerm.getName().equals("getNetworkInformation")) {
 					doAllow = true;
 				} else if (bPerm.getName().equals("getProxySelector")) {
 					doAllow = true;
 				} else {
 					// Deny
 				}
 			} else if (bPerm instanceof RuntimePermission) {
 				if (bPerm.getName().equals("getClassLoader")) {
 					doAllow = true;
 				} else if (bPerm.getName().equals("getProtectionDomain")) {
 					doAllow = true;
 				} else {
 					// Deny
 				}
 			} else if (bPerm instanceof AuthPermission) {
 			} else if (bPerm instanceof LinkPermission) {
 			} else if (bPerm instanceof LoggingPermission) {
 			} else if (bPerm instanceof ManagementPermission) {
 			} else if (bPerm instanceof PropertyPermission) {
 			} else if (bPerm instanceof ReflectPermission) {
 			} else if (bPerm instanceof SecurityPermission) {
 			} else if (bPerm instanceof SerializablePermission) {
 			}
 		}
 
 		if (doAllow) {
 			LOGGER.trace("Allowed permission {}.", perm);
 			LOGGER.exit("permission granted.");
 			return;
 		} else {
 			LOGGER.warn("Permission: {}, denied for application.", perm);
 			LOGGER.exit("permission denied.");
 			throw new SecurityException();
 		}
 	}
 
 	@Override
 	public void checkPermission(Permission perm, Object context) {
 		checkPermission(perm);
 	}
 
 }
