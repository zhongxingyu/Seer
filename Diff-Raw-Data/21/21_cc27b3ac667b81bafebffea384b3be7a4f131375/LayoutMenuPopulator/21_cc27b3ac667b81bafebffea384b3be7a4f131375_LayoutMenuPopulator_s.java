 /*
   File: LayoutMenuManager.java
 
   Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
   The Cytoscape Consortium is:
   - Institute for Systems Biology
   - University of California San Diego
   - Memorial Sloan-Kettering Cancer Center
   - Institut Pasteur
   - Agilent Technologies
 
   This library is free software; you can redistribute it and/or modify it
   under the terms of the GNU Lesser General Public License as published
   by the Free Software Foundation; either version 2.1 of the License, or
   any later version.
 
   This library is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
   MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
   documentation provided hereunder is on an "as is" basis, and the
   Institute for Systems Biology and the Whitehead Institute
   have no obligations to provide maintenance, support,
   updates, enhancements or modifications.  In no event shall the
   Institute for Systems Biology and the Whitehead Institute
   be liable to any party for direct, indirect, special,
   incidental or consequential damages, including lost profits, arising
   out of the use of this software and its documentation, even if the
   Institute for Systems Biology and the Whitehead Institute
   have been advised of the possibility of such damage.  See
   the GNU Lesser General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License
   along with this library; if not, write to the Free Software Foundation,
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package org.cytoscape.internal.layout.ui;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.JMenu;
 import javax.swing.event.MenuEvent;
 import javax.swing.event.MenuListener;
 
 import org.cytoscape.application.CyApplicationManager;
 import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.StringEnableSupport;
 import org.cytoscape.event.CyEventHelper;
 import org.cytoscape.internal.task.DynamicTaskFactoryProvisioner;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNode;
 import org.cytoscape.task.NetworkViewTaskFactory;
 import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
 import org.cytoscape.view.layout.AbstractLayoutContext;
 import org.cytoscape.view.layout.CyLayoutAlgorithm;
 import org.cytoscape.view.layout.CyLayoutContext;
 import org.cytoscape.view.model.CyNetworkView;
 import org.cytoscape.view.model.CyNetworkViewManager;
 import org.cytoscape.view.model.View;
 import org.cytoscape.work.TaskFactory;
 import org.cytoscape.work.TaskIterator;
 import org.cytoscape.work.swing.DynamicSubmenuListener;
 import org.cytoscape.work.swing.SubmenuTaskManager;
 import org.cytoscape.work.undo.UndoSupport;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class LayoutMenuPopulator {
 
 	private CyApplicationManager appMgr;
 	private final CyNetworkViewManager networkViewManager;
 	private SubmenuTaskManager tm;
 	private CySwingApplication swingApp;
 	private Map<CyLayoutAlgorithm,MenuListener> listenerMap = new HashMap<CyLayoutAlgorithm,MenuListener>();
 	private Set<JMenu> parentMenuSet = new HashSet<JMenu>();
 	private UndoSupport undo;
 	private CyEventHelper eventHelper;
 	private DynamicTaskFactoryProvisioner factoryProvisioner;
 
 	private static final Logger logger = LoggerFactory.getLogger(LayoutMenuPopulator.class);
 
 	public LayoutMenuPopulator(CySwingApplication swingApp, CyApplicationManager appMgr, SubmenuTaskManager tm, UndoSupport undo, CyEventHelper eventHelper, final CyNetworkViewManager networkViewManager) {
 		this.appMgr = appMgr;
 		this.networkViewManager = networkViewManager;
 		this.tm = tm;
 		this.swingApp = swingApp;
 		this.undo = undo;
 		this.eventHelper = eventHelper;
 		this.factoryProvisioner = new DynamicTaskFactoryProvisioner(appMgr);
 	}
 
 	public <T extends AbstractLayoutContext> void addLayout(CyLayoutAlgorithm<T> layout, Map props) {
 		String prefMenu = getPreferredMenu(props); 
 
 		String menuName = (String)props.get("title");
 		if (menuName == null) {
 			logger.warn("Failed to find menu title for layout algorithm: " + layout + " NOT adding!");
 			return;
 		}
 
 		// TODO: Can we assume all layouts derive from AbstractLayoutAlgorithm?
 		//       That class provides submenu bits that the framework needs so
 		//       Implementors of CyLayoutAlgorithm would need to mimic that
 		//       somehow if they choose to implement from scratch.
 		UndoSupportTaskFactory<T> taskFactory = new UndoSupportTaskFactory<T>((AbstractLayoutAlgorithm<T>) layout, undo, eventHelper);
 		T context = taskFactory.createLayoutContext();
 		TaskFactory provisioner = factoryProvisioner.createFor(wrapWithContext(taskFactory, context));
 		// get the submenu listener from the task manager
 		DynamicSubmenuListener submenu = tm.getConfiguration(provisioner, context);
 		submenu.setMenuTitle(menuName);
 
 		// now wrap it in a menulistener that sets the current network view for the layout
 		MenuListener ml = new NetworkViewMenuListener( submenu, appMgr, networkViewManager, "networkAndView");
 
 		JMenu parentMenu = swingApp.getJMenu(prefMenu);
 		parentMenu.addMenuListener(ml);
 
 		if ( !parentMenuSet.contains(parentMenu) ) {
 			JMenu layoutMenu = swingApp.getJMenu("Layout");
 			layoutMenu.addMenuListener( new LayoutMenuEnabler(parentMenu,"networkAndView",appMgr, networkViewManager) );
 			parentMenuSet.add(parentMenu);
 		}
 
 		listenerMap.put(layout,ml);
 	}
 
 	private <T extends CyLayoutContext> NetworkViewTaskFactory wrapWithContext(final CyLayoutAlgorithm<T> layout, final T tunableContext) {
 		return new NetworkViewTaskFactory() {
 			@Override
 			public boolean isReady(CyNetworkView networkView) {
 				return layout.isReady(networkView, tunableContext, getAffectedNodes(tunableContext, networkView));
 			}
 			
 			@Override
 			public TaskIterator createTaskIterator(CyNetworkView networkView) {
 				return layout.createTaskIterator(networkView, tunableContext, getAffectedNodes(tunableContext, networkView));
 			}
 		};
 	}
 	
 	static Set<View<CyNode>> getAffectedNodes(CyLayoutContext context, CyNetworkView networkView) {
 		if (context.useOnlySelectedNodes()) {
 			CyNetwork network = networkView.getModel();
 			Set<View<CyNode>> views = new HashSet<View<CyNode>>();
 			for (View<CyNode> view : networkView.getNodeViews()) {
 				if (network.getRow(view.getModel()).get(CyNetwork.SELECTED, Boolean.class)) {
 					views.add(view);
 				}
 			}
 			return views;
 		}
 		return Collections.emptySet();
 	}
 	
 	public void removeLayout(CyLayoutAlgorithm layout, Map props) {
 		String prefMenu = getPreferredMenu(props); 
 		
 		JMenu parentMenu = swingApp.getJMenu(prefMenu);
 		parentMenu.removeMenuListener( listenerMap.get(layout) );
 	}
 
 	private String getPreferredMenu(Map props) {
 		String prefMenu = (String)props.get("preferredMenu");
 		if (prefMenu == null )
 			prefMenu = "Layout";	
 		return prefMenu;
 	}
 
 	private class LayoutMenuEnabler implements MenuListener {
		private final StringEnableSupport parentMenuSupport; 
 		LayoutMenuEnabler(JMenu parentMenu, String enableFor, CyApplicationManager appMgr, final CyNetworkViewManager networkViewManager) {
			parentMenuSupport = new StringEnableSupport(parentMenu,enableFor,appMgr, networkViewManager);
 		}
 		public void menuSelected(MenuEvent m) {
 			parentMenuSupport.updateEnableState();
 		}
 		public void menuDeselected(MenuEvent m) {}
 		public void menuCanceled(MenuEvent m) {}
 	}
 }
