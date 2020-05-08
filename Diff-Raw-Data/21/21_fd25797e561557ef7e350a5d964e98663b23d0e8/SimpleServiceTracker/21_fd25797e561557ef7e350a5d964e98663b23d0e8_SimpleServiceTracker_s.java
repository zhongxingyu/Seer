 package org.mosaic.util.osgi;
 
 import com.google.common.base.Objects;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.osgi.framework.BundleContext;
 import org.osgi.util.tracker.ServiceTracker;
 
 import static org.mosaic.util.osgi.BundleUtils.bundleContext;
 
 /**
  * A simplified version of OSGi's {@link ServiceTracker} class.
  * <p/>
  * This class will track services of a given type, and provides it through two methods - the {@link #get()} and
  * {@link #require()}. The first returns an available service, or {@code null} if no service is available; the second
  * will either return an available service, or throw an exception.
  * <p/>
  * This tracker will automatically open, if has not been explicitly opened using the {@link #open()} method. You can
  * optionally {@link #close() close} it but that's usually not necessary.
  *
  * @author arik
  */
 public class SimpleServiceTracker<Type>
 {
     @Nonnull
     private final BundleContext bundleContext;
 
     @Nonnull
     private final ServiceTracker<Type, Type> serviceTracker;
 
     @Nonnull
     private final Class<Type> serviceType;
 
     /**
      * Constructs a new service tracker, using the bundle context of the bundle that loaded the given context class,
      * and will track services of the given service type.
      *
      * @param contextType use the bundle that loaded this class to obtain the services
      * @param serviceType track services of this type
      */
     public SimpleServiceTracker( @Nonnull Class<?> contextType, @Nonnull Class<Type> serviceType )
     {
        this( bundleContext( contextType ).get(), serviceType );
     }
 
     /**
      * Constructs a new service tracker that will use the given bundle context to obtain services, and will track
      * services of the given service type.
      *
      * @param bundleContext the bundle context to use for obtaining services
      * @param serviceType   the type of services to track
      */
     public SimpleServiceTracker( @Nonnull BundleContext bundleContext, @Nonnull Class<Type> serviceType )
     {
         this.bundleContext = bundleContext;
         this.serviceType = serviceType;
         this.serviceTracker = new ServiceTracker<>( this.bundleContext, this.serviceType, null );
     }
 
     /**
      * Open this service tracker.
      * <p/>
      * This method will do nothing if already opened.
      */
     public void open()
     {
         this.serviceTracker.open();
     }
 
     /**
      * Close this service tracker. Will automatically re-open on next invocation of {@link #get()} or
      * {@link #require()}.
      * <p/>
      * This method will do nothing if already closed.
      */
     public void close()
     {
         this.serviceTracker.close();
     }
 
     /**
      * Return a service instance of our tracked service type, or {@code null} if no such service is available.
      *
      * @return service instance, or {@code null}
      */
     @Nullable
     public Type get()
     {
         open();
         return this.serviceTracker.getService();
     }
 
     /**
      * Return a service instance of our tracked service type, or {@code null} if no such service is available.
      *
      * @return service instance, or {@code null}
      */
     @Nonnull
     public Type getOrWait( long timeout ) throws InterruptedException
     {
         open();
         return this.serviceTracker.waitForService( timeout );
     }
 
     /**
      * Return a service instance of our tracked service type, throwing an exception if no such service is available.
      *
      * @return service instance
      * @throws java.lang.IllegalStateException if no appropriate service is available
      */
     @Nonnull
     public Type require()
     {
         Type service = get();
         if( service == null )
         {
             throw new IllegalStateException( "service '" + this.serviceType.getName() + "' is not currently available" );
         }
         else
         {
             return service;
         }
     }
 
     @Override
     public String toString()
     {
         return Objects.toStringHelper( this )
                       .add( "bundle", BundleUtils.toString( this.bundleContext ) )
                       .add( "type", this.serviceType.getSimpleName() )
                       .toString();
     }
 }
