 /*
  File: NetworkViewManager.java
 
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
 package cytoscape.view;
 
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.CytoscapeInit;
 
 import ding.view.DGraphView;
 import ding.view.InnerCanvas;
 
 import java.awt.Component;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowFocusListener;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyVetoException;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.swing.JDesktopPane;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.JTabbedPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.InternalFrameAdapter;
 import javax.swing.event.InternalFrameEvent;
 import javax.swing.event.InternalFrameListener;
 import javax.swing.event.SwingPropertyChangeSupport;
 
 /**
  * 
  */
 public class NetworkViewManager implements PropertyChangeListener,
 		InternalFrameListener, WindowFocusListener, ChangeListener {
 	private JDesktopPane desktopPane;
 
 	private Map<String, JInternalFrame> networkViewMap;
 
 	private Map<JInternalFrame, String> componentMap;
 
 	private Map<String, InternalFrameComponent> internalFrameComponentMap;
 
 	protected CytoscapeDesktop cytoscapeDesktop;
 
 	protected SwingPropertyChangeSupport pcs;
 	protected int MINIMUM_WIN_WIDTH = 200;
 	protected int MINIMUM_WIN_HEIGHT = 200;
 
 	/**
 	 * @deprecated view_type is no longer used. Use the other constructor. Will
 	 *             be removed Aug 2008.
 	 */
 	public NetworkViewManager(CytoscapeDesktop desktop, int view_type) {
 		this(desktop);
 	}
 
 	/**
 	 * Creates a new NetworkViewManager object.
 	 * 
 	 * @param desktop
 	 *            DOCUMENT ME!
 	 */
 	public NetworkViewManager(CytoscapeDesktop desktop) {
 		this.cytoscapeDesktop = desktop;
 		desktopPane = new JDesktopPane();
 		pcs = new SwingPropertyChangeSupport(this);
 
 		// add Help hooks
 		cytoscapeDesktop.getHelpBroker().enableHelp(desktopPane,
 				"network-view-manager", null);
 
 		networkViewMap = new HashMap<String, JInternalFrame>();
 		componentMap = new HashMap<JInternalFrame, String>();
 		internalFrameComponentMap = new HashMap<String, InternalFrameComponent>();
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @return DOCUMENT ME!
 	 */
 	public SwingPropertyChangeSupport getSwingPropertyChangeSupport() {
 		return pcs;
 	}
 
 	/**
 	 * @deprecated Tabbed view is no longer used. Will be removed Aug 2008.
 	 */
 	public JTabbedPane getTabbedPane() {
 		return null;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @return DOCUMENT ME!
 	 */
 	public JDesktopPane getDesktopPane() {
 		return desktopPane;
 	}
 
 	/**
 	 * Given a CyNetworkView, returns the InternalFrameComponent that wraps it.
 	 * 
 	 * @param view
 	 *            CyNetworkView
 	 * @return InternalFrameComponent
 	 * @throws IllegalArgumentException
 	 */
 	public InternalFrameComponent getInternalFrameComponent(CyNetworkView view)
 			throws IllegalArgumentException {
 		// check args
 		if (view == null) {
 			throw new IllegalArgumentException(
 					"NetworkViewManager.getInternalFrameComponent(), argument is null");
 		}
 
 		// outta here
 		return internalFrameComponentMap.get(view.getIdentifier());
 	}
 
 	/**
 	 * Given a CyNetworkView, returns the internal frame.
 	 * 
 	 * @param view
 	 *            CyNetworkView
 	 * @return InternalFrameComponent
 	 * @throws IllegalArgumentException
 	 */
 	public JInternalFrame getInternalFrame(CyNetworkView view)
 			throws IllegalArgumentException {
 		// check args
 		if (view == null) {
 			throw new IllegalArgumentException(
 					"NetworkViewManager.getInternalFrame(), argument is null");
 		}
 
 		// outta here
 		return networkViewMap.get(view.getIdentifier());
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @param network
 	 *            DOCUMENT ME!
 	 */
 	public void updateNetworkTitle(CyNetwork network) {
 		JInternalFrame frame = networkViewMap.get(network.getIdentifier());
 		if (frame != null) {
 			frame.setTitle(network.getTitle());
 			frame.repaint();
 		}
 	}
 
 	/**
 	 * @deprecated Will no longer support ChangeListener. Will be removed August
 	 *             2008.
 	 */
 	public void stateChanged(ChangeEvent e) {
 	}
 
 	/**
 	 * Fire Events when a Managed Network View gets the Focus.
 	 */
 	public void internalFrameActivated(InternalFrameEvent e) {
 		String network_id = componentMap.get(e.getInternalFrame());
 
 		if (network_id == null) {
 			return;
 		}
 
 		//System.out.println("NetworkViewManager: firing NETWORK_VIEW_FOCUSED (intenalFrameActivate)");
 		firePropertyChange(CytoscapeDesktop.NETWORK_VIEW_FOCUSED, null,
 				network_id);
 	}
 
 	/**
 	 * Fire Events when a Managed Network View gets the Focus.
 	 */
 	public void internalFrameOpened(InternalFrameEvent e) {
 		internalFrameActivated(e);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @param e
 	 *            DOCUMENT ME!
 	 */
 	public void internalFrameClosed(InternalFrameEvent e) {
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @param e
 	 *            DOCUMENT ME!
 	 */
 	public void internalFrameClosing(InternalFrameEvent e) {
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @param e
 	 *            DOCUMENT ME!
 	 */
 	public void internalFrameDeactivated(InternalFrameEvent e) {
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @param e
 	 *            DOCUMENT ME!
 	 */
 	public void internalFrameDeiconified(InternalFrameEvent e) {
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @param e
 	 *            DOCUMENT ME!
 	 */
 	public void internalFrameIconified(InternalFrameEvent e) {
 	}
 
 	/**
 	 * @deprecated Will no longer support WindowFocusListener. Will be removed
 	 *             August 2008.
 	 */
 	public void windowGainedFocus(WindowEvent e) {
 	}
 
 	/**
 	 * @deprecated Will no longer support WindowFocusListener. Will be removed
 	 *             August 2008.
 	 */
 	public void windowLostFocus(WindowEvent e) {
 	}
 
 	/**
 	 * This handles all of the incoming PropertyChangeEvents. If you are going
 	 * to have multiple NetworkViewManagers, then this method should be extended
 	 * such that the desired behaviour is achieved, assuming of course that you
 	 * want your NetworkViewManagers to behave differently.
 	 */
 	public void propertyChange(PropertyChangeEvent e) {
 
 		// handle focus event
 		if (e.getPropertyName() == CytoscapeDesktop.NETWORK_VIEW_FOCUS) {
 			//System.out.println("NetworkViewManager got NETWORK_VIEW_FOCUS " + e.getSource().getClass().getName());
 			String network_id = (String) e.getNewValue();
 			e = null;
 			unsetFocus(); // in case the newly focused network doesn't have a
 							// view
 			setFocus(network_id);
 
 			// hack to add transfer handlers to canvas
 			InnerCanvas canvas = ((DGraphView) Cytoscape
 					.getCurrentNetworkView()).getCanvas();
 
 			if (this.getDesktopPane() != null) {
 				canvas.addTransferComponent(this.getDesktopPane());
 			}
 		}
 
 		// handle putting a newly created CyNetworkView into a Container
 		else if (e.getPropertyName() == CytoscapeDesktop.NETWORK_VIEW_CREATED) {
 			//System.out.println("NetworkViewManager got NETWORK_VIEW_CREATED " + e.getSource().getClass().getName());
 			CyNetworkView new_view = (CyNetworkView) e.getNewValue();
 			createContainer(new_view);
 			e = null;
 		}
 
 		// handle a NetworkView destroyed
 		else if (e.getPropertyName() == CytoscapeDesktop.NETWORK_VIEW_DESTROYED) {
 			CyNetworkView view = (CyNetworkView) e.getNewValue();
 			removeView(view);
 			e = null;
 		}
 	}
 
 	/**
 	 * Fires a PropertyChangeEvent
 	 */
 	public void firePropertyChange(String property_type, Object old_value,
 			Object new_value) {
 		pcs.firePropertyChange(new PropertyChangeEvent(this, property_type,
 				old_value, new_value));
 	}
 
 	/**
 	 * Used to unset the focus of all the views. This is for the situation when
 	 * a network is focused but the network doesn't have a view.
 	 */
 	protected void unsetFocus() {
 		for (JInternalFrame f : networkViewMap.values()) {
 			try {
 				f.setSelected(false);
 			} catch (PropertyVetoException pve) {
 				System.out
 						.println("NetworkViewManager: Couldn't unset focus for internal frame.");
 			}
 		}
 	}
 
 	/**
 	 * Sets the focus of the passed network, if possible The Network ID
 	 * corresponds to the CyNetworkView.getNetwork().getIdentifier()
 	 */
 	protected void setFocus(String network_id) {
 		if (networkViewMap.containsKey(network_id)) {
 			try {
 				networkViewMap.get(network_id).setIcon(false);
 				networkViewMap.get(network_id).show();
 				// fires internalFrameActivated
 				networkViewMap.get(network_id).setSelected(true);
 			} catch (Exception e) {
 				System.err.println("Network View unable to be focused");
 			}
 		}
 	}
 
 	protected void removeView(CyNetworkView view) {
 		try {
 			networkViewMap.get(view.getNetwork().getIdentifier()).dispose();
 		} catch (Exception e) {
 			System.err.println("Network View unable to be killed");
 		}
 
 		networkViewMap.remove(view.getNetwork().getIdentifier());
 	}
 
 	/**
 	 * Contains a CyNetworkView.
 	 */
 	protected void createContainer(final CyNetworkView view) {
 		if (networkViewMap.containsKey(view.getNetwork().getIdentifier())) {
 			// already contains
 			return;
 		}
 
 		// create a new InternalFrame and put the CyNetworkViews Component into
 		// it
 		JInternalFrame iframe = new JInternalFrame(view.getTitle(), true, true,
 				true, true);
 		iframe.addInternalFrameListener(new InternalFrameAdapter() {
 			public void internalFrameClosing(InternalFrameEvent e) {
 				Cytoscape.destroyNetworkView(view);
 			}
 		});
 		desktopPane.add(iframe);
 
 		// code added to support layered canvas for each CyNetworkView
 		if (view instanceof DGraphView) {
 			InternalFrameComponent internalFrameComp = new InternalFrameComponent(
 					iframe.getLayeredPane(), (DGraphView) view);
 			iframe.setContentPane(internalFrameComp);
 			internalFrameComponentMap.put(view.getNetwork().getIdentifier(),
 					internalFrameComp);
 		} else {
 			System.out
 					.println("NetworkViewManager.createContainer() - DGraphView not found!");
 			iframe.getContentPane().add(view.getComponent());
 		}
 
 		iframe.pack();
 		//iframe.setSize(400, 400);
 		// create cascade iframe
 		int x=0,y=0;
		JInternalFrame refFrame = null;
		JInternalFrame[] allFrames = desktopPane.getAllFrames();
		if ( allFrames.length > 1) {
			refFrame = allFrames[0];
		}
		
		if (refFrame != null) {
			x = refFrame.getLocation().x + 20;
			y = refFrame.getLocation().y + 20;
 		}
 
 		if (x > desktopPane.getWidth() - MINIMUM_WIN_WIDTH){
 			x = desktopPane.getWidth() - MINIMUM_WIN_WIDTH;
 		}
 		if (y > desktopPane.getHeight() - MINIMUM_WIN_HEIGHT){
 			y = desktopPane.getHeight() - MINIMUM_WIN_HEIGHT;
 		}
 
 		if (x < 0) {
 			x =0;
 		}
 		if (y < 0) {
 			y =0;
 		}
 		
 		iframe.setBounds(x, y, 400, 400);
 		
 		// maximize the frame if the specified property is set
 		try {
 			String max = CytoscapeInit.getProperties().getProperty(
 					"maximizeViewOnCreate");
 
 			if ((max != null) && Boolean.parseBoolean(max))
 				iframe.setMaximum(true);
 		} catch (PropertyVetoException pve) {
 			pve.printStackTrace();
 		}
 
 		iframe.setVisible(true);
 		iframe.addInternalFrameListener(this);
 
 		networkViewMap.put(view.getNetwork().getIdentifier(), iframe);
 		componentMap.put(iframe, view.getNetwork().getIdentifier());
 		
 		//System.out.println("NetworkViewManager: firing NETWORK_VIEW_FOCUSED (createContainer)");
 		firePropertyChange(CytoscapeDesktop.NETWORK_VIEW_FOCUSED, null, view
 				.getNetwork().getIdentifier());
 	}
 }
