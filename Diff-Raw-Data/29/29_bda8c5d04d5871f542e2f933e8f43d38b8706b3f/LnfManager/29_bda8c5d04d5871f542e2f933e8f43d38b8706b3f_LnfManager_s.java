 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.lnf;
 
 import javax.swing.Renderer;
 
 import org.eclipse.swt.widgets.Display;
 
 import org.eclipse.riena.core.singleton.SessionSingletonProvider;
 import org.eclipse.riena.core.singleton.SingletonProvider;
 import org.eclipse.riena.internal.ui.swt.lnf.LnfManagerInternal;
 import org.eclipse.riena.ui.swt.lnf.renderer.EmbeddedTitlebarRenderer;
 import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;
 
 /**
  * The {@code LnfManager} manages the current look and feel of the riena
  * (navigation) widgets.
  * <p>
  * The {@code LnfManager} has the term of a default look-and-feel (L&F). The
  * default L&F is initially set by Riena to {@code RienaDefaultLnf}. But the
  * default L&F may also be overridden by frameworks based on Riena. That allows
  * them to define their own default L&F.
  * <p>
  * <b>Note:</b> Changing the L&F within a running application might result in
  * system resources such as colors, fonts and images which will not be disposed.
  * <p>
  * However, applications can again override the default L&F. This can be done by
  * either setting the system property "riena.lnf" or by specifying their L&F
  * with the methods {@code LnfManager.setLnf()}.
  * <p>
  * When specifying the L&F class via a string (either system property or one of
  * the above mentioned methods) the string should conform to:
  * 
  * <pre>
  * lnf := [ Bundle-Symbolic-Name &quot;:&quot; ] LnF-Class-Name
  * </pre>
  * 
  * Where Bundle-Symbolic-Name allows to load the L&F class from the bundle with
  * this symbolic name.<br>
  * If the Bundle-Symbolic-Name is omitted the {@code LnfManager} tries to load
  * the Lnf class with the {LnfMangager}'s class loader.
  */
 public final class LnfManager {
 
 	/**
 	 * Allows setting of an application L&F.
 	 * 
 	 * @since 1.2
 	 */
 	public static final String RIENA_LNF_SYSTEM_PROPERTY = "riena.lnf"; //$NON-NLS-1$
 
 	/**
 	 * The LnFManager has to be an SessionSingleton, because it he LnF that
 	 * holds a Map of {@link Renderer}. The Renderer get passed a UIControl that
 	 * was created with a {@link Display} from a specific user. Some Renderer
 	 * like {@link EmbeddedTitlebarRenderer} keep the UIControl to redraw later.
 	 */
 	private final static SingletonProvider<LnfManagerInternal> LMI = new SessionSingletonProvider<LnfManagerInternal>(
 			LnfManagerInternal.class);
 
 	private LnfManager() {
 		// cannot instantiated, because all methods are static
 	}
 
 	/**
 	 * Set a new default look and feel. See class header JavaDoc for details.
 	 * 
 	 * @param defaultLnf
 	 *            new default L&F
 	 * @since 1.2
 	 */
 	public static void setDefaultLnf(final RienaDefaultLnf defaultLnf) {
 		LMI.getInstance().setDefaultLnf(defaultLnf);
 	}
 
 	/**
 	 * Set the new look and feel specified by the given class name (see class
 	 * header JavaDoc).
 	 * <p>
 	 * <b>Note:</b> Changing the L&F in a running application might result in
 	 * system resources such as colors, fonts and images which will not be
 	 * disposed.
 	 * 
 	 * @param currentLnfClassName
 	 *            a string specifying the name of the class that implements the
 	 *            look and feel
 	 */
 	public static void setLnf(final String currentLnfClassName) {
 		LMI.getInstance().setLnf(currentLnfClassName);
 	}
 
 	/**
 	 * Sets the new look and feel.
 	 * <p>
 	 * If this is set, it will override the default look and feel. See class
 	 * header JavaDoc for details.
 	 * <p>
 	 * <b>Note:</b> Changing the L&F in a running application might result in
 	 * system resources such as colors, fonts and images which will not be
 	 * disposed.
 	 * 
 	 * @param currentLnf
 	 *            new look and feel to install.
 	 */
 	public static synchronized void setLnf(final RienaDefaultLnf currentLnf) {
 		LMI.getInstance().setLnf(currentLnf);
 	}
 
 	/**
 	 * Returns the current look and feel. If no look and feel is set, the
 	 * default look and feel is returned.
 	 * 
 	 * @return current look and feel
 	 */
 	public static RienaDefaultLnf getLnf() {
 		return LMI.getInstance().getLnf();
 	}
 
 	/**
	 * Return the current L&F class name.
	 * 
	 * @return the lnfClassName
	 * 
	 * @deprecated This was only needed for unit tests!! We should abandon it.
	 */
	@Deprecated
	public static String getLnfClassName() {
		return LMI.getInstance().getLnfClassName();
	}

	/**
 	 * Disposes (uninitializes) the current look and feel.
 	 */
 	public static void dispose() {
 		LMI.getInstance().dispose();
 	}
 
 }
