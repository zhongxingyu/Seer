 package no.anderska.wta.servlet;
 
 import com.google.gson.Gson;
 import no.anderska.wta.SetupGame;
 import no.anderska.wta.StatusGiver;
 import no.anderska.wta.dto.CategoryDTO;
 import no.anderska.wta.dto.GameStatusDTO;
 import no.anderska.wta.dto.PlayerDTO;
 import no.anderska.wta.game.AdminHandler;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 
 public class StatusServlet extends HttpServlet {
     private StatusGiver statusGiver;
     private PlayerHandler playerHandler;
     private AdminHandler adminHandler;
 
     public void setStatusGiver(StatusGiver statusGiver) {
         this.statusGiver = statusGiver;
     }
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         List<CategoryDTO> categories = statusGiver.catergoryStatus();
         List<PlayerDTO> players = playerHandler.playerList();
         GameStatusDTO gameStatusDTO = new GameStatusDTO(players, categories);
         Gson gson = new Gson();
 
         resp.setContentType("text/json");
         resp.getWriter().append(gson.toJson(gameStatusDTO));
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         String action = req.getParameter("action");
         String errormessage = null;
         String password = req.getParameter("password");
         if ("resetAll".equals(action)) {
             errormessage = adminHandler.restartGame(password);
         } else if ("resetCategories".equals(action)) {
             errormessage = adminHandler.resetCategories(password);
         } else if ("categoryEdit".equals(action)) {
             errormessage = adminHandler.editCategories(password,req.getParameterValues("engines"));
         } else {
             errormessage = "Unknown action";
         }
         resp.setContentType("text/html");
        writeResponse(resp.getWriter(),errormessage != null ? errormessage : "Action performed");
         }
 
     private void writeResponse(PrintWriter writer, String message) {
         writer.append("<html><body><p>");
         writer.append(message);
         writer.append("</p><p><a href='admin.html'>Back</a></p></body></html>");
     }
 
 
     @Override
     public void init() throws ServletException {
         this.statusGiver = SetupGame.instance().getGameHandler();
         this.playerHandler = SetupGame.instance().getPlayerHandler();
         this.adminHandler = SetupGame.instance().getGameHandler();
     }
 
     public void setPlayerHandler(PlayerHandler playerHandler) {
         this.playerHandler = playerHandler;
     }
 
     public void setAdminHandler(AdminHandler adminHandler) {
         this.adminHandler = adminHandler;
     }
 }
