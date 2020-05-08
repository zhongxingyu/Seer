 package edu.teco.dnd.module;
 
 import java.awt.AWTPermission;
 import java.io.FilePermission;
 import java.io.SerializablePermission;
 import java.lang.management.ManagementPermission;
 import java.lang.reflect.ReflectPermission;
 import java.net.NetPermission;
 import java.net.SocketPermission;
 import java.security.BasicPermission;
 import java.security.Permission;
 import java.security.SecurityPermission;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.PropertyPermission;
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
 	 * Classes/Methods after passing through which code is considered insecure. Class/Method names can be partial
 	 * (missing beginning and/or end). specifying an array with only 1 element, means the whole class. Syntax: <br>
 	 * String[0]=classname ; <br>
 	 * String[1] = method name.(optional)
 	 */
 	private static final Collection<String[]> insecureMethods = new LinkedList<String[]>();
 
 	/**
 	 * Methods the code can call after which execution is considered privileged again.<br>
 	 * Syntax: <br>
 	 * String[0]=fully qualified classname ; <br>
 	 * String[1] = method name.
 	 * 
 	 */
 	private static final Collection<String[]> securedMethods = new LinkedList<String[]>();
 
 	static {
 		insecureMethods.add(new String[] { "edu.teco.dnd.module.UsercodeWrapper" });
 		insecureMethods.add(new String[] { "edu.teco.dnd.module.FunctionBlockSecurityDecorator" });
 		insecureMethods.add(new String[] { "java.io.ObjectStreamClass", "invokeReadObject" });
 		insecureMethods.add(new String[] { "java.io.ObjectStreamClass", "invokeReadResolve" });
 
 		securedMethods.add(new String[] { "edu.teco.dnd.module.Application", "sendValue" });
 		// sending need special privileges.
 		securedMethods.add(new String[] { "java.lang.ClassLoader", "loadClass" });
 		// Default class loader needs FilePrivileges (not sure whether safe).
 		securedMethods.add(new String[] { "edu.teco.dnd.blocks.FunctionBlock", "getInputs" });
 		securedMethods.add(new String[] { "edu.teco.dnd.blocks.FunctionBlock", "getOptions" });
 		securedMethods.add(new String[] { "edu.teco.dnd.blocks.FunctionBlock", "getTimebetweenSchedules" });
 
 		securedMethods.add(new String[] { "edu.teco.dnd.util.Base64$1", "<init>" });
 		securedMethods.add(new String[] { "java.io.ObjectInputStream", "readObject" });
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
 		String[] reasonForGrant = null;
 		boolean isPrivileged = true;
 		Thread currentThread = Thread.currentThread();
 		if (currentThread == null) {
 			throw new SecurityException();
 			// Thread group is dead, which is strange in this context.
 		}
 
 		StackTraceElement[] stackTrace = currentThread.getStackTrace();
 
 		for (int i = stackTrace.length - 1; i >= 0; i--) {
 			StackTraceElement ste = stackTrace[i];
 			for (String[] str : insecureMethods) {
 				if (ste.getClassName().contains(str[0])) {
 					if (str.length == 1 || ste.getMethodName().contains(str[1])) {
 						// stack shows we are inside code that is considered unsafe.
 						isPrivileged = false;
 						break;
 					}
 				}
 			}
 			for (String[] str : securedMethods) {
 				if (ste.getClassName().equals(str[0]) && ste.getMethodName().equals(str[1])) {
 					// Code assumed safe again;
 					if (!isPrivileged) {
 						isPrivileged = true;
 						reasonForGrant = str;
 					}
 					break;
 					// FIXME: we really need a finer distinction than this.
 				}
 			}
 		} // Next ste;
 		if (isPrivileged && reasonForGrant != null) {
 			LOGGER.trace("granting privileges because of {}.{}", reasonForGrant[0], reasonForGrant[1]);
 		}
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
 				} else if (bPerm.getName().equals("accessDeclaredMembers")) {
 					StackTraceElement trace = Thread.currentThread().getStackTrace()[15];
					if (trace.getClass().equals("edu.teco.dnd.util.Base64$1")
							&& trace.getMethodName().equals("readObject")) {
 						doAllow = true;
 					}
 				} else {
 					// Deny
 				}
 			} else if (bPerm instanceof AuthPermission) {
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
