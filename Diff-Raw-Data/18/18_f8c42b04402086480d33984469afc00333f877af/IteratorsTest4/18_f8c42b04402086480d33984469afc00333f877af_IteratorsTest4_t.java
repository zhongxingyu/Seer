 /**
  * <copyright>
  * 
  * Copyright (c) 2006,2011 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   IBM - Initial API and implementation
  *   E.D.Willink - Bug 296409, 297541
  *
  * </copyright>
  *
  * $Id: IteratorsTest.java,v 1.9 2011/05/20 15:27:16 ewillink Exp $
  */
 
 package org.eclipse.ocl.examples.pivot.tests;
 
 import java.math.BigInteger;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.ocl.examples.domain.evaluation.InvalidValueException;
 import org.eclipse.ocl.examples.domain.ids.CollectionTypeId;
 import org.eclipse.ocl.examples.domain.ids.TypeId;
 import org.eclipse.ocl.examples.domain.messages.EvaluatorMessages;
 import org.eclipse.ocl.examples.domain.utilities.DomainUtil;
 import org.eclipse.ocl.examples.domain.values.BagValue;
 import org.eclipse.ocl.examples.domain.values.CollectionValue;
 import org.eclipse.ocl.examples.domain.values.OrderedSetValue;
 import org.eclipse.ocl.examples.domain.values.SequenceValue;
 import org.eclipse.ocl.examples.domain.values.SetValue;
 import org.eclipse.ocl.examples.domain.values.Value;
 import org.eclipse.ocl.examples.domain.values.util.ValuesUtil;
 import org.eclipse.ocl.examples.library.LibraryConstants;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.PivotConstants;
 import org.eclipse.ocl.examples.pivot.PivotTables;
 import org.eclipse.ocl.examples.pivot.Property;
 import org.eclipse.ocl.examples.pivot.Root;
 import org.eclipse.ocl.examples.pivot.SemanticException;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.messages.OCLMessages;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 /**
  * Tests for iterator expressions.
  * 
  * @author Christian W. Damus (cdamus)
  */
 @SuppressWarnings("nls")
 @RunWith(value = Parameterized.class)
 public class IteratorsTest4 extends PivotTestSuite
 {
 	@Parameters
 	public static Collection<Object[]> data() {
 		Object[][] data = new Object[][]{{false}, {true}};
 		return Arrays.asList(data);
 	}
 
 	public IteratorsTest4(boolean useCodeGen) {
 		super(useCodeGen);
 	}
 
 	Root root;
 	org.eclipse.ocl.examples.pivot.Package pkg1;
 	org.eclipse.ocl.examples.pivot.Package pkg2;
 	org.eclipse.ocl.examples.pivot.Package pkg3;
 	org.eclipse.ocl.examples.pivot.Package pkg4;
 	org.eclipse.ocl.examples.pivot.Package pkg5;
 	org.eclipse.ocl.examples.pivot.Package jim;
 	org.eclipse.ocl.examples.pivot.Package bob;
 	org.eclipse.ocl.examples.pivot.Package george;
 
 	@Override
 	protected String getTestPackageName() {
 		return "Iterators";
 	}
 
     @Override
     @Before public void setUp() throws Exception {
         super.setUp();
         PivotTables.LIBRARY.getClass();
 		metaModelManager.addGlobalNamespace(PivotConstants.OCL_NAME, metaModelManager.getPivotMetaModel());
 
         // need a metamodel that has a reflexive EReference.
         // Ecore will do nicely. Create the following structure:
         // pkg1
         // pkg1::pkg2
         // pkg1::pkg2::jim
         // pkg1::bob
         // pkg1::pkg3
         // pkg1::pkg3::pkg4
         // pkg1::pkg3::pkg5
         // pkg1::pkg3::pkg5::george
 
         root = createRoot("model");
         pkg1 = createPackage(root, "pkg1");
         pkg2 = createPackage(pkg1, "pkg2");
         jim = createPackage(pkg2, "jim");
         bob = createPackage(pkg1, "bob");
         pkg3 = createPackage(pkg1, "pkg3");
         pkg4 = createPackage(pkg3, "pkg4");
         pkg5 = createPackage(pkg3, "pkg5");
         george = createPackage(pkg5, "george");
         metaModelManager.installRoot(root);
         helper.setContext(metaModelManager.getPivotType("Package"));
     }
 
 	@Override
 	@After public void tearDown() throws Exception {
 		super.tearDown();
 	}
 
     /**
      * Tests the generic iterate() iterator.
      */
     @Test public void test_iterate_143996() {
     	SetValue expected = metaModelManager.createSetValueOf(null, "pkg2", "bob", "pkg3");
 
         // complete form
         assertQueryEquals(pkg1, expected, "nestedPackage->iterate(p; s : Set(String) = Set{} | s->including(p.name))");
 
         // shorter form
         assertQueryEquals(pkg1, expected, "nestedPackage->iterate(p; s : Set(String) = Set{} | s->including(p.name))");
 
         // shortest form
         assertQueryEquals(pkg1, expected, "nestedPackage->iterate(s : Set(String) = Set{} | s->including(name))");
 
         assertQueryEquals(pkg1, "pfx_a_b_c", "Sequence{'a','b','c'}->iterate(e : String; s : String = 'pfx' | s + '_' + e)");
     }
     
     /**
      * Tests the select() iterator.
      */
 	@Test public void test_select() {
 		CollectionValue expected = metaModelManager.createSetValueOf(null, pkg2, pkg3);
 
         // complete form
         assertQueryEquals(pkg1, expected, "nestedPackage->select(p : ocl::Package | p.name <> 'bob')");
 
         // shorter form
         assertQueryEquals(pkg1, expected, "nestedPackage->select(p | p.name <> 'bob')");
 
         // shortest form
         assertQueryEquals(pkg1, expected, "nestedPackage->select(name <> 'bob')");
 
         Value expected2 = metaModelManager.createSetValueOf(null, bob, pkg2, pkg3);
         assertQueryEquals(pkg1, expected2, "nestedPackage->select(true)");
     }
 
     /**
      * Tests the reject() iterator.
      */
     @Test public void test_reject() {
 		CollectionValue expected = metaModelManager.createSetValueOf(null, pkg2, pkg3);
 
         // complete form
         assertQueryEquals(pkg1, expected, "nestedPackage->reject(p : ocl::Package | p.name = 'bob')");
 
         // shorter form
         assertQueryEquals(pkg1, expected, "nestedPackage->reject(p | p.name = 'bob')");
 
         // shortest form
         assertQueryEquals(pkg1, expected, "nestedPackage->reject(name = 'bob')");
 
         expected = metaModelManager.createSetValueOf(null);
         assertQueryEquals(pkg1, expected, "nestedPackage->reject(true)");
     }
 
     /**
      * Tests the any() iterator.
      */
     @Test public void test_any() {
         // complete form
     	assertQueryEquals(pkg1, bob, "nestedPackage->any(p : ocl::Package | p.name = 'bob')");
 
         // shorter form
     	assertQueryEquals(pkg1, bob, "nestedPackage->any(p | p.name = 'bob')");
 
         // shortest form
     	assertQueryEquals(pkg1, bob, "nestedPackage->any(name = 'bob')");
 
         // negative
         assertQueryNotSame(pkg1, bob, "nestedPackage->any(name = 'pkg2')");
 
 //        assertQueryInvalid(null, "Sequence{}->any(s | s = false)");
         assertQueryNull(null, "Sequence{}->any(s | s = false)");
         assertQueryFalse(null, "Sequence{false}->any(s | s = false)");
         assertQueryFalse(null, "Sequence{false, false}->any(s | s = false)");
 
 //        assertQueryInvalid(null, "Sequence{}->any(s | s = null)");
         assertQueryNull(null, "Sequence{}->any(s | s = null)");
         assertQueryNull(null, "Sequence{null}->any(s | s = null)");
         assertQueryNull(null, "Sequence{null, null}->any(s | s = null)");
         
         
         assertQueryDefined(pkg1, "nestedPackage->any(true)");
         assertQueryNull(pkg1, "nestedPackage->any(false)");
     }
 
     /**
      * Tests the isUnique() iterator.
      */
     @Test public void test_isUnique_126861() {
 //    	Abstract2Moniker.TRACE_MONIKERS.setState(true);
         assertQueryTrue(pkg1, "Sequence{'a', 'b', 'c', 'd', 'e'}->isUnique(e | e)");
 
         assertQueryFalse(pkg1, "Sequence{'a', 'b', 'c', 'c', 'e'}->isUnique(e | e)");
 
         // when there are no values, they implicitly all evaluate to a
         // different result
         assertQueryTrue(pkg1, "Sequence{}->isUnique(e | e)");
 
         assertQueryTrue(pkg1, "nestedPackage->isUnique(name)");
     }
 
     /**
      * Tests the exists() iterator.
      */
     @Test public void test_exists() {
         assertQueryTrue(pkg1, "Sequence{'a', 'b', 'c', 'd', 'e'}->exists(e | e = 'c')");
 
         assertQueryTrue(pkg1, "Sequence{'a', 'b', 'c', 'c', 'e'}->exists(e | e = 'c')");
 
         assertQueryFalse(pkg1, "Sequence{'a', 'b', 'd', 'e'}->exists(e | e = 'c')");
 
         // when there are no values, they the desired result implictly
         // does not occur
         assertQueryFalse(pkg1, "Sequence{}->exists(e | e = 'c')");
 
         assertQueryTrue(pkg1, "nestedPackage->exists(true)");
     }
 
     /**
      * Tests the forAll() iterator.
      */
     @Test public void test_forAll() {
         assertQueryFalse(pkg1, "Sequence{'a', 'b', 'c', 'd', 'e'}->forAll(e | e = 'c')");
 
         assertQueryFalse(pkg1, "Sequence{'a', 'b', 'd', 'e'}->forAll(e | e = 'c')");
 
         assertQueryTrue(pkg1, "Sequence{'c', 'c', 'c', 'c'}->forAll(e | e = 'c')");
 
         assertQueryTrue(pkg1, "Sequence{'c'}->forAll(e | e = 'c')");
 
         // when there are no values, they implicitly all evaluate to the
         // desired result
         assertQueryTrue(pkg1, "Sequence{}->forAll(e | e = 'c')");
 
         assertQueryTrue(pkg1, "nestedPackage->forAll(true)");
         //
         assertQueryTrue(pkg1, "Sequence{1..0}->forAll(false)");
         assertQueryFalse(pkg1, "Sequence{1..1}->forAll(false)");
     }
 
     /**
      * Tests the one() iterator.
      */
     @Test public void test_one() {
         assertQueryTrue(pkg1, "Sequence{'a', 'b', 'c', 'd', 'e'}->one(e | e = 'c')");
 
         assertQueryFalse(pkg1, "Sequence{'a', 'b', 'c', 'c', 'e'}->one(e | e = 'c')");
 
         assertQueryFalse(pkg1, "Sequence{'a', 'b', 'd', 'e'}->one(e | e = 'c')");
 
         assertQueryTrue(pkg1, "Sequence{'a'}->one(true)");
     }
     
     /**
      * Tests the collect() iterator.
      */
     @Test public void test_collect() {
 //    	Abstract2Moniker.TRACE_MONIKERS.setState(true);
         CollectionValue expected1 = metaModelManager.createBagValueOf(null, "pkg2", "bob", "pkg3");
 
         // complete form
         assertQueryEquals(pkg1, expected1, "nestedPackage->collect(p : ocl::Package | p.name)");
 
         // shorter form
         assertQueryEquals(pkg1, expected1, "nestedPackage->collect(p | p.name)");
 
         // yet shorter form
         assertQueryEquals(pkg1, expected1, "nestedPackage->collect(name)");
 
         // shortest form
         assertQueryEquals(pkg1, expected1, "nestedPackage.name");
 
         // flattening of nested collections
         CollectionValue expected2 = metaModelManager.createBagValueOf(null, jim, pkg4, pkg5);
         // nestedPackage is Set<Package>
         // nestedPackage->collectNested(nestedPackage) is Bag<Set<Package>>
         // nestedPackage->collectNested(nestedPackage)->flatten() is Bag<Package>
         assertQueryEquals(pkg1, expected2, "nestedPackage.nestedPackage");
     }
 
 	/**
      * Tests that parsing fails, in the case of an unknown property in a
      * collection navigation, with an appropriate parse failure, not a
      * <code>ClassCastException</code>.
      */
     @Test public void test_implicitCollect_unknownAttribute_232669() {
         assertBadInvariant(SemanticException.class, Diagnostic.ERROR,
     		"nestedPackage.unknownAttribute",
         	OCLMessages.UnresolvedProperty_ERROR_, "unknownAttribute", "Set(Package)");
    }
 
     /**
      * Tests that parsing fails, in the case of an unknown operation in a
      * collection navigation, with an appropriate parse failure, not a
      * <code>ClassCastException</code>.
      */
     @Test public void test_implicitCollect_unknownOperation_232669() {
     	assertBadInvariant(SemanticException.class, Diagnostic.ERROR,
     		"nestedPackage.unknownOperation(self)",
         	OCLMessages.UnresolvedOperationCall_ERROR_, "unknownOperation", "Set(Package)", "Package");
    }
 
     /**
      * Tests that the collect() iterator correctly flattens its result.
      */
     @Test public void test_collect_flattens_217461() {
         String self = "foo";
         SequenceValue expected = ValuesUtil.createSequenceValue(null, "THIS AND", "THAT", "THE OTHER");
 
         assertQueryEquals(self, expected, "Sequence{Sequence{'this and', 'that'}, Sequence{'the other'}}->collect(s : Sequence(String) | s.toUpperCase())");
     }
 
     /**
      * Tests that the collect() iterator correctly deals with empty collections.
      */
     @Test public void test_collect_empty_217461() {
         String self = "foo";
         List<String> expected = Collections.emptyList();
 
         assertQueryEquals(self, expected, "let c : Sequence(OrderedSet(String)) = Sequence{} in c->collect(s : OrderedSet(String) | s.toUpperCase())");
     }
 
     /**
      * Tests the collectNested() iterator.
      */
     @Test public void test_collectNested() {
         CollectionValue expected1 = metaModelManager.createBagValueOf(null, "pkg2", "bob", "pkg3");
 
         // complete form
         assertQueryEquals(pkg1, expected1, "nestedPackage->collectNested(p : ocl::Package | p.name)");
 
         // shorter form
         assertQueryEquals(pkg1, expected1, "nestedPackage->collectNested(p | p.name)");
 
         // shortest form
         assertQueryEquals(pkg1, expected1, "nestedPackage->collectNested(name)");
 
         // nested collections not flattened
 		Set<org.eclipse.ocl.examples.pivot.Package> e1 = Collections.singleton(jim);
         Set<?> e2 = Collections.EMPTY_SET;
         HashSet<Object> e3 = new HashSet<Object>(Arrays.asList(new Object[] {pkg4, pkg5}));
 		CollectionValue expected2 = metaModelManager.createBagValueOf(null, e1, e2, e3);
 
         assertQueryEquals(pkg1, expected2, "nestedPackage->collectNested(nestedPackage)");
     }
 
     /**
      * Tests the sortedBy() iterator.
      */
     @Test public void test_sortedBy() {
         OrderedSetValue expectedSet = ValuesUtil.createOrderedSetValue(null, bob, pkg2, pkg3);
 
         // complete form
         assertQueryEquals(pkg1, expectedSet, "nestedPackage->sortedBy(p : ocl::Package | p.name)");
 
         // shorter form
         assertQueryEquals(pkg1, expectedSet, "nestedPackage->sortedBy(p | p.name)");
 
         // shortest form
         assertQueryEquals(pkg1, expectedSet, "nestedPackage->sortedBy(name)");
 
         SequenceValue expected = ValuesUtil.createSequenceValue(null, "a", "b", "c", "d", "e");
         assertQueryEquals(pkg1, expected, "Bag{'d', 'b', 'e', 'a', 'c'}->sortedBy(e | e)");
         assertQueryResults(null, "Sequence{'x', 'aa', 'zzz', 'zzz', 'zzz', 'yyyy', 'yyyy'}", "Bag{'x', 'yyyy', 'zzz', 'aa', 'zzz', 'yyyy', 'zzz'}->sortedBy(size())");
     }
 
     /**
      * Tests the closure() iterator.
      */
     // pkg1
     // pkg1::pkg2
     // pkg1::pkg2::jim
     // pkg1::bob
     // pkg1::pkg3
     // pkg1::pkg3::pkg4
     // pkg1::pkg3::pkg5
     // pkg1::pkg3::pkg5::george
     @Test public void test_closure() {
     	CollectionValue expected1 = metaModelManager.createSetValueOf(null, pkg1, pkg3, pkg5); // closure does not include self (george)
         assertQueryEquals(george, expected1, "self.oclAsType(Package)->closure(nestingPackage)");
 
         CollectionValue expected2 = metaModelManager.createSetValueOf(null, pkg2, jim, bob, pkg3, pkg4, pkg5, george);
 //        CollectionValue expected2a = metaModelManager.createOrderedSetValue(null, pkg2, jim, bob, pkg3, pkg4, pkg5, george);
         assertQueryEquals(pkg1, expected2, "self.oclAsType(Package)->closure(nestedPackage)");
 // FIXME not a valid test for UML's unordered nested packages
 //        assertQueryEquals(pkg1, expected2a, "self->asSequence()->closure(nestedPackage)");
         assertQueryEquals(pkg1, expected2, "self.oclAsType(Package)->closure(nestedPackage->asSequence())");
 	    SetValue expected3 = metaModelManager.createSetValueOf(null, pkg2, jim, bob, pkg3, pkg4, pkg5, george);
         assertQueryEquals(pkg1, expected3, "self.oclAsType(Package)->asBag()->closure(nestedPackage)");
         assertQueryEquals(pkg1, expected3, "self.oclAsType(Package)->closure(nestedPackage->asBag())");
 
         // empty closure
         CollectionTypeId collectedId = expected1.getTypeId();
 //        @SuppressWarnings("unused") DomainType elementType = collectionType.getElementType();
 		assertQueryEquals(pkg1, metaModelManager.createSetValueOf(collectedId), "self.oclAsType(Package)->closure(nestingPackage)");
 //WIP        assertQueryNotEquals(pkg1, getEmptySetValue(), "self->closure(nestingPackage)");
         // empty closure
         assertQueryEquals(pkg1, getEmptyOrderedSetValue(), "self.oclAsType(Package)->asSequence()->closure(nestingPackage)");
 //WIP 		assertQueryNotEquals(pkg1, metaModelManager.createOrderedSetValue(metaModelManager.getOrderedSetType(elementType)), "self->asSequence()->closure(nestingPackage)");
     }
 
     /**
      * Tests that the closure() iterator handles cycles.
      */
     @Test public void test_closure_cycles() {
         Type packageMetaclass = metaModelManager.getPivotType("Package");
         Property nestedPackage = getAttribute(packageMetaclass, "nestedPackage", packageMetaclass);
         Property nestingPackage = getAttribute(packageMetaclass, "nestingPackage", packageMetaclass);
         SetValue expected = metaModelManager.createSetValueOf(null, nestedPackage, nestingPackage); // cyclic closure *does* include self
         assertQueryEquals(nestingPackage, expected, "self->closure(opposite)");
         assertQueryEquals(nestedPackage, expected, "self->closure(opposite)");
     }
 
     /**
      * Tests parsing the closure of operation calls.
      */
     @Test public void test_closure_operations() {
     	Resource fakeResource = new XMIResourceFactoryImpl().createResource(URI.createURI("fake"));
     	Root fakeRoot = metaModelManager.createRoot("root", null);
     	org.eclipse.ocl.examples.pivot.Package fakePkg = createPackage(fakeRoot, "fake");
     	fakeResource.getContents().add(fakePkg);
         org.eclipse.ocl.examples.pivot.Class fake = createOwnedClass(fakePkg, "Fake", false);
         createGeneralization(fake, metaModelManager.getOclAnyType());
         Operation getFakes = createOwnedOperation(fake, "getFakes", null, null, fake, true);
         getFakes.setType(metaModelManager.getSetType(fake, null, null));
 
         assertQuery(fake, "self->closure(getFakes())");
     }
 
     /**
      * Tests the validation of the closure() iterator.
      *
     @Test public void test_closureValidation() {
         // non-recursive reference
         assertQueryInvalid(pkg1, "self->closure(xyzzy)");
         assertBadQuery(SemanticException.class, Diagnostic.ERROR,
         	"self->closure(ownedType)",
         	OCLMessages.NonStd_Iterator_, "closure");
     } */
 
     /**
      * Tests the validation of the closure() iterator for conformance of the
      * body type with the iterator variable (source element) type.
      */
     @Test public void test_closureValidation_typeConformance_154695() {
     	Resource fakeResource = new XMIResourceFactoryImpl().createResource(URI.createURI("fake"));
     	Root fakeRoot = metaModelManager.createRoot("root", null);
     	org.eclipse.ocl.examples.pivot.Package fakePkg = createPackage(fakeRoot, "fake");
     	fakeResource.getContents().add(fakePkg);
         org.eclipse.ocl.examples.pivot.Class fake = createOwnedClass(fakePkg, "Fake", false);
         @SuppressWarnings("unused")
         Operation getFakes = createOwnedOperation(fake, "getFakes", null, null, metaModelManager.getSetType(fake, null, null), true);
 
         // subclass the Fake class
         org.eclipse.ocl.examples.pivot.Class subFake = createOwnedClass(fakePkg, "Subfake", false);
         createGeneralization(subFake, fake);
         createGeneralization(fake, metaModelManager.getOclAnyType());
 
         // get sub-fakes from a fake
         @SuppressWarnings("unused")
 		Operation getSubFakes = createOwnedOperation(fake, "getSubFakes", null, null, metaModelManager.getSetType(subFake, null, null), true);
  
         helper.setContext(subFake);
 
         // this should not parse because the result of the closure
         // expression
         // is more general than the iterator variable, so cannot be
         // assigned recursively
         assertValidationErrorQuery("self->closure(getFakes())",
         	OCLMessages.IncompatibleBodyType_WARNING_, fake, subFake);
 
         // this should parse OK because the result of the closure expression
         // is more specific than the iterator variable, so it can be
         // assigned recursively
         assertQuery(fake, "self->closure(getSubFakes())");
     }
 
     /**
      * Tests that when the body of an iterator results in invalid, the entire
      * iterator expression's value is invalid.
      */
     @Test public void test_forAll_invalidBody_142518() {
         assertQueryInvalid(EcorePackage.eINSTANCE,
             "let b:Boolean = null in Bag{1, 2, 3}->forAll(b and b)");
 
         // check that the "check" API interprets invalid as a constraint
         // violation
         assertInvariantFalse(EcorePackage.eINSTANCE,
             "let b:Boolean = null in Bag{1}->forAll(b and b)");
 
         // same deal for a null value (in the forAll case)
         assertQueryInvalid(EcorePackage.eINSTANCE,
             "Bag{1, 2, 3}->forAll(null.oclAsType(Boolean))");
     }
 
     /**
      * Tests that when the body of an iterator results in invalid, the entire
      * iterator expression's value is invalid.
      */
     @Test public void test_exists_invalidBody_142518() {
     	assertQueryInvalid(EcorePackage.eINSTANCE,
             "let b:Boolean = null in Bag{1, 2, 3}->exists(b and b)");
 
         // same deal for a null value (in the exists case)
     	assertQueryInvalid(EcorePackage.eINSTANCE,
             "Bag{1, 2, 3}->exists(null.oclAsType(Boolean))");
     }
 
     /**
      * Tests that when the body of an iterator results in invalid, the entire
      * iterator expression's value is invalid.
      */
     @Test public void test_one_invalidBody_142518() {
     	assertQueryInvalid(EcorePackage.eINSTANCE,
             "let b:Boolean = null in Bag{1, 2, 3}->one(b and b)");
 
         // same deal for a null value (in the one case)
         assertQueryInvalid(EcorePackage.eINSTANCE,
             "Bag{1, 2, 3}->one(null.oclAsType(Boolean))");
     }
 
     /**
      * Tests that when the body of an iterator results in invalid, the entire
      * iterator expression's value is invalid.
      */
     @Test public void test_any_invalidBody_142518() {
         assertQueryInvalid(EcorePackage.eINSTANCE,
             "let b:Boolean = null in Bag{1, 2, 3}->any(b and b)");
 
         // same deal for a null value (in the any case)
         assertQueryInvalid(EcorePackage.eINSTANCE,
             "Bag{1, 2, 3}->any(null.oclAsType(Boolean))");
     }
 
     /**
      * Tests that when the body of an iterator results in invalid, the entire
      * iterator expression's value is invalid.
      */
     @Test public void test_select_invalidBody_142518() {
         assertQueryInvalid(EcorePackage.eINSTANCE,
             "let b:Boolean = null in Bag{1, 2, 3}->select(b and b)");
 
         // same deal for a null value (in the exists case)
         assertQueryInvalid(EcorePackage.eINSTANCE,
             "Bag{1, 2, 3}->select(null.oclAsType(Boolean))");
     }
 
     /**
      * Tests that when the body of an iterator results in invalid, the entire
      * iterator expression's value is invalid.
      */
     @Test public void test_reject_invalidBody_142518() {
         assertQueryInvalid(EcorePackage.eINSTANCE,
             "let b:Boolean = null in Bag{1, 2, 3}->reject(b and b)");
 
         // same deal for a null value (in the exists case)
         assertQueryInvalid(EcorePackage.eINSTANCE,
             "Bag{1, 2, 3}->reject(null.oclAsType(Boolean))");
     }
 
     /**
      * Tests that when the body of an iterator results in invalid, the
      * isUnique iterator expression treats it like any other value.
      */
     @Test public void test_isUnique_invalidBody_142518() {
     	assertQueryInvalid(EcorePackage.eINSTANCE,
             "let b:Boolean = null in Bag{1, 2, 3}->isUnique(b and b)");
 
         assertQueryFalse(EcorePackage.eINSTANCE,
             "let b:Boolean = null in Bag{1, 2, 3}->isUnique(null)");
     }
 
     /**
      * Tests that when the body of an iterator results in invalid, the entire
      * iterator expression's value is invalid.
      */
     @Test public void test_collect_invalidBody_142518() {
         assertQueryInvalid(EcorePackage.eINSTANCE,
             "let b:Boolean = null in Bag{1, 2, 3}->collect(b and b)");
 
         // in the case of a null value, null is allowed in a collection, so
         // it does not result in invalid
         BagValue expected = metaModelManager.createBagValueOf(null, getNull(), getNull(), getNull());
         assertQueryEquals(EcorePackage.eINSTANCE, expected,
             "let b:Boolean = null in Bag{1, 2, 3}->collect(null)");
     }
 
     /**
      * Tests that when the body of an iterator results in invalid, the entire
      * iterator expression's value is invalid.
      */
 	@Test public void test_collectNested_invalidBody_142518() {
         assertQueryInvalid(EcorePackage.eINSTANCE,
             "let b:Boolean = null in Bag{1, 2, 3}->collectNested(b and b)");
 
         // in the case of a null value, null is allowed in a collection, so
         // it does not result in invalid
     	Set<BigInteger> e1 = Collections.singleton(BigInteger.valueOf(1));
     	Object e2 = getNull();
     	Set<BigInteger> e3 = Collections.singleton(BigInteger.valueOf(3));
         BagValue expected = metaModelManager.createBagValueOf(null, e1, e2, e3);
         assertQueryEquals(EcorePackage.eINSTANCE, expected,
             "let b:Boolean = null in Bag{1, 2, 3}->collectNested(e | if e = 2 then null else Set{e} endif)");
     }
 
     /**
      * Tests that when the body of an iterator results in invalid, the entire
      * iterator expression's value is invalid.
      */
     @Test public void test_closure_invalidBody_142518() {
         assertQueryInvalid(getUMLMetamodel(),
             "let c : ocl::Type = invalid in ownedType->closure(c)");
 
         // in the case of a null value, null is allowed in a collection, so
         // it does not result in invalid
         assertQueryResults(getUMLMetamodel(), "Set{null}",
     		"let c : Set(ocl::Type) = Set{null} in ownedType->closure(c)");
  
 //        Set<Object> expected = Collections.singleton(getNull());
 //        assertQueryEquals(EcorePackage.eINSTANCE, expected,
 //        	"let c : Set(ocl::Type) = Set{null} in ownedType->closure(c)");
     }
 
 	/**
      * Tests that when the body of an iterator results in invalid, the entire
      * iterator expression's value is invalid.
      */
     @Test public void test_sortedBy_invalidBody_142518() {
         assertQueryInvalid(EcorePackage.eINSTANCE,
             "let s : String = null in Bag{1, 2, 3}->sortedBy(s.size())", DomainUtil.bind(EvaluatorMessages.TypedValueRequired, TypeId.STRING_NAME, ValuesUtil.getTypeName(null)), InvalidValueException.class);
 
         // same deal for null values
         assertQueryInvalid(EcorePackage.eINSTANCE,
             "Bag{1, 2, 3}->sortedBy(null.oclAsType(Integer))", DomainUtil.bind(EvaluatorMessages.UndefinedBody, "sortedBy"), InvalidValueException.class);
 
     }
 
     /**
      * Tests that the generic iterate() iterator returns invalid when the
      * source collection is null or invalid.
      */
     @Test public void test_iterateWithNullSource_143996() {
         assertQueryInvalid(pkg1,
             "let e : Collection(ocl::Package) = null in e->iterate(" +
                "p : ocl::Package; s : String = '' | s.concat(p.name))", DomainUtil.bind(EvaluatorMessages.TypedValueRequired, TypeId.COLLECTION_NAME, ValuesUtil.getTypeName(null)), InvalidValueException.class);
 
         assertQueryInvalid(pkg1,
             "let e : Collection(ocl::Package) = invalid in e->iterate(" +
                "p : ocl::Package; s : String = '' | s.concat(p.name))", EvaluatorMessages.InvalidLiteral, InvalidValueException.class);
     }
 
     /**
      * Tests that the exists() iterator return invalid when the source
      * collection is null or invalid.
      */
     @Test public void test_existsWithNullSource_143996() {
     	assertQueryInvalid(pkg1,
             "let e : Collection(ocl::Package) = null in e->exists(" +
                "p : ocl::Package | p.name = 'bob')", DomainUtil.bind(EvaluatorMessages.TypedValueRequired, TypeId.COLLECTION_NAME, ValuesUtil.getTypeName(null)), InvalidValueException.class);
 
     	assertQueryInvalid(pkg1,
             "let e : Collection(ocl::Package) = invalid in e->exists(" +
                "p : ocl::Package | p.name = 'bob')", EvaluatorMessages.InvalidLiteral, InvalidValueException.class);
     }
 
     /**
      * Tests the exists iterator with multiple iterator variables.
      */
     @Test public void test_exists_multipleIteratorVariables() {
         assertInvariantTrue(pkg1, "Sequence{1, 2, 3, 4}->exists(e1, e2 | e1 = e2)");
 
         assertInvariantFalse(pkg1, "Sequence{1, 2, 3, 4}->exists(e1, e2 | (e1 + e2) = 0)");
 
         // when there are no values, the the desired result implictly
         // does not occur
         assertInvariantFalse(pkg1, "Sequence{}->exists(e1, e2 | e1 = e2)");
     }
 
     /**
      * Tests the forAll iterator with multiple iterator variables.
      */
     @Test public void test_forAll_multipleIteratorVariables() {
     	assertInvariantFalse(pkg1, "Sequence{1, 2, 3, 4}->forAll(e1, e2 | e1 = e2)");
 
         assertInvariantTrue(pkg1, "Sequence{1, 2, 3, 4}->forAll(e1, e2 | (e1 + e2) > e1)");
 
 	    // when there are no values, the the desired result implictly occurs
 	    assertInvariantTrue(pkg1, "Sequence{}->forAll(e1, e2 | e1 = e2)");
     }
 
     /**
      * Tests the validation the number of iterator variables for iterators that
      * do not support multiple variables.
      */
     @Test public void test_invalidMultipleIteratorVariables() {
         assertBadQuery(SemanticException.class, Diagnostic.ERROR,		// FIXME Bug 296990
         	"Sequence{'a', 'b', 'c'}->exists(e1, e2, e3 | e1 = e2)",
         	OCLMessages.UnresolvedOperationCall_ERROR_, "exists", "Sequence(String)", "e1, e2, e3| e1 = e2");
 
         assertBadQuery(SemanticException.class, Diagnostic.ERROR,		// FIXME Bug 296990
         	"Sequence{'a', 'b', 'c'}->forAll(e1, e2, e3 | e1 = e2)",
         	OCLMessages.UnresolvedOperationCall_ERROR_, "forAll", "Sequence(String)", "e1, e2, e3| e1 = e2");
 
         assertBadQuery(SemanticException.class, Diagnostic.ERROR,
         	"Sequence{'a', 'b', 'c'}->collect(e1, e2 | Tuple{a : String = e1, b : String = e2})",
         	OCLMessages.UnresolvedOperationCall_ERROR_, "collect", "Sequence(String)", "e1, e2| Tuple{a : String = e1, b : String = e2}");
 
         assertBadQuery(SemanticException.class, Diagnostic.ERROR,
         	"Sequence{'a', 'b', 'c'}->any(e1, e2 | e1 = e2)",
         	OCLMessages.UnresolvedOperationCall_ERROR_, "any", "Sequence(String)", "e1, e2| e1 = e2");
 
         assertBadQuery(SemanticException.class, Diagnostic.ERROR,
         	"Sequence{'a', 'b', 'c'}->one(e1, e2 | e1 = e2)",
         	OCLMessages.UnresolvedOperationCall_ERROR_, "one", "Sequence(String)", "e1, e2| e1 = e2");
 
         assertBadQuery(SemanticException.class, Diagnostic.ERROR,
         	"Sequence{'a', 'b', 'c'}->select(e1, e2 | e1 = e2)",
         	OCLMessages.UnresolvedOperationCall_ERROR_, "select", "Sequence(String)", "e1, e2| e1 = e2");
 
         assertBadQuery(SemanticException.class, Diagnostic.ERROR,
         	"Sequence{'a', 'b', 'c'}->reject(e1, e2 | e1 = e2)",
         	OCLMessages.UnresolvedOperationCall_ERROR_, "reject", "Sequence(String)", "e1, e2| e1 = e2");
 
         assertBadQuery(SemanticException.class, Diagnostic.ERROR,
         	"Sequence{'a', 'b', 'c'}->isUnique(e1, e2 | e1 = e2)",
         	OCLMessages.UnresolvedOperationCall_ERROR_, "isUnique", "Sequence(String)", "e1, e2| e1 = e2");
     }
 
 	/**
      * Test to check the validation of the <tt>sortedBy</tt> iterator, that
      * the body expression type has a <tt>&lt;</tt> operation.
      */
     @Test public void test_sortedByRequiresComparability_192729() {
     	Type context = metaModelManager.getPivotType("Package");
     	Type type = metaModelManager.getPivotType("Type");
      	assertValidationErrorQuery("ownedType->sortedBy(e | e)",
         	OCLMessages.UnresolvedOperation_ERROR_, LibraryConstants.COMPARE_TO, type + "");
        
     	assertQuery(context, "ownedType->sortedBy(e | e.name)");
     	loadEPackage("ecore", EcorePackage.eINSTANCE);
         
         // EDate defines an OclComparable::compareTo by having a java.lang.Comparable instance class
         assertQuery(context, "let dates : Sequence(ecore::EDate) = Sequence{} in dates->sortedBy(e | e)");
         // EInt defines OclComparable::compareTo by having a behavioral mapping to the Integer type
         assertQueryResults(context, "Sequence{1,7,9}", "let values : Sequence(ecore::EInt) = Sequence{1,9,7} in values->sortedBy(e | e)");
     }
 }
