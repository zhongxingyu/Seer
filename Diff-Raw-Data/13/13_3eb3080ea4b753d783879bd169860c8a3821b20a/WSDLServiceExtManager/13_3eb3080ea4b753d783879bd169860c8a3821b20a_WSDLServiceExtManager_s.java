 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.webservices;
 
 public class WSDLServiceExtManager {
 	public static WSDLServiceHelper extension;
 
 	public static WSDLServiceHelper getServiceHelper() {
 		return extension;
 	}
 
 	public static void registerWSDLServiceHelper(WSDLServiceHelper ext) {
 		extension = ext;
 	}
 
 	public static void removeWSDLServiceHelper() {
 		extension = null;
 	}
 
 	public WSDLServiceExtManager() {
 		super();
 	}
 
 }
