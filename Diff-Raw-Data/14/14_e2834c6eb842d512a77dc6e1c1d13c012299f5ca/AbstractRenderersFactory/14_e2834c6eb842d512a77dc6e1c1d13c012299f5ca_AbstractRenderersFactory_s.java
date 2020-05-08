 package com.redshape.renderer;
 
 import com.redshape.utils.Commons;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public abstract class AbstractRenderersFactory implements IRenderersFactory {
     protected Map<Class<?>, Class<? extends IRenderer<?, ?>>> entities =
             new HashMap<Class<?>, Class<? extends IRenderer<?,?>>>();
     protected Map<Class<? extends IRenderer<?, ?>>, IRenderer<?, ?>> renderers =
             new HashMap<Class<? extends IRenderer<?, ?>>, IRenderer<?, ?>>();
    
     @Override
     public <T, V> IRenderer<T, V> forEntity(T entity) {
         Commons.checkNotNull(entity);
 
         return this.<T, V>forEntity( (Class<T>) entity.getClass() );
     }
 
     @Override
     public <T, V> void addRenderers(Map<Class<T>, Class<? extends IRenderer<T, V>>> renderers) {
         for ( Map.Entry<Class<T>, Class<? extends IRenderer<T, V>>> entry : renderers.entrySet() ) {
             this.addRenderer( entry.getKey(), entry.getValue() );
         }
     }
 
     @Override
     public <T, V> void addRenderer( Class<T> entity, Class<? extends IRenderer<T, V>> rendererClazz ) {
         if ( this.entities.containsKey(entity) ) {
             throw new IllegalArgumentException("Entity already exists in registry");
         }
 
         this.entities.put( entity, rendererClazz );
     }
 
 }
