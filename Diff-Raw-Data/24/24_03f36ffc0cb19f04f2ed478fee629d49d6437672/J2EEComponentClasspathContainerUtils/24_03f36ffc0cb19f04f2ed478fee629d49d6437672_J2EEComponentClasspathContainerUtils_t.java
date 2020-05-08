 package org.eclipse.jst.j2ee.internal.common.classpath;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jst.common.jdt.internal.classpath.FlexibleProjectContainer;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 
 public class J2EEComponentClasspathContainerUtils {
 
 	public static boolean getDefaultUseEARLibraries() {
 		return J2EEPlugin.getDefault().getJ2EEPreferences().getUseEARLibraries();
 	}
 
 	public static boolean getDefaultUseEARLibrariesJDTExport() {
 		return J2EEPlugin.getDefault().getJ2EEPreferences().getUseEARLibrariesJDTExport();
 	}
 	
 	public static boolean getDefaultUseWebAppLibraries() {
 		return J2EEPlugin.getDefault().getJ2EEPreferences().getUseWebLibaries();
 	}
 	
 	public static IClasspathContainer getInstalledContainer(IProject project, IPath containerPath) {
 		IJavaProject jproj = JavaCore.create(project);
 		IClasspathEntry entry = getInstalledContainerEntry(jproj, containerPath);
 		IClasspathContainer container = null;
 		if (entry != null) {
 			try {
 				container = JavaCore.getClasspathContainer(containerPath, jproj);
 			} catch (JavaModelException e) {
 				J2EEPlugin.getDefault().getLogger().logError(e);
 			}
 		}
 		return container;
 	}
 
 	public static J2EEComponentClasspathContainer getInstalledEARLibrariesContainer(IProject project) {
 		IClasspathContainer container = getInstalledContainer(project, J2EEComponentClasspathContainer.CONTAINER_PATH);
 		J2EEComponentClasspathContainer earLibrariesContainer = null;
 		if (null != container && container instanceof J2EEComponentClasspathContainer) {
 			earLibrariesContainer = (J2EEComponentClasspathContainer) container;
 		}
 		return earLibrariesContainer;
 	}
 
 	public static FlexibleProjectContainer getInstalledWebAppLibrariesContainer(IProject project){
 		IClasspathContainer container = getInstalledContainer(project, J2EEComponentClasspathUpdater.WEB_APP_LIBS_PATH);
 		FlexibleProjectContainer webAppLibrariesContainer = null;
 		if (null != container && container instanceof FlexibleProjectContainer) {
 			webAppLibrariesContainer = (FlexibleProjectContainer) container;
 		}
 		return webAppLibrariesContainer;
 	}
 	
 	public static IClasspathEntry getInstalledContainerEntry(IJavaProject jproj, IPath classpathContainerPath) {
		if (jproj.isOpen()) {
			try {
				IClasspathEntry[] cpes;
				cpes = jproj.getRawClasspath();
				for (int j = 0; j < cpes.length; j++) {
					final IClasspathEntry cpe = cpes[j];
					if (cpe.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
						if (cpe.getPath().equals(classpathContainerPath)) {
							return cpe; // entry found
						}
 					}
 				}
			} catch (JavaModelException e) {
				J2EEPlugin.getDefault().getLogger().logError(e);
 			}
 		}
 		// entry not found
 		return null;
 	}
 
 }
