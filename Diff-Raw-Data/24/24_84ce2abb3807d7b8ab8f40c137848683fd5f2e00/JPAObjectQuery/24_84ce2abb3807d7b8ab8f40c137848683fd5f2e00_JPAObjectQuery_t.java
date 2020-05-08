 package org.objectquery.jpaobjectquery;
 
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.objectquery.ObjectQuery;
 import org.objectquery.generic.GenericObjectQuery;
 import org.objectquery.generic.ObjectQueryException;
 
 public class JPAObjectQuery {
 
 	public static JPQLQueryGenerator jpqlGenerator(ObjectQuery<?> objectQuery) {
 		if (objectQuery instanceof GenericObjectQuery<?>)
 			return new JPQLQueryGenerator((GenericObjectQuery<?>) objectQuery);
 		throw new ObjectQueryException("The Object query instance of unconvertable implementation ", null);
 	}
 
 	public static Query buildQuery(ObjectQuery<?> objectQuery, EntityManager entityManager) {
 		JPQLQueryGenerator gen = jpqlGenerator(objectQuery);
 		Query qu = entityManager.createQuery(gen.getQuery());
		Map<String, Object> pars = gen.getParameters();
 		for (Map.Entry<String, Object> ent : pars.entrySet()) {
 			qu.setParameter(ent.getKey(), ent.getValue());
 		}
 		return qu;
 	}
 
 	public static Object execute(ObjectQuery<?> objectQuery, EntityManager entityManager) {
 		return buildQuery(objectQuery, entityManager).getResultList();
 	}
 
 }
