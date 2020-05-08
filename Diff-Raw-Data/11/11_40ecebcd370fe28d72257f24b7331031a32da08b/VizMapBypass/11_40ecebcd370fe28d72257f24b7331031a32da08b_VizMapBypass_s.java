 /*
  File: VizMapBypass.java
 
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
 package cytoscape.visual.ui;
 
 import giny.model.GraphObject;
 
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 import cytoscape.logger.CyLogger;
 import cytoscape.visual.VisualMappingManager;
 import cytoscape.visual.VisualPropertyType;
 import cytoscape.visual.parsers.ObjectToString;
 
 
 /**
  * An abstract class providing common methods and data structures to the
  * Node and Edge bypass classes.
  */
 abstract class VizMapBypass {
 	protected Frame parent = Cytoscape.getDesktop();
 	protected VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
 	protected CyAttributes attrs = null;
 	protected GraphObject graphObj = null;
 	protected CyLogger logger = CyLogger.getLogger(VizMapBypass.class);
 
 	abstract protected List<String> getBypassNames();
 
 	protected void addResetAllMenuItem(JMenu menu) {
 		JMenuItem jmi = new JMenuItem(new AbstractAction("Reset All") {
 				public void actionPerformed(ActionEvent e) {
 					List<String> names = getBypassNames();
 					String id = graphObj.getIdentifier();
 
 					for (String attrName : names)
 						if (attrs.hasAttribute(id, attrName))
 							attrs.deleteAttribute(id, attrName);
 
					vmm.getNetworkView().redrawGraph(false, true);
 					BypassHack.finished();
 				}
 			});
 		menu.add(jmi);
 	}
 
 	protected void addResetMenuItem(JMenu menu, final VisualPropertyType type) {
 		JMenuItem jmi = new JMenuItem(new AbstractAction("[ Reset " + type.getName() + " ]") {
 				public void actionPerformed(ActionEvent e) {
 					String id = graphObj.getIdentifier();
 
 					if (attrs.hasAttribute(id, type.getBypassAttrName()))
 						attrs.deleteAttribute(id, type.getBypassAttrName());
 
					vmm.getNetworkView().redrawGraph(false, true);
 					BypassHack.finished();
 				}
 			});
 		menu.add(jmi);
 	}
 
 	protected void addMenuItem(JMenu menu, final VisualPropertyType type) {
 		final JMenuItem jmi = new JCheckBoxMenuItem(new AbstractAction(type.getName()) {
 				public void actionPerformed(ActionEvent e) {
 					Object obj = null;
 
 					try {
 						obj = type.showDiscreteEditor();
 					} catch (Exception ex) {
 						logger.warn("Unable to show descrete editor", ex);
 						obj = null;
 					}
 
 					if (obj == null)
 						return;
 
 					String val = ObjectToString.getStringValue(obj);
 					attrs.setAttribute(graphObj.getIdentifier(), type.getBypassAttrName(), val);
					vmm.getNetworkView().redrawGraph(false, true);
 					BypassHack.finished();
 				}
 			});
 
 		menu.add(jmi);
 		
 		// Check node size lock state 
 		if(type.equals(VisualPropertyType.NODE_SIZE)) {
 			if(Cytoscape.getVisualMappingManager().getVisualStyle().getNodeAppearanceCalculator().getNodeSizeLocked() == false) {
 				jmi.setEnabled(false);
 			}
 		} else if(type.equals(VisualPropertyType.NODE_WIDTH) || type.equals(VisualPropertyType.NODE_HEIGHT)) {
 			if(Cytoscape.getVisualMappingManager().getVisualStyle().getNodeAppearanceCalculator().getNodeSizeLocked() == true) {
 				jmi.setEnabled(false);
 			}
 		}
 
 		String attrString = attrs.getStringAttribute(graphObj.getIdentifier(),
 		                                             type.getBypassAttrName());
 
 		if ((attrString == null) || (attrString.length() == 0))
 			jmi.setSelected(false);
 		else {
 			jmi.setSelected(true);
 			addResetMenuItem(menu, type);
 		}
 	}
 }
