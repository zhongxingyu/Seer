 /*
  * FrontlineSMS <http://www.frontlinesms.com>
  * Copyright 2007, 2008 kiwanja
  * 
  * This file is part of FrontlineSMS.
  * 
  * FrontlineSMS is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or (at
  * your option) any later version.
  * 
  * FrontlineSMS is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with FrontlineSMS. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.frontlinesms;
 
 import java.io.File;
 import java.util.*;
 
 import net.frontlinesms.data.*;
 import net.frontlinesms.data.domain.*;
 import net.frontlinesms.data.domain.Message.Type;
 import net.frontlinesms.data.repository.*;
 import net.frontlinesms.events.EventBus;
 import net.frontlinesms.listener.*;
 import net.frontlinesms.plugins.PluginController;
 import net.frontlinesms.plugins.PluginControllerProperties;
 import net.frontlinesms.plugins.PluginProperties;
 import net.frontlinesms.resources.ResourceUtils;
 import net.frontlinesms.smsdevice.*;
 import net.frontlinesms.smsdevice.internet.SmsInternetService;
 
 import org.apache.log4j.Logger;
 import org.smslib.CIncomingMessage;
 import org.springframework.beans.MutablePropertyValues;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.config.ListFactoryBean;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.beans.factory.support.GenericBeanDefinition;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ConfigurableApplicationContext;
 import org.springframework.context.support.FileSystemXmlApplicationContext;
 import org.springframework.context.support.StaticApplicationContext;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.dao.DataAccessResourceFailureException;
 
 /**
  * 
  * FrontlineSMS - an SMS gateway in a box.
  * 
  * Built for the not-for-profit sector, to provide 
  * -> Group messaging
  * -> Auto-responders
  * -> SMS data gathering
  * 
  * This system is built to provide out of the box auto-detection of standard GSM phones for
  * sending messages, and also to use a built-in or external database for storing the results.
  * 
  * The architecture is built so that it can be easily extended.
  * 
  * The default usage is as a desktop application, looking after the phones and providing the 
  * GUI interface at the same time, but it should be straightforward to separate it into a 
  * server and client interface, or even a web driven interface if that would be required.
  * 
  * The architecture is as follows:
  * 
  * FrontlineSMS.java - starts the application and passes messages and events around the system.
  * SmsHandler.java - runs as a separate thread, will create and manage GSM phone handlers and also internet SMS messaging centres
  *  
  * see {@link "http://www.frontlinesms.net"} for more details. 
  * copyright owned by Kiwanja.net
  * 
  * @author Ben Whitaker ben(at)masabi(dot)com
  * @author Alex Anderson alex(at)masabi(dot)com
  */
 public class FrontlineSMS implements SmsSender, SmsListener, EmailListener {
 	/** Logging object */
 	private static Logger LOG = Utils.getLogger(FrontlineSMS.class);
 	/** SMS device emulator */
 	public static final SmsDevice EMULATOR = new DummySmsDevice(FrontlineSMSConstants.EMULATOR_MSISDN);
 	
 //> INSTANCE VARIABLES
 
 //> DATA ACCESS OBJECTS
 	/** Data Access Object for {@link Keyword}s */
 	private KeywordDao keywordDao;
 	/** Data Access Object for {@link Group}s */
 	private GroupDao groupDao;
 	/** Data Access Object for {@link Group}s */
 	private GroupMembershipDao groupMembershipDao;
 	/** Data Access Object for {@link Contact}s */
 	private ContactDao contactDao;
 	/** Data Access Object for {@link Message}s */
 	private MessageDao messageDao;
 	/** Data Access Object for {@link KeywordAction}s */
 	private KeywordActionDao keywordActionDao;
 	/** Data Access Object for {@link SmsModemSettings} */
 	private SmsModemSettingsDao smsModemSettingsDao;
 	/** Data Access Object for {@link SmsInternetServiceSettings} */
 	private SmsInternetServiceSettingsDao smsInternetServiceSettingsDao;
 	/** Data Access Object for {@link EmailAccount}s */
 	private EmailAccountDao emailAccountDao;
 	/** Data Access Object for {@link Email}s */
 	private EmailDao emailDao;
 	
 //> SERVICE MANAGERS
 	/** Class that handles sending of email messages */
 	private EmailServerHandler emailServerManager;
 	/** Manager of SMS devices */
 	private SmsDeviceManager smsDeviceManager;
 	/** Asynchronous processor of received messages. */
 	private IncomingMessageProcessor incomingMessageProcessor;
 	private PluginManager pluginManager;
 
 	//> EVENT LISTENERS
 	/** Listener for email events */
 	private EmailListener emailListener;
 	/** Listener for UI-related events */
 	private UIListener uiListener;
 	/** Listener for {@link SmsDevice} events. */
 	private SmsDeviceEventListener smsDeviceEventListener;
 	/** Main {@link EventBus} through which all core events should be channelled. */
 	private EventBus eventBus;
 	
 //> INITIALISATION METHODS
 	/** The application context describing dependencies of the application. */
 	private ConfigurableApplicationContext applicationContext;
 	
 	/** Initialise {@link #applicationContext}. */
 	public void initApplicationContext() throws DataAccessResourceFailureException {
 		// Load the data mode from the app.properties file
 		AppProperties appProperties = AppProperties.getInstance();
 		
 		LOG.info("Load Spring/Hibernate application context to initialise DAOs");
 			
 		// Create a base ApplicationContext defining the hibernate config file we need to import
 		StaticApplicationContext baseApplicationContext = new StaticApplicationContext();
 		baseApplicationContext.registerBeanDefinition("hibernateConfigLocations", createHibernateConfigLocationsBeanDefinition());
 		
 		// Get the spring config locations
 		String databaseExternalConfigPath = ResourceUtils.getConfigDirectoryPath() + ResourceUtils.PROPERTIES_DIRECTORY_NAME + File.separatorChar + appProperties.getDatabaseConfigPath();
 		String[] configLocations = getSpringConfigLocations(databaseExternalConfigPath);
 		baseApplicationContext.refresh();
 		
 		FileSystemXmlApplicationContext applicationContext = new FileSystemXmlApplicationContext(configLocations, false, baseApplicationContext);
 		this.applicationContext = applicationContext;
 		
 		// Add post-processor to handle substituted database properties
 		PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
 		String databasePropertiesPath = ResourceUtils.getConfigDirectoryPath() + ResourceUtils.PROPERTIES_DIRECTORY_NAME + File.separatorChar + appProperties.getDatabaseConfigPath() + ".properties";
 		propertyPlaceholderConfigurer.setLocation(new FileSystemResource(new File(databasePropertiesPath)));
 		propertyPlaceholderConfigurer.setIgnoreResourceNotFound(true);
 		applicationContext.addBeanFactoryPostProcessor(propertyPlaceholderConfigurer);
 		applicationContext.refresh();
 		
 		LOG.info("Context loaded successfully.");
 		
 		this.pluginManager = new PluginManager(this, applicationContext);
 		
 		LOG.info("Getting DAOs from application context...");
 		groupDao = (GroupDao) applicationContext.getBean("groupDao");
 		groupMembershipDao = (GroupMembershipDao) applicationContext.getBean("groupMembershipDao");
 		contactDao = (ContactDao) applicationContext.getBean("contactDao");
 		keywordDao = (KeywordDao) applicationContext.getBean("keywordDao");
 		keywordActionDao = (KeywordActionDao) applicationContext.getBean("keywordActionDao");
 		messageDao = (MessageDao) applicationContext.getBean("messageDao");
 		emailDao = (EmailDao) applicationContext.getBean("emailDao");
 		emailAccountDao = (EmailAccountDao) applicationContext.getBean("emailAccountDao");
 		smsInternetServiceSettingsDao = (SmsInternetServiceSettingsDao) applicationContext.getBean("smsInternetServiceSettingsDao");
 		smsModemSettingsDao = (SmsModemSettingsDao) applicationContext.getBean("smsModemSettingsDao");
 		eventBus = (EventBus) applicationContext.getBean("eventBus");
 	}
 	
 	/** Deinitialise {@link #applicationContext}. */
 	public void deinitApplicationContext() {
 		this.applicationContext.close();
 	}
 	
 	/**
 	 * Start services for the {@link FrontlineSMS} instance.  N.B. this must be called <strong>after</strong> {@link #initApplicationContext()}
 	 * This method should be called only once before the services are stopped using {@link #stopServices()}.
 	 */
 	public void startServices() {
 		try {
 			LOG.debug("Creating blank keyword...");
 			Keyword blankKeyword = new Keyword("", "Blank keyword, used to be triggerd by every received message.[i18n]");
 			keywordDao.saveKeyword(blankKeyword);
 			LOG.debug("Blank keyword created.");
 		} catch (DuplicateKeyException e) {
 			// Looks like this has been created already, so ignore the exception
 			LOG.debug("Blank keyword creation failed - already exists.");
 		}
 		
 		LOG.debug("Initialising email server handler...");
 		emailServerManager = new EmailServerHandler();
 		emailServerManager.setEmailListener(this);
 
 		LOG.debug("Initialising incoming message processor...");
 		// Initialise the incoming message processor
 		incomingMessageProcessor = new IncomingMessageProcessor(this);
 		incomingMessageProcessor.start();
 		
 		LOG.debug("Starting Phone Manager...");
 		smsDeviceManager = new SmsDeviceManager();
 		smsDeviceManager.setSmsListener(this);
 		smsDeviceManager.setEventBus(getEventBus());
		smsDeviceManager.listComPortsAndOwners(false);
 		smsDeviceManager.start();
 
 		initSmsInternetServices();
 		
 		this.pluginManager.initPluginControllers();
 
 		LOG.debug("Starting E-mail Manager...");
 		emailServerManager.start();
 
 		LOG.debug("Re-Loading messages to outbox.");
 		//We need to reload all messages, which status is OUTBOX, to the outbox.
 		for (Message m : messageDao.getMessages(Type.TYPE_OUTBOUND, new Integer[] { Message.STATUS_OUTBOX, Message.STATUS_PENDING})) {
 			smsDeviceManager.sendSMS(m);
 		}
 
 		LOG.debug("Re-Loading e-mails to outbox.");
 		//We need to reload all email, which status is RETRYING, to the outbox.
 		for (Email m : emailDao.getEmailsForStatus(new Email.Status[] {Email.Status.RETRYING, Email.Status.PENDING, Email.Status.OUTBOX})) {
 			emailServerManager.sendEmail(m);
 		}
 	}
 	
 	private void stopServices() {
 		// de-initialise plugin controllers
 		if(this.pluginManager != null) {
 			for(PluginController pluginController : this.pluginManager.getPluginControllers()) {
 				pluginController.deinit();
 			}
 		}
 		
 		if (smsDeviceManager != null) {
 			LOG.debug("Stopping Phone Manager...");
 			smsDeviceManager.stopRunning();
 		}
 		if (emailServerManager != null) {
 			LOG.debug("Stopping E-mail Manager...");
 			emailServerManager.stopRunning();
 		}
 		if(this.incomingMessageProcessor != null) {
 			LOG.debug("Stopping the incoming message processor...");
 			this.incomingMessageProcessor.die();
 		}
 	}
 	
 	/**
 	 * Shutdown and re-initialise the {@link #applicationContext}.
 	 * TODO it has not been confirmed this method exectutes cleanly, so it should be used with caution.
 	 */
 	public void reboot() {
 		deinitApplicationContext();
 		stopServices();
 		initApplicationContext();
 		startServices();
 	}
 	
 	/** Initialise {@link SmsInternetService}s. */
 	private void initSmsInternetServices() {
 		for (SmsInternetServiceSettings settings : this.smsInternetServiceSettingsDao.getSmsInternetServiceAccounts()) {
 			String className = settings.getServiceClassName();
 			LOG.info("Initializing SmsInternetService of class: " + className);
 			try {
 				SmsInternetService service = (SmsInternetService) Class.forName(className).newInstance();
 				service.setSettings(settings);
 				this.smsDeviceManager.addSmsInternetService(service);
 			} catch (Throwable t) {
 				LOG.warn("Unable to initialize SmsInternetService of class: " + className, t);
 			}
 		}
 	}
 	
 	/**
 	 * Create the configLocations bean for the Hibernate config.
 	 * This method should only be called from within {@link FrontlineSMS#initApplicationContext()}
 	 * @return {@link BeanDefinition} containing details of the hibernate config for the app and its plugins.
 	 */
 	private BeanDefinition createHibernateConfigLocationsBeanDefinition() {
 		// Initialise list of hibernate config files
 		List<String> hibernateConfigList = new ArrayList<String>();
 		// Add main hibernate config location
 		hibernateConfigList.add("classpath:frontlinesms.hibernate.cfg.xml");
 		// Add hibernate config locations for plugins
 		for(Class<PluginController> pluginClass : PluginProperties.getInstance().getPluginClasses()) {
 			LOG.info("Processing plugin class: " + pluginClass.getName());
 			if(pluginClass.isAnnotationPresent(PluginControllerProperties.class)) {
 				PluginControllerProperties properties = pluginClass.getAnnotation(PluginControllerProperties.class);
 				String pluginHibernateLocation = properties.hibernateConfigPath();
 				if(!PluginControllerProperties.NO_VALUE.equals(pluginHibernateLocation)) {
 					hibernateConfigList.add(pluginHibernateLocation);
 				}
 			}
 		}
 		
 		GenericBeanDefinition myBeanDefinition = new GenericBeanDefinition();
 		myBeanDefinition.setBeanClass(ListFactoryBean.class);
 		MutablePropertyValues values = new MutablePropertyValues();
 		values.addPropertyValue("sourceList", hibernateConfigList);
 		myBeanDefinition.setPropertyValues(values);
 		
 		return myBeanDefinition;
 	}
 	
 	/**
 	 * Gets a list of configLocations required for initialising Spring {@link ApplicationContext}.
 	 * This method should only be called from within {@link FrontlineSMS#initApplicationContext()}
 	 * @param externalConfigPath Spring path to the external Spring database config file 
 	 * @return list of configLocations used for initialising {@link ApplicationContext}
 	 */
 	private String[] getSpringConfigLocations(String...externalConfigPaths) {
 		ArrayList<String> configLocations = new ArrayList<String>();
 		
 		// Custom Spring configurations
 		for(String externalConfigPath : externalConfigPaths) {
 			LOG.info("Loading spring application context from: " + externalConfigPath);
 			configLocations.add("file:" + externalConfigPath);
 		}
 		
 		// Main Spring configuration
 		configLocations.add("classpath:frontlinesms-spring-hibernate.xml");
 		
 		// Add config locations for plugins
 		for(Class<PluginController> pluginControllerClass : PluginProperties.getInstance().getPluginClasses()) {
 			if(pluginControllerClass.isAnnotationPresent(PluginControllerProperties.class)) {
 				PluginControllerProperties properties = pluginControllerClass.getAnnotation(PluginControllerProperties.class);
 				String pluginConfigLocation = properties.springConfigLocation();
 				if(!PluginControllerProperties.NO_VALUE.equals(pluginConfigLocation)) {
 					LOG.trace("Adding plugin Spring config location: " + pluginConfigLocation);
 					configLocations.add(pluginConfigLocation);
 				}
 			}
 		}
 		
 		return configLocations.toArray(new String[configLocations.size()]);
 	}
 	
 	/**
 	 * This method makes the phone manager thread stop.
 	 */
 	public void destroy() {
 		LOG.trace("ENTER");
 		stopServices();
 		deinitApplicationContext();
 		LOG.trace("EXIT");
 	}
 	
 //> EVENT HANDLER METHODS
 	/** Called by the SmsHandler when an SMS message is received. */
 	public synchronized void incomingMessageEvent(SmsDevice receiver, CIncomingMessage incomingMessage) {
 		this.incomingMessageProcessor.queue(receiver, incomingMessage);
 	}
 
 	/** Passes an outgoing message event to the SMS Listener if one is specified. */
 	public synchronized void outgoingMessageEvent(SmsDevice sender, Message outgoingMessage) {
 		// The message status will have changed, so save it here
 		this.messageDao.updateMessage(outgoingMessage);
 		
 		// FIXME should log this message here
 		if (uiListener != null) {
 			uiListener.outgoingMessageEvent(outgoingMessage);
 		}
 	}
 
 	/** Passes a device event to the SMS Listener if one is specified. */
 	public void smsDeviceEvent(SmsDevice activeDevice, SmsDeviceStatus status) {
 		// FIXME these events MUST be queued and processed on a separate thread
 		// FIXME should log this message here
 		if (this.smsDeviceEventListener != null) {
 			this.smsDeviceEventListener.smsDeviceEvent(activeDevice, status);
 		}
 	}
 
 	/** Passes an outgoing email event to {@link #emailListener} if it is defined */
 	public synchronized void outgoingEmailEvent(EmailSender sender, Email email) {
 		// The email status will have changed, so save it here
 		this.emailDao.updateEmail(email);
 		
 		if (emailListener != null) {
 			emailListener.outgoingEmailEvent(sender, email);
 		}
 	}
 
 //> SMS SEND METHODS
 	/** Persists and sends an SMS message. */
 	public void sendMessage(Message message) {
 		messageDao.saveMessage(message);
 		smsDeviceManager.sendSMS(message);
 		if (uiListener != null) { 
 			uiListener.outgoingMessageEvent(message);
 		}
 	}
 	
 	/**
 	 * Sends an SMS using the phoneManager in the standard way.  The only advantage this
 	 * method provides over using the phoneManager is that this will redirect emulator
 	 * messages in the correct manner.
 	 * 
 	 * @param targetNumber The recipient number.
 	 * @param textContent The message to be sent.
 	 * @return the {@link Message} describing the sent message
 	 */
 	public Message sendTextMessage(String targetNumber, String textContent) {
 		LOG.trace("ENTER");
 		Message m;
 		if (targetNumber.equals(FrontlineSMSConstants.EMULATOR_MSISDN)) {
 			m = Message.createOutgoingMessage(System.currentTimeMillis(), FrontlineSMSConstants.EMULATOR_MSISDN, FrontlineSMSConstants.EMULATOR_MSISDN, textContent.trim());
 			m.setStatus(Message.STATUS_SENT);
 			messageDao.saveMessage(m);
 			outgoingMessageEvent(EMULATOR, m);
 			incomingMessageEvent(EMULATOR, new CIncomingMessage(System.currentTimeMillis(), FrontlineSMSConstants.EMULATOR_MSISDN, textContent.trim(), 1, "NYI"));
 		} else {
 			m = Message.createOutgoingMessage(System.currentTimeMillis(), "", targetNumber, textContent.trim());
 			this.sendMessage(m);
 		}
 		LOG.trace("EXIT");
 		return m;
 	}
 	
 //> ACCESSOR METHODS
 	/** @return {@link #contactDao} */
 	public ContactDao getContactDao() {
 		return this.contactDao;
 	}
 	/** @return {@link #groupDao} */
 	public GroupDao getGroupDao() {
 		return this.groupDao;
 	}
 	/** @return {@link #groupMembershipDao} */
 	public GroupMembershipDao getGroupMembershipDao() {
 		return this.groupMembershipDao;
 	}
 	/** @return {@link #messageDao} */
 	public MessageDao getMessageDao() {
 		return this.messageDao;
 	}
 	/** @return {@link #keywordDao} */
 	public KeywordDao getKeywordDao() {
 		return this.keywordDao;
 	}
 	/** @return {@link #keywordActionDao} */
 	public KeywordActionDao getKeywordActionDao() {
 		return this.keywordActionDao;
 	}
 	/** @return {@link #smsModemSettingsDao} */
 	public SmsModemSettingsDao getSmsModemSettingsDao() {
 		return smsModemSettingsDao;
 	}
 	/** @return a new instance of {@link StatisticsManager} */
 	public StatisticsManager getStatisticsManager() {
 		return (StatisticsManager) applicationContext.getBean("statisticsManager");
 	}
 	/** @return {@link #emailAccountDao} */
 	public EmailAccountDao getEmailAccountFactory() {
 		return emailAccountDao;
 	}
 	/** @return {@link #emailDao} */
 	public EmailDao getEmailDao() {
 		return emailDao;
 	}
 	/** @return {@link #eventBus} */
 	public EventBus getEventBus() {
 		return eventBus;
 	}
 	/** @return {@link #emailServerManager} */
 	public EmailServerHandler getEmailServerHandler() {
 		return emailServerManager;
 	}
 	/** @return {@link #pluginManager} */
 	public PluginManager getPluginManager() {
 		return pluginManager;
 	}
 	
 	/** @return {@link #uiListener} */
 	public UIListener getUiListener() {
 		return uiListener;
 	}
 	/** @param uiListener new value for {@link #uiListener} */
 	public void setUiListener(UIListener uiListener) {
 		this.uiListener = uiListener;
 		this.incomingMessageProcessor.setUiListener(uiListener);
 	}
 	
 	/** @param smsDeviceEventListener new value for {@link #smsDeviceEvent(SmsDevice, SmsDeviceStatus)} */
 	public void setSmsDeviceEventListener(SmsDeviceEventListener smsDeviceEventListener) {
 		this.smsDeviceEventListener = smsDeviceEventListener;
 	}
 	
 	/** @return {@link #smsDeviceManager} */
 	public SmsDeviceManager getSmsDeviceManager() {
 		return this.smsDeviceManager;
 	}
 
 	/** @param emailListener new value for {@link #emailListener} */
 	public void setEmailListener(EmailListener emailListener) {
 		this.emailListener = emailListener;
 	}
 	
 	/**
 	 * Adds another {@link IncomingMessageListener} to {@link IncomingMessageProcessor}.
 	 * @param incomingMessageListener new {@link IncomingMessageListener}
 	 * @see IncomingMessageProcessor#addIncomingMessageListener(IncomingMessageListener)
 	 */
 	public void addIncomingMessageListener(IncomingMessageListener incomingMessageListener) {
 		this.incomingMessageProcessor.addIncomingMessageListener(incomingMessageListener);
 	}
 	
 	/**
 	 * Removes a {@link IncomingMessageListener} from {@link IncomingMessageProcessor}.
 	 * @param incomingMessageListener {@link IncomingMessageListener} to be removed
 	 * @see IncomingMessageProcessor#removeIncomingMessageListener(IncomingMessageListener)
 	 */
 	public void removeIncomingMessageListener(IncomingMessageListener incomingMessageListener) {
 		this.incomingMessageProcessor.removeIncomingMessageListener(incomingMessageListener);
 	}
 
 	/** @return {@link #smsInternetServiceSettingsDao} */
 	public SmsInternetServiceSettingsDao getSmsInternetServiceSettingsDao() {
 		return this.smsInternetServiceSettingsDao;
 	}
 
 	/** @return {@link #smsDeviceManager}'s {@link SmsInternetService}s */
 	public Collection<SmsInternetService> getSmsInternetServices() {
 		return this.smsDeviceManager.getSmsInternetServices();
 	}
 
 	public boolean shouldLaunchStatsCollection() {
 		return true;
 //		long dateLastStatisticsSubmit = AppProperties.getInstance().getLastStatisticsPromptDate();
 //		long numberOfDaysSinceLastSubmit = (System.currentTimeMillis() - dateLastStatisticsSubmit) / FrontlineSMSConstants.MILLIS_PER_DAY;
 //		return numberOfDaysSinceLastSubmit >= FrontlineSMSConstants.STATISTICS_DAYS_BEFORE_RELAUNCH;
 	}
 }
