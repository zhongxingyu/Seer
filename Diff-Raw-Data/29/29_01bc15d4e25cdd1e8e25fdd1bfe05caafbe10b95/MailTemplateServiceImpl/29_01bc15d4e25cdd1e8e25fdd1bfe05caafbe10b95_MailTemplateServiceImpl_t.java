 package org.jasig.portlet.blackboardvcportlet.service.impl;
 
 import net.fortuna.ical4j.data.CalendarOutputter;
 import net.fortuna.ical4j.model.Calendar;
 import net.fortuna.ical4j.model.DateTime;
 import net.fortuna.ical4j.model.component.VEvent;
 import net.fortuna.ical4j.model.property.Description;
 import net.fortuna.ical4j.model.property.Location;
 import net.fortuna.ical4j.model.property.Status;
 import net.fortuna.ical4j.model.property.Uid;
 
 import org.apache.velocity.app.VelocityEngine;
 import org.jasig.portlet.blackboardvcportlet.data.ConferenceUser;
 import org.jasig.portlet.blackboardvcportlet.data.Session;
 import org.jasig.portlet.blackboardvcportlet.service.MailTask;
 import org.jasig.portlet.blackboardvcportlet.service.MailTemplateService;
 import org.jasig.portlet.blackboardvcportlet.service.SessionService;
 import org.jasig.portlet.blackboardvcportlet.service.util.MailMessages;
 import org.jasig.portlet.blackboardvcportlet.service.util.MailSubstitutions;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.core.io.ByteArrayResource;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.springframework.mail.javamail.MimeMessagePreparator;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 import org.springframework.ui.velocity.VelocityEngineUtils;
 import javax.mail.internet.MimeMessage;
 
 import java.io.ByteArrayOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 @Service("jasigMailTemplateService")
 public class MailTemplateServiceImpl implements BeanFactoryAware, MailTemplateService
 {
     protected Logger logger = LoggerFactory.getLogger(getClass());
 
 	final private Queue<MailTask> theQueue = new ConcurrentLinkedQueue<MailTask>();
 	private JavaMailSender mailSender;
 	@SuppressWarnings("unused")
 	private BeanFactory beanFactory;
 	private VelocityEngine velocityEngine;
 	private SessionService sessionService;
 	
 	private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm";
 
 	private String defaultFromAddress;
 	
 	@Value("${mail.sendMail}")
 	private String sendMail;
 
 	@Value("${mail.extParticipantMailMessage.subject}")
 	private String externalParticipantSubject;
 	@Value("${mail.intParticipantMailMessage.subject}")
 	private String internalParticipantSubject;
 	@Value("${mail.moderatorMailMessage.subject}")
 	private String moderatorSubject;
 	@Value("${mail.sessionDeletionMessage.subject}")
 	private String sessionDeletionSubject;
 
 	/**
 	 * Default Constructor
 	 */
 	public MailTemplateServiceImpl()
 	{
 		super();
 	}
 	
 	@Value("${mail.from}")
 	public void setFrom(String from) {
 		this.defaultFromAddress = from;
 	}
 	
 	@Autowired
 	public void setSessionService(SessionService service) {
 		this.sessionService = service;
 	}
 
 	@Autowired
 	public void setBeanFactory(BeanFactory bf) throws BeansException
 	{
 		beanFactory = bf;
 	}
 
 	@Autowired
 	public void setJavaMailSender(JavaMailSender ms)
 	{
 		this.mailSender = ms;
 	}
 
 	@Autowired
 	public void setVelocityEngine(VelocityEngine velocityEngine)
 	{
 		this.velocityEngine = velocityEngine;
 	}
 
 	/**
 	 * Clear the queue every 1 second after last completion
 	 */
 	@Scheduled(fixedDelay = 1000)
 	private void clearQueue()
 	{
 		MailTask cur = theQueue.poll();
 		while (cur != null)
 		{
 			sendMail(cur);
 			cur = theQueue.poll();
 		}
 	}
 
 	public void sendMail(final MailTask mt)
 	{
 		try
 		{
 			MimeMessagePreparator messagePreparator = new MimeMessagePreparator()
 			{
 				public void prepare(MimeMessage mimeMessage) throws Exception
 				{
 					MimeMessageHelper message = new MimeMessageHelper(mimeMessage,true);
 					
 					if(mt.getMeetingInvite() != null) {
 						CalendarOutputter outputter = new CalendarOutputter();
 						ByteArrayOutputStream os = new ByteArrayOutputStream();
 						outputter.setValidating(false);
 						outputter.output(mt.getMeetingInvite(), os);
 						
 						message.addAttachment("invite.ics", new ByteArrayResource(os.toByteArray()));
 					}
 					
 					message.setFrom(mt.getFrom() != null ? mt.getFrom() : defaultFromAddress);
 					
 					if (mt.getTo() != null) {
 						String[] toArray = mt.getTo().toArray(new String[mt.getTo().size()]);
 						message.setTo(toArray);
 					}
 					if (mt.getSubject() != null) {
 						message.setSubject(mt.getSubject());
 					}
 					else
 					{
 						switch (mt.getTemplate())
 						{
 							case MODERATOR:
 								message.setSubject(moderatorSubject);
 								break;
 							case INTERNAL_PARTICIPANT:
 								message.setSubject(internalParticipantSubject);
 								break;
 							case EXTERNAL_PARTICIPANT:
 								message.setSubject(externalParticipantSubject);
 								break;
 							case SESSION_DELETION:
 								message.setSubject(sessionDeletionSubject);
 								break;
 							default:
 								message.setSubject("");
 						}
 					}
 
 					message.setText(buildEmailMessage(mt), false);
 				}
 			};
 			mailSender.send(messagePreparator);
 		}
 		catch (Exception e)
 		{
 			logger.error("Issue with sending email", e);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public String buildEmailMessage(final MailTask mailTask)
 	{
 		return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, mailTask.getTemplate().getClassPathToTemplate(), "UTF-8", mailTask.getSubstitutions());
 	}
 
 	@Override
 	public void sendEmail(MailTask mailTask) {
 		if("true".equalsIgnoreCase(sendMail))
 			theQueue.add(mailTask);
 	}
 	
 	public void buildAndSendSessionEmails(Session session, boolean isUpdate) {
 		
 		for(ConferenceUser moderator : sessionService.getSessionChairs(session)) {
 			this.sendEmail(buildModeratorMailTask(moderator, session, isUpdate));
 		}
 		
 		for(ConferenceUser user : sessionService.getSessionNonChairs(session)) {
 			this.sendEmail(buildParticipantMailTask(user, session, isUpdate));
 		}
 	}
 	
 	//build substitutions for moderator
 	public MailTask buildModeratorMailTask(ConferenceUser moderator, Session session, boolean isUpdate) {
 		List<String> emailList = new ArrayList<String>();
 		emailList.add(moderator.getEmail());
 		String userSessionUrl = sessionService.getOrCreateSessionUrl(moderator, session);
 		
 		//substitutions
 		Map<String, String> substitutions = new HashMap<String, String>();
 		
 		substitutions.put(MailSubstitutions.DISPLAY_NAME.toString(), moderator.getDisplayName());
 		substitutions.put(MailSubstitutions.SESSION_NAME.toString(), session.getSessionName());
 		substitutions.put(MailSubstitutions.SESSION_TYPE.toString(), session.getAccessType().getName());
 		substitutions.put(MailSubstitutions.SESSION_START_TIME.toString(), session.getStartTime().toString(DATE_FORMAT));
 		substitutions.put(MailSubstitutions.SESSION_END_TIME.toString(), session.getEndTime().toString(DATE_FORMAT));
 		substitutions.put(MailSubstitutions.SESSION_USER_URL.toString(), userSessionUrl);
 		substitutions.put(MailSubstitutions.SESSION_GUEST_URL.toString(), session.getGuestUrl());
 		substitutions.put(MailSubstitutions.SESSION_UPDATE_TEXT.toString(), isUpdate ? "**Time update for existing session.**" : "");
		substitutions.put(MailSubstitutions.SESSION_BOUNDARY_TIME.toString(), String.valueOf(session.getBoundaryTime()));
 		
 		MailTask mt = new MailTask(emailList,substitutions,MailMessages.MODERATOR);
 		mt.setMeetingInvite(buildIcsFile(session, moderator));
 		return mt;
 	}
 		
 	//build substitutions for participant
 	public MailTask buildParticipantMailTask(ConferenceUser participant, Session session, boolean isUpdate) {
 		List<String> emailList = new ArrayList<String>();
 		emailList.add(participant.getEmail());
 		
 		//substitutions
 		Map<String, String> substitutions = new HashMap<String, String>();
 		
 		substitutions.put(MailSubstitutions.DISPLAY_NAME.toString(), participant.getDisplayName());
 		substitutions.put(MailSubstitutions.SESSION_NAME.toString(), session.getSessionName());
 		substitutions.put(MailSubstitutions.SESSION_TYPE.toString(), session.getAccessType().getName());
 		substitutions.put(MailSubstitutions.SESSION_START_TIME.toString(), session.getStartTime().toString(DATE_FORMAT));
 		substitutions.put(MailSubstitutions.SESSION_END_TIME.toString(), session.getEndTime().toString(DATE_FORMAT));
 		substitutions.put(MailSubstitutions.SESSION_USER_URL.toString(), sessionService.getOrCreateSessionUrl(participant, session));
 		substitutions.put(MailSubstitutions.SESSION_GUEST_URL.toString(), session.getGuestUrl());
 		substitutions.put(MailSubstitutions.SESSION_CREATOR_EMAIL.toString(), session.getCreator().getEmail());
 		substitutions.put(MailSubstitutions.SESSION_CREATOR_NAME.toString(), session.getCreator().getDisplayName());
		substitutions.put(MailSubstitutions.SESSION_UPDATE_TEXT.toString(), isUpdate ? "**Time update for existing session.**" : "");
		substitutions.put(MailSubstitutions.SESSION_BOUNDARY_TIME.toString(), String.valueOf(session.getBoundaryTime()));
 		
 		MailTask mt = new MailTask(emailList,substitutions,MailMessages.EXTERNAL_PARTICIPANT);
 		mt.setMeetingInvite(buildIcsFile(session, participant));
 		return mt;
 	}
 	
 	public void buildAndSendCancelationMeetingEmail(Session session) {
 		List <ConferenceUser> users = new ArrayList<ConferenceUser>();
 		
 		users.addAll(sessionService.getSessionChairs(session));
 		users.addAll(sessionService.getSessionNonChairs(session));
 		
 		for(ConferenceUser user : users) {
 			this.sendEmail(buildCancellationNoticeMailTask(user, session));
 		}
 	}
 		
 	//build cancellation notice
 	public MailTask buildCancellationNoticeMailTask(ConferenceUser user, Session session) {
 		List<String> emailList = new ArrayList<String>();
 		emailList.add(user.getEmail());
 		
 		//substitutions
 		Map<String, String> substitutions = new HashMap<String, String>();
 		
 		substitutions.put(MailSubstitutions.DISPLAY_NAME.toString(), user.getDisplayName());
 		substitutions.put(MailSubstitutions.SESSION_NAME.toString(), session.getSessionName());
 		substitutions.put(MailSubstitutions.SESSION_TYPE.toString(), session.getAccessType().getName());
 		substitutions.put(MailSubstitutions.SESSION_START_TIME.toString(), session.getStartTime().toString(DATE_FORMAT));
 		substitutions.put(MailSubstitutions.SESSION_END_TIME.toString(), session.getEndTime().toString(DATE_FORMAT));
 		substitutions.put(MailSubstitutions.SESSION_CREATOR_EMAIL.toString(), session.getCreator().getEmail());
 		substitutions.put(MailSubstitutions.SESSION_CREATOR_NAME.toString(), session.getCreator().getDisplayName());
		substitutions.put(MailSubstitutions.SESSION_BOUNDARY_TIME.toString(), String.valueOf(session.getBoundaryTime()));
 		
 		MailTask mt = new MailTask(emailList,substitutions,MailMessages.SESSION_DELETION);
 		mt.setMeetingInvite(buildIcsFile(session, user, true));
 		return mt;
 	}
 	
 	private Calendar buildIcsFile(Session session, ConferenceUser user) {
 		return buildIcsFile(session, user, false);
 	}
 	
 	private Calendar buildIcsFile(Session session, ConferenceUser user, boolean isCancellation) {
 		//create meeting Invite .ics file
 		String userSessionUrl = sessionService.getOrCreateSessionUrl(user, session);
 		VEvent event = new VEvent(new DateTime(session.getStartTime().toDate()),new DateTime(session.getEndTime().toDate()),session.getSessionName());
 		Calendar cal = new Calendar();
 		event.getProperties().add(new Uid(String.valueOf(session.getSessionId()) + "-BlackboardCollaborate"));
 		if(isCancellation) {
 			event.getProperties().add(Status.VEVENT_CANCELLED);
 		} else {
 			event.getProperties().add(new Location(userSessionUrl));
 			Description desc = new Description("Your join URL: " + userSessionUrl + "\n\nGuest join URL: " + session.getGuestUrl() + "\n\nCreator: " + session.getCreator().getDisplayName() + " (" + session.getCreator().getEmail() + ")");
 			event.getProperties().add(desc);
 		}
 		cal.getComponents().add(event);
 		return cal;
 	}
 }
