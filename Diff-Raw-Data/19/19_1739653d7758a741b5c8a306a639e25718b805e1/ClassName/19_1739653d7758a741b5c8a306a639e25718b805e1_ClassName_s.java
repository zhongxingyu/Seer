 package net.ayld.facade.model;
 
 import java.util.regex.Pattern;
 
 import net.ayld.facade.util.Tokenizer;
 
 /** 
  * Meant to represent a fully qualified class name.
  * 
  * The idea behind the class is to provide a model object that makes sure that a string is actually a class name and not 
  * something else, so one doesn't need to check 'by hand' every time.
  * */
 public final class ClassName {
 
 	/** 
 	 * This regex only checks plausibility it doesn't check validity. For a validity check a comparison against Java's reserved words must be made.
 	 * Also checks whether the name contains only Unicode chars.
 	 * 
 	 * More info here:
 	 *   http://docs.oracle.com/javase/specs/#3.8
 	 * 
 	 * */
 	private final static String CLASS_NAME_VALIDATION_REGEX = "([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*";
 	
 	private final String qualifiedClassName;
 
 	/** 
 	 * Creates a {@link ClassName}.
 	 * 
 	 * Doesn't allow nulls or empty strings.
 	 * Does everything it can to make sure that the argument is in fact a fully qualified class name and not something else.
 	 * 
 	 * @param qualifiedClassName a class name
 	 * 
 	 * @return a new {@link ClassName}
 	 * */
 	public ClassName(String qualifiedClassName) {
 		if (!isClassName(qualifiedClassName)) {
 			throw new IllegalArgumentException("invalid fully qualified class name: " + qualifiedClassName + ", expected: " + CLASS_NAME_VALIDATION_REGEX);
 		}
 		this.qualifiedClassName = qualifiedClassName;
 	}
 
 	private static boolean isClassName(String qualifiedClassName) {
		return Pattern.matches(CLASS_NAME_VALIDATION_REGEX, qualifiedClassName);
 	}
 
 	/** 
 	 * Returns the short name of the wrapped qualified class name.
 	 * 
 	 * In other words for this:
 	 *   
 	 *   <code>net.ayld.facade.model.ClassName</code>
 	 * 
 	 * this method will return:
 	 * 
 	 *   <code>ClassName</code>
 	 *   
 	 *   
 	 * @return the short name of the wrapped qualified class name
 	 * */
 	public String shortName() {
 		return Tokenizer.delimiter(".").tokenize(qualifiedClassName).lastToken();
 	}
 	
 	/** 
 	 * Returns the wrapped qualifiedClassName.
 	 * 
 	 * @return the wrapped qualifiedClassName
 	 * */
 	@Override
 	public String toString() {
 		return qualifiedClassName;
 	}
 
 	@Override
 	public int hashCode() {
 		return qualifiedClassName.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null) {
 			return false;
 		}
 		
 		if (!(obj instanceof ClassName)) {
 			return false;
 		}
 		
 		final ClassName other = (ClassName) obj;
 		
 		return other.toString().equals(this.toString());
 	}
 }	
