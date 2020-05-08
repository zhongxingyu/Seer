 // This file is part of PleoCommand:
 // Interactively control Pleo with psychobiological parameters
 //
 // Copyright (C) 2010 Oliver Hoffmann - Hoffmann_Oliver@gmx.de
 //
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 51 Franklin Street, Boston, USA.
 
 package pleocmd;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import pleocmd.itfc.gui.ErrorDialog;
 import pleocmd.itfc.gui.MainFrame;
 
 /**
  * Contains all relevant content of one log message (one line in log view) as
  * well as static methods to create log messages of any {@link Type}.
  * 
  * @author oliver
  */
 public final class Log {
 
 	/**
 	 * Specifies the type of a message.
 	 * 
 	 * @author oliver
 	 */
 	public enum Type {
 		/**
 		 * Detailed or debug messages.
 		 */
 		Detail,
 		/**
 		 * Informational messages.
 		 */
 		Info,
 		/**
 		 * Warnings and other severe messages.
 		 */
 		Warn,
 		/**
 		 * Errors and Exceptions.
 		 */
 		Error,
 		/**
 		 * The standard output is captured via this {@link Type} in GUI mode.<br>
 		 * Will not be used in console mode.
 		 */
 		ConsoleOutput,
 		/**
 		 * The standard input is represented via this {@link Type} in GUI mode.<br>
 		 * Will not be used in console mode.
 		 */
 		ConsoleInput
 	}
 
 	private static boolean minLogTypeKnown;
 
 	private static boolean quiStatusKnown;
 
 	private static List<Log> queuedLogs = new ArrayList<Log>(128);
 
 	private final Type type;
 
 	private final StackTraceElement caller;
 
 	private final String msg;
 
 	private final Throwable backtrace;
 
 	private final long time;
 
 	private Log(final Type type, final StackTraceElement caller,
 			final String msg, final String msgAlt, final Throwable backtrace) {
 		this.type = type;
 		this.caller = caller;
 		this.msg = msgAlt == null ? msg : msgAlt;
 		this.backtrace = backtrace;
 		time = System.currentTimeMillis();
 		switch (type) {
 		case Error:
 			if (MainFrame.hasGUI()) {
 				MainFrame.the().addLog(this);
 				ErrorDialog.show(this);
 			} else if (quiStatusKnown)
 				System.err.println(toString()); // CS_IGNORE
 			else
 				queuedLogs.add(this);
 			break;
 		case ConsoleOutput:
 			System.out.println(msg); // CS_IGNORE
 			if (MainFrame.hasGUI())
 				MainFrame.the().addLog(this);
 			else if (!quiStatusKnown) queuedLogs.add(this);
 			break;
 		case ConsoleInput:
 			if (MainFrame.hasGUI())
 				MainFrame.the().addLog(this);
 			else if (!quiStatusKnown) queuedLogs.add(this);
 			break;
 		default:
 			if (!minLogTypeKnown)
 				// we have to queue it for later output, because
 				// we currently don't now, if we really have to output this log
 				queuedLogs.add(this);
 			else if (MainFrame.hasGUI())
 				MainFrame.the().addLog(this);
 			else if (quiStatusKnown)
 				System.err.println(toString()); // CS_IGNORE
 			else
 				queuedLogs.add(this);
 			break;
 		}
 	}
 
 	/**
 	 * @return the {@link Type} of this log entry.
 	 */
 	public Type getType() {
 		return type;
 	}
 
 	/**
 	 * @return a {@link StackTraceElement} describing the method which created
 	 *         this log entry.
 	 */
 	public StackTraceElement getCaller() {
 		return caller;
 	}
 
 	/**
 	 * @return a {@link String} describing the class and method which created
 	 *         this log entry.
 	 */
 	public String getFormattedCaller() {
 		return String.format("%s.%s()",
 				caller.getClassName().replaceFirst("^.*\\.([^.]*)$", "$1"),
 				caller.getMethodName());
 	}
 
 	/**
 	 * @return a {@link String} with the message of this log entry.
 	 */
 	public String getMsg() {
 		return msg;
 	}
 
 	/**
 	 * @return the complete backtrace for this log entry if this log is an
 	 *         {@link Type#Error} or backtracing for all kind of logs has been
 	 *         enabled or <b>null</b> if not.
 	 */
 	public Throwable getBacktrace() {
 		return backtrace;
 	}
 
 	/**
 	 * @return the time in milliseconds since the epoch when this log entry has
 	 *         been created.
 	 */
 	public long getTime() {
 		return time;
 	}
 
 	/**
 	 * @return a {@link Color} matching the {@link Type} of this log entry.
 	 * @see #getType()
 	 */
 	public Color getTypeColor() {
 		switch (type) {
 		case Detail:
 			return Color.GRAY;
 		case Info:
 			return Color.BLUE;
 		case Warn:
 			return new Color(160, 100, 0); // dark orange
 		case Error:
 			return Color.RED;
 		case ConsoleOutput:
 		case ConsoleInput:
 		default:
 			return Color.BLACK;
 		}
 	}
 
 	/**
 	 * @return a {@link String} with an HTML color code matching the
 	 *         {@link Type} of this log entry.
 	 * @see #getType()
 	 */
 	public String getTypeHTMLColor() {
 		switch (type) {
 		case Detail:
 			return "gray";
 		case Info:
 			return "blue";
 		case Warn:
 			return "#A06400";// dark orange
 		case Error:
 			return "red";
 		case ConsoleOutput:
 		case ConsoleInput:
 		default:
 			return "black";
 		}
 	}
 
 	/**
 	 * @return a {@link String} with an Latex color code matching the
 	 *         {@link Type} of this log entry.
 	 * @see #getType()
 	 */
 	public String getTypeTexColor() {
 		switch (type) {
 		case Detail:
 			return "gray";
 		case Info:
 			return "blue";
 		case Warn:
 			return "orange";
 		case Error:
 			return "red";
 		case ConsoleOutput:
 		case ConsoleInput:
 		default:
 			return "black";
 		}
 	}
 
 	/**
 	 * @return the time (with milliseconds - no date), from {@link #getTime()}
 	 *         formatted as a {@link String}
 	 */
 	public String getFormattedTime() {
 		return LogConfig.DATE_FORMATTER.format(new Date(time));
 	}
 
 	/**
 	 * @return a three character long {@link String} matching the {@link Type}
 	 *         of this log entry.
 	 * @see #getType()
 	 */
 	public String getTypeShortString() {
 		switch (type) {
 		case Detail:
 			return "DTL";
 		case Info:
 			return "INF";
 		case Warn:
 			return "WRN";
 		case Error:
 			return "ERR";
 		case ConsoleOutput:
 			return "OUT";
 		case ConsoleInput:
 			return "IN ";
 		default:
 			return "!?!";
 		}
 	}
 
 	@Override
 	public String toString() {
 		final StringBuilder sb = new StringBuilder();
 		final String ec = getExportColumns();
 		if (ec.contains("T")) {
 			if (sb.length() > 0) sb.append(' ');
 			sb.append(getFormattedTime());
 		}
 		if (ec.contains("Y")) {
 			if (sb.length() > 0) sb.append(' ');
 			sb.append(getTypeShortString());
 		}
 		if (ec.contains("S")) {
 			if (sb.length() > 0) sb.append(' ');
 			final String s = caller.toString();
 			sb.append(String.format("%-50s",
 					s.substring(0, Math.min(50, s.length()))));
 		}
 		if (ec.contains("M")) {
 			if (sb.length() > 0) sb.append(' ');
 			final StringBuilder sb2 = new StringBuilder();
 			for (int i = 0; i < sb.length(); ++i)
 				sb2.append(' ');
 			sb.append(StringManip.removePseudoHTML(msg).replace("\n",
 					"\n" + sb2.toString()));
 		}
 		return sb.toString();
 	}
 
 	public String toHTMLString() {
 		final StringBuilder sb = new StringBuilder();
 		final String ec = getExportColumns();
 		if (ec.contains("T")) {
 			sb.append("<td>");
 			sb.append(StringManip.safeHTML(getFormattedTime()));
 			sb.append("</td>");
 		}
 		if (ec.contains("Y")) {
 			sb.append("<td>");
 			sb.append(StringManip.safeHTML(getTypeShortString()));
 			sb.append("</td>");
 		}
 		if (ec.contains("S")) {
 			sb.append("<td>");
 			sb.append(StringManip.safeHTML(caller.toString()));
 			sb.append("</td>");
 		}
 		if (ec.contains("M")) {
 			sb.append("<td>");
 			sb.append(StringManip.convertPseudoToRealHTML(msg));
 			sb.append("</td>");
 		}
 		return sb.toString();
 	}
 
 	public String toTexString(final Set<String> colorNames) {
 		final StringBuilder sb = new StringBuilder();
 		final String ec = getExportColumns();
 		int cnt = 0;
 		if (ec.contains("T")) {
 			if (sb.length() > 0) sb.append(" & ");
 			sb.append(String.format("\\textcolor{%s}{", getTypeTexColor()));
 			sb.append(StringManip.safeTex(getFormattedTime()));
 			sb.append("}");
 			++cnt;
 		}
 		if (ec.contains("Y")) {
 			if (sb.length() > 0) sb.append(" & ");
 			sb.append(String.format("\\textcolor{%s}{", getTypeTexColor()));
 			sb.append(StringManip.safeTex(getTypeShortString()));
 			sb.append("}");
 			++cnt;
 		}
 		if (ec.contains("S")) {
 			if (sb.length() > 0) sb.append(" & ");
 			sb.append(String.format("\\textcolor{%s}{", getTypeTexColor()));
 			sb.append(StringManip.safeTex(caller.toString()));
 			sb.append("}");
 			++cnt;
 		}
 		if (ec.contains("M")) {
 			if (sb.length() > 0) sb.append(" & ");
 			sb.append(String.format("\\textcolor{%s}{", getTypeTexColor()));
 			final StringBuilder sb2 = new StringBuilder("}\\\\\n ");
 			for (int i = 0; i < cnt; ++i)
 				sb2.append("& ");
 			sb2.append(String.format("\\textcolor{%s}{", getTypeTexColor()));
 			sb.append(StringManip.convertPseudoHTMLToTex(msg, colorNames)
 					.replace("\\\\\n", sb2.toString()));
 			sb.append("}");
 		}
 		return sb.toString();
 	}
 
 	private static StackTraceElement getCallerSTE(final int stepsBack) {
 		final StackTraceElement[] st = new Throwable().getStackTrace();
 		return st.length < stepsBack ? st[st.length - 1] : st[stepsBack];
 	}
 
 	/**
 	 * Prints messages to the GUI's log, if any, or to the standard error
 	 * otherwise if their {@link #type} is not "lower" than the
 	 * {@link LogConfig#CFG_MIN_LOG_TYPE}.<br>
 	 * Always prints messages of type Console to the standard output (instead of
 	 * standard error) no matter if a GUI exists or not.
 	 * 
 	 * @param type
 	 *            type {@link Type} of the message
 	 * @param throwable
 	 *            a backtrace for the message or <b>null</b>
 	 * @param msg
 	 *            the message - interpreted as a format string (like in
 	 *            {@link String#format(String, Object...)}) if any arguments are
 	 *            given or just used as is otherwise.
 	 * @param msgAlt
 	 *            if not <b>null</b>, this is the message which will be
 	 *            displayed in the GUI, while "msg" will only be used for the
 	 *            standard-output
 	 * @param caller
 	 *            the name of the creator of this message
 	 * @param args
 	 *            arbitrary number of arguments for the format string (may also
 	 *            be zero)
 	 */
 	private static void msg(final Type type, final Throwable throwable,
 			final StackTraceElement caller, final String msg,
 			final String msgAlt, final Object... args) {
 		if (type.ordinal() >= LogConfig.CFG_MIN_LOG_TYPE.getEnum().ordinal()) {
 			final String msgStr = args.length == 0 ? msg : String.format(msg,
 					args);
 			final String msgAltStr = msgAlt == null || args.length == 0 ? msgAlt
 					: String.format(msgAlt, args);
 			new Log(type, caller, msgStr, msgAltStr, throwable);
 		}
 	}
 
 	/**
 	 * Creates a new message of {@link Type#Detail} which will be printed to
 	 * error output or send to the GUI.
 	 * 
 	 * @param msg
 	 *            the message - interpreted as a format string (like in
 	 *            {@link String#format(String, Object...)}) if any arguments are
 	 *            given or just used as is otherwise.
 	 * @param args
 	 *            arbitrary number of arguments for the format string (may also
 	 *            be zero)
 	 */
 	public static void detail(final String msg, final Object... args) {
 		msg(Type.Detail, null, getCallerSTE(2), msg, null, args);
 	}
 
 	/**
 	 * Creates a new message of {@link Type#Info} which will be printed to error
 	 * output or send to the GUI.
 	 * 
 	 * @param msg
 	 *            the message - interpreted as a format string (like in
 	 *            {@link String#format(String, Object...)}) if any arguments are
 	 *            given or just used as is otherwise.
 	 * @param args
 	 *            arbitrary number of arguments for the format string (may also
 	 *            be zero)
 	 */
 	public static void info(final String msg, final Object... args) {
 		msg(Type.Info, null, getCallerSTE(2), msg, null, args);
 	}
 
 	/**
 	 * Creates a new message of {@link Type#Warn} which will be printed to error
 	 * output or send to the GUI.
 	 * 
 	 * @param msg
 	 *            the message - interpreted as a format string (like in
 	 *            {@link String#format(String, Object...)}) if any arguments are
 	 *            given or just used as is otherwise.
 	 * @param args
 	 *            arbitrary number of arguments for the format string (may also
 	 *            be zero)
 	 */
 	public static void warn(final String msg, final Object... args) {
 		msg(Type.Warn, null, getCallerSTE(2), msg, null, args);
 	}
 
 	/**
 	 * Creates a new message of {@link Type#Error} which will be printed to
 	 * error output or send to the GUI.
 	 * 
 	 * @param msg
 	 *            the message - interpreted as a format string (like in
 	 *            {@link String#format(String, Object...)}) if any arguments are
 	 *            given or just used as is otherwise.
 	 * @param args
 	 *            arbitrary number of arguments for the format string (may also
 	 *            be zero)
 	 */
 	public static void error(final String msg, final Object... args) {
 		msg(Type.Error, null, getCallerSTE(2), msg, null, args);
 	}
 
 	/**
 	 * Creates a new message of {@link Type#ConsoleOutput} which will be printed
 	 * to standard output <b>and</b> send to the GUI.
 	 * 
 	 * @param msg
 	 *            the message - interpreted as a format string (like in
 	 *            {@link String#format(String, Object...)}) if any arguments are
 	 *            given or just used as is otherwise.
 	 * @param args
 	 *            arbitrary number of arguments for the format string (may also
 	 *            be zero)
 	 */
 	public static void consoleOut(final String msg, final Object... args) {
 		msg(Type.ConsoleOutput, null, getCallerSTE(2), msg, null, args);
 	}
 
 	/**
 	 * Creates a new message of {@link Type#ConsoleOutput} which will be printed
 	 * to standard output <b>and</b> send to the GUI.
 	 * 
 	 * @param msgStdOut
 	 *            the message - interpreted as a format string (like in
 	 *            {@link String#format(String, Object...)}) if any arguments are
 	 *            given or just used as is otherwise. This one will be printed
 	 *            to the standard output.
 	 * @param msgGUI
 	 *            the message - interpreted as a format string (like in
 	 *            {@link String#format(String, Object...)}) if any arguments are
 	 *            given or just used as is otherwise. This one will be added to
 	 *            the GUI's log if GUI is currently available.
 	 * @param args
 	 *            arbitrary number of arguments for the format string (may also
 	 *            be zero)
 	 */
 	public static void consoleOut2(final String msgStdOut, final String msgGUI,
 			final Object... args) {
 		msg(Type.ConsoleOutput, null, getCallerSTE(2), msgStdOut, msgGUI, args);
 	}
 
 	/**
 	 * Creates a new message of {@link Type#ConsoleInput} which will be send to
 	 * the GUI and represents a message from standard input.
 	 * 
 	 * @param msg
 	 *            the message - interpreted as a format string (like in
 	 *            {@link String#format(String, Object...)}) if any arguments are
 	 *            given or just used as is otherwise.
 	 * @param args
 	 *            arbitrary number of arguments for the format string (may also
 	 *            be zero)
 	 */
 	public static void consoleIn(final String msg, final Object... args) {
 		msg(Type.ConsoleInput, null, getCallerSTE(2), msg, null, args);
 	}
 
 	/**
 	 * Creates a new message of {@link Type#Error} which will be printed to
 	 * error output or send to the GUI. This message will contain a complete
 	 * backtrace.
 	 * 
 	 * @param throwable
 	 *            the {@link Exception} that occurred as a reason for this
 	 *            message.
 	 * @see #getBacktrace()
 	 */
 	public static void error(final Throwable throwable) {
 		error(throwable, "");
 	}
 
 	/**
 	 * Creates a new message of {@link Type#Error} which will be printed to
 	 * error output or send to the GUI. This message will contain a complete
 	 * backtrace.
 	 * 
 	 * @param throwable
 	 *            the {@link Exception} that occurred as a reason for this
 	 *            message.
 	 * @param msg
 	 *            an optional message prepended before the exception -
 	 *            interpreted as a format string (like in
 	 *            {@link String#format(String, Object...)}) if any arguments are
 	 *            given or just used as is otherwise.
 	 * @param args
 	 *            arbitrary number of arguments for the format string (may also
 	 *            be zero) - will be ignored if the message string is empty.
 	 * @see #getBacktrace()
 	 */
 	public static void error(final Throwable throwable, final String msg,
 			final Object... args) {
 		final StringBuilder sb = new StringBuilder();
 		if (!msg.isEmpty()) {
 			sb.append(args.length == 0 ? msg : String.format(msg, args));
 			sb.append(": ");
 		}
 		Throwable t = throwable;
 		while (true) {
 			sb.append(t.getClass().getSimpleName());
 			sb.append(": '");
 			sb.append(t.getMessage());
 			sb.append("'");
 			if (t.getCause() == null) break;
 			t = t.getCause();
 			sb.append(" caused by ");
 		}
 
 		msg(Type.Error, throwable, t.getStackTrace()[0], null, sb.toString());
 	}
 
 	/**
 	 * Sets the "lowest" {@link Type} which will be processed. Messages of
 	 * "lower" types will be ignored.
 	 * <p>
 	 * Possible values:
 	 * <table>
 	 * <tr>
 	 * <td>{@link Type#Detail}</td>
 	 * <td>all messages will be processed</td>
 	 * </tr>
 	 * <tr>
 	 * <td>{@link Type#Info}</td>
 	 * <td>all but detailed messages will be processed</td>
 	 * </tr>
 	 * <tr>
 	 * <td>{@link Type#Warn}</td>
 	 * <td>only warnings and errors will be processed</td>
 	 * </tr>
 	 * <tr>
 	 * <td>{@link Type#Error}</td>
 	 * <td>only errors will be processed</td>
 	 * </tr>
 	 * <tr>
 	 * <td>{@link Type#ConsoleOutput}</td>
 	 * <td>no messages will be processed</td>
 	 * </tr>
 	 * </table>
 	 * <p>
 	 * Note that messages of {@link Type#ConsoleOutput} and
 	 * {@link Type#ConsoleInput} are treated as wrapped standard output and not
 	 * as normal messages and are therefore always be processed even if
 	 * minLogType is set to {@link Type#ConsoleOutput}.
 	 * 
 	 * @param minLogType
 	 *            true if {@link Type#Detail} will be processed
 	 */
 	public static void setMinLogType(final Type minLogType) {
 		if (minLogType.ordinal() < Type.Detail.ordinal()
 				|| minLogType.ordinal() > Type.ConsoleOutput.ordinal())
 			throw new IllegalArgumentException("Invalid value for minLogType");
 		LogConfig.CFG_MIN_LOG_TYPE.setEnum(minLogType);
 		minLogTypeKnown = true;
 		processQueue();
 	}
 
 	public static void setGUIStatusKnown() {
 		quiStatusKnown = true;
 		processQueue();
 	}
 
 	private static void processQueue() {
 		// wait with processing until known
 		if (!minLogTypeKnown || !quiStatusKnown) return;
 
 		if (MainFrame.hasGUI()) {
 			for (final Log log : queuedLogs)
 				if (Log.canLog(log.getType())) {
 					MainFrame.the().addLog(log);
 					if (log.getType() == Type.Error) ErrorDialog.show(log);
 				}
 		} else
 			for (final Log log : queuedLogs)
 				if (Log.canLog(log.getType()))
 					System.err.println(log.toString()); // CS_IGNORE
 		queuedLogs.clear();
 	}
 
 	/**
 	 * @return the "lowest" {@link Type} which will be processed. Messages of
 	 *         "lower" types will be ignored.
 	 */
 	public static Type getMinLogType() {
 		return LogConfig.CFG_MIN_LOG_TYPE.getEnum();
 	}
 
 	/**
 	 * The columns of the LogTable, which should be exported with the to...
 	 * methods. Must contains characters 'T', 'Y', 'S' and 'M' for Time, Type,
 	 * Source and Message.
 	 * 
 	 * @return a String with one or more of "TYSM"
 	 */
 	public static String getExportColumns() {
 		return LogConfig.CFG_EXPORT_COLUMNS.getContent().toUpperCase();
 	}
 
 	/**
 	 * @param type
 	 *            one of the log {@link Type}s
 	 * @return true if messages of the given {@link Type} can be logged
 	 */
 	public static boolean canLog(final Type type) {
 		return type.ordinal() >= LogConfig.CFG_MIN_LOG_TYPE.getEnum().ordinal();
 	}
 
 	/**
 	 * @return true if messages of {@link Type#Detail} can be logged
 	 */
 	public static boolean canLogDetail() {
 		return Type.Detail.ordinal() >= LogConfig.CFG_MIN_LOG_TYPE.getEnum()
 				.ordinal();
 	}
 
 	/**
 	 * @return true if messages of {@link Type#Info} can be logged
 	 */
 	public static boolean canLogInfo() {
 		return Type.Info.ordinal() >= LogConfig.CFG_MIN_LOG_TYPE.getEnum()
 				.ordinal();
 	}
 
 	/**
 	 * @return true if messages of {@link Type#Warn} can be logged
 	 */
 	public static boolean canLogWarning() {
 		return Type.Warn.ordinal() >= LogConfig.CFG_MIN_LOG_TYPE.getEnum()
 				.ordinal();
 	}
 
 	/**
 	 * @return true if messages of {@link Type#Error} can be logged
 	 */
 	public static boolean canLogError() {
 		return Type.Error.ordinal() >= LogConfig.CFG_MIN_LOG_TYPE.getEnum()
 				.ordinal();
 	}
 
 }
