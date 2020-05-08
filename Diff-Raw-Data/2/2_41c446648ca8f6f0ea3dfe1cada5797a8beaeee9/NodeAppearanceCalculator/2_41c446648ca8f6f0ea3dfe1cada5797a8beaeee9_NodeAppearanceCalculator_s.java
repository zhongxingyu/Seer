 /*
  File: NodeAppearanceCalculator.java
 
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
 package cytoscape.visual;
 
 import cytoscape.CyNetwork;
 
 import cytoscape.visual.calculators.*;
 import cytoscape.visual.mappings.ObjectMapping;
 
 import giny.model.Node;
 
 import java.awt.Color;
 import java.awt.Font;
 
 import java.util.Properties;
 import java.util.List;
 
 
 /**
  * This class calculates the appearance of a Node. It holds a default value and
  * a (possibly null) calculator for each visual attribute.
  */
 public class NodeAppearanceCalculator extends AppearanceCalculator {
     private NodeAppearance defaultAppearance = new NodeAppearance();
 
 	/**
 	 * @deprecated Use VisualStyle.getNodeAppearanceCalculator() instead. Will be removed
 	 * January 2011.
 	 */
 	@Deprecated
     public NodeAppearanceCalculator() {
		super(null);
 	}
 
     /**
      * Creates a new NodeAppearanceCalculator object.
      */
     public NodeAppearanceCalculator(VisualPropertyDependency deps) {
         super(deps);
     }
 
     /**
      * Creates a new NodeAppearanceCalculator and immediately customizes it by
      * calling applyProperties with the supplied arguments.
      */
     public NodeAppearanceCalculator(String name, Properties nacProps,
         String baseKey, CalculatorCatalog catalog, VisualPropertyDependency deps) {
         super(name, nacProps, baseKey, catalog, new NodeAppearance(), deps);
         defaultAppearance = (NodeAppearance) tmpDefaultAppearance;
     }
 
     /**
      * Copy constructor. Returns a default object if the argument is null.
      */
     public NodeAppearanceCalculator(NodeAppearanceCalculator toCopy) {
         super(toCopy);
     }
 
     /**
      * Using the rules defined by the default values and calculators in this
      * object, compute an appearance for the requested Node in the supplied
      * CyNetwork. A new NodeApperance object will be created.
      */
     public NodeAppearance calculateNodeAppearance(Node node, CyNetwork network) {
         NodeAppearance appr = new NodeAppearance(deps);
         calculateNodeAppearance(appr,node,network,VisualPropertyType.getNodeVisualPropertyList());
 
         return appr;
     }
     
     /**
      * Create deep copy of the object.
      */
     public Object clone() {
     	final NodeAppearanceCalculator copy = new NodeAppearanceCalculator(deps);
     	
     	// Copy defaults
     	final NodeAppearance defAppr = new NodeAppearance();
     	for(VisualPropertyType type : VisualPropertyType.getNodeVisualPropertyList()) {
     		defAppr.set(type, defaultAppearance.get(type));
     	}
     	copy.setDefaultAppearance(defAppr);
     	
     	//Copy mappings
     	for(Calculator cal  : this.calcs) {
     		ObjectMapping mCopy = (ObjectMapping) cal.getMapping(0).clone();
     		BasicCalculator bCalc = new BasicCalculator(cal.toString(), mCopy, cal.getVisualPropertyType());
     		copy.setCalculator(bCalc);
     	}
     	
     	return copy;
     }
 
     /**
      * Using the rules defined by the default values and calculators in this
      * object, compute an appearance for the requested Node in the supplied
      * CyNetwork. The supplied NodeAppearance object will be changed to hold the
      * new values.
      */
     public void calculateNodeAppearance(NodeAppearance appr, Node node, CyNetwork network) {
 		calculateNodeAppearance(appr,node,network,VisualPropertyType.getNodeVisualPropertyList());
 	}
 
 
     void calculateNodeAppearance(NodeAppearance appr, Node node,
         CyNetwork network, List<VisualPropertyType> bypassedVPs) {
         appr.copy(defaultAppearance); // set defaults and node lock state
 
         for (Calculator nc : calcs)
             nc.apply(appr, node, network);
 
         appr.applyBypass(node,bypassedVPs);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public NodeAppearance getDefaultAppearance() {
         return defaultAppearance;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param n DOCUMENT ME!
      */
     public void setDefaultAppearance(NodeAppearance n) {
         defaultAppearance = n;
     }
 
     /**
      * Returns a text description of the current default values and calculator
      * names.
      */
     public String getDescription() {
         return getDescription("NodeAppearanceCalculator", defaultAppearance);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param name DOCUMENT ME!
      * @param nacProps DOCUMENT ME!
      * @param baseKey DOCUMENT ME!
      * @param catalog DOCUMENT ME!
      */
     public void applyProperties(String name, Properties nacProps,
         String baseKey, CalculatorCatalog catalog) {
         applyProperties(defaultAppearance, name, nacProps, baseKey, catalog);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param baseKey DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public Properties getProperties(String baseKey) {
         return getProperties(defaultAppearance, baseKey);
     }
 
     protected void copyDefaultAppearance(AppearanceCalculator toCopy) {
         defaultAppearance = (NodeAppearance) (((NodeAppearanceCalculator) toCopy).getDefaultAppearance().clone());
     }
 
     /**
      * @deprecated Use VisualStyle.getDependency().check(VisualPropertyDependency.Definition.NODE_SIZE_LOCKED) instead.
 	 * Will be removed Jan 2011.
      */
     public boolean getNodeSizeLocked() {
         return deps.check(VisualPropertyDependency.Definition.NODE_SIZE_LOCKED);
     }
 
     /**
      * @deprecated Use VisualStyle.getDependency().set(VisualPropertyDependency.Definition.NODE_SIZE_LOCKED,b) instead.
 	 * Will be removed Jan 2011.
      */
     public void setNodeSizeLocked(boolean b) {
        	deps.set(VisualPropertyDependency.Definition.NODE_SIZE_LOCKED,b);
     }
 }
