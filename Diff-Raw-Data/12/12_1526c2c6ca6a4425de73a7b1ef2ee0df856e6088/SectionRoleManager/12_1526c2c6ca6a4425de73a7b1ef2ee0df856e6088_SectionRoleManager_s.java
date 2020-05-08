 package com.rfp.manager;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import com.rfp.service.RFPSectionService;
 import com.rfp.service.SectionRoleService;
 import com.rfp.service.UserService;
 import com.rfp.to.RFPSectionTO;
 import com.rfp.to.RFPTO;
 import com.rfp.to.SectionRoleTO;
 import com.rfp.to.UserTO;
 
 public class SectionRoleManager {
 	
 	public boolean createSectionRole(SectionRoleTO to, RFPTO rfp, long rfpSectionId)
 	{
 		SectionRoleService service = new SectionRoleService();
 		if (service.createSectionRole(to))
 		{
 			UserTO user = new UserService().getUser(to.getUsername());
 			RFPSectionTO rfpSection = new RFPSectionService().getRFPSection(rfpSectionId);
 			sendMail(to, rfp, rfpSection, user);
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	public void sendMail(SectionRoleTO to, RFPTO rfp, RFPSectionTO rfpSection, UserTO user)
 	{		
 		String toMail = user.getEmail();
 		String fromMail = "noreplay@jyl.hostjava.net";
 		
 		Properties properties = new Properties();
		properties.put("mail.smtp.host", "localhost");    
        properties.put("mail.smtp.port", "26");
        properties.put("mail.smtp.starttls.enable","true");
        properties.put("mail.smtp.user", "noreplay");
 		properties.put("mail.smtp.auth", "true");
//		properties.put("mail.smtp.socketFactory.port", "465");
		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.socketFactory.fallback", "false");
 
 		Session session = Session.getDefaultInstance(properties);
 		
 		try {
 			String subject = "Asignacion en el RFP " + rfp.getName();
 		    MimeMessage msg = new MimeMessage(session);
 		    msg.setFrom(new InternetAddress(fromMail));
 		    InternetAddress[] address = {new InternetAddress(toMail)};
 		    msg.setRecipients(Message.RecipientType.TO, address);
 		    msg.setSubject(subject);
 		    msg.setSentDate(new Date());
 		    msg.setText("Estimado " + user.getFirstName() + " " + user.getLastName() + "\n" +
 		    		"Se le ha realizado la asignacion de la seccion " + rfpSection.getSectionName() +
 		    		" del RFP '" + rfp.getName() + "' emitido por la empresa " + rfp.getCompany() + 
 		    		".\nEl RFP tiene fecha de decision para el " + calToString(rfp.getDecitionDate()) + 
 		    		".\n\nNo responda a este correo.");
		    //Transport.send(msg);
 		    Transport t = session.getTransport("smtp");
             t.connect((String)properties.get("mail.smtp.user"), "hola123Mundo");  
             t.sendMessage(msg, msg.getAllRecipients());
             t.close();
 		} catch (MessagingException mex) {
 			mex.printStackTrace();
 		}
 	}
 	
 	public String calToString(Calendar calendar)
 	{
 		return "" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DATE) + "/" + 
 				calendar.get(Calendar.YEAR); 
 	}
 }
