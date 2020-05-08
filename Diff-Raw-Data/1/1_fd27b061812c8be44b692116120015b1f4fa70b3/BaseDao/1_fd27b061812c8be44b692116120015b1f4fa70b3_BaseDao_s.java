 package org.karmaexchange.dao;
 
 import static java.lang.String.format;
 import static org.karmaexchange.util.OfyService.ofy;
 import static org.karmaexchange.util.UserService.isCurrentUserAdmin;
 
 import java.util.Collection;
 import java.util.List;
 
 import javax.annotation.Nullable;
 import javax.xml.bind.annotation.XmlTransient;
 
 import lombok.Data;
 import lombok.EqualsAndHashCode;
 import lombok.NoArgsConstructor;
 import lombok.ToString;
 
 import org.karmaexchange.resources.msg.ErrorResponseMsg;
 import org.karmaexchange.resources.msg.ErrorResponseMsg.ErrorInfo;
 import org.karmaexchange.resources.msg.ValidationErrorInfo.ValidationError;
 import org.karmaexchange.resources.msg.ValidationErrorInfo.ValidationErrorType;
 
 import com.google.common.collect.Lists;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.VoidWork;
 import com.googlecode.objectify.annotation.Ignore;
 import com.googlecode.objectify.annotation.Parent;
 
 @Data
 public abstract class BaseDao<T extends BaseDao<T>> {
   @Parent
   protected Key<?> owner;
   @Ignore
   private String key;
   private ModificationInfo modificationInfo;
 
   @Ignore
   protected Permission permission;
 
   public final void setOwner(String keyStr) {
     owner = (keyStr == null) ? null : Key.<Object>create(keyStr);
   }
 
   public final String getOwner() {
     return (owner == null) ? null : owner.getString();
   }
 
   public static <T extends BaseDao<T>> void upsert(T resource) {
     ofy().transact(new UpsertTxn<T>(resource));
   }
 
   @Data
   @EqualsAndHashCode(callSuper=false)
   public static class UpsertTxn<T extends BaseDao<T>> extends VoidWork {
     private final T resource;
 
     public void vrun() {
       // Cleanup any id and key mismatch.
       resource.syncKeyAndIdForUpsert();
 
       T prevResource = null;
       if (resource.isKeyComplete()) {
         prevResource = load(Key.create(resource));
       }
       if (prevResource == null) {
         resource.insert();
       } else {
         resource.update(prevResource);
       }
     }
   }
 
   protected abstract void syncKeyAndIdForUpsert();
 
   @XmlTransient
   public abstract boolean isKeyComplete();
 
   public static <T extends BaseDao<T>> void partialUpdate(T resource) {
     resource.partialUpdate();
   }
 
   @Nullable
   public static <T extends BaseDao<T>> T load(String keyStr) {
     return load(Key.<T>create(keyStr));
   }
 
   @Nullable
   public static <T extends BaseDao<T>> T load(Key<T> key) {
     T resource = ofy().load().key(key).now();
     if (resource != null) {
       resource.processLoad();
     }
     return resource;
   }
 
   public static <T extends BaseDao<T>> List<T> load(Collection<Key<T>> keys) {
     return load(keys, false);
   }
 
   public static <T extends BaseDao<T>> List<T> load(Collection<Key<T>> keys,
       boolean transactionless) {
     Objectify ofyService = transactionless ? ofy().transactionless() : ofy();
     List<T> resources = Lists.newArrayList(ofyService.load().keys(keys).values());
     for (T resource : resources) {
       resource.processLoad();
     }
     return resources;
   }
 
   public static <T extends BaseDao<T>> List<T> loadAll(Class<T> resourceClass) {
     List<T> resources = ofy().load().type(resourceClass).list();
     for (T resource : resources) {
       resource.processLoad();
     }
     return resources;
   }
 
   public static <T extends BaseDao<T>> void processLoadResults(Collection<T> resources) {
     for (T resource : resources) {
       resource.processLoad();
     }
   }
 
   public static <T extends BaseDao<T>> void delete(Key<T> key) {
     ofy().transact(new DeleteTxn<T>(key));
   }
 
   @Data
   @EqualsAndHashCode(callSuper=false)
   public static class DeleteTxn<T extends BaseDao<T>> extends VoidWork {
     private final Key<T> resourceKey;
 
     public void vrun() {
       T resource = BaseDao.load(resourceKey);
       if (resource != null) {
         resource.delete();
       }
     }
   }
 
   final void insert() {
     preProcessInsert();
     ofy().save().entity(this).now();
     postProcessInsert();
   }
 
   final void update(T prevObj) {
     processUpdate(prevObj);
     ofy().save().entity(this).now();
   }
 
   final void partialUpdate() {
     processPartialUpdate(null);
     ofy().save().entity(this).now();
   }
 
   final void delete() {
     processDelete();
     ofy().delete().key(Key.create(this)).now();
   }
 
   protected void preProcessInsert() {
     setModificationInfo(ModificationInfo.create());
   }
 
   protected void postProcessInsert() {
     updateKey();
   }
 
   protected void processUpdate(T prevObj) {
     updateKey();
     if (!prevObj.getKey().equals(getKey())) {
       throw ErrorResponseMsg.createException(
         format("thew new resource key [%s] does not match the previous key [%s]",
           getKey(), prevObj.getKey()),
         ErrorInfo.Type.BAD_REQUEST);
     }
     processPartialUpdate(prevObj);
   }
 
   final void processPartialUpdate(T prevObj) {
     if (getModificationInfo() == null) {
       // Handle objects that were created without modification info.
       if ((prevObj == null) || (prevObj.getModificationInfo() == null)) {
         setModificationInfo(ModificationInfo.create());
       } else {
         setModificationInfo(prevObj.getModificationInfo());
       }
     }
     getModificationInfo().update();
   }
 
   protected void processDelete() {
   }
 
   protected void processLoad() {
     updateKey();
     updatePermission();
   }
 
   protected void updateKey() {
     setKey(KeyWrapper.create(this).getKey());
   }
 
   protected final void updatePermission() {
     if (isCurrentUserAdmin()) {
       permission = Permission.ALL;
     } else {
       permission = evalPermission();
     }
   }
 
   protected abstract Permission evalPermission();
 
   @Data
   @NoArgsConstructor
   @EqualsAndHashCode(callSuper=true)
   @ToString(callSuper=true)
   public static class ResourceValidationError extends ValidationError {
 
     private String resourceKey;
     private String field;
 
     public ResourceValidationError(BaseDao<?> resource, ValidationErrorType errorType,
         String fieldName) {
       super(errorType);
       if (resource.isKeyComplete()) {
         resourceKey = Key.create(resource).getString();
       }
     }
   }
 
   @Data
   @NoArgsConstructor
   @EqualsAndHashCode(callSuper=true)
   @ToString(callSuper=true)
   public static class MultiFieldResourceValidationError extends ResourceValidationError {
 
     private String otherField;
 
     public MultiFieldResourceValidationError(BaseDao<?> resource, ValidationErrorType errorType,
         String fieldName, String otherFieldName) {
       super(resource, errorType, fieldName);
       this.otherField = otherFieldName;
     }
   }
 
   @Data
   @NoArgsConstructor
   @EqualsAndHashCode(callSuper=true)
   @ToString(callSuper=true)
   public static class LimitResourceValidationError extends ResourceValidationError {
 
     private int limit;
 
     public LimitResourceValidationError(BaseDao<?> resource, ValidationErrorType errorType,
         String fieldName, int limit) {
       super(resource, errorType, fieldName);
       this.limit = limit;
     }
   }
 
 }
