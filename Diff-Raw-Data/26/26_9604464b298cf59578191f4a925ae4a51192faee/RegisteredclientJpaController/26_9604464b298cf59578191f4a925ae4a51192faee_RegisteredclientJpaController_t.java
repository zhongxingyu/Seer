 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Flipmotor.model;
 
 import Flipmotor.Entities.Registeredclient;
 import Flipmotor.model.exceptions.IllegalOrphanException;
 import Flipmotor.model.exceptions.NonexistentEntityException;
 import Flipmotor.model.exceptions.RollbackFailureException;
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Query;
 import javax.persistence.EntityNotFoundException;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 import Flipmotor.Entities.Conciliation;
 import java.util.ArrayList;
 import java.util.Collection;
 import Flipmotor.Entities.Fav;
 import Flipmotor.Entities.Vehicleadvert;
 import Flipmotor.Entities.Businessadvert;
import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;
 import javax.transaction.UserTransaction;
 
 /**
  *
  * @author root
  */
 public class RegisteredclientJpaController implements Serializable {
 
     public RegisteredclientJpaController(UserTransaction utx, EntityManagerFactory emf) {
         this.utx = utx;
         this.emf = emf;
     }
    @Resource private UserTransaction utx;
    @PersistenceUnit private EntityManagerFactory emf = Persistence.createEntityManagerFactory("ProjectPU");  
 
     public RegisteredclientJpaController() {
         
     }
 
     public EntityManager getEntityManager() {
         return emf.createEntityManager();
     }
 
     public void create(Registeredclient registeredclient) throws RollbackFailureException, Exception {
         if (registeredclient.getConciliationCollection() == null) {
             registeredclient.setConciliationCollection(new ArrayList<Conciliation>());
         }
         if (registeredclient.getFavCollection() == null) {
             registeredclient.setFavCollection(new ArrayList<Fav>());
         }
         if (registeredclient.getVehicleadvertCollection() == null) {
             registeredclient.setVehicleadvertCollection(new ArrayList<Vehicleadvert>());
         }
         if (registeredclient.getBusinessadvertCollection() == null) {
             registeredclient.setBusinessadvertCollection(new ArrayList<Businessadvert>());
         }
         EntityManager em = null;
        Context ctx = new InitialContext();
        this.utx = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
         try {
             utx.begin();
             em = getEntityManager();
             Collection<Conciliation> attachedConciliationCollection = new ArrayList<Conciliation>();
             for (Conciliation conciliationCollectionConciliationToAttach : registeredclient.getConciliationCollection()) {
                 conciliationCollectionConciliationToAttach = em.getReference(conciliationCollectionConciliationToAttach.getClass(), conciliationCollectionConciliationToAttach.getCode());
                 attachedConciliationCollection.add(conciliationCollectionConciliationToAttach);
             }
             registeredclient.setConciliationCollection(attachedConciliationCollection);
             Collection<Fav> attachedFavCollection = new ArrayList<Fav>();
             for (Fav favCollectionFavToAttach : registeredclient.getFavCollection()) {
                 favCollectionFavToAttach = em.getReference(favCollectionFavToAttach.getClass(), favCollectionFavToAttach.getId());
                 attachedFavCollection.add(favCollectionFavToAttach);
             }
             registeredclient.setFavCollection(attachedFavCollection);
             Collection<Vehicleadvert> attachedVehicleadvertCollection = new ArrayList<Vehicleadvert>();
             for (Vehicleadvert vehicleadvertCollectionVehicleadvertToAttach : registeredclient.getVehicleadvertCollection()) {
                 vehicleadvertCollectionVehicleadvertToAttach = em.getReference(vehicleadvertCollectionVehicleadvertToAttach.getClass(), vehicleadvertCollectionVehicleadvertToAttach.getCode());
                 attachedVehicleadvertCollection.add(vehicleadvertCollectionVehicleadvertToAttach);
             }
             registeredclient.setVehicleadvertCollection(attachedVehicleadvertCollection);
             Collection<Businessadvert> attachedBusinessadvertCollection = new ArrayList<Businessadvert>();
             for (Businessadvert businessadvertCollectionBusinessadvertToAttach : registeredclient.getBusinessadvertCollection()) {
                 businessadvertCollectionBusinessadvertToAttach = em.getReference(businessadvertCollectionBusinessadvertToAttach.getClass(), businessadvertCollectionBusinessadvertToAttach.getCode());
                 attachedBusinessadvertCollection.add(businessadvertCollectionBusinessadvertToAttach);
             }
             registeredclient.setBusinessadvertCollection(attachedBusinessadvertCollection);
             em.persist(registeredclient);
             for (Conciliation conciliationCollectionConciliation : registeredclient.getConciliationCollection()) {
                 Registeredclient oldUserIDOfConciliationCollectionConciliation = conciliationCollectionConciliation.getUserID();
                 conciliationCollectionConciliation.setUserID(registeredclient);
                 conciliationCollectionConciliation = em.merge(conciliationCollectionConciliation);
                 if (oldUserIDOfConciliationCollectionConciliation != null) {
                     oldUserIDOfConciliationCollectionConciliation.getConciliationCollection().remove(conciliationCollectionConciliation);
                     oldUserIDOfConciliationCollectionConciliation = em.merge(oldUserIDOfConciliationCollectionConciliation);
                 }
             }
             for (Fav favCollectionFav : registeredclient.getFavCollection()) {
                 Registeredclient oldClientIDOfFavCollectionFav = favCollectionFav.getClientID();
                 favCollectionFav.setClientID(registeredclient);
                 favCollectionFav = em.merge(favCollectionFav);
                 if (oldClientIDOfFavCollectionFav != null) {
                     oldClientIDOfFavCollectionFav.getFavCollection().remove(favCollectionFav);
                     oldClientIDOfFavCollectionFav = em.merge(oldClientIDOfFavCollectionFav);
                 }
             }
             for (Vehicleadvert vehicleadvertCollectionVehicleadvert : registeredclient.getVehicleadvertCollection()) {
                 Registeredclient oldClientIDOfVehicleadvertCollectionVehicleadvert = vehicleadvertCollectionVehicleadvert.getClientID();
                 vehicleadvertCollectionVehicleadvert.setClientID(registeredclient);
                 vehicleadvertCollectionVehicleadvert = em.merge(vehicleadvertCollectionVehicleadvert);
                 if (oldClientIDOfVehicleadvertCollectionVehicleadvert != null) {
                     oldClientIDOfVehicleadvertCollectionVehicleadvert.getVehicleadvertCollection().remove(vehicleadvertCollectionVehicleadvert);
                     oldClientIDOfVehicleadvertCollectionVehicleadvert = em.merge(oldClientIDOfVehicleadvertCollectionVehicleadvert);
                 }
             }
             for (Businessadvert businessadvertCollectionBusinessadvert : registeredclient.getBusinessadvertCollection()) {
                 Registeredclient oldClientIDOfBusinessadvertCollectionBusinessadvert = businessadvertCollectionBusinessadvert.getClientID();
                 businessadvertCollectionBusinessadvert.setClientID(registeredclient);
                 businessadvertCollectionBusinessadvert = em.merge(businessadvertCollectionBusinessadvert);
                 if (oldClientIDOfBusinessadvertCollectionBusinessadvert != null) {
                     oldClientIDOfBusinessadvertCollectionBusinessadvert.getBusinessadvertCollection().remove(businessadvertCollectionBusinessadvert);
                     oldClientIDOfBusinessadvertCollectionBusinessadvert = em.merge(oldClientIDOfBusinessadvertCollectionBusinessadvert);
                 }
             }
             utx.commit();
         } catch (Exception ex) {
             try {
                 utx.rollback();
             } catch (Exception re) {
                 throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
             }
             throw ex;
         } finally {
             if (em != null) {
                 em.close();
             }
         }
     }
 
     public void edit(Registeredclient registeredclient) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
         EntityManager em = null;
         try {
             utx.begin();
             em = getEntityManager();
             Registeredclient persistentRegisteredclient = em.find(Registeredclient.class, registeredclient.getClientID());
             Collection<Conciliation> conciliationCollectionOld = persistentRegisteredclient.getConciliationCollection();
             Collection<Conciliation> conciliationCollectionNew = registeredclient.getConciliationCollection();
             Collection<Fav> favCollectionOld = persistentRegisteredclient.getFavCollection();
             Collection<Fav> favCollectionNew = registeredclient.getFavCollection();
             Collection<Vehicleadvert> vehicleadvertCollectionOld = persistentRegisteredclient.getVehicleadvertCollection();
             Collection<Vehicleadvert> vehicleadvertCollectionNew = registeredclient.getVehicleadvertCollection();
             Collection<Businessadvert> businessadvertCollectionOld = persistentRegisteredclient.getBusinessadvertCollection();
             Collection<Businessadvert> businessadvertCollectionNew = registeredclient.getBusinessadvertCollection();
             List<String> illegalOrphanMessages = null;
             for (Conciliation conciliationCollectionOldConciliation : conciliationCollectionOld) {
                 if (!conciliationCollectionNew.contains(conciliationCollectionOldConciliation)) {
                     if (illegalOrphanMessages == null) {
                         illegalOrphanMessages = new ArrayList<String>();
                     }
                     illegalOrphanMessages.add("You must retain Conciliation " + conciliationCollectionOldConciliation + " since its userID field is not nullable.");
                 }
             }
             for (Fav favCollectionOldFav : favCollectionOld) {
                 if (!favCollectionNew.contains(favCollectionOldFav)) {
                     if (illegalOrphanMessages == null) {
                         illegalOrphanMessages = new ArrayList<String>();
                     }
                     illegalOrphanMessages.add("You must retain Fav " + favCollectionOldFav + " since its clientID field is not nullable.");
                 }
             }
             for (Vehicleadvert vehicleadvertCollectionOldVehicleadvert : vehicleadvertCollectionOld) {
                 if (!vehicleadvertCollectionNew.contains(vehicleadvertCollectionOldVehicleadvert)) {
                     if (illegalOrphanMessages == null) {
                         illegalOrphanMessages = new ArrayList<String>();
                     }
                     illegalOrphanMessages.add("You must retain Vehicleadvert " + vehicleadvertCollectionOldVehicleadvert + " since its clientID field is not nullable.");
                 }
             }
             for (Businessadvert businessadvertCollectionOldBusinessadvert : businessadvertCollectionOld) {
                 if (!businessadvertCollectionNew.contains(businessadvertCollectionOldBusinessadvert)) {
                     if (illegalOrphanMessages == null) {
                         illegalOrphanMessages = new ArrayList<String>();
                     }
                     illegalOrphanMessages.add("You must retain Businessadvert " + businessadvertCollectionOldBusinessadvert + " since its clientID field is not nullable.");
                 }
             }
             if (illegalOrphanMessages != null) {
                 throw new IllegalOrphanException(illegalOrphanMessages);
             }
             Collection<Conciliation> attachedConciliationCollectionNew = new ArrayList<Conciliation>();
             for (Conciliation conciliationCollectionNewConciliationToAttach : conciliationCollectionNew) {
                 conciliationCollectionNewConciliationToAttach = em.getReference(conciliationCollectionNewConciliationToAttach.getClass(), conciliationCollectionNewConciliationToAttach.getCode());
                 attachedConciliationCollectionNew.add(conciliationCollectionNewConciliationToAttach);
             }
             conciliationCollectionNew = attachedConciliationCollectionNew;
             registeredclient.setConciliationCollection(conciliationCollectionNew);
             Collection<Fav> attachedFavCollectionNew = new ArrayList<Fav>();
             for (Fav favCollectionNewFavToAttach : favCollectionNew) {
                 favCollectionNewFavToAttach = em.getReference(favCollectionNewFavToAttach.getClass(), favCollectionNewFavToAttach.getId());
                 attachedFavCollectionNew.add(favCollectionNewFavToAttach);
             }
             favCollectionNew = attachedFavCollectionNew;
             registeredclient.setFavCollection(favCollectionNew);
             Collection<Vehicleadvert> attachedVehicleadvertCollectionNew = new ArrayList<Vehicleadvert>();
             for (Vehicleadvert vehicleadvertCollectionNewVehicleadvertToAttach : vehicleadvertCollectionNew) {
                 vehicleadvertCollectionNewVehicleadvertToAttach = em.getReference(vehicleadvertCollectionNewVehicleadvertToAttach.getClass(), vehicleadvertCollectionNewVehicleadvertToAttach.getCode());
                 attachedVehicleadvertCollectionNew.add(vehicleadvertCollectionNewVehicleadvertToAttach);
             }
             vehicleadvertCollectionNew = attachedVehicleadvertCollectionNew;
             registeredclient.setVehicleadvertCollection(vehicleadvertCollectionNew);
             Collection<Businessadvert> attachedBusinessadvertCollectionNew = new ArrayList<Businessadvert>();
             for (Businessadvert businessadvertCollectionNewBusinessadvertToAttach : businessadvertCollectionNew) {
                 businessadvertCollectionNewBusinessadvertToAttach = em.getReference(businessadvertCollectionNewBusinessadvertToAttach.getClass(), businessadvertCollectionNewBusinessadvertToAttach.getCode());
                 attachedBusinessadvertCollectionNew.add(businessadvertCollectionNewBusinessadvertToAttach);
             }
             businessadvertCollectionNew = attachedBusinessadvertCollectionNew;
             registeredclient.setBusinessadvertCollection(businessadvertCollectionNew);
             registeredclient = em.merge(registeredclient);
             for (Conciliation conciliationCollectionNewConciliation : conciliationCollectionNew) {
                 if (!conciliationCollectionOld.contains(conciliationCollectionNewConciliation)) {
                     Registeredclient oldUserIDOfConciliationCollectionNewConciliation = conciliationCollectionNewConciliation.getUserID();
                     conciliationCollectionNewConciliation.setUserID(registeredclient);
                     conciliationCollectionNewConciliation = em.merge(conciliationCollectionNewConciliation);
                     if (oldUserIDOfConciliationCollectionNewConciliation != null && !oldUserIDOfConciliationCollectionNewConciliation.equals(registeredclient)) {
                         oldUserIDOfConciliationCollectionNewConciliation.getConciliationCollection().remove(conciliationCollectionNewConciliation);
                         oldUserIDOfConciliationCollectionNewConciliation = em.merge(oldUserIDOfConciliationCollectionNewConciliation);
                     }
                 }
             }
             for (Fav favCollectionNewFav : favCollectionNew) {
                 if (!favCollectionOld.contains(favCollectionNewFav)) {
                     Registeredclient oldClientIDOfFavCollectionNewFav = favCollectionNewFav.getClientID();
                     favCollectionNewFav.setClientID(registeredclient);
                     favCollectionNewFav = em.merge(favCollectionNewFav);
                     if (oldClientIDOfFavCollectionNewFav != null && !oldClientIDOfFavCollectionNewFav.equals(registeredclient)) {
                         oldClientIDOfFavCollectionNewFav.getFavCollection().remove(favCollectionNewFav);
                         oldClientIDOfFavCollectionNewFav = em.merge(oldClientIDOfFavCollectionNewFav);
                     }
                 }
             }
             for (Vehicleadvert vehicleadvertCollectionNewVehicleadvert : vehicleadvertCollectionNew) {
                 if (!vehicleadvertCollectionOld.contains(vehicleadvertCollectionNewVehicleadvert)) {
                     Registeredclient oldClientIDOfVehicleadvertCollectionNewVehicleadvert = vehicleadvertCollectionNewVehicleadvert.getClientID();
                     vehicleadvertCollectionNewVehicleadvert.setClientID(registeredclient);
                     vehicleadvertCollectionNewVehicleadvert = em.merge(vehicleadvertCollectionNewVehicleadvert);
                     if (oldClientIDOfVehicleadvertCollectionNewVehicleadvert != null && !oldClientIDOfVehicleadvertCollectionNewVehicleadvert.equals(registeredclient)) {
                         oldClientIDOfVehicleadvertCollectionNewVehicleadvert.getVehicleadvertCollection().remove(vehicleadvertCollectionNewVehicleadvert);
                         oldClientIDOfVehicleadvertCollectionNewVehicleadvert = em.merge(oldClientIDOfVehicleadvertCollectionNewVehicleadvert);
                     }
                 }
             }
             for (Businessadvert businessadvertCollectionNewBusinessadvert : businessadvertCollectionNew) {
                 if (!businessadvertCollectionOld.contains(businessadvertCollectionNewBusinessadvert)) {
                     Registeredclient oldClientIDOfBusinessadvertCollectionNewBusinessadvert = businessadvertCollectionNewBusinessadvert.getClientID();
                     businessadvertCollectionNewBusinessadvert.setClientID(registeredclient);
                     businessadvertCollectionNewBusinessadvert = em.merge(businessadvertCollectionNewBusinessadvert);
                     if (oldClientIDOfBusinessadvertCollectionNewBusinessadvert != null && !oldClientIDOfBusinessadvertCollectionNewBusinessadvert.equals(registeredclient)) {
                         oldClientIDOfBusinessadvertCollectionNewBusinessadvert.getBusinessadvertCollection().remove(businessadvertCollectionNewBusinessadvert);
                         oldClientIDOfBusinessadvertCollectionNewBusinessadvert = em.merge(oldClientIDOfBusinessadvertCollectionNewBusinessadvert);
                     }
                 }
             }
             utx.commit();
         } catch (Exception ex) {
             try {
                 utx.rollback();
             } catch (Exception re) {
                 throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
             }
             String msg = ex.getLocalizedMessage();
             if (msg == null || msg.length() == 0) {
                 Integer id = registeredclient.getClientID();
                 if (findRegisteredclient(id) == null) {
                     throw new NonexistentEntityException("The registeredclient with id " + id + " no longer exists.");
                 }
             }
             throw ex;
         } finally {
             if (em != null) {
                 em.close();
             }
         }
     }
 
     public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
         EntityManager em = null;
        Context ctx = new InitialContext();
        this.utx = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
         try {
             utx.begin();
             em = getEntityManager();
             Registeredclient registeredclient;
             try {
                 registeredclient = em.getReference(Registeredclient.class, id);
                 registeredclient.getClientID();
             } catch (EntityNotFoundException enfe) {
                 throw new NonexistentEntityException("The registeredclient with id " + id + " no longer exists.", enfe);
             }
             List<String> illegalOrphanMessages = null;
             Collection<Conciliation> conciliationCollectionOrphanCheck = registeredclient.getConciliationCollection();
             for (Conciliation conciliationCollectionOrphanCheckConciliation : conciliationCollectionOrphanCheck) {
                 if (illegalOrphanMessages == null) {
                     illegalOrphanMessages = new ArrayList<String>();
                 }
                 illegalOrphanMessages.add("This Registeredclient (" + registeredclient + ") cannot be destroyed since the Conciliation " + conciliationCollectionOrphanCheckConciliation + " in its conciliationCollection field has a non-nullable userID field.");
             }
             Collection<Fav> favCollectionOrphanCheck = registeredclient.getFavCollection();
             for (Fav favCollectionOrphanCheckFav : favCollectionOrphanCheck) {
                 if (illegalOrphanMessages == null) {
                     illegalOrphanMessages = new ArrayList<String>();
                 }
                 illegalOrphanMessages.add("This Registeredclient (" + registeredclient + ") cannot be destroyed since the Fav " + favCollectionOrphanCheckFav + " in its favCollection field has a non-nullable clientID field.");
             }
             Collection<Vehicleadvert> vehicleadvertCollectionOrphanCheck = registeredclient.getVehicleadvertCollection();
             for (Vehicleadvert vehicleadvertCollectionOrphanCheckVehicleadvert : vehicleadvertCollectionOrphanCheck) {
                 if (illegalOrphanMessages == null) {
                     illegalOrphanMessages = new ArrayList<String>();
                 }
                 illegalOrphanMessages.add("This Registeredclient (" + registeredclient + ") cannot be destroyed since the Vehicleadvert " + vehicleadvertCollectionOrphanCheckVehicleadvert + " in its vehicleadvertCollection field has a non-nullable clientID field.");
             }
             Collection<Businessadvert> businessadvertCollectionOrphanCheck = registeredclient.getBusinessadvertCollection();
             for (Businessadvert businessadvertCollectionOrphanCheckBusinessadvert : businessadvertCollectionOrphanCheck) {
                 if (illegalOrphanMessages == null) {
                     illegalOrphanMessages = new ArrayList<String>();
                 }
                 illegalOrphanMessages.add("This Registeredclient (" + registeredclient + ") cannot be destroyed since the Businessadvert " + businessadvertCollectionOrphanCheckBusinessadvert + " in its businessadvertCollection field has a non-nullable clientID field.");
             }
             if (illegalOrphanMessages != null) {
                 throw new IllegalOrphanException(illegalOrphanMessages);
             }
             em.remove(registeredclient);
             utx.commit();
         } catch (Exception ex) {
             try {
                 utx.rollback();
             } catch (Exception re) {
                 throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
             }
             throw ex;
         } finally {
             if (em != null) {
                 em.close();
             }
         }
     }
 
     public List<Registeredclient> findRegisteredclientEntities() {
         return findRegisteredclientEntities(true, -1, -1);
     }
 
     public List<Registeredclient> findRegisteredclientEntities(int maxResults, int firstResult) {
         return findRegisteredclientEntities(false, maxResults, firstResult);
     }
 
     private List<Registeredclient> findRegisteredclientEntities(boolean all, int maxResults, int firstResult) {
         EntityManager em = getEntityManager();
         try {
             CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
             cq.select(cq.from(Registeredclient.class));
             Query q = em.createQuery(cq);
             if (!all) {
                 q.setMaxResults(maxResults);
                 q.setFirstResult(firstResult);
             }
             return q.getResultList();
         } finally {
             em.close();
         }
     }
 
     public Registeredclient findRegisteredclient(Integer id) {
         EntityManager em = getEntityManager();
         try {
             return em.find(Registeredclient.class, id);
         } finally {
             em.close();
         }
     }
 
     public int getRegisteredclientCount() {
         EntityManager em = getEntityManager();
         try {
             CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
             Root<Registeredclient> rt = cq.from(Registeredclient.class);
             cq.select(em.getCriteriaBuilder().count(rt));
             Query q = em.createQuery(cq);
             return ((Long) q.getSingleResult()).intValue();
         } finally {
             em.close();
         }
     }
     
 }
