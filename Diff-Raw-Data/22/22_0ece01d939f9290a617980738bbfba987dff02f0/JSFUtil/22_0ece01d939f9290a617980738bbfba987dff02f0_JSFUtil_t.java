 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.javaloader.blog;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 import javax.faces.context.FacesContext;
 import java.lang.reflect.Method;
 import java.lang.reflect.InvocationTargetException;
 
 import static org.apache.myfaces.scripting.core.util.ReflectUtil.*;
 import static org.apache.myfaces.scripting.core.util.ClassUtils.*;
 /**
  * @author werpu2
  * @date: 01.09.2009
  * <p/>
  * A helper for JSF and introspection related tasks
  */
 public class JSFUtil {
 
     public JSFUtil() {
     }
 
     /**
      * resolves a variable in the current facesContext
      *
      * @param beanName
      * @return
      */
     public static Object resolveVariable(String beanName) {
         Log log = LogFactory.getLog(JSFUtil.class);
         Object facesContext = FacesContext.getCurrentInstance();
 
        Object elContext = executeMethod(facesContext, "getELContext");
        Object elResolver = executeMethod(elContext, "getELResolver");
 
 
             /*
              if you want to enable this then use
              org.apache.myfaces.scripting.java.JAR_PATHS
              pointing towards the lingering jars
              The compiler cannot pick up the implicit containers classpaths
 
 
 
             */
             //we use the introspection calls here to achieve our goal that way
             //we can shift the dependency resolution from compile time to runtime
            return executeMethod(elResolver, "getValue",  elContext, null,  beanName);
             // return FacesContext.getCurrentInstance().getELContext().getELResolver().getValue(FacesContext.getCurrentInstance().getELContext(), null, beanName);
 
 
 
     }
 
 }
