 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Philip Langer - initial API and implementation
  */
 package org.modelexecution.xmof.diagram;
 
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.ICreateConnectionFeature;
 import org.eclipse.graphiti.features.ICreateFeature;
 import org.eclipse.graphiti.features.IDeleteFeature;
 import org.eclipse.graphiti.features.ILayoutFeature;
 import org.eclipse.graphiti.features.IMoveShapeFeature;
 import org.eclipse.graphiti.features.IReconnectionFeature;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.IResizeShapeFeature;
 import org.eclipse.graphiti.features.IUpdateFeature;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.IDeleteContext;
 import org.eclipse.graphiti.features.context.ILayoutContext;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.features.context.IReconnectionContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.Action;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.CallBehaviorAction;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.CallOperationAction;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.Pin;
 import org.modelexecution.xmof.Syntax.Activities.CompleteStructuredActivities.StructuredActivityNode;
 import org.modelexecution.xmof.Syntax.Activities.ExtraStructuredActivities.ExpansionNode;
 import org.modelexecution.xmof.Syntax.Activities.ExtraStructuredActivities.ExpansionRegion;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.Activity;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ActivityEdge;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ActivityNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ActivityParameterNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ControlFlow;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ControlNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.DecisionNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ForkNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.InitialNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.JoinNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.MergeNode;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ObjectFlow;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ObjectNode;
 import org.modelexecution.xmof.diagram.features.AddActionFeature;
 import org.modelexecution.xmof.diagram.features.AddActivityFeature;
 import org.modelexecution.xmof.diagram.features.AddDecisionMergeNodeFeature;
 import org.modelexecution.xmof.diagram.features.AddExpansionNodeFeature;
 import org.modelexecution.xmof.diagram.features.AddFlowFeature;
 import org.modelexecution.xmof.diagram.features.AddInitialNodeFeature;
 import org.modelexecution.xmof.diagram.features.AddJoinForkNodeFeature;
 import org.modelexecution.xmof.diagram.features.AddStructuredActivityNodeFeature;
 import org.modelexecution.xmof.diagram.features.CreateAddStructuralFeatureValueActionFeature;
 import org.modelexecution.xmof.diagram.features.CreateCallBehaviorActionFeature;
 import org.modelexecution.xmof.diagram.features.CreateCallOperationActionFeature;
 import org.modelexecution.xmof.diagram.features.CreateControlFlowFeature;
 import org.modelexecution.xmof.diagram.features.CreateDecisionNodeFeature;
 import org.modelexecution.xmof.diagram.features.CreateExpansionRegionFeature;
 import org.modelexecution.xmof.diagram.features.CreateForkNodeFeature;
 import org.modelexecution.xmof.diagram.features.CreateInitialNodeFeature;
 import org.modelexecution.xmof.diagram.features.CreateInputExpansionNodeFeature;
 import org.modelexecution.xmof.diagram.features.CreateJoinNodeFeature;
 import org.modelexecution.xmof.diagram.features.CreateMergeNodeFeature;
 import org.modelexecution.xmof.diagram.features.CreateObjectFlowFeature;
 import org.modelexecution.xmof.diagram.features.CreateOutputExpansionNodeFeature;
 import org.modelexecution.xmof.diagram.features.CreateReadIsClassifiedObjectActionFeature;
 import org.modelexecution.xmof.diagram.features.CreateReadSelfActionFeature;
 import org.modelexecution.xmof.diagram.features.CreateReadStructuralFeatureActionFeature;
 import org.modelexecution.xmof.diagram.features.CreateTestIdentityActionFeature;
 import org.modelexecution.xmof.diagram.features.CreateValueSpecificationActionFeature;
 import org.modelexecution.xmof.diagram.features.DeleteActionFeature;
 import org.modelexecution.xmof.diagram.features.DeleteActivityNodeFeature;
import org.modelexecution.xmof.diagram.features.DeleteActivityEdgeFeature;
 import org.modelexecution.xmof.diagram.features.DeleteExpansionRegionFeature;
 import org.modelexecution.xmof.diagram.features.DisallowReconnectActivityEdgeFeature;
 import org.modelexecution.xmof.diagram.features.DisallowDeleteActivityFeature;
 import org.modelexecution.xmof.diagram.features.DisallowDeletePinFeature;
 import org.modelexecution.xmof.diagram.features.DisallowMoveExpansionNodeFeature;
 import org.modelexecution.xmof.diagram.features.DisallowMovePinFeature;
 import org.modelexecution.xmof.diagram.features.DisallowRemoveActivityFeature;
 import org.modelexecution.xmof.diagram.features.DisallowRemoveActivityParameterNodeFeature;
 import org.modelexecution.xmof.diagram.features.DisallowRemovePinFeature;
 import org.modelexecution.xmof.diagram.features.DisallowResizeControlNodeFeature;
 import org.modelexecution.xmof.diagram.features.DisallowResizeObjectNodeFeature;
 import org.modelexecution.xmof.diagram.features.LayoutActionFeature;
 import org.modelexecution.xmof.diagram.features.LayoutActivityFeature;
 import org.modelexecution.xmof.diagram.features.LayoutExpansionRegionFeature;
 import org.modelexecution.xmof.diagram.features.MoveActionFeature;
 import org.modelexecution.xmof.diagram.features.MoveActivityFeature;
 import org.modelexecution.xmof.diagram.features.MoveExpansionRegionFeature;
 import org.modelexecution.xmof.diagram.features.RemoveActionFeature;
 import org.modelexecution.xmof.diagram.features.RemoveActivityNodeFeature;
 import org.modelexecution.xmof.diagram.features.RemoveExpansionRegionFeature;
 import org.modelexecution.xmof.diagram.features.UpdateActionFeature;
 import org.modelexecution.xmof.diagram.features.UpdateCallBehaviorActionFeature;
 import org.modelexecution.xmof.diagram.features.UpdateCallOperationActionFeature;
 
 public class XMOFFeatureProvider extends DefaultFeatureProvider {
 
 	public XMOFFeatureProvider(IDiagramTypeProvider dtp) {
 		super(dtp);
 	}
 
 	@Override
 	public IAddFeature getAddFeature(IAddContext context) {
 		Object newObject = context.getNewObject();
 		if (newObject instanceof Activity) {
 			return new AddActivityFeature(this);
 		} else if (newObject instanceof StructuredActivityNode) {
 			return new AddStructuredActivityNodeFeature(this);
 		} else if (newObject instanceof Action) {
 			return new AddActionFeature(this);
 		} else if (isControlOrObjectFlow(newObject)) {
 			return new AddFlowFeature(this);
 		} else if (newObject instanceof InitialNode) {
 			return new AddInitialNodeFeature(this);
 		} else if (newObject instanceof MergeNode
 				|| newObject instanceof DecisionNode) {
 			return new AddDecisionMergeNodeFeature(this);
 		} else if (newObject instanceof JoinNode
 				|| newObject instanceof ForkNode) {
 			return new AddJoinForkNodeFeature(this);
 		} else if (newObject instanceof ExpansionNode) {
 			return new AddExpansionNodeFeature(this);
 		}
 		return super.getAddFeature(context);
 	}
 
 	private boolean isControlOrObjectFlow(Object newObject) {
 		return newObject instanceof ControlFlow
 				|| newObject instanceof ObjectFlow;
 	}
 
 	@Override
 	public ICreateFeature[] getCreateFeatures() {
 		return new ICreateFeature[] {
 				new CreateReadIsClassifiedObjectActionFeature(this),
 				new CreateTestIdentityActionFeature(this),
 				new CreateValueSpecificationActionFeature(this),
 				new CreateAddStructuralFeatureValueActionFeature(this),
 				new CreateReadStructuralFeatureActionFeature(this),
 				new CreateInitialNodeFeature(this),
 				//new CreateActivityFeature(this),
 				new CreateMergeNodeFeature(this),
 				new CreateDecisionNodeFeature(this),
 				new CreateJoinNodeFeature(this),
 				new CreateForkNodeFeature(this),
 				new CreateReadSelfActionFeature(this),
 				new CreateCallOperationActionFeature(this),
 				new CreateCallBehaviorActionFeature(this),
 				new CreateExpansionRegionFeature(this),
 				new CreateInputExpansionNodeFeature(this),
 				new CreateOutputExpansionNodeFeature(this) };
 	}
 
 	@Override
 	public ICreateConnectionFeature[] getCreateConnectionFeatures() {
 		return new ICreateConnectionFeature[] {
 				new CreateObjectFlowFeature(this),
 				new CreateControlFlowFeature(this) };
 	}
 
 	@Override
 	public IUpdateFeature getUpdateFeature(IUpdateContext context) {
 		PictogramElement pictogramElement = context.getPictogramElement();
 		if (pictogramElement instanceof ContainerShape) {
 			Object bo = getBusinessObjectForPictogramElement(pictogramElement);
 			if (bo instanceof CallBehaviorAction) {
 				return new UpdateCallBehaviorActionFeature(this);
 			} else if(bo instanceof CallOperationAction) {
 				return new UpdateCallOperationActionFeature(this);
 			} else if (bo instanceof Action && !(bo instanceof StructuredActivityNode)) {
 				return new UpdateActionFeature(this);
 			}
 		}
 		return super.getUpdateFeature(context);
 	}
 
 	@Override
 	public ILayoutFeature getLayoutFeature(ILayoutContext context) {
 		PictogramElement pictogramElement = context.getPictogramElement();
 		Object bo = getBusinessObjectForPictogramElement(pictogramElement);
 		if (bo instanceof ExpansionRegion) {
 			return new LayoutExpansionRegionFeature(this);
 		} else if (bo instanceof Action) {
 			return new LayoutActionFeature(this);
 		} else if (bo instanceof Activity) {
 			return new LayoutActivityFeature(this);
 		}
 		return super.getLayoutFeature(context);
 	}
 
 	@Override
 	public IMoveShapeFeature getMoveShapeFeature(IMoveShapeContext context) {
 		Object bo = getBusinessObjectForPictogramElement(context.getShape());
 		if (bo instanceof Pin) {
 			return new DisallowMovePinFeature(this);
 		} else if (bo instanceof ExpansionRegion) {
 			return new MoveExpansionRegionFeature(this);
 		} else if (bo instanceof Action) {
 			return new MoveActionFeature(this);
 		} else if (bo instanceof ExpansionNode) {
 			return new DisallowMoveExpansionNodeFeature(this);
 		} else if (bo instanceof Activity) {
 			return new MoveActivityFeature(this);
 		}
 		return super.getMoveShapeFeature(context);
 	}
 
 	@Override
 	public IDeleteFeature getDeleteFeature(IDeleteContext context) {
 		// Delete means deleting the element from the model and the diagram
 		Object bo = getBusinessObjectForPictogramElement(context
 				.getPictogramElement());
 		if (bo instanceof Pin) {
 			return new DisallowDeletePinFeature(this);
 		} else if (bo instanceof ExpansionRegion) {
 			return new DeleteExpansionRegionFeature(this);
 		} else if (bo instanceof Action) {
 			return new DeleteActionFeature(this);
 		} else if (bo instanceof ActivityNode) {
 			return new DeleteActivityNodeFeature(this);
 		} else if (bo instanceof Activity) {
 			return new DisallowDeleteActivityFeature(this);
 		} 
 		return super.getDeleteFeature(context);
 	}
 
 	@Override
 	public IRemoveFeature getRemoveFeature(IRemoveContext context) {
 		// Remove means removing the element only from the diagram, not the
 		// model
 		Object bo = getBusinessObjectForPictogramElement(context
 				.getPictogramElement());
 		if (bo instanceof Pin) {
 			return new DisallowRemovePinFeature(this);
 		} else if (bo instanceof ExpansionRegion) {
 			return new RemoveExpansionRegionFeature(this);
 		} else if (bo instanceof ActivityParameterNode) {
 			return new DisallowRemoveActivityParameterNodeFeature(this);
 		} else if (bo instanceof Action) {
 			return new RemoveActionFeature(this);
 		} else if (bo instanceof ActivityNode) {
 			return new RemoveActivityNodeFeature(this);
 		} else if (bo instanceof Activity) {
 			return new DisallowRemoveActivityFeature(this);
 		} 
 		return super.getRemoveFeature(context);
 	}
 
 	@Override
 	public IResizeShapeFeature getResizeShapeFeature(IResizeShapeContext context) {
 		Object bo = getBusinessObjectForPictogramElement(context
 				.getPictogramElement());
 		if (bo instanceof ObjectNode) {
 			return new DisallowResizeObjectNodeFeature(this);
 		} else if (bo instanceof ControlNode) {
 			return new DisallowResizeControlNodeFeature(this);
 		}
 		return super.getResizeShapeFeature(context);
 	}
 	
 	@Override
 	public IReconnectionFeature getReconnectionFeature(
 			IReconnectionContext context) {
 		Object bo = context.getConnection().getLink().getBusinessObjects().get(0);
 		if(bo instanceof ActivityEdge) {
 			return new DisallowReconnectActivityEdgeFeature(this);
 		}
 		return super.getReconnectionFeature(context);
 	}
 }
