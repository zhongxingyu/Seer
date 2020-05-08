 package edu.kit.pp.minijava.tokens;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Collections;
 
 public class Keyword extends Token {
 
     public static final Map<String, Boolean> _KEYWORDS = createMap();
 
     private static Map<String, Boolean> createMap() {
 
         Map<String, Boolean> aMap = new HashMap<String, Boolean>(); 
         aMap.put("abstract", true);
         aMap.put("assert", true);
         aMap.put("boolean", true);
         aMap.put("break", true);
         aMap.put("byte", true);
         aMap.put("case", true);
         aMap.put("catch", true);
         aMap.put("char", true);
         aMap.put("class", true);
         aMap.put("const", true);
         aMap.put("continue", true);
         aMap.put("default", true);
         aMap.put("double", true);
         aMap.put("do", true);
         aMap.put("else", true);
         aMap.put("enum", true);
         aMap.put("extends", true);
         aMap.put("false", true);
         aMap.put("finally", true);
         aMap.put("final", true);
         aMap.put("float", true);
         aMap.put("for", true);
         aMap.put("goto", true);
         aMap.put("if", true);
         aMap.put("implements", true);
         aMap.put("import", true);
         aMap.put("instanceof", true);
         aMap.put("interface", true);
         aMap.put("int", true);
         aMap.put("long", true);
         aMap.put("native", true);
         aMap.put("new", true);
         aMap.put("null", true);
         aMap.put("package", true);
         aMap.put("private", true);
         aMap.put("protected", true);
         aMap.put("public", true);
         aMap.put("return", true);
         aMap.put("short", true);
         aMap.put("static", true);
         aMap.put("strictfp", true);
         aMap.put("super", true);
         aMap.put("switch", true);
         aMap.put("synchronized", true);
         aMap.put("this", true);
         aMap.put("throws", true);
         aMap.put("throw", true);
         aMap.put("transient", true);
         aMap.put("true", true);
         aMap.put("try", true);
         aMap.put("void", true);
         aMap.put("volatile", true);
         aMap.put("while", true);
         return Collections.unmodifiableMap(aMap);
     }
 
     public Keyword(String value) {
 	super(value);
     }
 
     public String toString() {
	return "keyword " + _value;
     }
 }
