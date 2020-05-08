 package org.facile;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 
 import static org.facile.Facile.*;
 
 public class Types {
 	static Class<Types> types = Types.class;
 
 	private static final Logger log = log(types);
 
 	public static int toInt(Object obj) {
 		try {
 			if (obj instanceof Number) {
 				return ((Number) obj).intValue();
 			} else if (obj instanceof CharSequence) {
 				try {
 					return Integer.parseInt(((CharSequence) obj).toString());
 				} catch (Exception ex) {
 					char[] chars = chars(obj);
 					Appendable buf = buf(chars.length);
 					boolean found = false;
 					for (char c : chars) {
 						if (Character.isDigit(c) && !found) {
 							found = true;
 							add(buf, c);
 						} else if (Character.isDigit(c) && found) {
 							add(buf, c);
 						} else if (!Character.isDigit(c) && found) {
 						}
 					}
 					try {
 						if (len(buf) > 0) {
 							return Integer.parseInt(str(buf));
 						}
 					} catch (Exception ex2) {
 						// noop
 					}
 					warning(log, "unable to convert to int");
 					return obj.hashCode();
 				}
 			} else {
 				String str = obj.toString();
 				return toInt(str);
 			}
 		} catch (Exception ex) {
 			warning(log,
 					"unable to convert to int and there was an exception %s",
 					ex.getMessage());
 			return obj.hashCode();
 		}
 	}
 
 	public static boolean toBoolean(Object obj) {
 		
 		Set<String> trueSet = set("t", "true", "True", "y", "yes", "1", "aye", "ofcourse", "T", "TRUE", "ok");
 		if (obj instanceof String || obj instanceof CharSequence || obj.getClass() == char[].class) {
 			String str = str(obj);
 			if (str.length()==0) {
 				return false;
 			} else {
 				return isIn(str, trueSet);
 			}
 		} else if (obj instanceof Boolean) {
 			return ((Boolean)obj).booleanValue();
 		} else if (isArray(obj) || obj instanceof Collection) {
 			return len(obj) > 0;
 		} else {
 			return toBoolean(str(obj));
 		}
 	}
 	public static double toDouble(Object obj) {
 		try {
 			if (obj instanceof Double) {
 				return (Double) obj;
 			} else if (obj instanceof Number) {
 				return ((Number) obj).doubleValue();
 			} else if (obj instanceof CharSequence) {
 				try {
 					return Double.parseDouble(((CharSequence) obj).toString());
 				} catch (Exception ex) {
 					String svalue = str(obj);
 					Matcher re = Regex.re(
 							"[-+]?[0-9]+\\.?[0-9]+([eE][-+]?[0-9]+)?", svalue);
 					if (re.find()) {
 						svalue = re.group(0);
 						return Double.parseDouble(svalue);
 					}
 					warning(log, "unable to convert to double after regex");
 					return Double.NaN;
 				}
 			} else {
 				String str = obj.toString();
 				return toInt(str);
 			}
 		} catch (Exception ex) {
 			warning(log,
 					"unable to convert to int and there was an exception %s",
 					ex.getMessage());
 			return Double.NaN;
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T> T coerce(Class<T> clz, Object value) {
 		if (clz == integer || clz == pint) {
 			Integer i = toInt(value);
 			return (T) i;
 		} else if (clz == dbl || clz == pdouble) {
 			Double i = toDouble(value);
 			return (T) i;
 		} else if (clz == sarray) {
 			return (T) toStringArray(value);
 		}  else if (clz == bool || clz == pboolean) {
 			Boolean b = toBoolean(value);
 			return (T) b;
 		} else if (clz == fileT) {
 			return (T) toFile(value);
 		}
 		else {// TODO toFloat, toList, toArray
 	
 			return (T) value;
 		}
 	}
 
 	public static File toFile(Object value) {
 		if (value instanceof File) {
 			return (File)value;
 		} else if (value instanceof CharSequence){
 			return file(str(value));
 		} else {
 			return toFile(value.toString());
 		}
 	}
 
 	public static String[] toStringArray(Object value) {
 		if (value instanceof CharSequence) {
 			String str = toString(value);
 
 			List<Character> delims = ls(',', '\t', ' ', '|', ':', ';');
 			char[] chars = chars(str);
 			for (char c : delims) {
 				if (isIn(c, chars)) {
 					return split(chars, c);
 				}
 			}
 
 		} else if (value instanceof List) {
 			// complete this
 		}
 
 		return null;
 	}
 
 	public static String toString(Object obj) {
		if (obj == null) {
			return "";
		}
 		if (obj instanceof String) {
 			return (String) obj;
 		} else {
 			return obj.toString();
 		}
 	}
 
 }
