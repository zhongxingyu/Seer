 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package wingset;
 
 import org.wings.*;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 /**
  * <code>DialogExample</code>.
  * <p/>
  * User: raedler
  * Date: Oct 11, 2007
  * Time: 12:42:05 PM
  *
  * @author raedler
  * @version $Id
  */
 public class DialogExample extends WingSetPane {
 
     protected static final Boolean[] TRUE_FALSE = new Boolean[]{Boolean.TRUE, Boolean.FALSE};
 
     private ComponentControls controls;
 
     protected SComponent createControls() {
         controls = new DialogControls();
         return controls;
     }
 
     protected SComponent createExample() {
         return new SPanel();
     }
 
     class DialogControls extends ComponentControls {
 
         private STextField firstname;
         private STextField lastname;
         private STextField email;
 
         DialogControls() {
             final SComboBox modality = new SComboBox(TRUE_FALSE);
             addControl(new SLabel("Modal:"));
             addControl(modality);
 
             final SComboBox draggability = new SComboBox(TRUE_FALSE);
             addControl(new SLabel("Draggable"));
             addControl(draggability);
 
             SButton createDialog = new SButton("Create Dialog");
             addControl(createDialog);
 
             createDialog.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                 	
                     Boolean modal = (Boolean) modality.getSelectedItem();
                     Boolean draggable = (Boolean) draggability.getSelectedItem();
 
                     final SDialog dialog = new SDialog(getParentFrame(), "Contact Dialog", modal);
                     dialog.setLayout(new SFlowDownLayout());
                     dialog.setDraggable(draggable);
                     dialog.add(createContent());
                    
                    SPanel buttonPanel = new SPanel(new SFlowLayout(SConstants.CENTER, 5, 20));
                    buttonPanel.setPreferredSize(SDimension.FULLWIDTH);
                     SButton okButton = new SButton("OK");
                     buttonPanel.add(okButton);
                     okButton.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent e) {
 
                             if (!"".equals(firstname.getText()) && firstname.getText() != null &&
                                     !"".equals(lastname.getText()) && lastname.getText() != null &&
                                     !"".equals(email.getText()) && email.getText() != null) {
                                 SOptionPane.showMessageDialog(DialogExample.this, "Hi " + firstname.getText() + " " + lastname.getText() + ", your email address is " + email.getText() + ".", "Your Information", SOptionPane.INFORMATION_MESSAGE);
                                 dialog.setVisible(false);
                             }
                             else {
                             	SOptionPane.showMessageDialog(DialogExample.this, "Please fill out the form.", "Information incomplete", SOptionPane.ERROR_MESSAGE);
                             }
                         }
                     });
 
                     SButton cancelButton = new SButton("Cancel");
                     buttonPanel.add(cancelButton);
                     cancelButton.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent e) {
                             dialog.setVisible(false);
                         }
                     });
 
                     dialog.add(buttonPanel);
                     dialog.setVisible(true);
                 }
             });
         }
 
         private SPanel createContent() {
 
             firstname = new STextField();
             lastname = new STextField();
             email = new STextField();
             SLabel image = new SLabel(new SURLIcon("../icons/cowSmall.gif"));
 
            SPanel content = new SPanel(new SGridLayout(4, 2, 20, 5));
             content.add(new SLabel("Firstname:"));
             content.add(firstname);
             content.add(new SLabel("Lastname:"));
             content.add(lastname);
             content.add(new SLabel("E-Mail:"));
             content.add(email);
             content.add(new SLabel("Image:"));
             content.add(image);
 
             return content;
         }
     }
 }
