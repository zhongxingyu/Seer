 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.lnf.rienadefault;
 
 import java.util.Map;
 
 import org.eclipse.riena.ui.swt.lnf.ColorLnfResource;
 import org.eclipse.riena.ui.swt.lnf.FontLnfResource;
 import org.eclipse.riena.ui.swt.lnf.ILnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.ILnfResource;
 import org.eclipse.riena.ui.swt.lnf.ILnfTheme;
 import org.eclipse.riena.ui.swt.lnf.ImageLnfResource;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * Default theme of Riena used by the default Look and Feel,
  * <code>RienaDefaultLnf</code>.
  */
 public class RienaDefaultTheme implements ILnfTheme {
 
 	private static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$
 	private static final String PATH_ICONS = "org.eclipse.riena.ui.swt:" + PATH_SEPARATOR + "icons"; //$NON-NLS-1$ //$NON-NLS-2$
 	private static final String IMAGE_FOLDER = PATH_ICONS + PATH_SEPARATOR + "node_expanded.gif"; //$NON-NLS-1$
 	private static final String IMAGE_FOLDER_CLOSED = PATH_ICONS + PATH_SEPARATOR + "node_collapsed.gif"; //$NON-NLS-1$
 	private static final String IMAGE_EMPTY_DOCUMENT = PATH_ICONS + PATH_SEPARATOR + "no_format.gif"; //$NON-NLS-1$
 	private static final String IMAGE_THIN_CLOSE = PATH_ICONS + PATH_SEPARATOR + "thin_close_view.gif"; //$NON-NLS-1$
 	private static final String IMAGE_BACKGROUND = PATH_ICONS + PATH_SEPARATOR + "background.png"; //$NON-NLS-1$
 	private static final String IMAGE_LOGO = PATH_ICONS + PATH_SEPARATOR + "logo.png"; //$NON-NLS-1$
 	private static final String IMAGE_CLOSE = PATH_ICONS + PATH_SEPARATOR + "mb_close.gif"; //$NON-NLS-1$
 	private static final String IMAGE_CLOSE_HOVER = PATH_ICONS + PATH_SEPARATOR + "mb_close_hover.gif"; //$NON-NLS-1$
 	private static final String IMAGE_CLOSE_HOVER_SELECTED = PATH_ICONS + PATH_SEPARATOR
 			+ "mb_close_hover_selected.gif"; //$NON-NLS-1$
 	private static final String IMAGE_CLOSE_INACTIVE = PATH_ICONS + PATH_SEPARATOR + "mb_close_inactive.gif"; //$NON-NLS-1$
 	private static final String IMAGE_MAX = PATH_ICONS + PATH_SEPARATOR + "mb_max.gif"; //$NON-NLS-1$
 	private static final String IMAGE_MAX_HOVER = PATH_ICONS + PATH_SEPARATOR + "mb_max_hover.gif"; //$NON-NLS-1$
 	private static final String IMAGE_MAX_HOVER_SELECTED = PATH_ICONS + PATH_SEPARATOR + "mb_max_hover_selected.gif"; //$NON-NLS-1$
 	private static final String IMAGE_MAX_INACTIVE = PATH_ICONS + PATH_SEPARATOR + "mb_max_inactive.gif"; //$NON-NLS-1$
 	private static final String IMAGE_MIN = PATH_ICONS + PATH_SEPARATOR + "mb_min.gif"; //$NON-NLS-1$
 	private static final String IMAGE_MIN_HOVER = PATH_ICONS + PATH_SEPARATOR + "mb_min_hover.gif"; //$NON-NLS-1$
 	private static final String IMAGE_MIN_HOVER_SELECTED = PATH_ICONS + PATH_SEPARATOR + "mb_min_hover_selected.gif"; //$NON-NLS-1$
 	private static final String IMAGE_MIN_INACTIVE = PATH_ICONS + PATH_SEPARATOR + "mb_min_inactive.gif"; //$NON-NLS-1$
 	private static final String IMAGE_RESTORE = PATH_ICONS + PATH_SEPARATOR + "mb_restore.gif"; //$NON-NLS-1$
 	private static final String IMAGE_RESTORE_HOVER = PATH_ICONS + PATH_SEPARATOR + "mb_restore_hover.gif"; //$NON-NLS-1$
 	private static final String IMAGE_RESTORE_HOVER_SELECTED = PATH_ICONS + PATH_SEPARATOR
 			+ "mb_restore_hover_selected.gif"; //$NON-NLS-1$
 	private static final String IMAGE_RESTORE_INACTIVE_ICON = PATH_ICONS + PATH_SEPARATOR + "mb_restore_inactive.gif"; //$NON-NLS-1$
 	private static final String IMAGE_HAND = PATH_ICONS + PATH_SEPARATOR + "hand.png"; //$NON-NLS-1$
 	private static final String IMAGE_GRAB = PATH_ICONS + PATH_SEPARATOR + "grab.png"; //$NON-NLS-1$
 	private static final String IMAGE_GRAB_CORNER = PATH_ICONS + PATH_SEPARATOR + "grabCorner.png"; //$NON-NLS-1$
 	private static final String IMAGE_SPACER = PATH_ICONS + PATH_SEPARATOR + "spacer.png"; //$NON-NLS-1$
 	private static final String IMAGE_ERROR = PATH_ICONS + PATH_SEPARATOR + "statusline_error.gif"; //$NON-NLS-1$
 	private static final String IMAGE_WARNING = PATH_ICONS + PATH_SEPARATOR + "statusline_warning.gif"; //$NON-NLS-1$
 	private static final String IMAGE_INFO = PATH_ICONS + PATH_SEPARATOR + "statusline_info.gif"; //$NON-NLS-1$
 	private static final String IMAGE_ERROR_MARKER = PATH_ICONS + PATH_SEPARATOR + "errorMarker.png"; //$NON-NLS-1$
 	private static final String IMAGE_MANDATORY_MARKER = PATH_ICONS + PATH_SEPARATOR + "mandatoryMarker.png"; //$NON-NLS-1$
 	private FontLnfResource primaryFont;
 	private ColorLnfResource primaryBackground;
 	private ColorLnfResource primaryForeground;
 
 	/**
 	 * @see org.eclipse.riena.ui.swt.lnf.ILnfTheme#addCustomColors(java.util.Map)
 	 */
 	public void addCustomColors(Map<String, ILnfResource> table) {
 
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_FOREGROUND, new ColorLnfResource(255, 255, 255));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_PASSIVE_FOREGROUND, new ColorLnfResource(128, 128, 128));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_BACKGROUND, getPrimaryBackground());
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_BORDER_BOTTOM_RIGHT_COLOR, new ColorLnfResource(83, 85, 94));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_BORDER_TOP_LEFT_COLOR, new ColorLnfResource(121, 124, 137));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_INNER_BORDER_BOTTOM_RIGHT_COLOR, new ColorLnfResource(83, 95, 94));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_INNER_BORDER_TOP_LEFT_COLOR, new ColorLnfResource(173, 180, 205));
 		table
 				.put(ILnfKeyConstants.TITLELESS_SHELL_INNER_BORDER_BOTTOM_RIGHT_COLOR, new ColorLnfResource(161, 168,
 						190));
 
 		table.put(ILnfKeyConstants.COOLBAR_BACKGROUND, getPrimaryBackground());
 
 		table.put(ILnfKeyConstants.EMBEDDED_TITLEBAR_FOREGROUND, getPrimaryForeground());
 		table
 				.put(ILnfKeyConstants.EMBEDDED_TITLEBAR_ACTIVE_BACKGROUND_START_COLOR, new ColorLnfResource(196, 225,
 						244));
 		table.put(ILnfKeyConstants.EMBEDDED_TITLEBAR_ACTIVE_BACKGROUND_END_COLOR, new ColorLnfResource(100, 153, 186));
 		table.put(ILnfKeyConstants.EMBEDDED_TITLEBAR_ACTIVE_BORDER_COLOR, new ColorLnfResource(171, 171, 174));
 		table.put(ILnfKeyConstants.EMBEDDED_TITLEBAR_PASSIVE_BACKGROUND_START_COLOR,
 				new ColorLnfResource(244, 244, 245));
 		table.put(ILnfKeyConstants.EMBEDDED_TITLEBAR_PASSIVE_BACKGROUND_END_COLOR, new ColorLnfResource(220, 220, 220));
 		table.put(ILnfKeyConstants.EMBEDDED_TITLEBAR_PASSIVE_BORDER_COLOR, new ColorLnfResource(213, 213, 216));
 
 		table.put(ILnfKeyConstants.EMBEDDED_TITLEBAR_HOVER_BORDER_TOP_COLOR, new ColorLnfResource(251, 233, 168));
 		table.put(ILnfKeyConstants.EMBEDDED_TITLEBAR_HOVER_BORDER_BOTTOM_COLOR, new ColorLnfResource(192, 151, 1));
 		table.put(ILnfKeyConstants.EMBEDDED_TITLEBAR_HOVER_BORDER_START_COLOR, new ColorLnfResource(255, 207, 32));
 		table.put(ILnfKeyConstants.EMBEDDED_TITLEBAR_HOVER_BORDER_END_COLOR, new ColorLnfResource(255, 176, 1));
 
 		table.put(ILnfKeyConstants.NAVIGATION_BACKGROUND, getPrimaryBackground());
 		table.put(ILnfKeyConstants.MODULE_GROUP_WIDGET_BACKGROUND, getPrimaryBackground());
 		table.put(ILnfKeyConstants.SUB_MODULE_TREE_BACKGROUND, getPrimaryBackground());
 		table.put(ILnfKeyConstants.SUB_MODULE_BACKGROUND, getPrimaryBackground());
 
 		table.put(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_FOREGROUND, getPrimaryForeground());
 		table.put(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_BACKGROUND, getPrimaryBackground());
 		table.put(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_TOP_SELECTION_COLOR, new ColorLnfResource(64, 132, 191));
 		table.put(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_ACTIVE_BACKGROUND_START_COLOR, new ColorLnfResource(255,
 				255, 255));
 		table.put(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_ACTIVE_BACKGROUND_END_COLOR, new ColorLnfResource(255, 255,
 				255));
 		table.put(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_PASSIVE_BACKGROUND_START_COLOR, new ColorLnfResource(245,
 				245, 245));
 		table.put(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_PASSIVE_BACKGROUND_END_COLOR, new ColorLnfResource(229,
 				229, 229));
 		table.put(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_INNER_BORDER_COLOR, new ColorLnfResource(245, 245, 245));
 		table
 				.put(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_BORDER_TOP_RIGHT_COLOR, new ColorLnfResource(206, 206,
 						206));
 		table.put(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_BORDER_BOTTOM_LEFT_COLOR, new ColorLnfResource(183, 183,
 				183));
 
 		table.put(ILnfKeyConstants.SUB_MODULE_ITEM_TOOLTIP_BACKGROUND, getPrimaryBackground());
 		table.put(ILnfKeyConstants.MODULE_ITEM_TOOLTIP_BACKGROUND, getPrimaryBackground());
 
 		table.put(ILnfKeyConstants.SUB_MODULE_TREE_ITEM_BACKGROUND, getPrimaryBackground());
 		table.put(ILnfKeyConstants.SUB_MODULE_TREE_ITEM_FOREGROUND, getPrimaryForeground());
 		table.put(ILnfKeyConstants.SUB_MODULE_TREE_ITEM_SELECTION_COLOR, new ColorLnfResource(249, 223, 146));
 		table.put(ILnfKeyConstants.SUB_MODULE_TREE_ITEM_FOCUS_COLOR, new ColorLnfResource(166, 202, 240));
 
 		table.put(ILnfKeyConstants.STATUSLINE_BACKGROUND, getPrimaryBackground());
 
 		table.put(ILnfKeyConstants.GRAB_CORNER_BACKGROUND, getPrimaryBackground());
 
 		table.put("white", new ColorLnfResource(255, 255, 255)); //$NON-NLS-1$
 		table.put("lightGray", new ColorLnfResource(192, 192, 192)); //$NON-NLS-1$
 		table.put("gray", new ColorLnfResource(128, 128, 128)); //$NON-NLS-1$
 		table.put("darkGray", new ColorLnfResource(64, 64, 64)); //$NON-NLS-1$
 		table.put("black", new ColorLnfResource(0, 0, 0)); //$NON-NLS-1$
 		table.put("red", new ColorLnfResource(255, 0, 0)); //$NON-NLS-1$
 		table.put("pink", new ColorLnfResource(255, 175, 175)); //$NON-NLS-1$
 		table.put("orange", new ColorLnfResource(255, 200, 0)); //$NON-NLS-1$
 		table.put("yellow", new ColorLnfResource(255, 255, 0)); //$NON-NLS-1$
 		table.put("green", new ColorLnfResource(0, 255, 0)); //$NON-NLS-1$
 		table.put("magenta", new ColorLnfResource(255, 0, 255)); //$NON-NLS-1$
 		table.put("cyan", new ColorLnfResource(0, 255, 255)); //$NON-NLS-1$
 		table.put("blue", new ColorLnfResource(0, 0, 255)); //$NON-NLS-1$
 
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.swt.lnf.ILnfTheme#addCustomFonts(java.util.Map)
 	 */
 	public void addCustomFonts(Map<String, ILnfResource> table) {
 
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_FONT, getPrimaryFont());
 
 		table.put(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_FONT, getPrimaryFont());
 
 		table.put(ILnfKeyConstants.MODULE_ITEM_TOOLTIP_FONT, getPrimaryFont());
 
 		table.put(ILnfKeyConstants.EMBEDDED_TITLEBAR_FONT, getPrimaryFont());
 
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.swt.lnf.ILnfTheme#addCustomImages(java.util.Map)
 	 */
 	public void addCustomImages(Map<String, ILnfResource> table) {
 
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_BACKGROUND_IMAGE, new ImageLnfResource(IMAGE_BACKGROUND));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_LOGO, new ImageLnfResource(IMAGE_LOGO));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_CLOSE_ICON, new ImageLnfResource(IMAGE_CLOSE));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_CLOSE_HOVER_ICON, new ImageLnfResource(IMAGE_CLOSE_HOVER));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_CLOSE_HOVER_SELECTED_ICON, new ImageLnfResource(
 				IMAGE_CLOSE_HOVER_SELECTED));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_CLOSE_INACTIVE_ICON, new ImageLnfResource(IMAGE_CLOSE_INACTIVE));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_MAX_ICON, new ImageLnfResource(IMAGE_MAX));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_MAX_HOVER_ICON, new ImageLnfResource(IMAGE_MAX_HOVER));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_MAX_HOVER_SELECTED_ICON, new ImageLnfResource(
 				IMAGE_MAX_HOVER_SELECTED));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_MAX_INACTIVE_ICON, new ImageLnfResource(IMAGE_MAX_INACTIVE));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_MIN_ICON, new ImageLnfResource(IMAGE_MIN));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_MIN_HOVER_ICON, new ImageLnfResource(IMAGE_MIN_HOVER));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_MIN_HOVER_SELECTED_ICON, new ImageLnfResource(
 				IMAGE_MIN_HOVER_SELECTED));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_MIN_INACTIVE_ICON, new ImageLnfResource(IMAGE_MIN_INACTIVE));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_RESTORE_ICON, new ImageLnfResource(IMAGE_RESTORE));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_RESTORE_HOVER_ICON, new ImageLnfResource(IMAGE_RESTORE_HOVER));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_RESTORE_HOVER_SELECTED_ICON, new ImageLnfResource(
 				IMAGE_RESTORE_HOVER_SELECTED));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_RESTORE_INACTIVE_ICON, new ImageLnfResource(
 				IMAGE_RESTORE_INACTIVE_ICON));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_HAND_IMAGE, new ImageLnfResource(IMAGE_HAND));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_GRAB_IMAGE, new ImageLnfResource(IMAGE_GRAB));
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_GRAB_CORNER_IMAGE, new ImageLnfResource(IMAGE_GRAB_CORNER));
 
 		table.put(ILnfKeyConstants.SUB_MODULE_TREE_DOCUMENT_LEAF_ICON, new ImageLnfResource(IMAGE_EMPTY_DOCUMENT));
 		table.put(ILnfKeyConstants.SUB_MODULE_TREE_FOLDER_CLOSED_ICON, new ImageLnfResource(IMAGE_FOLDER_CLOSED));
		table.put(ILnfKeyConstants.SUB_MODULE_TREE_ERROR_MARKER_ICON, new ImageLnfResource(IMAGE_ERROR_MARKER));
		table.put(ILnfKeyConstants.SUB_MODULE_TREE_MANDATORY_MARKER_ICON, new ImageLnfResource(IMAGE_MANDATORY_MARKER));
 		// table.put(ILnfKeyConstants."treeFolderOpen.icon",
 		// getSharedImageResource(ISharedImages.IMG_OBJ_FOLDER));
 		table.put(ILnfKeyConstants.SUB_MODULE_TREE_FOLDER_OPEN_ICON, new ImageLnfResource(IMAGE_FOLDER));
 		table.put(ILnfKeyConstants.EMBEDDED_TITLEBAR_CLOSE_ICON, new ImageLnfResource(IMAGE_THIN_CLOSE));
 
 		table.put(ILnfKeyConstants.STATUSLINE_SPACER_ICON, new ImageLnfResource(IMAGE_SPACER));
 		table.put(ILnfKeyConstants.STATUSLINE_ERROR_ICON, new ImageLnfResource(IMAGE_ERROR));
 		table.put(ILnfKeyConstants.STATUSLINE_WARNING_ICON, new ImageLnfResource(IMAGE_WARNING));
 		table.put(ILnfKeyConstants.STATUSLINE_INFO_ICON, new ImageLnfResource(IMAGE_INFO));
 
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.swt.lnf.ILnfTheme#addCustomSettings(java.util.Map)
 	 */
 	public void addCustomSettings(Map<String, Object> table) {
 
 		table.put(ILnfKeyConstants.SHELL_HIDE_OS_BORDER, true);
 
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_PADDING, 2);
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_HORIZONTAL_LOGO_POSITION, SWT.LEFT);
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_VERTICAL_LOGO_POSITION, SWT.CENTER);
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_HORIZONTAL_LOGO_MARGIN, 5);
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_VERTICAL_LOGO_MARGIN, 0);
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_HORIZONTAL_TEXT_POSITION, SWT.RIGHT);
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_SHOW_CLOSE, true);
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_SHOW_MAX, true);
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_SHOW_MIN, true);
 		table.put(ILnfKeyConstants.TITLELESS_SHELL_RESIZEABLE, true);
 
 		table.put(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_TOP_MARGIN, 22);
 		table.put(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_HEIGHT, 40);
 		table.put(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_HORIZONTAL_TAB_POSITION, SWT.CENTER);
 		table.put(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_TAB_SHOW_ICON, false);
 
 		table.put(ILnfKeyConstants.SUB_MODULE_ITEM_TOOLTIP_POPUP_DELAY, 0);
 
 		table.put(ILnfKeyConstants.MODULE_ITEM_TOOLTIP_POPUP_DELAY, 500);
 
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
 			FontData[] data = Display.getCurrent().getSystemFont().getFontData();
 			if (data.length > 0) {
 				return data[0];
 			}
 		}
 		return new FontData("Arial", 10, SWT.NORMAL); //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns the color used for the foreground of widgets.
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
 	 * Returns the color used for the background of widgets.
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
 	 * Returns the font used for widgets.
 	 * 
 	 * @return font
 	 */
 	protected FontLnfResource getPrimaryFont() {
 		if (primaryFont == null) {
 			String name = getSystemFont().getName();
 			int height = getSystemFont().getHeight() + 1;
 			primaryFont = new FontLnfResource(name, height, SWT.NORMAL);
 		}
 		return primaryFont;
 	}
 
 }
