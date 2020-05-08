 package eu.execom.testutil;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 import eu.execom.testutil.enums.ReferenceCheckType;
 import eu.execom.testutil.graph.NodesList;
 import eu.execom.testutil.model.A;
 import eu.execom.testutil.model.B;
 import eu.execom.testutil.model.C;
 import eu.execom.testutil.model.EntityTierOneType;
 import eu.execom.testutil.model.EntityTierTwoType;
 import eu.execom.testutil.model.IgnoredType;
 import eu.execom.testutil.model.NoGetMethodsType;
 import eu.execom.testutil.model.TierFiveType;
 import eu.execom.testutil.model.TierFourType;
 import eu.execom.testutil.model.TierOneType;
 import eu.execom.testutil.model.TierSixType;
 import eu.execom.testutil.model.TierThreeType;
 import eu.execom.testutil.model.TierTwoType;
 import eu.execom.testutil.model.TierTwoTypeWithIgnoreProperty;
 import eu.execom.testutil.model.TierTwoTypeWithListProperty;
 import eu.execom.testutil.model.TierTwoTypeWithPrimitiveProperty;
 import eu.execom.testutil.model.UnknownType;
 import eu.execom.testutil.property.ChangedProperty;
 import eu.execom.testutil.property.IProperty;
 import eu.execom.testutil.property.IgnoreProperty;
 import eu.execom.testutil.property.NotNullProperty;
 import eu.execom.testutil.property.NullProperty;
 import eu.execom.testutil.property.Property;
 import eu.execom.testutil.report.AssertReportBuilder;
 
 /**
  * Tests methods from {@link AbstractExecomAssert}.
  * 
  * @author Dusko Vesin
  * @author Nikola Olah
  * @author Bojan Babic
  * @author Nikola Trkulja
  */
 @SuppressWarnings("rawtypes")
 public class ExecomEntityAssertTest extends AbstractExecomAssertTest {
     private static final String EMPTY_STRING = "";
     private static final String TEST = "test";
     private static final String DOT = ".";
 
     /**
      * Test for assertObject if it ignores types added to ignore list.
      */
     @Test
     public void testAssertObjectIgnoreType() {
         // setup
         final IgnoredType ignoredType = new IgnoredType();
 
         // method
         assertObject(EMPTY_STRING, ignoredType);
     }
 
     /**
      * Test if assertObject only recognizes getMethods for getting properties.
      */
     @Test
     public void testAssertObjectNoGetMethodsType() {
         // setup
         final NoGetMethodsType noGetMethodsType = new NoGetMethodsType(TEST);
         final ChangedProperty<String> jokerProperty = Property.changed(NoGetMethodsType.PROPERTY, TEST + TEST);
 
         // method
         assertObject(EMPTY_STRING, noGetMethodsType, jokerProperty);
     }
 
     /**
      * Test if assertObject throws {@link AssertionError} when there is no property associated to a {@link TierOneType}.
      */
     @Test(expected = AssertionError.class)
     public void testAssertObjectNoProperty() {
         // setup
         final TierOneType tierOneType = new TierOneType(TEST);
 
         // method
         assertObject(EMPTY_STRING, tierOneType);
     }
 
     /**
      * Test if assertObject throws {@link AssertionError} when type has null value property and we assert it with
      * {@link NotNullProperty} with a {@link TierOneType}.
      */
     @Test(expected = AssertionError.class)
     public void testAssertObjectNotNullPropertyActuallyNull() {
         // setup
         final TierOneType tierOneType = new TierOneType(null);
 
         // method
         assertObject(EMPTY_STRING, tierOneType, Property.notNull(TierOneType.PROPERTY));
     }
 
     /**
      * Test for assertObject with {@link NotNullProperty} with a {@link TierOneType}.
      */
     @Test
     public void testAssertObjectNotNullProperty() {
         // setup
         final TierOneType tierOneType = new TierOneType(TEST);
 
         // method
         assertObject(EMPTY_STRING, tierOneType, Property.notNull(TierOneType.PROPERTY));
 
     }
 
     /**
      * Test if assertObject throws {@link AssertionError} when type has not null value property and we assert it with
      * {@link NullProperty} with a {@link TierOneType}.
      */
     @Test(expected = AssertionError.class)
     public void testAssertObjectNullPropertyActuallyNotNull() {
         // setup
         final TierOneType tierOneType = new TierOneType(TEST);
 
         // method
         assertObject(EMPTY_STRING, tierOneType, Property.nulll(TierOneType.PROPERTY));
     }
 
     /**
      * Test for assertObject with {@link NullProperty} with a {@link TierOneType}.
      */
     @Test
     public void testAssertObjectNullProperty() {
         // setup
         final TierOneType tierOneType = new TierOneType(null);
 
         // method
         assertObject(EMPTY_STRING, tierOneType, Property.nulll(TierOneType.PROPERTY));
     }
 
     /**
      * Test for assertObject with {@link IgnoreProperty} with a {@link TierOneType}.
      */
     @Test
     public void testAssertObjectIgnoreProperty() {
         // setup
         final TierOneType tierOneType = new TierOneType(TEST);
 
         // method
         assertObject(EMPTY_STRING, tierOneType, Property.ignored(TierOneType.PROPERTY));
     }
 
     /**
      * Test for asssertObject with {@link ChangedProperty} when expected value is null and actual value is null with a
      * {@link TierOneType}.
      */
     @Test
     public void testAssertObjectChangedPropertyExpectedNullActualNull() {
         // setup
         final TierOneType tierOneType = new TierOneType(null);
 
         // method
         assertObject(EMPTY_STRING, tierOneType, Property.changed(TierOneType.PROPERTY, null));
     }
 
     /**
      * Test if assertObject throws {@link AssertionError} when expected value is not null and actual value is null and
      * we assert it with {@link ChangedProperty} with a {@link TierOneType}.
      */
     @Test(expected = AssertionError.class)
     public void testAssertObjectChangedPropertyActualNull() {
         // setup
         final TierOneType tierOneType = new TierOneType(null);
 
         // method
         assertObject(EMPTY_STRING, tierOneType, Property.changed(TierOneType.PROPERTY, TEST));
     }
 
     /**
      * Test if assertObject throws {@link AssertionError} when expected value is null and actual value is not null and
      * we assert it with {@link ChangedProperty} with a {@link TierOneType}.
      */
     @Test(expected = AssertionError.class)
     public void testAssertObjectChangedPropertyExpectedNull() {
         // setup
         final TierOneType tierOneType = new TierOneType(TEST);
 
         // method
         assertObject(EMPTY_STRING, tierOneType, Property.changed(TierOneType.PROPERTY, null));
     }
 
     /**
      * Test for assertObject with {@link ChangedProperty} when expected value is equal to actual value with a
      * {@link TierOneType}.
      */
     @Test
     public void testAssertObjectChangedPropertyEqual() {
         // setup
         final TierOneType tierOneType = new TierOneType(TEST);
 
         // method
         assertObject(EMPTY_STRING, tierOneType, Property.changed(TierOneType.PROPERTY, TEST));
     }
 
     /**
      * Test for assertObject with {@link ChangedProperty} when expected value is not equal to actual value with a
      * {@link TierOneType}.
      */
     @Test(expected = AssertionError.class)
     public void testAssertObjectChangedPropertyNotEqual() {
         // setup
         final TierOneType tierOneType = new TierOneType(TEST);
 
         // method
         assertObject(EMPTY_STRING, tierOneType, Property.changed(TierOneType.PROPERTY, TEST + TEST));
     }
 
     /**
      * Test for assertObject when ignored type is property of complex object.
      */
     @Test
     public void testAssertObjectChangedPropertyWithIgnoredType() {
         // setup
         final TierTwoTypeWithIgnoreProperty tierTwoTypeWithIgnoreProperty = new TierTwoTypeWithIgnoreProperty(
                 new IgnoredType());
         final ChangedProperty<IgnoredType> jokerProperty = Property.changed(TierTwoTypeWithIgnoreProperty.IGNORED_TYPE,
                 new IgnoredType());
 
         // method
         assertObject(EMPTY_STRING, tierTwoTypeWithIgnoreProperty, jokerProperty);
     }
 
     /**
      * Test if assertObject throws {@link AssertionError} when size of actual list is not equal to expected list with a
      * {@link TierTwoTypeWithListProperty}.
      */
     @Test(expected = AssertionError.class)
     public void testAssertObjectChangedPropertyWithListNotEqualSize() {
         // setup
         final TierTwoTypeWithListProperty tierTwoTypeWithListProperty = new TierTwoTypeWithListProperty(
                 new ArrayList<String>());
         final List<String> jokerList = new ArrayList<String>();
         jokerList.add(TEST);
 
         // method
         assertObject(EMPTY_STRING, tierTwoTypeWithListProperty,
                 Property.changed(TierTwoTypeWithListProperty.PROPERTY, jokerList));
     }
 
     /**
      * Test for assertObject with {@link ChangedProperty} when all actual list members are primitive types and are equal
      * to expected list members with a {@link TierTwoTypeWithListProperty}.
      */
     @Test
     public void testAssertObjectChangedPropertyWithListAllPropertiesEqual() {
         // setup
         final List<String> actualList = new ArrayList<String>();
         actualList.add(TEST);
 
         final List<String> expectedList = new ArrayList<String>();
         expectedList.add(TEST);
 
         final TierTwoTypeWithListProperty tierTwoTypeWithListProperty = new TierTwoTypeWithListProperty(actualList);
 
         // method
         assertObject(EMPTY_STRING, tierTwoTypeWithListProperty,
                 Property.changed(TierTwoTypeWithListProperty.PROPERTY, expectedList));
     }
 
     /**
      * Test for assertObject with {@link ChangedProperty} when actual list members are primitive types and are not equal
      * to expected list members with a {@link TierTwoTypeWithListProperty}.
      */
     @Test(expected = AssertionError.class)
     public void testAssertObjectChangedPropertyWithListAllPropertiesNotEqual() {
         // setup
         final List<String> actualList = new ArrayList<String>();
         actualList.add(TEST);
 
         final List<String> expectedList = new ArrayList<String>();
         expectedList.add(TEST + TEST);
 
         final TierTwoTypeWithListProperty tierTwoTypeWithListProperty = new TierTwoTypeWithListProperty(actualList);
 
         // method
         assertObject(EMPTY_STRING, tierTwoTypeWithListProperty,
                 Property.changed(TierTwoTypeWithListProperty.PROPERTY, expectedList));
     }
 
     /**
      * Test for assertObjects for two {@link TierTwoType} objects with equal values.
      */
     @Test
     public void testAssertObjectsTierTwoObjectsWithEqualValues() {
         // setup
         final TierTwoType actual = new TierTwoType(new TierOneType(TEST));
         final TierTwoType expected = new TierTwoType(new TierOneType(TEST));
 
         // method
         assertObjects(actual, expected);
     }
 
     /**
      * Test for assertObjects for two {@link TierTwoType} objects with equal values.
      */
     @Test(expected = AssertionError.class)
     public void testAssertObjectsTierTwoObjectsWithNotEqualValues() {
         // setup
         final TierTwoType actual = new TierTwoType(new TierOneType(TEST));
         final TierTwoType expected = new TierTwoType(new TierOneType(TEST + TEST));
 
         // method
         assertObjects(actual, expected);
     }
 
     /**
      * Test for assertObjects for two {@link List}s of {@link TierOneType} with equal values.
      */
     @Test
     public void testAssertObjectsListOfTierOneObjectsWithEqualValues() {
         // setup
         final List<TierOneType> actual = new ArrayList<TierOneType>();
         actual.add(new TierOneType(TEST));
         actual.add(new TierOneType(TEST + TEST));
         actual.add(new TierOneType(TEST + TEST + TEST));
 
         final List<TierOneType> expected = new ArrayList<TierOneType>();
         expected.add(new TierOneType(TEST));
         expected.add(new TierOneType(TEST + TEST));
         expected.add(new TierOneType(TEST + TEST + TEST));
 
         // method
         assertObjects(expected, actual);
     }
 
     /**
      * Test for assertObjects for two {@link List}s of {@link TierOneType} with unequal values.
      */
     @Test(expected = AssertionError.class)
     public void testAssertObjectsListOfTierOneObjectsWithNotEqualValues() {
         // setup
         final List<TierOneType> actual = new ArrayList<TierOneType>();
         actual.add(new TierOneType(TEST));
         actual.add(new TierOneType(TEST + TEST));
         actual.add(new TierOneType(TEST + TEST + TEST));
 
         final List<TierOneType> expected = new ArrayList<TierOneType>();
         expected.add(new TierOneType(TEST + TEST));
         expected.add(new TierOneType(TEST + TEST + TEST));
         expected.add(new TierOneType(TEST + TEST + TEST + TEST));
 
         // method
         assertObjects(expected, actual);
     }
 
     /**
      * Test for assertObjects with two {@link TierTwoTypeWithPrimitiveProperty} with {@link IgnoreProperty}.
      */
     @Test
     public void testAssertObjectsTierTwoTypeWithPrimitivePropertyWithIgnoreProperty() {
         // setup
         final TierTwoTypeWithPrimitiveProperty actual = new TierTwoTypeWithPrimitiveProperty(new TierOneType(TEST),
                 TEST);
         final TierTwoTypeWithPrimitiveProperty expected = new TierTwoTypeWithPrimitiveProperty(new TierOneType(TEST
                 + TEST), TEST);
 
         // method
         assertObjects(expected, actual, Property.ignored(TierTwoType.PROPERTY + DOT + TierOneType.PROPERTY));
     }
 
     /**
      * Test for assertObjects with two {@link TierSixType} when complex object depth is six.
      */
     @Test(expected = AssertionError.class)
     public void testAssertObjectsTierSixTypeDepthSix() {
         // setup
         final TierSixType actual = new TierSixType(new TierFiveType(new TierFourType(new TierThreeType(new TierTwoType(
                 new TierOneType(TEST))))));
         final TierSixType expected = new TierSixType(new TierFiveType(new TierFourType(new TierThreeType(
                 new TierTwoType(new TierOneType(TEST + TEST))))));
 
         // method
         assertObjects(actual, expected);
     }
 
     /**
      * Test for assertObjects with {@link TierOneType} when varargs of actual is called.
      */
     @Test
     public void testAssertObjectsVarargsExpected() {
         // setup
         final List<TierOneType> expected = new ArrayList<TierOneType>();
         expected.add(new TierOneType(TEST));
         final TierOneType actual = new TierOneType(TEST);
 
         // method
         assertObjects(expected, actual);
 
     }
 
     /**
      * Test for assertObjects with {@link TierOneType} when varargs of properties is called.
      */
     @Test
     public void testAssertObjectVarargsProperties() {
         // setup
         final TierOneType actual = new TierOneType(TEST);
 
         // method
         assertObject(actual, Property.changed(TierOneType.PROPERTY, TEST));
     }
 
     /**
      * Test for assertObject with {@link EntityTierOneType} with {@link ChangedProperty}.
      */
     @Test
     public void testAssertObjectEntityTierOneType() {
         // setup
         final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
 
         // method
         assertObject(actual, Property.changed(EntityTierOneType.PROPERTY, TEST),
                 Property.changed(EntityTierOneType.ID, 1));
     }
 
     /**
      * Test is assertObject throws {@link AssertionError} with {@link EntityTierOneType} when property isn't asserted.
      */
     @Test(expected = AssertionError.class)
     public void testAssertObjectEntityTierOneTypeThrowsAssertionError() {
         // setup
         final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
 
         // method
         assertObject(actual, Property.changed(EntityTierOneType.ID, 1));
     }
 
     /**
      * Test for assertObjects with two {@link EntityTierTwoType} when all values are equal.
      */
     @Test
     public void testAssertObjectEntityTierTwoTypeAllValuesEqual() {
         // setup
         final EntityTierTwoType actual = new EntityTierTwoType(TEST, 1, new EntityTierOneType(TEST, 10));
         final EntityTierTwoType expected = new EntityTierTwoType(TEST, 1, new EntityTierOneType(TEST, 10));
 
         // method
         assertObjects(expected, actual);
     }
 
     /**
      * Test for assertObjects with two {@link EntityTierTwoType} when not all values are equal.
      */
     @Test
     public void testAssertObjectEntityTierTwoTypeNotAllValuesEqual() {
         // setup
         final EntityTierTwoType actual = new EntityTierTwoType(TEST, 1, new EntityTierOneType(TEST, 10));
         // note expected sub entity property differs from actual sub entity
         // property
         final EntityTierTwoType expected = new EntityTierTwoType(TEST, 1, new EntityTierOneType(TEST + TEST, 10));
 
         // method
         assertObjects(expected, actual);
     }
 
     /**
      * Test for assertObjects with two {@link EntityTierTwoType} when not all values are equal.
      */
     @Test(expected = AssertionError.class)
     public void testAssertObjectEntityTierTwoTypeIdNotEqual() {
         // setup
         final EntityTierTwoType actual = new EntityTierTwoType(TEST, 1, new EntityTierOneType(TEST, 10));
         final EntityTierTwoType expected = new EntityTierTwoType(TEST, 10, new EntityTierOneType(TEST, 20));
 
         // method
         assertObjects(expected, actual);
     }
 
     /**
      * Test for disassembleObject of {@link AbstractExecomAssert} when actual is pointing to itself.
      */
     @Test
     public void testDisassembleObjectTrivialGraphEqual() {
         // setup
         final A actual = new A(null);
         final A expected = new A(null);
         final NodesList nodesList = new NodesList();
 
         // method
         final boolean assertValue = assertBySubproperty(EMPTY_STRING, new AssertReportBuilder(), expected, actual,
                 new ArrayList<IProperty>(), nodesList);
 
         // assert
         assertTrue(assertValue);
     }
 
     /**
      * Test for disassembleObject of {@link AbstractExecomAssert} when actual and expected object nodes are contained in
      * nodes list.
      */
     @Test
     public void testDisassembleObjectNodePairInList() {
         // setup
         final Object actual = new Object();
         final Object expected = new Object();
         final NodesList nodesList = new NodesList();
         nodesList.addPair(expected, actual);
 
         // method
         final boolean assertValue = assertBySubproperty(EMPTY_STRING, new AssertReportBuilder(), expected, actual,
                 new ArrayList<IProperty>(), nodesList);
 
         // assert
         assertTrue(assertValue);
     }
 
     /**
      * Test for disassembledOBject of {@link AbstractExecomAssert} when actual and expected are null.
      */
     @Test
     public void testDisassembleObjectBothNull() {
         // method
         final boolean assertValue = assertBySubproperty(EMPTY_STRING, new AssertReportBuilder(), null, null, null, null);
 
         // assert
         assertTrue(assertValue);
     }
 
     @Test
     public void testDisassembleObjectCyclicGraphEqual() {
         // setup
         final NodesList nodesList = new NodesList();
 
         final A actual = new A(null);
         actual.setB(new B(new C(actual)));
 
         final A expected = new A(null);
         expected.setB(new B(new C(expected)));
 
         // method
         final boolean assertValue = assertBySubproperty(EMPTY_STRING, new AssertReportBuilder(), expected, actual,
                 new ArrayList<IProperty>(), nodesList);
 
         // assert
         assertTrue(assertValue);
     }
 
     /**
      * Test for disassembleObject of {@link AbstractExecomAssert} when getting reference to field via {@link Method}'s
      * method invoke and it trows exception.
      */
     @Test
     public void testDisassembleObject() {
         // method
         final boolean t = assertBySubproperty(EMPTY_STRING, new AssertReportBuilder(), new TierOneType(TEST),
                 new UnknownType(), new LinkedList<IProperty>(), new NodesList());
 
         // assert
         assertFalse(t);
     }
 
     /**
      * Test for assertChangedProperty of {@link AbstractExecomAssert} when actual and expected values are nulls.
      */
     @Test
     public void testAssertChangedPropertyBothNulls() {
         // method
         final boolean assertValue = assertChangedProperty(EMPTY_STRING, new AssertReportBuilder(), null, null,
                 new ArrayList<IProperty>(), new NodesList(), true);
 
         // assert
         assertTrue(assertValue);
     }
 
     /**
      * Test for assertChangedProperty of {@link AbstractExecomAssert} when actual or expected is exclusive null.
      */
     @Test
     public void testAssertChangedPropertyExclusive() {
         // method
         final boolean assertValue1 = assertChangedProperty(EMPTY_STRING, new AssertReportBuilder(), null, new Object(),
                 new ArrayList<IProperty>(), new NodesList(), true);
         final boolean assertValue2 = assertChangedProperty(EMPTY_STRING, new AssertReportBuilder(), new Object(), null,
                 new ArrayList<IProperty>(), new NodesList(), true);
 
         // assert
         assertFalse(assertValue1);
         assertFalse(assertValue2);
     }
 
     /**
      * Test for assertChangedProperty of {@link AbstractExecomAssert} when actual is instance of complex class.
      */
     @Test
     public void testAssertChangedPropertyComplexType() {
         // setup
         final TierOneType actual = new TierOneType(TEST);
         final TierOneType expected = new TierOneType(TEST);
 
         // method
         final boolean assertValue = assertChangedProperty(EMPTY_STRING, new AssertReportBuilder(), expected, actual,
                 new ArrayList<IProperty>(), new NodesList(), true);
 
         // assert
         assertTrue(assertValue);
     }
 
     /**
      * Test for assertChangedProperty of {@link AbstractExecomAssert} when actual is ignored type.
      */
     @Test
     public void testAssertChangedPropertyIgnoredType() {
         // setup
         final IgnoredType actual = new IgnoredType();
         final IgnoredType expected = new IgnoredType();
 
         // method
         final boolean assertValue = assertChangedProperty(EMPTY_STRING, new AssertReportBuilder(), expected, actual,
                 new ArrayList<IProperty>(), new NodesList(), true);
 
         // assert
         assertTrue(assertValue);
     }
 
     /**
      * Test for assertChangedProperty of {@link AbstractExecomAssert} when actual is type unknown to test util and its
      * considered to be primitive.Actual is not equal to expected.
      */
     @Test
     public void testAssertChangedPropertyPrimitiveTypeTrue() {
         // setup
         final String actual = TEST;
         final String expected = TEST;
 
         // method
         final boolean assertValue = assertChangedProperty(EMPTY_STRING, new AssertReportBuilder(), expected, actual,
                 new ArrayList<IProperty>(), new NodesList(), true);
 
         // assert
         assertTrue(assertValue);
     }
 
     /**
      * Test for assertChangedProperty of {@link AbstractExecomAssert} when actual is type unknown to test util and its
      * considered to be primitive. Actual is not equal to expected.
      */
     @Test
     public void testAssertChangedPropertyPrimitiveTypeFalse() {
         // setup
         final String actual = TEST;
         final String expected = TEST + TEST;
 
         // method
         final boolean assertValue = assertChangedProperty(EMPTY_STRING, new AssertReportBuilder(), expected, actual,
                 new ArrayList<IProperty>(), new NodesList(), true);
 
         // assert
         assertFalse(assertValue);
     }
 
     /**
      * Test for assertChangedProperty of {@link AbstractExecomAssert} when actual is entity type.
      */
     @Test
     public void testAssertChangedPropertyEntityType() {
         // setup
         final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
         final EntityTierOneType expected = new EntityTierOneType(TEST, 1);
 
         // method
         final boolean assertValue = assertChangedProperty(EMPTY_STRING, new AssertReportBuilder(), expected, actual,
                 null, null, true);
 
         // assert
         assertTrue(assertValue);
     }
 
     /**
      * Test for assertChangedProperty of {@link AbstractExecomAssert} when actual list and expected list are equal.
      */
     @Test
     public void testAssertChangedPropertyListTypeEqual() {
         // setup
         final List<String> actual = new ArrayList<String>();
         actual.add(TEST);
         actual.add(TEST);
         final List<String> expected = new ArrayList<String>();
         expected.add(TEST);
         expected.add(TEST);
 
         // method
         final boolean assertValue = assertChangedProperty(EMPTY_STRING, new AssertReportBuilder(), expected, actual,
                 new LinkedList<IProperty>(), null, true);
 
         // assert
         assertTrue(assertValue);
     }
 
     /**
      * Test for assertEntityTypes of {@link AbstractExecomAssert} when actual and expected are equal entity types.
      */
     @Test
     public void testAssertEntityTypesBothEntities() {
         // setup
         final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
         final EntityTierOneType expected = new EntityTierOneType(TEST, 1);
 
         // method
         final boolean assertValues = assertEntityById(new AssertReportBuilder(), EMPTY_STRING, expected, actual);
 
         // assert
         assertTrue(assertValues);
 
     }
 
     /**
      * Test for assertEntityTypes of {@link AbstractExecomAssert} when actual and expected are not entity types.
      */
     @Test
     @Ignore
     public void testAssertEntityTypesBothEntity() {
         // setup
         final UnknownType actual = new UnknownType();
         final UnknownType expected = new UnknownType();
 
         // method
         final boolean assertValues = assertEntityById(new AssertReportBuilder(), EMPTY_STRING, expected, actual);
 
         // assert
         assertFalse(assertValues);
 
     }
 
     /**
      * Test for assertEntityTypes of {@link AbstractExecomAssert} when actual is not entity type.
      */
     @Test
     public void testAssertEntityTypesActualNotEntity() {
         // setup
         final UnknownType actual = new UnknownType();
         final EntityTierOneType expected = new EntityTierOneType(TEST, 1);
 
         // method
         final boolean assertValues = assertEntityById(new AssertReportBuilder(), EMPTY_STRING, expected, actual);
 
         // assert
         assertFalse(assertValues);
 
     }
 
     /**
      * Test for assertEntityTypes of {@link AbstractExecomAssert} when expected is not entity type.
      */
     @Test
     public void testAssertEntityTypesExpectedNotEntity() {
         // setup
         final EntityTierOneType actual = new EntityTierOneType(TEST, 1);
         final UnknownType expected = new UnknownType();
 
         // method
         final boolean assertValues = assertEntityById(new AssertReportBuilder(), EMPTY_STRING, expected, actual);
 
         // assert
         assertFalse(assertValues);
 
     }
 
     /**
      * Test for assertProperties of {@link AbstractExecomAssert} when expected property is {@link NotNullProperty} and
      * actual is not null.
      */
     @Test
     public void testAssertPropertiesNotNullPropertyTrue() {
         // method
         final boolean assertValue = assertProperties(EMPTY_STRING, new AssertReportBuilder(),
                 Property.notNull(TierOneType.PROPERTY), new TierOneType(TEST), "", new ArrayList<IProperty>(), null,
                 true);
 
         // assert
         assertTrue(assertValue);
     }
 
     /**
      * Test for assertProperties of {@link AbstractExecomAssert} when expected property is {@link NotNullProperty} and
      * actual is null.
      */
     @Test
     public void testAssertPropertiesNotNullPropertyFalse() {
         // method
         final boolean assertValue = assertProperties(EMPTY_STRING, new AssertReportBuilder(),
                 Property.notNull(TierOneType.PROPERTY), null, "", new ArrayList<IProperty>(), null, true);
 
         // assert
         assertFalse(assertValue);
     }
 
     /**
      * Test for assertProperties of {@link AbstractExecomAssert} when expected property is {@link NullProperty} and
      * actual is null.
      */
     @Test
     public void testAssertPropertiesNullPropertyTrue() {
         // method
         final boolean assertValue = assertProperties(EMPTY_STRING, new AssertReportBuilder(),
                 Property.nulll(TierOneType.PROPERTY), null, "", new ArrayList<IProperty>(), null, true);
 
         // assert
         assertTrue(assertValue);
     }
 
     /**
      * Test for assertProperties of {@link AbstractExecomAssert} when expected property is {@link NullProperty} and
      * actual is not null.
      */
     @Test
     public void testAssertPropertiesNullPropertyFalse() {
         // method
         final boolean assertValue = assertProperties(EMPTY_STRING, new AssertReportBuilder(),
                 Property.nulll(TierOneType.PROPERTY), new TierOneType(TEST), "", new ArrayList<IProperty>(), null, true);
 
         // assert
         assertFalse(assertValue);
     }
 
     /**
      * Test for assertProperties of {@link AbstractExecomAssert} when expected property is {@link IgnoreProperty}.
      */
     @Test
     public void testAssertPropertiesIgnoreProperty() {
         // method
         final boolean assertValue = assertProperties(EMPTY_STRING, new AssertReportBuilder(),
                 Property.ignored(TierOneType.PROPERTY), new TierOneType(TEST), "", new ArrayList<IProperty>(), null,
                 true);
 
         // assert
         assertTrue(assertValue);
     }
 
     /**
      * Test for assertList of {@link AbstractExecomAssert} when actual list has different size than expected list.
      */
     @Test
     public void testAssertListNotEqualSize() {
         // setup
         final List<String> actual = new ArrayList<String>();
         actual.add(TEST);
         actual.add(TEST);
         final List<String> expected = new ArrayList<String>();
         expected.add(TEST);
 
         // method
         final boolean assertValue = assertList(EMPTY_STRING, new AssertReportBuilder(), expected, actual,
                 new ArrayList<IProperty>(), new NodesList(), false);
 
         // assert
         assertFalse(assertValue);
     }
 
     /**
      * Test for assertList of {@link AbstractExecomAssert} when actual list is equal to expected list.
      */
     @Test
     public void testAssertListEqual() {
         // setup
         final List<String> actual = new ArrayList<String>();
         actual.add(TEST);
         actual.add(TEST);
         final List<String> expected = new ArrayList<String>();
         expected.add(TEST);
         expected.add(TEST);
 
         // method
         final boolean assertValue = assertList(EMPTY_STRING, new AssertReportBuilder(), expected, actual,
                 new ArrayList<IProperty>(), new NodesList(), false);
 
         // assert
         assertTrue(assertValue);
     }
 
     /**
      * Test for assertList of {@link AbstractExecomAssert} when actual list is equal to expected list.
      */
     @Test
     public void testAssertListNotEqual() {
         // setup
         final List<String> actual = new ArrayList<String>();
         actual.add(TEST);
         actual.add(TEST + TEST);
         final List<String> expected = new ArrayList<String>();
         expected.add(TEST);
         expected.add(TEST);
 
         // method
         final boolean assertValue = assertList(EMPTY_STRING, new AssertReportBuilder(), expected, actual,
                 new ArrayList<IProperty>(), new NodesList(), false);
 
         // assert
         assertFalse(assertValue);
     }
 
     /**
      * Test for preAssertObjectWithProperties of {@link AbstractExecomAssert} when object properties match specified
      * properties.
      */
     @Test
     public void testPreAssertObjectWithPropertiesEqual() {
         // setup
         final List<IProperty> properties = new ArrayList<IProperty>();
         properties.add(Property.changed(TierOneType.PROPERTY, "ninja"));
 
         // method
         preAssertObjectWithProperties(new AssertReportBuilder(), new TierOneType("ninja"), properties);
 
     }
 
     /**
      * Test for preAssertObjectWithProperties of {@link AbstractExecomAssert} when object properties don't match
      * specified properties.
      */
     @Test
     public void testPreAssertObjectWithPropertiesNotEqual() {
         // setup
         final List<IProperty> properties = new ArrayList<IProperty>();
         properties.add(Property.changed(TierOneType.PROPERTY, TEST));
 
         // method
         final boolean assertValue = preAssertObjectWithProperties(new AssertReportBuilder(), new TierOneType(TEST
                 + TEST), properties);
 
         // assert
         assertFalse(assertValue);
     }
 
     /**
      * Test for preAssertObjectWithProperties of {@link AbstractExecomAssert} when specified property's path doesn't
      * match object's field path.
      */
     @Test
     public void testPreAssertObjectWithPropertiesBadProperties() {
         // setup
         final List<IProperty> properties = new ArrayList<IProperty>();
         properties.add(Property.changed(TEST, TEST));
 
         // method
         final boolean assertValue = preAssertObjectWithProperties(new AssertReportBuilder(), new TierOneType(TEST),
                 properties);
 
         // assertTrue
         assertFalse(assertValue);
     }
 
     /**
      * Test for preListAssert of {@link AbstractExecomAssert} when expected and actual list are nulls.
      */
     @Test
     public void testPreListForAssertNull() {
         // method
         final boolean assertValue = beforeListAssert(new AssertReportBuilder(), null, null);
 
         // assert
         assertTrue(assertValue);
     }
 
     /**
      * Test for preListAssert of {@link AbstractExecomAssert} when expected is null and actual is not.
      */
     @Test
     public void testPreListForAssertExclusiveNull() {
         // method
         final boolean assertValue = beforeListAssert(new AssertReportBuilder(), null, new LinkedList<String>());
 
         // assert
         assertFalse(assertValue);
     }
 
     /**
      * Test for getIdValue of {@link AbstractExecomAssert} when specified type is entity.
      */
     @Test
     public void testGetIdValueEntity() {
         // setup
         final EntityTierOneType entityTierOneType = new EntityTierOneType(TEST, 1);
 
         // method
         final Integer id = (Integer) getIdValue(entityTierOneType);
 
         // assert
         assertEquals(1, id.intValue());
     }
 
     /**
      * Test for getIdValue of {@link AbstractExecomAssert} when specified type is unknown type.
      */
     @Test
     public void testGetIdValueUknownType() {
         // setup
         final UnknownType unknownType = new UnknownType();
         // method
         final Integer id = (Integer) getIdValue(unknownType);
 
         // assert
         assertNull(id);
     }
 
     /**
      * Test for removeParentQualificationForProperties of {@link AbstractExecomAssert}.
      */
     @Test
     public void testRemoveParentQualificationForProperty() {
         // setup
         final List<IProperty> properties = new ArrayList<IProperty>();
         properties.add(Property.notNull("parent.id"));
         properties.add(Property.notNull("parent.name"));
         properties.add(Property.notNull("parent.lastname"));
 
         // method
         final List<IProperty> unqualifiedProperties = removeParentQualificationForProperties("parent", properties);
 
         // assert
         assertEquals("id", unqualifiedProperties.get(0).getPath());
         assertEquals("name", unqualifiedProperties.get(1).getPath());
         assertEquals("lastname", unqualifiedProperties.get(2).getPath());
     }
 
     /**
      * Test for generateNewProperty of {@link AbstractExecomAssert} when property for that field already exists.
      */
     @Test
     public void testGeneratePropertyFromListOfExcluded() {
         // setup
         final TierOneType tierOneType = new TierOneType(TEST);
         final List<IProperty> properties = new ArrayList<IProperty>();
         properties.add(Property.notNull(TierOneType.PROPERTY));
         final int numProperties = properties.size();
 
         // method
         final IProperty property = obtainProperty(tierOneType, TierOneType.PROPERTY, properties);
 
         // assert
         assertEquals(TierOneType.PROPERTY, property.getPath());
         assertEquals(numProperties - 1, properties.size());
     }
 
     /**
      * Test for generateNewProperty of {@link AbstractExecomAssert} when specified field needs to be wrapped inside new
      * ChangedProperty.
      */
     @Test
     public void testGeneratePropertyCreateNew() {
         // setup
         final TierOneType tierOneType = new TierOneType(TEST);
         final List<IProperty> properties = new ArrayList<IProperty>();
         properties.add(Property.notNull(TEST));
         final int numProperties = properties.size();
 
         // method
         final ChangedProperty property = (ChangedProperty) obtainProperty(tierOneType, TierOneType.PROPERTY, properties);
 
         // assert
         assertEquals(TierOneType.PROPERTY, property.getPath());
         assertEquals(tierOneType, property.getExpectedValue());
         assertEquals(numProperties, properties.size());
 
     }
 
     /**
      * Test for popProperty of {@link AbstractExecomAssert} when specified property's path matches specified path.
      */
     @Test
     public void testPopPropertyEqualPath() {
         // setup
         final List<IProperty> properties = new LinkedList<IProperty>();
         properties.add(Property.nulll(TEST));
 
         // method
         final IProperty property = getPropertyFromList(TEST, properties);
 
         // assert
         assertEquals(TEST, property.getPath());
         assertEquals(0, properties.size());
     }
 
     /**
      * Test for popProperty of {@link AbstractExecomAssert} when there are no properties in the list.
      */
     @Test
     public void testPopPropertyNoProperties() {
         // setup
         final List<IProperty> properties = new LinkedList<IProperty>();
 
         // method
         final IProperty property = getPropertyFromList(TEST, properties);
 
         // assert
         assertEquals(null, property);
     }
 
     /**
      * Test for popProperty of {@link AbstractExecomAssert} when specified property's path doesn't match specified path.
      */
     @Test
     public void testPopPropertyNotEqualPath() {
         // setup
         final List<IProperty> properties = new LinkedList<IProperty>();
         properties.add(Property.nulll(TEST));
 
         // method
         final IProperty property = getPropertyFromList(TEST + TEST, properties);
 
         // assert
         assertEquals(null, property);
     }
 
     /**
      * Test for getObjectGetMethods of {@link AbstractExecomAssert} when has one real get method.
      */
     @Test
     public void testGetObjectGetMethodsTierOneType() {
         // method
         final List<Method> methods = getObjectGetMethods(new TierOneType(TEST));
         final Method method = methods.get(0);
         // assert
         assertEquals(1, methods.size());
         assertEquals("getProperty", method.getName());
     }
 
     /**
      * Test for getObjectGetMethods of {@link AbstractExecomAssert} when class has no real get methods.
      */
     @Test
     public void testGetObjectGetMethodsNoGetMethodsType() {
         // method
         final List<Method> methods = getObjectGetMethods(new NoGetMethodsType(TEST));
         // assert
         assertEquals(0, methods.size());
     }
 
     /**
      * Test for getObjectGetMethods of {@link AbstractExecomAssert} if it impose ordering of get methods for complex or
      * entity types come last in list.
      * 
      * @throws Exception
      */
     @Test
     public void testGetObjectGetMethodsCheckOrdering() throws Exception {
         // setup
         final Method getSubProperty = EntityTierTwoType.class.getMethod("getSubProperty");
 
         // method
         final List<Method> methods = getObjectGetMethods(new EntityTierTwoType(TEST, 10, new EntityTierOneType(TEST, 5)));
 
         // assert
         assertEquals(getSubProperty.getName(), methods.get(2).getName());
     }
 
     /**
      * Test for checkForNull of {@link AbstractExecomAssert} with two null objects.
      */
     @Test
     public void testCheckForNullsTrueNull() {
         // method
         final ReferenceCheckType assertValue = referenceCheck(new AssertReportBuilder(), null, null, TEST);
 
         // assert
         assertEquals(ReferenceCheckType.EQUAL_REFERENCE, assertValue);
     }
 
     /**
      * Test for checkForNulls of {@link AbstractExecomAssert} with two same references.
      */
     @Test
     public void testCheckForNullsTrueNotNull() {
         // setup
         final TierOneType expected = new TierOneType();
 
         // method
         final ReferenceCheckType assertValue = referenceCheck(new AssertReportBuilder(), expected, expected, TEST);
 
         // assert
         assertEquals(ReferenceCheckType.EQUAL_REFERENCE, assertValue);
     }
 
     /**
      * Test for checkForNulls of {@link AbstractExecomAssert} with one object null and one not null, both cases.
      */
     @Test
     public void testCheckForNullsFalse() {
         // method
         final ReferenceCheckType assertValue1 = referenceCheck(new AssertReportBuilder(), new Object(), null, TEST);
         final ReferenceCheckType assertValue2 = referenceCheck(new AssertReportBuilder(), null, new Object(), TEST);
 
         // assert
         assertEquals(ReferenceCheckType.EXCLUSIVE_NULL, assertValue1);
         assertEquals(ReferenceCheckType.EXCLUSIVE_NULL, assertValue2);
     }
 
     /**
      * Test for checkForNulls of {@link AbstractExecomAssert} with both objects not null.
      */
     @Test
     public void testCheckForNullsNull() {
         // method
         final ReferenceCheckType assertValue = referenceCheck(new AssertReportBuilder(), new Object(), new Object(),
                 TEST);
 
         // assert
         assertEquals(ReferenceCheckType.COMPLEX_ASSERT, assertValue);
     }
 }
