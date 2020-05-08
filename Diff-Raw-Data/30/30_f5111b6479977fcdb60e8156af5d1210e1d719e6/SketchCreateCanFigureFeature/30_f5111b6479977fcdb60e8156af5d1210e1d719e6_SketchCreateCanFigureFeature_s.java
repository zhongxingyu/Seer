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
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.testtool.sketch.SketchGraphicsAlgorithmRendererFactory;
 
 /**
  * The Class SketchCreateCanFigureFeature.
  */
 public class SketchCreateCanFigureFeature extends AbstractCreateFeature {
 
 	/**
 	 * Instantiates a new sketch create can figure feature.
 	 * 
 	 * @param fp
 	 *            the fp
 	 * @param name
 	 *            the name
 	 * @param description
 	 *            the description
 	 */
 	public SketchCreateCanFigureFeature(IFeatureProvider fp, String name, String description) {
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
 		AnchorContainer newAc = Graphiti.getPeCreateService().createContainerShape(targetContainer, true);
 		Graphiti.getPeCreateService().createChopboxAnchor(newAc);
 
 		int width = context.getWidth() < 50 ? 50 : context.getWidth();
 		int height = context.getHeight() < 50 ? 50 : context.getHeight();
 
 		GraphicsAlgorithm newGa = Graphiti.getGaCreateService().createPlatformGraphicsAlgorithm(newAc,
 				SketchGraphicsAlgorithmRendererFactory.CANFIGURE);
 		Graphiti.getGaService().setLocationAndSize(newGa, context.getX(), context.getY(), width, height);
 		layoutPictogramElement(newAc);
 
 		return new Object[] { newAc };
 	}
 }
