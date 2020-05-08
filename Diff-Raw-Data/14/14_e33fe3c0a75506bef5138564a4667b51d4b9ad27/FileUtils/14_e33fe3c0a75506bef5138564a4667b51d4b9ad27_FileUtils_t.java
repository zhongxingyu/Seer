 package nl.nikhef.jgridstart.util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /** Some file-related utilities */
 public class FileUtils {
     static protected Logger logger = Logger.getLogger("nl.nikhef.jgridstart.util");
     
     /** Copy a file from one place to another. This calls an external copy
      * program on the operating system to make sure it is properly copied and
      * permissions are retained.
      * @throws IOException */
     public static boolean CopyFile(File in, File out) throws IOException {
 	String[] cmd;
 	if (System.getProperty("os.name").startsWith("Windows")) {
 	    // windows
 	    cmd = new String[]{"xcopy.exe",
 		    in.getAbsolutePath(), out.getAbsolutePath(),
 		    "/x", "/q", "/y"};
 	} else {
 	    // other, assume unix-like
 	    cmd = new String[]{"cp",
 		    "-f", "-p",
 		    in.getAbsolutePath(), out.getAbsolutePath()};
 	}
 	return Exec(cmd) == 0;
     }
 
     /**
      * Change file permissions
      * 
      * Not supported natively until java 1.6. Bad Java.
     * Note that the ownerOnly argument differs from Java. When ownerOnly is
     * true for Java's File.setReadable(), File.setWritable() or
     * File.setExecutable(), the other/group permissions are left as they are.
     * This method resets them instead. When ownerOnly is false, behaviour is
     * as Java's.
      * 
      * @param file File to set permissions on
      * @param read  if reading should be allowed
      * @param write if writing should be allowed
      * @param exec if executing should be allowed
      * @param ownerOnly true to set group&world permissions to none,
      *                  false to set them all alike
      */
     static public boolean chmod(File file, boolean read, boolean write,
 	    boolean exec, boolean ownerOnly) {
 	try {
 	    // Try Java 1.6 method first.
 	    // This is also compilable on lower java versions
 	    boolean ret = true;
 	    Method setReadable = File.class.getDeclaredMethod("setReadable",
 		    new Class[] { boolean.class, boolean.class });
 	    Method setWritable = File.class.getDeclaredMethod("setWritable",
 		    new Class[] { boolean.class, boolean.class });
 	    Method setExecutable = File.class.getDeclaredMethod("setExecutable",
 		    new Class[] { boolean.class, boolean.class });
 
	    // first remove all permissions if ownerOnly is wanted, because File.set*
	    // doesn't touch other/group permissions when ownerOnly is true.
	    if (ownerOnly) {
		ret &= (Boolean)setReadable.invoke(file, new Object[]{ false, false });
		ret &= (Boolean)setWritable.invoke(file, new Object[]{ false, false });
		ret &= (Boolean)setExecutable.invoke(file, new Object[]{ false, false });
	    }
	    // then set owner/all permissions
 	    ret &= (Boolean)setReadable.invoke(file, new Object[] { read, ownerOnly });
 	    ret &= (Boolean)setWritable.invoke(file, new Object[] { write, ownerOnly });
 	    ret &= (Boolean)setExecutable.invoke(file, new Object[] { exec, ownerOnly });
 	    
 	    if (logger.isLoggable(Level.FINEST)) {
 		String perms = new String(new char[] {
 			read?  'r' : '-',
 			write? 'w' : '-',
 			exec?  'x' : '-'
 		}) + (ownerOnly ? "user" : "all");
 		logger.finest("Java 1.6 chmod "+perms+" of "+file+" returns "+ret);
 	    }
 
 	    return ret;
 	} catch (InvocationTargetException e) {
 	    // throw exceptions caused by set* methods
 	    throw (SecurityException)e.getTargetException();
 	    // return false; // (would be unreachable code)
 
 	} catch (NoSuchMethodException e) {
 	} catch (IllegalAccessException e) {
 	} catch (IllegalArgumentException e) {
 	}
 	
 	try {
 	    // fallback to unix command
 	    int perm = 0;
 	    if (read)  perm |= 4;
 	    if (write) perm |= 2;
 	    if (exec)  perm |= 1;
 	    String[] chmodcmd = { "chmod", null, file.getPath() };
 	    if (ownerOnly) {
 		Object args[] = { new Integer(perm) };
 		chmodcmd[1] = String.format("0%1d00", args);
 	    } else {
 		Object args[] = {new Integer(perm),new Integer(perm),new Integer(perm)};
 		chmodcmd[1] = String.format("0%1d%1d%1d", args);
 	    }
 	    return Exec(chmodcmd) == 0;
 	} catch (Exception e2) {
 	    return false;
 	}
     }
     
     /** Execute a command and return the exit code 
      * @throws InterruptedException */
     public static int Exec(String[] cmd) throws IOException {
 	// log
 	String scmd = "";
 	for (int i=0; i<cmd.length; i++) scmd += " "+cmd[i]; 
 	logger.finest("exec"+scmd);
 	// run
 	Process p = Runtime.getRuntime().exec(cmd);
 	int ret = -1;
 	try {
 	    ret = p.waitFor();
 	} catch (InterruptedException e) {
 	    // TODO verify this is the right thing to do
 	    throw new IOException(e.getMessage());
 	}
 	// log and return
 	logger.finest("exec"+scmd+" returns "+ret);
 	return ret;
     }
     
     /** Execute a command and return the exit code and store stdout and stderr.
      * 
      * @param cmd command to run
      * @param output String to which stdin and stdout is appended.
      * 
      * @throws IOException 
      * @throws InterruptedException */
     public static int Exec(String[] cmd, String output) throws IOException {
 	// log
 	String scmd = "";
 	for (int i=0; i<cmd.length; i++) scmd += " "+cmd[i]; 
 	logger.finest("exec"+scmd);
 	// run
 	Process p = Runtime.getRuntime().exec(cmd);
 	// retrieve output
 	String s = System.getProperty("line.separator");
 	String lineout, lineerr;
 	BufferedReader stdout = new BufferedReader(
 		new InputStreamReader(p.getInputStream()));
 	BufferedReader stderr = new BufferedReader(
 		new InputStreamReader(p.getErrorStream()));
 	while ( (lineout=stdout.readLine()) != null && (lineerr=stderr.readLine()) != null) {
 	    if (lineout!=null) output += lineout + s;
 	    if (lineerr!=null) output += lineerr + s;
 	}
 	stdout.close();	
 	// log and return
 	int ret = -1;
 	try {
 	    ret = p.waitFor();
 	} catch (InterruptedException e) {
 	    // TODO verify this is the right thing to do
 	    throw new IOException(e.getMessage());
 	}
 	logger.finest("exec"+scmd+" returns "+ret);
 	return ret;
     }
 }
