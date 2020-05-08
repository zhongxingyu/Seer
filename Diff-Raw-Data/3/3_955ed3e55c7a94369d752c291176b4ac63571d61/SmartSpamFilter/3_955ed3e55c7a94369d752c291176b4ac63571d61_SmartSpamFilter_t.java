 package ezhun.smsb.filter;
 
 import android.content.ContentResolver;
 import ezhun.smsb.SmsPojo;
 import ezhun.smsb.exceptions.ApplicationException;
 
 /**
  * Chain of spam filters. Pretends to be smart.
  */
 public class SmartSpamFilter implements ISpamFilter {
 	ContentResolver resolver;
 
 	public SmartSpamFilter(ContentResolver resolver) {
 		this.resolver = resolver;
 	}
 
 	public ContentResolver getContentResolver() {
 		return this.resolver;
 	}
 
 	@Override
 	public boolean isSpam(SmsPojo message) throws ApplicationException {
 		ISpamFilter contactSpamFilter = new ContactSpamFilter(getContentResolver());
 		ISpamFilter whiteListSpamFilter = new WhiteListSpamFilter(getContentResolver());
 
		return contactSpamFilter.isSpam(message) || whiteListSpamFilter.isSpam(message);
 	}
 }
