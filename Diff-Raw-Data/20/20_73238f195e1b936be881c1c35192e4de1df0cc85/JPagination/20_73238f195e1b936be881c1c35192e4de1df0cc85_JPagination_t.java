 package edu.wustl.cab2b.client.ui.pagination;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Vector;
 
 import javax.swing.event.EventListenerList;
 
 import org.jdesktop.swingx.JXHyperlink;
 import org.jdesktop.swingx.JXPanel;
 
 import edu.wustl.cab2b.client.ui.RiverLayout;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 
 /**
  * A Swing component to perform pagination of long list of user data.
  * This component is designed to simulate the Browser kind of pagination
  * of search results.
  * 
  * This component can be an alternative the ScrollPane component, with different
  * technique to display a long list of user data.
  * 
  * List of Property Change Listeners implemented and working.
  * 1) isSelectable
  * 2) isGroupActionEnabled // TODO this should automatically get disabled if isSelectable is disabled.
  * 3) elementsPerPage
  * 
  * @author chetan_bh
  */
 public class JPagination extends Cab2bPanel implements PropertyChangeListener, MouseWheelListener {
 
     private static final long serialVersionUID = 1L;
 
     /**
      * A boolean to set selection feature enabled or disabled.
      */
     private boolean isSelectable = true;
 
     /**
      * A boolean to set group actions set displayed or not displayed.
      */
     private boolean isGroupActionEnabled = true;
 
     /**
      * A model to hold user data.
      */
     private PaginationModel paginationModel;
 
     /**
      * A Panel to display some common action elements like "Select All", "Clear All", etc.
      */
     private JGroupActionPanel groupActionPanel;
 
     /**
      * A Panel to display the page indexes, for user to navigate through the data using
      * these indices.
      */
     private JPageBar pageBar;
 
     /**
      * A Panel to display the page elements. 
      */
     private JXPanel pagePanel;
 
     /**
      * S actionListenerList, so that many listener can register
      * to listen for page element actions.
      */
     private Vector<ActionListener> pageElementActionListenerList = new Vector<ActionListener>();
 
     /**
      * A selection model for the entire <code>JPagination</code> component.
      */
     private PageSelectionModel pageSelectionModel;
 
     /**
      * A selection listener for the page element selections.
      */
     private Vector<PageSelectionListener> pageSelectionListenerList = new Vector<PageSelectionListener>();
 
     @Deprecated
     protected EventListenerList listenerList = new EventListenerList();
 
     private Component parentComponent;
 
     /**
      * Auto-Resize will not work for Alphabetic Pager.
      */
     boolean automaticPageResize = true;
 
     boolean mouseWheelEnabled = true;
 
     private JPageElement selectedJPageElement;
 
     private Cab2bHyperlink selectAllHyperlink;
 
     private Cab2bHyperlink clearAllHyperlink;
 
     private Cab2bHyperlink invertSelectionHyperlink;
 
     public JPagination() {
         this(null);
     }
 
     public JPagination(Vector<PageElement> data) {
         this(data, new NumericPager(data));
     }
 
     public JPagination(Vector<PageElement> data, Pager pager) {
         this(data, pager, null, false);
     }
 
     public JPagination(Vector<PageElement> data, Pager pager, Component parentComponent, boolean autoPageResize) {
         paginationModel = new PaginationModel(data, pager);
         pageSelectionModel = new DefaultPageSelectionModel(this);
         this.addPropertyChangeListener(this);
 
         if (parentComponent == null) {
             automaticPageResize = false;
         } else {
             this.automaticPageResize = autoPageResize;
             this.parentComponent = parentComponent;
             parentComponent.addComponentListener(new PaginationComponentListener());
         }
 
         initGUI();
         addNewMouseWheelListener();
     }
 
     private void initGUI() {
         this.removeAll();
         pageBar = new JPageBar(paginationModel.pager.getAllPageIndices(), new Vector(), this);
         setLayout(new BorderLayout());
 
         if (isGroupActionEnabled) {
             groupActionPanel = new JGroupActionPanel();
             add(groupActionPanel, BorderLayout.NORTH);
         }
 
         pagePanel = getPagePanel(paginationModel.firstPage());
         if (automaticPageResize)
             pagePanel.addComponentListener(new PaginationComponentListener());
         add(pagePanel, BorderLayout.CENTER);
         add(pageBar, BorderLayout.SOUTH);
 
     }
 
    /**
     * This method is used to reset all the hyperlinks in JPagination into labels   
     *
     */
    public void resetAllLabels() {
        for (Component component : pagePanel.getComponents()) {
            if (component instanceof JPageElement) {
                ((JPageElement) component).resetLabel();
            }
        }
    }

     private void addNewMouseWheelListener() {
         MouseWheelListener[] mouseWheelListeners = this.getMouseWheelListeners();
         for (int i = 0; i < mouseWheelListeners.length; i++) {
             MouseWheelListener mouseWheelListener = mouseWheelListeners[i];
             this.removeMouseWheelListener(mouseWheelListener);
         }
 
         addMouseWheelListener(this);
         this.validateTree();
     }
 
     public void setPrefSize(Dimension preferredSize) {
         super.setPreferredSize(preferredSize);
     }
 
     public Dimension getPrefSize() {
         return this.getPreferredSize();
     }
 
     /**
      * Returns a Panel which is embedded with page element components represneting the a page.
      * @param pageElements a vector of page elements also called as page.
      * @return a page panel.
      * 
      * TODO this function should now take into account pageLayoutOrientation and pageDimension
      * attributes into account while laying out page elements in the page panel.
      * 
      */
     public JXPanel getPagePanel(Vector<PageElement> pageElements) {
         JXPanel pagePanel = new Cab2bPanel();
 
         String currentPageIndex = paginationModel.getCurrentPageIndex();
         pagePanel.setLayout(new RiverLayout(0, 8));
 
         for (PageElement element : pageElements) {
             int indexInPage = pageElements.indexOf(element);
             PageElementIndex pageElementIndex = new PageElementIndex(currentPageIndex, indexInPage);
 
             JPageElement pageElementPanel = new JPageElement(this, element, isSelectable, pageElementIndex);
             pageElementPanel.addHyperlinkActionListeners(this.pageElementActionListenerList);
             pagePanel.add("br", pageElementPanel);
         }
 
         if (automaticPageResize) {
             pagePanel.addComponentListener(new PaginationComponentListener());
         }
 
         return pagePanel;
     }
 
     public void addPageElementActionListener(ActionListener actionListener) {
         for (int i = 0; i < pagePanel.getComponentCount(); i++) {
             JPageElement pageElementComp = (JPageElement) pagePanel.getComponent(i);
             pageElementComp.addHyperlinkActionListener(actionListener);
         }
         pageElementActionListenerList.add(actionListener);
     }
 
     //TODO This looks like not conforming to JavaBeans standards
     //     This should set actionlisteners of each pageelements actionlistener to null
     //     Just like previous function.
     public void removePageElementActionListener(ActionListener actionListener) {
         pageElementActionListenerList.remove(actionListener);
         for (int i = 0; i < pagePanel.getComponentCount(); i++) {
             JPageElement pageElementComp = (JPageElement) pagePanel.getComponent(i);
             pageElementComp.removeHyperlinkActionListener(actionListener);
         }
     }
 
     /**
      * Gets the current action listener.
      * @return
      */
     public Vector<ActionListener> getPageElementActionListners() {
         return this.pageElementActionListenerList;
     }
 
     /**
      * A method to change the current page panel to a new page panel.
      * @param newPagePanel
      */
     public void changePagePanel(JXPanel newPagePanel) {
         this.remove(pagePanel);
         this.pagePanel = newPagePanel;
         this.add(pagePanel);
         this.updateUI();
     }
 
     /**
      * Returns the current pagination model.
      * @return
      */
     public PaginationModel getPaginationModel() {
         return paginationModel;
     }
 
     /**
      * Sets the pagination model to new one.
      * @param paginationModel new pagination model
      */
     public void setPaginationModel(PaginationModel paginationModel) {
         PaginationModel oldValue = this.paginationModel;
         this.paginationModel = paginationModel;
         firePropertyChange("paginationModel", oldValue, this.paginationModel);
     }
 
     /**
      * Changes the pager in the pagination model.
      * @param newPager new pager.
      */
     public void changePager(Pager newPager) {
         Pager oldValue = null; //paginationModel.getPa
         paginationModel.changePager(newPager);
         firePropertyChange("pager", oldValue, newPager);
     }
 
     /**
      * Returns the current <code>PageSelectionModel</code>.
      * @return
      */
     public PageSelectionModel getPageSelectionModel() {
         return pageSelectionModel;
     }
 
     /**
      * Sets a new <code>PageSelectionModel</code>.
      * @param pageSelectionModel new page selection model.
      */
     public void setPageSelectionModel(PageSelectionModel pageSelectionModel) {
         PageSelectionModel oldValue = this.pageSelectionModel;
         this.pageSelectionModel = pageSelectionModel;
         firePropertyChange("pageSelectionModel", oldValue, pageSelectionModel);
     }
 
     /**
      * Returns true if selection is enabled, else false. 
      * @return
      */
     public boolean isSelectable() {
         return isSelectable;
     }
 
     /**
      * Enables/Diables selections of the page elements.
      * @param newValue
      */
     public void setSelectableEnabled(boolean newValue) {
         if (newValue != this.isSelectable) {
             Boolean oldValue = isSelectable;
             this.isSelectable = newValue;
             firePropertyChange("isSelectable", oldValue, new Boolean(newValue));
         }
     }
 
     /**
      * Enables/Disables group action panel. 
      * @param newValue
      */
     public void setGroupActionEnabled(boolean newValue) {
         if (newValue != this.isGroupActionEnabled) {
             Boolean oldValue = this.isGroupActionEnabled;
             this.isGroupActionEnabled = newValue;
             firePropertyChange("isGroupActionEnabled", oldValue, new Boolean(newValue));
         }
     }
 
     /**
      * Sets element per page parameter.
      * @param newValue
      */
     public void setElementsPerPage(int newValue) {
         int oldValue = paginationModel.getElementsPerPage();
         if (newValue != oldValue && newValue > 0) {
             firePropertyChange("elementsPerPage", new Integer(oldValue), new Integer(newValue));
         }
     }
 
     /**
      * Returns elements per page parameter.
      * @return
      */
     public int getElementsPerPage() {
         return paginationModel.getElementsPerPage();
     }
 
     /**
      * Returns true if group actions are eanbled, else false.
      * @return
      */
     public boolean isGroupActionEnabled() {
         return isGroupActionEnabled;
     }
 
     /**
      * This function propagates the selectionEvent to selectionModel, where all the selections
      * are maintained by selectionModels datastructure.
      * @param pageSelectionEvent
      */
     protected void fireSelectionValueChanged(PageSelectionEvent pageSelectionEvent) {
         for (PageSelectionListener pageSelectionListener : pageSelectionListenerList) {
             pageSelectionListener.selectionChanged(pageSelectionEvent);
         }
     }
 
     /**
      * Returns a collection of selected page elements user object.
      * @return
      */
     public Vector getSelectedPageElementsUserObjects() {
         Vector<Object> selectedUserObjects = new Vector<Object>();
 
         Vector<PageElementIndex> selectedPageIndices = pageSelectionModel.getSelectedPageIndices();
         for (int i = 0; i < selectedPageIndices.size(); i++) {
             PageElementIndex pageElementIndex = selectedPageIndices.get(i);
 
             PageElement selectedPageElement = paginationModel.getPageElement(pageElementIndex);
             if (selectedPageElement == null)
                 continue;
             Object userObjectOfSelectedPageElement = selectedPageElement.getUserObject();
             selectedUserObjects.add(userObjectOfSelectedPageElement);
         }
         return selectedUserObjects;
     }
 
     /**
      * Property change listener method.
      */
     public void propertyChange(PropertyChangeEvent pcEvent) {
         String propertyName = pcEvent.getPropertyName();
         Object newValue = pcEvent.getNewValue();
 
         if (propertyName.equals("isGroupActionEnabled")) {
             Boolean newBoolValue = (Boolean) newValue;
             if (newBoolValue) {
                 this.isGroupActionEnabled = newBoolValue;
                 groupActionPanel = new JGroupActionPanel();
                 add(groupActionPanel, BorderLayout.NORTH);
             } else {
                 this.isGroupActionEnabled = newBoolValue;
                 this.remove(groupActionPanel);
             }
             this.revalidate();
         } else if (propertyName.equals("elementsPerPage")) {
             int newElementsPerPage = ((Integer) newValue).intValue();
             if (getElementsPerPage() != newElementsPerPage) {
                 paginationModel.setElementsPerPage(newElementsPerPage);
                 initGUI();
                 this.revalidate();
                 this.updateUI();
             }
         }
     }
 
     public void addPageSelectionListener(PageSelectionListener pageSelectionListener) {
         if (pageSelectionListener != null)
             pageSelectionListenerList.add(pageSelectionListener);
     }
 
     public void removePageSelectionListener(PageSelectionListener pageSelectionListener) {
         if (pageSelectionListener != null)
             pageSelectionListenerList.remove(pageSelectionListener);
     }
 
     /**
      * A Panel to hold common page action hyperlinks.
      * 
      * @author chetan_bh
      */
     private class JGroupActionPanel extends Cab2bPanel implements ActionListener {
         private static final long serialVersionUID = 1L;
 
         private String selectAllText = PaginationConstants.SELECT_ALL_TEXT;
 
         private String clearAllText = PaginationConstants.CLEAR_ALL_TEXT;
 
         private String invertSelectionText = PaginationConstants.INVERT_SELECTION_TEXT;
 
         public JGroupActionPanel() {
             this(PaginationConstants.SELECT_ALL_TEXT, PaginationConstants.CLEAR_ALL_TEXT,
                     PaginationConstants.INVERT_SELECTION_TEXT);
         }
 
         public JGroupActionPanel(String selectAllText, String clearAllText, String invertSelectionText) {
             if (selectAllText != null && !(selectAllText.trim().equals("")))
                 this.selectAllText = selectAllText;
 
             if (clearAllText != null && !(clearAllText.trim().equals("")))
                 this.clearAllText = clearAllText;
 
             if (invertSelectionText != null && !(invertSelectionText.trim().equals("")))
                 this.invertSelectionText = invertSelectionText;
 
             initGUI();
         }
 
         /**
          * Initialize the GUI for Group Action panel.
          */
         private void initGUI() {
             selectAllHyperlink = new Cab2bHyperlink();
             selectAllHyperlink.setText(selectAllText);
             selectAllHyperlink.addActionListener(this);
             this.add("br", selectAllHyperlink);
 
             clearAllHyperlink = new Cab2bHyperlink();
             clearAllHyperlink.setText(clearAllText);
             clearAllHyperlink.addActionListener(this);
             this.add("tab", clearAllHyperlink);
 
             invertSelectionHyperlink = new Cab2bHyperlink();
             invertSelectionHyperlink.setText(invertSelectionText);
             invertSelectionHyperlink.addActionListener(this);
             this.add("tab", invertSelectionHyperlink);
         }
 
         /**
          * Action listener for Group action hyperlinks.
          */
         public void actionPerformed(ActionEvent e) {
             JXHyperlink source = (JXHyperlink) e.getSource();
             String sourceText = source.getText();
             String currentPageIndex = paginationModel.getCurrentPageIndex();
 
             if (sourceText.equals(selectAllText)) {
                 pageSelectionModel.selectAll();
             } else if (sourceText.equals(clearAllText)) {
                 pageSelectionModel.clearAll();
             } else {
                 pageSelectionModel.invertAll();
             }
             JXPanel currentPagePanel = getPagePanel(paginationModel.getPage(currentPageIndex));
             changePagePanel(currentPagePanel);
         }
     }
 
     /**
      * This is a component listener for page panel as well as a parent component.
      * TODO Needs to do some more work on this.
      * 
      * @author chetan_bh
      */
     class PaginationComponentListener extends ComponentAdapter {
         public void componentResized(ComponentEvent e) {
             /**
              * There are many conditions that needs to be checked before starting rezise.
              * 1) automaticPageResize should be enabled.
              * 2) No page resize for last numeric pager.
              * 3) No page resize for non-numeric level-1 pager.
              */
             // For Condition 1.
             if (automaticPageResize == false)
                 return;
 
             // For Condition 3.
             if (!paginationModel.getPagerName().equals(PaginationConstants.NUMERIC_PAGER))
                 return;
 
             Dimension pagePanelSize = pagePanel.getSize();
             Dimension pagePanelPreferredSize = pagePanel.getPreferredSize();
             if (pagePanelSize.height == 0 && pagePanelSize.width == 0)
                 return;
 
             /*
              * On an approximate page elements need 47 pixels each; <<-- this is a kind of assumption which may not be correct all the time. 
              * TODO Need to find proper solution.
              */
             int difference = Math.round(Math.round(pagePanelSize.getHeight() - pagePanelPreferredSize.getHeight()));
             int currentElementsPerPage = getElementsPerPage();
 
             //	For Condition 2.
             if (!paginationModel.hasNextPage() && difference > 50)
                 return;
 
             // TODO Need to remove these hardcoding like 50, -10.
             if (difference > 55) {
                 int increment = difference / 50;
                 setElementsPerPage(currentElementsPerPage + increment);
 
             } else if (difference < -1) {
                 int decrement = difference / 50;
                 decrement -= 1;
                 setElementsPerPage(currentElementsPerPage + decrement);
             }
         }
     }
 
     public void mouseWheelMoved(MouseWheelEvent e) {
         /* Return without doing nothing, if the mouse wheel support is not enabled for JPagination. */
         if (!mouseWheelEnabled)
             return;
 
         if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
             /* Wheel rotation is either 1 or -1. */
             int wheelRotation = e.getWheelRotation();
             String currentPageIndex = "";
             if (wheelRotation == 1) {/* If mouse wheel is rotated down/toward the user.  -> move behind, decrease the page index. */
                 currentPageIndex = pageBar.sequentialNavigation(pageBar.getPreviousPageText());
             } else if (wheelRotation == -1) {/* If mouse wheel is rotated up/away from the user. -> move forward, increase the page index. */
                 currentPageIndex = pageBar.sequentialNavigation(pageBar.getNextPageText());
             }
             pageBar.displayLinkColor(currentPageIndex);
 
         }
     }
 
     /**
      * @return the selectedJPageElement
      */
     public JPageElement getSelectedJPageElement() {
         return selectedJPageElement;
     }
 
     /**
      * @param selectedJPageElement the selectedJPageElement to set
      */
     public void setSelectedJPageElement(JPageElement selectedJPageElement) {
         this.selectedJPageElement = selectedJPageElement;
     }
 
     public void setPageLinksDisabled() {
         selectAllHyperlink.setEnabled(false);
         clearAllHyperlink.setEnabled(false);
         invertSelectionHyperlink.setEnabled(false);
     }
 
     /**
      * @return the parentComponent
      */
     public Component getParentComponent() {
         return parentComponent;
     }
 
 }
