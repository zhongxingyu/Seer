 /*
 * Copyright (c) 2006 Eclipse.org
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Dmitry Stadnik - initial API and implementation
  */
 package org.eclipse.gmf.internal.codegen.lite.dashboard;
 
 import org.eclipse.gmf.bridge.ui.dashboard.DashboardAction;
 import org.eclipse.gmf.bridge.ui.dashboard.DashboardFacade;
 import org.eclipse.gmf.bridge.ui.dashboard.DashboardState;
 import org.eclipse.gmf.internal.codegen.lite.popup.actions.ExecuteLiteTemplatesOperation;
 
 /**
  * @author dstadnik
  */
 public class GenerateLiteDEAction implements DashboardAction {
 
 	private DashboardFacade context;
 
 	public void init(DashboardFacade context) {
 		this.context = context;
 	}
 
 	public boolean isEnabled() {
 		DashboardState state = context.getState();
 		if (context.isStrict()) {
 			if (state.getDM() == null || state.getDGM() == null) {
 				return false;
 			}
 		}
 		return state.getGM() != null;
 	}
 
 	public void run() {
 		ExecuteLiteTemplatesOperation op = new ExecuteLiteTemplatesOperation();
 		op.setGenModelURI(context.getState().getGM());
 		op.run();
 	}
 }
