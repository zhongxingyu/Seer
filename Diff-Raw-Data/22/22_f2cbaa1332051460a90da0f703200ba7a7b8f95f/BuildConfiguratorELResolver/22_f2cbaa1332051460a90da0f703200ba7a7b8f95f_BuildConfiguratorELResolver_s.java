 /*
  * Copyright 2012 htfv (Aliaksei Lahachou)
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
 
 package com.github.htfv.maven.plugins.buildconfigurator.el;
 
 import java.beans.FeatureDescriptor;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.el.ArrayELResolver;
 import javax.el.BeanELResolver;
 import javax.el.CompositeELResolver;
 import javax.el.ELContext;
 import javax.el.ELResolver;
 import javax.el.ListELResolver;
 import javax.el.MapELResolver;
 import javax.el.ResourceBundleELResolver;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.maven.project.MavenProject;
 
 import com.github.htfv.maven.plugins.buildconfigurator.utils.ELUtils;
 
 /**
  * Implements Build Configurator specific EL resolver, which allows using Maven
  * project properties in EL.
  *
  * @author htfv (Aliaksei Lahachou)
  */
 public class BuildConfiguratorELResolver extends ELResolver
 {
     /**
      * Creates a new instance of EL resolver which may be used for expression
      * evaluation. Returned resolver includes not only
      * {@code BuildConfiguratorELResolver}, but also all standard resolvers.
      *
      * @param mavenProject
      *            reference to the configured Maven project. Used to resolve
      *            properties referenced in EL expressions.
      * @param configuration
      *            loaded property files.
 
      * @return new instance of EL resolver.
      */
     public static ELResolver newInstance(final MavenProject mavenProject,
             final Configuration configuration)
     {
         CompositeELResolver elResolver = new CompositeELResolver();
 
         elResolver.add(new BuildConfiguratorELResolver(mavenProject, configuration));
         elResolver.add(new ArrayELResolver());
         elResolver.add(new ListELResolver());
         elResolver.add(new MapELResolver());
         elResolver.add(new ResourceBundleELResolver());
 
         //
        // BeanELResolver must be the last one.
         //
 
         elResolver.add(new BeanELResolver());
 
         return elResolver;
     }
 
     /**
      * Loaded property files which is used for property resolving if property
      * was not found in Maven project.
      */
     private final Configuration configuration;
 
     /**
      * Maven project which is used for property resolving.
      */
     private final MavenProject mavenProject;
 
     /**
      * Caches resolved property values.
      */
     private final Map<String, Object> valueCache = new HashMap<String, Object>(0);
 
     /**
      * Constructs a new {@code BuildConfiguratorELResolver} object.
      *
      * @param mavenProject
      *            reference to the configured Maven project. Used to resolve
      *            properties referenced in EL expressions.
      * @param configuration
      *            loaded property files.
      */
     protected BuildConfiguratorELResolver(final MavenProject mavenProject,
             final Configuration configuration)
     {
         this.configuration = configuration;
         this.mavenProject  = mavenProject;
     }
 
     @Override
     public Class<?> getCommonPropertyType(final ELContext context, final Object base)
     {
         return null;
     }
 
     @Override
     public Iterator<FeatureDescriptor> getFeatureDescriptors(final ELContext context,
             final Object base)
     {
         return null;
     }
 
     @Override
     public Class<?> getType(final ELContext context, final Object base, final Object property)
     {
         return null;
     }
 
     @Override
     public Object getValue(final ELContext context, final Object base, final Object property)
     {
         if (base == null)
         {
             String key = property.toString();
 
             if ("prop".equals(key))
             {
                 context.setPropertyResolved(true);
                 return new PropertyAccessor(this, context);
             }
 
             return resolve(context, key);
         }
 
         return null;
     }
 
     @Override
     public boolean isReadOnly(final ELContext context, final Object base, final Object property)
     {
         return false;
     }
 
     /**
      * Resolves value of a property.
      *
      * @param context
      *            {@link ELContext} used to resolve EL expressions.
      * @param property
      *            name of the property to resolve.
      *
      * @return resolved value of the property.
      */
     public Object resolve(final ELContext context, final String property)
     {
         if (valueCache.containsKey(property))
         {
             context.setPropertyResolved(true);
             return valueCache.get(property);
         }
 
         Properties projectProperties = mavenProject.getProperties();
 
         if (projectProperties.containsKey(property))
         {
             String value = projectProperties.getProperty(property);
 
             context.setPropertyResolved(true);
 
             valueCache.put(property, value);
             return value;
         }
 
         if (configuration.containsKey(property))
         {
             Object value = ELUtils.getValue(
                     context, configuration.getString(property), Object.class);
 
             context.setPropertyResolved(true);
 
             valueCache.put(property, value);
             return value;
         }
 
         return null;
     }
 
     @Override
     public void setValue(final ELContext context, final Object base, final Object property,
             final Object value)
     {
     }
 }
