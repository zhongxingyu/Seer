 package tk.c4se.halt.ih31.nimunimu.controller;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import tk.c4se.halt.ih31.nimunimu.dto.Member;
 import tk.c4se.halt.ih31.nimunimu.dto.MemberAuthority;
 
 @WebServlet("/")
 public class IndexController extends Controller {
 	private static final long serialVersionUID = 4945277894364938223L;
 
 	public IndexController() {
 		super();
 		authorities.add(MemberAuthority.ADMIN);
 		authorities.add(MemberAuthority.SALES);
 		authorities.add(MemberAuthority.SALES_MANAGER);
 		authorities.add(MemberAuthority.STORE);
 		authorities.add(MemberAuthority.STORE_MANAGER);
 		authorities.add(MemberAuthority.ACCOUNTING);
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		if (!checkAuthorized(req, resp)) {
 			return;
 		}
 		Member currentMember = (Member) req.getAttribute("loginAccount");
 		String menuJspPath = "";
 		switch (currentMember.getAuthority()) {
 		case ADMIN:
 			menuJspPath = "/admin/index.jsp";
 			break;
 		case SALES:
 			menuJspPath = "/sales/index.jsp";
 			break;
 		case SALES_MANAGER:
 			menuJspPath = "/sales_manager/index.jsp";
 			break;
 		case STORE:
 			menuJspPath = "/store/index.jsp";
 			break;
 		case STORE_MANAGER:
 			menuJspPath = "/store_manager/index.jsp";
 			break;
 		case ACCOUNTING:
 			menuJspPath = "/accounting/index.jsp";
 			break;
 		}
 		req.setAttribute("menuJspPath", menuJspPath);
		forward(req, resp, "nimunimu", "/index.jsp");
 	}
 }
