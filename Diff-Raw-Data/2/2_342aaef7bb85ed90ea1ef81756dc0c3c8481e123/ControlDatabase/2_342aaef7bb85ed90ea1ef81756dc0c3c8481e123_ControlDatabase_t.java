 package controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 
 import daointerfaces.DALException;
 import dto.DeveloperDTO;
 import dto.GenreDTO;
 import dto.LangDTO;
 import dto.PublisherDTO;
 import dto.UsersDTO;
 import funktionalitet.DataLogic;
 import funktionalitet.IDataLogic;
 import funktionalitet.IUserLogic;
 import funktionalitet.UserLogic;
 
 public class ControlDatabase extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 	private IDataLogic dataLogic = null;
 	private UsersDTO user = null;
 	private IUserLogic u = null;
 
 	public ControlDatabase() {
 		super();
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		this.doPost(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		ServletContext application = request.getSession().getServletContext();
 		request.setCharacterEncoding("UTF-8");
 		HttpSession session = request.getSession();
 
 		dataLogic = (DataLogic) application.getAttribute("dataLogic");
 		if (dataLogic == null) {
 			try {
 				dataLogic = new DataLogic();
 				application.setAttribute("dataLogic", dataLogic);
 			} catch (DALException e) {
 				e.printStackTrace();
 				request.setAttribute("error", e.getMessage());
 			}
 		}
 		u = (UserLogic) application.getAttribute("userLogic");
 		if (u == null) {
 			try {
 				u = new UserLogic();
 				application.setAttribute("userLogic", u);
 			} catch (DALException e) {
 				e.printStackTrace();
 				request.setAttribute("error", e.getMessage());
 			}
 		}
 		user = (UsersDTO) session.getAttribute("user");
 		if (user == null) {
 			try {
 				String email = request.getUserPrincipal().getName();
 				user = u.getUser(email);
 				session.setAttribute("user", user);
 			} catch (DALException e) {
 				e.printStackTrace();
 				request.setAttribute("error", e.getMessage());
 			}
 		}
 
 		String action = null;
 		action = request.getParameter("action");
 
 		// Genre administration
 		if ("createGenre".equals(action)) { 
 			request.getRequestDispatcher("../WEB-INF/database/createGenre.jsp").forward(request, response);
 		}
 		else if ("genreFilled".equals(action)) { 
 			try {
 				String genre = request.getParameter("newGenre");
 				dataLogic.createGenre(genre);
 				request.setAttribute("message", genre +" succesfully added");
 				request.getRequestDispatcher("../WEB-INF/database/index.jsp").forward(request, response);
 			} catch (DALException e) {
 				request.setAttribute("error", e.getMessage());
 				request.getRequestDispatcher("../WEB-INF/database/createGenre.jsp").forward(request, response);
 			}
 		}
 		else if ("listGenre".equals(action)) { 
 			try {
 				List<GenreDTO> genreList = new ArrayList<GenreDTO>();
 				genreList = dataLogic.getListGenre();
 				request.setAttribute("List", genreList);
 				request.getRequestDispatcher("../WEB-INF/database/listGenre.jsp").forward(request, response);
 			} catch (DALException e) {
 				request.setAttribute("error", e.getMessage());
 				request.getRequestDispatcher("../WEB-INF/database/index.jsp").forward(request, response);
 			}
 		
 
 		}
 		// Language administration
 		else if ("createLang".equals(action)) { 
 			request.getRequestDispatcher("../WEB-INF/database/createLang.jsp").forward(request, response);
 		}else if ("langFilled".equals(action)) { 
 			try {
 				String lang = request.getParameter("newLang");
 				dataLogic.createLang(lang);
 				request.setAttribute("message", lang +" succesfully added");
 				request.getRequestDispatcher("../WEB-INF/database/index.jsp").forward(request, response);
 			} catch (DALException e) {
 				request.setAttribute("error", e.getMessage());
 				request.getRequestDispatcher("../WEB-INF/database/createLang.jsp").forward(request, response);
 			}
 		}
 		else if ("listLang".equals(action)) { 
 			try {
 				List<LangDTO> langList = new ArrayList<LangDTO>();
 				langList = dataLogic.getListLang();
 				request.setAttribute("List", langList);
 				request.getRequestDispatcher("../WEB-INF/database/listLang.jsp").forward(request, response);
 			} catch (DALException e) {
 				request.setAttribute("error", e.getMessage());
 				request.getRequestDispatcher("../WEB-INF/database/index.jsp").forward(request, response);
 			}
 		
 
 		}
 		// Developer administration
 		else if ("createDev".equals(action)) { 
 				request.getRequestDispatcher("../WEB-INF/database/createDev.jsp").forward(request, response);
 		}else if ("devFilled".equals(action)) { 
 			try {
 				String dev = request.getParameter("newDev");
				String con = request.getParameter("newCon");
 				dataLogic.createDev(dev,con);
 				request.setAttribute("message", dev +" succesfully added");
 				request.getRequestDispatcher("../WEB-INF/database/index.jsp").forward(request, response);
 			} catch (DALException e) {
 				request.setAttribute("error", e.getMessage());
 				request.getRequestDispatcher("index.jsp?action=createDev").forward(request, response);
 			}
 		}
 		else if ("listDev".equals(action)) { 
 			try {
 				List<DeveloperDTO> devList = new ArrayList<DeveloperDTO>();
 				devList = dataLogic.getListDev();
 				request.setAttribute("List", devList);
 				request.getRequestDispatcher("../WEB-INF/database/listDev.jsp").forward(request, response);
 			} catch (DALException e) {
 				request.setAttribute("error", e.getMessage());
 				request.getRequestDispatcher("../WEB-INF/database/index.jsp").forward(request, response);
 			}
 		
 
 		}
 		// Publisher administration
 		else if ("createPub".equals(action)) { 
 				request.getRequestDispatcher("../WEB-INF/database/createPub.jsp").forward(request, response);
 		}else if ("pubFilled".equals(action)) { 
 			try {
 				String pub = request.getParameter("newPub");
 				String con = request.getParameter("newCon");
 				dataLogic.createPub(pub, con);
 				request.setAttribute("message", pub +" succesfully added");
 				request.getRequestDispatcher("../WEB-INF/database/index.jsp").forward(request, response);
 			} catch (DALException e) {
 				request.setAttribute("error", e.getMessage());
 				request.getRequestDispatcher("index.jsp?action=createPub").forward(request, response);
 			}
 		}
 		else if ("listPub".equals(action)) { 
 			try {
 				List<PublisherDTO> pubList = new ArrayList<PublisherDTO>();
 				pubList = dataLogic.getListPub();
 				request.setAttribute("List", pubList);
 				request.getRequestDispatcher("../WEB-INF/database/listPub.jsp").forward(request, response);
 			} catch (DALException e) {
 				request.setAttribute("error", e.getMessage());
 				request.getRequestDispatcher("../WEB-INF/database/index.jsp").forward(request, response);
 			}
 		}else request.getRequestDispatcher("../WEB-INF/database/index.jsp?").forward(request, response);
 	}
 }
