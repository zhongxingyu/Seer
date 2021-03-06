 package com.redhat.qe.jon.common.util;
 
 import java.io.IOException;
 
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * This intefrace defines Command Runner interface. 
  * @author lzoubek@redhat.com
  *
  */
 public interface ICommandRunner {
 
 	/**
 	 * copies file to destination
 	 * @param srcPath must always be on local machine
 	 * @param destDir
 	 * @throws IOException
 	 */
 	void copyFile(String srcPath,String destDir) throws IOException;
 	/**
 	 * copies file
 	 * @param srcPath must always be on local machine
 	 * @param destDir
 	 * @param destFileName name of destination file
 	 * @throws IOException
 	 */
 	void copyFile(String srcPath, String destDir, String destFileName) throws IOException;
 	/**
 	 * runs a command
 	 * @param command
	 * @return
 	 */
 	SSHCommandResult runAndWait(String command);
 	SSHCommandResult runAndWait(String command, long commandTimeout);
 	/**
 	 * disconnects (useful for remote runner implementations)
 	 */
 	void disconnect();
 	void connect();
 }
