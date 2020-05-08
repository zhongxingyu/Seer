 package xeon.cm.dao;
 
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 import xeon.cm.model.Company;
 import xeon.cm.util.HibernateUtil;
 
 /**
  * User: xeon
  * Date: 3/17/13
  * Time: 10:39 PM
  */
 public class CompanyDAO {
     public Company getById(int id) {
         Session session = HibernateUtil.sessionFactory.getCurrentSession();
         session.beginTransaction();
        Company company = (Company) session.get(Company.class, id);
         session.getTransaction().commit();
         return company;
     }
 
     public Company getByName(String name) {
         Session session = HibernateUtil.sessionFactory.getCurrentSession();
         session.beginTransaction();
         Company company = (Company) session.createCriteria(Company.class).add(Restrictions.eq("name", name)).uniqueResult();
         session.getTransaction().commit();
         return company;
     }
 }
