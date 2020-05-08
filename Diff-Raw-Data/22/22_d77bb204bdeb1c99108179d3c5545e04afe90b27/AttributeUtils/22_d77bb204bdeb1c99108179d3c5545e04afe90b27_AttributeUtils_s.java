 /*
  jBilling - The Enterprise Open Source Billing System
  Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde
 
  This file is part of jbilling.
 
  jbilling is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  jbilling is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.sapienter.jbilling.server.pricing.util;
 
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.pricing.db.AttributeDefinition;
 import com.sapienter.jbilling.server.pricing.strategy.PricingStrategy;
 import org.joda.time.LocalTime;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 
 /**
  * Simple utilities for parsing price model attributes.
  *
  * @author Brian Cowdery
  * @since 02/02/11
  */
 public class AttributeUtils {
 
     /**
      * Validates that all the required attributes of the given strategy are present and of the
      * correct type.
      *
      * @param attributes attribute map
      * @param strategy strategy to validate against
      * @throws SessionInternalError if attributes are missing or of an incorrect type
      */
     public static void validateAttributes(SortedMap<String, String> attributes, PricingStrategy strategy)
             throws SessionInternalError {
 
         String strategyName = strategy.getClass().getSimpleName();
         List<String> errors = new ArrayList<String>();
 
         for (AttributeDefinition definition : strategy.getAttributeDefinitions()) {
             String name = definition.getName();
             String value = attributes.get(name);
 
             // validate required attributes
             if (definition.isRequired() && (value == null || value.trim().equals(""))) {
                 errors.add(strategyName + "," + name + ",validation.error.is.required");
             }
 
             // validate attribute types
             try {
                 switch (definition.getType()) {
                     case STRING:
                         // a string is a string...
                         break;
                     case TIME:
                         parseTime(value);
                         break;
                     case INTEGER:
                         parseInteger(value);
                         break;
                     case DECIMAL:
                         parseDecimal(value);
                         break;
                 }
             } catch (SessionInternalError validationException) {
                 errors.add(strategyName + "," + name + "," + validationException.getErrorMessages()[0]);
             }
         }
 
         // throw new validation exception with complete error list
         if (!errors.isEmpty()) {
             throw new SessionInternalError(strategyName + " attributes failed validation.",
                                            errors.toArray(new String[errors.size()]));
         }
     }
 
 
     public static LocalTime getTime(Map<String, String> attributes, String name) {
         return parseTime(attributes.get(name));
     }
 
     /**
      * Parses the given value as LocalTime. If the value cannot be parsed, an exception will be thrown.
      *
      * @param value value to parse
      * @return parsed LocalTime
      * @throws SessionInternalError if value cannot be parsed as LocalTime
      */
     public static LocalTime parseTime(String value) {
         String[] time = value.split(":");
 
         if (time.length != 2)
             throw new SessionInternalError("Cannot parse attribute value '" + value + "' as a time of day.",
                                            new String[] { "validation.error.not.time.of.day" });
 
         try {
             return new LocalTime(Integer.valueOf(time[0]), Integer.valueOf(time[1]));
         } catch (NumberFormatException e) {
             throw new SessionInternalError("Cannot parse attribute value '" + value + "' as a time of day.",
                                            new String[] { "validation.error.not.time.of.day" });
         }
     }
 
     public static Integer getInteger(Map<String, String> attributes, String name) {
         return parseInteger(attributes.get(name));
     }
 
     /**
      * Parses the given value as an Integer. If the value cannot be parsed, an exception will be thrown.
      *
      * @param value value to parse
      * @return parsed integer
      * @throws SessionInternalError if value cannot be parsed as an integer
      */
     public static Integer parseInteger(String value) {
         if (value != null) {
             try {
                 return Integer.valueOf(value);
             } catch (NumberFormatException e) {
                 throw new SessionInternalError("Cannot parse attribute value '" + value + "' as an integer.",
                                                new String[] { "validation.error.not.a.integer" });
             }
         }
         return null;
     }
 
     public static BigDecimal getDecimal(Map<String, String> attributes, String name) {
         return parseDecimal(attributes.get(name));
     }
 
     /**
      * Parses the given value as a BigDecimal. If the value cannot be parsed, an exception will be thrown.
      *
      * @param value value to parse
      * @return parsed integer
      * @throws SessionInternalError if value cannot be parsed as an BigDecimal
      */
     public static BigDecimal parseDecimal(String value) {
         if (value != null) {
             try {
                return new BigDecimal(value);
             } catch (NumberFormatException e) {
                 throw new SessionInternalError("Cannot parse attribute value '" + value + "' as a decimal number.",
                                                new String[] { "validation.error.not.a.number" });
             }
         }
         return null;
     }
 
 
 }
