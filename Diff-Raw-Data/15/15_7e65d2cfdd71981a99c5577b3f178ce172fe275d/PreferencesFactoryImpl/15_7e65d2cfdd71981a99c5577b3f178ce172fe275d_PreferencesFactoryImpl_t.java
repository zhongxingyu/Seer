 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contentsOfthis file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copyOfthe License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is partOfdcm4che, an implementationOfDICOM(TM) in
  * Java(TM), hosted at https://github.com/gunterze/dcm4che.
  *
  * The Initial DeveloperOfthe Original Code is
  * Agfa Healthcare.
  * Portions created by the Initial Developer are Copyright (C) 2011
  * the Initial Developer. All Rights Reserved.
  *
  * Contributor(s):
  * See @authors listed below
  *
  * Alternatively, the contentsOfthis file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisionsOfthe GPL or the LGPL are applicable instead
  *Ofthose above. If you wish to allow useOfyour versionOfthis file only
  * under the termsOfeither the GPL or the LGPL, and not to allow others to
  * use your versionOfthis file under the termsOfthe MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your versionOfthis file under
  * the termsOfany oneOfthe MPL, the GPL or the LGPL.
  *
  * ***** END LICENSE BLOCK ***** */
 
 package org.dcm4che.jdbc.prefs;
 
 import java.util.List;
 import java.util.prefs.Preferences;
 import java.util.prefs.PreferencesFactory;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NoResultException;
 
 import org.apache.log4j.Logger;
 import org.dcm4che.jdbc.prefs.persistence.Attribute;
 import org.dcm4che.jdbc.prefs.persistence.Node;
 
 /**
  * @author Michael Backhaus <michael.backhaus@agfa.com>
  */
 public class PreferencesFactoryImpl implements PreferencesFactory {
 
     private Preferences rootPreferences;
 
     protected static final Logger LOG = Logger.getLogger(PreferencesFactoryImpl.class);
 
     EntityManager em;
 
    protected PreferencesFactoryImpl(EntityManagerFactory emf) {
        this.em = emf.createEntityManager();
    }

     private static EntityManagerFactory lookupEntityManagerFactory() throws InterruptedException {
         EntityManagerFactory emf = null;
         int counter = getCounter();
         while (emf == null)
             try {
                 emf = (EntityManagerFactory) new InitialContext().lookup("java:jboss/JdbcPrefsEntityManagerFactory");
             } catch (NamingException e) {
                 if (counter == 0)
                     throw new RuntimeException(e);
                 else {
                     LOG.error("Waiting for JNDI lookup of java:jboss/JdbcPrefsEntityManagerFactory ... " + counter);
                     counter--;
                     Thread.sleep(1000);
                 }
             }
         return emf;
     }
 
     private static int getCounter() {
         try {
             String counterString = System.getProperty("jdbc.prefs.jndi.counter");
             return (counterString == null) ? 30 : Integer.parseInt(counterString);
         } catch (NumberFormatException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public Preferences systemRoot() {
         if (rootPreferences == null) {
             String datasource = System.getProperty("jdbc.prefs.datasource");
             if (datasource == null)
                 throw new RuntimeException("Missing system property 'jdbc.prefs.datasource'");
 
             if (datasource.startsWith("jdbc:")) {
                 rootPreferences = new PreferencesImpl(new PreferencesFactoryJDBCImpl());
             } else if (datasource.startsWith("java:")) {
                 try {
                     em = lookupEntityManagerFactory().createEntityManager();
                     rootPreferences = new PreferencesImpl(this);
                 } catch (InterruptedException e) {
                     throw new RuntimeException(e);
                 }
             } else
                 throw new RuntimeException("Unsupported datasource: " + datasource);
         }
         return rootPreferences;
     }
 
     @Override
     public Preferences userRoot() {
         return systemRoot();
     }
 
     public void insertNode(Node node) {
         em.persist(node);
     }
 
     public void removeNode(Node node) {
         em.createNamedQuery(Attribute.DELETE_BY_NODE).setParameter(1, node).executeUpdate();
         em.remove(node);
     }
 
     public void updateAttribute(Attribute attribute) {
         em.persist(attribute);
     }
 
     public void removeAttributeByKey(String key, Node node) {
         em.createNamedQuery(Attribute.DELETE_BY_KEY).setParameter(1, key).setParameter(2, node).executeUpdate();
     }
 
     public Node getNodeByName(String name) {
         try {
             Node result = em.createNamedQuery(Node.GET_NODE_BY_NAME, Node.class).setParameter(1, name)
                     .getSingleResult();
             return result;
         } catch (NoResultException e) {
             return new Node();
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     public List<Node> getChildren(Node parent) {
         List<Node> result = em.createNamedQuery(Node.GET_CHILDREN, Node.class).setParameter(1, parent).getResultList();
         return result;
     }
 
     public List<Attribute> getAttributes(Node parent) {
         List<Attribute> result = em.createNamedQuery(Attribute.GET_ATTRIBUTE_BY_PARENT_NODE, Attribute.class)
                 .setParameter(1, parent).getResultList();
         return result;
     }
 
     public void flush() {
         try {
             em.flush();
         } catch (Exception e) {
             LOG.error(e);
         }
     }
 
     public void refresh(Node node) {
         em.refresh(node);
     }
 }
