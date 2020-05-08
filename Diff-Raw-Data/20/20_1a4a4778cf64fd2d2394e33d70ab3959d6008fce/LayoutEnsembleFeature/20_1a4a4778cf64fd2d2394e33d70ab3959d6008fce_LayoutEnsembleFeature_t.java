 /*
  * License (BSD Style License):
  * Copyright (c) 2012
  * Software Engineering
  * Department of Computer Science
  * Technische Universitiät Darmstadt
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * - Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * - Neither the name of the Software Engineering Group or Technische
  * Universität Darmstadt nor the names of its contributors may be used to
  * endorse or promote products derived from this software without specific
  * prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package de.opalproject.vespucci.sliceEditor.features;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.graphiti.datatypes.IDimension;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.ILayoutContext;
 import org.eclipse.graphiti.features.impl.AbstractLayoutFeature;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Image;
 import org.eclipse.graphiti.mm.algorithms.Polyline;
 import org.eclipse.graphiti.mm.algorithms.styles.Point;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 
 import de.opalproject.vespucci.datamodel.Ensemble;
 
 /**
  * This feature take adjusts the layout while resizing
  * 
  * @author Lars
  * 
  */
 public class LayoutEnsembleFeature extends AbstractLayoutFeature {
 
 	private static final int MIN_HEIGHT = 30;
 
 	private static final int MIN_WIDTH = 50;
 
 	public LayoutEnsembleFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.ILayout#canLayout(org.eclipse.graphiti.features
 	 * .context.ILayoutContext)
 	 */
 	@Override
 	public boolean canLayout(ILayoutContext context) {
 		// return true, if pictogram element is linked to an Ensemble
 		PictogramElement pe = context.getPictogramElement();
 		if (!(pe instanceof ContainerShape))
 			return false;
 		EList<EObject> businessObjects = pe.getLink().getBusinessObjects();
 		return businessObjects.size() == 1
 				&& businessObjects.get(0) instanceof Ensemble;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.ILayout#layout(org.eclipse.graphiti.features
 	 * .context.ILayoutContext)
 	 */
 	@Override
 	public boolean layout(ILayoutContext context) {
 		boolean anythingChanged = false;
 		ContainerShape containerShape = (ContainerShape) context
 				.getPictogramElement();
 		GraphicsAlgorithm containerGa = containerShape.getGraphicsAlgorithm();
 
 		// height
 		if (containerGa.getHeight() < MIN_HEIGHT) {
 			containerGa.setHeight(MIN_HEIGHT);
 			anythingChanged = true;
 		}
 
 		// width
 		if (containerGa.getWidth() < MIN_WIDTH) {
 			containerGa.setWidth(MIN_WIDTH);
 			anythingChanged = true;
 		}
 
 		int containerWidth = containerGa.getWidth();
 
 		for (Shape shape : containerShape.getChildren()) {
 			GraphicsAlgorithm graphicsAlgorithm = shape.getGraphicsAlgorithm();
 			IGaService gaService = Graphiti.getGaService();
 			IDimension size = gaService.calculateSize(graphicsAlgorithm);
 			if (containerWidth != size.getWidth()) {
 				if (graphicsAlgorithm instanceof Polyline) {
 					Polyline polyline = (Polyline) graphicsAlgorithm;
 					Point secondPoint = polyline.getPoints().get(1);
 					Point newSecondPoint = gaService.createPoint(
 							containerWidth, secondPoint.getY());
 					polyline.getPoints().set(1, newSecondPoint);
 					anythingChanged = true;
				} 
				else if (graphicsAlgorithm instanceof Image) {
					Image icon = (Image) graphicsAlgorithm;
					icon.setX(2);
					anythingChanged = true;
				}
				else {
 					gaService.setWidth(graphicsAlgorithm, containerWidth);
 					anythingChanged = true;
 				}
 			}
 		}
 		return anythingChanged;
 	}
 }
