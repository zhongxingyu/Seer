 /*******************************************************************************
  *  Copyright (c) 2010 Weltevree Beheer BV, Remain Software & Industrial-TSI
  *                                                                      
  * All rights reserved. This program and the accompanying materials     
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at             
  * http://www.eclipse.org/legal/epl-v10.html                            
  *                                                                      
  * Contributors:                                                        
  *    Wim Jongman - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.ecf.salvo.application.internal;
 
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
 import org.eclipse.ui.application.ActionBarAdvisor;
 import org.eclipse.ui.application.IActionBarConfigurer;
 
 /**
  * An action bar advisor is responsible for creating, adding, and disposing of
  * the actions added to a workbench window. Each window will be populated with
  * new actions.
  */
 public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
 
 	// Actions - important to allocate these only in makeActions, and then use
 	// them
 	// in the fill methods. This ensures that the actions aren't recreated
 	// when fillActionBars is called with FILL_PROXY.
 	private IWorkbenchAction exitAction;
 	private IWorkbenchAction aboutAction;
 	private IWorkbenchAction preferenceAction;
 	private IWorkbenchAction perspectiveAction;
	private IWorkbenchAction showViewAction;
 
 	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
 		super(configurer);
 	}
 
 	@Override
 	protected void makeActions(final IWorkbenchWindow window) {
 		// Creates the actions and registers them.
 		// Registering is needed to ensure that key bindings work.
 		// The corresponding commands keybindings are defined in the plugin.xml
 		// file.
 		// Registering also provides automatic disposal of the actions when
 		// the window is closed.
 
 		exitAction = ActionFactory.QUIT.create(window);
 		register(exitAction);
 		aboutAction = ActionFactory.ABOUT.create(window);
 		register(aboutAction);
 		preferenceAction = ActionFactory.PREFERENCES.create(window);
 		register(preferenceAction);
 		perspectiveAction = ActionFactory.OPEN_PERSPECTIVE_DIALOG
 				.create(window);
 		register(perspectiveAction);
		showViewAction = ActionFactory.SHOW_VIEW_MENU
				.create(window);
		register(showViewAction);
 	}
 
 	@Override
 	protected void fillMenuBar(IMenuManager menuBar) {
 		MenuManager fileMenu = new MenuManager("&File",
 				IWorkbenchActionConstants.M_FILE);
 		menuBar.add(fileMenu);
 		fileMenu.add(aboutAction);
		fileMenu.add(showViewAction);
 		fileMenu.add(preferenceAction);
 		fileMenu.add(exitAction);
 		MenuManager helpMenu = new MenuManager("&Help",
 				IWorkbenchActionConstants.M_HELP);
 		menuBar.add(helpMenu);
 		helpMenu.add(perspectiveAction);
 	}
 
 }
