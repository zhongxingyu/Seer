 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.st.business.persistence.hibernate;
 
 import de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException;
 import de.escidoc.core.st.business.StagingFile;
 import de.escidoc.core.st.business.persistence.StagingFileDao;
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.dao.DataAccessException;
 import org.springframework.dao.DataAccessResourceFailureException;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 
 import java.util.List;
 
 /**
  * Implementation of a staging file data access object (DAO).
  * 
  * @author TTE
  * @spring.bean id="persistence.StagingFileDao"
  */
 public class HibernateStagingFileDao extends HibernateDaoSupport
     implements StagingFileDao {
 
     /**
      * Wrapper of setSessionFactory to enable bean stuff generation for this
      * bean.
      * 
      * @param stagingSessionFactory
      *            The sessionFactory to set.
      * @spring.property ref="st.SessionFactory"
      */
     public final void setStagingSessionFactory(
         final SessionFactory stagingSessionFactory) {
 
         super.setSessionFactory(stagingSessionFactory);
     }
 
     // CHECKSTYLE:JAVADOC-OFF
 
     /**
      * See Interface for functional description.
      * 
      * @param token
      * @return
      * @see StagingFileDao#findStagingFile(java.lang.String)
      */
     public StagingFile findStagingFile(final String token)
         throws SqlDatabaseSystemException {
 
         try {
             Session session = getSession(false);
             Criteria criteria = session.createCriteria(StagingFile.class);
             criteria.add(Restrictions.eq("token", token));
            return (StagingFile) criteria.uniqueResult();
         }
         catch (DataAccessResourceFailureException e) {
             throw new SqlDatabaseSystemException(e);
         }
         catch (HibernateException e) {
             throw new SqlDatabaseSystemException(
                 convertHibernateAccessException(e));
         }
         catch (IllegalStateException e) {
             throw new SqlDatabaseSystemException(e);
         }
 
     }
 
     /**
      * See Interface for functional description.
      * 
      * @return
      * @see StagingFileDao#findExpiredStagingFiles()
      */
     public List<StagingFile> findExpiredStagingFiles()
         throws SqlDatabaseSystemException {
 
         try {
             DetachedCriteria criteria =
                 DetachedCriteria.forClass(StagingFile.class);
             criteria.add(Restrictions.lt("expiryTs", Long.valueOf(System
                 .currentTimeMillis())));
             return getHibernateTemplate().findByCriteria(criteria);
         }
         catch (DataAccessException e) {
             throw new SqlDatabaseSystemException(e);
         }
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param stagingFile
      * @see StagingFileDao#save(de.escidoc.core.st.business.StagingFile)
      */
     public void save(final StagingFile stagingFile)
         throws SqlDatabaseSystemException {
 
         if (stagingFile != null) {
             try {
                 getHibernateTemplate().save(stagingFile);
             }
             catch (DataAccessException e) {
                 throw new SqlDatabaseSystemException(e);
             }
         }
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param stagingFile
      * @see StagingFileDao#update(de.escidoc.core.st.business.StagingFile)
      */
     public void update(final StagingFile stagingFile)
         throws SqlDatabaseSystemException {
 
         if (stagingFile != null) {
             try {
                 getHibernateTemplate().update(stagingFile);
             }
             catch (DataAccessException e) {
                 throw new SqlDatabaseSystemException(e);
             }
         }
 
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param stagingFile
      * @see StagingFileDao#saveOrUpdate(de.escidoc.core.st.business.StagingFile)
      */
     public void saveOrUpdate(final StagingFile stagingFile)
         throws SqlDatabaseSystemException {
 
         if (stagingFile != null) {
             try {
                 getHibernateTemplate().saveOrUpdate(stagingFile);
             }
             catch (DataAccessException e) {
                 throw new SqlDatabaseSystemException(e);
             }
         }
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param stagingFile
      * @see StagingFileDao#delete(de.escidoc.core.st.business.StagingFile)
      */
     public void delete(final StagingFile stagingFile)
         throws SqlDatabaseSystemException {
 
         if (stagingFile != null) {
             try {
                 getHibernateTemplate().delete(stagingFile);
             }
             catch (DataAccessException e) {
                 throw new SqlDatabaseSystemException(e);
             }
         }
     }
 
     /**
      * See Interface for functional description.
      * 
      * @param stagingFiles
      * @see StagingFileDao#delete(de.escidoc.core.st.business.StagingFile[])
      */
     public void delete(final StagingFile[] stagingFiles)
         throws SqlDatabaseSystemException {
 
         if (stagingFiles != null) {
             try {
                 for (int i = 0; i < stagingFiles.length; i++) {
                     getHibernateTemplate().delete(stagingFiles[i]);
                 }
             }
             catch (DataAccessException e) {
                 throw new SqlDatabaseSystemException(e);
             }
         }
     }
 
 }
