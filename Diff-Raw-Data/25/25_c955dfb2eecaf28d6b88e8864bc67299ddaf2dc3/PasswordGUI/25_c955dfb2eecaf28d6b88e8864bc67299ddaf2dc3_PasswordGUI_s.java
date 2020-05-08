 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 public class PasswordGUI
 {
     private JButton button;
     private JTextField lengthField;
     private Password p;
     private JFrame frame;
 
     public void go(){
         frame = new JFrame();
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	p = new Password();
 
        lengthField = new JTextField(3);
         button = new JButton("Generate");
         button.addActionListener(new ButtonListener());
         lengthField.addActionListener(new TextFieldListener());
 
        frame.getContentPane().add(BorderLayout.SOUTH, button);
        frame.getContentPane().add(BorderLayout.NORTH, lengthField);
        frame.setSize(300,300);
         frame.setVisible(true);
     }
 
     public class ButtonListener implements ActionListener
     {
 	public void actionPerformed(ActionEvent event)
 	{
 	    //@@@ STUB FIX ME
 	}
     }
     public class TextFieldListener implements ActionListener
     {
         public void actionPerformed(ActionEvent event)
         {
	    /*
             try
                 {
                     int length = Integer.parseInt(lengthField.getText());
 		    p.setPassLength(length);
                 }
             catch(NumberFormatException e)
                 {
                     lengthField.setText("");
                 }
	    */
         }
     }
     public static void main(String[] args)
     {
 	PasswordGUI pwGUI = new PasswordGUI();
 	pwGUI.go();
     }
 
 }
