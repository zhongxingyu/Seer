 package gov.nih.nci.ncicb.cadsr.loader.ui;
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 import gov.nih.nci.ncicb.cadsr.loader.UserSelections;
 import gov.nih.nci.ncicb.cadsr.loader.event.ElementChangeEvent;
 import gov.nih.nci.ncicb.cadsr.loader.event.ElementChangeListener;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.*;
 import gov.nih.nci.ncicb.cadsr.loader.ui.util.UIUtil;
 import gov.nih.nci.ncicb.cadsr.loader.util.*;
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import javax.swing.*;
 import javax.swing.text.PlainDocument;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Arrays;
 
 public class ConceptEditorPanel extends JPanel 
   implements KeyListener,
   UserPreferencesListener, Editable
 {
 
   private List<PropertyChangeListener> propChangeListeners 
     = new ArrayList<PropertyChangeListener>();
     
   private List<ElementChangeListener> changeListeners 
     = new ArrayList<ElementChangeListener>();
 
   private ConceptEditorPanel _this = this;
   private UMLNode node;
 
   private Concept[] concepts;
   private ConceptUI[] conceptUIs;
   
   private JPanel gridPanel;
   private JScrollPane scrollPane;
   
   private JLabel conceptLabel = new JLabel();
   private JLabel nameLabel = new JLabel();
   
   private boolean remove = false,
     orderChanged = false;
     
   private static EvsDialog evsDialog;
   private VDPanel vdPanel;
     
   private static boolean editable = false;
   static {
     UserSelections selections = UserSelections.getInstance();
     editable = selections.getProperty("MODE").equals(RunMode.Curator);
   }
   
   private UserPreferences prefs = UserPreferences.getInstance();
 
   public ConceptEditorPanel(UMLNode node) 
   {
     this.node = node;
     initConcepts();
 
     vdPanel = new VDPanel(node);
   }
 
   
   public void addPropertyChangeListener(PropertyChangeListener l) {
     propChangeListeners.add(l);
 
     if(node.getUserObject() instanceof DataElement)
       vdPanel.addPropertyChangeListener(l);
   }
 
   private void firePropertyChangeEvent(PropertyChangeEvent evt) {
     for(PropertyChangeListener l : propChangeListeners) 
       l.propertyChange(evt);
   }
   
   public void updateNode(UMLNode node) 
   {
     this.node = node;
     initConcepts();
     updateConcepts(concepts);
     if(node.getUserObject() instanceof DataElement) {
       vdPanel.updateNode(node);
     }
   }
 
   private AdminComponent checkForDuplicateMapping() {
     Concept[] tempConcepts = new Concept[conceptUIs.length];
 
     if(tempConcepts.length == 0)
       return null;
 
     for(int i = 0 ; i < conceptUIs.length; i++) {
       Concept con = DomainObjectFactory.newConcept();
       con.setPreferredName(conceptUIs[i].code.getText());
       tempConcepts[i] = con;
     }
 
     String newPrefName = ConceptUtil.preferredNameFromConcepts(Arrays.asList(tempConcepts));
     
     if(node.getUserObject() instanceof ObjectClass) {
       List<ObjectClass> ocs = ElementsLists.getInstance().
         getElements(DomainObjectFactory.newObjectClass());
 
       ObjectClass currentOc = (ObjectClass)node.getUserObject();
 
       for(ObjectClass oc : ocs) {
         if(currentOc != oc) {
           if(newPrefName.equals(oc.getPreferredName())) 
             return oc;
         }
       }
       return null;    
     } else if (node.getUserObject() instanceof DataElement) {
       DataElement currentDe = 
         (DataElement)node.getUserObject();
       List<ObjectClass> ocs = ElementsLists.getInstance().
         getElements(DomainObjectFactory.newObjectClass());
       List<DataElement> des = ElementsLists.getInstance().
         getElements(DomainObjectFactory.newDataElement());
 
       if(des != null && ocs != null) {
         for(DataElement de : des) {
           if((de.getDataElementConcept().getObjectClass() == 
               currentDe.getDataElementConcept().getObjectClass())
              && (de != currentDe))
             if(newPrefName
                .equals
                (de.getDataElementConcept()
                 .getProperty().getPreferredName()))
               return de;
         }
       }
       return null;
     }
     
     // shouldn't get to here.
     return null;
   }
   
   private void initConcepts() 
   {
     if(node instanceof AssociationNode) {
         concepts = NodeUtil.getAssociationConcepts((AssociationNode)node);
     }
     else if(node instanceof AssociationEndNode) {
       AssociationEndNode assocNode = (AssociationEndNode)node;
       if(assocNode.getType() == AssociationEndNode.TYPE_SOURCE)
         concepts = NodeUtil.getAssociationSourceConcepts(assocNode.getParent());
       else
         concepts = NodeUtil.getAssociationTargetConcepts(assocNode.getParent());
     } 
     else {
       concepts = NodeUtil.getConceptsFromNode(node);
     }
   }
 
   Concept[] getConcepts() 
   {
    return concepts; 
   }
 
   public UMLNode getNode() 
   {
     return node;
   }
   
   public VDPanel getVDPanel() 
   {
     return vdPanel;
   }
   
   boolean areAllFieldEntered() {
      for(int i=0; i < conceptUIs.length; i++) {
       if(conceptUIs[i].code.getText().trim().equals("")
          | conceptUIs[i].name.getText().trim().equals("")
          | conceptUIs[i].defSource.getText().trim().equals("")
          | conceptUIs[i].def.getText().trim().equals("")) {
         return false;
       } 
     }      
     return true;
   }
 
   
   private void updateConcepts(Concept[] concepts) {
     this.concepts = concepts;
     this.removeAll();
     initUI();
     this.updateUI();
   }
   
   public void setEnabled(boolean enabled) {
       for(int i = 0; i<concepts.length; i++) {
         conceptUIs[i].setEnabled(enabled);
       }
       vdPanel.setEnabled(enabled);
   }
   
   public void apply(boolean toAll) throws ApplyException {
     boolean update = remove;
     remove = false;
     Concept[] newConcepts = new Concept[concepts.length];
 
     AdminComponent ac = checkForDuplicateMapping();
     if(ac != null) {
       if(ac instanceof ObjectClass)
       JOptionPane.showMessageDialog 
         (null,
          "Duplicate Mapping with " + LookupUtil.lookupFullName((ObjectClass)ac),
          "Same Concept List",
          JOptionPane.ERROR_MESSAGE);
       if(ac instanceof DataElement)
       JOptionPane.showMessageDialog 
         (null,
          "Duplicate Mapping with " + LookupUtil.lookupFullName((DataElement)ac),
          "Same Concept List",
          JOptionPane.ERROR_MESSAGE);
       //throw new ApplyException("Same Concept List");
     }
     
     
     for(int i = 0; i<concepts.length; i++) {
       newConcepts[i] = concepts[i];
       // concept code has not changed
       if(conceptUIs[i].code.getText().equals(concepts[i].getPreferredName())) {
         concepts[i].setLongName(conceptUIs[i].name.getText());
         concepts[i].setPreferredDefinition(conceptUIs[i].def.getText());
         concepts[i].setDefinitionSource(conceptUIs[i].defSource.getText());
       } else { // concept code has changed
         Concept concept = DomainObjectFactory.newConcept();
         concept.setPreferredName(conceptUIs[i].code.getText());
         concept.setLongName(conceptUIs[i].name.getText());
         concept.setPreferredDefinition(conceptUIs[i].def.getText());
         concept.setDefinitionSource(conceptUIs[i].defSource.getText());
         newConcepts[i] = concept;
         update = true;
       }
     }
     
     update = orderChanged | update;
     
     if(update) {
       Object o = node.getUserObject();
       if(toAll) {
         if(o instanceof DataElement) {
           DataElement de = (DataElement)o;
           String attName = de.getDataElementConcept().getProperty().getLongName();
           ObjectUpdater.updateByAltName(attName, concepts, newConcepts);
 
           // sent change tracker event for each element
           List<DataElement> des = (List<DataElement>)ElementsLists.getInstance().getElements(DomainObjectFactory.newDataElement().getClass());
           for(DataElement _de : des) {
             if(_de.getDataElementConcept().getProperty().getLongName().equals(attName)) {
               fireElementChangeEvent(new ElementChangeEvent(_de));
             }
           }
         }
       } 
       if(o instanceof ValueMeaning) {
         ValueMeaning vm = (ValueMeaning)o;
         ObjectUpdater.update(vm, concepts, newConcepts);
       } else if(o instanceof ObjectClassRelationship) {
         ObjectClassRelationship ocr = (ObjectClassRelationship)o;
         if (node instanceof AssociationNode) {
             ObjectUpdater.updateAssociation(ocr, concepts, newConcepts);
         }
         else {
             AssociationEndNode endNode = (AssociationEndNode)node;
             if(endNode.getType() == AssociationEndNode.TYPE_SOURCE)
               ObjectUpdater.updateAssociationSource(ocr, concepts, newConcepts);
             else
               ObjectUpdater.updateAssociationTarget(ocr, concepts, newConcepts);
         }
       }
       else
         ObjectUpdater.update((AdminComponent)node.getUserObject(), concepts, newConcepts);
       
       concepts = newConcepts;
     } 
 
     orderChanged = false;
     
     firePropertyChangeEvent(
       new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, false));
     firePropertyChangeEvent(
       new PropertyChangeEvent(this, AddButtonPanel.ADD, null, true));
     firePropertyChangeEvent(
       new PropertyChangeEvent(this, ApplyButtonPanel.REVIEW, null, true));
 //    firePropertyChangeEvent(
 //      new PropertyChangeEvent(this, ButtonPanel.SWITCH, null, false));
 
     fireElementChangeEvent(new ElementChangeEvent(node));
   }
     
     
   void initUI() {
     prefs.addUserPreferencesListener(this);
     this.setLayout(new BorderLayout());
     
     JPanel summaryPanel = new JPanel(new GridBagLayout());
     JLabel summaryTitle = new JLabel("UML Concept Code Summary: ");
     
     JLabel summaryNameTitle = new JLabel("UML Concept Name Summary: ");
     
     insertInBag(summaryPanel, summaryTitle, 0, 0);
     insertInBag(summaryPanel, conceptLabel, 1, 0);
     insertInBag(summaryPanel, summaryNameTitle, 0, 1);
     insertInBag(summaryPanel, nameLabel, 1, 1);
 
     this.add(summaryPanel,BorderLayout.NORTH); 
     
     initViewPanel();
 
     firePropertyChangeEvent(
       new PropertyChangeEvent(this, AddButtonPanel.ADD, null, areAllFieldEntered()));
     firePropertyChangeEvent(
       new PropertyChangeEvent(this, ApplyButtonPanel.REVIEW, null, areAllFieldEntered()));
 
   }
   
   private void initViewPanel() {
 
     gridPanel = new JPanel(new GridBagLayout());
 
     scrollPane = new JScrollPane(gridPanel);
//    scrollPane.getVerticalScrollBar().setUnitIncrement(30);
    
 
     conceptUIs = new ConceptUI[concepts.length];
     JPanel[] conceptPanels = new JPanel[concepts.length];
 
     if(prefs.getUmlDescriptionOrder().equals("first"))
       insertInBag(gridPanel, UIUtil.createDescriptionPanel(node), 0, 0);
 
     boolean primaryConceptFirst = prefs.getOrderOfConcepts().equalsIgnoreCase("first");
     
     for(int i = 0; i<concepts.length; i++) {
       conceptUIs[i] = new ConceptUI(concepts[i]);
 
       String title = i == 0?"Primary Concept":"Qualifier Concept" +" #" + (i);
 
       conceptPanels[i] = new JPanel();
       conceptPanels[i].setBorder
         (BorderFactory.createTitledBorder(title));
 
       conceptPanels[i].setLayout(new BorderLayout());
 
       JPanel mainPanel = new JPanel(new GridBagLayout());
 
       insertInBag(mainPanel, conceptUIs[i].labels[0], 0, 0);
       insertInBag(mainPanel, conceptUIs[i].labels[1], 0, 1);
       insertInBag(mainPanel, conceptUIs[i].labels[2], 0, 2);
       insertInBag(mainPanel, conceptUIs[i].labels[3], 0, 3);
 
       insertInBag(mainPanel, conceptUIs[i].code, 1, 0, 2, 1);
       insertInBag(mainPanel, conceptUIs[i].name, 1, 1, 2, 1);
       insertInBag(mainPanel, conceptUIs[i].defScrollPane, 1, 2, 2, 1);
       insertInBag(mainPanel, conceptUIs[i].defSource, 1, 3,1, 1);
 
       JButton evsButton = conceptUIs[i].getEvsButton();
       insertInBag(mainPanel, evsButton, 2, 3);
       
       JButton upButton = conceptUIs[i].getUpButton();
       JButton deleteButton = conceptUIs[i].getDeleteButton();
       JButton downButton = conceptUIs[i].getDownButton();
 
       JPanel arrowPanel = new JPanel(new GridBagLayout());
       insertInBag(arrowPanel, upButton, 0, 0);
       insertInBag(arrowPanel, deleteButton, 0, 1);
       insertInBag(arrowPanel, downButton, 0, 2);
       
       conceptPanels[i].add(mainPanel, BorderLayout.CENTER);
       conceptPanels[i].add(arrowPanel, BorderLayout.EAST);
       
       //display Primary Concept first
       if(primaryConceptFirst)
         insertInBag(gridPanel, conceptPanels[i], 0, i+1);
       //display Qualifiers first
       else
         insertInBag(gridPanel, conceptPanels[i], 0, conceptPanels.length - i);
 
       conceptUIs[i].code.addKeyListener(this);
       conceptUIs[i].name.addKeyListener(this);
       conceptUIs[i].def.addKeyListener(this);
       conceptUIs[i].defSource.addKeyListener(this);
 
       final int index = i;
       if(!primaryConceptFirst ){
         if(index == 0)
           downButton.setVisible(false);
         if(index == concepts.length-1)
           upButton.setVisible(false);
       }
       else {
         if(index == 0)
           upButton.setVisible(false);
         if(index == concepts.length-1)
           downButton.setVisible(false);
       }
 
 //       if(concepts.length <= 1)
 //         deleteButton.setVisible(false);
       
       deleteButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event) {
           removePressed(index);
         }
       });
       
       ActionListener action1 = new ActionListener() {
         public void actionPerformed(ActionEvent event) {
           Concept temp = concepts[index-1];
           concepts[index-1] = concepts[index];
           concepts[index] = temp;
           updateConcepts(concepts);
 
           orderChanged = true;
 
           firePropertyChangeEvent(
             new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, areAllFieldEntered()));
           firePropertyChangeEvent(
             new PropertyChangeEvent(this, ApplyButtonPanel.REVIEW, null, false));
           firePropertyChangeEvent(
             new PropertyChangeEvent(this, AddButtonPanel.ADD, null, false));          
         }
       };
       
       ActionListener action2 = new ActionListener() {
         public void actionPerformed(ActionEvent event) {
           Concept temp = concepts[index];
           concepts[index] = concepts[index+1];
           concepts[index+1] = temp;
           updateConcepts(concepts);
 
           orderChanged = true;
 
           firePropertyChangeEvent(
             new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, areAllFieldEntered()));          
           //setSaveButtonState(areAllFieldEntered());
           firePropertyChangeEvent(
             new PropertyChangeEvent(this, ApplyButtonPanel.REVIEW, null, false));
           //reviewButton.setEnabled(false);
           firePropertyChangeEvent(
             new PropertyChangeEvent(this, AddButtonPanel.ADD, null, false)); 
           //addButton.setEnabled(false);
         }
       };
       
       downButton.addActionListener(primaryConceptFirst?action2:action1);
       
       upButton.addActionListener(primaryConceptFirst?action1:action2);
 
 
       evsButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent event) {
             if (evsDialog == null)
               evsDialog = new EvsDialog();
 
             UIUtil.putToCenter(evsDialog);
             
             if(prefs.getEvsAutoSearch())
               evsDialog.startSearch(conceptUIs[index].name.getText(), EvsDialog.SYNONYMS);
             evsDialog.setVisible(true);
 
             Concept c = evsDialog.getConcept();
 
             if(c.getPreferredDefinition().length() > 2000) {
               JOptionPane.showMessageDialog
                 (null, PropertyAccessor.getProperty("ui.concept.definition.too.long"), "Definition Too Long", JOptionPane.ERROR_MESSAGE);
               return;
             }
 
             if(c != null) {
               conceptUIs[index].code.setText(c.getPreferredName());
               conceptUIs[index].name.setText(c.getLongName());
               conceptUIs[index].def.setText(c.getPreferredDefinition());
               conceptUIs[index].defSource.setText(c.getDefinitionSource());
 
               if(areAllFieldEntered()) {
               firePropertyChangeEvent(
                 new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, true)); 
               } else {
                 firePropertyChangeEvent(
                   new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, false)); 
               }
               firePropertyChangeEvent(
                 new PropertyChangeEvent(this, ApplyButtonPanel.REVIEW, null, false)); 
             }
           }
         });
       
     }
     UserSelections selections = UserSelections.getInstance();
     if(node.getUserObject() instanceof DataElement
       && !selections.getProperty("MODE").equals(RunMode.Curator))
       insertInBag(gridPanel, vdPanel, 0, concepts.length + 1);
     updateHeaderLabels();
     
     if(prefs.getUmlDescriptionOrder().equals("last"))
       insertInBag(gridPanel, UIUtil.createDescriptionPanel(node), 0, concepts.length + 2); 
 
     this.add(scrollPane, BorderLayout.CENTER);
     
   }
   
 
   
   private void updateHeaderLabels()
   {
     String s = "";
     for(int i = 0; i < concepts.length; i++) {
       s = conceptUIs[i].code.getText() + " " + s; 
     }
     conceptLabel.setText(s);
     
     s = "";
     for(int i = 0; i < concepts.length; i++) {
       s = conceptUIs[i].name.getText() + " " + s; 
     }
     nameLabel.setText(s);
   }
   
   
   public void applyPressed() throws ApplyException {
     updateHeaderLabels();
     apply(false);
     if(node.getUserObject() instanceof DataElement)
       vdPanel.applyPressed();
   }
   
   public void addPressed() 
   {
     Concept[] newConcepts = new Concept[concepts.length + 1];
       for(int i = 0; i<concepts.length; i++) {
         newConcepts[i] = concepts[i];
       }
       Concept concept = DomainObjectFactory.newConcept();
       concept.setPreferredName("");
       concept.setLongName("");
       concept.setDefinitionSource("");
       concept.setPreferredDefinition("");
       newConcepts[newConcepts.length - 1] = concept;
       concepts = newConcepts;
 
       this.remove(scrollPane);
       initViewPanel();
       this.updateUI();
 
       firePropertyChangeEvent
         (new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, areAllFieldEntered()));
       firePropertyChangeEvent(
                 new PropertyChangeEvent(this, AddButtonPanel.ADD, null, false));
       firePropertyChangeEvent(
                 new PropertyChangeEvent(this, ApplyButtonPanel.REVIEW, null, false));
   }
   
   
   public void removePressed() 
   {
     removePressed(0);
   }
 
   
   /**
    * @parameter index index starting with 0, 0 is the primaryConcept. n is conceptQualifierN
    */
   public void removePressed(int index) 
   {
       Concept[] newConcepts = new Concept[concepts.length - 1];
       for(int i = 0, j=0; i<newConcepts.length; i++) {
         if(i == index) 
           j++;
         
           newConcepts[i] = concepts[j++];
         
       }
       concepts = newConcepts;
       
       _this.remove(scrollPane);
       initViewPanel();
       
       if(areAllFieldEntered()) {
         firePropertyChangeEvent(
                 new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, true));
       } else {
         firePropertyChangeEvent(
                 new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, false));
       }
       firePropertyChangeEvent(
                 new PropertyChangeEvent(this, AddButtonPanel.ADD, null, false));
       firePropertyChangeEvent(
                 new PropertyChangeEvent(this, ApplyButtonPanel.REVIEW, null, false));
       
 //       if(concepts.length < 2)
 //         firePropertyChangeEvent(
 //                 new PropertyChangeEvent(this, ButtonPanel.DELETE, null, false));
       this.updateUI();
 
       remove = true;
   }
   
   public void setRemove(boolean value) 
   {
     remove = value;
   }
   
   public void keyTyped(KeyEvent evt) {}
   public void keyPressed(KeyEvent evt) {}
 
   /**
    *  Text Change Use Case.
    */
   public void keyReleased(KeyEvent evt) {
     if(areAllFieldEntered()) {
       firePropertyChangeEvent(
                 new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, true));
     } else {
       firePropertyChangeEvent(
                 new PropertyChangeEvent(this, ApplyButtonPanel.SAVE, null, false));
     }
     firePropertyChangeEvent(
                 new PropertyChangeEvent(this, ApplyButtonPanel.REVIEW, null, false));
     updateHeaderLabels();
   }
   
   public void preferenceChange(UserPreferencesEvent event) 
   {
     if(event.getTypeOfEvent() == UserPreferencesEvent.UML_DESCRIPTION
         || event.getTypeOfEvent() == UserPreferencesEvent.ORDER_CONCEPTS) 
     {
       _this.remove(scrollPane);
       initViewPanel();
     }
   }
   
   private void fireElementChangeEvent(ElementChangeEvent event) {
     for(ElementChangeListener l : changeListeners)
       l.elementChanged(event);
   }
   
   public void addElementChangeListener(ElementChangeListener listener) {
     changeListeners.add(listener);
   }
   
   private void insertInBag(JPanel bagComp, Component comp, int x, int y) {
 
     insertInBag(bagComp, comp, x, y, 1, 1);
 
   }
 
   private void insertInBag(JPanel bagComp, Component comp, int x, int y, int width, int height) {
     JPanel p = new JPanel();
     p.add(comp);
 
     bagComp.add(p, new GridBagConstraints(x, y, width, height, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
   }
   
 }
   
   class ConceptUI {
   // initialize once the mode in which we're running
   private static boolean editable = false;
   static {
     UserSelections selections = UserSelections.getInstance();
     editable = selections.getProperty("MODE").equals(RunMode.Curator);
   }
 
   JLabel[] labels = new JLabel[] {
     new JLabel("Concept Code"),
     new JLabel("Concept Preferred Name"),
     new JLabel("Concept Definition"),
     new JLabel("Concept Definition Source")
   };
 
   JTextField code = new JTextField(10);
   JTextField name = new JTextField(20);
   JTextArea def = new JTextArea();
   JTextField defSource = new JTextField(10);
 
   JScrollPane defScrollPane;
   JButton evsButton, deleteButton, upButton, downButton;
   
   public ConceptUI(Concept concept) {
     initUI(concept);
   }
 
   private void initUI(Concept concept) {
     TextFieldLimiter tf = new TextFieldLimiter(2000);
     def.setDocument(tf);
 
     def.setFont(new Font("Serif", Font.ITALIC, 16));
     def.setLineWrap(true);
     def.setWrapStyleWord(true);
     defScrollPane = new JScrollPane(def);
     defScrollPane
       .setVerticalScrollBarPolicy
       (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
     defScrollPane.setPreferredSize(new Dimension(400, 100));
 
     code.setText(concept.getPreferredName());
     name.setText(concept.getLongName());
     def.setText(concept.getPreferredDefinition());
     defSource.setText(concept.getDefinitionSource());
 
     this.evsButton = new JButton("Evs Link");
     this.deleteButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("delete-x.gif")));
     this.upButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("up-arrow.gif")));
     this.downButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("down-arrow.gif")));
     
     upButton.setBorder(null);
     upButton.setContentAreaFilled(false);
     upButton.setOpaque(true);
 
     downButton.setBorder(null);
     downButton.setContentAreaFilled(false);
     downButton.setOpaque(true);
 
     deleteButton.setBorder(null);
     deleteButton.setContentAreaFilled(false);
     deleteButton.setOpaque(true);
 
     upButton.setToolTipText("Move this Concept up");
     downButton.setToolTipText("Move this Concept down");
     deleteButton.setToolTipText("Delete this Concept");
     
     upButton.setPreferredSize(new Dimension(31, 32));
     deleteButton.setPreferredSize(new Dimension(28,29));
     downButton.setPreferredSize(new Dimension(31, 32));
     
   }
   
   public JButton getEvsButton() {
     return evsButton;
   }
   
   public JButton getDeleteButton() {
     return deleteButton;
   }
 
   public JButton getDownButton() {
     return downButton;
   }
 
   public JButton getUpButton() {
     return upButton;
   } 
 
     public void setEnabled(boolean enabled) {
       code.setEnabled(enabled);
       name.setEnabled(enabled);
       def.setEnabled(enabled);
       defSource.setEnabled(enabled);
       evsButton.setEnabled(enabled);
       deleteButton.setEnabled(enabled);
       downButton.setEnabled(enabled);
       upButton.setEnabled(enabled);
     }
  
     class TextFieldLimiter extends PlainDocument
     {
       int maxChar = -1;
       public TextFieldLimiter(int len){maxChar = len;}
       public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
       {
         if (str != null && maxChar > 0 && this.getLength() + str.length() > maxChar)
           {
             java.awt.Toolkit.getDefaultToolkit().beep();
             return;
           }
         super.insertString(offs, str, a);
       }
     }
  
 }
