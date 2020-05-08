 package com.jamesward;
 
 
 import java.util.List;
 
 import org.springframework.flex.remoting.RemotingDestination;
 import org.springframework.stereotype.Repository;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 
 @Repository
 @Service
 @RemotingDestination
 public class FooService
 {
 
   @PersistenceContext
   private EntityManager entityManager;
 
   @Transactional
   public Foo getFooById(String id)
   {
     return entityManager.find(Foo.class, id);
   }
 
   @Transactional
   public List<Foo> getAll()
   {
     CriteriaQuery<Foo> criteriaQuery = entityManager.getCriteriaBuilder().createQuery(Foo.class);
     Root<Foo> rootFoo= criteriaQuery.from(Foo.class);
     criteriaQuery.select(rootFoo);
     return entityManager.createQuery(criteriaQuery).getResultList();
   }
 
   @Transactional
   public Foo create(Foo foo)
   {
     entityManager.persist(foo);
     //entityManager.flush();
     return foo;
   }
 
   @Transactional
   public Foo update(Foo foo)
   {
     entityManager.merge(foo);
     //entityManager.flush();
     return foo;
   }
   
 }
