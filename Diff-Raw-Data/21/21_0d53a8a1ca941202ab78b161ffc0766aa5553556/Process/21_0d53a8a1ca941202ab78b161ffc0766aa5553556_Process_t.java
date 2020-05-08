 package src.servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.util.HashMap;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import org.json.simple.JSONObject;

 
 import src.objects.Users;
 import src.objects.Wod;
 import src.utils.DBManager;
 
 public class Process extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public Process() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String action = request.getParameter("action");
 		Users User = new Users();
 		Wod wod = new Wod();
 
 		DBManager db = new DBManager();
 		Connection conn = null;
 		PrintWriter out = response.getWriter();
 		JSONObject obj = new JSONObject();
 		HashMap<Integer, String> ex = wod.getAllExercises();
 
 		try{
 			conn = db.connect();
 			if(action.equals("enter")){
 				//admin is entering a new workout
 				
 			}else if (action.equals("save")){
 				//user is trying to save their userdata
 				
 			}else if(action.equals("adminfill") || action.equals("userfill")){
 				//user just typed in WOD or Date, so autofill fields
 				String wodName = request.getParameter("Name");
 				String wodDate = request.getParameter("Date");
 
 				wod = wod.getWod(wodDate);
 				
 				obj.put("Response", "Success");
 				obj.put("Name", wod.getName());
 				obj.put("Date", wod.getDate());
 				obj.put("Rounds", wod.getRounds());
 				obj.put("Type", wod.getType());
 				obj.put("WODID", wod.getWODID());
 				
 					for(int i = 1;  i < 11; i++){
 	        			switch(i){
 	        				case 1:
 		        				obj.put("Ex"+i+"ID", ex.get(wod.getEx1ID()));
 								obj.put("Ex"+i+"Reps", wod.getEx1Reps());
 								obj.put("Ex"+i+"RX", wod.getEx1RX());
 								break;
 		        			case 2:
 		        				obj.put("Ex"+i+"ID", ex.get(wod.getEx2ID()));
 								obj.put("Ex"+i+"Reps", wod.getEx2Reps());
 								obj.put("Ex"+i+"RX", wod.getEx2RX());
 								break;
 		        			case 3:
 		        				obj.put("Ex"+i+"ID", ex.get( wod.getEx3ID()));
 								obj.put("Ex"+i+"Reps", wod.getEx3Reps());
 								obj.put("Ex"+i+"RX", wod.getEx3RX());
 								break;
 		        			case 4:
 		        				obj.put("Ex"+i+"ID", ex.get(wod.getEx4ID()));
 								obj.put("Ex"+i+"Reps", wod.getEx4Reps());
 								obj.put("Ex"+i+"RX", wod.getEx4RX());
 								break;
 		        			case 5:
 		        				obj.put("Ex"+i+"ID", ex.get(wod.getEx5ID()));
 								obj.put("Ex"+i+"Reps", wod.getEx5Reps());
 								obj.put("Ex"+i+"RX", wod.getEx5RX());
 								break;
 		        			case 6:
 		        				obj.put("Ex"+i+"ID", ex.get(wod.getEx6ID()));
 								obj.put("Ex"+i+"Reps", wod.getEx6Reps());
 								obj.put("Ex"+i+"RX", wod.getEx6RX());
 								break;
 		        			case 7:
 		        				obj.put("Ex"+i+"ID", ex.get(wod.getEx7ID()));
 								obj.put("Ex"+i+"Reps", wod.getEx7Reps());
 								obj.put("Ex"+i+"RX", wod.getEx7RX());
 								break;
 		        			case 8:
 		        				obj.put("Ex"+i+"ID", ex.get(wod.getEx8ID()));
 								obj.put("Ex"+i+"Reps", wod.getEx8Reps());
 								obj.put("Ex"+i+"RX", wod.getEx8RX());
 								break;
 		        			case 9:
 		        				obj.put("Ex"+i+"ID", ex.get(wod.getEx9ID()));
 								obj.put("Ex"+i+"Reps", wod.getEx9Reps());
 								obj.put("Ex"+i+"RX", wod.getEx9RX());
 								break;
 		        			case 10:
 		        				obj.put("Ex"+i+"ID", ex.get(wod.getEx10ID()));
 								obj.put("Ex"+i+"Reps", wod.getEx10Reps());
 								obj.put("Ex"+i+"RX", wod.getEx10RX());
 								break;
 	        			}//switch
 					}//for
 
 			}else{
 				obj.put("Response", "failed");
 				System.out.println("Failed save attempt.");
 			}
 		}finally{
 			
 		}
 		
 	}
 
 }
