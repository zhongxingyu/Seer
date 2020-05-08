 package org.eclipse.dltk.ssh.core;
 
 import org.eclipse.core.runtime.IPath;
 
 public interface ISshConnection {
 
 	public void setPassword(String password);
 
 	public void disconnect();
 
 	public ISshFileHandle getHandle(IPath path) throws Exception;
 
 	public boolean isConnected();
 
 	public boolean connect();
 
 }
