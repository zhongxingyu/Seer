 /**
  * Projekt ANNtool 
  *
  * Copyright (c) 2011 github.com/timaschew/jANN
  * 
  * anton
  */
 package de.unikassel.ann.model;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 
 import de.unikassel.ann.controller.ActionController;
 import de.unikassel.ann.controller.Actions;
 import de.unikassel.ann.gui.Main;
 import de.unikassel.ann.gui.sidebar.TopologyPanel;
 import de.unikassel.ann.gui.sidebar.TrainStrategyPanel;
 import de.unikassel.ann.model.func.ActivationFunction;
 
 /**
  * @author anton
  * 
  */
 public class SidebarModel {
 
 	private PropertyChangeSupport pcs;
 
 	public enum P {
 		inputNeurons, outputNeurons, hiddenLayers, hiddenNeurons, topology, inputBias, hiddenBias, mouseModi, activateStrategy, algorithm, learnrate, momentum, online, offline
 	};
 
 	// Topology Pane
 	private Integer inputNeurons = 0;
 	private Integer outputNeurons = 0;
 	private Integer hiddenLayers = 0;
 	private List<Integer> hiddenNeurons;
 	private List<Boolean> hiddenBias;
 	private boolean inputBias;
 
 	// TrainStrategy Pane
 	private String algorithm;
 	private Double learnrate = 0.0;
 	private Double momentum = 0.0;
 	private Boolean online;
 	private Boolean offline;
 	private Boolean activateStrategy;
 
 	private ActionController ac;
 	public Neuron selectedNeuron;
 	public Synapse selectedSynapse;
 	public ActivationFunction selectedActivation;
 
 	public SidebarModel() {
 		pcs = new PropertyChangeSupport(this);
 		hiddenNeurons = new ArrayList<Integer>();
 		hiddenBias = new ArrayList<Boolean>();
 		initChangeListener();
 		ac = ActionController.get();
 	}
 
 	public Neuron getSelectedNeuron() {
 		return selectedNeuron;
 	}
 
 	public void setSelectedNeuron(final Neuron selectedNeuron) {
 		this.selectedNeuron = selectedNeuron;
 	}
 
 	public Synapse getSelectedSynapse() {
 		return selectedSynapse;
 	}
 
 	public void setSelectedSynapse(final Synapse selectedSynapse) {
 		this.selectedSynapse = selectedSynapse;
 	}
 
 	public ActivationFunction getSelectedActivation() {
 		return selectedActivation;
 	}
 
 	public void setSelectedActivation(final ActivationFunction selectedActivation) {
 		this.selectedActivation = selectedActivation;
 	}
 
 	public Boolean getInputBias() {
 		return inputBias;
 	}
 
 	public void setInputBias(final Boolean bias) {
 		Boolean oldValue = !bias;
 		inputBias = bias;
 		pcs.firePropertyChange(P.inputBias.name(), oldValue, bias);
 	}
 
 	/**
 	 * Returns the list with the bias flags for the hiden layer only and with relative index !
 	 * 
 	 * @return
 	 */
 	public List<Boolean> getHiddenBias() {
 		return hiddenBias;
 	}
 
 	/**
 	 * Returns the bias flag for the global layerIndex
 	 * 
 	 * @param layerIndex
 	 *            global layerIndex
 	 * @return
 	 */
 	public Boolean hasLayerBias(final int layerIndex) {
 		if (layerIndex == 0) {
 			return inputBias;
 		} else if (layerIndex <= hiddenLayers) {
 			// sub the input layer
 			return hiddenBias.get(layerIndex - 1);
 		}
 		return false; // output layer have never a bias
 	}
 
 	/**
 	 * Sets the bias for the relative hidden layer index
 	 * 
 	 * @param layerIndex
 	 *            relative layerIndex
 	 * @param bias
 	 */
 	public void setHiddenBias(final Integer layerIndex, final Boolean bias) {
 		if (layerIndex < 0) {
 			return;
 		}
 		Boolean oldValue = !bias;
 		if (hiddenBias.size() <= layerIndex) {
 			hiddenBias.add(bias);
 		} else {
 			oldValue = hiddenBias.get(layerIndex);
 			hiddenBias.set(layerIndex, bias);
 		}
 		pcs.firePropertyChange(P.hiddenBias.name(), oldValue, bias);
 
 	}
 
 	/**
 	 * Returns the the global index for the selected hidden layer (Mouse Insert Mode)
 	 * 
 	 * @return
 	 */
 	public Integer getSelectedGlobalHiddenLayerIndex() {
 		TopologyPanel topoPanel = Main.instance.sidebar.topolgyPanel;
 		// local = relative
 		Integer localHiddenLayerIndex = (Integer) topoPanel.hiddenLayerComboModel.getSelectedItem();
 		// is equals to the global layer index, because its the size of all hidden layers and starts with 1
 		return localHiddenLayerIndex;
 	}
 
 	public Integer getMouseInsertLayer() {
 		TopologyPanel topoPanel = Main.instance.sidebar.topolgyPanel;
 		if (topoPanel.mouseInputRB.isSelected()) {
 			return 0;
 		} else if (topoPanel.mouseOutputRB.isSelected()) {
 			// input + hidden + 1 = output
 			return hiddenLayers + 1;
 		} else if (topoPanel.mouseHiddenRB.isSelected()) {
 			Integer selectedHiddenLayer = (Integer) topoPanel.comboBoxHiddenMausModus.getSelectedItem();
 			return selectedHiddenLayer;
 		}
 		throw new IllegalAccessError("radio buttons for insert mode are all deselected");
 	}
 
 	/**
 	 * @return the inputNeurons
 	 */
 	public Integer getInputNeurons() {
 		return inputNeurons;
 	}
 
 	/**
 	 * @param inputNeurons
 	 *            the inputNeurons to set
 	 */
 	public void setInputNeurons(final Integer inputNeurons) {
 		if (inputNeurons < 0) {
 			return;
 		}
 		Integer oldValue = this.inputNeurons;
 		this.inputNeurons = inputNeurons;
 		pcs.firePropertyChange(P.inputNeurons.name(), oldValue, inputNeurons);
 	}
 
 	/**
 	 * @return the outputNeurons
 	 */
 	public Integer getOutputNeurons() {
 		return outputNeurons;
 	}
 
 	/**
 	 * @param outputNeurons
 	 *            the outputNeurons to set
 	 */
 	public void setOutputNeurons(final Integer outputNeurons) {
 		if (outputNeurons < 0) {
 			return;
 		}
 		Integer oldValue = this.outputNeurons;
 		this.outputNeurons = outputNeurons;
 		pcs.firePropertyChange(P.outputNeurons.name(), oldValue, outputNeurons);
 	}
 
 	/**
 	 * @return the hiddenLayers
 	 */
 	public Integer getHiddenLayers() {
 		return hiddenLayers;
 	}
 
 	/**
 	 * @param hiddenLayers
 	 *            the hiddenLayers to set
 	 */
 	public void setHiddenLayers(final Integer hiddenLayers) {
 		if (hiddenLayers < 0) {
 			return;
 		}
 		Integer oldValue = this.hiddenLayers;
 		this.hiddenLayers = hiddenLayers;
 		pcs.firePropertyChange(P.hiddenLayers.name(), oldValue, hiddenLayers);
 		for (int i = oldValue; i < hiddenLayers; i++) {
 			// i is relaltive hidden layer index !!!!!
 			// init values
 			setHiddenNeurons(i, 1);
 			setHiddenBias(i, false);
 		}
 	}
 
 	/**
 	 * RELATIVE INDEX FOR HIDDEN LAYER !
 	 * 
 	 * @return the hiddenNeurons
 	 */
 	public List<Integer> getHiddenNeurons() {
 		return hiddenNeurons;
 	}
 
 	/**
 	 * RELATIVE INDEX FOR HIDDEN LAYER !
 	 * 
 	 * @param hiddenNeurons
 	 *            the hiddenNeurons to set
 	 */
 	public void setHiddenNeurons(final Integer layerIndex, final Integer neuronCount) {
 		if (layerIndex < 0 || neuronCount < 0) {
 			return;
 		}
 		Integer oldValue = 0;
 		if (hiddenNeurons.size() <= layerIndex) {
 			hiddenNeurons.add(neuronCount);
 		} else {
			oldValue = hiddenNeurons.get(layerIndex);
 			hiddenNeurons.set(layerIndex, neuronCount);
 		}
 		pcs.firePropertyChange(P.hiddenNeurons.name(), oldValue, neuronCount);
 	}
 
 	public void createNetwork() {
 		// TODO create Network
 	}
 
 	/**
 	 * Training-Strategy panel
 	 */
 
 	/**
 	 * Checkbox that activate the Strategy
 	 * 
 	 * @return the activateStrategy
 	 */
 	public boolean getActivateStrategy() {
 		return activateStrategy;
 	}
 
 	/**
 	 * @param activateStrategy
 	 *            the activateStrategy to set
 	 */
 	public void setActivateStrategy(final Boolean activStrategy) {
 		Boolean oldValue = !activateStrategy;
 		activateStrategy = activStrategy;
 		pcs.firePropertyChange(P.activateStrategy.name(), oldValue, activStrategy);
 
 	}
 
 	/**
 	 * @return
 	 */
 	public String getAlgorithmCombo() {
 		return algorithm;
 	}
 
 	public void setAlgorithmCombo(final String algorithm) {
 		String oldValue = this.algorithm;
 		this.algorithm = algorithm;
 		pcs.firePropertyChange(P.algorithm.name(), oldValue, algorithm);
 
 	}
 
 	/**
 	 * @return
 	 */
 	public Double getLearnRate() {
 		return learnrate;
 	}
 
 	public void setLearnRate(final Double learnrate) {
 		Double oldValue = this.learnrate;
 		this.learnrate = learnrate;
 		pcs.firePropertyChange(P.learnrate.name(), oldValue, learnrate);
 	}
 
 	/**
 	 * @return
 	 */
 	public Double getMomentum() {
 		return momentum;
 	}
 
 	public void setMomentum(final Double momentum) {
 		Double oldValue = this.momentum;
 		this.momentum = momentum;
 		pcs.firePropertyChange(P.momentum.name(), oldValue, momentum);
 	}
 
 	public Boolean setTrainingModus() {
 		TrainStrategyPanel trainStrPanel = Main.instance.sidebar.trainStrategyPanel;
 		if (trainStrPanel.rdbtnOnline.isSelected()) {
 			// TODO Set the Trainingmodus
 		} else if (trainStrPanel.rdbtnOffline.isSelected()) {
 
 		}
 
 		throw new IllegalAccessError("radio buttons for insert mode are all deselected");
 	}
 
 	// /**
 	// * @return
 	// */
 	// public boolean getTrainOnline() {
 	// return online;
 	// }
 	//
 	// public void setTrainOnline(final Boolean online) {
 	// Boolean oldValue = !online;
 	// this.online = online;
 	// pcs.firePropertyChange(P.online.name(), oldValue, online);
 	// }
 	//
 	// /**
 	// * @return
 	// */
 	// public boolean getTrainOffline() {
 	// return offline;
 	// }
 	//
 	// public void setTrainOffline(final Boolean offline) {
 	// Boolean oldValue = !offline;
 	// this.offline = offline;
 	// pcs.firePropertyChange(P.offline.name(), oldValue, offline);
 	// }
 
 	/**
 	 * 
 	 */
 	private void initChangeListener() {
 		// fire for all property changes
 		pcs.addPropertyChangeListener(new PropertyChangeListener() {
 			@Override
 			public void propertyChange(final PropertyChangeEvent evt) {
 				ac.doAction(Actions.UPDATE_SIDEBAR_TOPOLOGY_VIEW, evt);
 			}
 		});
 
 		pcs.addPropertyChangeListener(P.inputNeurons.name(), new PropertyChangeListener() {
 			@Override
 			public void propertyChange(final PropertyChangeEvent evt) {
 				ac.doAction(Actions.UPDATE_JUNG_GRAPH, evt);
 			}
 		});
 		pcs.addPropertyChangeListener(P.hiddenNeurons.name(), new PropertyChangeListener() {
 			@Override
 			public void propertyChange(final PropertyChangeEvent evt) {
 				ac.doAction(Actions.UPDATE_JUNG_GRAPH, evt);
 			}
 		});
 		pcs.addPropertyChangeListener(P.outputNeurons.name(), new PropertyChangeListener() {
 			@Override
 			public void propertyChange(final PropertyChangeEvent evt) {
 				ac.doAction(Actions.UPDATE_JUNG_GRAPH, evt);
 			}
 		});
 		pcs.addPropertyChangeListener(P.hiddenLayers.name(), new PropertyChangeListener() {
 			@Override
 			public void propertyChange(final PropertyChangeEvent evt) {
 				ac.doAction(Actions.UPDATE_JUNG_GRAPH, evt);
 			}
 		});
 		pcs.addPropertyChangeListener(P.hiddenBias.name(), new PropertyChangeListener() {
 			@Override
 			public void propertyChange(final PropertyChangeEvent evt) {
 				ac.doAction(Actions.UPDATE_JUNG_GRAPH_INPUT_BIAS, evt);
 			}
 		});
 		pcs.addPropertyChangeListener(P.inputBias.name(), new PropertyChangeListener() {
 			@Override
 			public void propertyChange(final PropertyChangeEvent evt) {
 				ac.doAction(Actions.UPDATE_JUNG_GRAPH_HIDDEN_BIAS, evt);
 			}
 		});
 
 		// TrainStrategy Pane
 		pcs.addPropertyChangeListener(P.algorithm.name(), new PropertyChangeListener() {
 			@Override
 			public void propertyChange(final PropertyChangeEvent evt) {
 				ac.doAction(Actions.UPDATE_SIDEBAR_TRAINSTRATEGY_VIEW, evt);
 			}
 		});
 
 		pcs.addPropertyChangeListener(P.learnrate.name(), new PropertyChangeListener() {
 			@Override
 			public void propertyChange(final PropertyChangeEvent evt) {
 				ac.doAction(Actions.UPDATE_SIDEBAR_TRAINSTRATEGY_VIEW, evt);
 			}
 		});
 
 		pcs.addPropertyChangeListener(P.momentum.name(), new PropertyChangeListener() {
 			@Override
 			public void propertyChange(final PropertyChangeEvent evt) {
 				ac.doAction(Actions.UPDATE_SIDEBAR_TRAINSTRATEGY_VIEW, evt);
 			}
 		});
 
 		// pcs.addPropertyChangeListener(P.online.name(), new PropertyChangeListener() {
 		// @Override
 		// public void propertyChange(final PropertyChangeEvent evt) {
 		// ac.doAction(Actions.UPDATE_SIDEBAR_TRAINSTRATEGY_VIEW, evt);
 		// }
 		// });
 		//
 		// pcs.addPropertyChangeListener(P.offline.name(), new PropertyChangeListener() {
 		// @Override
 		// public void propertyChange(final PropertyChangeEvent evt) {
 		// ac.doAction(Actions.UPDATE_SIDEBAR_TRAINSTRATEGY_VIEW, evt);
 		// }
 		// });
 
 		pcs.addPropertyChangeListener(P.activateStrategy.name(), new PropertyChangeListener() {
 			@Override
 			public void propertyChange(final PropertyChangeEvent evt) {
 				ac.doAction(Actions.UPDATE_SIDEBAR_TRAINSTRATEGY_VIEW, evt);
 			}
 		});
 
 	}
 
 }
