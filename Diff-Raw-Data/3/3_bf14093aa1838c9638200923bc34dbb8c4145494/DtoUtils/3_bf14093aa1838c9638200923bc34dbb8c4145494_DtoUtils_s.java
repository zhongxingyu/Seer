 package com.redshape.persistence.entities;
 
 import com.redshape.persistence.DaoContextHolder;
 import com.redshape.persistence.dao.DAOException;
 import com.redshape.persistence.dao.DAOFacade;
 import com.redshape.persistence.dao.IDAO;
 import com.redshape.persistence.utils.ISessionManager;
 import com.redshape.utils.Commons;
 import com.redshape.utils.beans.Property;
 import com.redshape.utils.beans.PropertyUtils;
 import org.apache.log4j.Logger;
 
 import java.beans.IntrospectionException;
 import java.util.*;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.concurrent.LinkedBlockingQueue;
 
 /**
  * @author Cyril A. Karpenko <self@nikelin.ru>
  * @package com.redshape.persistence.entities
  * @date 2/6/12 {4:04 PM}
  */
 public final class DtoUtils {
     private static class Deferred {
         private Object target;
         private Property property;
         private Object object;
         private boolean toDTO;
 
         public Deferred(Object target, Property targetProperty, Object object, boolean toDTO) {
             Commons.checkNotNull(target);
             Commons.checkNotNull(targetProperty);
 
             this.target = target;
             this.property = targetProperty;
             this.object = object;
             this.toDTO = toDTO;
         }
 
         public String getName() {
             return this.property.getName();
         }
 
         public Object getObject() {
             return object;
         }
 
         public void initialize( Object value ) throws IntrospectionException {
             try {
                 this.property.set( this.target, value );
             } catch ( Exception e ) {
                 throw new IntrospectionException( e.getMessage() );
             }
         }
 
         public boolean isToDTO() {
             return toDTO;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (!(o instanceof Deferred)) return false;
 
             Deferred deferred = (Deferred) o;
 
             if (property != null ? !property.equals(deferred.property) : deferred.property != null) return false;
             if (target != null ? !target.equals(deferred.target) : deferred.target != null) return false;
 
             return true;
         }
 
         @Override
         public int hashCode() {
             int result = target != null ? target.hashCode() : 0;
             result = 31 * result + (property != null ? property.hashCode() : 0);
             return result;
         }
     }
 
     private static class Counter {
         private static final int MAX_STACK_SIZE = 100;
         private int entranceCounter;
 
         public void reset() {
             this.entranceCounter = 0;
         }
 
         public void enter() {
             Commons.checkArgument( entranceCounter < MAX_STACK_SIZE, "Illegal counter state");
             entranceCounter++;
         }
 
         public void leave() {
             Commons.checkArgument( entranceCounter < MAX_STACK_SIZE, "Illegal counter state");
             Commons.checkArgument( entranceCounter > 0, "Illegal counter state");
             entranceCounter--;
         }
 
         public boolean isBalanced() {
             return entranceCounter == 0;
         }
 
     }
 
     private static final Logger log = Logger.getLogger(DtoUtils.class);
 
     private static final ThreadLocal<Counter> fromCounter = new ThreadLocal<Counter>();
     private static final ThreadLocal<Counter> toCounter = new ThreadLocal<Counter>();
 
     private static final ThreadLocal<Collection<Deferred>> deferred = new ThreadLocal<Collection<Deferred>>();
     private static final ThreadLocal<Collection<Object>> processing = new ThreadLocal<Collection<Object>>();
     private static final ThreadLocal<Map<IDtoCapable, IDTO>> cache = new ThreadLocal<Map<IDtoCapable, IDTO>>();
     private static final ThreadLocal<Map<IEntity, IEntity>> reverseCache = new ThreadLocal<Map<IEntity, IEntity>>();
 
     private static final Counter toCounter() {
         if ( toCounter.get() == null ) {
             toCounter.set( new Counter() );
         }
 
         return toCounter.get();
     }
 
     private static final Counter fromCounter() {
         if ( fromCounter.get() == null ) {
             fromCounter.set( new Counter() );
         }
 
         return fromCounter.get();
     }
 
     private static final Map<IDtoCapable, IDTO> cache() {
         if ( cache.get() == null ) {
             cache.set( new HashMap<IDtoCapable, IDTO>() );
         }
 
         return cache.get();
     }
 
     private static final Map<IEntity, IEntity> reverseCache() {
         if ( reverseCache.get() == null ) {
             reverseCache.set( new HashMap<IEntity, IEntity>() );
         }
 
         return reverseCache.get();
     }
 
     private static final Collection<Object> processing() {
         if ( processing.get() == null ) {
             processing.set( new HashSet<Object>() );
         }
 
         return processing.get();
     }
 
     private static final Collection<Deferred> deferred() {
         if ( deferred.get() == null ) {
             deferred.set( new HashSet<Deferred>() );
         }
 
         return deferred.get();
     }
 
     private static void resetReverseCache() {
         reverseCache().clear();
     }
 
     private static void resetCache() {
         cache().clear();
     }
 
     protected synchronized static void processDeferred( boolean toDto ) {
         Collection<Deferred> toRemove = new HashSet<Deferred>();
         for ( Deferred deferred : deferred() ) {
             boolean processed = false;
             try {
                 if ( deferred.isToDTO() && toDto ) {
                     deferred.initialize( cache().get(deferred.getObject()) );
                     processed = true;
                 } else if ( !toDto ) {
                     deferred.initialize( reverseCache().get(deferred.getObject()) );
                     processed = true;
                 }
             } catch ( IntrospectionException e ) {
                 log.error("Unable to process deferred initialization on property " + deferred.getName() );
             }
 
             if ( processed ) {
                 toRemove.add( deferred );
             }
         }
 
 
         for (Deferred deferred : toRemove ) {
             processing().remove( deferred.getObject() );
             deferred().remove(deferred);
         }
     }
 
     protected static void saveDeferred( Deferred def ) {
         for ( Deferred deferred : deferred() ) {
             if ( def.equals(deferred) ) {
                 return;
             }
         }
 
         deferred().add(def);
     }
 
     protected static boolean toPreCheck( IEntity entity ) {
         Commons.checkNotNull(DaoContextHolder.instance().getContext(), "Global context not wired");
         if ( entity == null ) {
             return false;
         }
 
         if ( entity.isDto() ) {
             return false;
         }
 
         return true;
     }
 
     protected static boolean fromPreCheck( IEntity entity ) {
         Commons.checkNotNull(DaoContextHolder.instance().getContext(), "Global context not wired");
         if ( entity == null ) {
             return false;
         }
 
         if ( !entity.isDto() ) {
             return false;
         }
 
         return true;
     }
 
     public static <T extends IEntity> T fromDTO( IEntity entity ) {
         try {
             /**
              * Can be represented as aspect
              */
             if ( fromCounter().isBalanced() ) {
                 if ( !fromPreCheck(entity) ) {
                     if ( entity == null ) {
                         return null;
                     } else if ( !entity.isDto() ) {
                         return (T) entity;
                     }
                 }
 
                 fromCounter().enter();
                 openSession();
             } else {
                 fromCounter().enter();
                 if ( !fromPreCheck(entity) ) {
                     if ( entity == null ) {
                         return null;
                     } else if ( !entity.isDto() ) {
                         return (T) entity;
                     }
                 }
             }
 
             final IDTO dto = (IDTO) entity;
 
             IEntity result = null;
 
             Class<? extends IEntity> entityClazz = dto.getEntityClass();
             if ( entityClazz == null ) {
                 throw new IllegalStateException("<null>");
             }
 
             if ( entityClazz.isInterface() ) {
                 entityClazz = DaoContextHolder.instance().getContext().getBean( entityClazz ).getClass();
             }
         
             if ( dto.getId() != null ) {
                 DAOFacade facade = DaoContextHolder.instance().getContext().getBean(DAOFacade.class);
         
                 IDAO<? extends IEntity> dao = facade.getDAO(entityClazz);
                 Commons.checkNotNull(dao, "DAO for " + entityClazz.getCanonicalName() + " is not registered");
                 result = dao.findById( dto.getId() );
             } else {
                 try {
                     result = entityClazz.newInstance();
                 } catch ( Throwable e ) {
                     throw new DAOException( e.getMessage(), e );
                 }
             }
 
             reverseCache().put(dto, result = fromDTO(result, dto));
 
             return (T) result;
         } catch ( Throwable e ) {
             throw new IllegalStateException( e.getMessage(), e );
         } finally {
             if ( !fromCounter().isBalanced() ) {
                 fromCounter().leave();
                 if ( fromCounter().isBalanced() ) {
                     processDeferred(false);
 
                     try {
                        closeSession();
                     } catch ( Throwable e ) {}
 
                     resetReverseCache();
                 }
             }
         }
     }
 
     private static boolean isProcessing( Object object ) {
         for ( Object processing : processing() ) {
             if ( processing.equals( object ) ) {
                 return true;
             }
         }
 
         return false;
     }
     
     protected static IEntity fromDTO( IEntity entity, IEntity dto ) throws DAOException {
         try {
             Collection<Property> entityProperties = PropertyUtils.getInstance().getProperties(entity.getClass());
             Collection<Property> dtoProperties = PropertyUtils.getInstance().getProperties(dto.getClass());
             for ( Property property : entityProperties ) {
                 for ( Property dtoProperty : dtoProperties ) {
                     try {
                         if ( !dtoProperty.getName().equals( property.getName() ) ) {
                             continue;
                         }
     
                         Object value = dtoProperty.get(dto);
                         if ( value == entity ) {
                             value = null;
                         }
 
                         if ( value != null ) {
                             if ( value instanceof IDTO ) {
                                 if ( !isProcessing(value) ) {
                                     processing().add(value);
                                 } else {
                                     saveDeferred( new Deferred(entity, property, value, false ) );
                                     continue;
                                 }
 
                                 value = DtoUtils.fromDTO( (IEntity) value );
                             } else if ( isListType(value) ) {
                                 value = processList(value, true);
                             }
                         }
     
                         property.set( entity, value );
                         break;
                     } catch ( Throwable e ) {
                         log.error( e.getMessage(), e );
                     }
                 }
             }
     
             return entity;
         } catch ( IntrospectionException e ) {
             throw new DAOException( e.getMessage(), e );
         }
     }
     
     protected static Collection<?> processList( Object value, boolean fromDTO ) throws DAOException {
         Collection result;
 
         Class<?> valueClazz = value.getClass();
         if ( List.class.isAssignableFrom(valueClazz) ) {
             result = new ArrayList();
         } else if ( Set.class.isAssignableFrom(valueClazz) ) {
             result = new HashSet();
         } else if ( Queue.class.isAssignableFrom(valueClazz) ) {
             result = new LinkedBlockingQueue();
         } else if ( Deque.class.isAssignableFrom(valueClazz) ) {
             result = new LinkedBlockingDeque();
         } else {
             result = new HashSet();
         }
 
         boolean entitiesCollection = true;
 
         Object[] collection = new Object[ ( (Collection) value ).size() ];
         for ( Object part : (Collection) value ) {
             if ( part == null ) {
                 continue;
             }
 
             if ( entitiesCollection && part instanceof IEntity ) {
                 IEntity item = (IEntity) part;
                 if ( fromDTO ) {
                     if ( item.isDto() ) {
                         item = fromDTO( (IEntity) part);
                     }
                 } else {
                     if (!item.isDto() && (item instanceof IDtoCapable) ) {
                         item = DtoUtils.<IDTO, IDtoCapable<IDTO>>toDTO( (IDtoCapable) item );
                     }
                 }
 
                 if ( item == null ) {
                     continue;
                 }
 
                 result.add( item );
 
                 entitiesCollection = true;
             } else {
                 result.add( part );
 
                 entitiesCollection = false;
             }
         }
 
         return result;
     } 
     
     protected static boolean isListType( Object value ) {
         return Collection.class.isAssignableFrom(value.getClass());
     }
     
     public static <T extends IDTO, V extends IDtoCapable<T>> T toDTO( V entity ) {
         try {
             if ( toCounter().isBalanced() ) {
                 Commons.checkNotNull(DaoContextHolder.instance().getContext(), "Global context not wired");
 
                 if ( !toPreCheck( (IEntity) entity) ) {
                     if ( entity == null ) {
                         return null;
                     } else if ( entity instanceof IDTO ) {
                         return (T) entity;
                     }
                 }
 
                 toCounter().enter();
                 openSession();
             } else {
                 toCounter().enter();
                 if ( !toPreCheck( (IEntity) entity) ) {
                     if ( entity == null ) {
                         return null;
                     } else if ( entity instanceof IDTO ) {
                         return (T) entity;
                     }
                 }
             }
 
             entity = (V) getSessionManager().refresh( (IEntity) entity);
 
             T dto = entity.createDTO();
             cache().put(entity, dto);
             Commons.checkNotNull(dto);
 
             for ( Property property : PropertyUtils.getInstance().getProperties(entity.getClass()) ) {
                 for ( Property dtoProperty : PropertyUtils.getInstance().getProperties(dto.getClass()) ) {
                     try {
                         if ( !dtoProperty.getName().equals( property.getName() ) ) {
                             continue;
                         }
 
                         Object value = property.get(entity);
                         if ( value == entity ) {
                             value = null;
                         }
 
                         if ( value != null ) {
                             if ( value != null && value instanceof IDtoCapable ) {
                                 if ( !processing().contains(value) ) {
                                     processing().add( value );
                                 } else {
                                     saveDeferred( new Deferred(dto, dtoProperty, value, true) );
                                     continue;
                                 }
 
                                 value = ( (IDtoCapable) value ).toDTO();
                             } else if ( isListType(value) ) {
                                 value = processList(value, false);
                             }
                         }
 
                         dtoProperty.set( dto, value );
                     } catch ( Throwable e ) {
                         log.error( e.getMessage(), e );
                     }
                 }
             }
             return dto;
         } catch ( Throwable e ) {
             throw new IllegalStateException( e.getMessage(), e );
         } finally {
             if ( !toCounter().isBalanced() ) {
                 toCounter().leave();
                 if ( toCounter().isBalanced() ) {
                     processDeferred(true);
                     try {
                         closeSession();
                     } catch ( Throwable e ) {}
 
                     resetCache();
                 }
             }
         }
     }
 
     protected static ISessionManager getSessionManager() {
         return DaoContextHolder.instance().getContext().getBean(ISessionManager.class);
     }
 
     protected static void openSession() throws DAOException {
         getSessionManager().open();
     }
 
     protected static void closeSession() throws DAOException {
         getSessionManager().close();
     }
 
 }
