 package de.devboost.codecomposers.test;
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 import de.devboost.codecomposers.java.JavaComposite;
 
 /**
  * A set of test for the special 'interface mode' provided by
  * {@link JavaComposite} where two line breaks are automatically added after
  * method declarations.
  */
 public class InterfaceModeTest {
 
 	@Test
 	public void testInterfaceMode() {
 		JavaComposite jc = new JavaComposite();
 		jc.setInterfaceMode(true);
 		
 		jc.add("public interface ITest {");
 		jc.add("public void m1();");
 		jc.add("public void m2();");
 		jc.add("}");
 
 		assertTrue("Two breaks expected after m1().", jc.toString().contains("m1();\n\t\n\tpublic"));
 	}

	@Test
	public void testInterfaceModeWithPackage() {
		JavaComposite jc = new JavaComposite();
		jc.setInterfaceMode(true);
		
		jc.add("package com.acme;");
		jc.add("public interface ITest {");
		jc.add("public void m1();");
		jc.add("public void m2();");
		jc.add("}");

		assertTrue("One break expected after package declaraction.", jc.toString().contains("com.acme;\npublic"));
	}
 }
