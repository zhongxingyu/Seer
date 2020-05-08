 package edu.wustl.cab2b.client.ui.viewresults;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GradientPaint;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import javax.swing.JButton;
 import javax.swing.JScrollPane;
 
 import org.jdesktop.swingx.JXPanel;
 import org.jdesktop.swingx.JXTitledPanel;
 import org.jdesktop.swingx.painter.gradient.BasicGradientPainter;
 
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.wustl.cab2b.client.ui.MainSearchPanel;
 import edu.wustl.cab2b.client.ui.RiverLayout;
 import edu.wustl.cab2b.client.ui.SaveDatalistPanel;
 import edu.wustl.cab2b.client.ui.SearchNavigationPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bTitledPanel;
 import edu.wustl.cab2b.client.ui.mainframe.GlobalNavigationPanel;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.client.ui.util.CustomSwingWorker;
 import edu.wustl.cab2b.common.datalist.DataRow;
 import edu.wustl.cab2b.common.datalist.IDataRow;
 import edu.wustl.cab2b.common.ejb.EjbNamesConstants;
 import edu.wustl.cab2b.common.ejb.queryengine.QueryEngineBusinessInterface;
 import edu.wustl.cab2b.common.ejb.queryengine.QueryEngineHome;
 import edu.wustl.cab2b.common.queryengine.result.IQueryResult;
 import edu.wustl.cab2b.common.queryengine.result.IRecord;
 import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.common.querysuite.metadata.associations.IInterModelAssociation;
 import edu.wustl.common.util.logger.Logger;
 
 public abstract class ResultPanel extends Cab2bPanel {
 
     protected IQueryResult<IRecord> queryResult;
 
     protected JButton addToDataListButton;
 
     protected Cab2bButton m_applyAllButton;
 
     /**
      * myDataListPanel and myDataListTitledPanel  
      * caontains a summary of data items added to the data list
      * parent panel is used to adjust location of titlied panel  
      */
     protected static JXPanel myDataListPanel;
 
     protected static JXTitledPanel myDataListTitledPanel;
 
     protected static Cab2bPanel myDataListParentPanel;
 
     protected SimpleSearchResultBreadCrumbPanel searchPanel;
 
     protected Collection<AssociationInterface> incomingAssociationCollection;
 
     protected List<IInterModelAssociation> intraModelAssociationCollection;
 
     /**
      * @return list of selected objects to be added to the data list
      */
     abstract List<IDataRow> getSelectedDataRows();
 
     abstract public void doInitialization();
 
     abstract public void addDataSummaryPanel();
 
     public ResultPanel(
             SimpleSearchResultBreadCrumbPanel searchPanel,
             Collection<AssociationInterface> incomingAssociationCollection,
             List<IInterModelAssociation> intraModelAssociationCollection) {
         this.searchPanel = searchPanel;
         this.incomingAssociationCollection = incomingAssociationCollection;
         this.intraModelAssociationCollection = intraModelAssociationCollection;
     }
 
     protected void initDataListButtons() {
 
         addToDataListButton = new Cab2bButton("Add To Data List");
         addToDataListButton.setPreferredSize(new Dimension(140, 22));
         addToDataListButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 List<IDataRow> dataRows = getSelectedDataRows();
                 MainSearchPanel.getDataList().addDataRows(dataRows);
 
                 updateMyDataListPanel();
                 SaveDatalistPanel.isDataListSaved = false;
 
                 SearchNavigationPanel.messageLabel.setText(" *Added " + dataRows.size() + " elements to data list");
                 updateUI();
             }
         });
 
         // Add Apply All button to apply currently added datalist options
         // to the currently selected objects.
         m_applyAllButton = new Cab2bButton("Apply Data List");
         m_applyAllButton.setPreferredSize(new Dimension(130, 22));
         m_applyAllButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 List<IDataRow> dataRows = getSelectedDataRows();
                 // Perform apply all action
                 if ((dataRows.size() > 0) && (false == MainSearchPanel.getDataList().isTreeEmpty())) {
                     performApplyAllAction(dataRows);
                 }
             }
         });
     }
 
     /**
      * Method to perform apply all action
      * for currently selected objects 
      */
     public void performApplyAllAction(final List<IDataRow> selectedUserObjects) {
         /* Get result by executing the Query in a worker thread. */
         CustomSwingWorker swingWorker = new CustomSwingWorker(this) {
             @Override
             protected void doNonUILogic() throws RuntimeException {
                 // Get the path of current entity
                 List<IDataRow> pathEnitites = new ArrayList<IDataRow>();
                 IDataRow dataRow = (IDataRow) selectedUserObjects.get(0);
                 while (dataRow != null) {
                     pathEnitites.add(0, dataRow);
                     dataRow = dataRow.getParent();
                 }
                 List<IDataRow> entityTreetoFetch = MainSearchPanel.getDataList().getTreeForApplyAll(pathEnitites);
                 if (entityTreetoFetch.size() == 0)
                     return;
                 // For every selected entity fetch corresponding data
                 // from data services and add it to data list
                 QueryEngineBusinessInterface queryEngineBus = (QueryEngineBusinessInterface) CommonUtils.getBusinessInterface(
                                                                                                                               EjbNamesConstants.QUERY_ENGINE_BEAN,
                                                                                                                               QueryEngineHome.class);
                 List<IDataRow> parentRows = new ArrayList<IDataRow>();
                 for (int i = 0; i < selectedUserObjects.size(); i++) {
                     parentRows.add((IDataRow) selectedUserObjects.get(i));
                 }
                 Collection<Callable<QueryResultObject>> queryCallables = new ArrayList<Callable<QueryResultObject>>();
                 List<IDataRow> childRows = entityTreetoFetch.get(0).getChildren();
                 for (int i = 0; i < parentRows.size(); i++) {
                     MainSearchPanel.getDataList().addDataRow(parentRows.get(i));
                     for (int j = 0; j < childRows.size(); j++) {
                         queryCallables.add(new QueryExecutionCallable(parentRows.get(i), childRows.get(j),
                                 queryEngineBus, childRows.get(j).getChildren()));
                     }
                 }
                 fetchApplyAllResults(queryCallables, queryEngineBus);
             }
 
             @Override
             protected void doUIUpdateLogic() throws RuntimeException {
                 // TODO Auto-generated method stub
                 updateMyDataListPanel();
                 /*  JOptionPane.showMessageDialog(component, "Apply All operation completed successfully", "Information",
                  JOptionPane.INFORMATION_MESSAGE);*/
                 SearchNavigationPanel.messageLabel.setText("Apply All operation completed successfully");
             }
         };
         swingWorker.start();
     }
 
     /**
      * Method to fetch results for apply. This method spawns threads to execute each query seperately
      */
     public void fetchApplyAllResults(Collection<Callable<QueryResultObject>> queryCallables,
                                      QueryEngineBusinessInterface queryEngineBus) {
         do {
             ExecutorService executorService = Executors.newCachedThreadPool();
             try {
                 List<Future<QueryResultObject>> results = executorService.invokeAll(queryCallables);
                 queryCallables.clear();
                 for (Future<QueryResultObject> future : results) {
                     QueryResultObject queryResult = future.get();
                     if (queryResult != null) {
                         List<IDataRow> parentRows = queryResult.getResults();
                         List<IDataRow> childRows = queryResult.getChilds();
                         for (int i = 0; i < parentRows.size(); i++) {
                             MainSearchPanel.getDataList().addDataRow(parentRows.get(i));
                             for (int j = 0; j < childRows.size(); j++) {
                                 queryCallables.add(new QueryExecutionCallable(parentRows.get(i), childRows.get(j),
                                         queryEngineBus, childRows.get(j).getChildren()));
                             }
                         }
                     }
                 }
             } catch (InterruptedException e) {
                 Logger.out.warn("Unable to get results : " + e.getMessage());
                 break;
             } catch (ExecutionException e) {
                 Logger.out.warn("Unable to get results : " + e.getMessage());
                 break;
             }
         } while (0 < queryCallables.size());
     }
 
     /**
      * Method to Initialize Data list summary panel 
      *
      */
     public void initDataListSummaryPanel() {
         Logger.out.debug("In initDataListSummaryPanel method");
         if (myDataListTitledPanel == null) {
 
             // TODO externalize these titles.
             myDataListTitledPanel = new Cab2bTitledPanel("My Data List Summary");
             GradientPaint gp1 = new GradientPaint(new Point2D.Double(.05d, 0), new Color(185, 211, 238),
                     new Point2D.Double(.95d, 0), Color.WHITE);
             myDataListTitledPanel.setTitlePainter(new BasicGradientPainter(gp1));
             myDataListTitledPanel.setTitleFont(new Font("SansSerif", Font.BOLD, 11));
             myDataListTitledPanel.setTitleForeground(Color.BLACK);
             myDataListTitledPanel.setBorder(null);
             myDataListTitledPanel.setPreferredSize(new Dimension(267,653));
 
             if (myDataListPanel == null) {
                 myDataListPanel = new Cab2bPanel();
                 myDataListPanel.setBorder(null);
                 myDataListPanel.setBackground(Color.WHITE);
                 myDataListPanel.setLayout(new RiverLayout(5, 10));
             } else {
                 myDataListPanel.removeAll();
             }
 
             myDataListTitledPanel.setContentContainer(myDataListPanel);
             //setting the scroll bar
             JScrollPane myDataListPane = new JScrollPane(myDataListPanel);
             myDataListPane.getViewport().setBackground(Color.WHITE);
             myDataListTitledPanel.add(myDataListPane);
 
         } else {
             if (myDataListPanel != null) {
                 myDataListPanel.removeAll();
             }
         }
 
         //create the parent panel
         if (myDataListParentPanel == null) {
             myDataListParentPanel = new Cab2bPanel();
             myDataListParentPanel.setBorder(null);
             myDataListParentPanel.add("br br br vfill hfill", myDataListTitledPanel);
         }
 
         updateMyDataListPanel();
     }
 
     /**
      * Updates My DataList Summary panel present on view Search result page 
      *
      */
     protected void updateMyDataListPanel() {
         //removing all previously added hyperlinks
         myDataListPanel.removeAll();
         IDataRow rootNode = MainSearchPanel.getDataList().getRootDataRow(); //datalistTree.get(0); // This node is hidden node in the tree view     
         for (int i = 0; i < rootNode.getChildren().size(); i++) {
             final IDataRow currentNode = rootNode.getChildren().get(i);
             if (currentNode.isData()) {
                 createHyperlink(currentNode);
             } else {
                 for (int k = 0; k < currentNode.getChildren().size(); k++) {
                     createHyperlink(currentNode.getChildren().get(k));
                 }
             }
         }
         myDataListPanel.add("br ", new Cab2bLabel("      "));
     }
 
     /**
      * Add hyperlinks for the datalist result into My Data List Summary panel      
      * @param row
      */
     private static void createHyperlink(final IDataRow row) {
         Cab2bHyperlink selectedRootClassName = new Cab2bHyperlink(true);
 
         DataRow dataRow = (DataRow) row;
         String displayClassName = Utility.getDisplayName(dataRow.getEntity());
         selectedRootClassName.setText(displayClassName + "_" + dataRow.getId());
         selectedRootClassName.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 GlobalNavigationPanel.mainSearchPanel.getNavigationPanel().gotoDataListPanel(row);
             }
         });
         myDataListPanel.add("br ", selectedRootClassName);
     }
 
     /**
      * @return Returns the incomingAssociationCollection.
      */
     public Collection<AssociationInterface> getIncomingAssociationCollection() {
         return incomingAssociationCollection;
     }
 
     /**
      * @param incomingAssociationCollection The incomingAssociationCollection to set.
      */
     public void setIncomingAssociationCollection(Collection<AssociationInterface> incomingAssociationCollection) {
         this.incomingAssociationCollection = incomingAssociationCollection;
     }
 
     /**
      * @return Returns the intraModelAssociationCollection.
      */
     public List<IInterModelAssociation> getIntraModelAssociationCollection() {
         return intraModelAssociationCollection;
     }
 
     /**
      * @param intraModelAssociationCollection The intraModelAssociationCollection to set.
      */
     public void setIntraModelAssociationCollection(List<IInterModelAssociation> intraModelAssociationCollection) {
         this.intraModelAssociationCollection = intraModelAssociationCollection;
     }
 
 }
