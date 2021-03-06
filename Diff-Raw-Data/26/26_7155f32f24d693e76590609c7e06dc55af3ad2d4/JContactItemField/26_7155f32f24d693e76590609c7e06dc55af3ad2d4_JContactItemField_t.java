 /**
  * $Revision$
  * $Date$
  *
  * Copyright (C) 1999-2005 Jive Software. All rights reserved.
  * This software is the proprietary information of Jive Software. Use is subject to license terms.
  */
 
 package org.jivesoftware.spark.component;
 
 import org.jivesoftware.resource.SparkRes;
 import org.jivesoftware.spark.ui.ContactItem;
 import org.jivesoftware.spark.util.ModelUtil;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JWindow;
 import javax.swing.ListCellRenderer;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Point;
 import java.awt.Window;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Implementation of a popup field from a TextField.
  *
  * @author Derek DeMoro
  */
public class JContactItemField extends JPanel {
 
     private JTextField textField = new JTextField();
     private DefaultListModel model = new DefaultListModel();
     private JList list = new JList(model);
     private JWindow popup;
     private List<ContactItem> items;
 
     private Window parentWindow;
 
     public JContactItemField(List items, Window parentWindow) {
         setLayout(new BorderLayout());
         this.items = items;
 
         add(textField, BorderLayout.CENTER);
 
 
         textField.addKeyListener(new KeyAdapter() {
             public void keyReleased(KeyEvent keyEvent) {
                 char ch = keyEvent.getKeyChar();
                 if (validateChar(ch)) {
                     showPopupMenu();
                 }
 
                 if (ch == KeyEvent.VK_ENTER) {
                     int index = list.getSelectedIndex();
                     if (index >= 0) {
                         ContactItem selection = (ContactItem)list.getSelectedValue();
                         textField.setText(selection.getNickname());
                         popup.setVisible(false);
                     }
                 }
 
                 if (ch == KeyEvent.VK_ESCAPE) {
                     popup.setVisible(false);
                 }
                 dispatchEvent(keyEvent);
             }
 
             public void keyPressed(KeyEvent e) {
                 if (isArrowKey(e)) {
                     list.dispatchEvent(e);
                 }
 
             }
         });
 
 
        list.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    textField.requestFocus();
                }
            }
        });
 
 
         popup = new JWindow(parentWindow);
 
 
         popup.getContentPane().add(new JScrollPane(list));
 
         list.setCellRenderer(new PopupRenderer());
     }
 
     public void setItems(List list) {
         this.items = items;
     }
 
     private void showPopupMenu() {
         model.removeAllElements();
 
         String typedItem = textField.getText();
 
         final List<ContactItem> validItems = new ArrayList<ContactItem>();
         for (ContactItem contactItem : items) {
             String nickname = contactItem.getNickname().toLowerCase();
             if (nickname.startsWith(typedItem.toLowerCase())) {
                 validItems.add(contactItem);
             }
         }
 
 
         if (validItems.size() > 0) {
             for (final ContactItem label : validItems) {
                 model.addElement(label);
             }
         }
 
         if (validItems.size() != 0 && !popup.isVisible()) {
             popup.pack();
             popup.setSize(textField.getWidth(), 200);
             Point pt = textField.getLocationOnScreen();
             pt.translate(0, textField.getHeight());
             popup.setLocation(pt);
             popup.toFront();
             popup.setVisible(true);
         }
     }
 
     /**
      * Validate the given text - to pass it must contain letters, digits, '@', '-', '_', '.', ','
      * or a space character.
      *
      * @param text the text to check
      * @return true if the given text is valid, false otherwise.
      */
     public boolean validateChars(String text) {
         if (!ModelUtil.hasLength(text)) {
             return false;
         }
 
         for (int i = 0; i < text.length(); i++) {
             char ch = text.charAt(i);
             if (!Character.isLetterOrDigit(ch) && ch != '@' && ch != '-' && ch != '_'
                     && ch != '.' && ch != ',' && ch != ' ') {
                 return false;
             }
         }
 
 
         return true;
     }
 
     /**
      * Validate the given text - to pass it must contain letters, digits, '@', '-', '_', '.', ','
      * or a space character.
      *
      * @param ch the character
      * @return true if the given text is valid, false otherwise.
      */
     public boolean validateChar(char ch) {
         if (!Character.isLetterOrDigit(ch) && ch != '@' && ch != '-' && ch != '_'
                 && ch != '.' && ch != ',' && ch != ' ' && ch != KeyEvent.VK_BACK_SPACE && ch != KeyEvent.CTRL_DOWN_MASK
                 && ch != KeyEvent.CTRL_MASK) {
             return false;
         }
 
         return true;
     }
 
     public boolean isArrowKey(KeyEvent e) {
         if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
             return true;
         }
         return false;
     }
 
     public String getText() {
         return textField.getText();
     }
 
     public void setText(String text) {
         textField.setText(text);
     }
 
     class PopupRenderer extends JLabel implements ListCellRenderer {
 
         /**
          * Construct Default JLabelIconRenderer.
          */
         public PopupRenderer() {
             setOpaque(true);
             this.setHorizontalTextPosition(JLabel.RIGHT);
             this.setHorizontalAlignment(JLabel.LEFT);
         }
 
         public Component getListCellRendererComponent(JList list,
                                                       Object value,
                                                       int index,
                                                       boolean isSelected,
                                                       boolean cellHasFocus) {
             if (isSelected) {
                 setBackground(list.getSelectionBackground());
                 setForeground(list.getSelectionForeground());
             }
             else {
                 setBackground(list.getBackground());
                 setForeground(list.getForeground());
             }
 
 
             ContactItem contactItem = (ContactItem)value;
             setText(contactItem.getNickname());
             if (contactItem.getIcon() == null) {
                 setIcon(SparkRes.getImageIcon(SparkRes.CLEAR_BALL_ICON));
             }
             else {
                 setIcon(contactItem.getIcon());
             }
             setFont(contactItem.getNicknameLabel().getFont());
             setForeground(contactItem.getForeground());
 
             return this;
         }
     }
 
 
 }
