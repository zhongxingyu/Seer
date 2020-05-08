 package Dao;
 
 import Utils.HibernateUtil;
 import Model.Subjects;
 import java.util.List;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 /**
  *
  * @author AgtLucas
  */
 public class SubjectsDaoImp implements SubjectsDao {
     
     @Override
     public void save(Subjects subjects) {
         Session session = HibernateUtil.getSessionFactory().openSession();
         Transaction t = session.beginTransaction();
         session.save(subjects);
         t.commit();
     }
     
     @Override
     public Subjects getSubjects(long id) {
         Session session = HibernateUtil.getSessionFactory().openSession();
         return (Subjects) session.load(Subjects.class, id);
     }
     
     @Override
     public List<Subjects> list() {
         Session session = HibernateUtil.getSessionFactory().openSession();
         Transaction t = session.beginTransaction();
        List li = session.createQuery("from subjects").list();
         t.commit();
         return li;
     }
     
     @Override
     public void delete(Subjects subjects) {
         Session session = HibernateUtil.getSessionFactory().openSession();
         Transaction t = session.beginTransaction();
         session.delete(subjects);
         t.commit();
     }
     
     @Override
     public void update(Subjects subjects) {
         Session session = HibernateUtil.getSessionFactory().openSession();
         Transaction t = session.beginTransaction();
         t.commit();
     }
     
 }
