 package org.bloodtorrent.repository;
 
 import com.yammer.dropwizard.hibernate.AbstractDAO;
 import org.bloodtorrent.dto.SuccessStory;
 import org.hibernate.Query;
 import org.hibernate.SQLQuery;
 import org.hibernate.SessionFactory;
 
 import java.util.List;
 
 public class SuccessStoryRepository extends AbstractDAO<SuccessStory> {
 
     public SuccessStoryRepository(SessionFactory sessionFactory) {
         super(sessionFactory);
     }
 
     /**
      * List up at most 3 success stories for showing on main page.
      * @return
      */
     public List<SuccessStory> list() {
        Query query = currentSession().createQuery("from SuccessStory s where s.showMainPage like 'Y' order by s.createDate desc");
         return list(query);
     }
 }
