 /*
  * JAHM - Java Advanced Hierarchical Model 
  * 
  * FunctionFactory.java
  * 
  * Copyright 2009 Robert Arvin Dunnagan
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.xmodel.xpath.function;
 
 import java.util.Hashtable;
 import org.xmodel.xpath.expression.IExpression;
 import org.xmodel.xpath.function.custom.*;
 
 import static java.lang.System.err;
 
 /**
  * The default IFunctionFactory implementation which will generate all of the
  * X-Path 1.0 function objects except local-name() and namespace-uri().
  */
 public class FunctionFactory implements IFunctionFactory
 {
   protected FunctionFactory()
   {
     addBaseFunctions();
   }
   
   @SuppressWarnings("rawtypes")
   private void addBaseFunctions()
   {
     Class[] classes = {
       AvgFunction.class,
       BooleanFunction.class,
       CeilingFunction.class,
       CollectionFunction.class,
       ConcatFunction.class,
       ContainsFunction.class,
       CountFunction.class,
       CreatePathFunction.class,
       DeepEqualFunction.class,
       DereferenceFunction.class,
       DistinctValuesFunction.class,
       DocFunction.class,
       EmptyFunction.class,
       EvaluateFunction.class,
       FalseFunction.class,
       FloorFunction.class,
       FormatFunction.class,
       IDFunction.class,
       HashCodeFunction.class,
       IndexOfFunction.class,
       LastFunction.class,
       LowercaseFunction.class,
       MatchesFunction.class,
       MinFunction.class,
       MaxFunction.class,
       NotFunction.class,
       NumberFunction.class,
       NameFunction.class,
       NosyncFunction.class,
       ParseXmlFunction.class,
       PositionFunction.class,
       PrintfFunction.class,
       ReplaceFunction.class,
       ReverseFunction.class,
       RoundFunction.class,
       SortFunction.class,
       StartsWithFunction.class,
       StaticFunction.class,
       StringFunction.class,
       StringJoinFunction.class,
       StringLengthFunction.class,
       SubstringAfterFunction.class,
       SubstringBeforeFunction.class,
       SubstringFunction.class,
      SumFunction.class,
      SumMulFunction.class,
       TranslateFunction.class,
       TraceFunction.class,
       TrueFunction.class,
       UppercaseFunction.class
     };
     
     classRegistry = new Hashtable<String, Class<?>>();
     for ( int i=0; i<classes.length; i++)
     {
       try
       {
         IExpression function = (IExpression)classes[ i].newInstance();
         classRegistry.put( function.getName(), classes[ i]);
         classRegistry.put( "fn:"+function.getName(), classes[ i]);
       }
       catch( Exception e)
       {
         err.println( e);
       }
     }
     
     objectRegistry = new Hashtable<String, Function>();
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.xpath.function.IFunctionFactory#register(java.lang.String, java.lang.Class)
    */
   public void register( String functionName, Class<? extends Function> clss)
   {
     objectRegistry.remove( functionName);
     classRegistry.put( functionName, clss);
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.function.IFunctionFactory#register(java.lang.String, org.xmodel.xpath.function.Function)
    */
   public void register( String functionName, Function prototype)
   {
     classRegistry.remove( functionName);
     objectRegistry.put( functionName, prototype);
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.function.IFunctionFactory#createFunction(java.lang.String)
    */
   @SuppressWarnings("rawtypes")
   public IExpression createFunction( String functionName)
   {
     // trim default namespace
     if ( functionName.startsWith( "fn:"))
       functionName = functionName.substring( 3);
     
     try
     {
       // create from registered class
       Class functionClass = classRegistry.get( functionName);
       if ( functionClass != null) return (IExpression)functionClass.newInstance();
       
       // create from registered prototype
       Function function = objectRegistry.get( functionName);
       if ( function != null) return (IExpression)function.clone();
       
       return null;
     }
     catch( InstantiationException e)
     {
       err.println( e);
       return null;
     }
     catch( IllegalAccessException e)
     {
       err.println( e);
       return null;
     }
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
     StringBuilder builder = new StringBuilder();
     for( String key: classRegistry.keySet())
     {
       builder.append( key);
       builder.append( " = ");
       builder.append( classRegistry.get( key));
       builder.append( '\n');
     }
 
     for( String key: objectRegistry.keySet())
     {
       builder.append( key);
       builder.append( " = ");
       builder.append( objectRegistry.get( key));
       builder.append( '\n');
     }
     
     return builder.toString();
   }
 
   /**
    * Returns the singleton instance.
    * @return Returns the singleton instance.
    */
   static public FunctionFactory getInstance()
   {
 //    FunctionFactory factory = instance.get();
 //    if ( factory == null) 
 //    {
 //      factory = new FunctionFactory();
 //      factory.addBaseFunctions();
 //      instance.set( factory);
 //    }
 //    return factory;
 
     if ( instance == null) instance = new FunctionFactory();
     return instance;
   }
   
   //private static ThreadLocal<FunctionFactory> instance = new ThreadLocal<FunctionFactory>();
   private static FunctionFactory instance;
   private Hashtable<String, Function> objectRegistry;
   private Hashtable<String, Class<?>> classRegistry;
 }
