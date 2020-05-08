 package edu.northwestern.bioinformatics.studycalendar.domain;
 
 import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
 import org.acegisecurity.GrantedAuthority;
 import org.acegisecurity.userdetails.UserDetails;
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 import org.hibernate.annotations.GenericGenerator;
 import org.hibernate.annotations.Parameter;
 
 import javax.persistence.Entity;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 @Entity
 @Table (name = "users")
 @GenericGenerator(name="id-generator", strategy = "native",
     parameters = {
         @Parameter(name="sequence", value="seq_users_id")
     }
 )
 public class User extends AbstractMutableDomainObject implements Named, Serializable, UserDetails {
     private String name;
     private String firstName;
     private String middleName;
     private String lastName;
     private Long csmUserId;
     private Set<UserRole> userRoles;
     private Boolean activeFlag;
     private List<StudySubjectAssignment> studySubjectAssignments;
 
     public User() {
         this.userRoles = new LinkedHashSet<UserRole>();
         this.studySubjectAssignments = new ArrayList<StudySubjectAssignment>();
         this.activeFlag = true;
     }
 
     ////// LOGIC
 
     @Transient
     public String getDisplayName() {
         StringBuffer sb = new StringBuffer();
         if (firstName != null) {
             sb.append(firstName);
             sb.append(" ");
         }
         if (middleName != null) {
             sb.append(middleName);
             sb.append(" ");
         }
         if (lastName != null) {
             sb.append(lastName);
         }
 
         String displayName = sb.toString();
        if (displayName != null && displayName.length() > 0) {
             return displayName;
         } else {
             return name;
         }
     }
 
     public boolean hasRole(Role role) {
         return getUserRole(role) != null;
     }
 
     public UserRole getUserRole(Role role) {
         for (UserRole userRole : getUserRoles()) {
             if (role.equals(userRole.getRole())) return userRole;
         }
         return null;
     }
 
     public void addUserRole(UserRole userRole) {
         userRoles.add(userRole);
     }
 
     public void removeUserRole(UserRole userRole) {
         userRoles.remove(userRole);
     }
 
     public void clearUserRoles() {
         userRoles.clear();
     }
 
     public void addAllUserRoles(Set<UserRole> srcRoles) {
         this.userRoles.addAll(srcRoles);
     }
 
     public boolean hasAssignment(Site site) {
         for (StudySubjectAssignment assignment : getStudySubjectAssignments()) {
             if (assignment.getStudySite().getSite().equals(site)) return true;
         }
         return false;
     }
 
     public boolean hasAssignment(StudySite ss) {
         for (StudySubjectAssignment assignment : getStudySubjectAssignments()) {
             if (assignment.getStudySite().equals(ss)) return true;
         }
         return false;
     }
 
     ////// IMPLEMENTATION OF UserDetails
 
     @Transient
     public GrantedAuthority[] getAuthorities() {
         Role[] authorities = new Role[getUserRoles().size()];
         int i = 0;
         for (UserRole userRole : getUserRoles()) {
             authorities[i] = userRole.getRole();
             i++;
         }
         return authorities;
     }
 
     @Transient
     public String getPassword() {
         return "PASSWORD NOT AVAILABLE FOR " + getClass().getName();
     }
 
     @Transient
     public String getUsername() {
         return getName();
     }
 
     @Transient
     public boolean isAccountNonExpired() {
         return true;
     }
 
     @Transient
     public boolean isAccountNonLocked() {
         return true;
     }
 
     @Transient
     public boolean isCredentialsNonExpired() {
         return true;
     }
 
     @Transient
     public boolean isEnabled() {
         return getActiveFlag();
     }
 
     ////// BEAN PROPERTIES
 
     public String getName() {
         return name;
     }
 
     public Long getCsmUserId() {
         return csmUserId;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setCsmUserId(Long csmUserId) {
         this.csmUserId = csmUserId;
     }
 
     public Boolean getActiveFlag() {
         return activeFlag;
     }
 
     public void setActiveFlag(Boolean activeFlag) {
         this.activeFlag = activeFlag;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getMiddleName() {
         return middleName;
     }
 
     public void setMiddleName(String middleName) {
         this.middleName = middleName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     @OneToMany (mappedBy = "user")
     @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN})
     public Set<UserRole> getUserRoles() {
         return userRoles;
     } 
 
     public void setUserRoles(Set<UserRole> userRoles) {
         this.userRoles = userRoles;
     }
 
     @OneToMany (mappedBy = "subjectCoordinator")
     @OrderBy // order by ID for testing consistency
     @Cascade (value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
     public List<StudySubjectAssignment> getStudySubjectAssignments() {
         return studySubjectAssignments;
     }
 
     public void setStudySubjectAssignments(List<StudySubjectAssignment> studySubjectAssignments) {
         this.studySubjectAssignments = studySubjectAssignments;
     }
 
     ////// OBJECT METHODS
 
     @Override
     public String toString() {
         return getName();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         User otherUser = (User) o;
 
         boolean namesEq = !(name != null ? !name.equals(otherUser.name) : otherUser.name != null);
         boolean rolesEq = true;
         for (UserRole thisUserRole : getUserRoles()) {
             UserRole otherUserRole = otherUser.getUserRole(thisUserRole.getRole());
             rolesEq = rolesEq &&
                 otherUserRole != null &&
                 otherUserRole.getSites().equals(thisUserRole.getSites()); 
         }
 
         return namesEq && rolesEq;
 
     }
 
     @Override
     public int hashCode() {
         return (name != null ? name.hashCode() : 0);
     }
 }
