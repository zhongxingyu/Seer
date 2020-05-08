 package se.umu.cs.umume;
 
 import java.net.URI;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlElements;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 @XmlType
 @XmlRootElement(name="person")
 public class PersonBean {
     private URI resourceRef;
     private String givenName;
     private String familyName;
     private URI photoURI;
     private List<String> emails;
     private List<String> tweets;
     private String twitterName;
     private String floor;
     private String street;
     private String postalCode;
     private String institution;
     private String buildingName;
     private String roomNumber;
     private String phoneNumber;
     private String description;
 
     @XmlAttribute(name="self")
     public URI getResourceRef() {
         return resourceRef;
     }
     public void setResourceRef(URI resourceRef) {
         this.resourceRef = resourceRef;
     }
 
     public String getGivenName() {
         return givenName;
     }
     public void setGivenName(String givenName) {
         this.givenName = givenName;
     }
     public String getFamilyName() {
         return familyName;
     }
     public void setFamilyName(String familyName) {
         this.familyName = familyName;
     }
 
     @XmlElementWrapper(name="emails")
     @XmlElements({@XmlElement(name="email")})
     public List<String> getEmails() {
         return emails;
     }
     public void setEmails(List<String> emails) {
         this.emails = emails;
     }
     public String getFloor() {
         return floor;
     }
     public void setFloor(String floor) {
         this.floor = floor;
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
     public String getInstitution() {
         return institution;
     }
     public void setInstitution(String institution) {
         this.institution = institution;
     }
     public String getRoomNumber() {
         return roomNumber;
     }
     public void setRoomNumber(String roomNumber) {
         this.roomNumber = roomNumber;
     }
     public String getPhoneNumber() {
         return phoneNumber;
     }
     public void setPhoneNumber(String phoneNumber) {
         this.phoneNumber = phoneNumber;
     }
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((emails == null) ? 0 : emails.hashCode());
         result = prime * result
             + ((familyName == null) ? 0 : familyName.hashCode());
         result = prime * result + ((floor == null) ? 0 : floor.hashCode());
         result = prime * result
             + ((givenName == null) ? 0 : givenName.hashCode());
         result = prime * result
             + ((institution == null) ? 0 : institution.hashCode());
         result = prime * result
             + ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
         result = prime * result
             + ((postalCode == null) ? 0 : postalCode.hashCode());
         result = prime * result
             + ((resourceRef == null) ? 0 : resourceRef.hashCode());
         result = prime * result
             + ((roomNumber == null) ? 0 : roomNumber.hashCode());
         result = prime * result + ((street == null) ? 0 : street.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         PersonBean other = (PersonBean) obj;
         if (emails == null) {
             if (other.emails != null)
                 return false;
         } else if (!emails.equals(other.emails))
             return false;
         if (familyName == null) {
             if (other.familyName != null)
                 return false;
         } else if (!familyName.equals(other.familyName))
             return false;
         if (floor == null) {
             if (other.floor != null)
                 return false;
         } else if (!floor.equals(other.floor))
             return false;
         if (givenName == null) {
             if (other.givenName != null)
                 return false;
         } else if (!givenName.equals(other.givenName))
             return false;
         if (institution == null) {
             if (other.institution != null)
                 return false;
         } else if (!institution.equals(other.institution))
             return false;
         if (phoneNumber == null) {
             if (other.phoneNumber != null)
                 return false;
         } else if (!phoneNumber.equals(other.phoneNumber))
             return false;
         if (postalCode == null) {
             if (other.postalCode != null)
                 return false;
         } else if (!postalCode.equals(other.postalCode))
             return false;
         if (resourceRef == null) {
             if (other.resourceRef != null)
                 return false;
         } else if (!resourceRef.equals(other.resourceRef))
             return false;
         if (roomNumber == null) {
             if (other.roomNumber != null)
                 return false;
         } else if (!roomNumber.equals(other.roomNumber))
             return false;
         if (street == null) {
             if (other.street != null)
                 return false;
         } else if (!street.equals(other.street))
             return false;
         return true;
     }
 
     @XmlElementWrapper(name="tweets")
     @XmlElements({@XmlElement(name="tweet")})
     public List<String> getTweets() {
         return tweets;
     }
     
     public void setTweets(List<String> tweets) {
         this.tweets = tweets;
     }
 
     public void setTwitterName(String twitterName) {
         this.twitterName = twitterName;
     }
     public String getTwitterName() {
         return twitterName;
     }
     public void setDescription(String description) {
         this.description = description;
     }
     public String getDescription() {
         return description;
     }
     public String getBuildingName() {
         return buildingName;
     }
     public void setBuildingName(String buildingName) {
         this.buildingName = buildingName;
     }
     public URI getPhotoURI() {
         return photoURI;
     }
     public void setPhotoURI(URI photoURI) {
         this.photoURI = photoURI;
     }
 }
