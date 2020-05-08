 package com.checkers.server.dao;
 
 import com.checkers.server.beans.Game;
 import com.checkers.server.beans.Step;
 import com.checkers.server.beans.User;
 import com.checkers.server.beans.proxy.StepProxy;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import java.util.List;
 
 /**
  *
  *
  * @author Pavel Kuchin
  */
 @Repository("stepDao")
 @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
 public class StepDaoImpl implements StepDao {
     @PersistenceContext
     private EntityManager em;
 
     @Override
     public Step getStep(Long suid) {
         return em.find(Step.class, suid);
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
     public void newStep(Step step) {
         em.persist(step);
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
     public Step newStep(StepProxy stepProxy) {
         Step step = new Step(stepProxy);
 
         step.setGame(em.getReference(Game.class, stepProxy.getGauid()));
         step.setUser(em.getReference(User.class, stepProxy.getUuid()));
 
         this.newStep(step);
 
         return step;
     }
 
     @Override
     public List<Step> getGameSteps(Long gauid) {
         return em.createQuery("SELECT s FROM Step s WHERE s.game.gauid = :gauid")
                 .setParameter("gauid", gauid)
                 .getResultList();
     }
 
     @Override
     public Step getGameLastStep(Long gauid) {
         try{
            return (Step)em.createQuery("SELECT s FROM Step s ORDER BY s.suid DESC").setMaxResults(1).getSingleResult();
         }catch(javax.persistence.NoResultException e){
             return null;
         }
     }
 }
