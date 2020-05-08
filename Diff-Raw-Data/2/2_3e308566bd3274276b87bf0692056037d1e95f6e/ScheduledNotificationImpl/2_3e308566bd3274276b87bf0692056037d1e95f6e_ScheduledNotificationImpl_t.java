 /**********************************************************************************
  * $URL: 
  * $Id: 
  ***********************************************************************************
  *
  * Copyright (c) 2007 The Sakai Foundation.
  * 
  * Licensed under the Educational Community License, Version 1.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  * 
  *      http://www.opensource.org/licenses/ecl1.php
  * 
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  *
  **********************************************************************************/
 package org.sakaiproject.assignment2.logic.impl;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.logic.AssignmentBundleLogic;
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.ScheduledNotification;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentGroup;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.SubmissionAttachment;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.authz.api.AuthzGroupService;
 import org.sakaiproject.authz.api.GroupNotDefinedException;
 import org.sakaiproject.authz.api.Member;
 import org.sakaiproject.authz.api.SecurityService;
 import org.sakaiproject.component.api.ServerConfigurationService;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.email.api.DigestService;
 import org.sakaiproject.email.api.EmailService;
 import org.sakaiproject.entity.api.Entity;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.TypeException;
 import org.sakaiproject.time.api.Time;
 import org.sakaiproject.time.api.TimeService;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.api.UserDirectoryService;
 import org.sakaiproject.user.api.UserNotDefinedException;
 import org.sakaiproject.util.StringUtil;
 import org.sakaiproject.util.Validator;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.SiteService;
 
 public class ScheduledNotificationImpl implements ScheduledNotification
 {
 	String relativeAccessPoint = null;
 
 	private static final String MULTIPART_BOUNDARY = "======sakai-multi-part-boundary======";
 
 	private static final String MIME_ADVISORY = "This message is for MIME-compliant mail readers.";
 
 	private static final String BOUNDARY_LINE = "\n\n--" + MULTIPART_BOUNDARY + "\n";
 
 	private static final String PLAIN_TEXT_HEADERS = "Content-Type: text/plain\n\n";
 
 	private static final String HTML_HEADERS = "Content-Type: text/html\n\n";
 
 	private static final String HTML_END = "\n  </body>\n</html>\n";
 
 	private static final String TERMINATION_LINE = "\n\n--" + MULTIPART_BOUNDARY
 			+ "--\n\n";
 
 	/** This string starts the references to resources in this service. */
 	public static final String REFERENCE_ROOT = "/assignment";
 
 	private AssignmentLogic assignmentLogic;
 
 	public void setAssignmentLogic(AssignmentLogic assignmentLogic)
 	{
 		this.assignmentLogic = assignmentLogic;
 	}
 
 	private SiteService siteService;
 
 	public void setSiteService(SiteService siteService)
 	{
 		this.siteService = siteService;
 	}
 
 	private UserDirectoryService userDirectoryService;
 
 	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
 	{
 		this.userDirectoryService = userDirectoryService;
 	}
 
 	private EmailService emailService;
 
 	public void setEmailService(EmailService emailService)
 	{
 		this.emailService = emailService;
 	}
 
 	private AssignmentBundleLogic assignmentBundleLogic;
 
 	public void setAssignmentBundleLogic(AssignmentBundleLogic assignmentBundleLogic)
 	{
 		this.assignmentBundleLogic = assignmentBundleLogic;
 	}
 
 	private ServerConfigurationService serverConfigurationService;
 
 	public void setServerConfigurationService(
 			ServerConfigurationService serverConfigurationService)
 	{
 		this.serverConfigurationService = serverConfigurationService;
 	}
 
 	private ContentHostingService contentHostingService;
 
 	public void setContentHostingService(ContentHostingService contentHostingService)
 	{
 		this.contentHostingService = contentHostingService;
 	}
 
 	private SecurityService securityService;
 
 	public void setSecurityService(SecurityService securityService)
 	{
 		this.securityService = securityService;
 	}
 
 	private TimeService timeService;
 
 	public void setTimeService(TimeService timeService)
 	{
 		this.timeService = timeService;
 	}
 
 	private DigestService digestService;
 
 	public void setDigestService(DigestService digestService)
 	{
 		this.digestService = digestService;
 	}
 
 	private AuthzGroupService authzGroupService;
 
	public void setAuthzGroupService(AuthzGroupService authzGroupService)
 	{
 		this.authzGroupService = authzGroupService;
 	}
 
 	private static final Log log = LogFactory.getLog(ScheduledNotificationImpl.class);
 
 	/**
 	 * Final initialization, once all dependencies are set.
 	 */
 	public void init()
 	{
 		relativeAccessPoint = REFERENCE_ROOT;
 		log.info("init()");
 	}
 
 	public void execute(String opaqueContext)
 	{
 		Assignment2 assignment = assignmentLogic.getAssignmentByIdWithGroups(new Long(
 				opaqueContext));
 
 		String context = assignment.getContextId();
 		Set<AssignmentGroup> groups = assignment.getAssignmentGroupSet();
 
 		// Send to all members of the groups associated with the assignment unless there are no groups,
 		// then we send to all site members
 		if (groups != null && !groups.isEmpty())
 			for (AssignmentGroup group : groups)
 			{
 				context = group.getGroupId();
 				try
 				{
 					for (Object u : authzGroupService.getAuthzGroup(context).getMembers())
 					{
 						Member user = (Member) u;
 						notifyUser(user.getUserId(), assignment);
 					}
 				}
 				catch (GroupNotDefinedException e)
 				{
 					log.error(e);
 				}
 			}
 		else
 		{
 			try
 			{
 				for (Object u : siteService.getSite(context).getUsers())
 				{
 					String userId = (String) u;
 					notifyUser(userId, assignment);
 				}
 			}
 			catch (IdUnusedException e)
 			{
 				log.error(e);
 			}
 		}
 
 	}
 
 	private void notifyUser(String userId, Assignment2 assignment)
 	{
 		try
 		{
 
 			User user = userDirectoryService.getUser(userId);
 			if (StringUtil.trimToNull(user.getEmail()) != null)
 			{
 				ArrayList<User> receivers = new ArrayList<User>();
 				receivers.add(user);
 
 				emailService.sendToUsers(receivers, buildNotificationHeaders(user
 						.getEmail(), "noti.subject.post.content"),
 						buildPostNotificationMessage(assignment));
 			}
 		}
 		catch (IdUnusedException e)
 		{
 			log.error(e);
 		}
 		catch (UserNotDefinedException e)
 		{
 			log.error(e);
 		}
 	}
 
 	public void notifyStudentThatSubmissionWasAccepted(AssignmentSubmission s)
 			throws IdUnusedException, UserNotDefinedException, PermissionException,
 			TypeException
 	{
 		if (serverConfigurationService.getBoolean(
 				"assignment.submission.confirmation.email", true))
 		{
 			// send notification
 			User u = userDirectoryService.getCurrentUser();
 
 			if (StringUtil.trimToNull(u.getEmail()) != null)
 			{
 				ArrayList<User> receivers = new ArrayList<User>();
 				receivers.add(u);
 
 				emailService.sendToUsers(receivers, buildNotificationHeaders(
 						u.getEmail(), "noti.subject.submission.content"),
 						buildSubmissionNotificationMessage(s));
 			}
 		}
 	}
 
 	private List<String> buildNotificationHeaders(String receiverEmail,
 			String subjectMessageId)
 	{
 		ArrayList<String> rv = new ArrayList<String>();
 
 		rv.add("MIME-Version: 1.0");
 		rv.add("Content-Type: multipart/alternative; boundary=\"" + MULTIPART_BOUNDARY
 				+ "\"");
 		// set the subject
 		rv.add(buildSubject(subjectMessageId));
 
 		// from
 		rv.add(buildFrom());
 
 		// to
 		if (StringUtil.trimToNull(receiverEmail) != null) rv.add("To: " + receiverEmail);
 
 		return rv;
 	}
 
 	private String buildSubject(String messageId)
 	{
 		return assignmentBundleLogic.getString("noti.subject.label") + " "
 				+ assignmentBundleLogic.getString(messageId);
 	}
 
 	private String buildFrom()
 	{
 		return "From: " + "\""
 				+ serverConfigurationService.getString("ui.service", "Sakai")
 				+ "\"<no-reply@" + serverConfigurationService.getServerName() + ">";
 	}
 
 	private String buildPostNotificationMessage(Assignment2 assignment)
 			throws IdUnusedException
 	{
 		StringBuilder message = new StringBuilder();
 		message.append(MIME_ADVISORY);
 		message.append(BOUNDARY_LINE);
 		message.append(PLAIN_TEXT_HEADERS);
 		message.append(plainTextContent(assignment));
 		message.append(BOUNDARY_LINE);
 		message.append(HTML_HEADERS);
 		message.append(htmlPreamble("noti.subject.post.content"));
 		message.append(htmlContent(assignment));
 		message.append(HTML_END);
 		message.append(TERMINATION_LINE);
 		return message.toString();
 	}
 
 	/**
 	 * Get the message for the email.
 	 * 
 	 * @param event
 	 *        The event that matched criteria to cause the notification.
 	 * @return the message for the email.
 	 */
 	private String buildSubmissionNotificationMessage(AssignmentSubmission s)
 			throws IdUnusedException, UserNotDefinedException, PermissionException,
 			TypeException
 	{
 		StringBuilder message = new StringBuilder();
 		message.append(MIME_ADVISORY);
 		message.append(BOUNDARY_LINE);
 		message.append(PLAIN_TEXT_HEADERS);
 		message.append(plainTextContent(s));
 		message.append(BOUNDARY_LINE);
 		message.append(HTML_HEADERS);
 		message.append(htmlPreamble("noti.subject.submission.content"));
 		message.append(htmlContent(s));
 		message.append(HTML_END);
 		message.append(TERMINATION_LINE);
 		return message.toString();
 	}
 
 	private List<User> allowReceiveSubmissionNotificationUsers(String context)
 	{
 		String resourceString = getAccessPoint(true) + Entity.SEPARATOR + "a"
 				+ Entity.SEPARATOR + context + Entity.SEPARATOR;
 		if (log.isDebugEnabled())
 		{
 			log
 					.debug("Entering allowReceiveSubmissionNotificationUsers with resource string : "
 							+ resourceString + "; context string : " + context);
 		}
 		List<User> users = securityService.unlockUsers(
 				AssignmentConstants.SECURE_ASSIGNMENT_RECEIVE_NOTIFICATIONS,
 				resourceString);
 		return users;
 	}
 
 	/**
 	 * Access the partial URL that forms the root of resource URLs.
 	 * 
 	 * @param relative -
 	 *        if true, form within the access path only (i.e. starting with
 	 *        /msg)
 	 * @return the partial URL that forms the root of resource URLs.
 	 */
 	private String getAccessPoint(boolean relative)
 	{
 		return (relative ? "" : serverConfigurationService.getAccessUrl())
 				+ relativeAccessPoint;
 	}
 
 	private String plainTextContent(AssignmentSubmission s) throws IdUnusedException,
 			UserNotDefinedException, PermissionException, IdUnusedException,
 			TypeException
 	{
 		return htmlContent(s);
 	}
 
 	private String plainTextContent(Assignment2 a) throws IdUnusedException
 	{
 		return htmlContent(a);
 	}
 
 	private String htmlPreamble(String messageId)
 	{
 		StringBuilder buf = new StringBuilder();
 		buf.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n");
 		buf.append("    \"http://www.w3.org/TR/html4/loose.dtd\">\n");
 		buf.append("<html>\n");
 		buf.append("  <head><title>");
 		buf.append(buildSubject(messageId));
 		buf.append("</title></head>\n");
 		buf.append("  <body>\n");
 		return buf.toString();
 	}
 
 	private String htmlContent(AssignmentSubmission s) throws IdUnusedException,
 			UserNotDefinedException, PermissionException, TypeException
 	{
 		String newline = "<br />\n";
 
 		Assignment2 a = s.getAssignment();
 
 		String context = a.getContextId();
 
 		Site site = siteService.getSite(context);
 		String siteTitle = site.getTitle();
 		String siteId = site.getId();
 
 		StringBuffer content = new StringBuffer();
 		// site title and id
 		content.append(assignmentBundleLogic.getString("noti.site.title") + " "
 				+ siteTitle + newline);
 		content.append(assignmentBundleLogic.getString("noti.site.id") + " " + siteId
 				+ newline + newline);
 		// assignment title and due date
 		content.append(assignmentBundleLogic.getString("noti.assignment") + " "
 				+ a.getTitle() + newline);
 		Time time = timeService.newTime();
 		if (a.getDueDate() != null)
 		{
 			time.setTime(a.getDueDate().getTime());
 			content.append(assignmentBundleLogic.getString("noti.assignment.duedate")
 					+ " " + time.toStringLocalFull() + newline + newline);
 		}
 		// submitter name and id
 		AssignmentSubmissionVersion curSubVers = s.getCurrentSubmissionVersion();
 		String submitterId = curSubVers.getCreatedBy();
 		User submitter = userDirectoryService.getUser(submitterId);
 		content.append(assignmentBundleLogic.getString("noti.student") + " "
 				+ submitter.getDisplayName());
 		content.append("( " + submitter.getDisplayId() + " )");
 		content.append(newline + newline);
 
 		// submit time
 		content.append(assignmentBundleLogic.getString("noti.submit.id") + " "
 				+ s.getId() + newline);
 
 		// submit time
 		time.setTime(curSubVers.getSubmittedTime().getTime());
 		content.append(assignmentBundleLogic.getString("noti.submit.time") + " "
 				+ time.toStringLocalFull() + newline + newline);
 
 		// submit text
 		String text = StringUtil.trimToNull(curSubVers.getSubmittedText());
 		if (text != null)
 		{
 			content.append(assignmentBundleLogic.getString("noti.submit.text") + newline
 					+ newline + Validator.escapeHtmlFormattedText(text) + newline
 					+ newline);
 		}
 
 		// attachment if any
 		Set<SubmissionAttachment> attachments = curSubVers.getSubmissionAttachSet();
 		if (attachments != null && attachments.size() > 0)
 		{
 			content.append(assignmentBundleLogic.getString("noti.submit.attachments")
 					+ newline + newline);
 			for (SubmissionAttachment attachment : attachments)
 			{
 				String ref = attachment.getAttachmentReference();
 				ContentResource res = contentHostingService.getResource(ref);
 				ResourceProperties resProps = res.getProperties();
 				content.append(resProps.getProperty(ResourceProperties.PROP_DISPLAY_NAME)
 						+ "(" + res.getContentLength() + ")\n");
 			}
 		}
 
 		return content.toString();
 	}
 
 	private String htmlContent(Assignment2 a) throws IdUnusedException
 	{
 		String newline = "<br />\n";
 
 		String context = a.getContextId();
 
 		Site site = siteService.getSite(context);
 		String siteTitle = site.getTitle();
 		String siteId = site.getId();
 
 		StringBuilder content = new StringBuilder();
 		// site title and id
 		content.append(assignmentBundleLogic.getString("noti.site.title") + " "
 				+ siteTitle + newline);
 		content.append(assignmentBundleLogic.getString("noti.site.id") + " " + siteId
 				+ newline + newline);
 		// assignment title and due date
 		content.append(assignmentBundleLogic.getString("noti.assignment") + " "
 				+ a.getTitle() + newline);
 		Time time = timeService.newTime();
 		if (a.getDueDate() != null)
 		{
 			time.setTime(a.getDueDate().getTime());
 			content.append(assignmentBundleLogic.getString("noti.assignment.duedate")
 					+ " " + time.toStringLocalFull() + newline + newline);
 		}
 
 		return content.toString();
 	}
 
 	public void notifyInstructorsOfSubmission(AssignmentSubmission s, Assignment2 a)
 			throws IdUnusedException, UserNotDefinedException, PermissionException,
 			IdUnusedException, TypeException
 	{
 		int notiType = a.getNotificationType();
 		if (notiType != AssignmentConstants.NOTIFY_NONE)
 		{
 			// need to send notification email
 			String context = a.getContextId();
 
 			// compare the list of users with the receive.notifications and list
 			// of users who can
 			// actually grade this assignment
 			List<User> receivers = allowReceiveSubmissionNotificationUsers(context);
 
 			// filter out users who's not able to grade this submission
 			ArrayList<User> finalReceivers = new ArrayList<User>();
 
 			HashSet<String> receiverSet = new HashSet<String>();
 			Set<AssignmentGroup> groups = a.getAssignmentGroupSet();
 			if (groups.size() > 0)
 			{
 				for (AssignmentGroup g : groups)
 				{
 					try
 					{
 						for (User rUser : receivers)
 						{
 							String rUserId = rUser.getId();
 							if (!receiverSet.contains(rUserId))
 							{
 								finalReceivers.add(rUser);
 								receiverSet.add(rUserId);
 							}
 						}
 					}
 					catch (Exception e)
 					{
 						log.warn("notificationToInstructors, group id =" + g);
 					}
 				}
 			}
 			else
 			{
 				finalReceivers.addAll(receivers);
 			}
 
 			String messageBody = buildSubmissionNotificationMessage(s);
 
 			if (notiType == AssignmentConstants.NOTIFY_FOR_EACH)
 			{
 				// send the message immediately
 				emailService.sendToUsers(finalReceivers, buildNotificationHeaders(null,
 						"noti.subject.submission.content"), messageBody);
 			}
 			else if (notiType == AssignmentConstants.NOTIFY_DAILY_SUMMARY)
 			{
 				// digest the message to each user
 				for (User user : finalReceivers)
 				{
 					digestService.digest(user.getId(),
 							buildSubject("noti.subject.submission.content"), messageBody);
 				}
 			}
 		}
 	}
 }
