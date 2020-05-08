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
 
 import org.eclipse.emf.common.command.Command;
 
 import org.eclipse.emf.edit.command.RemoveCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.impl.DefaultRemoveFeature;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 
 import de.opalproject.vespucci.datamodel.DatamodelPackage;
 import de.opalproject.vespucci.datamodel.Slice;
 
 /**
  * Removes the graphical representation from the slice without touching the
  * business object
  * 
  * @author Lars
  * 
  */
 public class RemoveFeature extends DefaultRemoveFeature {
 
 	/**
 	 * @param fp
 	 */
 	public RemoveFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.features.impl.DefaultRemoveFeature#remove(org.eclipse
 	 * .graphiti.features.context.IRemoveContext)
 	 */
 	@Override
 	public void remove(IRemoveContext context) {
 		Object buisnessObject = getBusinessObjectForPictogramElement(getDiagram());
 		if (buisnessObject instanceof Slice) {
 
 			TransactionalEditingDomain domain = TransactionalEditingDomain.Registry.INSTANCE
 					.getEditingDomain("de.opalproject.vespucci.navigator.domain.DatamodelEditingDomain");
 
 			Slice slice = (Slice) buisnessObject;
 			Command removeCommand;
 			// checks if the element to remove is a connection or an ensemble
 			if (context.getPictogramElement() instanceof Connection) {
 				removeCommand = RemoveCommand.create(domain, slice,
 						DatamodelPackage.Literals.SLICE__CONSTRAINTS,
 						getBusinessObjectForPictogramElement(context
 								.getPictogramElement()));
 			} else {
 				removeCommand = RemoveCommand.create(domain, slice,
 						DatamodelPackage.Literals.SLICE__ENSEMBLES,
 						getBusinessObjectForPictogramElement(context
 								.getPictogramElement()));
 			}
 
 			domain.getCommandStack().execute(removeCommand);
 			super.remove(context);
 		}
 	}
 
 }
