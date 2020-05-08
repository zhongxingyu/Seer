 /*
  * Collab desktop - Software for shared drawing via internet in real-time
  * Copyright (C) 2012 Martin Indra <aktive@seznam.cz>
  *
  * This file is part of Collab desktop.
  *
  * Collab desktop is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Collab desktop is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Collab desktop.  If not, see <http://www.gnu.org/licenses/>.
  */
 package cz.mgn.collabdesktop.menu.frames.settings;
 
 import cz.mgn.collabdesktop.menu.MenuFrame;
 import cz.mgn.collabdesktop.menu.frames.settings.sections.Connection;
 import cz.mgn.collabdesktop.menu.frames.settings.sections.LoadAndSave;
 import cz.mgn.collabdesktop.menu.frames.settings.sections.Room;
import cz.mgn.collabdesktop.utils.settings.SettingsIO;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.ArrayList;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.border.EmptyBorder;
 
 /**
  *
  *   @author Martin Indra <aktive@seznam.cz>
  */
 public class SettingsFrame extends MenuFrame implements SettingsInterface, WindowListener {
 
     protected JTabbedPane tabs;
     protected ArrayList<SettingsPanel> sections = new ArrayList<SettingsPanel>();
     protected JDialog closingDialog;
 
     public SettingsFrame() {
         super();
         centerWindow();
         setVisible(true);
         addWindowListener(this);
         initSections();
     }
 
     @Override
     public void afterGo() {
         setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
     }
 
     @Override
     protected String getSectionName() {
         return "settings";
     }
 
     @Override
     protected void initComponents() {
         setPreferredSize(new Dimension(600, 400));
         setSize(getPreferredSize());
         setLayout(new BorderLayout());
         tabs = new JTabbedPane();
         add(tabs);
     }
 
     protected void initSections() {
         addSection(new Connection());
         addSection(new Room());
         addSection(new LoadAndSave(this));
     }
 
     public void addSection(SettingsPanel panel) {
         tabs.add(panel.getPanelName(), panel);
         sections.add(panel);
     }
 
     @Override
     public void resetAll() {
         for (SettingsPanel panel : sections) {
             panel.reset();
         }
     }
 
     @Override
     public void setAll() {
         for (SettingsPanel panel : sections) {
             panel.set();
         }
     }
 
     @Override
     public void windowOpened(WindowEvent e) {
     }
 
     @Override
     public void windowClosing(WindowEvent e) {
         boolean isChanged = false;
         for (SettingsPanel panel : sections) {
             isChanged = isChanged || panel.isChanged();
         }
         if (isChanged) {
             showWantToSaveDialog();
         } else {
             dispose();
         }
     }
 
     @Override
     public void windowClosed(WindowEvent e) {
     }
 
     @Override
     public void windowIconified(WindowEvent e) {
     }
 
     @Override
     public void windowDeiconified(WindowEvent e) {
     }
 
     @Override
     public void windowActivated(WindowEvent e) {
     }
 
     @Override
     public void windowDeactivated(WindowEvent e) {
     }
 
     protected void showWantToSaveDialog() {
         if(closingDialog != null) {
             closingDialog.dispose();
         }
         closingDialog = new JDialog(this, "Unsaved changes");
 
         JTextArea errorText = new JTextArea("There are any unsaved changes.");
         errorText.setLineWrap(true);
         errorText.setWrapStyleWord(true);
         errorText.setEditable(false);
         errorText.setOpaque(false);
         JButton exit = new JButton("Exit");
         JButton save = new JButton("Save & exit");
         JPanel buttons = new JPanel(new GridLayout(1, 2, 5, 0));
         buttons.add(save);
         buttons.add(exit);
         JPanel all = new JPanel(new BorderLayout(0, 5));
         all.setBorder(new EmptyBorder(5, 5, 5, 5));
         all.add(errorText);
         all.add(buttons, BorderLayout.SOUTH);
         closingDialog.getContentPane().setLayout(new BorderLayout());
         closingDialog.getContentPane().add(all);
         closingDialog.setSize(400, 150);
         closingDialog.setVisible(true);
         Point center = getLocationOnScreen();
         center.x += (getWidth() - closingDialog.getWidth()) / 2;
         center.y += (getHeight() - closingDialog.getHeight()) / 2;
         closingDialog.setLocation(center);
 
         exit.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 dispose();
             }
         });
 
         save.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 setAll();
                SettingsIO.writeSettings();
                 dispose();
             }
         });
     }
 }
