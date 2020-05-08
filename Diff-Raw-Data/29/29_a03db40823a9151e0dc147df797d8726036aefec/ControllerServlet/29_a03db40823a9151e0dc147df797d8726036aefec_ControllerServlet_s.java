 package ass2;
 
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import sun.org.mozilla.javascript.internal.Context;
 import beans.AuctionListBean;
 import beans.UserBean;
 import contollers.AccountController;
 import contollers.AddAuctionController;
 import contollers.AlertController;
 import contollers.BidController;
 import contollers.GetAuctionController;
 import contollers.LoginController;
 import contollers.RegistrationController;
 
 
 /**
  * Servlet implementation class ControllerServlet
  */
 //@WebServlet(name="ControllerServlet",urlPatterns={"/ControllerServlet","/","/home"})
 @MultipartConfig
 public class ControllerServlet extends HttpServlet {
 
 	//TODO fix removing auctions
 	private LinkedList<popAuction> popAuctions;
 
 	private static final String JSP_LOGIN = "/login.jsp";
 	private static final String JSP_HOME = "/index.jsp";
 	private static final String JSP_REGISTRATION = "/registration.jsp";
 	private static final String JSP_MESSAGE = "/message.jsp";
 	private static final String JSP_ADD_AUCTION = "/auction.jsp";
 	private static final String JSP_GET = "/getAuction.jsp";
 	private static final String JSP_CONTROLLER = "/controller";
 	private static final String JSP_ACCOUNT = "/account.jsp";
 	private static final String JSP_WAUCTIONS = "/winningAuctions.jsp";
 	private static final String JSP_ADMIN = "/admin.jsp";
 
 	private static final long serialVersionUID = 1L;
 
 	//private LoginController loginController;
 	//private RegistrationController registrationController;
 
 
 	private ParameterManager pm;
 
 	private final java.util.concurrent.ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
 
 	public ControllerServlet() {
 
 	}
 
 	public void init(){
 		popAuctions = new LinkedList<popAuction>();
 	}
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		pm = new ParameterManager(request.getParameterMap());		
 		response.setContentType("text/html");
 		String forward = JSP_HOME;
 
 		String action = request.getParameter("action");
 
 		UserBean ub = getAccountBean(request);
 		if ("auction".equals(action)) {
 			GetAuctionController gac = new GetAuctionController(pm);
 			request.setAttribute("auction", gac.getAuction());
 
 			if (request.getParameter("id") != null) {
 				BidController bd = new BidController(pm);
 				request.setAttribute("bid", bd.getBids());
 			}
 			forward = JSP_GET;
 		} else if ("wauction".equals(action)) {
 			GetAuctionController gac = new GetAuctionController(pm);
 			if (ub != null) {
 				request.setAttribute("wauction", gac.getWinningAuctions(ub.getUsername()));
 			}
 			forward = JSP_WAUCTIONS;
 		} else if ("account".equals(action)) {
 			AlertController ac = new AlertController(pm);
 			if (ub != null) {
 				request.setAttribute("alert", ac.getAlert(ub.getUsername()));
 			}
 			forward = JSP_ACCOUNT;
 		} else if ("admin".equals(action)) {
 			LoginController lc = new LoginController(pm);
 			if (ub != null && ub.getIsAdmin()) {
 				request.setAttribute("users", lc.getUsers());
 			} else
 				forward = JSP_HOME;
 			forward = JSP_ADMIN;
 		} else if ("verify".equals(action)) {
 			LoginController lc = new LoginController(pm);
 			lc.verify();
 			request.setAttribute("message", lc.getMessage());
 
 		}
 		formatMessage(request);
 
 		request.getRequestDispatcher(forward).forward(request, response);
 	}
 
 
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		pm = new ParameterManager(request.getParameterMap());		
 
 
 
 		UserBean ub = getAccountBean(request);
 
 		//useful for debugging
 		//pm.printAllValues();
 
 		//default go home?
 		String forward = JSP_HOME;
 
 		if (pm.hasParameter("action")) {
 			String action = pm.getIndividualParam("action");
 			if ("addAuction".equals(action)) {
 
 				forward = doAddAuction(request);
 			}
 			else if ("halt_auction".equals(action)) {
 				forward = doHaltAuction(request, ub);
 			}
 			else if ("halt_all_auctions".equals(action)) {
 				forward = doHaltAllAuctions(request, ub);
 			}
 			else if ("remove_auction".equals(action)) {
 				forward = doRemoveAuction(request, ub);
 			}
 			else if ("ban_user".equals(action)) {
 				forward = doBanUser(request, ub);
 			}
 			else if ("logout".equals(action)) {
 				request.getSession().removeAttribute("account");
 				forward = JSP_HOME;
 			}
 			else if ("login".equals(action)) {
 				forward = doLogin(request, false);
 			}
 			else if ("adminLogin".equals(action)) {
 				forward = doLogin(request, true);
 			}
 			else if ("register".equals(action)) {
 				forward = doRegister(request);
 			}
 			else if ("bid".equals(action)) {
 				forward = doBid(request);
 			}
 			else if ("win".equals(action)) {
 				doWinAuction(ub);
 			}
 			else if ("removeAlert".equals(action)) {
 				forward = doRemoveAlert(request, ub);
 			}
 			else if (("update").equals(action)) {
 				AccountController ac = new AccountController(pm);
 				forward= JSP_ACCOUNT;
 
 				if (ub != null) {
 					if (ac.isInvalidForm()) {
 						request.setAttribute("message", ac.getFormMessage());
 					} else {
						request.setAttribute("message", "Successfully updated<br />");
						if  (ac.hasChanged()) {
							request.setAttribute("message", "Updated: " + ac.getChangedDetails() + "<br />");
							ac.updateAccount(ub);
 
							//i think we need to update the session as the bean changes??
 							request.getSession().setAttribute("account", ub);
 						} else {
 							request.setAttribute("message", "No changes made.");
 						}
 					}
 				}
 			}
 		}
 
 		formatMessage(request);
		
 
 		//responsible for saving cached copies of form data
 		if (request.getSession().getAttribute("oldParameters") != null) {
 			System.out.println("oldParams exists");
 			ParameterManager oldParameterMap = (ParameterManager) request.getSession().getAttribute("oldParameters");
 			createFormParameters(request, oldParameterMap);
 			request.getSession().removeAttribute("oldParameters");
 		} else {
 			//System.out.println("oldParams not exists");
 
 		}
 
 		RequestDispatcher requestDispatcher = request.getServletContext().getRequestDispatcher(forward);
 		System.out.println("Forwarding to: " + forward);
 		requestDispatcher.forward(request, response);
 	}
 
 	private void createFormParameters(HttpServletRequest request, ParameterManager oldParameterMap) {
 		Map<String,String[]> map = oldParameterMap.getMap();
 		for(String keys: map.keySet()) {
 			for (String value: map.get(keys)) {
 				request.setAttribute("formValue_" + keys, value);
 				System.out.println("Setting attribute: formValue_" +keys+ " as "+  value);
 			}
 		}
 	}
 
 	private void formatMessage(HttpServletRequest request) {
 		String message = (String) request.getAttribute("message");
 		if (message != null) {
 			message = "<font color=\"red\">" + message + "</font>";
 		}
 		request.setAttribute("message", message);
 	}
 
 	private String doRemoveAlert(HttpServletRequest request, UserBean ub) {
 		String forward;
 		AlertController ac = new AlertController(pm);
 		if (ub != null) {
 			ac.removeAlert(ub.getUsername());
 			request.setAttribute("alert", ac.getAlert(ub.getUsername()));
 		}
 		forward = JSP_ACCOUNT;
 		return forward;
 	}
 
 	private void doWinAuction(UserBean ub) {
 		GetAuctionController gac = new GetAuctionController(pm);
 		if (ub!= null) {
 			gac.winAuction(ub.getUsername());
 		}
 	}
 
 	private String doBanUser(HttpServletRequest request, UserBean ub) {
 		String forward = JSP_ADMIN;
 		LoginController lc = new LoginController(pm);
 		if (ub!= null && ub.getIsAdmin()) {
 			lc.banUsers();
 
 			GetAuctionController gac = new GetAuctionController(pm);
 			LinkedList<Integer> ids = null;
 			ids = gac.haltAllAuctions();
 
 			Iterator<popAuction> i = popAuctions.iterator();
 			while (i.hasNext()) {
 				popAuction pa = i.next();
 				for (int id : ids) {
 					popAuction tmp = new popAuction(id);
 					if (pa.equals(tmp))
 						pa.stop();
 				}
 			}
 			request.setAttribute("users", lc.getUsers());
 		} else
 			forward = JSP_HOME;
 		return forward;
 	}
 
 	private String doRemoveAuction(HttpServletRequest request, UserBean ub) {
 		String forward;
 		GetAuctionController gac = new GetAuctionController(pm);
 		if (ub != null && ub.getIsAdmin()) {
 			gac.deleteAuction();
 			Iterator<popAuction> i = popAuctions.iterator();
 			popAuction tmp = new popAuction(Integer.parseInt(request.getParameter("id")));
 			while (i.hasNext()) {
 				popAuction pa = i.next();
 				if (pa.equals(tmp))
 					pa.stop();
 			}
 		}
 
 		forward = JSP_HOME;
 		return forward;
 	}
 
 	private String doHaltAllAuctions(HttpServletRequest request, UserBean ub) {
 		String forward;
 		GetAuctionController gac = new GetAuctionController(pm);
 		LinkedList<Integer> ids = null;
 		if (ub != null && ub.getIsAdmin()) {
 			ids = gac.haltAllAuctions();
 			LoginController lc = new LoginController(pm);
 			request.setAttribute("users", lc.getUsers());
 		}
 
 		Iterator<popAuction> i = popAuctions.iterator();
 		while (i.hasNext()) {
 			popAuction pa = i.next();
 			for (int id : ids) {
 				popAuction tmp = new popAuction(id);
 				if (pa.equals(tmp))
 					pa.stop();
 			}
 		}
 
 		forward = JSP_ADMIN;
 		return forward;
 	}
 
 	private String doHaltAuction(HttpServletRequest request, UserBean ub) {
 		String forward;
 		GetAuctionController gac = new GetAuctionController(pm);
 		if (ub != null && ub.getIsAdmin()) {
 			gac.haltAuction(request.getRequestURL().toString());
 			Iterator<popAuction> i = popAuctions.iterator();
 			popAuction tmp = new popAuction(Integer.parseInt(request.getParameter("id")));
 			while (i.hasNext()) {
 				popAuction pa = i.next();
 				if (pa.equals(tmp))
 					pa.stop();
 			}
 		}
 
 		request.setAttribute("auction", gac.getAuction());
 
 		if (request.getParameter("id") != null) {
 			BidController bd = new BidController(pm);
 			request.setAttribute("bid", bd.getBids());
 		}
 		forward = JSP_GET;
 		return forward;
 	}
 
 	private UserBean getAccountBean(HttpServletRequest request) {
 		return (UserBean) request.getSession().getAttribute("account");
 	}
 
 	private String doAddAuction(HttpServletRequest request) throws IOException,
 	ServletException {
 		String forward = JSP_ADD_AUCTION;
 		AddAuctionController ac = new AddAuctionController(pm);
 
 		if (ac.isInvalidForm() || request.getPart("picture").getSize() <= 0) {
 			String msg = ac.getFormMessage();
 			if (request.getPart("picture").getSize() <= 0)
 				msg += "picture not specified.\n";
 			request.setAttribute("message", msg);
			
 			request.getSession().setAttribute("oldParameters", pm);
 
 		} else {
 			UserBean ub;
 			if ((ub = (UserBean) request.getSession().getAttribute("account")) != null && !ub.getIsAdmin() && !ub.getIsBanned()) {
 				int id = ac.addAuction(request.getPart("picture"), getServletContext().getRealPath("/"), "auction_images/", ub.getUsername());
 				if (id > 0) {
 					forward=JSP_HOME;
 					popAuction pa = new popAuction(id);
 					scheduler.schedule(pa, Integer.parseInt(request.getParameter("auctionEnd")), TimeUnit.MINUTES);
 					popAuctions.add(pa);
 					request.setAttribute("message", "Added new auction");
 				} else
 					request.setAttribute("message", ac.getMessage());
 			} else {
 				request.setAttribute("message", "You cannot place an auction.");
 			}
 		}
 		return forward;
 	}
 
 	private String doBid(HttpServletRequest request) {
 		String forward;
 		BidController bd = new BidController(pm);
 		GetAuctionController gac = new GetAuctionController(pm);
 		UserBean ub = null;
 		AuctionListBean alb = gac.getAuction();
 		if (bd.isInvalidForm()) {
 			request.setAttribute("message", bd.getFormMessage());
 		} else {
 			bd.message = "something went wrong (modified data/priviledges)<br>";
 			if ((ub = (UserBean) request.getSession().getAttribute("account")) != null &&
 					!alb.isEmpty() && !alb.getAuctions().get(0).getFinished()) {
 				if (bd.addBid(ub.getUsername(), ub.getEmail(), request.getRequestURL().toString()) == false) {
 					Iterator<popAuction> i = popAuctions.iterator();
 					popAuction tmp = new popAuction(Integer.parseInt(request.getParameter("id")));
 					while (i.hasNext()) {
 						popAuction pa = i.next();
 						if (pa.equals(tmp))
 							pa.stop();
 					}
 
 					GetAuctionController.popAuction(Integer.parseInt(request.getParameter("id")), true);
 				}
 			}
 			request.setAttribute("message", bd.message);
 		}
 		alb = gac.getAuction();
 		request.setAttribute("auction", alb);
 		forward = JSP_GET;
 		if (request.getParameter("id") != null) {
 			request.setAttribute("bid", bd.getBids());
 		}
 
 		return forward;
 	}
 
 	private String doLogin(HttpServletRequest request, boolean isAdminLogin) {
 		String forward;
 		LoginController loginController = new LoginController(pm);
 		if (loginController.isInvalidForm()) {
 			//entered the wrong information, keep them on the same page
 			forward = JSP_LOGIN;
 			request.setAttribute("message", loginController.getFormMessage());
 
 			request.getSession().setAttribute("oldParameters", pm);
 		} else {
 			//check to see if its a valid account					
 			UserBean ub = loginController.requestLogin(isAdminLogin);
 			if (ub != null) {
 				request.getSession().setAttribute("account", ub);
 				forward = JSP_HOME;
 			} else {
 				request.setAttribute("message", loginController.getMessage());
 				forward = JSP_LOGIN;
 			}
 		}
 		return forward;
 	}
 
 	private String doRegister(HttpServletRequest request) {
 		String forward;
 		RegistrationController registrationController = new RegistrationController(pm);
 		if (registrationController.isInvalidForm()) {
 			forward = JSP_REGISTRATION;
 			request.setAttribute("message", registrationController.getFormMessage());
 
 			//System.out.println("SETTING OLD PARAMETERS as ");
 			//pm.printAllValues();
 			//System.out.println("-----");
 			request.getSession().setAttribute("oldParameters", pm);
 
 		} else {
 			if (registrationController.isAccountAlreadyExists()) {
 				forward = JSP_REGISTRATION;
 				request.setAttribute("message", registrationController.getMessage());
 			} else {
 				forward = JSP_MESSAGE;
 				request.setAttribute("message", "You have been registered, an email will be sent to you to confirm your account");
 
 				//TODO make sure to securily parse strings
 
 				registrationController.registerUser(request.getRequestURL().toString());
 
 			}
 		}
 		return forward;
 	}
 
 }
