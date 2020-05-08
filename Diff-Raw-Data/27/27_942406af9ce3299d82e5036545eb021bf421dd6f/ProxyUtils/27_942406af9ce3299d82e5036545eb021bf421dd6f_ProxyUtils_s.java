 /*
  * Copyright (C) 2013 The Conclusions Authors
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file without in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package ru.rulex.conclusion.delegate;
 
 import ru.rulex.conclusion.ConclusionPredicate;
 import ru.rulex.conclusion.JavaCglibInvocInterceptor;
 import ru.rulex.conclusion.Selector;
 import ru.rulex.conclusion.dagger.SelectorKeeper;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 
 import org.apache.log4j.Logger;
 
 import com.google.common.base.Preconditions;
 import com.google.common.reflect.Reflection;
 import net.sf.cglib.proxy.Enhancer;
 import net.sf.cglib.proxy.MethodInterceptor;
 
 public class ProxyUtils
 {
   private static final Logger logger = Logger.getLogger( ProxyUtils.class );
   private static final ThreadLocal<InvocationManager> invocationManager = new ThreadLocal<InvocationManager>();
 
   private static <T> InvocationManager threadSafe()
   {
     InvocationManager manager = invocationManager.get();
     if ( manager == null )
     {
       manager = new InvocationManager();
       invocationManager.set( manager );
     }
     return manager;
   }
 
   /**
    * @param ignoredValue
    * @param <T>
    * @return ConclusionPredicate<T>
    */
   public static <T> ConclusionPredicate<T> toPredicate( Object ignoredValue )
   {
     final Invokable<T, Boolean> invokable = ProxyUtils.<T, Boolean> poolInvokable();
     Preconditions.checkNotNull( invokable );
     return Invokable.invokablePredicate( invokable );
   }
 
   /**
    * 
    * @param ignoredValue
    * @return Selector<T, E>
    * 
    */
   public static <T, E> Selector<T, E> toSelector( E ignoredValue )
   {
     final Invokable<T, E> invokable = ProxyUtils.<T, E> poolInvokable();
     Preconditions.checkNotNull( invokable );
     return Invokable.<T, E> invokableSelector( invokable );
   }
 
   public static <T, E> SelectorKeeper toCastedSelector( E ignoredValue )
   {
     final Invokable<T, E> invokable = ProxyUtils.<T, E> poolInvokable();
     Preconditions.checkNotNull( invokable );
    SelectorKeeper.INSTANCE.setDelegate(Invokable.invokableSelector( invokable ));
     SelectorKeeper.INSTANCE.setExpressionClass( invokable.toString()  );
     return SelectorKeeper.INSTANCE;
   }
 
   public static <T> T callOn( Class<T> clazz, final T original )
   {
     return JavaReflectionImposterizer.INSTANCE.imposterise( clazz, original );
   }
 
   public static <T> T callOn( Class<T> clazz )
   {
     return JavaReflectionImposterizer.INSTANCE.imposterise( clazz );
   }
 
   interface Imposterizer
   {
     public boolean canImposterise( Class<?> type );
 
     public <T> T imposterise( Class<T> mockedType, Class<?>... ancilliaryTypes );
 
     public <T> T imposterise( Class<T> mockedType, T original, Class<?>... ancilliaryTypes );
   }
 
   public static <T> void pushInvokable( Invokable<?, ?> invokable )
   {
     threadSafe().pushInvokable( invokable );
   }
 
   @SuppressWarnings("unchecked")
   public static <T, E> Invokable<T, E> poolInvokable()
   {
     return (Invokable<T, E>) threadSafe().poolInvokable();
   }
 
   public static class JavaReflectionImposterizer implements Imposterizer
   {
     public static final Imposterizer INSTANCE = new JavaReflectionImposterizer();
 
     public boolean canImposterise( Class<?> type )
     {
       return type.isInterface();
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <T> T imposterise( Class<T> mockedType, Class<?>... types )
     {
       return canImposterise( mockedType ) ? createNativeJavaProxy( mockedType, new PushableHandler() )
           : (T) createEnhancer( new PredicateProxyArgument(), mockedType, types ).create();
     }
 
     private Class<?>[] prepend( Class<?> first, Class<?>... rest )
     {
       Class<?>[] proxiedClasses = new Class<?>[rest.length + 1];
       proxiedClasses[0] = first;
       System.arraycopy( rest, 0, proxiedClasses, 1, rest.length );
       return proxiedClasses;
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <T> T imposterise( Class<T> mockedType, final T original, Class<?>... types )
     {
       return canImposterise( mockedType ) ? createNativeJavaProxy( mockedType, new InterceptableHandler<T>(
           original ) ) : (T) createEnhancer( new PredicateProxyArgument(), mockedType, types ).create();
     }
   }
 
   private static <T> T createNativeJavaProxy( Class<T> mockedType, InvocationHandler interceptor )
   {
     // guava way instead Proxy.newProxyInstance(classLoader, interfaces,
     // interceptor);
     return Reflection.newProxy( mockedType, interceptor );
   }
 
   private static Enhancer createEnhancer( MethodInterceptor interceptor, Class<?> clazz,
       Class<?>... interfaces )
   {
     Enhancer enhancer = new Enhancer();
     enhancer.setCallback( interceptor );
     enhancer.setSuperclass( clazz );
     if ( interfaces != null && interfaces.length > 0 )
       enhancer.setInterfaces( interfaces );
     return enhancer;
   }
 
   // TODO: change name
   private static class InterceptableHandler<T> implements InvocationHandler
   {
     final T original;
 
     InterceptableHandler( T original )
     {
       this.original = original;
     }
 
     protected boolean isApplyMethod( Method method )
     {
       return method.getName().equals( "apply" ) && (method.getParameterTypes()[0] == Object.class);
     }
 
     @Override
     public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
     {
       if ( isApplyMethod( method ) )
         logger.info( String.format( "%s %s", proxy.toString(), args[0] ) );
 
       return method.invoke( original, args );
     }
   }
 
   private static class PushableHandler implements InvocationHandler
   {
     @Override
     public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
     {
       pushInvokable( Invokable.invokableMethod( method, args ) );
       return true;
     }
   }
 
   private static class PredicateProxyArgument extends JavaCglibInvocInterceptor
   {
     @Override
     public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
     {
       pushInvokable( Invokable.invokableMethod( method, args ) );
       return null;
     }
   }
 }
