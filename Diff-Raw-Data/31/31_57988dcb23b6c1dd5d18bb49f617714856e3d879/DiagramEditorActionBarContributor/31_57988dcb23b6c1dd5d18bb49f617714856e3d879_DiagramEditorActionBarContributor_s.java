 /*******************************************************************************
  * <copyright>
  *
 * Copyright (c) 2005, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 327669 - removed dependencies to GEF internal stuff
  *    jpasch - Bug 323025 ActionBarContributor cleanup
  *    mwenz - Fixed NPE in RCP usecase
  *    Felix Velasco (mwenz) - Bug 323351 - Enable to suppress/reactivate the speed buttons
  *    mwenz - Bug 381437 - IllegalArgumentException when edit menu missing
  *    fvelasco - Bug 396247 - ImageDescriptor changes
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.editor;
 
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.gef.ui.actions.ActionBarContributor;
 import org.eclipse.gef.ui.actions.AlignmentRetargetAction;
 import org.eclipse.gef.ui.actions.GEFActionConstants;
 import org.eclipse.gef.ui.actions.MatchHeightRetargetAction;
 import org.eclipse.gef.ui.actions.MatchWidthRetargetAction;
 import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
 import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
 import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
 import org.eclipse.graphiti.platform.IPlatformImageConstants;
 import org.eclipse.graphiti.ui.internal.Messages;
 import org.eclipse.graphiti.ui.internal.action.RemoveAction;
 import org.eclipse.graphiti.ui.internal.action.SaveImageAction;
 import org.eclipse.graphiti.ui.internal.action.ToggleContextButtonPadAction;
 import org.eclipse.graphiti.ui.internal.action.UpdateAction;
 import org.eclipse.graphiti.ui.services.GraphitiUi;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.actions.RetargetAction;
 
 /**
  * Provides some standard-actions and adds them to the toolbar or the
  * toplevel-menu.
  * <p>
  * Some of the standard-actions are: undo/redo, delete, copy/paste, alignment,
  * zooming.
  * 
  * @noinstantiate This class is not intended to be instantiated by clients.
  */
 public class DiagramEditorActionBarContributor extends ActionBarContributor {
 
 	/**
 	 * Creates and initialises all Actions. See the corresponding method in the
 	 * super class.
 	 * 
 	 * @see org.eclipse.gef.ui.actions.ActionBarContributor
 	 */
 	@Override
 	protected void buildActions() {
 		addRetargetAction((RetargetAction) ActionFactory.UNDO.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
 		addRetargetAction((RetargetAction) ActionFactory.REDO.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
 		addRetargetAction((RetargetAction) ActionFactory.DELETE.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
 		addRetargetAction((RetargetAction) ActionFactory.COPY.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
 		addRetargetAction((RetargetAction) ActionFactory.PASTE.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
 		addRetargetAction((RetargetAction) ActionFactory.PRINT.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
 		addRetargetAction((RetargetAction) ActionFactory.SELECT_ALL.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
 
 		addRetargetAction(new AlignmentRetargetAction(PositionConstants.LEFT));
 		addRetargetAction(new AlignmentRetargetAction(PositionConstants.CENTER));
 		addRetargetAction(new AlignmentRetargetAction(PositionConstants.RIGHT));
 		addRetargetAction(new AlignmentRetargetAction(PositionConstants.TOP));
 		addRetargetAction(new AlignmentRetargetAction(PositionConstants.MIDDLE));
 		addRetargetAction(new AlignmentRetargetAction(PositionConstants.BOTTOM));
 		addRetargetAction(new MatchWidthRetargetAction());
 		addRetargetAction(new MatchHeightRetargetAction());
 
 		addRetargetAction(new ZoomInRetargetAction());
 		addRetargetAction(new ZoomOutRetargetAction());
 		addRetargetAction(new RetargetAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY, Messages.DiagramEditorActionBarContributor_Grid,
 				IAction.AS_CHECK_BOX));
 
 		addRetargetAction(new RetargetAction(GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY,
 				Messages.DiagramEditorActionBarContributor_SnapGeometry, IAction.AS_CHECK_BOX));
 
 		// Bug 323351: Add button to toggle a flag if the context pad buttons
 		// shall be shown or not
 		RetargetAction toggleContextPadAction = new RetargetAction(ToggleContextButtonPadAction.ACTION_ID,
 				ToggleContextButtonPadAction.TEXT, IAction.AS_CHECK_BOX);
 		toggleContextPadAction.setImageDescriptor(GraphitiUi.getImageService().getPlatformImageDescriptorForId(
 				IPlatformImageConstants.IMG_TOGGLE_PAD));
 		addRetargetAction(toggleContextPadAction);
 		// End bug 323351
 
 		RetargetAction removeRetargetAction = new RetargetAction(RemoveAction.ACTION_ID, RemoveAction.TEXT);
 		removeRetargetAction.setImageDescriptor(GraphitiUi.getImageService().getPlatformImageDescriptorForId(
 				IPlatformImageConstants.IMG_EDIT_REMOVE));
 		removeRetargetAction.setActionDefinitionId(RemoveAction.ACTION_DEFINITION_ID);
 		addRetargetAction(removeRetargetAction);
 		RetargetAction updateRetargetAction = new RetargetAction(UpdateAction.ACTION_ID, UpdateAction.TEXT);
 		updateRetargetAction.setImageDescriptor(GraphitiUi.getImageService().getPlatformImageDescriptorForId(
 				IPlatformImageConstants.IMG_EDIT_REFRESH));
 		updateRetargetAction.setActionDefinitionId(UpdateAction.ACTION_DEFINITION_ID);
 		addRetargetAction(updateRetargetAction);
 		RetargetAction saveImageRetargetAction = new RetargetAction(SaveImageAction.ACTION_ID, SaveImageAction.TEXT);
 		saveImageRetargetAction.setActionDefinitionId(SaveImageAction.ACTION_DEFINITION_ID);
 		addRetargetAction(saveImageRetargetAction);
 	}
 
 	/**
 	 * Global action keys are already declared with
 	 * {@link #addRetargetAction(RetargetAction)}. See the corresponding method
 	 * in the super class.
 	 * 
 	 * @see org.eclipse.gef.ui.actions.ActionBarContributor
 	 */
 	@Override
 	protected void declareGlobalActionKeys() {
 
 	}
 
 	/**
 	 * Adds Actions to the given IToolBarManager, which is displayed above the
 	 * editor. See the corresponding method in the super class.
 	 * 
 	 * @param tbm
 	 *            the {@link IToolBarManager}
 	 * 
 	 * @see org.eclipse.ui.part.EditorActionBarContributor
 	 */
 	@Override
 	public void contributeToToolBar(IToolBarManager tbm) {
 		tbm.add(getAction(ActionFactory.UNDO.getId()));
 		tbm.add(getAction(ActionFactory.REDO.getId()));
 
 		tbm.add(new Separator());
 		tbm.add(getAction(ActionFactory.COPY.getId()));
 		tbm.add(getAction(ActionFactory.PASTE.getId()));
 
 		tbm.add(new Separator());
 		tbm.add(getAction(GEFActionConstants.ALIGN_LEFT));
 		tbm.add(getAction(GEFActionConstants.ALIGN_CENTER));
 		tbm.add(getAction(GEFActionConstants.ALIGN_RIGHT));
 		tbm.add(new Separator());
 		tbm.add(getAction(GEFActionConstants.ALIGN_TOP));
 		tbm.add(getAction(GEFActionConstants.ALIGN_MIDDLE));
 		tbm.add(getAction(GEFActionConstants.ALIGN_BOTTOM));
 		tbm.add(new Separator());
 		tbm.add(getAction(GEFActionConstants.MATCH_WIDTH));
 		tbm.add(getAction(GEFActionConstants.MATCH_HEIGHT));
 
 		// Bug 323351: Add button to toggle a flag if the context pad buttons
 		// shall be shown or not
 		tbm.add(new Separator());
 		tbm.add(getAction(ToggleContextButtonPadAction.ACTION_ID));
 		// End bug 323351
 
 		tbm.add(new Separator());
 		tbm.add(getAction(GEFActionConstants.ZOOM_OUT));
 		tbm.add(getAction(GEFActionConstants.ZOOM_IN));
 		ZoomComboContributionItem zoomCombo = new ZoomComboContributionItem(getPage());
 		tbm.add(zoomCombo);
 
 		tbm.add(new Separator());
 	}
 
 	/**
 	 * Adds Actions to the given IMenuManager, which is displayed as the
 	 * main-menu of Eclipse. See the corresponding method in the super class.
 	 * 
 	 * @param menubar
 	 *            the menubar
 	 * 
 	 * @see org.eclipse.ui.part.EditorActionBarContributor
 	 */
 	@Override
 	public void contributeToMenu(IMenuManager menubar) {
 		super.contributeToMenu(menubar);
 		IMenuManager editMenu = menubar.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
 
 		if (editMenu != null) {
 			MenuManager alignments = new MenuManager(Messages.DiagramEditorActionBarContributor_0_xmen);
 			alignments.add(getAction(GEFActionConstants.ALIGN_LEFT));
 			alignments.add(getAction(GEFActionConstants.ALIGN_CENTER));
 			alignments.add(getAction(GEFActionConstants.ALIGN_RIGHT));
 			alignments.add(new Separator());
 			alignments.add(getAction(GEFActionConstants.ALIGN_TOP));
 			alignments.add(getAction(GEFActionConstants.ALIGN_MIDDLE));
 			alignments.add(getAction(GEFActionConstants.ALIGN_BOTTOM));
 			alignments.add(new Separator());
 			alignments.add(getAction(GEFActionConstants.MATCH_WIDTH));
 			alignments.add(getAction(GEFActionConstants.MATCH_HEIGHT));
 			editMenu.insertAfter(ActionFactory.SELECT_ALL.getId(), alignments);
 
 			editMenu.insertAfter(ActionFactory.DELETE.getId(), getAction(RemoveAction.ACTION_ID));
 			editMenu.insertAfter(RemoveAction.ACTION_ID, getAction(UpdateAction.ACTION_ID));
 		}
 
 		// Create view menu ...
 		MenuManager viewMenu = new MenuManager(Messages.GraphicsActionBarContributor_0_xmen);
 		viewMenu.add(getAction(GEFActionConstants.ZOOM_IN));
 		viewMenu.add(getAction(GEFActionConstants.ZOOM_OUT));
 		viewMenu.add(getAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY));
 		viewMenu.add(getAction(GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY));
 
 		// ... and add it. The position of the view menu differs depending on
 		// which menus exist (see Bugzilla
 		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=381437)
 		if (editMenu != null) {
 			// Edit menu exists --> place view menu directly in front of it
 			menubar.insertAfter(IWorkbenchActionConstants.M_EDIT, viewMenu);
 		} else if (menubar.findMenuUsingPath(IWorkbenchActionConstants.M_FILE) != null) {
 			// File menu exists --> place view menu behind it
 			menubar.insertAfter(IWorkbenchActionConstants.M_FILE, viewMenu);
 		} else {
 			// Add view menu as first entry
 			IContributionItem[] contributionItems = menubar.getItems();
 			if (contributionItems != null && contributionItems.length > 0) {
 				// Any menu exists --> place view menu in front of it it
 				menubar.insertBefore(contributionItems[0].getId(), viewMenu);
 			} else {
 				// No item exists --> simply add view menu
 				menubar.add(viewMenu);
 			}
 		}
 
 
 		IMenuManager fileMenu = menubar.findMenuUsingPath(IWorkbenchActionConstants.M_FILE);
 		if (fileMenu != null) {
 			// Might not be available in RCP case
			fileMenu.insertAfter(ActionFactory.EXPORT.getId(), getAction(SaveImageAction.ACTION_ID));
 		}
 	}

 }
