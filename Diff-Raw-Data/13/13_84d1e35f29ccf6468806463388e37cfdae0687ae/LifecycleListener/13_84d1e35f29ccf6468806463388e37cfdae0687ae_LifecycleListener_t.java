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
 package org.eclipse.jst.jsf.common.internal.resource;
 
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.jst.jsf.common.internal.ITestTracker;
 import org.eclipse.jst.jsf.common.internal.ITestTracker.Event;
 import org.eclipse.jst.jsf.common.internal.resource.IResourceLifecycleListener.EventResult;
 import org.eclipse.jst.jsf.common.internal.resource.ResourceLifecycleEvent.EventType;
 import org.eclipse.jst.jsf.common.internal.resource.ResourceLifecycleEvent.ReasonType;
 
 /**
  * Listens to resource changes and fires lifecycle events
  * 
  * @author cbateman
  * 
  */
 public class LifecycleListener extends ImmutableLifecycleListener implements
         IResourceChangeListener
 {
    private static boolean                           ENABLE_TEST_TRACKING = false;
     private static long                                    _seqId;
 
     private final CopyOnWriteArrayList<IResource>          _resources;
     final CopyOnWriteArrayList<IResourceLifecycleListener> _listeners;
     private AtomicBoolean                                  _isDisposed          = new AtomicBoolean(
                                                                                         false);
     private ITestTracker                                   _testTracker;                       // ==
                                                                                                 // null;
                                                                                                 // initialized
                                                                                                 // by
                                                                                                 // setter
                                                                                                 // injection
 
     /**
      * Initialize an inactive lifecycle listener. A workspace listener will not
      * be installed by this constructor. The object created using this
      * constructor will not fire any events until addResource is called at least
      * once to add a target resource
      */
     public LifecycleListener()
     {
         _resources = new CopyOnWriteArrayList<IResource>();
         _listeners = new CopyOnWriteArrayList<IResourceLifecycleListener>();
     }
 
     /**
      * Create a new lifecycle listener for the res
      * 
      * @param res
      */
     public LifecycleListener(final IResource res)
     {
         this();
         _resources.add(res);
         ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
     }
 
     /**
      * @param resources
      */
     public LifecycleListener(final List<IResource> resources)
     {
         this();
         _resources.addAll(resources);
         ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
     }
 
     /**
      * @param testTracker
      */
     public final void setTestTracker(final ITestTracker testTracker)
     {
         _testTracker = testTracker;
     }
 
     /**
     * @param newValue
     */
    protected final void setEnableTracing(final boolean newValue)
    {
        ENABLE_TEST_TRACKING = newValue;
    }
    /**
      * Adds listener to the list of objects registered to receive lifecycle
      * events for this resource. Only adds the listener if it is not already in
      * the list.
      * 
      * Method is thread-safe and may block the caller
      * 
      * Throws {@link IllegalStateException} if isDisposed() == true
      * 
      * @param listener
      */
     public void addListener(final IResourceLifecycleListener listener)
     {
         if (isDisposed())
         {
             throw new IllegalStateException();
         }
         _listeners.addIfAbsent(listener);
     }
 
     /**
      * Removes listener from the list of registered listeners
      * 
      * Method is thread-safe and may block the caller
      * 
      * Throws {@link IllegalStateException} if isDisposed() == true
      * 
      * @param listener
      */
     public void removeListener(final IResourceLifecycleListener listener)
     {
         if (isDisposed())
         {
             throw new IllegalStateException();
         }
         _listeners.remove(listener);
     }
 
     /**
      * @param res
      */
     public void addResource(final IResource res)
     {
         synchronized (_resources)
         {
             // don't add any resources if we've disposed before acquiring the
             // lock
             if (isDisposed())
             {
                 return;
             }
 
             final int preSize = _resources.size();
             if (!_resources.contains(res))
             {
                 _resources.add(res);
             }
 
             // if the size of the array was 0
             // and is now greater, make sure the listener is added
             if (preSize == 0 && _resources.size() > 0)
             {
                 ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
             }
         }
     }
 
     /**
      * If there are no longer resources being targeted, the resource change
      * listener will be removed.
      * 
      * @param res
      */
     public void removeResource(final IResource res)
     {
         synchronized (_resources)
         {
             // don't bother with this stuff if we're disposed.
             if (isDisposed())
             {
                 return;
             }
             _resources.remove(res);
 
             // if there are no longer target resources,
             // remove the workspace listener
             if (_resources.size() == 0)
             {
                 ResourcesPlugin.getWorkspace().removeResourceChangeListener(
                         this);
             }
         }
     }
 
     /**
      * Release the resource change listener
      */
     public void dispose()
     {
         if (_isDisposed.compareAndSet(false, true))
         {
             // ensure that add/removeResource don't cause races to add or
             // remove the resource listener
             synchronized (_resources)
             {
                 // remove first to minimize the chance that the listener will
                 // be triggered during the remainder of dispose
                 ResourcesPlugin.getWorkspace().removeResourceChangeListener(
                         this);
                 _resources.clear();
             }
         }
     }
 
     /**
      * @return true if the listener has been disposed
      */
     public boolean isDisposed()
     {
         return _isDisposed.get();
     }
 
     public void resourceChanged(final IResourceChangeEvent event)
     {
         final long seqId = _seqId++;
 
        if (_testTracker != null && ENABLE_TEST_TRACKING)
         {
             _testTracker.fireEvent(Event.START_TRACKING, seqId,
                     "trackMethod_resourceChanged"); //$NON-NLS-1$
         }
 
         assert (!isDisposed());
 
         switch (event.getType())
         {
             case IResourceChangeEvent.PRE_CLOSE:
             {
                 final IProject proj = (IProject) event.getResource();
 
                 // must use iterator to ensure copy on write behaviour
                 for (final IResource res : _resources)
                 {
                     if (proj == res || proj == res.getProject())
                     {
                         fireLifecycleEvent(new ResourceLifecycleEvent(res,
                                 EventType.RESOURCE_INACCESSIBLE,
                                 ReasonType.RESOURCE_PROJECT_CLOSED));
                     }
                 }
             }
             break;
 
             case IResourceChangeEvent.PRE_DELETE:
             {
                 final IProject proj = (IProject) event.getResource();
 
                 // must use iterator to ensure copy on write behaviour
                 for (final IResource res : _resources)
                 {
                     // if the resource being tracked is the resource being
                     // deleted,
                     // then fire a resource delete event
                     if (proj == res)
                     {
                         fireLifecycleEvent(new ResourceLifecycleEvent(res,
                                 EventType.RESOURCE_INACCESSIBLE,
                                 ReasonType.RESOURCE_DELETED));
                     }
                     // if the resource being tracked is a resource in the
                     // project being
                     // deleted, then fire a project deleted event
                     else if (proj == res.getProject())
                     {
                         fireLifecycleEvent(new ResourceLifecycleEvent(res,
                                 EventType.RESOURCE_INACCESSIBLE,
                                 ReasonType.RESOURCE_PROJECT_DELETED));
                     }
                 }
             }
             break;
 
             case IResourceChangeEvent.POST_CHANGE:
             {
                 for (final IResource res : _resources)
                 {
                     IResourceDelta delta = event.getDelta();
 
                     // long seqId2 = _seqId++;
                     // if (ENABLE_TEST_TRACKING && _testTracker != null)
                     // {
                     // _testTracker.fireEvent(Event.START_TRACKING, seqId2,
                     // "testFindMember");
                     // }
                     // only care about post change events to resources
                     // that we are tracking
                     delta = delta.findMember(res.getFullPath());
 
                     if (delta != null)
                     {
                         visit(delta);
                     }
 
                     // if (ENABLE_TEST_TRACKING && _testTracker != null)
                     // {
                     // _testTracker.fireEvent(Event.STOP_TRACKING, seqId2,
                     // "testFindMember");
                     // }
                 }
             }
             break;
 
             default:
                 // do nothing
                 // we only handle these three
         }
 
         if (ENABLE_TEST_TRACKING && _testTracker != null)
         {
             _testTracker.fireEvent(Event.STOP_TRACKING, seqId,
                     "trackMethod_resourceChanged"); //$NON-NLS-1$
         }
     }
 
     private void fireLifecycleEvent(final ResourceLifecycleEvent event)
     {
         boolean disposeAfter = false;
 
         // NOTE: must use iterator through _listeners so that
         // CopyOnWriteArrayList protects us from concurrent modification
         for (final IResourceLifecycleListener listener : _listeners)
         {
             final EventResult result = listener.acceptEvent(event);
             disposeAfter |= result.getDisposeAfterEvent();
         }
 
         if (disposeAfter)
         {
             dispose();
         }
     }
 
     private void visit(final IResourceDelta delta)
     {
         assert (!isDisposed());
 
         final IResource res = delta.getResource();
 
         // the wkspace root is a special case since even though
         // it is registered as the target resource, we are interested
         // in new projects created in the root
         if (res.getType() == IResource.ROOT)
         {
             handleWorkspaceRoot(delta);
         }
 
         switch (delta.getKind())
         {
             case IResourceDelta.CHANGED:
             {
                 // the contents of the file have changed
                 if ((delta.getFlags() & IResourceDelta.CONTENT) != 0)
                 {
                     fireLifecycleEvent(new ResourceLifecycleEvent(res,
                             EventType.RESOURCE_CHANGED,
                             ReasonType.RESOURCE_CHANGED_CONTENTS));
                 }
             }
             break;
             case IResourceDelta.REMOVED:
             {
                 fireLifecycleEvent(new ResourceLifecycleEvent(res,
                         EventType.RESOURCE_INACCESSIBLE,
                         ReasonType.RESOURCE_DELETED));
             }
             break;
         }
     }
 
     private void handleWorkspaceRoot(final IResourceDelta delta)
     {
         for (final IResourceDelta childDelta : delta
                 .getAffectedChildren(IResourceDelta.ADDED))
         {
             final IResource res = childDelta.getResource();
             if ((childDelta.getFlags() & IResourceDelta.OPEN) != 0 &&
             // project was just opened
                     res.getType() == IResource.PROJECT)
             {
                 fireLifecycleEvent(new ResourceLifecycleEvent(res,
                         EventType.RESOURCE_ADDED, ReasonType.PROJECT_OPENED));
             }
         }
     }
 }
