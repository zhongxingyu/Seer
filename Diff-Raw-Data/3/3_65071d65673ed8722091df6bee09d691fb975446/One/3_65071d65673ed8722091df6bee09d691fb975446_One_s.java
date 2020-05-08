 package org.interfacegen.examples.circular;
 
 import org.interfacegen.GenInterface;
 import org.interfacegen.examples.circular.foo.ITwo;
 
 @GenInterface
 public class One implements IOne {
	@Override
 	public ITwo getTwo() {
 		return null;
 	}
 
	@Override
 	public IThree getThree() {
 		return null;
 	}
 }
