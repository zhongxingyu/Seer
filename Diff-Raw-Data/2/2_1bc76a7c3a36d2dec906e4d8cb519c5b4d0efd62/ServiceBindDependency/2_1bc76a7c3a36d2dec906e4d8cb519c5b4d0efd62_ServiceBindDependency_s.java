 package org.mosaic.lifecycle.impl.dependency;
 
 import java.util.Map;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.mosaic.lifecycle.impl.ModuleImpl;
 import org.mosaic.util.reflection.MethodHandle;
 import org.osgi.framework.ServiceReference;
 import org.osgi.util.tracker.ServiceTracker;
 
 import static org.mosaic.lifecycle.impl.util.FilterUtils.createFilter;
 
 /**
  * @author arik
  */
 public class ServiceBindDependency extends AbstractSingleServiceDependency
 {
     private final boolean bindUpdates;
 
     public ServiceBindDependency( @Nonnull ModuleImpl module,
                                   @Nullable String filterSpec,
                                   boolean bindUpdates,
                                   @Nonnull String beanName,
                                   @Nonnull MethodHandle methodHandle )
     {
         super( module, filterSpec, beanName, methodHandle );
         this.bindUpdates = bindUpdates;
     }
 
     @Override
     public String toString()
     {
         return String.format( "ServiceBind[%s] for %s",
                              createFilter( this.serviceType, this.filter ),
                               this.methodHandle );
     }
 
     @Override
     public boolean isSatisfiedInternal( @Nonnull ServiceTracker<?, ?> tracker )
     {
         return true;
     }
 
     public void beanCreated( @Nonnull Object bean )
     {
         if( this.tracker != null )
         {
             for( Map.Entry<? extends ServiceReference<?>, ?> entry : this.tracker.getTracked().entrySet() )
             {
                 inject( bean, entry.getKey(), entry.getValue() );
             }
         }
     }
 
     @Override
     public void beanInitialized( @Nonnull Object bean )
     {
         // no-op
     }
 
     @Override
     protected void onServiceAdded( @Nonnull ServiceReference<Object> reference, @Nonnull Object service )
     {
         inject( reference, service );
     }
 
     @Override
     protected void onServiceModified( @Nonnull ServiceReference<Object> reference, @Nonnull Object service )
     {
         if( this.bindUpdates )
         {
             onServiceAdded( reference, service );
         }
     }
 }
