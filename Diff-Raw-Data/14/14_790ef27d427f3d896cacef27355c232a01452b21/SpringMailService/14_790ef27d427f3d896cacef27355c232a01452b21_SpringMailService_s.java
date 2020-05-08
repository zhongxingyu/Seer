 package com.goodhope.thsale.services;
 
 import java.io.File;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMessage;
 
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.mail.javamail.MimeMessageHelper;
 
 public class SpringMailService {
 
 	private JavaMailSender javaMailSender;
 
 	public void sendSimpleMail(String to, String subject, String content) {
 		SimpleMailMessage message = new SimpleMailMessage();
 		message.setTo(to);
 		message.setSubject(subject);
 		message.setText(content);
 		javaMailSender.send(message);
 
 	}
 
	public void sendMimeMail(String to, String subject, String content, String[] paths) {
 
 		MimeMessage mime = javaMailSender.createMimeMessage();
 		MimeMessageHelper helper;
 		try {
 			helper = new MimeMessageHelper(mime, true, "utf-8");
 			helper.setTo(to);
 			helper.setSubject(subject);
 			helper.setText(content);
 			for (String path : paths) {
 				helper.addAttachment(path, new File(path));
 			}
 
 		} catch (MessagingException e) {
 			e.printStackTrace();
 		}
 		javaMailSender.send(mime);
 	}
 
 	public void setJavaMailSender(JavaMailSender javaMailSender) {
 		this.javaMailSender = javaMailSender;
 	}
 
 }
