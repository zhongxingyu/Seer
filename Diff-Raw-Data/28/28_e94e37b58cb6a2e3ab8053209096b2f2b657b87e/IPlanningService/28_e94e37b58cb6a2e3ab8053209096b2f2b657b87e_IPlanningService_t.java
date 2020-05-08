 package com.level42.mixit.services;
 
 import java.util.List;
 
 import com.level42.mixit.exceptions.FunctionnalException;
 import com.level42.mixit.exceptions.TechnicalException;
 import com.level42.mixit.models.Talk;
 
 /**
  * Interface de manipulation des planning de MixIT.
  */
 public interface IPlanningService {
 
     /**
      * Méthode retournant les talks groupés par date.
      *
      * @param delay
      *            Delai a partir duquel les sessions sont masquées
      * @return Talk groupés par date
      * @throws TechnicalException
      * @throws FunctionnalException
      */
    List<Talk> getTalksForPlanning(Integer delay, boolean hide)
             throws FunctionnalException, TechnicalException;
 }
