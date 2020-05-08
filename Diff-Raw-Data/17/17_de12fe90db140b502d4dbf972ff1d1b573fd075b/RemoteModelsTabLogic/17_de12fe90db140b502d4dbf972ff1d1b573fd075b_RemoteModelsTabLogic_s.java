 package jDistsim.application.designer.controller.tabLogic;
 
 import jDistsim.application.designer.common.Application;
 import jDistsim.application.designer.common.DialogBuilder;
 import jDistsim.application.designer.controller.InformationController;
 import jDistsim.application.designer.view.InformationView;
 import jDistsim.core.simulation.distributed.DistributedModelDefinition;
 import jDistsim.ui.dialog.BaseDialog;
 import jDistsim.ui.dialog.DistributedModelDialog;
 import jDistsim.ui.dialog.DistributedModuleSelectedDialog;
 import jDistsim.ui.dialog.LocalNetworkSettingsDialog;
 import jDistsim.ui.panel.listener.RemoteModelsTabListener;
 import jDistsim.utils.logging.Logger;
 import jDistsim.utils.pattern.observer.IObserver;
 import jDistsim.utils.pattern.observer.Observable;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import java.awt.event.MouseEvent;
 import java.util.Collection;
 import java.util.Vector;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 2.3.13
  * Time: 17:00
  */
 public class RemoteModelsTabLogic implements RemoteModelsTabListener, IObserver {
 
     private InformationController controller;
     private InformationView view;
     private DialogBuilder dialogBuilder;
 
     public RemoteModelsTabLogic(InformationController controller) {
         this.controller = controller;
         view = controller.getMainFrame().getView(InformationView.class);
         dialogBuilder = new DialogBuilder(controller.getMainFrame().getFrame());
 
         Application.global().getDistributedModels().addObserver(this);
         Application.global().getNetworkSettings().addObserver(this);
         rebuildTableModel();
         invalidateButtons();
         refreshInfoLabel();
     }
 
     @Override
     public void onOpenAddDialogButtonClick(MouseEvent mouseEvent, Object sender) {
         DistributedModelDialog dialog = new DistributedModelDialog(controller.getMainFrame().getFrame());
         dialog.showDialog();
         if (dialog.getDialogResult() == BaseDialog.Result.OK) {
             DistributedModelDefinition modelDefinition = dialog.getDistributedModelDefinition();
             Application.global().getDistributedModels().put(modelDefinition.getRmiModelName(), modelDefinition);
             Application.global().getDistributedModels().notifyObservers();
         }
     }
 
     @Override
     public void onOpenEditDialogButtonClick(MouseEvent mouseEvent, Object sender) {
         DistributedModuleSelectedDialog selectDialog = new DistributedModuleSelectedDialog(
                 controller.getMainFrame().getFrame(), "Select distributed model to edit");
 
         selectDialog.showDialog();
         if (selectDialog.getDialogResult() == BaseDialog.Result.OK) {
             DistributedModelDefinition modelDefinition = selectDialog.getModelDefinition();
             if (modelDefinition == null) {
                 dialogBuilder.buildErrorDialog("There was no model selected");
                 onOpenEditDialogButtonClick(mouseEvent, sender);
                 return;
             } else {
                 DistributedModelDialog dialog = new DistributedModelDialog(controller.getMainFrame().getFrame(), modelDefinition);
                 dialog.showDialog();
             }
         }
     }
 
     @Override
     public void onOpenRemoveDialogButtonClick(MouseEvent mouseEvent, Object sender) {
         DistributedModuleSelectedDialog selectDialog = new DistributedModuleSelectedDialog(
                 controller.getMainFrame().getFrame(), "Select distributed model to remove");
 
         selectDialog.showDialog();
         if (selectDialog.getDialogResult() == BaseDialog.Result.OK) {
             DistributedModelDefinition modelDefinition = selectDialog.getModelDefinition();
             if (modelDefinition == null) {
                 dialogBuilder.buildErrorDialog("There was no model selected");
                 onOpenEditDialogButtonClick(mouseEvent, sender);
                 return;
             } else {
                 int result = dialogBuilder.buildQuestionDialog("Really want to delete the distributed model?");
                 if (result == JOptionPane.YES_OPTION) {
                     Application.global().getDistributedModels().remove(modelDefinition.getRmiModelName());
                     Application.global().getDistributedModels().notifyObservers();
                 }
             }
         }
     }
 
     @Override
     public void onOpenLocalSettingsDialog(MouseEvent mouseEvent, Object sender) {
         LocalNetworkSettingsDialog dialog = new LocalNetworkSettingsDialog(controller.getMainFrame().getFrame(), "Local settings");
         dialog.showDialog();
 
         if (dialog.getDialogResult() == BaseDialog.Result.OK) {
 
         }
     }
 
     private void rebuildTableModel() {
         Collection<DistributedModelDefinition> distributedModels = Application.global().getDistributedModels().values();
         Vector<String> columns = makeColumns();
         Vector<Vector<String>> rows = new Vector<>();
 
         for (DistributedModelDefinition modelDefinition : distributedModels) {
             rows.addElement(makeRow(modelDefinition));
         }
 
         controller.getModel().getRemoteModelsTable().setModel(new DefaultTableModel(rows, columns));
         InformationView view = controller.getMainFrame().getView(InformationView.class);
         view.renderRemoteModelsTable();
         Logger.log("Rebuild remote models table");
 
     }
 
     private Vector<String> makeRow(DistributedModelDefinition modelDefinition) {
         Vector<String> row = new Vector<>();
         row.addElement(modelDefinition.getModelName());
         row.addElement(modelDefinition.getRmiModelName());
         row.addElement(String.valueOf(modelDefinition.isReceive()));
         row.addElement(modelDefinition.getAddress());
         row.addElement(String.valueOf(modelDefinition.getPort()));
         row.addElement(String.valueOf(modelDefinition.isLookahead()));
         return row;
     }
 
     private Vector<String> makeColumns() {
         Vector columns = new Vector();
         columns.addElement("Model name");
         columns.addElement("RMI name");
         columns.addElement("Receive");
         columns.addElement("Remote address");
         columns.addElement("Port");
         columns.addElement("Lookahead");
         return columns;
     }
 
     private void invalidateButtons() {
         if (Application.global().getDistributedModels().isEmpty()) {
             view.getContentPane().getRemoteModelsTabPanel().getEditButton().deactivate();
             view.getContentPane().getRemoteModelsTabPanel().getRemoveButton().deactivate();
         } else {
             view.getContentPane().getRemoteModelsTabPanel().getEditButton().activate();
             view.getContentPane().getRemoteModelsTabPanel().getRemoveButton().activate();
         }
     }
 
     @Override
     public void update(Observable observable, Object arguments) {
         rebuildTableModel();
         invalidateButtons();
         refreshInfoLabel();
     }
 
     private void refreshInfoLabel() {
         view.getContentPane().getRemoteModelsTabPanel().getLocalInfoLabel().setText("Local settings: " + Application.global().getNetworkSettings());
     }
 }
