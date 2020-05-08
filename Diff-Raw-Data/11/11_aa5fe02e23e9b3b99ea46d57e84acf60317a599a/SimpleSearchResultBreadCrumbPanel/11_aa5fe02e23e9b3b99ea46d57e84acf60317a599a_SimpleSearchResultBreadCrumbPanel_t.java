 package edu.wustl.cab2b.client.ui.viewresults;
 
 import java.awt.CardLayout;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.rmi.RemoteException;
 import java.util.Vector;
 
 import javax.swing.JComponent;
 
 import org.jdesktop.swingx.JXPanel;
 
 import edu.wustl.cab2b.client.ui.RiverLayout;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.pagination.JPageElement;
 import edu.wustl.cab2b.client.ui.pagination.PageElement;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.client.ui.util.CustomSwingWorker;
 import edu.wustl.cab2b.common.datalist.DataRow;
 import edu.wustl.cab2b.common.datalist.IDataRow;
 import edu.wustl.cab2b.common.queryengine.ICab2bQuery;
 import edu.wustl.cab2b.common.queryengine.result.IQueryResult;
 import edu.wustl.cab2b.common.queryengine.result.IRecord;
 import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.common.querysuite.metadata.associations.IAssociation;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * This panel is a container panel for all different panel in simple view of
  * search results. This panel has a bread crumb panel for easy navigation.
  * 
  * @author chetan_bh
  */
 public class SimpleSearchResultBreadCrumbPanel extends Cab2bPanel {
 
     private static final long serialVersionUID = 1L;
 
     //private HashMap<String, List<AttributeInterface>> mapResultLabel = new HashMap<String, List<AttributeInterface>>();
 
     private int panelCount = 0;
 
     /** A vector of display strings for bread crumbs hyperlink. */
     private Vector<String> m_vBreadCrumbs = new Vector<String>();
 
     /**
      * The breadcrumPanel common to all cards.
      */
     private JXPanel m_breadCrumbPanel = null;
 
     /**
      * The panel will house cards, where each card is a panel depicting some
      * details on the results.
      */
     private JXPanel m_resultsPanel = null;
 
     /**
      * Action listener for bread crumbs hyperlink.
      */
     private ActionListener breadCrumbsAL;
 
     /**
      * Pagination components hyperlinks action listener.
      */
     private ActionListener hyperlinkAL;
 
     /**
      * Associated Data hyperlinks action listener;
      */
     private ActionListener associatedDataAL;
 
     //private List<AttributeInterface> attributeList = null;
 
     private ViewSearchResultsPanel viewPanel;
 
     private String currentBreadCrumbName;
 
     public SimpleSearchResultBreadCrumbPanel(IQueryResult queryResult, ViewSearchResultsPanel viewPanel) {
         this.viewPanel = viewPanel;
         initData();
         initGUI(queryResult);
     }
 
     /** Initialize data. */
     private void initData() {
 
     }
 
     public void addPanel(JXPanel panel, String panelName) {
         this.m_resultsPanel.add(panel, panelName);
     }
 
     /** Shows a panel represented by a name, by bringing that panel to the top. */
     private void showPanel(String panelName) {
         Logger.out.debug("panel name : " + panelName);
         CardLayout layout = (CardLayout) this.m_resultsPanel.getLayout();
         layout.show(this.m_resultsPanel, panelName);
         int totalCardPanels = m_resultsPanel.getComponentCount();
         /**
          * have to add My Data list summary panel
          */
         for (int i = 0; i < totalCardPanels; i++) {
             Component comp = m_resultsPanel.getComponent(i);
             if (true == comp.isVisible()) {
                 if (comp instanceof ViewSearchResultsSimplePanel) {
                     ViewSearchResultsSimplePanel showingPanel = (ViewSearchResultsSimplePanel) m_resultsPanel.getComponent(i);
                     showingPanel.addDataSummaryPanel();
                     break;
                 } else if (comp instanceof ResultObjectDetailsPanel) {
                     ResultObjectDetailsPanel showingPanel = (ResultObjectDetailsPanel) m_resultsPanel.getComponent(i);
                     showingPanel.addDataSummaryPanel();
                     break;
                 }
             }
         }
     }
 
     /** Addes a new panel to the top of the stack of existing breadcrumbs panels. */
     private void addBreadCrumbPanel(JXPanel panel, String panelName) {
         this.m_breadCrumbPanel.add(panel, panelName);
     }
 
     /** @see showPanel(String panelName) */
     private void showBreadcrumbPanel(String panelName) {
         CardLayout layout = (CardLayout) this.m_breadCrumbPanel.getLayout();
         layout.show(this.m_breadCrumbPanel, panelName);
     }
 
     /**
      * Initialize GUI.
      * 
      * @param queryResult
      */
     private void initGUI(IQueryResult queryResult) {
         this.setLayout(new RiverLayout());
        currentBreadCrumbName = null;
 
         breadCrumbsAL = new BreadCrumbActionListener(this);
         hyperlinkAL = new HyperlinlActionListener(this);
         associatedDataAL = new AssociatedDataActionListener(this);
 
         /*
          * The breadcrumb panel should be common to all cards, and hence should
          * not be part of any card.
          */
         String key = this.panelCount + "#"
                 + edu.wustl.cab2b.common.util.Utility.getDisplayName(queryResult.getOutputEntity());
         this.m_vBreadCrumbs.add(key);
 
         this.m_breadCrumbPanel = new Cab2bPanel();
         this.m_breadCrumbPanel.setLayout(new CardLayout());
         BreadcrumbPanel breadCrumbPanel = new BreadcrumbPanel(breadCrumbsAL, m_vBreadCrumbs, this.viewPanel);
         currentBreadCrumbName = breadCrumbPanel.getCurrentBreadCrumbName();
         this.m_breadCrumbPanel.add(breadCrumbPanel, "" + this.panelCount);
 
         //Check if you got only one record
         JXPanel simpleSearchResultPanel = ResultPanelFactory.getResultPanel(this, queryResult, null, null);
 
         /*
          * Initialize the panel that will house all cards, and add the first
          * card.
          */
         this.m_resultsPanel = new Cab2bPanel();
         this.m_resultsPanel.setLayout(new CardLayout());
         this.m_resultsPanel.add(simpleSearchResultPanel, "" + this.panelCount);
 
         /* Add the main result panel to the current panel. */
         this.add("hfill", this.m_breadCrumbPanel);
         this.add("br vfill hfill", this.m_resultsPanel);
     }
 
     /** Gets bread-crumbs hyperlink action listener. */
     private ActionListener getBreadCrumbsAL() {
         return breadCrumbsAL;
     }
 
     /** Sets bread-crumbs hyperlink action listener. */
     public void setBreadCrumbsAL(ActionListener breadCrumbsAL) {
         this.breadCrumbsAL = breadCrumbsAL;
     }
 
     /** Gets pagination elements hyperlink action listener. */
     public ActionListener getHyperlinkAL() {
         return hyperlinkAL;
     }
 
     /** Sets pagination elements hyperlink action listener. */
     public void setHyperlinkAL(ActionListener hyperlinkAL) {
         this.hyperlinkAL = hyperlinkAL;
     }
 
     /** Gets associated data hyperlink action listener. */
     public ActionListener getAssociatedDataAL() {
         return associatedDataAL;
     }
 
     /** Sets associated data hyperlinks action listner. */
     public void setAssociatedDataAL(ActionListener associatedDataAL) {
         this.associatedDataAL = associatedDataAL;
     }
 
     /**
      * This action listener class should take care of creating new/existinf
      * panels in the Card layout.
      * 
      * @author chetan_bh
      */
     private class BreadCrumbActionListener implements ActionListener {
         SimpleSearchResultBreadCrumbPanel breadCrumbPanel;
 
         public BreadCrumbActionListener(SimpleSearchResultBreadCrumbPanel panel) {
             this.breadCrumbPanel = panel;
         }
 
         public void actionPerformed(ActionEvent evt) {
             Logger.out.debug("bread crumbs ActionListener : " + evt.getActionCommand());
             Cab2bHyperlink hyperlink = (Cab2bHyperlink) evt.getSource();
             /*
              * Get the user object for the hyperlink. This contains the whole
              * string.
              */
             String hyperlinkText = (String) hyperlink.getUserObject();
 
             int i;
             for (i = 0; i < m_vBreadCrumbs.size(); i++) {
                 String breadCrumbValue = (String) m_vBreadCrumbs.get(i);
                 if (breadCrumbValue.trim().equals(hyperlinkText.trim())) {
                     break;
                 }
             }
             panelCount = i++;
 
             for (int j = i; j < m_vBreadCrumbs.size(); j++) {
                 m_vBreadCrumbs.remove(j--);
                 m_resultsPanel.remove(i);
             }
 
             /*
              * Get the number from the hyperlink text and use that to show the
              * panel.
              */
             String strIndex = hyperlinkText.substring(0, hyperlinkText.indexOf("#"));
             breadCrumbPanel.showPanel(strIndex);
 
             BreadcrumbPanel breadcrumbPanel = new BreadcrumbPanel(breadCrumbPanel.getBreadCrumbsAL(),
                     m_vBreadCrumbs, this.breadCrumbPanel.viewPanel);
             currentBreadCrumbName = breadcrumbPanel.getCurrentBreadCrumbName();
 
             this.breadCrumbPanel.addBreadCrumbPanel(breadcrumbPanel, strIndex);
             this.breadCrumbPanel.showBreadcrumbPanel(strIndex);
         }
     }
 
     /**
      * This action listener class should take care of creating new/existing
      * panels in the Card layout.
      * 
      * @author chetan_bh
      */
     private class HyperlinlActionListener implements ActionListener {
         SimpleSearchResultBreadCrumbPanel searchPanel;
 
         public HyperlinlActionListener(SimpleSearchResultBreadCrumbPanel searchPanel) {
             this.searchPanel = searchPanel;
         }
 
         public void actionPerformed(ActionEvent evt) {
             Cab2bHyperlink<JPageElement> hyperlink = (Cab2bHyperlink<JPageElement>) evt.getSource();
 
             String hyperlinkText = hyperlink.getText();
             JPageElement jPageElement = hyperlink.getUserObject();
             PageElement pageElement = jPageElement.getPageElement();
 
             /* Get the data row corresponding to the clicked element. */
             Vector recordListUserObject = (Vector) pageElement.getUserObject();
 
             DataRow dataRow = (DataRow) recordListUserObject.get(0);
             IRecord record = (IRecord) recordListUserObject.get(1);
 
             ViewSearchResultsSimplePanel currentPanel = (ViewSearchResultsSimplePanel) m_resultsPanel.getComponent(panelCount);
 
             /*
              * Refresh the breadcrumb vector, and pass that instance onto a new
              * instance of the breadcrumb panel.
              */
             int currentCount = ++panelCount;
 
             m_vBreadCrumbs.add(currentCount + "#" + hyperlinkText);
             BreadcrumbPanel breadcrumbPanel = new BreadcrumbPanel(getBreadCrumbsAL(), m_vBreadCrumbs,
                     this.searchPanel.viewPanel);
            currentBreadCrumbName = hyperlinkText;
 
             JXPanel detailsPanel = ResultPanelFactory.getSearchResultPanel(
                                                                            searchPanel,
                                                                            record,
                                                                            dataRow,
                                                                            currentPanel.getIncomingAssociationCollection(),
                                                                            currentPanel.getIntraModelAssociationCollection());
 
             addBreadCrumbPanel(breadcrumbPanel, "" + currentCount);
             showBreadcrumbPanel("" + currentCount);
 
             addPanel(detailsPanel, "" + currentCount);
             showPanel("" + currentCount);
         }
     }
 
     /**
      * This action listener is to handle actions on associated data hyperlinks.
      * 
      * @author chetan_bh
      */
     private class AssociatedDataActionListener implements ActionListener {
 
         private String breadCrumbText;
 
         SimpleSearchResultBreadCrumbPanel breadCrumbPanel;
 
         public AssociatedDataActionListener(SimpleSearchResultBreadCrumbPanel panel) {
             this.breadCrumbPanel = panel;
         }
 
         public void actionPerformed(ActionEvent actionEvent) {
             Cab2bHyperlink hyperlink = (Cab2bHyperlink) actionEvent.getSource();
 
             /* Get the user object associated with that source. */
             final HyperLinkUserObject hyperLinkUserObject = (HyperLinkUserObject) hyperlink.getUserObject();
             IQuery tempQuery = null;
 
             AbstractAssociatedDataPanel linkContainer = (AbstractAssociatedDataPanel) hyperlink.getParent();
             try {
                 tempQuery = linkContainer.getQuery((hyperLinkUserObject.getAssociation()));
             } catch (RemoteException e) {
                 CommonUtils.handleException(e, breadCrumbPanel, true, true, true, false);
             }
             final IQuery query = tempQuery;
             breadCrumbText = Utility.getDisplayName(hyperLinkUserObject.getTargetEntity());
 
             /* Get result by executing the Query in a worker thread. */
             CustomSwingWorker swingWorker = new CustomSwingWorker((JXPanel) breadCrumbPanel) {
                 IQueryResult queryResult = null;
 
                 protected void doNonUILogic() throws RuntimeException {
                     queryResult = CommonUtils.executeQuery((ICab2bQuery) query, (JComponent) breadCrumbPanel);
                 }
 
                 protected void doUIUpdateLogic() throws RuntimeException {
                     /*
                      * Get attributes and set in map, for later when the user is
                      * navigating, and for now directly set it.
                      */
                     IAssociation association = hyperLinkUserObject.getAssociation();
                     IDataRow parentDataRow = hyperLinkUserObject.getParentDataRow();
 
                    currentBreadCrumbName = breadCrumbText;
                     JXPanel resultPanel = ResultPanelFactory.getResultPanel(breadCrumbPanel, queryResult,
                                                                             parentDataRow, association);
                     addToPanel(resultPanel, breadCrumbText);

                 }
             };
             if (query != null) {
                 swingWorker.start();
             } else {
                 addToPanel(ResultPanelFactory.getNoResultFoundPopup(breadCrumbText), breadCrumbText);
             }
         }
 
     }
 
     /**
      * @param simpleSearchResultPanelNew
      * @param breadCrumbText
      */
     private void addToPanel(JXPanel simpleSearchResultPanelNew, String breadCrumbText) {
         int currentCount = ++panelCount;
 
         addPanel(simpleSearchResultPanelNew, "" + currentCount);
         showPanel("" + currentCount);
 
         /*
          * Also we need to refresh the breadcrumb panel,so do the same with the
          * breadcrumb panel.
          */
         m_vBreadCrumbs.add(currentCount + "#" + breadCrumbText);
         BreadcrumbPanel breadcrumbPanel = new BreadcrumbPanel(getBreadCrumbsAL(), m_vBreadCrumbs, this.viewPanel);
        currentBreadCrumbName = breadCrumbText;
 
         addBreadCrumbPanel(breadcrumbPanel, "" + currentCount);
         showBreadcrumbPanel("" + currentCount);
     }
 
     /**
      * @return the currentBreadCrumbName
      */
     public String getCurrentBreadCrumbName() {
         return currentBreadCrumbName;
     }
 
 }
