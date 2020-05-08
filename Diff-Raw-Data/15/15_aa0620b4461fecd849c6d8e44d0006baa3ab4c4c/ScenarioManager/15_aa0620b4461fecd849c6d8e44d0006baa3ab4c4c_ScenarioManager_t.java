 /**
  * Copyright (c) 2013 SAP
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the SAP nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SAP BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.sopeco.webui.client.manager;
 
 import java.util.logging.Logger;
 
 import org.sopeco.persistence.entities.definition.ConstantValueAssignment;
 import org.sopeco.persistence.entities.definition.ExperimentSeriesDefinition;
 import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;
 import org.sopeco.persistence.entities.definition.ParameterDefinition;
 import org.sopeco.persistence.entities.definition.ParameterNamespace;
 import org.sopeco.persistence.entities.definition.ParameterRole;
 import org.sopeco.persistence.entities.definition.ScenarioDefinition;
 import org.sopeco.webui.client.SoPeCoUI;
 import org.sopeco.webui.client.helper.SimpleCallback;
 import org.sopeco.webui.client.layout.MainLayoutPanel;
 import org.sopeco.webui.client.layout.center.experiment.ExperimentController;
 import org.sopeco.webui.client.layout.center.specification.SpecificationController;
 import org.sopeco.webui.client.manager.helper.Duplicator;
 import org.sopeco.webui.shared.builder.ScenarioDefinitionBuilder;
 import org.sopeco.webui.shared.entities.ScenarioDetails;
 import org.sopeco.webui.shared.helper.Helper;
 import org.sopeco.webui.shared.helper.Utilities;
 import org.sopeco.webui.shared.rpc.RPC;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * 
  * @author Marius Oehler
  * 
  */
 public final class ScenarioManager {
 
 	private static final Logger LOGGER = Logger.getLogger(ScenarioManager.class.getName());
 	private static ScenarioManager modelManager;
 
 	/**
 	 * Creates a new scenario manager and stores the new object in a static
 	 * attribute which is accessible by the static get() method.
 	 */
 	public static void clear() {
 		modelManager = new ScenarioManager();
 	}
 
 	/**
 	 * Returns a object of the ScenarioManager class.
 	 * 
 	 * @return
 	 */
 	public static ScenarioManager get() {
 		if (modelManager == null) {
 			modelManager = new ScenarioManager();
 		}
 
 		return modelManager;
 	}
 
 	private ScenarioDefinitionBuilder builder;
 	private String currentScenarioName;
 	private ExperimentModul experimentModul;
 
 	private boolean scenarioLoaded = false;
 
 	private SpecificationModul specificationModul;
 
 	/**
 	 * Constructor.
 	 */
 	private ScenarioManager() {
 		builder = new ScenarioDefinitionBuilder();
 	}
 
 	/**
 	 * Changes the value of the given InitAssignment Parameter.
 	 * 
 	 * @param path
 	 * @param name
 	 * @param newValue
 	 * @return
 	 */
 	public boolean changeInitAssignmentValue(String path, String name, String newValue) {
 		ParameterNamespace namespace = getScenarioDefinitionBuilder().getEnvironmentBuilder().getNamespace(path, "\\.");
 		ParameterDefinition parameter = getScenarioDefinitionBuilder().getEnvironmentBuilder().getParameter(name, namespace);
 
 		if (parameter == null) {
 			return false;
 		}
 
 		for (ConstantValueAssignment cva : getScenarioDefinitionBuilder().getSpecificationBuilder().getBuiltSpecification()
 				.getInitializationAssignemts()) {
 			if (cva.getParameter().getFullName().equals(parameter.getFullName())) {
 				cva.setValue(newValue);
 				return true;
 			}
 		}
 
 		// TODO correct REST update?
		//storeScenario();
 		
 		return false;
 	}
 
 	/**
 	 * Clones the current/working scenario and sets the name of the new one to
 	 * the given name. The new scenario will be set as the working scenario.
 	 */
 	public void cloneCurrentScenario(final String targetName) {
 		ScenarioDefinition clone = Duplicator.cloneScenario(builder.getBuiltScenario());
 		clone.setScenarioName(Utilities.cleanString(targetName));
 		RPC.getScenarioManager().addScenario(clone, new AsyncCallback<Boolean>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				LOGGER.severe(caught.getMessage());
 			}
 
 			@Override
 			public void onSuccess(Boolean result) {
 				Manager.get().getAccountDetails().addScenarioDetails(Utilities.cleanString(targetName));
 				Manager.get().getAccountDetails().setSelectedScenario(Utilities.cleanString(targetName));
 				Manager.get().storeAccountDetails();
 
 				MainLayoutPanel.get().getNorthPanel().updateScenarioListAndSwitch(Utilities.cleanString(targetName));
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * @param scenarioName
 	 * @param specificationName
 	 * @param experiment
 	 * @param simpleNotify
 	 */
 	public void createScenario(String scenarioName, String specificationName, ExperimentSeriesDefinition experiment,
 			final SimpleCallback<Object> simpleNotify) {
 		String cleanedScenarioName = Utilities.cleanString(scenarioName);
 
 		String tempName = cleanedScenarioName;
 		int c = 2;
 		while (ScenarioManager.get().existScenario(tempName)) {
 			tempName = cleanedScenarioName + "_" + (c++);
 		}
 		final String newScenarioName = tempName;
 
 		RPC.getScenarioManager().addScenario(cleanedScenarioName, specificationName, experiment,
 				new AsyncCallback<Boolean>() {
 					@Override
 					public void onFailure(Throwable caught) {
 						SoPeCoUI.get().onUncaughtException(caught);
 					}
 
 					@Override
 					public void onSuccess(Boolean result) {
 						Manager.get().getAccountDetails().addScenarioDetails(newScenarioName);
 						Manager.get().getAccountDetails().setSelectedScenario(newScenarioName);
 						Manager.get().storeAccountDetails();
 
 						if (simpleNotify != null) {
 							simpleNotify.callback(null);
 						} else {
 							MainLayoutPanel.get().getNorthPanel().updateScenarioList();
 							switchScenario(newScenarioName);
 						}
 					}
 				});
 	}
 
 	/**
 	 * Returns whether a scenario with the given name exists.
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public boolean existScenario(String name) {
 		for (String sName : Manager.get().getAccountDetails().getScenarioNames()) {
 			if (sName.equals(name)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns the ExperimentModul, which contains all methods that were related
 	 * to experiments.
 	 * 
 	 * @return experimentModul
 	 */
 	public ExperimentModul getExperimentModul() {
 		if (experimentModul == null) {
 			experimentModul = new ExperimentModul(this);
 		}
 
 		return experimentModul;
 	}
 
 	/**
 	 * Returns the current scenario builder.
 	 * 
 	 * @return ScenarioDefinitionBuilder
 	 */
 	public ScenarioDefinitionBuilder getScenarioDefinitionBuilder() {
 		return builder;
 	}
 
 	/**
 	 * Returns the scenario definition of the current scenario builder.
 	 * 
 	 * @return ScenarioDefinition
 	 */
 	public ScenarioDefinition getCurrentScenarioDefinition() {
 		return builder.getBuiltScenario();
 	}
 
 	/**
 	 * Retuns the name of the current scnenario.
 	 * 
 	 * @return
 	 */
 	public String getCurrentScenarioName() {
 		return currentScenarioName;
 	}
 
 	/**
 	 * Returns true if any scenario has been loaded and is available now.
 	 * 
 	 * @return
 	 */
 	public boolean isScenarioAvailable() {
 		return scenarioLoaded;
 	}
 
 	/**
 	 * Loading the scenario definition of the current selected scenario from the
 	 * server and stored it at the client.
 	 */
 	public void loadCurrentScenarioFromServer() {
 		GWT.log("Loading scenario from Server.");
 		
 		RPC.getScenarioManager().getCurrentScenarioDefinition(new AsyncCallback<ScenarioDefinition>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				SoPeCoUI.get().onUncaughtException(caught);
 			}
 
 			@Override
 			public void onSuccess(ScenarioDefinition result) {
 				GWT.log("Loaded scenario from Server.");
 				
 				if (result == null) {
 					LOGGER.severe("Error while loading scenario definition.");
 					return;
 				}
 
 				builder = ScenarioDefinitionBuilder.load(result);
 				scenarioLoaded = true;
 
 				if (Manager.get().getCurrentScenarioDetails() != null) {
 					String specification = Manager.get().getCurrentScenarioDetails().getSelectedSpecification();
 					if (specification == null || !specification().existSpecification(specification)) {
 						specification = builder.getBuiltScenario().getMeasurementSpecifications().get(0).getName();
 					}

 					MainLayoutPanel.get().reloadPanels();
					
					specification().changeSpecification(specification);
 				}
 
 				MainLayoutPanel.get().switchView(SpecificationController.class);
 			}
 		});
 	}
 
 	/**
 	 * 
 	 */
 	public void loadDefinitionFromCurrentController() {
 		RPC.getMEControllerRPC().getMEDefinitionFromMEC(Manager.get().getControllerUrl(),
 				new AsyncCallback<MeasurementEnvironmentDefinition>() {
 					@Override
 					public void onFailure(Throwable caught) {
 						SoPeCoUI.get().onUncaughtException(caught);
 					}
 
 					@Override
 					public void onSuccess(MeasurementEnvironmentDefinition result) {
 						for (ParameterDefinition pd : result.getRoot().getAllParameters()) {
 							GWT.log("0: " + pd.getFullName());
 						}
 						
 						setMeasurementDefinition(result);
 					}
 				});
 	}
 
 	/**
 	 * Removes the scenario with the given name.
 	 * 
 	 * @param scenarioName
 	 */
 	public void removeScenario(final String scenarioName) {
 		RPC.getScenarioManager().removeScenario(scenarioName, new AsyncCallback<Boolean>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				SoPeCoUI.get().onUncaughtException(caught);
 			}
 
 			@Override
 			public void onSuccess(Boolean result) {
 				for (ScenarioDetails sd : Manager.get().getAccountDetails().getScenarioDetails()) {
 					if (sd.getScenarioName().equals(scenarioName)) {
 						Manager.get().getAccountDetails().getScenarioDetails().remove(sd);
 						Manager.get().storeAccountDetails();
 						break;
 					}
 				}
 				MainLayoutPanel.get().getNorthPanel().updateScenarioList();
 				if (Manager.get().getAccountDetails().getScenarioDetails().isEmpty()) {
 					MainLayoutPanel.get().switchView(null);
 				} else {
 					switchScenario(Manager.get().getAccountDetails().getScenarioNames()[0]);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Sets the given me-definition as the current definition and updates the
 	 * EnvrionmentTrees of the SpecificationView and ExperimentView.
 	 * 
 	 * @param environment
 	 *            new me-definition
 	 */
 	public void setMeasurementDefinition(MeasurementEnvironmentDefinition environment) {
		builder.getBuiltScenario().setMeasurementEnvironmentDefinition(environment);
 		
		// TODO why not UI update??
 		for (ParameterDefinition pd : environment.getRoot().getAllParameters()) {
 			GWT.log("1: " + pd.getFullName());
 		}
 		
 		RPC.getMEControllerRPC().setMEDefinition(environment, new AsyncCallback<Boolean>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				SoPeCoUI.get().onUncaughtException(caught);
 			}
 
 			@Override
 			public void onSuccess(Boolean result) {
 				if (MainLayoutPanel.get().getController(SpecificationController.class).getEnvironmentTree() != null) {
 					MainLayoutPanel.get().getController(SpecificationController.class).getEnvironmentTree().generateTree();
 				}
 				if (MainLayoutPanel.get().getController(ExperimentController.class).getEnvironmentTree() != null) {
 					MainLayoutPanel.get().getController(ExperimentController.class).getEnvironmentTree().generateTree();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Returns the SpecificationModul, which contains all methods that were
 	 * related to specification.
 	 * 
 	 * @return experimentModul
 	 */
 	public SpecificationModul specification() {
 		if (specificationModul == null) {
 			specificationModul = new SpecificationModul(this);
 		}
 
 		return specificationModul;
 	}
 
 	/**
 	 * Sends the current scenario to the server and stores them in the database.
 	 */
 	public void storeScenario() {
 		Helper.whoCalledMe();
 		
 		LOGGER.fine("Storing scenario. Sending it to RPC.");
 		
 		RPC.getScenarioManager().storeScenarioDefinition(getCurrentScenarioDefinition(), new AsyncCallback<Boolean>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				LOGGER.severe(caught.getLocalizedMessage());
 				SoPeCoUI.get().onUncaughtException(caught);
 			}
 
 			@Override
 			public void onSuccess(Boolean result) {
 			}
 		});
 	}
 
 	/**
 	 * Switch the current scenario to the given scenario(name).
 	 * 
 	 * @param scenarioName
 	 *            name of the new scenario
 	 */
 	public void switchScenario(final String scenarioName) {
 		LOGGER.fine("switch scenario to: " + scenarioName);
 		if (scenarioName == null) {
 			return;
 		}
 		Manager.get().getAccountDetails().setSelectedScenario(scenarioName);
 		Manager.get().storeAccountDetails();
 
 		RPC.getScenarioManager().switchScenario(scenarioName, new AsyncCallback<Boolean>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				LOGGER.severe(caught.getLocalizedMessage());
 				SoPeCoUI.get().onUncaughtException(caught);
 			}
 
 			@Override
 			public void onSuccess(Boolean result) {
 				currentScenarioName = scenarioName;
 				loadCurrentScenarioFromServer();
 
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * @param path
 	 * @param oldName
 	 * @param newName
 	 * @param type
 	 * @param role
 	 * @return
 	 */
 	public boolean updateParameter(String path, String oldName, String newName, String type, ParameterRole role) {
 		LOGGER.info("rpc: updateParameter: " + oldName + " from '" + path + "'");
 
 		ParameterNamespace ns = getScenarioDefinitionBuilder().getEnvironmentBuilder().getNamespace(path);
 
 		if (ns == null) {
 			LOGGER.info("no namespace '" + ns + "' found");
 			return false;
 		}
 
 		ParameterDefinition parameter = getScenarioDefinitionBuilder().getEnvironmentBuilder().getParameter(oldName, ns);
 
 		if (parameter == null) {
 			LOGGER.info("no parameter '" + oldName + "' found");
 			return false;
 		}
 
 		ConstantValueAssignment initialAssignmentParameter = null;
 		for (ConstantValueAssignment cva : getScenarioDefinitionBuilder().getSpecificationBuilder().getBuiltSpecification()
 				.getInitializationAssignemts()) {
 			if (cva.getParameter().getFullName().equals(parameter.getFullName())) {
 				initialAssignmentParameter = cva;
 			}
 		}
 
 		parameter.setName(newName);
 		parameter.setType(type);
 		parameter.setRole(role);
 
 		if (initialAssignmentParameter != null) {
 			initialAssignmentParameter.setParameter(parameter);
 			MainLayoutPanel.get().getController(SpecificationController.class).addExistingAssignments();
 		}
 
 		storeScenario();
 
 		return true;
 	}
 }
