 package org.makumba.parade.init;
 
 import java.io.PrintWriter;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServlet;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.tool.hbm2ddl.SchemaUpdate;
 import org.makumba.parade.model.Parade;
 import org.makumba.parade.view.managers.ViewManager;
 
 
public class InitServlet extends HttpServlet  implements Runnable {
 	
 	private static final long serialVersionUID = 1L;
 
 	static Logger logger = Logger.getLogger(InitServlet.class.getName());
 	
     private static Configuration cfg;
     
     private static SessionFactory sessionFactory = null;
     
     private static Long one = new Long(1);
     
     private Session session = null;
     
     private Parade p = null;
     
     {
     	/* Initializing Hibernate */
     	try {
         	
             cfg = new Configuration().configure();
             cfg.addResource("org/makumba/parade/model/AbstractFileData.hbm.xml");
             cfg.addResource("org/makumba/parade/model/Parade.hbm.xml");
             cfg.addResource("org/makumba/parade/model/Row.hbm.xml");
             cfg.addResource("org/makumba/parade/model/AbstractRowData.hbm.xml");
             cfg.addResource("org/makumba/parade/model/File.hbm.xml");
             
         	sessionFactory = cfg.buildSessionFactory();
             
             SchemaUpdate schemaUpdate = new SchemaUpdate(cfg);
             schemaUpdate.execute(true, true);
             
         } catch (Throwable t) {
             logger.error(t); t.printStackTrace();
         }
     	
     }
         
         public void init(ServletConfig conf) throws ServletException
         {
             new Thread(this).start();
        }

        public void run()
        {
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
             
             
             System.out.println("INFO: Launching ParaDe finished at "+new java.util.Date());
             long end = System.currentTimeMillis();
             
             long refresh = end-start;
             System.out.println("INFO: Initialisation took "+refresh+" ms");
             
         }
         
         public static SessionFactory getSessionFactory() {
         	return sessionFactory;
         }
 
 
 }
