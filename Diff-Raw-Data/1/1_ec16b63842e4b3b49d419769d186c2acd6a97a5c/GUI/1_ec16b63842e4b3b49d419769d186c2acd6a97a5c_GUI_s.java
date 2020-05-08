 //v1.0
 
 package com.hexcore.cas.ui;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import com.hexcore.cas.Server;
 import com.hexcore.cas.ServerEvent;
 import com.hexcore.cas.control.discovery.LobbyListener;
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.math.Vector3f;
 import com.hexcore.cas.model.Cell;
 import com.hexcore.cas.model.ColourRule;
 import com.hexcore.cas.model.ColourRule.Range;
 import com.hexcore.cas.model.ColourRuleSet;
 import com.hexcore.cas.model.ColourRuleSetParser;
 import com.hexcore.cas.model.ColourRuleSetWriter;
 import com.hexcore.cas.model.Grid;
 import com.hexcore.cas.model.GridType;
 import com.hexcore.cas.model.HexagonGrid;
 import com.hexcore.cas.model.RectangleGrid;
 import com.hexcore.cas.model.TriangleGrid;
 import com.hexcore.cas.model.VonNeumannGrid;
 import com.hexcore.cas.model.World;
 import com.hexcore.cas.rulesystems.CALCompiler;
 import com.hexcore.cas.rulesystems.CodeGen;
 import com.hexcore.cas.rulesystems.Rule;
 import com.hexcore.cas.rulesystems.RuleLoader;
 import com.hexcore.cas.ui.toolkit.Colour;
 import com.hexcore.cas.ui.toolkit.Event;
 import com.hexcore.cas.ui.toolkit.Fill;
 import com.hexcore.cas.ui.toolkit.Image;
 import com.hexcore.cas.ui.toolkit.LayoutParser;
 import com.hexcore.cas.ui.toolkit.Text;
 import com.hexcore.cas.ui.toolkit.Text.Size;
 import com.hexcore.cas.ui.toolkit.Theme;
 import com.hexcore.cas.ui.toolkit.Window;
 import com.hexcore.cas.ui.toolkit.Window.FileSelectResult;
 import com.hexcore.cas.ui.toolkit.WindowEventListener;
 import com.hexcore.cas.ui.toolkit.widgets.Button;
 import com.hexcore.cas.ui.toolkit.widgets.CheckBox;
 import com.hexcore.cas.ui.toolkit.widgets.ColourBox;
 import com.hexcore.cas.ui.toolkit.widgets.ColourPicker;
 import com.hexcore.cas.ui.toolkit.widgets.Container;
 import com.hexcore.cas.ui.toolkit.widgets.Dialog;
 import com.hexcore.cas.ui.toolkit.widgets.DiscreteSliderWidget;
 import com.hexcore.cas.ui.toolkit.widgets.DropDownBox;
 import com.hexcore.cas.ui.toolkit.widgets.Grid2DWidget;
 import com.hexcore.cas.ui.toolkit.widgets.Grid3DWidget;
 import com.hexcore.cas.ui.toolkit.widgets.GridWidget;
 import com.hexcore.cas.ui.toolkit.widgets.GridWidget.Slice;
 import com.hexcore.cas.ui.toolkit.widgets.HexagonGrid3DWidget;
 import com.hexcore.cas.ui.toolkit.widgets.HexagonGridWidget;
 import com.hexcore.cas.ui.toolkit.widgets.ImageWidget;
 import com.hexcore.cas.ui.toolkit.widgets.LinearLayout;
 import com.hexcore.cas.ui.toolkit.widgets.ListWidget;
 import com.hexcore.cas.ui.toolkit.widgets.NumberBox;
 import com.hexcore.cas.ui.toolkit.widgets.Panel;
 import com.hexcore.cas.ui.toolkit.widgets.RectangleGrid3DWidget;
 import com.hexcore.cas.ui.toolkit.widgets.RectangleGridWidget;
 import com.hexcore.cas.ui.toolkit.widgets.ScrollableContainer;
 import com.hexcore.cas.ui.toolkit.widgets.SliderWidget;
 import com.hexcore.cas.ui.toolkit.widgets.TabbedView;
 import com.hexcore.cas.ui.toolkit.widgets.TextArea;
 import com.hexcore.cas.ui.toolkit.widgets.TextWidget;
 import com.hexcore.cas.ui.toolkit.widgets.TriangleGrid3DWidget;
 import com.hexcore.cas.ui.toolkit.widgets.TriangleGridWidget;
 import com.hexcore.cas.ui.toolkit.widgets.View;
 import com.hexcore.cas.ui.toolkit.widgets.VonNeumannGrid3DWidget;
 import com.hexcore.cas.ui.toolkit.widgets.VonNeumannGridWidget;
 import com.hexcore.cas.ui.toolkit.widgets.Widget;
 import com.hexcore.cas.utilities.HeightMapConverter;
 import com.hexcore.cas.utilities.Log;
 
 /**
  * Class GUI
  * 
  * @authors Divan Burger; Leon Van Dyk
  */
 
 public class GUI implements WindowEventListener, LobbyListener
 {
 	enum EditorToolType {LOOK, BRUSH, FILL};
 	
 	//HISTORY HELPERS//
 	private boolean					recreate = true;
 	///////////////////
 	
 	//OUR WORLD///
 	public World					world;
 	//////////////
 	
 	//OUR VIEWPORTS//
 	public ArrayList<Viewport>		viewports;
 	public Viewport					selectedViewport;
 	////////////////
 	
 	//GUI VARIABLES//
 	/////////////////////////////////////////////
 	/// Public Variables
 	public static final String		TAG = "GUI";
 	
 	public Button					backButton;
 	
 	public LinearLayout				mainMenuLayout;
 	public LinearLayout				worldLayout;
 	
 	public Set<ClientEntry>			availableClients;   
 	public Set<ClientEntry>			usingClients;
 	
 	public TabbedView				tabbedWorldView;
 	public Theme					theme;
 	public View						masterView;
 	public Window					window;
 	
 	/////////////////////////////////////////////
 	/// Private Variables
 	private boolean					changePathMessageDestroyed = false;
 	private boolean					loadingMessageDestroyed = false;
 	private boolean					savingMessageDestroyed = false;
 	private boolean					streamingMessageDestroyed = false;
 	private boolean					shutDownMessageDestroyed = false;
 
 	private Dialog					changePathDialog;
 	private Dialog					loadingDialog;
 	private Dialog					savingDialog;
 	private Dialog					shutdownDialog;
 	private Dialog					streamingDialog;
 
 	private LinearLayout			changePathLayout;
 	private LinearLayout			loadingLayout;
 	private LinearLayout			savingLayout;
 	private LinearLayout			shutdownLayout;
 	private LinearLayout			streamingLayout;
 	
 	private LayoutParser			layoutParser = new LayoutParser();
 
 	private TextWidget				changePathMessage;
 	private TextWidget				changePathTitle;
 	private TextWidget				loadingMessage;
 	private TextWidget				loadingTitle;
 	private TextWidget				savingMessage;
 	private TextWidget				savingTitle;
 	private TextWidget				shutdownMessage;
 	private TextWidget				shutdownTitle;
 	private TextWidget				streamingMessage;
 	private TextWidget				streamingTitle;
 	//////////////
 	
 	//WORLD PROPERTIES TAB//
 	public CheckBox					wrapCheckBox;
 	public CheckBox					keepHistoryCheckBox;
 	
 	public Container				propertiesContainer;
 	public Container				widget3DPreviewContainer;
 	public Container				widgetPreviewContainer;
 	
 	public DropDownBox				cellShapeDropDownBox;
 	public DropDownBox				historyDropDownBox;
 	
 	public LinearLayout				cellShapeLayout;
 	public LinearLayout				masterPropertiesLayout;
 	public LinearLayout				propertiesLayout;
 	public LinearLayout				widgetPreviewLayout;
 	public LinearLayout				worldSizeLayout;
 	
 	public TextWidget				cellShapeLabel;
 	public TextWidget				emptyLabel;
 	public TextWidget				engineLabel;
 	public TextWidget				worldSizeLabel;
 	public TextWidget				worldSizeXLabel;
 	
 	public NumberBox				engineNumberBox;
 	public NumberBox				worldSizeXNumberBox;
 	public NumberBox				worldSizeYNumberBox;
 	
 	public Grid3DWidget				grid3DViewer = null;
 	public Grid2DWidget				gridViewer = null;
 	
 	//RULES TAB//
 	public Button					clearRulesButton;
 	public Button					compareRulesButton;
 	public Button					dialogCALOKButton;
 	public Button					openCALFileButton;
 	public Button					saveCALFileButton;
 	public Button					submitRulesButton;
 	
 	public DropDownBox				rulesetsDropDownBox;
 	
 	public Container				rulesContainer;
 	public Dialog					dialogCAL;
 	public File						calFile;
 	public FileSelectResult			selectedFile;
 	
 	public LinearLayout				dialogCALLayout;
 	public LinearLayout				outputLayout;
 	public LinearLayout				rulesLayout;
 	
 	public ScrollableContainer		outputContainer;
 	public TextArea					CALTextArea;
 	
 	public TextWidget				dialogCALTitle;
 	public TextWidget				dialogCALMessage;
 	//////////////
 	
 	//DISTRIBUTION TAB//
 	public Container 				distributionContainer;
 	//////////////
 	
 	//MAIN MENU//
 	public Button					createWorldButton;
 	public Button					dialogOKButton;
 	public Button					helpButton;
 	public Button					loadWorldButton;
 	public Button					optionsButton;
 	public Button					quitButton;
 	
 	public ColourRuleSet			colourRules;
 	public Dialog					dialog;
 	
 	public LinearLayout				buttonBarLayout;
 	public LinearLayout				dialogLayout;
 	public LinearLayout				headerLayout;
 	public LinearLayout				innerLayout;
 	public LinearLayout				mainLayout;
 	
 	public Panel					mainPanel;
 	
 	public String					currentThemeName = "lightV2";
 	public String					themeName = currentThemeName;
 	
 	public TextWidget				dialogTitle;
 	public TextWidget				dialogMessage;
 	
 	public View						mainView;
 	//////////////
 	
 	//SIMULATION SCREEN//
 	private Button					addViewportButton;
 	private Button					pauseButton;
 	private Button					playButton;
 	private Button					moveDownButton;
 	private Button					moveLeftButton;
 	private Button					moveRightButton;
 	private Button					moveUpButton;
 	private Button					pitchDownButton;
 	private Button					pitchUpButton;
 	private Button					refreshServerButton;
 	private Button					removeViewportButton;
 	private Button					resetButton;
 	private Button					saveAsCALFileButton;
 	private Button					simulateButton;
 	private Button					stepForwardButton;
 	private Button					toggle3dButton;
 	private Button					toggleHideButton;
 	private Button					toggleShowButton;
 	private Button					toggleWireframeButton;
 	private Button					yawLeftButton;
 	private Button					yawRightButton;
 	private Button					zoomInButton;
 	private Button					zoomOutButton;
 	
 	private DiscreteSliderWidget	generationSlider;
 	private Container				coloursContainer;
 	private Grid					currentGrid;
 	private ImageWidget				viewSettingsHeader;
 	private int						currentGeneration = 0;
 	
 	private LinearLayout			viewportsLayout;
 	private LinearLayout			controlLayout;
 	private LinearLayout			masterColoursLayout;
 	private LinearLayout			masterSimulationLayout;
 	
 	private Server					server;
 	private TextWidget				iterationsText;
 	//////////////
 	
 	//DISTRIBUTION TAB//
 	/////////////////////////////////////////////
 	/// Public Variables
 	public Image					computerIcon;
 	public Image					computerLinkIcon;
 	
 	public ListWidget				clientsAvailableList;
 	public ListWidget				clientsUsingList;
 	
 	/////////////////////////////////////////////
 	/// Private Variables
 	private Button					addAllClientsButton;
 	private Button					addClientButton;
 	private Button					removeAllClientsButton;
 	private Button					removeClientButton;
 	//////////////
 	
 	//WORLD EDITOR
 	private ArrayList<NumberBox>	numberboxList;
 	
 	private Button					addSliceButton;
 	private Button					backToMainMenuButton;
 	private Button					editorApplyButton;
 	private Button					editorBrushButton;
 	private Button					editorFillButton;
 	private Button					editorLookButton;
 	private Button					importHeightMapButton;
 	private Button					resetCameraButton;
 	private Button					resetColourRangesButton;
 	private Button					saveWorldButton;
 	private Button					setColourRangesButton;
 	
 	private Cell					c;
 	private ColourPicker			colourPicker;
 	private Container				previewWindowContainer;
 	
 	private DropDownBox				heightMapPropertySelector;
 	private DropDownBox				worldEditorPropertySelector;
 	
 	private EditorToolType			editorToolType = EditorToolType.LOOK;
 	private List<ColourContainer>	colourContainerList;
 	
 	private LinearLayout			cpRGBLayout;
 	private LinearLayout			colourButtonsLayout;
 	private LinearLayout			colourPropertiesLayout;
 	private LinearLayout			leftColourLayout;
 	private LinearLayout			masterWorldPreviewLayout;
 	private LinearLayout			propertyValues;
 	private LinearLayout			rightColourLayout;
 	private LinearLayout			simulationControlsLayout;
 	private LinearLayout			topLayout;
 	private LinearLayout			worldEditorLeftLayout;
 	private LinearLayout			worldEditorRightLayout;
 	private LinearLayout			worldHeaderLayout;
 	
 	private NumberBox				cpBNumberBox;
 	private NumberBox				cpGNumberBox;
 	private NumberBox				cpRNumberBox;
 	
 	private ScrollableContainer		colourPropertiesContainer;
 	private ScrollableContainer		propertyValuesContainer;
 	
 	private SliderWidget			playbackSpeedSlider;
 	private Viewport				previewViewport;
 	//////////////
 	
 	public GUI(Server server)
 	{
 		this.server = server;
 		
 		availableClients = new TreeSet<ClientEntry>();
 		usingClients = new TreeSet<ClientEntry>();
 		
 		colourRules = new ColourRuleSet(4);
 		ColourRule	colourRule;
 		
 		colourRule = new ColourRule();
 		colourRule.addRange(new ColourRule.Range(0.0, 1.0, new Colour(1.0f, 0.0f, 0.0f)));
 		colourRules.setColourRule(0, colourRule);
 		
 		colourRule = new ColourRule();
 		colourRule.useClosestRange = true;
 		colourRule.addRange(new ColourRule.Range(0.0, 10.0, new Colour(0.0f, 0.25f, 0.5f)));
 		colourRule.addRange(new ColourRule.Range(10.0, 20.0, new Colour(0.0f, 0.8f, 0.5f)));
 		colourRules.setColourRule(1, colourRule);
 		
 		colourRule = new ColourRule();
 		colourRule.addRange(new ColourRule.Range(0.0, 15.1, new Colour(0.0f, 0.5f, 0.8f), new Colour(0.0f, 0.25f, 0.5f)));
 		colourRules.setColourRule(2, colourRule);	
 		
 		colourRule = new ColourRule();
 		colourRule.addRange(new ColourRule.Range(0.0, 8.0, new Colour(0.5f, 0.25f, 0.0f), new Colour(0.0f, 0.8f, 0.5f)));
 		colourRule.addRange(new ColourRule.Range(8.0, 16.0, new Colour(0.0f, 0.8f, 0.5f), new Colour(0.4f, 1.0f, 0.8f)));
 		colourRules.setColourRule(3, colourRule);  
 		
 		theme = new Theme();
 		window = new Window("Cellular Automata Simulator - v1.0", 1366, 700, theme);
 		
 		window.addListener(this);
 		
 		WindowListener exitListener = new WindowAdapter()
 		{
 			@Override
 			public void windowClosing(WindowEvent e)
 			{
 				shutdownProcess();
 			}
 		};
 		window.addWindowListener(exitListener);
 		
 		window.show();
 	}
 	
 	@Override
 	public boolean close()
 	{
 		ServerEvent serverEvent = new ServerEvent(ServerEvent.Type.SHUTDOWN);
 		server.sendEvent(serverEvent);
 		
 		return false;
 	}
 	
 	public void createColoursTab()
 	{
 		int numProperties = world.getInitialGeneration().getNumProperties();
 		
 		if(numProperties != colourRules.getNumProperties())
 			colourRules.resize(numProperties);
 		
 		if(numProperties > 0)
 		{
 			colourPropertiesLayout.clear();
 			 
 			colourContainerList = Collections.synchronizedList(new ArrayList<GUI.ColourContainer>());
 			
 			for(int i = 0 ; i < numProperties; i++)
 			{
 				ColourContainer c = new ColourContainer(i, window, CodeGen.getPropertyList().get(i), colourRules.getColourRule(i));	
 				colourPropertiesLayout.add(c.getLayout());
 				colourContainerList.add(c);
 			}
 		}
 	}
 	
 	public void createDistributionTab()
 	{
 		distributionContainer = new Container(new Vector2i(100, 100));
 		distributionContainer.setMargin(new Vector2i(0, 0));
 		distributionContainer.setFlag(Widget.FILL);
 		tabbedWorldView.add(distributionContainer, "Distribution Settings");
 		
 		LinearLayout masterDistributionLayout = (LinearLayout)layoutParser.parse("distributionTab", distributionContainer);
 		
 		// Left List
 		clientsAvailableList = (ListWidget)masterDistributionLayout.findByName("clientsAvailableList");
 		
 		// Center buttons
 		addClientButton = (Button)masterDistributionLayout.findByName("addButton");
 		addAllClientsButton = (Button)masterDistributionLayout.findByName("addAllButton");
 		removeClientButton = (Button)masterDistributionLayout.findByName("removeButton");
 		removeAllClientsButton = (Button)masterDistributionLayout.findByName("removeAllButton");
 		
 		// Right List
 		clientsUsingList = (ListWidget)masterDistributionLayout.findByName("clientsUsingList");
 		
 		// Controls
 		refreshServerButton = (Button)masterDistributionLayout.findByName("refreshButton");
 	}
 	
 	public void createWorldEditorTab()
 	{
 		masterWorldPreviewLayout = new LinearLayout(LinearLayout.Direction.HORIZONTAL);
 		masterWorldPreviewLayout.setMargin(new Vector2i(0, 0));
 		masterWorldPreviewLayout.setFlag(Widget.FILL);
 		tabbedWorldView.add(masterWorldPreviewLayout, "World Editor");
 		
 		worldEditorLeftLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		worldEditorLeftLayout.setFlag(Widget.FILL);
 		masterWorldPreviewLayout.add(worldEditorLeftLayout);
 		
 		// Preview window
 		previewWindowContainer = new Container(new Vector2i(100,100));
 		previewWindowContainer.setFlag(Widget.FILL);
 		previewWindowContainer.setBackground(new Fill(new Colour(0f,0f,0f))); 
 		
 		previewViewport = new Viewport(previewWindowContainer, Viewport.Type.TWO_D, colourRules);	
 		worldEditorLeftLayout.add(previewViewport.container);
 		
 		worldEditorRightLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		worldEditorRightLayout.setFlag(Widget.FILL_VERTICAL | Widget.WRAP_HORIZONTAL);
 		masterWorldPreviewLayout.add(worldEditorRightLayout);
 		
 		TextWidget label = new TextWidget("Display property");
 		worldEditorRightLayout.add(label);
 		
 		worldEditorPropertySelector = new DropDownBox(new Vector2i(100, 20));
 		worldEditorPropertySelector.setFlag(Widget.FILL_HORIZONTAL);
 		worldEditorRightLayout.add(worldEditorPropertySelector);
 		
 		// Tools
 		LinearLayout toolLayout = new LinearLayout(LinearLayout.Direction.HORIZONTAL);
 		toolLayout.setFlag(Widget.WRAP | Widget.CENTER_HORIZONTAL);
 		worldEditorRightLayout.add(toolLayout);
 		
 		editorLookButton = new Button(window.getTheme().getImage("icons", "zoom_icon.png"));
 		editorLookButton.setToggles(true);
 		editorLookButton.setToggleState(true);
 		toolLayout.add(editorLookButton);
 		
 		editorBrushButton = new Button(window.getTheme().getImage("icons", "brush_icon.png"));
 		editorLookButton.setToggles(false);
 		toolLayout.add(editorBrushButton);
 		
 		editorFillButton = new Button(window.getTheme().getImage("icons", "fill_icon.png"));
 		editorLookButton.setToggles(false);
 		toolLayout.add(editorFillButton);
 		
 		// Property values
 		propertyValuesContainer = new ScrollableContainer(new Vector2i(20, 20));
 		propertyValuesContainer.setFlag(Widget.FILL);
 		worldEditorRightLayout.add(propertyValuesContainer);
 		
 		propertyValues = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		propertyValues.setMargin(new Vector2i(0, 0));
 		propertyValues.setFlag(Widget.WRAP);
 		propertyValuesContainer.setContents(propertyValues);
 		
 		editorApplyButton = new Button(new Vector2i(180, 40), "Apply");
 		editorApplyButton.setFlag(Widget.CENTER_HORIZONTAL);
 		worldEditorRightLayout.add(editorApplyButton);
 		
 		LinearLayout heightMapLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		heightMapLayout.setBorder(new Fill(new Colour(0.7F, 0.7F, 0.7F)));
 		heightMapLayout.setMargin(new Vector2i(0, 0));
 		heightMapLayout.setFlag(Widget.WRAP_VERTICAL | Widget.FILL_HORIZONTAL);
 		worldEditorRightLayout.add(heightMapLayout);
 		
 		ImageWidget heightMapWidgetHeader = new ImageWidget(theme.getImage("headers", "heightmap_widget_header.png"));
 		heightMapLayout.add(heightMapWidgetHeader);
 		
 		TextWidget label1 = new TextWidget("Property:");
 		heightMapLayout.add(label1);
 		
 		heightMapPropertySelector = new DropDownBox(new Vector2i(100, 20));
 		heightMapPropertySelector.setFlag(Widget.FILL_HORIZONTAL);
 		heightMapLayout.add(heightMapPropertySelector);
 		
 		importHeightMapButton = new Button(new Vector2i(150,40), "Import Heightmap");
 		importHeightMapButton.setFlag(Widget.FILL_HORIZONTAL);
 		heightMapLayout.add(importHeightMapButton); 
 	}
 	
 	public void destroyChangePathMessage()
 	{
 		if(!changePathMessageDestroyed)
 		{
 			window.closeModalDialog();
 			changePathMessageDestroyed = true;
 		}
 	}
 	
 	public void destroyLoadingMessage()
 	{
 		if(!loadingMessageDestroyed)
 		{
 			window.closeModalDialog();
 			loadingMessageDestroyed = true;
 		}
 		
 	}
 	
 	public void destroySavingMessage()
 	{
 		if(!savingMessageDestroyed)
 		{
 			window.closeModalDialog();
 			savingMessageDestroyed = true;
 		}
 	}
 	
 	public void destroyStreamingMessage()
 	{
 		if(!streamingMessageDestroyed)
 		{
 			window.closeModalDialog();
 			streamingMessageDestroyed = true;
 		}
 	}
 	
 	public void destroyShutDownMessage()
 	{
 		if(!shutDownMessageDestroyed)
 		{
 			window.closeModalDialog();
 			shutDownMessageDestroyed = true;
 		}
 	}
 	
 	public void displayChangePathMessage()
 	{
 		changePathMessageDestroyed = false;
 		
 		Log.information(TAG, "Changing path process initiated.");
 		
 		window.showModalDialog(changePathDialog);
 	}
 	
 	public void displayLoadingMessage()
 	{
 		loadingMessageDestroyed = false;
 		
 		Log.information(TAG, "Loading world process initiated.");
 		
 		window.showModalDialog(loadingDialog);
 	}
 	
 	public void displaySavingMessage()
 	{
 		savingMessageDestroyed = false;
 		
 		Log.information(TAG, "Saving world process initiated.");
 		
 		window.showModalDialog(savingDialog);
 	}
 	
 	public void displayStreamingMessage()
 	{
 		streamingMessageDestroyed = false;
 		
 		Log.information(TAG, "Streaming world process initiated.");
 		
 		window.showModalDialog(streamingDialog);
 	}
 	
 	public void displayShutdownMessage()
 	{
 		shutDownMessageDestroyed = false;
 		
 		Log.information(TAG, "Shutdown world process initiated.");
 		
 		window.showModalDialog(shutdownDialog);
 	}
 	
 	@Override
 	public void foundClient(InetSocketAddress address)
 	{
 		if(availableClients != null)
 		{
 			if(address.getHostName().equals("localhost"))
 				return;
 				
 			ClientEntry clientEntry = new ClientEntry(address);
 			availableClients.add(clientEntry);
 			
 			updateAvailableClientsList();
 		}
 	}
 	
 	@Override
 	public void handleWindowEvent(Event event)
 	{
 		if(event.type == Event.Type.GAINED_FOCUS)
 		{
 			for(Viewport viewport : viewports)
 				if((viewport.gridWidget != null) && viewport.gridWidget.hasFocus() && selectedViewport != viewport)
 				{
 					selectedViewport = viewport;
 					selectedViewport.updateControlPanel(controlLayout, addSliceButton);
 				}
 			
 			for(Viewport viewport : viewports)
 				if(viewport == selectedViewport)
 					viewport.container.setBackground(new Fill(new Colour(0.8f, 0.2f, 0.2f)));
 				else
 					viewport.container.setBackground(new Fill(Colour.BLACK));
 		}
 		else if(event.type == Event.Type.ACTION)
 		{
 			if(event.target == resetCameraButton)
 			{
 				for(Viewport viewport : viewports)
 				{
 					if(viewport.gridWidget.hasFocus())
 					{
 						if(viewport.type == Viewport.Type.THREE_D)
 						{
 							Grid3DWidget temp3DWidget = (Grid3DWidget) viewport.gridWidget;
 							temp3DWidget.resetCamera();
 						}
 					}
 				}
 				
 			}
 			
 			if(event.target == importHeightMapButton)
 			{
 				FileSelectResult result = window.askUserForFileToLoad("Load a world");
 				
 				if(result.isValid())
 				{
 					HeightMapConverter hmc = new HeightMapConverter();
 					hmc.loadHeightMap(result.getFullPath(), world.getLastGeneration(), heightMapPropertySelector.getSelected());
 				}
 				else
 				{
 					System.out.println("PUT ERROR DIALOG HERE");	
 				}
 			}
 			
 			if(colourContainerList != null)
 			{
 				for(ColourContainer c : colourContainerList)
 				{
 					for(RangeContainer r: c.rangeContainerList)
 					{
 						if(event.target == r.colourBoxFrom)
 						{
 							r.fromColour = colourPicker.getColour();
 							r.colourBoxFrom.setColour(colourPicker.getColour());
 						}
 						
 						if(event.target == r.colourBoxTo)
 						{
 							r.toColour = colourPicker.getColour();
 							r.colourBoxTo.setColour(colourPicker.getColour());
 						}
 					}
 						
 				}
 			}
 			
 			if(colourContainerList != null)
 			{
 				for(ColourContainer c : colourContainerList)
 				{System.out.println("CHECK BUTTON FOR ID:" + c.id);
 					if(event.target == c.addRangeButton)
 					{
 						System.out.println("ADD RANGE BUTTON PRESSED FOR: " + c.id);
 						
 						c.addRange(c.id);
 						window.relayout();
 					}	
 					
 					if(event.target == c.removeRangeButton)
 					{
 						if(!c.rangeContainerList.isEmpty())
 						{
 							c.removeRange();
 							createColoursTab();
 						}
 					}
 				}
 			}
 			
 			if(event.target == setColourRangesButton)
 			{
 				
 				for(ColourContainer c : colourContainerList)
 				{
 					ColourRule cr = new ColourRule();
 					
 					for(RangeContainer r : c.rangeContainerList)
 					{
 						Range range = new Range(r.firstRange.getValue(0), r.secondRange.getValue(0), r.fromColour, r.toColour);
 						
 						cr.addRange(range);
 						
 						System.out.println("ADDING RANGE FOR ID: " + CodeGen.getPropertyList().get(r.id) + r.fromColour);
 					}
 					colourRules.setColourRule(c.id, cr);
 				} 
 				saveColourCodeToWorld();
 			}
 			
 			if(event.target == createWorldButton)
 			{
 				recreate = true;
 				
 				ServerEvent serverEvent = new ServerEvent(ServerEvent.Type.CREATE_WORLD);
 				serverEvent.size = new Vector2i(32, 32);
 				serverEvent.gridType = GridType.RECTANGLE;
 				serverEvent.wrappable = true;
 				server.sendEvent(serverEvent);
 				
 				refreshClients();
 			}
 			else if(event.target == loadWorldButton)
 			{
 				recreate = true;
 				displayLoadingMessage();
 				
 				FileSelectResult result = window.askUserForFileToLoad("Load a world");
 				
 				if(result.isValid())
 				{
 					ServerEvent serverEvent = new ServerEvent(ServerEvent.Type.LOAD_WORLD);
 					serverEvent.filename = result.getFullPath();
 					server.sendEvent(serverEvent);
 				}
 				else
 					destroyLoadingMessage();
 				
 				refreshClients();
 			}
 			else if(event.target == saveWorldButton)
 			{
 				int selectedMemory = historyDropDownBox.getSelected();
 				if(selectedMemory == 2)
 					displayChangePathMessage();
 				else
 					displaySavingMessage();
 				
 				FileSelectResult result = window.askUserForFileToSave("world");
 				
 				if(result.isValid())
 				{
 					world.setFileName(result.getFullPath());
 					
 					if(selectedMemory != 2)
 					{
 						ServerEvent serverEvent = new ServerEvent(ServerEvent.Type.SAVE_WORLD);
 						server.sendEvent(serverEvent);
 					}
 				}
 				
 				if(selectedMemory == 2)
 					destroyChangePathMessage();
 				else
 					destroySavingMessage();
 				
 				refreshClients();
 			}
 			else if(event.target == helpButton)
 			{
 			}
 			else if(event.target == quitButton)
 			{
 				shutdownProcess();
 			}
 			else if((event.target == dialogOKButton) || (event.target == dialogCALOKButton))
 			{
 				window.closeModalDialog();
 			}
 			else if(event.target == backButton)
 			{
 				masterView.setIndex(0);
 			}
 			//RULES TAB
 			else if(event.target == clearRulesButton)
 			{
 				CALTextArea.clear();
 			}
 			else if(event.target == submitRulesButton)
 			{
 				String CALCode = CALTextArea.getText();
 				CALCompiler compiler = new CALCompiler();
 				compiler.compile(CALCode);
 				
 				TextWidget text = new TextWidget("Compiler Report:");
 				outputLayout.add(text);
 				
 				outputLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 				outputLayout.setMargin(new Vector2i(0, 0));
 				outputLayout.setFlag(Widget.WRAP);
 				outputContainer.setContents(outputLayout);
 				
 				for(String result : compiler.getResult())
 				{
 					outputLayout.add(new TextWidget(result));
 					window.relayout();
 				}
 				
 				if(compiler.getErrorCount() == 0)
 				{
 					saveRuleCodeToWorld();
 					//Update ruleset
 				}
 				else
 				{
 					Log.information(TAG, "Rule code contains " + compiler.getErrorCount() + " errors");
 					//world.setRuleCode("");
 					//No change to current rulesets
 				}
 				
 				createColoursTab();
 			}
 			else if(event.target == compareRulesButton)
 			{
 				ArrayList<String> rulesets = world.getRuleCodes();
 				
 				TextWidget text = new TextWidget("Ruleset comparison");
 				outputLayout.add(text);
 				
 				outputLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 				outputLayout.setMargin(new Vector2i(0, 0));
 				outputLayout.setFlag(Widget.WRAP);
 				outputContainer.setContents(outputLayout);
 				
 				if(rulesets.size() == 1)
 					outputLayout.add(new TextWidget("There is only a single ruleset. Comparison is unecessary."));
 				else
 				{
 					ArrayList<String> results = world.compareRulesets();
 					if(results.size() == 0)
 						outputLayout.add(new TextWidget("There is no descrepencies between the rulesets."));
 					else
 						for(int i = 0; i < results.size(); i++)
 							outputLayout.add(new TextWidget(results.get(i)));
 				}
 				
 				window.relayout();
 			}
 			else if(event.target == saveCALFileButton)
 			{
 				if(selectedFile == null)
 				{
 					selectedFile = window.askUserForFileToSave("Select a location to save", "cal");
 					
 					String fullPath = selectedFile.getFullPath();
 					
 					File f = null;
 					if(fullPath.contains(".cal"))
 						f = new File(fullPath);
 					else
 						f = new File(fullPath + ".cal");
 					
 					try
 					{
 						f.createNewFile();
 						PrintWriter out = new PrintWriter(f);
 						
 						out.write(CALTextArea.getText());
 						
 						out.close();
 					}
 					catch(IOException e)
 					{
 						e.printStackTrace();
 					}
 				}
 				else
 				{
 					String fullPath = selectedFile.getFullPath();
 					
 					File f = null;
 					if(fullPath.contains(".cal"))
 						f = new File(fullPath);
 					else
 						f = new File(fullPath + ".cal");
 					
 					try
 					{
 						PrintWriter out = new PrintWriter(f);
 						out.write(CALTextArea.getText());
 						out.close();
 					}
 					catch(IOException e)
 					{
 						e.printStackTrace();
 					}
 				}
 			}
 			else if(event.target == saveAsCALFileButton)
 			{
 				FileSelectResult calFile = window.askUserForFileToSave("Select a location to save", "cal");
 				
 				String fullPath = calFile.getFullPath();
 				
 				File f = null;
 				if(fullPath.contains(".cal"))
 					f = new File(fullPath);
 				else
 					f = new File(fullPath + ".cal");
 				
 				System.out.println(calFile.getFullPath() + "/" + calFile.filename);
 				try
 				{
 					f.createNewFile();
 					PrintWriter out = new PrintWriter(f);
 					out.write(CALTextArea.getText());
 					out.close();
 				}
 				catch(IOException e)
 				{
 					e.printStackTrace();
 				}
 			}
 			else if(event.target == openCALFileButton)
 			{
 				selectedFile = window.askUserForFileToLoad("Select CAL File", "cal");
 				
 				System.out.println(selectedFile.directory);
 				System.out.println(selectedFile.filename);
 				
 				if(selectedFile.isValid())
 				{
 					if(selectedFile.filename.contains(".cal"))
 					{
 						try
 						{
 							FileInputStream fstream = new FileInputStream(selectedFile.directory + selectedFile.filename);
 							DataInputStream in = new DataInputStream(fstream);
 							BufferedReader br = new BufferedReader(new InputStreamReader(in));
 							String strLine;
 							String output = "";
 							
 							while((strLine = br.readLine()) != null)
 								output += strLine + "\n";
 							
 							in.close();
 							
 							CALTextArea.setText(output);
 						}
 						catch (Exception e)
 						{
 							window.showModalDialog(dialogCAL);
 						}
 					} 
 				}
 			}
 			//OTHER
 			else if(event.target == simulateButton)
 			{
 				String ruleCode = world.getRuleCode();
 				if(ruleCode == null || ruleCode.equals(""))
 				{
 					showDialog("Simulation", "Cell rules not set yet");
 					return;
 				}
 				
 				List<InetSocketAddress> clients = new ArrayList<InetSocketAddress>();
 				
 				for(ClientEntry clientEntry : usingClients)
 					clients.add(clientEntry.address);
 				
 				ServerEvent serverEvent = new ServerEvent(ServerEvent.Type.READY_SIMULATION);
 				serverEvent.clients = clients;
 				server.sendEvent(serverEvent);
 				
 				masterView.setIndex(2);
 				Log.information(TAG, "Switched to simulation screen");
 				
 				currentGeneration = 0;
 				updateSimulationScreen(true);
 			}
 			else if(event.target == backToMainMenuButton)
 			{
 				ServerEvent serverEvent = new ServerEvent(ServerEvent.Type.STOP_SIMULATION);
 				server.sendEvent(serverEvent);
 				
 				masterView.setIndex(1);
 				
 				updateWorldSettings();
 			}
 			else if(event.target == playButton)
 			{
 				ServerEvent serverEvent = new ServerEvent(ServerEvent.Type.START_SIMULATION);
 				server.sendEvent(serverEvent);
 			}
 			else if(event.target == pauseButton)
 			{
 				ServerEvent serverEvent = new ServerEvent(ServerEvent.Type.PAUSE_SIMULATION);
 				server.sendEvent(serverEvent);
 			}			
 			else if(event.target == resetButton)
 			{
 				ServerEvent serverEvent = new ServerEvent(ServerEvent.Type.RESET_SIMULATION);
 				server.sendEvent(serverEvent);
 			}		
 			else if(event.target == stepForwardButton)
 			{
 				ServerEvent serverEvent = new ServerEvent(ServerEvent.Type.STEP_SIMULATION);
 				server.sendEvent(serverEvent);		   	
 			}
 			else if(event.target == addViewportButton)
 			{
 				Container container = new Container(new Vector2i(100,300));
 				container.setFlag(Widget.FILL);
 				container.setBackground(new Fill(new Colour(0f,0f,0f)));
 				viewportsLayout.add(container);
 				
 				Viewport viewport = new Viewport(container, Viewport.Type.THREE_D, colourRules);
 				viewport.recreate(currentGrid, window, colourRules);
 				
 				viewports.add(viewport);
 			}
 			else if(event.target == removeViewportButton)
 			{
 				System.out.println("VIEWPORT:::::::::::::::::::;;" + viewports.size());
 				
 				if(viewports.size() > 1)
 				{
 					viewports.remove(viewports.size() - 1);
 					reconstructViewportLayout();
 				}
 			}
 			else if(event.target == addSliceButton)
 			{
 				if(selectedViewport != null)
 				{
 					Slice slice = new Slice(1, 10.0f);
 					selectedViewport.gridWidget.addSlice(slice);
 					selectedViewport.updateControlPanel(controlLayout, addSliceButton);
 				}
 			}
 			// WORLD EDITOR GRID
 			else if(event.target == editorLookButton)
 			{
 				editorToolType = EditorToolType.LOOK;
 				editorLookButton.setToggleState(true);
 				editorBrushButton.setToggleState(false);
 				editorFillButton.setToggleState(false);
 				updateWorldEditorTool();
 			}
 			else if(event.target == editorBrushButton)
 			{
 				editorToolType = EditorToolType.BRUSH; 
 				editorLookButton.setToggleState(false);
 				editorBrushButton.setToggleState(true);
 				editorFillButton.setToggleState(false);
 				updateWorldEditorTool();
 			}
 			else if(event.target == editorFillButton)
 			{
 				editorToolType = EditorToolType.FILL;  
 				editorLookButton.setToggleState(false);
 				editorBrushButton.setToggleState(false);
 				editorFillButton.setToggleState(true);
 				updateWorldEditorTool();
 			}
 			// VIEWPORT CAMERA
 			else if(event.target == zoomOutButton)
 			{
 				for(Viewport viewport : viewports)
 				{
 					if(viewport.gridWidget.hasFocus())
 					{
 						if(viewport.type == Viewport.Type.THREE_D)
 						{
 							Grid3DWidget temp3DWidget = (Grid3DWidget) viewport.gridWidget;
 							temp3DWidget.move(new Vector3f(0, 0, 1));
 						}
 					}
 				}
 			}
 			else if(event.target == zoomInButton)
 			{
 				for(Viewport viewport : viewports)
 				{
 					if(viewport.gridWidget.hasFocus())
 					{
 						if(viewport.type == Viewport.Type.THREE_D)
 						{
 							Grid3DWidget temp3DWidget = (Grid3DWidget) viewport.gridWidget;
 							temp3DWidget.move(0, 0, -1);
 						}
 					}
 				}
 			}
 			else if(event.target == zoomOutButton)
 			{
 				for(Viewport viewport : viewports)
 				{
 					if(viewport.gridWidget.hasFocus())
 					{
 						if(viewport.type == Viewport.Type.THREE_D)
 						{
 							Grid3DWidget temp3DWidget = (Grid3DWidget) viewport.gridWidget;
 							temp3DWidget.move(0, 0, 1);
 						}
 					}
 				}
 			}
 			else if(event.target == moveUpButton)
 			{
 				for(Viewport viewport : viewports)
 				{
 					if(viewport.gridWidget.hasFocus())
 					{
 						if(viewport.type == Viewport.Type.THREE_D)
 						{
 							Grid3DWidget temp3DWidget = (Grid3DWidget) viewport.gridWidget;
 							temp3DWidget.move(0, -1, 0);
 						}
 					}
 				}
 			}
 			else if(event.target == moveDownButton)
 			{
 				for(Viewport viewport : viewports)
 				{
 					if(viewport.gridWidget.hasFocus())
 					{
 						if(viewport.type == Viewport.Type.THREE_D)
 						{
 							Grid3DWidget temp3DWidget = (Grid3DWidget) viewport.gridWidget;
 							temp3DWidget.move(0, 1, 0);
 						}
 					}
 				}
 			}
 			else if(event.target == moveLeftButton)
 			{
 				for(Viewport viewport : viewports)
 				{
 					if(viewport.gridWidget.hasFocus())
 					{
 						if(viewport.type == Viewport.Type.THREE_D)
 						{
 							Grid3DWidget temp3DWidget = (Grid3DWidget) viewport.gridWidget;
 							temp3DWidget.move(-1, 0, 0);
 						}
 					}
 				}
 			}
 			else if(event.target == moveRightButton)
 			{
 				for(Viewport viewport : viewports)
 				{
 					if(viewport.gridWidget.hasFocus())
 					{
 						if(viewport.type == Viewport.Type.THREE_D)
 						{
 							Grid3DWidget temp3DWidget = (Grid3DWidget) viewport.gridWidget;
 							temp3DWidget.move(1, 0, 0);
 						}
 						}
 					}
 			}
 			else if(event.target == yawLeftButton)
 			{
 				for(Viewport viewport : viewports)
 				{
 					if(viewport.gridWidget.hasFocus())
 					{
 						if(viewport.type == Viewport.Type.THREE_D)
 						{
 							Grid3DWidget temp3DWidget = (Grid3DWidget) viewport.gridWidget;
 							temp3DWidget.changeYaw(2);
 						}
 					}
 				}
 			}
 			else if(event.target == yawRightButton)
 			{
 				for(Viewport viewport : viewports)
 				{
 					if(viewport.gridWidget.hasFocus())
 					{
 						if(viewport.type == Viewport.Type.THREE_D)
 						{
 							Grid3DWidget temp3DWidget = (Grid3DWidget) viewport.gridWidget;
 							temp3DWidget.changeYaw(-2);
 						}
 					}
 				}
 			}
 			else if(event.target == pitchUpButton)
 			{
 				for(Viewport viewport : viewports)
 				{
 					if(viewport.gridWidget.hasFocus())
 					{
 						if(viewport.type == Viewport.Type.THREE_D)
 						{
 							Grid3DWidget temp3DWidget = (Grid3DWidget) viewport.gridWidget;
 							temp3DWidget.changePitch(2);
 						}
 					}
 				}
 			}
 			else if(event.target == pitchDownButton)
 			{
 				for(Viewport viewport : viewports)
 				{
 					if(viewport.gridWidget.hasFocus())
 					{
 						if(viewport.type == Viewport.Type.THREE_D)
 						{
 							Grid3DWidget temp3DWidget = (Grid3DWidget) viewport.gridWidget;
 							temp3DWidget.changePitch(-2);
 						}
 					}
 				}
 			}
 			//VIEWPORT SETTINGS BUTTONS
 			else if(event.target == toggle3dButton)
 			{
 				if(selectedViewport != null)
 					selectedViewport.switchDimension(currentGrid, window);
 			}
 			else if(event.target == toggleHideButton)
 			{
 				controlLayout.setVisible(false);
 				simulationControlsLayout.setVisible(false);
 				toggleShowButton.setVisible(true);
 				worldHeaderLayout.setVisible(false);
 				backToMainMenuButton.setVisible(false);
 				window.relayout();
 			}
 			else if(event.target == toggleShowButton)
 			{
 				controlLayout.setVisible(true);
 				simulationControlsLayout.setVisible(true);
 				toggleShowButton.setVisible(false);
 				worldHeaderLayout.setVisible(true);
 				backToMainMenuButton.setVisible(true);
 				window.relayout();
 			}
 			else if(event.target == toggleWireframeButton)
 			{
 				if(selectedViewport != null)
 					selectedViewport.gridWidget.toggleDrawWireframe();
 			}
 			// DISTRIBUTION BUTTONS
 			else if(event.target == refreshServerButton)
 			{
 				refreshClients();
 			}
 			else if(event.target == addAllClientsButton)
 			{
 				for(ClientEntry clientEntry : availableClients) usingClients.add(clientEntry);
 				
 				availableClients.clear();
 				
 				updateAvailableClientsList();
 			}
 			else if(event.target == removeAllClientsButton)
 			{
 				for(ClientEntry clientEntry : usingClients) availableClients.add(clientEntry);
 				
 				usingClients.clear();
 				
 				updateAvailableClientsList();
 			} 
 			else if(event.target == addClientButton)
 			{
 				String hostname = clientsAvailableList.getSelectedText();
 				
 				for(ClientEntry clientEntry : availableClients)
 				{
 					if(clientEntry.address.getHostName().equals(hostname))
 					{
 						availableClients.remove(clientEntry);
 						usingClients.add(clientEntry);
 					}
 				}
 				
 				updateAvailableClientsList();
 			}
 			else if(event.target == removeClientButton)
 			{
 				String hostname = clientsUsingList.getSelectedText();
 				
 				for(ClientEntry clientEntry : usingClients)
 				{
 					if(clientEntry.address.getHostName().equals(hostname))
 					{
 						usingClients.remove(clientEntry);
 						availableClients.add(clientEntry);
 					}
 				}
 				
 				updateAvailableClientsList();
 			}
 			//COLOUR RANGES
 			///PREVIEW
 			 else if(event.target == editorApplyButton)
 			 {
 				 int numProperties = world.getInitialGeneration().getNumProperties();
 				 
 				 for(int i = 0; i < numProperties; i++)
 				 {
 					 int newValue = numberboxList.get(i).getValue(0);
 					 
 					 System.out.println("VALUE:" + newValue);
 					 
 					 c.setValue(i, newValue);
 				 }
 			 }
 		}
 		else if(event.type == Event.Type.CHANGE)
 		{
 			if(event.target == colourPicker)
 			{
 				cpRNumberBox.setValue((int) (colourPicker.getColour().r * 255));
 				cpGNumberBox.setValue((int) (colourPicker.getColour().g * 255));
 				cpBNumberBox.setValue((int) (colourPicker.getColour().b * 255));
 			}
 			
 			if(selectedViewport != null)
 			{
 				for(int i = 0; i < selectedViewport.propertyDropDownBoxes.size(); i++)
 				{
 					DropDownBox selection = selectedViewport.propertyDropDownBoxes.get(i);
 					int index = selection.getSelected();
 					
 					if(selectedViewport.type == Viewport.Type.THREE_D)
 					{
 						System.out.println("Changed slice property");
 						Grid3DWidget grid3DWidget = (Grid3DWidget)selectedViewport.gridWidget;
 						grid3DWidget.setSlice(i, index, index);
 					}
 				}
 			}
 			else if(event.target == worldSizeXNumberBox 
 					|| event.target == worldSizeYNumberBox
 					|| event.target == wrapCheckBox
 					|| event.target == cellShapeDropDownBox)
 			{
 				recreate = true;
 				
 				if(worldSizeXNumberBox.getValue(5) < 5) worldSizeXNumberBox.setValue(5);
 				if(worldSizeYNumberBox.getValue(5) < 5) worldSizeYNumberBox.setValue(5);
 				
 				savePropertiesToWorld();
 				//saveRuleCodeToWorld(); //Point of this?
 				updateWorldEditorTab();
 				//createColoursTab(); //Point of this?
 				updatePreview();
 			}
 			else if(event.target == engineNumberBox)
 			{
 				int steps = engineNumberBox.getValue(1);
 				if(steps < 1)
 				{
 					engineNumberBox.setValue(1);
 					steps = 1;
 				}
 
 				world.setRuleCodes(steps);
 				setRulesetsDropDownBox();
 			}
 			//Disable generation slider for no history keep
 			else if(event.target == historyDropDownBox)
 			{
 				int selected = historyDropDownBox.getSelected();
 				
 				if(selected == 0)
 					generationSlider.toggleActivation(false);
 				else
 					generationSlider.toggleActivation(true);
 				
 				if(selected != 2)
 					saveWorldButton.setCaption("Save World");
 				else
 					saveWorldButton.setCaption("Change Path");
 				
 				savePropertiesToWorld();
 				updateSimulationScreen(true);
 			}
 			else if(event.target == rulesetsDropDownBox)
 			{
 				String selectedRuleset = rulesetsDropDownBox.getSelectedText();
 				int colonIndex = selectedRuleset.indexOf(":");
 				String name = selectedRuleset.substring(colonIndex + 1);
 				System.out.println("Looking for name |" + name + "|");
 				String code = world.getRuleCode(name);
 				
 				CALTextArea.setText(code);
 			}
 			else if(event.target == worldEditorPropertySelector)
 			{
 				updateWorldEditorTab();
 				
 				GridWidget gw = previewViewport.gridWidget; 
 				gw.clearSlices();
 				gw.addSlice(worldEditorPropertySelector.getSelected(), 10.0f);
 			}
 			else if(event.target == generationSlider)
 			{
 				ServerEvent serverEvent = new ServerEvent(ServerEvent.Type.PAUSE_SIMULATION);
 				server.sendEvent(serverEvent);
 			}
 			else if(event.target == playbackSpeedSlider)
 			{
 				ServerEvent serverEvent = new ServerEvent(ServerEvent.Type.SET_PLAYBACK_SPEED);
 				serverEvent.milliseconds = (int)(playbackSpeedSlider.getValue() * 1000.0);
 				server.sendEvent(serverEvent);	
 			}
 			// WORLD EDITOR TAB
 			else if(event.target == previewViewport.gridWidget)
 			{
 				Grid2DWidget temp2DWidget = (Grid2DWidget)previewViewport.gridWidget;
 				Vector2i pos = temp2DWidget.getSelectedCell();
 				currentGrid = world.getLastGeneration();
 				
 				switch(editorToolType)
 				{
 					case LOOK:
 					{
 						c = currentGrid.getCell(pos);
 						
 						for(int i = 0 ; i < numberboxList.size(); i++)
 							numberboxList.get(i).setValue((int)c.getValue(i));
 						break;
 					}
 					case BRUSH:
 					{
 						c = currentGrid.getCell(pos);
 						
 						for(int i = 0 ; i < numberboxList.size(); i++)
 							c.setValue(i, numberboxList.get(i).getValue(0));
 						break;	
 					}
 					case FILL:
 					{
 						Cell cell = new Cell(currentGrid.getNumProperties());
 						
 						for(int i = 0 ; i < numberboxList.size(); i++)
 							cell.setValue(i, numberboxList.get(i).getValue(0));
 						
 						for(int y = 0 ; y < currentGrid.getHeight(); y++)
 							for(int x = 0 ; x < currentGrid.getWidth(); x++)
 								currentGrid.setCell(x, y, cell);
 						break;	
 					}
 				}
 				
 				previewViewport.gridWidget.setGrid(currentGrid);
 				
 				if(historyDropDownBox.getSelected() == 2)
 					world.resetTo(currentGrid);
 				
 				updatePreview();
 			}
 		}
 	}
 	
 	public void initialise()
 	{
 		theme.loadTheme(themeName);
 		
 		computerIcon = window.getTheme().getImage("icons", "computer.png");
 		computerLinkIcon = window.getTheme().getImage("icons", "computer_link.png");
 		
 		masterView = new View(new Vector2i(10, 10));
 		masterView.setMargin(new Vector2i(0, 0));
 		masterView.setFlag(Widget.FILL);
 		window.add(masterView);
 		
 		mainMenuLayout = (LinearLayout)layoutParser.parse("mainMenu", masterView);
 		
 		createWorldButton = (Button)mainMenuLayout.findByName("createWorld");
 		loadWorldButton = (Button)mainMenuLayout.findByName("loadWorld");
 		optionsButton = (Button)mainMenuLayout.findByName("options");
 		helpButton = (Button)mainMenuLayout.findByName("help");
 		quitButton = (Button)mainMenuLayout.findByName("quit");
 		
 		// Main WORLD BUILDER
 		worldLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		worldLayout.setFlag(Widget.FILL);
 		
 		masterView.add(worldLayout);
 		
 		tabbedWorldView = new TabbedView(new Vector2i(30,30));
 		tabbedWorldView.setFlag(Widget.FILL);
 		
 		worldHeaderLayout = new LinearLayout(new Vector2i(230, 40), LinearLayout.Direction.HORIZONTAL);
 		worldHeaderLayout.setFlag(Widget.FILL_HORIZONTAL | Widget.WRAP_VERTICAL);
 		worldLayout.add(worldHeaderLayout);
 		worldLayout.add(tabbedWorldView);
 		
 		propertiesContainer = new Container(new Vector2i(100, 100));	
 		propertiesContainer.setFlag(Widget.FILL);
 		tabbedWorldView.add(propertiesContainer, "World Properties");
 		
 		masterPropertiesLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		masterPropertiesLayout.setFlag(Widget.FILL);
 		propertiesContainer.setContents(masterPropertiesLayout);
 		
 		LinearLayout instructionsLayout = new LinearLayout(LinearLayout.Direction.HORIZONTAL);
 		instructionsLayout.setHeight(35);
 		instructionsLayout.setFlag(Widget.CENTER_HORIZONTAL);
 		instructionsLayout.setBorder(new Fill(new Colour(0.6f,0.6f,0.6f)));
 		masterPropertiesLayout.add(instructionsLayout);
 		
 		TextWidget propertiesInstructions = new TextWidget("Specify your world properties such as cell shape, world size and whether the world is wrappable.");
 		instructionsLayout.add(propertiesInstructions);
 		instructionsLayout.setWidth(propertiesInstructions.getWidth()+ 20);
 		
 		propertiesLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		propertiesLayout.setFlag(Widget.FILL);
 		propertiesLayout.setBorder(new Fill(new Colour(0.6f,0.6f,0.6f)));
 		masterPropertiesLayout.add(propertiesLayout);
 		
 		worldSizeLayout = new LinearLayout(LinearLayout.Direction.HORIZONTAL);
 		worldSizeLayout.setHeight(50);
 		worldSizeLayout.setFlag(Widget.FILL_HORIZONTAL);
 		
 		propertiesLayout.add(worldSizeLayout);
 		
 		worldSizeLabel = new TextWidget("World Size:", Size.MEDIUM);
 		worldSizeLabel.setFlag(Widget.CENTER_VERTICAL);
 		worldSizeLayout.add(worldSizeLabel);
 		
 		worldSizeXNumberBox = new NumberBox(35);
 		worldSizeXNumberBox.setWidth(50);  
 		worldSizeXNumberBox.setValue(10);
 		worldSizeXNumberBox.setFlag(Widget.CENTER_VERTICAL);
 		worldSizeLayout.add(worldSizeXNumberBox);
 		
 		worldSizeXLabel = new TextWidget("X", Size.LARGE);
 		worldSizeXLabel.setFlag(Widget.CENTER_VERTICAL);
 		worldSizeLayout.add(worldSizeXLabel);
 		
 		worldSizeYNumberBox = new NumberBox(35);
 		worldSizeYNumberBox.setWidth(50);  
 		worldSizeYNumberBox.setValue(10);
 		worldSizeYNumberBox.setFlag(Widget.CENTER_VERTICAL);
 		worldSizeLayout.add(worldSizeYNumberBox);
 		
 		wrapCheckBox = new CheckBox(new Vector2i(100,50), "Wrappable");
 		wrapCheckBox.setFlag(Widget.CENTER_VERTICAL);
 		wrapCheckBox.setMargin(new Vector2i(50,0));
 		worldSizeLayout.add(wrapCheckBox);
 		
 		historyDropDownBox = new DropDownBox(new Vector2i(135,20));
 		historyDropDownBox.setFlag(Widget.CENTER_VERTICAL);
 		historyDropDownBox.addItem("No History");
 		historyDropDownBox.addItem("Memory history");
 		historyDropDownBox.addItem("Harddisk history");
 		historyDropDownBox.setSelected(1);
 		worldSizeLayout.add(historyDropDownBox);
 		
 		cellShapeLayout = new LinearLayout(LinearLayout.Direction.HORIZONTAL);
 		cellShapeLayout.setFlag(Widget.FILL_HORIZONTAL);
 		cellShapeLayout.setHeight(65);
 		
 		propertiesLayout.add(cellShapeLayout);
 		
 		cellShapeLabel = new TextWidget("Cell Shape:",Size.MEDIUM);
 		cellShapeLabel.setFlag(Widget.CENTER_VERTICAL);
 		cellShapeLayout.add(cellShapeLabel);
 		
 		cellShapeDropDownBox = new DropDownBox(new Vector2i(160,20));
 		cellShapeDropDownBox.setFlag(Widget.CENTER_VERTICAL);
 		cellShapeDropDownBox.addItem("Square");
 		cellShapeDropDownBox.addItem("Triangle");
 		cellShapeDropDownBox.addItem("Hexagon");
 		cellShapeDropDownBox.addItem("Von Neumann Square");
 		cellShapeDropDownBox.setSelected(0);
 		
 		cellShapeLayout.add(cellShapeDropDownBox);
 
 		emptyLabel = new TextWidget("", Size.MEDIUM);
 		emptyLabel.setFlag(Widget.CENTER_VERTICAL);
 		emptyLabel.setMargin(new Vector2i(25, 0));
 		cellShapeLayout.add(emptyLabel);
 		
 		engineLabel = new TextWidget("Engine Step Size:", Size.MEDIUM);
 		engineLabel.setFlag(Widget.CENTER_VERTICAL);
 		cellShapeLayout.add(engineLabel);
 		
 		engineNumberBox = new NumberBox(35);
 		engineNumberBox.setWidth(50);
 		engineNumberBox.setValue(1);
 		engineNumberBox.setFlag(Widget.CENTER_VERTICAL);
 		cellShapeLayout.add(engineNumberBox);
 		
 		LinearLayout widgetPreviewLayout = new LinearLayout(LinearLayout.Direction.HORIZONTAL);
 		widgetPreviewLayout.setBackground(new Fill(new Colour(0.0f,0.0f,0.0f)));
 		widgetPreviewLayout.setFlag(Widget.FILL);
 		propertiesLayout.add(widgetPreviewLayout);
 		
 		widgetPreviewContainer = new Container(new Vector2i(200,100));
 		widgetPreviewContainer.setFlag(Widget.FILL);
 		widgetPreviewLayout.add(widgetPreviewContainer);
 		
 		widget3DPreviewContainer = new Container(new Vector2i (300,100));
 		widget3DPreviewContainer.setFlag(Widget.FILL);
 		widgetPreviewLayout.add(widget3DPreviewContainer);
 		
 		widget3DPreviewContainer.setContents(grid3DViewer);
 		widgetPreviewContainer.setContents(gridViewer);
 		
 		LinearLayout buttonHeaderLayout = new LinearLayout(new Vector2i(700, 50), LinearLayout.Direction.HORIZONTAL);
 		buttonHeaderLayout.setBackground(new Fill(new Colour(0.7f, 0.7f, 0.7f)));
 		buttonHeaderLayout.setBorder(new Fill(new Colour(0.7f, 0.7f, 0.7f)));
 		buttonHeaderLayout.setFlag(Widget.CENTER_HORIZONTAL | Widget.WRAP);
 		worldLayout.add(buttonHeaderLayout);
 			
 		backButton = new Button(new Vector2i(100, 50), "Main Menu");
 		backButton.setWidth(165);
 		backButton.setHeight(35);
 		buttonHeaderLayout.add(backButton);
 		
 		saveWorldButton = new Button(new Vector2i(100, 50), "Save World");
 		saveWorldButton.setWidth(165);
 		saveWorldButton.setHeight(35);
 		buttonHeaderLayout.add(saveWorldButton);
 		
 		simulateButton = new Button(new Vector2i(100, 50), "Simulate");
 		simulateButton.setWidth(165);
 		simulateButton.setHeight(35);
 		buttonHeaderLayout.add(simulateButton);
 		
 		rulesContainer = new Container(new Vector2i(100, 100));
 		tabbedWorldView.add(rulesContainer, "CAL Rules");
 		rulesContainer.setFlag(Widget.FILL);
 		
 		LinearLayout masterRulesLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		masterRulesLayout.setFlag(Widget.FILL);
 		rulesContainer.setContents(masterRulesLayout);
 		
 		LinearLayout CALLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		CALLayout.setFlag(Widget.FILL);
 		masterRulesLayout.add(CALLayout);
 		
 		ImageWidget calEditorHeader = new ImageWidget(window.getTheme().getImage("headers","cal_editor_header.png"));
 		calEditorHeader.setFlag(Widget.CENTER_HORIZONTAL);
 		CALLayout.add(calEditorHeader);
 		
 		LinearLayout rulesetLayout = new LinearLayout(LinearLayout.Direction.HORIZONTAL);
 		rulesetLayout.setFlag(Widget.WRAP);
 		rulesetLayout.setFlag(Widget.CENTER_HORIZONTAL);
 		CALLayout.add(rulesetLayout);
 		
 		rulesetsDropDownBox = new DropDownBox(new Vector2i(200, 20));
 		rulesetsDropDownBox.setFlag(Widget.CENTER_VERTICAL);
 		rulesetsDropDownBox.setSelected(0);
 		rulesetLayout.add(rulesetsDropDownBox);
 		
 		CALTextArea = new TextArea(100, 20);
 		CALTextArea.setMargin(new Vector2i(0,0));
 		CALTextArea.setFlag(Widget.FILL);
 		CALTextArea.setLineNumbers(true);
 		
 		ScrollableContainer textAreaContainer = new ScrollableContainer(new Vector2i(100,100));
 		textAreaContainer.setFlag(Widget.FILL);
 		
 		textAreaContainer.setBorder(new Fill(new Colour(0.7f, 0.7f, 0.7f)));
 		CALLayout.add(textAreaContainer);
 		
 		textAreaContainer.setContents(CALTextArea);
 		
 		ImageWidget compilerOutputHeader = new ImageWidget(window.getTheme().getImage("headers","compiler_output_header.png"));
 		compilerOutputHeader.setFlag(Widget.CENTER_HORIZONTAL);
 		CALLayout.add(compilerOutputHeader);
 		
 		outputContainer = new ScrollableContainer(new Vector2i(250, 100));
 		outputContainer.setFlag(Widget.FILL_HORIZONTAL);
 		outputContainer.setFlag(Widget.CENTER_HORIZONTAL);
 		outputContainer.setThemeClass("List");
 		CALLayout.add(outputContainer);
 		
 		outputLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		outputLayout.setMargin(new Vector2i(0, 0));
 		outputLayout.setFlag(Widget.WRAP);
 		
 		outputContainer.setContents(outputLayout);
 		//875
 		LinearLayout buttonRulesLayout = new LinearLayout(new Vector2i(1045, 50), LinearLayout.Direction.HORIZONTAL);
 		buttonRulesLayout.setBorder(new Fill(new Colour(0.7f, 0.7f, 0.7f)));
 		buttonRulesLayout.setFlag(Widget.CENTER_HORIZONTAL);
 		masterRulesLayout.add(buttonRulesLayout);
 		
 		clearRulesButton = new Button(new Vector2i(100, 50), "Clear Rules");
 		clearRulesButton.setWidth(165);
 		clearRulesButton.setHeight(35);
 		buttonRulesLayout.add(clearRulesButton);
 		
 		submitRulesButton = new Button(new Vector2i(100, 50), "Submit Rules");
 		submitRulesButton.setWidth(165);
 		submitRulesButton.setHeight(35);
 		buttonRulesLayout.add(submitRulesButton);
 		
 		compareRulesButton = new Button(new Vector2i(100, 50), "Compare Rules");
 		compareRulesButton.setWidth(165);
 		compareRulesButton.setHeight(35);
 		buttonRulesLayout.add(compareRulesButton);
 		
 		openCALFileButton = new Button(new Vector2i(100, 50), "Open File");
 		openCALFileButton.setWidth(165);
 		openCALFileButton.setHeight(35);
 		buttonRulesLayout.add(openCALFileButton);
 		
 		saveCALFileButton = new Button(new Vector2i(100, 50), "Save File");
 		saveCALFileButton.setWidth(165);
 		saveCALFileButton.setHeight(35);
 		buttonRulesLayout.add(saveCALFileButton);
 		
 		saveAsCALFileButton = new Button(new Vector2i(100, 50), "Save File As");
 		saveAsCALFileButton.setWidth(165);
 		saveAsCALFileButton.setHeight(35);
 		buttonRulesLayout.add(saveAsCALFileButton);
 		
 		createDistributionTab();
 		
 		coloursContainer = new Container(new Vector2i(100, 100));
 		coloursContainer.setFlag(Widget.FILL);
 		tabbedWorldView.add(coloursContainer, "Colour Ranges");
 		
 		masterColoursLayout = new LinearLayout(LinearLayout.Direction.HORIZONTAL);
 		masterColoursLayout.setFlag(Widget.FILL);
 		coloursContainer.setContents(masterColoursLayout);
 		
 		leftColourLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		leftColourLayout.setFlag(Widget.FILL);
 		masterColoursLayout.add(leftColourLayout);
 		
 		rightColourLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		rightColourLayout.setFlag(Widget.FILL);
 		//rightColourLayout.setWidth(350);
 		masterColoursLayout.add(rightColourLayout);
 		
 		LinearLayout propertyColourLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		propertyColourLayout.setFlag(Widget.FILL);
 		leftColourLayout.add(propertyColourLayout);
 		
 		ImageWidget propertyColourHeader = new ImageWidget(theme.getImage("headers", "propery_colour_ranges_header.png"));
 		propertyColourHeader.setMargin(new Vector2i(150,10));
 		propertyColourLayout.add(propertyColourHeader);
 		
 		colourPropertiesContainer = new ScrollableContainer(new Vector2i(560,333));
 		colourPropertiesContainer.setFlag(Widget.FILL);
 		propertyColourLayout.add(colourPropertiesContainer);
 		
 		colourPropertiesLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		//colourPropertiesLayout.setBackground(new Fill(new Colour(0.52f, 0.527f, 0.52f)));
 		
 		colourPropertiesLayout.setHeight(800);
 		colourPropertiesLayout.setWidth(700);
 		colourPropertiesLayout.setFlag(Widget.WRAP);
 		colourPropertiesContainer.setContents(colourPropertiesLayout);	
 		
 		TextWidget noColourSetText = new TextWidget("NO COLOUR RANGE PROPERTIES AVAILABLE. APPLY WORLD CHANGES FIRST.");
 		noColourSetText.setPosition(new Vector2i(colourPropertiesLayout.getWidth()/2, colourPropertiesLayout.getHeight()/2));
 		colourPropertiesLayout.add(noColourSetText);
 		
 		LinearLayout innerRightColourLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		innerRightColourLayout.setFlag(Widget.WRAP);
 		innerRightColourLayout.setBorder(new Fill(new Colour(0.7f, 0.7f, 0.7f)));
 		
 		colourButtonsLayout = new LinearLayout(LinearLayout.Direction.HORIZONTAL);
 		colourButtonsLayout.setFlag(Widget.WRAP);
 		colourButtonsLayout.setFlag(Widget.CENTER_HORIZONTAL);
 		rightColourLayout.add(colourButtonsLayout);
 		
 		setColourRangesButton = new Button(new Vector2i(120,40), "Set Colours");
 		colourButtonsLayout.add(setColourRangesButton);
 		
 		resetColourRangesButton = new Button(new Vector2i(140,40), "Reset Colours");
 		colourButtonsLayout.add(resetColourRangesButton);
 		
 		colourPicker = new ColourPicker(window);
 		colourPicker.setFlag(Widget.CENTER_HORIZONTAL);
 		rightColourLayout.add(colourPicker);
 		
 		cpRGBLayout = new LinearLayout(LinearLayout.Direction.HORIZONTAL);
 		cpRGBLayout.setFlag(Widget.CENTER_HORIZONTAL);
 		cpRGBLayout.setFlag(Widget.WRAP);
 		rightColourLayout.add(cpRGBLayout);
 		
 		cpRNumberBox = new NumberBox(40);
 		cpRNumberBox.setValue(0);
 		cpRGBLayout.add(cpRNumberBox);
 		
 		cpGNumberBox = new NumberBox(40);
 		cpGNumberBox.setValue(0);
 		cpRGBLayout.add(cpGNumberBox);
 		
 		cpBNumberBox = new NumberBox(40);
 		cpBNumberBox.setValue(0);
 		cpRGBLayout.add(cpBNumberBox);
 		
 		createWorldEditorTab();
 		
 		dialog = new Dialog(window, new Vector2i(400, 200));
 		
 		dialogLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		dialogLayout.setFlag(Widget.FILL);
 		dialog.setContents(dialogLayout);
 		
 		dialogTitle = new TextWidget("Invalid World Size", Text.Size.LARGE);
 		dialogTitle.setFlag(Widget.CENTER_HORIZONTAL);
 		dialogLayout.add(dialogTitle);
 		
 		dialogMessage = new TextWidget("World Sizes cannot be less than 5 cells.");
 		dialogMessage.setFlag(Widget.FILL_HORIZONTAL);
 		dialogMessage.setFlag(Widget.FILL_VERTICAL); // This pushes the OK button down because it fills the space in between
 		dialogMessage.setFlowed(true);
 		dialogMessage.setFlag(Widget.CENTER_HORIZONTAL);
 		dialogLayout.add(dialogMessage);
 		
 		dialogOKButton = new Button(new Vector2i(120, 30), "OK");
 		dialogOKButton.setFlag(Widget.CENTER_HORIZONTAL);
 		dialogLayout.add(dialogOKButton);
 		
 		// Dialog CAL
 		dialogCAL = new Dialog(window, new Vector2i(400, 200));
 		
 		dialogCALLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		dialogCALLayout.setFlag(Widget.FILL);
 		dialogCAL.setContents(dialogCALLayout);
 		
 		dialogCALTitle = new TextWidget("CAL Rules Error", Text.Size.LARGE);
 		dialogCALTitle.setFlag(Widget.CENTER_HORIZONTAL);
 		dialogCALLayout.add(dialogCALTitle);
 		
 		dialogCALMessage = new TextWidget("Invalid .cal File");
 		dialogCALMessage.setFlag(Widget.FILL_HORIZONTAL);
 		dialogCALMessage.setFlag(Widget.FILL_VERTICAL); // This pushes the OK button down because it fills the space in between
 		dialogCALMessage.setFlowed(true);
 		dialogCALMessage.setFlag(Widget.CENTER_HORIZONTAL);
 		dialogCALLayout.add(dialogCALMessage);
 		
 		dialogCALOKButton = new Button(new Vector2i(120, 30), "OK");
 		dialogCALOKButton.setFlag(Widget.CENTER_HORIZONTAL);
 		dialogCALLayout.add(dialogCALOKButton);
 		
 		////Simulation
 		masterSimulationLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		masterSimulationLayout.setFlag(Widget.FILL);
 		masterSimulationLayout.setMargin(new Vector2i(0, 0));
 		masterView.add(masterSimulationLayout);
 		masterSimulationLayout.add(worldHeaderLayout);
 		
 		topLayout = new LinearLayout(LinearLayout.Direction.HORIZONTAL);
 		topLayout.setMargin(new Vector2i(0, 0));
 		topLayout.setFlag(Widget.FILL);
 		masterSimulationLayout.add(topLayout);
 		
 		controlLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		controlLayout.setFlag(Widget.FILL_VERTICAL | Widget.WRAP_HORIZONTAL);
 		topLayout.add(controlLayout);
 		
 		addSliceButton = new Button(new Vector2i(100, 50), "Add slice");
 		
 		viewportsLayout = new LinearLayout(LinearLayout.Direction.HORIZONTAL);
 		viewportsLayout.setFlag(Widget.FILL);
 		topLayout.add(viewportsLayout);
 		
 		Container simulationWindowContainer = new Container(new Vector2i(500,300));
 		simulationWindowContainer.setFlag(Widget.FILL);
 		simulationWindowContainer.setBackground(new Fill(new Colour(0f,0f,0f)));
 		
 		Viewport v = new Viewport(simulationWindowContainer, Viewport.Type.THREE_D, colourRules);	
 		viewportsLayout.add(v.container);
 		
 		viewports = new ArrayList<Viewport>();
 		viewports.add(v);
 		
 		//SLIDER
 		LinearLayout sliderLayout = new LinearLayout(new Vector2i(40, 55), LinearLayout.Direction.HORIZONTAL);
 		sliderLayout.setBorder(new Fill(new Colour(0.7F, 0.7F, 0.7F)));
 		sliderLayout.setFlag(Widget.FILL_HORIZONTAL | Widget.WRAP_VERTICAL);
 		masterSimulationLayout.add(sliderLayout);
 		
 		generationSlider = new DiscreteSliderWidget(100);
 		generationSlider.setMargin(new Vector2i(5, 5));
 		generationSlider.setFlag(Widget.FILL_HORIZONTAL);
 		generationSlider.setShowValue(true);
 		sliderLayout.add(generationSlider);
 		
 		simulationControlsLayout = new LinearLayout(new Vector2i(100, 150), LinearLayout.Direction.HORIZONTAL);
 		simulationControlsLayout.setFlag(Widget.WRAP);
 		simulationControlsLayout.setFlag(Widget.CENTER_HORIZONTAL);
 		
 		masterSimulationLayout.add(simulationControlsLayout);
 		
 		LinearLayout detailsLayout = new LinearLayout(new Vector2i(250, 90), LinearLayout.Direction.VERTICAL);
 		detailsLayout.setBorder(new Fill(new Colour(0.7F, 0.7F, 0.7F)));
 		simulationControlsLayout.add(detailsLayout);
 		
 		LinearLayout innerDetailsLayout = new LinearLayout(new Vector2i(160, 25), LinearLayout.Direction.HORIZONTAL);
 		innerDetailsLayout.setFlag(Widget.CENTER_HORIZONTAL | Widget.WRAP_VERTICAL);
 		detailsLayout.add(innerDetailsLayout);
 		
 		LinearLayout innerDetailsLayout2 = new LinearLayout(new Vector2i(205, 40), LinearLayout.Direction.VERTICAL);
 		innerDetailsLayout2.setFlag(Widget.CENTER_HORIZONTAL | Widget.WRAP_VERTICAL);
 		detailsLayout.add(innerDetailsLayout2);
 		
 		iterationsText = new TextWidget("Generations: 0");
 		innerDetailsLayout2.add(iterationsText);
 		
 		ImageWidget detailsImage = new ImageWidget(this.window.getTheme().getImage("headers", "details_header.png"));
 		detailsImage.setFlag(Widget.CENTER_HORIZONTAL);
 		innerDetailsLayout.add(detailsImage);
 		
 		LinearLayout playbackLayout = new LinearLayout(new Vector2i(250, 90), LinearLayout.Direction.VERTICAL);
 		playbackLayout.setFlag(Widget.WRAP_HORIZONTAL);
 		playbackLayout.setBorder(new Fill(new Colour(0.7F, 0.7F, 0.7F)));
 		simulationControlsLayout.add(playbackLayout);
 		
 		LinearLayout innerPlaybackLayout = new LinearLayout(new Vector2i(205, 25), LinearLayout.Direction.HORIZONTAL);
 		innerPlaybackLayout.setFlag(Widget.WRAP_HORIZONTAL | Widget.CENTER_HORIZONTAL);
 		playbackLayout.add(innerPlaybackLayout);
 		
 		LinearLayout innerPlaybackLayout2 = new LinearLayout(new Vector2i(205, 40), LinearLayout.Direction.HORIZONTAL);
 		innerPlaybackLayout2.setFlag(Widget.WRAP_HORIZONTAL);
 		playbackLayout.add(innerPlaybackLayout2);
 		
 		Button stepBackwardButton = new Button(this.window.getTheme().getImage("icons", "step_backward_icon.png"));
 		stepBackwardButton.setMargin(new Vector2i(5, 0));
 		innerPlaybackLayout2.add(stepBackwardButton);
 		
 		playButton = new Button(this.window.getTheme().getImage("icons", "play_icon.png"));
 		playButton.setMargin(new Vector2i(5, 0));
 		innerPlaybackLayout2.add(playButton);
 		
 		pauseButton = new Button(this.window.getTheme().getImage("icons", "pause_icon.png"));
 		pauseButton.setMargin(new Vector2i(5, 0));
 		innerPlaybackLayout2.add(pauseButton);
 		
 		resetButton = new Button(this.window.getTheme().getImage("icons", "reset_icon.png"));
 		resetButton.setMargin(new Vector2i(5, 0));
 		innerPlaybackLayout2.add(resetButton);
 		
 		stepForwardButton = new Button(window.getTheme().getImage("icons", "step_forward_icon.png"));
 		stepForwardButton.setMargin(new Vector2i(5, 0));
 		innerPlaybackLayout2.add(stepForwardButton);
 		
 		LinearLayout innerPlaybackLayout3 = new LinearLayout(new Vector2i(205, 40), LinearLayout.Direction.VERTICAL);
 		innerPlaybackLayout3.setMargin(new Vector2i(2, 0));
 		innerPlaybackLayout3.setFlag(Widget.WRAP);
 		innerPlaybackLayout2.add(innerPlaybackLayout3);
 		
 		TextWidget playbackSpeedHeader = new TextWidget("Playback Speed:");
 		playbackSpeedHeader.setMargin(new Vector2i(0, 0));
 		innerPlaybackLayout3.add(playbackSpeedHeader);
 		
 		playbackSpeedSlider = new SliderWidget(100);
 		playbackSpeedSlider.setShowValuePlaces(2);
 		playbackSpeedSlider.setMaximum(1);
 		playbackSpeedSlider.setFlag(Widget.FILL_HORIZONTAL);
 		playbackSpeedSlider.setShowValue(true);
 		innerPlaybackLayout3.add(playbackSpeedSlider);
 		
 		ImageWidget playbackImage = new ImageWidget(window.getTheme().getImage("headers", "playback_header.png"));
 		playbackImage.setFlag(Widget.CENTER_HORIZONTAL);
 		innerPlaybackLayout.add(playbackImage);
 		
 		LinearLayout cameraLayout = new LinearLayout(new Vector2i(470, 90), LinearLayout.Direction.VERTICAL);
 		cameraLayout.setBorder(new Fill(new Colour(0.7F, 0.7F, 0.7F)));
 		simulationControlsLayout.add(cameraLayout);
 		LinearLayout innerCameraLayout = new LinearLayout(new Vector2i(190, 25), LinearLayout.Direction.HORIZONTAL);
 		innerCameraLayout.setFlag(Widget.CENTER_HORIZONTAL);
 		cameraLayout.add(innerCameraLayout);
 		
 		LinearLayout innerCameraLayout2 = new LinearLayout(new Vector2i(445, 40), LinearLayout.Direction.HORIZONTAL);
 		innerCameraLayout2.setFlag(Widget.CENTER_HORIZONTAL);
 		cameraLayout.add(innerCameraLayout2);
 		
 		zoomInButton = new Button(window.getTheme().getImage("icons", "zoom_in_icon.png"));
 		zoomInButton.setMargin(new Vector2i(5, 0));
 		innerCameraLayout2.add(zoomInButton);
 		
 		zoomOutButton = new Button(window.getTheme().getImage("icons", "zoom_out_icon.png"));
 		zoomOutButton.setMargin(new Vector2i(5, 0));
 		innerCameraLayout2.add(zoomOutButton);
 		
 		moveUpButton = new Button(window.getTheme().getImage("icons", "up_icon.png"));
 		moveUpButton.setMargin(new Vector2i(5, 0));
 		innerCameraLayout2.add(moveUpButton);
 		
 		moveDownButton = new Button(window.getTheme().getImage("icons", "down_icon.png"));
 		moveDownButton.setMargin(new Vector2i(5, 0));
 		innerCameraLayout2.add(moveDownButton);
 		
 		moveLeftButton = new Button(window.getTheme().getImage("icons", "left_icon.png"));
 		moveLeftButton.setMargin(new Vector2i(5, 0));
 		innerCameraLayout2.add(moveLeftButton);
 		
 		moveRightButton = new Button(window.getTheme().getImage("icons", "right_icon.png"));
 		moveRightButton.setMargin(new Vector2i(5, 0));
 		innerCameraLayout2.add(moveRightButton);
 		
 		yawLeftButton = new Button(window.getTheme().getImage("icons", "yaw_left_icon.png"));
 		yawLeftButton.setMargin(new Vector2i(5, 0));
 		innerCameraLayout2.add(yawLeftButton);
 		
 		yawRightButton = new Button(window.getTheme().getImage("icons", "yaw_right_icon.png"));
 		yawRightButton.setMargin(new Vector2i(5, 0));
 		innerCameraLayout2.add(yawRightButton);
 		
 		pitchUpButton = new Button(window.getTheme().getImage("icons", "pitch_up_icon.png"));
 		pitchUpButton.setMargin(new Vector2i(5, 0));
 		innerCameraLayout2.add(pitchUpButton);
 		
 		pitchDownButton = new Button(window.getTheme().getImage("icons", "pitch_down_icon.png"));
 		pitchDownButton.setMargin(new Vector2i(5, 0));
 		innerCameraLayout2.add(pitchDownButton);
 		
 		resetCameraButton = new Button(window.getTheme().getImage("icons", "reset_camera_icon.png"));
 		resetCameraButton.setMargin(new Vector2i(5, 0));
 		innerCameraLayout2.add(resetCameraButton);
 		
 		ImageWidget cameraImage = new ImageWidget(window.getTheme().getImage("headers", "camera_header.png"));
 		cameraImage.setFlag(Widget.CENTER_HORIZONTAL);
 		innerCameraLayout.add(cameraImage);
 		
 		LinearLayout viewSettingsLayout = new LinearLayout(new Vector2i(250, 90), LinearLayout.Direction.VERTICAL);
 		viewSettingsLayout.setBorder(new Fill(new Colour(0.7F, 0.7F, 0.7F)));
 		simulationControlsLayout.add(viewSettingsLayout);
 		
 		LinearLayout innerViewSettingsLayout = new LinearLayout(new Vector2i(145, 25), LinearLayout.Direction.HORIZONTAL);
 		innerViewSettingsLayout.setFlag(Widget.CENTER_HORIZONTAL);
 		viewSettingsLayout.add(innerViewSettingsLayout);
 		
 		LinearLayout innerViewSettingsLayout2 = new LinearLayout(new Vector2i(220, 40), LinearLayout.Direction.HORIZONTAL);
 		innerViewSettingsLayout2.setFlag(Widget.CENTER_HORIZONTAL);
 		viewSettingsLayout.add(innerViewSettingsLayout2);
 		
 		toggleHideButton = new Button(window.getTheme().getImage("icons", "toggle_hide_icon.png"));
 		toggleHideButton.setMargin(new Vector2i(5, 0));
 		innerViewSettingsLayout2.add(toggleHideButton);
 		
 		toggle3dButton = new Button(this.window.getTheme().getImage("icons", "3d_icon.png"));
 		toggle3dButton.setMargin(new Vector2i(5, 0));
 		innerViewSettingsLayout2.add(toggle3dButton);
 		
 		toggleWireframeButton = new Button(this.window.getTheme().getImage("icons", "toggle_wireframe_icon.png"));
 		toggleWireframeButton.setMargin(new Vector2i(5, 0));
 		innerViewSettingsLayout2.add(toggleWireframeButton);
 		
 		addViewportButton = new Button(this.window.getTheme().getImage("icons", "add_viewport_icon.png"));
 		addViewportButton.setMargin(new Vector2i(5, 0));
 		innerViewSettingsLayout2.add(addViewportButton);
 		
 		removeViewportButton = new Button(this.window.getTheme().getImage("icons", "remove_viewport_icon.png"));
 		removeViewportButton.setMargin(new Vector2i(5, 0));
 		innerViewSettingsLayout2.add(removeViewportButton);
 		
 		viewSettingsHeader = new ImageWidget(this.window.getTheme().getImage("headers", "view_settings_header.png"));
 		cameraImage.setFlag(Widget.CENTER_HORIZONTAL);
 		innerViewSettingsLayout.add(viewSettingsHeader);
 		
 		toggleShowButton = new Button(new Vector2i(10, 15), "");
 		toggleShowButton.setFlag(Widget.FILL_HORIZONTAL);
 		toggleShowButton.setVisible(false);
 		masterSimulationLayout.add(toggleShowButton);
 		
 		LinearLayout backToMainMenuLayout = new LinearLayout(LinearLayout.Direction.HORIZONTAL);
 		backToMainMenuLayout.setMargin(new Vector2i(0,0));
 		backToMainMenuLayout.setFlag(Widget.WRAP | Widget.CENTER_HORIZONTAL);
 		worldHeaderLayout.add(backToMainMenuLayout);
 		
 		backToMainMenuButton = new Button(new Vector2i(60, 30), "Back");
 		backToMainMenuButton.setMargin(new Vector2i(0,0));
 		backToMainMenuLayout.add(backToMainMenuButton);
 		
 		Container headerLogoContainer = new Container(new Vector2i(10, 10));
 		headerLogoContainer.setMargin(new Vector2i(0,0));
 		headerLogoContainer.setFlag(Widget.FILL);
 		worldHeaderLayout.add(headerLogoContainer);
 		
 		ImageWidget headerLogo = new ImageWidget(theme.getImage("headers", "logo.png"));
 		headerLogo.setFlag(Widget.CENTER);
 		headerLogoContainer.setContents(headerLogo);
 		
 		//Creating messages to display
 		changePathDialog = new Dialog(window, new Vector2i(460, 80));
 		
 		changePathLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		changePathLayout.setFlag(Widget.FILL);
 		changePathDialog.setContents(changePathLayout);
 		
 		changePathTitle = new TextWidget("Changing Save Path", Text.Size.LARGE);
 		changePathTitle.setFlag(Widget.CENTER_HORIZONTAL);
 		changePathLayout.add(changePathTitle);
 		
 		changePathMessage = new TextWidget("The program is busy changing the world path. Please be patient.");
 		changePathMessage.setFlag(Widget.CENTER_HORIZONTAL);
 		changePathLayout.add(changePathMessage);
 		//
 		loadingDialog = new Dialog(window, new Vector2i(400, 80));
 		
 		loadingLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		loadingLayout.setFlag(Widget.FILL);
 		loadingDialog.setContents(loadingLayout);
 		
 		loadingTitle = new TextWidget("Loading", Text.Size.LARGE);
 		loadingTitle.setFlag(Widget.CENTER_HORIZONTAL);
 		loadingLayout.add(loadingTitle);
 		
 		loadingMessage = new TextWidget("The program is busy loading a world. Please be patient.");
 		loadingMessage.setFlag(Widget.CENTER_HORIZONTAL);
 		loadingLayout.add(loadingMessage);
 		//
 		savingDialog = new Dialog(window, new Vector2i(400, 80));
 		
 		savingLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		savingLayout.setFlag(Widget.FILL);
 		savingDialog.setContents(savingLayout);
 		
 		savingTitle = new TextWidget("Saving", Text.Size.LARGE);
 		savingTitle.setFlag(Widget.CENTER_HORIZONTAL);
 		savingLayout.add(savingTitle);
 		
 		savingMessage = new TextWidget("The program is busy saving the world. Please be patient.");
 		savingMessage.setFlag(Widget.CENTER_HORIZONTAL);
 		savingLayout.add(savingMessage);
 		//
 		shutdownDialog = new Dialog(window, new Vector2i(400, 80));
 		
 		shutdownLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		shutdownLayout.setFlag(Widget.FILL);
 		shutdownDialog.setContents(shutdownLayout);
 		
 		shutdownTitle = new TextWidget("Program Shut Down", Text.Size.LARGE);
 		shutdownTitle.setFlag(Widget.CENTER_HORIZONTAL);
 		shutdownLayout.add(shutdownTitle);
 		
 		shutdownMessage = new TextWidget("The program is busy shutting down. Please be patient.");
 		shutdownMessage.setFlag(Widget.CENTER_HORIZONTAL);
 		shutdownLayout.add(shutdownMessage);
 		//
 		streamingDialog = new Dialog(window, new Vector2i(400, 80));
 		
 		streamingLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 		streamingLayout.setFlag(Widget.FILL);
 		streamingDialog.setContents(streamingLayout);
 		
 		streamingTitle = new TextWidget("Streaming", Text.Size.LARGE);
 		streamingTitle.setFlag(Widget.CENTER_HORIZONTAL);
 		streamingLayout.add(streamingTitle);
 		
 		streamingMessage = new TextWidget("The program is busy streaming to disk. Please be patient.");
 		streamingMessage.setFlag(Widget.CENTER_HORIZONTAL);
 		streamingLayout.add(streamingMessage);
 		
 		window.relayout();
 	}
 	
 	public void loadPropertiesFromWorld()
 	{
 		Grid grid = world.getInitialGeneration();
 		
 		worldSizeXNumberBox.setValue(grid.getWidth());
 		worldSizeYNumberBox.setValue(grid.getHeight());
 		
 		switch(grid.getType())
 		{
 			case RECTANGLE:
 				cellShapeDropDownBox.setSelected(0);
 				break;
 			case TRIANGLE:
 				cellShapeDropDownBox.setSelected(1);
 				break;
 			case HEXAGON:
 				cellShapeDropDownBox.setSelected(2);
 				break;
 			case VONNEUMANN:
 				cellShapeDropDownBox.setSelected(3);
 				break;
 		}
 		
 		wrapCheckBox.setChecked(grid.isWrappable());
 		historyDropDownBox.setSelected(world.getHistoryType());
 		
 		//String ruleCode = world.getRuleCode();
 		String ruleCode = world.getRuleCode(rulesetsDropDownBox.getSelected());
 		if(ruleCode == null)
 			ruleCode = "";
 		CALTextArea.setText(ruleCode);
 
 		setRulesetsDropDownBox();
 		engineNumberBox.setValue(world.getStepAmount());
 		
 		saveRuleCodeToWorld();
 		
 		Log.debug(TAG, world.getColourCode());
 		
 		ColourRuleSetParser parser = new ColourRuleSetParser();
 		colourRules = parser.parseString(world.getColourCode(), CodeGen.getPropertyList());
 		
 		if(colourRules == null)
 		{
 			Log.warning(TAG, "Invalid colour code");
 			
 			colourRules = new ColourRuleSet(grid.getNumProperties());
 			
 			for(int i = 0; i < grid.getNumProperties(); i++)
 				colourRules.setColourRule(i, new ColourRule());
 		}
 		
 		createColoursTab();
 		updatePreview();
 	}
 	
 	public void reconstructViewportLayout()
 	{
 		viewportsLayout.clear();
 		
 		for(Viewport v : viewports)
 		{
 			viewportsLayout.add(v.container);
 		}
 	}
 	
 	public void refreshClients()
 	{
 		availableClients.clear();
 		usingClients.clear();
 		
 		ServerEvent serverEvent = new ServerEvent(ServerEvent.Type.PING_CLIENTS);
 		server.sendEvent(serverEvent);
 	}
 	
 	@Override
 	public void render()
 	{
 	}
 	
 	public void saveColourCodeToWorld()
 	{
 		ColourRuleSetWriter writer = new ColourRuleSetWriter();
 		world.setColourCode(writer.write(colourRules, "main", CodeGen.getPropertyList()));
 	}
 	
 	public void savePropertiesToWorld()
 	{
 		Grid grid = world.getInitialGeneration();
 		
 		Vector2i size = new Vector2i(worldSizeXNumberBox.getValue(5), worldSizeYNumberBox.getValue(5));
 		
 		GridType type;
 		if(cellShapeDropDownBox.getSelectedText() == "Triangle")
 			type = GridType.TRIANGLE;
 		else if(cellShapeDropDownBox.getSelectedText() == "Hexagon")
 			type = GridType.HEXAGON;
 		else if(cellShapeDropDownBox.getSelectedText() == "Rectangle")
 			type = GridType.RECTANGLE;
 		else
 			type = GridType.VONNEUMANN;
 		
 		if(!grid.getSize().equals(size) || grid.getType() != type)
 		{
 			Log.information(TAG, "Recreating grid, the current state will be lost");
 			grid = type.create(size, grid.getNumProperties());
 		}
 		
 		grid.setWrappable(wrapCheckBox.isChecked());
 		
 		if(world.getHistoryType() == 2 && historyDropDownBox.getSelected() != 2)
 		{
 			displayStreamingMessage();
 			
 			world.stop();
 			
 			destroyStreamingMessage();
 		}
 		world.setKeepHistory(historyDropDownBox.getSelected());
 
 		world.resetTo(grid);
 	}
 	
 	public void saveRuleCodeToWorld()
 	{
 		String code = CALTextArea.getText();
 		
 		CALCompiler compiler = new CALCompiler();
 		RuleLoader ruleLoader = new RuleLoader();
 		compiler.compile(code);
 
 		Rule rule = ruleLoader.loadRule(compiler.getCode());
 		
 		Log.information(TAG, "Loading rule code into World");
 		//world.setRuleCode(code);
 		world.updateRuleCode(code, rulesetsDropDownBox.getSelected());
 		
 		Grid grid = world.getInitialGeneration();
 		if(grid.getNumProperties() != rule.getNumProperties())
 		{
 			Log.information(TAG, "Recreating grid, the current state will be lost");
 			grid = grid.getType().create(grid.getSize(), rule.getNumProperties());
 			world.setWorldGenerations(new Grid[] {grid});
 			
 			world.reset();
 		}
 		
 		worldEditorPropertySelector.clear();
 		
 		for(String propertyName : CodeGen.getPropertyList())
 			worldEditorPropertySelector.addItem(propertyName);
 		
 		worldEditorPropertySelector.setSelected(1);
 		
 		heightMapPropertySelector.clear();
 		
 		for(String propertyName : CodeGen.getPropertyList())
 			heightMapPropertySelector.addItem(propertyName);
 		
 		heightMapPropertySelector.setSelected(1);
 	}
 	
 	public void setRulesetsDropDownBox()
 	{
 		ArrayList<String> rulesets = world.getRuleCodes();
 
 		rulesetsDropDownBox.clear();
 		
 		for(int i = 0; i < rulesets.size(); i++)
 		{
 			String code = rulesets.get(i);
 			String name = code.substring(0, code.indexOf("\n"));
 			
 			rulesetsDropDownBox.addItem((i + 1) + ":" + name);
 		}
 	}
 	
 	public void showDialog(String caption, String message)
 	{
 		dialogTitle.setCaption(caption);
 		dialogMessage.setCaption(message);
 		window.showModalDialog(dialog);
 	}
 	
 	public void shutdownProcess()
 	{
 		Log.information(TAG,"SHUTDOWN PROCESS INITIATED.");
 		
 		stopWorld();
 		
 		displayShutdownMessage();
 		window.shutdown();
 	}
 	
 	public void startWorldEditor(World world)
 	{
 		this.world = world;
 		loadPropertiesFromWorld();
 		updateWorldEditorTab();
 		
 		masterView.setIndex(1);
 		window.relayout();
 	}
 	
 	public void stopWorld()
 	{
 		if(world != null && world.getHistoryType() == 2)
 		{
 			displayStreamingMessage();
 			
 			world.stop();
 			while(world.hasStarted()) {}
 			
 			destroyStreamingMessage();
 		}
 	}
 	
 	@Override
 	public void update(float delta)
 	{
 		updateSimulationScreen(false);
 		
 		if(!themeName.equals(currentThemeName))
 		{
 			Log.information(TAG, "Changing theme to " + themeName);
 			
 			theme.loadTheme(themeName);
 			currentThemeName = themeName;
 			
 			window.relayout();
 		}	
 	}
 	
 	public void updateAvailableClientsList()
 	{
 		clientsAvailableList.clear();
 		clientsUsingList.clear();
 		
 		for(ClientEntry client : availableClients)
 			clientsAvailableList.addItem(computerIcon, client.address.getHostName());
 			
 		for(ClientEntry client : usingClients)
 			clientsUsingList.addItem(computerLinkIcon, client.address.getHostName());
 	}
 	
 	public void updatePreview()
 	{
 		Grid grid = world.getInitialGeneration();
 		if(grid == null)
 		{
 			Log.error(TAG, "World doesn't have an initial grid");
 			return;
 		}
 		
 		Log.debug(TAG, grid.toString());
 		
 		switch (grid.getType())
 		{
 			case RECTANGLE:
 				grid3DViewer = new RectangleGrid3DWidget(new Vector2i(400, 300), (RectangleGrid)grid, 24);
 				grid3DViewer.setFlag(Widget.FILL);
 				widget3DPreviewContainer.setContents(grid3DViewer);
 				
 				gridViewer = new RectangleGridWidget((RectangleGrid)grid, 16);
 				widgetPreviewContainer.setContents(gridViewer);
 				break;
 				
 			case TRIANGLE:
 				grid3DViewer = new TriangleGrid3DWidget(new Vector2i(400, 300), (TriangleGrid)grid, 24);
 				grid3DViewer.setFlag(Widget.FILL);
 				widget3DPreviewContainer.setContents(grid3DViewer);
 				
 				gridViewer = new TriangleGridWidget((TriangleGrid)grid, 32);
 				widgetPreviewContainer.setContents(gridViewer);	  
 				break;
 				
 			case HEXAGON:
 				grid3DViewer = new HexagonGrid3DWidget(new Vector2i(400, 300), (HexagonGrid)grid, 24);
 				grid3DViewer.setFlag(Widget.FILL);
 				widget3DPreviewContainer.setContents(grid3DViewer);
 				
 				gridViewer = new HexagonGridWidget((HexagonGrid)grid, 16);
 				widgetPreviewContainer.setContents(gridViewer);
 				break;
 				
 			case VONNEUMANN:
 				grid3DViewer = new VonNeumannGrid3DWidget(new Vector2i(400, 300), (VonNeumannGrid)grid, 24);
 				grid3DViewer.setFlag(Widget.FILL);
 				widget3DPreviewContainer.setContents(grid3DViewer);
 				
 				gridViewer = new VonNeumannGridWidget((VonNeumannGrid)grid, 16);
 				widgetPreviewContainer.setContents(gridViewer);
 				break;
 		}
 		
 		grid3DViewer.addSlice(1, 1, 16.0f);
 		gridViewer.addSlice(1, 1, 16.0f);
 		grid3DViewer.setColourRuleSet(colourRules);
 		gridViewer.setColourRuleSet(colourRules);
 	}
 	
 	public void updateSimulationScreen(boolean force)
 	{
 		if(world != null)
 		{
 			// Update slider
 			int	generations = world.getNumGenerations() - 1;
 			int origMaximum = generationSlider.getMaximum();
 			
 			generationSlider.setMaximum(world.getNumGenerations() - 1);
 			
 			if(generationSlider.getValue() >= origMaximum)
 				generationSlider.setValue(world.getNumGenerations() - 1);
 			
 			// Update tex
 			iterationsText.setCaption("Generations: " + generations);
 			
 			// Update viewports
 			int		generation = (historyDropDownBox.getSelected() != 0 ? generationSlider.getValue() : 0);
 			Grid 	grid = (historyDropDownBox.getSelected() != 0 ? world.getGeneration(generation) : world.getInitialGeneration());
 			
 			if(historyDropDownBox.getSelected() == 0)
 				force = true;
 			
 			if(grid != null && (currentGeneration != generation || force))
 			{
 				currentGeneration = generation;
 				currentGrid = grid;
 				
 				if(viewports.isEmpty())
 					System.out.println("VIEWPORTS IS EMPTY");
 				
 				if((currentGrid.getType() == grid.getType() && !force) || (!recreate))
 				{
 					for(Viewport viewport : viewports)
 					{
 						viewport.gridWidget.setGrid(currentGrid);
 					}
 				}
 				else
 				{
 					for(Viewport viewport : viewports)
 						viewport.recreate(currentGrid, window, colourRules);
 					
 					recreate = false;
 				}
 			}
 		}
 	}
 	
 	public void updateWorldEditorTab()
 	{
 		if(!loadingMessageDestroyed)
 			destroyLoadingMessage();
 		
 		currentGrid = world.getLastGeneration();
 		previewViewport.recreate(currentGrid, window, colourRules);
 		
 		propertyValues.clear();
 		
 		numberboxList = new ArrayList<NumberBox>();
 		
 		for(int i = 0 ; i < currentGrid.getNumProperties(); i++)
 		{
 			LinearLayout cellPropertyLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 			cellPropertyLayout.setMargin(new Vector2i(0, 6));
 			cellPropertyLayout.setBorder(new Fill(new Colour(0.7F, 0.7F, 0.7F)));
 			cellPropertyLayout.setFlag(Widget.WRAP);
 			propertyValues.add(cellPropertyLayout);
 			
 			TextWidget t = new TextWidget(CodeGen.getPropertyList().get(i));
 			cellPropertyLayout.add(t);
 			
 			NumberBox n = new NumberBox(60);
 			n.setFlag(Widget.FILL_HORIZONTAL);
 			n.setValue(0);
 			cellPropertyLayout.add(n);
 			
 			numberboxList.add(n);
 		}
 		
 		updateWorldEditorTool();
 	}
 	
 	public void updateWorldEditorTool()
 	{
 		if(previewViewport == null || previewViewport.type != Viewport.Type.TWO_D)
 			return;
 		
 		switch(editorToolType)
 		{
 			case LOOK:
 				Log.debug(TAG, "LOOK");
 				((Grid2DWidget)previewViewport.gridWidget).setDrawSelected(true);
 				editorApplyButton.setVisible(true);
 				break;
 			case BRUSH:
 				Log.debug(TAG, "BRUSH");
 				((Grid2DWidget)previewViewport.gridWidget).setDrawSelected(false);
 				editorApplyButton.setVisible(false);
 				break;
 			case FILL:
 				Log.debug(TAG, "FILL");
 				((Grid2DWidget)previewViewport.gridWidget).setDrawSelected(false);
 				editorApplyButton.setVisible(false);
 				break;
 		}
 	}
 	
 	public void updateWorldSettings()
 	{
 		currentGrid = world.getLastGeneration();
 		
 		loadPropertiesFromWorld();
 		updateWorldEditorTab();
 		updateWorldEditorTool();
 		updatePreview();
 	}
 	
 	/////////////////////////////////////////////
 	/// Inner classes
 	static class ClientEntry implements Comparable<ClientEntry>
 	{
 		InetSocketAddress address;
 		
 		ClientEntry(InetSocketAddress address)
 		{
 			this.address = address;
 		}
 		
 		@Override
 		public int compareTo(ClientEntry other)
 		{
 			return address.getHostName().compareTo(other.address.getHostName());
 		}
 	}
 	
 	public static class ColourContainer
 	{
 		public ArrayList<ColourRule>		colourRuleList;
 		public ArrayList<RangeContainer>	rangeContainerList;
 		
 		public Button						addRangeButton;
 		public Button						removeRangeButton;
 		
 		public int							id = 0;
 		public int							numRanges = 0;
 		
 		public LinearLayout					layout;
 		public LinearLayout					rangeButtonsLayout;
 		public LinearLayout					rangesLayout;
 		
 		public TextWidget					t1; 
 		
 		public ColourContainer(int id, Window window, String name, ColourRule c)
 		{
 			this.id = id;
 			
 			addRangeButton = new Button(new Vector2i(30,25), "+");
 			addRangeButton.setTooltip("Add a new colour range");
 			removeRangeButton = new Button(new Vector2i(30,25), "-");
 			removeRangeButton.setTooltip("Removes a colour range");
 			rangeButtonsLayout =  new LinearLayout(LinearLayout.Direction.HORIZONTAL);
 			rangeButtonsLayout.setWindow(window);
 			rangeButtonsLayout.setFlag(Widget.WRAP);
 			
 			layout = new LinearLayout(LinearLayout.Direction.VERTICAL);
 			layout.setWindow(window);
 			layout.setFlag(Widget.WRAP);
 			layout.setBorder(new Fill(new Colour(0.7f, 0.7f, 0.7f)));
 			layout.setHeight(50);
 			t1 = new TextWidget("Property: " + name, Text.Size.MEDIUM);
 			layout.add(t1);
 			
 			rangeButtonsLayout.add(addRangeButton);
 			rangeButtonsLayout.add(removeRangeButton);
 			
 			layout.add(rangeButtonsLayout);
 			
 			rangeContainerList = new ArrayList<GUI.RangeContainer>();
 			
 			if(c != null)
 			{
 				for(Range r : c.ranges)
 				{
 					if(r != null)
 					{
 						if(r.getType() == Range.Type.GRADIENT)
 							addRangeGradient(id, r);
 						else
 							addRangeSolid(id,r);
 					}
 				}
 			}
 		}
 		
 		public void addRange(int id)
 		{
 			numRanges++;
 			
 			RangeContainer rc = new RangeContainer(id, this.layout.getWindow());
 			rangeContainerList.add(rc);
 			
 			rc.colourBoxFrom.setColour(new Colour(255.0f,0.0f,0.0f));
 			rc.colourBoxTo.setColour(new Colour(255.0f,0.0f,0.0f));
 			
 			rc.firstRange.setValue(0);
 			rc.secondRange.setValue(0);
 			rc.fromColour = new Colour(0.0f,0.0f,0.0f);
 			rc.toColour = new Colour(0.0f,0.0f,0.0f);
 			this.layout.add(rc.getLayout());
 		}
 		
 		public void addRangeGradient(int id, Range r)
 		{
 			numRanges++;
 			
 			RangeContainer rc = new RangeContainer(id, this.layout.getWindow());
 			rangeContainerList.add(rc);
 			
 			rc.colourBoxFrom.setColour(r.getColour(0));
 			rc.colourBoxTo.setColour(r.getColour(1));
 			
 			rc.firstRange.setValue((int) r.from);
 			rc.secondRange.setValue((int) r.to);
 			rc.fromColour = r.getColour(0);
 			rc.toColour = r.getColour(1);
 			
 			this.layout.add(rc.getLayout());
 		}
 		
 		public void addRangeSolid(int id, Range r)
 		{
 			numRanges++;
 			
 			RangeContainer rc = new RangeContainer(id, this.layout.getWindow());
 			rangeContainerList.add(rc);
 			
 			rc.colourBoxFrom.setColour(r.getColour(0));
 			rc.colourBoxTo.setColour(r.getColour(0));
 			
 			rc.firstRange.setValue((int) r.from);
 			rc.secondRange.setValue((int) r.to);
 			rc.fromColour = r.getColour(0);
 			rc.toColour = r.getColour(0);
 			this.layout.add(rc.getLayout());
 		
 		}
 		
 		public Button getAddRangeButton()
 		{
 			return addRangeButton;
 		}
 		
 		public LinearLayout getLayout()
 		{
 			return layout;
 		}
 		
 		public void removeRange()
 		{
 			numRanges--;
 			rangeContainerList.remove(numRanges);
 		}
 	}
 	
 	public static class RangeContainer
 	{
 		int id;
 		
 		public Colour fromColour;
 		public Colour toColour;
 		public ColourBox colourBoxFrom;
 		public ColourBox colourBoxTo;
 		
 		public LinearLayout rangeLayout;
 		
 		public NumberBox firstRange;
 		public NumberBox secondRange;
 		
 		public String type = "gradient";
 		
 		public TextWidget t;
 		public TextWidget t1;
 		public TextWidget t2;
 		public TextWidget t3;
 		public TextWidget t4;
 		public TextWidget t5;
 		
 		public RangeContainer(int id, Window window)
 		{
 			this.id = id;
 			
 			fromColour = new Colour(0.0f,0.0f,0.0f);
 			toColour = new Colour(0.0f,0.0f,0.0f);
 			
 			firstRange = new NumberBox(40);
 			firstRange.setFlag(Widget.CENTER_VERTICAL);
 			secondRange = new NumberBox(40);
 			secondRange.setFlag(Widget.CENTER_VERTICAL);
 			
 			t2 = new TextWidget("Range: ");
 			t2.setFlag(Widget.CENTER_VERTICAL);
 			t3 = new TextWidget(" - ");
 			t3.setFlag(Widget.CENTER_VERTICAL);
 			t4 = new TextWidget("Gradient:");
 			t4.setFlag(Widget.CENTER_VERTICAL);
 			t5 = new TextWidget(" - ");
 			t5.setFlag(Widget.CENTER_VERTICAL);
 			
 			rangeLayout = new LinearLayout(LinearLayout.Direction.HORIZONTAL);
 			//rangeLayout.setFlag(Widget.WRAP);
 			rangeLayout.setHeight(50);
 			rangeLayout.setWidth(440);
 			
 			rangeLayout.setWindow(window);
 			rangeLayout.add(t2);
 			rangeLayout.add(firstRange);
 			rangeLayout.add(t3);
 			rangeLayout.add(secondRange);
 			rangeLayout.add(t4);
 			
 			colourBoxFrom = new ColourBox(new Vector2i(32,32));
 			colourBoxFrom.setColour(fromColour);
 			
 			rangeLayout.add(colourBoxFrom);
 			rangeLayout.add(t5);
 			
 			colourBoxTo = new ColourBox(new Vector2i(32,32));
 			colourBoxTo.setColour(toColour);
 			rangeLayout.add(colourBoxTo);
 		}
 		
 		public LinearLayout getLayout()
 		{
 			return rangeLayout;
 		}
 	}
 }
