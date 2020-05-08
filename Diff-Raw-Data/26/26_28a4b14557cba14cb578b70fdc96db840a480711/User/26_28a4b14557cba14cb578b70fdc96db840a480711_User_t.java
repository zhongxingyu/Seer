 package com.yrek.rideapp.facebook;
 
 import java.io.Serializable;
 
import javax.xml.bind.annotation.XmlElement;

 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
 
 @JsonIgnoreProperties(ignoreUnknown=true)
 public class User implements Serializable {
     private static final long serialVersionUID = 0L;
 
     private String id;
     private String name;
     private String firstName;
     private String lastName;
     private String link;
     private String gender;
     private Integer timezone;
     private String locale;
 
    @XmlElement
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
    @XmlElement
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
    @XmlElement
    @JsonProperty("first_name")
     public String getFirstName() {
         return firstName;
     }
 
    @JsonProperty("first_name")
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
    @XmlElement
    @JsonProperty("last_name")
     public String getLastName() {
         return lastName;
     }
 
    @JsonProperty("last_name")
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
    @XmlElement
     public String getLink() {
         return link;
     }
 
     public void setLink(String link) {
         this.link = link;
     }
 
    @XmlElement
     public String getGender() {
         return gender;
     }
 
     public void setGender(String gender) {
         this.gender = gender;
     }
 
    @XmlElement
     public Integer getTimezone() {
         return timezone;
     }
 
     public void setTimezone(Integer timezone) {
         this.timezone = timezone;
     }
 
    @XmlElement
     public String getLocale() {
         return locale;
     }
 
     public void setLocale(String locale) {
         this.locale = locale;
     }
 }
