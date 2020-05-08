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
 
import org.eclipse.ui.IFolderLayout;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveFactory;
 
 public class DeltaCloudPerspective implements IPerspectiveFactory {
 
 	@Override
 	public void createInitialLayout(IPageLayout layout) {
 		layout.addView("org.jboss.tools.deltacloud.ui.views.CloudView", IPageLayout.LEFT,
 				0.25f, IPageLayout.ID_EDITOR_AREA);
		IFolderLayout bottom =
			layout.createFolder("bottom", IPageLayout.BOTTOM, 0.80f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
		bottom.addView(IPageLayout.ID_PROP_SHEET);
		bottom.addView("org.jboss.tools.deltacloud.ui.views.InstanceView"); //$NON-NLS-1$
		bottom.addView("org.jboss.tools.deltacloud.ui.views.ImageView"); //$NON-NLS-1$
 	}
 
 }
