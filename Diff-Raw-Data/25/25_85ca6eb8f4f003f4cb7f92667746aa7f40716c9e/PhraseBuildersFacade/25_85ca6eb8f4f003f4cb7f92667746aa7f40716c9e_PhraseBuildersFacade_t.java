 /*
  * Copyright (C) 2013 The Conclusions Authors
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file without in compliance with the License. You may obtain a copy
  * of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package ru.rulex.conclusion;
 
 import com.google.common.util.concurrent.CheckedFuture;
 import com.google.common.util.concurrent.ListeningExecutorService;
 import com.google.common.util.concurrent.MoreExecutors;
 import ru.rulex.conclusion.ParserBuilders.*;
 import ru.rulex.conclusion.execution.ParallelStrategy;
 import java.util.concurrent.*;
 import org.apache.log4j.Logger;

import static ru.rulex.conclusion.delegate.ProxyUtils.callOn;
 import static ru.rulex.conclusion.execution.Callables.*;
import static ru.rulex.conclusion.guice.AbstractPhrasesAnalyzerModule.$eq;
import static ru.rulex.conclusion.guice.AbstractPhrasesAnalyzerModule.$less;
 /** 
  * <pre>
  *                         <b> Classes hierarchy (single object oriented)</b>
  *                            __________________________________________                                  __________________      
  *                           |   AbstractEventOrientedPhrasesBuilder   |-------------------------------->|  AbstractPhrase |
  *                           |_________________________________________|                                 |_________________|  
  *                                             |                                                      ___________|____________
  *                          ___________________|______________________                               |                       |
  *                         |    AbstractEventOrientedBuilderImpl     |
  *                         |_________________________________________|
  *                                             |
  *                                             |
  *         ____________________________________|_____________________
  *        |                        |                                |
  *        |        ________________|____________________            |
  *        |       |   GuiceEventOrientedPhrasesBuilder |            |
  *        |       |____________________________________|            |
  *  ______|_______________________    ______________________________|_____________
  * | EventOrientedPhrasesBuilder |   | EventOrientedFactConsequencePhrasesBuilder|
  * |_____________________________|   |___________________________________________|
  * 
  *                             <b> Classes hierarchy (collection oriented)</b>
  *                             ___________________________________________                            ___________________
  *                            |   AbstractIterableOrientedPhrasesBuilder |-------------------------->| IterablePhrases  | 
  *                            |__________________________________________|                           |__________________|
  *                                                 |                                              ____________|_____________
  *                         ________________________|___________________                          |                         |
  *   _____________________|______________________________              |
  *  | AbstractConclusionCollectionExecutionPhrasesBuilder|             |
  *  |____________________________________________________|             |
  *                       |                                             |
  *                       |                                             |
  *                       |                                             |
  *                       |                       ______________________|____________________
  *                       |                      |AbstractImperativeExecutionPhrasesBuilder |
  *                       |                      |__________________________________________|
  *  _____________________|____________________                       |
  * |IterableValidationCollectionPhrasesBuilder|                      |
  * |__________________________________________|                      |
  *                                                    _______________|___________________
  *                                                   | ImperativeExecutionPhrasesBuilder|
  *                                                   |__________________________________|
  * </pre>
  */
 public final class PhraseBuildersFacade {
 
   private PhraseBuildersFacade() {
   }
 
   /**
    *
    * {@code AbstractEventOrientedPhrasesBuilder} class is a root of class
    * hierarchy define a base abstraction for single event oriented PhraseBuilders 
    * <p/>
    * This class extended for implementation specific behavior.
    * <p/>
    * <p>
    * <b>Note:</b> {@code AbstractEventOrientedPhrasesBuilder}'s class
    * hierarchy is a abstraction part of <b>pattern bridge </b>. 
    * Every subclass relies on a set of abstract operations, 
    * where several implementations of the set of abstract operations are possible. 
    * The implementation part (Bridge Pattern) is a {@code AbstractPhrase} hierarchy
    * <p/>
    * <p>
    * See wiki article on <a href=<a
    * href="http://en.wikipedia.org/wiki/Bridge_pattern"> Builder Design
    * Pattern</a>.
    *
    */
   public static abstract class AbstractEventOrientedPhrasesBuilder {
 
     protected ParallelStrategy<Boolean, PhraseExecutionException> pStrategy;
     /**
      * 
      * @param pStrategy execution strategy
      */
     protected void setParallelStrategy(
         ParallelStrategy<Boolean, PhraseExecutionException> pStrategy) {
       this.pStrategy = pStrategy;
     }
 
     /**
      * @return AbstractPhrase
      */
     protected abstract <T> AbstractPhrase<T> getPhrase();
 
     /**
      * Submits a event instance for execution and initiate an asynchronous
      * computation returns {@code ListenableFuture<Boolean>}
      * <p/>
      * The CheckedFuture <tt>getChecked</tt> method will return the task's
      * result upon successful completion in blocking way.
      * <p/>
      * The CheckedFuture <tt>addListener</tt> method will return the task's
      * result upon successful completion in asynch way.
      * 
      * @param event
      * @return CheckedFuture<Boolean> representing the pending results of the
      *         task.
      */
     public abstract <T> CheckedFuture<Boolean, PhraseExecutionException> async(
         final T event);
 
     /**
      * Always block current thread on {@code future.get()} 
      * until result was not received
      * 
      * @param event
      * @return Boolean
      */
     public abstract <T> Boolean sync(final T event);
 
     /**
      * Always block current thread on {@code future.get()} until result
      * was not received
      * 
      * @param event
      * @param callback
      * @return Boolean
      */
     public <T> Boolean sync(final T event, Runnable callback) {
       return sync(event, callback, MoreExecutors.sameThreadExecutor());
     }
 
     /**
      * Always block current thread on {@code future.get()} until result 
      * was not received
      * 
      * @param event
      * @param callback
      * @param callbackExecutor
      * @return Boolean
      */
     public abstract <T> Boolean sync(final T event, Runnable callback,
         Executor callbackExecutor);
 
     /**
      * 
      * 
      * @param event
      */
     protected abstract <T> void setEvent(T event);
 
     /**
      * 
      * @param event
      * @return ConclusionFunction
      * 
      */
     protected <T, E extends AbstractEventOrientedPhrasesBuilder> ConclusionFunction<E, Boolean> makePhraseFunction(
         final T event) {
       return new ConclusionFunction<E, Boolean>() {
         @Override
         public Boolean apply(E arg) {
           setEvent(event);
           return getPhrase().evaluate();
         }
       };
     }
   }
 
   /**
    * 
    * {@code AbstractEventOrientedBuilderImpl} abstract private class which
    * extends hierarchy single event oriented PhraseBuiler's. 
    * For instantiation use his subclasses.
    */
   private static abstract class AbstractEventOrientedBuilderImpl
       extends AbstractEventOrientedPhrasesBuilder {
     
     private static final Logger logger = Logger
         .getLogger(AbstractEventOrientedBuilderImpl.class);
     
     protected final AbstractPhrase<?> delegate;
 
     private <T> AbstractEventOrientedBuilderImpl(
         AbstractPhrase<T> delegate,
         ParallelStrategy<Boolean, PhraseExecutionException> pStrategy) {
       this.delegate = delegate;
       this.pStrategy = pStrategy;
     }
 
     @SuppressWarnings("unchecked")
     protected <T> AbstractPhrase<T> getPhrase() {
       return (AbstractPhrase<T>) delegate;
     }
 
     @Override
     public <T> CheckedFuture<Boolean, PhraseExecutionException> async(final T event) {
       return pStrategy.lift(makePhraseFunction(event)).apply(this);
     }
 
     @Override
     protected <T> void setEvent(T event) {
       getPhrase().setEvent(event);
     }
 
     @Override
     public <T> Boolean sync(final T event) {
       try {
         return call(obtain(pStrategy.lift(makePhraseFunction(event))
             .apply(this)));
       } catch (PhraseExecutionException e) {
         logger.error(e.getMessage());
       } catch (Exception e) {
         logger.error(e.getMessage());
       }
       return false;
     }
 
     @Override
     public <T> Boolean sync(final T event, Runnable callback,
         Executor callbackExecutor) {
       try {
         return call(obtain(pStrategy.lift(makePhraseFunction(event))
             .apply(this), callback, callbackExecutor));
       } catch (PhraseExecutionException ex) {
         logger.error(ex.getMessage());
       } catch (Exception ex) {
         logger.error(ex.getMessage());
       }
       return false;
     }
   }
 
   /**
    * <p>
    * The class used for creation, configuration and running evaluation on
    * manually created predicates. Main usage through anonymous inner class
    * instance.
    * </p>
    * 
    * <pre>
   * <b>Usage example in {@code TestEventOrientedPhraseBuilders}</b>
    * </pre>
    */
   public static abstract class EventOrientedPhrasesBuilder
       extends AbstractEventOrientedBuilderImpl {
 
     public <T> EventOrientedPhrasesBuilder() {
       super(Phrases.ALL_TRUE.withNarrowedType(),
           ParallelStrategy.<Boolean, PhraseExecutionException>serial());
       build();
     }
 
     /**
      * method which should be override in all anonymous class instances and
      * include logic for construction underline engine with predicates.
      */
     protected abstract void build();
 
     protected <T> WithParser<T> through(Class<T> clazz, String description) {
       return rule(
           ParallelStrategy.<Boolean, PhraseExecutionException> serial(),
           clazz, description);
     }
 
     protected <T> WithParser<T> rule(
         ParallelStrategy<Boolean, PhraseExecutionException> pStrategy,
         Class<T> clazz, String description) {
       setParallelStrategy(pStrategy);
       return ParserBuilders.newWithParser(this.<T> getPhrase(),
           clazz, description);
     }
   }
 
   /**
    *
    * <pre>
   * <b>Usage example in {@code TestEventOrientedPhraseBuilders}</b>
    * </pre>
    *
    */
   public static abstract class EventOrientedFactConsequencePhrasesBuilder
       extends AbstractEventOrientedBuilderImpl {
 
     public <T> EventOrientedFactConsequencePhrasesBuilder() {
       super(Phrases.ALL_TRUE.withNarrowedType(),
           ParallelStrategy.<Boolean, PhraseExecutionException>serial());
       build();
     }
 
     /**
      * method which should be override in all anonymous subclasses and
      * include logic for construction underline phrase with predicates.
      */
     protected abstract void build();
     
     protected <T> FactConsequenceParser<T> through(
         ParallelStrategy<Boolean, PhraseExecutionException> pStrategy,
         Class<T> clazz, String description) {
       setParallelStrategy(pStrategy);
       return rule(clazz, description);
     }
     
     protected <T> FactConsequenceParser<T> rule(
         Class<T> clazz, String description) {
       return ParserBuilders.newFactConsequenceParser(
           this.<T> getPhrase(), clazz, description);
     }
   }
 
   /**
    * <p>
    * The class used for creation, configuration and running evaluation using 
    * Guice managed predicates from {@code GuicefyConclusionPredicates}. 
    * Main usage through
   * {@code ru.rulex.conclusion.guice.AbstractPhrasesAnalyzerModule}
    * class static methods.
    * </p>
    * <p/>
    * <pre>
    * <b>Usage example: </b>
    * {@code 
    * Injector injector = Guice.createInjector(
    *    $expression(Phrases.ANY_TRUE,
   *      $less(9, callOn(Model.class).getInteger(), "9 < en.getInput()"),
   *      $eq("aaaaaaa", callOn(Model.class).getString(), "aaaaaaa eq en.getString()")));
    *
    * final AbstractEventOrientedPhrasesBuilder phraseBuilder = 
    *       injector.getInstance(AbstractEventOrientedPhrasesBuilder.class);
    * 
    * Boolean result = phraseBuilder.async(andFoo).checkedGet();            
    * }
    * </pre>
    * 
   * <b>More examples in {@code GuiceEventOrientedEngineModuleTest} </b>
    * 
    * See Google Guice documentation on <a href=<a
    * href="http://code.google.com/p/google-guice/"> Google Guice DI framework </a>.
    * 
    * <b> Attention !!! </b> Other usages of this class are prohibited
    */
   public static class GuiceEventOrientedPhrasesBuilder extends
       AbstractEventOrientedBuilderImpl {
 
     public <T> GuiceEventOrientedPhrasesBuilder(AbstractPhrase<T> delegate) {
       super(delegate, 
           ParallelStrategy.<Boolean, PhraseExecutionException>serial());
     }
   }
 
   /**
    * {@code AbstractIterableOrientedPhrasesBuilder} class is a root of class
    * hierarchy define a base abstraction for collection oriented PhraseBuilders
    * <p/>
    * <b>Note:</b> {@code AbstractIterableOrientedPhrasesBuilder}'s class
    * hierarchy is a abstraction part of <b>pattern bridge </b>. 
    * Every subclass relies on a set of abstract operations, 
    * where several implementations of the set of abstract operations are possible. 
    * The implementation part (Bridge Pattern) is a {@code IterablePhrases} hierarchy
    * <p>
    * See wiki article on <a href=<a
    * href="http://en.wikipedia.org/wiki/Bridge_pattern"> Builder Design
    * Pattern</a>.
    * <p/>
    * Class similar to {@code AbstractEventOrientedPhrasesBuilder} by
    * functionality, but used for evaluation on Iterable.
    *
    *
    * @see PhraseBuildersFacade.IterableOrientedPhrasesBuilder
    * @see PhraseBuildersFacade.ImperativeIterableOrientedPhrasesBuilder
    *
    */
   public static abstract class AbstractIterableOrientedPhrasesBuilder {
 
     protected ParallelStrategy<Boolean, PhraseExecutionException> pStrategy;
     
     protected void setParallelStrategy(
         ParallelStrategy<Boolean, PhraseExecutionException> pStrategy) {
       this.pStrategy = pStrategy;
     }
     
     /**
      * Submits a event instance for execution and initiate an asynchronous
      * computation returns {@code ListenableFuture<Boolean>}
      * <p/>
      * The CheckedFuture's <tt>getChecked</tt> method will return the task's result
      * upon successful completion in blocking way.
      * <p/>
      * The CheckedFuture's <tt>addListener</tt> method will return the task's
      * result upon successful completion in async way.
      * 
      * @param <T>
      * @return CheckedFuture<Boolean> representing the pending results of the
      *         task.
      */
     public abstract <T> CheckedFuture<Boolean, PhraseExecutionException> async(final Iterable<T> collection);
 
     /**
      * 
      * @param collection
      */
     public abstract <T> void setIterable(Iterable<T> collection);
 
     /**
      * 
      * @param iterable
      * @return
      */
     public abstract <T> Boolean sync(final Iterable<T> iterable);
 
     /**
      * 
      * @param iterable
      * @param callback
      * @return
      */
     public <T> Boolean sync(final Iterable<T> iterable, Runnable callback) {
       return sync(iterable, callback, MoreExecutors.sameThreadExecutor());
     }
 
     /**
      * 
      * @param iterable
      * @param callback
      * @param service
      * @return Boolean
      */
     public abstract <T> Boolean sync(final Iterable<T> collection, Runnable callback, ListeningExecutorService service);
 
     /**
      * @return AbstractIterablePhrases
      */
     protected abstract <T> IterablePhrases<T> getIterablePhrase();
 
     /**
      * 
      * @param iterable
      * @return ConclusionFunction<? extends AbstractConclusionCollectionPhrasesBuilder, Boolean>
      */
     protected <T, E extends AbstractIterableOrientedPhrasesBuilder> ConclusionFunction<E, Boolean> makePhraseFunction(
         final Iterable<T> iterable) {
       return new ConclusionFunction<E, Boolean>() {
         @Override
         public Boolean apply(E arg) {
           setIterable(iterable);
           return getIterablePhrase().evaluate();
         }
       };
     }
   }
   /**
    * {@code AbstractIterableOrientedPhrasesBuilderImpl} abstract class
    * which extends hierarchy for iterable oriented PhraseBuilders 
    * <p/>
    * <p>
    * <b>Note:</b> This class cannot be instantiated directly. 
    * It's just implements main behavior
    * <p/>
    * For instantiation use subclasses.
    * 
    */
   private abstract static class AbstractIterableOrientedPhrasesBuilderImpl
       extends AbstractIterableOrientedPhrasesBuilder {
 
     protected final IterablePhrases<?> delegate;
 
     private static final Logger logger =
         Logger.getLogger(AbstractIterableOrientedPhrasesBuilderImpl.class);
 
     @Override
     @SuppressWarnings("unchecked")
     public <T> void setIterable(Iterable<T> collection) {
       getIterablePhrase().setIterable((Iterable<Object>) collection);
     }
 
     private <T> AbstractIterableOrientedPhrasesBuilderImpl(
         IterablePhrases<T> delegate,
         ParallelStrategy<Boolean, PhraseExecutionException> pStrategy) {
       this.delegate = delegate;
       this.pStrategy = pStrategy;
     }
 
     @Override
     public <T> CheckedFuture<Boolean, PhraseExecutionException> async(final Iterable<T> iterable) {
       return pStrategy.lift(makePhraseFunction(iterable)).apply(this);
     }
 
     public <T> Boolean sync(final Iterable<T> iterable) {
       try {
         return call(obtain(pStrategy.lift(
             makePhraseFunction(iterable)).apply(this)));
       } catch (PhraseExecutionException e) {
         logger.error(e.getMessage());
       } catch (Exception e) {
         logger.error(e.getMessage());
       }
       return false;
     }
 
     @Override
     public <T> Boolean sync(final Iterable<T> iterable, Runnable callback, ListeningExecutorService service) {
       try {
         return call(obtain(pStrategy.lift(makePhraseFunction(iterable))
             .apply(this), callback, service));
       } catch (PhraseExecutionException e) {
         logger.error(e.getMessage());
       } catch (Exception e) {
         logger.error(e.getMessage());
       }
       return false;
     }
   }
 
   /**
    * <p>
    * Go through Iterate, except defined and and stop after first success.
    * <p>
    * This class to direct usage through anonymous inner class instance in 
    * client code
    * </p>
    * <pre>
    * See usage in {@code IterablePhrasesBuilderTest}
    * 
    * Class for extending on client side
    */
   public static abstract class IterableOrientedPhrasesBuilder
       extends AbstractIterableOrientedPhrasesBuilderImpl {
 
     public IterableOrientedPhrasesBuilder() {
       super(AbstractIterablePhrases.defaultInstance(),
           ParallelStrategy.<Boolean, PhraseExecutionException>serial());
       build();
     }
     
     @Override
     @SuppressWarnings("unchecked")
     protected <T> AbstractIterablePhrases<T> getIterablePhrase() {
       return (AbstractIterablePhrases<T>) delegate;
     }
 
     /**
      * method which should be override in all anonymous subclasses and
      * include logic for construction underline phrase with predicates.
      */
     protected abstract void build();
 
     protected <T> IterableParser<T> through(Class<T> clazz, String description) {
       return new IterableParserBuilder<T>(
         this.<T> getIterablePhrase(), clazz, description);
     }
       
     protected <T> IterableParser<T> through(ParallelStrategy<Boolean, PhraseExecutionException> pStrategy,
           Class<T> clazz, String description) {
       setParallelStrategy(pStrategy);
       return through(clazz, description);  
     }
   }
 
   /**
    *
    * Imperative way for evaluation :
    * <p>
    * We define and pass to the {@code ImperativeExecutionPhrasesBuilder} 
    * two instances 
    * <p>
    * 1. {@code ConclusionPredicate<Delegate<FluentConclusionPredicate>> } - 
    * Simple or complex predicate with conditional logic 
    * <p>  
    * 2. {@code Delegate<FluentConclusionPredicate> } delegate, 
    * this is the place where in runtime instance of FluentConclusionPredicate will be passed to
    * in {@code Delegate<T>.execute(T... args)} method.
    * This give you opportunity to define evaluation way based on your FluentConclusionPredicate.
    * <p>
    * <pre>
    * <b> Simple example: </b>
    * {@code
    *  final Delegate<FluentConclusionPredicate> targetDelegate = new Delegate<FluentConclusionPredicate>() {
    *   final Iterable<Object> collection;
    *
    *   public void setIterable(Iterable<?> collection) {
    *     this.collection = ImmutableList.copyOf(collection);
    *   }
    *
    *   public boolean execute(FluentConclusionPredicate... fluentPredicate) {
    *     Preconditions.checkArgument(fluentPredicate[0] != null, "null FluentConclusionPredicate");
    *     for (Object object : collection) {
    *       if (fluentPredicate[0].apply(object)) {
    *         return true;
    *       }
    *     }
    *     return false;
    *   }
    * }; 
    *}  
    *</pre>
    * {@code Delegate<T>execute(FluentConclusionPredicate...)} is a varargs method, you can pass complete FluentConclusionPredicate 
    * <p>  
    * {@code
    *  final ConclusionPredicate<Delegate<FluentConclusionPredicate>> startEndPredicates = 
    *     DelegateFactory.<FluentConclusionPredicate>values(
    *         fluent().eq(4, Entity.class, Integer.class, "getInteger"), 
    *         fluent().eq(5, Entity.class, Integer.class, "getInteger")
    *           .or(fluent().eq(55, Entity.class, Integer.class, "getInt")));
    * 
    * final Delegate<FluentConclusionPredicate> targetDelegate = new Delegate<FluentConclusionPredicate>() {
    *   Iterable<Object> collection;
    *   public void setContent(Iterable<?> collection) {
    *     this.collection = ImmutableList.copyOf(collection);
    *   }
    *   
    *   public boolean execute(FluentConclusionPredicate... arguments) {
    *     assertTrue(collection.size() == 4);
    *     assertTrue(arguments.length == 2);
    *     return arguments[0].apply(collection.get(0)) && 
    *         arguments[1].apply(collection.get(collection.size()-1));
    *   }
    * };
    * }
    * </p>
   *  See usage in {@code TestImperativeIterableOrientedPhrasesBuilder}
    * <b>Class for extending on client side </b>
    */
   //TO DO : replace Delegate.execute(T...) varargs method with typesafe variation base on BiMap for example
   public static abstract class ImperativeIterableOrientedPhrasesBuilder
             extends AbstractIterableOrientedPhrasesBuilderImpl {
 
     public <T> ImperativeIterableOrientedPhrasesBuilder() {
       super(AbstractImperativePhrases.delegatePhrases(),
           ParallelStrategy.<Boolean, PhraseExecutionException>serial());
       build();
     }
 
     /**
      * method which should be override in all anonymous class instances and
      * include logic for construction underline engine with predicates.
      */
     protected abstract void build();
     
     @SuppressWarnings("unchecked")
     protected <T> AbstractImperativePhrases<T> getIterablePhrase() {
       return (AbstractImperativePhrases<T>) delegate;
     }
     
     protected <T, E> DelegateParser<T, E> rule(ParallelStrategy<Boolean, PhraseExecutionException> pStrategy,
         Class<T> clazz, Class<E> argClass, String description) {
       setParallelStrategy(pStrategy);
       return through(clazz, argClass, description);
     }
     
     protected <T, E> DelegateParser<T, E> through(Class<T> clazz,
         Class<E> argClass, String description) {
       return ParserBuilders.newDelegateParser(
           this.<T> getIterablePhrase(), clazz, description);
     }
   }
 }
