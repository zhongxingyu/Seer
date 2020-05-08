 package org.sakaiproject.yaft.tool;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 import net.sf.json.JsonConfig;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.log4j.Logger;
 import org.sakaiproject.component.api.ComponentManager;
 import org.sakaiproject.entitybroker.exception.EntityException;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.util.RequestFilter;
 import org.sakaiproject.util.ResourceLoader;
 import org.sakaiproject.yaft.api.ActiveDiscussion;
 import org.sakaiproject.yaft.api.Attachment;
 import org.sakaiproject.yaft.api.Discussion;
 import org.sakaiproject.yaft.api.Forum;
 import org.sakaiproject.yaft.api.Message;
 import org.sakaiproject.yaft.api.SakaiProxy;
 import org.sakaiproject.yaft.api.YaftForumService;
 import org.sakaiproject.yaft.api.YaftPreferences;
 
 /**
  * This servlet handles all of the REST type stuff. At some point this may all
  * move into an EntityProvider.
  * 
  * @author Adrian Fish (a.fish@lancaster.ac.uk)
  */
 public class YaftTool extends HttpServlet
 {
 	private Logger logger = Logger.getLogger(YaftTool.class);
 
 	private YaftForumService yaftForumService = null;
 
 	private SakaiProxy sakaiProxy;
 
 	public void destroy()
 	{
 		logger.info("destroy");
 
 		super.destroy();
 	}
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
 	{
 		if (logger.isDebugEnabled()) logger.debug("doGet()");
 		
 		if(yaftForumService == null || sakaiProxy == null)
 			throw new ServletException("yaftForumService and sakaiProxy MUST be initialised.");
 		
 		User user = sakaiProxy.getCurrentUser();
 		
 		if(user == null)
 		{
 			// We are not logged in
 			throw new ServletException("getCurrentUser returned null.");
 		}
 		
 		String siteId = sakaiProxy.getCurrentSiteId();
 		String placementId = sakaiProxy.getCurrentToolId();
 		
 		// We need to pass the language code to the JQuery code in the pages.
 		Locale locale = (new ResourceLoader(user.getId())).getLocale();
 		String languageCode = locale.getLanguage();
 
 		String pathInfo = request.getPathInfo();
 
 		if (pathInfo == null || pathInfo.length() < 1)
 		{
 			// There's no path info, so this is the initial state
 			response.sendRedirect("/yaft-tool/yaft.html?state=forums&siteId=" + siteId + "&placementId=" + placementId + "&language=" + languageCode);
 			return;
 		}
 		else
 		{
 			String[] parts = pathInfo.substring(1).split("/");
 
 			if (parts.length >= 1)
 			{
 				String part1 = parts[0];
 
 				if ("forums".equals(part1))
 				{
 					doForumsGet(request,response,parts);
 				}
 				
 				else if ("userPreferences".equals(part1))
 				{
 					YaftPreferences prefs = yaftForumService.getPreferencesForUser(sakaiProxy.getCurrentUser().getId(),siteId);
 					JSONObject data = JSONObject.fromObject(prefs);
 					response.setStatus(HttpServletResponse.SC_OK);
 					response.setContentType("application/json");
 					response.getWriter().write(data.toString());
 					return;
 				}
 				
 				else if ("activeDiscussions".equals(part1))
 				{
 					List<ActiveDiscussion> activeDiscussions = yaftForumService.getActiveDiscussions(siteId);
 					JSONArray data = JSONArray.fromObject(activeDiscussions);
 					response.setStatus(HttpServletResponse.SC_OK);
 					response.setContentType("application/json");
 					response.getWriter().write(data.toString());
 					return;
 				}
 				
 				else if ("forumContainingMessage".equals(part1))
 				{
 					String messageId = (String) request.getParameter("messageId");
 					
 					if (messageId == null)
 					{
 						response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Invalid path provided: expect to receive the message id");
 						return;
 					}
 					
 					Message message = yaftForumService.getMessage(messageId);
 					
 					if(message == null)
 					{
 						response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Invalid messageId provided.");
 						return;
 					}
 					
 					Forum forum = yaftForumService.getForumContainingMessage(messageId);
 					
 					JSONObject data = JSONObject.fromObject(forum);
 					response.setStatus(HttpServletResponse.SC_OK);
 					response.setContentType("application/json");
 					response.getWriter().write(data.toString());
 					return;
 				}
 				
 				else if("unsubscriptions".equals(part1))
 				{
 					List<String> unsubs = yaftForumService.getForumUnsubscriptions(user.getId());
 					JSONArray data = JSONArray.fromObject(unsubs);
 					response.setStatus(HttpServletResponse.SC_OK);
 					response.setContentType("application/json");
 					response.getWriter().write(data.toString());
 					return;
 				}
 				
 				else if ("discussions".equals(part1))
 				{
					doDiscussionsGet(request,response,parts,siteId,placementId,languageCode);
 				}
 				
 				else if ("messages".equals(part1))
 				{
 					doMessagesGet(request,response,parts,siteId,placementId,languageCode);
 				}
 			}
 		}
 	}
 	
 	private void doForumsGet(HttpServletRequest request, HttpServletResponse response,String[] parts) throws ServletException, IOException
     {
         String siteId = sakaiProxy.getCurrentSiteId();
         String state = request.getParameter("state");
 
         if(state == null) state  = "";
 
         if(parts.length == 1)
         {
             try
             {
                 List<Forum> fora = yaftForumService.getSiteForums(siteId, false);
 
                 JSONArray data = JSONArray.fromObject(fora);
                 response.setStatus(HttpServletResponse.SC_OK);
                 response.setContentType("application/json");
                 response.getWriter().write(data.toString());
                 response.getWriter().close();
                 return;
             }
             catch (Exception e)
             {
                 logger.error("Caught exception whilst getting fora.", e);
                 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 return;
             }
         }
         else if (parts.length >= 2)
         {
             String forumId = parts[1];
 
             if("allReadMessages".equals(forumId))
             {
             	Map<String,Integer> read = yaftForumService.getReadMessageCountForAllFora();
                 JSONObject data = JSONObject.fromObject(read);
                 response.setStatus(HttpServletResponse.SC_OK);
                 response.setContentType("application/json");
                 response.getWriter().write(data.toString());
                 return;
             }
 
             if(parts.length == 2)
             {
                 try
                 {
                     Forum forum = yaftForumService.getForum(forumId, state);
                     JsonConfig config = new JsonConfig();
                     config.setExcludes(new String[] {"properties","reference"});
                     JSONObject data = JSONObject.fromObject(forum,config);
                     response.setStatus(HttpServletResponse.SC_OK);
                     response.setContentType("application/json");
                     response.getWriter().write(data.toString());
                     return;
                 }
                 catch (Exception e)
                 {
                     logger.error("Caught exception whilst getting forum.", e);
                     response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                     return;
                 }
             }
             else
             {
                 String forumOp = parts[2];
 
                 if ("delete".equals(forumOp))
                 {
                     yaftForumService.deleteForum(forumId);
                     response.setStatus(HttpServletResponse.SC_OK);
                     response.setContentType("text/plain");
                     response.getWriter().write("success");
                     response.getWriter().close();
                     return;
                 }
                 if ("subscribe".equals(forumOp))
                 {
                     yaftForumService.subscribeToForum(forumId);
                     response.setStatus(HttpServletResponse.SC_OK);
                     response.setContentType("text/plain");
                     response.getWriter().write("success");
                     response.getWriter().close();
                     return;
                 }
                 else if ("unsubscribe".equals(forumOp))
                 {
                     yaftForumService.unsubscribeFromForum(forumId);
                     response.setStatus(HttpServletResponse.SC_OK);
                     response.setContentType("text/plain");
                     response.getWriter().write("success");
                     response.getWriter().close();
                     return;
                 }
                 else if ("readMessages".equals(forumOp))
                 {
             		Map<String,Integer> counts = yaftForumService.getReadMessageCountForForum(forumId);
                     JSONObject data = JSONObject.fromObject(counts);
                     response.setStatus(HttpServletResponse.SC_OK);
                     response.setContentType("application/json");
                     response.getWriter().write(data.toString());
                     return;
                 }
             }
         }
     }
 	
	private void doDiscussionsGet(HttpServletRequest request, HttpServletResponse response,String[] parts,String siteId,String placementId,String languageCode) throws ServletException, IOException
     {
         if (parts.length >= 2)
 		{
 			String discussionId = parts[1];
 			
 			if(parts.length == 2)
 			{
             	if("unsubscriptions".equals(discussionId))
             	{
             		List<String> unsubs = yaftForumService.getDiscussionUnsubscriptions(sakaiProxy.getCurrentUser().getId());
             		JSONArray data = JSONArray.fromObject(unsubs);
             		response.setStatus(HttpServletResponse.SC_OK);
             		response.setContentType("application/json");
             		response.getWriter().write(data.toString());
             		response.getWriter().close();
             		return;
             	}
             	else if("discussionContainingMessage".equals(discussionId))
             	{
             		String messageId = (String) request.getParameter("messageId");
             		
             		if (messageId == null)
             			throw new IllegalArgumentException("Invalid path provided: expect to receive the message id");
             		
             		Message message = yaftForumService.getMessage(messageId);
             		
             		if(message == null)
             		{
             			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Invalid message id supplied.");
             			return;
             		}
             		
             		Discussion discussion = yaftForumService.getDiscussion(message.getDiscussionId(),true);
 					JsonConfig config = new JsonConfig();
 					config.setExcludes(new String[] {"properties","reference"});
             		JSONArray data = JSONArray.fromObject(discussion,config);
             		response.setStatus(HttpServletResponse.SC_OK);
             		response.setContentType("application/json");
             		response.getWriter().write(data.toString());
             		response.getWriter().close();
             		return;
             	}
             	
            	boolean isHtmlRequest = false;
        	
            	if(discussionId.endsWith(".html"))
            	{
            		isHtmlRequest = true;
            		discussionId = discussionId.substring(0, discussionId.length() - 5);
            	}
            	
				if(isHtmlRequest)
				{
					Forum forum = yaftForumService.getForumContainingMessage(discussionId);
					response.sendRedirect("/yaft-tool/yaft.html?state=full&discussionId=" + discussionId + "&siteId=" + siteId + "&placementId=" + placementId + "&forumId=" + forum.getId() + "&language=" + languageCode);
					return;
				}
            	
                 Discussion discussion = yaftForumService.getDiscussion(discussionId, true);
                 
 				JsonConfig config = new JsonConfig();
 				config.setExcludes(new String[] {"properties","reference"});
 
                 JSONObject data = JSONObject.fromObject(discussion,config);
                 response.setStatus(HttpServletResponse.SC_OK);
                 response.setContentType("application/json");
                 response.getWriter().write(data.toString());
                 response.getWriter().close();
                 return;
 			}
 			else
 			{
 				String discussionOp = parts[2];
 				if ("delete".equals(discussionOp))
 				{
 					yaftForumService.deleteDiscussion(discussionId);
 					response.setStatus(HttpServletResponse.SC_OK);
 					response.setContentType("text/plain");
 					response.getWriter().write("success");
 					response.getWriter().close();
 					return;
 				}
 				else if ("subscribe".equals(discussionOp))
 				{
 					yaftForumService.subscribeToDiscussion(null, discussionId);
 					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
 					response.setContentType("text/plain");
 					response.getWriter().write("success");
 					response.getWriter().close();
 					return;
 				}
 				else if ("unsubscribe".equals(discussionOp))
 				{
 					yaftForumService.unsubscribeFromDiscussion(null, discussionId);
 					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
 					response.setContentType("text/plain");
 					response.getWriter().write("success");
 					response.getWriter().close();
 					return;
 				}
 				else if ("markRead".equals(discussionOp))
 				{
 					Forum forum = yaftForumService.getForumContainingMessage(discussionId);
 					String forumId = forum.getId();
 					if(yaftForumService.markDiscussionRead(discussionId, forumId))
 					{
 						response.setContentType("text/plain");
 						response.getWriter().write("success");
 						response.getWriter().close();
 					}
 					else
 					{
 						response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 						response.getWriter().close();
 					}
 					
 					return;
 				}
 				else if ("readMessages".equals(discussionOp))
 				{
 					List<String> ids = yaftForumService.getReadMessageIds(discussionId);
             		JSONArray data = JSONArray.fromObject(ids);
             		response.setStatus(HttpServletResponse.SC_OK);
             		response.setContentType("application/json");
             		response.getWriter().write(data.toString());
             		response.getWriter().close();
             		return;
 				}
 			}
 		}
     }
 	
 	private void doMessagesGet(HttpServletRequest request, HttpServletResponse response,String[] parts, String siteId,String placementId,String languageCode) throws ServletException, IOException
     {
         if (parts.length >= 2)
 		{
         	String messageId = parts[1];
         	
         	boolean isHtmlRequest = false;
         	
 			if(messageId.endsWith(".html"))
 			{
 				isHtmlRequest = true;
 				messageId = messageId.substring(0, messageId.length() - 5);
 			}
 			
 			Message message = yaftForumService.getMessage(messageId);
 			Forum forum = yaftForumService.getForumContainingMessage(messageId);
 			
 			if(parts.length == 2)
 			{
 				if(isHtmlRequest)
 				{
 					response.sendRedirect("/yaft-tool/yaft.html?state=full&discussionId=" + message.getDiscussionId() + "&siteId=" + siteId + "&placementId=" + placementId + "&forumId=" + forum.getId()  + "&language=" + languageCode + "#message-" + messageId);
 					return;
 				}
 				
 				JsonConfig config = new JsonConfig();
 				config.setExcludes(new String[] {"properties","reference"});
 
                 JSONObject data = JSONObject.fromObject(message,config);
                 response.setStatus(HttpServletResponse.SC_OK);
                 response.setContentType("application/json");
                 response.getWriter().write(data.toString());
                 response.getWriter().close();
                 return;
 			}
 			
 			String messageOp = "";
 			
 			if(parts.length >= 3)
 				messageOp = parts[2];
 			
 			if ("markRead".equals(messageOp))
 			{
 				if (yaftForumService.markMessageRead(messageId,forum.getId(),message.getDiscussionId()))
 				{
 					response.setStatus(HttpServletResponse.SC_OK);
 					response.setContentType("text/plain");
 					response.getWriter().write("success");
 				}
 				else
 					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 
 				response.getWriter().close();
 				
 				return;
 			}
 			else if ("markUnRead".equals(messageOp))
 			{
 				if (yaftForumService.markMessageUnRead(messageId,forum.getId(),message.getDiscussionId()))
 				{
 					response.setStatus(HttpServletResponse.SC_OK);
 					response.setContentType("text/plain");
 					response.getWriter().write("success");
 				}
 				else
 				{
 					logger.error("Failed to mark message with id '" + messageId + "' as un-read");
 					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				}
 				
 				response.getWriter().close();
 
 				return;
 			}
 			else if ("delete".equals(messageOp))
 			{
 				yaftForumService.deleteMessage(message,forum.getId());
 				response.setStatus(HttpServletResponse.SC_OK);
 				response.setContentType("text/plain");
 				response.getWriter().write("success");
 				response.getWriter().close();
 				return;
 			}
 			else if ("undelete".equals(messageOp))
 			{
 				yaftForumService.undeleteMessage(message,forum.getId());
 				response.setStatus(HttpServletResponse.SC_OK);
 				response.setContentType("text/plain");
 				response.getWriter().write("success");
 				response.getWriter().close();
 				return;
 
 			}
 			else if ("publish".equals(messageOp))
 			{
 				yaftForumService.publishMessage(forum.getId(),message);
 				response.setStatus(HttpServletResponse.SC_OK);
 				response.setContentType("text/plain");
 				response.getWriter().write("success");
 				response.getWriter().close();
 				return;
 			}
 			else if ("attachments".equals(messageOp))
 			{
 				if(parts.length >= 4)
 				{
 					String attachmentId = parts[3];
 					if(parts.length >= 5)
 					{
 						String attachmentOp = parts[4];
 						
 						if("delete".equals(attachmentOp))
 						{
 							yaftForumService.deleteAttachment(attachmentId, messageId);
 
 							response.setStatus(HttpServletResponse.SC_OK);
 							response.setContentType("text/plain");
 							response.getWriter().write("success");
 							response.getWriter().close();
 							return;
 						}
 					}
 				}
 			}
 			
 			if(parts.length == 2)
 			{
 				JsonConfig config = new JsonConfig();
 				config.setExcludes(new String[] {"properties","reference"});
 
                 JSONObject data = JSONObject.fromObject(message,config);
                 response.setStatus(HttpServletResponse.SC_OK);
                 response.setContentType("application/json");
                 response.getWriter().write(data.toString());
                 response.getWriter().close();
                 return;
 			}
 		}
     }
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
 	{
 		logger.info("doPost()");
 		
 		String pathInfo = request.getPathInfo();
 		
 		String[] parts = new String[] {};
 		
 		if(pathInfo != null)
 			parts = pathInfo.substring(1).split("/");
 
 		if (parts.length >= 1)
 		{
 			String part1 = parts[0];
 
 			if ("forums".equals(part1))
 				doForumsPost(request,response,parts);
 			else if ("discussions".equals(part1))
 				doDiscussionsPost(request,response,parts);
 			else if ("messages".equals(part1))
 				doMessagesPost(request,response,parts);
 			else if ("preferences".equals(part1))
 			{
 				String emailPreference = request.getParameter("emailPreference");
 				String viewPreference = request.getParameter("viewPreference");
 				if (logger.isDebugEnabled())
 				{
 					logger.debug("emailPreference: " + emailPreference);
 					logger.debug("viewPreference: " + viewPreference);
 				}
 			
 				YaftPreferences preferences = new YaftPreferences(emailPreference,viewPreference);
 			
 				if(yaftForumService.savePreferences(preferences))
 				{
 					JSONObject data = JSONObject.fromObject(preferences);
 					response.setStatus(HttpServletResponse.SC_OK);
 					response.setContentType("application/json");
 					response.getWriter().write(data.toString());
 					response.getWriter().close();
 					return;
 				}
 			}
 		}
 
 		String function = request.getParameter("function");
 
 		if (logger.isDebugEnabled())
 			logger.debug("function=" + function);
 
 		if(function != null && function.equals("moveDiscussion"))
 		{
 			String currentForumId = request.getParameter("currentForumId");
 			String newForumId = request.getParameter("forumId");
 			String discussionId = request.getParameter("discussionId");
 
 			if (logger.isDebugEnabled())
 			{
 				logger.debug("Current Forum ID: " + currentForumId);
 				logger.debug("Forum ID: " + newForumId);
 				logger.debug("Discussion ID: " + discussionId);
 			}
 
 			yaftForumService.moveDiscussion(discussionId, currentForumId, newForumId);
 			response.sendRedirect("/portal/tool/" + sakaiProxy.getCurrentToolId() + "/forums/" + currentForumId);
 			return;
 		}
 	}
 	
 	private void doForumsPost(HttpServletRequest request, HttpServletResponse response, String[] parts) throws ServletException, IOException
 	{
 		String siteId = sakaiProxy.getCurrentSiteId();
         String state = request.getParameter("state");
 
         if(state == null) state  = "";
 
         if(parts.length == 1)
         {
             try
             {
         		String id = (String) request.getParameter("id");
         		String title = (String) request.getParameter("title");
         		String description = (String) request.getParameter("description");
         		String startDate= (String) request.getParameter("startDate");
         		String endDate= (String) request.getParameter("endDate");
         		String lockWritingString = (String) request.getParameter("lockWriting");
         		String lockReadingString = (String) request.getParameter("lockReading");
         		
         		boolean lockWriting = true;
         		boolean lockReading = true;
         		
         		if(lockWritingString != null)
         			lockWriting = lockWritingString.equals("true");
         		else
         			lockWriting = false;
         		
         		if(lockReadingString != null)
         			lockReading = lockReadingString.equals("true");
         		else
         			lockReading = false;
         		
         		if (logger.isDebugEnabled())
         		{
         			logger.debug("Title: " + title);
         			logger.debug("Description: " + description);
         		}
         		
         		if(title == null || title.length() == 0)
         		{
         			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"A title of at least 1 character must be supplied");
         			return;
         		}
         		
         		if(description != null && description.length() > 64)
         		{
         			response.sendError(HttpServletResponse.SC_BAD_REQUEST,"The description cannot exceed 64 characters in length.");
         			return;
         		}
         		
         		Forum forum = new Forum();
         		
         		if(id != null)
         			forum.setId(id);
         		
         		forum.setTitle(title);
         		forum.setDescription(description);
         		forum.setSiteId(siteId);
         		forum.setCreatorId(sakaiProxy.getCurrentUser().getId());
         		forum.setLockedForWriting(lockWriting);
         		forum.setLockedForReading(lockReading);
         		
         		if(startDate != null && startDate.length() > 0
         				&& endDate != null && endDate.length() > 0)
         		{
         			try
         			{
         				long start = Long.parseLong(startDate);
         				long end = Long.parseLong(endDate);
         				if(start > 0L && end > 0L)
         				{
         					if(end <= start)
         					{
         						throw new IllegalArgumentException("The end date MUST come after the start date.");
         					}
         					else
         					{
         						forum.setStart(start);
         						forum.setEnd(end);
         					}
         				}
         			}
         			catch(NumberFormatException pe)
         			{
         				throw new IllegalArgumentException("The start and end dates MUST be supplied in millisecond format.");
         			}
         		}
 
         		if(yaftForumService.addOrUpdateForum(forum))
         		{
         			response.setStatus(HttpServletResponse.SC_OK);
         			response.setContentType("text/plain");
         			response.getWriter().write("success");
         			response.getWriter().close();
         			return;
         		}
         		else
         		{
         			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         			return;
         		}
             }
             catch (Exception e)
             {
                 logger.error("Caught exception whilst creating forum.", e);
                 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                 return;
             }
         }
 	}
 	
 	private void doDiscussionsPost(HttpServletRequest request, HttpServletResponse response, String[] parts) throws ServletException, IOException
 	{
 		String siteId = sakaiProxy.getCurrentSiteId();
 
         if(parts.length == 1)
         {
         	String id = (String) request.getParameter("id");
         	String subject = (String) request.getParameter("subject");
         	String content = (String) request.getParameter("content");
         	String forumId = (String) request.getParameter("forumId");
         	String startDate= (String) request.getParameter("startDate");
         	String endDate= (String) request.getParameter("endDate");
         	String lockWritingString = (String) request.getParameter("lockWriting");
         	String lockReadingString = (String) request.getParameter("lockReading");
         	
         	if(subject == null || subject.length() <= 0
         		|| content == null || content.length() <= 0)
         	{
         		response.setStatus(HttpServletResponse.SC_OK);
         		response.setContentType("text/html");
         		response.getWriter().write("ERROR:A subject and content must be supplied.");
         		response.getWriter().close();
         		return;
         	}
 		
         	boolean lockWriting = true;
         	boolean lockReading = true;
 		
         	if(lockWritingString != null)
         		lockWriting = lockWritingString.equals("true");
         	else
         		lockWriting = false;
 		
         	if(lockReadingString != null)
         		lockReading = lockReadingString.equals("true");
         	else
         		lockReading = false;
 
         	if (logger.isDebugEnabled())
         	{
         		logger.debug("Subject: " + subject);
         		logger.debug("Content: " + content);
         		logger.debug("Forum ID: " + forumId);
         	}
 
         	Message message = new Message();
 		
         	if(id != null)
         		message.setId(id);
 		
         	String currentUserId = sakaiProxy.getCurrentUser().getId();
 		
         	message.setSubject(subject);
         	message.setContent(content);
         	message.setSiteId(siteId);
         	message.setCreatorId(currentUserId);
         	message.setCreatorDisplayName(sakaiProxy.getDisplayNameForUser(currentUserId));
         	message.setAttachments(getAttachments(request));
 		
         	// The first messages in discussions always have the same id as the
         	// discussion
         	message.setDiscussionId(message.getId());
 		
         	message.setStatus("READY");
 		
         	Discussion discussion = new Discussion();
         	discussion.setFirstMessage(message);
         	discussion.setLockedForWriting(lockWriting);
         	discussion.setLockedForReading(lockReading);
 		
         	if(startDate != null && startDate.length() > 0
         			&& endDate != null && endDate.length() > 0)
         	{	
         		try
         		{
         			long start = Long.parseLong(startDate);
         			long end = Long.parseLong(endDate);
         			if(start > 0L && end > 0L)
         			{
         				if(end <= start)
         				{
         					response.setStatus(HttpServletResponse.SC_OK);
         					response.setContentType("text/html");
         					response.getWriter().write("ERROR:The end date MUST come after the start date.");
         					response.getWriter().close();
         					return;
         				}
         				else
         				{
         					discussion.setStart(start);
         					discussion.setEnd(end);
         				}
         			}
         		}
         		catch(NumberFormatException pe)
         		{
         			response.setStatus(HttpServletResponse.SC_OK);
         			response.setContentType("text/html");
         			response.getWriter().write("ERROR:The start and end dates MUST be supplied in millisecond format.");
         			response.getWriter().close();
         			return;
         		}
         	}
 
         	if(yaftForumService.addDiscussion(siteId, forumId, discussion, true) != null)
         	{
         		response.setStatus(HttpServletResponse.SC_OK);
         		response.setContentType("text/html");
         		response.getWriter().write(discussion.getId());
         		response.getWriter().close();
         		return;
         	}
         	else
         	{
         		response.setStatus(HttpServletResponse.SC_OK);
         		response.setContentType("text/html");
         		response.getWriter().write("ERROR");
         		response.getWriter().close();
         		return;
         	}
         }
 	}
 	
 	private void doMessagesPost(HttpServletRequest request, HttpServletResponse response, String[] parts) throws ServletException, IOException
 	{
 		String siteId = sakaiProxy.getCurrentSiteId();
 
         if(parts.length == 1)
         {
         	String status = (String) request.getParameter("status");
     		String subject = (String) request.getParameter("subject");
     		String content = (String) request.getParameter("content");
     		String forumId = (String) request.getParameter("forumId");
     		String viewMode = (String) request.getParameter("viewMode");
     		String messageId = (String) request.getParameter("messageId");
     		String messageBeingRepliedTo = (String) request.getParameter("messageBeingRepliedTo");
     		String discussionId = (String) request.getParameter("discussionId");
 
     		if (logger.isDebugEnabled())
     		{
     			logger.debug("Status: " + status);
     			logger.debug("Subject: " + subject);
     			logger.debug("Content: " + content);
     			logger.debug("Forum ID: " + forumId);
     			logger.debug("View Mode: " + viewMode);
     			logger.debug("Discussion ID: " + discussionId);
     			logger.debug("Message Being Replied To: " + messageBeingRepliedTo);
     		}
     		
     		if(subject == null || subject.length() <= 0)
     			throw new IllegalArgumentException("You must supply a subject.");
     		
     		if(content == null || content.length() <= 0)
     			throw new IllegalArgumentException("You must supply some content.");
     		
     		if(viewMode == null || viewMode.length() <= 0)
     			viewMode = "full";
     		
     		String currentUserId = sakaiProxy.getCurrentUser().getId();
     		
     		Message message = new Message();
     		message.setStatus(status);
     		message.setSubject(subject);
     		message.setContent(content);
     		message.setSiteId(siteId);
     		message.setCreatorId(currentUserId);
     		message.setCreatorDisplayName(sakaiProxy.getDisplayNameForUser(currentUserId));
     		message.setDiscussionId(discussionId);
     		message.setAttachments(getAttachments(request));
     		
     		// If no message id has been supplied this must be a new message, so
     		// we set the message id to empty
     		if(messageId == null)
     			message.setId("");
 
     		if (messageBeingRepliedTo != null && messageBeingRepliedTo.length() > 0)
     			message.setParent(messageBeingRepliedTo);
     		else if (messageBeingRepliedTo == null || messageBeingRepliedTo.length() <= 0)
     		{
     			// This is a discussion, or top level message
     			message.setId(messageId);
     		}
     		
     		if(yaftForumService.addOrUpdateMessage(siteId, forumId, message, true))
     		{
         		response.setStatus(HttpServletResponse.SC_OK);
         		response.setContentType("text/plain");
         		response.getWriter().write("success");
         		response.getWriter().close();
         		return;
     		}
     		else
     		{
     			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
     			return;
     		}
         }
 	}
 	
 	private List<Attachment> getAttachments(HttpServletRequest request)
 	{
 		List<FileItem> fileItems = new ArrayList<FileItem>();
 
 		String uploadsDone = (String) request.getAttribute(RequestFilter.ATTR_UPLOADS_DONE);
 
 		if (uploadsDone != null && uploadsDone.equals(RequestFilter.ATTR_UPLOADS_DONE))
 		{
 			logger.debug("UPLOAD STATUS: " + request.getAttribute("upload.status"));
 
 			try
 			{
 				FileItem attachment1 = (FileItem) request.getAttribute("attachment_0");
 				if (attachment1 != null && attachment1.getSize() > 0)
 					fileItems.add(attachment1);
 				FileItem attachment2 = (FileItem) request.getAttribute("attachment_1");
 				if (attachment2 != null && attachment2.getSize() > 0)
 					fileItems.add(attachment2);
 				FileItem attachment3 = (FileItem) request.getAttribute("attachment_2");
 				if (attachment3 != null && attachment3.getSize() > 0)
 					fileItems.add(attachment3);
 				FileItem attachment4 = (FileItem) request.getAttribute("attachment_3");
 				if (attachment4 != null && attachment4.getSize() > 0)
 					fileItems.add(attachment4);
 				FileItem attachment5 = (FileItem) request.getAttribute("attachment_4");
 				if (attachment5 != null && attachment5.getSize() > 0)
 					fileItems.add(attachment5);
 			}
 			catch (Exception e)
 			{
 
 			}
 		}
 		
 		List<Attachment> attachments = new ArrayList<Attachment>();
 		if (fileItems.size() > 0)
 		{
 		for (Iterator i = fileItems.iterator(); i.hasNext();)
 		{
 			FileItem fileItem = (FileItem) i.next();
 
 			String name = fileItem.getName();
 
 			if (name.contains("/"))
 				name = name.substring(name.lastIndexOf("/") + 1);
 			else if (name.contains("\\"))
 				name = name.substring(name.lastIndexOf("\\") + 1);
 
 			attachments.add(new Attachment(name, fileItem.getContentType(), fileItem.get()));
 		}
 		}
 		
 		return attachments;
 	}
 
 	/**
 	 * Sets up the YaftForumService and SakaiProxy instances
 	 */
 	public void init(ServletConfig config) throws ServletException
 	{
 		super.init(config);
 
 		if (logger.isDebugEnabled()) logger.debug("init");
 
 		try
 		{
 			ComponentManager componentManager = org.sakaiproject.component.cover.ComponentManager.getInstance();
 			yaftForumService = (YaftForumService) componentManager.get(YaftForumService.class);
 			sakaiProxy = yaftForumService.getSakaiProxy();
 		}
 		catch (Throwable t)
 		{
 			throw new ServletException("Failed to initialise YaftTool servlet.", t);
 		}
 	}
 }
