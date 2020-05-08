 package com.dottydingo.hyperion.service.translation;
 
 import com.dottydingo.hyperion.api.BaseApiObject;
 import com.dottydingo.hyperion.service.model.BasePersistentObject;
 import net.sf.cglib.beans.BeanMap;
 
 import java.util.*;
 
 /**
 * User: mark
 * Date: 9/3/12
 * Time: 9:31 PM
  */
 public abstract class BaseTranslator<C extends BaseApiObject,P extends BasePersistentObject> implements Translator<C,P>
 {
    private BeanMap clientBeanMap;
    private BeanMap persistentBeanMap;
     private Map<String,FieldMapper> fieldMapperMap = new HashMap<String, FieldMapper>();
 
     protected abstract C createClientInstance();
     protected abstract P createPersistentInstance();
 
     public void init()
     {
         clientBeanMap = BeanMap.create(createClientInstance());
         persistentBeanMap = BeanMap.create(createPersistentInstance());
         initializeDefaultFieldMappers();
         initializeCustomFieldMappers();
     }
 
     protected void beforeConvert(C client, P persistent, TranslationContext context){}
 
     @Override
     public P convertClient(C client, TranslationContext context)
     {
         P persistentObject = createPersistentInstance();
 
         beforeConvert(client,persistentObject,context);
 
         for (FieldMapper mapper : fieldMapperMap.values())
         {
             mapper.convertToPersistent(client,persistentObject,context);
         }
 
         afterConvert(client,persistentObject,context);
 
         return persistentObject;
     }
 
     protected void afterConvert(C client, P persistent, TranslationContext context){}
 
     protected void beforeCopy(C client, P persistent, TranslationContext context){}
 
     @Override
     public void copyClient(C client, P persistent, TranslationContext context)
     {
         beforeCopy(client,persistent,context);
         for (FieldMapper mapper : fieldMapperMap.values())
         {
             mapper.convertToPersistent(client,persistent,context);
         }
         afterCopy(client,persistent,context);
     }
 
     protected void afterCopy(C client, P persistent, TranslationContext context){}
 
     @Override
     public C convertPersistent(P persistent, TranslationContext context)
     {
         C clientObject = createClientInstance();
 
         Set<String> requestedFields = context.getRequestedFields();
 
         for (Map.Entry<String, FieldMapper> entry : fieldMapperMap.entrySet())
         {
             if(requestedFields == null || requestedFields.contains(entry.getKey()))
             {
                 entry.getValue().convertToClient(persistent,clientObject,context);
             }
         }
 
         convertPersistent(clientObject,persistent,context);
 
         return clientObject;
     }
 
     protected void convertPersistent(C client, P persistent, TranslationContext context){}
 
     @Override
     public List<C> convertPersistent(List<P> persistent, TranslationContext context)
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
         Set<String> clientFields =  clientBeanMap.keySet();
         for (String clientField : clientFields)
         {
             Class clientType = clientBeanMap.getPropertyType(clientField);
             Class persistentType = persistentBeanMap.getPropertyType(clientField);
             if(persistentType != null && clientType.equals(persistentType))
             {
                 fieldMapperMap.put(clientField,new DefaultFieldMapper(clientField,clientBeanMap,persistentBeanMap));
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
 }
