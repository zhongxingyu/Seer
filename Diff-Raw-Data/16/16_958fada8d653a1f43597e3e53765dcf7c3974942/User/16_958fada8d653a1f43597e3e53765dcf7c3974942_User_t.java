 package org.netvogue.server.neo4japi.domain;
 
 //Netvogue specific imports
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.Set;
 import java.util.UUID;
 
 import org.netvogue.server.neo4japi.common.USER_TYPE;
 import org.springframework.data.neo4j.annotation.GraphId;
 import org.springframework.data.neo4j.annotation.Indexed;
 import org.springframework.data.neo4j.annotation.NodeEntity;
 import org.springframework.data.neo4j.annotation.RelatedTo;
 import org.springframework.data.neo4j.support.index.IndexType;
 import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
 import org.springframework.security.core.GrantedAuthority;
 //Spring Framework imports
 //Generic
 
 //Base class for boutique and Brand
 @NodeEntity
 public class User {
 
   private static final String SALT = "cewuiqwzie"; // This is required for password change through Spring security
 
   // @Autowired UserRepository userrepo;
 
   @GraphId
   Long nodeId;
 
   @Indexed(indexName = "email", indexType = IndexType.FULLTEXT)
   String email;
 
   String password;
 
   @Indexed(indexName = "userid")
   String userId;
 
   @Indexed(indexName = "search", indexType = IndexType.FULLTEXT, unique = true)
   String username;
 
   @Indexed(indexName = "search", indexType = IndexType.FULLTEXT)
   String name;
 
   String profilePicLink = new String();
 
   @Indexed
   USER_TYPE userType;
 
   String primarycontact;
 
   String aboutUs;
 
   String address;
 
   @Indexed(indexName = "search", indexType = IndexType.FULLTEXT)
   String city;
 
   @Indexed(indexName = "search", indexType = IndexType.FULLTEXT)
   String state;
 
   int zipCode;
 
   @Indexed(indexName = "search", indexType = IndexType.FULLTEXT)
   String country;
 
   long mobileNo;
 
   long telephoneNo1;
 
   long telephoneNo2;
 
   String website;
 
   int yearofEst;
 
   long fromPrice = 0;
 
   long toPrice = 0;
 
   Date registeredDate = new Date();
 
   Date accessGrantedDate = new Date();
 
   boolean firstTimeLogin;
 
   boolean accountEnabled; // This would depend upon business requirements
 
   boolean accountExpired;
 
   private Roles[] roles; // Roles of this user -- This is required for Spring Security
 
   // Add relations now
   @RelatedTo(type = "has_category")
   Set<Category> productLinesCarried = new HashSet<Category>();
 
   @RelatedTo(type = "users_carried")
   Set<User> usersCarried = new HashSet<User>();
 
   // Because of this there would be two relationships between brand and boutique
   // BrandsCarried of boutique may or may not be part of their network
   // If any particular brand is not there in our network, just add with basic information and move ahead. Tomorrow, if
   // someone else
   // add this new brand into our network. Then these boutiques must be automatically connected to them
   // Azeez -- How do we connect it to existing brands in our network or to the new network.
 
   @RelatedTo(type = "NOTIFICATION")
   Set<Notification> notifications = new LinkedHashSet<Notification>();
 
   @RelatedTo(type = "NETWORK")
   private Iterable<User> friends; // revisit this once again
 
   public User() {
 
   }
 
   public User(String email, String password) {
     // By default account enabled should be false, once the user clicks on confirmation email, this flag should be set
     // to true
    accountEnabled = false;
     accountExpired = false;
     firstTimeLogin = true;
 
     userId = UUID.randomUUID().toString();
     this.email = email;
     this.password = encode(password);
   }
 
   public Long getNodeId() {
     return nodeId;
   }
 
   public void setNodeId(Long nodeId) {
     this.nodeId = nodeId;
   }
 
   public String getEmail() {
     return email;
   }
 
   public void setEmail(String email) {
     this.email = email;
   }
 
   public String getPassword() {
     return password;
   }
 
   /*
    * public void encryptPassword(String password) { System.out.println("Azeez: Password given is" + password);
    * System.out.println("Azeez: Before: Password entered is" + this.password); this.password = encode(password);
    * System.out.println("Azeez: After: Password entered is" + this.password); }
    */
 
   public void updatePassword(String old, String newPass1, String newPass2) {
     if (!password.equals(encode(old))) {
       throw new IllegalArgumentException("Existing Password invalid");
     }
     if (!newPass1.equals(newPass2)) {
       throw new IllegalArgumentException("New Passwords don't match");
     }
     password = encode(newPass1);
   }
 
   private String encode(String password) {
     return new Md5PasswordEncoder().encodePassword(password, SALT);
   }
 
   public String getUserId() {
     return userId;
   }
 
   public void setUserId(String userId) {
     this.userId = userId;
   }
 
   public String getUsername() {
     return username;
   }
 
   public void setUsername(String username) {
     this.username = username;
   }
 
   public String getName() {
     return name;
   }
 
   public void setName(String name) {
     this.name = name;
   }
 
   public String getPrimarycontact() {
     return primarycontact;
   }
 
   public void setPrimarycontact(String primarycontact) {
     this.primarycontact = primarycontact;
   }
 
   public String getProfilePicLink() {
     return profilePicLink;
   }
 
   public void setProfilePicLink(String profilePicLink) {
     this.profilePicLink = profilePicLink;
   }
 
   public USER_TYPE getUserType() {
     return userType;
   }
 
   public void setUserType(USER_TYPE userType) {
     this.userType = userType;
   }
 
   public String getAboutUs() {
     return aboutUs;
   }
 
   public void setAboutUs(String aboutUs) {
     this.aboutUs = aboutUs;
   }
 
   public String getAddress() {
     return address;
   }
 
   public void setAddress(String address) {
     this.address = address;
   }
 
   public String getCity() {
     return city;
   }
 
   public void setCity(String city) {
     this.city = city;
   }
 
   public String getState() {
     return state;
   }
 
   public void setState(String state) {
     this.state = state;
   }
 
   public int getZipCode() {
     return zipCode;
   }
 
   public void setZipCode(int zip) {
     zipCode = zip;
   }
 
   public String getCountry() {
     return country;
   }
 
   public void setCountry(String country) {
     this.country = country;
   }
 
   public long getMobileNo() {
     return mobileNo;
   }
 
   public void setMobileNo(long mobileNo) {
     this.mobileNo = mobileNo;
   }
 
   public long getTelephoneNo1() {
     return telephoneNo1;
   }
 
   public void setTelephoneNo1(long telephoneNo1) {
     this.telephoneNo1 = telephoneNo1;
   }
 
   public long getTelephoneNo2() {
     return telephoneNo2;
   }
 
   public void setTelephoneNo2(long telephoneNo2) {
     this.telephoneNo2 = telephoneNo2;
   }
 
   public String getWebsite() {
     return website;
   }
 
   public void setWebsite(String website) {
     this.website = website;
   }
 
   public int getYearofEst() {
     return yearofEst;
   }
 
   public void setYearofEst(int yearofEst) {
     this.yearofEst = yearofEst;
   }
 
   public long getFromPrice() {
     return fromPrice;
   }
 
   public void setFromPrice(long fromPrice) {
     this.fromPrice = fromPrice;
   }
 
   public long getToPrice() {
     return toPrice;
   }
 
   public void setToPrice(long toPrice) {
     this.toPrice = toPrice;
   }
 
   public Roles[] getRoles() {
     return roles;
   }
 
   public void setRoles(Roles... roles) {
     this.roles = roles;
   }
 
   public Date getRegisteredDate() {
     return registeredDate;
   }
 
   public void setRegisteredDate(Date registeredDate) {
     this.registeredDate = registeredDate;
   }
 
   public Date getAccessGrantedDate() {
     return accessGrantedDate;
   }
 
   public void setAccessGrantedDate(Date accessGrantedDate) {
     this.accessGrantedDate = accessGrantedDate;
   }
 
   public boolean isFirstTimeLogin() {
     return firstTimeLogin;
   }
 
   public void setFirstTimeLogin(boolean firstTimeLogin) {
     this.firstTimeLogin = firstTimeLogin;
   }
 
   public boolean getAccountEnabled() {
     return accountEnabled;
   }
 
   public void setAccountEnabled(boolean accountEnabled) {
     this.accountEnabled = accountEnabled;
   }
 
   public boolean getAccountExpired() {
     return accountExpired;
   }
 
   public void setAccountExpired(boolean accountExpired) {
     this.accountExpired = accountExpired;
   }
 
   public Boolean Authenticate(String password) {
     if (this.password.equals(encode(password))) {
       return true;
     }
     System.out.println("Original password is: " + this.password);
     System.out.println("Password entered  is: " + encode(password));
     return false;
   }
 
   public enum Roles implements GrantedAuthority {
     ROLE_USER, ROLE_ADMIN, ROLE_BOUTIQUE, ROLE_BRAND;
 
     @Override
     public String getAuthority() {
       return name();
     }
   }
 
   public Set<Category> getProductLinesCarried() {
     return productLinesCarried;
   }
 
   public void setProductLinesCarried(Set<Category> productLinesCarried) {
     this.productLinesCarried = productLinesCarried;
   }
 
   public Set<User> getUsersCarried() {
     return usersCarried;
   }
 
   public void setUsersCarried(Set<User> usersCarried) {
     this.usersCarried = usersCarried;
   }
 
   public void updateCategories(Category newCategory) {
     System.out.println("Update Categories---" + newCategory.toString());
     productLinesCarried.add(newCategory);
   }
 
   public void deleteCategories() {
     System.out.println("Delete Categories---");
     productLinesCarried.clear();
   }
 
   public void updateUsersCarried(User usercarried) {
     usersCarried.add(usercarried);
   }
 
   public boolean deleteUsersCarried(String usercarried) {
     for (User user : usersCarried) {
       System.out.println("Users carried1 " + user.getUsername() + " name:" + user.getName());
       System.out.println("Nodeid is " + user.getNodeId());
       System.out.println("Nodeid is " + user.hashCode());
       if (usercarried.equals(user.getUsername())) {
         try {
 
           if (usersCarried.remove(user)) {
             return true;
           } else {
             System.out.println("error while removing it");
           }
         } catch (Exception e) {
           System.out.println("error while removing it" + e.toString());
         }
       }
       System.out.println("Users carried2" + user.getUsername() + " name:" + user.getName());
     }
     System.out.println("Couldn't find user carried");
     return false;
   }
 
   public void deleteUsersCarried() {
     usersCarried.clear();
   }
 
   public void addNotification(Notification newNotification) {
     notifications.add(newNotification);
   }
 
   /*
    * public boolean isMyFriend(String username) { Iterator<User> first = friends.iterator(); while ( first.hasNext() ){
    * User dbUser = first.next() ; if(dbUser.getUsername() == usern) } }
    */
 
   public Set<Notification> getNotifications() {
     return notifications;
   }
 
   public void setNotifications(Set<Notification> notifications) {
     this.notifications = notifications;
   }
 
   @Override
   public boolean equals(Object other) {
     if (this == other) {
       return true;
     }
     if (nodeId == null) {
       return false;
     }
     if (!(other instanceof User)) {
       return false;
     }
     return nodeId.equals(((User) other).nodeId);
   }
 
   @Override
   public int hashCode() {
     return nodeId == null ? System.identityHashCode(this) : nodeId.hashCode();
   }
 }
