 package com.doapps.cakephp.files.impl;
 
 import java.io.ByteArrayInputStream;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 
 import com.doapps.cakephp.files.ICakeAction;
 import com.doapps.cakephp.files.ICakePHPFile;
 import com.doapps.cakephp.files.ICakePHPProject;
 import com.doapps.cakephp.files.IController;
 import com.doapps.cakephp.files.IModel;
 
 public class CakePHPProject implements ICakePHPProject
 {
   private IProject project;
   
   
 
   public CakePHPProject(IProject project)
   {
     this.project = project;
   }
 
   @Override
   public IProject getProject()
   {
     return this.project;
   }
   
   @Override
   public IFile getFile(IPath path)
   {
     return this.project.getFile(path);
   }
   
   public ICakePHPFile getCakePHPFile(IFile file)
   {
     if (file == null)
     {
       return null;
     }
     
     if (isModel(file))
     {
       return new Model(this, file);
     }
     else if (isController(file))
     {
       return new Controller(this, file);
     }
     else if (isElement(file)) // order matters, this needs to be ahead of view, because it's parent folder is View
     {
       return new Element(this, file);
     }
     else if (isView(file))
     {
       return new View(this, file);
     }
     else if (isJSFile(file))
     {
       return new JSFile(this, file);
     }
     
     return null;
   }
   
   @Override
   public boolean isModel(IFile file)
   {
     return hasParentFolderAndNameMatches(file, getModelFileRegex(), getModelFolderRegex(), 1);
   }
 
   private Pattern getModelFolderRegex()
   {
     // TODO: get Controller folder name from preferences
     String folderName = "Model";
     return Pattern.compile(folderName, Pattern.CASE_INSENSITIVE);
   }
 
   private Pattern getModelFileRegex()
   {
     // TODO: get Controller folder name from preferences
     String fileRegex = ".*\\.php";
     return Pattern.compile(fileRegex, Pattern.CASE_INSENSITIVE);
   }
   
   private boolean hasParentFolderAndNameMatches(IFile file, Pattern filePattern, Pattern folderPattern, int maxParentsToCheck)
   {
     if (file == null)
     {
       return false;
     }
     
     String fileName = file.getName();
     boolean isFile = filePattern.matcher(fileName).matches();
     // file name doesn't match, can't be what we are looking for
     if (!isFile)
     {
       return false;
     }
     // just checking file name, not checking parent folder
     if (maxParentsToCheck < 1)
     {
       return true;
     }
     
     IPath filePath = file.getProjectRelativePath();
     int segmentCount = filePath.segmentCount();
     // (segmentCount - 2) ....... 3....2....1
     int folderStart = segmentCount - 2;
     for (int i = 0; i < maxParentsToCheck && i < folderStart; ++i)
     {
       String folderName = filePath.segment(folderStart - i);
       boolean folderMatches = folderPattern.matcher(folderName).matches();
       if (folderMatches)
       {
         return true;
       }
     }
     return false;
   }
 
   @Override
   public boolean isController(IFile file)
   {
     return hasParentFolderAndNameMatches(file, getControllerFileRegex(), getControllerFolderRegex(), 1);
   }
 
   private Pattern getControllerFolderRegex()
   {
     // TODO: get Controller folder name from preferences
     String folderName = "Controller";
     return Pattern.compile(folderName, Pattern.CASE_INSENSITIVE);
   }
 
   private Pattern getControllerFileRegex()
   {
     // TODO: get Controller folder name from preferences
     String fileRegex = ".*Controller\\.php";
     return Pattern.compile(fileRegex, Pattern.CASE_INSENSITIVE);
   }
 
   @Override
   public boolean isView(IFile file)
   {
     // TODO: read max parents to check from project settings..cake 1.x -> 1,, 2.x -> 2
     int maxParentsToCheck = 2;
     return hasParentFolderAndNameMatches(file, getViewFileRegex(), getViewFolderRegex(), maxParentsToCheck);    
   }
 
   private Pattern getViewFolderRegex()
   {
     // TODO: get Controller folder name from preferences
     String folderName = "View";
     return Pattern.compile(folderName, Pattern.CASE_INSENSITIVE);
   }
 
   private Pattern getViewFileRegex()
   {
     // TODO: get Controller folder name from preferences
     String fileRegex = ".*\\.ctp";
     return Pattern.compile(fileRegex, Pattern.CASE_INSENSITIVE);
   }
 
   @Override
   public boolean isJSFile(IFile file)
   {
     // TODO: read max parents to check from project settings..cake 1.x -> 1,, 2.x -> 2
     int maxParentsToCheck = 2;
     return hasParentFolderAndNameMatches(file, getJSFileRegex(), getJSFolderRegex(), maxParentsToCheck);    
   }
 
   private Pattern getJSFolderRegex()
   {
     // TODO: get Controller folder name from preferences
     String folderName = "js";
     return Pattern.compile(folderName, Pattern.CASE_INSENSITIVE);
   }
 
   private Pattern getJSFileRegex()
   {
     // TODO: get Controller folder name from preferences
     String fileRegex = ".*\\.js";
     return Pattern.compile(fileRegex, Pattern.CASE_INSENSITIVE);
   }
 
   @Override
   public boolean isElement(IFile file)
   {
     int maxParentsToCheck = 1;
     return hasParentFolderAndNameMatches(file, getElementFileRegex(), getElementFolderRegex(), maxParentsToCheck);    
   }
 
   private Pattern getElementFolderRegex()
   {
     // TODO: get Controller folder name from preferences
     String folderName = "Elements";
     return Pattern.compile(folderName, Pattern.CASE_INSENSITIVE);
   }
 
   private Pattern getElementFileRegex()
   {
     // TODO: get Controller folder name from preferences
     String fileRegex = ".*\\.ctp";
     return Pattern.compile(fileRegex, Pattern.CASE_INSENSITIVE);
   }
 
   @Override
   public ICakePHPFile getFileToOpen()
   {
     IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
     if (page == null)
     {
       return null;
     }
     IFile file = null;
     ISelection selection = page.getSelection();
 
     if (selection instanceof ITextSelection)
     {
       IEditorInput editorInput = page.getActiveEditor().getEditorInput();
       if (editorInput instanceof IFileEditorInput)
       {
         file = ((IFileEditorInput) editorInput).getFile();
       }
     }
     else if (selection instanceof IStructuredSelection && !selection.isEmpty())
     {
       Object element = ((IStructuredSelection) selection).getFirstElement();
       if (element instanceof IFile)
       {
         file = (IFile) element;
       }
       else if (element instanceof IMethod)
       {
         IResource r = ((IMethod) element).getResource();
         if (r != null)
         {
           file = (IFile) r.getAdapter(IFile.class);
         }
       }
     }
     
     if (file != null)
     {
       ICakePHPFile currentFile = getCakePHPFile(file);
       if (currentFile != null)
       {
         switch (currentFile.getCakePHPFileType())
         {
           case MODEL:
           {
             return getController(currentFile);
           }
           case CONTROLLER:
           {
             break;
           }
           case VIEW:
           {
             return getModel(currentFile);
           }
           case JSFILE:
           {
             break;
           }
           case ELEMENT:
           {
             break;
           }
         }
       }
     }
     return null;
   }
   
   @Override
   public boolean openNextFile()
   {
     ICakePHPFile cakePHPFile = getFileToOpen();
     if (cakePHPFile == null)
     {
       return false;
     }
     IFile destinationFile = cakePHPFile.getFile();
     if (destinationFile == null)
     {
       return false;
     }
     try
     {
       // TODO: check in preferences to see if automatically create files or prompt or do nothing
       if (!destinationFile.exists())
       {
         // currently there's a bug that the file won't get created the first time after the folder is created.
         // so the user has to perform the action twice
         IPath fullPath = destinationFile.getLocation();
         if (fullPath.toFile().getParentFile().mkdirs())
         {
           // create Eclipse resource so that the create file doesn't blow chunks
           destinationFile.getProject().getFile(destinationFile.getParent().getProjectRelativePath()).refreshLocal(IFile.DEPTH_ZERO, null);
         }
         String initialContent = cakePHPFile.getInitialContents();
         byte[] initialBytes = new byte[0];
         if (initialContent != null)
         {
           initialBytes = initialContent.getBytes();
         }
         destinationFile.create(new ByteArrayInputStream(initialBytes), false, null);
       }
       if (destinationFile.exists())
       {
         IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), destinationFile);
       }
       return true;
     }
     catch (CoreException e)
     {
       // TODO: how to log error messages
       String clazz = destinationFile.getName();
       System.err.println("OpenCakeFile can not open file: " + clazz);
       e.printStackTrace();
       return false;
     }
   }
   
   @Override
   public IPath getWebrootFolder()
   {
     // TODO: read webroot from preferences...allow project specific preferences too
     String webroot = "webroot";
     return getAppFolder().append(webroot);
   }
 
   @Override
   public IPath getJSRootFolder()
   {
     // TODO: read from preferences...allow project specific preferences too
     String jsRootName = "js";
     return getWebrootFolder().append(jsRootName);
   }
   
   @Override
   public IPath getJSFolder(IController controller)
   {
     // TODO: read from preferences...allow project specific preferences too
     String jsFolderName = controller.getName();
     return getJSRootFolder().append(jsFolderName);
   }
 
   @Override
   public IPath getViewRootFolder()
   {
     // TODO: read from preferences...allow project specific preferences too
     String viewRoot = "View";
     return getAppFolder().append(viewRoot);
   }
 
   @Override
   public IPath getElementsFolder()
   {
     // TODO: read from preferences...allow project specific preferences too
     String elementsFolderName = "Elements";
     return getViewRootFolder().append(elementsFolderName);
   }
 
   @Override
   public IFile getJSFile(IController controller, ICakeAction action)
   {
     // TODO: get js file name
     String jsName = action.getName();
     IPath jsFilePath = getJSFolder(controller).append(jsName);
     return this.project.getFile(jsFilePath);
   }
 
   @Override
   public IFile getViewFile(IController controller, ICakeAction action)
   {
     // TODO: get js file name
     String viewFolderName = action.getName();
     IPath viewFilePath = getViewFolder(controller).append(viewFolderName);
     return this.project.getFile(viewFilePath);
   }
 
   @Override
   public IController getController(ICakePHPFile file)
   {
     switch (file.getCakePHPFileType())
     {
       case MODEL:
       {
         return getController(file);
       }
       case CONTROLLER:
       {
         break;
       }
       case VIEW:
       {
         //return getModel(file);
         break;
       }
       case JSFILE:
       {
         break;
       }
       case ELEMENT:
       {
         break;
       }
     }
    return null;
   }
 
   @Override
   public IModel getModel(ICakePHPFile file)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public IPath getAppFolder()
   {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public IPath getViewFolder(IController controller)
   {
     // TODO Auto-generated method stub
     return null;
   }
 
 }
