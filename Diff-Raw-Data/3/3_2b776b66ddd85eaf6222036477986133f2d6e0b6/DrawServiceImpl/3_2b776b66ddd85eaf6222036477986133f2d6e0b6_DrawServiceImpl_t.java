 package de.kile.zapfmaster2000.rest.impl.core.box;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.xml.ws.WebServiceException;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import de.kile.zapfmaster2000.rest.core.Zapfmaster2000Core;
 import de.kile.zapfmaster2000.rest.core.box.DrawService;
 import de.kile.zapfmaster2000.rest.core.box.DrawServiceListener;
 import de.kile.zapfmaster2000.rest.core.box.LoginFailureReason;
 import de.kile.zapfmaster2000.rest.core.configuration.ConfigurationConstants;
 import de.kile.zapfmaster2000.rest.core.configuration.ConfigurationService;
 import de.kile.zapfmaster2000.rest.impl.core.transaction.SharedQueries;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Account;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Box;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Drawing;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Keg;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.User;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.UserType;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Zapfmaster2000Factory;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Zapfmaster2000Package;
 
 public class DrawServiceImpl implements DrawService {
 
 	/** logger */
 	private static final Logger LOG = Logger.getLogger(DrawServiceImpl.class);
 
 	/** sync for user changes */
 	private final Object SYNC_USER_LOCK = new Object();
 
 	/** the box the manager is used for */
 	private Box box;
 
 	/** user that is currently logged in at this box */
 	private User currentUser;
 
 	/** the guest user */
 	private User guestUser;
 
 	/** time when the last drawing action was performed */
 	private long lastDrawing;
 
 	/** number of ticks for the current drawing process */
 	private int totalTicks;
 
 	/** the timer */
 	private Timer timer;
 
 	/** listeners */
 	private final List<DrawServiceListener> listeners = new ArrayList<>();
 
 	public DrawServiceImpl(Box pBox) {
 		assert (pBox != null);
 		box = pBox;
 	}
 
 	@Override
 	public User login(long pRfidId) {
 		synchronized (SYNC_USER_LOCK) {
 			if (currentUser == null || currentUser.getRfidTag() != pRfidId) {
 
 				// new user logs in
 				if (checkNewUserCanLogin(pRfidId)) {
 					User newUser = readUser(pRfidId);
 					if (newUser != null) {
 						// login succeeded
 						if (currentUser != null) {
 							finishCurrentDraw();
 						}
 
 						currentUser = newUser;
 						scheduleAutoLogout();
 						lastDrawing = System.currentTimeMillis();
 						retrieveOwnBox();
 						notifyLoginSuccessful(currentUser);
 						return currentUser;
 					} else {
 						notifyLoginFailed(LoginFailureReason.INVALID_RFID_TAG,
 								pRfidId);
 					}
 				} else {
 					notifyLoginFailed(
 							LoginFailureReason.OTHER_USER_IS_LOGGED_IN, pRfidId);
 				}
 			} else {
 				// current user logs in
 				scheduleAutoLogout();
 				return currentUser;
 			}
 		}
 
 		// log did fail
 		return null;
 	}
 
 	@Override
 	public double draw(int pRawAmount) {
 		ConfigurationService config = Zapfmaster2000Core.INSTANCE
 				.getConfigurationService();
 
 		if (pRawAmount < config
 				.getInt(ConfigurationConstants.BOX_DRAW_MIN_TICKS)) {
 			// ignore too little tick counts, otherwise a user might not get
 			// logged out even if he is not doing anything
 			return calcRealAmount(totalTicks);
 		}
 		pRawAmount -= box.getTickReduction();
 
 		scheduleAutoLogout();
 		lastDrawing = System.currentTimeMillis();
 
 		totalTicks += pRawAmount;
 		// ignore to little amounts. This is most likely a "problem" of the
 		// flowmeter
 		double realAmount = calcRealAmount(totalTicks);
 		if (realAmount > config
 				.getDouble(ConfigurationConstants.BOX_DRAW_MIN_AMOUNT)) {
 			if (currentUser != null) {
 				notifyDrawing(currentUser, realAmount);
 			} else {
 				// guest starts to draw
 				retrieveOwnBox();
 				guestUser = findGuest();
 				notifyLoginSuccessful(guestUser);
 				currentUser = guestUser;
 				notifyDrawing(guestUser, realAmount);
 				scheduleAutoLogout();
 			}
 
 		}
 
 		return realAmount;
 	}
 
 	/**
 	 * Reads the own box from db once more, since tick setup parameters might
 	 * have changed.
 	 */
 	private void retrieveOwnBox() {
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().openSession();
 		try {
 			session.beginTransaction();
 			box = (Box) session.load(Zapfmaster2000Package.eINSTANCE.getBox()
 					.getName(), box.getId());
 		} finally {
 			session.getTransaction().commit();
 			session.close();
 		}
 
 	}
 
 	@Override
 	public Box getBox() {
 		return box;
 	}
 
 	@Override
 	public void addListener(DrawServiceListener pListener) {
 		if (pListener != null) {
 			listeners.add(pListener);
 		}
 	}
 
 	@Override
 	public void removeListener(DrawServiceListener pListener) {
 		listeners.remove(pListener);
 	}
 
 	/**
 	 * Checks if a new user may login to the this box.
 	 * 
 	 * @return <code>true</code> if a login is allowed, <code>false</code>
 	 *         otherwise.
 	 */
 	private boolean checkNewUserCanLogin(long pId) {
 		if (currentUser == null || currentUser == guestUser) {
 			return true;
 		}
 		double diff = System.currentTimeMillis() - lastDrawing;
 		if (diff < Zapfmaster2000Core.INSTANCE.getConfigurationService()
 				.getInt(ConfigurationConstants.BOX_LOGIN_MIN_DIFF)) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Reads the user from database.
 	 * 
 	 * @param pRfidId
 	 *            rfid tag of the user to find
 	 * @return the user or <code>null</code> if there is no such user.
 	 */
 	private User readUser(long pRfidId) {
 		// query database for box manager otherwisse
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().openSession();
 		try {
 			session.beginTransaction();
 			@SuppressWarnings("unchecked")
 			List<User> users = session
 					.createQuery("FROM User u WHERE u.rfidTag = :rfid")
 					.setLong("rfid", pRfidId).list();
 			if (users.size() != 1) {
 				return null;
 			}
 			return users.get(0);
 		} finally {
 			session.getTransaction().commit();
 			session.close();
 		}
 	}
 
 	private void notifyLoginSuccessful(User pUser) {
 		for (DrawServiceListener listener : listeners) {
 			listener.onLoginsuccessful(pUser);
 		}
 	}
 
 	private void notifyDrawing(User pUser, double pAmount) {
 		for (DrawServiceListener listener : listeners) {
 			listener.onDrawing(pUser, pAmount);
 		}
 	}
 
 	private void notifyEndDrawing(Drawing pDrawing) {
 		for (DrawServiceListener listener : listeners) {
 			listener.onEndDrawing(pDrawing);
 		}
 	}
 
 	private void notifyLogout(User pUser) {
 		for (DrawServiceListener listener : listeners) {
 			listener.onLogout(pUser);
 		}
 	}
 
 	private void notifyLoginFailed(LoginFailureReason pReason, long pRfidId) {
 		for (DrawServiceListener listener : listeners) {
 			listener.onLoginFailed(pReason, pRfidId);
 		}
 	}
 
 	private void scheduleAutoLogout() {
 		if (timer != null) {
 			timer.cancel();
 		}
 		timer = new Timer();
 		int time = Zapfmaster2000Core.INSTANCE.getConfigurationService()
 				.getInt(ConfigurationConstants.BOX_LOGIN_AUTO_LOGOUT);
 		timer.schedule(createTimerTask(), time);
 	}
 
 	private TimerTask createTimerTask() {
 		return new TimerTask() {
 			@Override
 			public void run() {
 				try {
 					synchronized (SYNC_USER_LOCK) {
 						if (currentUser != null) {
 							LOG.info("Auto-Logout for user: "
 									+ currentUser.getName());
 							finishCurrentDraw();
 							currentUser = null;
 						}
 					}
 				} catch (Throwable th) {
 					LOG.error("Error while performing auto log out", th);
 				}
 			}
 		};
 	}
 
 	private double calcRealAmount(int pRawTicks) {
		return pRawTicks * box.getTickRegressor()
				+ box.getTickDisturbanceTerm();
 	}
 
 	/**
 	 * Finds the guest user. If there is no guest user in the db yet, it is
 	 * created now.
 	 * 
 	 * @return guest user, never <code>null</code>.
 	 */
 	private User findGuest() {
 		User user;
 
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 		Account account = box.getAccount();
 		@SuppressWarnings("unchecked")
 		List<User> guests = session
 				.createQuery(
 						"FROM User u WHERE u.type = :guest AND u.account.id = :accountId")
 				.setParameter("guest", UserType.GUEST)
 				.setLong("accountId", account.getId()).list();
 		if (guests.isEmpty()) {
 			account = (Account) session.load(Zapfmaster2000Package.eINSTANCE
 					.getAccount().getName(), account.getId());
 			User newUser = Zapfmaster2000Factory.eINSTANCE.createUser();
 			newUser.setName("Guest");
 			newUser.setImagePath("images/others/guest.png");
 			newUser.setWeight(100);
 			newUser.setType(UserType.GUEST);
 			newUser.setAccount(account);
 			session.save(newUser);
 			user = newUser;
 		} else {
 			user = guests.get(0);
 		}
 		tx.commit();
 		return user;
 	}
 
 	private void finishCurrentDraw() {
 		double realAmount = calcRealAmount(totalTicks);
 		totalTicks = 0;
 
 		boolean drewMinAmount = realAmount >= Zapfmaster2000Core.INSTANCE
 				.getConfigurationService().getDouble(
 						ConfigurationConstants.BOX_DRAW_MIN_AMOUNT);
 		if (currentUser != null && drewMinAmount) {
 			// add drawing to database
 			Keg activeKeg = SharedQueries.retrieveActiveKeg(getBox());
 
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 
 			Drawing drawing = null;
 
 			try {
 				activeKeg = (Keg) session.load(Zapfmaster2000Package.eINSTANCE
 						.getKeg().getName(), activeKeg.getId());
 				currentUser = (User) session.load(
 						Zapfmaster2000Package.eINSTANCE.getUser().getName(),
 						currentUser.getId());
 
 				drawing = Zapfmaster2000Factory.eINSTANCE.createDrawing();
 				drawing.setAmount(realAmount);
 				drawing.setDate(new Date());
 				drawing.setKeg(activeKeg);
 				drawing.setUser(currentUser);
 
 				session.save(drawing);
 				tx.commit();
 			} catch (RuntimeException ex) {
 				tx.rollback();
 				LOG.error("Could not write draw to db", ex);
 				throw new WebServiceException("Could not write draw to db", ex);
 			}
 
 			notifyEndDrawing(drawing);
 		}
 
 		if (currentUser != null) {
 			notifyLogout(currentUser);
 		}
 
 		// reset values
 		totalTicks = 0;
 		currentUser = null;
 	}
 
 }
