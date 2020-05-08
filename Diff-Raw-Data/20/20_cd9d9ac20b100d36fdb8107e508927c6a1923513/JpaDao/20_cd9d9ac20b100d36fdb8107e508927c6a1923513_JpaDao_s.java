 package com.thingtrack.konekti.dao.template;
 
 import java.io.Serializable;
 import java.lang.reflect.ParameterizedType;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 import javax.persistence.metamodel.EntityType;
 
 import com.thingtrack.konekti.dao.template.DaoFilter.Comparator;
 
 public abstract class JpaDao<T, ID extends Serializable> implements Dao<T, ID> {
 	private final Class<T> entityClass;
 	
 	@PersistenceContext
 	private EntityManager entityManager;
 	
 	@SuppressWarnings("unchecked")
 	public JpaDao() {
 		this.entityClass = (Class<T>) ((ParameterizedType) getClass()
 				.getGenericSuperclass()).getActualTypeArguments()[0];
 		
 	}
 	
 	public JpaDao(final Class<T> entityClass) {
 		super();
 		this.entityClass = entityClass;
 		
 	}
 	
 	@Override
 	public Class<T> getEntityClass() {
 		return entityClass;
 		
 	}
 
 	@Override
 	public String getEntityName() {
 		return entityClass.getSimpleName();
 		
 	}
 	
 	@Override
 	public EntityType<T> getEntityType() {
 		return getEntityManager().getEntityManagerFactory().getMetamodel().entity(entityClass);
 		
 	}
 	
 	public EntityManager getEntityManager() {
 		return entityManager;
 		
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<T> getAll() {
 		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
 		CriteriaQuery<T> criteriaQuery = (CriteriaQuery<T>) criteriaBuilder.createQuery();
 		Root<T> from = criteriaQuery.from(getEntityClass());
 		CriteriaQuery<T> select = criteriaQuery.select(from);
 		TypedQuery<T> typedQuery = getEntityManager().createQuery(select);
 		
 		return typedQuery.getResultList();
 		
 	}
 
 	@Override
 	public T get(final ID id) {
 		return getEntityManager().find(entityClass, id);
 		
 	}
 	
 	@Override
 	public void delete(T entity) {
 		getEntityManager().remove(getEntityManager().merge(entity));
 		
 	}
 	
 	@Override
 	public T save(T entity) {
 		return getEntityManager().merge(entity);
 		
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<T> getFiltered(List<DaoFilter> filters) throws Exception {
 		StringBuffer queryBuffer = new StringBuffer();
 
 		queryBuffer.append("SELECT e");
 		queryBuffer.append(" FROM " + getEntityName() + " e");
 		queryBuffer.append(" WHERE");
 		
 		// create dynamic criteria
 		List<String> criteria = new ArrayList<String>();
 
 		for(DaoFilter daoFilter : filters ) {
 			if (daoFilter.getFirstValue() != null) {
 				if (daoFilter.getComparator() == Comparator.EQUAL)
 					criteria.add("e." + daoFilter.getAttributeName() + " = :" + daoFilter.getAttributeName());
 				else if ((daoFilter.getComparator() == Comparator.GREATER_THAN))
 					criteria.add("e." + daoFilter.getAttributeName() + " > :" + daoFilter.getAttributeName());
 				else if ((daoFilter.getComparator() == Comparator.LESS_THAN))
 					criteria.add("e." + daoFilter.getAttributeName() + " < :" + daoFilter.getAttributeName());
 				else if ((daoFilter.getComparator() == Comparator.GREATER_THAN_OR_EQUAL_TO))
 					criteria.add("e." + daoFilter.getAttributeName() + " >= :" + daoFilter.getAttributeName());
 				else if ((daoFilter.getComparator() == Comparator.LESS_THAN_OR_EQUAL_TO))
 					criteria.add("e." + daoFilter.getAttributeName() + " <= :" + daoFilter.getAttributeName());
 				else if (daoFilter.getComparator() == Comparator.NOT_EQUAL)
 					criteria.add("e." + daoFilter.getAttributeName() + " != :" + daoFilter.getAttributeName());				
 				else if ((daoFilter.getComparator() == Comparator.BETWEEN))
 					criteria.add("e." + daoFilter.getAttributeName() + " BETWEEN :" + daoFilter.getAttributeName() + "_First" + " AND :" + daoFilter.getAttributeName() + "_Last");
 				else if ((daoFilter.getComparator() == Comparator.NOT_BETWEEN))
 					criteria.add("e." + daoFilter.getAttributeName() + " NOT BETWEEN :" + daoFilter.getAttributeName() + "_First" + " AND :" + daoFilter.getAttributeName() + "_Last");				
 				else if ((daoFilter.getComparator() == Comparator.CONTAINS))
 					criteria.add("e." + daoFilter.getAttributeName() + " LIKE %:" + daoFilter.getAttributeName() + "%");
 				else if ((daoFilter.getComparator() == Comparator.CONTAINS_LEFT))
 					criteria.add("e." + daoFilter.getAttributeName() + " LIKE %:" + daoFilter.getAttributeName());
 				else if ((daoFilter.getComparator() == Comparator.CONTAINS_RIGHT))
 					criteria.add("e." + daoFilter.getAttributeName() + " LIKE :" + daoFilter.getAttributeName() + "%");
 				else if ((daoFilter.getComparator() == Comparator.IS_NULL))
 					criteria.add("e." + daoFilter.getAttributeName() + " IS NULL");
 				else if ((daoFilter.getComparator() == Comparator.IS_NOT_NULL))
 					criteria.add("e." + daoFilter.getAttributeName() + " IS NOT NULL");
 				else if ((daoFilter.getComparator() == Comparator.MEMBER_OF))
 					criteria.add(":" + daoFilter.getAttributeName() + "_Atom MENBER OF e." + daoFilter.getAttributeName());
 				else if ((daoFilter.getComparator() == Comparator.NOT_MEMBER_OF))
 					criteria.add(":" + daoFilter.getAttributeName() + "_Atom NOT MENBER OF e." + daoFilter.getAttributeName());						
 				else if ((daoFilter.getComparator() == Comparator.EMPTY))
 					criteria.add("e." + daoFilter.getAttributeName() + " IS EMPTY");
 				else if ((daoFilter.getComparator() == Comparator.NOT_EMPTY))
 					criteria.add("e." + daoFilter.getAttributeName() + " IS NOT EMPTY");				
 			}
 
 		}
 		
 		if (criteria.size() == 0)
 			throw new RuntimeException("no criteria");
 			
 		for (int i = 0; i < criteria.size(); i++) {
 			if (i > 0)  
 				queryBuffer.append(" AND ");
 			
 			queryBuffer.append(criteria.get(i));
 		}
 	
 		
 		// create dynamic Query and bind data
 		Query query = getEntityManager().createQuery(queryBuffer.toString());
 		
 		for(DaoFilter daoFilter : filters ) {
 			if (daoFilter.getComparator() != Comparator.BETWEEN) {
 				query.setParameter(daoFilter.getEntityName() + "_First", daoFilter.getFirstValue());
 				query.setParameter(daoFilter.getEntityName() + "_Last", daoFilter.getLastValue());
 			}						
 			else if (daoFilter.getComparator() == Comparator.MEMBER_OF)
 				query.setParameter(daoFilter.getEntityName() + "_Atom", daoFilter.getFirstValue());
 			else if (daoFilter.getComparator() == Comparator.NOT_MEMBER_OF)
 				query.setParameter(daoFilter.getEntityName() + "_Atom", daoFilter.getFirstValue());			
 			else
 				query.setParameter(daoFilter.getEntityName(), daoFilter.getFirstValue());			
 				
 		}
 		
 		return query.getResultList();
 	}
 }
