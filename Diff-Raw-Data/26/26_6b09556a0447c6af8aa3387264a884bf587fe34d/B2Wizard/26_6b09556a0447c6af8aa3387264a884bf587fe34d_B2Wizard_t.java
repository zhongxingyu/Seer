 /**
  * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.b2eclipse.ui;
 
 import java.io.File;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.CheckboxTreeViewer;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IImportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkingSet;
 import org.eclipse.ui.IWorkingSetManager;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
 import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
 import org.eclipse.ui.model.BaseWorkbenchContentProvider;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 import org.sourcepit.b2eclipse.Activator;
 import org.sourcepit.b2eclipse.input.Node;
 import org.sourcepit.b2eclipse.input.ViewerInput;
 
 /**
  * @author WD
  */
 @SuppressWarnings("restriction")
 public class B2Wizard extends Wizard implements IImportWizard
 {
    private static String previouslyBrowsedDirectory = "";
 
    private B2WizardPage page;
 
    public B2Wizard()
    {
       super();
       page = new B2WizardPage(Messages.msgImportHeader, this);
       addPage(page);
    }
 
    public void init(IWorkbench workbench, IStructuredSelection selection)
    {
      // TODO pass selection to wizard page and pre-initialize UI (selection could contain an IResource or a Java IO File)
       setWindowTitle(Messages.msgImportTitle);
       Image projectFolder = Activator.getImageFromPath("icons/ProjectFolder.gif");
       setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(projectFolder));
    }
 
 
    public void doCheck(CheckboxTreeViewer viewer, boolean state)
    {
       for (Node dad : ((Node) viewer.getInput()).getChildren())
       {
          viewer.setSubtreeChecked(dad, state);
       }
       // TODO eyeCandy: nur wenn im Modul Projecte vorhanden sind, markieren
    }
 
 
    /**
     * Delete the <code>node</code> from the <code>previevTreeViever</code>.
     * 
     * @param previevTreeViever
     * @param node
     */
    public void deleteProjectFromPrevievTree(TreeViewer previewTreeViewer, Node node)
    {
       Node imDead = ((Node) previewTreeViewer.getInput()).getEqualNode(node);
 
       if (imDead != null)
       {
          Node deadDad = imDead.getRootModel();
          imDead.deleteNode();
 
          if (deadDad.getChildren().size() == 0)
          {
             deadDad.deleteNode();
          }
       }
       previewTreeViewer.refresh();
    }
 
    /**
     * Add the <code>node</code> to the <code>previevTreeViever</code>.
     * 
     * @param previevTreeViever
     * @param node
     */
    public void addProjectToPrevievTree(TreeViewer previewTreeViewer, Node node)
    {
       Node root = (Node) previewTreeViewer.getInput();
       Node module = node.getParent();
 
       boolean created = false;
 
       for (Node iter : root.getChildren())
       {
          if (iter.getFile() == module.getFile())
          {
             new Node(iter, node.getFile(), node.getType());
             created = true;
             break;
          }
       }
       if (!created)
       {
          new Node(new Node(root, module.getFile(), Node.Type.WORKINGSET, module.getWSName(module)), node.getFile(),
             node.getType());
       }
 
       previewTreeViewer.refresh();
    }
 
    /**
     * Checks if the parent file is null.
     * 
     * @param selectedProject
     * @return
     */
    public boolean testOnLocalDrive(String selectedProject)
    {
       if (selectedProject != null)
          if (new File(selectedProject).getParentFile() != null)
             return true;
 
       return false;
    }
 
    /**
     * Shows a directory select dialog.
     * 
     * @param directoryName
     * @param dialogShell
     * @return
     */
    public String showDirectorySelectDialog(String directoryName, Shell dialogShell)
    {
 
       DirectoryDialog directoryDialog = new DirectoryDialog(dialogShell, SWT.OPEN);
 
       directoryDialog.setText(Messages.msgSelectDirTitle);
 
       directoryName = directoryName.trim();
       if (directoryName.length() == 0)
       {
 
          if (previouslyBrowsedDirectory.length() == 0)
          {
             directoryName = IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getLocation().toOSString();
          }
          else
          {
             directoryName = previouslyBrowsedDirectory;
          }
       }
       if (!new File(directoryName).exists())
       {
          directoryName = IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getLocation().toOSString();
       }
 
       directoryDialog.setFilterPath(new Path(directoryName).toOSString());
 
       String selectedDirectory = directoryDialog.open();
       if (selectedDirectory != null)
       {
          if (testOnLocalDrive(selectedDirectory))
          {
             previouslyBrowsedDirectory = selectedDirectory;
             return selectedDirectory;
          }
       }
       return "";
    }
 
    /**
     * Shows a workspace select dialog.
     * 
     * @param dialogShell
     * @return
     */
    public String showWorkspaceSelectDialog(Shell dialogShell)
    {
       ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(dialogShell, new WorkbenchLabelProvider(),
          new BaseWorkbenchContentProvider());
       dialog.setTitle(Messages.msgSelectProjectTitle);
       dialog.setMessage(Messages.msgSelectProject);
       dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
       dialog.open();
       if (dialog.getFirstResult() != null)
       {
          String selectedProject = String.valueOf(((IResource) dialog.getFirstResult()).getLocation());
 
          if (testOnLocalDrive(selectedProject))
          {
             return selectedProject;
          }
       }
       return "";
    }
 
    /**
     * Handles a change in the directory Field, actual it sets a new input to the treeViewer's.
     * 
     * @param treeViewer
     * @param previevTreeViever
     * @param txt
     */
    public void handleDirTreeViever(CheckboxTreeViewer treeViewer, TreeViewer previevTreeViever, String txt)
    {
       ViewerInput in = new ViewerInput(new Node());
 
       treeViewer.setInput(in.createMainNodeSystem(new File(txt)));
       treeViewer.expandToLevel(2);
       doCheck(treeViewer, true);
 
       previevTreeViever.setInput(in.createNodeSystemForPreviev());
    }
 
    /**
     * Creates WorkingSets and Projects in the Workspace.
     */
    @Override
    public boolean performFinish()
    {
       // TODO eyeCandy: mit ner Progress Bar verschönern.
       IWorkingSetManager wSmanager = PlatformUI.getWorkbench().getWorkingSetManager();
       IWorkspace workspace = ResourcesPlugin.getWorkspace();
 
       Node root = page.getPreviewRootNode();
 
       for (Node currentElement : root.getChildren())
       {
          if (currentElement.getType() == Node.Type.WORKINGSET) 
          {
             String wsName = currentElement.getName();
             IWorkingSet workingSet = wSmanager.getWorkingSet(wsName);
             if (workingSet == null)
             {
                // org.eclipse.ui.resourceWorkingSetPage = Resource WorkingSet
                // org.eclipse.jdt.ui.JavaWorkingSetPage = Java WorkingSet
 
                workingSet = wSmanager.createWorkingSet(wsName, new IAdaptable[] {});
                workingSet.setId("org.eclipse.jdt.ui.JavaWorkingSetPage");
                wSmanager.addWorkingSet(workingSet);
             }
 
             for (Node currentSubElement : currentElement.getChildren())
             {
                if (currentSubElement.getType() == Node.Type.PROJECT) // Sollte immer wahr sein
                {
                   try
                   {
                      IProjectDescription projectDescription = workspace.loadProjectDescription(new Path(currentSubElement
                         .getFile().toString() + "/.project"));
                      IProject project = workspace.getRoot().getProject(projectDescription.getName());
                      JavaCapabilityConfigurationPage.createProject(project, projectDescription.getLocationURI(), null);
 
                      wSmanager.addToWorkingSets(project, new IWorkingSet[] { workingSet });
                   }
                   catch (CoreException e)
                   {
                      throw new IllegalStateException(e);
                      // TODO EyeCandy: Ausgabe (bsp: zugriff verweigert)
                   }
                }
             }
          }
          if (currentElement.getType() == Node.Type.PROJECT) 
          {
             try
             {
                IProjectDescription projectDescription = workspace.loadProjectDescription(new Path(currentElement
                   .getFile().toString() + "/.project"));
                IProject project = workspace.getRoot().getProject(projectDescription.getName());
                JavaCapabilityConfigurationPage.createProject(project, projectDescription.getLocationURI(), null);
             }
             catch (CoreException e)
             {
                throw new IllegalStateException(e);
                // TODO siehe oben, + evt. ein mege
             }
          }
       }
       return true;
    }
 }
