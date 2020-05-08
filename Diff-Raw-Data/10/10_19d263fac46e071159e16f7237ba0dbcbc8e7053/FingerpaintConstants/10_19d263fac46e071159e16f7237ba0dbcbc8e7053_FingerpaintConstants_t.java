 package nl.tue.fingerpaint.client.resources;
 
 import nl.tue.fingerpaint.client.gui.GuiState;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.Constants;
 
 /**
  * The {@code FingerpaintConstants} are a set of localised constants. They can
  * be used in the GUI for I18N for example.
  * 
  * @author Group Fingerpaint
  */
 public interface FingerpaintConstants extends Constants {
 	// --- PUBLIC GLOBALS -----------------------------------------------------
 	/**
 	 * An instance that can be used to obtain constants.
 	 */
 	public static final FingerpaintConstants INSTANCE = GWT
 			.create(FingerpaintConstants.class);
 
 	// --- CONSTANTS FOR LOADING ----------------------------------------------
 	/**
 	 * @return Localised string that indicates that the application is loading
 	 *         geometries and mixers from the server.
 	 */
 	@DefaultStringValue("Loading geometries and mixers...")
 	public String loadingGeometries();
 
 	/**
 	 * @return Localised string that indicates that the application is preparing
 	 *         the data to be send to the server.
 	 */
 	@DefaultStringValue("Preparing data...")
 	public String prepareData();
 
 	/**
 	 * @return Localised string that indicates that the application is busy
 	 *         running the simulation.
 	 */
 	@DefaultStringValue("Running the simulation. Please wait...")
 	public String runSimulation();
 
 	// --- CONSTANTS FOR FLEX TABLE -------------------------------------------
 	/**
 	 * @return Localised string that contains the text for the first row of the
 	 *         flextable {@link GuiState#resultsFlexTable}.
 	 */
 	@DefaultStringValue("File name")
 	public String flexFileName();
 
 	/**
 	 * @return Localised string that contains the text for the second row of the
 	 *         flextable {@link GuiState#resultsFlexTable}.
 	 */
 	@DefaultStringValue("Remove")
 	public String flexRemove();
 
 	// --- CONSTANTS FOR LABELS -----------------------------------------------
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#nrStepsLabel} label.
 	 */
 	@DefaultStringValue("Number of steps")
 	public String lblNrSteps();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#labelProtocolLabel} label.
 	 */
 	@DefaultStringValue("Protocol:")
 	public String lblProtocol();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#sizeLabel} label.
 	 */
 	@DefaultStringValue("Step size")
 	public String lblStepSize();
 	
 	/**
 	 * @return Localised string that contains the text for the
 	 * 			{@link GuiState#noFilesFoundLabel} label
 	 */
 	@DefaultStringValue("No saved files")
 	public String noFilesFound();
 
 	// -- CONSTANTS FOR SAVE MESSAGES -----------------------------------------
 	/**
 	 * @return Localised string that describes that an item has been saved
 	 *         successfully.
 	 */
 	@DefaultStringValue("Save successful.")
 	public String saveSuccess();
 
 	/**
 	 * @return Localised string that describes that an item could not be saved
 	 *         (because the name was already in use).
 	 */
 	@DefaultStringValue("This name is already in use. "
 			+ "Choose whether to overwrite existing file or to cancel.")
 	public String nameInUse();
 	
 	// -- CONSTANTS FOR DELETE MESSAGES -----------------------------------------
 	/**
 	 * @return Localised string that describes that an item has been saved
 	 *         successfully.
 	 */
 	@DefaultStringValue("Delete successful.")
 	public String deleteSuccess();
 	
 
 	// --- CONSTANTS FOR BUTTONS ----------------------------------------------
 	/**
 	 * @return Localised string that contains the text for
 	 *         {@link GuiState#squareDrawingTool}.
 	 */
 	@DefaultStringValue("square")
 	public String btnSquareDraw();
 
 	/**
 	 * @return Localised string that contains the text for
 	 *         {@link GuiState#circleDrawingTool}.
 	 */
 	@DefaultStringValue("circle")
 	public String btnCircleDraw();
 
 	/**
 	 * @return Localised string that contains the text for a save button.
 	 */
 	@DefaultStringValue("Save")
 	public String btnSave();
 
 	/**
 	 * @return Localised string that contains the text for a cancel button.
 	 */
	@DefaultStringValue("Close")
 	public String btnCancel();
 
 	/**
 	 * @return Localised string that contains the text for a close button.
 	 */
 	@DefaultStringValue("Close")
 	public String btnClose();
 
 	/**
 	 * @return Localised string that contains the text for an overwrite button.
 	 */
 	@DefaultStringValue("Overwrite")
 	public String btnOverwrite();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#toolSelectButton} button.
 	 */
 	@DefaultStringValue("Select Tool")
 	public String btnSelectTool();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#saveDistributionButton} button.
 	 */
 	@DefaultStringValue("Save Distribution")
 	public String btnSaveDist();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#loadInitDistButton} button.
 	 */
 	@DefaultStringValue("Load Initial Distribution")
 	public String btnLoadDist();
 	
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#removeInitDistButton} button.
 	 */
 	@DefaultStringValue("Remove Saved Distributions")
 	public String btnRemoveInitDistButton();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#loadInitDistButton} button.
 	 */
 	@DefaultStringValue("Reset Distribution")
 	public String btnResetDist();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#saveResultsButton} button.
 	 */
 	@DefaultStringValue("Save Results")
 	public String btnSaveResults();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#removeSavedResultsButton} button.
 	 */
 	@DefaultStringValue("Remove Saved Results")
 	public String btnRemoveResults();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#saveProtocolButton} button.
 	 */
 	@DefaultStringValue("Save Protocol")
 	public String btnSaveProt();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#loadProtocolButton} button.
 	 */
 	@DefaultStringValue("Load Mixing Protocol")
 	public String btnLoadProt();
 	
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#removeSavedProtButton} button.
 	 */
 	@DefaultStringValue("Remove Saved Protocols")
 	public String btnRemoveSavedProtButton();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#resetProtocolButton} button.
 	 */
 	@DefaultStringValue("Reset Protocol")
 	public String btnResetProt();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#mixNowButton} button.
 	 */
 	@DefaultStringValue("Mix Now")
 	public String btnMixNow();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#viewSingleGraphButton} button.
 	 */
 	@DefaultStringValue("View single graph")
 	public String btnViewSingleGraph();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#exportSingleGraphButton} button.
 	 */
 	@DefaultStringValue("Export graph")
 	public String btnExportGraph();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#exportDistributionButton} button.
 	 */
 	@DefaultStringValue("Export image")
 	public String btnExportDist();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#exportSingleGraphButton} button.
 	 */
 	@DefaultStringValue("Export graphs")
 	public String btnExportGraphs();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#comparePerformanceButton} button.
 	 */
 	@DefaultStringValue("Compare Performance")
 	public String btnComparePerfomance();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#compareButton} button.
 	 */
 	@DefaultStringValue("Compare")
 	public String btnCompare();
 
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#newCompareButton} button.
 	 */
 	@DefaultStringValue("New Comparison")
 	public String btnNewCompare();
 	
 	/**
 	 * @return Localised string that contains the text for the
 	 *         {@link GuiState#toggleDefineProtocol} toggle button.
 	 */
 	@DefaultStringValue("Define Protocol")
 	public String btnDefProt();	
 
 	// --- CONSTANTS FOR ERRORS -----------------------------------------------
 	/**
 	 * @return Localised string to indicate that a request timeout occurred on
 	 *         the simulation server.
 	 */
 	@DefaultStringValue("The simulation server did not respond in"
 			+ " time. Try again later.")
 	public String simulationRequestTimeout();
 
 	/**
 	 * @return Localised string to indicate that the server could not be
 	 *         reached.
 	 */
 	@DefaultStringValue("Could not reach the server, try again later.")
 	public String notReachServer();
 
 	/**
 	 * @return Localised string to indicate that an error has occurred.
 	 */
 	@DefaultStringValue("An error occurred!")
 	public String errorOccured();
 
 	/**
 	 * @return Localised string to indicate that the mixing performance graph
 	 *         could not be loaded.
 	 */
 	@DefaultStringValue("Loading graph failed.")
 	public String loadingGraphFailed();
 
 	/**
 	 * @return Localised string to indicate that saving an item was not
 	 *         successful because the local storage is full.
 	 */
 	@DefaultStringValue("Storage capacity exceeded.")
 	public String capacityExceeded();
 
 }
