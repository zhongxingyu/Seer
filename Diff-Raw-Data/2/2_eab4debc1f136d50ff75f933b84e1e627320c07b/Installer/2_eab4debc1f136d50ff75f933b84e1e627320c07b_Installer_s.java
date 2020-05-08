 /*******************************************************************************
  * Copyright (c) 2013 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.e4.tools.event.spy;
 
 
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.di.annotations.Execute;
 import org.eclipse.e4.tools.event.spy.handlers.OpenSpyDialogHandler;
 import org.eclipse.e4.tools.event.spy.internal.util.LoggerWrapper;
 import org.eclipse.e4.tools.event.spy.internal.util.PluginUtils;
 import org.eclipse.e4.ui.model.application.MApplication;
 import org.eclipse.e4.ui.model.application.commands.MBindingTable;
 import org.eclipse.e4.ui.model.application.commands.MCommand;
 import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
 import org.eclipse.e4.ui.model.application.commands.MHandler;
 import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
 
 @SuppressWarnings("restriction")
 public class Installer {
 	@Inject
 	private MApplication application;
 
 	@Inject
 	private LoggerWrapper logger;
 
 	@Execute
 	public void execute() {
 		logger.info("installing ...");
 
 		registerCommand(CommandDescriptor.OpenSpyDialog, BindingDescriptor.OpenSpyDialogInDialogAndWindow, OpenSpyDialogHandler.class);
 
 		logger.info("installed");
 	}
 
 	private void registerCommand(CommandDescriptor commandDesc, BindingDescriptor bindingDesc, Class<?> handlerCls) {
 		MCommand command = getCommand(application, commandDesc);
 		if (command == null) {
 			command = createCommand(commandDesc);
 			application.getCommands().add(command);
 		}
 
 		Object binding = getBindingOrBindingTable(application, command, bindingDesc);
 		if (binding == null) {
 			logger.warn("binding context ''{0}'' for command ''{1}'' not found",
 					bindingDesc.getBindingContextId(), commandDesc.getName());
 			return;
 		}
 
 		if (binding instanceof MKeyBinding) {
 			MKeyBinding keyBinding = (MKeyBinding) binding;
 			if (keyBinding.getTags().contains(Constants.BINDING_MODIFIED_BY_USER_TAG)) {
 				logger.info("key binding for command ''{0}'' changed to {1}", commandDesc.getName(), keyBinding.getKeySequence());
 
 			} else if (keyBinding.getTags().contains(Constants.BINDING_DELETED_BY_USER_TAG)) {
 				logger.info("key binding for command ''{0}'' has been deleted. The command is disabled", commandDesc.getName());
 			}
 			return; //command already processed
 		}
 
 		((MBindingTable) binding).getBindings().add(createKeyBinding(command, bindingDesc));
 		logger.info("key binding for command ''{0}'' is {1}", commandDesc.getName(), bindingDesc.getKeySequence());
 
 		MHandler handler = getHandler(application, commandDesc);
 		if (handler == null) {
 			handler = createHandler(command, handlerCls);
 			application.getHandlers().add(handler);
 		}
 	}
 
 	private MCommand getCommand(MApplication application, CommandDescriptor descriptor) {
 		for (MCommand command: application.getCommands()) {
 			if (descriptor.getId().equals(command.getElementId())) {
 				return command;
 			}
 		}
 		return null;
 	}
 
 	private MCommand createCommand(CommandDescriptor descriptor) {
 		MCommand command = MCommandsFactory.INSTANCE.createCommand();
 		command.setElementId(descriptor.getId());
 		command.setCommandName(descriptor.getName());
 		command.setContributorURI(PluginUtils.getContributorURI());
 		return command;
 	}
 
 	private MHandler getHandler(MApplication application, CommandDescriptor descriptor) {
 		for (MHandler handler: application.getHandlers()) {
 			if (descriptor.getId().equals(handler.getElementId())) {
 				return handler;
 			}
 		}
 		return null;
 	}
 
 	private MHandler createHandler(MCommand command, Class<?> handlerCls) {
 		MHandler handler = MCommandsFactory.INSTANCE.createHandler();
 		handler.setElementId(command.getElementId());
 		handler.setCommand(command);
 		handler.setContributionURI(PluginUtils.getContributionURI(handlerCls));
 		handler.setContributorURI(PluginUtils.getContributorURI());
 		return handler;
 	}
 
 	private Object getBindingOrBindingTable(MApplication application, MCommand command, BindingDescriptor descriptor) {
 		MBindingTable result = null;
 		for (MBindingTable bindingTable: application.getBindingTables()) {
 			for (MKeyBinding keyBinding : bindingTable.getBindings()) {
 				if (keyBinding.getCommand() == command) {
					return bindingTable;
 				}
 			}
 			if (descriptor.getBindingContextId().equals(bindingTable.getBindingContext().getElementId())) {
 				result = bindingTable;
 			}
 		}
 		return result;
 	}
 
 	private MKeyBinding createKeyBinding(MCommand command, BindingDescriptor descriptor) {
 		MKeyBinding keyBinding = MCommandsFactory.INSTANCE.createKeyBinding();
 		keyBinding.setElementId(command.getElementId());
 		keyBinding.setCommand(command);
 		keyBinding.setKeySequence(descriptor.getKeySequence());
 		keyBinding.setContributorURI(PluginUtils.getContributorURI());
 		return keyBinding;
 	}
 }
