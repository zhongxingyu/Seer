 package org.bh.platform;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
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
 import javax.swing.JScrollPane;
 
 import org.bh.calculation.IShareholderValueCalculator;
 import org.bh.calculation.IStochasticProcess;
 import org.bh.controller.IPeriodController;
 import org.bh.controller.InputController;
 import org.bh.data.DTOKeyPair;
 import org.bh.data.DTOScenario;
 import org.bh.data.IDTO;
 import org.bh.data.types.Calculable;
 import org.bh.data.types.DistributionMap;
 import org.bh.data.types.StringValue;
 import org.bh.gui.ICompValueChangeListener;
 import org.bh.gui.IDeterministicResultAnalyser;
 import org.bh.gui.IStochasticResultAnalyser;
 import org.bh.gui.IViewListener;
 import org.bh.gui.ValidationMethods;
 import org.bh.gui.View;
 import org.bh.gui.ViewEvent;
 import org.bh.gui.ViewException;
 import org.bh.gui.swing.BHButton;
 import org.bh.gui.swing.BHCheckBox;
 import org.bh.gui.swing.BHComboBox;
 import org.bh.gui.swing.BHDescriptionLabel;
 import org.bh.gui.swing.BHMainFrame;
 import org.bh.gui.swing.BHScenarioForm;
 import org.bh.gui.swing.BHScenarioHeadForm;
 import org.bh.gui.swing.BHScenarioHeadIntervalForm;
 import org.bh.gui.swing.BHSelectionList;
 import org.bh.gui.swing.BHStochasticInputForm;
 import org.bh.gui.swing.BHTree;
 import org.bh.gui.swing.BHTreeNode;
 import org.bh.gui.swing.IBHModelComponent;
 import org.bh.gui.swing.BHComboBox.Item;
 import org.bh.platform.PlatformEvent.Type;
 
 public class ScenarioController extends InputController {
 	protected static final BHComboBox.Item[] DCF_METHOD_ITEMS = getDcfMethodItems();
 	protected static final BHComboBox.Item[] DCF_STOCHASTIC_METHOD_ITEMS = getStochasticDcfMethodItems();
 	protected static final BHComboBox.Item[] PERIOD_TYPE_ITEMS = getPeriodTypeItems();
 	protected static final BHComboBox.Item[] STOCHASTIC_METHOD_ITEMS = getStochasticProcessItems();
 	private IStochasticProcess process = null;
 	private final BHMainFrame bhmf;
 
 	public ScenarioController(View view, IDTO<?> model, final BHMainFrame bhmf) {
 		super(view, model);
 
 		this.bhmf = bhmf;
 		
 		//check whether stochastic or not
 		DTOScenario scenario = (DTOScenario) getModel();
 		BHComboBox cbDcfMethod = (BHComboBox) view
 		.getBHComponent(DTOScenario.Key.DCF_METHOD);
 		
 		if(scenario.isDeterministic()){
 			if (cbDcfMethod != null) {
 				cbDcfMethod.setSorted(true);
 				cbDcfMethod.setValueList(DCF_METHOD_ITEMS);
 			}
 		}else{
 			if (cbDcfMethod != null) {
 				cbDcfMethod.setSorted(true);
 				cbDcfMethod.setValueList(DCF_STOCHASTIC_METHOD_ITEMS);
 			}
 		}
 		
 
 		reloadTopPanel();
 
 		BHComboBox cbStochasticMethod = (BHComboBox) view
 				.getBHComponent(DTOScenario.Key.STOCHASTIC_PROCESS);
 		if (cbStochasticMethod != null) {
 			cbStochasticMethod.setSorted(true);
 			cbStochasticMethod.setValueList(STOCHASTIC_METHOD_ITEMS);
 		}
 
 		((BHButton) view.getBHComponent(PlatformKey.CALCSHAREHOLDERVALUE))
 				.addActionListener(new CalculationListener(PlatformController
 						.getInstance().getMainFrame().getBHTree()));
 
 		final BHButton calcStochasticParameters = ((BHButton) view
 				.getBHComponent(BHStochasticInputForm.Key.CALC_PARAMETERS));
 		final BHButton resetStochasticParameters = ((BHButton) view
 				.getBHComponent(BHStochasticInputForm.Key.RESET_PARAMETERS));
 		if (calcStochasticParameters != null
 				&& resetStochasticParameters != null) {
 			calcStochasticParameters.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					DTOScenario scenario = (DTOScenario) getModel();
 					process = scenario.getStochasticProcess()
 							.createNewInstance();
 					process.setScenario(scenario);
 
 					JPanel parametersPanel = process.calculateParameters();
 					BHStochasticInputForm form = (BHStochasticInputForm) ((Component) e
 							.getSource()).getParent();
 					form.setParametersPanel(parametersPanel);
 					if (parametersPanel instanceof Container)
 						Services.setFocus((Container) parametersPanel);
 					try {
 						final View view = new View(parametersPanel,
 								new ValidationMethods());
 						view.addViewListener(new IViewListener() {
 							@Override
 							public void viewEvent(ViewEvent e) {
 								switch (e.getEventType()) {
 								case VALUE_CHANGED:
 									boolean allValid = !view.revalidate()
 											.hasErrors();
 									((Component) getView().getBHComponent(
 											PlatformKey.CALCSHAREHOLDERVALUE))
 											.setEnabled(allValid);
 									break;
 								case VALIDATION_FAILED:
 									((Component) getView().getBHComponent(
 											PlatformKey.CALCSHAREHOLDERVALUE))
 											.setEnabled(false);
 									break;
 								}
 							}
 						});
 						boolean allValid = !view.revalidate().hasErrors();
 						((Component) getView().getBHComponent(
 								PlatformKey.CALCSHAREHOLDERVALUE))
 								.setEnabled(allValid);
 					} catch (ViewException e1) {
 					}
 
 					resetStochasticParameters.setVisible(true);
 					calcStochasticParameters.setVisible(false);
 					((Component) getView().getBHComponent(
 							DTOScenario.Key.STOCHASTIC_KEYS)).setEnabled(false);
 					((Component) getView().getBHComponent(
 							DTOScenario.Key.STOCHASTIC_PROCESS))
 							.setEnabled(false);
 				}
 			});
 
 			resetStochasticParameters.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					BHStochasticInputForm form = (BHStochasticInputForm) ((Component) e
 							.getSource()).getParent();
 					process = null;
 					form.removeParametersPanel();
 					calcStochasticParameters.setVisible(true);
 					resetStochasticParameters.setVisible(false);
 					((Component) getView().getBHComponent(
 							PlatformKey.CALCSHAREHOLDERVALUE))
 							.setEnabled(false);
 					((Component) getView().getBHComponent(
 							DTOScenario.Key.STOCHASTIC_KEYS)).setEnabled(true);
 					((Component) getView().getBHComponent(
 							DTOScenario.Key.STOCHASTIC_PROCESS))
 							.setEnabled(true);
 				}
 			});
 		}
 
 		BHCheckBox chkInterval = (BHCheckBox) getView().getBHComponent(
 				DTOScenario.Key.INTERVAL_ARITHMETIC);
 		if (chkInterval != null) {
 			final View v = view;
 			chkInterval.getValueChangeManager().addCompValueChangeListener(
 					new ICompValueChangeListener() {
 						@Override
 						public void compValueChanged(IBHModelComponent comp) {
 							saveToModel(comp);
 							DTOScenario scenario = (DTOScenario) getModel();
 							if (!scenario.isIntervalArithmetic()) {
 								scenario.convertIntervalToDouble();
 							}
 							// ---- get selected item of the old ComboBox
 							BHComboBox cbPeriodType = (BHComboBox) v
 							.getBHComponent(DTOScenario.Key.PERIOD_TYPE);
 							Object item = cbPeriodType.getSelectedItem();
 							// ----
 							((BHScenarioForm) getViewPanel())
 									.setHeadPanel(scenario
 											.isIntervalArithmetic());			
 							try {
 								reloadView();
 							} catch (ViewException e) {
 							}
 							loadAllToView();
 							reloadTopPanel();
 							
 							//---- set selected item in the new ComboBox
 							JPanel shf = ((BHScenarioForm) getViewPanel()).getScenarioHeadForm();
 							if(shf instanceof BHScenarioHeadForm){
 								((BHScenarioHeadForm)shf).getCmbPeriodType().setSelectedItem(item);
 							}else if (shf instanceof BHScenarioHeadIntervalForm){
 								((BHScenarioHeadIntervalForm)shf).getCmbPeriodType().setSelectedItem(item);
 							}
 							//----
 							
 							setCalcEnabled(getModel().isValid(true));
 						}
 					});
 		}
 
 		setCalcEnabled(getModel().isValid(true));
 		if (!((DTOScenario) model).isDeterministic()) {
 			((Component) view.getBHComponent(PlatformKey.CALCSHAREHOLDERVALUE))
 					.setEnabled(false);
 		}
 
 		loadToView(DTOScenario.Key.PERIOD_TYPE, false);
 		updateStochasticFieldsList();
 		loadAllToView();
 	}
 
 	private static Item[] getStochasticDcfMethodItems() {
 		Collection<IShareholderValueCalculator> dcfMethods = Services.getDCFMethods().values();
 		ArrayList<BHComboBox.Item> items = new ArrayList<BHComboBox.Item>();
 		for (IShareholderValueCalculator dcfMethod : dcfMethods) {
 			if(dcfMethod.isApplicableForStochastic())
 				items.add(new BHComboBox.Item(dcfMethod.getGuiKey(), new StringValue(dcfMethod.getUniqueId())));
 		}
 		return items.toArray(new BHComboBox.Item[0]);
 	}
 
 	protected void reloadTopPanel() {
 		final BHComboBox cbPeriodType = (BHComboBox) view
 				.getBHComponent(DTOScenario.Key.PERIOD_TYPE);
 		if (cbPeriodType != null) {
 			cbPeriodType.setValueList(PERIOD_TYPE_ITEMS);
 			cbPeriodType.getValueChangeManager().addCompValueChangeListener(
 					new ICompValueChangeListener() {
 
 						@Override
 						public void compValueChanged(IBHModelComponent comp) {
 							if (cbPeriodType.getSelectedIndex() == cbPeriodType
 									.getLastSelectedIndex()) {
 								return;
 							}
 
 							DTOScenario scenario = (DTOScenario) getModel();
 							if (scenario.getChildrenSize() > 0) {
 								cbPeriodType.hidePopup();
 								int action = PlatformUserDialog
 										.getInstance()
 										.showYesNoDialog(
 												Services
 														.getTranslator()
 														.translate(
 																"PstochasticPeriodTypeChangeText"),
 												Services
 														.getTranslator()
 														.translate(
 																"PstochasticPeriodTypeChangeHeader"));
 								if (action == JOptionPane.YES_OPTION) {
 									bhmf.getBHTree().removeAllPeriods(scenario);
 								} else if (action == JOptionPane.NO_OPTION) {
 									cbPeriodType.setSelectedIndex(cbPeriodType
 											.getLastSelectedIndex());
 									return;
 								}
 							}
 
 							updateStochasticFieldsList();
 						}
 					});
 		}
 	}
 
 	protected void updateStochasticFieldsList() {
 		DTOScenario scenario = (DTOScenario) getModel();
 		BHComboBox cbPeriodType = (BHComboBox) view
 				.getBHComponent(DTOScenario.Key.PERIOD_TYPE);
 		if (cbPeriodType != null && !scenario.isDeterministic()) {
 			BHSelectionList slStochasticKeys = (BHSelectionList) getView()
 					.getBHComponent(DTOScenario.Key.STOCHASTIC_KEYS);
 			BHDescriptionLabel lNoStochasticKeys = (BHDescriptionLabel) getView()
 					.getBHComponent(
 							BHStochasticInputForm.Key.NO_STOCHASTIC_KEYS);
 			List<DTOKeyPair> keyPair = Services.getPeriodController(
 					cbPeriodType.getValue().toString()).getStochasticKeys();
 			if (!keyPair.isEmpty()) {
 				slStochasticKeys.setModel(keyPair.toArray());
 				slStochasticKeys.getParent().getParent().setVisible(true);
 				lNoStochasticKeys.setVisible(false);
 			} else {
 				lNoStochasticKeys.setVisible(true);
 				slStochasticKeys.getParent().getParent().setVisible(false);
 			}
 		}
 	}
 
 	@Override
 	public void platformEvent(PlatformEvent e) {
 		super.platformEvent(e);
 		if (e.getEventType() == Type.DATA_CHANGED
 				&& getModel().isMeOrChild(e.getSource())) {
 			setCalcEnabled(getModel().isValid(true));
 		}
 	}
 
 	protected void setCalcEnabled(boolean calculationEnabled) {
 		if (((DTOScenario) getModel()).isDeterministic()) {
 			((Component) view.getBHComponent(PlatformKey.CALCSHAREHOLDERVALUE))
 					.setEnabled(calculationEnabled);
 			((Component) view
 					.getBHComponent(BHScenarioForm.Key.CANNOT_CALCULATE_HINT))
 					.setVisible(!calculationEnabled);
 		} else {
 			((Component) view
 					.getBHComponent(BHStochasticInputForm.Key.CALC_PARAMETERS))
 					.setEnabled(calculationEnabled);
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
 		Collection<IStochasticProcess> stochasticProcesses = Services
 				.getStochasticProcesses().values();
 		ArrayList<BHComboBox.Item> items = new ArrayList<BHComboBox.Item>();
 		for (IStochasticProcess stochasticProcess : stochasticProcesses) {
 			items.add(new BHComboBox.Item(stochasticProcess.getGuiKey(),
 					new StringValue(stochasticProcess.getUniqueId())));
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
 
 		private final ImageIcon scCalcLoading = Services.createImageIcon(
 				"/org/bh/images/loading.gif", null);
 		private BHTree bhTree;
 
 		public CalculationListener(BHTree bhTree) {
 			this.bhTree = bhTree;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			final JButton b = (JButton) e.getSource();
 			final BHDescriptionLabel calcImage = (BHDescriptionLabel) getView().getBHComponent(
 					BHScenarioForm.Key.CALCULATING_IMAGE);
 			
 			b.setEnabled(false);
 			calcImage.setIcon(this.scCalcLoading);
 
 			Runnable r = new Runnable() {
 				long end;
 				long start;
 				
 				@Override
 				public void run() {
 					DTOScenario scenario = (DTOScenario) getModel();
 					Component panel;
 					if (scenario.isDeterministic()) {
 						// start calculation
 						Map<String, Calculable[]> result = scenario
 								.getDCFMethod().calculate(scenario, true);
 
 						// FIXME selection of result analyser plugin
 						if(log.isDebugEnabled()) {
 							start =  System.currentTimeMillis();
 						}
 						panel = new JPanel();
 						for (IDeterministicResultAnalyser analyser : PluginManager
 								.getInstance().getServices(
 										IDeterministicResultAnalyser.class)) {
 							panel = analyser.setResult(scenario, result);
 							break;
 						}
 
 						BHTreeNode tn = (BHTreeNode) bhTree.getSelectionPath()
 								.getPathComponent(2);
 						
 						JScrollPane sp = new JScrollPane(panel);
 						sp.setWheelScrollingEnabled(true);
 						tn.setResultPane(sp);
 						
 						if(log.isDebugEnabled()) {
 							end = System.currentTimeMillis();
 							log.info("Result Analysis View load time: " +  (end - start) + "ms");
 						}
 					} else {
 						process.updateParameters();
 						DistributionMap result = process.calculate();
 
 						// FIXME selection of result analyser plugin
 						panel = new JPanel();
 						for (IStochasticResultAnalyser analyser : PluginManager
 								.getInstance().getServices(
 										IStochasticResultAnalyser.class)) {
 							panel = analyser.setResult(scenario, result);
 							break;
 						}
 						
 						
 					}
 					
 					
 					if(panel != null){
 						
 						BHTreeNode tn = (BHTreeNode) bhTree.getSelectionPath()
 						.getPathComponent(2);
 						JScrollPane sp = new JScrollPane(panel);
 						sp.setWheelScrollingEnabled(true);
 						sp.getVerticalScrollBar().setUnitIncrement(10);
 						tn.setResultPane(sp);
 						//Put it onto screen
 						bhmf.moveInResultForm(tn.getResultPane());
 						
 						
 					}
 					
 					b.setEnabled(true);
 					calcImage.setIcon(null);
 				}
 			};
 			new Thread(r, "Calculation Thread").start();
 		}
 	}
 }
