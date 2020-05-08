 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.lnf;
 
 import org.osgi.framework.Bundle;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.Platform;
 
 import org.eclipse.riena.core.exception.Failure;
 import org.eclipse.riena.core.util.Nop;
 import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;
 import org.eclipse.riena.ui.swt.utils.BundleUtil;
 
 /**
  * The {@code LnfManager} manages the current look and feel of the riena
  * (navigation) widgets.<br>
  * The {@code LnfManager} has the term of a default look-and-feel. The default
  * L&F is initially set by Riena to {@code RienaDefaultLnf}. But the default L&F
  * may also be overridden by frameworks based on Riena. That allows them to
 * define their own L&F.<br>
  * However, applications can again override the default L&F. This can be done by
  * either setting the system property "riena.lnf" or by specifying their L&F
 * with the methods {@code LnfManager.setLnf()}. Setting the L&F with the system
 * property overrules all other methods.<br>
  * When specifying the L&F class via a string (either system property or one of
  * the above mentioned methods) the string should conform to:
  * 
  * <pre>
  * lnf := [ Bundle-Symbolic-Name &quot;:&quot; ] LnF-Class-Name
  * </pre>
  * 
  * Where Bundle-Symbolic-Name allows to load the L&F class from the bundle with
  * this symbolic name.
  */
 public final class LnfManager {
 
 	/**
 	 * Allows setting of an application L&F.
 	 */
 	public static final String RIENA_LNF_SYSTEM_PROPERTY = "riena.lnf"; //$NON-NLS-1$
 
	private static RienaDefaultLnf defaultLnf = new RienaDefaultLnf();;
 	private static RienaDefaultLnf currentLnf;
 
 	private LnfManager() {
 		// cannot instantiated, because all methods are static
 	}
 
 	/**
 	 * Set a new default L&F.
 	 * 
 	 * @param defaultLnf
 	 *            new default L&F
 	 * @since 1.2
 	 */
 	public static void setDefaultLnf(RienaDefaultLnf defaultLnf) {
 		Assert.isNotNull(defaultLnf, "defaultLnf must not be null."); //$NON-NLS-1$
		dispose();
 		LnfManager.defaultLnf = defaultLnf;
 	}
 
 	/**
 	 * Set the new look and feel specified by the given class name (see class
 	 * header JavaDoc). New look and feel will be initialized and the old one
 	 * will be uninitialized.
 	 * 
 	 * @param currentLnfClassName
 	 *            a string specifying the name of the class that implements the
 	 *            look and feel
 	 */
 	public static void setLnf(String currentLnfClassName) {
		dispose();
		setLnf(createLnf(currentLnfClassName));
 	}
 
 	/**
 	 * Sets the new look and feel. New look and feel will be initialized and the
 	 * old one will be uninitialized.
 	 * 
 	 * @param currentLnf
 	 *            new look and feel to install.
 	 */
 	public static void setLnf(RienaDefaultLnf currentLnf) {
		dispose();
 		LnfManager.currentLnf = currentLnf;
 	}
 
 	/**
 	 * Returns the current look and feel. If no look and feel is set, the
 	 * default look and feel is returned.
 	 * 
 	 * @return current look and feel
 	 */
 	public static RienaDefaultLnf getLnf() {
		final String rienaLnf = System.getProperty(RIENA_LNF_SYSTEM_PROPERTY);
		if (currentLnf == null || rienaLnf != null) {
			RienaDefaultLnf lnf = createLnf(rienaLnf);
			currentLnf = lnf == null ? defaultLnf : lnf;
 		}
 		currentLnf.initialize();
 		return currentLnf;
 	}
 
 	/**
 	 * Return the current L&F class name.
 	 * 
 	 * @return the lnfClassName
 	 */
 	public static String getLnfClassName() {
 		return getLnf().getClass().getName();
 	}
 
 	/**
 	 * Disposes (uninitializes) the current look and feel.
 	 */
 	public static void dispose() {
 		if (currentLnf != null) {
 			currentLnf.uninitialize();
 			currentLnf = null;
 		}
 	}
 
 	private static final String BUNDLE_CLASS_NAME_SEPARATOR = ":"; //$NON-NLS-1$
 
 	private static RienaDefaultLnf createLnf(String lnfClassName) {
 		if (lnfClassName == null) {
 			return null;
 		}
 		Class<?> lnfClass = null;
 		if (lnfClassName.contains(BUNDLE_CLASS_NAME_SEPARATOR)) {
 			String[] parts = lnfClassName.split(BUNDLE_CLASS_NAME_SEPARATOR);
 			String bundleSymbolicName = parts[0];
 			String className = parts[1];
 			Bundle bundle = Platform.getBundle(bundleSymbolicName);
 			if (!BundleUtil.isReady(bundle)) {
 				throw new LnfManagerFailure(
 						"can't load LnfClass '" + className + "' from bundle " + bundleSymbolicName + " because bundle is not ready."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			}
 			try {
 				lnfClass = bundle.loadClass(className);
 			} catch (ClassNotFoundException e) {
 				throw new LnfManagerFailure(
 						"can't load LnfClass '" + className + "' from bundle " + bundleSymbolicName + ".", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			}
 		} else {
 			lnfClass = loadClass(lnfClassName);
 		}
 		try {
 			return (RienaDefaultLnf) lnfClass.newInstance();
 		} catch (Exception e) {
 			throw new LnfManagerFailure("can't create instance for LnfClass '" + lnfClass.getName() + ".", e); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 
 	private static Class<?> loadClass(String className) {
 		try {
 			return LnfManager.class.getClassLoader().loadClass(className);
 		} catch (ClassNotFoundException e) {
 			Nop.reason("try next"); //$NON-NLS-1$
 		}
 		try {
 			return Thread.currentThread().getContextClassLoader().loadClass(className);
 		} catch (Exception e) {
 			Nop.reason("try next"); //$NON-NLS-1$
 		}
 
 		throw new LnfManagerFailure("can't load LnfClass '" + className + "."); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	@SuppressWarnings("serial")
 	private static class LnfManagerFailure extends Failure {
 
 		/**
 		 * @param msg
 		 */
 		public LnfManagerFailure(String msg) {
 			super(msg);
 		}
 
 		/**
 		 * @param msg
 		 */
 		public LnfManagerFailure(String msg, Throwable thrown) {
 			super(msg, thrown);
 		}
 	};
 }
