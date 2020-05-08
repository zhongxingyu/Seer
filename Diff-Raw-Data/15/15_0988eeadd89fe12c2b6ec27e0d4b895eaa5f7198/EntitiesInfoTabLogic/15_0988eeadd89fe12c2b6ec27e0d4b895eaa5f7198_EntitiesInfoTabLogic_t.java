 package jDistsim.application.designer.controller.tabLogic;
 
 import jDistsim.application.designer.common.Application;
 import jDistsim.application.designer.controller.InformationController;
 import jDistsim.application.designer.model.ModelSpaceModel;
 import jDistsim.application.designer.view.InformationView;
import jDistsim.core.simulation.distributed.DistributedReceiveModule;
 import jDistsim.core.simulation.modules.Module;
 import jDistsim.core.simulation.modules.RootModule;
 import jDistsim.core.simulation.modules.ui.ModuleUI;
 import jDistsim.utils.logging.Logger;
 import jDistsim.utils.pattern.observer.IObserver;
 import jDistsim.utils.pattern.observer.Observable;
 
 import javax.swing.table.DefaultTableModel;
 import java.util.Collection;
 import java.util.Vector;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 2.3.13
  * Time: 14:40
  */
 public class EntitiesInfoTabLogic implements IObserver {
 
     private InformationController controller;
 
     public EntitiesInfoTabLogic(InformationController controller) {
         this.controller = controller;
         controller.getMainFrame().getModel(ModelSpaceModel.class).getModuleList().addObserver(this);
         rebuildTableModel();
     }
 
     private void rebuildTableModel() {
         Collection<ModuleUI> modules = controller.getMainFrame().getModel(ModelSpaceModel.class).getModuleList().values();
         Vector<String> columns = makeColumns();
         Vector<Vector<String>> rows = new Vector<>();
 
         for (ModuleUI moduleUI : modules) {
             Module module = moduleUI.getModule();
             if (module instanceof RootModule) {
                 RootModule rootModule = (RootModule) module;
                 rows.addElement(makeEntityRow(rootModule));
             }
            if (module instanceof DistributedReceiveModule) {
                DistributedReceiveModule distributedModule = (DistributedReceiveModule) module;
                 rows.addElement(makeDistributedEntityRow(distributedModule));
             }
         }
 
         controller.getModel().getEntitiesInfoTable().setModel(new DefaultTableModel(rows, columns));
         InformationView view = controller.getMainFrame().getView(InformationView.class);
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
 
    private Vector<String> makeDistributedEntityRow(DistributedReceiveModule distributedReceiveModule) {
         Vector<String> row = new Vector<>();
        row.addElement(distributedReceiveModule.getAuthorizedEntityName());
        row.addElement(distributedReceiveModule.getIdentifier());
         row.addElement("-");
         row.addElement("true");
         row.addElement("-");
         row.addElement("-");
         row.addElement("-");
         row.addElement("-");
         row.addElement("inherit");
         return row;
     }
 
     @Override
     public void update(Observable observable, Object arguments) {
         rebuildTableModel();
     }
 }
