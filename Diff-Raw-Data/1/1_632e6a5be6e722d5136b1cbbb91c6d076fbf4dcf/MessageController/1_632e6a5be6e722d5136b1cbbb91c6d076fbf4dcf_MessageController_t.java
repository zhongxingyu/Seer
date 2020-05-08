 package controller;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import model.Chat;
 import model.Lobby;
 import model.Message;
 import model.User;
 
 @ManagedBean
 @ViewScoped
 public class MessageController implements Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private Chat chat;
 	private List<Message> history;
 	private Set<User> users;
 	private String content;
 	private String username;
 	private User user;
 
 	public MessageController() {
 	}
 
 	private void updateData() {
 		HttpServletRequest request = (HttpServletRequest) FacesContext
 				.getCurrentInstance().getExternalContext().getRequest();
 		HttpSession session = request.getSession();
 
 		List<Chat> temp = new ArrayList<Chat>(Lobby.getInstance().getChats());
 
 		for (Chat c : temp) {
 			if (c.getName().equals(session.getAttribute("chat"))) {
 				chat = c;
 			}
 		}
 
 		history = chat.getHistory();
 		users = chat.getUsers();
 
 		List<User> t = new ArrayList<User>(users);
 		for (User u : t) {
 			if (u.getName().equals(session.getAttribute("username"))) {
 				user = u;
 				username = u.getName();
 			}
 		}
 	}
 
 	public void sendMessage(ActionEvent e) {
 		if (content != null && history != null) {
 			Message m = new Message();
 			m.setContent(content);
 			m.setUser(user);
 			m.setDate(new Date());
			this.setContent(null);
 			history.add(m);
 		}
 	}
 
 	public List<Message> getHistory() {
 		updateData();
 
 		history = chat.getHistory();
 		return history;
 	}
 
 	public List<User> getUsers() {
 		updateData();
 		return new ArrayList<User>(users);
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}
 
 	public void addUser(User u) {
 		users.add(u);
 	}
 
 }
 
