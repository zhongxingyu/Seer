 package ua.dp.primat.repositories;
 
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 import ua.dp.primat.domain.StudentGroup;
 
 @Transactional
 @Repository("studentGroupRepositoty")
 public class StudentGroupRepositoryImpl implements StudentGroupRepository {
 
     @PersistenceContext(unitName = "curriculum")
     private EntityManager em;
 
     public List<StudentGroup> getGroups() {
         return em.createNamedQuery(StudentGroup.GET_GROUPS_QUERY).getResultList();
     }
 
     public StudentGroup getGroupByCodeAndYearAndNumber(String code, Long year, Long number) {
         try {
             return (StudentGroup) em.createNamedQuery(StudentGroup.GET_GROUPS_BY_CODE_AND_YEAR_AND_NUMBER_QUERY)
                     .setParameter("code", code)
                     .setParameter("year", year)
                     .setParameter("number", number)
                     .getSingleResult();
         } catch (NoResultException nre) {
             return null;
         }
     }
 
     public void store(StudentGroup studentGroup) {
         if (contains(studentGroup)) {
             em.merge(studentGroup);
         } else {
             em.persist(studentGroup);
         }
     }
 
     public void remove(StudentGroup studentGroup) {
        em.remove(studentGroup);
     }
 
     public EntityManager getEm() {
         return em;
     }
 
     public void setEm(EntityManager em) {
         this.em = em;
     }
 
     public boolean contains(StudentGroup studentGroup) {
         if (studentGroup.getId() == null) {
             return false;
         }
         if (em.find(StudentGroup.class, studentGroup.getId()) == null) {
             return false;
         }
         return true;
     }
 }
