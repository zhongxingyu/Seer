 package swarm.server.thirdparty.servlet;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Writer;
 import java.net.URLDecoder;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.JSONException;
 
 
 
 import swarm.server.app.A_ServerApp;
 import swarm.server.app.ServerContext;
 import swarm.server.transaction.ServerTransactionManager;
 import swarm.shared.app.BaseAppContext;
 import swarm.shared.app.S_CommonApp;
 import swarm.shared.json.A_JsonFactory;
 import swarm.shared.json.I_JsonObject;
 import swarm.shared.transaction.S_Transaction;
 import swarm.shared.transaction.TransactionRequest;
 import swarm.shared.transaction.TransactionResponse;
 
 
 public class TransactionServlet extends A_BaseServlet
 {
 	private static final Logger s_logger = Logger.getLogger(TransactionServlet.class.getName());
 	
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
 		//smU_Servlet.simulateLag(10000);
 		//smU_Servlet.simulateException(true);
 		
 		ServerContext context = A_ServerApp.getInstance().getContext();
 		
 		I_JsonObject requestJson = U_Servlet.getRequestJson(nativeRequest, isGet);
 		I_JsonObject responseJson = context.jsonFactory.createJsonObject();
 		
		TransactionResponse response = context.txnMngr.handleRequestFromClient(nativeRequest, nativeResponse, this.getServletContext(), requestJson, responseJson);
 		
 		if( isGet && !response.hasError() )
 		{
 			long expiration_seconds = A_ServerApp.getInstance().getConfig().requestCacheExpiration_seconds;
 			
 			if( expiration_seconds > 0 )
 			{
 				long expiration_millis = expiration_seconds*1000;
 				long now_millis = System.currentTimeMillis();
 				nativeResponse.setHeader("Cache-Control", "public, max-age="+expiration_seconds);
 				nativeResponse.setDateHeader("Expires", now_millis + expiration_millis);
 			}
 		}
 		
 		U_Servlet.writeJsonResponse(responseJson, nativeResponse.getWriter());
 		
 		nativeResponse.getWriter().flush();
 	}
 }
