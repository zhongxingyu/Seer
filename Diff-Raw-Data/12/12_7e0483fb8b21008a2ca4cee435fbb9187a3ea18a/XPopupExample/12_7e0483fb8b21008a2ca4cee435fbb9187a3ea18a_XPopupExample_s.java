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
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.AbstractAction;
 import org.wings.SBorderLayout;
 import org.wings.SButton;
 import org.wings.SComponent;
 import org.wings.SDimension;
import org.wings.SForm;
 import org.wings.SGridLayout;
 import org.wings.SIcon;
 import org.wings.SPanel;
 import org.wings.SLabel;
 import org.wings.SURLIcon;
 import org.wings.border.SLineBorder;
 import org.wingx.XPopup;
 
 /**
  *
  */
 public class XPopupExample
         extends WingSetPane {
 
     private static final SIcon WINGS_IMAGE = new SURLIcon("../icons/wings-logo.png");
     private static final String SOME_TEXT =
             "What is wingS? In a nutshell, wingS is a web application\n" +
             "framework based on Java Servlets, resembling the Swing API with\n" +
             "its MVC paradigm and event oriented design principles. It utilizes\n" +
             "the models, events, and event listeners of Swing. Like in Swing,\n" +
             "components are arranged in a hierarchy of containers, whose root\n" +
             "container is hooked to a frame.";
 
    public SPanel createContent() {
         SPanel content = new SPanel(new SBorderLayout());
         content.setBorder(new SLineBorder(1));
         content.setBackground(Color.WHITE);
         content.setPreferredSize(new SDimension(200, 200));
         content.add(new SLabel(WINGS_IMAGE), SBorderLayout.NORTH);
         content.add(new SLabel(SOME_TEXT), SBorderLayout.CENTER);
         final SButton button = new SButton(new AbstractAction("Change button text") {
 
             public void actionPerformed(ActionEvent e) {
                 ((SButton) e.getSource()).setText(String.valueOf(System.currentTimeMillis()));
             }
         });
         content.add(button, SBorderLayout.SOUTH);
         return content;
     }
 
     public SComponent createExample() {
         SPanel panel = new SPanel(new SGridLayout(2, 1, 50, 50));
 
         SButton popupButton = new SButton("Popup button 1");
         panel.add(popupButton);
        final XPopup popup = new XPopup(createContent(), popupButton, XPopup.BOTTOM_LEFT, 2, 2);
         popup.setPreferredSize(new SDimension(400, 200));
         popupButton.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 if (popup.isVisible()) {
                     popup.setVisible(false);
                 } else {
                     popup.setVisible(true);
                 }
             }
         });
         return panel;
     }
 
     protected SComponent createControls() {
         return null;
     }
 
 }
