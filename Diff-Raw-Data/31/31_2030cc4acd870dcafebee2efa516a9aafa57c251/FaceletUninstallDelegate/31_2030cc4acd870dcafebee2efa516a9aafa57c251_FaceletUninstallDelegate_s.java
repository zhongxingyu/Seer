 /*******************************************************************************
  * Copyright (c) 2008 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Cameron Bateman - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.facelet.core.internal.facet;
 
 import java.util.Iterator;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jst.jsf.designtime.DesignTimeApplicationManager;
 import org.eclipse.jst.jsf.facelet.core.internal.FaceletCoreTraceOptions;
 import org.eclipse.jst.jsf.facesconfig.emf.ApplicationType;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigType;
 import org.eclipse.jst.jsf.facesconfig.emf.ViewHandlerType;
 import org.eclipse.jst.jsf.facesconfig.util.FacesConfigArtifactEdit;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 
 /**
  * The facet removal delegate for Facelets.
  * 
  * @author cbateman
  * 
  */
 public class FaceletUninstallDelegate extends FaceletChangeDelegate
 {
 
     @Override
     public void execute(final IProject project, final IProjectFacetVersion fv,
             final Object config, final IProgressMonitor monitor)
             throws CoreException
     {
         final FacetChangeModel model = (FacetChangeModel) config;
         if (FaceletCoreTraceOptions.TRACE_FACETUNINSTALLDELEGATE)
         {
             FaceletCoreTraceOptions.log("Uninstalling facet on project: " //$NON-NLS-1$
                     + project.getName());
 
             FaceletCoreTraceOptions.log(String.format(
                     "FaceletUninstallDelegate: Remove default selection %b", //$NON-NLS-1$
                     Boolean.valueOf(model.isChgDefaultSuffix())));
             FaceletCoreTraceOptions
                     .log(String
                             .format(
                                     "FaceletUninstallDelegate: Remove view handler %b", Boolean.valueOf(model //$NON-NLS-1$
                                                     .isChgViewHandler())));
             FaceletCoreTraceOptions.log(String.format(
                     "FaceletUninstallDelegate: Remove configure listener %b", //$NON-NLS-1$
                     Boolean.valueOf(model.isChgConfigureListener())));
             FaceletCoreTraceOptions
                     .log(String
                             .format(
                                     "FaceletUninstallDelegate: Remove web app lifecycle listener %b", //$NON-NLS-1$
                                     Boolean.valueOf(model
                                             .isChgWebAppLifecycleListener())));
         }
 
         try
         {
             if (monitor != null)
             {
                 monitor.beginTask("Uninstalling facelet facet", 1); //$NON-NLS-1$
             }
 
             super.execute(project, fv, config, monitor);
         }
         finally
         {
             if (monitor != null)
             {
                 monitor.done();
             }
         }
     }
 
     /**
      * Install the facelet view handler in the WEB-INF/faces-config file if not
      * already present.
      * 
      * @param project
      * @param monitor
      */
     @SuppressWarnings("unchecked")
     @Override
     protected void maybeChangeFaceletViewHandler(final IProject project,
             final IProgressMonitor monitor)
     {
         if (FaceletCoreTraceOptions.TRACE_FACETUNINSTALLDELEGATE)
         {
             FaceletCoreTraceOptions
                     .log("FaceletInstallDelegate: Uninstalling facelet view handler"); //$NON-NLS-1$
         }
 
         final FacesConfigArtifactEdit edit = FacesConfigArtifactEdit
                 .getFacesConfigArtifactEditForWrite(project,
                         "WEB-INF/faces-config.xml"); //$NON-NLS-1$
         if (edit != null)
         {
             try
             {
                 final FacesConfigType root = edit.getFacesConfig();
                 if (isViewHandlerPresent(root))
                 {
                     if (FaceletCoreTraceOptions.TRACE_FACETUNINSTALLDELEGATE)
                     {
                         FaceletCoreTraceOptions
                                 .log("FaceletInstallDelegate: View Handler not already found in faces-config"); //$NON-NLS-1$
                     }
                     final EList applications = root.getApplication();
 
                     for (final Iterator<?> appIt = applications.iterator(); appIt
                             .hasNext();)
                     {
                         final ApplicationType application = (ApplicationType) appIt
                                 .next();
                         for (final Iterator viewHandlerIt = application
                                 .getViewHandler().iterator(); viewHandlerIt
                                 .hasNext();)
                         {
                             final ViewHandlerType viewHandlerType = (ViewHandlerType) viewHandlerIt
                                     .next();
                            if (viewHandlerType != null
                                     && RUNTIME_VIEWHANDLER_CLASS_NAME
                                             .equals(viewHandlerType
                                                     .getTextContent().trim()))
                             {
                                 viewHandlerIt.remove();
 
                                 if (FaceletCoreTraceOptions.TRACE_FACETUNINSTALLDELEGATE)
                                 {
                                     FaceletCoreTraceOptions
                                             .log("FaceletUninstallDelegate: Removed runtime view handler"); //$NON-NLS-1$
                                 }
                             }
                         }
                     }
 
                     edit.save(monitor);
 
                     if (FaceletCoreTraceOptions.TRACE_FACETUNINSTALLDELEGATE)
                     {
                         FaceletCoreTraceOptions
                                 .log("FaceletUninstallDelegate: Saved changes for facelet view handler"); //$NON-NLS-1$
                     }
                 }
                 else
                 {
                     if (FaceletCoreTraceOptions.TRACE_FACETUNINSTALLDELEGATE)
                     {
                         FaceletCoreTraceOptions
                                 .log("FaceletInstallDelegate: View Handler NOT already found in faces-config; not adding"); //$NON-NLS-1$
                     }
                 }
             }
             finally
             {
                 edit.dispose();
             }
         }
     }
 
     @Override
     protected void handleDesignTimeViewHandler(final IProject project)
     {
         final DesignTimeApplicationManager manager = DesignTimeApplicationManager
                 .getInstance(project);
 
         // TODO: need this API
         //manager.clearViewHandlerId();
         manager.setViewHandlerId("org.eclipse.jst.jsf.designtime.view.jspviewhandler"); //$NON-NLS-1$
     }
 
     @Override
     protected String getDisplayName()
     {
         return Messages.FaceletUninstallDelegate_FACET_INSTALLER_DELEGATE_DISPLAY_NAME;
     }
 
     @Override
     protected void maybeChangeDefaultSuffix(final FacetChangeModel model,
             final WebAppConfigurator configurator)
     {
         if (model.isChgDefaultSuffix())
         {
             if (FaceletCoreTraceOptions.TRACE_FACETUNINSTALLDELEGATE)
             {
                 FaceletCoreTraceOptions
                         .log("FaceletUninstallDelegate: Removing DEFAULT_SUFFIX"); //$NON-NLS-1$
             }
             configurator
                     .removeContextParam(
                             FaceletFacet.JAVAX_FACES_DEFAULT_SUFFIX,
                             FaceletFacet.XHTML);
         }
     }
 
     @Override
     protected void maybeChangeConfigureListener(final FacetChangeModel model,
             final WebAppConfigurator configurator)
     {
         if (model.isChgConfigureListener())
         {
             if (FaceletCoreTraceOptions.TRACE_FACETUNINSTALLDELEGATE)
             {
                 FaceletCoreTraceOptions
                         .log("FaceletUninstallDelegate: Remove Configure Listener"); //$NON-NLS-1$
             }
             configurator
                     .removeListener(FaceletFacet.COM_SUN_FACES_CONFIG_CONFIGURE_LISTENER);
         }
     }
 
     @Override
     protected void maybeChangeWebLifecycleListener(
             final FacetChangeModel model, final WebAppConfigurator configurator)
     {
         if (model.isChgWebAppLifecycleListener())
         {
             if (FaceletCoreTraceOptions.TRACE_FACETUNINSTALLDELEGATE)
             {
                 FaceletCoreTraceOptions
                         .log("FaceletUninstallDelegate: Remove WebappLifecycleListener"); //$NON-NLS-1$
             }
             configurator
                     .removeListener(FaceletFacet.COM_SUN_FACES_APPLICATION_WEBAPP_LIFECYCLE_LISTENER);
         }
     }
 }
