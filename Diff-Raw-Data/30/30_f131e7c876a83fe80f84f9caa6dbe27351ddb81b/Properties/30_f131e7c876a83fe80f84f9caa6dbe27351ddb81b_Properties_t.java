 /*
  * Copyright © 2013 VillageReach. All Rights Reserved. This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
  *
  * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
  */
 
 package org.openlmis.performancetesting;
 
 import lombok.NoArgsConstructor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import static java.lang.Integer.parseInt;
 
 @Component
 @NoArgsConstructor
 public class Properties {
 
   private static final String BUNDLE_NAME = "config";
   private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
   private static final Logger logger = LoggerFactory.getLogger(Properties.class);
 
   public static final int STATES_PER_COUNTRY = getInteger("states.per.country");
   public static final int DISTRICT_PER_STATE = getInteger("district.per.state");
   public static final int FACILITIES_PER_DISTRICT = getInteger("facilities.per.district");
   public static final int FACILITIES_PER_REQUISITION_GROUP = getInteger("facilities.per.rg");
   public static final int NO_OF_FACILITY_TYPES = getInteger("no.of.facility.types");
   public static final int ENDING_PERIOD_MONTH = getInteger("ending.period.month");
   public static final int STARTING_PERIOD_MONTH = getInteger("starting.period.month");
 
 
   public static final int MAX_FULL_SUPPLY_COUNT = getInteger("max.full.supply.products.count");
   public static final int AVERAGE_FULL_SUPPLY_COUNT = getInteger("average.full.supply.products.count");
   public static final int NUMBER_OF_NON_FULL_SUPPLY_ITEMS = getInteger("no.of.non.full.supply.products");
   public static final int TOTAL_NO_OF_PRODUCTS = getInteger("total.no.of.products");
  public static final int NO_OF_PARALLEL_THREADS= getInteger("no.of.parallel.threads");
 
   public static int getInteger(String key) {
     String strValue = getString(key);
     int result = -1;
     try {
       result = parseInt(strValue);
     } catch (Exception exc) {
       logger.error(exc.getLocalizedMessage());
     }
     return result;
   }
 
 
   public static String getString(String key) {
     String returnValue = System.getProperty(key);
     if (returnValue != null && returnValue.length() > 0) {
       logger.debug(key + " assigned by system property");
       return returnValue;
     }
     try {
       returnValue = RESOURCE_BUNDLE.getString(key);
     } catch (MissingResourceException e) {
       returnValue = '!' + key + '!';
     }
     return returnValue;
   }
 
   public static final String[] PROGRAM_NAMES = {"ESS MEDICINES", "TB", "MALARIA", "ARV/ART", "VACCINES"};
 
   public static final String[] ADJUSTMENT_NAMES = {"TRANSFER_IN", "TRANSFER_OUT", "DAMAGED", "LOST", "STOLEN", "EXPIRED", "PASSED_OPEN_VIAL_TIME_LIMIT", "COLD_CHAIN_FAILURE", "CLINIC_RETURN"};
 }
