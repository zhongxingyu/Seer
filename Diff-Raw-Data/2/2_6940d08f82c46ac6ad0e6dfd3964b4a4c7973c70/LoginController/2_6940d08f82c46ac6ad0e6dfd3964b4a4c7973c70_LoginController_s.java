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
 import javax.servlet.http.HttpSession;
 
 import lombok.val;
 import tk.c4se.halt.ih31.nimunimu.exception.DBAccessException;
 import tk.c4se.halt.ih31.nimunimu.model.Member;
 import tk.c4se.halt.ih31.nimunimu.repository.SessionRepository;
 import tk.c4se.halt.ih31.nimunimu.validator.LoginValidator;
 
 /**
  * @author ne_Sachirou
  * 
  */
 @WebServlet("/login")
 public class LoginController extends Controller {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 41306642246590835L;
 
 	private static final String JSP_PATH = "/resource/partial/login.jsp";
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		final String urlRedirectAfterLogin = req.getParameter("redirect");
 		req.setAttribute("redirect", urlRedirectAfterLogin);
 		forward(req, resp, "login", JSP_PATH);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException, ServletException {
 		if (!checkCsrf(req, resp))
 			return;
 		Map<String, Exception> errors = new LoginValidator().validate(req);
 		if (!errors.isEmpty()) {
 			showError(req, resp, errors);
 			return;
 		}
 		final String id = req.getParameter("id").trim();
 		final String password = req.getParameter("password").trim();
 		Boolean isCorrectPassword = false;
 		try {
 			isCorrectPassword = Member.isCorrectPassword(id, password);
 		} catch (DBAccessException e) {
 			errors.put("DBAccess", e);
 		}
 		if (!isCorrectPassword)
			errors.put("Login", new Exception("ID���p�X���[�h���قȂ�܂��B"));
 		if (!errors.isEmpty()) {
 			showError(req, resp, errors);
 			return;
 		}
 		HttpSession session = new SessionRepository().getSession(req, true);
 		session.setAttribute("memberId", id);
 		resp.sendRedirect("/");
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
