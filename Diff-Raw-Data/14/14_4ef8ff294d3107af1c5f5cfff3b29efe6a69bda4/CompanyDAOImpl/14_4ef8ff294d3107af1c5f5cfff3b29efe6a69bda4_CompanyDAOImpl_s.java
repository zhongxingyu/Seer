 package com.igenox.fierarie.DAO.Impl;
 
 import com.igenox.fierarie.DAO.CompanyDAO;
 import com.igenox.fierarie.domain.Company;
 import org.springframework.stereotype.Component;
 
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: iga
  * Date: 4/13/13
  * Time: 7:48 AM
  * To change this template use File | Settings | File Templates.
  */
 @Component
 public class CompanyDAOImpl extends TenantAwareBaseCrudDAO<Company, Long> implements CompanyDAO
 {
     public Company getRootTenant() {
         CriteriaQuery<Company> query = getContextCriteria();
        Predicate p = getCriteriaBuilder().equal(query.from(entityClass).get("type"), Company.CompanyType.ROOT_TENANT.getId());
         List<Predicate> predicates = Arrays.asList(new Predicate[]{p});
         return entityManager.createQuery(getContextCriteria(predicates, query)).getSingleResult();
     }
 }
