 package by.parf.checkers.servlet;
 
 import by.parf.checkers.beans.Team;
 import by.parf.checkers.dao.TeamDao;
 import by.parf.checkers.factory.TeamFactory;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: parf
  * Date: 1.9.13
  * Time: 2.12
  */
 @WebServlet(
         name = "showTeamListController",
         urlPatterns = {"/showTeamListController"}
 )
 public class ShowTeamListController extends AbstractController{
 
     @Override
     protected void performTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
         TeamDao teamDao = TeamFactory.getClassFromFactory();
 
         List<Team> teams = teamDao.getTeamList();
 
         JSONObject result = new JSONObject();
         JSONArray teamList = new JSONArray();
         JSONObject tmp = null;
 
         try {
 
             result.put("type", "teamList" );
 
             for (Team team: teams) {
 
                 tmp = new JSONObject();
                 tmp.put("id", team.getId());
                 tmp.put("name", team.getName());
 
                 teamList.put(tmp);
             }
 
             result.put("content", teamList);
 
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         response.setCharacterEncoding("UTF-8");
         response.setContentType("application/json");
         response.getWriter().write(result.toString());
     }
 }
