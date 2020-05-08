 package org.eclipse.dltk.core.environment;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.eclipse.core.runtime.IPath;
 import org.osgi.framework.Bundle;
 
 public interface IDeployment {
 	IPath add(Bundle bundle, String bundlePath) throws IOException;
	IPath add(InputStream stream, String filename) throws IOException;
 	void mkdirs(IPath path);
 	void dispose();
 	IFileHandle getFile(IPath deploymentPath);
 	IPath getAbsolutePath();
 }
