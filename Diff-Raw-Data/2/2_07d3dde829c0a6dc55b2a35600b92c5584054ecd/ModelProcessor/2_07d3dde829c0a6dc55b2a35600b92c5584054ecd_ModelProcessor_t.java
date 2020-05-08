 /*******************************************************************************
  * Copyright (c) 2010 BestSolution.at and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
  ******************************************************************************/
 package org.eclipse.e4.tools.emf.liveeditor;
 
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.di.annotations.Execute;
 import org.eclipse.e4.ui.model.application.MApplication;
 import org.eclipse.e4.ui.model.application.commands.MCommand;
 import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
 import org.eclipse.e4.ui.model.application.commands.MHandler;
 import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
 import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
 import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
 import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
 import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
 import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
 
 public class ModelProcessor {
 	
 	@Execute
 	public void process(MApplication application) {
 		MCommand command = MCommandsFactory.INSTANCE.createCommand();
 		command.setElementId("e4.tooling.livemodel");
 		command.setCommandName("Show running app model");
 		command.setDescription("Show the running application model");
 		application.getCommands().add(command);
 		
 		MHandler handler = MCommandsFactory.INSTANCE.createHandler();
 		handler.setContributionURI("platform:/plugin/org.eclipse.e4.tools.emf.liveeditor/org.eclipse.e4.tools.emf.liveeditor.OpenLiveDialogHandler");
 		handler.setCommand(command);
 		application.getHandlers().add(handler);
 		
 		MKeyBinding binding = MCommandsFactory.INSTANCE.createKeyBinding();
		binding.setKeySequence("ALT+SHIFT+F4");
 		binding.setCommand(command);
 		if( application.getBindingTables().size() > 0 ) {
 			application.getBindingTables().get(0).getBindings().add(binding);	
 		}
 	}
 }
