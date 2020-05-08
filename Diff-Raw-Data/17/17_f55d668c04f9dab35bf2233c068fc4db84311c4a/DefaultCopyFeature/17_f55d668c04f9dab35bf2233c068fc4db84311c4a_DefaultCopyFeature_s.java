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
 /*
  * Created on 06.07.2005
  */
package org.eclipse.graphiti.bot.tests.features;

import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.ICopyContext;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.ui.features.AbstractCopyFeature;
 
 /**
  * The Class DefaultCopyFeature.
  */
 public class DefaultCopyFeature extends AbstractCopyFeature {
 
 	/**
 	 * The Constructor.
 	 * 
 	 * @param fp
 	 *            the fp
 	 */
 	public DefaultCopyFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	public boolean canCopy(ICopyContext context) {
 		final PictogramElement[] pictogramElements = context.getPictogramElements();
 		return pictogramElements != null && pictogramElements.length > 0;
 	}
 
 	public void copy(ICopyContext context) {
 		final PictogramElement[] pes = context.getPictogramElements();
 		putToClipboard(pes);
 	}
 }
