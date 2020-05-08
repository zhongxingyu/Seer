 /**
  *   Copyright(c) 2010-2011 CodWar Soft
  * 
  *   This file is part of IPDB UrT.
  *
  *   IPDB UrT is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This software is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this software. If not, see <http://www.gnu.org/licenses/>.
  */
 package iddb.core.util;
 
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.mail.DefaultAuthenticator;
 import org.apache.commons.mail.Email;
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class MailManager {
 
 	private static final Logger log = LoggerFactory.getLogger(MailManager.class);
 	
 	private static MailManager instance;
 	Properties props;
 	
 	private MailManager() {
 		props = new Properties();
 		try {
 			props.load(getClass().getClassLoader().getResourceAsStream("mail.properties"));
 		} catch (Exception e) {
 			log.error("Unable to load mail properties [{}]", e.getMessage());
 		}
 	}
 	
 	public static MailManager getInstance() {
 		if (instance == null) {
 			instance = new MailManager();
 		}
 		return instance;
 	}
 	
 	private void setEmailProps(Email email) throws EmailException {
 		email.setFrom(props.getProperty("from"), "IPDB");
 		if (props.containsKey("bounce")) email.setBounceAddress(props.getProperty("bounce"));
 		if (props.containsKey("host")) email.setHostName(props.getProperty("host"));
 		if (props.containsKey("port")) email.setSmtpPort(Integer.parseInt(props.getProperty("port")));
 		if (props.containsKey("username")) email.setAuthenticator(new DefaultAuthenticator(props.getProperty("username"), props.getProperty("password")));
 		if (props.containsKey("ssl") && props.getProperty("ssl").equalsIgnoreCase("true")) email.setSSL(true);
 		if (props.containsKey("tls") && props.getProperty("tls").equalsIgnoreCase("true")) email.setTLS(true);
 		if (props.containsKey("debug") && props.getProperty("debug").equalsIgnoreCase("true")) email.setDebug(true);
 	}
 	
 	public void sendAdminMail(String subject, String message, String replyTo) throws Exception {
 		if (props == null) throw new Exception("Unable to access email subsystem.");
		try {
			Email email = new SimpleEmail();
			email.setSubject(subject);
			email.setMsg(message);
			if (replyTo != null) {
				email.addReplyTo(replyTo);
			}
			for (String adr : props.getProperty("admin").split(";")) {
				email.addTo(adr);
			}
			setEmailProps(email);
			email.send();
		} catch (Exception e) {
			log.error("{}: {}", e.getClass().getName(), e.getMessage());
			throw new Exception("We are unable to send your message right now.");
 		}
 	}
 	
 	public void sendMail(String subject, String template, String[] dest, Map<String, String> args) throws Exception {
 		if (props == null) throw new Exception("Unable to access email subsystem.");
 		try {
 			Email email = new SimpleEmail();
 			email.setSubject(subject);
 			email.setMsg(TemplateManager.getTemplate(template, args));
 			for (String adr : dest) {
 				email.addTo(adr);
 			}
 			setEmailProps(email);
 			email.send();
 		} catch (Exception e) {
 			log.error("{}: {}", e.getClass().getName(), e.getMessage());
 			throw new Exception("We are unable to send your message right now.");
 		}		
 	}
 
 }
