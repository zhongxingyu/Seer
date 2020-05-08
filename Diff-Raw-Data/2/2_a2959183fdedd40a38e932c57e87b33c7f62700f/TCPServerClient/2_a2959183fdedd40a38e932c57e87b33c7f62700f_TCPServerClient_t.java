 package backend;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.Socket;
 import java.sql.SQLException;
 
 import shared.*;
 import backend.DAO.*;
 
 public class TCPServerClient extends TCPClient {
 
 	public TCPServerClient(Socket socket) throws IOException {
 		super(socket, true);
 	}
 
 	@Override
 	public void receivedObject(Object obj, TCPClient client) {
 		if (obj instanceof DatabaseControl) {
 			handleDatabaseControl((DatabaseControl) obj, client);
 		}
		else if (obj instanceof RetrievalMessage) {
 			handleRetrievalMessage((RetrievalMessage) obj, client);
 		} else {
 			handleOther(obj, client);
 		}
 	}
 
 	private void handleOther(Object obj, TCPClient client) {
 		System.out.println("Received: " + obj.getClass().getName());
 		client.trySendMessage(new PillMessage(
 				"I don't know how to handle that object ("
 						+ obj.getClass().getName() + ")"));
 	}
 
 	private void handleRetrievalMessage(RetrievalMessage msg, TCPClient client) {
 		System.out.println("Received RetrievalMessage.");
 		try {
 			Serializable returned = null;
 			switch (msg.action) {
 			case DOC_CHECK_LOGIN: {
 				if (msg.obj instanceof Doctor) {
 					Doctor doc = (Doctor) msg.obj;
 					DoctorDAO docDb = new DoctorDAO();
 					if (docDb.isCorrectLogin(doc.username, doc.password)) {
 						returned = docDb.findByUsername(doc.username);
 					} else {
 						throw new IllegalArgumentException("Incorrect login");
 					}
 				} else {
 					throw new IllegalArgumentException(
 							"Please provide a Doctor object to check username, password");
 				}
 				break;
 			}
 			default:{
 				throw new IllegalArgumentException(
 						"I got a correct message, I just don't know what to do with it");
 			}
 			}
 			if (returned != null) {
 				client.trySendMessage(returned);
 			}
 		} catch (IllegalArgumentException ex) {
 			client.trySendMessage(new PillMessage(ex.getMessage(), false));
 		} catch (SQLException ex) {
 			client.trySendMessage(new PillMessage(ex.getMessage(), false));
 		}
 	}
 
 	private void handleDatabaseControl(DatabaseControl control, TCPClient client) {
 		System.out
 				.println("Received DatabaseControl: " + control.action.name());
 		try {
 			Serializable returned = null;
 			switch (control.action) {
 			case CREATE:
 				returned = createObject(control);
 				break;
 			case DELETE:
 				returned = deleteObject(control);
 				break;
 			case UPDATE:
 				returned = updateObject(control);
 				break;
 			}
 
 			if (returned != null) {
 				DatabaseControl sendBack = new DatabaseControl(control.action,
 						returned);
 				client.trySendMessage(sendBack);
 			}
 		} catch (IllegalArgumentException ex) {
 			client.trySendMessage(new PillMessage(ex.getMessage(), false));
 		} catch (SQLException ex) {
 			client.trySendMessage(new PillMessage(ex.getMessage(), false));
 		}
 	}
 
 	private Serializable createObject(DatabaseControl control)
 			throws IllegalArgumentException, SQLException {
 		Serializable obj = control.object;
 		if (obj instanceof Alert) {
 			AlertDAO alertDb = new AlertDAO();
 			return alertDb.insert((Alert) obj);
 		} else if (obj instanceof Doctor) {
 			DoctorDAO doctorDb = new DoctorDAO();
 			Doctor doc = (Doctor) obj;
 			return doctorDb.insert(doc, doc.password);
 		} else if (obj instanceof Patient) {
 			PatientDAO patientDb = new PatientDAO();
 			return patientDb.insert((Patient) obj);
 		} else if (obj instanceof Prescription) {
 			PrescriptionDAO prescriptionDb = new PrescriptionDAO();
 			return prescriptionDb.insert((Prescription) obj);
 		} else if (obj instanceof PrescriptionDateTime) {
 			PrescriptionDateTimeDAO prescriptionDateTimeDb = new PrescriptionDateTimeDAO();
 			return prescriptionDateTimeDb.insert((PrescriptionDateTime) obj);
 		} else
 			throw new IllegalArgumentException(
 					"This object cannot be created in the database");
 	}
 
 	private boolean updateObject(DatabaseControl control)
 			throws IllegalArgumentException, SQLException {
 		Serializable obj = control.object;
 		if (obj instanceof Alert) {
 			AlertDAO alertDb = new AlertDAO();
 			return alertDb.update((Alert) obj);
 		} else if (obj instanceof Doctor) {
 			DoctorDAO doctorDb = new DoctorDAO();
 			return doctorDb.update((Doctor) obj);
 		} else if (obj instanceof Patient) {
 			PatientDAO patientDb = new PatientDAO();
 			return patientDb.update((Patient) obj);
 		} else if (obj instanceof Prescription) {
 			PrescriptionDAO prescriptionDb = new PrescriptionDAO();
 			return prescriptionDb.update((Prescription) obj);
 		} else if (obj instanceof PrescriptionDateTime) {
 			PrescriptionDateTimeDAO prescriptionDateTimeDb = new PrescriptionDateTimeDAO();
 			return prescriptionDateTimeDb.update((PrescriptionDateTime) obj);
 		} else
 			throw new IllegalArgumentException(
 					"This object cannot be updated in the database");
 	}
 
 	private boolean deleteObject(DatabaseControl control)
 			throws IllegalArgumentException, SQLException {
 		Serializable obj = control.object;
 		if (obj instanceof Alert) {
 			AlertDAO alertDb = new AlertDAO();
 			return alertDb.delete((Alert) obj);
 		} else if (obj instanceof Doctor) {
 			DoctorDAO doctorDb = new DoctorDAO();
 			return doctorDb.delete((Doctor) obj);
 		} else if (obj instanceof Patient) {
 			PatientDAO patientDb = new PatientDAO();
 			return patientDb.delete((Patient) obj);
 		} else if (obj instanceof Prescription) {
 			PrescriptionDAO prescriptionDb = new PrescriptionDAO();
 			return prescriptionDb.delete((Prescription) obj);
 		} else if (obj instanceof PrescriptionDateTime) {
 			PrescriptionDateTimeDAO prescriptionDateTimeDb = new PrescriptionDateTimeDAO();
 			return prescriptionDateTimeDb.delete((PrescriptionDateTime) obj);
 		} else
 			throw new IllegalArgumentException(
 					"This object cannot be deleted from the database");
 	}
 }
