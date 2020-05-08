 package sk.gista.medobs;
 
 public class Reservation {
 
 	public enum Status {
 		disabled(1),
 		enabled(2),
 		booked(3),
 		in_held(4);
 		
 		public final int numCode;
 		Status(int numCode) {
 			this.numCode = numCode;
 		}
 		static Status valueOf(int num) {
 			switch (num) {
 			case 1:
 				return disabled;
 			case 2:
 				return enabled;
 			case 3:
 				return booked;
 			case 4:
 				return in_held;
 			default:
 				throw new IndexOutOfBoundsException();
 			}
 		}
 	}
 	
 	private String time;
 	private Status status;
 	private String patient;
 	private String patientPhoneNumber;
 	private String patientEmail;
 	private String bookedBy;
 	private String bookedAt;
	private String examinationKind; 
 	
 	public Reservation(String time, Status status, String patient, String patientPhoneNumber, String patientEmail,
			String bookedBy, String bookedAt, String examinationKind) {
 		this.time = time;
 		this.status = status;
 		this.patient = patient;
 		this.patientPhoneNumber = patientPhoneNumber;
 		this.patientEmail = patientEmail;
 		this.bookedBy = bookedBy;
 		this.bookedAt = bookedAt;
		this.examinationKind = examinationKind;
 	}
 
 	public String getTime() {
 		return time;
 	}
 
 	public Status getStatus() {
 		return status;
 	}
 
 	public String getpatient() {
 		return patient;
 	}
 
 	public String getPatientPhoneNumber() {
 		return patientPhoneNumber;
 	}
 
 	public String getPatientEmail() {
 		return patientEmail;
 	}
 
 	public String getBookedBy() {
 		return bookedBy;
 	}
 
 	public String getBookedAt() {
 		return bookedAt;
 	}

	public String getExaminationKind() {
		return examinationKind;
	}
 }
