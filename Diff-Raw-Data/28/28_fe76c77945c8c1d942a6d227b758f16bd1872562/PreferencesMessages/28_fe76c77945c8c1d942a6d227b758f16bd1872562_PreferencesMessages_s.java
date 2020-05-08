 /*******************************************************************************
  * Copyright (c) 2000, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.rubypeople.rdt.internal.ui.preferences;
 
 import org.eclipse.osgi.util.NLS;
 
 public final class PreferencesMessages extends NLS {
 
     private static final String BUNDLE_NAME = "org.rubypeople.rdt.internal.ui.preferences.PreferencesMessages";//$NON-NLS-1$
 
     private PreferencesMessages() {
     }
 
     public static String RiPreferencePage_description_label;
     public static String DebuggerPreferencePage_description_label;
     public static String CodeFormatterPreferencePage_title;
     public static String CodeFormatterPreferencePage_description;
     public static String MembersOrderPreferencePage_category_button_up;
     public static String MembersOrderPreferencePage_category_button_down;
     public static String MembersOrderPreferencePage_visibility_button_up;
     public static String MembersOrderPreferencePage_visibility_button_down;
     public static String MembersOrderPreferencePage_label_description;
     public static String MembersOrderPreferencePage_fields_label;
     public static String MembersOrderPreferencePage_constructors_label;
     public static String MembersOrderPreferencePage_methods_label;
     public static String MembersOrderPreferencePage_staticfields_label;
     public static String MembersOrderPreferencePage_staticmethods_label;
     public static String MembersOrderPreferencePage_types_label;
     public static String MembersOrderPreferencePage_public_label;
     public static String MembersOrderPreferencePage_private_label;
     public static String MembersOrderPreferencePage_protected_label;
     public static String MembersOrderPreferencePage_usevisibilitysort_label;
     public static String TodoTaskPreferencePage_description;
     public static String TodoTaskPreferencePage_title;
     public static String TodoTaskInputDialog_new_title;
     public static String TodoTaskInputDialog_edit_title;
     public static String TodoTaskInputDialog_priority_high;
     public static String TodoTaskInputDialog_name_label;
     public static String TodoTaskInputDialog_priority_normal;
     public static String TodoTaskInputDialog_priority_low;
     public static String TodoTaskInputDialog_priority_label;
     public static String TodoTaskInputDialog_error_noSpace;
     public static String TodoTaskInputDialog_error_entryExists;
     public static String TodoTaskInputDialog_error_comma;
     public static String TodoTaskInputDialog_error_enterName;
     public static String RubyEditorPreferencePage_link_tooltip;
     public static String RubyEditorPreferencePage_link;
     public static String RiPreferencePage_ripath_label;
     public static String RiPreferencePage_rdocpath_label;
     public static String DebuggerPreferencePage_useRubyDebug_label;
     public static String DebuggerPreferencePage_verboseDebugger_label;
     public static String DebuggerPreferencePage_useRubyDebug_comment;
     public static String TodoTaskConfigurationBlock_tasks_default;
     public static String TodoTaskConfigurationBlock_markers_tasks_high_priority;
     public static String TodoTaskConfigurationBlock_markers_tasks_normal_priority;
     public static String TodoTaskConfigurationBlock_markers_tasks_low_priority;
     public static String TodoTaskConfigurationBlock_markers_tasks_name_column;
     public static String TodoTaskConfigurationBlock_markers_tasks_priority_column;
     public static String TodoTaskConfigurationBlock_casesensitive_label;
     public static String TodoTaskConfigurationBlock_markers_tasks_add_button;
     public static String TodoTaskConfigurationBlock_markers_tasks_edit_button;
     public static String TodoTaskConfigurationBlock_markers_tasks_remove_button;
     public static String TodoTaskConfigurationBlock_markers_tasks_setdefault_button;
     public static String TodoTaskConfigurationBlock_needsbuild_title;
     public static String TodoTaskConfigurationBlock_needsfullbuild_message;
     public static String TodoTaskConfigurationBlock_needsprojectbuild_message;
 	public static String AppearancePreferencePage_note;
 	public static String AppearancePreferencePage_preferenceOnlyEffectiveForNewPerspectives;
 	public static String AppearancePreferencePage_methodtypeparams_label;
 	public static String AppearancePreferencePage_stackViewsVerticallyInTheRubyBrowsingPerspective;
 	public static String AppearancePreferencePage_description;
 	public static String RubyEditorColoringConfigurationBlock_link;
 	public static String RubyEditorPreferencePage_coloring_element;
 	public static String RubyEditorPreferencePage_enable;
 	public static String RubyEditorPreferencePage_color;
 	public static String RubyEditorPreferencePage_bold;
 	public static String RubyEditorPreferencePage_italic;
 	public static String RubyEditorPreferencePage_strikethrough;
 	public static String RubyEditorPreferencePage_underline;
 	public static String RubyEditorPreferencePage_preview;
 	public static String RubyEditorPreferencePage_multiLineComment;
 	public static String RubyEditorPreferencePage_coloring_category_ruby;
 	public static String RubyEditorPreferencePage_singleLineComment;
 	public static String RubyEditorPreferencePage_rubyCommentTaskTags;
 	public static String RubyEditorPreferencePage_keywords;
 	public static String RubyEditorPreferencePage_strings;
 	public static String RubyEditorPreferencePage_characters;
 	public static String RubyEditorPreferencePage_commands;
 	public static String RubyEditorPreferencePage_fixnums;
 	public static String RubyEditorPreferencePage_globals;
 	public static String RubyEditorPreferencePage_regular_expressions;
 	public static String RubyEditorPreferencePage_symbols;
 	public static String RubyEditorPreferencePage_variables;
 	public static String RubyEditorPreferencePage_others;
 	public static String RubyEditorPreferencePage_colors;
 	public static String RubyEditorPreferencePage_empty_input;
 	public static String RubyEditorPreferencePage_invalid_input;
 	public static String RubyEditorPreferencePage_folding_title;
 	public static String RubyEditorPreferencePage_matchingBracketsHighlightColor2;
 	public static String RubyEditorPreferencePage_backgroundForCompletionProposals;
 	public static String RubyEditorPreferencePage_foregroundForCompletionProposals;
 	public static String RubyEditorPreferencePage_backgroundForMethodParameters;
 	public static String RubyEditorPreferencePage_foregroundForMethodParameters;
 	public static String RubyEditorPreferencePage_backgroundForCompletionReplacement;
 	public static String RubyEditorPreferencePage_foregroundForCompletionReplacement;
 	public static String RubyEditorPreferencePage_analyseAnnotationsWhileTyping;
 	public static String SmartTypingConfigurationBlock_annotationReporting_link;
 	public static String RubyEditorPreferencePage_highlightMatchingBrackets;
 	public static String RubyEditorPreferencePage_appearanceOptions;
 	public static String RubyEditorPreferencePage_systemDefault;
 	public static String RubyEditorPreferencePage_general;
 	public static String RubyEditorPreferencePage_typing_tabTitle;
 	public static String SmartTypingConfigurationBlock_autoclose_title;
 	public static String RubyEditorPreferencePage_closeStrings;
 	public static String RubyEditorPreferencePage_closeBrackets;
 	public static String RubyEditorPreferencePage_closeBraces;
 	public static String RubyEditorPreferencePage_endStatements;
 	public static String ProblemSeveritiesPreferencePage_title;
 	public static String ProblemSeveritiesConfigurationBlock_needsbuild_title;
 	public static String ProblemSeveritiesConfigurationBlock_needsfullbuild_message;
 	public static String ProblemSeveritiesConfigurationBlock_needsprojectbuild_message;
 	public static String ProblemSeveritiesConfigurationBlock_error;
 	public static String ProblemSeveritiesConfigurationBlock_warning;
 	public static String ProblemSeveritiesConfigurationBlock_ignore;
 	public static String ProblemSeveritiesConfigurationBlock_common_description;
 	public static String ProblemSeveritiesConfigurationBlock_section_unnecessary_code;
 	public static String ProblemSeveritiesConfigurationBlock_section_potential_programming_problems;
 	public static String ProblemSeveritiesConfigurationBlock_pb_ensure_block_not_completing_label;
 	public static String ProblemSeveritiesConfigurationBlock_pb_empty_statement_label;
 	public static String ProblemSeveritiesConfigurationBlock_pb_hidden_rescueblock_label;
 	public static String ProblemSeveritiesConfigurationBlock_pb_fall_through_case;
 	public static String ProblemSeveritiesConfigurationBlock_pb_null_reference;
 	public static String ProblemSeveritiesConfigurationBlock_pb_unused_local_label;
 	public static String ProblemSeveritiesConfigurationBlock_pb_unused_parameter_label;
 	public static String ProblemSeveritiesConfigurationBlock_pb_unused_private_label;
 	public static String ProblemSeveritiesConfigurationBlock_pb_unnecessary_else_label;
 	public static String RubyEditorPreferencePage_background_color;
 	public static String MarkOccurrencesConfigurationBlock_markOccurrences;
 	public static String MarkOccurrencesConfigurationBlock_markTypeOccurrences;
 	public static String MarkOccurrencesConfigurationBlock_markMethodOccurrences;
 	public static String MarkOccurrencesConfigurationBlock_markConstantOccurrences;
 	public static String MarkOccurrencesConfigurationBlock_markFieldOccurrences;
 	public static String MarkOccurrencesConfigurationBlock_markLocalVariableOccurrences;
 	public static String MarkOccurrencesConfigurationBlock_markMethodExitPoints;
 	public static String MarkOccurrencesConfigurationBlock_stickyOccurrences;
 	public static String MarkOccurrencesConfigurationBlock_title;
 	
 	public static String PropertyAndPreferencePage_useprojectsettings_label;
 	public static String PropertyAndPreferencePage_useworkspacesettings_change;
 	public static String PropertyAndPreferencePage_showprojectspecificsettings_label;
 	public static String ProjectSelectionDialog_title;
 	public static String ProjectSelectionDialog_desciption;
 	public static String ProjectSelectionDialog_filter;
 	public static String FoldingConfigurationBlock_enable;
 	public static String FoldingConfigurationBlock_combo_caption;
 	public static String NewJavaProjectPreferencePage_error_decode;
 	public static String NewJavaProjectPreferencePage_jre_variable_description;
 	public static String NewJavaProjectPreferencePage_jre_container_description;
 
     static {
         NLS.initializeMessages(BUNDLE_NAME, PreferencesMessages.class);
     }
 }
