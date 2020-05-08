 package org.synyx.minos.support.jpa;
 
 import java.net.URL;
 
 import javax.persistence.spi.PersistenceUnitInfo;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
 import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
 
 
 /**
  * Extends <code>DefaultPersistenceUnitManager</code> to merge configurations of
  * one persistence unit residing in multiple <code>persistence.xml</code> files
  * into one. This is necessary to allow the declaration of entities in seperate
  * modules.
  * 
  * @author Oliver Gierke
  * @see http://jira.springframework.org/browse/SPR-2598
  */
 public class MergingPersistenceUnitManager extends
         DefaultPersistenceUnitManager {
 
     private static final Log log =
             LogFactory.getLog(MergingPersistenceUnitManager.class);
 
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager
      * #
      * postProcessPersistenceUnitInfo(org.springframework.orm.jpa.persistenceunit
      * .MutablePersistenceUnitInfo)
      */
     @Override
     protected void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
 
         // Invoke normal post processing
         super.postProcessPersistenceUnitInfo(pui);
        pui.addJarFileUrl(pui.getPersistenceUnitRootUrl());
 
         PersistenceUnitInfo oldPui =
                 getPersistenceUnitInfo(pui.getPersistenceUnitName());
 
         if (oldPui != null) {
 
             for (URL url : oldPui.getJarFileUrls()) {
 
                 // Add jar file url to PUI
                 if (!pui.getJarFileUrls().contains(url)) {
 
                     log.debug(String.format("Adding %s to persistence units",
                             url));
 
                     pui.addJarFileUrl(url);
                 }
             }
         }
     }
 }
