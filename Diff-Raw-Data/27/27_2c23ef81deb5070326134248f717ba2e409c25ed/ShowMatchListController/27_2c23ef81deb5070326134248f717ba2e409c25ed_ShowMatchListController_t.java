 package by.parf.checkers.servlet;
 
 import by.parf.checkers.beans.Match;
 import by.parf.checkers.dao.MatchDao;
 import by.parf.checkers.factory.MatchFactory;
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
  * Date: 4.9.13
  * Time: 1.34
  */
 @WebServlet(
         name = "showMatchListController",
         urlPatterns = {"/showMatchListController"}
 )
 public class ShowMatchListController extends AbstractController{
     @Override
     protected void performTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
         MatchDao matchDao = MatchFactory.getClassFromFactory();
 
         List<Match> matches = matchDao.getMatchList();
 
         JSONObject result = new JSONObject();
         JSONArray matchList = new JSONArray();
         JSONObject tmp = null;
 
         try {
 
             result.put("type", "matchList" );
 
             for (Match match: matches) {
 
                 tmp = new JSONObject();
                 tmp.put("id", match.getId());
                 tmp.put("name", match.getName());
                 tmp.put("team1Id", match.getTeamFirst().getName());
                 tmp.put("team2Id", match.getTeamSecond().getName());
                 tmp.put("time", match.getTime());
                 tmp.put("date", match.getDate());
 
                 matchList.put(tmp);
             }
 
             result.put("content", matchList);
 
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         response.setCharacterEncoding("UTF-8");
         response.setContentType("application/json");
         response.getWriter().write(result.toString());
        matchDao.close();
     }
 }
