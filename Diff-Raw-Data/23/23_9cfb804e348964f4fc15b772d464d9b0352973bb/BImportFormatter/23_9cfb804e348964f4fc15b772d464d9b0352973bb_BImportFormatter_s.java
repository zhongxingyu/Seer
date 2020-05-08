 /*
  * Metro allows customers from any affiliate library to join any other member library.
  *    Copyright (C) 2013  Andrew Nisbet
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
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import mecard.util.BImportDBFields;
 import mecard.util.City;
 import mecard.util.DateComparer;
 import mecard.util.Phone;
 
 /**
  * This class creates the data and header files.
  * @author metro
  */
 public class BImportFormatter
 {
     public final static String BORROWER_TABLE = "borrower";
     public final static String BORROWER_PHONE_TABLE = "borrower_phone"; 
     public final static String BORROWER_ADDRESS_TABLE = "borrower_address"; 
     public final static String BORROWER_BARCODE_TABLE = "borrower_barcode";
     
     public static class Builder
     {
         private final String headerName;
         private final String dataName;
         private String name;
         private String expiry;
         private String pin;
         private String phoneType = "h-noTC";
         private String phone;
         private String address1;
         private String address2 = "";
         private String city;
         private String postalCode;
         private String email;
         private String barcode;
         private String emailName;
         
         public Builder(String headerPath, String dataPath)
         {
             this.headerName = headerPath;
             this.dataName   = dataPath;
         }
         
         public Builder emailName(String e)
         {
             this.emailName = e;
             return this;
         }
         
         public Builder name(String n)
         {
             this.name = n;
             return this;
         }
         
         public Builder barcode(String b)
         {
             this.barcode = b;
             return this;
         }
         
         public Builder expire(String e)
         {
             this.expiry = e;
             return this;
         }
         
         public Builder pin(String p)
         {
             this.pin = p;
             return this;
         }
         
         public Builder pType(String p)
         {
             this.phoneType = p;
             return this;
         }
         
         public Builder pNumber(String p)
         {
             this.phone = p;
             return this;
         }
         
         public Builder address1(String a)
         {
             this.address1 = a;
             return this;
         }
         
         public Builder city(String c)
         {
             this.city = c;
             return this;
         }
           
         public Builder postalCode(String p)
         {
             this.postalCode = p;
             return this;
         }
         
         public Builder email(String e)
         {
             this.email = e;
             return this;
         }
         
         public BImportFormatter build()
         {
             return new BImportFormatter(this);
         }
     }
     
     private BImportFormatter(Builder b)
     {
         StringBuilder headerContent = new StringBuilder();
         StringBuilder dataContent   = new StringBuilder();
         File header = createFile(b.headerName);
         File data   = createFile(b.dataName);
         // These fields need to be formatted
         String expiryDate = DateComparer.convertToConfigDate(b.expiry);
         // Phone
         String phone = Phone.formatPhone(b.phone);
         // City needs to be mapped.
         String cityCode = City.getCity(b.city);
         
         // Table borrower
         headerContent.append("x- "); // add or modify if exists
         headerContent.append(BORROWER_TABLE + ": "); // add or modify if exists
         headerContent.append(BImportDBFields.SECOND_ID + "; ");
         headerContent.append(BImportDBFields.NAME + "; ");
         headerContent.append(BImportDBFields.EXPIRY + "; ");
        headerContent.append(BImportDBFields.PIN + "\n\r");
             
         dataContent.append("M- "); // add or modify if exists
         dataContent.append(BORROWER_TABLE + ": "); // add or modify if exists
         dataContent.append(b.barcode + "; ");
         dataContent.append(b.name + "; ");
         dataContent.append(expiryDate + "; ");    // see above for computation
        dataContent.append(b.pin + "\n\r");
         
         // Table borrower_phone
         headerContent.append(BORROWER_PHONE_TABLE + ": "); // add or modify if exists
         headerContent.append(BImportDBFields.PHONE_TYPE + "; ");
        headerContent.append(BImportDBFields.PHONE_NUMBER + "\n\r");
         
         dataContent.append(BORROWER_PHONE_TABLE + ": "); // add or modify if exists
         dataContent.append(b.phoneType + "; ");
        dataContent.append(phone + "\n\r"); // see computation above.
         
         // Table borrower_address
         headerContent.append(BORROWER_ADDRESS_TABLE + ": "); // add or modify if exists
         headerContent.append(BImportDBFields.ADDRESS_1 + "; ");
         headerContent.append(BImportDBFields.ADDRESS_2 + "; ");
         headerContent.append(BImportDBFields.CITY + "; ");
         headerContent.append(BImportDBFields.POSTAL_CODE + "; ");
         headerContent.append(BImportDBFields.EMAIL_NAME + "; ");
        headerContent.append(BImportDBFields.EMAIL_ADDRESS + "\n\r");
         
         dataContent.append(BORROWER_ADDRESS_TABLE + ": "); // add or modify if exists
         dataContent.append(b.address1 + "; ");
         dataContent.append(b.address2 + "; ");
         dataContent.append(cityCode + "; ");
         dataContent.append(b.postalCode + "; ");
         dataContent.append(b.emailName + "; ");
        dataContent.append(b.email + "\n\r");
         
         // Table borrower_barcode
         headerContent.append(BORROWER_BARCODE_TABLE + ": "); // add or modify if exists
        headerContent.append(BImportDBFields.BARCODE + "\n\r");
         
         dataContent.append(BORROWER_BARCODE_TABLE + ": "); // add or modify if exists
        dataContent.append(b.barcode + "\n\r");
         
         writeContent(headerContent, header);
         writeContent(dataContent, data);     
     }
     
     private void writeContent(StringBuilder content, File file)
     {
         BufferedWriter bWriter;
         try
         {
             // write the builder contents to the file with the correct switches.
             bWriter = new BufferedWriter(new FileWriter(file));
             bWriter.write(content.toString());
             bWriter.close();
         }
         catch (IOException ex)
         {
             String msg = "unable to create '"+file.getName()+"' file.";
             Logger.getLogger(BImportFormatter.class.getName()).log(Level.SEVERE, msg, ex);
         }
     }
     
     private File createFile(String name)
     {
         File file = new File(name);
         if (file.exists())
         {
             file.delete();
         }
         // create a new one.
         file = new File(name);
         return file;
     }
 }
