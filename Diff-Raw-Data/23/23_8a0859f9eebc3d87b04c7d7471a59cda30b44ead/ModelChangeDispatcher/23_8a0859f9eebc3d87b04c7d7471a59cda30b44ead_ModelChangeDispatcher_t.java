 /**
  * Copyright (C) 2013  Luc Hermans
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
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.commands.CommandStack;
 import org.eclipse.gef.commands.CommandStackEvent;
 import org.eclipse.gef.commands.CompoundCommand;
 import org.lh.dmlj.schema.editor.Plugin;
 import org.lh.dmlj.schema.editor.command.annotation.Features;
 import org.lh.dmlj.schema.editor.command.annotation.Item;
 import org.lh.dmlj.schema.editor.command.annotation.ModelChange;
 import org.lh.dmlj.schema.editor.command.annotation.ModelChangeCategory;
 import org.lh.dmlj.schema.editor.command.annotation.Owner;
 import org.lh.dmlj.schema.editor.command.annotation.OwnerType;
 import org.lh.dmlj.schema.editor.command.annotation.Reference;
 
 public class ModelChangeDispatcher implements IModelChangeProvider {
 	
 	public enum Availability {MANDATORY, OPTIONAL};	
 	
 	private boolean					   dispatching = false;
 	private List<IModelChangeListener> listeners = new ArrayList<>();
 	private Set<IModelChangeListener>  obsoleteListeners = new HashSet<>();
 
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
 	
 	private static String getEventDetailDescription(int eventDetail) {
 		if (eventDetail == CommandStack.POST_EXECUTE) {
 			return "POST_EXECUTE";
 		} else if (eventDetail == CommandStack.POST_REDO) {
 			return "POST_REDO";
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
 		if (!listeners.contains(listener)) {
 			// avoid notifying the same listener twice
 			listeners.add(listener);
 		}
 	}
 	
 	public void dispatch(CommandStackEvent event) {		
 		
 		// we don't care about pre-change events
 		if (event.isPreChangeEvent()) {
 			return;
 		}
 		
 		// we don't expect to be in dispatching mode
 		Assert.isTrue(!dispatching);		
 		
 		// the event's command is possibly a compound command - build a list of commands to process
 		Command eventCommand = event.getCommand();
 		List<Command> commands = new ArrayList<>();
 		if (eventCommand instanceof CompoundCommand) {
 			// the event command is a compound command
 			CompoundCommand compoundCommand = (CompoundCommand) eventCommand;
 			@SuppressWarnings("unchecked")
 			List<Command> compoundCommandCommands = compoundCommand.getCommands();			
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
 		} else {
 			// the event command is a simple command
 			commands.add(eventCommand);
 		}
 		
 		// perform the dispatch for each command
 		for (Command command : commands) {
 			dispatch(command, event.getDetail());
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
		// when dispatching, the listener-to-be-removed should be put on the obsolete listeners  
		// list; it can be removed immediately
 		if (dispatching) {
 			obsoleteListeners.add(listener);
		} 
		listeners.remove(listener);
 	}
 	
 }
