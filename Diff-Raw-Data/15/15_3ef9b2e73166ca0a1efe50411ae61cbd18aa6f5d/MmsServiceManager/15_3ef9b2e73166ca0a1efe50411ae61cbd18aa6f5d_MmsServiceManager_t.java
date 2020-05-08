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
 package net.frontlinesms.messaging.mms;
 
 import java.util.Collection;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import net.frontlinesms.AppProperties;
 import net.frontlinesms.FrontlineUtils;
 import net.frontlinesms.data.DuplicateKeyException;
 import net.frontlinesms.data.domain.EmailAccount;
 import net.frontlinesms.data.domain.FrontlineMultimediaMessage;
 import net.frontlinesms.data.repository.EmailAccountDao;
 import net.frontlinesms.events.EventBus;
 import net.frontlinesms.messaging.mms.email.MmsEmailService;
 import net.frontlinesms.messaging.mms.email.MmsEmailServiceStatus;
 import net.frontlinesms.messaging.mms.events.MmsReceivedNotification;
 import net.frontlinesms.mms.MmsMessage;
 import net.frontlinesms.mms.MmsReceiveException;
 
 import org.apache.log4j.Logger;
 
 /**
  * MmsServiceManager runs as a separate thread.
  * 
  * It handles the discovery of Multimedia Messages available on the e-mail accounts configures by the user, 
  * 
  * INCOMING MESSAGES
  * When a new MMS is filtered in the list of e-mails, an event is triggered through the {@link EventBus}
  * 
  * Incoming messages are immediately removed from accounts with a POP protocol
  * 
  * @author Morgan Belkadi <morgan@frontlinesms.com>
  */
 public class MmsServiceManager extends Thread  {
 	/** Set of {@link MmsEmailService} */
 	private final Set<MmsService> mmsEmailServices = new CopyOnWriteArraySet<MmsService>();
 	/** Flag indicating that the thread should continue running. */
 	private boolean running;
 	private EventBus eventBus;
 	private EmailAccountDao emailAccountDao;
 
 	private static Logger LOG = FrontlineUtils.getLogger(MmsServiceManager.class);
 
 	public MmsServiceManager() {
 		super("MmsServiceManager");
 	}
 	
 	public void setEventBus(EventBus eventBus) {
 		this.eventBus = eventBus;
 	}
 	
 	/**
 	 * Adds a new {@link EmailAccount} which will be used to receive Multimedia Messages
 	 * @param emailAccount
 	 */
 	public synchronized void addMmsEmailReceiver(EmailAccount emailAccount) {
 		MmsEmailService mmsEmailService = new MmsEmailService(emailAccount);
 		
 		mmsEmailServices.add(mmsEmailService);
 	}
 	
 	/**
 	 * Remove the {@link EmailAccount} from the list
 	 * @param emailAccount
 	 */
 	public synchronized void removeMmsEmailReceiver(EmailAccount emailAccount) {
 		for (MmsService mmsEmailService : this.mmsEmailServices) {
 			if (((MmsEmailService)mmsEmailService).getEmailAccount().equals(emailAccount)) {
 				this.mmsEmailServices.remove(mmsEmailService);
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @return All {@link MmsService}s
 	 */
 	public Set<MmsService> getAll () {
 		return this.mmsEmailServices;
 	}
 
 	/**
 	 * Starts the thread.
 	 * Frequently fetches for new {@link FrontlineMultimediaMessage}
 	 */
 	public void run() {
 		LOG.trace("ENTER");
 		running = true;
 		while (running) {
 			processMmsEmailReceiving();
 
 			FrontlineUtils.sleep_ignoreInterrupts(AppProperties.getInstance().getMmsPollingFrequency());
 		}
 		LOG.trace("EXIT");
 	}
 
 	/**
 	 * Flags the internal thread to stop running.
 	 */
 	public void stopRunning() {
 		this.running = false;
 	}
 	
 	/**
 	 * Processes the retrieving of e-mails.
 	 * Automatically sets the status of the {@link MmsEmailService} depending on its state
 	 */
 	private void processMmsEmailReceiving() {
 		for (MmsService mmsService : this.mmsEmailServices) {
 			if (mmsService.isConnected()) {
 				MmsEmailService mmsEmailService = (MmsEmailService)mmsService;
 				try {
 					mmsEmailService.setStatus(MmsEmailServiceStatus.FETCHING, this.eventBus);
 					Collection<MmsMessage> mmsMessages = mmsEmailService.receive();
 					
 					for (MmsMessage mmsMessage : mmsMessages) {
 						if (this.eventBus != null) {
 							// Let's notify the observers that a new MMS has arrived
 							this.eventBus.notifyObservers(new MmsReceivedNotification(mmsMessage));
 						}
 					}
 					mmsEmailService.setStatus(MmsEmailServiceStatus.READY, this.eventBus);
					
					/** Last check update */
					// First, check if the service has not been removed
					if (this.mmsEmailServices.contains(mmsEmailService)) {
						mmsEmailService.updateLastCheck(this.emailAccountDao);
					}
 				} catch (MmsReceiveException e) {
 					mmsEmailService.setStatus(MmsEmailServiceStatus.FAILED_TO_CONNECT, this.eventBus);
 				}
 			}
 		}
 	}
 
 	public void clearMmsEmailReceivers() {
 		this.mmsEmailServices.clear();
 	}
 
 	public void setEmailAccountDao(EmailAccountDao emailAccountDao) {
 		this.emailAccountDao = emailAccountDao;
 	}
 
 	/**
 	 * Connect the {@link MmsEmailService}
 	 * @param mmsEmailService
 	 * @param connect
 	 */
 	public void connectMmsEmailService(MmsEmailService mmsEmailService, boolean connect) {
 		mmsEmailService.setStatus((connect ? MmsEmailServiceStatus.READY : MmsEmailServiceStatus.DISCONNECTED), this.eventBus);
 		EmailAccount emailAccount = mmsEmailService.getEmailAccount();
 		emailAccount.setEnabled(connect);
 		try {
 			this.emailAccountDao.updateEmailAccount(mmsEmailService.getEmailAccount());
 		} catch (DuplicateKeyException e) { }
 	}
 
 	/**
 	 * Update the {@link MmsEmailService}s
 	 */
 	public synchronized void updateMmsEmailService(EmailAccount emailAccount) {
 		for (MmsService mmsService : this.mmsEmailServices) {
 			MmsEmailService mmsEmailService = (MmsEmailService) mmsService;
 			if (mmsEmailService.getEmailAccount().isSameDatabaseEntity(emailAccount)) {
 				mmsEmailService.populateReceiver(emailAccount);
 				mmsEmailService.setStatus((emailAccount.isEnabled() ? MmsEmailServiceStatus.READY : MmsEmailServiceStatus.DISCONNECTED), this.eventBus);
 			}
 		}
 	}
 }
