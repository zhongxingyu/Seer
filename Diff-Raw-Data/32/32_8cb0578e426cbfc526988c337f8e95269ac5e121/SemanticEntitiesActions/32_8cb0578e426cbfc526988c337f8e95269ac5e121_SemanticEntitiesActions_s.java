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
 package org.nuxeo.ecm.platform.semanticentities.jsf.actions;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.Factory;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Observer;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.contexts.Contexts;
 import org.jboss.seam.core.Interpolator;
 import org.jboss.seam.faces.FacesMessages;
 import org.jboss.seam.international.StatusMessage;
 import org.nuxeo.common.utils.StringUtils;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentRef;
 import org.nuxeo.ecm.core.api.IdRef;
 import org.nuxeo.ecm.core.trash.TrashService;
 import org.nuxeo.ecm.platform.query.api.PageProvider;
 import org.nuxeo.ecm.platform.semanticentities.DereferencingException;
 import org.nuxeo.ecm.platform.semanticentities.EntitySuggestion;
 import org.nuxeo.ecm.platform.semanticentities.LocalEntityService;
 import org.nuxeo.ecm.platform.semanticentities.ProgressStatus;
 import org.nuxeo.ecm.platform.semanticentities.RemoteEntity;
 import org.nuxeo.ecm.platform.semanticentities.RemoteEntityService;
 import org.nuxeo.ecm.platform.semanticentities.SemanticAnalysisService;
 import org.nuxeo.ecm.platform.semanticentities.adapter.OccurrenceRelation;
 import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
 import org.nuxeo.ecm.webapp.helpers.EventManager;
 import org.nuxeo.ecm.webapp.helpers.EventNames;
 import org.nuxeo.runtime.api.Framework;
 
 @Name("semanticEntitiesActions")
 @Scope(ScopeType.CONVERSATION)
 public class SemanticEntitiesActions {
 
     public static final Log log = LogFactory.getLog(SemanticEntitiesActions.class);
 
     @In(create = true)
     protected NavigationContext navigationContext;
 
     @In(required = false)
     protected CoreSession documentManager;
 
     @In(create = true)
     protected FacesMessages facesMessages;
 
     @In(create = true)
     protected Map<String, String> messages;
 
     protected String documentSuggestionKeywords;
 
     protected String selectedDocumentId;
 
     protected EntitySuggestion selectedEntitySuggestion;
 
     protected List<DocumentModel> documentSuggestions;
 
     protected LocalEntityService leService;
 
     protected SemanticAnalysisService saService;
 
     protected boolean isRemoteEntitySearchDisplayed = false;
 
     protected URI selectedEntitySuggestionUri;
 
     protected String selectedEntitySuggestionLabel;
 
     protected LocalEntityService getLocalEntityService() {
         if (leService == null) {
             try {
                 leService = Framework.getService(LocalEntityService.class);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         return leService;
     }
 
     protected SemanticAnalysisService getSemanticAnalysisService() {
         if (saService == null) {
             try {
                 saService = Framework.getService(SemanticAnalysisService.class);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         return saService;
     }
 
     public String getSemanticWorkInProgressMessageFor(String docId) {
         if (docId == null) {
             return null;
         }
         String contextKey = "semanticWorkInProgressMessageFor--" + docId;
         String i18nStatus = (String) Contexts.getEventContext().get(contextKey);
         if (i18nStatus == null) {
             DocumentRef docRef = new IdRef(docId);
             ProgressStatus status = getSemanticAnalysisService().getProgressStatus(
                     documentManager.getRepositoryName(), docRef);
             if (status == null) {
                 i18nStatus = "";
             } else {
                 // there is some work in progress: invalidate the cached results
                 // to
                 // display the real state
                 invalidateCurrentDocumentProviders();
 
                 // i18n status message with interpolation
                 String i18nMessageTemplate = messages.get(status.getMessage());
                 if (i18nMessageTemplate == null) {
                     i18nStatus = "";
                 } else {
                     i18nStatus = Interpolator.instance().interpolate(
                             i18nMessageTemplate, status.positionInQueue,
                             status.queueSize);
                 }
             }
             Contexts.getEventContext().set(contextKey, i18nStatus);
         }
         return i18nStatus;
     }
 
     @Factory(scope = ScopeType.SESSION, value = "canBrowseEntityContainer")
     public boolean getCanBrowseEntityContainer() throws Exception {
         // the ScopeType.SESSION scope might hide change in permissions on the
         // entity container unless affected users do logout but this is
         // necessary to avoid DB requests at each page view since the
         // canBrowseEntityContainer variable is used to filter an action that
         // shows up in the top level banner
         return getLocalEntityService().getEntityContainer(documentManager) != null;
     }
 
     public void launchAsyncAnalysis() throws ClientException {
         DocumentModel doc = navigationContext.getCurrentDocument();
         getSemanticAnalysisService().launchAnalysis(doc.getRepositoryName(),
                 doc.getRef());
         invalidateCurrentDocumentProviders();
     }
 
     public String goToEntityContainer() throws Exception {
         DocumentModel entityContainer = getLocalEntityService().getEntityContainer(
                 documentManager);
         if (entityContainer == null) {
             // the user does not have the permission to browse the entities
             return null;
         }
         return navigationContext.navigateToDocument(entityContainer);
     }
 
     @Factory(scope = ScopeType.CONVERSATION, value = "entityOccurrenceProvider")
     public PageProvider<DocumentModel> getCurrentEntityOccurrenceProvider()
             throws ClientException, Exception {
         return getEntityOccurrenceProvider(navigationContext.getCurrentDocument());
     }
 
     /**
      * Return the documents that hold an occurrence to the given entity.
      */
     public PageProvider<DocumentModel> getEntityOccurrenceProvider(
             DocumentModel entity) throws ClientException, Exception {
         return getLocalEntityService().getRelatedDocuments(documentManager,
                 entity.getRef(), null);
     }
 
     @Factory(scope = ScopeType.EVENT, value = "relatedPeopleOccurrences")
     public List<EntityOccurrence> getRelatedPeopleOccurrences()
             throws ClientException, Exception {
         return getRelatedOccurrences(navigationContext.getCurrentDocument(),
                 "Person");
     }
 
     @Factory(scope = ScopeType.EVENT, value = "relatedPlacesOccurrences")
     public List<EntityOccurrence> getRelatedPlacesProvider()
             throws ClientException, Exception {
         return getRelatedOccurrences(navigationContext.getCurrentDocument(),
                 "Place");
     }
 
     @Factory(scope = ScopeType.EVENT, value = "relatedOrganizationsOccurrences")
     public List<EntityOccurrence> getRelatedOrganizationsProvider()
             throws ClientException, Exception {
         return getRelatedOccurrences(navigationContext.getCurrentDocument(),
                 "Organization");
     }
 
     /**
      * Return occurrence information to the local entities linked to the given
      * document.
      * 
      * TODO: make it a provider to enable pagination TODO: make it possible to
      * choose the type of relation (occurrence, topic, ...)
      */
     protected List<EntityOccurrence> getRelatedOccurrences(DocumentModel doc,
             String entityType) throws ClientException {
         PageProvider<DocumentModel> entities = getLocalEntityService().getRelatedEntities(
                 documentManager, doc.getRef(), entityType);
         List<EntityOccurrence> occurrences = new ArrayList<EntityOccurrence>();
         for (DocumentModel entity : entities.getCurrentPage()) {
             OccurrenceRelation relation = getLocalEntityService().getOccurrenceRelation(
                     documentManager, doc.getRef(), entity.getRef());
             EntityOccurrence occ = new EntityOccurrence(doc, entity,
                     relation.getOccurrenceDocument(),
                     relation.getOccurrences(), 3);
             occurrences.add(occ);
         }
         return occurrences;
     }
 
     /*
      * Ajax callbacks for new occurrence relationship creation.
      */
 
     public List<DocumentModel> suggestDocuments(Object keywords) {
         try {
             return getLocalEntityService().suggestDocument(documentManager,
                     keywords.toString(), null, 10);
         } catch (Exception e) {
             log.error(e, e);
             facesMessages.add(StatusMessage.Severity.ERROR,
                     messages.get("error.fetchingDocuments"));
             return Collections.emptyList();
         }
     }
 
     public void setSelectedDocumentId(String selectedDocumentId) {
         this.selectedDocumentId = selectedDocumentId;
     }
 
     public List<EntitySuggestion> suggestEntities(Object keywords) {
         try {
             return getLocalEntityService().suggestEntity(documentManager,
                     keywords.toString(), null, 10);
         } catch (Exception e) {
             log.error(e, e);
             facesMessages.add(StatusMessage.Severity.ERROR,
                     messages.get("error.fetchingEntities"));
             return Collections.emptyList();
         }
     }
 
     public void setSelectedSuggestion(EntitySuggestion suggestion) {
         this.selectedEntitySuggestion = suggestion;
     }
 
     public void addNewOccurrenceRelation() {
         try {
             if (selectedDocumentId != null) {
                 getLocalEntityService().addOccurrences(documentManager,
                         new IdRef(selectedDocumentId),
                         navigationContext.getCurrentDocument().getRef(), null);
             } else if (selectedEntitySuggestion != null) {
                 DocumentModel localEntity = leService.asLocalEntity(
                         documentManager, selectedEntitySuggestion);
                 OccurrenceRelation relation = leService.addOccurrences(
                         documentManager,
                         navigationContext.getCurrentDocument().getRef(),
                         localEntity.getRef(), null);
                 DocumentRef occRef = relation.getOccurrenceDocument().getRef();
                 if ("deleted".equals(documentManager.getCurrentLifeCycleState(occRef))) {
                     documentManager.followTransition(occRef, "undelete");
                 }
             }
         } catch (Exception e) {
             log.error(e, e);
             facesMessages.add(StatusMessage.Severity.ERROR,
                     messages.get("error.addingRelation"));
         }
         invalidateCurrentDocumentProviders();
     }
 
     public void removeOccurrenceRelation(String docId, String entityId) {
         OccurrenceRelation rel;
         try {
             rel = getLocalEntityService().getOccurrenceRelation(
                     documentManager, new IdRef(docId), new IdRef(entityId));
             if (rel != null) {
                 // TODO: define an invalidate transition to be used by default
                 // for explicitly handling human correction of false positives
                 DocumentModel relDoc = rel.getOccurrenceDocument();
                 List<DocumentModel> docToDelete = Arrays.asList(relDoc);
                 TrashService trashService = Framework.getService(TrashService.class);
                 if (trashService.canDelete(docToDelete,
                         documentManager.getPrincipal(), false)) {
                     trashService.trashDocuments(docToDelete);
                 } else {
                     facesMessages.add(StatusMessage.Severity.WARN,
                             messages.get("error.removingRelation"));
                 }
             }
         } catch (Exception e) {
             log.error(e, e);
             facesMessages.add(StatusMessage.Severity.ERROR,
                     messages.get("error.removingRelation"));
         }
         invalidateCurrentDocumentProviders();
     }
 
     /*
      * Ajax callbacks for remote entity linking and syncing
      */
 
     @Factory(scope = ScopeType.EVENT, value = "currentEntitySameAs")
     public List<RemoteEntity> getCurrentEntitySameAs() {
         try {
             return RemoteEntity.fromDocument(navigationContext.getCurrentDocument());
         } catch (ClientException e) {
             log.error(e, e);
             facesMessages.add(StatusMessage.Severity.ERROR,
                     messages.get("error.fetchingLocalLinkedEntities"));
             return Collections.emptyList();
         }
     }
 
     public void showSuggestRemoteEntitySearch() {
         isRemoteEntitySearchDisplayed = true;
     }
 
     public boolean getShowSuggestRemoteEntitySearch() {
         return isRemoteEntitySearchDisplayed;
     }
 
     public List<RemoteEntity> suggestRemoteEntity(Object input) {
         String type = navigationContext.getCurrentDocument().getType();
         String keywords = (String) input;
         try {
             RemoteEntityService remoteEntityService = Framework.getService(RemoteEntityService.class);
             List<RemoteEntity> filteredSuggestions = new ArrayList<RemoteEntity>();
             List<RemoteEntity> suggestions = remoteEntityService.suggestRemoteEntity(
                     keywords, type, 5);
             // TODO: filter out entities that already have a local entity synced
             // to them
             filteredSuggestions.addAll(suggestions);
             return filteredSuggestions;
         } catch (Exception e) {
             log.error(e, e);
             facesMessages.add(StatusMessage.Severity.ERROR,
                     messages.get("error.fetchingRemoteEntities"));
             return Collections.emptyList();
         }
     }
 
     public void setSelectedEntitySuggestionUri(URI uri) {
         selectedEntitySuggestionUri = uri;
     }
 
     public void setSelectedEntitySuggestionLabel(String label) {
         selectedEntitySuggestionLabel = label;
     }
 
     public void addRemoteEntityLinkAndSync() {
         if (selectedEntitySuggestionLabel == null
                 || selectedEntitySuggestionUri == null) {
             // TODO: display some user friendly warning
             return;
         }
         RemoteEntity re = new RemoteEntity(selectedEntitySuggestionLabel,
                 selectedEntitySuggestionUri);
         DocumentModel doc = navigationContext.getChangeableDocument();
         try {
             // TODO: once the UI allows to set multiple links to several remote
             // sources we should set override back to false
             syncAndSaveDocument(doc, re.uri, true);
         } catch (Exception e) {
             log.error(e, e);
             facesMessages.add(StatusMessage.Severity.ERROR,
                     messages.get("error.linkingToRemoteEntity"));
         }
         Contexts.removeFromAllContexts("currentEntitySameAs");
     }
 
     public void syncWithSameAsLink(String uri) {
         try {
             syncAndSaveDocument(navigationContext.getChangeableDocument(),
                     URI.create(uri), true);
         } catch (Exception e) {
             log.error(e, e);
             facesMessages.add(StatusMessage.Severity.ERROR,
                     messages.get("error.syncingWithRemoteEntity"));
         }
     }
 
     protected void syncAndSaveDocument(DocumentModel doc, URI uri,
             boolean fullSync) throws Exception, DereferencingException,
             ClientException {
         RemoteEntityService remoteEntityService = Framework.getService(RemoteEntityService.class);
         if (remoteEntityService.canDereference(uri)) {
             remoteEntityService.dereferenceInto(doc, uri, fullSync);
         }
         doc = documentManager.saveDocument(doc);
         documentManager.save();
         notifyDocumentUpdated(doc);
     }
 
     public void removeSameAsLink(String uri) {
         try {
             DocumentModel doc = navigationContext.getChangeableDocument();
             RemoteEntityService remoteEntityService = Framework.getService(RemoteEntityService.class);
             remoteEntityService.removeSameAsLink(doc, URI.create(uri));
             doc = documentManager.saveDocument(doc);
             documentManager.save();
             notifyDocumentUpdated(doc);
         } catch (Exception e) {
             log.error(e, e);
             facesMessages.add(StatusMessage.Severity.ERROR,
                     messages.get("error.unlinkingRemoteEntity"));
         }
         Contexts.removeFromAllContexts("currentEntitySameAs");
     }
 
     @Observer(value = EventNames.USER_ALL_DOCUMENT_TYPES_SELECTION_CHANGED, create = false)
     public void onDocumentNavigation() {
         selectedDocumentId = null;
         selectedEntitySuggestion = null;
         isRemoteEntitySearchDisplayed = false;
         invalidateCurrentDocumentProviders();
     }
 
     public void invalidateCurrentDocumentProviders() {
         Contexts.removeFromAllContexts("entityOccurrenceProvider");
         Contexts.removeFromAllContexts("relatedPeopleOccurrences");
         Contexts.removeFromAllContexts("relatedPlacesOccurrences");
         Contexts.removeFromAllContexts("relatedOrganizationsOccurrences");
     }
 
     protected void notifyDocumentUpdated(DocumentModel doc)
             throws ClientException {
         navigationContext.invalidateCurrentDocument();
         facesMessages.add(StatusMessage.Severity.INFO,
                 messages.get("document_modified"), messages.get(doc.getType()));
         EventManager.raiseEventsOnDocumentChange(doc);
     }
 
     // TODO: move this to a JSF function
     protected String ellipsis(String content, int maxWords, boolean reverse) {
         if (content == null) {
             return "";
         }
         List<String> tokens = Arrays.asList(content.split(" "));
         if (tokens.size() > maxWords) {
             if (reverse) {
                 Collections.reverse(tokens);
             }
             tokens = new ArrayList<String>(tokens.subList(0, maxWords));
             if ((!reverse && content.startsWith(" "))
                     || (reverse && content.endsWith(" "))) {
                 // re-add missing space removed by split
                 tokens.add(0, " ");
             }
             tokens.add("(...)");
             if (reverse) {
                 Collections.reverse(tokens);
             }
             return StringUtils.join(tokens, " ");
         }
         return content;
     }
 
     public String ellipsis(String content, int maxWords) {
         return ellipsis(content, maxWords, false);
     }
 
     public String reverseEllipsis(String content, int maxWords) {
         return ellipsis(content, maxWords, true);
     }
 }
