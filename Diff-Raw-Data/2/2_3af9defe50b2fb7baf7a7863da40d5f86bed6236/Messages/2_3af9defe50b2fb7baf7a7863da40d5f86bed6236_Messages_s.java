 /*******************************************************************************
  * Copyright (c) 2011-2013 EclipseSource Muenchen GmbH and others.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Edgar Mueller - initial API and implementation
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.model.impl.api;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * API implementation related messages.
  * 
  * @author emueller
 * 
  */
 public final class Messages extends NLS {
 	private static final String BUNDLE_NAME = "org.eclipse.emf.emfstore.internal.client.model.impl.api.messages"; //$NON-NLS-1$
 	public static String ESLocalProjectImpl_No_Usersession_Found;
 	public static String ESRemoteProjectImpl_Fetching_Project;
 	public static String ESRemoteProjectImpl_Fetching_Title;
 	public static String ESRemoteProjectImpl_Server_Returned_Null_Project;
 	public static String ESServerImpl_Creating_Project;
 	public static String ESServerImpl_Invalid_Userssesion;
 	public static String ESWorkspaceImpl_Server_Not_Found;
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	private Messages() {
 	}
 }
