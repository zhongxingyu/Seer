 /*
 jGuard is a security framework based on top of jaas (java authentication and authorization security).
 it is written for web applications, to resolve simply, access control problems.
 version $Name$
 http://sourceforge.net/projects/jguard/
 
 Copyright (C) 2004  Charles GAY
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 
 jGuard project home page:
 http://sourceforge.net/projects/jguard/
 
 */
 package net.sf.jguard.core.principals;
 
 import net.sf.jguard.core.PolicyEnforcementPointOptions;
 import net.sf.jguard.core.authorization.Permission;
 
 import javax.persistence.*;
 import java.util.*;
 
 
 /**
  * This Principal represents the notion of a role.
  * it is owned by an Organization.
  * it is the link between Authentication and Authorization parts.
  *
  * @author <a href="mailto:diabolo512@users.sourceforge.net">Charles Gay</a>
  * @author <a href="mailto:vinipitta@users.sourceforge.net">VinÃ­cius Pitta Lima de Araujo</a>
  * @author <a href="mailto:tandilero@users.sourceforge.net">Maximiliano Batelli</a>
  */
 @Entity
 public class RolePrincipal implements BasePrincipal, Cloneable {
 
 
     /**
      * serial version id.
      */
     private static final long serialVersionUID = 3761412993065431095L;
 
     private String localName = "";
 
     //indicate to which application this role apply
     private String applicationName = PolicyEnforcementPointOptions.DEFAULT_APPLICATION_NAME.getLabel();
 
     //all the permissions  owned by this Principal
     @OneToMany(mappedBy = "rolePrincipal",cascade=CascadeType.ALL,fetch=FetchType.EAGER)
     private Set<Permission> permissions = new HashSet<Permission>();
 
     private boolean active = true;
     private String definition = "true";
     //define the owner of the RolePrincipal, which is the only one which can modify the
    //RolePrincipal.Note taht a RolePrincipal can only be owned by one Organization, but be granted
     //to multiple users of multiple organizations.
     //changes done by the owner impact all users with this RolePrincipal.
     private Organization organization = null;
     @Id @GeneratedValue
     private long id;
 
     /**
      * All principals that this role inherites from. This property is use to implement the General Hierarchy
      * proposed by the NIST.
      */
     @OneToMany(mappedBy = "ascendant",cascade=CascadeType.ALL, fetch=FetchType.EAGER)
     private Set<RolePrincipal> descendants = new HashSet<RolePrincipal>();
     @ManyToOne
     private RolePrincipal ascendant;
     private static final String STAR = "*";
     private static final String LOCAL_NAME_APPLICATION_NAME_SEPARATOR = "#";
     private static final String FULL_NAME_LABEL = "fullName";
     private static final String APPLICATION_NAME_LABEL = "applicationName";
     private static final String LOCAL_NAME_LABEL = "localName";
 
 
     public RolePrincipal(){}
 
 
     /**
      * constructor.
      *
      * @param fullName applicationName#localName
      */
     public RolePrincipal(String fullName) {
         checkNullOrEmptyParameter(fullName, FULL_NAME_LABEL);
         List<String> names = extractNames(fullName);
         applicationName = names.get(0);
         localName = names.get(1);
     }
 
 
     /**
      * constructor.
      * only used in the <b>Authorization</b> part.
      *
      * @param localName
      * @param applicationName
      */
     public RolePrincipal(String localName, String applicationName) {
         checkNullOrEmptyParameter(localName, LOCAL_NAME_LABEL);
         checkNullOrEmptyParameter(applicationName, APPLICATION_NAME_LABEL);
         this.localName = localName;
         this.applicationName = applicationName;
 
     }
 
 
     /**
      * constructor.
      *
      * @param localName
      * @param applicationName
      * @param organizationOwner
      */
     public RolePrincipal(String localName, String applicationName, Organization organizationOwner) {
         checkNullOrEmptyParameter(localName, LOCAL_NAME_LABEL);
         checkNullOrEmptyParameter(applicationName, APPLICATION_NAME_LABEL);
         if (organizationOwner == null) {
             throw new IllegalArgumentException("mandatory organizationOwner is null");
         }
         this.localName = localName;
         this.applicationName = applicationName;
         organization = organizationOwner;
     }
 
     /**
      * Copy constructor.
      *
      * @param fullName        applicationName#localName
      * @param principalToCopy
      */
     public RolePrincipal(String fullName, RolePrincipal principalToCopy) {
         checkNullOrEmptyParameter(fullName, FULL_NAME_LABEL);
         if (principalToCopy == null) {
             throw new IllegalArgumentException("principalToCopy is null");
         }
         List<String> names = extractNames(fullName);
         applicationName = names.get(0);
         localName = names.get(1);
         organization = principalToCopy.getOrganization();
         this.permissions = principalToCopy.getPermissions();
         this.descendants = principalToCopy.getDescendants();
     }
 
     public static Permission translateToJGuardPermission(java.security.Permission permission){
         return new Permission(permission.getClass(),permission.getName(),permission.getActions());
     }
 
     public static Collection<java.security.Permission> translateToJavaPermissions(Collection<Permission> permissionColl) {
         Collection<java.security.Permission> permissions = new HashSet<java.security.Permission>();
         for (Permission permission:permissionColl){
                 permissions.add(permission.toJavaPermission());
         }
         return permissions;
     }
 
 
     private void checkNullOrEmptyParameter(String localName, String label) {
         if (localName == null || "".equals(localName)) {
             throw new IllegalArgumentException("mandatory '" + label + "' parameter is null ");
         }
     }
 
     /**
      * override the java.lang.Object 's <i>clone</i> method.
      *
      * @return new JGuardPricipal with the same internal references (permissions and Domains).
      */
     public Object clone() throws CloneNotSupportedException {
         RolePrincipal clone = (RolePrincipal) super.clone();
         clone.setApplicationName(applicationName);
         clone.setLocalName(localName);
         clone.setOrganization(this.organization);
         clone.setPermissions(new HashSet<Permission>(this.permissions));
         clone.setDescendants(new HashSet<RolePrincipal>(this.descendants));
         return clone;
 
     }
 
     /**
      * return  the unique name formed by <i>localname#applicationName.</i>
      *
      * @see java.security.Principal#getName()
      */
     public String getName() {
         return getName(localName, applicationName);
     }
 
 
     /**
      * @param localName
      * @return
      * @see java.security.Principal#getName()
      */
     public static String getName(String localName) {
         return getName(localName, STAR);
     }
 
     /**
      * return a <i>composed</i> name of 'applicationName#LocalName'.
      *
      * @param localName
      * @param applicationName
      * @return composed name
      * @see java.security.Principal#getName()
      */
     public static String getName(String localName, String applicationName) {
         StringBuilder sb = new StringBuilder();
         sb.append(applicationName);
         sb.append(LOCAL_NAME_APPLICATION_NAME_SEPARATOR);
         sb.append(localName);
         return sb.toString();
     }
 
     /**
      * compare an object to this RolePrincipal.
      * override the Object's <i>equals</i> method.
      *
      * @param another object to compare
      * @return true if another is equals to this RolePrincipal
      */
     public boolean equals(Object another) {
 
         if (another instanceof RolePrincipal) {
             RolePrincipal principal = (RolePrincipal) another;
             if (principal.getName().equals(this.getName())) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * override the Object's <i>toString</i> method.
      * return String representation
      */
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append(" principal class name =");
         sb.append(getClass().getName());
         sb.append("\n");
         sb.append(" principal localName =");
         sb.append(localName);
         sb.append("\n");
         sb.append(" principal application name =");
         sb.append(applicationName);
         sb.append("\n");
         sb.append(" organization owner =");
         sb.append(getOrganization());
         sb.append("\n");
         sb.append(" principal permissions =");
         sb.append(permissions);
         sb.append("\n");
         sb.append(" principal descendants =");
         sb.append(descendants);
         sb.append("\n");
         return sb.toString();
     }
 
     /**
      * override Object's <i>hashcode</i> method.
      */
     public int hashCode() {
         int i = 0;
         i += localName.hashCode() + applicationName.hashCode();
         if (organization != null) {
             i += organization.hashCode();
         }
         return i;
     }
 
     /**
      * @param fullName applicationName#localName
      * @return list string with first name is application name, and second name is local/role name
      */
     public static List<String> extractNames(String fullName) {
         List<String> names = new ArrayList<String>(2);
         String[] tokens = fullName.split(LOCAL_NAME_APPLICATION_NAME_SEPARATOR);
         if (tokens.length == 1) {
             names.add(0, STAR);
             names.add(1, tokens[0]);
         } else if (tokens.length == 2) {
             names.add(0, tokens[0]);
             names.add(1, tokens[1]);
         } else {
             throw new IllegalArgumentException(" name is composed of applicationName#localName");
         }
 
         return names;
     }
 
     /**
      * return all permissions owned by this Principal plus
      * permissions inherited from descendants.
      *
      * @return permissions
      */
     public Set<java.security.Permission> getAllPermissions() {
         //all permissions owned by this principal
         Set<java.security.Permission> allPermissions = new HashSet<java.security.Permission>();
         allPermissions.addAll(translateToJavaPermissions(permissions));
 
         //get inherited permissions
         for (RolePrincipal descendant : descendants) {
             allPermissions.addAll(descendant.getAllPermissions());
         }
 
         return allPermissions;
     }
 
     /**
      * return the permissions bounded to a domain plus orphanedPermisions.
      *
      * @return
      */
     public Set<Permission> getPermissions() {
         return permissions;
     }
 
 
     /**
      * @param perms set of permissions defined
      */
     public void setPermissions(Set<Permission> perms) {
         for (Permission perm1 : perms) {
             permissions.add(perm1);
         }
     }
 
     /**
      * add a permission to the RolePrincipal.
      *
      * @param permission permission to add
      */
     public void addPermission(Permission permission) {
         permissions.add(permission);
     }
 
 
     /**
      * add a permission to the RolePrincipal.
      *
      * @param permission permission to add
      */
     public void addPermission(java.security.Permission permission) {
         permissions.add(translateToJGuardPermission(permission));
     }
 
 
     /**
      * @return application name
      */
     public String getApplicationName() {
         return applicationName;
     }
 
     /**
      * define application name.
      *
      * @param string application name
      */
     public void setApplicationName(String string) {
         applicationName = string;
     }
 
 
 
 
 
     /**
      * method used to compare two objects.
      * this method is used in Collection to <strong>order</strong> items, and MUST be
      * consistent with the <i>equals</i> method (eache method should return 0/true in the same cases).
      *
      * @param o object to compare
      * @return 0 if both objects are equals,
      *         &lt;0 if 0 is lesser than the RolePrincipal,
      *         &gt;0 if 0 is greater than the RolePrincipal
      * @see java.lang.Comparable#compareTo(java.lang.Object)
      * @see java.lang.Comparable#compareTo(java.lang.Object)
      */
     public int compareTo(Object o) {
         RolePrincipal principal = (RolePrincipal) o;
         if (this.equals(o)) {
             return 0;
         }
 
         return this.getName().compareTo(principal.getName());
     }
 
 
 
     public Set<RolePrincipal> getDescendants() {
         return descendants;
     }
 
     void setDescendants(Set<RolePrincipal> descendants) {
         this.descendants = descendants;
     }
 
     public boolean isActive() {
         return active;
     }
 
     public void setActive(boolean active) {
         this.active = active;
     }
 
     /**
      * @return Returns definition.
      */
     public String getDefinition() {
         return definition;
     }
 
     /**
      * @param definition The definition to set.
      */
     public void setDefinition(String definition) {
         this.definition = definition;
     }
 
     public String getLocalName() {
         return localName;
     }
 
     void setLocalName(String localName) {
         this.localName = localName;
     }
 
     public Organization getOrganization() {
         return organization;
     }
 
     public void setOrganization(Organization organization) {
         this.organization = organization;
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
 
     public RolePrincipal getAscendant() {
         return ascendant;
     }
 
     public void setAscendant(RolePrincipal ascendant) {
         this.ascendant = ascendant;
     }
 
     @PreRemove
     public void preRemove(){
         for(Permission permission:permissions){
             permission.setRolePrincipal(null);
         }
         permissions.clear();
         if(getAscendant()!=null){
             getAscendant().getDescendants().remove(this);
             setAscendant(null);
         }
     }
 }
