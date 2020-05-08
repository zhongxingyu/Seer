 /*******************************************************************************
  * Copyright (c) 2010 Oracle.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * and Apache License v2.0 which accompanies this distribution. 
  * The Eclipse Public License is available at
  *     http://www.eclipse.org/legal/epl-v10.html
  * and the Apache License v2.0 is available at 
  *     http://www.opensource.org/licenses/apache2.0.php.
  * You may elect to redistribute this code under either of these licenses.
  *
  * Contributors:
  *     Hal Hildebrand - Initial JMX support 
  ******************************************************************************/
 
 package org.eclipse.gemini.management.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.Constants;
 import org.osgi.framework.startlevel.BundleStartLevel;
 import org.osgi.framework.wiring.BundleRevision;
 import org.osgi.framework.wiring.BundleWire;
 import org.osgi.framework.wiring.BundleWiring;
 
 /**
  * Static utilities
  * 
  */
 public final class BundleUtil {
	private static final String FRAGMENT_HOST_HEADER = "Fragment-Host";
 	
 	/**
 	 * Answer the string representation of the exported packages of the bundle
 	 * 
 	 * @param b
 	 * @param admin
 	 * @return the string representation of the exported packages of the bundle
 	 */
 	public static String[] getBundleExportedPackages(Bundle bundle) {
 		BundleWiring wiring = bundle.adapt(BundleWiring.class);
		if (wiring == null) {
			return new String[0];
		}
 		List<BundleWire> providedWires = wiring.getProvidedWires(BundleRevision.PACKAGE_NAMESPACE);
 		List<String> packages = new ArrayList<String>();
         for(BundleWire wire: providedWires){
         	String packageName = String.format("%s;%s", wire.getCapability().getAttributes().get(BundleRevision.PACKAGE_NAMESPACE), wire.getCapability().getAttributes().get(Constants.VERSION_ATTRIBUTE));
         	if(!packages.contains(packageName)){
         		packages.add(packageName);
         	}
         }
         return packages.toArray(new String[packages.size()]);
 	}
 
 	/**
 	 * Answer the string representation of the packages imported by a bundle
 	 * 
 	 * @param b
 	 * @param bundleContext
 	 * @param admin
 	 * @return the string representation of the packages imported by a bundle
 	 */
 	public static String[] getBundleImportedPackages(Bundle bundle) {
 		BundleWiring wiring = bundle.adapt(BundleWiring.class);
		if (wiring == null) {
			return new String[0];
		}
 		List<BundleWire> providedWires = wiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE);
         List<String> packages = new ArrayList<String>();
         for(BundleWire wire: providedWires){
             String packageName = String.format("%s;%s", wire.getCapability().getAttributes().get(BundleRevision.PACKAGE_NAMESPACE), wire.getCapability().getAttributes().get(Constants.VERSION_ATTRIBUTE));
             if(!packages.contains(packageName)){
                 packages.add(packageName);
             }
         }
         return packages.toArray(new String[packages.size()]);
 	}
 
 	/**
 	 * Answer the string representation of the bundle state
 	 * 
 	 * @param b
 	 * @return the string representation of the bundle state
 	 */
 	public static String getBundleState(Bundle b) {
 		switch (b.getState()) {
 			case Bundle.ACTIVE:
 				return "ACTIVE";
 			case Bundle.INSTALLED:
 				return "INSTALLED";
 			case Bundle.RESOLVED:
 				return "RESOLVED";
 			case Bundle.STARTING:
 				return "STARTING";
 			case Bundle.STOPPING:
 				return "STOPPING";
 			case Bundle.UNINSTALLED:
 				return "UNINSTALLED";
 			default:
 				return "UNKNOWN";
 		}
 	}
 
 	/**
 	 * Answer true if the bundle is a fragment
 	 * 
 	 * @param bundle
 	 * @param admin
 	 * @return true if the bundle is a fragment
 	 */
 	public static boolean isBundleFragment(Bundle bundle) {
 		BundleWiring wiring = bundle.adapt(BundleWiring.class);
		if (wiring != null) {
			return 0 != (wiring.getRevision().getTypes() & BundleRevision.TYPE_FRAGMENT);
		} else {
			return bundle.getHeaders().get(FRAGMENT_HOST_HEADER) != null;
		}
 	}
 
 	/**
 	 * Return the start level for the given bundle
 	 * 
 	 * @param bundle
 	 * @return
 	 */
 	public static int getBundleStartLevel(Bundle bundle) {
 		BundleStartLevel startLevel = bundle.adapt(BundleStartLevel.class);
 		return startLevel.getStartLevel();
 	}
 	
 	/**
 	 * Answer true if the bundle has been persitently started
 	 * 
 	 * @param bundle
 	 * @param sl
 	 * @return true if the bundle has been persitently started
 	 */
 	public static boolean isBundlePersistentlyStarted(Bundle bundle) {
 		BundleStartLevel startLevel = bundle.adapt(BundleStartLevel.class);
 		return startLevel.isPersistentlyStarted();
 	}
 	
 	/**
 	 * Answer true if the bundle has been persitently started
 	 * 
 	 * @param bundle
 	 * @param sl
 	 * @return true if the bundle has been persitently started
 	 */
 	public static boolean isBundleActivationPolicyUsed(Bundle bundle) {
 		BundleStartLevel startLevel = bundle.adapt(BundleStartLevel.class);
 		return startLevel.isActivationPolicyUsed();
 	}
 
 	/**
 	 * Answer true if the bundle is required
 	 * 
 	 * @param bundle
 	 * @param bc
 	 * @return true if the bundle is required
 	 */
 	public static boolean isRequired(Bundle bundle) {
 		BundleWiring wiring = bundle.adapt(BundleWiring.class);
		if (wiring != null) {
			return wiring.getProvidedWires(BundleRevision.BUNDLE_NAMESPACE).size() > 0;
		} else {
			return false;
		}
 	}
 
 	/**
 	 * Answer true if the bundle is pending removal
 	 * 
 	 * @param bundle
 	 * @param bc
 	 * @return true if the bundle is pending removal
 	 */
 	public static boolean isRemovalPending(Bundle bundle) {
         BundleWiring wiring = bundle.adapt(BundleWiring.class);
        if (wiring != null) {
        	return (!wiring.isCurrent()) && wiring.isInUse();
        } else {
        	return false;
        }
 	}
 
 }
