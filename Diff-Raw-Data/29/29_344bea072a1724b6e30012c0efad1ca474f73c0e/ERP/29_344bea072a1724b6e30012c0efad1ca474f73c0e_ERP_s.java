 package org.wyona.erp;
 
 import org.apache.jackrabbit.core.jndi.RegistryHelper;
 
 import javax.jcr.Repository;
 import javax.jcr.RepositoryException;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import java.util.Hashtable;
 
 import org.apache.log4j.Category;
 
 /**
  *
  */
 public class ERP {
 
     private static Category log = Category.getInstance(ERP.class);
 
    String REPO_NAME = "erp-repo";
 
     Context context;
 
     /**
      *
      */
     public ERP(String repoConfig, String repoHomeDir) {
         try {
             bindRepository(repoConfig, repoHomeDir);
         } catch(Exception e) {
             log.error(e);
         }
     }
 
     /**
      *
      */
     public void addTask(String title, String owner) {
         try {
             Repository repo = getRepository();
            System.out.println("Add Task: " + title + " (" + owner + ")");
         } catch (Exception e) {
             log.error(e);
         }
     }
 
     /**
      *
      */
     private Repository getRepository() throws NamingException {
         return (Repository) context.lookup(REPO_NAME);
     }
 
     /**
      *
      */
     private void bindRepository(String repoConfig, String repoHomeDir) throws NamingException, RepositoryException {
         Hashtable env = new Hashtable();
         env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.jackrabbit.core.jndi.provider.DummyInitialContextFactory");
         env.put(Context.PROVIDER_URL, "localhost");
 
         context = new InitialContext(env);
         RegistryHelper.registerRepository(context, REPO_NAME, repoConfig, repoHomeDir, true);
     }
 }
