 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.applet.*;
 
 import java.awt.Component;
 import javax.swing.BoxLayout;
 import javax.swing.WindowConstants;
 
 
 import java.awt.FlowLayout;
 import javax.swing.JFrame;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 
 /**
  * The class that sets up our GUI to use the englishToPigLatin class
  *
 * @author Christian Rivera Cruz, and Adam Kazberuk
 * @version CS56, Spring 2012
  * @see englishToPigLatin
  */
 
 
 public class WindowSetUp extends JApplet implements ActionListener {
     /* Declaration */
     private Container Panel;
     private JTextArea Output;
     private JScrollPane Scroller;
 
     private JTextField welcomePhrase;
     private JTextField resultPhrase;
     public TextField t1 = new TextField(20);
     
     /**
      * windowSetUp creates the JPanels that we use for the GUI, which include JTextArea, and JTextField
      */
 
     public WindowSetUp() {
 	/* Instantiation */
 	Panel = getContentPane ();
 
 	Output = new JTextArea (30, 10);
 	Scroller = new JScrollPane(Output);
 	Output.setLineWrap(true);
 	Scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 	Scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 	
 	welcomePhrase = new JTextField ("Please enter a word or phrase and then press enter:", 30);
 	resultPhrase = new JTextField("Result:", 30);
 	/* Location */
 	Panel.setLayout(new BoxLayout(Panel, BoxLayout.Y_AXIS));
 	Panel.add(welcomePhrase);
 	welcomePhrase.setEditable(false);
 	Panel.add(t1);
 	Panel.add(resultPhrase);
 	resultPhrase.setEditable(false);
 	Panel.add(Scroller);
 
    
 	/* Configuration */
 	t1.addActionListener(this);
 	//t1.setActionCommand ("text");
 	Output.setEditable (false);
     }
 
     /**
        actionPerformed is where the translations happen in our code, where the toPigLatin method is invoked
      */
 
     public void actionPerformed(ActionEvent e) {
 	String Action;
 	EnglishToPigLatin word1= new EnglishToPigLatin();
 	Action = e.getActionCommand ();
  	Output.setText(word1.toPigLatin(Action));
 	t1.selectAll();
     }     
 }
 
