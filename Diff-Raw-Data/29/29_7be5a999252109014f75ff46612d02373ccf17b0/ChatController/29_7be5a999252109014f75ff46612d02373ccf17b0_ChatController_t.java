 package epam.ph.sg.controllers;
 
 /**
  * @author Talash Pavlo
  */
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import epam.ph.sg.models.User;
 import epam.ph.sg.tab.chat.Chat;
 import epam.ph.sg.tab.chat.ChatUser;
 import epam.ph.sg.tab.chat.ChatWebSocketSpeacker;
 import epam.ph.sg.tab.chat.Message;
 
 @Controller
 public class ChatController {
 	private static Logger log = Logger.getLogger(ChatController.class);
 
 	@RequestMapping(value = "/Chat.html")
 	public String chat(HttpServletRequest request, Model model) {
 		User user = (User) request.getSession().getAttribute("user");
 		if (user != null) {
 			log.info(request.getRequestURI() + " request received. User id="
 					+ user.getId());
 		} else {
 			log.info(request.getRequestURI() + " request received.");
 		}
		request.getSession().setAttribute("chatUser", new ChatUser());
		return "Tab/Chat";
 	}
 
 	@RequestMapping(value = "/Send.html", method = RequestMethod.POST)
 	public @ResponseBody
 	boolean send(@RequestParam("msg") String msgText, HttpServletRequest request) {
 		User user = (User) request.getSession().getAttribute("user");
 		if (user != null) {
 			log.info(request.getRequestURI() + " request received. User id="
 					+ user.getId() + ". Message: " + msgText);
 			Message msg = new Message(user, msgText);
 			Chat.addMessage(msg);
 			ChatWebSocketSpeacker.informAll();
 			return true;
 		} else {
 			log.info(request.getRequestURI() + " request received.");
 			return false;
 		}
 	}
 
 	@RequestMapping(value = "/Refresh.html", method = RequestMethod.POST)
 	public @ResponseBody
 	Message[] refresh(HttpServletRequest request) {
 		User user = (User) request.getSession().getAttribute("user");
 		if (user != null) {
 			log.info(request.getRequestURI() + " request received. User id="
 					+ user.getId());
 		} else {
 			log.info(request.getRequestURI() + " request received.");
 		}
		ChatUser cu = (ChatUser) request.getSession().getAttribute("chatUser");
		return cu.refresh();
 	}
 }
