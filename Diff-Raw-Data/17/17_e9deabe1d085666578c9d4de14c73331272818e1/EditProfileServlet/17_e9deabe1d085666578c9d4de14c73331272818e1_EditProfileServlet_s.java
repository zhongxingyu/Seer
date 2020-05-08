 package com.epam.lab.intouch.web.servlet;
 
 import static com.epam.lab.intouch.web.util.RequestParser.getMember;
 import static com.epam.lab.intouch.web.util.RequestParser.getUpdatedMember;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.epam.lab.intouch.controller.exception.DataAccessingException;
 import com.epam.lab.intouch.controller.exception.InputDataFormatException;
 import com.epam.lab.intouch.controller.member.common.MemberController;
 import com.epam.lab.intouch.model.member.Member;
 
 public class EditProfileServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 	private final static Logger LOG = LogManager.getLogger(EditProfileServlet.class);
 	
 	
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		MemberController controller = new MemberController();
 		Member updatedMember;
 		Member oldMember = new Member();
		Member loginedMember = (Member) request.getSession().getAttribute("loginedMember");
 		try {
 			
 			oldMember.setLogin(loginedMember.getLogin());
 			updatedMember = getUpdatedMember(request);
 			
 			controller.updateProfile(oldMember, updatedMember);
 			response.sendRedirect("/InTouch/profile.jsp");
 		} catch (InputDataFormatException | DataAccessingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 
 }
