 package thesaurus.gui.window;
 
 import java.io.IOException;
 import javafx.fxml.FXML;
 import javafx.fxml.FXMLLoader;
 import javafx.scene.control.TabPane;
 import javafx.scene.layout.AnchorPane;
 import javafx.scene.layout.Pane;
 import thesaurus.gui.canvas.*;
 
 public class VisualisationRoot extends AnchorPane {
 
 	private MainWindow referenceWindow;
 	
     @FXML
     private Pane canvasFullGraph;
     
     @FXML
     private Pane canvasDualGraph;
 
     @FXML
     private TabPane mainTabWindow;
     
 	public VisualisationRoot(MainWindow inputWindow) {
		
		super();
 
 		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("visualisationLayout.fxml"));
 		fxmlLoader.setRoot(this);
 		fxmlLoader.setController(this);
 
 		try {
 			fxmlLoader.load();
 		} catch (IOException exception) {
 			throw new RuntimeException(exception);
 		}
 		
 		referenceWindow = inputWindow;
 		
 		addCanvas();
 		
 	}
 	
 	@FXML
 	protected void doReturn() {
 		referenceWindow.getStage().setScene(referenceWindow.getSplashScene());
 	}
 	
 	private void addCanvas(){
 		ViewGraph displayGraphFull = new ViewGraph(700,316);
 		ViewGraph displayGraphDual = new ViewGraph(334, 290);
 		canvasFullGraph.getChildren().add(displayGraphFull.returnGraph());
 		canvasDualGraph.getChildren().add(displayGraphDual.returnGraph());
 	}
 
 }
