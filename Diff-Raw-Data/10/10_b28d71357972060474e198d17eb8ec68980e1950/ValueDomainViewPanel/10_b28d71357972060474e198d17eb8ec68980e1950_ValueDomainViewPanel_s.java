 package gov.nih.nci.ncicb.cadsr.loader.ui;
 import gov.nih.nci.ncicb.cadsr.domain.ValueDomain;
 import java.awt.*;
 import javax.swing.*;
 
 public class ValueDomainViewPanel extends JPanel
 {
   private JLabel vdPrefDefTitleLabel = new JLabel("VD Preferred Definition"),
     vdDatatypeTitleLabel = new JLabel("VD Datatype"),
     vdTypeTitleLabel = new JLabel("VD Type"),
     vdCdIdTitleLabel = new JLabel("VD CD PublicId / Version"),
     vdCdLongNameTitleLabel = new JLabel("VD CD Long Name");
   
   private JTextArea vdPrefDefValueTextField = new JTextArea();
   private JTextField vdDatatypeValueLabel = new JTextField(),
     vdTypeValueLabel = new JTextField(),
     vdCdIdValueLabel = new JTextField(),
     vdCdLongNameValueLabel = new JTextField();
   
   private JScrollPane scrollPane;
   
   private ValueDomain vd;
 
   public ValueDomainViewPanel(ValueDomain vd)
   {
     this.vd = vd;
     initValues();
     initUI();
   }
   
   public void update(ValueDomain vd) 
   {
     this.vd = vd;
     initValues();
   }
   
   private void initUI() 
   {
     this.setLayout(new BorderLayout());
     JPanel mainPanel = new JPanel(new GridBagLayout());
     
     vdPrefDefValueTextField.setEditable(false);
     vdDatatypeValueLabel.setEditable(false);
     vdTypeValueLabel.setEditable(false);
     vdCdIdValueLabel.setEditable(false);
     vdCdLongNameValueLabel.setEditable(false);
         
     vdPrefDefValueTextField.setLineWrap(true);
     vdPrefDefValueTextField.setWrapStyleWord(true);
     scrollPane = new JScrollPane(vdPrefDefValueTextField);
     scrollPane
       .setVerticalScrollBarPolicy
       (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
     //vdPrefDefValueTextField.setSize(200,200);
     scrollPane = new JScrollPane(vdPrefDefValueTextField);
     scrollPane.setPreferredSize(new Dimension(200, 100));
     
     insertInBag(mainPanel, vdPrefDefTitleLabel, 0, 1);
     insertInBag(mainPanel, scrollPane, 1, 1, 3, 1); 
     
     insertInBag(mainPanel, vdDatatypeTitleLabel, 0, 3);
     insertInBag(mainPanel, vdDatatypeValueLabel, 1, 3);
     
     insertInBag(mainPanel, vdTypeTitleLabel, 0, 4);
     insertInBag(mainPanel, vdTypeValueLabel, 1, 4);
     
     insertInBag(mainPanel, vdCdIdTitleLabel, 0, 5);
     insertInBag(mainPanel, vdCdIdValueLabel, 1, 5);
     
     insertInBag(mainPanel, vdCdLongNameTitleLabel, 0, 6);
     insertInBag(mainPanel, vdCdLongNameValueLabel, 1, 6);
     
     this.add(mainPanel);
   }
   
   private void initValues() 
   {
     vdPrefDefValueTextField.setText(vd.getPreferredDefinition());
     vdDatatypeValueLabel.setText(vd.getDataType());
     vdTypeValueLabel.setText(vd.getVdType());
    vdCdIdValueLabel.setText(vd.getConceptualDomain().getPublicId());
    vdCdLongNameValueLabel.setText(vd.getConceptualDomain().getLongName());
    
   }
   
   private void insertInBag(JPanel bagComp, Component comp, int x, int y) {
     insertInBag(bagComp, comp, x, y, 1, 1);
   }
   
   private void insertInBag(JPanel bagComp, Component comp, int x, int y, int width, int height) {
     JPanel p = new JPanel();
     p.add(comp);
 
     bagComp.add(p, new GridBagConstraints(x, y, width, height, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
   }
   
   public static void main(String args[]) 
   {
 //    JFrame frame = new JFrame();
 //    ValueDomainViewPanel vdPanel = new ValueDomainViewPanel();
 //    vdPanel.setVisible(true);
 //    frame.add(vdPanel);
 //    frame.setVisible(true);
 //    frame.setSize(450, 350);
   }
 
 }
