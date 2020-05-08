 package de.deepamehta.core.impl;
 
 import de.deepamehta.core.model.DataField;
 import de.deepamehta.core.model.Topic;
 import de.deepamehta.core.model.TopicType;
 import de.deepamehta.core.model.RelatedTopic;
 import de.deepamehta.core.model.Relation;
 import de.deepamehta.core.service.CoreService;
 import de.deepamehta.core.service.Migration;
 import de.deepamehta.core.service.Plugin;
 import de.deepamehta.core.storage.Storage;
 import de.deepamehta.core.storage.Transaction;
 import de.deepamehta.core.storage.neo4j.Neo4jStorage;
 import de.deepamehta.core.util.JSONHelper;
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.InvocationTargetException;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.logging.Logger;
 
 
 
 /**
  * Implementation of the DeepaMehta core service. Embeddable into Java applications.
  */
 public class EmbeddedService implements CoreService {
 
     // ------------------------------------------------------------------------------------------------------- Constants
 
     private static final String DATABASE_PATH = "deepamehta-db";
     private static final String CORE_MIGRATIONS_PACKAGE = "de.deepamehta.core.migrations";
     private static final int REQUIRED_CORE_MIGRATION = 1;
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     /**
      * Registered plugins.
      * Hashed by plugin bundle's symbolic name, e.g. "de.deepamehta.3-topicmaps".
      */
     private Map<String, Plugin> plugins = new HashMap();
 
     private Storage storage;
 
     private enum Hook {
 
          PRE_CREATE_TOPIC("preCreateHook",  Topic.class, Map.class),
         POST_CREATE_TOPIC("postCreateHook", Topic.class, Map.class),
          PRE_UPDATE_TOPIC("preUpdateHook",  Topic.class, Map.class),
         POST_UPDATE_TOPIC("postUpdateHook", Topic.class, Map.class),
 
          PRE_DELETE_RELATION("preDeleteRelationHook", Long.TYPE),
         POST_DELETE_RELATION("postDeleteRelationHook", Long.TYPE),
 
         PROVIDE_TOPIC_PROPERTIES("providePropertiesHook", Topic.class),
         PROVIDE_RELATION_PROPERTIES("providePropertiesHook", Relation.class),
 
         EXECUTE_COMMAND("executeCommandHook", String.class, Map.class, Map.class);
 
         private final String name;
         private final Class[] paramClasses;
 
         private Hook(String name, Class... paramClasses) {
             this.name = name;
             this.paramClasses = paramClasses;
         }
     }
 
     private enum MigrationRunMode {
         CLEAN_INSTALL, UPDATE, ALWAYS
     }
 
     private Logger logger = Logger.getLogger(getClass().getName());
 
     // ---------------------------------------------------------------------------------------------------- Constructors
 
     public EmbeddedService() {
         try {
             openDB();
         } catch (Throwable e) {
             throw new RuntimeException("Database can't be opened", e);
         }
         //
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             boolean isCleanInstall = initDB();
             runCoreMigrations(isCleanInstall);
             tx.success();   
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Database can't be initialized", e);
         } finally {
             tx.finish();
             if (ex != null) {
                 closeDB();
                 throw ex;
             }
         }
     }
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
 
 
     // **********************************
     // *** CoreService Implementation ***
     // **********************************
 
 
 
     // === Topics ===
 
     @Override
     public Topic getTopic(long id) {
         Topic topic = null;
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             topic = storage.getTopic(id);
             tx.success();   
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Topic " + id + " can't be retrieved", e);
         } finally {
             tx.finish();
             if (ex == null) {
                 return topic;
             } else {
                 throw ex;
             }
         }
     }
 
     @Override
     public Topic getTopic(String key, Object value) {
         Topic topic = null;
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             topic = storage.getTopic(key, value);
             tx.success();   
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Error while retrieving topic by property (\"" + key + "\"=" + value + ")", e);
         } finally {
             tx.finish();
             if (ex == null) {
                 return topic;
             } else {
                 throw ex;
             }
         }
     }
 
     @Override
     public Object getTopicProperty(long topicId, String key) {
         Object value = null;
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             value = storage.getTopicProperty(topicId, key);
             tx.success();   
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Property \"" + key + "\" of topic " + topicId + " can't be retrieved", e);
         } finally {
             tx.finish();
             if (ex == null) {
                 return value;
             } else {
                 throw ex;
             }
         }
     }
 
     @Override
     public List<Topic> getTopics(String typeUri) {
         List<Topic> topics = null;
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             topics = storage.getTopics(typeUri);
             //
             for (Topic topic : topics) {
                 triggerHook(Hook.PROVIDE_TOPIC_PROPERTIES, topic);
             }
             //
             tx.success();
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Topics of type \"" + typeUri + "\" can't be retrieved", e);
         } finally {
             tx.finish();
             if (ex == null) {
                 return topics;
             } else {
                 throw ex;
             }
         }
     }
 
     @Override
     public List<Topic> getTopics(String key, Object value) {
         List<Topic> topics = null;
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             topics = storage.getTopics(key, value);
             tx.success();   
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Error while retrieving topics by property (\"" + key + "\"=" + value + ")", e);
         } finally {
             tx.finish();
             if (ex == null) {
                 return topics;
             } else {
                 throw ex;
             }
         }
     }
 
     @Override
     public List<RelatedTopic> getRelatedTopics(long topicId, List<String> includeTopicTypes,
                                                              List<String> includeRelTypes,
                                                              List<String> excludeRelTypes) {
         // set defaults
         if (includeTopicTypes == null) includeTopicTypes = new ArrayList();
         if (includeRelTypes   == null) includeRelTypes   = new ArrayList();
         if (excludeRelTypes   == null) excludeRelTypes   = new ArrayList();
         // error check
         if (!includeRelTypes.isEmpty() && !excludeRelTypes.isEmpty()) {
             throw new IllegalArgumentException("includeRelTypes and excludeRelTypes can not be used at the same time");
         }
         //
         List<RelatedTopic> relTopics = null;
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             relTopics = storage.getRelatedTopics(topicId, includeTopicTypes, includeRelTypes, excludeRelTypes);
             //
             for (RelatedTopic relTopic : relTopics) {
                 triggerHook(Hook.PROVIDE_TOPIC_PROPERTIES, relTopic.getTopic());
                 triggerHook(Hook.PROVIDE_RELATION_PROPERTIES, relTopic.getRelation());
             }
             //
             tx.success();
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Related topics of topic " + topicId + " can't be retrieved", e);
         } finally {
             tx.finish();
             if (ex == null) {
                 return relTopics;
             } else {
                 throw ex;
             }
         }
     }
 
     @Override
     public List<Topic> searchTopics(String searchTerm, String fieldUri, boolean wholeWord, Map clientContext) {
         List<Topic> searchResult = null;
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             searchResult = storage.searchTopics(searchTerm, fieldUri, wholeWord);
             //
             tx.success();   
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Error while searching topics (searchTerm=" + searchTerm + ", fieldUri=" +
                 fieldUri + ", wholeWord=" + wholeWord + ", clientContext=" + clientContext + ")", e);
         } finally {
             tx.finish();
             if (ex == null) {
                 return searchResult;
             } else {
                 throw ex;
             }
         }
     }
 
     @Override
     public Topic createTopic(String typeUri, Map properties, Map clientContext) {
         Topic topic = null;
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             Topic t = new Topic(-1, typeUri, null, initProperties(properties, typeUri));
             //
             triggerHook(Hook.PRE_CREATE_TOPIC, t, clientContext);
             //
             topic = storage.createTopic(t.typeUri, t.getProperties());
             //
             triggerHook(Hook.POST_CREATE_TOPIC, topic, clientContext);
             //
             tx.success();
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Topic of type \"" + typeUri + "\" can't be created", e);
         } finally {
             tx.finish();
             if (ex == null) {
                 return topic;
             } else {
                 throw ex;
             }
         }
     }
 
     @Override
     public void setTopicProperties(long id, Map properties) {
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             Topic topic = getTopic(id);
            Map oldProperties = topic.getProperties();
             //
             triggerHook(Hook.PRE_UPDATE_TOPIC, topic, properties);
             //
             storage.setTopicProperties(id, properties);
             //
             topic.setProperties(properties);
             triggerHook(Hook.POST_UPDATE_TOPIC, topic, oldProperties);
             //
             tx.success();   
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Properties of topic " + id + " can't be set (" + properties + ")", e);
         } finally {
             tx.finish();
             if (ex != null) {
                 throw ex;
             }
         }
     }
 
     @Override
     public void deleteTopic(long id) {
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             // delete all the topic's relationships
             for (Relation rel : storage.getRelations(id)) {
                 deleteRelation(rel.id);
             }
             //
             storage.deleteTopic(id);
             tx.success();
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Topic " + id + " can't be deleted", e);
         } finally {
             tx.finish();
             if (ex != null) {
                 throw ex;
             }
         }
     }
 
     // === Relations ===
 
     @Override
     public Relation getRelation(long id) {
         Relation relation = null;
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             relation = storage.getRelation(id);
             tx.success();   
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Relation " + id + " can't be retrieved", e);
         } finally {
             tx.finish();
             if (ex == null) {
                 return relation;
             } else {
                 throw ex;
             }
         }
     }
 
     @Override
     public Relation getRelation(long srcTopicId, long dstTopicId, String typeId, boolean isDirected) {
         Relation relation = null;
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             relation = storage.getRelation(srcTopicId, dstTopicId, typeId, isDirected);
             tx.success();
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Error while retrieving relation between topic " + srcTopicId +
                 " and topic " + dstTopicId + " (typeId=" + typeId + ", isDirected=" + isDirected + ")", e);
         } finally {
             tx.finish();
             if (ex == null) {
                 return relation;
             } else {
                 throw ex;
             }
         }
     }
 
     @Override
     public Relation createRelation(String typeId, long srcTopicId, long dstTopicId, Map properties) {
         Relation relation = null;
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             Relation rel = new Relation(-1, typeId, srcTopicId, dstTopicId, properties);
             //
             relation = storage.createRelation(rel.typeId, rel.srcTopicId, rel.dstTopicId, rel.properties);
             //
             tx.success();
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Relation of type \"" + typeId + "\" can't be created", e);
         } finally {
             tx.finish();
             if (ex == null) {
                 return relation;
             } else {
                 throw ex;
             }
         }
     }
 
     @Override
     public void setRelationProperties(long id, Map properties) {
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             storage.setRelationProperties(id, properties);
             tx.success();   
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Properties of relation " + id + " can't be set (" + properties + ")", e);
         } finally {
             tx.finish();
             if (ex != null) {
                 throw ex;
             }
         }
     }
 
     @Override
     public void deleteRelation(long id) {
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             triggerHook(Hook.PRE_DELETE_RELATION, id);
             //
             storage.deleteRelation(id);
             //
             triggerHook(Hook.POST_DELETE_RELATION, id);
             //
             tx.success();
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Relation " + id + " can't be deleted", e);
         } finally {
             tx.finish();
             if (ex != null) {
                 throw ex;
             }
         }
     }
 
     // === Types ===
 
     @Override
     public Set<String> getTopicTypeUris() {
         Set typeUris = null;
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             typeUris = storage.getTopicTypeUris();
             tx.success();
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Topic type URIs can't be retrieved", e);
         } finally {
             tx.finish();
             if (ex == null) {
                 return typeUris;
             } else {
                 throw ex;
             }
         }
     }
 
     @Override
     public TopicType getTopicType(String typeUri) {
         TopicType topicType = null;
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             topicType = storage.getTopicType(typeUri);
             tx.success();
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Topic type \"" + typeUri + "\" can't be retrieved", e);
         } finally {
             tx.finish();
             if (ex == null) {
                 return topicType;
             } else {
                 throw ex;
             }
         }
     }
 
     @Override
     public TopicType createTopicType(Map properties, List dataFields) {
         TopicType topicType = null;
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             TopicType tt = new TopicType(properties, dataFields);
             //
             triggerHook(Hook.PRE_CREATE_TOPIC, tt, null);  // FIXME: clientContext=null
             //
             topicType = storage.createTopicType(properties, dataFields);
             //
             tx.success();
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Topic type \"" +
                 properties.get("de/deepamehta/core/property/TypeURI") + "\" can't be created", e);
         } finally {
             tx.finish();
             if (ex == null) {
                 return topicType;
             } else {
                 throw ex;
             }
         }
     }
 
     @Override
     public void addDataField(String typeUri, DataField dataField) {
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             storage.addDataField(typeUri, dataField);
             tx.success();
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Data field \"" + dataField.getUri() + "\" can't be added to topic type \"" +
                 typeUri + "\"", e);
         } finally {
             tx.finish();
             if (ex != null) {
                 throw ex;
             }
         }
     }
 
     @Override
     public void updateDataField(String typeUri, DataField dataField) {
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             storage.updateDataField(typeUri, dataField);
             tx.success();
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Data field \"" + dataField.getUri() + "\" of topic type \"" +
                 typeUri + "\" can't be updated", e);
         } finally {
             tx.finish();
             if (ex != null) {
                 throw ex;
             }
         }
     }
 
     @Override
     public void removeDataField(String typeUri, String fieldUri) {
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             storage.removeDataField(typeUri, fieldUri);
             tx.success();
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Data field \"" + fieldUri + "\" of topic type \"" +
                 typeUri + "\" can't be removed", e);
         } finally {
             tx.finish();
             if (ex != null) {
                 throw ex;
             }
         }
     }
 
     @Override
     public void setDataFieldOrder(String typeUri, List fieldUris) {
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             storage.setDataFieldOrder(typeUri, fieldUris);
             tx.success();
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Data field order of topic type \"" + typeUri + "\" can't be set", e);
         } finally {
             tx.finish();
             if (ex != null) {
                 throw ex;
             }
         }
     }
 
     // === Commands ===
 
     @Override
     public JSONObject executeCommand(String command, Map params, Map clientContext) {
         JSONObject result = null;
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             Iterator<JSONObject> i = triggerHook(Hook.EXECUTE_COMMAND, command, params, clientContext).iterator();
             if (!i.hasNext()) {
                 throw new RuntimeException("Command is not handled by any plugin");
             }
             result = i.next();
             if (i.hasNext()) {
                 throw new RuntimeException("Ambiguity: more than one plugin returned a result");
             }
             //
             tx.success();
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Command \"" + command + "\" can't be executed " + params, e);
         } finally {
             tx.finish();
             if (ex == null) {
                 return result;
             } else {
                 throw ex;
             }
         }
     }
 
     // === Plugins ===
 
     @Override
     public void registerPlugin(String pluginId, Plugin plugin) {
         plugins.put(pluginId, plugin);
     }
 
     @Override
     public void unregisterPlugin(String pluginId) {
         if (plugins.remove(pluginId) == null) {
             throw new RuntimeException("Plugin " + pluginId + " is not registered");
         }
     }
 
     @Override
     public Set<String> getPluginIds() {
         return plugins.keySet();
     }
 
     @Override
     public Plugin getPlugin(String pluginId) {
         return plugins.get(pluginId);
     }
 
     /**
      * Runs a plugin migration in a transaction.
      */
     @Override
     public void runPluginMigration(Plugin plugin, int migrationNr, boolean isCleanInstall) {
         RuntimeException ex = null;
         Transaction tx = storage.beginTx();
         try {
             runMigration(migrationNr, plugin, isCleanInstall);
             setPluginMigrationNr(plugin, migrationNr);
             //
             tx.success();
         } catch (Throwable e) {
             logger.warning("ROLLBACK!");
             ex = new RuntimeException("Error while running migration " + migrationNr +
                 " of plugin \"" + plugin.getName() + "\"", e);
         } finally {
             tx.finish();
             if (ex != null) throw ex;
         }
     }
 
     // === Misc ===
 
     @Override
     public void shutdown() {
         closeDB();
     }
 
     // ------------------------------------------------------------------------------------------------- Private Methods
 
     // === Topics ===
 
     // FIXME: method to be dropped. Missing properties are regarded as normal state.
     // Otherwise all instances would be required to be updated once a data field has been added to the type definition.
     // Application logic (server-side) and also the client should cope with missing properties.
     private Map initProperties(Map properties, String typeUri) {
         if (properties == null) {
             properties = new HashMap();
         }
         for (DataField dataField : getTopicType(typeUri).getDataFields()) {
             if (!dataField.getDataType().equals("reference") && properties.get(dataField.getUri()) == null) {
                 properties.put(dataField.getUri(), "");
             }
         }
         return properties;
     }
 
     // === Plugins ===
 
     private Set triggerHook(Hook hook, Object... params) throws NoSuchMethodException,
                                                                 IllegalAccessException,
                                                                 InvocationTargetException {
         Set resultSet = new HashSet();
         for (Plugin plugin : plugins.values()) {
             Method hookMethod = plugin.getClass().getMethod(hook.name, hook.paramClasses);
             Object result = hookMethod.invoke(plugin, params);
             if (result != null) {
                 resultSet.add(result);
             }
         }
         return resultSet;
     }
 
     private void setPluginMigrationNr(Plugin plugin, int migrationNr) {
         Map properties = new HashMap();
         properties.put("de/deepamehta/core/property/PluginMigrationNr", migrationNr);
         setTopicProperties(plugin.getPluginTopic().id, properties);
     }
 
     // === DB ===
 
     private void openDB() {
         storage = new Neo4jStorage(DATABASE_PATH);
     }
 
     /**
      * @return  <code>true</code> if this is a clean install, <code>false</code> otherwise.
      */
     private boolean initDB() {
         return storage.init();
     }
 
     private void closeDB() {
         storage.shutdown();
     }
 
     // === Migrations ===
 
     private void runCoreMigrations(boolean isCleanInstall) {
         int migrationNr = storage.getMigrationNr();
         int requiredMigrationNr = REQUIRED_CORE_MIGRATION;
         int migrationsToRun = requiredMigrationNr - migrationNr;
         logger.info("migrationNr=" + migrationNr + ", requiredMigrationNr=" + requiredMigrationNr +
             " -- running " + migrationsToRun + " core migrations");
         for (int i = migrationNr + 1; i <= requiredMigrationNr; i++) {
             runCoreMigration(i, isCleanInstall);
         }
     }
 
     private void runCoreMigration(int migrationNr, boolean isCleanInstall) {
         runMigration(migrationNr, null, isCleanInstall);
         storage.setMigrationNr(migrationNr);
     }
 
     // ---
 
     /**
      * Runs a core migration or a plugin migration.
      *
      * @param   migrationNr     Number of the migration to run.
      * @param   plugin          The plugin that provides the migration to run.
      *                          <code>null</code> for a core migration.
      * @param   isCleanInstall  <code>true</code> if the migration is run as part of a clean install,
      *                          <code>false</code> if the migration is run as part of an update.
      */
     private void runMigration(int migrationNr, Plugin plugin, boolean isCleanInstall) {
         MigrationInfo mi = null;
         try {
             mi = new MigrationInfo(migrationNr, plugin);
             if (!mi.success) {
                 throw mi.exception;
             }
             // error checks
             if (!mi.isDeclarative && !mi.isImperative) {
                 throw new RuntimeException("Neither a types file (" + mi.typesFile +
                     ") nor a migration class (" + mi.migrationClassName + ") is found");
             }
             if (mi.isDeclarative && mi.isImperative) {
                 throw new RuntimeException("Ambiguity: a types file (" + mi.typesFile +
                     ") AND a migration class (" + mi.migrationClassName + ") are found");
             }
             // run migration
             String runInfo = " (runMode=" + mi.runMode + ", isCleanInstall=" + isCleanInstall + ")";
             if (mi.runMode.equals(MigrationRunMode.CLEAN_INSTALL.name()) == isCleanInstall ||
                 mi.runMode.equals(MigrationRunMode.ALWAYS.name())) {
                 logger.info("Running " + mi.migrationInfo + runInfo);
                 if (mi.isDeclarative) {
                     JSONHelper.readTypesFromFile(mi.typesIn, mi.typesFile, this);
                 } else {
                     Migration migration = (Migration) mi.migrationClass.newInstance();
                     logger.info("Running " + mi.migrationType + " migration class " + mi.migrationClassName);
                     migration.setService(this);
                     migration.run();
                 }
                 logger.info(mi.migrationType + " migration complete");
             } else {
                 logger.info("Do NOT run " + mi.migrationInfo + runInfo);
             }
             logger.info("Updating migration number (" + migrationNr + ")");
         } catch (Throwable e) {
             throw new RuntimeException("Error while running " + mi.migrationInfo, e);
         }
     }
 
     // ---
 
     /**
      * Collects the info required to run a migration.
      */
     private class MigrationInfo {
 
         String migrationType;       // "core", "plugin"
         String migrationInfo;       // for logging
         String runMode;             // "CLEAN_INSTALL", "UPDATE", "ALWAYS"
         //
         boolean isDeclarative;
         boolean isImperative;
         //
         String typesFile;           // for declarative migration
         InputStream typesIn;        // for declarative migration
         //
         String migrationClassName;  // for imperative migration
         Class migrationClass;       // for imperative migration
         //
         boolean success;            // error occurred?
         Throwable exception;        // the error
 
         MigrationInfo(int migrationNr, Plugin plugin) {
             try {
                 String configFile = migrationConfigFile(migrationNr);
                 InputStream configIn;
                 typesFile = migrationTypesFile(migrationNr);
                 migrationType = plugin != null ? "plugin" : "core";
                 //
                 if (migrationType.equals("core")) {
                     migrationInfo = "core migration " + migrationNr;
                     logger.info("Preparing " + migrationInfo + " ...");
                     configIn = getClass().getResourceAsStream(configFile);
                     typesIn  = getClass().getResourceAsStream(typesFile);
                     migrationClassName = coreMigrationClassName(migrationNr);
                     migrationClass = loadClass(migrationClassName);
                 } else {
                     migrationInfo = "migration " + migrationNr + " of plugin \"" + plugin.getName() + "\"";
                     logger.info("Preparing " + migrationInfo + " ...");
                     configIn = plugin.getResourceAsStream(configFile);
                     typesIn  = plugin.getResourceAsStream(typesFile);
                     migrationClassName = plugin.getMigrationClassName(migrationNr);
                     if (migrationClassName != null) {
                         migrationClass = plugin.loadClass(migrationClassName);
                     }
                 }
                 //
                 isDeclarative = typesIn != null;
                 isImperative = migrationClass != null;
                 //
                 readMigrationConfigFile(configIn, configFile);
                 //
                 success = true;
             } catch (Throwable e) {
                 exception = e;
             }
         }
 
         // ---
 
         private void readMigrationConfigFile(InputStream in, String configFile) {
             try {
                 Properties migrationConfig = new Properties();
                 if (in != null) {
                     logger.info("Reading migration config file \"" + configFile + "\"");
                     migrationConfig.load(in);
                 } else {
                     logger.info("No migration config file found (tried \"" + configFile + "\")" +
                         " -- using default configuration");
                 }
                 //
                 runMode = migrationConfig.getProperty("migrationRunMode", MigrationRunMode.ALWAYS.name());
                 MigrationRunMode.valueOf(runMode);  // check if value is valid
             } catch (IllegalArgumentException e) {
                 throw new RuntimeException("Error in config file \"" + configFile + "\": \"" + runMode +
                     "\" is an invalid value for \"migrationRunMode\"");
             } catch (IOException e) {
                 throw new RuntimeException("Config file \"" + configFile + "\" can't be read", e);
             }
         }
 
         // ---
 
         private String migrationTypesFile(int migrationNr) {
             return "/migrations/types" + migrationNr + ".json";
         }
 
         private String migrationConfigFile(int migrationNr) {
             return "/migrations/migration" + migrationNr + ".properties";
         }
 
         private String coreMigrationClassName(int migrationNr) {
             return CORE_MIGRATIONS_PACKAGE + ".Migration" + migrationNr;
         }
 
         // --- Generic Utilities ---
 
         /**
          * Uses the core bundle's class loader to load a class by name.
          *
          * @return  the class, or <code>null</code> if the class is not found.
          */
         private Class loadClass(String className) {
             try {
                 return Class.forName(className);
             } catch (ClassNotFoundException e) {
                 return null;
             }
         }
     }
 }
