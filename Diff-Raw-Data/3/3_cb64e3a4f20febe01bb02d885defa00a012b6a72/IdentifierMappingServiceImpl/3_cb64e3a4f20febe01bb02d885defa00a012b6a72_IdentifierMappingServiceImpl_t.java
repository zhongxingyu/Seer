 /**
  * This file is part of Jahia, next-generation open source CMS:
  * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
  * of enterprise application convergence - web, search, document, social and portal -
  * unified by the simplicity of web content management.
  *
  * For more information, please visit http://www.jahia.com.
  *
  * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  *
  * As a special exception to the terms and conditions of version 2.0 of
  * the GPL (or any later version), you may redistribute this Program in connection
  * with Free/Libre and Open Source Software ("FLOSS") applications as described
  * in Jahia's FLOSS exception. You should have received a copy of the text
  * describing the FLOSS exception, and it is also available here:
  * http://www.jahia.com/license
  *
  * Commercial and Supported Versions of the program (dual licensing):
  * alternatively, commercial and supported versions of the program may be used
  * in accordance with the terms and conditions contained in a separate
  * written agreement between you and Jahia Solutions Group SA.
  *
  * If you are unsure which license is appropriate for your use,
  * please contact the sales department at sales@jahia.com.
  */
 package org.jahia.modules.external.id;
 
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.*;
 import org.jahia.exceptions.JahiaInitializationException;
 import org.jahia.exceptions.JahiaRuntimeException;
 import org.jahia.modules.external.IdentifierMappingService;
 import org.jahia.services.cache.Cache;
 import org.jahia.services.cache.CacheFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.jcr.RepositoryException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.UUID;
 
 /**
  * Node identifier mapping service for external content for maintaining internal to external ID mappings and provider IDs.
  *
  * @author Sergiy Shyrkov
  */
 public class IdentifierMappingServiceImpl implements IdentifierMappingService {
 
     private static final String ID_CACHE_NAME = "ExternalIdentifierMapping";
 
     private static final Logger logger = LoggerFactory.getLogger(IdentifierMappingServiceImpl.class);
 
     private SessionFactory hibernateSessionFactory;
 
     // The ID mapping cache, where a key is a <providerKey>-<externalId-hashCode> and a value is
     // the corresponding internalId
     private Cache<String, String> idCache;
 
     @Override
     public void delete(List<String> externalIds, String providerKey, boolean includeDescendats)
             throws RepositoryException {
         if (externalIds.isEmpty()) {
             return;
         }
         StatelessSession session = null;
         try {
             List<Integer> hashes = new LinkedList<Integer>();
             for (String externalId : externalIds) {
                 int hash = externalId.hashCode();
                 hashes.add(hash);
                 invalidateCache(hash, providerKey);
             }
             session = hibernateSessionFactory.openStatelessSession();
             session.beginTransaction();
 
             // delete all
             session.createQuery(
                     "delete from UuidMapping where providerKey=:providerKey and externalIdHash in (:externalIds)")
                     .setString("providerKey", providerKey).setParameterList("externalIds", hashes).executeUpdate();
 
             if (includeDescendats) {
                 // delete descendants
                 Query deleteStmt = session.createQuery(
                         "delete from UuidMapping where providerKey=? and externalId like ?").setString(0, providerKey);
                 for (String externalId : externalIds) {
                     deleteStmt.setString(1, externalId + "/%");
                     deleteStmt.executeUpdate();
                     // TODO how to invalidate cache here?
                 }
             }
 
             session.getTransaction().commit();
         } catch (HibernateException e) {
             if (session != null) {
                 session.getTransaction().rollback();
             }
             throw new RepositoryException(e);
         } finally {
             if (session != null) {
                 session.close();
             }
         }
     }
 
     protected String getCacheKey(int externalIdHash, String providerKey) {
         return providerKey + "-" + externalIdHash;
     }
 
     @Override
     public String getExternalIdentifier(String internalId) throws RepositoryException {
         String externalId = null;
         StatelessSession session = null;
         try {
             session = getHibernateSessionFactory().openStatelessSession();
             UuidMapping mapping = (UuidMapping) session.get(UuidMapping.class, internalId);
             if (mapping != null) {
                 externalId = mapping.getExternalId();
             }
         } catch (HibernateException e) {
             throw new RepositoryException(e);
         } finally {
             if (session != null) {
                 session.close();
             }
         }
 
         return externalId;
     }
 
     public SessionFactory getHibernateSessionFactory() {
         return hibernateSessionFactory;
     }
 
     public Cache<String, String> getIdentifierCache() {
         if (idCache == null) {
             try {
                 idCache = CacheFactory.getInstance().getCache(ID_CACHE_NAME, true);
             } catch (JahiaInitializationException e) {
                 logger.error(e.getMessage(), e);
                 throw new JahiaRuntimeException(e);
             }
         }
 
         return idCache;
 
     }
 
     @Override
     public String getInternalIdentifier(String externalId, String providerKey) throws RepositoryException {
         int hash = externalId.hashCode();
         String cacheKey = getCacheKey(hash, providerKey);
         String uuid = getIdentifierCache().get(cacheKey);
         if (uuid == null) {
             StatelessSession session = null;
             try {
                 session = getHibernateSessionFactory().openStatelessSession();
                 session.beginTransaction();
                 List<?> list = session.createQuery("from UuidMapping where providerKey=? and externalIdHash=?")
                         .setString(0, providerKey).setLong(1, hash).setReadOnly(true).list();
                 if (list.size() > 0) {
                     uuid = ((UuidMapping) list.get(0)).getInternalUuid();
                     getIdentifierCache().put(cacheKey, uuid);
                 }
                 session.getTransaction().commit();
             } catch (Exception e) {
                if (session != null) {
                    session.getTransaction().rollback();
                }
                 throw new RepositoryException(e);
             } finally {
                 if (session != null) {
                     session.close();
                 }
             }
         }
 
         return uuid;
     }
 
     @Override
     public Integer getProviderId(String providerKey) throws RepositoryException {
         ExternalProviderID providerId = null;
         SessionFactory hibernateSession = getHibernateSessionFactory();
         Session session = null;
         try {
             session = hibernateSession.openSession();
             List<?> list = session.createQuery("from ExternalProviderID where providerKey=?").setString(0, providerKey)
                     .setReadOnly(true).setFlushMode(FlushMode.MANUAL).list();
             if (list.size() > 0) {
                 providerId = (ExternalProviderID) list.get(0);
             } else {
                 // not registered yet -> generate ID and store it
                 providerId = new ExternalProviderID();
                 providerId.setProviderKey(providerKey);
                 try {
                     session.beginTransaction();
                     session.save(providerId);
                     session.getTransaction().commit();
                 } catch (HibernateException e) {
                     session.getTransaction().rollback();
                     throw new RepositoryException("Issue when storing external provider ID for provider " + providerId,
                             e);
                 }
             }
         } catch (HibernateException e) {
             throw new RepositoryException("Issue when obtaining external provider ID for provider " + providerId, e);
         } finally {
             if (session != null) {
                 session.close();
             }
         }
 
         return providerId.getId();
     }
 
     public void invalidateCache(int externalIdHash, String providerKey) {
         getIdentifierCache().remove(getCacheKey(externalIdHash, providerKey));
     }
 
     public void invalidateCache(String externalId, String providerKey) {
         invalidateCache(externalId.hashCode(), providerKey);
     }
 
     @Override
     public String mapInternalIdentifier(String externalId, String providerKey, String providerId)
             throws RepositoryException {
         UuidMapping uuidMapping = new UuidMapping();
         uuidMapping.setExternalId(externalId);
         uuidMapping.setProviderKey(providerKey);
         uuidMapping.setInternalUuid(providerId + "-" + StringUtils.substringAfter(UUID.randomUUID().toString(), "-"));
         org.hibernate.Session session = null;
         try {
             session = getHibernateSessionFactory().openSession();
             session.beginTransaction();
             session.save(uuidMapping);
             session.getTransaction().commit();
 
             // cache it
             getIdentifierCache().put(getCacheKey(externalId.hashCode(), providerKey), uuidMapping.getInternalUuid());
         } catch (HibernateException e) {
             if (session != null) {
                 session.getTransaction().rollback();
             }
             throw new RepositoryException("Error storing mapping for external node " + externalId + " [provider: "
                     + providerKey + "]", e);
         } finally {
             if (session != null) {
                 session.close();
             }
         }
 
         return uuidMapping.getInternalUuid();
     }
 
     @Override
     public void removeProvider(String providerKey) throws RepositoryException {
         SessionFactory hibernateSession = getHibernateSessionFactory();
         StatelessSession session = null;
         try {
             session = hibernateSession.openStatelessSession();
             session.beginTransaction();
             int deletedCount = session.createQuery("delete from ExternalProviderID where providerKey=?")
                     .setString(0, providerKey).executeUpdate();
             if (deletedCount > 0) {
                 logger.info("Deleted external provider entry for key {}", providerKey);
                 deletedCount = session.createQuery("delete from UuidMapping where providerKey=?")
                         .setString(0, providerKey).executeUpdate();
                 logger.info("Deleted {} identifier mapping entries for external provider with key {}", deletedCount,
                         providerKey);
             } else {
                 logger.info("No external provider entry found for key {}", providerKey);
             }
             session.getTransaction().commit();
         } catch (HibernateException e) {
             if (session != null) {
                 session.getTransaction().rollback();
             }
             throw new RepositoryException(
                     "Issue when removing external provider entry and identifier mappings for provider key "
                             + providerKey, e);
         } finally {
             if (session != null) {
                 session.close();
             }
         }
     }
 
     public void setHibernateSessionFactory(SessionFactory hibernateSession) {
         this.hibernateSessionFactory = hibernateSession;
     }
 
     @Override
     public void updateExternalIdentifier(String oldExternalId, String newExternalId, String providerKey,
                                          boolean includeDescendats) throws RepositoryException {
         Session session = null;
         try {
             session = getHibernateSessionFactory().openSession();
             session.beginTransaction();
             List<?> list = session.createQuery("from UuidMapping where providerKey=? and externalIdHash=?")
                     .setString(0, providerKey).setLong(1, oldExternalId.hashCode()).list();
             if (list.size() > 0) {
                 for (Object mapping : list) {
                     ((UuidMapping) mapping).setExternalId(newExternalId);
                     invalidateCache(oldExternalId, providerKey);
                 }
             }
             if (includeDescendats) {
                 // update descendants
                 List<?> descendants = session.createQuery("from UuidMapping where providerKey=? and externalId like ?")
                         .setString(0, providerKey).setString(1, oldExternalId + "/%").list();
                 for (Object mapping : descendants) {
                     UuidMapping m = (UuidMapping) mapping;
                     m.setExternalId(newExternalId + StringUtils.substringAfter(m.getExternalId(), oldExternalId));
                     invalidateCache(m.getExternalIdHash(), providerKey);
                 }
             }
             session.getTransaction().commit();
         } catch (HibernateException e) {
             if (session != null) {
                 session.getTransaction().rollback();
             }
             throw new RepositoryException(e);
         } finally {
             if (session != null) {
                 session.close();
             }
         }
     }
 }
