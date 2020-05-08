 package ca.todoist.email;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import ca.todoist.parse.Link;
 import ca.todoist.util.LoadProperties;
 
 public class SendMail {
 
 	private static final String DEFAULT_PROJECT_NAME = "default";
 	private static Session session;
 
 	private final String username;
 	private final String password;
	private final Map<String, String> todoistProjects;
 
 	public SendMail() {
 		LoadProperties load = new LoadProperties();
 		username = load.getUser();
 		password = load.getPassword();
 		todoistProjects = load.getProjects();
 	}
 
 	public void sendTasks(List<Link> tasks) {
 		for (int x = 0; x < tasks.size(); x++) {
 			printMessage(tasks, x);
 			Link task = tasks.get(x);
 			sendEmail(getTo(task), task.toString(), "");
 
 			if (notLastTask(tasks, x)) {
 				sleepForFiveSeconds();
 			}
 		}
 	}
 
 	private String getTo(Link task) {
 		String firstTag = getFirstTag(task);
 		String to = todoistProjects.get(firstTag);
		if (to == null) {
			System.err.println("Could not find project (" + firstTag
					+ ") setting project as default.");
 			to = todoistProjects.get(DEFAULT_PROJECT_NAME);
 		}
 		return to;
 	}
 
 	private String getFirstTag(Link task) {
 		ArrayList<String> tags = task.getTags();
 		String firstTag = tags.get(0);
 		String lowerCase = firstTag.toLowerCase();
 		return lowerCase;
 	}
 
 	private void printMessage(List<Link> tasks, int index) {
 		Link link = tasks.get(index);

 		StringBuilder message = new StringBuilder();
 		message.append("Sending task ");
		message.append(index + 1);
 		message.append(" of ");
 		message.append(tasks.size());
 		message.append(" with tags ");
 		message.append(link.getTags());

 		message.append(": ");
 		message.append(link.getName());
 		System.out.println(message.toString());
 	}
 
 	private boolean notLastTask(List<Link> tasks, int x) {
 		return x < tasks.size() - 1;
 	}
 
 	private void sleepForFiveSeconds() {
 		try {
 			Thread.sleep(5000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void sendEmail(String to, String subject, String note) {
 		try {
 
 			Message message = new MimeMessage(getSession());
 			message.setFrom(new InternetAddress(username));
 			message.setRecipients(Message.RecipientType.TO,
 					InternetAddress.parse(to));
 			message.setSubject(subject);
 			message.setText(note);
 
 			Transport.send(message);
 
			System.out.println("Email Sending Done");
 
 		} catch (MessagingException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private Session getSession() {
 		if (session == null) {
 			Properties props = new Properties();
 			props.put("mail.smtp.auth", "true");
 			props.put("mail.smtp.starttls.enable", "true");
 			props.put("mail.smtp.host", "smtp.gmail.com");
 			props.put("mail.smtp.port", "587");
 
 			session = Session.getInstance(props,
 					new javax.mail.Authenticator() {
 
 						@Override
 						protected PasswordAuthentication getPasswordAuthentication() {
 							return new PasswordAuthentication(username,
 									password);
 						}
 					});
 		}
 		return session;
 	}
 }
