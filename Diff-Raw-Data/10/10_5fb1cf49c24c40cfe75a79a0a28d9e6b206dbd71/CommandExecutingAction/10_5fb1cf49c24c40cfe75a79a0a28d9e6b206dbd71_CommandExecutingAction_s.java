 package org.metatrope.cmdhere.popup.actions;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 public abstract class CommandExecutingAction extends AbstractHandler {
     /**
      * Constructor
      */
     public CommandExecutingAction() {
         super();
     }
 
     public Object execute(ExecutionEvent event) throws ExecutionException {
         IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getActiveMenuSelection(event);
         Object firstElement = selection.getFirstElement();
         if (firstElement instanceof ICompilationUnit) {
             ICompilationUnit cu = (ICompilationUnit) firstElement;
             launchProgram(cu.getResource().getLocation());
         } else if (firstElement instanceof IPackageFragmentRoot ) {
             IPackageFragmentRoot  cu = (IPackageFragmentRoot ) firstElement;
             launchProgram(cu.getResource().getLocation());
         } else if (firstElement instanceof IPackageFragment) {
             IPackageFragment cu = (IPackageFragment) firstElement;
             launchProgram(cu.getResource().getLocation());
         } else if (firstElement instanceof IResource) {
             IResource cu = (IResource) firstElement;
             launchProgram(cu.getLocation());
         } else if (firstElement instanceof IType) {
             IType cu = (IType) firstElement;
             launchProgram(cu.getResource().getLocation());
         } else if (firstElement instanceof IScriptProject) {
             IScriptProject cu = (IScriptProject) firstElement;
             launchProgram(cu.getResource().getLocation());
         } else {
             launchProgram(null);
         }
         return null;
     }
 
     private void launchProgram(IPath path) {
         Runtime run = Runtime.getRuntime();
         String fullPath = findDirectoryFor(path);
 		if (fullPath == null)
 			fullPath = ".";
         String[] cmd = getCommand(fullPath);
         try {
             run.exec(cmd, null, new File(fullPath));
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     
     public abstract String[] getCommand(String path);
 
     /**
      * @param path where a given resource is located
      * @return the OS-specific file system path to a resource within Eclipse; if the resource
      * is a file, the directory containing it is returned instead  
      */
     private String findDirectoryFor(IPath path) {
         if (path == null)
             return null;
         String fullPath = path.toOSString();
         File file = path.toFile();
         if (!file.isDirectory()) {
             fullPath = file.getParent();
         }
         return fullPath;
     }
     
     private String getOS() {
     	String osName = System.getProperty("os.name").toLowerCase();
     	return osName;
     }
     
     protected boolean isWindows() {
     	return getOS().indexOf("win") >= 0;
     }
     
     protected boolean isMacOSX() {
     	return getOS().indexOf("mac") >= 0;
     }
 }
