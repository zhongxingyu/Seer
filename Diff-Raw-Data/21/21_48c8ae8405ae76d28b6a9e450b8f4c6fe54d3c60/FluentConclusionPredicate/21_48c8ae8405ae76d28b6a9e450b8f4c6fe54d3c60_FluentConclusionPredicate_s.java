 /**
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
 package ru.rulex.conclusion;
 
import com.google.common.reflect.Invokable;
 
 import java.lang.reflect.Method;
 import java.text.MessageFormat;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import ru.rulex.external.JvmBasedLanguageAdapter;
import ru.rulex.conclusion.MoreSelectors.*;
 import ru.rulex.conclusion.RulexMatchersDsl.AccessorDescriptor;
 import ru.rulex.conclusion.RulexMatchersDsl.Argument;

import static com.google.common.base.Preconditions.checkNotNull;
 import static ru.rulex.conclusion.delegate.ProxyUtils.toSelector;
 
 /**
  * {@code FluentConclusionPredicate} provides a rich interface for manipulating
  * {@code ConclusionPredicate} instances in a chained fashion. A
  * {@code FluentConclusionPredicate} can be created from an
  * {@code ConclusionPredicate}, or from a set of elements. The following types
  * of methods are provided on {@code FluentConclusionPredicate}
  * 
  * <p>
  * Static factory methods are a convenient way to simulate named parameters in
  * Java, so methods {@code argument(T value)} and {@code descriptor()} is a
  * named parameters. See article on <a
  * href="http://www.infoq.com/articles/internal-dsls-java"> DSL </a>.
  * </p>
  * 
  */
 public abstract class FluentConclusionPredicate<T> implements ConclusionPredicate<T>
 {
 
   private static final Logger logger = Logger.getLogger( FluentConclusionPredicate.class );
 
   private final static Map<Class<?>, JvmBasedLanguageAdapter> languageAdaptors = new HashMap<Class<?>, JvmBasedLanguageAdapter>();
 
   /**
    * External JVM language support, like in RxJava
    * 
    */
   static
   {
     /* look for supported languages if they are in the classpath */
     loadLanguageAdaptor( "GroovyLanguageAdapter" );
     // loadLanguageAdaptor("JRuby");
     // loadLanguageAdaptor("Clojure");
     // loadLanguageAdaptor("Scala");
   }
 
   private static boolean loadLanguageAdaptor( String className )
   {
     try
     {
       Class<?> c = Class.forName( "ru.rulex.external." + className );
       JvmBasedLanguageAdapter a = (JvmBasedLanguageAdapter) c.newInstance();
       registerLanguageAdaptor( a.getFunctionClass(), a );
       logger.info( "Successfully loaded function language adaptor: " + className );
     }
     catch (ClassNotFoundException e)
     {
       logger.info( "Could not find function language adaptor: " + className );
       return false;
     }
     catch (Exception e)
     {
       logger.error( "Failed trying to initialize function language adaptor: " + className, e );
       return false;
     }
     return true;
   }
 
   public static void registerLanguageAdaptor( Class<?> functionClass,
       JvmBasedLanguageAdapter adaptor )
   {
     if ( functionClass.getPackage().getName().startsWith( "java." ) )
       throw new IllegalArgumentException(
           "FunctionLanguageAdaptor implementations can not specify java.lang.* classes." );
     languageAdaptors.put( functionClass, adaptor );
   }
 
   public static void removeLanguageAdaptor( Class<?> functionClass )
   {
     languageAdaptors.remove( functionClass );
   }
 
   public static Collection<JvmBasedLanguageAdapter> getRegisteredLanguageAdaptors()
   {
     return languageAdaptors.values();
   }
 
   protected FluentConclusionPredicate()
   {
   }
 
   /**
    * Generic singleton factory pattern (Effective java 2e ITEM 27:) On occasion,
    * you will need to create an object that is immutable but applicable to many
    * different types. Because generics are implemented by erasure, you can use a
    * single object for all required type parameterizations, but you need to
    * write a static factory method to repeatedly dole out the object for each
    * requested type parameterization.
    */
   private static final FluentConclusionPredicate<Object> ENTRY_POINT = new FluentConclusionPredicate<Object>()
   {
     private final ConclusionPredicate<Object> delegate = ConstantPredicate.FLUENT;
 
     @Override
     public boolean apply( Object argument )
     {
       return delegate.apply( argument );
     }
   };
 
   private enum ConstantPredicate implements ConclusionPredicate<Object>
   {
     FLUENT
     {
       @Override
       public boolean apply( Object argument )
       {
         throw new IllegalStateException( "FluentConclusionPredicate illegal state !!!" );
       }
     },
     ALWAYS
     {
       @Override
       public boolean apply( Object argument )
       {
         return true;
       }
     },
     NEVER
     {
       @Override
       public boolean apply( Object argument )
       {
         return false;
       }
     };
 
     @SuppressWarnings("unchecked")
     // these Object predicates work for any T
     <T> ConclusionPredicate<T> withNarrowType()
     {
       return (ConclusionPredicate<T>) this;
     }
   }
 
   /**
    * Method for define a start point for fluent building process.
    * 
    * <p>
    * since {@code fluent()} return FluentConclusionPredicate<T> instance we able
    * to call his method {@code apply()} which will throw
    * {@code IllegalStateException}
    * </p>
    * 
    * @param <T>
    * @return FluentConclusionPredicate<T>
    */
   @SuppressWarnings("unchecked")
   public static <T> FluentConclusionPredicate<T> fluent()
   {
     return (FluentConclusionPredicate<T>) ENTRY_POINT;
   }
 
   public static <T> FluentConclusionPredicate<T> always()
   {
     return bind( ConstantPredicate.ALWAYS.<T> withNarrowType() );
   }
 
   public static <T> FluentConclusionPredicate<T> never()
   {
     return bind( ConstantPredicate.NEVER.<T> withNarrowType() );
   }
 
   public static <T> FluentConclusionPredicate<T> bind(
       final ConclusionPredicate<? super T> predicate )
   {
     return new FluentConclusionPredicate<T>()
     {
       @Override
       public boolean apply( T argument )
       {
         return predicate.apply( argument );
       }
     };
   }
 
   /**
    * <p>
    * {@code fluent().and(some())  -> some() }
    * <p/>
    * <p>
    * {@code fluent().and(fluent()) -> ConstantPredicate.NEVER }
    * </p>
    * <p>
    * {@code some().and(fluent()) -> some() }
    * </p>
    * <p>
    * {@code some().and(never()) -> some() }
    * </p>
    * 
    * @param predicate
    *          argument
    * @return FluentConclusionPredicate<T> result
    * 
    */
   public FluentConclusionPredicate<T> and( ConclusionPredicate<? super T> predicate )
   {
     return this == ENTRY_POINT ? (predicate == ENTRY_POINT ? bind( ConstantPredicate.NEVER
         .<T> withNarrowType() ) : bind( predicate )) : (predicate == ENTRY_POINT) ? bind( this )
         : DslFacade.and( this, predicate );
   }
 
   /**
    * <p>
    * {@code fluent().or(some())  -> some() }
    * <p/>
    * <p>
    * {@code fluent().or(fluent()) -> ConstantPredicate.NEVER }
    * </p>
    * <p>
    * {@code some().or(fluent()) -> some() }
    * </p>
    * <p>
    * {@code some().or(never()) -> some() }
    * </p>
    * 
    * @param predicate
    *          argument
    * @return FluentConclusionPredicate<T> result
    */
   public FluentConclusionPredicate<T> or( ConclusionPredicate<? super T> predicate )
   {
     return this == ENTRY_POINT ? (predicate == ENTRY_POINT ? bind( ConstantPredicate.NEVER
         .<T> withNarrowType() ) : bind( predicate )) : (predicate == ENTRY_POINT) ? bind( this )
         : DslFacade.or( this, predicate );
   }
 
   /**
    * @param selector
    * @return
    */
   public static <T> TypeSafeSelector<T, String> string( Selector<T, String> selector )
   {
     return MoreSelectors.string( selector );
   }
 
   /**
    * @param srcType
    * @param returnType
    * @param methodName
    * @return TypeSafeSelector<T, E>
    */
   public static <T, E extends Number & Comparable<? super E>> TypeSafeSelector<T, E> number(
       Class<T> srcType, Class<E> returnType, String methodName )
   {
     return MoreSelectors.number( new ReflectiveSelector<T, E>( srcType, methodName ) );
   }
 
   /**
    * Method used for clarification your purposes to create lambda
    * 
    * @param conclusionPredicate
    * @return ConclusionPredicate<T>
    */
   public static <T> ConclusionPredicate<T> lambda( ConclusionPredicate<T> conclusionPredicate )
   {
     return conclusionPredicate;
   }
 
   /**
    * Method used for clarification your purposes to create selector
    * 
    * @param selector
    * @param <T>
    * @param <E>
    * @return Selector<T, E>
    */
   public static <T, E> Selector<T, E> selector( Selector<T, E> selector )
   {
     return selector;
   }
 
   /**
    * Returns the composition of a selector and a arguments. For every
    * {@code argument}, the generated arguments returns
    * {@code arguments.select(selector.select(argument)) }
    * 
    * @return ConclusionPredicate<T>
    */
   public static <T, E> ConclusionPredicate<T> query( Selector<T, E> selector,
       ConclusionPredicate<E> predicate )
   {
     return new SelectorPredicate<T, E>( predicate, selector );
   }
 
  /**
   * 
   * @param selectorArgument
   * @param predicate
   * @param clazz
   * @return ConclusionPredicate<T>
   */
   public static <T, E> ConclusionPredicate<T> query( E selectorArgument,
       ConclusionPredicate<E> predicate, Class<T> clazz )
   {
     Selector<T, E> selector = toSelector( selectorArgument );
     return new SelectorPredicate<T, E>( predicate, selector );
   }
 
   /**
    * @param <T>
    * @param <E>
    */
   private static final class ReflectiveSelector<T, E> implements Selector<T, E>
   {
     private final Class<T> clazz;
     private final String method;
 
     private ReflectiveSelector( final Class<T> clazz, final String method )
     {
       this.clazz = checkNotNull( clazz );
       this.method = checkNotNull( method );
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public E select( T input )
     {
       E returnValue = null;
       try
       {
         final Method reflectionMethod = clazz.getDeclaredMethod( method );
         Invokable<T, E> delegate = (Invokable<T, E>) Invokable.from( reflectionMethod );
         returnValue = delegate.invoke( input );
       }
       catch (Exception e)
       {
         throw new RuntimeException( "ReflectiveSelector: reflection error" );
       }
       return returnValue;
     }
   }
 
   /**
    * @param <T>
    * @param <E>
    */
   private static class TypeSafeSelectorPredicate<T, E> implements ConclusionPredicate<T>
   {
     private final ConclusionPredicate<E> predicate;
     private final TypeSafeSelector<T, E> path;
 
     private TypeSafeSelectorPredicate( ConclusionPredicate<E> predicate, TypeSafeSelector<T, E> path )
     {
       this.predicate = checkNotNull( predicate );
       this.path = checkNotNull( path );
     }
 
     @Override
     public boolean apply( T argument )
     {
       return predicate.apply( path.invoke( argument ) );
     }
 
     @Override
     public String toString()
     {
       return predicate.toString() + "(" + path.toString() + ")";
     }
   }
 
   /**
    * @param <T>
    * @param <E>
    */
   public static class SelectorPredicate<T, E> implements ConclusionPredicate<T>
   {
     private ConclusionPredicate<E> predicate;
     private Selector<T, E> selector;
 
     public SelectorPredicate( ConclusionPredicate<E> predicate, Selector<T, E> selector )
     {
       this.predicate = checkNotNull( predicate );
       this.selector = checkNotNull( selector );
     }
 
     @Override
     public boolean apply( T argument )
     {
       return predicate.apply( selector.select( argument ) );
     }
 
     @Override
     public String toString()
     {
       return predicate.toString() + "(" + selector.toString() + ")";
     }
   }
 
   /**
    * @param <T>
    */
   static final class OrConclusionPredicate<T> extends FluentConclusionPredicate<T>
   {
     private final Iterator<ConclusionPredicate<? super T>> predicates;
 
     OrConclusionPredicate( Iterator<ConclusionPredicate<? super T>> predicates0 )
     {
       this.predicates = checkNotNull( predicates0 );
     }
 
     @Override
     public boolean apply( T argument )
     {
       while (predicates.hasNext())
       {
         if ( predicates.next().apply( argument ) )
         {
           return true;
         }
       }
       return false;
     }
   }
 
   /**
    * @param <T>
    */
   static class AndConclusionPredicate<T> extends FluentConclusionPredicate<T>
   {
     private T argument;
     private final Iterator<ConclusionPredicate<? super T>> predicates;
 
     AndConclusionPredicate( Iterator<ConclusionPredicate<? super T>> predicates )
     {
       this.predicates = checkNotNull( predicates );
     }
 
     @Override
     public boolean apply( T argument0 )
     {
       this.argument = checkNotNull( argument0 );
       while (predicates.hasNext())
       {
         if ( !predicates.next().apply( argument ) )
         {
           return false;
         }
       }
       return true;
     }
   }
 
   /**
    * Returns the composition of TypeSafeSelector and ConclusionPredicate. For
    * every {@code argument}, the generated arguments returns
    * {@code arguments.select(path.invoke(argument)); }
    * <p/>
    * More type safe and errorless way to create
    * 
    * @return ConclusionPredicate<T>
    */
   public static <T, E> ConclusionPredicate<T> typeSafeQuery(
       TypeSafeSelector<T, E> typeSafeSelector, ConclusionPredicate<E> predicate )
   {
     return new TypeSafeSelectorPredicate<T, E>( predicate, typeSafeSelector );
   }
 
   /**
    *
    * 
    */
   public <T, E extends Number & Comparable<? super E>> FluentConclusionPredicate<T> eq(
       final Argument<E> argument, final AccessorDescriptor<T> accessorDescriptor )
   {
     return bind( typeSafeQuery( MoreSelectors.number( new ReflectiveSelector<T, E>(
         accessorDescriptor.getClazz(), accessorDescriptor.getMethod() ) ),
         new EqualsConclusionPredicates<E>( argument.getArgumentClazz() ) ) );
   }
 
   /**
    *
    * @return FluentConclusionPredicate<T>
    * 
    */
   public <T, E extends Number & Comparable<? super E>> FluentConclusionPredicate<T> less(
       final Argument<E> argument, final AccessorDescriptor<T> accessorDescriptor )
   {
     return bind( typeSafeQuery( MoreSelectors.number( new ReflectiveSelector<T, E>(
         accessorDescriptor.getClazz(), accessorDescriptor.getMethod() ) ),
         new LessConclusionPredicate<E>( argument.getArgumentClazz() ) ) );
   }
 
   /**
    * 
    * @param <T>
    */
   private static final class EqualsConclusionPredicates<T extends Number & Comparable<? super T>>
       implements ConclusionPredicate<T>
   {
     private final T value;
     private T argument;
     private boolean result = false;
 
     private EqualsConclusionPredicates( T value )
     {
       this.value = checkNotNull( value );
     }
 
     @Override
     public boolean apply( T argument )
     {
       this.argument = checkNotNull( argument );
       this.result = argument.compareTo( value ) == 0;
       return result;
     }
 
     @Override
     public String toString()
     {
       return MessageFormat.format( "{0} ({1} $equals {2}) => {3} ", "EqualsConclusionPredicate",
           value, argument != null ? argument : "placeholder", result );
     }
   }
 
   /**
    * 
    * @author haghard
    * 
    * @param <T>
    */
   private static final class MoreConclusionPredicate<T extends Number & Comparable<? super T>>
       implements ConclusionPredicate<T>
   {
     private final T value;
     private T argument;
     private boolean result = false;
 
     private MoreConclusionPredicate( T value )
     {
       this.value = checkNotNull( value );
     }
 
     @Override
     public boolean apply( T argument )
     {
       this.argument = checkNotNull( argument );
       this.result = argument.compareTo( value ) > 0;
       return result;
     }
 
     @Override
     public String toString()
     {
       return MessageFormat.format( "{0} ({1} $more {2}) => {3} ", "MoreConclusionPredicate", value,
           argument != null ? argument : "placeholder", result );
     }
   }
 
   /**
    * 
    * 
    * @param <T>
    */
   private static final class LessConclusionPredicate<T extends Number & Comparable<? super T>>
       implements ConclusionPredicate<T>
   {
     private final T value;
     private T argument;
     private boolean result = false;
 
     LessConclusionPredicate( T value )
     {
       this.value = checkNotNull( value );
     }
 
     @Override
     public boolean apply( T argument )
     {
       this.argument = checkNotNull( argument );
       this.result = argument.compareTo( value ) > 0;
       return result;
     }
 
     @Override
     public String toString()
     {
       return MessageFormat.format( "{0} ({1} $less {2}) => {3} ", "LessConclusionPredicate", value,
           argument != null ? argument : "placeholder", result );
     }
   }
 
   // check for language adaptor
   private static JvmBasedLanguageAdapter findLanguageAdapter( Object closure )
   {
     for (final Class<?> c : languageAdaptors.keySet())
     {
       if ( c.isInstance( closure ) )
       {
         final JvmBasedLanguageAdapter la = languageAdaptors.get( c );
         return la;
       }
     }
     throw new RuntimeException( "Unsupported closure type: " + closure.getClass().getSimpleName() );
   }
 
   public static <T> ConclusionPredicate<T> toJavaPredicate( final Object predicate )
   {
     if ( predicate == null )
       throw new RuntimeException( "predicate is null. Can't send arguments to null predicate." );
     final JvmBasedLanguageAdapter la = findLanguageAdapter( predicate );
     return new ConclusionPredicate<T>()
     {
       @Override
       public boolean apply( Object argument )
       {
         Object result = la.call( predicate, new Object[] { argument } );
         if (result instanceof Boolean) {
             boolean res = (Boolean)result;
             return  res;
         }
         throw new IllegalArgumentException("result should be boolean");
       }
     };
   }
 
   public static <E, U extends Comparable<? super U>> Selector<E, U> toJavaSelector(
       final Object selector )
   {
     if ( selector == null )
       throw new RuntimeException( "selector is null. Can't send arguments to null selector." );
     final JvmBasedLanguageAdapter la = findLanguageAdapter( selector );
     return new Selector<E, U>()
     {
       @SuppressWarnings("unchecked")
       @Override
       public U select( Object argument )
       {
         return (U) la.call( selector, new Object[]
         { argument } );
       }
     };
   }
 }
