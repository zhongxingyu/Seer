 /**
  * Copyright (C) 2008 Ivan S. Dubrov
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *         http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.google.code.nanorm.internal.config;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.code.nanorm.DataSink;
 import com.google.code.nanorm.SQLSource;
 import com.google.code.nanorm.TypeHandlerFactory;
 import com.google.code.nanorm.annotations.Insert;
 import com.google.code.nanorm.annotations.Mapping;
 import com.google.code.nanorm.annotations.ResultMap;
 import com.google.code.nanorm.annotations.ResultMapList;
 import com.google.code.nanorm.annotations.ResultMapRef;
 import com.google.code.nanorm.annotations.Select;
 import com.google.code.nanorm.annotations.SelectKey;
 import com.google.code.nanorm.annotations.SelectKeyType;
 import com.google.code.nanorm.annotations.Source;
 import com.google.code.nanorm.annotations.Update;
 import com.google.code.nanorm.exceptions.ConfigurationException;
 import com.google.code.nanorm.internal.DynamicFragment;
 import com.google.code.nanorm.internal.Fragment;
 import com.google.code.nanorm.internal.TextFragment;
 import com.google.code.nanorm.internal.introspect.IntrospectionFactory;
 import com.google.code.nanorm.internal.introspect.Setter;
 import com.google.code.nanorm.internal.mapping.result.DefaultRowMapper;
 import com.google.code.nanorm.internal.mapping.result.RowMapper;
 import com.google.code.nanorm.internal.mapping.result.ScalarRowMapper;
 import com.google.code.nanorm.internal.util.Messages;
 import com.google.code.nanorm.internal.util.ToStringBuilder;
 
 /**
  * TODO: Merge processing and searching. Maybe, lazy loading (when referenced).
  * 
  * TODO: Thread safety?
  * 
  * TODO: Validate: one map on method, no map + ref at the same time, no maps
  * with same id
  * 
  * TODO: Validate: one data sink and query method is void
  * 
  * @author Ivan Dubrov
  * @version 1.0 29.05.2008
  */
 public class InternalConfiguration {
 
 	private final static Logger LOGGER = LoggerFactory.getLogger(InternalConfiguration.class);
 
 	private final Set<Class<?>> mapped;
 
 	/**
 	 * Result map id is [package].[class]#id
 	 */
 	private final Map<String, ResultMapConfig> resultMapsConfig;
 
 	/**
 	 * Statement id is [package].[class]#method
 	 */
 	private final Map<StatementKey, StatementConfig> statementsConfig;
 
 	// private final List<SubselectConfig> postProcessSubselects;
 
 	private final List<Runnable> postConfigureList;
 
 	private final TypeHandlerFactory typeHandlerFactory;
 
 	private final IntrospectionFactory introspectionFactory;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param typeHandlerFactory type handler factory
 	 * @param introspectionFactory introspection factory
 	 */
 	public InternalConfiguration(TypeHandlerFactory typeHandlerFactory,
 			IntrospectionFactory introspectionFactory) {
 		resultMapsConfig = new HashMap<String, ResultMapConfig>();
 		statementsConfig = new HashMap<StatementKey, StatementConfig>();
 		mapped = new HashSet<Class<?>>();
 		// postProcessSubselects = new ArrayList<SubselectConfig>();
 		postConfigureList = new ArrayList<Runnable>();
 
 		// TODO: Should be configurable
 		this.typeHandlerFactory = typeHandlerFactory;
 		this.introspectionFactory = introspectionFactory;
 	}
 
 	/**
 	 * Get statement configuration for method.
 	 * 
 	 * @param method method
 	 * @return statement configuration for method
 	 */
 	private StatementConfig getStatementConfig(StatementKey key) {
 		StatementConfig statementConfig = statementsConfig.get(key);
 		if (statementConfig == null) {
 			throw new IllegalArgumentException("Missing configuration for method '" + key + "'");
 		}
 		return statementConfig;
 	}
 
 	/**
 	 * Get statement configuration for method.
 	 * 
 	 * @param mapper mapper interface
 	 * @param name statement name (method name)
 	 * @param parameters statement parameter types
 	 * @return statement configuration for method
 	 */
 	public StatementConfig getStatementConfig(Class<?> mapper, String name, Type[] parameters) {
 		StatementKey key = new StatementKey(mapper, name, parameters);
 		return getStatementConfig(key);
 	}
 
 	/**
 	 * Configure mapper.
 	 * 
 	 * TODO: Elaborate Javadoc!
 	 * 
 	 * @param mapper mapper interface
 	 * @throws ConfigurationException configuration is invalid
 	 */
 	public void configure(Class<?> mapper) throws ConfigurationException {
 		synchronized (this) {
 			if (mapped.add(mapper)) {
 				// Configure super mappers first
 				for (Class<?> superMapper : mapper.getInterfaces()) {
 					configure(superMapper);
 				}
 
 				LOGGER.info("Configuring mapper interface " + mapper.getName());
 
 				// Configure mapper itself
 				processResultMaps(mapper);
 				for (Method method : mapper.getMethods()) {
 					processMethod(mapper, method);
 				}
 
 				postProcess();
 			}
 		}
 	}
 
 	/**
 	 * Post process the configuration, resolving the references of subselects.
 	 */
 	private void postProcess() throws ConfigurationException {
 		for (Runnable runnable : postConfigureList) {
 			runnable.run();
 		}
 		postConfigureList.clear();
 	}
 
 	/**
 	 * Process given method and generate statement configuration from it.
 	 * 
 	 * TODO: Validate we don't have more than one from insert, select, update
 	 * and delete.
 	 * 
 	 * @param method method to gather configuration from
 	 */
 	private void processMethod(Class<?> mapper, Method method) throws ConfigurationException {
 		StatementKey key = new StatementKey(mapper, method.getName(), method
 				.getGenericParameterTypes());
 
 		if (statementsConfig.containsKey(key)) {
 			// TODO: Log debug message "Query method '" + key + "' is already
 			// configured!";
 			return;
 		}
 
 		// TODO: Invoke method.getGeneryParameters once!
 		final StatementConfig stConfig = new StatementConfig(key);
 
 		final ResultMapConfig config = createResultMapConfig(method);
 
 		// TODO: Check we have only one of those!
 		Select select = method.getAnnotation(Select.class);
 		Update update = method.getAnnotation(Update.class);
 		Insert insert = method.getAnnotation(Insert.class);
 		Source source = method.getAnnotation(Source.class);
 		String sql = null;
 		boolean isUpdate = (update != null);
 		boolean isInsert = (insert != null);
 		if (select != null) {
 			sql = select.value();
 		} else if (insert != null) {
 			sql = insert.value();
 		} else if (update != null) {
 			sql = update.value();
 		} else if (source != null) {
 			Class<? extends SQLSource> sqlSource = source.value();
 			Fragment builder = new DynamicFragment(sqlSource, introspectionFactory);
 			stConfig.setStatementBuilder(builder);
 		} else {
 			// Skip method
 			// TODO: Logging!
 			return;
 		}
 		if (sql != null) {
 			// TODO: Batch case!
 			Fragment builder = new TextFragment(sql, method.getGenericParameterTypes(),
 					introspectionFactory);
 			stConfig.setStatementBuilder(builder);
 			stConfig.setUpdate(isUpdate);
 			stConfig.setInsert(isInsert);
 		}
 		// TODO: Check sql is not empty!
 
 		// Configure select key statement
 		SelectKey selectKey = method.getAnnotation(SelectKey.class);
 		if (selectKey != null) {
 			validateSelectKey(selectKey, mapper, method);
 
 			StatementKey selectKeyKey = new StatementKey(mapper, method.getName() + ":key", method
 					.getGenericParameterTypes());
 
 			StatementConfig selectKeySt = new StatementConfig(selectKeyKey);
 			if (selectKey.value().length() > 0) {
 				selectKeySt.setStatementBuilder(new TextFragment(selectKey.value(), method
 						.getGenericParameterTypes(), introspectionFactory));
 			}
 			selectKeySt.setParameterTypes(method.getGenericParameterTypes());
 			selectKeySt.setResultType(method.getGenericReturnType());
 			selectKeySt.setRowMapper(new ScalarRowMapper(method.getGenericReturnType(),
 					typeHandlerFactory));
 
 			if (selectKey.property().length() > 0) {
 				Setter keySetter = introspectionFactory.buildParameterSetter(method
 						.getGenericParameterTypes(), selectKey.property());
 				stConfig.setKeySetter(keySetter);
 			}
 
 			stConfig.setSelectKeyType(selectKey.type());
 			stConfig.setSelectKey(selectKeySt);
 		}
 
 		// For update we always use method return value, but for select we try
 		// to find DataSink if return type is void
 		// TODO: Should this work for generated keys as well?
 		Type returnType = null;
 		if (select != null && method.getReturnType() == void.class) {
 			int pos = searchResultCallback(method);
 			if (pos == -1) {
 				throw new ConfigurationException(Messages.invalidReturnType(method));
 			}
 
 			stConfig.setCallbackIndex(pos);
 
 			ParameterizedType pt = (ParameterizedType) method.getGenericParameterTypes()[pos];
 			returnType = pt.getActualTypeArguments()[0];
 
 		} else {
 			returnType = method.getGenericReturnType();
 		}
 		stConfig.setResultType(returnType);
 		if (returnType != void.class) {
 			// Create row mapper during post-configuration step, after the subselects
 			// are configured
 			postConfigureList.add(new Runnable() {
 				public void run() {
 					// At this time, all subselect properties should be post-configured already
 					stConfig.setRowMapper(createRowMapper(stConfig.getResultType(), config));
 				}
 			});
 		}
 
 		stConfig.setParameterTypes(method.getGenericParameterTypes());
 
 		// Put it two times: one time with parameter types, for regular
 		// processing
 		statementsConfig.put(key, stConfig);
 
 		// Second time without parameter types (for subselect references,
 		// where parameter types are not known and therefore the matching
 		// query is searched by mapper class and query name only)
 		StatementKey key2 = new StatementKey(key.getMapper(), key.getName(), null);
 		statementsConfig.put(key2, stConfig);
 	}
 
 	// TODO: Move to separate helper class
 	private int searchResultCallback(Method method) {
 		// Try to find DataSink
 		Type[] types = method.getGenericParameterTypes();
 		for (int i = 0; i < types.length; ++i) {
 			if (types[i] instanceof ParameterizedType) {
 				ParameterizedType pt = (ParameterizedType) types[i];
 				if (pt.getRawType() == DataSink.class) {
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * 
 	 * @param type result type to map result set row to
 	 * @param config result map configuration
 	 * @return
 	 */
 	private RowMapper createRowMapper(Type type, ResultMapConfig config) {
 		// For primitive types we simply return the mapped value of first column
 		if (type instanceof Class<?> && ((Class<?>) type).isPrimitive()) {
 			return new ScalarRowMapper(type, typeHandlerFactory);
 		}
 		// TODO: If config is null, automap it?
 		return new DefaultRowMapper(type, config, introspectionFactory, typeHandlerFactory);
 	}
 
 	/**
 	 * Process result maps defined for given class.
 	 * 
 	 * @param clazz class to gather result maps from
 	 */
 	private void processResultMaps(Class<?> clazz) throws ConfigurationException {
 		ResultMap classResultMap = clazz.getAnnotation(ResultMap.class);
 
 		if (classResultMap != null) {
 			processResultMap(clazz, classResultMap);
 		}
 
 		ResultMapList classResultMapList = clazz.getAnnotation(ResultMapList.class);
 		if (classResultMapList != null) {
 			for (ResultMap classResultMap2 : classResultMapList.value()) {
 				processResultMap(clazz, classResultMap2);
 			}
 		}
 	}
 
 	/**
 	 * Process the result map annotation.
 	 * 
 	 * @param clazz declaring class
 	 * @param resultMap result map annotation
 	 */
 	private void processResultMap(Class<?> clazz, ResultMap resultMap)
 			throws ConfigurationException {
 		ResultMapConfig cfg = createResultMapConfig(clazz, null, resultMap);
 		resultMapsConfig.put(cfg.getId(), cfg);
 	}
 
 	/**
 	 * Get result map config for given method.
 	 * 
 	 * @param method method
 	 * @return
 	 */
 	private ResultMapConfig createResultMapConfig(Method method) throws ConfigurationException {
 		ResultMap resultMap = method.getAnnotation(ResultMap.class);
 		ResultMapRef ref = method.getAnnotation(ResultMapRef.class);
 		Class<?> mapper = method.getDeclaringClass();
 		if (resultMap != null) {
 			return createResultMapConfig(method.getDeclaringClass(), method, resultMap);
 		}
 
 		// No explicit map on method is defined -- try to find reference or
 		// default map
 		ResultMapConfig resultMapConfig;
 		if (ref != null) {
 			// Resolve reference
 			Class<?> refMapper = ref.declaringClass();
 			if (ref.declaringClass() == Object.class) {
 				refMapper = mapper;
 			}
 			resultMapConfig = findResultMap(refMapper, ref.value());
 			if (resultMapConfig == null) {
 				throw new ConfigurationException(Messages.resultMapNotFound(mapper, method, ref));
 			}
 		} else {
 			// Search for default map for the mapper (with empty id)
 			// FIXME: Remove this feature?
 			resultMapConfig = findResultMap(method.getDeclaringClass(), "");
 			if (resultMapConfig == null) {
 				// Return automap if no map is defined
 				return createResultMapConfig(method.getDeclaringClass(), method, null);
 			}
 		}
 		return resultMapConfig;
 	}
 
 	/**
 	 * Create {@link ResultMapConfig} instance from {@link RowMapper}
 	 * annotation.
 	 * 
 	 * TODO: Validate we don't have nested map with "select" property at the
 	 * same time
 	 * 
 	 * @param clazz
 	 * @param resultMap
 	 * @return
 	 */
 	private ResultMapConfig createResultMapConfig(Class<?> mapper, Method method,
 			ResultMap resultMap) throws ConfigurationException {
 		List<PropertyMappingConfig> mappings = new ArrayList<PropertyMappingConfig>();
 		boolean auto = true;
 		if (resultMap != null) {
 
 			// Set of all properties, for groupBy list validation
 			Set<String> propnames = null;
 			if (resultMap.groupBy().length > 0) {
 				propnames = new HashSet<String>();
 			}
 			for (Mapping mapping : resultMap.mappings()) {
 				PropertyMappingConfig propMapping = createPropertyMappingConfig(mapper, method,
 						resultMap, mapping);
 				mappings.add(propMapping);
 
 				if (propnames != null) {
 					propnames.add(propMapping.getProperty());
 				}
 			}
 
 			validateGroupBy(mapper, method, resultMap, propnames);
 			auto = resultMap.auto();
 		}
 
 		String id;
 		if (resultMap != null) {
 			id = mapper.getName() + "#" + resultMap.id();
 		} else {
 			// TODO: Generated!
 			id = mapper.getName() + "#(auto)";
 		}
 
 		ResultMapConfig config = new ResultMapConfig(id);
 		config.setMappings(mappings.toArray(new PropertyMappingConfig[mappings.size()]));
 		config.setAuto(auto);
 		if (resultMap != null) {
 			config.setGroupBy(resultMap.groupBy());
 		}
 		return config;
 	}
 
 	private PropertyMappingConfig createPropertyMappingConfig(Class<?> mapper, Method method,
 			ResultMap resultMap, Mapping mapping) throws ConfigurationException {
 		PropertyMappingConfig propMapping = new PropertyMappingConfig();
 
 		validatePropertyMapping(mapping, mapper, resultMap);
 
 		propMapping.setProperty(mapping.property());
 		propMapping.setColumn(mapping.column());
 		propMapping.setColumnIndex(mapping.columnIndex());
 		if (propMapping.getColumnIndex() == 0
 				&& (propMapping.getColumn() == null || "".equals(propMapping.getColumn()))) {
 			propMapping.setColumn(propMapping.getProperty());
 		}
 		if (!"".equals(mapping.nestedMap().value())) {
 			Class<?> clazz = mapping.nestedMap().declaringClass();
 			if (clazz == Object.class) {
 				clazz = mapper;
 			}
 			ResultMapConfig nestedMapConfig = findResultMap(clazz, mapping.nestedMap().value());
 
 			if (nestedMapConfig == null) {
 				throw new ConfigurationException(Messages.nestedMapNotFound(mapper, method,
 						resultMap, mapping));
 			}
 
 			propMapping.setNestedMapConfig(nestedMapConfig);
 		}
 		// TODO: Validate method present!
 		if (!"".equals(mapping.subselect())) {
 			// TODO: Map it?
 			Class<?> subselectMapper = mapping.subselectMapper() != Object.class ? mapping
 					.subselectMapper() : mapper;
 
 			// null parameters mean that parameters are not known.
 			// in that case, any matching method will be used
 			// FIXME: Derive parameters from the property type!
 			StatementKey subselectKey = new StatementKey(subselectMapper, mapping.subselect(), null);
 
 			// Subselect properties requires post-configuration (the referenced
 			// query could be not configured yet, so wait until all queries are processed)
 			final SubselectConfig subselectInfo = new SubselectConfig(subselectKey, propMapping,
 					mapper, resultMap);
 			postConfigureList.add(new Runnable() {
 				public void run() {
 					subselectPostConfigure(subselectInfo);
 				}
 			});
 		}
 		return propMapping;
 	}
 
 	/**
 	 * Post configuration step for the subselects. Searches for the referenced
 	 * statement.
 	 * 
 	 * @param subselectInfo
 	 */
 	private void subselectPostConfigure(SubselectConfig subselectInfo) {
 		StatementKey key = subselectInfo.getSubselectKey();
 		StatementConfig stConfig = statementsConfig.get(key);
 
 		if (stConfig == null) {
 			throw new ConfigurationException(Messages.subselectNotFound(subselectInfo));
 		}
 
 		if (stConfig.getParameterTypes().length != 1) {
 			throw new ConfigurationException(Messages.subselectParameterCount(subselectInfo));
 		}
 		subselectInfo.getPropertyMapping().setSubselect(stConfig);
 	}
 
 	/**
 	 * Find result map config with given reference id.
 	 * 
 	 * @param clazz declaring class
 	 * @param refId reference id
 	 * @return result map config
 	 */
 	private ResultMapConfig findResultMap(Class<?> clazz, String refId)
 			throws ConfigurationException {
 		String key = clazz.getName() + "#" + refId;
 
 		if (!mapped.contains(clazz)) {
 			throw new ConfigurationException(Messages.notMapped(clazz, refId));
 		}
 
 		ResultMapConfig resultMapConfig = resultMapsConfig.get(key);
 
 		// Search in superinterfaces
 		if (resultMapConfig == null) {
 			for (Class<?> superMapper : clazz.getInterfaces()) {
 				resultMapConfig = findResultMap(superMapper, refId);
 				if (resultMapConfig != null) {
 					break;
 				}
 			}
 		}
 		return resultMapConfig;
 	}
 
 	/** @return Returns the typeHandlerFactory. */
 	public TypeHandlerFactory getTypeHandlerFactory() {
 		return typeHandlerFactory;
 	}
 
 	/** @return Returns the introspectionFactory. */
 	public IntrospectionFactory getIntrospectionFactory() {
 		return introspectionFactory;
 	}
 
 	/**
 	 * Validate that every property mentioned in the groupBy is explicitly
 	 * configured.
 	 * 
 	 * @param mapper mapper
 	 * @param method mapper method result map is applied to
 	 * @param resultMap result map
 	 * @param propnames configured property names
 	 */
 	private void validateGroupBy(Class<?> mapper, Method method, ResultMap resultMap,
 			Set<String> propnames) throws ConfigurationException {
 
 		for (String prop : resultMap.groupBy()) {
 			if (!propnames.contains(prop)) {
				throw new ConfigurationException(Messages.groupByPropertyMissing(prop, mapper, method, 
 						resultMap));
 			}
 		}
 	}
 
 	/**
 	 * Validate usage of {@link SelectKey} annotation.
 	 * 
 	 * @param selectKey
 	 * @param mapper
 	 * @param method
 	 */
 	private void validateSelectKey(SelectKey selectKey, Class<?> mapper, Method method)
 			throws ConfigurationException {
 		if (selectKey != null && selectKey.type() == SelectKeyType.BEFORE) {
 			if (selectKey.value().length() == 0) {
 				throw new ConfigurationException(Messages.beforeKeyWithoutSQL(mapper, method));
 			}
 
 			if (selectKey.property().length() == 0) {
 				throw new ConfigurationException(Messages.beforeKeyWithoutProperty(mapper, method));
 			}
 		}
 	}
 
 	/**
 	 * Validate property mapping.
 	 * 
 	 * @param mapping
 	 * @param mapper
 	 * @param resultMap
 	 */
 	private void validatePropertyMapping(Mapping mapping, Class<?> mapper, ResultMap resultMap)
 			throws ConfigurationException {
 		if (mapping.columnIndex() != 0 && mapping.column().length() > 0) {
 			throw new ConfigurationException(Messages.multipleColumn(mapping, mapper, resultMap));
 		}
 
 		if (mapping.property().length() == 0) {
 			throw new ConfigurationException(Messages.emptyProperty(mapping, mapper, resultMap));
 		}
 
 		if (mapping.nestedMap().value().length() > 0) {
 			if (mapping.subselect().length() > 0) {
 				throw new ConfigurationException(Messages.bothSubselectNested(mapping, mapper,
 						resultMap));
 			}
 
 			for (String prop : resultMap.groupBy()) {
 				if (mapping.property().equals(prop)) {
 					throw new ConfigurationException(Messages.bothNestedGroupBy(mapping, mapper,
 							resultMap));
 				}
 			}
 		}
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return new ToStringBuilder(this).append("resultMapsConfig", resultMapsConfig).append(
 				"statementsConfig", statementsConfig).append("typeHandlerFactory",
 				typeHandlerFactory).append("introspectionFactory", introspectionFactory).toString();
 	}
 }
