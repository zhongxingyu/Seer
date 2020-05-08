 /**
  * Coop Network Tetris — A cooperative tetris over the Internet.
  * 
  * Copyright Ⓒ 2012  Mattias Andrée, Peyman Eshtiagh,
  *                   Calle Lejdbrandt, Magnus Lundberg
  *
  * Project for prutt12 (DD2385), KTH.
  */
 package cnt.gui;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 
 
 /**
  * @author  Magnus Lundberg
  */
 public class Chatt extends JFrame implements ActionListener
 {
     /**
      * Desired by {@link Serializable}
      */
     private static final long serialVersionUID = 1L;
     
     scrollText ta;
     public Chatt() throws IOException
     {
 	ta = new scrollText();
 	JTextField da = new JTextField("Type message here");
 	add(ta);
 	add(da);
 	this.setLayout(new GridLayout(2,1));
 	setVisible(true);
 	da.addActionListener(this);
 	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	pack();
     }
     
     public void actionPerformed(ActionEvent e)
     {
 	String temp = ((JTextField) e.getSource()).getText(); //Retrieves typed message
 	ta.addText(temp, "Magnus", Color.red);
 	((JTextField) e.getSource()).setText("Type message here"); //Reset message field
     }
     
     public static void main(String[] args) throws IOException
     {
	new Chatt();
     }
 }
 
