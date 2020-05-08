 package com.testdroid.api.model;
 
 import com.testdroid.api.APIEntity;
 import com.testdroid.api.APIException;
 import com.testdroid.api.APIListResource;
 import com.testdroid.api.APISort;
 import javax.xml.bind.annotation.XmlRootElement;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  *
  * @author kajdus
  */
 @XmlRootElement
 public class APIUser extends APIEntity {
     
     private String email;
     private String name;
     private String state;
     private String country;
     private String city;
     private String code;
     private String address;
     private String phone;
     private String organization;
     private String vatID;
     private String timeZone;
     private boolean enabled;
     private APIRole[] roles;
 
     public APIUser() {}
     public APIUser(Long id, String email, String name, String state, String country, String city, String code, String address, String phone, 
             String organization, String vatID, String timeZone, boolean enabled, APIRole... roles) {
         super(id);
         this.email = email;
         this.name = name;
         this.state = state;
         this.country = country;
         this.city = city;
         this.code = code;
         this.address = address;
         this.phone = phone;
         this.organization = organization;
         this.vatID = vatID;
         this.timeZone = timeZone;
         this.enabled = enabled;
         this.roles = roles;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getState() {
         return state;
     }
 
     public void setState(String state) {
         this.state = state;
     }
 
     public String getCountry() {
         return country;
     }
 
     public void setCountry(String country) {
         this.country = country;
     }
 
     public String getCity() {
         return city;
     }
 
     public void setCity(String city) {
         this.city = city;
     }
 
     public String getCode() {
         return code;
     }
 
     public void setCode(String code) {
         this.code = code;
     }
 
     public String getAddress() {
         return address;
     }
 
     public void setAddress(String address) {
         this.address = address;
     }
 
     public String getPhone() {
         return phone;
     }
 
     public void setPhone(String phone) {
         this.phone = phone;
     }
 
     public String getOrganization() {
         return organization;
     }
 
     public void setOrganization(String organization) {
         this.organization = organization;
     }
 
     public String getVatID() {
         return vatID;
     }
 
     public void setVatID(String vatID) {
         this.vatID = vatID;
     }
 
     public String getTimeZone() {
         return timeZone;
     }
 
     public void setTimeZone(String timeZone) {
         this.timeZone = timeZone;
     }
 
     public boolean isEnabled() {
         return enabled;
     }
 
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
 
     public APIRole[] getRoles() {
         return roles;
     }
 
     public void setRoles(APIRole[] roles) {
         this.roles = roles;
     }
     
     private String getProjectsURI() { return selfURI + "/projects"; };
     
    public APIProject createProject() throws APIException {
        return postResource(getProjectsURI(), "", APIProject.class);
     }
     
     @JsonIgnore
     public APIListResource<APIProject> getProjectsResource() throws APIException {
         return getListResource(getProjectsURI(), APIProject.class);
     }
     
     @JsonIgnore
     public APIListResource<APIProject> getProjectsResource(long offset, long limit, String search, APISort sort) throws APIException {
         return getListResource(getProjectsURI(), offset, limit, search, sort, APIProject.class);
     }
     
     public APIProject getProject(Long id) throws APIException {
         return getResource(selfURI + "/projects/" + id, APIProject.class).getEntity();
     }
     
     @JsonIgnore
     public APIListResource<APIDeviceGroup> getClustersResource() throws APIException {
         return getListResource(getProjectsURI(), APIDeviceGroup.class);
     }
     
     @JsonIgnore
     public APIListResource<APIDeviceGroup> getClustersResource(long offset, long limit, String search, APISort sort) throws APIException {
         return getListResource(getProjectsURI(), offset, limit, search, sort, APIDeviceGroup.class);
     }
 }
