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
 package org.eclipse.wst.server.core;
 
 import org.eclipse.core.runtime.IAdaptable;
 /**
  * A runtime target handler is used to apply some properties to a project
  * this is being targeted to a given runtime. For instance, the handler
  * might update the classpath of a Java project to include the runtime's
  * classes, add validation for the given runtime, or restrict the type of
  * resources that can be created.
  * 
  * <p>This interface is not intended to be implemented by clients.</p>
  * <p>
  * <b>Provisional API:</b> This class/interface is part of an interim API that is still under development and expected to 
  * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
  * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken 
  * (repeatedly) as the API evolves.
  * </p>
  * 
  * @plannedfor 1.0
  */
 public interface IRuntimeTargetHandler extends IAdaptable {
 	/**
 	 * Returns the id of this runtime target handler.
 	 * Each known runtime target handler has a distinct id. 
 	 * Ids are intended to be used internally as keys; they are not
 	 * intended to be shown to end users.
 	 * 
 	 * @return the runtime target handler id
 	 */
 	public String getId();
 
 	/**
 	 * Returns <code>true</code> if this runtime target handler supports
 	 * (can work with) the given runtime.
 	 * 
 	 * @param runtimeType a runtime type
 	 * @return <code>true</code> if the handler can accept the given runtime type,
 	 *    and <code>false</code> otherwise
 	 */
 	public boolean supportsRuntimeType(IRuntimeType runtimeType);
 }
