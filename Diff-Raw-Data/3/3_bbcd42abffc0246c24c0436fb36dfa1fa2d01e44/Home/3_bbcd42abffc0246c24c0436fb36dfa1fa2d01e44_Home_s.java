 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.wineshop.admin.client;
 
 import java.net.URL;
 import java.util.Calendar;
 import java.util.ResourceBundle;
 
 import javafx.beans.binding.Bindings;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.Node;
 import javafx.scene.Parent;
 import javafx.scene.control.Button;
 import javafx.scene.control.ChoiceBox;
 import javafx.scene.control.Label;
 import javafx.scene.control.ListCell;
 import javafx.scene.control.ListView;
 import javafx.scene.control.TableCell;
 import javafx.scene.control.TableColumn;
 import javafx.scene.control.TableColumn.CellEditEvent;
 import javafx.scene.control.TableView;
 import javafx.scene.control.TextField;
 import javafx.scene.control.cell.PropertyValueFactory;
 import javafx.scene.control.cell.TextFieldTableCell;
 import javafx.scene.layout.HBox;
 import javafx.util.Callback;
 import javafx.util.converter.IntegerStringConverter;
 
 import javax.inject.Inject;
 
 import org.granite.client.tide.collections.javafx.PagedQuery;
 import org.granite.client.tide.collections.javafx.TableViewSort;
 import org.granite.client.tide.data.Conflicts;
 import org.granite.client.tide.data.EntityManager;
 import org.granite.client.tide.data.EntityManager.UpdateKind;
 import org.granite.client.tide.javafx.JavaFXDataManager;
 import org.granite.client.tide.javafx.spring.Identity;
 import org.granite.client.tide.server.SimpleTideResponder;
 import org.granite.client.tide.server.TideFaultEvent;
 import org.granite.client.tide.server.TideResultEvent;
 import org.granite.client.tide.spring.TideApplicationEvent;
 import org.granite.client.validation.javafx.FormValidator;
 import org.granite.client.validation.javafx.ValidationResultEvent;
 import org.springframework.context.ApplicationListener;
 import org.springframework.stereotype.Component;
 
 import com.wineshop.admin.client.entities.Address;
 import com.wineshop.admin.client.entities.Vineyard;
 import com.wineshop.admin.client.entities.Wine;
 import com.wineshop.admin.client.entities.Wine$Type;
 import com.wineshop.admin.client.services.VineyardRepository;
 
 
 /**
  * 
  * @author william
  */
 @Component
 public class Home implements Initializable, ApplicationListener<TideApplicationEvent> {
 
 	@FXML
 	private TextField textSearch;
 
 	@FXML
 	private TableView<Vineyard> listVineyards;
 	
 	@FXML
 	private TableColumn<Vineyard, String> columnName;
 	
 	@FXML
 	private Parent formVineyard;
 	
 	@FXML
 	private Label labelForm;
 
 	@FXML
 	private TextField textName;
 
 	@FXML
 	private TextField textAddress;
 
 	@FXML
 	private Button saveButton;
 
 	@FXML
 	private Button deleteButton;
 
 	@FXML
 	private Button cancelButton;
 	
 	@FXML
 	private ListView<Wine> listWines;
 
 	@Inject
 	private PagedQuery<Vineyard, Vineyard> vineyards;
 
 	@FXML
 	private Vineyard vineyard = new Vineyard();
 	
 	@Inject
 	private Identity identity;
 	
 	@Inject
 	private VineyardRepository vineyardRepository;
 	
 	@Inject
 	private EntityManager entityManager;
 	
 	@Inject
 	private JavaFXDataManager dataManager;
 	
 	
 	private FormValidator formValidator = new FormValidator();
 	
 	
 	@SuppressWarnings("unused")
 	@FXML
 	private void logout(ActionEvent event) {
 		identity.logout(null);
 	}
 
 	@SuppressWarnings("unused")
 	@FXML
 	private void search(ActionEvent event) {
 		vineyards.refresh();
 	}
 
 	private void select(Vineyard vineyard) {
 		if (vineyard == this.vineyard && this.vineyard != null)
 			return;
 		
 		formValidator.setForm(null);
 		
 		if (this.vineyard != null) {
 			textName.textProperty().unbindBidirectional(this.vineyard.nameProperty());
 			if (this.vineyard.getAddress() != null)
 				textAddress.textProperty().unbindBidirectional(this.vineyard.getAddress().addressProperty());
 			entityManager.resetEntity(this.vineyard);
 		}
 		
 		if (vineyard != null)
 			this.vineyard = vineyard;
 		else {
 			this.vineyard = new Vineyard();
 			this.vineyard.setName("");
 			this.vineyard.setAddress(new Address());
 			this.vineyard.getAddress().setAddress("");
 			entityManager.mergeExternalData(this.vineyard);
 		}
 		
 		textName.textProperty().bindBidirectional(this.vineyard.nameProperty());
 		textAddress.textProperty().bindBidirectional(this.vineyard.getAddress().addressProperty());
 		listWines.setItems(this.vineyard.getWines());
 		
 		formValidator.setForm(formVineyard);
 		
 		labelForm.setText(vineyard != null ? "Edit vineyard" : "Create vineyard");
 		deleteButton.setVisible(vineyard != null);
 		cancelButton.setVisible(vineyard != null);
 	}
 
 	@SuppressWarnings("unused")
 	@FXML
 	private void save(ActionEvent event) {
 		if (!formValidator.validate(vineyard))
 			return;
 		
 		final boolean isNew = vineyard.getId() == null;
 		vineyardRepository.save(vineyard, 
 			new SimpleTideResponder<Vineyard>() {
 				@Override
 				public void result(TideResultEvent<Vineyard> tre) {
 					if (isNew)
 						select(null);
 					else
 						listVineyards.getSelectionModel().clearSelection();
 				}
 				
 				@Override
 				public void fault(TideFaultEvent tfe) {
 					System.out.println("Error: " + tfe.getFault().getFaultDescription());
 				}
 			}
 		);
 	}
 	
 	private void save(Vineyard vineyard) {
 		if (!formValidator.validate(vineyard))
 			return;
 		
 		vineyardRepository.save(vineyard, new SimpleTideResponder<Vineyard>() {
 			@Override
 			public void result(TideResultEvent<Vineyard> event) {
 			}
 		});
 	}
 
 	@SuppressWarnings("unused")
 	@FXML
 	private void cancel(ActionEvent event) {
 		listVineyards.getSelectionModel().clearSelection();
 	}
 
 	@SuppressWarnings("unused")
 	@FXML
 	private void delete(ActionEvent event) {
 		vineyardRepository.delete(vineyard.getId(), 
 			new SimpleTideResponder<Void>() {
 				@Override
 				public void result(TideResultEvent<Void> tre) {
 					listVineyards.getSelectionModel().clearSelection();
 					select(null);
 				}
 			}
 		);
 	}
 	
 	@SuppressWarnings("unused")
 	@FXML
 	private void addWine(ActionEvent event) {
 		Wine wine = new Wine();
 		wine.setVineyard(this.vineyard);
 		wine.setName("");
 		wine.setYear(Calendar.getInstance().get(Calendar.YEAR)-3);
 		wine.setType(Wine$Type.RED);
 		this.vineyard.getWines().add(wine);
 	}
 	
 	@SuppressWarnings("unused")
 	@FXML
 	private void removeWine(ActionEvent event) {
 		if (!listWines.getSelectionModel().isEmpty())
 			this.vineyard.getWines().remove(listWines.getSelectionModel().getSelectedIndex());
 	}
 	
 
 	@Override
 	public void onApplicationEvent(TideApplicationEvent event) {
 		if (event.getType().equals(UpdateKind.REMOVE.eventName(Vineyard.class)) && event.getArgs()[0].equals(this.vineyard))
 			select(null);
 		else if (event.getType().equals(UpdateKind.CONFLICT)) {
 			try {
 				new Conflict((Conflicts)event.getArgs()[0]).show();
 			}
 			catch (Exception e) {
 				System.err.println("Could not get conflicts");
 			}
 		}
 	}
 	
 	@Override
 	public void initialize(URL url, ResourceBundle rb) {
 		((Vineyard)vineyards.getFilter()).nameProperty().bindBidirectional(textSearch.textProperty());
 		
 		columnName.prefWidthProperty().bind(listVineyards.widthProperty().add(-2.0));
 		columnName.setCellValueFactory(new PropertyValueFactory<Vineyard,String>("name"));
 		Callback<TableColumn<Vineyard,String>, TableCell<Vineyard,String>> cellFactory = TextFieldTableCell.forTableColumn();
 		columnName.setCellFactory(cellFactory);
 		listVineyards.setEditable(true);
 		columnName.setEditable(true);
 		columnName.setOnEditCommit(new EventHandler<CellEditEvent<Vineyard, String>>() {
 			@Override
 			public void handle(CellEditEvent<Vineyard, String> event) {
 				Vineyard vineyard = event.getRowValue();
 				vineyard.setName(event.getNewValue());
 				save(vineyard);
 			}
 		});
 		
 		select(null);
 		listVineyards.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Vineyard>() {
 			@Override
 			public void changed(ObservableValue<? extends Vineyard> property, Vineyard oldSelection, Vineyard newSelection) {
 				select(newSelection);
 			}			
 		});
 		vineyards.setSort(new TableViewSort<Vineyard>(listVineyards, Vineyard.class));
 		
		textName.textProperty().bindBidirectional(vineyard.nameProperty());
		textAddress.textProperty().bindBidirectional(vineyard.getAddress().addressProperty());
		listWines.setItems(vineyard.getWines());
 		listWines.setCellFactory(new Callback<ListView<Wine>, ListCell<Wine>>() {
 			public ListCell<Wine> call(ListView<Wine> listView) {
 				return new WineListCell();
 			}
 		});
 		
 		saveButton.disableProperty().bind(Bindings.not(dataManager.dirtyProperty()));
 		deleteButton.disableProperty().bind(Bindings.not(identity.ifAllGranted("ROLE_ADMIN")));
 		
 		formValidator.setForm(formVineyard);
 		formVineyard.addEventHandler(ValidationResultEvent.ANY, new EventHandler<ValidationResultEvent>() {
 			@Override
 			public void handle(ValidationResultEvent event) {
 				if (event.getEventType() == ValidationResultEvent.INVALID)
 					((Node)event.getTarget()).setStyle("-fx-border-color: red");
 				else if (event.getEventType() == ValidationResultEvent.VALID)
 					((Node)event.getTarget()).setStyle("-fx-border-color: null");
 			}
 		});
 		
 		deleteButton.setVisible(false);
 		cancelButton.setVisible(false);
 	}
 	
 	
 	private static class WineListCell extends ListCell<Wine> {
 		
 		private ChoiceTypeListener choiceTypeListener = null;
 		
 		protected void updateItem(Wine wine, boolean empty) {
 			Wine oldWine = getItem();
 			if (oldWine != null && wine == null) {
 				HBox hbox = (HBox)getGraphic();
 				
 				TextField fieldName = (TextField)hbox.getChildren().get(0);
 				fieldName.textProperty().unbindBidirectional(getItem().nameProperty());
 				
 				TextField fieldYear = (TextField)hbox.getChildren().get(1);
 				fieldYear.textProperty().unbindBidirectional(getItem().yearProperty());
 				
 				getItem().typeProperty().unbind();
 				getItem().typeProperty().removeListener(choiceTypeListener);
 				choiceTypeListener = null;
 				
 				setGraphic(null);
 			}
 			
 			super.updateItem(wine, empty);
 			
 			if (wine != null && wine != oldWine) {
 				TextField fieldName = new TextField();
 				fieldName.textProperty().bindBidirectional(wine.nameProperty());
 				
 				TextField fieldYear = new TextField();
 				fieldYear.setPrefWidth(40);
 				fieldYear.textProperty().bindBidirectional(wine.yearProperty(), new IntegerStringConverter());
 				
 				ChoiceBox<Wine$Type> choiceType = new ChoiceBox<Wine$Type>(FXCollections.observableArrayList(Wine$Type.values()));
 				choiceType.getSelectionModel().select(getItem().getType());
 				getItem().typeProperty().bind(choiceType.getSelectionModel().selectedItemProperty());
 				choiceTypeListener = new ChoiceTypeListener(choiceType);
 				getItem().typeProperty().addListener(choiceTypeListener);
 				
 				HBox hbox = new HBox();
 				hbox.setSpacing(5.0);
 				hbox.getChildren().add(fieldName);
 				hbox.getChildren().add(fieldYear);
 				hbox.getChildren().add(choiceType);
 				setGraphic(hbox);
 			}
 		}
 		
 		private final static class ChoiceTypeListener implements ChangeListener<Wine$Type> {
 			private ChoiceBox<Wine$Type> choiceBox;
 			
 			public ChoiceTypeListener(ChoiceBox<Wine$Type> choiceBox) {
 				this.choiceBox = choiceBox;
 			}
 			
 			@Override
 			public void changed(ObservableValue<? extends Wine$Type> property,
 					Wine$Type oldValue, Wine$Type newValue) {
 				choiceBox.getSelectionModel().select(newValue);
 			}
 		}
 	}
 }
