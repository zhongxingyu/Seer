 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.avangarde.gnosis.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import javax.persistence.criteria.CriteriaQuery;
 import org.avangarde.gnosis.entity.Publication;
 
 /**
  *
  * @author Familia Martinez
  */
 public class PublicationDAO implements IDAO<Publication> {
 
     private static PublicationDAO instance;
 
     private PublicationDAO() {
     }
 
     public static synchronized PublicationDAO getInstance() {
         if (instance == null) {
             instance = new PublicationDAO();
         }
         return instance;
     }
 
     @Override
     public void persist(Publication entity, EntityManager em) {
         em.persist(entity);
     }
 
     @Override
     public Publication find(Object id, EntityManager em) {
         return (Publication) em.find(Publication.class, id);
     }
 
     @Override
     public void update(Publication entity, EntityManager em) {
         em.merge(entity);
     }
 
     @Override
     public void delete(Object id, EntityManager em) {
         Publication publication = (Publication) em.getReference(Publication.class, id);
         em.remove(publication);
     }
 
     @Override
     public List<Publication> getList(EntityManager em) {
         CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
         cq.select(cq.from(Publication.class));
         Query q = em.createQuery(cq);
         return q.getResultList();
     }
 
     public List<String> getTopicsBySubject(EntityManager em, Integer subjectCode) {
         List<String> list;
         Query q = em.createQuery("SELECT DISTINCT p.topic FROM Subject s INNER JOIN s.publicationList p "
                 + "WHERE s.code LIKE :subjectCode").setParameter("subjectCode", subjectCode.toString());
         try {
             list = q.getResultList();
         } catch (Exception e) {
             list = new ArrayList<String>();
         }
         return list;
     }
 
     public List<Publication> getPublicationsByTopic(String topic, EntityManager em) {
         List<Publication> list;
         Query q = em.createQuery("SELECT p FROM Publication p WHERE p.topic LIKE :topic "
                 + "ORDER BY p.title").setParameter("topic", topic);
         try {
             list = q.getResultList();
         } catch (Exception e) {
             list = new ArrayList<Publication>();
         }
         return list;
     }
     
     public int getNewId(EntityManager em){
         Integer newId;
         newId = ((Integer)em.createQuery("SELECT MAX(p.id) FROM Publication p").getSingleResult());
        return newId+1;
     }
 }
