 package org.bh.platform;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JScrollPane;
 
 import org.apache.log4j.Logger;
 import org.bh.controller.InputController;
 import org.bh.data.DTOProject;
 import org.bh.data.DTOScenario;
 import org.bh.data.IDTO;
 import org.bh.data.types.Calculable;
 import org.bh.gui.View;
 import org.bh.gui.ViewException;
 import org.bh.gui.swing.BHButton;
import org.bh.gui.BHDashBoardPanelView;
 import org.bh.gui.swing.BHDescriptionLabel;
 import org.bh.gui.swing.BHProjectForm;
 import org.bh.gui.swing.BHTree;
 import org.bh.gui.swing.BHTreeNode;
 import org.bh.platform.PlatformEvent.Type;
 
 /**
  * This class helps to create the dashboard on projectlevel. There is the
  * CALCDASHBOARD Button, which starts the calculation of scenario's results.
  * 
  * @author Norman Weisenburger, Patrick Heinz
  * @version 1.0, 22.01.2010
  * 
  */
 public class ProjectController extends InputController implements
 		IPlatformListener {
 	/**
 	 * Logger for this class
 	 */
 	static final Logger log = Logger.getLogger(ProjectController.class);
 	
 	/**
 	 * The constructor for the distribution map.
 	 * 
 	 * @param view, IDTO<?> model
 	 */
 	public ProjectController(View view, IDTO<?> model) {
 		super(view, model);
 		Services.addPlatformListener(this);
 
 		((BHButton) view.getBHComponent(PlatformKey.CALCDASHBOARD))
 				.addActionListener(new CalculationListener(PlatformController
 						.getInstance().getMainFrame().getBHTree()));
 		
 		setCalcEnabled();
 	}
 
 	/**
 	 * Inner class calculating all shareholder values of a project's scenarios.
 	 */
 	protected class CalculationListener implements ActionListener {
 		private final ImageIcon prCalcLoading = Services.createImageIcon("/org/bh/images/loading.gif", null);
 
 		private BHTree bhTree;
 
 		public CalculationListener(BHTree bhTree) {
 			this.bhTree = bhTree;
 		}
 
 		public void actionPerformed(final ActionEvent ae) {
 			new Thread(new Runnable() {
 				public void run() {
 					if (log.isDebugEnabled()) {
 						log.debug("actionPerformed(ActionEvent)-start-");
 					}
 
 					try {
 						JButton b = (JButton) ae.getSource();
 						BHDescriptionLabel calcImage = (BHDescriptionLabel) getView().getBHComponent(
 								BHProjectForm.Key.CALCULATING_IMAGE);
 						
 						b.setEnabled(false);
 						calcImage.setIcon(prCalcLoading);
 						
 						DTOProject project = (DTOProject) getModel();
 						// Give Map to DashboardController
 						Map<DTOScenario, Map<?, ?>> results = new HashMap<DTOScenario, Map<?, ?>>();
 
 						for (DTOScenario scenario : project.getChildren()) {
 							// start calculation
 							if (scenario.isDeterministic()) {
 								Map<String, Calculable[]> resultsDCF = scenario
 										.getDCFMethod().calculate(scenario, true);
 
 								results.put(scenario, resultsDCF);
 
 //							Decommented, because dashboard does not support stochastic calculation so far.
 //							Use the code below, if you want to implement stochastic calculation for dashboard.
 
 //							} else {
 //								DistributionMap resultStochastic = scenario
 //										.getStochasticProcess().calculate();
 //
 //								results.put(scenario, resultStochastic);																						
 							}
 						}
 
 						View v = new BHDashBoardPanelView();
 						DashBoardController d = new DashBoardController(v);
 						d.setResult(results);
 
 						BHTreeNode tn = (BHTreeNode) bhTree.getSelectionPath()
 						.getPathComponent(1);
 
 						tn.setResultPane(new JScrollPane(v.getViewPanel()));
 						PlatformController.getInstance().getMainFrame().moveInResultForm(tn.getResultPane());
 						
 						calcImage.setIcon(null);
 						b.setEnabled(true);
 					} catch (ViewException e) {
 						log.error(e);
 						((JButton) ae.getSource()).setIcon(null);
 					}
 
 					if (log.isDebugEnabled()) {
 						log.debug("actionPerformed(ActionEvent)-end-");
 					}
 				}
 			}).start();
 		}
 	}
 
 	@Override
 	public void platformEvent(PlatformEvent e) {
 		super.platformEvent(e);
 		if (e.getEventType() == Type.DATA_CHANGED
 				&& getModel().isMeOrChild(e.getSource())) {
 			setCalcEnabled();
 		}
 	}
 
 	/**
 	 * This method enables the calculation button if there are more than
 	 * 2 valid scenarios to compare. Otherwise there will be a hint that
 	 * the calculation is not possible.
 	 */
 	protected void setCalcEnabled() {
 		DTOProject project = (DTOProject) getModel();
 		int counter = 0;
 		for (DTOScenario scenario : project.getChildren()) {
 			if (scenario.isValid(true)) {
 				counter++;
 			}
 		}
 		boolean calculationEnabled = (counter >= 2);
 
 		((Component) view.getBHComponent(PlatformKey.CALCDASHBOARD))
 				.setEnabled(calculationEnabled);
 		((Component) view.getBHComponent(BHProjectForm.Key.CANNOT_CALCULATE_HINT))
 				.setVisible(!calculationEnabled);
 	}
 }
