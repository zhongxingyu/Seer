 /*******************************************************************************
  * Copyright (c) 2011 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.eclipse.etrice.ui.structure.support;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.etrice.core.room.ActorContainerRef;
 import org.eclipse.etrice.core.room.Binding;
 import org.eclipse.etrice.core.room.BindingEndPoint;
 import org.eclipse.etrice.core.room.InterfaceItem;
 import org.eclipse.etrice.core.room.LayerConnection;
 import org.eclipse.etrice.core.room.Port;
 import org.eclipse.etrice.core.room.RefSAPoint;
 import org.eclipse.etrice.core.room.RelaySAPoint;
 import org.eclipse.etrice.core.room.SAPoint;
 import org.eclipse.etrice.core.room.SPPRef;
 import org.eclipse.etrice.core.room.SPPoint;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
 import org.eclipse.graphiti.features.context.impl.AddContext;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 
 /**
  * description
  *
  * @author Henrik Rentz-Reichert initial contribution and API
  *
  */
 public class SupportUtil {
 
 	public static final String SEP = "#";
 	
 	public static EObject getOwnObject(EObject obj, ResourceSet rs) {
 		URI uri = EcoreUtil.getURI(obj);
 		EObject own = rs.getEObject(uri, true);
 		assert(own!=null): "own object must exist";
 		return own;
 	}
 	
 	public static ContainerShape addItem(EObject obj, int x, int y, ContainerShape container, IFeatureProvider fp) {
 		return addItem(obj, x, y, container, null, fp);
 	}
 	
 	public static ContainerShape addItem(EObject obj, int x, int y, ContainerShape container, Map<String,Anchor> ifitem2anchor, IFeatureProvider fp) {
 		AddContext addContext = new AddContext();
 
 		addContext.setNewObject(obj);
 		addContext.setTargetContainer(container);
 		addContext.setX(x);
 		addContext.setY(y);
 		ContainerShape newShape = (ContainerShape) fp.addIfPossible(addContext);
 		assert(newShape!=null): "shape creation must succeed";
 		
 		if (ifitem2anchor!=null) {
 			if (obj instanceof ActorContainerRef)
				getAnchors((ActorContainerRef)obj, newShape, ifitem2anchor);
 			else if (obj instanceof InterfaceItem) {
 				assert(!newShape.getAnchors().isEmpty()): "interface item must have an anchor";
 				ifitem2anchor.put(SEP+((InterfaceItem)obj).getName(), newShape.getAnchors().get(0));
 			}
 		}
 		
 		return newShape;
 	}
 
 	public static void getAnchors(ActorContainerRef acr, PictogramElement refShape,
 			final Map<String, Anchor> ifitem2anchor) {
 		
 		if (refShape instanceof ContainerShape) {
 			ifitem2anchor.put(acr.getName()+SEP, ((ContainerShape)refShape).getAnchors().get(0));
 			for (Shape child : ((ContainerShape) refShape).getChildren()) {
 				if (child instanceof ContainerShape) {
 					ContainerShape childShape = (ContainerShape) child;
 					if (!childShape.getAnchors().isEmpty()) {
 						if (!childShape.getLink().getBusinessObjects().isEmpty()) {
 							EObject obj = childShape.getLink().getBusinessObjects().get(0);
 							if (obj instanceof Port) {
 								ifitem2anchor.put(acr.getName()+SEP+((Port)obj).getName(), childShape.getAnchors().get(0));
 							}
 							if (obj instanceof SPPRef) {
 								ifitem2anchor.put(acr.getName()+SEP+((SPPRef)obj).getName(), childShape.getAnchors().get(0));
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public static void addRefItem(ActorContainerRef obj, ContainerShape acShape, int x, int y, IFeatureProvider featureProvider, final Map<String, Anchor> ifitem2anchor) {
 		AddContext addContext = new AddContext();
 		addContext.setNewObject(obj);
 		addContext.setTargetContainer(acShape);
 		addContext.setX(x);
 		addContext.setY(y);
 		
 		ContainerShape refShape = (ContainerShape) featureProvider.addIfPossible(addContext);
 		
 		SupportUtil.getAnchors(obj, refShape, ifitem2anchor);
 	}
 
 	public static void addInterfaceItem(InterfaceItem item, ContainerShape acShape, int pos, IFeatureProvider featureProvider, final Map<String, Anchor> ifitem2anchor) {
 		AddContext addContext = new AddContext();
 		addContext.setNewObject(item);
 		addContext.setTargetContainer(acShape);
 		addContext.setX(pos);
 		addContext.setY(2*StructureClassSupport.MARGIN);
 		
 		ContainerShape pe = (ContainerShape) featureProvider.addIfPossible(addContext);
 		assert(!pe.getAnchors().isEmpty()): "interface item should have an anchor";
 		ifitem2anchor.put(SEP+item.getName(), pe.getAnchors().get(0));
 	}
 
 	public static void addBinding(Binding bind, IFeatureProvider featureProvider,
 			final Map<String, Anchor> ifitem2anchor) {
 		String ep1 = getName(bind.getEndpoint1());
 		String ep2 = getName(bind.getEndpoint2());
 		Anchor a1 = ifitem2anchor.get(ep1);
 		Anchor a2 = ifitem2anchor.get(ep2);
 		assert(a1!=null && a2!=null): "ports for binding must be present";
 		
 		AddConnectionContext context = new AddConnectionContext(a1, a2);
 		context.setNewObject(bind);
 		featureProvider.addIfPossible(context);
 	}
 
 	public static void addLayerConnection(LayerConnection lc, IFeatureProvider featureProvider,
 			final Map<String, Anchor> ifitem2anchor) {
 		String ep1 = getName(lc.getFrom());
 		String ep2 = getName(lc.getTo());
 		Anchor a1 = ifitem2anchor.get(ep1);
 		Anchor a2 = ifitem2anchor.get(ep2);
 		assert(a1!=null && a2!=null): "ports for layer connection must be present";
 		
 		AddConnectionContext context = new AddConnectionContext(a1, a2);
 		context.setNewObject(lc);
 		featureProvider.addIfPossible(context);
 	}
 
 	public static String getName(BindingEndPoint ep) {
 		String ar = ep.getActorRef()==null? "":ep.getActorRef().getName();
 		String p = ep.getPort().getName();
 		return ar+SEP+p;
 	}
 
 	public static String getName(SAPoint sapt) {
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
 
 	public static String getName(SPPoint sppt) {
 		return sppt.getRef().getName()+SEP+sppt.getService().getName();
 	}
 
 	public static List<InterfaceItem> getInterfaceItems(ContainerShape shape, IFeatureProvider fp) {
 		List<InterfaceItem> items = new ArrayList<InterfaceItem>();
 		for (Shape ch : shape.getChildren()) {
 			Object bo = fp.getBusinessObjectForPictogramElement(ch);
 			if (bo instanceof InterfaceItem)
 				items.add((InterfaceItem)bo);
 		}
 		return items;
 	}
 
 	public static List<ActorContainerRef> getRefs(ContainerShape shape, IFeatureProvider fp) {
 		List<ActorContainerRef> refs = new ArrayList<ActorContainerRef>();
 		for (Shape ch : shape.getChildren()) {
 			Object bo = fp.getBusinessObjectForPictogramElement(ch);
 			if (bo instanceof ActorContainerRef)
 				refs.add((ActorContainerRef)bo);
 		}
 		return refs;
 	}
 
 	public static List<Binding> getBindings(Diagram diag, IFeatureProvider fp) {
 		List<Binding> bindings = new ArrayList<Binding>();
 		
 		for (Connection conn : diag.getConnections()) {
 			Object bo = fp.getBusinessObjectForPictogramElement(conn);
 			if (bo instanceof Binding)
 				bindings.add((Binding)bo);
 		}
 		return bindings;
 	}
 
 	public static List<LayerConnection> getConnections(Diagram diag, IFeatureProvider fp) {
 		List<LayerConnection> bindings = new ArrayList<LayerConnection>();
 		
 		for (Connection conn : diag.getConnections()) {
 			Object bo = fp.getBusinessObjectForPictogramElement(conn);
 			if (bo instanceof LayerConnection)
 				bindings.add((LayerConnection)bo);
 		}
 		return bindings;
 	}
 }
