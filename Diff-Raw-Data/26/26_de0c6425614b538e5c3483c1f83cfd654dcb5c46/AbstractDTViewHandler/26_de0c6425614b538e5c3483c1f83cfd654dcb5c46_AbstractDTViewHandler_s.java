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
 
 import java.util.Collections;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jst.jsf.common.internal.resource.ImmutableLifecycleListener;
 import org.eclipse.jst.jsf.common.runtime.internal.model.component.ComponentTypeInfo;
 import org.eclipse.jst.jsf.context.symbol.ISymbol;
 import org.eclipse.jst.jsf.designtime.context.DTFacesContext;
 import org.eclipse.jst.jsf.designtime.internal.view.DTUIViewRoot.StalenessAdvisor;
 import org.eclipse.jst.jsf.designtime.internal.view.DTUIViewRoot.StalenessListener;
 import org.eclipse.jst.jsf.designtime.internal.view.DTUIViewRoot.VersionStamp;
 import org.eclipse.jst.jsf.designtime.internal.view.IDTViewHandler.ViewHandlerException.Cause;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 
 /**
  * All IDTViewHandler's must sub-class this abstract class.
  * 
  * @author cbateman
  * 
  */
 public abstract class AbstractDTViewHandler implements IDTViewHandler
 {
     private final AtomicBoolean _isDisposed    = new AtomicBoolean(false);
 
     private AbstractDTViewHandler parent;
 
     /**
      * the path separator
      */
     public static final String  PATH_SEPARATOR = "/"; //$NON-NLS-1$
 
     public abstract String calculateLocale(DTFacesContext context)
             throws ViewHandlerException;
 
     public final DTUIViewRoot createView(final DTFacesContext facesContext,
             final String viewId) throws ViewHandlerException
     {
         if (_isDisposed.get())
         {
             throw new IllegalStateException("View handler is disposed"); //$NON-NLS-1$
         }
 
         final DTUIViewRoot viewRoot = internalCreateView(facesContext, viewId);
         
         if (viewRoot == null)
         {
             throw new ViewHandlerException("Problem in createView", Cause.UNABLE_TO_CREATE_VIEW); //$NON-NLS-1$
         }
         viewRoot.setViewId(viewId);
         final VersionStamp versionStamp = createVersionStamp(facesContext,
                 viewId);
         if (versionStamp == null)
         {
             throw new ViewHandlerException(new Throwable(
                     "Bad version stamp detected"), Cause.BAD_VERSION_STAMP); //$NON-NLS-1$
         }
         viewRoot.setVersionStamp(versionStamp);
 
         final StalenessAdvisor advisor = createStalenessAdvisor(viewRoot,
                 facesContext, viewId);
 
         if (advisor == null)
         {
             throw new ViewHandlerException(new Throwable(
                     "Bad staleness advisor"), Cause.BAD_STALENESS_ADVISOR); //$NON-NLS-1$
         }
         viewRoot.setStalenessAdvisor(advisor);
 
         // lock down the tree
         viewRoot.setSubtreeProtected();
 
         registerView(viewRoot, facesContext, viewId);
 
         return viewRoot;
     }
 
     /**
      * @param facesContext
      * @param viewId
      * @return internal construction of the view root.
      */
     protected abstract DTUIViewRoot internalCreateView(
             final DTFacesContext facesContext, final String viewId);
 
     public abstract IResource getActionDefinition(DTFacesContext context,
             String viewId) throws ViewHandlerException;
 
     public abstract IPath getActionURL(DTFacesContext context,
             IResource resource, IPath requestPath) throws ViewHandlerException;
 
     public abstract IPath getRelativeActionPath(DTFacesContext context,
             String relativeToViewId, String uri) throws ViewHandlerException;
 
     public abstract IViewDefnAdapterFactory getViewMetadataAdapterFactory(
             DTFacesContext context) throws ViewHandlerException;
 
     public abstract boolean supportsViewDefinition(IFile file);
 
     
     public abstract void setLifecycleListener(ImmutableLifecycleListener listener);
 
     public final void dispose()
     {
         if (_isDisposed.compareAndSet(false, true))
         {
             doDispose();
         }
     }
 
     /**
      * Sub-classes should override to add disposal tasks
      */
     protected void doDispose()
     {
         // do nothing by default
     }
 
     /**
      * @return the parent or null if there is no parent.  A handler may
      * delegate to its parent if it has one or override behaviour itself.
      */
     public AbstractDTViewHandler getParent() {
         return parent;
     }
 
     /**
      * @param parent
      */
     public void setParent(AbstractDTViewHandler parent) {
         this.parent = parent;
     }
 
     /**
      * IMPORTANT: this method must not return null. All view creation will fail
      * for this handler when createVersionStamp() returns null. create() will
      * throw ViewHandlerException.
      * 
      * @param facesContext
      * @param viewId
      * @return a new version stamp for the facesContext/viewId. The version
      *         stamp needs to be unique for all instances of viewId only, not
      *         all instances of component trees everywhere in the system.
      */
     protected abstract VersionStamp createVersionStamp(
             final DTFacesContext facesContext, final String viewId);
 
     /**
      * IMPORTANT: this method must not return null. All view creation will fail
      * for this handler when createStalenessAdvisor returns null.
      * 
      * @param viewRoot
      * @param facesContext
      * @param viewId
      * @return a new staleness advisor.
      */
     protected abstract StalenessAdvisor createStalenessAdvisor(
             final DTUIViewRoot viewRoot, DTFacesContext facesContext,
             String viewId);
 
     /**
      * Called by createView immediately before returning.  Provides sub-class
      * a chance to register a fully initialized view root with any stateful
      * activities such as automatic change tracking and caching.
      * 
      * @param viewRoot
      * @param context
      * @param viewId
      */
     protected void registerView(DTUIViewRoot viewRoot, final DTFacesContext context, final String viewId)
     {
         // do nothing by default
     }
 
     public String getViewId(final DTFacesContext context, final IResource res)
     {
         // TODO: sync with WebrootUtil?
         String strWebrootPath = ""; //$NON-NLS-1$
         final IProject project = res.getProject();
         final IPath path = res.getFullPath();
         final IPath webContentPath = getWebContentPath(project);
         if (webContentPath != null && webContentPath.isPrefixOf(path))
         {
             final int start = path.matchingFirstSegments(webContentPath);
             final String[] segments = path.segments();
             for (int i = start, n = path.segmentCount(); i < n; i++)
             {
                 strWebrootPath = strWebrootPath + PATH_SEPARATOR + segments[i];
             }
         }
         return strWebrootPath;
     }
 
     private IPath getWebContentPath(final IProject project)
     {
         if (project != null)
         {
            return ComponentCore.createComponent(project).getRootFolder()
                    .getUnderlyingFolder().getFullPath();
         }
        return null;
     }
 
     /**
      * A default DTUIViewRoot that represents an uncreatable or uninitializable
      * view root.
      *
      */
     protected static final class NullViewRoot extends DTUIViewRoot
     {
         /**
          * 
          */
         private static final long serialVersionUID = 1187035338610719171L;
 
         /**
          */
         protected NullViewRoot()
         {
             super(null, null, new ComponentTypeInfo("", "", "","")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
         }
 
         @Override
         public IAdaptable getServices()
         {
             return new IAdaptable()
             {
                 public Object getAdapter(Class adapter)
                 {
                     // no services
                     return null;
                 }
             };
         }
 
 		@Override
 		protected Map<String, ISymbol> doGetMapForScope(int scopeMask) {			
 			return Collections.emptyMap();
 		}
         
     }
 
     /**
      * Provides a simple time-based stamp that makes almost impossible to create
      * the same timestamp twice. To do so, two threads would need to get the
      * same system simulataneously and get same number back from the same
      * gaussian distribution.
      * 
      * @author cbateman
      * 
      */
     protected static class TimeBasedVersionStamp extends VersionStamp
     {
         /**
          * 
          */
         private static final long serialVersionUID = 5557828245936568977L;
         private final long        timestamp;
         private final long        randomStamp;
 
         /**
          * 
          */
         public TimeBasedVersionStamp()
         {
             super();
             // use the millisecond time, since it is offset from a known
             // point on all platform, whereas no guarantee is made about
             // the nano time value.
             this.timestamp = System.currentTimeMillis();
             // this protects against two threads constructing two time
             // stamp objects "simulataneously" and having the same time based
             // stamp. Math.random is thread-safe.
             this.randomStamp = (long) (Long.MAX_VALUE * Math.random());
         }
 
         @Override
         public int hashCode()
         {
             return (int) (timestamp ^ randomStamp);
         }
 
         @Override
         protected boolean isEqual(final VersionStamp other)
         {
             return other instanceof TimeBasedVersionStamp
                     && (timestamp == ((TimeBasedVersionStamp) other).timestamp)
                     && (randomStamp == ((TimeBasedVersionStamp) other).randomStamp);
         }
     }
 
     /**
      * Returns a default staleness advisor that always returns false.
      * 
      * @author cbateman
      * 
      */
     protected static final class NullStalenessAdvisor extends StalenessAdvisor
     {
         /**
          * 
          */
         private static final long serialVersionUID = 5946420948320047613L;
 
         @Override
         public boolean isStale()
         {
             return false;
         }
 
         @Override
         public void addListener(final StalenessListener listener)
         {
             // do nothing
         }
 
         @Override
         public void removeListener(final StalenessListener listener)
         {
             // do nothing
         }
         
         public boolean isAccessible()
         {
             return true;
         }
     }
 }
