 package arcade.view;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import arcade.model.Model;
 
 
 /**
  * The view that allows a new user to register an account.
  * 
  * @author Ellango
  * 
  */
 @SuppressWarnings("serial")
 public class RegisterView extends Account {
     private static final String DEFAULT_IMAGE =
             new File(System.getProperty("user.dir") + "/src/arcade/resources/images/rcd.jpg")
                     .getPath();
     private static final int WINDOW_WIDTH = 240;
     private static final int WINDOW_HEIGHT = 330;
     private JTextField myFirstNameTextField;
     private JTextField myLastNameTextField;
     private JTextField myDOBTextField;
     private String myImagePath = DEFAULT_IMAGE;
 
     /**
      * Constructs the register view with a Model and Resource Bundle
      * 
      * 
      * @param model
      * @param resources
      */
     public RegisterView (Model model, ResourceBundle resources) {
         super(model, resources);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
         setLocationRelativeTo(null);
     }
     
     /**
      * Lets the user know that the username has already been taken and prompts
      * them to choose a different one.
      */
     public void sendUsernameTakenError() {
         clearUsername();
         sendMessage(getResources().getString(TextKeywords.USERNAME_ERROR));
     }
     
     /**
      * Lets the user know that they formatted the date of birth field 
      * incorrectly and prompts them to try reentering.
      */
     public void sendDOBError() {
         myDOBTextField.setText(TextKeywords.BIRTHDATE_MESSAGE);
         sendMessage(getResources().getString(TextKeywords.BIRTHDATE_ERROR));
     }
 
     @Override
     protected List<JComponent> makeComponents () {
         List<JComponent> components = new ArrayList<JComponent>();
         components.add(createInstructions());
         components.add(createUsernameField());
         components.add(createPasswordField());
         components.add(createFirstNameField());
         components.add(createLastNameField());
         components.add(createDOBField());
         components.add(createImageSelector());
         components.add(createMessageArea());
         components.add(createButton());
         return components;
     }
 
     /**
      * Creates a label representing instructions for the user.
      * 
      * @return
      */
     private JComponent createInstructions () {
         JPanel panel = new JPanel();
         JLabel instruction = new JLabel(getResources().getString(TextKeywords.REGISTER_MESSAGE));
         panel.add(instruction);
         return panel;
     }
 
     /**
      * Creates the field where the user can enter his/her first name
      * 
      * @return
      */
     private JComponent createFirstNameField () {
         myFirstNameTextField = new JTextField();
         return createTextPanel(TextKeywords.FIRSTNAME, myFirstNameTextField);
     }
 
     /**
      * Creates the field where the user can enter his/her last name.
      * 
      * @return
      */
     private JComponent createLastNameField () {
         myLastNameTextField = new JTextField();
         return createTextPanel(TextKeywords.LASTNAME, myLastNameTextField);
     }
 
     /**
      * Creates the field where the user can enter his/her date of birth.
      * 
      * @return
      */
     private JComponent createDOBField () {
         myDOBTextField = new JTextField();
         myDOBTextField.setText(getResources().getString(TextKeywords.BIRTHDATE_MESSAGE));
         myDOBTextField.addFocusListener(new FocusListener() {
             @Override
             public void focusGained (FocusEvent arg0) {
                 myDOBTextField.setText("");
             }
 
             @Override
             public void focusLost (FocusEvent arg0) {
                 // do nothing
             }
         });
         return createTextPanel(TextKeywords.BIRTHDATE, myDOBTextField);
     }
 
     /**
      * Creates the field to select the user's image
      */
     private JComponent createImageSelector () {
         JPanel panel = new JPanel();
         JLabel description = new JLabel(getResources().getString(TextKeywords.IMAGE_MESSAGE));
         panel.add(description);
         JButton button = new JButton(getResources().getString(TextKeywords.IMAGE_BUTON));
         button.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed (ActionEvent arg0) {
                 JFileChooser chooser = new JFileChooser();
                 FileFilter filter =
                         new FileNameExtensionFilter(getResources().getString(TextKeywords.IMAGE),
                                                     "jpg", "gif", "png");
                 chooser.setFileFilter(filter);
 
                 int returnVal = chooser.showOpenDialog(null);
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     myImagePath = chooser.getSelectedFile().getPath();
                 }
             }
         });
         panel.add(button);
         return panel;
     }
 
     /**
      * Creates the button where the user can submit his/her information.
      * 
      * @return
      */
     private JComponent createButton () {
         JPanel panel = new JPanel();
         JButton register = new JButton(getResources().getString(TextKeywords.REGISTER));
         register.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed (ActionEvent arg0) {
                 getModel().createNewUserProfile(getUsername(), 
                                                 getPassword(), 
                                                 myFirstNameTextField.getText(),
                                                 myLastNameTextField.getText(),
                                                myDOBTextField.getText());//,
                                                //myImagePath);
                 // maybe.
                 dispose();
             }
         });
         panel.add(register);
         return panel;
     }
 
 }
