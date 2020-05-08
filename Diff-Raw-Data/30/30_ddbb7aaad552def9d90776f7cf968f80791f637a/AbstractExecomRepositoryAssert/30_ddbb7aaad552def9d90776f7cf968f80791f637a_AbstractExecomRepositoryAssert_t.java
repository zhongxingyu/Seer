 package eu.execom.testutil;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import junit.framework.Assert;
 
 import org.apache.commons.lang3.StringUtils;
 
 import eu.execom.testutil.property.IProperty;
 import eu.execom.testutil.report.AssertReportBuilder;
 import eu.execom.testutil.util.ReflectionUtil;
 
 /**
  * ExeCom test util class. Extends {@link AbstractExecomAssert} with possibility to assert entire repository (DB ...) .
  * TODO think of better comment.
  * 
  * @param <EntityType>
  *            Entity type that all tested entities must implement.
  * @param <EntityId>
  *            type of entity id
  * @author Dusko Vesin
  * @author Nikola Olah
  * @author Bojan Babic
  * @author Nikola Trkulja
  */
 @SuppressWarnings({"unchecked"})
 public abstract class AbstractExecomRepositoryAssert<EntityType, EntityId> extends AbstractExecomAssert<EntityType>
         implements ExecomRepositoryAssert<EntityType, EntityId> {
 
     /** The Constant SET_METHOD_PREFIX. */
     protected static final String SET_METHOD_PREFIX = "set";
 
     /** The db snapshot. */
     private final Map<Class<?>, Map<EntityId, CopyAssert<EntityType>>> dbSnapshot;
 
     /**
      * Default constructor.
      */
     public AbstractExecomRepositoryAssert() {
         super();
         dbSnapshot = new HashMap<Class<?>, Map<EntityId, CopyAssert<EntityType>>>();
         initDbSnapshot();
     }
 
     @Override
    public void assertEntityAsDeleted(final EntityType actual) {
 
         ignoreEntity(actual);
 
         final EntityType findById = findById(actual.getClass(), (EntityId) getIdValue(actual));
         Assert.assertNull(findById);
     }
 
     @Override
     public void ignoreEntity(final EntityType actual) {
 
         if (ReflectionUtil.isEntityType(actual.getClass(), entityTypes)) {
             markAsserted(actual);
         }
 
     }
 
     @Override
     protected final <X> void afterAssertEntity(final X object, final boolean isProperty) {
         if (ReflectionUtil.isEntityType(object.getClass(), entityTypes) && !isProperty) {
             markAsserted((EntityType) object);
         }
     };
 
     @Override
     public Map<Class<?>, Map<EntityId, CopyAssert<EntityType>>> getDbSnapshot() {
         return dbSnapshot;
     }
 
     @Override
     public <X extends EntityType> void assertEntityWithSnapshot(final X actual, final IProperty... properties) {
         assertEntityWithSnapshot(EMPTY_STRING, actual, properties);
     };
 
     @Override
     public <X extends EntityType> void assertEntityWithSnapshot(final String message, final X actual,
             final IProperty... properties) {
 
         final EntityId id = getIdValue(actual);
         Assert.assertNotNull("Entity id can't be null " + actual, id);
 
         final Map<EntityId, CopyAssert<EntityType>> map = dbSnapshot.get(actual.getClass());
         final boolean isTypeSupported = map != null;
 
         if (isTypeSupported) {
             final CopyAssert<EntityType> copyAssert = map.get(id);
             if (copyAssert != null) {
                 final EntityType expected = copyAssert.getEntity();
                 assertObjects(message, expected, actual, properties);
             } else {
                 Assert.fail("Entity doesn't exist in snapshot  " + actual);
             }
         } else {
             Assert.fail("Type of entity is not supported  " + actual.getClass());
         }
 
     };
 
     /**
      * Initialize database snapshot.
      */
     private void initDbSnapshot() {
        dbSnapshot.clear();
         for (final Class<?> entityType : entityTypes) {
             getDbSnapshot().put(entityType, new HashMap<EntityId, CopyAssert<EntityType>>());
         }
     }
 
     /**
      * Mark entity bean as asserted.
      * 
      * @param entity
      *            AbstractEntity
      */
     protected void markAsserted(final EntityType entity) {
         final boolean markAsserted = markAsAsserted(entity, entity.getClass());
         Assert.assertTrue("Type " + entity.getClass() + " is not currently supported.", markAsserted);
     }
 
     /**
      * Mark entity bean as asserted in db snapshot map. Go trough all its supper classes and if its possible assert it.
      * 
      * @param entity
      *            the entity
      * @param actualType
      *            the actual type
      * @return <code>true</code> if entity is successfully asserted else return <code>false</code>.
      */
     protected boolean markAsAsserted(final EntityType entity, final Class<?> actualType) {
 
         final EntityId id = getIdValue(entity);
         Assert.assertNotNull("Entity id can't be null " + entity, id);
 
         final Map<EntityId, CopyAssert<EntityType>> map = dbSnapshot.get(actualType);
         final boolean isTypeSupported = map != null;
 
         if (isTypeSupported) {
             CopyAssert<EntityType> copyAssert = map.get(id);
             if (copyAssert == null) {
                 copyAssert = new CopyAssert<EntityType>(createCopy(entity));
                 map.put((EntityId) getIdValue(entity), copyAssert);
             }
             copyAssert.setAsserted(true);
         }
 
         final Class<?> superClassType = actualType.getSuperclass();
         final boolean isSuperClassTypeSupported = (superClassType != null) && markAsAsserted(entity, superClassType);
         return isTypeSupported || isSuperClassTypeSupported;
     }
 
     /**
      * Takes current database snapshot and saves it.
      */
     protected void takeSnapshot() {
        initDbSnapshot();

         for (final Entry<Class<?>, Map<EntityId, CopyAssert<EntityType>>> enties : dbSnapshot.entrySet()) {
             final List<EntityType> findAll = (List<EntityType>) findAll(enties.getKey());
             for (final EntityType abstractEntity : findAll) {
                 enties.getValue().put((EntityId) getIdValue(abstractEntity),
                         new CopyAssert<EntityType>(createCopy(abstractEntity)));
             }
         }
     };
 
     /**
      * Asserts current database snapshot with one previously taken.
      */
     protected void assertDbState() {
         boolean ok = true;
         final AssertReportBuilder report = new AssertReportBuilder();
         for (final Entry<Class<?>, Map<EntityId, CopyAssert<EntityType>>> entry : dbSnapshot.entrySet()) {
             final List<EntityType> afterEnitities = (List<EntityType>) findAll(entry.getKey());
             ok &= assertByEntity(entry.getValue(), afterEnitities, report);
         }
         if (!ok) {
             throw new AssertionError(report.getMessage());
         }
     }
 
     /**
      * Asserts all entities of same type in current snapshot with ones from before snapshot.
      * 
      * @param <X>
      *            type of the entities
      * @param beforeEntities
      *            entities from before snapshot
      * @param afterEntities
      *            entities from current snapshot
      * @param report
      *            report builder
      * @return <code>true</code> if all entities from before snapshot are asserted with entities from current snapshot,
      *         <code>false</code> otherwise
      */
     protected <X extends EntityType> boolean assertByEntity(final Map<EntityId, CopyAssert<X>> beforeEntities,
             final List<X> afterEntities, final AssertReportBuilder report) {
 
         boolean ok = true;
         final List<EntityId> ids = new ArrayList<EntityId>();
 
         for (final Entry<EntityId, CopyAssert<X>> beforeEntry : beforeEntities.entrySet()) {
             ok &= validateEntry(ids, beforeEntry, afterEntities, report);
         }
 
         ok &= removeAfterEntitites(ids, afterEntities);
         report.reportEntityIsntAsserted(afterEntities);
         return ok;
     }
 
     /**
      * Validates entity by checking if entity is already asserted if not, then checks if entity from before snapshot has
      * its match in current snapshot.
      * 
      * @param <X>
      *            type of the entities
      * @param ids
      *            asserted entity ids
      * @param beforeEntry
      *            entity from before snapshot
      * @param afterEntities
      *            entities from current snapshot
      * @param report
      *            report builder
      * @return <code>true</code> if entity from before snapshot is already asserted or if it has match in current
      *         snapshot and is asserted with it, <code>false</code> otherwise
      */
     protected <X extends EntityType> boolean validateEntry(final List<EntityId> ids,
             final Entry<EntityId, CopyAssert<X>> beforeEntry, final List<X> afterEntities,
             final AssertReportBuilder report) {
 
         final X afterEntity = popEntityFromList(beforeEntry.getKey(), afterEntities);
         final CopyAssert<X> beforeAssertEntity = beforeEntry.getValue();
 
         if (beforeAssertEntity.isAsserted()) {
             return true;
         }
 
         if (afterEntity == null) {
             report.reportNoEntityFailure(beforeAssertEntity.getEntity().toString());
             return false;
 
         }
 
         ids.add(beforeEntry.getKey());
         return assertEntities(beforeAssertEntity.getEntity(), afterEntity, report);
     }
 
     /**
      * Calls assertObjects of {@link AbstractExecomAssert} to assert two entities.
      * 
      * @param <X>
      *            the generic type
      * @param beforeEntity
      *            entity from before snapshot
      * @param afterEntity
      *            entity from current snapshot
      * @param report
      *            report builder
      * @return <code>true</code> if entities are asserted, i.e. assertObjects doesn't throw {@link AssertionError},
      *         <code>false</code> otherwise
      */
     protected <X extends EntityType> boolean assertEntities(final X beforeEntity, final X afterEntity,
             final AssertReportBuilder report) {
         try {
             assertObjects(beforeEntity, afterEntity);
             return true;
         } catch (final AssertionError e) {
             report.append(e.getMessage());
             return false;
         }
     }
 
     /**
      * Pops entity from list of entities and returns it.
      * 
      * @param <X>
      *            entity type
      * @param id
      *            id of the desired entity
      * @param afterEntities
      *            list of entities
      * @return entity with specified id
      */
     protected <X extends EntityType> X popEntityFromList(final EntityId id, final List<X> afterEntities) {
         final Iterator<X> iterator = afterEntities.iterator();
         while (iterator.hasNext()) {
             final X entity = iterator.next();
             if (getIdValue(entity).equals(id)) {
                 iterator.remove();
                 return entity;
             }
         }
         return null;
     }
 
     /**
      * Removes all entities from list of entities from current snapshot with matching id in list of ids.
      * 
      * @param <X>
      *            type of entity
      * @param ids
      *            list of ids
      * @param afterEntities
      *            entities from current snapshot
      * @return <code>true</code> if all entities from current snapshot have matching id in list of ids,
      *         <code>false</code> otherwise
      */
     protected <X extends EntityType> boolean removeAfterEntitites(final List<EntityId> ids, final List<X> afterEntities) {
         final Iterator<X> iterator = afterEntities.iterator();
         while (iterator.hasNext()) {
             if (ids.contains(getIdValue(iterator.next()))) {
                 iterator.remove();
             }
         }
         return afterEntities.size() > 0 ? false : true;
     }
 
     /**
      * Find all entities of type entity class in DB.
      * 
      * @param entityClass
      *            the entity class
      * @return the list
      */
     protected abstract List<?> findAll(Class<?> entityClass);
 
     /**
      * Find specific entity of type entity class and with specific id in DB.
      * 
      * @param entityClass
      *            the entity class
      * @param id
      *            the id
      * @return the entity type
      */
     protected abstract EntityType findById(Class<?> entityClass, EntityId id);
 
     /**
      * Create copy of specified object and return its copy.
      * 
      * @param <T>
      *            type of the object
      * @param object
      *            object for copying
      * @return copied object
      */
     protected <T> T createCopy(final T object) {
         if (object == null) {
             return null;
         }
 
         if (ReflectionUtil.isListType(object)) {
             final List<?> list = (List<?>) object;
             return (T) copyList(list);
         }
 
         return createCopyObject(object);
     }
 
     /**
      * Creates a copy of specified list.
      * 
      * @param <T>
      *            type objects in the list
      * @param list
      *            list for copying
      * @return copied list
      */
     protected <T> List<T> copyList(final List<T> list) {
         return new ArrayList<T>(list);
     }
 
     /**
      * Creates a copy of specified object by creating instance with reflection and fills it using get and set method of
      * a class.
      * 
      * @param <T>
      *            type of the object
      * @param object
      *            object for copying
      * @return copied entity
      */
     private <T> T createCopyObject(final T object) {
 
         final T copy = createEmptyCopyOf(object);
 
         final Class<?> classObject = object.getClass();
         for (final Method method : classObject.getMethods()) {
 
             if (ReflectionUtil.isGetMethod(object.getClass(), method) && method.getParameterAnnotations().length == 0) {
                 final String propertyName = ReflectionUtil.getFieldName(method);
                 final Object propertyForCopying = getPropertyForCopying(object, method);
                 final Object copiedProperty = copyProperty(propertyForCopying);
                 invokeSetMethod(method, classObject, propertyName, copy, copiedProperty);
             }
         }
         return copy;
     }
 
     /**
      * Gets property for copying using reflection.
      * 
      * @param <T>
      *            type of the object
      * @param object
      *            property's parent
      * @param method
      *            get method for property
      * @return property
      */
     protected <T> Object getPropertyForCopying(final T object, final Method method) {
         try {
             return method.invoke(object);
         } catch (final Exception e) {
             e.printStackTrace();
             fail(e.getMessage());
         }
         return null;
     }
 
     /**
      * Copies property.
      * 
      * @param propertyForCopying
      *            property for copying
      * @return copied property
      */
     protected Object copyProperty(final Object propertyForCopying) {
         if (propertyForCopying == null) {
             // its null we shouldn't do anything
             return null;
         }
 
         if (ReflectionUtil.isComplexType(propertyForCopying.getClass(), complexTypes)) {
             // its complex object, we need its copy
             return createCopyObject(propertyForCopying);
         }
 
         if (ReflectionUtil.isListType(propertyForCopying)) {
             // just creating new list with same elements
             return copyList((List<?>) propertyForCopying);
         }
 
         // if its not list or some complex type same object will be added.
         return propertyForCopying;
 
     }
 
     /**
      * Invokes specified set method via reflection to set property to object.
      * 
      * @param <T>
      *            object type
      * @param method
      *            get method for property
      * @param classObject
      *            parent class for property
      * @param propertyName
      *            property name
      * @param object
      *            copied parent object
      * @param copiedProperty
      *            copied property
      */
     protected <T> void invokeSetMethod(final Method method, final Class<?> classObject, final String propertyName,
             final T object, final Object copiedProperty) {
         Method setMethod = null;
         try {
             setMethod = classObject.getMethod(SET_METHOD_PREFIX + StringUtils.capitalize(propertyName),
                     method.getReturnType());
             setMethod.invoke(object, copiedProperty);
         } catch (final Exception e) {
             e.printStackTrace();
             fail(e.getMessage());
         }
     }
 
     /**
      * Creates empty copy of object using reflection to call default constructor.
      * 
      * @param <T>
      *            type of copied object
      * @param object
      *            object for copying
      * @return copied empty instance of specified object or <code>null</code> if default constructor can not be called
      */
     protected <T> T createEmptyCopyOf(final T object) {
         try {
             return (T) object.getClass().getConstructor().newInstance();
         } catch (final Exception e) {
             e.printStackTrace();
             fail(e.getMessage());
         }
         return null;
     }
 
 }
