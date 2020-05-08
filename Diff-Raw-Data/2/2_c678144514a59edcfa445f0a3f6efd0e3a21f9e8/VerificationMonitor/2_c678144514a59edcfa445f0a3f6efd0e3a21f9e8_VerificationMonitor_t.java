 package eu.gloria.gs.services.api.mail;
 
 import java.util.Date;
 import java.util.List;
 
 import eu.gloria.gs.services.api.data.UserDataAdapter;
 import eu.gloria.gs.services.api.data.dbservices.UserDataAdapterException;
 import eu.gloria.gs.services.api.data.dbservices.UserVerificationEntry;
 import eu.gloria.gs.services.core.ErrorLogEntry;
 import eu.gloria.gs.services.core.InfoLogEntry;
 import eu.gloria.gs.services.core.LogEntry;
 import eu.gloria.gs.services.core.LogStore;
 import eu.gloria.gs.services.core.WarningLogEntry;
 import eu.gloria.gs.services.core.client.GSClientProvider;
 import eu.gloria.gs.services.core.tasks.ServerThread;
 import eu.gloria.gs.services.log.action.LogAction;
 
 public class VerificationMonitor extends ServerThread {
 
 	private UserDataAdapter adapter;
 	private LogStore logStore;
 	private String username;
 	private String password;
 	private SendMailSSL mailSender;
 	private static boolean analyzeWaitingOnes = true;
 	private static int waitingCount = 0;
 	private static int MS_PER_DAY = 86400000;
 
 	/**
 	 * @param name
 	 */
	public VerificationMonitor() {
 		super(VerificationMonitor.class.getSimpleName());
 	}
 
 	public void setAdapter(UserDataAdapter adapter) {
 		this.adapter = adapter;
 	}
 
 	public void setLogStore(LogStore logStore) {
 		this.logStore = logStore;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public SendMailSSL getMailSender() {
 		return mailSender;
 	}
 
 	public void setMailSender(SendMailSSL mailSender) {
 		this.mailSender = mailSender;
 	}
 
 	@Override
 	protected void doWork() {
 
 		try {
 			Thread.sleep(5000);
 
 			if (waitingCount < 1) {
 				waitingCount++;
 			} else {
 				analyzeWaitingOnes = true;
 			}
 		} catch (InterruptedException e) {
 			log.warn(e.getMessage());
 		}
 
 		GSClientProvider.setCredentials(this.username, this.password);
 
 		try {
 			List<UserVerificationEntry> remaining = adapter
 					.getPendingVerifications();
 
 			for (UserVerificationEntry entry : remaining) {
 				this.mailSender.sendNotification(entry.getEmail(),
 						entry.getAlias(), entry.getCode());
 				this.adapter.setVerificationSent(entry.getAlias());
 			}
 		} catch (UserDataAdapterException e) {
 		} catch (Exception e) {
 		}
 
 		try {
 			List<UserVerificationEntry> resets = adapter
 					.getPendingResetRequests();
 
 			for (UserVerificationEntry entry : resets) {
 				this.mailSender.sendReset(entry.getEmail(), entry.getAlias(),
 						entry.getCode(), entry.getNewPassword());
 				this.adapter.setWaitForReset(entry.getAlias());
 			}
 		} catch (UserDataAdapterException e) {
 		} catch (Exception e) {
 		}
 
 		try {
 			List<UserVerificationEntry> resets = adapter
 					.getPendingChangePasswordRequests();
 
 			for (UserVerificationEntry entry : resets) {
 				this.mailSender.sendChangePassword(entry.getEmail(),
 						entry.getAlias(), entry.getCode(),
 						entry.getNewPassword());
 				this.adapter.setWaitForChangePassword(entry.getAlias());
 			}
 		} catch (UserDataAdapterException e) {
 		} catch (Exception e) {
 		}
 
 		if (analyzeWaitingOnes) {
 			analyzeWaitingOnes = false;
 
 			Date now = new Date();
 
 			try {
 
 				List<UserVerificationEntry> pendingResets = adapter
 						.getPendingResetRequests();
 
 				for (UserVerificationEntry entry : pendingResets) {
 					if (now.getTime() - entry.getSentDate().getTime() > MS_PER_DAY) {
 						adapter.setVerificationObsolete(entry.getAlias());
 					}
 				}
 			} catch (UserDataAdapterException e) {
 			} catch (Exception e) {
 			}
 
 			try {
 
 				List<UserVerificationEntry> waitingResets = adapter
 						.getWaitingResetRequests();
 
 				for (UserVerificationEntry entry : waitingResets) {
 					if (now.getTime() - entry.getResetRequestDate().getTime() > MS_PER_DAY) {
 						adapter.setResetObsolete(entry.getAlias());
 					}
 				}
 
 			} catch (UserDataAdapterException e) {
 			} catch (Exception e) {
 			}
 
 			try {
 
 				List<UserVerificationEntry> waitingResets = adapter
 						.getWaitingChangePasswordRequests();
 
 				for (UserVerificationEntry entry : waitingResets) {
 					if (now.getTime() - entry.getChPassRequestDate().getTime() > MS_PER_DAY) {
 						adapter.setChangePasswordObsolete(entry.getAlias());
 					}
 				}
 
 			} catch (UserDataAdapterException e) {
 			} catch (Exception e) {
 			}
 		}
 	}
 
 	private void processLogEntry(LogEntry entry, LogAction action) {
 		entry.setUsername(this.username);
 		entry.setDate(new Date());
 
 		entry.setAction(action);
 		this.logStore.addEntry(entry);
 	}
 
 	private void logError(LogAction action) {
 
 		LogEntry entry = new ErrorLogEntry();
 		this.processLogEntry(entry, action);
 	}
 
 	private void logInfo(LogAction action) {
 
 		LogEntry entry = new InfoLogEntry();
 		this.processLogEntry(entry, action);
 	}
 
 	private void logWarning(LogAction action) {
 
 		LogEntry entry = new WarningLogEntry();
 		this.processLogEntry(entry, action);
 	}
 }
