 package com.epam.lab.intouch.web.servlet.api.rest;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.epam.lab.intouch.controller.exception.DataAccessingException;
 import com.epam.lab.intouch.controller.member.common.MemberController;
 import com.epam.lab.intouch.dao.exception.DAOException;
 import com.epam.lab.intouch.model.member.Member;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class LoginServlet extends HttpServlet {
 	private static final long serialVersionUID = -963277169431608297L;
 
 	private static final String MIDDLE_MEMBER = "middleMember";
 	private static final String HEAVY_MEMBER = "heavyMember";
 	private static final String MEMBER_LOGIN = "memberLogin";
 	private static final String MEMBER_PASSWORD = "password";
 	private static final String MEMBER_WEIGHT = "memberWeight";
 
 	private String selializeToJson(Member member) {
 		GsonBuilder builder = new GsonBuilder();
 		builder.excludeFieldsWithoutExposeAnnotation();
 
 		Gson gson = builder.create();
 
 		return gson.toJson(member);
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		String userlogin = request.getParameter(MEMBER_LOGIN);
 		String password = request.getParameter(MEMBER_PASSWORD);
 		String memberWeight = request.getParameter(MEMBER_WEIGHT);
 
 		MemberController memberController = new MemberController();
 
 		Member autorized = null;
 		try {
 			autorized = memberController.authorizeMember(userlogin, password);
 
 			if (autorized != null) {
 
 				if (HEAVY_MEMBER.equals(memberWeight)) {
 					autorized = memberController.memberWithFullActiveProject(autorized.getLogin());
 				} else if (MIDDLE_MEMBER.equals(memberWeight)) {
 					autorized = memberController.memberWithActiveProjectInfo(autorized.getLogin());
 				} else {
 					autorized = memberController.memberWithActiveProjectId(autorized.getLogin());
 				}
 			}

 		} catch (DataAccessingException e) {
 			throw new IOException("Cannot access database!", e);
 		} catch (DAOException e) {
 			// this block will be deleted
 			throw new IOException("Cannot access database!", e);
 		}
 
 		response.getWriter().write(selializeToJson(autorized));
 
 	}
 
 }
