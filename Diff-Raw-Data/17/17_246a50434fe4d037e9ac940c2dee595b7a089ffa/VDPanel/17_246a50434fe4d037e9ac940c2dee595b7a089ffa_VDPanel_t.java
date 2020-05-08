 package gov.nih.nci.ncicb.cadsr.loader.ui;
 import gov.nih.nci.ncicb.cadsr.domain.DataElement;
 import gov.nih.nci.ncicb.cadsr.domain.DomainObjectFactory;
 import gov.nih.nci.ncicb.cadsr.domain.ValueDomain;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
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
  vdLongNameValueLabel = new JLabel(""),
   vdPublicIdTitleLabel = new JLabel("Public Id"),
  vdPublicIdValueLabel = new JLabel(""),
   vdContextNameTitleLabel = new JLabel("Context Name"),
  vdContextNameValueLabel = new JLabel(""),
   vdVersionTitleLabel = new JLabel("Version"),
  vdVersionValueLabel = new JLabel(""),
   vdDatatypeTitleLabel = new JLabel("Datatype"),
  vdDatatypeValueLabel = new JLabel("");
 
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
     insertInBag(mainPanel, vdPublicIdTitleLabel, 0, 1);
     insertInBag(mainPanel, vdPublicIdValueLabel, 1, 1);
     insertInBag(mainPanel, vdContextNameTitleLabel, 0, 2);
     insertInBag(mainPanel, vdContextNameValueLabel, 1, 2);
     insertInBag(mainPanel, vdVersionTitleLabel, 0, 3);
     insertInBag(mainPanel, vdVersionValueLabel, 1, 3);
     insertInBag(mainPanel, vdDatatypeTitleLabel, 0, 4);
     insertInBag(mainPanel, vdDatatypeValueLabel, 1, 4);
     
     insertInBag(mainPanel, searchVdButton, 1, 5, 2, 1);
 
     mainPanel.setBorder
         (BorderFactory.createTitledBorder("Value Domain"));
 
     
     this.add(mainPanel);
     this.setSize(300, 300);
     
     searchVdButton.setActionCommand(SEARCH);
     searchVdButton.setVisible(!isMappedToLocalVD());
       
     searchVdButton.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent event) {
         JButton button = (JButton)event.getSource();
         if(button.getActionCommand().equals(SEARCH)) {
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
     if(tempVD != null) {
       vd.setLongName(tempVD.getLongName());
       vd.setPublicId(tempVD.getPublicId());
       vd.setVersion(tempVD.getVersion());
       vd.setContext(tempVD.getContext());
       vd.setDataType(tempVD.getDataType());
     }
   }
   
   public void updateNode(UMLNode node) 
   {
     this.node = node;
     if(node.getUserObject() instanceof DataElement) {
        vd = ((DataElement)node.getUserObject()).getValueDomain();
        searchVdButton.setVisible(!isMappedToLocalVD());
 
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
 
   public boolean isMappedToLocalVD() 
   {
     if(node.getUserObject() instanceof DataElement) {
     ValueDomain vd = ((DataElement)node.getUserObject()).getValueDomain();
     ElementsLists elements = ElementsLists.getInstance();
     List<ValueDomain> vds = elements.getElements(DomainObjectFactory.newValueDomain());
     if(vd.getPublicId() != null)
       return false;
       
     if(vds != null) {
       for(ValueDomain currentVd : vds) 
         if(currentVd.getLongName().equals(vd.getLongName()))
           return true;
           
     }
     }
     return false;
   }
 
 }
