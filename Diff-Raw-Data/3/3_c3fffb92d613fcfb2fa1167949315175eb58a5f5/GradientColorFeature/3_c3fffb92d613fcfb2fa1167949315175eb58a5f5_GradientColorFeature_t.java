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
  * Created on 12.12.2005
  */
 package org.eclipse.graphiti.testtool.sketch.features;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.graphiti.examples.common.SampleUtil;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.mm.pictograms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.util.IPredefinedRenderingStyle;
import org.eclipse.graphiti.util.PredefinedColoredAreas;
 
 /**
  * The Class GradientColorFeature.
  */
 public class GradientColorFeature extends AbstractColorFeature {
 
 	private static final String NAME = "Gradient Color...";
 
 	private static final String DESCRIPTION = "Modify Gradient Color";
 
 	/**
 	 * The Constructor.
 	 * 
 	 * @param fp
 	 *            the fp
 	 */
 	public GradientColorFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	@Override
 	public String getDescription() {
 		return DESCRIPTION;
 	}
 
 	@Override
 	public String getName() {
 		return NAME;
 	}
 
 	@Override
 	public boolean canExecute(ICustomContext context) {
 		boolean ret = false;
 		PictogramElement[] pes = context.getPictogramElements();
 		if (pes != null && pes.length >= 1) {
 			GraphicsAlgorithm ga = pes[0].getGraphicsAlgorithm();
 			if (ga != null) {
 				ret = true;
 			}
 		}
 		return ret;
 	}
 
 	public void execute(ICustomContext context) {
 		PictogramElement[] pes = context.getPictogramElements();
 		if (pes != null && pes.length >= 1) {
 			GraphicsAlgorithm ga = pes[0].getGraphicsAlgorithm();
 			if (ga != null) {
 				// init with default-values
 				String id = IPredefinedRenderingStyle.BLUE_WHITE_GLOSS_ID;
 
 				{
 					// ask user for new values
 					while (true) {
 						List<String> allIDs = Arrays.asList(IPredefinedRenderingStyle.BLUE_WHITE_GLOSS_ID,
 								IPredefinedRenderingStyle.BLUE_WHITE_ID, IPredefinedRenderingStyle.COPPER_WHITE_GLOSS_ID,
 								IPredefinedRenderingStyle.LIGHT_GRAY_ID, IPredefinedRenderingStyle.LIGHT_YELLOW_ID,
 								IPredefinedRenderingStyle.SILVER_WHITE_GLOSS_ID);
 						String askString = SampleUtil.askString("Enter gradient values",
 								"Enter the predefined style ID here. The following IDs are supported: " + allIDs, id);
 						if (askString == null) {
 							return;
 						}
 						if (allIDs.contains(askString)) {
 							id = askString;
 							break;
 						}
 					}
 				}
 
 				// set new values to all selected pictogram-elements
 				for (int i = 0; i < pes.length; i++) {
 					PictogramElement pe = pes[i];
 					GraphicsAlgorithm currentGa = pe.getGraphicsAlgorithm();
 					Graphiti.getGaService().setRenderingStyle(currentGa, PredefinedColoredAreas.getAdaptedGradientColoredAreas(id));
 				}
 			}
 		}
 	}
 }
