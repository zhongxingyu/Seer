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
 import game.configuration.Configurable;
 import game.core.Experiment;
 import game.core.Metric;
 import game.editorsystem.EditorWindow;
 import game.editorsystem.Option;
 import game.editorsystem.Option.Temporary;
 import game.editorsystem.OptionEditor;
 import game.plugins.Implementation;
 import game.plugins.constraints.CompatibleWith;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ResourceBundle;
 import java.util.SortedSet;
 
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.TreeItem;
 import javafx.scene.control.TreeView;
 import javafx.stage.FileChooser;
 
 public class ResultListController implements Initializable {
 	
 	@FXML
 	private TreeView resultsView;
 
 	@Override
 	public void initialize(URL location, ResourceBundle resources) {
 		resultsView.setRoot(new TreeItem("Results"));
 		resultsView.getRoot().setExpanded(true);
 	}
 	
 	public void addCompletedExperiment(Experiment e) {
 		if (!e.completed)
 			return;
 		
 		TreeItem expItem = new TreeItem(e);
 		
 		SortedSet<Implementation<Metric>> metrics = Settings.getPluginManager().
 				getCompatibleImplementationsOf(Metric.class, new CompatibleWith(new Temporary(e), "content"));
 		
 		for(Implementation<Metric> impl: metrics) {
 			TreeItem evaItem = new TreeItem(impl.getContent());
 			expItem.getChildren().add(evaItem);
 		}
 		if (!expItem.getChildren().isEmpty())
 			resultsView.getRoot().getChildren().add(expItem);
 	}
 	
 	@FXML
 	public void onLoad(ActionEvent event) {
 		FileChooser chooser = new FileChooser();
 		chooser.setInitialDirectory(new File(Settings.RESULTSDIR));
 		chooser.setTitle("Load completed experiment");
 		File file = chooser.showOpenDialog(resultsView.getScene().getWindow());
 		if (file != null)
 			addCompletedExperiment((Experiment)Configurable.createFromConfiguration(file));
 	}
 	
 	@FXML
 	public void onShow(ActionEvent event) {
 		TreeItem selected = (TreeItem)resultsView.getSelectionModel().getSelectedItem();
 		if (selected.getValue() instanceof Metric) {
 			Experiment e = (Experiment)selected.getParent().getValue();
 			Metric m = (Metric)selected.getValue();
 			m.evaluate(e);
 			TextViewer viewer = new TextViewer(m);
 			viewer.show();
 		}
 		if (selected.getValue() instanceof Experiment) {
 			Option option = new Option(selected.getValue());
 			OptionEditor editor = option.getBestEditor(true);
 			editor.setReadOnly(true);
 			new EditorWindow(editor).startEdit(option);
 		}
 	}
 	
 	@FXML
 	public void onRemove(ActionEvent event) {
 		TreeItem selected = (TreeItem)resultsView.getSelectionModel().getSelectedItem();
		if (selected != null && selected.getValue() instanceof Experiment) {
 			resultsView.getRoot().getChildren().remove(selected);
 		}
 	}
 	
 	@FXML
 	public void onClear(ActionEvent event) {
 		resultsView.getRoot().getChildren().clear();
 	}
 
 }
