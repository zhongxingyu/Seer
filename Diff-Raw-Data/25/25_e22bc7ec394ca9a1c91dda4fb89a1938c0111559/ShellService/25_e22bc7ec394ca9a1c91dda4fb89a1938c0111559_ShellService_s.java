 package com.buglabs.bug.base;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Writer;
 
 import com.buglabs.bug.base.pub.IShellService;
 import com.buglabs.bug.base.pub.IShellSession;
 
 
 /**
  * Default implementation of IShellService.
  * 
  * @author kgilmer
  */
 public class ShellService implements IShellService {
 
 	public IShellSession createShellSession() throws IOException {
 		return new ShellSession(new File("/tmp"), null);
 	}
 	
 	public IShellSession createShellSession(File directory, Writer output) throws IOException {
 		return new ShellSession(directory, output);
 	}
 
 }
