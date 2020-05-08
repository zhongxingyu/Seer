 package id.ac.pcr.springhibernate.service;
 
 import id.ac.pcr.springhibernate.model.Dosen;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Deny Prasetyo
  * Date: 4/4/13
  * Time: 6:19 AM
  * To change this template use File | Settings | File Templates.
  */
 
 @Service
@Transactional
 public class DosenService {
     @PersistenceContext
     protected EntityManager entityManager;
 
 
     public void save(Dosen dosen) {
         entityManager.persist(dosen);
     }
 
 
    public void delete(Dosen dosen) {
        if (dosen != null) {
            entityManager.remove(dosen);
         }
 
     }
 
 
     public Dosen findDosen(Long id) {
         return entityManager.find(Dosen.class, id);
     }
 
 
     @SuppressWarnings("unchecked")
     public List<Dosen> findDosens() {
         return entityManager.createQuery("SELECT o from Dosen o order by o.id").getResultList();
     }
 
 
     public List<Dosen> findByNama(String nama) {
         return entityManager.createQuery("SELECT o FROM Dosen o WHERE o.nama LIKE :nama")
                 .setParameter("nama", "%" + nama + "%").getResultList();
     }
 
 
     @SuppressWarnings("unchecked")
     public List<Dosen> findDosens(int start, int max) {
         return entityManager.createQuery("SELECT o from Dosen o order by o.id").setFirstResult(start).setMaxResults(max).getResultList();
     }
 
 
     public Long countDosens() {
         return (Long) entityManager.createQuery("select count(o) from Dosen o").getSingleResult();
     }
 
 }
