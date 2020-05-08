 /**
  * 
  */
 package net.socio.ui.executors.basic;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import net.mysocio.data.IConnectionData;
 import net.mysocio.data.IMessage;
 import net.mysocio.data.UnreaddenMessages;
 import net.mysocio.ui.management.ICommandExecutor;
 
 /**
  * @author Aladdin
  *
  */
 public class GetMessagesExecutor implements ICommandExecutor {
 	/* (non-Javadoc)
 	 * @see net.mysocio.ui.management.ICommandExecutor#execute(javax.servlet.http.HttpServletRequest)
 	 */
 	@Override
 	public String execute(IConnectionData connectionManager) {
 		StringBuffer output = new StringBuffer();
 		List<? extends IMessage> messages = getMessages(connectionManager);
 		for (IMessage message : messages) {
 			output.append(wrapXmlMessage(message.getId(), message.getTitle(), message.getLink(), message.getText()));
 		}
 		return output.toString();
 	}
 	
 	private static List<? extends IMessage> getMessages(IConnectionData connectionData) {
 		String id = connectionData.getRequestParameter("sourceId");
 		if (id == null){
 			return Collections.emptyList();
 		}
 		if (id.equalsIgnoreCase("ALL")){
 			Collection<UnreaddenMessages> allMessages = connectionData.getUser().getUnreadMessages().values();
 			ArrayList<IMessage> messages = new ArrayList<IMessage>();
 			for (UnreaddenMessages unreaddenMessages : allMessages) {
 				messages.addAll(unreaddenMessages.getMessages());
 			}
 			return messages;
 		}
 		UnreaddenMessages unreadMessages = connectionData.getUser().getUnreadMessages(Long.parseLong(id));
 		if (unreadMessages == null){
 			return Collections.emptyList();
 		}
 		return unreadMessages.getMessages();
 	}
 	
 	/**
 	 * @param output
 	 * @param text
 	 */
 	private static String wrapXmlMessage(Long id, String title, String link, String text) {
 		StringBuffer output = new StringBuffer();
 		output.append("<div class='Message' id=\"").append(id).append("\">");
 		String actualTitle = "No Title";
 		if (title != null && title.length() >= 0){
 			actualTitle = title;
 		}
 		output.append("<div class='MessageTitle'>");
 		output.append("<a href=\""+ link + "\" target=\"_blank\">").append(actualTitle).append("</a>");
 		output.append("</div>");
 		output.append(text);
 		output.append("</div>");
 		return output.toString();
 	}
 }
