 /*******************************************************************************
  * Copyright (c) 2004, 2012 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.app.autrun.i18n;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS {
     private static final String BUNDLE_NAME = "org.eclipse.jubula.app.autrun.i18n.messages";  //$NON-NLS-1$
     
     public static String infoAutAgentHost;
     public static String infoAutAgentPort;
     public static String infoSwingToolkit;
     public static String infoSwtToolkit;
     public static String infoRcpToolkit;
     public static String infoAutId;
     public static String infoGenerateTechnicalComponentNames;
     public static String infoKbLayout;
     public static String infoAutWorkingDirectory;
     public static String infoHelp;
     public static String infoExecutableFile;
     public static String infoConnectionToAutAgentFailed;
     public static String infoNonAutAgentConnectionInfo;
     public static String restartAutFailed;
 
     static {
         NLS.initializeMessages(BUNDLE_NAME, Messages.class);
     }
     /** Constructor */
     private Messages () {
     }
 }
