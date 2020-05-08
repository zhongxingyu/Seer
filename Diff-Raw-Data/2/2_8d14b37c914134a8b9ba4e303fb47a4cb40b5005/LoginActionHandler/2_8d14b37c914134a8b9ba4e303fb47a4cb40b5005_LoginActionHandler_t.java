 package ic.doc.cpp.server.handler.student;
 
 import ic.doc.cpp.server.dao.CompanyUserDao;
 import ic.doc.cpp.server.dao.StudentUserDao;
 import ic.doc.cpp.server.domain.CompanyUser;
 import ic.doc.cpp.server.domain.StudentUser;
 import ic.doc.cpp.server.util.Security;
 import ic.doc.cpp.shared.action.student.Login;
 import ic.doc.cpp.shared.action.student.LoginResult;
 import ic.doc.cpp.shared.exception.LoginException;
 
 import com.gwtplatform.dispatch.server.actionhandler.ActionHandler;
 
 import com.google.inject.Inject;
 import com.gwtplatform.dispatch.server.ExecutionContext;
 import com.gwtplatform.dispatch.shared.ActionException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import com.google.inject.Provider;
 
 public class LoginActionHandler implements ActionHandler<Login, LoginResult> {
 	
 	private final Provider<HttpServletRequest> requestProvider;
 	
 	@Inject
 	public LoginActionHandler(final Provider<HttpServletRequest> requestProvider) {
 	    this.requestProvider = requestProvider;
 	  }
 
 	@Override
 	public LoginResult execute(Login action, ExecutionContext context)
 			throws ActionException {
 		LoginResult result = null;
 		
 		if (action.getType().equals("student")) {
 			StudentUserDao userDao = new StudentUserDao();
 			try {
 				StudentUser user = userDao.retrieveUser(action.getLogin());
 				
 				if (user != null && isValidLogin(action, user.getPassword(), user.getSalt())) {
 					HttpSession session = requestProvider.get().getSession();
 					session.setAttribute("login.authenticated", action.getLogin());
 					result = new LoginResult(session.getId());
 				} else {
 					throw new LoginException("Invalid user name or password.");
 				}
 			} catch (Exception e) {
 				throw new ActionException(e);
 			}
 		} else if (action.getType().equals("company")){
 			CompanyUserDao userDao = new CompanyUserDao();
 			try {
 				CompanyUser user = userDao.retrieveUser(action.getLogin());
 				
 				if (user != null && isValidLogin(action, user.getPassword(), user.getSalt())) {
 					HttpSession session = requestProvider.get().getSession();
 					session.setAttribute("login.authenticated", action.getLogin());
 					result = new LoginResult(session.getId());
				} else {
					throw new LoginException("Invalid user name or password.");
 				}
 			} catch (Exception e) {
 				throw new ActionException(e);
 			}
 		} else {
 			throw new ActionException("Invalid type of user.");
 		}
 		
 		return result;
 	}
 	
 	private boolean isValidLogin(Login action, String password, String salt) {
 		String hash = Security.sha256(salt + action.getPassword());
 		return hash.equals(password);
 	}
 
 	@Override
 	public void undo(Login action, LoginResult result, ExecutionContext context)
 			throws ActionException {
 	}
 
 	@Override
 	public Class<Login> getActionType() {
 		return Login.class;
 	}
 }
