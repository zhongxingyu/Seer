 /**
  * Digi-Lib - base library for Digi components
  * 
  * Copyright (c) 2012 Alexey Aksenov ezh@ezh.msk.ru
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.digimead.digi.lib.log.logger;
 
 import org.slf4j.helpers.FormattingTuple;
 import org.slf4j.helpers.MarkerIgnoringBase;
 import org.slf4j.helpers.MessageFormatter;
 
 abstract class AbstractBaseLogger extends MarkerIgnoringBase implements
 		java.io.Serializable {
 	private static final long serialVersionUID = -9028148048410031021L;
 
 	AbstractBaseLogger(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Log a message at level TRACE according to the specified format and
 	 * argument.
 	 * 
 	 * <p>
 	 * This form avoids superfluous object creation when the logger is disabled
 	 * for level TRACE.
 	 * </p>
 	 * 
 	 * @param format
 	 *            the format string
 	 * @param arg
 	 *            the argument
 	 */
 	public void trace(String format, Object arg) {
 		if (isTraceEnabled()) {
 			FormattingTuple ft = MessageFormatter.format(format, arg);
 			trace(ft.getMessage());
 		}
 	}
 
 	/**
 	 * Log a message at level TRACE according to the specified format and
 	 * arguments.
 	 * 
 	 * <p>
 	 * This form avoids superfluous object creation when the logger is disabled
 	 * for the TRACE level.
 	 * </p>
 	 * 
 	 * @param format
 	 *            the format string
 	 * @param arg1
 	 *            the first argument
 	 * @param arg2
 	 *            the second argument
 	 */
 	public void trace(String format, Object arg1, Object arg2) {
 		if (isTraceEnabled()) {
 			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
 			trace(ft.getMessage());
 		}
 	}
 
 	/**
 	 * Log a message at level TRACE according to the specified format and
 	 * arguments.
 	 * 
 	 * <p>
 	 * This form avoids superfluous object creation when the logger is disabled
 	 * for the TRACE level.
 	 * </p>
 	 * 
 	 * @param format
 	 *            the format string
 	 * @param argArray
 	 *            an array of arguments
 	 */
 	public void trace(String format, Object... argArray) {
 		if (isTraceEnabled()) {
			if (argArray.length == 1 && argArray instanceof Object[]) {
 				// some crazy behavior from Log4jLoggerAdapter(Ceki
 				// G&uuml;lc&uuml;) That man override 'Object ...' with
 				// 'Object[]'
 				FormattingTuple ft = MessageFormatter.arrayFormat(format,
 						(Object[]) argArray[0]);
 				trace(ft.getMessage());
 			} else {
 				FormattingTuple ft = MessageFormatter.arrayFormat(format,
 						argArray);
 				trace(ft.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Log a message at level DEBUG according to the specified format and
 	 * argument.
 	 * 
 	 * <p>
 	 * This form avoids superfluous object creation when the logger is disabled
 	 * for level DEBUG.
 	 * </p>
 	 * 
 	 * @param format
 	 *            the format string
 	 * @param arg
 	 *            the argument
 	 */
 	public void debug(String format, Object arg) {
 		if (isDebugEnabled()) {
 			FormattingTuple ft = MessageFormatter.format(format, arg);
 			debug(ft.getMessage());
 		}
 	}
 
 	/**
 	 * Log a message at level DEBUG according to the specified format and
 	 * arguments.
 	 * 
 	 * <p>
 	 * This form avoids superfluous object creation when the logger is disabled
 	 * for the DEBUG level.
 	 * </p>
 	 * 
 	 * @param format
 	 *            the format string
 	 * @param arg1
 	 *            the first argument
 	 * @param arg2
 	 *            the second argument
 	 */
 	public void debug(String format, Object arg1, Object arg2) {
 		if (isDebugEnabled()) {
 			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
 			debug(ft.getMessage());
 		}
 	}
 
 	/**
 	 * Log a message at level DEBUG according to the specified format and
 	 * arguments.
 	 * 
 	 * <p>
 	 * This form avoids superfluous object creation when the logger is disabled
 	 * for the DEBUG level.
 	 * </p>
 	 * 
 	 * @param format
 	 *            the format string
 	 * @param argArray
 	 *            an array of arguments
 	 */
 	public void debug(String format, Object... argArray) {
 		if (isDebugEnabled()) {
			if (argArray.length == 1 && argArray instanceof Object[]) {
 				// some crazy behavior from Log4jLoggerAdapter(Ceki
 				// G&uuml;lc&uuml;) That man override 'Object ...' with
 				// 'Object[]'
 				FormattingTuple ft = MessageFormatter.arrayFormat(format,
 						(Object[]) argArray[0]);
 				debug(ft.getMessage());
 			} else {
 				FormattingTuple ft = MessageFormatter.arrayFormat(format,
 						argArray);
 				debug(ft.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Log a message at level INFO according to the specified format and
 	 * argument.
 	 * 
 	 * <p>
 	 * This form avoids superfluous object creation when the logger is disabled
 	 * for level INFO.
 	 * </p>
 	 * 
 	 * @param format
 	 *            the format string
 	 * @param arg
 	 *            the argument
 	 */
 	public void info(String format, Object arg) {
 		if (isInfoEnabled()) {
 			FormattingTuple ft = MessageFormatter.format(format, arg);
 			info(ft.getMessage());
 		}
 	}
 
 	/**
 	 * Log a message at level INFO according to the specified format and
 	 * arguments.
 	 * 
 	 * <p>
 	 * This form avoids superfluous object creation when the logger is disabled
 	 * for the INFO level.
 	 * </p>
 	 * 
 	 * @param format
 	 *            the format string
 	 * @param arg1
 	 *            the first argument
 	 * @param arg2
 	 *            the second argument
 	 */
 	public void info(String format, Object arg1, Object arg2) {
 		if (isInfoEnabled()) {
 			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
 			info(ft.getMessage());
 		}
 	}
 
 	/**
 	 * Log a message at level INFO according to the specified format and
 	 * arguments.
 	 * 
 	 * <p>
 	 * This form avoids superfluous object creation when the logger is disabled
 	 * for the INFO level.
 	 * </p>
 	 * 
 	 * @param format
 	 *            the format string
 	 * @param argArray
 	 *            an array of arguments
 	 */
 	public void info(String format, Object... argArray) {
 		if (isInfoEnabled()) {
			if (argArray.length == 1 && argArray instanceof Object[]) {
 				// some crazy behavior from Log4jLoggerAdapter(Ceki
 				// G&uuml;lc&uuml;) That man override 'Object ...' with
 				// 'Object[]'
 				FormattingTuple ft = MessageFormatter.arrayFormat(format,
 						(Object[]) argArray[0]);
 				info(ft.getMessage());
 			} else {
 				FormattingTuple ft = MessageFormatter.arrayFormat(format,
 						argArray);
 				info(ft.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Log a message at level WARN according to the specified format and
 	 * argument.
 	 * 
 	 * <p>
 	 * This form avoids superfluous object creation when the logger is disabled
 	 * for level WARN.
 	 * </p>
 	 * 
 	 * @param format
 	 *            the format string
 	 * @param arg
 	 *            the argument
 	 */
 	public void warn(String format, Object arg) {
 		if (isWarnEnabled()) {
 			FormattingTuple ft = MessageFormatter.format(format, arg);
 			warn(ft.getMessage());
 		}
 	}
 
 	/**
 	 * Log a message at level WARN according to the specified format and
 	 * arguments.
 	 * 
 	 * <p>
 	 * This form avoids superfluous object creation when the logger is disabled
 	 * for the WARN level.
 	 * </p>
 	 * 
 	 * @param format
 	 *            the format string
 	 * @param arg1
 	 *            the first argument
 	 * @param arg2
 	 *            the second argument
 	 */
 	public void warn(String format, Object arg1, Object arg2) {
 		if (isWarnEnabled()) {
 			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
 			warn(ft.getMessage());
 		}
 	}
 
 	/**
 	 * Log a message at level WARN according to the specified format and
 	 * arguments.
 	 * 
 	 * <p>
 	 * This form avoids superfluous object creation when the logger is disabled
 	 * for the WARN level.
 	 * </p>
 	 * 
 	 * @param format
 	 *            the format string
 	 * @param argArray
 	 *            an array of arguments
 	 */
 	public void warn(String format, Object... argArray) {
 		if (isWarnEnabled()) {
			if (argArray.length == 1 && argArray instanceof Object[]) {
 				// some crazy behavior from Log4jLoggerAdapter(Ceki
 				// G&uuml;lc&uuml;) That man override 'Object ...' with
 				// 'Object[]'
 				FormattingTuple ft = MessageFormatter.arrayFormat(format,
 						(Object[]) argArray[0]);
 				warn(ft.getMessage());
 			} else {
 				FormattingTuple ft = MessageFormatter.arrayFormat(format,
 						argArray);
 				warn(ft.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Log a message at level ERROR according to the specified format and
 	 * argument.
 	 * 
 	 * <p>
 	 * This form avoids superfluous object creation when the logger is disabled
 	 * for level ERROR.
 	 * </p>
 	 * 
 	 * @param format
 	 *            the format string
 	 * @param arg
 	 *            the argument
 	 */
 	public void error(String format, Object arg) {
 		if (isErrorEnabled()) {
 			FormattingTuple ft = MessageFormatter.format(format, arg);
 			error(ft.getMessage());
 		}
 	}
 
 	/**
 	 * Log a message at level ERROR according to the specified format and
 	 * arguments.
 	 * 
 	 * <p>
 	 * This form avoids superfluous object creation when the logger is disabled
 	 * for the ERROR level.
 	 * </p>
 	 * 
 	 * @param format
 	 *            the format string
 	 * @param arg1
 	 *            the first argument
 	 * @param arg2
 	 *            the second argument
 	 */
 	public void error(String format, Object arg1, Object arg2) {
 		if (isErrorEnabled()) {
 			FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
 			error(ft.getMessage());
 		}
 	}
 
 	/**
 	 * Log a message at level ERROR according to the specified format and
 	 * arguments.
 	 * 
 	 * <p>
 	 * This form avoids superfluous object creation when the logger is disabled
 	 * for the ERROR level.
 	 * </p>
 	 * 
 	 * @param format
 	 *            the format string
 	 * @param argArray
 	 *            an array of arguments
 	 */
 	public void error(String format, Object... argArray) {
 		if (isErrorEnabled()) {
			if (argArray.length == 1 && argArray instanceof Object[]) {
 				// some crazy behavior from Log4jLoggerAdapter(Ceki
 				// G&uuml;lc&uuml;) That man override 'Object ...' with
 				// 'Object[]'
 				FormattingTuple ft = MessageFormatter.arrayFormat(format,
 						(Object[]) argArray[0]);
 				error(ft.getMessage());
 			} else {
 				FormattingTuple ft = MessageFormatter.arrayFormat(format,
 						argArray);
 				error(ft.getMessage());
 			}
 		}
 	}
 }
