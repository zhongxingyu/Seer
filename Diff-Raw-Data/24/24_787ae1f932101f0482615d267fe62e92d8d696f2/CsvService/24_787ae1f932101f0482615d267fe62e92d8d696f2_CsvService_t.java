 package fr.esiea.esieaddress.service.validation.csv;
 
 import au.com.bytecode.opencsv.CSVReader;
 import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
 import au.com.bytecode.opencsv.bean.CsvToBean;
 import fr.esiea.esieaddress.model.Address;
 import fr.esiea.esieaddress.model.Contact;
 import org.springframework.stereotype.Service;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.*;
 
 /**
  * Copyright (c) 2013 ESIEA M. Labusquiere D. Déïs
  * <p/>
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * <p/>
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * <p/>
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 @Service
 public class CsvService {
     private final static String[] outlookTemplate;
 
     static  {
         outlookTemplate = new String[88];
         outlookTemplate[0] = "firstname";
         outlookTemplate[2] = "lastname";
 //      outlookTemplate[8] = "dateOfBirth";
         outlookTemplate[14] = "email";
         //outlookTemplate[0] = "numero";
         outlookTemplate[24] = "street";
         outlookTemplate[30] = "postalCode";
         outlookTemplate[28] = "city";
 
     }
 
 
     private String[] actualTemplate = pickOutlookTemplate();
 
     public Collection<Contact> ReadContactCSV(Reader input)    {
         ColumnPositionMappingStrategy strat = new ColumnPositionMappingStrategy();
 
        strat.setType(CsvContact.class);
         strat.setColumnMapping(actualTemplate);
 
         CsvToBean csv = new CsvToBean();
         CSVReader reader = new CSVReader(input, ',');
         //need to pass the header
         try {
             reader.readNext();
         } catch (IOException e) {
             e.printStackTrace();
             //TODO
         }
        List<CsvContact> csvContacts= csv.parse(strat, reader);
        
        
         List<Contact> contacts = contactCsvToContact(csvContacts);
         
        
 
         return contacts;
 
     }
 
    private List<Contact> contactCsvToContact(List<CsvContact> csvContacts) {
 
         List<Contact> contacts = new ArrayList<>();
 
        for(CsvContact csvContact:csvContacts)  {
            Address address = new Address();
            Contact contact = new Contact();
 
            address.setCity(csvContact.getCity());
            address.setNumero(csvContact.getNumero());
            address.setPostalCode(csvContact.getPostalCode());
            address.setStreet(csvContact.getStreet());
 
            contact.setActif(true);
            contact.setDateOfBirth(csvContact.getDateOfBirth());
            contact.setEmail(csvContact.getEmail());
            contact.setFirstname(csvContact.getFirstname());
            contact.setLastname(csvContact.getLastname());
 
            contact.addAddress("personnel",address);
            contacts.add(contact);
         }
 
         return contacts;
     }
 
     protected String[] pickOutlookTemplate() {
         return outlookTemplate;
     }
 
 
 }
