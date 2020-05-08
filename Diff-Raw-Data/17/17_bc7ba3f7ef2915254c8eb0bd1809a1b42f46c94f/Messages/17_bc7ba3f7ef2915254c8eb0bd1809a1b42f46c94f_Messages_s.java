 /*******************************************************************************
  * Copyright (c) 2011 University of Illinois at Urbana-Champaign
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *  
  * Contributors: 
  * University of Illinois at Urbana-Champaign - initial API and implementation
  *******************************************************************************/
 package edu.illinois.parallelism.openmp.refactoring.padscalarvariables;
 
 import org.eclipse.osgi.util.NLS;
 

 public class Messages extends NLS {
 	private static final String BUNDLE_NAME = "edu.illinois.parallelism.openmp.refactoring.padscalarvariables.messages"; //$NON-NLS-1$
 	public static String PadScalarVariableRefactoring_humanName;
 	public static String PadScalarVariablesRunner_unavailableDialogInfo;
 	public static String PadScalarVariablesRunner_unavailableDialogTitle;
 	public static String PadScalarVariablesUserInputWizardPage_deselectAllButtonLabel;
 	public static String PadScalarVariablesUserInputWizardPage_dialogMessage;
 	public static String PadScalarVariablesUserInputWizardPage_dialogTitle;
 	public static String PadScalarVariablesUserInputWizardPage_selectAllButtonLabel;
 	public static String PadScalarVariablesUserInputWizardPage_wizardPageTitle;
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	private Messages() {
 	}
 }
