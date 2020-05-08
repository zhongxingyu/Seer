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
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.UUID;
 
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixWebFramework.services.ServiceManager;
 import pt.ist.fenixWebFramework.services.ServicePredicate;
 import pt.ist.vaadinframework.data.util.ServiceUtils;
 
 import com.vaadin.data.BufferedValidatable;
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.Validator;
 import com.vaadin.data.Validator.InvalidValueException;
 import com.vaadin.data.util.AbstractInMemoryContainer;
 import com.vaadin.data.util.filter.UnsupportedFilterException;
 
 public abstract class AbstractBufferedContainer<ItemId, Id, ItemType extends AbstractBufferedItem<Id, ? extends ItemId>> extends
 	AbstractInMemoryContainer<Object, Id, ItemType> implements HintedProperty<List<ItemId>>, BufferedValidatable,
 	Container.Sortable, Container.Filterable, Container.PropertySetChangeNotifier {
     private static Class classType;
 
     static {
 	try {
 	    classType = Class.forName("java.util.List");
 	} catch (ClassNotFoundException e) {
 	}
     }
 
     public class ContainerPropertyWrapper extends BufferedProperty<List<Object>> {
 	public ContainerPropertyWrapper(Property wrapped, Hint... hints) {
 	    super(wrapped, hints);
 	}
 
 	public ContainerPropertyWrapper(Hint... hints) {
 	    super(classType, hints);
 	}
 
 	public ContainerPropertyWrapper(List<Object> elements, Hint... hints) {
 	    super(elements, hints);
 	}
 
 	@Override
 	protected List<Object> convertValue(Object value) throws ConversionException {
 	    ArrayList<Object> result = new ArrayList<Object>();
 	    if (value != null) {
 		if (value instanceof Iterable) {
 		    for (Object itemId : (Iterable<?>) value) {
 			result.add(itemId);
 		    }
 		} else {
 		    throw new ConversionException();
 		}
 	    }
 	    return result;
 	}
 
 	@Override
 	protected void processNewCacheValue() {
 	    fireItemSetChange();
 	}
 
 	@Override
 	public void commit() throws SourceException, InvalidValueException {
 	    ServiceManager.execute(new ServicePredicate() {
 		@Override
 		public void execute() {
 		    try {
 			for (Object itemId : getAllItemIds()) {
 			    if (indexPropertyId != null) {
 				getContainerProperty(itemId, indexPropertyId).setValue(indexOfId(itemId));
 			    }
 			    getItem(itemId).commit();
 			}
 			ContainerPropertyWrapper.super.commit();
 		    } catch (Throwable e) {
 			ServiceUtils.handleException(e);
 		    }
 		}
 	    });
 	}
 
 	@Override
 	public void discard() throws SourceException {
 	    super.discard();
 	    for (Object itemId : getAllItemIds()) {
 		getItem(itemId).discard();
 	    }
 	}
 
 	@Override
 	public boolean isValid() {
 	    for (Object itemId : getAllItemIds()) {
 		if (!getItem(itemId).isValid()) {
 		    return false;
 		}
 	    }
 	    return super.isValid();
 	}
 
 	@Override
 	public void validate() throws InvalidValueException {
 	    for (Object itemId : getAllItemIds()) {
 		getItem(itemId).validate();
 	    }
 	    super.validate();
 	}
     }
 
     protected final ContainerPropertyWrapper value;
 
     protected final List<Id> propertyIds = new ArrayList<Id>();
 
     protected final Map<Id, Class<?>> types = new HashMap<Id, Class<?>>();
 
     protected final Map<ItemId, ItemType> items = new HashMap<ItemId, ItemType>();
 
     protected final List<ItemType> newItems = new ArrayList<ItemType>();
 
     protected final Map<UUID, ItemType> limboItems = new HashMap<UUID, ItemType>();
 
     protected final Class<? extends ItemId> elementType;
 
     protected Object indexPropertyId;
 
     private final ValueChangeListener itemChangeListener = new ValueChangeListener() {
 	@Override
 	public void valueChange(ValueChangeEvent event) {
 	    ItemType item = (ItemType) event.getProperty();
 	    if (newItems.contains(item)) {
 		newItems.remove(item);
 		// registerNewItem will add the listener again so avoid
 		// duplication by removing now.
 		item.removeListener(this);
 	    } else {
 		for (Entry<UUID, ItemType> entry : limboItems.entrySet()) {
 		    if (entry.getValue().equals(item)) {
 			item.removeListener(this);
 			getAllItemIds().remove(entry.getKey());
 			limboItems.remove(entry.getKey());
 		    }
 		}
 		// we lost the old id, so search the item in the map and remove
 		// by its key.
 		for (Entry<ItemId, ItemType> entry : items.entrySet()) {
 		    if (entry.getValue().equals(item)) {
 			removeItem(entry.getKey());
 		    }
 		}
 	    }
 	    internalAddItemAtEnd(item.getValue(), item, true);
 	}
     };
 
     public AbstractBufferedContainer(Property wrapped, Class<? extends ItemId> elementType, Hint... hints) {
 	super();
 	value = new ContainerPropertyWrapper(wrapped, hints);
 	this.elementType = elementType;
     }
 
     public AbstractBufferedContainer(Class<? extends ItemId> elementType, Hint... hints) {
 	super();
 	value = new ContainerPropertyWrapper(hints);
 	this.elementType = elementType;
     }
 
     public AbstractBufferedContainer(List<ItemId> elements, Class<? extends ItemId> elementType, Hint... hints) {
 	super();
 	value = new ContainerPropertyWrapper((List<Object>) elements, hints);
 	this.elementType = elementType;
     }
 
     public Class<? extends ItemId> getElementType() {
 	return elementType;
     }
 
     public void setIndexProperty(Object propertyId) {
 	indexPropertyId = propertyId;
     }
 
     // container implementation
 
     @Override
     protected List<Object> getAllItemIds() {
 	return value.getValue();
     }
 
     @Override
     public Collection<Id> getContainerPropertyIds() {
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
 	propertyIds.add((Id) propertyId);
 	types.put((Id) propertyId, type);
 
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
 
     // container filterable interface
 
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
 
     // container sortable interface
 
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
 	if (itemId != null) {
 	    if (limboItems.containsKey(itemId)) {
 		return limboItems.get(itemId);
 	    }
	    if (getAllItemIds().contains(itemId)) {
 		if (!items.containsKey(itemId)) {
 		    items.put((ItemId) itemId, makeItem((ItemId) itemId));
 		}
 		return items.get(itemId);
 	    }
 	}
 	return null;
     }
 
     protected abstract ItemType makeItem(ItemId itemId);
 
     protected abstract ItemType makeItem(Class<? extends ItemId> type);
 
     @Service
     public void addItemBatch(Collection<ItemId> itemIds) {
 	for (ItemId itemId : itemIds) {
 	    addItem(itemId);
 	}
     }
 
     @Override
     public ItemType addItemAt(int index, Object newItemId) throws UnsupportedOperationException {
 	return internalAddItemAt(index, newItemId, makeItem((ItemId) newItemId), true);
     }
 
     @Override
     public ItemType addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
 	return internalAddItemAfter(previousItemId, newItemId, makeItem((ItemId) newItemId), true);
     }
 
     @Override
     public ItemType addItem(Object itemId) throws UnsupportedOperationException {
 	return internalAddItemAtEnd(itemId, makeItem((ItemId) itemId), true);
     }
 
     public ItemType addItem(Class<? extends ItemId> type) {
 	ItemType item = makeItem(type);
 	newItems.add(item);
 	item.addListener(itemChangeListener);
 	return item;
     }
 
     public void addToLimbo(Item item) {
 	if (newItems.contains(item)) {
 	    newItems.remove(item);
 	    UUID id = UUID.randomUUID();
 	    getAllItemIds().add(id);
 	    limboItems.put(id, (ItemType) item);
 	    fireItemSetChange();
 	}
     }
 
     @Override
     public boolean removeItem(Object itemId) throws UnsupportedOperationException {
 	return internalRemoveItem(itemId);
     }
 
     @Override
     public boolean removeAllItems() throws UnsupportedOperationException {
 	internalRemoveAllItems();
 	return true;
     }
 
     @Override
     protected void registerNewItem(int position, final Object itemId, ItemType item) {
 	if (isReadOnly()) {
 	    // TODO: too late, changes have been made.
 	    throw new ReadOnlyException();
 	}
 	items.put((ItemId) itemId, item);
 	item.addListener(itemChangeListener);
 	value.modified = true;
 	if (isWriteThrough()) {
 	    commit();
 	}
     }
 
     @Override
     protected boolean internalRemoveItem(Object itemId) {
 	if (isReadOnly()) {
 	    // TODO: too late, changes have been made.
 	    throw new ReadOnlyException();
 	}
 	int position = indexOfId(itemId);
 	boolean result = super.internalRemoveItem(itemId);
 	if (result) {
 	    if (limboItems.containsKey(itemId)) {
 		limboItems.get(itemId).removeListener(itemChangeListener);
 		limboItems.remove(itemId);
 	    } else if (items.containsKey(itemId)) {
 		items.get(itemId).removeListener(itemChangeListener);
 		items.remove(itemId);
 		value.modified = true;
 		if (isWriteThrough()) {
 		    commit();
 		}
 		fireItemRemoved(position, itemId);
 	    }
 	}
 	return result;
     }
 
     @Override
     protected void internalRemoveAllItems() {
 	if (isReadOnly()) {
 	    // TODO: too late, changes have been made.
 	    throw new ReadOnlyException();
 	}
 	int size = size();
 	super.internalRemoveAllItems();
 	for (ItemType item : limboItems.values()) {
 	    item.removeListener(itemChangeListener);
 	}
 	limboItems.clear();
 	for (ItemType item : items.values()) {
 	    item.removeListener(itemChangeListener);
 	}
 	if (size > 0) {
 	    value.modified = true;
 	    if (isWriteThrough()) {
 		commit();
 	    }
 	    // Sends a change event
 	    fireItemSetChange();
 	}
     }
 
     @Override
     public void addListener(PropertySetChangeListener listener) {
 	super.addListener(listener);
     }
 
     @Override
     public void removeListener(PropertySetChangeListener listener) {
 	super.removeListener(listener);
     }
 
     // Property delegate methods
 
     @Override
     public List<ItemId> getValue() {
 	List<ItemId> result = new ArrayList<ItemId>();
 	for (Object itemId : getAllItemIds()) {
 	    try {
 		result.add((ItemId) itemId);
 	    } catch (ClassCastException e) {
 		// ignore this one.
 	    }
 	}
 	return result;
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
     public Class<? extends List<ItemId>> getType() {
 	return (Class<? extends List<ItemId>>) value.getType();
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
 	value.addListener(listener);
     }
 
     @Override
     public void removeListener(ReadOnlyStatusChangeListener listener) {
 	value.removeListener(listener);
     }
 
     @Override
     public void addListener(ValueChangeListener listener) {
 	value.addListener(listener);
     }
 
     @Override
     public void removeListener(ValueChangeListener listener) {
 	value.removeListener(listener);
     }
 
     @Override
     public void commit() throws SourceException, InvalidValueException {
 	ServiceManager.execute(new ServicePredicate() {
 	    @Override
 	    public void execute() {
 		try {
 		    for (Object itemId : getAllItemIds()) {
 			if (indexPropertyId != null) {
 			    getContainerProperty(itemId, indexPropertyId).setValue(indexOfId(itemId));
 			}
 			getItem(itemId).commit();
 		    }
 		    value.commit();
 		} catch (Throwable e) {
 		    ServiceUtils.handleException(e);
 		}
 	    }
 	});
     }
 
     @Override
     public void discard() throws SourceException {
 	value.discard();
     }
 
     @Override
     public boolean isWriteThrough() {
 	return value.isWriteThrough();
     }
 
     @Override
     public void setWriteThrough(boolean writeThrough) throws SourceException, InvalidValueException {
 	value.setWriteThrough(writeThrough);
     }
 
     @Override
     public boolean isReadThrough() {
 	return value.isReadThrough();
     }
 
     @Override
     public void setReadThrough(boolean readThrough) throws SourceException {
 	value.setReadThrough(readThrough);
     }
 
     @Override
     public boolean isModified() {
 	return value.isModified();
     }
 
     @Override
     public void addValidator(Validator validator) {
 	value.addValidator(validator);
     }
 
     @Override
     public void removeValidator(Validator validator) {
 	value.removeValidator(validator);
     }
 
     @Override
     public Collection<Validator> getValidators() {
 	return value.getValidators();
     }
 
     @Override
     public boolean isValid() {
 	return value.isValid();
     }
 
     @Override
     public void validate() throws InvalidValueException {
 	value.validate();
     }
 
     @Override
     public boolean isInvalidAllowed() {
 	return value.isInvalidAllowed();
     }
 
     @Override
     public void setInvalidAllowed(boolean invalidAllowed) throws UnsupportedOperationException {
 	value.setInvalidAllowed(invalidAllowed);
     }
 
     @Override
     public boolean isInvalidCommitted() {
 	return value.isInvalidCommitted();
     }
 
     @Override
     public void setInvalidCommitted(boolean invalidCommitted) {
 	value.setInvalidCommitted(invalidCommitted);
     }
 
     @Override
     public String toString() {
 	return value.toString();
     }
 }
