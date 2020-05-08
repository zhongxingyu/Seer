 package nl.tue.fingerpaint.client;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import nl.tue.fingerpaint.client.Geometry.StepAddedListener;
 import nl.tue.fingerpaint.client.serverdata.ServerDataCache;
 
 import com.google.gwt.canvas.dom.client.CssColor;
 import com.google.gwt.cell.client.ClickableTextCell;
 import com.google.gwt.cell.client.ValueUpdater;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.storage.client.Storage;
 import com.google.gwt.storage.client.StorageMap;
 import com.google.gwt.user.cellview.client.CellBrowser;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.ToggleButton;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.gwt.view.client.TreeViewModel;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  * 
  * @author Group Fingerpaint
  */
 public class Fingerpaint implements EntryPoint {
 	// Class to keep track of everything the user has selected
 	protected ApplicationState as;
 
 	// Label that displays the userChoice values
 	private Label mixingDetails = new Label();
 
 	// Button to toggle between black and white drawing colour
 	private ToggleButton toggleColor;
 
 	// Button to load predefined distribution half black, half white
 	// Needed for testing purposes for story 32
 	private Button loadDistButton;
 
 	// Button to reset the distribution to all white
 	private Button resetDistButton;
 
 	// Button to save the current results
 	private Button saveResultsButton;
 
 	// Popup Panel to handle the saving of the current results
 	private PopupPanel saveResultsPanel;
 
 	// Vertical Panel to hold the textbar and the save button in the save
 	// popuppanel
 	private VerticalPanel saveResultsVerticalPanel;
 
 	// Horizontal Panel to hold the Save and Cancel buttons in the popup panel
 	private HorizontalPanel saveButtonsPanel;
 
 	// Textbox to input the name in to name the file
 	private TextBox saveNameTextBox;
 
 	// Save Button inside the save popup menu
 	private Button saveResultsPanelButton;
 
 	// Cancel Button inside the save popup menu
 	private Button cancelSaveResultsButton;
 
 	// Popup Panel that appears after the Save button in the save popup panel
 	// has been pressed
 	private PopupPanel confirmSavePanel;
 
 	// Vertical Panel to hold the save message and the ok/overwrite button in
 	// the confirm save popup panel
 	private VerticalPanel confirmSaveVerticalPanel;
 
 	// Label to hold the save message
 	private Label saveMessageLabel;
 
 	// Horizontal Panel to hold the ok or overwrite/cancel button(s) in the
 	// confirm save popup panel
 	private HorizontalPanel confirmButtonsPanel;
 
 	// Ok / Cancel button to close the save results popup panel
 	private Button closeSaveResultsButton;
 
 	// Overwrite button to confirm the save if an already used name has been
 	// chosen
 	private Button confirmSaveButton;
 
 	//--------------------------------------------------------------------------------------
 	
 	// Button to remove previously saved results
 	private Button removeSavedResultsButton;
 	
 	
 	
 	
 	//--------------------------------------------------------------------------------------
 
 	// Button to adapt the drawing tool
 	// TODO: Change this to a button on which the current tool is drawn
 	private Button toolSelectButton;
 
 	// PopupPanel which contains options for selecting a different drawing tool
 	private PopupPanel toolSelector;
 
 	// The panel in the popup panel to seperate the toolSelector from the
 	// toolSizer
 	private HorizontalPanel popupPanelPanel;
 
 	// The panel in the popup panel that contains the different drawing tools
 	private VerticalPanel popupPanelMenu;
 
 	// Button to select the square drawing tool
 	// TODO: Change this to a button on which a square is drawn
 	private ToggleButton squareDrawingTool;
 
 	// Button to select the circle drawing tool
 	// TODO: Change this to a button on which a circle is drawn
 	private ToggleButton circleDrawingTool;
 
 	// Horizontal panel to contain drawing canvas and menu bar
 	private HorizontalPanel panel = new HorizontalPanel();
 
 	// Vertical panel to contain all menu items
 	private VerticalPanel menuPanel = new VerticalPanel();
 
 	// Panel that covers the entire application and blocks the user from
 	// accessing other features
 	private static FlowPanel loadPanel;
 	private Label loadPanelMessage;
 
 	// The NumberSpinner and label to define the step size
 	// TODO: The text 'Step size' should be translated later on
 	private Label sizeLabel = new Label("Step size");
 	private NumberSpinner sizeSpinner;
 
 	// Checkbox that needs to be checked to define a protocol. If it isn't
 	// checked, steps are executed directly.
 	private CheckBox defineProtocolCheckBox;
 
 	// The NumberSpinner and label to define how many times the mixing protocol
 	// is executed
 	// TODO: The text '#steps' should be translated later on
 	private Label nrStepsLabel = new Label("#steps");
 	private NumberSpinner nrStepsSpinner;
 
 	/**
 	 * Shows the textual representation of the mixing protocol.
 	 */
 	private TextArea taProtocolRepresentation = new TextArea();
 
 	// Button that executes the current mixing run when it is pressed
 	private Button mixNowButton;
 
 	// Button that resets the protocol when it is pressed
 	private Button resetProtocolButton;
 
 	/*
 	 * The NumberSpinner to set the #steps parameter. Its settings are described
 	 * via the following parameters.
 	 */
 	private final double NRSTEPS_DEFAULT = 1.0;
 	private final double NRSTEPS_RATE = 1.0;
 	private final double NRSTEPS_MIN = 1.0;
 	private final double NRSTEPS_MAX = 50.0;
 
 	private static final String LOADPANEL_ID = "loading-overlay";
 	private static final String LOADPANEL_MESSAGE_ID = "loading-overlay-message";
 
 	// Width of the menu in which buttons are displayed
 	// on the right side of the window in pixels
 	private final int menuWidth = 200;
 
 	// Height of address-bar / tabs / menu-bar in the
 	// browser in pixels. If this is not taken into account,
 	// a vertical scroll bar appears.
 	private final int topBarHeight = 65;
 
 	private Storage storage;
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 
 		// initialise the underlying model of the application
 		as = new ApplicationState();
 		as.setNrSteps(1.0);
 		ServerDataCache.initialise(new AsyncCallback<String>() {
 			@Override
 			public void onSuccess(String result) {
 				setLoadPanelVisible(false);
 				loadMenu();
 			}
 
 			@Override
 			public void onFailure(Throwable caught) {
 				setLoadPanelVisible(false);
 				showError(caught.getMessage());
 			}
 		});
 
 		try {
 			initLocalStorage();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Build and show the main menu.
 	 */
 	protected void loadMenu() {
 		// Create a model for the cellbrowser.
 		TreeViewModel model = new CustomTreeModel();
 
 		/*
 		 * Create the browser using the model. We specify the default value of
 		 * the hidden root node as "null".
 		 */
 		CellBrowser tree = (new CellBrowser.Builder<Object>(model, null))
 				.build();
 
 		// Add the tree to the root layout panel.
 		RootLayoutPanel.get().add(tree);
 	}
 
 	/**
 	 * Tries to initialise local storage.
 	 * 
 	 * @throws Exception
 	 *             If HTML5 Local Storage is not supported in this browser.
 	 */
 	private void initLocalStorage() throws Exception {
 		storage = Storage.getLocalStorageIfSupported();
 
 		if (storage == null) {
 			throw new Exception(
 					"HTML5 Local Storage is not supported in this browser.");
 		}
 	}
 
 	/**
 	 * Saves the state of the application to the HTML5 local storage under key
 	 * {@code name}.
 	 * 
 	 * @param name
 	 *            The name to save the state under.
 	 */
 	private void saveState(String name) {
 		String asJson = as.jsonize();
 
 		storage.setItem(name, asJson);
 	}
 
 	/**
 	 * Returns whether a saved state with key {@code name} exists in local
 	 * storage.
 	 * 
 	 * @param name
 	 *            The key to check.
 	 * @return whether a saved state with key {@code name} exists in local
 	 *         storage.
 	 */
 	private boolean isNameInUse(String name) {
 		StorageMap storageMap = new StorageMap(storage);
 		return storageMap.containsKey(name);
 	}
 
 	/**
 	 * Loads the JSON object from HTML storage. Has no effect if no information
 	 * is stored under {@code saveName}.
 	 * 
 	 * @param saveName
 	 */
 	private void loadState(String saveName) {
 		String jsonObject = storage.getItem(saveName);
 
 		if (jsonObject != null && jsonObject != "") {
 			as.unJsonize(jsonObject);
 		}
 
 		refreshWidgets();
 	}
 
 	private void refreshWidgets() {
 		nrStepsSpinner.setValue(as.getNrSteps());
 		sizeSpinner.setValue(as.getStepSize());
 
 		for (MixingStep step : as.getProtocol().getProgram()) {
 			updateProtocolLabel(step);
 		}
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
 		verPanel.add(new Label("An error occurred!"));
 		if (message != null) {
 			verPanel.add(new Label(message));
 		}
 		verPanel.add(new Button("Close", new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				errorPopup.hide();
 			}
 		}));
 		errorPopup.add(verPanel);
 		errorPopup.center();
 	}
 
 	/**
 	 * Change the message that is displayed in the load panel below the loading
 	 * animation.
 	 * 
 	 * @param message
 	 *            The message to show below the animation. When {@code null},
 	 *            the message will be deleted.
 	 */
 	protected void setLoadPanelMessage(String message) {
 		if (message == null) {
 			message = "";
 		}
 
 		loadPanelMessage.setText(message);
 	}
 
 	/**
 	 * <p>
 	 * Show or hide an overlay with a loading animation in the centre. Making
 	 * this panel visible will make it impossible for the user to give input.
 	 * </p>
 	 * 
 	 * <p>
 	 * When hiding the panel, the message will also be reset. Change it with
 	 * {@link #setLoadPanelMessage}.
 	 * </p>
 	 * 
 	 * @param visible
 	 *            If the panel should be hidden or shown.
 	 */
 	protected void setLoadPanelVisible(boolean visible) {
 		if (visible) {
 			RootPanel.get().add(loadPanel);
 		} else {
 			if (RootPanel.get(LOADPANEL_ID) != null) {
 				loadPanel.removeFromParent();
 				setLoadPanelMessage(null);
 			}
 		}
 	}
 
 	/**
 	 * The model that defines the nodes in the tree.
 	 */
 	private class CustomTreeModel implements TreeViewModel {
 
 		/**
 		 * Number of levels in the tree. Is used to determine when the browser
 		 * should be closed.
 		 */
 		private final static int NUM_LEVELS = 2;
 
 		/** A selection model that is shared along all levels. */
 		private final SingleSelectionModel<String> selectionModel = new SingleSelectionModel<String>();
 
 		/** Updater on the highest level. */
 		private final ValueUpdater<String> valueGeometryUpdater = new ValueUpdater<String>() {
 			@Override
 			public void update(String value) {
 				as.setGeometry(value);
 				lastClickedLevel = 0;
 				GWT.log("Update geometry = \"" + as.getGeometryChoice() + "\"!");
 			}
 		};
 
 		/** Updater on level 1. */
 		private final ValueUpdater<String> valueMixerUpdater = new ValueUpdater<String>() {
 			@Override
 			public void update(String value) {
 				as.setMixer(value);
 				lastClickedLevel = 1;
 				GWT.log("Update mixer = \"" + as.getMixerChoice() + "\"!");
 			}
 		};
 
 		/** Indicate which level was clicked the last. */
 		private int lastClickedLevel = -1;
 
 		private void setUserChoiceValues(String selectedMixer) {
 			// TODO: Actually create a different geometry depending on the
 			// chosen geometry...
 			as.setGegeom(new RectangleGeometry(Window.getClientHeight()
 					- topBarHeight, Window.getClientWidth() - menuWidth));
 		}
 
 		public CustomTreeModel() {
 
 			selectionModel
 					.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 						@Override
 						public void onSelectionChange(SelectionChangeEvent event) {
 							String selected = selectionModel
 									.getSelectedObject();
 
 							if (selected != null
 									&& lastClickedLevel == NUM_LEVELS - 1) {
 								setUserChoiceValues(selected);
 
 								// "closes" Cellbrowser widget (clears whole
 								// rootpanel)
 								// TODO: Make decent close-code
 								RootPanel.get().clear();
 
 								if (as.getGeometryChoice() != null
 										&& as.getMixerChoice() != null) {
 									mixingDetails.setText("Geometry: "
 											+ as.getGeometryChoice().toString()
 											+ ", Mixer: "
 											+ as.getMixerChoice().toString());
 								} else {// This should never happen. Just to be
 										// safe i made this msg so fails are
 										// visible
 									mixingDetails
 											.setText("Geometry and/or Mixer was not selected succesfully");
 								}
 
 								RootPanel.get().add(mixingDetails);
 
 								createMixingWidgets();
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
 
 			// Initialise the loadPanel
 			createLoadPanel();
 			
 			// Initialise the toolSelectButton and add to menuPanel
 			createToolSelector();
 			menuPanel.add(toolSelectButton);
 
 			// Initialise toggleButton and add to
 			// menuPanel
 			createToggleButton();
 			menuPanel.add(toggleColor);
 
 			// Initialise the loadDistButton and add to
 			// menuPanel
 			createLoadDistButton();
 			menuPanel.add(loadDistButton);
 
 			// Initialise the resetDistButton and add to menuPanel
 			createResetDistButton();
 			menuPanel.add(resetDistButton);
 
 			// Initialise the saveResultsButton and add it to the menuPanel
 			createSaveResultsButton();
 			menuPanel.add(saveResultsButton);
 
 			// Initialise the removeSavedResultsButton and add it to the
 			// menuPanel
 			createRemoveSavedResultsButton();
 			menuPanel.add(removeSavedResultsButton);
 
 			// Initialise a spinner for changing the length of a mixing protocol
 			// step and add to menuPanel.
 			createStepSizeSpinner();
 			menuPanel.add(sizeLabel);
 			menuPanel.add(sizeSpinner);
 
 			// Initialise the checkbox that indicates whether a protocol is
 			// being defined, or single steps have to be executed and add to
 			// menu panel
 			createDefineProtocolCheckBox();
 			menuPanel.add(defineProtocolCheckBox);
 
 			// Initialise a spinner for #steps
 			createNrStepsSpinner();
 
 			// Initialise the resetProtocol button
 			createResetProtocolButton();
 
 			createProtocolRepresentationTextArea();
 
 			// Initialise the mixNow button
 			createMixNowButton();
 
 			// TODO: Initialise other menu items and add them to menuPanel
 			// Add all the protocolwidgets to the menuPanel and hide them
 			// initially.
 			menuPanel.add(nrStepsLabel);
 			menuPanel.add(nrStepsSpinner);
 			menuPanel.add(taProtocolRepresentation);
 			menuPanel.add(mixNowButton);
 			menuPanel.add(resetProtocolButton);
 			hideProtocolWidgets();
 
 			// Add canvas and menuPanel to the panel
 			// Make the canvas the entire width of the
 			// screen except for the
 			// menuWidth
 			panel.setWidth("100%");
 			panel.add(as.getGeometry().getCanvas());
 			panel.add(menuPanel);
 			panel.setCellWidth(menuPanel, Integer.toString(menuWidth) + "px");
 
 			// Add panel to RootPanel
 			RootPanel.get().add(panel);
 		}
 
 		/**
 		 * Get the {@link NodeInfo} that provides the children of the specified
 		 * value.
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
 						new ClickableTextCell(), selectionModel,
 						valueMixerUpdater);
 
 			}
 			return null;
 		}
 
 		/**
 		 * Check if the specified value represents a leaf node. Leaf nodes
 		 * cannot be opened.
 		 */
 		// You can define your own definition of leaf-node here.
 		public boolean isLeaf(Object value) {
 			return lastClickedLevel == NUM_LEVELS - 1;
 		}
 	}
 
 	/*
 	 * Initialises the spinner for the stepSize
 	 */
 	private void createStepSizeSpinner() {
 		// initial initialisation of the spinner
 		sizeSpinner = new NumberSpinner(MixingStep.STEP_DEFAULT,
 				MixingStep.STEP_UNIT, MixingStep.STEP_MIN, MixingStep.STEP_MAX,
 				true);
 		as.editStepSize(MixingStep.STEP_DEFAULT);
 
 		// set a listener for the spinner
 		sizeSpinner.setSpinnerListener(new NumberSpinnerListener() {
 
 			@Override
 			public void onValueChange(double value) {
 				// change the current mixing step
 				as.editStepSize(value);
 			}
 
 		});
 	}
 
 	/*
 	 * Initialises the define Protocol checkbox. When this button is pressed,
 	 * the current protocol is reset, and the protocol widgets are shown/hidden.
 	 */
 	private void createDefineProtocolCheckBox() {
 		// TODO: The text 'Define Protocol' should be translated later on
 		defineProtocolCheckBox = new CheckBox("Define Protocol");
 		defineProtocolCheckBox.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				if (defineProtocolCheckBox.getValue()) {
 					showProtocolWidgets();
 				} else {
 					hideProtocolWidgets();
 				}
 			}
 		});
 	}
 
 	/*
 	 * Initialises the loadPanel
 	 */
 	private void createLoadPanel() {
 		// Initialise the loading panel
 		// Add animation image
 		loadPanel = new FlowPanel();
 		Image loadImage = new Image("/img/loading_animation.gif");
 		loadPanel.add(loadImage);
 		// Add label that may contain explanatory text
 		loadPanelMessage = new Label("Loading geometries and mixers...", false);
 		loadPanelMessage.getElement().setId(LOADPANEL_MESSAGE_ID);
 		loadPanel.add(loadPanelMessage);
 		loadPanel.getElement().setId(LOADPANEL_ID);
 	}
 
 	/*
 	 * Initialises the reset Distribution Button. When this button is pressed,
 	 * the current canvas is reset to all white
 	 */
 	private void createResetDistButton() {
 		resetDistButton = new Button("Reset Dist");
 		resetDistButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				as.getGeometry().resetDistribution();
 			}
 
 		});
 	}
 
 	/*
 	 * Shows all the protocol widgets on the menu bar.
 	 */
 	private void showProtocolWidgets() {
 		nrStepsLabel.setVisible(true);
 		nrStepsSpinner.setVisible(true);
 		taProtocolRepresentation.setVisible(true);
 		mixNowButton.setVisible(true);
 		resetProtocolButton.setVisible(true);
 	}
 
 	/*
 	 * Hides all the protocol widgets on the menu bar.
 	 */
 	private void hideProtocolWidgets() {
 		nrStepsLabel.setVisible(false);
 		nrStepsSpinner.setVisible(false);
 		taProtocolRepresentation.setVisible(false);
 		mixNowButton.setVisible(false);
 		resetProtocolButton.setVisible(false);
 	}
 
 	/*
 	 * resets the current protocol and the protocol widgets
 	 */
 	private void resetProtocol() {
 		as.setProtocol(new MixingProtocol());
 		taProtocolRepresentation.setText("");
 		as.setNrSteps(1);
 		nrStepsSpinner.setValue(1);
 		mixNowButton.setEnabled(false);
 	}
 
 	/*
 	 * Initialises the spinner for the nrSteps.
 	 */
 	private void createNrStepsSpinner() {
 		// Initialise the spinner with the required settings.
 		nrStepsSpinner = new NumberSpinner(NRSTEPS_DEFAULT, NRSTEPS_RATE,
 				NRSTEPS_MIN, NRSTEPS_MAX, true);
 		// Also initialise the initial value in the ApplicationState class.
 		as.setNrSteps(NRSTEPS_DEFAULT);
 
 		// The spinner for #steps should update the nrSteps variable whenever
 		// the value is changed.
 		nrStepsSpinner.setSpinnerListener(new NumberSpinnerListener() {
 
 			@Override
 			public void onValueChange(double value) {
 				as.setNrSteps(value);
 			}
 		});
 	}
 
 	/*
 	 * Initialises the toggleColor button. TODO: Use pictures instead of text on
 	 * the button.
 	 * 
 	 * Note: If the button shows "black" it means the current drawing colour is
 	 * black. Not 'toggle to black'.
 	 */
 	private void createToggleButton() {
 		toggleColor = new ToggleButton("black", "white");
 		toggleColor.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				toggleColor();
 
 			}
 		});
 		toggleColor.setWidth("100px");
 	}
 	
 	public ArrayList<String> getStoredNames() {
 		ArrayList<String> names = new ArrayList<String>();
 		for (int i = 0; i < storage.getLength(); i++) {
 			names.add(storage.key(i));
 		}
 		return names;
 	}
 	
 	/**
 	 * Removes an item from local storage.
 	 * @param key Item to remove.
 	 */
 	public void removeStoredItem(String key) {
 		storage.removeItem(key);
 	}
 
 	/*
 	 * Initialises the protocol representation text area. TODO: this code has to
 	 * be removed!
 	 */
 	private void createProtocolRepresentationTextArea() {
 		taProtocolRepresentation.setText("");
 		taProtocolRepresentation.setWidth(String.valueOf(menuWidth) + "px");
 		taProtocolRepresentation
 				.setWidth(String.valueOf(menuWidth - 10) + "px");
 		taProtocolRepresentation.setEnabled(false);
 	}
 
 	/*
 	 * Changes the current drawing colour from black to white, and from white to
 	 * black.
 	 */
 	private void toggleColor() {
 		if (toggleColor.isDown()) {
 			as.getGeometry().setColor(CssColor.make("white"));
 		} else {
 			as.getGeometry().setColor(CssColor.make("black"));
 		}
 	}
 
 	/*
 	 * Initialises the tool selector, including buttons to select the shape of
 	 * the tool, and the slider to select the size of the tool
 	 */
 	private void createToolSelector() {
 		// --Initialise all elements--------------------------------
 		toolSelector = new PopupPanel(true);
 		popupPanelPanel = new HorizontalPanel();
 		popupPanelMenu = new VerticalPanel();
 		squareDrawingTool = new ToggleButton("square", "square");
 		circleDrawingTool = new ToggleButton("circle", "circle");
 
 		squareDrawingTool.addClickHandler(new ClickHandler() {
 
 			/*
 			 * Select the square drawing tool when this button is clicked
 			 */
 			@Override
 			public void onClick(ClickEvent event) {
 
 				if (!squareDrawingTool.isDown()) {
 					squareDrawingTool.setDown(true);
 				} else {
 					// TODO Change hard-coded 3 to 'size-slider.getValue()' or
 					// something
 					as.getGeometry().setDrawingTool(new SquareDrawingTool(3));
 
 					circleDrawingTool.setDown(false);
 				}
 			}
 		});
 		// Initial drawing tool is square
 		squareDrawingTool.setDown(true);
 
 		circleDrawingTool.addClickHandler(new ClickHandler() {
 
 			/*
 			 * Select the square drawing tool when this button is clicked
 			 */
 			@Override
 			public void onClick(ClickEvent event) {
 
 				if (!circleDrawingTool.isDown()) {
 					circleDrawingTool.setDown(true);
 				} else {
 					// TODO Change hard-coded 3 to 'size-slider.getValue()' or
 					// something
 					as.getGeometry().setDrawingTool(new CircleDrawingTool(3));
 
 					squareDrawingTool.setDown(false);
 				}
 			}
 		});
 
 		// -- Add all Drawings Tools below ---------------------
 		popupPanelMenu.add(squareDrawingTool);
 		popupPanelMenu.add(circleDrawingTool);
 
 		// --TODO: Add DrawingTool Size slider below ----------------
 		popupPanelPanel.add(popupPanelMenu);
 
 		// Add everything to the popup panel
 		toolSelector.add(popupPanelPanel);
 
 		// Create the button the triggers the popup panel
 		// TODO: The text 'Select Tool' should be translated later on
 		toolSelectButton = new Button("Select Tool");
 		toolSelectButton.addClickHandler(new ClickHandler() {
 
 			/*
 			 * Show the popupPanel when this button is clicked
 			 */
 			@Override
 			public void onClick(ClickEvent event) {
 				toolSelector
 						.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
 							public void setPosition(int offsetWidth,
 									int offsetHeight) {
 								int left = (Window.getClientWidth()
 										- offsetWidth - 75);
 								int top = 40;
 								toolSelector.setPopupPosition(left, top);
 							}
 						});
 			}
 
 		});
 
 	}
 
 	/*
 	 * Initialises the mixNow button. When pressed, the current protocol is
 	 * executed. TODO: When this button is disabled, hovering it should not make
 	 * it appear 'active'
 	 */
 	private void createMixNowButton() {
 		// TODO: The text 'Mix Now' should be translated later on
 		mixNowButton = new Button("Mix Now");
 		mixNowButton.setEnabled(false);
 		mixNowButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				executeMixingRun();
 			}
 
 		});
 	}
 
 	/*
 	 * Initialises the resetProtocol button. When pressed, this button sets a
 	 * new (and empty) protocol in the application state, and it clear the
 	 * protocol representation text area.
 	 */
 	private void createResetProtocolButton() {
 		// TODO: The text 'Reset Protocol' should be translated later on
 		resetProtocolButton = new Button("Reset Protocol");
 		resetProtocolButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				resetProtocol();
 			}
 
 		});
 	}
 
 	/*
 	 * Initialises the createSaveResultsButton. When pressed, this button allows
 	 * a user to save a mixing run
 	 */
 	private void createSaveResultsButton() {
 		// TODO: The text 'Save Results' should be translated later on
 		saveResultsButton = new Button("Save Results");
 		saveResultsPanel = new PopupPanel();
 		saveResultsPanel.setModal(true);
 		
 		saveResultsVerticalPanel = new VerticalPanel();
 		saveButtonsPanel = new HorizontalPanel();
 		saveNameTextBox = new TextBox();
 		saveNameTextBox.setMaxLength(30);
 
 		saveResultsPanelButton = new Button("Save");
 		saveResultsPanelButton.setEnabled(false);
 		cancelSaveResultsButton = new Button("Cancel");
 		confirmSavePanel = new PopupPanel();
 		confirmSavePanel.setModal(true);
 		
 		confirmSaveVerticalPanel = new VerticalPanel();
 		saveMessageLabel = new Label();
 		confirmButtonsPanel = new HorizontalPanel();
 		closeSaveResultsButton = new Button();
 		confirmSaveButton = new Button("Overwrite");
 
 		// Display the first popuppanel when the save button is pressed
 		saveResultsButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				saveResultsPanel
 						.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
 							public void setPosition(int offsetWidth,
 									int offsetHeight) {
 								int left = (Window.getClientWidth() - offsetWidth) / 2;
 								int top = (Window.getClientHeight() - offsetHeight) / 2;
 								saveResultsPanel.setPopupPosition(left, top);
 							}
 						});
 				saveNameTextBox.setFocus(true);
 			}
 
 		});
 
 		// add all components to first popuppanel
 		saveResultsPanel.add(saveResultsVerticalPanel);
 		saveResultsVerticalPanel.add(saveNameTextBox);
 		saveResultsVerticalPanel.add(saveButtonsPanel);
 		saveButtonsPanel.add(saveResultsPanelButton);
 		saveButtonsPanel.add(cancelSaveResultsButton);
 
 		// add all components to second popup panel
 		confirmSavePanel.add(confirmSaveVerticalPanel);
 		confirmSaveVerticalPanel.add(saveMessageLabel);
 		confirmSaveVerticalPanel.add(confirmButtonsPanel);
 		confirmButtonsPanel.add(closeSaveResultsButton);
 
 		// display the second popup panel when the second save button is pressed
 		saveResultsPanelButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				String name = saveNameTextBox.getText();
 				if (isNameInUse(name)) {
 					saveMessageLabel.setText("This name is already in use. "
 							+ "Choose whether to overwrite existing file "
 							+ "or to cancel.");
 					closeSaveResultsButton.setText("Cancel");
 
 					confirmButtonsPanel.remove(closeSaveResultsButton);
 					confirmButtonsPanel.add(confirmSaveButton);
 					confirmButtonsPanel.add(closeSaveResultsButton);
 				} else {
 					saveState(name);
 					saveMessageLabel.setText("Save has been succesful");
 					closeSaveResultsButton.setText("OK");
 				}
 				confirmSavePanel
 						.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
 							public void setPosition(int offsetWidth,
 									int offsetHeight) {
 								int left = (Window.getClientWidth() - offsetWidth) / 2;
 								int top = (Window.getClientHeight() - offsetHeight) / 2;
 								confirmSavePanel.setPopupPosition(left, top);
 							}
 						});
 				saveResultsPanel.hide();
 			}
 		});
 
 		// Hide the first popup panel when the first cancel button is pressed
 		cancelSaveResultsButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				saveResultsPanel.hide();
 				saveNameTextBox.setText("");
 			}
 		});
 
 		// Determine whether user input is valid. Enable/disable the save
 		// button. Execute save when ENTER is pressed.
 		saveNameTextBox.addKeyPressHandler(new KeyPressHandler() {
 
 			@Override
 			public void onKeyPress(KeyPressEvent event) {
 				String text = saveNameTextBox.getText();
 				String inputCharacter = Character.toString(event.getCharCode());
 				int textlength = text.length();
 				if (inputCharacter
 						.matches("[~`!@#$%^&*()+={}\\[\\]:;\"|\'\\\\<>?,./\\s]")) {
 					saveNameTextBox.cancelKey();
 				}
 				if (inputCharacter.matches("[A-Za-z0-9]")) {
 					textlength++;
 				}
 				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_BACKSPACE) {
 					textlength--;
 				}
 				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
 					saveResultsPanelButton.click();
 				}
 				saveResultsPanelButton.setEnabled(textlength > 0);
 			}
 		});
 
 		// Hide both popup panels if the OK button was pressed. Hide only the
 		// second panel if the cancel button was pressed.
 		closeSaveResultsButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				confirmSavePanel.hide();
 				if (!closeSaveResultsButton.getText().equals("OK")) {
 					confirmSavePanel.remove(confirmSaveButton);
 					saveResultsPanel.show();
 					saveNameTextBox.setSelectionRange(0, saveNameTextBox
 							.getText().length());
 					saveNameTextBox.setFocus(true);
 				} else {
 					saveNameTextBox.setText("");
 				}
 			}
 		});
 
 		// Display a save was succesful message after overwrite has been
 		// pressed.
 		confirmSaveButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				String name = saveNameTextBox.getText();
 				saveState(name);
 				saveMessageLabel.setText("Save has been succesful");
 				closeSaveResultsButton.setText("OK");
 				confirmSaveButton.removeFromParent();
 				confirmSavePanel.center();
 			}
 		});
 	}
 
 	/*
 	 * Initialises the removeSavedResultsButton. When pressed, this button
 	 * allows a user to remove a previously saved mixing run
 	 */
 	private void createRemoveSavedResultsButton() {
 		// TODO: The text 'Remove Saved Results' should be translated later on
 		removeSavedResultsButton = new Button("Remove Saved Results");
 		removeSavedResultsButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				// TODO: handle click by opening remove saves options
 			}
 		});
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
 		String oldProtocol = taProtocolRepresentation.getText();
 		String stepString;
 
 		if (step.isTopWall() && step.movesForward()) {
 			stepString = "T";
 		} else if (step.isTopWall() && !step.movesForward()) {
 			stepString = "-T";
 		} else if (!step.isTopWall() && step.movesForward()) {
 			stepString = "B";
 		} else { // (!step.isTopWall() && !step.movesForward()) {
 			stepString = "-B";
 		}
 
 		stepString += "[" + step.getStepSize() + "]";
 
 		taProtocolRepresentation.setText(oldProtocol + stepString + " ");
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
 		if (defineProtocolCheckBox.getValue()) {
 			step.setStepSize(as.getStepSize());
 			as.addMixingStep(step);
 
 			updateProtocolLabel(step);
 			mixNowButton.setEnabled(true);
 		}
 	}
 
 	/**
 	 * Saves the initial distribution. Sends all current information about the
 	 * protocol and the distribution to the server. Displays the results on
 	 * screen.
 	 */
 	private void executeMixingRun() {
 		as.setInitialDistribution(as.getGeometry().getDistribution());
 		// TODO: collect all necessary information and send it to server
 	}
 
 	// --Methods for testing purposes only---------------------------------
 	/*
 	 * Initialises the Load Distribution button. This button only exists for
 	 * testing purposes. When it is pressed, the distribution of the geometry is
 	 * set to a colour bar from black to white, from left to right. This
 	 * distribution is then drawn on the canvas, to demonstrate we can load an
 	 * arbitrary distribution, with 256 gray scale colours. TODO: Can (and
 	 * should) be removed when the communication is functional
 	 */
 	private void createLoadDistButton() {
 		loadDistButton = new Button("Load Dist");
 		loadDistButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				// RectangleDistribution dist = new RectangleDistribution();
 				double[] dist = new double[96000];
 				for (int x = 0; x < 400; x++) {
 					for (int y = 0; y < 240; y++) {
 						// dist.setValue(x, y, (double) x / 400);
 						dist[x + 400 * (239 - y)] = (double) x / 400;
 					}
 				}
 				as.getGeometry().drawDistribution(dist);
 			}
 		});
 	}
 }
