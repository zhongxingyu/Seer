 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dab.gui.mainpanels;
 
 import dab.engine.simulator.Simulator;
 import dab.gui.application.MainWindow;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 /**
  *
  * @author Aiste
  */
 public class NameMenu extends JPanel{
     private final Simulator simulator;
     private final JTextField enter_name, enter_name2;
     private final JLabel name_label;
     private MainWindow mainWindow;
     private boolean onePlayerMode;
    
     
     public NameMenu(final Simulator simulator,MainWindow mw, final JLayeredPane invoker, final boolean onePlayerMode){
         this.simulator = simulator;
         this.mainWindow = mw;
         this.onePlayerMode = onePlayerMode;
         setLayout(new BoxLayout(this,BoxLayout.Y_AXIS)); 
         
         if(onePlayerMode){
             name_label = new JLabel("Please enter your name");
         } else {
             name_label = new JLabel("Please enter your names");
         }
         name_label.setFont(new Font("Bookman Old Style", Font.BOLD, 18));
         name_label.setForeground(Color.ORANGE);
         
         //ok button has the same functionality as when the user presses enter
         JButton ok = new JButton();
         ImageIcon theOk = new ImageIcon("resources/menu/ok.png");
         ok.setIcon(theOk);
         ok.setBorder(BorderFactory.createEmptyBorder());
         ok.setContentAreaFilled(false);
         ok.setBorderPainted(false);
         ok.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 tryStart();
             }
         });
         
         enter_name = new JTextField(20);
         enter_name.setFont(new Font("Bookman Old Style", Font.BOLD, 18));
         enter_name.setPreferredSize(new Dimension(theOk.getIconWidth(), 40));
         enter_name.setMaximumSize(new Dimension(theOk.getIconWidth(), 40));
         enter_name.addKeyListener(new KeyAdapter() {
             @Override
             public void keyReleased(KeyEvent e) {
                 super.keyReleased(e);
                 if(e.getKeyCode() == KeyEvent.VK_ENTER){
                     if(onePlayerMode){
                         tryStart();
                     } else {
                         enter_name2.requestFocus();
                     }
                 }
             }
 
             @Override
             public void keyTyped(KeyEvent e) {            }
 
             @Override
             public void keyPressed(KeyEvent e) {            }
         });
         enter_name.setText(mainWindow.getUserName());
         
         enter_name2 = new JTextField(20);
         enter_name2.setFont(new Font("Bookman Old Style", Font.BOLD, 18));
         enter_name2.setPreferredSize(new Dimension(theOk.getIconWidth(), 40));
         enter_name2.setMaximumSize(new Dimension(theOk.getIconWidth(), 40));
         enter_name2.addKeyListener(new KeyAdapter() {
             @Override
             public void keyReleased(KeyEvent e) {
                 super.keyReleased(e);
                 if(e.getKeyCode() == KeyEvent.VK_ENTER){
                     tryStart();
                 }
             }
 
             @Override
             public void keyTyped(KeyEvent e) {            }
 
             @Override
             public void keyPressed(KeyEvent e) {            }
         });
         enter_name2.setText(mainWindow.getUserName2());
         
        
         
        
  
         add(name_label);
         add(Box.createRigidArea(new Dimension(0,10)));
         add(enter_name);
         add(Box.createRigidArea(new Dimension(0,10)));
         if(!onePlayerMode){
             add(enter_name2);
             add(Box.createRigidArea(new Dimension(0,10)));
             setBounds(400,200,theOk.getIconWidth(),300);
         } else{
             setBounds(400,200,theOk.getIconWidth(),150);
         }
         add(ok); 
     };
 
     private void tryStart() {
         if (enter_name.getText().length() == 0 ||(!onePlayerMode&&enter_name2.getText().length() == 0)) {
             JOptionPane.showMessageDialog(null, "Name can't be empty!");
         } else{
             //set the user name and initialize a new Interface                           
             simulator.setUsername(enter_name.getText());    
             if(!onePlayerMode){
                 simulator.setUsername2(enter_name2.getText());                
             }
             mainWindow.startGame(simulator,onePlayerMode);
         }
     }
    
     @Override
     public void requestFocus() {
         enter_name.requestFocusInWindow();
     }
 }
