 package edu.simplex.controller;
 
 import edu.simplex.algorithm.SimplexSolver;
 import javafx.beans.property.ReadOnlyObjectWrapper;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.control.TableColumn;
 import javafx.scene.control.TableView;
 import javafx.stage.FileChooser;
 import javafx.util.Callback;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 import java.io.*;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.util.*;
 
 /**
  * Catalin Dumitru
  * Universitatea Alexandru Ioan Cuza
  */
 public class ApplicationController implements Initializable {
     @FXML
     private Button loadButton;
     @FXML
     private Button nextButton;
     @FXML
     private TableView simplexTableView;
     @FXML
     private TableView cVector;
     @FXML
     private TableView optimalSolutionVector;
     @FXML
     private TableView sMatrix;
     @FXML
     private Label mLabel;
 
     private List<Double[]> table;
     private List<TableColumn> tableColumns;
     private List<TableColumn> cVectorColumns;
    private List<TableColumn> yVectorColumns;
     private List<TableColumn> sMatrixColumns;
    private List<Double[]> sTable;
     private Double m;
 
     @Override
     public void initialize(URL url, ResourceBundle resourceBundle) {
         m = (new Random().nextDouble() / 2 + 0.5) + 100000000000D;
         bindControls();
     }
 
     private void bindControls() {
         loadButton.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent actionEvent) {
                 loadTable();
             }
         });
         nextButton.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent actionEvent) {
                 nextIteration();
             }
         });
 
         mLabel.setText(new DecimalFormat("0.000").format(m));
     }
 
     private void nextIteration() {
         int pivotColumn = new SimplexSolver(table).nextIteration();
         highlightPivotColumn(pivotColumn);
         resetRows();
     }
 
     private void highlightPivotColumn(int pivotColumn) {
         for (int i = 0; i < tableColumns.size(); i++) {
             if (i == pivotColumn) {
                 tableColumns.get(i).setStyle("-fx-background-color:linear-gradient(#77AA77,#88AA88);");
             } else {
                 tableColumns.get(i).setStyle("-fx-background-color:linear-gradient(white,#DDDDDD);");
             }
         }
     }
 
     private void loadTable() {
         FileChooser fileChooser = new FileChooser();
         File chosenFile = fileChooser.showOpenDialog(null);
 
         if (chosenFile != null) {
             try {
                 loadTableFromFile(chosenFile);
                 bindTableData();
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
             }
         }
     }
 
     private void bindTableData() {
         resetColumns();
         resetRows();
     }
 
     private void resetColumns() {
         tableColumns = createColumns();
         cVectorColumns = createCVectorColumns();
         sMatrixColumns = createSMatrixColumns();
         yVectorColumns = createYVectorColumns();
         simplexTableView.getColumns().addAll(tableColumns);
         cVector.getColumns().addAll(cVectorColumns);
         optimalSolutionVector.getColumns().addAll(yVectorColumns);
         sMatrix.getColumns().addAll(sMatrixColumns);
     }
 
     private void resetRows() {
         List<SimplexRow> tableRows = createRows();
         List<SimplexRow> cRows = createCVectorRows();
         List<SimplexRow> sMatrixRows = createSMatrixRows();
         List<SimplexRow> yRows = createYVectorRows();
         simplexTableView.setItems(FXCollections.observableArrayList(tableRows));
         cVector.setItems(FXCollections.observableArrayList(cRows));
         sMatrix.setItems(FXCollections.observableArrayList(sMatrixRows));
         optimalSolutionVector.setItems(FXCollections.observableArrayList(yRows));
     }
 
     private List<SimplexRow> createRows() {
         List<SimplexRow> rows = new ArrayList<>();
         for (Double[] values : table) {
             rows.add(new SimplexRow(values));
         }
         return rows;
     }
 
     private List<SimplexRow> createYVectorRows() {
         List<SimplexRow> rows = new ArrayList<>();
         Double[] yResult = multiply(getCVector(), getSMatrix());
         rows.add(new SimplexRow(yResult));
         return rows;
     }
 
     private List<SimplexRow> createCVectorRows() {
         List<SimplexRow> rows = new ArrayList<>();
         rows.add(new SimplexRow(getCVector()));
         return rows;
     }
 
     private Double[] getCVector() {
         Double[] firstRow = table.get(table.size() - 1);
         return Arrays.copyOf(firstRow, firstRow.length - table.size());
     }
 
     private List<SimplexRow> createSMatrixRows() {
         List<SimplexRow> rows = new ArrayList<>();
         Double[][] sMatrix = getSMatrix();
         for (Double[] values : sMatrix) {
             rows.add(new SimplexRow(values));
         }
         return rows;
     }
 
     private Double[][] getSMatrix() {
         List<Double[]> rows = new ArrayList<>();
         for (Double[] values : table) {
             rows.add(Arrays.copyOfRange(values, values.length - table.size(), values.length));
         }
         return rows.toArray(new Double[rows.size()][]);
     }
 
     private List<TableColumn> createColumns() {
         if (table.isEmpty()) {
             return new ArrayList<>();
         }
         final List<TableColumn> columns = new ArrayList<>();
 
         for (int i = 0; i < table.get(0).length; i++) {
             TableColumn<SimplexRow, Double> tableColumn = new TableColumn<>(String.format("X%s", i + 1));
             tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SimplexRow, Double>, ObservableValue<Double>>() {
                 @Override
                 public ObservableValue<Double> call(TableColumn.CellDataFeatures<SimplexRow, Double> data) {
                     Integer tableColumnsIndex = columns.indexOf(data.getTableColumn());
                     return new ReadOnlyObjectWrapper<>(data.getValue().getRowValues()[tableColumnsIndex]);
                 }
             });
             columns.add(tableColumn);
         }
         return columns;
     }
 
     private List<TableColumn> createCVectorColumns() {
         if (table.isEmpty()) {
             return new ArrayList<>();
         }
         final List<TableColumn> columns = new ArrayList<>();
 
         for (int i = 0; i < table.get(0).length - table.size(); i++) {
             TableColumn<SimplexRow, Double> tableColumn = new TableColumn<>(String.format("C%s", i + 1));
             tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SimplexRow, Double>, ObservableValue<Double>>() {
                 @Override
                 public ObservableValue<Double> call(TableColumn.CellDataFeatures<SimplexRow, Double> data) {
                     Integer tableColumnsIndex = columns.indexOf(data.getTableColumn());
                     return new ReadOnlyObjectWrapper<>(data.getValue().getRowValues()[tableColumnsIndex]);
                 }
             });
             columns.add(tableColumn);
         }
         return columns;
     }
 
     private List<TableColumn> createYVectorColumns() {
         if (table.isEmpty()) {
             return new ArrayList<>();
         }
         final List<TableColumn> columns = new ArrayList<>();
 
         for (int i = 0; i < sMatrixColumns.size(); i++) {
             TableColumn<SimplexRow, Double> tableColumn = new TableColumn<>(String.format("Y%s", i + 1));
             tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SimplexRow, Double>, ObservableValue<Double>>() {
                 @Override
                 public ObservableValue<Double> call(TableColumn.CellDataFeatures<SimplexRow, Double> data) {
                     Integer tableColumnsIndex = columns.indexOf(data.getTableColumn());
                     return new ReadOnlyObjectWrapper<>(data.getValue().getRowValues()[tableColumnsIndex]);
                 }
             });
             columns.add(tableColumn);
         }
         return columns;
     }
 
     private List<TableColumn> createSMatrixColumns() {
         if (table.isEmpty()) {
             return new ArrayList<>();
         }
         final List<TableColumn> columns = new ArrayList<>();
 
         for (int i = table.get(0).length - table.size(); i < table.get(0).length; i++) {
             TableColumn<SimplexRow, Double> tableColumn = new TableColumn<>(String.format("S%s", i + 1));
             tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SimplexRow, Double>, ObservableValue<Double>>() {
                 @Override
                 public ObservableValue<Double> call(TableColumn.CellDataFeatures<SimplexRow, Double> data) {
                     Integer tableColumnsIndex = columns.indexOf(data.getTableColumn());
                     return new ReadOnlyObjectWrapper<>(data.getValue().getRowValues()[tableColumnsIndex]);
                 }
             });
             columns.add(tableColumn);
         }
         return columns;
     }
 
     private void loadTableFromFile(File chosenFile) throws FileNotFoundException {
         try (FileReader fileReader = new FileReader(chosenFile);
              BufferedReader reader = new BufferedReader(fileReader)) {
             processFile(reader);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private void processFile(BufferedReader reader) throws IOException {
         String line = null;
         initTable();
 
         while ((line = reader.readLine()) != null) {
             String[] params = line.split("\\s");
             addRow(params);
         }
     }
 
     private void addRow(String[] params) {
         Double[] values = new Double[params.length];
         for (int i = 0; i < params.length; i++) {
             values[i] = getValue(params[i]);
         }
         table.add(values);
     }
 
     private Double getValue(String expression) {
         ScriptEngineManager mgr = new ScriptEngineManager();
         ScriptEngine engine = mgr.getEngineByName("JavaScript");
 
         try {
             return (Double) engine.eval(expression.replaceAll("M", new DecimalFormat("0.000").format(m)));
         } catch (ScriptException e) {
             return null;
         }
     }
 
     private void initTable() {
         table = new ArrayList<>();
     }
 
     public static Double[] multiply(Double[] x, Double[][] A) {
         int m = A.length;
         int n = A[0].length;
         if (x.length != m) throw new RuntimeException("Illegal matrix dimensions.");
         Double[] y = new Double[n];
         Arrays.fill(y, 0D);
         for (int j = 0; j < n; j++)
             for (int i = 0; i < m; i++)
                 y[j] += (A[i][j] * x[i]);
         return y;
     }
 }
