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
 import org.eclipse.jst.jsf.facelet.core.internal.FaceletCoreTraceOptions;
 import org.eclipse.jst.jsf.facesconfig.emf.ApplicationType;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigType;
 import org.eclipse.jst.jsf.facesconfig.emf.ViewHandlerType;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.wst.common.project.facet.core.IDelegate;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 
 /**
  * The super-class of all Facelet facet change delegates.  The only expected 
  * delegates at this time are install and uninstall.  Each super-class must
  * decide the meaning of "change" and implement the abstract members 
  * appropriately.
  * 
  * @author cbateman
  *
  */
 public abstract class FaceletChangeDelegate implements IDelegate
 {
     /**
      * The default name of the Facelet runtime view handler
      */
     protected static final String RUNTIME_VIEWHANDLER_CLASS_NAME = "com.sun.facelets.FaceletViewHandler"; //$NON-NLS-1$
 
     public void execute(final IProject project, final IProjectFacetVersion fv,
             final Object config, final IProgressMonitor monitor)
             throws CoreException
     {
         PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable()
         {
             public void run()
             {
                 final FacetChangeModel model = (FacetChangeModel) config;
 
                 // XXX: look at glassfish changes
                 handleDesignTimeViewHandler(project);
 
                 if (model.isChgViewHandler())
                 {
                     maybeChangeFaceletViewHandler(project, monitor);
 				}
 				
 				final WebAppConfigurator configurator = WebAppConfigurator
                         .getConfigurator(project);
 
                 if (configurator != null)
                 {
                     maybeChangeDefaultSuffix(model, configurator);
                     maybeChangeConfigureListener(model, configurator);
                     maybeChangeWebLifecycleListener(model, configurator);
                 }
                 else if (FaceletCoreTraceOptions.TRACE_FACETCHANGEDELEGATE)
                 {
                     FaceletCoreTraceOptions
                             .log("FaceletChangeDelegate: No web configurator"); //$NON-NLS-1$
                 }
             }
         });
     }
 
     /**
      * Performs the change to the web lifecycle listener configuration of of the
      * web.xml model using configurator, if applicable.
      * 
      * @param model
      * @param configurator
      */
     protected abstract void maybeChangeWebLifecycleListener(
             FacetChangeModel model, WebAppConfigurator configurator);
 
     /**
      * Performs the change to the configure listener configuration of the
      * web.xml model using configurator, if applicable.
      * 
      * @param model
      * @param configurator
      */
     protected abstract void maybeChangeConfigureListener(
             FacetChangeModel model, WebAppConfigurator configurator);
 
     /**
      * Performs the change to the DEFAULT_SUFFIX configuration of the web.xml
      * model using configurator, if applicable.
      * 
      * @param model
      * @param configurator
      */
     protected abstract void maybeChangeDefaultSuffix(FacetChangeModel model,
             WebAppConfigurator configurator);
 
     /**
      * Changes the runtime view handler settings on project if applicable.
      * 
      * @param project
      * @param monitor
      * 
      */
     protected abstract void maybeChangeFaceletViewHandler(IProject project,
             IProgressMonitor monitor);
 
     /**
      * Change the designtime view handler if applicable.
      * 
      * @param project
      */
     protected abstract void handleDesignTimeViewHandler(final IProject project);
 
     /**
      * @return a user displayable name of the sub-classing change delegate.
      */
     protected abstract String getDisplayName();
 
     /**
      * @param root
      * @return true if the Facelet view handler is already present in the
      *         WEB-INF/faces-config.xml file.
      */
     protected final boolean isViewHandlerPresent(final FacesConfigType root)
     {
         final EList<?> applications = root.getApplication();
         for (final Object name : applications)
         {
             final ApplicationType app = (ApplicationType) name;
             if (app != null)
             {
                 for (final Iterator<?> viewIt = app.getViewHandler().iterator(); viewIt
                         .hasNext();)
                 {
                     final ViewHandlerType viewHandler = (ViewHandlerType) viewIt
                             .next();
                    if (viewHandler != null
                             && RUNTIME_VIEWHANDLER_CLASS_NAME
                                     .equals(viewHandler.getTextContent().trim()))
                     {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 }
