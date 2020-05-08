 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
  * Java(TM), hosted at https://github.com/gunterze/dcm4che.
  *
  * The Initial Developer of the Original Code is
  * Agfa Healthcare.
  * Portions created by the Initial Developer are Copyright (C) 2012
  * the Initial Developer. All Rights Reserved.
  *
  * Contributor(s):
  * See @authors listed below
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  *
  * ***** END LICENSE BLOCK ***** */
 
 package org.dcm4che.jdbc.prefs;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NoResultException;
 import javax.persistence.Persistence;
 
 import org.dcm4che.jdbc.prefs.persistence.Attribute;
 import org.dcm4che.jdbc.prefs.persistence.Node;
 
 /**
  * @author Michael Backhaus <michael.backhaus@agfa.com>
  */
 public class PreferencesFactoryJDBCImpl extends PreferencesFactoryImpl {
 
     public static EntityManagerFactory createEntityManagerFactory() {
         String username = System.getProperty("jdbc.prefs.connection.username");
         if (username == null)
             throw new RuntimeException("Missing system property 'jdbc.prefs.connection.username'");
 
         String password = System.getProperty("jdbc.prefs.connection.password");
         if (password == null)
             throw new RuntimeException("Missing system property 'jdbc.prefs.connection.password'");
 
         Map<String, String> properties = new HashMap<String, String>();
         properties.put("hibernate.connection.username", username);
         properties.put("hibernate.connection.password", password);
         return Persistence.createEntityManagerFactory("dcm4che-jdbc-prefs", properties);
     }
 
     @Override
     public void insertNode(Node node) {
         em.getTransaction().begin();
         em.persist(node);
         em.getTransaction().commit();
     }
 
     @Override
     public void removeNode(Node node) {
         em.getTransaction().begin();
         em.createNamedQuery(Attribute.DELETE_BY_NODE).setParameter(1, node).executeUpdate();
         em.remove(node);
         em.getTransaction().commit();
     }
 
     @Override
     public void updateAttribute(Attribute attribute) {
         em.getTransaction().begin();
         em.persist(attribute);
         em.getTransaction().commit();
     }
 
     @Override
     public void removeAttributeByKey(String key, Node node) {
         em.getTransaction().begin();
         em.createNamedQuery(Attribute.DELETE_BY_KEY).setParameter(1, key).setParameter(2, node).executeUpdate();
         em.getTransaction().commit();
     }
 
     @Override
     public Node getNodeByName(String name) {
         em.getTransaction().begin();
         try {
             Node result = em.createNamedQuery(Node.GET_NODE_BY_NAME, Node.class).setParameter(1, name)
                     .getSingleResult();
             return result;
         } catch (NoResultException e) {
             return new Node();
         } catch (Exception e) {
             throw new RuntimeException(e);
         } finally {
             em.getTransaction().commit();
         }
     }
 
     @Override
     public List<Node> getChildren(Node parent) {
         em.getTransaction().begin();
         List<Node> result = em.createNamedQuery(Node.GET_CHILDREN, Node.class).setParameter(1, parent).getResultList();
         em.getTransaction().commit();
         return result;
     }
 
     @Override
     public List<Attribute> getAttributes(Node parent) {
         em.getTransaction().begin();
         List<Attribute> result = em.createNamedQuery(Attribute.GET_ATTRIBUTE_BY_PARENT_NODE, Attribute.class)
                 .setParameter(1, parent).getResultList();
         em.getTransaction().commit();
         return result;
     }
 
     @Override
     public void flush() {
         try {
             em.getTransaction().begin();
             em.flush();
             em.getTransaction().commit();
         } catch (Exception e) {
             LOG.debug(e);
         }
     }
 
     @Override
     public void refresh(Node node) {
         em.getTransaction().begin();
         em.refresh(node);
         em.getTransaction().commit();
     }
 }
