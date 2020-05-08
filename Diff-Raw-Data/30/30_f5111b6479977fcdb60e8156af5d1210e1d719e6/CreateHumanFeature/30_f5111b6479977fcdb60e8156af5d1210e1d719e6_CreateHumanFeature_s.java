 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    cbrand - Bug 378341 - Enlargement of the Line Width of Shapes isn't calculated based on the center.
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.testtool.sketch.features.create;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
 import org.eclipse.graphiti.mm.algorithms.Ellipse;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Rectangle;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeCreateService;
 import org.eclipse.graphiti.util.IColorConstant;
 
 /**
  * The Class CreateCsGhostAndInnerShapeFeature.
  */
 public class CreateHumanFeature extends AbstractCreateFeature {
 	private static final int LINE_WIDTH = 1;
 	static final int UNIT = 5;
 	static final int BORDER_X = UNIT * 1;
 	static final int BORDER_Y = BORDER_X;
 
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
 	public CreateHumanFeature(IFeatureProvider fp, String name, String description) {
 		super(fp, name, description);
 	}
 
 	public boolean canCreate(ICreateContext context) {
 		return (context.getTargetContainer() != null);
 	}
 
 	public Object[] create(ICreateContext context) {
 		ContainerShape targetContainer = context.getTargetContainer();
 
 		final IPeCreateService peCreateService = Graphiti.getPeCreateService();
 		final IGaService gaService = Graphiti.getGaService();
 
 		Shape ret = peCreateService.createShape(targetContainer, true);
 		Rectangle ghostRect = gaService.createInvisibleRectangle(ret);
 		// gaService.setLocationAndSize(ghostRect, 0, 0, 2 * BORDER_X + 8 *
 		// UNIT, 2 * BORDER_Y + 14 * UNIT);
 		gaService
 				.setLocationAndSize(ghostRect, context.getX(), context.getY(), context.getWidth(), context.getHeight());
 
 		//RoundedRectangle roundedRectangle = gaService.createRoundedRectangle(ghostRect, 10, 10);
 		// roundedRectangle.setBackground(manageColor(ColorConstant.GREEN));
 		// Shape innerShape = peCreateService.createShape(ret, true);
 		// peCreateService.createChopboxAnchor(innerShape);
 
 		GraphicsAlgorithm mansParent = ghostRect;
 		
 		// man's head
 		Ellipse head = gaService.createEllipse(mansParent);
 		gaService.setLocationAndSize(head, BORDER_X + 2 * UNIT, BORDER_Y, 4 * UNIT, 4 * UNIT);
 		// head.setBackground(manageColor(ColorConstant.RED));
 		head.setFilled(false);
 		
 		// man's body
 		gaService.createPolyline(mansParent, new int[] { BORDER_X + 4 * UNIT, BORDER_Y + 4 * UNIT,
 				BORDER_X + 4 * UNIT, BORDER_Y + 10 * UNIT });
 
 		// man's arms
 		gaService.createPolyline(mansParent, new int[] { BORDER_X, BORDER_Y + 4 * UNIT, BORDER_X + 4 * UNIT,
 				BORDER_Y + 6 * UNIT });
 		gaService.createPolyline(mansParent, new int[] { BORDER_X + 8 * UNIT, BORDER_Y + 4 * UNIT,
 				BORDER_X + 4 * UNIT, BORDER_Y + 6 * UNIT });
 		
 		// man's leggs
 		gaService.createPolyline(mansParent, new int[] { BORDER_X + 4 * UNIT, BORDER_Y + 10 * UNIT,
 				BORDER_X, BORDER_Y + 14 * UNIT });
 		gaService.createPolyline(mansParent, new int[] { BORDER_X + 4 * UNIT, BORDER_Y + 10 * UNIT,
 				BORDER_X + 8 * UNIT, BORDER_Y + 14 * UNIT });
 
 		configureMan(mansParent);
 
 		return new Object[] { ret };
 	}
 
 	private void configureMan(GraphicsAlgorithm mansParent) {
 		mansParent.setLineWidth(LINE_WIDTH);
 		EList<GraphicsAlgorithm> gaChildren = mansParent.getGraphicsAlgorithmChildren();
 		for (GraphicsAlgorithm ga : gaChildren) {
 			ga.setLineWidth(LINE_WIDTH);
 			ga.setForeground(manageColor(IColorConstant.BLACK));
 		}
 	}
 }
