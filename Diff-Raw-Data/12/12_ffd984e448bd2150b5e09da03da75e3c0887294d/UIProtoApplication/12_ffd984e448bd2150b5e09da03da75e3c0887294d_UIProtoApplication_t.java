 //v1.0
 
 package com.hexcore.cas.ui.test;
 
 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 
import com.hexcore.cas.Server;
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.model.Cell;
 import com.hexcore.cas.model.ColourRule;
 import com.hexcore.cas.model.ColourRuleSet;
 import com.hexcore.cas.model.RectangleGrid;
 import com.hexcore.cas.model.HexagonGrid;
 import com.hexcore.cas.model.TriangleGrid;
 import com.hexcore.cas.rulesystems.HexcoreVM;
 import com.hexcore.cas.rulesystems.Parser;
 import com.hexcore.cas.test.GameOfLife;
 import com.hexcore.cas.test.WaterFlow;
 import com.hexcore.cas.ui.toolkit.Button;
 import com.hexcore.cas.ui.toolkit.CheckBox;
 import com.hexcore.cas.ui.toolkit.Colour;
 import com.hexcore.cas.ui.toolkit.Container;
 import com.hexcore.cas.ui.toolkit.Dialog;
 import com.hexcore.cas.ui.toolkit.DropDownBox;
 import com.hexcore.cas.ui.toolkit.Event;
 import com.hexcore.cas.ui.toolkit.Fill;
 import com.hexcore.cas.ui.toolkit.HexagonGrid3DWidget;
 import com.hexcore.cas.ui.toolkit.HexagonGridWidget;
 import com.hexcore.cas.ui.toolkit.Image;
 import com.hexcore.cas.ui.toolkit.ImageWidget;
 import com.hexcore.cas.ui.toolkit.Layout;
 import com.hexcore.cas.ui.toolkit.LinearLayout;
 import com.hexcore.cas.ui.toolkit.Panel;
 import com.hexcore.cas.ui.toolkit.RectangleGrid3DWidget;
 import com.hexcore.cas.ui.toolkit.RectangleGridWidget;
 import com.hexcore.cas.ui.toolkit.ScrollableContainer;
 import com.hexcore.cas.ui.toolkit.SliderWidget;
 import com.hexcore.cas.ui.toolkit.TabbedView;
 import com.hexcore.cas.ui.toolkit.Text;
 import com.hexcore.cas.ui.toolkit.TextArea;
 import com.hexcore.cas.ui.toolkit.TextBox;
 import com.hexcore.cas.ui.toolkit.TextWidget;
 import com.hexcore.cas.ui.toolkit.Theme;
 import com.hexcore.cas.ui.toolkit.TriangleGrid3DWidget;
 import com.hexcore.cas.ui.toolkit.TriangleGridWidget;
 import com.hexcore.cas.ui.toolkit.View;
 import com.hexcore.cas.ui.toolkit.Widget;
 import com.hexcore.cas.ui.toolkit.Window;
 import com.hexcore.cas.ui.toolkit.Window.FileSelectResult;
 import com.hexcore.cas.ui.toolkit.WindowEventListener;
 import com.hexcore.cas.ui.toolkit.Text.Size;
 
 public class UIProtoApplication implements WindowEventListener
 {
     public Theme    theme;
     public Window    window;
     
     
     public View masterView;
     
     
     public TabbedView tabbedWorldView;
     
     public LinearLayout    mainMenuLayout;
     public LinearLayout worldLayout;
     
     
     public TextWidget worldEditorLabel;
     
     public Button simulateButton;
     
     //1st,2,3,4 ...tab
     
     public Button backButton;
     public Container propertiesContainer;
         
         public LinearLayout masterPropertiesLayout;
         public Container widgetPreviewContainer;
         public Container widget3DPreviewContainer;
         public LinearLayout widgetPreviewLayout;
         public LinearLayout cellShapeLayout;
         
         public GameOfLife            rectGameOfLife;
         public GameOfLife            triGameOfLife;
         public GameOfLife            hexGameOfLife;
         public LinearLayout propertiesLayout;
         public LinearLayout worldSizeLayout;
     
         public TextWidget worldSizeLabel;
         public TextWidget worldSizeXLabel;
         public TextWidget cellShapeLabel;
     
         
         public TextBox    worldSizeXTextBox;
         public TextBox    worldSizeYTextBox;
     
         
         public CheckBox wrapCheckBox;
         public DropDownBox cellShapeDropDownBox;
         
         public Button submitButton;
         
         public RectangleGrid3DWidget    rectGrid3DViewer;
         public HexagonGrid3DWidget        hexGrid3DViewer;
         public TriangleGrid3DWidget        triGrid3DViewer;
         
         public HexagonGridWidget gridViewer;
         public RectangleGridWidget rectGridViewer;
         public TriangleGridWidget triGridViewer;
         
         
     public Container rulesContainer;
         public LinearLayout rulesLayout;
         
         public Button clearRulesButton;
         public Button submitRulesButton;
         public Button openCALFileButton;
         public Button saveCALFileButton;
         
         public TextArea CALTextArea;
         public ScrollableContainer outputContainer;
         public LinearLayout outputLayout;
         
         public File calFile;
         public FileSelectResult selectedFile;
         
         public Dialog dialogCAL; 
         public LinearLayout dialogCALLayout;
         public    TextWidget dialogCALTitle;
         public TextWidget    dialogCALMessage;
         public Button dialogCALOKButton;
         
     
     
     public Container distributionContainer;
     public Container worldPreviewContainer;
     
     
     
     
     public LinearLayout    headerLayout;
     public LinearLayout    mainLayout;
     public LinearLayout    buttonBarLayout;
     public LinearLayout    innerLayout;
     
     public TextBox        nameTextBox;
     public CheckBox        checkBox;
     public DropDownBox    dropDownBox;
     public ImageWidget    headingImage;
     public Container    headingContainer;
     public TextWidget    headingLabel;
     public View            mainView;
     
     public Button        createWorldButton;
     public Button        loadWorldButton;
     public Button        optionsButton;
     public Button        helpButton;
     public Button        quitButton;
         
     public Panel        mainPanel;
     
     public LinearLayout            gridViewLayout;
 
 
     public Button                nextIterationButton;
     
     public ColourRuleSet        colourRules;
     
     public String    currentThemeName = "lightV2";
     public String    themeName = currentThemeName;
     
     public Dialog        dialog;
     public LinearLayout    dialogLayout;
     public TextWidget    dialogTitle;
     public TextWidget    dialogMessage;
     public Button        dialogOKButton;
     
     
     public HexagonGrid grid;
     public RectangleGrid rectGrid;
     public TriangleGrid triGrid;
     
     
     
     
     //Simulation Screen
     
     public Container simulationContainer;
     
     
     
     public HexagonGrid                waterFlowGrid;
     public WaterFlow                waterFlow;
     public HexagonGrid3DWidget        waterGrid3DViewer;
     
    public static Server s;
    
    public UIProtoApplication(Server _s)
     {
        s=_s;
         //Compiler Integration
         
         
         
         waterFlowGrid = new HexagonGrid(new Vector2i(100, 100));
         waterFlowGrid.setWrappable(false);
 
         waterFlow = new WaterFlow(waterFlowGrid);
         
         
         grid = new HexagonGrid(new Vector2i(12, 12));
         grid.getCell(6, 5).setValue(0, 1);
         grid.getCell(6, 6).setValue(0, 1);
         grid.getCell(6, 7).setValue(0, 1);        
         hexGameOfLife = new GameOfLife(grid);
         
         rectGrid = new RectangleGrid(new Vector2i(12, 12));
         rectGrid.getCell(2, 4).setValue(0, 1);
         rectGrid.getCell(3, 4).setValue(0, 1);
         rectGrid.getCell(4, 4).setValue(0, 1);
         rectGrid.getCell(4, 3).setValue(0, 1);
         rectGrid.getCell(3, 2).setValue(0, 1);
         rectGameOfLife = new GameOfLife(rectGrid);
         
         triGrid = new TriangleGrid(new Vector2i(12, 12));
         triGrid.getCell(7, 6).setValue(0, 1);
         triGrid.getCell(7, 7).setValue(0, 1);
         triGrid.getCell(7, 8).setValue(0, 1);
         triGrid.getCell(6, 6).setValue(0, 1);
         triGrid.getCell(6, 7).setValue(0, 1);
         triGrid.getCell(6, 8).setValue(0, 1);
         triGameOfLife = new GameOfLife(triGrid);
         
         
         colourRules = new ColourRuleSet(3);
         ColourRule    colourRule;
         
         colourRule = new ColourRule();
         colourRule.addRange(new ColourRule.Range(0.0, 1.0, new Colour(0.0f, 0.25f, 0.5f)));
         colourRule.addRange(new ColourRule.Range(1.0, 2.0, new Colour(0.0f, 0.8f, 0.5f)));
         colourRules.setColourRule(0, colourRule);
         
         colourRule = new ColourRule();
         colourRule.addRange(new ColourRule.Range(0.0, 15.1, new Colour(0.0f, 0.5f, 0.8f), new Colour(0.0f, 0.25f, 0.5f)));
         colourRules.setColourRule(1, colourRule);    
         
         colourRule = new ColourRule();
         colourRule.addRange(new ColourRule.Range(0.0, 8.0, new Colour(0.5f, 0.25f, 0.0f), new Colour(0.0f, 0.8f, 0.5f)));
         colourRule.addRange(new ColourRule.Range(8.0, 16.0, new Colour(0.0f, 0.8f, 0.5f), new Colour(0.4f, 1.0f, 0.8f)));
         colourRules.setColourRule(2, colourRule);    
         
         theme = new Theme();
         window = new Window("Cellular Automata Simulator - v1.0", 1024, 700, theme);
         
         window.addListener(this);
         window.show();
     }
     
     public void initialise()
     {
         theme.loadTheme(themeName);
         
         masterView = new View(new Vector2i(10, 10));
         masterView.setMargin(new Vector2i(0, 0));
         masterView.setFlag(Widget.FILL);
         window.add(masterView);
         
         mainMenuLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
         mainMenuLayout.setFlag(Widget.FILL);
         mainMenuLayout.setMargin(new Vector2i(0, 0));
         masterView.add(mainMenuLayout);
         
     
         
         
         
         mainLayout = new LinearLayout(new Vector2i(1000, 600), LinearLayout.Direction.HORIZONTAL);
         mainLayout.setFlag(Widget.CENTER);
         mainMenuLayout.add(mainLayout);
         
         
         ImageWidget mainLogo = new ImageWidget(theme.getImage("backgrounds", "main_bg.png"));
         mainLogo.setFlag(Widget.CENTER_VERTICAL);
         mainLayout.add(mainLogo);
         
         
         
         buttonBarLayout = new LinearLayout(new Vector2i(340, 350), LinearLayout.Direction.VERTICAL);
         buttonBarLayout.setFlag(Widget.CENTER_VERTICAL);
 
         mainLayout.add(buttonBarLayout);
         
         createWorldButton = new Button(new Vector2i(300, 50), "Create World");
         
         createWorldButton.setIcon(theme.getImage("icons", "create_icon.png"), theme.getImage("icons", "create_icon-white.png"));
         createWorldButton.setFlag(Widget.CENTER_HORIZONTAL);
         createWorldButton.setFlag(Widget.CENTER_VERTICAL);
         buttonBarLayout.add(createWorldButton);
         
         loadWorldButton = new Button(new Vector2i(300, 50), "Load World");
         
         loadWorldButton.setIcon(theme.getImage("icons", "load_icon.png"), theme.getImage("icons", "load_icon-white.png"));
         loadWorldButton.setFlag(Widget.CENTER_HORIZONTAL);
         loadWorldButton.setFlag(Widget.CENTER_VERTICAL);
         buttonBarLayout.add(loadWorldButton);
         
         optionsButton = new Button(new Vector2i(300, 50), "Options");
         
         optionsButton.setIcon(theme.getImage("icons", "options_icon.png"), theme.getImage("icons", "options_icon-white.png"));
         optionsButton.setFlag(Widget.CENTER_HORIZONTAL);
         optionsButton.setFlag(Widget.CENTER_VERTICAL);
         buttonBarLayout.add(optionsButton);
         
         helpButton = new Button(new Vector2i(300, 50), "Help");
         
         helpButton.setIcon(theme.getImage("icons", "help_icon.png"), theme.getImage("icons", "help_icon-white.png"));
         helpButton.setFlag(Widget.CENTER_HORIZONTAL);
         helpButton.setFlag(Widget.CENTER_VERTICAL);
         buttonBarLayout.add(helpButton);
         
         quitButton = new Button(new Vector2i(300, 50), "Quit");
         
         quitButton.setIcon(theme.getImage("icons", "on-off_icon.png"), theme.getImage("icons", "on-off_icon-white.png"));
         quitButton.setFlag(Widget.CENTER_HORIZONTAL);
         quitButton.setFlag(Widget.CENTER_VERTICAL);
         buttonBarLayout.add(quitButton);
         
 
         
         /// Main WORLD BUILDER
         
         
         worldLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
         worldLayout.setFlag(Widget.FILL);
         
         masterView.add(worldLayout);
         
         
         tabbedWorldView = new TabbedView(new Vector2i(30,30));
         tabbedWorldView.setFlag(Widget.FILL);
         
         
         
         LinearLayout worldHeaderLayout = new LinearLayout(new Vector2i(100, 30), LinearLayout.Direction.HORIZONTAL);
         worldHeaderLayout.setFlag(Widget.FILL_HORIZONTAL);
         worldLayout.add(worldHeaderLayout);
         
         
         
     
         
         
     
     //    worldHeaderLayout.add(minimizeImage);
     //    worldHeaderLayout.add(fullscreenImage);
     //    worldHeaderLayout.add(quitImage);
         
         
         
     
         
         
 //        worldEditorLabel = new TextWidget("World Editor Menu", Text.Size.LARGE);
         
         
     //    worldEditorLabel.setFlag(Widget.CENTER_HORIZONTAL);
         //worldLayout.add(worldEditorLabel);
         worldLayout.add(tabbedWorldView);
         
         // OUR WORLD EDITOR Containers
         
         
         propertiesContainer = new Container(new Vector2i(100, 100));    
         propertiesContainer.setFlag(Widget.FILL);
         tabbedWorldView.add(propertiesContainer, "World Properties");
         
         masterPropertiesLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
         masterPropertiesLayout.setFlag(Widget.FILL);
         propertiesContainer.setContents(masterPropertiesLayout);
         
     
         
        LinearLayout instructionsLayout = new LinearLayout(LinearLayout.Direction.HORIZONTAL);
        //instructionsLayout.setBackground(new Fill(new Colour(0.4f, 0.63f, 0.91f)));
 
        instructionsLayout.setHeight(35);
 
        instructionsLayout.setFlag(Widget.CENTER_HORIZONTAL);
               masterPropertiesLayout.add(instructionsLayout);
        
     instructionsLayout.setBorder(new Fill(new Colour(0.6f,0.6f,0.6f)));
     
    
     
     TextWidget propertiesInstructions = new TextWidget("Specify your world properties such as cell shape, world size and whether the world is wrappable.");
     instructionsLayout.add(propertiesInstructions);
         
        instructionsLayout.setWidth(propertiesInstructions.getWidth()+ 20);
         
        
        
            //2d
 
     
     gridViewer = new HexagonGridWidget((HexagonGrid)hexGameOfLife.getGrid(), 16);
     gridViewer.setColourRuleSet(colourRules);
 
     
 
     
     rectGridViewer = new RectangleGridWidget((RectangleGrid)rectGameOfLife.getGrid(), 24);
     rectGridViewer.setColourRuleSet(colourRules);
     
 
 
 
     triGridViewer = new TriangleGridWidget((TriangleGrid)triGameOfLife.getGrid(), 32);
     triGridViewer.setColourRuleSet(colourRules);
     
        
            //
         
         rectGrid3DViewer = new RectangleGrid3DWidget(new Vector2i(400, 300), (RectangleGrid)rectGameOfLife.getGrid(), 24);
         rectGrid3DViewer.setFlag(Widget.FILL);
         rectGrid3DViewer.addSlice(0, 16.0f);
         
         triGrid3DViewer = new TriangleGrid3DWidget(new Vector2i(400, 300), (TriangleGrid)triGameOfLife.getGrid(), 24);
         triGrid3DViewer.setFlag(Widget.FILL);
         triGrid3DViewer.addSlice(0, 16.0f);
         
         hexGrid3DViewer = new HexagonGrid3DWidget(new Vector2i(400, 300), (HexagonGrid)hexGameOfLife.getGrid(), 24);
         hexGrid3DViewer.setFlag(Widget.FILL);
         hexGrid3DViewer.addSlice(0, 16.0f);
         
         
      // Water Flow
         waterGrid3DViewer = new HexagonGrid3DWidget(new Vector2i(400, 300), (HexagonGrid)waterFlow.getGrid(), 24);
         waterGrid3DViewer.setFlag(Widget.FILL);
         waterGrid3DViewer.setBackgroundColour(new Colour(0.6f, 0.85f, 1.0f));
         waterGrid3DViewer.setColourRuleSet(colourRules);
         waterGrid3DViewer.addSlice(2, 2, 15.0f);
         waterGrid3DViewer.addSlice(1, 1, 15.0f);
         
         
         
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
         
         worldSizeXTextBox = new TextBox(35);
         worldSizeXTextBox.setFlag(Widget.CENTER_VERTICAL);
         worldSizeLayout.add(worldSizeXTextBox);
         
         worldSizeXLabel = new TextWidget("X", Size.LARGE);
         worldSizeXLabel.setFlag(Widget.CENTER_VERTICAL);
         worldSizeLayout.add(worldSizeXLabel);
         
         worldSizeYTextBox = new TextBox(35);
         worldSizeYTextBox.setFlag(Widget.CENTER_VERTICAL);
         worldSizeLayout.add(worldSizeYTextBox);
         
         wrapCheckBox = new CheckBox(new Vector2i(100,50), "Wrappable");
         wrapCheckBox.setFlag(Widget.CENTER_VERTICAL);
         wrapCheckBox.setMargin(new Vector2i(50,0));
         worldSizeLayout.add(wrapCheckBox);
         
     
         cellShapeLayout = new LinearLayout(LinearLayout.Direction.HORIZONTAL);
         cellShapeLayout.setFlag(Widget.FILL_HORIZONTAL);
         cellShapeLayout.setHeight(65);
 
         
         propertiesLayout.add(cellShapeLayout);
         
         cellShapeLabel = new TextWidget("Cell Shape:",Size.MEDIUM);
         cellShapeLabel.setFlag(Widget.CENTER_VERTICAL);
         cellShapeLayout.add(cellShapeLabel);
         
         
         
         
         
         
         
         cellShapeDropDownBox = new DropDownBox(new Vector2i(100,20));
         cellShapeDropDownBox.setFlag(Widget.CENTER_VERTICAL);
         cellShapeDropDownBox.addItem("Square");
         cellShapeDropDownBox.addItem("Triangle");
         cellShapeDropDownBox.addItem("Hexagon");
         cellShapeDropDownBox.setSelected(0);
         
         
         
         
         
     
         
         cellShapeLayout.add(cellShapeDropDownBox);
                 
         submitButton = new Button(new Vector2i(120,35), "Preview");
         propertiesLayout.add(submitButton);
         
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
         
         widget3DPreviewContainer.setContents(rectGrid3DViewer);
         widgetPreviewContainer.setContents(rectGridViewer);
         
 
         LinearLayout buttonHeaderLayout = new LinearLayout(new Vector2i(525, 50), LinearLayout.Direction.HORIZONTAL);
         buttonHeaderLayout.setBackground(new Fill(new Colour(0.7f, 0.7f, 0.7f)));
         buttonHeaderLayout.setBorder(new Fill(new Colour(0.7f, 0.7f, 0.7f)));
         buttonHeaderLayout.setFlag(Widget.CENTER_HORIZONTAL);
         worldLayout.add(buttonHeaderLayout);
             
             backButton = new Button(new Vector2i(100, 50), "Main Menu");
             backButton.setWidth(165);
             backButton.setHeight(35);
             buttonHeaderLayout.add(backButton);
             
             Button saveWorldButton = new Button(new Vector2i(100, 50), "Save World");
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
         
         
         
         //
         
         
         //
         
         
         
             
         LinearLayout CALLayout = new LinearLayout(LinearLayout.Direction.HORIZONTAL);
         CALLayout.setFlag(Widget.FILL);
         masterRulesLayout.add(CALLayout);
         
         LinearLayout leftLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
         leftLayout.setFlag(Widget.FILL);
         CALLayout.add(leftLayout);
         
         LinearLayout rightLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
         rightLayout.setFlag(Widget.FILL);
         CALLayout.add(rightLayout);
         
         
         ImageWidget calEditorHeader = new ImageWidget(window.getTheme().getImage("headers","cal_editor_header.png"));
         calEditorHeader.setFlag(Widget.CENTER_HORIZONTAL);
         leftLayout.add(calEditorHeader);    
         
          CALTextArea = new TextArea(100, 20);
          CALTextArea.setMargin(new Vector2i(0,0));
          CALTextArea.setFlag(Widget.FILL);
          CALTextArea.setLineNumbers(true);
     
         
         ScrollableContainer textAreaContainer = new ScrollableContainer(new Vector2i(100,100));
         textAreaContainer.setFlag(Widget.FILL);
     
         textAreaContainer.setBorder(new Fill(new Colour(0.7f, 0.7f, 0.7f)));
         leftLayout.add(textAreaContainer);
         
         textAreaContainer.setContents(CALTextArea);
         
         
         ImageWidget compilerOutputHeader = new ImageWidget(window.getTheme().getImage("headers","compiler_output_header.png"));
         compilerOutputHeader.setFlag(Widget.CENTER_HORIZONTAL);
         rightLayout.add(compilerOutputHeader);    
         
         
         /** A list **/        
         outputContainer = new ScrollableContainer(new Vector2i(350, 100));
         outputContainer.setFlag(Widget.FILL);
         outputContainer.setFlag(Widget.CENTER_HORIZONTAL);
         outputContainer.setThemeClass("List");
         rightLayout.add(outputContainer);
         
         outputLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
         outputLayout.setMargin(new Vector2i(0, 0));
         outputLayout.setFlag(Widget.WRAP);
         
         outputContainer.setContents(outputLayout);
         
 /**********/
         LinearLayout buttonRulesLayout = new LinearLayout(new Vector2i(700, 50), LinearLayout.Direction.HORIZONTAL);
         //buttonRulesLayout.setBackground(new Fill(new Colour(0.7f, 0.7f, 0.7f)));
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
             
             openCALFileButton = new Button(new Vector2i(100, 50), "Open CAL File");
             openCALFileButton.setWidth(165);
             openCALFileButton.setHeight(35);
             buttonRulesLayout.add(openCALFileButton);
             
             saveCALFileButton = new Button(new Vector2i(100, 50), "Save CAL File");
             saveCALFileButton.setWidth(165);
             saveCALFileButton.setHeight(35);
             buttonRulesLayout.add(saveCALFileButton);
             
         
         
         
         distributionContainer = new Container(new Vector2i(100, 100));
         distributionContainer.setFlag(Widget.FILL);
         
         worldPreviewContainer = new Container(new Vector2i(100, 100));
         worldPreviewContainer.setFlag(Widget.FILL);
         
         
 
         tabbedWorldView.add(distributionContainer, "Distribution Settings");
         tabbedWorldView.add(worldPreviewContainer, "World Preview");
         
         
         
         
         
         
         
      
         headingLabel = new TextWidget("Cellular Automata Simulator", Text.Size.LARGE, Colour.WHITE);
         headingLabel.setFlag(Widget.CENTER);
         
         
         ////
         
         
         // Dialog
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
         
         simulationContainer = new Container(new Vector2i(100,100));
         simulationContainer.setFlag(Widget.FILL);
         masterView.add(simulationContainer);
         
         
         LinearLayout masterSimulationLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
         masterSimulationLayout.setFlag(Widget.FILL);
         simulationContainer.setContents(masterSimulationLayout);
         
         masterSimulationLayout.add(worldHeaderLayout);
         
         Container simulationWindowContainer = new Container(new Vector2i(100,300));
         simulationWindowContainer.setFlag(Widget.FILL);
         simulationWindowContainer.setBackground(new Fill(new Colour(0f,0f,0f)));
         masterSimulationLayout.add(simulationWindowContainer);
         simulationWindowContainer.setContents(waterGrid3DViewer);
         
         
         
         
         
 
         //SLIDER
         
         LinearLayout sliderLayout = new LinearLayout(new Vector2i(40, 55), LinearLayout.Direction.HORIZONTAL);
         sliderLayout.setBorder(new Fill(new Colour(0.7F, 0.7F, 0.7F)));
         sliderLayout.setFlag(2);
         masterSimulationLayout.add(sliderLayout);
         
         SliderWidget slider = new SliderWidget(100);
         slider.setMargin(new Vector2i(5, 5));
         slider.setFlag(2);
         slider.setShowValue(true);
         sliderLayout.add(slider);
         
         LinearLayout simulationControlsLayout = new LinearLayout(new Vector2i(100, 150), LinearLayout.Direction.HORIZONTAL);
         simulationControlsLayout.setFlag(Widget.FILL_HORIZONTAL);
         
         masterSimulationLayout.add(simulationControlsLayout);
         
         LinearLayout detailsLayout = new LinearLayout(new Vector2i(250, 150), LinearLayout.Direction.VERTICAL);
         detailsLayout.setBorder(new Fill(new Colour(0.7F, 0.7F, 0.7F)));
         simulationControlsLayout.add(detailsLayout);
         
         LinearLayout innerDetailsLayout = new LinearLayout(new Vector2i(160, 25), LinearLayout.Direction.HORIZONTAL);
         innerDetailsLayout.setFlag(8);
         detailsLayout.add(innerDetailsLayout);
         
         LinearLayout innerDetailsLayout2 = new LinearLayout(new Vector2i(205, 40), LinearLayout.Direction.VERTICAL);
        // innerDetailsLayout2.setFlag(8);
         detailsLayout.add(innerDetailsLayout2);
         
         TextWidget iterationsText = new TextWidget("Iteration: 255");
         innerDetailsLayout2.add(iterationsText);
         
         TextWidget timeText = new TextWidget("Elapsed: 00:00:02");
         innerDetailsLayout2.add(timeText);
         
         TextWidget numCellsText = new TextWidget("Num. Cells: 355 000");
         innerDetailsLayout2.add(numCellsText);
         
         
         ImageWidget detailsImage = new ImageWidget(this.window.getTheme().getImage("headers", "details_header.png"));
         detailsImage.setFlag(8);
         innerDetailsLayout.add(detailsImage);
         
         LinearLayout playbackLayout = new LinearLayout(new Vector2i(250, 150), LinearLayout.Direction.VERTICAL);
         playbackLayout.setBorder(new Fill(new Colour(0.7F, 0.7F, 0.7F)));
         simulationControlsLayout.add(playbackLayout);
         
         LinearLayout innerPlaybackLayout = new LinearLayout(new Vector2i(205, 25), LinearLayout.Direction.HORIZONTAL);
         innerPlaybackLayout.setFlag(8);
         playbackLayout.add(innerPlaybackLayout);
         
         LinearLayout innerPlaybackLayout2 = new LinearLayout(new Vector2i(205, 40), LinearLayout.Direction.HORIZONTAL);
         innerPlaybackLayout2.setFlag(8);
         playbackLayout.add(innerPlaybackLayout2);
         
         LinearLayout innerPlaybackLayout3 = new LinearLayout(new Vector2i(225, 35), LinearLayout.Direction.HORIZONTAL);
         innerPlaybackLayout3.setFlag(8);
         playbackLayout.add(innerPlaybackLayout3);
         
         
         Button stepBackwardButton = new Button(this.window.getTheme().getImage("icons", "step_backward_icon.png"));
         stepBackwardButton.setMargin(new Vector2i(5, 0));
         innerPlaybackLayout2.add(stepBackwardButton);
         Button playButton = new Button(this.window.getTheme().getImage("icons", "play_icon.png"));
         playButton.setMargin(new Vector2i(5, 0));
         innerPlaybackLayout2.add(playButton);
         Button pauseButton = new Button(this.window.getTheme().getImage("icons", "pause_icon.png"));
         pauseButton.setMargin(new Vector2i(5, 0));
         innerPlaybackLayout2.add(pauseButton);
         Button resetButton = new Button(this.window.getTheme().getImage("icons", "reset_icon.png"));
         resetButton.setMargin(new Vector2i(5, 0));
         innerPlaybackLayout2.add(resetButton);
         Button stepForwardButton = new Button(window.getTheme().getImage("icons", "step_forward_icon.png"));
         stepForwardButton.setMargin(new Vector2i(5, 0));
         innerPlaybackLayout2.add(stepForwardButton);
         TextWidget playbackSpeedHeader = new TextWidget("Playback Speed:");
         innerPlaybackLayout3.add(playbackSpeedHeader);
         SliderWidget playbackSpeedSlider = new SliderWidget(100);
         playbackSpeedSlider.setFlag(12);
         playbackSpeedSlider.setShowValue(true);
         innerPlaybackLayout3.add(playbackSpeedSlider);
         ImageWidget playbackImage = new ImageWidget(window.getTheme().getImage("headers", "playback_header.png"));
         playbackImage.setFlag(8);
         innerPlaybackLayout.add(playbackImage);
         
         
         
         LinearLayout cameraLayout = new LinearLayout(new Vector2i(270, 150), LinearLayout.Direction.VERTICAL);
         cameraLayout.setBorder(new Fill(new Colour(0.7F, 0.7F, 0.7F)));
         simulationControlsLayout.add(cameraLayout);
         LinearLayout innerCameraLayout = new LinearLayout(new Vector2i(190, 25), LinearLayout.Direction.HORIZONTAL);
         innerCameraLayout.setFlag(8);
         cameraLayout.add(innerCameraLayout);
         
         LinearLayout innerCameraLayout2 = new LinearLayout(new Vector2i(245, 40), LinearLayout.Direction.HORIZONTAL);
         innerCameraLayout2.setFlag(8);
         cameraLayout.add(innerCameraLayout2);
         
         LinearLayout innerCameraLayout3 = new LinearLayout(new Vector2i(160, 40), LinearLayout.Direction.HORIZONTAL);
         innerCameraLayout3.setFlag(8);
         cameraLayout.add(innerCameraLayout3);
         
         Button zoomInButton = new Button(window.getTheme().getImage("icons", "zoom_in_icon.png"));
         zoomInButton.setMargin(new Vector2i(5, 0));
         innerCameraLayout2.add(zoomInButton);
         Button zoomOutButton = new Button(window.getTheme().getImage("icons", "zoom_out_icon.png"));
         zoomOutButton.setMargin(new Vector2i(5, 0));
         innerCameraLayout2.add(zoomOutButton);
         Button moveUpButton = new Button(window.getTheme().getImage("icons", "up_icon.png"));
         moveUpButton.setMargin(new Vector2i(5, 0));
         innerCameraLayout2.add(moveUpButton);
         Button moveDownButton = new Button(window.getTheme().getImage("icons", "down_icon.png"));
         moveDownButton.setMargin(new Vector2i(5, 0));
         innerCameraLayout2.add(moveDownButton);
         Button moveLeftButton = new Button(window.getTheme().getImage("icons", "left_icon.png"));
         moveLeftButton.setMargin(new Vector2i(5, 0));
         innerCameraLayout2.add(moveLeftButton);
         Button moveRightButton = new Button(window.getTheme().getImage("icons", "right_icon.png"));
         moveRightButton.setMargin(new Vector2i(5, 0));
         innerCameraLayout2.add(moveRightButton);
         
         
         Button yawLeftButton = new Button(window.getTheme().getImage("icons", "yaw_left_icon.png"));
         yawLeftButton.setMargin(new Vector2i(5, 0));
         innerCameraLayout3.add(yawLeftButton);
         
         Button yawRightButton = new Button(window.getTheme().getImage("icons", "yaw_right_icon.png"));
         yawRightButton.setMargin(new Vector2i(5, 0));
         innerCameraLayout3.add(yawRightButton);
         
         Button pitchUpButton = new Button(window.getTheme().getImage("icons", "pitch_up_icon.png"));
         pitchUpButton.setMargin(new Vector2i(5, 0));
         innerCameraLayout3.add(pitchUpButton);
         
         Button pitchDownButton = new Button(window.getTheme().getImage("icons", "pitch_down_icon.png"));
         pitchDownButton.setMargin(new Vector2i(5, 0));
         
         innerCameraLayout3.add(pitchDownButton);
         
         
         ImageWidget cameraImage = new ImageWidget(window.getTheme().getImage("headers", "camera_header.png"));
         cameraImage.setFlag(8);
         innerCameraLayout.add(cameraImage);
         
         
         
         
         
         
         LinearLayout viewSettingsLayout = new LinearLayout(new Vector2i(250, 90), LinearLayout.Direction.VERTICAL);
         viewSettingsLayout.setBorder(new Fill(new Colour(0.7F, 0.7F, 0.7F)));
         simulationControlsLayout.add(viewSettingsLayout);
         LinearLayout innerViewSettingsLayout = new LinearLayout(new Vector2i(148, 25), LinearLayout.Direction.HORIZONTAL);
         innerViewSettingsLayout.setFlag(8);
         viewSettingsLayout.add(innerViewSettingsLayout);
         LinearLayout innerViewSettingsLayout2 = new LinearLayout(new Vector2i(218, 40), LinearLayout.Direction.HORIZONTAL);
         innerViewSettingsLayout2.setFlag(8);
         viewSettingsLayout.add(innerViewSettingsLayout2);
         Button toggleHideButton = new Button(window.getTheme().getImage("icons", "toggle_hide_icon.png"));
         toggleHideButton.setMargin(new Vector2i(5, 0));
         innerViewSettingsLayout2.add(toggleHideButton);
         Button toggle3DButton = new Button(this.window.getTheme().getImage("icons", "3d_icon.png"));
         toggle3DButton.setMargin(new Vector2i(5, 0));
         innerViewSettingsLayout2.add(toggle3DButton);
         Button toggleWireframeButton = new Button(this.window.getTheme().getImage("icons", "toggle_wireframe_icon.png"));
         toggleWireframeButton.setMargin(new Vector2i(5, 0));
         innerViewSettingsLayout2.add(toggleWireframeButton);
         Button addViewportButton = new Button(this.window.getTheme().getImage("icons", "add_viewport_icon.png"));
         addViewportButton.setMargin(new Vector2i(5, 0));
         innerViewSettingsLayout2.add(addViewportButton);
         Button removeViewportButton = new Button(this.window.getTheme().getImage("icons", "remove_viewport_icon.png"));
 removeViewportButton.setMargin(new Vector2i(5, 0));
         innerViewSettingsLayout2.add(removeViewportButton);
         ImageWidget viewSettingsHeader = new ImageWidget(this.window.getTheme().getImage("headers", "view_settings_header.png"));
         cameraImage.setFlag(8);
         innerViewSettingsLayout.add(viewSettingsHeader);
         Button toggleShowButton = new Button(new Vector2i(10, 15), "");
         toggleShowButton.setFlag(2);
         toggleShowButton.setVisible(false);
         masterSimulationLayout.add(toggleShowButton);
 
         
         
         
         
         
         ///
         window.relayout();
     }
     
     static public void main(String args[])
     {
        new UIProtoApplication(s);
     }
     
     @Override
     public void update(float delta)
     {
         if (!themeName.equals(currentThemeName))
         {
             System.out.println("Changing theme to "+themeName);
             
             theme.loadTheme(themeName);
             currentThemeName = themeName;
             
             window.relayout();
         }    
     }
     
     @Override
     public void render()
     {
 
     }
 
     @Override
     public void handleWindowEvent(Event event)
     {
         if (event.type == Event.Type.ACTION)
         {
             if (event.target == createWorldButton)
             {
                 
                 masterView.setIndex(1 - masterView.getIndex());
                 //buttonBarLayout.toggleVisibility();
                 window.relayout();
                 
                 
                             }
             else if (event.target == loadWorldButton)
             {
                 System.out.println(window.askUserForFile("Load a world"));
             }
             else if (event.target == helpButton)
             {
                 mainView.setIndex(3 - mainView.getIndex());
             }
             else if (event.target == quitButton)
             {
                 window.exit();
             }
             else if (event.target == nextIterationButton)
             {
                 
                     
             
             }
             else if ((event.target == dialogOKButton) || (event.target == dialogCALOKButton))
             {
                 window.closeModalDialog();
             }
             
             else if (event.target == backButton)
             {
                 masterView.setIndex(0);
             }
             else if (event.target == submitButton)
             {
                 
                 
                 if(( Integer.parseInt(worldSizeXTextBox.getText()) < 5) || ( Integer.parseInt(worldSizeYTextBox.getText()) < 5))
                 {
                     window.showModalDialog(dialog);
                     return;
                 }
             
             String shape = cellShapeDropDownBox.getSelectedText();
                 
                 
             
                 if (shape == "Square")
                 {
                     // 3D Rectangle Grid
                     
                     rectGrid = new RectangleGrid(new Vector2i(Integer.parseInt(worldSizeXTextBox.getText()), Integer.parseInt(worldSizeYTextBox.getText())));
                     rectGrid.getCell(2, 4).setValue(0, 1);
                     rectGrid.getCell(3, 4).setValue(0, 1);
                     rectGrid.getCell(4, 4).setValue(0, 1);
                     rectGrid.getCell(4, 3).setValue(0, 1);
                     rectGrid.getCell(3, 2).setValue(0, 1);
                     rectGameOfLife = new GameOfLife(rectGrid);
                     
                     rectGrid3DViewer = new RectangleGrid3DWidget(new Vector2i(400, 300), (RectangleGrid)rectGameOfLife.getGrid(), 24);
                     rectGrid3DViewer.setFlag(Widget.FILL);
                     rectGrid3DViewer.addSlice(0, 16.0f);
                     
                     
                     rectGridViewer = new RectangleGridWidget((RectangleGrid)rectGameOfLife.getGrid(), 16);
                     rectGridViewer.setColourRuleSet(colourRules);
                     
                     widgetPreviewContainer.setContents(rectGridViewer);
                     widget3DPreviewContainer.setContents(rectGrid3DViewer);
                 }
                 else if (shape == "Triangle")
                 {
                     // 3D Triangle Grid
                     
                     triGrid = new TriangleGrid(new Vector2i(Integer.parseInt(worldSizeXTextBox.getText()), Integer.parseInt(worldSizeYTextBox.getText())));
                     triGrid.getCell(7, 6).setValue(0, 1);
                     triGrid.getCell(7, 7).setValue(0, 1);
                     triGrid.getCell(7, 8).setValue(0, 1);
                     triGrid.getCell(6, 6).setValue(0, 1);
                     triGrid.getCell(6, 7).setValue(0, 1);
                     triGrid.getCell(6, 8).setValue(0, 1);
                     triGameOfLife = new GameOfLife(triGrid);
         
                     
                     
                     triGrid3DViewer = new TriangleGrid3DWidget(new Vector2i(400, 300), (TriangleGrid)triGameOfLife.getGrid(), 24);
                     triGrid3DViewer.setFlag(Widget.FILL);
                     triGrid3DViewer.addSlice(0, 16.0f);
                     
                     triGridViewer = new TriangleGridWidget((TriangleGrid)triGameOfLife.getGrid(), 32);
                     triGridViewer.setColourRuleSet(colourRules);
                     
                     
                     widgetPreviewContainer.setContents(triGridViewer);
                     widget3DPreviewContainer.setContents(triGrid3DViewer);
                     
                 }
                 else
                 {
                     grid = new HexagonGrid(new Vector2i(Integer.parseInt(worldSizeXTextBox.getText()), Integer.parseInt(worldSizeYTextBox.getText())));
                     grid.getCell(6, 5).setValue(0, 1);
                     grid.getCell(6, 6).setValue(0, 1);
                     grid.getCell(6, 7).setValue(0, 1);        
                     hexGameOfLife = new GameOfLife(grid);
                     
                     hexGrid3DViewer = new HexagonGrid3DWidget(new Vector2i(400, 300), (HexagonGrid)hexGameOfLife.getGrid(), 24);
                     hexGrid3DViewer.setFlag(Widget.FILL);
                     hexGrid3DViewer.addSlice(0, 16.0f);
             
                     gridViewer = new HexagonGridWidget((HexagonGrid)hexGameOfLife.getGrid(), 16);
                     gridViewer.setColourRuleSet(colourRules);
 
                     widgetPreviewContainer.setContents(gridViewer);
                     widget3DPreviewContainer.setContents(hexGrid3DViewer);
                 }
             }
             else if (event.target == clearRulesButton)
             {
                 CALTextArea.setText(" ");
     
             }
             else if (event.target ==submitRulesButton)
             {
                 
                     
                 if (selectedFile == null)
                     calFile = new File("rules/rules.cal");
                 else
                     calFile = new File(selectedFile.directory + "/" + selectedFile.filename);
                 
                 try {
                                  FileWriter outFile = new FileWriter(calFile);
                                  PrintWriter out = new PrintWriter(outFile);
                          
                                       out.println(CALTextArea.getText());
                                       out.close();
                                   
                               } catch (IOException e){
                                   e.printStackTrace();
                               }
                 
                 System.out.println("THE PATH:" + calFile.getAbsolutePath());
         
                 HexcoreVM.loadRules(calFile.getAbsolutePath());
                 
                 TextWidget text = new TextWidget("Compiler Report:");
                 outputLayout.add(text);
             
                 
                 ArrayList<String> parserResults  = Parser.getResult();
                 
                 Iterator<String> iterator = parserResults.iterator();
                 outputLayout = new LinearLayout(LinearLayout.Direction.VERTICAL);
                 outputLayout.setMargin(new Vector2i(0, 0));
                 outputLayout.setFlag(Widget.WRAP);
                 outputContainer.setContents(outputLayout);
                 
                 while(iterator.hasNext()) {
                       TextWidget t = new TextWidget((String) iterator.next());
                      
                       outputLayout.add(t);
                       window.relayout();
                 }
                 
                 
                 
                 
                 
             }
             else if (event.target == openCALFileButton)
             {
             
             
                 
                 selectedFile = window.askUserForFileToLoad("Select CAL File", "cal");
                 
                 System.out.println(selectedFile.directory);
                 System.out.println(selectedFile.filename);
                 
                 if (selectedFile.filename.contains(".cal"))
                 {
                      try
                          {
                           
                           FileInputStream fstream = new FileInputStream(selectedFile.directory + selectedFile.filename);
                           DataInputStream in = new DataInputStream(fstream);
                           BufferedReader br = new BufferedReader(new InputStreamReader(in));
                           String strLine;
                           String output = "";
                           
                           while ((strLine = br.readLine()) != null)
                               {
                               output += strLine + "\n";
                               }
                           
                           in.close();
                          
                           CALTextArea.setText(output);
                           
                           
                          } catch (Exception e) { window.showModalDialog(dialogCAL);}
                     
                     
                 }
                 
             
                 
                  
             }
             
             else if (event.target == simulateButton)
             {
                 masterView.setIndex(2);
             }
             
             
         }
         else if (event.type == Event.Type.CHANGE)
         {
             if (event.target == checkBox)
             {
                 if (checkBox.isChecked())
                     nameTextBox.setText("Flying Dutchmen");
                 else
                     nameTextBox.setText("Ostrich");
             }
             else if (event.target == cellShapeDropDownBox)
             {
     
                 
                 
             }
             
         }
     }
 }
