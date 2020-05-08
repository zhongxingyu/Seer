 /*******************************************************************************
  * Copyright (c) 2013 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.examples.aut.adder.javafx.gui;
 
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.scene.control.Menu;
 import javafx.scene.control.MenuBar;
 import javafx.scene.control.MenuItem;
 import javafx.scene.input.KeyCombination;
 
 /**
  * This class represents the MenuBar.
  * 
  */
 public class CalcMenuBar extends MenuBar {
 
     /**
      * File Menu
      */
     private Menu m_file;
 
     /**
      * Reset menu item 
      */
     private MenuItem m_reset;
 
     /**
      * Quit menu item
      */
     private MenuItem m_quit;
 
     /**
      * Help menu
      */
     private Menu m_help;
 
     /**
      * About menu item
      */
     private MenuItem m_about;
 
     /**
      * Constructor that Initializes the MenuBar
      */
     public CalcMenuBar() {
        this.setId("menuBar");
         m_file = new Menu("File");
         m_reset = new MenuItem("reset");
         m_quit = new MenuItem("quit");
         m_help = new Menu("Help");
         m_about = new MenuItem("about");
         initialize();
     }
 
     /**
      * Manages the Layout of the MenuBar
      */
     private void initialize() {
         m_reset.setAccelerator(KeyCombination.keyCombination("Ctrl+R"));
         this.getMenus().addAll(m_file, m_help);
 
         m_file.getItems().addAll(m_reset, m_quit);
 
         m_about.setOnAction(new EventHandler<ActionEvent>() {
 
             @Override
             public void handle(ActionEvent event) {
                 new AboutDialog().show();
             }
 
         });
         m_help.getItems().addAll(m_about);
     }
 
     /**
      * @return The reset menu item
      */
     public MenuItem getReset() {
         return m_reset;
     }
 
     /**
      * @return The close menu item
      */
     public MenuItem getQuit() {
         return m_quit;
     }
 
 }
