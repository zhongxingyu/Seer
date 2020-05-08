 package com.codingstory.polaris.parser;
 
 import com.codingstory.polaris.IdGenerator;
 import com.codingstory.polaris.SimpleIdGenerator;
 import com.codingstory.polaris.parser.ParserProtos.ClassType;
 import com.codingstory.polaris.parser.ParserProtos.ClassTypeHandle;
 import com.codingstory.polaris.parser.ParserProtos.FileHandle;
 import com.codingstory.polaris.parser.ParserProtos.Method;
 import com.codingstory.polaris.parser.ParserProtos.MethodUsage;
 import com.codingstory.polaris.parser.ParserProtos.TypeHandle;
 import com.codingstory.polaris.parser.ParserProtos.TypeKind;
 import com.codingstory.polaris.parser.ParserProtos.TypeUsage;
 import com.codingstory.polaris.parser.ParserProtos.Usage;
 import com.codingstory.polaris.parser.ParserProtos.Variable;
 import com.codingstory.polaris.parser.ParserProtos.VariableUsage;
 import com.google.common.base.Objects;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import static com.codingstory.polaris.parser.TypeUtils.handleOf;
 import static com.codingstory.polaris.parser.TypeUtils.positionOf;
 import static com.codingstory.polaris.parser.TypeUtils.spanOf;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 public class MultiPassProcessorsTest {
 
     private static final String TEST_PROJECT = "TestProject";
     private static final IdGenerator ID_GENERATOR = new SimpleIdGenerator();
     private static final Comparator<ClassType> CLASS_TYPE_COMPARATOR_BY_NAME = new Comparator<ClassType>() {
         @Override
         public int compare(ClassType left, ClassType right) {
             return left.getHandle().getName().compareTo(right.getHandle().getName());
         }
     };
 
     @Test
     public void testEmpty() throws IOException {
         SecondPassProcessor.Result result = extractFromCode("");
         assertTrue(result.getClassTypes().isEmpty());
         assertTrue(result.getUsages().isEmpty());
     }
 
     @Test
     public void testClass() throws IOException {
         String code = "package pkg;\npublic class MyClass { /* body */ }\n";
         SecondPassProcessor.Result result = extractFromCode(code);
         ClassType clazz = Iterables.getOnlyElement(result.getClassTypes());
         assertEquals("pkg.MyClass", clazz.getHandle().getName());
         assertEquals(ClassType.Kind.CLASS, clazz.getKind());
         assertEquals(spanOf(positionOf(1, 13), positionOf(1, 20)), clazz.getJumpTarget().getSpan());
         Usage clazzDeclaration = findUniqueTypeUsageByKind(
                 result.getUsages(), TypeUsage.Kind.TYPE_DECLARATION);
         assertEquals(clazz.getHandle(), clazzDeclaration.getType().getType().getClazz());
         assertEquals(spanOf(positionOf(1, 13), positionOf(1, 20)), clazzDeclaration.getJumpTarget().getSpan());
         assertEquals(clazzDeclaration.getJumpTarget(), clazzDeclaration.getDefinitionJumpTarget());
     }
 
     @Test
     public void testClass_multiple() throws IOException {
         String code = "package pkg;\nclass A {}\nclass B {}\nclass C {}\n";
         SecondPassProcessor.Result result = extractFromCode(code);
         List<ClassType> classes = result.getClassTypes();
         List<String> names = Lists.newArrayList();
         for (ClassType clazz: classes) {
             names.add(clazz.getHandle().getName());
         }
         assertEquals(ImmutableList.of("pkg.A", "pkg.B", "pkg.C"), names);
     }
 
     @Test
     public void testClass_noPackage() throws IOException {
         String code = "class A {}";
         ClassType clazz = extractUniqueTypeFromCode(code);
         assertEquals("A", clazz.getHandle().getName());
     }
 
     @Test
     public void testClass_inheritance() throws IOException {
         String code = "class A extends B implements C, D {}";
         SecondPassProcessor.Result result = extractFromCode(code);
         ClassType clazz = Iterables.getOnlyElement(result.getClassTypes());
         assertEquals("A", clazz.getHandle().getName());
         List<String> superTypeNames = Lists.newArrayList();
         for (TypeHandle superType : clazz.getSuperTypesList()) {
             assertEquals(TypeKind.CLASS, superType.getKind());
             superTypeNames.add(superType.getClazz().getName());
         }
         assertEquals(ImmutableList.of("B", "C", "D"), superTypeNames);
         List<Usage> usages = filterTypeUsagesByKind(result.getUsages(), TypeUsage.Kind.SUPER_CLASS);
         assertEquals(3, usages.size());
         assertEquals(spanOf(positionOf(0, 16), positionOf(0, 17)), usages.get(0).getJumpTarget().getSpan());
         assertEquals("B", usages.get(0).getType().getType().getClazz().getName());
         assertFalse(usages.get(0).hasDefinitionJumpTarget());
         assertEquals(spanOf(positionOf(0, 29), positionOf(0, 30)), usages.get(1).getJumpTarget().getSpan());
         assertEquals("C", usages.get(1).getType().getType().getClazz().getName());
         assertFalse(usages.get(1).hasDefinitionJumpTarget());
         assertEquals(spanOf(positionOf(0, 32), positionOf(0, 33)), usages.get(2).getJumpTarget().getSpan());
         assertEquals("D", usages.get(2).getType().getType().getClazz().getName());
         assertFalse(usages.get(2).hasDefinitionJumpTarget());
     }
 
     @Test
     public void testClass_generic() throws IOException {
         String code = "class A<T> {}";
         SecondPassProcessor.Result result = extractFromCode(code);
         ClassType clazz = Iterables.getOnlyElement(result.getClassTypes());
         assertEquals("A", clazz.getHandle().getName());
         assertEquals(spanOf(positionOf(0, 6), positionOf(0, 7)), clazz.getJumpTarget().getSpan());
         Usage usage = findUniqueTypeUsageByKind(result.getUsages(), TypeUsage.Kind.GENERIC_TYPE_PARAMETER);
         assertEquals(Usage.Kind.TYPE, usage.getKind());
         assertEquals("T", usage.getType().getType().getClazz().getName());
         assertEquals(spanOf(positionOf(0, 8), positionOf(0, 9)), usage.getJumpTarget().getSpan());
         assertFalse(usage.hasDefinitionJumpTarget());
     }
 
     @Test
     @Ignore // The new javaparser has a different behavior to the old.
     public void testClass_javaDoc() throws IOException {
         String code = "/** doc */ class A {}";
         ClassType clazz = extractUniqueTypeFromCode(code);
         assertEquals("A", clazz.getHandle().getName());
         assertEquals("doc", clazz.getJavaDoc());
     }
 
     @Test
     public void testInterface() throws IOException {
         String code = "package pkg; public interface I {};";
         ClassType clazz = extractUniqueTypeFromCode(code);
         assertEquals(ClassType.Kind.INTERFACE, clazz.getKind());
         assertEquals("pkg.I", clazz.getHandle().getName());
     }
 
     @Test
     public void testInnerClass() throws IOException {
         String code = "package pkg; public class A { static public class B { public static class C {} } }";
         SecondPassProcessor.Result result = extractFromCode(code);
         List<ClassType> classes = result.getClassTypes();
         Collections.sort(classes, CLASS_TYPE_COMPARATOR_BY_NAME);
         assertEquals(3, classes.size());
         assertEquals("pkg.A", classes.get(0).getHandle().getName());
         assertEquals("pkg.A.B", classes.get(1).getHandle().getName());
         assertEquals("pkg.A.B.C", classes.get(2).getHandle().getName());
     }
 
     // TODO: testClass_public
     // TODO: testClass_private
     // TODO: testClass_packagePrivate
     // TODO: testClass_abstract
     // TODO: testClass_final
     // TODO: testNestedClass
     // TODO: testNestedClass_static
 
     @Test
     public void testEnum() throws IOException {
         String code = "package pkg; public enum E { /* some values */ }";
         SecondPassProcessor.Result result = extractFromCode(code);
         ClassType clazz = Iterables.getOnlyElement(result.getClassTypes());
         assertEquals(ClassType.Kind.ENUM, clazz.getKind());
         assertEquals("pkg.E", clazz.getHandle().getName());
         Usage typeDeclaration = findUniqueTypeUsageByKind(result.getUsages(), TypeUsage.Kind.TYPE_DECLARATION);
         assertEquals(clazz.getHandle(), typeDeclaration.getType().getType().getClazz());
         assertEquals(spanOf(positionOf(0, 25), positionOf(0, 26)), typeDeclaration.getJumpTarget().getSpan());
     }
 
     // TODO: testEnum_public
     // TODO: testEnum_private
     // TODO: testEnum_packagePrivate
     // TODO: testEnumValues
 
     @Test
     public void testMethod() throws IOException {
         String code = "package pkg; class A { void func() {} }";
         SecondPassProcessor.Result result = extractFromCode(code);
         ClassType clazz = Iterables.getOnlyElement(result.getClassTypes());
         Method method = Iterables.getOnlyElement(clazz.getMethodsList());
         assertEquals("pkg.A.func", method.getHandle().getName());
         Usage methodDeclaration = findUniqueMethodUsageByKind(
                 result.getUsages(), MethodUsage.Kind.METHOD_DECLARATION);
         assertEquals(method.getHandle(), methodDeclaration.getMethod().getMethod());
         assertEquals(spanOf(positionOf(0, 28), positionOf(0, 32)), methodDeclaration.getJumpTarget().getSpan());
         assertEquals(methodDeclaration.getJumpTarget(), methodDeclaration.getDefinitionJumpTarget());
     }
 
     @Test
     public void testMethod_inEnum() throws IOException {
         String code = "enum E { SOME_VALUE; void f() {} }";
         ClassType clazz = extractUniqueTypeFromCode(code);
         Method method = Iterables.getOnlyElement(clazz.getMethodsList());
         assertEquals("E.f", method.getHandle().getName());
     }
 
     @Test
     public void testMethod_complexSignature() throws IOException {
         String code = "class A { B f(C c, D d) throws E, F { return new B(); } }";
         SecondPassProcessor.Result result = extractFromCode(code);
         ClassType clazz = Iterables.getOnlyElement(result.getClassTypes());
         Method method = Iterables.getOnlyElement(clazz.getMethodsList());
         assertEquals("A.f", method.getHandle().getName());
         assertEquals("B", method.getReturnType().getClazz().getName());
         assertEquals(2, method.getParametersCount());
         Variable parameter0 = method.getParameters(0);
         assertEquals("C", parameter0.getType().getClazz().getName());
         assertEquals("c", parameter0.getHandle().getName());
         assertEquals(Variable.Kind.PARAMETER, parameter0.getKind());
         Variable parameter1 = method.getParameters(1);
         assertEquals("D", parameter1.getType().getClazz().getName());
         assertEquals("d", parameter1.getHandle().getName());
         assertEquals(Variable.Kind.PARAMETER, parameter1.getKind());
         assertEquals(2, method.getExceptionsCount());
         TypeHandle exceptionType0 = method.getExceptions(0);
         assertEquals("E", exceptionType0.getClazz().getName());
         TypeHandle exceptionType1 = method.getExceptions(1);
         assertEquals("F", exceptionType1.getClazz().getName());
         List<Usage> usages = filterTypeUsagesByKind(result.getUsages(), TypeUsage.Kind.METHOD_SIGNATURE);
         assertEquals(5, usages.size());
         assertEquals(spanOf(positionOf(0, 10), positionOf(0, 11)), usages.get(0).getJumpTarget().getSpan());
         assertEquals("B", usages.get(0).getType().getType().getClazz().getName());
         assertEquals(spanOf(positionOf(0, 14), positionOf(0, 15)), usages.get(1).getJumpTarget().getSpan());
         assertEquals("C", usages.get(1).getType().getType().getClazz().getName());
         assertEquals(spanOf(positionOf(0, 19), positionOf(0, 20)), usages.get(2).getJumpTarget().getSpan());
         assertEquals("D", usages.get(2).getType().getType().getClazz().getName());
         assertEquals(spanOf(positionOf(0, 31), positionOf(0, 32)), usages.get(3).getJumpTarget().getSpan());
         assertEquals("E", usages.get(3).getType().getType().getClazz().getName());
         assertEquals(spanOf(positionOf(0, 34), positionOf(0, 35)), usages.get(4).getJumpTarget().getSpan());
         assertEquals("F", usages.get(4).getType().getType().getClazz().getName());
     }
 
     // TODO: testMethod_generic
 
     @Test
     public void testConstructor() throws IOException {
         String code = "class A { A() {} }";
         Method method = extractUniqueMethodFromCode(code);
         assertEquals("A.<init>", method.getHandle().getName());
         assertEquals(spanOf(positionOf(0, 10), positionOf(0, 11)), method.getJumpTarget().getSpan());
     }
 
     @Test
     public void testStaticInitializer() throws IOException {
         String code = "class A { static { int a; } }";
         Method method = extractUniqueMethodFromCode(code);
         assertEquals("A.<cinit>", method.getHandle().getName());
         assertEquals(spanOf(positionOf(0, 10), positionOf(0, 16)), method.getJumpTarget().getSpan());
     }
 
     // TODO: testMethod_public
     // TODO: testMethod_private
     // TODO: testMethod_protected
     // TODO: testMethod_packagePrivate
     // TODO: testMethod_final
     // TODO: testMethod_static
 
     @Test
     public void testField() throws IOException {
         String code = "package pkg; class A { int n; }";
         SecondPassProcessor.Result result = extractFromCode(code);
         ClassType clazz = Iterables.getOnlyElement(result.getClassTypes());
         Variable field = Iterables.getOnlyElement(clazz.getFieldsList());
         assertEquals("pkg.A.n", field.getHandle().getName());
         assertEquals(Variable.Kind.FIELD, field.getKind());
         assertEquals(handleOf(PrimitiveTypes.INTEGER), field.getType());
         Usage fieldDeclaration = findUniqueVariableUsageByKind(result.getUsages(), VariableUsage.Kind.DECLARATION);
         assertEquals(field.getHandle(), fieldDeclaration.getVariable().getVariable());
         assertEquals(spanOf(positionOf(0, 27), positionOf(0, 28)), fieldDeclaration.getJumpTarget().getSpan());
         Usage usage = findUniqueTypeUsageByKind(result.getUsages(), TypeUsage.Kind.FIELD);
         assertEquals(spanOf(positionOf(0, 23), positionOf(0, 26)), usage.getJumpTarget().getSpan());
         assertEquals(handleOf(PrimitiveTypes.INTEGER), usage.getType().getType());
     }
 
     @Test
     public void testField_fullyQualifiedType() throws IOException {
         String code = "class A { java.util.List l; }";
         Variable field = extractUniqueFieldFromCode(code);
         assertEquals("A.l", field.getHandle().getName());
         TypeHandle type = field.getType();
         assertEquals(TypeKind.CLASS, type.getKind());
         ClassTypeHandle clazz = type.getClazz();
         assertFalse(clazz.getResolved());
         assertEquals("java.util.List", type.getClazz().getName());
     }
 
     @Test
     public void testField_unqualifiedType() throws IOException {
         String code = "import java.util.List; class A { List l; }";
         Variable field = extractUniqueFieldFromCode(code);
         assertEquals("A.l", field.getHandle().getName());
         TypeHandle type = field.getType();
         assertEquals(TypeKind.CLASS, type.getKind());
         ClassTypeHandle clazz = type.getClazz();
         assertFalse(clazz.getResolved());
         assertEquals("java.util.List", clazz.getName());
     }
 
     @Test
     public void testField_genericType() throws IOException {
         String code = "class A { List<Integer> m; }";
         SecondPassProcessor.Result result = extractFromCode(code);
         Variable field = Iterables.getOnlyElement(
                 Iterables.getOnlyElement(result.getClassTypes()).getFieldsList());
         assertEquals("A.m", field.getHandle().getName());
         Usage usage = findUniqueTypeUsageByKind(result.getUsages(), TypeUsage.Kind.FIELD);
         assertEquals("List", usage.getType().getType().getClazz().getName());
         // TODO: Check span
         // assertEquals(spanOf(positionOf(0, 10), positionOf(0, 14)), usage.getJumpTarget().getSpan());
         // TODO: Extract generic types from field declarations.
         // Usage usage = findUniqueTypeUsageByKind(result.getUsages(), TypeUsage.Kind.GENERIC_TYPE_PARAMETER);
         // assertEquals("Integer", usage.getType().getType().getClazz().getName());
     }
 
     @Test
     public void testField_initialized() throws IOException {
         String code = "class A { int m = 1; }";
         Variable field = extractUniqueFieldFromCode(code);
         assertEquals("A.m", field.getHandle().getName());
     }
 
     // TODO: testField_multiple
     @Test
     public void testLocalVariable() throws IOException {
         String code = "package pkg; class A { void f() { B n; } }";
         List<Usage> usages = extractFromCode(code).getUsages();
         TypeUsage typeUsage = findUniqueTypeUsageByKind(usages, TypeUsage.Kind.LOCAL_VARIABLE).getType();
         VariableUsage variableUsage =
                 findUniqueVariableUsageByKind(usages, VariableUsage.Kind.DECLARATION).getVariable();
         assertEquals("B", typeUsage.getType().getClazz().getName());
         assertEquals("n", variableUsage.getVariable().getName());
         // assertEquals(Variable.Kind.LOCAL_VARIABLE, variableUsage.getVariable().getScope());
         // TODO: Test local variable id.
     }
 
     // TODO: testLocalVariable_multiple
     // TODO: testLocalVariable_array
 
     @Test
     public void testLocalVariable_access() throws IOException {
         String code = "class A { void f() { int n; n = 10; } }";
         Usage usage = findUniqueVariableUsageByKind(
                 extractFromCode(code).getUsages(), VariableUsage.Kind.ACCESS);
         assertEquals("n", usage.getVariable().getVariable().getName());
         assertEquals(spanOf(positionOf(0, 28), positionOf(0, 29)), usage.getJumpTarget().getSpan());
         assertEquals(spanOf(positionOf(0, 25), positionOf(0, 26)), usage.getDefinitionJumpTarget().getSpan());
         // assertEquals(Variable.Kind.LOCAL_VARIABLE, usage.getVariable().getKind());
     }
 
     @Test
     public void testParameter() throws IOException {
         String code = "class A { void f(int n) {} }";
         Usage usage = findUniqueVariableUsageByKind(
                 extractFromCode(code).getUsages(), VariableUsage.Kind.DECLARATION);
         assertEquals("n", usage.getVariable().getVariable().getName());
         assertEquals(usage.getJumpTarget(), usage.getDefinitionJumpTarget());
     }
 
     @Test
     public void testParameter_access() throws IOException {
         String code = "class A { void f(int n) { n = 10; } }";
         Usage usage = findUniqueVariableUsageByKind(
                 extractFromCode(code).getUsages(), VariableUsage.Kind.ACCESS);
         assertEquals("n", usage.getVariable().getVariable().getName());
         assertEquals(spanOf(positionOf(0, 26), positionOf(0, 27)), usage.getJumpTarget().getSpan());
         assertEquals(spanOf(positionOf(0, 21), positionOf(0, 22)), usage.getDefinitionJumpTarget().getSpan());
     }
 
     @Test
     public void testMethodCall_implicitThis() throws IOException {
         String code = "class A { void f() { f(); } }";
         Usage usage = findUniqueMethodUsageByKind(
                 extractFromCode(code).getUsages(),
                 MethodUsage.Kind.METHOD_CALL);
         assertEquals("A.f", usage.getMethod().getMethod().getName());
         assertEquals(spanOf(positionOf(0, 21), positionOf(0, 22)), usage.getJumpTarget().getSpan());
         assertEquals(spanOf(positionOf(0, 15), positionOf(0, 16)), usage.getDefinitionJumpTarget().getSpan());
     }
 
     @Test
     public void testMethodCall_explicitThis() throws IOException {
         String code = "class A { void f() { this.f(); } }";
         Usage usage = findUniqueMethodUsageByKind(
                 extractFromCode(code).getUsages(),
                 MethodUsage.Kind.METHOD_CALL);
         assertEquals("A.f", usage.getMethod().getMethod().getName());
         assertEquals(spanOf(positionOf(0, 26), positionOf(0, 27)), usage.getJumpTarget().getSpan());
         assertEquals(spanOf(positionOf(0, 15), positionOf(0, 16)), usage.getDefinitionJumpTarget().getSpan());
     }
 
     @Test
     public void testMethodCall_methodOfField() throws IOException {
         String code = "class A { void f() {} }\n" +
                 "class B { A a; void g() { a.f(); } }\n";
         Usage usage = findUniqueMethodUsageByKind(
                 extractFromCode(code).getUsages(),
                 MethodUsage.Kind.METHOD_CALL);
         assertEquals("A.f", usage.getMethod().getMethod().getName());
         assertEquals(spanOf(positionOf(1, 28), positionOf(1, 29)), usage.getJumpTarget().getSpan());
         assertEquals(spanOf(positionOf(0, 15), positionOf(0, 16)), usage.getDefinitionJumpTarget().getSpan());
     }
 
     @Test
     public void testMethodCall_methodOfParameter() throws IOException {
         String code = "class A { void f() {} }\n" +
                 "class B { void g(A a) { a.f(); } }\n";
         Usage usage = findUniqueMethodUsageByKind(
                 extractFromCode(code).getUsages(),
                 MethodUsage.Kind.METHOD_CALL);
         assertEquals("A.f", usage.getMethod().getMethod().getName());
     }
 
     @Test
     public void testMethodCall_methodOfLocalVariable() throws IOException {
         String code = "class A { void f() {} }\n" +
                 "class B { void g() { A a; a.f(); } }\n";
         Usage usage = findUniqueMethodUsageByKind(
                 extractFromCode(code).getUsages(),
                 MethodUsage.Kind.METHOD_CALL);
         assertEquals("A.f", usage.getMethod().getMethod().getName());
     }
 
     @Test
     public void testMethodCall_staticMethod() throws IOException {
         String code = "class A { static void f(); }\n" +
                 "class B { void g() { A.f(); } }\n";
         Usage usage = findUniqueMethodUsageByKind(
                 extractFromCode(code).getUsages(),
                 MethodUsage.Kind.METHOD_CALL);
         assertEquals("A.f", usage.getMethod().getMethod().getName());
     }
 
     @Test
     public void testMethodCall_overloadByArgumentCount() throws IOException {
         String code = "class A { void f(); void f(int n); }\n" +
                 "class B { void g() { A a; a.f(1); } }\n";
         Usage usage = findUniqueMethodUsageByKind(
                 extractFromCode(code).getUsages(),
                 MethodUsage.Kind.METHOD_CALL);
         assertEquals("A.f", usage.getMethod().getMethod().getName());
         assertEquals(1, usage.getMethod().getMethod().getParametersCount());
     }
 
     @Test
     public void testMethodCall_constructor() throws IOException {
         String code = "class A { A() {} }\n" +
                 "class B { void g() { new A(); } }\n";
         Usage usage = findUniqueMethodUsageByKind(
                 extractFromCode(code).getUsages(),
                 MethodUsage.Kind.INSTANCE_CREATION);
         assertEquals("A.<init>", usage.getMethod().getMethod().getName());
         assertEquals(spanOf(positionOf(1, 25), positionOf(1, 26)), usage.getJumpTarget().getSpan());
         assertEquals(spanOf(positionOf(0, 10), positionOf(0, 11)), usage.getDefinitionJumpTarget().getSpan());
     }
 
     @Test
     public void testTypeInference_newInstance() throws IOException {
         String code = "class A { void f() {} }\n" +
                 "class B { void g() { new A().f(); }}\n";
         Usage usage = findUniqueMethodUsageByKind(
                 extractFromCode(code).getUsages(),
                 MethodUsage.Kind.METHOD_CALL);
         assertEquals("A.f", usage.getMethod().getMethod().getName());
     }
 
     @Test
     public void testTypeInference_chainingCalls() throws IOException {
         String code = "class A { A f() {} A g() {} }\n" +
                 "class B { void g() { A a; a.f().g(); }}\n";
         List<Usage> usages = filterMethodUsagesByKind(
                 extractFromCode(code).getUsages(),
                 MethodUsage.Kind.METHOD_CALL);
         assertEquals(2, usages.size());
     }
 
    @Test
    public void testUsageOfGenericTypes() throws IOException {
        String code = "class List<T> { int size() { return 0; } }\n" +
                "class A {}\n" +
                "class B { void f() { List<A> a; a.size(); } }\n";
        List<Usage> usages = extractFromCode(code).getUsages();
        Usage usage1 = findUniqueTypeUsageByKind(
                usages,
                TypeUsage.Kind.LOCAL_VARIABLE);
        assertEquals("List", usage1.getType().getType().getClazz().getName());
        System.out.println("usage1 = " + usage1);
        Usage usage2 = findUniqueMethodUsageByKind(
                usages,
                MethodUsage.Kind.METHOD_CALL);
        assertEquals("List.size", usage2.getMethod().getMethod().getName());
    }

     // TODO: testMethodCall_staticBlock()
 
     public static SecondPassProcessor.Result extractFromCode(String code) throws IOException {
         FileHandle fakeFile = FileHandle.newBuilder()
                 .setKind(FileHandle.Kind.NORMAL_FILE)
                 .setId(100L)
                 .setProject("project")
                 .setPath("/file")
                 .build();
         FirstPassProcessor.Result result1 = FirstPassProcessor.process(
                 fakeFile,
                 code,
                 ID_GENERATOR);
         SecondPassProcessor.Result result2 = SecondPassProcessor.extract(
                 TEST_PROJECT,
                 fakeFile,
                 code,
                 createSymbolTableAndRegisterClasses(result1.getDiscoveredClasses()),
                 ID_GENERATOR,
                 result1.getPackage());
         List<Usage> result3 = ThirdPassProcessor.extract(
                 fakeFile,
                 code,
                 createSymbolTableAndRegisterClasses(result2.getClassTypes()),
                 result1.getPackage(),
                 ID_GENERATOR);
         result2.getUsages().addAll(result3); // temp hack
         return result2;
     }
 
     private static SymbolTable createSymbolTableAndRegisterClasses(List<ClassType> classes) {
         SymbolTable symbolTable = new SymbolTable();
         for (ClassType clazz : classes) {
             symbolTable.registerClassType(clazz);
         }
         return symbolTable;
     }
 
     public static ClassType extractUniqueTypeFromCode(String code) throws IOException {
         return Iterables.getOnlyElement(extractFromCode(code).getClassTypes());
     }
 
     public static Method extractUniqueMethodFromCode(String code) throws IOException {
         return Iterables.getOnlyElement(extractUniqueTypeFromCode(code).getMethodsList());
     }
 
     public static Variable extractUniqueFieldFromCode(String code) throws IOException {
         return Iterables.getOnlyElement(extractUniqueTypeFromCode(code).getFieldsList());
     }
 
     private List<Usage> filterTypeUsages(List<Usage> usages) {
         List<Usage> result = Lists.newArrayList();
         for (Usage usage : usages) {
             if (Objects.equal(usage.getKind(), Usage.Kind.TYPE)) {
                 result.add(usage);
             }
         }
         return result;
     }
 
     private List<Usage> filterTypeUsagesByKind(List<Usage> usages, TypeUsage.Kind kind) {
         List<Usage> result = Lists.newArrayList();
         for (Usage u : filterTypeUsages(usages)) {
             if (u.getType().getKind() == kind) {
                 result.add(u);
             }
         }
         return result;
     }
 
     private Usage findUniqueTypeUsageByKind(List<Usage> usages, final TypeUsage.Kind kind) {
         return Iterables.getOnlyElement(filterTypeUsagesByKind(usages,  kind));
     }
 
     private List<Usage> filterMethodUsages(List<Usage> usages) {
         List<Usage> result = Lists.newArrayList();
         for (Usage usage : usages) {
             if (usage.getKind() == Usage.Kind.METHOD) {
                 result.add(usage);
             }
         }
         return result;
     }
 
     private List<Usage> filterMethodUsagesByKind(List<Usage> usages, MethodUsage.Kind kind) {
         List<Usage> result = Lists.newArrayList();
         for (Usage u : filterMethodUsages(usages)) {
             if (u.getKind() == Usage.Kind.METHOD && u.getMethod().getKind() == kind) {
                 result.add(u);
             }
         }
         return result;
     }
 
     private Usage findUniqueMethodUsageByKind(List<Usage> usages, MethodUsage.Kind kind) {
         return Iterables.getOnlyElement(filterMethodUsagesByKind(usages, kind));
     }
 
     private List<Usage> filterVariableUsages(List<Usage> usages) {
         List<Usage> result = Lists.newArrayList();
         for (Usage usage : usages) {
             if (usage.getKind() == Usage.Kind.VARIABLE) {
                 result.add(usage);
             }
         }
         return result;
     }
 
     private List<Usage> filterVariableUsagesByKind(List<Usage> usages, VariableUsage.Kind kind) {
         List<Usage> result = Lists.newArrayList();
         for (Usage u : filterVariableUsages(usages)) {
             if (u.getKind() == Usage.Kind.VARIABLE && u.getVariable().getKind() == kind) {
                 result.add(u);
             }
         }
         return result;
     }
 
     private Usage findUniqueVariableUsageByKind(List<Usage> usages, VariableUsage.Kind kind) {
         return Iterables.getOnlyElement(filterVariableUsagesByKind(usages, kind));
     }
 }
