 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.splash.handlers;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map;
 
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.database.nodes.RubyProjectNode;
 import org.amanzi.neo.core.database.services.AweProjectService;
 import org.amanzi.splash.editors.ReportEditor;
 import org.amanzi.splash.report.ReportInput;
 import org.amanzi.splash.swing.SplashTableModel;
 import org.amanzi.splash.ui.AbstractSplashEditor;
 import org.amanzi.splash.ui.SplashPlugin;
 import org.amanzi.splash.utilities.NeoSplashUtil;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.part.FileEditorInput;
 
 /**
  * Command handler for report editor
  * 
  * @author Pechko_E
  * @since 1.0.0
  */
 public class ReportCommandHandler extends AbstractHandler {
 
     @Override
     public Object execute(ExecutionEvent event) throws ExecutionException {
         final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
         // get project TODO
         final AbstractSplashEditor editor = (AbstractSplashEditor)window.getActivePage().getActiveEditor();
         SplashTableModel model = (SplashTableModel)editor.getTable().getModel();
         RubyProjectNode spreadsheetRootProject = model.getSpreadsheet().getSpreadsheetRootProject();
         String projectName = spreadsheetRootProject.getName();
         IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
         final IProject resource = root.getProject(projectName);
         // create editor input
         IFile file;
         int i = 0;
        while ((file = resource.getFile(new Path("report" + i))).exists()) {
             i++;
         }
         if (!file.exists()) {
             try {
                 StringBuffer sb = new StringBuffer("report '").append("report" + i).append("' do\nauthor '").append(
                         System.getProperty("user.name")).append("'\ndate '").append(
                         new SimpleDateFormat("yyyy-MM-dd").format(new Date())).append("'\nend");
                 InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
                 file.create(is, true, null);
                 is.close();
             } catch (CoreException e1) {
                 // TODO Handle CoreException
                 displayErrorMessage(e1);
                 throw (RuntimeException)new RuntimeException().initCause(e1);
             } catch (IOException e) {
                 // TODO Handle IOException
                 displayErrorMessage(e);
                 throw (RuntimeException)new RuntimeException().initCause(e);
             }
         }
 
         IFileEditorInput ei = new FileEditorInput(file);
         try {
             window.getActivePage().openEditor(ei, ReportEditor.class.getName());
         } catch (PartInitException e) {
             // TODO Handle PartInitException
             displayErrorMessage(e);
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
         return null;
     }
 
     /**
      * Displays error message instead of throwing an exception
      * 
      * @param e exception thrown
      */
     private void displayErrorMessage(final Exception e) {
         final Display display = PlatformUI.getWorkbench().getDisplay();
         display.asyncExec(new Runnable() {
 
             @Override
             public void run() {
                 ErrorDialog.openError(display.getActiveShell(), "Error", "An exception occured", new Status(Status.ERROR,
                         SplashPlugin.getId(), e.getClass().getName(), e));
             }
 
         });
     }
 
 }
