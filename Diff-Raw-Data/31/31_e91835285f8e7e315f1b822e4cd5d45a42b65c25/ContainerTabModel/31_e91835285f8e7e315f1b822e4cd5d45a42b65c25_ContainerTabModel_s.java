 /*
  * Created On: 13-Jul-06 1:03:06 PM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.container;
 
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.ActionEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.util.*;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.jabber.JabberId;
 import com.thinkparity.codebase.model.artifact.ArtifactReceipt;
 import com.thinkparity.codebase.model.container.Container;
 import com.thinkparity.codebase.model.container.ContainerVersion;
 import com.thinkparity.codebase.model.document.Document;
 import com.thinkparity.codebase.model.profile.Profile;
 import com.thinkparity.codebase.model.user.TeamMember;
 import com.thinkparity.codebase.model.user.User;
 import com.thinkparity.codebase.sort.DefaultComparator;
 import com.thinkparity.codebase.sort.StringComparator;
 import com.thinkparity.codebase.swing.dnd.TxUtils;
 
 import com.thinkparity.ophelia.browser.BrowserException;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabAvatarSortBy;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabAvatarSortByDelegate;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabPanelModel;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabAvatarSortBy.SortDirection;
 import com.thinkparity.ophelia.browser.application.browser.display.provider.tab.container.ContainerProvider;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.ContainerPanel;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.view.DocumentView;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.view.DraftView;
 import com.thinkparity.ophelia.browser.platform.Platform.Connection;
 import com.thinkparity.ophelia.browser.util.DocumentUtil;
 import com.thinkparity.ophelia.model.container.ContainerDraftMonitor;
 import com.thinkparity.ophelia.model.events.ContainerDraftListener;
 import com.thinkparity.ophelia.model.events.ContainerEvent;
 
 /**
  * @author rob_masako@shaw.ca; raykroeker@gmail.com
  * @version 1.1.2.4
  */
 public final class ContainerTabModel extends TabPanelModel implements
         TabAvatarSortByDelegate {
 
     /** A session key for the draft monitor. */
     private static final String SK_DRAFT_MONITOR;
 
     static {
         SK_DRAFT_MONITOR = new StringBuffer(ContainerTabModel.class.getName())
             .append("#ContainerDraftMonitor").toString();
     }
 
     /** A <code>ContainerTabActionDelegate</code>. */
     private final ContainerTabActionDelegate actionDelegate;
 
     /** A way to lookup container ids from document ids. */
     private final Map<Long, Long> containerIdLookup;
 
     /** A <code>ContainerTabPopupDelegate</code>. */
     private final ContainerTabPopupDelegate popupDelegate;
 
     /** The current ordering. */
     private final List<SortBy> sortedBy;
 
     /**
      * Create ContainerModel.
      * 
      */
     ContainerTabModel() {
         super();
         this.actionDelegate = new ContainerTabActionDelegate(this);
         this.containerIdLookup = new HashMap<Long, Long>();
         this.popupDelegate = new ContainerTabPopupDelegate(this);
         this.sortedBy = new Stack<SortBy>();
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
                 public SortDirection getDirection() {
                     return getSortDirection(sortByValue);
                 }
             });
         }
         return sortBy;
     }
    
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabPanelModel#toggleExpansion(com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel, java.lang.Boolean)
      */
     @Override
    public void toggleExpansion(TabPanel tabPanel, Boolean animate) {
        super.toggleExpansion(tabPanel, animate);
        
        final ContainerPanel containerPanel = (ContainerPanel) tabPanel;         
         if (!containerPanel.getContainer().isSeen()) {
             browser.runApplyContainerFlagSeen(containerPanel.getContainer().getId());
             syncContainer(containerPanel.getContainer().getId(), Boolean.FALSE);
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
         browser.runDisplayContainerSeenFlagInfo();
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
      * Set the draft to be selected for the container.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      */
     void setDraftSelection(final Long containerId) {
         final TabPanel tabPanel = lookupPanel(containerId);
         if (isExpanded(tabPanel))
             ((ContainerPanel) tabPanel).setDraftSelection();
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
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabPanelModel#lookupPanel(java.lang.Object)
      */
     @Override
     protected TabPanel lookupPanel(final Object uniqueId) {
         final Long containerId = (Long)uniqueId;
         final int panelIndex = lookupIndex(containerId); 
         if (-1 == panelIndex)
             return null;
         else
             return panels.get(panelIndex);
     }
     
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabPanelModel#readSearchResults()
      */
     @Override
     protected List<? extends Object> readSearchResults() {
         return ((ContainerProvider) contentProvider).search(searchExpression);
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
      * Apply the sort to the filtered list of panels.
      *
      */
     protected void applySort() {
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
         if (isSortApplied(sortBy)) {
             sortBy.ascending = !sortBy.ascending;
         }
         sortedBy.clear();
         sortedBy.add(sortBy);
         synchronize();
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
      * Obtain a container draft monitor created by the model.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param listener
      *            A <code>ContainerDraftListener</code>.
      * @return A <code>ContainerDraftMonitor</code>.
      */
     private ContainerDraftMonitor getDraftMonitor(final Long containerId,
             final ContainerDraftListener listener) {
         return ((ContainerProvider) contentProvider).getDraftMonitor(containerId, listener);
     }
 
     /**
      * Obtain the session draft monitor.
      * 
      * @return The session draft monitor; or null if no draft monitor is set.
      */
     private ContainerDraftMonitor getSessionDraftMonitor() {
         return (ContainerDraftMonitor) session.getAttribute(SK_DRAFT_MONITOR);        
     }
     
     /**
      * Get the sort direction.
      * 
      * @param sortBy
      *            A <code>SortBy</code>.
      * @return A <code>SortDirection</code>.        
      */
     public SortDirection getSortDirection(final SortBy sortBy) {
         if (isSortApplied(sortBy)) {
             if (sortBy.ascending) {
                 return SortDirection.ASCENDING;
             } else {
                 return SortDirection.DESCENDING;
             }
         } else {
             return SortDirection.NONE;
         }
     }
     
     /**
      * Obtain a localized string for an ordering.
      * 
      * @param sortBy
      *            A <code>SortBy</code>.
      * @return A localized <code>String</code>.
      */
     private String getString(final SortBy sortBy) {
         return localization.getString(sortBy);
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
      * Determine if the session draft monitor is set for the container.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @return True if the session draft monitor is not null and is monitoring
      *         the container.
      */
     private boolean isSetSessionDraftMonitor(final Long containerId) {
         final ContainerDraftMonitor monitor = getSessionDraftMonitor();
         return null != monitor && monitor.getContainerId().equals(containerId);
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
         final TabPanel containerPanel = panels.remove(panelIndex);
         if (removeExpandedState) {
             expandedState.remove(containerPanel);
         }
         if (isSetSessionDraftMonitor(containerId)) {
             stopSessionDraftMonitor();
         }
     }
 
     /**
      * Start a draft monitor for a container.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      */
     private void startSessionDraftMonitor(final Long containerId) {
         // if the current draft monitor is set for this container; do nothing
         if (isSetSessionDraftMonitor(containerId))
             return;
         // if a current draft monitor exists stop it
         ContainerDraftMonitor monitor = getSessionDraftMonitor();
         if (null != monitor) {
             stopSessionDraftMonitor();
         }
         // create a new monitor and start it
         monitor = getDraftMonitor(containerId, new ContainerDraftListener() {
             public void documentModified(final ContainerEvent e) {
                 syncContainer(containerId, Boolean.FALSE);
             }
         });
         session.setAttribute(SK_DRAFT_MONITOR, monitor);
         monitor.start();
     }
 
     /**
      * Stop the session draft monitor.
      *
      */
     private void stopSessionDraftMonitor() {
         final ContainerDraftMonitor monitor = getSessionDraftMonitor();
         if (null != monitor) {
             monitor.stop();
             session.removeAttribute(SK_DRAFT_MONITOR);
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
         if (isExpanded(panel)) {
             browser.runApplyContainerFlagSeen(
                     panel.getContainer().getId());
         }
         // the session will maintain the single draft monitor for the tab
         panel.addPropertyChangeListener(new PropertyChangeListener() {
             public void propertyChange(final PropertyChangeEvent evt) {
                 if ("expanded".equals(evt.getPropertyName())
                         || "draftSelected".equals(evt.getPropertyName())) {
                     if (panel.isExpanded() && panel.isDraftSelected()) {
                         startSessionDraftMonitor(panel.getContainer().getId());
                     }
                 }
             }
         });
         return panel;
     }
 
     /** An enumerated type defining the tab panel ordering. */
     private enum SortBy implements Comparator<TabPanel> {
 
         BOOKMARK(true), CREATED_ON(false), UPDATED_ON(false), NAME(true), OWNER(true);
 
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
