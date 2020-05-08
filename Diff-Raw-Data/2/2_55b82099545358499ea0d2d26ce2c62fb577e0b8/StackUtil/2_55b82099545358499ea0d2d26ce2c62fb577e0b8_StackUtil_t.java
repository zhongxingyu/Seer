 /*
  * Feb 9, 2006
  */
 package com.thinkparity.codebase;
 
 
 /**
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 public class StackUtil {
 
 	/**
 	 * Obtain the name of the immediate caller from the stack.
 	 * 
 	 * @return The name of the immediate caller.
 	 */
 	public static String getCallerMethodName() {
		return getCallerMethodName(3);
 	}
 
 	/**
 	 * Obtain the caller method name at the given level.
 	 * 
 	 * @param level
 	 *            The level.
 	 * @return The caller method name.
 	 */
 	public static String getCallerMethodName(final Integer level) {
 		final StackTraceElement[] stack = new Throwable().getStackTrace();
 		if(null == stack) { return null; }
 		else {
 			if(stack.length > level) { return stack[level].getMethodName(); }
 			else { return null; }
 		}
 	}
 
 	/**
 	 * Create a StackUtil.
 	 * 
 	 */
 	private StackUtil() { super(); }
 }
