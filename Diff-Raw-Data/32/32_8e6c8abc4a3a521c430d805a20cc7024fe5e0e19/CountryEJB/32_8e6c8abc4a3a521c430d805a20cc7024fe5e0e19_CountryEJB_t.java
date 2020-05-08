 /*
  * Copyright 2011 QTronic GmbH. All rights reserved.
  */
 package org.ffplanner.bean;
 
 import org.ffplanner.entity.Country;
 import org.ffplanner.entity.Country_;
 
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 import java.util.List;
 
 /**
  * @author Bogdan Dumitriu
  */
@Stateless
@LocalBean
 public class CountryEJB {
 
     @PersistenceContext(unitName = "ffp")
     private EntityManager entityManager;
 
     public Country getCountry(String countryName) {
         final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
         final CriteriaQuery<Country> query = criteriaBuilder.createQuery(Country.class);
         final Root<Country> root = query.from(Country.class);
         query.where(criteriaBuilder.equal(root.get(Country_.name), countryName));
         final TypedQuery<Country> countryQuery = entityManager.createQuery(query);
         final List<Country> result = countryQuery.getResultList();
         final Country country;
         if (result.isEmpty()) {
             country = new Country(countryName);
         } else {
             assert result.size() == 1;
             country = result.get(0);
         }
         return country;
     }
 }
