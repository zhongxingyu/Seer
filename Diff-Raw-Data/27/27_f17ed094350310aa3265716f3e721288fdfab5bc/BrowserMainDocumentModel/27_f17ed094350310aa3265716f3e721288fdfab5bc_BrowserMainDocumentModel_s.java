 /*
  * Apr 4, 2006
  */
 package com.thinkparity.browser.application.browser.display.avatar;
 
 import java.awt.Component;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.event.MouseEvent;
 import java.util.*;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JPopupMenu;
 import javax.swing.ListModel;
 
 import org.apache.log4j.Logger;
 
 import com.thinkparity.browser.application.browser.Browser;
 import com.thinkparity.browser.application.browser.component.MenuFactory;
 import com.thinkparity.browser.application.browser.display.avatar.main.MainCell;
 import com.thinkparity.browser.application.browser.display.avatar.main.MainCellDocument;
 import com.thinkparity.browser.application.browser.display.avatar.main.MainCellHistoryItem;
 import com.thinkparity.browser.application.browser.display.avatar.main.popup.PopupDocument;
 import com.thinkparity.browser.application.browser.display.avatar.main.popup.PopupHistoryItem;
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
 
     /** An apache logger. */
     protected final Logger logger;
 
     /** The application. */
     private final Browser browser;
 
     /** The content provider. */
     private CompositeFlatSingleContentProvider contentProvider;
 
     /** The filter that is used to filter documents to produce visibleDocuments. */
     private final FilterChain<Artifact> documentFilter;
 
     /** A map of documents to their history. */
     private final Map<MainCellDocument, List<MainCellHistoryItem>> documentHistory;
 
     /** A list of all documents. */
     private final List<MainCellDocument> documents;
 
     /** The swing list model. */
     private final DefaultListModel jListModel;
 
     /** A list of "dirty" cells. */
     private final List<MainCell> dirtyCells;
 
     /** The set of all visible cells. */
     private final List<MainCell> visibleCells;
 
     /**
      * Create a BrowserMainDocumentModel.
      * 
      */
     BrowserMainDocumentModel(final Browser browser) {
         super();
         this.browser = browser;
         this.documentFilter = new FilterChain<Artifact>();
         this.documents = new LinkedList<MainCellDocument>();
         this.documentHistory = new Hashtable<MainCellDocument, List<MainCellHistoryItem>>(10, 0.65F);
         this.jListModel = new DefaultListModel();
         this.logger = browser.getPlatform().getLogger(getClass());
         this.dirtyCells = new LinkedList<MainCell>();
         this.visibleCells = new LinkedList<MainCell>();
     }
 
     /**
      * Determine whether or not the main cell is expanded.
      * 
      * @param mainCell
      *            The cell.
      * @return True if the cell is expanded; false otherwise.
      */
     public Boolean isExpanded(final MainCell mainCell) {
         if(mainCell instanceof MainCellDocument) {
             return ((MainCellDocument) mainCell).isExpanded();
         }
         else { return Boolean.FALSE; }
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
         syncModel();
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
         syncModel();
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
             syncModel();
         }
         else if(ArtifactState.CLOSED == state) {
             applyDocumentFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.STATE_CLOSED));
             syncModel();
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
         syncModel();
     }
 
     /**
      * Debug the document filter.
      *
      */
     void debug() {
         if(browser.getPlatform().isDebugMode()) {
             logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL] [" + documents.size() + " DOCUMENTS]");
             Integer historyItems = 0;
             for(final MainCellDocument mcd : documents) {
                 historyItems += documentHistory.get(mcd).size();
             }
             logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL] [" + historyItems + " HISTORY EVENTS]");
             logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL] [" + visibleCells.size() + " VISIBLE CELLS]");
             logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL] [" + dirtyCells.size() + " DIRTY CELLS]");
             logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL] [" + jListModel.size() + " MODEL ELEMENTS]");
 
             // documents
             logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL] [DOCUMENTS (" + documents.size() + ")]");
             for(final MainCellDocument mcd : documents) {
                 logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL]\t[" + mcd.getText() + "]");
             }
             // history
             logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL] [HISTORY EVENTS (" + historyItems + ")]");
             for(final MainCellDocument mcd : documents) {
                 for(final MainCellHistoryItem mchi : documentHistory.get(mcd)) {
                     logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL]\t[" + mchi.getText() + "]");
                 }
             }
             // visible cells
             logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL] [VISIBLE CELLS (" + visibleCells.size() + ")]");
             for(final MainCell mc : visibleCells) {
                 logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL]\t[" + mc.getText() + "]");
             }
             
             // list elements
             final Enumeration e = jListModel.elements();
             MainCell mc;
             logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL] [MODEL ELEMENTS (" + jListModel.size() + ")]");
             while(e.hasMoreElements()) {
                 mc = (MainCell) e.nextElement();
                 logger.debug("[BROWSER2] [APP] [B2] [MAIN MODEL]\t[" + mc.getText() + "] ["
                     + mc.isGroupSelected() + "]");
             }
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
     Boolean isDocumentVisible(final MainCellDocument mainCellDocument) {
         return visibleCells.contains(mainCellDocument);
     }
 
     /**
      * Remove all key holder filters.
      *
      * @see #applyKeyHolderFilter(Boolean)
      */
     void removeKeyHolderFilters() {
         removeDocumentFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.KEY_HOLDER_FALSE));
         removeDocumentFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.KEY_HOLDER_TRUE));
         syncModel();
     }
 
     /**
      * Remove the search filter.
      * 
      * @see #applySearchFilter(List)
      */
     void removeSearchFilter() {
         removeDocumentFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.SEARCH));
         syncModel();
     }
 
     /**
      * Remove the state filters.
      * 
      * @see #applyStateFilter(ArtifactState)
      */
     void removeStateFilters() {
         removeDocumentFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.STATE_ACTIVE));
         removeDocumentFilter(DOCUMENT_FILTERS.get(DocumentFilterKey.STATE_CLOSED));
         syncModel();
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
         syncDocumentInternal(documentId, remote);
         syncModel();
     }
 
     /**
      * Synchronize the documents with the list. The content provider is queried
      * for the document and if it can be obtained; it will either be added to or
      * updated in the list. If it cannot be found; it will be removed from the
      * list.
      * 
      * @param documentIds
      *            The document ids.
      * @param remote
      *            Whether or not the reload is the result of a remote event or
      *            not.
      * @see #syncDocumentInternal(Long, Boolean)
      * @see #syncModel()
      */
     void syncDocuments(final List<Long> documentIds, final Boolean remote) {
         for(final Long documentId : documentIds) {
             syncDocumentInternal(documentId, remote);
         }
         syncModel();
     }
 
     /**
      * Trigger a double click event for the cell.
      * 
      * @param mainCell
      *            The main cell.
      */
     void triggerDoubleClick(final MainCell mainCell) {
         debug();
         triggerExpand(mainCell);
     }
 
     /**
      * Trigger a drag event for the cell.
      * 
      * @param mainCell
      *            The main cell.
      * @param dtde
      *            The drop target drag event.
      */
     void triggerDragOver(final MainCell mainCell, final DropTargetDragEvent dtde) {}
 
     /**
      * Trigger the expansion of the cell.
      * 
      * @param mainCell
      *            The main cell.
      */
     void triggerExpand(final MainCell mainCell) {
         if(mainCell instanceof MainCellDocument) {
             final MainCellDocument mcd = (MainCellDocument) mainCell;
             if(isExpanded(mcd)) { collapse(mcd); }
             else { expand(mcd); }
 
             syncModel();
         }
     }
 
     /**
      * Trigger a popup event for the cell.
      * 
      * @param mainCell
      *            The main cell.
      */
     void triggerPopup(final MainCell mainCell, final Component invoker, final MouseEvent e,
             final int x, final int y) {
         final JPopupMenu jPopupMenu = MenuFactory.createPopup();
         if(mainCell instanceof MainCellDocument) {
             new PopupDocument((MainCellDocument) mainCell).trigger(browser, jPopupMenu, e);
         }
         else if(mainCell instanceof MainCellHistoryItem) {
             new PopupHistoryItem((MainCellHistoryItem) mainCell).trigger(browser, jPopupMenu, e);
         }
         jPopupMenu.show(invoker, x, y);
     }
 
     /**
      * Trigger a selection event for the cell.
      * 
      * @param mainCell
      *            The main cell.
      */
     void triggerSelection(final MainCell mainCell) {
         if(mainCell instanceof MainCellDocument) {
             browser.selectDocument(((MainCellDocument) mainCell).getId());
         }
         // set group selection
         if(mainCell instanceof MainCellDocument ||
             mainCell instanceof MainCellHistoryItem) {
             for(final MainCell mc : getAllCells()) {
                 mc.setGroupSelected(Boolean.FALSE);
             }
             for(final MainCell mc : getDocumentGroup(mainCell)) {
                 mc.setGroupSelected(Boolean.TRUE);
             }
         }
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
      * Perform a shallow clone of the documents list.
      * 
      * @return A copy of the documents list.
      */
     private List<MainCellDocument> cloneDocuments() {
         final List<MainCellDocument> clone = new LinkedList<MainCellDocument>();
         clone.addAll(documents);
         return clone;
     }
 
     /**
      * Collapse the history.
      * 
      * @param mcd
      *            The main cell document.
      */
     private void collapse(final MainCellDocument mcd) {
         mcd.setExpanded(Boolean.FALSE);
 
         syncModel();
     }
 
     /**
      * Expand the history for the document.
      * 
      * @param mcd
      *            The main cell document.
      */
     private void expand(final MainCellDocument mcd) {
         mcd.setExpanded(Boolean.TRUE);
 
         syncModel();
     }
 
     /**
      * Obtain the list of cells adhering to the document group.  This
      * consists of the document; as well as all history cells.
      *
      */
     private MainCell[] getAllCells() {
         final MainCell[] allCells = new MainCell[jListModel.size()];
         jListModel.copyInto(allCells);
         return allCells;
     }
 
     /**
      * Obtain the list of cells adhering to the document group.  This
      * consists of the document; as well as all history cells.
      *
      */
     private MainCell[] getDocumentGroup(final MainCell mainCell) {
         final Set<MainCell> group = new HashSet<MainCell>();
         group.add(mainCell);
         if(documentHistory.containsKey(mainCell))
             group.addAll(documentHistory.get(mainCell));
         return group.toArray(new MainCell[] {});
     }
 
     /**
      * Initialize the document model
      * <ol>
      * <li>Load the documents from the provider.
      * <li>Load the history from the provider.
      * <li>Synchronize the data with the model.
      * <ol>
      */
     private void initModel() {
         // read the documents from the provider into the list
         documents.clear();
         documents.addAll(readDocuments());
         for(final MainCellDocument mcd : documents) {
             documentHistory.put(mcd, readHistory(mcd));
         }
         syncModel();
     }
 
     /**
      * Read a document from the provider.
      * 
      * @param documentId
      *            The document id.
      * @return The document.
      */
     private MainCellDocument readDocument(final Long documentId) {
         return (MainCellDocument) contentProvider.getElement(0, documentId);
     }
 
     /**
      * Read the documents from the provider.
      * 
      * @return The documents.
      */
     private List<MainCellDocument> readDocuments() {
         final List<MainCellDocument> l = new LinkedList<MainCellDocument>();
         final MainCellDocument[] a =
                 (MainCellDocument[]) contentProvider.getElements(0, null);
         for(final MainCellDocument mcd : a) { l.add(mcd); }
         return l;
     }
 
     /**
      * Read the history for the document from the provider.
      * 
      * @param mainCellDocument
      *            The document.
      * @return The history.
      */
     private List<MainCellHistoryItem> readHistory(
             final MainCellDocument mainCellDocument) {
         final List<MainCellHistoryItem> l = new LinkedList<MainCellHistoryItem>();
         final MainCellHistoryItem[] a =
                 (MainCellHistoryItem[]) contentProvider.getElements(1, mainCellDocument);
         for(final MainCellHistoryItem mchi : a) { l.add(mchi); }
         return l;
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
      * Synchronize the document with the list. The content provider is queried
      * for the document and if it can be obtained; it will either be added to or
      * updated in the list as well as its history updated. If it cannot be found;
      * it will be removed from the list.
      * 
      * @param documentId
      *            The document id.
      * @param remote
      *            Whether or not the reload is the result of a remote event or
      *            not.
      * 
      * @see #syncDocument(Long, Boolean)
      * @see #syncModel()
      */
     private void syncDocumentInternal(final Long documentId,
             final Boolean remote) {
         final MainCellDocument mainCellDocument = readDocument(documentId);
 
         // if the document is null; we can assume the document has been
         // deleted (it's not longer being created by the provider); so we find
         // the document and remove it
         if(null == mainCellDocument) {
             for(int i = 0; i < documents.size(); i++) {
                 if(documents.get(i).getId().equals(documentId)) {
                     documents.remove(i);
                     break;
                 }
             }
             final MainCellDocument[] historyKeys =
                 (MainCellDocument[]) documentHistory.keySet().toArray(new MainCellDocument[] {});
             for(int i = 0; i < historyKeys.length; i++) {
                 if(historyKeys[i].getId().equals(documentId)) {
                     documentHistory.remove(historyKeys[i]);
                     break;
                 }
             }
         }
         // the document is not null; therefore it is either new; or updated
         else {
 
             // the document is new
             if(!documents.contains(mainCellDocument)) {
                 documents.add(0, mainCellDocument);
                 documentHistory.put(mainCellDocument, readHistory(mainCellDocument));
             }
             // the document has been updated
             else {
                 final int index = documents.indexOf(mainCellDocument);
                 documents.remove(index);
 
                 // if the reload is the result of a remote event add the document
                 // at the top of the list; otherwise add it in the same location
                 // it previously existed
                 if(remote) { documents.add(0, mainCellDocument); }
                 else { documents.add(index, mainCellDocument); }
                 documentHistory.put(mainCellDocument, readHistory(mainCellDocument));
 
                 dirtyCells.add(mainCellDocument);
                 dirtyCells.addAll(documentHistory.get(mainCellDocument));
             }
         }
     }
 
     /**
      * Filter the list of documents. Update the visible cell list with documents
      * as well as the history. Update the model with the visible cell list.
      * 
      */
     private void syncModel() {
         debug();
 
         // filter documents
         final List<MainCellDocument> filteredDocuments = cloneDocuments();
         ModelFilterManager.filter(filteredDocuments, documentFilter);
         // update all visible cells
         visibleCells.clear();
         for(final MainCellDocument mcd : filteredDocuments) {
             visibleCells.add(mcd);
             if(mcd.isExpanded())
                 visibleCells.addAll(documentHistory.get(mcd));
         }
 
         // add visible cells not in the model; as well as update cell
         // locations
         for(final MainCell mc : visibleCells) {
             if(!jListModel.contains(mc)) {
                 jListModel.add(visibleCells.indexOf(mc), mc);
             }
             else {
                 if(jListModel.indexOf(mc) != visibleCells.indexOf(mc)) {
                     jListModel.removeElement(mc);
                     jListModel.add(visibleCells.indexOf(mc), mc);
                 }
             }
         }
 
         // prune cells
         final MainCell[] mcModel = new MainCell[jListModel.size()];
         jListModel.copyInto(mcModel);
         for(final MainCell mc : mcModel) {
             if(!visibleCells.contains(mc)) { jListModel.removeElement(mc); }
         }
 
         // update dirty cells
         final Iterator<MainCell> iDirty = dirtyCells.iterator();
         MainCell mcDirty;
         while(iDirty.hasNext()) {
             mcDirty = iDirty.next();
             if(jListModel.contains(mcDirty)) {  // might not contain history cells
                 jListModel.removeElement(mcDirty);
                 jListModel.add(visibleCells.indexOf(mcDirty), mcDirty);
                 iDirty.remove();
             }
         }
 
         if(isDocumentListFiltered()) { browser.fireFilterApplied(); }
         else { browser.fireFilterRevoked(); }
 
         debug();
     }
 
     /**
      * Unique keys used in the {@link DOCUMENT_FILTERS} collection.
      * 
      */
     private enum DocumentFilterKey {
         KEY_HOLDER_FALSE, KEY_HOLDER_TRUE, SEARCH, STATE_ACTIVE, STATE_CLOSED
     }
 }
