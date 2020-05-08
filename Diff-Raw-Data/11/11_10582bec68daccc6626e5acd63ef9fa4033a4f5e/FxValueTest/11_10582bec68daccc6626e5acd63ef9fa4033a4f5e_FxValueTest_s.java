 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.shared;
 
 import com.flexive.shared.value.*;
 import com.flexive.shared.FxLanguage;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 import org.testng.Assert;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Generic FxValue tests
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Test(groups = "shared")
 public class FxValueTest {
     private static Object[][] testInstances = new Object[][]{
             {new ValueTestBean<String, FxString>(new FxString(false, "Test string"), "Element",
                     new String[]{"String value"}, new String[0])},
             {new ValueTestBean<Integer, FxNumber>(new FxNumber(false, 42), 1234,
                     new String[]{"5678", "0", "-23124"},
                    new String[]{"6789x", "", "5.6", "-", "-0.1"})},
             {new ValueTestBean<Long, FxLargeNumber>(new FxLargeNumber(false, (long) 42), (long) 1234,
                     new String[]{"5678", "0", "-23124"},
                    new String[]{"6789x", "", "5.6", "-", "-0.1"})},
             {new ValueTestBean<Double, FxDouble>(new FxDouble(false, 1979.1111), 1234.5678,
                     new String[]{"5678", "0", "-23124", "0.0", "-5.3", "213341.314231", "321,432", "123,456,000", "123,456.000"},
                    new String[]{"6789x", "", "abc"})},
             {new ValueTestBean<Float, FxFloat>(new FxFloat(false, 1979.1111f), 1234.5678f,
                     new String[]{"5678", "0", "-23124", "0.0", "-5.3", "213341.314231", "321,432", "123,456,000", "123,456.000"},
                    new String[]{"6789x", "", "abc"})},
             {new ValueTestBean<Boolean, FxBoolean>(new FxBoolean(false, true), false,
                     new String[]{"true", "false"},
                     new String[]{})}
             // TODO add test instances for FxDate, FxDateRange
     };
 
     @Test(dataProvider = "testInstances")
     public <T, TDerived extends FxValue<T, TDerived>> void testIsValid(ValueTestBean<T, TDerived> testBean) {
         FxValue<T, TDerived> value = testBean.getValue().copy();
         assertTrue(value.isValid(), "Test value should be valid: " + value);
         assertTrue(!value.isMultiLanguage(), "Value must not be multilingual: " + value);
 
         // test valid string values
         for (String validValue : testBean.getValidStringValues()) {
             //noinspection unchecked
             ((FxValue) value).setValue(validValue);
             assertTrue(value.isValid(), "String value should be valid: " + validValue);
         }
 
         // test invalid string values
         for (String invalidValue : testBean.getInvalidStringValues()) {
             //noinspection unchecked
             ((FxValue) value).setValue(invalidValue);
             assertTrue(!value.isValid(), "String value should not be valid: " + invalidValue);
         }
         value.setValue(testBean.getValueElement());
         assertTrue(value.isValid(), "Value must be valid: " + testBean.getValueElement());
     }
 
     @Test(dataProvider = "testInstances")
     public <T, TDerived extends FxValue<T, TDerived>> void testValidContract(ValueTestBean<T, TDerived> testBean) {
         // checks the contract of FxValue#isValid() and FxValue#getErrorValue():
         FxValue<T, TDerived> value = testBean.getValue().copy();
         // if !isValid(), then getErrorValue() must return an object.
         for (String validValue : testBean.getValidStringValues()) {
             //noinspection unchecked
             ((FxValue) value).setValue(validValue);
             assertTrue(value.isValid());
             try {
                 value.getErrorValue();
                 Assert.fail("Valid values cannot return error values.");
             } catch (IllegalStateException e) {
                 // pass
             }
         }
         // check empty values
         value.setEmpty();
         if (value.isValid()) {
             try {
                 value.getErrorValue();
                 Assert.fail("Valid values cannot return error values.");
             } catch (IllegalStateException e) {
                 // pass
             }
         } else {
             assertTrue(value.getErrorValue() != null, "Invalid values must return an error value.");
         }
         // if isValid(), then getErrorValue() must throw an IllegalStateException.
         for (String invalidValue : testBean.getInvalidStringValues()) {
             //noinspection unchecked
             ((FxValue) value).setValue(invalidValue);
             assertTrue(!value.isValid());
             assertTrue(value.getErrorValue() != null, "Invalid value must return an error value.");
         }
     }
 
     /**
      * Bug check: if a previously empty number property is updated with the same
      * "empty" number, the isEmpty flag is not set correctly.
      */
     @Test
     public void testUpdateEmptyValue() {
         FxNumber number = new FxNumber(0).setEmpty();
         assertTrue(number.isEmpty());
         number.setValue(0);
         assertTrue(!number.isEmpty(), "FxNumber still empty after explicit value update.");
     }
 
     /**
      * Bug check: FxNoAccess should override equals, otherwise it will throw an NPE
      */
     @Test
     public void testNoAccessEquals() {
         final FxString value = new FxString(false, "test");
         final FxNoAccess noAccess = new FxNoAccess(null, value);
         assertFalse(noAccess.equals(new FxNoAccess(null, value)));
     }
 
     @SuppressWarnings({"unchecked"})
     @Test
     public void multiLanguageValidTest() {
         // create a FxValue with valid and invalid entries
         final FxNumber value = new FxNumber(true, 41).setTranslation(FxLanguage.ENGLISH, 21);
         // force string value into content - this will usually happen for user input from an input component
         ((FxValue) value).setTranslation(FxLanguage.GERMAN, "abc");
         assertFalse(value.isValid());
         assertTrue(value.isValid(FxLanguage.SYSTEM_ID));
         assertTrue(value.isValid(FxLanguage.ENGLISH));
         assertFalse(value.isValid(FxLanguage.GERMAN));
     }
 
     @DataProvider(name = "testInstances")
     private Object[][] getTestInstances() {
         return testInstances;
     }
 
     /**
      * Test instance for a FxValue element.
      */
     private static class ValueTestBean<T, TDerived extends FxValue<T, TDerived>> {
         private FxValue<T, TDerived> value;
         private T valueElement;
         private List<String> validStringValues;
         private List<String> invalidStringValues;
 
         public ValueTestBean(FxValue<T, TDerived> value, T valueElement, String[] validStringValues, String[] invalidStringValues) {
             super();
             this.value = value;
             this.valueElement = valueElement;
             this.validStringValues = Collections.unmodifiableList(Arrays.asList(validStringValues));
             this.invalidStringValues = Collections.unmodifiableList(Arrays.asList(invalidStringValues));
         }
 
         public List<String> getInvalidStringValues() {
             return invalidStringValues;
         }
 
         public List<String> getValidStringValues() {
             return validStringValues;
         }
 
         public FxValue<T, TDerived> getValue() {
             return value;
         }
 
         public T getValueElement() {
             return valueElement;
         }
     }
 }
