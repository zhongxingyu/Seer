 package org.bh.gui.swing;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 
 import org.bh.BusinessHorizon;
 import org.bh.platform.PlatformKey;
 import org.bh.platform.Services;
 import org.bh.platform.i18n.ITranslator;
 
 /**
  * BHMenuBar to display a menu bar in screen.
  * 
  * <p>
  * This class extends the Swing <code>JMenuBar</code> to display a menu bar on
  * the screen.
  * 
  * To use the shortcut keys, you should use the constant field values shown on
  * http://java.sun.com/j2se/1.4.2/docs/api/constant-values.html. If no shortcut
  * is necessary use '0' as the key.
  * 
  * @author Tietze.Patrick
  * @version 0.1, 2009/12/16
  * 
  */
 
 public class BHMenuBar extends JMenuBar{
 
 	ITranslator translator = Services.getTranslator();
 	
 	private JMenu menuFile, menuProject, menuScenario, menuPeriod, menuOptions, menuHelp;
 	private BHMenuItem projectCreate, projectDuplicate, projectImport, projectExport, projectRemove,
 	scenarioCreate, scenarioDuplicate, scenarioRemove, periodCreate, periodDuplicate, periodRemove;
 	
 	public BHMenuBar() {
 
 		/**
 		 * create the menu bar with all the items
 		 **/
 
 		// create menu --> File
 		menuFile = new JMenu(translator.translate("Mfile"));
 		menuFile.setMnemonic(translator.translate("Mfile", ITranslator.MNEMONIC).charAt(0));
 		add(menuFile);
 
 		// create menu --> Project
 		menuProject = new JMenu(translator.translate("Mproject"));
 		menuProject.setMnemonic(translator.translate("Mproject", ITranslator.MNEMONIC).charAt(0));
 		add(menuProject);
 
 		// create menu --> Scenario
 		menuScenario = new JMenu(translator.translate("Mscenario"));
 		menuScenario.setMnemonic(translator.translate("Mscenario", ITranslator.MNEMONIC).charAt(0));
 		add(menuScenario);
 
 		// create menu --> Period
 		menuPeriod = new JMenu(translator.translate("Mperiod"));
 		menuPeriod.setMnemonic(translator.translate("Mperiod", ITranslator.MNEMONIC).charAt(0));
 		add(menuPeriod);
 
 		// create menu --> Options
 		menuOptions = new JMenu(translator.translate("Moptions"));
 		menuOptions.setMnemonic(translator.translate("Moptions", ITranslator.MNEMONIC).charAt(0));
 		add(menuOptions);
 
 		// create menu --> Help
 		menuHelp = new JMenu(translator.translate("Mhelp"));
 		menuHelp.setMnemonic(translator.translate("Mhelp", ITranslator.MNEMONIC).charAt(0));
 		add(menuHelp);
 		
 		/**
 		 * create menu items --> file
 		 **/
 		menuFile.add(new BHMenuItem(PlatformKey.FILENEW, 78)); //N
 		menuFile.add(new BHMenuItem(PlatformKey.FILEOPEN, 79)); // O
 		menuFile.add(new BHMenuItem(PlatformKey.FILESAVE, 83)); // S
 		menuFile.add(new BHMenuItem(PlatformKey.FILESAVEAS, 83)); // S
 		menuFile.addSeparator();
 		menuFile.add(new BHMenuItem(PlatformKey.FILECLOSE, 87)); // W
 		menuFile.add(new BHMenuItem(PlatformKey.FILEQUIT, 81));  // Q
 
 
 		/**
 		 * create menu items --> project
 		 **/
 		projectCreate = new BHMenuItem(PlatformKey.PROJECTCREATE, 114); //F5
 		menuProject.add(projectCreate);
 		projectDuplicate = new BHMenuItem(PlatformKey.PROJECTDUPLICATE);
 		menuProject.add(projectDuplicate);
 		menuProject.addSeparator();
 		projectImport = new BHMenuItem(PlatformKey.PROJECTIMPORT);
 		menuProject.add(projectImport);
 		projectExport = new BHMenuItem(PlatformKey.PROJECTEXPORT);
 		menuProject.add(projectExport);
 		menuProject.addSeparator();
 		projectRemove = new BHMenuItem(PlatformKey.PROJECTREMOVE);
 		menuProject.add(projectRemove);
 
 
 		/**
 		 * create menu items --> scenario
 		 **/
 		scenarioCreate = new BHMenuItem(PlatformKey.SCENARIOCREATE, 115); //F6
 		menuScenario.add(scenarioCreate);
 		scenarioDuplicate = new BHMenuItem(PlatformKey.SCENARIODUPLICATE);
 		menuScenario.add(scenarioDuplicate);
 		menuScenario.addSeparator();
 		scenarioRemove = new BHMenuItem(PlatformKey.SCENARIOREMOVE);
 		menuScenario.add(scenarioRemove);
 
 		/**
 		 * create menu items --> period
 		 **/
 		periodCreate = new BHMenuItem(PlatformKey.PERIODCREATE, 116); //F7
 		menuPeriod.add(periodCreate);
 		periodDuplicate = new BHMenuItem(PlatformKey.PERIODDUPLICATE);
 		menuPeriod.add(periodDuplicate);
 		menuPeriod.addSeparator();
 		periodRemove = new BHMenuItem(PlatformKey.PERIODREMOVE);
 		menuPeriod.add(periodRemove);
 		
 		
 		/**
 		 * create menu items --> options
 		 **/
 		menuOptions.add(new BHMenuItem(PlatformKey.OPTIONSCHANGE, 80));
 		
 		
 		/**
 		 * create menu items --> help
 		 **/
 		menuHelp.add(new BHMenuItem(PlatformKey.HELPUSERHELP, 112)); //F1
 		menuHelp.add(new BHMenuItem(PlatformKey.HELPMATHHELP));
 		menuHelp.addSeparator();
 		if (BusinessHorizon.DEBUG) 
 			menuHelp.add(new BHMenuItem(PlatformKey.HELPDEBUG));
 		menuHelp.add(new BHMenuItem(PlatformKey.HELPINFO));
 	}
 	
 	public void disableMenuProjectItems(){
 		projectDuplicate.setEnabled(false);
 		projectExport.setEnabled(false);
 		projectImport.setEnabled(false);
		projectRemove.setEnabled(false);
 	}
 	
 	public void enableMenuProjectItems(){
 		projectDuplicate.setEnabled(true);
 		projectExport.setEnabled(true);
 		projectImport.setEnabled(true);
 		projectRemove.setEnabled(true);
 	}
 	
 	public void disableMenuScenarioItems(){
 	    	scenarioCreate.setEnabled(true);
 	    	scenarioDuplicate.setEnabled(false);
 	    	scenarioRemove.setEnabled(false);
 	}
 	
 	public void disableMenuScenarioAllItems(){
 	    	scenarioCreate.setEnabled(false);
 	    	scenarioDuplicate.setEnabled(false);
 	    	scenarioRemove.setEnabled(false);
 	}
 	
 	public void enableMenuScenarioItems(){
 	    	scenarioCreate.setEnabled(true);
 	    	scenarioDuplicate.setEnabled(true);
 	    	scenarioRemove.setEnabled(true);
 	}
 	
 	public void disableMenuPeriodItems(){
 	    	periodCreate.setEnabled(true);
 	    	periodDuplicate.setEnabled(false);
 	    	periodRemove.setEnabled(false);
 	}
 	
 	public void disableMenuPeriodAllItems(){
 	    	periodCreate.setEnabled(false);
 	    	periodDuplicate.setEnabled(false);
 	    	periodRemove.setEnabled(false);
 	}
 	
 	public void enableMenuPeriodItems(){
 	    	periodCreate.setEnabled(true);
 	    	periodDuplicate.setEnabled(true);
 	    	periodRemove.setEnabled(true);
 	}
 }
