 /*******************************************************************************
  * Copyright (c) 2010-2011 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.internal.perspective;
 
 import org.eclipse.ui.IFolderLayout;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveFactory;
 
 /**
  * Factory for creating the Fedora Packaging perspective.
  */
 public class PackagingPerspectiveFactory implements IPerspectiveFactory {
 
 	@Override
 	public void createInitialLayout(IPageLayout layout) {
 		defineActions(layout);
 		defineLayout(layout);
 	}
 
 	/**
 	 * 
 	 * @param layout
 	 */
 	public void defineActions(IPageLayout layout) {
 		// Add "new wizards".
 		layout.addNewWizardShortcut("org.fedoraproject.eclipse.packager.local.newprojectwizard"); //$NON-NLS-1$
 		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder"); //$NON-NLS-1$
 		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file"); //$NON-NLS-1$
 		
 		// Add "show views".
 		layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);
 		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
 		layout.addShowViewShortcut("org.eclipse.ui.console.ConsoleView"); //$NON-NLS-1$
 		layout.addShowViewShortcut("org.eclipse.egit.ui.RepositoriesView"); //$NON-NLS-1$
 		// Show compatibility action set
 		layout.addActionSet("org.fedoraproject.eclipse.packager.compatibility.FedoraPackagerActionSet"); //$NON-NLS-1$
 	}
 
 	/**
 	 * 
 	 * @param layout
 	 */
 	public void defineLayout(IPageLayout layout) {
 		// Editors are placed for free.
 		String editorArea = layout.getEditorArea();
 
 		// Place Package Explorer on the left, Outline on the right and
 		// Git Repositories, Console view on the bottom
 		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, //$NON-NLS-1$
 				(float) 0.2, editorArea);
 		left.addView(IPageLayout.ID_PROJECT_EXPLORER);
 		IFolderLayout bottom = layout.createFolder("bottom", //$NON-NLS-1$
 				IPageLayout.BOTTOM, (float) 0.7, editorArea);
 		bottom.addView("org.eclipse.egit.ui.RepositoriesView"); //$NON-NLS-1$
 		bottom.addView("org.eclipse.ui.console.ConsoleView"); //$NON-NLS-1$
 		IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT, //$NON-NLS-1$
 				(float) 0.8, editorArea);
 		right.addView(IPageLayout.ID_OUTLINE);
 	}
 
 }
