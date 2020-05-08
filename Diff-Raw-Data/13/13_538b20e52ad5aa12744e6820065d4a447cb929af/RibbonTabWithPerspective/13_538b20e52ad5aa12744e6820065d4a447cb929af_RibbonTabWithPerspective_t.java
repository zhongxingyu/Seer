 /*******************************************************************************
  * Copyright (c) 2009, Elias Volanakis
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Elias Volanakis - initial API and implementation
  *******************************************************************************/
 package de.volanakis.ribbonide.internal;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 
 import com.hexapixel.widgets.ribbon.RibbonTab;
 import com.hexapixel.widgets.ribbon.RibbonTabFolder;
 
 final class RibbonTabWithPerspective extends RibbonTab {
 	private final String perspectiveId;
	private boolean isBusy;
 
 	RibbonTabWithPerspective(RibbonTabFolder rtf, String name,
 			String perspectiveId) {
 		super(rtf, name);
 		Assert.isNotNull(perspectiveId);
 		this.perspectiveId = perspectiveId;
 	}
 
 	@Override
 	public void setSelected(boolean selected) {
 		if (selected && selected != isSelected()) {
 			switchPerspective(perspectiveId);
 		}
 		super.setSelected(selected);
 	}
 
 	/**
 	 * Select this tab without switching perspective
 	 */
 	public void selectTab() {
 		getTabFolder().selectTab(this);
 	}
 
 	public String getPerspectiveId() {
 		return perspectiveId;
 	}
 
 	// helping methods
 	// ////////////////
 
 	private void switchPerspective(final String perspId) {
		if (!PlatformUI.isWorkbenchRunning() || perspId == null || isBusy) {
 			return;
 		}
		isBusy = true;
 		final IWorkbench workbench = PlatformUI.getWorkbench();
 		final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
 		if (window != null) {
 			Runnable op = new Runnable() {
 				public void run() {
 					try {
 						workbench.showPerspective(perspId, window);
 					} catch (WorkbenchException wex) {
 						Activator.log(wex);
 					}
 				}
 			};
 			// this must be synchronous, because we want to switch perspective,
 			// switch tabs and update button enablement state all in one step
			try {
				workbench.getDisplay().syncExec(op);
			} finally {
				isBusy = false;
			}
 		}
 	}
 }
