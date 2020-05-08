 /*
  * Copyright 2011 Instituto Superior Tecnico
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the vaadin-framework.
  *
  *   The vaadin-framework Infrastructure is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version
  *   3 of the License, or (at your option) any later version.*
  *
  *   vaadin-framework is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with vaadin-framework. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package pt.ist.vaadinframework.data;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EventObject;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import jvstm.CommitException;
 import jvstm.cps.ConsistencyException;
 
 import org.apache.commons.lang.StringUtils;
 
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixframework.FFDomainException;
 import pt.ist.fenixframework.pstm.AbstractDomainObject.UnableToDetermineIdException;
 import pt.ist.fenixframework.pstm.IllegalWriteException;
 import pt.ist.vaadinframework.VaadinFrameworkLogger;
 import pt.ist.vaadinframework.terminal.DomainExceptionErrorMessage;
 
 import com.vaadin.data.Buffered;
 import com.vaadin.data.BufferedValidatable;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.Validatable;
 import com.vaadin.data.Validator;
 import com.vaadin.data.Validator.InvalidValueException;
 import com.vaadin.data.util.AbstractProperty;
 import com.vaadin.data.util.ObjectProperty;
 
 public abstract class BufferedItem<PropertyId, Type> implements Item, Item.PropertySetChangeNotifier, Property, HintedProperty,
 BufferedValidatable, Property.ReadOnlyStatusChangeNotifier, Property.ValueChangeNotifier {
     public class BufferedProperty extends AbstractProperty implements HintedProperty {
 	private final PropertyId propertyId;
 
 	protected Class<?> type;
 
 	private final Collection<Hint> hints;
 
 	public BufferedProperty(PropertyId propertyId, Class<?> type, Hint... hints) {
 	    this(propertyId, type, Arrays.asList(hints));
 	}
 
 	public BufferedProperty(PropertyId propertyId, Class<?> type, Collection<Hint> hints) {
 	    this.propertyId = propertyId;
 	    this.type = type;
 	    this.hints = hints;
 	}
 
 	@Override
 	public Object getValue() {
 	    return getPropertyValue(propertyId);
 	}
 
 	@Override
 	public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
 	    if (isReadOnly()) {
 		throw new ReadOnlyException();
 	    }
 	    setPropertyValue(propertyId, newValue);
 	    fireValueChange();
 	}
 
 	@Override
 	public Collection<Hint> getHints() {
 	    return Collections.unmodifiableCollection(hints);
 	}
 
 	@Override
 	public Class<?> getType() {
 	    return type;
 	}
 
 	@Override
 	public String toString() {
 	    return getValue() != null ? getValue().toString() : null;
 	}
     }
 
     private final HintedProperty value;
 
     private final LinkedList<PropertyId> list = new LinkedList<PropertyId>();
 
     private final HashMap<Object, Property> map = new HashMap<Object, Property>();
 
     private final Map<Object, Object> propertyValues = new HashMap<Object, Object>();
 
     private ItemConstructor<PropertyId> constructor;
 
     private ItemWriter<PropertyId> writer;
 
     private boolean readThrough = true;
 
     private boolean writeThrough = false;
 
     private boolean invalidAllowed = true;
 
     private boolean invalidCommited = false;
 
    private boolean modified = false;
 
     private List<Validator> validators;
 
     private LinkedList<Item.PropertySetChangeListener> propertySetChangeListeners = null;
 
     private boolean propertySetChangePropagationEnabled = true;
 
     private Item.PropertySetChangeEvent lastEvent;
 
     private final ValueChangeListener innerPropertyChangeListener = new ValueChangeListener() {
 	@Override
 	public void valueChange(ValueChangeEvent event) {
 	    discard();
 	}
     };
 
     public BufferedItem(HintedProperty value) {
 	this.value = value;
 	this.value.addListener(innerPropertyChangeListener);
     }
 
     protected Object getPropertyValue(PropertyId propertyId) {
 	if (isReadThrough() && !isModified()) {
 	    Type value = getValue();
 	    propertyValues.put(propertyId, value == null ? null : readPropertyValue(value, propertyId));
 	}
 	return propertyValues.get(propertyId);
     }
 
     /**
      * Access method for the value of the property with the specified id in the
      * value of the item.
      * 
      * @param host instance of the object mapped in the {@link Item}
      * @param propertyId id of the property.
      * @return the value of the property in the host object.
      */
     protected abstract Object readPropertyValue(Type host, PropertyId propertyId);
 
     protected void setPropertyValue(PropertyId propertyId, Object newValue) throws SourceException, InvalidValueException {
 	VaadinFrameworkLogger.getLogger().debug("writting item property: " + propertyId + " with value: " + newValue);
 	propertyValues.put(propertyId, newValue);
 	modified = true;
 	if (isWriteThrough()) {
 	    commit(Collections.singletonList(propertyId));
 	}
     }
 
     /**
      * Write method for the value of the property with the specified id in the
      * value of the item.
      * 
      * @param host instance of the object mapped in the {@link Item}
      * @param propertyId id of the property.
      * @param newValue the new value of the property in the host object.
      */
     protected abstract void writePropertyValue(Type host, PropertyId propertyId, Object newValue);
 
     @Override
     public Type getValue() {
 	return (Type) value.getValue();
     }
 
     @Override
     public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
 	value.setValue(newValue);
     }
 
     @Override
     public Class<? extends Type> getType() {
 	return (Class<? extends Type>) value.getType();
     }
 
     @Override
     public boolean isReadOnly() {
 	return value.isReadOnly();
     }
 
     @Override
     public void setReadOnly(boolean newStatus) {
 	value.setReadOnly(newStatus);
     }
 
     @Override
     public Collection<Hint> getHints() {
 	return value.getHints();
     }
 
     @Override
     public void addListener(ReadOnlyStatusChangeListener listener) {
 	if (value instanceof ReadOnlyStatusChangeNotifier) {
 	    ((ReadOnlyStatusChangeNotifier) value).addListener(listener);
 	} else {
 	    throw new UnsupportedOperationException("Underlying property is not a ReadOnlyStatusChangeNotifier");
 	}
     }
 
     @Override
     public void removeListener(ReadOnlyStatusChangeListener listener) {
 	if (value instanceof ReadOnlyStatusChangeNotifier) {
 	    ((ReadOnlyStatusChangeNotifier) value).removeListener(listener);
 	} else {
 	    throw new UnsupportedOperationException("Underlying property is not a ReadOnlyStatusChangeNotifier");
 	}
     }
 
     @Override
     public void addListener(ValueChangeListener listener) {
 	if (value instanceof ValueChangeNotifier) {
 	    ((ValueChangeNotifier) value).addListener(listener);
 	} else {
 	    throw new UnsupportedOperationException("Underlying property is not a ValueChangeNotifier");
 	}
     }
 
     @Override
     public void removeListener(ValueChangeListener listener) {
 	if (value instanceof ValueChangeNotifier) {
 	    ((ValueChangeNotifier) value).removeListener(listener);
 	} else {
 	    throw new UnsupportedOperationException("Underlying property is not a ValueChangeNotifier");
 	}
     }
 
     @Override
     public boolean addItemProperty(Object propertyId, Property property) {
 	// Null ids are not accepted
 	if (propertyId == null) {
 	    throw new NullPointerException("Item property id can not be null");
 	}
 
 	// Cant add a property twice
 	if (map.containsKey(propertyId)) {
 	    return false;
 	}
 
 	// Put the property to map
 	map.put(propertyId, property);
 	list.add((PropertyId) propertyId);
 
 	Type value = getValue();
 	if (property instanceof ObjectProperty<?>) {
 	    propertyValues.put(propertyId, property.getValue());
 	} else {
 	    propertyValues.put(propertyId, value == null ? (property.getType().equals(Boolean.class) ? Boolean.FALSE : null)
 		    : readPropertyValue(value, (PropertyId) propertyId));
 	}
 
 	if (property instanceof Buffered) {
 	    ((Buffered) property).setWriteThrough(isWriteThrough());
 	    ((Buffered) property).setReadThrough(isReadThrough());
 	}
 	if (property instanceof Validatable) {
 	    ((Validatable) property).setInvalidAllowed(isInvalidAllowed());
 	}
 
 	// Send event
 	fireItemPropertySetChange();
 	return true;
     }
 
     @Override
     public boolean removeItemProperty(Object propertyId) {
 	// Cant remove missing properties
 	if (map.remove(propertyId) == null) {
 	    return false;
 	}
 	list.remove(propertyId);
 	propertyValues.remove(propertyId);
 
 	// Send change events
 	fireItemPropertySetChange();
 
 	return true;
     }
 
     @Override
     public Collection<PropertyId> getItemPropertyIds() {
 	return Collections.unmodifiableCollection(list);
     }
 
     public void setConstructor(ItemConstructor<PropertyId> constructor) {
 	this.constructor = constructor;
     }
 
     public void setWriter(ItemWriter<PropertyId> writer) {
 	this.writer = writer;
     }
 
     @Override
     public Property getItemProperty(Object propertyId) {
 	Property property = map.get(propertyId);
 	if (property == null) {
 	    property = makeProperty((PropertyId) propertyId);
 	}
 	return property;
     }
 
     /**
      * Lazy creation of properties, this method is invoked for every propertyId
      * that is requested of the Item. The created properties are not
      * automatically registered in the item, you have to invoke
      * {@link #addItemProperty(Object, Property)} yourself. You also need to
      * ensure that the returned properties are of {@link BufferedProperty}s or
      * {@link Item}s or {@link Collection}s over {@link BufferedProperty}s.
      * 
      * @param propertyId The key of the property.
      * @return A {@link Property} instance.
      */
     protected abstract Property makeProperty(PropertyId propertyId);
 
     @Override
     public void commit() throws SourceException, InvalidValueException {
 	commit(getItemPropertyIds());
     }
 
     @Service
     private void commit(Collection<PropertyId> savingPropertyIds) throws SourceException, InvalidValueException {
 	this.value.removeListener(innerPropertyChangeListener);
 	final LinkedList<Throwable> problems = new LinkedList<Throwable>();
 	final LinkedList<PropertyId> savingIds = new LinkedList<PropertyId>(savingPropertyIds);
 	for (PropertyId propertyId : savingIds) {
 	    try {
 		if (getItemProperty(propertyId) instanceof Buffered) {
 		    ((Buffered) getItemProperty(propertyId)).commit();
 		}
 	    } catch (Throwable e) {
 		handleException(e);
 		problems.add(e);
 	    }
 	}
 	if (getValue() == null) {
 	    // construction
 	    if (constructor != null) {
 		try {
 		    Method method = findMethod(constructor.getClass(), getArgumentTypes(constructor.getOrderedArguments()));
 		    Object[] argumentValues = readArguments(constructor.getOrderedArguments());
 		    VaadinFrameworkLogger.getLogger().debug(
 			    "persisting item with constructor with properties: ["
 				    + StringUtils.join(constructor.getOrderedArguments(), ", ") + "] with values: ["
 				    + StringUtils.join(argumentValues, ", ") + "]");
 		    setValue(method.invoke(constructor, argumentValues));
 		    savingIds.removeAll(Arrays.asList(constructor.getOrderedArguments()));
 		} catch (SecurityException e) {
 		    handleException(e);
 		    problems.add(e);
 		} catch (NoSuchMethodException e) {
 		    handleException(e);
 		    problems.add(e);
 		} catch (IllegalArgumentException e) {
 		    handleException(e);
 		    problems.add(e);
 		} catch (IllegalAccessException e) {
 		    handleException(e);
 		    problems.add(e);
 		} catch (InvocationTargetException e) {
 		    handleException(e);
 		    problems.add(e);
 		}
 	    } else {
 		try {
 		    setValue(getType().newInstance());
 		} catch (InstantiationException e) {
 		    handleException(e);
 		    problems.add(e);
 		} catch (IllegalAccessException e) {
 		    handleException(e);
 		    problems.add(e);
 		}
 	    }
 	} else {
 	    if (writer != null) {
 		try {
 		    if (fieldDiffer(writer.getOrderedArguments())) {
 			LinkedList<Class<?>> argumentTypes = new LinkedList<Class<?>>();
 			argumentTypes.add(getType());
 			argumentTypes.addAll(Arrays.asList(getArgumentTypes(writer.getOrderedArguments())));
 			Method method = findMethod(writer.getClass(), argumentTypes.toArray(new Class<?>[0]));
 			LinkedList<Object> argumentValues = new LinkedList<Object>();
 			argumentValues.add(getValue());
 			argumentValues.addAll(Arrays.asList(readArguments(writer.getOrderedArguments())));
 			VaadinFrameworkLogger.getLogger().debug(
 				"persisting item with writer with properties: ["
 					+ StringUtils.join(writer.getOrderedArguments(), ", ") + "] with values: ["
 					+ StringUtils.join(argumentValues.subList(1, argumentValues.size()), ", ") + "]");
 			method.invoke(writer, argumentValues.toArray(new Object[0]));
 		    }
 		    savingIds.removeAll(Arrays.asList(writer.getOrderedArguments()));
 		} catch (IllegalArgumentException e) {
 		    handleException(e);
 		    problems.add(e);
 		} catch (IllegalAccessException e) {
 		    handleException(e);
 		    problems.add(e);
 		} catch (InvocationTargetException e) {
 		    handleException(e);
 		    problems.add(e);
 		} catch (SecurityException e) {
 		    handleException(e);
 		    problems.add(e);
 		} catch (NoSuchMethodException e) {
 		    handleException(e);
 		    problems.add(e);
 		}
 	    }
 	}
 	for (PropertyId propertyId : savingIds) {
 	    try {
 		Object old = readPropertyValue(getValue(), propertyId);
 		Object current = propertyValues.get(propertyId);
 		if (current != null && current instanceof Property) {
 		    // could be wrapped inside a property when coming from
 		    // selections of containers that accept new items.
 		    current = ((Property) current).getValue();
 		}
 
 		if ((current == null && old != null) || (current != null && !current.equals(old))) {
 		    VaadinFrameworkLogger.getLogger()
 		    .debug("persisting item property: " + propertyId + " with value: " + current);
 		    writePropertyValue(getValue(), propertyId, current);
 		}
 	    } catch (Throwable e) {
 		handleException(e);
 		problems.add(e);
 	    }
 	}
 	this.value.addListener(innerPropertyChangeListener);
 	if (!problems.isEmpty()) {
 	    SourceException se = handleDomainException(new SourceException(this, problems.toArray(new Throwable[0])));
 	    throw se;
 	}
 	modified = false;
     }
 
     private ArrayList<Throwable> getAllCauses(Throwable t) {
 	final ArrayList<Throwable> causes = new ArrayList<Throwable>();
 	causes.add(t);
 	if (t instanceof Buffered.SourceException) {
 	    for (Throwable sec : ((Buffered.SourceException) t).getCauses()) {
 		causes.addAll(getAllCauses(sec));
 	    }
 	} else {
 	    if (t.getCause() != null) {
 		causes.addAll(getAllCauses(t.getCause()));
 	    }
 	}
 	return causes;
     }
 
     private Buffered.SourceException handleDomainException(Buffered.SourceException se) {
 	final ArrayList<Throwable> causes = new ArrayList<Throwable>();
 	for (Throwable throwable : getAllCauses(se)) {
 	    if (throwable instanceof FFDomainException) {
 		return new Buffered.SourceException(se.getSource(),
 			new Throwable[] { new DomainExceptionErrorMessage(throwable) });
 	    } else {
 		causes.add(throwable);
 	    }
 	}
 	return new Buffered.SourceException(se.getSource(), causes.toArray(new Throwable[0]));
     }
 
     private boolean fieldDiffer(PropertyId[] arguments) {
 	for (PropertyId propertyId : arguments) {
 	    if ((propertyValues.get(propertyId) == null && readPropertyValue(getValue(), propertyId) != null)
 		    || (propertyValues.get(propertyId) != null && !propertyValues.get(propertyId).equals(
 			    readPropertyValue(getValue(), propertyId)))) {
 		return true;
 	    }
 	}
 	return false;
     }
 
     private Method findMethod(Class<?> type, Class<?>[] types) throws NoSuchMethodException {
 	for (Method method : type.getMethods()) {
 	    Class<?>[] mTypes = method.getParameterTypes();
 	    boolean match = true;
 	    for (int i = 0; i < types.length; i++) {
 		if (i >= mTypes.length) {
 		    match = false;
 		    break;
 		}
 		if (!mTypes[i].isAssignableFrom(types[i])) {
 		    match = false;
 		    break;
 		}
 	    }
 	    if (!getType().isAssignableFrom(method.getReturnType())) {
 		match = false;
 	    }
 	    if (match) {
 		return method;
 	    }
 	}
 	final String message = "Must specify a method in class %s with a signature compatible with the arguments in getOrderedArguments() [%s]";
 	throw new NoSuchMethodException(String.format(message, type.getName(), StringUtils.join(types, ",")));
     }
 
     private void handleException(Throwable throwable) {
 	// This is a little hackish but is somewhat forced by the
 	// combination of architectures of both vaadin and the jvstm
 	if (throwable instanceof IllegalWriteException) {
 	    throw (IllegalWriteException) throwable;
 	} else if (throwable instanceof ConsistencyException) {
 	    throw (ConsistencyException) throwable;
 	} else if (throwable instanceof UnableToDetermineIdException) {
 	    throw (UnableToDetermineIdException) throwable;
 	} else if (throwable instanceof CommitException) {
 	    throw (CommitException) throwable;
 	} else if (throwable instanceof Buffered.SourceException) {
 	    for (Throwable cause : ((Buffered.SourceException) throwable).getCauses()) {
 		handleException(cause);
 	    }
 	} else if (throwable.getCause() != null) {
 	    handleException(throwable.getCause());
 	}
     }
 
     private Class<?>[] getArgumentTypes(PropertyId[] argumentIds) {
 	Class<?>[] types = new Class<?>[argumentIds.length];
 	for (int i = 0; i < argumentIds.length; i++) {
 	    types[i] = getItemProperty(argumentIds[i]).getType();
 	}
 	return types;
     }
 
     private Object[] readArguments(PropertyId[] argumentIds) {
 	Object[] arguments = new Object[argumentIds.length];
 	for (int i = 0; i < argumentIds.length; i++) {
 	    arguments[i] = propertyValues.get(argumentIds[i]);
 	}
 	return arguments;
     }
 
     @Override
     public void discard() throws SourceException {
 	Type value = getValue();
 	for (PropertyId propertyId : getItemPropertyIds()) {
 	    final Property itemProperty = getItemProperty(propertyId);
 	    if (itemProperty instanceof Buffered) {
 		((Buffered) itemProperty).discard();
 	    }
 	    propertyValues.put(propertyId, value == null ? (itemProperty.getType().equals(Boolean.class) ? Boolean.FALSE : null)
 		    : readPropertyValue(value, propertyId));
 	}
 	modified = false;
     }
 
     @Override
     public boolean isWriteThrough() {
 	return writeThrough;
     }
 
     @Override
     public void setWriteThrough(boolean writeThrough) throws SourceException, InvalidValueException {
 	if (writeThrough != this.writeThrough) {
 	    this.writeThrough = writeThrough;
 	    for (PropertyId propertyId : getItemPropertyIds()) {
 		if (getItemProperty(propertyId) instanceof Buffered) {
 		    ((Buffered) getItemProperty(propertyId)).setWriteThrough(writeThrough);
 		}
 	    }
 	    if (writeThrough && modified) {
 		commit();
 	    }
 	}
     }
 
     @Override
     public boolean isReadThrough() {
 	return readThrough;
     }
 
     @Override
     public void setReadThrough(boolean readThrough) throws SourceException {
 	if (readThrough != this.readThrough) {
 	    this.readThrough = readThrough;
 	    for (PropertyId propertyId : getItemPropertyIds()) {
 		if (getItemProperty(propertyId) instanceof Buffered) {
 		    ((Buffered) getItemProperty(propertyId)).setReadThrough(readThrough);
 		}
 	    }
 	}
     }
 
     @Override
     public boolean isModified() {
 	return modified;
     }
 
     @Override
     public void addValidator(Validator validator) {
 	if (validators == null) {
 	    validators = new LinkedList<Validator>();
 	}
 	validators.add(validator);
     }
 
     @Override
     public void removeValidator(Validator validator) {
 	if (validators != null) {
 	    validators.remove(validator);
 	}
     }
 
     @Override
     public Collection<Validator> getValidators() {
 	if (validators == null || validators.isEmpty()) {
 	    return null;
 	}
 	return Collections.unmodifiableCollection(validators);
     }
 
     @Override
     public boolean isValid() {
 	if (validators != null) {
 	    for (Validator validator : validators) {
 		if (!validator.isValid(this)) {
 		    return false;
 		}
 	    }
 	}
 	return true;
     }
 
     @Override
     public void validate() throws InvalidValueException {
 	LinkedList<InvalidValueException> errors = null;
 	if (validators != null) {
 	    for (Validator validator : validators) {
 		try {
 		    validator.validate(this);
 		} catch (InvalidValueException e) {
 		    if (errors == null) {
 			errors = new LinkedList<InvalidValueException>();
 		    }
 		    errors.add(e);
 		}
 	    }
 	}
 	if (errors != null) {
 	    throw new InvalidValueException(null, errors.toArray(new InvalidValueException[0]));
 	}
     }
 
     @Override
     public boolean isInvalidAllowed() {
 	return invalidAllowed;
     }
 
     @Override
     public void setInvalidAllowed(boolean invalidAllowed) throws UnsupportedOperationException {
 	this.invalidAllowed = invalidAllowed;
     }
 
     @Override
     public boolean isInvalidCommitted() {
 	return invalidCommited;
     }
 
     @Override
     public void setInvalidCommitted(boolean isCommitted) {
 	this.invalidCommited = isCommitted;
     }
 
     /* Notifiers */
 
     private class PropertySetChangeEvent extends EventObject implements Item.PropertySetChangeEvent {
 
 	private PropertySetChangeEvent(Item source) {
 	    super(source);
 	}
 
 	/**
 	 * Gets the Item whose Property set has changed.
 	 * 
 	 * @return source object of the event as an <code>Item</code>
 	 */
 	@Override
 	public Item getItem() {
 	    return (Item) getSource();
 	}
     }
 
     /**
      * Registers a new property set change listener for this Item.
      * 
      * @param listener the new Listener to be registered.
      */
     @Override
     public void addListener(Item.PropertySetChangeListener listener) {
 	if (propertySetChangeListeners == null) {
 	    propertySetChangeListeners = new LinkedList<PropertySetChangeListener>();
 	}
 	propertySetChangeListeners.add(listener);
     }
 
     /**
      * Removes a previously registered property set change listener.
      * 
      * @param listener the Listener to be removed.
      */
     @Override
     public void removeListener(Item.PropertySetChangeListener listener) {
 	if (propertySetChangeListeners != null) {
 	    propertySetChangeListeners.remove(listener);
 	}
     }
 
     /**
      * Sends a Property set change event to all interested listeners.
      */
     protected void fireItemPropertySetChange() {
 	if (propertySetChangeListeners != null) {
 	    final Item.PropertySetChangeEvent event = new BufferedItem.PropertySetChangeEvent(this);
 	    if (propertySetChangePropagationEnabled) {
 		final Object[] l = propertySetChangeListeners.toArray();
 		for (int i = 0; i < l.length; i++) {
 		    ((Item.PropertySetChangeListener) l[i]).itemPropertySetChange(event);
 		}
 	    } else {
 		lastEvent = event;
 	    }
 	}
     }
 
     public void setPropertySetChangePropagationEnabled(boolean propertySetChangePropagationEnabled) {
 	if (this.propertySetChangePropagationEnabled != propertySetChangePropagationEnabled) {
 	    this.propertySetChangePropagationEnabled = propertySetChangePropagationEnabled;
 	    if (propertySetChangePropagationEnabled && lastEvent != null) {
 		if (propertySetChangeListeners != null) {
 		    final Object[] l = propertySetChangeListeners.toArray();
 		    for (int i = 0; i < l.length; i++) {
 			((Item.PropertySetChangeListener) l[i]).itemPropertySetChange(lastEvent);
 		    }
 		}
 		lastEvent = null;
 	    }
 	}
     }
 
     public Collection<?> getListeners(Class<?> eventType) {
 	if (Item.PropertySetChangeEvent.class.isAssignableFrom(eventType)) {
 	    if (propertySetChangeListeners == null) {
 		return Collections.EMPTY_LIST;
 	    } else {
 		return Collections.unmodifiableCollection(propertySetChangeListeners);
 	    }
 	}
 
 	return Collections.EMPTY_LIST;
     }
 
     /**
      * Gets the <code>String</code> representation of the contents of the Item.
      * The format of the string is a space separated catenation of the
      * <code>String</code> representations of the Properties contained by the
      * Item.
      * 
      * @return <code>String</code> representation of the Item contents
      */
     @Override
     public String toString() {
 	String retValue = "";
 
 	for (final Iterator<?> i = getItemPropertyIds().iterator(); i.hasNext();) {
 	    final Object propertyId = i.next();
 	    retValue += getItemProperty(propertyId).toString();
 	    if (i.hasNext()) {
 		retValue += " ";
 	    }
 	}
 
 	return retValue;
     }
 }
