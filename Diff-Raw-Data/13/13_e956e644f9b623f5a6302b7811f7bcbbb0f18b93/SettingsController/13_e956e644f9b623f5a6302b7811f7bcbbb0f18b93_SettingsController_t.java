 package org.comtel.fritz.connect;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ResourceBundle;
 
 import javafx.application.Platform;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.concurrent.Service;
 import javafx.concurrent.Task;
 import javafx.concurrent.WorkerStateEvent;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Accordion;
 import javafx.scene.control.Button;
 import javafx.scene.control.CheckBox;
 import javafx.scene.control.ListCell;
 import javafx.scene.control.ListView;
 import javafx.scene.control.PasswordField;
 import javafx.scene.control.SelectionMode;
 import javafx.scene.control.TextField;
 import javafx.scene.control.TitledPane;
 import javafx.scene.input.MouseButton;
 import javafx.scene.input.MouseEvent;
 import javafx.util.Callback;
 
 import org.comtel.fritz.connect.bookmark.Bookmark;
 import org.controlsfx.dialog.Dialogs;
 import org.slf4j.LoggerFactory;
 
 public class SettingsController implements Initializable {
 
 	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(SettingsController.class);
 
 	@FXML
 	private ListView<Bookmark> bookmarkListView;
 
 	@FXML
 	private Accordion settingsPane;
 
 	@FXML
 	private TextField ipField;
 
 	@FXML
 	private TextField portField;
 
 	@FXML
 	private TextField userField;
 
 	@FXML
 	private PasswordField pwdField;
 
 	@FXML
 	private CheckBox sslCheckBox;
 
 	@FXML
 	private Button testBtn;
 
 	@FXML
 	private Button loadBtn;
 	@FXML
 	private Button removeBtn;
 
 	@Override
 	public void initialize(URL arg0, ResourceBundle arg1) {
 
 		settingsPane.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
 			@Override
 			public void changed(ObservableValue<? extends TitledPane> observable, TitledPane oldValue, TitledPane newValue) {
 				Platform.runLater(new Runnable() {
 					@Override
 					public void run() {
 						if (settingsPane.getExpandedPane() == null)
 							settingsPane.setExpandedPane(settingsPane.getPanes().get(0));
 					}
 				});
 			}
 		});
 
 		portField.textProperty().addListener(new ChangeListener<String>() {
 			@Override
 			public void changed(ObservableValue<? extends String> observable, String oldValue, String port) {
 
 				if (port == null || port.isEmpty()) {
 					SwitcherApp.getSwitchService().getPortProperty().set(-1);
 					return;
 				}
 
 				if (port.length() > 5) {
 					portField.setText(port.substring(0, 5));
 				}
 
 				if (!port.matches("[0-9]{0,5}")) {
 					if (oldValue.matches("[0-9]{0,5}")) {
 						portField.setText(oldValue);
 					} else {
 						portField.setText("");
 					}
 					return;
 				}
 
 				try {
 					SwitcherApp.getSwitchService().getPortProperty().set(Integer.valueOf(port));
 				} catch (NumberFormatException e) {
 					logger.error(e.getMessage(), e);
 				}
 			}
 		});
 
 		SessionManager sessionManager = SessionManager.getSessionManager();
 
 		sessionManager.bind(ipField.textProperty(), "ipField");
 		sessionManager.bind(portField.textProperty(), "portField");
 		sessionManager.bind(userField.textProperty(), "userField");
 		sessionManager.bindCrypt(pwdField.textProperty(), "pwdField");
 		sessionManager.bind(sslCheckBox.selectedProperty(), "useSSL");
 
 		sessionManager.bind(settingsPane, "settingsPane");
 
 		SwitcherApp.getSwitchService().getHostProperty().bind(ipField.textProperty());
 		SwitcherApp.getSwitchService().getUserProperty().bind(userField.textProperty());
 		SwitcherApp.getSwitchService().getPwdProperty().bind(pwdField.textProperty());
 		SwitcherApp.getSwitchService().getSslProperty().bind(sslCheckBox.selectedProperty());
 
 		bookmarkListView.setItems(sessionManager.getBookmarks());
 		bookmarkListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
 
 		bookmarkListView.setCellFactory(new Callback<ListView<Bookmark>, ListCell<Bookmark>>() {
 			@Override
 			public ListCell<Bookmark> call(ListView<Bookmark> param) {
 				return new BookmarkCell();
 			}
 		});
 
 		bookmarkListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
 			@Override
 			public void handle(MouseEvent mouseEvent) {
 				if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
 					if (mouseEvent.getClickCount() == 2) {
 						loadBookmark(null);
 					}
 				}
 			}
 		});
 		loadBtn.disableProperty().bind(bookmarkListView.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
 		removeBtn.disableProperty().bind(bookmarkListView.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
 
 	}
 
 	private class BookmarkCell extends ListCell<Bookmark> {
 
 		public BookmarkCell() {
 			itemProperty().addListener(new ChangeListener<Bookmark>() {
 				@Override
 				public void changed(ObservableValue<? extends Bookmark> observable, Bookmark oldValue, Bookmark newValue) {
					textProperty().set(newValue != null ? newValue.getId() : "");
 				}
 			});
 		}
 
 	}
 
 	@FXML
 	public void loadBookmark(ActionEvent event) {
 		Bookmark bm = bookmarkListView.getSelectionModel().getSelectedItem();
 		if (bm != null) {
 			logger.info("try to load bookmark: {}", bm.getId());
 			ipField.setText(bm.getIp());
 			portField.setText(bm.getPort() > 0 ? Integer.toString(bm.getPort()) : "");
 			sslCheckBox.setSelected(bm.isUseSSL());
 			userField.setText(bm.getUser());
 			pwdField.setText(bm.getPassword());
 			settingsPane.setExpandedPane(settingsPane.getPanes().get(0));
 		}
 	}
 
 	@FXML
 	public void removeBookmark(ActionEvent event) {
 		Bookmark bm = bookmarkListView.getSelectionModel().getSelectedItem();
 		if (bm != null) {
 			bookmarkListView.getItems().remove(bm);
 		}
 	}
 
 	@FXML
 	public void testConnection(ActionEvent event) {
 
 		TestConnectionService service = new TestConnectionService();
 
 		settingsPane.disableProperty().bind(service.runningProperty());
 		service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
 			@Override
 			public void handle(WorkerStateEvent t) {
 				Dialogs.create().owner(testBtn.getScene().getWindow()).lightweight().title("CONNECTION").masthead("Response ok from URL: " + SwitcherApp.getSwitchService().getURL())
 						.message("try to refresh smart home devices").showInformation();
 			}
 		});
 		service.setOnFailed(new EventHandler<WorkerStateEvent>() {
 			@Override
 			public void handle(WorkerStateEvent t) {
 				Throwable e = t.getSource().getException();
 				String msg = e != null ? e.getMessage() : "general error";
 				;
 				Dialogs.create().owner(testBtn.getScene().getWindow()).lightweight().title("CONNECTION ERROR").masthead("Test connection fails! URL: " + SwitcherApp.getSwitchService().getURL())
 						.message(msg).showExceptionInNewWindow(e);
 				logger.error("test host connection failed", e);
 			}
 		});
 		service.start();
 	}
 
 	private class TestConnectionService extends Service<Void> {
 
 		protected Task<Void> createTask() {
 			return new Task<Void>() {
 				protected Void call() throws IOException, Exception {
 					SwitcherApp.getSwitchService().validateConnection();
 					return null;
 				}
 			};
 		}
 	}
 
 	@FXML
 	public void addBookmark(ActionEvent event) {
 
 		String id = Dialogs.create().owner(testBtn.getScene().getWindow()).lightweight().title("ADD BOOKMARK").masthead("Insert connection name:").showTextInput(ipField.getText());
 
 		if (id == null || id.isEmpty()) {
 			Dialogs.create().owner(testBtn.getScene().getWindow()).lightweight().title("ADD BOOKMARK").masthead("Connection name invalid!").showError();
 			return;
 		}
 		Bookmark bm = new Bookmark();
 		bm.setId(id);
 		bm.setIp(ipField.getText());
 		String port = portField.getText();
 		bm.setPort(Integer.valueOf(port != null && !port.isEmpty() ? port : "0"));
 
 		bm.setUseSSL(sslCheckBox.isSelected());
 		bm.setUser(userField.getText());
 		bm.setPassword(pwdField.getText());
 
 		bookmarkListView.getItems().add(bm);
 	}
 
 	@FXML
 	public void resetFields(ActionEvent event) {
 		ipField.setText("fritz.box");
 		portField.setText("");
 		sslCheckBox.setSelected(false);
 		userField.setText("");
 		pwdField.setText("");
 	}
 }
