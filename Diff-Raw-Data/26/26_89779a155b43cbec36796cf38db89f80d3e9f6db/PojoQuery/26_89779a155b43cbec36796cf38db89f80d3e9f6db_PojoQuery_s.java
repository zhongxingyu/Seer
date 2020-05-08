 package nl.pojoquery;
 
 import static nl.pojoquery.util.Strings.implode;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Modifier;
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.sql.DataSource;
 
 import nl.pojoquery.QueryStructure.Alias;
 import nl.pojoquery.annotations.Embedded;
 import nl.pojoquery.annotations.GroupBy;
 import nl.pojoquery.annotations.Id;
 import nl.pojoquery.annotations.Join;
 import nl.pojoquery.annotations.Link;
 import nl.pojoquery.annotations.Other;
 import nl.pojoquery.annotations.Select;
 import nl.pojoquery.annotations.SubClasses;
 import nl.pojoquery.annotations.Table;
 import nl.pojoquery.annotations.Transient;
 import nl.pojoquery.util.Iterables;
 
 public class PojoQuery<T> {
 
 	private Class<T> resultClass;
 	private String table;
 	private List<String> fields = new ArrayList<String>();
 	private List<String> joins = new ArrayList<String>();
 	private List<SqlExpression> wheres = new ArrayList<SqlExpression>();
 	private List<String> groupBy = new ArrayList<String>();
 	private List<String> orderBy = new ArrayList<String>();
 	private int offset = -1;
 	private int rowCount = -1;
 
 	public PojoQuery(String table) {
 		this.table = table;
 	}
 
 	public PojoQuery<T> addField(String expression) {
 		fields.add(expression);
 		return this;
 	}
 
 	public PojoQuery<T> addJoin(String join) {
 		joins.add(join);
 		return this;
 	}
 
 	public PojoQuery<T> addGroupBy(String group) {
 		groupBy.add(group);
 		return this;
 	}
 
 	public PojoQuery<T> addWhere(String where) {
 		wheres.add(new SqlExpression(where));
 		return this;
 	}
 
 	public PojoQuery<T> addWhere(String sql, Object... params) {
 		wheres.add(new SqlExpression(sql, Arrays.asList(params)));
 		return this;
 	}
 
 	public PojoQuery<T> addOrderBy(String order) {
 		orderBy.add(order);
 		return this;
 	}
 
 	public PojoQuery<T> setLimit(int offset, int rowCount) {
 		this.offset = offset;
 		this.rowCount = rowCount;
 		return this;
 	}
 
 	public PojoQuery<T> setResultClass(Class<T> resultClass) {
 		this.resultClass = resultClass;
 		return this;
 	}
 
 	public String toSql() {
 		return toStatement().getSql();
 	}
 
 	private SqlExpression toStatement() {
 		return toStatement(new SqlExpression("SELECT\n " + implode(",\n ", fields)), table, joins, wheres, groupBy, orderBy, offset, rowCount);
 	}
 
 	private static SqlExpression toStatement(SqlExpression selectClause, String from, List<String> joins, List<SqlExpression> wheres, List<String> groupBy, List<String> orderBy, int offset, int rowCount) {
 
 		List<Object> params = new ArrayList<Object>();
 		Iterables.addAll(params, selectClause.getParameters());
 
 		SqlExpression whereClause = buildWhereClause(wheres);
 		Iterables.addAll(params, whereClause.getParameters());
 
 		String groupByClause = buildClause("GROUP BY", groupBy);
 		String orderByClause = buildClause("ORDER BY", orderBy);
 		String limitClause = buildLimitClause(offset, rowCount);
 
 		String sql = implode(" ", Arrays.asList(selectClause.getSql(), "\nFROM", from, "\n", implode("\n ", joins), whereClause == null ? "" : whereClause.getSql(), groupByClause, orderByClause, limitClause));
 
 		return new SqlExpression(sql, params);
 	}
 
 	private static String buildLimitClause(int offset, int rowCount) {
 		String limitClause = "";
 		if (offset > -1 || rowCount > -1) {
 			if (rowCount < 0) {
 				// No rowcount
 				rowCount = Integer.MAX_VALUE;
 			}
 			if (offset > -1) {
 				limitClause = "\nLIMIT " + offset + "," + rowCount;
 			} else {
 				limitClause = "\nLIMIT " + rowCount;
 			}
 		}
 		return limitClause;
 	}
 
 	private static SqlExpression buildWhereClause(List<SqlExpression> parts) {
 		List<Object> parameters = new ArrayList<Object>();
 		String whereClause = "";
 		if (parts.size() > 0) {
 			List<String> clauses = new ArrayList<String>();
 			for (SqlExpression exp : parts) {
 				clauses.add(exp.getSql());
 				for (Object o : exp.getParameters()) {
 					parameters.add(o);
 				}
 			}
 			whereClause = "\nWHERE " + implode("\n AND ", clauses);
 		}
 		return new SqlExpression(whereClause, parameters);
 	}
 
 	private static String buildClause(String preamble, List<String> parts) {
 		String groupByClause = "";
 		if (parts != null && parts.size() > 0) {
 			groupByClause = "\n" + preamble + " " + implode(", ", parts);
 		}
 		return groupByClause;
 	}
 
 	public List<T> execute(DataSource db) {
 		return processRows(DB.queryRows(db, toStatement()), resultClass);
 	}
 
 	public List<T> execute(Connection connection) {
 		return processRows(DB.queryRows(connection, toStatement()), resultClass);
 	}
 
 	public static <R> List<R> execute(DataSource db, Class<R> clz, String sql, Object... params) {
 		return processRows(DB.queryRows(db, sql, params), clz);
 	}
 
 	public T findById(Connection connection, Object id) {
 		wheres.addAll(buildIdCondition(resultClass, id));
 		return returnSingleRow(execute(connection));
 	}
 
 	public T findById(DataSource db, Object id) {
 		wheres.addAll(buildIdCondition(resultClass, id));
 		return returnSingleRow(execute(db));
 	}
 
 	public static void delete(DataSource db, Object entity) {
 		for (TableMapping table : determineTableMapping(entity.getClass())) {
 			executeDelete(db, table.tableName, buildIdConditionFromEntity(entity.getClass(), entity));
 		}
 	}
 	
 	public static void deleteById(DataSource db, Class<?> clz, Object id) {
 		for (TableMapping table : determineTableMapping(clz)) {
 			List<SqlExpression> wheres = buildIdCondition(table.clazz, id);
 			executeDelete(db, table.tableName, wheres);
 		}
 	}
 
 	private static void executeDelete(DataSource db, String tableName, List<SqlExpression> where) {
 		List<String> whereClauses = new ArrayList<String>();
 		List<Object> params = new ArrayList<Object>();
 		for (SqlExpression w : where) {
 			whereClauses.add(w.getSql());
 			for (Object o : w.getParameters()) {
 				params.add(o);
 			}
 		}
 
 		DB.update(db, new SqlExpression("DELETE FROM `" + tableName + "` WHERE " + implode(" AND ", whereClauses), Arrays.asList((Object) params)));
 	}
 
 	private T returnSingleRow(List<T> resultList) {
 		if (resultList.size() == 1) {
 			return resultList.get(0);
 		}
 		if (resultList.size() > 1) {
 			throw new RuntimeException("More than one result found in findById on class " + resultClass.getName());
 		}
 		return null;
 	}
 
 	private static List<SqlExpression> buildIdCondition(Class<?> resultClass, Object id) {
 		List<Field> idFields = assertIdFields(resultClass);
 		String tableName = determineTableMapping(resultClass).get(0).tableName;
 		if (idFields.size() == 1) {
 			return Arrays.asList(new SqlExpression("`" + tableName + "`." + idFields.get(0).getName() + "=?", Arrays.asList((Object) id)));
 		} else {
 			if (id instanceof Map) {
 				@SuppressWarnings("unchecked")
 				Map<String, Object> idvalues = (Map<String, Object>) id;
 
 				List<SqlExpression> result = new ArrayList<SqlExpression>();
 				for (String field : idvalues.keySet()) {
 					result.add(new SqlExpression("`" + tableName + "`." + field + "=?", Arrays.asList((Object) idvalues.get(field))));
 				}
 				return result;
 			} else {
 				throw new MappingException("Multiple @Id annotations on class " + resultClass.getName() + ": expecting a map id.");
 			}
 		}
 	}
 
 	private static List<SqlExpression> buildIdConditionFromEntity(Class<?> resultClass, Object entity) {
 		try {
 			List<Field> idFields = assertIdFields(resultClass);
 			String tableName = determineTableMapping(resultClass).get(0).tableName;
 			List<SqlExpression> result = new ArrayList<SqlExpression>();
 			for (Field field : idFields) {
 				Object idvalue = idFields.get(0).get(entity);
 				if (idvalue == null) {
 					throw new MappingException("Cannot create wherecondition for entity with null value in idfield " + field.getName());
 				}
 				result.add(new SqlExpression("`" + tableName + "`." + field + "=?", Arrays.asList(idvalue)));
 			}
 			return result;
 		} catch (IllegalArgumentException e) {
 			throw new MappingException(e);
 		} catch (IllegalAccessException e) {
 			throw new MappingException(e);
 		}
 	}
 	
 	private static List<Field> assertIdFields(Class<?> resultClass) {
 		List<Field> idFields = determineIdFields(resultClass);
 		if (idFields.size() == 0) {
 			throw new MappingException("No @Id annotations found on fields of class " + resultClass.getName());
 		}
 		return idFields;
 	}
 
 	public static <R> List<R> processRows(List<Map<String, Object>> rows, Class<R> clz) {
 		QueryStructure structure = createQueryStructure(clz);
 		return processRows(rows, structure);
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private static <R> List<R> processRows(List<Map<String, Object>> rows, QueryStructure structure) {
 		try {
 			List<R> result = new ArrayList<R>(rows.size());
 
 			Map<String, Map<Object, Object>> allEntities = new HashMap<String, Map<Object, Object>>();
 
 			for (Map<String, Object> row : rows) {
 				// Collect all entities for this row
 				Map<String, Object> onThisRow = new HashMap<String, Object>();
 
 				// Create entities for non-null subclasses
 				for (Alias subClass : structure.subClasses) {
 					String idField = subClass.idFields.get(0).getName();
 					if (row.get(subClass.alias + "." + idField) == null) {
 						continue;
 					}
 					onThisRow.put(subClass.superAlias.alias, createInstance(subClass.resultClass));
 				}
 
 				for (String key : row.keySet()) {
 					String tableAlias = structure.principalAlias.alias;
 					int dotpos = key.lastIndexOf('.');
 					if (dotpos > 0) {
 						tableAlias = key.substring(0, dotpos);
 					}
 
 					Alias alias = structure.aliases.get(tableAlias);
 					Object instance;
 					if (alias.isSubClass()) {
 						String idField = alias.idFields.get(0).getName();
 						instance = onThisRow.get(alias.superAlias.alias);
 						if (row.get(tableAlias + "." + idField) == null) {
 							continue;
 						}
 					} else {
 						instance = onThisRow.get(tableAlias);
 					}
 					if (instance == null) {
 						Class<?> valClass = alias.resultClass;
 						if (!valClass.isEnum()) {
 							instance = createInstance(valClass);
 							onThisRow.put(tableAlias, instance);
 						} else {
 							Object val = row.get(key);
 							if (val instanceof String) {
 								instance = Enum.valueOf((Class<? extends Enum>) valClass, (String) val);
 								onThisRow.put(tableAlias, instance);
 							}
 							continue;
 						}
 					}
 
 					Field f = structure.classFields.get(key);
 					if (f != null) {
 						f.setAccessible(true);
 						if (row.get(key) != null || !f.getType().isPrimitive()) {
 							Object val = row.get(key);
 							if (val instanceof String && f.getType().isEnum()) {
 								val = Enum.valueOf((Class<? extends Enum>) f.getType(), (String) val);
 							}
 							f.set(instance, val);
 						}
 					} else {
 						if (alias.otherField != null) {
 							alias.otherField.setAccessible(true);
 							Map<String,Object> othersMap = (Map<String, Object>) alias.otherField.get(instance);
 							if (othersMap == null) {
 								othersMap = new HashMap<String,Object>();
 								alias.otherField.set(instance, othersMap);
 							}
 							String fieldName = key.substring(key.lastIndexOf(".") + 1);
 							Other otherAnn = alias.otherField.getAnnotation(Other.class);
 							if (otherAnn != null && otherAnn.prefix().length() > 0) {
 								// Remove prefix.
 								fieldName = fieldName.substring(otherAnn.prefix().length());
 							}
 							othersMap.put(fieldName, row.get(key));
 						}
 					}
 				}
 
 				for (String tableAlias : onThisRow.keySet()) {
 					if (allEntities.get(tableAlias) == null) {
 						allEntities.put(tableAlias, new HashMap<Object, Object>());
 					}
 					Object entity = onThisRow.get(tableAlias);
 					if (entity.getClass().isEnum()) {
 						continue;
 					}
 					Alias alias = structure.aliases.get(tableAlias);
 					List<Field> ids = alias.idFields;
 					Object id;
 					boolean isNull = true;
 					if (ids.size() == 1) {
 						id = ids.get(0).get(entity);
 						isNull = id == null;
 					} else {
 						id = new ArrayList<Object>();
 						isNull = true;
 						for (Field f : ids) {
 							Object val = f.get(entity);
 							if (val != null) {
 								isNull = false;
 							}
 							((List<Object>) id).add(val);
 						}
 					}
 					if (isNull) {
 						onThisRow.put(tableAlias, null);
 					} else {
 						Map<Object, Object> all = allEntities.get(tableAlias);
 						if (all.containsKey(id)) {
 							onThisRow.put(tableAlias, all.get(id));
 						} else {
 							all.put(id, entity);
 							if (alias.isPrincipalAlias()) {
 								result.add((R) entity);
 							}
 						}
 					}
 				}
 
 				for (String tableAlias : onThisRow.keySet()) {
 					Alias alias = structure.aliases.get(tableAlias);
 					if (alias.isPrincipalAlias() || alias.linkField == null) {
 						continue;
 					}
 					Field linkField = alias.linkField;
 					linkField.setAccessible(true);
 					Object parentEntity = onThisRow.get(alias.parent.alias);
 					if (parentEntity != null) {
 						Object entity = onThisRow.get(tableAlias);
 						if (List.class.isAssignableFrom(linkField.getType())) {
 							List<Object> coll = (List<Object>) linkField.get(parentEntity);
 							if (coll == null) {
 								coll = new ArrayList<Object>();
 								linkField.set(parentEntity, coll);
 							}
 							if (!coll.contains(entity) && entity != null) {
 								coll.add(entity);
 							}
 						} else if (Set.class.isAssignableFrom(linkField.getType())) {
 							Set<Object> coll = (Set<Object>) linkField.get(parentEntity);
 							if (coll == null) {
 								coll = new HashSet<Object>();
 								linkField.set(parentEntity, coll);
 							}
 							if (!coll.contains(entity) && entity != null) {
 								coll.add(entity);
 							}
 						} else if (linkField.getType().isArray()) {
 							Object arr = linkField.get(parentEntity);
 							if (arr == null) {
 								arr = Array.newInstance(linkField.getType().getComponentType(), 0);
 							}
 							boolean contains = false;
 							int arrlen = Array.getLength(arr);
 							for (int i = 0; i < arrlen; i++) {
 								if (Array.get(arr, i).equals(entity)) {
 									contains = true;
 									break;
 								}
 							}
 							if (!contains && entity != null) {
 								Object extended = Array.newInstance(linkField.getType().getComponentType(), arrlen + 1);
 								if (arrlen > 0) {
 									System.arraycopy(arr, 0, extended, 0, arrlen);
 								}
 								Array.set(extended, arrlen, entity);
 								arr = extended;
 							}
 							linkField.set(parentEntity, arr);
 						} else {
 							linkField.set(parentEntity, entity);
 						}
 					}
 				}
 			}
 			return result;
 		} catch (Exception e) {
 			throw new MappingException(e);
 		}
 	}
 
 	public static <R> QueryStructure createQueryStructure(Class<R> clz) {
 		String table = determineTableMapping(clz).get(0).tableName;
 		QueryStructure structure = new QueryStructure();
 		addClassToStructure(structure, table, null, clz, null);
 		return structure;
 	}
 
 	private static Object createInstance(Class<?> valClass) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		Object instance;
 		Constructor<?> constructor = valClass.getDeclaredConstructor();
 		constructor.setAccessible(true);
 		instance = constructor.newInstance();
 		return instance;
 	}
 
 	public static <T> PojoQuery<T> build(Class<T> clz) {
 		if (clz == null)
 			throw new NullPointerException("clz");
 
 		String table = determineTableMapping(clz).get(0).tableName;
 
 		PojoQuery<T> q = new PojoQuery<T>(table);
 		q.setResultClass(clz);
 
 		if (clz.getAnnotation(Join.class) != null) {
 			String[] joins = clz.getAnnotation(Join.class).value();
 			for (String join : joins) {
 				q.addJoin(join);
 			}
 		}
 
 		if (clz.getAnnotation(GroupBy.class) != null) {
 			String[] groupBy = clz.getAnnotation(GroupBy.class).value();
 			for (String group : groupBy) {
 				q.addGroupBy(group);
 			}
 		}
 
 		addClassToQuery(q, table, clz, new ArrayList<Class<?>>());
 
 		return q;
 	}
 
 	private static <T> void addClassToQuery(PojoQuery<T> q, String tableAlias, Class<?> clz, List<Class<?>> joinPath) {
 
 		List<Class<?>> subclassesAdded = new ArrayList<Class<?>>();
 
 		sanityCheck(clz);
 
 		addClassToQueryIgnoringSubClasses(q, tableAlias, clz, joinPath, subclassesAdded);
 
 		// Add classes subclass up
 		if (clz.getAnnotation(SubClasses.class) != null) {
 			// Joined subclasses
 			for (Class<?> subClass : clz.getAnnotation(SubClasses.class).value()) {
 				List<TableMapping> tableMappings = determineTableMapping(subClass);
 				TableMapping mapping = tableMappings.get(tableMappings.size() - 1);
 				String foreignalias = combinedAlias(tableAlias, subClass.getSimpleName().toLowerCase(), joinPath.size() == 0);
 				String idField = determineIdField(subClass).getName();
 				String linkfield = "`" + foreignalias + "`." + idField;
 				String localidfield = "`" + tableAlias + "`." + idField;
 				q.addJoin("LEFT JOIN " + mapping.tableName + " `" + foreignalias + "` ON " + linkfield + "=" + localidfield);
 
 				q.addField("`" + foreignalias + "`." + idField + " `" + foreignalias + "." + idField + "`");
 				addClassToQueryIgnoringSubClasses(q, foreignalias, subClass, joinPath, subclassesAdded);
 
 			}
 		}
 
 	}
 
 	private static <T> void addClassToQueryIgnoringSubClasses(PojoQuery<T> q, String tableAlias, Class<?> clz, List<Class<?>> joinPath, List<Class<?>> subclassesAdded) {
 
 		List<TableMapping> tableMappings = determineTableMapping(clz);
 		if (tableMappings.size() == 0) {
 			throw new MappingException("Missing @Table annotation on class " + clz.getName() + " or any of its superclasses");
 		}
 
 		// Throw in the super types, top type first
 		TableMapping superType = null;
 		for (TableMapping mapping : tableMappings) {
 			if (subclassesAdded.contains(mapping.clazz)) {
 				continue;
 			}
 			subclassesAdded.add(mapping.clazz);
 			if (superType == null) {
 				// this is the top type
 				addFieldsToQueryIgnoreSuperClasses(q, tableAlias, mapping.clazz, mapping.fields, joinPath, mapping.tableName);
 			} else {
 				// Some super class
 				String foreignalias = combinedAlias(tableAlias, mapping.tableName, joinPath.size() == 0);
 				String idField = determineIdField(mapping.clazz).getName();
 				String linkfield = "`" + foreignalias + "`." + idField;
 				String localidfield = "`" + tableAlias + "`." + idField;
 				q.addJoin("INNER JOIN " + mapping.tableName + " `" + foreignalias + "` ON " + linkfield + "=" + localidfield);
 
 				q.addField("`" + foreignalias + "`." + idField + " `" + foreignalias + "." + idField + "`");
 				addFieldsToQueryIgnoreSuperClasses(q, foreignalias, superType.clazz, mapping.fields, joinPath, mapping.tableName);
 
 				tableAlias = foreignalias;
 			}
 			superType = mapping;
 		}
 	}
 
 	private static <T> void addFieldsToQueryIgnoreSuperClasses(PojoQuery<T> q, String tableAlias, Class<?> clz, Iterable<Field> fields, List<Class<?>> joinPath, String table) {
 		boolean isPrincipalAlias = joinPath.size() == 0;
 
 		if (joinPath.contains(clz)) {
 			// Loop!
 			throw new MappingException("Reference loop detected processing " + q.resultClass + " at property " + tableAlias + " referencing " + clz.getSimpleName());
 		}
 		joinPath.add(clz);
 
 		if (fields == null) {
 			fields = collectFieldsOfClass(clz);
 		}
 
 		for (Field f : fields) {
 			if (f.getAnnotation(Other.class) != null) {
 				continue;
 			}
 
 			if (f.getAnnotation(Link.class) != null) {
 				String linktable = f.getAnnotation(Link.class).linktable();
 				if (!linktable.equals(Link.NONE)) {
 					String foreignvaluefield = f.getAnnotation(Link.class).foreignvaluefield();
 					if (!foreignvaluefield.equals(Link.NONE)) {
 						// A list of values
 						String idfield = tableAlias + "." + determineIdField(clz).getName();
 						String linktableAlias = combinedAlias(tableAlias, linktable, isPrincipalAlias);
 						String linkfield = "`" + linktableAlias + "`." + table + "_id";
 
 						q.addJoin("LEFT JOIN " + linktable + " `" + linktableAlias + "` ON " + linkfield + "=" + idfield);
 
 						q.addField("`" + linktableAlias + "`." + foreignvaluefield + " `" + combinedAlias(tableAlias, f.getName(), isPrincipalAlias) + ".value`");
 					} else {
 						// many to many
 						String idfield = tableAlias + "." + determineIdField(clz).getName();
 						String linktableAlias = combinedAlias(tableAlias, linktable, isPrincipalAlias);
 						String linkfield = "`" + linktableAlias + "`." + table + "_id";
 
 						q.addJoin("LEFT JOIN " + linktable + " `" + linktableAlias + "` ON " + linkfield + "=" + idfield);
 
 						String foreigntable = null;
 						for (String t : linktable.split("_")) {
 							if (tableAlias.equalsIgnoreCase(t)) {
 								continue;
 							} else {
 								foreigntable = t;
 							}
 						}
 						Class<?> foreignClass = f.getAnnotation(Link.class).resultClass();
 						String foreignlinkfield = "`" + linktableAlias + "`." + foreigntable + "_id";
 						String foreignAlias = combinedAlias(linktableAlias, f.getName(), isPrincipalAlias);
 						String foreignidfield = "`" + foreignAlias + "`." + determineIdField(foreignClass).getName();
 
 						q.addJoin("LEFT JOIN " + foreigntable + " " + foreignAlias + " ON " + foreignlinkfield + "=" + foreignidfield);
 
 						addClassToQuery(q, foreignAlias, foreignClass, joinPath);
 					}
 
 				} else {
 					Class<?> foreignClass = f.getAnnotation(Link.class).resultClass();
 					String foreigntable = foreignClass.getAnnotation(Table.class).value();
 					String foreignalias = combinedAlias(tableAlias, f.getName(), isPrincipalAlias);
 					String foreignlinkfield = "`" + foreignalias + "`." + table + "_id";
 					String idfield = "`" + tableAlias + "`." + determineIdField(clz).getName();
 					q.addJoin("LEFT JOIN " + foreigntable + " `" + foreignalias + "` ON " + foreignlinkfield + "=" + idfield);
 
 					addClassToQuery(q, foreignalias, foreignClass, joinPath);
 				}
 				continue;
 			}
 			if (f.getType().isArray()) {
 				List<TableMapping> linkedMapping = determineTableMapping(f.getType().getComponentType());
 				if (linkedMapping.size() > 0) {
 					String foreignalias = combinedAlias(tableAlias, f.getName(), isPrincipalAlias);
 					addOneToManyLink(q, tableAlias, table, foreignalias, linkedMapping.get(0).tableName, f.getName());
 					addClassToQuery(q, foreignalias, f.getType().getComponentType(), joinPath);
 				}
 				continue;
 			} else {
 				List<TableMapping> linkedMapping = determineTableMapping(f.getType());
 				if (!f.getType().isPrimitive() && linkedMapping.size() > 0) {
 					String foreignalias = combinedAlias(tableAlias, f.getName(), isPrincipalAlias);
 					addManyToOneLink(q, tableAlias, foreignalias, linkedMapping.get(0).tableName, f.getName());
 					addClassToQuery(q, foreignalias, f.getType(), joinPath);
 					continue;
 				}
 			}
 			if (f.getAnnotation(Select.class) != null) {
 				q.addField(f.getAnnotation(Select.class).value() + " `" + tableAlias + "." + f.getName() + "`");
 			} else {
 				if (f.getAnnotation(Embedded.class) != null) {
 					String prefix = determinePrefix(f);
 
 					String foreignalias = combinedAlias(tableAlias, f.getName(), isPrincipalAlias);
 					for (Field embeddedField : collectFieldsOfClass(f.getType())) {
 						q.addField("`" + tableAlias + "`." + prefix + embeddedField.getName() + " `" + foreignalias + "." + embeddedField.getName() + "`");
 					}
 					continue;
 				}
 
 				q.addField("`" + tableAlias + "`." + f.getName() + " `" + tableAlias + "." + f.getName() + "`");
 			}
 		}
 
 		joinPath.remove(joinPath.size() - 1);
 	}
 
 	private static void sanityCheck(Class<?> clz) {
 		if (clz.getEnclosingClass() != null && (clz.getModifiers() & Modifier.STATIC) == 0) {
 			throw new MappingException("Cannot map non-static inner class " + clz.getName());
 		}
 	}
 
 	private static String determinePrefix(Field f) {
 		String prefix = f.getAnnotation(Embedded.class).prefix();
 		if (prefix.equals(Embedded.DEFAULT)) {
 			prefix = f.getName();
 		}
 		if (!prefix.isEmpty()) {
 			prefix = prefix + "_";
 		}
 		return prefix;
 	}
 
 	private static <T> void addOneToManyLink(PojoQuery<T> q, String tableAlias, String localTable, String foreignalias, String foreigntable, String fieldName) {
 		String linkfield = "`" + foreignalias + "`." + localTable + "_id";
 		String idfield = "`" + tableAlias + "`.id";
 		q.addJoin("LEFT JOIN " + foreigntable + " `" + foreignalias + "` ON " + linkfield + "=" + idfield);
 	}
 
 	private static <T> void addManyToOneLink(PojoQuery<T> q, String tableAlias, String foreignalias, String foreigntable, String fieldName) {
 		String linkfield = "`" + tableAlias + "`." + fieldName.toLowerCase() + "_id";
 		String idfield = "`" + foreignalias + "`.id";
 		q.addJoin("LEFT JOIN " + foreigntable + " `" + foreignalias + "` ON " + linkfield + "=" + idfield);
 	}
 
 	private static String combinedAlias(String tableAlias, String fieldName, boolean isPrincipalAlias) {
 		if (!isPrincipalAlias) {
 			return tableAlias + "." + fieldName;
 		}
 		return fieldName;
 	}
 
 	private static void addClassToStructure(QueryStructure structure, String linkName, Alias parent, Class<?> resultClass, Field linkField) {
 
 		List<Class<?>> subclassesAdded = new ArrayList<Class<?>>();
 
 		Alias parentAlias = addClassToStructureIgnoringSubclasses(structure, linkName, parent, resultClass, linkField, subclassesAdded);
 
 		SubClasses subClassesAnn = resultClass.getAnnotation(SubClasses.class);
 		if (subClassesAnn != null) {
 			for (Class<?> subClass : subClassesAnn.value()) {
 				List<TableMapping> mappings = determineTableMapping(subClass);
 				TableMapping mapping = mappings.get(mappings.size() - 1);
 
 				Alias alias = structure.addSubClass(mapping.clazz.getSimpleName().toLowerCase(), parentAlias, subClass);
 				addFieldsToStructure(structure, alias, mapping.fields);
 			}
 		}
 	}
 
 	private static Alias addClassToStructureIgnoringSubclasses(QueryStructure structure, String linkName, Alias parent, Class<?> resultClass, Field linkField, List<Class<?>> subclassesAdded) {
 		// Check if this class inherits from a super class in another table
 		// If so, add them top type down
 		List<TableMapping> tableHierarchy = determineTableMapping(resultClass);
 		Alias parentAlias = parent;
 		for (TableMapping mapping : tableHierarchy) {
 			if (subclassesAdded.contains(mapping.clazz)) {
 				continue;
 			}
 			subclassesAdded.add(mapping.clazz);
 			if (parentAlias != parent) {
 				linkName = mapping.tableName;
 			}
 			Alias alias = structure.createAlias(linkName, parentAlias, resultClass, linkField);
 			addFieldsToStructure(structure, alias, mapping.fields);
 			subclassesAdded.add(mapping.clazz);
 			parentAlias = alias;
 			linkField = null;
 		}
 		return parentAlias;
 	}
 
 	private static void addFieldsToStructure(QueryStructure structure, Alias alias, Iterable<Field> fields) {
 		List<Field> nonLinkFields = new ArrayList<Field>();
 
 		if (fields == null) {
 			fields = collectFieldsOfClass(alias.resultClass);
 		}
 
 		for (Field f : fields) {
 			structure.classFields.put(alias.alias + "." + f.getName(), f);
 
 			if (f.getAnnotation(Id.class) != null) {
 				alias.idFields.add(f);
 			}
 
 			if (f.getAnnotation(Other.class) != null) {
 				alias.otherField = f;
 				continue;
 			}
 			if (f.getAnnotation(Link.class) != null) {
 				Class<?> linkedClass;
 				if (f.getType().isArray()) {
 					linkedClass = f.getType().getComponentType();
 				} else {
 					linkedClass = f.getAnnotation(Link.class).resultClass();
 				}
 
 				if (f.getAnnotation(Link.class).foreignvaluefield().equals(Link.NONE)) {
 					addClassToStructure(structure, f.getName(), alias, linkedClass, f);
 				} else {
 					structure.createAlias(f.getName(), alias, linkedClass, f);
 				}
 			} else if (!f.getType().isPrimitive() && f.getType().getAnnotation(Table.class) != null) {
 				addClassToStructure(structure, f.getName(), alias, f.getType(), f);
 			} else if (!f.getType().isPrimitive() && f.getAnnotation(Embedded.class) != null) {
 				Alias embedded = structure.createAlias(f.getName(), alias, f.getType(), f);
 				addFieldsToStructure(structure, embedded, null);
 			} else if (f.getType().isArray()) {
 				addClassToStructure(structure, f.getName(), alias, f.getType().getComponentType(), f);
 			} else {
 				nonLinkFields.add(f);
 			}
 		}
 
 		if (alias.idFields.size() == 0) {
 			alias.idFields.addAll(nonLinkFields);
 		}
 	}
 
 	private static Iterable<Field> collectFieldsOfClass(Class<?> type) {
 		return collectFieldsOfClass(type, null);
 	}
 
 	private static List<Field> collectFieldsOfClass(Class<?> clz, Class<?> stopAtSuperClass) {
 		List<Field> result = new ArrayList<Field>();
 		while (clz != null && !clz.equals(stopAtSuperClass)) {
 			result.addAll(0, filterFields(clz));
 			clz = clz.getSuperclass();
 		}
 		return result;
 	}
 
 	private static Collection<Field> filterFields(Class<?> clz) {
 		List<Field> result = new ArrayList<Field>();
 		for (Field f : clz.getDeclaredFields()) {
 			if ((f.getModifiers() & Modifier.STATIC) > 0) {
 				continue;
 			}
 			if (f.getAnnotation(Transient.class) != null) {
 				continue;
 			}
 			result.add(f);
 		}
 		return result;
 	}
 
 	private static <PK> PK insert(Connection conn, DataSource db, Class<?> type, Object o) {
 		// If the class hierarchy contains multiple tables, create separate
 		// inserts
 		List<TableMapping> tables = determineTableMapping(type);
 
 		if (tables.size() == 1) {
 			PK ids;
 			if (conn != null) {
 				ids = DB.insert(conn, determineTableMapping(type).get(0).tableName, extractValues(type, o));
 			} else {
 				ids = DB.insert(db, determineTableMapping(type).get(0).tableName, extractValues(type, o));
 			}
 			if (ids != null) {
 				applyGeneratedId(o, ids);
 			}
 			return ids;
 		} else {
 			TableMapping topType = tables.remove(0);
 			Map<String, Object> values = extractValues(topType.clazz, o);
 			PK ids;
 			if (conn != null) {
 				ids = DB.insert(conn, topType.tableName, values);
 			} else {
 				ids = DB.insert(db, topType.tableName, values);
 			}
 			if (ids != null) {
 				applyGeneratedId(o, ids);
 			}
 			List<Field> idFields = determineIdFields(type);
 			if (idFields.size() != 1) {
 				throw new MappingException("Need single ID field annotated with @Id for inserting joined subclasses");
 			}
 			String idField = idFields.get(0).getName();
 
 			while (tables.size() > 0) {
 				TableMapping supertype = tables.remove(0);
 				Map<String, Object> subvals = extractValues(tables.size() > 0 ? supertype.clazz : type, o, topType.clazz);
 				subvals.put(idField, ids);
 				if (conn != null) {
 					DB.insert(conn, supertype.tableName, subvals);
 				} else {
 					DB.insert(db, supertype.tableName, subvals);
 				}
 				topType = supertype;
 			}
 			return ids;
 		}
 
 	}
 
 	public static List<TableMapping> determineTableMapping(Class<?> clz) {
 		List<TableMapping> tables = new ArrayList<TableMapping>();
 		List<Field> fields = new ArrayList<Field>();
 		while (clz != null) {
 			Table tableAnn = clz.getAnnotation(Table.class);
 			fields.addAll(0, collectFieldsOfClass(clz, clz.getSuperclass()));
 			if (tableAnn != null) {
 				String name = tableAnn.value();
 				tables.add(0, new TableMapping(name, clz, new ArrayList<Field>(fields)));
 				fields.clear();
 			}
 			clz = clz.getSuperclass();
 		}
 		if (fields.size() > 0 && tables.size() > 0) {
 			tables.get(0).fields.addAll(0, fields);
 		}
 		return tables;
 	}
 
 	public static <PK> PK insert(Connection conn, Class<?> type, Object o) {
 		return insert(conn, null, type, o);
 	}
 
 	public static <PK> PK insert(DataSource db, Class<?> type, Object o) {
 		return insert(null, db, type, o);
 	}
 
 	public static <PK> PK insert(DataSource db, Object o) {
 		return insert(db, o.getClass(), o);
 	}
 
 	public static <PK> PK insert(Connection connection, Object o) {
 		return insert(connection, o.getClass(), o);
 	}
 
 	public static int update(DataSource db, Object object) {
 		return update(null, db, object.getClass(), object);
 	}
 
 	public static int update(Connection connection, Object object) {
 		return update(connection, null, object.getClass(), object);
 	}
 
 	public static int update(DataSource db, Class<?> type, Object object) {
 		return update(null, db, type, object);
 	}
 
 	public static int update(Connection connection, Class<?> type, Object object) {
 		return update(connection, null, type, object);
 	}
 
 	private static int update(Connection conn, DataSource db, Class<?> type, Object o) {
 		// If the class hierarchy contains multiple tables, create separate
 		// inserts
 		List<TableMapping> tables = determineTableMapping(type);
 
 		if (tables.size() == 1) {
 			Map<String, Object> values = extractValues(type, o);
 			Map<String, Object> ids = splitIdFields(o, values);
 
 			if (conn != null) {
 				return DB.update(conn, tables.get(0).tableName, values, ids);
 			} else {
 				return DB.update(db, tables.get(0).tableName, values, ids);
 			}
 		} else {
 
 			int affectedRows = 0;
 
 			TableMapping topType = tables.remove(0);
 			Map<String, Object> values = extractValues(topType.clazz, o);
 			Map<String, Object> ids = splitIdFields(o, values);
 
 			if (conn != null) {
 				affectedRows = DB.update(conn, topType.tableName, values, ids);
 			} else {
 				affectedRows = DB.update(db, topType.tableName, values, ids);
 			}
 
 			while (tables.size() > 0) {
 				TableMapping supertype = tables.remove(0);
 				Map<String, Object> subvals = extractValues(tables.size() > 0 ? supertype.clazz : type, o, topType.clazz);
 				if (conn != null) {
 					DB.update(conn, supertype.tableName, subvals, ids);
 				} else {
 					DB.update(db, supertype.tableName, subvals, ids);
 				}
 				topType = supertype;
 			}
 			return affectedRows;
 		}
 
 	}
 
 	private static Map<String, Object> extractValues(Class<?> type, Object o) {
 		return extractValues(type, o, null);
 	}
 
 	private static Map<String, Object> extractValues(Class<?> type, Object o, Class<?> stopAtSuperclass) {
 		try {
 			Map<String, Object> values = new HashMap<String, Object>();
 			for (Field f : collectFieldsOfClass(type, stopAtSuperclass)) {
 				f.setAccessible(true);
 				
 				Other otherAnn = f.getAnnotation(Other.class);
 				if (otherAnn != null) {
 					@SuppressWarnings("unchecked")
 					Map<String,Object> otherMap = (Map<String, Object>) f.get(o);
 					if (otherMap != null) {
 						for(String fieldName : otherMap.keySet()) {
 							values.put(otherAnn.prefix() + fieldName, otherMap.get(fieldName));
 						}
 					}
 					continue;
 				}
 
 				Object val = f.get(o);
 				if (f.getAnnotation(Embedded.class) != null) {
 					if (val != null) {
 						Map<String, Object> embeddedVals = extractValues(f.getType(), val);
 						String prefix = determinePrefix(f);
 						for (String embeddedField : embeddedVals.keySet()) {
 							values.put(prefix + embeddedField, embeddedVals.get(embeddedField));
 						}
 					}
 				} else {
 					values.put(f.getName(), val);
 				}
 			}
 			return values;
 		} catch (IllegalArgumentException e) {
 			throw new MappingException(e);
 		} catch (IllegalAccessException e) {
 			throw new MappingException(e);
 		}
 	}
 
 	private static Map<String, Object> splitIdFields(Object object, Map<String, Object> values) {
 		List<Field> idFields = determineIdFields(object.getClass());
 		if (idFields.size() == 0) {
 			throw new RuntimeException("No @Id annotations found on fields of class " + object.getClass().getName());
 		}
 		Map<String, Object> ids = new HashMap<String, Object>();
 		for (Field f : idFields) {
 			ids.put(f.getName(), values.get(f.getName()));
 			values.remove(f.getName());
 		}
 		return ids;
 	}
 
 	private static final List<Field> determineIdFields(Class<?> clz) {
 		Iterable<Field> fields = collectFieldsOfClass(clz);
 		ArrayList<Field> result = new ArrayList<Field>();
 		for (Field f : fields) {
 			if (f.getAnnotation(Id.class) != null) {
 				result.add(f);
 			}
 		}
 		return result;
 	}
 
 	private static Field determineIdField(Class<?> clz) {
 		List<Field> idFields = determineIdFields(clz);
 		if (idFields.size() != 1) {
 			throw new MappingException("Need single id field annotated with @Id on class " + clz);
 		}
 		return idFields.get(0);
 	}
 
 	private static <PK> void applyGeneratedId(Object o, PK ids) {
 		List<Field> idFields = determineIdFields(o.getClass());
 		if (ids != null && idFields.size() == 1) {
 			Field idField = idFields.get(0);
 			idField.setAccessible(true);
 			try {
 				idField.set(o, ids);
 			} catch (IllegalArgumentException e) {
 				throw new MappingException("Could not set Id field value " + idField, e);
 			} catch (IllegalAccessException e) {
 				throw new MappingException("Could not set Id field value " + idField, e);
 			}
 		}
 	}
 
 	public PojoQuery<T> addWhere(SqlExpression expression) {
 		wheres.add(expression);
 		return this;
 	}
 
 	SqlExpression buildListIdsStatement(List<Field> idFields) {
 		return toStatement(new SqlExpression("SELECT DISTINCT " + implode("\n , ", getFieldNames(idFields))), table, joins, wheres, null, orderBy, -1, -1);
 	}
 
 	SqlExpression buildCountStatement() {
 		List<Field> idFields = determineIdFields(resultClass);
 		String selectClause = "SELECT COUNT(DISTINCT " + implode(", ", getFieldNames(idFields)) + ") ";
 
 		return toStatement(new SqlExpression(selectClause), table, joins, wheres, null, null, -1, -1);
 	}
 
 	private List<String> getFieldNames(List<Field> fields) {
 		List<String> fieldNames = new ArrayList<String>();
 		for (Field f : fields) {
 			fieldNames.add(table + "." + f.getName());
 		}
 		return fieldNames;
 	}
 
 	public int countTotal(DataSource db) {
 		SqlExpression stmt = buildCountStatement();
 		List<Map<String, Object>> rows = DB.queryRows(db, stmt);
 		return ((Long) rows.get(0).values().iterator().next()).intValue();
 	}
 
 	@SuppressWarnings("unchecked")
 	public <PK> List<PK> listIds(DataSource db) {
 		List<Field> idFields = determineIdFields(resultClass);
 		SqlExpression stmt = buildListIdsStatement(idFields);
 		List<Map<String, Object>> rows = DB.queryRows(db, stmt);
 		if (idFields.size() > 1) {
 			return (List<PK>) rows;
 		}
 		List<PK> result = new ArrayList<PK>();
 		for (Map<String, Object> r : rows) {
 			result.add((PK) r.values().iterator().next());
 		}
 		return result;
 	}
 
 	public <PK> List<T> findByIdsList(DataSource db, List<PK> idsList) {
 		SqlExpression statement = buildFindByIdsListStatement(idsList);
 		return processRows(DB.queryRows(db, statement), resultClass);
 	}
 
 	@SuppressWarnings("unchecked")
 	private <PK> SqlExpression buildFindByIdsListStatement(List<PK> idsList) {
 		List<Field> idFields = determineIdFields(resultClass);
 		if (idFields.size() > 1) {
 			throw new MappingException("findByIdsList is not supported for tables with composite id's");
 		}
 		StringBuilder qmarks = new StringBuilder();
 		for (int i = 0; i < idsList.size(); i++) {
 			if (qmarks.length() > 0) {
 				qmarks.append(",");
 			}
 			qmarks.append("?");
 		}
 
 		String idField = table + "." + idFields.get(0).getName();
 		SqlExpression whereCondition = new SqlExpression(idField + " IN (" + qmarks + ")", (Iterable<Object>) idsList);
 
 		SqlExpression statement = toStatement(new SqlExpression("SELECT\n " + implode(",\n ", fields)), table, joins, Arrays.asList(whereCondition), groupBy, orderBy, offset, rowCount);
 		return statement;
 	}
 
 	public static class MappingException extends RuntimeException {
 		public MappingException(String message) {
 			super(message);
 		}
 
 		public MappingException(String message, Throwable cause) {
 			super(message, cause);
 		}
 
 		public MappingException(Throwable cause) {
 			super(cause);
 		}
 	}
 
 	public static class TableMapping {
 		public final String tableName;
 		public final Class<?> clazz; // The class on which the @Table is
 										// declared
 		public final List<Field> fields;
 
 		public TableMapping(String tableName, Class<?> clazz, List<Field> fields) {
 			this.tableName = tableName;
 			this.clazz = clazz;
 			this.fields = fields;
 		}
 	}
 
 }
