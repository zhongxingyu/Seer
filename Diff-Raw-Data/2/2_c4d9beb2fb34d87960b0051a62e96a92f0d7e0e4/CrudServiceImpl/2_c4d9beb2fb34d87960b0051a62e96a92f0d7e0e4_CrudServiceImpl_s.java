 /**
  * Copyright 2011 Henric Persson (henric.persson@gmail.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package burrito.services;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import siena.Id;
 import siena.Model;
 import siena.Query;
 import siena.SienaException;
 import burrito.Configurator;
 import burrito.ValidationException;
 import burrito.annotations.AdminLink;
 import burrito.annotations.Cloneable;
 import burrito.annotations.DefaultSort;
 import burrito.annotations.Displayable;
 import burrito.annotations.EmbeddedBy;
 import burrito.annotations.Hidden;
 import burrito.annotations.Image;
 import burrito.annotations.ImageKey;
 import burrito.annotations.Link;
 import burrito.annotations.ListedBy;
 import burrito.annotations.ListedByEnum;
 import burrito.annotations.LongText;
 import burrito.annotations.ReadOnly;
 import burrito.annotations.RedundantForPerformance;
 import burrito.annotations.RegexpValidation;
 import burrito.annotations.Relation;
 import burrito.annotations.Required;
 import burrito.annotations.RichText;
 import burrito.annotations.SearchableField;
 import burrito.annotations.Unique;
 import burrito.client.crud.CrudGenericException;
 import burrito.client.crud.CrudNameIdPair;
 import burrito.client.crud.CrudService;
 import burrito.client.crud.FieldValueNotUniqueException;
 import burrito.client.crud.generic.CrudEntityDescription;
 import burrito.client.crud.generic.CrudEntityInfo;
 import burrito.client.crud.generic.CrudEntityList;
 import burrito.client.crud.generic.CrudField;
 import burrito.client.crud.generic.fields.AdminLinkMethodField;
 import burrito.client.crud.generic.fields.BooleanField;
 import burrito.client.crud.generic.fields.DateField;
 import burrito.client.crud.generic.fields.DisplayableMethodField;
 import burrito.client.crud.generic.fields.EmbeddedListField;
 import burrito.client.crud.generic.fields.ImageField;
 import burrito.client.crud.generic.fields.IntegerField;
 import burrito.client.crud.generic.fields.IntegerListField;
 import burrito.client.crud.generic.fields.LinkListField;
 import burrito.client.crud.generic.fields.LinkedEntityField;
 import burrito.client.crud.generic.fields.ListedByEnumField;
 import burrito.client.crud.generic.fields.LongField;
 import burrito.client.crud.generic.fields.LongListField;
 import burrito.client.crud.generic.fields.ManyToManyRelationField;
 import burrito.client.crud.generic.fields.ManyToOneRelationField;
 import burrito.client.crud.generic.fields.RichTextField;
 import burrito.client.crud.generic.fields.StringField;
 import burrito.client.crud.generic.fields.StringListField;
 import burrito.client.crud.generic.fields.StringSelectionField;
 import burrito.client.widgets.panels.table.ItemCollection;
 import burrito.client.widgets.panels.table.PageMetaData;
 import burrito.links.Linkable;
 import burrito.sitelet.Sitelet;
 import burrito.util.ValidationUtil;
 
 import com.google.gson.Gson;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * Service that deals with all the serialization/deserialization of objects from
  * and to admin.
  * 
  * Basically: a _lot_ of reflection.
  * 
  * @author henper
  * 
  */
 public class CrudServiceImpl extends RemoteServiceServlet implements
 		CrudService {
 
 	private static final long serialVersionUID = 1L;
 
 	private SearchManager searchManager = SearchManager.get();
 	private PluginCrudManager pluginManager = PluginCrudManager.get();
 
 	@SuppressWarnings("unchecked")
 	public List<CrudNameIdPair> getListValues(String entityName) {
 		Class<? extends Model> clazz = extractClass(entityName);
 		List<Model> all;
 		try {
 			all = (List<Model>) clazz.getMethod("listValues").invoke(null);
 		} catch (Exception e) {
 			all = (List<Model>) Model.all(clazz).fetch();
 		}
 		List<CrudNameIdPair> result = new ArrayList<CrudNameIdPair>(all.size());
 		for (Model entity : all) {
 			result.add(new CrudNameIdPair(extractIDFromEntity(entity), entity
 					.toString()));
 		}
 		return result;
 	}
 
 	public List<String> getEnumListValues(String type) {
 		Class<?> clazz;
 		try {
 			clazz = Class.forName(type);
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException("Unknown enum class: " + type);
 		}
 		List<String> enums = new ArrayList<String>();
 		for (Object o : clazz.getEnumConstants()) {
 			enums.add(((Enum<?>) o).name());
 		}
 		return enums;
 	}
 
 	public CrudEntityDescription getEntityHeaders(String entityName) {
 		Class<? extends Model> clazz = extractClass(entityName);
 		Model fakeEntity = extractEntity(-1l, null, clazz);
 		CrudEntityDescription desc = new CrudEntityDescription();
 		desc.setCloneable(clazz.isAnnotationPresent(Cloneable.class));
 		ArrayList<CrudField> result = new ArrayList<CrudField>();
 		ArrayList<CrudField> delayed = new ArrayList<CrudField>();
 		//find all Displayable methods
 		for (Method method : clazz.getDeclaredMethods()) {
 			Displayable dispAnn = method.getAnnotation(Displayable.class);
 			if (dispAnn != null) {
 				CrudField crudField = new DisplayableMethodField();
 				crudField.setName(method.getName());
 				if (dispAnn.last()) {
 					//delay it
 					delayed.add(crudField);
 				} else {
 					result.add(crudField);
 				}
 			}
 		}
 		//find all Displayable fields
 		for (Field field : clazz.getDeclaredFields()) {
 			Displayable dispAnn = field.getAnnotation(Displayable.class);
 			if (dispAnn != null) {
 				try {
 					CrudField crudField = createCrudField(field, fakeEntity);
 					if (dispAnn.last()) {
 						delayed.add(crudField);
 					} else {
 						result.add(crudField);
 					}
 				} catch (Exception e) {
 					throw new RuntimeException("Failed to create crud field", e);
 				}
 			}
 		}
 		
 		//add all delayed fields
 		result.addAll(delayed);
 		
 		//find all AdminLinks
 		for (Method method : clazz.getDeclaredMethods()) {
 			AdminLink linkAnn = method.getAnnotation(AdminLink.class);
 			if (linkAnn != null) {
 				CrudField crudField = new AdminLinkMethodField();
 				crudField.setName(method.getName());
 				result.add(crudField);
 			}
 		}
 		
 		desc.setFields(result);
 		desc.setEntityName(entityName);
 		return desc;
 	}
 
 	public List<CrudEntityInfo> getAllEntities() {
 		List<CrudEntityInfo> result = new ArrayList<CrudEntityInfo>();
 		for (Class<? extends Model> clazz : Configurator.crudables) {
 			result.add(new CrudEntityInfo(clazz.getName()));
 		}
 		return result;
 	}
 
 	public Boolean isCrudEnabled(String className) {
 		for (Class<? extends Model> clazz : Configurator.crudables) {
 			if (clazz.getName().equals(className)) {
 				return Boolean.TRUE;
 			}
 		}
 		return Boolean.FALSE;
 	}
 
 	public List<String> getLinkableTypes() {
 		List<String> result = new ArrayList<String>();
 		for (Class<? extends Linkable> clazz : Configurator.linkables) {
 			result.add(clazz.getName());
 		}
 		return result;
 	}
 
 	public void deleteEntities(List<CrudEntityDescription> selected) {
 		for (CrudEntityDescription crud : selected) {
 			Class<? extends Model> clazz = extractClass(crud.getEntityName());
 			Long entityId = crud.getId();
 			Model entity = extractEntity(entityId, null, clazz);
 			searchManager.deleteSearchEntry(entity, entityId);
 			cascadeDeleteRelations(entity);
 			entity.delete();
 		}
 	}
 
 	private void cascadeDeleteRelations(Model toBeDeleted) {
 		// Searches model entities to find relations to the object about to be
 		// deleted
 		Long toBeDeletedId = extractIDFromEntity(toBeDeleted);
 		for (Class<? extends Model> clazz : Configurator.crudables) {
 			for (Field field : clazz.getFields()) {
 				if (field.isAnnotationPresent(Relation.class)) {
 					Relation relation = field.getAnnotation(Relation.class);
 					boolean isCorrectClass = relation.value().equals(
 							toBeDeleted.getClass());
 					if (isCorrectClass) {
 						// Get relateted class values based on the id from the
 						// deleted entity
 						List<? extends Model> relateds = Model.all(clazz)
 								.filter(field.getName(), toBeDeletedId).fetch();
 						for (Model related : relateds) {
 							try {
 								if (field.getType() == Long.class) {
 									field.set(related, null);
 									related.update();
 								} else if (field.getType() == List.class) {
 									@SuppressWarnings("unchecked")
 									List<Long> list = (List<Long>) field
 											.get(related);
 									if (list != null) {
 										list.remove(toBeDeletedId);
 										related.update();
 									}
 								}
 							} catch (Exception e) {
 								throw new RuntimeException(
 										"Failed to update related entity. This could mean that the database is inconsistent. ",
 										e);
 							}
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 	public CrudEntityList listEntities(String filter, String entityName,
 			PageMetaData<String> p) {
 		Class<? extends Model> clazz = extractClass(entityName);
 
 		if (filter != null) {
 			return search(clazz, filter, p);
 		}
 
 		// Prepare query object
 		@SuppressWarnings("unchecked")
 		Query<Model> q = (Query<Model>) Model.all(clazz);
 
 		// Get a list of entities from the current page:
 		String order = null;
 		if (p.getSortKey() != null) {
 			order = p.getSortKey();
 			if (!p.isAscending()) {
 				order = "-" + order;
 			}
 		}
 		if (order != null) {
 			q.order(order);
 		}
 		// If a filter is available, use it!
 		// if (filter != null) {
 		// // Using a technique found here:
 		// //
 		// http://code.google.com/appengine/docs/python/datastore/queriesandindexes.html#Introducing_Indexes
 		// q.filter(filter.getName() + " >=", filter.getValue());
 		// q.filter(filter.getName() + " <", filter.getValue() + "\ufffd");
 		// }
 		List<Model> entities = q.fetch(p.getItemsPerPage(),
 				(int) p.getRangeStart());
 
 		// Check if there is a next page by fetching the first entity from the
 		// next page:
 		boolean hasNextPage = Model.all(clazz).fetch(1, (int) p.getRangeEnd())
 				.size() == 1;
 		CrudEntityList collection = new CrudEntityList();
 		collection.setItems(convertEntitesToCrudEntityDescriptions(entities));
 		collection.setPage(p.getPage());
 		collection.setHasNextPage(hasNextPage);
 		collection.setItemsPerPage(p.getItemsPerPage());
 		return collection;
 	}
 
 	private CrudEntityList search(Class<? extends Model> clazz, String filter,
 			PageMetaData<String> p) {
 
 		ItemCollection<SearchEntry> entries = searchManager.search(clazz,
 				filter, p);
 		List<Model> entities = new ArrayList<Model>();
 		for (SearchEntry entry : entries) {
 			Model entity = extractEntity(entry.ownerId, null, clazz);
 			if (entity != null)
 				entities.add(entity);
 		}
 		CrudEntityList collection = new CrudEntityList();
 		collection.setItems(convertEntitesToCrudEntityDescriptions(entities));
 		collection.setPage(p.getPage());
 		collection.setHasNextPage(entries.isHasNextPage());
 		collection.setItemsPerPage(p.getItemsPerPage());
 		return collection;
 	}
 
 	private List<CrudEntityDescription> convertEntitesToCrudEntityDescriptions(
 			List<Model> entities) {
 		List<CrudEntityDescription> result = new ArrayList<CrudEntityDescription>();
 		for (Model entity : entities) {
 			result.add(createEntityDescription(entity.getClass().getName(),
 					extractIDFromEntity(entity), entity.getClass(), entity));
 		}
 		return result;
 	}
 
 	private Long extractIDFromEntity(Model entity) {
 		try {
 			Field id = entity.getClass().getDeclaredField("id");
 			id.setAccessible(true);
 			return (Long) id.get(entity);
 		} catch (Exception e) {
 			throw new RuntimeException("Failed to get id field from entity", e);
 		}
 	}
 
 	public Long save(CrudEntityDescription desc) throws FieldValueNotUniqueException, CrudGenericException {
 		Class<? extends Model> clazz = extractClass(desc.getEntityName());
 		Model entity = extractEntity(desc.getId(), null, clazz);
 		updateEntityFromDescription(entity, desc, clazz);
 		validateEntityUniqueness(entity, desc, clazz);
 
 		try {
 			if (desc.isNew()) {
 				entity.insert();
 			} else {
 				entity.update();
 				if (entity instanceof Sitelet) {
 					SiteletProperties prop = SiteletProperties.getByEntityId(desc.getId());
 					prop.triggerRefreshAsync();
 				}
 			}
 	
 			Long id = extractIDFromEntity(entity);
 			updateSearchIndicies(entity, id);
 	
 			return id;
 		} catch (SienaException e) {
 			ValidationException ve = ValidationUtil.getValidationErrorOrRethrow(e);
 			throw new CrudGenericException(ve.getMessage());
 		}
 	}
 
 	private void updateSearchIndicies(Model entity, Long databaseId) {
 		searchManager.insertOrUpdateSearchEntry(entity, databaseId);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void updateEntityFromDescription(Object entity,
 			CrudEntityDescription desc, Class<?> clazz) {
 		for (CrudField field : desc.getFields()) {
 			try {
 				Object value = field.getValue();
 				if (field instanceof EmbeddedListField) {
 					value = deserializeEmbedded(
 							((EmbeddedListField) field).getEmbeddedClassName(),
 							(List<CrudEntityDescription>) field.getValue());
 				}
 				
 				Field privField = clazz.getDeclaredField(field.getName());
 				@SuppressWarnings("rawtypes")
 				Class fieldType = privField.getType();
 				
				if (field instanceof ListedByEnumField && (Enum.class.isAssignableFrom(fieldType))) {
 					ListedByEnumField fieldEnum = (ListedByEnumField) field;
 					String className = fieldEnum.getTypeClassName();
 					@SuppressWarnings("rawtypes")
 					Class enumClass = Class.forName(className);
 					
 					value = Enum.valueOf(enumClass, (String) value);
 				}
 				
 				
 				privField.setAccessible(true);
 				privField.set(entity, value);
 
 			} catch (Exception e) {
 				throw new RuntimeException("Failed to set field "
 						+ field.getName(), e);
 			}
 		}
 	}
 
 	private void validateEntityUniqueness(Model entity, CrudEntityDescription desc, Class<?> clazz) throws FieldValueNotUniqueException {
 		Object id;
 
 		try {
 			Field field = clazz.getDeclaredField("id");
 			field.setAccessible(true);
 			id = field.get(entity);
 		}
 		catch (Exception e) {
 			throw new RuntimeException("Failed to get entity ID", e);
 		}
 
 		for (CrudField field : desc.getFields()) {
 			String fieldName = field.getName();
 			try {
 				Field privField = clazz.getDeclaredField(fieldName);
 				if (privField.isAnnotationPresent(Unique.class)) {
 					privField.setAccessible(true);
 					Object value = privField.get(entity);
 					if (value != null) {
 						Model existing = Model.all(entity.getClass()).filter(fieldName, value).get();
 						if (existing != null) {
 							if (!existing.getClass().getField("id").get(existing).equals(id)) {
 								throw new FieldValueNotUniqueException(fieldName);
 							}
 						}
 					}
 				}
 			}
 			catch (FieldValueNotUniqueException e) {
 				throw e; // pass it on
 			}
 			catch (Exception e) {
 				throw new RuntimeException("Failed to validate uniqueness for field " + field.getName(), e);
 			}
 		}
 	}
 
 	private List<String> deserializeEmbedded(String embeddedClassName,
 			List<CrudEntityDescription> value) {
 		List<String> result = new ArrayList<String>();
 		for (CrudEntityDescription desc : value) {
 			Class<?> clazz;
 			try {
 				clazz = Class.forName(embeddedClassName);
 				Object entity = clazz.newInstance();
 				updateEntityFromDescription(entity, desc, clazz);
 				Gson gson = new Gson();
 				String json = gson.toJson(entity, clazz);
 				result.add(json);
 			} catch (Exception e) {
 				throw new RuntimeException("Failed to instantiate class: "
 						+ embeddedClassName, e);
 			}
 		}
 		return result;
 	}
 
 	public CrudEntityDescription describe(String entityName, Long id,
 			Long copyFromId) {
 		Class<? extends Model> clazz = extractClass(entityName);
 		Object entity = extractEntity(id, copyFromId, clazz);
 
 		return createEntityDescription(entityName, id, clazz, entity);
 	}
 
 	public CrudEntityDescription describeEmbeddedObject(String embeddedClassName) {
 		try {
 			Class<?> clazz = Class.forName(embeddedClassName);
 			Object o = clazz.newInstance();
 			return createEntityDescription(embeddedClassName, null, clazz, o);
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException("No such class: " + embeddedClassName, e);
 		} catch (InstantiationException e) {
 			throw new RuntimeException(
 					"Failed to create embedded object of type "
 							+ embeddedClassName, e);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(
 					"Failed to create embedded object of type "
 							+ embeddedClassName, e);
 		}
 
 	}
 
 	private CrudEntityDescription createEntityDescription(String entityName,
 			Long id, Class<?> clazz, Object entity) {
 		Field[] fields = clazz.getDeclaredFields();
 		CrudEntityDescription desc = new CrudEntityDescription();
 		desc.setCloneable(clazz.isAnnotationPresent((Class<? extends Annotation>) Cloneable.class));
 		desc.setEntityName(entityName);
 		desc.setId(id);
 		desc.setDisplayString(entity.toString());
 		for (Method method : clazz.getDeclaredMethods()) {
 			if (method.isAnnotationPresent(Displayable.class)) {
 				CrudField cf = new DisplayableMethodField();
 				cf.setName(method.getName());
 				try {
 					cf.setValue(method.invoke(entity));
 				} catch (Exception e) {
 					throw new RuntimeException("Failed to invoke method", e);
 				}
 				desc.add(cf);
 			}
 		}
 		for (Field field : fields) {
 			if (!okField(field)) {
 				// skip the id field
 				continue;
 			}
 			field.setAccessible(true);
 			CrudField crudField;
 			try {
 				crudField = createCrudField(field, entity);
 			} catch (Exception e) {
 				throw new RuntimeException("Failed to create CrudField", e);
 			}
 			desc.add(crudField);
 		}
 		// Find AdminLinks
 		for (Method method : clazz.getDeclaredMethods()) {
 			AdminLink linkAnn = method.getAnnotation(AdminLink.class);
 			if (linkAnn != null) {
 				AdminLinkMethodField cf = new AdminLinkMethodField();
 				cf.setName(method.getName());
 				cf.setText(linkAnn.text());
 				try {
 					cf.setValue(method.invoke(entity));
 				} catch (Exception e) {
 					throw new RuntimeException("Failed to invoke method", e);
 				}
 				desc.add(cf);
 			}
 		}
 		return desc;
 	}
 
 	private boolean okField(Field field) {
 		if ((field.getModifiers() & Modifier.TRANSIENT) == Modifier.TRANSIENT) {
 			// skip transient fields
 			return false;
 		}
 		if (field.isAnnotationPresent(Id.class)) {
 			// skip the id field
 			return false;
 		}
 		if (field.getType().equals(Class.class)) {
 			return false;
 		}
 		if (Modifier.isStatic(field.getModifiers())) {
 			return false;
 		}
 		if (field.isAnnotationPresent(Hidden.class)) {
 			return false;
 		}
 		if (field.isAnnotationPresent(RedundantForPerformance.class)) {
 			return false;
 		}
 		return true;
 	}
 
 	private CrudField createCrudField(Field field, Object entity)
 			throws Exception {
 		field.setAccessible(true);
 
 		CrudField crud = pluginManager.process(field, entity); 
 		if (crud == null) {
 			crud = processStandardCrud(field, entity);
 		}
 		crud.setName(field.getName());
 		crud.setRequired(field.isAnnotationPresent(Required.class));
 		crud.setSearchable(field.isAnnotationPresent(SearchableField.class));
 		if (field.isAnnotationPresent(DefaultSort.class)) {
 			crud.setDefaultSort(true);
 			crud.setSortAscending(field.getAnnotation(DefaultSort.class)
 					.ascending());
 		}
 		return crud;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public CrudField processStandardCrud(Field field, Object entity)
 			throws IllegalAccessException {
 		CrudField crud = null;
 		
 		Class clazz = field.getType();
 		
 		if (Enum.class.isAssignableFrom(clazz)) {
 			Enum value = (Enum) field.get(entity);
 			String clazzName = clazz.getName();
 			
 			crud = new ListedByEnumField((value == null) ? null : value.name(), clazzName);
 		
 		} else if (clazz == Date.class) {
 			crud = new DateField((Date) field.get(entity),
 					field.isAnnotationPresent(ReadOnly.class));
 			
 		} else if (clazz == Boolean.class) {
 			crud = new BooleanField((Boolean) field.get(entity));
 			
 		} else if (clazz == Long.class) {
 			if (field.isAnnotationPresent(Relation.class)) {
 				String relatedEntityClass = field.getAnnotation(Relation.class)
 						.value().getName();
 				crud = new ManyToOneRelationField((Long) field.get(entity),
 						relatedEntityClass);
 			} else {
 				crud = new LongField((Long) field.get(entity));
 			}
 			
 		} else if (clazz == Integer.class) {
 			crud = new IntegerField((Integer) field.get(entity));
 			
 		} else if (clazz == List.class) {
 			ParameterizedType pType = (ParameterizedType) field
 					.getGenericType();
 			Type type = pType.getActualTypeArguments()[0];
 			
 			if (field.isAnnotationPresent(EmbeddedBy.class)) {
 				EmbeddedBy embeddedBy = field.getAnnotation(EmbeddedBy.class);
 				crud = createEmbeddedListField(embeddedBy.value(),
 						(List<String>) field.get(entity));
 				
 			} else if (type.equals(Long.class) && field.isAnnotationPresent(Relation.class)) {
 				String relatedEntityClass = field.getAnnotation(Relation.class)
 						.value().getName();
 				crud = new ManyToManyRelationField(
 						(List<Long>) field.get(entity), relatedEntityClass);
 				
 			} else if (type.equals(String.class) && field.isAnnotationPresent(Link.class)) {
 				crud = new LinkListField((List<String>) field.get(entity));
 				
 			} else if (type.equals(String.class)) {
 				crud = new StringListField((List<String>) field.get(entity));
 				
 			} else if (type.equals(Integer.class)) {
 				crud = new IntegerListField((List<Integer>) field.get(entity));
 				
 			} else if (type.equals(Long.class)) {
 				crud = new LongListField((List<Long>) field.get(entity));
 				
 			} else {
 				throw new RuntimeException("Unknown list type: " + type);
 			}
 			
 		} else if (clazz == String.class
 				&& field.isAnnotationPresent(ReadOnly.class)) {
 			StringField stringCrud = new StringField((String) field.get(entity));
 			stringCrud.setReadOnly(true);
 			crud = stringCrud;
 		} else if (clazz == String.class
 				&& field.isAnnotationPresent(ListedByEnum.class)) {
 			ListedByEnum lenum = field.getAnnotation(ListedByEnum.class);
 			crud = new ListedByEnumField((String) field.get(entity), lenum.type()
 					.getName());
 		} else if (clazz == String.class
 				&& field.isAnnotationPresent(Image.class)) {
 			Image image = field.getAnnotation(Image.class);
 			crud = new ImageField((String) field.get(entity), image.width(),
 					image.height(), true);
 		} else if (clazz == String.class
 				&& field.isAnnotationPresent(ImageKey.class)) {
 			crud = new ImageField((String) field.get(entity), 0, 0, false);
 		} else if (clazz == String.class
 				&& field.isAnnotationPresent(RichText.class)) {
 			crud = new RichTextField((String) field.get(entity));
 		} else if (clazz == String.class
 				&& field.isAnnotationPresent(ListedBy.class)) {
 			ListedBy listedBy = field.getAnnotation(ListedBy.class);
 			String[] list = listedBy.value();
 			crud = new StringSelectionField((String) field.get(entity), list);
 		} else if (clazz == String.class
 				&& field.isAnnotationPresent(Link.class)) {
 			crud = new LinkedEntityField((String) field.get(entity));
 		} else if (clazz == String.class) {
 			StringField stringCrud = new StringField((String) field.get(entity));
 			if (field.isAnnotationPresent(RegexpValidation.class)) {
 				RegexpValidation regexp = field
 						.getAnnotation(RegexpValidation.class);
 				stringCrud.setRegexpPattern(regexp.pattern());
 				stringCrud.setRegexpDescription(regexp.description());
 			}
 			if (field.isAnnotationPresent(LongText.class)) {
 				stringCrud.setRenderAsTextArea(true);
 			}
 			crud = stringCrud;
 		} else {
 			throw new RuntimeException("No such field type: " + clazz.getName());
 		}
 		return crud;
 	}
 
 	private CrudField createEmbeddedListField(Class<?> type,
 			List<String> jsonList) {
 		List<CrudEntityDescription> descs = new ArrayList<CrudEntityDescription>();
 		if (jsonList != null) {
 			for (String embedded : jsonList) {
 				Gson gson = new Gson();
 				Object overlay = gson.fromJson(embedded, type);
 				CrudEntityDescription desc = createEntityDescription(
 						type.getName(), null, type, overlay);
 				descs.add(desc);
 			}
 		}
 		@SuppressWarnings("unchecked")
 		EmbeddedListField field = new EmbeddedListField(descs,
 				((Class<? extends Model>) type).getName());
 		return field;
 	}
 
 	private Model extractEntity(Long id, Long copyFromId, Class<? extends Model> clazz) {
 		// Based on the type and database id, fetches an entity from the
 		// database. Id -1 is treated specially and is used to signal the
 		// creation of a new object.
 		Model entity = null;
 		if (id == -1) {
 			// Id -1 means get a description for a new object of the type
 			if (copyFromId != null) {
 				// the field values are to be copied from another object:
 				entity = (Model) Model.all(clazz).filter("id", copyFromId)
 						.get();
 				resetId(entity);
 			} else {
 				try {
 					entity = (Model) clazz.newInstance();
 				} catch (Exception e) {
 					throw new RuntimeException(
 							"Failed to construct an object of type "
 									+ clazz.getName(), e);
 				}
 			}
 		} else {
 			entity = (Model) Model.all(clazz).filter("id", id).get();
 		}
 		return entity;
 	}
 
 	private void resetId(Model entity) {
 		// set the id field to null
 		try {
 			entity.getClass().getField("id").set(entity, null);
 		} catch (Exception e) {
 			throw new RuntimeException("Failed to set id to null", e);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private Class<? extends Model> extractClass(String entityName) {
 		// Attempts to get the class from the entity name.
 		Class<? extends Model> clazz;
 		try {
 			clazz = (Class<? extends Model>) Class.forName(entityName);
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException("Class not found: " + entityName, e);
 		}
 		if (!(clazz.getSuperclass().equals(Model.class))) {
 			throw new RuntimeException(
 					"Class must be subclass of siena.Model. The specified class is not: "
 							+ clazz.getName());
 		}
 		return clazz;
 	}
 
 }
