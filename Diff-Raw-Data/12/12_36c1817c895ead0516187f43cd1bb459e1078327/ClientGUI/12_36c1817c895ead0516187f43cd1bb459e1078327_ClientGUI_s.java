 package ucbang.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 
 import java.awt.Insets;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import java.util.ArrayList;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import ucbang.core.Card;
 import ucbang.core.Player;
 
 public class ClientGUI extends JFrame{
     public ClientGUI() {
         //set window sizes
         setPreferredSize(new Dimension(800,600));
         setSize(new Dimension(800,600));
         
         //create panels
         fields = new JPanel();
         fields.setPreferredSize(new Dimension(400, 400));
         
             //TODO: create real backgrounds
         fields.setBackground(new Color(100,0,0));
         
         chat = new JPanel();
         chat.setPreferredSize(new Dimension(800, 200));
         chat.setLayout(new GridBagLayout());
         
             //TODO: create real backgrounds
         chat.setBackground(new Color(0,100,0));
 
         getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
         getContentPane().add(fields);
         getContentPane().add(chat);
         
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         //I'll take a shot at making a keylistener, but it's probably not the best way to do things
          addKeyListener(new KeyListener(){
                     public void keyTyped(KeyEvent e) {
                         lastKey = e.getKeyChar();
                     }
 
                     public void keyPressed(KeyEvent e) {
                     }
 
                     public void keyReleased(KeyEvent e) {
                     }
                 });
         
         validate();
     }
        
     JPanel fields;
     JPanel chat;
    Player player;
     Character lastKey;
     
     /**
      * Asks the player to choose a card
      * @param al
      * @return
      */
     public int promptChooseCard(ArrayList<Card> al){
         while(true){
             while(lastKey == null){
             }
             if(Character.isDigit(lastKey)){
                 if(Integer.valueOf(lastKey)%49<al.size()){
                    return Integer.valueOf(lastKey)%49;
                 }
                 else{   
                     System.out.println("You typed invalid number "+Integer.valueOf(lastKey)%48);
                     lastKey = null;
                 }
             }
         }
     }
 }
