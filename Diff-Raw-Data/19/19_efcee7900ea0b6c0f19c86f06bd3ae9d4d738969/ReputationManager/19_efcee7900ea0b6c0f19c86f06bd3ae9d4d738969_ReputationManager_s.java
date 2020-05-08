 /*
  * To change this template, choose Tools | Templates and open the template in
  * the editor.
  */
 package com.mymed.controller.core.manager.reputation.api.recommendation_manager;
 
 import com.mymed.controller.core.manager.reputation.api.mymed_ids.IMymedRepId;
 import com.mymed.controller.core.manager.reputation.db.table.ReputationEntity;
 import com.mymed.model.data.reputation.MReputationBean;
 
 /**
  * This class contains methods allowing the MyMed system to read the reputation
  * value of a given userservice.
  * 
  * @author piccolo
  */
 public class ReputationManager implements IReputationManager {
     @Override
     public MReputationBean read(final IMymedRepId repEntityId) {
         final ReputationEntity re = ReputationEntity.getCreating(repEntityId.getPrimaryId());
         if (re == null) {
            return null;
         } else {
             return new MReputationBean(re.getReputation(), re.getNumberOfVerdicts());
         }
     }
 
     @Override
     public MReputationBean calculate(final IMymedRepId repEntityId, final IReputationAlgorithm alg) {
         return alg.computeReputation(repEntityId.getPrimaryId());
     }
 }
