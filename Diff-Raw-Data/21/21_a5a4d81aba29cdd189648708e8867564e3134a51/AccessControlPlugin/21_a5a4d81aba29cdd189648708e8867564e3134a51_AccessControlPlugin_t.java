 package de.deepamehta.plugins.accesscontrol;
 
 import de.deepamehta.plugins.accesscontrol.model.Permissions;
 
 import de.deepamehta.core.model.DataField;
 import de.deepamehta.core.model.RelatedTopic;
 import de.deepamehta.core.model.Relation;
 import de.deepamehta.core.model.Topic;
 import de.deepamehta.core.model.TopicType;
 import de.deepamehta.core.service.Plugin;
 import de.deepamehta.core.util.JavaUtils;
 import de.deepamehta.core.util.JSONHelper;
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import static java.util.Arrays.asList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 
 
 public class AccessControlPlugin extends Plugin {
 
     private static final String DEFAULT_USER = "admin";
     private static final String DEFAULT_PASSWORD = "";
     private static final String ENCRYPTED_PASSWORD_PREFIX = "-SHA256-";  // don't change this
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     public enum Role {
 
         CREATOR, OWNER, EVERYONE;
         
         private String s() {
             return name().toLowerCase();
         }
 
         private boolean is(String name) {
             return s().equals(name);
         }
     }
 
     public enum Permission {
 
         WRITE, CREATE;
 
         public String s() {
             return this.name().toLowerCase();
         }
     }
 
    private static final Permissions DEFAULT_CREATOR_PERMISSIONS = new Permissions();
     static {
        DEFAULT_CREATOR_PERMISSIONS.add(Permission.WRITE, true);
        DEFAULT_CREATOR_PERMISSIONS.add(Permission.CREATE, true);
     }
 
     private Logger logger = Logger.getLogger(getClass().getName());
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
 
 
     // ************************
     // *** Overriding Hooks ***
     // ************************
 
 
 
     @Override
     public void postInstallPluginHook() {
         createUser(DEFAULT_USER, DEFAULT_PASSWORD);
     }
 
     // Note: we must use the postCreateHook to create the relation because at pre_create the document has no ID yet.
     @Override
     public void postCreateHook(Topic topic, Map<String, String> clientContext) {
         /* check precondition 4
         if (topic.id == user.id) {
             logger.warning(topic + " can't be related to user \"" + username + "\" (the topic is the user itself!)");
             return;
         }*/
         //
         setCreator(topic, clientContext);
        createACLEntry(topic.id, Role.CREATOR, DEFAULT_CREATOR_PERMISSIONS);
     }
 
     @Override
     public void preUpdateHook(Topic topic, Map<String, Object> newProperties) {
         // encrypt password of new users
         if (topic.typeUri.equals("de/deepamehta/core/topictype/user")) {
             // we recognize a new user (or changed password) if password doesn't begin with ENCRYPTED_PASSWORD_PREFIX
             String password = (String) newProperties.get("de/deepamehta/core/property/password");
             if (!password.startsWith(ENCRYPTED_PASSWORD_PREFIX)) {
                 newProperties.put("de/deepamehta/core/property/password", encryptPassword(password));
             }
         }
     }
 
     @Override
     public void modifyTopicTypeHook(TopicType topicType, Map<String, String> clientContext) {
         addCreatorFieldToType(topicType);
         addOwnerFieldToType(topicType);
         //
         setCreator(topicType, clientContext);
        createACLEntry(topicType.id, Role.CREATOR, DEFAULT_CREATOR_PERMISSIONS);
     }
 
     // ---
 
     @Override
     public void providePropertiesHook(Topic topic) {
         if (topic.typeUri.equals("de/deepamehta/core/topictype/role")) {
             String roleName = (String) dms.getTopicProperty(topic.id, "de/deepamehta/core/property/rolename");
             topic.setProperty("de/deepamehta/core/property/rolename", roleName);
         }
     }
 
     @Override
     public void providePropertiesHook(Relation relation) {
         if (relation.typeId.equals("ACCESS_CONTROL")) {
             // transfer all relation properties
             Map properties = dms.getRelation(relation.id).getProperties();
             relation.setProperties(properties);
         }
     }
 
     // ---
 
     @Override
     public void enrichTopicHook(Topic topic, Map<String, String> clientContext) {
         Map permissions = new HashMap();
         permissions.put("write", hasPermission(topic, getUser(clientContext), Permission.WRITE));
         topic.setEnrichment("permissions", permissions);
     }
 
     @Override
     public void enrichTopicTypeHook(TopicType topicType, Map<String, String> clientContext) {
         Topic user = getUser(clientContext);
         Map permissions = new HashMap();
         permissions.put("write",  hasPermission(topicType, user, Permission.WRITE));
         permissions.put("create", hasPermission(topicType, user, Permission.CREATE));
         topicType.setEnrichment("permissions", permissions);
     }
 
 
 
     // ******************
     // *** Public API ***
     // ******************
 
 
 
     public void setOwner(long topicId, long userId) {
         dms.createRelation("OWNER", topicId, userId, null);
     }
 
     public void createACLEntry(long topicId, Role role, Permissions permissions) {
         dms.createRelation("ACCESS_CONTROL", topicId, getRoleTopic(role).id, permissions);
     }
 
 
 
     // ------------------------------------------------------------------------------------------------- Private Methods
 
     private Topic createUser(String username, String password) {
         Map properties = new HashMap();
         properties.put("de/deepamehta/core/property/username", username);
         properties.put("de/deepamehta/core/property/password", encryptPassword(password));
         return dms.createTopic("de/deepamehta/core/topictype/user", properties, null);     // clientContext=null
     }
 
     // ---
 
     /**
      * Returns the user that is represented by the client context, or <code>null</code> if no user is logged in.
      */
     private Topic getUser(Map<String, String> clientContext) {
         if (clientContext == null) {    // some callers to dms.getTopic() doesn't pass a client context
             return null;
         }
         String username = clientContext.get("dm3_username");
         if (username == null) {
             return null;
         }
         return getUser(username);
     }
 
     /**
      * Returns the user (topic) by username, or <code>null</code> if no such user exists.
      */
     private Topic getUser(String username) {
         return dms.getTopic("de/deepamehta/core/property/username", username);
     }
 
     private Topic getAdminUser() {
         Topic user = getUser(DEFAULT_USER);
         if (user == null) {
             throw new RuntimeException("The \"" + DEFAULT_USER + "\" user doesn't exist");
         }
         return user;
     }
 
     // ---
 
     private String encryptPassword(String password) {
         return ENCRYPTED_PASSWORD_PREFIX + JavaUtils.encodeSHA256(password);
     }
 
     // ---
 
     private void addCreatorFieldToType(TopicType topicType) {
         DataField creatorField = new DataField("Creator", "reference");
         creatorField.setUri("de/deepamehta/core/property/creator");
        creatorField.setRefTopicTypeUri("de/deepamehta/core/topictype/user");
        creatorField.setRefRelationTypeId("CREATOR");
         creatorField.setEditor("checkboxes");
         //
         topicType.addDataField(creatorField);
     }
 
     private void addOwnerFieldToType(TopicType topicType) {
         DataField ownerField = new DataField("Owner", "reference");
         ownerField.setUri("de/deepamehta/core/property/owner");
        ownerField.setRefTopicTypeUri("de/deepamehta/core/topictype/user");
        ownerField.setRefRelationTypeId("OWNER");
         ownerField.setEditor("checkboxes");
         //
         topicType.addDataField(ownerField);
     }
 
     // ---
 
     private void setCreator(Topic topic, Map<String, String> clientContext) {
         Topic user = getUser(clientContext);
         if (user == null) {
             logger.warning("### There is no current user. The admin user is set as the creator of " + topic);
             user = getAdminUser();
         }
         setCreator(topic.id, user.id);
     }
 
     private void setCreator(long topicId, long userId) {
         dms.createRelation("CREATOR", topicId, userId, null);
     }
 
     // === ACL Entries ===
 
     private Topic getRoleTopic(Role role) {
         Topic roleTopic = dms.getTopic("de/deepamehta/core/property/rolename", role.s());
         if (roleTopic == null) {
             throw new RuntimeException("Role topic \"" + role.s() + "\" doesn't exist");
         }
         return roleTopic;
     }
 
     // ---
 
     private boolean hasPermission(Topic topic, Topic user, Permission permission) {
         String roleName = null;
         try {
             logger.fine("Determine permission of user " + user + " to " + permission + " " + topic);
             for (RelatedTopic relTopic : getACLEntries(topic.id)) {
                 roleName = (String) relTopic.getTopic().getProperty("de/deepamehta/core/property/rolename");
                 Role role = Role.valueOf(roleName.toUpperCase());   // throws IllegalArgumentException
                 logger.fine("There is an ACL entry for role " + role);
                 if (role.equals(Role.EVERYONE)) {
                     if (checkEveryone(relTopic, permission)) {
                         return true;
                     }
                 } else if (role.equals(Role.OWNER)) {
                     if (checkOwner(topic, user, relTopic, permission)) {
                         return true;
                     }
                 } else if (role.equals(Role.CREATOR)) {
                     if (checkCreator(topic, user, relTopic, permission)) {
                         return true;
                     }
                 } else {
                     throw new RuntimeException("Role \"" + role + "\" not yet handled");
                 }
             }
             logger.fine("=> DENIED");
             return false;
         } catch (IllegalArgumentException e) {
             throw new RuntimeException("Unexpected role \"" + roleName + "\" in ACL entry for " + topic, e);
         }
     }
 
     private boolean checkEveryone(RelatedTopic relTopic, Permission permission) {
         boolean perm = (Boolean) relTopic.getRelation().getProperty(permission.s());
         logger.fine("value=" + perm);
         if (perm) {
             // everyone has permission
             logger.fine("=> ALLOWED");
             return true;
         }
         return false;
     }
 
     private boolean checkOwner(Topic topic, Topic user, RelatedTopic relTopic, Permission permission) {
         boolean perm = (Boolean) relTopic.getRelation().getProperty(permission.s());
         logger.fine("value=" + perm);
         if (perm) {
             // the owner has permission -- check if the user is the owner
             Topic owner = getOwner(topic.id);
             logger.fine("The owner is " + owner);
             if (user != null && owner != null && user.id == owner.id) {
                 logger.fine("=> ALLOWED");
                 return true;
             }
         }
         return false;
     }
 
     private boolean checkCreator(Topic topic, Topic user, RelatedTopic relTopic, Permission permission) {
         boolean perm = (Boolean) relTopic.getRelation().getProperty(permission.s());
         logger.fine("value=" + perm);
         if (perm) {
             // the creator has permission -- check if the user is the creator
             Topic creator = getCreator(topic.id);
             logger.fine("The creator is " + creator);
             if (user != null && creator != null && user.id == creator.id) {
                 logger.fine("=> ALLOWED");
                 return true;
             }
         }
         return false;
     }
 
     // ---
 
     private List<RelatedTopic> getACLEntries(long topicId) {
         return getService().getRelatedTopics(topicId,
             asList("de/deepamehta/core/topictype/role"),
             asList("ACCESS_CONTROL;INCOMING"), null);
     }
 
     // ---
 
     /**
      * Returns the topic's creator (a user topic), or <code>null</code> if no creator is set.
      */
     private Topic getCreator(long topicId) {
         List<RelatedTopic> users = dms.getRelatedTopics(topicId, asList("de/deepamehta/core/topictype/user"),
             asList("CREATOR;INCOMING"), null);
         //
         if (users.size() == 0) {
             return null;
         } else if (users.size() > 1) {
             throw new RuntimeException("Ambiguity: topic " + topicId + " has " + users.size() + " creators");
         }
         //
         return users.get(0).getTopic();
     }
 
     /**
      * Returns the topic's owner (a user topic), or <code>null</code> if no owner is set.
      */
     private Topic getOwner(long topicId) {
         List<RelatedTopic> users = dms.getRelatedTopics(topicId, asList("de/deepamehta/core/topictype/user"),
             asList("OWNER;INCOMING"), null);
         //
         if (users.size() == 0) {
             return null;
         } else if (users.size() > 1) {
             throw new RuntimeException("Ambiguity: topic " + topicId + " has " + users.size() + " owners");
         }
         //
         return users.get(0).getTopic();
     }
 }
