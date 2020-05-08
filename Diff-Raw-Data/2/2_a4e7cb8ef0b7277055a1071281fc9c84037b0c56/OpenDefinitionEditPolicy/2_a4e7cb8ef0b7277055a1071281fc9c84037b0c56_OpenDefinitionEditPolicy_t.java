 package org.ow2.mindEd.adl.editor.graphic.ui.custom.edit.policies;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.RootEditPart;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.OpenEditPolicy;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 
 import org.ow2.mindEd.adl.AdlDefinition;
 import org.ow2.mindEd.adl.ComponentReference;
 import org.ow2.mindEd.adl.FileC;
 import org.ow2.mindEd.adl.ImportDefinition;
 
 import org.ow2.mindEd.adl.custom.util.DefinitionLoaderUtil;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.part.CustomMindDiagramEditorUtil;
 import org.ow2.mindEd.adl.editor.graphic.ui.part.MindDiagramEditorPlugin;
 import org.ow2.mindEd.ide.core.ModelToProjectUtil;
 import org.ow2.mindEd.ide.model.MindPackage;
 import org.ow2.mindEd.ide.model.MindProject;
 
 /**
  * Install this edit policy on a ComponentReference or a FileC to make it
  * open the referenced file (.adl or .c). If you want to install it on another edit part,
  * you may need to implement the open behaviour in getOpenCommand()
  * @author maroto
  *
  */
 public class OpenDefinitionEditPolicy extends OpenEditPolicy {
 	
 	public static final QualifiedName EDITOR_KEY = new QualifiedName(
 			"org.eclipse.ui.internal.registry.ResourceEditorRegistry", "EditorProperty");
 
 	public OpenDefinitionEditPolicy() {
 		super();
 	}
 
 	/**
 	 * This method is called by a double-clic on the edit part which
 	 * installed this edit policy.<p>
 	 * This will open another page with the proper editor, depending on which item
 	 * was clicked : <p>
 	 * reference -> opens the definition in a diagram editor<p>
 	 * FileC -> opens the file in the cdt editor (TODO)
 	 */
 	@Override
 	protected Command getOpenCommand(Request request) {
 		EObject model = ((View)getHost().getModel()).getElement();
 		// TODO return an open command instead of opening right here
 		
 		// The item is a reference : try to open the definition in a diagram editor
 		if (model instanceof ComponentReference) {
 			// Get the referenced component
 			String name = ((ComponentReference) model).getReferenceName();
 			// Get imports to resolve the reference
 			ArrayList<String> importsList = new ArrayList<String>();
 			RootEditPart root = getHost().getRoot();
 			if (root != null && root.getContents()!=null) {
 				View rootView = (View)root.getContents().getModel();
 				if (rootView != null) {
 					EObject adl = rootView.getElement();
 					if (adl != null && adl instanceof AdlDefinition) {
 						EList<ImportDefinition> imports = ((AdlDefinition) adl).getImports();
 						for (ImportDefinition temp : imports) {
 							importsList.add(temp.getImportName());
 						}
 					}
 				}
 			}
 			
 			try {
 				// Get the resource by resolving the reference
 				URI modelURI = DefinitionLoaderUtil.getInstance().getResourcePath(name,importsList);
 				if (modelURI == null) {
 					MindDiagramEditorPlugin.getInstance().logError("Reference not found, please check imports");
 					return null;
 				}
 				IFile definition = ModelToProjectUtil.INSTANCE.getIFile(modelURI);
 				IEditorInput adlEditorInput = new FileEditorInput(definition);
 				
 				// Get the diagram resource
 				URI diagramURI = URI.createFileURI(modelURI.path()+"_diagram");
 				IFile diagram = ModelToProjectUtil.INSTANCE.getIFile(diagramURI);
 				IEditorInput diagramEditorInput = new FileEditorInput(diagram);
 				
 				// Diagram file doesn't exist, but adl file exists (else modelURI would be null)
 				// So create the diagram
 				if (!diagramEditorInput.exists()) {
 					CustomMindDiagramEditorUtil.initDiagram(diagramURI, modelURI, new NullProgressMonitor());
 				}
 				
 				// Now try to open the editors
 				IWorkbenchWindow window=PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 				IWorkbenchPage page = window.getActivePage();
 				String editorID = definition.getPersistentProperty(EDITOR_KEY);
 				if (editorID != null && editorID.equals("org.ow2.mindEd.adl.editor.graphic.ui.part.MindDiagramEditorID")) {
 					page.openEditor(diagramEditorInput, editorID);
 				}
 				else {
					page.openEditor(adlEditorInput, "org.ow2.mindEd.adl.textual.Fractal");
 				}
 				
 			} catch (PartInitException e) {
 				e.printStackTrace();
 				MindDiagramEditorPlugin.getInstance().logError("Failed to open the editor");
 			} catch (NullPointerException e) {
 				e.printStackTrace();
 				MindDiagramEditorPlugin.getInstance().logError("Failed to open the editor");
 			} catch (Exception e) {
 				e.printStackTrace();
 				MindDiagramEditorPlugin.getInstance().logError("Failed to open the editor");
 			}
 		}
 			
 		if (model instanceof FileC) {
 			String directory = ((FileC) model).getDirectory();
 			if (directory == null || directory == "") {
 				MindProject project = ModelToProjectUtil.INSTANCE.getMindProject();
 				MindPackage pack = ModelToProjectUtil.INSTANCE.getPackage(project);
 				directory = pack.getFullpath();
 			}
 			if (!directory.endsWith("/"))
 				directory = directory + "/";
 			String fileName = ((FileC) model).getFileName();
 			String fullPath = directory + fileName;
 			
 			try {
 				// Get the file URI
 				URI fileURI = URI.createFileURI(fullPath);
 				
 				// Create the editor input
 				IFile file = ModelToProjectUtil.INSTANCE.getIFile(fileURI);
 				if (file == null || !(file.exists())) {
 					MindDiagramEditorPlugin.getInstance().logError("File not found : "+fullPath);
 					return null;
 				}
 				
 				IEditorInput cdtEditorInput = new FileEditorInput(file);
 				
 								
 				// Now try to open the editors
 				IWorkbenchWindow window=PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 				IWorkbenchPage page = window.getActivePage();
 				page.openEditor(cdtEditorInput, "org.eclipse.cdt.ui.editor.CEditor");
 				
 			} catch (PartInitException e) {
 				MindDiagramEditorPlugin.getInstance().logError("Failed to open the editor");
 			} catch (NullPointerException e) {
 				MindDiagramEditorPlugin.getInstance().logError("Failed to open the editor");
 			} catch (Exception e) {
 				e.printStackTrace();
 				MindDiagramEditorPlugin.getInstance().logError("Failed to open the editor");
 			}
 		}
 		
 		return null;
 	}
 
 }
