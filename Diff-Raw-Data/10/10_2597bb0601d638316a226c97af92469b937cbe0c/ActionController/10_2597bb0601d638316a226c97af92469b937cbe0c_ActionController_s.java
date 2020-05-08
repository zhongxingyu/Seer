 /**
  * Projekt ANNtool 
  *
  * Copyright (c) 2011 github.com/timaschew/jANN
  * 
  * anton
  */
 package de.unikassel.ann.controller;
 
 import java.beans.PropertyChangeEvent;
 import java.util.List;
 
 import de.unikassel.ann.config.NetConfig;
 import de.unikassel.ann.factory.NetworkFactory;
 import de.unikassel.ann.gui.Main;
 import de.unikassel.ann.gui.graph.GraphLayoutViewer;
 import de.unikassel.ann.gui.sidebar.Sidebar;
 import de.unikassel.ann.model.Layer;
 import de.unikassel.ann.model.SidebarModel;
 import de.unikassel.ann.model.func.ActivationFunction;
 import de.unikassel.ann.strategy.MaxLearnIterationsStrategy;
 import de.unikassel.ann.strategy.MinErrorStrategy;
 import de.unikassel.ann.strategy.RestartErrorStrategy;
 import de.unikassel.ann.strategy.RestartImprovementStrategy;
 import de.unikassel.ann.strategy.Strategy;
 import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
 
 /**
  * @author anton
  * 
  */
 public class ActionController {
 
 	private static ActionController instance;
 
 	private ActionController() {
 	}
 
 	public static ActionController get() {
 		if (instance == null) {
 			instance = new ActionController();
 		}
 		return instance;
 	}
 
 	public void doAction(final Actions a, final PropertyChangeEvent evt) {
 
 		// System.out.println(a);
 		// System.out.println(evt.getOldValue());
 		// System.out.println(evt.getNewValue());
 
 		Sidebar sidebar = Main.instance.sidebar;
 		SidebarModel sidebarModel = Settings.getInstance().getCurrentSession().sidebarModel;
 		switch (a) {
 
 		case UPDATE_SIDEBAR_CONFIG_INPUT_NEURON_MODEL:
 			Integer newValue = (Integer) evt.getNewValue();
 			sidebarModel.setInputNeurons(newValue);
 			break;
 
 		case UPDATE_SIDEBAR_CONFIG_HIDDEN_NEURON_MODEL:
			Integer selectedHiddenLayer = sidebarModel.getSelectedGlobalHiddenLayerIndex();
 			sidebarModel.setHiddenNeurons(selectedHiddenLayer, (Integer) evt.getNewValue());
 			break;
 
 		case UPDATE_SIDEBAR_CONFIG_HIDDEN_LAYER_MODEL:
 			newValue = (Integer) evt.getNewValue();
 			sidebarModel.setHiddenLayers(newValue);
 			break;
 
 		case UPDATE_SIDEBAR_CONFIG_OUTPUT_NEURON_MODEL:
 			newValue = (Integer) evt.getNewValue();
 			sidebarModel.setOutputNeurons(newValue);
 			break;
 
 		case UPDATE_SIDEBAR_CONFIG_INPUT_BIAS_MODEL:
 			Boolean bValue = (Boolean) evt.getNewValue();
 			sidebarModel.setInputBias(bValue);
 			break;
 
 		case UPDATE_SIDEBAR_CONFIG_HIDDEN_BIAS_MODEL:
 			bValue = (Boolean) evt.getNewValue();
 			Integer selectedLayer = (Integer) sidebar.topolgyPanel.hiddenLayerDropDown.getSelectedItem();
 			// use relative (hidden) layer index
 			sidebarModel.setHiddenBias(selectedLayer - 1, bValue);
 			break;
 
 		case UPDATE_JUNG_GRAPH:
 			newValue = (Integer) evt.getNewValue();
 			Integer oldValue = (Integer) evt.getOldValue();
 
 			selectedHiddenLayer = sidebarModel.getSelectedGlobalHiddenLayerIndex();
 			// sidebarConfig.
 			Integer hiddenLayerSize = sidebarModel.getHiddenLayers();
 			LayerController<Layer> layerController = LayerController.getInstance();
 
 			String propertyName = evt.getPropertyName();
 			//
 			// Remove
 			//
 			if ((Integer) evt.getOldValue() > newValue) {
 				// INPUT NEURON
 				if (propertyName.equals(SidebarModel.P.inputNeurons.name())) {
 					layerController.removeVertex(0, newValue);
 				}
 				// HIDDEN NEURON
 				else if (propertyName.equals(SidebarModel.P.hiddenNeurons.name())) {
 					layerController.removeVertex(selectedHiddenLayer, newValue);
 				}
 				// HIDDEN LAYER
 				else if (propertyName.equals(SidebarModel.P.hiddenLayers.name())) {
 					for (int i = oldValue; i > newValue; i--) {
 						layerController.removeLayer(i);
 					}
 				} // OUTPUT NEURON
 				else if (propertyName.equals(SidebarModel.P.outputNeurons.name())) {
 					layerController.removeVertex(hiddenLayerSize + 1, newValue);
 				}
 			}
 			//
 			// Add
 			//
 			else if ((Integer) evt.getOldValue() < newValue) {
 				// INPUT NEURON
 				if (propertyName.equals(SidebarModel.P.inputNeurons.name())) {
 					layerController.addVertex(0, newValue, true);
 				}
 				// HIDDEN NEURON
 				else if (propertyName.equals(SidebarModel.P.hiddenNeurons.name())) {
 					layerController.addVertex(selectedHiddenLayer, newValue, true);
 				}
 				// HIDDEN LAYER
 				else if (propertyName.equals(SidebarModel.P.hiddenLayers.name())) {
 					for (int i = oldValue; i < newValue; i++) {
 						// i is amount of hidden layers
 						// layer controller need the global layer index
 						layerController.addLayer(i + 1);
 					}
 				}
 
 				// OUTPUT NEURON
 				else if (propertyName.equals(SidebarModel.P.outputNeurons.name())) {
 					layerController.addVertex(hiddenLayerSize + 1, newValue, true);
 				}
 			}
 
 			break;
 
 		case UPDATE_SIDEBAR_TOPOLOGY_VIEW:
 			// dont't know which the correct event
 			// update full topology panel, get the values by the sidebar and sidebarModel
 
 			/*
 			 * update input spinner
 			 */
 			sidebar.topolgyPanel.inputNeuroSpinner.setValue(sidebarModel.getInputNeurons());
 
 			/*
 			 * update output spinner
 			 */
 			sidebar.topolgyPanel.outputNeuroSpinner.setValue(sidebarModel.getOutputNeurons());
 
 			/*
 			 * update input bias
 			 */
 			sidebar.topolgyPanel.inputBiasCB.setSelected(sidebarModel.getInputBias());
 
 			/*
 			 * update hidden bias for selected hidden layer AND hidden neuron spinner
 			 */
 			// get last selection from combobox
 			Integer selectedItem = (Integer) sidebar.topolgyPanel.hiddenLayerDropDown.getSelectedItem();
 			if (selectedItem != null) {
 
 				// hidden neuron spinner
 				try {
 					List<Integer> hiddenNeuronList = sidebarModel.getHiddenNeurons();
 					Integer neuronCountForSelectedHiddenLayer = hiddenNeuronList.get(selectedItem - 1);
 					sidebar.topolgyPanel.hiddenNeuronSpinner.setValue(neuronCountForSelectedHiddenLayer);
 				} catch (IndexOutOfBoundsException e) {
 					// see below (prophylactical)
 				}
 				// hidden bias checkbox
 				try {
 					List<Boolean> hiddenBiasList = sidebarModel.getHiddenBias();
 					// use relative hidden layer index
 					Boolean val = hiddenBiasList.get(selectedItem - 1);
 					sidebar.topolgyPanel.hiddenBiasCB.setSelected(val);
 				} catch (IndexOutOfBoundsException e) {
 					// catch when the list is empty, because a new layer will be added
 					// and the this will be called because the layer gets a initial neuron
 					// but not a list with initial bias values yet, have to wait to the next property change
 				}
 			}
 
 			/*
 			 * Update hidden layer combobox and mouse mode combobox
 			 */
 
 			// refresh the comboboxen: remove all and add then again
 			// remove all items
 			sidebar.topolgyPanel.hiddenLayerComboModel.removeAllElements();
 			// add the items
 			for (int i = 1; i <= sidebarModel.getHiddenLayers(); i++) {
 				// if getHiddenayer >= 1 then enable der Elements
 				sidebar.topolgyPanel.hiddenLayerDropDown.setEnabled(true);
 				sidebar.topolgyPanel.hiddenBiasCB.setEnabled(true);
 				sidebar.topolgyPanel.hiddenNeuronSpinner.setEnabled(true);
 				sidebar.topolgyPanel.comboBoxHiddenMausModus.setEnabled(true);
 				sidebar.topolgyPanel.hiddenLayerComboModel.addElement(new Integer(i));
 			}
 			// if previous selection was not null, set to old value
 			if (selectedItem != null) {
 				// if old value don't exist anymore set to current hidden layer amount,
 				// otherwise use the previous selected item
 				if (selectedItem > sidebarModel.getHiddenLayers()) {
 					sidebar.topolgyPanel.hiddenLayerDropDown.setSelectedItem(sidebarModel.getHiddenLayers());
 					sidebar.topolgyPanel.comboBoxHiddenMausModus.setSelectedItem(sidebarModel.getHiddenLayers());
 
 					sidebar.topolgyPanel.hiddenLayerDropDown.setEnabled(false);
 					sidebar.topolgyPanel.hiddenBiasCB.setEnabled(false);
 					sidebar.topolgyPanel.hiddenNeuronSpinner.setEnabled(false);
 					sidebar.topolgyPanel.comboBoxHiddenMausModus.setEnabled(false);
 				} else {
 					sidebar.topolgyPanel.hiddenLayerDropDown.setSelectedItem(selectedItem);
 					sidebar.topolgyPanel.comboBoxHiddenMausModus.setSelectedItem(selectedItem);
 				}
 			}
 
 			break;
 
 		case UPDATE_SIDEBAR_TRAINSTRATEGY_VIEW:
 			// Update all elements in the TrainstrategyView
 			sidebar.trainStrategyPanel.comboBoxAlgorithm.setSelectedItem(sidebarModel.getAlgorithmCombo());
 			sidebar.trainStrategyPanel.spinnerLearnRate.setValue(sidebarModel.getLearnRate());
 			sidebar.trainStrategyPanel.spinnerMomentum.setValue(sidebarModel.getMomentum());
 
 			break;
 
 		case CREATE_NETWORK:
 			NetworkFactory factory = new NetworkFactory();
 			Integer inputNeurons = sidebarModel.getInputNeurons();
 			Boolean inputBias = sidebarModel.getInputBias();
 			Integer outputNeurons = sidebarModel.getOutputNeurons();
 			List<Integer> hiddenNeuronList = sidebarModel.getHiddenNeurons();
 			List<Boolean> hiddenBiasList = sidebarModel.getHiddenBias();
 			ActivationFunction activation = sidebarModel.getSelectedActivation();
 			NetConfig netConfig = factory.createNetwork(inputNeurons, inputBias, hiddenNeuronList, hiddenBiasList, outputNeurons,
 					activation);
 
 			boolean connectAll = sidebar.topolgyPanel.chckbxAllNeuronsBind.isSelected();
 			if (connectAll) {
 				netConfig.getNetwork().finalizeStructure();
 			}
 			Settings.getInstance().getCurrentSession().setNetworkConfig(netConfig);
 
 			break;
 
 		case PLAY_TRAINING:
 
 			break;
 
 		case CHANGE_MOUSE_MODI:
 			sidebar = Main.instance.sidebar;
 			String selected = (String) sidebar.topolgyPanel.comboBoxMouseModes.getSelectedItem();
 			if (selected.equals("Picking")) {
 				GraphLayoutViewer.getInstance().graphMouse.setMode(Mode.PICKING);
 				System.out.println("picking");
 				sidebar.topolgyPanel.mouseHiddenRB.setEnabled(false);
 				sidebar.topolgyPanel.mouseInputRB.setEnabled(false);
 				sidebar.topolgyPanel.mouseOutputRB.setEnabled(false);
 			} else if (selected.equals("Editing")) {
 				GraphLayoutViewer.getInstance().graphMouse.setMode(Mode.EDITING);
 				sidebar.topolgyPanel.mouseHiddenRB.setEnabled(true);
 				sidebar.topolgyPanel.mouseInputRB.setEnabled(true);
 				sidebar.topolgyPanel.mouseOutputRB.setEnabled(true);
 				System.out.println("editing");
 			} else if (selected.equals("Transforming")) {
 				GraphLayoutViewer.getInstance().graphMouse.setMode(Mode.TRANSFORMING);
 				sidebar.topolgyPanel.mouseHiddenRB.setEnabled(false);
 				sidebar.topolgyPanel.mouseInputRB.setEnabled(false);
 				sidebar.topolgyPanel.mouseOutputRB.setEnabled(false);
 				System.out.println("transforming");
 			}
 
 			break;
 
 		case SET_THE_STRATEGY:
 
 			sidebar = Main.instance.sidebar;
 			// "MaxIteration", "MinError", "RestartError","RestartImprovement"
 			String selectedStrategy = (String) sidebar.trainStrategyPanel.comboBoxTypStrategien.getSelectedItem();
 			Strategy strategy;
 			NetConfig net = Settings.getInstance().getCurrentSession().getNetworkConfig();
 			List<Strategy> strategyList = net.getStrategies();
 			Object nValue = evt.getNewValue();
 			Boolean checkBoxValue = null;
 			if (nValue instanceof Boolean) {
 				// checkbox
 				checkBoxValue = (Boolean) nValue;
 			}
 
 			if (selectedStrategy.equals("MaxIteration")) {
 				strategy = checkIfExist(strategyList, MaxLearnIterationsStrategy.class);
 				updateCBforCombo(sidebar, strategy);
 				Integer maxIterationSpinner = (Integer) sidebar.trainStrategyPanel.spinnerMaxIterations.getValue();
 				if (strategy == null) {
 					strategy = new MaxLearnIterationsStrategy();
 				}
 				((MaxLearnIterationsStrategy) strategy)._maxIteration = maxIterationSpinner;
 				if (checkBoxValue != null) {
 					if (checkBoxValue) {
 						net.addOrUpdateExisting(strategy);
 						sidebar.trainStrategyPanel.chckbxActivateStrategie.setSelected(true);
 					} else {
 						strategyList.remove(strategy);
 						sidebar.trainStrategyPanel.chckbxActivateStrategie.setSelected(false);
 					}
 				}
 
 				System.out.println("MaxIteration");
 
 			} else if (selectedStrategy.equals("MinError")) {
 				strategy = checkIfExist(strategyList, MinErrorStrategy.class);
 				updateCBforCombo(sidebar, strategy);
 				Double minErrorSpinner = (Double) sidebar.trainStrategyPanel.spinnerMinError.getValue();
 				if (strategy == null) {
 					strategy = new MinErrorStrategy();
 
 				}
 				((MinErrorStrategy) strategy)._minerror = minErrorSpinner;
 				if (checkBoxValue != null) {
 					if (checkBoxValue) {
 						net.addOrUpdateExisting(strategy);
 						sidebar.trainStrategyPanel.chckbxActivateStrategie.setSelected(true);
 					} else {
 						strategyList.remove(strategy);
 						sidebar.trainStrategyPanel.chckbxActivateStrategie.setSelected(false);
 					}
 				}
 				System.out.println("MinError");
 
 			} else if (selectedStrategy.equals("RestartError")) {
 
 				strategy = checkIfExist(strategyList, RestartErrorStrategy.class);
 				updateCBforCombo(sidebar, strategy);
 				Double maxErrorForRestartSpinner = (Double) sidebar.trainStrategyPanel.spinnerMaxErrorForRestart.getValue();
 				Integer iterationsForRestartSpinner = (Integer) sidebar.trainStrategyPanel.spinnerIterationsForRestart.getValue();
 				if (strategy == null) {
 					strategy = new RestartErrorStrategy();
 				}
 				((RestartErrorStrategy) strategy)._iterationForRestart = iterationsForRestartSpinner;
 				((RestartErrorStrategy) strategy)._error = maxErrorForRestartSpinner;
 				if (checkBoxValue != null) {
 					if (checkBoxValue) {
 						net.addOrUpdateExisting(strategy);
 						sidebar.trainStrategyPanel.chckbxActivateStrategie.setSelected(true);
 					} else {
 						strategyList.remove(strategy);
 						sidebar.trainStrategyPanel.chckbxActivateStrategie.setSelected(false);
 					}
 				}
 
 				System.out.println("RestartError");
 
 			} else if (selectedStrategy.equals("RestartImprovement")) {
 
 				strategy = checkIfExist(strategyList, RestartImprovementStrategy.class);
 				updateCBforCombo(sidebar, strategy);
 				Double minImprovForRestartSpinner = (Double) sidebar.trainStrategyPanel.spinnerMinImprovementForRestart.getValue();
 				Integer iterImprForRestartSpinner = (Integer) sidebar.trainStrategyPanel.spinnerImprIterationsForRestart.getValue();
 				if (strategy == null) {
 					strategy = new RestartImprovementStrategy();
 				}
 				((RestartImprovementStrategy) strategy)._minimalImprovement = minImprovForRestartSpinner;
 				((RestartImprovementStrategy) strategy)._iterationForRestart = iterImprForRestartSpinner;
 				if (checkBoxValue != null) {
 					if (checkBoxValue) {
 						net.addOrUpdateExisting(strategy);
 						sidebar.trainStrategyPanel.chckbxActivateStrategie.setSelected(true);
 					} else {
 						strategyList.remove(strategy);
 						sidebar.trainStrategyPanel.chckbxActivateStrategie.setSelected(false);
 					}
 				}
 				System.out.println("RestartImprovement");
 			}
 
 			break;
 
 		case TEST_UPDATEMODEL:
 
 		}
 
 	}
 
 	/**
 	 * @param sidebar
 	 * @param strategy
 	 */
 	private void updateCBforCombo(final Sidebar sidebar, final Strategy strategy) {
 		if (strategy != null) {
 			sidebar.trainStrategyPanel.chckbxActivateStrategie.setSelected(true);
 		} else {
 			sidebar.trainStrategyPanel.chckbxActivateStrategie.setSelected(false);
 		}
 	}
 
 	/**
 	 * @param strategyList
 	 * @param class1
 	 * @return
 	 */
 	private Strategy checkIfExist(final List<Strategy> strategyList, final Class<? extends Strategy> clazz) {
 		for (Strategy s : strategyList) {
 			if (s.getClass().equals(clazz)) {
 				return s;
 			}
 		}
 		return null;
 	}
 }
