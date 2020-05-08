 package sleet.email;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * This class sits on top of {@link EmailHeadersRaw} and provides methods for
  * accessing the headers that are specifically defined in the RFCs.
  * @author Mike Angstadt [mike.angstadt@gmail.com]
  */
 public class EmailHeaders extends EmailHeadersRaw {
 	private static final Logger logger = Logger.getLogger(EmailHeaders.class.getName());
 
 	/**
 	 * Gets the "Date" header.
 	 * @return the date the message was created, null if there is no date, or
 	 * null if there was a problem parsing the date field
 	 * @see RFC-5322, p.22
 	 */
 	public Date getDate() {
 		String value = getHeader("Date");
 		if (value == null) {
 			return null;
 		}
 		value = removeComments(value);
 		try {
 			DateFormat df = new EmailDateFormat();
 			return df.parse(value);
 		} catch (ParseException e) {
 			logger.log(Level.WARNING, "Email has malformed \"Date\" header: " + value);
 			return null;
 		}
 	}
 
 	/**
 	 * Sets the "Date" header.
 	 * @param date the header value
 	 * @see RFC-5322, p.22
 	 */
 	public void setDate(Date date) {
 		setHeader("Date", new EmailDateFormat().format(date));
 	}
 
 	/**
 	 * Gets the "From" header. It is possible for this header to contain more
 	 * than one address. If there are multiple addresses, then there must be a
 	 * "Sender" header with exactly one email.
 	 * @return the from address(es)
 	 * @see RFC-5322, p.22
 	 */
 	public AddressHeader getFrom() {
 		return getAddressHeader("From");
 	}
 
 	/**
 	 * Gets the "Sender" header. This header should only be present if there are
 	 * multiple addresses in the "From" header or if the actual sender of the
 	 * email is different from the person who composed the email (like a
 	 * secretary sending an email on behalf of his boss). It only contains one
 	 * email address.
 	 * @return the sender address or null if one does not exist
 	 * @see RFC-5322, p.22
 	 */
 	public EmailAddress getSender() {
 		AddressHeader emails = getAddressHeader("Sender");
 		return emails.getAddresses().isEmpty() ? null : emails.getAddresses().get(0);
 	}
 
 	/**
 	 * Gets the "Reply-To" header.
 	 * @return the reply-to address(es)
 	 * @see RFC-5322, p.22
 	 */
 	public AddressHeader getReplyTo() {
 		return getAddressHeader("Reply-To");
 	}
 
 	/**
 	 * Gets the primary recipients of the email.
 	 * @return the to address(es)
 	 * @see RFC-5322, p.23
 	 */
 	public AddressHeader getTo() {
 		return getAddressHeader("To");
 	}
 
 	/**
 	 * Gets the people who should receive a copy of the email, but who are not
 	 * the primary recipients. "Cc" stands for "carbon-copy".
 	 * @return the cc address(es) or null if there are none
 	 * @see RFC-5322, p.23
 	 */
 	public AddressHeader getCc() {
 		return getAddressHeader("Cc");
 	}
 
 	/**
 	 * Gets the recipients whose addresses should not be revealed to the other
 	 * recipients of the email (the "to" and "cc" recipients). "Bcc" stands for
 	 * "blind carbon copy".
 	 * @return the bcc address(es) or null if there aren't any
 	 * @see RFC-5322, p.23
 	 */
 	public AddressHeader getBcc() {
 		return getAddressHeader("Cc");
 	}
 
 	/**
 	 * Gets the message ID.
 	 * @return the message ID or null if there isn't one
 	 * @see RFC-5322, p.23
 	 */
 	public String getMessageId() {
 		List<String> ids = getIdHeader("Message-ID");
 		return ids.isEmpty() ? null : ids.get(0);
 	}
 
 	/**
 	 * Sets the message ID.
 	 * @param id the message ID
 	 * @see RFC-5322, p.23
 	 */
 	public void setMessageId(String id) {
 		setIdHeader("Message-ID", Arrays.asList(new String[] { id }));
 	}
 
 	/**
 	 * Gets the in-reply-to message IDs.
 	 * @return the in-reply-to message IDs or empty list if there aren't any
 	 * @see RFC-5322, p.25
 	 */
 	public List<String> getInReplyTo() {
 		return getIdHeader("In-Reply-To");
 	}
 
 	/**
 	 * Gets the references message IDs.
 	 * @return the references message IDs or empty list if there aren't any
 	 * @see RFC-5322, p.25
 	 */
 	public List<String> getReferences() {
 		return getIdHeader("References");
 	}
 
 	/**
 	 * Gets the message subject.
 	 * @return the subject
 	 */
 	public String getSubject() {
 		return unescape(getHeader("Subject"));
 	}
 
 	/**
 	 * Sets the message subject.
 	 * @param subject the subject
 	 */
 	public void setSubject(String subject) {
 		setHeader("Subject", escape(subject));
 	}
 
 	/**
 	 * Gets the "Comments" header, describing the contents of the email.
 	 * @return the comments or null if there aren't any
 	 */
 	public String getComments() {
 		return getHeader("Comments");
 	}
 
 	/**
 	 * Gets any important words or phrases that are related to the email
 	 * message.
 	 * @return the keywords or null if there aren't any
 	 */
 	public List<String> getKeywords() {
 		String value = getHeader("Keywords");
 		value = value.trim();
 		return Arrays.asList(value.split("\\s*,\\s*"));
 	}
 
 	/**
 	 * Adds a header that contains an email address (such as "From").
 	 * @param name the header name
 	 * @param email the email
 	 */
 	public void addEmailHeader(String name, EmailAddress email) {
 		addEmailHeader(name, Arrays.asList(new EmailAddress[] { email }));
 	}
 
 	/**
 	 * Adds a header that contains a list of email addresses (such as "To").
 	 * @param name the header name
 	 * @param emails the emails
 	 */
 	public void addEmailHeader(String name, List<EmailAddress> emails) {
 		String value = buildEmailHeaderValue(emails);
 		addHeader(name, value.toString());
 	}
 
 	public void setEmailHeader(String name, EmailAddress email) {
 		setEmailHeader(name, Arrays.asList(new EmailAddress[] { email }));
 	}
 
 	public void setEmailHeader(String name, List<EmailAddress> emails) {
 		String value = buildEmailHeaderValue(emails);
 		setHeader(name, value);
 	}
 
 	private String buildEmailHeaderValue(List<EmailAddress> emails) {
 		StringBuilder value = new StringBuilder();
 		if (!emails.isEmpty()) {
 			boolean first = true;
 			for (EmailAddress email : emails) {
 				if (!first) {
 					value.append(", ");
 				}
 				String emailName = email.getName();
 				if (emailName != null) {
 					value.append(emailName);
 					value.append(" <").append(email.getAddress()).append('>');
 				} else {
 					value.append(email.getAddress());
 				}
 				first = false;
 			}
 		}
 		return value.toString();
 	}
 
 	/**
 	 * Gets the groups and individual email addresses in a header.
 	 * @param name the header name (e.g. "To")
 	 * @return
 	 */
 	public AddressHeader getAddressHeader(String name) {
 		String value = getHeader(name);
		if (value == null){
			return null;
		}
 		value = removeComments(value);
 		value = value.trim();
 
 		AddressHeader header = new AddressHeader();
 		EmailGroup curGroup = null;
 		boolean inEmail = false;
 		boolean inQuote = false;
 		StringBuilder buf = new StringBuilder();
 		String addressName = null;
 		String address = null;
 		for (int i = 0; i < value.length(); i++) {
 			char cur = value.charAt(i);
 			char prev = (i == 0) ? 0 : value.charAt(i - 1);
 
 			if (prev == '\\' && (i < 2 || value.charAt(i - 2) != '\\')) {
 				//the current character is escaped
 				buf.append(cur);
 			} else if (cur == '\\') {
 				//escape character
 				continue;
 			} else if (cur == '"') {
 				if (inQuote) {
 					inQuote = false;
 					addressName = buf.toString().trim();
 					buf = new StringBuilder();
 				} else {
 					inQuote = true;
 				}
 			} else if (cur == ':' && !inQuote) {
 				curGroup = new EmailGroup(buf.toString().trim());
 				buf = new StringBuilder();
 			} else if (cur == ';' && !inQuote) {
 				//end of group
 
 				if (address == null) {
 					//if we haven't found an address yet, that means the address is not enclosed in "<>"
 
 					String tempAddr = buf.toString().trim();
 
 					if (tempAddr.isEmpty()) {
 						//obsolete syntax allows for consecutive commas
 						//if the curBuffer is empty, then there were consecutive commas
 						//see RFC-5322, p.50
 					} else {
 						address = tempAddr;
 					}
 				}
 
 				if (address != null) {
 					//obsolete syntax allows whitespace to be inside the address
 					//remove all whitespace inside the address
 					//see RFC-5322, p.50
 					address = address.replaceAll("\\s+", "");
 
 					//add email to return list
 					EmailAddress email = new EmailAddress(address, addressName);
 					curGroup.getAddresses().add(email);
 				}
 
 				header.getGroups().add(curGroup);
 
 				//reset buffers
 				address = addressName = null;
 				curGroup = null;
 				buf = new StringBuilder();
 				inQuote = inEmail = false;
 			} else if (cur == '<' && !inQuote) {
 				String tempName = buf.toString().trim();
 				if (!tempName.isEmpty()) {
 					//the name wasn't enclosed in quotes
 					addressName = tempName;
 				}
 				buf = new StringBuilder();
 				inEmail = true;
 			} else if (cur == '>' && !inQuote) {
 				inEmail = false;
 				address = buf.toString();
 				buf = new StringBuilder();
 			} else if (cur == ',' && !inQuote && !inEmail) {
 				if (address == null) {
 					//if we haven't found an address yet, that means the address is not enclosed in "<>"
 
 					String tempAddr = buf.toString().trim();
 
 					if (tempAddr.isEmpty()) {
 						//obsolete syntax allows for consecutive commas
 						//if the curBuffer is empty, then there were consecutive commas
 						//see RFC-5322, p.50
 					} else {
 						address = tempAddr;
 					}
 				}
 
 				if (address != null) {
 					//obsolete syntax allows whitespace to be inside the address
 					//remove all whitespace inside the address
 					//see RFC-5322, p.50
 					address = address.replaceAll("\\s+", "");
 
 					//add email to return list
 					EmailAddress email = new EmailAddress(address, addressName);
 					if (curGroup == null) {
 						header.getAddresses().add(email);
 					} else {
 						curGroup.getAddresses().add(email);
 					}
 				}
 
 				//reset buffers
 				address = addressName = null;
 				buf = new StringBuilder();
 				inQuote = inEmail = false;
 			} else {
 				buf.append(cur);
 			}
 		}
 
 		if (address == null) {
 			//if we haven't found an address yet, that means the address is not enclosed in "<>"
 
 			String tempAddr = buf.toString().trim();
 
 			if (tempAddr.isEmpty()) {
 				//obsolete syntax allows for consecutive commas
 				//if the curBuffer is empty, then there were consecutive commas
 				//see RFC-5322, p.50
 			} else {
 				address = tempAddr;
 			}
 		}
 
 		if (address != null) {
 			//obsolete syntax allows whitespace to be inside the address
 			//remove all whitespace inside the address
 			//see RFC-5322, p.50
 			address = address.replaceAll("\\s+", "");
 
 			//add email to return list
 			EmailAddress email = new EmailAddress(address, addressName);
 			if (curGroup == null) {
 				header.getAddresses().add(email);
 			} else {
 				curGroup.getAddresses().add(email);
 			}
 		}
 
 		if (curGroup != null) {
 			header.getGroups().add(curGroup);
 		}
 
 		return header;
 	}
 
 	/**
 	 * Gets the IDs in an ID header.
 	 * @param name the header name
 	 * @return the IDs or empty string if the header doesn't exist or there are
 	 * no IDs
 	 */
 	public List<String> getIdHeader(String name) {
 		List<String> ids = new ArrayList<String>();
 		String value = getHeader(name);
 		if (value != null) {
 			Pattern p = Pattern.compile("<(.*?)>");
 			Matcher m = p.matcher(value);
 			while (m.find()) {
 				String id = m.group(1);
 				id = removeComments(id); //remove comments (RFC-5322, p.51)
 				id = id.replaceAll("\\s+", ""); //remove all whitespace (RFC-5322, p.51)
 				ids.add(id);
 			}
 		}
 		return ids;
 	}
 
 	/**
 	 * Sets the value of an ID header.
 	 * @param name the header name
 	 * @param ids the IDs to include in the header value
 	 */
 	public void setIdHeader(String name, List<String> ids) {
 		StringBuilder sb = new StringBuilder();
 		boolean first = true;
 		for (String id : ids) {
 			if (!first) {
 				sb.append(' ');
 			}
 			sb.append('<').append(id).append('>');
 		}
 		setHeader(name, sb.toString());
 	}
 }
