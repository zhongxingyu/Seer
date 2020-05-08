 package main;
 import java.io.File;
 import java.io.FileDescriptor;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.security.Permission;
 
 public class NoFollowSymlink extends SecurityManager {
 
 	private String restrictedRoot;
 	public NoFollowSymlink(String restrictedRoot) {
 		this.restrictedRoot = restrictedRoot;
 	}
 	@Override
 	public void checkRead(String file) {
 		if (file == null) throw new NullPointerException();
 		final File f = new File(file);
 		
 
 		//System.out.println("Sym?: " + file + " " + isSymlink(f));
 		/*boolean symlink = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
 
 			@Override
 			public Boolean run() {
 				// TODO Auto-generated method stub
 				try {
 				return new Boolean(isSymlink(f));
 				} catch (IOException ex) {
 					return new Boolean(true);
 				}
 			}
 			
 			
 		});*/
 		
 		
 		try {
 		if (file.startsWith(restrictedRoot) && isSymlink(f)) {
 			throw new SecurityException(f + " is a special file and must not be read");
 		}
 		} catch (IOException ex) {
 			throw new SecurityException(ex);
 			
 		}
 	}
 	
 	
 	
 
 
 
 
 
 	@Override
 	public void checkAccept(String host, int port) {
 		// TODO Auto-generated method stub
 		//super.checkAccept(host, port);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkAccess(Thread t) {
 		// TODO Auto-generated method stub
 		//super.checkAccess(t);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkAccess(ThreadGroup g) {
 		// TODO Auto-generated method stub
 		//super.checkAccess(g);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkAwtEventQueueAccess() {
 		// TODO Auto-generated method stub
 		//super.checkAwtEventQueueAccess();
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkConnect(String host, int port, Object context) {
 		// TODO Auto-generated method stub
 		//super.checkConnect(host, port, context);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkConnect(String host, int port) {
 		// TODO Auto-generated method stub
 		//super.checkConnect(host, port);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkCreateClassLoader() {
 		// TODO Auto-generated method stub
 		//super.checkCreateClassLoader();
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkDelete(String file) {
 		// TODO Auto-generated method stub
 		//super.checkDelete(file);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkExec(String cmd) {
 		// TODO Auto-generated method stub
 		//super.checkExec(cmd);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkExit(int status) {
 		// TODO Auto-generated method stub
 		//super.checkExit(status);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkLink(String lib) {
 		// TODO Auto-generated method stub
 		//super.checkLink(lib);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkListen(int port) {
 		// TODO Auto-generated method stub
 		//super.checkListen(port);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkMemberAccess(Class<?> clazz, int which) {
 		// TODO Auto-generated method stub
 		//super.checkMemberAccess(clazz, which);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkMulticast(InetAddress maddr, byte ttl) {
 		// TODO Auto-generated method stub
 		//super.checkMulticast(maddr, ttl);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkMulticast(InetAddress maddr) {
 		// TODO Auto-generated method stub
 		//super.checkMulticast(maddr);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkPackageAccess(String pkg) {
 		// TODO Auto-generated method stub
 		//super.checkPackageAccess(pkg);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkPackageDefinition(String pkg) {
 		// TODO Auto-generated method stub
 		//super.checkPackageDefinition(pkg);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkPermission(Permission perm, Object context) {
 		// TODO Auto-generated method stub
 		//super.checkPermission(perm, context);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkPermission(Permission perm) {
 		// TODO Auto-generated method stub
 		//super.checkPermission(perm);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkPrintJobAccess() {
 		// TODO Auto-generated method stub
 		//super.checkPrintJobAccess();
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkPropertiesAccess() {
 		// TODO Auto-generated method stub
 		//super.checkPropertiesAccess();
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkPropertyAccess(String key) {
 		// TODO Auto-generated method stub
 		//super.checkPropertyAccess(key);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkRead(FileDescriptor fd) {
 		// TODO Auto-generated method stub
 		//super.checkRead(fd);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkRead(String file, Object context) {
 		// TODO Auto-generated method stub
 		//super.checkRead(file, context);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkSecurityAccess(String target) {
 		// TODO Auto-generated method stub
 		//super.checkSecurityAccess(target);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkSetFactory() {
 		// TODO Auto-generated method stub
 		//super.checkSetFactory();
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkSystemClipboardAccess() {
 		// TODO Auto-generated method stub
 		//super.checkSystemClipboardAccess();
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkWrite(FileDescriptor fd) {
 		// TODO Auto-generated method stub
 		//super.checkWrite(fd);
 	}
 
 
 
 
 
 
 
 
 	@Override
 	public void checkWrite(String file) {
 		// TODO Auto-generated method stub
 		//super.checkWrite(file);
 	}
 
 
 
 
 
 
 
 
 	public static boolean isSymlink(File file) throws IOException {
 		  if (file == null)
 		    throw new NullPointerException("File must not be null");
 		  return !file.getCanonicalPath().equals(file.getAbsolutePath());
		  /*File canon;
		  if (file.getParent() == null) {
		    canon = file;
		  } else {
		    File canonDir = file.getParentFile().getCanonicalFile();
		    canon = new File(canonDir, file.getName());
		  }
		  return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());*/
 		}
 	
 	
 	
 }
