 package jDistsim.ui;
 
import jDistsim.ui.form.DesignerForm;

 import javax.swing.*;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 24.9.12
  * Time: 21:22
  */
 public class MenuBar extends JMenuBar {
 
    private DesignerForm designerForm;

     public MenuBar() {
        this(null);
     }
 
    public MenuBar(DesignerForm designerForm) {
        this.designerForm = designerForm;

         JMenu menu;
         JMenuItem menuItem;
 
         menu = new JMenu("File");
         menuItem = new JMenuItem("New model");
         menu.add(menuItem);
 
         add(menu);
     }
 }
