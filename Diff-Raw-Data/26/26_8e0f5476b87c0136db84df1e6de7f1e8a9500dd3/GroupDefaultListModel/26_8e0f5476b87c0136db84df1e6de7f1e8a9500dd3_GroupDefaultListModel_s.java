 package net.bounceme.dur.usenet.controller;
 
 import java.util.List;
 import java.util.logging.Logger;
 import javax.mail.Folder;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.swing.DefaultListModel;
 import net.bounceme.dur.usenet.model.Newsgroups;
 import net.bounceme.dur.usenet.model.Usenet;
 
 public class GroupDefaultListModel extends DefaultListModel {
 
     private static final Logger LOG = Logger.getLogger(GroupDefaultListModel.class.getName());
     private Usenet usenet = Usenet.INSTANCE;
     private EntityManagerFactory emf;
     private EntityManager em;
 
     public GroupDefaultListModel() {
         loadFolders();
     }
 
     @SuppressWarnings("unchecked")
     private void loadFolders() {
         LOG.fine("trying..");
         List<Folder> usenetFolderList = usenet.getFolders();
         emf = Persistence.createEntityManagerFactory("USENETPU");
         em = emf.createEntityManager();
         for (Folder folder : usenetFolderList) {
             this.addElement(folder);
             persist(folder);
         }
         em.close();
         LOG.fine(this.toString());
     }
 
     private void persist(Folder folder) {
         Newsgroups newNewsgroup = new Newsgroups(folder);
         LOG.info("insert to database.." + newNewsgroup);


         em.getTransaction().begin();
         em.persist(newNewsgroup);
         em.getTransaction().commit();
     }
    /*
     * public List<Article> getArticles(String lastName) { String queryString =
     * "SELECT a FROM Articles a "; Query query = em.createQuery(queryString);
     * query.setParameter("lastName", StringUtils.lowerCase(lastName)); return
     * query.getResultList();
}
     */
 }
