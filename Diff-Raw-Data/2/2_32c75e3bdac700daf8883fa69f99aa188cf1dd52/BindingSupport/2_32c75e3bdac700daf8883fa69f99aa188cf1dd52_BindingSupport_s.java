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
 
 package org.eclipse.etrice.ui.structure.support;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.etrice.core.validation.ValidationUtil;
 import org.eclipse.etrice.ui.structure.ImageProvider;
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
 import org.eclipse.graphiti.features.context.IDeleteContext;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.IUpdateContext;
 import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
 import org.eclipse.graphiti.features.context.impl.RemoveContext;
 import org.eclipse.graphiti.features.impl.AbstractAddFeature;
 import org.eclipse.graphiti.features.impl.AbstractCreateConnectionFeature;
 import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
 import org.eclipse.graphiti.features.impl.DefaultRemoveFeature;
 import org.eclipse.graphiti.features.impl.Reason;
 import org.eclipse.graphiti.mm.algorithms.Polyline;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
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
 
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.ActorContainerClass;
 import org.eclipse.etrice.core.room.ActorContainerRef;
 import org.eclipse.etrice.core.room.Binding;
 import org.eclipse.etrice.core.room.BindingEndPoint;
 import org.eclipse.etrice.core.room.LogicalSystem;
 import org.eclipse.etrice.core.room.Port;
 import org.eclipse.etrice.core.room.RoomFactory;
 import org.eclipse.etrice.core.room.StructureClass;
 import org.eclipse.etrice.core.room.SubSystemClass;
 
 public class BindingSupport {
 
 	public static final IColorConstant LINE_COLOR = new ColorConstant(0, 0, 0);
 	public static final IColorConstant INHERITED_COLOR = new ColorConstant(100, 100, 100);
 
 	class FeatureProvider extends DefaultFeatureProvider {
 		
 		private class CreateFeature extends AbstractCreateConnectionFeature {
 			
 			private boolean justStarted = false;
 
 			public CreateFeature(IFeatureProvider fp) {
 				super(fp, "Binding", "create Binding");
 			}
 			
 			@Override
 			public String getCreateImageId() {
 				return ImageProvider.IMG_BINDING;
 			}
 	
 			@Override
 			public boolean canCreate(ICreateConnectionContext context) {
 				Port src = getPort(context.getSourceAnchor());
 				Port tgt = getPort(context.getTargetAnchor());
 				ActorContainerRef srcRef = getRef(context.getSourceAnchor());
 				
 				if (justStarted) {
 					justStarted = false;
 					beginHighLightMatches(src, srcRef);
 				}
 				
 				if (src==null || tgt==null) {
 					return false;
 				}
 				
 				StructureClass ac = getParent(context);
 				if (ac==null) {
 					return false;
 				}
 				
 				ActorContainerRef tgtRef = getRef(context.getTargetAnchor());
 				
 				return ValidationUtil.isConnectable(src, srcRef, tgt, tgtRef, ac).isOk();
 			}
 			
 			public boolean canStartConnection(ICreateConnectionContext context) {
 				Port src = getPort(context.getSourceAnchor());
 				boolean canStart = src!=null;
 				if (canStart) {
 					ActorContainerRef ref = getRef(context.getSourceAnchor());
 					if (ref==null) {
 						// this port is local, i.e. owned by the parent itself
 						ActorContainerClass acc = (ActorContainerClass) src.eContainer();
 						if (!ValidationUtil.isConnectable(src, null, acc).isOk())
 							canStart = false;
 					}
 					else {
 						ActorContainerClass acc = (ActorContainerClass) ref.eContainer();
 						if (!ValidationUtil.isConnectable(src, ref, acc).isOk())
 							canStart = false;
 					}
 				}
 				if (canStart)
 					justStarted = true;
 				return canStart;
 			}
 
 			private Port getPort(Anchor anchor) {
 				if (anchor != null) {
 					Object obj = getBusinessObjectForPictogramElement(anchor.getParent());
 					if (obj instanceof Port) {
 						return (Port) obj;
 					}
 				}
 				return null;
 			}
 			
 			public StructureClass getParent(ICreateConnectionContext context) {
 				ContainerShape shape = (ContainerShape) context.getSourcePictogramElement().eContainer();
 				Object bo = getBusinessObjectForPictogramElement(shape);
 				if (bo instanceof StructureClass)
 					return (StructureClass) bo;
 				
 				shape = (ContainerShape) shape.eContainer();
 				bo = getBusinessObjectForPictogramElement(shape);
 				if (bo instanceof StructureClass)
 					return (StructureClass) bo;
 				
 				return null;
 			}
 			
 			public ActorContainerRef getRef(Anchor anchor) {
 				ContainerShape shape = (ContainerShape) anchor.getParent().eContainer();
 				Object bo = getBusinessObjectForPictogramElement(shape);
 				if (bo instanceof ActorContainerRef)
 					return (ActorContainerRef) bo;
 
 				return null;
 			}
 			
 			@Override
 			public Connection create(ICreateConnectionContext context) {
 				Connection newConnection = null;
 				
 				endHighLightMatches();
 				
 				Port src = getPort(context.getSourceAnchor());
 				Port dst = getPort(context.getTargetAnchor());
 				StructureClass ac = getParent(context);
 				if (src!=null && dst!=null && ac!=null) {
 					Binding bind = RoomFactory.eINSTANCE.createBinding();
 					BindingEndPoint ep1 = RoomFactory.eINSTANCE.createBindingEndPoint();
 					ActorContainerRef ar1 = getRef(context.getSourceAnchor());
 					ep1.setPort(src);
 					ep1.setActorRef(ar1);
 					BindingEndPoint ep2 = RoomFactory.eINSTANCE.createBindingEndPoint();
 					ActorContainerRef ar2 = getRef(context.getTargetAnchor());
 					ep2.setPort(dst);
 					ep2.setActorRef(ar2);
 					bind.setEndpoint1(ep1);
 					bind.setEndpoint2(ep2);
 					ac.getBindings().add(bind);
 					
 					AddConnectionContext addContext = new AddConnectionContext(context.getSourceAnchor(), context.getTargetAnchor());
 					addContext.setNewObject(bind);
 					newConnection = (Connection) getFeatureProvider().addIfPossible(addContext);
 				}
 				
 				return newConnection;
 			}
 			
 			private void beginHighLightMatches(Port port, ActorContainerRef ref) {
 				if (port==null)
 					return;
 				
 				StructureClass acc = (ActorContainerClass) ((ref!=null)? ref.eContainer():port.eContainer());
 				if (acc instanceof ActorClass) {
 					
 				}
 				else if (acc instanceof SubSystemClass) {
 					
 				}
 				else if (acc instanceof LogicalSystem) {
 					
 				}
 				else {
 					assert(false): "unknown kind of StructureClass";
 				}
 			}
 
 			private void endHighLightMatches() {
 			}
 		}
 		
 		private class AddFeature extends AbstractAddFeature {
 
 			public AddFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 
 			@Override
 			public boolean canAdd(IAddContext context) {
 				if (context instanceof IAddConnectionContext && context.getNewObject() instanceof Binding) {
 					return true;
 				}
 				return false;
 			}
 
 			@Override
 			public PictogramElement add(IAddContext context) {
 				IAddConnectionContext addConContext = (IAddConnectionContext) context;
 				Binding addedEReference = (Binding) context.getNewObject();
 
 				boolean inherited = isInherited(getDiagram(), addedEReference);
 				
 				IPeCreateService peCreateService = Graphiti.getPeCreateService();
 				// CONNECTION WITH POLYLINE
 				Connection connection = peCreateService.createFreeFormConnection(getDiagram());
 				connection.setStart(addConContext.getSourceAnchor());
 				connection.setEnd(addConContext.getTargetAnchor());
 				
 				Graphiti.getPeService().setPropertyValue(connection, Constants.TYPE_KEY, Constants.BIND_TYPE);
 
 				IGaService gaService = Graphiti.getGaService();
 				Polyline polyline = gaService.createPolyline(connection);
 				polyline.setForeground(manageColor(inherited?INHERITED_COLOR:LINE_COLOR));
 
 				// create link and wire it
 				link(connection, addedEReference);
 
 				return connection;
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
 				
 				return false;
 			}
 
 			@Override
 			public IReason updateNeeded(IUpdateContext context) {
 				Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
 				if (bo instanceof EObject && ((EObject)bo).eIsProxy()) {
 					return Reason.createTrueReason("Binding deleted from model");
 				}
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
 				return false;
 			}
 		}
 		
 		private class RemoveFeature extends DefaultRemoveFeature {
 
 			public RemoveFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 			
 			@Override
 			public boolean canRemove(IRemoveContext context) {
 				return false;
 			}
 		}
 		
 		private class DeleteFeature extends DefaultDeleteFeature {
 
 			public DeleteFeature(IFeatureProvider fp) {
 				super(fp);
 			}
 			
 			@Override
 			public boolean canDelete(IDeleteContext context) {
 				EObject bo = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(context.getPictogramElement());
 				if (bo instanceof Binding) {
 					Binding b = (Binding) bo;
 					if (isInherited(getDiagram(), b))
 						return false;
 				}
 				
 				return true;
 			}
 		}
 		
 		private IFeatureProvider fp;
 		
 		public FeatureProvider(IDiagramTypeProvider dtp, IFeatureProvider fp) {
 			super(dtp);
 			this.fp = fp;
 		}
 
 		private boolean isInherited(Diagram diag, Binding b) {
			ActorClass ac = (ActorClass) Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(diag.getChildren().get(0));
 			return (b.eContainer()!=ac);
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
 	}
 	
 	class BehaviorProvider extends DefaultToolBehaviorProvider {
 
 		public BehaviorProvider(IDiagramTypeProvider dtp) {
 			super(dtp);
 		}
 	}
 	
 	private FeatureProvider pfp;
 	private BehaviorProvider tbp;
 	
 	public BindingSupport(IDiagramTypeProvider dtp, IFeatureProvider fp) {
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
