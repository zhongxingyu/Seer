 package gov.nih.nci.ncicb.cadsr.loader.ui;
 import gov.nih.nci.ncicb.cadsr.domain.DataElement;
 import gov.nih.nci.ncicb.cadsr.domain.DomainObjectFactory;
 import gov.nih.nci.ncicb.cadsr.domain.ValueDomain;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.UMLNode;
 import gov.nih.nci.ncicb.cadsr.loader.util.*;
 import gov.nih.nci.ncicb.cadsr.loader.event.*;
 import gov.nih.nci.ncicb.cadsr.loader.ui.util.UIUtil;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import javax.swing.*;
 
 import java.util.List;
 import java.util.ArrayList;
 
 
 import org.apache.log4j.Logger;
 
 public class VDPanel extends JPanel implements MouseListener
 {
     private JButton searchVdButton = new JButton("Search Value Domain");
   
     private JLabel vdLongNameTitleLabel = new JLabel("Long Name: "),
         vdLongNameValueLabel = new JLabel(""),
         vdPublicIdTitleLabel = new JLabel("Public Id: "),
         vdPublicIdValueLabel = new JLabel(""),
         vdContextNameTitleLabel = new JLabel("Context Name: "),
         vdContextNameValueLabel = new JLabel(""),
         vdVersionTitleLabel = new JLabel("Version: "),
         vdVersionValueLabel = new JLabel(""),
         vdDatatypeTitleLabel = new JLabel("Datatype: "),
         vdDatatypeValueLabel = new JLabel(""),
         lvdTitleLabel = new JLabel("Local Value Domain: "),
         lvdValueLabel = new JLabel("");
 
     private JToolBar toolbar = new JToolBar();
     private DropDownButton ddb;
 
     private JButton mainButton;
     private JButton arrowButton;
     private final String MAIN_BUTTON = "mainButton";
     private final String ARROW_BUTTON = "arrowButton";
     private JPopupMenu popup = new JPopupMenu();
     private JToolBar tb;
 
     private JLabel cadsrVDLabel = new JLabel("Search caDSR Value Domain");
     private JLabel mapToLVDLabel = new JLabel("Map to Local Value Domain");
     private static final String MAP_CADSR_VD = "Search caDSR Value Domain";
     private static final String MAP_LOCAL_VD = "Map to Local Value Domain";
     private List<PropertyChangeListener> propChangeListeners = new ArrayList<PropertyChangeListener>(); 
 
     private List<ElementChangeListener> changeListeners = new ArrayList<ElementChangeListener>();
   
     protected ElementsLists elements = ElementsLists.getInstance();
   
     private ValueDomain tempVD, vd;
     private UMLNode node;
     private boolean modified = false;
     private Logger logger = Logger.getLogger(MapToLVD.class.getName());
     private boolean isLVD = false;
     private ValueDomain selectedLVD = null;
     private JPanel cadsrVDPanel;
     private JPanel lvdPanel;
     
     public VDPanel(UMLNode node)
     {
         this.node = node;
         DataElement de = null;
         if(node.getUserObject() instanceof DataElement) {
             de = (DataElement)node.getUserObject();
             vd = de.getValueDomain();
         }    
 
         this.setLayout(new BorderLayout());
         JPanel mainPanel = new JPanel(new GridBagLayout());
     
         UIUtil.insertInBag(mainPanel, lvdTitleLabel, 0, 0);
         UIUtil.insertInBag(mainPanel, lvdValueLabel, 1, 0);
         UIUtil.insertInBag(mainPanel, vdLongNameTitleLabel, 0, 1);
         UIUtil.insertInBag(mainPanel, vdLongNameValueLabel, 1, 1);
         UIUtil.insertInBag(mainPanel, vdPublicIdTitleLabel, 0, 2);
         UIUtil.insertInBag(mainPanel, vdPublicIdValueLabel, 1, 2);
         UIUtil.insertInBag(mainPanel, vdContextNameTitleLabel, 0, 3);
         UIUtil.insertInBag(mainPanel, vdContextNameValueLabel, 1, 3);
         UIUtil.insertInBag(mainPanel, vdVersionTitleLabel, 0, 4);
         UIUtil.insertInBag(mainPanel, vdVersionValueLabel, 1, 4);
         UIUtil.insertInBag(mainPanel, vdDatatypeTitleLabel, 0, 5);
         UIUtil.insertInBag(mainPanel, vdDatatypeValueLabel, 1, 5);
     
         ddb = new DropDownButton();
         
         cadsrVDPanel = new JPanel();
         cadsrVDPanel.setBackground(Color.WHITE);
         cadsrVDPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         cadsrVDPanel.add(cadsrVDLabel);
         
         lvdPanel = new JPanel();
         lvdPanel.setBackground(Color.WHITE);
         lvdPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
         lvdPanel.add(mapToLVDLabel);
         
         ddb.addComponent(cadsrVDPanel);
         ddb.addComponent(lvdPanel);
         toolbar.add(ddb);
        toolbar.setFloatable(false);
 
         UIUtil.insertInBag(mainPanel, toolbar, 1, 8, 2, 1);
         mainPanel.setBorder(BorderFactory.createTitledBorder("Value Domain"));
     
         cadsrVDLabel.addMouseListener(this);
         mapToLVDLabel.addMouseListener(this);
     
         this.add(mainPanel);
         this.setSize(300, 300);
     }
 
     public void showCADSRSearchDialog(){
         try {
             CadsrDialog cd = BeansAccessor.getCadsrVDDialog();
             cd.setVisible(true);
             
             tempVD = (ValueDomain)cd.getAdminComponent();
             if(tempVD != null) {
                 vdLongNameValueLabel.setText(tempVD.getLongName());
                 vdPublicIdValueLabel.setText(tempVD.getPublicId());
                 vdContextNameValueLabel.setText(tempVD.getContext().getName());
                 vdVersionValueLabel.setText(tempVD.getVersion().toString());
                 vdDatatypeValueLabel.setText(tempVD.getDataType());
                 
                 vdLongNameTitleLabel.setVisible(true);
                 vdPublicIdTitleLabel.setVisible(true);
                 vdContextNameTitleLabel.setVisible(true);
                 vdVersionTitleLabel.setVisible(true);
                 vdDatatypeTitleLabel.setVisible(true);
                 
                 firePropertyChangeEvent(
                   new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, true));
                 
                 modified = true;
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     public void showLVDSearchDialog(){
         try {
             List<ValueDomain> lvds = elements.getElements(DomainObjectFactory.newValueDomain());
             MapToLVD mapToLVD = new MapToLVD(lvds); 
             mapToLVD.setAlwaysOnTop(true);
             mapToLVD.setVisible(true);
             
             if(lvds != null){
                 selectedLVD = mapToLVD.getLocalValueDomain();
                 if(selectedLVD != null){
                     vdLongNameValueLabel.setText(selectedLVD.getLongName());
                     vdLongNameTitleLabel.setVisible(true);
                     
                     vdPublicIdValueLabel.setText("");
                     vdContextNameValueLabel.setText("");
                     vdVersionValueLabel.setText("");
                     vdDatatypeValueLabel.setText("");
                     
                     vdPublicIdTitleLabel.setVisible(false);
                     vdContextNameTitleLabel.setVisible(false);
                     vdVersionTitleLabel.setVisible(false);
                     vdDatatypeTitleLabel.setVisible(false);
     
                     firePropertyChangeEvent(new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, true));
         
                     modified = true;
                     isLVD = true;
                 }
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     public void mouseClicked(MouseEvent e) {
         cadsrVDPanel.setBackground(Color.WHITE);
         lvdPanel.setBackground(Color.WHITE);
         popup.setVisible(false);
         if(((JLabel)e.getSource()).getText().equals(MAP_CADSR_VD))
             showCADSRSearchDialog();        
         else if(((JLabel)e.getSource()).getText().equals(MAP_LOCAL_VD))
             showLVDSearchDialog();
     }
 
     public void mousePressed(MouseEvent e) {}
     public void mouseReleased(MouseEvent e) {}
     public void mouseEntered(MouseEvent e) {
         if(((JLabel)e.getSource()).getText().equals(MAP_CADSR_VD))
             cadsrVDPanel.setBackground(Color.LIGHT_GRAY);
         else if(((JLabel)e.getSource()).getText().equals(MAP_LOCAL_VD))
             lvdPanel.setBackground(Color.LIGHT_GRAY);
     }
     public void mouseExited(MouseEvent e) {
         if(((JLabel)e.getSource()).getText().equals(MAP_CADSR_VD))
             cadsrVDPanel.setBackground(Color.WHITE);
         else if(((JLabel)e.getSource()).getText().equals(MAP_LOCAL_VD))
             lvdPanel.setBackground(Color.WHITE);
     }
 
     class DropDownButton extends JButton implements ActionListener {
         public DropDownButton() {
              super();
              tb = new ToolBar();
              mainButton = new JButton("Value Domains");
              mainButton.setActionCommand(MAIN_BUTTON);
              arrowButton = new JButton(new DownArrow());
              arrowButton.setActionCommand(ARROW_BUTTON);
              init();
              setFixedSize();
              setBorder(null);
         }
         protected void setFixedSize() {
             arrowButton.setPreferredSize(new Dimension(15, 15));
             arrowButton.setMaximumSize(new Dimension(15, 15));
             arrowButton.setMinimumSize(new Dimension(15, 15));
         }
         private void init() {
             Icon disDownArrow = new DisabledDownArrow();
             arrowButton.setDisabledIcon(disDownArrow);
             arrowButton.setMaximumSize(new Dimension(15,100));
             mainButton.addActionListener(this); 
             arrowButton.addActionListener(this);
             
             setMargin(new Insets(0, 0, 0, 0));
             
             tb.setBorder(null);
             tb.setMargin(new Insets(0, 0, 0, 0));
             tb.setFloatable(false);
             tb.add(mainButton);
             tb.add(arrowButton);
             add(tb);
             
             setFixedSize(mainButton, arrowButton);
         }
         /**
         * Forces the width of this button to be the sum of the widths of the main
         * button and the arrow button. The height is the max of the main button or
         * the arrow button.
         */
         private void setFixedSize(JButton mainButton, JButton arrowButton) {
             int width = (int)(mainButton.getPreferredSize().getWidth() + arrowButton.getPreferredSize().getWidth());
             int height = (int)Math.max(mainButton.getPreferredSize().getHeight(), arrowButton.getPreferredSize().getHeight());
             
             setMaximumSize(new Dimension(width, height));
             setMinimumSize(new Dimension(width, height));
             setPreferredSize(new Dimension(width, height));
         }
     
         /**
         * Adds a component to the popup
         * @param component
         * @return
         */
         public Component addComponent(Component component) {
             return popup.add(component);
         }
          
         public void actionPerformed(ActionEvent ae){ 
             if(ae.getActionCommand().equals(ARROW_BUTTON)){
                 JPopupMenu popup = getPopupMenu(); 
                 popup.setVisible(true);
                 popup.show(this, 0, this.getHeight()); 
             }
             else{
                 showCADSRSearchDialog();
             }
         } 
     
         protected JPopupMenu getPopupMenu() { return popup; }
          
         private class DownArrow implements Icon {
             Color arrowColor = Color.black;
     
             public void paintIcon(Component c, Graphics g, int x, int y) {
                 g.setColor(arrowColor);
                 g.drawLine(x, y, x+4, y);
                 g.drawLine(x+1, y+1, x+3, y+1);
                 g.drawLine(x+2, y+2, x+2, y+2);
             }
     
             public int getIconWidth() {
                 return 6;
             }
     
             public int getIconHeight() {
                 return 4;
             }
         }
     
         private class DisabledDownArrow extends DownArrow {
           
             public DisabledDownArrow() {
                 arrowColor = new Color(140, 140, 140);
             }
      
             public void paintIcon(Component c, Graphics g, int x, int y) {
                 super.paintIcon(c, g, x, y);
                 g.setColor(Color.white);
                 g.drawLine(x+3, y+2, x+4, y+1);
                 g.drawLine(x+3, y+3, x+5, y+1);
             }
         }
     
         private class ToolBar extends JToolBar {
             public void updateUI() {
                 super.updateUI();
                 setBorder(null);
             }
         }
     }
 
     public void applyPressed() 
     {
         apply();
     }
   
     public void setEnabled(boolean enabled) {
         searchVdButton.setEnabled(enabled);
     }
   
     public void apply() 
     {
         if(!modified)
             return;
         modified = false;
 
         if(node.getUserObject() instanceof DataElement) 
             vd = ((DataElement)node.getUserObject()).getValueDomain();
       
         if(!isLVD){
             if(tempVD != null) {
              ((DataElement)node.getUserObject()).setValueDomain(tempVD);
             
             }
         }
         else{
             isLVD = false;
             ((DataElement)node.getUserObject()).setValueDomain(selectedLVD);
         }
         firePropertyChangeEvent(new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, false));
         firePropertyChangeEvent(new PropertyChangeEvent(this, ApplyButtonPanel.REVIEW, null, true));
         fireElementChangeEvent(new ElementChangeEvent(node));
     }
   
     public void updateNode(UMLNode node) 
     {
         this.node = node;
         if(node.getUserObject() instanceof DataElement) {
             DataElement de = (DataElement)node.getUserObject();
             vd = de.getValueDomain();
             searchVdButton.setVisible(DEMappingUtil.isMappedToLocalVD(de) == null);
 
             vdLongNameValueLabel.setText(vd.getLongName()); 
       
             if(vd != null && !StringUtil.isEmpty(vd.getPublicId())) {
                 vdContextNameValueLabel.setText(vd.getContext().getName());
                 vdVersionValueLabel.setText(vd.getVersion().toString());
                 vdPublicIdValueLabel.setText(vd.getPublicId());
                 vdDatatypeValueLabel.setText(vd.getDataType());
                 
                 vdLongNameTitleLabel.setVisible(true);
                 vdPublicIdTitleLabel.setVisible(true);
                 vdContextNameTitleLabel.setVisible(true);
                 vdVersionTitleLabel.setVisible(true);
                 vdDatatypeTitleLabel.setVisible(true);
             }
             else 
             { 
                 vdContextNameValueLabel.setText("");
                 vdVersionValueLabel.setText("");
                 vdPublicIdValueLabel.setText("");
                 vdDatatypeValueLabel.setText("");
             }
       
             if(vdLongNameValueLabel.getText().equals(""))
                 vdLongNameTitleLabel.setVisible(false);
             if(vdVersionValueLabel.getText().equals(""))
                 vdVersionTitleLabel.setVisible(false);
             if(vdPublicIdValueLabel.getText().equals(""))
                 vdPublicIdTitleLabel.setVisible(false);
             if(vdDatatypeValueLabel.getText() == null || vdDatatypeValueLabel.getText().equals(""))
                 vdDatatypeTitleLabel.setVisible(false);
             if(vdContextNameValueLabel.getText().equals(""))
                 vdContextNameTitleLabel.setVisible(false);
 
             if(DEMappingUtil.isMappedToLocalVD(de) != null) {
                 lvdTitleLabel.setVisible(true);
                 lvdValueLabel.setText(LookupUtil.lookupFullName(vd));
             } else {
                 lvdTitleLabel.setVisible(false);
             }
         }
     }
 
     public void addPropertyChangeListener(PropertyChangeListener l) {
         propChangeListeners.add(l);
     }
     public void addElementChangeListener(ElementChangeListener listener) {
         changeListeners.add(listener);
     }
   
     private void firePropertyChangeEvent(PropertyChangeEvent evt) {
         for(PropertyChangeListener l : propChangeListeners) 
             l.propertyChange(evt);
     }
   
     private void fireElementChangeEvent(ElementChangeEvent event) {
         for(ElementChangeListener l : changeListeners)
             l.elementChanged(event);
     }
 }
