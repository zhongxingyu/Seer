 /*******************************************************************************
  * Copyright (c) 2007-2009 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.css;
 
 import org.eclipse.debug.ui.IDebugUIConstants;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.ui.IFolderLayout;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveFactory;
 
 /**
  * @author Sergey Dzmitrovich
  * 
  */
 public class CSSPerspective implements IPerspectiveFactory {
 
 	public static final String CSS_EDITOR_VIEW = "org.jboss.tools.jst.css.view.editor"; //$NON-NLS-1$
 	public static final String CSS_PREVIEW_VIEW = "org.jboss.tools.jst.css.view.preview"; //$NON-NLS-1$
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui
 	 * .IPageLayout)
 	 */
 	public void createInitialLayout(IPageLayout layout) {
 
 		String editorArea = layout.getEditorArea();
 
 		IFolderLayout leftTop = layout.createFolder(
 				"leftTop", IPageLayout.LEFT, (float) 0.2, editorArea); //$NON-NLS-1$
 		leftTop.addView(JavaUI.ID_PACKAGES);
 		leftTop.addView("org.jboss.tools.jst.web.ui.navigator.WebProjectsView"); //$NON-NLS-1$
		leftTop
				.addView("org.jboss.tools.seam.ui.views.SeamComponentsNavigator"); //$NON-NLS-1$
 		leftTop.addPlaceholder(IPageLayout.ID_RES_NAV);
 
 		IFolderLayout leftBottom = layout.createFolder(
 				"leftBottom", IPageLayout.BOTTOM, (float) 0.64, "leftTop"); //$NON-NLS-1$ //$NON-NLS-2$
 		leftBottom.addView(IPageLayout.ID_PROP_SHEET);
 
 		IFolderLayout bottomCenter = layout.createFolder(
 				"bottomCenter", IPageLayout.BOTTOM, (float) 0.64, editorArea); //$NON-NLS-1$
 		bottomCenter.addView(CSS_EDITOR_VIEW);
 
 		IFolderLayout bottomRight = layout.createFolder(
 				"bottomRight", IPageLayout.RIGHT, (float) 0.7, "bottomCenter"); //$NON-NLS-1$ //$NON-NLS-2$
 		bottomRight.addView(CSS_PREVIEW_VIEW);
 
 		IFolderLayout rightTop = layout.createFolder(
 				"right", IPageLayout.RIGHT, (float) 0.8, editorArea); //$NON-NLS-1$
 		rightTop.addView(IPageLayout.ID_OUTLINE);
 
 		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
 		layout.addActionSet(JavaUI.ID_ACTION_SET);
 		layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
 		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
 
 		// views - seam
 		layout
 				.addShowViewShortcut("org.jboss.tools.seam.ui.views.SeamComponentsNavigator"); //$NON-NLS-1$
 
 		// views - java
 		layout.addShowViewShortcut(JavaUI.ID_PACKAGES);
 		layout.addShowViewShortcut(JavaUI.ID_TYPE_HIERARCHY);
 		layout.addShowViewShortcut(JavaUI.ID_SOURCE_VIEW);
 
 		// views - standard workbench
 		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
 		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
 		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
 
 		// new actions - Java project creation wizard
 		layout
 				.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewPackageCreationWizard"); //$NON-NLS-1$
 		layout
 				.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewClassCreationWizard"); //$NON-NLS-1$
 		layout
 				.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewInterfaceCreationWizard"); //$NON-NLS-1$
 		layout
 				.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSourceFolderCreationWizard"); //$NON-NLS-1$
 		layout
 				.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSnippetFileCreationWizard"); //$NON-NLS-1$
 		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
 		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
 
 	}
 
 }
