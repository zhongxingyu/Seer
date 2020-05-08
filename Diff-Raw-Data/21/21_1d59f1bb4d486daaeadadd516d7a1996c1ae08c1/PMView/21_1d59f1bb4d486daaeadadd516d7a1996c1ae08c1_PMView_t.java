 package dashteacup.pman;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 
 /**
  * View class for the Password Management (PM) application.
  */
 public class PMView {
 
     /**
      * The main window for this application.
      */
     private JFrame mainWindow;
 
     /**
      * Menu item for opening a new password file.
      */
     private JMenuItem newMenu;
 
     /**
      * Menu item for opening an existing password file.
      */
     private JMenuItem openMenu;
 
     /**
      * Menu item for closing a password file.
      */
     private JMenuItem closeMenu;
 
     /**
      * Menu item for saving a password file.
      */
     private JMenuItem saveMenu;
 
     /**
      * Menu item for saving a password file as a chosen file name.
      */
     private JMenuItem saveAsMenu;
 
     /**
      * Tool bar button for creating a new password file.
      */
     private JButton newButton;
 
     /**
      * Tool bar button for opening an existing password file.
      */
     private JButton openButton;
 
     /**
      * Tool bar button for saving the current password file.
      */
     private JButton saveButton;
 
     /**
      * Tool bar button for saving the current password file as something else.
      */
     private JButton saveAsButton;
 
     /**
      * Text area where the password file will be displayed.
      */
     private JTextArea textArea;
 
     /**
      * Constructor builds the view
      */
     public PMView() {
         try {
             // Use the native look and feel if possible
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             // Put the Mac Menubar in the right place.
             // TODO: Check what this does on non-macs
             System.setProperty("apple.laf.useScreenMenuBar", "true");
         } catch(Exception e) {
             // It's not a big deal if you can't access the system look and feel
             System.err.println("Couldn't get native look and feel.");
         }
         mainWindow = new JFrame("CS242 Password Manager");
         setUpMenu();
         JPanel panel = new JPanel(new BorderLayout());
         setUpToolbar(panel);
         setUpTextArea(panel);
         mainWindow.add(panel);
         mainWindow.pack();
         mainWindow.setVisible(true);
         mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     }
 
     /**
      * Get the main window for the password manager app.
      * @return the mainWindow
      */
     public JFrame getMainWindow() {
         return mainWindow;
     }
 
     /**
      * Displays a string in the main window's text area.
      * @param text to be added
      */
     public void addText(String text) {
         textArea.append(text);
     }
 
     /**
      * Set the main windows text area to display the given source text.
      * @param text to be shown.
      */
     public void setText(String text) {
         textArea.setText(text);
     }
 
     /**
      * Get the current contents of the text area.
      * @return text area contents.
      */
     public String getText() {
         return textArea.getText();
     }
 
     /**
      * Hide the main window's text area. This should be the state for the
      * text area when no file is loaded or being written.
      */
     public void hideTextArea() {
         textArea.setEditable(false);
         textArea.setBackground(Color.GRAY);
         textArea.setText("");
     }
 
     /**
      * Show the main window's text area. This should be the state for when
      * a file is open or being created.
      */
     public void showTextArea() {
         textArea.setEditable(true);
         textArea.setBackground(Color.WHITE);
         textArea.requestFocusInWindow();
     }
 
     /**
      * Show a message dialog box attached to the main window of the application.
      * @param message to be displayed to the user.
      */
     public void showMessageDialog(String message) {
         // TODO: figure out what I'm going to do with this dialog here.
         JOptionPane.showMessageDialog(mainWindow, message, "Error", JOptionPane.WARNING_MESSAGE);
     }
 
     /**
      * Obtain a password from the user.
      * @return char[] with a potential password or null if they cancel.
      */
     public char[] showPasswordDialog() {
         PasswordDialog dialog = new PasswordDialog(mainWindow);
         return dialog.getPasswordFromUser();
     }
 
     public int unsavedChangesDialog() {
         String message = "You have unsaved changes in your current file. Do you want to save?";
         String title = "Save file?";
         return JOptionPane.showConfirmDialog(
                 mainWindow,
                 message,
                 title,
                 JOptionPane.YES_NO_CANCEL_OPTION,
                 JOptionPane.QUESTION_MESSAGE);
     }
 
     /**
      * Adds an action listener to the create new password file components.
      * @param action listener to be attached to the components.
      */
     public void addNewFileListener(ActionListener action) {
         newMenu.addActionListener(action);
         newButton.addActionListener(action);
     }
 
     /**
      * Adds an action listener to the open a preexisting password file
      * components.
      * @param action listener to be attached to the components.
      */
     public void addOpenFileListener(ActionListener action) {
         openMenu.addActionListener(action);
         openButton.addActionListener(action);
     }
 
     /**
      * Adds an action listener to the close password file components.
      * @param action listener to be attached to the components.
      */
     public void addCloseFileListener(ActionListener action) {
         closeMenu.addActionListener(action);
     }
 
     /**
      * Adds an action listener to the save password file components.
      * @param action listener to be attached to the components.
      */
     public void addSaveFileListener(ActionListener action) {
         saveMenu.addActionListener(action);
         saveButton.addActionListener(action);
     }
 
     /**
     * Adds an action listener to the save as file components.
      * @param action listener to be attached to the components.
      */
     public void addSaveAsFileListener(ActionListener action) {
         saveAsMenu.addActionListener(action);
         saveAsButton.addActionListener(action);
     }
 
     /**
      * Create the PM's menu bar.
      */
     private void setUpMenu() {
         JMenuBar menubar = new JMenuBar();
         JMenu fileMenu = new JMenu("File");
 
         newMenu = makeMenuItem("New Password File", KeyEvent.VK_N);
         openMenu = makeMenuItem("Open File", KeyEvent.VK_O);
         closeMenu = makeMenuItem("Close", KeyEvent.VK_W);
         saveMenu = makeMenuItem("Save", KeyEvent.VK_S);
         saveAsMenu = makeMenuItem("Save As", KeyEvent.VK_A);
 
         fileMenu.add(newMenu);
         fileMenu.add(openMenu);
         fileMenu.add(closeMenu);
         fileMenu.add(saveMenu);
         fileMenu.add(saveAsMenu);
 
         menubar.add(fileMenu);
         mainWindow.setJMenuBar(menubar);
     }
 
     /**
      * Create a new menu item with an appropriate keyboard shortcut.
      * @param text of the menu item
      * @param keyCode should be a {@link KeyEvent} key code such as
      * VK_A, VK_B, etc.
      * @return a new {@link JMenuItem} instance.
      */
     private JMenuItem makeMenuItem(String text, int keyCode) {
         JMenuItem item = new JMenuItem(text);
         item.setAccelerator(KeyStroke.getKeyStroke(keyCode, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         return item;
     }
 
     /**
      * Create the PM's tool bar.
      * @param panel to add the tool bar to.
      */
     private void setUpToolbar(JPanel panel) {
         JToolBar toolBar = new JToolBar();
         toolBar.setFloatable(false);
 
         newButton = makeToolbarButton("new", "New");
         openButton = makeToolbarButton("open", "Open");
         saveButton = makeToolbarButton("save", "Save");
         saveAsButton = makeToolbarButton("saveAs", "Save As");
 
         toolBar.add(newButton);
         toolBar.add(openButton);
         toolBar.add(saveButton);
         toolBar.add(saveAsButton);
         panel.add(toolBar, BorderLayout.PAGE_START);
     }
 
     /**
      * Create a new tool bar button.
      * @param imageName name of the source image without the file extension.
      * @param toolTipText text to be shown on mouseover.
      * @return the new button
      */
     private JButton makeToolbarButton(String imageName, String toolTipText) {
         JButton button = new JButton(new ImageIcon("icons/" + imageName + ".gif"));
         button.setToolTipText(toolTipText);
         return button;
     }
 
 
     /**
      * Create the text area where the password file will be displayed.
      * @param panel to add the text area to.
      */
     private void setUpTextArea(JPanel panel) {
         textArea = new JTextArea(30, 40);
         textArea.setMargin(new Insets(5, 5, 5, 5));
         textArea.setLineWrap(true);
 
         // At start up you should have nothing loaded.
         hideTextArea();
 
         JScrollPane scrollPane = new JScrollPane(textArea);
         panel.add(scrollPane, BorderLayout.CENTER);
     }
 
 
 
 }
