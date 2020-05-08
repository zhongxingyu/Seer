 package com.excilys.computerdatabase.dao;
 
 import com.excilys.computerdatabase.model.Computer;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.orm.hibernate3.HibernateTemplate;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: gplassard
  * Date: 06/06/13
  * Time: 16:48
  * To change this template use File | Settings | File Templates.
  */
 @Repository
 public class HibernateComputerDao implements ComputerDao{
 
     private final static String[] columns = {"","c.id","c.name","c.introduced","c.discontinued","c.company.name"};
 
     private final static Logger logger = LoggerFactory.getLogger(HibernateComputerDao.class);
 
     private HibernateTemplate hibernateTemplate;
 
     @Autowired
     public void setSessionFactory(SessionFactory sessionFactory){
         hibernateTemplate = new HibernateTemplate(sessionFactory);
     }
 
     @Override
     public void openConnection() {
         logger.debug("Not opening connection since it's Spring's job to do it");
     }
 
     @Override
     public void closeConnection() {
         logger.debug("Not closing connection since it's Spring's job to do it");
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<Computer> getAll() throws DaoException {
         return hibernateTemplate.find("FROM Computer c LEFT JOIN FETCH c.company");
     }
 
     @Override
     @Transactional
     public void save(Computer computer) throws DaoException {
         hibernateTemplate.save(computer);
     }
 
     @Override
     @Transactional
     public void update(Computer computer) throws DaoException {
         hibernateTemplate.update(computer);
     }
 
     @Override
     @Transactional
     public void deleteAll() throws DaoException {
         hibernateTemplate.bulkUpdate("DELETE FROM Computer ");
     }
 
     @Override
     @Transactional(readOnly = true)
     public Computer findById(long computerId) throws DaoException {
         return hibernateTemplate.get(Computer.class,computerId);
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<Computer> getMatchingFromToWithSortedByColumn(String namePattern, int firstIndice, int lastIndice, int columnId) throws DaoException {
         String hqlQuery = "FROM Computer c LEFT JOIN FETCH c.company";
         if (!namePattern.trim().isEmpty()){
             hqlQuery += " WHERE c.name LIKE '%" + namePattern + "%'";
         }
         hqlQuery += String.format(" ORDER BY ISNULL(%s),%s %s",columns[Math.abs(columnId)],columns[Math.abs(columnId)], columnId > 0 ? "ASC" : "DESC");
         Query query = hibernateTemplate.getSessionFactory().getCurrentSession().createQuery(hqlQuery);
        query.setFirstResult(firstIndice).setMaxResults(lastIndice - firstIndice);
         return query.list();
     }
 
     @Override
     @Transactional(readOnly = true)
     public int numberOfMatching(String namePattern) throws DaoException {
         return ((Long) hibernateTemplate.find("SELECT COUNT(c.id) FROM Computer c WHERE c.name LIKE ?","%"+namePattern+"%").get(0)).intValue();
     }
 
     @Override
     @Transactional
     public void deleteById(long computerId) throws DaoException {
         hibernateTemplate.bulkUpdate("DELETE FROM Computer c WHERE c.id=?",computerId);
     }
 }
