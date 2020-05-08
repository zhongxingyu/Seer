 package balle.strategy;
  
 import java.awt.BorderLayout;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.Scanner;
 
 import javax.swing.JFrame;
 import javax.swing.JTextField;
  
 import lejos.nxt.Button;
 import lejos.nxt.ButtonListener;
  
 import balle.brick.Controller;
 import balle.world.AbstractWorld;
  
 public class UserInputStrategy extends AbstractStrategy {
  
         public UserInputStrategy(Controller controller, AbstractWorld world) {
                 super(controller, world);
                 Listener l = new Listener();
                 
                 JFrame frame = new JFrame("Controller");
                 frame.setSize(500,500);
                 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                 frame.setVisible(true);
                 
                 JTextField textField = new JTextField();
                 frame.getContentPane().add(BorderLayout.CENTER, textField);
                 
                 textField.addKeyListener(l);
         }
         
         @Override
         protected void aiStep(){
                 
         }
  
         private char cmd = 's';
         
         @Override
         protected void aiMove(Controller controller) {
         	
         	System.out.println(cmd);
                 
                 switch (cmd) {
                case 'w':       controller.setWheelSpeeds( 1,  1);
                case 'a':       controller.setWheelSpeeds(-1,  1);
                case 's':       controller.setWheelSpeeds( 0,  0);
                case 'd':       controller.setWheelSpeeds( 1, -1);
                case 'x':       controller.setWheelSpeeds(-1, -1);
                 }
                 
         }
         
         class Listener implements KeyListener {
  
                 @Override
                 public void keyPressed(KeyEvent arg0) {
                         cmd = arg0.getKeyChar();
                 }
  
                 @Override
                 public void keyReleased(KeyEvent arg0) {
                 }
  
                 @Override
                 public void keyTyped(KeyEvent arg0) {                   
                 }
                 
         }
  
 }
