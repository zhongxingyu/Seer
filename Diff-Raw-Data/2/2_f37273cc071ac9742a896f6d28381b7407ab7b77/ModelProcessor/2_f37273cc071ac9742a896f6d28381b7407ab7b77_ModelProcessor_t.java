 /*******************************************************************************
  * Copyright (c) 2010 - 2013 BestSolution.at and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
  *     Lars Vogel <lars.vogel@gmail.com> - Bug 419723, 422644
  *     Markus A. Kuppe <bugs.eclipse.org@lemmster.de> - Bug 421259
  ******************************************************************************/
 package org.eclipse.e4.tools.emf.liveeditor;
 
 import java.util.List;
 
 import org.eclipse.e4.core.di.annotations.Execute;
 import org.eclipse.e4.ui.model.application.MApplication;
 import org.eclipse.e4.ui.model.application.commands.MBindingContext;
 import org.eclipse.e4.ui.model.application.commands.MBindingTable;
 import org.eclipse.e4.ui.model.application.commands.MCommand;
 import org.eclipse.e4.ui.model.application.commands.MHandler;
 import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
 import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
 import org.eclipse.e4.ui.workbench.modeling.EModelService;
 
 public class ModelProcessor {
 
 	private static final String E4_TOOLING_LIVEMODEL_HANDLER = "e4.tooling.livemodel.handler";
 	private static final String E4_TOOLING_LIVEMODEL = "e4.tooling.livemodel";
 
 	@Execute
 	public void process(MApplication application, EModelService modelService) {
 
 		MCommand command = null;
 		for (MCommand cmd : application.getCommands()) {
 			if (E4_TOOLING_LIVEMODEL.equals(cmd.getElementId())) {
 				command = cmd;
 			}
 		}
 		// List<MCommand> commands = modelService.findElements(application,
 		// E4_TOOLING_LIVEMODEL, MCommand.class, null);
 
 		if (command == null) {
 			command = modelService.createModelElement(MCommand.class);
 			command.setElementId(E4_TOOLING_LIVEMODEL);
			command.setCommandName("Show Live Application Model");
 			command.setDescription("Show the running application model");
 			application.getCommands().add(command);
 		}
 
 		MHandler handler = null;
 
 		for (MHandler hdl : application.getHandlers()) {
 			if (E4_TOOLING_LIVEMODEL_HANDLER.equals(hdl.getElementId())) {
 				handler = hdl;
 			}
 		}
 
 		if (handler == null) {
 			handler = modelService.createModelElement(MHandler.class);
 			handler.setElementId(E4_TOOLING_LIVEMODEL_HANDLER);
 			handler.setContributionURI("bundleclass://org.eclipse.e4.tools.emf.liveeditor/org.eclipse.e4.tools.emf.liveeditor.OpenLiveDialogHandler");
 			application.getHandlers().add(handler);
 		}
 
 		handler.setCommand(command);
 
 		MKeyBinding binding = null;
 
 		if (application.getBindingTables().size() <= 0) {
 			MBindingContext bc = null;
 			final List<MBindingContext> bindingContexts = application
 					.getBindingContexts();
 			if (bindingContexts.size() == 0) {
 				bc = modelService.createModelElement(MBindingContext.class);
 				bc.setElementId("org.eclipse.ui.contexts.window");
 			} else {
 				// Prefer org.eclipse.ui.contexts.dialogAndWindow but randomly
 				// select another one
 				// if org.eclipse.ui.contexts.dialogAndWindow cannot be found
 				for (MBindingContext aBindingContext : bindingContexts) {
 					bc = aBindingContext;
 					if ("org.eclipse.ui.contexts.dialogAndWindow"
 							.equals(aBindingContext.getElementId())) {
 						break;
 					}
 				}
 			}
 			MBindingTable bt = modelService
 					.createModelElement(MBindingTable.class);
 			bt.setElementId("e4.tooling.livemodel.bindingTable");
 			bt.setBindingContext(bc);
 			application.getBindingTables().add(bt);
 		}
 		List<MKeyBinding> keyBindings = modelService.findElements(application,
 				"e4.tooling.livemodel.binding", MKeyBinding.class, null);
 
 		if (keyBindings.size() == 0) {
 			binding = modelService.createModelElement(MKeyBinding.class);
 			binding.setElementId("e4.tooling.livemodel.binding");
 			binding.setKeySequence("M2+M3+F9");
 			if (application.getBindingTables().size() > 0) {
 				application.getBindingTables().get(0).getBindings()
 						.add(binding);
 			}
 		} else {
 			binding = keyBindings.get(0);
 		}
 		binding.setCommand(command);
 
 		MPartDescriptor descriptor = null;
 		List<MPartDescriptor> descriptors = modelService.findElements(
 				application, "org.eclipse.e4.tools.emf.liveeditor.view",
 				MPartDescriptor.class, null);
 
 		if (descriptors.size() == 0) {
 			descriptor = modelService.createModelElement(MPartDescriptor.class);
 			descriptor.setCategory("org.eclipse.e4.secondaryDataStack");
 			descriptor.setElementId("org.eclipse.e4.tools.emf.liveeditor.view");
 			descriptor.getTags().add("View");
 			descriptor.getTags().add("categoryTag:General");
 
 			descriptor.setLabel("Live Application Model");
 			descriptor
 					.setContributionURI("bundleclass://org.eclipse.e4.tools.emf.liveeditor/org.eclipse.e4.tools.emf.liveeditor.LivePartDelegator");
 			descriptor
 					.setContributorURI("bundleclass://org.eclipse.e4.tools.emf.liveeditor");
 			descriptor
 					.setIconURI("platform:/plugin/org.eclipse.e4.tools.emf.liveeditor/icons/full/obj16/application_lightning.png");
 			application.getDescriptors().add(descriptor);
 		}
 	}
 
 }
