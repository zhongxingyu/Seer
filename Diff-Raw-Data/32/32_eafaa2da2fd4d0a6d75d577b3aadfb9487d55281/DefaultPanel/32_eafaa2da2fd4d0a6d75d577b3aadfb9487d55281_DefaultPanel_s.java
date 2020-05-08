 //----------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //----------------------------------------------------------------------------
 package cytoscape.visual.ui;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import cytoscape.dialogs.GridBagGroup;
 import cytoscape.dialogs.MiscGB;
 import y.view.Graph2D;
 import y.view.Graph2DView;
 import y.view.DefaultBackgroundRenderer;
 //----------------------------------------------------------------------------
 /**
  * Defines a class to provide the interface for specifying global defaults such as background color.
  */
 public class DefaultPanel extends JPanel {
    private DefaultBackgroundRenderer bgRender;
     private ValueDisplayer backColor;
    public DefaultPanel(VizMapUI parentDialog, Graph2D graph) {
 	super(false);
 
 	// this is evil
	this.bgRender = (DefaultBackgroundRenderer) ((Graph2DView) graph.getCurrentView()).getBackgroundRenderer();
 
 	// this is really really evil
 	GridBagGroup def = new GridBagGroup();
 	def.panel = this;
 	setLayout(def.gridbag);
 	MiscGB.pad(def.constraints, 2, 2);
 	MiscGB.inset(def.constraints, 3);
 	
 	// background color
	Color initColor = bgRender.getColor();
 	this.backColor = ValueDisplayer.getDisplayFor(parentDialog, "Background Color", initColor);
 	backColor.addItemListener(new BackColorListener());
 	JButton backColorBut = new JButton("Background Color");
 	backColorBut.addActionListener(backColor.getInputListener());
 	MiscGB.insert(def, backColorBut, 0, 0);
 	MiscGB.insert(def, backColor, 1, 0);
     }
 
     private class BackColorListener implements ItemListener {
 	public void itemStateChanged(ItemEvent e) {
 	    if (e.getStateChange() == ItemEvent.SELECTED) {
 		Color newBG = (Color) backColor.getValue();
 		bgRender.setColor(newBG);
 	    }
 	}
     }
 	    
 }
