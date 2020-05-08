 package au.org.scoutmaster.views.wizards.bulkSMS;
 
 import java.util.ArrayList;
 
import au.com.vaadinutils.dao.EntityManagerProvider;
 import au.org.scoutmaster.domain.Contact;
 import au.org.scoutmaster.domain.Phone;
 import au.org.scoutmaster.domain.PhoneType;
 import au.org.scoutmaster.domain.Tag;
 
 public class SMSTransmission
 {
 	private Contact contact;
 	private Message message;
 	private Phone recipient;
 	// If an exception is thrown during transmission it is stored here.
 	private Exception exception;
 	private ArrayList<Tag> activityTags;
 
 	public SMSTransmission(ArrayList<Tag> activityTags, Contact contact, Message message, Phone recipient)
 	{
 		this.contact = contact;
 		this.message = message;
 		this.recipient = recipient;
 		this.activityTags = activityTags;
 
 		if (recipient.getPhoneType() != PhoneType.MOBILE)
 			throw new IllegalArgumentException("The phone argument must be of type MOBILE");
		
		// We are about to pass these to a new thread and em so we must detach them.
		EntityManagerProvider.detach(contact);
		for (Tag tag: this.activityTags)
			EntityManagerProvider.detach(tag);

 	}
 
 	public SMSTransmission(Contact contact, Message message, Exception exception)
 	{
 		this.contact = contact;
 		this.message = message;
 		this.exception = exception;
 		this.activityTags = new ArrayList<>();
		
		// We are about to pass these to a new thread and em so we must detach them.
		EntityManagerProvider.detach(contact);

 	}
 
 	public Message getMessage()
 	{
 		return this.message;
 	}
 
 	public Phone getRecipient()
 	{
 		return this.recipient;
 	}
 
 	public String getRecipientPhoneNo()
 	{
 		if (this.recipient != null)
 			return this.recipient.getPhoneNo();
 		else
 			return "";
 	}
 
 	public void setException(Exception e)
 	{
 		this.exception = e;
 
 	}
 
 	public String getResult()
 	{
 		if (exception != null)
 			return exception.getClass().getSimpleName() + ": " + exception.getMessage();
 		else
 			return "Success";
 	}
 
 	public String getContactName()
 	{
 		return this.contact.getFirstname() + " " + this.contact.getLastname();
 	}
 	
 	public Contact getContact()
 	{
 		return contact;
 	}
 
 	public ArrayList<Tag> getActivityTags()
 	{
 		return this.activityTags;
 	}
 
 }
