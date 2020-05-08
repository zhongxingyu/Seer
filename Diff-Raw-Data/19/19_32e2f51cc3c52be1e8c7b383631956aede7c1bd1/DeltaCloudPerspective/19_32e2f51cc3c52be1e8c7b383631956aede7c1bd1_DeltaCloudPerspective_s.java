 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc..
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Incorporated - initial API and implementation
  *******************************************************************************/
 package org.jboss.tools.deltacloud.ui;
 
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveFactory;
 
 public class DeltaCloudPerspective implements IPerspectiveFactory {
 
 	@Override
 	public void createInitialLayout(IPageLayout layout) {
 		layout.addView("org.jboss.tools.deltacloud.ui.views.CloudView", IPageLayout.LEFT,
 				0.25f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(IPageLayout.ID_PROP_SHEET, IPageLayout.BOTTOM,
				0.80f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("org.jboss.tools.deltacloud.ui.views.InstanceView", IPageLayout.BOTTOM, 
				0.80f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("org.jboss.tools.deltacloud.ui.views.ImageView", IPageLayout.BOTTOM, 
				0.80f, IPageLayout.ID_EDITOR_AREA);
 	}
 
 }
