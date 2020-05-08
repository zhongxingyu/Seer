 package com.wgcat.cheatsheet.dao;
 
 import java.util.List;
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
        List<Theme> themes = getHibernateTemplate().
                find("from Theme themeEntry left join fetch themeEntry.topics order by themeEntry.id");
         //List<Theme> themes = getHibernateTemplate().
        //        find("from Theme themeEntry  order by themeEntry.id");
         return themes;
     }
 
     @Override
     public void removeTheme(Theme theme) {
         getHibernateTemplate().delete(theme);
 
     }
 
 }
