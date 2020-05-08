 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jplanner.service;
 
 import java.util.List;
 import jplanner.domain.Aktivitas;
 import jplanner.domain.Proyek;
 import jplanner.domain.Resource;
 import org.hibernate.SessionFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author martinusadyh
  */
 @Service("jplannerService")
 @Transactional(readOnly = true)
 public class JPlannerServiceImpl implements JPlannerService {
 
     private final Logger logger = LoggerFactory.getLogger(this.getClass());
     @Autowired
     private SessionFactory sessionFactory;
 
     @Override
     @Transactional(readOnly = false)
     public void save(Object obj) {
         sessionFactory.getCurrentSession().saveOrUpdate(obj);
     }
 
     @Override
     @Transactional(readOnly = false, rollbackFor = Exception.class)
     public void saveAktivitas(Aktivitas obj) {
         obj.setNomorTask(generateNomorTask(obj.getProyek(), obj.getParent()));
 
         // update resource dulu
         List<Resource> resources = obj.getResources();
         for (Resource r : resources) {
             r.setIsUsed(Boolean.TRUE);
             r.setAktivitas(obj);
         }
         
         sessionFactory.getCurrentSession().saveOrUpdate(obj);
     }
 
     private String generateNomorTask(Proyek p, Aktivitas parent) {
         Aktivitas lastActivity = getLastAktivitasByProjectAndParent(p, parent);
 
         if (lastActivity == null) {
             if (parent == null) {
                 return "1";
             } else {
                 return parent.getNomorTask() + ".1";
             }
         }
 
         String lastNumber = lastActivity.getNomorTask();
         String[] splitedLastNumber = lastNumber.split("\\.");
         String endChar = splitedLastNumber[splitedLastNumber.length - 1];
         Integer nextNumber = Integer.valueOf(endChar) + 1;
 
         StringBuilder newNumber = new StringBuilder();
         for (int i = 0; i < splitedLastNumber.length - 1; i++) {
             newNumber.append(splitedLastNumber[i]);
             newNumber.append(".");
         }
         newNumber.append(nextNumber);
        
        // membersihkan object aktivitas yang sudah di load
        sessionFactory.getCurrentSession().clear();
 
         return newNumber.toString();
     }
 
     @Override
     public List findAll(String className) {
         StringBuilder sb = new StringBuilder();
         sb.append("from ").append(className).append(" obj ");
         sb.append("order by obj.createdDate desc");
         return sessionFactory.getCurrentSession()
                 .createQuery("from " + className)
                 .list();
     }
 
     @Override
     public List<Resource> findAvailableResource(String name) {
         return sessionFactory.getCurrentSession()
                 .createQuery("from Resource r where r.isUsed = false")
                 .list();
     }
 
     @Override
     public List<Aktivitas> findAllAktivitasByProject(Proyek proyek) {
         return sessionFactory.getCurrentSession()
                 .createQuery("from Aktivitas a where a.proyek.id = :prmProyekSID order by a.nomorTask asc")
                 .setParameter("prmProyekSID", proyek.getId())
                 .list();
     }
 
     @Override
     public Integer findAktivitasByProject(Proyek p) {
         Integer durasi = (Integer) sessionFactory.getCurrentSession()
                 .createQuery("select obj.durasi from Aktivitas obj where obj.proyek.id = :prmIDProyek order by obj.durasi desc")
                 .setParameter("prmIDProyek", p.getId())
                 .setMaxResults(1)
                 .uniqueResult();
 
         if (durasi == null) {
             durasi = 0;
         }
 
         return durasi;
     }
 
     private Aktivitas getLastAktivitasByProjectAndParent(Proyek p, Aktivitas parent) {
         if (parent != null) {
             return (Aktivitas) sessionFactory.getCurrentSession()
                     .createQuery("select obj from Aktivitas obj where obj.proyek.id = :prmIDProyek and obj.parent.id = :prmIDParent order by obj.nomorTask desc")
                     .setParameter("prmIDProyek", p.getId())
                     .setParameter("prmIDParent", parent.getId())
                     .setMaxResults(1)
                     .uniqueResult();
         } else {
             return (Aktivitas) sessionFactory.getCurrentSession()
                     .createQuery("select obj from Aktivitas obj where obj.proyek.id = :prmIDProyek and obj.parent is null order by obj.nomorTask desc")
                     .setParameter("prmIDProyek", p.getId())
                     .setMaxResults(1)
                     .uniqueResult();
         }
     }
 
     @Override
     public List<Aktivitas> findAllAktivitas() {
         return sessionFactory.getCurrentSession()
                 .createQuery("from Aktivitas a order by a.proyek.id asc, a.nomorTask asc")
                 .list();
     }
 }
