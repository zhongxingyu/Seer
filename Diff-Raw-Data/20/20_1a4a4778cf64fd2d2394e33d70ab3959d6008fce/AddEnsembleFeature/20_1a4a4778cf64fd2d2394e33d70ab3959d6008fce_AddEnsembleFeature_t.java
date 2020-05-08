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
 
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.impl.AbstractAddShapeFeature;
 import org.eclipse.graphiti.mm.algorithms.Image;
 import org.eclipse.graphiti.mm.algorithms.MultiText;
 import org.eclipse.graphiti.mm.algorithms.Polyline;
 import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeCreateService;
 import org.eclipse.graphiti.util.ColorConstant;
 import org.eclipse.graphiti.util.IColorConstant;
 import de.opalproject.vespucci.datamodel.Ensemble;
 
 /**
  * This feature allows to drag an ensemble from the Navigator into the slice
  * editor
  * 
  * @author Lars
  * @author Marius
  * 
  */
 public class AddEnsembleFeature extends AbstractAddShapeFeature {
 
 	public AddEnsembleFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	/*
 	 * checks if the dragged element is an ensemble and therefore can be added
 	 * to the slice editor
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.IAdd#canAdd(org.eclipse.graphiti.features.context
 	 * .IAddContext)
 	 */
 	@Override
 	public boolean canAdd(IAddContext context) {
 		// check if user wants to add an Ensemble
 		if (context.getNewObject() instanceof Ensemble) {
 			// check if user wants to add to a diagram
 			if (context.getTargetContainer() instanceof Diagram) {
 				// check if the pictogram element is already existing
 				// TODO skip for empty ensemble types once implemented
 				if (Graphiti
 						.getLinkService()
 						.getPictogramElements(
 								(Diagram) context.getTargetContainer(),
 								(Ensemble) context.getNewObject()).size() == 0) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * execute the add
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.IAdd#add(org.eclipse.graphiti.features.context
 	 * .IAddContext)
 	 */
 	@Override
 	public PictogramElement add(IAddContext context) {
 		Ensemble addedEnsemble = (Ensemble) context.getNewObject();
 		Diagram targetDiagram = (Diagram) context.getTargetContainer();
 
 		// sets the Layout - normal or empty ensemble
 		IColorConstant Ensemble_TEXT_FOREGROUND;
 		IColorConstant Ensemble_FOREGROUND;
 		IColorConstant Ensemble_BACKGROUND;
 
 		if (addedEnsemble.getName() == "Empty Ensemble") {
 			Ensemble_TEXT_FOREGROUND = new ColorConstant(176, 176, 176);
 
 			Ensemble_FOREGROUND = new ColorConstant(48, 48, 48);
 
 			Ensemble_BACKGROUND = new ColorConstant(176, 176, 176);
 		} else {
 			Ensemble_TEXT_FOREGROUND = ColorConstant.BLACK;
 
 			Ensemble_FOREGROUND = new ColorConstant(98, 131, 167);
 
 			Ensemble_BACKGROUND = new ColorConstant(187, 218, 247);
 		}
 
 		// CONTAINER SHAPE WITH ROUNDED RECTANGLE
 		IPeCreateService peCreateService = Graphiti.getPeCreateService();
 		ContainerShape containerShape = peCreateService.createContainerShape(
 				targetDiagram, true);
 
 		// check whether the context has a size (e.g. from a create feature)
 		// otherwise define a default size for the shape
 		final int width = context.getWidth() <= 0 ? 100 : context.getWidth();
 		final int height = context.getHeight() <= 0 ? 50 : context.getHeight();
 
 		IGaService gaService = Graphiti.getGaService();
 		RoundedRectangle roundedRectangle; // need to access it later
 
 		{
 			// create and set graphics algorithm
 			roundedRectangle = gaService.createRoundedRectangle(containerShape,
 					5, 5);
 
 			roundedRectangle.setForeground(manageColor(Ensemble_FOREGROUND));
 			roundedRectangle.setBackground(manageColor(Ensemble_BACKGROUND));
 			roundedRectangle.setLineWidth(2);
 			gaService.setLocationAndSize(roundedRectangle, context.getX(),
 					context.getY(), width, height);
 
 			// if added Ensemble has no resource we add it to the resource
 			// of the diagram
 			if (addedEnsemble.eResource() == null) {
 				getDiagram().eResource().getContents().add(addedEnsemble);
 			}
 			// create link and wire it
 			link(containerShape, addedEnsemble);
 		}
 
 		// SHAPE WITH LINE
 		if (!(addedEnsemble.getName() == "Empty Ensemble")) {
 			// create shape for line
 			Shape lineShape = peCreateService
 					.createShape(containerShape, false);
 
 			// create and set graphics algorithm
 			Polyline polyline = gaService.createPolyline(lineShape, new int[] {
 					0, 20, width, 20 });
 			polyline.setForeground(manageColor(Ensemble_FOREGROUND));
 			polyline.setLineWidth(2);
 		}
 
 		// SHAPE WITH TEXT
 		{
 			// create shape for name
 			Shape nameShape = peCreateService
 					.createShape(containerShape, false);
 
 			// sets the name of the ensemble
 			Text name = gaService
 					.createText(nameShape, addedEnsemble.getName());
 			name.setForeground(manageColor(Ensemble_TEXT_FOREGROUND));
 			name.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
 
 			// vertical alignment has as default value "center"
 			name.setFont(gaService.manageDefaultFont(getDiagram(), false, true));
 			gaService.setLocationAndSize(name, 0, 0, width, 20);
 
 			// create shape for the description
 			Shape descriptionShape = peCreateService.createShape(
 					containerShape, false);
 
 			// sets the description of the ensemble
 			MultiText description = gaService.createMultiText(descriptionShape,
 					addedEnsemble.getDescription());
 			description.setLineWidth(width);
 			description.setHorizontalAlignment(Orientation.ALIGNMENT_LEFT);
 			description.setVerticalAlignment(Orientation.ALIGNMENT_TOP);
 			description.setFont(gaService.manageDefaultFont(getDiagram(),
 					false, true));
 			gaService
 					.setLocationAndSize(description, 2, 20, width, height - 20);
 			// create link and wire it
 			link(nameShape, addedEnsemble);
 			link(descriptionShape, addedEnsemble);
 
 			// create shape for icon
 			if (!(addedEnsemble.getName() == "Empty Ensemble")) {
 				Shape iconShape = peCreateService.createShape(containerShape,
 						false);
 				Image icon = gaService.createImage(iconShape,
 						"de.opalproject.vespucci.sliceEditor.ensembleIcon");
				gaService.setLocationAndSize(icon, 2, 3, 16,
 						16);
 			}
 		}
 
 		// add a chopbox anchor to the shape
 		peCreateService.createChopboxAnchor(containerShape);
 
 		layoutPictogramElement(containerShape);
 
 		return containerShape;
 	}
 
 //	/**
 //	 * Generate a problem marker when an invalid slice is detected.
 //	 * 
 //	 * @param str - String containing the type of infringement
 //	 * @param picel - Pictogramelement
 //	 * @param ensA - Ensemble to be added
 //	 * @param ensB - An already existing conflicting ensembleinstance
 //	 */
 //	private void generateMarker(String str, PictogramElement picel, Ensemble ensA,
 //			Ensemble ensB, Diagram dia) {
 //		EObject bo = (EObject) getBusinessObjectForPictogramElement(picel);
 //
 //		try {
 //			// retrieve URI
 //			URI uri = EcoreUtil.getURI(bo);
 //			uri = uri.trimFragment();
 //			// remove "platform:..." from uri
 //			if (uri.isPlatform()) {
 //				uri = URI.createURI(uri.toPlatformString(true));
 //			}
 //			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace()
 //					.getRoot();
 //
 //			// try to get project from whole uri resource
 //			IResource resource = workspaceRoot.findMember(uri.toString());
 //
 //			// create marker
 //			IMarker marker = resource.createMarker(IMarker.PROBLEM);
 //			marker.setAttribute(
 //					IMarker.MESSAGE,
 //					str+ "-Slice is invalid "
 //							+ ensB.toString() + " is a descendant of  "
 //							+ ensA.toString());
 //			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
 //
 //		} catch (Exception e) {
 //			e.printStackTrace();
 //		}
 //	}
 }
