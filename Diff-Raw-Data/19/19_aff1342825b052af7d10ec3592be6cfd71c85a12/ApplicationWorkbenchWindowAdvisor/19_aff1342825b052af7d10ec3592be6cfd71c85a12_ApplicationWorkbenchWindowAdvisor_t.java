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
 
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.application.ActionBarAdvisor;
 import org.eclipse.ui.application.IActionBarConfigurer;
 import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
 import org.eclipse.ui.application.WorkbenchWindowAdvisor;
 
 public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
 
 	public ApplicationWorkbenchWindowAdvisor(
 			IWorkbenchWindowConfigurer configurer) {
 		super(configurer);
 	}
 
 	@Override
 	public ActionBarAdvisor createActionBarAdvisor(
 			IActionBarConfigurer configurer) {
 		return new ApplicationActionBarAdvisor(configurer);
 	}
 
 	@Override
 	public void preWindowOpen() {
 		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
 
		configurer.setInitialSize(new Point(900, 600));
 		configurer.setShowCoolBar(true);
 		configurer.setShowStatusLine(true);
 		configurer.setShowProgressIndicator(true);
 
 	}
 
 	@Override
 	public void postWindowOpen() {
 		// TODO Auto-generated method stub
 		super.postWindowOpen();
 		int x = PlatformUI.getPreferenceStore().getInt("posx");
 		int y = PlatformUI.getPreferenceStore().getInt("posy");
 		getWindowConfigurer().getWindow().getShell().setLocation(
 				0 == x ? 800 : x, 0 == y ? 300 : y);
 	}
 
 	@Override
 	public boolean preWindowShellClose() {
 		int x = getWindowConfigurer().getWindow().getShell().getLocation().x;
 		int y = getWindowConfigurer().getWindow().getShell().getLocation().y;
 
 		PlatformUI.getPreferenceStore().setValue("posx", x);
 		PlatformUI.getPreferenceStore().setValue("posy", y);
 		return super.preWindowShellClose();
 
 	}
 }
