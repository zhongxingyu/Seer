 /*******************************************************************************
  * Copyright (c) May 18, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdkcli.internal.options;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.cli.Options;
 import org.zend.sdkcli.internal.commands.AbstractCommand;
 
 /**
  * detects options in command according annotation
  * 
  * @see Option
  * @author Roy, 2011
  */
 public class DetectOptionUtility {
 
 	private static final String BOOLEAN_OPTION = "is";
 	private static final String STRING_OPTION = "get";
 
 	public static void addOption(Class subject, Options options) {
 		addOption(subject, options, false);
 	}
 
 	public static void addOption(Class subject, Options options,
 			boolean specific) {
		final Method[] methods = subject.getMethods();
 
 		for (Method method : methods) {

			// skip general options if specific
			if (specific && AbstractCommand.class.equals(method.getDeclaringClass())) {
				break;
			}
			
 			if (isString(method) && hasOptionAnnotation(method)) {
 				addString(options, method);
 			} else if (isBoolean(method) && hasOptionAnnotation(method)) {
 				addBoolean(options, method);
 
 			}
 		}
 	}
 
 	/**
 	 * List all options in a command, leaving out the global options if the
 	 * ignoreGlobal is true
 	 * 
 	 * @param subject
 	 * @param ignoreGlobal
 	 * @return the list of options
 	 */
 	public static List<Option> getOptions(Class subject, boolean ignoreGlobal) {
 
 		final Method[] methods = subject.getMethods();
 
 		List<Option> result = new ArrayList<Option>();
 		for (Method method : methods) {
 			final boolean isDeclaredGlobal = method.getDeclaringClass().equals(
 					AbstractCommand.class);
 			if (hasOptionAnnotation(method)
 					&& (isString(method) || isBoolean(method))
 					&& (!isDeclaredGlobal || !ignoreGlobal)) {
 				result.add(method.getAnnotation(Option.class));
 			}
 		}
 		return result;
 	}
 
 	private final static boolean hasOptionAnnotation(Method method) {
 		return method.getAnnotation(Option.class) != null;
 	}
 
 	private static void addBoolean(Options options, Method method) {
 		final Option a = method.getAnnotation(Option.class);
 
 		// create option object
 		final org.apache.commons.cli.Option o = new org.apache.commons.cli.Option(
 				a.opt(), a.description());
 		if (a.longOpt() != null && a.longOpt().length() > 0) {
 			o.setLongOpt(a.longOpt());
 		}
 		o.setArgs(0);
 		o.setRequired(a.required());
 
 		// assign to options list options.addOption(o);
 		options.addOption(o);
 	}
 
 	private static void addString(Options options, Method method) {
 		final Option a = method.getAnnotation(Option.class);
 
 		// create option object
 		final org.apache.commons.cli.Option o = new org.apache.commons.cli.Option(
 				a.opt(), a.description());
 		if (a.longOpt() != null && a.longOpt().length() > 0) {
 			o.setLongOpt(a.longOpt());
 		}
 		o.setArgs(a.numberOfArgs());
 		o.setRequired(a.required());
 		o.setType(a.type());
 		o.setArgName(a.argName());
 
 		// assign to options list
 		options.addOption(o);
 	}
 
 	private static boolean isBoolean(Method method) {
 		return method.getName().startsWith(BOOLEAN_OPTION);
 	}
 
 	private static boolean isString(Method method) {
 		return method.getName().startsWith(STRING_OPTION);
 	}
 }
