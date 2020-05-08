 package org.jz.domain;
 
 /**
  * @author Kristian Rosenvold
  */
public class PersonCustomer
    extends Customer
{
 
     private final PersonName personName;
 
     public PersonCustomer( PersonName personName, CustomerId customerId )
     {
        super( customerId );
         this.personName = personName;
     }
 
     public PersonName getPersonName()
     {
         return personName;
     }
 
     @Override
     public String getDisplayName()
     {
         return personName.getFullName();
     }
 
    public String getFullName()
    {
         return personName.getFullName();
     }
 }
