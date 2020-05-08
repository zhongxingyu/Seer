 package chatbox;
 
 import java.io.*;
 import java.sql.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 public class ProjectServlet extends HttpServlet {
 
     @Override
     public void doGet(HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {
 
         //Allocate an output writer to write the response message into the network socket
 
         PrintWriter out = response.getWriter();
 
         //Go through the shennanigans to get a connection to our database, then find the current user
         
         Connection conn = null;
         PreparedStatement stmt = null;
 
         String projectidString = request.getParameter("id");
         String acceptString = request.getParameter("accept");
         try {
             int projectid = Integer.parseInt(projectidString);
             try {
 
                 //Attempts to connect to the database. ("hostname:port/default database", username, password)
 
                 conn = DriverManager.getConnection(
                         "jdbc:mysql://localhost:3306/geekbase", "root", "gizz442a");
 
                 int userid = SessionManager.getLoggedInUserId(request,conn);
                 if (acceptString != null && acceptString.equals("no")) {
                     stmt = conn.prepareStatement("delete from userProjects where userId = ? and projectId = ?");
                     stmt.setInt(1,userid);
                     stmt.setInt(2,projectid);
                     stmt.executeUpdate();
                     printRedirect(out);
                 }
                 else {
                     stmt = conn.prepareStatement("select * from userProjects where userId = ? and projectId = ?");
                     stmt.setInt(1,userid);
                     stmt.setInt(2,projectid);
                     ResultSet rset = stmt.executeQuery();
 
                     if (rset.next()) {
 
                         //This user is authorized to see this project
 
                         if (rset.getInt("accepted") == 0) {
                             stmt.close();
                             stmt = conn.prepareStatement("update userProjects set accepted = 1 where userId = ? and projectId = ?");
                             stmt.setInt(1,userid);
                             stmt.setInt(2,projectid);
                             stmt.executeUpdate();
                         }
                         stmt.close();
                         stmt = conn.prepareStatement("select * from projects where id = ?");
                         stmt.setInt(1,projectid);
                         ResultSet project = stmt.executeQuery();
                         if (project.next()) {
                             String projectName = project.getString("name");
                             boolean creator = (project.getInt("creatorId") == userid);
                             printProject(out,projectid,projectName,creator);
                         }
                     }
                     else {
 
                         //Tell the user we didn't find a project
 
                         printError(out);
                     }
                 }
             }
             catch (SQLException e) {
                 e.printStackTrace();
             }
             finally {
                 try {
                     if (conn != null) conn.close();
                     if (stmt != null) stmt.close();
                 }
                 catch (SQLException e) {
                     e.printStackTrace();
                 }
                 out.close();
             }
         }
         catch (Exception e) {
 
             //Tell the user the number they entered didn't parse
 
             printError(out);
         }
     }
 
     @Override
     public void doPost(HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {
 
         //Set the response MIME type of the response message
         
         response.setContentType("text/html");
 
         //Allocate an output writer to write the response message into the network socket
 
         PrintWriter out = response.getWriter();
         try {
             printRedirect(out);
         }
         finally {
             out.close();
         }
     }
 
     public void printRedirect(PrintWriter out) {
         out.write("<html><meta http-equiv='REFRESH' content='0;url=/'></html>");
     }
 
     public void printProject(PrintWriter out, int projectId, String projectName, boolean creator) {
         out.write("<html>");
             out.write("<head>");
                 out.write("<script src='js/utilities.js'></script>");
                out.write("<script src='js/simpleShapes.js'></script>");
                 out.write("<script src='js/dynamicAttributes.js'></script>");
                 out.write("<script src='js/canvas.js'></script>");
                 out.write("<script src='js/todoItem.js'></script>");
                 out.write("<script src='js/todoManager.js'></script>");
                 out.write("<script src='js/networkManager.js'></script>");
                 out.write("<script src='js/main.js'></script>");
                 out.write("<link rel='stylesheet' type='text/css' href='css/style.css'>");
                 out.write("<div class='headerBar'>");
                     out.write("<a href='/'>home</a><br>");
                     out.write("<a href='/logout'>logout</a>");
                 out.write("</div>");
             out.write("</head>");
             out.write("<body>");
                 out.write("<div style='margin-top:20px;'>");
                     out.write("<center>");
                         out.write("<img src='img/tree.png'>");
                         out.write("<h1 class='tree'>"+projectName+"</h1><hr>");
                         out.write("<h2>Your Project Tree</h2>");
                         out.write("<div class='projectContainer todo' style='overflow:auto;height:70%;' id='workflowparent'>");
                             out.write("<div class='instructions' id='instructions'>");
                                 out.write("Instructions:<br>");
                                 out.write("-Double click to add todo items<br>");
                                 out.write("-Double click text to edit<br>");
                                 out.write("-Drag to rearrange<br>");
                                 out.write("-Drag from the top or bottom of a todo to another todo to add a dependency<br>");
                             out.write("</div>");
                             out.write("<canvas id='workflow' width='600' height='400'></canvas>");
                         out.write("</div>");
                     out.write("</center>");
                 out.write("</div>");
                 out.write("<center>");
                     out.write("<hr style='margin-top:30px'>");
                     out.write("<h2 style='margin-top:10px;'>Your Conversation Wall</h2>");
                     out.write("<div class='chatContainer' id='chatText'>");
                     out.write("</div>");
                     out.write("<input type='text' size='30' id='chatBox'>");
                     out.write("<button onclick='NetworkManager.sendChat()'>Post</button>");
                 out.write("</center>");
                 out.write("<center>");
                     out.write("<hr>");
                     out.write("<h2 style='margin-top:40px;'>Grow the Project</h2>");
                     out.write("<form method='post' action='/invite?id="+projectId+"'>");
                         out.write("email address to invite:");
                         out.write("<input type='text' size='30' name='email'><br>");
                         out.write("<button>Send Invitation</button>");
                     out.write("</form>");
                 out.write("</center>");
                 out.write("<div class='credits'>this app brought to you with love by Keenon Werling &copy; 2013</div>");
             out.write("</body>");
         out.write("</html>");
     }
 
     public void printError(PrintWriter out) {
         out.write("<html>");
             out.write("<head>");
                 out.write("<link rel='stylesheet' type='text/css' href='css/style.css'>");
             out.write("</head>");
             out.write("<body>");
                 out.write("There was an error getting the project. Perhaps your session expired? Try logging back in.<br>");
                 out.write("<a href='/'>Home</a><br>");
             out.write("</body>");
         out.write("</html>");
     }
 }
