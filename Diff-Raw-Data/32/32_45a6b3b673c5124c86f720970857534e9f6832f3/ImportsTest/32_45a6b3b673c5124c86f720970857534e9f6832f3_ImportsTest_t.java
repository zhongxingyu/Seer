 /*******************************************************************************
  * Copyright (c) 2006-2013
  * Software Technology Group, Dresden University of Technology
  * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany;
  *   DevBoost GmbH - Berlin, Germany
  *      - initial API and implementation
  ******************************************************************************/
 package de.devboost.codecomposers.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
import java.lang.reflect.Field;

 import org.junit.Test;
 
 import de.devboost.codecomposers.java.JavaComposite;
 
 public class ImportsTest {
 
 	@Test
 	public void testSimpleImport() {
 		JavaComposite jc = new JavaComposite();
 		jc.add("package com.pany;");
 		jc.addImportsPlaceholder();
 		jc.add("public class MyClass {");
 		jc.add(jc.getClassName("com.pany.OtherClass") +  " myField;");
 		jc.add("}");
 		
 		String result = getCleanResult(jc);
 		assertEquals(result, 
 			"package com.pany;" +
 			"import com.pany.OtherClass;" +
 			"public class MyClass {" +
 			"OtherClass myField;" +
 			"}"
 		);
 	}
 
 	@Test
 	public void testImportWithNameConflict() {
 		JavaComposite jc = new JavaComposite();
 		jc.add("package com.pany;");
 		jc.addImportsPlaceholder();
 		jc.add("public class MyClass {");
 		jc.add(jc.getClassName("com.pany.OtherClass") + " myField1;");
 		jc.add(jc.getClassName("third.party.OtherClass") + " myField2;");
 		jc.add("}");
 		
 		String result = getCleanResult(jc);
 		assertEquals(result, 
 			"package com.pany;" +
 			"import com.pany.OtherClass;" +
 			"public class MyClass {" +
 			"OtherClass myField1;" +
 			"third.party.OtherClass myField2;" +
 			"}"
 		);
 	}
 
 	@Test
 	public void testImplicitImport() {
 		JavaComposite jc = new JavaComposite();
 		jc.add("package com.pany;");
 		jc.addImportsPlaceholder();
 		jc.add("public class MyClass {");
 		jc.addImplicitImport("com.pany.MyClass");
 		jc.add(jc.getClassName("third.party.MyClass") + " myField1;");
 		jc.add("}");
 		
 		String result = getCleanResult(jc);
 		assertEquals(result, 
 			"package com.pany;" +
 			"public class MyClass {" +
 			"third.party.MyClass myField1;" +
 			"}"
 		);
 	}
 
 	@Test
 	public void testImplicitImport2() {
 		JavaComposite jc = new JavaComposite();
 		jc.add("package com.pany;");
 		jc.addImportsPlaceholder();
 		jc.add("public class MyClass {");
 		jc.addImplicitImport("com.pany.MyClassInSamePackage");
 		jc.add(jc.getClassName("com.pany.MyClassInSamePackage") + " myField;");
 		jc.add("}");
 		
 		String result = getCleanResult(jc);
 		assertEquals(result, 
 			"package com.pany;" +
 			"public class MyClass {" +
 			"MyClassInSamePackage myField;" +
 			"}"
 		);
 	}
 
 	@Test
 	public void testImplicitImportFromJavaLangPackage1() {
 		JavaComposite jc = new JavaComposite();
 		jc.add("package com.pany;");
 		jc.addImportsPlaceholder();
 		jc.add("public class MyClass {");
 		// We do not expect an import for java.lang.Throwable as is it imported
 		// by default
 		jc.add(jc.getClassName("java.lang.Throwable") + " myField1;");
 		jc.add(jc.getClassName("third.party.Throwable") + " myField2;");
 		jc.add("}");
 		
 		String result = getCleanResult(jc);
 		assertEquals(result, 
 			"package com.pany;" +
 			"public class MyClass {" +
 			"Throwable myField1;" +
 			"third.party.Throwable myField2;" +
 			"}"
 		);
 	}
 
 	@Test
 	public void testImplicitImportFromJavaLangPackage2() {
 		JavaComposite jc = new JavaComposite();
 		jc.add("package com.pany;");
 		jc.addImportsPlaceholder();
 		jc.add("public class MyClass {");
 		// We do expect qualified references to java.lang.Throwable there is an
 		// import of another class with the same simple name
 		jc.add(jc.getClassName("third.party.Throwable") + " myField1;");
 		jc.add(jc.getClassName("java.lang.Throwable") + " myField2;");
 		jc.add("}");
 		
 		String result = getCleanResult(jc);
 		assertEquals(result, 
 			"package com.pany;" +
 			"import third.party.Throwable;" +
 			"public class MyClass {" +
 			"Throwable myField1;" +
 			"java.lang.Throwable myField2;" +
 			"}"
 		);
 	}
 
 	@Test
	public void testImportFromSubPackageOfJavaLang() {
		JavaComposite jc = new JavaComposite();
		jc.add("package com.pany;");
		jc.addImportsPlaceholder();
		jc.add("public class MyClass {");
		// We do expect qualified references to java.lang.Throwable there is an
		// import of another class with the same simple name
		jc.add(jc.getClassName(Field.class) + " myField;");
		jc.add("}");
		
		String result = getCleanResult(jc);
		assertEquals(result, 
			"package com.pany;" +
			"import java.lang.reflect.Field;" +
			"public class MyClass {" +
			"Field myField;" +
			"}"
		);
	}

	@Test
 	public void testMultipleImports() {
 		JavaComposite jc = new JavaComposite();
 		jc.add("package com.pany;");
 		jc.addImportsPlaceholder();
 		jc.add("public class MyClass {");
 		jc.addImplicitImport("MyClass");
 		jc.add(jc.getClassName("third.party.OtherClass1") + " myField1;");
 		jc.add(jc.getClassName("third.party.OtherClass2") + " myField2;");
 		jc.add("}");
 		
 		String result = getCleanResult(jc, false);
 		print(result);
 		
 		String import1 = "import third.party.OtherClass1;";
 		String import2 = "import third.party.OtherClass2;";
 		assertTrue("Line break between imports expected.", result.contains(import1 + "\n" + import2));
 	}
 
 	@Test
 	public void testIgnorePrimitiveType() {
 		JavaComposite jc = new JavaComposite();
 		jc.add("package com.pany;");
 		jc.addImportsPlaceholder();
 		jc.add("public class MyClass {");
 		jc.add(jc.getClassName("int") +  " myField1;");
 		jc.add(jc.getClassName("byte") +  " myField2;");
 		jc.add(jc.getClassName("char") +  " myField3;");
 		jc.add(jc.getClassName("boolean") +  " myField4;");
 		jc.add(jc.getClassName("long") +  " myField5;");
 		jc.add(jc.getClassName("double") +  " myField6;");
 		jc.add(jc.getClassName("float") +  " myField7;");
 		// Add some primitive arrays
 		jc.add(jc.getClassName("int[]") +  " myField8;");
 		jc.add("}");
 		
 		String result = getCleanResult(jc);
 		assertEquals(result, 
 			"package com.pany;" +
 			"public class MyClass {" +
 			"int myField1;" +
 			"byte myField2;" +
 			"char myField3;" +
 			"boolean myField4;" +
 			"long myField5;" +
 			"double myField6;" +
 			"float myField7;" +
 			"int[] myField8;" +
 			"}"
 		);
 	}
 
 	@Test
 	public void testGenericImports() {
 		JavaComposite jc = new JavaComposite();
 		jc.add("package com.pany;");
 		jc.addImportsPlaceholder();
 		jc.add("public class MyClass {");
 		jc.add(jc.getClassName("java.util.List<third.party.OtherClass>") + " myField;");
 		jc.add("}");
 		
 		String result = getCleanResult(jc, false);
 		print(result);
 		
 		String import1 = "import java.util.List;";
 		String import2 = "import third.party.OtherClass;";
 		assertTrue("Import of List expected.", result.contains(import1));
 		assertTrue("Import of OtherClass expected.", result.contains(import2));
 		
 		String fieldDeclaration = "List<OtherClass> myField;";
 		assertTrue("Wrong field declaration.", result.contains(fieldDeclaration));
 		
 		assertTrue("Line break between imports expected.", result.contains(import1 + "\n" + import2));
 	}
 
 	private String getCleanResult(JavaComposite jc) {
 		return getCleanResult(jc, true);
 	}
 
 	private String getCleanResult(JavaComposite jc, boolean removeBreaks) {
 		String result = jc.toString();
 		if (removeBreaks) {
 			result = result.replace("\n", "");
 		}
 		result = result.replace("\t", "");
 		print(result);
 		return result;
 	}
 
 	private void print(String result) {
 		System.out.println(">>>" + result + "<<<");
 	}
 }
