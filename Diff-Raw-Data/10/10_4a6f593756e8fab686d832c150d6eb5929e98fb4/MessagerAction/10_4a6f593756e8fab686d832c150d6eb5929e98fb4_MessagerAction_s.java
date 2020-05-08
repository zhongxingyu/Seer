 package com.ISharing.Action;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts2.ServletActionContext;
 
 import com.ISharing.IService.MessagerService;
 import com.ISharing.IService.UserService;
 import com.ISharing.ORM.Contacter;
 import com.ISharing.ORM.Message;
 import com.ISharing.ORM.User;
 import com.ISharing.Tool.ConstFigure;
 import com.opensymphony.xwork2.ActionSupport;
 
 @SuppressWarnings("serial")
 public class MessagerAction  extends ActionSupport
 {
 	private MessagerService messagerService;
 	private UserService userService;
 
 	public MessagerService getMessagerService() {
 		return messagerService;
 	}
 
 	public void setMessagerService(MessagerService messagerService) {
 		this.messagerService = messagerService;
 	}
 
 	public UserService getUserService() {
 		return userService;
 	}
 
 	public void setUserService(UserService userService) {
 		this.userService = userService;
 	}
 
 	//д
 	public void writeMessage() throws IOException
 	{
 		HttpServletRequest request = ServletActionContext.getRequest();
 		HttpServletResponse response = ServletActionContext.getResponse();
 
 		response.setContentType("text/html");
 		response.setCharacterEncoding("UTF-8");
 		PrintWriter out = response.getWriter();
 
 		//ȡж֤
 		String kaptchaExpected = (String)request.getSession()
 				.getAttribute(com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY);
 		String kaptchaReceived = request.getParameter("code");
 
 		if (kaptchaReceived == null
 				|| !(kaptchaReceived.equalsIgnoreCase(kaptchaExpected)
 				|| kaptchaReceived.equals("immediate")))
 		{
 			out.write("wrong_code");
 		}
 		else
 		{
 			String recevier = request.getParameter("recevier");
 			String content = request.getParameter("content");
 			String userId = (String) request.getSession().getAttribute("id");
 
 			User targetUser = userService.getUser(recevier);
 			if(targetUser == null)
 				out.write("fail");
 			else if(hasShielded(targetUser.getId()))
 				out.write("has_been_shielded");
 			else
 			{
 				Message msg = new Message();
 				msg.setSenderid(userId);
 				msg.setContent(content);
 				msg.setState(ConstFigure.MESSAGE_STATUS_UNREAD);
 				msg.setType(ConstFigure.MESSAGE_TYPE_PRIVATE_LETTER);
 				msg.setTime(new Timestamp(System.currentTimeMillis()));
 				msg.setUser(targetUser);
 
 				if(messagerService.writeMsg(msg))
 				{
 					String output = "success#" + targetUser.getName() + "#" + targetUser.getPhotourl();
 
 					out.write(output);
 				}
 				else
 					out.write("fail");
 			}
 		}
 	}
 
 	private boolean hasShielded(String id)
 	{
 		String userId = (String) ServletActionContext.getRequest()
 				.getSession().getAttribute("id");
 		Contacter ct = messagerService.getContacterById(id);
 		if(ct != null)
 		{
 			for(User user : ct.getUsers())
 			{
 				if(user.getId().equals(userId))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	//õйص˽
 	public String getMyMessages()
 	{
 		HttpServletRequest request = ServletActionContext.getRequest();
 		List<Message> list = messagerService.getGroupMessages();
 
 		String userId = (String) request.getSession().getAttribute("id");
 
 		List<User> userList = new ArrayList<User>();
 		for(Message m : list)
 		{
 			if(m.getSenderid().equals(userId))
 				addToListIfNotExist(userList,m.getUser());
 			else
 				addToListIfNotExist(userList,userService.getUser(m.getSenderid()));
 		}
 
 		List<User> userBlackList = messagerService.getUserBlackList();
 
 		request.setAttribute("userList", userList);
 		request.setAttribute("userBlackList", userBlackList);
 
 		return SUCCESS;
 	}
 
 	private void addToListIfNotExist(List<User> userList, User user)
 	{
 		for(User u : userList)
 		{
 			if(u.getId().equals(user.getId()))
 				return;
 		}
 		userList.add(user);
 	}
 
 	//ӵ
 	public void addToBlackList() throws IOException
 	{
 		HttpServletRequest request = ServletActionContext.getRequest();
 		HttpServletResponse response = ServletActionContext.getResponse();
 
 		response.setContentType("text/html");
 		response.setCharacterEncoding("UTF-8");
 		PrintWriter out = response.getWriter();
 
 		String entrance = request.getParameter("shieldUser");
 		User user = userService.getUser(entrance);
 
 		if(user == null)
 		{
 			out.write("no_such_user");
 		}
 		else
 		{
 			String userId = (String) request.getSession().getAttribute("id");
 
 			if(userId.equals(user.getId()))
 				out.write("not_yourself");
 			else
 			{
 				Contacter ct = messagerService.getContacter();
 				if(ct == null)
 				{
 					ct = new Contacter();
 					ct.setId(userId);
 					ct.setType(ConstFigure.CONTACTER_TYPE_BLACKlIST);
 					messagerService.saveContacter(ct);
 				}
 
 				boolean isShielded = false;
 				Iterator<User> iterator = ct.getUsers().iterator();
 				while(iterator.hasNext()){
 					User u = (User)iterator.next();
 					if((u.getId()).equals(user.getId())){
 						isShielded = true;
 						break;
 					}	
 				}
 
 				if(isShielded)
 					out.write("repeat_shield");
 				else
 				{
 					ct.getUsers().add(user);
 					messagerService.updateContacter(ct);
 
 					String output = "success#" + user.getName() + "#" + user.getPhotourl() + "#" + user.getId();
 					out.write(output);
 				}
 			}
 		}
 	}
 
 	//ΣƳ
 	public void deleteBlackList() throws IOException
 	{
 		HttpServletRequest request = ServletActionContext.getRequest();
 		HttpServletResponse response = ServletActionContext.getResponse();
 
 		response.setContentType("text/html");
 		response.setCharacterEncoding("UTF-8");
 		PrintWriter out = response.getWriter();
 
 		String entrance = request.getParameter("shieldedUserId");
 		User shieldedUser = userService.getUser(entrance);
 		Contacter ct = messagerService.getContacter();
 
 		if(shieldedUser == null || ct == null)
 		{
 			out.write("no_such_user");
 		}
 		else
 		{
 			List<User> userBlackList = new ArrayList<User>(ct.getUsers());
 			Set<User> afterRemoveSet = new HashSet<User>();
 			boolean blackFlag = false;
 			for(User black : userBlackList)
 			{
 				if(shieldedUser.getId().equals(black.getId()))
 					blackFlag = true;
 				else
 					afterRemoveSet.add(black);
 			}
 
 			//б
 			if(!blackFlag)
 			{
 				out.write("no_int_black_list");
 			}
 			else
 			{
 				ct.setUsers(afterRemoveSet);
 				messagerService.updateContacter(ct);
 				String output = "success#" + shieldedUser.getName();
 				out.write(output);
 			}
 		}
 	}
 
 	//ˢվʾ˽
 	public void updateCurrentMessages() throws IOException
 	{
 		HttpServletRequest request = ServletActionContext.getRequest();
 		HttpServletResponse response = ServletActionContext.getResponse();
 
 		response.setContentType("text/html");
 		response.setCharacterEncoding("UTF-8");
 		PrintWriter out = response.getWriter();
 
 		/*String html = "<div class='oneMsg alert alert-info'>"
 				+ "Best check yo self, you're not... <img src='img/pian.jpg' />"
 				+ "<p class='timeInOneMsg'>"
 				+	"<small>2013-02-01 12:00</small>"
 				+	"</p>"
 				+	"</div>"
 				
 				+	"<div class='oneMsg myMsg alert'>"
 				+	"Best check yo self,"
 				+	"asdasdasdfasfasdfkjasfhkjasdhfiuasdfhkajsdfasdyou're not... "
 				+	"<img src='img/pian.jpg' />"
 				+	"<p class='timeInOneMsg'>"
 				+	"<small>2013-02-01 12:00</small>"
 				+	"</p>"
 				+	"</div>";
 
 		out.write(html);*/
 		String entrance = request.getParameter("chosenUserId");
 		User chosenUser = userService.getUser(entrance);
 
 		if(chosenUser!=null)
 		{
 			String html = "";
 			List<Message> showMessages = getRelevantMessages(chosenUser.getId());
 			for(Message message : showMessages)
 			{
 				html += formMessageForMessage(message);
 			}
 			out.write(html);
 		}
 	}
 
 	//ݵǰϢ͵ǰ¼ûʾhtml
 	private String formMessageForMessage(Message message)
 	{
 		String userId = (String) ServletActionContext.getRequest()
 				.getSession().getAttribute("id");
 		
 		String html = null;
 		if(!userId.equals(message.getSenderid()))
 		{
			html = "<div class='oneMsg alert alert-info'>"
 					+ message.getContent()
 					+ "<img src='img/pian.jpg' />"
 					+ "<p class='timeInOneMsg'>"
 					+ "<small>" + message.getTime().toString().substring(0,19) +"</small>"
 					+ "</p>"
 					+ "</div>";
 		}
 		else
 		{
			html = "<div class='oneMsg myMsg alert'>"
 					+ message.getContent()
 					+ "<img src='img/pian.jpg' />"
 					+ "<p class='timeInOneMsg'>"
 					+ "<small>" + message.getTime().toString().substring(0,19) +"</small>"
 					+ "</p>"
					+ "</div>";
 		}
 			
 		return html;
 	}
 
 	//õеǰ¼û͵ûϢ¼
 	//ʱ˳
 	private List<Message> getRelevantMessages(String id)
 	{
 		return messagerService.getRelevantMessages(id);
 	}
 }
