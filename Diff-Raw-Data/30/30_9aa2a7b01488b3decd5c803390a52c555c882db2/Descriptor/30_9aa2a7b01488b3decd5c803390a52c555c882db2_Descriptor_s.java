 package ca.uwaterloo.joos.ast.descriptor;
 
 /**
  * @author wenzhuman
  * 
  */
 public class Descriptor {
 	private final Class<?> elementClass;
 
 	public Descriptor(Class<?> elementClass) {
 		this.elementClass = elementClass;
 	}
 
 	public Class<?> getElementClass() {
 		return elementClass;
 	}
 }
