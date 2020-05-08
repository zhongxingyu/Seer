 // vim: set ts=2: */
 package cytoscape.layout;
 
 import cytoscape.CytoscapeInit;
 import cytoscape.util.ModulePropertiesImpl;
 
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.util.Iterator;
 
 import javax.swing.JPanel;
 import javax.swing.BoxLayout;
 
 
 /**
  * The LayoutProperties class is a helper class to support the management
  * of settings and properties for layout algorithms that implement
  * CyLayoutAlgorithm or extend AbstractLayout.  LayoutProperties objects
  * maintain a list of Tunables that are supplied by the individual
  * algorithms.  Each Tunable represents a value that should be loaded
  * from the Cytoscape properties file, and made available as a setting
  * in the LayoutSettingsDialog.  Tunables are added to the LayoutProperties
  * using the <tt>add</tt> method and are retrieved with the <tt>get</tt>
  * method.
  */
 public class LayoutProperties extends ModulePropertiesImpl implements TunableListener {
 	JPanel tunablesPanel = null;
 
 	/**
 	 * Constructor.
 	 *
 	 * @param propertyPrefix String representing the prefix to be used
 	 *                       when pulling properties from the property
 	 *                       list.
 	 */
 	public LayoutProperties(String propertyPrefix) {
 		super(propertyPrefix, "layout");
 	}
 
 
 	/**
 	 * This method returns a JPanel that represents the all of the Tunables
 	 * associated with this LayoutProperties object.
 	 *
 	 * @return JPanel that contains all of the Tunable widgets
 	 */
 	public JPanel getTunablePanel() {
 		if (tunablesPanel != null) return tunablesPanel;
 
 		tunablesPanel = new JPanel();
 		BoxLayout box = new BoxLayout(tunablesPanel, BoxLayout.Y_AXIS);
 		tunablesPanel.setLayout(box);
 
 		addSubPanels(tunablesPanel, tunablesList.iterator(), new Integer(100000), this);
 
 		tunablesPanel.validate();
 		return tunablesPanel;
 	}
 
 	/**
  	 * This method is called to update the panel for this property sheet.
  	 */
 	public void updateTunablePanel() {
 		if (tunablesPanel == null) return;
 
 		tunablesPanel.removeAll();
 
 		addSubPanels(tunablesPanel, tunablesList.iterator(), new Integer(100000), null);
 
 		tunablesPanel.doLayout();
 		tunablesPanel.validate();
 		// now signal any listeners that we've resized
 		ComponentListener[] resizeListeners = tunablesPanel.getComponentListeners();
 		if (resizeListeners == null) return;
 
 		for (int i = 0; i < resizeListeners.length; i++) {
 			resizeListeners[i].componentResized(new ComponentEvent(tunablesPanel, ComponentEvent.COMPONENT_RESIZED));
 		}
 	}
 
 	private void addSubPanels(JPanel panel, Iterator<Tunable>iter, Object count, TunableListener listener) {
 		int groupCount = ((Integer)count).intValue();
 		for (int n = 0; n < groupCount; n++) {
 			if (!iter.hasNext()) {
 				return;
 			}
 			// Get the next tunable
 			Tunable tunable = iter.next();
 
			if (panel == null) 
				continue;

 			JPanel p = tunable.getPanel();
 			if (tunable.getType() == Tunable.GROUP) {
 				if (!tunable.checkFlag(Tunable.COLLAPSABLE)) {
 					addSubPanels(p, iter, tunable.getValue(), listener);
 				} else {
 					tunable.addTunableValueListener(listener); // We need to listen to collapse/expand changes
 					Boolean collapsed = (Boolean)tunable.getLowerBound();
 					if (!collapsed.booleanValue())
 						addSubPanels(p, iter, tunable.getValue(), listener);
 					else 
 						addSubPanels(null, iter, tunable.getValue(), listener);
 				}
 			}
 			if (p != null)
 				panel.add(p);
 		}
 	}
 
 	public void tunableChanged(Tunable tunable) {
 		// In our case, we just update our display
 		updateTunablePanel();
 	}
 }
