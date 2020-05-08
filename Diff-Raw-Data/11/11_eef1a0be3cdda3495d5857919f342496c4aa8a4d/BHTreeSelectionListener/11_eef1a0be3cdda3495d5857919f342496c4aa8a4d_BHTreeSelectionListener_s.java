 package org.bh.gui.swing.tree;
 
 import java.awt.Component;
 import java.awt.Container;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.SwingUtilities;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.TreePath;
 
 import org.apache.log4j.Logger;
 import org.bh.controller.IPeriodController;
 import org.bh.controller.InputController;
 import org.bh.data.DTO;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOProject;
 import org.bh.data.DTOScenario;
 import org.bh.data.IDTO;
 import org.bh.gui.swing.BHContent;
 import org.bh.gui.swing.BHMainFrame;
 import org.bh.gui.swing.comp.BHTextField;
 import org.bh.gui.swing.forms.BHDeterministicProcessForm;
 import org.bh.gui.swing.forms.BHPeriodForm;
 import org.bh.gui.swing.forms.BHProjectForm;
 import org.bh.gui.swing.forms.BHScenarioForm;
 import org.bh.gui.view.BHProjectView;
 import org.bh.gui.view.BHScenarioView;
 import org.bh.gui.view.View;
 import org.bh.gui.view.ViewException;
 import org.bh.platform.PlatformController;
 import org.bh.platform.ProjectController;
 import org.bh.platform.ScenarioController;
 import org.bh.platform.Services;
 import org.bh.validation.ValidationMethods;
 
 /**
  * 
  * The BHSelectionListnener handles a changed selection on the tree and starts
  * necessary actions for new selected node and connected elements (like screens
  * etc.)
  * 
  * <p>
  * When a node is selected the following actions have to be done:
  * 
  * 
  * @author Schmalzhaf.Alexander
  * @version 1.0, 04.01.2010
  * 
  */
 public class BHTreeSelectionListener implements TreeSelectionListener {
 
     protected static final Logger log = Logger.getLogger(BHTreeSelectionListener.class);
 
     private BHMainFrame bhmf;
     private PlatformController pc;
     private InputController controller;
 
     public BHTreeSelectionListener(PlatformController pc, BHMainFrame bhmf) {
 	this.pc = pc;
 	this.bhmf = bhmf;
     }
     
     /*
      * Getters and Setters for variables
      * -> allows use in swing thread called in valueChanged method (see below)
      */
     protected BHMainFrame getBHMainFrame(){
 	return bhmf;
     }
     
     protected InputController getController(){
 	return controller;
     }
     
     protected void setController(InputController ic){
 	this.controller = ic;
     }
     
     protected PlatformController getPlatformController(){
 	return pc;
     }
     
 
     /**
      * Handler for valueChanged action which is fired by new tree selection
      */
     @Override
     public void valueChanged(final TreeSelectionEvent tse) {
 	if (tse.getNewLeadSelectionPath() == null) {
 	    //no node is selected - background must be set to "empty" and result form must be cleared
 	    bhmf.setContentForm(new BHContent());
 	    bhmf.removeResultForm();
 
 	    // disable menu items
 	    bhmf.getBHMenuBar().disableMenuScenarioAllItems();
 	    bhmf.getBHMenuBar().disableMenuPeriodAllItems();
 
 	    // disable ToolBar buttons
 	    bhmf.getBHToolBar().disableScenarioButton();
 	    bhmf.getBHToolBar().disablePeriodButton();
 
 	} else {
 	    //--> TIDY UP
 	    
 	    // save divider location of split pane to rebuild current state 
 	    // at a reselect of "old" node
 	    BHTreeNode oldActiveNode;
 	    if (tse.getOldLeadSelectionPath() != null) {
 		TreePath tp = tse.getOldLeadSelectionPath();
 		oldActiveNode = (BHTreeNode) tp.getLastPathComponent();
 		oldActiveNode.setDividerLocation(bhmf.getVDividerLocation());
 	    }
 	    
	    // remove result panel
 	    bhmf.removeResultForm();
 	    
 	    //--> GET DATA AND FILL CONTENT AREA (RIGHT AREA)
 	    SwingUtilities.invokeLater(new Runnable() {
 		@Override
 		public void run() {
 		    if (tse.getPath().getLastPathComponent() instanceof BHTreeNode) {
 			BHTreeNode selectedNode = (BHTreeNode) tse.getPath().getLastPathComponent();
 			DTO<?> selectedDto = (DTO<?>) selectedNode.getUserObject();
 
 			if (selectedDto instanceof DTOProject) {
 
 			    // set menu items enabled oder disabled
 			    getBHMainFrame().getBHMenuBar().enableMenuProjectItems();
 			    getBHMainFrame().getBHMenuBar().disableMenuScenarioItems();
 			    getBHMainFrame().getBHMenuBar().disableMenuPeriodAllItems();
 
 			    // set ToolBar button disabled
 			    getBHMainFrame().getBHToolBar().disablePeriodButton();
 			    getBHMainFrame().getBHToolBar().enableScenarioButton();
 
 			    try {
 				View view = new BHProjectView(new BHProjectForm());
 
 				IDTO<?> model = selectedDto;
 				setController(new ProjectController(view, model));
 
 				selectedNode.setController(getController());
 
 				Component comp = getController().getViewPanel();
 
 				if (comp instanceof Container) {
 				    Container cont = (Container) comp;
 				    setFocus(cont);
 				}
 				// set content and result (that is dashboard in this case) if
 				// available
 				getBHMainFrame().setContentForm(comp);
 
 				JScrollPane resultPane;
 				if ((resultPane = selectedNode.getResultPane()) != null) {
 				    getBHMainFrame().setResultForm(resultPane);
 				    getBHMainFrame().setVDividerLocation(selectedNode.getDividerLocation());
 				} else {
 				    getBHMainFrame().removeResultForm();
 				}
 
 				getController().loadAllToView();
 
 			    } catch (ViewException e) {
 				e.printStackTrace();
 			    }
 
 			} else if (selectedDto instanceof DTOScenario) {
 
 			    // set menu items enabled oder disabled
 			    getBHMainFrame().getBHMenuBar().disableMenuProjectItems();
 			    getBHMainFrame().getBHMenuBar().enableMenuScenarioItems();
 			    getBHMainFrame().getBHMenuBar().disableMenuPeriodItems();
 
 			    // set ToolBar button enabled
 			    getBHMainFrame().getBHToolBar().enablePeriodButton();
 			    getBHMainFrame().getBHToolBar().enableScenarioButton();
 
 			    try {
 
 				DTOScenario model = (DTOScenario) selectedDto;
 
 				// check if controller is already there...
 				if (selectedNode.getController() == null) {
				    setController(selectedNode.getController());
 				    // if not, create view at first
 
 				    View view;
 				    // find out if stochastic process was chosen at init of strategy
 				    if (!model.isDeterministic()) {
 					view = new BHScenarioView(new BHScenarioForm(BHScenarioForm.Type.STOCHASTIC, model.isIntervalArithmetic()), new ValidationMethods());
 				    } else {
 					view = new BHScenarioView(new BHScenarioForm(BHScenarioForm.Type.DETERMINISTIC, model.isIntervalArithmetic()), new ValidationMethods());
 				    }
 
 				    // create controller
 				    setController(new ScenarioController(view, model, getBHMainFrame()));
 				    selectedNode.setController(getController());
 				}
 
 				if (model.isDeterministic()) {
 				    // if scenario is deterministic, an overview table is provided
 				    ((BHDeterministicProcessForm) ((BHScenarioForm) getController().getViewPanel()).getProcessForm()).setPeriodTable(getPlatformController().prepareScenarioTableData(model));
 				}
 
 				// set content and result panels to bhmf
 				JScrollPane resultPane;
 
 				Component comp = getController().getViewPanel();
 
 				if (comp instanceof Container) {
 				    Container cont = (Container) comp;
 				    setFocus(cont);
 				}
 
 				getBHMainFrame().setContentForm(comp);
 
 				if ((resultPane = selectedNode.getResultPane()) != null) {
 				    getBHMainFrame().setResultForm(resultPane);
 				    getBHMainFrame().setVDividerLocation(selectedNode.getDividerLocation());
 				} else {
 				    getBHMainFrame().removeResultForm();
 				}
 
 			    } catch (ViewException e) {
 				e.printStackTrace();
 			    }
 
 			} else if (selectedDto instanceof DTOPeriod) {
 
 			    // set menu items enabled oder disabled
 			    getBHMainFrame().getBHMenuBar().disableMenuProjectItems();
 			    getBHMainFrame().getBHMenuBar().disableMenuScenarioItems();
 			    getBHMainFrame().getBHMenuBar().enableMenuPeriodItems();
 
 			    // set ToolBar button enabled
 			    getBHMainFrame().getBHToolBar().enablePeriodButton();
 			    getBHMainFrame().getBHToolBar().enableScenarioButton();
 
 			    getBHMainFrame().removeResultForm();
 
 			    DTOPeriod period = (DTOPeriod) selectedDto;
 
 			    IPeriodController periodController = Services.getPeriodController(period.getScenario().get(DTOScenario.Key.PERIOD_TYPE).toString());
 			    Component viewComponent = periodController.editDTO(period);
 			    BHPeriodForm container = new BHPeriodForm();
 			    try {
 				View periodView = new View(container.getPperiod(), new ValidationMethods());
 				InputController controller = new InputController(periodView, period);
 				controller.loadAllToView();
 				// set Controller to tree
 				selectedNode.setController(controller);
 			    } catch (ViewException e) {
 				// should not happen
 				log.error("Cannot create period view", e);
 			    }
 
 			    if (viewComponent instanceof Container) {
 				Container cont = (Container) viewComponent;
 				setFocus(cont);
 			    }
 
 			    container.setPvalues((JPanel) viewComponent);
 			    getBHMainFrame().setContentForm(container);
 			}
 		    }
 
 		}
 	    });
 	}
     }
     
     
     
     public static boolean setFocus(Container cont) {
 	for (Component comp : cont.getComponents()) {
 	    if (comp instanceof BHTextField) {
 		final BHTextField tf = (BHTextField) comp;
 
 		SwingUtilities.invokeLater(new Runnable() {
 		    public void run() {
 			tf.requestFocus();
 			tf.selectAll();
 		    }
 		});
 		return true;
 	    } else if (comp instanceof Container) {
 		if (setFocus((Container) comp))
 		    return true;
 	    }
 
 	}
 	return false;
     }
 }
