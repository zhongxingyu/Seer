 /*******************************************************************************
  * Copyright (c) 2010 - 2013 BestSolution.at and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
  *     Lars Vogel <lars.vogel@gmail.com> - Bug 419723
  ******************************************************************************/
 package org.eclipse.e4.tools.emf.liveeditor;
 
 import java.util.List;
 
 import org.eclipse.e4.core.di.annotations.Execute;
 import org.eclipse.e4.ui.model.application.MApplication;
 import org.eclipse.e4.ui.model.application.commands.MCommand;
 import org.eclipse.e4.ui.model.application.commands.MHandler;
 import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
 import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
 import org.eclipse.e4.ui.workbench.modeling.EModelService;
 
 public class ModelProcessor {
 
 	@Execute
 	public void process(MApplication application, EModelService modelService) {
 		List<MCommand> commands = modelService.findElements(application,
 				"e4.tooling.livemodel", MCommand.class, null);
 
 		MCommand command = null;
 
 		if (commands.size() == 0) {
 			command = modelService.createModelElement(MCommand.class);
 			command.setElementId("e4.tooling.livemodel");
 			command.setCommandName("Show running app model");
 			command.setDescription("Show the running application model");
 			application.getCommands().add(command);
 		} else {
 			command = commands.get(0);
 		}
 
 		List<MHandler> handlers = modelService.findElements(application,
 				"e4.tooling.livemodel.handler", MHandler.class, null);
 
 		MHandler handler = null;
 
 		if (handlers.size() == 0) {
 			handler = modelService.createModelElement(MHandler.class);
 			handler.setElementId("e4.tooling.livemodel.handler");
 			handler.setContributionURI("bundleclass://org.eclipse.e4.tools.emf.liveeditor/org.eclipse.e4.tools.emf.liveeditor.OpenLiveDialogHandler");
 			application.getHandlers().add(handler);
 		} else {
 			handler = handlers.get(0);
 		}
 		handler.setCommand(command);
 
 		MKeyBinding binding = null;
 
 		if (application.getBindingTables().size() > 0) {
 			List<MKeyBinding> keyBindings = modelService.findElements(
 					application, "e4.tooling.livemodel.binding",
 					MKeyBinding.class, null);
 
 			if (keyBindings.size() == 0) {
 				binding = modelService.createModelElement(MKeyBinding.class);
 				binding.setElementId("e4.tooling.livemodel.binding");
 				binding.setKeySequence("ALT+SHIFT+F9");
 				if (application.getBindingTables().size() > 0) {
 					application.getBindingTables().get(0).getBindings()
 							.add(binding);
 				}
 			} else {
 				binding = keyBindings.get(0);
 			}
 			binding.setCommand(command);
 		}
 
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
