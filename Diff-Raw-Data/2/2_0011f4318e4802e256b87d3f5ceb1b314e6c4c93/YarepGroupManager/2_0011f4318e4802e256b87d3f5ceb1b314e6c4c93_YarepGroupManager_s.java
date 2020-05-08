 package org.wyona.security.impl.yarep;
 
 import java.util.HashMap;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
 
 import org.apache.log4j.Logger;
 
 import org.wyona.security.core.api.AccessManagementException;
 import org.wyona.security.core.api.Group;
 import org.wyona.security.core.api.GroupManager;
 import org.wyona.security.core.api.IdentityManager;
 import org.wyona.security.core.api.UserManager;
 
 import org.wyona.yarep.core.NoSuchNodeException;
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.NodeType;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryException;
 
 /**
  * The YarepGroupManager expects to find all existing groups under the node /groups.
  * If the node /groups does not exist, it will look under the root node.
  * All files which have &lt;group&gt; as root element will be recognized as a group
  * configuration. 
  */
 public class YarepGroupManager implements GroupManager {
     private static Logger log = Logger.getLogger(YarepGroupManager.class);
     
     protected static final String GROUPS_PARENT_PATH = "/groups";
     private String SUFFIX = "xml";
     
     private Repository identitiesRepository;
     protected UserManager userManager;
     protected HashMap groups;
 
     /**
      * Constructor.
      * @param identityManager
      * @param identitiesRepository
      * @throws AccessManagementException
      */
     public YarepGroupManager(IdentityManager identityManager, Repository identitiesRepository) throws AccessManagementException {
         this.userManager = identityManager.getUserManager();
         this.identitiesRepository = identitiesRepository;
     }
 
     /**
      * Finds all group nodes in the repository and instantiates the groups. 
      * @throws AccessManagementException
      */
     public void loadGroups() throws AccessManagementException {
         log.warn("Load groups from repository '" + identitiesRepository.getName() + "'");
         this.groups = new HashMap();
         try {
             Node groupsParentNode = getGroupsParentNode();
             Node[] groupNodes = groupsParentNode.getNodes();
             DefaultConfigurationBuilder configBuilder = new DefaultConfigurationBuilder(true);
             for (int i = 0; i < groupNodes.length; i++) {
                 if (groupNodes[i].isResource()) {
                     try {
                         Configuration config = configBuilder.build(groupNodes[i].getInputStream());
                         if (config.getName().equals(YarepGroup.GROUP)) {
                             Group group = constructGroup(groupNodes[i]);
                             this.groups.put(group.getID(), group);
                         }
                     } catch (Exception e) {
                         String errorMsg = "Could not create group from repository node: " + groupNodes[i].getPath() + ": " + e;
                         log.error(errorMsg, e);
                         // NOTE[et]: Do not fail here because other groups may still be ok
                         //throw new AccessManagementException(errorMsg, e);
                     }
                 }
             }
         } catch (RepositoryException e) {
             String errorMsg = "Could not read groups from repository: " + e;
             log.error(errorMsg, e);
             throw new AccessManagementException(errorMsg, e);
         }
     }
 
     /**
      * @see org.wyona.security.core.api.GroupManager#createGroup(java.lang.String, java.lang.String)
      */
     public Group createGroup(String id, String name) throws AccessManagementException {
         if (existsGroup(id)) {
             throw new AccessManagementException("Group " + id + " already exists.");
         }
         try {
             Node groupsParentNode = getGroupsParentNode();
             YarepGroup group = new YarepGroup(userManager, this, id, name);
             group.setNode(groupsParentNode.addNode(id + "." + SUFFIX, NodeType.RESOURCE));
             group.save();
             this.groups.put(id, group);
             return group;
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             throw new AccessManagementException(e.getMessage(), e);
         }
     }
     
     /**
      *
      */
     protected Group constructGroup(Node node) throws AccessManagementException{
         return new YarepGroup(userManager, this, node);
     }
 
     /**
      * @see org.wyona.security.core.api.GroupManager#existsGroup(java.lang.String)
      */
     public boolean existsGroup(String id) throws AccessManagementException {
         if (this.groups.containsKey(id)) {
             // Exists within memory cache
             return true;
         } else {
             // Check if group exists within persitent repository
             try {
                 return getGroupsParentNode().hasNode(id + "." + SUFFIX);
             } catch(Exception e) {
                 log.error(e, e);
                 return false;
             }
         }
     }
 
     /**
      * @see org.wyona.security.core.api.GroupManager#getGroup(java.lang.String)
      */
     public Group getGroup(String id) throws AccessManagementException {
         if (!existsGroup(id)) {
             return null;
         } else {
             if (this.groups.containsKey(id)) {
                 return (Group) this.groups.get(id);
             } else {
                 log.warn("Group '" + id + "' exists within persistent repository, but not within memory yet! Will be loaded into memory ...");
                 try {
                    log.error("DEBUG: Add group id '" + id + "' to hash map first in order to break loops!");
                     this.groups.put(id, null);
                     Group group = constructGroup(getGroupsParentNode().getNode(id + "." + SUFFIX));
                     this.groups.put(group.getID(), group);
                     return (Group) this.groups.get(id);
                 } catch (Exception e) {
                     log.error(e, e);
                     throw new AccessManagementException(e.getMessage());
                 }
             }
         }
     }
 
     /**
      * @see org.wyona.security.core.api.GroupManager#getGroups()
      */
     public Group[] getGroups() throws AccessManagementException {
         return (Group[]) this.groups.values().toArray(new Group[this.groups.size()]);
     }
 
     /**
      * @see org.wyona.security.core.api.GroupManager#removeGroup(java.lang.String)
      */
     public void removeGroup(String id) throws AccessManagementException {
         if (!existsGroup(id)) {
             throw new AccessManagementException("Group " + id + " does not exist.");
         }
         Group group = getGroup(id);
         this.groups.remove(id);
         group.delete();
     }
 
     /**
      * Gets the repository node which is the parent node of all group nodes.
      * @return parent node of group nodes
      * @throws NoSuchNodeException
      * @throws RepositoryException
      */
     protected Node getGroupsParentNode() throws NoSuchNodeException, RepositoryException {
         if (this.identitiesRepository.existsNode(GROUPS_PARENT_PATH)) {
             return this.identitiesRepository.getNode(GROUPS_PARENT_PATH);
         }
         // fallback to root node for backwards compatibility:
         return this.identitiesRepository.getNode("/");    
     }
 
 }
