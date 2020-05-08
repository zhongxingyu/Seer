 /*
  File: EdgeAppearanceCalculator.java
 
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
 
 //----------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //----------------------------------------------------------------------------
 package cytoscape.visual;
 
 import cytoscape.CyNetwork;
 
 import cytoscape.visual.calculators.BasicCalculator;
 import cytoscape.visual.calculators.Calculator;
 
 import cytoscape.visual.mappings.ObjectMapping;
 
 import giny.model.Edge;
 
 import java.awt.Color;
 import java.awt.Font;
 
 import java.util.Properties;
 import java.util.List;
 
 
 //----------------------------------------------------------------------------
 /**
  * This class calculates the appearance of an Edge. It holds a default value and
  * a (possibly null) calculator for each visual attribute.
  */
 public class EdgeAppearanceCalculator extends AppearanceCalculator {
 	private EdgeAppearance defaultAppearance = new EdgeAppearance();
 
	/** 
	 * @deprecated Use VisualStyle.getEdgeAppearanceCalculator() or new EdgeAppearanceCalculator( VisualStyle.getDependency() );  Will be removed Jan 2010.
	 */
	@Deprecated
	public EdgeAppearanceCalculator() {
		super(new VisualPropertyDependencyImpl());
	}
 	/**
 	 * Creates a new EdgeAppearanceCalculator object.
 	 */
 	public EdgeAppearanceCalculator(VisualPropertyDependency deps) {
 		super(deps);
 	}
 
 	/**
 	 * Copy constructor. Returns a default object if the argument is null.
 	 */
 	public EdgeAppearanceCalculator(EdgeAppearanceCalculator toCopy) {
 		super(toCopy);
 	}
 
 	/**
 	 * Creates a new EdgeAppearanceCalculator and immediately customizes it by
 	 * calling applyProperties with the supplied arguments.
 	 */
 	public EdgeAppearanceCalculator(String name, Properties eacProps, String baseKey,
 	                                CalculatorCatalog catalog, VisualPropertyDependency deps) {
 		super(name, eacProps, baseKey, catalog, new EdgeAppearance(), deps);
 		defaultAppearance = (EdgeAppearance) tmpDefaultAppearance;
 	}
 
 	/**
 	 * Create deep copy of the object.
 	 */
 	public Object clone() {
 		final EdgeAppearanceCalculator copy = new EdgeAppearanceCalculator(deps);
 
 		// Copy defaults
 		final EdgeAppearance defAppr = new EdgeAppearance();
 
 		for (VisualPropertyType type : VisualPropertyType.getEdgeVisualPropertyList()) {
 			defAppr.set(type, defaultAppearance.get(type));
 		}
 
 		copy.setDefaultAppearance(defAppr);
 
 		//Copy mappings
 		for (Calculator cal : this.calcs) {
 			final ObjectMapping mCopy = (ObjectMapping) cal.getMapping(0).clone();
 			BasicCalculator bCalc = new BasicCalculator(cal.toString(), mCopy,
 			                                            cal.getVisualPropertyType());
 			copy.setCalculator(bCalc);
 		}
 
 		return copy;
 	}
 
 	/**
 	 * Using the rules defined by the default values and calculators in this
 	 * object, compute an appearance for the requested Edge in the supplied
 	 * CyNetwork. A new EdgeApperance object will be created.
 	 */
 	public EdgeAppearance calculateEdgeAppearance(Edge edge, CyNetwork network) {
 		EdgeAppearance appr = (EdgeAppearance) defaultAppearance.clone();
 		calculateEdgeAppearance(appr,edge,network,VisualPropertyType.getEdgeVisualPropertyList());
 
 		return appr;
 	}
 
 	/**
 	 * Using the rules defined by the default values and calculators in this
 	 * object, compute an appearance for the requested Edge in the supplied
 	 * CyNetwork. The supplied EdgeAppearance object will be changed to hold the
 	 * new values.
 	 */
 	public void calculateEdgeAppearance(EdgeAppearance appr, Edge edge, CyNetwork network) {
 		calculateEdgeAppearance(appr,edge,network,VisualPropertyType.getEdgeVisualPropertyList());
 	}
 
 	void calculateEdgeAppearance(EdgeAppearance appr, Edge edge, CyNetwork network, List<VisualPropertyType> bypassedVPs) {
 		appr.copy(defaultAppearance); // set default values
 
 		for (Calculator c : calcs)
 			c.apply(appr, edge, network);
 
 		appr.applyBypass(edge,bypassedVPs);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public EdgeAppearance getDefaultAppearance() {
 		return defaultAppearance;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param appr DOCUMENT ME!
 	 */
 	public void setDefaultAppearance(EdgeAppearance appr) {
 		defaultAppearance = appr;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public String getDescription() {
 		return getDescription("EdgeAppearanceCalculator", defaultAppearance);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param name DOCUMENT ME!
 	 * @param eacProps DOCUMENT ME!
 	 * @param baseKey DOCUMENT ME!
 	 * @param catalog DOCUMENT ME!
 	 */
 	public void applyProperties(String name, Properties eacProps, String baseKey,
 	                            CalculatorCatalog catalog) {
 		applyProperties(defaultAppearance, name, eacProps, baseKey, catalog);
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
 		defaultAppearance = (EdgeAppearance) (((EdgeAppearanceCalculator) toCopy).getDefaultAppearance()
 		                                       .clone());
 	}
 
 }
