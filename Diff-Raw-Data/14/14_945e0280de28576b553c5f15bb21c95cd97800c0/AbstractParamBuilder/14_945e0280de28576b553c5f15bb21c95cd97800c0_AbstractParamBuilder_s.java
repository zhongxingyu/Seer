 package com.tacitknowledge.noexcuses;
 
 /**
  * Created by IntelliJ IDEA.
  * User: mshort
  * Date: Dec 21, 2006
  * Time: 4:58:07 PM
  * To change this template use File | Settings | File Templates.
  */
abstract public class AbstractParamBuilder implements com.tacitknowledge.noexcuses.ParamBuilder
 {
     public Object[] createParams(Class<?>[] paramTypes)
     {
         Object[] params = new Object[paramTypes.length];
         for (int i = 0; i < params.length; i++)
         {
             params[i] = createObject(paramTypes[i]);
         }
         return params;
     }
 
     abstract protected <T> T createObject(Class<T> paramType);
 }
