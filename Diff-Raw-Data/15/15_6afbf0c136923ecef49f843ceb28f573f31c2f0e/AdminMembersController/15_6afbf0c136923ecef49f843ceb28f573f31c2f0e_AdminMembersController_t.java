 /**
  *
  */
 package tk.c4se.halt.ih31.nimunimu.controller;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import tk.c4se.halt.ih31.nimunimu.model.Member;
 import tk.c4se.halt.ih31.nimunimu.repository.MemberRepository;
 
 /**
  * @author ne_Sachirou
 * 
  */
 @WebServlet("/admin/members")
 public class AdminMembersController extends Controller {
 	/**
 	 *
 	 */
 	private static final long serialVersionUID = -8637277308340058722L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		final List<Member> members = new MemberRepository().all();
 		req.setAttribute("members", members);
		forward(req, resp, "admin/members", "/resource/partial/admin/members.jsp");
 	}
 }
