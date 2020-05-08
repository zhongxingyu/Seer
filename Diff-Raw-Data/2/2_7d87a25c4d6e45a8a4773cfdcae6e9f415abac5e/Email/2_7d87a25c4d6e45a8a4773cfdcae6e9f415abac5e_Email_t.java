 /**
  * This package contains all the available messagetypes and also the superclass <Message>
  */
 package messageTypes;
 
 import java.io.File;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import exceptions.ValidationException;
 
 
 /**
  * This is a specific message type which 
  *
  */
 public class Email extends Message implements IValidator {
 	//TODO: Javadoc
 	private File attachment;
 	
 	public Email(String recipient,String subject, String message, Date sendTime, Date reminderTime, File attachment) throws Exception {
 		super(recipient, subject, message, sendTime, reminderTime);
 		
 		if(attachment!=null){
 			this.setAttachment(attachment);
 		}
 		
 		try{
 			this.validate();
 		}
 		catch(ValidationException validationException){
 			throw validationException;
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void send() {
 		System.out.println("\"" + this.getSubject() + "\" an \""
 				+ this.getRecipient() + "\" geschickt.");
 		System.out.println("Nachricht:");
 		System.out.println(this.getText());
 		if(this.getAttachment()!=null) {
 			System.out.println("Attachment: " + this.getAttachment().getAbsolutePath());
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean validate() throws ValidationException{
		if (isValidEmailAddress(this.getRecipient().trim())) {
 			if (this.getSubject().equals("") || this.getText().equals("")) {
 				throw new ValidationException(this.getRecipient(),"Subject or Text is empty!");
 			}
 		} else {
 			throw new ValidationException(this.getRecipient(),"Email address is invalid");
 		}
 		return true;
 	}
 	
 	// RegEx für Email addresse
 	public boolean isValidEmailAddress(String emailAddress) {
 		//String expression = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
 		String expression = "^[\\w\\-]([\\.\\w])+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
 		CharSequence inputStr = emailAddress;
 		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
 		Matcher matcher = pattern.matcher(inputStr);
 		return matcher.matches();
 
 	}
 	
 	@Override
 	public void sendReminder() {
 		System.out.println("\"Das ist der Reminder an die Message: "
 				+ this.getSubject() + " an den Empfänger " + this.getRecipient()
 				+ "\"");
 	}
 
 	public File getAttachment() {
 		return attachment;
 	}
 
 	public void setAttachment(File attachment) {
 		this.attachment = attachment;
 	}
 }
