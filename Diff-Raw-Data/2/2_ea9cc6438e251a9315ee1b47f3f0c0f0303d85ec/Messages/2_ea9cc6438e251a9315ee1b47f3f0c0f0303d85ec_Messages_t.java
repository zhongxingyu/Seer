 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.views.workingsets.nls;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Target Explorer UI plugin externalized strings management.
  */
 public class Messages extends NLS {
 
 	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tcf.te.ui.views.workingsets.nls.Messages"; //$NON-NLS-1$
 
 	/**
 	 * Static constructor.
 	 */
 	static {
 		// Load message values from bundle file
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	// **** Declare externalized string id's down here *****
 
 	public static String WorkingSetActionProvider_multipleWorkingSets;
 
 	public static String WorkingSetActionGroup_Top_Level_Element;
 	public static String WorkingSetActionGroup_Elements;
 	public static String WorkingSetActionGroup_Working_Set;
 
 	public static String ViewStateManager_others_name;
 
 	public static String ConfigureWorkingSetAction_text;
 	public static String ConfigureWorkingSetAction_toolTip;
 
 	public static String TargetWorkingSetPage_workingSet_name;
 	public static String TargetWorkingSetPage_workspace_content;
 	public static String TargetWorkingSetPage_add_button;
 	public static String TargetWorkingSetPage_addAll_button;
 	public static String TargetWorkingSetPage_remove_button;
 	public static String TargetWorkingSetPage_removeAll_button;
 	public static String TargetWorkingSetPage_workingSet_content;
 	public static String TargetWorkingSetPage_warning_nameWhitespace;
 	public static String TargetWorkingSetPage_warning_nameMustNotBeEmpty;
 	public static String TargetWorkingSetPage_warning_workingSetExists;
 	public static String TargetWorkingSetPage_warning_resourceMustBeChecked;
 	public static String TargetWorkingSetPage_title;
 	public static String TargetWorkingSetPage_workingSet_description;
 
 	public static String WorkingSetConfigurationDialog_down_label;
 	public static String WorkingSetConfigurationDialog_up_label;
 	public static String WorkingSetConfigurationDialog_title;
 	public static String WorkingSetConfigurationDialog_selectAll_label;
 	public static String WorkingSetConfigurationDialog_edit_label;
 	public static String WorkingSetConfigurationDialog_message;
 	public static String WorkingSetConfigurationDialog_deselectAll_label;
 	public static String WorkingSetConfigurationDialog_new_label;
 	public static String WorkingSetConfigurationDialog_sort_working_sets;
 	public static String WorkingSetConfigurationDialog_remove_label;
 }
