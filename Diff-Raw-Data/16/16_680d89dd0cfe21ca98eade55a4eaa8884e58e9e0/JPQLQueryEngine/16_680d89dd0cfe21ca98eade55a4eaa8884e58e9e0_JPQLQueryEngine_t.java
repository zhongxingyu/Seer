 package org.romaframework.aspect.persistence.datanucleus.jdo;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.jdo.spi.PersistenceCapable;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.romaframework.aspect.persistence.PersistenceAspect;
 import org.romaframework.aspect.persistence.PersistenceException;
 import org.romaframework.aspect.persistence.QueryByExample;
 import org.romaframework.aspect.persistence.QueryByFilter;
 import org.romaframework.aspect.persistence.QueryByFilterItem;
 import org.romaframework.aspect.persistence.QueryByFilterItemGroup;
 import org.romaframework.aspect.persistence.QueryByFilterItemPredicate;
 import org.romaframework.aspect.persistence.QueryByFilterItemReverse;
 import org.romaframework.aspect.persistence.QueryByFilterItemText;
 import org.romaframework.aspect.persistence.QueryByFilterOrder;
 import org.romaframework.aspect.persistence.QueryByFilterProjection;
 import org.romaframework.aspect.persistence.QueryByFilterProjection.ProjectionOperator;
 import org.romaframework.aspect.persistence.QueryByText;
 import org.romaframework.aspect.persistence.QueryOperator;
 import org.romaframework.core.Roma;
 import org.romaframework.core.schema.SchemaField;
 
 public class JPQLQueryEngine implements QueryEngine {
 
 	protected static Log	log	= LogFactory.getLog(JPQLQueryEngine.class);
 
 	public Query createQuery(PersistenceManager manager, String query) {
 		if (log.isDebugEnabled())
 			log.debug("Executing query:" + query);
 		return manager.newQuery("javax.jdo.query.JPQL", query);
 	}
 
 	public long countByFilter(PersistenceManager manager, QueryByFilter queryInput) {
 		QueryByFilter query2 = new QueryByFilter(queryInput.getCandidateClass(), queryInput.getPredicateOperator());
 		query2.merge(queryInput);
 
 		query2.getProjections().clear();
 		query2.getOrders().clear();
 		clearItems(query2.getItems());
 		query2.addProjection("*", ProjectionOperator.COUNT);
 		Object o = queryByFilter(manager, query2).get(0);
 		if (o instanceof Number) {
 			return ((Number) o).longValue();
 		}
 		throw new PersistenceException("Error no execute count , result not a number");
 	}
 
 	private void clearItems(List<QueryByFilterItem> items) {
 		for (QueryByFilterItem queryByFilterItem : items) {
 			if (queryByFilterItem instanceof QueryByFilterItemReverse) {
 				((QueryByFilterItemReverse) queryByFilterItem).getQueryByFilter().getProjections().clear();
 				clearItems(((QueryByFilterItemReverse) queryByFilterItem).getQueryByFilter().getItems());
 			}
 		}
 	}
 
 	public long countByExample(PersistenceManager manager, QueryByExample queryInput) {
 		return countByFilter(manager, buildQueryByFilter(queryInput));
 	}
 
 	public long countByText(PersistenceManager manager, QueryByText queryInput) {
 		return 0;
 	}
 
 	public List<?> queryByExample(PersistenceManager manager, QueryByExample queryInput) {
 		return queryByFilter(manager, buildQueryByFilter(queryInput));
 	}
 
 	public List<?> queryByFilter(PersistenceManager manager, QueryByFilter queryInput) {
 		StringBuilder stringQuery = new StringBuilder();
 		Map<String, Object> params = new HashMap<String, Object>();
 		List<String> projectionList = new ArrayList<String>();
 		buildQuery(queryInput, stringQuery, params, projectionList);
 		Query query = createQuery(manager, stringQuery.toString());
 		return executeQuery(manager, query, queryInput, params, projectionList);
 	}
 
 	public List<?> queryByText(PersistenceManager manager, QueryByText queryInput) {
 		Query query = createQuery(manager, queryInput.getText());
 		return executeQuery(manager, query, queryInput, null, null);
 	}
 
 	private Object strategySingle(PersistenceManager manager, Object toChange, byte strategy) {
 		if (toChange instanceof PersistenceCapable) {
 			switch (strategy) {
 			case PersistenceAspect.STRATEGY_DETACHING:
 				toChange = manager.detachCopy(toChange);
 				break;
 			case PersistenceAspect.STRATEGY_TRANSIENT:
 				manager.makeTransient(toChange, false);
 				break;
 			}
 		}
 		return toChange;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public List<?> executeQuery(PersistenceManager manager, Query query, org.romaframework.aspect.persistence.Query queryInput, Map<String, Object> params,
 			List<String> projectionList) {
 		if (queryInput.getRangeFrom() > -1 && queryInput.getRangeTo() > -1)
 			query.setRange(queryInput.getRangeFrom(), queryInput.getRangeTo());
 		Object result;
 		if (params == null || params.isEmpty())
 			result = query.execute();
 		else
 			result = query.executeWithMap(params);
 
 		if (result instanceof Collection<?>) {
 			if (queryInput.hasProjection()) {
 				List newResult = new ArrayList(((Collection) result).size());
 				for (Object curRes : (Iterable) result) {
 					if (curRes instanceof Object[]) {
 						Map<String, Object> newRow = new HashMap<String, Object>();
 						Object[] curArray = ((Object[]) curRes);
 						for (int i = 0; i < curArray.length; i++) {
 							int app = 0;
 							String projectionName = projectionList.get(i);
 							while (newRow.get(projectionName) != null) {
 								projectionName = projectionList.get(i) + app++;
 							}
 							newRow.put(projectionName, strategySingle(manager, curArray[i], queryInput.getStrategy()));
 						}
 						newResult.add(newRow);
 					} else {
 						newResult.add(strategySingle(manager, curRes, queryInput.getStrategy()));
 					}
 				}
 				query.close(result);
 				result = newResult;
 			} else {
 				byte strategy = queryInput.getStrategy();
 				switch (strategy) {
 				case PersistenceAspect.STRATEGY_DETACHING:
 					result = manager.detachCopyAll((Collection<?>) result);
 					query.close(result);
 					break;
 				case PersistenceAspect.STRATEGY_TRANSIENT:
 					manager.makeTransientAll(result, queryInput.getMode() != null);
 					query.close(result);
 					break;
 				}
 			}
 		} else {
 			result = strategySingle(manager, result, queryInput.getStrategy());
 		}
 
 		if (!(result instanceof List)) {
 			if (result instanceof Set<?>) {
 				result = new ArrayList((Set<?>) result);
 			} else
 				result = Arrays.asList(result);
 		}
 
 		return (List<?>) result;
 	}
 
 	public void buildQuery(QueryByFilter filter, StringBuilder query, Map<String, Object> params, List<String> projectionList) {
 		StringBuilder where = new StringBuilder();
 		Map<String, Class<?>> froms = new HashMap<String, Class<?>>();
 		Map<String, List<QueryByFilterProjection>> projections = new LinkedHashMap<String, List<QueryByFilterProjection>>();
 		Map<String, List<QueryByFilterOrder>> orders = new LinkedHashMap<String, List<QueryByFilterOrder>>();
 		projections.put("A", filter.getProjections());
 		if (!filter.getOrders().isEmpty())
 			orders.put("A", filter.getOrders());
 		if (!filter.getItems().isEmpty()) {
 			where.append(" where ");
 			buildWhere(where, filter.getItems(), params, filter.getPredicateOperator(), "A", froms, projections, orders, 'A');
 		}
 		query.append("select ");
 		if (filter.isDistinct()) {
 			query.append("distinct ");
 		}
 		StringBuilder groupBy = new StringBuilder();
 		if (!filter.getProjections().isEmpty()) {
 			buildProjection(query, groupBy, projections, projectionList);
 		} else {
 			query.append('A');
 		}
 		query.append(" from ").append(filter.getCandidateClass().getName()).append(" A");
 		for (Map.Entry<String, Class<?>> from : froms.entrySet()) {
 			query.append(',').append(from.getValue().getName()).append(' ').append(from.getKey());
 		}
 		query.append(where);
 		query.append(groupBy);
 		buildOrder(query, orders);
 	}
 
 	public void buildOrder(StringBuilder order, Map<String, List<QueryByFilterOrder>> orders) {
 		if (orders.isEmpty())
 			return;
 		order.append(" order by ");
 		Iterator<Map.Entry<String, List<QueryByFilterOrder>>> entries = orders.entrySet().iterator();
 		while (entries.hasNext()) {
 			Map.Entry<String, List<QueryByFilterOrder>> entry = entries.next();
 			if (entry.getValue() != null && !entry.getValue().isEmpty()) {
 				Iterator<QueryByFilterOrder> ordersI = entry.getValue().iterator();
 				while (ordersI.hasNext()) {
 					QueryByFilterOrder ord = ordersI.next();
 					order.append(entry.getKey()).append('.').append(ord.getFieldName()).append(" ").append(ord.getFieldOrder());
 					if (ordersI.hasNext())
 						order.append(',');
 				}
 				if (entries.hasNext())
 					order.append(',');
 			}
 		}
 	}
 
 	public void buildProjection(StringBuilder pro, StringBuilder groupBy, Map<String, List<QueryByFilterProjection>> projections, List<String> projectionList) {
 		Iterator<Map.Entry<String, List<QueryByFilterProjection>>> entries = projections.entrySet().iterator();
 		boolean hasFunction = false, hasSimple = false;
 		while (entries.hasNext()) {
 			Map.Entry<String, List<QueryByFilterProjection>> entry = entries.next();
 			if (entry.getValue() != null && !entry.getValue().isEmpty()) {
 				Iterator<QueryByFilterProjection> projectionI = entry.getValue().iterator();
 				while (projectionI.hasNext()) {
 					QueryByFilterProjection projection = projectionI.next();
 					if (projection instanceof ReverseQueryByFilterProject) {
 						((ReverseQueryByFilterProject) projection).getReverse();
 					} else {
 						if (!ProjectionOperator.PLAIN.equals(projection.getOperator()))
 							hasFunction = true;
 						else
 							hasSimple = true;
 						resolveProjection(pro, projection.getField(), projection.getOperator(), entry.getKey());
 						projectionList.add(projection.getField());
 						if (projectionI.hasNext())
 							pro.append(',');
 					}
 				}
 				if (entries.hasNext())
 					pro.append(',');
 			}
 		}
 		entries = projections.entrySet().iterator();
 		while (entries.hasNext()) {
 			Map.Entry<String, List<QueryByFilterProjection>> entry = entries.next();
 			if (entry.getValue() != null) {
 				Iterator<QueryByFilterProjection> projectionI = entry.getValue().iterator();
 
 				projectionI = entry.getValue().iterator();
 				if (hasSimple && hasFunction) {
 					groupBy.append(" group by ");
 					boolean needSep = false;
 					while (projectionI.hasNext()) {
 						QueryByFilterProjection projection = projectionI.next();
 						if (ProjectionOperator.PLAIN.equals(projection.getOperator())) {
 							if (needSep)
 								groupBy.append(',');
 							else
 								needSep = true;
 							projectionList.add(projection.getField());
 							resolveProjection(groupBy, projection.getField(), projection.getOperator(), entry.getKey());
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void resolveProjection(StringBuilder build, String field, ProjectionOperator oper, String alias) {
 		switch (oper) {
 		case AVG:
 			build.append("AVG(").append(alias).append('.').append(field).append(')');
 			break;
 		case COUNT:
 			if ("*".equals(field))
 				build.append("COUNT(").append(alias).append(')');
 			else
 				build.append("COUNT(").append(alias).append('.').append(field).append(')');
 			break;
 		case MAX:
 			build.append("MAX(").append(alias).append('.').append(field).append(')');
 			break;
 		case MIN:
 			build.append("MIN(").append(alias).append('.').append(field).append(')');
 			break;
 		case SUM:
 			build.append("SUM(").append(alias).append('.').append(field).append(')');
 			break;
 		case PLAIN:
 			if ("this".equals(field)) {
 				build.append(alias);
 			} else {
 				build.append(alias).append('.').append(field);
 			}
 			break;
 		}
 	}
 
 	public void buildWhere(StringBuilder where, List<QueryByFilterItem> items, Map<String, Object> params, String predicate, String alias, Map<String, Class<?>> froms,
 			Map<String, List<QueryByFilterProjection>> projections, Map<String, List<QueryByFilterOrder>> orders, char aliasAppend) {
 		if (items == null)
 			return;
 		Iterator<QueryByFilterItem> iter = items.iterator();
 		while (iter.hasNext()) {
 			QueryByFilterItem item = iter.next();
 			if (item instanceof QueryByFilterItemGroup) {
 				if (((QueryByFilterItemGroup) item).getItems() == null || ((QueryByFilterItemGroup) item).getItems().isEmpty())
 					continue;
 				where.append("(");
 				buildWhere(where, ((QueryByFilterItemGroup) item).getItems(), params, ((QueryByFilterItemGroup) item).getPredicate(), alias, froms, projections, orders, aliasAppend);
 				where.append(")");
 			} else if (item instanceof QueryByFilterItemPredicate) {
 				QueryByFilterItemPredicate pred = ((QueryByFilterItemPredicate) item);
 				String fieldName = pred.getFieldName();
 				String pName = fieldName.replace('.', '_');
 				int i = 1;
 				while (params.get(pName) != null)
 					pName = fieldName.replace('.', '_') + (i++);
 				if (QueryOperator.LIKE.equals(pred.getFieldOperator()) && ((pred.getFieldValue() instanceof String) || pred.getFieldValue() == null)) {
 					where.append("UPPER(").append(alias).append(".").append(fieldName).append(")");
 				} else if (pred.getFieldOperator().equals(QueryOperator.CONTAINS)) {
 					where.append(":").append(pName);
 				} else {
 					where.append(alias).append(".").append(fieldName);
 				}
 
 				if (pred.getFieldValue() == null) {
 					if (QueryOperator.EQUALS.equals(pred.getFieldOperator())) {
 						where.append(" IS NULL ");
 					} else if (QueryOperator.NOT_EQUALS.equals(pred.getFieldOperator())) {
 						where.append(" IS NOT NULL ");
 					} else {
 						throw new PersistenceException("field:" + pred.getFieldName() + " with Operator: " + pred.getFieldOperator() + " cannot be null.");
 					}
 				} else {
 					where.append(getJPQLOperator(pred.getFieldOperator()));
 					if (pred.getFieldOperator().equals(QueryOperator.IN) || pred.getFieldOperator().equals(QueryOperator.CONTAINS))
 						where.append("(");
 					if (QueryOperator.LIKE.equals(pred.getFieldOperator()) && ((pred.getFieldValue() instanceof String) || pred.getFieldValue() == null)) {
						String value = (String) quote(pred.getFieldValue());
 						if (value == null)
 							params.put(pName, "%");
 						else {
 							if (value.indexOf("*") != -1)
 								params.put(pName, value.toUpperCase().replaceAll("[*]", "%"));
 							else
 								params.put(pName, "%" + value.toUpperCase() + "%");
 						}
 					} else {
						params.put(pName, quote(pred.getFieldValue()));
 					}
 					if (pred.getFieldOperator().equals(QueryOperator.CONTAINS))
 						where.append(alias).append(".").append(fieldName);
 					else
 						where.append(':').append(pName);
 					if (pred.getFieldOperator().equals(QueryOperator.IN) || pred.getFieldOperator().equals(QueryOperator.CONTAINS))
 						where.append(")");
 				}
 			} else if (item instanceof QueryByFilterItemText) {
 				where.append(alias).append(".").append(((QueryByFilterItemText) item).getCondition());
 			} else if (item instanceof QueryByFilterItemReverse) {
 				String field = ((QueryByFilterItemReverse) item).getField();
 				String newAlias = alias + (aliasAppend++);
 				QueryByFilter qbf = ((QueryByFilterItemReverse) item).getQueryByFilter();
 				where.append(newAlias).append(".").append(field);
 				where.append(getJPQLOperator(((QueryByFilterItemReverse) item).getOperator()));
 				String fieldReverse = ((QueryByFilterItemReverse) item).getFieldReverse();
 
 				if (fieldReverse == null || "this".equals(fieldReverse) || "".equals(fieldReverse))
 					where.append(alias);
 				else
 					where.append(alias).append('.').append(fieldReverse);
 				froms.put(newAlias, qbf.getCandidateClass());
 				if (!qbf.getProjections().isEmpty())
 					projections.put(newAlias, qbf.getProjections());
 				if (!qbf.getOrders().isEmpty())
 					orders.put(newAlias, qbf.getOrders());
 				if (qbf.getItems().size() > 0) {
 					where.append(" ").append(predicate).append(" ");
 				}
 				buildWhere(where, qbf.getItems(), params, qbf.getPredicateOperator(), newAlias, froms, projections, orders, 'A');
 			}
 			if (iter.hasNext())
 				where.append(" ").append(predicate).append(" ");
 		}
 	}
 
	private Object quote(Object fieldValue) {
		if (fieldValue != null && fieldValue instanceof String) {
			return ((String) fieldValue).replaceAll("\\\\", "\\\\\\\\");
		}
		return fieldValue;
	}

 	private String getJPQLOperator(QueryOperator operator) {
 		switch (operator) {
 		case CONTAINS:
 			return " MEMBER OF ";
 		case IN:
 			return " IN ";
 		case LIKE:
 			return " LIKE ";
 		case EQUALS:
 			return " = ";
 		case MAJOR_EQUALS:
 			return " >= ";
 		case MAJOR:
 			return " > ";
 		case MINOR:
 			return " < ";
 		case MINOR_EQUALS:
 			return " <= ";
 		case NOT_EQUALS:
 			return " <> ";
 		case NOT_IN:
 			return " NOT IN ";
 		}
 		return "";
 	}
 
 	protected QueryByFilter buildQueryByFilter(QueryByExample iQuery) {
 
 		QueryByFilter filter = new QueryByFilter(iQuery.getCandidateClass());
 		filter.setRangeFrom(iQuery.getRangeFrom(), iQuery.getRangeTo());
 		filter.setSubClasses(iQuery.isSubClasses());
 		filter.setMode(iQuery.getMode());
 		filter.setStrategy(iQuery.getStrategy());
 
 		if (iQuery.getFilter() != null) {
 			Iterator<SchemaField> sf = Roma.schema().getSchemaClass(iQuery.getCandidateClass()).getFieldIterator();
 			Object fieldValue;
 			QueryOperator operator = null;
 			while (sf.hasNext()) {
 				SchemaField field = sf.next();
 				try {
 					boolean found = false;
					Field[] fields = iQuery.getCandidateClass().getDeclaredFields();
 					for (Field classField : fields) {
 						if (classField.getName().equals(field.getName())) {
 							found = true;
 							break;
 						}
 					}
 					if (!found)
 						continue;
 				} catch (SecurityException e) {
 				}
 				fieldValue = field.getValue(iQuery.getFilter());
 				if (fieldValue != null) {
 					if (fieldValue instanceof Collection<?> || fieldValue instanceof Map<?, ?>)
 						continue;
 					if (fieldValue instanceof String && ((String) fieldValue).length() == 0)
 						continue;
 
 					if (String.class.equals(field.getLanguageType()))
 						operator = QueryByFilter.FIELD_LIKE;
 					else
 						operator = QueryByFilter.FIELD_EQUALS;
 
 					filter.addItem(field.getName(), operator, fieldValue);
 				}
 			}
 		}
 
 		QueryByFilter addFilter = iQuery.getAdditionalFilter();
 		if (addFilter != null) {
 			filter.setSubClasses(addFilter.isSubClasses());
 			filter.merge(addFilter);
 		}
 		return filter;
 	}
 }
