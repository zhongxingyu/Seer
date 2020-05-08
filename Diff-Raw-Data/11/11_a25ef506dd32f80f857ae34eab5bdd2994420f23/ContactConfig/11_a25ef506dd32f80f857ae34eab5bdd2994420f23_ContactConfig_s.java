 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.config;
 
 import org.vfny.geoserver.global.dto.ContactDTO;
 
 
 /**
  * Represents a Contact (or Party).
  * 
  * <p>
  * This is used by GeoServer to represent a contact person or organization
  * associated with the Service.
  * </p>
  * 
  * <p>
  * The configuration file represents Contact information using the following
  * XML fragment (at the time of writing):
  * </p>
  * <pre><code>
  * {ContactInformation}
  *   {ContactPersonPrimary}
  *     {ContactPerson}Chris Holmes{/ContactPerson}
  *     {ContactOrganization}TOPP{/ContactOrganization}
  *   {/ContactPersonPrimary}
  *   {ContactPosition}Computer Scientist{/ContactPosition}
  *   {ContactAddress}
  *     {AddressType}postal{/AddressType}
  *     {Address}Street addresss here{/Address}
  *     {City}New York{/City}
  *     {StateOrProvince}New York{/StateOrProvince}
  *     {PostCode}0001{/PostCode}
  *     {Country}USA{/Country}
  *   {/ContactAddress}
  *   {ContactVoiceTelephone}+1 301 283-1569{/ContactVoiceTelephone}
  *   {ContactFacsimileTelephone}+1 301 283-1569{/ContactFacsimileTelephone}
  * {/ContactInformation}
  * </code></pre>
  * 
  * <p>
  * To communicate with the running GeoServer application, represented by the
  * classes in global, the Contact information will need to be placed into the
  * ContactDTO.
  * </p>
  *
  * @author David Zwiers, Refractions Research, Inc.
 * @version $Id: ContactConfig.java,v 1.6 2004/02/18 21:29:10 jive Exp $
  */
 public class ContactConfig {
     /** The name of the contact person */
     private String contactPerson;
 
     /** The name of the organization with which the contact is affiliated. */
     private String contactOrganization;
 
     /** The position of the contact within their organization. */
     private String contactPosition;
 
     /** The type of address specified, such as postal. */
     private String addressType;
 
     /** The actual street address. */
     private String address;
 
     /** The city of the address. */
     private String addressCity;
 
     /** The state/prov. of the address. */
     private String addressState;
 
     /** The postal code for the address. */
     private String addressPostalCode;
 
     /** The country of the address. */
     private String addressCountry;
 
     /** The contact phone number. */
     private String contactVoice;
 
     /** The contact Fax number. */
     private String contactFacsimile;
 
     /** The contact email address. */
     private String contactEmail;
 
     /**
      * Default ContactConfig constructor.
      * 
      * <p>
      * Creates an empty ContactConfig object which must be setup prior to use.
      * </p>
      */
     public ContactConfig() {
     }
 
     /**
      * ContactConfig constructor.
      * 
      * <p>
      * Creates a copy of the ContactConfig specified, or returns a default
      * ContactConfig when null is provided. None of the data is cloned, as
      * String are stored in a hashtable in memory.
      * </p>
      *
      * @param dto The ContactConfig to create a copy of.
      */
     public ContactConfig(ContactDTO dto) {
         update(dto);
     }
 
     /**
      * Update the configuration to reflect the provided Data Transfer Object.
      * 
      * <p>
      * This may be used as a course grained set method, this is the entry point
      * for the live GeoServer application to update the configuration system
      * when a new XML file is loaded.
      * </p>
      *
      * @param dto Data Transfer Object representing Contact Information
      *
      * @throws NullPointerException DOCUMENT ME!
      */
     public void update(ContactDTO dto) {
         if (dto == null) {
             throw new NullPointerException(
                 "Contact Data Transfer Object required");
         }
 
         contactPerson = dto.getContactPerson();
         contactOrganization = dto.getContactOrganization();
         contactPosition = dto.getContactPosition();
         addressType = dto.getAddressType();
         address = dto.getAddress();
         addressCity = dto.getAddressCity();
         addressState = dto.getAddressState();
         addressPostalCode = dto.getAddressPostalCode();
         addressCountry = dto.getAddressCountry();
         contactVoice = dto.getContactVoice();
         contactFacsimile = dto.getContactFacsimile();
         contactEmail = dto.getContactEmail();
     }
 
     public ContactDTO toDTO() {
         ContactDTO dto = new ContactDTO();
         dto.setContactPerson(contactPerson);
         dto.setContactOrganization(contactOrganization);
         dto.setContactPosition(contactPosition);
         dto.setAddressType(addressType);
         dto.setAddress(address);
         dto.setAddressCity(addressCity);
         dto.setAddressState(addressState);
         dto.setAddressPostalCode(addressPostalCode);
         dto.setAddressCountry(addressCountry);
         dto.setContactVoice(contactVoice);
         dto.setContactFacsimile(contactFacsimile);
         dto.setContactEmail(contactEmail);
 
         return dto;
     }
 
     /**
      * Property representing the contact party (person, position or organization).
      * <p>
      * This is a derived property.
      * </p>
      * @return Contact party (person, position or organization), null if unknown
      */
     public String getContactParty(){
        if( getContactPerson() != null ){
             return getContactPerson(); // ie Chris Holmes 
         }
        if( getContactPosition() != null ){
             return getContactPosition(); // ie Lead Developer 
         }        
        if( getContactOrganization() != null ){
             return getContactOrganization(); // ie TOPP 
         }        
         return null;
     }
     /**
      * getAddress purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public String getAddress() {
         return address;
     }
 
     /**
      * getAddressCity purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public String getAddressCity() {
         return addressCity;
     }
 
     /**
      * getAddressCountry purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public String getAddressCountry() {
         return addressCountry;
     }
 
     /**
      * getAddressPostalCode purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public String getAddressPostalCode() {
         return addressPostalCode;
     }
 
     /**
      * getAddressState purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public String getAddressState() {
         return addressState;
     }
 
     /**
      * getAddressType purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public String getAddressType() {
         return addressType;
     }
 
     /**
      * getContactEmail purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public String getContactEmail() {
         return contactEmail;
     }
 
     /**
      * getContactFacsimile purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public String getContactFacsimile() {
         return contactFacsimile;
     }
 
     /**
      * getContactOrganization purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public String getContactOrganization() {
         return contactOrganization;
     }
 
     /**
      * getContactPerson purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public String getContactPerson() {
         return contactPerson;
     }
 
     /**
      * getContactPosition purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public String getContactPosition() {
         return contactPosition;
     }
 
     /**
      * getContactVoice purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public String getContactVoice() {
         return contactVoice;
     }
 
     /**
      * setAddress purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setAddress(String string) {
         if (string != null) {
             address = string;
         }
     }
 
     /**
      * setAddressCity purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setAddressCity(String string) {
         if (string != null) {
             addressCity = string;
         }
     }
 
     /**
      * setAddressCountry purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setAddressCountry(String string) {
         if (string != null) {
             addressCountry = string;
         }
     }
 
     /**
      * setAddressPostalCode purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setAddressPostalCode(String string) {
         if (string != null) {
             addressPostalCode = string;
         }
     }
 
     /**
      * setAddressState purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setAddressState(String string) {
         if (string != null) {
             addressState = string;
         }
     }
 
     /**
      * setAddressType purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setAddressType(String string) {
         if (string != null) {
             addressType = string;
         }
     }
 
     /**
      * setContactEmail purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setContactEmail(String string) {
         if (string != null) {
             contactEmail = string;
         }
     }
 
     /**
      * setContactFacsimile purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setContactFacsimile(String string) {
         if (string != null) {
             contactFacsimile = string;
         }
     }
 
     /**
      * setContactOrganization purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setContactOrganization(String string) {
         if (string != null) {
             contactOrganization = string;
         }
     }
 
     /**
      * setContactPerson purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setContactPerson(String string) {
         if (string != null) {
             contactPerson = string;
         }
     }
 
     /**
      * setContactPosition purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setContactPosition(String string) {
         if (string != null) {
             contactPosition = string;
         }
     }
 
     /**
      * setContactVoice purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setContactVoice(String string) {
         if (string != null) {
             contactVoice = string;
         }
     }
 }
