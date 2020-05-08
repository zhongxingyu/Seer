 package net.frontlinesms.messaging.mms.email;
 
 import java.util.Collection;
 
 import net.frontlinesms.data.DuplicateKeyException;
 import net.frontlinesms.data.domain.EmailAccount;
 import net.frontlinesms.data.domain.FrontlineMessage;
 import net.frontlinesms.data.repository.EmailAccountDao;
 import net.frontlinesms.email.receive.EmailReceiveProtocol;
 import net.frontlinesms.email.receive.EmailReceiver;
 import net.frontlinesms.events.EventBus;
 import net.frontlinesms.listener.SmsListener;
 import net.frontlinesms.messaging.mms.MmsService;
 import net.frontlinesms.messaging.mms.MmsServiceStatus;
 import net.frontlinesms.messaging.mms.MmsUtils;
 import net.frontlinesms.messaging.mms.events.MmsServiceStatusNotification;
 import net.frontlinesms.mms.MmsMessage;
 import net.frontlinesms.mms.MmsReceiveException;
 import net.frontlinesms.mms.email.receive.EmailMmsReceiver;
 
 public class MmsEmailService implements MmsService {
 	private EmailMmsReceiver mmsEmailReceiver;
 	private EmailAccount emailAccount;
 	private MmsServiceStatus status = MmsEmailServiceStatus.READY;
 
 	public MmsEmailService (EmailAccount emailAccount) {
 		this.setEmailAccount(emailAccount);
 		this.status = (emailAccount.isEnabled() ? MmsEmailServiceStatus.READY : MmsEmailServiceStatus.DISCONNECTED);
 		
 		mmsEmailReceiver = new EmailMmsReceiver();
 		populateReceiver(emailAccount);
 		mmsEmailReceiver.addParsers(MmsUtils.getAllEmailMmsParsers());
 	}
 	
 	/** Update the email receiver with hte settings from an {@link EmailAccount} */
 	public void populateReceiver(EmailAccount emailAccount) {
 		EmailReceiver receiver = new EmailReceiver(this.mmsEmailReceiver);
 		receiver.setHostAddress(emailAccount.getAccountServer());
 		receiver.setHostPassword(emailAccount.getAccountPassword());
 		receiver.setHostPort(emailAccount.getAccountServerPort());
 		receiver.setHostUsername(emailAccount.getAccountName());
 		receiver.setUseSsl(emailAccount.useSsl());
 		receiver.setLastCheck(emailAccount.getLastCheck());
 		receiver.setProtocol(EmailReceiveProtocol.valueOf(emailAccount.getProtocol()));
 		this.mmsEmailReceiver.setReceiver(receiver);
 	}
 
 	public Collection<MmsMessage> receive () throws MmsReceiveException {
 		return this.mmsEmailReceiver.receive();
 	}
 
 	public MmsServiceStatus getStatus() {
 		return this.status;
 	}
 
 	public String getStatusDetail() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public boolean isConnected() {
 		return (!this.status.equals(MmsEmailServiceStatus.DISCONNECTED) && !this.status.equals(MmsEmailServiceStatus.FAILED_TO_CONNECT));
 	}
 
 	public boolean isUseForReceiving() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void sendMMS(FrontlineMessage outgoingMessage) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void setMmsListener(SmsListener smsListener) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void setUseForReceiving(boolean use) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public String getServiceName () {
		return this.mmsEmailReceiver.getReceiver().getHostAddress();
 	}
 	
 	public String getUsername() {
		return this.mmsEmailReceiver.getReceiver().getHostUsername();
 	}
 	
 	public String getProtocol () {
		return this.mmsEmailReceiver.getReceiver().getProtocol().toString();
 	}
 
 	public void setStatus(MmsServiceStatus status, EventBus eventBus) {
 		this.status = status;
 		
 		if (eventBus != null) {
 			eventBus.notifyObservers(new MmsServiceStatusNotification(this, this.status));
 		}
 	}
 
 	public void updateLastCheck(EmailAccountDao emailAccountDao) {
 		long lastCheck = System.currentTimeMillis();
 		
 		this.mmsEmailReceiver.getReceiver().setLastCheck(lastCheck);
 		this.getEmailAccount().setLastCheck(lastCheck);
 		try {
 			emailAccountDao.updateEmailAccount(getEmailAccount());
 		
 		} catch (DuplicateKeyException e) { }
 	}
 
 	public void setEmailAccount(EmailAccount emailAccount) {
 		this.emailAccount = emailAccount;
 	}
 
 	public EmailAccount getEmailAccount() {
 		return emailAccount;
 	}
 
 	public String getServiceIdentification() {
 		return this.getUsername();
 	}
 
 	public boolean isUseForSending() {
 		return false;
 	}
 
 	public String getDisplayPort() {
 		return null;
 	}
 }
