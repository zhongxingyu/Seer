 /*******************************************************************************
  * Copyright (c) 2012 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.ide.ui.editors.viewer;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.commands.CompoundCommand;
 import org.eclipse.wazaabi.ide.ui.editparts.commands.binding.InsertNewBindingCommand;
 import org.eclipse.wazaabi.mm.core.widgets.TextComponent;
 import org.eclipse.wazaabi.mm.edp.EventDispatcher;
 import org.eclipse.wazaabi.mm.edp.events.EDPEventsFactory;
 import org.eclipse.wazaabi.mm.edp.events.Event;
 import org.eclipse.wazaabi.mm.edp.events.PropertyChangedEvent;
 import org.eclipse.wazaabi.mm.edp.handlers.Binding;
 import org.eclipse.wazaabi.mm.edp.handlers.EDPHandlersFactory;
 import org.eclipse.wazaabi.mm.edp.handlers.EDPHandlersPackage;
 import org.eclipse.wazaabi.mm.edp.handlers.EventHandler;
 import org.eclipse.wazaabi.mm.edp.handlers.StringParameter;
 
 public class Transformer {
 
 	public Command getCommand(EObject sourceModel, EObject targetUI, int index) {
 		if (targetUI == null || sourceModel == null)
 			return null;
 
 		if (targetUI instanceof TextComponent) {
 			if (sourceModel instanceof EAttribute
 					&& ((EAttribute) sourceModel).getEType() == EcorePackage.Literals.ESTRING) {
 
 				EAttribute attr = (EAttribute) sourceModel;
 
 				Binding model2UIBinding = createBinding(
 						"$input/@" + attr.getName(), "@text");
 				addPropertyChangedEvent(model2UIBinding,
 						"$input/@" + attr.getName());
 
 				InsertNewBindingCommand model2UIBindingCommand = new InsertNewBindingCommand();
 				model2UIBindingCommand.setBinding(model2UIBinding);
 				model2UIBindingCommand.setIndex(0);
 				model2UIBindingCommand
 						.setEventDispatcher((EventDispatcher) targetUI);
 
 				Binding UI2ModelBinding = createBinding("@text", "$input/@"
 						+ attr.getName());
 				addEvent(UI2ModelBinding, "core:ui:focus:out");
 
 				InsertNewBindingCommand UI2ModelBindingCommand = new InsertNewBindingCommand();
 				UI2ModelBindingCommand.setBinding(UI2ModelBinding);
 				UI2ModelBindingCommand.setIndex(0);
 				UI2ModelBindingCommand
 						.setEventDispatcher((EventDispatcher) targetUI);
 
 				CompoundCommand cmd = new CompoundCommand();
 				cmd.add(model2UIBindingCommand);
 				cmd.add(UI2ModelBindingCommand);
 				return cmd;
 
 			}
 		}
 
 		return null;
 	}
 
 	protected Binding createBinding(String sourcePath, String targetPath) {
 		Binding model2UIBinding = EDPHandlersFactory.eINSTANCE.createBinding();
 		StringParameter source = EDPHandlersFactory.eINSTANCE
 				.createStringParameter();
 		source.setName("source");
 		StringParameter target = EDPHandlersFactory.eINSTANCE
 				.createStringParameter();
 		target.setName("target");
 		source.setValue(sourcePath);
 		target.setValue(targetPath);
 		model2UIBinding.getParameters().add(source);
 		model2UIBinding.getParameters().add(target);
 		return model2UIBinding;
 	}
 
 	protected void addPropertyChangedEvent(Binding binding, String path) {
 		PropertyChangedEvent event = EDPEventsFactory.eINSTANCE
 				.createPropertyChangedEvent();
 		event.setPath(path);
 		binding.getEvents().add(event);
 	}
 
 	protected void addEvent(Binding binding, String id) {
 		Event event = EDPEventsFactory.eINSTANCE.createEvent();
 		event.setId(id);
 		binding.getEvents().add(event);
 	}
 
 	protected List<Binding> getBindings(EObject sourceModel, EObject targetUI,
 			int index) {
 		List<Binding> bindings = new ArrayList<Binding>();
 		if (targetUI == null || sourceModel == null)
 			return Collections.emptyList();
 
 		if (targetUI instanceof TextComponent) {
 			if (sourceModel instanceof EAttribute
 					&& ((EAttribute) sourceModel).getEType() == EcorePackage.Literals.ESTRING) {
 
 				EAttribute attr = (EAttribute) sourceModel;
 
 				Binding model2UIBinding = createBinding(
 						"$input/@" + attr.getName(), "@text");
 				addPropertyChangedEvent(model2UIBinding,
 						"$input/@" + attr.getName());
 
 				InsertNewBindingCommand model2UIBindingCommand = new InsertNewBindingCommand();
 				model2UIBindingCommand.setBinding(model2UIBinding);
 				model2UIBindingCommand.setIndex(0);
 				model2UIBindingCommand
 						.setEventDispatcher((EventDispatcher) targetUI);
 
 				Binding UI2ModelBinding = createBinding("@text", "$input/@"
 						+ attr.getName());
 				addEvent(UI2ModelBinding, "core:ui:focus:out");
 
 				InsertNewBindingCommand UI2ModelBindingCommand = new InsertNewBindingCommand();
 				UI2ModelBindingCommand.setBinding(UI2ModelBinding);
 				UI2ModelBindingCommand.setIndex(0);
 				UI2ModelBindingCommand
 						.setEventDispatcher((EventDispatcher) targetUI);
 
 				CompoundCommand cmd = new CompoundCommand();
 				cmd.add(model2UIBindingCommand);
 				cmd.add(UI2ModelBindingCommand);
 //				return cmd;
 
 			}
 		}
 
 		return bindings;
 	}
 
 	public void remove(Binding binding) {
 
 	}
 
 	public void getEStringOnTextComponentEventHandlers() {
 
 	}
 
 	public void getEClassOnContainerComponents() {
 
 	}
 
 	@SuppressWarnings("unchecked")
 	protected List<EventHandler> getEventHandlers(EObject sourceModel,
 			EventDispatcher eventDispatcher, int index) {
 		if (sourceModel == null || eventDispatcher == null)
 			return Collections.emptyList();
 		FFactory myFactory = new FFactory();
 		return (List<EventHandler>) myFactory.get(eventDispatcher, index,
 				sourceModel, EDPHandlersPackage.Literals.EVENT_HANDLER, null);
 	}
 }
