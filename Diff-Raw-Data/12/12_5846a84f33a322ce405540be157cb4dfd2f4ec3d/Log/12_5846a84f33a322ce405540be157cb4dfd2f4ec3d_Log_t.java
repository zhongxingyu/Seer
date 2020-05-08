 package pleocmd;
 
 import java.awt.Color;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import pleocmd.cfg.ConfigEnum;
 import pleocmd.cfg.ConfigString;
 import pleocmd.cfg.Configuration;
 import pleocmd.cfg.ConfigurationInterface;
 import pleocmd.cfg.Group;
 import pleocmd.exc.ConfigurationException;
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
 		Console
 	}
 
 	private static final ConfigString CFG_TIMEFORMAT = new ConfigString(
 			"Time Format", "HH:mm:ss.SSS");
 
 	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(
 			CFG_TIMEFORMAT.getContent());
 
 	private static ConfigEnum<Type> cfgMinLogType = new ConfigEnum<Type>(
 			"Minimal Log-Type", Type.Detail);
 
 	private static boolean minLogTypeKnown;
 
 	private static List<Log> queuedLogs = new ArrayList<Log>(128);
 
 	/**
 	 * Needed to inline next line after a line-break when writing messages to a
 	 * plain {@link String}. Number of spaces must fit to format specificier in
 	 * {@link #toString()} and the length of the {@link #DATE_FORMATTER}.
 	 */
 	private static final String SPACES = String.format("%68s", "");
 
 	static {
 		// must be *after* declaration of all static fields !!!
 		new LogConfig();
 	}
 
 	private final Type type;
 
 	private final StackTraceElement caller;
 
 	private final String msg;
 
 	private final Throwable backtrace;
 
 	private final long time;
 
 	private Log(final Type type, final StackTraceElement caller,
 			final String msg, final Throwable backtrace) {
 		this.type = type;
 		this.caller = caller;
 		this.msg = msg;
 		this.backtrace = backtrace;
 		time = System.currentTimeMillis();
 		switch (type) {
 		case Error:
 			if (MainFrame.hasGUI()) {
 				MainFrame.the().getMainLogPanel().addLog(this);
 				ErrorDialog.show(this);
 			} else
 				System.err.println(toString()); // CS_IGNORE
 			break;
 		case Console:
 			System.out.println(msg); // CS_IGNORE
 			if (MainFrame.hasGUI())
 				MainFrame.the().getMainLogPanel().addLog(this);
 			break;
 		default:
 			if (!minLogTypeKnown) {
 				// we have to queue it for later output, because
 				// we currently don't now, if we really have to output this log
 				queuedLogs.add(this);
 				break;
 			}
 			if (MainFrame.hasGUI())
 				MainFrame.the().getMainLogPanel().addLog(this);
 			else
 				System.err.println(toString()); // CS_IGNORE
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
 		return String.format("%s.%s()", caller.getClassName().replaceFirst(
 				"^.*\\.([^.]*)$", "$1"), caller.getMethodName());
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
 			return Color.ORANGE;
 		case Error:
 			return Color.RED;
 		case Console:
 		default:
 			return Color.BLACK;
 		}
 	}
 
 	/**
 	 * @return the time (with milliseconds - no date), from {@link #getTime()}
 	 *         formatted as a {@link String}
 	 */
 	public String getFormattedTime() {
 		return DATE_FORMATTER.format(new Date(time));
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
 		case Console:
 			return ">  ";
 		default:
 			return "!?!";
 		}
 	}
 
 	@Override
 	public String toString() {
 		return String.format("%s %s %-50s %s", getFormattedTime(),
 				getTypeShortString(), caller, msg.replace("\n", "\n" + SPACES));
 	}
 
 	private static StackTraceElement getCallerSTE(final int stepsBack) {
 		final StackTraceElement[] st = new Throwable().getStackTrace();
 		return st.length < stepsBack ? st[st.length - 1] : st[stepsBack];
 	}
 
 	/**
 	 * Prints messages to the GUI's log, if any, or to the standard error
 	 * otherwise if their {@link #type} is not "lower" than the
 	 * {@link #cfgMinLogType}.<br>
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
 	 * @param caller
 	 *            the name of the creator of this message
 	 * @param args
 	 *            arbitrary number of arguments for the format string (may also
 	 *            be zero)
 	 */
 	private static void msg(final Type type, final Throwable throwable,
 			final StackTraceElement caller, final String msg,
 			final Object... args) {
 		if (type.ordinal() >= cfgMinLogType.getEnum().ordinal()) {
 			final String msgStr = args.length == 0 ? msg : String.format(msg,
 					args);
 			new Log(type, caller, msgStr, throwable);
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
 		msg(Type.Detail, null, getCallerSTE(2), msg, args);
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
 		msg(Type.Info, null, getCallerSTE(2), msg, args);
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
 		msg(Type.Warn, null, getCallerSTE(2), msg, args);
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
 		msg(Type.Error, null, getCallerSTE(2), msg, args);
 	}
 
 	/**
 	 * Creates a new message of {@link Type#Console} which will be printed to
 	 * standard output <b>and</b> send to the GUI.
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
 		msg(Type.Console, null, getCallerSTE(2), msg, args);
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
 
 		msg(Type.Error, throwable, t.getStackTrace()[0], sb.toString());
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
 	 * <td>{@link Type#Console}</td>
 	 * <td>no messages will be processed</td>
 	 * </tr>
 	 * </table>
 	 * <p>
 	 * Note that messages of {@link Type#Console} are treated as wrapped
 	 * standard output and not as normal messages and are therefore always be
 	 * processed even if minLogType is set to {@link Type#Console}.
 	 * 
 	 * @param minLogType
 	 *            true if {@link Type#Detail} will be processed
 	 */
 	public static void setMinLogType(final Type minLogType) {
 		if (minLogType.ordinal() < Type.Detail.ordinal()
 				|| minLogType.ordinal() > Type.Console.ordinal())
 			throw new IllegalArgumentException("Invalid value for minLogType");
 		cfgMinLogType.setEnum(minLogType);
 		minLogTypeKnown = true;
		if (MainFrame.hasGUI()) {
 			for (final Log log : queuedLogs)
				if (Log.canLog(log.getType()))
					MainFrame.the().getMainLogPanel().addLog(log);
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
 		return cfgMinLogType.getEnum();
 	}
 
 	/**
 	 * @param type
 	 *            one of the log {@link Type}s
 	 * @return true if messages of the given {@link Type} can be logged
 	 */
 	public static boolean canLog(final Type type) {
 		return type.ordinal() >= cfgMinLogType.getEnum().ordinal();
 	}
 
 	/**
 	 * @return true if messages of {@link Type#Detail} can be logged
 	 */
 	public static boolean canLogDetail() {
 		return Type.Detail.ordinal() >= cfgMinLogType.getEnum().ordinal();
 	}
 
 	/**
 	 * @return true if messages of {@link Type#Info} can be logged
 	 */
 	public static boolean canLogInfo() {
 		return Type.Info.ordinal() >= cfgMinLogType.getEnum().ordinal();
 	}
 
 	/**
 	 * @return true if messages of {@link Type#Warn} can be logged
 	 */
 	public static boolean canLogWarning() {
 		return Type.Warn.ordinal() >= cfgMinLogType.getEnum().ordinal();
 	}
 
 	/**
 	 * @return true if messages of {@link Type#Error} can be logged
 	 */
 	public static boolean canLogError() {
 		return Type.Error.ordinal() >= cfgMinLogType.getEnum().ordinal();
 	}
 
 	static class LogConfig implements ConfigurationInterface {
 
 		LogConfig() {
 			try {
 				Configuration.getMain().registerConfigurableObject(this,
 						getClass().getSimpleName());
 			} catch (final ConfigurationException e) {
 				Log.error(e);
 			}
 		}
 
 		@Override
 		@SuppressWarnings("synthetic-access")
 		public Group getSkeleton(final String groupName) {
 			return new Group(groupName).add(CFG_TIMEFORMAT).add(cfgMinLogType);
 		}
 
 		@Override
 		public void configurationAboutToBeChanged() {
 			// nothing to do
 		}
 
 		@Override
 		@SuppressWarnings("synthetic-access")
 		public void configurationChanged(final Group group) {
 			DATE_FORMATTER.applyPattern(CFG_TIMEFORMAT.getContent());
 			if (MainFrame.hasGUI())
 				MainFrame.the().getMainLogPanel().updateState();
 			setMinLogType(getMinLogType());
 		}
 
 		@Override
 		public List<Group> configurationWriteback() {
 			return Configuration
 					.asList(getSkeleton(getClass().getSimpleName()));
 		}
 
 	}
 
 }
