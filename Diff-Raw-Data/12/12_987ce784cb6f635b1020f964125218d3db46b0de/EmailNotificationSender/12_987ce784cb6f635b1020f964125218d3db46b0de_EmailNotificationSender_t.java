 package org.imirsel.nema.flowservice;
 
 import java.util.Comparator;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 
 import net.jcip.annotations.ThreadSafe;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.imirsel.nema.dao.DaoFactory;
 import org.imirsel.nema.dao.NotificationDao;
 import org.imirsel.nema.model.Notification;
 import org.springframework.mail.MailException;
 import org.springframework.mail.MailSender;
 import org.springframework.mail.SimpleMailMessage;
 
 /**
  * 
  * @author shirk
  * @since 0.5.0
  */
 @ThreadSafe
 public class EmailNotificationSender implements NotificationSender {
 
 	static private final int DELIVERY_DELAY = 30;
 
 	/** Logger for this class. */
 	static private final Logger logger = Logger
 			.getLogger(EmailNotificationSender.class.getName());
 
 	private DaoFactory daoFactory;
 
 	private MailSender mailSender;
 
 	private SimpleMailMessage messageTemplate;
 
 	private final Queue<Notification> mailQueue = new PriorityQueue<Notification>(16,
 			new NotificationComparator());
 
 	private final Lock mailQueueLock = new ReentrantLock();
 
 	public EmailNotificationSender() {
 
 	}
 
 	/** Periodically checks for mail to deliver. */
 	@SuppressWarnings("unused")
 	private ScheduledFuture<?> mailDeliveryFuture;
 
 	// ~ Instance initializers ------------------------------------------------
 
 	{
 		ScheduledExecutorService executor = Executors
 				.newSingleThreadScheduledExecutor();
 
 		mailDeliveryFuture = executor.scheduleWithFixedDelay(
 				new MailDeliveryTask(), 30, DELIVERY_DELAY, TimeUnit.SECONDS);
 	}
 
 	@PostConstruct
 	public void init() {
 		logger.fine("Checking for unsent notifications in the datastore");
 		loadUnsentNotifications();
 	}
 
 	private void loadUnsentNotifications() {
		logger.fine("Preparing to load unsent notifications.");
 		NotificationDao dao = daoFactory.getNotificationDao();
 		Session session = null;
 		List<Notification> unsent = null;
 		try {
 			session=dao.getSessionFactory().openSession();
 			dao.startManagedSession(session);
 			unsent = dao.getUnsentNotifications();
			logger.fine("Successfully loaded unsent notifications.");
 		} catch (Exception e) {
			logger.fine("Failed to load unsent notifications: " + e.getMessage());
 		} finally {
 			dao.endManagedSession();
 			if (session != null) {
 				session.close();
 			}
 		}
 
 		if (unsent != null) {
 			mailQueueLock.lock();
 			try {
 				for (Notification notification : unsent) {
 					mailQueue.offer(notification);
 				}
 			} finally {
 				mailQueueLock.unlock();
 			}
 		}
 	}
 
 	public void send(Notification notification) {
 		mailQueueLock.lock();
 		try {
 			mailQueue.offer(notification);
 		} finally {
 			mailQueueLock.unlock();
 		}
 	}
 
 	private class MailDeliveryTask implements Runnable {
 		public void run() {
 			logger.fine("Preparing to deliver " + mailQueue.size()
 					+ " email notifications...");
 			NotificationDao dao = daoFactory.getNotificationDao();
 			mailQueueLock.lock();
 			try {
 				while (!mailQueue.isEmpty()) {
 
 					Notification notification = mailQueue.peek();
 
 					SimpleMailMessage msg = new SimpleMailMessage(
 							messageTemplate);
 					msg.setTo(notification.getRecipientEmail());
 					msg.setSubject(notification.getSubject());
 					msg.setText(notification.getMessage());
 					try {
 						mailSender.send(msg);
 						logger.fine("Delivered notification to " + msg.getTo()[0]
 								+ ".");
 						notification
 								.setDeliveryStatus(Notification.DeliveryStatus.SUCCESS);
 					} catch (MailException e) {
 						logger.warning("Failed to deliver notification: "
 								+ e.getMessage());
						notification.setErrorMessage(e.getMessage());
 						notification
 								.setDeliveryStatus(Notification.DeliveryStatus.FAILURE);
 					}
 					mailQueue.remove();
 
 					Session session = null;
 					Transaction transaction = null;
 					try {
 						session = dao.getSessionFactory().openSession();
 						dao.startManagedSession(session);
 						transaction = session.beginTransaction();
 						logger.fine("Preparing to record notification status changes.");
 						dao.makePersistent(notification);
 						logger.fine("Successfully recorded notification status changes.");
 						transaction.commit();
 					} catch (Exception e) {
 						logger.warning(e.getMessage());
 						if (transaction != null) {
 							transaction.rollback();
 						}
 					} finally {
 						dao.endManagedSession();
 						if (session != null) {
 							session.close();
 						}
 					}
 				}
 
 			} finally {
 				mailQueueLock.unlock();
 			}
 		}
 	}
 
 	public DaoFactory getDaoFactory() {
 		return daoFactory;
 	}
 
 	public void setDaoFactory(DaoFactory daoFactory) {
 		this.daoFactory = daoFactory;
 	}
 
 	public MailSender getMailSender() {
 		return mailSender;
 	}
 
 	public void setMailSender(MailSender mailSender) {
 		this.mailSender = mailSender;
 	}
 
 	public SimpleMailMessage getMessageTemplate() {
 		return messageTemplate;
 	}
 
 	public void setMessageTemplate(SimpleMailMessage messageTemplate) {
 		this.messageTemplate = messageTemplate;
 	}
 
 	private class NotificationComparator implements Comparator<Notification> {
 
 		@Override
 		public int compare(Notification o1, Notification o2) {
 			if (o1.getId() < o2.getId()) {
 				return -1;
 			} else if (o1.getId() > o2.getId()) {
 				return 1;
 
 			} else {
 				return 0;
 			}
 		}
 	}
 
 }
