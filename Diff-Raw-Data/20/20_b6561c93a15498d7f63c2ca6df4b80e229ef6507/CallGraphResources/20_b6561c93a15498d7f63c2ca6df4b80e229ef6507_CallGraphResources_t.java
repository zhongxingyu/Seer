 package callgraphanalyzer;
 
 import java.text.SimpleDateFormat;
 import db.Resources;
 public class CallGraphResources extends Resources {
	public static final int ANALYZER_MAX_DEPTH = 1;
 	public static boolean isLiteral(String literal) {
 		if(literal.equals("String") || literal.equals("int") || literal.equals("long") ||
 				literal.equals("short") || literal.equals("byte") || literal.equals("double") ||
 				literal.equals("float") || literal.equals("boolean") || literal.equals("char") ||
 				literal.equals("null"))
 			return true;
 		else
 			return false;
 	}
 	
 	public static boolean isGeneric(String type) {
 		if(type.contains("<") && type.contains(">"))
 			return true;
 		else
 			return false;
 	}
 	
 	public static int findTypeDivider(String methodName) {
 		int index = -1;
 		for(int i = 0; i < methodName.length(); i++) {
 			if(methodName.charAt(i) == '.')
 				index = i;
 			if(methodName.charAt(i) == '(')
 				break;
 		}
 		return index;
 	}
 }
