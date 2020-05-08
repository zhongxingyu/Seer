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
 package org.eclipse.riena.ui.core.resource;
 
 /**
  * Constants for icon sizes.
  */
public class IconSize {
 
 	/**
 	 * None special size.
 	 */
 	public static final IconSize NONE = new IconSize(""); //$NON-NLS-1$
 
 	/** Size a (16x16). */
 	public static final IconSize A16 = new IconSize("a"); //$NON-NLS-1$
 
 	/** Size b (22x22). */
 	public static final IconSize B22 = new IconSize("b"); //$NON-NLS-1$
 
 	/** Size c (32x32). */
 	public static final IconSize C32 = new IconSize("c"); //$NON-NLS-1$
 
 	/** Size d (48x48). */
 	public static final IconSize D48 = new IconSize("d"); //$NON-NLS-1$
 
 	/** Size e (64x64). */
 	public static final IconSize E64 = new IconSize("e"); //$NON-NLS-1$
 
 	/** Size f (128x128). */
 	public static final IconSize F128 = new IconSize("f"); //$NON-NLS-1$
 
 	private final String defaultMapping;
 
 	private IconSize(final String defaultMapping) {
 		this.defaultMapping = defaultMapping;
 	}
 
 	/**
 	 * Returns the mapping of this {@code IconSize}.
 	 * 
 	 * @return the filename character the icon size is mapped to
 	 */
 	public String getDefaultMapping() {
 		return defaultMapping;
 	}
 
 	@Override
 	public String toString() {
 		return getDefaultMapping();
 	}
 
 }
