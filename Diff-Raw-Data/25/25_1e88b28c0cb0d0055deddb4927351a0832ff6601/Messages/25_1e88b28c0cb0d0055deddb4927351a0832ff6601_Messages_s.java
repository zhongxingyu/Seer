 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.cmd.i18n;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * @author BREDEX GmbH
  * @created 10.12.2010
  */
 public class Messages extends NLS {
     private static final String BUNDLE_NAME = "org.eclipse.jubula.client.cmd.i18n.messages"; //$NON-NLS-1$
     
     public static String AnErrorOcurred;
     public static String ClientAutconfigOpt;
     public static String ClientAutIdOpt;
     public static String ClientAutoScreenshot;
     public static String ClientBuildingReport;
     public static String ClientCalculating;
     public static String ClientCmdArgs;
     public static String ClientCollectingInformation;
     public static String ClientConfigFile;
     public static String ClientConfigOpt;
     public static String ClientDataFile;
     public static String ClientDbpwOpt;
     public static String ClientDbschemeOpt;
     public static String ClientDburlOpt;
     public static String ClientDbuserOpt;
     public static String ClientDisconnectFromServerMessage;
     public static String ClientDisconnectFromServerTitle;
     public static String ClientError;
     public static String ClientestTestcase;
     public static String ClientExitCode;
     public static String ClientExitCodeTask;
     public static String ClientHelpOpt;
     public static String ClientJobFileError;
     public static String ClientLanguageOpt;
     public static String ClientMissingArgs;
     public static String ClientMonitoringInfoDialog;
     public static String ClientNoRunOpt;
     public static String ClientogViewerName;
     public static String ClientPortOpt;
     public static String ClientProjectOpt;
     public static String ClientProjectVersionOpt;
     public static String ClientQuietOpt;
     public static String ClientRelevantFlag;
     public static String ClientResultdirOpt;
     public static String ClientServerOpt;
     public static String ClientStartServerOpt;
     public static String ClientSwtOpt;
     public static String ClientTestJobOpt;
     public static String ClientTestSuiteOpt;
     public static String ClientTimeout;
     public static String ClientWritingReport;
     public static String ClientWritingReportToDB;
     public static String ConnectionToAutUnexpectedly;
     public static String ErrorWhileShuttingDownDisconnecting;
     public static String ErrorWhileShuttingDownStopping;
     public static String ExecutionControllerDatabase;
     public static String ExecutionControllerDataBaseEnd;
     public static String ExecutionControllerDatabaseStart;
     public static String I18nUtilsEventHandler;
     public static String I18nUtilsRetryStep;
     public static String I18nUtilsSeparator;
     public static String I18nUtilsStep;
     public static String I18nUtilsTestCase;
     public static String I18nUtilsTestSuite;
     public static String NoArgumentFor;
     public static String NoCorrespondingMessage;
     public static String NoSuchDatabaseConnection;
     public static String UnrecognizedOption;
     
     static {
         // initialize resource bundle
         NLS.initializeMessages(BUNDLE_NAME, Messages.class);
     }
 
     private Messages() {
         // empty
     }
 }
