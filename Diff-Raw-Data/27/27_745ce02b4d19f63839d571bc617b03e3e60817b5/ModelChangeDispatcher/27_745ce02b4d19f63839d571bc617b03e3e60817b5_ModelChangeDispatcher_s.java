 /**
  * Copyright (C) 2014  Luc Hermans
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program.  If
  * not, see <http://www.gnu.org/licenses/>.
  * 
  * Contact information: kozzeluc@gmail.com.
  */
 package org.lh.dmlj.schema.editor.command.infrastructure;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.commands.CommandStack;
 import org.eclipse.gef.commands.CommandStackEvent;
 import org.eclipse.gef.commands.CompoundCommand;
 import org.lh.dmlj.schema.Schema;
 import org.lh.dmlj.schema.editor.Plugin;
 import org.lh.dmlj.schema.editor.command.IModelChangeCommand;
 import org.lh.dmlj.schema.editor.command.ModelChangeBasicCommand;
 import org.lh.dmlj.schema.editor.command.ModelChangeCompoundCommand;
 import org.lh.dmlj.schema.editor.command.annotation.Features;
 import org.lh.dmlj.schema.editor.command.annotation.Item;
 import org.lh.dmlj.schema.editor.command.annotation.ModelChange;
 import org.lh.dmlj.schema.editor.command.annotation.ModelChangeCategory;
 import org.lh.dmlj.schema.editor.command.annotation.Owner;
 import org.lh.dmlj.schema.editor.command.annotation.OwnerType;
 import org.lh.dmlj.schema.editor.command.annotation.Reference;
 
 public class ModelChangeDispatcher implements IModelChangeProvider {
 	
 	public enum Availability {MANDATORY, OPTIONAL};	
 	
 	private boolean	dispatching = false;
 	private boolean disposed = false;
 	private List<IModelChangeListener> listeners = new ArrayList<>();
 	private Set<IModelChangeListener> obsoleteListeners = new HashSet<>();
 	private Schema schema;
 	
 	// things to carry from a pre-change to a post-change event:
 	private Command previousCommand;
 	private List<IModelChangeListener> allListeners;
 	private Map<Integer, Object> listenerDataMap;
 	
 	private static <T extends Annotation> Field getAnnotatedField(
 		Object object, 
 		Class<T> annotationType) {	
 		
 		Assert.isNotNull(object, "object is null");
 		Assert.isNotNull(annotationType, "annotationType is null");
 		
 		List<Field> annotatedFields = getAnnotatedFields(object, annotationType);
 		Assert.isTrue(annotatedFields.size() < 2, "more than 1 @" + annotationType.getSimpleName() + 
 					  " found: " + object.getClass().getName());
 		
 		if (!annotatedFields.isEmpty()) {
 			return annotatedFields.get(0);
 		} else {
 			return null;
 		}
 		
 	}
 	
 	private static <T extends Annotation, U> Field getAnnotatedField(
 		Object object, 
 		Class<T> annotationType,
 		String element, 
 		U elementValue) {	
 		
 		Assert.isNotNull(object, "object is null");
 		Assert.isNotNull(annotationType, "annotationType is null");
 		Assert.isNotNull(element, "element is null");
 		// elementValue is allowed to be null
 		
 		List<Field> annotatedFields = 
 			getAnnotatedFields(object, annotationType, element, elementValue);
 		Assert.isTrue(annotatedFields.size() < 2, "more than 1 @" + annotationType.getSimpleName() + 
 					  "(" + element + "=" + elementValue + ") found: " + 
 					  object.getClass().getName());
 		
 		if (!annotatedFields.isEmpty()) {
 			return annotatedFields.get(0);
 		} else {
 			return null;
 		}
 		
 	}	
 	
 	private static <T extends Annotation> List<Field> getAnnotatedFields(
 		Object object, 
 		Class<T> annotationType) {
 		
 		Assert.isNotNull(object, "object is null");
 		Assert.isNotNull(annotationType, "annotationType is null");
 		
 		List<Field> annotatedFields = new ArrayList<>();
 		for (Field aField : object.getClass().getDeclaredFields()) {
 			T annotation = aField.getAnnotation(annotationType);
 			if (annotation != null) {
 				annotatedFields.add(aField);				
 			}
 		}
 		return annotatedFields;
 		
 	}	
 	
 	@SuppressWarnings("unchecked")
 	private static <T extends Annotation, U> List<Field> getAnnotatedFields(
 		Object object, 
 		Class<T> annotationType,
 		String element, 
 		U elementValue) {
 			
 		Assert.isNotNull(object, "object is null");
 		Assert.isNotNull(annotationType, "annotationType is null");
 		Assert.isNotNull(element, "element is null");
 		// elementValue is allowed to be null
 		
 		Method method;
 		try {
 			method = annotationType.getMethod(element, new Class<?>[] {});			
 		} catch (NoSuchMethodException | SecurityException e) {
 			throw new RuntimeException("cannot locate element '" + element + "' in @" + 
 									   annotationType.getSimpleName(), e);
 		}
 		
 		List<Field> annotatedFields = getAnnotatedFields(object, annotationType);
 		List<Field> filteredAnnotatedFields = new ArrayList<>();
 		for (Field annotatedField : annotatedFields) {			
 			try {	
 				T annotation = annotatedField.getAnnotation(annotationType); 
 				U annotatedFieldsElementValue = 
 					(U) method.invoke(annotation, new Object[] {}); // no ClassCastException !? 
 				if (elementValue == null && annotatedFieldsElementValue == null ||
 					elementValue != null && elementValue.equals(annotatedFieldsElementValue)) {
 					
 					filteredAnnotatedFields.add(annotatedField);
 				}
 			} catch (IllegalAccessException | IllegalArgumentException | 
 					 InvocationTargetException e) {
 				
 				throw new RuntimeException("cannot access element '" + element + "' of @" + 
 						   				   annotationType.getSimpleName() + " on field " +
 						   				   annotatedField.getName() + ": " + 
 						   				   object.getClass().getName(), e);
 			}
 		}
 		return filteredAnnotatedFields;
 		
 	}	
 	
 	@SuppressWarnings("unchecked")
 	public static <T, U extends Annotation> T getAnnotatedFieldValue(
 		Object object, 
 		Class<U> annotationType, 
 		Availability availability) {		
 		
 		Assert.isNotNull(object, "object is null");
 		Assert.isNotNull(annotationType, "annotationType is null");
 		
 		Field annotatedField = getAnnotatedField(object, annotationType);
 		if (availability == Availability.MANDATORY) {
 			Assert.isNotNull(annotatedField, "an @" + annotationType.getSimpleName() + 
 							 " is mandatory: " + object.getClass().getName());
 		}
 		
 		if (annotatedField != null) {
 			annotatedField.setAccessible(true);
 			T value;
 			try {
 				value = (T) annotatedField.get(object);
 			} catch (IllegalArgumentException | IllegalAccessException e) {
 				throw new RuntimeException("cannot get value for field annotated with @" + 
 										   annotationType.getSimpleName() + ": " + 
 										   object.getClass().getName(), e);
 			}
 			return value;
 		} else {
 			return null;
 		}
 		
 	}
 	
 	private static CommandExecutionMode getCommandExecutionMode(CommandStackEvent event) {
 		if (event.getDetail() == CommandStack.PRE_EXECUTE || 
 			event.getDetail() == CommandStack.POST_EXECUTE) {
 			
 			return CommandExecutionMode.EXECUTE;
 		} else if (event.getDetail() == CommandStack.PRE_UNDO ||
 				   event.getDetail() == CommandStack.POST_UNDO) {
 			
 			return CommandExecutionMode.UNDO;
 		} else if (event.getDetail() == CommandStack.PRE_REDO ||
 				   event.getDetail() == CommandStack.POST_REDO) {
 			
 			return CommandExecutionMode.REDO;
 		}
 		throw new IllegalArgumentException("cannot derive command execution mode: " + 
 										   event.getDetail()); 
 	}
 
 	private static String getEventDetailDescription(int eventDetail) {
 		if (eventDetail == CommandStack.PRE_EXECUTE) {
 			return "PRE_EXECUTE";		
 		} if (eventDetail == CommandStack.POST_EXECUTE) {
 			return "POST_EXECUTE";
 		} else if (eventDetail == CommandStack.PRE_REDO) {
 			return "PRE_REDO";
 		} else if (eventDetail == CommandStack.POST_REDO) {
 			return "POST_REDO";
 		} else if (eventDetail == CommandStack.PRE_UNDO) {
 			return "PRE_UNDO";	
 		} else if (eventDetail == CommandStack.POST_UNDO) {
 			return "POST_UNDO";	
 		}
 		return "<unexpected value>";
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static <T, U extends Annotation, V> T getAnnotatedFieldValue(
 		Object object, 
 		Class<U> annotationType,
 		String element, 
 		V elementValue, 
 		Availability availability) {		
 		
 		Field annotatedField = getAnnotatedField(object, annotationType, element, elementValue);		
 		if (availability == Availability.MANDATORY) {
 			Assert.isNotNull(annotatedField, "an @" + annotationType.getSimpleName() + "(" + 
 							 element + "=" + elementValue + ") is mandatory: " + 
 							 object.getClass().getName());
 		}
 				
 		if (annotatedField != null) {
 			annotatedField.setAccessible(true);
 			T value;
 			try {
 				value = (T)annotatedField.get(object);
 			} catch (IllegalArgumentException | IllegalAccessException e) {
 				throw new RuntimeException("cannot get value for field annotated with @" + 
 										   annotationType.getSimpleName() + "(" + element + "=" +
 										   elementValue + "): " + object.getClass().getName(), e);
 			}
 			return value;
 		} else {
 			return null;
 		}
 		
 	}
 
 	public ModelChangeDispatcher() {
 		super();
 	}
 	
 	public void addModelChangeListener(IModelChangeListener listener) {
 		Assert.isTrue(!isDisposed(), "model change dispatcher is disposed");
 		if (!listeners.contains(listener)) {
 			// avoid notifying the same listener twice
 			listeners.add(listener);
 			// write a debug message to the log...
 			StringBuilder debugMessage = new StringBuilder();
 			debugMessage.append("Added model change dispatch listener:" + "\n");
 			debugMessage.append("Listener class: " + listener.getClass().getName() + "\n");			
 			if (listener instanceof EditPart) {
 				EditPart editPart = (EditPart) listener;
 				debugMessage.append("Edit part model: " + (editPart).getModel());
 				debugMessage.append("\n");
 				if (editPart.getParent() != null) {
 					EditPart parent = editPart.getParent();
 					debugMessage.append("Parent edit part class: " + parent.getClass().getName() + "\n");
 					debugMessage.append("Parent edit part model: " + parent.getModel() + "\n");	
 					if (parent.getParent() != null) {
 						EditPart grandParent = parent.getParent();
 						debugMessage.append("Grand parent edit part class: " + 
 											grandParent.getClass().getName() + "\n");
 						debugMessage.append("Grand parent edit part model: " + grandParent.getModel() + 
 											"\n");				
 					} else {
 						debugMessage.append("Grand parent edit part: null\n");
 					}
 				} else {
 					debugMessage.append("Parent edit part: null\n");
 				}
 			}
 			debugMessage.append("The listener count is now " + listeners.size());			
 			logDebug(debugMessage.toString());			
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void dispatch(CommandStackEvent event) {		
 		
 		Assert.isTrue(!isDisposed(), "model change dispatcher is disposed");
 		
 		// log every event when in debug mode
 		StringBuilder debugMessage = new StringBuilder();
 		debugMessage.append("****************************** CommandStackEvent: " +
 							"******************************\n");
 		debugMessage.append("Command class: " + event.getCommand().getClass().getName() + "\n");
 		debugMessage.append("Event detail: " + getEventDetailDescription(event.getDetail()) + " (" + 
 	 						event.getDetail() + ")");
 		logDebug(debugMessage.toString());
 		
 		// we don't expect to be in dispatching mode
 		Assert.isTrue(!dispatching, "already dispatching; check for previous exceptions");		
 		
 		// we need to extract the model change context from the command that was put on the command
 		// stack
 		ModelChangeContext context;		
 		
 		// the event's command is possibly a compound command - build a list of commands to process		
 		Command eventCommand = event.getCommand();		
 		List<Command> commands = new ArrayList<>();
 		if (eventCommand instanceof CompoundCommand) {
 			// the event command is a compound command
 			CompoundCommand compoundCommand = (CompoundCommand) eventCommand;
 			List<Command> compoundCommandCommands = compoundCommand.getCommands();			
 			if (compoundCommandCommands.size() == 1 &&
 				compoundCommandCommands.get(0) instanceof ModelChangeCompoundCommand) {
 				
 				// in some cases, compound commands that we create are wrapped themselves in a
 				// compound command, so make sure we can handle this situation
 				ModelChangeCompoundCommand wrappedCompoundCommand = 
 					(ModelChangeCompoundCommand) compoundCommandCommands.get(0);
 				// ignore non model change commands
 				if (!(wrappedCompoundCommand instanceof IModelChangeCommand)) {
 					return;
 				}
 				compoundCommandCommands = wrappedCompoundCommand.getCommands();
 				context = wrappedCompoundCommand.getContext();
 			} else if (compoundCommandCommands.size() == 1 &&
 					   compoundCommandCommands.get(0) instanceof ModelChangeBasicCommand) {
 				
 				// in some cases, basic commands that we create are wrapped themselves in a compound 
 				// command, so make sure we can handle this situation
 				ModelChangeBasicCommand wrappedBasicCommand = 
 					(ModelChangeBasicCommand) compoundCommandCommands.get(0);
 				compoundCommandCommands = new ArrayList<>();
 				compoundCommandCommands.add(wrappedBasicCommand);
 				context = wrappedBasicCommand.getContext();
 			} else {
 				// ignore non model change commands
 				if (!(eventCommand instanceof IModelChangeCommand)) {
 					return;
 				}
 				context = ((IModelChangeCommand) eventCommand).getContext();
 			}
 			if (isExecuteOrRedo(event.getDetail())) {
 				// add the individual commands in the order they are listed in the compound command,
 				// listeners should be aware that the model contains the situation after undoing all
 				// the commands contained in the compound command				
 				commands.addAll(compoundCommandCommands);
 			} else {
 				// in the case we're undoing a command, add the individual commands in reverse 
 				// order, that way we follow the order in which the commands were undone BUT 
 				// listeners should be aware that the model contains the situation after undoing all
 				// the commands contained in the compound command
 				for (int i = compoundCommandCommands.size() - 1; i > -1; i--) {
 					commands.add(compoundCommandCommands.get(i));
 				}
 			}
 			// log the commands contained in the compound command
 			debugMessage = new StringBuilder();
 				debugMessage.append("Compound command classes (in the order in which they will " +
 									"be dispatched):\n");
 			
 			for (int i = 0; i < commands.size(); i++) {
 				if (i > 0) {
 					debugMessage.append("\n");				
 				}
 				debugMessage.append("[" + i + "] " + commands.get(i).getClass().getName() + 
 									"(label='" + commands.get(i).getLabel() + "')");				
 			}
 			logDebug(debugMessage.toString());			
 		} else {
 			// the event command is a basic command; ignore non model change commands
 			if (!(eventCommand instanceof IModelChangeCommand)) {
 				return;
 			} 
 			commands.add(eventCommand);
 			context = ((IModelChangeCommand) eventCommand).getContext();
 		}
 		
 		// make sure the context's schema is set
 		if (context != null) {
 			// the schema should be set, but we do NOT enforce it; just copy whatever we have or not
 			// to the context
 			context.setSchema(getSchema());
 		}
 		
 		// in the case of a pre-change event, (only) invoke the beforeModelChangeListener(context) 
 		// method on all current listeners and set aside the listener data; if no context is set, 
 		// bypass this step (note that we should avoid this situation but are allowing it to be able 
 		// to move gradually to the new dispatching methods				
 		if (context != null && event.isPreChangeEvent()) {
 			Assert.isTrue(previousCommand == null, "previousCommand is already set");
 			Assert.isTrue(allListeners == null, "allListeners is already set");
 			Assert.isTrue(listenerDataMap == null, "allListeners is already set");
 			// initialize the things we need to carry from the pre-change to the post-change event:						
 			previousCommand = eventCommand;
 			allListeners = new ArrayList<>(listeners);
 			listenerDataMap = new HashMap<>();
 			for (int i = 0; i < allListeners.size(); i++) {
 				
 				ModelChangeContext contextCopy = context.copy();
 				contextCopy.setCommandExecutionMode(getCommandExecutionMode(event));
 				
 				IModelChangeListener listener = allListeners.get(i);
 				listener.beforeModelChange(contextCopy);
 				
 				Object listenerData = contextCopy.getListenerData();
 				if (listenerData != null) {
 					listenerDataMap.put(Integer.valueOf(i), listenerData);
 				}
 			}
 		}
 		
 		// we're done in case of pre-change events because the afterAddItem, afterRemoveItem,
 		// afterMoveItem and afterSetAttributes methods shouldn't be called for those events
 		if (event.isPreChangeEvent()) {
 			return;
 		}
 		
 		// perform the dispatch for each command
 		for (Command command : commands) {
 			dispatch(command, event.getDetail());
 		}
 		
 		// in the case of a post-change event, (only) invoke the afterModelChangeListener(context) 
 		// method on the same listeners as the corresponding pre-change event (we don't work with 
 		// the notion of 'obsolete listeners' here); if no context is set, bypass this step (note 
 		// that we should avoid this situation but are allowing it to be able to move gradually to 
 		// the new dispatching methods
 		if (context != null && event.isPostChangeEvent()) {
 			Assert.isTrue(eventCommand == previousCommand, 
 						  "eventCommand does NOT match previousCommand");
 			Assert.isNotNull(allListeners, "allListeners not set during pre-change event");
 			Assert.isNotNull(listenerDataMap, "allListeners not set during pre-change event");
 			for (int i = 0; i < allListeners.size(); i++) {
 				
 				ModelChangeContext contextCopy = context.copy();
 				contextCopy.setCommandExecutionMode(getCommandExecutionMode(event));
 				Object listenerData = listenerDataMap.get(Integer.valueOf(i));
 				if (listenerData != null) {
 					contextCopy.setListenerData(listenerData);
 				}
 				
 				IModelChangeListener listener = allListeners.get(i);
 				listener.afterModelChange(contextCopy);
 			}
 			// nullify the things we needed to carry from the pre-change to the post-change event:
 			previousCommand = null;
 			allListeners = null;
 			listenerDataMap = null;
 		}
 
 	}
 
 	private void dispatch(Command command, int eventDetail) {		
 		
 		// only bother with commands that have a @ModelChange annotation on their class		
 		ModelChange modelChangeAnnotation = command.getClass().getAnnotation(ModelChange.class);		
 		if (modelChangeAnnotation == null) {
 			return; // no @ModelChange annotation
 		}
 		
 		// we're in 'dispatching' mode, set the flag; we need this flag to mark listeners as 
 		// obsolete, making sure they will no longer be notified when they have unregistered
 		dispatching = true;
 		
 		// obtain a model change notifier, depending on the model change category
 		IModelChangeNotifier notifier = null;
 		ModelChangeCategory category = modelChangeAnnotation.category();
 		if (category == ModelChangeCategory.ADD_ITEM ||
 			category == ModelChangeCategory.REMOVE_ITEM) {
 			
 			notifier = getAddOrRemoveItemNotifier(command, category, eventDetail);	
 		} else if (category == ModelChangeCategory.MOVE_ITEM) {
 			notifier = getMoveItemNotifier(command, eventDetail);	
 		} else if (category == ModelChangeCategory.SET_FEATURES) {
 			notifier = getSetFeaturesNotifier(command, eventDetail);
 		}
 		
 		// notify the listeners using the obtained notifier, i.e. if it's not null
 		if (notifier != null) {
 			
 			// write a debug message to the log...
 			StringBuilder debugMessage = new StringBuilder();
 			debugMessage.append("Model change dispatch:" + "\n");
 			debugMessage.append("Command class: " + command.getClass().getName() + "\n");
 			debugMessage.append("Model change category: " + category + "\n");
 			debugMessage.append("Event detail: " + getEventDetailDescription(eventDetail) + " (" + 
 					 			eventDetail + ")\n");
 			debugMessage.append("About to notify " + listeners.size() + " listeners... " +
 								"will call method '" + notifier.getMethodName() + 
 					 			"' for each listener, with the following arguments:");
 			String[] argumentNames = notifier.getMethodArgumentNames();
 			String[] argumentValues = notifier.getMethodArgumentValues();
 			for (int i = 0; i < argumentNames.length; i++) {
 				debugMessage.append("\n- " + argumentNames[i] + ": " + argumentValues[i]);
 			}
 			logDebug(debugMessage.toString());			
 			
 			// only notify a listener when it's NOT on the queue of obsolete listeners; it is 
 			// important to traverse a copy of the listener list because listeners can be added and
 			// removed during the notification process
 			for (IModelChangeListener listener : new ArrayList<>(listeners)) {
 				if (!isObsolete(listener)) {
 					notifier.doNotify(listener);
 				} 
 			}		
 			
 			// clear the obsolete listeners set
 			obsoleteListeners.clear();
 			
 		}
 		
 		// reset the 'dispatching' indicator
 		dispatching = false;		
 		
 	}
 	
 	public void dispose() {
 		// we provide this method because when a diagram editor is closed, the graphical edit parts
 		// will not have the opportunity to remove themselves as a listener because their 
 		// removeNotify() method will not be called; calling dispose() on the model change 
 		// dispatcher clears all listeners, which is good enough for us
 		Assert.isTrue(!isDisposed(), "model change dispatcher is already disposed");
 		Assert.isTrue(!dispatching, "shouldn't be invoked when dispatching");
 		listeners.clear();
 		disposed = true;
 		logDebug("Model change dispatcher was disposed; the listener count is now " + 
 				 listeners.size());
 	}
 
 	IModelChangeNotifier getAddOrRemoveItemNotifier(
 		Command command,
 		ModelChangeCategory category,
 		int eventDetail) {
 		
 		Assert.isTrue(eventDetail == CommandStack.POST_EXECUTE ||
 				  	  eventDetail == CommandStack.POST_UNDO ||
 				  	  eventDetail == CommandStack.POST_REDO,
 				  	  "not POST_EXECUTE/POST_UNDO/POST_REDO");		
 		
 		// we need the owner, reference and the item added or removed 		
 		final EObject owner = 
 			getAnnotatedFieldValue(command, Owner.class, "type", OwnerType.DEFAULT, 
 								   Availability.MANDATORY);
 		final EReference reference = 
 			getAnnotatedFieldValue(command, Reference.class, Availability.MANDATORY);
 		final Object item = getAnnotatedFieldValue(command, Item.class, Availability.MANDATORY);		
 		
 		// create the notifier and return it
 		IModelChangeNotifier notifier;
 		if (category == ModelChangeCategory.ADD_ITEM && isExecuteOrRedo(eventDetail) ||
 			category == ModelChangeCategory.REMOVE_ITEM && !isExecuteOrRedo(eventDetail)) {
 			
 			notifier = new IModelChangeNotifier() {
 				@Override
 				public void doNotify(IModelChangeListener listener) {
 					listener.afterAddItem(owner, reference, item);
 				}
 				@Override
 				public String[] getMethodArgumentNames() {
 					return new String[] {"owner", "reference", "item"};
 				}
 				@Override
 				public String[] getMethodArgumentValues() {
 					return new String[] {owner.toString(), reference.toString(), item.toString()};
 				}
 				@Override
 				public String getMethodName() {
 					return "afterAddItem";
 				}							
 			};
 		} else {
 			notifier = new IModelChangeNotifier() {
 				@Override
 				public void doNotify(IModelChangeListener listener) {
 					listener.afterRemoveItem(owner, reference, item);
 				}
 				@Override
 				public String[] getMethodArgumentNames() {
 					return new String[] {"owner", "reference", "item"};
 				}				
 				@Override
 				public String[] getMethodArgumentValues() {
 					return new String[] {owner.toString(), reference.toString(), item.toString()};
 				}
 				@Override
 				public String getMethodName() {
 					return "afterRemoveItem";
 				}
 			};			
 		}
 		return notifier;		
 		
 	}
 	
 	IModelChangeNotifier getMoveItemNotifier(
 		Command command, 
 		int eventDetail) {
 		
 		Assert.isTrue(eventDetail == CommandStack.POST_EXECUTE ||
 				  	  eventDetail == CommandStack.POST_UNDO ||
 				  	  eventDetail == CommandStack.POST_REDO,
 				  	  "not POST_EXECUTE/POST_UNDO/POST_REDO");		
 		
 		// we need the old and new owner, the reference and the item; all this stuff needs to be in 
 		// place after the command's execute() method was called 		
 		final EObject oldOwner, newOwner;
 		if (isExecuteOrRedo(eventDetail)) {			
 			// the command was executed for the first time, or was redone
 			oldOwner = getAnnotatedFieldValue(command, Owner.class, "type", OwnerType.OLD, 
 											  Availability.MANDATORY);
 			newOwner = getAnnotatedFieldValue(command, Owner.class, "type", OwnerType.NEW, 
 											  Availability.MANDATORY);			
 		} else {			
 			// the command was undone
 			oldOwner = getAnnotatedFieldValue(command, Owner.class, "type", OwnerType.NEW, 
 											  Availability.MANDATORY);
 			newOwner = getAnnotatedFieldValue(command, Owner.class, "type", OwnerType.OLD, 
 											  Availability.MANDATORY);			
 		}		
 		final EReference reference = 
 			getAnnotatedFieldValue(command, Reference.class, Availability.MANDATORY);
 		final Object item = 
 			getAnnotatedFieldValue(command, Item.class, Availability.MANDATORY);		
 		
 		// create the notifier and return it
 		IModelChangeNotifier notifier = new IModelChangeNotifier() {
 			@Override
 			public void doNotify(IModelChangeListener listener) {
 				listener.afterMoveItem(oldOwner, reference, item, newOwner);
 			}
 			@Override
 			public String[] getMethodArgumentNames() {
 				return new String[] {"oldOwner", "reference", "item", "newOwner"};
 			}
 			@Override
 			public String[] getMethodArgumentValues() {
 				return new String[] {oldOwner.toString(), reference.toString(), item.toString(),
 									 newOwner.toString()};
 			}
 			@Override
 			public String getMethodName() {
 				return "afterMoveItem";
 			}
 		};
 		return notifier;		
 		
 	}
 	
 	public Schema getSchema() {
 		return schema;
 	}
 
 	IModelChangeNotifier getSetFeaturesNotifier(
 		Command command,
 		int eventDetail) {
 		
 		Assert.isTrue(eventDetail == CommandStack.POST_EXECUTE ||
 					  eventDetail == CommandStack.POST_UNDO ||
 					  eventDetail == CommandStack.POST_REDO,
 					  "not POST_EXECUTE/POST_UNDO/POST_REDO");		
 		
 		// we need the target item (owner) and the attributes; these items need to be in place after 
 		// the command's execute() method was called 		
 		final EObject owner = getAnnotatedFieldValue(command, Owner.class, "type", 
 													 OwnerType.DEFAULT, Availability.MANDATORY);
 		final EStructuralFeature[] features = 
 			getAnnotatedFieldValue(command, Features.class, Availability.MANDATORY);		
 		
 		// create the notifier and return it
 		IModelChangeNotifier notifier = new IModelChangeNotifier() {
 			@Override
 			public void doNotify(IModelChangeListener listener) {
 				listener.afterSetFeatures(owner, features);
 			}
 			@Override
 			public String[] getMethodArgumentNames() {
 				return new String[] {"owner", "features"};
 			}
 			@Override
 			public String[] getMethodArgumentValues() {
 				List<EStructuralFeature> listOfFeatures = Arrays.asList(features);
 				return new String[] {owner.toString(), listOfFeatures.toString()};
 			}
 			@Override
 			public String getMethodName() {
 				return "afterSetFeatures";
 			}			
 		};
 		return notifier;		
 		
 	}
 	
 	public boolean isDisposed() {
 		return disposed;
 	}
 	
 	private boolean isExecuteOrRedo(int eventDetail) {
 		if (eventDetail == CommandStack.PRE_EXECUTE ||
 			eventDetail == CommandStack.PRE_REDO ||
 			eventDetail == CommandStack.POST_EXECUTE ||
 			eventDetail == CommandStack.POST_REDO) {
 			
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	boolean isObsolete(IModelChangeListener listener) {
 		return obsoleteListeners.contains(listener);
 	}	
 	
 	private void logDebug(String message) {
 		// TODO configure debug logging; it's fine for now but we should change it in the future
 		Plugin.logDebug(message);
 	}
 
 	public void removeModelChangeListener(IModelChangeListener listener) {
 		Assert.isTrue(!isDisposed(), "model change dispatcher is disposed");
 		// when dispatching, the listener-to-be-removed should be put on the obsolete listeners  
 		// list; it can be removed immediately from the listener list however
 		if (dispatching) {
 			obsoleteListeners.add(listener);
 		} 
 		listeners.remove(listener);
 		// write a debug message to the log...
 		StringBuilder debugMessage = new StringBuilder();
 		debugMessage.append("Removed model change dispatch listener:" + "\n");
 		debugMessage.append("Listener class: " + listener.getClass().getName() + "\n");
 		if (listener instanceof EditPart) {
 			EditPart editPart = (EditPart) listener;
 			debugMessage.append("Edit part model: " + (editPart).getModel());
 			debugMessage.append("\n");
 			if (editPart.getParent() != null) {
 				EditPart parent = editPart.getParent();
 				debugMessage.append("Parent edit part class: " + parent.getClass().getName() + "\n");
 				debugMessage.append("Parent edit part model: " + parent.getModel() + "\n");
 				if (parent.getParent() != null) {
 					EditPart grandParent = parent.getParent();
 					debugMessage.append("Grand parent edit part class: " + 
 										grandParent.getClass().getName() + "\n");
 					debugMessage.append("Grand parent edit part model: " + grandParent.getModel() + 
 										"\n");				
 				} else {
 					debugMessage.append("Grand parent edit part: null\n");
 				}				
 			} else {
 				debugMessage.append("Parent edit part: null\n");
 			}
 		}
 		debugMessage.append("The listener count is now " + listeners.size());			
 		logDebug(debugMessage.toString());		
 	}
 
 	public void setSchema(Schema schema) {
 		this.schema = schema;
 	}
 	
 }
