 /*
  * Copyright (c) 2010-2012 Matthias Klass, Johannes Leimer,
  *               Rico Lieback, Sebastian Gabriel, Lothar Gesslein,
  *               Alexander Rampp, Kai Weidner
  *
  * This file is part of the Physalix Enrollment System
  *
  * Foobar is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Foobar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package hsa.awp.scire.services;
 
 import hsa.awp.campaign.facade.ICampaignFacade;
 import hsa.awp.campaign.model.ConfirmedRegistration;
 import hsa.awp.campaign.model.DrawProcedure;
 import hsa.awp.campaign.model.PriorityList;
 import hsa.awp.campaign.model.PriorityListItem;
 import hsa.awp.campaign.rule.ICampaignRuleChecker;
import hsa.awp.event.facade.IEventFacade;
 import hsa.awp.event.model.Event;
 import hsa.awp.user.model.SingleUser;

import org.hibernate.LazyInitializationException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.LinkedList;
 import java.util.List;
 
 public class EventCheckService {
   /**
    * Standard logger.
    */
   protected transient Logger logger;
 
   private ICampaignFacade campaignFacade;
 
   private ICampaignRuleChecker ruleChecker;
  
  private IEventFacade eventFacade;

  public void setEventFacade(IEventFacade eventFacade) {
	this.eventFacade = eventFacade;
}
 
public EventCheckService() {
 
     logger = LoggerFactory.getLogger(this.getClass());
   }
 
   @Transactional
   public List<Event> filterEventList(List<Event> events, SingleUser singleUser, DrawProcedure drawProcedure, List<Event> blackList) {
 
     List<Event> eventList = new LinkedList<Event>();
 
     List<PriorityList> priorityListsByUserAndProcedure = campaignFacade.findPriorityListsByUserAndProcedure(singleUser.getId(),
         drawProcedure);
     List<ConfirmedRegistration> confirmedRegistrations = campaignFacade.findConfirmedRegistrationsByParticipantId(singleUser
         .getId());
     for (Event e : events) {
       /*
       * if nothing found add event
       */
       if (!checkEvent(e, priorityListsByUserAndProcedure, confirmedRegistrations, singleUser, drawProcedure, blackList)) {
         eventList.add(e);
       }
     }
 
     return eventList;
   }
 
   private boolean checkEvent(Event e, List<PriorityList> priorityListsByUserAndProcedure,
                              List<ConfirmedRegistration> confirmedRegistrations, SingleUser singleUser, DrawProcedure drawProcedure, List<Event> blackList) {
 
     if (blackList.contains(e)) {
       return true;
     }
 
     /*
     * check if event is already in a persisted priolist
     */
     for (PriorityList prioList : priorityListsByUserAndProcedure) {
       for (PriorityListItem item : prioList.getItems()) {
         if (item.getEvent().equals(e.getId())) {
           return true;
         }
       }
     }
 
     /*
     * check if user is already registered.
     */
     for (ConfirmedRegistration registration : confirmedRegistrations) {
       if (registration.getEventId().equals(e.getId())) {
         return true;
       }
     }
 
     /*
     * check if event has enough empty slots
     */
     int maxParticipants = e.getMaxParticipants();
    long participantCount = -1;
    try{
    	participantCount = e.getConfirmedRegistrations().size();	
    }catch(LazyInitializationException ex){
    	e = eventFacade.getEventByEventId(e.getEventId());
    }finally{
    	participantCount = e.getConfirmedRegistrations().size();
    }
    		
 
     if ((maxParticipants - participantCount) <= 0) {
       return true;
     }
 
     /*
     * check if rules disallow user
     */
     if (!ruleChecker.isRegistrationAllowed(singleUser, drawProcedure.getCampaign(), e)) {
       return true;
     }
 
     return false;
   }
 
   public void setCampaignFacade(ICampaignFacade campaignFacade) {
 
     this.campaignFacade = campaignFacade;
   }
 
   public void setRuleChecker(ICampaignRuleChecker ruleChecker) {
 
     this.ruleChecker = ruleChecker;
   }
 }
