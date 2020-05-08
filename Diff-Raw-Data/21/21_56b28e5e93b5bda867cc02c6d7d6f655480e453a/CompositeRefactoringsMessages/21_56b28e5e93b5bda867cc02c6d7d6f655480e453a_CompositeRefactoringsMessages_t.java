 /**
  * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 
 package edu.illinois.compositerefactorings.messages;
 
 import org.eclipse.osgi.util.NLS;
 
 public class CompositeRefactoringsMessages extends NLS {
 
 	private static final String BUNDLE_NAME= "edu.illinois.compositerefactorings.messages.compositerefactoringsmessages"; //$NON-NLS-1$
 
 	public static String ReplaceTypeBySupertypeInInstanceOf_description;
 
 	public static String ReplaceTypeBySupertypeInVariableDeclarations_description;
 
 	public static String CreateNewSuperclass_description;
 
 	public static String MoveToImmediateSuperclass_description;
 
	public static String MoveTypeToNewFile_description;

 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, CompositeRefactoringsMessages.class);
 	}
 
 	private CompositeRefactoringsMessages() {
 	}
 }
