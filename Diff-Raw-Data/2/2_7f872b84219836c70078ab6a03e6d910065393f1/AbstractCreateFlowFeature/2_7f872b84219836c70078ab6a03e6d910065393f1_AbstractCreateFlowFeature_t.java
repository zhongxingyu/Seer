 /******************************************************************************* 
  * Copyright (c) 2011, 2012 Red Hat, Inc. 
  *  All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  *
  * @author Ivar Meikas
  ******************************************************************************/
 package org.eclipse.bpmn2.modeler.core.features.flow;
 
 import org.eclipse.bpmn2.BaseElement;
 import org.eclipse.bpmn2.EndEvent;
 import org.eclipse.bpmn2.InteractionNode;
 import org.eclipse.bpmn2.Participant;
 import org.eclipse.bpmn2.modeler.core.features.AbstractBpmn2CreateConnectionFeature;
 import org.eclipse.bpmn2.modeler.core.runtime.ModelEnablementDescriptor;
 import org.eclipse.bpmn2.modeler.core.runtime.TargetRuntime;
 import org.eclipse.bpmn2.modeler.core.utils.AnchorUtil;
 import org.eclipse.bpmn2.modeler.core.utils.BusinessObjectUtil;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.graphiti.datatypes.ILocation;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.ICreateConnectionContext;
 import org.eclipse.graphiti.features.context.IReconnectionContext;
 import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
 import org.eclipse.graphiti.mm.algorithms.styles.Point;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeService;
 
 public abstract class AbstractCreateFlowFeature<
 		CONNECTION extends BaseElement,
 		SOURCE extends EObject,
 		TARGET extends EObject>
 	extends AbstractBpmn2CreateConnectionFeature<CONNECTION> {
 
 	protected boolean changesDone = false;
 	
 	public AbstractCreateFlowFeature(IFeatureProvider fp, String name, String description) {
 		super(fp, name, description);
 	}
 
 	@Override
 	public boolean canCreate(ICreateConnectionContext context) {
 		SOURCE source = getSourceBo(context);
 		TARGET target = getTargetBo(context);
 		return source != null && target != null;
 	}
 
 	@Override
 	public Connection create(ICreateConnectionContext context) {
 		CONNECTION bo = createBusinessObject(context);
 		AddConnectionContext addContext = new AddConnectionContext(
 				context.getSourceAnchor(),
 				context.getTargetAnchor());
 		addContext.setNewObject(bo);
 
 		IPeService peService = Graphiti.getPeService();
 		IGaService gaService = Graphiti.getGaService();
 		ILocation loc, shapeLoc;
 		
 		// the CreateConnectionContext contains the source and target locations - the actual
 		// mouse locations where the connection was started and ended. These locations must
 		// be passed to the AddConnectionContext so they can be added (as String properties)
 		// to the Connection once it is created. These String properties are then decoded in
 		// AnchorUtil.getSourceAndTargetBoundaryAnchors() to create Ad Hoc anchors if necessary.
 		loc = context.getSourceLocation();
 		if (loc==null)
 			loc = peService.getLocationRelativeToDiagram(context.getSourceAnchor());
 		shapeLoc = peService.getLocationRelativeToDiagram((Shape)context.getSourceAnchor().getParent());
 		Point p = gaService.createPoint(
 				loc.getX() - shapeLoc.getX(),
 				loc.getY() - shapeLoc.getY());
 		addContext.putProperty(AnchorUtil.CONNECTION_SOURCE_LOCATION, p);
 		
 		loc = context.getTargetLocation();
		if (loc==null)
			loc = peService.getLocationRelativeToDiagram(context.getTargetAnchor());
 		shapeLoc = peService.getLocationRelativeToDiagram((Shape)context.getTargetAnchor().getParent());
 		p = gaService.createPoint(
 				loc.getX() - shapeLoc.getX(),
 				loc.getY() - shapeLoc.getY());
 		addContext.putProperty(AnchorUtil.CONNECTION_TARGET_LOCATION, p);
 		addContext.putProperty(AnchorUtil.CONNECTION_CREATED, Boolean.TRUE);
 
 		Connection connection = (Connection) getFeatureProvider().addIfPossible(addContext);
 		ModelUtil.setID(bo);
 
 		changesDone = true;
 		return connection;
 	}
 
 	@Override
 	public boolean canStartConnection(ICreateConnectionContext context) {
 		return getSourceBo(context) != null;
 	}
 
 	protected abstract String getStencilImageId();
 
 	@Override
 	public String getCreateImageId() {
 		return getStencilImageId();
 	}
 
 	@Override
 	public String getCreateLargeImageId() {
 		return getStencilImageId();
 	}
 
 	protected SOURCE getSourceBo(ICreateConnectionContext context) {
 		if (context.getSourceAnchor() != null) {
 			return BusinessObjectUtil.getFirstElementOfType(context.getSourceAnchor().getParent(), getSourceClass());
 		}
 		return null;
 	}
 
 	protected TARGET getTargetBo(ICreateConnectionContext context) {
 		if (context.getTargetAnchor() != null) {
 			return BusinessObjectUtil.getFirstElementOfType(context.getTargetAnchor().getParent(), getTargetClass());
 		}
 		return null;
 	}
 
 	protected abstract Class<SOURCE> getSourceClass();
 
 	protected abstract Class<TARGET> getTargetClass();
 
 	@Override
 	public boolean hasDoneChanges() {
 		return changesDone;
 	}
 }
