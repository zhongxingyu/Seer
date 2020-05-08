 /**
  * This file is part of libRibbonData library (check README).
  * Copyright (C) 2012-2013 Stanislav Nepochatov
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 **/
 
 package MessageClasses;
 
 import java.util.Arrays;
 
 /**
  * Message property class. This properties may control 
  * message processing.
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  */
 public class MessageProperty extends Generic.CsvElder {
     
     /**
      * Prefix of the property.
      * Displays type of property.
      * @deprecated this filed unsatisfied new specs.<br>
      * Use <code>TYPE</code> field.
      * @see #TYPE
      */
     public MessagePropertyTypes PROPERTY_PREFIX;
     
     /**
      * Type of the property.
      */
     public String TYPE;
     
     /**
      * Owner of this property.
      */
     public String USER;
     
     /**
      * Short decription message for property (one line).
      */
     public String TEXT_MESSAGE;
     
     /**
      * Date of marking.
      */
     public String DATE;
     
     /**
      * Display property with supported type.
      */
     private Boolean TYPE_SUPPORTED;
     
     /**
      * Types of message properties.
      */
     public static class Types {
         
         /**
          * Type list storage.
          */
         private static java.util.ArrayList<String> typeList = new java.util.ArrayList<>(Arrays.asList(new String[] {
         
         /** Generic properties **/    
         "URGENT",                       //Message was marked as urgent
         "MARK_USER",                    //User was marked message with custom text message
         "MARK_ADM",                     //Admin was marked message with custom text message
         "PROCESSING_FORBIDDEN",         //User forbid any processing of message
         "NIGHT_EMBARGO",                //Message export will be performed at night
         
         /** System properties **/
         "CORRUPTED_AND_RESTORED",       //Message lost one of the link and it's link was restored by system
         "CORRUPTED_AND_LOST",           //Message lost all links and system cann't restore message
         "RELOCATED",                    //Message was relocated during deletion of directory
         }));
         
         /**
          * Register types from string array.
          * @param givenTypes types to register;
          */
         public static void registerType(String[] givenTypes) {
             Types.typeList.addAll(Arrays.asList(givenTypes));
         }
         
         /**
          * Register type if it doesn't exist in <code>typeList</code>.
          * @param givenType type to register;
          */
        public static void registerTypeIfNotExist(String givenType) {
             if(!Types.typeList.contains(givenType)) {
                 Types.typeList.add(givenType);
             }
         }
         
         /**
          * Find out if type existed.
          * @param givenType type to check;
          * @return true if type registered / false if not; 
          */
         public static Boolean isTypeRegistered(String givenType) {
             return Types.typeList.contains(typeList);
         }
         
     }
     
     /**
      * Empty configuration constructor.
      */
     public MessageProperty() {
         this.baseCount = 4;
         this.currentFormat = csvFormatType.SimpleCsv;
     }
     
     /**
      * Default constructor from csv form.
      * @param givenCsv given csv line;
      */
     public MessageProperty(String givenCsv) {
         this();
         java.util.ArrayList<String[]> parsedStruct = Generic.CsvFormat.fromCsv(this, givenCsv);
         String[] baseArray = parsedStruct.get(0);
         //this.PROPERTY_PREFIX = MessagePropertyTypes.valueOf(baseArray[0]);
         this.TYPE = baseArray[0];
         this.USER = baseArray[1];
         this.TEXT_MESSAGE = baseArray[2];
         this.DATE = baseArray[3];
         this.TYPE_SUPPORTED = Types.isTypeRegistered(TYPE);
     }
     
     /**
      * All parametrick constructor for internal usage.
      * @param givenType
      * @param givenUser
      * @param givenMessage
      * @param givenDate 
      */
     public MessageProperty(String givenType, String givenUser, String givenMessage, String givenDate) {
         TYPE = givenType;
         USER = givenUser;
         TEXT_MESSAGE = givenMessage;
         DATE = givenDate;
         this.TYPE_SUPPORTED = Types.isTypeRegistered(TYPE);
     }
     
     public Boolean isTypeSafe() {
         return TYPE_SUPPORTED;
     }
     
     @Override
     public String toCsv() {
        return this.PROPERTY_PREFIX + ",{" + this.USER + "},{" + this.TEXT_MESSAGE + "}," + this.DATE;
     }
 }
