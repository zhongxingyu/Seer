 package org.greatage.domain.objectify;
 
 import com.google.appengine.api.datastore.Transaction;
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.Query;
 import org.greatage.domain.AbstractEntityRepository;
 import org.greatage.domain.Criteria;
 import org.greatage.domain.Entity;
 import org.greatage.domain.Pagination;
 import org.greatage.domain.SessionCallback;
 import org.greatage.domain.TransactionExecutor;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Ivan Khalopik
  * @since 1.0
  */
 public class ObjectifyRepository extends AbstractEntityRepository {
 	private final TransactionExecutor<Transaction, Objectify> executor;
 
 	public ObjectifyRepository(final TransactionExecutor<Transaction, Objectify> executor,
 							   final Map<Class, Class> entityMapping) {
 		super(entityMapping);
 		this.executor = executor;
 	}
 
 	public <PK extends Serializable, E extends Entity<PK>>
 	long count(final Class<E> entityClass, final Criteria<PK, E> criteria) {
 		return execute(entityClass, criteria, Pagination.ALL, new QueryCallback<Number, E>() {
 			public Number doInQuery(final Query<? extends E> query) {
 				return query.count();
 			}
 		}).longValue();
 	}
 
 	public <PK extends Serializable, E extends Entity<PK>>
 	List<E> find(final Class<E> entityClass, final Criteria<PK, E> criteria, final Pagination pagination) {
 		return execute(entityClass, criteria, pagination, new QueryCallback<List<E>, E>() {
 			public List<E> doInQuery(final Query<? extends E> query) {
 				return (List) query.list();
 			}
 		});
 	}
 
 	public <PK extends Serializable, E extends Entity<PK>>
 	List<PK> findKeys(final Class<E> entityClass, final Criteria<PK, E> criteria, final Pagination pagination) {
 		throw new UnsupportedOperationException("cannot find keys");
 	}
 
 	public <PK extends Serializable, E extends Entity<PK>>
 	List<Map<String, Object>> findValueObjects(final Class<E> entityClass, final Criteria<PK, E> criteria, final Map<String, String> projection, final Pagination pagination) {
 		throw new UnsupportedOperationException("cannot find keys");
 	}
 
 	public <PK extends Serializable, E extends Entity<PK>>
 	E findUnique(final Class<E> entityClass, final Criteria<PK, E> criteria) {
 		return execute(entityClass, criteria, Pagination.UNIQUE, new QueryCallback<E, E>() {
 			public E doInQuery(final Query<? extends E> query) {
 				return query.get();
 			}
 		});
 	}
 
 	public <PK extends Serializable, E extends Entity<PK>>
 	E get(final Class<E> entityClass, final PK pk) {
 		return executor.execute(new SessionCallback<E, Objectify>() {
 			public E doInSession(final Objectify session) throws Exception {
				return session.get(getImplementation(entityClass), (Long) pk);
 			}
 		});
 	}
 
 	public <PK extends Serializable, E extends Entity<PK>>
 	void save(final E entity) {
 		executor.execute(new SessionCallback<Object, Objectify>() {
 			public Object doInSession(final Objectify session) throws Exception {
 				session.put(entity);
 				return null;
 			}
 		});
 	}
 
 	public <PK extends Serializable, E extends Entity<PK>>
 	void update(final E entity) {
 		executor.execute(new SessionCallback<Object, Objectify>() {
 			public Object doInSession(final Objectify session) throws Exception {
 				session.put(entity);
 				return null;
 			}
 		});
 	}
 
 	public <PK extends Serializable, E extends Entity<PK>>
 	void delete(final E entity) {
 		executor.execute(new SessionCallback<Object, Objectify>() {
 			public Object doInSession(final Objectify session) throws Exception {
 				session.delete(entity);
 				return null;
 			}
 		});
 	}
 
 	private <T, PK extends Serializable, E extends Entity<PK>>
 	T execute(final Class<E> entityClass, final Criteria<PK, E> criteria, final Pagination pagination, final QueryCallback<T, E> callback) {
 		return executor.execute(new SessionCallback<T, Objectify>() {
 			public T doInSession(final Objectify session) throws Exception {
 				final Query<? extends E> query = session.query(getImplementation(entityClass));
 
 				final ObjectifyCriteriaVisitor<PK, E> visitor = new ObjectifyCriteriaVisitor<PK, E>(query);
 				visitor.visit(criteria);
 
 				if (pagination.getStart() > 0) {
 					query.offset(pagination.getStart());
 				}
 				if (pagination.getCount() >= 0) {
 					query.limit(pagination.getCount());
 				}
 
 				return callback.doInQuery(query);
 			}
 		});
 	}
 
 
 	public static interface QueryCallback<T, E> {
 
 		T doInQuery(Query<? extends E> query);
 	}
 }
