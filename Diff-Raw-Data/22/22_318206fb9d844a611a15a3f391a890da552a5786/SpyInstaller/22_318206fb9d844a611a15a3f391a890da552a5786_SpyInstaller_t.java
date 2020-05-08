 /*******************************************************************************
 * Copyright (c) 2011,2014 Manumitting Technologies, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Brian de Alwis (MT) - initial API and implementation
 *     Lars Vogel <lars.vogel@gmail.com> - Bug 427451
  *******************************************************************************/
 package org.eclipse.e4.tools.css.spy;
 
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.di.annotations.Execute;
 import org.eclipse.e4.core.di.annotations.Optional;
 import org.eclipse.e4.core.services.log.Logger;
 import org.eclipse.e4.ui.bindings.EBindingService;
 import org.eclipse.e4.ui.model.application.MApplication;
 import org.eclipse.e4.ui.model.application.commands.MBindingTable;
 import org.eclipse.e4.ui.model.application.commands.MCommand;
 import org.eclipse.e4.ui.model.application.commands.MHandler;
 import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
 
 public class SpyInstaller {
 	private static final String BUNDLE_ID = "org.eclipse.e4.tools.css.spy";
 	public static final String OPEN_SPY_COMMAND_ID = "org.eclipse.e4.css.OpenSpy";
 	private static final String SPY_HANDLER_ID = OpenSpyHandler.class.getName();
 	private static final String SPY_HANDLER_URI = "bundleclass://" + BUNDLE_ID
 			+ "/"
 			+ OpenSpyHandler.class.getName();
 
 	public static final String OPEN_SCRATCHPAD_COMMAND_ID = "org.eclipse.e4.css.OpenSctachpad";
 	private static final String SCRATCHPAD_HANDLER_ID = OpenScratchpadHandler.class
 			.getName();
 	private static final String SCRATCHPAD_HANDLER_URI = "bundleclass://"
 			+ BUNDLE_ID + "/"
 			+ OpenScratchpadHandler.class.getName();
 	private static final String CONTRIBUTOR_URI = "platform:/plugin/"
 			+ BUNDLE_ID;
 
 	@Inject
 	protected MApplication app;
 
 	@Inject
	EModelService modelService;

	@Inject
 	@Optional
 	protected Logger logger;
 
 	@Execute
 	public void execute() {
 		// rectify situation introduced by bug 376475
 		removeBindingTable("bt.org.eclipse.e4.css.OpenSpy");
 
 		MCommand openSpyCommand = installCommand("Open CSS Spy",
 				OPEN_SPY_COMMAND_ID);
 		installHandler(openSpyCommand, SPY_HANDLER_ID, SPY_HANDLER_URI);
 
 		// M1 = Control or Cmd on MacOS X
 		// M2 = Shift
 		// M3 = Alt
 		// M4 = Control on MacOS X, Command on others
 		installBinding("org.eclipse.ui.contexts.dialogAndWindow",
 				openSpyCommand, "M2+M3+F5");
 
 		MCommand openScratchpadCommand = installCommand("Open CSS Scratchpad",
 				OPEN_SCRATCHPAD_COMMAND_ID);
 		installHandler(openScratchpadCommand, SCRATCHPAD_HANDLER_ID, SCRATCHPAD_HANDLER_URI);
 		installBinding("org.eclipse.ui.contexts.dialogAndWindow",
 				openScratchpadCommand, "M2+M3+F6");
 	}
 
 	private void removeBindingTable(String tableId) {
 		for (MBindingTable table : app.getBindingTables()) {
 			if (tableId.equals(table.getElementId())) {
 				app.getBindingTables().remove(table);
 				return;
 			}
 		}
 	}
 
 	private MCommand installCommand(String label, String commandId) {
 		for(MCommand cmd : app.getCommands()) {
 			if (commandId.equals(cmd.getElementId())) {
 				return cmd;
 			}
 		}
 
		MCommand cmd = modelService.createModelElement(MCommand.class);
 		cmd.setCommandName(label);
 		cmd.setElementId(commandId);
 		cmd.setContributorURI(CONTRIBUTOR_URI);
 		app.getCommands().add(cmd);
 		return cmd;
 	}
 
 	private MHandler installHandler(MCommand cmd, String handlerId,
 			String handlerURI) {
 		for(MHandler hdlr : app.getHandlers()) {
 			if (handlerId.equals(hdlr.getElementId())) {
 				return hdlr;
 			}
 		}
 
		MHandler hdlr = modelService.createModelElement(MHandler.class);
 		hdlr.setElementId(handlerId);
 		hdlr.setContributionURI(handlerURI);
 		hdlr.setCommand(cmd);
 		hdlr.setContributorURI(CONTRIBUTOR_URI);
 		app.getHandlers().add(hdlr);
 		return hdlr;
 	}
 
 	private void installBinding(String bindingContextId, MCommand cmd,
 			String keySeq) {
 		// there is a one-to-one mapping between binding contexts and
 		// binding tables, though binding tables may not necessarily
 		// guaranteed an element id.
 		MBindingTable bindingTable = null;
 		for(MBindingTable table : app.getBindingTables()) {
 			// check if the binding was a user-defined binding, otherwise remove
 			for (MKeyBinding binding : table.getBindings()) {
 				if (binding.getCommand() == cmd) {
 					// if explicitly set or unbound by the user...
 					if (binding.getTags().contains(
 							EBindingService.TYPE_ATTR_TAG + ":user")) {
 						logInfo("Found user-remapped binding for {0} to {1}: not rebinding",
 								cmd.getElementId(), binding.getKeySequence());
 						return;
 					} else if (binding.getTags().contains(
 									EBindingService.DELETED_BINDING_TAG)) {
 						logInfo("User deleted binding for {0}: not rebinding",
 								cmd.getElementId());
 						return;
 					}
 					logInfo("Removing existing binding for {0} to {1}",
 							cmd.getElementId(), binding.getKeySequence());
 					table.getBindings().remove(binding);
 					break;
 				}
 			}
 			if (table.getBindingContext() != null
 					&& bindingContextId.equals(table.getBindingContext()
 							.getElementId())) {
 				bindingTable = table;
 			}
 		}
 
 		if(bindingTable == null) {
 			logError("Cannot find table for binding context: {0}",
 					bindingContextId);
 			return;
 		}
 
		MKeyBinding binding = modelService
				.createModelElement(MKeyBinding.class);
 		binding.setCommand(cmd);
 		binding.setKeySequence(keySeq);
 		binding.setElementId("kb." + cmd.getElementId());
 		binding.setContributorURI(CONTRIBUTOR_URI);
 		bindingTable.getBindings().add(binding);
 	}
 
 	private void logInfo(String message, Object... args) {
 		if (logger == null) {
 			return;
 		}
 		logger.info(message, args);
 	}
 
 	private void logError(String message, Object... args) {
 		if (logger == null) {
 			return;
 		}
 		logger.error(message, args);
 	}
 
 }
