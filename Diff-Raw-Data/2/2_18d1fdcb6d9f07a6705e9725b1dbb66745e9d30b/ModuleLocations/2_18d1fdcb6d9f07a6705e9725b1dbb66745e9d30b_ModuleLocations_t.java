 package mhcs.blaed;
 
 import java.util.ArrayList;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.PushButton;
 import mhcs.dan.Module;
 import mhcs.dan.ModuleList;
 
 
 /*
  * This is the map that shows the landing locations of all the modules
  * and allows the user to see details about each module.
  * @author Blaed J
  * @class sets up a UI that can be returned via a static method
  */
 public class ModuleLocations {
 
     private FlowPanel containerPanel;
     private Grid landingAreaGrid;
     //private DecoratorPanel decImage;
 
     public Panel createMainPanel(){
 	intitializeMembers();
 
	ArrayList<Module> moduleList = getModuleList();
 
 	for (int i = 0; i < landingAreaGrid.getRowCount(); i++) {
 	    for (int j = 0; j < landingAreaGrid.getColumnCount(); j++) {
 		landingAreaGrid.getCellFormatter().setStylePrimaryName(i, j, "tableCell");
 	    }
 	}
 	landingAreaGrid.setPixelSize(500, 250);
 
 	// actually plot the modules according to their location
 	// pass in the list of modules and the image to plot them on.
 	plotModuleLocations(moduleList, landingAreaGrid);
 
 	containerPanel.add(landingAreaGrid);
 	containerPanel.addStyleName("landingMap");
 
 	return containerPanel;
     }
 
     private void intitializeMembers() {
 	containerPanel = new FlowPanel();
 	landingAreaGrid = new Grid(50, 100);
     }
 
     private void plotModuleLocations( ArrayList<Module> moduleList, Grid mapGrid){
 	// make buttons for each object
 	// set the button's image according to the type of the module? or just a dot w/a number
 	// add a clickEventHandler to each button that calls a popup w/ details by module number.
 	// add each button to the grid based on coordinates.
 	Module module;
 	for (int i = 0; i < moduleList.size(); i++) {
 	    // make a new button
 	    module = moduleList.get(i);
 	    assert module != null;
 	    
 	    mapGrid.setWidget(Integer.parseInt(module.getXCoor()), Integer.parseInt(module.getYCoor()), createButton(module));
 	}
     }
 
     private PushButton createButton(final Module module) {
 
 	final PushButton moduleButton = new PushButton( module.getCode() );
 
 	moduleButton.setPixelSize(5, 5);
 
 	moduleButton.addClickHandler(new ClickHandler() {
 		@Override
 		public void onClick(ClickEvent event) {
 		    PopupPanel alertPanel = new PopupPanel();
 		    FlowPanel infoPanel = new FlowPanel();
 		    Label idLabel = new Label("Module ID:" + module.getCode());
 		    Label typeLabel = new Label("Module Type:" + module.getType());
 		    Label coordLabelX = new Label("X coordinate:" + module.getXCoor());
 		    Label coordLabelY = new Label("Y coordinate:" + module.getYCoor());
 		    moduleButton.addStyleName("moduleSelected");
 		    infoPanel.add(idLabel);
 		    infoPanel.add(typeLabel);
 		    infoPanel.add(coordLabelX);
 		    infoPanel.add(coordLabelY);
 		    infoPanel.addStyleName("largerFont");
 		    alertPanel.addCloseHandler(new CloseHandler<PopupPanel>() {
 			    @Override
 				public void onClose(CloseEvent<PopupPanel> event) {
 				moduleButton.setStyleName("moduleSelected", false);
 			    }
 			});
 		    alertPanel.add(infoPanel);
 		    alertPanel.setModal(false);
 		    alertPanel.setAutoHideEnabled(true);
 		    alertPanel.center();
 		}
 	    });
 
 	return moduleButton;
     }
 
     private ArrayList<Module> getModuleList() {
 	/**
 	 * TODO method stub, finish this when module class is figured out.
 	 */
 	//	ModuleList list = new ModuleList();
 	//list[0] = new HabitatModule(9, 3, 20);
 
 	//assert list[0] != null;
 	// HabitatModule(x, y, code)
 	ModuleList.addModule(new Module("21", "fine", "0", "0", "0"));
 	ModuleList.addModule(new Module("22", "fine", "0", "1", "0"));
 	ModuleList.addModule(new Module("23", "fine", "10", "13", "0"));
 	ModuleList.addModule(new Module("24", "fine", "8", "2", "0"));
 	ModuleList.addModule(new Module("25", "fine", "42", "17", "0"));
 	return ModuleList.moduleList;
     }
 }
