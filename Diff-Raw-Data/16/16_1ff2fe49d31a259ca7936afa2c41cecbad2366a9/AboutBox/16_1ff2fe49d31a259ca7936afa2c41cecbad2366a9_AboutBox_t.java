 /*
 Mjdj MIDI Morph - an extensible MIDI processor and translator.
 Copyright (C) 2010 Confusionists, LLC (www.confusionists.com)
 
 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 
 See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>. 
 
 You may contact the author at mjdj_midi_morph [at] confusionists.com
 */
 package com.confusionists.mjdj.ui;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.URL;
import java.util.Calendar;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 
 import com.confusionists.mjdj.Main;
 
 @SuppressWarnings("serial")
 public class AboutBox extends JDialog implements ActionListener {
 	JButton buttonOkay = new JButton();
 	JLabel imageLabel = new JLabel();
 	JLabel labelProduct = new JLabel(Main.PRODUCT_NAME);
	JLabel labelVersion = new JLabel(Main.PRODUCT_VERSION + " (Java " + System.getProperty("java.version") +")");
	JLabel labelCopyright = new JLabel("Copyright (c) 2010-" + Calendar.getInstance().get(Calendar.YEAR) + " Confusionists, LLC");
 	JTextArea labelComments = new JTextArea("See www.confusionists.com/mjdj.\n\nOn Mac OSX, mmj MIDI library generously provided by \nhumatic.de. See http://www.humatic.de/htools/mmj.htm.");
 	ImageIcon image1 = new ImageIcon();
 
 
 	public AboutBox(Frame parent) {
 		super(parent);
 		try {
 			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 			jbInit();
 		} catch (Exception exception) {
 			exception.printStackTrace();
 		}
 	}
 
 
 	public ImageIcon getImage(String filename, String filename2) {
 		URL url = getClass().getResource(filename);
 		if (url == null && filename2 != null)
 			url = getClass().getResource(filename2);
 		ImageIcon icon = null;
 		if (url == null) {
 			icon = new ImageIcon(filename);
 			if (icon == null && filename2 != null)
 				icon = new ImageIcon(filename2);
 		}
 		else
 			icon = new ImageIcon(url);
 		return icon;
 	}
 
 
 	/**
 	 * Component initialization.
 	 *
 	 * @throws java.lang.Exception
 	 */
 	private void jbInit() throws Exception {
 		try {
 			image1 = getImage("logo.jpg","/logo.jpg");
 			imageLabel.setIcon(image1);
 		} catch (Exception e) {
 			// do nothing e.printStackTrace();
 			System.err.println("Couldn't load image for about box " + e.getMessage());
 		}
 
 		setTitle("About Mjdj MIDI Morph");
 
 		JPanel topPanel = new JPanel();
 		topPanel.setLayout(new FlowLayout());
 		topPanel.add(imageLabel);
 		
 		JPanel rightTop = new JPanel();
 		rightTop.setPreferredSize(new Dimension(275, 80));
 		topPanel.add(rightTop);
 		
 		rightTop.setLayout(new GridLayout(3, 1));
 		rightTop.setBorder(new EmptyBorder(10, 20, 10,10));
 		rightTop.add(labelProduct);
 		rightTop.add(labelVersion);
 		rightTop.add(labelCopyright);
 		
 		this.add(topPanel, BorderLayout.NORTH);
 		
 		labelComments.setBorder(new EmptyBorder(10, 10, 10, 10));
 		labelComments.setAlignmentY(CENTER_ALIGNMENT);
 		labelComments.setWrapStyleWord(true);
 		labelComments.setEditable(false);
 		labelComments.setBackground(this.getBackground());
 		this.add(labelComments, BorderLayout.CENTER);
 		
 		buttonOkay.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				AboutBox.this.setVisible(false);
 			}
 		});
 		
 		//buttonOkay.setPreferredSize(new Dimension(this.getSize().width-100, 50));
 		buttonOkay.setText("Ok");
 		this.add(buttonOkay, BorderLayout.SOUTH);
 
 		setResizable(false);
 
 	}
 
 	/**
 	 * Close the dialog on a button event.
 	 *
 	 * @param actionEvent ActionEvent
 	 */
 	public void actionPerformed(ActionEvent actionEvent) {
 		if (actionEvent.getSource() == buttonOkay) {
 			dispose();
 		}
 	}
 	
 	public static void main(String[] args) {
 		AboutBox dlg = new AboutBox(null);
 		dlg.setModal(true);
 		dlg.pack();
		dlg.setSize(new Dimension(525, 250));
 		dlg.setVisible(true);
 		dlg.setResizable(false);
 	}
 }
