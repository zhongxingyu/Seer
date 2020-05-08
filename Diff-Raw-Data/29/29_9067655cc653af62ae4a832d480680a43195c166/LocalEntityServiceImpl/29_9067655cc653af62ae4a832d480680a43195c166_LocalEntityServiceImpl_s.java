 /*
  * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     Olivier Grisel
  */
 package org.nuxeo.ecm.platform.semanticentities.service;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.core.api.DocumentRef;
 import org.nuxeo.ecm.core.api.IdRef;
 import org.nuxeo.ecm.core.api.PathRef;
 import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
 import org.nuxeo.ecm.core.api.security.ACE;
 import org.nuxeo.ecm.core.api.security.ACP;
 import org.nuxeo.ecm.core.api.security.SecurityConstants;
 import org.nuxeo.ecm.core.api.security.impl.ACLImpl;
 import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
 import org.nuxeo.ecm.core.schema.SchemaManager;
 import org.nuxeo.ecm.platform.query.api.PageProvider;
 import org.nuxeo.ecm.platform.semanticentities.Constants;
 import org.nuxeo.ecm.platform.semanticentities.DereferencingException;
 import org.nuxeo.ecm.platform.semanticentities.EntitySuggestion;
 import org.nuxeo.ecm.platform.semanticentities.LocalEntityService;
 import org.nuxeo.ecm.platform.semanticentities.RemoteEntity;
 import org.nuxeo.ecm.platform.semanticentities.RemoteEntityService;
 import org.nuxeo.ecm.platform.semanticentities.adapter.OccurrenceInfo;
 import org.nuxeo.ecm.platform.semanticentities.adapter.OccurrenceRelation;
 import org.nuxeo.runtime.api.Framework;
 import org.nuxeo.runtime.model.DefaultComponent;
 
 /**
  * Service to handle semantic entities linked to documents in the local
  * repository.
 * 
  * Relations between documents and entities are stored in the Nuxeo repository
  * and documents of type "Occurrence" which is a sub-type of the "Relation" core
  * type.
  */
 public class LocalEntityServiceImpl extends DefaultComponent implements
         LocalEntityService {
 
     public static final Log log = LogFactory.getLog(LocalEntityServiceImpl.class);
 
     // TODO: make me configurable in an extension point
     public static final String ENTITY_CONTAINER_PATH = "/entities";
 
     public static final String ENTITY_CONTAINER_TITLE = "%i18nEntities";
 
     @Override
     public DocumentModel getEntityContainer(CoreSession session)
             throws ClientException {
         final PathRef ref = new PathRef(ENTITY_CONTAINER_PATH);
         if (!session.exists(ref)) {
             // either the container has not been created yet or the current user
             // cannot see it because of a lack of permissions to do so
 
             int lastSlashIdx = ENTITY_CONTAINER_PATH.lastIndexOf('/');
             final String id = ENTITY_CONTAINER_PATH.substring(lastSlashIdx + 1);
             final String parentPath = ENTITY_CONTAINER_PATH.substring(0,
                     lastSlashIdx + 1);
 
             UnrestrictedSessionRunner runner = new UnrestrictedSessionRunner(
                     session) {
                 @Override
                 public void run() throws ClientException {
                     if (!session.exists(ref)) {
                         DocumentModel container = session.createDocumentModel(
                                 parentPath, id, Constants.ENTITY_CONTAINER_TYPE);
                         container.setPropertyValue("dc:title",
                                 ENTITY_CONTAINER_TITLE);
                         session.createDocument(container);
                         session.save();
                     }
                 }
             };
             runner.runUnrestricted();
         }
 
         if (!session.exists(ref)) {
             // the user does not have the right to see the container
             return null;
         }
         return session.getDocument(ref);
     }
 
     @Override
     public OccurrenceRelation addOccurrence(CoreSession session,
             DocumentRef docRef, DocumentRef entityRef, String quoteContext,
             int startPosInContext, int endPosInContext) throws ClientException {
         OccurrenceInfo info = new OccurrenceInfo(quoteContext,
                 startPosInContext, endPosInContext);
         return addOccurrences(session, docRef, entityRef, Arrays.asList(info));
     }
 
     @Override
     public OccurrenceRelation getOccurrenceRelation(CoreSession session,
             DocumentRef docRef, DocumentRef entityRef) throws ClientException {
         return getOccurrenceRelation(session, docRef, entityRef, false);
     }
 
     public OccurrenceRelation getOccurrenceRelation(CoreSession session,
             DocumentRef docRef, DocumentRef entityRef, boolean createIfMissing)
             throws ClientException {
         String q = String.format("SELECT * FROM Occurrence"
                 + " WHERE relation:source = '%s'"
                 + " AND relation:target = '%s'"
                 + " ORDER BY dc:created LIMIT 2", docRef, entityRef);
         DocumentModelList occurrences = session.query(q);
         if (occurrences.isEmpty()) {
             if (createIfMissing) {
                 // create an empty document model in memory and adapt it to the
                 // OccurrenceRelation interface
                 DocumentModel occ = session.createDocumentModel(Constants.OCCURRENCE_TYPE);
                 occ.setPropertyValue("relation:source", docRef.toString());
                 occ.setPropertyValue("relation:target", entityRef.toString());
                 return occ.getAdapter(OccurrenceRelation.class);
             } else {
                 return null;
             }
         } else {
             if (occurrences.size() > 1) {
                 log.warn(String.format(
                         "more than one occurrence found linking document"
                                 + " '%s' to entity '%s'", docRef, entityRef));
             }
             return occurrences.get(0).getAdapter(OccurrenceRelation.class);
         }
     }
 
     @Override
     public OccurrenceRelation addOccurrences(CoreSession session,
             DocumentRef docRef, DocumentRef entityRef,
             List<OccurrenceInfo> occurrences) throws ClientException {
         if (!session.hasPermission(docRef, Constants.ADD_OCCURRENCE_PERMISSION)) {
             // check the permission on the source document
             throw new SecurityException(String.format(
                     "%s has not the permission to add an entity"
                             + " occurrence on document with id '%s'",
                     session.getPrincipal().getName(), docRef));
         }
         OccurrenceRelation relation = getOccurrenceRelation(session, docRef,
                 entityRef, true);
         if (occurrences != null && !occurrences.isEmpty()) {
             relation.addOccurrences(occurrences);
         }
         UpdateOrCreateOccurrenceRelation op = new UpdateOrCreateOccurrenceRelation(
                 session, relation);
         op.runUnrestricted();
         return session.getDocument(op.occRef).getAdapter(
                 OccurrenceRelation.class, true);
     }
 
     protected static class UpdateOrCreateOccurrenceRelation extends
             UnrestrictedSessionRunner {
 
         protected final OccurrenceRelation relation;
 
         protected DocumentRef occRef;
 
         public UpdateOrCreateOccurrenceRelation(CoreSession session,
                 OccurrenceRelation relation) {
             super(session);
             this.relation = relation;
         }
 
         @SuppressWarnings("unchecked")
         @Override
         public void run() throws ClientException {
             // update the entity aggregated alternative names for better
             // fulltext indexing
             DocumentModel entity = session.getDocument(relation.getTargetEntityRef());
             List<String> altnames = entity.getProperty("entity:altnames").getValue(
                     List.class);
             for (OccurrenceInfo occInfo : relation.getOccurrences()) {
                 if (!occInfo.mention.equals(entity.getPropertyValue("dc:title"))) {
                     if (!altnames.contains(occInfo.mention)) {
                         altnames = new ArrayList<String>(altnames);
                         altnames.add(occInfo.mention);
                     }
                 }
             }
             entity.setPropertyValue("entity:altnames", (Serializable) altnames);
 
             if (relation.getOccurrenceDocument().getId() == null) {
                 // this is a creation of a new relation between a document and
                 // the entity
                 occRef = session.createDocument(
                         relation.getOccurrenceDocument()).getRef();
 
                 // remove ACL checks on relations
                 ACP openAcp = new ACPImpl();
                 ACLImpl acl = new ACLImpl("open", true);
                 acl.add(new ACE(SecurityConstants.EVERYONE,
                         SecurityConstants.BROWSE, true));
                 openAcp.addACL(acl);
                 session.setACP(occRef, openAcp, true);
 
                 // update the popularity estimate
                 Long newPopularity = entity.getProperty("entity:popularity").getValue(
                         Long.class) + 1;
                 entity.setPropertyValue("entity:popularity", newPopularity);
 
             } else {
                 // this is an update of an existing relation
                 occRef = session.saveDocument(relation.getOccurrenceDocument()).getRef();
 
             }
             session.saveDocument(entity);
             session.save();
         }
     }
 
     @Override
     public PageProvider<DocumentModel> getRelatedDocuments(CoreSession session,
             DocumentRef entityRef, String documentType) throws ClientException {
         if (documentType == null) {
             documentType = "cmis:document";
         }
         if (!(entityRef instanceof IdRef)) {
             throw new NotImplementedException(
                     "Only IdRef instance are currently supported, got "
                             + entityRef.getClass().getName());
         }
         String query = String.format(
                 "SELECT Doc.cmis:objectId FROM %s Doc "
                         + "JOIN Occurrence Occ ON Occ.relation:source = Doc.cmis:objectId "
                         + "WHERE Occ.relation:target = '%s' "
                         + "ORDER BY Doc.dc:modified DESC", documentType,
                 entityRef);
         return new CMISQLDocumentPageProvider(session, query,
                 "Doc.cmis:objectId", "relatedDocuments");
     }
 
     @Override
     public PageProvider<DocumentModel> getRelatedEntities(CoreSession session,
             DocumentRef docRef, String entityType) throws ClientException {
         if (entityType == null) {
             entityType = Constants.ENTITY_TYPE;
         }
         if (!(docRef instanceof IdRef)) {
             throw new NotImplementedException(
                     "Only IdRef instance are currently supported, got "
                             + docRef.getClass().getName());
         }
 
         // order by number of incoming links instead?
         String query = String.format(
                 "SELECT Ent.cmis:objectId FROM %s Ent "
                         + "JOIN Occurrence Occ ON Occ.relation:target = Ent.cmis:objectId "
                         + "WHERE Occ.relation:source = '%s' "
                         + "ORDER BY Ent.dc:title", entityType, docRef);
         return new CMISQLDocumentPageProvider(session, query,
                 "Ent.cmis:objectId", "relatedEntities");
     }
 
     @Override
     public List<DocumentModel> suggestLocalEntity(CoreSession session,
             String keywords, String type, int maxSuggestions)
             throws ClientException {
         if (type == null) {
             type = Constants.ENTITY_TYPE;
         }
         String q = String.format("SELECT * FROM %s WHERE ecm:fulltext = '%s'"
                 + " ORDER BY entity:popularity DESC, dc:title" + " LIMIT %d",
                 type, keywords.replace("'", "\'"), maxSuggestions);
         return session.query(q);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<EntitySuggestion> suggestEntity(CoreSession session,
             String keywords, String type, int maxSuggestions)
             throws ClientException, DereferencingException {
         // lookup remote entities
         List<RemoteEntity> remoteEntities = Collections.emptyList();
         RemoteEntityService reService;
         try {
             reService = Framework.getService(RemoteEntityService.class);
             if (reService.canSuggestRemoteEntity()) {
                 remoteEntities = reService.suggestRemoteEntity(keywords, type,
                         maxSuggestions);
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         // lookup local entities
         List<DocumentModel> localEntities = suggestLocalEntity(session,
                 keywords, type, maxSuggestions);
 
         // TODO: rewrite the following to perform local checks of remote
         // entities in the complete local DB and not just the partial suggested
         // results
 
         List<EntitySuggestion> suggestions = new ArrayList<EntitySuggestion>();
         double invScoreLocal = 10.0;
         Set<RemoteEntity> mergedRemoteEntities = new HashSet<RemoteEntity>();
         for (DocumentModel localEntity : localEntities) {
             EntitySuggestion suggestion = new EntitySuggestion(localEntity).withScore(1 / invScoreLocal);
             suggestions.add(suggestion);
             List<String> sameas = localEntity.getProperty("entity:sameas").getValue(
                     List.class);
             if (sameas == null) {
                 sameas = Collections.emptyList();
             }
             double invScoreRemote = 2.0;
             for (RemoteEntity remoteEntity : remoteEntities) {
                 if (sameas.contains(remoteEntity.getUri().toString())) {
                     suggestion.remoteEntityUris.add(remoteEntity.uri.toString());
                     suggestion.score += 1 / invScoreRemote;
                     mergedRemoteEntities.add(remoteEntity);
                 }
                 invScoreRemote += 1.0;
             }
             invScoreLocal += 1.0;
         }
 
         remoteEntities.removeAll(mergedRemoteEntities);
         double invScoreRemote = 2.0;
         for (RemoteEntity remoteEntity : remoteEntities) {
             EntitySuggestion suggestion = new EntitySuggestion(
                     remoteEntity.label, remoteEntity.uri.toString(), type).withScore(1 / invScoreRemote);
             // TODO: optimize me and change the source suggestion API to fetch
             // admissible types up-front instead
             Set<String> types = reService.getAdmissibleTypes(remoteEntity.uri);
 
             // quick hack to filter out entities with overly generic types (such
             // as Entity) or entities without any admissible local types
             if (types.size() > 1 && types.contains(Constants.ENTITY_TYPE)) {
                 types.remove(Constants.ENTITY_TYPE);
             }
             if (types.size() > 0) {
                 suggestion.type = types.iterator().next();
                 suggestions.add(suggestion);
             }
             invScoreRemote += 1.0;
         }
         Collections.sort(suggestions);
         Collections.reverse(suggestions);
         return suggestions;
     }
 
     @Override
     public List<DocumentModel> suggestDocument(CoreSession session,
             String keywords, String type, int maxSuggestions) throws Exception {
         if (type == null) {
             type = "cmis:document";
         }
         String query = String.format(
                 "SELECT cmis:objectId, SCORE() relevance FROM %s "
                         + "WHERE CONTAINS('%s') AND cmis:objectTypeId NOT IN ('%s') "
                         + "ORDER BY relevance", type,
                 keywords.replace("'", " "),
                 StringUtils.join(getEntityTypeNames(), "', '"));
         PageProvider<DocumentModel> provider = new CMISQLDocumentPageProvider(
                 session, query, "cmis:objectId", "suggestedDocuments");
         provider.setPageSize(maxSuggestions);
         return provider.getCurrentPage();
     }
 
     @Override
     public Set<String> getEntityTypeNames() throws Exception {
         return Framework.getService(SchemaManager.class).getDocumentTypeNamesExtending(
                 Constants.ENTITY_TYPE);
     }
 
     @Override
     public DocumentModel getLinkedLocalEntity(CoreSession session,
             URI remoteEntityURI) throws ClientException {
         String query = String.format("SELECT cmis:objectId FROM Entity "
                 + "WHERE '%s' = ANY entity:sameas ORDER BY dc:created",
                 remoteEntityURI.toString());
         PageProvider<DocumentModel> provider = new CMISQLDocumentPageProvider(
                 session, query, "cmis:objectId", "linkedEntities");
         provider.setPageSize(1);
         List<DocumentModel> currentPage = provider.getCurrentPage();
         long count = provider.getResultsCount();
         if (count == 0) {
             return null;
         } else if (count > 1) {
             log.warn(String.format(
                     "semantic inconsistency: found %d local entities linked to '%s'",
                     count, remoteEntityURI));
         }
         return currentPage.get(0);
     }
 
     @Override
     public DocumentModel asLocalEntity(CoreSession session,
             EntitySuggestion suggestion) throws ClientException, IOException {
         if (suggestion.isLocal()) {
             return suggestion.localEntity;
         } else if (suggestion.remoteEntityUris.isEmpty()) {
             throw new IllegalArgumentException(
                     "The provided suggestion has neither local"
                             + " entity nor emote entities links");
         }
 
         RemoteEntityService reService;
         try {
             reService = Framework.getService(RemoteEntityService.class);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         DocumentModel entityContainer = getEntityContainer(session);
 
         DocumentModel localEntity = session.createDocumentModel(
                 entityContainer.getPathAsString(), suggestion.label,
                 suggestion.type);
 
         for (String remoteEntity : suggestion.remoteEntityUris) {
             URI uri = URI.create(remoteEntity);
             reService.dereferenceInto(localEntity, uri, false);
         }
         localEntity = session.createDocument(localEntity);
         session.save();
         return localEntity;
     }
 
 }
