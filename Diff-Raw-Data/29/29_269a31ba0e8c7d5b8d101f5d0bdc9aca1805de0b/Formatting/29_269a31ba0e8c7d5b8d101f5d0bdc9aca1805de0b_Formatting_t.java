 package se.mockachino.util;
 
 import se.mockachino.matchers.matcher.Matcher;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 public class Formatting {
 
 	public static String calls(int num) {
 		if (num == 1) {
 			return "1 call";
 		}
 		if (num == 0) {
 			return "no calls";
 		}
 		return num + " calls";
 	}
 
 	public static String list(Object[] args) {
 		return join(", ", args);
 	}
 
 	public static String list(Iterable<Matcher> iterable) {
 		return join(", ", iterable);
 	}
 
 	public static String argument(Object arg) {
 		arg = PrimitiveList.toList(arg);
 		if (arg == null) {
 			return "null";
 		}
 		if (arg instanceof String) {
 			return "\"" + arg + "\"";
 		}
 		if (arg instanceof List) {
 			return "[" + join(", ", (List) arg) + "]";
 		}
 		if (arg instanceof Set) {
 			return "{" + join(", ", (Set) arg) + "}";
 		}
 		if (arg instanceof Map) {
			return "(" + join(", ", (Set) ((Map) arg).entrySet()) + ")";
 		}
 		return arg.toString();
 	}
 
 	public static String join(String sep, Iterable iterable) {
 		if (iterable == null) {
 			return "";
 		}
 		StringBuilder builder = new StringBuilder();
 		boolean first = true;
 		for (Object object : iterable) {
 			if (first) {
 				first = false;
 			} else {
 				builder.append(sep);
 			}
 			builder.append(argument(object));
 		}
 		return builder.toString();
 	}
 
 	public static String join(String sep, Object... objects) {
 		if (objects == null) {
 			return "";
 		}
 		StringBuilder builder = new StringBuilder();
 		boolean first = true;
 		for (Object object : objects) {
 			if (first) {
 				first = false;
 			} else {
 				builder.append(sep);
 			}
 			builder.append(argument(object));
 		}
 		return builder.toString();
 	}
 	public static String toString(StackTraceElement[] stacktrace) {
 		return toString(stacktrace, Integer.MAX_VALUE);
 	}
 
 	public static String toString(StackTraceElement[] stacktrace, int maxLines) {
 		if (stacktrace == null || stacktrace.length == 0) {
 			return null;
 		}
 		StringBuilder builder = new StringBuilder();
 		for (StackTraceElement element : stacktrace) {
 			if (--maxLines < 0) {
 				break;
 			}
 			builder.append("\tat ").append(element.getClassName()).append(".").append(element.getMethodName());
 			builder.append("(").append(element.getFileName()).append(":").append(element.getLineNumber()).append(")");
 			builder.append("\n");
 		}
 		return builder.toString();
 	}
 
 	public static String quote(String s) {
 		String ret = Pattern.quote(s);
 		if (ret.equals("\\Q" + s + "\\E")) {
 			return s;
 		}
 		return ret;
 	}
 
 }
