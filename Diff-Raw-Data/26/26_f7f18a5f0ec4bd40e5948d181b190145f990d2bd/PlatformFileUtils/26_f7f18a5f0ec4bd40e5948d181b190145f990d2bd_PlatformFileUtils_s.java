 package org.eclipse.dltk.utils;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.osgi.service.datalocation.Location;
 
 public class PlatformFileUtils {
 	/**
 	 * Returns same file if not exist.
 	 */
 	public static File findAbsoluteOrEclipseRelativeFile(File file) {
 		String locationName = file.getPath();
 		if (!file.exists() && !file.isAbsolute()) {
 			String loc;
			Location location = Platform.getInstanceLocation();
			if (location == null) {
				location = Platform.getInstallLocation();
 			}
 			if (location != null) {
 				try {
 					loc = FileLocator.resolve(location.getURL()).getPath();
 					File nfile = new File(loc + File.separator + locationName);
					if( nfile.exists() ) {
 						return nfile;
 					}
 				} catch (IOException e) {
 					if (DLTKCore.DEBUG) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		return file;
 	}
 }
