 package nl.bhit.mtor.server.timer;
 
 import java.util.List;
 import java.util.Set;
 
 import nl.bhit.mtor.model.MTorMessage;
 import nl.bhit.mtor.model.Project;
 import nl.bhit.mtor.model.Status;
 import nl.bhit.mtor.model.User;
 import nl.bhit.mtor.service.ProjectManager;
 import nl.bhit.mtor.service.MailEngine;
 import nl.bhit.mtor.service.MessageManager;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mail.MailSender;
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.stereotype.Component;
 
 @Component
 public class AlertSender {
 	@Autowired( required = false) 
 private ProjectManager projectManager; 
 
 	@Autowired
 	private MessageManager messageManager;
 	@Autowired
 	MailSender mailSender;
 	@Autowired
 	SimpleMailMessage mailMessage;
 	@Autowired
 	MailEngine mailEngine;
 
 	protected final Log log = LogFactory.getLog(AlertSender.class);
 
 	public void process() {
 		List<Project> projects = projectManager.getAllDistinct();
 
 		for (Project project : projects) {
			if(!project.isMonitoring()){
 				if (!project.hasHeartBeat()) {
 					sendMailToUsers(project);
 				}
 				//TODO (tibi) rewrite code to make it more readable
 				/*if (!project.hasStatus(Status.ERROR)) {
 					sendMailToUsers(project);
 				}*/
 				Set<MTorMessage> currentMessages= project.getMessages();
 				if(!currentMessages.isEmpty()){
 					for (MTorMessage message : currentMessages) {
 						if(!message.isAlertSent()){
 							Status status = message.getStatus();
 							if(status.equals(Status.ERROR) && !message.isResolved()){
 								sendMessageAlert(project, 
 										"An error message has arrived",
 										message.getContent());
 								message.setAlertSent(true);
 								messageManager.save(message);
 							} else if(status.equals(Status.WARN) && !message.isResolved()){
 								sendMessageAlert(project, 
 										"A warning message has arrived",
 										message.getContent());
 								message.setAlertSent(true);
 								messageManager.save(message);
 							} else {
 								sendMessageAlert(project, 
 										"Info",
 										message.getContent());
 								message.setAlertSent(true);
 								messageManager.save(message);
 							}
 						}
 					}						
 				}
 			}
 		}
 	}
 
 	protected void sendMailToUsers(Project project) {
 		for (User user : project.getUsers()) {
 			sendHeartBeatAlert(user.getEmail());
 		}
 	}
 
 	private void sendHeartBeatAlert(String to) {
 		if (log.isDebugEnabled()) {
 			log.debug("sending e-mail to user [" + to + "]...");
 		}
 
 		mailMessage.setTo(to + "<" + to + ">");
 
 		mailMessage.setSubject("there is no heartBeat!");
 		mailMessage.setText("there is no heartBeat!");
 		mailEngine.send(mailMessage);
 	}
 	
 	private void sendMessageAlert(Project project, String subject, String content){
 		for (User user : project.getUsers()) {
 			String to = user.getEmail();
 			if (log.isDebugEnabled()) {
 				log.debug("sending e-mail to user [" + to + "]...");
 			}
 
 			mailMessage.setTo(to + "<" + to + ">");
 
 			mailMessage.setSubject(subject);
 			mailMessage.setText(content);
 			mailEngine.send(mailMessage);
 		}
 	}
 }
