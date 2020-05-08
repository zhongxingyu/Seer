 /*******************************************************************************
  * Copyright 2011: Matthias Beste, Hannes Bischoff, Lisa Doerner, Victor Guettler, Markus Hattenbach, Tim Herzenstiel, Günter Hesse, Jochen Hülß, Daniel Krauth, Lukas Lochner, Mark Maltring, Sven Mayer, Benedikt Nees, Alexandre Pereira, Patrick Pfaff, Yannick Rödl, Denis Roster, Sebastian Schumacher, Norman Vogel, Simon Weber 
  *
  * Copyright 2010: Anna Aichinger, Damian Berle, Patrick Dahl, Lisa Engelmann, Patrick Groß, Irene Ihl, Timo Klein, Alena Lang, Miriam Leuthold, Lukas Maciolek, Patrick Maisel, Vito Masiello, Moritz Olf, Ruben Reichle, Alexander Rupp, Daniel Schäfer, Simon Waldraff, Matthias Wurdig, Andreas Wußler
  *
  * Copyright 2009: Manuel Bross, Simon Drees, Marco Hammel, Patrick Heinz, Marcel Hockenberger, Marcus Katzor, Edgar Kauz, Anton Kharitonov, Sarah Kuhn, Michael Löckelt, Heiko Metzger, Jacqueline Missikewitz, Marcel Mrose, Steffen Nees, Alexander Roth, Sebastian Scharfenberger, Carsten Scheunemann, Dave Schikora, Alexander Schmalzhaf, Florian Schultze, Klaus Thiele, Patrick Tietze, Robert Vollmer, Norman Weisenburger, Lars Zuckschwerdt
  *
  * Copyright 2008: Camil Bartetzko, Tobias Bierer, Lukas Bretschneider, Johannes Gilbert, Daniel Huser, Christopher Kurschat, Dominik Pfauntsch, Sandra Rath, Daniel Weber
  *
  * This program is free software: you can redistribute it and/or modify it un-der the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FIT-NESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *******************************************************************************/
 package org.bh.gui.swing;
 
import java.awt.Component;
 import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 
 import org.bh.BusinessHorizon;
 import org.bh.platform.IPlatformListener;
 import org.bh.platform.PlatformEvent;
 import org.bh.platform.PlatformKey;
 import org.bh.platform.Services;
 import org.bh.platform.PlatformEvent.Type;
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
  * @update Hesse.Guenter, 2011/12/12
  * @update Yannick Rödl, 27.12.2012
  * 
  */
 
 @SuppressWarnings("serial")
 public final class BHMenuBar extends JMenuBar implements IPlatformListener {
 
 	ITranslator translator = Services.getTranslator();
 	
 	private JMenu menuFile, menuProject, menuScenario, menuPeriod, menuOptions, menuHelp;
 	private BHMenuItem projectCreate, projectDuplicate, projectImport, projectExport, projectRemove,
 	scenarioCreate, scenarioDuplicate, scenarioRemove, periodCreate, periodDuplicate, periodRemove;
 	public static BHMenuItem filePrint, scenarioExport;
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
 		
 		// neue Funktion "Drucken" falls Auswertungen vorhanden sind, default = inaktiv
 		filePrint = new BHMenuItem(PlatformKey.FILEPRINT, 80);
 		filePrint.setEnabled(false);
 		menuFile.add(filePrint); // P
 
 		
 		// neue Funktion "Drucken" falls Auswertungen vorhanden sind, default = inaktiv
 		scenarioExport = new BHMenuItem(PlatformKey.FILESCEXP);
 		scenarioExport.setEnabled(false);
 		menuFile.add(scenarioExport); 
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
 		menuOptions.add(new BHMenuItem(PlatformKey.OPTIONSCHANGE, 123)); //F12
 		
 		
 		/**
 		 * create menu items --> help
 		 **/
 		menuHelp.add(new BHMenuItem(PlatformKey.HELPUSERHELP, 112)); //F1
 		menuHelp.add(new BHMenuItem(PlatformKey.HELPMATHHELP));
 		menuHelp.addSeparator();
 		if (BusinessHorizon.DEBUG) 
 			menuHelp.add(new BHMenuItem(PlatformKey.HELPDEBUG));
 		menuHelp.add(new BHMenuItem(PlatformKey.HELPINFO));
 		
 		if(Services.doesBranchSpecificRepresentativePluginExist()){
 			BHPopupFrame frame = (BHPopupFrame) Services.getPopUpFrame(BHPopupFrame.ID.MAINTAIN_COMPANIES.toString());
 			frame.setAdditionalMenuEntriesInMainFrame(this);
 		}
 		
 		Services.addPlatformListener(this);
 	}
 	
 	public JMenu add(JMenu component) {
 		super.add(component);
 		
		List<Component> menuEntries = Arrays.asList(super.getComponents());
		
		if(menuHelp != null && menuEntries.contains(menuHelp)) {
			remove(menuHelp);
 			super.add(menuHelp);
 		}
 		
 		return component;
 	}
 
 	
 	public void disableMenuProjectItems(){
 			projectDuplicate.setEnabled(false);
 			projectExport.setEnabled(false);
 			projectImport.setEnabled(false);
 			//projectRemove.setEnabled(true);
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
 	
 	/**
 	 * 02.12.2011, D. Roster
 	 * deactivate and activate the print function in menu
 	 */
 	
 	public static void enableFilePrint(){
 		filePrint.setEnabled(true);
 	}
 	
 	public static void disableFilePrint(){
 		filePrint.setEnabled(false);
 	}
 	
 	/**
 	 * @author Denis Roster
 	 * deactivate and activate the export function for scenario in menu
 	 */
 	
 	public static void enableSceExport(){
 		scenarioExport.setEnabled(true);
 	}
 	
 	public static void disableSceExport(){
 		scenarioExport.setEnabled(false);
 	}
 	
 
 	public void platformEvent(PlatformEvent e) {
 		if (e.getEventType() == Type.LOCALE_CHANGED) {
 			menuFile.setText(translator.translate("Mfile"));
 			menuFile.setMnemonic(translator.translate("Mfile", ITranslator.MNEMONIC).charAt(0));
 			
 			menuHelp.setText(translator.translate("Mhelp"));
 			menuFile.setMnemonic(translator.translate("Mhelp", ITranslator.MNEMONIC).charAt(0));
 			
 			menuOptions.setText(translator.translate("Moptions"));
 			menuFile.setMnemonic(translator.translate("Moptions", ITranslator.MNEMONIC).charAt(0));
 			
 			menuPeriod.setText(translator.translate("Mperiod"));
 			menuFile.setMnemonic(translator.translate("Mperiod", ITranslator.MNEMONIC).charAt(0));
 			
 			menuProject.setText(translator.translate("Mproject"));
 			menuFile.setMnemonic(translator.translate("Mproject", ITranslator.MNEMONIC).charAt(0));
 			
 			menuScenario.setText(translator.translate("Mscenario"));
 			menuFile.setMnemonic(translator.translate("Mscenario", ITranslator.MNEMONIC).charAt(0));
 		}
 	}
 }
