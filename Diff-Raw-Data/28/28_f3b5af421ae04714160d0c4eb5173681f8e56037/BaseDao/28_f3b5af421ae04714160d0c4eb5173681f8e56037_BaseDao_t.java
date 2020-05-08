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
 import org.karmaexchange.util.OfyUtil;
 
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
     return load(OfyUtil.<T>createKey(keyStr));
   }
 
   @Nullable
   public static <T extends BaseDao<T>> T load(Key<T> key) {
     return load(key, ofy());
   }
 
   @Nullable
   public static <T extends BaseDao<T>> T load(Key<T> key, Objectify ofyService) {
     T resource = ofyService.load().key(key).now();
     if (resource != null) {
       resource.processLoad();
     }
     return resource;
   }
 
   public static <T extends BaseDao<T>> List<T> load(Collection<Key<T>> keys) {
     return load(keys, ofy());
   }
 
   public static <T extends BaseDao<T>> List<T> load(Collection<Key<T>> keys,
       Objectify ofyService) {
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
     validateMutationPermission();
     ofy().save().entity(this).now();
     postProcessInsert();
   }
 
   final void update(T prevObj) {
     processUpdate(prevObj);
     validateMutationPermission();
     ofy().save().entity(this).now();
   }
 
   private void validateMutationPermission() {
     // TODO(avaliani): handle incomplete key. "Key.create(this)" does not work for incomplete
     //    keys.
 //    updatePermission();
 //    if (!permission.canEdit()) {
 //      // Too important for testing to throw an error right now.
 //      logger.warning(format("invalid permissions for user[%s] to mutate resource[%s]",
 //        getCurrentUserKey().getString(), Key.create(this).toString()));
 //      //      throw ValidationErrorInfo.createException(asList(new ResourceValidationError(this,
 //      //          ValidationErrorType.RESOURCE_MUTATION_PERMISSION_REQUIRED, null)));
 //    }
   }
 
   final void partialUpdate() {
     processPartialUpdate(null);
     // Partial updates don't require validating mutation permissions since they manipulate
     // internally managed fields.
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
     @Nullable
     private String field;
 
     public ResourceValidationError(BaseDao<?> resource, ValidationErrorType errorType,
         @Nullable String fieldName) {
       super(errorType);
       if (resource.isKeyComplete()) {
         resourceKey = Key.create(resource).getString();
       }
       this.field = fieldName;
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
 
   @Data
   @NoArgsConstructor
   @EqualsAndHashCode(callSuper=true)
   @ToString(callSuper=true)
   public static class ListValueValidationError extends ResourceValidationError {
 
     private String memberValue;
 
     public ListValueValidationError(BaseDao<?> resource, ValidationErrorType errorType,
         String fieldName, String memberValue) {
       super(resource, errorType, fieldName);
       this.memberValue = memberValue;
     }
   }
 }
