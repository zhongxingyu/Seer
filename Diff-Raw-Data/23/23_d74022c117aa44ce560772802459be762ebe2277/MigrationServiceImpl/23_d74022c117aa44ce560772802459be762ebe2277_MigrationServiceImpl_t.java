 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2011 SAP AG.
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
 package org.eclipse.graphiti.internal.services.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.graphiti.mm.algorithms.AbstractText;
 import org.eclipse.graphiti.mm.algorithms.styles.Font;
 import org.eclipse.graphiti.mm.algorithms.styles.Style;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IMigrationService;
 
 public class MigrationServiceImpl implements IMigrationService {
 
 	@Override
 	public void migrate070To080(Diagram d) {
 
 		// Traverse model and collect fonts
 		Map<Font, ArrayList<EObject>> fontToUser = new HashMap<Font, ArrayList<EObject>>();
 		Resource eResource = d.eResource();
 		TreeIterator<EObject> allContents = eResource.getAllContents();
 		while (allContents.hasNext()) {
 			EObject eObject = allContents.next();
 			if (eObject instanceof AbstractText) {
 				AbstractText t = (AbstractText) eObject;
 				Font font = t.getFont();
 				addFontUser(fontToUser, eObject, font);
 			} else if (eObject instanceof Style) {
 				Style s = (Style) eObject;
 				Font font = s.getFont();
 				addFontUser(fontToUser, eObject, font);
 			}
 		}
 
 		// Manage collected fonts and set the new font on the respective
 		// elements. Includes possibly a write.
 		// The caller has to use a write transaction.
 		for (Font font : fontToUser.keySet()) {
 			Font newFont = Graphiti.getGaService().manageFont(d, font.getName(), font.getSize(), font.isItalic(), font.isBold());
 			ArrayList<EObject> fontUsers = fontToUser.get(font);
 			for (EObject fontUser : fontUsers) {
 				if (fontUser instanceof AbstractText) {
 					((AbstractText) fontUser).setFont(newFont);
 				} else {
 					((Style) fontUser).setFont(newFont);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void migrate080To090(Diagram d) {
 		// Traverse model and and set unfilled texts to filled
 		Resource eResource = d.eResource();
 		TreeIterator<EObject> allContents = eResource.getAllContents();
 		while (allContents.hasNext()) {
 			EObject eObject = allContents.next();
 			if (eObject instanceof AbstractText) {
 				AbstractText t = (AbstractText) eObject;
 				if (t.getFilled()) {
 					t.setFilled(Boolean.FALSE);
 				}
 			}
 		}
 	}
 
 	@Override
 	public boolean shouldMigrate080To090(Diagram d) {
 		String version = d.getVersion();
		if (version == null || version.length() == 0) {
 			Resource eResource = d.eResource();
 			TreeIterator<EObject> allContents = eResource.getAllContents();
 			while (allContents.hasNext()) {
 				EObject eObject = allContents.next();
 				if (eObject instanceof AbstractText) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 
 	private void addFontUser(Map<Font, ArrayList<EObject>> fontToUser, EObject fontUser, Font font) {
 		if (font != null) {
 			if (fontToUser.get(font) == null) {
 				fontToUser.put(font, new ArrayList<EObject>());
 			}
 			fontToUser.get(font).add(fontUser);
 		}
 	}
 
 	@Override
 	public boolean shouldMigrate070To080(Diagram d) {
 		// Traverse model and collect fonts
 		HashSet<Font> fonts = new HashSet<Font>();
 		Resource eResource = d.eResource();
 		TreeIterator<EObject> allContents = eResource.getAllContents();
 		while (allContents.hasNext()) {
 			EObject next = allContents.next();
 			if (next instanceof AbstractText) {
 				AbstractText t = (AbstractText) next;
 				Font font = t.getFont();
 				if (font != null)
 					fonts.add(font);
 
 			} else if (next instanceof Style) {
 				Style s = (Style) next;
 				Font font = s.getFont();
 				if (font != null)
 					fonts.add(font);
 			}
 		}
 		for (Font font : fonts) {
 
 			if (!d.getFonts().contains(font))
 				return true;
 		}
 		return false;
 	}
 
 }
