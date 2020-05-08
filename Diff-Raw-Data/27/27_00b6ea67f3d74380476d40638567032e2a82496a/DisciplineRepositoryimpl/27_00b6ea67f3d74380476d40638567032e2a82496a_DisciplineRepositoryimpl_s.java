/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 package ua.dp.primat.repositories;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 import ua.dp.primat.domain.workload.Discipline;
 /**
  *
  * @author pesua
  */
 
 
 @Repository("disciplineRepository")
 @Transactional
 public class DisciplineRepositoryimpl implements DisciplineRepository {
 
     public void store(Discipline discipline) {
         if (em.contains(discipline) || (discipline.getId() != null)) {
             em.merge(discipline);
         } else {
             em.persist(discipline);
         }
     }
 
     @SuppressWarnings("unchecked")
     @Transactional(readOnly=true)
     public List<Discipline> getDisciplines() {
         return em.createNamedQuery(Discipline.GET_ALL_DISCIPLINES_QUERY).getResultList();
     }
 
     public void delete(Discipline discipline) {
         //load(discipline.id);
         final Discipline r = em.find(Discipline.class, discipline.getId());
         if (em.contains(r)) {
             em.remove(r);
         }
     }
 
     public Discipline load(Long id){
         return em.find(Discipline.class, id);
     }
 
     @PersistenceContext(unitName = "curriculum")
     private EntityManager em;
 }
