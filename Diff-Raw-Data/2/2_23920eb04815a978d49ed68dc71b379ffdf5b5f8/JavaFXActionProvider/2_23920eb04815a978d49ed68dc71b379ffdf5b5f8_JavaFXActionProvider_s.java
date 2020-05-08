 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */
 
 package org.netbeans.modules.javafx.project;
 
 import java.awt.Dialog;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.StringTokenizer;
 import javax.swing.JButton;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import org.apache.tools.ant.module.api.support.ActionUtils;
 import org.netbeans.api.java.classpath.ClassPath;
 import org.netbeans.api.java.project.JavaProjectConstants;
 import org.netbeans.api.java.queries.UnitTestForSourceQuery;
 import org.netbeans.api.java.source.ui.ScanDialog;
 import org.netbeans.api.javafx.platform.JavaFXPlatform;
 import org.netbeans.api.project.ProjectInformation;
 import org.netbeans.api.project.ProjectManager;
 import org.netbeans.api.project.ProjectUtils;
 import org.netbeans.api.project.SourceGroup;
 import org.netbeans.api.project.Sources;
 import org.netbeans.modules.java.api.common.ant.UpdateHelper;
 import org.netbeans.modules.javafx.project.classpath.ClassPathProviderImpl;
 import org.netbeans.modules.javafx.project.ui.customizer.JavaFXProjectProperties;
 import org.netbeans.modules.javafx.project.ui.customizer.MainClassWarning;
 import org.netbeans.spi.project.ActionProvider;
 import org.netbeans.spi.project.support.ant.AntProjectHelper;
 import org.netbeans.spi.project.support.ant.EditableProperties;
 import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
 import org.openide.DialogDescriptor;
 import org.openide.DialogDisplayer;
 import org.openide.ErrorManager;
 import org.openide.NotifyDescriptor;
 import org.openide.awt.MouseUtils;
 import org.openide.execution.ExecutorTask;
 import org.openide.filesystems.FileChangeAdapter;
 import org.openide.filesystems.FileChangeListener;
 import org.openide.filesystems.FileEvent;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileStateInvalidException;
 import org.openide.filesystems.FileSystem;
 import org.openide.filesystems.FileUtil;
 import org.openide.filesystems.Repository;
 import org.openide.filesystems.URLMapper;
 import org.openide.util.Exceptions;
 import org.openide.util.Lookup;
 import org.openide.util.NbBundle;
 import org.openide.util.Task;
 import org.openide.util.TaskListener;
 import org.openide.util.Utilities;
 
 /** Action provider of the JavaFX project. This is the place where to do
  * strange things to JavaFX actions. E.g. compile-single.
  */
 class JavaFXActionProvider implements ActionProvider {
 
     public static final String COMMAND_RUN_APPLET = "run.applet"; // NOI18N
     
     // Commands available from JavaFX project
     private static final String[] supportedActions = {
         COMMAND_BUILD,
         COMMAND_CLEAN,
         COMMAND_REBUILD,
         COMMAND_COMPILE_SINGLE,
         COMMAND_RUN,
         COMMAND_RUN_SINGLE,
         COMMAND_DEBUG,
         COMMAND_DEBUG_SINGLE,
         JavaProjectConstants.COMMAND_JAVADOC,
         COMMAND_DEBUG_STEP_INTO,
         COMMAND_DELETE,
         COMMAND_COPY,
         COMMAND_MOVE,
         COMMAND_RENAME,
     };
 
 
     private static final String[] platformSensitiveActions = {
         COMMAND_BUILD,
         COMMAND_REBUILD,
         COMMAND_COMPILE_SINGLE,
         COMMAND_RUN,
         COMMAND_RUN_SINGLE,
         COMMAND_DEBUG,
         COMMAND_DEBUG_SINGLE,
         JavaProjectConstants.COMMAND_JAVADOC,
         COMMAND_DEBUG_STEP_INTO,
     };
 
     // Project
     final JavaFXProject project;
 
     // Ant project helper of the project
     private UpdateHelper updateHelper;
 
 
     /** Map from commands to ant targets */
     Map<String,String[]> commands;
 
     /**Set of commands which are affected by background scanning*/
     final Set<String> bkgScanSensitiveActions;
 
     /** Set of Java source files (as relative path from source root) known to have been modified. See issue #104508. */
     private Set<String> dirty = null;
 
     private Sources src;
     private List<FileObject> roots;
     
     // Used only from unit tests to suppress detection of top level classes. If value
     // is different from null it will be returned instead.
     String unitTestingSupport_fixClasses;
 
     public JavaFXActionProvider(JavaFXProject project, UpdateHelper updateHelper) {
 
         commands = new HashMap<String,String[]>();
         commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
         commands.put(COMMAND_BUILD, new String[] {"jar"}); // NOI18N
         commands.put(COMMAND_REBUILD, new String[] {"clean", "jar"}); // NOI18N
         commands.put(COMMAND_COMPILE_SINGLE, new String[] {"jar"}); // NOI18N
         commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
         commands.put(COMMAND_RUN_SINGLE, new String[] {"run"}); // NOI18N
         commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
         commands.put(COMMAND_DEBUG_SINGLE, new String[] {"debug"}); // NOI18N
         commands.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
         commands.put(COMMAND_DEBUG_STEP_INTO, new String[] {"debug-stepinto"}); // NOI18N
 
         this.bkgScanSensitiveActions = new HashSet<String>(Arrays.asList(
             COMMAND_DEBUG,
             COMMAND_DEBUG_SINGLE,
             COMMAND_DEBUG_STEP_INTO
         ));
 
         this.updateHelper = updateHelper;
         this.project = project;        
     }
     
     private final FileChangeListener modificationListener = new FileChangeAdapter() {
         public @Override void fileChanged(FileEvent fe) {
             modification(fe.getFile());
         }
         public @Override void fileDataCreated(FileEvent fe) {
             modification(fe.getFile());
         }
     };
 
     private final ChangeListener sourcesChangeListener = new ChangeListener() {
 
         public void stateChanged(ChangeEvent e) {
             synchronized (JavaFXActionProvider.this) {
                 JavaFXActionProvider.this.roots = null;
             }
         }
     };
     
     
     void startFSListener () {
         //Listener has to be started when the project's lookup is initialized
         try {
             FileSystem fs = project.getProjectDirectory().getFileSystem();
             // XXX would be more efficient to only listen while DO_DEPEND=false (though this is the default)
             fs.addFileChangeListener(FileUtil.weakFileChangeListener(modificationListener, fs));
         } catch (FileStateInvalidException x) {
             Exceptions.printStackTrace(x);
         }
     }
 
     private void modification(FileObject f) {
         final Iterable <? extends FileObject> roots = getRoots();
         assert roots != null;
         for (FileObject root : roots) {
             String path = FileUtil.getRelativePath(root, f);
             if (path != null) {
                 synchronized (this) {
                     if (dirty != null) {
                         dirty.add(path);
                     }
                 }
                 break;
             }
         }
     }
 
     private Iterable <? extends FileObject> getRoots () {
         Sources _src = null;
         synchronized (this) {
             if (this.roots != null) {
                 return this.roots;
             }
             if (this.src == null) {
                 this.src = this.project.getLookup().lookup(Sources.class);
                 this.src.addChangeListener (sourcesChangeListener);
             }
             _src = this.src;
         }
         assert _src != null;
         final SourceGroup[] sgs = _src.getSourceGroups (JavaProjectConstants.SOURCES_TYPE_JAVA);
         final List<FileObject> _roots = new ArrayList<FileObject>(sgs.length);
         for (SourceGroup sg : sgs) {
             final FileObject root = sg.getRootFolder();
             if (UnitTestForSourceQuery.findSources(root).length == 0) {
                 _roots.add (root);
             }
         }
         synchronized (this) {
             if (this.roots == null) {
                 this.roots = _roots;
             }
             return this.roots;
         }
     }
 
     private FileObject findBuildXml() {
         return JavaFXProjectUtil.getBuildXml(project);
     }
 
     public String[] getSupportedActions() {
         return supportedActions;
     }
 
     public void invokeAction( final String command, final Lookup context ) throws IllegalArgumentException {
         if (COMMAND_DELETE.equals(command)) {
             DefaultProjectOperations.performDefaultDeleteOperation(project);
             return ;
         }
 
         if (COMMAND_COPY.equals(command)) {
             DefaultProjectOperations.performDefaultCopyOperation(project);
             return ;
         }
 
         if (COMMAND_MOVE.equals(command)) {
             DefaultProjectOperations.performDefaultMoveOperation(project);
             return ;
         }
 
         if (COMMAND_RENAME.equals(command)) {
             DefaultProjectOperations.performDefaultRenameOperation(project, null);
             return ;
         }
 
         final Runnable action = new Runnable () {
             public void run () {
                 Properties p = new Properties();
                 String[] targetNames;
                 if (Utilities.isWindows() && "desktop".equalsIgnoreCase(project.evaluator().getProperty("javafx.profile"))) { // NOI18N
                     String codeBaseURL = getCodebaseURL();
                     if (codeBaseURL != null) p.put("codebase.url", codeBaseURL); //NOI18N
                 }
                 targetNames = getTargetNames(command, context, p);
                 if (targetNames == null) {
                     return;
                 }
                 if (targetNames.length == 0) {
                     targetNames = null;
                 }
                 if (p.keySet().size() == 0) {
                     p = null;
                 }
                 try {
                     FileObject buildFo = findBuildXml();
                     if (buildFo == null || !buildFo.isValid()) {
                         //The build.xml was deleted after the isActionEnabled was called
                         NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(JavaFXActionProvider.class,
                                 "LBL_No_Build_XML_Found"), NotifyDescriptor.WARNING_MESSAGE); // NOI18N
                         DialogDisplayer.getDefault().notify(nd);
                     }
                     else {
                         ActionUtils.runTarget(buildFo, targetNames, p).addTaskListener(new TaskListener() {
                             public void taskFinished(Task task) {
                                 if (((ExecutorTask) task).result() != 0) {
                                     synchronized (JavaFXActionProvider.this) {
                                         // #120843: if a build fails, disable dirty-list optimization.
                                         dirty = null;
                                     }
                                 }
                             }
                         });
                     }
                 }
                 catch (IOException e) {
                     ErrorManager.getDefault().notify(e);
                 }
             }
         };
 
         if (this.bkgScanSensitiveActions.contains(command)) {
             ScanDialog.runWhenScanFinished(action, NbBundle.getMessage (JavaFXActionProvider.class,"ACTION_"+command));   //NOI18N
         } else {
             action.run();
         }
     }
 
     String getCodebaseURL() {
         URL base = URLMapper.findURL(Repository.getDefault().getDefaultFileSystem().findResource("HTTPServer_DUMMY"), URLMapper.NETWORK);// NOI18N
         if (base == null) return null;
         try {
             return new URL(base.getProtocol(), "localhost", base.getPort(), encodeURL("/servlet/org.netbeans.modules.javafx.project.JnlpDownloadServlet/" + project.getProjectDirectory().getPath() + "/" + project.evaluator().evaluate("${dist.dir}/"))).toExternalForm(); // NOI18N
         } catch (MalformedURLException e) {
             ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
             return null;
         }
     }
     
     String encodeURL(final String orig) {
         StringTokenizer slashTok = new StringTokenizer(orig, "/", true); // NOI18N
         StringBuffer path = new StringBuffer();
         while (slashTok.hasMoreTokens()) {
             final String tok = slashTok.nextToken();
             if (tok.startsWith("/")) { // NOI18N
                 path.append(tok);
             } else {
                 try {
                     path.append(URLEncoder.encode(tok, "UTF-8")); // NOI18N
                 } catch (UnsupportedEncodingException e) {
                     path.append(URLEncoder.encode(tok));
                 }
             }
         }
         return path.toString();
     } 
     
     /**
      * @return array of targets or null to stop execution; can return empty array
      */
     /*private*/ String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
         if (Arrays.asList(platformSensitiveActions).contains(command)) {
             final String activePlatformId = this.project.evaluator().getProperty("platform.active");  //NOI18N
             if (JavaFXProjectUtil.getActivePlatform (activePlatformId) == null) {
                 showPlatformWarning ();
                 return null;
             }
         }
         String[] targetNames = commands.get(command);
         if (targetNames == null) throw new IllegalArgumentException(command);
        if (command.equals (COMMAND_RUN) || command.equals(COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_STEP_INTO)) {
             String config = project.evaluator().getProperty(JavaFXConfigurationProvider.PROP_CONFIG);
             String path;
             if (config == null || config.length() == 0) {
                 path = AntProjectHelper.PROJECT_PROPERTIES_PATH;
             } else {
                 // Set main class for a particular config only.
                 path = "nbproject/configs/" + config + ".properties"; // NOI18N
             }
             EditableProperties ep = updateHelper.getProperties(path);
 
             // check project's main class
             // Check whether main class is defined in this config. Note that we use the evaluator,
             // not ep.getProperty(MAIN_CLASS), since it is permissible for the default pseudoconfig
             // to define a main class - in this case an active config need not override it.
             String mainClass = project.evaluator().getProperty(JavaFXProjectProperties.MAIN_CLASS);
             MainClassStatus result = isSetMainClass (project.getSourceRoots().getRoots(), mainClass);
             
             if (context.lookup(JavaFXConfigurationProvider.Config.class) != null) {
 //            if(ep.getProperty(JavaFXProjectProperties.MAIN_CLASS) != null){
                 // If a specific config was selected, just skip this check for now.
                 // XXX would ideally check that that config in fact had a main class.
                 // But then evaluator.getProperty(MAIN_CLASS) would be inaccurate.
                 // Solvable but punt on it for now.
                 result = MainClassStatus.SET_AND_VALID;
             }
   
             if (result != MainClassStatus.SET_AND_VALID) {
                 do {
                     // show warning, if cancel then return
                     if (showMainClassWarning (mainClass, ProjectUtils.getInformation(project).getDisplayName(), ep,result)) {
                         return null;
                     }
                     // No longer use the evaluator: have not called putProperties yet so it would not work.
                     mainClass = ep.get(JavaFXProjectProperties.MAIN_CLASS);
                     result=isSetMainClass (project.getSourceRoots().getRoots(), mainClass);
                 } while (result != MainClassStatus.SET_AND_VALID);
                 try {
                     if (updateHelper.requestUpdate()) {
                         updateHelper.putProperties(path, ep);
                         ProjectManager.getDefault().saveProject(project);
                     }
                     else {
                         return null;
                     }
                 } catch (IOException ioe) {
                     ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Error while saving project: " + ioe); // NOI18N
                 }
             }
         } else if (command.equals (COMMAND_RUN_SINGLE) || command.equals (COMMAND_DEBUG_SINGLE)) {
             FileObject file = null;
             String clazz = null;
             for (FileObject srcPath : project.getSourceRoots().getRoots()) {
                 FileObject srcs[] = ActionUtils.findSelectedFiles(context, srcPath, ".fx", true); // NOI18N
                 if (srcs != null && srcs.length > 0) {
                     file = srcs[0];
                     clazz = FileUtil.getRelativePath(srcPath, file);
                     break;
                 }
             }
             if (clazz == null) return null;
             p.setProperty(JavaFXProjectProperties.MAIN_CLASS, clazz.substring(0, clazz.length() - 3).replace('/', '.')); // NOI18N
         }
         JavaFXConfigurationProvider.Config c = context.lookup(JavaFXConfigurationProvider.Config.class);
         if (c != null) {
             String config;
             if (c.name != null) {
                 config = c.name;
             } else {
                 // Invalid but overrides any valid setting in config.properties.
                 config = ""; // NOI18N
             }
             p.setProperty(JavaFXConfigurationProvider.PROP_CONFIG, config);
         }
         return targetNames;
     }
     
     public boolean isActionEnabled( String command, Lookup context ) {
         FileObject buildXml = findBuildXml();
         if (  buildXml == null || !buildXml.isValid()) return false;
         if ("mobile".equals(project.evaluator().getProperty("javafx.profile"))) {
             JavaFXPlatform jp = JavaFXProjectUtil.getActivePlatform(project.evaluator().getProperty("platform.active")); //NOI18N
             if (jp == null) return false;
             try {
                 File f = new File(jp.getJavaFXFolder().toURI());
                 if (!new File(f, "emulator/mobile/bin/preverify" + (Utilities.isWindows() ? ".exe" : "")).isFile()) return false; //NOI18N
                 if (command.equals(COMMAND_RUN) || command.equals(COMMAND_RUN_SINGLE) || command.equals(COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_SINGLE) || command.equals(COMMAND_DEBUG_STEP_INTO)) { //NOI18N
                     if (!new File(f, "emulator/mobile/bin/emulator" + (Utilities.isWindows() ? ".exe" : "")).isFile()) return false; //NOI18N
                 }
             } catch (URISyntaxException e) {
                 return false;
             }
         }
         if (command.equals(COMMAND_RUN_SINGLE) || command.equals(COMMAND_DEBUG_SINGLE)) {
             boolean foundExactlyOne = false;;
             for (FileObject srcPath : project.getSourceRoots().getRoots()) {
                 FileObject srcs[] = ActionUtils.findSelectedFiles(context, srcPath, null, true);
                 if (srcs != null) {
                     if (foundExactlyOne || srcs.length > 1 || !"fx".equals(srcs[0].getExt())) return false; //NOI18N
                     foundExactlyOne = true;
                 }                    
             }
             return foundExactlyOne;
         }
         return true;
     }
 
 
 
     // Private methods -----------------------------------------------------
 
     private static enum MainClassStatus {
         SET_AND_VALID,
         SET_BUT_INVALID,
         UNSET
     }
 
     /**
      * Tests if the main class is set
      * @param sourcesRoots source roots
      * @param mainClass main class name
      * @return status code
      */
     private MainClassStatus isSetMainClass(FileObject[] sourcesRoots, String mainClass) {
 
         if (mainClass == null || mainClass.length () == 0) {
             return MainClassStatus.UNSET;
         }
         if (sourcesRoots.length > 0) {
             ClassPath bootPath = ClassPath.getClassPath (sourcesRoots[0], ClassPath.BOOT);        //Single compilation unit
             ClassPath compilePath = ClassPath.getClassPath (sourcesRoots[0], ClassPath.EXECUTE);
             ClassPath sourcePath = ClassPath.getClassPath(sourcesRoots[0], ClassPath.SOURCE);
             if (JavaFXProjectUtil.isMainClass (mainClass, bootPath, compilePath, sourcePath)) {
                 return MainClassStatus.SET_AND_VALID;
             }
         }
         else {
             ClassPathProviderImpl cpProvider = project.getClassPathProvider();
             if (cpProvider != null) {
                 ClassPath bootPath = cpProvider.getProjectSourcesClassPath(ClassPath.BOOT);
                 ClassPath compilePath = cpProvider.getProjectSourcesClassPath(ClassPath.EXECUTE);
                 ClassPath sourcePath = cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE);   //Empty ClassPath
                 if (JavaFXProjectUtil.isMainClass (mainClass, bootPath, compilePath, sourcePath)) {
                     return MainClassStatus.SET_AND_VALID;
                 }
             }
         }
         return MainClassStatus.SET_BUT_INVALID;
     }
 
     /**
      * Asks user for name of main class
      * @param mainClass current main class
      * @param projectName the name of project
      * @param ep project.properties to possibly edit
      * @param messgeType type of dialog
      * @return true if user selected main class
      */
     private boolean showMainClassWarning(String mainClass, String projectName, EditableProperties ep, MainClassStatus messageType) {
         boolean canceled;
         final JButton okButton = new JButton (NbBundle.getMessage (MainClassWarning.class, "LBL_MainClassWarning_ChooseMainClass_OK")); // NOI18N
         okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (MainClassWarning.class, "AD_MainClassWarning_ChooseMainClass_OK")); // NOI18N
 
         // main class goes wrong => warning
         String message;
         switch (messageType) {
             case UNSET:
                 message = MessageFormat.format (NbBundle.getMessage(MainClassWarning.class,"LBL_MainClassNotFound"), new Object[] { // NOI18N
                     projectName
                 });
                 break;
             case SET_BUT_INVALID:
                 message = MessageFormat.format (NbBundle.getMessage(MainClassWarning.class,"LBL_MainClassWrong"), new Object[] { // NOI18N
                     mainClass,
                     projectName
                 });
                 break;
             default:
                 throw new IllegalArgumentException ();
         }
         final MainClassWarning panel = new MainClassWarning (message,project.getSourceRoots().getRoots());
         Object[] options = new Object[] {
             okButton,
             DialogDescriptor.CANCEL_OPTION
         };
 
         panel.addChangeListener (new ChangeListener () {
            public void stateChanged (ChangeEvent e) {
                if (e.getSource () instanceof MouseEvent && MouseUtils.isDoubleClick (((MouseEvent)e.getSource ()))) {
                    // click button and the finish dialog with selected class
                    okButton.doClick ();
                } else {
                    okButton.setEnabled (panel.getSelectedMainClass () != null);
                }
            }
         });
 
         okButton.setEnabled (false);
         DialogDescriptor desc = new DialogDescriptor (panel,
             NbBundle.getMessage (MainClassWarning.class, "CTL_MainClassWarning_Title", ProjectUtils.getInformation(project).getDisplayName()), // NOI18N
             true, options, options[0], DialogDescriptor.BOTTOM_ALIGN, null, null);
         desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
         Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
         dlg.setVisible (true);
         if (desc.getValue() != options[0]) {
             canceled = true;
         } else {
             mainClass = panel.getSelectedMainClass ();
             canceled = false;
             ep.put(JavaFXProjectProperties.MAIN_CLASS, mainClass == null ? "" : mainClass); // NOI18N
         }
         dlg.dispose();
 
         return canceled;
     }
 
     private void showPlatformWarning () {
         final JButton closeOption = new JButton (NbBundle.getMessage(JavaFXActionProvider.class, "CTL_BrokenPlatform_Close")); // NOI18N
         closeOption.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JavaFXActionProvider.class, "AD_BrokenPlatform_Close")); // NOI18N
         final ProjectInformation pi = project.getLookup().lookup(ProjectInformation.class);
         final String projectDisplayName = pi == null ?
             NbBundle.getMessage (JavaFXActionProvider.class,"TEXT_BrokenPlatform_UnknownProjectName") // NOI18N
             : pi.getDisplayName();
         final DialogDescriptor dd = new DialogDescriptor(
             NbBundle.getMessage(JavaFXActionProvider.class, "TEXT_BrokenPlatform", projectDisplayName), // NOI18N
             NbBundle.getMessage(JavaFXActionProvider.class, "MSG_BrokenPlatform_Title"), // NOI18N
             true,
             new Object[] {closeOption},
             closeOption,
             DialogDescriptor.DEFAULT_ALIGN,
             null,
             null);
         dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);
         final Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
         dlg.setVisible(true);
     }
 }
