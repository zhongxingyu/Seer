 package org.eclipse.vjet.eclipse.ui.handlers;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.tools.ant.filters.StringInputStream;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.mod.core.DLTKCore;
 import org.eclipse.dltk.mod.core.IBuildpathAttribute;
 import org.eclipse.dltk.mod.core.IBuildpathEntry;
 import org.eclipse.dltk.mod.core.IScriptProject;
 import org.eclipse.dltk.mod.core.ModelException;
 import org.eclipse.dltk.mod.internal.core.BuildpathEntry;
 import org.eclipse.dltk.mod.internal.core.ScriptProject;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 /**
  * Our sample handler extends AbstractHandler, an IHandler base class.
  * @see org.eclipse.core.commands.IHandler
  * @see org.eclipse.core.commands.AbstractHandler
  */
 public class AddBootstrapEntryToVJETProject extends AbstractHandler {
 	/**
 	 * The constructor.
 	 */
 	public AddBootstrapEntryToVJETProject() {
 	}
 
 	/**
 	 * the command has been executed, so extract extract the needed information
 	 * from the application context.
 	 */
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		ISelection base = HandlerUtil.getActiveMenuSelection(event);
 		if (base instanceof IStructuredSelection) {
 			IStructuredSelection selection = (IStructuredSelection) base;
 			Object firstElem = selection.getFirstElement();
 			if(firstElem instanceof ScriptProject){
 				ScriptProject project = (ScriptProject)firstElem;
 				// don't add if path exists
 				IFolder bootstrapFolder = project.getProject().getFolder("bootstrap");
 				if(!bootstrapFolder.exists()){
 					try {
 						bootstrapFolder.create(true,true,null);
 						IFile bootstrapjsfile = bootstrapFolder.getFile("bootstrap.js");
 						if(!bootstrapjsfile.exists()){
							bootstrapjsfile.create(new StringInputStream("typeExtensions = {} // \"typetoextend\" : \"typewhichdefinesextension\")"), true, null);
 						}
 					} catch (CoreException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				// add bootstrap file to project under bootstrap folder
 				
 				// add bootstrap entry to .buildpath file
 				
 				
 				if(!containsBootstrap(project)){
 				
 					List<IBuildpathEntry> bentries = new ArrayList<IBuildpathEntry>();
 					IBuildpathEntry bootstrapEntry = DLTKCore.newBootstrapEntry(new Path("bootstrap"), null, 
 							BuildpathEntry.INCLUDE_ALL, BuildpathEntry.EXCLUDE_NONE,  new IBuildpathAttribute[]{}, false);
 					bentries.add(bootstrapEntry);
 					try {
 						addEntriesToBuildPath(project, bentries);
 					} catch (ModelException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				
 			}
 		}
 		
 		return null;
 	}
 	
 	private boolean containsBootstrap(ScriptProject project)  {
 		IBuildpathEntry[] rawBuildpath = null;
 		try {
 			rawBuildpath = project.getRawBuildpath();
 		} catch (ModelException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		for (IBuildpathEntry buildpathEntry : rawBuildpath) {
 			if(buildpathEntry.getEntryKind()==BuildpathEntry.BPE_BOOTSTRAP){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Adds the given entries to the Build Path
 	 * 
 	 * @param scriptProject
 	 * @param entries
 	 * @throws ModelException
 	 */
 	public void addEntriesToBuildPath(IScriptProject scriptProject,
 			List<IBuildpathEntry> entries) throws ModelException {
 		IBuildpathEntry[] rawBuildpath = scriptProject.getRawBuildpath();
 
 		// get the current buildpath entries, in order to add/remove entries
 		Set<IBuildpathEntry> newRawBuildpath = new HashSet<IBuildpathEntry>();
 
 		// get all of the source folders and the language library from the
 		// existing build path
 		for (IBuildpathEntry buildpathEntry : rawBuildpath) {
 			newRawBuildpath.add(buildpathEntry);
 		}
 		// add all of the entries added in this dialog
 		for (IBuildpathEntry buildpathEntry : entries) {
 			newRawBuildpath.add(buildpathEntry);
 		}
 
 		// set the new updated buildpath for the project
 		scriptProject.setRawBuildpath(newRawBuildpath
 				.toArray(new IBuildpathEntry[newRawBuildpath.size()]), null);
 
 	}
 }
