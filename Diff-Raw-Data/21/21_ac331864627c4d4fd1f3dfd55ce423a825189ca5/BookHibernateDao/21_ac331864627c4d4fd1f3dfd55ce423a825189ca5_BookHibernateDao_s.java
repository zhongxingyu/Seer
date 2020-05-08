 package ru.example.model.dao.hibernate;
 
 import org.hibernate.Query;
import ru.example.model.dao.Dao;
 import ru.example.model.entity.Book;
import ru.example.model.entity.Persistent;
 
 import java.util.List;
 
 public class BookHibernateDao extends AbstractHibernateDao<Book> {
 
     @Override
     public void saveOrUpdate(Book persistent) {
         getSession().save(persistent);
     }
 
     @Override
     public void delete(Long id) {
Query query = getSession().createQuery("from Book where id = :id");
         query.setLong("id", id);
     }
 
     @Override
     public void delete(Book persistent) {
        getSession().save(persistent);
     }
 
     @Override
     public Book get(Long id) {
         return (Book) getSession().get(Book.class, id);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Book> getAll() {
         return getSession().createQuery("from Book").list();
     }
 }
