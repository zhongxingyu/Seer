 package com.leetchi.api.client.model;
 
 import com.leetchi.api.client.Leetchi;
 import org.codehaus.jackson.annotate.JsonProperty;
 
 import java.util.List;
 
 public class User extends Entity<User> {
 
     public static final String PATH = "users";
     public static final String NATURAL_PERSON_TYPE = "NATURAL_PERSON";
    public static final String LEGAL_PERSON_TYPE = "LEGAL_PERSONALITY";
     private String firstName;
     private String lastName;
     private String email;
     private String nationality;
     private String personType;
     private String ip;
     @JsonProperty("IsStrongAuthenticated")
     private Boolean isStrongAuthenticated;
     private Boolean canRegisterMeanOfPayment = true;
     private Boolean hasRegisteredMeansOfPayment;
     private Long birthday;
     @JsonProperty("Password")
     private String password;
     @JsonProperty("PersonalWalletAmount")
     private Long personalWalletAmount;
 
     User() {
     }
 
     public static User newUser() {
         return new User();
     }
 
     public User firstName(String firstName) {
         this.firstName = firstName;
         return this;
     }
 
     public User lastName(String lastName) {
         this.lastName = lastName;
         return this;
     }
 
     public User email(String email) {
         this.email = email;
         return this;
     }
 
     public User nationality(String nationality) {
         this.nationality = nationality;
         return this;
     }
 
     public User personType(String personType) {
         this.personType = personType;
         return this;
     }
 
     @JsonProperty("PersonType")
     public String getPersonType() {
         return personType;
     }
 
     @JsonProperty("Nationality")
     public String getNationality() {
         return nationality;
     }
 
     @JsonProperty("Email")
     public String getEmail() {
         return email;
     }
 
     @JsonProperty("LastName")
     public String getLastName() {
         return lastName;
     }
 
     @JsonProperty("FirstName")
     public String getFirstName() {
         return firstName;
     }
 
     @JsonProperty("Birthday")
     public Long getBirthday() {
         return birthday;
     }
 
     @JsonProperty("HasRegisteredMeansOfPayment")
     public Boolean getHasRegisteredMeansOfPayment() {
         return hasRegisteredMeansOfPayment;
     }
 
     @JsonProperty("CanRegisterMeanOfPayment")
     public Boolean getCanRegisterMeanOfPayment() {
         return canRegisterMeanOfPayment;
     }
 
     public Boolean isStrongAuthenticated() {
         return isStrongAuthenticated;
     }
 
     @JsonProperty("IP")
     public String getIp() {
         return ip;
     }
 
     @Override
     public String path() {
         return User.PATH;
     }
 
     public String path(Long id) {
         return path(User.PATH, id);
     }
 
     public static User fetch(Long id) throws Exception {
         return Leetchi.fetch(path(User.PATH, id), User.class);
     }
 
     public List<Wallet> wallets() throws Exception {
         return Wallet.fromUser(id);
     }
 
     public Long getPersonalWalletAmount() {
         return personalWalletAmount;
     }
 
     public List<PaymentCard> paymentCards() throws Exception {
         return PaymentCard.fromUser(id);
     }
 
     public User canRegisterMeanOfPayment(boolean canRegisterMeanOfPayment) {
         this.canRegisterMeanOfPayment = canRegisterMeanOfPayment;
         return this;
     }
 
     public User birthday(Long birthday) {
         this.birthday = birthday;
         return this;
     }
 
     public String getPassword() {
         return password;
     }
 }
