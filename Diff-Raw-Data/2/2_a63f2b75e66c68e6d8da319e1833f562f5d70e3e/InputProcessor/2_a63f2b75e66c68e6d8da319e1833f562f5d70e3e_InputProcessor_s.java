 package chameleon.input;
 
 import chameleon.core.compilationunit.CompilationUnit;
 import chameleon.core.element.Element;
 import chameleon.tool.Processor;
 
 /**
  * An interface for processors used while reading a model from source files.
  * 
  * @author Marko van Dooren
  */
 public interface InputProcessor extends Processor {
 
 	/**
 	 * Set the locations of the given element. The location is a range marked by the offset and the length of the element.
 	 */
   public void setLocation(Element element, int offset, int length, CompilationUnit compilationUnit);
 
   public void setLocation(Element element, int offset, int length, CompilationUnit compilationUnit, String tagType);
 
   /**
 	 * Report a parse error.
 	 * 
 	 * @param exc
 	 */
 	public void reportParseError(ParseException exc);
 
 }
