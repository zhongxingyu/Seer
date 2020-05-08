 package org.ocactus.sms.server;
 
 import java.io.IOException;
 import java.sql.Connection;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.ocactus.sms.common.Utils;
 import org.ocactus.sms.server.c2dm.C2DMessaging;
 import org.ocactus.sms.server.c2dm.IC2DMessaging;
 import org.ocactus.sms.server.db.Database;
 
 public class C2DMServlet extends ServletBase {
 
 	public void login(HttpServletRequest req, HttpServletResponse resp)
 		throws ServletException, IOException {
 		
 		Connection db = getDbConnection();
 		IC2DMessaging c2dm = new C2DMessaging(new Database(db));
 		
 		try {
 			String email = req.getParameter("email");
 			String password = req.getParameter("password");
 			
 			if(Utils.isNullOrEmpty(email) || Utils.isNullOrEmpty(password)) {
 				resp.setStatus(400);
 				resp.getWriter().write("missing email or password");
 			} else {
 				c2dm.login(email, password);
 			}
 			
 			db.close();
 		} catch(Exception ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 	
 	public void register(HttpServletRequest req, HttpServletResponse resp)
 		throws ServletException, IOException {
 		
 		Connection db = getDbConnection();
 		IC2DMessaging c2dm = new C2DMessaging(new Database(db));
 		
 		try {
 			String registrationId = req.getParameter("id");
 			
 			if(registrationId != null) {
 				c2dm.registerDevice(registrationId);
 				resp.sendRedirect(req.getContextPath());			
 			} else {
 				resp.setStatus(400);
 				resp.getWriter().write("id missing");
 			}
 			
 			db.close();
 		} catch(Exception ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 	
 	public void send(HttpServletRequest req, HttpServletResponse resp)
 		throws ServletException, IOException {
 		
 		Connection db = getDbConnection();
 		IC2DMessaging c2dm = new C2DMessaging(new Database(db));
 		
 		try {
 			if(!c2dm.isRegisteredAndLoggedIn()) {
 				resp.setStatus(401);
 				resp.getWriter().write("not logged in or registered");
 			} else {
 				c2dm.send();
				resp.sendRedirect(req.getContextPath() + "/");
 			}
 			
 			db.close();
 			
 		} catch(Exception ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 }
