 /*
 	Marabou Audio Tagger - A cross platform audio tagger using SWT
 	
 	Copyright (C) 2009  Jan-Hendrik Peters, Markus Herpich
 	
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 	      
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 	
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.github.marabou.gui;
 
 import static com.github.marabou.helper.I18nHelper._;
 import com.github.marabou.db.DBController;
 import com.github.marabou.db.GUINotConnectedException;
 import com.github.marabou.db.HSQLDBController;
 import com.github.marabou.helper.PropertiesAllowedKeys;
 import com.github.marabou.helper.PropertiesHelper;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 
 public class MainWindow {
 		
 		Display display = new Display();
 		Shell shell = new Shell(display);
 		Composite comp = new Composite(shell, SWT.NONE);
 		SashForm sashForm;
 		MainMenu menu;
 		DBController controller;
 		
 		/**
 		 * the main window holds elements such as the menu, the table,
 		 *  and the tabs on the left
 		 */
 		public MainWindow() {
 		shell.setLayout(new FillLayout(SWT.VERTICAL));
 		Image logo = new Image(display, "src/main/resources/graphics/marabou_16.png");
 		shell.setImage(logo);
 		shell.setText(_("Marabou - Audio tagger"));
 
 		// get Controller instance
 		controller = HSQLDBController.getInstance();
 		
 		//Create a menu, place it in the shell and fill the menu
 		menu = new MainMenu(shell);
 		menu.init();
 		shell.setMenuBar(menu.getMenu());
 
 		// the comp is a child of the shell which holds the toolbar
 		GridLayout gl = new GridLayout();
 		gl.marginHeight = 0;
 		gl.marginWidth = 0;
 		gl.horizontalSpacing = 0;
 		gl.verticalSpacing = 0;
 		comp.setLayout(gl);
 		GridData gd = new GridData(GridData.FILL_BOTH);
 		comp.setLayoutData(gd);
 		
 		// the upper toolbar below the menu
 		ToolBar toolbar = new ToolBar(comp, SWT.HORIZONTAL);
 		ToolItem ti = new ToolItem(toolbar, SWT.PUSH);
 		// TODO replace this with a call to an image load helper
 		ti.setImage(new Image(display, "src/main/resources/graphics/save.png"));
 		ti.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				try {
 					controller.saveSelectedFiles();
 				} catch (GUINotConnectedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		toolbar.pack();
 		
 		//Create a sashForm which will hold composites
 		sashForm = new SashForm(comp, SWT.HORIZONTAL);
 		sashForm.setLayoutData(gd);
 
 		// the toolbar at the bottom
 		ToolBar toolbar2 = new ToolBar(comp, SWT.HORIZONTAL);
 		ToolItem ti2 = new ToolItem(toolbar2, SWT.PUSH);
 		ti2.setText("bar");
 		toolbar2.pack();
 		
 		//Create the composites that will hold the table
 		//on the right side and the tabs on the left 
 		
 	    /* Left side */
 		
 	    Composite leftComp = new Composite(sashForm, SWT.NONE);
 		leftComp.setLayout(new FillLayout());
 		
 		// Tab folder
 		TabFolder tabFolder = new TabFolder(leftComp, SWT.NONE);
 		// link tabfolder to controller
 		controller.connectTabFolder(tabFolder);
 		
 		// Tab1 - Tags
 		TabItem item1 = new TabItem(tabFolder, SWT.NONE);
 		item1.setText(_("Tags"));
 		item1.setControl(TagTab.init(tabFolder));
 //		item1.addListener(SWT.MouseExit, new Listener() {
 //			public void handleEvent(final Event event) {
 //				//TODO update database and table
 //				try {
 //					controller.updateDBandTable();
 //				} catch (GUINotConnectedException e) {
 //					// TODO Auto-generated catch block
 //					e.printStackTrace();
 //				}
 //			}
 //			    
 //		});
 		
 		// Tab 2 - Filesystem
 		TabItem item2 = new TabItem(tabFolder, SWT.NONE);
 		item2.setText(_("Filesystem"));
 		item2.setControl(FilesystemTab.init(tabFolder));
 		//TODO add Listener to open files from tree
 		
 		/* Right side */
 		Composite rightComp = new Composite(sashForm, SWT.NONE);
 		rightComp.setLayout(new FillLayout());
 		
 		// Table
 		TableShell table = new TableShell(rightComp);
 		menu.setTableShell(table);
 		// link tableshell with controller
 		controller.connectTableShell(table);
 		
 		// Now we have the composites, and can set a ratio between them
 		// TODO make this 2/5 ratio storeable by the user
 		sashForm.setWeights(new int[]{2, 5});
 		sashForm.pack();
 	}
 	
 	/**
 	 * creates a new main window
 	 */
 	public void createGUI() {
 	
		// maximize on first run
 		shell.setMaximized(true);
 		if (Boolean.parseBoolean(PropertiesHelper.getProp(PropertiesAllowedKeys.saveWindowSize))) {
 			String x = PropertiesHelper.getProp(PropertiesAllowedKeys.windowSizeX);
 			String y = PropertiesHelper.getProp(PropertiesAllowedKeys.windowSizeY);
			if (x.equals("max") && y.equals("max")) {
			    shell.setMaximized(true);
			}
 			try {
 				int xInt = Integer.parseInt(x);
 				int yInt = Integer.parseInt(y);
 				shell.setSize(xInt, yInt);
			} catch (NumberFormatException e) {}
 		} else {
 			shell.setMaximized(true);
 		}
 		shell.open();
 		
 		// save window size on exit to open it in that size again
 		shell.addDisposeListener(new DisposeListener() {
		    @Override
		    public void widgetDisposed(DisposeEvent arg0) {
			if (Boolean.parseBoolean(PropertiesHelper.getProp(PropertiesAllowedKeys.saveWindowSize))) {
			    if (shell.getMaximized()) {
				PropertiesHelper.setProp(PropertiesAllowedKeys.windowSizeX, "max");
				PropertiesHelper.setProp(PropertiesAllowedKeys.windowSizeY, "max");
			    }
			    String x = String.valueOf(shell.getSize().x);
			    String y = String.valueOf(shell.getSize().y);
			    PropertiesHelper.setProp(PropertiesAllowedKeys.windowSizeX, x);
			    PropertiesHelper.setProp(PropertiesAllowedKeys.windowSizeY, y);
 			}
		    }
 		});
 		
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch())
 				display.sleep();
 		}
 		display.dispose();
 	}
 }
