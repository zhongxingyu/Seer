 package nl.tue.fingerpaint.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import nl.tue.fingerpaint.client.Geometry.StepAddedListener;
 import nl.tue.fingerpaint.client.websocket.Request;
 import nl.tue.fingerpaint.client.websocket.Response;
 import nl.tue.fingerpaint.client.websocket.ResponseCallback;
 import nl.tue.fingerpaint.client.websocket.SimulatorServiceSocket;
 import nl.tue.fingerpaint.client.websocket.Step;
 
 import com.google.gwt.canvas.dom.client.CssColor;
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.cellview.client.CellBrowser;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.SerializationException;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.ToggleButton;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.gwt.view.client.TreeViewModel;
 import com.seanchenxi.gwt.storage.client.StorageExt;
 import com.seanchenxi.gwt.storage.client.StorageKey;
 import com.seanchenxi.gwt.storage.client.StorageKeyFactory;
 import com.seanchenxi.gwt.storage.client.StorageQuotaExceededException;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  * 
  * @author Group Fingerpaint
  */
 public class Fingerpaint implements EntryPoint {
 	// Class to remember which Geometry and Mixer the user has selected
 	private ApplicationState as;
 
 	// Label that displays the userChoice values
 	private Label mixingDetails = new Label();
 
 	// Button to toggle between black and white drawing colour
 	private ToggleButton toggleColor;
 
 	// Button to load predefined distribution half black, half white
 	// Needed for testing purposes for story 32
 	private Button loadDistButton;
 
 	// Rectangular geometry to draw on
 	private Geometry geom;
 
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
 	private static SimplePanel loadPanel = new SimplePanel();
 
 	// The image that will be shown in the loadPanel
 	final Image loadImage = new Image();
 
 	private StorageExt storage;
 
 	/**
 	 * Shows the textual representation of the mixing protocol.
 	 */
 	private TextArea taProtocolRepresentation = new TextArea();
 	/*
 	 * The NumberSpinner to set the #steps parameter. Its settings are described
 	 * via the following parameters.
 	 */
 	private final double NRSTEPS_DEFAULT = 1.0;
 	private final double NRSTEPS_RATE = 1.0;
 	private final double NRSTEPS_MIN = 1.0;
 	private final double NRSTEPS_MAX = 50.0;
 
 	private NumberSpinner nrStepsSpinner;
 	private NumberSpinner sizeSpinner;
 	private Label nrStepsLabel = new Label("#steps");
 	private Label sizeLabel = new Label("Step size");
 
 	// Width of the menu in which buttons are displayed
 	// on the right side of the window in pixels
 	private final int menuWidth = 125;
 
 	// Height of address-bar / tabs / menu-bar in the
 	// browser in pixels. If this is not taken into account,
 	// a vertical scroll bar appears.
 	private final int topBarHeight = 50;
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 		// Point the loadImage at a URL.
 		loadImage.setUrl("/img/loading_animation.gif");
 		// add the loading-image to the panel
 		loadPanel.add(loadImage);
 		// give the image the center css-style
 		loadImage.addStyleName("center");
 		// set item ID for loadpanel
 		loadPanel.getElement().setId("loading-overlay");
 
 		// initialise the UC
 		as = new ApplicationState();
 
 		// Create a model for the cellbrowser.
 		TreeViewModel model = new CustomTreeModel();
 		/*
 		 * Create the browser using the model. We specify the default value of
 		 * the hidden root node as "null".
 		 */
 		CellBrowser tree = (new CellBrowser.Builder<Object>(model, null))
 				.build();
 
 		// ((CustomTreeModel) model).setCellBrowser(tree);
 
 		// Add the tree to the root layout panel.
 		RootLayoutPanel.get().add(tree);
 
 		storage = StorageExt.getLocalStorage();
 		if (storage == null) {
 			// Handle this
 		}
 
 		StorageKey<int[]> myFirstKey = StorageKeyFactory
 				.intArrayKey("koekjesding");
 		int[] myKoekjes = { 1, 4, 7, 10, 1073741824 };
 		try {
 			storage.put(myFirstKey, myKoekjes);
 		} catch (SerializationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (StorageQuotaExceededException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		
		try {
			int[] koekjesVanStorage = storage.get(myFirstKey);
			
			for (int i : koekjesVanStorage) {
				String text = String.valueOf(i) + ", " + taProtocolRepresentation.getText();
				taProtocolRepresentation.setText(text);
				System.out.println(String.valueOf(i));
			}
			
		} catch (SerializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
 		testRequestSimulation();
 	}
 
 	/**
 	 * Test if we can call the service simulator.
 	 */
 	protected void testRequestSimulation() {
 		double[] dist = { 1, 0, .8, 0.5 };
 		Step[] protocol = { new Step("TL", 2.0), new Step("TR", 5.0) };
 		Request request = new Request(0, 0, dist, protocol, 5, true);
 		ResponseCallback callback = new ResponseCallback() {
 			@Override
 			public void onError(String message) {
 				GWT.log("onError: " + message);
 			}
 
 			@Override
 			public void onResponse(Response result) {
 				GWT.log("onResponse: " + result.toString());
 			}
 		};
 		SimulatorServiceSocket sss = SimulatorServiceSocket.getInstance();
 		sss.requestSimulation(request, callback);
 		Timer togglebuttonTimer = new Timer() {
 			public void run() {
 				GWT.log("FAIL: timer ran out");
 			}
 		};
 		togglebuttonTimer.schedule(10000);
 	}
 
 	/**
 	 * The model that defines the nodes in the tree.
 	 */
 	private class CustomTreeModel implements TreeViewModel {
 		private final List<GeometryNames> geometries = new ArrayList<GeometryNames>();
 
 		private final SingleSelectionModel<String> selectionModel = new SingleSelectionModel<String>();
 
 		// private CellBrowser cb; //Reference to self. Used in an attempt to
 		// use some kind of .close() method on itself.
 
 		private void setupGeometryValues() {
 			// add all instances of GeometryNames to geometries
 			for (GeometryNames gm : GeometryNames.values()) {
 				geometries.add(gm);
 			}
 		}
 
 		private void setUserChoiceValues(String selected) {
 			// TODO: This structure will change when GeometryNames and
 			// MixerNames are stored on the server.
 			// The switch sort of simulates that MixerNames are linked to a
 			// Geometry, but currently they
 			// are enum classes and are not connected.
 
 			for (GeometryNames gn : GeometryNames.values()) {
 				switch (gn) {
 				case Rectangle:
 					for (RectangleMixers rm : RectangleMixers.values()) {
 						if ((rm.toString()).equals(selected)) {
 							as.setGeometry(gn);
 							as.setMixer(rm);
 						}
 					}
 					break;
 				case exampleGeometry1:
 					for (ExampleGeometryMixers egm : ExampleGeometryMixers
 							.values()) {
 						if ((egm.toString()).equals(selected)) {
 							as.setGeometry(gn);
 							as.setMixer(egm);
 						}
 					}
 					break;
 				}
 			}
 		}
 
 		public CustomTreeModel() {
 
 			setupGeometryValues();
 
 			selectionModel
 					.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 						@Override
 						public void onSelectionChange(SelectionChangeEvent event) {
 							String selected = selectionModel
 									.getSelectedObject();
 
 							setUserChoiceValues(selected);
 
 							if (selected != null) {
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
 								{
 									createMixingWidgets();
 								}
 
 							}
 						}
 					});
 		}
 
 		/**
 		 * Helper method that initialises the widgets for the mixing interface
 		 */
 		void createMixingWidgets() {
 			// Initialise geometry
 			geom = new RectangleGeometry(Window.getClientHeight()
 					- topBarHeight, Window.getClientWidth() - menuWidth);
 			StepAddedListener l = new StepAddedListener() {
 				@Override
 				public void onStepAdded(MixingStep step) {
 					addStep(step);
 				}
 			};
 
 			geom.addStepAddedListener(l);
 
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
 
 			// TODO: Initialise other menu items and add them to menuPanel
 
 			// Initialise a spinner for changing the length of a mixing protocol
 			// step
 			// and add to menuPanel.
 			createStepSizeSpinner();
 			menuPanel.add(sizeLabel);
 			menuPanel.add(sizeSpinner);
 
 			// Initialise a spinner for #steps and add to
 			// menuPanel.
 			createNrStepsSpinner();
 			menuPanel.add(nrStepsLabel);
 			menuPanel.add(nrStepsSpinner);
 
 			createProtocolRepresentationTextArea();
 
 			// Add canvas and menuPanel to the panel
 			// Make the canvas the entire width of the
 			// screen except for the
 			// menuWidth
 			panel.setWidth("100%");
 			panel.add(geom.getCanvas());
 			panel.add(menuPanel);
 			panel.setCellWidth(menuPanel, Integer.toString(menuWidth));
 
 			// Add panel to RootPanel
 			RootPanel.get().add(panel);
 		}
 
 		/**
 		 * Get the {@link NodeInfo} that provides the children of the specified
 		 * value.
 		 */
 		public <T> NodeInfo<?> getNodeInfo(T value) {
 			if (value == null) {
 				// LEVEL 0. - Geometry
 				// We passed null as the root value. Return the Geometries.
 
 				// Create a data provider that contains the list of Geometries.
 				ListDataProvider<GeometryNames> dataProvider = new ListDataProvider<GeometryNames>(
 						geometries);
 
 				// Create a cell to display a Geometry.
 				Cell<GeometryNames> cell = new AbstractCell<GeometryNames>() {
 					@Override
 					public void render(Context context, GeometryNames value,
 							SafeHtmlBuilder sb) {
 						if (value != null) {
 							sb.appendEscaped(value.toString());
 						}
 					}
 				};
 				// Return a node info that pairs the data provider and the cell.
 				return new DefaultNodeInfo<GeometryNames>(dataProvider, cell);
 			} else if (value instanceof GeometryNames) {
 				// LEVEL 1 - Mixer (leaf)
 
 				// Construct a List<String> of MixerNames. This is needed for
 				// the DefaultNodeInfo to use TextCell()
 				// (it only works for strings)
 				List<String> mixerlist = new ArrayList<String>();
 
 				switch ((GeometryNames) value) {
 				case Rectangle:
 					for (RectangleMixers rm : RectangleMixers.values()) {
 						mixerlist.add(rm.toString());
 					}
 					break;
 				case exampleGeometry1:
 					for (ExampleGeometryMixers egm : ExampleGeometryMixers
 							.values()) {
 						mixerlist.add(egm.toString());
 					}
 					break;
 				}
 
 				// We want the children of the Geometry. Return the mixers.
 				ListDataProvider<String> dataProvider = new ListDataProvider<String>(
 						mixerlist);
 
 				// Use the shared selection model.
 				return new DefaultNodeInfo<String>(dataProvider,
 						new TextCell(), selectionModel, null);
 
 			}
 			return null;
 		}
 
 		/**
 		 * Check if the specified value represents a leaf node. Leaf nodes
 		 * cannot be opened.
 		 */
 		// You can define your own definition of leaf-node here.
 		public boolean isLeaf(Object value) {
 			// works because all non-leaf nodes are enums, only
 			// leaf nodes are String.
 			return value instanceof String;
 		}
 
 		/*
 		 * public void setCellBrowser(CellBrowser cellbrowser) { this.cb =
 		 * cellbrowser; }
 		 */
 	}
 
 	/*
 	 * initialises the spinner for the stepSize
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
 	}
 
 	private void createProtocolRepresentationTextArea() {
 		taProtocolRepresentation.setText("");
 		taProtocolRepresentation.setWidth(String.valueOf(menuWidth));
 		menuPanel.add(taProtocolRepresentation);
 	}
 
 	/*
 	 * Changes the current drawing colour from black to white, and from white to
 	 * black.
 	 */
 	private void toggleColor() {
 		if (toggleColor.isDown()) {
 			geom.setColor(CssColor.make("white"));
 		} else {
 			geom.setColor(CssColor.make("black"));
 		}
 	}
 
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
 					geom.setDrawingTool(new SquareDrawingTool(3));
 
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
 					geom.setDrawingTool(new CircleDrawingTool(3));
 
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
 
 	// --Methods for testing purposes only---------------------------------
 	/**
 	 * Updates the protocol label to show the textual representation of
 	 * {@code step}.
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
 
 	/*
 	 * Initialises the Load Distribution button. This button only exists for
 	 * testing purposes. When it is pressed, the distribution of the geometry is
 	 * set to a colour bar from black to white, from left to right. This
 	 * distribution is then drawn on the canvas, to demonstrate we can load an
 	 * arbitrary distribution, with 256 gray scale colours.
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
 				geom.drawDistribution(dist);
 			}
 		});
 	}
 
 	/**
 	 * Adds a new {@code MixingStep} to the mixing protocol, and updates the
 	 * text area {@code taProtocolRepresentation} accordingly.
 	 * 
 	 * @param step
 	 *            The {@code MixingStep} to be added.
 	 */
 	private void addStep(MixingStep step) {
 		step.setStepSize(as.getStepSize());
 		as.addMixingStep(step);
 		updateProtocolLabel(step);
 	}
 
 	/**
 	 * A semi-transparent windows that covers the entire application pops up
 	 * that blocks the user from accessing other features. A loading-icon will
 	 * be shown. {@code closeLoadingWindow()} removes this window.
 	 */
 	private void showLoadingWindow() {
 		RootPanel.get().add(loadPanel);
 	}
 
 	/**
 	 * Removes Removes the loading-window that {@code showLoadingWindow()} has
 	 * created.
 	 * 
 	 * @pre showLoadingWindow() has been executed
 	 */
 	private void closeLoadingWindow() {
 		loadPanel.removeFromParent();
 	}
 
 }
