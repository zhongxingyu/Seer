 /*
  * Copyright 2012, Gary Piercey, All Rights Reserved.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 
 package com.vaadin.data.hbnutil;
 
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.lang.ref.WeakReference;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.ParameterizedType;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.hibernate.Criteria;
 import org.hibernate.EntityMode;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.engine.spi.SessionImplementor;
 import org.hibernate.metadata.ClassMetadata;
 import org.hibernate.type.ComponentType;
 import org.hibernate.type.Type;
 
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.hbnutil.HbnContainer.EntityItem.EntityItemProperty;
 import com.vaadin.data.util.MethodProperty;
 import com.vaadin.data.util.converter.Converter.ConversionException;
 import com.vaadin.data.util.filter.SimpleStringFilter;
 import com.vaadin.data.util.filter.UnsupportedFilterException;
 
 public class HbnContainer<T> implements Container, Container.Indexed, Container.Sortable,
 		Container.Filterable, Container.Hierarchical, Container.ItemSetChangeNotifier, Container.Ordered
 {
 	private static final long serialVersionUID = -6410337120924382057L;
 	private Logger logger = LoggerFactory.getLogger(HbnContainer.class);
 
 	private SessionFactory sessionFactory;
 	private ClassMetadata classMetadata;
 	private Class<T> entityType;
 	private String parentPropertyName = null;
 
 	private static final int REFERENCE_CLEANUP_INTERVAL = 2000;
 	private static final int ROW_BUF_SIZE = 100;
 	private static final int ID_TO_INDEX_MAX_SIZE = 300;
 
 	private boolean normalOrder = true;
 	private List<T> ascRowBuffer;
 	private List<T> descRowBuffer;
 	private Object lastId;
 	private Object firstId;
 	private List<T> indexRowBuffer;
 	private int indexRowBufferFirstIndex;
 	private final Map<Object, Integer> idToIndex = new LinkedHashMap<Object, Integer>();
 	private boolean[] orderAscendings;
 	private Object[] orderPropertyIds;
 	private Integer size;
 	private LinkedList<ItemSetChangeListener> itemSetChangeListeners;
 	private HashSet<ContainerFilter> filters;
 	private final HashMap<Object, WeakReference<EntityItem<T>>> entityCache = new HashMap<Object, WeakReference<EntityItem<T>>>();
 	private final Map<String, Class<?>> addedProperties = new HashMap<String, Class<?>>();
 	private int loadCount;
 
 	/**
 	 * Item wrappping a Hibernate mapped entity object. EntityItems are generally instantiated automatically by
 	 * HbnContainer.
 	 */
 	@SuppressWarnings("hiding")
 	public class EntityItem<T> implements Item
 	{
 
 		private static final long serialVersionUID = -2847179724504965599L;
 
 		/**
 		 * Reference to hibernate mapped entity that this Item wraps.
 		 */
 		protected T pojo;
 
 		/**
 		 * Instantiated properties of this EntityItem. May be either EntityItemProperty (hibernate field) or manually
 		 * added container property (MethodProperty).
 		 */
 		protected Map<Object, Property<?>> properties = new HashMap<Object, Property<?>>();
 
 		@SuppressWarnings("unchecked")
 		public EntityItem(Serializable id)
 		{
 			pojo = (T) sessionFactory.getCurrentSession().get(entityType, id);
 			// add non-hibernate mapped container properties
 			for (String propertyId : addedProperties.keySet())
 			{
 				addItemProperty(propertyId, new MethodProperty<Object>(pojo, propertyId));
 			}
 		}
 
 		/**
 		 * @return the wrapped entity object.
 		 */
 		public T getPojo()
 		{
 			return pojo;
 		}
 
 		@SuppressWarnings("rawtypes")
 		public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException
 		{
 			properties.put(id, property);
 			return true;
 		}
 
 		public Property<?> getItemProperty(Object id)
 		{
 			Property<?> p = properties.get(id);
 			if (p == null)
 			{
 				p = new EntityItemProperty(id.toString());
 				properties.put(id, p);
 			}
 			return p;
 		}
 
 		public Collection<?> getItemPropertyIds()
 		{
 			return getContainerPropertyIds();
 		}
 
 		public boolean removeItemProperty(Object id) throws UnsupportedOperationException
 		{
 			Property<?> removed = properties.remove(id);
 			return removed != null;
 		}
 
 		/**
 		 * EntityItemProperty wraps one Hibernate controlled field of the pojo used by EntityItem. For common fields the
 		 * field value is the same as Property value. For relation fields it is the identifier of related object or a
 		 * collection of identifiers.
 		 */
 		@SuppressWarnings("rawtypes")
 		public class EntityItemProperty implements Property, Property.ValueChangeNotifier
 		{
 
 			private static final long serialVersionUID = -4086774943938055297L;
 			private final String propertyName;
 
 			public EntityItemProperty(String propertyName)
 			{
 				this.propertyName = propertyName;
 			}
 
 			public EntityItem<T> getEntityItem()
 			{
 				return EntityItem.this;
 			}
 
 			public T getPojo()
 			{
 				return pojo;
 			}
 
 			/**
 			 * A helper method to get raw type of this (Hibernate) property.
 			 * 
 			 * @return the raw type of field
 			 */
 			private Type getPropertyType()
 			{
 				return classMetadata.getPropertyType(propertyName);
 			}
 
 			private boolean propertyInEmbeddedKey()
 			{
 				// TODO a place for optimization, this is not needed to be done
 				// for each separate property
 				Type idType = classMetadata.getIdentifierType();
 				if (idType.isComponentType())
 				{
 					ComponentType idComponent = (ComponentType) idType;
 					String[] idPropertyNames = idComponent.getPropertyNames();
 					List<String> idPropertyNameList = Arrays.asList(idPropertyNames);
 					if (idPropertyNameList.contains(propertyName))
 					{
 						return true;
 					}
 					else
 					{
 						return false;
 					}
 				}
 				else
 				{
 					return false;
 				}
 			}
 
 			public Class<?> getType()
 			{
 				// TODO clean, optimize, review
 
 				if (propertyInEmbeddedKey())
 				{
 					ComponentType idType = (ComponentType) classMetadata.getIdentifierType();
 					String[] propertyNames = idType.getPropertyNames();
 					for (int i = 0; i < propertyNames.length; i++)
 					{
 						String name = propertyNames[i];
 						if (name.equals(propertyName))
 						{
 							try
 							{
 								String idName = classMetadata.getIdentifierPropertyName();
 								Field idField = entityType.getDeclaredField(idName);
 								Field propertyField = idField.getType().getDeclaredField(propertyName);
 								return propertyField.getType();
 							}
 							catch (NoSuchFieldException ex)
 							{
 								throw new RuntimeException("Could not find the type of specified container property.",
 										ex);
 							}
 						}
 					}
 				}
 
 				Type propertyType = getPropertyType();
 				if (propertyType.isCollectionType())
 				{
 					/*
 					 * For collection types the Property value is the same type of collection, but containing
 					 * identifiers instead of the actual referenced objects.
 					 */
 					Class<?> returnedClass = propertyType.getReturnedClass();
 					return returnedClass;
 				}
 				else if (propertyType.isAssociationType())
 				{
 					/*
 					 * For association the the property value type is the type of referenced types identifier. Use
 					 * Hibernates ClassMetadata for referenced type and get the type of its identifier.
 					 */
 					// TODO clean, optimize, review, this could be optimized
 					// among similar properties
 					ClassMetadata classMetadata2 = sessionFactory.getClassMetadata(classMetadata.getPropertyType(
 							propertyName).getReturnedClass());
 					return classMetadata2.getIdentifierType().getReturnedClass();
 
 				}
 				else
 				{
 					/*
 					 * For basic fields the Property type is the same as the type in entity class.
 					 */
 					return classMetadata.getPropertyType(propertyName).getReturnedClass();
 				}
 			}
 
 			@SuppressWarnings("unchecked")
 			public Object getValue()
 			{
 
 				// TODO clean, optimize, review
 
 				// Ensure we have an attached pojo
 				if (!sessionFactory.getCurrentSession().contains(pojo))
 				{
 					pojo = (T) sessionFactory.getCurrentSession().get(entityType, (Serializable) getIdForPojo(pojo));
 				}
 
 				if (propertyInEmbeddedKey())
 				{
 					ComponentType idType = (ComponentType) classMetadata.getIdentifierType();
 					String[] propertyNames = idType.getPropertyNames();
 					for (int i = 0; i < propertyNames.length; i++)
 					{
 						String name = propertyNames[i];
 						if (name.equals(propertyName))
 						{
 							Object id = classMetadata.getIdentifier(pojo,
 									(SessionImplementor) sessionFactory.getCurrentSession());
 							return idType.getPropertyValue(id, i, EntityMode.POJO);
 						}
 					}
 				}
 
 				Type propertyType = getPropertyType();
 				Object propertyValue = classMetadata.getPropertyValue(pojo, propertyName);
 
 				if (!propertyType.isAssociationType())
 				{
 					return propertyValue;
 				}
 				else if (propertyType.isCollectionType())
 				{
 					if (propertyValue == null)
 					{
 						return null;
 					}
 
 					/*
 					 * Build a HashSet of identifiers of entities stored in collection.
 					 */
 					HashSet<Serializable> identifiers = new HashSet<Serializable>();
 					Collection<?> pojos = (Collection<?>) propertyValue;
 
 					for (Object object : pojos)
 					{
 						// here, object must be of an association type
 						if (!sessionFactory.getCurrentSession().contains(object))
 						{
 							// ensure a fresh object if session contains the
 							// object
 							object = sessionFactory.getCurrentSession().merge(object);
 						}
 						identifiers.add(sessionFactory.getCurrentSession().getIdentifier(object));
 					}
 					return identifiers;
 				}
 				else
 				{
 					/*
 					 * the return value will be the identifier of referenced object
 					 */
 					if (propertyValue == null)
 					{
 						return null;
 					}
 					Class<?> propertyTypeClass = propertyType.getReturnedClass();
 
 					ClassMetadata classMetadata2 = sessionFactory.getClassMetadata(propertyTypeClass);
 
 					Serializable identifier = classMetadata2.getIdentifier(propertyValue,
 							(SessionImplementor) sessionFactory.getCurrentSession());
 					return identifier;
 				}
 			}
 
 			public boolean isReadOnly()
 			{
 				// TODO
 				return false;
 			}
 
 			public void setReadOnly(boolean newStatus)
 			{
 				throw new UnsupportedOperationException();
 			}
 
 			public void setValue(Object newValue) throws ReadOnlyException, ConversionException
 			{
 
 				try
 				{
 					Object value;
 					try
 					{
 						if (newValue == null || getType().isAssignableFrom(newValue.getClass()))
 						{
 							value = newValue;
 						}
 						else
 						{
 							// Gets the string constructor
 							final Constructor<?> constr = getType().getConstructor(new Class[] { String.class });
 
 							value = constr.newInstance(new Object[] { newValue.toString() });
 						}
 
 						// TODO same optimizations (caching introspection of
 						// types) as in getType and getValue
 						// could be done here.
 
 						if (propertyInEmbeddedKey())
 						{
 							ComponentType idType = (ComponentType) classMetadata.getIdentifierType();
 							String[] propertyNames = idType.getPropertyNames();
 							for (int i = 0; i < propertyNames.length; i++)
 							{
 								String name = propertyNames[i];
 								if (name.equals(propertyName))
 								{
 									Object id = classMetadata.getIdentifier(pojo,
 											(SessionImplementor) sessionFactory.getCurrentSession());
 									Object[] values = idType.getPropertyValues(id, EntityMode.POJO);
 									values[i] = value;
 									idType.setPropertyValues(id, values, EntityMode.POJO);
 								}
 							}
 						}
 						else
 						{
 							Type propertyType = classMetadata.getPropertyType(propertyName);
 							if (propertyType.isCollectionType())
 							{
 								/*
 								 * Value is a collection of identifiers of referenced objects.
 								 */
 								// TODO figure out how to fetch mapped type
 								// properly
 								Field declaredField = entityType.getDeclaredField(propertyName);
 								java.lang.reflect.Type genericType = declaredField.getGenericType();
 								java.lang.reflect.Type[] actualTypeArguments = ((ParameterizedType) genericType)
 										.getActualTypeArguments();
 
 								java.lang.reflect.Type assosiatedType = actualTypeArguments[0];
 								String typestring = assosiatedType.toString().substring(6);
 
 								/*
 								 * Reuse existing persistent collection if possible so Hibernate may optimize queries
 								 * properly.
 								 */
 								@SuppressWarnings("unchecked")
 								Collection<Object> pojoCollection = (Collection<Object>) classMetadata
 										.getPropertyValue(pojo, propertyName);
 								if (pojoCollection == null)
 								{
 									pojoCollection = new HashSet<Object>();
 									classMetadata.setPropertyValue(pojo, propertyName, pojoCollection);
 								}
 								// copy existing set, so we can track which are
 								// to be removed
 								Collection<Object> orphans = new HashSet<Object>(pojoCollection);
 
 								Collection<?> identifiers = (Collection<?>) value;
 								Session session = sessionFactory.getCurrentSession();
 								// add missing objects
 								for (Object id : identifiers)
 								{
 									Object object = session.get(typestring, (Serializable) id);
 									if (!pojoCollection.contains(object))
 									{
 										pojoCollection.add(object);
 									}
 									else
 									{
 										orphans.remove(object);
 									}
 								}
 								// remove the ones that are no more supposed to
 								// be in collection
 								pojoCollection.removeAll(orphans);
 
 							}
 							else if (propertyType.isAssociationType())
 							{
 								/*
 								 * Property value is identifier, convert to the referenced type
 								 */
 								Class<?> referencedType = classMetadata.getPropertyType(propertyName)
 										.getReturnedClass();
 								Object object = sessionFactory.getCurrentSession().get(referencedType,
 										(Serializable) value);
 								classMetadata.setPropertyValue(pojo, propertyName, object);
 								// TODO check if these are needed
 								sessionFactory.getCurrentSession().merge(object);
 								sessionFactory.getCurrentSession().saveOrUpdate(pojo);
 
 							}
 							else
 							{
 								classMetadata.setPropertyValue(pojo, propertyName, value);
 							}
 						}
 						// Persist (possibly) detached pojo
 						@SuppressWarnings("unchecked")
 						T newPojo = (T) sessionFactory.getCurrentSession().merge(pojo);
 						pojo = newPojo;
 
 						fireValueChange();
 
 					}
 					catch (final java.lang.Exception e)
 					{
 						e.printStackTrace();
 						throw new ConversionException(e);
 					}
 
 				}
 				catch (HibernateException e)
 				{
 					e.printStackTrace();
 				}
 			}
 
 			@Override
 			public String toString()
 			{
 				Object v = getValue();
 				if (v != null)
 				{
 					return v.toString();
 				}
 				else
 				{
 					return null;
 				}
 			}
 
 			private class HbnPropertyValueChangeEvent implements Property.ValueChangeEvent
 			{
 				private static final long serialVersionUID = 166764621324404579L;
 
 				public Property<?> getProperty()
 				{
 					return EntityItemProperty.this;
 				}
 			}
 
 			private List<ValueChangeListener> valueChangeListeners;
 
 			private void fireValueChange()
 			{
 				if (valueChangeListeners != null)
 				{
 					HbnPropertyValueChangeEvent event = new HbnPropertyValueChangeEvent();
 					Object[] array = valueChangeListeners.toArray();
 					for (int i = 0; i < array.length; i++)
 					{
 						((ValueChangeListener) array[i]).valueChange(event);
 					}
 				}
 			}
 
 			public void addListener(ValueChangeListener listener)
 			{
 				if (valueChangeListeners == null)
 				{
 					valueChangeListeners = new LinkedList<ValueChangeListener>();
 				}
 				if (!valueChangeListeners.contains(listener))
 				{
 					valueChangeListeners.add(listener);
 				}
 			}
 
 			public void removeListener(ValueChangeListener listener)
 			{
 				if (valueChangeListeners != null)
 				{
 					valueChangeListeners.remove(listener);
 				}
 			}
 
 			@Override
 			public void addValueChangeListener(ValueChangeListener listener)
 			{
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void removeValueChangeListener(ValueChangeListener listener)
 			{
 				// TODO Auto-generated method stub
 
 			}
 
 		}
 	}
 
 	/**
 	 * Constructor creates a new instance of HbnContainer.
 	 */
 	public HbnContainer(Class<T> entityType, SessionFactory sessionFactory)
 	{
 		this.entityType = entityType;
 		this.sessionFactory = sessionFactory;
 		this.classMetadata = sessionFactory.getClassMetadata(entityType);
 	}
 
 	/**
 	 * This is an internal utility method that takes an exception, extracts the stack trace and creates a string from
 	 * the stack trace.
 	 */
 	private String StackToString(Throwable exception)
 	{
 		try
 		{
 			final StringWriter stringWriter = new StringWriter();
 			final PrintWriter printWriter = new PrintWriter(stringWriter);
 			exception.printStackTrace(printWriter);
 			return stringWriter.toString();
 		}
 		catch (Exception e)
 		{
 			logger.error(e.toString());
 			return "Stack Trace Unavailable";
 		}
 	}
 
 	/**
 	 * Adds a new Property to all Items in the Container. The Property ID, data type and default value of the new
 	 * Property are given as parameters. This functionality is optional.
 	 * 
 	 * HbnContainer automatically adds all fields that are mapped by Hibernate to the database. With this method we can
 	 * add a bean property to the container that is contained in the pojo but not hibernate-mapped.
 	 */
 	@Override
 	public boolean addContainerProperty(Object propertyId, Class<?> classType, Object defaultValue)
 			throws UnsupportedOperationException
 	{
 		boolean propertyExists = true;
 
 		try
 		{
 			// Determine if the property exists and if not, determine if it can be created.
 			new MethodProperty<Object>(this.entityType.newInstance(), propertyId.toString());
 		}
 		catch (Exception e)
 		{
 			logger.debug("Note: this is not an error: " + StackToString(e));
 			propertyExists = false;
 		}
 
 		addedProperties.put(propertyId.toString(), classType);
 		return propertyExists;
 	}
 
 	/**
 	 * This is an HbnContainer specific method to persist a newly created entity. This method will trigger a cache clear
 	 * and will also fire an item set change event.
 	 */
 	public Serializable saveEntity(T entity)
 	{
 		final Session session = sessionFactory.getCurrentSession();
 		final Serializable entityId = session.save(entity);
 
 		clearInternalCache();
 		fireItemSetChange();
 
 		return entityId;
 	}
 
 	/**
 	 * This is an HbnContainer specific method to update an entity. This method takes care of updating the cache
 	 * appropriately as well as firing value change events when necessary.
 	 */
 	public Serializable updateEntity(T entity)
 	{
 		final Session session = sessionFactory.getCurrentSession();
 		session.update(entity);
 
 		EntityItem<T> cachedEntity = null;
 		final Serializable entityId = (Serializable) getIdForPojo(entity);
 
 		if (entityCache != null) // Refresh the entity cache
 		{
 			final WeakReference<EntityItem<T>> weakReference = entityCache.get(entityId);
 
 			if (weakReference != null)
 			{
 				cachedEntity = weakReference.get();
 
 				if (cachedEntity != null) // May be already collected but not cleaned
 					cachedEntity.pojo = entity;
 			}
 		}
 
 		if (cachedEntity != null) // If it was in cache it might be rendered
 		{
 			for (Object propertyId : cachedEntity.getItemPropertyIds())
 			{
 				// fire change events on this item, properties might have changed
 				final Property<?> property = cachedEntity.getItemProperty(propertyId);
 
 				if (property instanceof EntityItem.EntityItemProperty)
 				{
 					@SuppressWarnings("rawtypes")
 					final EntityItemProperty entityProperty = (EntityItemProperty) property;
 					entityProperty.fireValueChange();
 				}
 			}
 		}
 
 		return entityId;
 	}
 
 	/**
 	 * Creates a new Item into the Container and assigns it an automatic ID. The new ID is returned, or null if the
 	 * operation fails. After a successful call you can use the getItemmethod to fetch the Item. This functionality is
 	 * optional.
 	 */
 	@Override
 	public Object addItem() throws UnsupportedOperationException
 	{
 		try
 		{
 			final Object entity = entityType.newInstance();
 			final Session session = sessionFactory.getCurrentSession();
 			final Object entityId = session.save(entity);
 
 			clearInternalCache();
 			fireItemSetChange();
 
 			return entityId;
 		}
 		catch (Exception e)
 		{
 			logger.error(StackToString(e));
 			return null;
 		}
 	}
 
 	/**
 	 * Creates a new Item with the given ID in the Container. The new Item is returned, and it is ready to have its
 	 * Properties modified. Returns null if the operation fails or the Container already contains a Item with the given
 	 * ID. This functionality is optional.
 	 * 
 	 * Note that in this implementation we are expecting auto-generated identifiers so this method is not implemented.
 	 */
 	@Override
 	public Item addItem(Object entityId) throws UnsupportedOperationException
 	{
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * Tests if the Container contains the specified Item. Filtering can hide items so that they will not be visible
 	 * through the container API, and this method should respect visibility of items (i.e. only indicate visible items
 	 * as being in the container) if feasible for the container.
 	 */
 	@Override
 	public boolean containsId(Object entityId)
 	{
 		try
 		{
 			final Session session = sessionFactory.getCurrentSession();
 			final Object entity = session.get(entityType, (Serializable) entityId);
 			return (entity != null);
 		}
 		catch (Exception e)
 		{
 			logger.debug(StackToString(e)); // not an error
 			return false;
 		}
 	}
 
 	/**
 	 * Gets the Property identified by the given entityId and propertyId from the Container. If the Container does not
 	 * contain the item or it is filtered out, or the Container does not have the Property, null is returned.
 	 */
 	@Override
 	public Property<?> getContainerProperty(Object entityId, Object propertyId)
 	{
 		try
 		{
 			EntityItem<?> entity = getItem(entityId);
 			Property<?> property = entity.getItemProperty(propertyId);
 			return property;
 		}
 		catch (Exception e)
 		{
 			logger.debug(StackToString(e)); // not an error
 			return null;
 		}
 	}
 
 	/**
 	 * Gets the ID's of all Properties stored in the Container. The ID's cannot be modified through the returned
 	 * collection.
 	 */
 	@Override
 	public Collection<String> getContainerPropertyIds()
 	{
 		Collection<String> propertyIds = getSortableContainerPropertyIds();
 		propertyIds.addAll(addedProperties.keySet());
 		return propertyIds;
 	}
 
 	/**
 	 * This is an HbnContainer specific utility method that is used to retrieve the list of embedded property key
 	 * identifiers.
 	 */
 	private Collection<String> getEmbeddedKeyPropertyIds()
 	{
 		final ArrayList<String> embeddedKeyPropertyIds = new ArrayList<String>();
 		final Type identifierType = classMetadata.getIdentifierType();
 
 		if (identifierType.isComponentType())
 		{
 			final ComponentType idComponent = (ComponentType) identifierType;
 			final String[] propertyNameArray = idComponent.getPropertyNames();
 
 			if (propertyNameArray != null)
 			{
 				final List<String> propertyNames = Arrays.asList(propertyNameArray);
 				embeddedKeyPropertyIds.addAll(propertyNames);
 			}
 		}
 
 		return embeddedKeyPropertyIds;
 	}
 
 	/**
 	 * Gets the Item with the given Item ID from the Container. If the Container does not contain the requested Item,
 	 * null is returned. Containers should not return Items that are filtered out.
 	 */
 	@Override
 	public EntityItem<T> getItem(Object entityId)
 	{
 		return loadEntity((Serializable) entityId);
 	}
 
 	/**
 	 * Gets the ID's of all visible (after filtering and sorting) Items stored in the Container. The ID's cannot be
 	 * modified through the returned collection. If the container is Container.Ordered, the collection returned by this
 	 * method should follow that order. If the container is Container.Sortable, the items should be in the sorted order.
 	 * Calling this method for large lazy containers can be an expensive operation and should be avoided when practical.
 	 * 
 	 * Create an optimized query to return only identifiers. Note that this method does not scale well for large
 	 * database. At least Table is optimized so that it does not call this method.
 	 */
 	@Override
 	public Collection<?> getItemIds()
 	{
 		// TODO: BUG: does not preserve sort order!
 		final Criteria criteria = getCriteria();
 		criteria.setProjection(Projections.id());
 		return criteria.list();
 	}
 
 	/**
 	 * Get numberOfItems consecutive item ids from the container, starting with the item id at startIndex.
 	 * 
 	 * Implementations should return at most numberOfItems item ids, but can contain less if the container has less
 	 * items than required to fulfill the request. The returned list must hence contain all of the item ids from the
 	 * range:
 	 * 
 	 * startIndex to max(startIndex + (numberOfItems-1), container.size()-1).
 	 */
 	@Override
 	public List<?> getItemIds(int startIndex, int count)
 	{
 		final List<?> entityIds = (List<?>) getItemIds();
 		return entityIds.subList(startIndex, startIndex + count);
 	}
 
 	/**
 	 * Gets the data type of all Properties identified by the given Property ID. This method does pretty much the same
 	 * thing as EntityItemProperty#getType()
 	 */
 	public Class<?> getType(Object propertyId)
 	{
 		// TODO: refactor to use same code as EntityItemProperty#getType()
 		// This will also fix incomplete implementation of this method (for association types). Not critical as
 		// Components don't really rely on this methods.
 
 		if (addedProperties.keySet().contains(propertyId))
 			return addedProperties.get(propertyId);
 
 		if (propertyInEmbeddedKey(propertyId))
 		{
 			final ComponentType idType = (ComponentType) classMetadata.getIdentifierType();
 			final String[] propertyNames = idType.getPropertyNames();
 
 			for (int i = 0; i < propertyNames.length; i++)
 			{
 				String name = propertyNames[i];
 				if (name.equals(propertyId))
 				{
 					String idName = classMetadata.getIdentifierPropertyName();
 					try
 					{
 						Field idField = entityType.getDeclaredField(idName);
 						Field propertyField = idField.getType().getDeclaredField((String) propertyId);
 						return propertyField.getType();
 					}
 					catch (NoSuchFieldException ex)
 					{
 						throw new RuntimeException("Could not find the type of specified container property.", ex);
 					}
 				}
 			}
 		}
 
 		Type propertyType = classMetadata.getPropertyType(propertyId.toString());
 		return propertyType.getReturnedClass();
 	}
 
 	/**
 	 * Removes all Items from the Container. Note that Property ID and type information is preserved. This functionality
 	 * is optional.
 	 */
 	@Override
 	public boolean removeAllItems() throws UnsupportedOperationException
 	{
 		try
 		{
 			final Session session = sessionFactory.getCurrentSession();
 			final Query query = session.createQuery("DELETE FROM " + entityType.getSimpleName());
 			int deleted = query.executeUpdate();
 
 			if (deleted > 0)
 			{
 				clearInternalCache();
 				fireItemSetChange();
 			}
 
 			return (size() == 0);
 		}
 		catch (Exception e)
 		{
 			logger.error(StackToString(e));
 			return false;
 		}
 	}
 
 	/**
 	 * Removes a Property specified by the given Property ID from the Container. Note that the Property will be removed
 	 * from all Items in the Container. This functionality is optional.
 	 */
 	@Override
 	public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException
 	{
 		final Class<?> removed = addedProperties.remove(propertyId);
 		return (removed != null);
 	}
 
 	/**
 	 * Removes the Item identified by entityId from the Container. Containers that support filtering should also allow
 	 * removing an item that is currently filtered out. This functionality is optional.
 	 * 
 	 * Note that this method recursively removes all children of this entity before removing this entity.
 	 */
 	@Override
 	public boolean removeItem(Object entityId) throws UnsupportedOperationException
 	{
 		for (Object id : getChildren(entityId))
 			removeItem(id);
 
 		final Session session = sessionFactory.getCurrentSession();
 		final Object entity = session.load(entityType, (Serializable) entityId);
 
 		session.delete(entity);
 
 		clearInternalCache();
 		fireItemSetChange();
 
 		return true;
 	}
 
 	/**
 	 * Gets the number of visible Items in the Container. Filtering can hide items so that they will not be visible
 	 * through the container API.
 	 */
 	@Override
 	public int size()
 	{
 		size = ((Number) getBaseCriteria()
 				.setProjection(Projections.rowCount())
 				.uniqueResult())
 				.intValue();
 
 		return size.intValue();
 	}
 
 	/**
 	 * Adds a new item after the given item. Adding an item after null item adds the item as first item of the ordered
 	 * container. Note that we can't implement properly for database backed container like this so it is unsupported.
 	 */
 	@Override
 	public Object addItemAfter(Object previousEntityId) throws UnsupportedOperationException
 	{
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * Adds a new item after the given item. Adding an item after null item adds the item as first item of the ordered
 	 * container. Note that we can't implement properly for database backed container like this so it is unsupported.
 	 */
 	@Override
 	public Item addItemAfter(Object previousEntityId, Object newEntityId) throws UnsupportedOperationException
 	{
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * Gets the ID of the first Item in the Container.
 	 */
 	@Override
 	public Object firstItemId()
 	{
 		firstId = firstItemId(true);
 		return firstId;
 	}
 
 	/**
 	 * Tests if the Item corresponding to the given Item ID is the first Item in the Container.
 	 */
 	@Override
 	public boolean isFirstId(Object entityId)
 	{
 		return entityId.equals(firstItemId());
 	}
 
 	/**
 	 * Tests if the Item corresponding to the given Item ID is the last Item in the Container.
 	 */
 	@Override
 	public boolean isLastId(Object entityId)
 	{
 		return entityId.equals(lastItemId());
 	}
 
 	/**
 	 * Gets the ID of the last Item in the Container.
 	 */
 	@Override
 	public Object lastItemId()
 	{
 		if (lastId == null)
 		{
 			normalOrder = !normalOrder;
 			lastId = firstItemId(true);
 			normalOrder = !normalOrder;
 		}
 
 		return lastId;
 	}
 
 	/**
 	 * Gets the ID of the Item following the Item that corresponds to entityId. If the given Item is the last or not
 	 * found in the Container, null is returned.
 	 * 
 	 * This is a simple method but it contains a lot of code. The complicated logic is needed to avoid:
 	 * 
 	 * - a large number of database queries - scrolling through a large query result
 	 * 
 	 * This way this container can be used with large data sets.
 	 */
 	@Override
 	public Object nextItemId(Object entityId)
 	{
 		final EntityItem<T> entity = new EntityItem<T>((Serializable) entityId);
 		final List<T> rowBuffer = getRowBuffer();
 
 		try
 		{
 			final int index = rowBuffer.indexOf(entity.getPojo());
 
 			if (index != -1)
 			{
 				final T nextEntity = rowBuffer.get(index + 1);
 				return getIdForPojo(nextEntity);
 			}
 		}
 		catch (Exception e) // not in buffer
 		{
 			// TODO: hackish... refactor...
 		}
 
 		// entityId was not in the row buffer so build query with current order and limit result set with the reference
 		// row. Then first result is next item.
 
 		int currentIndex = indexOfId(entityId);
		int size = size();
 
 		int firstIndex = (normalOrder)
 				? currentIndex + 1
				: size - currentIndex;
		
		if (firstIndex < 0 || firstIndex >= size)
			return null;
 
 		final Criteria criteria = getCriteria()
 				.setFirstResult(firstIndex)
 				.setMaxResults(ROW_BUF_SIZE);
 
 		@SuppressWarnings("unchecked")
 		final List<T> newRowBuffer = criteria.list();
 
 		if (newRowBuffer.size() > 0)
 		{
 			setRowBuffer(newRowBuffer, firstIndex);
 			final T nextPojo = newRowBuffer.get(0);
 			return getIdForPojo(nextPojo);
 		}
 
 		return null;
 	}
 
 	/**
 	 * Gets the ID of the Item preceding the Item that corresponds to entityId. If the given Item is the first or not
 	 * found in the Container, null is returned.
 	 */
 	@Override
 	public Object prevItemId(Object entityId)
 	{
 		normalOrder = !normalOrder;
 		Object previous = nextItemId(entityId);
 		normalOrder = !normalOrder;
 		return previous;
 	}
 
 	/**
 	 * Not supported in HbnContainer. Indexing/order is controlled by underlying database.
 	 */
 	@Override
 	public Object addItemAt(int index) throws UnsupportedOperationException
 	{
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * Not supported in HbnContainer. Indexing/order is controlled by underlying database.
 	 */
 	@Override
 	public Item addItemAt(int index, Object newEntityId) throws UnsupportedOperationException
 	{
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * Get the item id for the item at the position given by index.
 	 */
 	@Override
 	public Object getIdByIndex(int index)
 	{
 		if (indexRowBuffer == null)
 			resetIndexRowBuffer(index);
 
 		int indexInCache = index - indexRowBufferFirstIndex;
 
 		if (!(indexInCache >= 0 && indexInCache < indexRowBuffer.size()))
 		{
 			resetIndexRowBuffer(index);
 			indexInCache = 0;
 		}
 
 		final T pojo = indexRowBuffer.get(indexInCache);
 		final Object id = getIdForPojo(pojo);
 
 		idToIndex.put(id, new Integer(index));
 
 		if (idToIndex.size() > ID_TO_INDEX_MAX_SIZE)
 			idToIndex.remove(idToIndex.keySet().iterator().next());
 
 		return id;
 	}
 
 	/**
 	 * Gets the index of the Item corresponding to the entityId. The following is true for the returned index: 0 <=
 	 * index < size(), or index = -1 if there is no visible item with that id in the container.
 	 * 
 	 * Note! Expects that getIdByIndex is called for this entityId. Otherwise it will be potentially rather slow
 	 * operation with large tables. When used with Table, this shouldn't be a problem.
 	 */
 	@Override
 	public int indexOfId(Object entityId)
 	{
 		final Integer index = idToIndex.get(entityId);
 
 		return (index == null)
 				? slowIndexOfId(entityId)
 				: index;
 	}
 
 	/**
 	 * Gets the container property IDs which can be used to sort the items.
 	 */
 	@Override
 	public Collection<String> getSortableContainerPropertyIds()
 	{
 		final String[] propertyNames = classMetadata.getPropertyNames();
 		final LinkedList<String> propertyIds = new LinkedList<String>();
 
 		propertyIds.addAll(Arrays.asList(propertyNames));
 		propertyIds.addAll(getEmbeddedKeyPropertyIds());
 
 		return propertyIds;
 	}
 
 	/**
 	 * Sort method. Sorts the container items. Sorting a container can irreversibly change the order of its items or
 	 * only change the order temporarily, depending on the container.
 	 * 
 	 * HbnContainer does not actually sort anything here, just clearing cache will do the thing lazily.
 	 */
 	@Override
 	public void sort(Object[] propertyId, boolean[] ascending)
 	{
 		clearInternalCache();
 		orderPropertyIds = propertyId;
 		orderAscendings = ascending;
 	}
 
 	/**
 	 * Remove all active filters from the container.
 	 */
 	@Override
 	public void removeAllContainerFilters()
 	{
 		if (filters != null)
 		{
 			filters = null;
 			clearInternalCache();
 			fireItemSetChange();
 		}
 	}
 
 	/**
 	 * HbnContainer only supports old style addContainerFilter(Object, String, boolean booblean) API and
 	 * {@link SimpleStringFilter}. Support for this newer API maybe in upcoming versions.
 	 * 
 	 * Also note that for complex filtering it is possible to override {@link #getBaseCriteria()} method and add filter
 	 * so the query directly.
 	 */
 	// TODO support new filtering api properly
 	@Override
 	public void addContainerFilter(Filter filter) throws UnsupportedFilterException
 	{
 		if (!(filter instanceof SimpleStringFilter))
 		{
 			final String message = "HbnContainer only supports old style addContainerFilter(Object, String, boolean booblean) API";
 			throw new UnsupportedFilterException(message);
 		}
 
 		final SimpleStringFilter sf = (SimpleStringFilter) filter;
 		final String filterString = sf.getFilterString();
 		final Object propertyId = sf.getPropertyId();
 		final boolean ignoreCase = sf.isIgnoreCase();
 		final boolean onlyMatchPrefix = sf.isOnlyMatchPrefix();
 
 		addContainerFilter(propertyId, filterString, ignoreCase, onlyMatchPrefix);
 	}
 
 	/**
 	 * Finds the identifiers for the children of the given item. The returned collection is unmodifiable.
 	 */
 	@Override
 	public Collection<?> getChildren(Object entityId)
 	{
 		final ArrayList<Object> children = new ArrayList<Object>();
 		parentPropertyName = getParentPropertyName();
 
 		if (parentPropertyName == null)
 			return children;
 
 		for (Object id : getItemIds())
 		{
 			EntityItem<T> entity = getItem(id);
 			Property<?> property = entity.getItemProperty(parentPropertyName);
 			Object value = property.getValue();
 
 			if (entityId.equals(value))
 				children.add(id);
 		}
 
 		return children;
 	}
 
 	/**
 	 * Gets the identifier of the given item's parent. If there is no parent or we are unable to infer the name of the
 	 * parent property this method will return null.
 	 */
 	@Override
 	public Object getParent(Object entityId)
 	{
 		parentPropertyName = getParentPropertyName();
 
 		if (parentPropertyName == null)
 		{
 			logger.warn("failed to find a parent property name; hierarchy may be incomplete.");
 			return null;
 		}
 
 		final EntityItem<T> entity = getItem(entityId);
 		final Property<?> property = entity.getItemProperty(parentPropertyName);
 		final Object value = property.getValue();
 
 		return value;
 	}
 
 	/**
 	 * Gets the IDs of all Items in the container that don't have a parent. Such items are called root Items. The
 	 * returned collection is unmodifiable.
 	 */
 	@Override
 	public Collection<?> rootItemIds()
 	{
 		final ArrayList<Object> rootItems = new ArrayList<Object>();
 		parentPropertyName = getParentPropertyName();
 
 		if (parentPropertyName == null)
 		{
 			logger.warn("failed to find a parent property name; hierarchy may be incomplete.");
 			return rootItems;
 		}
 		
 		final Collection<?> allItemIds = getItemIds();
 
 		for (Object id : allItemIds)
 		{
 			EntityItem<T> entity = getItem(id);
 			Property<?> property = entity.getItemProperty(parentPropertyName);
 			Object value = property.getValue();
 
 			if (value == null)
 				rootItems.add(id);
 		}
 
 		return rootItems;
 	}
 
 	/**
 	 * Sets the parent of an Item. The new parent item must exist and be able to have children. (
 	 * areChildrenAllowed(Object) == true ). It is also possible to detach a node from the hierarchy (and thus make it
 	 * root) by setting the parent null. This operation is optional.
 	 */
 	@Override
 	public boolean setParent(Object entityId, Object newParentId)
 	{
 		parentPropertyName = getParentPropertyName();
 
 		if (parentPropertyName == null)
 		{
 			logger.warn("failed to find a parent property name; unable to set the parent.");
 			return false;
 		}
 
 		final EntityItem<T> item = getItem(entityId);
 		final Property<?> property = item.getItemProperty(parentPropertyName);
 
 		property.setValue(newParentId);
 
 		final Object value = property.getValue();
 		return (value.equals(newParentId));
 	}
 
 	/**
 	 * Tests if the Item with given ID can have children.
 	 */
 	@Override
 	public boolean areChildrenAllowed(Object entityId)
 	{
 		parentPropertyName = getParentPropertyName();
 		return (parentPropertyName != null && containsId(entityId));
 	}
 
 	/**
 	 * Sets the given Item's capability to have children. If the Item identified with entityId already has children and
 	 * areChildrenAllowed(Object) is false this method fails and false is returned.
 	 * 
 	 * The children must be first explicitly removed with setParent(Object entityId, Object newParentId)or
 	 * com.vaadin.data.Container.removeItem(Object entityId).
 	 * 
 	 * This operation is optional. If it is not implemented, the method always returns false.
 	 */
 	@Override
 	public boolean setChildrenAllowed(Object entityId, boolean areChildrenAllowed)
 	{
 		return false;
 	}
 
 	/**
 	 * Tests if the Item specified with entityId is a root Item. The hierarchical container can have more than one root
 	 * and must have at least one unless it is empty. The getParent(Object entityId) method always returns null for root
 	 * Items.
 	 */
 	@Override
 	public boolean isRoot(Object entityId)
 	{
 		parentPropertyName = getParentPropertyName();
 
 		if (parentPropertyName == null)
 		{
 			logger.warn("failed to find a parent property name; hierarchy may be incomplete.");
 			return false;
 		}
 
 		final EntityItem<T> item = getItem(entityId);
 		final Property<?> property = item.getItemProperty(parentPropertyName);
 
 		final Object value = property.getValue();
 		return (value == null);
 	}
 
 	/**
 	 * Tests if the Item specified with entityId has child Items or if it is a leaf. The getChildren(Object entityId)
 	 * method always returns null for leaf Items.
 	 * 
 	 * Note that being a leaf does not imply whether or not an Item is allowed to have children.
 	 */
 	@Override
 	public boolean hasChildren(Object entityId)
 	{
 		parentPropertyName = getParentPropertyName();
 
 		if (parentPropertyName == null)
 		{
 			logger.warn("failed to find a parent property name; hierarchy may be incomplete.");
 			return false;
 		}
 
 		for (Object id : getItemIds())
 		{
 			EntityItem<T> item = getItem(id);
 			Property<?> property = item.getItemProperty(parentPropertyName);
 			Object value = property.getValue();
 
 			if (entityId.equals(value))
 				return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Adds an Item set change listener for the object.
 	 */
 	@Override
 	public void addItemSetChangeListener(ItemSetChangeListener listener)
 	{
 		if (itemSetChangeListeners == null)
 			itemSetChangeListeners = new LinkedList<ItemSetChangeListener>();
 
 		itemSetChangeListeners.add(listener);
 	}
 
 	/**
 	 * Removes the Item set change listener from the object.
 	 */
 	@Override
 	public void removeItemSetChangeListener(ItemSetChangeListener listener)
 	{
 		if (itemSetChangeListeners != null)
 			itemSetChangeListeners.remove(listener);
 	}
 
 	/**
 	 * Adds an Item set change listener for the object. This method is deprecated. You should use
 	 * addItemSetChangeListener() instead.
 	 */
 	@Override
 	@Deprecated
 	public void addListener(ItemSetChangeListener listener)
 	{
 		addItemSetChangeListener(listener);
 	}
 
 	/**
 	 * Removes the Item set change listener from the object. This method is deprecated. You should use
 	 * addItemSetChangeListener() instead.
 	 */
 	@Override
 	@Deprecated
 	public void removeListener(ItemSetChangeListener listener)
 	{
 		removeItemSetChangeListener(listener);
 	}
 
 	//
 	// UTILITY METHODS
 	//
 
 	/**
 	 * This is an internal HbnContainer utility method. Determines if a property is contained within an embedded key.
 	 */
 	private boolean propertyInEmbeddedKey(Object propertyId)
 	{
 		// TODO: combine with the same method from EntityItemProperty
 
 		final Type identifierType = classMetadata.getIdentifierType();
 
 		if (identifierType.isComponentType())
 		{
 			final ComponentType componentType = (ComponentType) identifierType;
 			final String[] idPropertyNames = componentType.getPropertyNames();
 			final List<String> idPropertyNameList = Arrays.asList(idPropertyNames);
 			return idPropertyNameList.contains(propertyId);
 		}
 
 		return false;
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method. Fetches entities by identifier. Override this if you need to
 	 * customize a query for EntityItems.
 	 */
 	protected EntityItem<T> loadEntity(Serializable entityId)
 	{
 		if (entityId == null)
 			return null;
 
 		cleanCache();
 
 		EntityItem<T> entity;
 		WeakReference<EntityItem<T>> weakReference = entityCache.get(entityId);
 
 		if (weakReference != null)
 		{
 			entity = weakReference.get();
 
 			if (entity != null)
 				return entity;
 		}
 
 		entity = new EntityItem<T>(entityId);
 		entityCache.put(entityId, new WeakReference<EntityItem<T>>(entity));
 
 		return entity;
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method. This method triggers events associated with the
 	 * ItemSetChangeListener.
 	 */
 	private void fireItemSetChange()
 	{
 		if (itemSetChangeListeners != null)
 		{
 			final Object[] changeListeners = itemSetChangeListeners.toArray();
 
 			final Container.ItemSetChangeEvent changeEvent = new Container.ItemSetChangeEvent()
 			{
 				private static final long serialVersionUID = -3002746333251784195L;
 
 				public Container getContainer()
 				{
 					return HbnContainer.this;
 				}
 			};
 
 			for (int i = 0; i < changeListeners.length; i++)
 			{
 				ItemSetChangeListener changeListener = (ItemSetChangeListener) changeListeners[i];
 				changeListener.containerItemSetChange(changeEvent);
 			}
 		}
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method. Gets a base listing using current ordering criteria.
 	 */
 	private Criteria getCriteria()
 	{
 		final Criteria criteria = getBaseCriteria();
 		final List<Order> orders = getOrder(!normalOrder);
 
 		for (Order order : orders)
 		{
 			criteria.addOrder(order);
 		}
 
 		return criteria;
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method. Return the ordering criteria in the order in which they should be
 	 * applied. The composed order must be stable and must include {@link #getNaturalOrder(boolean)} at the end.
 	 */
 	protected final List<Order> getOrder(boolean flipOrder)
 	{
 		final List<Order> orders = new ArrayList<Order>();
 		orders.addAll(getDefaultOrder(flipOrder));
 		orders.add(getNaturalOrder(flipOrder));
 		return orders;
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method. Returns the ordering to use for the container contents. The
 	 * default implementation provides the {@link Container.Sortable} functionality. Can be overridden to customize item
 	 * sort order.
 	 */
 	protected List<Order> getDefaultOrder(boolean flipOrder)
 	{
 		final List<Order> orders = new ArrayList<Order>();
 
 		if (orderPropertyIds != null)
 		{
 			for (int i = 0; i < orderPropertyIds.length; i++)
 			{
 				String propertyId = orderPropertyIds[i].toString();
 
 				if (propertyInEmbeddedKey(propertyId))
 					propertyId = classMetadata.getIdentifierPropertyName() + "." + propertyId;
 
 				boolean ascending = (flipOrder)
 						? !orderAscendings[i]
 						: orderAscendings[i];
 
 				Order order = (ascending)
 						? Order.asc(propertyId)
 						: Order.desc(propertyId);
 
 				orders.add(order);
 			}
 		}
 
 		return orders;
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method. Creates the base criteria for entity class and add possible
 	 * restrictions to query. This method is protected so developers can add their own custom criteria.
 	 */
 	protected Criteria getBaseCriteria()
 	{
 		final Session session = sessionFactory.getCurrentSession();
 		Criteria criteria = session.createCriteria(entityType);
 
 		if (filters != null)
 		{
 			for (ContainerFilter filter : filters)
 			{
 				String idName = null;
 
 				if (propertyInEmbeddedKey(filter.getPropertyId()))
 					idName = classMetadata.getIdentifierPropertyName();
 
 				criteria = criteria.add(filter.getCriterion(idName));
 			}
 		}
 
 		return criteria;
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method. Natural order is the order in which the database is sorted if
 	 * container has no other ordering set. Natural order is always added as least significant order to queries. This is
 	 * needed to keep items stable order across queries. The default implementation sorts entities by identifier column.
 	 */
 	protected Order getNaturalOrder(boolean flipOrder)
 	{
 		final String propertyName = getIdPropertyName();
 		return (flipOrder) ? Order.desc(propertyName) : Order.asc(propertyName);
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method to implement {@link #firstItemId()} and {@link #lastItemId()}.
 	 */
 	protected Object firstItemId(boolean bypassCache)
 	{
 		if (bypassCache)
 		{
 			Object first = getCriteria()
 					.setMaxResults(1)
 					.setCacheable(true)
 					.uniqueResult();
 
 			final Object entityId = getIdForPojo(first);
 			idToIndex.put(entityId, normalOrder ? 0 : size() - 1);
 
 			return entityId;
 		}
 
 		return firstItemId();
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method to detect identifier of given entity object.
 	 */
 	private Object getIdForPojo(Object pojo)
 	{
 		final Session session = sessionFactory.getCurrentSession();
 		return classMetadata.getIdentifier(pojo, (SessionImplementor) session);
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method. RowBuffer stores a list of entity items to avoid excessive number
 	 * of DB queries.
 	 */
 	private List<T> getRowBuffer()
 	{
 		return (normalOrder) ? ascRowBuffer : descRowBuffer;
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method. RowBuffer stores some pojos to avoid excessive number of DB
 	 * queries. Also updates the idToIndex map.
 	 */
 	private void setRowBuffer(List<T> list, int firstIndex)
 	{
 		if (normalOrder)
 		{
 			ascRowBuffer = list;
 
 			for (int i = 0; i < list.size(); ++i)
 			{
 				idToIndex.put(getIdForPojo(list.get(i)), firstIndex + i);
 			}
 		}
 		else
 		{
 			descRowBuffer = list;
 			final int lastIndex = size() - 1;
 
 			for (int i = 0; i < list.size(); ++i)
 			{
 				idToIndex.put(getIdForPojo(list.get(i)), lastIndex - firstIndex - i);
 			}
 		}
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method that gets the property name of the identifier.
 	 */
 	private String getIdPropertyName()
 	{
 		return classMetadata.getIdentifierPropertyName();
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method to query new set of entity items to cache from given index.
 	 */
 	@SuppressWarnings("unchecked")
 	private void resetIndexRowBuffer(int index)
 	{
 		indexRowBufferFirstIndex = index;
 		indexRowBuffer = getCriteria().setFirstResult(index).setMaxResults(ROW_BUF_SIZE).list();
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method that gets the index of the given identifier.
 	 */
 	private int slowIndexOfId(Object entityId)
 	{
 		final Criteria criteria = getCriteria().setProjection(Projections.id());
 		final List<?> list = criteria.list();
 		return list.indexOf(entityId);
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method. Adds container filter for hibernate mapped property. For property
 	 * not mapped by Hibernate.
 	 */
 	public void addContainerFilter(Object propertyId, String filterString, boolean ignoreCase, boolean onlyMatchPrefix)
 	{
 		addContainerFilter(new StringContainerFilter(propertyId, filterString, ignoreCase, onlyMatchPrefix));
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method that adds a container filter.
 	 */
 	public void addContainerFilter(ContainerFilter containerFilter)
 	{
 		if (addedProperties.containsKey(containerFilter.getPropertyId()))
 		{
 			final String message = "HbnContainer does not support filtering properties not mapped by Hibernate";
 			throw new UnsupportedOperationException(message);
 		}
 
 		if (filters == null)
 			filters = new HashSet<ContainerFilter>();
 
 		filters.add(containerFilter);
 
 		clearInternalCache();
 		fireItemSetChange();
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method that removes container filters for the given property identifier.
 	 */
 	public void removeContainerFilters(Object propertyId)
 	{
 		if (filters != null)
 		{
 			for (Iterator<ContainerFilter> iterator = filters.iterator(); iterator.hasNext();)
 			{
 				ContainerFilter containerFilter = iterator.next();
 
 				if (containerFilter.getPropertyId().equals(propertyId))
 					iterator.remove();
 			}
 
 			clearInternalCache();
 			fireItemSetChange();
 		}
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method that removes the given container filter.
 	 */
 	@Override
 	public void removeContainerFilter(Filter filter)
 	{
 		// TODO support new filtering api properly
 		// TODO the workaround for SimpleStringFilter works wrong, but hopefully will be good enough for now
 
 		if (filter instanceof SimpleStringFilter)
 		{
 			final SimpleStringFilter sf = (SimpleStringFilter) filter;
 			final Object propertyId = sf.getPropertyId();
 			removeContainerFilters(propertyId);
 		}
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method that infers the name of the parent field belonging to the current
 	 * property based on type.
 	 */
 	private String getParentPropertyName()
 	{
 		// TODO: make this a little more robust, there are a number of cases where this will fail.
 
 		if (parentPropertyName == null)
 		{
 			String[] propertyNames = classMetadata.getPropertyNames();
 
 			for (int i = 0; i < propertyNames.length; ++i)
 			{
 				String entityTypeName = entityType.getName();
 				String propertyTypeName = classMetadata.getPropertyType(propertyNames[i]).getName();
 
 				if (entityTypeName.equals(propertyTypeName))
 				{
 					parentPropertyName = propertyNames[i];
 					break;
 				}
 			}
 		}
 
 		return parentPropertyName;
 	}
 
 	//
 	// CACHE RELATED METHODS
 	//
 
 	/**
 	 * This is an internal HbnContainer utility method. Cleans the entityCache of collected item references. This method
 	 * runs occasionally by {@link #loadEntity(Serializable)}, but may be run manually too.
 	 * 
 	 * <p>
 	 * TODO figure out if this is the best possible way to free the memory consumed by (empty) weak references and open
 	 * this mechanism for extension
 	 */
 	private void cleanCache()
 	{
 		// TODO: substitute a Google Guava cache for this home-grown alternative.
 
 		if (++loadCount % REFERENCE_CLEANUP_INTERVAL == 0)
 		{
 			final Set<Entry<Object, WeakReference<EntityItem<T>>>> entries = entityCache.entrySet();
 			Iterator<Entry<Object, WeakReference<EntityItem<T>>>> iterator;
 
 			for (iterator = entries.iterator(); iterator.hasNext();)
 			{
 				Entry<Object, WeakReference<EntityItem<T>>> entry = iterator.next();
 
 				if (entry.getValue().get() == null)
 				{
 					// if the referenced entityitem is carbage collected, remove the weak reference itself
 					iterator.remove();
 				}
 			}
 		}
 	}
 
 	/**
 	 * This is an internal HbnContainer utility method to clear all cache fields.
 	 */
 	protected void clearInternalCache()
 	{
 		idToIndex.clear();
 		indexRowBuffer = null;
 		ascRowBuffer = null;
 		descRowBuffer = null;
 		firstId = null;
 		lastId = null;
 		size = null;
 	}
 
 }
