 package gov.nih.nci.ncicb.cadsr.loader.ui;
 
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.loader.event.*;
 
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.NavigationEvent;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.NavigationListener;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.*;
 
 import gov.nih.nci.ncicb.cadsr.loader.util.*;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeEvent;
 
 import java.util.*;
 import javax.swing.*;
 import java.awt.*;
 
 public class UMLElementViewPanel extends JPanel
   implements NavigationListener, Editable {
 
   private ConceptEditorPanel conceptEditorPanel;
   private DEPanel dePanel;
   private OCPanel ocPanel;
   private ButtonPanel buttonPanel;
 
   private UMLNode node;
 
   private JPanel cardPanel;
   
   private JPanel displayedPanel;
 
   static final String DE_PANEL_KEY = "dePanel", 
     OC_PANEL_KEY = "ocPanel",
     CONCEPT_PANEL_KEY = "conceptPanel";
     
   private Map<String, JPanel> panelKeyMap = new HashMap<String, JPanel>();
 
   public UMLElementViewPanel(UMLNode node) 
   {
     this.node = node;
   
     conceptEditorPanel = new ConceptEditorPanel(node);
     dePanel = new DEPanel(node);
     ocPanel = new OCPanel(node);
 
     if(node instanceof AttributeNode)
       buttonPanel = new ButtonPanel(conceptEditorPanel, this, dePanel);
     else if(node instanceof ValueMeaningNode)
       buttonPanel = new ButtonPanel(conceptEditorPanel, this, null);
     else 
       buttonPanel = new ButtonPanel(conceptEditorPanel, this, ocPanel);
 
     conceptEditorPanel.addPropertyChangeListener(buttonPanel);
     dePanel.addPropertyChangeListener(buttonPanel);
     ocPanel.addPropertyChangeListener(buttonPanel);
     cardPanel = new JPanel();
     initUI();
     updateNode(node);
   }
   
   private void initUI() {
 
     conceptEditorPanel.initUI();
     
     cardPanel.setLayout(new CardLayout());
     cardPanel.add(conceptEditorPanel, CONCEPT_PANEL_KEY);
     cardPanel.add(dePanel, DE_PANEL_KEY);
     cardPanel.add(ocPanel, OC_PANEL_KEY);
     
     panelKeyMap.put(CONCEPT_PANEL_KEY, conceptEditorPanel);
     panelKeyMap.put(DE_PANEL_KEY, dePanel);
     panelKeyMap.put(OC_PANEL_KEY, ocPanel);
     
     setLayout(new BorderLayout());
     
     JPanel newPanel = new JPanel();
     newPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); 
     JLabel space = new JLabel("      ");
     newPanel.add(space);
     newPanel.add(buttonPanel);
     
     this.add(cardPanel, BorderLayout.CENTER);
     this.add(newPanel, BorderLayout.SOUTH);
   }
   
   public void setEnabled(boolean enabled) {
       buttonPanel.setEnabled(enabled);
       conceptEditorPanel.setEnabled(enabled);
   }
   
   public void switchCards(String key) 
   {
     CardLayout layout = (CardLayout)cardPanel.getLayout();
     layout.show(cardPanel, key);
     displayedPanel = panelKeyMap.get(key);
   }
   
   public void updateNode(UMLNode node) 
   {
   
     this.node = node;
     // is UMLNode a de?
     Object o = node.getUserObject();
     if(o instanceof DataElement) { //if it is, does it have pubID
       DataElement de = (DataElement)o;
       if(!StringUtil.isEmpty(de.getPublicId())) 
       {
         switchCards(DE_PANEL_KEY);
       } else {
         switchCards(CONCEPT_PANEL_KEY);
       }
     } else if(o instanceof ObjectClass) {
       ObjectClass oc = (ObjectClass)o;
       if(!StringUtil.isEmpty(oc.getPublicId())) 
       {
         switchCards(OC_PANEL_KEY);
       }
       else 
       {
         switchCards(CONCEPT_PANEL_KEY);
       }
     } else if (o instanceof ValueMeaning) { 
       switchCards(CONCEPT_PANEL_KEY);
     }
       
     conceptEditorPanel.updateNode(node);
     dePanel.updateNode(node);
     ocPanel.updateNode(node);
 
     if(node instanceof AttributeNode)
       buttonPanel.setEditablePanel(dePanel);
     else if (node instanceof ValueMeaningNode)
       buttonPanel.setEditablePanel(null);
     else 
       buttonPanel.setEditablePanel(ocPanel);
 
 
     buttonPanel.propertyChange
       (new PropertyChangeEvent(this, ButtonPanel.SETUP, null, true));
     
     buttonPanel.update();
     
   }
 
 //  public boolean isReviewed() 
 //  {
 //    return ((ReviewableUMLNode)node).isReviewed();
 //  }
 
   public void apply(boolean value) throws ApplyException
   {
     conceptEditorPanel.apply(value);
   }
 
   public void navigate(NavigationEvent evt) {  
     buttonPanel.navigate(evt);
   }
 
   public void addReviewListener(ReviewListener listener) {
     buttonPanel.addReviewListener(listener);
   }
 
   public void addNavigationListener(NavigationListener listener) 
   {
     buttonPanel.addNavigationListener(listener);
   }
   
   public void addElementChangeListener(ElementChangeListener listener) {
     conceptEditorPanel.addElementChangeListener(listener);
     dePanel.addElementChangeListener(listener);
   }
 
   public void addPropertyChangeListener(PropertyChangeListener l) {
     conceptEditorPanel.addPropertyChangeListener(l);
     buttonPanel.addPropertyChangeListener(l);
     dePanel.addPropertyChangeListener(l);
     ocPanel.addPropertyChangeListener(l);
   }
   
   public void applyPressed() throws ApplyException
   {
     if(displayedPanel instanceof Editable) 
     {
      ((Editable)displayedPanel).applyPressed();
     }
   }
     
   public ConceptEditorPanel getConceptEditorPanel() {
     return conceptEditorPanel;
   }
 
 }
 
       
