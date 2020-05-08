 package mhcs.blaed;
 
 import java.util.ArrayList;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.PushButton;
 import mhcs.dan.Module;
 import mhcs.dan.ModuleList;
 
 
 /**
  * This is the map that shows the landing locations of all the modules
  * and allows the user to see details about each module.
  * @author Blaed J
  * @class creates a widget that shows the locations of all the modules landed
  */
 public class ModuleLocations {
 
     // the main panel for the module locations panel.
 	transient private FlowPanel containerPanel;
     
     transient private Grid landingAreaGrid;
 
     /**
      * The main application framework
      * @return the main interface panel
      */
     public Panel createMainPanel(){
 	intitializeMembers();
 
 	final ArrayList<Module> moduleList = getModuleList();
 	
 	// add a style to each cell in the table
 	for (int i = 0; i < landingAreaGrid.getRowCount(); i++) {
 	    for (int j = 0; j < landingAreaGrid.getColumnCount(); j++) {
 		landingAreaGrid.getCellFormatter().setStylePrimaryName(i, j, "tableCell");
 	    }
 	}
 	landingAreaGrid.setPixelSize(500, 250);
 
 	plotModuleLocations(moduleList, landingAreaGrid);
 
 	Button clearButton = new Button("Clear the map");
 	Button refreshButton = new Button("Refresh the map");
 
 	clearButton.addClickHandler(new ClickHandler() {
 		@Override
 		public void onClick(final ClickEvent event) {
 		    landingAreaGrid.clear(true);
 		}
 	    });
 
 	refreshButton.addClickHandler(new ClickHandler() {
 
 		@Override
 		public void onClick(final ClickEvent event) {
		    plotModuleLocations(ModuleList.moduleList, landingAreaGrid);
 		}
 	    });
 	// add the widgets
 	containerPanel.add(landingAreaGrid);
 	containerPanel.addStyleName("landingMap");
 	containerPanel.add(refreshButton);
 	containerPanel.add(clearButton);
 	return containerPanel;
     }
 
     /**
      * initalize necessary members
      */
     private void intitializeMembers() {
 	containerPanel = new FlowPanel();
 	landingAreaGrid = new Grid(50, 100);
     }
 
     /**
      * Clears the map of all module icons and then plots the locations of all the
      * modules in the module list.
      * @param moduleList the module to plot on the map
      * @param mapGrid the map to plot the modules from moduleList on
      * @post all the modules in the module list are plotted onto the map
      */
     private void plotModuleLocations( final ArrayList<Module> moduleList, final Grid mapGrid){
 	mapGrid.clear(true);
    	Module module;
 	for (int i = 0; i < moduleList.size(); i++) {
 	    module = moduleList.get(i);
 	    assert module != null;
 	    mapGrid.setWidget(50 - Integer.parseInt(module.getYCoor()), Integer.parseInt(module.getXCoor()) - 1, createModuleRepresenter(module));
 	}
     }
 
     /**
      * creates a button to represent a module on a map.
      * @param module the module to represent with a button/icon
      * @return the button representing the module specified
      */
     private PushButton createModuleRepresenter(final Module module) {
 
     	final PushButton moduleButton = new PushButton( module.getCode() );
     	moduleButton.setPixelSize(5, 5);
     	moduleButton.addClickHandler(new ClickHandler() {
     		
     		@Override
     		public void onClick(final ClickEvent event) {
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
     				public void onClose(final CloseEvent<PopupPanel> event) {
 				moduleButton.setStyleName("moduleSelected", false);
 			    }
     			});
 
 		    switch(module.getType()){
 		    case PLAIN:
 			infoPanel.add(new Image("images/plain.png"));
 			break;
 		    case  DORMITORY:
 			infoPanel.add(new Image("images/dorm.jpg"));
 			break;
 		    case SANITATION:
 			infoPanel.add(new Image("images/sanitation.jpg"));
 			break;
 		    case FOOD_AND_WATER:
 			infoPanel.add(new Image("images/storage.jpg"));
 			break;
 		    case GYM_AND_RELAXATION:
 			infoPanel.add(new Image("images/gym.png"));
 			break;
 		    case CANTEEN:
 			infoPanel.add(new Image("images/canteen.png"));
 			break;
 		    case POWER:
 			infoPanel.add(new Image("images/power.jpg"));
 			break;
 		    case CONTROL:
 			infoPanel.add(new Image("images/control.jpg"));
 			break;
 		    case AIRLOCK:
 			infoPanel.add(new Image("images/airlock.jpg"));
 			break;
 		    case MEDICAL:
 			infoPanel.add(new Image("images/medical.png"));
 			break;
 		    default:
 
 		    }
 		    alertPanel.add(infoPanel);
 		    alertPanel.setModal(false);
 		    alertPanel.setAutoHideEnabled(true);
 		    alertPanel.center();
     		}
 	    });
     	return moduleButton;
     }
 
     /**
      * Retrieve the current list of modules entered into the system.
      * @return the list of modules that have landed.
      */
     private ArrayList<Module> getModuleList() {
    	return ModuleList.moduleList;
     }
 }
