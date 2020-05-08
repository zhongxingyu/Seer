 package pl.agh.enrollme.model;
 
 import org.hibernate.annotations.Fetch;
 import org.hibernate.annotations.FetchMode;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 
 @Entity
 public class Person implements Serializable, UserDetails {
 
     @Transient
     private static final long serialVersionUID = -5777367229609230476L;
 
     @Transient
     private final List<GrantedAuthority> authorityList = new ArrayList<>();
 
     @Id
     @GeneratedValue
     private Integer id = 0;
 
     private String password = "";
 
     @Column(unique = true)
     private String username = "";
 
     private String firstName = "";
 
     private String lastName = "";
 
 
     @Column(unique = true)
     private Integer indeks;
 
     private Boolean accountNonExpired = true;
 
     private Boolean accountNonLocked = true;
 
     private Boolean credentialsNonExpired = true;
 
     private Boolean enabled = true;
 
     private String rolesToken = "ROLE_USER";
 
     @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
     @Fetch(value = FetchMode.SUBSELECT)
     private List<Group> groups;
 
     @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
     private List<Subject> subjects = new ArrayList<>();
 
     @ManyToMany(mappedBy = "persons", fetch = FetchType.LAZY)
     private List<Enroll> availableEnrolls = new ArrayList<>();
 
     @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @Fetch(value = FetchMode.SUBSELECT)
     private List<Subject> subjectsSaved = new ArrayList<>();
 
 
     public Person() {
     }
 
     public Person(String password, String username, String firstName, String lastName, Integer indeks,
                   Boolean accountNonExpired, Boolean accountNonLocked, Boolean credentialsNonExpired,
                   Boolean enabled, String rolesToken, List<Group> groups, List<Subject> subjects, List<Subject> subjectsSaved) {
         this.password = password;
         this.username = username;
         this.firstName = firstName;
         this.lastName = lastName;
         this.indeks = indeks;
         this.accountNonExpired = accountNonExpired;
         this.accountNonLocked = accountNonLocked;
         this.credentialsNonExpired = credentialsNonExpired;
         this.enabled = enabled;
         this.rolesToken = rolesToken;
         this.groups = groups;
         this.subjects = subjects;
         this.subjectsSaved = subjectsSaved;
     }
 
     public List<Subject> getSubjectsSaved() {
         return subjectsSaved;
     }
 
     public void setSubjectsSaved(List<Subject> subjectsSaved) {
         this.subjectsSaved = subjectsSaved;
     }
 
     public void addSubject(Subject subject) {
         if (!subjects.contains(subject)) {
             subjects.add(subject);
         }
     }
 
     public void addGroups(Group group) {
         groups.add(group);
     }
 
     public Integer getIndeks() {
         return indeks;
     }
 
     public void setIndeks(Integer indeks) {
         this.indeks = indeks;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public List<Enroll> getAvailableEnrolls() {
         return availableEnrolls;
     }
 
     public void setAvailableEnrolls(List<Enroll> availableEnrolls) {
         this.availableEnrolls = availableEnrolls;
     }
 
     public void setGroups(List<Group> groups) {
         this.groups = groups;
     }
 
     public void setSubjects(List<Subject> subjects) {
         this.subjects = subjects;
     }
 
     public List<Group> getGroups() {
         return groups;
     }
 
     public List<Subject> getSubjects() {
         return subjects;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     @Override
     public Collection<? extends GrantedAuthority> getAuthorities() {
         updateAuthorityList(rolesToken);
         return authorityList;
     }
 
     @Override
     public String getPassword() {
         return password;
     }
 
     @Override
     public String getUsername() {
         return username;
     }
 
     @Override
     public boolean isAccountNonExpired() {
         return accountNonExpired;
     }
 
     @Override
     public boolean isAccountNonLocked() {
         return accountNonLocked;
     }
 
     @Override
     public boolean isCredentialsNonExpired() {
         return credentialsNonExpired;
     }
 
     @Override
     public boolean isEnabled() {
         return enabled;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public void setAccountNonExpired(Boolean accountNonExpired) {
         this.accountNonExpired = accountNonExpired;
     }
 
     public void setAccountNonLocked(Boolean accountNonLocked) {
         this.accountNonLocked = accountNonLocked;
     }
 
     public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
         this.credentialsNonExpired = credentialsNonExpired;
     }
 
     public void setEnabled(Boolean enabled) {
         this.enabled = enabled;
     }
 
     public String getRolesToken() {
         return rolesToken;
     }
 
     public void setRolesToken(String roles) {
         this.rolesToken = roles;
         updateAuthorityList(roles);
     }
 
     /**
      * Updates authority list based on rolestoken provided. Uses regex to split roles into tokens.
      * Accepts role tokens such as: ROLE_1, ROLE_2 :;| ROLE_3:::ROLE_4
      * @param roles
      */
     private void updateAuthorityList(String roles) {
         final String[] split = roles.split("[.,;: |]+");
         authorityList.clear();
         for (String token : split) {
             authorityList.add(new SimpleGrantedAuthority(token.trim().toUpperCase()));
         }
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Person)) return false;
 
         Person person = (Person) o;
 
         if (!username.equals(person.username)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return username.hashCode();
     }
 
     @Override
     public String toString() {
         return "Person{" +
                 "authorityList=" + authorityList +
                 ", id=" + id + '}';
     }
 }
