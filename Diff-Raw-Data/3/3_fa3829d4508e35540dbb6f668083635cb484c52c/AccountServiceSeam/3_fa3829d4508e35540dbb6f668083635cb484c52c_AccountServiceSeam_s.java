 package de.objectcode.time4u.server.ejb.seam.impl;
 
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.ejb.Local;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.jboss.annotation.ejb.LocalBinding;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.AutoCreate;
 import org.jboss.seam.annotations.Factory;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.annotations.datamodel.DataModel;
 import org.jboss.seam.annotations.security.Restrict;
 import org.jboss.seam.security.Identity;
 
 import de.objectcode.time4u.server.api.data.EntityType;
 import de.objectcode.time4u.server.ejb.seam.api.IAccountServiceLocal;
 import de.objectcode.time4u.server.entities.PersonEntity;
 import de.objectcode.time4u.server.entities.account.UserAccountEntity;
 import de.objectcode.time4u.server.entities.revision.ILocalIdGenerator;
 import de.objectcode.time4u.server.entities.revision.IRevisionGenerator;
 import de.objectcode.time4u.server.entities.revision.IRevisionLock;
 
 @Stateless
 @Local(IAccountServiceLocal.class)
 @LocalBinding(jndiBinding = "time4u-server/seam/AccountServiceSeam/local")
 @Name("AccountService")
 @AutoCreate
 @Scope(ScopeType.CONVERSATION)
 public class AccountServiceSeam implements IAccountServiceLocal
 {
   @PersistenceContext(unitName = "time4u")
   private EntityManager m_manager;
 
   @EJB
   private IRevisionGenerator m_revisionGenerator;
 
   @EJB
   private ILocalIdGenerator m_idGenerator;
 
   @In("org.jboss.seam.security.identity")
   Identity m_identity;
 
   @DataModel("admin.accountList")
   List<UserAccountEntity> m_userAccounts;
 
   @SuppressWarnings("unchecked")
   @Restrict("#{s:hasRole('admin')}")
   @Factory("admin.accountList")
   public void initUserAccounts()
   {
     final Query query = m_manager.createQuery("from " + UserAccountEntity.class.getName() + " a");
 
     m_userAccounts = query.getResultList();
   }
 
   @Restrict("#{s:hasRole('user')}")
   public void changePassword(final String hashedPassword)
   {
     final UserAccountEntity userAccount = m_manager.find(UserAccountEntity.class, m_identity.getPrincipal().getName());
 
     userAccount.setHashedPassword(hashedPassword);
 
     m_manager.flush();
   }
 
   @Restrict("#{s:hasRole('admin')}")
   public void changePassword(final String userId, final String hashedPassword)
   {
     final UserAccountEntity userAccount = m_manager.find(UserAccountEntity.class, userId);
 
     userAccount.setHashedPassword(hashedPassword);
 
     m_manager.flush();
   }
 
   @Restrict("#{s:hasRole('admin')}")
   public void updatePerson(final String userId, final String givenName, final String surname, final String email)
   {
     final UserAccountEntity userAccount = m_manager.find(UserAccountEntity.class, userId);
 
     final PersonEntity person = userAccount.getPerson();
 
     person.setGivenName(givenName);
     person.setSurname(surname);
     person.setEmail(email);
 
     m_manager.flush();
 
     initUserAccounts();
   }
 
   @Restrict("#{s:hasRole('admin')}")
   public void createAccount(final String userId, final String hashedPassword, final String givenName,
       final String surname, final String email)
   {
     final IRevisionLock revisionLock = m_revisionGenerator.getNextRevision(EntityType.PERSON, null);
     final PersonEntity person = new PersonEntity(m_idGenerator.generateLocalId(EntityType.PERSON), revisionLock
         .getLatestRevision(), m_idGenerator.getClientId());
     person.setGivenName(givenName);
     person.setSurname(surname);
     person.setEmail(email);
 
     m_manager.persist(person);
 
     final UserAccountEntity userAccount = new UserAccountEntity(userId, hashedPassword, person);
 
     m_manager.persist(userAccount);
 
     m_manager.flush();
 
     initUserAccounts();
   }
 
   @Restrict("#{s:hasRole('admin')}")
   public void deleteAccount(final String userId)
   {
     final UserAccountEntity userAccount = m_manager.find(UserAccountEntity.class, userId);
 
     m_manager.remove(userAccount);
     m_manager.flush();
 
     initUserAccounts();
   }
 }
