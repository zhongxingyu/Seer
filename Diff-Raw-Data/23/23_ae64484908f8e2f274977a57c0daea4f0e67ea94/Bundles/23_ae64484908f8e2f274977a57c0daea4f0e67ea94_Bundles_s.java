 package com.akjava.gwt.webappmaker.client.resources;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.TextResource;
 
 public interface Bundles extends ClientBundle {
 public static Bundles INSTANCE=GWT.create(Bundles.class);
 	TextResource servlet();
 	TextResource web();
 	TextResource list_servlet();
 	TextResource show_servlet();
 	TextResource add_servlet();
 	TextResource add_confirm_servlet();
 	TextResource add_exec_servlet();
 	TextResource edit_servlet();
 	TextResource edit_confirm_servlet();
 	TextResource edit_exec_servlet();
 	TextResource delete_confirm_servlet();
 	TextResource delete_exec_servlet();
 	
 	TextResource top_servlet();
 	
 	TextResource list_html();
 	TextResource list_row_html();
 	TextResource show_html();
 	
 	TextResource add_html();
 	TextResource add_confirm_html();
 	TextResource add_exec_html();
 	
 	TextResource edit_html();
 	TextResource edit_confirm_html();
 	TextResource edit_exec_html();
 	
 	TextResource delete_confirm_html();
 	TextResource delete_exec_html();
 	
 	TextResource admin_list_html();
 	TextResource admin_list_row_html();
 
 	
 	
 	
 	TextResource top_links_html();
 	TextResource top_links_sub_html();
 	
 	TextResource main_base_html();
 	TextResource main_base_sub_html();
 	TextResource admin_base_html();
 	TextResource admin_base_sub_html();
 	
 	
 	TextResource japanese();
 	TextResource english();
 	
 	
 	TextResource sharedutils();
 	
 	TextResource modifier();
 	TextResource validator();
 	
 	TextResource tools();
 	TextResource tolabelmap_boolean();
 	TextResource tolabelmap_cmdate();
 	TextResource tolabelmap_text_long();
 	
 	TextResource createoptionvalue_check();
 	TextResource createoptionvalue_single_select();
 }
