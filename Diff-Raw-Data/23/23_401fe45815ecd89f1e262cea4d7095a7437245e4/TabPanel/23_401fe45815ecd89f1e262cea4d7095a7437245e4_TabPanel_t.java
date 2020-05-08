 /*
  * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
  */
 package org.javascool.gui;
 
 import javax.swing.*;
 import javax.swing.plaf.basic.BasicButtonUI;
 import java.awt.*;
 import java.awt.event.*;
 
 /**
  * Component to be used as tabComponent;
  * Contains a JLabel to show the text and
  * a JButton to close the tab it belongs to
  */
 class TabPanel extends JPanel {
 
     private static final long serialVersionUID = 1L;
     private final JTabbedPane pane;
     private final String file;
 
     public TabPanel(final JTabbedPane pane) {
         this(pane, "");
     }
 
     public TabPanel(final JTabbedPane pane, String fileId) {
         // unset default FlowLayout' gaps
         super(new FlowLayout(FlowLayout.LEFT, 0, 0));
         this.file = fileId;
         if (pane == null) {
             throw new NullPointerException("TabbedPane is null");
         }
         this.pane = pane;
         setOpaque(false);
 
         // make JLabel read titles from JTabbedPane
         JLabel label = new JLabel() {
 
             private static final long serialVersionUID = 1L;
 
             @Override
             public String getText() {
                 int i = pane.indexOfTabComponent(TabPanel.this);
                 if (i != -1) {
                     return pane.getTitleAt(i);
                 }
                 return null;
             }
         };
 
         add(label);
         // add more space between the label and the button
         label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
         // tab button
         JButton button = new TabButton();
         add(button);
         // add more space to the top of the component
         setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
     }
 
     private class TabButton extends JButton implements ActionListener {
 
         private static final long serialVersionUID = 1L;
 
         public TabButton() {
             int size = 17;
             setPreferredSize(new Dimension(size, size));
             setToolTipText("close this tab");
             // Make the button looks the same for all Laf's
             setUI(new BasicButtonUI());
             // Make it transparent
             setContentAreaFilled(false);
             // No need to be focusable
             setFocusable(false);
             setBorder(BorderFactory.createEtchedBorder());
             setBorderPainted(false);
             // Making nice rollover effect
             // we use the same listener for all buttons
             addMouseListener(buttonMouseListener);
             setRolloverEnabled(true);
             // Close the proper tab by clicking the button
             addActionListener(this);
         }
 
         @Override
         public void actionPerformed(ActionEvent e) {
             if(pane.getTabCount()<=1){
                 return;
             }
             if (confirmClose()) {
                 int i = pane.indexOfTabComponent(TabPanel.this);
                 if (i != -1) {
                     pane.remove(i);
                 }
             }
         }
         // we don't want to update UI for this button
 
         @Override
         public void updateUI() {
         }
         // paint the cross
 
         @Override
         protected void paintComponent(Graphics g) {
             if(pane.getTabCount()<=1){
                 this.setVisible(false);
                 this.revalidate();
                 return;
             } else {
                 this.setVisible(true);
                 this.revalidate();
             }
             super.paintComponent(g);
             Graphics2D g2 = (Graphics2D) g.create();
             // shift the image for pressed buttons
             if (getModel().isPressed()) {
                 g2.translate(1, 1);
             }
             g2.setStroke(new BasicStroke(2));
             g2.setColor(Color.BLACK);
             if (getModel().isRollover()) {
                 g2.setColor(Color.WHITE);
             }
             int delta = 6;
             g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
             g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
             g2.dispose();
         }
 
         private boolean confirmClose() {
             if (pane instanceof JVSFileTabs) {
                 if (!JVSPanel.getInstance().getHasToSave(file)) {
                    JVSFileTabs.getInstance().closeFile(file);
                     return true;
                 }
                 if (JVSPanel.getInstance().saveFileIdBeforeClose(file) == 1) {
                     JVSFileTabs.getInstance().closeFile(file);
                     return true;
                 } else {
                     return false;
                 }
             }
             if(pane.getTabCount()<=1){
                 return false;
             }
             return true;
         }
     }
     private final static MouseListener buttonMouseListener = new MouseAdapter() {
 
         @Override
         public void mouseEntered(MouseEvent e) {
             Component component = e.getComponent();
             if (component instanceof AbstractButton) {
                 AbstractButton button = (AbstractButton) component;
                 button.setBorderPainted(false);
             }
         }
 
         @Override
         public void mouseExited(MouseEvent e) {
             Component component = e.getComponent();
             if (component instanceof AbstractButton) {
                 AbstractButton button = (AbstractButton) component;
                 button.setBorderPainted(false);
             }
         }
     };
 }
