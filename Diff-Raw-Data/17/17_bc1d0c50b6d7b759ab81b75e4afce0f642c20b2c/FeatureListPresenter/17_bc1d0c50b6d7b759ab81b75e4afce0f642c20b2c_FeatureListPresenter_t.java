 package de.thatsich.bachelor.featureextraction.restricted.controller;
 
 import java.net.URL;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.concurrent.ExecutorService;
 
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
 import javafx.collections.ListChangeListener;
 import javafx.concurrent.WorkerStateEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.scene.control.TreeItem;
 import javafx.scene.control.TreeView;
 
 import com.google.inject.Inject;
 
 import de.thatsich.bachelor.featureextraction.api.entities.FeatureVector;
 import de.thatsich.bachelor.featureextraction.api.entities.FeatureVectorSet;
 import de.thatsich.bachelor.featureextraction.restricted.controller.commands.GetLastFeatureVectorIndexCommand;
 import de.thatsich.bachelor.featureextraction.restricted.controller.commands.InitFeatureVectorSetListCommand;
 import de.thatsich.bachelor.featureextraction.restricted.models.FeatureState;
 import de.thatsich.bachelor.featureextraction.restricted.models.FeatureVectorSets;
 import de.thatsich.bachelor.featureextraction.restricted.services.FeatureCommandService;
 import de.thatsich.bachelor.featureextraction.restricted.views.tree.FeatureVectorSetTreeItemAdapter;
 import de.thatsich.bachelor.featureextraction.restricted.views.tree.FeatureVectorTreeItemAdapter;
 import de.thatsich.bachelor.featureextraction.restricted.views.tree.IFeatureSpaceTreeItemAdapter;
 import de.thatsich.core.javafx.AFXMLPresenter;
 import de.thatsich.core.javafx.CommandExecutor;
 
 public class FeatureListPresenter extends AFXMLPresenter {
 
 	// Nodes
 	@FXML private TreeView<IFeatureSpaceTreeItemAdapter> nodeTreeViewFeatureVectorSetList;
 	private TreeItem<IFeatureSpaceTreeItemAdapter> nodeTreeViewRoot;
 	
 	// Injects
 	@Inject private FeatureState featureState;
 	@Inject private FeatureVectorSets featureVectors;
 	@Inject private FeatureCommandService commander;
 	
 	@Override
 	public void initialize(URL location, ResourceBundle resources) {
 		this.bindTreeView();
 		this.initTreeView();
 		
 		this.initFeatureVectorList();
 	}
 	
 	private void bindTreeView() {
 		this.bindTreeViewModel();
 		this.bindTreeViewSelection();
 	}
 	
 	private void bindTreeViewModel() {
 		this.featureVectors.getFeatureVectorSetListProperty().addListener(new ListChangeListener<FeatureVectorSet>() {
 			@Override public void onChanged(Change<? extends FeatureVectorSet> paramChange) {
 				while (paramChange.next()) {
 					if (paramChange.wasAdded()) {
 						log.info("Items were added to TreeView.");
 						
 						final List<? extends FeatureVectorSet> featureVectorSetList = paramChange.getAddedSubList();
 						log.info("List Size: " + featureVectorSetList.size());
 						
 						for (FeatureVectorSet set : featureVectorSetList) {
 							log.info(set.toString());
 							final List<FeatureVector> featureVectorList = set.getFeatureVectorList();
 							final TreeItem<IFeatureSpaceTreeItemAdapter> setTreeItem = new TreeItem<IFeatureSpaceTreeItemAdapter>(new FeatureVectorSetTreeItemAdapter(set));
 							setTreeItem.setExpanded(true);
 							
 							for (FeatureVector vector : featureVectorList) {
 								setTreeItem.getChildren().add(new TreeItem<IFeatureSpaceTreeItemAdapter>(new FeatureVectorTreeItemAdapter(vector)));
 							}
 				        	
 							nodeTreeViewRoot.getChildren().add(setTreeItem);
 						}
 					}
 					else if (paramChange.wasRemoved()) {
 						log.info("Items were removed from TreeView.");
 						
 						final List<? extends FeatureVectorSet> removedSubList = paramChange.getRemoved();
						final List<TreeItem<IFeatureSpaceTreeItemAdapter>> removing = FXCollections.observableArrayList();
						
 						for (TreeItem<IFeatureSpaceTreeItemAdapter> child : nodeTreeViewRoot.getChildren()) {
 							for (FeatureVectorSet set : removedSubList) {
								final FeatureVectorSet childSet = child.getValue().getSet(); 
								
								if (set.equals(childSet)) {
									removing.add(child);
 								}
 							}
 						}
						
						for (TreeItem<IFeatureSpaceTreeItemAdapter> removeChild : removing) {
							nodeTreeViewRoot.getChildren().remove(removeChild);
						}
 					}
 					else if (paramChange.wasUpdated()) {
 						log.info("Updated?" + paramChange.wasUpdated());
 					}
 					else if (paramChange.wasPermutated()) {
 						log.info("Permutated?" + paramChange.wasPermutated());
 					}
 					else if (paramChange.wasReplaced()) {
 						log.info("Replaced?" + paramChange.wasReplaced());
 					}
 					else {
 						log.info("Huh?");
 					}
 				}
  			}
 
 		});
 		this.log.info("Bound List to FeatureSpace.");
 	}
 	
 	private void bindTreeViewSelection() {
 		this.nodeTreeViewFeatureVectorSetList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<IFeatureSpaceTreeItemAdapter>>() {
 			@Override public void changed(ObservableValue<? extends TreeItem<IFeatureSpaceTreeItemAdapter>> observable, TreeItem<IFeatureSpaceTreeItemAdapter> oldValue, TreeItem<IFeatureSpaceTreeItemAdapter> newValue) {
 				if (newValue == null) return;
 				
 				final IFeatureSpaceTreeItemAdapter item = newValue.getValue();
 				
 				if (item.isSet()) {
 					featureVectors.getSelectedFeatureVectorSetProperty().set(item.getSet());
 					featureVectors.getSelectedFeatureVectorProperty().set(null);
 				} 
 				else if (item.isVector()) {
 					final IFeatureSpaceTreeItemAdapter set = newValue.getParent().getValue();
 					if (!set.isSet()) throw new IllegalStateException("Superset of a FeatureVector should be a FeatureVectorSet.");
 					
 					featureVectors.getSelectedFeatureVectorSetProperty().set(set.getSet());
 					featureVectors.getSelectedFeatureVectorProperty().set(item.getVector());
 				}
 				
 				// is root or something else
 				else {
 					featureVectors.getSelectedFeatureVectorSetProperty().set(null);
 					featureVectors.getSelectedFeatureVectorProperty().set(null);
 				}
 			}
 		});
 	}
 	
 	private void initTreeView() {
 		this.nodeTreeViewRoot = new TreeItem<IFeatureSpaceTreeItemAdapter>(new IFeatureSpaceTreeItemAdapter() {
 			@Override public String toString() { return "FeatureVector Sets"; }
 			@Override public boolean isVector() { return false; }
 			@Override public boolean isSet() { return false; }
 			@Override public FeatureVector getVector() { return null; }
 			@Override public FeatureVectorSet getSet() { return null; }
 		});
 		this.nodeTreeViewRoot.setExpanded(true);
 		this.nodeTreeViewFeatureVectorSetList.setRoot(nodeTreeViewRoot);
 	}
 	
 	private void initFeatureVectorList() {
 		final Path folderPath = Paths.get("io/featurevectors");
 		final InitFeatureVectorListSucceededHandler initHandler = new InitFeatureVectorListSucceededHandler();
 		final GetLastFeatureSpaceIndexSucceededHandler lastHandler = new GetLastFeatureSpaceIndexSucceededHandler();
 		final ExecutorService executor = CommandExecutor.newFixedThreadPool(1);
 		
 		this.featureState.getFeatureVectorFolderPathProperty().set(folderPath);
 		this.log.info("Set FeatureVectorInputFolderPath to Model.");
 		
 		final InitFeatureVectorSetListCommand initCommand = this.commander.createInitFeatureVectorListCommand(folderPath);
 		initCommand.setOnSucceeded(initHandler);
 		initCommand.setExecutor(executor);
 		initCommand.start();
 		this.log.info("Initialized InitFeatureVectorList Retrieval.");
 		
 		final GetLastFeatureVectorIndexCommand lastCommand = this.commander.createGetLastFeatureVectorIndexCommand();
 		lastCommand.setExecutor(executor);
 		lastCommand.setOnSucceeded(lastHandler);
 //		lastCommand.start();
 		this.log.info("Initialized GetLastFeatureVectorIndex Retrieval.");
 		
 		executor.shutdown();
 		this.log.info("Shutting down Executor.");
 	}
 
 	// ================================================== 
 	// Handler Implementation 
 	// ==================================================	
 	/**
 	 * Handler for what should happen if the Command was successfull 
 	 * for initializing the feature vector list
 	 * 
 	 * @author Minh
 	 */
 	@SuppressWarnings("unchecked")
 	private class InitFeatureVectorListSucceededHandler implements EventHandler<WorkerStateEvent> {
 		@Override public void handle(WorkerStateEvent event) {
 			final List<FeatureVectorSet> featureVectorList = (List<FeatureVectorSet>) event.getSource().getValue();
 			
 			featureVectors.getFeatureVectorSetListProperty().get().addAll(featureVectorList);
 			log.info("Added FeatureExtractor to Database.");
 		}
 	}
 	
 	/**
 	 * Handler for what should happen if the Command was successfull 
 	 * for getting the LastFeatureVectorIndex
 	 * 
 	 * @author Minh
 	 */
 	private class GetLastFeatureSpaceIndexSucceededHandler implements EventHandler<WorkerStateEvent> {
 		@Override public void handle(WorkerStateEvent event) {
 			final Integer commandResult = (Integer) event.getSource().getValue();
 			log.info("Retrieved LastFeatureVectorIndex.");
 			
 			if (commandResult != null && commandResult >= 0 && featureVectors.getFeatureVectorSetListProperty().size() > commandResult) {
 				nodeTreeViewFeatureVectorSetList.getSelectionModel().select(commandResult);
 				log.info("Set LastFeatureVectorIndex in TreeView.");
 				
 				final TreeItem<IFeatureSpaceTreeItemAdapter> selectedItem = nodeTreeViewFeatureVectorSetList.getSelectionModel().selectedItemProperty().get();
 				final IFeatureSpaceTreeItemAdapter selectedFeatureSpaceItem = selectedItem.getValue();
 				if (selectedFeatureSpaceItem.isSet()) {
 					featureVectors.getSelectedFeatureVectorSetProperty();
 				}
 				else if (selectedFeatureSpaceItem.isVector()) {
 					
 				}
 				log.info("Set LastFeatureVectorIndex in Model.");
 			}
 		}
 	}
 }
