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
 import org.eclipse.etrice.core.room.BaseState;
 import org.eclipse.etrice.core.room.ChoicePoint;
 import org.eclipse.etrice.core.room.ChoicepointTerminal;
 import org.eclipse.etrice.core.room.ContinuationTransition;
 import org.eclipse.etrice.core.room.EntryPoint;
 import org.eclipse.etrice.core.room.InitialTransition;
 import org.eclipse.etrice.core.room.NonInitialTransition;
 import org.eclipse.etrice.core.room.RefinedState;
 import org.eclipse.etrice.core.room.RoomFactory;
 import org.eclipse.etrice.core.room.State;
 import org.eclipse.etrice.core.room.StateGraph;
 import org.eclipse.etrice.core.room.StateTerminal;
 import org.eclipse.etrice.core.room.SubStateTrPointTerminal;
 import org.eclipse.etrice.core.room.TrPoint;
 import org.eclipse.etrice.core.room.TrPointTerminal;
 import org.eclipse.etrice.core.room.Transition;
 import org.eclipse.etrice.core.room.TransitionTerminal;
 import org.eclipse.etrice.core.room.TriggeredTransition;
 import org.eclipse.etrice.core.room.util.RoomHelpers;
 import org.eclipse.etrice.core.validation.ValidationUtil;
 import org.eclipse.etrice.ui.behavior.ImageProvider;
 import org.eclipse.etrice.ui.behavior.dialogs.TransitionPropertyDialog;
 import org.eclipse.graphiti.datatypes.ILocation;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.ICreateConnectionFeature;
 import org.eclipse.graphiti.features.IDeleteFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IReason;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.IUpdateFeature;
 import org.eclipse.graphiti.features.context.IAddConnectionContext;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.ICreateConnectionContext;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.context.IDeleteContext;
 import org.eclipse.graphiti.features.context.IDoubleClickContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
 import org.eclipse.graphiti.features.context.impl.RemoveContext;
 import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
 import org.eclipse.graphiti.features.custom.ICustomFeature;
 import org.eclipse.graphiti.features.impl.AbstractAddFeature;
 import org.eclipse.graphiti.features.impl.AbstractCreateConnectionFeature;
 import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
 import org.eclipse.graphiti.features.impl.DefaultRemoveFeature;
 import org.eclipse.graphiti.features.impl.Reason;
 import org.eclipse.graphiti.mm.GraphicsAlgorithmContainer;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Polygon;
 import org.eclipse.graphiti.mm.algorithms.Polyline;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.algorithms.styles.Color;
 import org.eclipse.graphiti.mm.algorithms.styles.Point;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeCreateService;
 import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;
 import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
 import org.eclipse.graphiti.util.ColorConstant;
 import org.eclipse.graphiti.util.IColorConstant;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 
 public class TransitionSupport {
 
 	private static final IColorConstant LINE_COLOR = new ColorConstant(0, 0, 0);
 	private static final IColorConstant INHERITED_COLOR = new ColorConstant(100, 100, 100);
 	private static final int LINE_WIDTH = 1;
 	private static final int MAX_LABEL_LENGTH = 20;
 
 	static class FeatureProvider extends DefaultFeatureProvider {
 		
 		private class CreateFeature extends AbstractCreateConnectionFeature {
 			
 			private boolean doneChanges = false;
 
 			public CreateFeature(IFeatureProvider fp) {
 				super(fp, "Transition", "create Transition");
 			}
 			
 			@Override
 			public String getCreateImageId() {
 				return ImageProvider.IMG_TRANSITION;
 			}
 	
 			@Override
 			public boolean canCreate(ICreateConnectionContext context) {
 				TransitionTerminal src = getTransitionTerminal(context.getSourceAnchor());
 				TransitionTerminal tgt = getTransitionTerminal(context.getTargetAnchor());
 				
 				if (src==null && !isInitialPoint(context.getSourceAnchor()))
 					return false;
 				if (tgt==null)
 					return false;
 				
 				StateGraph sg = getStateGraph(context);
 				if (sg==null)
 					return false;
 				
 				return ValidationUtil.isConnectable(src, tgt, sg).isOk();
 			}
 			
 			public boolean canStartConnection(ICreateConnectionContext context) {
 				TransitionTerminal src = getTransitionTerminal(context.getSourceAnchor());
 				if (src==null && !isInitialPoint(context.getSourceAnchor()))
 					return false;
 				
 				StateGraph sg = getStateGraph(context);
 				if (sg==null)
 					return false;
 				
 				return ValidationUtil.isConnectable(src, sg).isOk();
 			}
 			
 			@Override
 			public Connection create(ICreateConnectionContext context) {
 				Connection newConnection = null;
 				
 				TransitionTerminal src = getTransitionTerminal(context.getSourceAnchor());
 				TransitionTerminal dst = getTransitionTerminal(context.getTargetAnchor());
 				StateGraph sg = getStateGraph(context);
 				if (dst!=null && sg!=null) {
 
 					// TODOHRR-B transition dialog
 					// allow switch between default and non-default CP branch? This would change the transition type
 					
 					Transition trans = null;
 					if (src==null) {
 						InitialTransition t = RoomFactory.eINSTANCE.createInitialTransition();
 						t.setTo(dst);
 						trans = t;
 					}
 					else if (src instanceof SubStateTrPointTerminal
 							|| (src instanceof TrPointTerminal && ((TrPointTerminal)src).getTrPoint() instanceof EntryPoint)) {
 						ContinuationTransition t = RoomFactory.eINSTANCE.createContinuationTransition();
 						t.setFrom(src);
 						t.setTo(dst);
 						trans = t;
 					}
 					else if (src instanceof ChoicepointTerminal) {
 						boolean dfltBranch = true;
 						for (Transition tr : RoomHelpers.getAllTransitions(sg)) {
 							if (tr instanceof ContinuationTransition) {
 								TransitionTerminal from = ((ContinuationTransition) tr).getFrom();
 								if (from instanceof ChoicepointTerminal) {
 									if (((ChoicepointTerminal) from).getCp()==((ChoicepointTerminal)src).getCp())
 										dfltBranch = false;
 								}
 							}
 						}
 						NonInitialTransition t = dfltBranch? RoomFactory.eINSTANCE.createContinuationTransition()
 								: RoomFactory.eINSTANCE.createCPBranchTransition();
 						t.setFrom(src);
 						t.setTo(dst);
 						trans = t;
 					}
 					else {
 						TriggeredTransition t = RoomFactory.eINSTANCE.createTriggeredTransition();
 						t.setFrom(src);
 						t.setTo(dst);
 						trans = t;
 					}
 
 					if (trans instanceof InitialTransition) {
 						trans.setName("init");
 					}
 					else {
 						trans.setName(RoomNameProvider.getUniqueTransitionName(sg));
 					}
 
 					ActorClass ac = SupportUtil.getActorClass(getDiagram());
 					ContainerShape targetContainer = getStateGraphContainer((ContainerShape) context.getSourcePictogramElement().eContainer());
 					boolean inherited = SupportUtil.isInherited(getDiagram(), sg);
 					if (inherited) {
 						sg = SupportUtil.insertRefinedState(sg, ac, targetContainer, getFeatureProvider());
 					}
 
 					sg.getTransitions().add(trans);
 					
 		        	Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 		        	TransitionPropertyDialog dlg = new TransitionPropertyDialog(shell, sg, trans);
 					if (dlg.open()!=Window.OK) {
 						if (inherited) {
 							SupportUtil.undoInsertRefinedState(sg, ac, targetContainer, getFeatureProvider());
 						}
 						else {
 							sg.getTransitions().remove(trans);
 						}
 						return null;
 					}
 					
 					AddConnectionContext addContext = new AddConnectionContext(context.getSourceAnchor(), context.getTargetAnchor());
 					addContext.setNewObject(trans);
 					newConnection = (Connection) getFeatureProvider().addIfPossible(addContext);
 					doneChanges = true;
 				}
 				
 				return newConnection;
 			}
 			
 			@Override
 			public boolean hasDoneChanges() {
 				return doneChanges ;
 			}
 
 			private boolean isInitialPoint(Anchor anchor) {
 				if (anchor!=null) {
 					Object obj = getBusinessObjectForPictogramElement(anchor.getParent());
 					if (obj instanceof StateGraph) {
 						Object parent = getBusinessObjectForPictogramElement((ContainerShape) anchor.getParent().eContainer());
 						if (parent instanceof StateGraph)
 							return true;
 					}
 				}
 				return false;
 			}
 			
 			private TransitionTerminal getTransitionTerminal(Anchor anchor) {
 				if (anchor != null) {
 					Object obj = getBusinessObjectForPictogramElement(anchor.getParent());
 					if (obj instanceof TrPoint) {
 						Object parent = getBusinessObjectForPictogramElement((ContainerShape) anchor.getParent().eContainer());
 						if (parent instanceof State) {
 							BaseState state = (parent instanceof RefinedState)? ((RefinedState)parent).getBase() : (BaseState)parent;
 							SubStateTrPointTerminal sstpt = RoomFactory.eINSTANCE.createSubStateTrPointTerminal();
 							sstpt.setState(state);
 							sstpt.setTrPoint((TrPoint) obj);
 							return sstpt;
 						}
 						else {
 							TrPointTerminal tpt = RoomFactory.eINSTANCE.createTrPointTerminal();
 							tpt.setTrPoint((TrPoint) obj);
 							return tpt;
 						}
 					}
 					else if (obj instanceof State) {
 						BaseState state = (obj instanceof RefinedState)? ((RefinedState)obj).getBase() : (BaseState)obj;
 						StateTerminal st = RoomFactory.eINSTANCE.createStateTerminal();
 						st.setState(state);
 						return st;
 					}
 					else if (obj instanceof ChoicePoint) {
 						ChoicepointTerminal ct = RoomFactory.eINSTANCE.createChoicepointTerminal();
 						ct.setCp((ChoicePoint) obj);
 						return ct;
 					}
 				}
 				return null;
 			}
 			
 			/**
 			 * This method exploits the fact that the immediate children of the diagram are
 			 * associated with the state graphs.
 			 * 
 			 * @param shape
 			 * @return the container shape that is associated with the state graph of the diagram
 			 */
 			public ContainerShape getStateGraphContainer(ContainerShape shape) {
 				while (shape!=null) {
 					ContainerShape parent = shape.getContainer();
 					if (parent instanceof Diagram)
 						return shape;
 				}
 				return null;
 			}
 			
 			public StateGraph getStateGraph(ICreateConnectionContext context) {
 				ContainerShape shape = getStateGraphContainer((ContainerShape) context.getSourcePictogramElement().eContainer());
 				Object bo = getBusinessObjectForPictogramElement(shape);
 				if (bo instanceof StateGraph)
 					return (StateGraph) bo;
 				else
 					assert(false): "state graph expected";
 					
 				return null;
 			}
 		}
 		
 		private class AddFeature extends AbstractAddFeature {
 
 			public AddFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			public boolean canAdd(IAddContext context) {
 				if (context instanceof IAddConnectionContext && context.getNewObject() instanceof Transition) {
 					return true;
 				}
 				return false;
 			}
 
 			@Override
 			public PictogramElement add(IAddContext context) {
 				IAddConnectionContext addConContext = (IAddConnectionContext) context;
 				Transition trans = (Transition) context.getNewObject();
 				
 				// a transition target can not be the initial point (which has a StateGraph associated)
 				// so we use the target anchor to determine the StateGraph container
 				ContainerShape container = ((ContainerShape) addConContext.getTargetAnchor().getParent()).getContainer();
 				Object bo = getBusinessObjectForPictogramElement(container);
 				if (!(bo instanceof StateGraph))
 					container = container.getContainer();
 				boolean inherited = SupportUtil.isInherited(trans, container);
 				
 				IPeCreateService peCreateService = Graphiti.getPeCreateService();
 				FreeFormConnection connection = peCreateService.createFreeFormConnection(getDiagram());
 				connection.setStart(addConContext.getSourceAnchor());
 				connection.setEnd(addConContext.getTargetAnchor());
 
 				if (addConContext.getSourceAnchor()==addConContext.getTargetAnchor()) {
 					Point pt = createSelfTransitionBendPoint(connection);
 					connection.getBendpoints().add(pt);
 				}
 
 				Graphiti.getPeService().setPropertyValue(connection, Constants.TYPE_KEY, Constants.TRANS_TYPE);
 
 				IGaService gaService = Graphiti.getGaService();
 				Polyline polyline = gaService.createPolyline(connection);
 				Color color = manageColor(inherited?INHERITED_COLOR:LINE_COLOR);
 				polyline.setForeground(color);
 				polyline.setLineWidth(LINE_WIDTH);
 
 		        ConnectionDecorator cd = peCreateService
 		              .createConnectionDecorator(connection, false, 1.0, true);
 		        createArrow(cd, color);
 		        
 		        ConnectionDecorator textDecorator =
 		            peCreateService.createConnectionDecorator(connection, true,
 		            0.5, true);
 		        Text text = gaService.createDefaultText(getDiagram(), textDecorator, getLabel(trans));
 		        text.setForeground(color);
 		        gaService.setLocation(text, 10, 0);
 
 
 				// create link and wire it
 				link(connection, trans);
 
 				return connection;
 			}
 
 			private Point createSelfTransitionBendPoint(FreeFormConnection connection) {
 				ILocation begin = Graphiti.getPeService().getLocationRelativeToDiagram(connection.getStart());
 				
 				// TODOHRR: algorithm to determine self transition bend point position 
 				int deltaX = 0;
 				int deltaY = StateGraphSupport.MARGIN*3;
 				
 				return Graphiti.getGaService().createPoint(begin.getX()+deltaX, begin.getY()+deltaY);
 			}
 			
 			private Polyline createArrow(GraphicsAlgorithmContainer gaContainer, Color color) {
 
 				IGaService gaService = Graphiti.getGaService();
 				Polygon polygon =
 					gaService.createPolygon(gaContainer, new int[] { -15, 5, 0, 0, -15, -5 });
 
 				polygon.setForeground(color);
 				polygon.setBackground(color);
 				polygon.setLineWidth(LINE_WIDTH);
 
 				return polygon;
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
 				
 				if (bo instanceof Transition)
 					return true;
 				
 				return false;
 			}
 
 			@Override
 			public IReason updateNeeded(IUpdateContext context) {
 				Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 				if (bo instanceof EObject && ((EObject)bo).eIsProxy()) {
 					return Reason.createTrueReason("Transition deleted from model");
 				}
 				
 				if (bo instanceof Transition) {
 					Transition t = (Transition) bo;
 					Connection conn = (Connection)context.getPictogramElement();
 					if (conn.getConnectionDecorators().size()>=2) {
 						ConnectionDecorator cd = conn.getConnectionDecorators().get(1);
 						if (cd.getGraphicsAlgorithm() instanceof Text) {
 							Text label = (Text) cd.getGraphicsAlgorithm();
 							if (!label.getValue().equals(getLabel(t)))
 								return Reason.createTrueReason("Label needs update");
 						}
 					}
 				}
 				
 				return Reason.createFalseReason();
 			}
 
 			@Override
 			public boolean update(IUpdateContext context) {
 				Connection containerShape = (Connection)context.getPictogramElement();
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
 				
 				boolean updated = false;
 				
 				if (bo instanceof Transition) {
 					Transition t = (Transition) bo;
 					Connection conn = (Connection)context.getPictogramElement();
 					if (conn.getConnectionDecorators().size()>=2) {
 						ConnectionDecorator cd = conn.getConnectionDecorators().get(1);
 						if (cd.getGraphicsAlgorithm() instanceof Text) {
 							Text label = (Text) cd.getGraphicsAlgorithm();
 							String transitionLabelName = getLabel(t);
 							if (!label.getValue().equals(transitionLabelName)) {
 								label.setValue(transitionLabelName);
 								updated = true;
 							}
 						}
 					}
 				}
 
 				return updated;
 			}
 		}
 		
 		private static class PropertyFeature extends AbstractCustomFeature {
 
 			private String name;
 			private String description;
 			private boolean doneChanges = false;
 
 			public PropertyFeature(IFeatureProvider fp) {
 				super(fp);
 				this.name = "Edit Transition";
 				this.description = "Edit Transition";
 			}
 
 			@Override
 			public String getName() {
 				return name;
 			}
 			
 			@Override
 			public String getDescription() {
 				return description;
 			}
 			
 			@Override
 			public boolean canExecute(ICustomContext context) {
 				PictogramElement[] pes = context.getPictogramElements();
 				if (pes != null && pes.length == 1) {
 					PictogramElement pe = pes[0];
 					if (pe instanceof ConnectionDecorator)
 						pe = (PictogramElement) pe.eContainer();
 					if (!(pe instanceof Connection))
 						return false;
 					
 					Object bo = getBusinessObjectForPictogramElement(pe);
 					if (bo instanceof Transition) {
 						return true;
 					}
 				}
 				return false;
 			}
 
 			@Override
 			public void execute(ICustomContext context) {
 				PictogramElement pe = context.getPictogramElements()[0];
 				if (pe instanceof ConnectionDecorator)
 					pe = (PictogramElement) pe.eContainer();
 				Transition trans = (Transition) getBusinessObjectForPictogramElement(pe);
 				StateGraph sg = (StateGraph)trans.eContainer();
 				
 				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 				TransitionPropertyDialog dlg = new TransitionPropertyDialog(shell, sg, trans);
 				if (dlg.open()!=Window.OK)
 					return;
 
 				doneChanges = true;
 				updateLabel(trans, (Connection) pe);
 			}
 			
 			@Override
 			public boolean hasDoneChanges() {
 				return doneChanges;
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
 				PictogramElement pe = context.getPictogramElement();
 				if (pe instanceof ConnectionDecorator)
 					pe = (PictogramElement) pe.eContainer();
 				if (!(pe instanceof Connection))
 					return false;
 				
 				Object bo = getBusinessObjectForPictogramElement(pe);
 				if (bo instanceof Transition) {
 					return true;
 				}
 				return false;
 			}
 		}
 		
 		private IFeatureProvider fp;
 		
 		public FeatureProvider(IDiagramTypeProvider dtp, IFeatureProvider fp) {
 			super(dtp);
 			this.fp = fp;
 		}
 
 		@Override
 		public ICreateConnectionFeature[] getCreateConnectionFeatures() {
 			return new ICreateConnectionFeature[] { new CreateFeature(fp) };
 		}
 		
 		@Override
 		public IAddFeature getAddFeature(IAddContext context) {
 			return new AddFeature(fp);
 		}
 		
 		@Override
 		public IUpdateFeature getUpdateFeature(IUpdateContext context) {
 			return new UpdateFeature(fp);
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
 			return new ICustomFeature[] { new PropertyFeature(fp) };
 		}
 		
 		protected static void updateLabel(Transition trans, Connection conn) {
 			if (conn.getConnectionDecorators().size()<2)
 				return;
 			
 			ConnectionDecorator cd = conn.getConnectionDecorators().get(1);
 			if (cd.getGraphicsAlgorithm() instanceof Text) {
 				Text label = (Text) cd.getGraphicsAlgorithm();
 				label.setValue(getLabel(trans));
 			}
 		}
 		
 		protected static String getLabel(Transition trans) {
 			String label = RoomNameProvider.getTransitionLabelName(trans);
 			if (label.length()>MAX_LABEL_LENGTH)
 				label = label.substring(0, MAX_LABEL_LENGTH)+"...";
 			return label;
 		}
 	}
 	
 	class BehaviorProvider extends DefaultToolBehaviorProvider {
 
 		public BehaviorProvider(IDiagramTypeProvider dtp) {
 			super(dtp);
 		}
 		
 		@Override
 		public ICustomFeature getDoubleClickFeature(IDoubleClickContext context) {
 			return new FeatureProvider.PropertyFeature(getDiagramTypeProvider().getFeatureProvider());
 		}
 		
 		@Override
 		public String getToolTip(GraphicsAlgorithm ga) {
 			// if this is called we know there is a business object!=null
 			PictogramElement pe = ga.getPictogramElement();
 			if (pe instanceof ConnectionDecorator)
 				pe = (PictogramElement) pe.eContainer();
 			
 			EObject bo = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe);
 			if (bo instanceof Transition) {
 				Transition tr = (Transition) bo;
 				String label = RoomNameProvider.getTransitionLabelName(tr);
 				if (RoomHelpers.hasDetailCode(tr.getAction())) {
 					if (label.length()>0)
 						label += "\n";
 					label += "action:\n"+RoomHelpers.getDetailCode(tr.getAction());
 				}
 				return label;
 			}
 			
 			return super.getToolTip(ga);
 		}
 	}
 	
 	private FeatureProvider pfp;
 	private BehaviorProvider tbp;
 	
 	public TransitionSupport(IDiagramTypeProvider dtp, IFeatureProvider fp) {
 		pfp = new FeatureProvider(dtp,fp);
 		tbp = new BehaviorProvider(dtp);
 	}
 	
 	public IFeatureProvider getFeatureProvider() {
 		return pfp;
 	}
 	
 	public IToolBehaviorProvider getToolBehaviorProvider() {
 		return tbp;
 	}
 }
