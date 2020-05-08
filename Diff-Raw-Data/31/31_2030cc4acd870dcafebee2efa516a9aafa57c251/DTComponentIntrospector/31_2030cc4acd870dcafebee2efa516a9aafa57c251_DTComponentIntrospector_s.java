 /*******************************************************************************
  * Copyright (c) 2001, 2008 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.designtime.internal.view;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jem.internal.proxy.core.IBeanTypeProxy;
 import org.eclipse.jem.internal.proxy.core.IConfigurationContributor;
 import org.eclipse.jem.internal.proxy.core.IStandardBeanTypeProxyFactory;
 import org.eclipse.jem.internal.proxy.core.ProxyFactoryRegistry;
 import org.eclipse.jem.internal.proxy.ide.IDERegistration;
 import org.eclipse.jst.jsf.common.internal.types.TypeInfoCache;
 import org.eclipse.jst.jsf.common.runtime.internal.model.component.ComponentTypeInfo;
 import org.eclipse.jst.jsf.common.runtime.internal.model.decorator.ConverterTypeInfo;
 import org.eclipse.jst.jsf.common.runtime.internal.model.decorator.ValidatorTypeInfo;
 import org.eclipse.jst.jsf.common.util.JDTBeanIntrospector;
 import org.eclipse.jst.jsf.common.util.JDTBeanProperty;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.JSFCoreTraceOptions;
 import org.eclipse.jst.jsf.core.internal.jem.BeanProxyUtil.BeanProxyWrapper;
 import org.eclipse.jst.jsf.core.internal.jem.BeanProxyUtil.ProxyException;
 import org.eclipse.jst.jsf.core.jsfappconfig.internal.IJSFAppConfigManager;
 import org.eclipse.jst.jsf.core.jsfappconfig.internal.JSFAppConfigManagerFactory;
 import org.eclipse.jst.jsf.facesconfig.emf.ComponentType;
 import org.eclipse.jst.jsf.facesconfig.emf.ConverterType;
 import org.eclipse.jst.jsf.facesconfig.emf.ValidatorType;
 
 /**
  * Utility class for finding and bean introspecting component and related
  * classes.
  * 
  * @author cbateman
  * 
  */
 public final class DTComponentIntrospector
 {
     /**
      * A shared type cache for doing tag class introspection. The cache stores
      * type hierarchies and updates them on detected changes to reduce the
      * overhead of doing such lookups
      */
     private static TypeInfoCache TYPE_CACHE; // lazily created, don't access
 
     // directly
 
     /**
      * @return the type cache used by all DTComponentIntrospector internally.
      */
     public static TypeInfoCache getSharedTypeCache()
     {
         if (TYPE_CACHE == null)
         {
             TYPE_CACHE = TypeInfoCache.createNewInstance();
         }
         return TYPE_CACHE;
     }
 
     /**
      * @param classType
      * @param className
      * @param project
      * @param contributors  may be null
      * @return the component type info for the class type or null if none.  Adds
      * contributors to the class path
      */
     public static ComponentTypeInfo getComponent(final String classType,
             final String className, final IProject project,
             final IConfigurationContributor[] contributors)
     {
         ProxyFactoryRegistry registry = null;
         try
         {
             registry = getProxyFactoryRegistry(project,
                     contributors);
     
             if (registry != null)
             {
                 final IStandardBeanTypeProxyFactory factory = registry
                         .getBeanTypeProxyFactory();
                 final IBeanTypeProxy classTypeProxy = factory
                         .getBeanTypeProxy(className);
                 final BeanProxyWrapper classTypeWrapper = new BeanProxyWrapper(project,
                         classTypeProxy);
     
                 String family = null;
                 String renderer = null;
                 try
                 {
                     classTypeWrapper.init();
                     family = classTypeWrapper.callStringMethod("getFamily"); //$NON-NLS-1$
                     renderer = classTypeWrapper.callStringMethod("getRendererType"); //$NON-NLS-1$
                 }
                 catch (ProxyException e1)
                 {
                     // fall through
                     if (JSFCoreTraceOptions.TRACE_JSPTAGINTROSPECTOR)
                     {
                         JSFCoreTraceOptions.log("DTComponentIntrospector.getComponent:", e1); //$NON-NLS-1$
                     }
                 }
     
                 IType type = null;
     
                 try
                 {
                     type = JavaCore.create(project).findType(className);
                 }
                 catch (JavaModelException e)
                 {
                     // fall through;
                 }
     
                 List<String> interfaces = new ArrayList<String>();
                 List<String> superClasses = new ArrayList<String>();
     
                 if (type != null)
                 {
                     TypeInfoCache typeCache = getSharedTypeCache();
     
                     IType[] interfaceTypes = typeCache.cacheInterfaceTypesFor(type);
                     for (IType interfaze : interfaceTypes)
                     {
                         interfaces.add(interfaze.getFullyQualifiedName());
                     }
     
                     IType[] superClassTypes = typeCache.cacheSupertypesFor(type);
     
                     for (IType superClass : superClassTypes)
                     {
                         superClasses.add(superClass.getFullyQualifiedName());
                     }
                 }
     
                 return new ComponentTypeInfo(classType, className, superClasses
                         .toArray(new String[0]), interfaces.toArray(new String[0]),
                         family, renderer);
             }
         }
         finally
         {
             if (registry != null)
             {
                 registry.terminateRegistry(true);
             }
         }
         return null;
     }
 
     /**
      * @param classType
      * @param className
      * @param project
      * @return the component type info with no additional classpath
      *         contributions. May return null;
      */
     public static ComponentTypeInfo getComponent(final String classType,
             final String className, final IProject project)
     {
         return getComponent(classType, className, project, null);
     }
 
     /**
      * @param converterId
      * @param converterClass
      * @return a new converter type info for the converter id.
      */
     public static ConverterTypeInfo getConverter(final String converterId,
             final String converterClass)
     {
         return new ConverterTypeInfo(converterClass, converterId);
     }
 
     /**
      * @param validatorId
      * @param validatorClass
      * @return a new validator type info for the validator id
      */
     public static ValidatorTypeInfo getValidator(final String validatorId,
             final String validatorClass)
     {
         return new ValidatorTypeInfo(validatorClass, validatorId);
     }
 
     /**
      * @param componentType
      * @param project
      * @return the configured class name for the JSF component type id or null
      *         if not found.
      */
     public static String findComponentClass(final String componentType,
             final IProject project)
     {
         final IJSFAppConfigManager manager = JSFAppConfigManagerFactory
                 .getJSFAppConfigManagerInstance(project);
         final List<ComponentType> components = manager.getComponents();
 
         for (final ComponentType component : components)
         {
             final String type = component.getComponentType().getTextContent()
                     .trim();
 
            if (componentType.equals(type))
             {
                 return component.getComponentClass().getTextContent().trim();
             }
         }
         return null;
     }
 
     /**
      * @param converterId
      * @param project
      * @return a fully qualified class name corresponding to converterId or null
      *         if none.
      */
     public static String findConverterClass(final String converterId,
             final IProject project)
     {
         final IJSFAppConfigManager manager = JSFAppConfigManagerFactory
                 .getJSFAppConfigManagerInstance(project);
         final List<ConverterType> converters = manager.getConverters();
 
         for (final ConverterType converter : converters)
         {
             if (converter != null && converter.getConverterId() != null
                     && converter.getConverterId().getTextContent() != null)
             {
                 final String type = converter.getConverterId().getTextContent()
                         .trim();
 
                 if (converterId.equals(type))
                 {
                     if (converter.getConverterClass() != null && converter.getConverterClass().getTextContent()!=null)
                     {
                         return converter.getConverterClass().getTextContent()
                                 .trim();
                     }
                     return null;
                 }
             }
         }
         return null;
     }
 
     /**
      * @param validatorId
      * @param project
      * @return a fully qualified class name corresponding to the validatorId or
      *         null if none.
      */
     public static String findValidatorClass(final String validatorId,
             final IProject project)
     {
         final IJSFAppConfigManager manager = JSFAppConfigManagerFactory
                 .getJSFAppConfigManagerInstance(project);
         final List<ValidatorType> validators = manager.getValidators();
 
         for (final ValidatorType validatorType : validators)
         {
             if (validatorType != null && validatorType.getValidatorId() != null && 
                     validatorType.getValidatorId().getTextContent() != null)
             {
                 final String type = validatorType.getValidatorId().getTextContent()
                         .trim();
     
                 if (validatorId.equals(type))
                 {
                     return validatorType.getValidatorClass().getTextContent()
                             .trim();
                 }
             }
         }
         return null;
     }
 
     /**
      * @param type
      * @param checkInstanceOf
      * @return true if type resolves to a type that is an instance of one of the
      *         types in checkInstanceOf.
      */
     public static boolean isTypeNameInstanceOfClass(final IType type,
             final Set<String> checkInstanceOf)
     {
         if (checkInstanceOf == null)
         {
             throw new AssertionError();
         }
         if (type != null)
         {
             // first and foremost, are these exactly droids you're
             // looking for and not just their predecessors?
             if (checkInstanceOf.contains(type.getFullyQualifiedName()))
             {
                 return true;
             }
 
             for (final IType supertype : getSharedTypeCache()
                     .cacheSupertypesFor(type))
             {
                 if (checkInstanceOf.contains(supertype.getFullyQualifiedName()))
                 {
                     return true;
                 }
             }
 
             for (final IType interfaces : getSharedTypeCache()
                     .cacheInterfaceTypesFor(type))
             {
                 if (checkInstanceOf
                         .contains(interfaces.getFullyQualifiedName()))
                 {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     /**
      * @param typeInfo
      * @param project
      * @return a list of all bean properties (using the POJO method) on
      *         typeInfo. List may be empty, never null.
      */
     public static Map<String, JDTBeanProperty> getBeanProperties(
             final ComponentTypeInfo typeInfo, final IProject project)
     {
         Map<String, JDTBeanProperty> properties = new HashMap<String, JDTBeanProperty>();
 
         IJavaProject javaProject = JavaCore.create(project);
         try
         {
             IType type = javaProject.findType(typeInfo.getClassName());
 
             if (type != null)
             {
                 // TODO: should be able to use type cache for this
                 final JDTBeanIntrospector introspector = new JDTBeanIntrospector(
                         type);
                 properties = introspector.getProperties();
             }
         }
         catch (JavaModelException e)
         {
             JSFCorePlugin.log("Error finding component type", e); //$NON-NLS-1$
         }
 
         return properties;
     }
 
     /**
      * @param project
      * @return a new proxy factory registry or null;
      * 
      * TODO: caching?
      */
     private static ProxyFactoryRegistry getProxyFactoryRegistry(
             final IProject project, final IConfigurationContributor[] contributors)
     {
         try
         {
             return IDERegistration.startAnImplementation(contributors, false, project,
                     project.getName(), JSFCorePlugin.PLUGIN_ID,
                     new NullProgressMonitor());
         }
         catch (final CoreException e)
         {
             JSFCorePlugin.log("Error starting vm for project: " //$NON-NLS-1$
                     + project.getName(), e);
         }
 
         return null;
     }
 
     private DTComponentIntrospector()
     {
         // no external instantiation
     }
 
     // private static class
 }
