 /**
  * 
  */
 package org.imirsel.nema.dao.hibernate;
 
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.imirsel.nema.dao.ProfileDao;
 import org.imirsel.nema.model.MirexSubmission;
 import org.imirsel.nema.model.Profile;
 
 /**
  * Hibernate implementation of {@link ProfileDao}
  * @author gzhu1
  *
  */
 public class ProfileDaoHibernate extends GenericDaoHibernate<Profile, Long>
         implements ProfileDao {
 
     public ProfileDaoHibernate() {
         super(Profile.class);
     }
 
     /**
      * {@inheritDoc }
      */
     public List<Profile> getContributors(MirexSubmission submission) {
         List<Profile> list = getHibernateTemplate().find("from Profile where Id=?", submission.getId());
         return list;
     }
 
     /**
      * {@inheritDoc }
      */
     public List<Profile> findSimilar(String str) {
         List<Profile> list =
                 getHibernateTemplate().find(
                 "from Profile where (firstname like ?) or (lastname like ?) or (organization like ?)",
                 fuzzy(str), fuzzy(str), fuzzy(str));
         return list;
     }
 
     private String fuzzy(String str) {
         return "%" + str + "%";
     }
 
     /**
      * {@inheritDoc }
      */
     public Profile findByUuid(UUID uuid) {
        List<Profile> list=getHibernateTemplate().find("from Profile where uuidStr=",uuid.toString());
         if ((list!=null)&&(list.size()>0)){
             return list.get(0);
         }else{return null;}
     }
 }
