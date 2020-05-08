 package au.org.scoutmaster.util;
 
 import java.io.Closeable;
 import java.io.IOException;
 
 import org.apache.log4j.Logger;
 import org.marre.SmsSender;
 import org.marre.sms.SmsException;
 
 import au.org.scoutmaster.application.SMSession;
 import au.org.scoutmaster.dao.ActivityDao;
 import au.org.scoutmaster.dao.ActivityTypeDao;
 import au.org.scoutmaster.dao.ContactDao;
 import au.org.scoutmaster.dao.DaoFactory;
 import au.org.scoutmaster.domain.Activity;
 import au.org.scoutmaster.domain.ActivityType;
 import au.org.scoutmaster.domain.Phone;
 import au.org.scoutmaster.domain.SMSProvider;
 import au.org.scoutmaster.domain.Tag;
 import au.org.scoutmaster.domain.access.User;
 import au.org.scoutmaster.views.wizards.bulkSMS.SMSTransmission;
 
 import com.google.gwt.thirdparty.guava.common.base.Preconditions;
 import com.vaadin.ui.Notification.Type;
 
 public class SMSSession implements Closeable
 {
 	static private Logger logger = Logger.getLogger(SMSSession.class);
 
 	private SmsSender smsSender;
 
 	public SMSSession(SMSProvider provider) throws SmsException, IOException
 	{
 		smsSender = SmsSender.getClickatellSender(provider.getUsername(), provider.getPassword(), provider.getApiId());
 		Preconditions.checkNotNull(smsSender);
 		smsSender.connect();
 	}
 
 	@Override
 	public void close() throws IOException
 	{
 		if (smsSender != null)
 			try
 			{
 				smsSender.disconnect();
 			}
 			catch (SmsException e)
 			{
 				logger.error(e, e);
 			}
 
 	}
 
 	public void send(SMSTransmission transmission) throws SmsException, IOException
 	{
 		User user = (User) SMSession.INSTANCE.getLoggedInUser();
 
 		try
 		{
 			// The message that you want to send.
 			String msg = transmission.getMessage().expandBody(user, transmission.getContact()).toString();
 
 			// International number to target without leading "+"
 			Phone reciever = transmission.getRecipient();
 
 			// Number of sender (not supported on all transports)
 			smsSender.sendTextSms(msg, reciever.getPhoneNo().replaceAll("\\s", ""), transmission.getMessage()
 					.getSender().getPhoneNo());
 
 			// Log the activity
 			ActivityDao daoActivity = new DaoFactory().getActivityDao();
 			ActivityTypeDao daoActivityType = new DaoFactory().getActivityTypeDao();
 			Activity activity = new Activity();
 			activity.setAddedBy(user);
 			activity.setWithContact(transmission.getContact());
 			activity.setSubject(transmission.getMessage().getSubject());
 			activity.setType(daoActivityType.findByName(ActivityType.BULK_SMS));
 			activity.setDetails(transmission.getMessage().getBody());
 			daoActivity.persist(activity);
 			
 			
 			// Tag the contact
 			ContactDao daoContact = new DaoFactory().getContactDao();
 			for (Tag tag : transmission.getActivityTags())
 			{
 				daoContact.attachTag(transmission.getContact(), tag);
 			}
 
 		}
 		catch (VelocityFormatException e)
 		{
 			SMNotification.show(e, Type.ERROR_MESSAGE);
 		}
 
 	}
 }
