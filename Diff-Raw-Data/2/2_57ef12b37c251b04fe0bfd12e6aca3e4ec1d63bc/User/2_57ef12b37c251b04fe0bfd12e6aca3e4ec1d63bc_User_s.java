 package war.webapp.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 import org.compass.annotations.Searchable;
 import org.compass.annotations.SearchableComponent;
 import org.compass.annotations.SearchableId;
 import org.compass.annotations.SearchableProperty;
 import org.springframework.security.GrantedAuthority;
 import org.springframework.security.userdetails.UserDetails;
 
 /**
  * This class represents the basic "user" object in Hostel Duty that allows for
  * authentication and user management. It implements Acegi Security's
  * UserDetails interface.
  * 
  */
 @Entity
 @Table(name = "app_user")
 @Searchable
 public class User extends BaseObject implements Serializable, UserDetails {
     private static final long serialVersionUID = 3832626162173359411L;
 
     private Long id;
     private String username; // required
     private String password; // required
     private String firstName; // required
     private String lastName; // required
     private String middleName; // required
     private Address address = new Address();
     private String universityGroup;
     private Integer version;
     private Set<Role> roles = new HashSet<Role>();
     private boolean enabled;
     private boolean accountExpired = false;
     private boolean accountLocked;
     private boolean credentialsExpired = false;
 
     /**
      * Default constructor - creates a new instance with no values set.
      */
     public User() {
     }
 
     /**
      * Create a new instance and set the username.
      * 
      * @param username login name for user.
      */
     public User(final String username) {
         this.username = username;
     }
 
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     @SearchableId
     public Long getId() {
         return id;
     }
 
     @Column(nullable = false, length = 50, unique = true)
     @SearchableProperty
     public String getUsername() {
         return username;
     }
 
     @Column(nullable = false)
     public String getPassword() {
         return password;
     }
 
     @Column(name = "first_name", nullable = false, length = 50)
     @SearchableProperty
     public String getFirstName() {
         return firstName;
     }
 
     @Column(name = "last_name", nullable = false, length = 50)
     @SearchableProperty
     public String getLastName() {
         return lastName;
     }
 
    @Column(name = "middle_name", length = 50)
     @SearchableProperty
     public String getMiddleName() {
         return middleName;
     }
 
     /**
      * Returns the full name.
      * 
      * @return firstName + ' ' + lastName
      */
     @Transient
     public String getFullName() {
 //        String fullName = lastName + " " + firstName.charAt(0) + ".";
 //        if (middleName != null) {
 //            fullName += " " + middleName.charAt(0) + ".";
 //        }
 //        return fullName;
         return lastName;
     }
 
     @Embedded
     @SearchableComponent
     public Address getAddress() {
         return address;
     }
 
     @Column(name = "university_group")
     public String getUniversityGroup() {
         return universityGroup;
     }
 
     @ManyToMany(fetch = FetchType.EAGER)
     @JoinTable(name = "user_role", joinColumns = { @JoinColumn(name = "user_id") }, inverseJoinColumns = @JoinColumn(name = "role_id"))
     public Set<Role> getRoles() {
         return roles;
     }
 
     /**
      * Convert user roles to LabelValue objects for convenience.
      * 
      * @return a list of LabelValue objects with role information
      */
     @Transient
     public List<LabelValue> getRoleList() {
         List<LabelValue> userRoles = new ArrayList<LabelValue>();
 
         if (this.roles != null) {
             for (Role role : roles) {
                 // convert the user's roles to LabelValue Objects
                 userRoles.add(new LabelValue(role.getName(), role.getName()));
             }
         }
 
         return userRoles;
     }
 
     /**
      * Adds a role for the user
      * 
      * @param role the fully instantiated role
      */
     public void addRole(Role role) {
         getRoles().add(role);
     }
 
     /**
      * @return GrantedAuthority[] an array of roles.
      * @see org.springframework.security.userdetails.UserDetails#getAuthorities()
      */
     @Transient
     public GrantedAuthority[] getAuthorities() {
         return roles.toArray(new GrantedAuthority[0]);
     }
 
     @Version
     public Integer getVersion() {
         return version;
     }
 
     @Column(name = "account_enabled")
     public boolean isEnabled() {
         return enabled;
     }
 
     @Column(name = "account_expired", nullable = false)
     public boolean isAccountExpired() {
         return accountExpired;
     }
 
     /**
      * @see org.springframework.security.userdetails.UserDetails#isAccountNonExpired()
      */
     @Transient
     public boolean isAccountNonExpired() {
         return !isAccountExpired();
     }
 
     @Column(name = "account_locked", nullable = false)
     public boolean isAccountLocked() {
         return accountLocked;
     }
 
     /**
      * @see org.springframework.security.userdetails.UserDetails#isAccountNonLocked()
      */
     @Transient
     public boolean isAccountNonLocked() {
         return !isAccountLocked();
     }
 
     @Column(name = "credentials_expired", nullable = false)
     public boolean isCredentialsExpired() {
         return credentialsExpired;
     }
 
     /**
      * @see org.springframework.security.userdetails.UserDetails#isCredentialsNonExpired()
      */
     @Transient
     public boolean isCredentialsNonExpired() {
         return !credentialsExpired;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public void setMiddleName(String middleName) {
         this.middleName = middleName;
     }
 
     public void setAddress(Address address) {
         this.address = address;
     }
 
     public void setUniversityGroup(String universityGroup) {
         this.universityGroup = universityGroup;
     }
 
     public void setRoles(Set<Role> roles) {
         this.roles = roles;
     }
 
     public void setVersion(Integer version) {
         this.version = version;
     }
 
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
 
     public void setAccountExpired(boolean accountExpired) {
         this.accountExpired = accountExpired;
     }
 
     public void setAccountLocked(boolean accountLocked) {
         this.accountLocked = accountLocked;
     }
 
     public void setCredentialsExpired(boolean credentialsExpired) {
         this.credentialsExpired = credentialsExpired;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean equals(Object o) {
         if (this == o) {
             return true;
         }
         if (!(o instanceof User)) {
             return false;
         }
 
         final User user = (User) o;
 
         return !(username != null ? !username.equals(user.getUsername()) : user.getUsername() != null);
 
     }
 
     /**
      * {@inheritDoc}
      */
     public int hashCode() {
         return (username != null ? username.hashCode() : 0);
     }
 
     /**
      * {@inheritDoc}
      */
     public String toString() {
         ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE).append("username", this.username)
                 .append("enabled", this.enabled).append("accountExpired", this.accountExpired)
                 .append("credentialsExpired", this.credentialsExpired).append("accountLocked", this.accountLocked);
 
         GrantedAuthority[] auths = this.getAuthorities();
         if (auths != null) {
             sb.append("Granted Authorities: ");
 
             for (int i = 0; i < auths.length; i++) {
                 if (i > 0) {
                     sb.append(", ");
                 }
                 sb.append(auths[i].toString());
             }
         } else {
             sb.append("No Granted Authorities");
         }
         return sb.toString();
     }
 }
