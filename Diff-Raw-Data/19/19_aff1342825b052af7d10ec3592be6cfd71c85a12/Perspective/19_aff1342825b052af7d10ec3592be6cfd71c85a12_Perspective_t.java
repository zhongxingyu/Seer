 /*******************************************************************************
  *  Copyright (c) 2010 Weltevree Beheer BV, Remain Software & Industrial-TSI
  *                                                                      
  * All rights reserved. This program and the accompanying materials     
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at             
  * http://www.eclipse.org/legal/epl-v10.html                            
  *                                                                      
  * Contributors:                                                        
  *    Wim Jongman - initial API and implementation
  *******************************************************************************/
 
  
 package org.eclipse.ecf.salvo.application.internal;
 
 import org.eclipse.ecf.protocol.nntp.model.SALVO;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveFactory;
 
 
 
 public class Perspective implements IPerspectiveFactory {
 
 	public void createInitialLayout(IPageLayout layout) {
 
 		layout
 				.createPlaceholderFolder(SALVO.APPLICATION_LEFT, IPageLayout.LEFT, 0.3f, layout
 						.getEditorArea());
		layout.createPlaceholderFolder(SALVO.APPLICATION_BOTTOMLEFT, IPageLayout.BOTTOM, 0.6f,
 				SALVO.APPLICATION_LEFT);
 		layout.createPlaceholderFolder(SALVO.APPLICATION_BOTTOMRIGHT, IPageLayout.BOTTOM, 0.7f, layout
 				.getEditorArea());
 
		layout.createPlaceholderFolder(SALVO.APPLICATION_RIGHT, IPageLayout.RIGHT, 0.8f, layout
 				.getEditorArea());
 		
 	}
 }
