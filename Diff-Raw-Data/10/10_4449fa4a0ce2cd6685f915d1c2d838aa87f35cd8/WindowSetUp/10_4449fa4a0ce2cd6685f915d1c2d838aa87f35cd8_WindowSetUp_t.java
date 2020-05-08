 package edu.ucsb.cs56.projects.misc.translate_to_secret_languages.gibberish_translator;
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
  * The class that sets up our GUI to use the englishToGibberish class
  *
  * @author Christian Rivera Cruz and Adam Kazberuk
  * @author Alex Molina
  * @version CS56, Spring 2013
 * @see EnglishToGibberish
  */
 
 public class WindowSetUp extends JApplet implements ActionListener {
     /* Declaration */
     private Container Panel;
     private JTextArea Output;
     private JScrollPane Scroller;
 
     private JTextField welcomePhrase;
     private JTextField resultPhrase;
     public TextField t1 = new TextField(20);
     String[] types = {"English to Gibberish", 
 		      "Gibberish to English"};
     public JComboBox<String> pickt = new JComboBox<String>(types);
     int ty = 1;
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
 	Panel.add(pickt);
 	Panel.add(resultPhrase);
 	resultPhrase.setEditable(false);
 	Panel.add(Scroller);
 	// Adds action listener for combobox for choosing translation
     	pickt.addActionListener(new ActionListener() {
 	public void actionPerformed(ActionEvent e){
 	JComboBox cb = (JComboBox)e.getSource();
         String type = (String)cb.getSelectedItem();
 	if(type.equals("English to Gibberish"))ty = 1;
 	if(type.equals("Gibberish to English"))ty = 2; 
 	}});
 
 	/* Configuration */
 	t1.addActionListener(this);
 	Output.setEditable (false);
     }
 
     /**
        actionPerformed is where the translations happen in our code, where the toGibberish method is invoked
      */
 
     public void actionPerformed(ActionEvent e) {
 	String Action;
 	EnglishToGibberish word1= new EnglishToGibberish();
 	Action = e.getActionCommand ();
 	if(ty == 1)
  	Output.setText(word1.toGibberish(Action));
 	if(ty == 2)
  	Output.setText(word1.fromGibberish(Action));
  	t1.selectAll();
     }     
 }
 
