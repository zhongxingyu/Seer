 /* 
  * CDDL HEADER START 
  * 
  * The contents of this file are subject to the terms of the 
  * Common Development and Distribution License (the "License"). 
  * You may not use this file except in compliance with the License. 
  * 
  * You can obtain a copy of the license at LICENSE.txt 
  * or at http://www.opensource.org/licenses/cddl1.php. 
  * See the License for the specific language governing permissions 
  * and limitations under the License. 
  * 
  * When distributing Covered Code, include this CDDL HEADER in each 
  * file and include the License file at LICENSE.txt. 
  * If applicable, add the following below this CDDL HEADER, with the 
  * fields enclosed by brackets "[]" replaced with your own identifying 
  * information: Portions Copyright [yyyy] [name of copyright owner] 
  * 
  * CDDL HEADER END 
  */ 
 
 /* 
  * Copyright 2005, 2006 Julien Ponge. All rights reserved. 
  * Use is subject to license terms. 
  */ 
 
 package fr.isima.ponge.wsprotocol.gefeditor;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS
 {
     private static final String BUNDLE_NAME = "fr.isima.ponge.wsprotocol.gefeditor.messages"; //$NON-NLS-1$
 
     private Messages()
     {
     }
 
     static
     {
         // initialize resource bundle
         NLS.initializeMessages(BUNDLE_NAME, Messages.class);
     }
 
     public static String deleteState;
 
     public static String addBendpoint;
 
     public static String moveResize;
 
     public static String reconnect;
 
     public static String createState;
 
     public static String deleteBendpoint;
 
     public static String createOperation;
 
     public static String moveBendpoint;
 
     public static String messageName;
 
     public static String messagePolarity;
 
     public static String input;
 
     public static String output;
 
     public static String operationKind;
     
     public static String implicit;
     
     public static String explicit;
     
     public static String none;
 
     public static String notAvailableAbbr;
 
     public static String snapToGrid;
 
     public static String showGrid;
 
     public static String viewMenu;
 
     public static String loadTimeErrorMessage;
 
     public static String newProtocolNameInModel;
 
     public static String saveTimeErrorMessage;
 
     public static String savingTask;
 
     public static String toolsPaletteGroup;
 
     public static String statesPaletteGroup;
 
     public static String initialState;
 
     public static String createInitialState;
 
     public static String normalState;
 
     public static String createNormalState;
 
     public static String finalState;
 
     public static String createFinalState;
 
     public static String operationsPaletteGroup;
 
     public static String inputOperation;
 
     public static String createInputOperation;
 
     public static String outputOperation;
 
     public static String createOutputOperation;
 
     public static String implicitOperation;
 
     public static String createImplicitOperation;
 
     public static String newProtocolWizardPageTitle;
 
     public static String newProtocolWizardPageDescription;
 
     public static String containerEntry;
 
     public static String browse;
 
     public static String fileNameEntry;
 
     public static String newProtocolFilename;
 
     public static String selectNewFileContainer;
 
     public static String fileContainerMustBeSpecified;
 
     public static String fileContainerMustExist;
 
     public static String projectMustBeWritable;
 
     public static String filenameMustBeSpecified;
 
     public static String filenameMustBeValid;
 
     public static String fileExtensionMustBeValid;
 
     public static String error;
 
     public static String creatingTask;
 
     public static String containerException1;
 
     public static String containerException2;
 
     public static String openingFileForEditingTask;
 
     public static String newProtocolModelName;
 
     public static String protocolNameProperty;
 
     public static String wsdlUrlProperty;
 
     public static String stateNameProperty;
 
     public static String initialStateProperty;
 
     public static String finalStateProperty;
 
     public static String alignMenuEntry;
 }
