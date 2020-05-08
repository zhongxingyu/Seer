 package by.parf.checkers.servlet;
 
 import by.parf.checkers.beans.Team;
 import by.parf.checkers.dao.TeamDao;
 import by.parf.checkers.factory.TeamFactory;
 import by.parf.checkers.service.Constant;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: parf
  * Date: 31.8.13
  * Time: 22.59
  */
 @WebServlet(
         name = "addTeamController",
         urlPatterns = {"/addTeamController"}
 )
 public class AddTeamController extends AbstractController{
 
     @Override
     protected void performTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
         TeamDao teamDao = TeamFactory.getClassFromFactory();
         String teamName = request.getParameter(Constant.KEY_TEAM_NAME);
 
         long teamId = teamDao.getMaxId();
         Team team = new Team(++teamId);
         team.setName(teamName);
 
         teamDao.addTeam(team);
 
         response.setContentType("text/html");
         response.getWriter().write("true");
        teamDao.close();
     }
 }
