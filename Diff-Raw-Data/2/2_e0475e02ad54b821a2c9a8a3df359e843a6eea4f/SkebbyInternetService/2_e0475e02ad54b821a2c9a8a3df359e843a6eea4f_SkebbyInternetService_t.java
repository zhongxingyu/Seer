 package net.frontlinesms.messaging.sms.internet;
 
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.smslib.ReceiveNotSupportedException;
 
 import com.skebby.gateways.GetCreditResponse;
 import com.skebby.gateways.Response;
 import com.skebby.gateways.RestApi;
 import com.skebby.gateways.SendSmsResponse;
 import com.skebby.gateways.SkebbyResult;
 
 import net.frontlinesms.FrontlineUtils;
 import net.frontlinesms.data.domain.FrontlineMessage;
 import net.frontlinesms.data.domain.FrontlineMessage.Status;
 import net.frontlinesms.messaging.Provider;
 import net.frontlinesms.messaging.sms.properties.PasswordString;
 import net.frontlinesms.ui.SmsInternetServiceSettingsHandler;
 
 /**
  * Skebby Internet Service - http://www.skebby.com
  * 
  * @author Giancarlo Frison <giancarlo@gfrison.com>
  */
 @Provider(name = "Skebby (beta)", icon = "/icons/smsdevice/internet/servizio_skebby.png")
 public class SkebbyInternetService extends AbstractSmsInternetService {
 
 	
 	/** Prefix attached to every property name. */
 	private static final String PROPERTY_PREFIX = "smsdevice.internet.skebby.";
 
 	protected static final String PROPERTY_USERNAME = PROPERTY_PREFIX + "username";
 	protected static final String PROPERTY_PASSWORD = PROPERTY_PREFIX + "password";
 	protected static final String PROPERTY_CLASSIC = PROPERTY_PREFIX + "classic";
 	protected static final String PROPERTY_SSL = PROPERTY_PREFIX + "ssl";
 
 	private static Logger log = FrontlineUtils.getLogger(SkebbyInternetService.class);
 
 	private RestApi api;
 
 	public String getIdentifier() {
 		return this.getUsername();
 	}
 
 	public boolean isConnected() {
 		return getStatus().equals(SmsInternetServiceStatus.CONNECTED);
 	}
 
 	public boolean isEncrypted() {
 		return getPropertyValue(PROPERTY_SSL, Boolean.class);
 	}
 
 	public String getMsisdn() {
 		return null;
 	}
 
 	public Map<String, Object> getPropertiesStructure() {
 		LinkedHashMap<String, Object> defaultSettings = new LinkedHashMap<String, Object>();
 		defaultSettings.put(PROPERTY_USERNAME, "");
 		defaultSettings.put(PROPERTY_PASSWORD, new PasswordString(""));
 		defaultSettings.put(PROPERTY_CLASSIC, Boolean.FALSE);
 		defaultSettings.put(PROPERTY_SSL, Boolean.FALSE);
 		return defaultSettings;
 	}
 
 	public void setUseForSending(boolean use) {
 	}
 
 	public boolean supportsReceive() {
 		return false;
 	}
 
 	public void setUseForReceiving(boolean use) {
 		throw new ReceiveNotSupportedException();
 	}
 
 	public boolean isBinarySendingSupported() {
 		return false;
 	}
 
 	public boolean isUcs2SendingSupported() {
 		return false;
 	}
 
 	public String getServiceName() {
 		return this.getUsername() + UI_NAME_SEPARATOR + SmsInternetServiceSettingsHandler.getProviderName(getClass());
 	}
 
 	public boolean isUseForReceiving() {
 		return false;
 	}
 
 	public boolean isUseForSending() {
 		return true;
 	}
 
 	public String getDisplayPort() {
 		return null;
 	}
 
 	@Override
 	protected void init() throws SmsInternetServiceInitialisationException {
 		api = new RestApi(getUsername(), getPassword());
 		api.setUseSSL(getPropertyValue(PROPERTY_SSL, Boolean.class));
 		try {
 			GetCreditResponse res = api.credit();
 			if(!res.getResult().equals(SkebbyResult.success))
 				setStatus(res.getResponse());
 			else if(res.getCredit_left()<=0) {
 				log.info("low credit Skebby service user:"+getUsername());
 				setStatus(SmsInternetServiceStatus.LOW_CREDIT, null);
 			} else {
 				log.info("connected to Skebby service user:"+getUsername());
 				setStatus(SmsInternetServiceStatus.CONNECTED, null);
 			}
 			
		} catch (Exception e) {
 			log.warn("Could not connect Skebby", e);
 			setStatus(SmsInternetServiceStatus.FAILED_TO_CONNECT, e.getMessage());
 			throw new SmsInternetServiceInitialisationException(e);
 		}
 	}
 
 	@Override
 	protected void deinit() {
 
 	}
 
 	@Override
 	protected void sendSmsDirect(FrontlineMessage message) {
 		try {
 			if(!isConnected()) {
 				message.setStatus(Status.FAILED);
 				return;
 			}
 			SendSmsResponse res = api.sendSms(!getPropertyValue(PROPERTY_CLASSIC, Boolean.class), message.getRecipientMsisdn(), message.getTextContent(), null);
 			if(res.getResult().equals(SkebbyResult.success))
 				message.setStatus(Status.DELIVERED);
 			else {
 				message.setStatus(Status.FAILED);
 				setStatus(res.getResponse());
 			}
 		} catch (Throwable e) {
 			message.setStatus(Status.FAILED);
 		} finally {
 			if (smsListener != null) 
 				smsListener.outgoingMessageEvent(this, message);
 		}
 	}
 
 	protected void setStatus(Response skebbyResponse) {
 		if(skebbyResponse==null) {
 			setStatus(SmsInternetServiceStatus.CONNECTED, null);
 			return;
 		}
 		switch (skebbyResponse .getCode()) {
 			case 30://low credit 
 				setStatus(SmsInternetServiceStatus.LOW_CREDIT, skebbyResponse.getMessage());
 				break;
 			case 10://generic error 
 				setStatus(SmsInternetServiceStatus.FAILED_TO_CONNECT, skebbyResponse.getMessage());
 				break;
 			case 21://wrong credentials 
 				setStatus(SmsInternetServiceStatus.FAILED_TO_CONNECT, skebbyResponse.getMessage());
 				break;
 			default:
 				setStatus(SmsInternetServiceStatus.CONNECTED, skebbyResponse.getMessage());
 				break;
 		}
 	}
 
 	@Override
 	protected void receiveSms() throws SmsInternetServiceReceiveException {
 
 	}
 	
 
 	/**
 	 * @return The property value of {@value #PROPERTY_USERNAME}
 	 */
 	private String getUsername() {
 		return getPropertyValue(PROPERTY_USERNAME, String.class);
 	}
 
 	/**
 	 * @return The property value of {@value #PROPERTY_PASSWORD}
 	 */
 	private String getPassword() {
 		return getPropertyValue(PROPERTY_PASSWORD, PasswordString.class).getValue();
 	}
 
 }
