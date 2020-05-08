 
 /*
   File: EdgeAppearanceCalculatorTest.java 
   
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
 
 // EdgeAppearanceCalculatorTest.java
 
 
 //----------------------------------------------------------------------------
 // $Revision: 7760 $
 // $Date: 2006-06-26 09:28:49 -0700 (Mon, 26 Jun 2006) $
 // $Author: mes $
 //----------------------------------------------------------------------------
 package cytoscape.visual.unitTests;
 //----------------------------------------------------------------------------
 import junit.framework.*;
 import java.io.*;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.util.Properties;
 import java.util.Map;
 
 import giny.model.Node;
 import giny.model.Edge;
 import giny.model.RootGraph;
 
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.readers.CyAttributesReader;
 import cytoscape.CyNetwork;
 import cytoscape.CyEdge;
 import cytoscape.CyNode;
 import cytoscape.data.Semantics;
 import cytoscape.util.FileUtil;
 import cytoscape.visual.*;
 import cytoscape.visual.mappings.*;
 import cytoscape.visual.ui.*;
 import cytoscape.visual.calculators.*;
 
 public class EdgeAppearanceCalculatorTest extends TestCase {
 	CyNetwork cyNet;
 
 	CyNode a;
 	CyNode b;
 
 	CyEdge ab;
 
 	CalculatorCatalog catalog;
 	Properties props;
 
     public EdgeAppearanceCalculatorTest (String name) {super (name);}
 
     public void setUp() {
 	cyNet = Cytoscape.createNetworkFromFile("testData/small.sif");
 	a = Cytoscape.getCyNode("a");
 	b = Cytoscape.getCyNode("b");
 	ab = Cytoscape.getCyEdge(a,b,Semantics.INTERACTION,"pp",false);
 	props = new Properties();
 	try {
 	CyAttributesReader.loadAttributes(Cytoscape.getEdgeAttributes(),
                   new FileReader( "testData/small.edgeAttr"));
 
 	props.load(FileUtil.getInputStream("testData/small.vizmap.props"));
 	} catch(Exception e) { e.printStackTrace(); }
 	catalog = new CalculatorCatalog();
 	CalculatorIO.loadCalculators(props,catalog,true);
     }
 
     public void testDefaultAppearance() {
 
 	EdgeAppearanceCalculator eac = new EdgeAppearanceCalculator(); 
 
 	EdgeAppearance ea = eac.calculateEdgeAppearance(ab,cyNet);
 
 	// this tests that the default edge appearance is correct
 	assertTrue( "color", ea.getColor().equals(Color.BLACK) );
 	assertTrue( "lineType", ea.getLineType() == LineType.LINE_1 );
 	assertTrue( "src arrow", ea.getSourceArrow() == Arrow.NONE );
 	assertTrue( "trg arrow", ea.getTargetArrow() == Arrow.NONE );
 	assertTrue( "label", ea.getLabel().equals("") );
 	assertTrue( "tooltip", ea.getToolTip().equals("") );
 	assertTrue( "font size", ea.getFont().getSize() == 10 ); 
 	assertTrue( "font style", ea.getFont().getStyle() == Font.PLAIN );
     }
 
     public void testApplyProperties() {
 
 	EdgeAppearanceCalculator eac = new EdgeAppearanceCalculator(); 
 	eac.applyProperties("homer",props,"edgeAppearanceCalculator.homer",catalog);
 	EdgeAppearance ea = eac.calculateEdgeAppearance(ab,cyNet);
 	System.out.println(eac.getDescription());
 
 	System.out.println( "color " + ea.getColor());
 	System.out.println( "linetype " + ea.getLineType());
 	System.out.println( "src arrow " + ea.getSourceArrow());
 	System.out.println( "trg arrow " + ea.getTargetArrow());
 	System.out.println( "label " + ea.getLabel());
 	System.out.println( "tooltip " + ea.getToolTip());
 	System.out.println( "font size " + ea.getFont().getSize() ); 
 	System.out.println( "font style " +  ea.getFont().getStyle() );
 
	assertTrue( "color " + ea.getColor(), ea.getColor().equals(new Color(132,116,144)) );
 	assertTrue( "linetype " + ea.getLineType(), ea.getLineType() == LineType.LINE_1 );
 	assertTrue( "src arrow " + ea.getSourceArrow(), ea.getSourceArrow() == Arrow.BLACK_DIAMOND );
 	assertTrue( "trg arrow " + ea.getTargetArrow(), ea.getTargetArrow() == Arrow.NONE );
 	assertTrue( "label " + ea.getLabel(), ea.getLabel().equals("0.4") );
 	assertTrue( "tooltip " + ea.getToolTip(), ea.getToolTip().equals("") );
 	assertTrue( "font size " + ea.getFont().getSize(), ea.getFont().getSize() == 5 ); 
 	assertTrue( "font style " + ea.getFont().getStyle(), ea.getFont().getStyle() == Font.PLAIN );
     }
 
     public static void main (String [] args) {
 	junit.textui.TestRunner.run (new TestSuite (EdgeAppearanceCalculatorTest.class));
     }
 
 }
 
 
