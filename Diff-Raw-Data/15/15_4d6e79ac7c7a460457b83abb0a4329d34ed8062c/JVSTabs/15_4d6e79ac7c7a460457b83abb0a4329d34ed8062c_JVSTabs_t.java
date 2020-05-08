 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.javascool.gui;
 
 import java.util.HashMap;
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import org.javascool.Utils;
 
 /**
  *
  * @author philien
  */
 public class JVSTabs extends JTabbedPane {
 
     protected HashMap<String, JPanel> tabs = new HashMap<String, JPanel>();
 
     public JVSTabs() {
         super();
     }
 
     public void add(String name, String icon, JPanel panel) {
         this.add(name,icon,panel,null);
     }
     
     public void add(String name, String icon, JPanel panel, String tooltip) {
         tabs.put(name, panel);
         if (!icon.equalsIgnoreCase("")) {
             ImageIcon logo = Utils.getIcon(icon);
             this.addTab(name, logo, panel,tooltip);
         }else{
             this.addTab(name, null, panel,tooltip);
         }
         this.revalidate();
     }
 
     public void del(String name) {
         this.removeTabAt(this.indexOfTab(name));
         tabs.remove(name);
     }
 
     public void switchToTab(String name) {
         this.setSelectedIndex(this.indexOfTab(name));
     }
 }
