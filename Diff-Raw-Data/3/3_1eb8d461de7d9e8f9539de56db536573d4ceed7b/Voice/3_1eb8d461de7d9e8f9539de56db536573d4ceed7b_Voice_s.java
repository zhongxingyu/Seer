 package api;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Servlet implementation class Voice
  */
 @WebServlet("/api/voice")
 public class Voice extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	Db oDb;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public Voice() {
         super();
         oDb = new Db();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		final String VOICE_COMMAND = "voice_command";
 		final String MENU_RESULT = "menu_result";
 		final String VOICE_SUCCESS = "voice_success";
 		
 		// TODO Auto-generated method stub
 //		PrintWriter out;
 //		out = response.getWriter();
 		HttpSession sess = request.getSession();
 		
 		String target = "";
 		String forTTS = "";
 		int voicetime = 0;
		
 		if(request.getParameter("mode").equals("check_voice")) {
 			sess.setAttribute(VOICE_SUCCESS, false);
 			sess.removeAttribute(VOICE_COMMAND);
 			String value = request.getParameter("value");
 			sess.setAttribute(VOICE_COMMAND, value);
 			
 			oDb.clear();
 			oDb.setTable("command_list");
 			oDb.addField("weight");
 			oDb.setPostfixQuery("GROUP BY weight ORDER BY weight");
 
 			List<Map<String, String>> weightList = oDb.getData();
 			
 			for(Map<String, String> weightItem: weightList){
 				oDb.clear();
 				oDb.setTable("command_list");
 				oDb.addField("child");
 				oDb.addField("command");
 				oDb.addField("target");
 				oDb.addFilter("weight", weightItem.get("weight"));
 				List<Map<String, String>> queryList = oDb.getData();
 				
 				for(Map<String, String> queryItem: queryList){
 					if(value.indexOf(queryItem.get("command")) != -1){
 						if(queryItem.get("child") == null){
 							target = queryItem.get("target");
 							forTTS = "OK";
 							voicetime = 1;
 							sess.setAttribute(VOICE_SUCCESS, true);
 						} else {
 							oDb.clear();
 							oDb.setTable("command_list");
 							oDb.addField("command");
 							oDb.addField("target");
 							oDb.addFilter("parent", queryItem.get("child"));
 							List<Map<String, String>> childList = oDb.getData();
 							
 							for(Map<String, String> childItem: childList){
 								if(value.indexOf(childItem.get("command")) != -1){
 									target = childItem.get("target");
 									forTTS = "OK";
 									voicetime = 1;
 									sess.setAttribute(VOICE_SUCCESS, true);
 								}
 							}
 							if(target.equals("")){
 								target = queryItem.get("target");
 								if(queryItem.get("child").equals("office") 
 										|| queryItem.get("child").equals("lab")
 										){
 									forTTS = "I don't understand where you want to go";
 									voicetime = 3;
 								}
 								if(queryItem.get("child").equals("cafeteria")){
 									forTTS = "I don't understand where you want to eat";
 									voicetime = 3;
 								}
 							}
 						}
 					}
 				}
 			}
 			
 			if(target.equals("")){
 				target = "./menu.html";
 				forTTS = "I don't understand your command.";
 				voicetime = 3;
 			}
 
 			String result = "";
 			result += "{\"target\": \"" + target + "\", \"forTTS\": \"" + forTTS + "\", \"voicetime\": " + voicetime + "}";
 			
 			ServletOutputStream out = response.getOutputStream();
 			out.print(result);
 			
 			System.out.println(sess.getAttribute(VOICE_COMMAND));
 			
 		} else if(request.getParameter("mode").equals("addlog")) {
 			System.out.println(request.getParameter("value"));
 			if(!(boolean)sess.getAttribute(VOICE_SUCCESS)) {
 				oDb.clear();
 				oDb.setTable("error_log");
 				oDb.setInsert("voice_text", (String)sess.getAttribute(VOICE_COMMAND));
 				oDb.setInsert("selected_page", request.getParameter("value"));
 				oDb.putData();
 			}
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
