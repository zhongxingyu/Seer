 package thesaurus.gui.window;
 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 
 import thesaurus.gui.window.VisualisationRoot;
 import javafx.fxml.FXML;
 import javafx.fxml.FXMLLoader;
 import javafx.scene.Scene;
 import javafx.scene.layout.AnchorPane;
 import javafx.stage.FileChooser;
 
 public class SplashRoot extends AnchorPane {
 
 	private MainWindow referenceWindow;
 
 	public SplashRoot(MainWindow inputWindow) {
		
		super();
 
 		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("splashLayout.fxml"));
 		fxmlLoader.setRoot(this);
 		fxmlLoader.setController(this);
 		
 		try {
 			fxmlLoader.load();
 		} catch (IOException exception) {
 			throw new RuntimeException(exception);
 		}
 		referenceWindow = inputWindow;
 		
 	}
 
 	private void saveFile(String content, File file) {
 		try {
 			FileWriter fileWriter = null;
 			fileWriter = new FileWriter(file);
 			fileWriter.write(content);
 			fileWriter.close();
 		} catch (IOException ex) {}
 
 	}
 
 	@FXML
 	protected void doCreate() {
 		
 		FileChooser fileChooser = new FileChooser();
 		FileChooser.ExtensionFilter graphmlFilter = new FileChooser.ExtensionFilter("GraphML files (*.graphml)", "*.graphml");
 		FileChooser.ExtensionFilter xmlFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
 		fileChooser.getExtensionFilters().add(graphmlFilter);
 		fileChooser.getExtensionFilters().add(xmlFilter);
 		File file = fileChooser.showSaveDialog(referenceWindow.getStage());
 		if (file != null) {
 			saveFile("test", file);
 		}
 
 		VisualisationRoot visualisationRootCurrent = new VisualisationRoot(referenceWindow);
 		referenceWindow.getStage().setScene(new Scene(visualisationRootCurrent, 800, 600));
 	}
 
 	@FXML
 	protected void doImport() {
 		
 		FileChooser fileChooser = new FileChooser();
 		FileChooser.ExtensionFilter graphmlFilter = new FileChooser.ExtensionFilter("GraphML files (*.graphml)", "*.graphml");
 		FileChooser.ExtensionFilter xmlFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
 		FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("All files", "*");
 		fileChooser.getExtensionFilters().add(graphmlFilter);
 		fileChooser.getExtensionFilters().add(xmlFilter);
 		fileChooser.getExtensionFilters().add(allFilter);
 		
 		File file = fileChooser.showOpenDialog(referenceWindow.getStage());
 		if (file != null){
 			Scanner fileScanner = null;
 			try {
 				fileScanner = new Scanner(file);
 			} catch (FileNotFoundException e1) {
 				System.out.println();
 			}
 			if (fileScanner != null) {
 				//System.out.println(fileScanner.next());
 			}
 		}
 		
 		VisualisationRoot visualisationRootCurrent = new VisualisationRoot(referenceWindow);		
 		referenceWindow.getStage().setScene(new Scene(visualisationRootCurrent, 800, 600));
 	}
 
 }
