 package ru.rulex.conclusion.dagger;
 
 import java.util.ArrayDeque;
 import java.util.Queue;
 
 import com.google.common.collect.ImmutableMap;
 
 import ru.rulex.conclusion.AssertionUnit;
 import ru.rulex.conclusion.Selector;

 import ru.rulex.conclusion.dagger.AssertionUnits.IntExpression;
 import ru.rulex.conclusion.dagger.AssertionUnits.StringExpression;
 import ru.rulex.conclusion.dagger.AssertionUnits.FloatExpression;
 
 @SuppressWarnings("rawtypes")
 public enum SelectorKeeper implements Selector<Object, Object>
 {
   INSTANCE
   {
 
    private ImmutableMap<String, Class<? extends AssertionUnit>> map = ImmutableMap.of( "getInteger",
         IntExpression.class, "getOtherInteger", IntExpression.class, "getFloat", FloatExpression.class,
         "", StringExpression.class );
 
    private Queue<Class<? extends AssertionUnit>> classesQueue = new ArrayDeque<>();
 
    private Queue<Selector> selectorsQueue = new ArrayDeque<>();
    
     @Override
     public Object select( Object argument )
     {
        Selector selector = selectorsQueue.poll();
        return selector.select( argument );
     }
 
     @Override
     public <T, E> void setDelegate( Selector<T, E> delegate )
     {
       selectorsQueue.offer( delegate );
     }
 
     public Class<? extends AssertionUnit> getExpressionClass()
     {
       return classesQueue.poll();
     }
 
     @Override
     public void setExpressionClass( String methodName )
     {
       classesQueue.offer( map.get( methodName ) );
     }
   };
 
   public abstract <T, E> void setDelegate( Selector<T, E> delegate );
 
   public abstract Class<? extends AssertionUnit> getExpressionClass();
 
   public abstract void setExpressionClass( String methodName );
 
   <E, T> Selector<E, T> cast()
   {
     return (Selector<E, T>) this;
   }
 }
