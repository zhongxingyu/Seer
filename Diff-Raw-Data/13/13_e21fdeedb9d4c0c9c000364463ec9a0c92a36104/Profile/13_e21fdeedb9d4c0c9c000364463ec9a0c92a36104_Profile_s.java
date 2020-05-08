 package com.dateengine.models;
 
 import javax.jdo.annotations.*;
 import javax.jdo.PersistenceManager;
 import javax.jdo.JDOObjectNotFoundException;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.GeoPt;
 import com.dateengine.PMF;
 
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Map;
 
 @PersistenceCapable(identityType = IdentityType.APPLICATION)
 public class Profile {
 
    /* Demonstrate use of a 1:1 Object:enum relationship */
    public enum MaritalStatus {
       SINGLE, DIVORCED, MARRIED
    }
 
    public enum Gender {
       MALE, FEMALE, OTHER
    }
 
    /* Demonstrate use of a 1:N Object:enum relationship */
    public enum Pet {
       DOG, CAT, MONKEY, FISH, BIRD
    }
 
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
 
    @Persistent
    private String username;
 
    @Persistent
    private int age;
 
    @Persistent
    private MaritalStatus maritalStatus;
 
    @Persistent
    private Gender gender = Gender.OTHER;        // This is a default setting
 
    @Persistent
    private Set<Pet> pets;
 
    @Persistent
    private String aboutMe;
 
    /*
       Demonstrates eager fetching of child models, now when we fetch a Profile we
       also get the corresponding photo.
    */
    @Persistent(defaultFetchGroup = "true")
    private Photo photo;
 
    // D'oh, guess I need to update my SDK to get Geo
    @Persistent
    private GeoPt location;
 
    public static Profile findForUser(User user) {
       Key key = KeyFactory.createKey(Profile.class.getSimpleName(), user.getUserId());
 
       PersistenceManager pm = PMF.get().getPersistenceManager();
       Profile profile = null;
       try {
          profile = pm.getObjectById(Profile.class, key);
          profile.getPhoto();
 
          /*
             How interesting ... even internal collections of enums are lazy fetched and need to be eager fetched while
             the PersistenceManager instance is still open ...
          * */
          profile.getPets();
       } catch (JDOObjectNotFoundException e) {
          // Maybe we should do something here?         
       } finally {
          pm.close();
       }
 
       return profile;
    }
 
    public Profile() {
       getPets();
    }
 
    public String getEncodedKey(){
       return KeyFactory.keyToString(this.key);
    }
 
    public Key getKey() {
       return key;
    }
 
    public void setKey(Key key) {
       this.key = key;
    }
 
    public String getUsername() {
       return username;
    }
 
    public void setUsername(String username) {
       this.username = username;
    }
 
    public int getAge() {
       return age;
    }
 
    public void setAge(int age) {
       this.age = age;
    }
 
    public MaritalStatus getMaritalStatus() {
       return maritalStatus;
    }
 
    public void setMaritalStatus(MaritalStatus maritalStatus) {
       this.maritalStatus = maritalStatus;
    }
 
    public String getAboutMe() {
       return aboutMe;
    }
 
    public void setAboutMe(String aboutMe) {
       this.aboutMe = aboutMe;
    }
 
    public Photo getPhoto() {
      photo.getImage();
      photo.getThumbnail();
       return photo;
    }
 
    public void setPhoto(Photo photo) {
       this.photo = photo;
    }
 
    /* Hrm, should I be doing null checks at this layer? Maybe this doesn't make sense and should be moved to servlet or DAO layer */
    public void setPets(String[] petNames) throws IllegalArgumentException {
       if (this.pets == null) this.pets = new HashSet<Pet>();
 
       this.pets.clear();
       if(petNames != null) {
          for(String petName : petNames) {
             Pet pet = Pet.valueOf(petName);
             this.pets.add(pet);
          }
       }
    }
 
    public Set<Pet> getPets() {
       if (this.pets == null) this.pets = new HashSet<Pet>();
       
       return this.pets;
    }
    
    public void setMaritalStatus(String status) throws IllegalArgumentException {
       if(status != null) {
          this.maritalStatus = MaritalStatus.valueOf(status);
       }
    }
 
    public void setGender(Gender gender) {
       this.gender = gender;
    }
 
    public Gender getGender() {
       return this.gender;
    }
 
    public void setGender(String gender) throws IllegalArgumentException {
       if(gender != null) {
          this.gender = Gender.valueOf(gender);
       }
    }
 
 
 }
