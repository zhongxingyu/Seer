 /*******************************************************************************
  * Copyright (c) 2011 Manumitting Technologies, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Brian de Alwis (MT) - initial API and implementation
  *******************************************************************************/
 package org.eclipse.e4.tools.css.spy;
 
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.di.annotations.Execute;
 import org.eclipse.e4.ui.model.application.MApplication;
 import org.eclipse.e4.ui.model.application.commands.MBindingTable;
 import org.eclipse.e4.ui.model.application.commands.MCommand;
 import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
 import org.eclipse.e4.ui.model.application.commands.MHandler;
 import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
 
 public class SpyInstaller {
 	public static final String OPEN_SPY_COMMAND_ID = "org.eclipse.e4.css.OpenSpy";
 	private static final String SPY_HANDLER_ID = OpenSpyHandler.class.getName();
     private static final String SPY_HANDLER_URI = "bundleclass://org.eclipse.e4.tools.css.spy/"
 			+ OpenSpyHandler.class.getName();
 
 	public static final String OPEN_SCRATCHPAD_COMMAND_ID = "org.eclipse.e4.css.OpenSctachpad";
 	private static final String SCRATCHPAD_HANDLER_ID = OpenScratchpadHandler.class
 			.getName();
 	private static final String SCRATCHPAD_HANDLER_URI = "bundleclass://org.eclipse.e4.tools.css.spy/"
 			+ OpenScratchpadHandler.class.getName();
 
 	@Inject
 	protected MApplication app;
 
 	@Execute
 	public void execute() {
 		MCommand openSpyCommand = installCommand("Open CSS Spy",
 				OPEN_SPY_COMMAND_ID);
 		installHandler(openSpyCommand, SPY_HANDLER_ID, SPY_HANDLER_URI);
 		installBinding("org.eclipse.ui.contexts.dialogAndWindow",
 				openSpyCommand, "M2+M3+F4");
 
 		MCommand openScratchpadCommand = installCommand("Open CSS Scratchpad",
 				OPEN_SCRATCHPAD_COMMAND_ID);
 		installHandler(openScratchpadCommand, SCRATCHPAD_HANDLER_ID, SCRATCHPAD_HANDLER_URI);
 		installBinding("org.eclipse.ui.contexts.dialogAndWindow",
 				openScratchpadCommand, "M1+M2+M3+F4");
 	}
 
 	private MCommand installCommand(String label, String commandId) {
 		for(MCommand cmd : app.getCommands()) {
 			if (commandId.equals(cmd.getElementId())) {
 				System.err.println(cmd.getElementId()
 						+ ": command already installed");
 				return cmd;
 			}
 		}
 
 		MCommand cmd = MCommandsFactory.INSTANCE.createCommand();
 		cmd.setCommandName(label);
 		cmd.setElementId(commandId);
 		app.getCommands().add(cmd);
 		return cmd;
 	}
 
 	private MHandler installHandler(MCommand cmd, String handlerId,
 			String handlerURI) {
 		for(MHandler hdlr : app.getHandlers()) {
 			if (handlerId.equals(hdlr.getElementId())) {
 				System.err.println(handlerId + ": handler already installed");
 				return hdlr;
 			}
 		}
 
 		MHandler hdlr = MCommandsFactory.INSTANCE.createHandler();
 		hdlr.setElementId(handlerId);
 		hdlr.setContributionURI(handlerURI);
 		hdlr.setCommand(cmd);
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
 			for(MKeyBinding binding : table.getBindings()) {
 				if(binding.getCommand() == cmd) {
 					System.err.println(cmd.getElementId()
 							+ ": binding already installed as "
 							+ binding.getKeySequence());
 					return;
 				}
 			}
 			if (table.getBindingContext() != null
 					&& bindingContextId.equals(table.getBindingContext()
 							.getElementId())) {
 				bindingTable = table;
 			}
 		}
 
 		if(bindingTable == null) {
 			System.err.println("Cannot find table for binding context: "
 					+ bindingContextId);
 			return;
 		}
 
 		MKeyBinding binding = MCommandsFactory.INSTANCE.createKeyBinding();
 		binding.setCommand(cmd);
 		binding.setKeySequence(keySeq);
 		binding.setElementId("kb." + cmd.getElementId());
 		bindingTable.getBindings().add(binding);
 	}
 
 }
