 package pms;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import pms.PatientInfo.Gender;
 
 /**
  * Controller for LoginFrame.
  *
  * @author Anthony
  * @version 0.1
  */
 public class LoginController {
 	
 	private LoginFrame loginFrame;
 
 	/**
 	 * Constructs a controller for LoginFrame.
 	 */
 	public LoginController() {
 		loginFrame = new LoginFrame();
 		
 		addListeners();
 		loginFrame.setVisible(true);
 	}
 	
 	private void addListeners() {
 		loginFrame.addChangedListener(new DocumentListener() {
 
 			@Override
 			public void insertUpdate(DocumentEvent e) {
 				stateChanged();
 			}
 
 			@Override
 			public void removeUpdate(DocumentEvent e) {
 				stateChanged();
 			}
 
 			@Override
 			public void changedUpdate(DocumentEvent e) {
 				stateChanged();
 			}
 			
 			private void stateChanged() {
 				String name = loginFrame.getPatientName();
 				String address = loginFrame.getPatientAddress();
 				String patientNumber = loginFrame.getPatientNumber();
 				Gender gender = loginFrame.getPatientGender();
 				String age = loginFrame.getPatientAge();
 				String weight = loginFrame.getPatientWeight();
 				String height = loginFrame.getPatientHeight();
 				String condition = loginFrame.getPatientSpecificCondition();
 				
 				if(!name.isEmpty()
 					&& !address.isEmpty()
 					&& !patientNumber.isEmpty()
 					&& gender != null
 					&& age.matches("[1-9][0-9]{0,2}")
 					&& !condition.isEmpty()) {
 					try {
 						if(Double.parseDouble(weight) > 0
 							&& Double.parseDouble(weight) < 1000
 							&& Double.parseDouble(height) > 0
 							&& Double.parseDouble(height) < 1000) {
 							loginFrame.setOkButtonEnabled(true);
 							return;
 						}
 					}
 					catch (NumberFormatException ex) {}
 				}
 				loginFrame.setOkButtonEnabled(false);
 			}
 			
 		});
 		
 		loginFrame.addSafeLimitsChangedListener(new DocumentListener() {
 
 			@Override
 			public void insertUpdate(DocumentEvent e) {
 				stateChanged();
 			}
 
 			@Override
 			public void removeUpdate(DocumentEvent e) {
 				stateChanged();
 			}
 
 			@Override
 			public void changedUpdate(DocumentEvent e) {
 				stateChanged();
 			}
 			
 			private void stateChanged() {
 				String lowHeartRate = loginFrame.getLowHeartRate();
 				String highHeartRate = loginFrame.getHighHeartRate();
 				String lowBodyTemperature = loginFrame.getLowBodyTemperature();
 				String highBodyTemperature = loginFrame.getHighBodyTemperature();
 				String lowLowBloodPressure = loginFrame.getLowLowBloodPressure();
 				String lowHighBloodPressure = loginFrame.getLowHighBloodPressure();
 				String highLowBloodPressure = loginFrame.getHighLowBloodPressure();
 				String highHighBloodPressure = loginFrame.getHighHighBloodPressure();
 				String lowBloodGlucose = loginFrame.getLowBloodGlucose();
 				String highBloodGlucose = loginFrame.getHighBloodGlucose();
 				
 				if(!lowHeartRate.isEmpty()
 					&& !highHeartRate.isEmpty()
 					&& !lowBodyTemperature.isEmpty()
 					&& !highBodyTemperature.isEmpty()
 					&& !lowLowBloodPressure.isEmpty()
 					&& !lowHighBloodPressure.isEmpty()
 					&& !highLowBloodPressure.isEmpty()
 					&& !highHighBloodPressure.isEmpty()
 					&& !lowBloodGlucose.isEmpty()
 					&& !highBloodGlucose.isEmpty()) {
 					try {
 						if(Double.parseDouble(lowHeartRate) > 0
 							&& Double.parseDouble(lowHeartRate) < 
 							Double.parseDouble(highHeartRate)
 							&& Double.parseDouble(lowBodyTemperature) > 0
 							&& Double.parseDouble(lowBodyTemperature) < 
 							Double.parseDouble(highBodyTemperature)
 							&& Double.parseDouble(lowLowBloodPressure) > 0
 							&& Double.parseDouble(lowLowBloodPressure) < 
 							Double.parseDouble(lowHighBloodPressure)
 							&& Double.parseDouble(highLowBloodPressure) > 0
 							&& Double.parseDouble(highLowBloodPressure) < 
 							Double.parseDouble(highHighBloodPressure)
 							&& Double.parseDouble(lowLowBloodPressure) < 
 							Double.parseDouble(highLowBloodPressure)
 							&& Double.parseDouble(lowHighBloodPressure) < 
 							Double.parseDouble(highHighBloodPressure)
 							&& Double.parseDouble(lowBloodGlucose) > 0
 							&& Double.parseDouble(lowBloodGlucose) < 
 							Double.parseDouble(highBloodGlucose)) {
 							loginFrame.setSafeLimitsOkButtonEnabled(true);
 							return;
 						}
 					}
 					catch (NumberFormatException ex) {
 						int o = 0;
 					}
 				}
 				loginFrame.setSafeLimitsOkButtonEnabled(false);
 			}
 		});
 		
 		loginFrame.addOkButtonActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String name = loginFrame.getPatientName();
 				String address = loginFrame.getPatientAddress();
 				String patientNumber = loginFrame.getPatientNumber();
 				Gender gender = loginFrame.getPatientGender();
 				int age = Integer.parseInt(loginFrame.getPatientAge());
 				Double weight = Double.parseDouble(loginFrame.getPatientWeight());
 				Double height = Double.parseDouble(loginFrame.getPatientHeight());
 				String condition = loginFrame.getPatientSpecificCondition();
 				
 				String lowHeartRate = loginFrame.getLowHeartRate();
 				String highHeartRate = loginFrame.getHighHeartRate();
 				String lowBodyTemperature = loginFrame.getLowBodyTemperature();
 				String highBodyTemperature = loginFrame.getHighBodyTemperature();
 				String lowLowBloodPressure = loginFrame.getLowLowBloodPressure();
 				String lowHighBloodPressure = loginFrame.getLowHighBloodPressure();
 				String highLowBloodPressure = loginFrame.getHighLowBloodPressure();
 				String highHighBloodPressure = loginFrame.getHighHighBloodPressure();
 				String lowBloodGlucose = loginFrame.getLowBloodGlucose();
 				String highBloodGlucose = loginFrame.getHighBloodGlucose();
 				
 				loginFrame.dispose();
 				
 				new Monitor(new PatientInfo(
 					name,
 					address,
 					patientNumber,
 					age,
 					gender,
 					weight,
 					height,
 					new SafeLimit(
					Double.parseDouble(lowHeartRate),
 					Double.parseDouble(highHeartRate),
					Double.parseDouble(lowBodyTemperature),
 					Double.parseDouble(highBodyTemperature),
					Double.parseDouble(lowLowBloodPressure),
					Double.parseDouble(lowHighBloodPressure),
					Double.parseDouble(highLowBloodPressure),
					Double.parseDouble(highHighBloodPressure),
					Double.parseDouble(lowBloodGlucose),
					Double.parseDouble(highBloodGlucose)
 					),
 					condition
 					));
 			}
 		});
 		
 		loginFrame.addSafeLimitsButtonListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				loginFrame.setSafeLimitsDialogVisiable(true);
 			}
 		});
 		
 		loginFrame.addSafeLimitsOkButtonListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				loginFrame.setSafeLimitsDialogVisiable(false);
 			}
 		});
 	}
 }
