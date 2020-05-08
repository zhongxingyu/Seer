 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

 import edu.neumont.learningChess.json.Jsonizer;
 import edu.neumont.learningChess.model.User;
 
 public class RegisterServlet extends HttpServlet {
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		ServletContext context = request.getSession().getServletContext();
 		String responseString = null;
 		try {
 			responseString = register(context, request) + "";
 		} catch (Throwable t) {
 			if (MainServlet.LOG_ERROR) {
 				context.log("Exception in analyze history!");
 				ByteArrayOutputStream bos = new ByteArrayOutputStream();
 				PrintStream printStream = new PrintStream(bos);
 				t.printStackTrace(printStream);
 				context.log(bos.toString());
 			}
 		}
 		if (MainServlet.LOG_VERBOSE) {
 			context.log("ResponseString: " + responseString);
 			context.log("Server Activity: Finished DoPost");
 		}
 		PrintWriter writer = response.getWriter();
 		writer.println(responseString);
 		writer.flush();
 	}
 
 	private String register(ServletContext context, HttpServletRequest request) throws Exception {
 		String jsonString = MainServlet.getPostBody(request.getReader());
 		User user = Jsonizer.dejsonize(jsonString, User.class);
 
 		Class.forName(MainServlet.dbClass);
 		Connection con = DriverManager.getConnection(MainServlet.dbUrl, "root", "Ch3ssCh3ss");
		try {
			PreparedStatement stmt = con.prepareStatement("insert into user(username,password) values(?, ?)");
			stmt.setString(1, user.getUsername());
			stmt.setString(2, user.getPassword());
			stmt.executeUpdate();
		} catch(MySQLIntegrityConstraintViolationException e) {
			return null;
		}
 		return Jsonizer.jsonize(user);
 	}
 }
