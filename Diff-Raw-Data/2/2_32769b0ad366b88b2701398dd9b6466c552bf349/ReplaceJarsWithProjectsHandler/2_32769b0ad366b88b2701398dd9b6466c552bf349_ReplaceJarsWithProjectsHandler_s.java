 package uk.co.kgibbs.tools.eclipse.plugins.joinprojects;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.statushandlers.StatusManager;
 
 public class ReplaceJarsWithProjectsHandler extends AbstractHandler
 {
 
     private static final String PLUGIN_ID = ReplaceJarsWithProjectsHandler.class.getPackage().getName();
 
     @Override
     public Object execute(ExecutionEvent event)
     {
         final List<IJavaProject> selectedProjects = getSelectedJavaProjects(event);
         if (selectedProjects.isEmpty()) // should never happen, as menu should
                                         // not be available!
         {
             StatusManager.getManager().handle(
                     new Status(IStatus.WARNING, PLUGIN_ID, Messages.ReplaceJarsWithProjectsHandler_1),
                     StatusManager.SHOW);
             return null;
         }
 
         final IProject[] workspaceProjects = getWorkspaceProjects();
 
         final ArrayList<IStatus> infoStatusList = new ArrayList<IStatus>();
         for (IJavaProject selectedProject : selectedProjects)
         {
             try
             {
                 replaceJarsInProject(selectedProject, workspaceProjects, infoStatusList);
             } catch (JavaModelException e)
             {
                 StatusManager.getManager().handle(e, PLUGIN_ID);
                 return null;
             }
         }
 
         if (infoStatusList.isEmpty())
         {
             StatusManager.getManager()
                     .handle(new Status(IStatus.INFO, PLUGIN_ID, Messages.ReplaceJarsWithProjectsHandler_2),
                             StatusManager.BLOCK);
         } else
         {
             StatusManager.getManager().handle(
                     new MultiStatus(PLUGIN_ID, IStatus.OK, infoStatusList.toArray(new IStatus[infoStatusList.size()]),
                             Messages.ReplaceJarsWithProjectsHandler_6, null), StatusManager.BLOCK);
         }
 
         return null;
     }
 
     private void replaceJarsInProject(IJavaProject selectedProject, final IProject[] workspaceProjects,
             final List<IStatus> infoList) throws JavaModelException
     {
         final String selectedProjectName = selectedProject.getProject().getName();
         final IClasspathEntry[] rawClasspath = selectedProject.getRawClasspath();
         for (int i = 0; i < rawClasspath.length; i++)
         {
             final IClasspathEntry cp = rawClasspath[i];
             switch (cp.getEntryKind())
             {
             case IClasspathEntry.CPE_LIBRARY:
             case IClasspathEntry.CPE_VARIABLE:
                 for (IProject workspaceProject : workspaceProjects)
                 {
                     String workspaceProjectName = workspaceProject.getName();
 
                     final String libFilename = cp.getPath().toFile().getName();
                     if (libFilename.startsWith(workspaceProjectName + "-")) //$NON-NLS-1$
                     {
                         rawClasspath[i] = JavaCore.newProjectEntry(workspaceProject.getFullPath());
                         selectedProject.setRawClasspath(rawClasspath, true, null);
                         infoList.add(new Status(IStatus.INFO, PLUGIN_ID, String.format(
                                 Messages.ReplaceJarsWithProjectsHandler_3, cp.getPath(), selectedProjectName,
                                 workspaceProjectName)));
                     }
                 }
                 break;
 
             default:
                 break;
             }
         }
     }
 
     private List<IJavaProject> getSelectedJavaProjects(ExecutionEvent event)
     {
         final ArrayList<IJavaProject> selectedProjects = new ArrayList<IJavaProject>();
 
         final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
         if (window != null)
         {
             final IWorkbenchPage activePage = window.getActivePage();
             if (activePage != null)
             {
                 final IStructuredSelection selection = (IStructuredSelection) activePage.getSelection();
                 if (selection != null)
                 {
                    final Iterator selectedElements = selection.iterator();
                     while (selectedElements.hasNext())
                     {
                         Object element = selectedElements.next();
                         if (element instanceof IAdaptable)
                         {
                             IJavaProject selectedJavaProject = (IJavaProject) ((IAdaptable) element)
                                     .getAdapter(IJavaProject.class);
                             if (selectedJavaProject != null)
                                 selectedProjects.add(selectedJavaProject);
                         }
                     }
                 }
             }
         }
 
         return Collections.unmodifiableList(selectedProjects);
     }
 
     private IProject[] getWorkspaceProjects()
     {
         return ResourcesPlugin.getWorkspace().getRoot().getProjects();
     }
 
 }
