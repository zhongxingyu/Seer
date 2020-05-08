 package com.seitenbau.eclipse.plugin.datenmodell.generator.visualizer.menu;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareUI;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectNature;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 import com.seitenbau.eclipse.plugin.datenmodell.generator.visualizer.diff.compare.CompareInput;
 
 public class TreeDiffMenuHandler extends AbstractHandler {
 
     @Override
     public Object execute(ExecutionEvent event) throws ExecutionException {
         Shell shell = HandlerUtil.getActiveShell(event);
         ISelection sel = HandlerUtil.getActiveMenuSelection(event);
         IStructuredSelection selection = (IStructuredSelection) sel;
 
         Object firstElement = selection.getFirstElement();
         if (firstElement instanceof IProjectNature) {
             IProjectNature projectNature = (IProjectNature) firstElement;
             IProject project = projectNature.getProject();
 
             IWorkbenchPage page = PlatformUI.getWorkbench()
                     .getActiveWorkbenchWindow().getActivePage();
            CompareUI.openCompareEditorOnPage(new CompareInput(project, getCompareConfig(), "Tree Diff (some resources might be ignored)"), page);
         } else {
             MessageDialog.openInformation(shell, "Info", "Please select a Project");
         }
         return null;
     }
 
     private CompareConfiguration getCompareConfig() {
         CompareConfiguration cc = new CompareConfiguration();
         cc.setLeftEditable(true);
         cc.setLeftLabel("Source file");
         cc.setRightLabel("Fully generated file");
         cc.setProperty(CompareConfiguration.IGNORE_WHITESPACE, true);
         
         return cc;
     }
 
 }
