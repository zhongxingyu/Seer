 package vooga.towerdefense.gameeditor.gamemaker;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import vooga.towerdefense.factories.ActionAnnotation;
 
 
 /**
 * ElementWithActionEditorScreen is the super class for helping
 * the game developer make elements that have actions in them.
  * 
  * @author Angelica Schwartz
  */
 public abstract class ElementWithActionEditorScreen extends GameEditorScreen {
 
     /**
      * constant for text area height.
      */
     public static final int TEXT_AREA_HEIGHT = 25;
     /**
      * constant for text area width.
      */
     public static final int TEXT_AREA_WIDTH = 10;
     /**
      * default serialized id.
      */
     private static final long serialVersionUID = 1L;
     /**
      * constant for the attribute selector button.
      */
     private static final String ATTRIBUTES_ADD_BUTTON_TEXT = "Add attribute";
     /**
      * constant for the action selector button.
      */
     private static final String ACTION_ADD_BUTTON_TEXT = "Add action";
     /**
      * constant for follow up action button. 
      */
     private static final String FOLLOW_UP_ACTION_TEXT = "Add follow up action";
     /**
      * constant for the action delete button.
      */
     private static final String ACTION_DELETE_BUTTON_TEXT = "Clear selected action";
     /**
      * constant for the attributes delete button.
      */
     private static final String ATTRIBUTE_DELETE_BUTTON_TEXT = "Clear selected attribute";
     /**
      * option for new attribute.
      */
     private static final String NEW_TEXT = "NEW";
     /**
      * constant for the action section title.
      */
     private static final String ACTION_TITLE = "Actions";
     /**
      * constant for the attribute section title.
      */
     private static final String ATTRIBUTE_TITLE = "Attributes";
     /**
      * action path for this screen.
      */
     private String myActionPath;
     /**
      * box to enter the name of the game element.
      */
     private JTextField myNameBox;
     /**
      * drop down menu with the attributes available.
      */
     private JComboBox myAttributesBox;
     /**
      * button to add attributes to this game element.
      */
     private JButton myAddAttributeButton;
     /**
      * button to delete attributes from this game element.
      */
     private JButton myDeleteAttributeButton;
     /**
      * area where attributes the user has selected are displayed.
      */
     private JTextArea myAttributesSelected;
     /**
      * area where the game developer can enter the
      * attributes value.
      */
     private JTextField myAttributeValue;
     /**
      * drop down menu with the actions available.
      */
     private JComboBox myActionsBox;
     /**
      * button to add actions to this game element.
      */
     private JButton myAddActionButton;
     /**
      * button to add follow up actions to the selected action.
      */
     private JButton myAddFollowUpActionButton;
     /**
      * button to delete actions from this game element.
      */
     private JButton myDeleteActionButton;
     /**
      * area where actions the user has selected are displayed.
      */
     private JTextArea myActionsSelected;
     /**
      * holds the attributes section.
      */
     private JPanel myAttributesSection;
     /**
      * holds the actions section.
      */
     private JPanel myActionsSection;
 
     /**
      * Constructor.
      * 
      * @param size
      * @param controller
      */
     public ElementWithActionEditorScreen (Dimension size,
                                     GameEditorController controller,
                                     String title, String nextScreen) {
         super(size, controller, title, nextScreen);
     }
 
     /**
      * helper method to create all the parts of
      * the game elementEditorScreen.
      */
     public void makeScreen () {
         addCharacteristicsPanel();
         JPanel bottomScreen = new JPanel(new BorderLayout());
         bottomScreen.add(myActionsSection, BorderLayout.SOUTH);
         add(bottomScreen, BorderLayout.SOUTH);
     }
 
     /**
      * gets the name of this game element.
      * @return a string of the name
      */
     public String getName() {
         return myNameBox.getText();
     }
     
     /**
      * gets the attribute map of name to value.
      * @return a map of string to string
      */
     public Map<String, String> getAttributes() {
         Map<String, String> attributeMap = new HashMap<String, String>();
         String fullAttributes = myAttributesSelected.getText();
         while (!fullAttributes.equals("")) {
             int index = fullAttributes.indexOf("\n");
             String attribute = fullAttributes.substring(0, index);
             String[] nameAndValue = attribute.split(" ");
             if (nameAndValue.length != 1) {
                 attributeMap.put(nameAndValue[0], nameAndValue[1]);
             }
             else {
                 attributeMap.put(nameAndValue[0], "");
             }
             fullAttributes = fullAttributes.substring(index+1);
         }
         return attributeMap;
     }
     
     /**
      * gets the actions as a string.
      * @return a string
      */
     public String getActions() {
         return myActionsSelected.getText();
     }
     
     public JTextArea getAttributesSection() {
         return myAttributesSelected;
     }
     
     /**
      * clears all fields in the game elementEditorScreen.
      */
     @Override
     public void clearScreen () {
         myNameBox.setText("");
         myActionsSelected.setText("");
     }
 
     /**
      * helper method to make the text boxes for
      * name and image + image button.
      */
     private void addCharacteristicsPanel () {
         JPanel characteristicsPanel = new JPanel();
         myNameBox = new JTextField(TEXT_AREA_WIDTH);
         characteristicsPanel.add(new JLabel("Name: "));
         characteristicsPanel.add(myNameBox);
         add(characteristicsPanel, BorderLayout.NORTH);
     }
 
     /**
      * helper method to make the actions section of this screen.
      * 
      * @throws ClassNotFoundException
      * @throws IOException
      */
     public void makeActionsSection (String classPath) throws ClassNotFoundException, IOException {
         JPanel actionSection = new JPanel(new BorderLayout());
         JPanel westSide = new JPanel(new BorderLayout());
         westSide.add(new JLabel(ACTION_TITLE), BorderLayout.NORTH);
         myActionsBox = new JComboBox();
         List<String> actions = getController().getAvailableActions(classPath);
         for (String a : actions) {
             myActionsBox.addItem(a);
         }
         westSide.add(myActionsBox, BorderLayout.CENTER);
         actionSection.add(westSide, BorderLayout.WEST);
         myActionsSelected = new JTextArea(TEXT_AREA_WIDTH, TEXT_AREA_HEIGHT);
         actionSection.add(new JScrollPane(myActionsSelected), BorderLayout.CENTER);
         myAddActionButton = new JButton(ACTION_ADD_BUTTON_TEXT);
         myAddActionButton.addMouseListener(getMouseAdapter());
         JPanel optionsSubPanel = new JPanel(new BorderLayout());
         optionsSubPanel.add(myAddActionButton, BorderLayout.NORTH);
         myAddFollowUpActionButton = new JButton(FOLLOW_UP_ACTION_TEXT);
         myAddFollowUpActionButton.addMouseListener(getMouseAdapter());
         optionsSubPanel.add(myAddFollowUpActionButton, BorderLayout.CENTER);
         myDeleteActionButton = new JButton(ACTION_DELETE_BUTTON_TEXT);
         myDeleteActionButton.addMouseListener(getMouseAdapter());
         optionsSubPanel.add(myDeleteActionButton, BorderLayout.SOUTH);
         JPanel eastSide = new JPanel(new BorderLayout());
         eastSide.add(optionsSubPanel, BorderLayout.NORTH);
         actionSection.add(eastSide, BorderLayout.EAST);
         myActionsSection = actionSection;
     }
 
     /**
      * helper method to make the attributes drop down box,
      * button, and text field.
      * 
      * @throws ClassNotFoundException
      */
     public JPanel makeAttributesSection (List<String> attributes) throws ClassNotFoundException {
         JPanel attributesSection = new JPanel(new BorderLayout());
         JPanel optionsSubPanel1 = new JPanel(new BorderLayout());
         optionsSubPanel1.add(new JLabel(ATTRIBUTE_TITLE), BorderLayout.NORTH);
         myAttributesBox = new JComboBox();
         attributes.add(NEW_TEXT);
         for (String a : attributes) {
             myAttributesBox.addItem(a);
         }
         optionsSubPanel1.add(myAttributesBox, BorderLayout.CENTER);
         myAttributeValue = new JTextField();
         optionsSubPanel1.add(myAttributeValue, BorderLayout.SOUTH);
         attributesSection.add(optionsSubPanel1, BorderLayout.WEST);
         myAttributesSelected = new JTextArea(TEXT_AREA_WIDTH, TEXT_AREA_HEIGHT);
         attributesSection.add(new JScrollPane(myAttributesSelected), BorderLayout.CENTER);
         JPanel optionsSubPanel2 = new JPanel(new BorderLayout());
         myAddAttributeButton = new JButton(ATTRIBUTES_ADD_BUTTON_TEXT);
         myAddAttributeButton.addMouseListener(getMouseAdapter());
         optionsSubPanel2.add(myAddAttributeButton, BorderLayout.NORTH);
         myDeleteAttributeButton = new JButton(ATTRIBUTE_DELETE_BUTTON_TEXT);
         myDeleteAttributeButton.addMouseListener(getMouseAdapter());
         optionsSubPanel2.add(myDeleteAttributeButton, BorderLayout.SOUTH);
         JPanel eastSide = new JPanel(new BorderLayout());
         eastSide.add(optionsSubPanel2, BorderLayout.NORTH);
         attributesSection.add(eastSide, BorderLayout.EAST);
         myAttributesSection = attributesSection;
         return myAttributesSection;
     }
 
     /**
      * adds additional mouse behavior specific to this screen.
      * 
      * @return mouse adapter
      */
     @Override
     public void addAdditionalMouseBehavior (MouseEvent e) {
         if (e.getSource().equals(myAddAttributeButton)) {
             if (myAttributesBox.getSelectedItem().equals(NEW_TEXT)) {
                 String name = JOptionPane.showInputDialog("Enter the name of your new attribute");
                 String value = JOptionPane.showInputDialog("Enter the value for " + name);
                 myAttributesSelected.setText(myAttributesSelected.getText()
                    + name + " " + value + "\n");
             }
             else {
                 myAttributesSelected.setText(myAttributesSelected.getText()
                     + myAttributesBox.getSelectedItem().toString()
                     + " " + myAttributeValue.getText() + "\n");
             }
         }
         else if (e.getSource().equals(myAddActionButton)) {
             myActionsSelected.setText(myActionsSelected.getText() + addAction());
         }
         else if (e.getSource().equals(myAddFollowUpActionButton)) {
             String parentAction = myActionsSelected.getSelectedText();
             String childAction = addAction();
             for (char c : parentAction.toCharArray()) {
                 if (c == '\t') {
                     childAction = "\t" + childAction;
                 }
             }
             childAction = "\t" + childAction;
             myActionsSelected.setText(myActionsSelected.getText().substring(0, myActionsSelected.getText().indexOf(parentAction))
                                       + parentAction + "\n" + childAction
                                       + myActionsSelected.getText().substring(myActionsSelected.getText().indexOf(parentAction) 
                                       + parentAction.length()+1, myActionsSelected.getText().length()));
             
         }
         else if (e.getSource().equals(myDeleteAttributeButton)) {
             myAttributesSelected.replaceSelection("");
         }
         else if (e.getSource().equals(myDeleteActionButton)) {
             myActionsSelected.replaceSelection("");
         }
     }
     
     /**
      * sets the action path for this screen.
      * @param path is the path to the actions folder
      */
     public void setActionPath(String path) {
         myActionPath = path;
     }
     
     /**
      * returns the action as a string.
      * @return
      */
     private String addAction() {
         try {
             Class c = Class.forName(myActionPath + "." + myActionsBox.getSelectedItem().toString()+"Factory");
             Constructor[] constructors = c.getConstructors();
             Annotation[][] annotations = constructors[0].getParameterAnnotations();
             String display = myActionsBox.getSelectedItem().toString();
             for (Annotation[] annotation : annotations) {
                 for (Annotation a: annotation) {
                     display += " " + JOptionPane.showInputDialog("Enter the " + ((ActionAnnotation)a).name()
                                                              + " (" + ((ActionAnnotation)a).value() + ")"
                                                              + " for this action");
                 }
             }
             return display + "\n";
         }
         catch (ClassNotFoundException e1) {
             e1.printStackTrace();
         }
         return null;
     }
 
     /**
      * adds a game element to the game.
      */
     public abstract void addElementToGame();
     
     /**
      * helper method to make a map from the string from a text area.
      * @param s is a string from a text area.
      */
     public Map<String, String> makeMap(String s) {
         Map<String, String> map = new HashMap<String, String>();
         String[] subParts = s.split("\n");
         for (String subPart : subParts) {
             if (!subPart.equals("")) {
                 String[] nameAndValue = subPart.split(" ");
                 map.put(nameAndValue[0], nameAndValue[1]);
             }
         }
         return map;
     }
 }
