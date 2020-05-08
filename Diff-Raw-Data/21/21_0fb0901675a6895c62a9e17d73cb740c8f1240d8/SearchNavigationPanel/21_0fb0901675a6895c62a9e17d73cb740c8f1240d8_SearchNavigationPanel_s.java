 package edu.wustl.cab2b.client.ui.searchDataWizard;
 
 import static edu.wustl.cab2b.client.ui.util.ApplicationResourceConstants.MYSETTINGS_FRAME_TITLE;
 import static edu.wustl.cab2b.client.ui.util.ClientConstants.DIALOG_CLOSE_EVENT;
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 
 import org.jdesktop.swingx.JXPanel;
 
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.wustl.cab2b.client.cache.UserCache;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.experiment.NewExperimentDetailsPanel;
 import edu.wustl.cab2b.client.ui.mainframe.MainFrame;
 import edu.wustl.cab2b.client.ui.mainframe.NewWelcomePanel;
 import edu.wustl.cab2b.client.ui.mainframe.stackbox.MainFrameStackedBoxPanel;
 import edu.wustl.cab2b.client.ui.mysettings.RightPanel;
 import edu.wustl.cab2b.client.ui.parameterizedQuery.ParameterizedQueryDataModel;
 import edu.wustl.cab2b.client.ui.parameterizedQuery.ParameterizedQueryMainPanel;
 import edu.wustl.cab2b.client.ui.query.IClientQueryBuilderInterface;
 import edu.wustl.cab2b.client.ui.query.Utility;
 import edu.wustl.cab2b.client.ui.searchDataWizard.addLimit.AddLimitPanel;
 import edu.wustl.cab2b.client.ui.searchDataWizard.dag.DagControlPanel;
 import edu.wustl.cab2b.client.ui.searchDataWizard.dag.MainDagPanel;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.client.ui.util.CustomSwingWorker;
 import edu.wustl.cab2b.client.ui.util.WindowUtilities;
 import edu.wustl.cab2b.client.ui.viewresults.DataListPanel;
 import edu.wustl.cab2b.client.ui.viewresults.ViewSearchResultsPanel;
 import edu.wustl.cab2b.common.datalist.IDataRow;
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeConstants;
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeHandler;
 import edu.wustl.cab2b.common.queryengine.Cab2bQuery;
 import edu.wustl.cab2b.common.queryengine.ICab2bQuery;
 import edu.wustl.cab2b.common.queryengine.result.IQueryResult;
 import edu.wustl.common.querysuite.exceptions.MultipleRootsException;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.querysuite.queryobject.IQueryEntity;
 import edu.wustl.common.util.global.ApplicationProperties;
 
 /**
  * @author mahesh_iyer
  * 
  * The class represents the navigation searchPanel for the search dialog, and
  * also includes corresponding functionality.
  */
 public class SearchNavigationPanel extends Cab2bPanel implements ActionListener {
     private static final long serialVersionUID = 1234567890L;
 
     /* The reference to the parent searchPanel. */
     private MainSearchPanel mainSearchPanel = null;
 
     /* The navigation buttons. */
     private Cab2bButton previousButton;
 
     public Cab2bButton nextButton;
 
     private Cab2bButton saveDataListButton;
 
     private Cab2bButton saveConditionButton;
 
     private Cab2bButton addToExperimentButton;
 
     private Cab2bButton serviceUrlButton;
 
     private Cab2bPanel buttonPanel;
 
     private Cab2bPanel messagePanel;
 
     private static Cab2bLabel messageLabel;
 
     private IQueryResult queryResults;
 
     private AddLimitPanel addLimitPanel;
 
     private SearchCenterPanel searchCenterPanel;
 
     private final String previousButtonStr = "Previous";
 
     private final String nextButtonStr = "Next";
 
     private final String saveDataButtonStr = "Save Data List";
 
     private final String saveQueryButtonStr = "Save Query";
 
     private final String serviceUrlButtonStr = "Service URL";
 
     private final String addToExperimentButtonStr = "Add to Experiment";
 
     private Map<String, List<String>> entityURLMap = new HashMap<String, List<String>>();
 
     public SearchNavigationPanel(MainSearchPanel panel) {
         this.mainSearchPanel = panel;
         searchCenterPanel = mainSearchPanel.getCenterPanel();
         initGUI();
         this.setPreferredSize(new Dimension(976, 36));
         this.setBackground(new Color(240, 240, 240));
 
     }
 
     public Cab2bButton getNextButton() {
         return nextButton;
     }
 
     private void initGUI() {
         messageLabel = new Cab2bLabel();
         messageLabel.setForeground(new Color(0, 128, 0));
         messagePanel = new Cab2bPanel();
         messagePanel.setBackground(null);
         messagePanel.add(messageLabel);
 
         previousButton = new Cab2bButton(previousButtonStr);
         previousButton.addActionListener(new PreviousButtonActionListener());
 
         nextButton = new Cab2bButton(nextButtonStr);
         nextButton.addActionListener(new NextButtonActionListener());
 
         saveDataListButton = new Cab2bButton(saveDataButtonStr);
         saveDataListButton.setPreferredSize(new Dimension(160, 22));
         saveDataListButton.addActionListener(this);
 
         saveConditionButton = new Cab2bButton(saveQueryButtonStr);
         saveConditionButton.setPreferredSize(new Dimension(160, 22));
         saveConditionButton.addActionListener(new SaveConditionButtonActionListener());
 
         serviceUrlButton = new Cab2bButton(serviceUrlButtonStr);
         serviceUrlButton.setPreferredSize(new Dimension(160, 22));
         serviceUrlButton.addActionListener(new ServiceURLButtonActionListener());
 
         addToExperimentButton = new Cab2bButton(addToExperimentButtonStr);
         addToExperimentButton.setPreferredSize(new Dimension(160, 22));
         addToExperimentButton.addActionListener(this);
 
         FlowLayout flowLayout = new FlowLayout(FlowLayout.RIGHT);
         flowLayout.setHgap(10);
         buttonPanel = new Cab2bPanel();
         buttonPanel.setBackground(null);
         buttonPanel.setLayout(flowLayout);
         buttonPanel.add(saveConditionButton);
         buttonPanel.add(serviceUrlButton);
         buttonPanel.add(previousButton);
         buttonPanel.add(nextButton);
         buttonPanel.add(saveDataListButton);
         buttonPanel.add(addToExperimentButton);
         this.add("hfill", messagePanel);
         this.add("hfill right", buttonPanel);
         setButtons();
     }
 
     /**
      * Action listener class
      */
     public void actionPerformed(ActionEvent event) {
         Cab2bButton cab2bButton = (Cab2bButton) event.getSource();
         String actionCommand = cab2bButton.getActionCommand();
         if (actionCommand.equals(saveDataButtonStr)) {
             SaveDatalistPanel saveDataListPanel = new SaveDatalistPanel(mainSearchPanel);
             saveDataListPanel.showInDialog();
         } else if (actionCommand.equals(addToExperimentButtonStr)) {
             if (SaveDatalistPanel.isDataListSaved() == false) {
                 SaveDatalistPanel saveDataListPanel = new SaveDatalistPanel(mainSearchPanel);
                 saveDataListPanel.showInDialog();
             }
             if (SaveDatalistPanel.isDataListSaved() == true) {
                 NewExperimentDetailsPanel newExperimentDetailsPanel = new NewExperimentDetailsPanel();
                 newExperimentDetailsPanel.showInDialog();
             }
         }
         updateUI();
     }
 
     public void enableButtons() {
         setButtons();
     }
 
     /*
      * The method sets the focus appropriately for the top searchPanel. Also,
      * update the indexes. corresponding updated can be abstracted into the top
      * searchPanel class itself
      */
     public void showCard(boolean blnNext) {
         /* Get the center Panel through the parent. */
 
         /* Get the layout associated with the center searchPanel */
         CardLayout layout = (CardLayout) searchCenterPanel.getLayout();
 
         /* Get the currently selected index. */
         int iSelectedCard = searchCenterPanel.getSelectedCardIndex();
 
         /* Get the top Panel through the parent. */
         SearchTopPanel topPanel = this.mainSearchPanel.getTopPanel();
 
         if (blnNext && ((iSelectedCard + 1) < searchCenterPanel.getIdentifierCount())) {
             /*
              * Validate and increment index to get the corresponding identifier,
              * and use that to show the card. Also increment the index to
              * indicate the currently selected card.
              */
             layout.show(searchCenterPanel, searchCenterPanel.getIdentifier(iSelectedCard + 1));
             searchCenterPanel.setSelectedCardIndex((iSelectedCard + 1));
             topPanel.setFocus(iSelectedCard + 1, true);
         } else if (iSelectedCard > 0) {
             /*
              * Validate and decrement index to get the corresponding identifier,
              * and use that to show the card. Also decrement the index to
              * indicate the currently selected card.
              */
             layout.show(searchCenterPanel, searchCenterPanel.getIdentifier(iSelectedCard - 1));
             searchCenterPanel.setSelectedCardIndex((iSelectedCard - 1));
             topPanel.setFocus(iSelectedCard - 1, false);
         }
         setButtons();
     }
 
     private void setButtons() {
         int cardindex = mainSearchPanel.getCenterPanel().getSelectedCardIndex();
 
         if (cardindex == 0)
             previousButton.setVisible(false);
         else
             previousButton.setVisible(true);
 
         if (cardindex == 2)
             serviceUrlButton.setVisible(true);
         else
             serviceUrlButton.setVisible(false);
 
         if (cardindex > 1 && cardindex < 4)
             saveConditionButton.setVisible(true);
         else
             saveConditionButton.setVisible(false);
 
         if (cardindex == 4) {
             saveDataListButton.setVisible(true);
             nextButton.setVisible(false);
             addToExperimentButton.setVisible(true);
         } else {
             saveDataListButton.setVisible(false);
             nextButton.setVisible(true);
             addToExperimentButton.setVisible(false);
             if (cardindex == 3 && CommonUtils.getDataListSize() == 0) {
                 nextButton.setEnabled(false);
             } else {
                 nextButton.setEnabled(true);
             }
         }
     }
 
     /**
      * If got error or zero result on query execution call this method if we are
      * calling this method don't call showCard Method
      */
     public void gotoAddLimitPanel() {
         CardLayout layout = (CardLayout) searchCenterPanel.getLayout();
 
         layout.show(searchCenterPanel, searchCenterPanel.getIdentifier(1));
         searchCenterPanel.setSelectedCardIndex(1);
         SearchTopPanel topPanel = mainSearchPanel.getTopPanel();
         topPanel.setFocus(1, false);
         setButtons();
     }
 
     /**
      * Method to switch from view search result searchPanel to searchPanel data
      * list searchPanel ...
      * 
      * @param datarow
      */
 
     public void gotoDataListPanel(final IDataRow datarow) {
         DataListPanel dataListPanel = (DataListPanel) searchCenterPanel.getArrCardElement(searchCenterPanel.getSelectedCardIndex() + 1);
         if (dataListPanel != null) {
             searchCenterPanel.remove(dataListPanel);
         }
         if (datarow == null) {
             dataListPanel = new DataListPanel(MainSearchPanel.getDataList());
         } else {
             dataListPanel = new DataListPanel(MainSearchPanel.getDataList(), datarow);
         }
 
         searchCenterPanel.setArrCardElement(dataListPanel, 4);
         searchCenterPanel.add(dataListPanel, SearchCenterPanel.getStrDataListlbl());
         showCard(true);
     }
 
     /**
      * Action listener for next button
      * 
      */
     private class NextButtonActionListener implements ActionListener {
         public void actionPerformed(ActionEvent arg0) {
             final IClientQueryBuilderInterface clientQueryBuilder = mainSearchPanel.getQueryObject();
             SearchNavigationPanel.messageLabel.setText("");
 
             if (searchCenterPanel.getSelectedCardIndex() == 0) {
                 setAddLimitPanelInWizard();
             } else if (searchCenterPanel.getSelectedCardIndex() == 1) {
                 CustomSwingWorker swingWorker = new CustomSwingWorker(SearchNavigationPanel.this) {
                     @Override
                     protected void doNonUILogic() throws Exception {
                         validateQueryAndMoveToAddLimitPanel(clientQueryBuilder);
                     }
 
                     @Override
                     protected void doUIUpdateLogic() throws Exception {
                         if (clientQueryBuilder == null || clientQueryBuilder.getVisibleExressionIds().size() == 0) {
                             // Pop-up a dialog asking the user to add alteast a
                             // rule.
                             JOptionPane.showMessageDialog(mainSearchPanel.getParent(),
                                                           "Please add Limit(s) before proceeding",
                                                           "Cannot Proceed", JOptionPane.WARNING_MESSAGE);
                             return;
                         }
                     }
                 };
                 swingWorker.start();
             } else if (searchCenterPanel.getSelectedCardIndex() == 2) {
                 CustomSwingWorker swingWorker = new CustomSwingWorker(SearchNavigationPanel.this,
                         SearchNavigationPanel.this.mainSearchPanel) {
 
                     protected void doNonUILogic() throws Exception {
                         executeQuery(clientQueryBuilder);
                     }
 
                     protected void doUIUpdateLogic() {
                         showResult();
                     }
                 };
                 swingWorker.start();
             } else if (searchCenterPanel.getSelectedCardIndex() == 3) {
                 gotoDataListPanel(null);
             } else {
                 showCard(true);
             }
         }
     }
 
     /**
      * @param clientQueryBuilder
      */
     private void validateQueryAndMoveToAddLimitPanel(IClientQueryBuilderInterface clientQueryBuilder) {
 
         if (clientQueryBuilder != null && clientQueryBuilder.getVisibleExressionIds().size() > 0) {
 
             final IQuery b2bquery = mainSearchPanel.queryObject.getQuery();
 
             /*
              * Get the root expression ID. If exception occurs
              * show Error
              */
             try {
                 b2bquery.getConstraints().getRootExpression();
             } catch (MultipleRootsException e) {
                 JOptionPane.showMessageDialog(mainSearchPanel.getParent(),
                                               ErrorCodeHandler.getErrorMessage(ErrorCodeConstants.QM_0003),
                                               "Connect Nodes.", JOptionPane.WARNING_MESSAGE);
                 gotoAddLimitPanel();
                 return;
             }
             JXPanel secPanel = searchCenterPanel.getArrCardElement(2);
             if (null != searchCenterPanel.getArrCardElement(2)) {
                 searchCenterPanel.remove(secPanel);
             }
             AdvancedDefineViewPanel defineViewPanel = new AdvancedDefineViewPanel(searchCenterPanel);
             secPanel = defineViewPanel;
             searchCenterPanel.add(defineViewPanel, SearchCenterPanel.getStrDefineSearchResultslbl());
             // Implies the next button was clicked. Call
             // show card
             // with boolean set to true.
             showCard(true);
 
         }
 
     }
 
     /**
      * 
      */
     private void showResult() {
         if (queryResults != null) {
             int recordNo = Utility.getRecordNum(queryResults);
             if (recordNo == 0) {
                 JOptionPane.showMessageDialog(SearchNavigationPanel.this.mainSearchPanel.getParent(),
                                               "No result found.", "", JOptionPane.INFORMATION_MESSAGE);
                 if (SearchNavigationPanel.this.mainSearchPanel.isParaQueryShowResultButtonPressed()) {
                     // close the search wizard
                     NewWelcomePanel.getMainFrame().closeSearchWizardDialog();
                     return;
                 } else {
                     gotoAddLimitPanel();
                 }
             } else {
                 ViewSearchResultsPanel viewSearchResultsPanel = new ViewSearchResultsPanel(queryResults,
                         mainSearchPanel);
                 JXPanel panelThree = searchCenterPanel.getArrCardElement(3);
                 if (searchCenterPanel.getArrCardElement(3) != null) {
                     searchCenterPanel.remove(panelThree);
                 }
                 panelThree = viewSearchResultsPanel;
                 searchCenterPanel.add(viewSearchResultsPanel, SearchCenterPanel.getStrViewSearchResultslbl());
                 showCard(true);
             }
        } else {
            gotoAddLimitPanel();
         }
     }
 
     /**
      * @param clientQueryBuilder
      */
     private void executeQuery(IClientQueryBuilderInterface clientQueryBuilder) {
 
         try {
             ICab2bQuery cab2bQuery = (ICab2bQuery) clientQueryBuilder.getQuery();
             if (!CommonUtils.isServiceURLConfigured(cab2bQuery, mainSearchPanel)) {
                 queryResults = null;
             } else {
 
                 // Get the Functional class for root and update
                 // query
                 // object with it.
                 queryResults = CommonUtils.executeQuery((ICab2bQuery) clientQueryBuilder.getQuery(),
                                                         mainSearchPanel);
             }
         } catch (Exception e) {
             CommonUtils.handleException(e, SearchNavigationPanel.this.mainSearchPanel, true, false, false, false);
 
             queryResults = null;
             if (SearchNavigationPanel.this.mainSearchPanel.isParaQueryShowResultButtonPressed()) {
                 NewWelcomePanel.getMainFrame().closeSearchWizardDialog();
                 return;
             }
         }
 
     }
 
     /**
      * Action listener for previous button
      * 
      */
     private class PreviousButtonActionListener implements ActionListener {
         public void actionPerformed(ActionEvent arg0) {
             // Implies the previous button was clicked. Call show card with
             // boolean set to false.
             SearchNavigationPanel.messageLabel.setText("");
             int cardIndex = searchCenterPanel.getSelectedCardIndex();
             if (cardIndex == 1) {
                 // setting the search panel
                 ChooseCategoryPanel chooseCategoryPanel = searchCenterPanel.getChooseCategoryPanel();
                 chooseCategoryPanel.addSearchPanel(searchCenterPanel.getAddLimitPanel().getSearchPanel());
                 searchCenterPanel.setChooseCategoryPanel(chooseCategoryPanel);
             } else if (cardIndex == 2) {
                 updateMainDagPanel();
             } else if (cardIndex == 3 && mainSearchPanel.isParaQueryShowResultButtonPressed()) {
                 showCard(false);
                 updateMainDagPanel();
             }
             showCard(false);
         }
     }
 
     /**
      * Method to display add limit Page on Search Data Wizard 
      */
     public void setAddLimitPanelInWizard() {
         addLimitPanel = searchCenterPanel.getAddLimitPanel();
         addLimitPanel.addSearchPanel(searchCenterPanel.getChooseCategoryPanel().getSearchPanel());
         searchCenterPanel.setAddLimitPanel(addLimitPanel);
         showCard(true);
     }
 
     /**
      * Method to update MainDagPanel
      */
     private void updateMainDagPanel() {
         addLimitPanel = (AddLimitPanel) mainSearchPanel.getCenterPanel().getAddLimitPanel();
         addLimitPanel.addPropertyChangeListener(new AddLimitPanelPCL());
         MainDagPanel mainDagPanel = addLimitPanel.getMainDagPanel();
         if (mainDagPanel.getVisibleNodeCount() == 0 && mainDagPanel.getExpressionCount() > 0) {
             mainDagPanel.updateGraph();
             addLimitPanel.editAddLimitUI(mainDagPanel.getFirstExpression());
             mainDagPanel.selectNode(mainDagPanel.getFirstExpression().getExpressionId());
         }
     }
 
     private Collection<EntityGroupInterface> getAllEntityGroups() {
         Collection<EntityGroupInterface> selectedEntityList = new HashSet<EntityGroupInterface>();
         final IQuery b2bquery = mainSearchPanel.queryObject.getQuery();
         Set<IQueryEntity> iQuerySet = b2bquery.getConstraints().getQueryEntities();
         for (IQueryEntity iqueryEntity : iQuerySet) {
             selectedEntityList.addAll(iqueryEntity.getDynamicExtensionsEntity().getEntityGroupCollection());
         }
 
         return selectedEntityList;
     }
 
     class AddLimitPanelPCL implements PropertyChangeListener {
 
         /* (non-Javadoc)
          * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
          */
         public void propertyChange(PropertyChangeEvent evt) {
             if (evt.getPropertyName().equals(DagControlPanel.EVENT_RESET_BUTTON_CLICKED)) {
                 mainSearchPanel.setQueryObject(null);
             }
         }
     }
 
     /**
      * This class is the serviceurl button action listener 
      *  
      * @author atul_jawale
      *
      */
     private class ServiceURLButtonActionListener implements ActionListener {
 
         /**
          * This method initialises the entityURLMapi.e. entityName against the url list which is by 
          * default set to the service urls present already for user in the repository.This map is 
          * used only for the present query and all the url configuration will be made to this map only.
          */
         private void initialiseMap(Collection<EntityGroupInterface> entityGruopCollection) {
 
             Map<String, List<String>> entityNameToURL = UserCache.getInstance().getEntityGroupToURLs();
             for (EntityGroupInterface entityGroup : entityGruopCollection) {
                 String entityName = entityGroup.getLongName();
                 List<String> urlList = entityNameToURL.get(entityName);
                 entityURLMap.put(entityName, urlList);
             }
         }
 
         /**
          * This is the action listener for the service URL button which gets all the entities
          * present in the query and sends it to the Right Panel constructor and shows the user
          * service url screen  
          */
         public void actionPerformed(ActionEvent arg0) {
             Collection<EntityGroupInterface> selectedEntityList = getAllEntityGroups();
             Cab2bPanel mainPanel = new Cab2bPanel(new BorderLayout(10, 5));
             if (entityURLMap.size() == 0) {
                 initialiseMap(selectedEntityList);
             }
 
             MainFrame.setScreenDimesion(Toolkit.getDefaultToolkit().getScreenSize());
             Dimension screenDimesion = MainFrame.getScreenDimesion();
             final String title = ApplicationProperties.getValue(MYSETTINGS_FRAME_TITLE);
             Dimension dimension = new Dimension((int) (screenDimesion.width * 0.90),
                     (int) (screenDimesion.height * 0.85));
             final JDialog serviceInstanceDialog = WindowUtilities.setInDialog(NewWelcomePanel.getMainFrame(),
                                                                               mainPanel, title, dimension, true,
                                                                               true);
             final RightPanel rightPanel = new RightPanel(selectedEntityList, entityURLMap);
             mainPanel.add(rightPanel, BorderLayout.CENTER);
             serviceInstanceDialog.addWindowListener(new WindowAdapter() {
                 public void windowClosing(WindowEvent arg0) {
 
                     entityURLMap.putAll(rightPanel.getEntityToURLMap());
                     updateQueryForURLS();
                 }
             });
             rightPanel.addPropertyChangeListener(DIALOG_CLOSE_EVENT, new PropertyChangeListener() {
                 public void propertyChange(PropertyChangeEvent arg0) {
                     entityURLMap.putAll(rightPanel.getEntityToURLMap());
                     updateQueryForURLS();
                     serviceInstanceDialog.dispose();
                 }
             });
             serviceInstanceDialog.setVisible(true);
         }
 
         /**
          * This method updates the query urls when user selects the service urls 
          * by clicking on the service URL button. This method actually update only
          * the root entity urls.   
          */
         private void updateQueryForURLS() {
             List<String> queryURLList = new ArrayList<String>();
             final ICab2bQuery b2bquery = (ICab2bQuery) mainSearchPanel.queryObject.getQuery();
             EntityGroupInterface entityGroup = b2bquery.getOutputEntity().getEntityGroupCollection().iterator().next();
             String groupName = entityGroup.getLongName();
             queryURLList.addAll(entityURLMap.get(groupName));
             b2bquery.getOutputUrls().clear();
             b2bquery.getOutputUrls().addAll(queryURLList);
         }
     }
 
     /**
      * Save button action listener class
      * 
      * @author deepak_shingan
      * 
      */
     private class SaveConditionButtonActionListener implements ActionListener {
         public void actionPerformed(ActionEvent arg0) {
             ParameterizedQueryMainPanel parameterizedQueryMainPanel = null;
 
             Cab2bQuery query = (Cab2bQuery) mainSearchPanel.getQueryObject().getQuery();
             Long userId = UserCache.getInstance().getCurrentUser().getUserId();
             query.setUserId(userId);
             if (CommonUtils.isServiceURLConfigured(query, mainSearchPanel.getParent())) {
                 parameterizedQueryMainPanel = new ParameterizedQueryMainPanel(new ParameterizedQueryDataModel(
                         query));
                 parameterizedQueryMainPanel.showInDialog();
             }
             // for updating queries on main frame
             MainFrameStackedBoxPanel.getInstance().getSavedQueryLinkPanel().updateQueryLinkPanel();
         }
     }
 
     /**
      * @return the messageLabel
      */
     public static Cab2bLabel getMessageLabel() {
         return messageLabel;
     }
 
     /**
      * @param messageLabel the messageLabel to set
      */
     public static void setMessageLabel(Cab2bLabel messageLabel) {
         SearchNavigationPanel.messageLabel = messageLabel;
     }
 }
