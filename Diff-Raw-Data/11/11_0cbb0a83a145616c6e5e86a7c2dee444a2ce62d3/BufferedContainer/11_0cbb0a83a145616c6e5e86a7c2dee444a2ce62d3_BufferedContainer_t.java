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
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import jvstm.VBox;
 import pt.ist.fenixWebFramework.services.Service;
 
 import com.vaadin.data.Buffered;
 import com.vaadin.data.BufferedValidatable;
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.Validator;
 import com.vaadin.data.Validator.InvalidValueException;
 import com.vaadin.data.util.AbstractInMemoryContainer;
 import com.vaadin.data.util.filter.UnsupportedFilterException;
 
 public abstract class BufferedContainer<ItemId, PropertyId, ItemType extends Item> extends
	AbstractInMemoryContainer<Object, PropertyId, ItemType> implements Property, HintedProperty, BufferedValidatable,
	Property.ReadOnlyStatusChangeNotifier, Property.ValueChangeNotifier, Container.Sortable, Container.Filterable,
	Container.PropertySetChangeNotifier {
     private final HintedProperty value;
 
     private final List<PropertyId> propertyIds = new ArrayList<PropertyId>();
 
     private final Map<PropertyId, Class<?>> types = new HashMap<PropertyId, Class<?>>();
 
     private final Map<Object, ItemType> items = new HashMap<Object, ItemType>();
 
     private boolean readThrough = true;
 
     private boolean writeThrough = false;
 
     private boolean invalidAllowed = true;
 
     private boolean invalidCommited = false;
 
     private boolean modified = false;
 
     private List<Validator> validators;
 
     private ItemConstructor<PropertyId> constructor;
 
     private ItemWriter<PropertyId> writer;
 
     private final Class<? extends ItemId> elementType;
 
     private VBox<Boolean> disableCommitPropagation;
 
     private ItemRemover<ItemId> itemRemover;
 
     private Object indexPropertyId;
 
     public BufferedContainer(HintedProperty value, Class<? extends ItemId> elementType) {
 	this.value = value;
 	this.elementType = elementType;
 	if (!Collection.class.isAssignableFrom(value.getType())) {
 	    throw new UnsupportedOperationException("Containers work with Collection typed properties");
 	}
 	if (getValue() != null) {
 	    for (ItemId itemId : getValue()) {
 		addItem(itemId);
 	    }
 	}
 	initVBox();
 	value.addListener(new ValueChangeListener() {
 	    @Override
 	    public void valueChange(ValueChangeEvent event) {
		if (!disableCommitPropagation.get()) {
		    discard();
		}
 		// discard((Collection<ItemId>)event.getProperty().getValue());
 	    }
 	});
     }
 
     // private void discard(Collection<ItemId> newPropertyValues) {
     // final Collection<ItemId> oldItems = (Collection<ItemId>) getItemIds();
     //
     // //remove items that are no longer present
     // for(ItemId oldItem : oldItems) {
     // if (!newPropertyValues.contains(oldItem)) {
     // removeItem(oldItem);
     // }
     // }
     // }
 
     @Service
     private void initVBox() {
 	this.disableCommitPropagation = new VBox<Boolean>(false);
     }
 
     // Property implementation
     @Override
     public Collection<ItemId> getValue() {
 	return (Collection<ItemId>) value.getValue();
     }
 
     @Override
     public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
 	value.setValue(newValue);
     }
 
     @Override
     public void addHint(Hint hint) {
 	value.addHint(hint);
     }
 
     @Override
     public Collection<Hint> getHints() {
 	return value.getHints();
     }
 
     @Override
     public Class<? extends Collection<Property>> getType() {
 	return (Class<? extends Collection<Property>>) value.getType();
     }
 
     public Class<? extends ItemId> getElementType() {
 	return elementType;
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
 
     // end of property implementation
 
     public void setItemRemover(ItemRemover<ItemId> itemRemover) {
 	this.itemRemover = itemRemover;
     }
 
     // BufferedValidatable implementation
 
     public void setConstructor(ItemConstructor<PropertyId> constructor) {
 	this.constructor = constructor;
     }
 
     public void setWriter(ItemWriter<PropertyId> writer) {
 	this.writer = writer;
     }
 
     @Override
     @Service
     public void commit() throws SourceException, InvalidValueException {
 	if (!disableCommitPropagation.get()) {
 	    disableCommitPropagation.put(true);
 	    for (Object itemId : getItemIds()) {
 		if (indexPropertyId != null) {
 		    getContainerProperty(itemId, indexPropertyId).setValue(indexOfId(itemId));
 		}
 		if (getItem(itemId) instanceof Buffered) {
 		    ((Buffered) getItem(itemId)).commit();
 		}
 	    }
 	    Collection<ItemId> values = new ArrayList<ItemId>();
 	    for (Object itemId : getItemIds()) {
 		if (itemId instanceof Property) {
 		    values.add((ItemId) ((Property) itemId).getValue());
 		} else {
 		    values.add((ItemId) itemId);
 		}
 	    }
 	    if (getValue() != null) {
 		for (ItemId itemId : getValue()) {
 		    if (!values.contains(itemId)) {
 			if (itemRemover != null) {
 			    itemRemover.remove(itemId);
 			}
 		    }
 		}
 	    }
 	    setValue(values);
 	    disableCommitPropagation.put(false);
 	}
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void discard() throws SourceException {
 	final Collection<ItemId> oldItemIds = new ArrayList<ItemId>((Collection<ItemId>) getItemIds());
 	final Collection<ItemId> newItemIds = new ArrayList<ItemId>(getValue());
 
 	if (oldItemIds != null && newItemIds != null) {
 
 	    for (ItemId itemId : oldItemIds) {
 		if (!newItemIds.contains(itemId)) {
 		    internalRemoveItem(itemId);
 		} else {
 		    final ItemType item = getItem(itemId);
 		    if (item instanceof Buffered) {
 			((Buffered) item).discard();
 		    }
 		    newItemIds.remove(itemId);
 		}
 	    }
 
 	    for (ItemId itemId : newItemIds) {
 		addItem(itemId);
 	    }
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
 	    for (Object itemId : getItemIds()) {
 		if (getItem(itemId) instanceof Buffered) {
 		    ((Buffered) getItem(itemId)).setWriteThrough(writeThrough);
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
 	this.readThrough = readThrough;
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
 
     // end of BufferedValidatable implementation
 
     public void setIndexProperty(Object propertyId) {
 	indexPropertyId = propertyId;
     }
 
     // container implementation
 
     @Override
     public Collection<PropertyId> getContainerPropertyIds() {
 	return Collections.unmodifiableCollection(propertyIds);
     }
 
     /**
      * Add a new property to the container. The default value is ignored, we
      * have no intention to override the values in the underlying items.
      * 
      * @see Container#addContainerProperty(Object, Class, Object)
      */
     @Override
     public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue)
 	    throws UnsupportedOperationException {
 	// Fails, if nulls are given
 	if (propertyId == null || type == null) {
 	    return false;
 	}
 
 	// Fails if the Property is already present
 	if (propertyIds.contains(propertyId)) {
 	    return false;
 	}
 
 	// Adds the Property to Property list and types
 	propertyIds.add((PropertyId) propertyId);
 	types.put((PropertyId) propertyId, type);
 
 	// Sends a change event
 	fireContainerPropertySetChange();
 
 	return true;
     }
 
     /**
      * @see Container#removeContainerProperty(Object)
      */
     @Override
     public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
 	// Fails if the Property is not present
 	if (!propertyIds.contains(propertyId)) {
 	    return false;
 	}
 
 	// Removes the Property to Property list and types
 	propertyIds.remove(propertyId);
 	types.remove(propertyId);
 
 	// If remove the Property from all Items
 	for (Object itemId : getAllItemIds()) {
 	    items.get(itemId).removeItemProperty(propertyId);
 	}
 
 	// Sends a change event
 	fireContainerPropertySetChange();
 
 	return true;
     }
 
     /**
      * @see Container#getContainerProperty(java.lang.Object, java.lang.Object)
      */
     @Override
     public Property getContainerProperty(Object itemId, Object propertyId) {
 	return getItem(itemId).getItemProperty(propertyId);
     }
 
     /**
      * @see Container#getType(java.lang.Object)
      */
     @Override
     public Class<?> getType(Object propertyId) {
 	return types.get(propertyId);
     }
 
     /**
      * @see com.vaadin.data.util.AbstractInMemoryContainer#getItemIds()
      */
     @Override
     public Collection<?> getItemIds() {
 	return Collections.unmodifiableCollection(super.getItemIds());
     }
 
     /**
      * @see Container.Filterable#addContainerFilter(Container.Filter)
      */
     @Override
     public void addContainerFilter(Filter filter) throws UnsupportedFilterException {
 	addFilter(filter);
     }
 
     /**
      * @see Container.Filterable#removeContainerFilter(Container.Filter)
      */
     @Override
     public void removeContainerFilter(Filter filter) {
 	removeFilter(filter);
     }
 
     /**
      * @see Container.Filterable#removeAllContainerFilters()
      */
     @Override
     public void removeAllContainerFilters() {
 	removeAllFilters();
     }
 
     /**
      * @see Container.Sortable#sort(java.lang.Object[], boolean[])
      */
     @Override
     public void sort(Object[] propertyId, boolean[] ascending) {
 	sortContainer(propertyId, ascending);
     }
 
     /**
      * @see Container.Sortable#getSortableContainerPropertyIds()
      */
     @Override
     public Collection<?> getSortableContainerPropertyIds() {
 	return getSortablePropertyIds();
     }
 
     /**
      * @see com.vaadin.data.util.AbstractInMemoryContainer#getUnfilteredItem(java.lang.Object)
      */
     @Override
     protected ItemType getUnfilteredItem(Object itemId) {
 	if (itemId != null && items.containsKey(itemId)) {
 	    return items.get(itemId);
 	}
 	return null;
     }
 
     private ItemType internalMakeItem(Object itemId) {
 	final HintedProperty property;
 	if (itemId instanceof HintedProperty) {
 	    property = (HintedProperty) itemId;
 	    // react to creation of object, replacing the key from the promise
 	    // of a value to an actual value.
 	    // property.addListener(new ValueChangeListener() {
 	    // @Override
 	    // public void valueChange(ValueChangeEvent event) {
 	    // if (event.getProperty().getValue() != null) {
 	    // ItemType item = getUnfilteredItem(event.getProperty());
 	    // int index = indexOfId(event.getProperty());
 	    // removeItem(event.getProperty());
 	    // internalAddItemAt(index, event.getProperty().getValue(), item,
 	    // true);
 	    // ((ValueChangeNotifier) event.getProperty()).removeListener(this);
 	    // }
 	    // }
 	    // });
 	} else {
 	    property = new VBoxProperty(itemId);
 	}
 	property.addListener(new ValueChangeListener() {
 	    @Override
 	    public void valueChange(ValueChangeEvent event) {
 		if (isWriteThrough()) {
 		    property.removeListener(this);
 		    commit();
 		    property.addListener(this);
 		}
 	    }
 	});
 	if (property instanceof Item) {
 	    return (ItemType) property;
 	}
 	return makeItem(property);
     }
 
     protected abstract ItemType makeItem(HintedProperty itemId);
 
     @Override
     protected void registerNewItem(int position, final Object itemId, ItemType item) {
 	items.put(itemId, item);
 	if (item instanceof BufferedItem) {
 	    BufferedItem<PropertyId, ItemId> buffered = (BufferedItem<PropertyId, ItemId>) item;
 	    buffered.setConstructor(constructor);
 	    buffered.setWriter(writer);
 	    // buffered.setWriteThrough(writeThrough);
 	    buffered.setReadThrough(readThrough);
 	    buffered.setInvalidAllowed(invalidAllowed);
 	    buffered.setInvalidCommitted(invalidCommited);
 	}
     }
 
     @Override
     public Object addItemAt(int index) throws UnsupportedOperationException {
 	throw new UnsupportedOperationException();
     }
 
     @Override
     public Item addItemAt(int index, Object newItemId) throws UnsupportedOperationException {
 	return internalAddItemAt(index, newItemId, internalMakeItem(newItemId), true);
     }
 
     @Override
     public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
 	throw new UnsupportedOperationException();
     }
 
     @Override
     public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
 	return internalAddItemAfter(previousItemId, newItemId, internalMakeItem(newItemId), true);
     }
 
     @Override
     public Item addItem(Object itemId) throws UnsupportedOperationException {
 	return internalAddItemAtEnd(itemId, internalMakeItem(itemId), true);
     }
 
     public Item addItem(Class<? extends ItemId> type) {
 	return addItem(new VBoxProperty(type));
     }
 
     @Service
     public void addItemBatch(Collection<?> itemIds) {
 	for (Object itemId : itemIds) {
 	    addItem(itemId);
 	}
     }
 
     @Override
     public Object addItem() throws UnsupportedOperationException {
 	throw new UnsupportedOperationException();
     }
 
     @Override
     protected ItemType internalAddItemAt(int index, Object newItemId, ItemType item, boolean filter) {
 	ItemType result = super.internalAddItemAt(index, newItemId, item, filter);
 	if (isWriteThrough()) {
 	    commit();
 	}
 	return result;
     }
 
     @Override
     public boolean removeItem(Object itemId) throws UnsupportedOperationException {
 	if (itemId == null || items.remove(itemId) == null) {
 	    return false;
 	}
 	int origSize = size();
 	int position = indexOfId(itemId);
 	if (internalRemoveItem(itemId)) {
 	    // fire event only if the visible view changed, regardless of
 	    // whether filtered out items were removed or not
 	    if (size() != origSize) {
 		if (isWriteThrough()) {
 		    commit();
 		}
 		fireItemRemoved(position, itemId);
 	    }
 
 	    return true;
 	}
 	return false;
     }
 
     @Override
     public boolean removeAllItems() throws UnsupportedOperationException {
 	int origSize = size();
 
 	internalRemoveAllItems();
 
 	items.clear();
 
 	// fire event only if the visible view changed, regardless of whether
 	// filtered out items were removed or not
 	if (origSize != 0) {
 	    if (isWriteThrough()) {
 		commit();
 	    }
 	    // Sends a change event
 	    fireItemSetChange();
 	}
 	return true;
     }
 
     /**
      * @see com.vaadin.data.util.AbstractContainer#addListener(com.vaadin.data.Container.PropertySetChangeListener)
      */
     @Override
     public void addListener(PropertySetChangeListener listener) {
 	super.addListener(listener);
     }
 
     /**
      * @see com.vaadin.data.util.AbstractContainer#removeListener(com.vaadin.data.Container.PropertySetChangeListener)
      */
     @Override
     public void removeListener(PropertySetChangeListener listener) {
 	super.removeListener(listener);
     }
 
     // end of container implementation
 
     @Override
     public String toString() {
 	return getValue() != null ? getValue().toString() : null;
     }
 }
