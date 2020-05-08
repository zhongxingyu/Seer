import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

 import java.net.URL;
 import java.net.URLConnection;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 public class Proxy extends HttpServlet {
 
 	private static final long serialVersionUID = 1;
 
 	public void doGet(HttpServletRequest req, HttpServletResponse res)
 		throws ServletException, IOException
 	{
 
 		String urlString = req.getParameter("url");
 
 		URL url = new URL(urlString);
 		URLConnection conn = url.openConnection();
 
 		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 
 		String inputLine;
 		StringBuilder sb = new StringBuilder();
 		while((inputLine = in.readLine()) != null)
 			sb.append(inputLine);
 
 		in.close();
 
 		res.setContentType("text/xml");
 		res.setHeader("Cache-control", "no-cache");
 		res.getWriter().write(sb.toString());
 	}
 }
