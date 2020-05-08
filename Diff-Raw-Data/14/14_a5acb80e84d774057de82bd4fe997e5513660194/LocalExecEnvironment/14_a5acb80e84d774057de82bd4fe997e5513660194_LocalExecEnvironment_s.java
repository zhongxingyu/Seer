 package org.eclipse.dltk.internal.launching.execution;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
import java.security.AccessController;
 import java.util.Map;
 import java.util.Random;
 
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.environment.IDeployment;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IExecutionEnvironment;
 import org.eclipse.dltk.core.internal.environment.LocalEnvironment;
 
import sun.security.action.GetPropertyAction;

 public class LocalExecEnvironment implements IExecutionEnvironment {
 	private static IPath temp;
 	private static int counter = -1;
 
 	public IDeployment createDeployment() {
 		try {
 			IPath rootPath = getTempDirPath().append(
 					getTempName("dltk", ".tmp")); //$NON-NLS-1$ //$NON-NLS-2$
 			URI rootUri = createLocalURI(rootPath);
 			return new EFSDeployment(LocalEnvironment.getInstance(), rootUri);
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG)
 				e.printStackTrace();
 		}
 		return null;
 	}
 
 	private String getTempName(String prefix, String suffix) {
 		if (counter == -1) {
 			counter = new Random().nextInt() & 0xffff;
 		}
 		counter++;
 		return prefix + Integer.toString(counter) + suffix;
 	}
 
 	private URI createLocalURI(IPath path) {
 		return EFS.getLocalFileSystem().getStore(path).toURI();
 	}
 
 	private static IPath getTempDirPath() {
 		if (temp == null) {
			GetPropertyAction a = new GetPropertyAction("java.io.tmpdir"); //$NON-NLS-1$
			File tempFile = new File(
					((String) AccessController.doPrivileged(a)));
 			try {
 				temp = new Path(tempFile.getCanonicalPath());
 			} catch (IOException e) {
				temp = new Path(tempFile.getAbsolutePath());
 			}
 		}
 		return temp;
 	}
 
 	public Process exec(String[] cmdLine, IPath workingDir, String[] environment)
 			throws CoreException {
 		File workingDirFile = null;
 		if (workingDir != null) {
 			workingDirFile = workingDir.toFile();
 		}
 		return DebugPlugin.exec(cmdLine, workingDirFile, environment);
 	}
 
 	public Map getEnvironmentVariables() {
 		return DebugPlugin.getDefault().getLaunchManager()
 				.getNativeEnvironmentCasePreserved();
 	}
 
 	public IEnvironment getEnvironment() {
 		return LocalEnvironment.getInstance();
 	}
 
 	public boolean isValidExecutableAndEquals(String possibleName, IPath path) {
 		if (Platform.getOS().equals(Platform.OS_WIN32)) {
 			possibleName = possibleName.toLowerCase();
 			String fName = path.removeFileExtension().toString().toLowerCase();
 			String ext = path.getFileExtension();
 			if (possibleName.equals(fName)
 					&& ("exe".equalsIgnoreCase(ext) || "bat".equalsIgnoreCase(ext))) { //$NON-NLS-1$ //$NON-NLS-2$
 				return true;
 			}
 		} else {
 			String fName = path.lastSegment();
 			if (fName.equals(possibleName)) {
 				return true;
 			}
 		}
 		return false;
 	}
 }
