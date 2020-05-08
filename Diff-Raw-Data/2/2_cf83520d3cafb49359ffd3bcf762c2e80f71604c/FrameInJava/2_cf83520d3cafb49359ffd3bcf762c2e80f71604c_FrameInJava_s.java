 /**
  * FrameInJava - Sample Swing Application, written in Java, that does
  * Fahrenheit <--> Celsius Temperature Conversion
  *
  * Copyright 2009, Keith Bennett
  */
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Event;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 
 /**
  * Main application frame containing menus, text fields for the temperatures,
  * and buttons.  The menu items and buttons are driven by shared actions,
  * which are disabled and enabled based on the state of the text fields.
  *
  * Temperatures can be converted in either direction, Fahrenheit to Celsius
  * or Celsius to Fahrenheit.  The convert actions (F2C, C2F) are each enabled
  * when the respective source text field (F for F2C, C for C2F) contains text
  * that can successfully be parsed to a Double.
  *
  * The Clear action is enabled when there is any text in either of the text fields.
  */
 public class FrameInJava extends JFrame {
 
     private JTextField fahrTextField;
     private JTextField celsTextField;
 
     // These actions will be shared by menu items and buttons.
     private AbstractAction f2cAction;   // convert Fahr. to Cels.
     private AbstractAction c2fAction;   // convert Cels. to Fahr.
     private AbstractAction clearAction; // clear F and C text fields
     private AbstractAction exitAction;  // exit the program
 
 
     // Note: Making these calculation functions static makes it possible to
     // unit test them without the overhead of instantiating a Swing window.
     // They should ideally be in a separate class altogether, but for the
     // purposes of this illustration they're included here for convenience.
 
     /**
      * @return temperature in celsius given a fahrenheit value.
      */
     public static double calc_f2c(double fahr) {
         return (5.0 / 9.0) * (fahr - 32.0);
     }
 
 
     /**
      * @return temperature in fahrenheit given a celsius value.
      */
     public static double calc_c2f(double cels) {
         return cels * (9.0 / 5.0) + 32.0;
     }
 
 
     /**
      * @return true if, and only if, the string passed can successfully
      * be converted to a floating point number (specifically, a Double).
      */
     private static boolean doubleStringIsValid(String s) {
 
         boolean valid = true;
 
         try {
             new Double(s);
         } catch(NumberFormatException e) {
             valid = false;
         }
 
         return valid;
     }
 
 
 
     /**
      * Sole constructor, sets up frame with all components, centers on screen,
      * and sets it up to exit the program when closed.
      */
     public FrameInJava() {
         super("Fahrenheit <--> Celsius Converter");
         initActions();
         getContentPane().add(createConvertersPanel(), BorderLayout.CENTER);
         getContentPane().add(createButtonsPanel(),    BorderLayout.SOUTH);
         setJMenuBar(createMenuBar());
         ((JPanel) getContentPane()).setBorder
                 (BorderFactory.createEmptyBorder(12, 12, 12, 12));
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         pack();
         centerOnScreen();
     }
 
 
     /**
      * @return a text field for temperature input, with tooltip text.
      */
     private JTextField createTextField() {
         JTextField textField = new JTextField(15);
         textField.setToolTipText("Input a temperature.");
         return textField;
     }
 
 
     /**
      * Sets up the Fahrenheit and Celsius text fields with tooltip text.
      */
     private void createTextFields() {
         fahrTextField = createTextField();
         celsTextField = createTextField();
     }
 
 
     /**
      * @return a panel containing the Fahrenheit and Celsius labels
      *         and text fields.
      */
     private JPanel createConvertersPanel() {
 
         JPanel labelPanel = new JPanel(new GridLayout(0, 1, 5, 5));
         labelPanel.add(new JLabel("Fahrenheit:  "));
         labelPanel.add(new JLabel("Celsius:  "));
 
         JPanel textFieldPanel = new JPanel(new GridLayout(0, 1, 5, 5));
         createTextFields();
         textFieldPanel.add(fahrTextField);
         textFieldPanel.add(celsTextField);
 
         setupDocumentListeners();
 
         JPanel panel = new JPanel(new BorderLayout());
         panel.add(labelPanel, BorderLayout.WEST);
         panel.add(textFieldPanel, BorderLayout.CENTER);
 
         return panel;
     }
 
 
 
     /**
      * @return menu bar populated with File, Edit, and Convert
      *         menus with items
      */
     private JMenuBar createMenuBar() {
 
         JMenuBar menubar = new JMenuBar();
 
         JMenu file_menu = new JMenu("File");
         file_menu.add(exitAction);
         menubar.add(file_menu);
 
         JMenu edit_menu = new JMenu("Edit");
         edit_menu.add(clearAction);
         menubar.add(edit_menu);
 
         JMenu convert_menu = new JMenu("Convert");
         convert_menu.add(f2cAction);
         convert_menu.add(c2fAction);
         menubar.add(convert_menu);
 
         return menubar;
     }
 
 
 
     /**
      * Initialize and set enabled states for the actions.
      */
     private void initActions() {
 
         f2cAction    = new F2CAction();
         c2fAction    = new C2FAction();
         clearAction  = new ClearAction();
         exitAction   = new ExitAction();
 
         // The initial state of the form will have nothing in the text fields.
         // Therefore, the conversion and clear actions are inappropriate and
         // are set to disabled here.
         //
         // If the logic were more complex, and the desired enabled state could
         // not be known at compile time, then we would find a way to call the
         // same logic as the DocumentListener does, possibly by calling a
         // DocumentListener method with null as the event parameter.
         f2cAction.setEnabled(false);
         c2fAction.setEnabled(false);
         clearAction.setEnabled(false);
     }
 
 
 
     /**
      * Sets up text fields with document listeners so that the actions
      * are enabled only when appropriate.
      */
     private void setupDocumentListeners() {
 
         // Will result in the F2C action being enabled only when the field's
         // text can be parsed to a floating point number.
         fahrTextField.getDocument().addDocumentListener(new SimpleDocumentListener() {
             public void handleDocumentEvent(DocumentEvent event) {
                 f2cAction.setEnabled(doubleStringIsValid(fahrTextField.getText()));
             }
         });
 
         // Will result in the C2F action being enabled only when the Celsius
         // field's text can be parsed to a floating point number.
         celsTextField.getDocument().addDocumentListener(new SimpleDocumentListener() {
             public void handleDocumentEvent(DocumentEvent event) {
                 c2fAction.setEnabled(doubleStringIsValid(celsTextField.getText()));
             }
         });
 
 
         // Will result in the Clear action being enabled only when there is
         // any text in either text field.
         DocumentListener clearDocumentListener = new ClearActionDocumentListener();
         fahrTextField.getDocument().addDocumentListener(clearDocumentListener);
         celsTextField.getDocument().addDocumentListener(clearDocumentListener);
     }
 
 
     /**
      * @return the button panel laid out such that the buttons will always
      * stay at the right side of the window.
      */
     private JPanel createButtonsPanel() {
 
         JPanel innerPanel = new JPanel(new GridLayout(1, 0, 5, 5));
         innerPanel.add(new JButton(f2cAction));
         innerPanel.add(new JButton(c2fAction));
         innerPanel.add(new JButton(clearAction));
         innerPanel.add(new JButton(exitAction));
 
         JPanel outerPanel = new JPanel(new BorderLayout());
         outerPanel.add(innerPanel, BorderLayout.EAST);
         outerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
         return outerPanel;
     }
 
 
     /**
      * Action to convert from Fahrenheit to Celsius that will be accessible
      * via button, menu item, or Ctrl-s.
      */
     private class F2CAction extends AbstractAction {
 
         F2CAction() {
             super("Fahr -> Cels");
             putValue(Action.SHORT_DESCRIPTION, "Convert from Fahrenheit to Celsius");
             putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
         }
 
         public void actionPerformed(ActionEvent event) {
 
             String text = fahrTextField.getText();
             if (text != null && text.length() > 0)
             {
                 double fahr = Double.parseDouble(text);
                 double cels = calc_f2c(fahr);
                 String celsText = Double.toString(cels);
                 celsTextField.setText(celsText);
             }
         }
     }
 
 
 
     /**
      * Action to convert from Celsius to Fahrenheit that will be accessible
      * via button, menu item, or Ctrl-t.
      */
     private class C2FAction extends AbstractAction {
 
         C2FAction() {
             super("Cels -> Fahr");
             putValue(Action.SHORT_DESCRIPTION, "Convert from Celsius to Fahrenheit");
             putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_T, Event.CTRL_MASK));
         }
 
         public void actionPerformed(ActionEvent event) {
             String text = celsTextField.getText();
             if (text != null && text.length() > 0)
             {
                 double cels = Double.parseDouble(text);
                 double fahr = calc_c2f(cels);
                 String fahrText = Double.toString(fahr);
                 fahrTextField.setText(fahrText);
             }
         }
     }
 
 
 
     /**
      * Action to clear the text fields that will be accessible via button,
      * menu item, or Ctrl-l.
      */
     private class ClearAction extends AbstractAction {
 
         ClearAction() {
             super("Clear");
             putValue(Action.SHORT_DESCRIPTION, "Clear the temperature text fields");
             putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK));
         }
 
         public void actionPerformed(ActionEvent event) {
             fahrTextField.setText("");
             celsTextField.setText("");
         }
     }
 
 
 
     /**
      * DocumentListener that will set the enabled state of the Clear
      * action based on the state of the text fields.
      *
      * Usually, these kinds of actions are implemented as anonymous inner
      * classes, but this one's implementation was a little longer, so I
      * made it a named inner class instead.
      */
     class ClearActionDocumentListener extends SimpleDocumentListener {
 
         public void handleDocumentEvent(DocumentEvent event) {
 
             String ctext = celsTextField.getText();
             String ftext = fahrTextField.getText();
 
             boolean should_enable =
                     (ctext != null && ctext.length() > 0) ||
                     (ftext != null && ftext.length() > 0);
 
             clearAction.setEnabled(should_enable);
         }
     }
 
 
     /**
      * Action to exit the program that will be accessible via button,
      * menu item, or Ctrl-x.
      */
     private class ExitAction extends AbstractAction {
 
         ExitAction() {
             super("Exit");
             putValue(Action.SHORT_DESCRIPTION, "Exit this program");
             putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));
         }
 
         public void actionPerformed(ActionEvent event) {
             System.exit(0);
         }
     }
 
 
 
     /**
      * Centers the window on the screen based on the graphical information
      * reported by the java.awt.Toolkit.  Note that in some cases, such as
      * use of multiple nonmirrored displays, the position may be odd, since
      * the toolkit may report the sum of all display space across all available
      * displays.
      */
     private void centerOnScreen() {
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         Dimension componentSize = getSize();
         double new_x = (screenSize.getWidth()  - componentSize.getWidth())  / 2;
         double new_y = (screenSize.getHeight() - componentSize.getHeight()) / 2;
         setLocation((int) new_x, (int) new_y);
     }
 
 
 
     public static void main(String [] args) {
          new FrameInJava().setVisible(true);
     }
 }
