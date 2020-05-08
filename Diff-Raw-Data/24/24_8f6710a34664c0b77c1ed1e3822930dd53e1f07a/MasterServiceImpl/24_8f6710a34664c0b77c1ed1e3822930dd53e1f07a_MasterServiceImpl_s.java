 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package perpus.service;
 
 import java.util.List;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import perpus.domain.Anggota;
 import perpus.domain.Buku;
 import perpus.domain.security.Pegawai;
 
 /**
  *
  * @author martinusadyh
  */
 @Service("masterService")
 @Transactional(readOnly=true)
 public class MasterServiceImpl implements MasterService {
     
     @Autowired private SessionFactory sessionFactory;
     
     @Override
     @Transactional(readOnly=false)
     public void save(Object obj) {
         sessionFactory.getCurrentSession().saveOrUpdate(obj);
     }
     
     @Override
     @Transactional(readOnly=false)
     public void delete(Object obj) {
         sessionFactory.getCurrentSession().delete(obj);
     }
 
     @Override
     public List<Buku> findAllBukus() {
         return sessionFactory.getCurrentSession()
                 .createQuery("from Buku bk order by bk.createdDate desc")
                 .list();
     }
 
     @Override
     public List<Pegawai> findAllPegawai() {
         return sessionFactory.getCurrentSession()
                 .createQuery("from Pegawai pgw order by pgw.createdDate desc")
                 .list();
     }
 
     @Override
     public Anggota findAnggotaById(String id) {
         return (Anggota) sessionFactory.getCurrentSession()
                 .createQuery("from Anggota ag where ag.id = :id")
                 .setParameter("id", id)
                 .uniqueResult();
     }
 }
