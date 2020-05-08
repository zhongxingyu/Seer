 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.fhg.fokus.persistence;
 
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import javax.xml.bind.annotation.XmlTransient;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  *
  * @author Hannes Gorges
  */
 @Entity
 @Table(name = "userdata")
 @XmlRootElement
 @JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
 @NamedQueries({
     @NamedQuery(name = "Userdata.findAll", query = "SELECT u FROM Userdata u"),
     @NamedQuery(name = "Userdata.findByIdUserData", query = "SELECT u FROM Userdata u WHERE u.idUserData = :idUserData"),
     @NamedQuery(name = "Userdata.findByAge", query = "SELECT u FROM Userdata u WHERE u.age = :age"),
     @NamedQuery(name = "Userdata.findByAuthtime", query = "SELECT u FROM Userdata u WHERE u.authtime = :authtime"),
     @NamedQuery(name = "Userdata.findByEmail", query = "SELECT u FROM Userdata u WHERE u.email = :email"),
     @NamedQuery(name = "Userdata.findByUsername", query = "SELECT u FROM Userdata u WHERE u.username = :username"),
     @NamedQuery(name = "Userdata.findByFirstname", query = "SELECT u FROM Userdata u WHERE u.firstname = :firstname"),
     @NamedQuery(name = "Userdata.findByGender", query = "SELECT u FROM Userdata u WHERE u.gender = :gender"),
     @NamedQuery(name = "Userdata.findByOrganization", query = "SELECT u FROM Userdata u WHERE u.organization = :organization"),
     @NamedQuery(name = "Userdata.findByUserSIGN", query = "SELECT u FROM Userdata u WHERE u.userSIGN = :userSIGN"),
     @NamedQuery(name = "Userdata.findByViewLanguage", query = "SELECT u FROM Userdata u WHERE u.viewLanguage = :viewLanguage")})
 public class Userdata implements Serializable {
 
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @NotNull
     @Column(name = "idUserData")
     private Integer idUserData;
     @Column(name = "age")
     @Temporal(TemporalType.DATE)
     private Date age;
     @Column(name = "authtime")
     private BigInteger authtime;
     // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
     @Size(max = 255)
     @Column(name = "email")
     private String email;
     @Size(max = 255)
     @Column(name = "username")
     private String username;
     @Size(max = 255)
     @Column(name = "firstname")
     private String firstname;
     @Size(max = 255)
     @Column(name = "lastname")
     private String lastname;
     @Size(max = 255)
     @Column(name = "fullname")
     private String fullname;
     @Size(max = 255)
     @Column(name = "avatarUrl")
     private String avatarUrl;
     @Size(max = 255)
     @Column(name = "profileUrl")
     private String profileUrl;
     @Size(max = 255)
     @Column(name = "provider")
     private String provider;
     @Size(max = 255)
     @Column(name = "providerId")
     private String providerId;
     @Size(max = 45)
     @Column(name = "gender")
     private String gender;        
     @Size(max = 255)
     @Column(name = "organization")
     private String organization;
     @Size(max = 255)
     @Column(name = "userSIGN")
     private String userSIGN;
     @Size(max = 255)
     @Column(name = "viewLanguage")
     private String viewLanguage;
     @Basic(optional = false)
     @NotNull
     @Column(name = "firstLogin")
     private boolean firstLogin;    
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idUserData")
     private List<Smpaccount> smpaccountList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idUserData")
     private List<Userrole> userroleList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idUserData")
     private List<Publishchannel> publishchannelList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idUserData")
     private List<Message> messageList;
     @OneToMany(mappedBy = "idUserData")
     private List<Comment> commentList;
 
     public Userdata() {
     }
 
     public Userdata(Integer idUserData) {
         this.idUserData = idUserData;
     }
 
     public Integer getIdUserData() {
         return idUserData;
     }
 
     public void setIdUserData(Integer idUserData) {
         this.idUserData = idUserData;
     }
 
     public Date getAge() {
         return age;
     }
 
     public void setAge(Date age) {
         this.age = age;
     }
 
     public BigInteger getAuthtime() {
         return authtime;
     }
 
     public void setAuthtime(BigInteger authtime) {
         this.authtime = authtime;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getFirstname() {
         return firstname;
     }
 
     public void setFirstname(String firstname) {
         this.firstname = firstname;
     }
 
     public String geFullname() {
         return fullname;
     }
 
     public void setFullname(String fullname) {
         this.fullname  = fullname;
     }
 
     public String getLastname() {
         return lastname;
     }
 
     public void setLastname(String lastname) {
         this.lastname = lastname;
     }
 
     public String getGender() {
         return gender;
     }
 
     public void setGender(String gender) {
         this.gender = gender;
     }    
 
     public String getOrganization() {
         return organization;
     }
 
     public void setOrganization(String organization) {
         this.organization = organization;
     }
 
     @JsonIgnore
     @XmlTransient
     public String getUserSIGN() {
         return userSIGN;
     }
 
     public void setUserSIGN(String userSIGN) {
         this.userSIGN = userSIGN;
     }
 
     public String getViewLanguage() {
         return viewLanguage;
     }
 
     public void setViewLanguage(String viewLanguage) {
         this.viewLanguage = viewLanguage;
     }
     
    public boolean getFirstLogin() {
         return firstLogin;
     }
 
     public void setFirstLogin(boolean firstLogin) {
         this.firstLogin = firstLogin;
     }
 
     @JsonIgnore
     @XmlTransient
     public List<Smpaccount> getSmpaccountList() {
         return smpaccountList;
     }
 
     public void setSmpaccountList(List<Smpaccount> smpaccountList) {
         this.smpaccountList = smpaccountList;
     }
 
     @JsonIgnore
     @XmlTransient
     public List<Userrole> getUserroleList() {
         return userroleList;
     }
 
     public void setUserroleList(List<Userrole> userroleList) {
         this.userroleList = userroleList;
     }
 
     
     public boolean addUserrole(Userrole userrole) {
         return this.userroleList.add(userrole);
     }
 
     public boolean removeUserrole(Userrole userrole) {
         return this.userroleList.remove(userrole);
     }
     
     @JsonIgnore
     @XmlTransient
     public List<Publishchannel> getPublishchannelList() {
         return publishchannelList;
     }
 
     public void setPublishchannelList(List<Publishchannel> publishchannelList) {
         this.publishchannelList = publishchannelList;
     }    
 
     @JsonIgnore
     @XmlTransient
     public List<Message> getMessageList() {
         return messageList;
     }
 
     public void setMessageList(List<Message> messageList) {
         this.messageList = messageList;
     }
 
     public boolean addMessage(Message message) {
         return this.messageList.add(message);
     }
 
     public boolean removeMessage(Message message) {
         return this.messageList.remove(message);
     }
 
     @JsonIgnore
     @XmlTransient
     public List<Comment> getCommentList() {
         return commentList;
     }
 
     public void setCommentList(List<Comment> commentList) {
         this.commentList = commentList;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (idUserData != null ? idUserData.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         if (!(object instanceof Userdata)) {
             return false;
         }
         Userdata other = (Userdata) object;
         if ((this.idUserData == null && other.idUserData != null) || (this.idUserData != null && !this.idUserData.equals(other.idUserData))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "de.fhg.fokus.persistence.Userdata[ idUserData=" + idUserData + " ]";
     }
 
     /**
      * @return the avatarUrl
      */
     public String getAvatarUrl() {
         return avatarUrl;
     }
 
     /**
      * @param avatarUrl the avatarUrl to set
      */
     public void setAvatarUrl(String avatarUrl) {
         this.avatarUrl = avatarUrl;
     }
 
     /**
      * @return the profileUrl
      */
     public String getProfileUrl() {
         return profileUrl;
     }
 
     /**
      * @param profileUrl the profileUrl to set
      */
     public void setProfileUrl(String profileUrl) {
         this.profileUrl = profileUrl;
     }
 
     /**
      * @return the provider
      */
     public String getProvider() {
         return provider;
     }
 
     /**
      * @param provider the provider to set
      */
     public void setProvider(String provider) {
         this.provider = provider;
     }
 
     /**
      * @return the providerId
      */
     public String getProviderId() {
         return providerId;
     }
 
     /**
      * @param providerId the providerId to set
      */
     public void setProviderId(String providerId) {
         this.providerId = providerId;
     }
 }
