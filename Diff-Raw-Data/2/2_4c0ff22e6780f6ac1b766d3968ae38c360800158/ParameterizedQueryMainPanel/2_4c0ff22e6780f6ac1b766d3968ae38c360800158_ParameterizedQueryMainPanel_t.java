 package edu.wustl.cab2b.client.ui.parameterizedQuery;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 
 import javax.swing.JDialog;
 
 import edu.wustl.cab2b.client.cache.UserCache;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.mainframe.MainFrame;
 import edu.wustl.cab2b.client.ui.mainframe.NewWelcomePanel;
 import edu.wustl.cab2b.client.ui.util.WindowUtilities;
 
 /**
  * Class to create parameterized Query UI panel 
  * @author deepak_shingan
  *
  */
 public class ParameterizedQueryMainPanel extends Cab2bPanel {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * panel required to display Query info, Located at top in parameterizedQueryPanel  
      */
     private ParameterizedQueryInfoPanel informationQueryPanel;
 
     /**
      * panel required to display condition parameters of Query, Located at center in parameterizedQueryPanel  
      */
    ParameterizedQueryConditionPanel parameterConditionPanel;
 
     /**
      * panel located at bottom part of ParameterizedQueryMainPanel containing Nevigation 
      * buttons  
      */
     private ParameterizedQueryNavigationPanel parameterizedQueryNavigationPanel;
 
     /**
      *  Data model for parameterizedQueryPanel
      */
     private ParameterizedQueryDataModel parameterizedQueryDataModel;
 
     private JDialog dialog;
 
     public ParameterizedQueryMainPanel() {
         parameterizedQueryDataModel = new ParameterizedQueryDataModel();
         Long userId = UserCache.getInstance().getCurrentUser().getUserId();
         parameterizedQueryDataModel.setQueryUserId(userId);
         initGUI();
     }
 
     public ParameterizedQueryMainPanel(ParameterizedQueryDataModel queryModel) {
         parameterizedQueryDataModel = queryModel;
         Long userId = UserCache.getInstance().getCurrentUser().getUserId();
         parameterizedQueryDataModel.setQueryUserId(userId);
         initGUI();
     }
 
     /**
      * Method to generate UI         
      */
     private void initGUI() {
         this.setLayout(new BorderLayout());
         this.add(getInformationQueryPanel(), BorderLayout.NORTH);
         this.add(getParameterConditionPanel(), BorderLayout.CENTER);
         this.add(getparameterizedQueryNavigationPanel(), BorderLayout.SOUTH);
     }
 
     /**
      * @return parameterConditionPanel
      */
     public ParameterizedQueryConditionPanel getParameterConditionPanel() {
         if (parameterConditionPanel == null)
             parameterConditionPanel = new ParameterizedQueryConditionPanel(parameterizedQueryDataModel, false);
 
         return parameterConditionPanel;
     }
 
     /**
      * Returns informationQueryPanel 
      * @return ParameterizedQueryInfoPanel
      */
     public ParameterizedQueryInfoPanel getInformationQueryPanel() {
         if (informationQueryPanel == null)
             informationQueryPanel = new ParameterizedQueryInfoPanel();
         return informationQueryPanel;
     }
 
     /**
      * Returns parameterizedQueryNavigationPanel 
      * @return ParameterizedQueryNavigationPanel
      */
     private Cab2bPanel getparameterizedQueryNavigationPanel() {
         if (parameterizedQueryNavigationPanel == null)
             parameterizedQueryNavigationPanel = new ParameterizedQueryNavigationPanel(this);
 
         return parameterizedQueryNavigationPanel;
     }
 
     /**
      * @return the parameterizedQueryDataModel
      */
     public ParameterizedQueryDataModel getParameterizedQueryDataModel() {
         return parameterizedQueryDataModel;
     }
 
     /**
      * @param parameterizedQueryDataModel the parameterizedQueryDataModel to set
      */
     public void setParameterizedQueryDataModel(ParameterizedQueryDataModel parameterizedQueryDataModel) {
         this.parameterizedQueryDataModel = parameterizedQueryDataModel;
     }
 
     /**
      * @param informationQueryPanel the informationQueryPanel to set
      */
     public void setInformationQueryPanel(ParameterizedQueryInfoPanel informationQueryPanel) {
         this.informationQueryPanel = informationQueryPanel;
     }
 
     public void showInDialog() {
         Dimension dimension = MainFrame.getScreenDimesion();
         dialog = WindowUtilities.setInDialog(NewWelcomePanel.getMainFrame(), this, "Unsaved Condition",
                                              new Dimension((int) (dimension.width * 0.80),
                                                      (int) (dimension.height * 0.75)), true, false);
         dialog.setVisible(true);
 
     }
 
     /**
      * @return the dialog
      */
     public JDialog getDialog() {
         return dialog;
     }
 
     /**
      * @param dialog the dialog to set
      */
     public void setDialog(JDialog dialog) {
         this.dialog = dialog;
     }
 
 }
