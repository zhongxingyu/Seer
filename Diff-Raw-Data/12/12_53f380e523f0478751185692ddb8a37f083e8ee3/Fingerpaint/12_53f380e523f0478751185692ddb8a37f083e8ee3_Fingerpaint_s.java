 package nl.tue.fingerpaint.client;
 
 import java.util.ArrayList;
 
 import nl.tue.fingerpaint.client.gui.CustomTreeModel;
 import nl.tue.fingerpaint.client.gui.GraphVisualisator;
 import nl.tue.fingerpaint.client.gui.GuiState;
 import nl.tue.fingerpaint.client.gui.buttons.BackStopDefiningProtocolButton;
 import nl.tue.fingerpaint.client.gui.buttons.CircleDrawingToolToggleButton;
 import nl.tue.fingerpaint.client.gui.buttons.ComparePerformanceButton;
 import nl.tue.fingerpaint.client.gui.buttons.ExportDistributionButton;
 import nl.tue.fingerpaint.client.gui.buttons.ExportSingleGraphButton;
 import nl.tue.fingerpaint.client.gui.buttons.LoadInitDistButton;
 import nl.tue.fingerpaint.client.gui.buttons.LoadProtocolButton;
 import nl.tue.fingerpaint.client.gui.buttons.MixNowButton;
 import nl.tue.fingerpaint.client.gui.buttons.OverwriteSaveButton;
 import nl.tue.fingerpaint.client.gui.buttons.RemoveInitDistButton;
 import nl.tue.fingerpaint.client.gui.buttons.RemoveSavedProtButton;
 import nl.tue.fingerpaint.client.gui.buttons.ResetDistButton;
 import nl.tue.fingerpaint.client.gui.buttons.ResetProtocolButton;
 import nl.tue.fingerpaint.client.gui.buttons.SaveDistributionButton;
 import nl.tue.fingerpaint.client.gui.buttons.SaveItemPanelButton;
 import nl.tue.fingerpaint.client.gui.buttons.SaveProtocolButton;
 import nl.tue.fingerpaint.client.gui.buttons.SaveResultsButton;
 import nl.tue.fingerpaint.client.gui.buttons.SquareDrawingToolToggleButton;
 import nl.tue.fingerpaint.client.gui.buttons.ToggleColourButton;
 import nl.tue.fingerpaint.client.gui.buttons.ToggleDefineProtocol;
 import nl.tue.fingerpaint.client.gui.buttons.ViewSingleGraphButton;
 import nl.tue.fingerpaint.client.gui.celllists.LoadInitDistCellList;
 import nl.tue.fingerpaint.client.gui.celllists.LoadProtocolCellList;
 import nl.tue.fingerpaint.client.gui.celllists.LoadResultsCellList;
 import nl.tue.fingerpaint.client.gui.panels.NotificationPopupPanel;
 import nl.tue.fingerpaint.client.gui.spinners.CursorSizeSpinner;
 import nl.tue.fingerpaint.client.gui.spinners.NrStepsSpinner;
 import nl.tue.fingerpaint.client.gui.spinners.StepSizeSpinner;
 import nl.tue.fingerpaint.client.model.ApplicationState;
 import nl.tue.fingerpaint.client.model.Geometry.StepAddedListener;
 import nl.tue.fingerpaint.client.model.RectangleGeometry;
 import nl.tue.fingerpaint.client.resources.FingerpaintCellBrowserResources;
 import nl.tue.fingerpaint.client.resources.FingerpaintConstants;
 import nl.tue.fingerpaint.client.resources.FingerpaintResources;
 import nl.tue.fingerpaint.client.serverdata.ServerDataCache;
 import nl.tue.fingerpaint.client.storage.FileExporter;
 import nl.tue.fingerpaint.client.storage.FingerpaintJsonizer;
 import nl.tue.fingerpaint.client.storage.FingerpaintZipper;
 import nl.tue.fingerpaint.client.storage.ResultStorage;
 import nl.tue.fingerpaint.client.storage.StorageManager;
 import nl.tue.fingerpaint.shared.GeometryNames;
 import nl.tue.fingerpaint.shared.MixerNames;
 import nl.tue.fingerpaint.shared.model.MixingProtocol;
 import nl.tue.fingerpaint.shared.model.MixingStep;
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
 import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
 
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
 	 * Timer to execute on resize.
 	 */
 	protected Timer timer = new Timer() {
 		@Override
 		public void run() {
 			if (as.getGeometry() != null) {
 				as.getGeometry().resize(Window.getClientWidth(),
 						Window.getClientHeight());
 			}
 		}
 	};
 
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
 				// Here, we load the mixing widgets immediately.
 				// If more geometries/mixers are added to the application,
 				// loadMenu() should be called here instead of below code
 				as.setGeometryChoice(GeometryNames.RECT);
 				as.setMixerChoice(MixerNames.RectMixers.DEFAULT);
 				as.setGeometry(new RectangleGeometry(Window.getClientHeight(),
 					Window.getClientWidth(), 240, 400));
 				createMixingWidgets();
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
 				timer.cancel();
 				timer.schedule(250);
 				
 				centerPopupPanels();
 			}
 		});
 	}
 
 	/**
 	 * <p>
 	 * Center all currently visible {@link PopupPanel PopupPanels} both
 	 * horizontally and vertically in the screen.
 	 * </p>
 	 * 
 	 * <p>
 	 * Also positions the notifications where they belong.
 	 * </p>
 	 */
 	public void centerPopupPanels() {
 		if (GuiState.removeResultsPanel.isShowing()) {
 			GuiState.removeResultsPanel.center();
 		}
 		if (GuiState.saveItemPanel.isShowing()) {
 			GuiState.saveItemPanel.center();
 		}
 		if (GuiState.overwriteSavePanel.isShowing()) {
 			GuiState.overwriteSavePanel.center();
 		}
 		if (GuiState.loadPanel.isShowing()) {
 			if (GuiState.loadPanel.isLoading()) {
 				GuiState.loadPanel.setTopLeft();
 			} else {
 				GuiState.loadPanel.center();
 			}
 		}
 		if (GuiState.viewSingleGraphPopupPanel.isShowing()) {
 			GuiState.viewSingleGraphPopupPanel.center();
 		}
 		if (GuiState.compareSelectPopupPanel.isShowing()) {
 			GuiState.compareSelectPopupPanel.center();
 		}
 		if (GuiState.comparePopupPanel.isShowing()) {
 			GuiState.comparePopupPanel.center();
 		}
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
 		CellBrowser.Builder<Object> treeBuilder = new CellBrowser.Builder<Object>(model, null);
 		treeBuilder.resources((CellBrowser.Resources) GWT.create(FingerpaintCellBrowserResources.class));
 		CellBrowser tree = treeBuilder.build();
 		
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
 	 * this method is used to acquire the size of the current cursor in pixels
 	 * 
 	 * @return cursorSizeSpinner.getValue()
 	 */
 	public int getCursorSize() {
 		return (int) GuiState.cursorSizeSpinner.getValue();
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
  	 * @return <ul>
 	 *            <li>{@code SAVE_SUCCESSFUL} If saving was successful.</li>
 	 *            <li>{@code NOT_INITIALISED_ERROR} If the local storage is not initialised.</li>
 	 *            <li>{@code NAME_IN_USE_ERROR} If the name is already in use.</li>
 	 *            <li>{@code QUOTA_EXCEEDED_ERROR} If the local storage is full.</li>
 	 *            <li>{@code NONEXISTANT_KEY_ERROR} If the key does not exist.</li>
 	 *            <li>{@code UNKNOWN_ERROR} If an error occurs, other than those above.</li>
 	 *         </ul> 
 	 */
 	public int save(String name, boolean canOverwrite) {
 		if (lastSaveButtonClicked.equals(StorageManager.KEY_INITDIST)) {
 			return StorageManager.INSTANCE.putDistribution(
 					as.getGeometryChoice(), name,
 					as.getGeometry().getDistribution(), canOverwrite);
 		} else if (lastSaveButtonClicked.equals(StorageManager.KEY_PROTOCOLS)) {
 			return StorageManager.INSTANCE.putProtocol(
 					as.getGeometryChoice(), name,
 					as.getProtocol(), canOverwrite);
 		} else if (lastSaveButtonClicked.equals(StorageManager.KEY_RESULTS)) {
 			ResultStorage result = new ResultStorage();
 			result.setGeometry(as.getGeometryChoice());
 			result.setMixer(as.getMixerChoice());
 			result.setDistribution(as.getGeometry().getDistribution());
 			result.setMixingProtocol(as.getProtocol());
 			result.setSegregation(as.getSegregation());
 			result.setNrSteps(as.getNrSteps());
 
 			try {
 				return StorageManager.INSTANCE.putResult(name, result,
 						canOverwrite);
 			} catch (Exception e) {
 				GWT.log("Saving results encountered an error", e);
 				return StorageManager.UNKNOWN_ERROR;
 			}
 		}
 		return StorageManager.UNKNOWN_ERROR;
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
 	 * Initialise the widgets for the mixing interface.
 	 */
 	public void createMixingWidgets() {
 		// Initialise a listener for when a new step is entered to the
 		// protocol
 		StepAddedListener l = new StepAddedListener() {
 			@Override
 			public void onStepAdded(MixingStep step) {
 				addStep(step);
 			}
 		};
 		as.getGeometry().addStepAddedListener(l);
 
 		// Initialise the cursorSizeSpinner so it can be added to the tool
 		// selector popup
 		GuiState.cursorSizeSpinner = new CursorSizeSpinner(as);
 
 		// Initialise the toolSelectButton and add it to the menu panel
 		// Also intialise the widgets in the submenu that this button toggles
 		GuiState.squareDrawingTool = new SquareDrawingToolToggleButton(this, as);
 		GuiState.circleDrawingTool = new CircleDrawingToolToggleButton(this, as);
 		GuiState.menuPanels[0].add(GuiState.toolSelectButton);
 
 		// Initialise toggleButton and add to
 		// menuPanel
 		GuiState.toggleColour = new ToggleColourButton(as, ToggleColourButton.TOGGLE_COLOUR);
 		GuiState.toolMenuToggleColour = new ToggleColourButton(as, ToggleColourButton.TOGGLE_COLOUR_TOOL_MENU);
 		// GuiState.menuPanels[0].add(GuiState.toggleColour);
 
 		// Initialise the distribution buttons and add a button to access those
 		// to the menu panel. Also add the 'clear canvas' to the main menu
 		GuiState.resetDistButton = new ResetDistButton(as);
 		GuiState.menuPanels[0].add(GuiState.resetDistButton);
 		GuiState.saveDistributionButton = new SaveDistributionButton(this);
 		GuiState.loadInitDistButton = new LoadInitDistButton(as);
 		GuiState.loadInitDistCellList = new LoadInitDistCellList(as);
 		GuiState.removeInitDistButton = new RemoveInitDistButton(as);
 		GuiState.exportDistributionButton = new ExportDistributionButton(as);
 		GuiState.menuPanels[0].add(GuiState.distributionsButton);
 
 		// Initialise the results buttons and add a button to access those
 		// to the menu panel.
 		GuiState.saveResultsButton = new SaveResultsButton(this);
 		GuiState.saveResultsButton.setEnabled(false);
 
 		// Initialise panel to save items
 		GuiState.overwriteSaveButton = new OverwriteSaveButton(this);
 		GuiState.saveItemPanelButton = new SaveItemPanelButton(this);
 		GuiState.saveItemPanel.add(GuiState.saveItemVerticalPanel);
 		GuiState.saveItemVerticalPanel.add(GuiState.saveNameTextBox);
 		GuiState.saveItemVerticalPanel.add(GuiState.saveButtonsPanel);
 		GuiState.saveButtonsPanel.add(GuiState.saveItemPanelButton);
 		GuiState.saveButtonsPanel.add(GuiState.cancelSaveResultsButton);
 
 		// Initialise panel to overwrite already saved items
 		GuiState.overwriteSavePanel.add(GuiState.overwriteSaveVerticalPanel);
 		GuiState.overwriteSaveVerticalPanel.add(GuiState.saveMessageLabel);
 		GuiState.overwriteSaveVerticalPanel.add(GuiState.overwriteButtonsPanel);
 		GuiState.overwriteButtonsPanel.add(GuiState.closeSaveButton);
 
 		// Initialise the LoadResultsCellList and add the loadResultsButton
 		GuiState.LoadResultsCellList = new LoadResultsCellList(this, as);
 
 		GuiState.removeResultsPanel.add(GuiState.removeResultsVerticalPanel);
 
 		GuiState.menuPanels[0].add(GuiState.resultsButton);
 
 		// Initialise view single graph button
 		GuiState.viewSingleGraphButton = new ViewSingleGraphButton(this, as);
 		GuiState.exportSingleGraphButton = new ExportSingleGraphButton(this);
 
 		// Initialise the comparePerformanceButton
 		GuiState.comparePerformanceButton = new ComparePerformanceButton(this);
 
 		// Initialise a spinner for changing the length of a mixing protocol
 		// step and add to menuPanel.
 		GuiState.sizeSpinner = new StepSizeSpinner(as);
 		GuiState.sizeProtocolMenuSpinner = new StepSizeSpinner(as,
 				"sizeProtocolMenuSpinner");
 		GuiState.menuPanels[0].add(GuiState.sizeLabel);
 		GuiState.menuPanels[0].add(GuiState.sizeSpinner);
 
 		// Add a button with which the protocol submenu can be accessed
 		GuiState.toggleDefineProtocol = new ToggleDefineProtocol(as);
 		GuiState.menuPanels[0].add(GuiState.toggleDefineProtocol);
 
 		// Initialise a spinner for #steps
 		GuiState.nrStepsSpinner = new NrStepsSpinner(as);
 
 		// Initialise the resetProtocol button
 		GuiState.resetProtocolButton = new ResetProtocolButton(this);
 
 		// Initialise the saveProtocolButton and add it to the menuPanel
 		GuiState.saveProtocolButton = new SaveProtocolButton(this);
 
 		// Initialise the mixNow button
 		GuiState.mixNowButton = new MixNowButton(this, as);
 
 		// Initialise the loadProtocolButton
 		GuiState.loadProtocolButton = new LoadProtocolButton(as);
 		GuiState.loadProtocolCellList = new LoadProtocolCellList(as);
 
 		// Initialise the remove protocol button
 		GuiState.removeSavedProtButton = new RemoveSavedProtButton(as);
 
 		// Initiliase the button to leave the protocol submenu
 		GuiState.backStopDefiningProtocol = new BackStopDefiningProtocolButton(
 				as);
 
 		// Add canvas and menuPanel to the page
 		RootPanel.get().add(as.getGeometry().getCanvas());
 
 		for (int i = 0; i < GuiState.menuPanels.length; i++) {
 			GuiState.menuPanelInnerWrapper.add(GuiState.menuPanels[i]);
 		}
 		GuiState.menuPanelOuterWrapper.add(GuiState.menuPanelInnerWrapper);
 		RootPanel.get().add(GuiState.menuPanelOuterWrapper);
 
 		GuiState.menuToggleButton.refreshMenuSize();
 		RootPanel.get().add(GuiState.menuToggleButton);
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
 							graphHeight, graphWidth), CoreChart.PACKAGE);
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
 		as.setNrSteps(NrStepsSpinner.DEFAULT_VALUE);
 		GuiState.nrStepsSpinner.setValue(NrStepsSpinner.DEFAULT_VALUE);
 		GuiState.mixNowButton.setEnabled(false);
 		GuiState.saveProtocolButton.setEnabled(false);
 		GuiState.saveResultsButton.setEnabled(false);
 		GuiState.viewSingleGraphButton.setEnabled(false);
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
 	 * @param mixingRun
 	 *            {@code true} if a complete run is executed, {@code false} if
 	 *            this is only a single step.
 	 */
 	public void executeMixingRun(final MixingProtocol protocol,
 			final int nrSteps, final boolean mixingRun) {
 		setLoadingPanelMessage(FingerpaintConstants.INSTANCE.prepareData());
 		setLoadingPanelVisible(true);
 
 		// Now use timers, to make sure the loading panel is set up and shown
 		// correctly
 		// before we start doing some heavy calculations and simulation...
 		// Basically, we simply do a 'setTimeout' JavaScript call here
 		final Timer doEvenLaterTimer = new Timer() {
 			@Override
 			public void run() {
 				try {
 					Simulation simulation = new Simulation(as.getGeometryChoice(),
 							as.getMixerChoice(),
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
 							setLoadingPanelVisible(false);
 							if (mixingRun) {
 								GuiState.saveResultsButton.setEnabled(true);
 								GuiState.viewSingleGraphButton.setEnabled(true);
 							}
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
 				} catch (UnsupportedOperationException e) {
 					setLoadingPanelVisible(false);
 					new NotificationPopupPanel(FingerpaintConstants.INSTANCE
 							.geometryUnsupported())
 					        .show(GuiState.UNSUPPORTED_GEOM_TIMEOUT);
 				}
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
 	
 	/**
 	 * If the {@code Define Protocol} checkbox is ticked, this method adds a new
 	 * {@code MixingStep} to the mixing protocol, and updates the text area
 	 * {@code taProtocolRepresentation} accordingly.
 	 * 
 	 * @param step
 	 *            The {@code MixingStep} to be added.
 	 */
 	private void addStep(MixingStep step) {
 		GuiState.saveResultsButton.setEnabled(false);
 		GuiState.viewSingleGraphButton.setEnabled(false);
 		if (as.isDefiningProtocol()) {
 			step.setStepSize(as.getStepSize());
 			as.addMixingStep(step);
 			updateProtocolLabel(step);
 			GuiState.mixNowButton.setEnabled(true);
 			GuiState.saveProtocolButton.setEnabled(true);
 		} else {
 			MixingProtocol protocol = new MixingProtocol();
 			step.setStepSize(as.getStepSize());
 			protocol.addStep(step);
 			executeMixingRun(protocol, 1, false);
 		}
 	}
 
 	/**
 	 * Updates the protocol label to show the textual representation of
 	 * {@code step} and adds this to the existing steps in the protocol.
 	 * 
 	 * @param step
 	 *            The new {@code Step} of which the textual representation
 	 *            should be added.
 	 */
 	private void updateProtocolLabel(MixingStep step) {
 		String oldProtocol = GuiState.labelProtocolRepresentation.getText();
 
 		GuiState.labelProtocolRepresentation.setVisible(true);
 		GuiState.labelProtocolRepresentation.getElement().setInnerHTML(
 				oldProtocol + step.toString() + " ");
 	}
 }
