 /*****************************************************************************************
  * Copyright (c) 2012 Dylan Bettermann, Andrew Helgeson, Brian Maurer, Ethan Waytas
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  ****************************************************************************************/
 // PerDayCarExpensesTest.java
 package edu.umn.se.trap.rules.business;
 
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 import edu.umn.se.test.frame.FormDataQuerier;
 import edu.umn.se.test.frame.TrapTestFramework;
 import edu.umn.se.trap.data.TransportationTypeEnum;
 import edu.umn.se.trap.exception.BusinessLogicException;
 import edu.umn.se.trap.exception.TRAPException;
 import edu.umn.se.trap.form.InputFieldKeys;
 import edu.umn.se.trap.test.generate.TestDataGenerator.SampleDataEnum;
 
 /**
  * @author Dylan
  * 
  */
 public class PerDayCarExpensesTest extends TrapTestFramework
 {
    int numCarTravel, totalTransportationExpenses;
 
     String newDateField;
     String newTypeField;
    String newRentalField;
    String newMilesTravelled;

    String transportationTotalField = "NUM_TRANSPORTATION";
 
     @SuppressWarnings("javadoc")
     @Rule
     public ExpectedException exception = ExpectedException.none();
 
     /**
      * Load a sample international form.
      * 
      * This set up prepares a new car transportation expense to be added to the form.
      * 
      * @throws TRAPException When form processing fails.
      */
     @Before
     public void setup() throws TRAPException
     {
         super.setup(SampleDataEnum.INTERNATIONAL1);
 
        totalTransportationExpenses = Integer.parseInt(testFormData.get(transportationTotalField));

         List<Integer> carExpenses = FormDataQuerier.findTransportExpenses(testFormData,
                 TransportationTypeEnum.CAR);
         numCarTravel = carExpenses.size();
         if (numCarTravel == 0)
         {
             Assert.fail("Need car expenses in the sample form for this test");
         }
 
         newDateField = String.format(InputFieldKeys.TRANSPORTATION_DATE_FMT, 9);
         newTypeField = String.format(InputFieldKeys.TRANSPORTATION_TYPE_FMT, 9);
        newRentalField = String.format(InputFieldKeys.TRANSPORTATION_RENTAL_FMT, 9);
        newMilesTravelled = String.format(InputFieldKeys.TRANSPORTATION_MILES_FMT, 9);
     }
 
     /**
      * Verify that car expenses can exist on different days are accepted.
      * 
      * @throws TRAPException When form processing fails.
      */
     @Test
     public void validCarExpenses() throws TRAPException
     {
         // Sample form does not have personal car and rental car expenses on same day
         saveAndSubmitTestForm();
     }
 
     /**
      * Verify that the form is rejected when there is a rental and personal car expense on the same
      * day
      * 
      * @throws TRAPException When form processing fails.
      */
     @Test
     public void invalidCarExpenses() throws TRAPException
     {
         exception.expect(BusinessLogicException.class);
         exception.expectMessage("has personal and rental car expenses on the same day");
 
         testFormData.put(newDateField, "20121125");
         testFormData.put(newTypeField, "CAR");
        testFormData.put(newRentalField, "No");
        testFormData.put(newMilesTravelled, "30.0");

        ++totalTransportationExpenses;

        testFormData.put(transportationTotalField, Integer.toString(totalTransportationExpenses));
 
         saveAndSubmitTestForm();
 
     }
 }
