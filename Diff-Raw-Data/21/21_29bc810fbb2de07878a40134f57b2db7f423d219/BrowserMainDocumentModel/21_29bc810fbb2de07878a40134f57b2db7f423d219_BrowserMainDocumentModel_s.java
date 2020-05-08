 /*
  * Apr 4, 2006
  */
 package com.thinkparity.browser.application.browser.display.avatar;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.DefaultListModel;
 import javax.swing.ListModel;
 
 import org.apache.log4j.Logger;
 
 import com.thinkparity.browser.application.browser.Browser;
 import com.thinkparity.browser.application.browser.display.avatar.main.DisplayDocument;
 import com.thinkparity.browser.application.browser.display.avatar.main.DocumentListItem;
 import com.thinkparity.browser.application.browser.display.avatar.main.ListItem;
 import com.thinkparity.browser.application.browser.display.provider.CompositeFlatSingleContentProvider;
 
 import com.thinkparity.codebase.assertion.Assert;
 
 import com.thinkparity.model.parity.model.artifact.Artifact;
 import com.thinkparity.model.parity.model.artifact.ArtifactState;
 import com.thinkparity.model.parity.model.filter.Filter;
 import com.thinkparity.model.parity.model.filter.FilterChain;
 import com.thinkparity.model.parity.model.filter.ModelFilterManager;
 import com.thinkparity.model.parity.model.filter.artifact.Active;
 import com.thinkparity.model.parity.model.filter.artifact.Closed;
 import com.thinkparity.model.parity.model.filter.artifact.IsKeyHolder;
 import com.thinkparity.model.parity.model.filter.artifact.IsNotKeyHolder;
 import com.thinkparity.model.parity.model.filter.artifact.Search;
 import com.thinkparity.model.parity.model.index.IndexHit;
 
 /**
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 public class BrowserMainDocumentModel {
 
     /**
      * Collection of all filters used by the document model.
      * 
      */
     private static final Map<Enum<?>, Filter<Artifact>> DOCUMENT_FILTERS;
 
     static {
         DOCUMENT_FILTERS = new HashMap<Enum<?>, Filter<Artifact>>(5, 0.75F);
         DOCUMENT_FILTERS.put(DocumentFilterKey.STATE_ACTIVE, new Active());
         DOCUMENT_FILTERS.put(DocumentFilterKey.STATE_CLOSED, new Closed());
         DOCUMENT_FILTERS.put(DocumentFilterKey.KEY_HOLDER_FALSE, new IsNotKeyHolder());
         DOCUMENT_FILTERS.put(DocumentFilterKey.KEY_HOLDER_TRUE, new IsKeyHolder());
         DOCUMENT_FILTERS.put(DocumentFilterKey.SEARCH, new Search(new LinkedList<IndexHit>()));
     }
 
     /**
      * An apache logger.
      * 
      */
     protected final Logger logger;
 
     /**
      * The browser application.
      * 
      */
     private final Browser browser;
 
     /**
      * The model content provider.
      * 
      */
     private CompositeFlatSingleContentProvider contentProvider;
 
     /**
      * The filter that is used to filter documents to produce visibleDocuments.
      * 
      */
     private final FilterChain<Artifact> documentFilter;
 
     /**
      * The set of all documents.
      * 
      */
     private final List<DisplayDocument> documents;
 
     /**
      * The swing list model.
      * 
      */
     private final DefaultListModel jListModel;
 
     /**
      * The list of all updated documents.
      * 
      * @see #syncDocument(Long, Boolean)
      * @see #syncDocuments()
      */
     private final List<DisplayDocument> touchedDocuments;
 
     /**
      * The set of all visible documents.
      * 
      */
     private final List<DisplayDocument> visibleDocuments; 
 
     /**
      * Create a BrowserMainDocumentModel.
      * 
      */
     BrowserMainDocumentModel(final Browser browser) {
         super();
         this.browser = browser;
         this.documentFilter = new FilterChain<Artifact>();
         this.documents = new LinkedList<DisplayDocument>();
         this.jListModel = new DefaultListModel();
         this.logger = browser.getPlatform().getLogger(getClass());
         this.touchedDocuments = new LinkedList<DisplayDocument>();
         this.visibleDocuments = new LinkedList<DisplayDocument>();
     }
 
     /**
      * Apply a key holder filter to the list of visible documents.
      * 
      * @param keyHolder
      *            If true; will filter documents with a key; if false; it will
      *            filter documents without the key.
      * 
      * @see #removeKeyHolderFilters()
      */
     void applyKeyHolderFilter(final Boolean keyHolder) {
         applyDocumentFilter(keyHolder
                 ? DOCUMENT_FILTERS.get(DocumentFilterKey.KEY_HOLDER_TRUE)
                         : DOCUMENT_FILTERS.get(DocumentFilterKey.KEY_HOLDER_FALSE));
         syncDocuments();
     }
 
     /**
      * Apply a search filter to the list of visible documents.
      * 
      * @param searchResult
      *            The search result to filter by.
      * 
      * @see #removeSearchFilter()
      */
     void applySearchFilter(final List<IndexHit> searchResult) {
         final Search search = (Search) DOCUMENT_FILTERS.get(DocumentFilterKey.SEARCH);
         search.setResults(searchResult);
 
         applyDocumentFilter(search);
         syncDocuments();
     }
 
     /**
      * Apply an artifact state filter to the list of visible documents.
      * 
      * @param state
      *            The artifact state to filter by.
      * 
      * @see #removeStateFilters()
      */
     void applyStateFilter(final ArtifactState state) {
         if(ArtifactState.ACTIVE == state) {
             applyDocumentFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.STATE_ACTIVE));
             syncDocuments();
         }
         else if(ArtifactState.CLOSED == state) {
             applyDocumentFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.STATE_CLOSED));
             syncDocuments();
         }
         else {
             Assert.assertUnreachable(
                     "[BROWSER2] [APP] [B2] [MAIN AVATAR] [MODEL] [CANNOT FILTER BY STATE " + state + "]");
         }
     }
 
     /**
      * Clear the filter on the visible documents. Note that the search filter
      * will still be applied.
      * 
      * @see #removeSearchFilter()
      */
     void clearDocumentFilters() {
         // remove all document filters save the search filter and apply
         // changes
         for(final DocumentFilterKey filterKey : DocumentFilterKey.values()) {
             if(filterKey == DocumentFilterKey.SEARCH) { continue; }
             documentFilter.removeFilter(DOCUMENT_FILTERS.get(filterKey));
         }
         syncDocuments();
     }
 
     /**
      * Debug the document filter.
      *
      */
     void debug() {
         if(browser.getPlatform().isDebugMode()) {
             logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL] [" + documents.size() + " DOCUMENTS]");
             for(final DisplayDocument dd : documents)
                 logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL] [" + dd.getDisplay() + "]");
             logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL] [" + visibleDocuments.size() + " VISIBLE DOCUMENTS]");
             for(final DisplayDocument dd : visibleDocuments)
                 logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL] [" + dd.getDisplay() + "]");
             logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL] [" + jListModel.size() + " LIST MODEL DOCUMENTS]");
             final Enumeration e = jListModel.elements();
             while(e.hasMoreElements())
                 logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL] [" + ((ListItem) e.nextElement()).getName() + "]");
             documentFilter.debug(logger);
         }
     }
 
     /**
      * Obtain the swing list model.
      * 
      * @return The swing list model.
      */
     ListModel getListModel() { return jListModel; }
 
     /**
      * Determine whether the document list is currently filtered.
      * 
      * @return True if the document list is filtered; false otherwise.
      */
     Boolean isDocumentListFiltered() {
         if(documentFilter.isEmpty()) { return Boolean.FALSE; }
         else {
             if(documentFilter.containsFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.SEARCH))) {
                 if(documentFilter.containsFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.STATE_ACTIVE)) ||
                         documentFilter.containsFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.STATE_CLOSED)) ||
                         documentFilter.containsFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.KEY_HOLDER_FALSE)) ||
                         documentFilter.containsFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.KEY_HOLDER_TRUE))) {
                     return Boolean.TRUE;
                 }
                 else { return Boolean.FALSE; }
                 
             }
             else { return Boolean.TRUE; }
         }
     }
 
     /**
      * Determine if the document is visible.
      * 
      * @param displayDocument
      *            The display document.
      * @return True if the document is visible; false otherwise.
      */
     Boolean isDocumentVisible(final DisplayDocument displayDocument) {
         return visibleDocuments.contains(displayDocument);
     }
 
     /**
      * Synchronize the document with the list. The content provider is queried
      * for the document and if it can be obtained; it will either be added to or
      * updated in the list. If it cannot be found; it will be removed from the
      * list.
      * 
      * @param documentId
      *            The document id.
      * @param remote
      *            Whether or not the reload is the result of a remote event or
      *            not.
      */
     void syncDocument(final Long documentId, final Boolean remote) {
         final DisplayDocument displayDocument =
             (DisplayDocument) contentProvider.getElement(0, documentId);
         // if the display document is null; we can assume the document has been
         // deleted (it's not longer being created by the provider); so we find
         // the document and remove it
         if(null == displayDocument) {
             for(int i = 0; i < documents.size(); i++) {
                 if(documents.get(i).getId().equals(documentId)) {
                     documents.remove(i);
                     break;
                 }
             }
         }
         else {
             // if the document is in the list; we need to remove it;
             // and re-add it.
             if(documents.contains(displayDocument)) {
                 final int index = documents.indexOf(displayDocument);
                 documents.remove(index);
 
                 // if the reload is not the result of a remote event; put it back
                 // where it was; otherwise move it to the top
                 if(remote) {
                     documents.add(0, displayDocument);
                     touchedDocuments.add(displayDocument);
                 }
                 else {
                     documents.add(index, displayDocument);
                     touchedDocuments.add(displayDocument);
                 }
             }
             // if it's not in the list; just add it to the top
             else { documents.add(0, displayDocument); }
         }
         syncDocuments();
     }
 
     /**
      * Remove all key holder filters.
      *
      * @see #applyKeyHolderFilter(Boolean)
      */
     void removeKeyHolderFilters() {
         removeDocumentFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.KEY_HOLDER_FALSE));
         removeDocumentFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.KEY_HOLDER_TRUE));
         syncDocuments();
     }
 
     /**
      * Remove the search filter.
      * 
      * @see #applySearchFilter(List)
      */
     void removeSearchFilter() {
         removeDocumentFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.SEARCH));
         syncDocuments();
     }
 
     /**
      * Remove the state filters.
      * 
      * @see #applyStateFilter(ArtifactState)
      */
     void removeStateFilters() {
         removeDocumentFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.STATE_ACTIVE));
         removeDocumentFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.STATE_CLOSED));
         syncDocuments();
     }
 
     /**
      * Set the content provider. This will initialize the model with documents
      * via the provider.
      * 
      * @param contentProvider
      *            The content provider.
      */
     void setContentProvider(
             final CompositeFlatSingleContentProvider contentProvider) {
         this.contentProvider = contentProvider;
         initModel();
     }
 
     /**
      * Apply the specified filter.
      * 
      * @param filter
      *            The document filter.
      */
     private void applyDocumentFilter(final Filter<Artifact> filter) {
         if(!documentFilter.containsFilter(filter)) {
             documentFilter.addFilter(filter);
         }
     }
 
     /**
      * Initialize the document model
      * <ol>
      * <li>Load the documents from the provider.
      * <li>Load the visible document list using the filter.
      * <li>Load the swing list model.
      * <ol>
      */
     private void initModel() {
         // read the documents from the provider into the list
         documents.clear();
         final DisplayDocument[] ddArray = (DisplayDocument[]) contentProvider.getElements(0, null);
         for(final DisplayDocument dd : ddArray) { documents.add(dd); }
         syncDocuments();
     }
 
     /**
      * Remove a document filter.
      * 
      * @param filter
      *            The document filter.
      */
     private void removeDocumentFilter(final Filter<Artifact> filter) {
         if(documentFilter.containsFilter(filter)) {
             documentFilter.removeFilter(filter);
         }
     }
 
     /**
      * Synchronize the document list with the list of visible documents and
      * the list model.
      * 
      */
     private void syncDocuments() {
         // sync the documents with the visible documents
         visibleDocuments.clear();
         visibleDocuments.addAll(documents);
         ModelFilterManager.filter(visibleDocuments, documentFilter);
         // sync visible documents with the swing list's model
         ListItem li;
         for(int i = 0; i < visibleDocuments.size(); i++) {
             li = ListItem.create(visibleDocuments.get(i));
             if(!jListModel.contains(li)) {
                 jListModel.add(i, li);
             }
         }
         final ListItem[] modelItems = new ListItem[jListModel.size()];
         jListModel.copyInto(modelItems);
         DocumentListItem dli;
        int touchedIndex;
         for(int i = 0; i < modelItems.length; i++) {
             li = modelItems[i];
             if(li instanceof DocumentListItem) {
                 dli = (DocumentListItem) li;
                 // remove documents that are no longer visible
                 if(!visibleDocuments.contains(dli.getDisplayDocument()))
                     jListModel.removeElement(li);
 
                 // re-create the list item of those that have been touched
                 if(touchedDocuments.contains(dli.getDisplayDocument())) {
                    touchedIndex = touchedDocuments.indexOf(dli.getDisplayDocument());
                     jListModel.remove(i);
                    jListModel.add(i, ListItem.create(touchedDocuments.get(touchedIndex)));
                 }
             }
         }
         touchedDocuments.clear();
         if(1 > visibleDocuments.size()) {
             browser.disableHistory();
         }
         else { browser.enableHistory(); }
     }
 
     /**
      * Unique keys used in the {@link DOCUMENT_FILTERS} collection.
      * 
      */
     private enum DocumentFilterKey {
         KEY_HOLDER_FALSE, KEY_HOLDER_TRUE, SEARCH, STATE_ACTIVE, STATE_CLOSED
     }
 }
