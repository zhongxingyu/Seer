 package org.makumba.parade.init;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 
 import net.contentobjects.jnotify.JNotify;
 import net.contentobjects.jnotify.JNotifyException;
 import net.contentobjects.jnotify.JNotifyListener;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.tool.hbm2ddl.SchemaUpdate;
 import org.makumba.parade.model.Parade;
 
 import freemarker.template.DefaultObjectWrapper;
 
 
 public class InitServlet extends HttpServlet implements Runnable {
 
     private static final long serialVersionUID = 1L;
 
     static Logger logger = Logger.getLogger(InitServlet.class.getName());
 
     private static Configuration cfg;
 
     private static SessionFactory sessionFactory = null;
 
     private static Long one = new Long(1);
 
     private Session session = null;
 
     private Parade p = null;
     
     private static freemarker.template.Configuration freemarkerCfg;
 
     {
         /* Initializing Hibernate */
         try {
 
             cfg = new Configuration().configure();
             cfg.addResource("org/makumba/parade/model/AbstractFileData.hbm.xml");
             cfg.addResource("org/makumba/parade/model/Parade.hbm.xml");
             cfg.addResource("org/makumba/parade/model/Row.hbm.xml");
             cfg.addResource("org/makumba/parade/model/AbstractRowData.hbm.xml");
             cfg.addResource("org/makumba/parade/model/File.hbm.xml");
             cfg.addResource("org/makumba/parade/model/Log.hbm.xml");
 
             sessionFactory = cfg.buildSessionFactory();
 
             SchemaUpdate schemaUpdate = new SchemaUpdate(cfg);
             schemaUpdate.execute(true, true);
 
         } catch (Throwable t) {
             logger.error(t);
             t.printStackTrace();
         }
         
         /* Initalising Freemarker */
         try {
             
             freemarkerCfg = new freemarker.template.Configuration();
             
            String templatesPath = new java.io.File(ParadeProperties.getClassesPath() + "/org/makumba/parade/view/templates").getPath();
             
             freemarkerCfg.setDirectoryForTemplateLoading(new File(templatesPath));
 
             freemarkerCfg.setObjectWrapper(new DefaultObjectWrapper());
 
         } catch (Throwable t) {
             logger.error(t);
             t.printStackTrace();
         }
         
     }
 
     public void init(ServletConfig conf) throws ServletException {
         new Thread(this).start();
     }
 
     public void run() {
         try {
             Thread.sleep(3000);
         } catch (InterruptedException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         System.out.println("init: Starting Parade initialization at " + new java.util.Date());
         long start = System.currentTimeMillis();
         System.out.flush();
 
         /* Getting Parade, creating one if none found */
 
         session = sessionFactory.openSession();
 
         Transaction tx = session.beginTransaction();
 
         p = (Parade) session.get(Parade.class, new Long(1));
         if (p == null) {
             p = new Parade();
             p.setId(one);
 
             session.save(p);
         }
 
         p.refresh();
         
         tx.commit();
 
         session.close();
         
         System.out.println("INFO: Launching ParaDe finished at " + new java.util.Date());
         long end = System.currentTimeMillis();
 
         long refresh = end - start;
         System.out.println("INFO: Initialisation took " + refresh + " ms");
         
         
 
         
 
     }
 
     public static SessionFactory getSessionFactory() {
         return sessionFactory;
     }
 
     public static freemarker.template.Configuration getFreemarkerCfg() {
         return freemarkerCfg;
     }
 
 }
