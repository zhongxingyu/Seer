 package org.eclipse.jst.j2ee.internal.project;
 
 /***************************************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 import org.eclipse.osgi.util.NLS;
 
 public class ProjectSupportResourceHandler extends NLS {
	private static final String BUNDLE_NAME = "webserviceui";//$NON-NLS-1$
 
 	private ProjectSupportResourceHandler() {
 		// Do not instantiate
 	}
 
 	public static String Folder_name_cannot_be_the_same_as_Java_source_folder_5;
 	public static String Target_Update_Op;
 	public static String Operation_failed_due_to_SA_ERROR_;
 	public static String Creating_Web_Project____UI_;
 	public static String Could_not_rename_____2;
 	public static String A_web_project_must_be_open_and_must_exist_for_properties_to_be_edited_30;
 	public static String Operation_failed_due_to_IO_ERROR_;
 	public static String Cannot_clone_TaglibInfo_1_EXC_;
 	public static String Syntax_Error_in_the_links_UI_;
 	public static String Sync_WLP_Op;
 	public static String Generated_by_Web_Tooling_23;
 	public static String _1concat_EXC_;
 	public static String File_Serving_Enabler_7;
 	public static String Auto_Generated___File_Enabler_9;
 	public static String Not_a_web_project_29;
 	public static String Names_cannot_begin_or_end_with_whitespace_5;
 	public static String The_character_is_invalid_in_a_context_root;
 	public static String Folder_name_cannot_be_the_same_as_Java_class_folder_6;
 	public static String The_path_for_the_links_sta_EXC_;
 	public static String Operation_failed_due_to_Ja_ERROR_;
 	public static String Folder_name_cannot_be_empty_2;
 	public static String Error_importing_Module_Fil_EXC_;
 	public static String Operation_failed_due_to_Co_ERROR_;
 	public static String Folder_names_cannot_be_equal_4;
 	public static String Could_not_read_TLD_15;
 	public static String Folder_name_is_not_valid;
 	public static String Invalid_Servlet_Level_set_on_WebNature_3_EXC_;
 	public static String Context_Root_cannot_be_empty_2;
 	public static String Error_while_saving_links_s_EXC_;
 
 
 	static {
 		NLS.initializeMessages(BUNDLE_NAME, ProjectSupportResourceHandler.class);
 	}
 
 	public static String getString(String key, Object[] args) {
 		return NLS.bind(key, args);
 	}
 }
