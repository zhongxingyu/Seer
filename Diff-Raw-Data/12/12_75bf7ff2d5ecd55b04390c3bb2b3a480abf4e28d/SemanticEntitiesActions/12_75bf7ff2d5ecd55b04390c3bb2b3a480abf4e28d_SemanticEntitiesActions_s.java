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
 
 import java.io.Serializable;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.Factory;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Observer;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.contexts.Contexts;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.IdRef;
 import org.nuxeo.ecm.core.api.PageProvider;
 import org.nuxeo.ecm.core.api.model.PropertyException;
 import org.nuxeo.ecm.platform.semanticentities.DereferencingException;
 import org.nuxeo.ecm.platform.semanticentities.LocalEntityService;
 import org.nuxeo.ecm.platform.semanticentities.RemoteEntity;
 import org.nuxeo.ecm.platform.semanticentities.RemoteEntityService;
 import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
 import org.nuxeo.ecm.webapp.helpers.EventNames;
 import org.nuxeo.runtime.api.Framework;
 
 @Name("semanticEntitiesActions")
 @Scope(ScopeType.CONVERSATION)
 public class SemanticEntitiesActions {
 
     public static final String ENTITY_SAMEAS = "entity:sameas";
 
     @In(create = true)
     protected NavigationContext navigationContext;
 
     @In(required = false)
     protected CoreSession documentManager;
 
     protected String documentSuggestionKeywords;
 
     protected String selectedDocumentId;
 
     protected List<DocumentModel> documentSuggestions;
 
     protected LocalEntityService leService;
 
     protected boolean isRemoteEntitySearchDisplayed = false;
 
     protected URI selectedEntitySuggestionUri;
 
     protected String selectedEntitySuggestionLabel;
 
     protected LocalEntityService getLocalEntityService() throws Exception {
         if (leService == null) {
             leService = Framework.getService(LocalEntityService.class);
         }
         return leService;
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
 
     /*
      * Ajax callbacks for new occurrence relationship creation.
      */
 
     public String getDocumentSuggestionKeywords() {
         return documentSuggestionKeywords;
     }
 
     public void setDocumentSuggestionKeywords(String documentSuggestionKeywords) {
         this.documentSuggestionKeywords = documentSuggestionKeywords;
     }
 
     public List<DocumentModel> suggestDocuments(Object keywords)
             throws Exception {
         // TODO: wrap exception in friendly JSF error messages
         return getLocalEntityService().suggestDocument(documentManager,
                 keywords.toString(), null, 10);
     }
 
     public void setSelectedDocumentId(String selectedDocumentId) {
         this.selectedDocumentId = selectedDocumentId;
     }
 
     public void addNewOccurrenceRelation() throws Exception {
         // TODO: wrap exception in friendly JSF error messages
         getLocalEntityService().addOccurrences(documentManager,
                 new IdRef(selectedDocumentId),
                 navigationContext.getCurrentDocument().getRef(), null);
         invalidateEntityOccurrenceProvider();
     }
 
     /*
      * Ajax callbacks for remote entity linking and syncing
      */
 
    @Factory(scope = ScopeType.EVENT, value = "currentEntitySameAss")
    public void getCurrentEntitySameAs() {

     }
 
     public void showSuggestRemoteEntitySearch() {
         isRemoteEntitySearchDisplayed = true;
     }
 
     public boolean getShowSuggestRemoteEntitySearch() {
         return isRemoteEntitySearchDisplayed;
     }
 
     public void setSelectedEntitySuggestionUri(String uri) {
         selectedEntitySuggestionUri = URI.create(uri);
     }
 
     public void setSelectedEntitySuggestionLabel(String label) {
         selectedEntitySuggestionLabel = label;
     }
 
     public void addRemoteEntityLinkAndSync() throws Exception {
         if (selectedEntitySuggestionLabel == null
                 || selectedEntitySuggestionUri == null) {
             // TODO: display some user friendly warning
             return;
         }
         RemoteEntity re = new RemoteEntity(selectedEntitySuggestionLabel,
                 selectedEntitySuggestionUri);
         DocumentModel doc = navigationContext.getChangeableDocument();
         re.addToEntities(doc, ENTITY_SAMEAS);
 
         syncAndSaveDocument(doc, re.uri, false);
     }
 
     public void syncWithSameAsLink(String uri) throws Exception {
         syncAndSaveDocument(navigationContext.getChangeableDocument(),
                 URI.create(uri), true);
     }
 
     protected void syncAndSaveDocument(DocumentModel doc, URI uri,
             boolean fullSync) throws Exception, DereferencingException,
             ClientException {
         RemoteEntityService remoteEntityService = Framework.getService(RemoteEntityService.class);
        remoteEntityService.dereferenceInto(doc, uri, fullSync);
         documentManager.saveDocument(doc);
         documentManager.save();
     }
 
     @SuppressWarnings("unchecked")
     public void removeSameAsLink(String uri) throws PropertyException,
             ClientException {
         DocumentModel doc = navigationContext.getChangeableDocument();
         List<Map<String, String>> oldRemoteEntities = doc.getProperty(
                 ENTITY_SAMEAS).getValue(List.class);
         List<Map<String, String>> newRemoteEntities = new ArrayList<Map<String, String>>();
         for (Map<String, String> entity : oldRemoteEntities) {
             if (!uri.equals(entity.get("uri"))) {
                 newRemoteEntities.add(entity);
             }
         }
         doc.setPropertyValue(ENTITY_SAMEAS, (Serializable) newRemoteEntities);
         documentManager.saveDocument(doc);
         documentManager.save();
     }
 
     @Observer(value = EventNames.USER_ALL_DOCUMENT_TYPES_SELECTION_CHANGED, create = false, inject = false)
     public void onDocumentNavigation() {
         isRemoteEntitySearchDisplayed = false;
         invalidateEntityOccurrenceProvider();
     }
 
     public void invalidateEntityOccurrenceProvider() {
         Contexts.removeFromAllContexts("entityOccurrenceProvider");
     }
 }
