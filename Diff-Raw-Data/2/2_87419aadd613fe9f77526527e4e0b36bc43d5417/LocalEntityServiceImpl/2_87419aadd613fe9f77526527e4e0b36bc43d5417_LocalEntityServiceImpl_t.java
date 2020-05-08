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
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.UUID;
 import java.util.concurrent.TimeUnit;
 
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
 import org.nuxeo.ecm.core.api.pathsegment.PathSegmentService;
 import org.nuxeo.ecm.core.api.security.ACE;
 import org.nuxeo.ecm.core.api.security.ACL;
 import org.nuxeo.ecm.core.api.security.ACP;
 import org.nuxeo.ecm.core.api.security.SecurityConstants;
 import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
 import org.nuxeo.ecm.core.schema.SchemaManager;
 import org.nuxeo.ecm.core.trash.TrashService;
 import org.nuxeo.ecm.platform.query.api.PageProvider;
 import org.nuxeo.ecm.platform.semanticentities.Constants;
 import org.nuxeo.ecm.platform.semanticentities.DereferencingException;
 import org.nuxeo.ecm.platform.semanticentities.EntitySuggestion;
 import org.nuxeo.ecm.platform.semanticentities.LocalEntityService;
 import org.nuxeo.ecm.platform.semanticentities.RemoteEntityService;
 import org.nuxeo.ecm.platform.semanticentities.adapter.OccurrenceGroup;
 import org.nuxeo.ecm.platform.semanticentities.adapter.OccurrenceInfo;
 import org.nuxeo.ecm.platform.semanticentities.adapter.OccurrenceRelation;
 import org.nuxeo.runtime.api.Framework;
 import org.nuxeo.runtime.model.DefaultComponent;
 
 import com.google.common.collect.MapMaker;
 
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
     public static final String ENTITY_CONTAINER_PATH = "/default-domain/entities";
 
     public static final String ENTITY_CONTAINER_TITLE = "Entities";
 
     protected Map<String, DocumentRef> recentlyDereferenced = new MapMaker().concurrencyLevel(
             4).expiration(5, TimeUnit.SECONDS).makeMap();
 
     protected Map<DocumentRef, String> progressMessages = new MapMaker().concurrencyLevel(
             4).expiration(30, TimeUnit.MINUTES).makeMap();
 
     @Override
     synchronized public DocumentModel getEntityContainer(CoreSession session)
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
 
                         // create the entity container
                         DocumentModel entityContainer = session.createDocumentModel(
                                 parentPath, id, Constants.ENTITY_CONTAINER_TYPE);
                         entityContainer.setPropertyValue("dc:title",
                                 ENTITY_CONTAINER_TITLE);
 
                         DocumentModel createdContainer = session.createDocument(entityContainer);
                         if (!ENTITY_CONTAINER_PATH.equals(createdContainer.getPathAsString())) {
                             // container concurrent creation in a race
                             // condition: delete it
                             session.removeDocument(createdContainer.getRef());
                         } else {
                             // create the occurrence container
                             String parentPath = entityContainer.getPathAsString();
 
                             DocumentModel occurrenceContainer = session.createDocumentModel(
                                     parentPath, "occurrences",
                                     "OccurrenceContainer");
                             occurrenceContainer = session.createDocument(occurrenceContainer);
 
                             // put a single ACL that will be inherited for all
                             // occurrences
                             ACP openAcp = new ACPImpl();
                             ACL acl = openAcp.getOrCreateACL();
                             acl.add(new ACE("members", SecurityConstants.WRITE,
                                     true));
                             acl.add(new ACE(SecurityConstants.EVERYONE,
                                     SecurityConstants.BROWSE, true));
                             openAcp.addACL(acl);
                             session.setACP(occurrenceContainer.getRef(),
                                     openAcp, true);
                             session.save();
                         }
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
 
     public DocumentModel getOccurrenceContainer(CoreSession session)
             throws ClientException {
         DocumentModel entityContainer = getEntityContainer(session);
         String parentPath = entityContainer.getPathAsString();
         String localId = "occurrences";
         DocumentRef occurrencesRef = new PathRef(parentPath + "/" + localId);
         return session.getDocument(occurrencesRef);
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
 
     @Override
     public void removeOccurrences(CoreSession session, DocumentRef docRef,
             final DocumentRef entityRef, final boolean forcePhysicalDelete)
             throws ClientException {
         OccurrenceRelation rel = getOccurrenceRelation(session, docRef,
                 entityRef, false);
         if (rel == null) {
             return;
         }
         // mark the relation document for deletion
         DocumentModel relDoc = rel.getOccurrenceDocument();
         final List<DocumentRef> docToDelete = new ArrayList<DocumentRef>();
         docToDelete.add(relDoc.getRef());
 
         // find the linked entity to check whether it was automatically created
         // by the system
         PageProvider<DocumentModel> relatedDocuments = getRelatedDocuments(
                 session, entityRef, null);
         if (relatedDocuments.getCurrentPage().size() == 1
                 && relatedDocuments.getCurrentEntry().getRef().equals(docRef)) {
             // only related to the current document
             DocumentModel entity = session.getDocument(entityRef);
             Boolean auto = entity.getProperty("entity:automaticallyCreated").getValue(
                     Boolean.class);
             @SuppressWarnings("unchecked")
             List<String> contributors = entity.getProperty("dc:contributors").getValue(
                     List.class);
             if (auto && contributors.size() <= 1) {
                 // remove the automatically created entity as well
                 docToDelete.add(entity.getRef());
             }
         }
         final DocumentRef[] docToDeleteArray = docToDelete.toArray(new DocumentRef[docToDelete.size()]);
         UnrestrictedSessionRunner runner = new UnrestrictedSessionRunner(
                 session) {
 
             @Override
             public void run() throws ClientException {
                 if (!docToDelete.contains(entityRef)) {
                     // update the popularity count of the entity
                     DocumentModel entity = session.getDocument(entityRef);
                     Long newPopularity = entity.getProperty("entity:popularity").getValue(
                             Long.class) - 1;
                     entity.setPropertyValue("entity:popularity", newPopularity);
                     session.saveDocument(entity);
                 }
 
                 if (forcePhysicalDelete) {
                     session.removeDocuments(docToDeleteArray);
                 } else {
                     try {
                         // try to perform the actual deletion using the trash service
                         TrashService trashService = Framework.getService(TrashService.class);
                         trashService.trashDocuments(session.getDocuments(docToDeleteArray));
                     } catch (Exception e) {
                         // the trash service is not deployed
                         session.removeDocuments(docToDeleteArray);
                     }
                 }
             }
         };
         runner.runUnrestricted();
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
 
                 // use a subcontainer of the entity container document as
                 // parent to avoid having to put read ACLs on occurrence that
                 // are not behaving correctly concurrently
                 DocumentModel occ = session.createDocumentModel(
                         getOccurrenceContainer(session).getPathAsString(),
                         "occurrence-" + UUID.randomUUID().toString(),
                         Constants.OCCURRENCE_TYPE);
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
     public void addOccurrences(CoreSession session, DocumentRef docRef,
             EntitySuggestion entitySuggestion, List<OccurrenceInfo> occurrences)
             throws ClientException, IOException {
 
         DocumentRef entityRef = null;
         if (entitySuggestion.isLocal()) {
             entityRef = entitySuggestion.entity.getRef();
         } else {
             // use the recentlyDereferenced cache that is shared among threads
             // and concurrent transaction to avoid dereferencing the same remote
             // entity to duplicated local entities
             for (String uri : entitySuggestion.remoteEntityUris) {
                 entityRef = recentlyDereferenced.get(uri);
                 if (entityRef != null) {
                     break;
                 }
             }
             if (entityRef == null) {
                 entityRef = asLocalEntity(session, entitySuggestion).getRef();
             }
         }
         addOccurrences(session, docRef, entityRef, occurrences);
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
             DocumentModel entity = null;
             try {
                 entity = session.getDocument(relation.getTargetEntityRef());
             } catch (ClientException e) {
                 // There is a potential race condition if two users deference
                 // the same entity exactly at the same time.
 
                 // Will assume that the problem does not occur to often in
                 // practice so that the popularity score and the alternative
                 // names update discrepancies are not a major issue
             }
             if (entity != null) {
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
                 entity.setPropertyValue("entity:altnames",
                         (Serializable) altnames);
             }
 
             if (relation.getOccurrenceDocument().getId() == null) {
                 // this is a creation of a new relation between a document and
                 // the entity
                 occRef = session.createDocument(
                         relation.getOccurrenceDocument()).getRef();
 
                 if (entity != null) {
                     // update the popularity estimate
                     Long newPopularity = entity.getProperty("entity:popularity").getValue(
                             Long.class) + 1;
                     entity.setPropertyValue("entity:popularity", newPopularity);
                 }
             } else {
                 // this is an update of an existing relation
                 occRef = session.saveDocument(relation.getOccurrenceDocument()).getRef();
             }
             if (entity != null) {
                 session.saveDocument(entity);
             }
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
 
     public static String cleanupKeywords(String keywords) {
         return keywords.replaceAll("(\\p{P}|\\n| |<|>|\\+|\\-)+", " ").trim();
     }
 
     @Override
     public List<EntitySuggestion> suggestLocalEntity(CoreSession session,
             String keywords, String type, int maxSuggestions)
             throws ClientException {
         Set<String> entityTypeNames = new TreeSet<String>();
         if (type == null) {
             try {
                 entityTypeNames = getEntityTypeNames();
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
             entityTypeNames.remove(Constants.ENTITY_TYPE);
         } else {
             entityTypeNames.add(type);
         }
 
         String q = String.format(
                 "SELECT * FROM %s WHERE ecm:fulltext_title = '%s'"
                         + " AND ecm:primaryType IN ('%s')"
                         + " AND ecm:currentLifeCycleState != 'deleted'"
                         + " AND ecm:isCheckedInVersion = 0"
                         + " ORDER BY entity:popularity DESC, dc:title LIMIT %d",
                 Constants.ENTITY_TYPE, cleanupKeywords(keywords),
                 StringUtils.join(entityTypeNames, "', '"), maxSuggestions);
 
         // TODO: read the score info as well
         List<EntitySuggestion> suggestions = new ArrayList<EntitySuggestion>();
         for (DocumentModel doc: session.query(q)) {
             suggestions.add(new EntitySuggestion(doc));
         }
         return suggestions;
     }
 
     @Override
     public List<EntitySuggestion> suggestEntity(CoreSession session,
             OccurrenceGroup group, int maxSuggestions)
             throws DereferencingException, ClientException {
         if (group.hasPrefetchedSuggestions()) {
             return suggestEntity(session, group.name, group.type,
                     group.entitySuggestions, maxSuggestions);
         } else {
             return suggestEntity(session, group.name, group.type, null,
                     maxSuggestions);
         }
     }
 
     @Override
     public List<EntitySuggestion> suggestEntity(CoreSession session,
             String keywords, String type, int maxSuggestions)
             throws ClientException, DereferencingException {
         return suggestEntity(session, keywords, type, null,
                 maxSuggestions);
     }
 
     protected List<EntitySuggestion> suggestEntity(CoreSession session,
             String keywords, String type,
             List<EntitySuggestion> precomputedRemoteSuggestions,
             int maxSuggestions) throws ClientException, DereferencingException {
         // lookup remote entities
         RemoteEntityService reService;
         try {
             reService = Framework.getService(RemoteEntityService.class);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         List<EntitySuggestion> remoteSuggestions = precomputedRemoteSuggestions;
         if (remoteSuggestions == null) {
             if (reService.canSuggestRemoteEntity()) {
                 try {
                     remoteSuggestions = reService.suggestRemoteEntity(keywords,
                             type, maxSuggestions);
                 } catch (IOException e) {
                     log.warn(String.format(
                             "failed to suggest remote entity for '%s' with type '%s': %s",
                             keywords, type, e.getMessage()));
                     remoteSuggestions = Collections.emptyList();
                 }
             } else {
                 remoteSuggestions = Collections.emptyList();
             }
         }
         // lookup local entities
         List<EntitySuggestion> suggestions = suggestLocalEntity(session,
                 keywords, type, maxSuggestions);
         Set<String> alreadySeenRemoteUris = new HashSet<String>();
         for (EntitySuggestion suggestion: suggestions) {
             alreadySeenRemoteUris.addAll(suggestion.remoteEntityUris);
         }
 
         for (EntitySuggestion remoteSuggestion : remoteSuggestions) {
             String remoteUri = remoteSuggestion.getRemoteUri();
             if (alreadySeenRemoteUris.contains(remoteUri)) {
                 // filter out remote suggestions that already have a match
                 // in the local suggestions
                 // TODO: how to account for a rank boost?
                 continue;
             }
             suggestions.add(remoteSuggestion);
         }
 
         // TODO: re-rank results to boost exact name matches
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
                         + "WHERE CONTAINS('%s')"
                         + " AND cmis:objectTypeId NOT IN ('%s')"
                         + " AND nuxeo:isVersion = false "
                        + "ORDER BY relevance DESC", type,
                 cleanupKeywords(keywords),
                 StringUtils.join(getEntityTypeNames(), "', '"));
         PageProvider<DocumentModel> provider = new CMISQLDocumentPageProvider(
                 session, query, "cmis:objectId", "suggestedDocuments");
         provider.setPageSize(maxSuggestions);
         return provider.getCurrentPage();
     }
 
     @Override
     public Set<String> getEntityTypeNames() throws Exception {
         Set<String> types = Framework.getService(SchemaManager.class).getDocumentTypeNamesExtending(
                 Constants.ENTITY_TYPE);
         return new TreeSet<String>(types);
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
             return suggestion.entity;
         } else if (suggestion.remoteEntityUris.isEmpty()) {
             throw new IllegalArgumentException(
                     "The provided suggestion has neither local"
                             + " entity nor emote entities links");
         }
         // TODO: optimize to do a single core query using a disjunction on all
         // the remote URI if we ever have the case for many remote URI
         for (String remoteEntityUri : suggestion.remoteEntityUris) {
             DocumentModel localEntity = getLinkedLocalEntity(session,
                     URI.create(remoteEntityUri));
             if (localEntity != null) {
                 return localEntity;
             }
         }
 
         // dereference remote entity as a local entity
         RemoteEntityService reService;
         PathSegmentService psService;
         try {
             reService = Framework.getService(RemoteEntityService.class);
             psService = Framework.getService(PathSegmentService.class);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         DocumentModel entityContainer = getEntityContainer(session);
         DocumentModel localEntity;
         if (suggestion.entity != null) {
             // this is pre-fetched in memory representation of a remote entity
             // that does not already exist in the local repository
             localEntity = suggestion.entity;
             // ensure that the parent location and path segment are ok
             String pathSegment = psService.generatePathSegment(localEntity);
             localEntity.setPathInfo(entityContainer.getPathAsString(),
                     pathSegment);
         } else {
             // lazy dereferencing into a new local entity document
             localEntity = session.createDocumentModel(suggestion.type);
             localEntity.setPropertyValue("dc:title", suggestion.label);
             localEntity.setPropertyValue("entity:automaticallyCreated",
                     suggestion.automaticallyCreated);
             String pathSegment = psService.generatePathSegment(localEntity);
             localEntity.setPathInfo(entityContainer.getPathAsString(),
                     pathSegment);
             for (String remoteEntity : suggestion.remoteEntityUris) {
                 URI uri = URI.create(remoteEntity);
                 reService.dereferenceInto(localEntity, uri, false, false);
             }
         }
         localEntity = session.createDocument(localEntity);
         for (String remoteEntity : suggestion.remoteEntityUris) {
             recentlyDereferenced.put(remoteEntity, localEntity.getRef());
         }
         session.save();
         return localEntity;
     }
 
 }
