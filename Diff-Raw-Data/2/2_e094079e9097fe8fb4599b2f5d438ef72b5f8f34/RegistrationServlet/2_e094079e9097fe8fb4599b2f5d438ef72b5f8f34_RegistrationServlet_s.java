 package no.steria.swhrs;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.naming.NamingException;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.util.ajax.JSONObjectConvertor;
 import org.joda.time.LocalDate;
 import org.json.JSONWriter;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 
 
 public class RegistrationServlet extends HttpServlet{
 	
 	private static final long serialVersionUID = -1090477374982937503L;
 	private HibernateHourRegDao db;
 		
 	public void init() throws ServletException {
 		db = new HibernateHourRegDao(Parameters.DB_JNDI);
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		if (req.getRequestURL().toString().contains(("hours/list"))) { 
 			resp.setContentType("application/json");
 			List<HourRegistration> hrlist = db.getHours(1, LocalDate.now());
 			
 			JSONObject json = new JSONObject();
 			for (HourRegistration hr: hrlist) {
 				json.put(Integer.toString(hr.getProjectnumber()), hr.getHours());
 			}
 			
 			String jsonText = json.toString();
 			System.out.println(jsonText);
 			resp.getWriter().write(jsonText);
 		}
 		
 	}
 	
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		
 		if (req.getRequestURL().toString().contains(("hours/registration"))) {
 			int personId = Integer.parseInt(req.getParameter("personId"));
 			String favourite = req.getParameter("fav");
 			String pNr = req.getParameter("projectNr");
 			int projectNr = Integer.parseInt(req.getParameter("projectNr").trim());
 			double hours = Double.parseDouble(req.getParameter("hours"));
 			String date = req.getParameter("date");
 			
 			System.out.println("Trying to save project: " + pNr);
 			saveRegToDatabase(personId, projectNr, LocalDate.now(), hours);
 		}
 		
 		if (req.getRequestURL().toString().contains(("hours/login"))) {
 			System.out.println("Kom hit");
 			String username = req.getParameter("username");
 			String password = req.getParameter("password");
 			System.out.println("Username:" +username+" Password: "+password);
 			int autoLoginExpire = (60*60*24);
 			//Change this when database is up
 			//if(db.validateUser(username, password) == true){
 			if(username.equals("steria") && password.equals("123")){
 				Cookie loginCookie = new Cookie("USERNAME", username);
 				loginCookie.setMaxAge(autoLoginExpire);
 				resp.setContentType("text/plain");
 				PrintWriter writer = resp.getWriter();
 				writer.append("Login ok");
 			}else{
 				resp.setStatus(403);
 				System.out.println("FAIL");
 			}
 		}
 		
  	}
 
 	private void saveRegToDatabase(int personId, int projectNr, LocalDate date, double hours) {
 		HourRegistration reg = HourRegistration.createRegistration(personId, projectNr, LocalDate.now(), hours);
 		db.saveHours(reg);
 		System.out.println("Saving registration with data: " + projectNr + "," +hours+ ", " +LocalDate.now());
 	}
 	
 	@Override
 	protected void service(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		db.beginTransaction();
 		super.service(req, resp);
 		db.endTransaction(true);
		//TODO sleng p en finally her s den ender transaksjonen hvis servleten krsjer
 	}
 
 }
