 package net.kurochenko.ispub.author.dao;
 
 import java.util.List;
 import net.kurochenko.ispub.author.form.Author;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author Andrej Kuročenko <kurochenko@gmail.com>
  */
 @Repository
 @Transactional
 public class AuthorDAOImpl implements AuthorDAO {
     
     @Autowired
     private SessionFactory sessionFactory;
 
     @Override
     public void saveAuthor(Author author) {
         sessionFactory.getCurrentSession().saveOrUpdate(author);
     }
 
     @Override
     public void removeAuthor(Integer idAuthor) {
         Author author = getAuthorByID(idAuthor);
         if (null != author) {
             sessionFactory.getCurrentSession().delete(author);
         }
     }
 
     @Override
     public Author getAuthorByID(Integer idAuthor) {
         return (Author) sessionFactory.getCurrentSession().get(Author.class, idAuthor);
     }
 
     @Override
     public List<Author> listAuthor() {
         return sessionFactory.getCurrentSession().createQuery("from " + Author.class.getName()).list();
     }
 
     @Override
     public void updateAuthor(Author author) {
         sessionFactory.getCurrentSession().update(author);
     }
 }
