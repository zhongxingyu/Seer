 package fi.helsinki.biblex.ui;
 
 import fi.helsinki.biblex.domain.BibTexStyle;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.*;
 import java.util.List;
 
 /**
  * This class handles the visual appearance of the reference editor window All
  * handling of user input and actions is done elsewhere.
  */
 public class Window implements Iterable<Map.Entry<String, String>> {
 
     public static enum UIAction {
 
         SUBMIT,
         ADD_FIELD,
         DELETE_FIELD,
         SET_ENTRY,
         APPLY_FILTER,
         MENU_QUIT,
         MENU_EXPORT,
         MENU_IMPORT,
         MENU_NEW_ENTRY
     }
 
     public static class EntryIterator implements Iterator<Map.Entry<String, String>> {
 
         private int p_pos;
         private Window p_win;
 
         public EntryIterator(Window win) {
             p_pos = 0;
             p_win = win;
         }
 
         public boolean hasNext() {
             return (p_pos < p_win.p_fieldMap.size());
         }
 
         public Map.Entry<String, String> next() {
             if (!hasNext()) {
                 throw new IndexOutOfBoundsException("Iterator out of bounds");
             }
 
             Map.Entry<String, JPanel> fieldEntry = p_win.p_fieldMap.get(p_pos++);
 
             return new AbstractMap.SimpleEntry<String, String>(
                     fieldEntry.getKey(),
                     ((JTextField) fieldEntry.getValue().getComponent(FIELD_PANE_TEXT_ID)).getText());
         }
 
         public void remove() {
         }
     }
     // Ugly way to access the correct component in the field JPanel...
     private static final int FIELD_PANE_TEXT_ID = 2;
     private static final int FIELD_PANE_BUTTON_ID = 4;
    
     private JFrame p_window;
    
     private JPanel p_pane;
     private JScrollPane p_scrollPane;
    
     private JComboBox p_entryStyleInput;
     private JTextField p_entryNameInput;
     private JTextField p_fieldNameInput;
    
     private JTextField p_filterInput;
     private JButton p_filterButton;
    
     private JMenuBar p_menu;
    
     private JButton p_submitButton;
     private JButton p_addFieldButton;
     private JButton p_setEntryButton;
    
     private JMenuItem p_menuNewEntry;
     private JMenuItem p_menuExport;
     private JMenuItem p_menuImport;
     private JMenuItem p_menuQuit;
    
     // Need to keep track of this, as each field has it's own button
     private Action p_deleteAction;
    
     // Keep track of field name -> UI element pairs
     private List<Map.Entry<String, JPanel>> p_fieldMap;
 
    
     public Window(JPanel entryPane) {
         p_window = new JFrame();
 
         p_fieldMap = new ArrayList<Map.Entry<String, JPanel>>();
 
         p_window.setSize(680, 470);
         p_window.setMinimumSize(new Dimension(650, 450));
         p_window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 
         populate(entryPane);
         p_window.setVisible(true);
     }
 
     /**
      * Register action handler for a specific UI action
      *
      * @param uiAction Action ID, for which the action handler is set
      * @param action Action, to be set as the action handler
      */
     public void registerAction(UIAction uiAction, Action action) {
         switch (uiAction) {
             case SUBMIT:
                 p_submitButton.setAction(action);
                 return;
 
             case ADD_FIELD:
                 p_addFieldButton.setAction(action);
                 return;
 
             case DELETE_FIELD:
                 p_deleteAction = action;
                 for (Map.Entry<String, JPanel> entry : p_fieldMap) {
                     ((JButton) entry.getValue().getComponent(FIELD_PANE_BUTTON_ID)).setAction(action);
                 }
                 return;
 
             case SET_ENTRY:
                 p_setEntryButton.setAction(action);
                 return;
 
             case APPLY_FILTER:
                 p_filterButton.setAction(action);
                 break;
 
             case MENU_QUIT:
                 p_menuQuit.setAction(action);
                 return;
 
             case MENU_EXPORT:
                 p_menuExport.setName("p_menuExport");
                 p_menuExport.setAction(action);
                 return;
 
             case MENU_IMPORT:
                 p_menuImport.setName("p_menuImport");
                 p_menuImport.setAction(action);
                 return;
 
             case MENU_NEW_ENTRY:
                 p_menuNewEntry.setName("p_menuNewEntry");
                 p_menuNewEntry.setAction(action);
                 return;
 
             default:
                 // action not supported
                 assert (false);
         }
     }
 
     /**
      * Set window title
      *
      * @param name Name of the current entry
      */
     public void setEntry(String style, String name) {
         p_pane.removeAll();
         p_fieldMap.clear();
 
         if (name.isEmpty()) {
             p_window.setTitle(GUI.APP_NAME);
             p_scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
         } else {
             p_window.setTitle(GUI.APP_NAME + " - '" + name + "'");
             p_scrollPane.setBorder(BorderFactory.createTitledBorder(style + " - " + name));
         }
     }
 
     /**
      * Add a new style to the style selection combo-box
      *
      * @param style Style to be added
      */
     public void addEntryStyle(BibTexStyle style) {
         p_entryStyleInput.addItem(style);
     }
 
     /**
      * Add a new field to the current entry
      *
      * @param name Name/ID of the field to add
      */
     public void addField(String name, String content) {
         name = name.trim().toLowerCase();
 
         JPanel pane = new JPanel();
         pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
 
         JLabel nameLabel = new JLabel(name);
         JTextField nameField = new JTextField(content);
 
         nameField.setName(name);
         nameLabel.setMinimumSize(new Dimension(150, 20));
         nameLabel.setPreferredSize(new Dimension(150, 20));
         nameLabel.setMaximumSize(new Dimension(225, 20));
         nameField.setMinimumSize(new Dimension(225, 20));
         nameField.setPreferredSize(new Dimension(225, 20));
         nameField.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
 
         pane.add(nameLabel);
         pane.add(Box.createRigidArea(new Dimension(10, 0)));
         pane.add(nameField);
         pane.add(Box.createRigidArea(new Dimension(3, 0)));
 
         JButton deleteButton = new JButton();
         deleteButton.setAction(p_deleteAction); // null check not needed
         deleteButton.setName("btnDeleteField:" + name); // need this for testing.
         deleteButton.setActionCommand(name);
         pane.add(deleteButton);
 
         p_fieldMap.add(new AbstractMap.SimpleEntry<String, JPanel>(name, pane));
 
         p_pane.add(pane);
         p_pane.validate();
         p_scrollPane.validate();
     }
 
     /**
      * Remove a field from the current entry
      *
      * @param name Name/ID of the field to be removed
      */
     public void deleteField(String name) {
         for (int i = 0; i < p_fieldMap.size(); i++) {
             if (p_fieldMap.get(i).getKey().equals(name)) {
                 p_pane.remove(p_fieldMap.get(i).getValue());
                 p_fieldMap.remove(i);
 
                 p_pane.revalidate();
                 p_pane.repaint();
 
                 return;
             }
         }
 
         // Tänne ei pitäisi ikinä päästä...
         throw new RuntimeException("Delete field failed, field not found (" + name + ")");
     }
 
     public String getFieldNameEntry() {
         return p_fieldNameInput.getText();
     }
 
     public void clearFieldNameEntry() {
         p_fieldNameInput.setText("");
     }
 
     public String getEntryNameInput() {
         return p_entryNameInput.getText();
     }
 
     public void clearEntryNameInput() {
         p_entryNameInput.setText("");
     }
 
     public BibTexStyle getEntryStyleInput() {
         return (BibTexStyle) p_entryStyleInput.getSelectedItem();
     }
 
     public String getFilterEntry() {
         return p_filterInput.getText();
     }
 
     public void clearFilterEntry() {
         p_filterInput.setText("");
     }
 
     /**
      * Display an error dialog with a given message
      *
      * @param err Error message to display
      * @param title Title of the error dialog window
      */
     public void displayError(String err, String title) {
         JOptionPane.showMessageDialog(p_window, err, title, JOptionPane.ERROR_MESSAGE);
     }
 
     /**
      * Allow iteration through the entry fields
      *
      * @return Iterator for the field-value pairs in the window
      */
     public EntryIterator iterator() {
         return new EntryIterator(this);
     }
 
     /**
      * Dispose of the window
      */
     public void dispose() {
         p_window.dispose();
     }
 
     public JFrame getWindow() {
         return p_window;
     }
 
     /**
      * Add UI elements to the window, and setup action handling
      */
     private void populate(JPanel entryPane) {
         // Menu:
         p_menu = new JMenuBar();
         p_window.setJMenuBar(p_menu);
 
         JMenu fileMenu = new JMenu("File");
         p_menuNewEntry = new JMenuItem();
         p_menuExport = new JMenuItem();
         p_menuImport = new JMenuItem();
         p_menuQuit = new JMenuItem();
 
         fileMenu.add(p_menuNewEntry);
         fileMenu.add(new JSeparator());
         fileMenu.add(p_menuExport);
         fileMenu.add(p_menuImport);
         fileMenu.add(new JSeparator());
         fileMenu.add(p_menuQuit);
 
         p_menu.add(fileMenu);
 
         // Top right (create entry):
         JPanel topPane = new JPanel();
         p_entryStyleInput = new JComboBox();
         p_entryNameInput = new JTextField();
         p_entryNameInput.setName("p_entryNameInput");
         p_setEntryButton = new JButton();
         p_setEntryButton.setName("p_setEntryButton");
 
         topPane.setLayout(new BoxLayout(topPane, BoxLayout.X_AXIS));
         topPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
         topPane.add(new JLabel("Reference type:"));
         topPane.add(Box.createRigidArea(new Dimension(5, 0)));
         topPane.add(p_entryStyleInput);
         topPane.add(Box.createRigidArea(new Dimension(10, 0)));
         topPane.add(new JLabel("Reference name:"));
         topPane.add(Box.createRigidArea(new Dimension(5, 0)));
         topPane.add(p_entryNameInput);
         topPane.add(Box.createRigidArea(new Dimension(5, 0)));
         topPane.add(p_setEntryButton);
 
         // Bottom right (add field):
         JPanel bottomSubPane = new JPanel();
         p_fieldNameInput = new JTextField();
         p_fieldNameInput.setName("p_fieldNameInput");
         p_addFieldButton = new JButton();
         p_addFieldButton.setName("p_addFieldButton");
 
         bottomSubPane.setLayout(new BoxLayout(bottomSubPane, BoxLayout.X_AXIS));
         bottomSubPane.add(Box.createHorizontalGlue());
         bottomSubPane.add(p_fieldNameInput);
         bottomSubPane.add(Box.createRigidArea(new Dimension(3, 0)));
         bottomSubPane.add(p_addFieldButton);
 
         // Bottom right (submit):
         JPanel bottomPane = new JPanel();
         p_submitButton = new JButton();
         p_submitButton.setName("p_submitButton");
 
         GridLayout bottomLayout = new GridLayout(0, 1);
         bottomLayout.setVgap(3);
         bottomPane.setLayout(bottomLayout);
         bottomPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
         bottomPane.add(bottomSubPane);
         bottomPane.add(p_submitButton);
 
         // Main pane (entry fields):
         p_pane = new JPanel();
         p_pane.setName("p_pane");
         p_pane.setLayout(new BoxLayout(p_pane, BoxLayout.Y_AXIS));
 
         p_scrollPane = new JScrollPane(p_pane);
         p_scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         p_scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
 
         // Top left (filtering):
         p_filterInput = new JTextField();
         p_filterInput.setName("p_filterInput");
         p_filterButton = new JButton();
         p_filterButton.setName("p_filterButton");
 
         JPanel filterPane = new JPanel();
         filterPane.setLayout(new BoxLayout(filterPane, BoxLayout.X_AXIS));
         filterPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
 
         filterPane.add(p_filterInput);
         filterPane.add(Box.createRigidArea(new Dimension(3, 0)));
         filterPane.add(p_filterButton);
 
         // Top layer pane:
         JPanel mainPane = new JPanel();
         mainPane.setLayout(new BorderLayout());
 
         entryPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
 
         mainPane.add(topPane, BorderLayout.NORTH);
         mainPane.add(p_scrollPane, BorderLayout.CENTER);
         mainPane.add(bottomPane, BorderLayout.SOUTH);
 
         entryPane.add(filterPane, BorderLayout.NORTH);
 
         p_window.getContentPane().add(entryPane, BorderLayout.WEST);
         p_window.getContentPane().add(mainPane, BorderLayout.CENTER);
     }
 }
