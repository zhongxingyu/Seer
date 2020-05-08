 /*
  * Created On:  Sunday December 17, 2006 14:53
  */
 package com.thinkparity.codebase.model.user;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 
 /**
  * <b>Title:</b>thinkParity User VCard<br>
  * <b>Description:</b>An abstraction of a user object's interface into their
  * virtual card.<br>
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 @XStreamAlias("v-card")
 public abstract class UserVCard {
 
     private String address;
 
     private String city;
 
     private String country;
 
     private String language;
 
     private String mobilePhone;
 
     private String name;
 
     private String organization;
 
     private String organizationCity;
 
     private String organizationCountry;
 
     private String organizationPhone;
 
     private String organizationPostalCode;
 
     private String organizationProvince;
 
    private String organziationAddress;
 
     private String phone;
 
     private String postalCode;
 
     private String province;
 
     private String timeZone;
 
     private String title;
 
     /**
      * Create UserVCard.
      *
      */
     protected UserVCard() {
         super();
     }
 
     /**
      * Obtain address.
      *
      * @return A String.
      */
     public String getAddress() {
         return address;
     }
 
     /**
      * Obtain the city.
      * 
      * @return The city <code>String</code>.
      */
     public String getCity() {
         return city;
     }
 
     /**
      * Obtain the country.
      * 
      * @return The country <code>String</code>.
      */
     public String getCountry() {
         return country;
     }
 
     /**
      * Obtain language.
      *
      * @return A String.
      */
     public String getLanguage() {
         return language;
     }
 
     /**
      * Obtain the mobile phone.
      * 
      * @return The mobile phone <code>String</code>.
      */
     public String getMobilePhone() {
         return mobilePhone;
     }
 
     /**
      * Obtain name.
      *
      * @return A String.
      */
     public String getName() {
         return name;
     }
 
     /**
      * Obtain the organization.
      * 
      * @return The organization <code>String</code>.
      */
     public String getOrganization() {
         return organization;
     }
 
     /**
      * Obtain organizationCity.
      *
      * @return A String.
      */
     public String getOrganizationCity() {
         return organizationCity;
     }
 
     /**
      * Obtain organizationCountry.
      *
      * @return A String.
      */
     public String getOrganizationCountry() {
         return organizationCountry;
     }
 
     /**
      * Obtain organizationPhone.
      *
      * @return A String.
      */
     public String getOrganizationPhone() {
         return organizationPhone;
     }
 
     /**
      * Obtain organizationPostalCode.
      *
      * @return A String.
      */
     public String getOrganizationPostalCode() {
         return organizationPostalCode;
     }
 
     /**
      * Obtain organizationProvince.
      *
      * @return A String.
      */
     public String getOrganizationProvince() {
         return organizationProvince;
     }
 
     /**
     * Obtain organziationAddress.
      *
      * @return A String.
      */
    public String getOrganziationAddress() {
        return organziationAddress;
     }
 
     /**
      * Obtain the phone number.
      * 
      * @return The phone number <code>String</code>.
      */
     public String getPhone() {
         return phone;
     }
 
     /**
      * Obtain postalCode.
      *
      * @return A String.
      */
     public String getPostalCode() {
         return postalCode;
     }
 
     /**
      * Obtain province.
      *
      * @return A String.
      */
     public String getProvince() {
         return province;
     }
 
     /**
      * Obtain timeZone.
      *
      * @return A String.
      */
     public String getTimeZone() {
         return timeZone;
     }
 
     /**
      * Obtain the title.
      * 
      * @return The title <code>String</code>.
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * Determine if the city is set.
      * 
      * @return True if the city is set.
      */
     public Boolean isSetCity() {
         return isSet(city);
     }
 
     /**
      * Determine if the mobile phone is set.
      * 
      * @return True if the mobile phone is set.
      */
     public Boolean isSetMobilePhone() {
         return isSet(mobilePhone);
     }
 
     /**
      * Determine if the city is set.
      * 
      * @return True if the city is set.
      */
     public Boolean isSetOrganizationCity() {
         return isSet(organizationCity);
     }
 
     /**
      * Determine if the city is set.
      * 
      * @return True if the city is set.
      */
     public Boolean isSetOrganizationCountry() {
         return isSet(organizationCountry);
     }
 
     /**
      * Determine if the city is set.
      * 
      * @return True if the city is set.
      */
     public Boolean isSetOrganizationPostalCode() {
         return isSet(organizationPostalCode);
     }
 
     /**
      * Determine if the city is set.
      * 
      * @return True if the city is set.
      */
     public Boolean isSetOrganizationProvince() {
         return isSet(organizationProvince);
     }
 
     /**
      * Determine if the mobile phone is set.
      * 
      * @return True if the mobile phone is set.
      */
     public Boolean isSetPhone() {
         return isSet(phone);
     }
 
     /**
      * Set address.
      *
      * @param address
      *		A String.
      */
     public void setAddress(final String address) {
         this.address = address;
     }
 
     /**
      * Set city.
      *
      * @param city
      *		A String.
      */
     public void setCity(final String city) {
         this.city = city;
     }
 
     /**
      * Set country.
      *
      * @param country
      *		A String.
      */
     public void setCountry(final String country) {
         this.country = country;
     }
 
     /**
      * Set language.
      *
      * @param language
      *		A String.
      */
     public void setLanguage(final String language) {
         this.language = language;
     }
 
     /**
      * Set mobilePhone.
      *
      * @param mobilePhone
      *		A String.
      */
     public void setMobilePhone(final String mobilePhone) {
         this.mobilePhone = mobilePhone;
     }
 
     /**
      * Set the vcard name.
      * 
      * @param given
      *            The given name <code>String</code>.
      * @param middle
      *            The middle name <code>String</code>.
      * @param family
      *            The family name <code>String</code>.
      */
     public void setName(final String name) {
         this.name = name;
     }
 
     /**
      * Set organization.
      *
      * @param organization
      *		A String.
      */
     public void setOrganization(final String organization) {
         this.organization = organization;
     }
 
     /**
      * Set organizationCity.
      *
      * @param organizationCity
      *		A String.
      */
     public void setOrganizationCity(final String organizationCity) {
         this.organizationCity = organizationCity;
     }
 
     /**
      * Set organizationCountry.
      *
      * @param organizationCountry
      *		A String.
      */
     public void setOrganizationCountry(final String organizationCountry) {
         this.organizationCountry = organizationCountry;
     }
 
     /**
      * Set organizationPhone.
      *
      * @param organizationPhone
      *		A String.
      */
     public void setOrganizationPhone(final String organizationPhone) {
         this.organizationPhone = organizationPhone;
     }
 
     /**
      * Set organizationPostalCode.
      *
      * @param organizationPostalCode
      *		A String.
      */
     public void setOrganizationPostalCode(final String organizationPostalCode) {
         this.organizationPostalCode = organizationPostalCode;
     }
 
     /**
      * Set organizationProvince.
      *
      * @param organizationProvince
      *		A String.
      */
     public void setOrganizationProvince(final String organizationProvince) {
         this.organizationProvince = organizationProvince;
     }
 
     /**
     * Set organziationAddress.
      *
     * @param organziationAddress
      *		A String.
      */
    public void setOrganziationAddress(final String organziationAddress) {
        this.organziationAddress = organziationAddress;
     }
 
     /**
      * Set phone.
      *
      * @param phone
      *		A String.
      */
     public void setPhone(final String phone) {
         this.phone = phone;
     }
 
     /**
      * Set postalCode.
      *
      * @param postalCode
      *		A String.
      */
     public void setPostalCode(final String postalCode) {
         this.postalCode = postalCode;
     }
 
     /**
      * Set province.
      *
      * @param province
      *		A String.
      */
     public void setProvince(final String province) {
         this.province = province;
     }
 
     /**
      * Set timeZone.
      *
      * @param timeZone
      *		A String.
      */
     public void setTimeZone(final String timeZone) {
         this.timeZone = timeZone;
     }
 
     /**
      * Set title.
      *
      * @param title
      *		A String.
      */
     public void setTitle(final String title) {
         this.title = title;
     }
 
     /**
      * Determine if a field value is set.
      * 
      * @param fieldValue
      *            The field value <code>Object</code>.
      * @return True if it is set.
      */
     private Boolean isSet(final Object field) {
         return null != field;
     }
 }
