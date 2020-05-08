 package jDistsim.application.designer.controller;
 
 import jDistsim.application.designer.MainFrame;
 import jDistsim.application.designer.common.Application;
 import jDistsim.application.designer.model.InformationModel;
 import jDistsim.application.designer.model.ModelSpaceModel;
 import jDistsim.application.designer.view.InformationView;
 import jDistsim.core.simulation.modules.Module;
 import jDistsim.core.simulation.modules.ui.ModuleUI;
 import jDistsim.core.simulation.modules.RootModule;
 import jDistsim.core.simulation.simulator.Writer;
 import jDistsim.ui.control.button.ImageButton;
 import jDistsim.ui.panel.listener.LogTabListener;
 import jDistsim.ui.panel.listener.OutputTabListener;
 import jDistsim.ui.panel.tab.LogTabPanel;
 import jDistsim.utils.logging.Logger;
 import jDistsim.utils.pattern.mvc.AbstractController;
 import jDistsim.utils.pattern.observer.IObserver;
 import jDistsim.utils.pattern.observer.Observable;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import java.awt.*;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.MouseEvent;
 import java.util.Collection;
 import java.util.Vector;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 15.2.13
  * Time: 17:58
  */
 public class InformationController extends AbstractController<InformationModel> {
 
     private InformationView view;
     private LogTabListener logTabLogic;
     private OutputTabLogic outputTabLogic;
     private EntitiesInfoTabLogic entitiesInfoTabLogic;
 
     public InformationController(MainFrame mainFrame, InformationModel model) {
         super(mainFrame, model);
         view = getMainFrame().getView(InformationView.class);
 
         initialize();
     }
 
     private void initialize() {
         logTabLogic = new LogTabLogic();
         outputTabLogic = new OutputTabLogic();
         entitiesInfoTabLogic = new EntitiesInfoTabLogic();
 
         view.setLogTabListener(logTabLogic);
         view.setOutputListener(outputTabLogic);
     }
 
     public Writer makeSimulatorWriter() {
         return outputTabLogic;
     }
 
     private class LogTabLogic implements LogTabListener {
 
         private LogTabPanel logTabPanel;
 
         public LogTabLogic() {
             logTabPanel = view.getContentPane().getLogTabPanel();
 
             if (getModel().isLogPanelScrollToEnd())
                 logTabPanel.getControlPanel().getScrollToEndButton().setActive(true);
         }
 
 
         @Override
         public void onTrashButtonClick(Object sender, MouseEvent mouseEvent) {
             logTabPanel.getTextAreaPanel().getTextArea().setText("");
             Logger.log("Clear application log output textarea");
         }
 
         @Override
         public void onWordWrapButtonClick(Object sender, MouseEvent mouseEvent) {
             ImageButton imageButton = logTabPanel.getControlPanel().getWordWrapButton();
             imageButton.setActive(!imageButton.isActive());
             logTabPanel.getTextAreaPanel().setWordWrap(imageButton.isActive());
             getModel().setLogPanelWordWrap(imageButton.isActive());
 
             Logger.log("Set word wrapping on application textarea: " + imageButton.isActive());
         }
 
         @Override
         public void onScrollToEndButtonClick(Object sender, MouseEvent mouseEvent) {
             ImageButton imageButton = logTabPanel.getControlPanel().getScrollToEndButton();
             imageButton.setActive(!imageButton.isActive());
             logTabPanel.getTextAreaPanel().setAutoCaretPosition(imageButton.isActive());
             getModel().setLogPanelScrollToEnd(imageButton.isActive());
 
             Logger.log("Set scroll to end on application textarea: " + imageButton.isActive());
         }
 
         @Override
         public void onCopyToClipboardButtonClick(Object sender, MouseEvent mouseEvent) {
             String logText = logTabPanel.getTextAreaPanel().getTextArea().getText();
             StringSelection selection = new StringSelection(logText);
             Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
             clipboard.setContents(selection, selection);
 
             Logger.log("Log from textarea copy to clipboard");
         }
     }
 
     private class OutputTabLogic implements OutputTabListener, Writer {
 
         @Override
         public void write(String text) {
             JTextArea textArea = getModel().getOutputPanelTextArea();
             textArea.append(text + "\n");
 
             if (!text.contains("-----"))
                 textArea.setCaretPosition(textArea.getDocument().getLength());
 
         }
 
         @Override
         public void clear() {
             getModel().getOutputPanelTextArea().setText("");
         }
     }
 
     private class EntitiesInfoTabLogic implements IObserver {
 
         public EntitiesInfoTabLogic() {
             getMainFrame().getModel(ModelSpaceModel.class).getModuleList().addObserver(this);
             rebuildTableModel();
         }
 
         private void rebuildTableModel() {
             Collection<ModuleUI> modules = getMainFrame().getModel(ModelSpaceModel.class).getModuleList().values();
             Vector<String> columns = makeColumns();
             Vector<Vector<String>> rows = new Vector<>();
 
             for (ModuleUI moduleUI : modules) {
                 Module module = moduleUI.getModule();
                 if (module instanceof RootModule) {
                     RootModule rootModule = (RootModule) module;
                     rows.addElement(makeEntityRow(rootModule));
                 }
             }
 
             getModel().getEntitiesInfoTable().setModel(new DefaultTableModel(rows, columns));
             view.renderEntitiesTable();
             Logger.log("Rebuild entities info table");
         }
 
         private Vector<String> makeColumns() {
             Vector<String> columnNames = new Vector<>();
             columnNames.addElement("Name");
             columnNames.addElement("Originator");
             columnNames.addElement("Owner");
             columnNames.addElement("Distributed");
             columnNames.addElement("Per interval");
             columnNames.addElement("First creation");
             columnNames.addElement("Max arrivals");
             columnNames.addElement("Between arrivals");
             columnNames.addElement("Icon");
             return columnNames;
         }
 
         private Vector<String> makeEntityRow(RootModule rootModule) {
             Vector<String> row = new Vector<>();
             row.addElement(rootModule.getBaseEntityName());
             row.addElement(rootModule.getIdentifier());
             row.addElement(Application.global().getModelName());
             row.addElement("false");
             row.addElement(String.valueOf(rootModule.getEntityPerInterval()));
             row.addElement(String.valueOf(rootModule.getFirsCreation()));
             row.addElement(String.valueOf(rootModule.getMaxArrivals()));
             row.addElement(rootModule.getArrivalsType() + "(" + rootModule.getArrivalsTypeValue() + ")");
             row.addElement(rootModule.getIconName());
             return row;
         }
 
         @Override
         public void update(Observable observable, Object arguments) {
             rebuildTableModel();
         }
     }
 }
