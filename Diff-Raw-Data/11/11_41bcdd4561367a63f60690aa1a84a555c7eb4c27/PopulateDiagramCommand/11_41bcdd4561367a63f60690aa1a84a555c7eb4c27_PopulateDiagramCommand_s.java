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
 
 package org.eclipse.etrice.ui.structure.commands;
 
 import java.util.HashMap;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.transaction.RecordingCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.ActorContainerClass;
 import org.eclipse.etrice.core.room.ActorContainerRef;
 import org.eclipse.etrice.core.room.ActorRef;
 import org.eclipse.etrice.core.room.Binding;
 import org.eclipse.etrice.core.room.BindingEndPoint;
 import org.eclipse.etrice.core.room.InterfaceItem;
 import org.eclipse.etrice.core.room.LayerConnection;
 import org.eclipse.etrice.core.room.LogicalSystem;
 import org.eclipse.etrice.core.room.Port;
 import org.eclipse.etrice.core.room.RefSAPoint;
 import org.eclipse.etrice.core.room.RelaySAPoint;
 import org.eclipse.etrice.core.room.SAPoint;
 import org.eclipse.etrice.core.room.SPPRef;
 import org.eclipse.etrice.core.room.SPPoint;
 import org.eclipse.etrice.core.room.StructureClass;
 import org.eclipse.etrice.core.room.SubSystemClass;
 import org.eclipse.etrice.ui.structure.DiagramAccess;
 import org.eclipse.etrice.ui.structure.support.ActorContainerRefSupport;
 import org.eclipse.etrice.ui.structure.support.StructureClassSupport;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
 import org.eclipse.graphiti.features.context.impl.AddContext;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.ui.services.GraphitiUi;
 
 public class PopulateDiagramCommand extends RecordingCommand {
 
 	private static final String SEP = "#";
 
 	private StructureClass sc;
 	private Diagram diagram;
 
 	public PopulateDiagramCommand(Diagram diag, StructureClass sc, TransactionalEditingDomain domain) {
 		super(domain);
 		this.diagram = diag;
 		this.sc = sc;
 	}
 
 	@Override
 	protected void doExecute() {
 		IDiagramTypeProvider dtp = GraphitiUi.getExtensionManager().createDiagramTypeProvider(diagram, "org.eclipse.etrice.ui.structure.diagramTypeProvider"); //$NON-NLS-1$
 		IFeatureProvider featureProvider = dtp.getFeatureProvider();
 		
 		AddContext addContext = new AddContext();
 		addContext.setNewObject(sc);
 		addContext.setTargetContainer(diagram);
 		addContext.setX(StructureClassSupport.MARGIN);
 		addContext.setY(StructureClassSupport.MARGIN);
 
 		final HashMap<String, Anchor> ifitem2anchor = new HashMap<String, Anchor>();
 		
 		IAddFeature addFeature = featureProvider.getAddFeature(addContext);
 		if (addFeature!=null && addFeature.canAdd(addContext)) {
 			ContainerShape acShape = (ContainerShape) addFeature.add(addContext);
 
 			int width = acShape.getGraphicsAlgorithm().getGraphicsAlgorithmChildren().get(0).getWidth();
 			
 			addInterfaceItems(acShape, width, featureProvider, ifitem2anchor);
 			
 			// actor container references
 			if (sc instanceof ActorContainerClass) {
 				ActorContainerClass acc = (ActorContainerClass) sc;
 				
 	        	EList<? extends ActorContainerRef> actorRefs = acc.getActorRefs();
 				addRefItems(actorRefs, acShape, width, featureProvider, ifitem2anchor);
 			}
 			else if (sc instanceof LogicalSystem) {
 				LogicalSystem sys = (LogicalSystem) sc;
 				
 	        	EList<? extends ActorContainerRef> subSystems = sys.getSubSystems();
 				addRefItems(subSystems, acShape, width, featureProvider, ifitem2anchor);
 			}
			
			// bindings
			for (Binding bind : sc.getBindings()) {
				addBinding(bind, featureProvider, ifitem2anchor);
			}
 
 			// layer connections
 			for (LayerConnection lc : sc.getConnections()) {
 				addLayerConnection(lc, featureProvider, ifitem2anchor);
 			}
 			
 			// base class items
 			if (sc instanceof ActorClass) {
 				ActorClass base = ((ActorClass) sc).getBase();
 				
 				// add inherited ports and refs and bindings (and preserve layout)
 				if (base!=null)
 					addInheritedItems(base, acShape, ifitem2anchor, featureProvider);
 			}
 		}
 		
 	}
 
 	protected void addInterfaceItems(ContainerShape acShape, int width,
 			IFeatureProvider featureProvider,
 			final HashMap<String, Anchor> port2anchor) {
 		if (sc instanceof ActorClass) {
 			ActorClass ac = (ActorClass) sc;
 			
 			// interface spps and ports
 			int n = ac.getIfPorts().size() + ac.getIfSPPs().size();
 			int delta = width/(n+1);
 			int pos = delta;
 			for (SPPRef spp : ac.getIfSPPs()) {
 				addInterfaceItem(spp, acShape, pos, featureProvider, port2anchor);
 				pos += delta;
 			}
 			for (Port port : ac.getIfPorts()) {
 				addInterfaceItem(port, acShape, pos, featureProvider, port2anchor);
 				pos += delta;
 			}
 			
 			// internal ports
 			n = ac.getIntPorts().size();
 			delta = width/(n+1);
 			pos = delta;
 			addPorts(ac.getIntPorts(), acShape, width, featureProvider, port2anchor);
 		}
 		else if (sc instanceof SubSystemClass) {
 			SubSystemClass ssc = (SubSystemClass) sc;
 			
 			// interface spps and ports
 			int n = ssc.getRelayPorts().size() + ssc.getIfSPPs().size();
 			int delta = width/(n+1);
 			int pos = delta;
 			for (SPPRef spp : ssc.getIfSPPs()) {
 				addInterfaceItem(spp, acShape, pos, featureProvider, port2anchor);
 				pos += delta;
 			}
 			for (Port port : ssc.getRelayPorts()) {
 				addInterfaceItem(port, acShape, pos, featureProvider, port2anchor);
 				pos += delta;
 			}
 		}
 	}
 
 	protected void addRefItems(EList<? extends ActorContainerRef> actorRefs,
 			ContainerShape acShape, int width,
 			IFeatureProvider featureProvider, final HashMap<String, Anchor> port2anchor) {
 		int n = actorRefs.size();
 		int delta = width/(n+1);
 		int pos = delta;
 		for (ActorContainerRef ar : actorRefs) {
 			addRefItem(ar, acShape, pos, featureProvider, port2anchor);
 			pos += delta;
 		}
 	}
 
 	protected void addPorts(EList<Port> ifPorts, ContainerShape acShape,
 			int width, IFeatureProvider featureProvider, final HashMap<String, Anchor> port2anchor) {
 		int n = ifPorts.size();
 		int delta = width/(n+1);
 		int pos = delta;
 		for (Port port : ifPorts) {
 			addInterfaceItem(port, acShape, pos, featureProvider, port2anchor);
 			pos += delta;
 		}
 	}
 
 	protected void addRefItem(ActorContainerRef obj, ContainerShape acShape, int pos, IFeatureProvider featureProvider, final HashMap<String, Anchor> port2anchor) {
 		AddContext addContext = new AddContext();
 		addContext.setNewObject(obj);
 		addContext.setTargetContainer(acShape);
 		addContext.setX(pos+StructureClassSupport.MARGIN-(ActorContainerRefSupport.DEFAULT_SIZE_X/2+ActorContainerRefSupport.MARGIN));
 		addContext.setY(4*StructureClassSupport.MARGIN);
 		
 		ContainerShape refShape = (ContainerShape) featureProvider.addIfPossible(addContext);
 		
 		getAnchors(obj, refShape, port2anchor);
 	}
 
 	protected void addInterfaceItem(InterfaceItem item, ContainerShape acShape, int pos, IFeatureProvider featureProvider, final HashMap<String, Anchor> port2anchor) {
 		AddContext addContext = new AddContext();
 		addContext.setNewObject(item);
 		addContext.setTargetContainer(acShape);
 		addContext.setX(pos+StructureClassSupport.MARGIN);
 		addContext.setY(StructureClassSupport.MARGIN);
 		
 		ContainerShape pe = (ContainerShape) featureProvider.addIfPossible(addContext);
 		assert(!pe.getAnchors().isEmpty()): "interface item should have an anchor";
 		port2anchor.put(SEP+item.getName(), pe.getAnchors().get(0));
 	}
 
 	protected void addBinding(Binding bind, IFeatureProvider featureProvider,
 			final HashMap<String, Anchor> port2anchor) {
 		String ep1 = getName(bind.getEndpoint1());
 		String ep2 = getName(bind.getEndpoint2());
 		Anchor a1 = port2anchor.get(ep1);
 		Anchor a2 = port2anchor.get(ep2);
 		assert(a1!=null && a2!=null): "ports for binding must be present";
 		
 		AddConnectionContext context = new AddConnectionContext(a1, a2);
 		context.setNewObject(bind);
 		featureProvider.addIfPossible(context);
 	}
 
 	protected void addLayerConnection(LayerConnection lc, IFeatureProvider featureProvider,
 			final HashMap<String, Anchor> port2anchor) {
 		String ep1 = getName(lc.getFrom());
 		String ep2 = getName(lc.getTo());
 		Anchor a1 = port2anchor.get(ep1);
 		Anchor a2 = port2anchor.get(ep2);
 		assert(a1!=null && a2!=null): "ports for layer connection must be present";
 		
 		AddConnectionContext context = new AddConnectionContext(a1, a2);
 		context.setNewObject(lc);
 		featureProvider.addIfPossible(context);
 	}
 
 	private void addInheritedItems(ActorClass ac, ContainerShape acShape, HashMap<String,Anchor> port2anchor, IFeatureProvider featureProvider) {
 		
 		// we don't have to recurse since the base class diagram already contains all inherited items
 		
 		Diagram refDiag = DiagramAccess.getDiagram(ac);
 
 		ResourceSet rs = ac.eResource().getResourceSet();
 		
 		if (!refDiag.getChildren().isEmpty()) {
 			ContainerShape refAcShape = (ContainerShape) refDiag.getChildren().get(0);
 			Object bo = featureProvider.getBusinessObjectForPictogramElement(refAcShape);
 			if (bo instanceof StructureClass) {
 				StructureClass extRefClass = (StructureClass) bo;
 				assert(extRefClass.getName().equals(ac.getName())): "actor class names must match";
 				
 				int scaleX = refAcShape.getGraphicsAlgorithm().getWidth()/StructureClassSupport.DEFAULT_SIZE_X;
 				int scaleY = refAcShape.getGraphicsAlgorithm().getHeight()/StructureClassSupport.DEFAULT_SIZE_Y;
 				
 				for (Shape childShape : refAcShape.getChildren()) {
 					bo = featureProvider.getBusinessObjectForPictogramElement(childShape);
 					if (bo instanceof Port) {
 						Port ownObject = (Port) getOwnObject((Port)bo, rs);
 						int x = childShape.getGraphicsAlgorithm().getX()/scaleX;
 						int y = childShape.getGraphicsAlgorithm().getY()/scaleY;
 						ContainerShape pe = addItem(ownObject, x/scaleX, y/scaleY, acShape, featureProvider);
 						assert(!pe.getAnchors().isEmpty()): "port must have an anchor";
 						port2anchor.put(SEP+ownObject.getName(), pe.getAnchors().get(0));
 					}
 					else if (bo instanceof ActorRef) {
 						ActorContainerRef ownObject = (ActorContainerRef) getOwnObject((ActorRef)bo, rs);
 						int x = childShape.getGraphicsAlgorithm().getX()/scaleX;
 						int y = childShape.getGraphicsAlgorithm().getY()/scaleY;
 						ContainerShape arShape = addItem(ownObject, x/scaleX, y/scaleY, acShape, featureProvider);
 						getAnchors(ownObject, arShape, port2anchor);
 					}
 				}
 				for (Connection conn : refDiag.getConnections()) {
 					bo = featureProvider.getBusinessObjectForPictogramElement(conn);
 					if (bo instanceof Binding) {
 						Binding bind = (Binding)getOwnObject((Binding)bo, rs);
 						addBinding(bind, featureProvider, port2anchor);
 					}
 				}
 			}
 		}
 	}
 	
 	private ContainerShape addItem(EObject obj, int x, int y, ContainerShape container, IFeatureProvider featureProvider) {
 		AddContext addContext = new AddContext();
 
 		addContext.setNewObject(obj);
 		addContext.setTargetContainer(container);
 		addContext.setX(x);
 		addContext.setY(y);
 		ContainerShape newShape = (ContainerShape) featureProvider.addIfPossible(addContext);
 		assert(newShape!=null): "shape creation must succeed";
 		return newShape;
 	}
 	
 	private EObject getOwnObject(EObject obj, ResourceSet rs) {
 		URI uri = EcoreUtil.getURI(obj);
 		EObject own = rs.getEObject(uri, true);
 		assert(own!=null): "own object must exist";
 		return own;
 	}
 
 	protected void getAnchors(ActorContainerRef acr, PictogramElement refShape,
 			final HashMap<String, Anchor> port2anchor) {
 		
 		if (refShape instanceof ContainerShape) {
 			port2anchor.put(acr.getName()+SEP, ((ContainerShape)refShape).getAnchors().get(0));
 			for (Shape child : ((ContainerShape) refShape).getChildren()) {
 				if (child instanceof ContainerShape) {
 					ContainerShape childShape = (ContainerShape) child;
 					if (!childShape.getAnchors().isEmpty()) {
 						if (!childShape.getLink().getBusinessObjects().isEmpty()) {
 							EObject obj = childShape.getLink().getBusinessObjects().get(0);
 							if (obj instanceof Port) {
 								port2anchor.put(acr.getName()+SEP+((Port)obj).getName(), childShape.getAnchors().get(0));
 							}
 							if (obj instanceof SPPRef) {
 								port2anchor.put(acr.getName()+SEP+((SPPRef)obj).getName(), childShape.getAnchors().get(0));
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private String getName(BindingEndPoint ep) {
 		String ar = ep.getActorRef()==null? "":ep.getActorRef().getName();
 		String p = ep.getPort().getName();
 		return ar+SEP+p;
 	}
 
 	private String getName(SAPoint sapt) {
 		if (sapt instanceof RelaySAPoint) {
 			return SEP+((RelaySAPoint)sapt).getRelay().getName();
 		}
 		else if (sapt instanceof RefSAPoint) {
 			RefSAPoint rpt = (RefSAPoint) sapt;
 			return rpt.getRef().getName()+SEP;
 		}
 		assert(false): "unexpected sub type";
 		return null;
 	}
 
 	private String getName(SPPoint sppt) {
 		return sppt.getRef().getName()+SEP+sppt.getService().getName();
 	}
 	
 }
