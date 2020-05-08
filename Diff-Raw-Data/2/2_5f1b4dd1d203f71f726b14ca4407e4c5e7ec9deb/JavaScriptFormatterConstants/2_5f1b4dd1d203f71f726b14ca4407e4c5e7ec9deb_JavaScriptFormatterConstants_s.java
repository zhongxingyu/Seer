 /*******************************************************************************
  * Copyright (c) 2009 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Vladimir Belov)
  *******************************************************************************/
 
 package org.eclipse.dltk.javascript.formatter;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.dltk.ui.CodeFormatterConstants;
 
 public final class JavaScriptFormatterConstants {
 
 	/*
 	 * On adding a new option, you should register it in the static{} block!!!
 	 */
 
 	public static final String FORMATTER_TAB_CHAR = CodeFormatterConstants.FORMATTER_TAB_CHAR;
 	public static final String FORMATTER_TAB_SIZE = CodeFormatterConstants.FORMATTER_TAB_SIZE;
 	public static final String FORMATTER_INDENTATION_SIZE = CodeFormatterConstants.FORMATTER_INDENTATION_SIZE;
 
 	// JavaScriptFormatterConstants.LINES_FILE_AFTER_REQUIRE,
 	// JavaScriptFormatterConstants.LINES_FILE_BETWEEN_MODULE,
 	// JavaScriptFormatterConstants.LINES_FILE_BETWEEN_CLASS,
 	// JavaScriptFormatterConstants.LINES_FILE_BETWEEN_METHOD,
 	// JavaScriptFormatterConstants.LINES_BEFORE_FIRST,
 	// JavaScriptFormatterConstants.LINES_BEFORE_MODULE,
 	// JavaScriptFormatterConstants.LINES_BEFORE_CLASS,
 	// JavaScriptFormatterConstants.LINES_BEFORE_METHOD };
 
 	public static final String LINES_PRESERVE = "lines.preserve"; //$NON-NLS-1$
 
 	public static final String WRAP_COMMENTS = "wrap.comments"; //$NON-NLS-1$
 	public static final String WRAP_COMMENTS_LENGTH = "wrap.comments.length"; //$NON-NLS-1$
 
 	public static final String FORMATTER_PROFILES = "formatter.profiles"; //$NON-NLS-1$
 	public static final String FORMATTER_ACTIVE_PROFILE = "formatter.profiles.active"; //$NON-NLS-1$
 
 	public static final String INDENT_METHOD = "formatter.indent.function";//$NON-NLS-1$
 	public static final String INDENT_BLOCK = "formatter.indent.blocks";//$NON-NLS-1$
 	public static final String INDENT_SWITCH = "formatter.indent.switch";//$NON-NLS-1$
 	public static final String INDENT_CASE = "formatter.indent.case";//$NON-NLS-1$
 	public static final String INDENT_BREAK = "formatter.indent.break";//$NON-NLS-1$
 	public static final String INDENT_EMPTY_LINES = "formatter.indent.empty.lines";//$NON-NLS-1$
 
 	public static final String BRACE_METHOD = "formatter.braces.function";//$NON-NLS-1$
 	public static final String BRACE_BLOCK = "formatter.braces.blocks";//$NON-NLS-1$
 	public static final String BRACE_SWITCH = "formatter.braces.switch";//$NON-NLS-1$
 	public static final String BRACE_CASE = "formatter.braces.case";//$NON-NLS-1$
 	public static final String BRACE_ARRAY = "formatter.braces.array";//$NON-NLS-1$
 	public static final String BRACE_EMPTY_ARRAY = "formatter.braces.empty.array";//$NON-NLS-1$
 
 	// //////////////////////////////////////////////////////////////////////////
 	// NOT OPTIONS !
 	//
 
 	// Same line
 	public static final String BRACE_SAME_LINE = "same.line";//$NON-NLS-1$
 	// Next line
 	public static final String BRACE_NEXT_LINE = "next.line";//$NON-NLS-1$
 	// Next line indented
 	public static final String BRACE_NEXT_LINE_INDENTED = "next.line.indented";//$NON-NLS-1$
 	// Next line on wrap
 	public static final String BRACE_NEXT_LINE_ON_WRAP = "next.line.on.wrap";//$NON-NLS-1$
 
 	//
 	// //////////////////////////////////////////////////////////////////////////
 
 	public static final String NEW_LINE_IN_EMPTY_METHOD = "formatter.newlines.empty.method";//$NON-NLS-1$
 	public static final String NEW_LINE_IN_EMPTY_BLOCK = "formatter.newlines.empty.block";//$NON-NLS-1$
 	public static final String NEW_LINE_AT_EOF = "formatter.newlines.eof";//$NON-NLS-1$
 
 	public static final String NEW_LINE_BEFORE_ELSE_IN_IF = "formatter.newlines.else";//$NON-NLS-1$
 	public static final String NEW_LINE_BEFORE_CATCH_IN_TRY = "formatter.newlines.catch";//$NON-NLS-1$
 	public static final String NEW_LINE_BEFORE_FINALLY_IN_TRY = "formatter.newlines.finally";//$NON-NLS-1$
 	public static final String NEW_LINE_BEFORE_WHILE_IN_DO = "formatter.newlines.while";//$NON-NLS-1$
 
 	public static final String KEEP_THEN_ON_SAME_LINE = "formatter.samelines.then";//$NON-NLS-1$
 	public static final String KEEP_SIMPLE_IF_ON_ONE_LINE = "formatter.samelines.simpleif";//$NON-NLS-1$
 
 	// TODO does it duplicate NEW_LINE_BEFORE_ELSE_IN_IF ?
 	//public static final String KEEP_ELSE_ON_SAME_LINE = "formatter.samelines.else";//$NON-NLS-1$
 
 	public static final String KEEP_ELSE_IF_ON_ONE_LINE = "formatter.samelines.elseif";//$NON-NLS-1$
 	public static final String KEEP_RETURN_ON_ONE_LINE = "formatter.samelines.return";//$NON-NLS-1$
 	public static final String KEEP_THROW_ON_ONE_LINE = "formatter.samelines.throw";//$NON-NLS-1$
 
 	// /////////////////////////////////////////////
 	//
 	// SPACES
 	//
 
 	// IF spaces
 	public static final String INSERT_SPACE_BEFORE_LP_IF = "formatter.insertspace.before.lp.if";
 	public static final String INSERT_SPACE_AFTER_LP_IF = "formatter.insertspace.after.lp.if";
 	public static final String INSERT_SPACE_BEFORE_RP_IF = "formatter.insertspace.before.rp.if";
 
 	// FOR spaces
 	public static final String INSERT_SPACE_BEFORE_LP_FOR = "formatter.insertspace.before.lp.for";
 	public static final String INSERT_SPACE_AFTER_LP_FOR = "formatter.insertspace.after.lp.for";
 	public static final String INSERT_SPACE_BEFORE_RP_FOR = "formatter.insertspace.before.rp.for";
 
 	// SWITCH spaces
 	public static final String INSERT_SPACE_BEFORE_LP_SWITCH = "formatter.insertspace.before.lp.switch";
 	public static final String INSERT_SPACE_AFTER_LP_SWITCH = "formatter.insertspace.after.lp.switch";
 	public static final String INSERT_SPACE_BEFORE_RP_SWITCH = "formatter.insertspace.before.rp.switch";
 
 	// WHILE-DO..WHILE spaces
 	public static final String INSERT_SPACE_BEFORE_LP_WHILE = "formatter.insertspace.before.lp.while";
 	public static final String INSERT_SPACE_AFTER_LP_WHILE = "formatter.insertspace.after.lp.while";
 	public static final String INSERT_SPACE_BEFORE_RP_WHILE = "formatter.insertspace.before.rp.while";
 
 	// CALL spaces
 	public static final String INSERT_SPACE_BEFORE_LP_CALL = "formatter.insertspace.before.lp.call";
 	public static final String INSERT_SPACE_AFTER_LP_CALL = "formatter.insertspace.after.lp.call";
 	public static final String INSERT_SPACE_BEFORE_RP_CALL = "formatter.insertspace.before.rp.call";
 
 	// EXPRESSION spaces
 	public static final String INSERT_SPACE_BEFORE_LP_EXPRESSION = "formatter.insertspace.before.lp.expression";
 	public static final String INSERT_SPACE_AFTER_LP_EXPRESSION = "formatter.insertspace.after.lp.expression";
 	public static final String INSERT_SPACE_BEFORE_RP_EXPRESSION = "formatter.insertspace.before.rp.expression";
 
 	// FUNCTION spaces
 	public static final String INSERT_SPACE_BEFORE_LP_FUNCTION_ARGUMENTS = "formatter.insertspace.before.lp.function";
 	public static final String INSERT_SPACE_AFTER_LP_FUNCTION_ARGUMENTS = "formatter.insertspace.after.lp.function";
 	public static final String INSERT_SPACE_BEFORE_RP_FUNCTION_ARGUMENTS = "formatter.insertspace.before.rp.function";
 	public static final String INSERT_SPACE_BETWEEN_PARENS_FUNCTION_NO_ARGUMENTS = "formatter.insertspace.between.parents.empty";
 
 	// CATCH spaces
 	public static final String INSERT_SPACE_BEFORE_LP_CATCH = "formatter.insertspace.before.lp.catch";
 	public static final String INSERT_SPACE_AFTER_LP_CATCH = "formatter.insertspace.after.lp.catch";
 	public static final String INSERT_SPACE_BEFORE_RP_CATCH = "formatter.insertspace.before.rp.catch";
 
 	// WITH spaces
 	public static final String INSERT_SPACE_BEFORE_LP_WITH = "formatter.insertspace.before.lp.with";
 	public static final String INSERT_SPACE_AFTER_LP_WITH = "formatter.insertspace.after.lp.with";
 	public static final String INSERT_SPACE_BEFORE_RP_WITH = "formatter.insertspace.before.rp.with";
 
 	// Register options here
 
 	private static void registerOptions() {
 
 		registerStringOption(FORMATTER_TAB_CHAR, "\t");
 		registerIntegerOption(FORMATTER_TAB_SIZE, 4);
 		registerIntegerOption(FORMATTER_INDENTATION_SIZE, 2);
 
 		registerIntegerOption(LINES_PRESERVE, -1);
 
 		registerBooleanOption(WRAP_COMMENTS, true);
 		registerIntegerOption(WRAP_COMMENTS_LENGTH, 80);
 
 		// Miss this options!
 		// registerStringOption(FORMATTER_PROFILES);
 		// registerStringOption(FORMATTER_ACTIVE_PROFILE);
 
 		registerBooleanOption(INDENT_METHOD, true);
 		registerBooleanOption(INDENT_BLOCK, true);
 		registerBooleanOption(INDENT_SWITCH, true);
 		registerBooleanOption(INDENT_CASE, true);
 		registerBooleanOption(INDENT_BREAK, true);
 		registerBooleanOption(INDENT_EMPTY_LINES, true);
 
 		registerStringOption(BRACE_METHOD, BRACE_SAME_LINE);
 		registerStringOption(BRACE_BLOCK, BRACE_SAME_LINE);
 		registerStringOption(BRACE_SWITCH, BRACE_SAME_LINE);
 		registerStringOption(BRACE_CASE, BRACE_SAME_LINE);
 		registerStringOption(BRACE_ARRAY, BRACE_SAME_LINE);
 		registerBooleanOption(BRACE_EMPTY_ARRAY, true);
 
 		registerBooleanOption(NEW_LINE_IN_EMPTY_METHOD, false);
 		registerBooleanOption(NEW_LINE_IN_EMPTY_BLOCK, false);
 		registerBooleanOption(NEW_LINE_AT_EOF, true);
 
 		registerBooleanOption(NEW_LINE_BEFORE_ELSE_IN_IF, false);
 		registerBooleanOption(NEW_LINE_BEFORE_CATCH_IN_TRY, false);
 		registerBooleanOption(NEW_LINE_BEFORE_FINALLY_IN_TRY, false);
 		registerBooleanOption(NEW_LINE_BEFORE_WHILE_IN_DO, false);
 
 		registerBooleanOption(KEEP_THEN_ON_SAME_LINE, false);
 		registerBooleanOption(KEEP_SIMPLE_IF_ON_ONE_LINE, false);
 
 		// registerBooleanOption(KEEP_ELSE_ON_SAME_LINE, true);
 		registerBooleanOption(KEEP_ELSE_IF_ON_ONE_LINE, true);
 		registerBooleanOption(KEEP_RETURN_ON_ONE_LINE, false);
 		registerBooleanOption(KEEP_THROW_ON_ONE_LINE, false);
 
 		// IF spaces
 		registerBooleanOption(INSERT_SPACE_BEFORE_LP_IF, true);
 		registerBooleanOption(INSERT_SPACE_AFTER_LP_IF, false);
 		registerBooleanOption(INSERT_SPACE_BEFORE_RP_IF, false);
 
 		// FOR spaces
 		registerBooleanOption(INSERT_SPACE_BEFORE_LP_FOR, true);
 		registerBooleanOption(INSERT_SPACE_AFTER_LP_FOR, false);
 		registerBooleanOption(INSERT_SPACE_BEFORE_RP_FOR, false);
 
 		// SWITCH spaces
 		registerBooleanOption(INSERT_SPACE_BEFORE_LP_SWITCH, true);
 		registerBooleanOption(INSERT_SPACE_AFTER_LP_SWITCH, false);
 		registerBooleanOption(INSERT_SPACE_BEFORE_RP_SWITCH, false);
 
 		// WHILE-DO..WHILE spaces
 		registerBooleanOption(INSERT_SPACE_BEFORE_LP_WHILE, true);
 		registerBooleanOption(INSERT_SPACE_AFTER_LP_WHILE, false);
 		registerBooleanOption(INSERT_SPACE_BEFORE_RP_WHILE, false);
 
 		// CALL spaces
 		registerBooleanOption(INSERT_SPACE_BEFORE_LP_CALL, false);
 		registerBooleanOption(INSERT_SPACE_AFTER_LP_CALL, false);
 		registerBooleanOption(INSERT_SPACE_BEFORE_RP_CALL, false);
 
 		// EXPRESSION spaces
 		registerBooleanOption(INSERT_SPACE_BEFORE_LP_EXPRESSION, true);
 		registerBooleanOption(INSERT_SPACE_AFTER_LP_EXPRESSION, false);
 		registerBooleanOption(INSERT_SPACE_BEFORE_RP_EXPRESSION, false);
 
 		// FUNCTION spaces
 		registerBooleanOption(INSERT_SPACE_BEFORE_LP_FUNCTION_ARGUMENTS, true);
 		registerBooleanOption(INSERT_SPACE_AFTER_LP_FUNCTION_ARGUMENTS, false);
 		registerBooleanOption(INSERT_SPACE_BEFORE_RP_FUNCTION_ARGUMENTS, false);
 		registerBooleanOption(
 				INSERT_SPACE_BETWEEN_PARENS_FUNCTION_NO_ARGUMENTS, false);
 
 		// CATCH spaces
 		registerBooleanOption(INSERT_SPACE_BEFORE_LP_CATCH, true);
 		registerBooleanOption(INSERT_SPACE_AFTER_LP_CATCH, false);
 		registerBooleanOption(INSERT_SPACE_BEFORE_RP_CATCH, false);
 
 		// WITH spaces
 		registerBooleanOption(INSERT_SPACE_BEFORE_LP_WITH, true);
 		registerBooleanOption(INSERT_SPACE_AFTER_LP_WITH, false);
 		registerBooleanOption(INSERT_SPACE_BEFORE_RP_WITH, false);
 
 	}
 
 	// //////////////////////////////////////////////////////////////////////////
 	// //////////////////////////////////////////////////////////////////////////
 	// //////////////////////////////////////////////////////////////////////////
 	// //////////////////////////////////////////////////////////////////////////
 	// //////////////////////////////////////////////////////////////////////////
 
 	private static final Map<String, OptionInfo> options;
 	private static final String[] names;
 
 	static {
 		options = new HashMap<String, OptionInfo>();
 
 		registerOptions();
 
 		List<String> keys = new ArrayList<String>(options.keySet());
 		Collections.sort(keys);
 
 		names = keys.toArray(new String[keys.size()]);
 	}
 
 	// //////////////////////////////////////////////////////////////////////////
 	// //////////////////////////////////////////////////////////////////////////
 	// //////////////////////////////////////////////////////////////////////////
 	// //////////////////////////////////////////////////////////////////////////
 	// //////////////////////////////////////////////////////////////////////////
 
 	private final static class OptionInfo {
 		int type;
 		Object defaultValue;
 
 		public OptionInfo(int type, Object defaultValue) {
 			this.type = type;
 			this.defaultValue = defaultValue;
 		}
 	}
 
 	private static final int STRING = 0;
 	private static final int BOOLEAN = 1;
 	private static final int INT = 2;
 
 	private static void registerIntegerOption(String name, int defaultValue) {
 		internalRegisterOption(name, INT, new Integer(defaultValue));
 	}
 
 	private static void registerStringOption(String name, String defaultValue) {
 		internalRegisterOption(name, STRING, defaultValue);
 	}
 
 	private static void registerBooleanOption(String name, boolean defaultValue) {
 		internalRegisterOption(name, BOOLEAN, defaultValue ? Boolean.TRUE
 				: Boolean.FALSE);
 	}
 
 	private static void internalRegisterOption(String name, int type,
 			Object defaultValue) {
 		options.put(name, new OptionInfo(type, defaultValue));
 	}
 
 	public static boolean isDefined(String name) {
 		return options.containsKey(name);
 	}
 
 	public static boolean isBoolean(String name) {
 		if (!isDefined(name))
 			return false;
 
 		return options.get(name).type == BOOLEAN;
 	}
 
 	public static boolean isInteger(String name) {
 		if (!isDefined(name))
 			return false;
 
 		return options.get(name).type == INT;
 	}
 
 	public static boolean isString(String name) {
 		if (!isDefined(name))
 			return false;
 
 		return options.get(name).type == STRING;
 	}
 
 	public static String[] getNames() {
 		return names;
 	}
 
 	public static Object getDefaultValue(String name) {
 		if (!isDefined(name))
 			return null;
 
 		return options.get(name).defaultValue;
 	}
 
 	public static Map<String, Object> getDefaults() {
 		Map<String, Object> values = new HashMap<String, Object>();
 		for (Map.Entry<String, OptionInfo> entry : options.entrySet()) {
			values.put(entry.getKey(), entry.getValue());
 		}
 		return values;
 	}
 }
