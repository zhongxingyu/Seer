 package edu.wustl.cab2b.client.ui.mainframe;
 
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.APPLICATION_RESOURCES_FILE_NAME;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.CAB2B_LOGO_IMAGE;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.ERROR_CODE_FILE_NAME;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.DisplayMode;
 import java.awt.Font;
 import java.awt.GraphicsEnvironment;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.File;
 import java.rmi.RemoteException;
 import java.util.MissingResourceException;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.border.Border;
 import javax.swing.text.Keymap;
 
 import org.jdesktop.swingx.JXErrorDialog;
 import org.jdesktop.swingx.JXFrame;
 
 import edu.wustl.cab2b.client.ui.RiverLayout;
 import edu.wustl.cab2b.client.ui.controls.Cab2bComboBox;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bTextField;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.client.ui.util.CustomSwingWorker;
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeConstants;
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeHandler;
 import edu.wustl.cab2b.common.exception.CheckedException;
 import edu.wustl.cab2b.common.util.PropertyLoader;
 import edu.wustl.common.util.global.ApplicationProperties;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * @author Chandrakant Talele
  * @author Hrishikesh Rajpathak
  */
 public class LoginFrame extends JXFrame {
 
     private static final long serialVersionUID = 1L;
 
     private static final Border border = BorderFactory.createLineBorder(new Color(100, 200, 220));
 
     private Cab2bComboBox idProvider;
 
     private Cab2bTextField usrNameText;
 
     private JPasswordField passText;
 
     public LoginFrame selfReference = this;
 
     private static Font font = getTextFont();
 
     private JLabel credentialError;
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         try {
             Logger.configure();
             setHome();
             Logger.configure(); // pick config from log4j.properties
             initializeResources(); // Initialize all Resources
 
             LoginFrame loginFrame = new LoginFrame();
             loginFrame.setVisible(true);
         } catch (Throwable t) {
             JXErrorDialog.showDialog(
                                      null,
                                      "caB2B Fatal Error",
                                      "Fatal error orccured while launching caB2B client.\nPlease contact administrator",
                                      t);
             System.exit(1);
         }
     }
 
     /**
      * Set the caB2B home
      */
     public static void setHome() {
         File cab2bHome = new File(System.getProperty("user.home"), "cab2b");
         System.setProperty("cab2b.home", cab2bHome.getAbsolutePath());
     }
 
     protected static void initializeResources() {
         try {
             ErrorCodeHandler.initBundle(ERROR_CODE_FILE_NAME);
             ApplicationProperties.initBundle(APPLICATION_RESOURCES_FILE_NAME);
         } catch (MissingResourceException mre) {
             CheckedException checkedException = new CheckedException(mre.getMessage(), mre,
                     ErrorCodeConstants.IO_0002);
             CommonUtils.handleException(checkedException, null, true, true, false, true);
         }
     }
 
     private static Font getTextFont() {
         Cab2bLabel label = new Cab2bLabel(":");
         return new Font("calibri", Font.BOLD, label.getFont().getSize() + 2);
     }
 
     public LoginFrame() {
         super("Login - ca Bench To Bedside (B2B)");
         initUI();
     }
 
     public void initUI() {
         ImageIcon bannerImage = getImageIcon("top_img.gif");
         Cab2bLabel topImage = new Cab2bLabel(bannerImage);
         ImageIcon bgImage = getImageIcon("body_bg.gif");
 
         int totalWidth = bannerImage.getIconWidth();
         int totalHeight = bgImage.getIconHeight() + bannerImage.getIconHeight();
         Point start = getStartPosition(totalWidth, totalHeight);
 
         BackgroundImagePanel centralPanel = new BackgroundImagePanel(bgImage.getImage());
         centralPanel.setLayout(new BorderLayout());
         centralPanel.add(getLeftPanel(), BorderLayout.LINE_START);
         centralPanel.add(getRightPanel(), BorderLayout.CENTER);
 
         credentialError = new JLabel("    ");
         credentialError.setForeground(Color.RED);
         JPanel errorPanel = new JPanel(new BorderLayout());
         errorPanel.add(credentialError, BorderLayout.CENTER);
         errorPanel.add(new JLabel("   "), BorderLayout.SOUTH);
         errorPanel.setOpaque(false);
 
         centralPanel.add(errorPanel, BorderLayout.SOUTH);
 
         JPanel mainpanel = new JPanel();
         mainpanel.setLayout(new BorderLayout());
         mainpanel.add(topImage, BorderLayout.NORTH);
         mainpanel.add(centralPanel, BorderLayout.CENTER);
 
         mainpanel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(145, 145, 145)));
         getContentPane().add(mainpanel);
         setIconImage(getImageIcon(CAB2B_LOGO_IMAGE).getImage());
         setLocation(start);
         setUndecorated(true);
         setSize(new Dimension(totalWidth, totalHeight));
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setResizable(false);
         Toolkit.getDefaultToolkit().setDynamicLayout(true);
     }
 
     private Cab2bTextField getTextField(String text) {
         Cab2bTextField textField = new Cab2bTextField();
         textField.setBorder(border);
         textField.setText(text);
         return textField;
     }
 
     private Point getStartPosition(int width, int height) {
         DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
         Float w = (displayMode.getWidth() - width) / 2F;
         Float h = (displayMode.getHeight() - height) / 2F;
         return new Point(w.intValue(), h.intValue());
     }
 
     private JPanel getLeftPanel() {
         ImageIcon userImage = getImageIcon("user.gif");
         Cab2bLabel userImageLabel = new Cab2bLabel(userImage);
 
         JPanel leftPanel = getTransparentPanel();
         leftPanel.setLayout(new BorderLayout(25, 0));
         leftPanel.add(new Cab2bLabel(), BorderLayout.NORTH);
         leftPanel.add(new Cab2bLabel(), BorderLayout.LINE_START);
         leftPanel.add(userImageLabel, BorderLayout.CENTER);
         leftPanel.add(new Cab2bLabel(), BorderLayout.SOUTH);
         return leftPanel;
     }
 
     private JPanel getTransparentPanel() {
         JPanel panel = new JPanel();
         panel.setOpaque(false);
         return panel;
     }
 
     private Cab2bLabel getLabel(String text) {
         Cab2bLabel userNameLabel = new Cab2bLabel(text, font);
         userNameLabel.setOpaque(false);
         return userNameLabel;
     }
 
     private void setKeyMap(JTextField txtField) {
 
         Keymap keyMap = JTextField.addKeymap("enter", txtField.getKeymap());
         KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
         keyMap.addActionForKeyStroke(key, new AbstractAction() {
             public void actionPerformed(ActionEvent arg0) {
                 swingWorkerLogic();
             }
         });
         txtField.setKeymap(keyMap);
     }
 
     private JPanel getRightPanel() {
         Cab2bLabel userNameLabel = getLabel("User Name :");
         Cab2bLabel passWordLabel = getLabel("Password :");
         Cab2bLabel idProviderLabel = getLabel("ID Provider :");
 
         usrNameText = getTextField("");
         usrNameText.setPreferredSize(new Dimension(160, 20));
 
         passText = new JPasswordField();
         passText.setEchoChar('*');
         passText.setBorder(border);
         passText.setPreferredSize(new Dimension(160, 20));
 
         idProvider = new Cab2bComboBox();
         idProvider.setPreferredSize(new Dimension(160, 23));
         Font idProviderFont = new Font(idProvider.getFont().getName(), Font.PLAIN,
                 idProvider.getFont().getSize() + 1);
         idProvider.setFont(idProviderFont);
         idProvider.setOpaque(false);
         idProvider.setBorder(border);
         String[] idPNames = PropertyLoader.getIdPNames();
         int len = idPNames.length;
         for (int i = 0; i < len; i++) {
             idProvider.addItem(idPNames[i]);
         }
 
         ImageIcon loginImage = getImageIcon("login_button.gif");
         JButton loginButton = new JButton(loginImage);
         loginButton.setBorder(null);
         loginButton.addActionListener(new LoginButtonListener());
         loginButton.setPreferredSize(new Dimension(loginImage.getIconWidth(), loginImage.getIconHeight()));
 
         setKeyMap(usrNameText);
         setKeyMap(passText);
         idProvider.addKeyListener(new KeyListener() {
             public void keyPressed(KeyEvent e) {
             }
 
             public void keyReleased(KeyEvent e) {
                 if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                     swingWorkerLogic();
                 }
             }
 
             public void keyTyped(KeyEvent e) {
             }
         });
 
         Cab2bHyperlink<String> cancelLink = new Cab2bHyperlink<String>(true);
         cancelLink.setFont(new Font(cancelLink.getFont().getName(), Font.PLAIN, cancelLink.getFont().getSize() + 1));
         cancelLink.setText("Cancel");
         cancelLink.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 System.exit(-1);
             }
         });
 
         JPanel containerPanel = getTransparentPanel();
         containerPanel.setLayout(new RiverLayout(10, 5));
         containerPanel.add("br", userNameLabel);
         containerPanel.add("tab", usrNameText);
         containerPanel.add("br", passWordLabel);
         containerPanel.add("tab", passText);
         containerPanel.add("br", idProviderLabel);
         containerPanel.add("tab", idProvider);
         containerPanel.add("br", new Cab2bLabel());
         containerPanel.add("br tab", loginButton);
         containerPanel.add("tab", new Cab2bLabel("|"));
         containerPanel.add("tab", cancelLink);
 
         JPanel panel = getTransparentPanel();
         panel.setLayout(new BorderLayout(2, 60));
         panel.add(new Cab2bLabel(), BorderLayout.NORTH);
         panel.add(containerPanel, BorderLayout.CENTER);
         return panel;
     }
 
     public ImageIcon getImageIcon(String imageName) {
         return new ImageIcon(this.getClass().getClassLoader().getResource(imageName));
     }
 
     /**
      * This method checks if the user trying to login is a valid grid user or
      * not
      * 
      * @param userName
      * @param password
      * @param idProvider
      * @return boolean stating is the user is a valid grid user or not
      * @throws RemoteException
      */
     final private void validateCredentials(String userName, String password, String idProvider)
             throws RemoteException {
         UserValidator.validateUser(userName, password, idProvider);
     }
 
     /**
      * @author Hrishikesh Rajpathak
      */
     private class LoginButtonListener implements ActionListener {
 
         public void actionPerformed(ActionEvent e) {
             swingWorkerLogic();
         }
     }
 
     private void swingWorkerLogic() {
         CustomSwingWorker swingWorker = new CustomSwingWorker(LoginFrame.this) {
             @Override
             protected void doNonUILogic() {
                 nonUILogic();
             }
 
             @Override
             protected void doUIUpdateLogic() throws Exception {
             }
         };
         swingWorker.start();
     }
 
     private void nonUILogic() {
 
         credentialError.setText(" ");
         final String userName = usrNameText.getText();
         char[] passwordArray = passText.getPassword();
         if (userName == "" || passwordArray.length == 0) {
             credentialError.setText("  * Please enter username and password");
             credentialError.setForeground(Color.RED);
             return;
         }
         String password = new String(passwordArray);
         String IDProvider = idProvider.getSelectedItem().toString();
 
         String url = PropertyLoader.getJndiUrl();
         System.setProperty("java.naming.provider.url", url);
         System.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
         System.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
 
         try {
             validateCredentials(userName, password, IDProvider);
             Thread mainThread = new Thread() {
                 public void run() {
                     MainFrame.launchMainFrame(userName);
                 }
             };
             mainThread.setPriority(Thread.NORM_PRIORITY);
            selfReference.dispose();
             mainThread.start();
            
         } catch (Exception e) {
             credentialError.setText("  * Unable to authenticate: Invalid credentials");
             credentialError.setForeground(Color.RED);
             //CommonUtils.handleException(e, LoginFrame.this, true, true, true, true);
         }
         // selfReference.dispose();
     }
 }
