 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.border.Border;
 import javax.swing.border.TitledBorder;
 
 
 /**
  * The View is comprised of everything visible to the user.
  * The View is the interactive space containing a file menu, buttons, and the Canvas.
  * 
  * @author srwareham, yoshi
  * 
  */
 public class SLogoView extends View {
     /**
      * Default magnitude for moving the turtle using the forward and backward buttons.
      */
     private static final int DEFAULT_FD_MAG = 10;
     /**
      * Default magnitude for moving the turtle using the forward button.
      */
     private static final int DEFAULT_TURN_MAG = 15;
     /**
      * Value for the borders of the JPanels.
      */
     private static final int BORDER_OFFSET = 5;
     
     /**
      * A magic number for the increase in the space of the history panel when a command is added.
      */
     private static final int SIZE_INCREMENT = 60;
     /**
      * Forward button label text.
      */
     private static final String FORWARD_LABEL = "ForwardButton";
     /**
      * Backward button label text.
      */
     private static final String BACKWARD_LABEL = "BackwardButton";
     /**
      * Submit button label text.
      */
     private static final String SUBMIT_LABEL = "SubmitButton";
     /**
      * Right button label text.
      */
     private static final String RIGHT_LABEL = "RightButton";
     /**
      * Left button label text.
      */
     private static final String LEFT_LABEL = "LeftButton";
     /**
      * Forward command sent to the controller.
      */
     private static final String FD_COMMAND = "Forward";
     /**
      * Left command sent to the controller.
      */
     private static final String LEFT = "Left";
     /**
      * Right command sent to the controller.
      */
     private static final String RIGHT = "Right";
     /**
      * Initial size of the console area.
      */
     private static final Dimension PREFERRED_CONSOLE_SIZE = new Dimension(350, 100);
     /**
      * Initial size of the history area.
      */
     private static final Dimension PREFERRED_HISTORY_SIZE = new Dimension(350, 180);
     /**
      * Serial value.
      */
     private static final long serialVersionUID = 1L;
     /**
      * Name of the Canvas panel area.
      */
     private static final String CANVAS_NAME = "Canvas";
     /**
      * Name of the Workspace panel area.
      */
     private static final String WORKSPACE_NAME = "Workspace";
     /**
      * Name of the History panel area.
      */
     private static final String HISTORY_NAME = "History";
     /**
      * Name of the Input panel area.
      */
     private static final String INPUT_NAME = "Input";
     /**
      * ToolTip displayed when mouse is over the Workspace tab.
      */
     private static final String SLOGO_NAME = "SLogo";
     /**
      * Default location of the image resources.
      */
     private static final String RESOURCE_LOCATION = "/resources/images/";
     /**
      * Location of the turtle image.
      */
     private static final String TURTLE_IMG_FILENAME = "turtle_art.png";
     /**
      * Text area where the user can type, edit, copy and paste the commands.
      */
     private JTextArea myConsole;
     private JPanel myHistoryPanel;
     
     /**
      * Creates an instance of the View with personalized layout.
      * 
      * @param title The title of this View
      * @param language The desired language for the View
      */
     public SLogoView (String title, String language) {
         super(title, language);
         getContentPane().add(makeMainPanel(), BorderLayout.CENTER);
         pack();
         setVisible(true);
     }
     
     /**
      * Displays the text in the History panel and appends any text to it
      * 
      * @param text The string to be appended to the History.
      */
     @Override
     public void displayText (String text) {
         JTextArea button = makeTextButton(text);
         myHistoryPanel.add(button);
         double height = myHistoryPanel.getSize().getHeight() + SIZE_INCREMENT;
         // TODO: Change the magic number to the actual size of the buttons.
         myHistoryPanel.setPreferredSize(
                 new Dimension((int) (myHistoryPanel.getSize().getWidth()), (int) height));
         myHistoryPanel.revalidate();
         myHistoryPanel.repaint();
     }
     
     /**
      * Creates an area with a text that, when clicked, sends a command to the controller if
      * the text is formatted as a command.
      * 
      * @param text
      * @return A TextArea with a Mouse Listener
      */
     private JTextArea makeTextButton (final String text) {
         JTextArea button = new JTextArea(text);
         button.setFocusable(true);
        button.setEditable(false);
         if (!text.startsWith(">>")) {
             button.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseClicked (MouseEvent e) {
                     if (((e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK) ||
                         ((e.getModifiers() & InputEvent.META_MASK) == InputEvent.META_MASK)) {
                         StringSelection stringSelection = new StringSelection(text);
                         Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                         clpbrd.setContents(stringSelection, null);
                     }
                     else {
                         submitCommand(text);
                     }
                 }
             });
         }
         return button;
     }
     
     /**
      * Creates the main panel with a Canvas, a History and an Input panels.
      * 
      * @return The main panel with a workspace.
      */
     private JTabbedPane makeMainPanel () {
         JTabbedPane workspace = new JTabbedPane();
         JPanel contentPanel = new JPanel();
         workspace.addTab(getResourceName(WORKSPACE_NAME), null, contentPanel, SLOGO_NAME);
         contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.LINE_AXIS));
         contentPanel.setBorder(makeBorder(""));
         
         JPanel hstInpPanel = new JPanel();
         hstInpPanel.setLayout(new BoxLayout(hstInpPanel, BoxLayout.PAGE_AXIS));
         hstInpPanel.add(makeInput());
         hstInpPanel.add(makeHistoryPanel());
         contentPanel.add(makeCanvasPanel());
         contentPanel.add(hstInpPanel);
         return workspace;
     }
     
     /**
      * Creates a panel with the Canvas.
      * 
      * @return Panel with Canvas.
      */
     @Override
     protected JComponent makeCanvasPanel () {
         JPanel canvasPanel = new JPanel();
         canvasPanel.add(super.getCanvas());
         canvasPanel.setBorder(makeBorder(CANVAS_NAME));
         return canvasPanel;
     }
     
     /**
      * Creates a panel with the History, where the commands and results from the user are recorded.
      * 
      * @return Panel with History.
      */
     private JPanel makeHistoryPanel () {
         myHistoryPanel = new JPanel();
         myHistoryPanel.setPreferredSize(PREFERRED_HISTORY_SIZE);
         myHistoryPanel.setBackground(Color.white);
         myHistoryPanel.setLayout(new BoxLayout(myHistoryPanel, BoxLayout.PAGE_AXIS));
         JScrollPane myScroller = new JScrollPane();
         myScroller.setViewportView(myHistoryPanel);
         myScroller.setPreferredSize(PREFERRED_HISTORY_SIZE);
         myScroller.getVerticalScrollBar().setValue(myHistoryPanel.getHeight());
         JPanel histPane = new JPanel();
         histPane.setLayout(new BoxLayout(histPane, BoxLayout.PAGE_AXIS));
         histPane.add(myScroller);
         histPane.setBorder(makeBorder(HISTORY_NAME));
         return histPane;
     }
     
     /**
      * Creates the input panel with buttons for the user to interact with the turtle
      * and the console in which the user writes the SLogo code.
      * 
      * @return Input panel.
      */
     @Override
     protected JComponent makeInput () {
         JPanel result = new JPanel();
         result.setLayout(new BorderLayout());
         result.setBorder(makeBorder(INPUT_NAME));
         result.add(makeTurtleButtonsPanel(), BorderLayout.NORTH);
         result.add(makeCommandConsole(), BorderLayout.CENTER);
         result.add(makeSubmitButton(), BorderLayout.SOUTH);
         return result;
     }
     
     /**
      * Creates the buttons for moving the turtle.
      * The layout is not very nice, right now and are going to be improved in the following version.
      * 
      * @return Pane with the buttons.
      */
     private JComponent makeTurtleButtonsPanel () {
         JPanel turtleMovePane = new JPanel();
         ImageIcon icon =
                 new ImageIcon(getClass().getResource(RESOURCE_LOCATION + TURTLE_IMG_FILENAME));
         JLabel label = new JLabel(icon);
         turtleMovePane.setLayout(new BorderLayout());
         
         JButton fdButton =
                 makeMoveButton(getResourceName(FORWARD_LABEL),
                                getResourceName(FD_COMMAND) + " " + DEFAULT_FD_MAG);
         JButton leftButton =
                 makeMoveButton(getResourceName(LEFT_LABEL),
                                getResourceName(LEFT) + " " + DEFAULT_TURN_MAG);
         JButton rightButton =
                 makeMoveButton(getResourceName(RIGHT_LABEL),
                                getResourceName(RIGHT) + " " + DEFAULT_TURN_MAG);
         JButton backButton =
                 makeMoveButton(getResourceName(BACKWARD_LABEL),
                                getResourceName(FD_COMMAND) + " " + -DEFAULT_FD_MAG);
         
         turtleMovePane.add(fdButton, BorderLayout.NORTH);
         turtleMovePane.add(leftButton, BorderLayout.LINE_START);
         turtleMovePane.add(rightButton, BorderLayout.LINE_END);
         turtleMovePane.add(backButton, BorderLayout.SOUTH);
         turtleMovePane.add(label, BorderLayout.CENTER);
         
         return turtleMovePane;
     }
     
     /**
      * Creates a button that moves the turtle forward.
      * 
      * @return pane with the console
      */
     private JScrollPane makeCommandConsole () {
         JTextArea textArea = new JTextArea();
         myConsole = textArea;
         JScrollPane pane = new JScrollPane(myConsole);
         pane.setPreferredSize(PREFERRED_CONSOLE_SIZE);
         return pane;
     }
     
     /**
      * Creates a button that submits the text in the console to the controller.
      * The code for this button is very similar to the code of the makeTurtleMoveButton method
      * because of the distinct action listener implementation.
      * 
      * @return submit button
      */
     private JButton makeSubmitButton () {
         JButton submitButton = new JButton(getResourceName(SUBMIT_LABEL));
         submitButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed (ActionEvent e) {
                 submitCommand(myConsole.getText());
                 myConsole.setText("");
             }
         });
         submitButton.setFocusable(false);
         return submitButton;
     }
     
     /**
      * Creates a button that takes in a final command.
      * Used to create the turtle movement buttons.
      * 
      * @return button
      */
     private JButton makeMoveButton (String name, final String command) {
         JButton button = new JButton(name);
         button.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed (ActionEvent e) {
                 submitCommand(command);
             }
         });
         button.setFocusable(false);
         return button;
     }
     
     /**
      * Sends the string command for the controller to parse/ process/
      * 
      * @param command
      */
     private void submitCommand (String command) {
         super.getController().createRunInstruction(command);
     }
     
     /**
      * Creates a default border for most of the panels.
      * 
      * @return border for panels
      */
     private Border makeBorder (String panelName) {
         
         Border border;
         String title = ("".equals(panelName)) ? "" : getResourceName(panelName);
         TitledBorder titleBorder = BorderFactory.createTitledBorder(title);
         Border emptyBorder = BorderFactory.createEmptyBorder(BORDER_OFFSET, BORDER_OFFSET,
                                                              BORDER_OFFSET, BORDER_OFFSET);
         border = BorderFactory.createCompoundBorder(titleBorder, emptyBorder);
         return border;
     }
     
     /**
      * Returns a string with the text from the language resource file.
      * If there are multiple the first is returned.
      * 
      * @param name of the element
      * @return name in the desired language specified in Main.
      */
     private String getResourceName (String name) {
         String line = super.getResources().getString(name);
         String[] entries = line.split(",");
         return entries[0];
     }
 }
