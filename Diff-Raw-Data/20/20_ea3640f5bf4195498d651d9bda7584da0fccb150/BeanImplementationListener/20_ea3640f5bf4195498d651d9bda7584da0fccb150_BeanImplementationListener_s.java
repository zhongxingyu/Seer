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
 package org.apache.myfaces.scripting.jsf2.annotation;
 
 import org.apache.myfaces.config.RuntimeConfig;
 import org.apache.myfaces.config.impl.digester.elements.ManagedBean;
 
 import javax.faces.context.FacesContext;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Collections;
 
 import com.thoughtworks.qdox.model.JavaClass;
 import com.thoughtworks.qdox.model.JavaField;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  *          <p/>
  *          bean implementation listener which registers new java sources
  *          into the runtime config, note this class is not thread safe
  *          it is only allowed to be called from a single thread
  */
 
 public class BeanImplementationListener implements SourceClassAnnotationListener {
 
     static Map<String, ManagedBean> _alreadyRegistered = new HashMap<String, ManagedBean>();
 
     public boolean supportsAnnotation(Class annotation) {
         return annotation.equals(javax.faces.bean.ManagedBean.class);
     }
 
 
     public boolean hasToReregister(String name, JavaClass clazz) {
         return !_alreadyRegistered.containsKey(name);
     }
 
 
     /**
      * reregistration strategy:
      * <p/>
      * managed properties have changed
      * or class has changed
      * or class does not exist at all
      *
      * @param clazz
      * @param annotationName
      * @param params
      */
     public void register(JavaClass clazz, String annotationName, Map<String, String> params) {
         RuntimeConfig config = getRuntimeConfig();
 
         String beanName = params.get("name");
         if (!hasToReregister(beanName, clazz)) {
             return;
         }
 
         ManagedBean mbean = new ManagedBean();
         mbean.setBeanClass(clazz.getName());
         mbean.setName(beanName);
 
         _alreadyRegistered.put(beanName, mbean);
 
         config.addManagedBean(beanName, mbean);
     }
 
 
     protected RuntimeConfig getRuntimeConfig() {
         final FacesContext facesContext = FacesContext.getCurrentInstance();
         return RuntimeConfig.getCurrentInstance(facesContext.getExternalContext());
     }
 
     /*
             for (Field field : fields)
            {
                if (log.isTraceEnabled())
                {
                    log.trace("  Scanning field '" + field.getName() + "'");
                }
                javax.faces.bean.ManagedProperty property = (javax.faces.bean.ManagedProperty) field
                        .getAnnotation(javax.faces.bean.ManagedProperty.class);
                if (property != null)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("  Field '" + field.getName()
                                + "' has a @ManagedProperty annotation");
                    }
                    org.apache.myfaces.config.impl.digester.elements.ManagedProperty mpc =
                        new org.apache.myfaces.config.impl.digester.elements.ManagedProperty();
                    String name = property.name();
                    if ((name == null) || "".equals(name))
                    {
                        name = field.getName();
                    }
                    mpc.setPropertyName(name);
                    mpc.setPropertyClass(field.getType().getName()); // FIXME - primitives, arrays, etc.
                    mpc.setValue(property.value());
                    mbc.addProperty(mpc);
                    continue;
                }
            }
 
     */
 
     /**
      * <p>Return an array of all <code>Field</code>s reflecting declared
      * fields in this class, or in any superclass other than
      * <code>java.lang.Object</code>.</p>
      *
      * @param clazz Class to be analyzed
      */
 
     private JavaField[] fields(JavaClass clazz) {
 
         Map<String, JavaField> fields = new HashMap<String, JavaField>();
         do {
             for (JavaField field : clazz.getFields()) {
                 if (!fields.containsKey(field.getName())) {
                     fields.put(field.getName(), field);
                 }
             }
         } while ((clazz = clazz.getSuperJavaClass()) != null && !clazz.getName().equals("java.lang.Object"));
         return (JavaField[]) fields.values().toArray(new JavaField[fields.size()]);
 
     }
 }
