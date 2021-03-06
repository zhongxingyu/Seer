 package org.jdataset.impl.provider.jpa;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.jdataset.Parameter;
 import org.jdataset.impl.provider.AbstractQueryDataProvider;
 import org.jdataset.impl.provider.DataQuery;
 import org.jdataset.provider.QueryDataProvider;
 
 /**
  * Base class for a JPA based {@link QueryDataProvider}. Override and implement
  * {@link #createJpaQuery(String)} to create a query of the type needed.
  * 
  * @see JpaDataProvider
  * @see JpaNativeDataProvider
  * 
  * @author Andy Gibson
  * 
  * @param <T>
  */
 public abstract class AbstractJpaDataProvider<T> extends
 		AbstractQueryDataProvider<T> {
 
 	private static final long serialVersionUID = 1L;
 
 	private EntityManager entityManager;
 
 	/**
 	 * Override to create the specific type of query to use.
 	 * 
 	 * @see JpaDataProvider
 	 * @see JpaNativeDataProvider
 	 * 
 	 * @param ql
 	 *            Statement the query must execute (could be EJBQL or Native
 	 *            depending on subclass)
 	 * @return {@link Query} object created from the
 	 *         {@link AbstractJpaDataProvider#entityManager} and configured with
 	 *         the passed in sql.
 	 */
 	protected abstract Query createJpaQuery(String ql);
 
 	public EntityManager getEntityManager() {
 		return entityManager;
 	}
 
 	public void setEntityManager(EntityManager entityManager) {
 		this.entityManager = entityManager;
 	}
 
 	/**
 	 * Initializes a JPA {@link Query} using the passed in {@link DataQuery}.
 	 * The type of query returned is determined from the
 	 * {@link AbstractJpaDataProvider#createJpaQuery(String)} method which can
 	 * return a native or EJBQL query depending on the subclass.
 	 * 
 	 * @param dataQuery The {@link DataQuery} to initialize the query with
 	 * @return The initialized {@link Query}
 	 */
 	private final Query buildJpaQuery(DataQuery dataQuery) {
 		Query qry = createJpaQuery(dataQuery.getStatement());
 		for (Parameter param : dataQuery.getParameters()) {
 			qry.setParameter(param.getName(), param.getValue());
 		}
 		return qry;
 	}
 
 	@Override
 	protected Integer queryForCount(DataQuery query) {
 		Query qry = buildJpaQuery(query);
 		Long result = (Long) qry.getSingleResult();
 		return new Integer(result.intValue());
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	protected List<T> queryForResults(DataQuery query, Integer firstResult,
 			Integer count) {
 		Query qry = buildJpaQuery(query);
 		if (firstResult != null) {
 			qry.setFirstResult(firstResult.intValue());
 		}
 
 		if (count != null) {
 			qry.setMaxResults(count);
 		}
 
 		return qry.getResultList();
 	}
 }
