 package org.ow2.mindEd.adl.editor.graphic.ui.custom.part;
 
 import java.io.IOException;
 import java.util.Collections;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.operations.OperationHistoryFactory;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
 import org.eclipse.gmf.runtime.common.core.command.CommandResult;
 import org.eclipse.gmf.runtime.diagram.core.services.ViewService;
 import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
 import org.eclipse.gmf.runtime.emf.core.GMFEditingDomainFactory;
 import org.eclipse.gmf.runtime.notation.Diagram;
 
 import org.ow2.mindEd.adl.AdlDefinition;
 import org.ow2.mindEd.adl.editor.graphic.ui.edit.parts.AdlDefinitionEditPart;
 import org.ow2.mindEd.adl.editor.graphic.ui.part.Messages;
 import org.ow2.mindEd.adl.editor.graphic.ui.part.MindDiagramEditorPlugin;
 import org.ow2.mindEd.adl.editor.graphic.ui.part.MindDiagramEditorUtil;
 
 public class CustomMindDiagramEditorUtil extends MindDiagramEditorUtil {
 
 	
 	/**
 	 * This method should be called within a workspace modify operation since it creates resources.
	 * @generated
 	 */
 	public static Resource initDiagram(URI diagramURI, URI modelURI, IProgressMonitor progressMonitor) {
 		TransactionalEditingDomain editingDomain = GMFEditingDomainFactory.INSTANCE
 				.createEditingDomain();
 		progressMonitor.beginTask(
 				Messages.MindDiagramEditorUtil_CreateDiagramProgressTask, 3);
 		final Resource diagramResource = editingDomain.getResourceSet()
 				.createResource(diagramURI);
 		// -- Get existing diagram
 		final Resource modelResource = editingDomain.getResourceSet().
 				getResource(modelURI, true);
 		final String diagramName = diagramURI.lastSegment();
 		AbstractTransactionalCommand command = new AbstractTransactionalCommand(
 				editingDomain,
 				Messages.MindDiagramEditorUtil_CreateDiagramCommandLabel,
 				Collections.EMPTY_LIST) {
 			protected CommandResult doExecuteWithResult(
 					IProgressMonitor monitor, IAdaptable info)
 					throws ExecutionException {
 				// -- Get existing model
 				AdlDefinition model = (AdlDefinition) modelResource.getContents().get(0);			
 //				attachModelToResource(model, modelResource);
 
 				Diagram diagram = ViewService.createDiagram(model,
 						AdlDefinitionEditPart.MODEL_ID,
 						MindDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
 				if (diagram != null) {
 					diagramResource.getContents().add(diagram);
 					diagram.setName(diagramName);
 					diagram.setElement(model);
 				}
 
 				try {
 					diagramResource.save(org.ow2.mindEd.adl.editor.graphic.ui.part.MindDiagramEditorUtil
 							.getSaveOptions());
 				} catch (IOException e) {
 					MindDiagramEditorPlugin.getInstance().logError(
 							"Unable to store model and diagram resources", e); //$NON-NLS-1$
 				}
 				return CommandResult.newOKCommandResult();
 			}
 		};
 		try {
 			OperationHistoryFactory.getOperationHistory().execute(command,
 					new SubProgressMonitor(progressMonitor, 1), null);
 			setCharset(WorkspaceSynchronizer.getFile(modelResource));
 			setCharset(WorkspaceSynchronizer.getFile(diagramResource));
 		} catch (ExecutionException e) {
 			MindDiagramEditorPlugin.getInstance().logError(
 					"Unable to create diagram", e); //$NON-NLS-1$
 		}
 		
 		
 		return diagramResource;
 	}
 	
 
 	
 	/**
 	 * Store model element in the resource.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 */
 	@SuppressWarnings("unused")
 	private static void attachModelToResource(AdlDefinition model,
 			Resource resource) {
 		resource.getContents().add(model);
 	}
 	
 	
 	
 }
