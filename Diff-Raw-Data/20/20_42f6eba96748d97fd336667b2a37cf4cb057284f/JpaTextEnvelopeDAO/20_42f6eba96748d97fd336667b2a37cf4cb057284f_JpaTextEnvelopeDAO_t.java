 /*
  *   Copyright 2011-2012 George Norman
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package com.thruzero.domain.jpa.dao;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import com.thruzero.common.core.support.ContainerPath;
 import com.thruzero.common.core.support.EntityPath;
 import com.thruzero.common.core.support.StrBuilderExt;
 import com.thruzero.domain.dao.TextEnvelopeDAO;
 import com.thruzero.domain.jpa.utils.JpaUtils;
 import com.thruzero.domain.model.TextEnvelope;
 
 /**
  * An implementation of TextEnvelopeDAO that uses JPA for storage.
  *
  * @author George Norman
  */
 public class JpaTextEnvelopeDAO extends JpaGenericDAO<TextEnvelope> implements TextEnvelopeDAO {
 
  /**
   * Allow for class extensions; disallow client instantiation (use {@link com.thruzero.domain.locator.DAOLocator
   * DAOLocator} to access a particular DAO)
   */
  protected JpaTextEnvelopeDAO() {
    super(TextEnvelope.class);
  }

   @Override
   public boolean isExistingTextEnvelope(EntityPath entityPath) {
     EntityManager entityManager = getCurrentPersistenceManager();
 
     StrBuilderExt hql = new StrBuilderExt(100);
     hql.append("SELECT COUNT(textEnvelope) FROM TextEnvelope textEnvelope");
     hql.append("  WHERE textEnvelope.entityPath.containerPath.path = :path ");
     hql.append("    AND textEnvelope.entityPath.entityName = :entityName ");
 
     Query hqlQuery = entityManager.createQuery(hql.toString());
     hqlQuery.setParameter("path", entityPath.getContainerPath().getPath());
     hqlQuery.setParameter("entityName", entityPath.getEntityName());
 
     Long result = JpaUtils.getSingleResultHack(hqlQuery);
 
     return result != null && result > 0;
   }
 
   @Override
   public List<? extends TextEnvelope> getTextEnvelopes(final ContainerPath containerPath, final boolean recursive) { // TODO-p1(george) implement recursive
     EntityManager entityManager = getCurrentPersistenceManager();
 
     StrBuilderExt hql = new StrBuilderExt(100);
     hql.append(" FROM TextEnvelope textEnvelope ");
    hql.append("  WHERE textEnvelope.entityPath.containerPath.path = :path ");
 
     Query hqlQuery = entityManager.createQuery(hql.toString());
     hqlQuery.setParameter("path", containerPath.getPath());
 
     @SuppressWarnings("unchecked")
     List<? extends TextEnvelope> result = hqlQuery.getResultList();
 
     return result;
   }
 
   @Override
   public TextEnvelope getTextEnvelope(EntityPath entityPath) {
     EntityManager entityManager = getCurrentPersistenceManager();
 
     StrBuilderExt hql = new StrBuilderExt(100);
     hql.append(" FROM TextEnvelope textEnvelope ");
     hql.append("  WHERE textEnvelope.entityPath.containerPath.path = :path ");
     hql.append("    AND textEnvelope.entityPath.entityName = :entityName ");
 
     Query hqlQuery = entityManager.createQuery(hql.toString());
     hqlQuery.setParameter("path", entityPath.getContainerPath().getPath());
     hqlQuery.setParameter("entityName", entityPath.getEntityName());
 
     TextEnvelope result = JpaUtils.getSingleResultHack(hqlQuery);
 
     return result;
   }
 }
