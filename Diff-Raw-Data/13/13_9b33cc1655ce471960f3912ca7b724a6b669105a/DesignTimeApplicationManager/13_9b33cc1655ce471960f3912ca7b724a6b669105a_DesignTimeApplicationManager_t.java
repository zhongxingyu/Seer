 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Cameron Bateman/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 
 package org.eclipse.jst.jsf.designtime;
 
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.designtime.context.AbstractDTExternalContextFactory;
 import org.eclipse.jst.jsf.designtime.context.DTFacesContext;
 import org.eclipse.jst.jsf.designtime.context.IExternalContextFactoryLocator;
 import org.eclipse.jst.jsf.designtime.el.AbstractDTMethodResolver;
 import org.eclipse.jst.jsf.designtime.el.AbstractDTPropertyResolver;
 import org.eclipse.jst.jsf.designtime.el.AbstractDTVariableResolver;
 
 
 /**
  * Per-web-application manager that manages design time information for a corresponding
  * project.
  * 
  * @author cbateman
  *
  */
 public final class DesignTimeApplicationManager 
 {
     // TODO: load from property file?
     private static final String   PROPERTY_QUALIFIER = "org.eclipse.jst.jsf.designtime.internal"; //$NON-NLS-1$
     private static final String   SESSION_PROPERTY_NAME_PROJECT = "DesignTimeApplicationManager"; //$NON-NLS-1$
     private static final QualifiedName  SESSION_PROPERTY_KEY_PROJECT 
         = new QualifiedName(PROPERTY_QUALIFIER, SESSION_PROPERTY_NAME_PROJECT);
     
     private static final String   SESSION_PROPERTY_NAME_FACES_CONTEXT = "DTFacesContext"; //$NON-NLS-1$
     private static final QualifiedName SESSION_PROPERTY_KEY_FACES_CONTEXT
        =  new QualifiedName(PROPERTY_QUALIFIER, SESSION_PROPERTY_NAME_FACES_CONTEXT);
     
     private static final String   PERSIST_PROPERTY_NAME_EXTERNAL_CONTEXT_PROVIDER =
         "ExternalContextProvider"; //$NON-NLS-1$
     private static final QualifiedName PERSIST_PROPERTY_KEY_EXTERNAL_CONTEXT_PROVIDER =
         new QualifiedName(PROPERTY_QUALIFIER, PERSIST_PROPERTY_NAME_EXTERNAL_CONTEXT_PROVIDER);
     
     private static final String   PERSIST_PROPERTY_NAME_VARIABLE_RESOLVER_PROVIDER =
         "VariableResolverProvider"; //$NON-NLS-1$
     private static final QualifiedName PERSIST_PROPERTY_KEY_VARIABLE_RESOLVER_PROVIDER 
        = new QualifiedName(PROPERTY_QUALIFIER, PERSIST_PROPERTY_NAME_VARIABLE_RESOLVER_PROVIDER);
 
     private static final String   PERSIST_PROPERTY_NAME_PROPERTY_RESOLVER_PROVIDER =
         "PropertyResolverProvider"; //$NON-NLS-1$
     private static final QualifiedName PERSIST_PROPERTY_KEY_PROPERTY_RESOLVER_PROVIDER =
         new QualifiedName(PROPERTY_QUALIFIER, PERSIST_PROPERTY_NAME_PROPERTY_RESOLVER_PROVIDER);
 
     private static final String    PERSIST_PROPERTY_NAME_METHOD_RESOLVER_PROVIDER =
         "MethodResolverProvider"; //$NON-NLS-1$
     private static final QualifiedName PERSIST_PROPERTY_KEY_METHOD_RESOLVER_PROVIDER =
         new QualifiedName(PROPERTY_QUALIFIER, PERSIST_PROPERTY_NAME_METHOD_RESOLVER_PROVIDER);
 
     private static final String   DEFAULT_EXTERNAL_CONTEXT_ID = 
         "org.eclipse.jst.jsf.core.externalcontext.default"; //$NON-NLS-1$
     
     private static final String   DEFAULT_VARIABLE_RESOLVER_ID = 
         "org.eclipse.jst.jsf.core.variableresolver.default"; //$NON-NLS-1$
     
     private static final String   DEFAULT_PROPERTY_RESOLVER_ID =
         "org.eclipse.jst.jsf.core.propertyresolver.default"; //$NON-NLS-1$
         
     private static final String   DEFAULT_METHOD_RESOLVER_ID =
         "org.eclipse.jst.jsf.core.methodresolver.default"; //$NON-NLS-1$
 
     /**
      * @param project
      * @return the app manager associated with project
      */
     public final static DesignTimeApplicationManager getInstance(IProject project)
     {
         if (project == null)
         {
             return null;
         }
         
         try
         {
             synchronized(project)
             {
                 DesignTimeApplicationManager manager = 
                     (DesignTimeApplicationManager) project.getSessionProperty(SESSION_PROPERTY_KEY_PROJECT);
 
                
                 if (manager == null)
                 {
                     manager = new DesignTimeApplicationManager(project);
                     project.setSessionProperty(SESSION_PROPERTY_KEY_PROJECT, manager);
                 }
                 // bug 147729: if project was renamed, the project param will be
                 // valid, but it will not be in sync with the one for _project
                 // unfortunately, since we are using session propertie
                else
                 {
                    synchronized(manager)
                    {
                        if (!project.equals(manager._project))
                        {
                            manager._project = project;
                        }
                    }
                 }
 
                 return manager;
             }
         }
         catch (CoreException ce)
         {
             Platform.getLog(JSFCorePlugin.getDefault().getBundle()).log(new Status(IStatus.ERROR, JSFCorePlugin.getDefault().getBundle().getSymbolicName(), 0, "Problem loading design time appmanager", new Throwable(ce))); //$NON-NLS-1$
         }
         
         return null;
     }
     
     // instance definition
     // _project must be writable in case the manager needs to be retargetted
     // after a rename/move etc.
     private IProject                                    _project;
     private final IExternalContextFactoryLocator        _locator;
     
     private DesignTimeApplicationManager(IProject project)
     {
         _project = project;
         _locator = new MyExternalContextFactoryLocator();
     }
     
     /**
      * @param file must not be null
      * @return the faces context for the file or null if not found
      */
     public DTFacesContext getFacesContext(IFile file)
     {
         assert file != null;
         
         try
         {
             synchronized(file)
             {
                 Object context = file.getSessionProperty(SESSION_PROPERTY_KEY_FACES_CONTEXT);
                 
                 if (context == null)
                 {
                     context = new DTFacesContext(_locator);
                     file.setSessionProperty(SESSION_PROPERTY_KEY_PROJECT, context);
                 }
                 
                 return (DTFacesContext) context;
             }
         }
         catch (CoreException ce)
         {
             Platform.getLog(JSFCorePlugin.getDefault().getBundle())
                 .log(new Status(IStatus.ERROR, 
                         JSFCorePlugin.getDefault().getBundle().getSymbolicName(), 
                         0, "Problem loading design time facescontext",  //$NON-NLS-1$
                         new Throwable(ce)));
         }
         
         return null;
     }
     
     /**
      * @param resolverPluginId
      * @throws CoreException
      */
     public synchronized void setExternalContextProvider(final String resolverPluginId)
                                 throws CoreException
     {
         _project.setPersistentProperty
             (PERSIST_PROPERTY_KEY_EXTERNAL_CONTEXT_PROVIDER, resolverPluginId);
     }
     
     /**
      * @return the id of the active design time external context provider
      */
     public synchronized String getExternalContextProvider()
     {
         return getResolverId(PERSIST_PROPERTY_KEY_EXTERNAL_CONTEXT_PROVIDER,
                 DEFAULT_EXTERNAL_CONTEXT_ID);
     }
     
     /**
      * @return the designtime variable resolver for this application
      */
     public synchronized AbstractDTVariableResolver getVariableResolver()
     {
         return getResolver(PERSIST_PROPERTY_KEY_VARIABLE_RESOLVER_PROVIDER,
                                                         JSFCorePlugin.getVariableResolvers(),
                                                         DEFAULT_VARIABLE_RESOLVER_ID);
     }
     
     /**
      * Sets the plugin used to determine the designtime variable resolver.  To 
      * reset to the default, pass null.
      * 
      * @param resolverPluginId
      * @throws CoreException -- if the setting the new value fails
      */
     public synchronized void setVariableResolverProvider(final String resolverPluginId)
                                 throws CoreException
     {
         _project.setPersistentProperty
             (PERSIST_PROPERTY_KEY_VARIABLE_RESOLVER_PROVIDER, resolverPluginId);
     }
     
     /**
      * @return the id of the active design time variable resolver
      */
     public synchronized String getVariableResolverProvider()
     {
         return getResolverId(PERSIST_PROPERTY_KEY_VARIABLE_RESOLVER_PROVIDER,
                              DEFAULT_VARIABLE_RESOLVER_ID);
     }
     
     
     /**
      * @return the designtime property resolver for this application 
      */
     public synchronized AbstractDTPropertyResolver getPropertyResolver()
     {
         return getResolver(PERSIST_PROPERTY_KEY_PROPERTY_RESOLVER_PROVIDER,
                 JSFCorePlugin.getPropertyResolvers(),
                 DEFAULT_PROPERTY_RESOLVER_ID);
     }
     
     /**
      * @param resolverPluginId
      * @throws CoreException -- if setting the provider fails
      */
     public synchronized void setPropertyResolverProvider(final String resolverPluginId)
                                                   throws CoreException
     {
         _project.setPersistentProperty
             (PERSIST_PROPERTY_KEY_PROPERTY_RESOLVER_PROVIDER, resolverPluginId);       
     }
 
     /**
      * @return the id of the active design time variable resolver
      */
     public synchronized String getPropertyResolverProvider()
     {
         return getResolverId(PERSIST_PROPERTY_KEY_PROPERTY_RESOLVER_PROVIDER,
                              DEFAULT_PROPERTY_RESOLVER_ID);
     }
 
     /**
      * @return the designtime method resolver for this application
      */
     public synchronized AbstractDTMethodResolver getMethodResolver()
     {
         return getResolver(PERSIST_PROPERTY_KEY_METHOD_RESOLVER_PROVIDER,
                 JSFCorePlugin.getMethodResolvers(),
                 DEFAULT_METHOD_RESOLVER_ID);
     }
 
     /**
      * @param resolverPluginId
      * @throws CoreException -- if setting the plugin fails
      */
     public synchronized void setMethodResolverProvider(final String resolverPluginId)
                                             throws CoreException
     {
         _project.setPersistentProperty
             (PERSIST_PROPERTY_KEY_METHOD_RESOLVER_PROVIDER, resolverPluginId);       
 
     }
     
     /**
      * @return the id of the active design time variable resolver
      */
     public synchronized String getMethodResolverProvider()
     {
         return getResolverId(PERSIST_PROPERTY_KEY_METHOD_RESOLVER_PROVIDER,
                              DEFAULT_METHOD_RESOLVER_ID);
     }
 
     
     private <ResolverProvider> ResolverProvider getResolver(final QualifiedName pluginKey,
                                final Map<String, ResolverProvider>   registry,
                                final String defaultId)
     {
         String pluginId = defaultId;
         
         try
         {
            pluginId =
                 _project.getPersistentProperty(pluginKey);
            
            // if don't have the plugin in the registry, then 
            // revert to default
            if (pluginId == null)
            {
                pluginId = defaultId;
            }
            else if (registry.get(pluginId) == null)
            {
                JSFCorePlugin.getDefault().getLog().log(
                        new Status(IStatus.WARNING, JSFCorePlugin.PLUGIN_ID,
                                    0, "Plugin: "+pluginId+" not found", //$NON-NLS-1$ //$NON-NLS-2$
                                    new Throwable()));
                pluginId = defaultId;
            }
         }
         catch (CoreException ce)
         {
             JSFCorePlugin.getDefault().getLog().log(
                     new Status(IStatus.ERROR, JSFCorePlugin.PLUGIN_ID,
                                 0, "Error getting plugin property", //$NON-NLS-1$
                                 ce));
             pluginId = defaultId;
             // fall-through and use the default
         }
 
         return registry.get(pluginId);
     }
     
     private String getResolverId(final QualifiedName key, final String defaultValue)
     {
         String id = defaultValue; 
 
         try
         {
             final String userId = 
                 _project.getPersistentProperty(key);
             
             if (userId != null)
             {
                 id = userId;
             }
         }
         catch (CoreException ce)
         {
             // do nothing; fall through and return default
         }
 
         return id;
     }
     
     private class MyExternalContextFactoryLocator implements IExternalContextFactoryLocator
     {
         public AbstractDTExternalContextFactory getFactory() {
             return getResolver(PERSIST_PROPERTY_KEY_EXTERNAL_CONTEXT_PROVIDER,
                     JSFCorePlugin.getExternalContextProviders(),
                         DEFAULT_EXTERNAL_CONTEXT_ID);
         }
     }
 }
