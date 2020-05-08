 package com.igenox.fierarie.DAO.Impl;
 
 import com.igenox.fierarie.DAO.BaseCrudDAO;
 import com.igenox.fierarie.DAO.DAOUtil;
 import com.igenox.fierarie.domain.Company;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import java.io.Serializable;
 import java.lang.reflect.ParameterizedType;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: iga
  * Date: 8/16/13
  * Time: 8:24 PM
  * To change this template use File | Settings | File Templates.
  */
 public abstract class BaseCrudDAOImpl<T, PK extends Serializable>  implements BaseCrudDAO<T, PK>
 {
 
     public static final int DEFAULT_PAGE_SIZE = 100;
 
     protected Class<T> entityClass;
     @PersistenceContext
     protected EntityManager entityManager;
     private CriteriaBuilder criteriaBuilder;
 
 
     protected final CriteriaQuery<T> getContextCriteria() {
        return getContextCriteria(new ArrayList<Predicate>(), null);
     }
 
     protected final CriteriaQuery<T> getContextCriteria(CriteriaQuery<T> query) {
         return getContextCriteria(new ArrayList<Predicate>(), query);
     }
 
     protected final CriteriaQuery<T> getContextCriteria(List<Predicate> predicates, CriteriaQuery<T> query) {
         CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
         if (query == null)
             query = criteriaBuilder.createQuery(entityClass);
         EntityManagerFactory entityManagerFactory = entityManager.getEntityManagerFactory();
         Root<T> rootEntity = DAOUtil.findOrCreateFromRoot(entityManagerFactory, query, entityClass);
         List<Predicate> contextPredicates = getContextPredicate(rootEntity);
         contextPredicates.addAll(predicates);
         Predicate[] predicatesArray = contextPredicates.toArray(new Predicate[contextPredicates.size()]);
         query.select(rootEntity).where(criteriaBuilder.and(predicatesArray));
         return query;
     }
 
     protected List<Predicate> getContextPredicate(Root<T> rootEntity) {
         return new ArrayList<Predicate>();
     }
 
     public BaseCrudDAOImpl()
     {
         ParameterizedType genericTypeSuperClass = (ParameterizedType) this.getClass().getGenericSuperclass();
         this.entityClass = (Class<T>) genericTypeSuperClass.getActualTypeArguments()[0];
     }
 
     protected CriteriaBuilder getCriteriaBuilder() {
         if (criteriaBuilder == null)
             criteriaBuilder = entityManager.getCriteriaBuilder();
         return criteriaBuilder;
     }
 
     @Transactional
     @Override
     public T create(T t)
     {
         this.entityManager.persist(t);
         return t;
     }
 
     @Transactional
     @Override
     public T read(PK id)
     {
         return this.entityManager.find(entityClass, id);
     }
 
     @Transactional
     @Override
     public T update(T t)
     {
         return this.entityManager.merge(t);
     }
 
     @Transactional
     @Override
     public void delete(T t)
     {
         t = this.entityManager.merge(t);
         this.entityManager.remove(t);
     }
 
     @Transactional
     @Override
     public void delete(PK pk)
     {
         T found = this.entityManager.find(entityClass, pk);
         delete(found);
         /*
         this.entityManager.createQuery(
                 "delete from " + entityClass.getCanonicalName() + " where id = :deleteId ")
                 .setParameter("deleteId", pk).executeUpdate();
         */
     }
 
     @Transactional
     @Override
     public List<T> getAll() {
         return getAll(0, DEFAULT_PAGE_SIZE);
     }
 
     @Transactional
     @Override
     public List<T> getAllPaginated(int pageNo) {
         return getAll(pageNo, DEFAULT_PAGE_SIZE);
     }
 
     @Transactional
     @Override
     public List<T> getAll(int pageNo, int pageSize) {
         int from = pageNo * pageSize;
         int to = (pageNo + 1) * pageSize;
         return entityManager.createQuery(getContextCriteria()).setFirstResult(from).setMaxResults(to).getResultList();
     }
 
 }
