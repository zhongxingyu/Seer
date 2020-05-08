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
 package org.eclipse.graphiti.testtool.sketch.features.create;
 
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
 import org.eclipse.graphiti.mm.algorithms.Ellipse;
 import org.eclipse.graphiti.mm.algorithms.Rectangle;
 import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeCreateService;
 import org.eclipse.graphiti.util.ColorConstant;
 
 /**
  * The Class CreateCsGhostAndInnerShapeFeature.
  */
 public class CreateCsGhostAndInnerShapeFeature extends AbstractCreateFeature {
 
 	/**
 	 * Instantiates a new creates the cs ghost and inner shape feature.
 	 * 
 	 * @param fp
 	 *            the fp
 	 * @param name
 	 *            the name
 	 * @param description
 	 *            the description
 	 */
 	public CreateCsGhostAndInnerShapeFeature(IFeatureProvider fp, String name, String description) {
 		super(fp, name, description);
 	}
 
 	public boolean canCreate(ICreateContext context) {
 		return (context.getTargetContainer() != null);
 	}
 
 	public Object[] create(ICreateContext context) {
 		ContainerShape targetContainer = context.getTargetContainer();
 		final IPeCreateService peCreateService = Graphiti.getPeCreateService();
 		IGaService gaService = Graphiti.getGaService();
 		ContainerShape ret = peCreateService.createContainerShape(targetContainer, true);
 		peCreateService.createChopboxAnchor(ret);
 		Rectangle ghostRect = gaService.createInvisibleRectangle(ret);
 		RoundedRectangle roundedRectangle = gaService.createRoundedRectangle(ghostRect, 10, 10);
 		roundedRectangle.setBackground(manageColor(ColorConstant.GREEN));
 		Shape innerShape = peCreateService.createShape(ret, true);
 		peCreateService.createChopboxAnchor(innerShape);
 		Ellipse ellipse = gaService.createEllipse(innerShape);
 		ellipse.setBackground(manageColor(ColorConstant.RED));
 
 		gaService.setLocationAndSize(ghostRect, context.getX(), context.getY(), context.getWidth(), context.getHeight(), true);
 
 		layoutPictogramElement(ret);
 
 		return new Object[] { ret };
 	}
 }
