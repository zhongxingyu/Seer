 /*
  * Created On: 13-Jul-06 1:03:06 PM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.container;
 
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.DefaultListModel;
 
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.jabber.JabberId;
 import com.thinkparity.codebase.sort.DefaultComparator;
 import com.thinkparity.codebase.sort.StringComparator;
 import com.thinkparity.codebase.swing.dnd.TxUtils;
 
 import com.thinkparity.codebase.model.artifact.ArtifactReceipt;
 import com.thinkparity.codebase.model.container.Container;
 import com.thinkparity.codebase.model.container.ContainerVersion;
 import com.thinkparity.codebase.model.document.Document;
 import com.thinkparity.codebase.model.profile.Profile;
 import com.thinkparity.codebase.model.user.TeamMember;
 import com.thinkparity.codebase.model.user.User;
 
 import com.thinkparity.ophelia.browser.BrowserException;
 import com.thinkparity.ophelia.browser.application.browser.Browser;
 import com.thinkparity.ophelia.browser.application.browser.BrowserSession;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabAvatarSortBy;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabAvatarSortByDelegate;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabPanelModel;
 import com.thinkparity.ophelia.browser.application.browser.display.provider.tab.container.ContainerProvider;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.ContainerPanel;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.view.DocumentView;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.view.DraftView;
 import com.thinkparity.ophelia.browser.platform.Platform.Connection;
 import com.thinkparity.ophelia.browser.util.DocumentUtil;
 import com.thinkparity.ophelia.browser.util.localization.JPanelLocalization;
 
 /**
  * @author rob_masako@shaw.ca; raykroeker@gmail.com
  * @version 1.1.2.4
  */
 public final class ContainerTabModel extends TabPanelModel implements
         TabAvatarSortByDelegate {
 
     /** A <code>ContainerTabActionDelegate</code>. */
     private final ContainerTabActionDelegate actionDelegate;
 
     /** An application. */
     private final Browser browser;
 
     /** A way to lookup container ids from document ids. */
     private final Map<Long, Long> containerIdLookup;
 
     /** A list of the container's expanded state. */
     private final Map<TabPanel, Boolean> expandedState;
 
     /** A list of panels passing through all filters. */
     private final List<TabPanel> filteredPanels;
 
     /** A list model. */
     private final DefaultListModel listModel;
 
     /** A <code>JPanelLocalization</code>. */
     private JPanelLocalization localization;
 
     /** A list of all panels. */
     private final List<TabPanel> panels;
 
     /** A <code>ContainerTabPopupDelegate</code>. */
     private final ContainerTabPopupDelegate popupDelegate;
 
     /** A user search expression <code>String</code>. */
     private String searchExpression;
 
     /** A list search result container ids <code>Long</code>. */
     private final List<Long> searchResults;
 
     /** A <code>BrowserSession</code>. */
     private BrowserSession session;
 
     /** The current ordering. */
     private final List<SortBy> sortedBy;
 
     /** A list of visible panels. */
     private final List<TabPanel> visiblePanels;
 
     /**
      * Create ContainerModel.
      * 
      */
     ContainerTabModel() {
         super();
         this.actionDelegate = new ContainerTabActionDelegate(this);
         this.browser = getBrowser();
         this.containerIdLookup = new HashMap<Long, Long>();
         this.filteredPanels = new ArrayList<TabPanel>();
         this.expandedState = new HashMap<TabPanel, Boolean>();
         this.listModel = new DefaultListModel();
         this.panels = new ArrayList<TabPanel>();
         this.popupDelegate = new ContainerTabPopupDelegate(this);
         this.searchResults = new ArrayList<Long>();
         this.sortedBy = new Stack<SortBy>();
         this.visiblePanels = new ArrayList<TabPanel>();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabAvatarSortByDelegate#getSortedBy()
      *
      */
     public List<TabAvatarSortBy> getSortBy() {
         final List<TabAvatarSortBy> sortBy = new ArrayList<TabAvatarSortBy>();
         for (final SortBy sortByValue : SortBy.values()) {
             sortBy.add(new TabAvatarSortBy() {
                 public Action getAction() {
                     return new AbstractAction() {
                         public void actionPerformed(final ActionEvent e) {
                             applySort(sortByValue);
                         }
                     };
                 }
                 public String getText() {
                     return getString(sortByValue);
                 }
             });
         }
         return sortBy;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabModel#toggleExpansion(com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel)
      * 
      */
     public void toggleExpansion(final TabPanel tabPanel) {
         doToggleExpansion(tabPanel);
         synchronize();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabModel#applySearch(java.lang.String)
      * 
      */
     @Override
     protected void applySearch(final String searchExpression) {
         debug();
         if (searchExpression.equals(this.searchExpression)) {
             return;
         } else {
             this.searchExpression = searchExpression;
             applySearch();
             synchronize();
         }
     }
     
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabModel#canImportData(java.awt.datatransfer.DataFlavor[])
      * 
      */
     @Override
     protected boolean canImportData(final DataFlavor[] transferFlavors) {
         return TxUtils.containsJavaFileList(transferFlavors);
     }
     
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabModel#canImportData(com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel, java.awt.datatransfer.DataFlavor[])
      *
      */
     @Override
     protected boolean canImportData(final TabPanel tabPanel,
             final DataFlavor[] transferFlavors) {
         if (TxUtils.containsJavaFileList(transferFlavors)) {
             return canImportData(((ContainerPanel) tabPanel).getContainer());
         }
         return false;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabModel#debug()
      * 
      */
     @Override
     protected void debug() {
         logger.logDebug("{0} container panels.", panels.size());
         logger.logDebug("{0} filtered panels.", filteredPanels.size());
         logger.logDebug("{0} visible panels.", visiblePanels.size());
         for (final TabPanel filteredPanel : filteredPanels) {
             logger.logVariable("filteredPanel.getContainer().getName()", ((ContainerPanel) filteredPanel).getContainer().getName());
         }
         for (final TabPanel visiblePanel : visiblePanels) {
             logger.logVariable("visiblePanel.getContainer().getName()", ((ContainerPanel) visiblePanel).getContainer().getName());
         }
         logger.logDebug("{0} model elements.", listModel.size());
         final TabPanel[] listModelPanels = new TabPanel[listModel.size()];
         listModel.copyInto(listModelPanels);
         for (final TabPanel listModelPanel : listModelPanels) {
             logger.logVariable("listModelPanel.getId()", listModelPanel.getId());
         }
         logger.logDebug("Search expression:  {0}", searchExpression);
         logger.logDebug("{0} search result hits.", searchResults.size());
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabModel#getListModel()
      */
     @Override
     protected DefaultListModel getListModel() {
         return listModel;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabModel#importData(com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel, java.awt.datatransfer.Transferable)
      *
      */
     @Override
     protected void importData(final TabPanel tabPanel,
             final Transferable transferable) {
         Assert.assertTrue(canImportData(tabPanel,
                 transferable.getTransferDataFlavors()),
                 "Cannot import data {0} onto {1}.", transferable, tabPanel);
         importData(((ContainerPanel) tabPanel).getContainer(), transferable);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabModel#importData(java.awt.datatransfer.Transferable)
      * 
      */
     @Override
     protected void importData(final Transferable transferable) {
         Assert.assertTrue(canImportData(transferable.getTransferDataFlavors()),
                 "Cannot import data {0}.", transferable);
         final File[] transferableFiles = extractFiles(transferable);
         // Determine the list of files to add to a new package. Check if the user
         // is trying to drag folders.
         final List<File> importFiles = new ArrayList<File>();
         Boolean foundFolders = Boolean.FALSE;
         Boolean foundFiles = Boolean.FALSE;
         for (final File transferableFile : transferableFiles) {
             if (transferableFile.isDirectory()) {
                 foundFolders = Boolean.TRUE;
             } else {
                 foundFiles = Boolean.TRUE;
                 importFiles.add(transferableFile);
             }
         }
         // Report an error if the user tries to drag folders. Otherwise
         // create a package and add documents.
         if (foundFolders) {
             browser.displayErrorDialog("ErrorCreatePackageIsFolder");
         } else if (foundFiles) {
             browser.runCreateContainer(importFiles);
         }
     }
 
     /**
      * Initialize the container model with containers; container versions;
      * documents and users from the provider.
      * 
      */
     @Override
     protected void initialize() {
         debug();
         clearPanels();
         final List<Container> containers = readContainers();
         for (final Container container : containers) {
             addContainerPanel(container);
         }
         applySort(SortBy.CREATED_ON);
         debug();
     }
     
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabModel#removeSearch()
      * 
      */
     @Override
     protected void removeSearch() {
         debug();
         // if the member search expression is already null; then there is no
         // search applied -> do nothing
         if (null == searchExpression) {
             return;
         } else {
             searchExpression = null;
             searchResults.clear();
             synchronize();
         }
     }
     
     /**
      * Create the final list of container cells; container draft cells; draft
      * document cells; container version cells and container version document
      * cells. The search filter is also applied here.
      * 
      */
     @Override
     protected void synchronize() {
         debug();
         applyFilters();
         applySort();
         /* add the filtered panels the visibility list */
         visiblePanels.clear();
         for (final TabPanel filteredPanel : filteredPanels) {
             visiblePanels.add(filteredPanel);
         }
         // add newly visible panels to the model; and set other panels
         int listModelIndex;
         for (int i = 0; i < visiblePanels.size(); i++) {
             if (listModel.contains(visiblePanels.get(i))) {
                 listModelIndex = listModel.indexOf(visiblePanels.get(i));
                 /* the position of the panel in the model is identical to that
                  * of the panel the list */
                 if (i == listModelIndex) {
                     listModel.set(i, visiblePanels.get(i));
                 } else {
                     listModel.remove(listModelIndex);
                     listModel.add(i, visiblePanels.get(i));
                 }
             } else {
                 listModel.add(i, visiblePanels.get(i));
             }
         }
         // prune newly invisible panels from the model
         final TabPanel[] invisiblePanels = new TabPanel[listModel.size()];
         listModel.copyInto(invisiblePanels);
         for (int i = 0; i < invisiblePanels.length; i++) {
             if (!visiblePanels.contains(invisiblePanels[i])) {
                 listModel.removeElement(invisiblePanels[i]);
             }
         }
         debug();
     }
     
     /**
      * Obtain the popup delegate.
      * 
      * @return A <code>ContainerTabPopupDelegate</code>.
      */
     ContainerTabPopupDelegate getPopupDelegate() {
         return popupDelegate;
     }
 
     /**
      * Determine whether or not thinkParity is running in development mode.
      * 
      * @return True if thinkParity is running in development mode.
      */
     boolean isDevelopmentMode() {
         return browser.isDevelopmentMode();
     }
 
     /**
      * Determine whether or not the user is online.
      * 
      * @return True if the user is online.
      */
     Boolean isOnline() {
         return browser.getConnection() == Connection.ONLINE;
     }
 
     /**
      * Determine if the container has been distributed.
      * 
      * @param containerId
      *            A <code>Long</code>.
      * @return True if this container has been distributed; false otherwise.
      */
     Boolean readIsDistributed(final Long containerId) {
         return ((ContainerProvider) contentProvider).isDistributed(containerId).booleanValue();
     }
 
     /**
      * Determine if the specified user is the local user.
      * 
      * @param user
      *            A <code>User</code>.
      * @return True if this is the local user; false otherwise.
      */
     Boolean readIsLocalUser(final User user) {
         final Profile profile = readProfile();
         return (user.getId().equals(profile.getId()));
     }
 
     /**
      * Set the model's localization.
      * 
      * @param localization
      *            A <code>JPanelLocalization</code>.
      */
     void setLocalization(final JPanelLocalization localization) {
         this.localization = localization;
     }
 
     /**
      * Set the session.
      * 
      * @param session
      *            A <code>BrowserSession</code>.
      */
     void setSession(final BrowserSession session) {
         this.session = session;
     }
 
     /**
      * Synchronize the container in the display.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param remote
      *            A remote event <code>Boolean</code> indicator.
      */
     void syncContainer(final Long containerId, final Boolean remote) {
         debug();
         final Container container = read(containerId);
         // remove the container from the panel list
         if (null == container) {
             removeContainerPanel(containerId, true);
         } else {
             final int panelIndex = lookupIndex(container.getId());
             if (-1 < panelIndex) {
                 // if the reload is the result of a remote event add the container
                 // at the top of the list; otherwise add it in the same location
                 // it previously existed
                 removeContainerPanel(containerId, false);
                 if (remote) {
                     addContainerPanel(0, container);
                 } else {
                     addContainerPanel(panelIndex, container);
                 }
             } else {
                 addContainerPanel(0, container);
             }
         }
         synchronize();
         debug();
     }
 
     /**
      * Synchronize a document.
      * 
      * @param documentId
      *            A document id <code>Long</code>.
      * @param remote
      *            A remote event <code>Boolean</code> indicator.
      */
     void syncDocument(final Long documentId, final Boolean remote) {
         syncContainer(containerIdLookup.get(documentId), remote);
     }
 
     /**
      * Add a container panel. This will read the container's versions and add
      * the appropriate version panel as well.
      * 
      * @param container
      *            A <code>container</code>.
      */
     private void addContainerPanel(final Container container) {
         addContainerPanel(panels.size() == 0 ? 0 : panels.size() - 1, container);
     }
 
     /**
      * Add a container panel. This will read the container's versions and add the
      * appropriate version panel as well.
      * 
      * @param index
      *            An <code>Integer</code> index.
      * @param container
      *            A <code>container</code>.
      */
     private void addContainerPanel(final int index,
             final Container container) {
         final DraftView draftView = readDraftView(container.getId());
         final ContainerVersion latestVersion = readLatestVersion(container.getId());
         if ((null != draftView) && container.isLocalDraft()) {
             for (final Document document : draftView.getDocuments()) {
                 containerIdLookup.put(document.getId(), container.getId());
             }
         }
         final List<ContainerVersion> versions = readVersions(container.getId());
         final Map<ContainerVersion, List<DocumentView>> documentViews =
             new HashMap<ContainerVersion, List<DocumentView>>(versions.size(), 1.0F);
         final Map<ContainerVersion, Map<User, ArtifactReceipt>> publishedTo =
             new HashMap<ContainerVersion, Map<User, ArtifactReceipt>>(versions.size(), 1.0F);
         final Map<ContainerVersion, User> publishedBy = new HashMap<ContainerVersion, User>(versions.size(), 1.0F);
         List<DocumentView> versionDocumentViews;
         for (final ContainerVersion version : versions) {
             versionDocumentViews = readDocumentViews(version.getArtifactId(),
                     version.getVersionId());
             for (final DocumentView versionDocumentView : versionDocumentViews) {
                 containerIdLookup.put(versionDocumentView.getDocumentId(), container.getId());
             }
             documentViews.put(version, versionDocumentViews);
             publishedTo.put(version, readUsers(version.getArtifactId(), version.getVersionId()));
             publishedBy.put(version, readUser(version.getUpdatedBy()));
         }
         panels.add(index, toDisplay(container, draftView, latestVersion,
                 versions, documentViews, publishedTo, publishedBy,
                 readTeam(container.getId())));
     }
 
     /**
      * Apply a series of filters on the panels.
      * 
      */
     private void applyFilters() {
         filteredPanels.clear();
         if (isSearchApplied()) {
             TabPanel searchResultPanel;
             for (final Long searchResult : searchResults) {
                 searchResultPanel = lookupPanel(searchResult);
                 if (!filteredPanels.contains(searchResultPanel))
                     filteredPanels.add(searchResultPanel);
             }
         } else {
             // no filter is applied
             filteredPanels.addAll(panels);
         }
     }
 
     /**
      * Apply the search results.
      *
      */
     private void applySearch() {
         this.searchResults.clear();
         this.searchResults.addAll(readSearchResults());
     }
 
     /**
      * Apply the sort to the filtered list of panels.
      *
      */
     private void applySort() {
         final DefaultComparator<TabPanel> comparator = new DefaultComparator<TabPanel>();
         for (final SortBy sortBy : sortedBy) {
             comparator.add(sortBy);
         }
         Collections.sort(filteredPanels, comparator);
     }
 
     /**
      * Apply an ordering to the panels.
      * 
      * @param sortBy
      *            A <code>SortBy</code>.
      */
     private void applySort(final SortBy sortBy) {
         debug();
         // if the sorted by stack already contains the ordering do nothing
         if (isSortApplied(sortBy)) {
             if (sortBy.ascending) {
                 sortBy.ascending = false;
 
                 sortedBy.clear();
                 sortedBy.add(sortBy);
             } else {
                 sortedBy.clear();
             }
             synchronize();
         } else {
             sortBy.ascending = true;
 
             sortedBy.clear();
             sortedBy.add(sortBy);
             synchronize();
         }
     }
 
     /**
      * Determine if an import can be performed on a container. The user needs to
      * have the draft, or alternatively if nobody has the draft then he needs to
      * be online in order to create a new draft.
      * 
      * @param container
      *            A <code>Container</code>.
      * @return True if an import can be performed.
      */
     private boolean canImportData(final Container container) {
         if (container.isLocalDraft()) {
             return true;                
         } else if (!container.isDraft() && isOnline()) {
             return true;
         } else {
             return false;
         }
     }
 
     private void clearPanels() {
         panels.clear();
     }
 
     /**
      * Toggle a panel's expansion.
      * 
      * @param tabPanel
      *            A <code>TabPanel</code>.
      */
     private void doToggleExpansion(final TabPanel tabPanel) {
         final ContainerPanel containerPanel = (ContainerPanel) tabPanel;
         final Boolean expanded;
         if (isExpanded(containerPanel)) {
             expanded = Boolean.FALSE;
             containerPanel.collapse();
         } else {
             // NOTE-BEGIN:multi-expand to allow multiple selection in the list; remove here
             for (final TabPanel visiblePanel : visiblePanels) {
                 if (isExpanded(visiblePanel)) {
                     doToggleExpansion(visiblePanel);
                 }
             }
             // NOTE-END:multi-expand
 
             expanded = Boolean.TRUE;
             browser.runApplyContainerFlagSeen(
                     containerPanel.getContainer().getId());
             containerPanel.expand();
         }
         expandedState.put(tabPanel, expanded);
     }
 
     /**
      * Extract a list of files from a transferable.
      * 
      * @param transferable
      *            A <code>Transferable</code>.
      * @return A <code>List</code> of <code>File</code>s.
      */
     private File[] extractFiles(final Transferable transferable) {
         try {
             return TxUtils.extractFiles(transferable);
         } catch (final Exception x) {
             throw new BrowserException("Cannot extract files from transferrable.", x);
         }
     }
 
     /**
      * Obtain a localized string for an ordering.
      * 
      * @param ordering
      *            An <code>Ordering</code>.
      * @return A localized <code>String</code>.
      */
     private String getString(final SortBy sortBy) {
         if (isSortApplied(sortBy)) {
             if (sortBy.ascending) {
                 return localization.getString(sortBy + "_ASC");
             } else {
                 return localization.getString(sortBy + "_DESC");
             }
         } else {
             return localization.getString(sortBy);
         }
     }
 
     /**
      * Import data into a container.
      * 
      * @param container
      *            A <code>Container</code>.
      * @param transferable
      *            Import data <code>Transferable</code>.
      */
     private void importData(final Container container,
             final Transferable transferable) {
         final File[] transferableFiles = extractFiles(transferable);
         // Get the list of documents in this package.
         final List <String> existingDocuments = new ArrayList<String>();
         List<Document> draftDocuments = null;
         if (container.isLocalDraft()) {
             draftDocuments = readDraftDocuments(container.getId());
             for (final Document document : draftDocuments) {
                 existingDocuments.add(document.getName());
             }
         }
         // Determine the list of files to add and/or update. Check if the user
         // is trying to drag folders. Create two lists, one for adding and one
         // for updating, depending on whether there is a document of the same
         // name found in the package.
         final List<File> addFileList = new ArrayList<File>();
         final List<File> updateFileList = new ArrayList<File>();
         Boolean foundFolders = Boolean.FALSE;
         Boolean foundFilesToAdd = Boolean.FALSE;
         Boolean foundFilesToUpdate = Boolean.FALSE;
         for (final File transferableFile : transferableFiles) {
             if (transferableFile.isDirectory()) {
                 foundFolders = Boolean.TRUE;
             } else {
                 if (existingDocuments.contains(transferableFile.getName())) {
                     foundFilesToUpdate = Boolean.TRUE;
                     updateFileList.add(transferableFile);
                 }
                 else {
                     foundFilesToAdd = Boolean.TRUE;
                     addFileList.add(transferableFile);
                 }
             }
         }
         // Report an error if the user tries to drag folders.
         if (foundFolders) {
             browser.displayErrorDialog("ErrorAddDocumentIsFolder");
             return;
         }
         // If the draft is required, attempt to get it. This should succeed
         // unless somebody managed to get the draft, or the system went offline,
         // since the call to canImport() above.
         if (foundFilesToUpdate || foundFilesToAdd) {
             if (!container.isLocalDraft() && !container.isDraft() &&
                     (Connection.ONLINE == browser.getConnection())) {
                 browser.runCreateContainerDraft(container.getId());
             }
             
             if (!container.isLocalDraft()) {
                 browser.displayErrorDialog("ErrorAddDocumentLackDraft",
                         new Object[] { container.getName() });
                 return;
             }
             draftDocuments = readDraftDocuments(container.getId());
         }
         // Add one or more documents.
         if (foundFilesToAdd) {
             browser.runAddContainerDocuments(container.getId(), addFileList
                     .toArray(new File[] {}));
         }
         // Update one or more documents.
         final DocumentUtil documentUtil = DocumentUtil.getInstance();
         if ((foundFilesToUpdate) && (null != draftDocuments)) {
             for (final File file : updateFileList) {
                 if (documentUtil.contains(draftDocuments, file)) {
                     final Document document =
                         draftDocuments.get(documentUtil.indexOf(draftDocuments, file));
                     if (readIsDraftDocumentModified(document.getId())) {
                         if (browser.confirm("ConfirmOverwriteWorking",
                         new Object[] { file.getName() })) {
                             browser.runUpdateDocumentDraft(document.getId(), file);
                         }
                     } else {
                         browser.runUpdateDocumentDraft(document.getId(), file);
                     } 
                 }
             }
         }
     }
 
     /**
      * Determine if a panel is expanded.
      * 
      * @param tabPanel
      *            A <code>TabPanel</code>.
      * @return True if the panel is expanded; false otherwise.
      */
     private boolean isExpanded(final TabPanel tabPanel) {
         if (expandedState.containsKey(tabPanel)) {
             return expandedState.get(tabPanel).booleanValue();
         } else {
             // NOTE the default panel expanded state can be changed here
             expandedState.put(tabPanel, Boolean.FALSE);
             return isExpanded(tabPanel);
         }
     }
     
     /**
      * Determine if a search is applied by checking the search expression.
      * 
      * @return True if the search expression is not null.
      */
     private boolean isSearchApplied() {
         return null != searchExpression;
     }
 
     /**
      * Determine if an ordering is applied.
      * 
      * @param ordering
      *            An <code>Ordering</code>.
      * @return True if it is applied false otherwise.
      */
     private boolean isSortApplied(final SortBy sortBy) {
         debug();
         return sortedBy.contains(sortBy);
     }
 
     /**
      * Lookup the index of the container's corresponding panel.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @return An <code>int</code> index; or -1 if the container does not
      *         exist in the panel list.
      */
     private int lookupIndex(final Long containerId) {
         for (int i = 0; i < panels.size(); i++)
             if (((ContainerPanel) panels.get(i)).getContainer()
                     .getId().equals(containerId))
                 return i;
         return -1;
     }
 
     /**
      * Lookup the panel for the corresponding container id.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @return A <code>TabPanel</code>.
      */
     private TabPanel lookupPanel(final Long containerId) {
         final int panelIndex = lookupIndex(containerId); 
         if (-1 == panelIndex)
             return null;
         else
             return panels.get(panelIndex);
     }
 
     /**
      * Read the container from the provider.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @return A <code>Container</code>.
      */
     private Container read(final Long containerId) {
         return ((ContainerProvider) contentProvider).read(containerId);
     }
 
     /**
      * Read the containers from the provider.
      * 
      * @return The containers.
      */
     private List<Container> readContainers() {
         return ((ContainerProvider) contentProvider).read();
     }
     
     /**
      * Read the draft for a container.
      *
      * @param containerId
      *      A container id <code>Long</code>.
      * @return A <code>ContainerDraft</code>.
      */
     private DraftView readDraftView(final Long containerId) {
         return ((ContainerProvider) contentProvider).readDraftView(containerId);
     }
 
     /**
      * Read the draft's document list.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @return A <code>List</code> of <code>Document</code>s.
      */
     private List<Document> readDraftDocuments(final Long containerId) {
         final DraftView draftView = readDraftView(containerId);
         if (null == draftView) {
             return Collections.emptyList();
         } else {
             return draftView.getDocuments();
         }
     }
 
     /**
      * Determine if the draft document has been modified.
      * 
      * @param documentId
      *            A document id.
      * @return True if the draft document has been modified; false otherwise.
      */
     private boolean readIsDraftDocumentModified(final Long documentId) {
         return ((ContainerProvider) contentProvider).isDraftDocumentModified(documentId).booleanValue();
     }
     
     /**
      * Read the latest version for a container.
      * 
      * @param containerId
      *      A container id <code>Long</code>.
      * @return A <code>ContainerVersion</code>.
      */
     private ContainerVersion readLatestVersion(final Long containerId) {
         return ((ContainerProvider) contentProvider).readLatestVersion(containerId);
     }
 
     /**
      * Read the profile.
      * 
      * @return A <code>Profile</code>.   
      */
     private Profile readProfile() {
         return ((ContainerProvider) contentProvider).readProfile();
     }
 
     /**
      * Search for a list of container ids through the content provider.
      * 
      * @return A list of container ids.
      */
     private List<Long> readSearchResults() {
         return ((ContainerProvider) contentProvider).search(searchExpression);
     }
 
     /**
      * Read the team.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @return A list of <code>TeamMember</code>s.
      */
     private List<TeamMember> readTeam(final Long containerId) {
         return ((ContainerProvider) contentProvider).readTeam(containerId);
     }
 
     /**
      * Read the user.
      * 
      * @param userId
      *            A user id <code>JabberId</code>.
      * @return A <code>User</code>.
      */
     private User readUser(final JabberId userId) {
         return ((ContainerProvider) contentProvider).readUser(userId);
     }
 
     /**
      * Read the list of users.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A container version id <code>Long</code>.
      * @return A <code>Map&lt;User, ArtifactReceipt&gt;</code>.
      */
     private Map<User, ArtifactReceipt> readUsers(final Long containerId,
             final Long versionId) {
         return ((ContainerProvider) contentProvider).readPublishedTo(
                 containerId, versionId);
     }
 
     /**
      * Read the container versions from the provider.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @return A <code>List&lt;ContainerVersion&gt;</code>.
      */
     private List<ContainerVersion> readVersions(final Long containerId) {
         return ((ContainerProvider) contentProvider).readVersions(containerId);
     }
 
     /**
      * Read the documents from the provider.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      * @return A <code>Map&lt;DocumentVersion, Delta&gt;</code>.
      */
     private List<DocumentView> readDocumentViews(final Long containerId,
             final Long versionId) {
         return ((ContainerProvider) contentProvider).readDocumentViews(
                 containerId, versionId);
     }
 
     /**
      * Remove a container panel.
      * 
      * @param container
      *            A <code>Container</code>.
      */
     private void removeContainerPanel(final Long containerId,
             final boolean removeExpandedState) {
         Long lookupContainerId;
         for (final Iterator<Long> iLookupValues =
             containerIdLookup.values().iterator(); iLookupValues.hasNext(); ) {
             lookupContainerId = iLookupValues.next();
             if (lookupContainerId.equals(containerId)) {
                 iLookupValues.remove();
             }
         }
         final int panelIndex = lookupIndex(containerId);
         if (removeExpandedState) {
             final TabPanel containerPanel = panels.remove(panelIndex);
             expandedState.remove(containerPanel);
         } else {
             panels.remove(panelIndex);
         }
     }
 
     /**
      * Create a tab panel for a container.
      * 
      * @param container
      *            A <code>Container</code>.
      * @return A <code>TabPanel</code>.
      */
     private TabPanel toDisplay(
             final Container container,
             final DraftView draftView,
             final ContainerVersion latestVersion,
             final List<ContainerVersion> versions,
             final Map<ContainerVersion, List<DocumentView>> documentViews,
             final Map<ContainerVersion, Map<User, ArtifactReceipt>> publishedTo,
             final Map<ContainerVersion, User> publishedBy,
             final List<TeamMember> team) {
         final ContainerPanel panel = new ContainerPanel(session);
         panel.setActionDelegate(actionDelegate);
         panel.setPanelData(container, readUser(container.getCreatedBy()),
                 draftView, latestVersion, versions, documentViews, publishedTo,
                 publishedBy, team);
         panel.setPopupDelegate(popupDelegate);
         panel.setExpanded(isExpanded(panel));
         panel.setTabDelegate(this);
         return panel;
     }
 
     /** An enumerated type defining the tab panel ordering. */
     private enum SortBy implements Comparator<TabPanel> {
 
         BOOKMARK(true), CREATED_ON(true), NAME(true), OWNER(true), UPDATED_ON(true);
 
         /** An ascending <code>StringComparator</code>. */
         private static final StringComparator STRING_COMPARATOR_ASC;
 
         /** A descending <code>StringComparator</code>. */
         private static final StringComparator STRING_COMPARATOR_DESC;
 
         static {
             STRING_COMPARATOR_ASC = new StringComparator(Boolean.TRUE);
             STRING_COMPARATOR_DESC = new StringComparator(Boolean.FALSE);
         }
         
         /** Whether or not to sort in ascending order. */
         private boolean ascending;
 
         /**
          * Create SortBy.
          * 
          * @param ascending
          *            Whether or not to sort in ascending order.
          */
         private SortBy(final boolean ascending) {
             this.ascending = ascending;
         }
 
         /**
          * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
          * 
          */
         public int compare(final TabPanel o1, final TabPanel o2) {
             final ContainerPanel p1 = (ContainerPanel) o1;
             final ContainerPanel p2 = (ContainerPanel) o2;
             final int multiplier = ascending ? 1 : -1;
             switch (this) {
             case BOOKMARK:
                 // we want true values at the top
                 return -1 * multiplier * p1.getContainer().isBookmarked().compareTo(
                         p2.getContainer().isBookmarked());
             case CREATED_ON:
                 return multiplier * p1.getContainer().getCreatedOn().compareTo(
                         p2.getContainer().getCreatedOn());
             case UPDATED_ON:
                 return multiplier * p1.getContainer().getUpdatedOn().compareTo(
                         p2.getContainer().getUpdatedOn());
             case NAME:
                 // note the lack of multiplier
                 return ascending
                     ? STRING_COMPARATOR_ASC.compare(
                             p1.getContainer().getName(),
                             p2.getContainer().getName())
                     : STRING_COMPARATOR_DESC.compare(
                             p1.getContainer().getName(),
                             p2.getContainer().getName());
             case OWNER:
                 if (p1.getContainer().isDraft())
                     if(p2.getContainer().isDraft())
                         // note the lack of multiplier
                         return ascending
                             ? STRING_COMPARATOR_ASC.compare(
                                 p1.getDraft().getOwner().getName(),
                                 p2.getDraft().getOwner().getName())
                             : STRING_COMPARATOR_DESC.compare(
                                     p1.getDraft().getOwner().getName(),
                                     p2.getDraft().getOwner().getName());
                     else
                         return multiplier * -1;
                 else
                     if (p2.getContainer().isDraft())
                         return multiplier * 1;
                     else
                         return 0;
             default:
                 return 0;
             }
         }
     }
 }
