 package edu.wustl.cab2b.client.ui.experiment;
 
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.ANALYZE_DATA;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.BAR_GRAPH;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.FILTER_DATA;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.LINE_GRAPH;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.SCATTER_GRAPH;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.TREE_CLOSE_FOLDER;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.TREE_OPEN_FOLDER;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.VISUALIZE_DATA;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import org.jdesktop.swingx.JXPanel;
 import org.jdesktop.swingx.JXTree;
 
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.TaggedValueInterface;
 import edu.wustl.cab2b.client.cache.ClientSideCache;
 import edu.wustl.cab2b.client.ui.WindowUtilities;
 import edu.wustl.cab2b.client.ui.charts.Cab2bChartPanel;
 import edu.wustl.cab2b.client.ui.charts.ChartType;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bFormattedTextField;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.StackedBox;
 import edu.wustl.cab2b.client.ui.filter.CaB2BFilterInterface;
 import edu.wustl.cab2b.client.ui.main.IComponent;
 import edu.wustl.cab2b.client.ui.main.ParseXMLFile;
 import edu.wustl.cab2b.client.ui.main.SwingUIManager;
 import edu.wustl.cab2b.client.ui.mainframe.MainFrame;
 import edu.wustl.cab2b.client.ui.mainframe.NewWelcomePanel;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.client.ui.util.CustomSwingWorker;
 import edu.wustl.cab2b.client.ui.util.UserObjectWrapper;
 import edu.wustl.cab2b.client.ui.viewresults.DefaultSpreadSheetViewPanel;
 import edu.wustl.cab2b.common.IdName;
 import edu.wustl.cab2b.common.analyticalservice.ServiceDetailsInterface;
 import edu.wustl.cab2b.common.datalist.DataListBusinessInterface;
 import edu.wustl.cab2b.common.datalist.DataListHomeInterface;
 import edu.wustl.cab2b.common.domain.DataListMetadata;
 import edu.wustl.cab2b.common.domain.Experiment;
 import edu.wustl.cab2b.common.ejb.EjbNamesConstants;
 import edu.wustl.cab2b.common.ejb.analyticalservice.AnalyticalServiceOperationsBusinessInterface;
 import edu.wustl.cab2b.common.ejb.analyticalservice.AnalyticalServiceOperationsHomeInterface;
 import edu.wustl.cab2b.common.exception.CheckedException;
 import edu.wustl.cab2b.common.exception.RuntimeException;
 import edu.wustl.cab2b.common.queryengine.result.IRecord;
 import edu.wustl.cab2b.common.queryengine.result.QueryResultFactory;
 import edu.wustl.cab2b.common.queryengine.result.RecordId;
 import edu.wustl.cab2b.common.util.AttributeInterfaceComparator;
 import edu.wustl.cab2b.common.util.Constants;
 import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.common.util.logger.Logger;
 
 public class ExperimentStackBox extends Cab2bPanel {
     /** Default Serial version ID */
     private static final long serialVersionUID = 1L;
 
     /** panel to display filters on selected data-category */
     private static Cab2bPanel dataFilterPanel = null;
 
     /** panel to display analysed data on selected data-category */
     private Cab2bPanel analyseDataPanel = null;
 
     /** panel to display visual form of data on selected data-category */
     private Cab2bPanel visualiseDataPanel = null;
 
     /** stack box to add all this panels */
     private StackedBox stackedBox;
 
     /** ExperimentOpen Panel */
     private ExperimentDataCategoryGridPanel m_experimentDataCategoryGridPanel = null;
 
     private JXTree datalistTree;
 
     DefaultMutableTreeNode rootNode;
 
     final static int CATEGORY_NODE_NO = 0;
 
     final static int CUSTOM_CATEGORY_NODE_NO = 1;
 
     private JScrollPane treeViewScrollPane;
 
     private Experiment m_selectedExperiment = null;
 
     private static CaB2BFilterInterface obj = null;
 
     private static int chartIndex = 0;
 
     public ExperimentStackBox(Experiment selectedExperiment) {
         m_selectedExperiment = selectedExperiment;
         initGUI();
     }
 
     public ExperimentStackBox(
             Experiment selectedExperiment,
             ExperimentDataCategoryGridPanel experimentDataCategoryGridPanel) {
         m_selectedExperiment = selectedExperiment;
         m_experimentDataCategoryGridPanel = experimentDataCategoryGridPanel;
         initGUI();
     }
 
     public void initGUI() {
         this.setLayout(new BorderLayout());
         stackedBox = new StackedBox();
         stackedBox.setTitleBackgroundColor(new Color(200, 200, 220));
 
         // Set<Set<EntityInterface>> entitySet = null;
         // try {
         // entitySet =
         // m_experimentBusinessInterface.getDataListEntitySet(m_selectedExperiment);
         // } catch (RemoteException remoteException) {
         // CommonUtils.handleException(remoteException,
         // MainFrame.newWelcomePanel, true, true, true, false);
         // }
 
         DefaultMutableTreeNode rootNode = generateRootNode(m_selectedExperiment);
         initializeDataListTree(rootNode);
 
         initializePanels();
     }
 
     /**
      * This method generates a root node given the set of entities
      * 
      * @param experiment
      *            set of entities
      * @return root node
      */
     private DefaultMutableTreeNode generateRootNode(Experiment experiment) {
         rootNode = new DefaultMutableTreeNode("Make it invisible");
         DefaultMutableTreeNode CategoriesRoot = new DefaultMutableTreeNode("Experiment Data Categories");
         DefaultMutableTreeNode customCategoriesRoot = null;
 
         // for each datalist
         for (DataListMetadata dataListMetadata : experiment.getDataListMetadataCollection()) {
 
             if (dataListMetadata.isCustomDataCategory()) {
                 if (customCategoriesRoot == null) {
                     customCategoriesRoot = new DefaultMutableTreeNode("Custom Data Categories");
                 }
                 DefaultMutableTreeNode customCategoryNode = getNodeForCustomCategory(dataListMetadata);
                 customCategoriesRoot.add(customCategoryNode);
                 continue;
             }
 
             ClientSideCache clientCache = ClientSideCache.getInstance();
 
             for (IdName idName : dataListMetadata.getEntitiesNames()) {
 
                 EntityInterface originalEntity = clientCache.getEntityById(idName.getOriginalEntityId());
 
                 UserObjectWrapper<IdName> categoryUserObject = new UserObjectWrapper<IdName>(idName,
                         Utility.getDisplayName(originalEntity));
 
                 DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(categoryUserObject);
                 CategoriesRoot.add(categoryNode);
             }
         }
 
         rootNode.add(CategoriesRoot);
         if (customCategoriesRoot != null) {
             rootNode.add(customCategoriesRoot);
         }
         return rootNode;
     }
 
     /**
      * This method initalize the datalist tree given the root node
      * 
      * @param rootNode
      *            the root node to be set.
      */
     private void initializeDataListTree(DefaultMutableTreeNode rootNode) {
         // creating datalist tree
         datalistTree = new JXTree(rootNode);
         datalistTree.setRootVisible(false);
         datalistTree.expandAll();
 
         // setting the first node as selected and displaying the corresponding
         // records in the table
         if (datalistTree.getRowCount() >= 1) {
             datalistTree.setSelectionRow(1);
             Object nodeInfo = ((DefaultMutableTreeNode) datalistTree.getLastSelectedPathComponent()).getUserObject();
             if (nodeInfo instanceof UserObjectWrapper) {
                 updateSpreadSheet((UserObjectWrapper<IdName>) nodeInfo);
             }
         }
         // action listener for selected data category
         datalistTree.addTreeSelectionListener(new TreeSelectionListener() {
             public void valueChanged(TreeSelectionEvent e) {
                 treeSelectionListenerAction();
             }
         });
         ClassLoader loader = this.getClass().getClassLoader();
         datalistTree.setOpenIcon(new ImageIcon(loader.getResource(TREE_OPEN_FOLDER)));
         datalistTree.setClosedIcon(new ImageIcon(loader.getResource(TREE_CLOSE_FOLDER)));
        datalistTree.setLeafIcon(new ImageIcon(loader.getResource("mydatalist_icon.gif")));
         datalistTree.setBorder(null);
     }
 
     /**
      * This method initialzse the different UI panels and panes.
      */
     private void initializePanels() {
         // Adding Select data category pane
         treeViewScrollPane = new JScrollPane(datalistTree);
         treeViewScrollPane.setPreferredSize(new Dimension(250, 200));
         treeViewScrollPane.setBorder(null);
        stackedBox.addBox("Select Data Category", treeViewScrollPane, "mysearchqueries_icon.gif", false);
 
         // Adding Filter data category panel
         dataFilterPanel = new Cab2bPanel();
         dataFilterPanel.setPreferredSize(new Dimension(250, 200));
         dataFilterPanel.setOpaque(false);
         dataFilterPanel.setBorder(null);
         stackedBox.addBox("Filter Data ", dataFilterPanel, FILTER_DATA, true);
 
         // Adding Analyse data panel
         analyseDataPanel = new Cab2bPanel();
         analyseDataPanel.setPreferredSize(new Dimension(250, 150));
         analyseDataPanel.setOpaque(false);
         analyseDataPanel.setBorder(null);
         stackedBox.addBox("Analyze Data ", analyseDataPanel, ANALYZE_DATA, true);
 
         // Adding Visualize data panel
         visualiseDataPanel = new Cab2bPanel();
         visualiseDataPanel.setPreferredSize(new Dimension(250, 200));
         visualiseDataPanel.setOpaque(false);
         visualiseDataPanel.setBorder(null);
         stackedBox.addBox("Visualize Data ", visualiseDataPanel, VISUALIZE_DATA, true);
 
         // Set the type of charts to be displayed.
         Vector<ChartType> chartTypes = new Vector<ChartType>();
         chartTypes.add(ChartType.BAR_CHART);
         chartTypes.add(ChartType.LINE_CHART);
         chartTypes.add(ChartType.SCATTER_PLOT);
         setChartTypesForVisualiseDataPanel(chartTypes);
 
         stackedBox.setPreferredSize(new Dimension(250, 500));
         stackedBox.setMinimumSize(new Dimension(250, 500));
         stackedBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 220)));
         this.add(stackedBox);
         updateUI();
     }
 
     public Cab2bPanel getDataFilterPanel() {
         return dataFilterPanel;
     }
 
     /**
      * find and add children of the given tree node child node is added to the
      * current node if it is filtered, otherwise it is added to the root node
      * 
      * @param node
      *            the tree node
      */
     private void addChildren(DefaultMutableTreeNode rootNode, DefaultMutableTreeNode node) {
         EntityInterface entity = ((TreeEntityWrapper) node.getUserObject()).getEntityInterface();
 
         for (AssociationInterface association : entity.getAssociationCollection()) {
             EntityInterface targetEntity = association.getTargetEntity();
             TreeEntityWrapper child = new TreeEntityWrapper();
             child.setEntityInterface(targetEntity);
             DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
 
             Collection<TaggedValueInterface> taggedValues = targetEntity.getTaggedValueCollection();
 
             if (Utility.getTaggedValue(taggedValues, Constants.FILTERED) != null) {
                 node.add(childNode);
             } else {
                 rootNode.add(childNode);
             }
             addChildren(rootNode, childNode);
         }
     }
 
     /**
      * Method to perform tree node selection action for currently selected node
      */
     public void treeSelectionListenerAction() {
 
         Cab2bPanel selectedPanel = (Cab2bPanel) m_experimentDataCategoryGridPanel.getTabComponent().getSelectedComponent();
         Component applyFilterComponent = CommonUtils.getComponentByName(selectedPanel,
                                                                         Constants.APPLY_FILTER_PANEL_NAME);
         if (applyFilterComponent instanceof ApplyFilterPanel) {
             ApplyFilterPanel applyFilterPanel = (ApplyFilterPanel) applyFilterComponent;
             applyFilterPanel.clearMap();
             getDataForFilterPanel(applyFilterPanel);
         }
         updateUI();
 
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) datalistTree.getLastSelectedPathComponent();
         if (node == null) {
             return;
         }
 
         // if rot node is selected, clear all table content
         // if (node.isRoot() == true) {
         // columnName = null;
         // recordObject = null;
         // }
 
         Object nodeInfo = node.getUserObject();
         if (nodeInfo instanceof UserObjectWrapper) {
             updateSpreadSheet((UserObjectWrapper<IdName>) nodeInfo);
         }
     }
 
     private void updateSpreadSheet(final UserObjectWrapper<IdName> idName) {
 
         CustomSwingWorker swingWorker = new CustomSwingWorker(datalistTree) {
             List<IRecord> recordList = null;
 
             protected void doNonUILogic() throws RuntimeException {
                 try {
                     // getting datalist entity interface
                     DataListBusinessInterface dataListBI = (DataListBusinessInterface) CommonUtils.getBusinessInterface(
                                                                                                                         EjbNamesConstants.DATALIST_BEAN,
                                                                                                                         DataListHomeInterface.class);
 
                     recordList = dataListBI.getEntityRecord(idName.getUserObject().getId());
 
                 } catch (RemoteException remoteException) {
                     CommonUtils.handleException(remoteException, MainFrame.newWelcomePanel, true, true, true,
                                                 false);
                 }
                 addAvailableAnalysisServices(idName.getUserObject());
             }
 
             protected void doUIUpdateLogic() throws RuntimeException {
                 m_experimentDataCategoryGridPanel.refreshTable(recordList);
             }
         };
         swingWorker.start();
     }
 
     public static void setDataForFilterPanel(Vector<CaB2BFilterInterface> data) {
         Logger.out.debug("setDataForMyExperimentsPanel :: data " + data);
         dataFilterPanel.removeAll();
         dataFilterPanel.add(new Cab2bLabel());
         for (CaB2BFilterInterface caB2BFilterInterface : data) {
             obj = caB2BFilterInterface;
             String labelName = obj.toString();
 
             if ((labelName.indexOf("like **") < 0) && (labelName.indexOf("like *.*") < 0)) {
                 Cab2bLabel cab2bLabel = new Cab2bLabel(labelName);
                 dataFilterPanel.add("br", cab2bLabel);
             }
         }
         dataFilterPanel.revalidate();
         dataFilterPanel.updateUI();
     }
 
     public static void getDataForFilterPanel(ApplyFilterPanel applyFilterPanel) {
         Vector<CaB2BFilterInterface> vector = applyFilterPanel.getFilterMap();
         if (vector != null) {
             setDataForFilterPanel(vector);
         }
     }
 
     /**
      * This method displays the type of chart in the Visualize Data panel which
      * is at the left bottom of the screen.
      * 
      * @param chartTypes
      *            name of charts to be displayed.
      */
     private void setChartTypesForVisualiseDataPanel(Vector<ChartType> chartTypes) {
         Logger.out.debug("setChartTypesForVisualiseDataPanel :: chartTypes " + chartTypes);
         visualiseDataPanel.removeAll();
 
         for (ChartType chartType : chartTypes) {
             Cab2bHyperlink hyperlink = new Cab2bHyperlink();
             hyperlink.setBounds(new Rectangle(5, 5, 5, 5));
             hyperlink.setText(chartType.getActionCommand());
             hyperlink.setActionCommand(chartType.getActionCommand());
 
             URL url = null;
             switch (chartType) {
                 case BAR_CHART:
                     url = this.getClass().getClassLoader().getResource(BAR_GRAPH);
                     break;
 
                 case LINE_CHART:
                     url = this.getClass().getClassLoader().getResource(LINE_GRAPH);
                     break;
 
                 case SCATTER_PLOT:
                     url = this.getClass().getClassLoader().getResource(SCATTER_GRAPH);
             }
             if (url != null) {
                 hyperlink.setIcon(new ImageIcon(url));
             }
 
             hyperlink.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent actionEvent) {
                     String actionCommand = actionEvent.getActionCommand();
                     ChartType chartType = ChartType.getChartType(actionCommand);
                     showChartAction(chartType);
                     updateUI();
                 }
             });
             hyperlink.setEnabled(false);
             visualiseDataPanel.add("br", hyperlink);
         }
         visualiseDataPanel.revalidate();
     }
 
     /**
      * This method displays the chart selected in the Chart tab.
      * 
      * @param linkClicked
      *            the name of the chart to be displayed
      */
     private void showChartAction(ChartType chartType) {
         String entityName = null;
         Object nodeInfo = ((DefaultMutableTreeNode) datalistTree.getLastSelectedPathComponent()).getUserObject();
         if (nodeInfo instanceof TreeEntityWrapper) {
             entityName = ((TreeEntityWrapper) nodeInfo).toString();
         }
 
         // Cab2bChartPanel cab2bChartPanel = null;
         final JTabbedPane tabComponent = m_experimentDataCategoryGridPanel.getTabComponent();
         Cab2bPanel currentChartPanel = m_experimentDataCategoryGridPanel.getCurrentChartPanel();
         if (currentChartPanel == null) {
             final Cab2bPanel newVisualizeDataPanel = new Cab2bPanel();
             newVisualizeDataPanel.setBorder(null);
 
             Cab2bButton closeButton = new Cab2bButton("Close");
             closeButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent actionEvent) {
                     tabComponent.remove(newVisualizeDataPanel);
                     chartIndex--;
                 }
             });
             newVisualizeDataPanel.add("right ", closeButton);
 
             DefaultSpreadSheetViewPanel defaultSpreadSheetViewPanel = m_experimentDataCategoryGridPanel.getCurrentSpreadSheetViewPanel();
             Cab2bChartPanel cab2bChartPanel = new Cab2bChartPanel(defaultSpreadSheetViewPanel.getDataTable());
             cab2bChartPanel.setChartType(chartType, entityName);
             newVisualizeDataPanel.add("br hfill vfill ", cab2bChartPanel);
             m_experimentDataCategoryGridPanel.setCurrentChartPanel(newVisualizeDataPanel);
 
             tabComponent.add("Chart" + ++chartIndex, newVisualizeDataPanel);
             tabComponent.setSelectedComponent(newVisualizeDataPanel);
         } else {
             Cab2bChartPanel cab2bChartPanel = (Cab2bChartPanel) CommonUtils.getComponentByName(currentChartPanel,
                                                                                                "cab2bChartPanel");
             cab2bChartPanel.setChartType(chartType, entityName);
             tabComponent.setSelectedComponent(currentChartPanel);
         }
     }
 
     /**
      * update the tree, add the newly created entity to the current selection
      * 
      * @param dataListMetadata
      */
     public void updateStackBox(DataListMetadata dataListMetadata) {
         DefaultMutableTreeNode customCategoriesRoot = null;
         if (rootNode.getChildCount() == 1) {
             customCategoriesRoot = new DefaultMutableTreeNode("Custom Data Categories");
             rootNode.add(customCategoriesRoot);
         } else {
             customCategoriesRoot = (DefaultMutableTreeNode) rootNode.getChildAt(CUSTOM_CATEGORY_NODE_NO);
         }
 
         DefaultMutableTreeNode customCategoryNode = getNodeForCustomCategory(dataListMetadata);
         customCategoriesRoot.add(customCategoryNode);
         datalistTree.updateUI();
     }
 
     DefaultMutableTreeNode getNodeForCustomCategory(DataListMetadata dataListMetadata) {
         // Currently since custom data catrgories will have one and only one
         // entity, so hard coding
         IdName idName = dataListMetadata.getEntitiesNames().iterator().next();
 
         UserObjectWrapper<IdName> customCategoryUserObject = new UserObjectWrapper<IdName>(idName,
                 dataListMetadata.getName());
         return new DefaultMutableTreeNode(customCategoryUserObject);
 
     }
 
     /**
      * This method adds all the available services as link in analysis panel
      * 
      * @param experimentEntity
      */
     private void addAvailableAnalysisServices(IdName idName) {
         // final EntityInterface dataEntity = idName.getEntityInterface();
         ClientSideCache clientSideCache = ClientSideCache.getInstance();
         EntityInterface dataEntity = clientSideCache.getEntityById(idName.getOriginalEntityId());
         AnalyticalServiceOperationsBusinessInterface analyticalServiceOperationsBusinessInterface = (AnalyticalServiceOperationsBusinessInterface) CommonUtils.getBusinessInterface(
                                                                                                                                                                                     EjbNamesConstants.ANALYTICAL_SERVICE_BEAN,
                                                                                                                                                                                     AnalyticalServiceOperationsHomeInterface.class,
                                                                                                                                                                                     null);
         List<ServiceDetailsInterface> serviceDetailInterfaceList = null;
         try {
             serviceDetailInterfaceList = analyticalServiceOperationsBusinessInterface.getApplicableAnalyticalServices(idName.getOriginalEntityId());
             if (analyseDataPanel != null) {
                 analyseDataPanel.removeAll();
                 for (ServiceDetailsInterface serviceDetails : serviceDetailInterfaceList) {
                     addHyperLinkToAnalyticalPane(serviceDetails, dataEntity);
                 }
             }
         } catch (RemoteException remoteException) {
             CommonUtils.handleException(remoteException, MainFrame.newWelcomePanel, true, true, true, false);
         }
     }
 
     /**
      * This method adds a hyperlink to the Analytical Panel for the given
      * service
      * 
      * @param serviceDetails
      * @param dataEntity
      */
     private void addHyperLinkToAnalyticalPane(ServiceDetailsInterface serviceDetails,
                                               final EntityInterface dataEntity) {
         String serviceName = serviceDetails.getDisplayName();
 
         Cab2bHyperlink<ServiceDetailsInterface> hyperlink = new Cab2bHyperlink<ServiceDetailsInterface>();
         hyperlink.setBounds(new Rectangle(5, 5, 5, 5));
         hyperlink.setText(serviceName);
         hyperlink.setActionCommand(serviceName);
         hyperlink.setUserObject(serviceDetails);
 
         hyperlink.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent actionEvent) {
                 Cab2bHyperlink<ServiceDetailsInterface> hyperlink = (Cab2bHyperlink<ServiceDetailsInterface>) (actionEvent.getSource());
 
                 ServiceDetailsInterface serviceDetails = hyperlink.getUserObject();
                 List<EntityInterface> requiredEntityList = serviceDetails.getRequiredEntities();
                 for (EntityInterface requiredEntity : requiredEntityList) {
                     // TODO compare with entity id instead of name
                     // if(entity.getId() != requiredEntity.getId() &&
                     // !requiredEntity.getAbstractAttributeCollection().isEmpty())
                     // {
                     if (!dataEntity.getName().equals(requiredEntity.getName())
                             && !requiredEntity.getAbstractAttributeCollection().isEmpty()) {
                         serviceSelectAction(serviceDetails, requiredEntity, dataEntity);
                     }
                 }
             }
         });
         analyseDataPanel.add("br", hyperlink);
     }
 
     /**
      * The action listener for the individual service elements.
      * 
      * @param dataEntity
      * @param entityId
      * @param serviceDetailsInterface
      * @param actionEvent
      *            The event that contains details of the click on the individual
      *            service.
      */
     private void serviceSelectAction(ServiceDetailsInterface serviceDetails, final EntityInterface requiredEntity,
                                      final EntityInterface dataEntity) {
         // Create panel for Analysis Title
         JComponent tilteLabel = new Cab2bLabel("Analysis Title" + " : ");
         JComponent titleField = new Cab2bFormattedTextField(20, Cab2bFormattedTextField.PLAIN_FIELD);
         Cab2bPanel titlePanel = new Cab2bPanel();
         titlePanel.setName("titlePanel");
         titlePanel.add("br", tilteLabel);
         titlePanel.add("tab", titleField);
 
         // Create panels for service parameters
         Cab2bPanel parameterPanel = getParameterPanels(requiredEntity);
         JScrollPane jScrollPane = new JScrollPane();
         jScrollPane.setName("jScrollPane");
         jScrollPane.setBorder(BorderFactory.createLineBorder(Color.black, 1));
         jScrollPane.getViewport().add(parameterPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                       JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         jScrollPane.getViewport().setBackground(Color.WHITE);
 
         // Create finish button
         Cab2bButton finishButton = new Cab2bButton("Finish");
         finishButton.addActionListener(new FinishButtonActionListner(serviceDetails, dataEntity, requiredEntity,
                 m_experimentDataCategoryGridPanel));
 
         Cab2bPanel servicePanel = new Cab2bPanel();
         servicePanel.add("br left ", titlePanel);
         servicePanel.add("br center hfill vfill ", jScrollPane);
         servicePanel.add("br right ", finishButton);
 
         String displayName = CommonUtils.getFormattedString(requiredEntity.getName());
         WindowUtilities.showInDialog(NewWelcomePanel.mainFrame, servicePanel, displayName,
                                      Constants.WIZARD_SIZE2_DIMENSION, true, false);
     }
 
     /**
      * This method returns the parameter panel that has the controls to accept
      * the parameter values.
      * 
      * @param entity
      *            the entity for which the control panels are to be displayed
      * @return Array of control panel
      */
     private Cab2bPanel getParameterPanels(final EntityInterface entity) {
         ParseXMLFile parseFile = null;
         try {
             parseFile = ParseXMLFile.getInstance();
         } catch (CheckedException checkedException) {
             CommonUtils.handleException(checkedException, this, true, true, false, false);
         }
 
         final Collection<AttributeInterface> attributeCollection = entity.getAttributeCollection();
         Cab2bPanel parameterPanel = new Cab2bPanel();
         parameterPanel.setName("parameterPanel");
         if (attributeCollection != null) {
             List<AttributeInterface> attributeList = new ArrayList<AttributeInterface>(attributeCollection);
             Collections.sort(attributeList, new AttributeInterfaceComparator());
 
             try {
 
                 Dimension maxLabelDimension = CommonUtils.getMaximumLabelDimension(attributeList);
                 for (AttributeInterface attribute : attributeList) {
                     JXPanel jxPanel = (JXPanel) SwingUIManager.generateUIPanel(parseFile, attribute, false,
                                                                                maxLabelDimension);
                     parameterPanel.add("br", jxPanel);
                 }
             } catch (CheckedException checkedException) {
                 CommonUtils.handleException(checkedException, this, true, true, false, false);
             }
         }
 
         return parameterPanel;
     }
 
     /**
      * @return the visualiseDataPanel
      */
     public Cab2bPanel getVisualiseDataPanel() {
         return visualiseDataPanel;
     }
 
     /**
      * @param visualiseDataPanel
      *            the visualiseDataPanel to set
      */
     public void setVisualiseDataPanel(Cab2bPanel visualiseDataPanel) {
         this.visualiseDataPanel = visualiseDataPanel;
     }
 }
 
 /**
  * This Listener class adds a row in the table on the Analysis tab.
  * 
  * @author chetan_patil
  */
 class FinishButtonActionListner implements ActionListener {
     private ServiceDetailsInterface serviceDetails;
 
     private EntityInterface dataEntity;
 
     private EntityInterface requiredEntity;
 
     private ExperimentDataCategoryGridPanel experimentDataCategoryGridPanel;
 
     /**
      * Parameterized constructor
      * 
      * @param serviceDetails
      * @param dataEntity
      * @param experimentDataCategoryGridPanel
      */
     public FinishButtonActionListner(
             ServiceDetailsInterface serviceDetails,
             EntityInterface dataEntity,
             EntityInterface requiredEntity,
             ExperimentDataCategoryGridPanel experimentDataCategoryGridPanel) {
         super();
         this.serviceDetails = serviceDetails;
         this.dataEntity = dataEntity;
         this.requiredEntity = requiredEntity;
         this.experimentDataCategoryGridPanel = experimentDataCategoryGridPanel;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
      */
     public void actionPerformed(ActionEvent actionEvent) {
         MainFrame.setStatus(MainFrame.Status.BUSY);
         MainFrame.setStatusMessage("Analysing the data...");
 
         Cab2bButton finishButton = (Cab2bButton) actionEvent.getSource();
         Cab2bPanel servicePanel = (Cab2bPanel) finishButton.getParent();
         closeDialogWindow(servicePanel);
 
         // Obtain the analysis title
         Cab2bPanel titlePanel = (Cab2bPanel) CommonUtils.getComponentByName(servicePanel, "titlePanel");
         Cab2bFormattedTextField titleField = (Cab2bFormattedTextField) titlePanel.getComponents()[1];
         String analysisTitle = titleField.getText();
 
         // Obtain values of service parameters
         JScrollPane jScrollPane = (JScrollPane) CommonUtils.getComponentByName(servicePanel, "jScrollPane");
         Cab2bPanel parameterPanel = (Cab2bPanel) CommonUtils.getComponentByName(jScrollPane.getViewport(),
                                                                                 "parameterPanel");
         List<IRecord> serviceParameterList = collectServiceParameterValues(parameterPanel.getComponents());
 
         // Obtain table data
         DefaultSpreadSheetViewPanel defaultSpreadSheetViewPanel = experimentDataCategoryGridPanel.getCurrentSpreadSheetViewPanel();
         List<IRecord> recordList = defaultSpreadSheetViewPanel.getSelectedRecords();
 
         List<IRecord> entityRecordList = null;
         AnalyticalServiceOperationsBusinessInterface analyticalServiceOperationsBusinessInterface = (AnalyticalServiceOperationsBusinessInterface) CommonUtils.getBusinessInterface(
                                                                                                                                                                                     EjbNamesConstants.ANALYTICAL_SERVICE_BEAN,
                                                                                                                                                                                     AnalyticalServiceOperationsHomeInterface.class,
                                                                                                                                                                                     null);
         try {
             entityRecordList = analyticalServiceOperationsBusinessInterface.invokeService(serviceDetails,
                                                                                           recordList,
                                                                                           serviceParameterList);
             updateAnalysisTable(analysisTitle, entityRecordList);
         } catch (RemoteException remoteException) {
             CommonUtils.handleException(remoteException, MainFrame.newWelcomePanel, true, true, true, false);
         }
 
         MainFrame.setStatus(MainFrame.Status.READY);
         MainFrame.setStatusMessage("Analysis finished");
     }
 
     /**
      * This method refreshes the table of analysis in the Analysis tab. It
      * displays the values of entityRecordResult in the tablular form.
      * 
      * @param analysisTitle
      *            the analysis title to display as link value in the table
      * @param entityRecordResult
      *            the EntityRecordResult whose values are to be displayed in a
      *            single row of table.
      */
     private void updateAnalysisTable(final String analysisTitle, final List<IRecord> entityRecordList) {
         UserObjectWrapper<List<IRecord>> userObjectWrapper = new UserObjectWrapper<List<IRecord>>(
                 entityRecordList, analysisTitle);
         String entityName = Utility.getDisplayName(dataEntity);
         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
         String currentDate = simpleDateFormat.format(new Date());
         String progress = "Completed";
 
         Object[][] analysisTableData = new Object[][] { { entityName, userObjectWrapper, currentDate, progress } };
         experimentDataCategoryGridPanel.refreshAnalysisTable(analysisTableData);
     }
 
     /**
      * This method closes the currnet dialog window
      * 
      * @param servicePanel
      */
     private void closeDialogWindow(final Cab2bPanel servicePanel) {
         Container container = servicePanel.getParent();
         container = container.getParent();
         container = container.getParent();
         container = container.getParent();
         if (container instanceof JDialog) {
             JDialog jDialog = (JDialog) container;
             jDialog.dispose();
         }
     }
 
     /**
      * This method collects the values from the service popup window
      * 
      * @param controlPanels
      *            parameter paneles of the service pop window
      * @return Record which has the parameter values
      */
     private List<IRecord> collectServiceParameterValues(final Component[] controlPanels) {
         List<AttributeInterface> attributeList = new ArrayList<AttributeInterface>(
                 requiredEntity.getAttributeCollection());
         Collections.sort(attributeList, new AttributeInterfaceComparator());
 
         IRecord record = QueryResultFactory.createRecord(new HashSet<AttributeInterface>(attributeList),
                                                          new RecordId("-1", ""));
 
         int index = 0;
         for (AttributeInterface attribute : attributeList) {
             String value = null;
             ArrayList<String> parameterValues = ((IComponent) controlPanels[index++]).getValues();
             if (!parameterValues.isEmpty()) {
                 value = parameterValues.get(0);
             }
             record.putValueForAttribute(attribute, value);
         }
 
         List<IRecord> parameterList = new Vector<IRecord>();
         parameterList.add(record);
 
         return parameterList;
     }
 
 }
