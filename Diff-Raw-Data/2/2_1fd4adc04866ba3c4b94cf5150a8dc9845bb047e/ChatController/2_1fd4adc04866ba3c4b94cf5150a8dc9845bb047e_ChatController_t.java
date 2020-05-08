 package com.ctb.pilot.chat.service;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.core.io.Resource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.ctb.pilot.chat.model.Message;
 import com.ctb.pilot.common.util.JsonUtils;
 import com.ctb.pilot.user.model.User;
 import com.ctb.pilot.user.service.UserService;
 
 import eliza.Eliza;
 
 @Controller
 public class ChatController {
 
 	private static final int PAGE_SIZE = 20;
 
 	@Autowired
 	private ChatService chatService;
 
 	@Autowired
 	private UserService userService;
 
 	@Value("classpath:service/chat/ai/eliza/script")
 	private Resource elizaScript;
 
 	private Map<Integer, Eliza> elizaMap = new HashMap<Integer, Eliza>();
 	private User elizaUser;
 	private String elizaPrefix;
 
 	@RequestMapping("/services/chat/messages.do")
 	public void getRecentMessages(HttpServletRequest req,
 			HttpServletResponse resp) throws IOException {
		List<Message> messages = chatService.getMessages(PAGE_SIZE, 1);
 		resp.setCharacterEncoding("utf8");
 		PrintWriter out = resp.getWriter();
 		String json = JsonUtils.toJson(messages);
 		System.out.println(json);
 		out.print(json);
 		out.flush();
 		out.close();
 	}
 
 	@RequestMapping("/services/chat/history.do")
 	public String getHistoryMessages(HttpServletRequest req, Model model) {
 		String pageNoAsString = req.getParameter("page_no");
 		int pageNo;
 		if (pageNoAsString == null) {
 			pageNo = 1;
 		} else {
 			pageNo = Integer.parseInt(pageNoAsString);
 		}
 
 		long total = chatService.getAllMessageCount();
 		long pageCount = total / PAGE_SIZE + (total % PAGE_SIZE != 0 ? 1 : 0);
 
 		List<Message> messages = chatService.getMessages(PAGE_SIZE, pageNo);
 		model.addAttribute("messages", messages);
 		model.addAttribute("pageNo", pageNo);
 		model.addAttribute("pageCount", pageCount);
 
 		return "services/chat/chat_history_view";
 	}
 
 	@RequestMapping("/services/chat/send-message.do")
 	public void sendMessage(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		// Move Eliza.
 		if (elizaUser == null) {
 			elizaUser = userService.login("eliza@eliza.com", "1234");
 			elizaPrefix = "@" + elizaUser.getNickname() + " ";
 		}
 
 		HttpSession session = req.getSession();
 		User user = (User) session.getAttribute("user");
 		int userSequence = user.getSequence();
 
 		req.setCharacterEncoding("utf8");
 		String message = req.getParameter("message");
 		System.out.println("message: " + message);
 		if (message == null || message.isEmpty()) {
 			throw new ServletException("Message is null or empty.");
 		}
 
 		chatService.insertMessage(userSequence, message);
 
 		// Eliza works.
 		if (message.startsWith(elizaPrefix)) {
 			Eliza eliza = elizaMap.get(userSequence);
 			if (eliza == null) {
 				eliza = new Eliza();
 
 				// FIXME: Close InputStream.
 				InputStream is = elizaScript.getInputStream();
 				eliza.readScript(new InputStreamReader(is));
 
 				elizaMap.put(userSequence, eliza);
 			}
 
 			message = message.substring(elizaPrefix.length());
 			String reply = "@" + user.getNickname() + " "
 					+ eliza.processInput(message);
 			chatService.insertMessage(elizaUser.getSequence(), reply);
 		}
 	}
 
 }
