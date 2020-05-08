 //----------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //----------------------------------------------------------------------------
 package cytoscape.visual.ui;
 
 import javax.swing.*;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 import java.awt.*;
 import java.awt.event.*;
 import cytoscape.dialogs.GridBagGroup;
 import cytoscape.dialogs.MiscGB;
 import cytoscape.visual.VisualMappingManager;
 import cytoscape.visual.VisualStyle;
 import cytoscape.visual.GlobalAppearanceCalculator;
 //----------------------------------------------------------------------------
 /**
  * Defines a class to provide the interface for specifying global defaults such as background color.
  */
 public class DefaultPanel extends JPanel implements ChangeListener {
     //private DefaultBackgroundRenderer bgRender;
     private VisualMappingManager vmm;
     private ValueDisplayer backColor;
     private VizMapUI parentDialog;
 
     public DefaultPanel(VizMapUI parentDialog, VisualMappingManager vmm) {
 	super(false);
     this.parentDialog = parentDialog;
     this.vmm = vmm;
 

     //  Register class to receive notifications of changes in the
     //  GlobalAppearance Calculator.
     VisualStyle vs = vmm.getVisualStyle();
     GlobalAppearanceCalculator gCalc =
             vs.getGlobalAppearanceCalculator();
     gCalc.addChangeListener(this);
 
     //  Also, get notifications is user changes to a different
     //  visual style.
     vmm.addChangeListener(this);
 
     addColorButton();
     }
 
     private void addColorButton() {
         // this is really really evil
         GridBagGroup def = new GridBagGroup();
         def.panel = this;
         setLayout(def.gridbag);
         MiscGB.pad(def.constraints, 2, 2);
         MiscGB.inset(def.constraints, 3);
 
         // background color
         VisualStyle vs = vmm.getVisualStyle();
         GlobalAppearanceCalculator gCalc =
                 vs.getGlobalAppearanceCalculator();
         Color initColor = gCalc.getDefaultBackgroundColor();
         this.backColor = ValueDisplayer.getDisplayFor(parentDialog, "Background Color", initColor);
         backColor.addItemListener(new BackColorListener());
         JButton backColorBut = new JButton("Background Color");
         backColorBut.addActionListener(backColor.getInputListener());
         MiscGB.insert(def, backColorBut, 0, 0);
         MiscGB.insert(def, backColor, 1, 0);
     }
 
     public void stateChanged(ChangeEvent e) {
         VisualStyle vs = vmm.getVisualStyle();
         GlobalAppearanceCalculator gCalc =
                 vs.getGlobalAppearanceCalculator();
         Color color = gCalc.getDefaultBackgroundColor();
         backColor.setBackground(color);
         this.repaint();
     }
 
     private class BackColorListener implements ItemListener {
 	public void itemStateChanged(ItemEvent e) {
 	    if (e.getStateChange() == ItemEvent.SELECTED) {
 		Color newBG = (Color) backColor.getValue();
                 VisualStyle vs = vmm.getVisualStyle();
                 vs.getGlobalAppearanceCalculator().setDefaultBackgroundColor(newBG);
                 vmm.applyGlobalAppearances();
 	    }
 	}
     }
 
 }
