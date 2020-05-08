 package com.dharma.pm.portlet;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.mail.internet.InternetAddress;
 import javax.portlet.PortletException;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 
 import com.dharma.model.PMBlockedUser;
 import com.dharma.model.PMDeletedMessage;
 import com.dharma.model.PMMessage;
 import com.dharma.pm.util.PMConstants;
 import com.dharma.pm.util.PMUtil;
 import com.dharma.service.PMBlockedUserLocalServiceUtil;
 import com.dharma.service.PMDeletedMessageLocalServiceUtil;
 import com.dharma.service.PMMessageLocalServiceUtil;
 import com.liferay.mail.service.MailServiceUtil;
 import com.liferay.portal.kernel.log.Log;
 import com.liferay.portal.kernel.log.LogFactoryUtil;
 import com.liferay.portal.kernel.mail.MailMessage;
 import com.liferay.portal.kernel.util.HtmlUtil;
 import com.liferay.portal.kernel.util.ParamUtil;
 import com.liferay.portal.model.User;
 import com.liferay.portal.service.UserLocalServiceUtil;
 import com.liferay.portal.theme.ThemeDisplay;
 import com.liferay.util.bridges.mvc.MVCPortlet;
 import com.liferay.portal.kernel.util.PropsUtil;
 
 /** 
  * 
  * @author akhojoyan
  *
  */
 //@SuppressWarnings("unchecked")
 public class PMPortlet extends MVCPortlet {
 
 	static Log log = LogFactoryUtil.getLog(PMPortlet.class);
 
 	private long userId = 0L;
 
 	@Override
 	public void render(RenderRequest request, RenderResponse response)
 			throws PortletException, IOException {
 
 		int countPerPage = 10;	//	TODO:	get from configuration
 
 		try {
 			String includeFile = "/view.jsp";
 
 			ThemeDisplay td = (ThemeDisplay) request.getAttribute("THEME_DISPLAY");
 			if (!td.isSignedIn()) {
 				throw new Exception("Please sign in in order to access this portlet");
 			}
 
 			String action = ParamUtil.getString(request, PMConstants.ACTION);
 
 			String startIndexStr = ParamUtil.getString(request, "startIndex");
 			startIndexStr = startIndexStr.trim().equals("") ? "0" : startIndexStr;
 			int startIndex = Integer.parseInt(startIndexStr);
 			int endIndex = startIndex + countPerPage;
 			request.setAttribute("start", startIndex);
 			String pageStr = ParamUtil.getString(request, "page");
 			if (!pageStr.trim().equals("")) {
 				action = pageStr;
 			}
 
 			User user = td.getUser();
 			userId = user.getUserId();
 			if (action.equals(PMConstants.INBOX_ACTION)) {
 				includeFile = "/view.jsp";
 			} else if (action.equals(PMConstants.VIEW_MESSAGE_ACTION)) {
 				String pId = ParamUtil.getString(request, "messageId");
 				request.setAttribute("messageId", pId);
 				includeFile = "/view_message.jsp";
 			} else if (action.equals(PMConstants.OUTBOX_ACTION)) {
 				includeFile = "/outbox.jsp";
 			} else if (action.equals(PMConstants.TRASH_ACTION)) {
 				includeFile = "/trash.jsp";
 			} else if (action.equals(PMConstants.COMPOSE_ACTION)) {
 				request.setAttribute("parentMessageId", "-1");
 				includeFile = "/compose.jsp";
 			} else if (action.equals(PMConstants.REPLY_ACTION)) {
 				String pId = ParamUtil.getString(request, "parentMessageId");
 				request.setAttribute("parentMessageId", pId);
 				includeFile = "/compose.jsp";
 			} else if (action.equals(PMConstants.ADD_ACTION)) {
 				try {
 					createNewMessage(request);
 					includeFile = "/view.jsp";
 				} catch (Exception e) {
 					String parentMessageIdStr = ParamUtil.getString(request, "parentMessageId");
 					request.setAttribute("parentMessageId", parentMessageIdStr);
 					request.setAttribute(PMConstants.ERROR_MESSAGE, e.getMessage());
 					includeFile = "/compose.jsp";
 				}
 			} else if (action.equals(PMConstants.DELETE_ACTION)) {
 				try {
 					String type = ParamUtil.getString(request, "type");
 					if(type.equals("delete")) {
 						deleteMessages(request);
 					} else if(type.equals("undelete")) {
 						undeleteMessages(request);
 					} else if(type.equals("block")) {
 						blockUsers(request);
 					} else if(type.equals("unblock")) {
 						unblockUsers(request);
 					} else if(type.equals("erase")) {
 						eraseMessages(request);
 					}
 					includeFile = "/view.jsp";
 				} catch (Exception e) {
 					request.setAttribute(PMConstants.ERROR_MESSAGE, e.getMessage());
 					includeFile = "/view.jsp";
 				}
 			} else if (action.equals(PMConstants.SETTINGS_ACTION)) {
 				includeFile = "/settings.jsp";
 			} else {
 				includeFile = "/view.jsp";
 			}
 
 			if(includeFile.equals("/view.jsp")) {
 				// save selected page of messages list
 				List<PMMessage> messages = PMMessageLocalServiceUtil.getInboxMessages(userId);
 				int totalCount = messages.size();
 				List<PMMessage> filtered = new ArrayList<PMMessage>();
 				if (endIndex > messages.size()) {
 					endIndex = messages.size();
 				}
 				for (int i = startIndex; i < endIndex; i++) {
 					filtered.add(messages.get(i));
 				}
 				messages = filtered;
 				request.setAttribute(PMConstants.MESSAGES_LIST, messages);
 
 				//	save total count of Inbox messages
 				request.setAttribute(PMConstants.TOTAL_COUNT, totalCount);
 			} else if(includeFile.equals("/outbox.jsp")) {
 				// save selected page of messages list
 				List<PMMessage> messages = PMMessageLocalServiceUtil.getOutboxMessages(userId);
 				int totalCount = messages.size();
 				List<PMMessage> filtered = new ArrayList<PMMessage>();
 				if (endIndex > messages.size()) {
 					endIndex = messages.size();
 				}
 				for (int i = startIndex; i < endIndex; i++) {
 					filtered.add(messages.get(i));
 				}
 				messages = filtered;
 				request.setAttribute(PMConstants.MESSAGES_LIST, messages);
 
 				//	save total count of Outbox messages
 				request.setAttribute(PMConstants.TOTAL_COUNT, totalCount);
 			} else if(includeFile.equals("/trash.jsp")) {
 				// save selected page of messages list
 				List<PMDeletedMessage> messages = PMDeletedMessageLocalServiceUtil.findByOwnerId(userId);
 				int totalCount = messages.size();
 				List<PMDeletedMessage> filtered = new ArrayList<PMDeletedMessage>();
 				if (endIndex > messages.size()) {
 					endIndex = messages.size();
 				}
 				for (int i = startIndex; i < endIndex; i++) {
 					filtered.add(messages.get(i));
 				}
 				messages = filtered;
 				request.setAttribute(PMConstants.MESSAGES_LIST, messages);
 
 				//	save total count of Trash messages
 				request.setAttribute(PMConstants.TOTAL_COUNT, totalCount);
 			}
 
 			// show unread notification in portlet header
 			response.setTitle("Private Messages");
 			int unreadCount = PMMessageLocalServiceUtil.getUnreadCount(userId);
 			if (unreadCount > 0) {
 				response.setTitle("Private Messages (" + unreadCount + " new)");
 			}
 
 			include(includeFile, request, response);
 		} catch (Exception e) {
 			request.setAttribute(PMConstants.ERROR_MESSAGE, "Wrong usage");
 			include("/error.jsp", request, response);
 		}
 	}
 
 	/**
 	 * Creates a new message and sends an email notification to each recepient
 	 * 
 	 * @param request
 	 * @throws Exception
 	 */
 	private void createNewMessage(RenderRequest request) throws Exception {
 		String recepients = ParamUtil.getString(request, "recepients");
 		if (recepients.trim().equals("")) {
 			throw new Exception("Please select message recepients");
 		}
 		String subject = ParamUtil.getString(request, "subject");
 		if (subject.trim().equals("")) {
 			throw new Exception("Please enter message subject");
 		}
 		String body = ParamUtil.getString(request, "body");
 		if (body.trim().equals("")) {
 			throw new Exception("Please enter message body");
 		}
 		body = HtmlUtil.stripHtml(body);
 
 		String parentMessageIdStr = ParamUtil.getString(request, "parentMessageId");
 		long parentMessageId = Long.parseLong(parentMessageIdStr);
 
 		PMMessage pmMessage = PMMessageLocalServiceUtil.createPMMessage(0L);
 		pmMessage.setOwnerId(userId);
 		User user = UserLocalServiceUtil.getUserById(userId);
 		pmMessage.setOwnerName(user.getFullName());
 		pmMessage.setRecepients(recepients);
 		pmMessage.setSubject(subject);
 		pmMessage.setBody(body);
 		pmMessage.setParentMessageId(parentMessageId);
 		pmMessage.setPostedDate(new Date());
 		PMMessageLocalServiceUtil.updatePMMessage(pmMessage);
 
 		sendNotificationEmail(pmMessage);
 
 		request.setAttribute(PMConstants.INFO_MESSAGE, "Message successfully sent");
 	}
 
 	/**
 	 * Deletes selected messages
 	 * 
 	 * @param request
 	 * @throws Exception
 	 */
 	private void deleteMessages(RenderRequest request) throws Exception {
 		String ids = request.getParameter("ids");
 		String[] messages = ids.split(";");
 		for(String m : messages) {
 			long messageId = Long.parseLong(m);			
 			PMDeletedMessage dm = PMDeletedMessageLocalServiceUtil.createPMDeletedMessage(0L);
 			dm.setMessageId(messageId);
 			dm.setDeletedDate(new Date());
 			dm.setOwnerId(userId);
 			PMDeletedMessageLocalServiceUtil.addPMDeletedMessage(dm);
 		}
 		request.setAttribute(PMConstants.INFO_MESSAGE, "Messages successfully removed");
 	}
 
 	/**
 	 * Completele erases selected messages
 	 * 
 	 * @param request
 	 * @throws Exception
 	 */
 	private void eraseMessages(RenderRequest request) throws Exception {
 		String ids = request.getParameter("ids");
 		String[] messages = ids.split(";");
 		for(String m : messages) {
 			long messageId = Long.parseLong(m);				
 			List<PMDeletedMessage> dmList = PMDeletedMessageLocalServiceUtil.findByMessageId(messageId);
 			for(PMDeletedMessage dm : dmList) {
 				PMDeletedMessageLocalServiceUtil.deletePMDeletedMessage(dm.getDeletedMessageId());
 			}
 			PMMessageLocalServiceUtil.deletePMMessage(messageId);
 		}
 		request.setAttribute(PMConstants.INFO_MESSAGE, "Messages successfully erased");
 	}
 
 	/**
 	 * Undeletes selected messages
 	 * 
 	 * @param request
 	 * @throws Exception
 	 */
 	private void undeleteMessages(RenderRequest request) throws Exception {
 		String ids = request.getParameter("ids");
 		String[] messages = ids.split(";");
 		for(String m : messages) {
 			long messageId = Long.parseLong(m);
 			PMDeletedMessageLocalServiceUtil.deletePMDeletedMessage(messageId);
 		}
 		request.setAttribute(PMConstants.INFO_MESSAGE, "Messages successfully undeleted");
 	}
 
 	/**
 	 * Blocks selected users
 	 * 
 	 * @param request
 	 * @throws Exception
 	 */
 	private void blockUsers(RenderRequest request) throws Exception {
 		String ids = request.getParameter("ids");
 		String[] users = ids.split(";");
 		for(String u : users) {
 			long uId = Long.parseLong(u);
 			PMBlockedUser bu = PMBlockedUserLocalServiceUtil.createPMBlockedUser(0L);
 			bu.setOwnerId(userId);
 			bu.setUserId(uId);
 			bu.setBlockedDate(new Date());
 			PMBlockedUserLocalServiceUtil.addPMBlockedUser(bu);
 		}
 		request.setAttribute(PMConstants.INFO_MESSAGE, "Users successfully blocked");
 	}
 
 	/**
 	 * Unblocks selected users
 	 * 
 	 * @param request
 	 * @throws Exception
 	 */
 	private void unblockUsers(RenderRequest request) throws Exception {
 		String ids = request.getParameter("ids");
 		String[] users = ids.split(";");
 		for(String u : users) {
 			long blockedUserId = Long.parseLong(u);
 			PMBlockedUserLocalServiceUtil.deletePMBlockedUser(blockedUserId);
 		}
 		request.setAttribute(PMConstants.INFO_MESSAGE, "Users successfully unblocked");
 	}
 
 	private void sendNotificationEmail(PMMessage message) {
 		try {
 			String fromEmail =  PropsUtil.get("dharma.pm.fromaddress"); //"admin@interactivebuddha.com";
 			String fromName = PropsUtil.get("dharma.pm.fromname"); //"Interactive Buddha";
 			String subject = PropsUtil.get("dharma.pm.mailsubject"); //"New Message on DharmaOverground";
 			
 			if (null == fromEmail) {
 				fromEmail = "admin@example.com";
 			}
 			
 			if (null == fromName) {
 				fromName = "Portal Administrator";
 			}
 			
 			if (null == subject) {
				subject = "New personal message available on portal.";
 			}
 			
 			
 			String body = "You received a new message from " + PMUtil.getSenderEmail(message);
 			InternetAddress from = new InternetAddress(fromEmail, fromName);
 			Set<User> toList = PMUtil.getRecepientsList(message.getRecepients());
 			
 			System.err.println("Would send message from " + fromName + " at " + fromEmail);
 			
 			for (User user : toList) {
 				//	send notification if user is blocked
 				if(PMBlockedUserLocalServiceUtil.isUserBlocked(userId, user.getUserId())) {
 					sendBlockedNotification(user.getUserId());
 					continue;
 				}
 				InternetAddress to = new InternetAddress(user.getEmailAddress());
 				MailMessage emailMessage = new MailMessage(from, to, subject, body, false);
 				try {
 					MailServiceUtil.sendEmail(emailMessage);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void sendBlockedNotification(long blockerId) {
 		try {
 			User owner = UserLocalServiceUtil.getUserById(userId);
 			User blocker = UserLocalServiceUtil.getUserById(blockerId);
 			String fromEmail =  PropsUtil.get("dharma.pm.fromaddress"); //"admin@interactivebuddha.com";
 			String fromName = PropsUtil.get("dharma.pm.fromname"); //"Interactive Buddha";
 			
 			if (null == fromEmail) {
 				fromEmail = "admin@example.com";
 			}
 			
 			if (null == fromName) {
 				fromEmail = "Portal Administrator";
 			}
 			
 			String subject = "You are blocked by " + blocker.getFullName();
 			String body = "User " + blocker.getFullName() + " blocked you and won't receive your message";
 			InternetAddress from = new InternetAddress(fromEmail, fromName);
 			InternetAddress to = new InternetAddress(owner.getEmailAddress());
 			MailMessage emailMessage = new MailMessage(from, to, subject, body, false);
 			MailServiceUtil.sendEmail(emailMessage);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
