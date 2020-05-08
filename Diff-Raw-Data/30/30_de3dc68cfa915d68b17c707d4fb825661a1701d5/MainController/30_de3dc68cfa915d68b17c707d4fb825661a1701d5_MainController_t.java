 /*******************************************************************************
  * Copyright (c) 2012 Emanuele Tamponi.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Emanuele Tamponi - initial API and implementation
  ******************************************************************************/
 package game.main;
 
 import game.Settings;
 import game.configuration.Change;
 import game.configuration.Configurable;
 import game.configuration.ConfigurableList;
 import game.core.Experiment;
 import game.editorsystem.EditorWindow;
 import game.editorsystem.Option;
 import game.editorsystem.OptionEditor;
 import game.plugins.PluginManager;
 import game.plugins.editors.ConfigurableEditor;
 import game.plugins.editors.SerializationEditor;
 import game.plugins.editors.configurablelist.ConfigurableListEditor;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Observable;
 import java.util.ResourceBundle;
 
 import javafx.beans.property.BooleanProperty;
 import javafx.beans.property.SimpleBooleanProperty;
 import javafx.concurrent.WorkerStateEvent;
 import javafx.event.ActionEvent;
 import javafx.event.Event;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.FXMLLoader;
 import javafx.fxml.Initializable;
 import javafx.fxml.JavaFXBuilderFactory;
 import javafx.geometry.Insets;
 import javafx.geometry.Pos;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.control.CheckBox;
 import javafx.scene.control.Label;
 import javafx.scene.control.ProgressBar;
 import javafx.scene.control.Separator;
 import javafx.scene.control.ToolBar;
 import javafx.scene.layout.AnchorPane;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.Priority;
 import javafx.scene.layout.VBox;
 import javafx.scene.text.Text;
 import javafx.stage.Stage;
 
 public class MainController extends Configurable implements Initializable {
 	
 	public final ConfigurableList experimentList = new ConfigurableList(this, Experiment.class);
 	
 	public final PluginManager manager = Settings.getPluginManager();
 	
 	private ExperimentService service;
 	
 	private ResultListController resultListController;
 
 	@FXML
 	private AnchorPane resultsRoot;
 	@FXML
 	private VBox experimentsRoot;
 	@FXML
 	private Button startButton;
 	@FXML
 	private Button pauseButton;
 	@FXML
 	private Button stopButton;
 	@FXML
 	private Text overallMessage;
 	@FXML
 	private ProgressBar overallProgress;
 	@FXML
 	private Text currentMessage;
 	@FXML
 	private ProgressBar currentProgress;
 	@FXML
 	private CheckBox addToResults;
 
 	@Override
 	public void initialize(URL location, ResourceBundle resources) {
 		Option model = new Option(this, "experimentList");
 		OptionEditor serialization = new SerializationEditor();
 		serialization.connect(model);
 		addPluginManagerEditorToToolBar((ToolBar)serialization.getView());
 		
 		OptionEditor editor = new ConfigurableListEditor();
 		editor.connect(model);
 		experimentsRoot.getChildren().addAll(serialization.getView(), editor.getView());
 		experimentsRoot.setSpacing(10);
 		experimentsRoot.setStyle("-fx-border-color:-fx-color; -fx-padding: 0px 0px 5px 0px;");
 		VBox.setVgrow(editor.getView(), Priority.ALWAYS);
 		disableButtons(true, true, true);
 		
 		try {
 			location = getClass().getResource("ResultListView.fxml");
 	
 			FXMLLoader fxmlLoader = new FXMLLoader();
 			fxmlLoader.setLocation(location);
 			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
 
 			Parent parent = (Parent)fxmlLoader.load(location.openStream());
 			resultListController = fxmlLoader.getController();
 			resultsRoot.getChildren().add(parent);
 			AnchorPane.setTopAnchor(parent, 14.0);
 			AnchorPane.setLeftAnchor(parent, 14.0);
 			AnchorPane.setRightAnchor(parent, 14.0);
 			AnchorPane.setBottomAnchor(parent, 14.0);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void addPluginManagerEditorToToolBar(ToolBar toolbar) {
 		Button pmButton = new Button("Plugins");
 		pmButton.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent event) {
 				new EditorWindow(new ConfigurableEditor()).startEdit(new Option(MainController.this, "manager"));
 				Settings.getPluginManager().saveConfiguration(Settings.CONFIGFILE);
 			}
 		});
 		toolbar.getItems().addAll(new Separator(), pmButton);
 	}
 	
 	@FXML
 	public void onStart(ActionEvent event) {
 		service = new ExperimentService(this);
 		
 		currentProgress.progressProperty().bind(service.progressProperty());
 		currentMessage.textProperty().bind(service.messageProperty());
 		
 		double step = 1.0 / experimentList.size();
 		overallProgress.progressProperty().bind(
 				(service.progressProperty().divide(experimentList.size())).add(service.counterProperty().multiply(step)));
 		overallMessage.textProperty().bind(service.currentExperimentProperty());
 		
 		service.setOnFailed(new EventHandler<WorkerStateEvent>() {
 			@Override
 			public void handle(WorkerStateEvent event) {
 				if (!service.isStopped())
 					service.getException().printStackTrace();
 				controlButtons();
 			}
 		});
 
 		service.addEventHandler(ExperimentService.FINISHED, new EventHandler<Event>() {
 			@Override
 			public void handle(Event event) {
 				controlButtons();
 			}
 		});
 
		service.startList();
 		disableButtons(true, false, false);
 	}
 	
 	@FXML
 	public void onPause(ActionEvent event) {
 		if (pauseButton.getText().equals("Pause")) {
 			service.pause();
 			pauseButton.setText("Resume");
 		} else {
 			service.resume();
 			pauseButton.setText("Pause");
 		}
 	}
 	
 	private boolean confirmationDialog() {
 		final BooleanProperty sure = new SimpleBooleanProperty(false);
 		final Stage stage = new Stage();
 		
 		VBox container = new VBox();
 		HBox buttons = new HBox();
 		Button yes = new Button("Yes");
 		Button no = new Button("No");
 		yes.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent event) {
 				sure.set(true);
 				stage.close();
 			}
 		});
 		no.setOnAction(new EventHandler<ActionEvent>() {
 			@Override
 			public void handle(ActionEvent event) {
 				stage.close();
 			}
 		});
 		buttons.setSpacing(15);
 		buttons.getChildren().addAll(yes, no);
 		container.setSpacing(15);
 		container.setPadding(new Insets(15));
 		container.getChildren().addAll(new Label("Do you want to stop all the experiments?"), buttons);
 		buttons.setAlignment(Pos.CENTER);
 		stage.setScene(new Scene(container));
 		stage.setResizable(false);
 		stage.showAndWait();
 		return sure.get();
 	}
 	
 	@FXML
 	public void onStop(ActionEvent event) {
 		boolean alreadyPaused = service.isPaused();
 		if (!alreadyPaused)
 			service.pause();
 		
 		if (confirmationDialog()) {
 			service.stop();
 			disableButtons(false, true, true);
 			pauseButton.setText("Pause");
 			overallMessage.textProperty().unbind();
 			overallMessage.setText("");
 			currentMessage.textProperty().unbind();
 			currentMessage.setText("");
 			overallProgress.progressProperty().unbind();
 			overallProgress.setProgress(0);
 			currentProgress.progressProperty().unbind();
 			currentProgress.setProgress(0);
 		} else {
 			if (!alreadyPaused)
 				service.resume();
 		}
 	}
 
 	@Override
 	public void update(Observable observedOption, Object message) {
 		super.update(observedOption, message);
 		if (message instanceof Change) {
 			controlButtons();
 		}
 	}
 	
 	private void controlButtons() {
 		if (service != null && !service.hasFinished())
 			return;
 		if (experimentList.isEmpty() || !experimentList.getConfigurationErrors().isEmpty()) {
 			disableButtons(true, true, true);
 		} else {
 			disableButtons(false, true, true);
 		}
 	}
 
 	private void disableButtons(boolean start, boolean pause, boolean stop) {
 		startButton.setDisable(start);
 		pauseButton.setDisable(pause);
 		stopButton.setDisable(stop);
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		if (service != null && service.isRunning())
 			service.stop();
 		super.finalize();
 	}
 	
 	public ResultListController getResultListController() {
 		return resultListController;
 	}
 	
 	public boolean addToResultList() {
 		return addToResults.isSelected();
 	}
 
 }
