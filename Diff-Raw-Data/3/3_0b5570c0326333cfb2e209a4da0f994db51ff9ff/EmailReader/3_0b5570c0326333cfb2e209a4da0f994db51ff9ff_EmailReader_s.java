 /*
  * Copyright (C) 2012 Dan Klco
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy of 
  * this software and associated documentation files (the "Software"), to deal in 
  * the Software without restriction, including without limitation the rights to 
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies 
  * of the Software, and to permit persons to whom the Software is furnished to do 
  * so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in 
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
  * IN THE SOFTWARE.
  */
 package org.klco.email2html;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Properties;
 
 import javax.mail.BodyPart;
 import javax.mail.Folder;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Store;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMultipart;
 import javax.mail.search.SubjectTerm;
 
 import org.klco.email2html.models.Email2HTMLConfiguration;
 import org.klco.email2html.models.EmailMessage;
 import org.owasp.html.PolicyFactory;
 import org.owasp.html.Sanitizers;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class for reading emails from an email server.
  * 
  * @author dklco
  */
 public class EmailReader {
 
 	/**
 	 * The HTML Sanitizer policy, essentially allows only block level elements,
 	 */
 	private static final PolicyFactory policy = Sanitizers.FORMATTING
 			.and(Sanitizers.BLOCKS).and(Sanitizers.IMAGES)
 			.and(Sanitizers.LINKS);
 
 	/**
 	 * The OutputWriter instance.
 	 */
 	private OutputWriter outputWriter = null;
 
 	/** The Constant log. */
 	private static final Logger log = LoggerFactory
 			.getLogger(EmailReader.class);
 
 	/** The config. */
 	private Email2HTMLConfiguration config;
 
 	/** The Constant READABLE_DATE_FORMAT. */
 	private static final SimpleDateFormat READABLE_DATE_FORMAT = new SimpleDateFormat(
 			"MMM d, yyyy");
 
 	private boolean overwrite;
 
 	private String[] breakStrings;
 
 	/**
 	 * Instantiates a new email reader.
 	 * 
 	 * @param config
 	 *            the config
 	 */
 	public EmailReader(Email2HTMLConfiguration config) {
 		this.config = config;
 		outputWriter = new OutputWriter(config);
 
 		overwrite = Boolean.valueOf(config.getOverwrite());
 
 		breakStrings = config.getBreakStrings().split("\\,");
 		log.debug("Using break strings: " + Arrays.toString(breakStrings));
 	}
 
 	/**
 	 * Read emails.
 	 */
 	public void readEmails() {
 		log.info("getEmail");
 		Properties props = System.getProperties();
 		props.setProperty("mail.store.protocol", "imaps");
 		try {
 
 			Session session = Session.getDefaultInstance(props, null);
 			Store store = session.getStore("imaps");
 			store.connect(config.getUrl(), config.getUsername(),
 					config.getPassword());
 			Folder folder = store.getFolder(config.getFolder());
 			folder.open(Folder.READ_ONLY);
 
 			Message[] messages = null;
 			if (config.getSearchSubject() != null) {
 				log.debug("Searching for messages with subject {}",
 						config.getSearchSubject());
 				SubjectTerm subjectTerm = new SubjectTerm(
 						config.getSearchSubject());
 				messages = folder.search(subjectTerm);
 			} else {
 				messages = folder.getMessages();
 			}
 
 			List<EmailMessage> sortedMessages = new ArrayList<EmailMessage>();
 			log.debug("Loading messages from the server");
 			for (int i = 0; i < messages.length; i++) {
 				log.info("Processing message {} of {}", i, messages.length);
 				Message message = messages[i];
 				try {
 					sortedMessages.add(saveMessage(message));
 				} catch (Exception e) {
 					log.error(
 							"Exception saving message: "
 									+ READABLE_DATE_FORMAT.format(message
 											.getSentDate()), e);
 				}
 			}
 
 			log.debug("Sorting messages");
 			Collections.sort(sortedMessages, new Comparator<EmailMessage>() {
 				public int compare(EmailMessage o1, EmailMessage o2) {
 					return o1.getSentDate().compareTo(o2.getSentDate());
 				}
 			});
 
 			log.debug("Writing index file");
 			outputWriter.writeIndex(sortedMessages);
 
 		} catch (MessagingException e) {
 			log.error("Exception accessing emails", e);
 		} catch (IOException e) {
 			log.error("IOException accessing emails", e);
 		}
 	}
 
 	/**
 	 * Trim message.
 	 * 
 	 * @param message
 	 *            the message
 	 * @return the string
 	 */
 	private String trimMessage(String message) {
 		log.trace("trimMessage");
 
 		for (String breakString : breakStrings) {
 			int index = message.indexOf(breakString);
 			if (index != -1) {
 				message = message.substring(0, index);
 			}
 		}
 		return message;
 	}
 
 	/**
 	 * Gets the sender.
 	 * 
 	 * @param message
 	 *            the message
 	 * @return the sender
 	 */
 	private static String getSender(Message message) {
 		log.trace("getSender");
 		String from = "";
 		try {
 			log.debug("Getting sender");
 			InternetAddress address = (InternetAddress) message.getFrom()[0];
 			if (address.getPersonal() != null
 					&& address.getPersonal().trim().length() != 0) {
 				from = address.getPersonal();
 			} else {
				from = address.getAddress();
 			}
 		} catch (Exception e) {
 			log.warn("Unable to get address", e);
 		}
 		return from;
 	}
 
 	/**
 	 * Save message.
 	 * 
 	 * @param message
 	 *            the message
 	 * @param outputDir
 	 *            the output dir
 	 * @param templateStr
 	 *            the template str
 	 * @throws IOException
 	 *             Signals that an I/O exception has occurred.
 	 * @throws MessagingException
 	 *             the messaging exception
 	 */
 	private EmailMessage saveMessage(Message message) throws IOException,
 			MessagingException {
 		log.trace("saveMessage");
 
 		log.debug("Processing message from: " + message.getSentDate());
 
 		log.debug("Loading default properties");
 		EmailMessage emailMessage = new EmailMessage();
 		emailMessage.setSubject(message.getSubject());
 		emailMessage.setSender(getSender(message));
 		emailMessage.setSentDate(message.getSentDate());
 
 		boolean alreadyExists = outputWriter.fileExists(emailMessage);
 
 		if (message.getContent() instanceof MimeMultipart) {
 			MimeMultipart parts = (MimeMultipart) message.getContent();
 			for (int i = 0; i < parts.getCount(); i++) {
 				BodyPart bodyPart = parts.getBodyPart(i);
 				try {
 
 					log.info("Found part: " + bodyPart.getContentType());
 
 					if (bodyPart.getContentType().toUpperCase()
 							.startsWith("IMAGE")) {
 						if (!alreadyExists) {
 							outputWriter
 									.writeAttachment(emailMessage, bodyPart);
 						} else {
 							outputWriter.addAttachment(emailMessage, bodyPart);
 						}
 					} else {
 						log.debug("Processing message text");
 						if (bodyPart.getContent() instanceof MimeMultipart) {
 							MimeMultipart textParts = (MimeMultipart) bodyPart
 									.getContent();
 							for (int d = 0; d < textParts.getCount(); d++) {
 								BodyPart textPart = textParts.getBodyPart(d);
 
 								if (textPart.getContentType().toLowerCase()
 										.startsWith("text/html")
 										|| (emailMessage.getMessage() == null && textPart
 												.getContentType().toLowerCase()
 												.startsWith("text/plain"))) {
 									log.debug("Loading message from multi body part");
 									emailMessage
 											.setFullMessage((String) textPart
 													.getContent());
 									emailMessage.setMessage(policy
 											.sanitize(trimMessage(emailMessage
 													.getFullMessage())));
 
 								} else {
 									log.debug(
 											"Skipping part with content type: {}",
 											textPart.getContentType());
 								}
 							}
 						} else if (bodyPart.getContent() instanceof MimeBodyPart) {
 							MimeBodyPart mimePart = (MimeBodyPart) bodyPart
 									.getContent();
 							if (mimePart.getContentType().toLowerCase()
 									.startsWith("text/html")
 									|| emailMessage.getMessage() == null) {
 								log.debug("Loading message from mime body part");
 								emailMessage.setFullMessage((String) mimePart
 										.getContent());
 								emailMessage.setMessage(policy
 										.sanitize(trimMessage(emailMessage
 												.getFullMessage())));
 							} else {
 								log.debug(
 										"Skipping part with content type: {}",
 										mimePart.getContentType());
 							}
 						} else {
 							log.debug("Loading message from body part");
 							emailMessage.setFullMessage(bodyPart.getContent()
 									.toString());
 							emailMessage.setMessage(policy
 									.sanitize(trimMessage(emailMessage
 											.getFullMessage())));
 						}
 					}
 				} catch (Exception e) {
 					log.warn("Unable to process part " + bodyPart
 							+ " due to exception " + e.toString(), e);
 				}
 			}
 		} else if (message.getContent() instanceof MimeBodyPart) {
 			log.debug("Loading message from a BodyPart");
 			MimeBodyPart body = (MimeBodyPart) message.getContent();
 			emailMessage.setFullMessage(body.getContent().toString());
 			emailMessage.setMessage(policy.sanitize(trimMessage(emailMessage
 					.getFullMessage())));
 		} else {
 			log.debug("Loading message from email content");
 			emailMessage.setFullMessage(message.getContent().toString());
 			emailMessage.setMessage(policy.sanitize(trimMessage(emailMessage
 					.getFullMessage())));
 		}
 
 		if (overwrite || !alreadyExists) {
 			outputWriter.writeHTML(emailMessage);
 		} else {
 			log.debug("Message already exists, not writing");
 		}
 		return emailMessage;
 	}
 }
