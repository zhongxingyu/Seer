 package com.buglabs.bug.base.pub;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Writer;
 
 /**
  * This service is accessed by clients to create a IShellSession service.
  * 
  * @author kgilmer
  */
 public interface IShellService {
 	
 	/**
 	 * @return A default instance.  Directory defaults to /tmp.
 	 * @throws IOException
 	 */
 	public IShellSession createShellSession() throws IOException;
 	
 	/**
 	 * @param directory directory where shell should begin.
 	 * @param output A writer where all output should go, or null if not required.
 	 * @return
 	 * @throws IOException
 	 */
 	public IShellSession createShellSession(File directory, Writer output) throws IOException;
 }
