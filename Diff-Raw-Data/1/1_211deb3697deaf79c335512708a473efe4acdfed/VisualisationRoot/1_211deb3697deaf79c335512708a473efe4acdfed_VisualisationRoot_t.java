 package thesaurus.gui.window;
 
 import java.io.File;
 import java.io.IOException;
 import javafx.fxml.FXMLLoader;
 import javafx.scene.layout.AnchorPane;
 import javafx.stage.Popup;
 import thesaurus.controller.SystemController;
 import thesaurus.gui.canvas.ViewGraph;
 import thesaurus.gui.table.ViewTable;
 import thesaurus.parser.*;
 import thesaurus.spring.FrSpring;
 
 /**
  * This class is an extension of AnchorPane and defines how the visualisation
  * pages looks and what the buttons do via mouse handlers.
  */
 public class VisualisationRoot extends AnchorPane {
 
 	private MainWindow referenceWindow;
 	private SystemController currentController;
 	private InternalRepresentation currentParser;
 	private File currentFile;
 	private Popup currentPopup;
 	private Vertex currentVertex;
 	private ViewGraph displayGraphFull;
 	private ViewGraph displayGraphDual;
 
 	public VisualisationRoot(MainWindow inputWindow) throws IOException {
 
 		referenceWindow = inputWindow;
 
 		currentController = referenceWindow.getCurrentController();
 
 		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/resourcePackage/visualisationLayout.fxml"));
 		fxmlLoader.setRoot(this);
 		fxmlLoader.setController(currentController);
 
 		try {
 			fxmlLoader.load();
 		} catch (IOException exception) {
 			throw new RuntimeException(exception);
 		}
 
 	}
 
 	public void addCanvas() {
 		currentController.getCanvasFullGraph().getChildren().removeAll(currentController.getCanvasFullGraph().getChildren());
 		currentController.getCanvasDualGraph().getChildren().removeAll(currentController.getCanvasDualGraph().getChildren());
 		displayGraphFull = new ViewGraph(757, 375,currentVertex, referenceWindow.getVisualisationRoot(),1,1);
 		displayGraphDual = new ViewGraph(354, 362,currentVertex, referenceWindow.getVisualisationRoot(),1,1);
 		currentController.getCanvasFullGraph().getChildren().add(displayGraphFull.returnGraph());
 		currentController.getCanvasDualGraph().getChildren().add(displayGraphDual.returnGraph());
 	}
 	
 	public void addTable() {
 		currentController.getTableFullGraph().getChildren().removeAll(currentController.getTableFullGraph().getChildren());
 		currentController.getTableDualGraph().getChildren().removeAll(currentController.getTableDualGraph().getChildren());
 		ViewTable displayTableFull = new ViewTable(728, 366,currentVertex, referenceWindow.getVisualisationRoot());
 		ViewTable displayTableDual = new ViewTable(354, 362,currentVertex, referenceWindow.getVisualisationRoot());
 		currentController.getTableFullGraph().getChildren().add(displayTableFull.getTable());
 		currentController.getTableDualGraph().getChildren().add(displayTableDual.getTable());
 	}
 
 	public void setCurrentParser(File inputFile) {
 		setCurrentFile(inputFile);
 		currentParser = new InternalRepresentation(inputFile);
 	}
 
 	public File getCurrentFile() {
 		return currentFile;
 	}
 
 	public void setCurrentFile(File currentFileInput) {
 		currentFile = currentFileInput;
 	}
 
 	public void setCurrentVertex(Vertex currentVertexInput) {
 		currentVertex = currentVertexInput;
 	}
 
 	public InternalRepresentation getCurrentParser() {
 		return currentParser;
 	}
 
 	public void showPopup(String inputChoice) {
 		if(currentPopup != null){
 			currentPopup.hide();
 			currentPopup = null;
 		}
 		PopupFactory currentPopupFactory = new PopupFactory(inputChoice, referenceWindow);
 		currentPopup = currentPopupFactory.getPopup();
 		currentPopup.show(referenceWindow.getStage());
 		currentPopup.setY(currentPopup.getY()+15);
 	}
 	
 	public Vertex runSpringOnVertex(Vertex inputVertex){
 		FrSpring currentSpring = new FrSpring(inputVertex);
 		return currentSpring.getCoordinates();
 	}
 	
 	public void doClickSearchGraph(String inputString) {
 		setCurrentVertex(getCurrentParser().getOneSynomyn(inputString));
 		if (currentVertex == null) {
 			return;
 		}
 		setCurrentVertex(referenceWindow.getVisualisationRoot().runSpringOnVertex(currentVertex));
 		referenceWindow.getVisualisationRoot().addCanvas();
 		referenceWindow.getVisualisationRoot().addTable();
 		currentController.defaultZoomValue();
 	}
 	
 	public void doSearchRefresh(){
 		Vertex replacementVertex = getCurrentParser().getOneSynomyn(currentVertex.getWord());
 		setCurrentVertex(referenceWindow.getVisualisationRoot().runSpringOnVertex(replacementVertex));
 		currentController.defaultZoomValue();
 	}
 	
 	public Vertex getCurrentVertex(){
 		return currentVertex;
 	}
 	
 	public ViewGraph getFullGraph(){
 		return displayGraphFull;
 	}
 	
 	public ViewGraph getDualGraph(){
 		return displayGraphDual;
 	}
 }
