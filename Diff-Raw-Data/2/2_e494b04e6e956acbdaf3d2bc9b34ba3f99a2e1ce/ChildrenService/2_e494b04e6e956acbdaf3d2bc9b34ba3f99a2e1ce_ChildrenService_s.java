 package org.mat.nounou.services;
 
 import org.mat.nounou.model.Account;
 import org.mat.nounou.model.Child;
 import org.mat.nounou.model.Nurse;
 import org.mat.nounou.servlets.EntityManagerLoaderListener;
 import org.mat.nounou.util.Constants;
 import org.mat.nounou.vo.ChildVO;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.TypedQuery;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * UserVO: mlecoutre
  * Date: 27/10/12
  * Time: 12:01
  */
 @Path("/children")
 @Produces(MediaType.APPLICATION_JSON)
 public class ChildrenService {
 
     private static final Logger logger = LoggerFactory.getLogger(ChildrenService.class);
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public List<ChildVO> get() {
         logger.debug("Get Child service");
         List<Child> kids = null;
         List<ChildVO> children = new ArrayList<ChildVO>();
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         try {
             TypedQuery<Child> query = em.createQuery("FROM Child", Child.class);
             query.setMaxResults(Constants.MAX_RESULT);
             kids = query.getResultList();
         } catch (NoResultException nre) {
             logger.error("No children found in db.");
         } finally {
             em.close();
         }
         for (Child c : kids) {
             ChildVO vo = populate(c);
             children.add(vo);
         }
         return children;
     }
 
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public ChildVO registerKid(ChildVO child) {
         logger.debug("register Child " + child);
         Child childEntity = new Child();
         childEntity.setFirstName(child.getFirstName());
         childEntity.setLastName(child.getLastName());
         childEntity.setPictureUrl(child.getPictureUrl());
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         try {
             childEntity.setBirthday(Constants.sdfDate.parse(child.getBirthday()));
             TypedQuery<Account> qAccount = em.createQuery("FROM Account a WHERE accountId=:accountId", Account.class);
             qAccount.setParameter("accountId", child.getAccountId());
             Account account = qAccount.getSingleResult();
             childEntity.setAccount(account);
 
             TypedQuery<Nurse> qNurse = em.createQuery("FROM Nurse n WHERE nurseId=:nurseId", Nurse.class);
             qNurse.setParameter("nurseId", child.getNurseId());
             Nurse nurse = qNurse.getSingleResult();
             childEntity.setNurse(nurse);
             em.getTransaction().begin();
             em.persist(childEntity);
             em.getTransaction().commit();
             child.setChildId(childEntity.getChildId());
             child.setNurseName(nurse.getFirstName());
         } catch (Exception e) {
             logger.error("ERROR registerKid", e);
         } finally {
             em.close();
         }
         return child;
     }
 
 
     @POST
     @Path("/{childId}")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public ChildVO updateChild(ChildVO child, @PathParam("childId") Integer childId) {
         logger.debug("Update Child " + child);
 
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
 
         try {
             TypedQuery<Child> query = em.createQuery("FROM Child WHERE childId=:childId", Child.class);
             query.setParameter("childId", childId);
             Child childEntity = query.getSingleResult();
             childEntity.setFirstName(child.getFirstName());
             childEntity.setLastName(child.getLastName());
             childEntity.setBirthday(Constants.sdfDate.parse(child.getBirthday()));
            childEntity.setPictureUrl(child.getPictureUrl);
             TypedQuery<Account> qAccount = em.createQuery("FROM Account a WHERE accountId=:accountId", Account.class);
             qAccount.setParameter("accountId", child.getAccountId());
             Account account = qAccount.getSingleResult();
             childEntity.setAccount(account);
 
             TypedQuery<Nurse> qNurse = em.createQuery("FROM Nurse n WHERE nurseId=:nurseId", Nurse.class);
             qNurse.setParameter("nurseId", child.getNurseId());
             Nurse nurse = qNurse.getSingleResult();
             childEntity.setNurse(nurse);
             em.getTransaction().begin();
             em.persist(childEntity);
             em.getTransaction().commit();
             child.setChildId(childEntity.getChildId());
             child.setNurseName(nurse.getFirstName());
         }catch (NoResultException nre){
            logger.warn(String.format("No child with childId:%d to update\n", childId));
         }catch (Exception e) {
             logger.error("ERROR in updateChild", e);
         } finally {
             em.close();
         }
         return child;
     }
 
 
     @GET
     @Path("/account/{accountId}")
     public List<ChildVO> findByAccountId(@PathParam("accountId") Integer accountId) {
         List<ChildVO> cList = new ArrayList<ChildVO>();
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         try {
             TypedQuery<Child> query = em.createQuery("FROM Child c WHERE c.account.accountId=:accountId", Child.class);
             query.setMaxResults(Constants.MAX_RESULT);
             query.setParameter("accountId", accountId);
             List<Child> children = query.getResultList();
 
             //populate vo
             for (Child c : children) {
                 ChildVO vo = populate(c);
                 cList.add(vo);
             }
         } catch (NoResultException nre) {
             logger.warn("No result found for accountId:= " + accountId);
         } catch (Exception e) {
             logger.error("ERROR in findByAccountId", e);
         } finally {
             em.close();
         }
         return cList;
     }
 
     /**
      * Populate the value object wihtin the entity
      * @param c   the entity
      * @return    the value object
      */
     public static ChildVO populate(Child c) {
         ChildVO vo = new ChildVO();
         vo.setAccountId(c.getAccount().getAccountId());
         if (c.getBirthday() != null) //birthday is an optional parameter
             vo.setBirthday(Constants.sdfDate.format(c.getBirthday()));
         vo.setChildId(c.getChildId());
         vo.setFirstName(c.getFirstName());
         vo.setLastName(c.getLastName());
         vo.setPictureUrl(c.getPictureUrl());
         if (c.getNurse() != null) {
             vo.setNurseId(c.getNurse().getNurseId());
             vo.setNurseName(c.getNurse().getFirstName().concat(" ").concat(c.getNurse().getLastName()));
         }
         return vo;
     }
 
 
     @GET
     @Path("/delete/{childId}")
     public Response deleteById(@PathParam("childId") Integer childId) {
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         try {
             em.getTransaction().begin();
             TypedQuery<Child> query = em.createQuery(" FROM Child WHERE childId=:childId", Child.class);
 
             query.setParameter("childId", childId);
             Child c = query.getSingleResult();
             em.remove(c);
             em.getTransaction().commit();
 
         } catch (Exception e) {
             logger.error("ERROR in deleteById", e);
             return Response.serverError().build();
         } finally {
             em.close();
         }
         return Response.ok().build();
     }
 
     @GET
     @Path("/{childId}")
     public Response getById(@PathParam("childId") Integer childId) {
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         ChildVO childVO = new ChildVO();
         try {
             TypedQuery<Child> query = em.createQuery("FROM Child WHERE childId=:childId", Child.class);
             query.setParameter("childId", childId);
             Child c = query.getSingleResult();
             childVO = populate(c);
         } catch (Exception e) {
             logger.error("ERROR in getById", e);
             return Response.serverError().build();
         } finally {
             em.close();
         }
         return Response.ok().entity(childVO).build();
     }
 
 }
