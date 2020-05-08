 package com.wideplay.warp.util.beans;
 
 import com.wideplay.warp.util.TextTools;
 import org.mvel.MVEL;
 import org.mvel.PropertyAccessException;
 
 import java.io.Serializable;
 import static java.util.Collections.synchronizedMap;
 import java.util.Map;
 import java.util.WeakHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Callable;
 
 /**
  * Created with IntelliJ IDEA.
  * On: 25/03/2007
  *
  * @author Dhanji R. Prasanna (dhanji at gmail com)
  * @since 1.0
  */
 public class BeanUtils {
 
     public static final Object[] EMPTY_ARRAY = new Object[] { };
 
     //lets do some caching of expressions to see if we cant go a bit faster
     private static final ConcurrentMap<String, Serializable> compiledExpressions = new ConcurrentHashMap<String, Serializable>();
 
     public static Object getFromPropertyExpression(String expr, Object bean) {
         Serializable compiled = compiledExpressions.get(expr);
 
         //compile and store the expr (warms up the expression cache)
         if (null == compiled) {
             String preparedExpression = expr;
 
             //strip expression decorators as necessary
             if (TextTools.isExpression(expr)) {
                 preparedExpression = TextTools.stripExpression(expr);
             }
 
             //compile expression
             compiled = MVEL.compileExpression(preparedExpression);
 
             //place into map under original key (i.e. as it came in)
             compiledExpressions.put(expr, compiled);
         }
 
         //lets use mvel to retrieve an expression instead of a prop
         try {
             return MVEL.executeExpression(compiled, bean);
         } catch(PropertyAccessException e) {
             throw new NotReadablePropertyException(String.format("Could not read property from expression %s (missing a getter?)", expr), e);
         } catch(NullPointerException npe) {
             throw new NotReadablePropertyException(String.format("Evaluation of property expression [%s] resulted in a NullPointerException", expr), npe);            
         }
     }
 
     /**
      *
      * This method should never be called after setup as it will make the BeanUtils class unsafe to concurrency
      *
      * @param str A name
      * @param obj A value for the name
      */
     private static void setGlobalBeanContextVariable(String str, Object obj) {
 //        contextVars.put(str, obj);
     }
 
     public static void setProperty(String expr, Object bean, Object value) {
         //lets use mvel to store an expression
         MVEL.setProperty(bean, expr, value);
     }
 
     public static Object getProperty(String property, Object contextObject) {
         return MVEL.getProperty(property, contextObject);
     }
 }
