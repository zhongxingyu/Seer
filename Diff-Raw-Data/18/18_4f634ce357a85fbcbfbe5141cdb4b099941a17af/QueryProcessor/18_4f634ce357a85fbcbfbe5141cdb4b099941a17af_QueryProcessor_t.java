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
 package org.oobium.persist.db.internal;
 
 import static org.oobium.persist.db.internal.DbCache.getCache;
 import static org.oobium.persist.db.internal.QueryUtils.ID;
 import static org.oobium.persist.db.internal.QueryUtils.createModel;
 import static org.oobium.persist.db.internal.QueryUtils.valuePattern;
 import static org.oobium.utils.SqlUtils.asNestedFieldMaps;
 import static org.oobium.utils.SqlUtils.setObject;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.regex.Matcher;
 
 import org.oobium.logging.Logger;
 import org.oobium.logging.LogProvider;
 import org.oobium.persist.Model;
 import org.oobium.persist.db.DbPersistService;
 
 public class QueryProcessor<E extends Model> {
 
 	private static final Logger logger = LogProvider.getLogger(DbPersistService.class);
 
 	public static <T extends Model> QueryProcessor<T> create(Class<T> clazz, String sql, Object...values) throws SQLException {
 		QueryProcessor<T> processor = new QueryProcessor<T>();
 		processor.build(clazz, sql, values);
 		return processor;
 	}
 
 	private Query query;
 	private Object[] values;
 
 	private QueryProcessor() {
 		// private constructor
 	}
 	
 	private void build(Class<E> clazz, String sql, Object...values) throws SQLException {
 		if(sql != null) {
 			StringBuilder sb = new StringBuilder(sql);
 			int i = 0, ix = 0;
 			for(; i < values.length && ix != -1; i++, ix++) {
 				ix = sb.indexOf("?", ix);
 				if(ix != -1) {
 					sb.replace(ix, ix + 1, "#{" + i + "}");
 				}
 			}
 			if(i != values.length) {
 				throw new SQLException("The number of values does not match the number of place holders");
 			}
 			this.query = QueryBuilder.build(clazz, sb.toString(), values);
 		} else {
 			this.query = QueryBuilder.build(clazz, null);
 		}
 		this.values = values;
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private List createModels(Query query, List<Map<String, Map<String, Object>>> currentResults) throws SQLException {
 		try {
 			List models = new ArrayList();
 			for(Map<String, Map<String, Object>> nestedResult : currentResults) {
 				Model model = null;
 				Map<String, Model> map = null;
 				for(Entry<String, Map<String, Object>> entry : nestedResult.entrySet()) {
 					String alias = entry.getKey();
 					if(query.isSub() && "a".equals(alias)) {
 						continue;
 					}
 					
 					Map<String, Object> data = entry.getValue();
 					
 					Object id = data.get(ID);
 					if(id != null) {
 						Class<? extends Model> clazz = query.getType(alias);
 						Model object = getCache(clazz, (Integer) id);
 						if(object == null) {
 							object = createModel(clazz, data);
 						}
 						
 						if(model == null) {
 							model = object;
 							models.add(model);
 						} else {
 							String parentAlias = query.getParentAlias(alias);
 							Model parent = map.get(parentAlias);
 							String field = query.getField(alias);
 							parent.put(field, object);
 						}
 
 						if((query.isSub() && data.size() > 2) || (!query.isSub() && data.size() > 1)) {
 							if(map == null) {
 								map = new HashMap<String, Model>();
 							}
 							map.put(alias, object);
 						}
 					}
 				}
 				if(query.isSub() && model != null) {
 					String field = query.getField();
 					Class<? extends Model> parentClass = query.getParentClass();
 					Object parentId = nestedResult.get("a").get(ID);
 					Model parent = getCache(parentClass, (Integer) parentId);
 					Object collection = parent.get(field, false);
 					if(collection instanceof Set<?>) {
 						((Set) collection).add(model);
 					}
 				}
 			}
 			return models;
 		} catch(NoSuchFieldException e) {
 			logger.error(e.getMessage(), e);
 			throw new SQLException("cannot adapt class of type "/* + clazz.getSimpleName()*/);
 		}
 	}
 
 	private List<Map<String, Map<String, Object>>> executeQuery(Connection connection, String sql) throws SQLException {
 		if(logger.isLoggingDebug()) {
 			logger.debug("start executeQuery: " + sql);
 		}
 
 		StringBuilder sb = new StringBuilder(sql);
 		int[] ixs = new int[values.length];
 		if(values.length > 0) {
 			Arrays.fill(ixs, -1);
 		
 			Matcher matcher = valuePattern.matcher(sb);
 			for(int i = 0; matcher.find(); i++) {
 				ixs[i] = Integer.parseInt(matcher.group(1));
 				sb.replace(matcher.start(), matcher.end(), "?");
 				matcher = valuePattern.matcher(sb);
 			}
 		}
 		
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			ps = connection.prepareStatement(sb.toString());
 			for(int i = 0; i < values.length && ixs[i] != -1; i++) {
 				setObject(ps, i + 1, values[ixs[i]]);
 				if(logger.isLoggingTrace()) {
 					logger.trace((i + 1) + " <- " + values[ixs[i]]);
 				}
 			}
 
 			rs = ps.executeQuery();
 
 			List<Map<String, Map<String, Object>>> results = asNestedFieldMaps(rs);
 			return results;
 		} finally {
 			logger.debug("end executeQuery");
 			if(ps != null) {
 				ps.close();
 			}
 			if(rs != null) {
 				rs.close();
 			}
 		}
 	}
 
 	public List<E> process(Connection connection) throws SQLException {
 		try {
 			List<E> objects = processQuery(connection, query);
 			return objects;
 		} catch(Exception e) {
 			logger.error(e);
 			throw new SQLException(e);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private List<E> processQuery(Connection connection, Query query) throws SQLException {
 		String sql = query.getSql();
 		List<Map<String, Map<String, Object>>> results = executeQuery(connection, sql);
 		List<E> objects = createModels(query, results);
 		if(!objects.isEmpty() && query.hasChildren()) {
 			for(Query child : query.getChildren()) {
 				processQuery(connection, query, child, objects);
 			}
 		}
 		return objects;
 	}
 	
 	// hasMany: many to none, many to many, many to one
 	@SuppressWarnings("unchecked")
 	private void processQuery(Connection connection, Query parentQuery, Query query, List<? extends Model> models) throws SQLException {
 		String sql = query.getSql(models);
 		
 		List<Map<String, Map<String, Object>>> results = executeQuery(connection, sql);
		List<E> objects = createModels(query, results);
		if(!objects.isEmpty() && query.hasChildren()) {
			for(Query child : query.getChildren()) {
				processQuery(connection, query, child, objects);
 			}
 		}
		for(Model model : models) { // makes sure that the field is set for all models, even if no results (empty set)
			model.get(query.getField(), false);
		}
 	}
 	
 }
