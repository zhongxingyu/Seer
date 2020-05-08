 package org.eclipse.dltk.core.internal.rse;
 
 import java.net.URI;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.core.internal.environment.EFSFileHandle;
 import org.eclipse.rse.core.model.IHost;
 import org.eclipse.rse.internal.efs.RSEFileSystem;
 import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
 import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
 import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
 
 public class RSEEnvironment implements IEnvironment, IAdaptable {
 	private IRemoteFileSubSystem fs;
 	private IHost host;
 
 	public RSEEnvironment(IRemoteFileSubSystem fs) {
 		this.fs = fs;
 		this.host = fs.getConnectorService().getHost();
 	}
 
 	public IFileHandle getFile(IPath path) {
 		if (Path.EMPTY.equals(path)) {
 			throw new RuntimeException(
 					"Passing empty filename to RSEEnvironment.getFile() method...");
 		}
 		URI uri = RSEFileSystem.getURIFor(host.getHostName(), path.toString());
 		return new EFSFileHandle(this, RSEFileSystem.getInstance()
 				.getStore(uri));
 	}
 
 	public String getId() {
 		return RSEEnvironmentProvider.RSE_ENVIRONMENT_PREFIX
 				+ host.getAliasName();
 	}
 
 	public String getSeparator() {
 		return fs.getSeparator();
 	}
 
 	public char getSeparatorChar() {
 		return fs.getSeparatorChar();
 	}
 
 	public boolean equals(Object obj) {
 		if (obj instanceof RSEEnvironment) {
 			RSEEnvironment other = (RSEEnvironment) obj;
 			return getId().equals(other.getId());
 		}
 		return false;
 	}
 
 	public int hashCode() {
 		return getId().hashCode();
 	}
 
 	public String getName() {
 		return host.getName() + " (RSE)";
 	}
 
 	public IHost getHost() {
 		return host;
 	}
 
 	public boolean hasProject(IProject project) {
 		if (!project.isAccessible()) {
 			return false;
 		}
 		IProjectDescription description;
 		try {
 			description = project.getDescription();
 			URI uri = description.getLocationURI();
 			if (uri != null) {
 				String uriHost = uri.getHost();
 				if (host.getHostName().equalsIgnoreCase(uriHost)) {
 					try {
 						IRemoteFile remoteFileObject = fs.getRemoteFileObject(
 								uri.getPath(), null);
 						if (remoteFileObject != null) {
 							if (remoteFileObject.exists()) {
 								return true;
 							}
 						}
 					} catch (SystemMessageException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public Object getAdapter(Class adapter) {
 		return Platform.getAdapterManager()
 				.loadAdapter(this, adapter.getName());
 	}
 
 	public URI getURI(IPath location) {
 		return RSEFileSystem.getURIFor(host.getHostName(), location
 				.toPortableString());
 	}
 
 	public String convertPathToString(IPath path) {
 		if (host.getSystemType().isWindows()) {
 			return path.toString().replaceAll("/", "\\");
 		} else {
 			return path.toString();
 		}
 	}
 
 	public IFileHandle getFile(URI locationURI) {
 		return new EFSFileHandle(this, RSEFileSystem.getInstance().getStore(
 				locationURI));
 	}
	
	public String getPathsSeparator() {
		return Character.toString(getPathsSeparatorChar());
	}

	public char getPathsSeparatorChar() {
		return host.getSystemType().isWindows() ? ';' : ':';
	}
 }
