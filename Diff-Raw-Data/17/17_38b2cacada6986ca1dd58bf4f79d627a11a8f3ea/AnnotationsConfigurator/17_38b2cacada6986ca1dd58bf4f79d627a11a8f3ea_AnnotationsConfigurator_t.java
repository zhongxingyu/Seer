 /*
  * Copyright 2006 Maik Schreiber <blizzy AT blizzy DOT de>
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
 package org.directwebremoting.annotations;
 
 import java.beans.Introspector;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.directwebremoting.AccessControl;
 import org.directwebremoting.AjaxFilter;
 import org.directwebremoting.AjaxFilterManager;
 import org.directwebremoting.Configurator;
 import org.directwebremoting.Container;
 import org.directwebremoting.Converter;
 import org.directwebremoting.ConverterManager;
 import org.directwebremoting.Creator;
 import org.directwebremoting.CreatorManager;
 import org.directwebremoting.convert.BeanConverter;
 import org.directwebremoting.create.NewCreator;
 import org.directwebremoting.util.LocalUtil;
 import org.directwebremoting.util.Logger;
 
 /**
  * A Configurator that works off Annotations.
  * @author Maik Schreiber [blizzy AT blizzy DOT de]
  * @author Joe Walker [joe at getahead dot ltd dot uk]
  */
 public class AnnotationsConfigurator implements Configurator
 {
     /* (non-Javadoc)
      * @see org.directwebremoting.Configurator#configure(org.directwebremoting.Container)
      */
     public void configure(Container container)
     {
         Object data = container.getBean("classes"); //$NON-NLS-1$
         if (data == null)
         {
             return;
         }
 
         if (data instanceof String)
         {
             String classesStr = (String) data;
             String[] classNames = classesStr.split(","); //$NON-NLS-1$
             for (int i = 0; i < classNames.length; i++)
             {
                 String className = classNames[i].trim();
                 try
                 {
                     Class<?> clazz = LocalUtil.classForName(className);
                     processClass(clazz, container);
                 }
                 catch (Exception ex)
                 {
                     log.error("Failed to process class: " + className, ex); //$NON-NLS-1$
                 }
             }
         }
         else
         {
             try
             {
                 processClass(data.getClass(), container);
             }
             catch (Exception ex)
             {
                 log.error("Failed to process class: " + data.getClass().getName(), ex); //$NON-NLS-1$
             }
         }
     }
 
     /**
      * Process the annotations on a given class
      * @param clazz The class to search for annotations
      * @param container The IoC container to configure
      * @throws IllegalAccessException If annotation processing fails
      * @throws InstantiationException If annotation processing fails
      */
     private void processClass(Class<?> clazz, Container container) throws InstantiationException, IllegalAccessException
     {
        RemoteProxy createAnn = clazz.getAnnotation(RemoteProxy.class);
         if (createAnn != null)
         {
             processCreate(clazz, createAnn, container);
         }
 
         Convert convertAnn = clazz.getAnnotation(Convert.class);
         if (convertAnn != null)
         {
             processConvert(clazz, convertAnn, container);
         }
 
         GlobalFilter globalFilterAnn = clazz.getAnnotation(GlobalFilter.class);
         if (globalFilterAnn != null)
         {
             processGlobalFilter(clazz, globalFilterAnn, container);
         }
     }
 
     /**
     * Process the @RemoteProxy annotaion on a given class
     * @param clazz The class annotated with @RemoteProxy
      * @param createAnn The annotation
      * @param container The IoC container to configure
      */
    private void processCreate(Class<?> clazz, RemoteProxy createAnn, Container container)
     {
         Class<? extends Creator> creator = createAnn.creator();
         String creatorClass = creator.getName();
         Map<String, String> creatorParams = getParamsMap(createAnn.creatorParams());
         ScriptScope scope = createAnn.scope();
 
         CreatorManager creatorManager = (CreatorManager) container.getBean(CreatorManager.class.getName());
         String creatorName = LocalUtil.replace(creatorClass, ".", "_"); //$NON-NLS-1$ //$NON-NLS-2$
         creatorManager.addCreatorType(creatorName, creatorClass);
 
         Map<String, String> params = new HashMap<String, String>();
         if (NewCreator.class.isAssignableFrom(NewCreator.class))
         {
             params.put("class", clazz.getName()); //$NON-NLS-1$
         }
         params.putAll(creatorParams);
         params.put("scope", scope.getValue()); //$NON-NLS-1$
 
         String name = createAnn.name();
         if (name == null || name.length() == 0)
         {
             name = LocalUtil.getShortClassName(clazz);
         }
 
         try
         {
             log.info("Adding class " + clazz.getName() + " as " + name); //$NON-NLS-1$ //$NON-NLS-2$
             creatorManager.addCreator(name, creatorName, params);
         }
         catch (Exception ex)
         {
             log.error("Failed to add class as Creator: " + clazz.getName(), ex); //$NON-NLS-1$
         }
 
         AccessControl accessControl = (AccessControl) container.getBean(AccessControl.class.getName());
         Method[] methods = clazz.getMethods();
         for (int i = 0; i < methods.length; i++)
         {
             if (methods[i].getAnnotation(RemoteMethod.class) != null)
             {
                 accessControl.addIncludeRule(name, methods[i].getName());
 
                 Auth authAnn = methods[i].getAnnotation(Auth.class);
                 if (authAnn != null)
                 {
                     accessControl.addRoleRestriction(name, methods[i].getName(), authAnn.role());
                 }
             }
         }
 
         Filters filtersAnn = clazz.getAnnotation(Filters.class);
         if (filtersAnn != null)
         {
             Filter[] fs = filtersAnn.value();
             for (int i = 0; i < fs.length; i++)
             {
                 processFilter(fs[i], name, container);
             }
         }
         // process single filter for convenience
         else
         {
             Filter filterAnn = clazz.getAnnotation(Filter.class);
             if (filterAnn != null)
             {
                 processFilter(filterAnn, name, container);
             }
         }
     }
 
     /**
      * Process the @Filter annotaion
      * @param filterAnn The filter annotation
      * @param name The Javascript name of the class to filter 
      * @param container The IoC container to configure
      */
     private void processFilter(Filter filterAnn, String name, Container container)
     {
         Map<String, String> filterParams = getParamsMap(filterAnn.params());
         AjaxFilter filter = (AjaxFilter) LocalUtil.classNewInstance(name, filterAnn.type().getName(), AjaxFilter.class);
         if (filter != null)
         {
             LocalUtil.setParams(filter, filterParams, null);
             AjaxFilterManager filterManager = (AjaxFilterManager) container.getBean(AjaxFilterManager.class.getName());
             filterManager.addAjaxFilter(filter, name);
         }
     }
 
     /**
      * Process the @Convert annotaion on a given class
      * @param clazz The class annotated with @Convert
      * @param convertAnn The annotation
      * @param container The IoC container to configure
      * @throws InstantiationException
      * @throws IllegalAccessException
      */
     private void processConvert(Class<?> clazz, Convert convertAnn, Container container) throws InstantiationException, IllegalAccessException
     {
         Class<? extends Converter> converter = convertAnn.converter();
         String converterClass = converter.getName();
         Map<String, String> params = getParamsMap(convertAnn.params());
 
         ConverterManager converterManager = (ConverterManager) container.getBean(ConverterManager.class.getName());
         String converterName = LocalUtil.replace(converterClass, ".", "_"); //$NON-NLS-1$ //$NON-NLS-2$
         converterManager.addConverterType(converterName, converterClass);
 
         if (BeanConverter.class.isAssignableFrom(converter))
         {
             StringBuilder properties = new StringBuilder();
             Field[] fields = clazz.getFields();
             for (int i = 0; i < fields.length; i++)
             {
                 if (fields[i].getAnnotation(RemoteProperty.class) != null)
                 {
                     properties.append(',').append(fields[i].getName());
                 }
             }
 
             Method[] methods = clazz.getMethods();
             for (int i = 0; i < methods.length; i++)
             {
                 if (methods[i].getAnnotation(RemoteProperty.class) != null)
                 {
                     String name = methods[i].getName();
                     if (name.startsWith(METHOD_PREFIX_GET) || name.startsWith(METHOD_PREFIX_IS))
                     {
                         if (name.startsWith(METHOD_PREFIX_GET))
                         {
                             name = name.substring(3);
                         }
                         else
                         {
                             name = name.substring(2);
                         }
                         name = Introspector.decapitalize(name);
                         properties.append(',').append(name);
                     }
                 }
             }
             if (properties.length() > 0)
             {
                 properties.deleteCharAt(0);
                 params.put("include", properties.toString()); //$NON-NLS-1$
             }
         }
 
         converterManager.addConverter(clazz.getName(), converterName, params);
     }
 
     /**
      * Global Filters apply to all classes
      * @param clazz The class to use as a filter
      * @param globalFilterAnn The filter annotation
      * @param container The IoC container to configure
      * @throws InstantiationException In case we can't create the given clazz
      * @throws IllegalAccessException In case we can't create the given clazz
      */
     private void processGlobalFilter(Class<?> clazz, GlobalFilter globalFilterAnn, Container container) throws InstantiationException, IllegalAccessException
     {
         if (!AjaxFilter.class.isAssignableFrom(clazz))
         {
             throw new IllegalArgumentException(clazz.getName() + " is not an AjaxFilter implementation"); //$NON-NLS-1$
         }
 
         Map<String, String> filterParams = getParamsMap(globalFilterAnn.params());
         AjaxFilter filter = (AjaxFilter) clazz.newInstance();
         if (filter != null)
         {
             LocalUtil.setParams(filter, filterParams, null);
             AjaxFilterManager filterManager = (AjaxFilterManager) container.getBean(AjaxFilterManager.class.getName());
             filterManager.addAjaxFilter(filter);
         }
     }
 
     /**
      * Utility to turn a Param array into a Map<String, String>.
      * TODO: Should we move this code into Param? Is that even possible? 
      * @param params The params array from annotations
      * @return A Map<String, String>
      */
     private Map<String, String> getParamsMap(Param[] params)
     {
         Map<String, String> result = new HashMap<String, String>();
         if (params != null)
         {
             for (int i = 0; i < params.length; i++)
             {
                 Param p = params[i];
                 result.put(p.name(), p.value());
             }
         }
         return result;
     }
 
     /**
      * The getter prefix for boolean variables
      */
     private static final String METHOD_PREFIX_IS = "is"; //$NON-NLS-1$
 
     /**
      * The getter prefix for non-boolean variables
      */
     private static final String METHOD_PREFIX_GET = "get"; //$NON-NLS-1$
 
     /**
      * The log stream
      */
     private static final Logger log = Logger.getLogger(AnnotationsConfigurator.class);
 }
