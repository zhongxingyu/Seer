 /*
  * Copyright (c) 2003, Rafael Steil
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  * 
  * 1) Redistributions of source code must retain the above 
  * copyright notice, this list of conditions and the 
  * following  disclaimer.
  * 2)  Redistributions in binary form must reproduce the 
  * above copyright notice, this list of conditions and 
  * the following disclaimer in the documentation and/or 
  * other materials provided with the distribution.
  * 3) Neither the name of "Rafael Steil" nor 
  * the names of its contributors may be used to endorse 
  * or promote products derived from this software without 
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
  * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
  * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
  * 
  * This file creation date: 20/05/2004 - 21:05:45
  * The JForum Project
  * http://www.jforum.net
  */
 package net.jforum.view.forum;
 
 import java.util.List;
 
 import net.jforum.Command;
 import net.jforum.JForum;
 import net.jforum.SessionFacade;
 import net.jforum.entities.Post;
 import net.jforum.entities.PrivateMessage;
 import net.jforum.entities.PrivateMessageType;
 import net.jforum.entities.User;
 import net.jforum.entities.UserSession;
 import net.jforum.model.DataAccessDriver;
 import net.jforum.util.I18n;
 import net.jforum.util.concurrent.executor.QueuedExecutor;
 import net.jforum.util.mail.EmailSenderTask;
 import net.jforum.util.mail.PrivateMessageSpammer;
 import net.jforum.util.preferences.ConfigKeys;
 import net.jforum.util.preferences.SystemGlobals;
 import freemarker.template.Template;
 
 /**
  * @author Rafael Steil
 * @version $Id: PrivateMessageVH.java,v 1.6 2004/06/21 03:48:09 rafaelsteil Exp $
  */
 public class PrivateMessageVH extends Command
 {
 	private String templateName;
 	
 	public void inbox() throws Exception
 	{
 		User user = new User();
 		user.setId(SessionFacade.getUserSession().getUserId());
 		
 		List pmList = DataAccessDriver.getInstance().newPrivateMessageModel().selectFromInbox(user);
 
 		JForum.getContext().put("inbox", true);
 		JForum.getContext().put("pmList", pmList);
 		JForum.getContext().put("moduleAction", "pm_list.htm");
 		this.putTypes();		
 	}
 	
 	public void sentbox() throws Exception
 	{
 		User user = new User();
 		user.setId(SessionFacade.getUserSession().getUserId());
 		
 		List pmList = DataAccessDriver.getInstance().newPrivateMessageModel().selectFromSent(user);
 
 		JForum.getContext().put("sentbox", true);
 		JForum.getContext().put("pmList", pmList);
 		JForum.getContext().put("moduleAction", "pm_list.htm");
 		this.putTypes();
 	}
 	
 	private void putTypes()
 	{
 		JForum.getContext().put("NEW", new Integer(PrivateMessageType.NEW));
 		JForum.getContext().put("READ", new Integer(PrivateMessageType.READ));
 		JForum.getContext().put("UNREAD", new Integer(PrivateMessageType.UNREAD));
 	}
 	
 	public void send() throws Exception
 	{
 		User user = DataAccessDriver.getInstance().newUserModel().selectById(
 						SessionFacade.getUserSession().getUserId());
 		
 		JForum.getContext().put("user", user);
 		JForum.getContext().put("moduleName", "pm");
 		JForum.getContext().put("action", "sendSave");
 		JForum.getContext().put("moduleAction", "post_form.htm");
 	}
 	
 	public void sendSave() throws Exception
 	{
 		String sId = JForum.getRequest().getParameter("toUserId");
 		String toUsername = JForum.getRequest().getParameter("toUsername");
 		String userEmail = JForum.getRequest().getParameter("toUserEmail");
 		
 		int toUserId = -1;
 		if (sId == null || sId.equals("")) {
 			List l = DataAccessDriver.getInstance().newUserModel().findByName(toUsername, true);
 			
 			if (l.size() > 0) {
 				User u = (User)l.get(0);
 				toUserId = u.getId();
 				userEmail = u.getEmail();
 			}
 		}
 		else {
 			toUserId = Integer.parseInt(sId);
 		}
 		
 		// We failed to get the user id?
 		if (toUserId == -1) {
 			JForum.getContext().put("moduleAction", "message.htm");
 			JForum.getContext().put("message", I18n.getMessage("PrivateMessage.userIdNotFound"));
 			return;
 		}
 		
 		PrivateMessage pm = new PrivateMessage();
 		pm.setPost(PostCommon.fillPostFromRequest());
 		
 		User fromUser = new User();
 		fromUser.setId(SessionFacade.getUserSession().getUserId());
 		pm.setFromUser(fromUser);
 		
 		User toUser = new User();
 		toUser.setId(toUserId);
 		toUser.setUsername(toUsername);
 		toUser.setEmail(userEmail);
 		pm.setToUser(toUser);
 		
 		boolean preview = (JForum.getRequest().getParameter("preview") != null);
 		if (!preview) {
 			DataAccessDriver.getInstance().newPrivateMessageModel().send(pm);
 			
 			JForum.getContext().put("moduleAction", "message.htm");
 			JForum.getContext().put("message", I18n.getMessage("PrivateMessage.messageSent", 
 							new String[] { JForum.getRequest().getContextPath() +"/pm/inbox.page" }));
 			
 			// If the target user if in the forum, then increments its 
 			// private messate count
 			String sid = SessionFacade.isUserInSession(toUserId);
 			if (sid != null) {
 				UserSession us = SessionFacade.getUserSession(sid);
 				us.setPrivateMessages(us.getPrivateMessages() + 1);
 			}
 			
 			if (userEmail != null && userEmail.trim().length() > 0) {
 				if (SystemGlobals.getBoolValue(ConfigKeys.MAIL_NOTIFY_ANSWERS)) {
 					try {
 						QueuedExecutor.getInstance().execute(new EmailSenderTask(new PrivateMessageSpammer(toUser)));
 					}
 					catch (Exception e) {
 						System.out.println(e);
 					}
 				}
 			}
 		}
 		else {
 			JForum.getContext().put("preview", true);
 			JForum.getContext().put("post", pm.getPost());
 			
 			Post postPreview = new Post(pm.getPost());
 			JForum.getContext().put("postPreview", PostCommon.preparePostText(postPreview));
 			JForum.getContext().put("pm", pm);
 
 			this.send();
 		}
 	}
 	
 	public void findUser() throws Exception
 	{
 		boolean showResult = false;
 		String username = JForum.getRequest().getParameter("username");
 
 		if (username != null && !username.equals("")) {
 			List namesList = DataAccessDriver.getInstance().newUserModel().findByName(username, false);
 			JForum.getContext().put("namesList", namesList);
 			showResult = true;
 		}
 		
 		JForum.getContext().put("username", username);
 		JForum.getContext().put("showResult", showResult);
 		this.setTemplateName("default/pm_finduser.htm");
 	}
 	
 	public void read() throws Exception
 	{
 		int id = Integer.parseInt(JForum.getRequest().getParameter("id"));
 		
 		PrivateMessage pm = new PrivateMessage();
 		pm.setId(id);
 		
 		pm = DataAccessDriver.getInstance().newPrivateMessageModel().selectById(pm);
 		
 		// Don't allow the read of messages that don't belongs
 		// to the current user
 		UserSession us = SessionFacade.getUserSession();
 		int userId = us.getUserId();
 		if (pm.getToUser().getId() == userId || pm.getFromUser().getId() == userId) {
 			pm.getPost().setText(PostCommon.preparePostText(pm.getPost()).getText());
 			
 			// Update the message status, if needed
 			if (pm.getType() == PrivateMessageType.NEW) {
 				pm.setType(PrivateMessageType.READ);
 				DataAccessDriver.getInstance().newPrivateMessageModel().updateType(pm);
 				us.setPrivateMessages(us.getPrivateMessages() - 1);
 			}
 			
 			JForum.getContext().put("pm", pm);
 			JForum.getContext().put("moduleAction", "pm_read_message.htm");
 		}
 		else {
 			JForum.getContext().put("moduleAction", "message.htm");
 			JForum.getContext().put("message", I18n.getMessage("PrivateMessage.readDenied"));
 		}
 	}
 	
 	public void delete() throws Exception
 	{
 		String ids[] = JForum.getRequest().getParameterValues("id");
 		
 		if (ids != null && ids.length > 0) {
 			PrivateMessage[] pm = new PrivateMessage[ids.length];
 			User u = new User();
 			u.setId(SessionFacade.getUserSession().getUserId());
 	
 			for (int i = 0; i < ids.length; i++) {
 				pm[i] = new PrivateMessage();
 				pm[i].setFromUser(u);
 				pm[i].setId(Integer.parseInt(ids[i]));
 			}
 			
 			DataAccessDriver.getInstance().newPrivateMessageModel().delete(pm);
 		}
 		
 		JForum.getContext().put("moduleAction", "message.htm");
 		JForum.getContext().put("message", I18n.getMessage("PrivateMessage.deleteDone", 
 						new String[] { "/pm/inbox.page" }));
 	}
 	
 	public void reply() throws Exception
 	{
 		int id = Integer.parseInt(JForum.getRequest().getParameter("id"));
 		
 		PrivateMessage pm = new PrivateMessage();
 		pm.setId(id);
 		pm = DataAccessDriver.getInstance().newPrivateMessageModel().selectById(pm);
 		
 		pm.getPost().setSubject(I18n.getMessage("PrivateMessage.replyPrefix") + pm.getPost().getSubject());
 		
 		JForum.getContext().put("moduleAction", "post_form.htm");
 		JForum.getContext().put("action", "sendSave");
 		JForum.getContext().put("pm", pm);
 		JForum.getContext().put("pmReply", true);
 		JForum.getContext().put("user", DataAccessDriver.getInstance().newUserModel().selectById(
 						SessionFacade.getUserSession().getUserId()));
 	}
 	
 	public void quote() throws Exception
 	{
 		int id = Integer.parseInt(JForum.getRequest().getParameter("id"));
 		
 		PrivateMessage pm = new PrivateMessage();
 		pm.setId(id);
 		pm = DataAccessDriver.getInstance().newPrivateMessageModel().selectById(pm);
 		
 		pm.getPost().setSubject(I18n.getMessage("PrivateMessage.replyPrefix") + pm.getPost().getSubject());
 		
 		JForum.getContext().put("quote", "true");
 		JForum.getContext().put("action", "sendSave");
 		JForum.getContext().put("quoteUser", pm.getFromUser().getUsername());
 		JForum.getContext().put("moduleAction", "post_form.htm");
 		JForum.getContext().put("post", pm.getPost());
 		JForum.getContext().put("pm", pm);
 		JForum.getContext().put("user", DataAccessDriver.getInstance().newUserModel().selectById(
 						SessionFacade.getUserSession().getUserId()));
 	}
 	
 	/** 
 	 * @see net.jforum.Command#list()
 	 */
 	public void list() throws Exception
 	{
 		this.inbox();
 	}
 
 	/** 
 	 * @see net.jforum.Command#process()
 	 */
 	public Template process() throws Exception
 	{
 		if (!SessionFacade.isLogged()) {
 			JForum.setRedirect(JForum.getRequest().getContextPath() +"/forums/list.page");
 			return null;
 		}
 
 		return super.process();
 	}
 }
