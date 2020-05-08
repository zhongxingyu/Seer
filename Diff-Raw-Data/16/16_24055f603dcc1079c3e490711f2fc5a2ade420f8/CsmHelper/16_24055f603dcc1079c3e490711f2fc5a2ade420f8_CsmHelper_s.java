 package gov.nih.nci.cabig.ctms.suite.authorization;
 
 import gov.nih.nci.cabig.ctms.CommonsError;
 import gov.nih.nci.security.AuthorizationManager;
 import gov.nih.nci.security.authorization.domainobjects.Group;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
 import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
 import gov.nih.nci.security.authorization.domainobjects.Role;
 import gov.nih.nci.security.dao.GroupSearchCriteria;
 import gov.nih.nci.security.dao.ProtectionGroupSearchCriteria;
 import gov.nih.nci.security.dao.RoleSearchCriteria;
 import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
 import gov.nih.nci.security.exceptions.CSTransactionException;
 
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Provides low-level analysis and creation for Suite-specific CSM objects.
  *
  * @author Rhett Sutphin
  */
 public class CsmHelper {
     public static final String SUITE_APPLICATION_NAME = "CTMS_SUITE";
 
     private Map<ScopeType, IdentifiableInstanceMapping> mappings;
     private AuthorizationManager authorizationManager;
 
     public CsmHelper() {
         mappings = new LinkedHashMap<ScopeType, IdentifiableInstanceMapping>();
     }
 
     /**
      * Returns the PE for the given scope description.  If it doesn't already exist, it will be
      * created.
      */
     public ProtectionElement getOrCreateScopeProtectionElement(ScopeDescription description) {
         return ensureProtectionPairExists(description.getCsmName()).getProtectionElement();
     }
 
     /**
      * Returns the PE for the given scope and object.  If it doesn't already exist, it will be
      * created.
      * <p>
      * This method requires that a site instance mapping be configured.
      *
      * @see #setSiteMapping
      */
     @SuppressWarnings({"unchecked"})
     public ProtectionElement getOrCreateScopeProtectionElement(ScopeType scope, Object scopeObject) {
         return getOrCreateScopeProtectionElement(
             ScopeDescription.createForOne(scope, getMapping(scope).getSharedIdentity(scopeObject)));
     }
 
     /**
      * Returns the PG for the given scope description.  If it doesn't already exist, it will be
      * created.
      */
     public ProtectionGroup getOrCreateScopeProtectionGroup(ScopeDescription description) {
         return ensureProtectionPairExists(description.getCsmName()).getProtectionGroup();
     }
 
     /**
      * Returns the PG for the given scope and object.  If it doesn't already exist, it will be
      * created.
      * <p>
      * This method requires that a study instance mapping be configured.
      *
      * @see #setStudyMapping
      */
     @SuppressWarnings({"unchecked"})
     public ProtectionGroup getOrCreateScopeProtectionGroup(ScopeType scope, Object scopeObject) {
         return getOrCreateScopeProtectionGroup(ScopeDescription.createForOne(
             scope, getMapping(scope).getSharedIdentity(scopeObject)));
     }
 
     /**
      * Returns the CSM group object for the given suite logical role.  It must already exist.
      *
      * @throws SuiteAuthorizationAccessException if anything other than exactly one CSM group is
      *   found for the role
      */
     public Group getRoleCsmGroup(SuiteRole role) throws SuiteAuthorizationAccessException {
         Group example = new Group();
         example.setGroupName(role.getCsmName());
         List found = getAuthorizationManager().getObjects(new GroupSearchCriteria(example));
         if (found.size() == 0) {
             throw new SuiteAuthorizationAccessException("Missing CSM group for suite role %s (%s)", 
                 role.getDisplayName(), role.getCsmName());
         } else if (found.size() > 1) {
             throw new SuiteAuthorizationAccessException("Too many CSM groups found for suite role %s (%s): %s",
                 role.getDisplayName(), role.getCsmName(), found);
         } else {
             return (Group) found.get(0);
         }
     }
 
     /**
      * Returns the CSM role object for the given suite logical role.  It must already exist.
      *
      * @throws SuiteAuthorizationAccessException if anything other than exactly one CSM role is
      *   found for the suite role.
      */
     public Role getRoleCsmRole(SuiteRole role) {
         Role example = new Role();
         example.setName(role.getCsmName());
         List found = getAuthorizationManager().getObjects(new RoleSearchCriteria(example));
         if (found.size() == 0) {
             throw new SuiteAuthorizationAccessException("Missing CSM role for suite role %s (%s)",
                 role.getDisplayName(), role.getCsmName());
         } else if (found.size() > 1) {
             throw new SuiteAuthorizationAccessException("Too many CSM roles found for suite role %s (%s): %s",
                 role.getDisplayName(), role.getCsmName(), found);
         } else {
             return (Role) found.get(0);
         }
     }
 
     ////// INTERNAL
 
     private ProtectionPair ensureProtectionPairExists(String csmName) {
         ProtectionElement pe = ensureProtectionElementExists(csmName);
         ProtectionGroup pg = ensureProtectionGroupExists(csmName);
         ensurePgPeLink(pe, pg);
 
         return new ProtectionPair(pg, pe);
     }
 
     private ProtectionElement ensureProtectionElementExists(String csmName) {
         ProtectionElement existing = null;
        try {
            existing = getAuthorizationManager().getProtectionElement(csmName);
        } catch (CSObjectNotFoundException e) {
            // fall through
         }
         if (existing == null) {
             ProtectionElement newPe = new ProtectionElement();
             newPe.setProtectionElementName(csmName);
             newPe.setObjectId(csmName);
             try {
                 getAuthorizationManager().createProtectionElement(newPe);
             } catch (CSTransactionException e) {
                 throw new SuiteAuthorizationProvisioningFailure(e);
             }
             // reload to ensure that we always return an object with the same set of fields
             // filled in
             try {
                 existing = getAuthorizationManager().getProtectionElement(csmName);
             } catch (CSObjectNotFoundException e) {
                 throw new SuiteAuthorizationProvisioningFailure(
                     "Failed to reload the ProtectionElement that was just created.", e);
             }
         }
         return existing;
     }
 
     private ProtectionGroup ensureProtectionGroupExists(String csmName) {
         ProtectionGroup existing = getProtectionGroup(csmName);
         if (existing == null) {
             ProtectionGroup newPg = new ProtectionGroup();
             newPg.setProtectionGroupName(csmName);
             try {
                 getAuthorizationManager().createProtectionGroup(newPg);
             } catch (CSTransactionException e) {
                 throw new SuiteAuthorizationProvisioningFailure(e);
             }
             // reload to ensure that we always return an object with the same set of fields
             // filled in
             existing = getProtectionGroup(csmName);
         }
         return existing;
     }
 
     /**
      * Get a single PG by name, or return null.
      */
     private ProtectionGroup getProtectionGroup(String name) {
         ProtectionGroup template = new ProtectionGroup();
         template.setProtectionGroupName(name);
         List matches
             = getAuthorizationManager().getObjects(new ProtectionGroupSearchCriteria(template));
         if (matches.size() == 0) {
             return null;
         } else if (matches.size() == 1) {
             return (ProtectionGroup) matches.get(0);
         } else {
             throw new CommonsError(
                 "There are two or more PGs named " + name + ".  This shouldn't be possible.");
         }
     }
 
     private void ensurePgPeLink(ProtectionElement pe, ProtectionGroup pg) {
         boolean linked = false;
         try {
             Set eltForGroup = getAuthorizationManager().
                 getProtectionElements(pg.getProtectionGroupId().toString());
             for (Object o : eltForGroup) {
                 ProtectionElement elt = (ProtectionElement) o;
                 if (elt.getProtectionElementId().equals(pe.getProtectionElementId())) {
                     linked = true;
                     break;
                 }
             }
         } catch (CSObjectNotFoundException e) {
             linked = false;
         }
 
         if (!linked) {
             try {
                 getAuthorizationManager().addProtectionElements(
                     pg.getProtectionGroupId().toString(),
                     new String[] { pe.getProtectionElementId().toString() });
             } catch (CSTransactionException e) {
                 throw new SuiteAuthorizationProvisioningFailure("Linking PE {} and PG {} failed", e,
                     pe.getProtectionElementId(), pg.getProtectionGroupId());
             }
         }
     }
 
     ///// CONFIGURATION
 
     protected IdentifiableInstanceMapping getMapping(ScopeType scope) {
         IdentifiableInstanceMapping mapping = mappings.get(scope);
         if (mapping == null) {
             throw new SuiteAuthorizationProvisioningFailure(
                 "No %s mapping was provided.  Either provide one or stick to the identifier-based methods.",
                 scope.getName());
         }
         return mapping;
     }
 
     public void setSiteMapping(SiteMapping mapping) {
         mappings.put(ScopeType.SITE, mapping);
     }
 
     public void setStudyMapping(StudyMapping mapping) {
         mappings.put(ScopeType.STUDY, mapping);
     }
 
     protected AuthorizationManager getAuthorizationManager() {
         return authorizationManager;
     }
 
     public void setAuthorizationManager(AuthorizationManager authorizationManager) {
         this.authorizationManager = authorizationManager;
     }
 
     ////// INNER CLASSES
 
     /** A struct for returning both a PG and a PE from a single method */
     private static class ProtectionPair {
         private ProtectionGroup protectionGroup;
         private ProtectionElement protectionElement;
 
         private ProtectionPair(ProtectionGroup protectionGroup, ProtectionElement protectionElement) {
             this.protectionGroup = protectionGroup;
             this.protectionElement = protectionElement;
         }
 
         public ProtectionGroup getProtectionGroup() {
             return protectionGroup;
         }
 
         public ProtectionElement getProtectionElement() {
             return protectionElement;
         }
     }
 }
