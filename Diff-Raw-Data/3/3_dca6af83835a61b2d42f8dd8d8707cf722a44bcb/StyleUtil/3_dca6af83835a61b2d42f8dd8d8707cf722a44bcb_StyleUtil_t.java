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
 package org.eclipse.graphiti.examples.tutorial;
 
 import java.util.Collection;
 
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.Style;
 import org.eclipse.graphiti.mm.pictograms.StyleContainer;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.util.ColorConstant;
 import org.eclipse.graphiti.util.IColorConstant;
import org.eclipse.graphiti.util.PredefinedColoredAreas;
 
 public class StyleUtil {
 
 	private static final IColorConstant E_CLASS_TEXT_FOREGROUND = new ColorConstant(51, 51, 153);
 
 	private static final IColorConstant E_CLASS_FOREGROUND = new ColorConstant(255, 102, 0);
 
 	public static Style getStyleForEClass(Diagram diagram) {
 		final String styleId = "E-CLASS"; //$NON-NLS-1$
 
 		Style style = findStyle(diagram, styleId);
 
 		IGaService gaService = Graphiti.getGaService();
 		if (style == null) { // style not found - create new style
 			style = gaService.createStyle(diagram, styleId);
 			style.setForeground(gaService.manageColor(diagram, E_CLASS_FOREGROUND));
 			gaService.setRenderingStyle(style, PredefinedColoredAreas.getBlueWhiteGlossAdaptions());
 			style.setLineWidth(2);
 		}
 		return style;
 	}
 
 	public static Style getStyleForEClassText(Diagram diagram) {
 		final String styleId = "ECLASS-TEXT"; //$NON-NLS-1$
 
 		// this is a child style of the e-class-style
 		Style parentStyle = getStyleForEClass(diagram);
 		Style style = findStyle(parentStyle, styleId);
 
 		if (style == null) { // style not found - create new style
 			IGaService gaService = Graphiti.getGaService();
 			style = gaService.createStyle(diagram, styleId);
 			// "overwrites" values from parent style
 			style.setForeground(gaService.manageColor(diagram, E_CLASS_TEXT_FOREGROUND));
 		}
 		return style;
 	}
 
 	// find the style with a given id in the style-container, can return null
 	private static Style findStyle(StyleContainer styleContainer, String id) {
 		// find and return style
 		Collection<Style> styles = styleContainer.getStyles();
 		if (styles != null) {
 			for (Style style : styles) {
 				if (id.equals(style.getId())) {
 					return style;
 				}
 			}
 		}
 		return null;
 	}
 }
