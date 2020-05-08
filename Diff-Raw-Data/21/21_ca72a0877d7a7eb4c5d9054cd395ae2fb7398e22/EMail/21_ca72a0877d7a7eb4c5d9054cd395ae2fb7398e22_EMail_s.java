 /*
  * Copyright 2011-2014 Jeroen Meetsma - IJsberg Automatisering BV
  *
  * This file is part of Iglu.
  *
  * Iglu is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Iglu is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ijsberg.iglu.util.mail;
 
 import org.ijsberg.iglu.util.io.FileData;
 import org.ijsberg.iglu.util.misc.KeyGenerator;
 
 import java.io.PrintWriter;
 import java.net.URL;
 import java.net.URLConnection;
 
 /**
  * Mails an SMTP message to recipients.
  */
 public class EMail// implements Executable
 {
 	//specify mail.host using -D or special init-servlet
 	private String mailserver;
 	private String mailfrom;
 	private String mailto;
 	private String subject;
 	private String message;
 	private FileData[] attachments;
 	private PrintWriter outMail;
 
 	/**
 	 * @param senderName nice name of sender
 	 * @param mailto
 	 * @param subject
 	 * @param message
 	 */
 	public EMail(String senderName, String senderMailAddress, String mailto, String subject, String message) {
 		this(senderName, senderMailAddress, mailto, subject, message, null);
 	}
 
 	/**
 	 * @param senderName nice name of sender
 	 */
 	public EMail(String senderName, String senderMailAddress, String mailto, String subject, String message, FileData[] attachments) {
 		this.mailserver = (String) System.getProperties().get("mail.host");
 		//TODO get rid of this one
		String user = (String) System.getProperties().get("user.name");
 
		if (mailserver == null || user == null) {
			throw new RuntimeException("'mail.host' or 'user.name' not set in system properties");
 		}
 		if (mailto == null || "".equals(mailto)) {
 			throw new IllegalArgumentException("recipient unknown");
 		}
		this.mailfrom = '\"' + (senderName != null ? senderName : user) + '\"' + " <" + senderMailAddress + '>';
 		this.mailto = mailto;
 		this.subject = subject != null ? subject.trim() : "";
 		this.message = message;
		this.attachments = attachments;
 	}
 
 	/**
 	 * Attempts to send the message (asynchronously).
 	 *
 	 * @return
 	 * @throws Throwable
 	 */
 	public Object execute() throws Throwable {
 		try {
 			URL u = new URL("mailto:" + mailto);
 			URLConnection c = u.openConnection();
 			c.setDoInput(false);
 			c.setDoOutput(true);
 			c.connect();
 			outMail = new PrintWriter(c.getOutputStream(), true);
 			outMail.println("From: " + mailfrom);
 			outMail.println("To: " + mailto);
 			outMail.println("Subject: " + subject);
 			if (attachments == null) {
 				outMail.println("Content-Type: text/plain;");
 				outMail.println("	charset=\"UTF-8\"");
 				outMail.println();
 				if (message != null) {
 					outMail.println(message);
 				}
 			}
 			else {
 				String boundary = "----=" + KeyGenerator.generateKey();
 				outMail.println("MIME-Version: 1.0\n" +
 						"Content-Type: multipart/mixed; " +
 						"boundary=\"" + boundary + '\"');
 
 				outMail.println();
 				if (message != null) {
 					outMail.println("--" + boundary);
 					outMail.println("Content-Type: text/plain;");
 					outMail.println("	charset=\"UTF-8\"");
 	//				outMail.println("Content-Transfer-Encoding: quoted-printable");
 					outMail.println();
 					outMail.println(message);
 				}
 				for (int i = 0; i < attachments.length; i++) {
 					outMail.println();
 					outMail.println("--" + boundary);
 					outMail.println("Content-Type: " + attachments[i].getMimeType() + ';');
 					outMail.println("	name=\"" + attachments[i].getFileName() + '\"');
 					outMail.println("Content-Transfer-Encoding: base64");
 					outMail.println("Content-Disposition: attachment;");
 					outMail.println("	filename=\"" + attachments[i].getFileName() + '\"');
 					outMail.println();
 					outMail.println(attachments[i].getRawDataBase64Encoded());
 					outMail.println();
 				}
 				outMail.println("--" + boundary + "--");
 
 			}
 		}
 		catch (Throwable t) {
 			//failure of async action must be logged somewhere
 			t.printStackTrace();
 			throw new java.lang.RuntimeException("mailing " + mailto + " via " + mailserver + " failed", t);
 		}
 		finally {
 			if (outMail != null) {
 				outMail.close();
 			}
 		}
 		return null;
 	}
 
 
 	/**
 	 * Tries to abort sending the mail.
 	 */
 	public void abort() {
 		if (outMail != null) {
 			outMail.close();
 		}
 	}
 
 
 	/**
 	 * Mails a message to certain recipients.
 	 */
 	public void mail() {
 		final EMail mail = this;
 		try {
 			mail.execute();
 		} catch (Throwable throwable) {
 			throwable.printStackTrace();
 		}
 		/*
 		Executable exec = new Executable() {
 			protected Object execute() throws Throwable {
 				return mail.execute();
 			}
 		};
 		exec.executeAsync();
 		*/
 	}
 }
