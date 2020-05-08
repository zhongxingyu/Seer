 /*
  * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.ide.webengine.server;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchesListener2;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.ViewPart;
 import org.nuxeo.ide.webengine.Nuxeo;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  *
  */
 public class ServerView extends ViewPart implements ILaunchesListener2 {
 
     protected CheckboxTableViewer tv;
     protected Configuration config = null; // the active configuration
 
     protected ILaunch launch;
     
     public ILaunch getCurrentLaunch() {
         return launch;
     }
 
 
     public Configuration getConfiguration() {
         return config;
     }
 
     public void dispose() {
         super.dispose();
         DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
     }
 
     @Override
     public void init(IViewSite site, IMemento memento) throws PartInitException {
         super.init(site, memento);
         
         DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
         
         if (memento != null) {
             Configuration.init(memento);
             config = Configuration.get("default");
         }
 
         ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {
             public void resourceChanged(IResourceChangeEvent event) {
                 IResource resource = event.getResource();
                 if (resource == null) {
                     refresh(); // may be a nature modification
                 } else if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
                     if (resource.getType() == IResource.PROJECT && Nuxeo.isWebEngineProject((IProject)resource)) {
                         refresh();
                     }
                 }
 }
         });
 
         
     }
     
     @Override
     public void createPartControl(Composite parent) {
         IToolBarManager tbar = getViewSite().getActionBars().getToolBarManager();
         tbar.add(new DebugAction(this));
         tbar.add(new RunAction(this));
         tbar.add(new TerminateAction(this));
         tbar.add(new Separator());
         tbar.add(new RefreshAction(this));
         tbar.add(new SettingsAction(this));        
         
         //tbar.u
         
         tv = CheckboxTableViewer.newCheckList(parent, SWT.BORDER);
         PluginProvider provider = new PluginProvider();
         tv.setContentProvider(provider);
         tv.setLabelProvider(provider);
 
         tv.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {                
                 config.projects = new ArrayList<IProject>();
                 for (Object o : tv.getCheckedElements()) {
                     config.projects.add((IProject)o);
                 }
             }  
         });
         
         refresh();
     }
     
     public void refresh() {
         if (tv != null) {
             tv.setInput(ResourcesPlugin.getWorkspace());
             tv.setCheckedElements(config.projects.toArray());
         }
         updateActions();
     }
     
     
 
     @Override
     public void setFocus() {
     }
 
     @Override
     public void saveState(IMemento memento) {
         super.saveState(memento);
         Configuration.store(memento);
     }
     
     public void updateActions() {
         Display display = Display.getCurrent();
         if (display == null) {
             Display.getDefault().asyncExec(new Runnable() {
                 public void run() {
                     safeUpdateActions();
                 } 
             });
         } else {
             safeUpdateActions();
         }
     }
     
     public void safeUpdateActions() {
         IToolBarManager tbar = getViewSite().getActionBars().getToolBarManager();
         for (IContributionItem item :  tbar.getItems()) {
             item.update(IAction.ENABLED);
         }        
     }
     
     
     public void launchesAdded(ILaunch[] launches) {
         for (ILaunch launch : launches) {
             if (launch.getLaunchConfiguration().getName().indexOf("WebEngine") > -1) {
                 this.launch = launch;
                 if (!this.launch.isTerminated()) {
                     updateActions();
                 }
             }
         }
     }
     
     
 
     /* (non-Javadoc)
      * @see org.eclipse.debug.core.ILaunchesListener2#launchesTerminated(org.eclipse.debug.core.ILaunch[])
      */
     public void launchesTerminated(ILaunch[] launches) {
         for (ILaunch launch : launches) {
             if (this.launch == launch) {
                 this.launch = null;
                 updateActions();
             }
         }        
     }
     
     public void launchesRemoved(ILaunch[] launches) {
         // do nothing
     }
 
     public void launchesChanged(ILaunch[] launches) {
         // do nothing
     }
 
 }
