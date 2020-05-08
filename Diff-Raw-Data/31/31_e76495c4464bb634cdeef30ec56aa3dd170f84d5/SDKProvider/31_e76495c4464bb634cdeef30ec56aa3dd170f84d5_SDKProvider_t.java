 package org.nuxeo.ide.connect;
 
import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.equinox.security.storage.StorageException;
 import org.nuxeo.ide.sdk.IConnectProvider;
 import org.osgi.service.prefs.BackingStoreException;
 
 public class SDKProvider implements IConnectProvider {
 
     @Override
     public File[] getLibraries(IProject project, IProgressMonitor monitor) throws IOException, StorageException, BackingStoreException, CoreException {
        return ConnectPlugin.getStudioProvider().getLibraries(project);
     }
 
 }
