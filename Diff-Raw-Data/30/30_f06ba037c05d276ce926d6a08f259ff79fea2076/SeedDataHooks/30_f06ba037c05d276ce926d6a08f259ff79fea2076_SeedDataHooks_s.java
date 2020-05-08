 package com.example.xpSearchLiang.hook;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.List;
 
 import com.example.xpSearchLiang.dao.PostDao;
 import com.example.xpSearchLiang.entity.Post;
 import org.hibernate.jdbc.Work;
 
 import com.britesnow.snow.web.db.hibernate.HibernateDaoHelper;
 import com.britesnow.snow.web.db.hibernate.HibernateSessionInViewHandler;
 import com.britesnow.snow.web.hook.AppPhase;
 import com.britesnow.snow.web.hook.On;
 import com.britesnow.snow.web.hook.annotation.WebApplicationHook;
import com.example.xpSearchLiang.dao.ItemDao;
import com.example.xpSearchLiang.dao.UserDao;
import com.example.xpSearchLiang.entity.Item;
import com.example.xpSearchLiang.entity.User;
 import com.google.inject.Singleton;
 
 @Singleton
 public class SeedDataHooks {
 
     /**
      * This will be called to see the database (for demo only)
      * 
      * @param inView
      *            will be injected by Snow with the Guice binding (needed to open the connection for this thread to use
      *            daoHelper)
      * 
      */
     @WebApplicationHook(phase = AppPhase.INIT)
     public void seedStore(PostDao postDao, HibernateSessionInViewHandler inView) {
         List<Post> posts = XmlReader.readPosts();
 
         inView.openSessionInView();
         for (Post post : posts) {
             postDao.save(post);
         }
         inView.closeSessionInView();
     }
 
     /**
      * Using HSQLDB we need to shutdown the database to be written in the file system.
      * 
      * Note that if you do not shutdown your webapp gracefully, the data won't be written to disk. 
      * 
      * @param inView
      * @param daoHelper
      */
     @WebApplicationHook(phase = AppPhase.SHUTDOWN,on=On.BEFORE)
     public void shutdownDb(HibernateSessionInViewHandler inView, HibernateDaoHelper daoHelper){
         try {
             inView.openSessionInView();
             daoHelper.getSession().doWork(new Work() {
                 public void execute(Connection con) throws SQLException {
                     con.prepareStatement("shutdown compact").execute();
                 }
             });
             inView.closeSessionInView();
         } catch (Throwable e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }        
     }
     
     // --------- Some Seed Data --------- //
     private static String[][] seedUsers    = { { "john", "welcome", "John", "Doe" },
             { "jen", "welcome", "Jennifer", "Donavan" } };
 
     private static String[][] defaultItems = {
             { "Google", "http://google.com", "Search anything and anywhere (Might get some G+ result in the mix)" },
             { "snow", "http://britesnow.com/snow", "Lightweight, highly productive, Java Web Framework" },
             { "brite.js", "http://britesnow.com/brite", "Lightweight, jQuery based MVC, javascript framework" } };
     // --------- Some Seed Data --------- //
             
 
 }
