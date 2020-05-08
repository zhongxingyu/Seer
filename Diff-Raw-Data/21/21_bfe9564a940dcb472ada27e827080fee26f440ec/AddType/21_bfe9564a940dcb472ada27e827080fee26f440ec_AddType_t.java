 package servlets;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import model.AuthorityParameters;
 import model.TagParameter;
 import controller.ControllerAjoutTypeCertification;
import controller.ControllerParameter;
 
 /**
  * Servlet implementation class AddType
  */
 public class AddType extends HttpServlet
 {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public AddType()
 	{
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException
 	{
 		// TODO Auto-generated method stub
 
 		String name = request.getParameter("name");
 		String id = request.getParameter("id");
 		// String reason = request.getParameter("reason");
 		// String location = request.getParameter("location");
 		// String contact = request.getParameter("contact");
 
 		ControllerAjoutTypeCertification controller = ControllerAjoutTypeCertification
 				.getInstance();
 
 		request.setAttribute("type", controller.getType(id, name));
 
 		String action = request.getParameter("action");
 
 		if ("modify".equals(action))
 		{
 			request.getRequestDispatcher("addType.jsp?option=modify").forward(
 					request, response);
 		}
 		else if ("delete".equals(action))
 		{
 			TagParameter type = controller.getType(id, name);
 			String errMess = "success";
 			String messType = "KWS";
 			
 			if (!controller.removeType(type))
 			{
 				messType = "removing_type_error";
 			}
			String url = "typeCertifConfig.jsp";/*"parametrage.jsp?error=" + errMess + "&messType="
					+ messType;*/
			request.setAttribute("types", controller.getParameters());
			AuthorityParameters autho = ControllerParameter.getInstance().getAutho();
			request.setAttribute("authorityParameter", autho); 
 			request.getRequestDispatcher(url).forward(request, response);
 		}
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException
 	{
 		// TODO Auto-generated method stub
 
 		String name = request.getParameter("typeName");
 		String id = request.getParameter("typeIdentifiant");
 		String reason = request.getParameter("reason");
 		String location = request.getParameter("location");
 		String contact = request.getParameter("contact");
 		boolean checked = request.getParameter("default") != null;
 
 		String errMess = "success";
 		String messType = "KWS";
 
		String url = "typeCertifConfig.jsp";/*"parametrage.jsp?error=" + errMess + "&messType="
 				+ messType;
*/
 		TagParameter type = new TagParameter(name, id, reason, location,
 				contact, checked);
 		ControllerAjoutTypeCertification controller = ControllerAjoutTypeCertification
 				.getInstance();
 
 		String action = request.getParameter("action");
 
 		if ("save".equals(action))
 		{
 			if (!controller.addType(type))
 			{
 				errMess = "adding_type_error";
 				url = "addType.jsp?error=" + errMess;
 			}
 		}
 
 		/*
 		 * if ("modify".equals(action)) { request.getRequestDispatcher(
 		 * "addType.jsp?option=modify"?id=" + id + "&name=" + name + "&reason="
 		 * + reason + "&location=" + location + "&contact=" +
 		 * contact).forward(request, response); }
 		 */
 		else if ("edit".equals(action))
 		{
 			String editName = request.getParameter("name");
 			String editId = request.getParameter("id");
 
 			controller.modifyParameters(editId, editName, id, name, reason,
 					location, contact, checked);
 			if (checked)
 				controller.getParameters().setDefaultType(id, name);
 		}
 
 		request.setAttribute("types", controller.getParameters());
		AuthorityParameters autho = ControllerParameter.getInstance().getAutho();
		request.setAttribute("authorityParameter", autho); 
 		request.getRequestDispatcher(url).forward(request, response);
 	}
 
 }
