 public class HospitalManager {
 
 	private Patient patientListStart = null;
 
 	public static void main(String[] args) {
 		HospitalManager hm = new HospitalManager();
 		hm.init();
 	}
 	void init(){
 		patientListStart = new Patient("Sam", 62);
 		Patient p1 = new Patient("Tim", 29);
 		Patient p2 = new Patient("James", 41);
 		Patient p3 = new Patient("Laura", 45);
 		Patient p4 = new Patient("Ty", 22);
 		Patient p5 = new Patient("Bri", 24);
 		Patient p6 = new Patient("Glenn", 57);
 		Patient p7 = new Patient("Jim", 38);
 		Patient p8 = new Patient("John", 72);
 		Patient p9 = new Patient("Liz", 15);
 		patientListStart.addPatient(p1);
 		patientListStart.addPatient(p2);
 		patientListStart.addPatient(p3);
 		patientListStart.addPatient(p4);
 		patientListStart.addPatient(p5);
 		patientListStart.addPatient(p6);
 		patientListStart.addPatient(p7);
 		patientListStart.addPatient(p8);
 		patientListStart.addPatient(p9);
 
 		patientListStart.traverse();
 		patientListStart.deletePatient(p2);
		patientListStart.deletePatient(p2);
 		patientListStart.traverse();
 	}
 
 }

