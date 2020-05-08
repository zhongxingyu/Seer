 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 336075 - DiagramEditor accepts URIEditorInput
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.examples.common;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  *
  */
 public class Messages extends NLS {
 	private static final String BUNDLE_NAME = "org.eclipse.graphiti.examples.common.messages"; //$NON-NLS-1$
 	public static String CompartmentPattern_CompartmentChangesText;
 	public static String CreateDiagramWizard_DiagramNameField;
 	public static String CreateDiagramWizard_DiagramTypeField;
 	public static String CreateDiagramWizard_ErrorOccuredTitle;
 	public static String CreateDiagramWizard_NoProjectFoundError;
 	public static String CreateDiagramWizard_NoProjectFoundErrorTitle;
 	public static String CreateDiagramWizard_OpeningEditorError;
 	public static String CreateDiagramWizard_WizardTitle;
 	public static String DiagramNameWizardPage_PageDescription;
 	public static String DiagramNameWizardPage_PageTitle;
 	public static String DiagramNameWizardPage_Message;
 	public static String DiagramNameWizardPage_Label;
 	public static String DiagramsNode_DiagramNodeTitle;
 	public static String DiagramTypeWizardPage_DiagramTypeField;
 	public static String DiagramTypeWizardPage_PageDescription;
 	public static String DiagramTypeWizardPage_PageTitle;
 	public static String EClassesNode_EClassesNodeName;
 	public static String ExampleUtil_ChooseColorTitel;
 	public static String FileService_ErrorMessageCause;
 	public static String FileService_ErrorMessageStart;
 	public static String FileService_ErrorMessageUri;
 	public static String GFEmfLabelProvider_DiagramText;
 	public static String GFEmfLabelProvider_NameNotAvailable;
	public static String GraphicsTestPreferencePage_ActivateDebugLevelField;
	public static String GraphicsTestPreferencePage_ActivateInfoLevelField;
	public static String GraphicsTestPreferencePage_GraphitiTracingTitle;
 	public static String GraphicsTestPreferencePage_AnimationStepsField;
 	public static String GraphicsTestPreferencePage_ContextButtonPadField;
 	public static String GraphicsTestPreferencePage_CpuDurationField;
 	public static String GraphicsTestPreferencePage_DebugActionsField;
 	public static String GraphicsTestPreferencePage_DebuggingTitle;
 	public static String GraphicsTestPreferencePage_GenericOutlineField;
 	public static String GraphicsTestPreferencePage_GenericPropertyField;
 	public static String GraphicsTestPreferencePage_InternalTitle;
 	public static String GraphicsTestPreferencePage_InvisibleRectangleField;
 	public static String GraphicsTestPreferencePage_PageDescription;
 	public static String GraphicsTestPreferencePage_PolylineRoundingField;
 	public static String GraphicsTestPreferencePage_ProgressDialogField;
 	public static String GraphicsTestPreferencePage_RecursiveUpdateCheckField;
 	public static String GraphicsTestPreferencePage_VisualPlayingTitle;
 	public static String GraphicsTestPreferencePage_VisualStateRenderingField;
 	public static String OpenDiagramFromFileHandler_DialogTitle;
 	public static String OpenDiagramFromFileHandler_OpenEditorError;
 	public static String RenameActionProvider_ProvideNameDescription;
 	public static String RenameActionProvider_ProvideNameTitle;
 	public static String RenameActionProvider_RenameEClassText;
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	private Messages() {
 	}
 }
