 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.persist;
 
 import static org.oobium.persist.ModelAdapter.getAdapter;
 import static org.oobium.utils.StringUtils.blank;
 import static org.oobium.utils.StringUtils.titleize;
 import static org.oobium.utils.coercion.TypeCoercer.coerce;
 import static org.oobium.utils.json.JsonUtils.toList;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.oobium.logging.Logger;
 import org.oobium.logging.LogProvider;
 import org.oobium.utils.StringUtils;
 import org.oobium.utils.coercion.TypeCoercer;
 import org.oobium.utils.json.JsonModel;
 import org.oobium.utils.json.JsonUtils;
 
 
 public abstract class Model implements JsonModel {
 
 	private static final ThreadLocal<Logger> logService = new ThreadLocal<Logger>();
 	private static final ThreadLocal<PersistServiceProvider> persistServiceProvider = new ThreadLocal<PersistServiceProvider>();
 	
 	
 	public static int count(Class<? extends Model> clazz) throws SQLException {
 		return getPersistService(clazz).count(clazz, null);
 	}
 	
 	public static int count(Class<? extends Model> clazz, String where, Object...values) throws SQLException {
 		return getPersistService(clazz).count(clazz, where, values);
 	}
 	
 	public static List<Map<String, Object>> executeQuery(Class<? extends Model> clazz, String sql, Object...values) throws SQLException {
 		return getPersistService(clazz).executeQuery(sql, values);
 	}
 
 	public static int executeUpdate(Class<? extends Model> clazz, String sql, Object...values) throws SQLException {
 		return getPersistService(clazz).executeUpdate(sql, values);
 	}
 	
 	public static <T extends Model> T find(Class<T> clazz, int id) throws SQLException {
 		return getPersistService(clazz).find(clazz, id);
 	}
 
 	public static <T extends Model> T find(Class<T> clazz, String sql, Object...values) throws SQLException {
 		return getPersistService(clazz).find(clazz, sql, values);
 	}
 	
 	public static <T extends Model> List<T> findAll(Class<T> clazz) throws SQLException {
 		return getPersistService(clazz).findAll(clazz);
 	}
 
 	public static <T extends Model> List<T> findAll(Class<T> clazz, String sql, Object...values) throws SQLException {
 		return getPersistService(clazz).findAll(clazz, sql, values);
 	}
 	
 	public static Logger getLogger() {
 		Logger service = logService.get();
 		if(service == null) {
 			service = LogProvider.getLogger(PersistService.class);
 			logService.set(service);
 		}
 		return service;
 	}
 	
 	public static PersistService getPersistService(Class<? extends Model> clazz) {
 		return getPersistServiceProvider().getFor(clazz);
 	}
 	
 	public static PersistServiceProvider getPersistServiceProvider() {
 		PersistServiceProvider provider = persistServiceProvider.get();
 		if(provider == null) {
 			provider = new SimplePersistServiceProvider();
 			persistServiceProvider.set(provider);
 		}
 		return persistServiceProvider.get();
 	}
 	
 	protected static boolean notEquals(Object o1, Object o2) {
 		return (o1 != null && !o1.equals(o2)) || o2 != null; 
 	}
 	
 	public static void setLogger(Logger service) {
 		logService.set(service);
 	}
 
 	public static PersistServiceProvider setPersistService(PersistService service) {
 		PersistServiceProvider services = new SimplePersistServiceProvider(service);
 		setPersistServiceProvider(services);
 		return services;
 	}
 
 	public static void setPersistServiceProvider(PersistServiceProvider services) {
 		persistServiceProvider.set(services);
 	}
 	
 	private PersistService persistor;
 	protected Logger logger;
 	
 	private int id;
 	private Map<String, Object> fields;
 	private Map<String, ArrayList<String>> errors;
 
 	public Model() {
 		logger = getLogger();
 		fields = new HashMap<String, Object>();
 	}
 
 	/**
 	 * Add a single error message to the base (null) subject.
 	 * Usefull for adding errors not related to a particular field.
 	 * @param message the error message to be added
 	 */
 	public final void addError(String message) {
 		addError(null, message);
 	}
 
 	/**
 	 * Add a single error message to the given subject.
 	 * Used by the internal validation mechanism and available for use
 	 * in custom code.
 	 * @param subject the subject that the error is related to (a field, for example)
 	 * @param message the error message
 	 */
 	public final void addError(String subject, String message) {
 		if(errors == null) {
 			errors = new LinkedHashMap<String, ArrayList<String>>();
 		}
 		ArrayList<String> list = errors.get(subject);
 		if(list == null) {
 			list = new ArrayList<String>();
 			errors.put(subject, list);
 		}
 		list.add(message);
 		if(logger.isLoggingDebug()) {
 			logger.debug("added error to " + asSimpleString() + ": " + subject + " -> "+ message);
 		}
 	}
 	
 	public String asSimpleString() {
 		return asSimpleString(false);
 	}
 	
 	public String asSimpleString(boolean includeErrors) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(getClass().getSimpleName());
 		sb.append(" {id:").append(id);
 		if(fields.isEmpty()) {
 			sb.append("-empty");
 		}
 		if(includeErrors && !errors.isEmpty()) {
 			if(fields.isEmpty()) {
 				sb.append(',').append(' ');
 			}
 			sb.append("errors: [");
 			List<String> errors = getErrorsList();
 			for(int i = 0; i < errors.size(); i++) {
 				if(i != 0) sb.append(',').append(' ');
 				sb.append(errors.get(i));
 			}
 			sb.append(']');
 		}
 		sb.append("}");
 		return sb.toString();
 	}
 	
 	public String asString() {
 		return asString(false);
 	}
 	
 	public String asString(boolean includeErrors) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(getClass().getSimpleName());
 		sb.append(" {id:").append(id);
 		if(fields.isEmpty()) {
 			sb.append("-empty");
 		} else {
 			for(Entry<String, Object> entry : fields.entrySet()) {
 				sb.append(",").append(entry.getKey()).append(":").append(String.valueOf(entry.getValue()));
 			}
 		}
 		if(includeErrors && !errors.isEmpty()) {
 			sb.append(',').append(' ');
 			sb.append("errors: [");
 			List<String> errors = getErrorsList();
 			for(int i = 0; i < errors.size(); i++) {
 				if(i != 0) sb.append(',').append(' ');
 				sb.append(errors.get(i));
 			}
 			sb.append(']');
 		}
 		sb.append("}");
 		return sb.toString();
 	}
 	
 	public final boolean canCreate() {
 		clearErrors();
 		Observer.runBeforeValidateCreate(this);
 		runValidations(Validate.CREATE);
 		validateCreate();
 		Observer.runAfterValidateCreate(this);
 		return !hasErrors();
 	}
 	
 	public final boolean canDestroy() {
 		clearErrors();
 		Observer.runBeforeValidateDestroy(this);
 		runValidations(Validate.DESTROY);
 		validateDestroy();
 		Observer.runAfterValidateDestroy(this);
 		return !hasErrors();
 	}
 
 	public final boolean canSave() {
 		clearErrors();
 		Observer.runBeforeValidateSave(this);
 		validateSave();
 		if(!hasErrors()) {
 			if(isNew()) {
 				Observer.runBeforeValidateCreate(this);
 				runValidations(Validate.CREATE);
 				validateCreate();
 				Observer.runAfterValidateCreate(this);
 			} else {
 				Observer.runBeforeValidateUpdate(this);
 				runValidations(Validate.UPDATE);
 				validateUpdate();
 				Observer.runAfterValidateUpdate(this);
 			}
 		}
 		Observer.runAfterValidateSave(this);
 		return !hasErrors();
 	}
 	
 	public final boolean canUpdate() {
 		clearErrors();
 		if(isEmpty()) {
 			addError("nothing to update");
 		} else {
 			Observer.runBeforeValidateUpdate(this);
 			runValidations(Validate.UPDATE);
 			validateUpdate();
 			Observer.runAfterValidateUpdate(this);
 		}
 		return !hasErrors();
 	}
 	
 	/**
 	 * Clear this model's internal data map.
 	 */
 	public void clear() {
 		fields.clear();
 	}
 	
 	protected final void clearErrors() {
 		if(hasErrors()) {
 			errors.clear();
 			errors = null;
 		}
 	}
 	
 	public boolean create() {
 		if(!isNew()) {
 			addError("model has already been created");
 			return false;
 		}
 		if(canCreate()) {
 			return doCreate();
 		}
 		return false;
 	}
 
 	public boolean destroy() {
 		if(id == 0) {
 			addError("cannot destroy a model that has not been saved");
 		} else {
 			if(getAdapter(getClass()).isDeletable()) {
 				if(canDestroy()) {
 					Observer.runBeforeDestroy(this);
 					if(!hasErrors()) {
 						try {
 							getPersistor().destroy(this);
 							id = 0;
 							destroyDependents();
 							fields.clear();
 							Observer.runAfterDestroy(this);
 						} catch(SQLException e) {
 							logger.warn("failed to destroy " + asSimpleString(), e);
 							addError(e.getLocalizedMessage());
 						}
 					}
 					return id == 0;
 				}
 			} else {
 				addError("Destroy is not permitted.");
 			}
 		}
 		return false;
 	}
 
 	private void destroyDependents() {
 		ModelAdapter adapter = ModelAdapter.getAdapter(this);
 		for(String field : adapter.getRelations()) {
 			Relation relation = adapter.getRelation(field);
 			if(relation.dependent() == Relation.DESTROY) {
 				if(adapter.hasOne(field)) {
 					Model related = (Model) get(field);
 					if(related != null) {
 						related.destroy();
 					}
 				} else {
 					Collection<?> related = (Collection<?>) get(field);
 					for(Object o : related) {
 						((Model) o).destroy();
 					}
 				}
 			}
 		}
 	}
 	
 	private boolean doCreate() {
 		boolean saved = false;
 		Observer.runBeforeCreate(this);
 	
 		if(!hasErrors()) {
 			try {
 				getPersistor().create(this);
 				saved = true;
 				Observer.runAfterCreate(this);
 			} catch(SQLException e) {
 				logger.warn("failed to save " + asSimpleString(), e);
 				addError(e.getLocalizedMessage());
 			}
 		}
 		return saved;
 	}
 
 	private boolean doUpdate() {
 		boolean saved = false;
 		if(getAdapter(getClass()).isUpdatable()) {
 			Observer.runBeforeUpdate(this);
 			
 			if(!hasErrors()) {
 				try {
 					getPersistor().update(this);
 					saved = true;
 					Observer.runAfterUpdate(this);
 				} catch(SQLException e) {
 					logger.warn("failed to save " + asSimpleString(), e);
 					addError(e.getLocalizedMessage());
 				}
 			}
 		} else {
 			addError("Updates are not permitted.");
 		}
 		return saved;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if(obj == this) {
 			return true;
 		}
 		if(obj != null && obj.getClass() == getClass() && ((Model) obj).getId() == id) {
 			return id != 0;
 		}
 		return false;
 	}
 	
 	/**
 	 * Get a data field from the model object.  If the model is not new then 
 	 * the field will be resolved if it is not already resolved.
 	 * @param field the name of the field to be returned
 	 * @return the value of the field
 	 * @see #get(String, boolean)
 	 */
 	public Object get(String field) {
 		return get(field, true);
 	}
 	
 	/**
 	 * Get a data field from the model object.  If load is true, and 
 	 * the model is not new, then the field will be resolved if it 
 	 * is not already resolved.
 	 * @param field the name of the field to be returned
 	 * @param load true to make sure the field is resolved, false otherwise
 	 * @return the value of the field
 	 */
 	public Object get(String field, boolean load) {
 		if("id".equals(field)) {
 			return getId();
 		} else {
 			if(fields.containsKey(field)) {
 				Class<?> type = getAdapter(getClass()).getClass(field);
 				if(type == null) {
 					return fields.get(field);
 				} else {
 					Object value = fields.get(field);
 					if(value == null || value.getClass() == type) { // TODO also check if correct type for primitive
 						return value;
 					}
 					return set(field, value, type);
 				}
 			} else {
 				if(hasMany(field)) {
 					Set<?> set;
 					if(isThrough(field)) {
 						set = new LinkedHashSet<Model>();
 					} else if(isManyToNone(field)) {
 						set = new LinkedHashSet<Model>();
 					} else if(isOppositeRequired(field)) {
 						set = new RequiredSet<Model>(this, field, getOpposite(field));
 					} else {
 						set = new ActiveSet<Model>(this, field, getOpposite(field), isManyToMany(field));
 					}
 					fields.put(field, set);
 				}
 				if(load && !isNew()) {
 					if(hasContained(field)) {
 						// TODO include the requested field if it is a hasOne...?
 						load();
 					} else if(hasMany(field)) {
 						try {
 							getPersistor().retrieve(this, field);
 //							Model model = getPersistor().find(getClass(), "where id=? include:?", id, field);
 //							if(model != null) {
 //								set(field, model.get(field));
 //							}
 						} catch(SQLException e) {
 							logger.warn("failed to load relation " + field + " in " + asSimpleString(), e);
 						}
 					}
 				}
 				return fields.get(field);
 			}
 		}
 	}
 
 	public <T> T get(String field, Class<T> type) {
 		return coerce(get(field), type);
 	}
 	
 	public <T> T get(String field, Class<T> type, boolean load) {
 		return coerce(get(field, load), type);
 	}
 
 	/**
 	 * Get a map of all of the internal data fields that
 	 * are currently set in this model.  Does not include the
 	 * model's ID, and does not resolve the model.
 	 * <p>The returned map is a copy - changes to it will not
 	 * affect the model.</p>
 	 * @return a map of internal data fields
 	 */
 	public Map<String, Object> getAll() {
 		return new HashMap<String, Object>(fields);
 	}
 
 	public String getError(String subject) {
 		return getError(subject, 0);
 	}
 	
 	public String getError(String subject, int index) {
 		if(errors != null && index >= 0) {
 			List<String> list = errors.get(subject);
 			if(list != null && index < list.size()) {
 				return list.get(index);
 			}
 		}
 		return null;
 	}
 	
 	public int getErrorCount() {
 		if(errors == null) {
 			return 0;
 		}
 		int count = 0;
 		for(ArrayList<String> list : errors.values()) {
 			count += list.size();
 		}
 		return count;
 	}
 	
 	/**
 	 * Get all of the errors contained within this model.
 	 * @return a LinkedHashMap of Lists of error messages key by their subject in the order that they were added; never null.
 	 */
 	public LinkedHashMap<String, List<String>> getErrors() {
 		if(errors != null) {
 			return new LinkedHashMap<String, List<String>>(errors);
 		}
 		return new LinkedHashMap<String, List<String>>(0);
 	}
 
 	/**
 	 * Get the list of errors corresponding to the given subject.
 	 * @param subject the subject to get the errors list for
 	 * @return the errors {@link List} for the subject, if any; otherwise an empty List; never null.
 	 */
 	public List<String> getErrors(String subject) {
 		if(errors != null) {
 			List<String> list = errors.get(subject);
 			if(list != null) {
 				return new ArrayList<String>(list);
 			}
 		}
 		return new ArrayList<String>(0);
 	}
 
 	/**
 	 * A complete and flattened list of all errors in this model. The subject of each
 	 * error has been prepended to the original message.
 	 * @return a List of all errors, or an empty list if there are none; never null.
 	 */
 	public List<String> getErrorsList() {
 		if(errors == null) {
 			return new ArrayList<String>(0);
 		}
 		List<String> list = new ArrayList<String>();
 		for(Entry<String, ArrayList<String>> entry : errors.entrySet()) {
 			String subject = entry.getKey();
 			for(String message : entry.getValue()) {
 				if(subject == null) {
 					list.add(message);
 				} else {
 					list.add(titleize(subject) + " " + message);
 				}
 			}
 		}
 		return list;
 	}
 	
 	public final int getId() {
 		return getId(false);
 	}
 	
 	public final int getId(boolean saveFirst) {
 		if(saveFirst && isNew()) {
 			save();
 		}
 		return id;
 	}
 
 	private int getLength(Object value, String tokenizer) {
 		if(value == null) {
 			return 0;
 		} else if(value instanceof Collection) {
 			return ((Collection<?>) value).size();
 		} else if(value instanceof Map) {
 			return ((Map<?,?>) value).size();
 		} else if(value.getClass().isArray()) {
 			return Array.getLength(value);
 		}
 		
 		String s = value.toString();
 		return blank(tokenizer) ? s.length() : s.split(tokenizer).length;
 	}
 
 	private String getOpposite(String field) {
 		return getAdapter(getClass()).getOpposite(field);
 	}
 	
 	public PersistService getPersistor() {
 		if(persistor == null) {
 			persistor = getPersistService(getClass());
 		}
 		return persistor;
 	}
 	
 	private Map<String, Object> getSerializationMap() {
 		Map<String, Object> map = new TreeMap<String, Object>();
 		map.put("id", getId());
 //		TODO toJson: include canonical class name?
 		ModelAdapter adapter = ModelAdapter.getAdapter(getClass());
 		for(String field : adapter.getFields()) {
 			if(isSet(field)) {
 				if(adapter.hasAttribute(field)) {
 					map.put(field, get(field));
 				} else if(adapter.hasOne(field)) {
 					Model model = (Model) get(field);
 					if(model != null) {
 						ModelAdapter fadapter = ModelAdapter.getAdapter(model.getClass());
 						Map<String, Object> fmap = new TreeMap<String, Object>();
 						fmap.put("id", model.getId());
 						for(String ffield : fadapter.getFields()) {
 							if(model.isSet(ffield)) {
 								if(fadapter.hasAttribute(ffield)) {
 									fmap.put(ffield, model.get(ffield));
 								} else if(fadapter.hasOne(ffield)) {
 									fmap.put(ffield, ((Model) model.get(ffield)).getId());
 								} else if(fadapter.hasMany(ffield)) {
 									Collection<?> collection = (Collection<?>) model.get(ffield);
 									List<Object> list = new ArrayList<Object>();
 									for(Object o : collection) {
 										list.add(Collections.singletonMap("id", ((Model) o).getId()));
 									}
 									fmap.put(ffield, list);
 								}
 							}
 						}
 						map.put(field, fmap);
 					}
 				} else if(adapter.hasMany(field)) {
 					Collection<?> collection = (Collection<?>) get(field);
 					List<Object> list = new ArrayList<Object>();
 					for(Object o : collection) {
 						Model model = (Model) o;
 						ModelAdapter fadapter = ModelAdapter.getAdapter(model.getClass());
 						Map<String, Object> fmap = new TreeMap<String, Object>();
 						fmap.put("id", model.getId());
 						for(String ffield : fadapter.getFields()) {
 							if(model.isSet(ffield)) {
 								if(fadapter.hasAttribute(ffield)) {
 									fmap.put(ffield, model.get(ffield));
 								} else if(fadapter.hasOne(ffield)) {
 									fmap.put(ffield, ((Model) model.get(ffield)).getId());
 								} else if(fadapter.hasMany(ffield)) {
 									Collection<?> fcollection = (Collection<?>) get(ffield);
 									List<Object> flist = new ArrayList<Object>();
 									for(Object fo : fcollection) {
 										flist.add(Collections.singletonMap("id", ((Model) fo).getId()));
 									}
 									fmap.put(ffield, list);
 								}
 							}
 						}
 						list.add(fmap);
 					}
 					map.put(field, list);
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Find out if this model's persisted object contains the field.
 	 * True if the field is an attribute or hasOne relationship.
 	 */
 	private boolean hasContained(String field) {
 		ModelAdapter adapter = getAdapter(getClass());
 		return adapter.hasAttribute(field) || adapter.hasOne(field);
 	}
 
 	/**
 	 * Find out if this model has any errors associated with it.
 	 * Only valid after validations have been run.
 	 * @return true if there are errors; false otherwise.
 	 */
 	public boolean hasErrors() {
 		return errors != null && !errors.isEmpty();
 	}
 
 	/**
 	 * Find out if this model has any errors associated with the given subject.
 	 * Only valid after validations have been run.
 	 * @param subject the subject to check for errors
 	 * @return true if there are errors; false otherwise.
 	 */
 	public boolean hasErrors(String subject) {
 		if(errors != null) {
 			List<String> list = errors.get(subject);
 			return list != null && !list.isEmpty();
 		}
 		return false;
 	}
 
 	/**
 	 * Find out if there are any errors associated with the nested field.
 	 * <p>For example - if a Post model has one Owner model and the Owner model has a name field, you
 	 * could use this method on a post to find out if there is an error associated with the
 	 * owner's name field: post.hasErrors("owner", "name");</p>
 	 * If the given fields array is of zero length, then this method returns false. Similarly,
 	 * if the nested field is not specified in the containing model's {@link ModelDescription}, then
 	 * this method return false.
 	 * @param fields an array of nested fields leading to the one to check for errors
 	 * @return true if there are errors; false otherwise.
 	 */
 	public final boolean hasErrors(String...fields) {
 		if(fields.length == 0) {
 			return false;
 		}
 		if(fields.length == 1) {
 			return hasErrors(fields[0]);
 		}
 		Model model = null;
 		for(int i = 0; i < fields.length - 1; i++) {
 			Object o = get(fields[i]);
 			if(o instanceof Model) {
 				model = (Model) o;
 			} else {
 				// only models have an errors list
 				return false;
 			}
 		}
 		return model.hasErrors(fields[fields.length-1]);
 	}
 	
 	@Override
 	public int hashCode() {
 		int hash = id + 2;
 		hash = hash * 31 + getClass().getCanonicalName().hashCode();
 		return hash;
 	}
 	
 	private boolean hasMany(String field) {
 		return getAdapter(getClass()).hasMany(field);
 	}
 	
 	@Override
 	public final boolean isBlank() {
 		return isNew() && blank(getAll());
 	}
 	
 	@Override
 	public final boolean isEmpty() {
 		return fields.isEmpty();
 	}
 	
 	private boolean isManyToMany(String field) {
 		return getAdapter(getClass()).isManyToMany(field);
 	}
 	
 	private boolean isManyToNone(String field) {
 		return getAdapter(getClass()).isManyToNone(field);
 	}
 
 	@Override
 	public final boolean isNew() {
 		return id <= 0;
 	}
 	
 	private boolean isOppositeRequired(String field) {
 		return getAdapter(getClass()).isOppositeRequired(field);
 	}
 
 	/**
 	 * Find out if given field is marked as required in this model's {@link ModelDescription}.
 	 * If the given field is not specified in the {@link ModelDescription} then it cannot be
 	 * marked as required and this method returns false.
 	 * @param field
 	 * @return true if the given field is required; false otherwise.
 	 * @see ModelAdapter#isRequired(String)
 	 */
 	public final boolean isRequired(String field) {
 		return getAdapter(getClass()).isRequired(field);
 	}
 
 	/**
 	 * Find out if the nested field is marked as required by its model's {@link ModelDescription}.
 	 * <p>For example - if a Post model has one Owner model and the Owner model has a name field, you
 	 * could use this method on a post to find out if the owner's name field is required: 
 	 * post.isRequired("owner", "name");</p>
 	 * If the given fields array is of zero length, then this method returns false. Similarly,
 	 * if the nested field is not specified in the containing model's {@link ModelDescription}, then
 	 * this method return false.
 	 * @param fields an array of fields leading from this model to the nested model
 	 * @return true if the given nested field is required; false otherwise.
 	 * @see ModelAdapter#isRequired(String)
 	 */
 	public final boolean isRequired(String...fields) {
 		if(fields.length == 0) {
 			return false;
 		}
 		ModelAdapter adapter = getAdapter(getClass());
 		if(fields.length == 1) {
 			return adapter.isRequired(fields[0]);
 		}
 		for(int i = 0; i < fields.length - 1; i++) {
 			Class<?> clazz = adapter.getClass(fields[i]);
 			if(Model.class.isAssignableFrom(clazz)) {
 				adapter = getAdapter(clazz.asSubclass(Model.class));
 			} else {
 				// field is not a model (may be a Map...); only models can mark fields as required
 				return false;
 			}
 		}
 		return adapter.isRequired(fields[fields.length-1]);
 	}
 	
 	@Override
 	public final boolean isSet(String field) {
 		return fields.containsKey(field);
 	}
 	
 	private boolean isThrough(String field) {
 		return getAdapter(getClass()).isManyToNone(field);
 	}
 
 	public final boolean load() {
 		try {
 			getPersistor().retrieve(this);
 			return true;
 		} catch(SQLException e) {
 			logger.warn("failed to load " + asSimpleString(), e);
 		}
 		return false;
 	}
 	
     private String msg(String message, Validate validation) {
 		if(blank(validation.message())) {
 			return message;
 		}
 		return validation.message();
 	}
 	
     /**
 	 * Put the given field, and its given value, directly into this model's underlying
 	 * data map.  The model will not be resolved if it isn't already.  If the field
 	 * already exists in the data map, it will be over-written.  Other fields in the
 	 * data map will remain unchanged.
 	 * @param field
 	 * @param value
 	 */
 	@Override
 	public Model put(String field, Object value) {
 		if("id".equals(field)) {
 			setId(coerce(id, int.class));
 		}
 		fields.put(field, value);
 		return this;
 	}
 	
 	/**
 	 * Puts the fields of the given model into this model.
 	 * @param model
 	 * @see #setAll(Map)
 	 * @return this, for method chaining
 	 */
 	@Override
 	public Model putAll(JsonModel model) {
 		if(model != null) {
 			putAll(model.getAll());
 		}
 		return this;
 	}
 	
 	/**
 	 * Put all of the given fields, and their given values, directly into this model's underlying
 	 * data map.  The model will not be resolved if it isn't already.  If any of the fields
 	 * already exist in the data map, they will be over-written.  Fields in the data map that
 	 * do not exist in the given fields will remain unchanged.  If one of the fields in the data
 	 * map is "id", then it will be used to set this model's id and <b>not</b> put into the data map.
 	 * @param fields
 	 */
 	@Override
 	public Model putAll(Map<String, Object> fields) {
 		if(fields.containsKey("id")) {
 			Object id = fields.remove("id");
 			setId(coerce(id, int.class));
 		}
 		this.fields.putAll(fields);
 		return this;
 	}
 	
 	/**
 	 * Put all of the given fields, and their given values, directly into this model's underlying
 	 * data map.  The model will not be resolved if it isn't already.  If any of the fields
 	 * already exist in the data map, they will be over-written.  Fields in the data map that
 	 * do not exist in the given fields will remain unchanged.  If one of the fields in the data
 	 * map is "id", then it will be used to set this model's id and <b>not</b> put into the data map.
 	 * @param json a Map of fields in JSON
 	 * @see #putAll(Map)
 	 */
 	@Override
 	public Model putAll(String json) {
 		return putAll(JsonUtils.toMap(json));
 	}
 	
 	/**
 	 * Remove the field from this model's internal data map.
 	 * @param field the field to be removed
 	 * @return the previous value of the field, or null if there was value for the field
 	 */
 	public Object remove(String field) {
 		return fields.remove(field);
 	}
 
 	/**
      * Removes the error at the specified position in list for the base (null) subject.
      * Shifts any subsequent elements to the left (subtracts one from their indices).
      * @param index the index of the error to be removed
      * @return the error that was removed from the list
      * @throws IndexOutOfBoundsException {@inheritDoc}
      */
 	public final String removeError(int index) {
 		return removeError(null, index);
 	}
 	
 	/**
 	 * Removes the first occurrence of the specified error from the list for the base (null) subject,
 	 * if it is present.  If the list does not contain the error, it is
 	 * unchanged.  More formally, removes the error with the lowest index
 	 * <tt>i</tt> such that
 	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
 	 * (if such an error exists).  Returns <tt>true</tt> if this list
 	 * contained the specified error (or equivalently, if this list
 	 * changed as a result of the call).
 	 *
 	 * @param error the error to be removed from this list, if present
 	 * @return <tt>true</tt> if this list contained the specified error; false otherwise
 	 */
 	public final boolean removeError(String error) {
 		return removeError(null, error);
 	}
 	
 	/**
      * Removes the error at the specified position in the list for the given subject.
      * Shifts any subsequent elements to the left (subtracts one from their indices).
      * @param index the index of the error to be removed
      * @return the error that was removed from the list
      * @throws IndexOutOfBoundsException {@inheritDoc}
      */
 	public final String removeError(String subject, int index) {
 		if(errors != null) {
 			List<String> list = errors.get(subject);
 			if(list != null) {
 				String s = list.remove(index);
 				if(list.isEmpty()) {
 					errors.remove(subject);
 					if(errors.isEmpty()) {
 						errors = null;
 					}
 				}
 				return s;
 			}
 		}
 	    throw new IndexOutOfBoundsException("Index: " + index + ", Size: 0");
 	}
 
 	/**
 	 * Removes the first occurrence of the specified error from the list for the given subject,
 	 * if it is present.  If the list does not contain the error, it is
 	 * unchanged.  More formally, removes the error with the lowest index
 	 * <tt>i</tt> such that
 	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
 	 * (if such an error exists).  Returns <tt>true</tt> if this list
 	 * contained the specified error (or equivalently, if this list
 	 * changed as a result of the call).
 	 *
 	 * @param error the error to be removed from this list, if present
 	 * @return <tt>true</tt> if this list contained the specified error; false otherwise
 	 */
 	public final boolean removeError(String subject, String message) {
 		if(errors != null) {
 			List<String> list = errors.get(subject);
 			if(list != null) {
 				list.remove(message);
 				if(list.isEmpty()) {
 					errors.remove(subject);
 					if(errors.isEmpty()) {
 						errors = null;
 					}
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Remove all errors in the list for the given subject.
 	 * To remove errors from the base subject, simply pass a null in for the subject.
 	 * @param subject the subject whose errors are to be removed
 	 * @return the List of errors that was removed, or an empty list if
 	 * there weren't any; never null;
 	 */
 	public final List<String> removeErrors(String subject) {
 		if(errors != null) {
 			List<String> list = errors.remove(subject);
 			if(errors.isEmpty()) {
 				errors = null;
 			}
 			return list;
 		}
 		return new ArrayList<String>(0);
 	}
 	
 	private boolean run(String methodName, String field) {
 		boolean pass = true;
 		try {
 			if(blank(field)) {
 				Method method = getClass().getDeclaredMethod(methodName);
 				pass = (Boolean) method.invoke(this);
 			} else {
 				ModelAdapter adapter = ModelAdapter.getAdapter(this);
 				String[] fields = field.split("\\s*,\\s*");
 				for(int i = 0; pass && i < fields.length; i++) {
 					try {
 						Method method = getClass().getDeclaredMethod(methodName, adapter.getClass(fields[i]));
 						method.setAccessible(true);
 						pass = (Boolean) method.invoke(this, get(fields[i]));
 					} catch(NoSuchMethodException e) {
 						Method method = getClass().getDeclaredMethod(methodName);
 						method.setAccessible(true);
 						pass = (Boolean) method.invoke(this);
 					}
 				}
 			}
 		} catch(ClassCastException e) {
 			logger.warn("validation method does not return a boolean: " + methodName);
 		} catch(NoSuchMethodException e) {
 			logger.warn("validation method does not exist: " + methodName);
 		} catch(Exception e) {
 			// discard
 		}
 		return pass;
 	}
 	
 	private void runValidation(Validate validate, boolean onUpdate) {
 		if(!blank(validate.when()) && !run(validate.when(), validate.field())) {
 			return;
 		}
 		if(!blank(validate.unless()) && run(validate.unless(), validate.field())) {
 			return;
 		}
 		if(validate.unlessNull() && get(validate.field()) == null) {
 			return;
 		}
 		if(validate.unlessBlank() && blank(get(validate.field()))) {
 			return;
 		}
 		
 		if(validate.with() != Object.class) {
 			runValidator(validate.with());
 			return;
 		}
 		
 		if(!blank(validate.withMethod())) {
 			String[] fields = validate.field().split("\\s*,\\s*");
 			for(String field : fields) {
 				if(!onUpdate || isSet(field)) {
 					if(!run(validate.withMethod(), field)) {
 						addError(validate.field(), validate.message());
 					}
 				}
 			}
 			return;
 		}
 		
 		String[] fields = validate.field().split("\\s*,\\s*");
 		for(String field : fields) {
 			if(onUpdate && !isSet(field)) {
 				continue;
 			}
 			
 			Object value = get(field);
 			if(validate.isBlank() && !blank(value)) {
 				addError(field, msg("must be blank", validate));
 				continue;
 			}
 			if(validate.isNotBlank() && blank(value)) {
 				addError(field, msg("cannot be blank", validate));
 				continue;
 			}
 			if(validate.isNotNull() && value == null) {
 				addError(field, msg("cannot be null", validate));
 				continue;
 			}
 			if(validate.isNull() && value != null) {
 				addError(field, msg("must be null", validate));
 				continue;
 			}
 			if(!blank(validate.isNotIn()) && toList(validate.isNotIn()).contains(value)) {
 				addError(field, msg("cannot be one of \"" + validate.isNotIn() + "\"", validate));
 				continue;
 			}
 			if(!blank(validate.isIn()) && !toList(validate.isIn()).contains(value)) {
 				addError(field, msg("can only be one of \"" + validate.isIn() + "\"", validate));
 				continue;
 			}
 			if(!blank(validate.matches()) && !((value == null) ? "" : value.toString()).matches(validate.matches())) {
 				addError(field, msg("must be of format \"" + validate.matches() + "\"", validate));
 				continue;
 			}
 			if(validate.maxLength() >= 0 && getLength(value, validate.tokenizer()) > validate.maxLength()) {
 				addError(field, msg("length cannot be more than " + validate.maxLength(), validate));
 				continue;
 			}
 			if(validate.minLength() >= 0 && getLength(value, validate.tokenizer()) < validate.minLength()) {
 				addError(field, msg("length cannot be less than " + validate.minLength(), validate));
 				continue;
 			}
 			if(validate.lengthIs() >= 0 && getLength(value, validate.tokenizer()) != validate.lengthIs()) {
 				addError(field, msg("length must be " + validate.lengthIs(), validate));
 				continue;
 			}
 		}
 	}
 	
 	private void runValidations(int on) {
 		ModelDescription description = getClass().getAnnotation(ModelDescription.class);
 		if(description != null) {
 			boolean onUpdate = (on == Validate.UPDATE);
 			if((on & (Validate.CREATE | Validate.UPDATE)) != 0) {
 				for(Relation relation : description.hasOne()) {
 					String field = relation.name();
 					if(!onUpdate || isSet(field)) {
 						if(relation.required() && get(relation.name()) == null) {
 							addError(field, "cannot be null");
 						}
 					}
 				}
 			}
 			for(Validate validate : description.validations()) {
 				if((validate.on() & on) != 0) {
 					runValidation(validate, onUpdate);
 				}
 			}
 			if(on == Validate.DESTROY) {
 				validateDependents();
 			}
 		}
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private void runValidator(Class<?> validatorClass) {
 		try {
 			Validator validator = (Validator) validatorClass.newInstance();
 			validator.validate(this);
 		} catch(InstantiationException e) {
 			logger.warn("could not run validator: " + validatorClass);
 		} catch(IllegalAccessException e) {
 			logger.warn("could not run validator: " + validatorClass);
 		} catch(Exception e) {
 			logger.warn(e);
 		}
 	}
 	
 	public boolean save() {
 		boolean result = false;
 		if(canSave()) {
 			Observer.runBeforeSave(this);
 			if(isNew()) {
 				result = doCreate();
 			} else {
 				result = doUpdate();
 			}
 			Observer.runAfterSave(this);
 		}
 		return result;
 	}
 	
 	/**
 	 * Set the given field to the coerced type of the given value.
 	 * <p>The model will be resolved if:
 	 * <ul>
 	 *   <li>it isn't already resolved</li>
 	 *   <li>the given value is an instance of {@link Model}</li>
 	 *   <li>the opposite of the given field is either an {@link ActiveSet} or {@link RequiredSet}
 	 * </ul>
 	 * </p>
 	 * If the field already exists in the data map, it will be over-written.
 	 * @param field
 	 * @param value
 	 * @return this, for method chaining
 	 * @see TypeCoercer#coerce(Object, Class)
 	 */
 	@Override
 	public Model set(String field, Object value) {
 		if("id".equals(field)) {
 			setId(coerce(value, int.class));
 		} else {
 			Class<?> type = getAdapter(getClass()).getClass(field);
 			set(field, value, type);
 		}
 		return this;
 	}
 	
 	private Object set(String field, Object value, Class<?> type) {
 		if(type == null) {
 			// not a persisted field that we know about
 			fields.put(field, value);
 			return value;
 		} else if(Collection.class.isAssignableFrom(type)) {
 			Class<? extends Model> mtype = getAdapter(this).getHasManyMemberClass(field);
 			Model[] models = (Model[]) coerce(value, Array.newInstance(mtype, 0).getClass());
 			if(type == ActiveSet.class || type == RequiredSet.class) {
 				Set<Model> set;
 				if(type == ActiveSet.class) {
 					set = new ActiveSet<Model>(this, field, getOpposite(field), isManyToMany(field), models);
 				} else {
 					set = new RequiredSet<Model>(this, field, getOpposite(field), models);
 				}
 				fields.put(field, set);
 				return set;
 			} else {
 				return coerce(models, type);
 			}
 		} else {
 			Object coercedValue = coerce(value, type);
 			if(Model.class.isAssignableFrom(type)) {
 				String opposite = getOpposite(field);
 				if(getAdapter(type.asSubclass(Model.class)).hasMany(opposite)) {
					Model oldModel = (Model) (fields.containsKey(field) ? fields.get(field) : null);
 					if(oldModel != null) {
 						Object o = oldModel.get(opposite);
 						if(o instanceof ActiveSet<?>) {
 							((ActiveSet<?>) oldModel.get(opposite)).doRemove(this);
 						} else if(o instanceof RequiredSet<?>) {
 							((RequiredSet<?>) oldModel.get(opposite)).doRemove(this);
 						}
 					}
 					Model newModel = (Model) coercedValue;
 					if(newModel != null) {
 						Object o = newModel.get(opposite);
 						if(o instanceof ActiveSet<?>) {
 							((ActiveSet<?>) o).doAdd(this);
 						} else if(o instanceof RequiredSet<?>) {
 							((RequiredSet<?>) o).doAdd(this);
 						}
 					}
 				}
 			}
 			fields.put(field, coercedValue);
 			return coercedValue;
 		}
 	}
 
 	/**
 	 * Sets this model's fields to those of the given map.
 	 * The existing fields will first be cleared and then
 	 * each entry in the Map will be used to set a corresponding
 	 * field in this object through one of two methods:
 	 * 1. the setter method will be called, if it exists; otherwise
 	 * 2. it will be passed to the {@link #set(String, Object)} method.
 	 * @param fields
 	 * @see #set(String, Object)
 	 * @return this, for method chaining
 	 */
 	@Override
 	public Model setAll(Map<String, Object> fields) {
 		this.fields.clear();
 		if(fields != null) {
 			for(Entry<String, Object> entry : fields.entrySet()) {
 				String key = entry.getKey();
 				Object val = entry.getValue();
 				setField(key, val);
 			}
 		}
 		return this;
 	}
 
 	/**
 	 * Sets this model's fields to those of the given map.
 	 * The existing fields will first be cleared and then the
 	 * given map will be passed into the {@link #setAll(Map)}
 	 * method of this model.
 	 * @param json a String that contains a map object in JSON format
 	 * @see #setAll(Map)
 	 * @return this, for method chaining
 	 */
 	@Override
 	public Model setAll(String json) {
 		fields.clear();
 		if(json != null) {
 			setAll(JsonUtils.toMap(json));
 		}
 		return this;
 	}
 	
 	private void setField(String field, Object value) {
 		if("id".equals(field)) {
 			setId(coerce(value, int.class));
 		} else {
 			Class<?> type = getAdapter(getClass()).getClass(field);
 			if(type == null || Collection.class.isAssignableFrom(type)) {
 				set(field, value);
 			} else {
 				try {
 					Method setter = getClass().getMethod(StringUtils.setterName(field), type);
 					setter.invoke(this, coerce(value, type));
 				} catch(NoSuchMethodException e) {
 					set(field, value);
 				} catch(SecurityException e) {
 					logger.warn(e);
 				} catch(IllegalArgumentException e) {
 					logger.warn(e);
 				} catch(IllegalAccessException e) {
 					logger.warn(e);
 				} catch(InvocationTargetException e) {
 					logger.warn(e);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public Model setId(int id) {
 		this.id = id;
 		return this;
 	}
 
 	public void setPersistor(PersistService service) {
 		persistor = service;
 	}
 	
 	@Override
 	public String toJson() {
 		Map<String, Object> map = getSerializationMap();
 		String json = JsonUtils.toJson(map);
 		if(logger.isLoggingTrace()) {
 			logger.trace(this + ".toJson() -> \n  " + json);
 		}
 		return json;
 	}
 	
 	@Override
 	public String toString() {
 		return asSimpleString();
 	}
 
 	public boolean update() {
 		if(isNew()) {
 			addError("cannot update a model that has not been created");
 			return false;
 		}
 		if(canUpdate()) {
 			return doUpdate();
 		}
 		return false;
 	}
 	
 	protected void validateCreate() {
 		// subclasses to override
 	}
 	
 	private void validateDependents() {
 		ModelAdapter adapter = ModelAdapter.getAdapter(this);
 		for(String field : adapter.getRelations()) {
 			Relation relation = adapter.getRelation(field);
 			if(relation.dependent() == Relation.DESTROY) {
 				if(adapter.hasOne(field)) {
 					Model related = (Model) get(field);
 					if(related != null) {
 						if(!related.canDestroy()) {
 							addError(field, "is preventing this model from being destroyed");
 						}
 					}
 				} else {
 					Collection<?> related = (Collection<?>) get(field);
 					for(Object o : related) {
 						if(!((Model) o).canDestroy()) {
 							addError(field, "are preventing this model from being destroyed");
 							break;
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	protected void validateDestroy() {
 		// subclasses to override
 	}
 
 	protected void validateSave() {
 		// subclasses to override
 	}
 	
 	protected void validateUpdate() {
 		// subclasses to override
 	}
 	
 }
