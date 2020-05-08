 package me.format.shellformat;
 
import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * Published in terms of BSD or Apache2 license. <br/>
  *
  * ShellFormat supports variable replace. Variables are in form ${beer} or $milk.
  * Multiline formatting is preserved. Variable could expand into string with variables.
  *
  *
  */
 public class ShellDictionary {
 
     private final Map<String, Object> dictionary = new HashMap<String, Object>();
 
     private ShellDictionary() {}
     
     /*package*/ static ShellDictionary dictionary(String ... arguments) {
 	ShellDictionary dict = new ShellDictionary();
 	dict.appendAll(arguments);
 	return dict;
     }
 
     /**
      * Converts ShellDictionary into a map
      * 
      * @return an immutable map
      */
     public Map<String, Object> toMap() {
 	Map<String, Object> dictionary = new HashMap<String, Object>();
 	dictionary.putAll(this.dictionary);
        return Collections.unmodifiableMap(dictionary);
     }
     
     
     /**
      * Compiles a new ShellFormat with the actual dictionary
      * 
      * @return a new ShellFormat instance
      */
     public ShellFormat compile() {
 	return new ShellFormat(toMap());
     }
 
     /**
      * @param pattern a pattern to format
      * @return the formatted string
      */
     public String format(String pattern) {
 	return new ShellFormat(toMap()).format(pattern);
     }
     
     /**
      * @param lines the pattern lines to format
      * @return the formatted string
      */
     public String format(String ... lines) {
 	return new ShellFormat(toMap()).format(lines);
     }
     
     /**
      * @param strings the pairs of (key, value)
      * @return ShellDictinary builder (this)
      */
     public ShellDictionary appendAll(String...strings) {
     	
 	if (strings.length % 2 != 0) {
 	    throw new IllegalArgumentException("Incorrect number of parameters: " + strings.length);
 	}
 
 	for (int i = 0; i < strings.length; i += 2) {
 	    dictionary.put(strings[i], strings[i + 1]);
 	}
 	
 	return this;
     }
 
     /**
      * @param alias a variable to replace
      * @param value a replacement
      * @return ShellDictinary builder (this)
      */
     public ShellDictionary append(String alias, Object value) {
         this.dictionary.put(alias, value);
         return this;
     }
 }
