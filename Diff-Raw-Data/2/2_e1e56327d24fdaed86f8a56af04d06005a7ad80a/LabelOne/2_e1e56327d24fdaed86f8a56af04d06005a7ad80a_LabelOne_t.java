 package org.vbn.p2.one;
 
 import java.awt.Color;
 import java.awt.Font;
 import javax.swing.*;
 
 @SuppressWarnings("serial")
 public class LabelOne extends JPanel
 {
 	public LabelOne()
 	{
		JLabel label = new JLabel("One: Primary Systems are a go!");
 		label.setFont(label.getFont().deriveFont(Font.BOLD,24));
 		label.setHorizontalAlignment(SwingConstants.CENTER);
 		label.setVerticalAlignment(SwingConstants.CENTER);
 		this.add(label);
 		this.setBorder(BorderFactory.createLineBorder(Color.black));
 	}
 }
