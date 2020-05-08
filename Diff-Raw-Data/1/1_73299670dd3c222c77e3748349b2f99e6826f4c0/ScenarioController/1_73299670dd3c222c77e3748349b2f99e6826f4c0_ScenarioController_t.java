 package org.bh.platform;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 
import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 
 import org.bh.calculation.IShareholderValueCalculator;
 import org.bh.calculation.IStochasticProcess;
 import org.bh.controller.IPeriodController;
 import org.bh.controller.InputController;
 import org.bh.data.DTOKeyPair;
 import org.bh.data.DTOScenario;
 import org.bh.data.IDTO;
 import org.bh.data.types.Calculable;
 import org.bh.data.types.StringValue;
 import org.bh.gui.IDeterministicResultAnalyser;
 import org.bh.gui.View;
 import org.bh.gui.swing.BHButton;
 import org.bh.gui.swing.BHComboBox;
 import org.bh.gui.swing.BHScenarioForm;
 import org.bh.gui.swing.BHSelectionList;
 import org.bh.gui.swing.BHTree;
 import org.bh.gui.swing.BHTreeNode;
 import org.bh.platform.PlatformEvent.Type;
 
 public class ScenarioController extends InputController {
 	protected static final BHComboBox.Item[] DCF_METHOD_ITEMS = getDcfMethodItems();
 	protected static final BHComboBox.Item[] PERIOD_TYPE_ITEMS = getPeriodTypeItems();
 	protected static final BHComboBox.Item[] STOCHASTIC_METHOD_ITEMS = getStochasticProcessItems();
 
 	public ScenarioController(View view, IDTO<?> model) {
 		super(view, model);
 
 		BHComboBox cbDcfMethod = (BHComboBox) view
 				.getBHComponent(DTOScenario.Key.DCF_METHOD);
 		if (cbDcfMethod != null) {
 			cbDcfMethod.setSorted(true);
 			cbDcfMethod.setValueList(DCF_METHOD_ITEMS);
 		}
 
 		final BHComboBox cbPeriodType = (BHComboBox) view
 				.getBHComponent(DTOScenario.Key.PERIOD_TYPE);
 		if (cbPeriodType != null) {
 			cbPeriodType.setValueList(PERIOD_TYPE_ITEMS);
 			cbPeriodType.addItemListener(new ItemListener() {
 			
 				@Override
 				public void itemStateChanged(ItemEvent e) {
 					
 					if(!((DTOScenario)getModel()).isDeterministic()){
 						if(((DTOScenario)getModel()).getChildrenSize() != 0){
 							int action = JOptionPane.showConfirmDialog(getView().getViewPanel(),"Test","Test", JOptionPane.YES_NO_OPTION);
 							
 							if (action == JOptionPane.YES_OPTION) {
 								
 								((DTOScenario)getModel()).removeAllChildren();
 								BHSelectionList slStochasticKeys = (BHSelectionList)getView().getBHComponent(DTOScenario.Key.STOCHASTIC_KEYS);
 								List<DTOKeyPair> keyPair = Services.getPeriodController(cbPeriodType.getValue().toString()).getStochasticKeys();
 								slStochasticKeys.setModel(keyPair.toArray());
 								
 							} else if (action == JOptionPane.NO_OPTION) {}
 						}else{
 							BHSelectionList slStochasticKeys = (BHSelectionList)getView().getBHComponent(DTOScenario.Key.STOCHASTIC_KEYS);
 							List<DTOKeyPair> keyPair = Services.getPeriodController(cbPeriodType.getValue().toString()).getStochasticKeys();
 							slStochasticKeys.setModel(keyPair.toArray());
 						}
 					}
 				}
 			});
 		}
 		
 		BHComboBox cbStochasticMethod = (BHComboBox) view.getBHComponent(DTOScenario.Key.STOCHASTIC_PROCESS);
 		if (cbStochasticMethod != null) {
 			cbStochasticMethod.setSorted(true);
 			cbStochasticMethod.setValueList(STOCHASTIC_METHOD_ITEMS);
 		}
 
 		((BHButton) view.getBHComponent(PlatformKey.CALCSHAREHOLDERVALUE))
 				.addActionListener(new CalculationListener(PlatformController.getInstance().getMainFrame().getBHTree()));
 	}
 
 	@Override
 	public void platformEvent(PlatformEvent e) {
 		super.platformEvent(e);
 		if (e.getEventType() == Type.DATA_CHANGED
 				&& e.getSource() == getModel()) {
 			boolean calculationEnabled = getModel().isValid(true);
 			((Component) view.getBHComponent(PlatformKey.CALCSHAREHOLDERVALUE))
 					.setEnabled(calculationEnabled);
 			((Component) view
 					.getBHComponent(BHScenarioForm.Key.CANNOT_CALCULATE_HINT))
 					.setVisible(!calculationEnabled);
 		}
 	}
 
 	protected static BHComboBox.Item[] getDcfMethodItems() {
 		Collection<IShareholderValueCalculator> dcfMethods = Services
 				.getDCFMethods().values();
 		ArrayList<BHComboBox.Item> items = new ArrayList<BHComboBox.Item>();
 		for (IShareholderValueCalculator dcfMethod : dcfMethods) {
 			items.add(new BHComboBox.Item(dcfMethod.getGuiKey(),
 					new StringValue(dcfMethod.getUniqueId())));
 		}
 		return items.toArray(new BHComboBox.Item[0]);
 	}
 	
 	protected static BHComboBox.Item[] getStochasticProcessItems() {
 		Collection<IStochasticProcess> stochasticProcesses = Services.getStochasticProcesses().values();
 		ArrayList<BHComboBox.Item> items = new ArrayList<BHComboBox.Item>();
 		for (IStochasticProcess stochasticProcess : stochasticProcesses) {
 			items.add(new BHComboBox.Item(stochasticProcess.getGuiKey(), new StringValue(stochasticProcess.getUniqueId())));
 		}
 		return items.toArray(new BHComboBox.Item[0]);
 		
 	}
 
 	protected static BHComboBox.Item[] getPeriodTypeItems() {
 		IPeriodController[] periodTypes = Services.getPeriodControllers()
 				.values().toArray(new IPeriodController[0]);
 		// sort by priority
 		Arrays.sort(periodTypes, new Comparator<IPeriodController>() {
 			@Override
 			public int compare(IPeriodController o1, IPeriodController o2) {
 				return o2.getGuiPriority() - o1.getGuiPriority();
 			}
 		});
 		ArrayList<BHComboBox.Item> items = new ArrayList<BHComboBox.Item>();
 		for (IPeriodController periodType : periodTypes) {
 			items.add(new BHComboBox.Item(periodType.getGuiKey(),
 					new StringValue(periodType.getGuiKey())));
 		}
 		return items.toArray(new BHComboBox.Item[0]);
 	}
 
 	protected class CalculationListener implements ActionListener {
 		
 		private final ImageIcon LOADING = Services.createImageIcon("/org/bh/images/loading.gif", null);
 		
 		private BHTree bhTree;
 		
 		public CalculationListener(BHTree bhTree){
 			this.bhTree = bhTree;
 		}
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			final JButton b = (JButton) e.getSource();
 			b.setIcon(this.LOADING);
 			
 			Runnable r = new Runnable() {
 				@Override
 				public void run() {
 					DTOScenario scenario = (DTOScenario) getModel();
 					// start calculation
 					Map<String, Calculable[]> result = scenario.getDCFMethod()
 							.calculate(scenario);
 
 					// TODO maybe setResult should return the panel of the view
 					JPanel panel = new JPanel();
 
 					// FIXME selection of result analyser plugin
 					for (IDeterministicResultAnalyser analyser : PluginManager
 							.getInstance().getServices(
 									IDeterministicResultAnalyser.class)) {
 						analyser.setResult(scenario, result, panel);
 						break;
 					}
 					JSplitPane crForm = Services.createContentResultForm(panel);
 					BHTreeNode tn = (BHTreeNode)bhTree.getSelectionPath().getPathComponent(2);
 					
 					tn.setBackgroundPane(crForm);
 					
 					b.setIcon(null);
 				}
 			};
 			new Thread(r, "Calculation Thread").start();
 		}
 	}
 }
