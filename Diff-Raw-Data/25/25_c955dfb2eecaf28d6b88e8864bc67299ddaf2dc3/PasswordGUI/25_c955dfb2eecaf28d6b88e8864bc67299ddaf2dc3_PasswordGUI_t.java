 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 public class PasswordGUI
 {
     private JButton button;
    private JTextField greeting;
     private JTextField lengthField;
     private Password p;
     private JFrame frame;
    private JTextField passwordOutputField;
 
     public void go(){
         frame = new JFrame();
	frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	p = new Password();
 
	greeting = new JTextField("Please enter the length of the password:", 30);
        lengthField = new JTextField("");
         button = new JButton("Generate");
	passwordOutputField = new JTextField("");
         button.addActionListener(new ButtonListener());
         lengthField.addActionListener(new TextFieldListener());
 
	frame.getContentPane().add(BorderLayout.NORTH, greeting);
	greeting.setEditable(false);
	frame.getContentPane().add(BorderLayout.NORTH, lengthField);
        frame.getContentPane().add(BorderLayout.CENTER, button); 
	frame.getContentPane().add(BorderLayout.SOUTH, passwordOutputField);
        frame.setSize(300,150);
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
             try
                 {
                     int length = Integer.parseInt(lengthField.getText());
 		    p.setPassLength(length);
                 }
             catch(NumberFormatException e)
                 {
                     lengthField.setText("");
                 }
         }
     }
     public static void main(String[] args)
     {
 	PasswordGUI pwGUI = new PasswordGUI();
 	pwGUI.go();
     }
 
 }
