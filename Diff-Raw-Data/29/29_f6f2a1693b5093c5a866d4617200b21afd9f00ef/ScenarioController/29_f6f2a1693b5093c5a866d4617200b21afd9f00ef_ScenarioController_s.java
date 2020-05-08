 package org.bh.platform;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 import javax.swing.JPanel;
 
 import org.bh.calculation.IShareholderValueCalculator;
 import org.bh.controller.InputController;
 import org.bh.data.DTOScenario;
 import org.bh.data.IDTO;
 import org.bh.data.types.Calculable;
 import org.bh.data.types.StringValue;
 import org.bh.gui.IDeterministicResultAnalyser;
 import org.bh.gui.View;
 import org.bh.gui.swing.BHButton;
 import org.bh.gui.swing.BHComboBox;
 import org.bh.gui.swing.BHScenarioForm;
 import org.bh.platform.PlatformEvent.Type;
 
 public class ScenarioController extends InputController {
 
 	public ScenarioController(View view, IDTO<?> model) {
 		super(view, model);
 
 		BHComboBox cbDcfMethod = (BHComboBox) view
 				.getBHComponent(DTOScenario.Key.DCF_METHOD);
 		if (cbDcfMethod != null) {
 			Collection<IShareholderValueCalculator> dcfMethods = Services
 					.getDCFMethods().values();
 			ArrayList<BHComboBox.Item> items = new ArrayList<BHComboBox.Item>();
 			for (IShareholderValueCalculator dcfMethod : dcfMethods) {
 				items.add(new BHComboBox.Item(dcfMethod.getGuiKey(),
 						new StringValue(dcfMethod.getUniqueId())));
 			}
 			cbDcfMethod.setSorted(true);
 			cbDcfMethod.setValueList(items.toArray(new BHComboBox.Item[0]));
 		}
 
 		((BHButton) view.getBHComponent(PlatformKey.CALCSHAREHOLDERVALUE))
 				.addActionListener(new CalculationListener());
 	}
 	
 	@Override
 	public void platformEvent(PlatformEvent e) {
 		super.platformEvent(e);
 		if (e.getEventType() == Type.DATA_CHANGED && e.getSource() == getModel()) {
 			boolean calculationEnabled = getModel().isValid(true);
 			((Component)view.getBHComponent(PlatformKey.CALCSHAREHOLDERVALUE)).setEnabled(calculationEnabled);
 			((Component)view.getBHComponent(BHScenarioForm.Key.CANNOT_CALCULATE_HINT)).setVisible(!calculationEnabled);
 		}
 	}
 
 	protected class CalculationListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			
 			Runnable r = new Runnable() {
 				@Override
 				public void run() {
 					DTOScenario scenario = (DTOScenario) getModel();
 					// start calculation
 					Map<String, Calculable[]> result = scenario.getDCFMethod()
 							.calculate(scenario);
 
 					// TODO maybe setResult should return the panel of the view
 					JPanel panel = new JPanel();
					Services.setCharts(panel);
 					// FIXME selection of result analyser plugin
 					for (IDeterministicResultAnalyser analyser : PluginManager
 							.getInstance().getServices(
 									IDeterministicResultAnalyser.class)) {
 						analyser.setResult(scenario, result, panel);
 						break;
 					}
 				}	
 			};
 			new Thread(r, "Calculation Thread").start();
 		}
 	}
 }
