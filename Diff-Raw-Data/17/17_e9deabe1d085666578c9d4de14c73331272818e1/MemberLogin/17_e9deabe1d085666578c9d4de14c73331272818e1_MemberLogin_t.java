 package com.epam.lab.intouch.web.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.epam.lab.intouch.controller.exception.DataAccessingException;
 import com.epam.lab.intouch.controller.member.common.MemberController;
 import com.epam.lab.intouch.model.member.Member;
 import com.epam.lab.intouch.web.util.Attribute;
 
 public class MemberLogin extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	private MemberController controller;
 
 	@Override
 	public void init() throws ServletException {
 		super.init();
 		controller = new MemberController();
 
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		validateMember(request, response);
 
 	}
 
 	private void validateMember(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		final String userLogin = request.getParameter(Attribute.MEMBER_LOGIN);
 		final String password = request.getParameter(Attribute.MEMBER_PASSWORD);
 
 		try {
 			Member member = controller.authorizeMember(userLogin, password);
 			if (member != null) {
 				request.getSession().setAttribute("member", member);
 				response.sendRedirect("/InTouch/member_profile.jsp");
 				
 			} else {
				request.getRequestDispatcher("/user_not_found.jsp").forward(request, response);
 			}
 		} catch (DataAccessingException | ServletException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 }
