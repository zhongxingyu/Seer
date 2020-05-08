 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.jsp.messages;
 
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import org.eclipse.osgi.util.NLS;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 
 public class JstUIMessages extends NLS {
 
 	private static final String BUNDLE_NAME = "org.jboss.tools.jst.jsp.messages.messages";//$NON-NLS-1$
 	private static ResourceBundle fResourceBundle;
 	static {
 		// load message values from bundle file
 		NLS.initializeMessages(BUNDLE_NAME, JstUIMessages.class);		
 	}
 	private JstUIMessages(){}
 	
 	public static ResourceBundle getResourceBundle() {
 		try {
 			if (fResourceBundle == null)
 				fResourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());
 		}
 		catch (MissingResourceException x) {
 			JspEditorPlugin.getPluginLog().logError(x);
 			fResourceBundle = null;
 		}
 		return fResourceBundle;
 	}
 
 	public static String CSS_STYLE_CLASS_EDITOR_DESCRIPTION;
 	public static String CSS_STYLE_CLASS_EDITOR_TITLE;
 	public static String CSS_STYLE_EDITOR_TITLE;
     public static String CSS_EMPTY_FILE_PATH_MESSAGE;
     public static String CSS_EMPTY_STYLE_CLASS_MESSAGE;
 	public static String CSS_CLASS_DIALOG_FILE_LABEL;
 	public static String CSS_CLASS_DIALOG_STYLE_CLASS_LABEL;
 	public static String CSS_CLEAR_STYLE_SHEET;
 	public static String CSS_APPLY_CHANGES;
 	public static String ADD_FONT_FAMILY_TIP;
 	public static String REMOVE_FONT_FAMILY_TIP;
 	public static String FaceletPageContectAssistProcessor_NewELExpressionTextInfo;
 	public static String FONT_SIZE;
 	public static String FONT_STYLE;
 	public static String FONT_WEIGHT;
 	public static String FONT_FAMILY;
 	public static String FONT_FAMILY_DIALOG_TITLE;
 	public static String IMAGE_COMBO_TABLE_TOOL_TIP;
 	public static String ImageSelectionDialog_InvalidImageFile;
 	public static String COLOR_DIALOG_TITLE;
 	public static String IMAGE_DIALOG_MESSAGE;
 	public static String IMAGE_DIALOG_TITLE;
 	public static String IMAGE_DIALOG_EMPTY_MESSAGE;
 	public static String FONT_FAMILY_TIP;
 	public static String FormatJSPActionDelegate_Format;
 	public static String ALL_FILES;
 	public static String ALL_IMAGE_FILES;
 	public static String AttributeValueResourceFactory_UnknownResourceType;
 	public static String IMAGE_PREVIEW;
 	public static String TEXT_FONT_TAB_NAME;
 	public static String BACKGROUND_TAB_NAME;
 	public static String BOXES_TAB_NAME;
 	public static String PROPERTY_SHEET_TAB_NAME;
 	public static String QUICK_EDIT_TAB_NAME;
 	public static String BACKGROUND_COLOR;
 	public static String BACKGROUND_COLOR_TIP;
 	public static String BACKGROUND_IMAGE;
 	public static String BACKGROUND_REPEAT;
 	public static String DIMENSION_TITLE;
 	public static String WIDTH;
 	public static String HEIGHT;
 	public static String BORDER_TITLE;
 	public static String BORDER_STYLE;
 	public static String BORDER_COLOR;
 	public static String BORDER_COLOR_TIP;
 	public static String BORDER_WIDTH;
 	public static String BundleAliasElement_AddProperty;
 	public static String BundlesNameResourceElement_AddBundle;
 	public static String ManagedBeanForPropElement_AddProperty;
 	public static String MARGIN_PADDING_TITLE;
 	public static String MARGIN;
 	public static String PADDING;
 	public static String COLOR;
 	public static String COLOR_TIP;
 	public static String TEXT_DECORATION;
 	public static String TEXT_ALIGN;
 	public static String BUTTON_APPLY;
 	public static String BUTTON_CLEAR;
 	public static String PREVIEW_SHEET_TAB_NAME;
 	public static String DEFAULT_PREVIEW_TEXT;
 	public static String DEFAULT_TEXT_FOR_BROWSER_PREVIEW;
 	public static String CSS_NO_EDITED_PROPERTIES;
 	public static String BUTTON_ADD_NEW_STYLE_CLASS;
 	public static String ENTER_CSS_CLASS_NAME;
 	public static String CSS_CLASS_NAME_NOT_VALID;
 	public static String CSS_INVALID_STYLE_PROPERTY;
 	public static String CSS_SELECTOR_TITLE;
 	public static String JspContentAssistProcessor_CloseELExpression;
 	public static String JspContentAssistProcessor_CloseELExpressionInfo;
 	public static String JspContentAssistProcessor_NewELExpression;
 	public static String JspContentAssistProcessor_NewELExpressionAttrInfo;
 	public static String JSPDialogCellEditor_CodeAssist;
 	public static String JSPDialogCellEditor_EditAttribute;
 	public static String JSPDialogContentProposalProvider_CloseELExpression;
 	public static String JSPDialogContentProposalProvider_NewELExpression;
 	public static String PROPERTY_NAME_COLUMN;
 	public static String PROPERTY_VALUE_COLUMN;
 	public static String CSS_ADD_CSS_CLASS_TIP;
 	public static String CSS_REMOVE_CSS_CLASS_TIP;
 	public static String CSS_MOVE_UP_CSS_CLASS_TIP;
 	public static String CSS_MOVE_DOWN_CSS_CLASS_TIP;
 	public static String INFORM_WHEN_PROJECT_MIGHT_NOT_BE_CONFIGURED_PROPERLY_FOR_VPE;
 	public static String DOCS_INFO_LINK;
 	public static String DOCS_INFO_LINK_TEXT;
 	public static String CONFIRM_SELECTION_BAR_DIALOG_TITLE;
 	public static String CONFIRM_SELECTION_BAR_DIALOG_MESSAGE;
 	public static String CONFIRM_SELECTION_BAR_DIALOG_TOGGLE_MESSAGE;
 	public static String SelectionBar_MoreNodes;
 	public static String HIDE_SELECTION_BAR;
 	
 	public static String EXTERNALIZE_STRINGS;
 	public static String EXTERNALIZE_STRINGS_POPUP_MENU_TITLE;
 	public static String EXTERNALIZE_STRINGS_DIALOG_TITLE;
 	public static String EXTERNALIZE_STRINGS_DIALOG_DESCRIPTION;
 	public static String EXTERNALIZE_STRINGS_DIALOG_TEXT_STRING;
 	public static String EXTERNALIZE_STRINGS_DIALOG_PROPERTIES_KEY;
 	public static String EXTERNALIZE_STRINGS_DIALOG_PROPERTIES_VALUE;
 	public static String EXTERNALIZE_STRINGS_DIALOG_PROPERTIES_FILE;
 	public static String EXTERNALIZE_STRINGS_DIALOG_RESOURCE_BUNDLE_LIST;
 	public static String EXTERNALIZE_STRINGS_DIALOG_PROPS_STRINGS_GROUP;
 	public static String EXTERNALIZE_STRINGS_DIALOG_PROPS_FILES_GROUP;
 	public static String EXTERNALIZE_STRINGS_DIALOG_INITIALIZATION_ERROR;
 	public static String EXTERNALIZE_STRINGS_DIALOG_RB_IS_MISSING;
 	public static String EXTERNALIZE_STRINGS_DIALOG_WRONG_SELECTION;
 	public static String EXTERNALIZE_STRINGS_DIALOG_WRONG_SELECTED_TEXT;
 	public static String EXTERNALIZE_STRINGS_DIALOG_VALUE_EXISTS;
 	public static String EXTERNALIZE_STRINGS_DIALOG_PLEASE_SELECT_BUNDLE;
 	public static String EXTERNALIZE_STRINGS_DIALOG_SELECTED_TEXT_IS_EMPTY;
 	public static String EXTERNALIZE_STRINGS_DIALOG_KEY_MUST_BE_SET;
 	public static String EXTERNALIZE_STRINGS_DIALOG_VALUE_MUST_BE_SET;
 	public static String EXTERNALIZE_STRINGS_DIALOG_NEW_FILE;
 	public static String EXTERNALIZE_STRINGS_DIALOG_DEFAULT_KEY;
 	public static String EXTERNALIZE_STRINGS_DIALOG_DEFAULT_VALUE;
 	public static String EXTERNALIZE_STRINGS_DIALOG_PROPERTY_NAME;
 	public static String EXTERNALIZE_STRINGS_DIALOG_PROPERTY_VALUE;
 	public static String EXTERNALIZE_STRINGS_DIALOG_KEY_ALREADY_EXISTS;
 	public static String EXTERNALIZE_STRINGS_DIALOG_ENTER_KEY_NAME;
 	public static String EXTERNALIZE_STRINGS_DIALOG_SELECT_RESOURCE_BUNDLE;
 	public static String CANNOT_LOAD_TAGLIBS_FROM_PAGE_CONTEXT;
 	public static String EXTERNALIZE_STRINGS_DIALOG_SAVE_RESOURCE_BUNDLE;
 	public static String EXTERNALIZE_STRINGS_DIALOG_FACES_CONFIG;
 	public static String EXTERNALIZE_STRINGS_DIALOG_LOAD_BUNDLE;
 	public static String EXTERNALIZE_STRINGS_DIALOG_USER_DEFINED;
 	public static String EXTERNALIZE_STRINGS_DIALOG_BUNDLE_NAME;
 	public static String EXTERNALIZE_STRINGS_DIALOG_WRONG_BUNDLE_PLACEMENT;
 
 }
