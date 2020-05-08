 package org.eclipse.jem.internal.proxy.core;
 
 import java.util.Map;
 
 
 /**
  * Result from {@link ProxyPlugin#processContributionExtensionPoint(String)}
  * 
  * @since 1.0.0
  */
 public class ContributorExtensionPointInfo {
 	
 	/**
 	 * Contributions that are based upon container paths that are built up from
 	 * the extension point.
 	 * 
 	 * @since 1.2.0
 	 */
 	public ContainerPathContributionMapping containerPathContributions;
 
 	ContributorExtensionPointInfo() {
 		// Not meant to be instantiated or subclassed outside of ProxyPlugin.
 	}
 	
 	/**
 	 * Map of plugin ids (String) to contributions (IConfigurationElement[]) that was found with that id. For each plugin,
 	 * the contributions will be listed in plugin prereq order.
 	 */
 	public Map pluginToContributions;
 
 	
 }
