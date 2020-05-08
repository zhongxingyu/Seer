 package edu.sjsu.videolibrary.jspHelper;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import edu.sjsu.videolibrary.util.Parameters;
 
 public class SignInUserHelper {
 	public static final String getLoginError(HttpServletRequest req) {
 		HttpSession session = req.getSession();
 		String errorString = (String) session.getAttribute(Parameters.pLoginError);
 		session.removeAttribute(Parameters.pLoginError);
 		if(errorString == null){
 			return "";
 		}
 		return errorString;
 	}
 	
 	public static final String getPaymentError(HttpServletRequest req) {
 		HttpSession session = req.getSession();
 		String errorString = (String) session.getAttribute(Parameters.pMakePaymentMessage);
 		session.removeAttribute(Parameters.pMakePaymentMessage);
 		if(errorString == null){
 			return "";
 		}
 		return errorString;
 	}
 }
