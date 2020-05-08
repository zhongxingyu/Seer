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
 package de.opalproject.vespucci.sliceEditor.features.constraints;
 
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IAddConnectionContext;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.impl.AbstractAddFeature;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeCreateService;
 import org.eclipse.graphiti.util.ColorConstant;
 import org.eclipse.graphiti.util.IColorConstant;
 
 import de.opalproject.vespucci.datamodel.Constraint;
 
 /**
  * This feature add the possibility to link ensembles.
  * 
  * 
  * @author Lars
  * @author Marius
  * 
  */
 public abstract class AddConstraintFeature extends AbstractAddFeature {
 
 	protected static final IColorConstant CONSTRAINT_FOREGROUND = new ColorConstant(
 			98, 131, 167);
 	protected static final IColorConstant CONSTRAINT_BACKGROUND = new ColorConstant(
 			255, 255, 255);
 
 	public AddConstraintFeature(IFeatureProvider fp) {
 		super(fp);
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
 		IAddConnectionContext addConContext = (IAddConnectionContext) context;
 		Constraint addedConstraint = (Constraint) context.getNewObject();
 		IPeCreateService peCreateService = Graphiti.getPeCreateService();
 
 		// CONNECTION WITH POLYLINE
 		Connection connection = peCreateService
 				.createFreeFormConnection(getDiagram());
 		connection.setStart(addConContext.getSourceAnchor());
 		connection.setEnd(addConContext.getTargetAnchor());
 
 		IGaService gaService = Graphiti.getGaService();
 
 		// create link and wire it
 		link(connection, addedConstraint);
 
 		// add dynamic text decorator for the association name
 		ConnectionDecorator textDecorator = peCreateService
 				.createConnectionDecorator(connection, true, 0.5, true);
 		Text text = gaService.createText(textDecorator);
 		text.setForeground(manageColor(IColorConstant.BLACK));
 		text.setFont(gaService.manageFont(getDiagram(), "Arial", 10, false,
 				false));
 		gaService.setLocation(text, 10, 0);
 
 		// set dependency kind
 		text.setValue(addedConstraint.getDependencyKind());

 		// create any optical arrow representation
 		createArrow(connection, gaService, peCreateService);
 
 		return connection;
 	}
 
 	/*
 	 * checks if the given context is a constraint and therefore can be added
 	 * 
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.func.IAdd#canAdd(org.eclipse.graphiti.features.context
 	 * .IAddContext)
 	 */
 	@Override
 	public boolean canAdd(IAddContext context) {
 		// return true if given business object is a constraint
 		// note, that the context must be an instance of IAddConnectionContext
 		if (context instanceof IAddConnectionContext
 				&& context.getNewObject() instanceof Constraint) {
 			return true;
 		}
 		return false;
 	}
 
 	protected abstract void createArrow(Connection connection,
 			IGaService igaService, IPeCreateService peCreateService);
 }
