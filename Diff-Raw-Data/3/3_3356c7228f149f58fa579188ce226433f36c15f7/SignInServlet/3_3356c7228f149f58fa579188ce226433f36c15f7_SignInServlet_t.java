 package swarm.server.thirdparty.servlet;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.net.URLDecoder;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.JSONException;
 
 import swarm.server.account.E_Role;
 import swarm.server.account.ServerAccountManager;
 import swarm.server.account.UserSession;
 
 import swarm.server.app.A_ServerApp;
 import swarm.server.app.A_ServerJsonFactory;
 import swarm.server.app.ServerContext;
 import swarm.server.session.SessionManager;
 import swarm.server.transaction.ServerTransactionManager;
 import swarm.shared.account.SignInCredentials;
 import swarm.shared.account.SignInValidationResult;
 import swarm.shared.account.SignInValidator;
 import swarm.shared.app.BaseAppContext;
 import swarm.shared.app.S_CommonApp;
 import swarm.shared.json.A_JsonFactory;
 import swarm.shared.json.I_JsonObject;
 import swarm.shared.transaction.S_Transaction;
 import swarm.shared.transaction.TransactionRequest;
 import swarm.shared.transaction.TransactionResponse;
 
 
 public class SignInServlet extends A_BaseServlet
 {
 	private static final Logger s_logger = Logger.getLogger(SignInServlet.class.getName());
 	
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
 	{
 		doGetOrPost(req, resp, true);
 	}
 	
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
 	{
 		doGetOrPost(req, resp, false);
 	}
 	
 	private void doGetOrPost(HttpServletRequest nativeRequest, HttpServletResponse nativeResponse, boolean isGet) throws ServletException, IOException
 	{
 		ServerContext context = A_ServerApp.getInstance().getContext();
 		
 		SessionManager sessionMngr = context.sessionMngr;
		sessionMngr.onEnterScope();
 		ServerAccountManager accountMngr = context.accountMngr;
 		
 		((A_ServerJsonFactory)context.jsonFactory).startScope(true);
 		
 		try
 		{
 			nativeResponse.setContentType("text/html");
 			
 			I_JsonObject requestJson = null;
 			String email = "";
 			String password = "";
 			
 			boolean isSignedIn = sessionMngr.isAuthenticated(nativeRequest, nativeResponse);
 			
 			if( !isGet )
 			{
 				if( nativeRequest.getParameter("signin") != null )
 				{
 					email = nativeRequest.getParameter("email");
 					password = nativeRequest.getParameter("password");
 					
 					if( !isSignedIn )
 					{
 						SignInCredentials creds = new SignInCredentials(false, email, password);
 						SignInValidationResult result = new SignInValidationResult();
 
 						UserSession session = accountMngr.attemptSignIn(creds, result);
 						
 						if( session != null )
 						{
 							sessionMngr.startSession(session, nativeResponse, creds.rememberMe());
 							
 							isSignedIn = true;
 						}
 						
 					}
 				}
 				else if ( nativeRequest.getParameter("signout") != null )
 				{
 					if( isSignedIn )
 					{
 						sessionMngr.endSession(nativeRequest, nativeResponse);
 						isSignedIn = false;
 					}
 				}
 			}
 			
 			PrintWriter writer = nativeResponse.getWriter();
 			
 			writer.write("<form method='POST'>");
 			if( isSignedIn )
 			{
 				writer.write("<input type='submit' name='signout' value='Sign Out'>");
 			}
 			else
 			{
 				writer.write("<input placeholder='E-mail' type='text' name='email' value='"+email+"' ></input>");
 				writer.write("<input placeholder='Password' type='password' name='password'></input>");
 				writer.write("<input type='submit' name='signin' value='Sign In'>");
 			}
 				
 			writer.write("</form>");
 			
 			writer.flush();
 		}
 		finally
 		{
 			((A_ServerJsonFactory)context.jsonFactory).endScope();
			sessionMngr.onExitScope();
 		}
 	}
 }
