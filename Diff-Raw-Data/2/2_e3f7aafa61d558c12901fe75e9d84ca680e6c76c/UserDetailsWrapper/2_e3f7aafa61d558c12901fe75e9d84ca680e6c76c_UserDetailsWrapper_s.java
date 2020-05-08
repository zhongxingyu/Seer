 package org.mule.galaxy.impl.jcr;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Set;
 
 import javax.naming.directory.Attributes;
 import javax.naming.ldap.Control;
 
 import org.acegisecurity.GrantedAuthority;
 import org.acegisecurity.GrantedAuthorityImpl;
 import org.acegisecurity.userdetails.ldap.LdapUserDetails;
 import org.mule.galaxy.security.Permission;
 import org.mule.galaxy.security.User;
 
 
 public class UserDetailsWrapper implements LdapUserDetails {
 
     private User user;
     private String password;
     private Set<Permission> permissions;
     private GrantedAuthority[] authorities;
     private Attributes attributes;
     private Control[] controls;
     private String userDn;
 
     public UserDetailsWrapper(User user, Set<Permission> set, String password) {
         super();
         this.user = user;
         this.permissions = set;
         this.password = password;
     }
 
     public User getUser() {
         return user;
     }
 
     public GrantedAuthority[] getAuthorities() {
         if (authorities == null) {
             Object[] pArray = permissions.toArray();
             authorities = new GrantedAuthority[pArray.length+1];
             for (int i = 0; i < pArray.length; i++) {
                 authorities[i] = new GrantedAuthorityImpl(pArray[i].toString());
             }
            authorities[pArray.length+1] = new GrantedAuthorityImpl("role_user");
         }
         return authorities;
     }
 
     public String getPassword() {
         return password;
     }
 
     public String getUsername() {
         return user.getUsername();
     }
 
     public boolean isAccountNonExpired() {
         return user.isEnabled();
     }
 
     public boolean isAccountNonLocked() {
         return user.isEnabled();
     }
 
     public boolean isCredentialsNonExpired() {
         return user.isEnabled();
     }
 
     public boolean isEnabled() {
         // TODO Auto-generated method stub
         return user.isEnabled();
     }
 
     public Attributes getAttributes() {
         return attributes;
     }
 
     public void setAttributes(Attributes attributes) {
         this.attributes = attributes;
     }
 
     public Control[] getControls() {
         return controls;
     }
 
     public void setControls(Control[] controls) {
         this.controls = controls;
     }
 
     public String getDn() {
         return userDn;
     }
 
     public void setDn(String dn) {
         userDn = dn;
     }
 
     public void setAuthorities(GrantedAuthority[] auths) {
         ArrayList list = new ArrayList(Arrays.asList(auths));
         list.add(new GrantedAuthorityImpl("role_user"));
         authorities = (GrantedAuthority[]) list.toArray(new GrantedAuthority[0]);
     }
 
     public void setPermissions(Set<Permission> set) {
         permissions = set;
     }
 }
