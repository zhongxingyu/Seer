 package com.galaev.tsp.gui.controllers;
 
 import com.galaev.tsp.gui.Message;
 import com.galaev.tsp.gui.Prompt;
 import com.galaev.tsp.model.Cell;
 import com.galaev.tsp.model.Matrix;
 import com.galaev.tsp.model.Route;
 import com.galaev.tsp.solver.SolverService;
 import javafx.animation.FadeTransition;
 import javafx.animation.FadeTransitionBuilder;
 import javafx.animation.FillTransition;
 import javafx.animation.FillTransitionBuilder;
 import javafx.animation.StrokeTransition;
 import javafx.animation.StrokeTransitionBuilder;
 import javafx.application.Platform;
 import javafx.beans.binding.Bindings;
 import javafx.beans.property.DoubleProperty;
 import javafx.beans.property.SimpleDoubleProperty;
 import javafx.beans.property.SimpleStringProperty;
 import javafx.beans.property.StringProperty;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.ObservableList;
 import javafx.concurrent.WorkerStateEvent;
 import javafx.event.ActionEvent;
 import javafx.event.Event;
 import javafx.event.EventDispatchChain;
 import javafx.event.EventDispatcher;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.geometry.Pos;
 import javafx.geometry.Rectangle2D;
 import javafx.geometry.VPos;
 import javafx.scene.Node;
 import javafx.scene.control.Button;
 import javafx.scene.control.CheckMenuItem;
 import javafx.scene.control.ContextMenu;
 import javafx.scene.control.ContextMenuBuilder;
 import javafx.scene.control.Label;
 import javafx.scene.control.LabelBuilder;
 import javafx.scene.control.Menu;
 import javafx.scene.control.MenuItem;
 import javafx.scene.control.MenuItemBuilder;
 import javafx.scene.control.RadioMenuItem;
 import javafx.scene.control.Tab;
 import javafx.scene.control.TabPane;
 import javafx.scene.control.TextArea;
 import javafx.scene.control.TextField;
 import javafx.scene.control.TextFieldBuilder;
 import javafx.scene.control.ToggleGroup;
 import javafx.scene.effect.Lighting;
 import javafx.scene.input.ContextMenuEvent;
 import javafx.scene.input.MouseButton;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.GridPane;
 import javafx.scene.layout.Pane;
 import javafx.scene.layout.Priority;
 import javafx.scene.paint.Color;
 import javafx.scene.shape.Circle;
 import javafx.scene.shape.Line;
 import javafx.scene.shape.LineBuilder;
 import javafx.scene.text.Font;
 import javafx.scene.text.Text;
 import javafx.scene.text.TextBuilder;
 import javafx.stage.FileChooser;
 import javafx.stage.Screen;
 import javafx.stage.Stage;
 import javafx.util.Duration;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Random;
 import java.util.ResourceBundle;
 import java.util.Scanner;
 
 /**
  * Class represents a controller (in MVC model) for
  * the main window of the application.
  * 
  * @author Anton Galaev
  */
 public class MainController implements Initializable {
 
     /** Infinity sign for table */
     private static final String INFINITY_SIGN = "\u221e";
     /** Animation speed delay */
     private static final double ANIMATION_DELAY = 0.5;
 
     /* Main window controls and components */
     @FXML private TabPane tabPane;
     @FXML private Tab canvasTab;
     @FXML private Tab tableTab;
     @FXML private Tab resultTab;
     @FXML private Pane canvas;
     @FXML private GridPane table;
     @FXML private TextArea result;
     @FXML private Menu fileMenu;
     @FXML private Menu editMenu;
     @FXML private Menu solveMenu;
     @FXML private Menu settingsMenu;
     @FXML private Menu languageMenu;
     @FXML private Menu inputModeMenu;
     @FXML private MenuItem newMenuItem;
     @FXML private MenuItem openMenuItem;
     @FXML private MenuItem saveMenuItem;
     @FXML private MenuItem exitMenuItem;
     @FXML private MenuItem clearMenuItem;
     @FXML private MenuItem clearResMenuItem;
     @FXML private MenuItem solveMenuItem;
     @FXML private MenuItem abortMenuItem;
     @FXML private MenuItem randomMenuItem;
     @FXML private MenuItem maxRandomMenuItem;
     @FXML private CheckMenuItem symmetricMode;
     @FXML private RadioMenuItem tableMode;
     @FXML private RadioMenuItem canvasMode;
     @FXML private Button newButton;
     @FXML private Button openButton;
     @FXML private Button saveButton;
     @FXML private Button solveButton;
     @FXML private Button titleButton;
     @FXML private Text startText;
 
     /* Internationalized strings */
     private String blockTransition;
     private String allowTransition;
     private String rename;
     private String renameTitle;
     private String renameMessage;
     private String numberTitle;
     private String numberMessage;
     private String maxRandTitle;
     private String maxRandMessage;
     private String infoTitle;
     private String infoNumberMessage;
     private String infoRandomMessage;
     private String infoSolutionMessage;
     private String errorTitle;
     private String errorFileMessage;
     private String routeShortest;
     private String routeNode;
     private String routeCost;
 
     /** Localization resource bundle */
     private ResourceBundle bundle;
     /** File chooser */
     private FileChooser chooser;
     /** Service object for solving */
     private SolverService service;
     /** The Matrix */
     private Matrix matrix;
     /** Node names*/
     private Map<Integer, StringProperty> names;
     /** Maximum value for random table fill */
     private int maxRandomValue = 10;
     /** Current number of nodes in canvas mode */
     private int nodeCounter = 0;
     /* Window resizing utilities */
     private boolean maximized;
     private Rectangle2D backupWindowBounds;
     private double mouseDragOffsetX = 0;
     private double mouseDragOffsetY = 0;
 
     /**
      * Opens a file dialog, that allows
      * to choose a .txt or .tsp file.
      * Creates a table based on the file data.
      *
      * @param actionEvent action event
      * @throws FileNotFoundException if file not found
      */
     @FXML private void openFile(ActionEvent actionEvent)
             throws FileNotFoundException {
         // Choose the file
         File file = chooser.showOpenDialog(table.getScene().getWindow());
         if (file != null) { // If chosen
             String fileName = file.getName(); // Get extension next
             switch (fileName.substring(fileName.lastIndexOf("."))) {
                 case ".txt": // Read txt file
                     Scanner inp = new Scanner(file);
                     try{
                         matrix = new Matrix(inp);
                     } catch (Exception e) {
                         showMessage(errorTitle, errorFileMessage);
                         return;
                     }
                     break;
                 case ".tsp": // De-serialize tsp file
                     try {
                         ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                         matrix = (Matrix) ois.readObject();
                     } catch (Exception e) {
                         showMessage(errorTitle, errorFileMessage);
                         return;
                     }
                     break;
                 default:
                     showMessage(errorTitle, errorFileMessage);
                     return;
             }
             // Switch to table mode
             tableModeOn(new ActionEvent());
             int number = matrix.getSize();
             // Clear the grid and the map
             table.getChildren().clear();
             names = new HashMap<>();
             // Populate the grid and the map
             for (int i = 0; i < number; ++ i) {
                 names.put(i, new SimpleStringProperty(String.valueOf(i + 1)));
             }
             attachHeadings(number + 1);
             for (Cell cell : matrix) {
                 attachCell(cell);
             }
             // Set flags
             saveMenuItem.setDisable(false);
             saveButton.setDisable(false);
             solveButton.setDisable(false);
             solveMenuItem.setDisable(false);
             randomMenuItem.setDisable(false);
             symmetricMode.setDisable(false);
             tableMode.setSelected(true);
         }
     }
 
     /**
      * Creates a new table with size,
      * questioned in pop-up dialog.
      *
      * @param actionEvent event
      */
     @FXML private void createNew(ActionEvent actionEvent) {
         // Prompt about number
         Prompt prompt = new Prompt((Stage) table.getScene().getWindow(),
                 numberTitle, numberMessage);
         int number;
         if (prompt.show()) { // If got number
             try {
                 number = Integer.parseInt(Prompt.result);
                 if (number < 3 || number > 20) {
                     showMessage(infoTitle, infoNumberMessage);
                     return;
                 }
             } catch (NumberFormatException nfe) {
                 showMessage(infoTitle, infoNumberMessage);
                 return;
             }
         } else {
             return;
         }
         // Switch to table mode
         tableModeOn(new ActionEvent());
         // Clear the grid and the map
         table.getChildren().clear();
         names = new HashMap<>();
         // Populate the grid and the map
         for (int i = 0; i < number; ++ i) {
             names.put(i, new SimpleStringProperty(String.valueOf(i + 1)));
         }
         attachHeadings(number + 1);
         for (int i = 0; i < number; ++ i) {
             for (int j = 0; j < number; ++ j) {
                 Cell cell = new Cell(i == j ? -1 : 0, i, j);
                 attachCell(cell);
             }
         }
         // Set flags
         saveMenuItem.setDisable(false);
         saveButton.setDisable(false);
         solveButton.setDisable(false);
         solveMenuItem.setDisable(false);
         randomMenuItem.setDisable(false);
         symmetricMode.setDisable(false);
         tableMode.setSelected(true);
     }
 
     /**
      * Saves matrix in a chosen file .txt as text
      * or in .tsp file as serialized matrix object.
      *
      * @param actionEvent event
      */
     @FXML private void saveFile(ActionEvent actionEvent) {
         // Show save dialog
         File file = chooser.showSaveDialog(table.getScene().getWindow());
         if (file != null) { // If chosen
             List<Cell> cells = extractCells(); // Extract cells from table
             if (cells.size() < 9) {
                 showMessage(infoTitle, infoNumberMessage);
                 return;
             }
             matrix = new Matrix(cells); // Create matrix
             String fileName = file.getName(); // Get extension
             switch (fileName.substring(fileName.lastIndexOf("."))) {
                 case ".txt": // Save as text
                     try {
                         FileOutputStream fos = new FileOutputStream(file);
                         PrintStream ps = new PrintStream(fos);
                         ps.print(matrix);
                         ps.close();
                     } catch (Exception e) {
                         showMessage(errorTitle, errorFileMessage);
                     }
                     break;
                 case ".tsp": // Save as serialized object
                     try {
                         ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
                         oos.writeObject(matrix);
                         oos.close();
                     } catch (IOException e) {
                         showMessage(errorTitle, errorFileMessage);
                     }
                     break;
                 default:
                     showMessage(errorTitle, errorFileMessage);
             }
         }
     }
 
     /**
      * Switch table symmetric mode.
      * The table becomes symmetric over the main diagonal.
      *
      * @param actionEvent event
      */
     @FXML private void useSymmetricTable(ActionEvent actionEvent) {
         // Collect all text fields from table
         ObservableList<Node> nodes = table.getChildren();
         int size = GridPane.getColumnIndex(nodes.get(nodes.size() - 1));
         TextField[][] textFields = new TextField[size][size];
         int k = 2 * size + 1;
         for (int i = 0; i < size; ++ i) {
             for (int j = 0; j < size; ++ j) {
                 textFields[i][j] = (TextField) nodes.get(k++);
             }
         }
         if (symmetricMode.isSelected()) { // Switch symmetric mode on
             for (int i = 0; i < size; ++ i) {
                 for (int j = i + 1; j < size; ++ j) {
                     textFields[j][i].textProperty().bindBidirectional(textFields[i][j].textProperty());
                 }
             }
         } else { // Switch symmetric mode off
             for (int i = 0; i < size; ++ i) {
                 for (int j = i + 1; j < size; ++ j) {
                     textFields[j][i].textProperty().unbindBidirectional(textFields[i][j].textProperty());
                 }
             }
         }
     }
 
     /**
      * Solves the problem, first
      * chooses which mode is on,
      * whether canvas or table.
      */
     @FXML private void solve() {
         switchControls(true);
         if (canvasMode.isSelected()) {
             solveCanvas();
         } else {
             solveTable();
         }
     }
 
     /**
      * Aborts solving.
      *
      * @param actionEvent click event
      */
     @FXML private void abort(ActionEvent actionEvent) {
         service.cancel();
     }
 
     /**
      * Turns on the table mode.
      *
      * @param actionEvent click event
      */
     @FXML private void tableModeOn(ActionEvent actionEvent) {
         if (tableTab.isDisabled()) {
             // Clear the canvas
             canvas.getChildren().clear();
             names = new HashMap<>();
             // Set selections and disable buttons
             solveButton.setDisable(true);
             solveMenuItem.setDisable(true);
             saveMenuItem.setDisable(true);
             saveButton.setDisable(true);
             tableTab.setDisable(false);
             tabPane.getSelectionModel().select(tableTab);
         }
     }
 
     /**
      * Turns on the canvas mode.
      *
      * @param actionEvent click event
      */
     @FXML private void canvasModeOn(ActionEvent actionEvent) {
         if (! tableTab.isDisabled()) {
             // Clear the canvas, the table and the names map
             canvas.getChildren().clear();
             table.getChildren().clear();
             names = new HashMap<>();
             nodeCounter = 0;
             // Set selections and disable / enable buttons
             tabPane.getSelectionModel().select(canvasTab);
             tableTab.setDisable(true);
             solveButton.setDisable(false);
             solveMenuItem.setDisable(false);
             saveMenuItem.setDisable(false);
             saveButton.setDisable(false);
             randomMenuItem.setDisable(true);
             symmetricMode.setDisable(true);
         }
     }
 
     /**
      * Clears the canvas.
      *
      * @param actionEvent click event
      */
     @FXML private void clearCanvas(ActionEvent actionEvent) {
         canvas.getChildren().clear();
        nodeCounter = 0;
        names.clear();
     }
 
     /**
      * Clears the canvas.
      *
      * @param actionEvent click event
      */
     @FXML private void clearResult(ActionEvent actionEvent) {
        result.clear();
     }
 
     /**
      * Adds node to the canvas.
      * Node is added in MouseEvent coordinates.
      *
      * @param mouseEvent click event
      */
     @FXML private void addNode(MouseEvent mouseEvent) {
         if (mouseEvent.getTarget().equals(canvas) && canvasMode.isSelected()) {
             double fontSize = canvas.getHeight() / 25;
             if (canvas.getWidth() < canvas.getHeight()) {
                 fontSize = canvas.getWidth() / 25;
             }
             double x = mouseEvent.getX();
             double y = mouseEvent.getY();
             names.put(nodeCounter, new SimpleStringProperty(String.valueOf(nodeCounter + 1)));
             Circle circle = createCircle(nodeCounter, x, y);
             Text text = createText(nodeCounter, fontSize, 0, circle);
             ++ nodeCounter;
             canvas.getChildren().addAll(circle, text);
         }
     }
 
     /**
      * Initializes controller.
      * @param url url
      * @param resourceBundle resource bundle
      */
     @Override
     public void initialize(URL url, ResourceBundle resourceBundle) {
         updateLanguage(Locale.ENGLISH);
         chooser = new FileChooser();
         chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text", "*.txt"),
                 new FileChooser.ExtensionFilter("Travelling Salesman Project", "*.tsp"));
         saveMenuItem.setDisable(true);
         saveButton.setDisable(true);
         solveButton.setDisable(true);
         solveMenuItem.setDisable(true);
         randomMenuItem.setDisable(true);
         symmetricMode.setDisable(true);
         tableMode.setSelected(true);
         abortMenuItem.setDisable(true);
         ToggleGroup toggleGroup = new ToggleGroup();
         tableMode.setToggleGroup(toggleGroup);
         canvasMode.setToggleGroup(toggleGroup);
         canvasTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
             @Override
             public void changed(ObservableValue<? extends Boolean> observableValue,
                                 Boolean aBoolean, Boolean aBoolean2) {
 
             }
         });
     }
 
     /**
      * Exits application.
      *
      * @param actionEvent click event
      */
     @FXML private void exit(ActionEvent actionEvent) {
         Platform.exit();
     }
 
     /**
      * Sets new maximum value for table random fill.
      * Value is got through a popup dialog.
      *
      * @param actionEvent click event
      */
     @FXML private void setMaxRandomValue(ActionEvent actionEvent) {
         Prompt prompt = new Prompt((Stage) table.getScene().getWindow(),
                 maxRandTitle, maxRandMessage);
         if (prompt.show()) { // If chosen
             try {
                 int value = Integer.parseInt(Prompt.result);
                 if (value <= 0) {
                     showMessage(infoTitle, infoRandomMessage);
                 } else {
                     maxRandomValue = value;
                 }
             } catch (NumberFormatException nfe) {
                 showMessage(infoTitle, infoRandomMessage);
             }
         }
     }
 
     /**
      * Fills the table with random numbers.
      *
      * @param actionEvent event
      */
     @FXML private void randomFill(ActionEvent actionEvent) {
         Random random = new Random();
         ObservableList<Node> nodes = table.getChildren();
         int size = GridPane.getColumnIndex(nodes.get(nodes.size() - 1));
         for (int i = 2 * size + 1; i < nodes.size(); ++ i) {
             TextField textField = (TextField) nodes.get(i);
             if (! GridPane.getRowIndex(textField).equals(GridPane.getColumnIndex(textField))) {
                textField.setText(String.valueOf(random.nextInt(maxRandomValue + 1)));
             }
         }
     }
 
     /**
      * Attaches a cell to the grid pane.
      * On the grid pane cell represented as
      * a text field. Besides, it has context menu
      * for blocking or allowing the transition.
      *
      * @param cell cell to attach
      * @see com.galaev.tsp.model.Cell
      */
     private void attachCell(Cell cell) {
         // Cell parameters
         int from = cell.getFrom();
         int to = cell.getTo();
         int value = cell.getValue();
         // Create a cell for the grid pane:
         final TextField textCell = TextFieldBuilder.create()
                 .text(value == -1 ? INFINITY_SIGN : String.valueOf(value))
                 .maxHeight(Double.MAX_VALUE)
                 .maxWidth(Double.MAX_VALUE)
                 .editable(from != to)
                 .build();
         GridPane.setHgrow(textCell, Priority.ALWAYS);
         GridPane.setVgrow(textCell, Priority.ALWAYS);
         // Disabling the default context menu:
         final EventDispatcher initial = textCell.getEventDispatcher();
         textCell.setEventDispatcher(new EventDispatcher() {
             @Override
             public Event dispatchEvent(Event event, EventDispatchChain eventDispatchChain) {
                 if (event instanceof MouseEvent) {
                     MouseEvent mouseEvent = (MouseEvent)event;
                     if (mouseEvent.getButton() == MouseButton.SECONDARY ||
                             (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.isControlDown())) {
                         event.consume();
                     }
                 }
                 return initial.dispatchEvent(event, eventDispatchChain);
             }
         });
         // If it's not a diagonal cell, add custom context menu
         if (from != to) {
             // Context menu item for blocking or allowing transition
             final MenuItem menuItem = new MenuItem(blockTransition);
             menuItem.setOnAction(new EventHandler<ActionEvent>() {
                 @FXML private boolean blocked = false;
                 int value;
                 @Override
                 public void handle(ActionEvent actionEvent) {
                     if (blocked) {
                         textCell.setText(String.valueOf(value));
                         menuItem.setText(blockTransition);
                         blocked = false;
                     } else {
                         value = Integer.parseInt(textCell.getText());
                         textCell.setText(INFINITY_SIGN);
                         menuItem.setText(allowTransition);
                         blocked = true;
                     }
                 }
             });
             // Creating and setting the context menu
             final ContextMenu contextMenu = new ContextMenu();
             contextMenu.getItems().add(menuItem);
             textCell.setContextMenu(contextMenu);
         }
         textCell.textProperty().addListener(new ChangeListener<String>() {
             @Override
             public void changed(ObservableValue<? extends String> observable,
                                 String oldValue, String newValue) {
                 try {
                     if (! newValue.equals(INFINITY_SIGN)){
                        int value = Integer.parseInt(newValue);
                        if (value < 0) {
                            throw new NumberFormatException();
                        }
                     }
                 } catch (NumberFormatException e) {
                     textCell.setText(oldValue);
                 }
             }
         });
         // Attach the cell to the grid pane
         table.add(textCell, to + 1, from + 1);
     }
 
     /**
      * Attaches headings to the grid pane.
      * On the grid pane headings represented as
      * labels. Besides, it has context menu
      * for renaming the node.
      *
      * @see com.galaev.tsp.model.Cell
      */
     private void attachHeadings(int size) {
         Label corner = LabelBuilder.create()
                 .text("T")
                 .minWidth(20)
                 .maxWidth(Double.POSITIVE_INFINITY)
                 .alignment(Pos.CENTER)
                 .build();
         table.add(corner, 0, 0);
         GridPane.setHgrow(corner, Priority.ALWAYS);
         for (int i = 1; i < size; ++ i) {
             final int nodeNumber = i;
             LabelBuilder labelBuilder  = LabelBuilder.create()
                     .alignment(Pos.CENTER)
                     .maxWidth(Double.POSITIVE_INFINITY)
                     .minWidth(20)
                     .contextMenu(ContextMenuBuilder.create()
                             .items(MenuItemBuilder.create()
                                     .text(rename)
                                     .onAction(new EventHandler<ActionEvent>() {
                                         @Override
                                         public void handle(ActionEvent actionEvent) {
                                             Prompt prompt = new Prompt((Stage) table.getScene().getWindow(),
                                                     renameTitle, renameMessage);
                                             if (prompt.show()) {
                                                 String newName = Prompt.result;
                                                 names.get(nodeNumber - 1).set(newName);
                                             }
                                         }
                                     })
                                     .build())
                             .build());
             final Label label1 = labelBuilder.build();
             final Label label2 = labelBuilder.build();
             label1.textProperty().bind(names.get(nodeNumber - 1));
             label2.textProperty().bind(names.get(nodeNumber - 1));
             table.add(label1, i, 0);
             table.add(label2, 0, i);
             GridPane.setHgrow(label1, Priority.ALWAYS);
             GridPane.setVgrow(label1, Priority.ALWAYS);
             GridPane.setHgrow(label2, Priority.ALWAYS);
             GridPane.setVgrow(label2, Priority.ALWAYS);
         }
 
     }
 
     /**
      * Solves Travelling Salesman Problem
      * in canvas mode. Reads distances between
      * nodes on canvas and creates matrix out of it.
      */
     private void solveCanvas() {
         // Circles and titles on canvas
         final List<Circle> circles = new ArrayList<>();
         final List<Text> titles = new ArrayList<>();
         // Read them
         for (Node node : canvas.getChildren()) {
             if (node instanceof Circle) {
                 circles.add((Circle) node);
             }
             if (node instanceof Text) {
                 titles.add((Text) node);
             }
         }
         final int size = circles.size();
         if (size < 3 || size > 20) {
             showMessage(infoTitle, infoNumberMessage);
             switchControls(false);
             return;
         }
         // Temp matrix as jagged array
         int[][] tempMatrix = new int[size][size];
         for (int i = 0; i < size; ++ i) {
             for (int j = i; j < size; ++ j) {
                 if (i == j) {
                     tempMatrix[i][j] = -1;
                 } else {
                     tempMatrix[i][j] = distance(circles.get(i), circles.get(j));
                     tempMatrix[j][i] = tempMatrix[i][j];
                 }
             }
         }
         // Create matrix and service
         matrix = new Matrix(tempMatrix);
         service = new SolverService();
         service.setMatrix(matrix);
         // Set action on the end of solving
         service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
             @Override
             public void handle(WorkerStateEvent event) {
                 Route route = (Route) event.getSource().getValue();
                 if (route == null) {
                     showMessage(infoTitle, infoSolutionMessage);
                     switchControls(false);
                     return;
                 }
                 printRouteInfo(route);
                 Line[] lines = new Line[size];
                 createLines(lines, circles.toArray(new Circle[circles.size()]), route);
                 canvas.getChildren().clear();
                 canvas.getChildren().addAll(lines);
                 canvas.getChildren().addAll(circles);
                 canvas.getChildren().addAll(titles);
                 switchControls(false);
             }
         });
         // On abort
         service.setOnCancelled(new EventHandler<WorkerStateEvent>() {
             @Override
             public void handle(WorkerStateEvent workerStateEvent) {
                 switchControls(false);
             }
         });
         // Start solving
         service.start();
     }
 
     /**
      * Solves Travelling Salesman Problem
      * in table mode. Reads distances from
      * the table and creates matrix out of it.
      */
     private void solveTable() {
         // Read cells out of the table
         List<Cell> cells = extractCells();
         if (cells.size() < 9 || cells.size() > 400) {
             showMessage(infoTitle, infoNumberMessage);
             switchControls(false);
             return;
         }
         tabPane.getSelectionModel().select(canvasTab);
         // Create matrix and service
         matrix = new Matrix(cells);
         service = new SolverService();
         service.setMatrix(matrix);
         // Set action on the end of solving
         service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
             @Override
             public void handle(WorkerStateEvent event) {
                 Route route = (Route) event.getSource().getValue();
                 if (route == null) {
                     showMessage(infoTitle, infoSolutionMessage);
                     switchControls(false);
                     return;
                 }
                 printRouteInfo(route);
                 drawRoute(route);
                 switchControls(false);
             }
         });
         // On abort
         service.setOnCancelled(new EventHandler<WorkerStateEvent>() {
             @Override
             public void handle(WorkerStateEvent workerStateEvent) {
                  switchControls(false);
             }
         });
         // Start solving
         service.start();
     }
 
     /**
      * Extracts all values from the table as
      * a list of Cell's objects.
      *
      * @return list of cells
      */
     private List<Cell> extractCells() {
         List<Cell> cells = new ArrayList<>();
         ObservableList<Node> nodes = table.getChildren();
         int size = GridPane.getColumnIndex(nodes.get(nodes.size() - 1));
         for (int i = 2 * size + 1; i < nodes.size(); ++ i) {
             TextField textField = (TextField) nodes.get(i);
             int from = GridPane.getRowIndex(textField) - 1;
             int to = GridPane.getColumnIndex(textField) - 1;
             int value = textField.getText().equals(INFINITY_SIGN) ? -1 : Integer.parseInt(textField.getText());
             cells.add(new Cell(value, from, to));
         }
         return cells;
     }
 
     /**
      * Draws the final route on canvas.
      *
      * @param route route to draw
      */
     private void drawRoute(Route route) {
         // Clear the "canvas"
         canvas.getChildren().clear();
         // Set all the drawing parameters
         int n = route.getRoute().size() - 1;
         double alpha = 2 * Math.PI / n;
         double x = canvas.getWidth() / 2;
         double y = canvas.getHeight() / 2;
         double rad = canvas.getHeight() / 3;
         double fontSize = canvas.getHeight() / 25;
         if (canvas.getWidth() < canvas.getHeight()) {
             rad = canvas.getWidth() / 3;
             fontSize = canvas.getWidth() / 25;
         }
         Circle[] circles = new Circle[n];
         Line[] lines = new Line[n];
         Text[] titles = new Text[n];
         // Circles and text creation
         for (int i = 0; i < n; ++ i) {
             // Create a circle
             circles[i] = createCircle(i, x + rad * Math.sin(alpha * i), y - rad * Math.cos(alpha * i)
             );
             // Create circle's text
             titles[i] = createText(i, fontSize, alpha, circles[i]);
         }
         // Lines creation
         createLines(lines, circles, route);
         // Add everything to the canvas
         canvas.getChildren().addAll(lines);
         canvas.getChildren().addAll(circles);
         canvas.getChildren().addAll(titles);
     }
 
     /**
      * Creates lines between nodes,
      * according to the route.
      *
      * @param lines lines between nodes
      * @param circles nodes
      * @param route route
      */
     private void createLines(Line[] lines, Circle[] circles, Route route) {
         int n = route.getRoute().size() - 1;
         for (int i = 0; i < n; ++ i) {
             int node1 = route.getRoute().get(i);
             int node2 = route.getRoute().get(i + 1);
             // Create line
             lines[i] = createLine(circles[node1], circles[node2]);
             // Circle animation:
             FillTransition fill = FillTransitionBuilder.create()
                     .duration(Duration.seconds(ANIMATION_DELAY))
                     .shape(circles[node1])
                     .fromValue(Color.WHITE)
                     .toValue(Color.BLUE)
                     .delay(Duration.seconds(i * ANIMATION_DELAY))
                     .build();
             fill.play();
             // Lines animation:
             StrokeTransition stroke = StrokeTransitionBuilder.create()
                     .shape(lines[i])
                     .duration(Duration.seconds(ANIMATION_DELAY))
                     .fromValue(Color.BLUE)
                     .toValue(Color.RED)
                     .delay(Duration.seconds(i * ANIMATION_DELAY))
                     .build();
             FadeTransition fade = FadeTransitionBuilder.create()
                     .node(lines[i])
                     .duration(Duration.seconds(ANIMATION_DELAY))
                     .fromValue(0)
                     .toValue(1)
                     .delay(Duration.seconds(i * ANIMATION_DELAY))
                     .build();
             stroke.play();
             fade.play();
         }
     }
 
     /**
      * Creates a circle, that represents a node.
      *
      * @param i node number
      * @param x node x coordinate
      * @param y node y coordinate
      * @return created circle
      */
     private Circle createCircle(final int i, double x, double y) {
         // Create circle
         final Circle circle = new Circle(x, y, 10);
         circle.getStyleClass().add("circle");
         circle.setEffect(new Lighting());
         circle.setOnMouseDragged(new EventHandler<MouseEvent>() {
             @Override
             public void handle(MouseEvent mouseEvent) {
                 circle.setCenterX(mouseEvent.getX());
                 circle.setCenterY(mouseEvent.getY());
             }
         });
         // Set context menu for renaming
         circle.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
             @Override
             public void handle(ContextMenuEvent contextMenuEvent) {
                 ContextMenu contextMenu = new ContextMenu();
                 contextMenu.getItems().add(MenuItemBuilder.create()
                         .text(rename)
                         .onAction(new EventHandler<ActionEvent>() {
                             @Override
                             public void handle(ActionEvent actionEvent) {
                                 Prompt prompt = new Prompt((Stage) table.getScene().getWindow(),
                                         renameTitle, renameMessage);
                                 if (prompt.show()) {
                                     String newName = Prompt.result;
                                     names.get(i).set(newName);
                                 }
                             }
                         })
                         .build());
                 contextMenu.show(circle, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
             }
         });
         // Set radius
         circle.radiusProperty().bind(Bindings.min(
                 canvas.heightProperty().divide(25),
                 canvas.widthProperty().divide(25)));
         return circle;
     }
 
     /**
      * Creates text around a circle (for a node).
      *
      * @param i node number
      * @param size size of matrix
      * @param alpha angle
      * @param circle text's circle
      * @return created text
      */
     private Text createText(int i, final double size, double alpha, Circle circle) {
         final String name = names.get(i).get();
         VPos position = VPos.BOTTOM;
         final DoubleProperty offsetX = new SimpleDoubleProperty();
         final DoubleProperty offsetY = new SimpleDoubleProperty();
         final double beta = alpha * i;
         // Set appropriate offsets
         if (beta >= Math.PI / 2 && beta < 3 * Math.PI / 2)  {
             position = VPos.TOP;
         }
         setOffsets(beta, size, name, offsetX, offsetY);
         // Create text
         final Text text= TextBuilder.create()
                 .id("mytext")
                 .textOrigin(position)
                 .font(Font.font("Times New Roman", size))
                 .build();
         // Bind names
         text.textProperty().bind(names.get(i));
         // Bind position
         text.xProperty().bind(Bindings.add(circle.centerXProperty(), offsetX));
         text.yProperty().bind(Bindings.add(circle.centerYProperty(), offsetY));
         // Change position on changes
         canvas.heightProperty().addListener(new ChangeListener<Number>() {
             @Override
             public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                 double size = number2.doubleValue() / 25;
                 text.setFont(Font.font("Times New Roman", size));
                 setOffsets(beta, size, name, offsetX, offsetY);
             }
         });
         text.textProperty().addListener(new ChangeListener<String>() {
             @Override
             public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                 setOffsets(beta, size, text.getText(), offsetX, offsetY);
             }
         });
         return text;
     }
 
     /**
      * Sets offsets for a Text.
      *
      * @param beta angle
      * @param size size of matrix
      * @param name node name
      * @param offsetX x-offset
      * @param offsetY y-offset
      */
     private void setOffsets(double beta, double size, String name, DoubleProperty offsetX, DoubleProperty offsetY) {
         // Count offsets
         if (beta < Math.PI / 4) {
             offsetY.set(- size);
         } else if (beta < Math.PI / 2) {
             offsetY.set(- size);
         } else if (beta < 3 * Math.PI / 4) {
             offsetY.set(size);
         } else if (beta < Math.PI) {
             offsetY.set(size);
         } else if (beta < 5 * Math.PI / 4) {
             offsetY.set(size);
             offsetX.set(- size * name.length() / 2);
         } else if (beta < 3 * Math.PI / 2) {
             offsetY.set(size);
             offsetX.set( - size * name.length() / 2);
         } else if (beta < 7 * Math.PI / 4) {
             offsetY.set(- size);
             offsetX.set(- size * name.length() / 2);
         } else {
             offsetY.set(- size);
             offsetX.set(- size * name.length() / 2);
         }
     }
 
     /**
      * Creates a line between two circles.
      *
      * @param c1 first circle
      * @param c2 second circle
      * @return created line
      */
     private Line createLine(Circle c1, Circle c2) {
         // Build line
         Line line = LineBuilder.create()
                 .styleClass("line")
                 .effect(new Lighting())
                 .build();
         // Bind lines to circles
         line.startXProperty().bind(c1.centerXProperty());
         line.startYProperty().bind(c1.centerYProperty());
         line.endXProperty().bind(c2.centerXProperty());
         line.endYProperty().bind(c2.centerYProperty());
         line.strokeWidthProperty().bind(Bindings.min(
                 canvas.heightProperty().divide(50),
                 canvas.widthProperty().divide(50)));
         return line;
     }
 
     /**
      * Shows popup message.
      * @param title title of the message
      * @param message the message itself
      */
     private void showMessage(String title, String message) {
          new Message((Stage) table.getScene().getWindow(), title, message);
     }
 
     /**
      * Prints to result a string representation of the route.
      * That is route cost and the description of the route.
      *
      * @param route route to be printed
      */
     private void printRouteInfo(Route route) {
         StringBuilder resultBuilder = new StringBuilder();
         resultBuilder.append(routeCost).append(" ").append(route.getCost());
         resultBuilder.append("\n").append(routeShortest).append("\n");
         List<Integer> routeList = route.getRoute();
         for (int i = 0; i < routeList.size(); ++ i) {
             resultBuilder.append(routeNode).append(" ")
                          .append(names.get(routeList.get(i)).get());
             if (i != routeList.size() - 1) {
                 resultBuilder.append(" >>");
             }
             resultBuilder.append("\n");
         }
         result.appendText(resultBuilder.toString());
 
     }
 
     /**
      * Switches all controls.
      * Disables on solving start.
      * Enables on solving end.
      *
      * @param value disable or enable
      */
     private void switchControls(boolean value) {
         saveMenuItem.setDisable(value);
         randomMenuItem.setDisable(value);
         newMenuItem.setDisable(value);
         openMenuItem.setDisable(value);
         clearMenuItem.setDisable(value);
         clearResMenuItem.setDisable(value);
         solveMenuItem.setDisable(value);
         maxRandomMenuItem.setDisable(value);
         symmetricMode.setDisable(value);
         tableMode.setDisable(value);
         canvasMode.setDisable(value);
         newButton.setDisable(value);
         openButton.setDisable(value);
         saveButton.setDisable(value);
         solveButton.setDisable(value);
         abortMenuItem.setDisable(! value);
     }
 
     /**
      * Sets english language.
      *
      * @param actionEvent event
      */
     @FXML private void setEnglish(ActionEvent actionEvent) {
         updateLanguage(Locale.ENGLISH);
     }
 
     /**
      * Sets russian language.
      *
      * @param actionEvent event
      */
     @FXML private void setRussian(ActionEvent actionEvent) {
         updateLanguage(new Locale("ru"));
     }
 
     /**
      * Sets german language.
      *
      * @param actionEvent event
      */
     @FXML private void setDeutsch(ActionEvent actionEvent) {
         updateLanguage(Locale.GERMAN);
     }
 
     /**
      * Applies given localization to the application.
      * All l10ns are contained in resource bundle Bundle.
      *
      * @param locale localization
      */
     private void updateLanguage(Locale locale) {
         bundle = ResourceBundle.getBundle("com.galaev.tsp.gui.resources.bundles.Bundle", locale);
         titleButton.setText(utfProperty("title"));
         canvasTab.setText(utfProperty("canvas"));
         tableTab.setText(utfProperty("table"));
         resultTab.setText(utfProperty("result"));
         fileMenu.setText(utfProperty("file"));
         editMenu.setText(utfProperty("edit"));
         settingsMenu.setText(utfProperty("settings"));
         solveMenu.setText(utfProperty("solve"));
         newMenuItem.setText(utfProperty("file.new"));
         openMenuItem.setText(utfProperty("file.open"));
         saveMenuItem.setText(utfProperty("file.save"));
         exitMenuItem.setText(utfProperty("file.exit"));
         symmetricMode.setText(utfProperty("edit.symmetric"));
         randomMenuItem.setText(utfProperty("edit.random"));
         clearMenuItem.setText(utfProperty("edit.clear"));
         clearResMenuItem.setText(utfProperty("edit.clear.result"));
         solveMenuItem.setText(utfProperty("solve.solve"));
         abortMenuItem.setText(utfProperty("solve.abort"));
         inputModeMenu.setText(utfProperty("settings.input"));
         tableMode.setText(utfProperty("settings.input.table"));
         canvasMode.setText(utfProperty("settings.input.canvas"));
         maxRandomMenuItem.setText(utfProperty("settings.maxrandom"));
         languageMenu.setText(utfProperty("settings.language"));
         startText.setText(utfProperty("start.text"));
         blockTransition = utfProperty("block");
         allowTransition = utfProperty("allow");
         rename = utfProperty("rename");
         renameTitle = utfProperty("rename.title");
         renameMessage = utfProperty("rename.message");
         numberTitle = utfProperty("number.title");
         numberMessage = utfProperty("number.message");
         maxRandTitle = utfProperty("maxrand.title");
         maxRandMessage = utfProperty("maxrand.message");
         infoTitle = utfProperty("info.title");
         infoNumberMessage = utfProperty("info.number.message");
         infoRandomMessage = utfProperty("info.random.message");
         infoSolutionMessage = utfProperty("info.solution");
         errorTitle = utfProperty("error.title");
         errorFileMessage = utfProperty("error.file.message");
         routeCost = utfProperty("route.cost");
         routeNode = utfProperty("route.node");
         routeShortest = utfProperty("route.shortest");
     }
 
     /**
      * Gets property utf-8 encoded from resource bundle.
      *
      * @param property property name
      * @return property value utf-8 encoded
      */
     private String utfProperty(String property) {
         String value = bundle.getString(property);
         try {
             return new String(value.getBytes("ISO-8859-1"), "UTF-8");
         } catch (UnsupportedEncodingException e) {
             System.out.println("Unsupported Encoding!");
             return value;
         }
     }
 
     /**
      * Counts distance between two circles.
      *
      * @param c1 first circle
      * @param c2 second circle
      * @return distance
      */
     private int distance(Circle c1, Circle c2) {
         double dx2 = (c1.getCenterX() - c2.getCenterX()) * (c1.getCenterX() - c2.getCenterX());
         double dy2 = (c1.getCenterY() - c2.getCenterY()) * (c1.getCenterY() - c2.getCenterY());
         return (int) (Math.sqrt(dx2 + dy2) + 0.5);
     }
 
     /**
      * Minimizes application window.
      *
      * @param actionEvent click event
      */
     @FXML private void minimize(ActionEvent actionEvent) {
         ((Stage) table.getScene().getWindow()).setIconified(true);
     }
 
     /**
      * Maximizes application window.
      *
      * @param event click event
      */
     @FXML private void maximize(Event event) {
         Stage stage = (Stage) table.getScene().getWindow();
         final Screen screen = Screen.getScreensForRectangle(stage.getX(), stage.getY(), 1, 1).get(0);
         if (maximized) {
             maximized = false;
             if (backupWindowBounds != null) {
                 stage.setX(backupWindowBounds.getMinX());
                 stage.setY(backupWindowBounds.getMinY());
                 stage.setWidth(backupWindowBounds.getWidth());
                 stage.setHeight(backupWindowBounds.getHeight());
             }
         } else {
             maximized = true;
             backupWindowBounds = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
             stage.setX(screen.getVisualBounds().getMinX());
             stage.setY(screen.getVisualBounds().getMinY());
             stage.setWidth(screen.getVisualBounds().getWidth());
             stage.setHeight(screen.getVisualBounds().getHeight());
         }
     }
 
     /**
      * Handles mouse click on header.
      *
      * @param event mouse event
      */
     @FXML private void headerClick(MouseEvent event) {
         if (event.getClickCount() == 2) {
             maximize(event);
         }
     }
 
     /**
      * Handles mouse press on header.
      *
      * @param event mouse event
      */
     @FXML private void headerPress(MouseEvent event) {
         mouseDragOffsetX = event.getSceneX();
         mouseDragOffsetY = event.getSceneY();
     }
 
     /**
      * Handles mouse drag on header.
      *
      * @param event mouse event
      */
     @FXML private void headerDrag(MouseEvent event) {
         if (! maximized) {
             Stage stage = (Stage) table.getScene().getWindow();
             stage.setX(event.getScreenX() - mouseDragOffsetX);
             stage.setY(event.getScreenY() - mouseDragOffsetY);
         }
     }
 }
