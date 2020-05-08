 /*
  * ClasifiedAdRepository.java
  * Copyright (c) 2009, Monte Alto Research Center, All Rights Reserved.
  *
  * This software is the confidential and proprietary information of
  * Monte Alto Research Center ("Confidential Information"). You shall not
  * disclose such Confidential Information and shall use it only in
  * accordance with the terms of the license agreement you entered into
  * with Monte Alto Research Center
  */
 package com.marc.lastweek.business.entities.classifiedad.repository;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import com.marc.lastweek.business.entities.classifiedad.ClassifiedAd;
 import com.marc.lastweek.business.views.classifiedad.FilterParameters;
 
 @Repository
 public class ClassifiedAdRepository {
 
 	@Autowired
 	private SessionFactory sessionFactory;
 
 	public List<ClassifiedAd> filterAdvertisements(FilterParameters parameters, int start, int count) {
         Criteria criteriaQuery = advancedSearchQueyConstructor(parameters); 
        criteriaQuery.setFirstResult(start);
        criteriaQuery.setFetchSize(count);
         return criteriaQuery.list();
 	}
 
 	public int countFilterAdvertisements(FilterParameters parameters) {
         Criteria criteriaQuery = advancedSearchQueyConstructor(parameters); 
         
         return criteriaQuery.list().size();
 	}
 
 	private Criteria advancedSearchQueyConstructor(FilterParameters parameters) {
 
 		Criteria criteriaQuery = 
 			this.sessionFactory.getCurrentSession().createCriteria(ClassifiedAd.class);
 		criteriaQuery.addOrder(Order.asc("publicationDate"));
 
 		if (parameters.getCategoryId()!=null) {
 			criteriaQuery.createCriteria("category").add(Restrictions.eq("id", parameters.getCategoryId()));
 		}
 		if (parameters.getProvinceId()!=null) {
 			criteriaQuery.createCriteria("province").add(Restrictions.eq("id", parameters.getProvinceId()));
 		}
 		if (parameters.getSearchString()!=null) {
 			// TODO: how to search in description
 			criteriaQuery.add(Restrictions.ilike("description", "%"+parameters.getSearchString()+"%"));
 		}
 		if (parameters.getSubcategoryId()!=null) {
 			criteriaQuery.createCriteria("subcategory").add(Restrictions.eq("id", parameters.getSubcategoryId()));
 		}
 		return criteriaQuery;
 
 	}
 }
