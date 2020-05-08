 package com.belerweb.pentaho.ui.dao;
 
 import java.util.List;
 
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import com.belerweb.pentaho.ui.bean.Option;
 
 @Repository
 @SuppressWarnings("unchecked")
 public class OptionDao {
 
   @Autowired
   private SessionFactory sessionFactory;
 
   public void save(Option option) {
    sessionFactory.getCurrentSession().saveOrUpdate(option);
   }
 
   public List<Option> listOption() {
     return sessionFactory.getCurrentSession().createCriteria(Option.class).list();
   }
 
 }
