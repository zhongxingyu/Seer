 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *    Florian Pirchner - FontDescriptor
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.lnf.rienadefault;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Display;
 
 import org.eclipse.riena.ui.swt.lnf.ColorLnfResource;
 import org.eclipse.riena.ui.swt.lnf.FontLnfResource;
 import org.eclipse.riena.ui.swt.lnf.ILnfCustomizer;
 import org.eclipse.riena.ui.swt.lnf.ILnfTheme;
 import org.eclipse.riena.ui.swt.lnf.ImageLnfResource;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 
 /**
  * Default theme of Riena used by the default Look and Feel,
  * <code>RienaDefaultLnf</code>.
  */
 public class RienaDefaultTheme implements ILnfTheme {
 
 	private static final String IMAGE_FOLDER = "node_expanded.gif"; //$NON-NLS-1$
 	private static final String IMAGE_FOLDER_CLOSED = "node_collapsed.gif"; //$NON-NLS-1$
 	private static final String IMAGE_EMPTY_DOCUMENT = "leaf.gif"; //$NON-NLS-1$
 	private static final String IMAGE_CLOSE_MODULE = "closeModule"; //$NON-NLS-1$
 	private static final String IMAGE_CLOSE_MODULE_HOVER = "closeModule_hover"; //$NON-NLS-1$
 	private static final String IMAGE_CLOSE_MODULE_HOVER_SELECTED = "closeModule_hover_selected"; //$NON-NLS-1$
 	private static final String IMAGE_CLOSE_MODULE_INACTIVE = "closeModule_inactive"; //$NON-NLS-1$
 	private static final String IMAGE_BACKGROUND = "background"; //$NON-NLS-1$
 	private static final String IMAGE_LOGO = "RIENA_Logo_RGB"; //$NON-NLS-1$
 	private static final String IMAGE_CLOSE = "mb_close.gif"; //$NON-NLS-1$
 	private static final String IMAGE_CLOSE_HOVER = "mb_close_hover.gif"; //$NON-NLS-1$
 	private static final String IMAGE_CLOSE_HOVER_SELECTED = "mb_close_hover_selected.gif"; //$NON-NLS-1$
 	private static final String IMAGE_CLOSE_INACTIVE = "mb_close_inactive.gif"; //$NON-NLS-1$
 	private static final String IMAGE_MAX = "mb_max.gif"; //$NON-NLS-1$
 	private static final String IMAGE_MAX_HOVER = "mb_max_hover.gif"; //$NON-NLS-1$
 	private static final String IMAGE_MAX_HOVER_SELECTED = "mb_max_hover_selected.gif"; //$NON-NLS-1$
 	private static final String IMAGE_MAX_INACTIVE = "mb_max_inactive.gif"; //$NON-NLS-1$
 	private static final String IMAGE_MIN = "mb_min.gif"; //$NON-NLS-1$
 	private static final String IMAGE_MIN_HOVER = "mb_min_hover.gif"; //$NON-NLS-1$
 	private static final String IMAGE_MIN_HOVER_SELECTED = "mb_min_hover_selected.gif"; //$NON-NLS-1$
 	private static final String IMAGE_MIN_INACTIVE = "mb_min_inactive.gif"; //$NON-NLS-1$
 	private static final String IMAGE_RESTORE = "mb_restore.gif"; //$NON-NLS-1$
 	private static final String IMAGE_RESTORE_HOVER = "mb_restore_hover.gif"; //$NON-NLS-1$
 	private static final String IMAGE_RESTORE_HOVER_SELECTED = "mb_restore_hover_selected.gif"; //$NON-NLS-1$
 	private static final String IMAGE_RESTORE_INACTIVE_ICON = "mb_restore_inactive.gif"; //$NON-NLS-1$
 	private static final String IMAGE_HAND = "hand.gif"; //$NON-NLS-1$
 	private static final String IMAGE_GRAB = "grab.gif"; //$NON-NLS-1$
 	private static final String IMAGE_GRAB_CORNER = "grabCorner"; //$NON-NLS-1$
 	private static final String DIALOG_IMAGE_GRAB_CORNER = "dialogGrabCorner"; //$NON-NLS-1$
 	private static final String IMAGE_SPACER = "spacer"; //$NON-NLS-1$
 	private static final String IMAGE_ERROR = "statusline_error.gif"; //$NON-NLS-1$
 	private static final String IMAGE_WARNING = "statusline_warning.gif"; //$NON-NLS-1$
 	private static final String IMAGE_INFO = "statusline_info.gif"; //$NON-NLS-1$
 	private static final String IMAGE_ERROR_MARKER = "errorMarker"; //$NON-NLS-1$
 	private static final String IMAGE_MANDATORY_MARKER = "mandatoryMarker"; //$NON-NLS-1$
 	private static final String IMAGE_ATTENTION_MARKER = "attentionMarker"; //$NON-NLS-1$
 	private static final String IMAGE_PROCESS_FINISHED_MARKER = "processFinishedMarker"; //$NON-NLS-1$
 	private static final String IMAGE_SCROLL_UP = "arrowUp.gif"; //$NON-NLS-1$
 	private static final String IMAGE_SCROLL_DOWN = "arrowDown.gif"; //$NON-NLS-1$
 	private static final String IMAGE_DEFAULT_NODE_ICON = "defaultNode.png"; //$NON-NLS-1$
 	private static final String IMAGE_STATUSLINE_UI_PROCESS_ICON = "uiProcess.png"; //$NON-NLS-1$
 	private FontLnfResource primaryFont;
 	private ColorLnfResource primaryBackground;
 	private ColorLnfResource primaryForeground;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void customizeLnf(final ILnfCustomizer lnf) {
 		customizeColors(lnf);
 		customizeSWTControls(lnf);
 		customizeFonts(lnf);
 		customizeImages(lnf);
 		customizeSettings(lnf);
 	}
 
 	private void customizeColors(final ILnfCustomizer lnf) {
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_FOREGROUND, new ColorLnfResource(255, 255, 255));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_PASSIVE_FOREGROUND, new ColorLnfResource(128, 128, 128));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_BACKGROUND, getPrimaryBackground());
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_BORDER_BOTTOM_RIGHT_COLOR, new ColorLnfResource(83, 85, 94));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_BORDER_TOP_LEFT_COLOR, new ColorLnfResource(121, 124, 137));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_INNER_BORDER_TOP_LEFT_COLOR, new ColorLnfResource(173, 180,
 				205));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_INNER_BORDER_BOTTOM_RIGHT_COLOR, new ColorLnfResource(161,
 				168, 190));
 
 		lnf.putLnfResource(LnfKeyConstants.COOLBAR_BACKGROUND, getPrimaryBackground());
 
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_FOREGROUND, new ColorLnfResource(255, 255, 255));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_PASSIVE_FOREGROUND, new ColorLnfResource(128, 128, 128));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_BORDER_BOTTOM_RIGHT_COLOR, new ColorLnfResource(83, 85, 94));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_BORDER_TOP_LEFT_COLOR, new ColorLnfResource(121, 124, 137));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_INNER_BORDER_TOP_LEFT_COLOR, new ColorLnfResource(173, 180, 205));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_INNER_BORDER_BOTTOM_RIGHT_COLOR, new ColorLnfResource(161, 168, 190));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_TITLEBAR_BACKGROUND_START_COLOR, new ColorLnfResource(161, 176, 218));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_TITLEBAR_BACKGROUND_END_COLOR, new ColorLnfResource(124, 153, 205));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_TITLEBAR_BACKGROUND_TOP_COLOR_1, new ColorLnfResource(188, 201, 229));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_TITLEBAR_BACKGROUND_TOP_COLOR_2, new ColorLnfResource(158, 178, 218));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_TITLEBAR_BACKGROUND_TOP_COLOR_3, new ColorLnfResource(139, 163, 210));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_TITLEBAR_BACKGROUND_BOTTOM_COLOR_1,
 				new ColorLnfResource(99, 126, 175));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_TITLEBAR_BACKGROUND_BOTTOM_COLOR_2, new ColorLnfResource(139, 163,
 				210));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_TITLEBAR_BACKGROUND_BOTTOM_COLOR_3, new ColorLnfResource(164, 183,
 				220));
 
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_DISABLED_FOREGROUND, new ColorLnfResource(170, 170, 170));
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_DISABLED_BORDER_COLOR, new ColorLnfResource(233, 233, 238));
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_ACTIVE_FOREGROUND, getPrimaryForeground());
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_ACTIVE_BACKGROUND_START_COLOR, new ColorLnfResource(196,
 				225, 244));
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_ACTIVE_BACKGROUND_END_COLOR, new ColorLnfResource(100,
 				153, 186));
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_ACTIVE_BORDER_COLOR, new ColorLnfResource(171, 171, 174));
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_PASSIVE_FOREGROUND, getPrimaryForeground());
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_PASSIVE_BACKGROUND_START_COLOR, new ColorLnfResource(244,
 				244, 245));
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_PASSIVE_BACKGROUND_END_COLOR, new ColorLnfResource(220,
 				220, 220));
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_PASSIVE_BORDER_COLOR, new ColorLnfResource(213, 213, 216));
 
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_HOVER_BORDER_TOP_COLOR,
 				new ColorLnfResource(251, 233, 168));
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_HOVER_BORDER_BOTTOM_COLOR, new ColorLnfResource(192, 151,
 				1));
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_HOVER_BORDER_START_COLOR, new ColorLnfResource(255, 207,
 				32));
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_HOVER_BORDER_END_COLOR, new ColorLnfResource(255, 176, 1));
 
 		lnf.putLnfResource(LnfKeyConstants.NAVIGATION_BACKGROUND, getPrimaryBackground());
 		lnf.putLnfResource(LnfKeyConstants.MODULE_GROUP_WIDGET_BACKGROUND, getPrimaryBackground());
 		lnf.putLnfResource(LnfKeyConstants.MODULE_GROUP_ACTIVE_BORDER_COLOR, new ColorLnfResource(171, 171, 174));
 		lnf.putLnfResource(LnfKeyConstants.MODULE_GROUP_PASSIVE_BORDER_COLOR, new ColorLnfResource(213, 213, 216));
 		lnf.putLnfResource(LnfKeyConstants.MODULE_GROUP_DISABLED_BORDER_COLOR, new ColorLnfResource(233, 233, 238));
 		lnf.putLnfResource(LnfKeyConstants.SUB_MODULE_TREE_BACKGROUND, getPrimaryBackground());
 		lnf.putLnfResource(LnfKeyConstants.SUB_MODULE_BACKGROUND, getPrimaryBackground());
 
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_ACTIVE_FOREGROUND, getPrimaryForeground());
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_PASSIVE_FOREGROUND, getPrimaryForeground());
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_DISABLED_FOREGROUND, new ColorLnfResource(170, 170,
 				170));
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_TOP_SELECTION_COLOR, new ColorLnfResource(64, 132,
 				191));
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_ACTIVE_BACKGROUND_START_COLOR,
 				new ColorLnfResource(255, 255, 255));
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_ACTIVE_BACKGROUND_END_COLOR, new ColorLnfResource(
 				255, 255, 255));
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_PROCESS_FINISHED_BACKGROUND_START_COLOR,
 				new ColorLnfResource(255, 255, 255));
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_PROCESS_FINISHED_BACKGROUND_END_COLOR,
 				new ColorLnfResource(255, 255, 255));
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_PASSIVE_BACKGROUND_START_COLOR,
 				new ColorLnfResource(245, 245, 245));
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_PASSIVE_BACKGROUND_END_COLOR, new ColorLnfResource(
 				229, 229, 229));
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_INNER_BORDER_COLOR, new ColorLnfResource(245, 245,
 				245));
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_BORDER_TOP_RIGHT_COLOR, new ColorLnfResource(206,
 				206, 206));
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_BORDER_TOP_RIGHT_SECONDARY_COLOR,
 				new ColorLnfResource(240, 240, 240));
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_BORDER_BOTTOM_LEFT_COLOR, new ColorLnfResource(183,
 				183, 183));
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_BORDER_BOTTOM_LEFT_SECONDARY_COLOR,
 				new ColorLnfResource(230, 230, 230));
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_INNER_DISABLED_BORDER_COLOR, new ColorLnfResource(
 				245, 245, 245));
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_INNER_PROCESS_FINISHED_BORDER_COLOR,
 				new ColorLnfResource(245, 245, 245));
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_DISABLED_BORDER_TOP_RIGHT_COLOR,
 				new ColorLnfResource(226, 226, 226));
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_DISABLED_BORDER_BOTTOM_LEFT_COLOR,
 				new ColorLnfResource(203, 203, 203));
 
 		lnf.putLnfResource(LnfKeyConstants.SUB_MODULE_ITEM_TOOLTIP_BACKGROUND, getPrimaryBackground());
 		lnf.putLnfResource(LnfKeyConstants.MODULE_ITEM_TOOLTIP_BACKGROUND, getPrimaryBackground());
 
 		lnf.putLnfResource(LnfKeyConstants.STATUSLINE_BACKGROUND, getPrimaryBackground());
 		lnf.putLnfResource(LnfKeyConstants.STATUSLINE_UI_PROCESS_LIST_BACKGROUND, new ColorLnfResource(183, 216, 236));
 
 		lnf.putLnfResource(LnfKeyConstants.GRAB_CORNER_BACKGROUND, getPrimaryBackground());
 		lnf.putLnfResource(LnfKeyConstants.MANDATORY_MARKER_BACKGROUND, new ColorLnfResource(255, 255, 175));
 		lnf.putLnfResource(LnfKeyConstants.MANDATORY_OUTPUT_MARKER_BACKGROUND, new ColorLnfResource(255, 249, 216));
 		lnf.putLnfResource(LnfKeyConstants.ERROR_MARKER_BORDER_COLOR, new ColorLnfResource(200, 0, 0));
 
 		lnf.putLnfResource(LnfKeyConstants.OUTPUT_MARKER_BACKGROUND, new ColorLnfResource(231, 233, 245));
 
 		lnf.putLnfResource(LnfKeyConstants.DISABLED_MARKER_BACKGROUND, getPrimaryBackground());
 
 		lnf.putLnfResource(LnfKeyConstants.MASTER_DETAILS_WIDGET_SEPARATOR_FOREGROUND, new ColorLnfResource(171, 173,
 				179));
 
 		lnf.putLnfResource(LnfKeyConstants.INFO_FLYOUT_BACKGROUND_COLOR, new ColorLnfResource(222, 237, 244));
 
 		lnf.putLnfResource(LnfKeyConstants.INFO_FLYOUT_BORDER_COLOR, new ColorLnfResource(150, 165, 170));
 
 		lnf.putLnfResource(LnfKeyConstants.TITLEBAR_SEPARATOR_FIRST_LINE_FOREGROUND,
 				new ColorLnfResource(212, 208, 200));
 		lnf.putLnfResource(LnfKeyConstants.TITLEBAR_SEPARATOR_SECOND_LINE_FOREGROUND, new ColorLnfResource(245, 245,
 				245));
 
 		// StatusMeter
 		lnf.putLnfResource(LnfKeyConstants.STATUS_METER_BORDER_COLOR, new ColorLnfResource(128, 128, 128));
 		lnf.putLnfResource(LnfKeyConstants.STATUS_METER_BACKGROUND_COLOR, new ColorLnfResource(255, 255, 255));
 		lnf.putLnfResource(LnfKeyConstants.STATUS_METER_GRADIENT_START_COLOR, new ColorLnfResource(255, 255, 255));
 		lnf.putLnfResource(LnfKeyConstants.STATUS_METER_GRADIENT_END_COLOR, new ColorLnfResource(0, 0, 128));
 		lnf.putLnfResource(LnfKeyConstants.STATUS_METER_TRANSPARENT_COLOR, new ColorLnfResource(255, 0, 255));
 	}
 
 	private void customizeSWTControls(final ILnfCustomizer lnf) {
 		// Standard SWT controls
 		lnf.putLnfResource("Button.background", getPrimaryBackground()); //$NON-NLS-1$
 		lnf.putLnfResource("Button.foreground", getPrimaryForeground()); // Windows: no effect //$NON-NLS-1$
 
 		lnf.putLnfResource("CLabel.background", getPrimaryBackground()); //$NON-NLS-1$
 		lnf.putLnfResource("CLabel.foreground", getPrimaryForeground()); //$NON-NLS-1$
 
 		lnf.putLnfResource("Combo.background", getPrimaryBackground()); //$NON-NLS-1$
 		lnf.putLnfResource("Combo.foreground", getPrimaryForeground()); //$NON-NLS-1$
 
 		lnf.putLnfResource("Composite.background", getPrimaryBackground()); //$NON-NLS-1$
 		lnf.putLnfResource("Composite.foreground", getPrimaryForeground()); //$NON-NLS-1$
 
 		lnf.putLnfResource("DateTime.background", getPrimaryBackground()); // no effect //$NON-NLS-1$
 		lnf.putLnfResource("DateTime.foreground", getPrimaryForeground()); // no effect //$NON-NLS-1$
 
 		lnf.putLnfResource("Group.background", getPrimaryBackground()); //$NON-NLS-1$
 		lnf.putLnfResource("Group.foreground", getPrimaryForeground()); //$NON-NLS-1$
 
 		lnf.putLnfResource("Label.background", getPrimaryBackground()); //$NON-NLS-1$
 		lnf.putLnfResource("Label.foreground", getPrimaryForeground()); //$NON-NLS-1$
 
 		lnf.putLnfResource("List.background", getPrimaryBackground()); // no effect //$NON-NLS-1$
 		lnf.putLnfResource("List.foreground", getPrimaryForeground()); //$NON-NLS-1$
 
 		lnf.putLnfResource("Slider.background", getPrimaryBackground()); // no effect //$NON-NLS-1$
 		lnf.putLnfResource("Slider.foreground", getPrimaryForeground()); //$NON-NLS-1$
 
 		lnf.putLnfResource("Table.background", getPrimaryBackground()); //$NON-NLS-1$
 		lnf.putLnfResource("Table.foreground", getPrimaryForeground()); //$NON-NLS-1$
 
 		lnf.putLnfResource("Text.background", new ColorLnfResource(null)); //$NON-NLS-1$
 		lnf.putLnfResource("Text.foreground", getPrimaryForeground()); //$NON-NLS-1$
 
 		lnf.putLnfResource("Tree.background", getPrimaryBackground()); //$NON-NLS-1$
 		lnf.putLnfResource("Tree.foreground", getPrimaryForeground()); //$NON-NLS-1$
 
 		lnf.putLnfResource("CCombo.background", getPrimaryBackground()); //$NON-NLS-1$
 		lnf.putLnfResource("CCombo.foreground", getPrimaryForeground()); //$NON-NLS-1$
 
 		lnf.putLnfResource("CompletionCombo.background", getPrimaryBackground()); //$NON-NLS-1$
 		lnf.putLnfResource("CompletionCombo.foreground", getPrimaryForeground()); //$NON-NLS-1$
 
 		lnf.putLnfResource("DatePickerComposite.background", getPrimaryBackground()); // no effect //$NON-NLS-1$
 		lnf.putLnfResource("DatePickerComposite.foreground", getPrimaryForeground()); // no effect //$NON-NLS-1$
 
 		lnf.putLnfResource("white", new ColorLnfResource(255, 255, 255)); //$NON-NLS-1$
 		lnf.putLnfResource("lightGray", new ColorLnfResource(192, 192, 192)); //$NON-NLS-1$
 		lnf.putLnfResource("gray", new ColorLnfResource(128, 128, 128)); //$NON-NLS-1$
 		lnf.putLnfResource("darkGray", new ColorLnfResource(64, 64, 64)); //$NON-NLS-1$
 		lnf.putLnfResource("black", new ColorLnfResource(0, 0, 0)); //$NON-NLS-1$
 		lnf.putLnfResource("red", new ColorLnfResource(255, 0, 0)); //$NON-NLS-1$
 		lnf.putLnfResource("pink", new ColorLnfResource(255, 175, 175)); //$NON-NLS-1$
 		lnf.putLnfResource("orange", new ColorLnfResource(255, 200, 0)); //$NON-NLS-1$
 		lnf.putLnfResource("yellow", new ColorLnfResource(255, 255, 0)); //$NON-NLS-1$
 		lnf.putLnfResource("green", new ColorLnfResource(0, 255, 0)); //$NON-NLS-1$
 		lnf.putLnfResource("magenta", new ColorLnfResource(255, 0, 255)); //$NON-NLS-1$
 		lnf.putLnfResource("cyan", new ColorLnfResource(0, 255, 255)); //$NON-NLS-1$
 		lnf.putLnfResource("blue", new ColorLnfResource(0, 0, 255)); //$NON-NLS-1$
 
 		lnf.putLnfResource("Link.background", getPrimaryBackground()); //$NON-NLS-1$
 	}
 
 	private void customizeFonts(final ILnfCustomizer lnf) {
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_FONT, getPrimaryFont());
 
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_FONT, getPrimaryFont());
 
 		lnf.putLnfResource(LnfKeyConstants.MENUBAR_FONT, getPrimaryFont());
 		lnf.putLnfResource(LnfKeyConstants.TOOLBAR_FONT, getPrimaryFont());
 
 		lnf.putLnfResource(LnfKeyConstants.SUB_APPLICATION_SWITCHER_FONT, getPrimaryFont());
 
 		lnf.putLnfResource(LnfKeyConstants.MODULE_ITEM_TOOLTIP_FONT, getPrimaryFont());
 
 		lnf.putLnfResource(LnfKeyConstants.SUB_MODULE_ITEM_FONT, getPrimaryFont());
 		lnf.putLnfResource(LnfKeyConstants.SUB_MODULE_ITEM_TOOLTIP_FONT, getPrimaryFont());
 
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_FONT, getPrimaryFont());
 
 		lnf.putLnfResource(LnfKeyConstants.INFO_FLYOUT_FONT, new FontLnfResource(getPrimaryFont().getFontData()
 				.getName(), 8, SWT.BOLD));
 
 		// Standard SWT controls
 		lnf.putLnfResource("Button.font", getPrimaryFont()); //$NON-NLS-1$
 
 		lnf.putLnfResource("CLabel.font", getPrimaryFont()); //$NON-NLS-1$
 
 		lnf.putLnfResource("Combo.font", getPrimaryFont()); //$NON-NLS-1$
 
 		lnf.putLnfResource("Composite.font", getPrimaryFont()); //$NON-NLS-1$
 
 		lnf.putLnfResource("DateTime.font", getPrimaryFont()); //$NON-NLS-1$
 
 		lnf.putLnfResource("Group.font", getPrimaryFont()); //$NON-NLS-1$
 
 		lnf.putLnfResource("Label.font", getPrimaryFont()); //$NON-NLS-1$
 
 		lnf.putLnfResource("List.font", getPrimaryFont()); //$NON-NLS-1$
 
 		lnf.putLnfResource("Slider.font", getPrimaryFont()); //$NON-NLS-1$
 
 		lnf.putLnfResource("Table.font", getPrimaryFont()); //$NON-NLS-1$
 
 		lnf.putLnfResource("Text.font", getPrimaryFont()); //$NON-NLS-1$
 
 		lnf.putLnfResource("Tree.font", getPrimaryFont()); //$NON-NLS-1$
 
 		lnf.putLnfResource("Link.font", getPrimaryFont()); //$NON-NLS-1$
 
 		lnf.putLnfResource("CCombo.font", getPrimaryFont()); //$NON-NLS-1$
 
 		lnf.putLnfResource("CompletionCombo.font", getPrimaryFont()); //$NON-NLS-1$
 
 		lnf.putLnfResource("DatePickerComposite.font", getPrimaryFont()); //$NON-NLS-1$
 
 	}
 
 	private void customizeImages(final ILnfCustomizer lnf) {
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_BACKGROUND_IMAGE, new ImageLnfResource(IMAGE_BACKGROUND));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_LOGO, new ImageLnfResource(IMAGE_LOGO));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_CLOSE_ICON, new ImageLnfResource(IMAGE_CLOSE));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_CLOSE_HOVER_ICON, new ImageLnfResource(IMAGE_CLOSE_HOVER));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_CLOSE_HOVER_SELECTED_ICON, new ImageLnfResource(
 				IMAGE_CLOSE_HOVER_SELECTED));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_CLOSE_INACTIVE_ICON, new ImageLnfResource(
 				IMAGE_CLOSE_INACTIVE));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_MAX_ICON, new ImageLnfResource(IMAGE_MAX));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_MAX_HOVER_ICON, new ImageLnfResource(IMAGE_MAX_HOVER));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_MAX_HOVER_SELECTED_ICON, new ImageLnfResource(
 				IMAGE_MAX_HOVER_SELECTED));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_MAX_INACTIVE_ICON, new ImageLnfResource(IMAGE_MAX_INACTIVE));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_MIN_ICON, new ImageLnfResource(IMAGE_MIN));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_MIN_HOVER_ICON, new ImageLnfResource(IMAGE_MIN_HOVER));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_MIN_HOVER_SELECTED_ICON, new ImageLnfResource(
 				IMAGE_MIN_HOVER_SELECTED));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_MIN_INACTIVE_ICON, new ImageLnfResource(IMAGE_MIN_INACTIVE));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_RESTORE_ICON, new ImageLnfResource(IMAGE_RESTORE));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_RESTORE_HOVER_ICON,
 				new ImageLnfResource(IMAGE_RESTORE_HOVER));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_RESTORE_HOVER_SELECTED_ICON, new ImageLnfResource(
 				IMAGE_RESTORE_HOVER_SELECTED));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_RESTORE_INACTIVE_ICON, new ImageLnfResource(
 				IMAGE_RESTORE_INACTIVE_ICON));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_HAND_IMAGE, new ImageLnfResource(IMAGE_HAND));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_GRAB_IMAGE, new ImageLnfResource(IMAGE_GRAB));
 		lnf.putLnfResource(LnfKeyConstants.TITLELESS_SHELL_GRAB_CORNER_IMAGE, new ImageLnfResource(IMAGE_GRAB_CORNER));
 
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_CLOSE_ICON, new ImageLnfResource(IMAGE_CLOSE));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_CLOSE_HOVER_ICON, new ImageLnfResource(IMAGE_CLOSE_HOVER));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_CLOSE_HOVER_SELECTED_ICON, new ImageLnfResource(
 				IMAGE_CLOSE_HOVER_SELECTED));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_CLOSE_INACTIVE_ICON, new ImageLnfResource(IMAGE_CLOSE_INACTIVE));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_MAX_ICON, new ImageLnfResource(IMAGE_MAX));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_MAX_HOVER_ICON, new ImageLnfResource(IMAGE_MAX_HOVER));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_MAX_HOVER_SELECTED_ICON, new ImageLnfResource(
 				IMAGE_MAX_HOVER_SELECTED));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_MAX_INACTIVE_ICON, new ImageLnfResource(IMAGE_MAX_INACTIVE));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_MIN_ICON, new ImageLnfResource(IMAGE_MIN));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_MIN_HOVER_ICON, new ImageLnfResource(IMAGE_MIN_HOVER));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_MIN_HOVER_SELECTED_ICON, new ImageLnfResource(
 				IMAGE_MIN_HOVER_SELECTED));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_MIN_INACTIVE_ICON, new ImageLnfResource(IMAGE_MIN_INACTIVE));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_RESTORE_ICON, new ImageLnfResource(IMAGE_RESTORE));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_RESTORE_HOVER_ICON, new ImageLnfResource(IMAGE_RESTORE_HOVER));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_RESTORE_HOVER_SELECTED_ICON, new ImageLnfResource(
 				IMAGE_RESTORE_HOVER_SELECTED));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_RESTORE_INACTIVE_ICON, new ImageLnfResource(
 				IMAGE_RESTORE_INACTIVE_ICON));
 		lnf.putLnfResource(LnfKeyConstants.DIALOG_GRAB_CORNER_IMAGE, new ImageLnfResource(DIALOG_IMAGE_GRAB_CORNER));
 
		lnf.putLnfResource(LnfKeyConstants.DEFAULT_NODE_ICON, new ImageLnfResource(IMAGE_DEFAULT_NODE_ICON));
 		lnf.putLnfResource(LnfKeyConstants.SUB_MODULE_TREE_DOCUMENT_LEAF_ICON, new ImageLnfResource(
 				IMAGE_EMPTY_DOCUMENT));
 		lnf.putLnfResource(LnfKeyConstants.WORKAREA_TREE_DOCUMENT_LEAF_ICON, new ImageLnfResource(IMAGE_EMPTY_DOCUMENT));
 
 		lnf.putLnfResource(LnfKeyConstants.SUB_MODULE_TREE_FOLDER_CLOSED_ICON,
 				new ImageLnfResource(IMAGE_FOLDER_CLOSED));
 
 		lnf.putLnfResource(LnfKeyConstants.WORKAREA_TREE_FOLDER_CLOSED_ICON, new ImageLnfResource(IMAGE_FOLDER_CLOSED));
 
 		lnf.putLnfResource(LnfKeyConstants.SUB_MODULE_TREE_ERROR_MARKER_ICON, new ImageLnfResource(IMAGE_ERROR_MARKER));
 		lnf.putLnfResource(LnfKeyConstants.SUB_MODULE_TREE_MANDATORY_MARKER_ICON, new ImageLnfResource(
 				IMAGE_MANDATORY_MARKER));
 		lnf.putLnfResource(LnfKeyConstants.SUB_MODULE_TREE_ATTENTION_MARKER_ICON, new ImageLnfResource(
 				IMAGE_ATTENTION_MARKER));
 		lnf.putLnfResource(LnfKeyConstants.SUB_MODULE_TREE_PROCESSED_FINISHED_MARKER_ICON, new ImageLnfResource(
 				IMAGE_PROCESS_FINISHED_MARKER));
 		// lnf.putLnfResource(LnfKeyConstants."treeFolderOpen.icon",
 		// getSharedImageResource(ISharedImages.IMG_OBJ_FOLDER));
 		lnf.putLnfResource(LnfKeyConstants.SUB_MODULE_TREE_FOLDER_OPEN_ICON, new ImageLnfResource(IMAGE_FOLDER));
 		lnf.putLnfResource(LnfKeyConstants.WORKAREA_TREE_FOLDER_OPEN_ICON, new ImageLnfResource(IMAGE_FOLDER));
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_CLOSE_ICON, new ImageLnfResource(IMAGE_CLOSE_MODULE));
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_CLOSE_HOVER_ICON, new ImageLnfResource(
 				IMAGE_CLOSE_MODULE_HOVER));
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_CLOSE_INACTIVE_ICON, new ImageLnfResource(
 				IMAGE_CLOSE_MODULE_INACTIVE));
 		lnf.putLnfResource(LnfKeyConstants.EMBEDDED_TITLEBAR_CLOSE_HOVER_SELECTED_ICON, new ImageLnfResource(
 				IMAGE_CLOSE_MODULE_HOVER_SELECTED));
 
 		lnf.putLnfResource(LnfKeyConstants.STATUSLINE_SPACER_ICON, new ImageLnfResource(IMAGE_SPACER));
 		lnf.putLnfResource(LnfKeyConstants.STATUSLINE_ERROR_ICON, new ImageLnfResource(IMAGE_ERROR));
 		lnf.putLnfResource(LnfKeyConstants.STATUSLINE_WARNING_ICON, new ImageLnfResource(IMAGE_WARNING));
 		lnf.putLnfResource(LnfKeyConstants.STATUSLINE_INFO_ICON, new ImageLnfResource(IMAGE_INFO));
 		lnf.putLnfResource(LnfKeyConstants.STATUSLINE_UI_PROCESS_ICON, new ImageLnfResource(
 				IMAGE_STATUSLINE_UI_PROCESS_ICON));
 
 		lnf.putLnfResource(LnfKeyConstants.NAVIGATION_SCROLL_UP_ICON, new ImageLnfResource(IMAGE_SCROLL_UP));
 		lnf.putLnfResource(LnfKeyConstants.NAVIGATION_SCROLL_DOWN_ICON, new ImageLnfResource(IMAGE_SCROLL_DOWN));
 
 		lnf.putLnfResource(LnfKeyConstants.ERROR_MARKER_ICON, new ImageLnfResource(IMAGE_ERROR_MARKER));
 	}
 
 	private void customizeSettings(final ILnfCustomizer lnf) {
 		lnf.putLnfSetting(LnfKeyConstants.SHELL_HIDE_OS_BORDER, hideOsBorder());
 
 		lnf.putLnfSetting(LnfKeyConstants.TITLELESS_SHELL_PADDING, 2);
 		lnf.putLnfSetting(LnfKeyConstants.TITLELESS_SHELL_NAVIGATION_HORIZONTAL_GAP, 5);
 		lnf.putLnfSetting(LnfKeyConstants.TITLELESS_SHELL_SUB_MODULE_HORIZONTAL_GAP, 5);
 		lnf.putLnfSetting(LnfKeyConstants.TITLELESS_SHELL_HORIZONTAL_LOGO_POSITION, SWT.LEFT);
 		lnf.putLnfSetting(LnfKeyConstants.TITLELESS_SHELL_VERTICAL_LOGO_POSITION, SWT.CENTER);
 		lnf.putLnfSetting(LnfKeyConstants.TITLELESS_SHELL_HORIZONTAL_LOGO_MARGIN, 17);
 		lnf.putLnfSetting(LnfKeyConstants.TITLELESS_SHELL_VERTICAL_LOGO_MARGIN, 4);
 		lnf.putLnfSetting(LnfKeyConstants.TITLELESS_SHELL_HORIZONTAL_TEXT_POSITION, SWT.RIGHT);
 		lnf.putLnfSetting(LnfKeyConstants.TITLELESS_SHELL_SHOW_CLOSE, true);
 		lnf.putLnfSetting(LnfKeyConstants.TITLELESS_SHELL_SHOW_MAX, true);
 		lnf.putLnfSetting(LnfKeyConstants.TITLELESS_SHELL_SHOW_MIN, true);
 		lnf.putLnfSetting(LnfKeyConstants.TITLELESS_SHELL_RESIZEABLE, true);
 
 		//		lnf.putLnfSetting(LnfKeyConstants.SUB_MODULE_TREE_MARKER_HIERARCHIC_ORDER_POSITION,
 		//				IIconizableMarker.MarkerPosition.BOTTOM_RIGHT);
 
 		lnf.putLnfSetting(LnfKeyConstants.APPLICATION_MIN_SIZE, new Point(800, 600));
 
 		lnf.putLnfSetting(LnfKeyConstants.TOOLBAR_WORK_AREA_VERTICAL_GAP, 5);
 
 		lnf.putLnfSetting(LnfKeyConstants.NAVIGATION_WIDTH, 165);
 		lnf.putLnfSetting(LnfKeyConstants.NAVIGATION_SCROLL_BUTTON_HEIGHT, 14);
 		lnf.putLnfSetting(LnfKeyConstants.NAVIGATION_SCROLL_BAR, false);
 
 		lnf.putLnfSetting(LnfKeyConstants.STATUSLINE_HEIGHT, 20);
 
 		lnf.putLnfSetting(LnfKeyConstants.DIALOG_HIDE_OS_BORDER, hideOsBorder());
 
 		lnf.putLnfSetting(LnfKeyConstants.SUB_APPLICATION_SWITCHER_TOP_MARGIN, 22);
 		lnf.putLnfSetting(LnfKeyConstants.SUB_APPLICATION_SWITCHER_HEIGHT, 40);
 		lnf.putLnfSetting(LnfKeyConstants.SUB_APPLICATION_SWITCHER_HORIZONTAL_TAB_POSITION, SWT.CENTER);
 		lnf.putLnfSetting(LnfKeyConstants.SUB_APPLICATION_SWITCHER_TAB_SHOW_ICON, false);
 		lnf.putLnfSetting(LnfKeyConstants.SUB_APPLICATION_SWITCHER_TAB_MIN_WIDTH, 0);
 
 		lnf.putLnfSetting(LnfKeyConstants.SUB_MODULE_TREE_SHOW_ONE_SUB_TREE, false);
 		lnf.putLnfSetting(LnfKeyConstants.SUB_MODULE_TREE_DISABLED_BACKGROUND_IS_SWT_DEFAULT, false);
 
 		lnf.putLnfSetting(LnfKeyConstants.SUB_MODULE_ITEM_TOOLTIP_POPUP_DELAY, 0);
 		lnf.putLnfSetting(LnfKeyConstants.MODULE_ITEM_TOOLTIP_POPUP_DELAY, 500);
 
 		lnf.putLnfSetting(LnfKeyConstants.EMBEDDED_TITLEBAR_HOVER_BORDER_MARGIN, 0);
 
 		lnf.putLnfSetting(LnfKeyConstants.NAVIGATION_SUB_MODULE_GAP, 5);
 		lnf.putLnfSetting(LnfKeyConstants.MENUBAR_TOP_MARGIN, 0);
 		lnf.putLnfSetting(LnfKeyConstants.TOOLBAR_TOP_MARGIN, 0);
 
 		lnf.putLnfSetting(LnfKeyConstants.ERROR_MARKER_HORIZONTAL_POSITION, SWT.LEFT);
 		lnf.putLnfSetting(LnfKeyConstants.ERROR_MARKER_VERTICAL_POSITION, SWT.TOP);
 		lnf.putLnfSetting(LnfKeyConstants.ERROR_MARKER_MARGIN, 1);
 		lnf.putLnfSetting(LnfKeyConstants.ROW_ERROR_MARKER_BORDER_THICKNESS, 1);
 
 		lnf.putLnfSetting(LnfKeyConstants.FONTDESCRIPTOR_DEFAULT_HEIGHT, 8);
 
 		lnf.putLnfSetting(LnfKeyConstants.DISABLED_MARKER_ADVANCED, true);
 		lnf.putLnfSetting(LnfKeyConstants.DISABLED_MARKER_STANDARD_ALPHA, 90);
 		// lnf.putLnfSetting(LnfKeyConstants.DISABLED_MARKER_COMPLEX_ALPHA, 190);
 		lnf.putLnfSetting(LnfKeyConstants.DISABLED_MARKER_HIDE_CONTENT, true);
 
 		// lnf.putLnfSetting(LnfKeyConstants.MARKER_SUPPORT_ID, "defaultMarkerSupport"); //$NON-NLS-1$
 		lnf.putLnfSetting(LnfKeyConstants.MARKER_SUPPORT_ID, "borderMarkerSupport"); //$NON-NLS-1$
 		lnf.putLnfSetting(LnfKeyConstants.ERROR_MARKER_BORDER_WIDTH, 2);
 
 		lnf.putLnfSetting(LnfKeyConstants.INFO_FLYOUT_PAUSE_ANIMATION_TIME, 1000);
 		lnf.putLnfSetting(LnfKeyConstants.INFO_FLYOUT_SHOW_AND_HIDE_ANIMATION_TIME, 1500);
 		lnf.putLnfSetting(LnfKeyConstants.INFO_FLYOUT_WAIT_ANIMATION_TIME, 2500);
 		lnf.putLnfSetting(LnfKeyConstants.INFO_FLYOUT_WIDTH, 300);
 		lnf.putLnfSetting(LnfKeyConstants.INFO_FLYOUT_HEIGHT, 46);
 		lnf.putLnfSetting(LnfKeyConstants.INFO_FLYOUT_ICON_TEXT_GAP, 3);
 		lnf.putLnfSetting(LnfKeyConstants.INFO_FLYOUT_LEFT_MARGIN, 13);
 		lnf.putLnfSetting(LnfKeyConstants.INFO_FLYOUT_RIGHT_MARGIN, 20);
 		lnf.putLnfSetting(LnfKeyConstants.INFO_FLYOUT_RIGHT_INDENT, 12);
 
 		lnf.putLnfSetting(LnfKeyConstants.STATUSLINE_UI_PROCESS_LIST_WIDTH, 160);
 
 		lnf.putLnfSetting(LnfKeyConstants.NAVIGATION_TREE_CHARACTER_SELECTION_ENABLED, false);
 
 	}
 
 	//
 	// /**
 	// * Wraps and returns the image for the given name.
 	// *
 	// * @param symbolicName -
 	// * symbolic name of the image.
 	// * @return wrapper
 	// */
 	// protected ImageLnfResource getSharedImageResource(String symbolicName) {
 	// Image image =
 	// PlatformUI.getWorkbench().getSharedImages().getImage(symbolicName);
 	// return new ImageLnfResource(image);
 	// }
 
 	/**
 	 * Returns the data of the system font.
 	 * 
 	 * @return system font data
 	 */
 	protected FontData getSystemFont() {
 		if (Display.getCurrent() != null) {
 			final FontData[] data = Display.getCurrent().getSystemFont().getFontData();
 			if (data.length > 0) {
 				return data[0];
 			}
 		}
 		return new FontData("Arial Narrow", 10, SWT.NORMAL); //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns the color used for the foreground of widgets (of the navigation).
 	 * 
 	 * @return foreground color
 	 */
 	protected ColorLnfResource getPrimaryForeground() {
 		if (primaryForeground == null) {
 			primaryForeground = new ColorLnfResource(68, 70, 74);
 		}
 		return primaryForeground;
 	}
 
 	/**
 	 * Returns the color used for the background of widgets (of the navigation).
 	 * 
 	 * @return background color
 	 */
 	protected ColorLnfResource getPrimaryBackground() {
 		if (primaryBackground == null) {
 			primaryBackground = new ColorLnfResource(255, 255, 255);
 		}
 		return primaryBackground;
 	}
 
 	/**
 	 * Returns the font used for widgets (of the navigation).
 	 * 
 	 * @return font
 	 */
 	protected FontLnfResource getPrimaryFont() {
 		if (primaryFont == null) {
 			final String name = getSystemFont().getName();
 			final int height = getSystemFont().getHeight() + 1;
 			primaryFont = new FontLnfResource(name, height, SWT.NORMAL);
 		}
 		return primaryFont;
 	}
 
 	/**
 	 * Returns whether the border of the operation system should be used for the
 	 * shell and the dialog windows or the border of the Riena Look&Feel.
 	 * 
 	 * @return
 	 */
 	protected boolean hideOsBorder() {
 		return true;
 	}
 
 }
