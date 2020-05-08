 package com.dottydingo.hyperion.service.translation;
 
import com.dottydingo.hyperion.api.AuditableApiObject;
 import com.dottydingo.hyperion.service.context.RequestContext;
 import com.dottydingo.hyperion.service.model.AuditablePersistentObject;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * A base field mapper for auditable entities
  */
public abstract class AuditingTranslator<C extends AuditableApiObject,P extends AuditablePersistentObject>
     extends BaseTranslator<C,P>
 {
 
     @Override
     protected List<FieldMapper> getCustomFieldMappers()
     {
         List<FieldMapper> mappers = new ArrayList<FieldMapper>();
         mappers.addAll(super.getCustomFieldMappers());
         mappers.add(new ReadOnlyFieldMapper("created"));
         mappers.add(new ReadOnlyFieldMapper("createdBy"));
         mappers.add(new ReadOnlyFieldMapper("modified"));
         mappers.add(new ReadOnlyFieldMapper("modifiedBy"));
         return mappers;
     }
 
     @Override
     protected void afterConvert(ObjectWrapper<C> clientObjectWrapper, ObjectWrapper<P> persistentObjectWrapper, RequestContext context)
     {
         super.afterConvert(clientObjectWrapper, persistentObjectWrapper,context);
         Date now = new Date();
         P persistent = persistentObjectWrapper.getWrappedObject();
         persistent.setCreated(now);
         persistent.setCreatedBy(context.getUserContext().getUserIdentifier());
         persistent.setModified(now);
         persistent.setModifiedBy(context.getUserContext().getUserIdentifier());
     }
 
     @Override
     protected void afterCopy(ObjectWrapper<C> clientObjectWrapper, ObjectWrapper<P> persistentObjectWrapper, RequestContext context)
     {
         super.afterCopy(clientObjectWrapper, persistentObjectWrapper, context);
         P persistent = persistentObjectWrapper.getWrappedObject();
         persistent.setModified(new Date());
         persistent.setModifiedBy(context.getUserContext().getUserIdentifier()) ;
     }
 
 }
