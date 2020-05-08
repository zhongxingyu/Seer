 package com.infinitiessoft.btrs.validation;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 
 import org.hibernate.mapping.Property;
 import org.hibernate.validator.PropertyConstraint;
 import org.hibernate.validator.Validator;
 import org.jboss.seam.Component;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.framework.Home;
 
 @Name("UniqueValidator")
 public class UniqueValidator implements Validator<Unique>, PropertyConstraint {
 
 	// Entity for which validation is to be fired
 	private String targetEntity;
 	// Field for which validation is to be fired.
 	private String field;
 	// Seam home component for entity
 	private String idProvider;
 
 	
 
 	@Override
 	public boolean isValid(Object value) {
 		EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
 		
 		Home<?, ?> home = (Home<?, ?>) Component.getInstance(idProvider);
 		boolean hasId = home.getId() != null;
 		
 		String queryString = "select t from " + targetEntity + " t where lower(t." + field + ") = lower(:value)";
 		if (hasId) {
 			queryString += " and t.id <> :id";
 		}
 		
 		Query query = entityManager.createQuery(queryString);
 		query.setParameter("value", value);
 		if (hasId) {
 			query.setParameter("id", home.getId());
 		}
 		
 		try {
 			query.getSingleResult();
 			return false;
 		} catch (final NoResultException e) {
 			return true;
 		}
 	}
 
 	@Override
 	public void apply(Property property) {
 	}
 
 	@Override
 	public void initialize(Unique parameters) {
 		targetEntity = parameters.entityName();
 		field = parameters.fieldName();
 		idProvider = parameters.idProvider();
 	}
 
 }
