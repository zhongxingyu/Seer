 package nl.groep5.xchange.controllers;
 
 import java.net.URL;
 import java.util.ResourceBundle;
 
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Control;
 import javafx.scene.control.TableColumn;
 import javafx.scene.control.TableView;
 import javafx.scene.control.TextField;
 import javafx.scene.control.cell.PropertyValueFactory;
 import javafx.scene.input.KeyCode;
 import javafx.scene.input.KeyEvent;
 import javafx.scene.input.MouseEvent;
 import nl.groep5.xchange.Communicator;
 import nl.groep5.xchange.models.DownloadableFile;
 
 public class SearchController extends Control implements Initializable {
 
 	@FXML
 	TableView<DownloadableFile> searchResults;
 	@FXML
 	TableColumn<DownloadableFile, String> fileName;
 	@FXML
 	TableColumn<DownloadableFile, String> fileSize;
 	@FXML
 	TableColumn<DownloadableFile, String> ip;
 
 	@FXML
 	protected void handleSearchField(KeyEvent keyEvent) {
 		Communicator.clearSearchResults();
 		if (keyEvent.getCode() == KeyCode.ENTER) {
 			TextField searchField = (TextField) keyEvent.getSource();
 			String searchInput = searchField.getText();
 			if (searchInput.length() <= 0)
 				return;
 
 			Communicator.search(searchInput);
 		}
 	}
 
 	@FXML
 	protected void downloadFile(MouseEvent mouseEvent) {
 		DownloadableFile downloadableFile = searchResults.getSelectionModel()
 				.getSelectedItem();
 		if (downloadableFile == null)
 			return;
 
 		DownloadController.addDownload(downloadableFile);
 	}
 
 	@Override
 	public void initialize(URL arg0, ResourceBundle arg1) {
 		searchResults.setItems(Communicator.getSearchResults());
 
 		fileName.setCellValueFactory(new PropertyValueFactory<DownloadableFile, String>(
 				"fileName"));
 		fileSize.setCellValueFactory(new PropertyValueFactory<DownloadableFile, String>(
 				"fileSize"));
 		ip.setCellValueFactory(new PropertyValueFactory<DownloadableFile, String>(
 				"ip"));
 	}
 }
