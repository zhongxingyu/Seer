 package com.scholastic.sbam.server.servlets;
 
 import java.util.List;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.scholastic.sbam.client.services.WelcomeMessageService;
 import com.scholastic.sbam.server.database.codegen.WelcomeMessage;
 import com.scholastic.sbam.server.database.objects.DbWelcomeMessage;
 import com.scholastic.sbam.server.database.util.HibernateUtil;
 import com.scholastic.sbam.shared.util.WebUtilities;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings("serial")
 public class WelcomeMessageServiceImpl extends RemoteServiceServlet implements WelcomeMessageService {
 
 	@Override
 	public String getWelcomeMessages() throws IllegalArgumentException {
 		
 		HibernateUtil.openSession();
 		HibernateUtil.startTransaction();
 
 		StringBuffer sb = new StringBuffer();
 		try {
 			List<WelcomeMessage> msgs = DbWelcomeMessage.findToShow();
 
 			for (WelcomeMessage msg : msgs) {
 				sb.append("<div class=\"sbamMsg\">");
 				sb.append("<div class=\"sbamMsgHeader\">");
 				sb.append("<div class=\"sbamMsgPostDate\">");
 				sb.append(msg.getPostDate());
 				sb.append("</div>");
 				if (msg.getTitle() != null && msg.getTitle().length() > 0) {
 					sb.append("<div class=\"sbamMsgTitle\">");
 					sb.append(msg.getTitle());
 					sb.append("</div>");
 				}
 				sb.append("</div>");
 				sb.append("<div class=\"sbamMsgContent\">");
 				sb.append(msg.getContent());
 				sb.append("</div>");
 				sb.append("</div>");
 				sb.append("<hr/>");
 			}
 			
 			if (msgs.size() == 0) {
 				sb.append("<hr/>");
 				sb.append("<i>No messages today.</i>");
 			}
 		} catch (Exception exc) {
 			exc.printStackTrace();
			return "Internal error getting Welcome Messages:<br/><br/>" + WebUtilities.getAsHtml(exc.toString());
 		}
 		
 		HibernateUtil.endTransaction();
 		HibernateUtil.closeSession();
 		
 		return sb.toString();
 	}
 }
