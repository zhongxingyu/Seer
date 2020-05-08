 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Child;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.IndividualRole;
 import fr.cg95.cvq.business.users.RoleType;
 import fr.cg95.cvq.exception.CvqModelException;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.HashMap;
 
 import net.sf.oval.constraint.AssertValid;
 import net.sf.oval.constraint.MinSize;
 import net.sf.oval.constraint.NotNull;
 
 /*
  * Home Folder DTO useful to ease data binding 
  * in VO Card Request and HomeFolder Modification Request
  *
  * FIXME - move business code in business layer !
  */
 public class HomeFolderDTO implements Serializable {
 
     @NotNull(profiles = {"adults"}, message = "")
     @MinSize(value = 1, profiles = {"adults"}, message = "")
     @AssertValid(profiles = {"adults"}, message = "")
    private List<Adult> adults;
 
     @AssertValid(profiles = {"children"}, message = "")
     private List<Child> children;
 
     @AssertValid(profiles = {"foreignAdults"}, message = "")
     private List<Adult> foreignAdults;
 
     private Long homeFolderId;
     
     public HomeFolderDTO() {
         adults = new ArrayList<Adult>();
         children = new ArrayList<Child>();
         foreignAdults = new ArrayList<Adult>();
     }
 
     public HomeFolderDTO (HomeFolder homeFolder, Set<Individual> roleOwners) {
         this();
         homeFolderId = homeFolder.getId();
         for (Individual individual : homeFolder.getIndividuals()) {
             if (individual instanceof Adult)
                 adults.add((Adult)individual);
             else if (individual instanceof Child)
                 children.add((Child)individual);
         }
         
         for (Individual owner : roleOwners) {
             if (!homeFolder.getIndividuals().contains(owner))
                 foreignAdults.add((Adult)owner);
         }
     }
     
     public Set<Map<String, Object>> getRoleOwnersOnIndividual (Individual individual, List<Adult> owners) {
         Set<Map<String, Object>> individuals = new HashSet<Map<String, Object>>();;
         if (owners == null)
             return individuals;
         
         for (Individual owner : owners) {
             Set<IndividualRole> individualRoles = owner.getIndividualRoles()!= null ?
                     owner.getIndividualRoles() : new HashSet<IndividualRole>();
             for (IndividualRole role : individualRoles) {
                 if ((role.getIndividualName() != null 
                         && role.getIndividualName().equals(individual.getFullName()))
                     || (role.getIndividualId() != null 
                         && role.getIndividualId().equals(individual.getId()))) {
                     Map<String, Object> roles = new HashMap<String, Object>();
                     roles.put("index", owners.indexOf(owner));
                     roles.put("role", role.getRole());
                     roles.put("owner", owner);
                     individuals.add(roles);
                 }
             }
         }
         return individuals;
     }
 
     public Set<Map<String, Object>> getRoleOwnersOnHomeFolder(RoleType roleType ,List<Adult> owners) {
         Set<Map<String, Object>> individuals = new HashSet<Map<String, Object>>();
         if (owners == null)
             return individuals;
               
         for (Individual owner : owners) {
             Set<IndividualRole> individualRoles = owner.getIndividualRoles()!= null ?
                     owner.getIndividualRoles() : new HashSet<IndividualRole>();
             for (IndividualRole role : individualRoles) {
                 if (!role.getRole().equals(roleType))
                     continue;
                 if ((role.getHomeFolderId() != null 
                         && role.getHomeFolderId().equals(homeFolderId)) 
                     || (role.getHomeFolderId() == null
                         && role.getIndividualId() == null
                         && role.getIndividualName() == null)) {
                     Map<String, Object> roles = new HashMap<String, Object>();
                     roles.put("index", owners.indexOf(owner));
                     roles.put("role", role.getRole());
                     roles.put("owner", owner);
                     individuals.add(roles);
                 }
             }
         }
         return individuals;
     }
     
     public Set<Map<String, Object>> getHomeFolderResponsibles(List<Adult> owners) {
         return getRoleOwnersOnHomeFolder(RoleType.HOME_FOLDER_RESPONSIBLE ,owners);
     }
     
     public Set<Map<String, Object>> getHomeFolderTutors(List<Adult> owners) {
         return getRoleOwnersOnHomeFolder(RoleType.TUTOR ,owners);
     }
     
     public boolean isLegalsResponsible (Individual individual) {
         if (individual.getIndividualRoles() != null)
             for (IndividualRole role : individual.getIndividualRoles())
                 if (role.getRole().equals(RoleType.HOME_FOLDER_RESPONSIBLE))
                     return true;
 
         return false;
     }
     
     /*
      * - Homefolder must one and  only one homeFolderResponsible
      * - Children must have between one and tree legal responsibles
      */
     public void checkRoles() throws CvqModelException {
         List<RoleType> homeFolderRoleTypes = new ArrayList<RoleType>();
         for (Map<String, Object> rMap : getHomeFolderResponsibles(adults)){
             RoleType rt = (RoleType)rMap.get("role");
             if (rt.equals(RoleType.HOME_FOLDER_RESPONSIBLE))
                 homeFolderRoleTypes.add(rt);
         }
         if (homeFolderRoleTypes.size() < 1)
             throw new CvqModelException("homeFolder.error.responsibleIsRequired");
         else if (homeFolderRoleTypes.size() > 1)
             throw new CvqModelException("homeFolder.error.onlyOneResponsibleIsAllowed");
         
         if (children == null)
             return;
         for (Child child : children) {
             List<RoleType> childRoleTypes = new ArrayList<RoleType>();
             for (Map<String, Object> rMap : getRoleOwnersOnIndividual(child, adults)){
                 RoleType rt = (RoleType)rMap.get("role");
                 if (Arrays.asList(RoleType.childRoleTypes).contains(rt))
                     childRoleTypes.add(rt);
             }
             if (childRoleTypes.size() < 1 || childRoleTypes.size() > 3)
                 throw new CvqModelException("homeFolder.error.illegalLegalResponsiblesNumber", new String[]{child.getFirstName()});
         }
     }
     
     public RoleType[] getAllRoleTypes () {
         return RoleType.allRoleTypes;
     }
     
     public RoleType[] getChildRoleTypes () {
         return RoleType.childRoleTypes;
     }
     
     public List<Adult> getAdults() {
         return adults;
     }
 
     public void setAdults(List<Adult> adults) {
         this.adults = adults;
     }
 
     public List<Child> getChildren() {
         return children;
     }
 
     public void setChildren(List<Child> children) {
         this.children = children;
     }
 
     public List<Adult> getForeignAdults() {
         return foreignAdults;
     }
 
     public void setForeignAdults(List<Adult> foreignAdults) {
         this.foreignAdults = foreignAdults;
     }
 }
