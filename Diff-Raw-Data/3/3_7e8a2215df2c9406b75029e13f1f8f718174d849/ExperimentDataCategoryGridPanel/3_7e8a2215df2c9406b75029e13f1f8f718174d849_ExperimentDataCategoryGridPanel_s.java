 /**
  * <p>Title: ExperimentDataCategoryGridPanel Class>
  * Copyright:    Copyright (c) year
  * Company: Washington University, School of Medicine, St. Louis.
  * @author Hrishikesh Rajpathak/Deepak Shingan/Chetan 
  * @version 1.4
  */
 
 package edu.wustl.cab2b.client.ui.experiment;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.table.TableColumn;
 
 import org.jdesktop.swingx.LinkRenderer;
 import org.jdesktop.swingx.action.LinkAction;
 
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bTable;
 import edu.wustl.cab2b.client.ui.mainframe.MainFrame;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.client.ui.util.CustomSwingWorker;
 import edu.wustl.cab2b.client.ui.util.UserObjectWrapper;
 import edu.wustl.cab2b.client.ui.viewresults.DataListDetailedPanelInterface;
 import edu.wustl.cab2b.client.ui.viewresults.DefaultDetailedPanel;
 import edu.wustl.cab2b.client.ui.viewresults.DefaultSpreadSheetViewPanel;
 import edu.wustl.cab2b.client.ui.viewresults.ThreeDResultObjectDetailsPanel;
 import edu.wustl.cab2b.client.ui.visualization.charts.Cab2bChartPanel;
 import edu.wustl.cab2b.common.datalist.DataList;
 import edu.wustl.cab2b.common.datalist.DataListBusinessInterface;
 import edu.wustl.cab2b.common.datalist.DataListHomeInterface;
 import edu.wustl.cab2b.common.datalist.DataRow;
 import edu.wustl.cab2b.common.datalist.IDataRow;
 import edu.wustl.cab2b.common.domain.DataListMetadata;
 import edu.wustl.cab2b.common.domain.Experiment;
 import edu.wustl.cab2b.common.ejb.EjbNamesConstants;
 import edu.wustl.cab2b.common.exception.CheckedException;
 import edu.wustl.cab2b.common.exception.RuntimeException;
 import edu.wustl.cab2b.common.experiment.ExperimentBusinessInterface;
 import edu.wustl.cab2b.common.experiment.ExperimentHome;
 import edu.wustl.cab2b.common.queryengine.result.IRecord;
 
 /**
  * This class displays the experiment table. Also provides filtering tool for
  * the class
  * 
  * @author hrishikesh_rajpathak
  * @author deepak_shingan
  * @author chetan_patil
  */
 public class ExperimentDataCategoryGridPanel extends Cab2bPanel {
     private static final long serialVersionUID = 1L;
 
     public ExperimentOpenPanel experimentPanel;
 
     private JTabbedPane tabComponent;
 
     /**
      * Panel to display experiment data when data category node is selected
      */
     private Cab2bPanel experimentDataPanel;
 
     /**
      * Panel to display analysis performed on experiment Second tab panel on tab
      * component
      */
     private Cab2bPanel analysisDataPanel;
 
     /**
      * Reference to the current tab which is displaying the chart diagram.
      */
     private Cab2bPanel currentChartPanel;
 
     /**
      * Reference to the current tab that is displaying the heat map.
      */
     private Cab2bPanel currentHeatMapPanel;
 
     /**
      * Table to display records on Analysis Data panels
      */
     private Cab2bTable analysisTable;
 
     final public String[] ANALYSIS_TABLE_HEADERS = new String[] { "Data Category", "Analysis Title", "Date", "Status" };
 
     public static ArrayList<String> values = new ArrayList<String>();
 
     // fields used by Save Data Category functionality
     // private EntityInterface dataCategoryEntity;
     private String dataCategoryTitle;
 
     private DefaultSpreadSheetViewPanel spreadSheetViewPanel;
 
     private DataListDetailedPanelInterface currentSpreadSheetViewPanel;
 
     public ExperimentDataCategoryGridPanel(ExperimentOpenPanel parent) {
         this.experimentPanel = parent;
         initGUI();
     }
 
     /**
      * @return the tabComponent
      */
     public JTabbedPane getTabComponent() {
         return tabComponent;
     }
 
     /**
      * @return the currentSpreadSheetViewPanel
      */
     public DataListDetailedPanelInterface getCurrentSpreadSheetViewPanel() {
         return currentSpreadSheetViewPanel;
     }
 
     /**
      * @param currentSpreadSheetViewPanel
      *            the currentSpreadSheetViewPanel to set
      */
     public void setCurrentSpreadSheetViewPanel(DataListDetailedPanelInterface currentSpreadSheetViewPanel) {
         this.currentSpreadSheetViewPanel = currentSpreadSheetViewPanel;
     }
 
     /**
      * @return the visualizeDataPanel
      */
     public Cab2bPanel getCurrentChartPanel() {
         return currentChartPanel;
     }
 
     /**
      * @param visualizeDataPanel2
      */
     public void setCurrentChartPanel(Cab2bPanel currentChartPanel) {
         this.currentChartPanel = currentChartPanel;
     }
 
     /**
      * @return the analysisDataPanel
      */
     public Cab2bPanel getAnalysisDataPanel() {
         return analysisDataPanel;
     }
 
     /**
      * @return the analysisTable
      */
     public Cab2bTable getAnalysisTable() {
         return analysisTable;
     }
 
     /**
      * Building/refreshing the table
      */
     public void refreshTable(List<IRecord> recordList) {
         this.removeAll();
         this.spreadSheetViewPanel.refreshView(recordList);
         refreshUI();
         updateUI();
     }
 
     /**
      * This method refreshes analysis table
      * 
      * @param analysisTitle
      * 
      * @param columns
      *            array of column names
      * @param dataRecords
      *            array of record values
      */
     public void refreshAnalysisTable(final Object[][] dataRecords) {
         Experiment selectedExperiment = ((ExperimentOpenPanel) this.getParent().getParent()).getSelectedExperiment();
         Cab2bLabel experimentLabel = new Cab2bLabel("Analysis performed for '" + selectedExperiment.getName()
                 + "'");
         Font textFont = new Font(experimentLabel.getFont().getName(), Font.BOLD,
                 experimentLabel.getFont().getSize() + 2);
         experimentLabel.setFont(textFont);
 
         analysisDataPanel.removeAll();
         analysisDataPanel.add("br ", experimentLabel);
 
         analysisTable = new Cab2bTable(false, dataRecords, ANALYSIS_TABLE_HEADERS);
         analysisTable.setRowSelectionAllowed(false);
         analysisTable.setColumnMargin(10);
 
         TableColumn tableColumn = null;
         tableColumn = analysisTable.getColumnModel().getColumn(2);
         tableColumn.setMinWidth(85);
         tableColumn.setMaxWidth(85);
 
         tableColumn = analysisTable.getColumnModel().getColumn(3);
         tableColumn.setMinWidth(75);
         tableColumn.setMaxWidth(75);
 
         JScrollPane jScrollPane = new JScrollPane();
         jScrollPane.getViewport().add(analysisTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                       JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         analysisDataPanel.add("br center hfill vfill", jScrollPane);
 
         if (CommonUtils.getComponentByName(tabComponent, "analysisDataPanel") == null) {
             //tabComponent.add("Analysis", analysisDataPanel);
             tabComponent.insertTab("Analysis", null, analysisDataPanel, null, 1);
         }
         tabComponent.setSelectedComponent(analysisDataPanel);

         TableLinkAction myLinkAction = new TableLinkAction();
         analysisTable.getColumn(1).setCellRenderer(new LinkRenderer(myLinkAction));
         analysisTable.getColumn(1).setCellEditor(new LinkRenderer(myLinkAction));
 
         updateUI();
     }
 
     /**
      * Initializing the GUI. Building the table initially.
      */
     public void initGUI() {
         this.setLayout(new BorderLayout());
 
         tabComponent = new JTabbedPane();
         tabComponent.setBorder(null);
         tabComponent.addChangeListener(new TabSelectListner(this));
 
         experimentDataPanel = new Cab2bPanel();
         experimentDataPanel.setName("experimentDataPanel");
         experimentDataPanel.setBorder(null);
 
         spreadSheetViewPanel = new DefaultSpreadSheetViewPanel(new ArrayList<IRecord>(), this);
         spreadSheetViewPanel.doInitialization();
         spreadSheetViewPanel.addPropertyChangeListener(new PropertyChangeListener() {
             public void propertyChange(PropertyChangeEvent evt) {
                 firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
             }
         });
 
         analysisDataPanel = new Cab2bPanel();
         analysisDataPanel.setName("analysisDataPanel");
         analysisDataPanel.setBorder(null);
         this.setBorder(null);
         refreshUI();
     }
 
     /**
      * This method is common among the different UI initialization methods
      */
     private void refreshUI() {
         experimentDataPanel.removeAll();
         experimentDataPanel.setBorder(null);
         experimentDataPanel.add("br center hfill vfill", spreadSheetViewPanel);
 
         //tabComponent.add("Experiment Data", experimentDataPanel);
         tabComponent.insertTab("Experiment Data", null, experimentDataPanel, null, 0);
         tabComponent.setBorder(null);
         this.add(tabComponent, BorderLayout.CENTER);
     }
 
     /**
      * This method adds a dynamic tab that displays the analyzed data.
      * 
      * @param userObjectWrapper
      *            the analyzed data
      */
     final private void addAnalysisViewTabPanel(UserObjectWrapper<List<IRecord>> userObjectWrapper) {
         String tabTitle = userObjectWrapper.getDisplayName();
         Cab2bPanel analysisView = (Cab2bPanel) CommonUtils.getComponentByName(tabComponent, tabTitle);
         if (analysisView == null) {
             final Cab2bPanel analysisViewPanel = new Cab2bPanel();
             analysisViewPanel.setName(tabTitle);
             analysisViewPanel.setBorder(null);
 
             // Add close button
             Cab2bButton closeButton = new Cab2bButton("Close");
             closeButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent actionEvent) {
                     tabComponent.remove(analysisViewPanel);
                 }
             });
             analysisViewPanel.add("right ", closeButton);
 
             // Add SpreadsheetView
             List<IRecord> recordList = userObjectWrapper.getUserObject();
             DefaultSpreadSheetViewPanel defaultSpreadSheetViewPanel = new DefaultSpreadSheetViewPanel(recordList,
                     this);
             defaultSpreadSheetViewPanel.addPropertyChangeListener(new PropertyChangeListener() {
                 public void propertyChange(PropertyChangeEvent evt) {
                     firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                 }
             });
             defaultSpreadSheetViewPanel.doInitialization();
             analysisViewPanel.add("br center hfill vfill", defaultSpreadSheetViewPanel);
             analysisView = analysisViewPanel;
             tabComponent.add(tabTitle, analysisViewPanel);
         }
         tabComponent.setSelectedComponent(analysisView);
     }
 
     public void addDetailTabPanel(String tabTitle, final DefaultDetailedPanel component) {
         Cab2bPanel detailPanel = (Cab2bPanel) CommonUtils.getComponentByName(tabComponent, tabTitle);
         if (detailPanel == null) {
             final Cab2bPanel detailViewPanel = new Cab2bPanel();
             detailViewPanel.setName(tabTitle);
             detailViewPanel.setBorder(null);
 
             // Add close button
             Cab2bButton closeButton = new Cab2bButton("Close");
             closeButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent actionEvent) {
                     tabComponent.remove(detailViewPanel);
                 }
             });
             detailViewPanel.add("right ", closeButton);
             detailViewPanel.add("br center hfill vfill", component);
             detailPanel = detailViewPanel;
             tabComponent.add(tabTitle, detailViewPanel);
         }
         tabComponent.setSelectedComponent(detailPanel);
     }
 
     /**
      * Make list of attributes of the experimentPanel entity as well as a 2D
      * array of data and pass it to the server to make new entity this method is
      * called by the {@link SaveDataCategoryPanel} to save a subset of of a
      * datalist as a category
      * 
      * @param title
      *            the title for the category
      */
     public void saveDataCategory(final String title) {
         dataCategoryTitle = title;
 
         MainFrame.setStatus(MainFrame.Status.BUSY);
         MainFrame.setStatusMessage("saving data category '" + title + "'");
 
         CustomSwingWorker swingWorker = new CustomSwingWorker(MainFrame.openExperimentWelcomePanel) {
             DataListMetadata dataListMetadata = null;
 
             protected void doNonUILogic() throws RuntimeException {
                 List<IRecord> selectedRecords = spreadSheetViewPanel.getAllVisibleRecords();
                 EntityInterface outputEntity = spreadSheetViewPanel.getOldRecordsEntity();
 
                 dataListMetadata = new DataListMetadata();
                 dataListMetadata.setName(title);
                 dataListMetadata.setCreatedOn(new Date());
                 dataListMetadata.setLastUpdatedOn(new Date());
                 dataListMetadata.setCustomDataCategory(true);
 
                 DataList customCategoryDataList = new DataList();
                 customCategoryDataList.setDataListAnnotation(dataListMetadata);
 
                 IDataRow titleNode = null;
                 for (IRecord selectedRecord : selectedRecords) {
                     IDataRow dataRow = new DataRow(selectedRecord, outputEntity);
                     if (titleNode == null) {
                         titleNode = dataRow.getTitleNode();
                     }
                     titleNode.addChild(dataRow);
                 }
 
                 customCategoryDataList.getRootDataRow().addChild(titleNode);
 
                 // make a call to the server
                 ExperimentBusinessInterface ExperimentBI = (ExperimentBusinessInterface) CommonUtils.getBusinessInterface(
                                                                                                                           EjbNamesConstants.EXPERIMENT,
                                                                                                                           ExperimentHome.class);
 
                 DataListBusinessInterface dataListBI = (DataListBusinessInterface) CommonUtils.getBusinessInterface(
                                                                                                                     EjbNamesConstants.DATALIST_BEAN,
                                                                                                                     DataListHomeInterface.class);
 
                 try {
                     dataListMetadata = dataListBI.saveDataCategory(customCategoryDataList.getRootDataRow(),
                                                                    dataListMetadata, null,
                                                                    spreadSheetViewPanel.getUserDefinedAttributes());
                     experimentPanel.getSelectedExperiment().addDataListMetadata(dataListMetadata);
                     ExperimentBI.addDataListToExperiment(experimentPanel.getSelectedExperiment().getId(),
                                                          dataListMetadata.getId());
                 } catch (RemoteException e) {
                     dataListMetadata = null;
                     CommonUtils.handleException(e, experimentPanel, true, true, true, false);
                 } catch (CheckedException e) {
                     dataListMetadata = null;
                     CommonUtils.handleException(e, experimentPanel, true, true, true, false);
                 }
             }
 
             protected void doUIUpdateLogic() throws RuntimeException {
                 if (dataListMetadata != null) {
                     // update the tree in the stack box
                     experimentPanel.addDataList(dataListMetadata);
                 }
                 MainFrame.setStatus(MainFrame.Status.READY);
                 MainFrame.setStatusMessage(dataCategoryTitle + " saved");
             }
         };
         swingWorker.start();
     }
 
     /**
      * Class used for adding hyperlinks in Jtable rows
      */
     class TableLinkAction extends LinkAction {
         private static final long serialVersionUID = 1L;
 
         public TableLinkAction() {
         }
 
         public void actionPerformed(ActionEvent actionEvent) {
             setVisited(true);
             MainFrame.setStatus(MainFrame.Status.READY);
             MainFrame.setStatusMessage("Displaying the analyzed data");
 
             // getting the selected hyperlink row
             int selectionIndex = analysisTable.getSelectionModel().getLeadSelectionIndex();
 
             // Get the EntityRecordResultInterface object associated with
             // hyperlink at column 1
             final UserObjectWrapper<List<IRecord>> userObjectWrapper = (UserObjectWrapper<List<IRecord>>) analysisTable.getValueAt(
                                                                                                                                    selectionIndex,
                                                                                                                                    1);
             addAnalysisViewTabPanel(userObjectWrapper);
         }
     }
 
     /**
      * This class sets the current table on the change of the tab.
      * 
      * @author chetan_patil
      */
     class TabSelectListner implements ChangeListener {
         ExperimentDataCategoryGridPanel experimentDataCategoryGridPanel;
 
         TabSelectListner(ExperimentDataCategoryGridPanel experimentDataCategoryGridPanel) {
             this.experimentDataCategoryGridPanel = experimentDataCategoryGridPanel;
         }
 
         public void stateChanged(ChangeEvent changeEvent) {
             JTabbedPane jTabbedPane = (JTabbedPane) changeEvent.getSource();
             Cab2bPanel selectedTabPanel = (Cab2bPanel) jTabbedPane.getSelectedComponent();
             if (selectedTabPanel != null && selectedTabPanel.getComponentCount() > 0) {
                 Component component = CommonUtils.getComponentByName(selectedTabPanel, "DataListDetailedPanel");
                 if (component instanceof DataListDetailedPanelInterface) {
                     DataListDetailedPanelInterface tabPanel = (DataListDetailedPanelInterface) component;
 
                     experimentDataCategoryGridPanel.setCurrentSpreadSheetViewPanel(tabPanel);
                     experimentDataCategoryGridPanel.setCurrentChartPanel(null);
                     List<String> columnNameList = tabPanel.getSelectedColumnNames();
                     if (columnNameList != null && columnNameList.size() > 0) {
                         firePropertyChange(DefaultSpreadSheetViewPanel.ENABLE_CHART_LINK, -1, 0);
                         firePropertyChange(DefaultSpreadSheetViewPanel.DISABLE_HEATMAP_LINK, -1, 0);
 
                         if (tabPanel instanceof ThreeDResultObjectDetailsPanel) {
                             if (((ThreeDResultObjectDetailsPanel) tabPanel).getIsWholeColumnSelected())
                                 firePropertyChange(DefaultSpreadSheetViewPanel.ENABLE_HEATMAP_LINK, -1, 0);
                         }
                     } else
                         firePropertyChange(DefaultSpreadSheetViewPanel.DISABLE_CHART_LINK, -1, 0);
                 } else {
                     component = CommonUtils.getComponentByName(selectedTabPanel, "cab2bChartPanel");
                     if (component != null && component instanceof Cab2bChartPanel) {
                         experimentDataCategoryGridPanel.setCurrentChartPanel(selectedTabPanel);
                         firePropertyChange(DefaultSpreadSheetViewPanel.ENABLE_CHART_LINK, -1, 0);
                         firePropertyChange(DefaultSpreadSheetViewPanel.DISABLE_HEATMAP_LINK, -1, 0);
                         firePropertyChange(DefaultSpreadSheetViewPanel.DISABLE_ANALYSIS_LINK, -1, 0);
                     }
                 }
             }
             updateUI();
         }
     }
 
     /**
      * @return the currentHeatMapPanel
      */
     public Cab2bPanel getCurrentHeatMapPanel() {
         return currentHeatMapPanel;
     }
 
     /**
      * @param currentHeatMapPanel the currentHeatMapPanel to set
      */
     public void setCurrentHeatMapPanel(Cab2bPanel currentHeatMapPanel) {
         this.currentHeatMapPanel = currentHeatMapPanel;
     }
 }
