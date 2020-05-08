 /**
  * 
  */
 package edu.wustl.cab2b.client.ui.mainframe.showall;
 
 import java.rmi.RemoteException;
 
 import javax.swing.table.TableColumnModel;
 
 import edu.wustl.cab2b.client.ui.controls.Cab2bTable;
 import edu.wustl.cab2b.client.ui.mainframe.NewWelcomePanel;
 import edu.wustl.cab2b.client.ui.parameterizedQuery.ParameterizedQueryShowResultPanel;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.common.ejb.EjbNamesConstants;
 import edu.wustl.cab2b.common.ejb.queryengine.QueryEngineBusinessInterface;
 import edu.wustl.cab2b.common.ejb.queryengine.QueryEngineHome;
 import edu.wustl.cab2b.common.queryengine.ICab2bQuery;
 
 /**
  * Panel class for showing all Categories from database in table format.
  * @author deepak_shingan
  *
  */
 public class ShowAllSavedQueryPanel extends ShowAllPanel {
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * Query name string
      */
     public static final String QUERY_NAME_TITLE = "Query Name";
 
     /**
      * Query Description string
      */
     public static final String QUERY_DESCRIPTION_TITLE = "Description";
 
     /**
      * Query Date string
      */
     public static final String QUERY_DATE_TITLE = "Date Created";
 
     /**
      * @param tableHeader
      * @param data
      */
     public ShowAllSavedQueryPanel(Object[] tableHeader, Object[][] data) {
         super(tableHeader, data);
         //Hiding last column from table which contains query object having only
         //QueryID
         TableColumnModel colModel = getTable().getColumnModel();
         if (colModel.getColumnCount() > 3)
             getTable().getColumnModel().removeColumn(colModel.getColumn(colModel.getColumnCount() - 1));
     }
 
     /* (non-Javadoc)
      * @see edu.wustl.cab2b.client.ui.mainframe.showall.ShowAllPanel#linkActionPerformed()
      */
     @Override
     public void linkActionPerformed() {
         Cab2bTable table = getTable();
 
         //Getting the selected hyperlink row
         int selectionIndex = getTable().getSelectionModel().getLeadSelectionIndex();
 
         // Getting object associated with hyperlink 
         // column Number will be always last column
         long queryID = (Long) table.getModel().getValueAt(selectionIndex, table.getModel().getColumnCount() - 1);
         queryLinkAction(queryID);
         updateUI();
 
     }
 
     /**
      * Action method for home-page query link
      * @param queryID
      */
     final public static void queryLinkAction(Long queryID) {
         QueryEngineBusinessInterface queryEngineBusinessInterface = (QueryEngineBusinessInterface) CommonUtils.getBusinessInterface(
                                                                                                                                     EjbNamesConstants.QUERY_ENGINE_BEAN,
                                                                                                                                     QueryEngineHome.class,
                                                                                                                                     null);
         ICab2bQuery cab2bQuery = null;
         try {
             cab2bQuery = queryEngineBusinessInterface.retrieveQueryById(queryID);
         } catch (RemoteException exception) {
             CommonUtils.handleException(exception, NewWelcomePanel.getMainFrame(), true, true, true, false);
             return;
         }
         ParameterizedQueryShowResultPanel parameterizedQueryPreviewPanel = new ParameterizedQueryShowResultPanel(
                 cab2bQuery);
        parameterizedQueryPreviewPanel.showInDialog("Saved Conditions");
     }
 }
