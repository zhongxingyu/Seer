 package pl.com.it_crowd.seam.framework;
 
 import org.ajax4jsf.model.DataVisitor;
 import org.ajax4jsf.model.ExtendedDataModel;
 import org.ajax4jsf.model.Range;
 import org.ajax4jsf.model.SequenceRange;
 
 import javax.faces.context.FacesContext;
 import javax.persistence.Id;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.util.HashMap;
 import java.util.Map;
 
 public class EntityQueryDataModel<T> extends ExtendedDataModel<T> {
 // ------------------------------ FIELDS ------------------------------
 
     protected EntityQuery<T> dataProvider;
 
     private Field idField;
 
     private Method idGetter;
 
     private Map<Integer, Object> indexToKey = new HashMap<Integer, Object>();
 
     private Class<T> itemClass;
 
     private Map<Object, Integer> keyToIndex = new HashMap<Object, Integer>();
 
     private int rowCount = -1;
 
     private Object rowKey;
 
     private Map<Object, T> rowKeyMap = new HashMap<Object, T>();
 
     private boolean useIndex = false;
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     public EntityQueryDataModel(EntityQuery<T> query)
     {
         this(query, false);
     }
 
     public EntityQueryDataModel(EntityQuery<T> query, boolean useIndex)
     {
         this.useIndex = useIndex;
         dataProvider = query;
         this.itemClass = getEntityClass(query);
 
         Class iClass = itemClass;
         do {
             for (Field field : iClass.getDeclaredFields()) {
                 if (field.getAnnotation(Id.class) != null) {
                     idField = field;
                     break;
                 }
             }
             iClass = iClass.getSuperclass();
         } while (idField == null && iClass.getSuperclass() != null);
         if (idField == null) {
             for (Method method : itemClass.getMethods()) {
                 if (method.getAnnotation(Id.class) != null) {
                     idGetter = method;
                     break;
                 }
             }
         } else if (!Modifier.isPublic(idField.getModifiers())) {
             String idGetterName = "get" + idField.getName().substring(0, 1).toUpperCase() + idField.getName().substring(1);
             try {
                 idGetter = itemClass.getMethod(idGetterName);
             } catch (NoSuchMethodException e) {
                 throw new IllegalArgumentException(
                     "@Id annotated field " + idField.getName() + " is not public and there is no public accessor " + idGetterName);
             }
         }
         if (idField == null && idGetter == null) {
             throw new IllegalArgumentException("Entity must have @Id annotated property.");
         }
     }
 
 // --------------------- GETTER / SETTER METHODS ---------------------
 
     public int getRowCount()
     {
         if (rowCount == -1) {
             rowCount = dataProvider.getResultCount().intValue();
         }
         return rowCount;
     }
 
     public Object getRowKey()
     {
         return rowKey;
     }
 
     @SuppressWarnings("unchecked")
     public void setRowKey(Object o)
     {
         rowKey = o;
     }
 
     private Map<Object, T> getRowKeyMap()
     {
         if (rowKeyMap.isEmpty()) {
             final int resultCount = dataProvider.getResultList().size();
             for (int i = 0; i < resultCount; i++) {
                 T item = dataProvider.getResultList().get(i);
                 rowKeyMap.put(useIndex ? i : getId(item), item);
             }
         }
         return rowKeyMap;
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     @SuppressWarnings("unchecked")
     public Class<T> getEntityClass(EntityQuery query)
     {
         Class entityClass;
         Class investivatedClass = query.getClass();
         Type type;
         do {
             type = investivatedClass.getGenericSuperclass();
             if (type instanceof ParameterizedType) {
                 ParameterizedType paramType = (ParameterizedType) type;
                 if (paramType.getActualTypeArguments().length == 2) {
                     // likely dealing with -> new EntityHome<Person>().getEntityClass()
                     if (paramType.getActualTypeArguments()[1] instanceof TypeVariable) {
                         throw new IllegalArgumentException("Could not guess entity class by reflection");
                     } else {
                         // likely dealing with -> new Home<EntityManager, Person>() { ... }.getEntityClass()
                         entityClass = (Class<T>) paramType.getActualTypeArguments()[1];
                     }
                 } else {
                     // likely dealing with -> new PersonHome().getEntityClass() where PersonHome extends EntityHome<Person>
                     entityClass = (Class<T>) paramType.getActualTypeArguments()[0];
                 }
                 return entityClass;
             }
             investivatedClass = investivatedClass.getSuperclass();
         } while (!Object.class.equals(investivatedClass));
         throw new IllegalArgumentException("Could not guess entity class by reflection");
     }
 
     public T getRowData()
     {
         if (getRowKey() == null) {
             return null;
         } else {
             T item = getRowKeyMap().get(getRowKey());
             if (item == null) {
                 item = getCurrentItem();
                 getRowKeyMap().put(getRowKey(), item);
                 return item;
             } else {
                 return item;
             }
         }
     }
 
     public int getRowIndex()
     {
         final Object theRowKey = getRowKey();
         return theRowKey == null ? -1 : keyToIndex.get(theRowKey);
     }
 
     public Object getWrappedData()
     {
         throw new UnsupportedOperationException();
     }
 
     public boolean isRowAvailable()
     {
         return getRowKey() != null && null != getRowKeyMap().get(getRowKey());
     }
 
     public void setRowIndex(int i)
     {
         setRowKey(indexToKey.get(i));
     }
 
     public void setWrappedData(Object o)
     {
         throw new UnsupportedOperationException();
     }
 
     public void walk(FacesContext facesContext, DataVisitor dataVisitor, Range range, Object o)
     {
         int firstRow = ((SequenceRange) range).getFirstRow();
         int numberOfRows = ((SequenceRange) range).getRows();
         if (dataProvider.getFirstResult() == null || dataProvider.getFirstResult() != firstRow) {
             dataProvider.setFirstResult(firstRow >= 0 ? firstRow : null);
         }
         if (dataProvider.getMaxResults() == null || dataProvider.getMaxResults() != numberOfRows) {
             dataProvider.setMaxResults(numberOfRows >= 0 ? numberOfRows : null);
         }
         /**
          * Getter is not used here cause it could loop over result list, which we do not want to do twice
          */
         rowKeyMap.clear();
         indexToKey.clear();
         keyToIndex.clear();
         int i = 0;
         for (T item : dataProvider.getResultList()) {
             Object id = getId(item);
             final Object key = useIndex ? i : id;
             rowKeyMap.put(key, item);
            dataVisitor.process(facesContext, key, o);
             indexToKey.put(i, key);
             keyToIndex.put(key, i);
             i++;
         }
     }
 
     private T getCurrentItem()
     {
         return dataProvider.getEntityManager().find(itemClass, getRowKey());
     }
 
     @SuppressWarnings("unchecked")
     private Object getId(T item)
     {
         try {
             return idGetter != null ? idGetter.invoke(item) : idField.get(item);
         } catch (Exception e) {
             throw new RuntimeException((e));
         }
     }
 }
