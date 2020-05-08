 package gov.nih.nci.cadsr.cadsrpasswordchange.core;
 
 import gov.nih.nci.cadsr.cadsrpasswordchange.domain.UserSecurityQuestion;
 
 import java.io.IOException;
 import java.security.GeneralSecurityException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.sql.DataSource;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 import org.apache.log4j.Logger;
 
 public class MainServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = Logger.getLogger(MainServlet.class.getName());
 //	private static Connection connection = null;
 	private static DataSource datasource = null;
 	private static PasswordChange dao;
 	private static String HELP_LINK;
 	private static String LOGO_LINK;
 
     private static void connect() {
 		boolean isConnectionException = true;  // use to modify returned messages when exceptions are system issues instead of password change issues  
     	
 		Result result = new Result(ResultCode.UNKNOWN_ERROR);  // (should get replaced)
         try {
 //    		if(connection == null) {
             	datasource = ConnectionUtil.getDS(PasswordChangeDAO._jndiSystem);
             	dao = new PasswordChangeDAO(datasource);
             	logger.info("Connected to database");
 //    		}
         	isConnectionException = false;
 		} catch (Exception e) {
 			e.printStackTrace();
 			if (isConnectionException)
 				result = new Result(ResultCode.UNKNOWN_ERROR);  // error not related to user, provide a generic error 
 			else
 				result = ConnectionUtil.decode(e);			
 		}
 	}
 
     private static void disconnect() {
 		try {
 			datasource.getConnection().close();
 	    	datasource = null;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     }
     
     private static final int TOTAL_QUESTIONS = 3;
     private static final String ERROR_MESSAGE_SESSION_ATTRIBUTE = "ErrorMessage"; 
     private static final String USER_MESSAGE_SESSION_ATTRIBUTE = "UserMessage";
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		logger.debug("doGet");
 	}
 		
 	private void handleQuestionsOptions(HttpServletRequest req, String[] selectedQuestion) {
 		req.getSession().setAttribute("selectedQuestion1", selectedQuestion[0]);
 		req.getSession().setAttribute("selectedQuestion2", selectedQuestion[1]);
 		req.getSession().setAttribute("selectedQuestion3", selectedQuestion[2]);
 	}
 	
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		logger.info("doPost");
 		QuestionHelper.initQuestionsOptions(req);		
 		
 		try {
 			String servletPath = req.getServletPath();
 			logger.debug("getServletPath  |" + servletPath +"|");
 			if (servletPath.equals(Constants.SERVLET_URI + "/login")) {
 				doLogin(req, resp);
 			} else if (servletPath.equals(Constants.SERVLET_URI + "/changePassword")) {
 				if(req.getParameter("cancel") != null) {
 					resp.sendRedirect(Constants.LANDING_URL);
 				} else {
 					doChangePassword(req, resp);
 				}
 			} else if (servletPath.equals(Constants.SERVLET_URI + "/saveQuestions")) {
 				if(req.getParameter("cancel") != null) {
 					resp.sendRedirect(Constants.LANDING_URL);
 				} else {
 					doSaveQuestions(req, resp);
 				}
 			} else if (servletPath.equals(Constants.SERVLET_URI + "/promptUserQuestions")) {
 				if(req.getParameter("cancel") != null) {
 					resp.sendRedirect(Constants.LANDING_URL);
 				} else {				
 				doRequestUserQuestions(req, resp);
 				}
 			} else if (servletPath.equals(Constants.SERVLET_URI + "/promptQuestion1")) {
 				doQuestion1(req, resp);
 			} else if (servletPath.equals(Constants.SERVLET_URI + "/promptQuestion2")) {
 				doQuestion2(req, resp);
 			} else if (servletPath.equals(Constants.SERVLET_URI + "/promptQuestion3")) {
 				doQuestion3(req, resp);
 			} else if (servletPath.equals(Constants.SERVLET_URI + "/validateQuestion1")) {
 				if(req.getParameter("cancel") != null) {
 					resp.sendRedirect(Constants.LANDING_URL);
 				} else {
 				doValidateQuestion1(req, resp);
 				}
 			} else if (servletPath.equals(Constants.SERVLET_URI + "/validateQuestion2")) {
 				if(req.getParameter("cancel") != null) {
 					resp.sendRedirect(Constants.LANDING_URL);
 				} else {
 				doValidateQuestion2(req, resp);
 				}
 			} else if (servletPath.equals(Constants.SERVLET_URI + "/validateQuestion3")) {
 				if(req.getParameter("cancel") != null) {
 					resp.sendRedirect(Constants.LANDING_URL);
 				} else {
 				doValidateQuestion3(req, resp);
 				}
 			} else if (servletPath.equals(Constants.SERVLET_URI + "/resetPassword")) {
 				if(req.getParameter("cancel") != null) {
 					resp.sendRedirect(Constants.LANDING_URL);
 				} else {
 					doChangePassword2(req, resp);
 				}
 			} else {
 				// this also catches the intentional logout with path /logout 
 				logger.info("logging out because of invalid servlet path");				
 				HttpSession session = req.getSession(false);
 				if (session != null) {
 					logger.debug("non-null session");					
 					session.invalidate();
 				}				
 				resp.sendRedirect("./jsp/loggedOut.jsp");
 			}
 		}
 		catch (Throwable theException) {
 			logger.error(CommonUtil.toString(theException));
 		}
 	}
 
 	private void doQuestion3(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		resp.sendRedirect(Constants.RESET_URL);
 	}
 
 	private void doQuestion2(HttpServletRequest req, HttpServletResponse resp) throws Exception {
 		doValidateQuestion2(req, resp);
 	}
 
 
 	private void doQuestion1(HttpServletRequest req, HttpServletResponse resp) {
 		logger.info("doQuestion1");
 		
 		try {
 			HttpSession session = req.getSession(false);
 			if (session == null) {
 				logger.debug("null session");
 				// this shouldn't happen, make the user start over
 				resp.sendRedirect("./jsp/loggedOut.jsp");
 				return;
 			}
 
 			String username = req.getParameter("userid");
 			
 			logger.debug("username " + username);			
 			
 			// Security enhancement
 			Map<String, String> userQuestions = new HashMap<String, String>();
 			Map<String, String> userAnswers =  new HashMap<String, String>();
 			
 			//pull all questions related to this user
 			loadUserStoredQna(username, userQuestions, userAnswers);
 			
 			//TBD - retrieve all questions related to the users from dao and set them into sessions
 			session.setAttribute(Constants.USERNAME, username);
 			session.setAttribute(Constants.Q1, userQuestions.get(Constants.Q1));
 			session.setAttribute(Constants.ALL_QUESTIONS, userQuestions);
 			session.setAttribute(Constants.ALL_ANSWERS, userAnswers);
 			
 			if(userQuestions.size() == 0) {
 				logger.info("no security question found");
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.140"));
 				resp.sendRedirect(Constants.ASK_USERID_URL);
 			} else {
 				//resp.sendRedirect(Constants.Q1_URL);
 				req.getRequestDispatcher("./jsp/askQuestion1.jsp").forward(req, resp);
 			}
 		}
 		catch (Throwable theException) {
 			logger.error(theException);
 		}
 	}
 
 	private void saveUserStoredQna(String username, Map<String, String> userQuestions, Map<String, String> userAnswers) throws Exception {
 		UserSecurityQuestion qna = new UserSecurityQuestion();
 		try {
 		qna.setUaName(username);
 		qna.setQuestion1((String)userQuestions.get(Constants.Q1));
 		qna.setAnswer1(CommonUtil.encode((String)userAnswers.get(Constants.A1)));
 		qna.setQuestion2((String)userQuestions.get(Constants.Q2));
 		qna.setAnswer2(CommonUtil.encode((String)userAnswers.get(Constants.A2)));
 		qna.setQuestion3((String)userQuestions.get(Constants.Q3));
 		qna.setAnswer3(CommonUtil.encode((String)userAnswers.get(Constants.A3)));
 		} catch (GeneralSecurityException e1) {
 			e1.printStackTrace();
 		}
 
 		try {
 			connect();
 			PasswordChangeDAO dao = new PasswordChangeDAO(datasource);
 			UserSecurityQuestion oldQna = dao.findByUaName(username);
 
 			connect();			
 			PasswordChangeDAO dao1 = new PasswordChangeDAO(datasource);
 			if(oldQna == null) {
 				dao1.insert(qna);
 			} else {
 				dao1.update(username, qna);
 			}
 			//showUserSecurityQuestionList();	//just for debug
 			disconnect();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void getUserStoredAttemptedCount(String username) throws Exception {
 		try {
 			connect();
 			PasswordChangeDAO dao = new PasswordChangeDAO(datasource);
 			UserSecurityQuestion oldQna = dao.findByUaName(username);
 			if(oldQna == null) {
 				throw new Exception("Questions have to exists before attempted count can be updated.");
 			}
 			
 			connect();
 			PasswordChangeDAO dao1 = new PasswordChangeDAO(datasource);
 			long count = 1;
 			if(oldQna.getAttemptedCount() != null) {
 				count = oldQna.getAttemptedCount().longValue() + 1;
 			}
 			oldQna.setAttemptedCount(new Long(count));
 			dao1.update(username, oldQna);
 			//showUserSecurityQuestionList();	//just for debug
 			disconnect();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void updateUserStoredAttemptedCount(String username) throws Exception {
 		try {
 			connect();
 			PasswordChangeDAO dao = new PasswordChangeDAO(datasource);
 			UserSecurityQuestion oldQna = dao.findByUaName(username);
 			if(oldQna == null) {
 				throw new Exception("Questions have to exists before attempted count can be updated.");
 			}
 			
 			connect();
 			PasswordChangeDAO dao1 = new PasswordChangeDAO(datasource);
 			long count = 1;
 			if(oldQna.getAttemptedCount() != null) {
 				count = oldQna.getAttemptedCount().longValue() + 1;
 			}
 			oldQna.setAttemptedCount(new Long(count));
 			dao1.update(username, oldQna);
 			//showUserSecurityQuestionList();	//just for debug
 			disconnect();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	//Please the following method for debugging
 	/*
 	private void showUserSecurityQuestionList() {
 		UserSecurityQuestion[] results;
 		try {
 			connect();			
 			results = dao.findAll();
 			if (results.length > 0) {
 				for (UserSecurityQuestion e : results) {
 					System.out.println("User [" + e.getUaName() + "] updated ["
 							+ new Date() + "] question [" + e.getQuestion1()
 							+ "] answer [" + e.getAnswer1() + "]");
 				}
 			} else {
 				System.out.println("no question");
 			}
 			disconnect();
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 	}
 	*/
 	
 	protected void doLogin(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 		init();
 		
 		logger.info("doLogin");
 
 		UserBean userBean = null;
 		
 		try {	
 			HttpSession session = req.getSession(false);
 			if (session == null) {
 				logger.debug("null session");
 				// this shouldn't happen, make the user start over
 				resp.sendRedirect("./jsp/loggedOut.jsp");
 				return;
 			}
 			
 			session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, "");
 
 			String username = req.getParameter("userid");
 			String password = req.getParameter("pswd");
 			logger.info("unvalidated username " + username);
 
 			// Limit input to legal characters before attempting any processing
 			if(Messages.getString("PasswordChangeHelper.1").equals(PasswordChangeHelper.validateLogin(username, password))) {
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.1"));
 				resp.sendRedirect("./jsp/login.jsp");				
 				return;
 			}
 
 			if(Messages.getString("PasswordChangeHelper.2").equals(PasswordChangeHelper.validateLogin(username, password))) {
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.2"));
 				resp.sendRedirect("./jsp/login.jsp");				
 				return;
 			}
 
 			connect();
 			PasswordChangeDAO loginDAO = new PasswordChangeDAO(datasource);
 			userBean = loginDAO.checkValidUser(username, password);
 			disconnect();
 			session.setAttribute(UserBean.USERBEAN_SESSION_ATTRIBUTE, userBean);		
 			logger.debug ("validUser " + userBean.isLoggedIn());
 			logger.debug ("resultCode " + userBean.getResult().getResultCode().toString());
 			if (userBean.isLoggedIn()) {
 				//preload the questions
 				QuestionHelper.initQuestionsOptions(req);		
 				
 				// Provide a user message that notes the "expired" status
 				String userMessage = userBean.getResult().getMessage();
 				logger.debug ("userMessage " + userMessage);
 				session.setAttribute(USER_MESSAGE_SESSION_ATTRIBUTE, userMessage);
 				session.setAttribute("username", username);
 				resp.sendRedirect("./jsp/changePassword.jsp"); //logged-in page
 			} else {
 				String errorMessage1 = userBean.getResult().getMessage();
 				logger.debug ("errorMessage " + errorMessage1);
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, errorMessage1);
 				resp.sendRedirect("./jsp/login.jsp");				
 			}
 		}
 		catch (Throwable e) {
 			e.printStackTrace();
 			logger.error(e.getMessage());
 		}
 	
 	}
 
 	protected void doSaveQuestions(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 
 		logger.info("doSaveQuestions");
 		
 		try {
 //			req.getSession().invalidate();	//invalid session everytime
 //			HttpSession session = req.getSession(true);
 			HttpSession session = req.getSession(false);	//caDSR Password Change Station CADSRPASSW-43 Reset security questions/answers are the same
 			if (session == null) {
 				logger.debug("null session");
 				// this shouldn't happen, make the user start over
 				resp.sendRedirect("./jsp/loggedOut.jsp");
 				return;
 			}
 			
 			// Security enhancement
 			int paramCount = 0;
			String loginID = req.getParameter("userid");	//CADSRPASSW-40
 			String question1 = req.getParameter("question1");
 			String answer1 = req.getParameter("answer1");
 			String question2 = req.getParameter("question2");
 			String answer2 = req.getParameter("answer2");
 			String question3 = req.getParameter("question3");
 			String answer3 = req.getParameter("answer3");
 			
 			//"remember" the questions selected by the user
 			String selectedQ[] = {question1, question2, question3};
 			handleQuestionsOptions(req, selectedQ);
			req.getSession().setAttribute("userid", loginID);	//CADSRPASSW-40
 			
 			session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, "");			
 			UserBean userBean = (UserBean) session.getAttribute(UserBean.USERBEAN_SESSION_ATTRIBUTE);
 			
 			String username = req.getParameter("userid");
 			String password = req.getParameter("password");
 			
 			logger.debug("username " + username);
 			//xss prevention (http://ha.ckers.org/xss.html)
 			if(!StringEscapeUtils.escapeHtml4(answer1).equals(answer1) ||
 					!StringEscapeUtils.escapeHtml4(answer2).equals(answer2) ||
 					!StringEscapeUtils.escapeHtml4(answer3).equals(answer3)) {
 				logger.debug("invalid character failed during questions/answers save");
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.160"));
 				//req.getRequestDispatcher(Constants.SETUP_QUESTIONS_URL).forward(req, resp);		//didn't work for jboss 4.0.5
 				req.getRequestDispatcher("./jsp/setupPassword.jsp").forward(req, resp);
 				return;
 			}
 			
 			//DoS attack using string length overflow
 			if(!CommonUtil.truncate(answer1, Constants.MAX_ANSWER_LENGTH).equals(answer1) ||
 					!CommonUtil.truncate(answer2, Constants.MAX_ANSWER_LENGTH).equals(answer2) ||
 					!CommonUtil.truncate(answer3, Constants.MAX_ANSWER_LENGTH).equals(answer3) ||
 					!CommonUtil.truncate(question1, Constants.MAX_ANSWER_LENGTH).equals(question1) ||
 					!CommonUtil.truncate(question2, Constants.MAX_ANSWER_LENGTH).equals(question2) ||
 					!CommonUtil.truncate(question3, Constants.MAX_ANSWER_LENGTH).equals(question3)) {
 				logger.debug("invalid answer(s) length during questions/answers save");
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.112"));
 //				req.getRequestDispatcher(Constants.SETUP_QUESTIONS_URL).forward(req, resp);		//didn't work for jboss 4.0.5
 				req.getRequestDispatcher("./jsp/setupPassword.jsp").forward(req, resp);
 				return;
 			}
 			
 			connect();
 			PasswordChangeDAO loginDAO = new PasswordChangeDAO(datasource);
 			userBean = loginDAO.checkValidUser(username, password);
 			disconnect();
 			session.setAttribute(UserBean.USERBEAN_SESSION_ATTRIBUTE, userBean);		
 			logger.debug ("validUser" + userBean.isLoggedIn());
 			logger.debug ("resultCode " + userBean.getResult().getResultCode().toString());
 			if (!userBean.isLoggedIn()) {
 				logger.debug("auth failed during questions/answers save");
 				if(userBean.getResult().getResultCode() != ResultCode.LOCKED_OUT) {
 					session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.102"));
 				} else {
 					session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.103"));
 				}
 				//req.getRequestDispatcher(Constants.SETUP_QUESTIONS_URL).forward(req, resp);		//didn't work for jboss 4.0.5
 				req.getRequestDispatcher("./jsp/setupPassword.jsp").forward(req, resp);
 				return;
 			}
 			
 			// Security enhancement
 		    Map<String, String> userQuestions = new HashMap<String, String>();
 		    userQuestions.put(question1,"");
 		    userQuestions.put(question2,"");
 		    userQuestions.put(question3,"");
 		    if(question1 != null && !question1.equals("")) paramCount++;
 		    if(question2 != null && !question2.equals("")) paramCount++;
 		    if(question3 != null && !question3.equals("")) paramCount++;
 		    if(userQuestions.size() < TOTAL_QUESTIONS && paramCount == TOTAL_QUESTIONS) {
 				logger.debug("security Q&A validation failed");
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.135"));
 				//req.getRequestDispatcher(Constants.SETUP_QUESTIONS_URL).forward(req, resp);		//didn't work for jboss 4.0.5
 				req.getRequestDispatcher("./jsp/setupPassword.jsp").forward(req, resp);
 				return;
 			}
 		    userQuestions = new HashMap<String, String>();
 		    Map<String, String> userAnswers = new HashMap<String, String>();	
 		    if(question1 != null && !question1.equals("") && answer1 != null && !answer1.equals("")) userQuestions.put(Constants.Q1, question1); userAnswers.put(Constants.A1, answer1);
 		    if(question2 != null && !question2.equals("") && answer2 != null && !answer2.equals("")) userQuestions.put(Constants.Q2, question2); userAnswers.put(Constants.A2, answer2);
 		    if(question3 != null && !question3.equals("") && answer3 != null && !answer3.equals("")) userQuestions.put(Constants.Q3, question3); userAnswers.put(Constants.A3, answer3);
 			logger.debug("saving request: " + question1 + "=" + answer1 + " " +question2 + "=" + answer2 + " " +question3 + "=" + answer3);
 			if(Messages.getString("PasswordChangeHelper.125").equals(PasswordChangeHelper.validateSecurityQandA(TOTAL_QUESTIONS, username, userQuestions, userAnswers))) {
 				logger.debug("security Q&A validation failed");
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.125"));
 				//req.getRequestDispatcher(Constants.SETUP_QUESTIONS_URL).forward(req, resp);		//didn't work for jboss 4.0.5
 				req.getRequestDispatcher("./jsp/setupPassword.jsp").forward(req, resp);
 				return;
 			}
 			if(!PasswordChangeHelper.validateQuestionsLength(TOTAL_QUESTIONS, userQuestions, userAnswers)) {
 				logger.debug("security Q&A validation failed");
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.150"));
 				//req.getRequestDispatcher(Constants.SETUP_QUESTIONS_URL).forward(req, resp);		//didn't work for jboss 4.0.5
 				req.getRequestDispatcher("./jsp/setupPassword.jsp").forward(req, resp);
 				return;
 			}
 			
 			logger.info("saving request: user provided " +  userQuestions + " " + userAnswers);
 		    saveUserStoredQna(username, userQuestions, userAnswers);
 			
 			//TBD - retrieve all questions related to the users from dao and set them into sessions
 			session.setAttribute(Constants.USERNAME, username);
 			
 			session.invalidate();
 			resp.sendRedirect(Constants.SETUP_SAVED_URL);
 		}
 		catch (Throwable theException) {
 			logger.error(theException);
 		}		
 	}
 
 	protected void doRequestUserQuestions(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 
 		logger.info("doRequestUserQuestions");
 		
 		try {
 			HttpSession session = req.getSession(false);
 			if (session == null) {
 				logger.debug("null session");
 				// this shouldn't happen, make the user start over
 				resp.sendRedirect("./jsp/loggedOut.jsp");
 				return;
 			}
 
 			String username = req.getParameter("userid");
 			
 			logger.debug("username " + username);
 			
 			connect();
 			PasswordChangeDAO userDAO = new PasswordChangeDAO(datasource);
 			try {
 				if(!userDAO.checkValidUser(username)) {
 					session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.101"));
 					resp.sendRedirect(Constants.ASK_USERID_URL);
 					return;
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				disconnect();
 			}
 			
 			// Security enhancement
 			Map<String, String> userQuestions = new HashMap<String, String>();
 			Map<String, String> userAnswers =  new HashMap<String, String>();
 			
 			//pull all questions related to this user
 			loadUserStoredQna(username, userQuestions, userAnswers);
 			
 			//TBD - retrieve all questions related to the users from dao and set them into sessions
 			session.setAttribute(Constants.USERNAME, username);
 			session.removeAttribute(Constants.Q1);
 			session.setAttribute(Constants.Q1, userQuestions.get(Constants.Q1));
 			session.removeAttribute(Constants.Q2);
 			session.setAttribute(Constants.Q2, userQuestions.get(Constants.Q2));
 			session.removeAttribute(Constants.Q3);
 			session.setAttribute(Constants.Q3, userQuestions.get(Constants.Q3));			
 
 			session.removeAttribute(Constants.ALL_QUESTIONS);
 			logger.debug("questions removed from session.");
 			session.setAttribute(Constants.ALL_QUESTIONS, userQuestions);
 			logger.debug("questions saved in session.");
 			session.removeAttribute(Constants.ALL_ANSWERS);
 			logger.debug("answers removed from session.");
 			session.setAttribute(Constants.ALL_ANSWERS, userAnswers);
 			logger.debug("answers saved in session.");
 			
 			if(userQuestions == null || userQuestions.size() == 0) {
 				logger.info("no security question found");
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.140"));
 				resp.sendRedirect(Constants.ASK_USERID_URL);
 			} else {
 				//resp.sendRedirect(Constants.Q1_URL);
 				req.getRequestDispatcher("./jsp/askQuestion1.jsp").forward(req, resp);
 			}
 		}
 		catch (Throwable theException) {
 			logger.error(theException);
 		}		
 	}
 
 //	private void clearCache(HttpServletRequest req) {
 //		HttpSession session = req.getSession(false);
 //
 //		if(session != null) {
 //		}
 //	}
 	
 	protected void doValidateQuestion1(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 		logger.info("doValidateQuestion 1");
 		
 		HttpSession session = req.getSession(false);
 		if (session == null) {
 			logger.debug("null session");
 			// this shouldn't happen, make the user start over
 			resp.sendRedirect("./jsp/loggedOut.jsp");
 			return;
 		}		
 
 		try {
 			if (validateQuestions(req, resp)) {
 				logger.info("answer is correct");
 				resp.sendRedirect("./jsp/askQuestion2.jsp");				
 			} else {
 				logger.info("security question answered wrongly");
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.130"));
 				resp.sendRedirect("./jsp/askQuestion1.jsp");		
 			}
 		}
 		catch (Throwable theException) {
 			logger.error(CommonUtil.toString(theException));
 		}		
 	}
 	
 	protected void doValidateQuestion2(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 		logger.info("doValidateQuestion 2");
 		
 		HttpSession session = req.getSession(false);
 		if (session == null) {
 			logger.debug("null session");
 			// this shouldn't happen, make the user start over
 			resp.sendRedirect("./jsp/loggedOut.jsp");
 			return;
 		}		
 
 		try {
 			if (validateQuestions(req, resp)) {
 				logger.info("answer is correct");
 				resp.sendRedirect("./jsp/askQuestion3.jsp");				
 			} else {
 				logger.info("security question answered wrongly");
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.130"));
 				resp.sendRedirect("./jsp/askQuestion2.jsp");		
 			}
 		}
 		catch (Throwable theException) {
 			logger.error(CommonUtil.toString(theException));
 		}		
 	}
 
 	protected void doValidateQuestion3(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 		logger.info("doValidateQuestion 3");
 		
 		HttpSession session = req.getSession(false);
 		if (session == null) {
 			logger.debug("null session");
 			// this shouldn't happen, make the user start over
 			resp.sendRedirect("./jsp/loggedOut.jsp");
 			return;
 		}		
 
 		try {
 			if (validateQuestions(req, resp)) {
 				logger.info("answer is correct");
 				resp.sendRedirect("./jsp/resetPassword.jsp");				
 			} else {
 				logger.info("security question answered wrongly");
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.130"));
 				resp.sendRedirect("./jsp/askQuestion3.jsp");		
 			}
 		}
 		catch (Throwable theException) {
 			logger.error(CommonUtil.toString(theException));
 		}		
 	}
 
 	protected boolean validateQuestions(HttpServletRequest req, HttpServletResponse resp)
 	throws Exception {
 
 		HttpSession session = req.getSession(false);
 //		Map<?, ?> userQuestions = (HashMap<?, ?>) session.getAttribute(Constants.ALL_QUESTIONS);
 //		Map<?, ?> userAnswers = (HashMap<?, ?>) session.getAttribute(Constants.ALL_ANSWERS);
 		//begin CADSRPASSW-43
 		Map<String, String> userQuestions = new HashMap<String, String>();
 		Map<String, String> userAnswers =  new HashMap<String, String>();
 		String username = (String)session.getAttribute(Constants.USERNAME);
 		//pull all questions related to this user
 		loadUserStoredQna(username, userQuestions, userAnswers);
 		//end CADSRPASSW-43
 		logger.info("questions " + userQuestions != null?userQuestions.size():0 + " answers " + userAnswers.size());
 
 		String question1 = req.getParameter("question");
 		String answer1 = req.getParameter("answer");
 		String answerIndex =  req.getParameter("answerIndex");
 		logger.debug("doValidateQuestions: (" + question1 + ")=" + answer1);
 		
 		boolean validated = false;
 		//get user's stored answer related to the question selected
 		String expectedAnswer = (String)userAnswers.get(answerIndex);	//md5 approach
 //		String expectedAnswer = CommonUtil.decode((String)userAnswers.get(answerIndex));	//encryption approach
 //		if(correctAnswer != null && correctAnswer.equals(answer1)) {	//plain text
 //			validated = true;
 //		}
 //		String providedAnswer = CommonUtil.pad(answer1, DAO.MAX_ANSWER_LENGTH);		//encryption approach
 		String providedAnswer = CommonUtil.encode(answer1);
 		
 		validated = expectedAnswer.equals(providedAnswer);
 		
 		return validated;
 	}
 
 	protected void doChangePassword2(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 
 		logger.info("doChangePassword");
 		
 		try {
 			HttpSession session = req.getSession(false);
 			if (session == null) {
 				logger.debug("null session");
 				// this shouldn't happen, make the user start over
 				resp.sendRedirect("./jsp/loggedOut.jsp");
 				return;
 			}
 
 			String username = req.getParameter("userid");
 			String newPassword = req.getParameter("newpswd1");
 
 			// Security enhancement
 			String question1 = (String)req.getParameter("question1");
 			String answer1 = (String)req.getParameter("answer1");
 			String question2 = (String)req.getParameter("question2");
 			String answer2 = (String)req.getParameter("answer2");
 			String question3 = (String)req.getParameter("question3");
 			String answer3 = (String)req.getParameter("answer3");
 			logger.debug("changing request: " + question1 + "=" + answer1 + " " +question2 + "=" + answer2 + " " +question3 + "=" + answer3);
 		
 			logger.debug("username " + username);
 			//check locked state here
 
 			connect();
 			PasswordChangeDAO changeDAO = new PasswordChangeDAO(datasource);
 			Result passwordChangeResult = changeDAO.resetPassword(username, newPassword);
 			disconnect();
 
 			if (passwordChangeResult.getResultCode() == ResultCode.PASSWORD_CHANGED) {
 				logger.info("password changed");
 				session.invalidate();  // they are done, log them out
 				resp.sendRedirect("./jsp/passwordChanged.jsp");				
 			} else {
 				logger.info("password change failed");
 				String errorMessage = passwordChangeResult.getMessage();
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, errorMessage);
 				resp.sendRedirect("./jsp/resetPassword.jsp");		
 			}
 		}
 		catch (Throwable theException) {
 			logger.error(CommonUtil.toString(theException));
 		}		
 	}
 
 	protected void doChangePassword(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 
 		logger.info("doChangePassword");
 		
 		try {
 			HttpSession session = req.getSession(false);
 			if (session == null) {
 				logger.debug("null session");
 				// this shouldn't happen, make the user start over
 				resp.sendRedirect("./jsp/loggedOut.jsp");
 				return;
 			}
 
 			session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, "");
 			
 			String username = req.getParameter("userid");
 			String oldPassword = req.getParameter("pswd");
 			String newPassword = req.getParameter("newpswd1");
 			String newPassword2 = req.getParameter("newpswd2");
 			
 			//begin CADSRPASSW-16
 //			Map<String, String> userQuestions = new HashMap<String, String>();
 //			Map<String, String> userAnswers =  new HashMap<String, String>();
 //			loadUserStoredQna(username, userQuestions, userAnswers);
 //			if(userQuestions.size() == 0) {
 //				logger.info("no security question found");
 //				String msg = Messages.getString("PasswordChangeHelper.136");
 //				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, msg);
 //				resp.sendRedirect("./jsp/changePassword.jsp");
 //				return;
 //			}
 			//end CADSRPASSW-16
 
 			if(Messages.getString("PasswordChangeHelper.3").equals(PasswordChangeHelper.validateChangePassword(username, oldPassword, newPassword, newPassword2, username, req.getParameter("newpswd2")))) {
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.3"));
 				resp.sendRedirect("./jsp/changePassword.jsp");
 				return;
 			}
 
 			if(Messages.getString("PasswordChangeHelper.4").equals(PasswordChangeHelper.validateChangePassword(username, oldPassword, newPassword, newPassword2, username, req.getParameter("newpswd2")))) {
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.4"));
 				resp.sendRedirect("./jsp/changePassword.jsp");
 				return;
 			}
 			
 			if(Messages.getString("PasswordChangeHelper.5").equals(PasswordChangeHelper.validateChangePassword(username, oldPassword, newPassword, newPassword2, username, req.getParameter("newpswd2")))) {
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.5"));
 				resp.sendRedirect("./jsp/changePassword.jsp");
 				return;
 			}
 
 //			if(Messages.getString("PasswordChangeHelper.6").equals(PasswordChangeHelper.validateChangePassword(username, oldPassword, newPassword, newPassword2, username, req.getParameter("newpswd2")))) {
 //				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.6"));
 //				resp.sendRedirect("./jsp/changePassword.jsp");
 //				return;
 //			}					
 
 			if(Messages.getString("PasswordChangeHelper.7").equals(PasswordChangeHelper.validateChangePassword(username, oldPassword, newPassword, newPassword2, username, req.getParameter("newpswd2")))) {
 				logger.debug("entered username doesn't match session " + username + " " + req.getParameter("userid"));
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.7"));
 				resp.sendRedirect("./jsp/changePassword.jsp");				
 				return;
 			}
 			if(Messages.getString("PasswordChangeHelper.8").equals(PasswordChangeHelper.validateChangePassword(username, oldPassword, newPassword, newPassword2, username, req.getParameter("newpswd2")))) {
 				
 				logger.debug("new password mis-typed");
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.8"));
 				resp.sendRedirect("./jsp/changePassword.jsp");
 				return;
 			}
 
 			logger.debug("username " + username);
 			UserBean userBean = null;			
 			connect();
 			PasswordChangeDAO loginDAO = new PasswordChangeDAO(datasource);
 			userBean = loginDAO.checkValidUser(username, oldPassword);
 			disconnect();
 			session.setAttribute(UserBean.USERBEAN_SESSION_ATTRIBUTE, userBean);		
 			logger.debug ("validUser " + userBean.isLoggedIn());
 			logger.debug ("resultCode " + userBean.getResult().getResultCode().toString());
 			if (!userBean.isLoggedIn()) {
 				String errorMessage1 = userBean.getResult().getMessage();
 				logger.debug ("errorMessage " + errorMessage1);
 				if(userBean.getResult().getResultCode() != ResultCode.LOCKED_OUT) {
 					session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.102"));
 				} else {
 					session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, Messages.getString("PasswordChangeHelper.103"));
 				}
 				resp.sendRedirect("./jsp/changePassword.jsp");
 				return;
 			}
 			
 			connect();
 			PasswordChangeDAO changeDAO = new PasswordChangeDAO(datasource);
 			Result passwordChangeResult = changeDAO.changePassword(username, oldPassword, newPassword);
 			disconnect();
 
 			if (passwordChangeResult.getResultCode() == ResultCode.PASSWORD_CHANGED) {
 				logger.info("password changed");
 				session.invalidate();  // they are done, log them out
 				resp.sendRedirect("./jsp/passwordChanged.jsp");				
 			} else {
 				logger.info("password change failed");
 				String errorMessage = passwordChangeResult.getMessage();
 				session.setAttribute(ERROR_MESSAGE_SESSION_ATTRIBUTE, errorMessage);
 				resp.sendRedirect("./jsp/changePassword.jsp");		
 			}
 		}
 		catch (Throwable theException) {		
 			logger.error(theException);
 		}		
 	}
 
 	private boolean loadUserStoredQna(String username, Map<String, String> userQuestions, Map<String, String> userAnswers) {
 		UserSecurityQuestion qna = new UserSecurityQuestion();
 		boolean retVal = false;
 		try {
 			connect();
 			PasswordChangeDAO dao = new PasswordChangeDAO(datasource);
 			qna = dao.findByUaName(username);
 			if(qna != null) {
 				userQuestions.put(Constants.Q1, qna.getQuestion1());
 				userQuestions.put(Constants.Q2, qna.getQuestion2());
 				userQuestions.put(Constants.Q3, qna.getQuestion3());
 				userAnswers.put(Constants.A1, qna.getAnswer1());
 				userAnswers.put(Constants.A2, qna.getAnswer2());
 				userAnswers.put(Constants.A3, qna.getAnswer3());
 				retVal = true;
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		} finally {
 			disconnect();
 		}
 		return retVal;
 		
 	}
 
 	public static void initProperties() {
 		if(HELP_LINK == null) {
 			connect();
 			PasswordChangeDAO dao = new PasswordChangeDAO(datasource);
 			
 			HELP_LINK = dao.getToolProperty(Constants.TOOL_NAME, Constants.HELP_LINK_PROPERTY);
 			LOGO_LINK = dao.getToolProperty(Constants.TOOL_NAME, Constants.LOGO_LINK_PROPERTY);
 			
 			PropertyHelper.setHELP_LINK(HELP_LINK);
 			PropertyHelper.setLOGO_LINK(LOGO_LINK);
 			
 			PropertyHelper.setEMAIL_ID(dao.getToolProperty("SENTINEL", "EMAIL.HOST.USER"));
 			PropertyHelper.setEMAIL_PWD(dao.getToolProperty("SENTINEL", "EMAIL.HOST.PSWD"));
 			
 //			disconnect();
 		}
 	}
 	
 	@Override
 	public void init() throws ServletException {
 		super.init();
 		logger.debug("init");
 	}
 
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		logger.debug("init(ServletConfig config)");
 	}
 		
 }
