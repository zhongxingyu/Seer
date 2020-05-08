 package gov.nih.nci.ncicb.cadsr.loader.ui;
 import gov.nih.nci.ncicb.cadsr.domain.DataElement;
 import gov.nih.nci.ncicb.cadsr.domain.ValueDomain;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.UMLNode;
 import gov.nih.nci.ncicb.cadsr.loader.util.StringUtil;
 import gov.nih.nci.ncicb.cadsr.loader.util.BeansAccessor;
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import javax.swing.*;
 
 import java.util.List;
 import java.util.ArrayList;
 public class VDPanel extends JPanel 
 {
   private JButton searchVdButton = new JButton("Search Value Domain");
   
   private JLabel vdLongNameTitleLabel = new JLabel("Long Name"),
   vdLongNameValueLabel = new JLabel(),
   vdContextNameTitleLabel = new JLabel("Context Name"),
   vdContextNameValueLabel = new JLabel(),
   vdVersionTitleLabel = new JLabel("Version"),
   vdVersionValueLabel = new JLabel();
 
   private static final String SEARCH = "SEARCH";
   
   private List<PropertyChangeListener> propChangeListeners 
     = new ArrayList<PropertyChangeListener>(); 
   
   private ValueDomain tempVD, vd;
   private UMLNode node;
 
   public VDPanel(UMLNode node)
   {
     this.node = node;
     if(node.getUserObject() instanceof DataElement) 
        vd = ((DataElement)node.getUserObject()).getValueDomain();
     
     this.setLayout(new BorderLayout());
     JPanel mainPanel = new JPanel(new GridBagLayout());
     
     insertInBag(mainPanel, vdLongNameTitleLabel, 0, 0);
     insertInBag(mainPanel, vdLongNameValueLabel, 1, 0);
     insertInBag(mainPanel, vdContextNameTitleLabel, 0, 1);
     insertInBag(mainPanel, vdContextNameValueLabel, 1, 1);
     insertInBag(mainPanel, vdVersionTitleLabel, 0, 2);
     insertInBag(mainPanel, vdVersionValueLabel, 1, 2);
     
     insertInBag(mainPanel, searchVdButton, 1, 3, 2, 1);
 
     mainPanel.setBorder
         (BorderFactory.createTitledBorder("Value Domain"));
 
     
     this.add(mainPanel);
     this.setSize(300, 300);
     
     searchVdButton.setActionCommand(SEARCH);
     
     searchVdButton.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent event) {
         JButton button = (JButton)event.getSource();
         if(button.getActionCommand().equals(SEARCH)) {
           CadsrDialog cd = BeansAccessor.getCadsrVDDialog();
           cd.setVisible(true);
         
           tempVD = (ValueDomain)cd.getAdminComponent();
           if(tempVD != null) {
             vdLongNameValueLabel.setText(tempVD.getLongName());
             vdContextNameValueLabel.setText(tempVD.getContext().getName());
             vdVersionValueLabel.setText(tempVD.getVersion().toString());
             
             firePropertyChangeEvent(
                 new PropertyChangeEvent(this, ButtonPanel.SAVE, null, true));
           }
         }
       }
   });
 }
   
   public void applyPressed() 
   {
     apply();
   }
   
   public void apply() 
   {
    vd.setLongName(tempVD.getLongName());
    vd.setPublicId(tempVD.getPublicId());
    vd.setVersion(tempVD.getVersion());
    vd.setContext(tempVD.getContext());
   }
   
   public void updateNode(UMLNode node) 
   {
     this.node = node;
     if(node.getUserObject() instanceof DataElement) {
        vd = ((DataElement)node.getUserObject()).getValueDomain();
        
        
        if(vd != null && !StringUtil.isEmpty(vd.getPublicId())) {
          vdLongNameValueLabel.setText(vd.getLongName());
          vdContextNameValueLabel.setText(vd.getContext().getName());
          vdVersionValueLabel.setText(vd.getVersion().toString());
        }
        else 
          {
            vdLongNameValueLabel.setText("");
            vdContextNameValueLabel.setText("");
            vdVersionValueLabel.setText("");
          }
     }
   }
 
   public void addPropertyChangeListener(PropertyChangeListener l) {
     propChangeListeners.add(l);
   }
   
   private void firePropertyChangeEvent(PropertyChangeEvent evt) {
     for(PropertyChangeListener l : propChangeListeners) 
       l.propertyChange(evt);
   }
   
   
   
   private void insertInBag(JPanel bagComp, Component comp, int x, int y) {
     insertInBag(bagComp, comp, x, y, 1, 1);
   }
   
   private void insertInBag(JPanel bagComp, Component comp, int x, int y, int width, int height) {
     JPanel p = new JPanel();
     p.add(comp);
 
     bagComp.add(p, new GridBagConstraints(x, y, width, height, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
   }
 
     public static void main(String[] args)
   {
 //    JFrame frame = new JFrame();
 //    VDPanel vdPanel = new VDPanel();
 //    vdPanel.setVisible(true);
 //    frame.add(vdPanel);
 //    frame.setVisible(true);
 //    frame.setSize(450, 350);
   }
 }
