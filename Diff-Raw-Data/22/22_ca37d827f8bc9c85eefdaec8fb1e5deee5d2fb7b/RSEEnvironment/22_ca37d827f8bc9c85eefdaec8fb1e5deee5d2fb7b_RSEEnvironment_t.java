 package org.eclipse.dltk.core.internal.rse;
 
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Map;
 
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.rse.core.model.IHost;
 import org.eclipse.rse.core.subsystems.ISubSystem;
 import org.eclipse.rse.internal.efs.RSEFileSystem;
 import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
 
 @SuppressWarnings("restriction")
 public class RSEEnvironment implements IEnvironment, IAdaptable {
 	private IRemoteFileSubSystem fs;
 	private IHost host;
 	private static final Map<IRemoteFileSubSystem, Boolean> tryToConnect = new HashMap<IRemoteFileSubSystem, Boolean>();
 
 	public RSEEnvironment(IRemoteFileSubSystem fs) {
 		this.fs = fs;
 		this.host = fs.getConnectorService().getHost();
 	}
 
 	/*
 	 * @see org.eclipse.dltk.core.environment.IEnvironment#isLocal()
 	 */
 	public boolean isLocal() {
 		return false;
 	}
 
 	public IFileHandle getFile(IPath path) {
 		if (Path.EMPTY.equals(path)) {
 			throw new RuntimeException(
 					Messages.RSEEnvironment_EmptyFileNameError);
 		}
 		return new RSEFileHandle(this, getURIFor(host, path.toString()));
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
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof IEnvironment) {
 			IEnvironment other = (IEnvironment) obj;
 			return getId().equals(other.getId());
 		}
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
 		return getId().hashCode();
 	}
 
 	public String getName() {
 		return host.getName() + Messages.RSEEnvironment_EnvironmentNameSuffix;
 	}
 
 	public IHost getHost() {
 		return host;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Object getAdapter(Class adapter) {
 		return Platform.getAdapterManager()
 				.loadAdapter(this, adapter.getName());
 	}
 
 	public URI getURI(IPath location) {
 		return getURIFor(host, location.toString());
 	}
 
 	public String convertPathToString(IPath path) {
 		if (host.getSystemType().isWindows()) {
 			return path.toString().replace('/', '\\');
 		} else {
 			return path.toString();
 		}
 	}
 
 	public IFileHandle getFile(URI locationURI) {
 		if (RSEEnvironmentProvider.RSE_SCHEME.equalsIgnoreCase(locationURI
 				.getScheme())
 				&& locationURI.getHost().equals(host.getHostName())) {
 			return new RSEFileHandle(this, locationURI);
 		} else {
 			final URI[] resolved = EnvironmentManager.resolve(locationURI);
 			for (int i = 0; i < resolved.length; ++i) {
 				final URI newLocation = resolved[i];
 				if (RSEEnvironmentProvider.RSE_SCHEME
 						.equalsIgnoreCase(newLocation.getScheme())
 						&& newLocation.getHost().equals(host.getHostName())) {
 					return new RSEFileHandle(this, newLocation);
 				}
 			}
 			return null;
 		}
 	}
 
 	public String getPathsSeparator() {
 		return Character.toString(getPathsSeparatorChar());
 	}
 
 	public char getPathsSeparatorChar() {
 		return host.getSystemType().isWindows() ? ';' : ':';
 	}
 
 	public String getCanonicalPath(IPath path) {
		if (connect()) {
			try {
				IFileStore store = EFS.getStore(getURI(path));
				IFileInfo info = store.fetchInfo();
				if (info.getAttribute(EFS.ATTRIBUTE_SYMLINK)) {
					String linkTarget = info
							.getStringAttribute(EFS.ATTRIBUTE_LINK_TARGET);
					IFileStore resolved = store.getFileStore(new Path(
							linkTarget));
					return resolved.toURI().getPath();
				} else {
					return store.toURI().getPath();
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
 		return convertPathToString(path);
 	}
 
 	public static URI getURIFor(IHost host, String path) {
 		return RSEFileSystem.getURIFor(host.getHostName(), path);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public boolean isConnected() {
 		// IConnectorService[] services = host.getConnectorServices();
 		// int connected = 0;
 		// for (IConnectorService service : services) {
 		// if (service.isConnected()) {
 		// connected++;
 		// }
 		// }
 		// return connected == services.length;
 		ISubSystem[] subSystems = host.getSubSystems();
 		for (ISubSystem subsystem : subSystems) {
 			if (subsystem instanceof IRemoteFileSubSystem) {
 				return subsystem.isConnected();
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public boolean connect() {
 		return connect(false);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public boolean connect(boolean force) {
 		if (isConnected()) {
 			return true;
 		}
 		connectUnsafe(force);
 		return isConnected();
 	}
 
 	private void connectUnsafe(boolean force) {
 		boolean tryToConnect = isTryToConnect();
 		if (force || tryToConnect) {
 			RSEConnectionQueryManager.getInstance().connectTo(host);
 			setTryToConnect(false);
 		}
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public boolean isTryToConnect() {
 		boolean tryToConnect = true;
 		synchronized (RSEEnvironment.tryToConnect) {
 			final Boolean value = RSEEnvironment.tryToConnect.get(fs);
 			if (value != null) {
 				tryToConnect = value.booleanValue();
 			}
 		}
 		return tryToConnect;
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public void setTryToConnect(boolean value) {
 		synchronized (RSEEnvironment.tryToConnect) {
 			RSEEnvironment.tryToConnect.put(fs, Boolean.valueOf(value));
 		}
 	}
 
 }
