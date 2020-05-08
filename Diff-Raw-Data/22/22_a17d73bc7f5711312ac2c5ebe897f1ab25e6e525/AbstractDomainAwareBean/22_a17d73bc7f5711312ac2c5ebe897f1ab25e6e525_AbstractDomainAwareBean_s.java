 /**
  * ESUP-Portail Blank Application - Copyright (c) 2006 ESUP-Portail consortium
  * http://sourcesup.cru.fr/projects/esup-opiR1
  */
 package org.esupportail.opi.web.controllers;
 
 import org.esupportail.commons.beans.AbstractJsfMessagesAwareBean;
 import org.esupportail.commons.exceptions.UserNotFoundException;
 import org.esupportail.commons.services.logging.Logger;
 import org.esupportail.commons.services.logging.LoggerImpl;
 import org.esupportail.commons.utils.Assert;
 import org.esupportail.commons.web.controllers.Resettable;
 import org.esupportail.opi.domain.BusinessCacheService;
 import org.esupportail.opi.domain.DomainApoService;
 import org.esupportail.opi.domain.DomainService;
 import org.esupportail.opi.domain.ParameterService;
 import org.esupportail.opi.domain.beans.user.User;
 import org.esupportail.opi.web.beans.parameters.RegimeInscription;
 import org.esupportail.opi.web.beans.pojo.IndividuPojo;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 /**
  * An abstract class inherited by all the beans for them to get:
  * - the domain service (domainService).
  * - the application service (applicationService).
  * - the i18n service (i18nService).
  */
 public abstract class AbstractDomainAwareBean extends AbstractJsfMessagesAwareBean implements Resettable {
 
     /**
      *
      */
     private static final long serialVersionUID = 1L;
 
 
     /**
      * A logger.
      */
     private final Logger logger = new LoggerImpl(this.getClass());
 
     /**
      * see {@link DomainService}.
      */
     private DomainService domainService;
 
     /**
      * see {@link ParameterService}.
      */
     private ParameterService parameterService;
 
     /**
      * see {@link DomainApoService}.
      */
     private DomainApoService domainApoService;
 
     /**
      * see {@link BusinessCacheService}.
      */
     private BusinessCacheService businessCacheService;
 
     /**
      * The list of bean regimeInscription.
      */
     private List<RegimeInscription> regimeInscriptions;
 
     /**
      * Constructor.
      */
     protected AbstractDomainAwareBean() {
         super();
     }
 
     /**
      * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
      */
     @Override
     public final void afterPropertiesSet() {
         super.afterPropertiesSet();
         Assert.notNull(this.domainService,
                 "property domainService of class " + this.getClass().getName() + " can not be null");
         Assert.notNull(this.parameterService,
                 "property parameterService of class " + this.getClass().getName() + " can not be null");
         Assert.notNull(this.domainApoService,
                 "property domainApoService of class " + this.getClass().getName() + " can not be null");
         Assert.notNull(this.businessCacheService,
                 "property businessCacheService of class " + this.getClass().getName() + " can not be null");
         Assert.notNull(this.regimeInscriptions, "property regimeInscription of class "
                 + this.getClass().getName() + " can not be null");
         Assert.notEmpty(this.regimeInscriptions, "property regimeInscription of class "
                 + this.getClass().getName() + " can not be empty");
 
         afterPropertiesSetInternal();
         reset();
     }
 
     /**
      * This method is run once the object has been initialized, just before reset().
      */
     protected void afterPropertiesSetInternal() {
         // override this method
     }
 
     /**
      * @see org.esupportail.commons.web.controllers.Resettable#reset()
      */
     public void reset() {
         // nothing to reset
 
     }
 
 
     /**
      * @return Map all RegimeInscription by code.
      */
     public Map<Integer, RegimeInscription> getRegimeIns() {
         if (logger.isDebugEnabled()) {
             logger.debug("entering getRegimeInscriptions");
         }
         Map<Integer, RegimeInscription> map = new HashMap<Integer, RegimeInscription>();
         for (RegimeInscription ri : regimeInscriptions) {
             map.put(ri.getCode(), ri);
         }
         return map;
     }
 
 	/*
 	 ******************* ACCESSORS ******************** */
 
     /**
      * @param regimeInscriptions
      */
     public void setRegimeInscriptions(final List<RegimeInscription> regimeInscriptions) {
         this.regimeInscriptions = regimeInscriptions;
     }
 
 
     /**
      * @param domainService the domainService to set
      */
     public void setDomainService(final DomainService domainService) {
         this.domainService = domainService;
     }
 
     /**
      * @return the domainService
      */
     public DomainService getDomainService() {
         return domainService;
     }
 
     /**
      * @return the current user.
      */
     protected User getCurrentUser() {
         // this method should be overriden
         return null;
     }
 
     /**
      * @return the current individu.
      */
     protected IndividuPojo getCurrentInd() {
         // this method should be overriden
         return null;
     }
     
     /**
     * @return the current individu.
      */
     protected IndividuPojo getCurrentIndInit() {
         // this method should be overriden
         return null;
     }
 
     /**
      * @return the current user's locale.
      */
     @Override
     public Locale getCurrentUserLocale() {
         if (logger.isDebugEnabled()) {
             logger.debug(this.getClass().getName() + ".getCurrentUserLocale()");
         }
         User currentUser = null;
         try {
             currentUser = getCurrentUser();
         } catch (UserNotFoundException u) {
             if (logger.isDebugEnabled()) {
                 logger.debug("pour de gestionnaire connecte");
             }
         }
         if (getCurrentInd() != null) {
             currentUser = getCurrentInd().getIndividu();
         }
         if (currentUser == null) {
             if (logger.isDebugEnabled()) {
                 logger.debug("no current user, return null");
             }
             return null;
         }
         String lang = currentUser.getLanguage();
         if (lang == null) {
             if (logger.isDebugEnabled()) {
                 logger.debug("language not set for user '" + currentUser.getId()
                         + "', return null");
             }
             return null;
         }
         Locale locale = new Locale(lang);
         if (logger.isDebugEnabled()) {
             logger.debug("language for user '" + currentUser.getId() + "' is '" + locale + "'");
         }
         return locale;
     }
 
     /**
      * @return the parameterService
      */
     public ParameterService getParameterService() {
         return parameterService;
     }
 
     /**
      * @param parameterService the parameterService to set
      */
     public void setParameterService(final ParameterService parameterService) {
         this.parameterService = parameterService;
     }
 
     /**
      * @return the domainApoService
      */
     public DomainApoService getDomainApoService() {
         return domainApoService;
     }
 
     /**
      * @param domainApoService the domainApoService to set
      */
     public void setDomainApoService(final DomainApoService domainApoService) {
         this.domainApoService = domainApoService;
     }
 
     /**
      * @return the businessCacheService
      */
     public BusinessCacheService getBusinessCacheService() {
         return businessCacheService;
     }
 
     /**
      * @param businessCacheService the businessCacheService to set
      */
     public void setBusinessCacheService(final BusinessCacheService businessCacheService) {
         this.businessCacheService = businessCacheService;
     }
 
 
 }
