 /*
  * Copyright (c) 2013 KloudTek Ltd
  */
 
 package com.kloudtek.systyrant.resource;
 
 import com.kloudtek.systyrant.STContext;
 import com.kloudtek.systyrant.annotation.Service;
 import com.kloudtek.systyrant.exception.FieldInjectionException;
 import com.kloudtek.systyrant.exception.InvalidServiceException;
 import com.kloudtek.systyrant.host.Host;
 import com.kloudtek.systyrant.service.ServiceManager;
 
 import java.lang.reflect.Field;
 
 /**
  * Injector for the {@link com.kloudtek.systyrant.annotation.Inject} annotation.
  */
 public class GenericInjector extends AttrInjector {
     protected GenericInjector(Field field) {
         super(field.getName(),field);
     }
 
     @Override
     public void inject(Resource resource, Object obj, STContext ctx) throws FieldInjectionException {
         Class<?> type = field.getType();
         if(STContext.class.isAssignableFrom(type) ) {
             inject(obj,ctx);
         } else if( Resource.class.isAssignableFrom(type) ) {
             inject(obj,resource);
         } else if( ServiceManager.class.isAssignableFrom(type) ) {
             inject(obj,ctx.getServiceManager());
         } else if( Host.class.isAssignableFrom(type) ) {
             inject(obj,resource.getHost());
        } else if( type.getAnnotation(Service.class) != null ) {
             try {
                 inject(obj, ctx.getServiceManager().getService(resource.getClass()));
             } catch (InvalidServiceException e) {
                 throw new FieldInjectionException(field,e.getMessage(),e);
             }
         } else {
             super.inject(obj, ctx);
         }
     }
 }
