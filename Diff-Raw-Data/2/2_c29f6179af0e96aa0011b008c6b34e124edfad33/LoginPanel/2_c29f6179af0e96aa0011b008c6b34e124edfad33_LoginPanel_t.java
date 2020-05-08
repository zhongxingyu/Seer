 package com.alertscape.browser.upramp.firstparty.login;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 
 import com.alertscape.browser.model.BrowserContext;
 import com.alertscape.browser.upramp.model.AbstractUpRampPanel;
 import com.alertscape.browser.upramp.model.UpRamp;
 
 public class LoginPanel extends AbstractUpRampPanel {
   private static final long serialVersionUID = 4623156130067672541L;
   private JSeparator buttonSeparator;
   private JTextField userField;
   private JLabel userLabel;
   private JPasswordField passwordField = new JPasswordField();
   private JLabel passwordLabel;
   private JButton sendButton = new JButton("Login");
   private JButton cancelButton = new JButton("Cancel");
 
   private boolean sendPressed = false;
 
   public LoginPanel(BrowserContext bcontext, UpRamp ramp) {
     // setup the member variables
     setContext(bcontext);
     setUpramp(ramp);
   }
 
   @Override
   public void associateHideListener(ActionListener listener) {
     sendButton.addActionListener(listener);
     cancelButton.addActionListener(listener);
     passwordField.addActionListener(listener);
   }
 
   @Override
   protected Map buildSubmitMap() {
     HashMap map = new HashMap();
     map.put(LoginConstants.USER_ID, userField.getText());
     map.put(LoginConstants.PASSWORD, passwordField.getPassword());
 
     return map;
   }
 
   @Override
   public Dimension getBaseSize() {
     return new Dimension(300, 125);
   }
 
   @Override
   protected boolean initialize(Map values) {
     // setLayout(new BorderLayout());
     // userField = new JTextField();
     // userField.setMinimumSize(new Dimension(150, 20));
     // passwordField = new JPasswordField();
     // passwordField.setMinimumSize(new Dimension(150, 20));
     //
     // FormLayout layout = new FormLayout("right:pref, 3dlu, default:grow", "");
     // DefaultFormBuilder builder = new DefaultFormBuilder(layout);
     // builder.append("Username: ", userField);
     // builder.nextLine();
     // builder.append("Password: ", passwordField);
     //
     // add(builder.getPanel(), BorderLayout.CENTER);
     //
     // sendButton.addActionListener(new ActionListener() {
     // public void actionPerformed(ActionEvent arg0) {
     // sendPressed = true;
     // }
     // });
     //
     // add(ButtonBarFactory.buildOKCancelBar(sendButton, cancelButton), BorderLayout.SOUTH);
 
     // assuming that there is no reason we would get any config from the server, I will not pull anything from the map
 
     // step 1: build out the ui
     BoxLayout mainbox = new BoxLayout(this, BoxLayout.Y_AXIS);
     this.setLayout(mainbox);
 
     JPanel useridPanel = new JPanel();
     BoxLayout userbox = new BoxLayout(useridPanel, BoxLayout.X_AXIS);
     useridPanel.setLayout(userbox);
     useridPanel.add(Box.createRigidArea(new Dimension(5, 0)));
     userLabel = new JLabel("Username");
     userLabel.setToolTipText("The username you would like to login with");
     userLabel.setFocusable(false);
     userLabel.setPreferredSize(new Dimension(75, 20));
     userLabel.setMinimumSize(new Dimension(75, 20));
     useridPanel.add(userLabel);
     useridPanel.add(Box.createRigidArea(new Dimension(5, 0)));
     userField = new JTextField();
     useridPanel.add(userField);
     useridPanel.add(Box.createRigidArea(new Dimension(5, 0)));
     userField.setNextFocusableComponent(passwordField);
 
     this.add(Box.createRigidArea(new Dimension(0, 5)));
     this.add(useridPanel);
 
     JPanel passwordPanel = new JPanel();
     BoxLayout passwordbox = new BoxLayout(passwordPanel, BoxLayout.X_AXIS);
     passwordPanel.setLayout(passwordbox);
     passwordPanel.add(Box.createRigidArea(new Dimension(5, 0)));
     passwordLabel = new JLabel("Password");
     passwordLabel.setToolTipText("The password for the userid you would like to login with");
     passwordLabel.setFocusable(false);
     passwordLabel.setPreferredSize(new Dimension(75, 20));
     passwordLabel.setMinimumSize(new Dimension(75, 20));
     passwordPanel.add(passwordLabel);
     passwordPanel.add(Box.createRigidArea(new Dimension(5, 0)));
     passwordPanel.add(passwordField);
     passwordPanel.add(Box.createRigidArea(new Dimension(5, 0)));
     passwordField.setNextFocusableComponent(sendButton);
 
     this.add(Box.createRigidArea(new Dimension(0, 5)));
     this.add(passwordPanel);
     this.add(Box.createRigidArea(new Dimension(0, 5)));
 
     JPanel buttonSeparatorPanel = new JPanel();
     BoxLayout buttonSepBoxLayout = new BoxLayout(buttonSeparatorPanel, BoxLayout.X_AXIS);
     buttonSeparatorPanel.setLayout(buttonSepBoxLayout);
     buttonSeparator = new JSeparator();
     buttonSeparatorPanel.add(Box.createRigidArea(new Dimension(5, 0)));
     buttonSeparatorPanel.add(buttonSeparator);
     buttonSeparatorPanel.add(Box.createRigidArea(new Dimension(5, 0)));
 
     this.add(buttonSeparatorPanel);
     this.add(Box.createRigidArea(new Dimension(0, 5)));
 
     JPanel buttonPanel = new JPanel();
     BoxLayout buttonbox = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);
     buttonPanel.setLayout(buttonbox);
     buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
     cancelButton.setText("Cancel");
     cancelButton.setToolTipText("Cancel logging in");
     buttonPanel.add(cancelButton);
     buttonPanel.add(Box.createHorizontalGlue());
     sendButton.setText("Login");
     sendButton.setToolTipText("Login with this username/password");
     buttonPanel.add(sendButton);
     buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
     sendButton.setNextFocusableComponent(cancelButton);
     cancelButton.setNextFocusableComponent(userField);
 
     this.add(buttonPanel);
     this.add(Box.createRigidArea(new Dimension(0, 5)));
 
     // add a listener to store that the send button was pressed
     sendButton.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent arg0) {
         sendPressed = true;
       }
     });
 
     userField.addActionListener(new ActionListener() {
 
       @Override
       public void actionPerformed(ActionEvent e) {
        passwordField.requestFocus();
       }
 
     });
 
     passwordField.addActionListener(new ActionListener() {
 
       @Override
       public void actionPerformed(ActionEvent e) {
         sendPressed = true;
       }
 
     });
 
     return true; // TODO: should do something better than just returning true here
   }
 
   @Override
   public boolean needsSubmit() {
     return sendPressed;
   }
 
   public void setSendPressed(boolean sendPressed) {
     this.sendPressed = sendPressed;
   }
 
   public boolean isSendPressed() {
     return sendPressed;
   }
 }
