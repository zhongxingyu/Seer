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
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 
 import de.cismet.cids.jpa.backend.core.PersistenceProvider;
 import de.cismet.cids.jpa.backend.service.Backend;
 import de.cismet.cids.jpa.backend.service.UserService;
 import de.cismet.cids.jpa.entity.configattr.ConfigAttrEntry;
 import de.cismet.cids.jpa.entity.permission.AbstractPermission;
 import de.cismet.cids.jpa.entity.permission.AttributePermission;
 import de.cismet.cids.jpa.entity.permission.ClassPermission;
 import de.cismet.cids.jpa.entity.permission.NodePermission;
 import de.cismet.cids.jpa.entity.user.User;
 import de.cismet.cids.jpa.entity.user.UserGroup;
 
 import de.cismet.commons.utils.StackUtils;
 
 /**
  * DOCUMENT ME!
  *
  * @author   mscholl
  * @version  1.5
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
     public <T extends AbstractPermission> List<T> getPermissions(final Class<T> permType, final UserGroup ug) {
         final EntityManager em = provider.getEntityManager();
 
         final CriteriaBuilder cb = em.getCriteriaBuilder();
         final CriteriaQuery<T> cq = cb.createQuery(permType);
         final Root<T> root = cq.from(permType);
         cq.where(cb.equal(root.get("userGroup"), ug));
 
         final Query q = em.createQuery(cq);
 
         return q.getResultList();
     }
 
     @Override
     public Integer getLowestUGPrio() {
         final EntityManager em = provider.getEntityManager();
         final TypedQuery<Integer> q = em.createQuery("SELECT MAX(priority) FROM UserGroup", Integer.class); // NOI18N
 
        final Integer prio = q.getSingleResult();

        return (prio == null) ? 1 : (prio + 1);
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
 
         final Query delCfgAttrExempt = em.createQuery("DELETE FROM ConfigAttrExempt cae WHERE cae.usergroup = :ug"); // NOI18N
         delCfgAttrExempt.setParameter("ug", ug);                                                                     // NOI18N
 
         final int delCfgAttrExCount = delCfgAttrExempt.executeUpdate();
         if (LOG.isDebugEnabled()) {
             LOG.debug("deleted '" + delCfgAttrExCount + "' config attr exempt entries for usergroup: " + ug); // NOI18N
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
 
         for (final User u : ug.getUsers()) {
             u.getUserGroups().remove(ug);
             provider.store(u);
         }
 
         ug.getUsers().clear();
 
         provider.delete(ug);
     }
 
     @Override
     public void delete(final User user) {
         // use log.finest if we change to another logging utility
         if (LOG.isTraceEnabled()) {
             LOG.trace("enter " + StackUtils.getMethodName(true, true, new Object[] { user })); // NOI18N
         }
 
         final EntityManager em = provider.getEntityManager();
 
         final Query delCfgAttr = em.createQuery("DELETE FROM ConfigAttrEntry cae WHERE cae.user = :user"); // NOI18N
         delCfgAttr.setParameter("user", user);                                                             // NOI18N
 
         final int delCfgAttrCount = delCfgAttr.executeUpdate();
         if (LOG.isDebugEnabled()) {
             LOG.debug("deleted '" + delCfgAttrCount + "' config attr entries for user: " + user); // NOI18N
         }
 
         final Query delCfgAttrExempt = em.createQuery("DELETE FROM ConfigAttrExempt cae WHERE cae.user = :user"); // NOI18N
         delCfgAttrExempt.setParameter("user", user);                                                              // NOI18N
 
         final int delCfgAttrExCount = delCfgAttrExempt.executeUpdate();
         if (LOG.isDebugEnabled()) {
             LOG.debug("deleted '" + delCfgAttrExCount + "' config attr exempt entries for user: " + user); // NOI18N
         }
 
         for (final UserGroup ug : user.getUserGroups()) {
             ug.getUsers().remove(user);
             provider.store(ug);
         }
 
         user.getUserGroups().clear();
 
         provider.delete(user);
 
         if (LOG.isTraceEnabled()) {
             LOG.trace("leave " + StackUtils.getMethodName(true, true, new Object[] { user })); // NOI18N
         }
     }
 
     @Override
     public void removeMembership(final User user, final UserGroup ug) {
         if (LOG.isTraceEnabled()) {
             LOG.trace("enter " + StackUtils.getMethodName(true, true, new Object[] { user })); // NOI18N
         }
 
         if ((user == null) || (ug == null)) {
             throw new IllegalArgumentException("user or usergroup may not be null: [user=" + user + "|ug=" + ug + "]"); // NOI18N
         }
 
         user.getUserGroups().remove(ug);
         ug.getUsers().remove(user);
 
         final EntityManager em = provider.getEntityManager();
 
         final Query delCfgAttr = em.createQuery(
                 "DELETE FROM ConfigAttrEntry cae WHERE cae.user = :user AND cae.usergroup = :ug"); // NOI18N
         delCfgAttr.setParameter("user", user);                                                     // NOI18N
         delCfgAttr.setParameter("ug", ug);                                                         // NOI18N
 
         final int delCfgAttrCount = delCfgAttr.executeUpdate();
         if (LOG.isDebugEnabled()) {
             LOG.debug("deleted '" + delCfgAttrCount + "' config attr entries: [user=" + user + "|usergroup=" // NOI18N
                         + ug
                         + "]");         // NOI18N
         }
 
         final Query delCfgAttrExempt = em.createQuery(
                 "DELETE FROM ConfigAttrExempt cae WHERE cae.user = :user AND cae.usergroup = :ug"); // NOI18N
         delCfgAttrExempt.setParameter("user", user);                                                // NOI18N
         delCfgAttrExempt.setParameter("ug", ug);                                                    // NOI18N
 
         final int delCfgAttrExCount = delCfgAttrExempt.executeUpdate();
         if (LOG.isDebugEnabled()) {
             LOG.debug("deleted '" + delCfgAttrExCount + "' config attr exempt entries: [user=" + user // NOI18N
                         + "|usergroup=" // NOI18N
                         + ug + "]");    // NOI18N
         }
 
         provider.store(user);
         provider.store(ug);
 
         if (LOG.isTraceEnabled()) {
             LOG.trace("leave " + StackUtils.getMethodName(true, true, new Object[] { user })); // NOI18N
         }
     }
 
     @Override
     public UserGroup copy(final UserGroup original) {
         return copy(original, null);
     }
 
     @Override
     public UserGroup copy(final UserGroup original, final UserGroup newUg) {
         if (original == null) {
             throw new IllegalArgumentException("cannot copy user without original"); // NOI18N
         }
 
         UserGroup newGroup;
         if (newUg == null) {
             newGroup = new UserGroup();
             newGroup.setDescription(original.getDescription());
             newGroup.setDomain(original.getDomain());
             newGroup.setName(original.getName());
             newGroup.setUsers(original.getUsers());
         } else {
             newGroup = newUg;
         }
 
         newGroup.setPriority(getLowestUGPrio());
 
         newGroup = provider.store(newGroup);
 
         for (final User u : newGroup.getUsers()) {
             u.getUserGroups().add(newGroup);
             provider.store(u);
         }
 
         final Backend backend = provider.getBackend();
 
         final List<ConfigAttrEntry> caes = backend.getEntries(
                 original.getDomain(),
                 original,
                 null,
                 provider.getRuntimeProperties().getProperty("serverName"), // NOI18N
                 false);
         for (final ConfigAttrEntry cae : caes) {
             final ConfigAttrEntry clone = new ConfigAttrEntry();
             clone.setDomain(cae.getDomain());
             clone.setKey(cae.getKey());
             clone.setType(cae.getType());
             clone.setUser(null);
             clone.setUsergroup(newGroup);
             clone.setValue(cae.getValue());
 
             backend.storeEntry(clone);
         }
 
         final List<ClassPermission> cperms = getPermissions(ClassPermission.class, original);
         for (final ClassPermission cperm : cperms) {
             final ClassPermission perm = new ClassPermission();
             perm.setCidsClass(cperm.getCidsClass());
             perm.setPermission(cperm.getPermission());
             perm.setUserGroup(newGroup);
 
             backend.store(perm);
         }
 
         final List<AttributePermission> aperms = getPermissions(AttributePermission.class, original);
         for (final AttributePermission aperm : aperms) {
             final AttributePermission perm = new AttributePermission();
             perm.setAttribute(aperm.getAttribute());
             perm.setPermission(aperm.getPermission());
             perm.setUserGroup(newGroup);
 
             backend.store(perm);
         }
 
         final List<NodePermission> nperms = getPermissions(NodePermission.class, original);
         for (final NodePermission nperm : nperms) {
             final NodePermission perm = new NodePermission();
             perm.setNode(nperm.getNode());
             perm.setPermission(nperm.getPermission());
             perm.setUserGroup(newGroup);
 
             backend.store(perm);
         }
 
         return newGroup;
     }
 }
