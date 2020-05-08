 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package kasbon.service;
 
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.ejb.LocalBean;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import kasbon.entity.Karyawan;
 
 /**
  *
  * @author endy
  */
 @Stateless
 @LocalBean
 public class AplikasiKasbonService {
     @PersistenceContext(unitName = "aplikasi-kasbon-ejbPU")
     private EntityManager em;
     
     public void simpan(Karyawan karyawan) {
         em.persist(karyawan);
     }
 
     public Karyawan findKaryawanById(Long id){
         return em.find(Karyawan.class, id);
     }
     
     public List<Karyawan> findAllKaryawan(){
        return em.createQuery("select k from Karyawan order by k.nip")
                 .getResultList();
     }
     
     public List<Karyawan> findAllKaryawan(Integer start, Integer rows){
        return em.createQuery("select k from Karyawan order by k.nip")
                 .setFirstResult(start)
                 .setMaxResults(rows)
                 .getResultList();
     }
     
     public Long countAllKaryawan(){
        return (Long) em.createQuery("select count(k) from Karyawan")
                 .getSingleResult();
     }
     
     public List<Karyawan> findKaryawanByNama(String nama){
        return em.createQuery("select k from Karyawan "
                 + "where k.nama like :nama "
                 + "order by k.nip")
                 .setParameter("nama", "%"+nama+"%")
                 .getResultList();
     }
 }
