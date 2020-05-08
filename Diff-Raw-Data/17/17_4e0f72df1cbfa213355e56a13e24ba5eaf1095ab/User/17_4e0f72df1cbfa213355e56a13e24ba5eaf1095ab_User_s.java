 package edu.wustl.cab2b.common.user;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashSet;
 
 /**
  * @author Hrishikesh Rajpathak
  * @hibernate.class table="CAB2B_USER"
 * @hibernate.cache usage="read-write"
  * 
  */
 public class User implements UserInterface, Serializable {
 
     private Collection<ServiceURLInterface> serviceURLCollection = new HashSet<ServiceURLInterface>();
 
     private Long userId;
 
     private String userName;
 
     private String password;
 
     private boolean isAdmin;
 
     public User() {
         super();
     }
 
     public User(String userName, String password, boolean isAdmin) {
         this();
         this.userName = userName;
         this.password = password;
         this.isAdmin = isAdmin;
     }
 
     /**
      * @hibernate.id name="userId" column="USER_ID" type="long" length="30"
      *               unsaved-value="null" generator-class="native"
      * @hibernate.generator-param name="sequence" value="USER_ID_SEQ"
      */
     public Long getUserId() {
         return userId;
     }
 
     /**
      * @param userId
      *            The userId to set.
      */
     public void setUserId(Long userId) {
         this.userId = userId;
     }
 
     /**
      * @return Returns the userName.
      * 
      * @hibernate.property column="NAME" type="string" length="30"
      *                     not-null="true"
      */
     public String getUserName() {
         return userName;
     }
 
     /**
      * @param userName
      *            The userName to set.
      */
     public void setUserName(String userName) {
         this.userName = userName;
     }
 
     /**
      * @return Returns the serviceURLCollection.
      * 
      * @hibernate.set name="serviceURLCollection" cascade="all-delete-orphan" lazy="false" inverse="false" table="CAB2B_USER_URL_MAPPING"
      * @hibernate.collection-key column="USER_ID" 
      * @hibernate.collection-many-to-many class="edu.wustl.cab2b.common.user.ServiceURL" column="SERVICE_URL_ID"
     * @hibernate.cache usage="read-write"
      */
     public Collection<ServiceURLInterface> getServiceURLCollection() {
         return serviceURLCollection;
     }
 
     /**
      * @param serviceURLCollection The serviceURLCollection to set.
      */
     public void setServiceURLCollection(Collection<ServiceURLInterface> serviceURLCollection) {
         this.serviceURLCollection = serviceURLCollection;
     }
 
     /**
      * @return Returns the password.
      * 
      * @hibernate.property column="PASSWORD" type="string" length="30" not-null="true"
      */
     public String getPassword() {
         return password;
     }
 
     /**
      * @param password
      *            The password to set.
      */
     public void setPassword(String password) {
         this.password = password;
     }
 
     public void addServiceURL(ServiceURL serviceURL) {
         if (serviceURL != null) {
             serviceURLCollection.add(serviceURL);
         }
     }
 
     public void removeServiceURL(ServiceURL serviceURL) {
         if (serviceURL != null) {
             serviceURLCollection.remove(serviceURL);
         }
     }
 
     /**
      * @return Returns the isAdmin.
      */
     public boolean isAdmin() {
         return isAdmin;
     }
 
     /**
      * @param isAdmin
      *            The isAdmin to set.
      * 
      * @hibernate.property column="IS_ADMIN" type="boolean" not-null="true"
      */
     public void setAdmin(boolean isAdmin) {
         this.isAdmin = isAdmin;
     }
 
     @Override
     public boolean equals(Object arg) {
         if (this == arg)
             return true;
 
         boolean isEqual = false;
         if (null != arg && arg instanceof User) {
             User user = (User) arg;
 
             if (userName != null && user.getUserName().equals(userName)) {
                 isEqual = true;
             }
         }
         return isEqual;
     }
 
     @Override
     public int hashCode() {
         int hashCode = 1;
         if (userName != null) {
             hashCode += 7 * userName.hashCode();
         }
         return hashCode;
     }
 }
