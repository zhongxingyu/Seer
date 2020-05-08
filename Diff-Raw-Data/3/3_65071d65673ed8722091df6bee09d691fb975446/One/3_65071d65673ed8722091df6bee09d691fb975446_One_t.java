 package org.interfacegen.examples.circular;
 
 import org.interfacegen.GenInterface;
 import org.interfacegen.examples.circular.foo.ITwo;
 
 @GenInterface
 public class One implements IOne {
 	public ITwo getTwo() {
 		return null;
 	}
 
 	public IThree getThree() {
 		return null;
 	}
 }
