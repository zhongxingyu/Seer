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
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.InitialTransition;
 import org.eclipse.etrice.core.room.StateGraph;
 import org.eclipse.etrice.core.room.Transition;
 import org.eclipse.etrice.ui.behavior.ImageProvider;
import org.eclipse.etrice.ui.common.support.CommonSupportUtil;
 import org.eclipse.etrice.ui.common.support.DeleteWithoutConfirmFeature;
 import org.eclipse.etrice.ui.common.support.NoResizeFeature;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.ICreateConnectionFeature;
 import org.eclipse.graphiti.features.ICreateFeature;
 import org.eclipse.graphiti.features.IDeleteFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IMoveShapeFeature;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.IResizeShapeFeature;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.context.IDeleteContext;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.features.context.IPictogramElementContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
 import org.eclipse.graphiti.features.impl.AbstractAddFeature;
 import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
 import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
 import org.eclipse.graphiti.features.impl.DefaultRemoveFeature;
 import org.eclipse.graphiti.mm.algorithms.Ellipse;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Rectangle;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.algorithms.styles.Color;
 import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
 import org.eclipse.graphiti.mm.pictograms.ChopboxAnchor;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeCreateService;
 import org.eclipse.graphiti.tb.ContextButtonEntry;
 import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
 import org.eclipse.graphiti.tb.IContextButtonPadData;
 import org.eclipse.graphiti.tb.IToolBehaviorProvider;
 import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
 import org.eclipse.graphiti.util.ColorConstant;
 import org.eclipse.graphiti.util.IColorConstant;
 
 public class InitialPointSupport {
 	
 	public static final int ITEM_SIZE = StateGraphSupport.MARGIN;
 	public static final int ITEM_SIZE_SMALL = StateSupport.MARGIN;
 
 	protected static final int LINE_WIDTH = 2;
 	protected static final IColorConstant DARK_COLOR = new ColorConstant(0, 0, 0);
 	protected static final IColorConstant INHERITED_COLOR = new ColorConstant(100, 100, 100);
 	protected static final IColorConstant BRIGHT_COLOR = new ColorConstant(255, 255, 255);
 	protected static final String PROP_KIND = "item-kind";
 	
 	private static class FeatureProvider extends DefaultFeatureProvider {
 		
 		private static class CreateFeature extends AbstractCreateFeature {
 	
 			public CreateFeature(IFeatureProvider fp, String name, String description) {
 				super(fp, name, description);
 			}
 			
 			@Override
 			public String getCreateImageId() {
 				return ImageProvider.IMG_INITIAL;
 			}
 	
 			@Override
 			public Object[] create(ICreateContext context) {
 				ContainerShape targetContainer = context.getTargetContainer();
 				StateGraph sg = (StateGraph) targetContainer.getLink().getBusinessObjects().get(0);
 
 				boolean inherited = SupportUtil.isInherited(getDiagram(), sg);
 				if (inherited) {
 					ActorClass ac = SupportUtil.getActorClass(getDiagram());
 					sg = SupportUtil.insertRefinedState(sg, ac, targetContainer, getFeatureProvider());
 				}
 		        
 				// We don't create anything here since in the model the initial point is
 				// implicit only. We use the state graph itself as model element.
 				// The situation can be distinguished using the context.
 				
 		        // do the add
 		        addGraphicalRepresentation(context, sg);
 	
 		        // return newly created business object(s)
 		        return new Object[] { sg };
 			}
 	
 			@Override
 			public boolean canCreate(ICreateContext context) {
 				if (context.getTargetContainer().getLink()!=null)
 					if (context.getTargetContainer().getLink().getBusinessObjects().size()==1) {
 						EObject obj = context.getTargetContainer().getLink().getBusinessObjects().get(0);
 						if (obj instanceof StateGraph) {
 							ContainerShape parent = context.getTargetContainer().getContainer();
 							if (! (parent instanceof StateGraph)) {
 								StateGraph sg = (StateGraph) obj;
 								for (Transition t : sg.getTransitions()) {
 									if (t instanceof InitialTransition)
 										return false;
 								}
 								return true;
 							}
 						}
 					}
 				return false;
 			}
 		}
 		
 		private class AddFeature extends AbstractAddFeature {
 	
 			public AddFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 			
 			@Override
 			public boolean canAdd(IAddContext context) {
 				if (context.getNewObject() instanceof StateGraph) {
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
 				ContainerShape sgShape = context.getTargetContainer();
 				StateGraph sg = (StateGraph) context.getNewObject();
 	
 				boolean inherited = false;
 				for (Transition tr : sg.getTransitions()) {
 					if (tr instanceof InitialTransition) {
 						inherited = SupportUtil.isInherited(tr, sgShape);
 					}
 				}
 				
 				// CONTAINER SHAPE WITH RECTANGLE
 				IPeCreateService peCreateService = Graphiti.getPeCreateService();
 				ContainerShape containerShape =
 					peCreateService.createContainerShape(sgShape, true);
 	
 				Graphiti.getPeService().setPropertyValue(containerShape, Constants.TYPE_KEY, Constants.TRP_TYPE);
 				
 				int x = context.getX()-ITEM_SIZE;
 				int y = context.getY()-ITEM_SIZE;
 				
 				
 				Color dark = manageColor(inherited? INHERITED_COLOR:DARK_COLOR);
 				IGaService gaService = Graphiti.getGaService();
 				{
 					final Rectangle invisibleRectangle = gaService.createInvisibleRectangle(containerShape);
 					gaService.setLocationAndSize(invisibleRectangle, x, y, 2*ITEM_SIZE, 2*ITEM_SIZE);
 	
 					createFigure(containerShape,
 							invisibleRectangle,
 							dark,
 							manageColor(BRIGHT_COLOR));
 	
 					// create link and wire it
 					link(containerShape, sg);
 				}
 				
 				{
 					Shape labelShape = peCreateService.createShape(containerShape, false);
 					Text label = gaService.createDefaultText(getDiagram(), labelShape, "I");
 					label.setForeground(dark);
 					label.setBackground(dark);
 					label.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
 					label.setVerticalAlignment(Orientation.ALIGNMENT_CENTER);
 					gaService.setLocationAndSize(label, 0, ITEM_SIZE/2, 2*ITEM_SIZE, ITEM_SIZE);
 				}
 
 				// call the layout feature
 				layoutPictogramElement(containerShape);
 	
 				return containerShape;
 	
 			}
 
 		}
 		
 		protected class MoveShapeFeature extends DefaultMoveShapeFeature {
 	
 			public MoveShapeFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 	
 			@Override
 			public boolean canMoveShape(IMoveShapeContext context) {
 				boolean canMove = super.canMoveShape(context);
 	
 				if (canMove) {
 					Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 					if (bo instanceof StateGraph) {
 						return true;
 					}
 					return false;
 				}
 				
 				return canMove;
 			}
 		}
 		
 		protected static class RemoveFeature extends DefaultRemoveFeature {
 
 			public RemoveFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			public boolean canRemove(IRemoveContext context) {
 				Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 				if (bo instanceof StateGraph) {
 					return true;
 				}
 				return false;
 			}
			
			@Override
			public void preRemove(IRemoveContext context) {
				super.preRemove(context);

				ContainerShape container = (ContainerShape) context.getPictogramElement();
				CommonSupportUtil.deleteConnectionsRecursive(container, getFeatureProvider());
			}
 		}
 		
 		protected static class DeleteFeature extends DeleteWithoutConfirmFeature {
 
 			public DeleteFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 			
 			@Override
 			public boolean canDelete(IDeleteContext context) {
 				// deny deletion because we don't want to lose our
 				// business object, the state graph
 				return false;
 			}
 		}
 		
 		protected IFeatureProvider fp;
 		
 		protected FeatureProvider(IDiagramTypeProvider dtp, IFeatureProvider fp) {
 			super(dtp);
 			this.fp = fp;
 		}
 		
 		@Override
 		public ICreateFeature[] getCreateFeatures() {
 			return new ICreateFeature[] {
 					new CreateFeature(fp, "Initial Point", "Create Initial Point")
 				};
 		}
 		
 		@Override
 		public IAddFeature getAddFeature(IAddContext context) {
 			return new AddFeature(fp);
 		}
 	
 		@Override
 		public IMoveShapeFeature getMoveShapeFeature(IMoveShapeContext context) {
 			return new MoveShapeFeature(fp);
 		}
 		
 		@Override
 		public IResizeShapeFeature getResizeShapeFeature(IResizeShapeContext context) {
 			return new NoResizeFeature(fp);
 		}
 
 		@Override
 		public IRemoveFeature getRemoveFeature(IRemoveContext context) {
 			return new RemoveFeature(fp);
 		}
 
 		@Override
 		public IDeleteFeature getDeleteFeature(IDeleteContext context) {
 			return new DeleteFeature(fp);
 		}
 		
 		protected static void createFigure( 
 				ContainerShape containerShape,
 				GraphicsAlgorithm invisibleRectangle, Color darkColor, Color brightColor) {
 
 			IGaService gaService = Graphiti.getGaService();
 			
 			Ellipse circle = gaService.createEllipse(invisibleRectangle);
 			circle.setForeground(darkColor);
 			circle.setBackground(brightColor);
 			circle.setLineWidth(LINE_WIDTH);
 			int s2 = ITEM_SIZE/2;
 			gaService.setLocationAndSize(circle, s2, s2, ITEM_SIZE, ITEM_SIZE);
 
 			if (containerShape.getAnchors().isEmpty()) {
 				// here we place our anchor
 				IPeCreateService peCreateService = Graphiti.getPeCreateService();
 				// TODOHRR:  EllipseAnchor would be nice
 				ChopboxAnchor anchor = peCreateService.createChopboxAnchor(containerShape);
 				anchor.setReferencedGraphicsAlgorithm(circle);
 			}
 			else {
 				// we just set the referenced GA
 				//containerShape.getAnchors().get(0).setReferencedGraphicsAlgorithm(rect);
 			}
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
 	
 	public InitialPointSupport(IDiagramTypeProvider dtp, IFeatureProvider fp) {
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
