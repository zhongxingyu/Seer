 package org.makumba.parade.init;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.sql.DriverManager;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 
 import org.hibernate.Hibernate;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.tool.hbm2ddl.SchemaUpdate;
 import org.makumba.HibernateSFManager;
 import org.makumba.aether.Aether;
 import org.makumba.aether.AetherContext;
 import org.makumba.parade.aether.MakumbaContextRelationComputer;
 import org.makumba.parade.aether.ParadeRelationComputer;
 import org.makumba.parade.model.Parade;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.tools.ParadeLogger;
 
 import freemarker.template.DefaultObjectWrapper;
 
 public class InitServlet extends HttpServlet implements Runnable {
 
     private static final long serialVersionUID = 1L;
 
     public static Date startupDate = new Date();
 
     static Logger logger = ParadeLogger.getParadeLogger(InitServlet.class.getName());
 
     private static SessionFactory sessionFactory = null;
 
     private static Aether aether;
 
     private static Map<String, MakumbaContextRelationComputer> rowComputers = new HashMap<String, MakumbaContextRelationComputer>();
 
     private static Long one = new Long(1);
 
     private Session session = null;
 
     private Parade p = null;
 
     private static freemarker.template.Configuration freemarkerCfg;
 
     public static boolean aetherEnabled = ParadeProperties.getParadeProperty("aether.enabled") != null
             && ParadeProperties.getParadeProperty("aether.enabled").equals("true");
 
     static {
         initSessionFactory();
     }
 
     public static boolean isAetherEnabled() {
         return ParadeProperties.getParadeProperty("aether.enabled") != null
                 && ParadeProperties.getParadeProperty("aether.enabled").equals("true");
     }
 
     private static void initSessionFactory() {
         /* Initializing Makumba-driven Hibernate */
         Vector<String> resources = new Vector<String>();
 
         try {
             resources.add("org.makumba.parade.model.ActionLog");
             resources.add("org.makumba.parade.model.Parade");
             resources.add("org.makumba.parade.model.Row");
             resources.add("org.makumba.parade.model.AntTarget");
             resources.add("org.makumba.parade.model.File");
             resources.add("org.makumba.parade.model.Log");
             resources.add("org.makumba.parade.model.Application");
             resources.add("org.makumba.parade.model.User");
 
             if (isAetherEnabled()) {
                 // Aether XMLs
                 // In a standalone Aether those would be in initialised by an own Aether sessionFactory
                 // but that seems a bit too resource-consuming, and we anyway need to query this model
                 resources.add("org.makumba.aether.model.InitialPercolationRule");
                 resources.add("org.makumba.aether.model.PercolationRule");
                 resources.add("org.makumba.aether.model.PercolationStep");
                 resources.add("org.makumba.aether.model.RelationQuery");
                 resources.add("org.makumba.aether.model.MatchedAetherEvent");
                 resources.add("org.makumba.aether.model.ALE");
             }
 
             createDummyDatabaseConnection();
 
             HibernateSFManager.setExternalConfigurationResources(resources);
             SessionFactory sf = HibernateSFManager.getSF();
 
             SchemaUpdate schemaUpdate = new SchemaUpdate(HibernateSFManager.getConfiguredConfiguration());
             schemaUpdate.execute(true, true);
 
             // now it's ready to be available for other classes
             sessionFactory = sf;
 
         } catch (Throwable t) {
             logger.severe("Parade cannot continue due to no database connection.");
             logger.severe("Parade stopping CAUSE: " + t.getMessage());
             t.printStackTrace();
         }
     }
 
     @Override
     public void init(ServletConfig conf) throws ServletException {
         if (sessionFactory != null) {
             new Thread(this).start();
         } else {
             throw new Error("No Database");
         }
     }
 
     public void run() {
         // Joao - FIXME: remove
         try {
             Thread.sleep(3000);
         } catch (InterruptedException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         logger.info("INIT: Starting Parade initialization at " + new java.util.Date());
         long start = System.currentTimeMillis();
         System.out.flush();
 
         /* Getting Parade, creating one if none found */
 
         session = sessionFactory.openSession();
 
         Transaction tx = session.beginTransaction();
 
         p = (Parade) session.get(Parade.class, new Long(1));
         if (p == null) {
             p = new Parade();
             p.setId(one);
             p.hardRefresh();
             session.save(p);
             p.performPostRefreshOperations();
             session.save(p);
         }
         try {
             p.refreshApplicationsCache();
             p.clearCollections();
             session.saveOrUpdate(p);
             p.softRefresh();
             p.addJNotifyListeners();
         } catch (Throwable e) {
             e.printStackTrace();
         }
         Hibernate.initialize(p.getRows());
 
         if (isAetherEnabled()) {
 
             AetherContext ctx = new AetherContext(ParadeRelationComputer.PARADE_DATABASE_NAME, getSessionFactory());
 
             // parade relation computer, generating user to row relations
             ctx.addRelationComputer(new ParadeRelationComputer());
 
             for (Row r : p.getRows().values()) {
 
                 if (r.getRowname().equals("(root)")) {
                     continue;
                 }
 
                 if (r.getHasMakumba() && !r.getModuleRow()) {
                     MakumbaContextRelationComputer c = new MakumbaContextRelationComputer(r);
                     ctx.addRelationComputer(c);
                     rowComputers.put(r.getRowpath(), c);
                 }
             }
             aether = Aether.getAether(ctx);
         }
 
         tx.commit();
 
         session.close();
 
         logger.info("INIT: Launching ParaDe finished at " + new java.util.Date());
         long end = System.currentTimeMillis();
 
         long refresh = end - start;
         logger.info("INIT: Initialisation took " + refresh + " ms");
     }
 
     public static SessionFactory getSessionFactory() {
         return sessionFactory;
     }
 
     public static freemarker.template.Configuration getFreemarkerCfg() {
         return freemarkerCfg;
     }
 
     public static MakumbaContextRelationComputer getContextRelationComputer(String rowPath) {
         return rowComputers.get(rowPath);
     }
 
     public static Aether getAether() {
         return aether;
     }
 
     public static void createDummyDatabaseConnection() throws Throwable {
         // Get configuration from Makumba.conf
         File f = new File((System.getProperty("user.dir") + "/webapp/WEB-INF/classes/")
                 .replace('/', File.separatorChar)
                 + "Makumba.conf");
         // Configuration. = new Configuration(f.toURL(), null);
         String url = "jdbc:mysql://localhost/parade";
         String user = "root";
         String pass = "";
 
         // Try to make connection to Database
         DriverManager.getConnection(url, user, pass);
     }
 
     public static void shutdownTomcat() {
         try {
             Socket s = new Socket("localhost", 5055);
             PrintStream p = new PrintStream(s.getOutputStream());
             p.print("SHUTDOWN");
             p.close();
             s.close();
         } catch (UnknownHostException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         } catch (IOException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
     }
 
 }
