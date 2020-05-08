 package org.bh.gui.swing;
 
 import java.awt.event.*;
 import javax.swing.*;
 import org.bh.platform.i18n.BHTranslator;
 import org.bh.plugin.gcc.DTOGCCBalanceSheet;
 
 
 /**
  * BHMenuBar to display a menu bar in screen.
  * 
  * <p>
  * This class extends the Swing <code>JMenuBar</code> to display a menu bar
  * on the screen.
  * 
  * @author Tietze.Patrick
  * @version 0.1, 2009/12/16
  * 
  */
 
 public class BHMenuBar extends JMenuBar implements ActionListener{
     
     private static final long serialVersionUID = 1L;
     BHTranslator translator = BHTranslator.getInstance(); 
 
     public BHMenuBar(){
         
     	/**
          * create the menu bar with all the items
          **/
 	
         //create menu --> File
        JMenu menuFile = new JMenu("File");//translator.translate("Mfile"));
         //menuFile.setMnemonic(KeyEvent.VK_D);
         add(menuFile);
         
         //create menu --> Project
        JMenu menuProject = new JMenu("Project");//;translator.translate("Mproject"));
         //menuProject.setMnemonic(KeyEvent.VK_P);
         add(menuProject);
     
         //create menu --> Scenario
        JMenu menuScenario = new JMenu("Scenario");//;translator.translate("Mscenario"));
         //menuScenario.setMnemonic(KeyEvent.VK_S);
         add(menuScenario);
         
         //create menu --> Bilanz & GuV
        JMenu menuBilanzGuV = new JMenu("Bilanz GuV");//translator.translate("MbalanceProfitLoss"));
         //menuBilanzGuV.setMnemonic(KeyEvent.VK_B);
         add(menuBilanzGuV);
         
         //create menu --> Options
        JMenu menuOptions = new JMenu("Options");//translator.translate("Moptions"));
         //menuOptions.setMnemonic(KeyEvent.VK_O);
         add(menuOptions);
         
         //create menu --> Help
        JMenu menuHelp = new JMenu("Help");//translator.translate("Mhelp"));
         //menuHelp.setMnemonic(KeyEvent.VK_H);
         add(menuHelp);
         
         /**
          * create menu items --> file
          **/
                 
         menuFile.add(new BHMenuItem("Mnew", 1, 12, "new"));
         menuFile.add(new BHMenuItem("Mopen", 1, 12, "open"));
         menuFile.add(new BHMenuItem("Mclose", 1, 12, "close"));
         menuFile.add(new BHMenuItem("Msave", 1, 12, "save"));
         menuFile.add(new BHMenuItem("Msaveas", 1, 12, "saveAs"));
         menuFile.add(new BHMenuItem("Mquit", 1, 12, "quit"));
            
        
         /**
          * create menu items --> project
          **/
         
         menuProject.add(new BHMenuItem("Mcreate", 1, 12, "create"));
         menuProject.add(new BHMenuItem("Mrename", 1, 12, "rename"));
         menuProject.add(new BHMenuItem("Mduplicate", 1, 12, "duplicate"));
         menuProject.add(new BHMenuItem("Mcreate", 1, 12, "create"));
         menuProject.add(new BHMenuItem("Mimport", 1, 12, "import"));
         menuProject.add(new BHMenuItem("Mexport", 1, 12, "export"));
         menuProject.add(new BHMenuItem("Mremove", 1, 12, "remove"));
           
         /**
          * create menu items --> scenario
          **/
         
         menuScenario.add(new BHMenuItem("Mcreate", 1, 12, "create"));
         menuScenario.add(new BHMenuItem("Mrename", 1, 12, "rename"));
         menuScenario.add(new BHMenuItem("Mduplicate", 1, 12, "duplicate"));
         menuScenario.add(new BHMenuItem("Mmove", 1, 12, "move"));
         menuScenario.add(new BHMenuItem("Mremove", 1, 12, "remove"));
       
         /**
          * create menu items --> Bilanz & GuV
          **/
         
         menuBilanzGuV.add(new BHMenuItem("Mshow", 1, 12, "show"));
         menuBilanzGuV.add(new BHMenuItem("Mcreate", 1, 12, "create"));
         menuBilanzGuV.add(new BHMenuItem("Mimport", 1, 12, "import"));
         menuBilanzGuV.add(new BHMenuItem("Medit", 1, 12, "edit"));
         menuBilanzGuV.add(new BHMenuItem("Mremove", 1, 12, "remove"));
               
         /**
          * create menu items --> options
          **/
       
         menuOptions.add(new BHMenuItem("Mchange", 1, 12, "change"));
         
         /**
          * create menu items --> options
          **/
       
         menuHelp.add(new BHMenuItem("MuserHelp", 1, 12, "userHelp"));
         menuHelp.add(new BHMenuItem("MmathHelp", 1, 12, "mathHelp"));
         menuHelp.add(new BHMenuItem("Minfo", 1, 12, "about"));
         
       
     }
     
 
     //ActionListener
     public void actionPerformed(ActionEvent e) {
 	if ("new".equals(e.getActionCommand())) {
             
         } else if ("open".equals(e.getActionCommand())){
         	
         } else {
             
         }
     }
 
 }
