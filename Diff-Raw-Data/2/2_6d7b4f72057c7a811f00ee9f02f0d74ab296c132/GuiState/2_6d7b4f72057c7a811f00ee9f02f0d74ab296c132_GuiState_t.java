 package nl.tue.fingerpaint.client.gui;
 
 import nl.tue.fingerpaint.client.gui.buttons.CancelCompareButton;
 import nl.tue.fingerpaint.client.gui.buttons.CancelSaveResultsButton;
 import nl.tue.fingerpaint.client.gui.buttons.CloseCompareButton;
 import nl.tue.fingerpaint.client.gui.buttons.CloseLoadButton;
 import nl.tue.fingerpaint.client.gui.buttons.CloseResultsButton;
 import nl.tue.fingerpaint.client.gui.buttons.CloseSaveButton;
 import nl.tue.fingerpaint.client.gui.buttons.CloseSingleGraphViewButton;
 import nl.tue.fingerpaint.client.gui.buttons.CompareButton;
 import nl.tue.fingerpaint.client.gui.buttons.ComparePerformanceButton;
 import nl.tue.fingerpaint.client.gui.buttons.ExportDistributionButton;
 import nl.tue.fingerpaint.client.gui.buttons.ExportMultipleGraphsButton;
 import nl.tue.fingerpaint.client.gui.buttons.ExportSingleGraphButton;
 import nl.tue.fingerpaint.client.gui.buttons.LoadInitDistButton;
 import nl.tue.fingerpaint.client.gui.buttons.LoadProtocolButton;
 import nl.tue.fingerpaint.client.gui.buttons.MenuToggleButton;
 import nl.tue.fingerpaint.client.gui.buttons.MixNowButton;
 import nl.tue.fingerpaint.client.gui.buttons.NewCompareButton;
 import nl.tue.fingerpaint.client.gui.buttons.OverwriteSaveButton;
 import nl.tue.fingerpaint.client.gui.buttons.RemoveSavedResultsButton;
 import nl.tue.fingerpaint.client.gui.buttons.ResetDistButton;
 import nl.tue.fingerpaint.client.gui.buttons.ResetProtocolButton;
 import nl.tue.fingerpaint.client.gui.buttons.SaveDistributionButton;
 import nl.tue.fingerpaint.client.gui.buttons.SaveItemPanelButton;
 import nl.tue.fingerpaint.client.gui.buttons.SaveProtocolButton;
 import nl.tue.fingerpaint.client.gui.buttons.SaveResultsButton;
 import nl.tue.fingerpaint.client.gui.buttons.SquareDrawingToolToggleButton;
 import nl.tue.fingerpaint.client.gui.buttons.ToggleColourButton;
 import nl.tue.fingerpaint.client.gui.buttons.ToolSelectButton;
 import nl.tue.fingerpaint.client.gui.buttons.ViewSingleGraphButton;
 import nl.tue.fingerpaint.client.gui.celllists.CompareSelectPopupCellList;
 import nl.tue.fingerpaint.client.gui.celllists.LoadInitDistCellList;
 import nl.tue.fingerpaint.client.gui.celllists.LoadProtocolCellList;
 import nl.tue.fingerpaint.client.gui.checkboxes.DefineProtocolCheckBox;
 import nl.tue.fingerpaint.client.gui.flextables.ResultsFlexTable;
 import nl.tue.fingerpaint.client.gui.labels.ProtocolRepresentationLabel;
 import nl.tue.fingerpaint.client.gui.labels.SaveMessageLabel;
 import nl.tue.fingerpaint.client.gui.panels.LoadPopupPanel;
 import nl.tue.fingerpaint.client.gui.panels.ProtocolPanelContainer;
 import nl.tue.fingerpaint.client.gui.panels.RemoveResultsPopupPanel;
 import nl.tue.fingerpaint.client.gui.panels.SaveItemPopupPanel;
 import nl.tue.fingerpaint.client.gui.panels.ViewSingleGraphPopupPanel;
 import nl.tue.fingerpaint.client.gui.spinners.CursorSizeSpinner;
 import nl.tue.fingerpaint.client.gui.spinners.NrStepsSpinner;
 import nl.tue.fingerpaint.client.gui.spinners.StepSizeSpinner;
 import nl.tue.fingerpaint.client.gui.textboxes.SaveNameTextBox;
 import nl.tue.fingerpaint.client.resources.FingerpaintConstants;
 
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.ToggleButton;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * <p>
  * A class that contains references to all the GUI elements used in the
  * Fingerpaint application.
  * </p>
  * <p>
  * All widgets that can be initialised, must be initialised in this class.
  * </p>
  * 
  * @author Group Fingerpaint
  */
 public class GuiState {
 	// --- PUBLIC GLOBALS -----------------------------------------------------
 	/** The ID for the loading panel. */
 	public static final String LOADINGPANEL_ID = "loading-overlay";
 
 	/** The ID for the message to be displayed in the loading panel. */
 	public static final String LOADINGPANEL_MESSAGE_ID = "loading-overlay-message";
 
 	/**
 	 * Stores how long in milliseconds a SAVE_SUCCESS_MESSAGE should be shown in
 	 * a NotificationPanel.
 	 */
 	public static final int SAVE_SUCCESS_TIMEOUT = 2000;
 
 	// --- PROTECTED GLOBALS --------------------------------------------------
 	/*
 	 * The below initialisation parameters for the cursorSizeSpinner represent
 	 * cursor pixels.
 	 */
 	// TODO: determine good value for MIN and MAX.
 	/** The default value of {@link #cursorSizeSpinner}. */
 	protected static final double CURSOR_DEFAULT = 3.0;
 	/** The rate of {@link #cursorSizeSpinner}. */
 	protected static final double CURSOR_RATE = 1.0;
 	/** The minimum value of {@link #cursorSizeSpinner}. */
 	protected static final double CURSOR_MIN = 1.0;
 	/** The maximum value of {@link #cursorSizeSpinner}. */
 	protected static final double CURSOR_MAX = 50.0;
 
 	// --- LOADING APPLICATION WIDGETS ----------------------------------------
 	/**
 	 * Panel that covers the entire application and blocks the user from //
 	 * accessing other features.
 	 */
 	public static FlowPanel loadingPanel = new FlowPanel();
 
 	/**
 	 * The message to be shown during the loading animation of the loading
 	 * panel.
 	 */
 	public static Label loadingPanelMessage = new Label();
 
 	// --- MENU WIDGETS -------------------------------------------------------
 	/** Vertical panel to contain all menu items. */
 	public static VerticalPanel menuPanel = new VerticalPanel();
 
 	/** Wrapper for the {@link #menuPanel}, used in animation. */
 	public static SimplePanel menuPanelWrapper = new SimplePanel();
 
 	/** Button to toggle whether the menu is visible. */
 	public static MenuToggleButton menuToggleButton = new MenuToggleButton(
 			menuPanelWrapper);
 
 	// --- DRAWING TOOL WIDGETS -----------------------------------------------
 	/**
 	 * Pop-up panel which contains options for selecting a different drawing
 	 * tool.
 	 */
 	public static PopupPanel toolSelector = new PopupPanel(true);
 
 	/**
 	 * Panel in the pop-up panel to separate the tool selection and size
 	 * selection options for the drawing tool.
 	 */
 	public static HorizontalPanel popupPanelPanel = new HorizontalPanel();
 
 	/** Panel in the pop-up panel that contains the different drawing tools. */
 	public static VerticalPanel popupPanelMenu = new VerticalPanel();
 
 	/** Numberspinner to change the size of the drawing tool. */
 	public static CursorSizeSpinner cursorSizeSpinner;
 
 	/** Button to toggle between black and white drawing colour. */
 	public static ToggleColourButton toggleColor;
 
 	/** Button to change the shape of the selected drawing tool. */
 	// TODO: Change this to a button on which the current tool is drawn
 	public static ToolSelectButton toolSelectButton = new ToolSelectButton();
 
 	/** Button to select the square-shaped drawing tool. */
 	// TODO: Change this to a button on which a square is drawn
 	public static SquareDrawingToolToggleButton squareDrawingTool;
 
 	/** Button to select the circle-shaped drawing tool. */
 	// TODO: Change this to a button on which a circle is drawn
 	public static ToggleButton circleDrawingTool = new ToggleButton(
 			FingerpaintConstants.INSTANCE.btnCircleDraw(),
 			FingerpaintConstants.INSTANCE.btnCircleDraw());
 
 	// --- INITIAL DISTRIBUTION WIDGETS ---------------------------------------
 	/**
 	 * CellList used to select and load one previously saved concentration
 	 * distribution.
 	 */
 	public static LoadInitDistCellList loadInitDistCellList;
 
 	/** Button to save an initial concentration distribution. */
 	public static SaveDistributionButton saveDistributionButton;
 
 	/** Button to load an initial concentration distribution. */
 	public static LoadInitDistButton loadInitDistButton;
 
 	/** Button to reset the current distribution to completely white. */
 	public static ResetDistButton resetDistButton;
 
 	// -- SAVE MIXING RESULTS WIDGETS -----------------------------------------
 	/** Button to save the current mixing result. */
 	public static SaveResultsButton saveResultsButton;
 
 	// --- REMOVE RESULTS WIDGETS ---------------------------------------------
 	/** Pop-up panel to handle the removal of results. */
 	public static RemoveResultsPopupPanel removeResultsPanel = new RemoveResultsPopupPanel();
 
 	/** Vertical panel to hold the flextable and close button. */
 	public static VerticalPanel removeResultsVerticalPanel = new VerticalPanel();
 
 	/** Flextable to hold all the result entries. */
 	public static ResultsFlexTable resultsFlexTable = new ResultsFlexTable();
 
 	/** Button to remove previously saved mixing results. */
 	public static RemoveSavedResultsButton removeSavedResultsButton = new RemoveSavedResultsButton();
 
 	/** Button to close the remove results pop-up panel. */
 	public static CloseResultsButton closeResultsButton = new CloseResultsButton();
 
 	// --- MIXING PROTOCOL WIDGETS --------------------------------------------
 	/**
 	 * CellList that can be used to load a previously saved mixing protocol.
 	 */
 	public static LoadProtocolCellList loadProtocolCellList;
 
 	/** Container to hold all the widgets related to a mixing protocol. */
 	public static ProtocolPanelContainer protocolPanelContainer = new ProtocolPanelContainer();
 
 	/**
 	 * The numberspinner to define how many times the mixing protocol is
 	 * executed.
 	 */
 	public static NrStepsSpinner nrStepsSpinner;
 
 	/** Button to save the current mixing protocol. */
 	public static SaveProtocolButton saveProtocolButton;
 
 	/** Button to load a mixing protocol. */
 	public static LoadProtocolButton loadProtocolButton;
 
 	/** Button that resets the current mixing protocol when pressed. */
 	public static ResetProtocolButton resetProtocolButton;
 
 	/**
 	 * Button that executes the current mixing run (initial distribution and
 	 * mixing protocol) when pressed.
 	 */
 	public static MixNowButton mixNowButton;
 
 	/**
 	 * Label to be displayed above the {@link #nrStepsSpinner}, to explain its
 	 * purpose.
 	 */
 	public static Label nrStepsLabel = new Label(
 			FingerpaintConstants.INSTANCE.lblNrSteps());
 
 	/**
 	 * Label that shows the textual representation of the current mixing
 	 * protocol.
 	 */
 	public static ProtocolRepresentationLabel labelProtocolRepresentation = new ProtocolRepresentationLabel();
 
 	/**
 	 * Label to be displayed above the protocol-related buttons, to explain
 	 * their purpose.
 	 */
 	public static Label labelProtocolLabel = new Label(
 			FingerpaintConstants.INSTANCE.lblProtocol());
 
 	/**
 	 * Checkbox that needs to be checked to define a protocol. If it isn't
 	 * checked, steps (wall movements) are executed directly.
 	 */
 	public static DefineProtocolCheckBox defineProtocolCheckBox;
 
 	// --- SAVE POP-UP MENU WIDGETS -------------------------------------------
 	/** Pop-up panel to handle the saving of the current results. */
 	public static SaveItemPopupPanel saveItemPanel = new SaveItemPopupPanel();
 
 	/**
 	 * Horizontal panel to hold the Save and Cancel buttons in the pop-up panel.
 	 */
 	public static HorizontalPanel saveButtonsPanel = new HorizontalPanel();
 
 	/**
 	 * Vertical panel to hold the textbox and the save button in the save pop-up
 	 * panel.
 	 */
 	public static VerticalPanel saveItemVerticalPanel = new VerticalPanel();
 
 	/**
 	 * Button to save the item under the specified name in the
 	 * {@link #saveNameTextBox}.
 	 */
 	public static SaveItemPanelButton saveItemPanelButton;
 
 	/** Cancel button inside the save pop-up menu. */
 	public static CancelSaveResultsButton cancelSaveResultsButton = new CancelSaveResultsButton();
 
 	/** Ok / Cancel button to close the save results or overwrite pop-up panel. */
 	public static CloseSaveButton closeSaveButton = new CloseSaveButton();
 
 	/** Textbox to input the name in to name the file. */
 	public static SaveNameTextBox saveNameTextBox = new SaveNameTextBox();
 
 	/** Label to indicate that the chosen name is already in use. */
 	public static SaveMessageLabel saveMessageLabel = new SaveMessageLabel();
 
 	// --- OVERWRITE POP-UP MENU WIDGETS --------------------------------------
 	/**
 	 * Pop-up panel that appears after the Save button in the save pop-up panel
 	 * has been pressed.
 	 */
 	public static PopupPanel overwriteSavePanel = new PopupPanel();
 
 	/**
 	 * Horizontal panel to hold the OK or Overwrite/Cancel button(s) in the
 	 * confirm save pop-up panel.
 	 */
 	public static HorizontalPanel overwriteButtonsPanel = new HorizontalPanel();
 
 	/**
 	 * Vertical panel to hold the save message and the OK/Overwrite button in
 	 * the confirm save pop-up panel.
 	 */
 	public static VerticalPanel overwriteSaveVerticalPanel = new VerticalPanel();
 
 	/** Button to overwrite the item that is currently being saved. */
 	public static OverwriteSaveButton overwriteSaveButton;
 
 	// --- LOAD POP-UP MENU WIDGETS -------------------------------------------
 	/**
 	 * Vertical panel to hold the textbox and the cancel button in the load
 	 * pop-up panel.
 	 */
 	public static VerticalPanel loadVerticalPanel = new VerticalPanel();
 
 	/** Pop-up panel to handle the loading of previously saved items. */
 	public static LoadPopupPanel loadPanel = new LoadPopupPanel();
 
 	/** Button to close the load pop-up menu. */
 	public static CloseLoadButton closeLoadButton = new CloseLoadButton();
 
 	// --- STEP SIZE WIDGETS --------------------------------------------------
 	/**
 	 * The numberspinner to define the step size of a single wall movement.
 	 */
 	public static StepSizeSpinner sizeSpinner;
 
 	/**
 	 * Label to be displayed above the {@link #sizeSpinner}, to explain its
 	 * purpose.
 	 */
 	public static Label sizeLabel = new Label(
 			FingerpaintConstants.INSTANCE.lblStepSize());
 	// ---EXPORT CANVAS IMAGE WIDGET------------------------------------------
 
 	public static ExportDistributionButton exportDistributionButton;
 
 	// --- VIEW SINGLE GRAPH WIDGETS ------------------------------------------
 	/**
 	 * Pop-up menu to display the performance of a single graph. It is opened
 	 * when {@link #viewSingleGraphButton} is clicked. It contains a vertical
 	 * panel.
 	 */
 	public static ViewSingleGraphPopupPanel viewSingleGraphPopupPanel = new ViewSingleGraphPopupPanel();
 
 	/**
 	 * Horizontal panel to contain the Close and Export buttons.
 	 */
 	public static HorizontalPanel viewSingleGraphHorizontalPanel = new HorizontalPanel();
 
 	/**
 	 * Vertical panel to contain the horizontal panel and simple panel of the
 	 * single graph pop-up.
 	 */
 	public static VerticalPanel viewSingleGraphVerticalPanel = new VerticalPanel();
 
 	/**
 	 * Simple panel to display the graph of the previously executed mixing run.
 	 */
 	public static SimplePanel viewSingleGraphGraphPanel = new SimplePanel();
 
 	/** Button to view the performance of the previously executed mixing run. */
 	public static ViewSingleGraphButton viewSingleGraphButton;
 
 	/** Button to close the performance pop-up. */
 	public static CloseSingleGraphViewButton closeSingleGraphViewButton = new CloseSingleGraphViewButton();
 
 	/** Button to export the image of the current mixing performance. */
 	public static ExportSingleGraphButton exportSingleGraphButton;
 
 	/** Button to export the image of multiple mixing performances. */
 	public static ExportMultipleGraphsButton exportMultipleGraphButton;
 
 	// --- COMPARE PERFORMANCE WIDGETS ----------------------------------------
 	/**
 	 * CellList that can be used to select multiple mixing runs from all
 	 * available saved mixing runs.
 	 */
 	public static CompareSelectPopupCellList compareSelectPopupCellList = new CompareSelectPopupCellList();
 
 	/**
 	 * Pop-up panel to display all the previously stored mixing runs with
 	 * performance. It also contains the Compare and Close buttons.
 	 */
 	public static PopupPanel compareSelectPopupPanel = new PopupPanel();
 
 	/**
 	 * Pop-up panel that displays the simple panel with the performance graph
 	 * and New Comparison and Close buttons.
 	 */
 	public static PopupPanel comparePopupPanel = new PopupPanel();
 
 	/**
 	 * Simple panel that displays a graph with the mixing performance of the
 	 * selected mixing runs.
 	 */
 	public static SimplePanel compareGraphPanel = new SimplePanel();
 
 	/**
 	 * Button to compare the performance of previously saved mixing runs. When
 	 * clicked, it opens the {@link #compareSelectPopupPanel} pop-up.
 	 */
 	public static ComparePerformanceButton comparePerformanceButton = new ComparePerformanceButton();
 
 	/**
 	 * Button to compare the performance of the selected mixing runs.
 	 */
 	public static CompareButton compareButton;
 
 	/** Cancel button inside the compare performance pop-up. */
 	public static CancelCompareButton cancelCompareButton;
 
 	/** Close button inside the compare performance pop-up. */
 	public static CloseCompareButton closeCompareButton;
 
 	/**
 	 * Button inside the compare performance pop-up to start a new comparison.
 	 * When clicked, it closes the {@link #comparePopupPanel} pop-up and opens
 	 * the {@link #compareSelectPopupPanel}pop-up
 	 */
 	public static NewCompareButton newCompareButton = new NewCompareButton();
 
 	/**
 	 * Sets the IDs for all widgets in this class (except the CellBrowser,
 	 * ToggleColourButton and MenuToggleButton). The ID is either used in the
 	 * CSS file or for debugging purposes. A widget may <b>never</b> have an
 	 * ordinary ID and a debug ID at the same time.
 	 */
 	public static void setIDs() {
 		loadingPanel.getElement().setId(GuiState.LOADINGPANEL_ID);
 		loadingPanelMessage.getElement()
 				.setId(GuiState.LOADINGPANEL_MESSAGE_ID);
 
 		menuPanel.getElement().setId("menuPanel");
 		menuPanelWrapper.getElement().setId("menuPanelWrapper");
 
 		toolSelector.ensureDebugId("toolSelector");
 		popupPanelPanel.ensureDebugId("popupPanelPanel");
 		popupPanelMenu.ensureDebugId("popupPanelMenu");
 		removeResultsVerticalPanel.ensureDebugId("removeResultsVerticalPanel");
 
 		nrStepsLabel.ensureDebugId("nrStepsLabel");
 		labelProtocolLabel.ensureDebugId("labelProtocolLabel");
 
 		saveButtonsPanel.ensureDebugId("saveButtonsPanel");
 		saveItemVerticalPanel.ensureDebugId("saveItemVerticalPanel");
 
 		overwriteSavePanel.ensureDebugId("overwriteSavePanel");
 		overwriteButtonsPanel.ensureDebugId("overwriteButtonsPanel");
 		overwriteSaveVerticalPanel.ensureDebugId("overwriteSaveVerticalPanel");
 		loadVerticalPanel.ensureDebugId("loadVerticalPanel");
 
 		sizeLabel.ensureDebugId("sizeLabel");
 
 		viewSingleGraphHorizontalPanel
 				.ensureDebugId("viewSingleGraphHorizontalPanel");
 		viewSingleGraphVerticalPanel
 				.ensureDebugId("viewSingleGraphVerticalPanel");
		viewSingleGraphGraphPanel.getElement().setId("viewSingleGraphGraphPanel");
 
 		compareSelectPopupPanel.ensureDebugId("compareSelectPopupPanel");
 		comparePopupPanel.ensureDebugId("comparePopupPanel");
 		compareGraphPanel.getElement().setId("compareGraphPanel");
 	}
 
 }
