 package nl.tue.fingerpaint.client;
 
 import java.util.ArrayList;
 
 import nl.tue.fingerpaint.client.gui.CustomTreeModel;
 import nl.tue.fingerpaint.client.gui.GraphVisualisator;
 import nl.tue.fingerpaint.client.gui.GuiState;
 import nl.tue.fingerpaint.client.gui.spinners.NrStepsSpinner;
 import nl.tue.fingerpaint.client.model.ApplicationState;
 import nl.tue.fingerpaint.client.resources.FingerpaintConstants;
 import nl.tue.fingerpaint.client.resources.FingerpaintResources;
 import nl.tue.fingerpaint.client.serverdata.ServerDataCache;
 import nl.tue.fingerpaint.client.storage.FileExporter;
 import nl.tue.fingerpaint.client.storage.FingerpaintJsonizer;
 import nl.tue.fingerpaint.client.storage.FingerpaintZipper;
 import nl.tue.fingerpaint.client.storage.ResultStorage;
 import nl.tue.fingerpaint.client.storage.StorageManager;
 import nl.tue.fingerpaint.shared.GeometryNames;
 import nl.tue.fingerpaint.shared.model.MixingProtocol;
 import nl.tue.fingerpaint.shared.simulator.Simulation;
 import nl.tue.fingerpaint.shared.simulator.SimulationResult;
 import nl.tue.fingerpaint.shared.simulator.SimulatorService;
 import nl.tue.fingerpaint.shared.simulator.SimulatorServiceAsync;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.debug.client.DebugInfo;
 import com.google.gwt.dom.client.IFrameElement;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.http.client.RequestTimeoutException;
 import com.google.gwt.user.cellview.client.CellBrowser;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.view.client.TreeViewModel;
 import com.google.gwt.visualization.client.VisualizationUtils;
 import com.google.gwt.visualization.client.visualizations.LineChart;
 
 /**
  * This is the entry point of the Fingerpaint application.
  * 
  * @author Group Fingerpaint
  */
 public class Fingerpaint implements EntryPoint {
 	// ---- PROTECTED GLOBALS
 	// ---------------------------------------------------------------------
 	/** Class to keep track of everything the user has selected */
 	protected ApplicationState as;
 
 	/** Holds the mixingPerformance of the last run. */
 	protected GraphVisualisator graphVisualisator;
 
 	/**
 	 * String to determine the save button (either distribution, results or
 	 * protocol) that was clicked last.
 	 */
 	protected String lastSaveButtonClicked;
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 		// Set the DebugID prefix to empty
 		DebugInfo.setDebugIdPrefix("");
 
 		// Load CSS
 		FingerpaintResources.INSTANCE.css().ensureInjected();
 
 		// Initialise the loading panel
 		// Add animation image
 		Image loadImage = new Image(FingerpaintResources.INSTANCE
 				.loadImageDynamic().getSafeUri());
 		loadImage.getElement().setId("loadImage");
 		GuiState.loadingPanel.add(loadImage);
 
 		// Add label that may contain explanatory text
 		GuiState.loadingPanelMessage = new Label(
 				FingerpaintConstants.INSTANCE.loadingGeometries(), false);
 		GuiState.loadingPanelMessage.getElement().setId(
 				GuiState.LOADINGPANEL_MESSAGE_ID);
 		GuiState.loadingPanel.add(GuiState.loadingPanelMessage);
 
 		// initialise the underlying model of the application
 		as = new ApplicationState();
 		as.setNrSteps(NrStepsSpinner.DEFAULT_VALUE);
 		setLoadingPanelVisible(true);
 		ServerDataCache.initialise(new AsyncCallback<String>() {
 			@Override
 			public void onSuccess(String result) {
 				setLoadingPanelVisible(false);
 				loadMenu();
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				setLoadingPanelVisible(false);
 				if (caught instanceof RequestTimeoutException) {
 					showError(FingerpaintConstants.INSTANCE
 							.simulationRequestTimeout());
 				} else {
 					showError(caught.getMessage());
 				}
 			}
 		});
 
 		// Set (debug) IDs on a number of elements that do not have a dedicated
 		// class
 		GuiState.setIDs();
 
 		// switch between portrait and canvas view
 		Window.addResizeHandler(new ResizeHandler() {
 			@Override
 			public void onResize(ResizeEvent event) {
 				if (as.getGeometry() != null) {
 					as.getGeometry().resize(Window.getClientWidth() - 20,
 							Window.getClientHeight() - 20);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Returns the String that describes which save button has been clicked
 	 * last.
 	 * 
 	 * @return the String that describes which save button has been clicked
 	 *         last.
 	 */
 	public String getLastClicked() {
 		return lastSaveButtonClicked;
 	}
 
 	/**
 	 * Sets the String that describes which save button has been clicked last.
 	 * 
 	 * @param lastClicked
 	 *            The new value that describes which save button has been
 	 *            clicked last.
 	 */
 	public void setLastClicked(String lastClicked) {
 		lastSaveButtonClicked = lastClicked;
 	}
 
 	/**
 	 * Returns the graph visualisator that holds the mixing performance of the
 	 * last run.
 	 * 
 	 * @return The graph visualisator that holds the mixing performance of the
 	 *         last run
 	 */
 	public GraphVisualisator getGraphVisualisator() {
 		return graphVisualisator;
 	}
 
 	/**
 	 * Build and show the main menu.
 	 */
 	protected void loadMenu() {
 		// Create a model for the cellbrowser.
 		TreeViewModel model = new CustomTreeModel(this, as);
 
 		/*
 		 * Create the browser using the model. We specify the default value of
 		 * the hidden root node as "null".
 		 */
 		CellBrowser tree = (new CellBrowser.Builder<Object>(model, null))
 				.build();
 		tree.getElement().setId("cell");
 
 		// Add the tree to the root layout panel.
 		RootLayoutPanel.get().add(tree);
 	}
 
 	/**
 	 * Show a pop-up with given message that indicates an error has occurred.
 	 * 
 	 * @param message
 	 *            A message that explains the error.
 	 */
 	protected void showError(String message) {
 		final PopupPanel errorPopup = new PopupPanel(false, true);
 		errorPopup.setAnimationEnabled(true);
 		VerticalPanel verPanel = new VerticalPanel();
 		verPanel.add(new Label(FingerpaintConstants.INSTANCE.errorOccured()));
 		if (message != null) {
 			verPanel.add(new Label(message));
 		}
 		verPanel.add(new Button(FingerpaintConstants.INSTANCE.btnClose(),
 				new ClickHandler() {
 					@Override
 					public void onClick(ClickEvent event) {
 						errorPopup.hide();
 					}
 				}));
 		errorPopup.add(verPanel);
 		errorPopup.center();
 	}
 
 	/**
 	 * Save the currently shown graph to disk in svg format.
 	 * 
 	 * @param multiple
 	 *            Indicates whether the single graph or the multiple graphs
 	 *            graph should be exported.
 	 */
 	public void exportGraph(boolean multiple) {
 		// get the panel that contains the right graph (single or multiple)
 		String id = multiple ? "compareGraphPanel"
 				: "viewSingleGraphGraphPanel";
 
 		String svg = IFrameElement
 				.as(DOM.getElementById(id).getElementsByTagName("iframe")
 						.getItem(0)).getContentDocument()
 				.getElementById("chartArea").getInnerHTML();
 
 		FileExporter.exportSvgImage(svg);
 	}
 
 	/**
 	 * Change the message that is displayed in the load panel below the loading
 	 * animation.
 	 * 
 	 * @param message
 	 *            The message to show below the animation. When {@code null},
 	 *            the message will be deleted.
 	 */
 	protected void setLoadingPanelMessage(String message) {
 		if (message == null) {
 			message = "";
 		}
 
 		GuiState.loadingPanelMessage.setText(message);
 	}
 
 	/**
 	 * Toggles the visibility and availability of all the protocol widgets.
 	 * 
 	 * @param visible
 	 *            If the "define protocol" widgets should be visible or not.
 	 */
 	public void setProtocolWidgetsVisible(boolean visible) {
 		GuiState.protocolPanelContainer.setVisibleAnimated(visible);
 	}
 
 	/**
 	 * this method is used to acquire the size of the current cursor in pixels
 	 * 
 	 * @return cursorSizeSpinner.getValue()-1
 	 */
 	public int getCursorSize() {
 		return (int) GuiState.cursorSizeSpinner.getValue() - 1;
 	}
 
 	/**
 	 * Shows {@link GuiState#saveItemPanel} and clears
 	 * {@link GuiState#saveNameTextBox}. Also gives focus to the textbox.
 	 */
 	public void showSavePanel() {
 		GuiState.saveItemPanel.center();
 		GuiState.saveNameTextBox.setText("");
 		GuiState.saveNameTextBox.setFocus(true);
 	}
 
 	/**
 	 * Saves the current protocol, distribution or mixing results, depending on
 	 * which save-button was pressed last.
 	 * 
 	 * @param name
 	 *            Name of save "file".
 	 * @param canOverwrite
 	 *            If we can overwrite an already-existing "file" with the given
 	 *            name or not.
 	 * @return {@code true} if "file" was saved, {@code false} otherwise
 	 */
 	public boolean save(String name, boolean canOverwrite) {
 		if (lastSaveButtonClicked.equals(StorageManager.KEY_INITDIST)) {
 			return StorageManager.INSTANCE.putDistribution(
 					GeometryNames.getShortName(as.getGeometryChoice()), name,
 					as.getGeometry().getDistribution(), canOverwrite);
 		} else if (lastSaveButtonClicked.equals(StorageManager.KEY_PROTOCOLS)) {
 			return StorageManager.INSTANCE.putProtocol(
 					GeometryNames.getShortName(as.getGeometryChoice()), name,
 					as.getProtocol(), canOverwrite);
 		} else if (lastSaveButtonClicked.equals(StorageManager.KEY_RESULTS)) {
 			ResultStorage result = new ResultStorage();
 			result.setGeometry(as.getGeometryChoice());
 			result.setMixer(as.getMixerChoice());
 			result.setDistribution(as.getInitialDistribution());
 			result.setMixingProtocol(as.getProtocol());
 			result.setSegregation(as.getSegregation());
 			result.setNrSteps(as.getNrSteps());
 
 			try {
 				return StorageManager.INSTANCE.putResult(name, result,
 						canOverwrite);
 			} catch (Exception e) {
 				GWT.log("Saving results encountered an error", e);
 				return false;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * <p>
 	 * Show or hide an overlay with a loading animation in the centre. Making
 	 * this panel visible will make it impossible for the user to give input.
 	 * </p>
 	 * 
 	 * <p>
 	 * When hiding the panel, the message will also be reset. Change it with
 	 * {@link #setLoadingPanelMessage}.
 	 * </p>
 	 * 
 	 * @param visible
 	 *            If the panel should be hidden or shown.
 	 */
 	protected void setLoadingPanelVisible(boolean visible) {
 		if (visible) {
 			if (DOM.getElementById(GuiState.LOADINGPANEL_ID) == null) {
 				RootPanel.get().add(GuiState.loadingPanel);
 			}
 		} else {
 			if (DOM.getElementById(GuiState.LOADINGPANEL_ID) != null) {
 				GuiState.loadingPanel.removeFromParent();
 				setLoadingPanelMessage(null);
 			}
 		}
 	}
 
 	/**
 	 * Adds a linechart-graph of the {@code performance} to {@code panel}
 	 * 
 	 * @param panel
 	 *            The panel the chart will be added to
 	 * @param names
 	 *            List of names of the different plots in the chart
 	 * @param performance
 	 *            Values of the different plots
 	 * @param onLoad
 	 *            Callback to execute when the image with the graph has been
 	 *            loaded and added to the page.
 	 */
 	public void createGraph(Panel panel, ArrayList<String> names,
 			ArrayList<double[]> performance, AsyncCallback<Boolean> onLoad) {
 
 		graphVisualisator = new GraphVisualisator();
 		// Adds the graph to the Panel-parameter of
 		// visualisator.getOnLoadCallBack()
 
 		float windowHeight = Window.getClientHeight();
 		float windowWidth = Window.getClientWidth();
 
 		int graphHeight;
 		int graphWidth;
 
 		final float ratio = 5f / 3f; // w : h
 		final float percentage = 0.6f;
 
 		if (windowWidth / ratio < windowHeight) { // portrait
 			graphHeight = (int) (windowWidth * percentage / ratio);
 			graphWidth = (int) (windowWidth * percentage);
 		} else { // landscape
 			graphHeight = (int) (windowHeight * percentage);
 			graphWidth = (int) (windowHeight * percentage * ratio);
 		}
 
 		try {
 			VisualizationUtils.loadVisualizationApi(graphVisualisator
 					.createGraph(panel, names, performance, onLoad,
 							graphHeight, graphWidth), LineChart.PACKAGE);
 		} catch (Exception e) {
 			Window.alert(FingerpaintConstants.INSTANCE.loadingGraphFailed());
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * resets the current protocol and the protocol widgets
 	 */
 	public void resetProtocol() {
 		as.setProtocol(new MixingProtocol());
 		GuiState.labelProtocolRepresentation.setText("");
 		GuiState.labelProtocolRepresentation.setVisible(false);
 		GuiState.labelProtocolLabel.setVisible(false);
 		as.setNrSteps(NrStepsSpinner.DEFAULT_VALUE);
 		GuiState.nrStepsSpinner.setValue(NrStepsSpinner.DEFAULT_VALUE);
 		GuiState.mixNowButton.setEnabled(false);
 		GuiState.saveProtocolButton.setEnabled(false);
 		GuiState.saveResultsButton.setEnabled(false);
 	}
 
 	/**
 	 * Saves the initial distribution. Sends all current information about the
 	 * protocol and the distribution to the server. Displays the results on
 	 * screen.
 	 * 
 	 * @param protocol
 	 *            The protocol that should be executed.
 	 * @param nrSteps
 	 *            The number of times the protocol should be executed
 	 */
 	public void executeMixingRun(final MixingProtocol protocol,
 			final int nrSteps) {
 		setLoadingPanelMessage(FingerpaintConstants.INSTANCE.prepareData());
 		setLoadingPanelVisible(true);
 
 		// Now use timers, to make sure the loading panel is set up and shown
 		// correctly
 		// before we start doing some heavy calculations and simulation...
 		// Basically, we simply do a 'setTimeout' JavaScript call here
 		final Timer doEvenLaterTimer = new Timer() {
 			@Override
 			public void run() {
 				Simulation simulation = new Simulation(as.getMixerChoice(),
 						protocol, FingerpaintZipper.zip(
 								FingerpaintJsonizer.toString(as
 										.getInitialDistribution()))
 								.substring(1), nrSteps, false);
 
				TimeoutRpcRequestBuilder timeoutRpcRequestBuilder = new TimeoutRpcRequestBuilder();
 				SimulatorServiceAsync service = GWT
 						.create(SimulatorService.class);
 				((ServiceDefTarget) service)
 						.setRpcRequestBuilder(timeoutRpcRequestBuilder);
 				AsyncCallback<SimulationResult> callback = new AsyncCallback<SimulationResult>() {
 					@Override
 					public void onSuccess(SimulationResult result) {
 						as.getGeometry().drawDistribution(
 								result.getConcentrationVectors()[result
 										.getConcentrationVectors().length - 1]);
 						as.setSegregation(result.getSegregationPoints());
 						GuiState.saveResultsButton.setEnabled(true);
 						GuiState.viewSingleGraphButton.setEnabled(true);
 						setLoadingPanelVisible(false);
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						setLoadingPanelVisible(false);
 						if (caught instanceof RequestTimeoutException) {
 							showError(FingerpaintConstants.INSTANCE
 									.simulationRequestTimeout());
 						} else {
 							showError(FingerpaintConstants.INSTANCE
 									.notReachServer());
 						}
 					}
 				};
 				// Call the service
 				service.simulate(simulation, callback);
 			}
 		};
 
 		Timer doLaterTimer = new Timer() {
 			@Override
 			public void run() {
 				as.setInitialDistribution(as.getGeometry().getDistribution());
 
 				setLoadingPanelMessage(FingerpaintConstants.INSTANCE
 						.runSimulation());
 				doEvenLaterTimer.schedule(100);
 			}
 		};
 		doLaterTimer.schedule(100);
 	}
 }
