 package uk.ac.bham.cs.sdsts.handler;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.papyrus.infra.core.extension.commands.ICreationCommand;
 import org.eclipse.papyrus.infra.core.utils.DiResourceSet;
 import org.eclipse.papyrus.infra.core.utils.EditorUtils;
 import org.eclipse.papyrus.uml.diagram.sequence.CreateSequenceDiagramCommand;
 import org.eclipse.papyrus.uml.diagram.wizards.CreateModelWizard;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 import uk.ac.bham.cs.sdsts.View;
 import uk.ac.bham.cs.sdsts.common.ModelManager;
 import uk.ac.bham.cs.sdsts.common.SequenceDiagram;
 
 
 @SuppressWarnings({ "deprecation" })
 public class CallCreateSdFile extends AbstractHandler{
 
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
 	    IWorkbenchPage page = window.getActivePage();
 	    View view = (View) page.findView(View.ID);
 	    view.getViewer().refresh();
 	    FileDialog filedlg = new FileDialog(window.getShell(), SWT.SAVE);
 		filedlg.setText("Create Sequence Diagram File");
 		filedlg.setFilterPath("SystemRoot");
 		filedlg.setFilterExtensions(new String[]{"di"});
 		String selected=filedlg.open();
 		MyCreater myCreater = new MyCreater();
		myCreater.init(window.getWorkbench(), new StructuredSelection()); // fixed bug
 		IFile iFile = myCreater.create(selected);
 		
 		SequenceDiagram sdDiagram = new SequenceDiagram();
 		sdDiagram.setiFile(iFile);
 		sdDiagram.setFilePath(new Path(selected).removeFileExtension().addFileExtension("uml").toOSString());
 		ModelManager.getInstance().AddModel(sdDiagram);
 		
 		myCreater.open(iFile);
 		
 		view.getViewer().refresh();
 
 	    return null;
 	}
 	
 }
 class MyCreater extends CreateModelWizard{
 	public void open(IFile newFile){
 		openDiagram(newFile);
 	}
 	@SuppressWarnings("deprecation")
 	public IFile create(String fullpath){
 		DiResourceSet diResourceSet = new DiResourceSetExt();
 		final IFile newFile = createFile(fullpath);
 		process(diResourceSet, newFile, "uml", fullpath);
 		return newFile;
 	}
 	public static IFile createFile(String path){
 		IWorkspace ws = ResourcesPlugin.getWorkspace();
 		IProject project = ws.getRoot().getProject("tmp");
 		if (!project.exists())
 			try {
 				project.create(null);
 			} catch (CoreException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		if (!project.isOpen())
 			try {
 				project.open(null);
 			} catch (CoreException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		IPath iPath= new Path(path);
 		IFile iFile = project.getFile(iPath.lastSegment());
 		return iFile;
 		
 //		String name = path;
 //		IPath location = new Path(name);
 //		File file1 = location.toFile();
 //		
 //		try {
 //			file1.createNewFile();
 //		} catch (IOException e1) {
 //			// TODO Auto-generated catch block
 //			e1.printStackTrace();
 //		}
 //		IFile file = project.getFile(location.lastSegment());
 //		
 //		try {
 //			file.createLink(location, IResource.NONE, null);
 //		} catch (CoreException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 //		return file;
 	}
 	public void moveFile(String source, String targit, String filename, String ext){
 		File file = new File(String.format("%s/%s.%s", source, filename, ext));
 		file.renameTo(new File(String.format("%s/%s.%s", targit, filename, ext)));
 		LinkFile(String.format("%s/%s.%s", targit, filename, ext));
 	}
 	@SuppressWarnings("deprecation")
 	public void process(DiResourceSet diResourceSet, IFile newFile, String diagramCategoryId, String path){
 		
 		createPapyrusModels(diResourceSet, newFile);
 		
 		//initDomainModel(diResourceSet, newFile, diagramCategoryId);
 		createEmptyDomainModel(diResourceSet, diagramCategoryId);
 	
 
 		//initDiagrams(diResourceSet, null, "uml");
 		List<ICreationCommand> creationCommands = new ArrayList<ICreationCommand>();//getDiagramKindsFor("uml");
 		creationCommands.add((ICreationCommand) new CreateSequenceDiagramCommand());
 		String diagramName = "NewSequenceDiagram";
 		if(creationCommands.isEmpty()) {
 			EditorUtils.getTransactionalIPageMngr(diResourceSet.getDiResource(), diResourceSet.getTransactionalEditingDomain());
 		} else {
 			for(int i = 0; i < creationCommands.size(); i++) {
 				creationCommands.get(i).createDiagram(diResourceSet, null, diagramName);
 			}
 		}
 		
 		try {
 			diResourceSet.save(new NullProgressMonitor());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		String sourceDir = newFile.getLocation().removeLastSegments(1).toOSString();
 		String targitDir = new Path(path).removeLastSegments(1).toOSString();
 		String filename = newFile.getLocation().removeFileExtension().lastSegment();
 
 		moveFile(sourceDir, targitDir, filename, "di");
 		moveFile(sourceDir, targitDir, filename, "notation");
 		moveFile(sourceDir, targitDir, filename, "uml");
 
 //		openDiagram(newFile);
 	}
 	public static IFile LinkFile(String path){
 		IWorkspace ws = ResourcesPlugin.getWorkspace();
 		IProject project = ws.getRoot().getProject("tmp");
 		if (!project.exists())
 			try {
 				project.create(null);
 			} catch (CoreException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		if (!project.isOpen())
 			try {
 				project.open(null);
 			} catch (CoreException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		IPath location = new Path(path);
 		IFile file = project.getFile(location.lastSegment());
 		try {
 			file.delete(true, null);
 		} catch (CoreException e1) {
 		}
 		
 		try {
 			file.createLink(location, IResource.NONE, null);
 		} catch (CoreException e) {
 		}
 		return file;
 	}
 }
 
 
