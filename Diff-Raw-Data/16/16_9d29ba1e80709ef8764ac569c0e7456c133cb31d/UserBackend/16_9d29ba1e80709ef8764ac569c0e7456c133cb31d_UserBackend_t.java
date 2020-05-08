 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.jpa.backend.service.impl;
 
 import org.apache.log4j.Logger;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 
 import de.cismet.cids.jpa.backend.core.PersistenceProvider;
 import de.cismet.cids.jpa.backend.service.UserService;
 import de.cismet.cids.jpa.entity.permission.ClassPermission;
 import de.cismet.cids.jpa.entity.user.User;
 import de.cismet.cids.jpa.entity.user.UserGroup;
 
 /**
  * DOCUMENT ME!
  *
  * @author   mscholl
  * @version  $Revision$, $Date$
  */
 public class UserBackend implements UserService {
 
     //~ Static fields/initializers ---------------------------------------------
 
     /** LOGGER. */
     private static final transient Logger LOG = Logger.getLogger(UserBackend.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient PersistenceProvider provider;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new UserBackend object.
      *
      * @param  provider  DOCUMENT ME!
      */
     public UserBackend(final PersistenceProvider provider) {
         this.provider = provider;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public User getUser(final String userName, final String password) {
         final EntityManager em = provider.getEntityManager();
         final Query q = em.createQuery("FROM User WHERE login_name = :userName AND password =:password"); // NOI18N
         q.setParameter("userName", userName);                                                             // NOI18N
         q.setParameter("password", password);                                                             // NOI18N
         return (User)q.getSingleResult();
     }
 
     @Override
     public List<ClassPermission> getClassPermissions(final UserGroup ug) {
         final EntityManager em = provider.getEntityManager();
         final Query q = em.createQuery("FROM ClassPermission WHERE userGroup = :ug"); // NOI18N
         q.setParameter("ug", ug);                                                     // NOI18N
 
         return q.getResultList();
     }
 
     @Override
     public Integer getLowestUGPrio() {
         final EntityManager em = provider.getEntityManager();
         final TypedQuery<Integer> q = em.createQuery("SELECT MAX(priority) FROM UserGroup", Integer.class); // NOI18N
 
         return q.getSingleResult() + 1;
     }
 
     @Override
     public void delete(final UserGroup ug) {
         final EntityManager em = provider.getEntityManager();
 
         final Query delCfgAttr = em.createQuery(
                 "DELETE FROM ConfigAttrEntry cae WHERE cae.domain = :dom AND cae.usergroup = :ug"); // NOI18N
         delCfgAttr.setParameter("dom", ug.getDomain());                                             // NOI18N
         delCfgAttr.setParameter("ug", ug);                                                          // NOI18N
 
         final int delCfgAttrCount = delCfgAttr.executeUpdate();
         if (LOG.isDebugEnabled()) {
             LOG.debug("deleted '" + delCfgAttrCount + "' config attr entries for usergroup: " + ug); // NOI18N
         }
 
         final Query delCPerm = em.createQuery("DELETE FROM ClassPermission cperm WHERE cperm.userGroup = :ug"); // NOI18N
         delCPerm.setParameter("ug", ug);                                                                        // NOI18N
 
         final int delCPermCount = delCPerm.executeUpdate();
         if (LOG.isDebugEnabled()) {
             LOG.debug("deleted '" + delCPermCount + "' class permissions for usergroup: " + ug); // NOI18N
         }
 
        final Query delNPerm = em.createQuery("DELETE FROM NodePermission nperm WHERE nperm.userGroup = :ug"); // NOI18N
        delNPerm.setParameter("ug", ug);                                                                       // NOI18N

        final int delNPermCount = delNPerm.executeUpdate();
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleted '" + delNPermCount + "' node permissions for usergroup: " + ug); // NOI18N
        }

        final Query delAPerm = em.createQuery("DELETE FROM AttributePermission aperm WHERE aperm.userGroup = :ug"); // NOI18N
        delAPerm.setParameter("ug", ug);                                                                            // NOI18N

        final int delAPermCount = delAPerm.executeUpdate();
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleted '" + delAPermCount + "' attribute permissions for usergroup: " + ug); // NOI18N
        }

         provider.delete(ug);
     }
 }
