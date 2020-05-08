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
 // AlcoholOnlyAllowedUnderNonSponsored.java
 package edu.umn.se.trap.rules.business;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import edu.umn.se.trap.data.ReimbursementApp;
 import edu.umn.se.trap.data.TRAPConstants;
 import edu.umn.se.trap.data.TransportationExpense;
 import edu.umn.se.trap.data.TransportationTypeEnum;
 import edu.umn.se.trap.exception.BusinessLogicException;
 import edu.umn.se.trap.exception.InputValidationException;
 import edu.umn.se.trap.exception.TRAPException;
 
 /**
  * Adds transportation expenses involving car mileage. Mileage can only be claimed for personal car
  * use. Mileage is reimbursed at a rate of $0.55 per mile.
  * 
  * @author andrewh
  * 
  */
 public class TransportationMileageExpenses extends BusinessLogicRule
 {
     /** Log for the TransportationGasMileage class */
     private static final Logger log = LoggerFactory.getLogger(TransportationMileageExpenses.class);
 
     /** Reimbursement rate per mile for gas expenses */
     private static final Double mileageRate = 0.55;
 
     /**
      * Adds transportation expenses involving car mileage. Mileage can only be claimed for personal
      * car use. Mileage is reimbursed at a rate of $0.55 per mile.
      */
     @Override
     public void checkRule(ReimbursementApp app) throws TRAPException
     {
         Double mileageTotal = 0.0;
         for (TransportationExpense expense : app.getTransportationExpenseList())
         {
             Integer milesTraveled = expense.getTransportationMilesTraveled();
             if (expense.getTransportationType() == TransportationTypeEnum.CAR)
             {
                 // Make sure it isn't a rental car
                 String rental = expense.getTransportationRental();
                 if (rental != null && rental.compareToIgnoreCase(TRAPConstants.STR_YES) == 0)
                 {
                     if (milesTraveled != null)
                     {
                         log.error("Mileage claimed on rental car not allowed:\n{}", expense);
                         throw new BusinessLogicException(
                                 "Mileage cannot be claimed on a rental car expense");
                     }
 
                     continue;
                 }
 
                 // Make sure there are miles claimed since we are dealing with a personal car
                 // expense
                 if (milesTraveled == null)
                 {
                    log.error("Missing miles traveled on car expense:\n{}", expense);
                     throw new BusinessLogicException(
                             "Missing miles traveled for car transportation expense");
                 }
 
                 if (milesTraveled < 0)
                 {
                     log.error("Negative value for miles traveled:\n{}", expense);
                     throw new InputValidationException(
                             "Miles traveled must be positive for transportation expense");
                 }
 
                 // Otherwise we can charge it
                 Double reimbursementAmount = mileageRate * milesTraveled;
                 log.info("Granting mileage reimbursement for ${}. miles = {}", reimbursementAmount,
                         milesTraveled);
                 expense.setReimbursementAmount(reimbursementAmount);
                 app.addToReimbursementTotal(reimbursementAmount);
 
                 mileageTotal += reimbursementAmount;
             }
             else if (milesTraveled != null)
             {
                 throw new BusinessLogicException(
                         "Mileage claimed for non car transportation expense");
             }
         }
 
         log.info("Added ${} in mileage expenses to total", mileageTotal);
     }
 }
