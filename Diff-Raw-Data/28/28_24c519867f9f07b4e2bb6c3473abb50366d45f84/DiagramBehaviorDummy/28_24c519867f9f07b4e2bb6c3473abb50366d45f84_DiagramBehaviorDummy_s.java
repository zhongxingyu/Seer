 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2013, 2013 SAP AG.
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
 package org.eclipse.graphiti.ui.internal.editor;
 
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.IFeature;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.platform.IDiagramBehavior;
 import org.eclipse.graphiti.platform.IDiagramContainer;
 
 public class DiagramBehaviorDummy implements IDiagramBehavior {
 
 	private IDiagramContainer diagramContainer;
 
	public DiagramBehaviorDummy(IDiagramContainer diagramContainer) {
 		super();
 		this.diagramContainer = diagramContainer;
 	}
 
 	public IDiagramContainer getDiagramContainer() {
 		return diagramContainer;
 	}
 
 	public void refresh() {
 	}
 
 	public void refreshPalette() {
 	}
 
 	public void refreshContent() {
 	}
 
 	public void refreshRenderingDecorators(PictogramElement pe) {
 	}
 
 	/**
 	 * Can be called to execute the given {@link IFeature} using the given
 	 * {@link IContext}; also the Graphiti framework uses this method to call
 	 * additional features from with feature processing. In the latter case this
 	 * method is called from with an EMF Transaction so that modifications are
 	 * wrapped inside this Transaction. In case the method is called directly by
 	 * a user, it needs to be ensured that the call happens with an EMF
 	 * transaction, otherwise an according EMF Transactions exception will be
 	 * thrown.
 	 * 
 	 * @param feature
 	 *            the {@link IFeature} to execute
 	 * @param context
 	 *            the {@link IContext} to use while executing the feature
 	 * @return in case of an {@link IAddFeature} being passed as feature the
 	 *         newly added {@link PictogramElement} will be returned (in case
 	 *         the add method returning it), in all other cases
 	 *         <code>null</code>
 	 */
 	public Object executeFeature(IFeature feature, IContext context) {
 		Object returnValue = null;
 		if (feature != null && context != null && feature.canExecute(context)) {
 			if (feature instanceof IAddFeature) {
 				IAddFeature addFeature = (IAddFeature) feature;
 				returnValue = addFeature.add((IAddContext) context);
 			} else {
 				feature.execute(context);
 			}
 		}
 
 		return returnValue;
 	}
 
 	public TransactionalEditingDomain getEditingDomain() {
		// TODO Auto-generated method stub
		return null;
 	}
 }
