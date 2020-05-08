 /**
  * Copyright (C) Kamosoft 2010
  */
 package com.kamosoft.happycontacts.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 /**
  * @author tom
  *
  */
 public class ContactFeast
     implements Serializable
 {
     /**
      * 
      */
     private static final long serialVersionUID = 5984664253829892060L;
 
     private String nameDay;
 
     private String contactName;
 
     private Long contactId;
 
    private ArrayList<String> phones;
 
    private ArrayList<String> emails;
 
     /** for birthday only */
     private String birthdayDate;
 
     /** for birthday only */
     private String birthdayYear;
 
     public ContactFeast( String nameDay, String contactName )
     {
         this.nameDay = nameDay;
         this.contactName = contactName;
     }
 
     @Override
     public String toString()
     {
         return "ContactFeast [birthdayDate=" + birthdayDate + ", birthdayYear=" + birthdayYear + ", contactId="
             + contactId + ", contactName=" + contactName + ", emails=" + emails + ", nameDay=" + nameDay + ", phones="
             + phones + "]";
     }
 
     public boolean isContactable()
     {
         return ( hasEmail() || hasPhone() );
     }
 
     public boolean hasEmail()
     {
         return getEmails() != null && !getEmails().isEmpty();
     }
 
     public boolean hasPhone()
     {
         return getPhones() != null && !getPhones().isEmpty();
     }
 
     public void addPhone( String phone )
     {
        if ( phones == null )
        {
            phones = new ArrayList<String>();
        }
         phones.add( phone );
     }
 
     public void addEmail( String email )
     {
        if ( emails == null )
        {
            emails = new ArrayList<String>();
        }
         emails.add( email );
     }
 
     public ArrayList<String> getPhones()
     {
         return phones;
     }
 
     public void setPhones( ArrayList<String> phones )
     {
         this.phones = phones;
     }
 
     public ArrayList<String> getEmails()
     {
         return emails;
     }
 
     public void setEmails( ArrayList<String> emails )
     {
         this.emails = emails;
     }
 
     public String getContactName()
     {
         return contactName;
     }
 
     public void setContactName( String contactName )
     {
         this.contactName = contactName;
     }
 
     public void setContactId( Long contactId )
     {
         this.contactId = contactId;
     }
 
     public Long getContactId()
     {
         return contactId;
     }
 
     /**
      * @return the nameDay
      */
     public String getNameDay()
     {
         return nameDay;
     }
 
     /**
      * @param nameDay the nameDay to set
      */
     public void setNameDay( String nameDay )
     {
         this.nameDay = nameDay;
     }
 
     /**
      * @param birthday the birthday to set
      */
     public void setBirthdayDate( String birthday )
     {
         this.birthdayDate = birthday;
     }
 
     /**
      * @return the birthday
      */
     public String getBirthdayDate()
     {
         return birthdayDate;
     }
 
     /**
      * @param birthyear the birthyear to set
      */
     public void setBirthdayYear( String birthyear )
     {
         this.birthdayYear = birthyear;
     }
 
     /**
      * @return the birthyear
      */
     public String getBirthdayYear()
     {
         return birthdayYear;
     }
 }
