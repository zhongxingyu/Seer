 /**
  * ESUP-Portail Blank Application - Copyright (c) 2006 ESUP-Portail consortium
  * http://sourcesup.cru.fr/projects/esup-opiR1
  */
 package org.esupportail.opi.web.controllers;
 
 
 import org.esupportail.commons.utils.Assert;
 import org.esupportail.opi.domain.beans.user.Gestionnaire;
 import org.esupportail.opi.domain.beans.user.User;
 import org.esupportail.opi.web.beans.pojo.IndividuPojo;
 
 
 /**
  * An abstract class inherited by all the beans for them to get:
  * - the context of the application (sessionController).
  * - the domain service (domainService).
  * - the application service (applicationService).
  * - the i18n service (i18nService).
  */
 public abstract class AbstractContextAwareController extends AbstractDomainAwareBean {
 
     /**
      * The serialization id.
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * The SessionController.
      */
     private SessionController sessionController;
 
 
     /**
      * Constructor.
      */
     protected AbstractContextAwareController() {
         super();
     }
 
     /**
      * @see org.esupportail.opi.web.controllers.AbstractDomainAwareBean#afterPropertiesSetInternal()
      */
     @Override
     public void afterPropertiesSetInternal() {
         Assert.notNull(this.sessionController, "property sessionController of class "
                 + this.getClass().getName() + " can not be null");
 
     }
 
     /**
      * @param sessionController the sessionController to set
      */
     public void setSessionController(final SessionController sessionController) {
         this.sessionController = sessionController;
     }
 
     /**
      * @return the sessionController
      */
     public SessionController getSessionController() {
         return sessionController;
     }
 
 
     /**
      * Return the current user if is a gestionnaire.
      *
      * @return Gestionnaire
      */
     public Gestionnaire getCurrentGest() {
         User u = sessionController.getCurrentUser();
         if (!(u instanceof Gestionnaire)) {
             return sessionController.getManager();
         }
         return (Gestionnaire) u;
     }
 
 
     /**
      * Return the current user if is a Individu.
      *
      * @return Individu
      */
     @Override
     public IndividuPojo getCurrentInd() {
         return sessionController.getCurrentInd();
     }
     
     /**
     * Return the current user if is a Individu without using the cache
      *
      * @return Individu
      */
     @Override
     public IndividuPojo getCurrentIndInit() {
         return sessionController.getCurrentIndInit();
     }
 
 
 }
