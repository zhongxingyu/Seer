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
 package org.eclipse.jubula.app;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Properties;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jubula.app.core.JubulaWorkbenchAdvisor;
 import org.eclipse.jubula.app.core.WorkSpaceData;
 import org.eclipse.jubula.app.i18n.Messages;
 import org.eclipse.jubula.app.ui.ChooseWorkspaceDialog;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.osgi.service.datalocation.Location;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * This is the (standalone) launcher class.
  * 
  * @author BREDEX GmbH
  * @created 24.03.2005
  */
 public class Launcher implements IApplication,
     IExecutableExtension {
     /** the metadata folder*/
     private static final String METADATA_FOLDER = ".metadata"; //$NON-NLS-1$
     /** the version filename */
     private static final String VERSION_FILENAME = "version.ini"; //$NON-NLS-1$
     /** the workspace version key */
     private static final String WORKSPACE_VERSION_KEY = "org.eclipse.core.runtime"; //$NON-NLS-1$
     /** the workspace version value */
     private static final String WORKSPACE_VERSION_VALUE = "1"; //$NON-NLS-1$
     
     /** for log messages */
     private static Logger log = LoggerFactory.getLogger(Launcher.class);
 
     /**
      * Creates a new Application.
      */
     public Launcher() {
         // do nothing
     }
 
 
     /**
      * Return true if a valid workspace path has been set and false otherwise.
      * Prompt for and set the path if possible and required.
      * @param shell The actual shell.
      * @return true if a valid instance location has been set and false
      *         otherwise
      */
     protected boolean checkInstanceLocation(Shell shell) {
         // -data @none was specified but an ide requires workspace
         Location instanceLoc = Platform.getInstanceLocation();
         if (instanceLoc == null) {
             MessageDialog.openError(shell, Messages
                     .LauncherValidWorkspaceTitle,
                 Messages.LauncherValidWorkspace);
             return false;
         }
 
         // -data "/valid/path", workspace already set
         if (instanceLoc.isSet()) {
             return true;
         }
         // -data @noDefault or -data not specified, prompt and set
         URL defaultUrl = instanceLoc.getDefault();
         String initialDefault = defaultUrl == null ? null : defaultUrl
             .getFile();
         WorkSpaceData launchData = new WorkSpaceData(initialDefault);
 
         while (true) {
             URL workspaceUrl = promptForWorkspace(shell, launchData);
             if (workspaceUrl == null) {
                 return false;
             }
             try {
                 // the operation will fail if the url is not a valid
                 // instance data area, so other checking is unneeded
                 if (instanceLoc.set(workspaceUrl, true)) {
                     launchData.writePersistedData(shell);
                     writeWorkspaceVersion(shell);
                     return true;
                 }
             } catch (IllegalStateException e) {
                 MessageDialog.openError(shell, Messages
                         .LauncherCannotBeSetTitle,
                     Messages.LauncherCannotBeSet);
                 return false;
             } catch (IOException e) {
                 MessageDialog.openError(shell, Messages
                         .LauncherCannotBeSetTitle,
                         Messages.LauncherCannotBeSet);
                 return false;
             }
 
             // by this point it has been determined that the workspace is
             // already
             // in use -- force the user to choose again
             MessageDialog.openError(shell, Messages
                     .LauncherAlreadyInUseTitle,
                 Messages.LauncherAlreadyInUse);
         }
     }
 
     /**
      * Open a workspace selection dialog on the argument shell, populating the
      * argument data with the user's selection. Perform first level validation
      * on the selection by comparing the version information. This method does
      * not examine the runtime state (e.g., is the workspace already locked?).
      * 
      * @param shell The actual shell.
      * @param launchData The workspace data to launch.
      * @return An URL storing the selected workspace or null if the user has
      *         canceled the launch operation.
      */
     private URL promptForWorkspace(Shell shell, WorkSpaceData launchData) {
         URL url = null;
         do {
             ChooseWorkspaceDialog wd = 
                 new ChooseWorkspaceDialog(shell, launchData);
             wd.open();
             
             String instancePath = launchData.getSelection();
             if (instancePath == null
                     || wd.getReturnCode() == Window.CANCEL) {
                 return null;
             }
             try {
                 
                 File file = new File(instancePath);
                 url = file.toURL();
             } catch (MalformedURLException e) {
                 MessageDialog.openError(shell, Messages
                         .LauncherNotValidTitle,
                     Messages.LauncherNotValid);
                 continue;
             }
         } while (!isValidWorkspace(shell, url));
         return url;
     }
 
     /**
      * @param shell The actual shell.
      * @param url The workspace url.
      * @return True if the argument directory is ok to use as a workspace and
      * false otherwise. A version check will be performed, and a confirmation
      * box may be displayed on the argument shell if an older version is
      * detected.
      */
     private boolean isValidWorkspace(Shell shell, URL url) {
         String version = readWorkspaceVersion(url, shell);
         // if the version could not be read, then there is not any existing
         // workspace data to trample, e.g., perhaps its a new directory that
         // is just starting to be used as a workspace
         if (version == null) {
             return true;
         }
         final int ideversion = Integer.parseInt(WORKSPACE_VERSION_VALUE);
         int workspaceversion = Integer.parseInt(version);
         // equality test is required since any version difference (newer
         // or older) may result in data being trampled
         if (workspaceversion == ideversion) {
             return true;
         }
         // At this point workspace has been detected to be from a version
         // other than the current ide version -- find out if the user wants
         // to use it anyhow.
         String title = Messages.LauncherDifferentVersionTitle;
         // Use NLS.bind in Eclipse3.1
         String message = NLS.bind(
             Messages.LauncherDifferentVersionMessage, 
             url.getFile());
         MessageBox mbox = new MessageBox(shell, SWT.OK | SWT.CANCEL
             | SWT.ICON_WARNING | SWT.APPLICATION_MODAL);
         mbox.setText(title);
         mbox.setMessage(message);
         return mbox.open() == SWT.OK;
     }
 
     /**
      * Look at the argument URL for the workspace's version information. Return
      * that version if found and null otherwise.
      * @param workspace The workspace url.
      * @param shell The actual shell.
      * @return The workspace version.
      */
     private static String readWorkspaceVersion(URL workspace, Shell shell) {
         File versionFile = getVersionFile(workspace, false);
         if (versionFile == null || !versionFile.exists()) {
             return null;
         }
         try {
             // Although the version file is not spec'ed to be a Java properties
             // file, it happens to follow the same format currently, so using
             // Properties to read it is convenient.
             Properties props = new Properties();
             FileInputStream is = new FileInputStream(versionFile);
             try {
                 props.load(is);
             } finally {
                 is.close();
             }
             return props.getProperty(WORKSPACE_VERSION_KEY);
         } catch (IOException e) {
             log.error(Messages.CouldNotReadVersionFile, e);
             MessageDialog.openError(shell, Messages
                     .LauncherCouldNotReadTitle,
                     Messages.LauncherCouldNotRead);
             return null;
         }
     }
 
     /**
      * Write the version of the metadata into a known file overwriting any
      * existing file contents. Writing the version file isn't really crucial, so
      * the function is silent about failure.
      * @param shell The actual shell.
      */
     private static void writeWorkspaceVersion(Shell shell) {
         Location instanceLoc = Platform.getInstanceLocation();
         if (instanceLoc == null || instanceLoc.isReadOnly()) {
             return;
         }
         File versionFile = getVersionFile(instanceLoc.getURL(), true);
         if (versionFile == null) {
             return;
         }
         OutputStream output = null;
         try {
             String versionLine = WORKSPACE_VERSION_KEY
                     + StringConstants.EQUALS_SIGN + WORKSPACE_VERSION_VALUE;
             output = new FileOutputStream(versionFile);
             output.write(versionLine.getBytes("UTF-8")); //$NON-NLS-1$
         } catch (IOException e) {
             log.error(Messages.CouldNotWriteVersionFile, e);
             MessageDialog.openError(shell, Messages
                     .LauncherCouldNotWriteTitle,
                     Messages.LauncherCouldNotWrite);
         } finally {
             try {
                 if (output != null) {
                     output.close();
                 }
             } catch (IOException e) {
                 // do nothing
             }
         }
     }
 
     /**
      * The version file is stored in the metadata area of the workspace. This
      * method returns an URL to the file or null if the directory or file does
      * not exist (and the create parameter is false).
      * @param create If the directory and file does not exist this parameter controls whether it will be created.
      * @param workspaceUrl The URL of the workspace.
      * @return An url to the file or null if the version file does not exist or could not be created.
      */
     private static File getVersionFile(URL workspaceUrl, boolean create) {
         if (workspaceUrl == null) {
             return null;
         }
         try {
             // make sure the directory exists
             URL metaUrl = new URL(workspaceUrl, METADATA_FOLDER);
             File metaDir = new File(metaUrl.getFile());
             if (!metaDir.exists() && (!create || !metaDir.mkdir())) {
                 return null;
             }
             // make sure the file exists
             URL versionUrl = new URL(metaDir.toURL(), VERSION_FILENAME);
             File versionFile = new File(versionUrl.getFile());
             if (!versionFile.exists() 
                 && (!create || !versionFile.createNewFile())) {
                 return null;
             }
             return versionFile;
         } catch (IOException e) {
             // cannot log because instance area has not been set
             return null;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void setInitializationData(IConfigurationElement config, 
         String propertyName, Object data) {
         // do nothing 
     }
 
     /**
      * {@inheritDoc}
      */
     public Object start(IApplicationContext context) throws Exception {
         // create and startup the display for the workbench
         Display.setAppName("Jubula"); //$NON-NLS-1$
         Display display = new Display();
         try {
             Shell shell = new Shell(display, SWT.SYSTEM_MODAL | SWT.ON_TOP);
             try {
                 if (!checkInstanceLocation(shell)) {
                     context.applicationRunning();
                     return EXIT_OK;
                 }
             } finally {
                 if (shell != null) {
                     shell.dispose();
                 }
             }
             // create the workbench with this advisor and run it until it exits
             // N.B. createWorkbench remembers the advisor, and also registers
             // the
             // workbench globally so that all UI plug-ins can find it using
             // N.B. createWorkbench remembers the advisor, and also registers
             // the workbench globally so that all UI plug-ins can find it using
             // PlatformUI.getWorkbench() or AbstractUIPlugin.getWorkbench()
             // exit the application with an appropriate return code
             int returnCode = PlatformUI.createAndRunWorkbench(display,
                     new JubulaWorkbenchAdvisor());
             // exit the application with an appropriate return code
             return returnCode == PlatformUI.RETURN_RESTART 
                 ? IApplication.EXIT_RESTART : IApplication.EXIT_OK;
 
         } finally {
             if (display != null) {
                 display.dispose();
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void stop() {
         // nothing yet
         
     }
 }
