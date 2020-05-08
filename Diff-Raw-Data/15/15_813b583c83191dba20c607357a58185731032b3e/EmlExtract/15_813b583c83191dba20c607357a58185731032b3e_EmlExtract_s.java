 /**
  * This file is part of Vitam Project.
  * 
  * Copyright 2010, Frederic Bregier, and individual contributors by the @author
  * tags. See the COPYRIGHT.txt in the distribution for a full listing of individual contributors.
  * 
  * All Vitam Project is free software: you can redistribute it and/or modify it under the terms of
  * the GNU General Public License as published by the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later version.
  * 
  * Vitam is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with Vitam. If not, see
  * <http://www.gnu.org/licenses/>.
  */
 package fr.gouv.culture.vitam.eml;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 
 import javax.mail.Address;
 import javax.mail.BodyPart;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.Session;
 import javax.mail.internet.MimeMessage;
 
 import org.dom4j.Element;
 
 import fr.gouv.culture.vitam.extract.ExtractInfo;
 import fr.gouv.culture.vitam.utils.Commands;
 import fr.gouv.culture.vitam.utils.ConfigLoader;
 import fr.gouv.culture.vitam.utils.StaticValues;
 import fr.gouv.culture.vitam.utils.VitamArgument;
 import fr.gouv.culture.vitam.utils.XmlDom;
 
 /**
  * Class to try to handle EML files (email)
  * 
  * @author "Frederic Bregier"
  * 
  */
 public class EmlExtract {
 	
 	public static HashMap<String, String> filEmls = new HashMap<String, String>();
 	
 	private static String addAddress(Element root, String entry, Address address, String except) {
 		String value = address.toString();
 		value = StringUtils.selectChevron(value);
 		if (value == null || (except != null && value.equalsIgnoreCase(except))) {
 			return null;
 		}
 		Element val = XmlDom.factory.createElement(entry);
 		val.setText(StringUtils.unescapeHTML(value, true, false));
 		root.add(val);
 		return value;
 	}
 	
 	public static Element extractInfoEmail(File emlFile, String filename, VitamArgument argument,
 			ConfigLoader config) {
 		Element root = XmlDom.factory.createElement("email");
 		Element keywords = XmlDom.factory.createElement("keywords");
 		Element metadata = XmlDom.factory.createElement("metadata");
 		try {
 			MimeMessage message = createOneMessageFromFile(emlFile);
 			Address[] from = message.getFrom();
 			Address sender = message.getSender();
 			if (from != null && from.length > 0) {
 				String value0 = null;
 				Element sub = XmlDom.factory.createElement("from");
 				if (sender != null) {
 					value0 = addAddress(sub, "sender", sender, null);
 				}
 				for (Address address : from) {
 					addAddress(sub, "sender", address, value0);
 				}
 				metadata.add(sub);
 			} else if (sender != null) {
 				Element sub = XmlDom.factory.createElement("from");
 				addAddress(sub, "sender", sender, null);
 				metadata.add(sub);
 			}
 			Address[] replyTo = message.getReplyTo();
 			if (replyTo != null && replyTo.length > 0) {
 				Element sub = XmlDom.factory.createElement("replyTo");
 				for (Address address : replyTo) {
 					addAddress(sub, "replyto", address, null);
 				}
 				metadata.add(sub);
 			}
 			Address[] toRecipients = message.getRecipients(Message.RecipientType.TO);
 			if (toRecipients != null && toRecipients.length > 0) {
 				Element sub = XmlDom.factory.createElement("toRecipients");
 				for (Address address : toRecipients) {
 					addAddress(sub, "to", address, null);
 				}
 				metadata.add(sub);
 			}
 			Address[] ccRecipients = message.getRecipients(Message.RecipientType.CC);
 			if (ccRecipients != null && ccRecipients.length > 0) {
 				Element sub = XmlDom.factory.createElement("ccRecipients");
 				for (Address address : ccRecipients) {
 					addAddress(sub, "cc", address, null);
 				}
 				metadata.add(sub);
 			}
 			Address[] bccRecipients = message.getRecipients(Message.RecipientType.BCC);
 			if (bccRecipients != null && bccRecipients.length > 0) {
 				Element sub = XmlDom.factory.createElement("bccRecipients");
 				for (Address address : bccRecipients) {
 					addAddress(sub, "bcc", address, null);
 				}
 				metadata.add(sub);
 			}
 			String subject = message.getSubject();
 			if (subject != null) {
 				Element sub = XmlDom.factory.createElement("subject");
 				sub.setText(StringUtils.unescapeHTML(subject, true, false));
 				metadata.add(sub);
 			}
 			Date sentDate = message.getSentDate();
 			if (sentDate != null) {
 				Element sub = XmlDom.factory.createElement("sentDate");
 				sub.setText(sentDate.toString());
 				metadata.add(sub);
 			}
 			Date receivedDate = message.getReceivedDate();
 			if (receivedDate != null) {
 				Element sub = XmlDom.factory.createElement("receivedDate");
 				sub.setText(receivedDate.toString());
 				metadata.add(sub);
 			}
 			int internalSize = message.getSize();
 			if (internalSize > 0) {
 				Element sub = XmlDom.factory.createElement("internalSize");
 				sub.setText(Integer.toString(internalSize));
 				metadata.add(sub);
 			}
 			int lineCount = message.getLineCount();
 			if (lineCount > 0) {
 				Element sub = XmlDom.factory.createElement("lineCount");
 				sub.setText(Integer.toString(lineCount));
 				metadata.add(sub);
 			}
 			String encoding = message.getEncoding();
 			if (encoding != null) {
 				Element sub = XmlDom.factory.createElement("encoding");
 				sub.setText(StringUtils.unescapeHTML(encoding, true, false));
 				metadata.add(sub);
 			}
 			String description = message.getDescription();
 			if (description != null) {
 				Element sub = XmlDom.factory.createElement("description");
 				sub.setText(StringUtils.unescapeHTML(description, true, false));
 				metadata.add(sub);
 			}
 			String contentType = message.getContentType();
 			if (contentType != null) {
 				Element sub = XmlDom.factory.createElement("contentType");
 				sub.setText(StringUtils.unescapeHTML(contentType, true, false));
 				metadata.add(sub);
 			}
 			String messageId = message.getMessageID();
 			if (messageId != null) {
 				Element sub = XmlDom.factory.createElement("messageId"); 
 				messageId = StringUtils.removeChevron(StringUtils.unescapeHTML(messageId, true, false));
 				sub.setText(messageId);
 				metadata.add(sub);
 			}
 			String contentId = message.getContentID();
 			if (contentId != null) {
 				Element sub = XmlDom.factory.createElement("contentId");
 				sub.setText(StringUtils.removeChevron(StringUtils.unescapeHTML(contentId, true, false)));
 				metadata.add(sub);
 			}
 			String []contentLanguage = message.getContentLanguage();
 			if (contentLanguage != null) {
 				Element sub = XmlDom.factory.createElement("contentLanguage");
 				StringBuilder builder = new StringBuilder();
 				for (String string : contentLanguage) {
 					builder.append(StringUtils.unescapeHTML(string, true, false));
 					builder.append(' ');
 				}
 				sub.setText(builder.toString());
 				metadata.add(sub);
 			}
 			String []headers = message.getHeader("In-Reply-To");
 			String inreplyto = null;
 			if (headers != null) {
 				Element sub = XmlDom.factory.createElement("inReplyTo");
 				StringBuilder builder = new StringBuilder();
 				for (String string : headers) {
 					builder.append(StringUtils.removeChevron(StringUtils.unescapeHTML(string, true, false)));
 					builder.append(' ');
 				}
 				inreplyto = builder.toString().trim();
 				sub.setText(inreplyto);
 				String old = filEmls.get(inreplyto);
 				if (old == null) {
 					old = messageId;
 				} else {
 					old += ","+messageId;
 				}
 				filEmls.put(inreplyto, old);
 				metadata.add(sub);
 			}
 			headers = message.getHeader("References");
 			if (headers != null) {
 				Element sub = XmlDom.factory.createElement("references");
 				StringBuilder builder = new StringBuilder();
 				for (String string : headers) {
 					builder.append(StringUtils.removeChevron(StringUtils.unescapeHTML(string, true, false)));
 					builder.append(' ');
 				}
 				String []refs = builder.toString().trim().split(" ");
 				for (String string : refs) {
 					if (string.length() > 0) {
 						Element ref = XmlDom.factory.createElement("reference");
 						ref.setText(string);
 						sub.add(ref);
 					}
 				}
 				metadata.add(sub);
 			}
 			try {
 				String skey = handleMessage(message, metadata, argument, config);
 				if (metadata.hasContent()) {
 					root.add(metadata);
 				}
 				ExtractInfo.exportMetadata(keywords, skey, "", config, null);
 				if (keywords.hasContent()) {
 					root.add(keywords);
 				}
 			} catch (IOException e) {
 				System.err.println(StaticValues.LBL.error_error.get() + e.toString());
 			}
 			try {
 				message.getInputStream().close();
 			} catch (IOException e) {
 				System.err.println(StaticValues.LBL.error_error.get() + e.toString());
 			}
 		} catch (FileNotFoundException e) {
 			System.err.println(StaticValues.LBL.error_error.get() + e.toString());
 		} catch (MessagingException e) {
 			System.err.println(StaticValues.LBL.error_error.get() + e.toString());
 		}
 		
 		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
 		File [] todelete = tmpDir.listFiles(new FileFilter() {
 			
 			@Override
 			public boolean accept(File arg0) {
 				String name = arg0.getName();
 				return (name.startsWith("droid-archive~") && name.endsWith(".tmp"));
 			}
 		});
 		for (File file : todelete) {
 			if (! file.delete()) {
 				file.deleteOnExit();
 			}
 		}
 		return root;
 	}
 
 	private static final MimeMessage createOneMessageFromFile(File emlFile) throws FileNotFoundException, MessagingException {
 		Properties props = System.getProperties();
         props.put("mail.host", "smtp.vitamdomain.com");
         props.put("mail.transport.protocol", "smtp");
         Session mailSession = Session.getDefaultInstance(props, null);
 		InputStream source = new FileInputStream(emlFile);
         return new MimeMessage(mailSession, source);
 	}
 	
 	private static final String handleMessage(Message message, Element metadata, VitamArgument argument, ConfigLoader config) throws IOException, MessagingException {
 		Object content = message.getContent();
 		String result = "";
 		if (content instanceof String) {
 			result = (String) content;
 		} else if (content instanceof Multipart) {
 			// handle multi part
 			Multipart mp = (Multipart) content;
 			Element identification = XmlDom.factory.createElement("attachmentIdentification");
 			String value = handleMultipart(mp, identification, argument, config);
 			if (identification.hasContent()) {
 				metadata.add(identification);
 			}
 			result = value;
 		}
 		return result;
 	}
 
 	private static final String handleMultipart(Multipart mp, Element identification, VitamArgument argument, ConfigLoader config) throws MessagingException, IOException {
 		int count = mp.getCount();
 		String result = "";
 		for (int i = 0; i < count; i++) {
 			BodyPart bp = mp.getBodyPart(i);
 			
 			Object content = bp.getContent();
 			if (content instanceof String) {
 				result += " " + (String) content;
 			} else if (content instanceof InputStream) {
 				// handle input stream
 				result += " " + addSubIdentities(identification, bp, (InputStream) content, argument, config);
 				((InputStream) content).close();
 			} else if (content instanceof Message) {
 				Message message = (Message) content;
 				result += " " + handleMessageRecur(message, identification, argument, config);
 			} else if (content instanceof Multipart) {
 				Multipart mp2 = (Multipart) content;
 				result += " " + handleMultipartRecur(mp2, identification, argument, config);
 			}
 		}
 		return result;
 	}
 	
 	private static final String addSubIdentities(Element identification, BodyPart bp, InputStream inputStream, VitamArgument argument, ConfigLoader config) {
 		Element newElt = XmlDom.factory.createElement("subidentities");
 		String filename = null;
 		String result = "";
 		try {
 			filename = bp.getFileName();
 			if (filename != null) {
 				Element elt = XmlDom.factory.createElement("filename");
 				elt.setText(filename);
 				newElt.add(elt);
 			} else {
 				filename = "eml.eml";
 			}
 		} catch (MessagingException e) {
 		}
 		try {
 			int size = bp.getSize();
 			if (size > 0) {
 				Element elt = XmlDom.factory.createElement("size");
 				elt.setText(Integer.toString(size));
 				newElt.add(elt);
 			}
 		} catch (MessagingException e) {
 		}
 		try {
 			String description = bp.getDescription();
 			if (description != null) {
 				Element elt = XmlDom.factory.createElement("description");
 				elt.setText(description);
 				newElt.add(elt);
 			}
 		} catch (MessagingException e) {
 		}
 		try {
 			String disposition = bp.getDisposition();
 			if (disposition != null) {
 				Element elt = XmlDom.factory.createElement("disposition");
 				elt.setText(disposition);
 				newElt.add(elt);
 			}
 		} catch (MessagingException e) {
 		}
 		File filetemp = null;
 		FileOutputStream outputStream = null;
 		try {
 			// Force out as eml
 			filetemp = File.createTempFile("tmp", ".eml");
 			byte [] buffer = new byte[8192];
 			int read = 0;
 			outputStream = new FileOutputStream(filetemp);
 			while ((read = inputStream.read(buffer)) >= 0) {
 				outputStream.write(buffer, 0, read);
 			}
 			outputStream.close();
 			outputStream = null;
 		} catch (IOException e1) {
 			if (outputStream != null) {
 				try {
 					outputStream.close();
 				} catch (IOException e) {
 				}
 			}
 			String status = "Error during access to attachment";
 			newElt.addAttribute("status", status);
 			identification.add(newElt);
 			return "";
 		}
 		try {
 			Commands.addFormatIdentification(newElt, filename, filetemp, config, argument);
 			// get back keyword in the main list
 			Element keyw = (Element) newElt.selectSingleNode("keywords");
 			if (keyw != null) {
 				StringBuilder builder = new StringBuilder();
 				@SuppressWarnings("unchecked")
				List<Element> elts = (List<Element>) keyw.selectNodes("rank/word");
 				for (Element elt : elts) {
					builder.append(elt.selectSingleNode("@value").getText());
					builder.append(' ');
 				}
 				result = builder.toString().trim();
 			}
 			
 		} catch (Exception e) {
 			String status = "Error during identification";
 			e.printStackTrace();
 			newElt.addAttribute("status", status);
 		}
 		identification.add(newElt);
 		return result;
 	}
 	
 	private static final String handleMessageRecur(Message message, Element identification, VitamArgument argument, ConfigLoader config) throws IOException, MessagingException {
 		Object content = message.getContent();
 		String result = "";
 		if (content instanceof String) {
 			result = (String) content;
 			// ignore string
 		} else if (content instanceof Multipart) {
 			Multipart mp = (Multipart) content;
 			result = handleMultipartRecur(mp, identification, argument, config);
 			// handle multi part
 		}
 		return result;
 	}
 
 	private static final String handleMultipartRecur(Multipart mp, Element identification, VitamArgument argument, ConfigLoader config) throws MessagingException, IOException {
 		int count = mp.getCount();
 		String result = "";
 		for (int i = 0; i < count; i++) {
 			BodyPart bp = mp.getBodyPart(i);
 			Object content = bp.getContent();
 			if (content instanceof String) {
 				result += " "+ (String) content;
 				// ignore string
 			} else if (content instanceof InputStream) {
 				// handle input stream
 				result += " "+ addSubIdentities(identification, bp, (InputStream) content, argument, config);
 			} else if (content instanceof Message) {
 				Message message = (Message) content;
 				result += " "+ handleMessageRecur(message, identification, argument, config);
 			} else if (content instanceof Multipart) {
 				Multipart mp2 = (Multipart) content;
 				result += " "+ handleMultipartRecur(mp2, identification, argument, config);
 			}
 		}
 		return result;
 	}
 }
