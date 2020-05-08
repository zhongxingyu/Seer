 package org.mosaic.modules.spi;
 
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import java.lang.reflect.Method;
 import java.util.*;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.mosaic.util.collections.HashMapEx;
 import org.mosaic.util.collections.MapEx;
 import org.osgi.framework.ServiceReference;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static java.util.Arrays.copyOf;
 import static org.mosaic.util.osgi.BundleUtils.requireBundleContext;
 
 /**
  * @author arik
  */
 final class MethodInterceptorManager
 {
     private static final Logger LOG = LoggerFactory.getLogger( MethodInterceptorManager.class );
 
     @Nonnull
     private final MethodInterceptor.BeforeInvocationDecision continueDecision = new ContinueDecision();
 
     @Nonnull
     private final MethodInterceptor.BeforeInvocationDecision abortDecision = new AbortDecision();
 
     @Nonnull
     private final LoadingCache<Method, List<InterestedMethodInterceptorEntry>> methodInterceptorsCache =
             CacheBuilder.newBuilder()
                         .concurrencyLevel( 100 )
                         .initialCapacity( 100000 )
                         .weakKeys()
                         .build( new CacheLoader<Method, List<InterestedMethodInterceptorEntry>>()
                         {
                             @Nonnull
                             @Override
                             public List<InterestedMethodInterceptorEntry> load( @Nonnull Method method )
                                     throws Exception
                             {
                                 Object[] services = MethodInterceptorManager.this.interceptorsTracker.getServices();
                                 if( services == null || services.length == 0 )
                                 {
                                     return Collections.emptyList();
                                 }
 
                                 List<InterestedMethodInterceptorEntry> interceptors = null;
                                 for( Object service : services )
                                 {
                                     MapEx<String, Object> context = new HashMapEx<>();
 
                                     MethodInterceptor interceptor = ( MethodInterceptor ) service;
                                     if( interceptor.interestedIn( method, context ) )
                                     {
                                         if( interceptors == null )
                                         {
                                             interceptors = new LinkedList<>();
                                         }
                                         interceptors.add( new InterestedMethodInterceptorEntry( interceptor, context ) );
                                     }
                                 }
                                 return interceptors == null ? Collections.<InterestedMethodInterceptorEntry>emptyList() : interceptors;
                             }
                         } );
 
     @Nonnull
     private final ServiceTracker<MethodInterceptor, MethodInterceptor> interceptorsTracker =
             new ServiceTracker<>( requireBundleContext( getClass() ),
                                   MethodInterceptor.class,
                                   new ServiceTrackerCustomizer<MethodInterceptor, MethodInterceptor>()
                                   {
                                       @Override
                                       public MethodInterceptor addingService( @Nonnull ServiceReference<MethodInterceptor> reference )
                                       {
                                           MethodInterceptorManager.this.methodInterceptorsCache.invalidateAll();
                                           return requireBundleContext( getClass() ).getService( reference );
                                       }
 
                                       @Override
                                       public void modifiedService( @Nonnull ServiceReference<MethodInterceptor> reference,
                                                                    @Nonnull MethodInterceptor service )
                                       {
                                           MethodInterceptorManager.this.methodInterceptorsCache.invalidateAll();
                                       }
 
                                       @Override
                                       public void removedService( @Nonnull ServiceReference<MethodInterceptor> reference,
                                                                   @Nonnull MethodInterceptor service )
                                       {
                                           MethodInterceptorManager.this.methodInterceptorsCache.invalidateAll();
                                       }
                                   } );
 
     @Nonnull
     private final ThreadLocal<Deque<InvocationContext>> contextHolder = new ThreadLocal<Deque<InvocationContext>>()
     {
         @Override
         protected Deque<InvocationContext> initialValue()
         {
             return new LinkedList<>();
         }
     };
 
     MethodInterceptorManager()
     {
         this.interceptorsTracker.open();
     }
 
     public boolean beforeInvocation( long id, @Nullable Object object, @Nonnull Object[] arguments ) throws Throwable
     {
         // create context for method invocation and push to stack
         InvocationContext context = new InvocationContext( id, object, arguments );
         this.contextHolder.get().push( context );
 
         // invoke interceptors "before" action, returning true if method should proceed, false if circumvent method and request a call to "after" action
         return context.beforeInvocation();
     }
 
     @Nullable
     public Object afterAbortedInvocation() throws Throwable
     {
         return this.contextHolder.get().peek().afterInvocation();
     }
 
     @Nullable
     public Object afterSuccessfulInvocation( @Nullable Object returnValue ) throws Throwable
     {
         InvocationContext context = this.contextHolder.get().peek();
         context.returnValue = returnValue;
         return context.afterInvocation();
     }
 
     @Nullable
     public Object afterThrowable( @Nonnull Throwable throwable ) throws Throwable
     {
         InvocationContext context = this.contextHolder.get().peek();
         context.throwable = throwable;
         return context.afterInvocation();
     }
 
     public void cleanup( long id )
     {
         Deque<InvocationContext> deque = this.contextHolder.get();
         if( deque.isEmpty() )
         {
             try
             {
                 Method receivedMethod = MethodCache.getInstance().getMethod( id );
                 LOG.error( "STACK EMPTY! received method: {}", receivedMethod.toGenericString() );
             }
             catch( ClassNotFoundException e )
             {
                 LOG.error( "STACK EMPTY! could not extract method from {}", id, e );
             }
         }
         else if( deque.peek().id != id )
         {
             try
             {
                 Method receivedMethod = MethodCache.getInstance().getMethod( id );
                 Method methodOnStack = MethodCache.getInstance().getMethod( deque.peek().id );
                 LOG.error( "STACK DIRTY!\n" +
                            "    On stack: {}\n" +
                            "    Received: {}",
                            methodOnStack.toGenericString(), receivedMethod.toGenericString() );
             }
             catch( ClassNotFoundException e )
             {
                 LOG.error( "STACK DIRTY! could not extract methods from IDs {} and {}", id, deque.peek().id, e );
             }
         }
         else
         {
             deque.pop();
         }
     }
 
     private class InterestedMethodInterceptorEntry
     {
         @Nonnull
         private final MethodInterceptor target;
 
         @Nonnull
         private final MapEx<String, Object> interceptorContext;
 
         private InterestedMethodInterceptorEntry( @Nonnull MethodInterceptor target,
                                                   @Nonnull MapEx<String, Object> interceptorContext )
         {
             this.target = target;
             this.interceptorContext = interceptorContext;
         }
     }
 
     private class InvocationContext extends HashMapEx<String, Object>
     {
         private final long id;
 
         @Nonnull
         private final Method method;
 
         @Nullable
         private final Object object;
 
         @Nonnull
         private final Object[] arguments;
 
         @Nonnull
         private final List<InterestedMethodInterceptorEntry> invokedInterceptors = new LinkedList<>();
 
         @Nullable
         private Map<InterestedMethodInterceptorEntry, MapEx<String, Object>> interceptorInvocationContexts = null;
 
         @Nullable
         private BeforeMethodInvocationImpl beforeInvocation;
 
         @Nullable
         private AfterMethodInvocationImpl afterInvocation;
 
         @Nullable
         private AfterMethodExceptionImpl afterThrowable;
 
         @Nullable
         private Throwable throwable;
 
         @Nullable
         private Object returnValue;
 
         private InvocationContext( long id, @Nullable Object object, @Nonnull Object[] arguments )
                 throws ClassNotFoundException
         {
             super( 5 );
             this.id = id;
             this.method = MethodCache.getInstance().getMethod( id );
             this.object = object;
             this.arguments = arguments;
         }
 
         private boolean beforeInvocation()
         {
             for( InterestedMethodInterceptorEntry interceptorEntry : MethodInterceptorManager.this.methodInterceptorsCache.getUnchecked( this.method ) )
             {
                 MethodInterceptor interceptor = interceptorEntry.target;
                 try
                 {
                     BeforeMethodInvocationImpl invocation = getBeforeInvocation();
                     invocation.methodInterceptorEntry = interceptorEntry;
 
                     // invoke interceptor
                     MethodInterceptor.BeforeInvocationDecision decision = interceptor.beforeInvocation( invocation );
 
                     // if interceptor succeeded, add it to the list of interceptors to invoke on "after" action
                     // note that we add it to the start, so we can invoke them in reverse order in "after" action
                     this.invokedInterceptors.add( 0, interceptorEntry );
 
                     if( decision == MethodInterceptorManager.this.abortDecision )
                     {
                         return false;
                     }
                     else if( decision != MethodInterceptorManager.this.continueDecision )
                     {
                         throw new IllegalStateException( "Method interceptor's \"before\" did not use MethodInvocation.continue/abort methods" );
                     }
                 }
                 catch( Throwable throwable )
                 {
                     this.throwable = throwable;
                     this.returnValue = null;
                     return false;
                 }
             }
             return true;
         }
 
         @Nullable
         private Object afterInvocation() throws Throwable
         {
             for( InterestedMethodInterceptorEntry interceptorEntry : this.invokedInterceptors )
             {
                 MethodInterceptor interceptor = interceptorEntry.target;
                 try
                 {
                     if( this.throwable != null )
                     {
                         AfterMethodExceptionImpl invocation = getAfterThrowable();
                         invocation.methodInterceptorEntry = interceptorEntry;
                         this.returnValue = interceptor.afterThrowable( invocation );
                     }
                     else
                     {
                         AfterMethodInvocationImpl invocation = getAfterInvocation();
                         invocation.methodInterceptorEntry = interceptorEntry;
                         this.returnValue = interceptor.afterInvocation( invocation );
                     }
                     this.throwable = null;
                 }
                 catch( Throwable throwable )
                 {
                     this.throwable = throwable;
                     this.returnValue = null;
                 }
             }
 
             if( this.throwable != null )
             {
                 throw this.throwable;
             }
             else
             {
                 return this.returnValue;
             }
         }
 
         @Nonnull
         private BeforeMethodInvocationImpl getBeforeInvocation()
         {
             if( this.beforeInvocation == null )
             {
                 this.beforeInvocation = new BeforeMethodInvocationImpl( this );
             }
             return this.beforeInvocation;
         }
 
         @Nonnull
         private AfterMethodInvocationImpl getAfterInvocation()
         {
             if( this.afterInvocation == null )
             {
                 this.afterInvocation = new AfterMethodInvocationImpl( this );
             }
             return this.afterInvocation;
         }
 
         @Nonnull
         private AfterMethodExceptionImpl getAfterThrowable()
         {
             if( this.afterThrowable == null )
             {
                 this.afterThrowable = new AfterMethodExceptionImpl( this );
             }
             return this.afterThrowable;
         }
 
         @Nonnull
         private MapEx<String, Object> getInterceptorInvocationContext( @Nonnull InterestedMethodInterceptorEntry interceptorEntry )
         {
             if( this.interceptorInvocationContexts == null )
             {
                 this.interceptorInvocationContexts = new HashMap<>();
             }
 
             MapEx<String, Object> context = this.interceptorInvocationContexts.get( interceptorEntry );
             if( context == null )
             {
                 context = new HashMapEx<>();
                 this.interceptorInvocationContexts.put( interceptorEntry, context );
             }
             return context;
         }
     }
 
     private class ContinueDecision implements MethodInterceptor.BeforeInvocationDecision
     {
     }
 
     private class AbortDecision implements MethodInterceptor.BeforeInvocationDecision
     {
     }
 
     private abstract class AbstractMethodInvocation implements MethodInterceptor.MethodInvocation
     {
         @Nonnull
         protected final InvocationContext context;
 
         @Nullable
         protected InterestedMethodInterceptorEntry methodInterceptorEntry;
 
         protected AbstractMethodInvocation( @Nonnull InvocationContext context )
         {
             this.context = context;
         }
 
         @Nonnull
         @Override
         public final MapEx<String, Object> getInterceptorContext()
         {
             if( this.methodInterceptorEntry == null )
             {
                 throw new IllegalStateException( "Interceptor context not set on invocation!" );
             }
             return this.methodInterceptorEntry.interceptorContext;
         }
 
         @Nonnull
         @Override
         public final MapEx<String, Object> getInvocationContext()
         {
             if( this.methodInterceptorEntry == null )
             {
                 throw new IllegalStateException( "Interceptor context not set on invocation!" );
             }
             return this.context.getInterceptorInvocationContext( this.methodInterceptorEntry );
         }
 
         @Nonnull
         @Override
         public final Method getMethod()
         {
             return this.context.method;
         }
 
         @Nullable
         @Override
         public final Object getObject()
         {
             return this.context.object;
         }
     }
 
     private class BeforeMethodInvocationImpl extends AbstractMethodInvocation
             implements MethodInterceptor.BeforeMethodInvocation
     {
         public BeforeMethodInvocationImpl( @Nonnull InvocationContext context )
         {
             super( context );
         }
 
         @Nonnull
         @Override
         public Object[] getArguments()
         {
             return this.context.arguments;
         }
 
         @Nonnull
         @Override
         public MethodInterceptor.BeforeInvocationDecision continueInvocation()
         {
             return MethodInterceptorManager.this.continueDecision;
         }
 
         @Nonnull
         @Override
         public MethodInterceptor.BeforeInvocationDecision abort( @Nullable Object returnValue )
         {
             this.context.returnValue = returnValue;
             return MethodInterceptorManager.this.abortDecision;
         }
     }
 
     private class AfterMethodInvocationImpl extends AbstractMethodInvocation
             implements MethodInterceptor.AfterMethodInvocation
     {
         @Nonnull
         private final Object[] arguments;
 
         public AfterMethodInvocationImpl( @Nonnull InvocationContext context )
         {
             super( context );
             this.arguments = copyOf( this.context.arguments, this.context.arguments.length );
         }
 
         @Nonnull
         @Override
         public Object[] getArguments()
         {
             return this.arguments;
         }
 
         @Nullable
         @Override
         public Object getReturnValue()
         {
            return null;
         }
     }
 
     private class AfterMethodExceptionImpl extends AbstractMethodInvocation
             implements MethodInterceptor.AfterMethodException
     {
         @Nonnull
         private final Object[] arguments;
 
         public AfterMethodExceptionImpl( @Nonnull InvocationContext context )
         {
             super( context );
             this.arguments = copyOf( this.context.arguments, this.context.arguments.length );
         }
 
         @Nonnull
         @Override
         public Object[] getArguments()
         {
             return this.arguments;
         }
 
         @Nonnull
         @Override
         public Throwable getThrowable()
         {
             Throwable throwable = this.context.throwable;
             if( throwable == null )
             {
                 throw new IllegalStateException( "No throwable found" );
             }
             else
             {
                 return throwable;
             }
         }
     }
 }
