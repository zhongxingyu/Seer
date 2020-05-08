 /*
  * Nebula2D is a cross-platform, 2D game engine for PC, Mac, & Linux
  * Copyright (c) 2014 Jon Bonazza
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.nebula2d.editor.ui;
 
 import com.nebula2d.editor.framework.GameObject;
 import com.nebula2d.editor.framework.components.Component;
 import com.nebula2d.editor.ui.controls.N2DLabel;
 import com.nebula2d.editor.ui.controls.N2DList;
 import com.nebula2d.editor.ui.controls.N2DPanel;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * Dialog for manipulating GameObject components
  */
 public class ComponentsDialog extends JDialog {
 
     private N2DPanel rightPanel;
     private GameObject gameObject;
     private N2DPanel mainPanel;
     private N2DList<Component> componentList;
 
     public ComponentsDialog(GameObject gameObject) {
         this.gameObject = gameObject;
 
         N2DPanel componentListPanel = forgeComponentsListPanel();
         rightPanel = forgeEmptyPanel();
         mainPanel = new N2DPanel(new BorderLayout());
         mainPanel.add(componentListPanel, BorderLayout.WEST);
         mainPanel.add(rightPanel);
 
         add(mainPanel);
         setSize(new Dimension(800, 600));
         setResizable(false);
         setLocationRelativeTo(null);
         setVisible(true);
     }
 
     public N2DList<Component> getComponentList() {
         return componentList;
     }
 
     private N2DPanel forgeEmptyPanel() {
         N2DPanel panel = new N2DPanel();
         N2DLabel emptyLbl = new N2DLabel("No component currently selected.");
         panel.add(emptyLbl);
 
         return panel;
     }
 
     private N2DPanel forgeComponentsListPanel() {
         componentList = new N2DList<Component>();
         final DefaultListModel<Component> model = new DefaultListModel<Component>();
         componentList.setModel(model);
 
         populateComponentList(componentList);
 
         JScrollPane sp = new JScrollPane(componentList);
         sp.setPreferredSize(new Dimension(200, 400));
 
         final JButton addButton = new JButton("Add");
         addButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 new NewComponentPopup(gameObject, componentList).show(addButton, -1, addButton.getHeight());
             }
         });
         final JButton removeButton = new JButton("Remove");
         removeButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 Component selectedComponent = componentList.getSelectedValue();
                 gameObject.removeComponent(selectedComponent);
                 ((DefaultListModel<Component>) componentList.getModel()).removeElement(selectedComponent);
                 mainPanel.remove(rightPanel);
                rightPanel = forgeEmptyPanel();
                mainPanel.add(rightPanel);
                mainPanel.validate();
                mainPanel.repaint();
             }
         });
         removeButton.setEnabled(false);
 
         N2DPanel buttonPanel = new N2DPanel();
         buttonPanel.add(addButton);
         buttonPanel.add(removeButton);
 
         componentList.addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 if (e.getValueIsAdjusting())
                     return;
 
                 Component component = componentList.getSelectedValue();
 
                 removeButton.setEnabled(component != null);
 
                 if (component != null) {
                     mainPanel.remove(rightPanel);
                     rightPanel = component.forgeComponentContentPanel(ComponentsDialog.this);
                     mainPanel.add(rightPanel);
                    mainPanel.validate();
                    mainPanel.repaint();
                 }
             }
         });
 
         N2DPanel mainPanel = new N2DPanel(new BorderLayout());
 
         mainPanel.add(buttonPanel, BorderLayout.NORTH);
         mainPanel.add(sp);
 
         return mainPanel;
     }
 
     private void populateComponentList(N2DList<Component> listBox) {
         DefaultListModel<Component> model = (DefaultListModel<Component>) listBox.getModel();
         for (Component component : gameObject.getComponents()) {
             model.addElement(component);
         }
     }
 
 }
