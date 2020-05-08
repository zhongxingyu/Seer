 package org.ow2.mindEd.adl.editor.graphic.ui.custom.providers;
 
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.gef.DragTracker;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gmf.runtime.diagram.ui.tools.DragEditPartsTrackerEx;
 import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientRelationshipRequest;
 import org.eclipse.gmf.runtime.notation.impl.ConnectorImpl;
 import org.eclipse.gmf.runtime.notation.impl.ShapeImpl;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.edit.commands.BindingDefinitionCustomReorientCommand;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.edit.parts.BindingDefinitionCustomEditPart;
 
 import org.ow2.mindEd.adl.editor.graphic.ui.edit.parts.AdlDefinitionEditPart;
 import org.ow2.mindEd.adl.editor.graphic.ui.edit.parts.InterfaceDefinitionEditPart;
 import org.ow2.mindEd.adl.impl.BindingDefinitionImpl;
 import org.ow2.mindEd.adl.impl.InterfaceDefinitionImpl;
 
 /**
  * Custom drag tracker to implement extended drag and drop features :<p>
  * - do not drop on canvas or template sub component<p>
  * - check if the bindings on the dragged component still have the right container,
  * change it otherwise<p>
  * This tracker should be used by all component's main edit parts
  * @author maroto
  *
  */
 public class DragEditPartsCustomTracker extends DragEditPartsTrackerEx implements DragTracker {
 
 	public DragEditPartsCustomTracker(EditPart sourceEditPart) {
 		super(sourceEditPart);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public void performDrag() {
		// Prevent from dropping on canvas
 		if (getTargetEditPart() instanceof AdlDefinitionEditPart
 				&& !(getSourceEditPart().getParent() instanceof AdlDefinitionEditPart)) return;
 		
 		super.performDrag();
 		
 		if (getSourceEditPart().getParent() == null) {
 		// This means the edit part was dragged to another component
 		// So we must recalculate the binding's container
 			
 			//TODO optimisation de l'algo
 			
 			//Seeking interfaces
 			List<EditPart> ComponentChildrenList = getSourceEditPart().getChildren();
 			EditPart componentChild = ComponentChildrenList.get(0);
 			for (int i=0; i < ComponentChildrenList.size(); i++) {
 			componentChild = ComponentChildrenList.get(i);
 				if (componentChild instanceof InterfaceDefinitionEditPart) {
 					//Seeking related bindings in source connections
 					List<EditPart> interfaceSourcesList = ((InterfaceDefinitionEditPart) componentChild).getSourceConnections();
 					
 					for (int j = 0; j < interfaceSourcesList.size(); j++) {
 						EditPart interfaceSource = interfaceSourcesList.get(j);
 						if (interfaceSource instanceof BindingDefinitionCustomEditPart) {
 							//And execute a reorient command to reset the container
 							//if the binding is not being deleted
 							BindingDefinitionImpl connectorImpl = (BindingDefinitionImpl)((ConnectorImpl)interfaceSource.getModel()).getElement();
 							InterfaceDefinitionImpl interfaceImpl = (InterfaceDefinitionImpl)((ShapeImpl)componentChild.getModel()).getElement();
 							if (connectorImpl != null && interfaceImpl != null) {
 								ReorientRelationshipRequest reorientRequest = new ReorientRelationshipRequest(connectorImpl,interfaceImpl,interfaceImpl,1);
 								BindingDefinitionCustomReorientCommand reorientCommand = new BindingDefinitionCustomReorientCommand(reorientRequest);
 								if (reorientCommand.canExecute()) {
 									try {
 										reorientCommand.execute(null, null);
 									} catch (ExecutionException e) {
 										// TODO Auto-generated catch block
 										e.printStackTrace();
 									}
 								}
 								interfaceSource.refresh();	
 							}
 						}
 					}
 					//Seeking related bindings in target connections				
 					List<EditPart> InterfaceTargetsList = ((InterfaceDefinitionEditPart) componentChild).getTargetConnections();
 					
 					for (int h = 0; h < InterfaceTargetsList.size(); h++) {			
 						EditPart interfaceTarget = InterfaceTargetsList.get(h);
 						if (interfaceTarget instanceof BindingDefinitionCustomEditPart) {
 							//And execute a reorient command to reset the container
 							//if the binding is not being deleted
 							BindingDefinitionImpl connectorImpl = (BindingDefinitionImpl)((ConnectorImpl)interfaceTarget.getModel()).getElement();
 							InterfaceDefinitionImpl interfaceImpl = (InterfaceDefinitionImpl)((ShapeImpl)componentChild.getModel()).getElement();
 							if (connectorImpl != null && interfaceImpl != null) {
 								ReorientRelationshipRequest reorientRequest = new ReorientRelationshipRequest(connectorImpl,interfaceImpl,interfaceImpl,1);
 								BindingDefinitionCustomReorientCommand reorientCommand = new BindingDefinitionCustomReorientCommand(reorientRequest);
 								if (reorientCommand.canExecute()) {
 									try {
 										reorientCommand.execute(null, null);
 									} catch (ExecutionException e) {
 										// TODO Auto-generated catch block
 										e.printStackTrace();
 									}
 								}
 								interfaceTarget.refresh();
 							}
 						}
 					}
 				}
 			}
 		
 		}
 		
 	}
 }
