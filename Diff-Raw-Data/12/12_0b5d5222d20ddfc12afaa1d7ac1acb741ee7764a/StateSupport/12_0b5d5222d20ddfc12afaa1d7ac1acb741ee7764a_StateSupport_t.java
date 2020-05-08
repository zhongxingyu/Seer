 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.ui.behavior.support;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.etrice.core.naming.RoomNameProvider;
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.ActorContainerRef;
 import org.eclipse.etrice.core.room.ActorRef;
 import org.eclipse.etrice.core.room.BaseState;
 import org.eclipse.etrice.core.room.RefinedState;
 import org.eclipse.etrice.core.room.RoomFactory;
 import org.eclipse.etrice.core.room.State;
 import org.eclipse.etrice.core.room.StateGraph;
 import org.eclipse.etrice.core.room.SubSystemRef;
 import org.eclipse.etrice.core.room.TrPoint;
 import org.eclipse.etrice.core.room.util.RoomHelpers;
 import org.eclipse.etrice.ui.behavior.ImageProvider;
 import org.eclipse.etrice.ui.behavior.dialogs.StatePropertyDialog;
 import org.eclipse.etrice.ui.common.support.CommonSupportUtil;
 import org.eclipse.graphiti.datatypes.IDimension;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.ICreateConnectionFeature;
 import org.eclipse.graphiti.features.ICreateFeature;
 import org.eclipse.graphiti.features.IDeleteFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.ILayoutFeature;
 import org.eclipse.graphiti.features.IMoveShapeFeature;
 import org.eclipse.graphiti.features.IReason;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.IResizeShapeFeature;
 import org.eclipse.graphiti.features.IUpdateFeature;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.context.IDeleteContext;
 import org.eclipse.graphiti.features.context.IDoubleClickContext;
 import org.eclipse.graphiti.features.context.ILayoutContext;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.features.context.IPictogramElementContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.context.impl.AddContext;
 import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
 import org.eclipse.graphiti.features.context.impl.RemoveContext;
 import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
 import org.eclipse.graphiti.features.custom.ICustomFeature;
 import org.eclipse.graphiti.features.impl.AbstractAddFeature;
 import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
 import org.eclipse.graphiti.features.impl.AbstractLayoutFeature;
 import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
 import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
 import org.eclipse.graphiti.features.impl.DefaultRemoveFeature;
 import org.eclipse.graphiti.features.impl.DefaultResizeShapeFeature;
 import org.eclipse.graphiti.features.impl.Reason;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Polygon;
 import org.eclipse.graphiti.mm.algorithms.Rectangle;
 import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.algorithms.styles.Color;
 import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
 import org.eclipse.graphiti.mm.pictograms.ChopboxAnchor;
 import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeCreateService;
 import org.eclipse.graphiti.tb.ContextButtonEntry;
 import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
 import org.eclipse.graphiti.tb.IContextButtonPadData;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;
 import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
 import org.eclipse.graphiti.ui.services.GraphitiUi;
 import org.eclipse.graphiti.util.ColorConstant;
 import org.eclipse.graphiti.util.IColorConstant;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 
 public class StateSupport {
 	
 	private static final int LINE_WIDTH = 1;
 	public static final int DEFAULT_SIZE_X = 60;
 	public static final int DEFAULT_SIZE_Y = 30;
 	public static final int MIN_SIZE_X = 60;
 	public static final int MIN_SIZE_Y = 30;
 	public static final int MARGIN = 20;
 	public static final int CORNER_WIDTH = 20;
 	public static final int HINT_CORNER_WIDTH = 5;
 	private static final int TEXT_MARGIN = 10;
 	
 	public static final IColorConstant LINE_COLOR = new ColorConstant(0, 0, 0);
 	public static final IColorConstant INHERITED_COLOR = new ColorConstant(100, 100, 100);
 	private static final IColorConstant BACKGROUND = new ColorConstant(200, 200, 200);
 
 	private static class FeatureProvider extends DefaultFeatureProvider {
 
 		private class CreateFeature extends AbstractCreateFeature {
 	
 			private boolean doneChanges = false;
 			
 			public CreateFeature(IFeatureProvider fp) {
 				super(fp, "State", "create State");
 			}
 			
 			@Override
 			public String getCreateImageId() {
 				return ImageProvider.IMG_STATE;
 			}
 	
 			@Override
 			public boolean canCreate(ICreateContext context) {
 				if (context.getTargetContainer().getLink()!=null)
 					if (context.getTargetContainer().getLink().getBusinessObjects().size()==1) {
 						EObject obj = context.getTargetContainer().getLink().getBusinessObjects().get(0);
 						if (obj instanceof StateGraph) {
 							return true;
 						}
 					}
 				return false;
 			}
 	
 			@Override
 			public Object[] create(ICreateContext context) {
 		        
 		        ContainerShape targetContainer = context.getTargetContainer();
 		        ActorClass ac = SupportUtil.getActorClass(getDiagram());
 				StateGraph sg = (StateGraph) targetContainer.getLink().getBusinessObjects().get(0);
 		        
 				boolean inherited = SupportUtil.isInherited(getDiagram(), sg);
 				if (inherited) {
 					sg = SupportUtil.insertRefinedState(sg, ac, targetContainer, getFeatureProvider());
 				}
 				
 				// create new State and add it
 				BaseState s = RoomFactory.eINSTANCE.createBaseState();
 				s.setName(RoomNameProvider.getUniqueStateName(sg));
 				sg.getStates().add(s);
 		        
 	        	Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 				StatePropertyDialog dlg = new StatePropertyDialog(shell, s);
 				if (dlg.open()!=Window.OK) {
 					if (inherited) {
 						SupportUtil.undoInsertRefinedState(sg, ac, targetContainer, getFeatureProvider());
 					}
 					else {
 						sg.getStates().remove(s);
 					}
 					return EMPTY;
 				}
 		        
 		        addGraphicalRepresentation(context, s);
 		        doneChanges = true;
 		        
 		        // return newly created business object(s)
 		        return new Object[] { s };
 			}
 			
 			@Override
 			public boolean hasDoneChanges() {
 				return doneChanges;
 			}
 		}
 	
 		private class AddFeature extends AbstractAddFeature {
 	
 
 			public AddFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 	
 			@Override
 			public boolean canAdd(IAddContext context) {
 				if (context.getNewObject() instanceof State) {
 					if (context.getTargetContainer().getLink().getBusinessObjects().size()==1) {
 						EObject obj = context.getTargetContainer().getLink().getBusinessObjects().get(0);
 						if (obj instanceof StateGraph) {
 							return true;
 						}
 					}
 				}
 				return false;
 			}
 	
 			@Override
 			public PictogramElement add(IAddContext context) {
 				State s = (State) context.getNewObject();
 				ContainerShape acShape = context.getTargetContainer();
 	
 				// CONTAINER SHAPE WITH RECTANGLE
 				IPeCreateService peCreateService = Graphiti.getPeCreateService();
 				ContainerShape containerShape =
 					peCreateService.createContainerShape(acShape, true);
 				
 				Graphiti.getPeService().setPropertyValue(containerShape, Constants.TYPE_KEY, Constants.STATE_TYPE);
 	
 				int width = context.getWidth();
 				int height = context.getHeight();
 				if (width<=0) {
 					width = DEFAULT_SIZE_X;
 					IDimension sz = GraphitiUi.getUiLayoutService().calculateTextSize(s.getName(), getDiagram().getFonts().get(0));
 					if (width<sz.getWidth()+TEXT_MARGIN)
 						width = sz.getWidth()+TEXT_MARGIN;
 				}
 				if (height<=0)
 					height = DEFAULT_SIZE_Y;
 			
 				boolean inherited = SupportUtil.isInherited(getDiagram(), s);
 				Color lineColor = manageColor(inherited?INHERITED_COLOR:LINE_COLOR);
 				IGaService gaService = Graphiti.getGaService();
 				{
 					final Rectangle invisibleRectangle = gaService.createInvisibleRectangle(containerShape);
 					gaService.setLocationAndSize(invisibleRectangle,
 							context.getX()-(width/2+MARGIN), context.getY()-(height/2+MARGIN), width + 2*MARGIN, height + 2*MARGIN);
 
 					RoundedRectangle rect = createFigure(s, invisibleRectangle, lineColor, manageColor(BACKGROUND));
 					
 					// anchor for direct transitions to this state
 					ChopboxAnchor anchor = peCreateService.createChopboxAnchor(containerShape);
 					anchor.setReferencedGraphicsAlgorithm(rect);
 					
 					// create link and wire it
 					link(containerShape, s);
 				}
 				
 				{
 					Shape labelShape = peCreateService.createShape(containerShape, false);
 					Text label = gaService.createDefaultText(getDiagram(), labelShape, s.getName());
 					label.setForeground(lineColor);
 					label.setBackground(lineColor);
 					label.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
 					label.setVerticalAlignment(Orientation.ALIGNMENT_CENTER);
 					gaService.setLocationAndSize(label, MARGIN, MARGIN, width, height);
 				}
 	
 				// call the layout feature
 				layoutPictogramElement(containerShape);
 	
 				return containerShape;
 			}
 	
 		}
 		
 		private class LayoutFeature extends AbstractLayoutFeature {
 	
 			public LayoutFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 	
 			@Override
 			public boolean canLayout(ILayoutContext context) {
 				// return true, if pictogram element is linked to a State
 				PictogramElement pe = context.getPictogramElement();
 				if (!(pe instanceof ContainerShape))
 					return false;
 	
 				Object bo = getBusinessObjectForPictogramElement(pe);
 				return bo instanceof State;
 			}
 	
 			@Override
 			public boolean layout(ILayoutContext context) {
 				boolean anythingChanged = false;
 				ContainerShape containerShape = (ContainerShape) context
 						.getPictogramElement();
 	
 				// our invisible rectangle
 				GraphicsAlgorithm containerGa = containerShape.getGraphicsAlgorithm();
 				
 				int w = containerGa.getWidth();
 				int h = containerGa.getHeight();
 	
 				if (containerGa.getGraphicsAlgorithmChildren().size()>=1) {
 					// the visible border
 					GraphicsAlgorithm borderGA = containerGa.getGraphicsAlgorithmChildren().get(0);
 					
 					int nw = w-2*MARGIN;
 					int nh = h-2*MARGIN;
 					
 					borderGA.setWidth(nw);
 					borderGA.setHeight(nh);
 					
 					Object bo = getBusinessObjectForPictogramElement(containerShape);
 					if (bo instanceof State) {
 						State s = (State) bo;
 						while (!borderGA.getGraphicsAlgorithmChildren().isEmpty()) {
 							EcoreUtil.delete(borderGA.getGraphicsAlgorithmChildren().get(0), true);
 						}
 						Color lineColor = manageColor(SupportUtil.isInherited(getDiagram(), s)?INHERITED_COLOR:LINE_COLOR);
 						addHints(s, (RoundedRectangle) borderGA, lineColor);
 					}
 
 					if (!containerShape.getChildren().isEmpty()) {
 						GraphicsAlgorithm childGA = containerShape.getChildren().get(0).getGraphicsAlgorithm();
 						assert(childGA instanceof Text): "label expected";
 						childGA.setWidth(nw);
 						childGA.setHeight(nh);
 					}
 					
 					anythingChanged = true;
 				}
 				
 				return anythingChanged;
 			}
 		}
 
 		private static class PropertyFeature extends AbstractCustomFeature {
 
 			private boolean doneChanges = false;
 			
 			public PropertyFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			public String getName() {
 				return "Edit State...";
 			}
 			
 			@Override
 			public String getDescription() {
 				return "Edit State Properties";
 			}
 			
 			@Override
 			public boolean canExecute(ICustomContext context) {
 				PictogramElement[] pes = context.getPictogramElements();
 				if (pes != null && pes.length == 1) {
 					Object bo = getBusinessObjectForPictogramElement(pes[0]);
 					if (bo instanceof State) {
 						return true;
 					}
 				}
 				return false;
 			}
 
 			@Override
 			public void execute(ICustomContext context) {
 				State s = (State) getBusinessObjectForPictogramElement(context.getPictogramElements()[0]);
 
 				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 				StatePropertyDialog dlg = new StatePropertyDialog(shell, s);
 				if (dlg.open()!=Window.OK)
 					return;
 
 				doneChanges = true;
 				updateFigure(s, context);
				
		        ActorClass ac = SupportUtil.getActorClass(getDiagram());
				if (RoomHelpers.hasSubStructure(s, ac)) {
					// update the path text in the sub graph
					ContainerShape subShape = ContextSwitcher.getContext(getDiagram(), s.getSubgraph());
					if (subShape!=null && !subShape.getChildren().isEmpty()) {
						Shape labelShape = subShape.getChildren().get(0);
						GraphicsAlgorithm ga = labelShape.getGraphicsAlgorithm();
						if (ga instanceof Text)
							((Text)ga).setValue(RoomNameProvider.getStateGraphLabel(s.getSubgraph()));
					}
				}
 			}
 
 			private void updateFigure(State s, ICustomContext context) {
 				ContainerShape container = (ContainerShape)context.getPictogramElements()[0];
 				
 				// we clear the figure and rebuild it
 				GraphicsAlgorithm invisibleRect = context.getPictogramElements()[0].getGraphicsAlgorithm();
 				while (!invisibleRect.getGraphicsAlgorithmChildren().isEmpty()) {
 					EcoreUtil.delete(invisibleRect.getGraphicsAlgorithmChildren().get(0), true);
 				}
 				
 				boolean inherited = SupportUtil.isInherited(getDiagram(), s);
 				Color lineColor = manageColor(inherited?INHERITED_COLOR:LINE_COLOR);
 				createFigure(s, invisibleRect, lineColor, manageColor(BACKGROUND));
 				
 				GraphicsAlgorithm ga = container.getChildren().get(0).getGraphicsAlgorithm();
 				if (ga instanceof Text) {
 					((Text)ga).setValue(s.getName());
 				}
 
 			}
 			
 			@Override
 			public boolean hasDoneChanges() {
 				return doneChanges;
 			}
 		}
 		
 		private static class GoDownFeature extends AbstractCustomFeature implements
 				ICustomFeature {
 
 			public GoDownFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			public String getName() {
 				return "Switch to SubGraph";
 			}
 			
 			@Override
 			public String getDescription() {
 				return "Switch Context to SubGraph";
 			}
 
 			@Override
 			public boolean canExecute(ICustomContext context) {
 				PictogramElement pe = context.getPictogramElements()[0];
 				Object bo = getBusinessObjectForPictogramElement(pe);
 				if (bo instanceof State) {
 					State targetting = SupportUtil.getTargettingState((State) bo, getDiagram());
 					if (targetting.getSubgraph()!=null)
 						return true;
 				}
 				return false;
 			}
 			
 			@Override
 			public void execute(ICustomContext context) {
 				getDiagramEditor().selectPictogramElements(new PictogramElement[] {});
 				ContainerShape container = (ContainerShape)context.getPictogramElements()[0];
 				Object bo = getBusinessObjectForPictogramElement(container);
 				if (bo instanceof State) {
 					State targetting = SupportUtil.getTargettingState((State) bo, getDiagram());
 					if (targetting.getSubgraph()!=null) {
 						ContextSwitcher.switchTo(getDiagram(), targetting.getSubgraph());
 					}
 				}
 			}
 			
 			@Override
 			public boolean hasDoneChanges() {
 				return false;
 			}
 		}
 		
 		private static class CreateSubGraphFeature extends AbstractCustomFeature implements
 				ICustomFeature {
 
 			public CreateSubGraphFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			public String getName() {
 				return "Create SubGraph";
 			}
 			
 			@Override
 			public String getDescription() {
 				return "Create and Switch to New SubGraph";
 			}
 
 			@Override
 			public boolean canExecute(ICustomContext context) {
 				PictogramElement pe = context.getPictogramElements()[0];
 				Object bo = getBusinessObjectForPictogramElement(pe);
 				if (bo instanceof State) {
 					if (((State) bo).getSubgraph()==null)
 						return true;
 				}
 				return false;
 			}
 			
 			@Override
 			public void execute(ICustomContext context) {
 				getDiagramEditor().selectPictogramElements(new PictogramElement[] {});
 
 				ContainerShape container = (ContainerShape)context.getPictogramElements()[0];
 				Object bo = getBusinessObjectForPictogramElement(container);
 				if (bo instanceof State) {
 					State s = (State) bo;
 					
 					boolean inherited = SupportUtil.isInherited(getDiagram(), s);
 					if (inherited) {
 						ActorClass ac = SupportUtil.getActorClass(getDiagram());
 						RefinedState rs = null;
 						
 						// do we already have a RefinedState pointing to s?
 						for (State st : ac.getStateMachine().getStates()) {
 							if (st instanceof RefinedState)
 								if (((RefinedState) st).getBase()==s) {
 									rs = (RefinedState) st;
 									break;
 								}
 						}
 						
 						// if not so create one
 						if (rs==null) {
 							rs = RoomFactory.eINSTANCE.createRefinedState();
 							rs.setBase((BaseState) s);
 							ac.getStateMachine().getStates().add(rs);
 						}
 						s = rs;
 					}
 
 					s.setSubgraph(RoomFactory.eINSTANCE.createStateGraph());
 
 					AddContext addContext = new AddContext();
 					addContext.setNewObject(s.getSubgraph());
 					addContext.setTargetContainer(getDiagram());
 					addContext.setX(StateGraphSupport.MARGIN);
 					addContext.setY(StateGraphSupport.MARGIN);
 					PictogramElement subGraphShape = getFeatureProvider().addIfPossible(addContext);
 					if (subGraphShape!=null) {
 						RoundedRectangle borderRect = (RoundedRectangle) container.getGraphicsAlgorithm().getGraphicsAlgorithmChildren().get(0);
 						updateHints(s, borderRect);
 					}
 					
 					ContextSwitcher.switchTo(getDiagram(), s.getSubgraph());
 				}
 			}
 		}
 		
 		private class UpdateFeature extends AbstractUpdateFeature {
 
 			public UpdateFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			public boolean canUpdate(IUpdateContext context) {
 				Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 				if (bo instanceof EObject && ((EObject)bo).eIsProxy())
 					return true;
 				
 				return bo instanceof State;
 			}
 
 			@Override
 			public IReason updateNeeded(IUpdateContext context) {
 				ContainerShape containerShape = (ContainerShape)context.getPictogramElement();
 				Object bo = getBusinessObjectForPictogramElement(containerShape);
 				if (bo instanceof EObject && ((EObject)bo).eIsProxy()) {
 					return Reason.createTrueReason("State deleted from model");
 				}
 				State s = (State) bo;
 				ActorClass mainAc = SupportUtil.getActorClass(getDiagram());
 				
 				// check if ref still owned/inherited anymore
 				{
 					ActorClass ac = RoomHelpers.getActorClass(s);
 					ActorClass tmp = mainAc;
 					boolean found = false;
 					do {
 						if (tmp==ac)
 							found = true;
 						tmp = tmp.getBase();
 					}
 					while (!found && tmp!=null);
 					
 					if (!found)
 						return Reason.createTrueReason("Ref not inherited anymore");
 				}
 				
 				// check sub structure hint
 				{
 					boolean hasSubStruct = RoomHelpers.hasSubStructure(s, mainAc);
 					GraphicsAlgorithm invisibleRect = containerShape.getGraphicsAlgorithm();
 					if (!invisibleRect.getGraphicsAlgorithmChildren().isEmpty()) {
 						GraphicsAlgorithm borderRect = invisibleRect.getGraphicsAlgorithmChildren().get(0);
 						if (!borderRect.getGraphicsAlgorithmChildren().isEmpty()) {
 							GraphicsAlgorithm hint = borderRect.getGraphicsAlgorithmChildren().get(0);
 							if (hasSubStruct && !hint.getLineVisible())
 								return Reason.createTrueReason("state has sub structure now");
 							if (!hasSubStruct && hint.getLineVisible())
 								return Reason.createTrueReason("state has no sub structure anymore");
 						}
 					}
 				}
 				
 				// check class name
 				int last = containerShape.getChildren().size()-1;
 				if (last>=0) {
 					GraphicsAlgorithm ga = containerShape.getChildren().get(last).getGraphicsAlgorithm();
 					if (ga instanceof Text) {
 						if (bo instanceof ActorContainerRef) {
 							String label = getLabel((ActorContainerRef) bo);
 							if (!((Text)ga).getValue().equals(label))
 								return Reason.createTrueReason("Class name is out of date");
 						}
 					}
 				}
 				
 				// TODOHRR: check interface ports and spps added to model not present in diagram
 				
 				return Reason.createFalseReason();
 			}
 
 			@Override
 			public boolean update(IUpdateContext context) {
 				ContainerShape containerShape = (ContainerShape)context.getPictogramElement();
 				Object bo = getBusinessObjectForPictogramElement(containerShape);
 				if (bo instanceof EObject && ((EObject)bo).eIsProxy()) {
 					IRemoveContext rc = new RemoveContext(containerShape);
 					IFeatureProvider featureProvider = getFeatureProvider();
 					IRemoveFeature removeFeature = featureProvider.getRemoveFeature(rc);
 					if (removeFeature != null) {
 						removeFeature.remove(rc);
 					}
 					EcoreUtil.delete((EObject) bo);
 					return true;
 				}
 				
 				State s = (State) bo;
 				{
 					GraphicsAlgorithm invisibleRect = containerShape.getGraphicsAlgorithm();
 					if (!invisibleRect.getGraphicsAlgorithmChildren().isEmpty()) {
 						GraphicsAlgorithm borderRect = invisibleRect.getGraphicsAlgorithmChildren().get(0);
 						updateHints(s, (RoundedRectangle) borderRect);
 					}
 				}
 				
 				int last = containerShape.getChildren().size()-1;
 				if (last>=0) {
 					GraphicsAlgorithm ga = containerShape.getChildren().get(last).getGraphicsAlgorithm();
 					if (ga instanceof Text) {
 						((Text)ga).setValue(s.getName());
 					}
 				}
 				
 				return true;
 			}
 		}
 		
 		private class MoveShapeFeature extends DefaultMoveShapeFeature {
 	
 			public MoveShapeFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 	
 			@Override
 			public boolean canMoveShape(IMoveShapeContext context) {
 				boolean canMove = super.canMoveShape(context);
 	
 				if (canMove) {
 					Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 					
 					if (bo instanceof State) {
 						State s = (State) bo;
 						//ContainerShape sgShape = context.getTargetContainer();
 						
 						// TODOHRR: also check coordinates (no overlap with state graph boundaries)
 						
 						return !SupportUtil.isInherited(getDiagram(), s);
 					}
 				}
 				
 				return canMove;
 			}
 		}
 		
 		private class ResizeFeature extends DefaultResizeShapeFeature {
 
 			public ResizeFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 			
 			@Override
 			public boolean canResizeShape(IResizeShapeContext context) {
 				if (!super.canResizeShape(context))
 					return false;
 				
 				int width = context.getWidth()-MARGIN;
 				int height = context.getHeight()-MARGIN;
 				if (width>0 && height>0)
 					if (width < MIN_SIZE_X+MARGIN || height < MIN_SIZE_Y+MARGIN)
 						return false;
 				
 				return true;
 			}
 			
 			@Override
 			public void resizeShape(IResizeShapeContext context) {
 				Shape shape = context.getShape();
 				
 				if (shape.getGraphicsAlgorithm()!=null) {
 					GraphicsAlgorithm containerGa = shape.getGraphicsAlgorithm();
 					if (containerGa.getGraphicsAlgorithmChildren().size()==1) {
 						// scale interface item coordinates
 						// we refer to the visible rectangle which defines the border of our structure class
 						// since the margin is not scaled
 						GraphicsAlgorithm ga = containerGa.getGraphicsAlgorithmChildren().get(0);
 						double sx = (context.getWidth()-2*MARGIN)/(double)ga.getWidth();
 						double sy = (context.getHeight()-2*MARGIN)/(double)ga.getHeight();
 						
 						for (Shape childShape : ((ContainerShape)context.getShape()).getChildren()) {
 							Object childBo = getBusinessObjectForPictogramElement(childShape);
 							if (childBo instanceof TrPoint) {
 								ga = childShape.getGraphicsAlgorithm();
 								ga.setX((int) (ga.getX()*sx));
 								ga.setY((int) (ga.getY()*sy));
 							}
 							
 						}
 					}
 				}
 				super.resizeShape(context);
 			}
 		}
 		
 		protected static class RemoveFeature extends DefaultRemoveFeature {
 
 			public RemoveFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			public boolean canRemove(IRemoveContext context) {
 				return false;
 			}
 		}
 		
 		protected static class DeleteFeature extends DefaultDeleteFeature {
 
 			public DeleteFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 			
 			@Override
 			public boolean canDelete(IDeleteContext context) {
 				Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 				if (bo instanceof State) {
 					State state = (State) bo;
 					return !SupportUtil.isInherited(getDiagram(), state);
 				}
 				return false;
 			}
 			
 			/* (non-Javadoc)
 			 * @see org.eclipse.graphiti.ui.features.DefaultDeleteFeature#preDelete(org.eclipse.graphiti.features.context.IDeleteContext)
 			 */
 			@Override
 			public void preDelete(IDeleteContext context) {
 				super.preDelete(context);
 				
 				if (!(context.getPictogramElement() instanceof ContainerShape))
 					return;
 
 				State s = (State) getBusinessObjectForPictogramElement(context.getPictogramElement());
 				IFeatureProvider fp = getFeatureProvider();
 				Diagram diagram = getDiagram();
 				ActorClass ac = RoomHelpers.getActorClass(s);
 				SupportUtil.deleteSubStructureRecursive(s, ac, diagram, fp);
 				
 				ContainerShape container = (ContainerShape) context.getPictogramElement();
 				CommonSupportUtil.deleteConnectionsRecursive(container, fp);
 			}
 		}
 		
 		private IFeatureProvider fp;
 	
 		public FeatureProvider(IDiagramTypeProvider dtp, IFeatureProvider fp) {
 			super(dtp);
 			this.fp = fp;
 		}
 	
 		public static RoundedRectangle createFigure(State s,
 				GraphicsAlgorithm invisibleRect, Color darkColor,
 				Color brightColor) {
 			
 			IGaService gaService = Graphiti.getGaService();
 
 			RoundedRectangle rect = gaService.createRoundedRectangle(invisibleRect, CORNER_WIDTH, CORNER_WIDTH);
 			rect.setForeground(darkColor);
 			rect.setBackground(brightColor);
 			rect.setLineWidth(LINE_WIDTH);
 			gaService.setLocationAndSize(rect, MARGIN, MARGIN, invisibleRect.getWidth()-2*MARGIN, invisibleRect.getHeight()-2*MARGIN);
 
 			addHints(s, rect, darkColor);
 			
 			return rect;
 		}
 
 		@Override
 		public ICreateFeature[] getCreateFeatures() {
 			return new ICreateFeature[] { new CreateFeature(fp) };
 		}
 	
 		@Override
 		public IAddFeature getAddFeature(IAddContext context) {
 			return new AddFeature(fp);
 		}
 		
 		@Override
 		public ILayoutFeature getLayoutFeature(ILayoutContext context) {
 			return new LayoutFeature(fp);
 		}
 		
 		@Override
 		public IUpdateFeature getUpdateFeature(IUpdateContext context) {
 			return new UpdateFeature(fp);
 		}
 		
 		@Override
 		public IMoveShapeFeature getMoveShapeFeature(IMoveShapeContext context) {
 			return new MoveShapeFeature(fp);
 		}
 		
 		@Override
 		public IResizeShapeFeature getResizeShapeFeature(
 				IResizeShapeContext context) {
 			return new ResizeFeature(fp);
 		}
 
 		@Override
 		public IRemoveFeature getRemoveFeature(IRemoveContext context) {
 			return new RemoveFeature(fp);
 		}
 
 		@Override
 		public IDeleteFeature getDeleteFeature(IDeleteContext context) {
 			return new DeleteFeature(fp);
 		}
 		
 		@Override
 		public ICustomFeature[] getCustomFeatures(ICustomContext context) {
 			return new ICustomFeature[] { new PropertyFeature(fp), new GoDownFeature(fp), new CreateSubGraphFeature(fp) };
 		}
 
 		protected static String getLabel(ActorContainerRef acr) {
 			String className = "<unknown>";
 			if (acr instanceof ActorRef) {
 				if (((ActorRef)acr).getType()!=null)
 					className = ((ActorRef)acr).getType().getName();
 			}
 			else if (acr instanceof SubSystemRef) {
 				if (((SubSystemRef)acr).getType()!=null)
 					className = ((SubSystemRef)acr).getType().getName();
 			}
 			return acr.getName()+"\n("+className+")";
 		}
 	}
 	
 	private class BehaviorProvider extends DefaultToolBehaviorProvider {
 
 		public BehaviorProvider(IDiagramTypeProvider dtp) {
 			super(dtp);
 		}
 		
 		@Override
 		public GraphicsAlgorithm[] getClickArea(PictogramElement pe) {
             GraphicsAlgorithm invisible = pe.getGraphicsAlgorithm();
             GraphicsAlgorithm rectangle =
                             invisible.getGraphicsAlgorithmChildren().get(0);
             return new GraphicsAlgorithm[] { rectangle };
 		}
 		
 		@Override
 		public GraphicsAlgorithm getSelectionBorder(PictogramElement pe) {
             GraphicsAlgorithm invisible = pe.getGraphicsAlgorithm();
 
             GraphicsAlgorithm rectangle =
                 invisible.getGraphicsAlgorithmChildren().get(0);
             return rectangle;
 		}
 		
 		@Override
 		public ICustomFeature getDoubleClickFeature(IDoubleClickContext context) {
 			return new FeatureProvider.GoDownFeature(getDiagramTypeProvider().getFeatureProvider());
 		}
 		
 		@Override
 		public String getToolTip(GraphicsAlgorithm ga) {
 			// if this is called we know there is a business object!=null
 			PictogramElement pe = ga.getPictogramElement();
 			if (pe instanceof ConnectionDecorator)
 				pe = (PictogramElement) pe.eContainer();
 			
 			EObject bo = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe);
 			if (bo instanceof State) {
 				String label = "";
 				State s = (State) bo;
 				if (RoomHelpers.hasDetailCode(s.getEntryCode()))
 					label = "entry:\n"+RoomHelpers.getDetailCode(s.getEntryCode());
 				if (RoomHelpers.hasDetailCode(s.getExitCode())) {
 					if (label.length()>0)
 						label += "\n";
 					label += "exit:\n"+RoomHelpers.getDetailCode(s.getExitCode());
 				}
 				if (label.length()>0)
 					return label;
 			}
 			
 			return super.getToolTip(ga);
 		}
 		
 		@Override
 		public IContextButtonPadData getContextButtonPad(
 				IPictogramElementContext context) {
 			
 			IContextButtonPadData data = super.getContextButtonPad(context);
 			PictogramElement pe = context.getPictogramElement();
 
 			CreateConnectionContext ccc = new CreateConnectionContext();
 			ccc.setSourcePictogramElement(pe);
 			Anchor anchor = null;
 			if (pe instanceof AnchorContainer) {
 				// our transition point has four fixed point anchor - we choose the first one
 				anchor = ((ContainerShape)pe).getAnchors().get(0);
 			}
 			ccc.setSourceAnchor(anchor);
 			
 			ContextButtonEntry button = new ContextButtonEntry(null, context);
 			button.setText("Create Transition");
 			button.setIconId(ImageProvider.IMG_TRANSITION);
 			ICreateConnectionFeature[] features = getFeatureProvider().getCreateConnectionFeatures();
 			for (ICreateConnectionFeature feature : features) {
 				if (feature.isAvailable(ccc) && feature.canStartConnection(ccc))
 					button.addDragAndDropFeature(feature);
 			}
 
 			if (button.getDragAndDropFeatures().size() > 0) {
 				data.getDomainSpecificContextButtons().add(button);
 			}
 
 			return data;
 		}
 	}
 	
 	private FeatureProvider pfp;
 	private BehaviorProvider tbp;
 	
 	public StateSupport(IDiagramTypeProvider dtp, IFeatureProvider fp) {
 		pfp = new FeatureProvider(dtp,fp);
 		tbp = new BehaviorProvider(dtp);
 	}
 	
 	public IFeatureProvider getFeatureProvider() {
 		return pfp;
 	}
 	
 	public IToolBehaviorProvider getToolBehaviorProvider() {
 		return tbp;
 	}
 	
 	private static void addHints(State s,
 			RoundedRectangle border, Color lineColor) {
 		
 		// sub structure
 		{
 			int x = border.getWidth()-25;
 			int y = 3;
 			IGaService gaService = Graphiti.getGaService();
 			RoundedRectangle hint = gaService.createRoundedRectangle(border, HINT_CORNER_WIDTH, HINT_CORNER_WIDTH);
 			hint.setForeground(lineColor);
 			hint.setFilled(false);
 			hint.setLineWidth(LINE_WIDTH);
 			gaService.setLocationAndSize(hint, x, y, 15, 8);
 			
 			ActorClass ac = SupportUtil.getActorClass(SupportUtil.getDiagram(border));
 			if (!RoomHelpers.hasSubStructure(s, ac)) {
 				hint.setLineVisible(false);
 			}
 		}
 		
 		// entry and exit code
 		{
 			int x = border.getWidth()/2;
 			int y = border.getHeight()-6;
 			IGaService gaService = Graphiti.getGaService();
 			int xy1[] = new int[] { -1, -3, -1, 3, -8,  3};
 			Polygon entryHint = gaService.createPolygon(border, xy1);
 			entryHint.setForeground(lineColor);
 			entryHint.setFilled(false);
 			entryHint.setLineWidth(LINE_WIDTH);
 			gaService.setLocation(entryHint, x, y);
 			int xy2[] = new int[] {  1, -3,  1, 3,  8,  3};
 			Polygon exitHint = gaService.createPolygon(border, xy2);
 			exitHint.setForeground(lineColor);
 			exitHint.setFilled(false);
 			exitHint.setLineWidth(LINE_WIDTH);
 			gaService.setLocation(exitHint, x, y);
 			
 			if (!RoomHelpers.hasDetailCode(s.getEntryCode())) {
 				entryHint.setLineVisible(false);
 			}
 			
 			if (!RoomHelpers.hasDetailCode(s.getExitCode())) {
 				exitHint.setLineVisible(false);
 			}
 		}
 	}
 	
 	protected static void updateHints(State s, GraphicsAlgorithm border) {
 		ActorClass ac = SupportUtil.getActorClass(SupportUtil.getDiagram(border));
 		
 		// sub structure
 		GraphicsAlgorithm hint = border.getGraphicsAlgorithmChildren().get(0);
 		hint.setLineVisible(RoomHelpers.hasSubStructure(s, ac));
 		
 		// entry and exit code
 		hint = border.getGraphicsAlgorithmChildren().get(1);
 		hint.setLineVisible(RoomHelpers.hasDetailCode(s.getEntryCode()));
 		hint = border.getGraphicsAlgorithmChildren().get(2);
 		hint.setLineVisible(RoomHelpers.hasDetailCode(s.getExitCode()));
 	}
 }
