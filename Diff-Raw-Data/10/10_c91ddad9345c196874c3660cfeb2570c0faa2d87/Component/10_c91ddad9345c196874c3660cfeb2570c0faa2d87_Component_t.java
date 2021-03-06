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
 package org.apache.felix.scrplugin.om;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.felix.scrplugin.tags.JavaClassDescription;
 import org.apache.felix.scrplugin.tags.JavaMethod;
 import org.apache.felix.scrplugin.tags.JavaParameter;
 import org.apache.felix.scrplugin.tags.JavaTag;
 import org.apache.maven.plugin.MojoExecutionException;
 
 /**
  * <code>Component</code>
  * is a described component.
  *
  */
 public class Component extends AbstractObject {
 
     /** The name of the component. */
     protected String name;
 
     /** Is this component enabled? */
     protected Boolean enabled;
 
     /** Is this component immediately started. */
     protected Boolean immediate;
 
     /** The factory. */
     protected String factory;
 
     /** The implementation. */
     protected Implementation implementation;
 
     /** All properties. */
     protected List properties = new ArrayList();
 
     /** The corresponding service. */
     protected Service service;
 
     /** The references. */
     protected List references = new ArrayList();
 
     /** Is this an abstract description? */
     protected boolean isAbstract;
 
     /**
      * Default constructor.
      */
     public Component() {
         this(null);
     }
 
     /**
      * Constructor from java source.
      */
     public Component(JavaTag t) {
         super(t);
     }
 
     /**
      * Return the associated java class description
      */
     public JavaClassDescription getJavaClassDescription() {
         if ( this.tag != null ) {
             return this.tag.getJavaClassDescription();
         }
         return null;
     }
 
     /**
      * @return
      */
     public List getProperties() {
         return this.properties;
     }
 
     public void setProperties(List properties) {
         this.properties = properties;
     }
 
     public void addProperty(Property property) {
         this.properties.add(property);
     }
 
     public String getName() {
         return this.name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getFactory() {
         return this.factory;
     }
 
     public void setFactory(String factory) {
         this.factory = factory;
     }
 
     public Boolean isEnabled() {
         return this.enabled;
     }
 
     public void setEnabled(Boolean enabled) {
         this.enabled = enabled;
     }
 
     public Boolean isImmediate() {
         return this.immediate;
     }
 
     public void setImmediate(Boolean immediate) {
         this.immediate = immediate;
     }
 
     public Implementation getImplementation() {
         return this.implementation;
     }
 
     public void setImplementation(Implementation implementation) {
         this.implementation = implementation;
     }
 
     public Service getService() {
         return this.service;
     }
 
     public void setService(Service service) {
         this.service = service;
     }
 
     public List getReferences() {
         return this.references;
     }
 
     public void setReferences(List references) {
         this.references = references;
     }
 
     public void addReference(Reference ref) {
         this.references.add(ref);
     }
 
     public boolean isAbstract() {
         return this.isAbstract;
     }
 
     public void setAbstract(boolean isAbstract) {
         this.isAbstract = isAbstract;
     }
 
     /**
      * Validate the component description.
      * If errors occur a message is added to the issues list,
      * warnings can be added to the warnings list.
      */
     public void validate(List issues, List warnings)
     throws MojoExecutionException {
         final JavaClassDescription javaClass = this.tag.getJavaClassDescription();
         if (javaClass == null) {
             issues.add(this.getMessage("Tag not declared in a Java Class"));
         } else {
 
             // if the service is abstract, we do not validate everything
             if ( !this.isAbstract ) {
                 // ensure non-abstract, public class
                 if (!javaClass.isPublic()) {
                     issues.add(this.getMessage("Class must be public: " + javaClass.getName()));
                 }
                 if (javaClass.isAbstract() || javaClass.isInterface()) {
                     issues.add(this.getMessage("Class must be concrete class (not abstract or interface) : " + javaClass.getName()));
                 }
 
                 // no errors so far, let's continue
                 if ( issues.size() == 0 ) {
                     // check activate and deactivate methods
                     this.checkActivationMethod(javaClass, "activate", warnings);
                     this.checkActivationMethod(javaClass, "deactivate", warnings);
 
                     // ensure public default constructor
                     boolean constructorFound = true;
                     JavaMethod[] methods = javaClass.getMethods();
                     for (int i = 0; methods != null && i < methods.length; i++) {
                         if (methods[i].isConstructor()) {
                             // if public default, succeed
                             if (methods[i].isPublic()
                                 && (methods[i].getParameters() == null || methods[i].getParameters().length == 0)) {
                                 constructorFound = true;
                                 break;
                             }
 
                             // non-public/non-default constructor found, must have explicit
                             constructorFound = false;
                         }
                     }
                     if (!constructorFound) {
                         issues.add(this.getMessage("Class must have public default constructor: " + javaClass.getName()));
                     }
 
                     // verify properties
                     for (Iterator pi = this.getProperties().iterator(); pi.hasNext();) {
                         Property prop = (Property) pi.next();
                         prop.validate(issues, warnings);
                     }
 
                     // verify service
                     boolean isServiceFactory = false;
                     if (this.getService() != null) {
                        if ( this.getService().getInterfaces().size() == 0 ) {
                            issues.add(this.getMessage("Service interface information is missing for @scr.service tag"));
                        }
                         this.getService().validate(issues, warnings);
                         isServiceFactory = Boolean.valueOf(this.getService().getServicefactory()).booleanValue();
                     }
 
                     // serviceFactory must not be true for immediate of component factory
                     if (isServiceFactory && this.isImmediate() != null && this.isImmediate().booleanValue() && this.getFactory() != null) {
                         issues.add(this.getMessage("Component must not be a ServiceFactory, if immediate and/or component factory: " + javaClass.getName()));
                     }
 
                     // verify references
                     for (Iterator ri = this.getReferences().iterator(); ri.hasNext();) {
                         final Reference ref = (Reference) ri.next();
                         ref.validate(issues, warnings);
                     }
                 }
             }
         }
     }
 
     /**
      * Check methods.
      * @param javaClass
      * @param methodName
      */
     protected void checkActivationMethod(JavaClassDescription javaClass, String methodName, List warnings) {
         JavaMethod[] methods = javaClass.getMethods();
         JavaMethod activation = null;
         for (int i=0; i < methods.length; i++) {
             // ignore method not matching the name
             if (!methodName.equals(methods[i].getName())) {
                 continue;
             }
 
             // if the method has the correct parameter type, check protected
             JavaParameter[] params = methods[i].getParameters();
             if (params == null || params.length != 1) {
                 continue;
             }
 
             // this might be considered, if it is an overload, drop out of check
             if (activation != null) {
                 return;
             }
 
             // consider this method for further checks
             activation = methods[i];
         }
 
         // no activation method found
         if (activation == null) {
             return;
         }
 
         // check protected
         if (activation.isPublic()) {
             warnings.add(this.getMessage("Activation method " + activation.getName() + " should be declared protected"));
         } else if (!activation.isProtected()) {
             warnings.add(this.getMessage("Activation method " + activation.getName() + " has wrong qualifier, public or protected required"));
         }
 
         // check paramter (we know there is exactly one)
         JavaParameter param = activation.getParameters()[0];
         if (!"org.osgi.service.component.ComponentContext".equals(param.getType())) {
             warnings.add(this.getMessage("Activation method " + methodName + " has wrong argument type " + param.getType()));
         }
     }
 }
