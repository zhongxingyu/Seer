 package org.nextflection.impl;
 
 import static junit.framework.Assert.assertEquals;
 
 import org.junit.Test;
 import org.nextflection.ClassType;
 import org.nextflection.Method;
 import org.nextflection.Methods;
 
 public class DefaultMethodTest {
 
 	private DefaultReflector reflector = new DefaultReflector();
 
 	@Test
 	public void testDeclarationString(){
 		ClassType obj = (ClassType)reflector.reflect(Object.class);
 		Methods meths = obj.methods();
 
 		// primitive return type
 		Method m = meths.withName("hashCode").get(0);
 		assertEquals("public int hashCode()", m.declarationString());
 
 		// void return type
 		m = meths.withName("notify").get(0);
 		assertEquals("public final void notify()", m.declarationString());
 
 		// Wildcard return type
 		m = meths.withName("getClass").get(0);
 		assertEquals("public final java.lang.Class<?> getClass()", m.declarationString());

		// class return type
		m = meths.withName("toString").get(0);
		assertEquals("public java.lang.String toString()", m.declarationString());

		// with parameters
		m = meths.withName("equals").get(0);
		assertEquals("public boolean equals(java.lang.Object)", m.declarationString());
 	}
 
 }
