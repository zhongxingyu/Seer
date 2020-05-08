 package by.bsu.project.dao;
 
 import by.bsu.project.entity.ProgramFilesEntity;
 import by.bsu.project.entity.UserInfoEntity;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import java.util.List;
 
 /**
  * @author Alina Glumova
  */
 
 @Repository
 public class UserInfoDAOImpl implements UserInfoDAO {
 
     @Autowired
     private SessionFactory sessionFactory;
 
     @Override
     public List<UserInfoEntity> studentsList(int pageNumber) {
        Query query = sessionFactory.getCurrentSession().createQuery("from UserInfoEntity where form != 'admin'  order by id asc");
         return getSubList(query, pageNumber);
     }
 
     public List<ProgramFilesEntity> programsList(int pageNumber, Long id) {
         Query query = sessionFactory.getCurrentSession().createQuery("from ProgramFilesEntity where userId = :id").
                 setParameter("id", id);
         return getSubList(query, pageNumber);
 
     }
 
     @Override
     public Long studentsCountList() {
         return (Long) sessionFactory.getCurrentSession().createQuery("select count(*) from UserInfoEntity").uniqueResult();
     }
 
     public void save(UserInfoEntity userInfoEntity) {
         if (isExist(userInfoEntity.getId())) {
             sessionFactory.getCurrentSession().update(userInfoEntity);
         } else {
             sessionFactory.getCurrentSession().save(userInfoEntity);
         }
     }
 
     @Override
     public void deleteStudentById(Long id) {
         sessionFactory.getCurrentSession().delete(sessionFactory.getCurrentSession().load(UserInfoEntity.class, id));
     }
 
     @Override
     public UserInfoEntity getStudentById(Long id) {
         return (UserInfoEntity) sessionFactory.getCurrentSession().createQuery("from UserInfoEntity where id = :id").
                 setParameter("id", id).uniqueResult();
     }
 
     @Override
     public ProgramFilesEntity getFileById(Long id) {
         return (ProgramFilesEntity) sessionFactory.getCurrentSession().createQuery("from ProgramFilesEntity where id = :id").
                 setParameter("id", id).uniqueResult();
     }
 
     @Override
     public boolean isExist(Long id) {
         return id == null ? false : null != (UserInfoEntity) sessionFactory.getCurrentSession().load(UserInfoEntity.class, id);
     }
 
     @Override
     public UserInfoEntity findStudentByLogin(String login) {
         return (UserInfoEntity) sessionFactory.getCurrentSession().createQuery("from UserInfoEntity where login = :login").
                 setParameter("login", login).uniqueResult();
     }
 
     private List getSubList(Query query, int pageNumber) {
         query.setFirstResult(3 * (pageNumber));
         query.setMaxResults(3);
         return query.list();
     }
 }
