 package view;
 
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Arrays;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 import javax.swing.border.Border;
 import roosterprogramma.RoosterProgramma;
 
 public class Login extends JPanel implements MouseListener, ActionListener {
 
     private JTextField tfUsername;
     private JPasswordField tfPassword;
     private JTextField WrongPass;
     private JLabel lblPass;
     private JButton GebruikersnaamOk;
     private JButton WachtwoordOk;
     private Border blackline;
 
     private static String Gebruikersnaam = "arend";
     private static String Wachtwoord = "arjogi";
 
     public Login(int state) {
         super();
         this.setLayout(null);
         initComponents();
         addUpdateLabel(state);
     }
 
     private void initComponents() {
         addTitle();
         addForm();
     }
 
     private void addUpdateLabel(int state) {
         JPanel updatePanel = new JPanel();
         updatePanel.setLayout(new FlowLayout());
         updatePanel.setBounds(0, RoosterProgramma.getInstance().ScreenHeight, RoosterProgramma.getInstance().ScreenWidth, 50);
         add(updatePanel);
 
         JLabel updateLabel = new JLabel();
         updateLabel.setFont(RoosterProgramma.FONT_12_BOLD);
         updateLabel.setSize(updatePanel.getWidth(), updatePanel.getHeight());
         updatePanel.add(updateLabel);
 
         switch (state)
         {
             case 0:
             {
                 updateLabel.setText("U gebruikt de laatste versie van deze applicatie.");
                 updateLabel.setForeground(Color.green);
                 break;
             }
             case 1:
             {
                 updateLabel.setText("U gebruikt niet de laatste versie van deze applicatie");
                 updateLabel.setForeground(Color.red);
                 break;
             }
             case 2:
             {
                 updateLabel.setText("U heeft geen internetverbinding, versiecontrole is uit.");
                 updateLabel.setForeground(Color.orange);
                 break;
             }
             case 3:
             {
                 updateLabel.setText("Er kan niet worden gecontroleerd op updates, een herinstallatie verhelpt dit probleem.");
                 updateLabel.setForeground(Color.orange);
                 break;
             }
             default:
             {
                 break;
             }
         }
     }
 
     private void addTitle() {
         JLabel lblTitle1 = new JLabel();
        lblTitle1.setText("Winkelapplicatie");
         lblTitle1.setBounds(20, 20, 150, 20);
         lblTitle1.setFont(RoosterProgramma.FONT_16_BOLD);
         add(lblTitle1);
 
         JLabel lblTitle2 = new JLabel();
         lblTitle2.setText("-");
         lblTitle2.setBounds(170, 20, 20, 20);
         lblTitle2.setFont(RoosterProgramma.FONT_16_BOLD);
         add(lblTitle2);
 
         JLabel lblTitle3 = new JLabel();
         lblTitle3.setText("Login");
         lblTitle3.setBounds(190, 20, 500, 20);
         lblTitle3.setFont(RoosterProgramma.FONT_16_BOLD);
         add(lblTitle3);
 
     }
 
     private void addForm() {
         int height = (int) (RoosterProgramma.getInstance().ScreenHeight);
         double width = RoosterProgramma.getInstance().ScreenWidth;
 
         blackline = BorderFactory.createLineBorder(Color.black);
 
         JPanel loginbox = new JPanel();
         loginbox.setBackground(Color.gray);
         loginbox.setForeground(Color.black);
         loginbox.setBorder(blackline);
         loginbox.setSize((int) (width / 2.2), (int) (height / 2));
         loginbox.setLocation((int) (width / 3.5), height / 8);
         loginbox.setLayout(null);
         add(loginbox);
 
         JLabel lblFormTitle = new JLabel("Login");
         lblFormTitle.setBounds((int) (loginbox.getWidth() / 2.2), (int) (loginbox.getHeight()/ 5.5), 150, 50);
         lblFormTitle.setFont(RoosterProgramma.FONT_16_BOLD);
         loginbox.add(lblFormTitle);
 
         JLabel lblName = new JLabel("Username:");
         lblName.setBounds((int) (loginbox.getWidth() / 4), (int) (loginbox.getHeight() / 3), 100, 20);
         lblName.setFont(RoosterProgramma.FONT_12_BOLD);
         loginbox.add(lblName);
 
         lblPass = new JLabel("Password:");
         lblPass.setVisible(false);
         lblPass.setBounds((int) (loginbox.getWidth() / 4), (int) (loginbox.getHeight() / 2), 100, 20);
         lblPass.setFont(RoosterProgramma.FONT_12_BOLD);
         loginbox.add(lblPass);
 
         WrongPass = new JTextField();
         WrongPass.setBounds((int) (loginbox.getWidth() / 4), (int) (loginbox.getHeight() / 1.5), 270, 20);
         WrongPass.setFont(RoosterProgramma.FONT_12_BOLD);
         WrongPass.setVisible(false);
         loginbox.add(WrongPass);
 
         tfUsername = new JTextField();
         tfUsername.setBounds((int) (lblName.getWidth() + lblName.getX()), (int) (loginbox.getHeight() / 3), 150, 20);
         tfUsername.setFont(RoosterProgramma.FONT_12_BOLD);
         loginbox.add(tfUsername);
 
         GebruikersnaamOk = new JButton("Ok");
         GebruikersnaamOk.setBounds((int) (tfUsername.getWidth() + tfUsername.getX()+20), (int) (loginbox.getHeight() / 3), 50, 20);
         GebruikersnaamOk.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 HandleUsername();
             }
         });
         loginbox.add(GebruikersnaamOk);
 
         tfPassword = new JPasswordField();
         tfPassword.setVisible(false);
         tfPassword.setBounds((int) (lblPass.getWidth() + lblPass.getX()), (int) (loginbox.getHeight() / 2), 150, 20);
         tfPassword.setFont(RoosterProgramma.FONT_12_BOLD);
         loginbox.add(tfPassword);
 
         WachtwoordOk = new JButton("Ok");
         WachtwoordOk.setBounds((int) (tfPassword.getWidth() + tfPassword.getX()+20), (int) (loginbox.getHeight() / 2), 50, 20);
         WachtwoordOk.setVisible(false);
         WachtwoordOk.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 HandlePassword();
             }
         });
         loginbox.add(WachtwoordOk);
 
         tfUsername.addKeyListener(new java.awt.event.KeyAdapter()
         {
             @Override
             public void keyTyped(final KeyEvent e)
             {
                 if (e.getKeyChar() == '\n')
                 {
                     HandleUsername();
                 }
             }
         });
 
         tfPassword.addKeyListener(new java.awt.event.KeyAdapter()
         {
             @Override
             public void keyTyped(final KeyEvent e)
             {
                 if (e.getKeyChar() == '\n')
                 {
                     HandlePassword();
                 }
             }
         });
     }
 
     private void HandleUsername() {
         String Username = tfUsername.getText();
         if (Gebruikersnaam.equals(Username))
         {
             WrongPass.setVisible(false);
             tfPassword.setVisible(true);
             lblPass.setVisible(true);
             WachtwoordOk.setVisible(true);
             tfPassword.requestFocusInWindow();
         }
         else
         {
             WrongPass.setVisible(true);
             WrongPass.setText("Gebruikersnaam is onjuist!");
             WrongPass.setEnabled(false);
             tfPassword.setVisible(false);
             lblPass.setVisible(false);
             tfPassword.setText("");
             WachtwoordOk.setVisible(false);
         }
     }
 
     private void HandlePassword() {
         String TypedPass = "";
         char[] input = tfPassword.getPassword();
         for (char output : input)
         {
             TypedPass += output;
         }
         Arrays.fill(input, '0');
 
         if (TypedPass.equals(Wachtwoord))
         {
             RoosterProgramma.getInstance().showPanel(new medewerkerOverzicht());
         }
         else
         {
             tfPassword.setText("");
             WrongPass.setVisible(true);
             WrongPass.setText("Wachtwoord is onjuist!");
             WrongPass.setEnabled(false);
         }
     }
 
     @Override
     public void actionPerformed(ActionEvent event) {
         // Intentionally left blank.
     }
 
     @Override
     public void mouseClicked(MouseEvent event) {
         // Intentionally left blank.
     }
 
     @Override
     public void mouseEntered(MouseEvent event) {
         // Intentionally left blank.
     }
 
     @Override
     public void mousePressed(MouseEvent event) {
         // Intentionally left blank.
     }
 
     @Override
     public void mouseReleased(MouseEvent event) {
         // Intentionally left blank.
     }
 
     @Override
     public void mouseExited(MouseEvent event) {
         // Intentionally left blank.
     }
 
     @Override
     public void paint(Graphics graphics) {
         super.paint(graphics);
         graphics.drawLine(20, 45, 540, 45);		// under H Title
     }
 }
