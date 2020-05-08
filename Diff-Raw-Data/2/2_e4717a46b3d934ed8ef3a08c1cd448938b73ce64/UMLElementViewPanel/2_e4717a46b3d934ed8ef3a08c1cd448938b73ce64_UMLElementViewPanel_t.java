 package gov.nih.nci.ncicb.cadsr.loader.ui;
 
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.loader.event.ReviewEvent;
 import gov.nih.nci.ncicb.cadsr.loader.event.ReviewListener;
 
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.NavigationEvent;
 import gov.nih.nci.ncicb.cadsr.loader.ui.event.NavigationListener;
 import gov.nih.nci.ncicb.cadsr.loader.ui.tree.*;
 
 import gov.nih.nci.ncicb.cadsr.loader.util.*;
 import gov.nih.nci.ncicb.cadsr.loader.*;
 
 import gov.nih.nci.ncicb.cadsr.loader.ui.util.UIUtil;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeEvent;
 
 import java.awt.event.ItemEvent;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.event.*;
 
 public class UMLElementViewPanel extends JPanel
   implements ActionListener, KeyListener
              , ItemListener,
              UserPreferencesListener, NavigationListener {
 
   private Concept[] concepts;
   private ConceptUI[] conceptUIs;
 
   private UMLElementViewPanel _this = this;
 
   private UMLNode node;
   private boolean remove = false,
     orderChanged = false;
 
   private static final String ADD = "ADD",
     DELETE = "DELETE",
     SAVE = "APPLY", 
     PREVIOUS = "PREVIOUS",
     NEXT = "NEXT";
 
   private JButton addButton, deleteButton, saveButton;
   private JButton previousButton, nextButton;
   private JCheckBox reviewButton;
 
   private List<ReviewListener> reviewListeners = new ArrayList();
   private List<NavigationListener> navigationListeners = new ArrayList();
   private List<PropertyChangeListener> propChangeListeners = new ArrayList();
   
   private JPanel gridPanel;
   private JScrollPane scrollPane;
 
   // initialize once the mode in which we're running
   private static boolean editable = false;
   static {
     UserSelections selections = UserSelections.getInstance();
     editable = selections.getProperty("MODE").equals(RunMode.Curator);
   }
   
   private UserPreferences prefs = UserPreferences.getInstance();
 
   private static EvsDialog evsDialog;
   
   public UMLElementViewPanel(UMLNode node) 
   {
     this.node = node;
     initConcepts();
     initUI();
   }
 
   public void navigate(NavigationEvent evt) {
     if(saveButton.isEnabled()) {
       if(JOptionPane.showConfirmDialog(_this, "There are unsaved changes in this concept, would you like to apply the changes now?", "Unsaved Changes", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
         apply(false);
     }
   }
 
   public void addReviewListener(ReviewListener listener) {
     reviewListeners.add(listener);
   }
 
   public void addNavigationListener(NavigationListener listener) 
   {
     navigationListeners.add(listener);
   }
 
   public void addPropertyChangeListener(PropertyChangeListener l) {
     propChangeListeners.add(l);
   }
 
   public void updateNode(UMLNode node) 
   {
     this.node = node;
     initConcepts();
     updateConcepts(concepts);
   }
 
   public void apply(boolean toAll) {
     boolean update = remove;
     remove = false;
     Concept[] newConcepts = new Concept[concepts.length];
     
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
       if(toAll) {
         Object o = node.getUserObject();
         if(o instanceof DataElement) {
           DataElement de = (DataElement)o;
           ObjectUpdater.updateByAltName(de.getDataElementConcept().getProperty().getLongName(), concepts, newConcepts);
         }
       } else
         ObjectUpdater.update((AdminComponent)node.getUserObject(), concepts, newConcepts);
      
      concepts = newConcepts;
     } 
 
     orderChanged = false;
     
     setSaveButtonState(false);
     addButton.setEnabled(true);
     reviewButton.setEnabled(true);
   }
 
   private void initConcepts() 
   {
     concepts = NodeUtil.getConceptsFromNode(node);
   }
 
   private void updateConcepts(Concept[] concepts) {
     this.concepts = concepts;
     this.removeAll();
     initUI();
     this.updateUI();
   }
 
   private void initUI() {
     prefs.addUserPreferencesListener(this);
     this.setLayout(new BorderLayout());
     initViewPanel();
     initButtonPanel();
   }
   
   private void initViewPanel() {
 
     gridPanel = new JPanel(new GridBagLayout());
 
     scrollPane = new JScrollPane(gridPanel);
 
     conceptUIs = new ConceptUI[concepts.length];
     JPanel[] conceptPanels = new JPanel[concepts.length];
 
     JPanel summaryPanel = new JPanel();
     JLabel summaryTitle = new JLabel("UML Concept Code Summary: ");
     summaryPanel.add(summaryTitle);
     for(int i = 0; i < concepts.length; i++) {
       conceptUIs[i] = new ConceptUI(concepts[i]);
       JLabel label= new JLabel(concepts[i].getPreferredName());
       summaryPanel.add(label);
     }
     this.add(summaryPanel,BorderLayout.NORTH);
     
     if(prefs.getUmlDescriptionOrder().equals("first"))
       insertInBag(gridPanel, createDescriptionPanel(), 0, 0);
       
     for(int i = 0; i<concepts.length; i++) {
       conceptUIs[i] = new ConceptUI(concepts[i]);
 
       String title = i == 0?"Primary Concept":"Qualifier Concept" +" #" + i;
 
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
 
       JButton evsButton = new JButton("Evs Link");
       insertInBag(mainPanel, evsButton, 2, 3);
       
       JButton upButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("up-arrow.gif")));
       JButton downButton = new JButton(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("down-arrow.gif")));
 
       upButton.setPreferredSize(new Dimension(28, 35));
       downButton.setPreferredSize(new Dimension(28, 35));
 
       JPanel arrowPanel = new JPanel(new GridBagLayout());
       insertInBag(arrowPanel, upButton, 0, 0);
       insertInBag(arrowPanel, downButton, 0, 6);
       
       conceptPanels[i].add(mainPanel, BorderLayout.CENTER);
       conceptPanels[i].add(arrowPanel, BorderLayout.EAST);
 
       insertInBag(gridPanel, conceptPanels[i], 0, i+1);
 
       conceptUIs[i].code.addKeyListener(this);
       conceptUIs[i].name.addKeyListener(this);
       conceptUIs[i].def.addKeyListener(this);
       conceptUIs[i].defSource.addKeyListener(this);
 
       final int index = i;
       if(index == 0)
         upButton.setVisible(false);
       if(index == concepts.length-1)
         downButton.setVisible(false);
       
       upButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event) {
           Concept temp = concepts[index-1];
           concepts[index-1] = concepts[index];
           concepts[index] = temp;
           updateConcepts(concepts);
 
           orderChanged = true;
 
           setSaveButtonState(areAllFieldEntered());
           reviewButton.setEnabled(false);
         }
         });
       
       downButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event) {
           Concept temp = concepts[index];
           concepts[index] = concepts[index+1];
           concepts[index+1] = temp;
           updateConcepts(concepts);
 
           orderChanged = true;
           
           setSaveButtonState(areAllFieldEntered());
           reviewButton.setEnabled(false);
         }
         });
 
 
       evsButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent event) {
             if (evsDialog == null)
               evsDialog = new EvsDialog();
 
             UIUtil.putToCenter(evsDialog);
             evsDialog.setVisible(true);
 
             Concept c = evsDialog.getConcept();
 
             conceptUIs[index].code.setText(c.getPreferredName());
             conceptUIs[index].name.setText(c.getLongName());
             conceptUIs[index].def.setText(c.getPreferredDefinition());
             conceptUIs[index].defSource.setText(c.getDefinitionSource());
 
             if(areAllFieldEntered()) {
               setSaveButtonState(true);
 //               addButton.setEnabled(true);
             } else {
               setSaveButtonState(false);
 //               addButton.setEnabled(false);
             }
             reviewButton.setEnabled(false);
           }
         });
       
     }
     
     if(prefs.getUmlDescriptionOrder().equals("last"))
       insertInBag(gridPanel, createDescriptionPanel(), 0, concepts.length + 1); 
 
     this.add(scrollPane, BorderLayout.CENTER);
     
   }
 
   private JPanel createDescriptionPanel() {
     JPanel umlPanel = new JPanel();
     umlPanel.setBorder
       (BorderFactory.createTitledBorder("UML Description"));   
     umlPanel.setLayout(new BorderLayout());
 
     JTextArea descriptionArea = new JTextArea(5, 54);
     JScrollPane descScrollPane = new JScrollPane(descriptionArea);
 
     if(node instanceof ClassNode) {
       ObjectClass oc = (ObjectClass) node.getUserObject();
       descriptionArea.setText(oc.getPreferredDefinition());
     } else if(node instanceof AttributeNode) {
       DataElement de = (DataElement) node.getUserObject();
 
       for(Definition def : (List<Definition>) de.getDefinitions()) {
         descriptionArea.setText(def.getDefinition());
         break;
       }
 
     }
 
     descriptionArea.setLineWrap(true);
     descriptionArea.setEditable(false);
     
     if(StringUtil.isEmpty(descriptionArea.getText())) 
     {
       umlPanel.setVisible(false);
     }
 
     umlPanel.add(descScrollPane, BorderLayout.CENTER);
     
     return umlPanel;
  
   }
 
   private void initButtonPanel() {
     addButton = new JButton("Add");
     deleteButton = new JButton("Remove");
     saveButton = new JButton("Apply");
     reviewButton = new JCheckBox("Reviewed");
     previousButton = new JButton("Previous");
     nextButton = new JButton("Next");
     
     reviewButton.setSelected(((ReviewableUMLNode)node).isReviewed());
     addButton.setActionCommand(ADD);
     deleteButton.setActionCommand(DELETE);
     saveButton.setActionCommand(SAVE);
     previousButton.setActionCommand(PREVIOUS);
     nextButton.setActionCommand(NEXT);
     addButton.addActionListener(this);
     deleteButton.addActionListener(this);
     saveButton.addActionListener(this);
     reviewButton.addItemListener(this);
     previousButton.addActionListener(this);
     nextButton.addActionListener(this);
     
     if(concepts.length < 2)
       deleteButton.setEnabled(false);
     
     if(areAllFieldEntered()) {
       reviewButton.setEnabled(true);
       addButton.setEnabled(true);
     } else {
       addButton.setEnabled(false);
       reviewButton.setEnabled(false);
     }
     saveButton.setEnabled(false);
 
     JPanel buttonPanel = new JPanel();
     buttonPanel.add(addButton);
     buttonPanel.add(deleteButton);
     buttonPanel.add(saveButton);
     buttonPanel.add(reviewButton);
 //     buttonPanel.add(previousButton);
 //     buttonPanel.add(nextButton);
     
     this.add(buttonPanel, BorderLayout.SOUTH);
   }
 
   private void setSaveButtonState(boolean b) {
     saveButton.setEnabled(b);
 
     PropertyChangeEvent evt = new PropertyChangeEvent(this, SAVE, null, b);
     firePropertyChangeEvent(evt);
   }
 
   private boolean areAllFieldEntered() {
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
 
   public void keyTyped(KeyEvent evt) {}
   public void keyPressed(KeyEvent evt) {}
 
   /**
    *  Text Change Use Case.
    */
   public void keyReleased(KeyEvent evt) {
     if(areAllFieldEntered()) {
 //       addButton.setEnabled(true);
       setSaveButtonState(true);
     } else {
 //       addButton.setEnabled(false);
       setSaveButtonState(false);
     }
     reviewButton.setEnabled(false);
   }
 
 
   public void preferenceChange(UserPreferencesEvent event) 
   {
     if(event.getTypeOfEvent() == UserPreferencesEvent.UML_DESCRIPTION) 
     {
       _this.remove(scrollPane);
       initViewPanel();
     }
   }
 
   
   public void actionPerformed(ActionEvent evt) {
     JButton button = (JButton)evt.getSource();
     if(button.getActionCommand().equals(SAVE)) {
       apply(false);
     } else if(button.getActionCommand().equals(ADD)) {
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
 
       if(concepts.length > 1)
         deleteButton.setEnabled(true);
 
       if(areAllFieldEntered()) {
         saveButton.setEnabled(true);
       } else {
         saveButton.setEnabled(false);
       }
       addButton.setEnabled(false);
       reviewButton.setEnabled(false);
     } else if(button.getActionCommand().equals(DELETE)) {
       Concept[] newConcepts = new Concept[concepts.length - 1];
       for(int i = 0; i<newConcepts.length; i++) {
         newConcepts[i] = concepts[i];
       }
       concepts = newConcepts;
 
       _this.remove(scrollPane);
       initViewPanel();
 
       if(areAllFieldEntered()) {
         saveButton.setEnabled(true);
       } else {
         saveButton.setEnabled(false);
       }
       addButton.setEnabled(false);
       reviewButton.setEnabled(false);
       
       if(concepts.length < 2)
         deleteButton.setEnabled(false);
       this.updateUI();
 
       remove = true;
 
     } else if(button.getActionCommand().equals(PREVIOUS)) {
       NavigationEvent event = new NavigationEvent(NavigationEvent.NAVIGATE_PREVIOUS);
       fireNavigationEvent(event);
       remove = false;
     } else if(button.getActionCommand().equals(NEXT)) {
       NavigationEvent event = new NavigationEvent(NavigationEvent.NAVIGATE_NEXT);
       fireNavigationEvent(event);
       remove = false;
     }
 
   }
   
   private void firePropertyChangeEvent(PropertyChangeEvent evt) {
     for(PropertyChangeListener l : propChangeListeners) 
       l.propertyChange(evt);
   }
   
   private void fireNavigationEvent(NavigationEvent event) 
   {
     for(NavigationListener l : navigationListeners)
       l.navigate(event);
   }
   
   
   public void itemStateChanged(ItemEvent e) {
     if(e.getStateChange() == ItemEvent.SELECTED
        || e.getStateChange() == ItemEvent.DESELECTED
        ) {
       ReviewEvent event = new ReviewEvent();
       event.setUserObject(node);
       
       event.setReviewed(ItemEvent.SELECTED == e.getStateChange());
 
       fireReviewEvent(event);
       
       //if item is reviewed go to next item in the tree
       if(e.getStateChange() == ItemEvent.SELECTED) 
       {
         NavigationEvent goToNext = new NavigationEvent(NavigationEvent.NAVIGATE_NEXT);
         fireNavigationEvent(goToNext);
       }
         
     }
   }
   
   private void fireReviewEvent(ReviewEvent event) {
     for(ReviewListener l : reviewListeners)
       l.reviewChanged(event);
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
 
   public ConceptUI(Concept concept) {
     initUI(concept);
   }
 
   private void initUI(Concept concept) {
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
 
     if(!editable) {
       code.setEnabled(false);
       name.setEnabled(false);
       def.setEnabled(false);
       defSource.setEnabled(false);
     }
     
   }
 
   
 
 }
