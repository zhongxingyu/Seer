 package clcert;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 
 public class Login extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private Configuration config = null;
        
     public Login() {
         super();
         try {
 			config  = new PropertiesConfiguration("app.properties");
 		} catch (ConfigurationException e) {
 			e.printStackTrace();
 		}
     }
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String username = request.getParameter("username");
 		String password = request.getParameter("password");
 		try {
 			Class.forName("org.sqlite.JDBC");
 			Connection conexion = DriverManager.getConnection(config.getString("JDBC.connectionURL"));
 			Statement statement = conexion.createStatement();
 			String query = "select * from usuarios where username='" + username + "' and password = '" + password + "'";
 			ResultSet resultado = statement.executeQuery(query);
 			int numFilas = 0;
 			while (resultado.next())
 				numFilas++;
 			statement.close();
 			conexion.close();
 			if (numFilas > 0) {
 				response.addCookie(new Cookie("userIP", request.getRemoteAddr()));
 				response.addCookie(new Cookie("userHost", request.getRemoteHost()));
 				response.sendRedirect("saludos.jsp");
 			}
 		} catch (Exception e) {
 			throw new ServletException(e);
 		}
 	}
 
 }
