 /*
  * Created On:  October 7, 2006, 10:36 AM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar.tab;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.EventQueue;
 import java.awt.Rectangle;
 import java.awt.Window;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TimerTask;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JComponent;
 import javax.swing.SwingUtilities;
 
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.log4j.Log4JWrapper;
 import com.thinkparity.codebase.swing.SwingUtil;
 
 import com.thinkparity.ophelia.browser.application.browser.Browser;
 import com.thinkparity.ophelia.browser.application.browser.BrowserSession;
 import com.thinkparity.ophelia.browser.application.browser.component.MenuFactory;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabButtonActionDelegate;
 import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel;
 import com.thinkparity.ophelia.browser.platform.application.Application;
 import com.thinkparity.ophelia.browser.platform.application.ApplicationListener;
 import com.thinkparity.ophelia.browser.platform.util.persistence.Persistence;
 import com.thinkparity.ophelia.browser.platform.util.persistence.PersistenceFactory;
 import com.thinkparity.ophelia.browser.util.localization.Localization;
 
 /**
  * <b>Title:</b>thinkParity Tab Panel Model<br>
  * <b>Description:</b>A model for all tab panels.<br>
  *
  * @author raymond@thinkparity.com
  * @version 1.1.2.2
  */
 public abstract class TabPanelModel<T extends Object> extends TabModel {
 
     /** The application. */
     protected final Browser browser;
 
     /** The model's expanded state map. */
     private final Map<TabPanel, Boolean> expandedState;
 
     /** A list of filtered panels. */
     protected final List<TabPanel> filteredPanels;
 
     /** The swing list model. */
     protected final DefaultListModel listModel;
 
     /** A <code>Localization</code>. */
     protected Localization localization;
 
     /** An apache logger wrapper. */
     protected final Log4JWrapper logger;
 
     /** A list of all panels. */
     protected final List<TabPanel> panels;
 
     /** A parity persistence. */
     protected final Persistence persistence;
     
     /** A user search expression <code>String</code>. */
     protected String searchExpression;
     
     /** A search result list of unique id <code>T</code>s. */
     protected final List<T> searchResults;
     
     /** A <code>BrowserSession</code>. */
     protected BrowserSession session;
 
     /** A <code>java.util.Timer</code>. */
     private java.util.Timer timer;
 
     /** A list of all visible cells. */
     protected final List<TabPanel> visiblePanels;
 
     /** The selected panel id. */
     private T selectedPanelId;
 
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
         addApplicationListener();
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
         checkThread();
         final TabPanel tabPanel = (TabPanel)lookupPanel(panelId);
         if (isExpanded(tabPanel)) {
             toggleExpansion(tabPanel, animate);
         }
     }
 
     /**
      * Delete the selected panel (run appropriate action).
      */
     public void deleteSelectedPanel() {
         checkThread();
         final TabPanel selectedPanel = getSelectedPanel();
         if (null != selectedPanel) {
             deletePanel(selectedPanel);
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
         checkThread();
         final TabPanel tabPanel = (TabPanel)lookupPanel(panelId);
         if (null != tabPanel && !isExpanded(tabPanel)) {
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
         checkThread();
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
      * Obtain the tab button action delegate.
      *
      * @return A <code>TabButtonActionDelegate</code>.
      */
     public abstract TabButtonActionDelegate getTabButtonActionDelegate();
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabModel#initialize()
      */
     @Override
     protected void initialize() {
         initTimer();
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
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabDelegate#isNextPanelExpanded(com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel)
      */
     public Boolean isNextPanelExpanded(final TabPanel tabPanel) {
         final int visibleIndex = visiblePanels.indexOf(tabPanel);
         if (visibleIndex<0 || visibleIndex>=visiblePanels.size()-1) {
             return Boolean.FALSE;
         } else {
             return isExpanded(visiblePanels.get(visibleIndex+1));
         }
     }
 
     /**
      * Determine whether or not the user is online.
      * 
      * @return True if the user is online.
      */
     public Boolean isOnline() {
         return browser.isOnline();
     }
 
     /**
      * Scroll the panel so it is visible.
      * 
      * @param panelId
      *            A panel id <code>T</code>.
      */
     public void scrollPanelToVisible(final T panelId) {
         checkThread();
         final JComponent panel = (JComponent) lookupPanel(panelId);
         if (null != panel) {
             final Rectangle rectangle = panel.getBounds();
             rectangle.x = rectangle.y = 0;
             panel.scrollRectToVisible(rectangle);
         }
     }
 
     /**
      * Select the first panel.
      * Scroll so the panel is visible and request focus.
      */
     public void selectFirstPanel() {
         selectFirstPanel(Boolean.TRUE);
     }
 
     /**
      * Select the first panel.
      * Scroll so the panel is visible.
      * 
      * @param requestFocus
      *            A <code>Boolean</code>, true if request focus in window. 
      */
     public void selectFirstPanel(final Boolean requestFocus) {
         checkThread();
         if (visiblePanels.size() > 0) {
             selectPanel(visiblePanels.get(0), Boolean.TRUE, requestFocus);
         }
     }
 
     /**
      * Select the last panel.
      * Scroll so the panel is visible and request focus.
      */
     public void selectLastPanel() {
         checkThread();
         if (visiblePanels.size() > 0) {
             selectPanel(visiblePanels.get(visiblePanels.size() - 1),
                     Boolean.TRUE, Boolean.TRUE);
         }
     }
 
     /**
      * Select the next panel, or the first panel if none are selected.
      * Scroll so the panel is visible and request focus.
      */
     public void selectNextPanel() {
         checkThread();
         if (visiblePanels.size() > 0) {
             final TabPanel oldSelectedPanel = getSelectedPanel();
             if (null == oldSelectedPanel) {
                 selectPanel(visiblePanels.get(0), Boolean.TRUE, Boolean.TRUE);
             } else {
                 final int visibleIndex = visiblePanels.indexOf(oldSelectedPanel);
                 if (visibleIndex < 0) {
                     selectPanel(visiblePanels.get(0), Boolean.TRUE, Boolean.TRUE);
                 } else if (visibleIndex < visiblePanels.size()-1) {
                     selectPanel(visiblePanels.get(visibleIndex+1), Boolean.TRUE, Boolean.TRUE);
                 }
             }
         }
     }
 
     /**
      * Select the panel and request focus.
      * 
      * @param panelId
      *            A panel id <code>T</code>.    
      */
     public void selectPanel(final T panelId) {
         checkThread();
         final TabPanel panel = lookupPanel(panelId);
         // Panel can be null in some cases, for example if a notification refers
         // to an invitation which has already been accepted.
         if (null != panel) {
             selectPanel(panel, Boolean.FALSE, Boolean.TRUE);
         }
     }
 
     /**
      * Select the panel and request focus.
      * 
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabDelegate#selectPanel(com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.TabPanel)
      */
     public void selectPanel(final TabPanel tabPanel) {
         selectPanel(tabPanel, Boolean.FALSE, Boolean.TRUE);
     }
 
     /**
      * Select the panel.
      * 
      * @param tabPanel
      *            A <code>TabPanel</code>. 
      * @param scrollPanelToVisible
      *            A <code>Boolean</code>, true if scroll the panel so it is visible.
      * @param requestFocus
      *            A <code>Boolean</code>, true if request focus in window. 
      */
     public void selectPanel(final TabPanel tabPanel,
             final Boolean scrollPanelToVisible, final Boolean requestFocus) {
         checkThread();
         if (requestFocus) {
             requestFocusInWindow(tabPanel);
         }
         if (isSelected(tabPanel)) {
             return;
         }
         final TabPanel oldSelectedPanel = getSelectedPanel();
         if (null != oldSelectedPanel) {
             oldSelectedPanel.setSelected(Boolean.FALSE);
         }
         selectedPanelId = lookupId(tabPanel);
         tabPanel.setSelected(Boolean.TRUE);
         if (scrollPanelToVisible) {
             scrollPanelToVisible(selectedPanelId);
         }
     }
 
     /**
      * Select the previous panel, or the first panel if none are selected.
      * Scroll so the panel is visible and request focus.
      */
     public void selectPreviousPanel() {
         checkThread();
         if (visiblePanels.size() > 0) {
             final TabPanel oldSelectedPanel = getSelectedPanel();
             if (null == oldSelectedPanel) {
                 selectPanel(visiblePanels.get(0), Boolean.TRUE, Boolean.TRUE);
             } else {
                 final int visibleIndex = visiblePanels.indexOf(oldSelectedPanel);
                 if (visibleIndex < 0) {
                     selectPanel(visiblePanels.get(0), Boolean.TRUE, Boolean.TRUE);
                 } else if (visibleIndex > 0) {
                     selectPanel(visiblePanels.get(visibleIndex-1), Boolean.TRUE, Boolean.TRUE);
                 }
             }
         }
     }
 
     /**
      * Set localization.
      *
      * @param localization
      *      A Localization.
      */
     public void setLocalization(final Localization localization) {
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
         checkThread();
         if (!tabPanel.isSetExpandedData()) {
             applyBusyIndicator();
             setExpandedPanelData(tabPanel);
             removeBusyIndicator();
         }
         doToggleExpansion(tabPanel, animate);
         synchronize();
     }
 
     /**
      * Apply a busy indicator.
      */
     protected void applyBusyIndicator() {
         SwingUtil.setCursor(browser.getMainWindow(), java.awt.Cursor.WAIT_CURSOR);
     }
 
     /**
      * Apply a busy indicator.
      */
     protected void removeBusyIndicator() {
         SwingUtil.setCursor(browser.getMainWindow(), null);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabModel#applySearch(java.lang.String)
      *
      */
     @Override
     protected void applySearch(final String searchExpression) {
         checkThread();
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
      * @see com.thinkparity.ophelia.browser.application.browser.display.avatar.tab.TabModel#applySearchFilter()
      */
     @Override
     protected void applySearchFilter() {
         checkThread();
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
      * Clear all panels.
      *
      */
     protected void clearPanels() {
         checkThread();
         panels.clear();
     }
 
     /**
      * Delete the panel (run appropriate action).
      * 
      * @param tabPanel
      *            A <code>TabPanel</code>.
      */
     protected abstract void deletePanel(final TabPanel tabPanel);
 
     /**
      * Toggle the expansion of a single panel.
      * 
      * @param tabPanel
      *            A <code>TabPanel</code>.
      * @param animate
      *            Animate flag <code>Boolean</code>.             
      */
     protected void doToggleExpansion(final TabPanel tabPanel, final Boolean animate) {
         checkThread();
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
                                         synchronize();
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
         checkThread();
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
         return (null != selectedPanelId && lookupId(tabPanel).equals(selectedPanelId));
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
      * Remove expanded state.
      * 
      * @param tabPanel
      *            A <code>TabPanel</code>.
      */
     protected void removeExpandedState(final TabPanel tabPanel) {
         expandedState.remove(tabPanel);
     }
 
     /**
      * Remove the search.
      * 
      * @see #searchExpression
      * @see #searchResults
      * @see #applySearch(String)
      */
     @Override
     protected void removeSearch() {
         checkThread();
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
      * Request focus on the selected panel, if there is one.
      */
     protected void requestFocusInSelectedPanel() {
         final TabPanel selectedPanel = getSelectedPanel();
         if (null != selectedPanel) {
             requestFocusInWindow(selectedPanel);
         }
     }
 
     /**
      * Request focus.
      * 
      * @param tabPanel
      *            A <code>TabPanel</code>.
      */
     protected void requestFocusInWindow(final TabPanel tabPanel) {
         ((JComponent) tabPanel).requestFocusInWindow();
         if (!((JComponent) tabPanel).isFocusOwner()) {
             // focus is gained more consistently with invokeLater
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     if (!MenuFactory.isPopupMenu() && !browser.isBusy()) {
                         ((JComponent) tabPanel).requestFocusInWindow();
                     }
                 }
             });
         }
     }
 
     /**
      * Set additional data that a panel needs when expanded.
      * 
      * @param tabPanel
      *            A <code>TabPanel</code>.
      */
     protected abstract void setExpandedPanelData(final TabPanel tabPanel);
 
     /**
      * Create a final list of panels. Apply the
      * search results to the list.
      */
     @Override
     protected void synchronizeImpl() {
         final Boolean requestFocus = isFocusedPanel();
         checkThread();
         debug();
         applySearchFilter();
         applyFilter();
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
         // if there is no selection, or if the selection is not visible,
         // select the first panel.
         if (!isSelectedPanel() ||
                 (isSelectedPanel() && !visiblePanels.contains(getSelectedPanel()))) {
             selectFirstPanel(Boolean.FALSE);
         }
         // if there was a panel with focus at the start, request focus again
         if (requestFocus) {
             requestFocusInWindow(getSelectedPanel());
         }
         debug();
     }
 
     /**
      * Add an application listener.
      */
     private void addApplicationListener() {
         browser.addListener(new ApplicationListener() {
             public void notifyEnd(final Application application) {
                 if (null != timer) {
                     timer.cancel();
                 }
             }
             public void notifyHibernate(Application application) {}
             public void notifyRestore(Application application) {}
             public void notifyStart(Application application) {}           
         });
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
      * Check we are on the AWT event dispatching thread.
      */
     private void checkThread() {
         Assert.assertTrue(EventQueue.isDispatchThread(), "Tab panel model not on the AWT event dispatch thread.");
     }
 
     /**
      * Get the focused panel.
      * Only the selected panel can have focus but there may be times
      * that no panel has focus.
      * 
      * @return The focused <code>TabPanel</code>, or null if there is none.
      */
     private TabPanel getFocusedPanel() {
         if (isSelectedPanel()) {
             final TabPanel panel = getSelectedPanel();
             // isFocusOwner() is not used here because it will return
             // false if the parent window does not currently have focus.
             final Window window = javax.swing.SwingUtilities.getWindowAncestor((Component)panel);
             if (null != window) {
                final Component focusOwner = window.getMostRecentFocusOwner();
                if (null != focusOwner && focusOwner.equals((Component)panel)) {
                     return panel;
                 }
             }
         }
         return null;
     }
 
     /**
      * Get the selected panel.
      * Returns null if no panel is selected.
      * Also returns null if a panel is selected and later deleted.
      * 
      * @return The selected <code>TabPanel</code>, or null if there is none.
      */
     private TabPanel getSelectedPanel() {
         if (null == selectedPanelId) {
             return null;
         } else {
             return lookupPanel(selectedPanelId);
         }
     }
 
     /**
      * Initialize the timer to refresh panels each day just past midnight.
      */
     private void initTimer() {
         final TimerTask timerTask = new TimerTask() {
             @Override
             public void run() {
                 SwingUtil.ensureDispatchThread(new Runnable() {
                     public void run() {
                         refreshPanels();
                         synchronize();
                     }
                 });
                 initTimer();
             }
         };
         scheduleTaskPastMidnight(timerTask);
     }
 
     /**
      * Determine if there is a focused panel.
      * 
      * @return true if there is a focused panel.
      */
     private Boolean isFocusedPanel() {
         return (null != getFocusedPanel());
     }
 
     /**
      * Determine if there is a selected panel.
      * 
      * @return true if there is a selected panel.
      */
     private Boolean isSelectedPanel() {
         return (null != getSelectedPanel());
     }
 
     /**
      * Refresh all panels.
      */
     private void refreshPanels() {
         for (final TabPanel panel : panels) {
             panel.refresh();
         }
     }
 
     /**
      * Schedule the provided timer task to run just past midnight.
      * 
      * @param timerTask
      *            The <code>TimerTask</code>.
      */
     private void scheduleTaskPastMidnight(final TimerTask timerTask) {
         final Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 1);
         calendar.add(Calendar.DAY_OF_YEAR, 1);
         if (null != timer) {
             timer.cancel();
         }
         timer = new java.util.Timer();
         timer.schedule(timerTask, calendar.getTime());
     }
 }
