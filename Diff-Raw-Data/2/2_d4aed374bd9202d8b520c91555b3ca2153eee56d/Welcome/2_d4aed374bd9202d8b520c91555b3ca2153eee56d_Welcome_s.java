 package Viewer;
 
 import Model.Modeller;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 /**
  *
  * @author mbaxkmt6
  */
 public class Welcome extends JPanel{
     
     public JFrame mainFrame;
     public Modeller model;
     public Welcome(JFrame frame,Modeller model) {
         initComponents();
         mainFrame = frame;
         this.model = model;
         
     }
 
     private javax.swing.JButton studentButton;
     public static  javax.swing.JButton teacherButton;
     private javax.swing.JLabel welcomeLogo;
     private java.awt.Label welcomeLabel;
     
     /**
      * This method is called from within the constructor to initialize the form.
      */
     private void initComponents() {
 
         welcomeLabel = new java.awt.Label();
         studentButton = new javax.swing.JButton();
         teacherButton = new javax.swing.JButton();
         //teacherButton.setActionCommand("teacherButton"); //test
         welcomeLogo = new javax.swing.JLabel(new ImageIcon("images/uom-logo.jpg"));
 
 
         welcomeLabel.setAlignment(java.awt.Label.CENTER);
         welcomeLabel.setBackground(new java.awt.Color(237, 230, 230));
         welcomeLabel.setFont(new java.awt.Font("Monospaced", 3, 18)); // NOI18N
         welcomeLabel.setText("Welcome to the English Language online Examination");
 
         studentButton.setText("I am a Student");
         studentButton.setToolTipText("");
         studentButton.addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                   ((Viewer)mainFrame).guiChanger(new Student(mainFrame,model));
             }
         });
         
         teacherButton.setText("I am a Teacher");
         teacherButton.addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                  ((Viewer)mainFrame).guiChanger(new TestWizard(mainFrame,model,null));
             }
         });
         
         welcomeLogo.setText("");
         
         setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
         c.anchor = GridBagConstraints.CENTER;
         c.gridx = GridBagConstraints.RELATIVE;
         c.gridwidth = GridBagConstraints.REMAINDER;
         c.gridy = 0;
         c.ipady = 50;
         c.weighty = 0;
         add(welcomeLogo,c);
         
         GridBagConstraints c1 = new GridBagConstraints();
         c1.fill = GridBagConstraints.HORIZONTAL;
         c1.weightx = 1;
         c1.weighty = 0;
         c1.gridx = GridBagConstraints.RELATIVE;
         c1.gridy = 1;
         c1.ipady = 30;
         c1.ipadx = 80;
         c1.anchor = GridBagConstraints.CENTER;
         c1.gridwidth = GridBagConstraints.REMAINDER;
         add(welcomeLabel,c1);
         
         GridBagConstraints c2 = new GridBagConstraints();
         c2.anchor = GridBagConstraints.EAST;
         c2.gridx = 0;
         c2.gridy = 2;
         c2.ipadx = 20;
         c2.ipady = 20;
         c2.insets = new Insets(50, 0, 30, 20);
         c2.weightx = 0.5;
         add(studentButton, c2);
         c2.insets = new Insets(50, 20, 30, 0);
         c2.anchor = GridBagConstraints.WEST;
         c2.gridx = 1;
         add(teacherButton,c2);
         
     }           
 
    
     
 }
