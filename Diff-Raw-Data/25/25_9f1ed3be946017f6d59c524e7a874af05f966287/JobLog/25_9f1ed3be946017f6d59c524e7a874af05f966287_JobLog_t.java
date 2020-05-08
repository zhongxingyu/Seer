 /*******************************************************************************
 * Copyright (c) 2011 AGETO Service GmbH and others.
  * All rights reserved.
  *
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html.
  *
  * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
  *******************************************************************************/
 package org.eclipse.gyrex.admin.ui.jobs.internal;
 
 /**
  *
  */
 public class JobLog {
 
 	private final String id;
 	private final boolean error;
 	private final boolean warning;
 
 	/**
 	 * Creates a new instance.
 	 * 
 	 * @param id
 	 * @param error
 	 * @param warning
 	 */
 	public JobLog(final String id, final boolean error, final boolean warning) {
 		super();
 		this.id = id;
 		this.error = error;
 		this.warning = warning;
 	}
 
 	/**
 	 * Returns the id.
 	 * 
 	 * @return the id
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * Returns the error.
 	 * 
 	 * @return the error
 	 */
 	public boolean isError() {
 		return error;
 	}
 
 	/**
 	 * Returns the warning.
 	 * 
 	 * @return the warning
 	 */
 	public boolean isWarning() {
 		return warning;
 	}
 
 }
