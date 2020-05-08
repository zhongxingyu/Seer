 /**
  * 
  */
 package tk.c4se.halt.ih31.nimunimu.controller;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import lombok.val;
 import tk.c4se.halt.ih31.nimunimu.repository.SessionRepository;
 import tk.c4se.halt.ih31.nimunimu.validator.LoginValidator;
 
 /**
  * @author ne_Sachirou
  */
 @WebServlet("/login")
 public class LoginController extends Controller {
 	private static final long serialVersionUID = 1L;
 
 	private static final String JSP_PATH = "/login.jsp";
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		val urlRedirectAfterLogin = req.getParameter("redirect");
 		req.setAttribute("redirectUrl", urlRedirectAfterLogin);
		forward(req, resp, "login", JSP_PATH);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException, ServletException {
 		// if (!checkCsrf(req, resp)) {
 		// return;
 		// }
 		val validator = new LoginValidator(req);
 		val errors = validator.validate();
 		if (!errors.isEmpty()) {
 			showError(req, resp, errors);
 			return;
 		}
 		new SessionRepository(req).setLoginAccountId(validator.getId());
 		resp.sendRedirect(req.getParameter("redirectUrl"));
 	}
 
 	private void showError(HttpServletRequest req, HttpServletResponse resp,
 			Map<String, Exception> errors) throws ServletException, IOException {
 		val id = req.getParameter("id");
 		val password = req.getParameter("password");
 		req.setAttribute("id", id);
 		req.setAttribute("password", password);
 		req.setAttribute("errors", errors);
		forward(req, resp, "login", JSP_PATH);
 	}
 }
