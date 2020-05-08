 /**
  * @author Laura Irlinger
  * @author Tamino Hartmann
  * @author Oemer Sahin
  * @author Patryk Boczon
  */
 package servlet;
 
 import java.io.IOException;
 import java.util.Vector;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import logger.Log;
 import user.Applicant;
 
 import com.google.gson.Gson;
 
 import database.application.Application;
 import database.offer.Offer;
 
 /**
  * Das <code>Applicant</code> Servlet behandelt alle Aktionen von angemeldeten
  * Bewerbern (Applicants).
  */
 
 @WebServlet("/Applicant/*")
 public class ApplicantServlet extends HttpServlet {
 
 	/**
 	 * Standart default serial.
 	 */
 	private static final long serialVersionUID = 1L;
 	/**
 	 * Variable zum speichern der Log Instanz.
 	 */
 	private Log log;
 
 	/**
 	 * Variable zum speichern der GSON Instanz.
 	 */
 	private Gson gson;
 
 	/**
 	 * Konstruktor. Hier werden die wichtigen Referenzen gesetzt und wenn noetig
 	 * erstellt. Auch wird ein log Eintrag geschrieben um die Initialisierung
 	 * ersichtlich zu machen.
 	 **/
 	public ApplicantServlet() {
 		super();
 		log = Helper.log;
 		gson = new Gson();
 		// offcon = OfferController.getInstance();
 	}
 
 	/**
 	 * Diese Methode handhabt die Abarbeitung von Aufrufen.
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// Check authenticity:
 		Applicant applicant = Helper.checkAuthenticity(request.getSession(),
 				Applicant.class);
 		if (applicant == null) {
 			response.setContentType("text/url");
 			response.getWriter().write(Helper.D_INDEX);
 			return;
 		}
 		// Switch action on path:
 		String path = request.getPathInfo();
 		// Do loadAccount:
 		if (path.equals("/js/loadAccount")) {
 			String realName = applicant.getUserData().getName();
 			String email = applicant.getUserData().getEmail();
 			String JsonString = Helper.jsonAtor(new String[] { "realName",
 					"email" }, new String[] { realName, email });
 			response.setContentType("application/json");
 			response.getWriter().write(JsonString);
 		}
 		// Delete an application, bewerbung wiederrufen:
 		else if (path.equals("/js/deleteApplication")) {
 			// aid und benutzername sind identifier
 			int aid;
 			try {
 				aid = Integer.parseInt(request.getParameter("AID"));
 			} catch (NumberFormatException e) {
 				response.setContentType("text/error");
 				response.getWriter().write("Fehler beim Parsen der AID!");
 				return;
 			}
 			log.write("ApplicationServlet", "Deleting application <" + aid
 					+ ">");
 			Application appToDelete = applicant.getApplication(aid);
 			if (!applicant.deleteApplication(appToDelete)) {
 				log.write("ApplicantServlet", "Error deleting application!");
 				response.setContentType("text/error");
 				response.getWriter()
 						.write("Fuer diesen Benutzernamen existiert keine Bewerbung mit der gegebenen AID!");
 				return;
 			}
 
 			response.setContentType("text/url");
 			response.getWriter().write(Helper.D_APPLICANT_USERINDEX);
 			return;
 		}
 		// Load my offers:
 		else if (path.equals("/js/loadMyOffers")) {
 			// Offer vom User geholt
 			Vector<Offer> myoffers = applicant.myOffers();
 			response.setContentType("myapplication/json");
 			response.getWriter().write(
 					gson.toJson(myoffers, myoffers.getClass()));
 		}
 		// Load offers:
 		else if (path.equals("/js/loadOffers")) {
 			Vector<Offer> offers = applicant.possibleOffers();
 			response.setContentType("application/json");
 			response.getWriter().write(gson.toJson(offers, offers.getClass()));
 		}
 
 		// Load my information about one application:
 		else if (path.equals("/js/selectApplication")) {
 			int aid = -1;
 			try {
 				aid = Integer.parseInt(request.getParameter("AID"));
 			} catch (NumberFormatException e) {
 				response.setContentType("text/error");
 				response.getWriter().write("Fehler beim Parsen der AID!");
 				return;
 			}
 			String json = applicant.getApplicationInfo(aid);
 			if (json == null) {
 				response.setContentType("text/url");
 				response.getWriter().write(Helper.D_APPLICANT_USERINDEX);
 				return;
 			}
 			response.setContentType("application/json");
 			response.getWriter().write(json);
 			return;
 		}
 		// Load my information about one application(documents):
 		else if (path.equals("/js/selectDocuments")) {
 			int aid = -1;
 			try {
 				aid = Integer.parseInt(request.getParameter("aid"));
 			} catch (NumberFormatException e) {
 				response.setContentType("text/error");
 				response.getWriter().write("Fehler beim Parsen der AID!");
 				return;
 			}
 			// Create JSON version of custom data:
 			Vector<String> docDataObject = applicant.getDocuments(aid);
 			response.setContentType("application/json");
 			response.getWriter().write(
 					gson.toJson(docDataObject, docDataObject.getClass()));
 		}
 		// Delete own account:
 		else if (path.equals("/js/deleteAccount")) {
 			// Das hier ist sehr falsch (wer auch immer es war)! ->
 			// "String name = request.getParameter("name");"
 			// name wäre so IMMER NULL! (js liest also falschen parameter aus
 			// und generell soll man NIE den client trauen!)
 			String name = applicant.getUserData().getUsername();
 			if (applicant.deleteOwnAccount()) {
 				log.write("ApplicantServlet", name
 						+ " has deleted his account.");
 				// Simply now for debugging:
 				response.setContentType("text/url");
 				response.getWriter().write(Helper.D_INDEX);
 			} else {
 				response.setContentType("text/error");
 				response.getWriter().write("Error while deleting account!");
 			}
 		}
 		// change own account data
 		else if (path.equals("/js/changeAccount")) {
 			String name = request.getParameter("name");
 			String email = request.getParameter("mail");
 			String pw = request.getParameter("pw");
 			if (pw.equals(""))
 				pw = null; // falls leeres pw-> null damit die editOwnAccount
 							// funktion das pw nicht auf "" setzt!
 			if (applicant.editOwnAccount(name, email, pw, null)) {
 				log.write("ApplicantServlet", applicant.getUserData()
 						.getUsername() + " has modified his account.");
 				response.setContentType("text/url");
 				response.getWriter().write(Helper.D_APPLICANT_USERINDEX);
 			} else {
 				response.setContentType("text/error");
 				response.getWriter().write("Fehler beim ändern der Daten.");
 			}
 		} else if (path.equals("/js/apply")) {
 			int aid = Integer.parseInt(request.getParameter("aid"));
 			if (applicant.apply(aid)) {
 				response.setContentType("text/url");
 				response.getWriter().write(Helper.D_APPLICANT_USERINDEX);
 			} else {
 				response.setContentType("text/error");
 				response.getWriter().write("error");
 			}
 		}
 		// Get email addresses if required:
 		else if (path.equals("/js/getEmail")) {
 			String user = request.getParameter("user");
 			if (!Helper.validate(user)) {
 				response.setContentType("text/error");
 				response.getWriter().write("Invalid user parameter!");
 				return;
 			}
 			String email = applicant.getEmail(user);
 			if (email == null) {
 				response.setContentType("text/url");
 				response.getWriter().write("No email read from database!");
 				return;
 			}
 			response.setContentType("text/email");
 			response.getWriter().write(email);
 		} else {
 			log.write("ApplicantServlet", "Unknown path <" + path + ">");
 		}
 	}
 }
