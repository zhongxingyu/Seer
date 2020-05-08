 package controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import daoimpl.MySQLGameDAO;
 import daoimpl.MySQLLangDAO;
 import daointerfaces.DALException;
 import daointerfaces.GameIDAO;
 import daointerfaces.LangIDAO;
 import dto.GameDTO;
 import dto.LangDTO;
 import dto.RankDTO;
 import dto.UsersDTO;
 import funktionalitet.*;
 
 
 
 public class ControlStat extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private IUserLogic userLogic = null;
 	private UsersDTO user = null;
 	private IStat statLogic = null;
 	private LangIDAO lan = null;
 	private GameIDAO games = null;
 
 
 	public ControlStat() {
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
 		HttpSession session = request.getSession();
 		request.setCharacterEncoding("UTF-8");
 
 		// Create path to user logic
 		userLogic = (UserLogic) application.getAttribute("userLogic");
 		if (userLogic == null) {
 			try {
 				userLogic = new UserLogic();
 				application.setAttribute("userLogic", userLogic);
 			} catch (DALException e) {
 				e.printStackTrace();
 				request.setAttribute("error", e.getMessage());
 			}
 		}
 		statLogic = (StatLogic) application.getAttribute("statLogic");
 		if (statLogic == null) {
 			try {
 				statLogic = new StatLogic();
 				application.setAttribute("statLogic", statLogic);
 			} catch (DALException e) {
 				e.printStackTrace();
 				request.setAttribute("error", e.getMessage());
 			}
 		}
 
 		// Create user bean if not already existing.
 		user = (UsersDTO) session.getAttribute("user");
 		if (user == null) {
 			try {
 				String email = request.getUserPrincipal().getName();
 				user = userLogic.getUser(email);
 				session.setAttribute("user", user);
 			} catch (DALException e) {
 				e.printStackTrace();
 				request.setAttribute("error", e.getMessage());
 			}
 		}
 		lan = (MySQLLangDAO) application.getAttribute("LangDAO");
 		if (lan == null) {
 			lan = new MySQLLangDAO();
 			application.setAttribute("LangDAO", lan);
 		}
 		games = (MySQLGameDAO) session.getAttribute("games");
 		if (games == null) {
 			games = new MySQLGameDAO();
 			application.setAttribute("games", games);
 		}
 		// Getting the action parameter.
 		String action = null;
 		action = request.getParameter("action");
 
 		if ("countPlayers".equals(action)) {
 			String game = request.getParameter("statGame");
 			String sex = request.getParameter("statSex");
 			String lang = request.getParameter("statLang");
 			String min = request.getParameter("statMin");
 			String max = request.getParameter("statMax");
 			int[] i = {0};
 			List<GameDTO> gameList = null;
 			List<LangDTO> langList = null;
 			try {
 				i[0] = statLogic.countPLayers(game, sex, lang, min, max);
 				gameList = new ArrayList<GameDTO>(games.getList());
 				langList = new ArrayList<LangDTO>(lan.getList());
 			} catch (DALException e) {
 				request.setAttribute("error", e.getMessage());
 			}
 
 			String[] names = {"All"};
 			if (!game.equalsIgnoreCase("null")){
 				try {
 					names[0] = games.getById(Integer.parseInt(game)).getGname();
 				} catch (NumberFormatException e) {
 					request.setAttribute("error", e.getMessage());
 				} catch (DALException e) {
 					request.setAttribute("error", e.getMessage());
 				}
 			}
 			request.setAttribute("array", i);
 			request.setAttribute("names", names);
 			request.setAttribute("gameList", gameList);
 			request.setAttribute("langList", langList);
 			request.setAttribute("graphBars", true);
 			request.setAttribute("graphSevBars", false);
 			request.setAttribute("graphSevLines", false);
 			request.getRequestDispatcher("../WEB-INF/stat/index.jsp?").forward(request, response);
 		}else if("rankGames".equals(action)){
 			String trank = request.getParameter("popular");
 			int rank = Integer.parseInt(trank);
 			List<RankDTO> rankList = null;
 			List<GameDTO> gameList = null;
 			List<LangDTO> langList = null;
 			try {
 				rankList = statLogic.getMostOwnedGame(rank);
 				gameList = new ArrayList<GameDTO>(games.getList());
 				langList = new ArrayList<LangDTO>(lan.getList());
 			} catch (NumberFormatException e) {
 				request.setAttribute("error", e.getMessage());
 			} catch (DALException e) {
 				request.setAttribute("error", e.getMessage());
 			}
 			String[] names = new String[rank];
 			int[] array = new int[rank];
 			for(int i = 0 ; i<rankList.size() ; i ++){
 				RankDTO rankdto = rankList.get(i);
 				names[i] = rankdto.getGname();
 				array[i] = rankdto.getCount();
 			}
 			request.setAttribute("gameList", gameList);
 			request.setAttribute("langList", langList);
 			request.setAttribute("array", array);
 			request.setAttribute("names", names);	
 			request.setAttribute("graphBars", true);
 			request.setAttribute("graphSevBars", false);
 			request.setAttribute("graphSevLines", false);
 			request.getRequestDispatcher("../WEB-INF/stat/index.jsp?").forward(request, response);
 		}
 		else if("rankSex".equals(action)){
 			String trank = request.getParameter("sexPopular");
 			int rank = Integer.parseInt(trank);
 			List<RankDTO> rankList = null;
 			List<GameDTO> gameList = null;
 			List<LangDTO> langList = null;
 			try {
 				rankList = statLogic.getMostOwnedGameMW(rank);
 				gameList = new ArrayList<GameDTO>(games.getList());
 				langList = new ArrayList<LangDTO>(lan.getList());
 			} catch (NumberFormatException e) {
 				request.setAttribute("error", e.getMessage());
 			} catch (DALException e) {
 				request.setAttribute("error", e.getMessage());
 			}
 			String[] names = new String[rank];
 			int[] arrayMen = new int[rank];
 			int[] arrayGirl = new int[rank];
 			for(int i = 0 ; i<rankList.size() ; i ++){
 				RankDTO rankdto = rankList.get(i);
 				names[i] = rankdto.getGname();
 				arrayMen[i] = rankdto.getMen();
 				arrayGirl[i] = rankdto.getWoman();
 			}
 			request.setAttribute("gameList", gameList);
 			request.setAttribute("langList", langList);
 			request.setAttribute("arrayM", arrayMen);
 			request.setAttribute("arrayG", arrayGirl);
 			request.setAttribute("names", names);	
 			request.setAttribute("graphBars", false);
 			request.setAttribute("graphSevBars", true);
 			request.setAttribute("graphSevLines", false);
 			request.getRequestDispatcher("../WEB-INF/stat/index.jsp?").forward(request, response);
 		}else if("rankCountry".equals(action)){
 			String gameMax = request.getParameter("popular");
 			String sex = request.getParameter("statSex");
 			String country = request.getParameter("statLang");
 			String minDOB = request.getParameter("statMin");
 			String maxDOB = request.getParameter("statMax");
 			List<RankDTO> rankList = null;
 			List<GameDTO> gameList = null;
 			List<LangDTO> langList = null;
 			try {
 				rankList = statLogic.getMostOwnedGame(gameMax, sex, country, minDOB, maxDOB);
 				gameList = new ArrayList<GameDTO>(games.getList());
 				langList = new ArrayList<LangDTO>(lan.getList());
 			} catch (NumberFormatException e) {
 				request.setAttribute("error", e.getMessage());
 			} catch (DALException e) {
 				request.setAttribute("error", e.getMessage());
 			}
 			int size = rankList.size();
			if (size != 0){
				String[] names = new String[size];
				int[] array = new int[size];
				for(int i = 0 ; i<rankList.size() ; i ++){
					RankDTO rankdto = rankList.get(i);
					names[i] = rankdto.getGname();
					array[i] = rankdto.getCount();
				}
				request.setAttribute("array", array);
				request.setAttribute("names", names);
			}else {
				String[] names = new String[0];
				int[] array = {0};
				request.setAttribute("array", array);
				request.setAttribute("names", names);
 			}
 			request.setAttribute("gameList", gameList);
 			request.setAttribute("langList", langList);
 			request.setAttribute("graphBars", true);
 			request.setAttribute("graphSevBars", false);
 			request.setAttribute("graphSevLines", false);
 			request.getRequestDispatcher("../WEB-INF/stat/index.jsp?").forward(request, response);
 		}
 		else {
 			List<GameDTO> gameList;
 			List<LangDTO> langList;
 			try {
 				gameList = new ArrayList<GameDTO>(games.getList());
 				langList = new ArrayList<LangDTO>(lan.getList());
 				request.setAttribute("gameList", gameList);
 				request.setAttribute("langList", langList);
 				request.setAttribute("graphBars", false);
 				request.setAttribute("graphSevBars", false);
 				request.setAttribute("graphSevLines", false);
 				request.getRequestDispatcher("../WEB-INF/stat/index.jsp?").forward(request, response);
 			} catch (DALException e) {
 				request.setAttribute("error", e.getMessage());
 				request.getRequestDispatcher("../WEB-INF/stat/index.jsp?").forward(request, response);
 			}
 		}

 	}
 }
