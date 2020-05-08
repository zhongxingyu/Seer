 /*
  * Metro allows customers from any affiliate library to join any other member library.
  *    Copyright (C) 2013  Edmonton Public Library
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301, USA.
  *
  */
 package mecard.customer;
 
 import mecard.config.CustomerFieldTypes;
 import java.util.List;
 import mecard.util.Address;
 
 /**
  *
  * @author Andrew Nisbet <anisbet@epl.ca>
  */
 public class SIPFormatter implements CustomerFormatter
 {
 
     public SIPFormatter()
     {
     }
 
     @Override
     public Customer getCustomer(String s)
     {
 //        System.out.println(">>>>> SIP_CUSTOMER:'"+s+"'");
         Customer customer = new Customer();
         // parse the string appart.
 //        sent:63                               AO|AA21221012345678|AD64058|AY0AZF374
 //        recv:64YYYY      Y   00020130606    115820000000000000000100000000AO|AA21221012345678|AEBilly, Balzac|AQEPLMNA|BZ0025|CA0041|CB0040|BLY|CQY|BV 12.00|BD7 Sir Winston Churchill Square Edmonton, AB T5J 2V4|BEilsteam@epl.ca|BHUSD|PA20140321    235900|PD20050303|PCEPL-THREE|PFM|DB$0.00|DM$0.00|AFUser BLOCKED|AY0AZACC6
 //
 //        Response code:64
 //        Patron Information Response
 //          (O) Patron Status : YYYY      Y   
 //                Charge Privileges Denied :          Y
 //                Renewal Privileges Denied :         Y
 //                Recall Privileges Denied :          Y
 //                Hold Privileges Denied:             Y
 //                Card Reported Lost :                 
 //                Too Many Items Charged :             
 //                Too many Items Overdue :             
 //                Too Many Renewals :                  
 //                Too Many Claims Of Items Returned :  
 //                Too Many Items Lost :                
 //                Excessive Outstanding Fines :       Y
 //                Excessive Outstanding Fees :         
 //                Recall Overdue :                     
 //                Too Many Items Billed :              
 //          (F) Language:                      000
 //          (F) Transaction Date:              20130606    115820
 //          (F) Hold Items Count:              0000
 //          (F) Overdue Items Count:           0000
 //          (F) Charged Items Count:           0000
 //          (F) Fine Items Count:              0001
 //          (F) Recall Items Count:            0000
 //          (F) Unavailable Hold Items Count:  0000
 //          (R) Institution Id:
 //          (R) Patron Identifier:21221012345678
 //          (R) Personal Name:Billy, Balzac
 //          (R) Patron Library:EPLMNA
 //          (O) Hold Items Limit:0025:
 //          (O) Overdue Items Limit:0041:
 //          (O) Charged Items Limit:0040:
 //          (O) Valid Patron:Y:
 //          (O) Valid Patron Password:Y:
 //          (O) Fee Amount: 12.00
 //          (O) Home Address:7 Sir Winston Churchill Square Edmonton, AB T5J 2V4
 //          (O) E-mail Address:ilsteam@epl.ca
 //          (O) Currency Type:USD:
 //          (O) Expiration Date:20140321    235900
 //          (O) Birth date:20050303
 //          (O) Profile:EPL-THREE
 //          (O) User Category 2:M
 //          (O) DB INVALID DATA CODE FOR THIS MESSAGE:$0.00
 //          (O) DM INVALID DATA CODE FOR THIS MESSAGE:$0.00
 //          (O) Screen Message:User BLOCKED
 //          (R) Sequence Number : 0 :  matches what was sent
 //          (R) Checksum : ACC6 : Checksum OK
         String[] sipResponseFields = s.split("\\|");
         for (String sipField : sipResponseFields)
         {
             // take off the first two characters, that's the name of the field.
             // like 'AA' is the user's id so 'AA21221012345678'.
             if (sipField.length() == 0)
             {
                 continue;
             }
             String fieldName = sipField.substring(0, 2);
             // the rest of the field is the value.
             String fieldValue = sipField.substring(2);
             CustomerFieldTypes fieldType = this.translateToCustomerField(fieldName);
             if (fieldType != null)
             {
                 // This value is used because sip returns the "lastname, fname"
                 // not individual lastname firstname. Using the USER_PREFERRED_NAME
                 // is just a convension used here.
                 if (fieldType == CustomerFieldTypes.PREFEREDNAME)
                 {
                     customer.setName(sipField.substring(2));
                 } 
                 else if (fieldType == CustomerFieldTypes.STREET)
                 {
                     // special STREET in this context comes with everything
                     Address address = new Address(sipField.substring(2), true);
                     customer.set(CustomerFieldTypes.STREET, address.getStreet());
                     customer.set(CustomerFieldTypes.CITY, address.getCity());
                     customer.set(CustomerFieldTypes.PROVINCE, address.getProvince());
                     customer.set(CustomerFieldTypes.POSTALCODE, address.getPostalCode());
                     customer.set(CustomerFieldTypes.PHONE, address.getPhone());
                 } 
                 else if (fieldType == CustomerFieldTypes.PRIVILEGE_EXPIRES)
                 {
                     // PA20140321    235900
                     String strippedDate = fieldValue.substring(0, 8);
                     customer.set(CustomerFieldTypes.PRIVILEGE_EXPIRES, strippedDate);
                 } 
                 else if (fieldType == CustomerFieldTypes.DOB)
                 {
                     // PD20050303
                     if (fieldValue.isEmpty() == false)
                     {
                         // the site can subclass MeCardPolicy object to test 
                         // a customer for adult with profile as an alternate.
                         customer.set(CustomerFieldTypes.DOB, fieldValue);
                     }
                 } 
                 else // direct translation, no conversion of data requred.
                 {
                     customer.set(fieldType, fieldValue);
                 }
             }
         }
         return customer;
     }
 
     /**
      *
      * @param string
      * @return CustomerField that matches a customer field, or null.
      */
     private CustomerFieldTypes translateToCustomerField(String userFieldValue)
     {
         if (userFieldValue.equals("AA"))
         {
             return CustomerFieldTypes.ID;
         } 
         else if (userFieldValue.equals("AE"))
         {
             return CustomerFieldTypes.PREFEREDNAME; // last name, first name.
         }
         else if (userFieldValue.equals("AF"))
         {
             return CustomerFieldTypes.RESERVED; // Message from SIP2 Server.
         }
         else if (userFieldValue.equals("BD"))
         {
             return CustomerFieldTypes.STREET; // Complete address break it up.
         } 
         else if (userFieldValue.equals("BE"))
         {
             return CustomerFieldTypes.EMAIL;
         }
         else if (userFieldValue.equals("BF"))
         {
             return CustomerFieldTypes.PHONE; // EPL throws this on the end of the address but St. A uses correct field label.
         } 
         else if (userFieldValue.equals("PA"))
         {
             return CustomerFieldTypes.PRIVILEGE_EXPIRES;
         } 
         else if (userFieldValue.equals("PE"))
         {
             return CustomerFieldTypes.PRIVILEGE_EXPIRES;
         }
         else if (userFieldValue.equals("PD"))
         {
             return CustomerFieldTypes.DOB;
         } // PCEPL-THREE|PFM|DB$0.00|DM$0.00|AFUser BLOCKED|
         else if (userFieldValue.equals("PF"))
         {
             return CustomerFieldTypes.SEX;
         } 
         else
         {
             return null;
         }
     }
 
     @Override
     public Customer getCustomer(List<String> s)
     {
         return this.getCustomer(s.get(0));
     }
 
     /**
      *
      * @param c the value of c
      * @return the List<String>
      */
     @Override
     public List<String> setCustomer(Customer c)
     {
         throw new UnsupportedOperationException("SIP does not support customer creation.");
     }
 }
