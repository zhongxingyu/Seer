 package Servlets;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import DBConnection.DBCommands;
 import MemberManagement.Member;
 import MemberManagement.MemberRegistration;
 
 /**
  * Servlet implementation class User
  */
 public class User extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public User() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession(false);
 		Member member = (Member) session.getAttribute("member");
 		if (member != null) {
 			int rsUserid = member.GetMemberID();
 
 			String requestDestination = "";
 			// int rsUserid = 1;
 
 			if (rsUserid >= 1) {
 				Member m = DBCommands.SelectMemberByID(rsUserid);
 				request.setAttribute("memberID", m.GetMemberID());
 				request.setAttribute("fname", m.GetFirstName());
 				request.setAttribute("lname", m.GetLastName());
 				request.setAttribute("email", m.GetEMail());
 				request.setAttribute("zip", m.GetPostCode());
 				request.setAttribute("street", m.GetStreet());
 				request.setAttribute("hnr", m.GetStreetNumber());
 				request.setAttribute("place", m.GetCity());
 			}
 
 			if (request.getParameter("toModus") == null
 					|| request.getParameter("toModus").equalsIgnoreCase("view")) {
 				request.setAttribute("toModus", "userView");
 				System.out.println("userView1");
 			} else if (request.getParameter("toModus").equalsIgnoreCase("edit")) {
 				System.out.println("userEdit2");
 				request.setAttribute("toModus", "userEdit");
 			} else if (request.getParameter("toModus").equalsIgnoreCase("commit")) {
 				member.SetMemberID(Integer.parseInt(request.getParameter(
 						"memberID").trim()));
 				member.SetCity(request.getParameter("place").trim());
 				member.SetEMail(request.getParameter("email").trim());
 				member.SetFirstName(request.getParameter("firstname").trim());
 				member.SetLastName(request.getParameter("lastname").trim());
 				member.SetPostCode(request.getParameter("zip").trim());
 				member.SetStreet(request.getParameter("street").trim());
 				member.SetStreetNumber(request.getParameter("hnr").trim());
 
 				if (member.GetMemberID() == 0 
 						|| member.GetCity() == ""
 						|| member.GetEMail() == ""
 						|| MemberRegistration.ValidateMemberEMail(member.GetEMail()) == false
 						|| member.GetFirstName() == ""
 						|| member.GetLastName() == ""
 						|| member.GetPostCode() == ""
 						|| member.GetStreet() == ""
 						|| member.GetStreetNumber() == "") {
 					
 					request.setAttribute("memberID", member.GetMemberID());
 					request.setAttribute("fname", member.GetFirstName());
 					request.setAttribute("lname", member.GetLastName());
 					request.setAttribute("email", member.GetEMail());
 					request.setAttribute("zip", member.GetPostCode());
 					request.setAttribute("street", member.GetStreet());
 					request.setAttribute("hnr", member.GetStreetNumber());
 					request.setAttribute("place", member.GetCity());
 					
 					request.setAttribute("message",
							"Es mssen alle Felder gefllt sein. Bitte berprfen Sie auch das Format Ihrer E-Mail Adresse ( @ und . mssen enthalten sein)");
 					request.setAttribute("toModus", "userEdit");
 					
 					
 				} else {
 					try {
 						DBCommands.UpdateMember(member);
 					} catch (Exception e) {
 					}
 
 					System.out.println(3);
 
 					if (rsUserid >= 1) {
 						Member m = DBCommands.SelectMemberByID(rsUserid);
 						request.setAttribute("memberID", m.GetMemberID());
 						request.setAttribute("fname", m.GetFirstName());
 						request.setAttribute("lname", m.GetLastName());
 						request.setAttribute("email", m.GetEMail());
 						request.setAttribute("zip", m.GetPostCode());
 						request.setAttribute("street", m.GetStreet());
 						request.setAttribute("hnr", m.GetStreetNumber());
 						request.setAttribute("place", m.GetCity());
 					}
 					// System.out.println(member.GetMemberID());
 					request.setAttribute("message", "nderungen wurden bernommen.");
 					request.setAttribute("toModus", "userView");
 				}
 
 			}
 
 		} else {
 			// no Permission, because no member is logged in
 			request.setAttribute("toModus", "noPermission");
 
 		}
 
 		RequestDispatcher dispatcher = getServletContext()
 				.getRequestDispatcher("/home.jsp");
 		dispatcher.forward(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		doGet(request, response);
 	}
 
 }
