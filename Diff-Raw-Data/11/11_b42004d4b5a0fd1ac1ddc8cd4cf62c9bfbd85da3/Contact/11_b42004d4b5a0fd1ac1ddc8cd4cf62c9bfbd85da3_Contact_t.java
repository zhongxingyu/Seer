 package com.esiea.core;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Contact {
 
     private String name;
     private String surname;
     private String emails;
     private String phones;
     private Date birthday;
     private boolean actif;
 
     public Contact(String nom, String prenom, String email, String phone, String brithday) {
         this.name = nom;
         this.surname = prenom;
         this.emails = email;
         this.phones = phone;
         this.actif = true;
         try {
            this.birthday = new SimpleDateFormat("dd-MM-yyyy").parse(brithday);
         } catch (ParseException ex) {
             Logger.getLogger(Contact.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public Contact(String nom, String prenom, String email, String phone, String brithday, boolean actif) {
         this.name = nom;
         this.surname = prenom;
         this.emails = email;
         this.phones = phone;
         this.actif = actif;
         try {
            this.birthday = new SimpleDateFormat("dd-MM-yyyy").parse(brithday);
         } catch (ParseException ex) {
             Logger.getLogger(Contact.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getSurname() {
         return surname;
     }
 
     public void setSurname(String surname) {
         this.surname = surname;
     }
 
     public String getEmails() {
         return emails;
     }
 
     public void setEmails(String emails) {
         this.emails = emails;
     }
 
     public String getPhones() {
         return phones;
     }
 
     public void setPhones(String phones) {
         this.phones = phones;
     }
 
     public String getBirthday() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
         String formattedDate = formatter.format(birthday);
         return formattedDate;
     }
         public Date getBirthdayDate() {
         return birthday;
     }
 
     public void setBirthday(String birthday) {
         try {
            Date date = new SimpleDateFormat("dd-MM-yyyy").parse(birthday);
             this.birthday = date;
         } catch (ParseException ex) {
             Logger.getLogger(Contact.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public boolean isActif() {
         return actif;
     }
 
     public void setActif(boolean actif) {
         this.actif = actif;
     }
 
     public boolean isValideEmail(String str) {
         Pattern p = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$");
         Matcher m = p.matcher(str.toUpperCase());
         return m.matches();
     }
 
     public boolean isValidePhone(String str) {
         Pattern p = Pattern.compile("(\\+[0-9]{3}( [0-9][0-9])+)|([0-9]+)");
         Matcher m = p.matcher(str.toUpperCase());
         return m.matches();
     }
 
     public void modifyContact(String nom, String prenom, String email, String phone, String birthday) {
         this.name = nom;
         this.surname = prenom;
         this.emails = email;
         this.phones = phone;
         setBirthday(birthday);
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Contact other = (Contact) obj;
         boolean isSameContact = true;
         isSameContact = isSameContact && name.equals(other.getName());
         isSameContact = isSameContact && surname.equals(other.getSurname());
         isSameContact = isSameContact && emails.equals(other.getEmails());
         isSameContact = isSameContact && birthday.equals(other.getBirthdayDate());
         isSameContact = isSameContact && phones.equals(other.getPhones());
         return isSameContact;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
         hash = 89 * hash + (this.surname != null ? this.surname.hashCode() : 0);
         hash = 89 * hash + (this.emails != null ? this.emails.hashCode() : 0);
         hash = 89 * hash + (this.phones != null ? this.phones.hashCode() : 0);
         hash = 89 * hash + (this.birthday != null ? this.birthday.hashCode() : 0);
         return hash;
     }
 }
