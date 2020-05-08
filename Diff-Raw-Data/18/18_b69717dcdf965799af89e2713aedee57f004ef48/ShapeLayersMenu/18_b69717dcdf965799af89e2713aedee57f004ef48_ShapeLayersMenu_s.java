 package dk.frv.eavdam.menus;
 
 import com.bbn.openmap.Layer;
 import dk.frv.eavdam.data.EAVDAMData;
 import dk.frv.eavdam.data.EAVDAMUser;
 import dk.frv.eavdam.data.OtherUserStations;
 import dk.frv.eavdam.data.Simulation;
 import dk.frv.eavdam.layers.OMAISShapeLayer;
 import dk.frv.eavdam.utils.DBHandler;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 /**
  * This class represents a menu item where the user can select which shape layers are shown on the map.
  */
 public class ShapeLayersMenu extends JMenuItem {
 
     public static final long serialVersionUID = 3L;
 	
 	public static final String SHAPE_FILE_DIR = "./share/data/shape/mapki";
 	
 	private List<JCheckBox> shapeLayersCheckBoxes;		
 	
     public ShapeLayersMenu(EavdamMenu eavdamMenu) {        
         super("Shape File Layers");		
 		
 		shapeLayersCheckBoxes  = new ArrayList<JCheckBox>(); 
 		File dir = new File(ShapeLayersMenu.SHAPE_FILE_DIR);			
 		String[] children = dir.list();
 		if (children != null) {
 			for (int i=0; i<children.length; i++) {
 				String filename = children[i];
 				if (filename.endsWith(".shp")) {
 					JCheckBox shapeLayersCheckBox = new JCheckBox(filename.substring(0, filename.length()-4));
 					shapeLayersCheckBox.setSelected(false);
 					shapeLayersCheckBoxes.add(shapeLayersCheckBox);
 				}
 			}
 		}		
 		
         addActionListener(new ShapeLayersActionListener(this, eavdamMenu));
 	}
 	
 	public List<JCheckBox> getShapeLayersCheckBoxes() {
 		return shapeLayersCheckBoxes;
 	}
 
 }
 	
 class ShapeLayersActionListener implements ActionListener {
 
     private EavdamMenu eavdamMenu;	
     private ShapeLayersMenu shapeLayersMenu;
 
 	private JDialog dialog;
 	
 	private List<Boolean> initialSelections;
 	
 	private JButton saveButton;
 	private JButton cancelButton;
 	
     public ShapeLayersActionListener(ShapeLayersMenu shapeLayersMenu, EavdamMenu eavdamMenu) {
         super();
         this.eavdamMenu = eavdamMenu;		
         this.shapeLayersMenu = shapeLayersMenu;
     }
                    
     public void actionPerformed(ActionEvent e) {
         
         if (e.getSource() instanceof ShapeLayersMenu) {
 	
 			dialog = new JDialog(eavdamMenu.getOpenMapFrame(), "Shape File Layers", true);
 		
 			JPanel panel = new JPanel();
 			panel.setLayout(new GridBagLayout());  
 			GridBagConstraints c = new GridBagConstraints();    
             c.gridx = 0;			
             c.gridy = 0;	
 			c.insets = new Insets(5, 5, 5, 5);				
             c.anchor = GridBagConstraints.FIRST_LINE_START;
     
 			panel.add(new JLabel("Put shape files in " + ShapeLayersMenu.SHAPE_FILE_DIR + "."), c);
 			c.gridy = 1;
 			panel.add(new JLabel("Select here which are shown on the map."), c);
 
 			JPanel shapeLayersPane = new JPanel();
 			shapeLayersPane.setLayout(new GridBagLayout());   
 			c.insets = new Insets(0, 0, 0, 0);	
 			
 			initialSelections = new ArrayList<Boolean>();				
 			for (JCheckBox shapeLayersCheckBox : shapeLayersMenu.getShapeLayersCheckBoxes()) {			
 				initialSelections.add(new Boolean(shapeLayersCheckBox.isSelected()));
 				shapeLayersPane.add(shapeLayersCheckBox, c);
 				c.gridy++;
 			}		
 			
 			JScrollPane scrollPane = new JScrollPane(shapeLayersPane);
 			scrollPane.setBorder(BorderFactory.createTitledBorder(""));
 			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setMinimumSize(new Dimension(400, 600));
			scrollPane.setPreferredSize(new Dimension(400, 600));
			scrollPane.setMaximumSize(new Dimension(400, 600));        
 			
 			c.gridy = 2;
             c.insets = new Insets(5, 5, 5, 5);					
             panel.add(scrollPane, c);
 			
 			saveButton = new JButton("Save and exit", null);        
 			saveButton.setVerticalTextPosition(AbstractButton.BOTTOM);
 			saveButton.setHorizontalTextPosition(AbstractButton.CENTER);
 			saveButton.setPreferredSize(new Dimension(130, 20));
 			saveButton.setMaximumSize(new Dimension(130, 20));
 			saveButton.addActionListener(this);
             
 			cancelButton = new JButton("Cancel", null);        
 			cancelButton.setVerticalTextPosition(AbstractButton.BOTTOM);
 			cancelButton.setHorizontalTextPosition(AbstractButton.CENTER);
 			cancelButton.setPreferredSize(new Dimension(100, 20));
 			cancelButton.setMaximumSize(new Dimension(100, 20));                              
 			cancelButton.addActionListener(this);
 			
 			JPanel buttonPanel = new JPanel();
 			buttonPanel.setLayout(new GridBagLayout());
 			buttonPanel.add(saveButton, c);
 			c.gridx = 1;
 			buttonPanel.add(cancelButton, c);
 			
 			c.gridx = 0;
 			c.gridy = 3;	
 			c.anchor = GridBagConstraints.CENTER;     
 			panel.add(buttonPanel, c);
 			
 			dialog.getContentPane().add(panel);
 
             int frameWidth = 440;
            int frameHeight = 760;
             Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
             dialog.setBounds((int) screenSize.getWidth()/2 - frameWidth/2,
                 (int) screenSize.getHeight()/2 - frameHeight/2, frameWidth, frameHeight);
             dialog.setVisible(true);			
 		
 		} else if (e.getSource() == saveButton) {
 		
 			Layer[] currentLayers = eavdamMenu.getLayerHandler().getMapLayers();
 			for (Layer currentLayer : currentLayers) {
 				if (currentLayer instanceof OMAISShapeLayer) {
 					eavdamMenu.getLayerHandler().removeLayer(currentLayer);
 				}
 			}
 		
 			for (JCheckBox shapeLayersCheckBox : shapeLayersMenu.getShapeLayersCheckBoxes()) {
 				if (shapeLayersCheckBox.isSelected()) {
 					OMAISShapeLayer shapeLayer = new OMAISShapeLayer(ShapeLayersMenu.SHAPE_FILE_DIR + "/" + shapeLayersCheckBox.getText() + ".shp");
 					eavdamMenu.getLayerHandler().addLayer(shapeLayer);
 				}
 			}
 			dialog.dispose();    
 			
 		} else if (e.getSource() == cancelButton) {
 		
 			for (int i=0; i<shapeLayersMenu.getShapeLayersCheckBoxes().size(); i++) {
 				if (i < initialSelections.size()) {
 					JCheckBox shapeLayersCheckBox = shapeLayersMenu.getShapeLayersCheckBoxes().get(i);
 					shapeLayersCheckBox.setSelected(initialSelections.get(i).booleanValue());
 				}
 			}
 			
 			dialog.dispose();
 		}
 	}		
 			
 }
 
