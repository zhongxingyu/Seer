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
 import org.apache.myfaces.scripting.api.AnnotationScanListener;
 
 import javax.faces.context.FacesContext;
 import java.util.Map;
 import java.util.HashMap;
 
 import com.thoughtworks.qdox.model.JavaClass;
 import com.thoughtworks.qdox.model.JavaField;
 import com.thoughtworks.qdox.model.Annotation;
 import com.thoughtworks.qdox.model.annotation.AnnotationConstant;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  *          <p/>
  *          bean implementation listener which registers new java sources
  *          into the runtime config, note this class is not thread safe
  *          it is only allowed to be called from a single thread
  */
 
 public class BeanImplementationListener implements AnnotationScanListener {
 
     static Map<String, ManagedBean> _alreadyRegistered = new HashMap<String, ManagedBean>();
 
     public boolean supportsAnnotation(String annotation) {
         return annotation.equals(javax.faces.bean.ManagedBean.class.getName());
     }
 
 
     /**
      * simple check we do not check for the contents of the managed property here
      * This is somewhat a simplification does not drag down the managed property handling
      * speed too much
      * <p/>
      * TODO we have to find a way to enable the checking on managed property level
      * so that we can replace the meta data on the fly (probably by extending the interface)
      * for first registration this is enough
      *
      * @param name
      * @param clazz
      * @return
      */
     public boolean hasToReregister(String name, JavaClass clazz) {
         ManagedBean mbean = _alreadyRegistered.get(name);
         return mbean == null || !mbean.getManagedBeanClassName().equals(clazz.getName());
     }
 
 
     public void register(Class clazz, String annotationName, Map<String, Object> params) {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
 
     /**
      * reregistration strategy:
      * <p/>
      * managed properties have changed
      * or class has changed
      * or class does not exist at all
      *
      * @param sourceClass
      * @param annotationName
      * @param params
      */
     public void registerSource(Object sourceClass, String annotationName, Map<String, Object> params) {
         JavaClass clazz = (JavaClass) sourceClass;
 
         RuntimeConfig config = getRuntimeConfig();
 
         AnnotationConstant propVal = (AnnotationConstant) params.get("name");
         String beanName = (String) propVal.getParameterValue();
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
 
 
     private void handleManagedproperties(ManagedBean mbean, JavaField[] fields) {
         for (JavaField field : fields) {
             Annotation[] annotations = field.getAnnotations();
             if (annotations != null && annotations.length > 0) {
                 for (Annotation ann : annotations) {
                     if (ann.getType().getValue().equals(javax.faces.bean.ManagedProperty.class.getName())) {
                         //TODO implement meta handling
                         org.apache.myfaces.config.impl.digester.elements.ManagedProperty managedProperty =
                                 new org.apache.myfaces.config.impl.digester.elements.ManagedProperty();
                         String name = (String) ann.getPropertyMap().get("name");
                         if ((name == null) || "".equals(name)) {
                             name = field.getName();
                         }
                         managedProperty.setPropertyName(name);
                         managedProperty.setPropertyClass(field.getType().getValue()); // FIXME - primitives, arrays, etc.
                         managedProperty.setValue((String) ann.getPropertyMap().get("value"));
                         mbean.addProperty(managedProperty);
                     }
                 }
             }
         }
     }
 
 
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
