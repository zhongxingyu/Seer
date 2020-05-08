 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.core.tests.typeinference;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import junit.framework.TestCase;
 
 import org.eclipse.dltk.compiler.env.ModuleSource;
 import org.eclipse.dltk.compiler.problem.ProblemCollector;
 import org.eclipse.dltk.core.tests.util.StringList;
 import org.eclipse.dltk.internal.javascript.ti.IReferenceAttributes;
 import org.eclipse.dltk.internal.javascript.ti.TypeInferencer2;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.parser.JavaScriptParser;
 import org.eclipse.dltk.javascript.typeinference.IValueCollection;
 import org.eclipse.dltk.javascript.typeinference.IValueReference;
 import org.eclipse.dltk.javascript.typeinfo.ITypeNames;
 import org.eclipse.dltk.javascript.typeinfo.model.Member;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelLoader;
 
 @SuppressWarnings("nls")
 public class TypeInferenceTests extends TestCase implements ITypeNames {
 
 	private static Script parse(String code) {
 		final JavaScriptParser parser = new JavaScriptParser();
 		parser.setTypeInformationEnabled(true);
 		final ProblemCollector reporter = new ProblemCollector();
 		final Script script = parser.parse(new ModuleSource(code), reporter);
 		if (reporter.hasErrors()) {
 			fail(reporter.getErrors().toString());
 		}
 		return script;
 	}
 
 	private static Set<Type> getTypes(String... names) {
 		final Set<Type> types = new HashSet<Type>();
 		for (String name : names) {
 			final Type type = TypeInfoModelLoader.getInstance().getType(name);
 			assertNotNull(type);
 			types.add(type);
 		}
 		return types;
 	}
 
 	private static IValueCollection inference(final String code) {
 		TypeInferencer2 inferencer = new TestTypeInferencer2();
		return inferencer.doInferencing(parse(code));
 	}
 
 	public void testNewNamedFunction() throws Exception {
 		List<String> lines = new StringList();
 		lines.add("var test = new function Test() {");
 		lines.add("this.p = 10;");
 		lines.add("}");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference a = collection.getChild("test");
 		assertEquals(1, a.getTypes().size());
 		assertEquals(ITypeNames.OBJECT, a.getTypes().iterator().next().getName());
 		assertEquals(1, a.getDirectChildren().size());
 		assertEquals("p", a.getDirectChildren().iterator().next());
 	}
 
 	public void testNewFunction() throws Exception {
 		List<String> lines = new StringList();
 		lines.add("var test = new function() {");
 		lines.add("this.p = 10;");
 		lines.add("}");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference a = collection.getChild("test");
 		assertEquals(1, a.getTypes().size());
 		assertEquals(ITypeNames.OBJECT, a.getTypes().iterator().next().getName());
 		assertEquals(1, a.getDirectChildren().size());
 		assertEquals("p", a.getDirectChildren().iterator().next());
 	}
 
 	public void testNewType() throws Exception {
 		List<String> lines = new StringList();
 		lines.add("function Test() {");
 		lines.add("this.p = 10;");
 		lines.add("}");
 		lines.add("var test = new Test();");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference a = collection.getChild("test");
 		assertEquals(1, a.getTypes().size());
 		assertEquals(ITypeNames.OBJECT, a.getTypes().iterator().next().getName());
 		assertEquals(1, a.getDirectChildren().size());
 		assertEquals("p", a.getDirectChildren().iterator().next());
 	}
 
 	public void testNestedFunctionType() throws Exception {
 		List<String> lines = new StringList();
 		lines.add("function Test() {");
 		lines.add("this.newNode = function newNode():Node {");
 		lines.add("return new Node();");
 		lines.add("}");
 		lines.add("function Node(){");
 		lines.add("this.a = 10;");
 		lines.add("this.toString = function toString():String {");
 		lines.add("return 'Node';");
 		lines.add("}");
 		lines.add("}");
 		lines.add("}");
 		lines.add("var test = new Test();");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference test = collection.getChild("test");
 		assertEquals(true, test.exists());
 		assertEquals(1, test.getDirectChildren().size());
 		assertEquals(1, test.getTypes().size());
 		assertEquals(ITypeNames.OBJECT, test.getTypes().iterator().next().getName());
 
 		assertEquals("newNode", test.getDirectChildren().iterator().next());
 
 		test = test.getChild("newNode");
 		assertEquals(true, test.exists());
 		assertEquals(1, test.getDeclaredTypes().size());
 		assertEquals("Function", typename(test.getDeclaredTypes()));
 
 		test = test.getChild(IValueReference.FUNCTION_OP);
 		assertEquals(2, test.getDirectChildren().size());
 		assertNull(test.getDeclaredType());
 
 		IValueReference a = test.getChild("a");
 		assertEquals(true, a.exists());
 		assertEquals(1, a.getTypes().size());
 		assertEquals("Number", typename(a.getTypes()));
 
 		IValueReference toString = test.getChild("toString");
 		assertEquals(true, toString.exists());
 		assertEquals(1, toString.getDeclaredTypes().size());
 		assertEquals(FUNCTION, typename(toString.getDeclaredTypes()));
 
 		toString = toString.getChild(IValueReference.FUNCTION_OP);
 		assertEquals(true, toString.exists());
 		assertEquals(1, toString.getTypes().size());
 		assertEquals("String", typename(toString.getTypes()));
 
 	}
 
 	protected String typename(Set<Type> types) {
 		assertEquals(1, types.size());
 		return types.iterator().next().getName();
 	}
 
 	public void testNestedFunctionTypeWithoutDeclaration() throws Exception {
 		List<String> lines = new StringList();
 		lines.add("function Test() {");
 		lines.add("this.newNode = function newNode() {");
 		lines.add("return new Node();");
 		lines.add("}");
 		lines.add("function Node(){");
 		lines.add("this.a = 10;");
 		lines.add("this.toString = function toString() {");
 		lines.add("return 'Node';");
 		lines.add("}");
 		lines.add("}");
 		lines.add("}");
 		lines.add("var test = new Test();");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference test = collection.getChild("test");
 		assertEquals(true, test.exists());
 		assertEquals(1, test.getDirectChildren().size());
 		assertEquals(1, test.getTypes().size());
 		assertEquals(ITypeNames.OBJECT, test.getTypes().iterator().next().getName());
 
 		assertEquals("newNode", test.getDirectChildren().iterator().next());
 
 		test = test.getChild("newNode");
 		assertEquals(true, test.exists());
 		assertEquals(1, test.getDeclaredTypes().size());
 		assertEquals("Function", typename(test.getDeclaredTypes()));
 
 		test = test.getChild(IValueReference.FUNCTION_OP);
 		assertEquals(2, test.getDirectChildren().size());
 		assertNull(test.getDeclaredType());
 
 		IValueReference a = test.getChild("a");
 		assertEquals(true, a.exists());
 		assertEquals(1, a.getTypes().size());
 		assertEquals("Number", a.getTypes().iterator().next().getName());
 
 		IValueReference toString = test.getChild("toString");
 		assertEquals(true, toString.exists());
 		assertEquals(1, toString.getDeclaredTypes().size());
 		assertEquals("Function", typename(toString.getDeclaredTypes()));
 
 		toString = toString.getChild(IValueReference.FUNCTION_OP);
 		assertEquals(true, toString.exists());
 		assertEquals(1, toString.getTypes().size());
 		assertEquals("String", typename(toString.getTypes()));
 	}
 
 	public void testNestedFunctionTypeWithScopeReturn() throws Exception {
 		List<String> lines = new StringList();
 		lines.add("function Test() {");
 		lines.add("this.newScope = function() {");
 		lines.add("return  {");
 		lines.add("x:10,");
 		lines.add("y:'10',");
 		lines.add("z:function(){return 'test';}");
 		lines.add("}");
 		lines.add("}");
 		lines.add("}");
 		lines.add("var test = new Test();");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference test = collection.getChild("test");
 		assertEquals(true, test.exists());
 		assertEquals(1, test.getDirectChildren().size());
 		assertEquals(1, test.getTypes().size());
 		assertEquals(ITypeNames.OBJECT, test.getTypes().iterator().next().getName());
 
 		assertEquals("newScope", test.getDirectChildren().iterator().next());
 
 		test = test.getChild("newScope");
 		assertEquals(true, test.exists());
 		assertEquals(1, test.getTypes().size());
 		assertEquals("Function", test.getTypes().iterator().next().getName());
 
 		test = test.getChild(IValueReference.FUNCTION_OP);
 		assertEquals(3, test.getDirectChildren().size());
 		assertNull(test.getDeclaredType());
 
 		IValueReference x = test.getChild("x");
 		assertEquals(true, x.exists());
 		assertEquals(0, x.getDeclaredTypes().size());
 		assertEquals(1, x.getTypes().size());
 		assertEquals("Number", x.getTypes().iterator().next().getName());
 
 		IValueReference y = test.getChild("y");
 		assertEquals(true, y.exists());
 		assertEquals(0, y.getDeclaredTypes().size());
 		assertEquals(1, y.getTypes().size());
 		assertEquals("String", y.getTypes().iterator().next().getName());
 
 		IValueReference toString = test.getChild("z");
 		assertEquals(true, toString.exists());
 		assertEquals(0, toString.getDeclaredTypes().size());
 		assertEquals(1, toString.getTypes().size());
 		assertEquals("Function", typename(toString.getTypes()));
 
 		toString = toString.getChild(IValueReference.FUNCTION_OP);
 		assertEquals(true, toString.exists());
 		assertEquals(1, toString.getTypes().size());
 		assertEquals("String", toString.getTypes().iterator().next().getName());
 
 	}
 
 	public void testNumberVar() {
 		IValueCollection collection = inference("var a = 1");
 		IValueReference a = collection.getChild("a");
 		assertEquals(getTypes(NUMBER), a.getTypes());
 	}
 
 	public void testStringVar() {
 		IValueCollection collection = inference("var a = \"Hello\"");
 		IValueReference a = collection.getChild("a");
 		assertEquals(getTypes(STRING), a.getTypes());
 	}
 
 	public void testBooleanVar() {
 		IValueCollection collection = inference("var a = false");
 		IValueReference a = collection.getChild("a");
 		assertEquals(getTypes(BOOLEAN), a.getTypes());
 	}
 
 	public void testXmlVar() {
 		IValueCollection collection = inference("var a = <hello/>");
 		IValueReference a = collection.getChild("a");
 		assertEquals(getTypes(XML), a.getTypes());
 	}
 
 	public void testConditionalOperator() {
 		IValueCollection collection = inference("var a = isActive ? 1 : 0");
 		IValueReference a = collection.getChild("a");
 		assertEquals(getTypes(NUMBER), a.getTypes());
 	}
 
 	public void testReturnNumber() {
 		List<String> lines = new StringList();
 		lines.add("var s = 1");
 		lines.add("s.execute = function() {");
 		lines.add("  return 1");
 		lines.add("}");
 		lines.add("var z = s.execute()");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference z = collection.getChild("z");
 		assertEquals(getTypes(NUMBER), z.getTypes());
 	}
 
 	public void testReturnObjectProperties() {
 		List<String> lines = new StringList();
 		lines.add("var s = 1");
 		lines.add("s.execute = function() {");
 		lines.add("  return { a: 1, b: true }");
 		lines.add("}");
 		lines.add("var z = s.execute()");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference z = collection.getChild("z");
 		assertEquals(getTypes(OBJECT), z.getTypes());
 		final IValueReference a = z.getChild("a");
 		assertTrue(a.exists());
 		assertEquals(getTypes(NUMBER), a.getTypes());
 		final IValueReference b = z.getChild("b");
 		assertTrue(b.exists());
 		assertEquals(getTypes(BOOLEAN), b.getTypes());
 	}
 
 	public void testInlineFunctionStatementCall() {
 		List<String> lines = new StringList();
 		lines.add("var str = (function() {");
 		lines.add("  return 'Hello'");
 		lines.add("})()");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference str = collection.getChild("str");
 		assertEquals(getTypes(STRING), str.getTypes());
 	}
 
 	public void testToStringMethod() {
 		List<String> lines = new StringList();
 		lines.add("var num = 1");
 		lines.add("var str = num.toString()");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference str = collection.getChild("str");
 		assertEquals(getTypes(STRING), str.getTypes());
 	}
 
 	public void testStringLengthProperty() {
 		List<String> lines = new StringList();
 		lines.add("var str = 'STRING'");
 		lines.add("var len = str.length");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference len = collection.getChild("len");
 		assertEquals(getTypes(NUMBER), len.getTypes());
 	}
 
 	public void testFunctionCall() {
 		List<String> lines = new StringList();
 		lines.add("function hello() {");
 		lines.add("  return 'Hello'");
 		lines.add("}");
 		lines.add("var str = hello()");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference str = collection.getChild("str");
 		assertEquals(getTypes(STRING), str.getTypes());
 	}
 
 	public void testIf() {
 		List<String> lines = new StringList();
 		lines.add("if (1 == 2) {");
 		lines.add("  x = 1");
 		lines.add("}");
 		lines.add("else {");
 		lines.add("  x = 'No'");
 		lines.add("}");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference x = collection.getChild("x");
 		assertEquals(getTypes(STRING, NUMBER), x.getTypes());
 	}
 
 	public void testIf2() {
 		List<String> lines = new StringList();
 		lines.add("if (1 == 2) {");
 		lines.add("  x = 1");
 		lines.add("}");
 		lines.add("else {");
 		lines.add("  y = 'No'");
 		lines.add("}");
 		IValueCollection collection = inference(lines.toString());
 		assertEquals(getTypes(NUMBER), collection.getChild("x").getTypes());
 		assertEquals(getTypes(STRING), collection.getChild("y").getTypes());
 	}
 
 	public void testFor() {
 		List<String> lines = new StringList();
 		lines.add("for (var i = 0; i < 10; ++i) {");
 		lines.add("  x = 'No'");
 		lines.add("}");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference i = collection.getChild("i");
 		assertEquals(getTypes(NUMBER), i.getTypes());
 		IValueReference x = collection.getChild("x");
 		assertEquals(getTypes(STRING), x.getTypes());
 	}
 
 	public void testForIn() {
 		List<String> lines = new StringList();
 		lines.add("for (var i in objectWithIterator) {");
 		lines.add("  print(objectWithIterator[i])");
 		lines.add("}");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference i = collection.getChild("i");
 		assertEquals(getTypes(STRING), i.getTypes());
 	}
 
 	public void testWhile() {
 		List<String> lines = new StringList();
 		lines.add("while (1 == 2) {");
 		lines.add("  x = 'No'");
 		lines.add("}");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference x = collection.getChild("x");
 		assertEquals(getTypes(STRING), x.getTypes());
 	}
 
 	public void testUnknownVar() {
 		List<String> lines = new StringList();
 		lines.add("var x = y");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference x = collection.getChild("x");
 		assertEquals(getTypes(), x.getTypes());
 		assertFalse(collection.hasChild("y"));
 	}
 
 	public void testUnknownProperty() {
 		List<String> lines = new StringList();
 		lines.add("var x = {a:1}");
 		lines.add("var y = x.b");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference x = collection.getChild("x");
 		assertEquals(getTypes(OBJECT), x.getTypes());
 		assertEquals(Collections.singleton("a"), x.getDirectChildren());
 		IValueReference y = collection.getChild("y");
 		assertEquals(getTypes(), y.getTypes());
 	}
 
 	public void testUnknownProperty3() {
 		List<String> lines = new StringList();
 		lines.add("x.y.z = 1");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference x = collection.getChild("x");
 		IValueReference y = x.getChild("y");
 		IValueReference z = y.getChild("z");
 		assertEquals(getTypes(NUMBER), z.getTypes());
 	}
 
 	public void testSwitch() {
 		List<String> lines = new StringList();
 		lines.add("switch(n) {");
 		lines.add("case 0: str = 'Zero'; break;");
 		lines.add("case 1: str = 'One'; break;");
 		lines.add("}");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference x = collection.getChild("str");
 		assertEquals(getTypes(STRING), x.getTypes());
 	}
 
 	public void testWith() {
 		List<String> lines = new StringList();
 		lines.add("var a = {name:1}");
 		lines.add("with (a) {");
 		lines.add("  name = 'Alex'");
 		lines.add("}");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference a = collection.getChild("a");
 		assertEquals(getTypes(OBJECT), a.getTypes());
 		IValueReference name = a.getChild("name");
 		assertTrue(name.getTypes().containsAll(getTypes(STRING)));
 	}
 
 	public void testNew() {
 		List<String> lines = new StringList();
 		lines.add("var str = new String()");
 		lines.add("var num = new Number()");
 		lines.add("var bool = new Boolean()");
 		lines.add("var arr = new Array()");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference str = collection.getChild("str");
 		assertEquals(getTypes(STRING), str.getTypes());
 		IValueReference num = collection.getChild("num");
 		assertEquals(getTypes(NUMBER), num.getTypes());
 		IValueReference bool = collection.getChild("bool");
 		assertEquals(getTypes(BOOLEAN), bool.getTypes());
 		IValueReference arr = collection.getChild("arr");
 		assertEquals(getTypes(ARRAY), arr.getTypes());
 	}
 
 	public void testRecursion1() {
 		List<String> lines = new StringList();
 		lines.add("context = { 'index': 1, 'prev': context }");
 		lines.add("doSomethind()");
 		lines.add("context = context['prev']");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference context = collection.getChild("context");
 		IValueReference index = context.getChild("index");
 		assertFalse(index.exists());
 		IValueReference prev = context.getChild("prev");
 		assertFalse(prev.exists());
 	}
 
 	public void testRecursion2() {
 		List<String> lines = new StringList();
 		lines.add("var a = function() { return 1 }");
 		lines.add("a.operation2 = a");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference a = collection.getChild("a");
 		assertTrue(a.exists());
 	}
 
 	public void testRecursion3() {
 		List<String> lines = new StringList();
 		lines.add("var a = function() { return 1 }");
 		lines.add("a.operation2 = a");
 		lines.add("var z = a.operation2()");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference z = collection.getChild("z");
 		assertEquals(getTypes(NUMBER), z.getTypes());
 	}
 
 	public void testExampleTypeProvider1() {
 		List<String> lines = new StringList();
 		lines.add("var a:ExampleService");
 		lines.add("var status = a.execute().status");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference status = collection.getChild("status");
 		assertEquals(getTypes(NUMBER), status.getTypes());
 	}
 
 	public void testExampleTypeProvider2() {
 		List<String> lines = new StringList();
 		lines.add("var a:ExampleService");
 		lines.add("var name = a.execute().service.name");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference name = collection.getChild("name");
 		assertEquals(getTypes(STRING), name.getTypes());
 	}
 
 	public void testExampleTypeProvider3() {
 		List<String> lines = new StringList();
 		lines.add("var a:ExampleService2");
 		lines.add("var name = a.execute().service.name");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference name = collection.getChild("name");
 		assertTrue(name.exists());
 		assertTrue(name.getTypes().isEmpty());
 	}
 
 	public void testGenericArrayTypeMethod() {
 		List<String> lines = new StringList();
 		lines.add("var a:" + ExampleTypeProvider.TYPE_GENERIC_ARRAY_METHOD);
 		lines.add("var name = a.execute();");
 		lines.add("var name2 = a.execute()[0];");
 		IValueCollection collection = inference(lines.toString());
 
 		IValueReference name = collection.getChild("name");
 		assertTrue(name.exists());
 		assertEquals(1, name.getTypes().size());
 		assertEquals("Array<String>", typename(name.getTypes()));
 
 		IValueReference name2 = collection.getChild("name2");
 		assertTrue(name2.exists());
 		assertEquals(1, name2.getTypes().size());
 		assertEquals("String", typename(name2.getTypes()));
 	}
 
 	public void testGenericArrayResolverMethod() {
 		List<String> lines = new StringList();
 		lines.add("var name = myGenericArrayTest.execute()[0];");
 		IValueCollection collection = inference(lines.toString());
 
 		IValueReference name = collection.getChild("name");
 		assertTrue(name.exists());
 		assertEquals(1, name.getTypes().size());
 		assertEquals("String", name.getTypes().iterator().next().getName());
 
 	}
 
 	public void testExampleElementResolver1() {
 		List<String> lines = new StringList();
 		lines.add("var name = ExampleGlobal.execute().service.name");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference name = collection.getChild("name");
 		assertEquals(getTypes(STRING), name.getTypes());
 	}
 
 	public void testExampleElementResolver2() {
 		List<String> lines = new StringList();
 		lines.add("var name = executeExampleGlobal().service.name");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference name = collection.getChild("name");
 		assertEquals(getTypes(STRING), name.getTypes());
 	}
 
 	public void testSelfReferenceAssignment() throws Exception {
 		List<String> lines = new StringList();
 		lines.add("var str = '10';");
 		lines.add("str = str.big();");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference name = collection.getChild("str");
 		assertEquals(getTypes(STRING), name.getTypes());
 	}
 
 	public void testStaticTypeAssignment() throws Exception {
 		List<String> lines = new StringList();
 		lines.add("var num = Number;");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference name = collection.getChild("num");
 		assertEquals(getTypes(NUMBER), name.getTypes());
 
 		assertEquals(true, name.getAttribute(IReferenceAttributes.STATIC));
 
 		// TODO should a static reference getchild really return existing none
 		// static childs?
 		// assertEquals(true, name.getChild("prototype").exists());
 		// assertEquals(false, name.getChild("toFixed").exists());
 
 	}
 
 	public void testAssignToResolvedProperty() {
 		List<String> lines = new StringList();
 		lines.add("ExampleGlobal.abcdef = 1");
 		inference(lines.toString());
 	}
 
 	public void testJavaClzIntegrationWithPackagesPrefix() {
 		List<String> lines = new StringList();
 		lines.add("var str = Packages.java.lang.String;");
 		lines.add("var x = new str()");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference strClz = collection.getChild("str");
 		assertEquals(1, strClz.getTypes().size());
 		Type type = strClz.getTypes().iterator().next();
 		assertEquals("java.lang.String", type.getName());
 
 		boolean valueOfFound = false;
 		for (Member member : type.getMembers()) {
 			valueOfFound = member.getName().equals("valueOf");
 			if (valueOfFound) {
 				assertEquals(true, member.isStatic());
 				break;
 			}
 		}
 		assertEquals(true, valueOfFound);
 
 		IValueReference str = collection.getChild("x");
 		assertEquals(1, str.getTypes().size());
 		assertEquals(type, str.getTypes().iterator().next());
 
 		boolean toStringFound = false;
 		for (Member member : type.getMembers()) {
 			toStringFound = member.getName().equals("toString");
 			if (toStringFound) {
 				assertEquals(false, member.isStatic());
 				break;
 			}
 		}
 		assertEquals(true, toStringFound);
 
 	}
 
 	public void testJavaClzIntegrationWithJavaPrefix() {
 		List<String> lines = new StringList();
 		lines.add("var str = java.lang.String;");
 		lines.add("var x = new str()");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference strClz = collection.getChild("str");
 		assertEquals(1, strClz.getTypes().size());
 		Type type = strClz.getTypes().iterator().next();
 		assertEquals("java.lang.String", type.getName());
 
 		boolean valueOfFound = false;
 		for (Member member : type.getMembers()) {
 			valueOfFound = member.getName().equals("valueOf");
 			if (valueOfFound) {
 				assertEquals(true, member.isStatic());
 				break;
 			}
 
 		}
 		assertEquals(true, valueOfFound);
 
 		IValueReference str = collection.getChild("x");
 		assertEquals(1, str.getTypes().size());
 		assertEquals(type, str.getTypes().iterator().next());
 
 		boolean toStringFound = false;
 		for (Member member : type.getMembers()) {
 			toStringFound = member.getName().equals("toString");
 			if (toStringFound) {
 				assertEquals(false, member.isStatic());
 				break;
 			}
 
 		}
 		assertEquals(true, toStringFound);
 
 	}
 
 	public void testJavaIntegrationWithJavaPrefix() {
 		List<String> lines = new StringList();
 		lines.add("var str = new java.lang.String()");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference strClz = collection.getChild("str");
 		assertEquals(1, strClz.getTypes().size());
 		Type type = strClz.getTypes().iterator().next();
 		assertEquals("java.lang.String", type.getName());
 
 		boolean toStringFound = false;
 		for (Member member : type.getMembers()) {
 			toStringFound = member.getName().equals("toString");
 			if (toStringFound) {
 				assertEquals(false, member.isStatic());
 				break;
 			}
 
 		}
 
 		assertEquals(true, toStringFound);
 	}
 
 	public void testJavaIntegrationWithPackagesPrefix() {
 		List<String> lines = new StringList();
 		lines.add("var str = new Packages.java.lang.String()");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference strClz = collection.getChild("str");
 		assertEquals(1, strClz.getTypes().size());
 		Type type = strClz.getTypes().iterator().next();
 		assertEquals("java.lang.String", type.getName());
 
 		boolean toStringFound = false;
 		for (Member member : type.getMembers()) {
 			toStringFound = member.getName().equals("toString");
 			if (toStringFound) {
 				assertEquals(false, member.isStatic());
 				break;
 			}
 		}
 
 		assertEquals(true, toStringFound);
 	}
 
 	public void testJSDocParamWithDefaultProperties() throws Exception {
 		List<String> lines = new StringList();
 		lines.add("/**");
 		lines.add(" * @param node a nice node");
 		lines.add(" * @param node.name the name of the node");
 		lines.add(" * @param node.value the value of the node");
 		lines.add(" */");
 		lines.add("function addChild(node) {");
 		lines.add("}");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference addChild = collection.getChild("addChild");
 		assertEquals(1, addChild.getDirectChildren().size());
 		assertEquals(IValueReference.FUNCTION_OP, addChild.getDirectChildren()
 				.iterator().next());
 
 		IValueCollection functionCollection = (IValueCollection) addChild
 				.getAttribute(IReferenceAttributes.FUNCTION_SCOPE);
 		assertNotNull(functionCollection);
 
 		IValueReference node = functionCollection.getChild("node");
 		assertEquals(0, node.getDirectChildren().size());
 
 		assertNotNull(node.getDeclaredType());
 
 		assertEquals(2, node.getDeclaredType().getMembers().size());
 
 		assertEquals("name", node.getDeclaredType().getMembers().get(0)
 				.getName());
 		assertEquals("value", node.getDeclaredType().getMembers().get(1)
 				.getName());
 
 	}
 
 	public void testJSDocTypeTagFunction() throws Exception {
 		List<String> lines = new StringList();
 		lines.add("/**");
 		lines.add(" * @type String");
 		lines.add(" */");
 		lines.add("function getChild() {");
 		lines.add("}");
 		lines.add("function test() {");
 		lines.add("var x = getChild();");
 		lines.add("}");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference test = collection.getChild("test");
 		IValueCollection functionScope = (IValueCollection) test
 				.getAttribute(IReferenceAttributes.FUNCTION_SCOPE);
 		IValueReference x = functionScope.getChild("x");
 		assertEquals(1, x.getTypes().size());
 		assertEquals("String", x.getTypes().iterator().next().getName());
 	}
 
 	public void testJSDocReturnsTagFunction() throws Exception {
 		List<String> lines = new StringList();
 		lines.add("/**");
 		lines.add(" * @returns {String} a nice string");
 		lines.add(" */");
 		lines.add("function getChild() {");
 		lines.add("}");
 		lines.add("function test() {");
 		lines.add("var x = getChild();");
 		lines.add("}");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference test = collection.getChild("test");
 		IValueCollection functionScope = (IValueCollection) test
 				.getAttribute(IReferenceAttributes.FUNCTION_SCOPE);
 		IValueReference x = functionScope.getChild("x");
 		assertEquals(1, x.getTypes().size());
 		assertEquals("String", x.getTypes().iterator().next().getName());
 	}
 
 	public void testJSDocReturnsTagLazyFunction() throws Exception {
 		List<String> lines = new StringList();
 		lines.add("function test() {");
 		lines.add("var x = getChild();");
 		lines.add("}");
 		lines.add("/**");
 		lines.add(" * @returns {String} a nice string");
 		lines.add(" */");
 		lines.add("function getChild() {");
 		lines.add("}");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference test = collection.getChild("test");
 		IValueCollection functionScope = (IValueCollection) test
 				.getAttribute(IReferenceAttributes.FUNCTION_SCOPE);
 		IValueReference x = functionScope.getChild("x");
 		assertEquals(1, x.getTypes().size());
 		assertEquals("String", x.getTypes().iterator().next().getName());
 	}
 
 	public void testJSDocTypeTagVariable() throws Exception {
 		List<String> lines = new StringList();
 		lines.add("/**");
 		lines.add(" * @type String");
 		lines.add(" */");
 		lines.add("var str;");
 		lines.add("function test() {");
 		lines.add("var x = str;");
 		lines.add("}");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference test = collection.getChild("test");
 		IValueCollection functionScope = (IValueCollection) test
 				.getAttribute(IReferenceAttributes.FUNCTION_SCOPE);
 		IValueReference x = functionScope.getChild("x");
 		assertNotNull(x.getDeclaredType());
 		assertEquals("String", x.getDeclaredType().getName());
 	}
 
 	public void testJSDocTypeTagVariableLazy() throws Exception {
 		List<String> lines = new StringList();
 		lines.add("function test() {");
 		lines.add("var x = str;");
 		lines.add("}");
 		lines.add("/**");
 		lines.add(" * @type String");
 		lines.add(" */");
 		lines.add("var str;");
 		IValueCollection collection = inference(lines.toString());
 		IValueReference test = collection.getChild("test");
 		IValueCollection functionScope = (IValueCollection) test
 				.getAttribute(IReferenceAttributes.FUNCTION_SCOPE);
 		IValueReference x = functionScope.getChild("x");
 		assertNotNull(x.getDeclaredType());
 		assertEquals("String", x.getDeclaredType().getName());
 	}
 
 }
