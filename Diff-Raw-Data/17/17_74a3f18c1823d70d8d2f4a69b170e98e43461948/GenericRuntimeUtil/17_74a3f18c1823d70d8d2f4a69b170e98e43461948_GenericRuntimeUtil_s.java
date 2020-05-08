 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.server.core;
 
 import org.eclipse.wst.server.core.IRuntime;
 /**
  * Utility methods for the generic J2EE runtime.
  * 
  * @since 1.0
  */
 public class GenericRuntimeUtil {
 	protected static final String RUNTIME_TYPE_ID = "org.eclipse.jst.server.core.runtimeType";
 
 	/**
 	 * Returns <code>true</code> if the given runtime is a generic J2EE runtime, and
 	 * <code>false</code> otherwise. The runtime may not be null.
 	 * 
 	 * @param runtime 
 	 * @return <code>true</code> if 
 	 */
 	public static boolean isGenericJ2EERuntime(IRuntime runtime) {
 		if (runtime == null)
 			throw new IllegalArgumentException();
 
 		return (runtime.getRuntimeType() != null &&
 			runtime.getRuntimeType().getId().startsWith(RUNTIME_TYPE_ID));
 	}
 }
