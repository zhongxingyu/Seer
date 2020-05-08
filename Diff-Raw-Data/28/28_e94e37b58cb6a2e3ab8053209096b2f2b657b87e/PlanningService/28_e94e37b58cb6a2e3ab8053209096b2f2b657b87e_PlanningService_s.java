 package com.level42.mixit.services.impl;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import com.google.inject.Inject;
 import com.level42.mixit.exceptions.FunctionnalException;
 import com.level42.mixit.exceptions.TechnicalException;
 import com.level42.mixit.models.Planning;
 import com.level42.mixit.models.Session;
 import com.level42.mixit.models.Talk;
 import com.level42.mixit.services.IPlanningService;
 import com.level42.mixit.services.ITalkService;
 
 /**
  * Service de gestion du planning.
  */
 public class PlanningService extends AbstractService implements
         IPlanningService {
 
     /**
      * Interface vers le service de gestion des talk.
      */
     @Inject
     private ITalkService talkService;
 
     /**
      * Liste des sessions indexées sur l'identifiant.
      */
     private Map<Integer, Session> sessions;
 
     /*
      * (non-Javadoc)
      * @see com.level42.mixit.services.IPlanningService#getTalksForPlanning(java.lang.Integer)
      */
    public List<Talk> getTalksForPlanning(Integer delay)
             throws FunctionnalException, TechnicalException {
         // Récupère les sessions
         List<Talk> talks = talkService.getTalks();
         Long limiteDate = new Date().getTime() - (delay * 60 * 1000);
 
         // Ajout des sessions
         List<Talk> plannedTalks = new ArrayList<Talk>();
         for (Talk talk : talks) {
             if (talk.getDateSession() != null 
                     && talk.getDateSession().after(new Date(limiteDate))) {
                 plannedTalks.add(talk);
             }
         }
 
         // Tri
         Collections.sort(plannedTalks);
 
         return plannedTalks;
     }
 
     /**
      * Index les sessions par identifiant de talk.
      * @param planning
      */
     protected void indexPlanning(Planning planning) {
         if (sessions == null) {
             sessions = new Hashtable<Integer, Session>();
 
             for (Session session : planning.getSessions()) {
                 sessions.put(session.getId(), session);
             }
         }
     }
 }
