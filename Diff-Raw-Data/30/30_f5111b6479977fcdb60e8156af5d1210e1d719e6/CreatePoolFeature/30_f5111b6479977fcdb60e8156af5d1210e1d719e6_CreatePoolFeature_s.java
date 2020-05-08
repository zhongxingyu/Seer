 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.testtool.sketch.features.pool;
 
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeService;
 import org.eclipse.graphiti.testtool.sketch.SketchUtil;
 import org.eclipse.graphiti.util.IColorConstant;
 
 /**
  * The Class CreatePoolFeature.
  */
 public class CreatePoolFeature extends AbstractCreateFeature {
 
 	/**
 	 * Instantiates a new creates the pool feature.
 	 * 
 	 * @param fp
 	 *            the fp
 	 * @param name
 	 *            the name
 	 * @param description
 	 *            the description
 	 */
 	public CreatePoolFeature(IFeatureProvider fp, String name, String description) {
 		super(fp, name, description);
 	}
 
 	public boolean canCreate(ICreateContext context) {
 		boolean ret = false;
 		ContainerShape targetContainer = context.getTargetContainer();
 		if (targetContainer != null) {
 			ret = true;
 		}
 		return ret;
 	}
 
 	public Object[] create(ICreateContext context) {
 		ContainerShape targetContainer = context.getTargetContainer();
 		int width = context.getWidth() < 50 ? 50 : context.getWidth();
 		int height = context.getHeight() < 50 ? 50 : context.getHeight();
 
 		// create pool
 		final IPeService peService = Graphiti.getPeService();
 		IGaService gaService = Graphiti.getGaService();
 		ContainerShape poolPe = peService.createContainerShape(targetContainer, true);
 		peService.setPropertyValue(poolPe, SketchUtil.POOL_TAG, "");
 		Diagram diagram = getDiagram();
 		{
 			peService.createChopboxAnchor(poolPe);
 			GraphicsAlgorithm poolGa = gaService.createRectangle(poolPe);
 			gaService.setLocationAndSize(poolGa, context.getX(), context.getY(), width, height);
 			poolGa.setBackground(gaService.manageColor(diagram, IColorConstant.RED));
 			poolGa.setForeground(gaService.manageColor(diagram, IColorConstant.DARK_GRAY));
 		}
 
 		// create lane
 		CreateLaneFeauture.createLaneInPool(poolPe, diagram);
 
 		layoutPictogramElement(poolPe);
 
 		return new Object[] { poolPe };
 	}
 
 }
