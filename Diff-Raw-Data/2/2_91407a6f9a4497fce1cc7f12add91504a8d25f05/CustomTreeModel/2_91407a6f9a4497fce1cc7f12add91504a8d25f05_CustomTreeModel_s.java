 package nl.tue.fingerpaint.client.gui;
 
 import java.util.Arrays;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import nl.tue.fingerpaint.client.Fingerpaint;
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
 import nl.tue.fingerpaint.client.gui.spinners.CursorSizeSpinner;
 import nl.tue.fingerpaint.client.gui.spinners.NrStepsSpinner;
 import nl.tue.fingerpaint.client.gui.spinners.StepSizeSpinner;
 import nl.tue.fingerpaint.client.model.ApplicationState;
 import nl.tue.fingerpaint.client.model.Geometry.StepAddedListener;
 import nl.tue.fingerpaint.client.model.RectangleGeometry;
 import nl.tue.fingerpaint.client.serverdata.ServerDataCache;
 import nl.tue.fingerpaint.shared.GeometryNames;
 import nl.tue.fingerpaint.shared.model.MixingProtocol;
 import nl.tue.fingerpaint.shared.model.MixingStep;
 
 import com.google.gwt.cell.client.ClickableTextCell;
 import com.google.gwt.cell.client.ValueUpdater;
 import com.google.gwt.user.cellview.client.CellBrowser;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.gwt.view.client.TreeViewModel;
 
 /**
  * The model that defines the nodes in the {@link CellBrowser} that is used as a
  * main menu.
  */
 public class CustomTreeModel implements TreeViewModel {
 
 	/**
 	 * Number of levels in the tree. Is used to determine when the browser
 	 * should be closed.
 	 */
 	private final static int NUM_LEVELS = 2;
 
 	/** Reference to the "parent" class. Used for executing mixing runs. */
 	private Fingerpaint fp;
 	/**
 	 * Reference to the state of the application, to update stuff there when a
 	 * menu item is selected.
 	 */
 	private ApplicationState as;
 
 	/** A selection model that is shared along all levels. */
 	private final SingleSelectionModel<String> selectionModel = new SingleSelectionModel<String>();
 
 	/** Updater on the highest level. */
 	private final ValueUpdater<String> valueGeometryUpdater = new ValueUpdater<String>() {
 		@Override
 		public void update(String value) {
 			as.setGeometryChoice(value);
 			lastClickedLevel = 0;
 		}
 	};
 
 	/** Updater on level 1. */
 	private final ValueUpdater<String> valueMixerUpdater = new ValueUpdater<String>() {
 		@Override
 		public void update(String value) {
 			as.setMixerChoice(value);
 			lastClickedLevel = 1;
 		}
 	};
 
 	/** Indicate which level was clicked the last. */
 	private int lastClickedLevel = -1;
 
 	/**
 	 * Creates the chosen geometry.
 	 */
 	private void createGeometry() {
 		if (as.getGeometryChoice().equals(GeometryNames.RECT)) {
 			as.setGeometry(new RectangleGeometry(Window.getClientHeight(),
 					Window.getClientWidth(), 240, 400));
 		} else if (as.getGeometryChoice().equals(GeometryNames.SQR)) {
 			int size = Math.min(Window.getClientHeight(), Window.getClientWidth());
 			as.setGeometry(new RectangleGeometry(size - 20,	size - 20,
 					240, 240));
 			Logger.getLogger("").log(Level.INFO, "Length of distribution array: " + as.getGeometry().getDistribution().length);
 		} else { // No valid mixer was selected
 			Logger.getLogger("").log(Level.WARNING,
 					"Invalid geometry selected");
 		}
 	}
 
 	/**
 	 * Construct a specific {@link TreeViewModel} that can be used in the
 	 * {@link CellBrowser} of the Fingerpaint application.
 	 * 
 	 * @param parent
 	 *            A reference to the Fingerpaint class. Used to execute mixing
 	 *            protocols.
 	 * @param appState
 	 *            Reference to the model that holds the state of the
 	 *            application.
 	 */
 	public CustomTreeModel(Fingerpaint parent, ApplicationState appState) {
 		this.fp = parent;
 		this.as = appState;
 
 		selectionModel
 				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 					@Override
 					public void onSelectionChange(SelectionChangeEvent event) {
 						String selected = selectionModel.getSelectedObject();
 
 						if (selected != null) {
 							if (lastClickedLevel == NUM_LEVELS - 1) {
 								as.setMixerChoice(selected);
 
 								// "closes" Cellbrowser widget (clears whole
 								// rootpanel)
 								RootPanel.get().clear();
 
 								createGeometry();
 								createMixingWidgets();
 							} else if (lastClickedLevel == NUM_LEVELS - 2) {
 								as.setGeometryChoice(selected);
 							}
 						}
 					}
 				});
 	}
 
 	/**
 	 * Helper method that initialises the widgets for the mixing interface
 	 */
 	private void createMixingWidgets() {
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
 		GuiState.squareDrawingTool = new SquareDrawingToolToggleButton(fp, as);
 		GuiState.circleDrawingTool = new CircleDrawingToolToggleButton(fp, as);
 		GuiState.mainMenuPanel.add(GuiState.toolSelectButton);
 
 		// Initialise toggleButton and add to
 		// menuPanel
 		GuiState.toggleColor = new ToggleColourButton(as);
 		GuiState.mainMenuPanel.add(GuiState.toggleColor);
 
 		// Initialise the distribution buttons and add a button to access those
 		// to the menu panel. Also add the 'clear canvas' to the main menu
 		GuiState.resetDistButton = new ResetDistButton(as);
 		GuiState.mainMenuPanel.add(GuiState.resetDistButton);
 		GuiState.saveDistributionButton = new SaveDistributionButton(fp);
 		GuiState.loadInitDistButton = new LoadInitDistButton(as);
 		GuiState.loadInitDistCellList = new LoadInitDistCellList(as);
 		GuiState.removeInitDistButton = new RemoveInitDistButton(as);
 		GuiState.exportDistributionButton = new ExportDistributionButton(as);
 		GuiState.mainMenuPanel.add(GuiState.distributionsButton);
 
 		// Initialise the saveResultsButton and add it to the menuPanel
 		GuiState.saveResultsButton = new SaveResultsButton(fp);
 		GuiState.saveResultsButton.setEnabled(false);
 		GuiState.mainMenuPanel.add(GuiState.saveResultsButton);
 
 		// Initialise panel to save items
 		GuiState.overwriteSaveButton = new OverwriteSaveButton(fp);
 		GuiState.saveItemPanelButton = new SaveItemPanelButton(fp);
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
 
 		//Initialise the LoadResultsCellList and add the loadResultsButton
		GuiState.menuPanel.add(GuiState.loadResultsButton);
 		GuiState.LoadResultsCellList = new LoadResultsCellList(fp, as);
 		
 		// Initialise the removeSavedResultsButton and add it to the
 		// menuPanel
 		GuiState.removeResultsPanel.add(GuiState.removeResultsVerticalPanel);
 		GuiState.mainMenuPanel.add(GuiState.removeSavedResultsButton);
 
 		GuiState.viewSingleGraphButton = new ViewSingleGraphButton(fp, as);
 		GuiState.exportSingleGraphButton = new ExportSingleGraphButton(fp);
 		GuiState.mainMenuPanel.add(GuiState.viewSingleGraphButton);
 
 		// Initialise the comparePerformanceButton and add it to the
 		// menuPanel
 		// createComparePerformanceButton();
 		GuiState.comparePerformanceButton = new ComparePerformanceButton(fp);
 		GuiState.mainMenuPanel.add(GuiState.comparePerformanceButton);
 
 		// Initialise a spinner for changing the length of a mixing protocol
 		// step and add to menuPanel.
 		GuiState.sizeSpinner = new StepSizeSpinner(as);
 		GuiState.mainMenuPanel.add(GuiState.sizeLabel);
 		GuiState.mainMenuPanel.add(GuiState.sizeSpinner);
 
 		// Initialise the toggleButton that indicates whether a protocol is
 		// being defined, or single steps have to be executed and add to
 		// menu panel
 		GuiState.toggleDefineProtocol = new ToggleDefineProtocol(fp);
 		GuiState.mainMenuPanel.add(GuiState.toggleDefineProtocol);
 
 		// Initialise a spinner for #steps
 		GuiState.nrStepsSpinner = new NrStepsSpinner(as);
 
 		// Initialise the resetProtocol button
 		GuiState.resetProtocolButton = new ResetProtocolButton(fp);
 
 		// Initialise the saveProtocolButton and add it to the menuPanel
 		GuiState.saveProtocolButton = new SaveProtocolButton(fp);
 
 		// Initialise the mixNow button
 		GuiState.mixNowButton = new MixNowButton(fp, as);
 
 		// Initialise the loadProtocolButton
 		GuiState.loadProtocolButton = new LoadProtocolButton(as);
 		GuiState.loadProtocolCellList = new LoadProtocolCellList(as);
 
 		// Initialise the loadProtocolButton
 		GuiState.removeSavedProtButton = new RemoveSavedProtButton(as);		
 
 		// Add all the protocol widgets to the menuPanel and hide them
 		// initially.
 		VerticalPanel protocolPanel = new VerticalPanel();
 		protocolPanel.add(GuiState.nrStepsLabel);
 		protocolPanel.add(GuiState.nrStepsSpinner);
 		protocolPanel.add(GuiState.labelProtocolLabel);
 		protocolPanel.add(GuiState.labelProtocolRepresentation);
 		protocolPanel.add(GuiState.mixNowButton);
 		protocolPanel.add(GuiState.resetProtocolButton);
 		protocolPanel.add(GuiState.saveProtocolButton);
 		protocolPanel.add(GuiState.loadProtocolButton);
 		protocolPanel.add(GuiState.removeSavedProtButton);
 		GuiState.protocolPanelContainer.add(protocolPanel);
 		GuiState.mainMenuPanel.add(GuiState.protocolPanelContainer);
 
 		fp.setProtocolWidgetsVisible(false);
 
 		// Add canvas and menuPanel to the page
 		RootPanel.get().add(as.getGeometry().getCanvas());
 		
 		GuiState.menuPanelInnerWrapper.add(GuiState.mainMenuPanel);
 		GuiState.menuPanelInnerWrapper.add(GuiState.subLevel1MenuPanel);
 		GuiState.menuPanelInnerWrapper.add(GuiState.subLevel2MenuPanel);
 		GuiState.menuPanelOuterWrapper.add(GuiState.menuPanelInnerWrapper);
 		RootPanel.get().add(GuiState.menuPanelOuterWrapper);
 		
 		GuiState.menuToggleButton.refreshMenuSize();
 		RootPanel.get().add(GuiState.menuToggleButton);
 	}
 
 	/**
 	 * Get the {@link com.google.gwt.view.client.TreeViewModel.NodeInfo} that
 	 * provides the children of the specified value.
 	 */
 	public <T> NodeInfo<?> getNodeInfo(T value) {
 		// When the Tree is being initialised, the last clicked level will
 		// be -1,
 		// in other cases, we need to load the level after the currently
 		// clicked one.
 		if (lastClickedLevel < 0) {
 			// LEVEL 0. - Geometry
 			// We passed null as the root value. Return the Geometries.
 
 			// Create a data provider that contains the list of Geometries.
 			ListDataProvider<String> dataProvider = new ListDataProvider<String>(
 					Arrays.asList(ServerDataCache.getGeometries()));
 
 			// Return a node info that pairs the data provider and the cell.
 			return new DefaultNodeInfo<String>(dataProvider,
 					new ClickableTextCell(), selectionModel,
 					valueGeometryUpdater);
 		} else if (lastClickedLevel == 0) {
 			// LEVEL 1 - Mixer (leaf)
 
 			// We want the children of the Geometry. Return the mixers.
 			ListDataProvider<String> dataProvider = new ListDataProvider<String>(
 					Arrays.asList(ServerDataCache
 							.getMixersForGeometry((String) value)));
 
 			// Use the shared selection model.
 			return new DefaultNodeInfo<String>(dataProvider,
 					new ClickableTextCell(), selectionModel, valueMixerUpdater);
 
 		}
 		return null;
 	}
 
 	/**
 	 * Check if the specified value represents a leaf node. Leaf nodes cannot be
 	 * opened.
 	 */
 	// You can define your own definition of leaf-node here.
 	public boolean isLeaf(Object value) {
 		return lastClickedLevel == NUM_LEVELS - 1;
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
 		GuiState.labelProtocolLabel.setVisible(true);
 		if (!GuiState.toggleDefineProtocol.isHidden()) {
 			step.setStepSize(as.getStepSize());
 			as.addMixingStep(step);
 			updateProtocolLabel(step);
 			GuiState.mixNowButton.setEnabled(true);
 			GuiState.saveProtocolButton.setEnabled(true);
 		} else {
 			MixingProtocol protocol = new MixingProtocol();
 			step.setStepSize(as.getStepSize());
 			protocol.addStep(step);
 			fp.executeMixingRun(protocol, 1, false);
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
 		String stepString = step.toString();
 		if (stepString.charAt(0) == 'B' || stepString.charAt(0) == 'T') {
 			stepString = "&nbsp;" + stepString;
 		}
 
 		GuiState.labelProtocolRepresentation.setVisible(true);
 		GuiState.labelProtocolRepresentation.getElement().setInnerHTML(
 				oldProtocol + stepString + " ");
 	}
 }
