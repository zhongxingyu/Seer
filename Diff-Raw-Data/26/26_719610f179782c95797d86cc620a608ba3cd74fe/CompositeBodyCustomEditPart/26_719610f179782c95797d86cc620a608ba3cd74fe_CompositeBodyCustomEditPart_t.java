 package org.ow2.fractal.mind.diagram.custom.edit.parts;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.gef.ConnectionEditPart;
 import org.eclipse.gef.DragTracker;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.Request;
 import org.eclipse.gmf.runtime.notation.View;
 import org.ow2.fractal.mind.diagram.custom.edit.parts.generic.MindEditPart;
 import org.ow2.fractal.mind.diagram.custom.edit.parts.generic.MindGenericEditPartFactory;
 
 import adl.diagram.edit.parts.CompositeBodyEditPart;
 
 public class CompositeBodyCustomEditPart extends CompositeBodyEditPart {
 
 	public CompositeBodyCustomEditPart(View view) {
 		super(view);
 	}
 	
 	@Override
 	public void createDefaultEditPolicies() {
 		super.createDefaultEditPolicies();
 		genericEditPart.createDefaultEditPolicies();
 	}
 	
 	
 	@Override
 	protected boolean addFixedChild(EditPart childEditPart) {
 		if (genericEditPart.addFixedChild(childEditPart)) return true;
 		return super.addFixedChild(childEditPart);
 	}
 	
	protected boolean removeFixedChild(EditPart childEditPart) {
		if (genericEditPart.removeFixedChild(childEditPart)) return true;
		return super.removeFixedChild(childEditPart);
	}
	
 	@Override
 	protected void addChildVisual(EditPart childEditPart, int index) {
 		if(genericEditPart.addChildVisual(childEditPart, index))
 			return;
 		super.addChildVisual(childEditPart, index);
 	}
 	
 
 	
 	@Override
 	public DragTracker getDragTracker(Request request) {
 		DragTracker tracker = genericEditPart.getDragTracker(request);
 		if (tracker == null)
 			tracker = super.getDragTracker(request);
 		return tracker;
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	protected void refreshSourceConnections()
 	{
 		Map modelToEditPart = new HashMap();
 		List editParts = getSourceConnections();
 		
 		 for (int i = 0; i < editParts.size(); ++i) {
 			 ConnectionEditPart editPart = (ConnectionEditPart)editParts.get(i);
 			 modelToEditPart.put(editPart.getModel(), editPart);
 		 }
 		
 		List modelObjects = getModelSourceConnections();
 		if (modelObjects == null) modelObjects = new ArrayList();
 		
 		for (int i = 0; i < modelObjects.size(); ++i) {
 			Object model = modelObjects.get(i);
 		
 			if ((i < editParts.size()) && 
 					(((EditPart)editParts.get(i)).getModel() == model)) {
 				continue;
 			}
 			ConnectionEditPart editPart = (ConnectionEditPart)modelToEditPart.get(model);
 			if (editPart != null) {
 				reorderSourceConnection(editPart, i);
 			} else {
 				editPart = createOrFindConnection(model);
 				addSourceConnection(editPart, i);
 			}
 	
 		}
 
 		List trash = new ArrayList();
 		for (int i=0 ; i < editParts.size(); ++i)
 			trash.add(editParts.get(i));
 		for (int i = 0; i < trash.size(); ++i)
 			removeSourceConnection((ConnectionEditPart)trash.get(i));
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	protected void refreshTargetConnections()
 	{
 		Map mapModelToEditPart = new HashMap();
 		List connections = getTargetConnections();
 		
 		for (int i = 0; i < connections.size(); ++i) {
 			ConnectionEditPart editPart = (ConnectionEditPart)connections.get(i);
 			mapModelToEditPart.put(editPart.getModel(), editPart);
 		}
 		
 		List modelObjects = getModelTargetConnections();
 		if (modelObjects == null) modelObjects = new ArrayList();
 		 
 		for (int i = 0; i < modelObjects.size(); ++i) {
 			Object model = modelObjects.get(i);
 		
 			if ((i < connections.size()) && 
 					(((EditPart)connections.get(i)).getModel() == model)) {
 				continue;
 			}
 			ConnectionEditPart editPart = (ConnectionEditPart)mapModelToEditPart.get(model);
 			if (editPart != null) {
 				reorderTargetConnection(editPart, i);
 			} else {
 				editPart = createOrFindConnection(model);
 				addTargetConnection(editPart, i);
 			}
 	
 		}	
 
 		List trash = new ArrayList();
 		for (int i=0; i < connections.size(); ++i)
 			trash.add(connections.get(i));
 		for (int i = 0; i < trash.size(); ++i)
 			removeTargetConnection((ConnectionEditPart)trash.get(i));
 	}
 
 }
