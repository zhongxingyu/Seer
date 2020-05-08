 /*-
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.dawnsci.intro.actions;
 
 import java.util.Properties;
 
import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.intro.IIntroSite;
 import org.eclipse.ui.intro.config.IIntroAction;
 
 /**
  * Class to open a webbrowser through an org.eclipse.ui.intro/runAction
  * 
  * @author Baha El Kassaby
  * 
  */
 public class OpenWebBrowserAction implements IIntroAction {
 
 	private static final String ATT_URL = "url"; //$NON-NLS-1$
 
 	@Override
 	public void run(IIntroSite site, Properties params) {
 		final String url = params.getProperty(ATT_URL);

 		//run the appropriate launcher
 		Display.getDefault().asyncExec(new Runnable(){
 			@Override
 			public void run() {
				Program.launch(url);
 			}
 		});
 	}
 }
