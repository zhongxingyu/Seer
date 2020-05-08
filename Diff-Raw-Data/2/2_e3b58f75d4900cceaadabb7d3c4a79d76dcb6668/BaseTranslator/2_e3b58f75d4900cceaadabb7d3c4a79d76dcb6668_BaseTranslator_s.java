 package com.dottydingo.hyperion.service.translation;
 
 import com.dottydingo.hyperion.api.ApiObject;
 import com.dottydingo.hyperion.service.persistence.PersistenceContext;
 import com.dottydingo.hyperion.service.model.PersistentObject;
 import com.dottydingo.hyperion.service.pipeline.auth.AuthorizationContext;
 
 import java.util.*;
 
 /**
  */
 public abstract class BaseTranslator<C extends ApiObject,P extends PersistentObject> implements Translator<C,P>
 {
     protected TypeMapper clientTypeMapper;
     protected TypeMapper persistentTypeMapper;
     private Map<String,FieldMapper> fieldMapperMap = new HashMap<String, FieldMapper>();
 
 
 
     protected abstract C createClientInstance();
     protected abstract P createPersistentInstance();
 
     public void init()
     {
         clientTypeMapper = new TypeMapper(createClientInstance().getClass());
         persistentTypeMapper = new TypeMapper(createPersistentInstance().getClass());
         initializeDefaultFieldMappers();
         initializeCustomFieldMappers();
     }
 
     protected void beforeConvert(ObjectWrapper<C> clientObjectWrapper, ObjectWrapper<P> persistentObjectWrapper,
                                  PersistenceContext context){}
 
     @Override
     public P convertClient(C client, PersistenceContext context)
     {
         P persistentObject = createPersistentInstance();
         ObjectWrapper<P> persistentObjectWrapper = createPersistentObjectWrapper(persistentObject,context);
         ObjectWrapper<C> clientObjectWrapper = createClientObjectWrapper(client,context);
 
         beforeConvert(clientObjectWrapper,persistentObjectWrapper,context);
 
         AuthorizationContext authorizationContext = context.getAuthorizationContext();
         for (FieldMapper mapper : fieldMapperMap.values())
         {
             if(authorizationContext.isWritable(mapper.getClientFieldName()))
                 mapper.convertToPersistent(clientObjectWrapper,persistentObjectWrapper,context);
         }
 
         afterConvert(clientObjectWrapper,persistentObjectWrapper,context);
 
         return persistentObject;
     }
 
     protected void afterConvert(ObjectWrapper<C> clientObjectWrapper, ObjectWrapper<P> persistentObjectWrapper,
                                 PersistenceContext context){}
 
     protected void beforeCopy(ObjectWrapper<C> clientObjectWrapper, ObjectWrapper<P> persistentObjectWrapper,
                               PersistenceContext context){}
 
     @Override
     public boolean copyClient(C client, P persistent, PersistenceContext context)
     {
         boolean dirty = false;
         ObjectWrapper<P> persistentObjectWrapper = createPersistentObjectWrapper(persistent,context);
         ObjectWrapper<C> clientObjectWrapper = createClientObjectWrapper(client,context);
 
         beforeCopy(clientObjectWrapper,persistentObjectWrapper,context);
 
         AuthorizationContext authorizationContext = context.getAuthorizationContext();
         for (FieldMapper mapper : fieldMapperMap.values())
         {
             if(authorizationContext.isWritable(mapper.getClientFieldName())
                     && mapper.convertToPersistent(clientObjectWrapper,persistentObjectWrapper,context))
             {
                 dirty = true;
                 context.addChangedField(mapper.getClientFieldName());
             }
         }
 
         if(dirty)
             context.setDirty();
 
         afterCopy(clientObjectWrapper,persistentObjectWrapper,context);
 
         return dirty;
     }
 
     protected void afterCopy(ObjectWrapper<C> clientObjectWrapper, ObjectWrapper<P> persistentObjectWrapper,
                              PersistenceContext context){}
 
     @Override
     public C convertPersistent(P persistent, PersistenceContext context)
     {
         C clientObject = createClientInstance();
 
         ObjectWrapper<P> persistentObjectWrapper = createPersistentObjectWrapper(persistent,context);
         ObjectWrapper<C> clientObjectWrapper = createClientObjectWrapper(clientObject,context);
 
         Set<String> requestedFields = context.getRequestedFields();
         AuthorizationContext authorizationContext = context.getAuthorizationContext();
 
         for (Map.Entry<String, FieldMapper> entry : fieldMapperMap.entrySet())
         {
             if((requestedFields == null || requestedFields.contains(entry.getKey()))
                    && authorizationContext.isWritable(entry.getKey()))
             {
 
                 entry.getValue().convertToClient(persistentObjectWrapper,clientObjectWrapper,context);
             }
         }
 
         convertPersistent(clientObject,persistent,context);
 
         return clientObject;
     }
 
     protected void convertPersistent(C client, P persistent, PersistenceContext context){}
 
     @Override
     public List<C> convertPersistent(List<P> persistent, PersistenceContext context)
     {
         List<C> list = new LinkedList<C>();
         for (P p : persistent)
         {
             list.add(convertPersistent(p,context));
         }
 
         return list;
     }
 
 
     private void initializeDefaultFieldMappers()
     {
         Set<String> clientFields =  clientTypeMapper.getFieldNames();
         for (String clientField : clientFields)
         {
             Class clientType = clientTypeMapper.getFieldType(clientField);
             Class persistentType = persistentTypeMapper.getFieldType(clientField);
             if(persistentType != null && clientType.equals(persistentType))
             {
                 fieldMapperMap.put(clientField,new DefaultFieldMapper(clientField));
             }
         }
     }
 
     private void initializeCustomFieldMappers()
     {
         List<FieldMapper> mappers = getCustomFieldMappers();
         for (FieldMapper mapper : mappers)
         {
             fieldMapperMap.put(mapper.getClientFieldName(),mapper);
         }
     }
 
     protected List<FieldMapper> getCustomFieldMappers()
     {
         return new ArrayList<FieldMapper>();
     }
 
     protected ObjectWrapper<C> createClientObjectWrapper(C client,  PersistenceContext context)
     {
         return new ObjectWrapper<C>(client, clientTypeMapper);
     }
 
     protected ObjectWrapper<P> createPersistentObjectWrapper(P persistent, PersistenceContext context)
     {
         return new ObjectWrapper<P>(persistent, persistentTypeMapper);
     }
 
 }
