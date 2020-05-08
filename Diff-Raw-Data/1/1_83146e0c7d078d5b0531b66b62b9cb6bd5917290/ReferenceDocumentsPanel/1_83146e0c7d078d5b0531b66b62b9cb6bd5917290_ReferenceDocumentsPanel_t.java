 package gov.nih.nci.ncicb.cadsr.loader.ui;
 
 import gov.nih.nci.ncicb.cadsr.domain.ReferenceDocument;
 import gov.nih.nci.ncicb.cadsr.domain.DomainObjectFactory;
 
import gov.nih.nci.ncicb.cadsr.loader.ext.CadsrModule;
 import gov.nih.nci.ncicb.cadsr.loader.ui.util.UIUtil;
 
 import java.awt.Dimension;
 import java.awt.GridBagLayout;
 
 import java.util.*;
 import javax.swing.*;
 
 public class ReferenceDocumentsPanel extends JPanel {
   
   private List<ReferenceDocument> refDocs = new ArrayList<ReferenceDocument>();
 
   private JLabel languageLabel = new JLabel("Select Language"),
     typeLabel = new JLabel("Select Reference Document Type"),
     nameLabel = new JLabel("Create Reference Document Name (Maximum 255 Characters)"),
     textLabel = new JLabel("Create Reference Document Text (Maximum 4000 Characters)"), 
     urlLabel = new JLabel("Create Reference Document URL (Maximum 240 Characters)");
  
   private JTextArea textTextArea = new JTextArea();
 
   private JComboBox languageCombo = new JComboBox();
 
   private CadsrModule cadsrModule = null;
 
   public ReferenceDocumentsPanel() {
     initUI();
   }
   
 
   private void initUI() {
     this.setLayout(new GridBagLayout());
 
     UIUtil.insertInBag(this, languageLabel, 0, 0);
     UIUtil.insertInBag(this, languageCombo, 0, 1); 
 
     textTextArea.setLineWrap(true);
     textTextArea.setWrapStyleWord(true);
     JScrollPane textScrollPane = new JScrollPane(textTextArea);
     textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
     textScrollPane.setPreferredSize(new Dimension(200, 100));
 
     UIUtil.insertInBag(this, textLabel, 0, 6);
     UIUtil.insertInBag(this, textScrollPane, 0, 7); 
 
   }
 
   public void setReferenceDocuments(List<ReferenceDocument> refDocs) {
     this.refDocs = refDocs;
   }
 
   private void addReferenceDocument(String language, 
                                     String type,
                                     String name,
                                     String text,
                                     String url) {
 
     ReferenceDocument refDoc = DomainObjectFactory.newReferenceDocument();
     refDoc.setLanguage(language);
     refDoc.setType(type);
     refDoc.setName(name);
     refDoc.setText(text);
     refDoc.setUrl(url);
     
   }
   
   public static void main(String[] args) {
     JFrame f = new JFrame();
     f.setSize(500, 500);
     f.add(new ReferenceDocumentsPanel());
     f.setVisible(true);
   }
 
   public void setCadsrModule(CadsrModule cadsrModule){
     this.cadsrModule = cadsrModule;
   }
 
 
 }
