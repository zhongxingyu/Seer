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
 // GrantJustificationTest.java
 package edu.umn.se.trap.data;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 import edu.umn.se.test.frame.TrapTestFramework;
 import edu.umn.se.trap.exception.InputValidationException;
import edu.umn.se.trap.exception.MissingFieldException;
 import edu.umn.se.trap.exception.TRAPException;
 import edu.umn.se.trap.form.InputFieldKeys;
 import edu.umn.se.trap.test.generate.TestDataGenerator.SampleDataEnum;
 
 /**
  * TODO: Requirement
  * 
  * @author andrewh
  * 
  */
 public class GrantJustificationTest extends TrapTestFramework
 {
 
     /**
      * Load sample form.
      * 
      * @throws TRAPException When form saving fails.
      */
     @Before
     public void setup() throws TRAPException
     {
         super.setup(SampleDataEnum.DOMESTIC1);
     }
 
     /**
      * Verify that a form using sponsored funding and a corresponding justification is accepted.
      * 
      * @throws TRAPException When form processing fails.
      */
     @Test
     public void sponsoredWithJust() throws TRAPException
     {
         saveAndSubmitTestForm();
     }
 
     @SuppressWarnings("javadoc")
     @Rule
     public ExpectedException exception = ExpectedException.none();
 
     /**
      * Verify that sponsored funding with a non sponsored just is rejected.
      * 
      * @throws TRAPException When form processing fails.
      */
     @Test
     public void sponsoredWithNonSponsoredJust() throws TRAPException
     {
         exception.expect(InputValidationException.class);
 
         testFormData.remove(InputFieldKeys.JUSTIFICATION_SPONSORED);
         testFormData.put(InputFieldKeys.JUSTIFICATION_NONSPONSORED, "party");
         saveAndSubmitTestForm();
     }
 
     /**
      * Verify that use of sponsored funding without a justification is rejected.
      * 
      * @throws TRAPException When form processing fails.
      */
     @Test
     public void sponsoredWithNoJust() throws TRAPException
     {
        exception.expect(MissingFieldException.class);
         exception
                 .expectMessage("Grant justfication field. Must have at least one depending on the grants used.");
 
         testFormData.remove(InputFieldKeys.JUSTIFICATION_SPONSORED);
         saveAndSubmitTestForm();
     }
 
     /**
      * Verify that the use of non-sponsored funding with a justification is accepted.
      * 
      * @throws TRAPException When form processing fails.
      */
     @Test
     public void nonSponsoredWithJust() throws TRAPException
     {
         testFormData.remove(InputFieldKeys.TRAVEL_TYPE_CSE_SPONSORED);
         testFormData.remove(InputFieldKeys.JUSTIFICATION_SPONSORED);
         testFormData.put(InputFieldKeys.TRAVEL_TYPE_NONSPONSORED, TRAPConstants.STR_YES);
         testFormData.put(InputFieldKeys.JUSTIFICATION_NONSPONSORED, "party");
 
         saveAndSubmitTestForm();
     }
 
 }
