 package sleet.email;
 
 import java.util.List;
 
 /**
  * Defines an email message. This class is a wrapper of the {@link EmailRaw}
  * class, providing methods geared mostly towards creating new emails. Use this
  * class for creating a new email to send with the {@link sleet.smtp.MailSender}
  * class.
  * @author Mike Angstadt [mike.angstadt@gmail.com]
  */
 public class Email {
 	private EmailRaw email = new EmailRaw();
 
 	/**
 	 * Gets the "from" address.
 	 * @return the "from" address
 	 */
 	public EmailAddress getFrom() {
 		return email.getMailFrom();
 	}
 
 	/**
 	 * Sets the "from" address.
 	 * @param from the "from" address
 	 */
 	public void setFrom(EmailAddress from) {
 		email.setMailFrom(from);
 		email.getData().getHeaders().setEmailHeader("From", from);
 	}
 
 	/**
 	 * Adds a "To" address.
 	 * @param to the "To" address
 	 */
 	public void addTo(EmailAddress to) {
 		email.getRecipients().add(to);
 		AddressHeader addresses = email.getData().getHeaders().getAddressHeader("To");
		if (addresses == null){
			addresses = new AddressHeader();
		}
 		addresses.getAddresses().add(to);
 		email.getData().getHeaders().setEmailHeader("To", addresses.getAddresses());
 	}
 
 	/**
 	 * Sets the "To" addresses.
 	 * @param to the "To" addresses
 	 */
 	public void setTo(List<EmailAddress> to) {
 		email.getRecipients().addAll(to);
 		email.getData().getHeaders().setEmailHeader("To", to);
 	}
 
 	/**
 	 * Sets the "CC" (carbon copy) addresses.
 	 * @param cc the "CC" addresses
 	 */
 	public void setCc(List<EmailAddress> cc) {
 		email.getRecipients().addAll(cc);
 		email.getData().getHeaders().setEmailHeader("Cc", cc);
 	}
 
 	/**
 	 * Sets the "BCC" (blind carbon copy) addresses.
 	 * @param bcc the "BCC" addresses.
 	 */
 	public void setBcc(List<EmailAddress> bcc) {
 		email.getRecipients().addAll(bcc);
 		//do not add Bcc recipients to header
 		//email.getData().getHeaders().addHeader("Bcc", "Undisclosed recipients:;");
 	}
 
 	//TODO support groups
 
 	/**
 	 * Gets all the recipients of the email.
 	 * @param all the recipients of the email
 	 */
 	public List<EmailAddress> getAllRecipients() {
 		return email.getRecipients();
 	}
 
 	/**
 	 * Sets the subject.
 	 * @param subject the subject
 	 */
 	public void setSubject(String subject) {
 		email.getData().getHeaders().setSubject(subject);
 	}
 
 	/**
 	 * Sets the email body.
 	 * @param body the email body
 	 */
 	public void setBody(String body) {
 		email.getData().setBody(body);
 	}
 
 	/**
 	 * Gets the email body.
 	 * @return the email body
 	 */
 	public String getBody() {
 		return email.getData().getBody();
 	}
 
 	/**
 	 * Gets the email headers.
 	 * @return the email headers
 	 */
 	public EmailHeaders getHeaders() {
 		return email.getData().getHeaders();
 	}
 
 	/**
 	 * Gets the raw "DATA" portion of the email.
 	 * @return the "DATA" portion of the email
 	 */
 	public EmailData getData() {
 		return email.getData();
 	}
 
 	/**
 	 * Sets the raw "DATA" portion of the email.
 	 * @param data the "DATA" portion of the email
 	 */
 	public void setData(EmailData data) {
 		email.setData(data);
 	}
 
 	/**
 	 * Gets the object that this class wraps.
 	 * @return the raw email object
 	 */
 	public EmailRaw getEmailRaw() {
 		return email;
 	}
 }
