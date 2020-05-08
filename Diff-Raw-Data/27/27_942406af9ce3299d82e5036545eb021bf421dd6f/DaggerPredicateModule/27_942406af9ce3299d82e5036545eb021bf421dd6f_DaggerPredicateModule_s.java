 package ru.rulex.conclusion.dagger;
 
 import dagger.Module;
 import dagger.Provides;
 import ru.rulex.conclusion.AssertionUnit;
 import ru.rulex.conclusion.Selector;
 import com.google.common.collect.ImmutableMap;
 import ru.rulex.conclusion.ConclusionPredicate;
 import ru.rulex.conclusion.dagger.AssertionUnits.IntExpression;
 import ru.rulex.conclusion.dagger.AssertionUnits.FloatExpression;
import ru.rulex.conclusion.dagger.AssertionUnits.StringExpression;
 import ru.rulex.conclusion.dagger.PredicateCreators.PredicateCreator;
 import ru.rulex.conclusion.dagger.PredicateCreators.LessPredicateCreator;
 import ru.rulex.conclusion.dagger.PredicateCreators.MorePredicateCreator;
 import ru.rulex.conclusion.dagger.PredicateCreators.MoreOrEqualsPredicateCreator;
 import ru.rulex.conclusion.dagger.PredicateCreators.LessOrEqualsPredicateCreator;
 import static ru.rulex.conclusion.delegate.ProxyUtils.callOn;
 
 
 //StringExpression.class 
 @Module(
     injects = { IntExpression.class, FloatExpression.class},
     library = true)
 @SuppressWarnings("rawtypes")
 public class DaggerPredicateModule
 {
   private static ImmutableMap<LogicOperation, PredicateCreator<?>> map;
  private final ValueSuppler<Comparable<?>> suppler = Generefier.INSTANCE.generify();
   private PredicateCreator builder;
   private Comparable<?> value;
   private SelectorKeeper selector;
 
   static
   {
     ImmutableMap.Builder<LogicOperation, PredicateCreator<?>> mapBuilder = ImmutableMap.builder();
     mapBuilder.put( LogicOperation.lessThan, builderInstance( LogicOperation.lessThan ) );
     mapBuilder.put( LogicOperation.moreThan, builderInstance( LogicOperation.moreThan ) );
     mapBuilder.put( LogicOperation.lessOrEquals, builderInstance( LogicOperation.lessOrEquals ) );
     mapBuilder.put( LogicOperation.moreOrEquals, builderInstance( LogicOperation.moreOrEquals ) );
     map = mapBuilder.build();
   }
   
   @SuppressWarnings("unchecked")
   static <T extends Comparable<? super T>, E extends PredicateCreator<T>> E builderInstance(
       LogicOperation operation )
   {
     switch (operation)
     {
     case lessThan:
       return (E) new LessPredicateCreator<T>();
     case moreThan:
       return (E) new MorePredicateCreator<T>();
     case moreOrEquals:
       return (E) new MoreOrEqualsPredicateCreator<T>();
     case lessOrEquals:
       return (E) new LessOrEqualsPredicateCreator<T>();
     default:
       throw new IllegalArgumentException( "Invalid operation" );
     }
   }
 
   protected <T extends Comparable<? super T>> DaggerPredicateModule( T value, PredicateCreator<?> builder, 
       SelectorKeeper selector )
   {
     this.value = value;
     this.builder = builder;
     this.selector = selector;
   }
 
   public <T extends Comparable<? super T>> DaggerPredicateModule( T value, SelectorKeeper selector,
       LogicOperation operation )
   {
     this( value, map.get( operation ), selector );
   }
 
   @Provides Selector<Object, Integer> iSelector()
   {
     return selector.cast(); 
   }
 
   @Provides Selector<Object, Float> fSelector()
   {
     return selector.cast(); 
   }
 
   @Provides
   @SuppressWarnings({"unchecked" })
   ConclusionPredicate<Integer> intPredicate()
   {
     //proxy object with intercepted toString()
     final ConclusionPredicate<Integer> conclusionPredicate = callOn( ConclusionPredicate.class,
         ConclusionPredicate.class.cast(builder.createPredicate(suppler.supply(value))));
     return conclusionPredicate;
   }
 
   @Provides
   @SuppressWarnings({"unchecked" })
   ConclusionPredicate<Float> floatPredicate()
   {
     final ConclusionPredicate<Float> conclusionPredicate = callOn( ConclusionPredicate.class,
         ConclusionPredicate.class.cast(builder.createPredicate(suppler.supply(value))));
     return conclusionPredicate;
   }
 
   public Class<? extends AssertionUnit> getExpressionClass()
   {
     return selector.getExpressionClass();
   }
 }
