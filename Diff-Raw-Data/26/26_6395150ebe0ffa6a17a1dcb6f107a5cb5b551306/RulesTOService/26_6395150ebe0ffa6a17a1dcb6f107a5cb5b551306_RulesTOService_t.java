 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ch.heigvd.gamification.services.to;
 
 import ch.heigvd.gamification.exceptions.EntityNotFoundException;
 import ch.heigvd.gamification.model.Rule;
 import ch.heigvd.gamification.services.crud.BadgesManagerLocal;
 import ch.heigvd.gamification.to.PublicRuleTO;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 
 /**
  *
  * @author Jean-Luc
  */
 @Stateless
 public class RulesTOService implements RulesTOServiceLocal 
 {
     
     @EJB
     BadgesTOServiceLocal badgesTOService;
     
     @EJB
     BadgesManagerLocal badgesManager;
     
     @Override
     public PublicRuleTO buildPublicRuleTo (Rule source)
     {  
         PublicRuleTO rule = new PublicRuleTO(
                 source.getId(), 
                 source.getOnEventType(),
                 source.getNumberOfPoints(),
                 badgesTOService.buildPublicBadgeTO(source.getBadge())/*,
                 appTO.buildPublicApplicationTO(source.getApplication())*/
         );
         return rule;
     }
     
     @Override
     public void updateRuleEntity(Rule existingEntity, PublicRuleTO newState)
     {
         existingEntity.setOnEventType(newState.getOnEventType());
         existingEntity.setNumberOfPoints(newState.getNumberOfPoints());
         try {
             existingEntity.setBadge(badgesManager.findById(newState.getBadge().getId()));
         } catch (EntityNotFoundException ex) {
             Logger.getLogger(RulesTOService.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
 }
