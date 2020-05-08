 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
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
 package org.eclipse.graphiti.examples.common.perspective;
 
 import org.eclipse.graphiti.ui.internal.editor.ThumbNailView;
 import org.eclipse.ui.IFolderLayout;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveFactory;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.views.IViewDescriptor;
 
 /**
  * The Class GFPerspective. Defines the Eclipse perspective for testing
  * Graphiti.
  */
 @SuppressWarnings("restriction")
 public class GFPerspective implements IPerspectiveFactory {
 
 	/**
 	 * Instantiates a new perspective.
 	 */
 	public GFPerspective() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui
 	 * .IPageLayout)
 	 */
 	public void createInitialLayout(IPageLayout layout) {
 		defineActions(layout);
 		defineLayout(layout);
 	}
 
 	/**
 	 * Defines the initial actions for a page.
 	 * 
 	 * @param layout
 	 *            The layout we are filling
 	 */
 	private void defineActions(IPageLayout layout) {
 		// Add "new wizards".
 		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
 		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
 
 		// Add "show views".
 		layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);
 		layout.addShowViewShortcut(IPageLayout.ID_BOOKMARKS);
 		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
 		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
 		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_PROGRESS_VIEW);
 		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
 
 		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
 	}
 
 	/**
 	 * Defines the initial layout for a page.
 	 * 
 	 * @param layout
 	 *            The layout we are filling
 	 */
 	private void defineLayout(IPageLayout layout) {
 		// Editors are placed for free.
 		String editorArea = layout.getEditorArea();
 
 		// Top left.
 		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, (float) 0.26, editorArea);//$NON-NLS-1$
		if (isViewAvailable(IPageLayout.ID_PROJECT_EXPLORER)) {
			topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);
		}
 		if (isViewAvailable("org.eclipse.jdt.ui.PackageExplorer")) { //$NON-NLS-1$
 			topLeft.addView("org.eclipse.jdt.ui.PackageExplorer"); //$NON-NLS-1$
 		}
 		topLeft.addPlaceholder(IPageLayout.ID_BOOKMARKS);
 
 		// Bottom left.
 		IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, (float) 0.50,//$NON-NLS-1$
 				"topLeft");//$NON-NLS-1$
 		if (isViewAvailable(IPageLayout.ID_OUTLINE)) {
 			bottomLeft.addView(IPageLayout.ID_OUTLINE);
 		}
 		if (isViewAvailable(ThumbNailView.VIEW_ID)) {
 			bottomLeft.addView(ThumbNailView.VIEW_ID); //$NON-NLS-1$
 		}
 
 		// Bottom right.
 		IFolderLayout bottomRight = layout.createFolder("bottomRight", IPageLayout.BOTTOM, (float) 0.66,//$NON-NLS-1$
 				editorArea);
 		if (isViewAvailable("org.eclipse.pde.runtime.LogView")) { //$NON-NLS-1$
 			bottomRight.addView("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
 		}
 		if (isViewAvailable(IPageLayout.ID_PROP_SHEET)) {
 			bottomRight.addView(IPageLayout.ID_PROP_SHEET);
 		}
 		if (isViewAvailable(IPageLayout.ID_PROBLEM_VIEW)) {
 			bottomRight.addView(IPageLayout.ID_PROBLEM_VIEW);
 		}
 	}
 
 	private static boolean isViewAvailable(String viewId) {
 		IViewDescriptor viewDesc = PlatformUI.getWorkbench().getViewRegistry().find(viewId);
 		return viewDesc != null;
 	}
 }
