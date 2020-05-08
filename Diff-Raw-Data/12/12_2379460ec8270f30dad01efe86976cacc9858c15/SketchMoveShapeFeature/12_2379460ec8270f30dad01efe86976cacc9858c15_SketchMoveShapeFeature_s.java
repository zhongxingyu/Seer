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
 package org.eclipse.graphiti.testtool.sketch.features;
 
 import java.util.Collection;
 
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Polyline;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 
 /**
  * The Class SketchMoveShapeFeature.
  */
 public class SketchMoveShapeFeature extends DefaultMoveShapeFeature {
 
 	/**
 	 * Instantiates a new sketch move shape feature.
 	 * 
 	 * @param fp
 	 *            the fp
 	 */
 	public SketchMoveShapeFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	@Override
 	public boolean canMoveShape(IMoveShapeContext context) {
 		return true;
 	}
 
 	@Override
 	protected void internalMove(IMoveShapeContext context) {
 
 		super.internalMove(context);
 
 		Connection targetConnection = context.getTargetConnection();
 		if (targetConnection != null) {
 			if (context.getSourceContainer().equals(context.getTargetContainer())) {
 
 				Shape shape = context.getShape();
 				GraphicsAlgorithm ga = shape.getGraphicsAlgorithm();
 
 				Graphiti.getGaService().setLocation(ga, context.getX() - (ga.getWidth() / 2), context.getY() - (ga.getHeight() / 2));
 
 				Collection<Anchor> anchors = shape.getAnchors();
 				if (anchors.size() > 0) {
 					Anchor newAnchor = anchors.iterator().next();
 
 					Anchor oldEndAnchor = targetConnection.getEnd();
 					targetConnection.setEnd(newAnchor);
 
 					Connection connection = Graphiti.getPeCreateService().createFreeFormConnection(getDiagram());
 					Polyline p = Graphiti.getGaCreateService().createPolyline(connection);
 
 					GraphicsAlgorithm targetConnectionGraphicsAlgorithm = targetConnection.getGraphicsAlgorithm();
 					p.setLineWidth(targetConnectionGraphicsAlgorithm.getLineWidth());
 					p.setForeground(targetConnectionGraphicsAlgorithm.getForeground());
 					p.setLineStyle(targetConnectionGraphicsAlgorithm.getLineStyle());
 
 					connection.setStart(newAnchor);
 					connection.setEnd(oldEndAnchor);
 				}
 			}
 		}
 	}
 
 }
