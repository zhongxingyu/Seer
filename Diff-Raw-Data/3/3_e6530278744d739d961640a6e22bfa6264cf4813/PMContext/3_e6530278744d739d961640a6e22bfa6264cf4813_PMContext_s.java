 package jpaoletti.jpm.core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import jpaoletti.jpm.core.exception.ConnectionNotFoundException;
 import jpaoletti.jpm.core.message.Message;
 import jpaoletti.jpm.security.core.PMSecurityUser;
 
 /**
  * A context for object container
  */
 public class PMContext {
 
     private Map<String, Object> contents = new HashMap<String, Object>();
     private String sessionId;
     private PersistenceManager persistenceManager;
     private EntityContainer entityContainer;
     private Operation operation;
     private Object entityInstance;
     private EntityInstanceWrapper entityInstanceWrapper;
     private Field field;
     private Object fieldValue;
     private List<Message> messages;
 
     public PMContext(String sessionId) {
         this.sessionId = sessionId;
     }
 
     public PMContext() {
         this("");
     }
 
     /**
      * Shortcut for getEntity().getDataAccess()
      */
     public DataAccess getDataAccess() throws PMException {
         return getEntity().getDataAccess();
     }
 
     /**
      * Getter for PM singleton
      * @return
      */
     public PresentationManager getPresentationManager() {
         return PresentationManager.getPm();
     }
 
     /**
      * Return the persistence manager
      * @return PersistenceManager
      */
     public PersistenceManager getPersistenceManager() {
         if (persistenceManager == null) {
             try {
                 persistenceManager = getPresentationManager().newPersistenceManager();
             } catch (Exception ex) {
                 getPresentationManager().error(ex);
                 throw new ConnectionNotFoundException();
             }
         }
         return persistenceManager;
     }
 
     /**
      * @param entityContainer the entity_container to set
      */
     public void setEntityContainer(EntityContainer entityContainer) {
         this.entityContainer = entityContainer;
     }
 
     /**
      * @return the entity_container
      * @throws PMException
      */
     public EntityContainer getEntityContainer() throws PMException {
         if (entityContainer == null) {
            PresentationManager.getPm().error("Entity container not found");
             throw new PMException("pm_core.entity.not.found");
         }
         return entityContainer;
     }
 
     /**
      * Retrieve the container with the given id from session. If not defined, a
      * new one is created.
      *
      * @param id The entity id
      * @return The container
      */
     public EntityContainer getEntityContainer(String id) {
         EntityContainer ec = (EntityContainer) getPmsession().getContainer(id);
         if (ec == null) {
             ec = getPresentationManager().newEntityContainer(id);
             getPmsession().setContainer(id, ec);
         }
         return ec;
     }
 
     /**
      * Returns the entity container 
      * @param ignorenull If true, does not throws an exception on missing container
      * @return The container
      * @throws PMException
      */
     public EntityContainer getEntityContainer(boolean ignorenull) throws PMException {
         if (ignorenull) {
             return entityContainer;
         }
         if (entityContainer == null) {
             throw new PMException("pm_core.entity.not.found");
         }
         return entityContainer;
     }
 
     /**
      * Informs if there is a container in the context
      *
      * @return true if there is a container in the context
      */
     public boolean hasEntityContainer() {
         return entityContainer != null;
     }
 
     /**
      * @param operation the operation to set
      */
     public void setOperation(Operation operation) {
         this.operation = operation;
     }
 
     /**
      * @return the operation
      */
     public Operation getOperation() {
         return operation;
     }
 
     /**
      * @return the operation
      */
     public Operations getOperations(Object item, Operation oper) throws PMException {
         if (getEntity() != null) {
             return getEntity().getOperations().getOperationsFor(this, item, oper);
         } else {
             return new Operations();
         }
     }
 
     /**
      * Return the entity in the container
      * 
      * @return The entity
      */
     public Entity getEntity() {
         try {
             return getEntityContainer().getEntity();
         } catch (PMException ex) {
            getPresentationManager().warn("Entity not found");
             return null;
         }
     }
 
     /**
      * @return true when the context contains an entity. False otherwise
      */
     public boolean getEntityExist() {
         try {
             return getEntityContainer(true) != null && getEntityContainer().getEntity() != null;
         } catch (PMException ex) {
             return false;
         }
     }
 
     /**
      * Return the list of the container
      * @return The list
      * @throws PMException
      */
     public PaginatedList getList() throws PMException {
         return getEntityContainer().getList();
     }
 
     /**
      * Return the selected item of the container
      * @return The EntityInstanceWrapper
      * @throws PMException
      */
     public EntityInstanceWrapper getSelected() throws PMException {
         final EntityContainer container = getEntityContainer(true);
         if (container == null) {
             return null;
         }
         return container.getSelected();
     }
 
     /**
      * Indicate if there is a container with an entity
      * 
      * @return
      */
     public boolean hasEntity() {
         try {
             return (hasEntityContainer() && getEntityContainer().getEntity() != null);
         } catch (PMException e) {
             return false;
         }
     }
 
     public PMSession getPmsession() {
         return getPresentationManager().getSession(getSessionId());
     }
 
     public String getSessionId() {
         return sessionId;
     }
 
     /**Getter for the logged user
      * @return The user
      */
     public PMSecurityUser getUser() {
         if (getPmsession() == null) {
             return null;
         }
         return getPmsession().getUser();
     }
 
     /**Indicates if there is a user online
      * @return True if there is a user online
      */
     public boolean isUserOnLine() {
         return (getUser() != null);
     }
 
     public Object getParameter(String paramid) {
         final Object v = get("param_" + paramid);
         if (v == null) {
             return null;
         } else {
             if (v instanceof String[]) {
                 String[] s = (String[]) v;
                 if (s.length == 1) {
                     return s[0];
                 } else {
                     return s;
                 }
             }
             return v;
         }
     }
 
     public void setSessionId(String sessionId) {
         this.sessionId = sessionId;
     }
 
     public Object[] getParameters(String paramid) {
         return (Object[]) get("param_" + paramid);
     }
 
     /**
      * Getter for a boolean value.
      * @param key The key
      * @param def Default value if there is no item at key
      * @return A boolean
      */
     public boolean getBoolean(String key, boolean def) {
         try {
             if (!contains(key)) {
                 return def;
             }
             return (Boolean) get(key);
         } catch (Exception e) {
             return def;
         }
     }
 
     /**
      * Obtains a pair based on the given key. In there is no key, this method
      * returns null
      *
      * @param key The key
      * @return the ContextPair for the given key.
      */
     public ContextPair getPair(String key) {
         if (!this.contains(key)) {
             return null;
         }
         return new ContextPair(key, get(key));
     }
 
     /**
      * Puts the key/value pair from each pair into the context
      * @param pair The pair
      */
     public void put(ContextPair... pairs) {
         if (pairs != null) {
             for (int i = 0; i < pairs.length; i++) {
                 ContextPair pair = pairs[i];
                 this.put(pair.getKey(), pair.getValue());
             }
         }
     }
 
     /**
      * Indicate if the key is present.
      *
      * @param key The key
      * @return true if value asociated to the key is not null
      */
     public boolean contains(final String key) {
         return get(key) != null;
     }
 
     /**
      * Build a new pair
      */
     public ContextPair newPair(final String key, final Object value) {
         return new ContextPair(key, value);
     }
 
     public Object get(String string) {
         return contents.get(string);
     }
 
     public void put(String key, Object value) {
         contents.put(key, value);
     }
 
     public String getString(String string) {
         final Object o = get(string);
         if (o == null) {
             return null;
         }
         return o.toString();
     }
 
     /**
      * Helper class to simplify context pairs to be moved from one context to
      * another.
      *
      */
     public static class ContextPair {
 
         private String key;
         private Object value;
 
         public ContextPair(String key, Object value) {
             this.key = key;
             this.value = value;
         }
 
         public String getKey() {
             return key;
         }
 
         public void setKey(String key) {
             this.key = key;
         }
 
         public Object getValue() {
             return value;
         }
 
         public void setValue(Object value) {
             this.value = value;
         }
     }
 
     public Object getEntityInstance() {
         return entityInstance;
     }
 
     public void setEntityInstance(Object entityInstance) {
         this.entityInstance = entityInstance;
     }
 
     public EntityInstanceWrapper getEntityInstanceWrapper() {
         return entityInstanceWrapper;
     }
 
     public void setEntityInstanceWrapper(EntityInstanceWrapper entityInstanceWrapper) {
         this.entityInstanceWrapper = entityInstanceWrapper;
     }
 
     public Field getField() {
         return field;
     }
 
     public void setField(Field field) {
         this.field = field;
     }
 
     public Object getFieldValue() {
         return fieldValue;
     }
 
     public void setFieldValue(Object fieldValue) {
         this.fieldValue = fieldValue;
     }
 
     public List<Message> getMessages() {
         if (messages == null) {
             messages = new ArrayList<Message>();
         }
         return messages;
     }
 
     public PMContext addMessage(Message message) {
         getMessages().add(message);
         return this;
     }
 
     public boolean hasErrors() {
         for (Message message : getMessages()) {
             if (message.isError()) {
                 return true;
             }
         }
         return false;
     }
 
     public Map<String, Object> getMap() {
         return contents;
     }
 }
