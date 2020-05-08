 /*
  * Created On:  October 7, 2006, 10:36 AM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar.tab;
 
 import java.awt.Container;
 import java.awt.Rectangle;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JComponent;
 
 import com.thinkparity.codebase.log4j.Log4JWrapper;
 
 import com.thinkparity.ophelia.browser.application.browser.Browser;
 import com.thinkparity.ophelia.browser.application.browser.BrowserSession;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel;
 import com.thinkparity.ophelia.browser.platform.Platform.Connection;
 import com.thinkparity.ophelia.browser.platform.util.persistence.Persistence;
 import com.thinkparity.ophelia.browser.platform.util.persistence.PersistenceFactory;
 import com.thinkparity.ophelia.browser.util.localization.JPanelLocalization;
 
 /**
  * <b>Title:</b>thinkParity Tab Panel Model<br>
  * <b>Description:</b>A model for all tab panels.<br>
  *
  * @author raymond@thinkparity.com
  * @version 1.1.2.2
  */
 public abstract class TabPanelModel<T extends Object> extends TabModel {
     
     /** A sort ascending persistence key */
     // TODO use appropriate variable naming for constants
     // TODO move the persistence key variables to the same scope as the persitence instance
     protected static final String sortAscendingKey;
     
     /** A sort by persistence key */
     // TODO use appropriate variable naming for constants
     // TODO move the persistence key variables to the same scope as the persitence instance
     protected static final String sortByKey;
     
     static {
         sortByKey = "sort.by";
         sortAscendingKey = "sort.ascending";
     }
 
     /** The application. */
     protected final Browser browser;
 
     /** The model's expanded state map. */
     protected final Map<TabPanel, Boolean> expandedState;
 
     /** A list of filtered panels. */
     protected final List<TabPanel> filteredPanels;
 
     /** The swing list model. */
     protected final DefaultListModel listModel;
 
     /** A <code>JPanelLocalization</code>. */
     protected JPanelLocalization localization;
 
     /** An apache logger wrapper. */
     protected final Log4JWrapper logger;
 
     /** A list of all panels. */
     protected final List<TabPanel> panels;
     
     /** A parity persistence. */
     protected final Persistence persistence;
     
     /** A user search expression <code>String</code>. */
     protected String searchExpression;
     
     /** The selected panel. */
     protected TabPanel selectedPanel;
     
     /** A search result list of unique id <code>T</code>s. */
     protected final List<T> searchResults;
 
     /** A <code>BrowserSession</code>. */
     protected BrowserSession session;
 
     /** A list of all visible cells. */
     protected final List<TabPanel> visiblePanels;
 
     /**
      * Create TabPanelModel.
      *
      */
     public TabPanelModel() {
         super();
         this.logger = new Log4JWrapper(getClass());
         this.browser = getBrowser();
         this.expandedState = new HashMap<TabPanel, Boolean>();
         this.filteredPanels = new ArrayList<TabPanel>();
         this.listModel = new DefaultListModel();
         this.panels = new ArrayList<TabPanel>();
         this.persistence = PersistenceFactory.getPersistence(getClass()); 
         this.searchResults = new ArrayList<T>();
         this.visiblePanels = new ArrayList<TabPanel>();
     }
 
     /**
      * Collapse the panel.
      * 
      * @param panelId
      *            A panel id <code>T</code>.
      * @param animate
      *            Animate flag <code>Boolean</code>.           
      */
     public void collapsePanel(final T panelId, final Boolean animate) {
         final TabPanel tabPanel = (TabPanel)lookupPanel(panelId);
         if (isExpanded(tabPanel)) {
             toggleExpansion(tabPanel, animate);
         }
     }
 
     /**
      * Expand the panel.
      * 
      * @param panelId
      *            A panel id <code>T</code>.
      * @param animate
      *            Animate flag <code>Boolean</code>.           
      */
     public void expandPanel(final T panelId, final Boolean animate) {
         final TabPanel tabPanel = (TabPanel)lookupPanel(panelId);
         if (!isExpanded(tabPanel)) {
             toggleExpansion(tabPanel, animate);
         }
     }
     
     /**
      * Sort and filter the ids according to the existing visible order. Visible
      * elements are returned in the order they are currently displayed top to
      * bottom. Elements that are not visible are not included in the returned
      * list. This method assumes visiblePanels are already filtered and sorted
      * correctly.
      * 
      * @param ids
      *            A list of panel id <code>T</code>.
      * @return A sorted list of panel id <code>T</code>s.
      */
     public List<T> getCurrentVisibleOrder(final List<T> panelIds) {
         final List<T> sortedIds = new ArrayList<T>();
         for (final TabPanel visiblePanel : visiblePanels) {
             final T panelId = lookupId(visiblePanel);
             if (panelIds.contains(panelId)) {
                 sortedIds.add(panelId);
             }            
         }
         return sortedIds;
     }
 
     /**
      * Determine whether or not thinkParity is running in development mode.
      * 
      * @return True if thinkParity is running in development mode.
      */
     public boolean isDevelopmentMode() {
         return browser.isDevelopmentMode();
     }
     
     /**
      * Determine whether or not the user is online.
      * 
      * @return True if the user is online.
      */
     public Boolean isOnline() {
         return browser.getConnection() == Connection.ONLINE;
     }
     
     /**
      * Scroll the panel so it is visible.
      * 
      * @param panelId
      *            A panel id <code>T</code>.
      */
     public void scrollPanelToVisible(final T panelId) {
         final JComponent panel = (JComponent) lookupPanel(panelId);
         final Rectangle rectangle = panel.getBounds();
         rectangle.x = rectangle.y = 0;
         panel.scrollRectToVisible(rectangle);
     }
 
     /**
      * Select the panel.
      * 
      * @param panelId
      *            A panel id <code>T</code>.    
      */
     public void selectPanel(final T panelId) {
         final TabPanel panel = lookupPanel(panelId);
         selectPanel(panel);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabDelegate#selectPanel(com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel)
      */
     public void selectPanel(final TabPanel tabPanel) {
         if (isSelected(tabPanel)) {
             return;
         }
         if (null != selectedPanel) {
             // Note that the panel may have been replaced since it was set.
             // The selectedPanel id can be trusted.
            // Also note the selected panel may have been deleted.
             final T oldPanelId = lookupId(selectedPanel);
            final TabPanel oldPanel = lookupPanel(oldPanelId);
            if (null != oldPanel) {
                oldPanel.setSelected(Boolean.FALSE);
            }
         }
         selectedPanel = tabPanel;
         selectedPanel.setSelected(Boolean.TRUE);
     }
 
     /**
      * Set localization.
      *
      * @param localization
      *      A JPanelLocalization.
      */
     public void setLocalization(final JPanelLocalization localization) {
         this.localization = localization;
     }
     
     /**
      * Set the session.
      * 
      * @param session
      *            A <code>BrowserSession</code>.
      */
     public void setSession(final BrowserSession session) {
         this.session = session;
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabModel#toggleExpansion(com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel)
      * 
      */
     public void toggleExpansion(final TabPanel tabPanel) {
         toggleExpansion(tabPanel, Boolean.TRUE);
     }
     
     /**
      * Toggle expansion of a panel.
      * 
      * @param tabPanel
      *            A <code>TabPanel</code>.
      * @param animate
      *            Animate flag <code>Boolean</code>.           
      */
     public void toggleExpansion(final TabPanel tabPanel, final Boolean animate) {
         doToggleExpansion(tabPanel, animate);
         synchronize();
     }
     
     /**
      * Apply a series of filters on the panels.
      * 
      */
     protected void applyFilters() {
         filteredPanels.clear();
         if (isSearchApplied()) {
             TabPanel searchResultPanel;
             for (final T searchResult : searchResults) {
                 searchResultPanel = lookupPanel(searchResult);
                 /* NOTE if the search result panel is null something has
                  * happened to bump the panel out of this tab */ 
                 if (null != searchResultPanel)
                     if (!filteredPanels.contains(searchResultPanel))
                         filteredPanels.add(searchResultPanel);
             }
         } else {
             // no filter is applied
             filteredPanels.addAll(panels);
         }
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
      * Clear all panels.
      *
      */
     protected void clearPanels() {
         panels.clear();
     }
        
     /**
      * Toggle the expansion of a single panel.
      * 
      * @param tabPanel
      *            A <code>TabPanel</code>.
      * @param animate
      *            Animate flag <code>Boolean</code>.             
      */
     protected void doToggleExpansion(final TabPanel tabPanel, final Boolean animate) {
         if (isExpanded(tabPanel)) {
             // if the panel is already expanded; just collapse it
             tabPanel.collapse(animate);
             expandedState.put(tabPanel, Boolean.FALSE);
         } else {
             // find the first expanded panel and collapse it
             boolean didHit = false;
             if (animate) {
                 for (final TabPanel otherTabPanel : visiblePanels) {
                     if (isExpanded(otherTabPanel)) {
                         ((Container)otherTabPanel).addPropertyChangeListener("expanded",
                                 new PropertyChangeListener() {
                                     public void propertyChange(
                                             final PropertyChangeEvent evt) {
                                         ((Container)otherTabPanel).removePropertyChangeListener("expanded", this);
     
                                         tabPanel.expand(animate);
                                         expandedState.put(tabPanel, Boolean.TRUE);
                                     }
                                 });
                         otherTabPanel.collapse(animate);
                         expandedState.put(otherTabPanel, Boolean.FALSE);
                         didHit = true;
                         break;
                     }
                 }
             }
             // Collapse any expanded panels even if they are not visible, without animation.
             // Panels may be non-visible due to search, and when the search is cleared
             // we don't want multiple expanded panels.
             for (final TabPanel panel : panels) {
                 if (!panel.equals(tabPanel) && isExpanded(panel)) {
                     panel.setExpanded(Boolean.FALSE);
                     expandedState.put(panel, Boolean.FALSE);
                 }
             }
             if (!didHit) {
                 tabPanel.expand(animate);
                 expandedState.put(tabPanel, Boolean.TRUE);
             }
         }
     }
    
     /**
      * Obtain the swing list model.
      * 
      * @return The swing list model.
      */
     @Override
     protected DefaultListModel getListModel() {
         return listModel;
     }
     
     /**
      * Determine if a panel is expanded.
      * 
      * @param tabPanel
      *            A <code>TabPanel</code>.
      * @return True if the panel is expanded; false otherwise.
      */
     protected boolean isExpanded(final TabPanel tabPanel) {
         if (expandedState.containsKey(tabPanel)) {
             return expandedState.get(tabPanel).booleanValue();
         } else {
             // NOTE the default panel expanded state can be changed here
             expandedState.put(tabPanel, Boolean.FALSE);
             return isExpanded(tabPanel);
         }
     }
     
     /**
      * Determine if search is applied.
      * 
      * @return True if a search expression is set.
      */
     protected boolean isSearchApplied() {
         return null != searchExpression;
     }
 
     /**
      * Determine if a panel is selected.
      * 
      * @param tabPanel
      *            A <code>TabPanel</code>.
      * @return True if the panel is selected; false otherwise.
      */
     protected boolean isSelected(final TabPanel tabPanel) {
         return (null != selectedPanel && lookupId(tabPanel).equals(lookupId(selectedPanel)));
     }
 
     /**
      * Lookup the panel id for a panel.
      * 
      * @param tabPanel
      *            A <code>TabPanel</code>.
      * @return A panel id <code>T</code>.
      */
     protected abstract T lookupId(final TabPanel tabPanel);
         
     /**
      * Lookup the panel for the corresponding id.
      * 
      * @param uniqueId
      *          The id.
      * @return A <code>TabPanel</code>.
      */
     protected abstract TabPanel lookupPanel(final T panelId);
     
     /**
      * Search for a list of ids through the content provider.
      * 
      * @return A list of ids.
      */
     protected abstract List<T> readSearchResults();
     
     /**
      * Remove the search.
      * 
      * @see #searchExpression
      * @see #searchResults
      * @see #applySearch(String)
      */
     @Override
     protected void removeSearch() {
         debug();
         if (null == searchExpression) {
             return;
         } else {
             searchExpression = null;
             searchResults.clear();
             synchronize();
         }
     }
     
     /**
      * Create a final list of panels. Apply the
      * search results to the list.
      */
     @Override
     protected void synchronizeImpl() {
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
      * Apply the search results.
      *
      */
     private void applySearch() {
         this.searchResults.clear();
         this.searchResults.addAll(readSearchResults());
     }
 }
