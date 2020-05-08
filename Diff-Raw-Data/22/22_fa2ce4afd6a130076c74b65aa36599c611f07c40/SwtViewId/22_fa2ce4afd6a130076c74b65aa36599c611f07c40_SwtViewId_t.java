 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.presentation;
 
import org.eclipse.core.runtime.Assert;

 /**
  * ID of a SWT view. The ID consists of two IDs, ID and secondary ID.
  */
 public class SwtViewId {
 
 	private String id;
 	private String secondary;
 
 	/**
 	 * Creates a new instance of <code>SwtViewId</code> and sets the two IDs.
 	 * 
	 * @param id
	 *            - ID
	 * @param secondary
	 *            - secondary ID
 	 */
 	public SwtViewId(String id, String secondary) {
 		setId(id);
 		setSecondary(secondary);
 	}
 
 	/**
 	 * Creates a new instance of <code>SwtViewId</code> and sets the two IDs.
 	 * 
	 * @param compoundId
	 *            - compund ID
 	 * @see #getCompoundId()
 	 * @pre compoundId != null
 	 */
 	public SwtViewId(String compoundId) {
 
		Assert.isNotNull(compoundId);
 
 		String[] ids = compoundId.split(":"); //$NON-NLS-1$
		if (ids.length != 2) {
 			throw new RuntimeException("The compound ID is not correct!"); //$NON-NLS-1$
 		}
 		setId(ids[0]);
 		setSecondary(ids[1]);
 	}
 
 	/**
 	 * Returns the compound ID, a string with the ID and the secondary ID
 	 * seperated by a colon.
 	 * 
 	 * @return compound ID
 	 */
 	public String getCompoundId() {
 		return getId() + ":" + getSecondary(); //$NON-NLS-1$
 	}
 
 	/**
 	 * @return the id
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * @param id
 	 *            the id to set
 	 */
 	private void setId(String id) {
 		this.id = id;
 	}
 
 	/**
 	 * @return the secondary
 	 */
 	public String getSecondary() {
 		return secondary;
 	}
 
 	/**
 	 * @param secondary
 	 *            the secondary to set
 	 */
 	private void setSecondary(String secondary) {
 		this.secondary = secondary;
 	}
 
 }
