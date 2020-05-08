 package web.dao.impl;
 
 import java.util.List;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.stereotype.Repository;
 import web.dao.CommentDao;
 import web.model.Comment;
 import web.model.Property;
 import web.model.User;
 
 /**
  * @author Romain <ro.foncier@gmail.com>
  */
 
 @Repository
 public class CommentDaoImpl extends AbstractDaoImpl<Comment, Integer> implements CommentDao {
 
     protected CommentDaoImpl() {
         super(Comment.class);
     }
     
     @Override
     public Boolean saveComment(Comment comment) {
         if (comment == null) {
             return false;
         }
         saveOrUpdate(comment);
         return true;
     }
     
     @Override
     public Comment findByProperty(Property property) {
        return findByCriteria(Restrictions.eq("property", property)).get(0);
     }
     
     @Override
     public List<Comment> findByUser(User user) {
         return findByCriteria(Restrictions.eq("creator", user));
     }
 }
