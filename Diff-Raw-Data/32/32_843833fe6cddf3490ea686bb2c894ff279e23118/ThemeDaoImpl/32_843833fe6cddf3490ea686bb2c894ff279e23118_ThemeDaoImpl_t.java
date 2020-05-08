 package com.wgcat.cheatsheet.dao;
 
 import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.wgcat.cheatsheet.dbobjects.Theme;
 import com.wgcat.cheatsheet.dbobjects.Topic;
 
 @Transactional(readOnly = true)
 public class ThemeDaoImpl extends HibernateDaoSupport implements ThemeDao {
 
     @Override
     @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
     public void addTheme(Theme theme) {
         getHibernateTemplate().saveOrUpdate(theme);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public List<Theme> listThemes() {
         //List<Theme> themes = getHibernateTemplate().
        //        find("from Theme themeEntry left join fetch themeEntry.topics order by themeEntry.id");
        
        List<Theme> themes = getHibernateTemplate().
                find("from Theme themeEntry left join fetch themeEntry.topics as topics "+
                      "order by themeEntry.id, "+
                      "topics.sort_id");
        
        
        /*SessionFactory sf = getHibernateTemplate().getSessionFactory();
        Session sess = sf.openSession();
        List<Theme> themes = sess.createSQLQuery("select theme.id, theme.title, theme.version, topic.id, " +
                "topic.theme_id, topic.title, topic.sort_id, topic.version, topic.is_delete "+
                "from theme inner join topic on theme.id = " +
                "topic.theme_id order by theme.id, topic.sort_id, topic.id").
                    addEntity("Topic", Topic.class).addJoin("topics", "Theme.topics").list();*/
         return themes;
     }
 
     @Override
     public void removeTheme(Theme theme) {
         getHibernateTemplate().delete(theme);
 
     }
 
 }
