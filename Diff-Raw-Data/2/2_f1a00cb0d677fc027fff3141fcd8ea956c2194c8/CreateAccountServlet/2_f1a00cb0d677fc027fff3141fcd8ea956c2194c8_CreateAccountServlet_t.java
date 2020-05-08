 package chatbox;
 
 import java.io.*;
 import java.sql.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 public class CreateAccountServlet extends HttpServlet {
     @Override
     public void doGet(HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {
 
         //Allocate an output writer to write the response message into the network socket
 
         PrintWriter out = response.getWriter();
 
         String email = request.getParameter("email");
 
         printCreateAccount(out,email);
     }
 
     @Override
     public void doPost(HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {
 
         //Location
 
         String uri = request.getRequestURI().substring(1);
 
         String username = request.getParameter("username");
         String password = request.getParameter("password");
         String hash = Hashes.MD5(password);
         String email = request.getParameter("email");
         String securityQuestion = request.getParameter("securityQuestion");
         String securityAnswer = request.getParameter("securityAnswer");
         String activationHash = Hashes.randomString();
 
         System.out.println(username+": "+password+", "+hash+"\n"+activationHash);
 
         //Set the response MIME type of the response message
 
         response.setContentType("text/html");
 
         //Allocate an output writer to write the response message into the network socket
 
         PrintWriter out = response.getWriter();
 
         //Do database stuff
 
         Connection conn = null;
         PreparedStatement stmt = null;
 
         try {
 
             //Attempts to connect to the database. ("hostname:port/default database", username, password)
 
             conn = DriverManager.getConnection(
                     "jdbc:mysql://localhost:3306/geekbase", "root", "gizz442a");
 
             //Check if the email already exists
 
             stmt = conn.prepareStatement("select * from users where email = ?");
             stmt.setString(1,email);
             ResultSet rset = stmt.executeQuery();
             int id = 0;
             if (rset.next()) {
 
                 //Replace the user in the users table
 
                 stmt = conn.prepareStatement("update users set username=?, passwordHash=?, email=?, securityQuestion=?, securityAnswer=?, activated=?, activationCode=? where email=?", Statement.RETURN_GENERATED_KEYS);
                 stmt.setString(8,email);
 
                 //We'll need the user's ID so we can set it as logged in
 
                 id = rset.getInt("id");
             }
             else {
 
                 //Add the new user to the users table
 
                 stmt = conn.prepareStatement("insert into users(username,passwordHash,email,securityQuestion,securityAnswer,activated,activationCode) values (?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
             }
             stmt.setString(1,username);
             stmt.setString(2,hash);
             stmt.setString(3,email);
             stmt.setString(4,securityQuestion);
             stmt.setString(5,securityAnswer);
             stmt.setInt(6,0); //set activated false
             stmt.setString(7,activationHash);
             
             //Send query to server
 
             stmt.executeUpdate();
 
             //Get the id we just generated
 
             ResultSet keys = stmt.getGeneratedKeys();
             if (keys.next()) {
                 
                 //We just inserted a new user
 
                 id = keys.getInt(1);
             }
             keys.close();
             stmt.close();
 
             //Drop this user into the session table so they won't have to log back in
 
             SessionManager.setLoggedInUserId(request,conn,id);
 
             //Test sending mail
 
             MailBot mail = new MailBot();
             try {
                 mail.sendMessage(email,"Activation for Graph","Visit <a href='http://www.rebelmoreproductively.com/activate?key="+activationHash+"'>here</a> to activate your account!");
                 out.write("<html>");
                     out.write("<head>");
                         out.write("<link rel='stylesheet' type='text/css' href='css/style.css'>");
                     out.write("</head>");
                     out.write("<body>");
                         out.write("Activation email sent.<br>");
                         out.write("Go <a href='/'>use your account!</a><br>");
                     out.write("</body>");
                 out.write("</html>");
             }
             catch (Exception e) {
                 out.write("Mail sending didn't work");
                 e.printStackTrace();
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
         finally {
             out.close();
             try {
                 if (stmt != null) stmt.close();
                 if (conn != null) conn.close();
             }
             catch (SQLException e) {
                 e.printStackTrace();
             }
         }
     }
 
     public void printCreateAccount(PrintWriter out,String email) {
         out.write("<html>");
             out.write("<head>");
                 out.write("<link rel='stylesheet' type='text/css' href='css/style.css'>");
             out.write("</head>");
             out.write("<body>");
                 out.write("<center>");
                     out.write("<h1 class='tree' style='margin-top:45px'>Create Account</h1>");
                     out.write("<h3>you're almost in the club, but first<br>you need to answer a few questions:</h3>");
                     out.write("<div>");
                         out.write("<img src='img/grass.png' style='margin-top:10px;'>");
                         out.write("<div style='position:relative;margin:auto;width:10px;'>");
                             out.write("<div style='background-color:#0f99c2;height:800px;width:5px;position:absolute;left:85px;top:-70px;z-index:-1;'> </div>");
                         out.write("</div>");
                     out.write("</div>");
                 out.write("</center>");
                 out.write("<div style='margin-left:auto;margin-right:auto;width:270px;'>");
                     out.write("<!--<img src='img/tree.png' style='float:left;margin-right:40px;margin-top:40px;'>-->");
                     out.write("<form style='margin-top:40px' method='post'>");
                         out.write("your username?<br><input type='text' name='username' size='25'><br>");
                         out.write("<hr>");
                         out.write("your password?<br><input type='password' name='password' size='25'><br>");
                         out.write("copy password:<br><input type='password' name='passwordCopy' size='25'><br>");
                         out.write("<hr>");
                         out.write("your security question?<br><input type='text' name='securityQuestion' size='25'><br>");
                         out.write("security answer?<br><input type='text' name='securityAnswer' size='25'><br>");
                         out.write("<hr>");
                         out.write("your email?<br>");
                         if (email == null) {
                            out.write("<input type='text' name='email' size='25'>");
                         }
                         else {
                             out.write("<input type='text' size='25' name='email' readonly value='"+email+"'><br>");
                         }
                         out.write("<br><br><br><button>Give me an account!</button>");
                     out.write("</form>");
                     out.write("<a href='/'>back home</a><br>");
                     out.write("<br>");
                 out.write("</div>");
                 out.write("<div class='credits'>this app brought to you with love by Keenon Werling &copy; 2013</div>");
             out.write("</body>");
         out.write("</html>");
     }
 
 }
