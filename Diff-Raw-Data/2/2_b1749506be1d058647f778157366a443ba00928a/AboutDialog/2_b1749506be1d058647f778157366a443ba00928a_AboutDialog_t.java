 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2010 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.app.ui;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Properties;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 
 /**
  * The about dialog.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class AboutDialog extends JDialog {
 
     /**
      * Creates new about dialog.
      *
      * @param parent         the parent frame
      * @param buildInfo      the application build information
      */
     public AboutDialog(ControlPanel parent, Properties buildInfo) {
         super(parent, true);
         initialize(parent, buildInfo);
         setLocationByPlatform(true);
     }
 
     /**
      * Initializes the dialog components.
      *
      * @param parent         the parent frame
      * @param buildInfo      the application build information
      */
     private void initialize(final ControlPanel parent, Properties buildInfo) {
         JLabel              label;
         JButton             button;
         GridBagConstraints  c;
         String              str;
 
         // Set dialog title
         setTitle("About RapidContext Server");
         setResizable(false);
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         getContentPane().setLayout(new GridBagLayout());
         setBackground(parent.getBackground());
 
         // Add logotype
         c = new GridBagConstraints();
         c.gridheight = 8;
         c.insets = new Insets(10, 15, 15, 10);
         c.anchor = GridBagConstraints.NORTHWEST;
         getContentPane().add(new JLabel(new ImageIcon(parent.logotype)), c);
 
         // Add application name
         label = new JLabel("RapidContext Server");
         label.setFont(Font.decode("sans bold 20"));
         label.setForeground(new Color(14, 102, 167));
         c = new GridBagConstraints();
         c.gridx = 1;
         c.gridwidth = 2;
         c.anchor = GridBagConstraints.WEST;
         c.insets = new Insets(15, 15, 10, 15);
         getContentPane().add(label, c);
 
         // Add version label
         c = new GridBagConstraints();
         c.gridx = 1;
         c.gridy = 1;
         c.anchor = GridBagConstraints.WEST;
         c.insets = new Insets(0, 15, 0, 10);
         label = new JLabel("Version:");
         label.setFont(label.getFont().deriveFont(Font.BOLD));
         getContentPane().add(label, c);
         str = buildInfo.getProperty("build.version", "N/A") +
               " (built " + buildInfo.getProperty("build.date", "N/A") + ")";
         label = new JLabel(str);
         c = new GridBagConstraints();
         c.gridx = 2;
         c.gridy = 1;
         c.weightx = 1.0;
         c.anchor = GridBagConstraints.WEST;
         c.insets = new Insets(0, 0, 0, 15);
         getContentPane().add(label, c);
 
         // Add license label
         c = new GridBagConstraints();
         c.gridx = 1;
         c.gridy = 2;
         c.anchor = GridBagConstraints.WEST;
         c.insets = new Insets(4, 15, 0, 10);
         label = new JLabel("License:");
         label.setFont(label.getFont().deriveFont(Font.BOLD));
         getContentPane().add(label, c);
         button = new JButton("BSD License");
         button.setHorizontalAlignment(SwingConstants.LEFT);
         button.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
         button.setOpaque(false);
         button.setForeground(Color.BLUE);
         button.setBackground(getBackground());
         button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         button.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 try {
                     AppUtils.openURL("http://www.rapidcontext.com/doc/LICENSE.txt");
                 } catch (Exception e) {
                     parent.error(e.getMessage());
                 }
             }
         });
         c = new GridBagConstraints();
         c.gridx = 2;
         c.gridy = 2;
         c.weightx = 1.0;
         c.anchor = GridBagConstraints.WEST;
         c.insets = new Insets(6, 0, 0, 15);
         getContentPane().add(button, c);
 
         // Add copyright
         c = new GridBagConstraints();
         c.gridx = 1;
         c.gridy = 3;
         c.gridwidth = 2;
         c.anchor = GridBagConstraints.WEST;
         c.insets = new Insets(10, 15, 0, 15);
        getContentPane().add(new JLabel("Copyright \u00A9 2007-2010 by Per Cederberg."), c);
         c = new GridBagConstraints();
         c.gridx = 1;
         c.gridy = 4;
         c.gridwidth = 2;
         c.anchor = GridBagConstraints.WEST;
         c.insets = new Insets(0, 15, 0, 15);
         getContentPane().add(new JLabel("All rights reserved."), c);
 
         // Add web site link
         label = new JLabel("Please visit the project web site:");
         c = new GridBagConstraints();
         c.gridx = 1;
         c.gridy = 5;
         c.gridwidth = 2;
         c.anchor = GridBagConstraints.WEST;
         c.insets = new Insets(10, 15, 0, 15);
         getContentPane().add(label, c);
         button = new JButton("http://www.rapidcontext.com/");
         button.setHorizontalAlignment(SwingConstants.LEFT);
         button.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
         button.setOpaque(false);
         button.setForeground(Color.BLUE);
         button.setBackground(getBackground());
         button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         button.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 try {
                     AppUtils.openURL("http://www.rapidcontext.com/");
                 } catch (Exception e) {
                     parent.error(e.getMessage());
                 }
             }
         });
         c = new GridBagConstraints();
         c.gridx = 1;
         c.gridy = 6;
         c.gridwidth = 2;
         c.anchor = GridBagConstraints.WEST;
         c.insets = new Insets(0, 15, 0, 15);
         getContentPane().add(button, c);
 
         // Add close button
         button = new JButton("Close");
         button.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 dispose();
             }
         });
         c = new GridBagConstraints();
         c.gridx = 1;
         c.gridy = 7;
         c.gridwidth = 2;
         c.weighty = 1.0;
         c.anchor = GridBagConstraints.SOUTH;
         c.insets = new Insets(20, 15, 10, 15);
         getContentPane().add(button, c);
 
         // Layout components
         pack();
     }
 }
