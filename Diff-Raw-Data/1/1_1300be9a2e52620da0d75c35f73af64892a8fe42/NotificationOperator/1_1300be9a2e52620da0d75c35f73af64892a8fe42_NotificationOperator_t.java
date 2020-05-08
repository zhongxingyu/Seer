 package ajaxLearner;
 
 import dao.DAOFactory;
 import dao.NotificationDao;
 import dao.NotificationMessageDao;
 import dao.UserDAO;
 import entity.NotifMessage;
 import entity.Notification;
 import entity.User;
 import infoResource.AttributeNames;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import servlets.BaseServlet;
 
 /**
  * A servelt the 
 * 
  */
 @WebServlet(description = "Notification messages Fetcher for learner", urlPatterns = { "/learnerNotif" })
 public class NotificationOperator extends BaseServlet{
 
 	public String getNotifMsgs(User givenUser){/* MsgID */
 		DAOFactory daoFactory=DAOFactory.getInstance(DAOFactory.MYSQL);
 		NotificationMessageDao nDao=daoFactory.createNotificationMessageDao();
 		List<NotifMessage> Messages=nDao.getUserMessages(givenUser.getAccountID());
 		String json="[";
 		
 		for(int ctr=0;ctr<Messages.size();ctr++){
 			
 			json=json.concat("{");
 			json=json.concat("\"Type\":\""+Messages.get(ctr).getnType()+"\",");
 			json=json.concat("\"Message\":\""+Messages.get(ctr).getMessage()+"\"");
 			if(ctr<Messages.size()-1)
 				json=json.concat("},");
 			else json=json.concat("}");
 		}
 		
 		return json.concat("]");
 	}
 	
 	
 	public String getNotifMsgs(int NotificationId){/* MsgID */
 		DAOFactory daoFactory=DAOFactory.getInstance(DAOFactory.MYSQL);
 		NotificationMessageDao nDao=daoFactory.createNotificationMessageDao();
 		List<NotifMessage> Messages=nDao.getMessagesOfNotice(NotificationId);
 		String json="[";
 		
 		for(int ctr=0;ctr<Messages.size();ctr++){
 			
 			json=json.concat("{");
 			json=json.concat("\"Type\":\""+Messages.get(ctr).getnType()+"\",");
 			json=json.concat("\"Message\":\""+Messages.get(ctr).getMessage()+"\"");
 			if(ctr<Messages.size()-1)
 				json=json.concat("},");
 			else json=json.concat("}");
 		}
 		
 		return json.concat("]");
 	}
 	
 	
 	public String getUserNotifMsgs(User givenUser){/* MsgID */
 		DAOFactory daoFactory=DAOFactory.getInstance(DAOFactory.MYSQL);
 		NotificationDao noticeDao=daoFactory.createNotificationDao();
 		List<Notification> notices=noticeDao.getUserNotifications(givenUser.getAccountID());
 		String json="[";
 		
 		for(int ctr=0;ctr<notices.size();ctr++){
 			
 			json=json.concat("{\"Time\":\""+notices.get(ctr).getStartedOn()+"\",");
 			json=json.concat("\"Notifications\":"+getNotifMsgs(notices.get(ctr).getNotificationId()));
 			//json=json.concat("{");
 			if(ctr<notices.size()-1)
 				json=json.concat("},");
 			else json=json.concat("}");
 		}
 		
 		return json.concat("]");
 	}
 		
 	
 	
 	
 	@Override
 	public void executeCustomCode(HttpServletRequest request,
 			HttpServletResponse response) {
 		String uName=request.getParameter(AttributeNames.user.toString());
 		DAOFactory factory=DAOFactory.getInstance(DAOFactory.MYSQL);
 		UserDAO uDao=factory.createUserDAO();
 		PrintWriter out;
 		response.setContentType("application/json");
 		try{
 			out=response.getWriter();
 			out.write(getUserNotifMsgs(uDao.findUserWithName(uName)));
 			out.flush();
 		}catch(IOException ioEX){}
 	}
 }
