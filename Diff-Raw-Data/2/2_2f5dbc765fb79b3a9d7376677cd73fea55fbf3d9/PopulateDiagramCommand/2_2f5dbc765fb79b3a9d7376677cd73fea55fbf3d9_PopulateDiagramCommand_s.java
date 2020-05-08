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
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.emf.transaction.RecordingCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.ActorContainerClass;
 import org.eclipse.etrice.core.room.Binding;
 import org.eclipse.etrice.core.room.InterfaceItem;
 import org.eclipse.etrice.core.room.LayerConnection;
 import org.eclipse.etrice.core.room.LogicalSystem;
 import org.eclipse.etrice.core.room.StructureClass;
 import org.eclipse.etrice.core.room.SubSystemClass;
import org.eclipse.etrice.ui.behavior.DiagramTypeProvider;
 import org.eclipse.etrice.ui.structure.support.StructureClassSupport;
 import org.eclipse.etrice.ui.structure.support.SupportUtil;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.impl.AddContext;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.ui.services.GraphitiUi;
 
 public class PopulateDiagramCommand extends RecordingCommand {
 
 	private StructureClass sc;
 	private Diagram diagram;
 
 	public PopulateDiagramCommand(Diagram diag, StructureClass sc, TransactionalEditingDomain domain) {
 		super(domain);
 		this.diagram = diag;
 		this.sc = sc;
 	}
 
 	@Override
 	protected void doExecute() {
 		IDiagramTypeProvider dtp = GraphitiUi.getExtensionManager().createDiagramTypeProvider(diagram, DiagramTypeProvider.PROVIDER_ID); //$NON-NLS-1$
 		IFeatureProvider featureProvider = dtp.getFeatureProvider();
 		
 		AddContext addContext = new AddContext();
 		addContext.setNewObject(sc);
 		addContext.setTargetContainer(diagram);
 		addContext.setX(StructureClassSupport.MARGIN);
 		addContext.setY(StructureClassSupport.MARGIN);
 
 		HashMap<String, Anchor> ifitem2anchor = new HashMap<String, Anchor>();
 		
 		IAddFeature addFeature = featureProvider.getAddFeature(addContext);
 		if (addFeature!=null && addFeature.canAdd(addContext)) {
 			ContainerShape acShape = (ContainerShape) addFeature.add(addContext);
 
 			int width = acShape.getGraphicsAlgorithm().getGraphicsAlgorithmChildren().get(0).getWidth();
 			
 			if (sc instanceof ActorClass) {
 				List<InterfaceItem> items = new ArrayList<InterfaceItem>();
 				items.addAll(((ActorClass) sc).getIfPorts());
 				items.addAll(((ActorClass) sc).getIfSPPs());
 				SupportUtil.addInterfaceItems(items, 0, acShape, width, featureProvider, ifitem2anchor);
 
 				SupportUtil.addInterfaceItems(((ActorClass)sc).getIntPorts(), 3*StructureClassSupport.MARGIN, acShape, width, featureProvider, ifitem2anchor);
 			}
 			else if (sc instanceof SubSystemClass) {
 				List<InterfaceItem> items = new ArrayList<InterfaceItem>();
 				items.addAll(((SubSystemClass) sc).getRelayPorts());
 				items.addAll(((SubSystemClass) sc).getIfSPPs());
 				SupportUtil.addInterfaceItems(items, 0, acShape, width, featureProvider, ifitem2anchor);
 			}
 			
 			// actor container references
 			if (sc instanceof ActorContainerClass) {
 				ActorContainerClass acc = (ActorContainerClass) sc;
 	        	SupportUtil.addRefItems(acc.getActorRefs(), acShape, width, featureProvider, ifitem2anchor);
 			}
 			else if (sc instanceof LogicalSystem) {
 				LogicalSystem sys = (LogicalSystem) sc;
 	        	SupportUtil.addRefItems(sys.getSubSystems(), acShape, width, featureProvider, ifitem2anchor);
 			}
 			
 			// base class items
 			if (sc instanceof ActorClass) {
 				ActorClass base = ((ActorClass) sc).getBase();
 				
 				// add inherited ports and refs and bindings (and preserve layout)
 				if (base!=null)
 					StructureClassSupport.addInheritedItems(base, acShape, ifitem2anchor, featureProvider);
 			}
 			
 			// layer connections
 			for (LayerConnection lc : sc.getConnections()) {
 				SupportUtil.addLayerConnection(lc, featureProvider, ifitem2anchor);
 			}
 			
 			// bindings
 			for (Binding bind : sc.getBindings()) {
 				SupportUtil.addBinding(bind, featureProvider, ifitem2anchor);
 			}
 		}
 		
 	}
 	
 }
