 package pl.tzaras.fitness.manager;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import pl.tzaras.fitness.manager.db.DataManager;
 import pl.tzaras.fitness.manager.db.RoomWrapper;
 import pl.tzaras.fitness.manager.db.TrainerWrapper;
 import pl.tzaras.fitness.manager.db.TypeWrapper;
 import pl.tzaras.fitness.manager.db.data.GymClass;
 import pl.tzaras.fitness.manager.db.data.GymTrainer;
 import pl.tzaras.fitness.manager.utils.ManagerUtils;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
 import javafx.scene.control.ComboBox;
 import javafx.scene.control.Label;
 import javafx.scene.control.TextField;
 import javafx.scene.input.KeyCode;
 import javafx.scene.input.KeyEvent;
 import javafx.scene.input.MouseEvent;
 import javafx.stage.Stage;
 
 public class EditDialogController  implements Initializable  {
 
 	public static final int CANCEL_BUTTON = 1;
 	public static final int OK_BUTTON = 0;
 
 	Stage dialogStage;
 	
 	@FXML private TextField tfDuration;
 	@FXML private TextField tfParticipants;
 	@FXML private TextField tfNumberOfWeeks;
 	
 	@FXML private Label lblNumberOfWeeks;
 	
 	@FXML private ComboBox<TrainerWrapper> cmbTrainer;
 	@FXML private ComboBox<TrainerWrapper> cmbTrainer2;
 	@FXML private ComboBox<TypeWrapper> cmbClassType;
 	@FXML private ComboBox<String> cmbDay;
 	@FXML private ComboBox<RoomWrapper> cmbRoom;
 	@FXML private ComboBox<String> cmbHour;
 	@FXML private ComboBox<String> cmbMinute;
 	
	@FXML private CheckBox chbTrainer2;
	
 	@FXML private Button okButton;
 	
 	private GymClass gymClass;
 
 	private int status = CANCEL_BUTTON;
 	private boolean editMode;
 
 	public void initialize(URL arg0, ResourceBundle arg1) {
 		ManagerUtils.fillWithWeekDays(cmbDay);
 		ManagerUtils.fillWithHours(cmbHour);
 		ManagerUtils.fillWithMinutes(cmbMinute);
 		
 		DataManager.getInstance().getGymTrainerManager().initializeCombo(cmbTrainer);
 		DataManager.getInstance().getGymTrainerManager().initializeCombo(cmbTrainer2);
 		DataManager.getInstance().getGymClassTypeManager().initializeCombo(cmbClassType);
 		DataManager.getInstance().getGymRoomManager().initializeCombo(cmbRoom);
 		tfParticipants.setText("0");
 		okButton.setDisable(true);
 	}
 	
 	@FXML protected void updateClass(MouseEvent event) {
 		
 		tfNumberOfWeeks.setStyle("-fx-border-style: none;");
 		tfParticipants.setStyle("-fx-border-style: none;");
 		tfDuration.setStyle("-fx-border-style: none;");
 		if (!tfDuration.getText().matches("\\d+")) {
 			tfDuration.setStyle("-fx-border-style: solid; -fx-border-color:red;");
 			GUIHelper.cannotDelete("Czas trwania zajęć musi być liczbą");
 		} else if ( !tfParticipants.getText().matches("\\d+") ) {
 			tfParticipants.setStyle("-fx-border-style: solid; -fx-border-color:red;");
 			GUIHelper.cannotDelete("Ilość uczestników musi być liczbą");		
 		} else if ( tfNumberOfWeeks.isVisible() && !tfNumberOfWeeks.getText().matches("\\d+") ) {
 			tfNumberOfWeeks.setStyle("-fx-border-style: solid; -fx-border-color:red");
 			GUIHelper.cannotDelete("Ilość tygodni musi być liczbą");
 		} else 	{
 
 			gymClass.setClassRoom(cmbRoom.getSelectionModel().getSelectedItem()
 					.getRoom());
 			Set<GymTrainer> trainers = new HashSet<GymTrainer>();
 			trainers.add(cmbTrainer.getSelectionModel().getSelectedItem().getGymTrainer());
 			trainers.add(cmbTrainer2.getSelectionModel().getSelectedItem().getGymTrainer());
 			gymClass.setTrainers(trainers);
 			gymClass.setClassType(cmbClassType.getSelectionModel()
 					.getSelectedItem().getClassType());
 			gymClass.setStartTime(gymClass.getStartTime()
 					.withDayOfWeek(ManagerUtils.dayToInt(cmbDay.getValue()))
 					.withHourOfDay(Integer.valueOf(cmbHour.getValue()))
 					.withMinuteOfHour(Integer.valueOf(cmbMinute.getValue())));
 			System.out.println("!!!!!!!!! Start time: " + gymClass.getStartTime().toString());
 			gymClass.setDuration(Long.valueOf(tfDuration.getText()));
 			int participants = tfParticipants.getText().isEmpty() ? 0 : Integer
 					.valueOf(tfParticipants.getText());
 			gymClass.setParticipants(participants);
 
 			dialogStage.close();
 		}
 	}
 	
 	@FXML protected void handleCancel(MouseEvent event) {
 		status = CANCEL_BUTTON;
 		dialogStage.close();
 	}
 	
 	public GymClass getGymClass() {
 		return this.gymClass;
 	}
 	
 	public void setGymClass(GymClass gymClass, boolean initialize) {
 		this.gymClass = gymClass;
 
 		editMode = initialize;
 		if (initialize) {
 			tfNumberOfWeeks.setVisible(false);
 			lblNumberOfWeeks.setVisible(false);
 			Object[] trainers = gymClass.getTrainers().toArray();
 			for (TrainerWrapper tw : cmbTrainer.getItems()) {
 				if (tw.getGymTrainer().equals(trainers[0])) {
 					cmbTrainer.getSelectionModel().select(tw);
 					break;
 				}
 			}
 
 			for (TrainerWrapper tw : cmbTrainer2.getItems()) {
 				if (tw.getGymTrainer().equals(trainers[0])) {
 					cmbTrainer2.getSelectionModel().select(tw);
 					break;
 				}
 			}
 			
 			for (RoomWrapper rw : cmbRoom.getItems()) {
 				if (rw.getRoom().getID() == gymClass.getClassRoom().getID()) {
 					cmbRoom.getSelectionModel().select(rw);
 					break;
 				}
 			}
 			for (TypeWrapper tw : cmbClassType.getItems()) {
 				if (tw.getClassType().getClassId() == gymClass.getClassType()
 						.getClassId()) {
 					cmbClassType.getSelectionModel().select(tw);
 					break;
 				}
 			}
 
 			cmbDay.getSelectionModel().select(
 					ManagerUtils.intToWeekDay(gymClass.getStartTime()
 							.getDayOfWeek()));
 			int hour = gymClass.getStartTime().getHourOfDay();
 			cmbHour.getSelectionModel().select(
 					hour < 10 ? "0"+String.valueOf(hour) : String.valueOf(hour));
 			int minute = gymClass.getStartTime().getMinuteOfHour();
 			cmbMinute.getSelectionModel().select(
 					minute < 10 ? "0"+String.valueOf(minute) : String.valueOf(minute));
 			tfDuration.setText(String.valueOf(gymClass.getDuration()));
 			tfParticipants.setText(String.valueOf(gymClass.getParticipants()));
 		} else {
 			tfNumberOfWeeks.setVisible(true);
 			lblNumberOfWeeks.setVisible(true);
 		}
 	}
 
 	public void setEditDialogStage(Stage stage) {
 		this.dialogStage = stage;
 	}
 	
 	@FXML protected void updateDialog(ActionEvent event) {
 		status = OK_BUTTON;
 		updatedOkButton();;
 	}
 	
 	@FXML protected void updateDialogOnKey(KeyEvent event) {
 		System.out.println(event.toString());
 			if ( event.getSource().equals(tfParticipants) && editMode ) {
 				okButton.setDisable(false);
 			} else {
 				if ( cmbTrainer.getSelectionModel().getSelectedItem() != null
 						&& cmbClassType.getSelectionModel().getSelectedItem() != null
 						&& cmbDay.getSelectionModel().getSelectedItem() != null
 						&& cmbRoom.getSelectionModel().getSelectedItem() != null
 						&& cmbHour.getSelectionModel().getSelectedItem() != null
 						&& cmbMinute.getSelectionModel().getSelectedItem() != null
 						&& (!tfDuration.getText().isEmpty() ? tfDuration.getText().matches("\\d+") : true)
 						&& (event.getCharacter() != KeyEvent.CHAR_UNDEFINED ? event.getCharacter().matches("\\d") : true) ) {
 					if (!tfParticipants.getText().isEmpty() && !tfParticipants.getText().matches("\\d+")) {
 						okButton.setDisable(true);
 					} else {
 						okButton.setDisable(false);
 					}
 				} else {
 					
 					okButton.setDisable(true);
 				}
 			}
 	}
 
 	private void updatedOkButton() {
 		if ( cmbTrainer.getSelectionModel().getSelectedItem() != null &&
 				cmbClassType.getSelectionModel().getSelectedItem() != null &&
 				cmbDay.getSelectionModel().getSelectedItem() != null &&
 				cmbRoom.getSelectionModel().getSelectedItem() != null &&
 				cmbHour.getSelectionModel().getSelectedItem() != null &&
 				cmbMinute.getSelectionModel().getSelectedItem() != null) {
 			okButton.setDisable(false);
 		} else {
 			okButton.setDisable(true);
 		}
 	}
 
 	public int getStatus() {
 		return status ;
 	}
 
 	public int getRepetition() {
 		return Integer.valueOf(tfNumberOfWeeks.getText());
 	}
	
	@FXML protected void enableWidget(ActionEvent event) {
		cmbTrainer2.setDisable(!chbTrainer2.isSelected());
	}
 }
