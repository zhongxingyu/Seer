 package org.wyona.security.util;
 
 import org.apache.log4j.Logger;
 
 import org.wyona.security.core.api.Group;
 import org.wyona.security.core.api.GroupManager;
 
 import java.util.ArrayList;
 
 /**
  *
  */
 public class GroupUtil {
 
     private static Logger log = Logger.getLogger(GroupUtil.class);
 
     /**
      * Get parent group IDs of a particular group
      *
      * @param group Particular group
      * @param gm Group manager to which particular group belongs to
      * @param parentsOfParents Flag whether only direct parents or parents of parents shall be retrieved
      */
     public static String[] getParentGroupIDs(Group group, GroupManager gm, boolean parentsOfParents) throws org.wyona.security.core.api.AccessManagementException {
         if (gm != null) {
             ArrayList<String> groupIDs = new ArrayList<String>();
 
             Group[] parents = group.getParents();
             if (parents != null) {
                 for (int i = 0; i < parents.length; i++) {
                     groupIDs.add(parents[i].getID());
                 }
 
             if (parentsOfParents) {
                 log.info("Resolve parent groups for group '" + group.getID() + "' ...");
 
                 ArrayList<String> groupIDsInclParents = new ArrayList<String>();
                 ArrayList<String> branchGroups = new ArrayList<String>();
                 for (int i = 0; i < groupIDs.size(); i++) {
                     try {
                         // TOOD: Replace this implementation
                         groupIDsInclParents.add((String) groupIDs.get(i));
                         branchGroups.add((String) groupIDs.get(i)); // INFO: Add in order to detect loops with a particular branch
                         getParentGroupIDsImplV2((String) groupIDs.get(i), gm, branchGroups, groupIDsInclParents);
                         branchGroups.remove((String) groupIDs.get(i)); // INFO: Remove in order to avoid "phantom" loops with regard to multiple branches
                     } catch(Exception e) {
                         log.error(e, e);
                     }
                 }
                 log.debug("Get parent group IDs of group '" + group.getID() + "' including parents of parents: " + groupIDsInclParents.size());
                 return (String[]) groupIDsInclParents.toArray(new String[groupIDsInclParents.size()]);
             } else {
                 log.debug("Get parent group IDs of group '" + group.getID() + "' excluding parents of parents: " + groupIDs.size());
                 return (String[]) groupIDs.toArray(new String[groupIDs.size()]);
             }
             } else {
                log.warn("Group '" + group.getID() + "'  has not parents!");
                 return null;
             }
         } else {
             log.error("Group manager is null!");
             return null;
         }
     }
 
     /**
      * Get parent group IDs of a particular group
      *
      * @param groupID ID of particular group
      * @param gm Group manager
      * @param branchGroups Group IDs within a specific branch (in order to detect loops with a particular branch, but also in order to avoid "phantom" loops with regard to multiple branches)
      * @param groupIDsInclParents Group IDs which have already been found
      */
     private static void getParentGroupIDsImplV2(String groupID, GroupManager gm, ArrayList<String> branchGroups, ArrayList<String> groupIDsInclParents) throws Exception {
         log.debug("Get parent group IDs for particular group: " + groupID);
 
         if (gm != null) {
             Group[] parentGroups = gm.getGroup(groupID).getParents();
             if (parentGroups != null) {
                 for (int i = 0; i < parentGroups.length; i++) {
                     String parentGroupID = parentGroups[i].getID();
                     log.debug("Group '" + groupID + "' is group member of parent group: " + parentGroupID);
 
                     boolean alreadyContainedWithinBranch = false;
                     //log.debug("DEBUG: Depth of branch: " + branchGroups.size());
                     for (int k = 0; k < branchGroups.size(); k++) {
                         if (branchGroups.get(k).equals(parentGroupID)) {
                             log.error("Maybe loop detected for group '" + groupID + "' and parent group '" + parentGroupID + "' or root group '" + parentGroupID + "' reached! Group resolving will be aborted in order to avoid loop.");
                             alreadyContainedWithinBranch = true;
                             break;
                         }
                     }
 
                     if (!alreadyContainedWithinBranch) {
 
                         boolean alreadyPartOfList = false;
                         for (int k = 0; k < groupIDsInclParents.size(); k++) {
                             if (groupIDsInclParents.get(k).equals(parentGroupID)) {
                                 alreadyPartOfList = true;
                                 break;
                             }
                         }
                         if (!alreadyPartOfList) {
                             groupIDsInclParents.add(parentGroupID);
                         }
 
                         branchGroups.add(parentGroupID); // INFO: Add parent group in order to detect loops within this particular branch
                         getParentGroupIDsImplV2(parentGroupID, gm, branchGroups, groupIDsInclParents);
                         branchGroups.remove(parentGroupID); // INFO: Remove parent group in order to avoid "phantom" loops with regard to multiple branches
                     }
                 }
             } else {
                 log.debug("Group '" + groupID + "' does not seem to have parent groups.");
             }
         } else {
             log.error("Group manager is null!");
         }
     }
 }
