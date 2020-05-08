 package org.jboss.tools.portlet.core.internal;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;
 import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponentType;
 import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponentVersion;
 import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.internal.facets.RuntimeFacetComponentProviderDelegate;
 import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
 import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
 import org.jboss.tools.portlet.core.IPortletConstants;
 import org.jboss.tools.portlet.core.Messages;
 import org.jboss.tools.portlet.core.PortletCoreActivator;
 
 public class PortletRuntimeComponentProvider extends
 		RuntimeFacetComponentProviderDelegate {
 
 	private static final IRuntimeComponentType PORTAL_TYPE = RuntimeManager
 			.getRuntimeComponentType("org.jboss.tools.portlet.core.runtime.component"); //$NON-NLS-1$
 
 	private static final IRuntimeComponentVersion PORTAL_VERSION_1 = PORTAL_TYPE
 			.getVersion("1.0"); //$NON-NLS-1$
 
 	public List<IRuntimeComponent> getRuntimeComponents(final IRuntime runtime) {
		List<IRuntimeComponent> components = new ArrayList<IRuntimeComponent>();
		if (runtime != null && runtime.getLocation() != null) {
			File location = runtime.getLocation().toFile();

			if (isPortalPresent(location, runtime)) {
				final IRuntimeComponent portalComponent = RuntimeManager
						.createRuntimeComponent(PORTAL_VERSION_1, null);
				components.add(portalComponent);
			}
 		}
 		return components;
 	}
 
 	
 	private static boolean isPortalPresent(final File location, IRuntime runtime) {
 		boolean check = PortletCoreActivator.getDefault().getPluginPreferences().getBoolean(PortletCoreActivator.CHECK_RUNTIMES);
 		if (!check) {
 			return true;
 		}
 		
 		IJBossServerRuntime jbossRuntime = (IJBossServerRuntime)runtime.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
 		if (jbossRuntime != null) {
 			// JBoss Portal server
 			IPath jbossLocation = runtime.getLocation();
 			IPath configPath = jbossLocation.append(IJBossServerConstants.SERVER).append(jbossRuntime.getJBossConfiguration());
 			File configFile = configPath.toFile();
 			// JBoss Portal server
 			if (exists(configFile,
 					IPortletConstants.SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_SAR)) {
 				return true;
 			}
 			// JBoss Portal clustering server
 			if (exists(configFile,
 					IPortletConstants.SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_HA_SAR)) {
 				return true;
 			}
 			// JBoss portletcontainer
 			if (exists(configFile,
 					IPortletConstants.SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL)) {
 				return true;
 			}
 		}
 		// Tomcat portletcontainer
 		File tomcatLib = new File(location,IPortletConstants.TOMCAT_LIB);
 		if (tomcatLib.exists() && tomcatLib.isDirectory()) {
 			String[] files = tomcatLib.list(new FilenameFilter() {
 
 				public boolean accept(File dir, String name) {
 					if (name.startsWith(IPortletConstants.PORTLET_API) && name.endsWith(IPortletConstants.JAR)) {
 						return true;
 					}
 					return false;
 				}
 				
 			});
 			return files.length > 0;
 		}
 		
 		return false; 
 	}
 
 
 	private static boolean exists(final File location,String portalDir) {
 		if (Platform.getOS().equals(Platform.OS_WIN32)) {
 			portalDir = portalDir.replace("/", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		File file = new File(location,portalDir);
 		return file.exists();
 	}
 }
