 package edu.wustl.cab2b.client.ui;
 
 import static edu.wustl.cab2b.client.ui.util.ApplicationResourceConstants.MAIN_FRAME_SEARCH_HELP_TEXT;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.util.Set;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.ToolTipManager;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.Keymap;
 
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.client.cache.ClientSideCache;
 import edu.wustl.cab2b.client.metadatasearch.MetadataSearch;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.client.ui.util.CustomSwingWorker;
 import edu.wustl.cab2b.common.beans.MatchedClass;
 import edu.wustl.cab2b.common.cache.IEntityCache;
 import edu.wustl.cab2b.common.exception.CheckedException;
 import edu.wustl.common.util.global.ApplicationProperties;
 
 /**
  * The abstract class that contains commonalities between the advanced/category
  * search panels for the main as well as 'AddLimit' section from the main search
  * dialog. Concrete classes must over ride methods to effect custom layout.
  * 
  * @author mahesh_iyer/Deepak_Shingan
  * 
  */
 
 public class SearchPanel extends Cab2bPanel {
     private static final long serialVersionUID = 1L;
 
     /**
      * The reference to the parent content searchPanel required to be refreshed for
      * the appropritate event.
      */
     private ContentPanel contentPanel;
 
     /**
      * A generic reference to the specific implementation of the advanced search
      * searchPanel.
      */
     public AdvancedSearchPanel advSearchPanel;
 
     /** A specific implementation of the results searchPanel. */
    public SearchResultPanel srhResultPanel;
 
     /** Text field to specify the search term. */
     public JTextField srhTextField;
 
     /** search button.*/
     protected JButton srhButton;
 
     /**Error message searchPanel**/
     Cab2bPanel errorMsgPanel;
 
     /**
      * constructor
      * 
      * @param contentPanel
      *            The reference to the parent content searchPanel that is saved, so
      *            that it can be made available to child components, which can
      *            then cause the parent to refresh for appropriate events.
      */
 
     public SearchPanel(ContentPanel addLimitPanel) {
         this.contentPanel = addLimitPanel;
         initGUI();
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 srhTextField.requestFocus();
             }
         });
     }
 
     /**
      * Method initializes the searchPanel by appropriately laying out child components.
      * 
      */
 
     private void initGUI() {
         /* Set the layout.*/
         this.setLayout(new RiverLayout());
 
         /* Intialize the Search button.*/
         srhButton = new Cab2bButton("Search");
         srhButton.setEnabled(false);
         srhButton.addActionListener(new SearchActionListener(this.contentPanel));
 
         /* Intializa the text field.*/
         srhTextField = new JTextField();
         Keymap keyMap = JTextField.addKeymap("enter", srhTextField.getKeymap());
         KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
         keyMap.addActionForKeyStroke(key, new SearchActionListener(this.contentPanel));
         srhTextField.setKeymap(keyMap);
 
         /* Add a listener to the text-field.*/
         srhTextField.getDocument().addDocumentListener(new DocumentListener() {
             public void insertUpdate(DocumentEvent arg0) {
                 srhButton.setEnabled(true);
             }
 
             public void removeUpdate(DocumentEvent arg0) {
                 if (arg0.getDocument().getLength() == 0) {
                     srhButton.setEnabled(false);
                 }
             }
 
             public void changedUpdate(DocumentEvent arg0) {
                 /* No implementation for this method is required.*/
             }
         });
 
         srhTextField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 220)));
         final String helpText = ApplicationProperties.getValue(MAIN_FRAME_SEARCH_HELP_TEXT);
         srhTextField.setToolTipText(helpText);
         ToolTipManager.sharedInstance().setDismissDelay(500000);
         /* Invoke the method based on concrete implementations from sub-class*/
         addTextField();
 
         /* Add the components to the searchPanel.*/
 
         /* Invoke the method to get the specific type of Advanced search searchPanel to be added*/
         advSearchPanel = this.getAdvancedSearchPanel();
         this.add(srhButton);
         Cab2bLabel cab2bLabel = new Cab2bLabel(helpText);
         this.add("br ", new Cab2bLabel(" "));
         this.add(cab2bLabel);
         this.add("br tab", advSearchPanel);
     }
 
     public void setUIForChooseCategorySearchPanel() {
         srhTextField.setPreferredSize(new Dimension(350, 22));
         if (advSearchPanel.getTaskPane().isExpanded()) {
             advSearchPanel.setPreferredSize(new Dimension(472, 140));
         } else {
             advSearchPanel.setPreferredSize(new Dimension(472, 30));
         }
         setBorder(null);
     }
 
     public void setUIForAddLimitSearchPanel() {
         srhTextField.setPreferredSize(new Dimension(160, 23));
         if (advSearchPanel.getTaskPane().isExpanded()) {
             advSearchPanel.setPreferredSize(new Dimension(265, 140));
         } else {
             advSearchPanel.setPreferredSize(new Dimension(265, 30));
         }
         setBorder(null);
     }
 
     /**
      * Method to set search text in searchTextField 
      * @param searchText
      */
     public void setSearchtext(String searchText) {
         srhTextField.setText(searchText);
     }
 
     /**
      * Method to get search text in searchTextField
      *  
      */
     public String getSearchtext() {
         return srhTextField.getText();
     }
 
     /**
      * Getter method for returning a reference to the text field.
      * 
      */
     JTextField getTextField() {
         return srhTextField;
     }
 
     /**
      * The method clears any previously searched results, by removing the
      * corresponding searchPanel.
      * 
      * @param resultPanel
      *            The results searchPanel to be removed
      * 
      */
     private void removeResultPanel() {
         if (srhResultPanel != null) {
             srhResultPanel.removeResultPanel();
             this.remove(srhResultPanel);
         }
         this.updateUI();
     }
 
     /**
      * The method adds the {@link AddLimitSearchResultPanel}dynamically to this searchPanel. 
      * 
      * @param resultPanel
      *            The results searchPanel to be added.
      */
     public void addResultsPanel(SearchResultPanel resultPanel) {
         if (resultPanel != null) {
             this.add("p vfill", resultPanel);
             srhResultPanel = resultPanel;
         }
         this.updateUI();
     }
 
     public SearchResultPanel getSerachResultPanel() {
         return srhResultPanel;
     }
 
     public void setSerachResultPanel(SearchResultPanel searchResultPanel) {
         srhResultPanel = searchResultPanel;
     }
 
     /**
      * Action listener for the text field as well as the search button.
      * 
      */
 
     private class SearchActionListener extends AbstractAction {
         private static final long serialVersionUID = 1L;
 
         /** Component reference to pass to Error dialog boxes, for centering dialogs. */
         private Component comp;
 
         public SearchActionListener(Component comp) {
             this.comp = comp;
         }
 
         public void actionPerformed(ActionEvent ae) {
             /* Read the value from the text field.*/
             String value = srhTextField.getText();
             value = CommonUtils.removeContinuousSpaceCharsAndTrim(value);
             final String[] values = value.split("\\s");
 
             /* Invoke the method to determing the combination of search.*/
             final int[] searchTargetStatus = advSearchPanel.getSearchTargetStatus();
             final int searchOn = advSearchPanel.getSearchOnStatus();
 
             CustomSwingWorker swingWorker = new CustomSwingWorker(comp) {
                 Set<EntityInterface> srhResult = null;
 
                 protected void doNonUILogic() throws RuntimeException {
 
                     try {
                         IEntityCache cache = ClientSideCache.getInstance();
                         MetadataSearch metadataSearch = new MetadataSearch(cache);
                         MatchedClass matchedClass = metadataSearch.search(searchTargetStatus, values, searchOn);
                         /* The results that is the collection of entities. */
                         srhResult = matchedClass.getEntityCollection();
                     } catch (CheckedException e1) {
                         CommonUtils.handleException(e1, comp, true, true, true, false);
                     }
                 }
 
                 @Override
                 protected void doUIUpdateLogic() throws RuntimeException {
                     //replace previous panels 
                     removeResultPanel();
 
                     /* Add an appropriate instance of the search results searchPanel to this searchPanel */
                     srhResultPanel = getSearchResultPanel(contentPanel, srhResult);
                     contentPanel.setSearchResultPanel(srhResultPanel);
                     addResultsPanel(srhResultPanel);
                 }
             };
             swingWorker.start();
         }
     }
 
     /**
      * The abstract method returns the appropriate type of
      * {@link SearchResultPanel} to be added to this searchPanel. Sub-classes
      * are required to over-ride this method.
      * 
      * @param contentPanel
      *            The reference to the parent content searchPanel required by the a
      *            specific instance of {@link SearchResultPanel} to be
      *            refreshed for the appropritate events it can generate.
      * 
      * @param searchResult
      *            The collection of Entities
      */
     public SearchResultPanel getSearchResultPanel(ContentPanel addLimitPanel, Set<EntityInterface> searchResult) {
         return new SearchResultPanel(addLimitPanel, searchResult);
     }
 
     /**
      * The abstract method returns the appropriate type of
      * {@link AdvancedSearchPanel} to be added to this searchPanel. Sub-classes
      * are required to over-ride this method.
      * 
      * @return AdvancedSearchPanel 
      */
 
     public AdvancedSearchPanel getAdvancedSearchPanel() {
         if (advSearchPanel == null)
             return new AdvancedSearchPanel();
         else
             return advSearchPanel;
     }
 
     public void setAdvancedSearchPanel(AdvancedSearchPanel advancedSearchPanel) {
         if (advancedSearchPanel != null) {
             this.remove(advSearchPanel);
         }
         advSearchPanel = advancedSearchPanel;
         this.add("br ", advSearchPanel);
     }
 
     /**
      * The abstract method to add the text field in a manner required by the
      * specific instance of {@link SearchPanel}
      * 
      */
     public void addTextField() {
         this.getTextField().setPreferredSize(new Dimension(350, 22));
         this.add("tab ", new Cab2bLabel());
         this.add(this.getTextField());
     }
 }
