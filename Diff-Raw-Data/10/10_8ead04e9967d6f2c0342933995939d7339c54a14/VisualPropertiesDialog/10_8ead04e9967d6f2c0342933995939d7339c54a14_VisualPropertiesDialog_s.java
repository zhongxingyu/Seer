 // VisualPropertiesDialog.java
 //---------------------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //---------------------------------------------------------------------------------------
 package cytoscape.dialogs;
 //---------------------------------------------------------------------------------------
 import javax.swing.*;
 import javax.swing.JSlider; 
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.AbstractAction;
 import java.awt.BorderLayout;
 import javax.swing.border.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.event.*;
 import java.text.NumberFormat;
 
 import javax.swing.colorchooser.*;
 import java.util.HashMap;
 import java.util.Map;
 import cytoscape.data.*;
 import cytoscape.vizmap.*;
 import cytoscape.dialogs.NewSlider;
import cytoscape.dialogs.MutableColor;
 //import csplugins.activePathsNew.data.ActivePathFinderParameters;
 //--------------------------------------------------------------------------------------
 public class VisualPropertiesDialog extends JDialog {
 
     JTextField readout;
     AttributeMapper aMapper;
     MutableColor nColor;
     MutableColor ppColor;
     MutableColor pdColor;
     MutableColor bgColor;
 //--------------------------------------------------------------------------------------
 public VisualPropertiesDialog (Frame parentFrame,
 			       String title,
 			       AttributeMapper mapper)
 {
   super (parentFrame, true);
   setTitle (title);
 
   aMapper = mapper;
   nColor = new MutableColor(getBasicColor(VizMapperCategories.NODE_FILL_COLOR));
   //ppColor = new Color(0,0,255);
   //pdColor = new Color(255,0,0);
   ppColor = new MutableColor(getDMColor(VizMapperCategories.EDGE_COLOR, "pp"));
   pdColor = new MutableColor(getDMColor(VizMapperCategories.EDGE_COLOR, "pd"));
   bgColor = new MutableColor(getBasicColor(VizMapperCategories.BG_COLOR));
 
   JPanel mainPanel = new JPanel ();
   GridBagLayout gridbag = new GridBagLayout();   // see note below
   GridBagConstraints c = new GridBagConstraints();  // see note below
   mainPanel.setLayout (gridbag);
 
   JButton applyButton = new JButton ("Apply");
   applyButton.addActionListener (new ApplyAction ());
   c.gridx=0;
   c.gridy=0;
   gridbag.setConstraints(applyButton,c);
   mainPanel.add (applyButton);
 
   JButton edgePPButton = new JButton ("Edge Coloring: PP");
   edgePPButton.addActionListener (new SpawnGeneralColorDialogListener(ppColor,"Choose a P-P Edge Color"));
   c.gridx=0;
   c.gridy=1;
   gridbag.setConstraints(edgePPButton,c);
   mainPanel.add (edgePPButton);
 
   JButton edgePDButton = new JButton ("Edge Coloring: PD");
   edgePDButton.addActionListener (new SpawnGeneralColorDialogListener(pdColor,"Choose a P-D Edge Color"));
   c.gridx=1;
   c.gridy=1;
   gridbag.setConstraints(edgePDButton,c);
   mainPanel.add (edgePDButton);
   
   JButton colorButton = new JButton("Choose Node Color");
   colorButton.addActionListener(new SpawnGeneralColorDialogListener(nColor,"Choose a Node Color"));
   c.gridx=0;
   c.gridy=2;
   gridbag.setConstraints(colorButton,c);
   mainPanel.add(colorButton);
 
   JButton bgColorButton
       = new JButton("Choose Background Color");
   bgColorButton.addActionListener(new SpawnGeneralColorDialogListener(bgColor,"Choose a Background Color"));
   c.gridx=0;
   c.gridy=3;
   gridbag.setConstraints(bgColorButton,c);
   mainPanel.add(bgColorButton);
 
   setContentPane (mainPanel);
 } // PopupDialog ctor
 
 //--------------------------------------------------------------------------------------
 public class ApplyAction extends AbstractAction {
   ApplyAction () {
       super ("");
   }
 
   public void actionPerformed (ActionEvent e) {
 
       Object o1 = aMapper.setDefaultValue(VizMapperCategories.NODE_FILL_COLOR, nColor.getColor());
       Object o2 = aMapper.setDefaultValue(VizMapperCategories.BG_COLOR, bgColor.getColor());
 
       EdgeArrowColor.removeThenAddEdgeColor(aMapper,"pp",ppColor.getColor());
       EdgeArrowColor.removeThenAddEdgeColor(aMapper,"pd",pdColor.getColor());
 
       VisualPropertiesDialog.this.dispose ();
   }
 
 } // QuitAction
 
 
 class SpawnGeneralColorDialogListener implements ActionListener {
     private MutableColor returnColor;
     private String title;
     public SpawnGeneralColorDialogListener(MutableColor writeToThisColor, String title) {
 	super ();
 	//System.out.println("writecolor:" + writeToThisColor);
 	returnColor = writeToThisColor;
 	this.title = title;
     }
     
     public void actionPerformed(ActionEvent e) {
 	// Args are parent component, title, initial color
 	Color tempColor = JColorChooser.showDialog(VisualPropertiesDialog.this,
 						   title,
 						   returnColor.getColor());
 	if (tempColor != null)
 	    returnColor.setColor(tempColor);
 	//System.out.println("after: returncolor:" + returnColor);
     }
 }
 
     private Color getBasicColor(Integer category) {
 	Color tempColor;
 	try {
 	    tempColor = 
 		(Color)aMapper.getDefaultValue(category);
 	} catch (NullPointerException ex) {
 	    tempColor = new Color(255,255,255);
 	}
 	if(tempColor != null)
 	    return tempColor;
 	else {
 	    return new Color(255,255,255);
 	}
     }
 
     private Color getDMColor(Integer category, String key) {
 	Color tempColor;
 
 	DiscreteMapper dm =
 	    (DiscreteMapper)
 	    aMapper.getValueMapper(category);
 
 	Map valueMap = dm.getValueMap();
 	tempColor = (Color)valueMap.get(key);
 
 	if(tempColor != null)
 	    return tempColor;
 	else {
 	    return new Color(255,255,255);
 	}
     }
 
 } // class VisualPropertiesDialog
 
 
 
 
 
 //-----------------------------------------------------------------------------
 
 /*
 public class ColorChooserDialog extends JDialog {
     public ColorChooserDialog(JDialog parentDialog, String whatFor) {
         super(parentDialog, "Choose Color for " + whatFor);
 
         //Set up the banner at the top of the window
         final JLabel banner = new JLabel("Choose Color for " + whatFor,
                                          JLabel.CENTER);
         banner.setForeground(Color.yellow);
         banner.setBackground(Color.blue);
         banner.setOpaque(true);
         banner.setFont(new Font("SansSerif", Font.BOLD, 24));
         banner.setPreferredSize(new Dimension(100, 65));
 
         JPanel bannerPanel = new JPanel(new BorderLayout());
         bannerPanel.add(banner, BorderLayout.CENTER);
         bannerPanel.setBorder(BorderFactory.createTitledBorder("Banner"));
 
         //Set up color chooser for setting text color
         final JColorChooser tcc = new JColorChooser(banner.getForeground());
         tcc.getSelectionModel().addChangeListener(
             new ChangeListener() {
                 public void stateChanged(ChangeEvent e) {
                     Color newColor = tcc.getColor();
                     banner.setForeground(newColor);
                 }
             }
         );
         tcc.setBorder(BorderFactory.createTitledBorder(
                                              "Choose Text Color"));
 
         //Add the components to the demo frame
         Container contentPane = getContentPane();
         contentPane.add(bannerPanel, BorderLayout.CENTER);
         contentPane.add(tcc, BorderLayout.SOUTH);
     }
     
 }
     
 */
