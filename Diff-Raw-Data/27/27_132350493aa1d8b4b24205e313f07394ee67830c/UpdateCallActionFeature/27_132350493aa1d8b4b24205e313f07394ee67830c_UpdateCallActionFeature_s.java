 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Philip Langer - initial API and implementation
  */
 package org.modelexecution.xmof.diagram.features;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
 import org.eclipse.graphiti.features.context.impl.AddContext;
 import org.eclipse.graphiti.features.context.impl.AreaContext;
 import org.eclipse.graphiti.features.context.impl.RemoveContext;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.CallAction;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.Pin;
 import org.modelexecution.xmof.Syntax.Activities.CompleteStructuredActivities.StructuredActivityNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.Activity;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ActivityEdge;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ActivityNode;
 
 public abstract class UpdateCallActionFeature extends UpdateActionFeature {
 
 	public UpdateCallActionFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 	
 	protected void removeActionDiagramRepresentation(CallAction action) {
 		PictogramElement pictogramElement = getFeatureProvider()
 				.getPictogramElementForBusinessObject(action);
 
 		IRemoveContext removeContext = new RemoveContext(pictogramElement);
 		IRemoveFeature removeFeature = getFeatureProvider().getRemoveFeature(
 				removeContext);
 		if (removeFeature != null) {
 			removeFeature.remove(removeContext);
 		}
 	}
 
 	protected void addActionDiagramRepresentation(CallAction action, int x, int y) {
 		addActionRepresentation(action, x, y);
 		addEdgeRepresentation(action);
 	}
 
 	private void addActionRepresentation(CallAction action, int x, int y) {
 		AreaContext areaContext = new AreaContext();
 		areaContext.setSize(100, 100);
 		AddContext addContext = new AddContext(areaContext, action);
 		addContext.setLocation(x, y);
		PictogramElement targetContainer = getFeatureProvider()
				.getPictogramElementForBusinessObject(action.getActivity());
		addContext.setTargetContainer((ContainerShape) targetContainer);
		addGraphicalRepresentation(addContext, action);
 	}
 	
 	private void addEdgeRepresentation(CallAction action) {
 		EList<ActivityEdge> edges = new BasicEList<ActivityEdge>();
 		edges.addAll(action.getIncoming());
 		edges.addAll(action.getOutgoing());
 		for(ActivityEdge edge : edges) {
 			ActivityNode source = edge.getSource();
 			ActivityNode target = edge.getTarget();			
 			PictogramElement sourcePe = getFeatureProvider().getPictogramElementForBusinessObject(source);
 			PictogramElement targetPe = getFeatureProvider().getPictogramElementForBusinessObject(target);
 			Anchor sourceAnchor = ((Shape)sourcePe).getAnchors().get(0);
 			Anchor targetAnchor = ((Shape)targetPe).getAnchors().get(0);
 			AddConnectionContext connectionContext = new AddConnectionContext(sourceAnchor, targetAnchor);
 			connectionContext.setNewObject(edge);			
 			
 			IAddFeature addFeature = getFeatureProvider().getAddFeature(connectionContext);
 			if(addFeature != null) {
 				addFeature.add(connectionContext);
 			}			
 		}
 	}	
 	
 	protected void deletePins(CallAction action) {
 		EList<Pin> pins = new BasicEList<Pin>();
 		pins.addAll(action.getInput());
 		pins.addAll(action.getOutput());
 		
 		for(Pin pin : pins) {
 			deleteEdges(pin);
 		}		
 		
 		action.getArgument().clear();
 		action.getInput().clear();
 		action.getResult().clear();
 		action.getOutput().clear();
 	}
 	
 	private void deleteEdges(Pin pin) {
 		EList<ActivityEdge> edges = new BasicEList<ActivityEdge>();
 		edges.addAll(pin.getIncoming());
 		edges.addAll(pin.getOutgoing());
 		
 		pin.getIncoming().clear();
 		pin.getOutgoing().clear();
 		
 		for(ActivityEdge edge : edges) {
 			Activity activity = edge.getActivity();
 			StructuredActivityNode structuredNode = edge.getInStructuredNode();
 			
 			if(activity != null) {
 				activity.getEdge().remove(edge);
 			} else if(structuredNode != null) {
 				structuredNode.getEdge().remove(edge);
 			}				
 		}
 	}
 	
 }
