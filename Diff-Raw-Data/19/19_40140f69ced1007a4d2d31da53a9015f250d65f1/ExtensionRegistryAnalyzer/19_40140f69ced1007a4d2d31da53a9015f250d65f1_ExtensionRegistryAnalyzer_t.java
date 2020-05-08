 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.core.test;
 
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.RegistryFactory;
 
 /**
  * Helper class for analyzing the current state of the {@code
  * IExtensionRegistry}.
  */
 public final class ExtensionRegistryAnalyzer {
 
 	private ExtensionRegistryAnalyzer() {
 		// utility
 	}
 
 	/**
 	 * Dump the whole extension registry to system.out
 	 * 
 	 * @param extensionPointPrefix
 	 *            considering only extension points that have this prefix or
 	 *            null for all
 	 */
 	public static void dumpRegistry(final String extensionPointPrefix) {
 		dumpRegistry(extensionPointPrefix, Integer.MAX_VALUE);
 	}
 
 	/**
 	 * Dump the whole extension registry up to the specified nesting depth.
 	 * 
 	 * @param extensionPointPrefix
 	 *            considering only extension points that have this prefix or
 	 *            null for all
 	 * @param depth
 	 *            nesting depth
 	 */
 	public static void dumpRegistry(final String extensionPointPrefix, final int depth) {
 		System.out.println("Registry:"); //$NON-NLS-1$
 		System.out.println("========="); //$NON-NLS-1$
 		final IExtensionPoint[] extensionPoints = RegistryFactory.getRegistry().getExtensionPoints();
 		Arrays.sort(extensionPoints, new Comparator<IExtensionPoint>() {
 			public int compare(final IExtensionPoint ep1, final IExtensionPoint ep2) {
 				return ep1.getUniqueIdentifier().compareTo(ep2.getUniqueIdentifier());
 			}
 		});
 		for (final IExtensionPoint extensionPoint : extensionPoints) {
 			if (extensionPointPrefix != null && !extensionPoint.getUniqueIdentifier().startsWith(extensionPointPrefix)) {
 				continue;
 			}
 			System.out.println(extensionPoint.getUniqueIdentifier() + ":"); //$NON-NLS-1$
 			dumpExtensions(extensionPoint.getExtensions(), depth);
 		}
 	}
 
 	private static void dumpExtensions(final IExtension[] extensions, final int depth) {
 		for (final IExtension extension : extensions) {
 			System.out.println(indent(0) + "uid=" + extension.getUniqueIdentifier() + " bundle=" //$NON-NLS-1$ //$NON-NLS-2$
 					+ extension.getContributor().getName() + " "); //$NON-NLS-1$
 			dumpConfigurationElements(1, depth, extension.getConfigurationElements());
 		}
 	}
 
 	private static void dumpConfigurationElements(final int level, final int depth,
 			final IConfigurationElement[] elements) {
 		if (elements.length == 0 || level == depth) {
 			return;
 		}
 		for (final IConfigurationElement element : elements) {
 			System.out.println(indent(level) + "<" + element.getName() + " " + getAttributes(element) + "/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			dumpConfigurationElements(level + 1, depth, element.getChildren());
 		}
 	}
 
 	private static String indent(final int level) {
 		final StringBuilder bob = new StringBuilder();
 		for (int l = 0; l < level; l++) {
 			bob.append("   "); //$NON-NLS-1$
 		}
 		bob.append(" - "); //$NON-NLS-1$
 		return bob.toString();
 	}
 
 	/**
 	 * Print the extension registry path set to system.out
 	 * 
 	 * @param set
 	 */
 	public static void print(final Set<String> set) {
 		for (final String string : set) {
 			System.out.println(string);
 		}
 	}
 
 	/**
 	 * Get the whole extension registry as a set of paths.
 	 * 
 	 * @param extensionPointPrefix
 	 *            considering only extension points that have this prefix or
 	 *            null for all
 	 * 
 	 * @return
 	 */
 	public static Set<String> getRegistryPaths(final String extensionPointPrefix) {
 		final Set<String> result = new HashSet<String>();
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		if (registry != null) { // is null when running without the workbench (plain junit test)
			final IExtensionPoint[] extensionPoints = registry.getExtensionPoints();
			for (final IExtensionPoint extensionPoint : extensionPoints) {
				if (extensionPointPrefix != null
						&& !extensionPoint.getUniqueIdentifier().startsWith(extensionPointPrefix)) {
					continue;
				}
				final String path = extensionPoint.getUniqueIdentifier() + ": "; //$NON-NLS-1$
				getExtensionsPaths(result, path, extensionPoint.getExtensions());
 			}
 		}
 		return result;
 	}
 
 	private static void getExtensionsPaths(final Set<String> result, final String path, final IExtension[] extensions) {
 		for (final IExtension extension : extensions) {
 			final String subPath = path + "uid=" + extension.getUniqueIdentifier() + " bundle=" //$NON-NLS-1$ //$NON-NLS-2$
 					+ extension.getContributor().getName() + " "; //$NON-NLS-1$
 			getConfigurationElementsPaths(result, subPath, extension.getConfigurationElements());
 		}
 	}
 
 	private static void getConfigurationElementsPaths(final Set<String> result, final String path,
 			final IConfigurationElement[] elements) {
 		if (elements.length == 0) {
 			if (!result.add(path)) {
 				System.err.println("Error while collecting registry paths. Adding " + path + " twice."); //$NON-NLS-1$ //$NON-NLS-2$
 				// Commented, because that pollutes the log!
 				//				for (final String str : result) {
 				//					if (path.equals(str)) {
 				//						System.err.println(str);
 				//					} else {
 				//						System.out.println(str);
 				//					}
 				//				}
 			}
 			return;
 		}
 		for (final IConfigurationElement element : elements) {
 			final String subPath = path + "<" + element.getName() + " " + getAttributes(element) + "/>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			getConfigurationElementsPaths(result, subPath, element.getChildren());
 		}
 	}
 
 	private static String getAttributes(final IConfigurationElement element) {
 		final StringBuilder bob = new StringBuilder();
 		for (final String attribute : element.getAttributeNames()) {
 			bob.append(attribute).append("=").append(element.getAttribute(attribute)).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		bob.setLength(Math.max(0, bob.length() - 1));
 		return bob.toString();
 	}
 
 	/**
 	 * Calculate the symmetric difference between the two sets.
 	 * 
 	 * @see <a
 	 *      href="http://en.wikipedia.org/wiki/Symmetric_difference">Symmetric_difference</a>
 	 * 
 	 * @param <T>
 	 * @param set1
 	 * @param set2
 	 * @return
 	 */
 	public static <T> Set<T> symmetricDiff(final Set<T> set1, final Set<T> set2) {
 		final Set<T> result = union(set1, set2);
 		result.removeAll(intersect(set1, set2));
 		return result;
 	}
 
 	public static <T> Set<T> union(final Set<T> set1, final Set<T> set2) {
 		final Set<T> union = new HashSet<T>(set1);
 		union.addAll(set2);
 		return union;
 	}
 
 	public static <T> Set<T> intersect(final Set<T> set1, final Set<T> set2) {
 		final Set<T> intersection = new HashSet<T>(set1);
 		intersection.retainAll(set2);
 		return intersection;
 	}
 
 }
