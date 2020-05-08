 /*
  * Created On: Sep 1, 2006 8:13:28 AM
  */
 package com.thinkparity.ophelia.browser.plugin.archive.tab;
 
 import java.awt.event.ActionEvent;
 import java.text.MessageFormat;
 import java.util.*;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 
 import com.thinkparity.codebase.jabber.JabberId;
 import com.thinkparity.codebase.model.artifact.ArtifactReceipt;
 import com.thinkparity.codebase.model.container.Container;
 import com.thinkparity.codebase.model.container.ContainerVersion;
 import com.thinkparity.codebase.model.user.TeamMember;
 import com.thinkparity.codebase.model.user.User;
 import com.thinkparity.codebase.sort.DefaultComparator;
 import com.thinkparity.codebase.sort.StringComparator;
 
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabAvatarSortBy;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabAvatarSortByDelegate;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabAvatarSortBy.SortDirection;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.ContainerPanel;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.view.DocumentView;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.view.DraftView;
 import com.thinkparity.ophelia.browser.platform.plugin.extension.TabPanelExtension;
 import com.thinkparity.ophelia.browser.platform.plugin.extension.TabPanelExtensionModel;
 import com.thinkparity.ophelia.model.user.UserUtils;
 
 /**
  * <b>Title:</b><br>
  * <b>Description:</b><br>
  * @author raymond@thinkparity.com
  * @version $Revision$
  */
 final class ArchiveTabModel extends TabPanelExtensionModel<ArchiveTabProvider>
         implements TabAvatarSortByDelegate {
 
     /** A set of user object utilities. */
     private static final UserUtils USER_UTILS;
 
     static {
         USER_UTILS = UserUtils.getInstance();
     }
 
     /** The <code>ArchiveTabActionDelegate</code>. */
     private final ArchiveTabActionDelegate actionDelegate;
 
     /** The <code>ArchiveTabPopupDelegate</code>. */
     private final ArchiveTabPopupDelegate popupDelegate;
 
     /** The current ordering. */
     private final List<SortBy> sortedBy;
 
     /** Create ArchiveTabModel. */
     ArchiveTabModel(final TabPanelExtension extension) {
         super(extension);
         this.actionDelegate = new ArchiveTabActionDelegate(this);
         this.popupDelegate = new ArchiveTabPopupDelegate(this);
         this.sortedBy = new ArrayList<SortBy>();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabAvatarSortByDelegate#getSortBy()
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
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabModel#debug()
      * 
      */
     @Override
     protected void debug() {
         logger.logDebug("{0} container panels.", panels.size());
         int expandedCount = 0;
         for (final Boolean state : expandedState.values()) {
             if (Boolean.TRUE == state)
                 expandedCount++;
         }
         logger.logDebug("{0} expanded panels.", expandedCount);
         logger.logDebug("{0} visible panels.", visiblePanels.size());
         logger.logDebug("{0} model elements.", listModel.size());
     }
 
     /**
      * Initialize the model. This reads from the provider the initial archive
      * list.
      * 
      */
     protected void initialize() {
         debug();
         clearPanels();
         final List<Container> containers = readContainers();
         for (final Container container : containers) {
             addContainerPanel(container);
         }
         applySort(getInitialSort());
         debug();
     }
 
     /**
      * Apply an ordering to the panels.
      * 
      * @param ordering
      *            An <code>Ordering</code>.
      */
     void applySort(final SortBy sortBy) {
         debug();
         if (isSortApplied(sortBy)) {
             sortBy.ascending = !sortBy.ascending;
         }
         sortedBy.clear();
         sortedBy.add(sortBy);
         persistence.set(sortByKey, sortBy);
         persistence.set(sortAscendingKey, sortBy.ascending);
         synchronize();
     }
 
     /**
      * Obtain the popup delegate.
      * 
      * @return A <code>ArchiveTabPopupDelegate</code>.
      */
     ArchiveTabPopupDelegate getPopupDelegate() {
         return popupDelegate;
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
     String getString(final SortBy sortBy) {
         final StringBuffer pattern = new StringBuffer("ArchiveTab.{0}");
         return getExtension().getLocalization().getString(
                 MessageFormat.format(pattern.toString(), sortBy));
     }
 
     /**
      * Synchronize a container.
      * 
      * @param uniqueId
      *            A unique id <code>UUID</code>.
      */
     void synchronizeContainer(final UUID uniqueId) {
         debug();
         final Container container = read(uniqueId);
         if (null ==  container) {
             removeContainerPanel(uniqueId, true);
         } else {
             final int panelIndex = lookupIndex(uniqueId);
             if (-1 < panelIndex) {
                 removeContainerPanel(uniqueId, false);
                 addContainerPanel(panelIndex, container);
             } else {
                 addContainerPanel(0, container);
             }
         }
         synchronize();
         debug();
     }
     
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabPanelModel#lookupId(com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel)
      */
     @Override
     protected Object lookupId(final TabPanel tabPanel) {
         return ((ContainerPanel)tabPanel).getContainer().getUniqueId();
     }
     
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabPanelModel#lookupPanel(java.lang.Object)
      */
     @Override
     protected TabPanel lookupPanel(final Object uniqueId) {
         final UUID containerId = (UUID)uniqueId;
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
         return Collections.emptyList();
     }
 
     /**
      * Add a container panel.
      * 
      * @param container
      *            A <code>Container</code>.
      */
     private void addContainerPanel(final Container container) {
         addContainerPanel(panels.isEmpty() ? 0 : panels.size() - 1,
                 container);
     }
 
     /**
      * Add a container panel.
      * 
      * @param index
      *            The index in the container list within which to add the
      *            container.
      * @param container
      *            A <code>Container</code>.
      */
     private void addContainerPanel(final int index, final Container container) {
         final List<TeamMember> team = readTeam(container.getUniqueId());
         final List<ContainerVersion> versions = readVersions(container.getUniqueId());
         final ContainerVersion latestVersion = findLatestVersion(versions);
 
         final Map<ContainerVersion, List<DocumentView>> documentViews =
             new HashMap<ContainerVersion, List<DocumentView>>(versions.size(), 1.0F);
         final Map<ContainerVersion, User> publishedBy =
             new HashMap<ContainerVersion, User>(versions.size(), 1.0F);
         final Map<ContainerVersion, Map<User, ArtifactReceipt>> publishedTo =
             new HashMap<ContainerVersion, Map<User, ArtifactReceipt>>(versions.size(), 1.0F);
         ContainerVersion version, previousVersion;
         for (int i = 0; i < versions.size(); i++) {
             version = versions.get(i);
             if (versions.size() - 1 == i) {
                 /* reading the last version in the list which is the first
                  * version chronologically */
                 documentViews.put(version, readDocumentViews(
                         container.getUniqueId(), versions.get(i).getVersionId()));
             } else {
                 previousVersion = versions.get(i + 1);
                 documentViews.put(version, readDocumentViews(
                         container.getUniqueId(), version.getVersionId(),
                         previousVersion.getVersionId()));
             }
             publishedBy.put(version, find(team, version.getUpdatedBy()));
             publishedTo.put(version, readPublishedTo(container.getUniqueId(), version.getVersionId()));
         }
         panels.add(index, toDisplay(container, find(team, container
                 .getCreatedBy()), new DraftView(), latestVersion, versions, documentViews,
                 publishedTo, publishedBy, team));
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
 
     private <T extends User> T find(final List<T> users,
             final JabberId userId) {
         return users.get(USER_UTILS.indexOf(users, userId));
     }
     
     /**
      * Get the initial sort from persistence.
      * 
      * @return A <code>SortBy</code>.
      */
     private SortBy getInitialSort() {
        final SortBy sortBy = (SortBy)persistence.get(sortByKey, SortBy.CREATED_ON);
         sortBy.ascending = persistence.get(sortAscendingKey, false);
         return sortBy;
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
      * Lookup the panel index for the container unique id.
      * 
      * @param uniqueId
      *            A container unique id <code>UUID</code>.
      * @return The panel index; or -1 if the container does not exist in the
      *         list.
      */
     private int lookupIndex(final UUID uniqueId) {
         for (int i = 0; i < panels.size(); i++)
             if (((ContainerPanel) panels.get(i)).getContainer()
                     .getUniqueId().equals(uniqueId))
                 return i;
         return -1;    }
 
     /**
      * Read a container from the archive.
      * 
      * @param uniqueId
      *            A contianer unique id <code>UUID</code>.
      * @return A <code>Container</code>.
      */
     private Container read(final UUID uniqueId) {
         return ((ArchiveTabProvider) contentProvider).readContainer(uniqueId);
     }
 
     /**
      * Read the archived containers.
      * 
      * @return A <code>List</code> of <code>Container</code>s.
      */
     private List<Container> readContainers() {
         return ((ArchiveTabProvider) contentProvider).readContainers();
     }
 
     private List<DocumentView> readDocumentViews(
             final UUID uniqueId, final Long compareVersionId) {
         return ((ArchiveTabProvider) contentProvider).readDocumentVersionDeltas(
                 uniqueId, compareVersionId);
     }
 
     private List<DocumentView> readDocumentViews(
             final UUID uniqueId, final Long compareVersionId,
             final Long compareToVersionId) {
         return ((ArchiveTabProvider) contentProvider).readDocumentViews(
                 uniqueId, compareVersionId, compareToVersionId);
     }
     
     /**
      * Find the latest version for a container.
      * 
      * @param versions
      *            A list of <code>ContainerVersion</code>.
      * @return The latest <code>ContainerVersion</code>.
      */
     private ContainerVersion findLatestVersion(final List<ContainerVersion> versions) {
         if (versions.isEmpty()) {
             return null;
         } else {
             ContainerVersion latestVersion = versions.get(0);
             for (final ContainerVersion version : versions) {
                 if (version.getCreatedOn().compareTo(latestVersion.getCreatedOn()) > 0) {
                     latestVersion = version;
                 }
             }
             return latestVersion;
         }        
     }
 
     private Map<User, ArtifactReceipt> readPublishedTo(final UUID uniqueId,
             final Long versionId) {
         return ((ArchiveTabProvider) contentProvider).readPublishedTo(uniqueId,
                 versionId);
     }
 
     private List<TeamMember> readTeam(final UUID uniqueId) {
         return ((ArchiveTabProvider) contentProvider).readTeam(uniqueId);
     }
 
     /**
      * Read a list of container versions for a container.
      * 
      * @param uniqueId
      *            A container unique id <code>UUID</code>.
      * 
      * @return A <code>List</code> of <code>ContainerVersion</code>s.
      */
     private List<ContainerVersion> readVersions(final UUID uniqueId) {
         return ((ArchiveTabProvider) contentProvider).readVersions(uniqueId);
     }
 
     /**
      * Remove a container panel.
      * 
      * @param uniqueId
      *            A container unique id <code>UUID</code>.
      */
     private void removeContainerPanel(final UUID uniqueId,
             final boolean removeExpandedState) {
         final int panelIndex = lookupIndex(uniqueId);
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
             final User containerCreatedBy,
             final DraftView draftView,
             final ContainerVersion latestVersion,
             final List<ContainerVersion> versions,
             final Map<ContainerVersion, List<DocumentView>> documentViews,
             final Map<ContainerVersion, Map<User, ArtifactReceipt>> publishedTo,
             final Map<ContainerVersion, User> publishedBy,
             final List<TeamMember> team) {
         final ContainerPanel panel = new ArchiveTabContainerPanel(session);
         panel.setActionDelegate(actionDelegate);
         panel.setPanelData(container, containerCreatedBy,
                 draftView, latestVersion, versions, documentViews, publishedTo,
                 publishedBy, team);
         panel.setExpanded(isExpanded(panel));
         panel.setPopupDelegate(popupDelegate);
         panel.setTabDelegate(this);
         return panel;
     }
 
     /** An enumerated type defining the tab panel ordering. */
     private enum SortBy implements Comparator<TabPanel> {
 
         CREATED_ON(false), UPDATED_ON(false), NAME(true);
 
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
             default:
                 return 0;
             }
         }
     }
 }
