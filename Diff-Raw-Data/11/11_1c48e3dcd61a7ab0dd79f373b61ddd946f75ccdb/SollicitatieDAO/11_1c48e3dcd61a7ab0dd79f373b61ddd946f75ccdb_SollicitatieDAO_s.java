 package control;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import model.Opdracht;
 import model.Sollicitatie;
 import model.Student;
 
 public class SollicitatieDAO extends DbDAO {
 	public SollicitatieDAO() {
 		super();
 	}
 
 	public ArrayList<Sollicitatie> getAllSollicitatie() {
 		makeConnection();
 		StudentDAO studentDAO = new StudentDAO();
 		OpdrachtDAO opdrachtDAO = new OpdrachtDAO();
 		makeResultSet("select * from `Begeleider`");
 		ArrayList<Sollicitatie> sollicitatie = new ArrayList<Sollicitatie>();
 		try {
 
 			while (rst.next()) {
 				String toelichting = rst.getString("toelichting");
 				int studentID = rst.getInt("studentID");
 				int opdrachtID = rst.getInt("opdrachtID");
 
 				Student student = studentDAO.getStudentByID(studentID);
 				Opdracht opdracht = opdrachtDAO.getOpdrachtByID(opdrachtID);
 				Sollicitatie tempSollicitatie = new Sollicitatie(toelichting);
 				sollicitatie.add(tempSollicitatie);
 				// student.addSollicitatie(tempSollicitatie);
 
 			}
 
 		} catch (SQLException e) {
 
 			e.printStackTrace();
 		}
 		closeConnectRst();
 		return sollicitatie;
 	}
 
 	public void updateSollicitatie(String toelichting, int studentID,
 			int opdrachtID) {
 		makeConnection();
 		String query = "UPDATE Sollicitatie SET toelichting='" + toelichting
 				+ "' WHERE studentID=" + studentID + " AND opdrachtID="
 				+ opdrachtID + " ";
 		makeResultSet(query, true);
 		closeConnectRst();
 
 	}
 
 	public void insertSollicitatie(String toelichting, int studentID,
 			int opdrachtID) {
		System.out.println("insetSollicitatie init");
 		makeConnection();
 		System.out.println("===SollicitatieDAO===");
 		System.out.println(toelichting);
 		System.out.println(opdrachtID);
 		System.out.println(studentID);
 		System.out.println("===");
 		String query = "INSERT INTO Sollicitatie VALUES ('"
 				 + opdrachtID +", "+studentID+ ",\"" + toelichting + "\"' )";
 		makeResultSet(query, true);
 		closeConnectRst();
 
 	}
 
 }
