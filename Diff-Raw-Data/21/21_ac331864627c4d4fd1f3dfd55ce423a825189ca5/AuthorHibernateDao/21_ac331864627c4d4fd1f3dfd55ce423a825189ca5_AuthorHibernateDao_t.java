 package ru.example.model.dao.hibernate;
 
 import org.hibernate.Query;
 import ru.example.model.entity.Author;
 
 import java.util.List;
 
 public class AuthorHibernateDao extends AbstractHibernateDao<Author> {
     @Override
     public void saveOrUpdate(Author persistent) {
         getSession().save(persistent);
     }
 
     @Override
     public void delete(Long id) {
         Query query = getSession().createQuery("from Author where id=:id");
         query.setLong("id", id);
        query.executeUpdate();
     }
 
     @Override
     public void delete(Author persistent) {
         getSession().delete(persistent);
     }
 
     @Override
     public Author get(Long id) {
         return (Author) getSession().get(Author.class, id);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Author> getAll() {
         return getSession().createQuery("from Author").list();
     }
 
 
 }
