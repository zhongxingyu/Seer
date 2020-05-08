 /**
  * Copyright (C) 2012 Future Invent Informationsmanagement GmbH. All rights
  * reserved. <http://www.fuin.org/>
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 3 of the License, or (at your option) any
  * later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.fuin.srcmixins4j.core;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.emftext.language.java.annotations.AnnotationInstance;
 import org.emftext.language.java.classifiers.Annotation;
 import org.emftext.language.java.classifiers.Class;
 import org.emftext.language.java.classifiers.Classifier;
 import org.emftext.language.java.classifiers.ConcreteClassifier;
 import org.emftext.language.java.classifiers.Interface;
 import org.emftext.language.java.containers.CompilationUnit;
 import org.emftext.language.java.generics.TypeParameter;
 import org.emftext.language.java.members.Field;
 import org.emftext.language.java.members.Member;
 import org.emftext.language.java.members.MemberContainer;
 import org.emftext.language.java.members.Method;
 import org.emftext.language.java.parameters.Parameter;
 import org.emftext.language.java.resource.JaMoPPUtil;
 import org.emftext.language.java.types.NamespaceClassifierReference;
 import org.emftext.language.java.types.Type;
 import org.fuin.srcmixins4j.annotations.MixinGenerated;
 import org.fuin.srcmixins4j.annotations.MixinProvider;
 import org.fuin.srcmixins4j.core.SrcMixins4JUtils.TypeParam2Type;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * Test for the {@link SrcMixins4JUtils} class.
  */
 // CHECKSTYLE:OFF
 public final class SrcMixins4JUtilsTest extends AbstractSrcMixins4JTest {
 
     private static File RES_DIR = new File("src/test/resources");
 
     private static ResourceSet resourceSet;
 
     private static File TEST_CLASS_A = new File(RES_DIR, "TestClassA.java");
     private static Class testClassA;
 
     // No generics
 
     private static File TEST_INTF = new File(RES_DIR, "TestIntf.java");
     private static ConcreteClassifier testIntfClassifier;
 
     private static File TEST_MIXIN_INTF = new File(RES_DIR,
             "TestMixinIntf.java");
     private static Interface testMixinIntfClassifier;
 
     private static File TEST_MIXIN_PROVIDER = new File(RES_DIR,
             "TestMixinProvider.java");
     private static Class testMixinProviderClassifier;
 
     private static File TEST_MIXIN_USER = new File(RES_DIR,
             "TestMixinUser.java");
     private static Class testMixinUserClassifier;
 
     private static File TEST_MIXIN_USER2 = new File(RES_DIR,
             "TestMixinUser2.java");
     private static Class testMixinUser2Classifier;
 
     // Generics
 
     private static File TEST_GENERIC_INTF = new File(RES_DIR,
             "TestGenericIntf.java");
     private static ConcreteClassifier testGenericIntfClassifier;
 
     private static File TEST_GENERIC_MIXIN_INTF = new File(RES_DIR,
             "TestGenericMixinIntf.java");
     private static Interface testGenericMixinIntfClassifier;
 
     private static File TEST_GENERIC_MIXIN_PROVIDER = new File(RES_DIR,
             "TestGenericMixinProvider.java");
     private static Class testGenericMixinProviderClassifier;
 
     private static File TEST_GENERIC_MIXIN_USER = new File(RES_DIR,
             "TestGenericMixinUser.java");
     private static Class testGenericMixinUserClassifier;
 
     private static File TEST_GENERIC_MIXIN_USER2 = new File(RES_DIR,
             "TestGenericMixinUser2.java");
     private static Class testGenericMixinUser2Classifier;
 
     @BeforeClass
     public static void beforeClass() throws IOException {
 
         JaMoPPUtil.initialize();
         resourceSet = createResourceSet(RES_DIR);
 
         testIntfClassifier = loadClassifier(resourceSet, TEST_INTF);
         testMixinIntfClassifier = loadClassifier(resourceSet, TEST_MIXIN_INTF);
         testMixinProviderClassifier = loadClassifier(resourceSet, TEST_MIXIN_PROVIDER);
         testMixinUserClassifier = loadClassifier(resourceSet, TEST_MIXIN_USER);
         testMixinUser2Classifier = loadClassifier(resourceSet, TEST_MIXIN_USER2);
 
         testGenericIntfClassifier = loadClassifier(resourceSet, TEST_GENERIC_INTF);
         testGenericMixinIntfClassifier = loadClassifier(resourceSet, TEST_GENERIC_MIXIN_INTF);
         testGenericMixinProviderClassifier = loadClassifier(resourceSet, TEST_GENERIC_MIXIN_PROVIDER);
         testGenericMixinUserClassifier = loadClassifier(resourceSet, TEST_GENERIC_MIXIN_USER);
         testGenericMixinUser2Classifier = loadClassifier(resourceSet, TEST_GENERIC_MIXIN_USER2);
 
         testClassA = loadClassifier(resourceSet, TEST_CLASS_A);
 
     }
 
     @AfterClass
     public static void afterClass() {
         resourceSet = null;
     }
 
     private static Resource loadResource(final ResourceSet set, final File file) throws IOException {
         return loadResource(set, URI.createFileURI(file.getCanonicalPath()));
     }
 
     private static Resource loadResource(final ResourceSet set, final URI uri) throws IOException {
         final Resource resource = set.getResource(uri, true);
         assertThat(resource).isNotNull();
         return resource;
     }
 
     private static File saveToTempFile(final Resource res) {
         try {
             final File tmpFile = File.createTempFile("tmp-", ".java");
             tmpFile.deleteOnExit();
             final FileOutputStream fos = new FileOutputStream(tmpFile);
             try {
                 testGenericMixinUserClassifier.eResource().save(fos, null);
                 return tmpFile;
             } finally {
                 fos.close();
             }
         } catch (final IOException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     @SuppressWarnings("unchecked")
     private static <T extends Classifier> T loadClassifier(final ResourceSet set, final File file)
             throws IOException {
 
         final Resource resource = loadResource(set, file);
         final CompilationUnit compilationUnit = SrcMixins4JUtils
                 .getCompilationUnit(resource);
         assertThat(compilationUnit).isNotNull();
         assertThat(compilationUnit.getClassifiers()).hasSize(1);
         return (T) compilationUnit.getClassifiers().get(0);
     }
 
     @Test
     public final void testGetMixinInterfaces() {
 
         // TEST
         final List<Interface> intfs = SrcMixins4JUtils
                 .getMixinInterfaces(testMixinUserClassifier);
 
         // VERIFY
         assertThat(intfs).hasSize(1);
         assertThat(SrcMixins4JUtils.getFullQualifiedName(intfs.get(0)))
                 .isEqualTo("a.b.c.TestMixinIntf");
 
     }
 
     @Test
     public final void testGetAnnotationInstance() {
 
         // TEST
         final AnnotationInstance ai = SrcMixins4JUtils.getAnnotationInstance(
                 testMixinProviderClassifier, MixinProvider.class.getName());
 
         // VERIFY
         assertThat(ai).isNotNull();
         assertThat(SrcMixins4JUtils.getFullQualifiedName(ai.getAnnotation()))
                 .isEqualTo(MixinProvider.class.getName());
 
     }
 
     @Test
     public final void testGetSingleAnnotationInterfaceParameter() {
 
         // PREPARE
         final AnnotationInstance ai = SrcMixins4JUtils.getAnnotationInstance(
                 testMixinProviderClassifier, MixinProvider.class.getName());
 
         // TEST
         final Interface intf = SrcMixins4JUtils
                 .getSingleAnnotationInterfaceParameter(ai);
 
         // VERIFY
         assertThat(intf).isNotNull();
         assertThat(SrcMixins4JUtils.getFullQualifiedName(intf)).isEqualTo(
                 "a.b.c.TestMixinIntf");
 
     }
 
     @Test
     public final void testGetMixinInterface() {
 
         // TEST
         final Interface intf = SrcMixins4JUtils
                 .getMixinInterface(testMixinProviderClassifier);
 
         // VERIFY
         assertThat(intf).isNotNull();
         assertThat(SrcMixins4JUtils.getFullQualifiedName(intf)).isEqualTo(
                 "a.b.c.TestMixinIntf");
 
     }
 
     @Test
     public final void testApplyAndRemoveMixin() {
 
         // PREPARE
         final Annotation annotation = (Annotation) testMixinUserClassifier
                 .getConcreteClassifier(MixinGenerated.class.getName());
         final CompilationUnit cu = testMixinUserClassifier
                 .getContainingCompilationUnit();
         assertThat(SrcMixins4JUtils.containsClassifierImport(cu, annotation))
                 .isFalse();
         assertThat(testMixinUserClassifier.getFields()).isEmpty();
         assertThat(testMixinUserClassifier.getMethods()).hasSize(1);
 
         // TEST APPLY
         SrcMixins4JUtils.applyMixin(testMixinUserClassifier,
                 testMixinProviderClassifier, testMixinIntfClassifier);
 
         // VERIFY APPLY
 
         assertThat(SrcMixins4JUtils.containsClassifierImport(cu, annotation))
                 .isTrue();
         assertThat(testMixinUserClassifier.getFields()).hasSize(1);
         assertThat(testMixinUserClassifier.getFields().get(0).getName())
                 .isEqualTo("xyz");
         assertThat(testMixinUserClassifier.getMethods()).hasSize(2);
         assertThat(testMixinUserClassifier.getMethods().get(0).getName())
                 .isEqualTo("sayWow");
         final Method getXyzMethod = testMixinUserClassifier.getMethods().get(1);
         assertThat(getXyzMethod.getName()).isEqualTo("getXyz");
         assertThat(getXyzMethod.getTypeReference()).isNotNull();
         assertThat(getXyzMethod.getTypeReference().getTarget()).isInstanceOf(
                 Classifier.class);
         assertThat(
                 SrcMixins4JUtils.getFullQualifiedName((Classifier) getXyzMethod
                         .getTypeReference().getTarget())).isEqualTo(
                 String.class.getName());
 
         // TEST REMOVE
         SrcMixins4JUtils.removeAllMixinMembers(testMixinUserClassifier);
 
         // VERIFY REMOVE
         assertThat(SrcMixins4JUtils.containsClassifierImport(cu, annotation))
                 .isFalse();
         assertThat(testMixinUserClassifier.getFields()).isEmpty();
         assertThat(testMixinUserClassifier.getMethods()).hasSize(1);
         assertThat(testMixinUserClassifier.getMethods().get(0).getName())
                 .isEqualTo("sayWow");
 
     }
 
     private void assertHasFieldWithoutMixinGeneratedAnnotation(
             final MemberContainer memberContainer, final String fieldName) {
         assertThat(memberContainer.getFields()).hasSize(1);
         final Field field = memberContainer.getFields().get(0);
         assertThat(field.getName()).isEqualTo(fieldName);
         assertThat(
                 SrcMixins4JUtils.getAnnotationInstance(field,
                         MixinGenerated.class.getName())).isNull();
     }
 
     private void assertHasMethodWithoutMixinGeneratedAnnotation(
             final MemberContainer memberContainer, final String methodName) {
         assertThat(memberContainer.getMethods()).hasSize(1);
         final Method method = memberContainer.getMethods().get(0);
         assertThat(method.getName()).isEqualTo(methodName);
         assertThat(
                 SrcMixins4JUtils.getAnnotationInstance(method,
                         MixinGenerated.class.getName())).isNull();
     }
 
     @Test
     public final void testApplyAndRemoveMixinWithOverriddenFieldAndMethod() {
 
         // PREPARE
         final Annotation annotation = (Annotation) testMixinUser2Classifier
                 .getConcreteClassifier(MixinGenerated.class.getName());
         final CompilationUnit cu = testMixinUser2Classifier
                 .getContainingCompilationUnit();
 
         assertThat(SrcMixins4JUtils.containsClassifierImport(cu, annotation))
                 .isFalse();
         assertHasFieldWithoutMixinGeneratedAnnotation(testMixinUser2Classifier,
                 "xyz");
         assertHasMethodWithoutMixinGeneratedAnnotation(
                 testMixinUser2Classifier, "getXyz");
 
         // TEST APPLY
         SrcMixins4JUtils.applyMixin(testMixinUser2Classifier,
                 testMixinProviderClassifier, testMixinIntfClassifier);
 
         // VERIFY nothing changed
         assertThat(SrcMixins4JUtils.containsClassifierImport(cu, annotation))
                 .isFalse();
         assertHasFieldWithoutMixinGeneratedAnnotation(testMixinUser2Classifier,
                 "xyz");
         assertHasMethodWithoutMixinGeneratedAnnotation(
                 testMixinUser2Classifier, "getXyz");
 
         // TEST REMOVE
         SrcMixins4JUtils.removeAllMixinMembers(testMixinUser2Classifier);
 
         // VERIFY nothing changed
         assertThat(SrcMixins4JUtils.containsClassifierImport(cu, annotation))
                 .isFalse();
         assertHasFieldWithoutMixinGeneratedAnnotation(testMixinUser2Classifier,
                 "xyz");
         assertHasMethodWithoutMixinGeneratedAnnotation(
                 testMixinUser2Classifier, "getXyz");
 
     }
 
     @Test
     public final void testContainsClassifierImport() throws IOException {
 
         // PREPARE
         final ConcreteClassifier concreteClassifier = testMixinProviderClassifier
                 .getConcreteClassifier(MixinProvider.class.getName());
         assertThat(concreteClassifier).isNotNull();
         assertThat(concreteClassifier.eIsProxy()).isFalse();
 
         final Resource resource = loadResource(resourceSet, TEST_MIXIN_PROVIDER);
         final CompilationUnit compilationUnit = SrcMixins4JUtils
                 .getCompilationUnit(resource);
 
         // TEST
         assertThat(SrcMixins4JUtils.containsClassifierImport(compilationUnit,
                 concreteClassifier));
 
     }
 
     @Test
     public final void testAddRemoveClassifierImport() throws IOException {
 
         // PREPARE
         final Class stringClass = testIntfClassifier.getStringClass();
         assertThat(stringClass).isNotNull();
         assertThat(stringClass.eIsProxy()).isFalse();
         final Resource resource = loadResource(resourceSet, TEST_INTF);
         final CompilationUnit compilationUnit = SrcMixins4JUtils
                 .getCompilationUnit(resource);
         assertThat(
                 SrcMixins4JUtils.containsClassifierImport(compilationUnit,
                         stringClass)).isFalse();
 
         // TEST ADD
         SrcMixins4JUtils.addClassifierImport(compilationUnit, stringClass);
         assertThat(
                 SrcMixins4JUtils.containsClassifierImport(compilationUnit,
                         stringClass)).isTrue();
 
         // TEST REMOVE
         SrcMixins4JUtils.removeClassifierImport(compilationUnit, stringClass);
         assertThat(
                 SrcMixins4JUtils.containsClassifierImport(compilationUnit,
                         stringClass)).isFalse();
 
     }
 
     @Test
     public final void testGetFullQualifiedName() throws IOException {
 
         assertThat(
                 SrcMixins4JUtils
                         .getFullQualifiedName(testMixinProviderClassifier))
                 .isEqualTo("a.b.c.TestMixinProvider");
 
     }
 
     @Test
     public final void findMethodBySignature() {
 
         // PREPARE
         final Method methodA = (Method) testClassA.getMembersByName("methodA")
                 .get(0);
         final Method methodB = (Method) testClassA.getMembersByName("methodB")
                 .get(0);
 
         // TEST
         assertThat(
                 SrcMixins4JUtils.findMethodBySignature(testClassA, "methodA",
                         methodB.getParameters())).isSameAs(methodA);
         assertThat(
                 SrcMixins4JUtils.findMethodBySignature(testClassA, "methodB",
                         methodA.getParameters())).isSameAs(methodB);
 
     }
 
     @Test
     public final void testFindFieldByName() {
 
     }
 
     @Test
     public final void testSameParameters() {
 
         // PREPARE
         final Method methodA = (Method) testClassA.getMembersByName("methodA")
                 .get(0);
         final Method methodB = (Method) testClassA.getMembersByName("methodB")
                 .get(0);
         final Method methodC = (Method) testClassA.getMembersByName("methodC")
                 .get(0);
         final Method methodD = (Method) testClassA.getMembersByName("methodD")
                 .get(0);
 
         // TEST
         assertThat(
                 SrcMixins4JUtils.sameParameters(methodA.getParameters(),
                         methodB.getParameters())).isTrue();
         assertThat(
                 SrcMixins4JUtils.sameParameters(methodA.getParameters(),
                         methodC.getParameters())).isFalse();
         assertThat(
                 SrcMixins4JUtils.sameParameters(methodA.getParameters(),
                         methodD.getParameters())).isFalse();
         assertThat(
                 SrcMixins4JUtils.sameParameters(methodC.getParameters(),
                         methodD.getParameters())).isFalse();
 
     }
 
     @Test
     public final void testSameType() {
 
         // PREPARE
         final Method methodA = (Method) testClassA.getMembersByName("methodA")
                 .get(0);
 
         final Type[] typesA = new Type[4];
         typesA[0] = methodA.getParameters().get(0).getTypeReference()
                 .getTarget();
         typesA[1] = methodA.getParameters().get(1).getTypeReference()
                 .getTarget();
         typesA[2] = methodA.getParameters().get(2).getTypeReference()
                 .getTarget();
         typesA[3] = methodA.getParameters().get(3).getTypeReference()
                 .getTarget();
 
         final Method methodB = (Method) testClassA.getMembersByName("methodB")
                 .get(0);
         final Type[] typesB = new Type[4];
         typesB[0] = methodB.getParameters().get(0).getTypeReference()
                 .getTarget();
         typesB[1] = methodB.getParameters().get(1).getTypeReference()
                 .getTarget();
         typesB[2] = methodB.getParameters().get(2).getTypeReference()
                 .getTarget();
         typesB[3] = methodB.getParameters().get(3).getTypeReference()
                 .getTarget();
 
         // TEST
         for (int i = 0; i < typesA.length; i++) {
             for (int j = 0; j < typesA.length; j++) {
                 if (i == j) {
                     assertThat(SrcMixins4JUtils.sameType(typesA[i], typesA[j]))
                             .isTrue();
                     assertThat(SrcMixins4JUtils.sameType(typesB[i], typesB[j]))
                             .isTrue();
                     assertThat(SrcMixins4JUtils.sameType(typesA[i], typesB[j]))
                             .isTrue();
                     assertThat(SrcMixins4JUtils.sameType(typesB[i], typesA[j]))
                             .isTrue();
                 } else {
                     assertThat(SrcMixins4JUtils.sameType(typesA[i], typesA[j]))
                             .isFalse();
                     assertThat(SrcMixins4JUtils.sameType(typesB[i], typesB[j]))
                             .isFalse();
                     assertThat(SrcMixins4JUtils.sameType(typesA[i], typesB[j]))
                             .isFalse();
                     assertThat(SrcMixins4JUtils.sameType(typesB[i], typesA[j]))
                             .isFalse();
                 }
             }
         }
 
     }
 
     @Test
     public void testCreateTypeParam2ArgMapping() {
 
         // TEST
         final List<TypeParam2Type> typeParam2ArgList = SrcMixins4JUtils
                 .createTypeParam2ArgMapping(testGenericMixinIntfClassifier,
                         testGenericMixinUserClassifier);
 
         // VERIFY
         assertThat(typeParam2ArgList).hasSize(2);
 
         assertThat(typeParam2ArgList.get(0).getParam().getName())
                 .isEqualTo("A");
         assertThat(typeParam2ArgList.get(0).getArg()).isNull();
         assertThat(typeParam2ArgList.get(0).isParamReplaced()).isFalse();
 
         assertThat(typeParam2ArgList.get(1).getParam().getName())
                 .isEqualTo("B");
         assertThat(typeParam2ArgList.get(1).getArg()).isNotNull();
         assertThat(typeParam2ArgList.get(1).isParamReplaced()).isTrue();
         assertThat(typeParam2ArgList.get(1).getArgType()).isInstanceOf(
                 Class.class);
         final Class clasz = (Class) typeParam2ArgList.get(1).getArgType();
         assertThat(SrcMixins4JUtils.getFullQualifiedName(clasz)).isEqualTo(
                 ArrayList.class.getName());
 
     }
 
     @Test
     public void testReplaceGenericsChanged() throws IOException {
 
         // PREPARE
         final List<TypeParam2Type> typeParam2ArgList = SrcMixins4JUtils
                 .createTypeParam2ArgMapping(testGenericMixinIntfClassifier,
                         testGenericMixinUserClassifier);
 
         final EList<Member> addBMembers = testGenericMixinProviderClassifier
                 .getMembersByName("add");
         final Method addB = EcoreUtil.copy((Method) addBMembers.get(0));
 
         // TEST
         SrcMixins4JUtils.replaceGenerics(addB, typeParam2ArgList);
 
         // VERIFY
         assertThat(addB.getName()).isEqualTo("add");
         assertThat(addB.getParameters()).hasSize(1);
         final Parameter param0 = addB.getParameters().get(0);
         assertThat(param0).isNotNull();
         assertThat(param0.getTypeReference()).isNotNull();
         final Type target = param0.getTypeReference().getTarget();
         assertThat(target).isNotNull();
         assertThat(target).isInstanceOf(ConcreteClassifier.class);
         final ConcreteClassifier cc = (ConcreteClassifier) target;
         assertThat(cc.getName()).isEqualTo(ArrayList.class.getSimpleName());
 
     }
 
     @Test
     public void testReplaceGenericsUnchanged() {
 
         // PREPARE
         final List<TypeParam2Type> typeParam2ArgList = SrcMixins4JUtils
                 .createTypeParam2ArgMapping(testGenericMixinIntfClassifier,
                         testGenericMixinUserClassifier);
 
         final EList<Member> getAMembers = testGenericMixinProviderClassifier
                 .getMembersByName("getA");
         final Method getA = EcoreUtil.copy((Method) getAMembers.get(0));
 
         // TEST
         SrcMixins4JUtils.replaceGenerics(getA, typeParam2ArgList);
 
         // VERIFY
         assertThat(getA.getName()).isEqualTo("getA");
         assertThat(getA.getTypeReference()).isInstanceOf(
                 NamespaceClassifierReference.class);
         final NamespaceClassifierReference ncr = (NamespaceClassifierReference) getA
                 .getTypeReference();
         assertThat(ncr.getTarget()).isInstanceOf(TypeParameter.class);
         final TypeParameter tp = (TypeParameter) ncr.getTarget();
         assertThat(tp.getName()).isEqualTo("A");
 
     }
 
     @Test
     public void testFindMapping() {
 
         // PREPARE
         final List<TypeParam2Type> typeParam2ArgList = SrcMixins4JUtils
                 .createTypeParam2ArgMapping(testGenericMixinIntfClassifier,
                         testGenericMixinUserClassifier);
 
         // TEST & VERIFY
         assertThat(SrcMixins4JUtils.findMapping("A", typeParam2ArgList))
                 .isNotNull();
         assertThat(SrcMixins4JUtils.findMapping("B", typeParam2ArgList))
                 .isNotNull();
         assertThat(SrcMixins4JUtils.findMapping("C", typeParam2ArgList))
                 .isNull();
 
     }
 
     @Test
     public final void testApplyGenericMixin() throws IOException {
 
         // PREPARE
         final Annotation annotation = (Annotation) testGenericMixinUserClassifier
                 .getConcreteClassifier(MixinGenerated.class.getName());
         final CompilationUnit cu = testGenericMixinUserClassifier
                 .getContainingCompilationUnit();
         assertThat(SrcMixins4JUtils.containsClassifierImport(cu, annotation))
                 .isFalse();
         assertThat(testGenericMixinUserClassifier.getFields()).isEmpty();
         assertThat(testGenericMixinUserClassifier.getMethods()).isEmpty();
 
         SrcMixins4JUtils.applyMixin(testGenericMixinUserClassifier,
                 testGenericMixinProviderClassifier,
                 testGenericMixinIntfClassifier);
 
         final File expected = new File(
                 "src/test/TestApplyGenericMixinResult.java");
 
         // TEST
         final File result = saveToTempFile(testGenericMixinUserClassifier
                 .eResource());
 
         // VERIFY
         assertThat(result).hasSameContentAs(expected);
 
     }
 
     @Test
     public final void testFindMixinProviders() {
 
         // TEST
         final List<Class> mixinProviders = SrcMixins4JUtils
                 .findMixinProviders(resourceSet);
 
         // VERIFY
         assertThat(mixinProviders).isNotNull();
         assertThat(mixinProviders).hasSize(2);
         assertThat(mixinProviders.get(0).getName()).isEqualTo(
                 "TestMixinProvider");
         assertThat(mixinProviders.get(1).getName()).isEqualTo(
                 "TestGenericMixinProvider");
 
     }
 
     @Test
     public final void testFindImplementors() {
 
         // TEST
         final List<Class> implementors = SrcMixins4JUtils.findImplementors(
                 resourceSet, testMixinIntfClassifier);
 
         // VERIFY
         assertThat(implementors).isNotNull();
         assertThat(implementors).hasSize(2);
         assertThat(implementors.get(0).getName()).isEqualTo("TestMixinUser");
         assertThat(implementors.get(1).getName()).isEqualTo("TestMixinUser2");
 
     }
 
 }
 // CHECKSTYLE:ON
