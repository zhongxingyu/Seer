 package control;
 
 import java.util.ArrayList;
 
 import model.Begeleider;
 import model.Opdracht;
import model.Opdrachtgever;
 import model.Sollicitatie;
 import model.Student;
 
 public class Controller {
 	private static OpdrachtDAO opdrachtDAO;
 	private static SollicitatieDAO sollicitatieDAO;
 	private static BegeleiderDAO begeledierDAO;
 	private static StudentDAO studentDAO;
 	private static DbDAO dbDAO;
 
 	// voorlopig, we snappen de constructor niet. Staat toch ook al in dbdao?
 	// private OpdrachtDAO opdrachtDAO = new OpdrachtDAO();
 	/*
 	 * //debug studenten static Student s1 = new Student("Pietje Puk",
 	 * Long.parseLong("06123456788"), "Pietje.puk@student.hu.nl"); static
 	 * Student s2 = new Student("Henk de Vries", Long.parseLong("0182123456"),
 	 * "henk@devries.eu"); static Student s3 = new Student("Sander Soeters",
 	 * Long.parseLong("09064623"), "ultrabaas@student.hu.nl");
 	 * 
 	 * //debug begeleiders Begeleider b1 = new Begeleider("Aad Dis",
 	 * "030123456", "Aad.dis@hu.nl"); Begeleider b2 = new
 	 * Begeleider("Jeroen Weber", "030129636", "jeroen.weber@hu.nl"); Begeleider
 	 * b3 = new Begeleider("Nini Salet", "020749294", "nini.salet@hu.nl");
 	 * 
 	 * //debug opdrachtgevers Opdrachtgever og1 = new
 	 * Opdrachtgever("Jopie Boer", "Kaaslaan 4", "06134752",
 	 * "jopie.boer@autototaaldiensten.nl", "autototaaldiensten.nl");
 	 * Opdrachtgever og2 = new Opdrachtgever("VingerFriet", "Oudenoord 86",
 	 * "0618376459", "Vinger@friet.nl", "friet.nl"); Opdrachtgever og3 = new
 	 * Opdrachtgever("Martijn Cornips", "Onder de Brug", "061293753",
 	 * "martijn@cornips.nl", "corni.ps"); public Controller() {
 	 * studenten.add(s1); studenten.add(s2); studenten.add(s3);
 	 * 
 	 * begeleiders.add(b1); begeleiders.add(b2); begeleiders.add(b3);
 	 * b3.setCoordinator(true);
 	 * 
 	 * opdrachtgevers.add(og1); opdrachtgevers.add(og2);
 	 * opdrachtgevers.add(og3); }
 	 */
 
 	public static void addOpdracht(String opdrachtNaam,
 			String opdrachtToelichting, int minPer, int maxPer,
 			Boolean SolNodig, String opdrachtFile, int opdrachtgeverID,
 			int begeleiderID) {
 
 		opdrachtDAO.insertOpdracht(opdrachtNaam, opdrachtToelichting, minPer,
 				maxPer, SolNodig, opdrachtFile, opdrachtgeverID, begeleiderID);
 
 	}
 
 	public static void addSollicitatie(int opdrachtID, int studentID, String toelichting) {
 		SollicitatieDAO sol = new SollicitatieDAO();
 		System.out.println("addSollicitatie init");
 		System.out.println("===Controller===");
 		System.out.println(toelichting);
 		System.out.println(opdrachtID);
 		System.out.println(studentID);
 		System.out.println("===");
 		sol.insertSollicitatie(toelichting, studentID, opdrachtID);
 		System.out.println("addSollicitatie finish");
 
 	}
 
 	public static void goedkeurSollicitatie(Sollicitatie sollicitatie) {
 		// TODO code logic voor verplaatsen sollicitatie naar
 		// koppelOpdrachttabel
 	}
 
 	public static void goedkeurOpdracht(Opdracht Opdracht) {
 		// TODO code logic mysql record in opdracht isGoedgekeurd
 	}
 
 	public static void koppelOpdrachtStudentDocent(int opdrachtID,
 			int studentID, int docentID) {
 		// TODO code logic begeleider plaatsen in opdracht
 	}
 
 	public static ArrayList<Opdracht> getAllOpdrachten() {
 		// TODO get allOpdrachten in overzicht
			OpdrachtDAO opd = new OpdrachtDAO();
			return opd.getAllOpdrachten();
 
 	}
 
 	public static ArrayList<Student> getAllStudenten() {
 		StudentDAO stud = new StudentDAO();
 		return stud.getAllStudenten();
 	}
 
	public static ArrayList<Opdrachtgever> getAllOpdrachtgever() {
		OpdrachtgeverDAO opd = new OpdrachtgeverDAO();
		return opd.getAllOpdrachtgever();
 
 	}
 
 	public static ArrayList<Sollicitatie> getAllSollicitatie() {
		SollicitatieDAO soll = new SollicitatieDAO();
		return soll.getAllSollicitatie();
 
 	}
 
 	public static ArrayList<Begeleider> getAllBegeleiders() {
		BegeleiderDAO beg = new BegeleiderDAO();
		return beg.getAllBegeleider();
 
 	}
 
 	public static void getAllCoordinators() {
 		// TODO getAll overzicht
 
 	}
 
 }
