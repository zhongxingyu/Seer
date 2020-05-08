 /*
  * Copyright (c) 2005, 2006 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.common.codegen;
 
 import org.eclipse.osgi.util.NLS;
 
 public class GeneratorBaseMessages extends NLS {
 	private static final String BUNDLE_NAME = "org.eclipse.gmf.internal.common.codegen.messages"; //$NON-NLS-1$
 
 	private GeneratorBaseMessages() {
 	}
 
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, GeneratorBaseMessages.class);
 	}
 
 	public static String merge;
 
 	public static String interrupted;
 
 	public static String unexpected;
 
 	public static String problems; 
 
 	public static String initproject;
 
 	public static String start;
 }
