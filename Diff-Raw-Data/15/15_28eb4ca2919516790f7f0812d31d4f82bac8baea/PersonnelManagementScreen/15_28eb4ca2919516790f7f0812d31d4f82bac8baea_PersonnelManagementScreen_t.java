 package com.newdawn.gui.personnel;
 
 import java.net.URL;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Spliterator;
 import java.util.function.Predicate;
 
 import javafx.beans.binding.Bindings;
 import javafx.beans.binding.IntegerBinding;
 import javafx.beans.binding.ListBinding;
 import javafx.beans.binding.ObjectBinding;
 import javafx.beans.binding.StringExpression;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.collections.ListChangeListener;
 import javafx.collections.MapChangeListener;
 import javafx.collections.ObservableList;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.CheckBox;
 import javafx.scene.control.ComboBox;
 import javafx.scene.control.ListCell;
 import javafx.scene.control.ListView;
 import javafx.scene.control.MenuItem;
 import javafx.scene.control.TableColumn;
 import javafx.scene.control.TableColumn.CellDataFeatures;
 import javafx.scene.control.TableView;
 import javafx.scene.control.TextField;
 import javafx.scene.control.cell.PropertyValueFactory;
 import javafx.scene.layout.AnchorPane;
 import javafx.util.Callback;
 
 import javax.annotation.Resource;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.newdawn.controllers.GameData;
 import com.newdawn.controllers.OfficialsController;
 import com.newdawn.controllers.TeamController;
 import com.newdawn.controllers.TeamController.FieldTeamType;
 import com.newdawn.gui.PropertyListCellFactory;
 import com.newdawn.model.personnel.CivilianAdministrators;
 import com.newdawn.model.personnel.GroundForceOfficers;
 import com.newdawn.model.personnel.NavalOfficer;
 import com.newdawn.model.personnel.Official;
 import com.newdawn.model.personnel.PersonnelAssignment;
 import com.newdawn.model.personnel.Scientist;
 import com.newdawn.model.personnel.Skill;
 import com.newdawn.model.personnel.SkillLevel;
 import com.sun.javafx.collections.CompositeMatcher;
 
 /**
  * 
  * @author Pierrick Puimean-Chieze
  */
 @Component
 public class PersonnelManagementScreen implements Initializable {
 	@Autowired
 	private GameData gameData;
 	private final MapChangeListener<Skill, SkillLevel> mapChangeListener = new MapChangeListener<Skill, SkillLevel>() {
 		@Override
 		public void onChanged(
 				MapChangeListener.Change<? extends Skill, ? extends SkillLevel> arg0) {
 			updateSkills();
 		}
 	};
 	@Autowired
 	private Skill[] skills;
 	@Resource
 	private OfficialsController officialsController;
 	@Autowired
 	private TeamController teamController;
 	@Autowired
 	private AssignementFilter assignementFilters;
 	private ObservableList<Official> officialFilteredList = FXCollections
 			.observableArrayList();
 	private final CompositeMatcher<Official> compositeMatcher = new CompositeMatcher<>();
 	private final CompositeMatcher<Official> skillFiltersMatcher = new CompositeMatcher<>();
 	private final Predicate<Official> typeOfficialMatcher = new Predicate<Official>() {
 
 		@Override
 		public boolean test(Official e) {
 			return (e instanceof Scientist && scientistFilterCheckBox
 					.isSelected())
 					|| (e instanceof NavalOfficer && navalOfficerFilterCheckBox
 							.isSelected())
 					|| (e instanceof GroundForceOfficers && groundOfficerFilterCheckBox
 							.isSelected())
 					|| (e instanceof CivilianAdministrators && civilianAdministratorCheckBox
 							.isSelected());
 		}
 	};
 
 	@FXML
 	// fx:id="assignOfficialMenuItem"
 	private MenuItem assignOfficialMenuItem; // Value injected by FXMLLoader
 
 	@FXML
 	private Map<Skill, TableColumn<Official, Number>> skillFiltersColumn = new HashMap<>();
 	@FXML
 	// fx:id="civilianAdministratorCheckBox"
 	private CheckBox civilianAdministratorCheckBox;
 	@FXML
 	// fx:id="filteredPersonnelTableView"
 	private TableView<Official> officialsFilteredTableView; // Value injected by
 															// FXMLLoader
 	@FXML
 	// fx:id="groundOfficerFilterCheckBox"
 	private CheckBox groundOfficerFilterCheckBox; // Value injected by
 													// FXMLLoader
 	@FXML
 	// fx:id="navalOfficerFilterCheckBox"
 	private CheckBox navalOfficerFilterCheckBox; // Value injected by FXMLLoader
 	@FXML
 	// fx:id="personnelNameColumn"
 	private TableColumn<Official, String> personnelNameColumn; // Value injected
 																// by FXMLLoader
 	@FXML
 	// fx:id="personnelRankColumn"
 	private TableColumn<Official, String> personnelRankColumn; // Value injected
 																// by FXMLLoader
 	@FXML
 	// fx:id="scientistFilterCheckBox"
 	private CheckBox scientistFilterCheckBox; // Value injected by FXMLLoader
 	@FXML
 	// fx:id="skillFilterComboBox"
 	private ComboBox<Skill> skillFilterComboBox; // Value injected by FXMLLoader
 	@FXML
 	// fx:id="skillFilterMaxValueColumn"
 	private TableColumn<SkillFilter, Integer> skillFilterMaxValueColumn; // Value
 																		// injected
 																		// by
 																		// FXMLLoader
 	@FXML
 	// fx:id="skillFilterMaxValueComponent"
 	private TextField skillFilterMaxValueComponent; // Value injected by
 													// FXMLLoader
 	@FXML
 	// fx:id="skillFilterMinValueColumn"
 	private TableColumn<SkillFilter, Integer> skillFilterMinValueColumn; // Value
 																		// injected
 																		// by
 																		// FXMLLoader
 	@FXML
 	// fx:id="skillFilterMinValueComponent"
 	private TextField skillFilterMinValueComponent; // Value injected by
 													// FXMLLoader
 	@FXML
 	// fx:id="skillFiltersNameColumn"
 	private TableColumn<SkillFilter, String> skillFiltersNameColumn; // Value
 																		// injected
 																		// by
 																		// FXMLLoader
 	// TODO make this table editable
 	// TODO Allow the users to delete a filter
 	@FXML
 	// fx:id="skillFiltersTableView"
 	private TableView<SkillFilter> skillFiltersTableView; // Value injected by
 															// FXMLLoader
 	@FXML
 	// fx:id="detailsPane"
 	private AnchorPane detailsPane; // Value injected by FXMLLoader
 	@FXML
 	// fx:id="detailsPane"
 	private PersonnelDetailsPane detailsPaneController; // Value injected by
 														// FXMLLoader
 														// @FXML
 	// TODO implements the filter
 	@FXML
 	private ComboBox<AssignementFilter> assignmentsFilterComboBox;
 	// TODO correct variable name
 	@FXML
 	private ListView<PersonnelAssignment> assigmentsListView;
 	@FXML
 	// fx:id="createTeamMenuItem"
 	private MenuItem createTeamMenuItem; // Value injected by FXMLLoader
 
 	@FXML
 	private TableColumn<SkillLevel, String> skillColumn;
 	@FXML
 	private TableView<SkillLevel> skillsTableView;
 	@FXML
 	private TableColumn<SkillLevel, Integer> levelColumn;
 
 	// Handler for MenuItem[fx:id="assignOfficialMenuItem"] onAction
 	public void assignOfficial(ActionEvent event) {
 		// TODO add assertions to be sure that no null value are passed to the
 		// controller
 		officialsController.assignOfficialTo(officialsFilteredTableView
 				.getSelectionModel().getSelectedItem(), assigmentsListView
 				.getSelectionModel().getSelectedItem());
 	}
 
 	@Override
 	// This method is called by the FXMLLoader when initialization is complete
 	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
 		assert civilianAdministratorCheckBox != null : "fx:id=\"civilianAdministratorCheckBox\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 		assert officialsFilteredTableView != null : "fx:id=\"filteredPersonnelTableView\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 		assert groundOfficerFilterCheckBox != null : "fx:id=\"groundOfficerFilterCheckBox\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 		assert navalOfficerFilterCheckBox != null : "fx:id=\"navalOfficerFilterCheckBox\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 		assert personnelNameColumn != null : "fx:id=\"personnelNameColumn\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 		assert personnelRankColumn != null : "fx:id=\"personnelRankColumn\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 
 		assert scientistFilterCheckBox != null : "fx:id=\"scientistFilterCheckBox\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 		assert skillFilterComboBox != null : "fx:id=\"skillFilterComboBox\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 		assert skillFilterMaxValueColumn != null : "fx:id=\"skillFilterMaxValueColumn\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 		assert skillFilterMaxValueComponent != null : "fx:id=\"skillFilterMaxValueComponent\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 		assert skillFilterMinValueColumn != null : "fx:id=\"skillFilterMinValueColumn\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 		assert skillFilterMinValueComponent != null : "fx:id=\"skillFilterMinValueComponent\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 		assert skillFiltersNameColumn != null : "fx:id=\"skillFiltersNameColumn\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 		assert skillFiltersTableView != null : "fx:id=\"skillFiltersTableView\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 		assert assignmentsFilterComboBox != null;
 		assert assigmentsListView != null;
 		assert createTeamMenuItem != null : "fx:id=\"createTeamMenuItem\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 		assert assignOfficialMenuItem != null;
 		assert skillsTableView != null : "fx:id=\"skillsTableView\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 		assert skillColumn != null : "fx:id=\"skillColumn\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 		assert levelColumn != null : "fx:id=\"levelColumn\" was not injected: check your FXML file 'PersonnelManagementScreen.fxml'.";
 		// initialize your logic here: all @FXML variables will have been
 		// injected
 		initializeCompositeMatcher();
 
 		initializeFilteredPersonnelTableView();
 
 		initializeSkillFilterTab();
 
 		initializeAssignmentListViewAndFilters();
 
 		createTeamMenuItem.disableProperty().bind(
 				Bindings.select(
 						officialsFilteredTableView.selectionModelProperty(),
 						"selectedItem").isNull());
 		
 		final ObjectBinding<Official> selectedPersonnel = Bindings.select(
 				officialsFilteredTableView.selectionModelProperty(),
 				"selectedItem");
 		
		detailsPaneController.officialProperty().bind(officialsFilteredTableView.getSelectionModel().selectedItemProperty());
 		skillColumn
 				.setCellValueFactory(new Callback<CellDataFeatures<SkillLevel, String>, ObservableValue<String>>() {
 					public ObservableValue<String> call(
 							CellDataFeatures<SkillLevel, String> p) {
 						SkillLevel value = p.getValue();
 						return Bindings.selectString(value, "skill", "name");
 					}
 				});
 		levelColumn
 				.setCellValueFactory(new PropertyValueFactory<SkillLevel, Integer>(
 						"level"));
 
 		selectedPersonnel.addListener(new ChangeListener<Official>() {
 			@Override
 			public void changed(ObservableValue<? extends Official> arg0,
 					Official arg1, Official arg2) {
 				updateSkills();
 			}
 		});
 
 		updateFilters(null);
 	}
 
 	private void initializeAssignmentListViewAndFilters() {
 		final ListBinding<PersonnelAssignment> filteredAssignementBinding = new ListBinding<PersonnelAssignment>() {
 			final ObjectBinding<ObservableList<PersonnelAssignment>> internalBinding = Bindings
 					.select(assignmentsFilterComboBox.getSelectionModel()
 							.selectedItemProperty(), "assignments");
 			{
 				bind(internalBinding);
 			}
 
 			@Override
 			protected ObservableList<PersonnelAssignment> computeValue() {
 				return internalBinding.get();
 			}
 
 			@Override
 			public Spliterator<PersonnelAssignment> spliterator() {
 				return super.spliterator();
 			}
 		};
 
 		assigmentsListView.itemsProperty().bind(filteredAssignementBinding);
 		assigmentsListView
 				.setCellFactory(new PropertyListCellFactory<PersonnelAssignment>(
 						"name", null));
 
 		assignmentsFilterComboBox.getItems().clear();
 		assignmentsFilterComboBox.getItems().addAll(assignementFilters);
 		assignmentsFilterComboBox
 				.setCellFactory(new PropertyListCellFactory<AssignementFilter>(
 						"name", null));
 		assignmentsFilterComboBox
 				.setButtonCell(new ListCell<AssignementFilter>() {
 					@Override
 					protected void updateItem(AssignementFilter item,
 							boolean empty) {
 						super.updateItem(item, empty);
 						if (textProperty().isBound()) {
 							textProperty().unbind();
 						}
 						if (item != null || !empty) {
 							textProperty().bind(item.nameProperty());
 						}
 					}
 				});
 	}
 
 	private void initializeSkillFilterTab() {
 		skillFilterComboBox.getItems().addAll(skills);
 		PropertyListCellFactory<Skill> propertyListCellFactory = new PropertyListCellFactory<>(
 				"name", null);
 		skillFilterComboBox.setButtonCell(new ListCell<Skill>() {
 			@Override
 			protected void updateItem(Skill arg0, boolean arg1) {
 				super.updateItem(arg0, arg1);
 				if (arg1 || arg0 == null) {
 					this.setText(null);
 				} else {
 					this.setText(arg0.getName());
 				}
 			}
 		});
 		skillFilterComboBox.setCellFactory(propertyListCellFactory);
 		skillFiltersNameColumn
 				.setCellValueFactory(new Callback<CellDataFeatures<SkillFilter, String>, ObservableValue<String>>() {
 					@Override
 					public ObservableValue<String> call(
 							CellDataFeatures<SkillFilter, String> arg0) {
 						return Bindings.selectString(arg0.getValue()
 								.skillProperty(), "name");
 					}
 				});
 	
 		skillFilterMinValueColumn
 				.setCellValueFactory(new PropertyValueFactory<SkillFilter, Integer>("minValue"));
 		skillFilterMaxValueColumn
 				.setCellValueFactory(new PropertyValueFactory<SkillFilter, Integer>("maxValue"));
 		skillFiltersTableView.getItems().addListener(
 				new ListChangeListener<SkillFilter>() {
 					@Override
 					public void onChanged(Change<? extends SkillFilter> arg0) {
 						while (arg0.next()) {
 							for (SkillFilter skillFilter : arg0
 									.getAddedSubList()) {
 								final Skill skill = skillFilter.getSkill();
 								TableColumn<Official, Number> skillColumn = new TableColumn<>(
 										skill.getName());
 								skillColumn
 										.setCellValueFactory(new Callback<CellDataFeatures<Official, Number>, ObservableValue<Number>>() {
 											@Override
 											public ObservableValue<Number> call(
 													CellDataFeatures<Official, Number> arg0) {
 												Official official = arg0
 														.getValue();
 												IntegerBinding selectInteger = Bindings
 														.selectInteger(
 																official.skillLevelsProperty()
 																		.valueAt(
 																				skill),
 																"level");
 												selectInteger
 														.addListener(new ChangeListener<Number>() {
 															@Override
 															public void changed(
 																	ObservableValue<? extends Number> arg0,
 																	Number arg1,
 																	Number arg2) {
 																updateFilters(null);
 															}
 														});
 												return selectInteger;
 											}
 										});
 								officialsFilteredTableView.getColumns().add(
 										skillColumn);
 							}
 
 							for (SkillFilter skillFilter : arg0.getRemoved()) {
 								final Skill skill = skillFilter.getSkill();
 								TableColumn<Official, Number> column = skillFiltersColumn
 										.remove(skill);
 								if (column != null) {
 									officialsFilteredTableView.getColumns()
 											.remove(column);
 								}
 							}
 						}
 					}
 				});
 		Bindings.bindContent(skillFiltersMatcher.getMatchers(),
 				skillFiltersTableView.getItems());
 	}
 
 	private void initializeFilteredPersonnelTableView() {
 		personnelNameColumn
 				.setCellValueFactory(new PropertyValueFactory<Official, String>(
 						"name"));
 		personnelRankColumn
 				.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Official, String>, ObservableValue<String>>() {
 					@Override
 					public ObservableValue<String> call(
 							CellDataFeatures<Official, String> arg0) {
 						final Official official = arg0.getValue();
 						final StringExpression concat = Bindings.selectString(
 								official.rankProperty(), "designation");
 						return concat;
 					}
 				});
 		Bindings.bindContent(officialsFilteredTableView.getItems(),
 				officialFilteredList);
 		gameData.getOfficials().addListener(new ListChangeListener<Official>() {
 			@Override
 			public void onChanged(Change<? extends Official> arg0) {
 				updateFilters(null);
 			}
 		});
 
 		// TODO initialize assignOfficialMenuItem to be sure that it's not
 		// enable if not correct values are selected
 	}
 
 	private void initializeCompositeMatcher() {
 		compositeMatcher.getMatchers().add(typeOfficialMatcher);
 		compositeMatcher.getMatchers().add(skillFiltersMatcher);
 	}
 
 	// TODO Try to use a filtered List
 	public void updateFilters(ActionEvent event) {
 		for (Official official : officialFilteredList) {
 			official.getSkillLevels().removeListener(mapChangeListener);
 		}
 		officialFilteredList.clear();
 		for (Official official : gameData.getOfficials()) {
 			if (compositeMatcher.test(official)) {
 				officialFilteredList.add(official);
 
 				official.getSkillLevels().addListener(mapChangeListener);
 
 			}
 		}
 		updateSkills();
 	}
 
 	public void addSkillFilter(ActionEvent event) {
 		final Skill skill = skillFilterComboBox.getSelectionModel()
 				.getSelectedItem();
 		SkillFilter skillFilter = new SkillFilter();
 		skillFilter.setSkill(skill);
 		String minValueText = skillFilterMinValueComponent.getText();
 		skillFilter
 				.setMinValue((minValueText == null || minValueText.isEmpty()) ? null
 						: Integer.parseInt(minValueText));
 		String maxValueText = skillFilterMaxValueComponent.getText();
 		skillFilter
 				.setMaxValue((maxValueText == null || maxValueText.isEmpty()) ? null
 						: Integer.parseInt(maxValueText));
 		skillFiltersTableView.getItems().add(skillFilter);
 		compositeMatcher.getMatchers().add(skillFilter);
 		updateFilters(event);
 	}
 
 	// TODO try to use a binding
 	private void updateSkills() {
 		skillsTableView.getItems().clear();
 		Official selectedMember = officialsFilteredTableView
 				.getSelectionModel().getSelectedItem();
 		if (selectedMember != null) {
 			skillsTableView.getItems().addAll(
 					selectedMember.getSkillLevels().values());
 			Collections.sort(skillsTableView.getItems(),
 					new Comparator<SkillLevel>() {
 						@Override
 						public int compare(SkillLevel arg0, SkillLevel arg1) {
 							return arg0.getSkill().getName()
 									.compareTo(arg1.getSkill().getName());
 						}
 					});
 		}
 	}
 
 	public void createTeam(ActionEvent event) {
 		Official selectedPersonnel = officialsFilteredTableView
 				.getSelectionModel().getSelectedItem();
 		assert selectedPersonnel != null;
 		teamController.createTeamWithLeader(selectedPersonnel,
 				FieldTeamType.GEOLOGICAL);
 	}
 }
