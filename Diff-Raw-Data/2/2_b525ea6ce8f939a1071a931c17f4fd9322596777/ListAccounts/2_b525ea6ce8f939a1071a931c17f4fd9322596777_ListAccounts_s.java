 package cpsc415;
 
 import static cpsc415.BankAccounts.BANK;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class ListAccounts extends HttpServlet {
 
 	public ListAccounts() { }
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		resp.setContentType("text/html");
 		StringBuilder b = new StringBuilder();
 		b.append("<html>")
		 .append("<head><title>Account Created</title></head>")
 		 .append("<body>")
 		 .append("<ul>");
 		for (Account a : BANK.getAccounts()) {
 			b.append("<li>")
 			 .append(a)
 			 .append("</li>");
 		}
 		b.append("</ul>")
 		 .append(Experiment.linkToHome("Return to Accounts"))
 		 .append("</body>")
 		 .append("</html>");
 		resp.getOutputStream().println(b.toString());
 	}
 
 }
