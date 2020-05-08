 package org.alveolo.simpa.jdbc;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.EmbeddedId;
 import javax.persistence.EntityNotFoundException;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.NonUniqueResultException;
 import javax.persistence.PersistenceException;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.metamodel.Attribute;
 import javax.persistence.metamodel.Attribute.PersistentAttributeType;
 import javax.persistence.metamodel.EntityType;
 import javax.persistence.metamodel.ManagedType;
 import javax.sql.DataSource;
 
 import org.alveolo.simpa.EntityStore;
 import org.alveolo.simpa.metamodel.AttributeImpl;
 import org.alveolo.simpa.metamodel.MetamodelImpl;
 import org.alveolo.simpa.metamodel.SingularAttributeImpl;
 import org.alveolo.simpa.query.AttrSelect;
 import org.alveolo.simpa.query.Conjunction;
 import org.alveolo.simpa.query.Group;
 import org.alveolo.simpa.query.Order;
 import org.alveolo.simpa.query.Query;
 import org.alveolo.simpa.query.Raw;
 import org.alveolo.simpa.query.Select;
 import org.alveolo.simpa.query.SqlBuilder;
 import org.alveolo.simpa.query.WhereBuilder;
 import org.alveolo.simpa.util.EntityUtil;
 
 
 public class JdbcStore implements EntityStore, RawCallbacks, QueryCallbacks {
 	protected final DefaultNaming naming = new DefaultNaming();
 
 	protected final DataSource ds;
 	protected final MetamodelImpl metamodel;
 
 	public JdbcStore(DataSource ds, List<Class<?>> classes) {
 		this.ds = ds;
 		this.metamodel = new MetamodelImpl(classes);
 	}
 
 	@Override
 	public MetamodelImpl getMetamodel() {
 		return metamodel;
 	}
 
 	public long nextval(SequenceGenerator annotation) {
 		try {
 			Connection con = acquireConnection();
 			try {
 				String name = naming.getQualifiedSequenceName(annotation);
 
 				PreparedStatement stmt = con.prepareStatement("SELECT nextval('" + name + "')");
 				try {
 					ResultSet rset = stmt.executeQuery();
 					try {
 						if (!rset.next()) {
 							throw new PersistenceException("Error fetching sequence value: " + name);
 						}
 
 						Object id = rset.getObject(1);
 
 						if (rset.next()) {
 							throw new PersistenceException("Only one sequence value is expected: " + name);
 						}
 
 						return (Long) id;
 					} finally {
 						rset.close();
 					}
 				} finally {
 					stmt.close();
 				}
 			} finally {
 				releaseConnection(con);
 			}
 		} catch (SQLException e) {
 			throw new PersistenceException(e);
 		}
 	}
 
 	@Override
 	public void insert(Object entity) {
 		EntityType<?> type = metamodel.entity(entity.getClass());
 
 		List<AttributeValue> values = getInsertableValues(type, entity);
 
 		for (AttributeValue av : values) {
 			if (av.attribute.getPersistentAttributeType() != PersistentAttributeType.BASIC) {
 				continue;
 			}
 
 			SingularAttributeImpl<?, ?> attribute = (SingularAttributeImpl<?, ?>) av.attribute;
 			if (!attribute.isId()) {
 				continue;
 			}
 		}
 
 		StringBuilder sql = new StringBuilder();
 
 		sql.append("INSERT INTO ").append(naming.getQualifiedTableName(type.getJavaType())).append(" (");
 
 		for (Iterator<AttributeValue> i = values.iterator(); i.hasNext();) {
 			sql.append(naming.getColumnName(i.next().attribute.getJavaMember()));
 			if (i.hasNext()) {
 				sql.append(',');
 			}
 		}
 
 		sql.append(") VALUES (");
 
 		for (Iterator<AttributeValue> i = values.iterator(); i.hasNext();) {
 			i.next();
 			sql.append("?");
 			if (i.hasNext()) {
 				sql.append(',');
 			}
 		}
 
 		sql.append(")");
 
 		try {
 			Connection con = acquireConnection();
 			try {
 				PreparedStatement stmt = con.prepareStatement(sql.toString());
 				try {
 					int index = 0;
 
 					for (AttributeValue cv : values) {
 						JdbcUtil.setParameter(stmt, ++index, cv.attribute, cv.value);
 					}
 
 					int updated = stmt.executeUpdate();
 					if (updated == 0) {
 						throw new EntityNotFoundException(type.getName() + " with the given identity is not found");
 					}
 				} finally {
 					stmt.close();
 				}
 			} finally {
 				releaseConnection(con);
 			}
 		} catch (SQLException e) {
 			throw new PersistenceException(e);
 		}
 	}
 
 	@Override
 	public void update(Object entity) {
 		EntityType<?> type = metamodel.entity(entity.getClass());
 
 		List<AttributeValue> values = getUpdatableValues(type, entity);
 
 		StringBuilder sql = new StringBuilder();
 
 		sql.append("UPDATE ").append(naming.getQualifiedTableName(type.getJavaType())).append(" SET ");
 
 		for (Iterator<AttributeValue> i = values.iterator(); i.hasNext();) {
 			sql.append(naming.getColumnName(i.next().attribute.getJavaMember())).append("=?");
 			if (i.hasNext()) {
 				sql.append(',');
 			}
 		}
 
 		Conjunction conjunction = EntityUtil.conditionsForEntity(type, entity);
 
 		appendWhereSection(new JdbcSqlAppendVisitor(sql), conjunction);
 
 		try {
 			Connection con = acquireConnection();
 			try {
 				PreparedStatement stmt = con.prepareStatement(sql.toString());
 				try {
 					JdbcSetParameterVisitor visitor = new JdbcSetParameterVisitor(stmt);
 
 					for (AttributeValue av : values) {
 						visitor.visit(av);
 					}
 
 					conjunction.accept(visitor);
 
 					int updated = stmt.executeUpdate();
 					if (updated == 0) {
 						throw new EntityNotFoundException(type.getName() + " with the given identity is not found");
 					}
 				} finally {
 					stmt.close();
 				}
 			} finally {
 				releaseConnection(con);
 			}
 		} catch (SQLException e) {
 			throw new PersistenceException(e);
 		}
 	}
 
 	@Override
 	public void delete(Object entity) {
 		EntityType<?> type = metamodel.entity(entity.getClass());
 		delete(type, EntityUtil.conditionsForEntity(type, entity));
 	}
 
 	@Override
 	public <T> void delete(Class<T> javaType, Object id) {
 		EntityType<T> type = metamodel.entity(javaType);
 		delete(type, EntityUtil.conditionsForId(type, id));
 	}
 
 	private <T> void delete(EntityType<T> type, Conjunction conjunction) {
 		StringBuilder sql = new StringBuilder();
 
 		sql.append("DELETE");
 
 		JdbcSqlAppendVisitor visitor = new JdbcSqlAppendVisitor(sql);
 
 		appendFromSection(visitor, type);
 		appendWhereSection(visitor, conjunction);
 
 		try {
 			Connection con = acquireConnection();
 			try {
 				PreparedStatement stmt = con.prepareStatement(sql.toString());
 				try {
 					conjunction.accept(new JdbcSetParameterVisitor(stmt));
 
 					int updated = stmt.executeUpdate();
 					if (updated == 0) {
 						throw new EntityNotFoundException(type.getName() + " with the given identity is not found");
 					}
 				} finally {
 					stmt.close();
 				}
 			} finally {
 				releaseConnection(con);
 			}
 		} catch (SQLException e) {
 			throw new PersistenceException(e);
 		}
 	}
 
 	@Override
 	public <T> T find(Class<T> javaType, Object id) {
 		EntityType<T> type = metamodel.entity(javaType);
 
 		return from(javaType).eq(type.getId(type.getIdType().getJavaType()), id).find();
 	}
 
 	@Override
 	public <T> SqlBuilder sql(String sql, Object... values) {
 		SqlBuilder builder = new SqlBuilder(this);
 		builder.sql(sql, values);
 		return builder;
 	}
 
 	@Override
 	public <T> WhereBuilder<T> from(Class<T> type) {
 		return new WhereBuilder<>(this, type);
 	}
 
 	@Override
 	public <T> T find(Class<T> javaType, List<Raw> fragments) {
 		List<T> list = list(javaType, fragments);
 
 		int size = list.size();
 
 		if (size == 0) {
 			return null;
 		}
 
 		if (size > 1) {
 			throw new NonUniqueResultException("Expecting single result but found: " + size);
 		}
 
 		return list.get(0);
 	}
 
 	@Override
 	public <T> List<T> list(Class<T> javaType, List<Raw> fragments) {
 		StringBuilder sql = new StringBuilder();
 
 		for (Iterator<Raw> i = fragments.iterator(); i.hasNext();) {
 			sql.append(i.next().sql);
 			if (i.hasNext()) {
 				sql.append(' ');
 			}
 		}
 
 		List<T> list = new ArrayList<>();
 
 		try {
 			Connection con = acquireConnection();
 			try {
 				PreparedStatement stmt = con.prepareStatement(sql.toString());
 				try {
 					JdbcSetParameterVisitor visitor = new JdbcSetParameterVisitor(stmt);
 
 					for (Raw r : fragments) {
 						visitor.visit(r);
 					}
 
 					ResultSet rset = stmt.executeQuery();
 					try {
 						RawMapper<T> mapper = new RawMapper<>(rset.getMetaData(), javaType);
 
 						while (rset.next()) {
 							T entity = newInstance(javaType);
 							mapper.setEntityValues(rset, entity);
 							list.add(entity);
 						}
 					} finally {
 						rset.close();
 					}
 				} finally {
 					stmt.close();
 				}
 			} finally {
 				releaseConnection(con);
 			}
 		} catch (ReflectiveOperationException | SQLException e) {
 			throw new PersistenceException(e);
 		}
 
 		return list;
 	}
 
 	@Override
 	public <T> int delete(Query<T> query) {
 		EntityType<T> type = metamodel.entity(query.type);
 
 		StringBuilder sql = new StringBuilder();
 
 		sql.append("DELETE");
 
 		JdbcSqlAppendVisitor visitor = new JdbcSqlAppendVisitor(sql);
 
 		appendFromSection(visitor, type);
 		appendWhereSection(visitor, query.where);
 
 		try {
 			Connection con = acquireConnection();
 			try {
 				PreparedStatement stmt = con.prepareStatement(sql.toString());
 				try {
 					query.where.accept(new JdbcSetParameterVisitor(stmt));
 
 					return stmt.executeUpdate();
 				} finally {
 					stmt.close();
 				}
 			} finally {
 				releaseConnection(con);
 			}
 		} catch (SQLException e) {
 			throw new PersistenceException(e);
 		}
 	}
 
 	@Override
 	public <T> T find(Query<T> query) {
 		List<T> list = list(query);
 
 		int size = list.size();
 
 		if (size == 0) {
 			return null;
 		}
 
 		if (size > 1) {
 			throw new NonUniqueResultException("Expecting single result but found: " + size);
 		}
 
 		return list.get(0);
 	}
 
 	@Override
 	public <T> List<T> list(Query<T> query) {
 		EntityType<T> type = metamodel.entity(query.type);
 
 		StringBuilder sql = new StringBuilder();
 
 		List<Select> select = query.select;
 		if (select == null) {
 			select = new ArrayList<>();
 			for (Attribute<? super T, ?> a : type.getAttributes()) {
 				select.add(new AttrSelect(a));
 			}
 		}
 
 		{
 			JdbcSqlAppendVisitor visitor = new JdbcSqlAppendVisitor(sql);
 
 			appendSelectSection(visitor, select);
 			appendFromSection(visitor, type);
 			appendWhereSection(visitor, query.where);
 			appendGroupBySection(visitor, query.groups);
 			appendHavingSection(visitor, query.having);
 			appendOrderBySection(visitor, query.order);
 		}
 
 		if (query.offset != null) {
 			sql.append(" OFFSET " + query.offset);
 		}
 
		if (query.fetch != null) {
 			sql.append(" LIMIT " + query.fetch);
 		}
 
 		List<T> list = new ArrayList<>();
 
 		try {
 			Connection con = acquireConnection();
 			try {
 				PreparedStatement stmt = con.prepareStatement(sql.toString());
 				try {
 					JdbcSetParameterVisitor visitor = new JdbcSetParameterVisitor(stmt);
 
 					query.where.accept(visitor);
 					for (Group g : query.groups) g.accept(visitor);
 					query.having.accept(visitor);
 					for (Order o : query.order) o.accept(visitor);
 
 					ResultSet rset = stmt.executeQuery();
 					try {
 						JdbcRowMapperVisitor<T> results = (query.select != null && select.size() == 1)
 								? new JdbcSingleValueMapperVisitor<>(rset, type.getJavaType())
 								: new JdbcRowMapperVisitor<>(rset, type.getJavaType());
 
 						while (rset.next()) {
 							for (Select s : select) s.accept(results);
 
 //							if (query.select != null && select.size() == 1) {
 //								Select s = select.iterator().next();
 //								AttrSelect as = (AttrSelect) s;
 //								@SuppressWarnings("unchecked") // A hack, Path<X> should fix type problems
 //								T value = (T) EntityUtil.getValue(as.attribute, results.getEntity());
 //								list.add(value);
 //							} else {
 								list.add(results.getEntity());
 //							}
 
 							results.reset();
 						}
 					} finally {
 						rset.close();
 					}
 				} finally {
 					stmt.close();
 				}
 			} finally {
 				releaseConnection(con);
 			}
 		} catch (SQLException e) {
 			throw new PersistenceException(e);
 		}
 
 		return list;
 	}
 
 	protected Connection acquireConnection() throws SQLException {
 		// TODO: Better transactions: JTA, Spring
 
 		return ds.getConnection();
 	}
 
 	protected void releaseConnection(Connection con) throws SQLException {
 		// TODO: Better transactions: JTA, Spring
 
 		con.close();
 	}
 
 	// TODO: for collecting insertable/updatable valued use single function with custom filter as parameter
 
 	private List<AttributeValue> getInsertableValues(EntityType<?> type, Object entity) {
 		return addInsertableValues(new ArrayList<AttributeValue>(), type, entity);
 	}
 
 	private List<AttributeValue> addInsertableValues(List<AttributeValue> values, ManagedType<?> type, Object object) {
 		for (Attribute<?, ?> attribute : type.getAttributes()) {
 			if (isInsertable(attribute)) {
 				Object value = EntityUtil.getValue(attribute, object);
 
 				if (attribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {
 					SingularAttributeImpl<?, ?> singular = (SingularAttributeImpl<?, ?>) attribute;
 					ManagedType<?> t = (ManagedType<?>) singular.getType();
 					addInsertableValues(values, t, value);
 					continue;
 				}
 
 				GeneratedValue generated = ((AttributeImpl<?, ?>) attribute).getAnnotation(GeneratedValue.class);
 				if (generated != null) {
 					IdGenerator generator = metamodel.getGenerator(generated.generator());
 					Field field = (Field) attribute.getJavaMember(); // TODO: method
 					value = generator.next(this);
 					JdbcUtil.setPersistenceValue(field, object, value);
 				}
 
 				values.add(new AttributeValue(attribute, value));
 			}
 		}
 
 		return values;
 	}
 
 	private List<AttributeValue> getUpdatableValues(EntityType<?> type, Object entity) {
 		return addUpdatableValues(new ArrayList<AttributeValue>(), type, entity);
 	}
 
 	private List<AttributeValue> addUpdatableValues(List<AttributeValue> values, ManagedType<?> type, Object object) {
 		for (Attribute<?, ?> attribute : type.getAttributes()) {
 			if (isUpdatable(attribute)) {
 				Object value = EntityUtil.getValue(attribute, object);
 
 				if (attribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {
 					SingularAttributeImpl<?, ?> singular = (SingularAttributeImpl<?, ?>) attribute;
 					ManagedType<?> t = (ManagedType<?>) singular.getType();
 					addUpdatableValues(values, t, value);
 					continue;
 				}
 
 				values.add(new AttributeValue(attribute, value));
 			}
 		}
 
 		return values;
 	}
 
 	private void appendSelectSection(JdbcSqlAppendVisitor visitor, List<Select> select) {
 		visitor.append("SELECT ");
 
 		for (Iterator<Select> i = select.iterator(); i.hasNext();) {
 			i.next().accept(visitor);
 			if (i.hasNext()) {
 				visitor.append(",");
 			}
 		}
 	}
 
 	private void appendFromSection(JdbcSqlAppendVisitor visitor, EntityType<?> type) {
 		visitor.append(" FROM ").append(naming.getQualifiedTableName(type.getJavaType()));
 	}
 
 	private void appendWhereSection(JdbcSqlAppendVisitor visitor, Conjunction conjunction) {
 		if (conjunction.conditions.size() > 0) {
 			visitor.append(" WHERE ");
 			conjunction.accept(visitor);
 		}
 	}
 
 	private void appendGroupBySection(JdbcSqlAppendVisitor visitor, List<Group> groups) {
 		if (groups.size() > 0) {
 			visitor.append(" GROUP BY ");
 
 			for (Iterator<Group> i = groups.iterator(); i.hasNext();) {
 				i.next().accept(visitor);
 				if (i.hasNext()) {
 					visitor.append(",");
 				}
 			}
 		}
 	}
 
 	private void appendHavingSection(JdbcSqlAppendVisitor visitor, Conjunction conjunction) {
 		if (conjunction.conditions.size() > 0) {
 			visitor.append(" HAVING ");
 			conjunction.accept(visitor);
 		}
 	}
 
 	private void appendOrderBySection(JdbcSqlAppendVisitor visitor, List<Order> order) {
 		if (order.size() > 0) {
 			visitor.append(" ORDER BY ");
 
 			for (Iterator<Order> i = order.iterator(); i.hasNext();) {
 				i.next().accept(visitor);
 
 				if (i.hasNext()) {
 					visitor.append(",");
 				}
 			}
 		}
 	}
 
 	private boolean isInsertable(Attribute<?, ?> attribute) {
 		// TODO: Support for plural etc.
 		SingularAttributeImpl<?, ?> singular = (SingularAttributeImpl<?, ?>) attribute;
 
 		Column column = singular.getAnnotation(Column.class);
 		if (column != null) {
 			return column.insertable();
 		}
 
 		return true;
 	}
 
 	private boolean isUpdatable(Attribute<?, ?> attribute) {
 		// TODO: Support for plural etc.
 		SingularAttributeImpl<?, ?> singular = (SingularAttributeImpl<?, ?>) attribute;
 
 		if (singular.getAnnotation(Id.class) != null || singular.getAnnotation(EmbeddedId.class) != null) {
 			return false;
 		}
 
 		Column column = singular.getAnnotation(Column.class);
 		if (column != null) {
 			return column.updatable();
 		}
 
 		return true;
 	}
 
 	private static <T> T newInstance(Class<T> type)
 	throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
 		Constructor<T> constructor = type.getDeclaredConstructor();
 		constructor.setAccessible(true);
 		T entity = constructor.newInstance();
 		return entity;
 	}
 }
