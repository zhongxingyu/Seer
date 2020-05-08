 package fr.esiea.esieaddress.service.validation.csv;
 
 import java.util.Date;
 
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
public class CsvContact {
 
     private String id;
     private String lastname;
     private String firstname;
     private String email;
     private Date dateOfBirth;
     private boolean actif;
     private String numero;
     private String street;
     private String postalCode;
     private String city;
 
    public CsvContact() {}
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getLastname() {
         return lastname;
     }
 
     public void setLastname(String lastname) {
         this.lastname = lastname;
     }
 
     public String getFirstname() {
         return firstname;
     }
 
     public void setFirstname(String firstname) {
         this.firstname = firstname;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public Date getDateOfBirth() {
         return dateOfBirth;
     }
 
     public void setDateOfBirth(Date dateOfBirth) {
         this.dateOfBirth = dateOfBirth;
     }
 
     public boolean isActif() {
         return actif;
     }
 
     public void setActif(boolean actif) {
         this.actif = actif;
     }
 
     public String getNumero() {
         return numero;
     }
 
     public void setNumero(String numero) {
         this.numero = numero;
     }
 
     public String getStreet() {
         return street;
     }
 
     public void setStreet(String street) {
         this.street = street;
     }
 
     public String getPostalCode() {
         return postalCode;
     }
 
     public void setPostalCode(String postalCode) {
         this.postalCode = postalCode;
     }
 
     public String getCity() {
         return city;
     }
 
     public void setCity(String city) {
         this.city = city;
     }
 
     @Override
     public String toString() {
        return "CsvContact{" +
                 "id='" + id + '\'' +
                 ", lastname='" + lastname + '\'' +
                 ", firstname='" + firstname + '\'' +
                 ", email='" + email + '\'' +
                 ", dateOfBirth=" + dateOfBirth +
                 ", actif=" + actif +
                 ", numero='" + numero + '\'' +
                 ", street='" + street + '\'' +
                 ", postalCode='" + postalCode + '\'' +
                 ", city='" + city + '\'' +
                 "}\n";
     }
 }
