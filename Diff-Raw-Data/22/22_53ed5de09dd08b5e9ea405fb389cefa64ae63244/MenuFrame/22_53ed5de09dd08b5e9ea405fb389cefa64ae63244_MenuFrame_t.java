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
 package cz.mgn.collabdesktop.menu;
 
 import cz.mgn.collabdesktop.menu.frames.about.About;
 import cz.mgn.collabdesktop.menu.frames.settings.SettingsFrame;
 import cz.mgn.collabdesktop.utils.CConstans;
 import cz.mgn.collabdesktop.utils.Utils;
 import cz.mgn.collabdesktop.utils.gui.GUIUtils;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
 /**
  *
  * @author Martin Indra <aktive@seznam.cz>
  */
 public abstract class MenuFrame extends JFrame {
 
     public MenuFrame() {
         setTitle("Collab - " + getSectionName());
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         Image icon = Toolkit.getDefaultToolkit().getImage(MenuFrame.class.getResource("/resources/images/icon-32.png"));
         setIconImage(icon);
         initComponents();
     }
 
     protected abstract String getSectionName();
 
     protected abstract void initComponents();
 
     protected void initMenuBar() {
         JMenuBar menuBar = new JMenuBar();
 
         JMenu file = new JMenu("File");
         menuBar.add(file);
         JMenu help = new JMenu("Help");
         menuBar.add(help);
 
         JMenuItem menuItem = new JMenuItem("Settings", KeyEvent.VK_S);
         menuItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 goTo(new SettingsFrame(), true);
             }
         });
         file.add(menuItem);
 
         menuItem = new JMenuItem("Online", KeyEvent.VK_O);
         menuItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 Utils.browse(CConstans.WEB_PAGE_MAIN);
             }
         });
         help.add(menuItem);
         menuItem = new JMenuItem("About", KeyEvent.VK_A);
         menuItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 About about = new About();
                 goTo(about, true);
             }
         });
         help.add(menuItem);
 
         setJMenuBar(menuBar);
     }
 
     protected void centerWindow() {
         GUIUtils.centerWindow(this);
     }
 
     public void setWindowCenterLocation(int cX, int cY) {
         int x = Math.max(0, cX - (getWidth() / 2));
         int y = Math.max(0, cY - (getHeight() / 2));
         setLocation(x, y);
     }
 
     public void afterGo() {
     }
 
     protected void goTo(MenuFrame menuFrame, boolean dialog) {
         int cX = getLocationOnScreen().x + (getWidth() / 2);
         int cY = getLocationOnScreen().y + (getHeight() / 2);
         if (dialog) {
             menuFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         } else {
             dispose();
         }
         menuFrame.setVisible(true);
        menuFrame.setWindowCenterLocation(cX, cY);
         menuFrame.afterGo();
     }
 }
