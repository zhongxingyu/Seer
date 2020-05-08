 package edu.wustl.cab2b.client.ui;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import javax.swing.JOptionPane;
 
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.dag.DagControlPanel;
 import edu.wustl.cab2b.client.ui.dag.MainDagPanel;
 import edu.wustl.cab2b.client.ui.experiment.NewExperimentDetailsPanel;
 import edu.wustl.cab2b.client.ui.mainframe.NewWelcomePanel;
 import edu.wustl.cab2b.client.ui.parameterizedQuery.ParameterizedQueryDataModel;
 import edu.wustl.cab2b.client.ui.parameterizedQuery.ParameterizedQueryMainPanel;
 import edu.wustl.cab2b.client.ui.query.IClientQueryBuilderInterface;
 import edu.wustl.cab2b.client.ui.query.Utility;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.client.ui.util.CustomSwingWorker;
 import edu.wustl.cab2b.client.ui.viewresults.DataListPanel;
 import edu.wustl.cab2b.client.ui.viewresults.ViewSearchResultsPanel;
 import edu.wustl.cab2b.common.datalist.IDataRow;
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeConstants;
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeHandler;
 import edu.wustl.cab2b.common.queryengine.ICab2bQuery;
 import edu.wustl.cab2b.common.queryengine.result.IQueryResult;
 import edu.wustl.common.querysuite.exceptions.MultipleRootsException;
 import edu.wustl.common.querysuite.queryobject.IQuery;
 import edu.wustl.common.querysuite.queryobject.impl.ExpressionId;
 
 /**
  * @author mahesh_iyer
  * 
  * The class represents the navigation searchPanel for the search dialog, and
  * also includes corresponding functionality.
  */
 
 public class SearchNavigationPanel extends Cab2bPanel implements ActionListener {
     private static final long serialVersionUID = 1234567890L;
 
     /* The reference to the parent searchPanel. */
     private MainSearchPanel m_mainSearchPanel = null;
 
     /* The navigation buttons. */
     private Cab2bButton previousButton;
 
     public Cab2bButton nextButton;
 
     private Cab2bButton saveDataListButton;
 
     private Cab2bButton saveConditionButton;
 
     private Cab2bButton addToExperimentButton;
 
     private Cab2bPanel buttonPanel;
 
     private Cab2bPanel messagePanel;
 
     public static Cab2bLabel messageLabel;
 
     private IQueryResult queryResults;
 
     private AddLimitPanel addLimitPanel;
 
     public SearchNavigationPanel(MainSearchPanel panel) {
         this.m_mainSearchPanel = panel;
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
 
         previousButton = new Cab2bButton("Previous");
         previousButton.addActionListener(new PreviousButtonActionListener());
 
         nextButton = new Cab2bButton("Next");
         nextButton.addActionListener(new NextButtonActionListener());
 
         saveDataListButton = new Cab2bButton("Save Data List");
         saveDataListButton.setPreferredSize(new Dimension(160, 22));
         saveDataListButton.addActionListener(this);
 
         saveConditionButton = new Cab2bButton("Save Query");
         saveConditionButton.setPreferredSize(new Dimension(160, 22));
         saveConditionButton.addActionListener(new SaveConditionButtonActionListener());
 
         addToExperimentButton = new Cab2bButton("Add to Experiment");
         addToExperimentButton.setPreferredSize(new Dimension(160, 22));
         addToExperimentButton.addActionListener(this);
 
         FlowLayout flowLayout = new FlowLayout(FlowLayout.RIGHT);
         flowLayout.setHgap(10);
         buttonPanel = new Cab2bPanel();
         buttonPanel.setBackground(null);
         buttonPanel.setLayout(flowLayout);
         buttonPanel.add(saveConditionButton);
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
         String strActionCommand = cab2bButton.getActionCommand();
         if (strActionCommand.equals("Save Data List")) {
             SaveDatalistPanel saveDataListPanel = new SaveDatalistPanel(m_mainSearchPanel);
             saveDataListPanel.showInDialog();
         } else if (strActionCommand.equals("Add to Experiment")) {
             if (SaveDatalistPanel.isDataListSaved == false) {
                 SaveDatalistPanel saveDataListPanel = new SaveDatalistPanel(m_mainSearchPanel);
                 saveDataListPanel.showInDialog();
            }
            if (SaveDatalistPanel.isDataListSaved) {
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
         SearchCenterPanel centerPanel = this.m_mainSearchPanel.getCenterPanel();
 
         /* Get the layout associated with the center searchPanel */
         CardLayout layout = (CardLayout) centerPanel.getLayout();
 
         /* Get the currently selected index. */
         int iSelectedCard = centerPanel.getSelectedCardIndex();
 
         /* Get the top Panel through the parent. */
         SearchTopPanel topPanel = this.m_mainSearchPanel.getTopPanel();
 
         if (blnNext && ((iSelectedCard + 1) < centerPanel.getIdentifierCount())) {
             /*
              * Validate and increment index to get the corresponding identifier,
              * and use that to show the card. Also increment the index to
              * indicate the currently selected card.
              */
             layout.show(centerPanel, centerPanel.getIdentifier(iSelectedCard + 1));
             centerPanel.setSelectedCardIndex((iSelectedCard + 1));
             topPanel.setFocus(iSelectedCard + 1, true);
         } else if (iSelectedCard > 0) {
             /*
              * Validate and decrement index to get the corresponding identifier,
              * and use that to show the card. Also decrement the index to
              * indicate the currently selected card.
              */
             layout.show(centerPanel, centerPanel.getIdentifier(iSelectedCard - 1));
             centerPanel.setSelectedCardIndex((iSelectedCard - 1));
             topPanel.setFocus(iSelectedCard - 1, false);
         }
         setButtons();
     }
 
     private void setButtons() {
         int cardindex = m_mainSearchPanel.getCenterPanel().getSelectedCardIndex();
 
         if (cardindex == 0)
             previousButton.setVisible(false);
         else
             previousButton.setVisible(true);
 
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
         SearchCenterPanel centerPanel = m_mainSearchPanel.getCenterPanel();
         CardLayout layout = (CardLayout) centerPanel.getLayout();
 
         layout.show(centerPanel, centerPanel.getIdentifier(1));
         centerPanel.setSelectedCardIndex(1);
         SearchTopPanel topPanel = this.m_mainSearchPanel.getTopPanel();
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
         SearchCenterPanel searchCenterPanel = m_mainSearchPanel.getCenterPanel();
         DataListPanel dataListPanel = (DataListPanel) searchCenterPanel.m_arrCards[searchCenterPanel.getSelectedCardIndex() + 1];
         if (dataListPanel != null) {
             searchCenterPanel.remove(dataListPanel);
         }
         if (datarow == null) {
             dataListPanel = new DataListPanel(MainSearchPanel.getDataList());
         } else {
             dataListPanel = new DataListPanel(MainSearchPanel.getDataList(), datarow);
         }
 
         searchCenterPanel.m_arrCards[4] = dataListPanel;
         searchCenterPanel.add(dataListPanel, SearchCenterPanel.m_strDataListlbl);
         showCard(true);
     }
 
     /**
      * Action listener for next button
      * 
      */
     private class NextButtonActionListener implements ActionListener {
         public void actionPerformed(ActionEvent arg0) {
             final SearchCenterPanel searchCenterPanel = m_mainSearchPanel.getCenterPanel();
             final IClientQueryBuilderInterface clientQueryBuilder = m_mainSearchPanel.getQueryObject();
             SearchNavigationPanel.messageLabel.setText("");
 
             if (searchCenterPanel.getSelectedCardIndex() == 0) {
                 addLimitPanel = searchCenterPanel.getAddLimitPanel();
                 addLimitPanel.addSearchPanel(searchCenterPanel.getChooseCategoryPanel().getSearchPanel());
                 searchCenterPanel.setAddLimitPanel(addLimitPanel);
                 showCard(true);
             } else if (searchCenterPanel.getSelectedCardIndex() == 1) {
 
                 CustomSwingWorker swingWorker = new CustomSwingWorker(SearchNavigationPanel.this) {
                     @Override
                     protected void doNonUILogic() throws Exception {
                         if (clientQueryBuilder != null && clientQueryBuilder.getVisibleExressionIds().size() > 0) {
 
                             final IQuery b2bquery = m_mainSearchPanel.queryObject.getQuery();
 
                             /* Get the root expression ID. If exception occurs show Error*/
                             try {
                                 b2bquery.getConstraints().getRootExpressionId();
                             } catch (MultipleRootsException e) {
                                 JOptionPane.showMessageDialog(
                                                               m_mainSearchPanel.getParent(),
                                                               ErrorCodeHandler.getErrorMessage(ErrorCodeConstants.QM_0003),
                                                               "Connect Nodes.", JOptionPane.WARNING_MESSAGE);
                                 gotoAddLimitPanel();
                                 return;
                             }
 
                             if (null != searchCenterPanel.m_arrCards[2]) {
                                 searchCenterPanel.remove(searchCenterPanel.m_arrCards[2]);
                             }
                             AdvancedDefineViewPanel defineViewPanel = new AdvancedDefineViewPanel(
                                     searchCenterPanel);
                             searchCenterPanel.m_arrCards[2] = defineViewPanel;
                             searchCenterPanel.add(defineViewPanel, SearchCenterPanel.m_strDefineSearchResultslbl);
                             // Implies the next button was clicked. Call
                             // show card
                             // with boolean set to true.
                             showCard(true);
 
                         }
                     }
 
                     @Override
                     protected void doUIUpdateLogic() throws Exception {
                         if (clientQueryBuilder == null || clientQueryBuilder.getVisibleExressionIds().size() == 0) {
                             // Pop-up a dialog asking the user to add alteast a
                             // rule.
                             JOptionPane.showMessageDialog(m_mainSearchPanel.getParent(),
                                                           "Please add Limit(s) before proceeding",
                                                           "Cannot Proceed", JOptionPane.WARNING_MESSAGE);
                             return;
                         }
                     }
                 };
                 swingWorker.start();
             } else if (searchCenterPanel.getSelectedCardIndex() == 2) {
                 CustomSwingWorker swingWorker = new CustomSwingWorker(SearchNavigationPanel.this,
                         SearchNavigationPanel.this.m_mainSearchPanel) {
 
                     protected void doNonUILogic() throws Exception {
                         try {
                             // Get the Functional class for root and update query
                             // object with it.
                             queryResults = CommonUtils.executeQuery((ICab2bQuery) clientQueryBuilder.getQuery(),
                                                                     m_mainSearchPanel);
                         } catch (Exception e) {
                             CommonUtils.handleException(e, SearchNavigationPanel.this.m_mainSearchPanel, true,
                                                         false, false, false);
                             if (SearchNavigationPanel.this.m_mainSearchPanel.isParaQueryShowResultButtonPressed()) {
                                 NewWelcomePanel.mainFrame.closeSearchWizardDialog();
                                 return;
                             }
                         }
                     }
 
                     protected void doUIUpdateLogic() {
                         if (queryResults != null) {
                             int recordNo = Utility.getRecordNum(queryResults);
                             if (recordNo == 0) {
                                 JOptionPane.showMessageDialog(
                                                               SearchNavigationPanel.this.m_mainSearchPanel.getParent(),
                                                               "No result found.", "",
                                                               JOptionPane.INFORMATION_MESSAGE);
                                 if (SearchNavigationPanel.this.m_mainSearchPanel.isParaQueryShowResultButtonPressed()) {
                                     // close the search wizard
                                     NewWelcomePanel.mainFrame.closeSearchWizardDialog();
                                     return;
                                 } else {
                                     gotoAddLimitPanel();
                                 }
                             } else {
                                 ViewSearchResultsPanel viewSearchResultsPanel = new ViewSearchResultsPanel(
                                         queryResults, m_mainSearchPanel);
                                 if (searchCenterPanel.m_arrCards[3] != null) {
                                     searchCenterPanel.remove(searchCenterPanel.m_arrCards[3]);
                                 }
                                 searchCenterPanel.m_arrCards[3] = viewSearchResultsPanel;
                                 searchCenterPanel.add(viewSearchResultsPanel,
                                                       SearchCenterPanel.m_strViewSearchResultslbl);
                                 showCard(true);
                             }
                         } else {
                             gotoAddLimitPanel();
                         }
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
      * Action listener for previous button
      * 
      */
     private class PreviousButtonActionListener implements ActionListener {
         public void actionPerformed(ActionEvent arg0) {
             final SearchCenterPanel searchCenterPanel = m_mainSearchPanel.getCenterPanel();
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
                 addLimitPanel = (AddLimitPanel) m_mainSearchPanel.getCenterPanel().getAddLimitPanel();
                 addLimitPanel.addPropertyChangeListener(new AddLimitPanelPCL());
                 MainDagPanel mainDagPanel = addLimitPanel.getMainDagPanel();
 
                 if (mainDagPanel.getVisibleNodeCount() == 0 && mainDagPanel.getExpressionCount() > 0) {
 
                     mainDagPanel.updateGraph();
                     addLimitPanel.editAddLimitUI(mainDagPanel.getFirstExpression());
                     mainDagPanel.selectNode((ExpressionId) mainDagPanel.getFirstExpression().getExpressionId());
                 }
             }
             showCard(false);
         }
     }
 
     class AddLimitPanelPCL implements PropertyChangeListener {
 
         /* (non-Javadoc)
          * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
          */
         public void propertyChange(PropertyChangeEvent evt) {
             if (evt.getPropertyName().equals(DagControlPanel.EVENT_RESET_BUTTON_CLICKED)) {
                 m_mainSearchPanel.setQueryObject(null);
             }
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
 
             if (m_mainSearchPanel.getQueryObject().getQuery().getId() != null) {
                 messageLabel.setText("Any changes made in current query will be saved in system.");
             }
 
             ParameterizedQueryMainPanel parameterizedQueryMainPanel = new ParameterizedQueryMainPanel(
                     new ParameterizedQueryDataModel((ICab2bQuery) m_mainSearchPanel.getQueryObject().getQuery()));
             parameterizedQueryMainPanel.showInDialog();
         }
     }
 }
