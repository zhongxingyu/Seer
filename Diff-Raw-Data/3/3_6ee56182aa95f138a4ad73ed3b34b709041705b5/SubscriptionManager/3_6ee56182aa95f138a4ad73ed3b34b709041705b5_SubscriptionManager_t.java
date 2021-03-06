 /* This code is part of Freenet. It is distributed under the GNU General
  * Public License, version 2 (or at your option any later version). See
  * http://www.gnu.org/ for further details of the GPL. */
 package plugins.WebOfTrust;
 
 import java.util.UUID;
 
 import plugins.WebOfTrust.Identity.IdentityID;
 import plugins.WebOfTrust.exceptions.DuplicateObjectException;
 import plugins.WebOfTrust.exceptions.UnknownIdentityException;
 
 import com.db4o.ObjectSet;
 import com.db4o.ext.ExtObjectContainer;
 import com.db4o.query.Query;
 
 import freenet.node.PrioRunnable;
 import freenet.pluginmanager.PluginRespirator;
 import freenet.support.Logger;
 import freenet.support.TrivialTicker;
 import freenet.support.codeshortification.IfNull;
 import freenet.support.io.NativeThread;
 
 /**
  * The subscription manager allows client application to subscribe to certain data sets of WoT and get notified on change.
  * For example, if you subscribe to the list of identities, you will get a notification when an identity is added or removed.
  * 
  * The architecture of this class supports implementing different types of subscriptions: Currently, only FCP is implemented, but it is also technically possible to have subscriptions
  * which do a callback within the WoT plugin or maybe even via OSGI.
  * 
  * The class/object model is as following:
  * - There is exactly one SubscriptionManager object running in the WOT plugin. It is the interface for clients.
  * - Subscribing to something yields a {@link Subscription} object which is stored by the SubscriptionManager in the database. Clients do not need to keep track of it. They only need to know its ID.
  * - When an event happens, a {@link Notification} object is created for each {@link Subscription} which matches the type of event. The Notification is stored in the database.
  * - After a delay, the SubscriptionManager deploys the notifications to the clients.
  * 
  * The {@link Notification}s are deployed strictly sequential per {@link Subscription}.
  * If a single Notification cannot be deployed, the processing of the Notifications for that Subscription is halted until the failed
  * Notification can be deployed successfully.
  * This especially applies to the FCP-client-interface and all other client interfaces: If a client returns "ERROR!" from the callback it has received,
  * the notification queue is halted and the previous notification is re-sent a few times until it can be imported successfully by the client
  * or the connection is dropped due to too many failures.
  * 
  * TODO: What to do, if the client does not support a certain message?
  * 
  * This is a very important principle which makes client design easy: You do not need transaction-safety when caching things such as score values
  * incrementally. For example your client might need to do mandatory actions due to a score-value change, such as deleting messages from identities
  * which have a bad score now. If the score-value import succeeds but the message deletion fails, you can just return "ERROR!" to the WOT-callback-caller
  * (and maybe even keep your score-cache as is) - you will continue to receive the notification about the changed score value for which the import failed,
  * you will not receive change-notifications after that. This ensures that your consistency is not destroyed: There will be no missing slot
  * in the incremental change chain.
  * FIXME: AFAIK the FCP-client does not support the above yet, this is a MUST-have feature needed by Freetalk, check whether it works.
  * 
  * <b>Synchronization:</b>
  * The locking order must be:
  * 	synchronized(instance of WebOfTrust) {
  *	synchronized(instance of IntroductionPuzzleStore) {
  *	synchronized(instance of IdentityFetcher) {
  *	synchronized(instance of SubscriptionManager) {
  *	synchronized(Persistent.transactionLock(instance of ObjectContainer)) {
  * 
  * TODO: Allow out-of-order notifications if the client desires them
  * TODO: Optimization: Allow coalescing of notifications: If a single object changes twice, only send one notification
  * TODO: Optimization: Allow the client to specify filters to reduce traffic: - Context of identities, etc. 
  * 
  * 
  * TODO: This should be used for powering the IntroductionClient/IntroductionServer.
  * 
  * @author xor (xor@freenetproject.org)
  */
 public final class SubscriptionManager implements PrioRunnable {
 	
 	/**
 	 * A subscription stores the information which client is subscribed to which content and how it is supposed
 	 * to be notified about updates.
 	 * Subscriptions are stored one per {@link Notification}-type and per way of notification:
 	 * Because we want the notification queue to block on error, a single subscription does not support
 	 * multiple ways of notifying the client.
 	 * 
 	 * Notice: Even though this is an abstract class, it contains code specific <b>all</>b> types of subscription clients such as FCP and callback.
 	 * At first glance, this looks like a violation of abstraction principles. But it is not:
 	 * Subclasses of this class shall NOT be created for different types of clients such as FCP and callbacks.
 	 * Subclasses are created for different types of content to which the subscriber is subscribed: There is a subclass for subscriptions to the
 	 * list of {@link Identity}s, the list of {@link Trust}s, and so on. Each subclass has to implement the code for notifying <b>all</b> types
 	 * of clients (FCP, callback, etc.).
 	 * Therefore, this base class also contains code for <b>all</b> kinds of clients.
 	 */
 	@SuppressWarnings("serial")
 	public static abstract class Subscription<NotificationType extends Notification> extends Persistent {
 		
 		/**
 		 * The UUID of this Subscription. Stored as String for db4o performance, but must be valid in terms of the UUID class.
 		 * 
 		 * @see #getID()
 		 */
 		@IndexedField
 		private final String mID;
 		
 		/**
 		 * The way of notifying a client
 		 */
 		public static enum Type {
 			FCP,
 			Callback
 		};
 		
 		/**
 		 * The way the client of this subscription desires notification.
 		 * 
 		 * @see #getType()
 		 */
 		private final Type mType;
 		
 		/**
 		 * An ID which associates this subscription with a FCP connection if the type is FCP.
 		 * 
 		 * @see #getFCP_ID()
 		 */
 		@IndexedField
 		private final String mFCP_ID;
 
 		/**
 		 * Each {@link Notification} is given an index upon creation. The indexes ensure sequential processing.
 		 * If an error happens when trying to process a notification to a client, we might want to retry some time later.
 		 * Therefore, the {@link Notification} queue exists per client and not globally - if a single {@link Notification} is created by WoT,
 		 * multiple {@link Notification} objects are stored - one for each Subscription.
 		 */
 		private long mNextNotificationIndex = 0;
 		
 		/**
 		 * Constructor for being used by child classes.
 		 * @param myType The type of the Subscription
 		 * @param fcpID The FCP ID of the subscription. Can be null if the type is not FCP.
 		 */
 		protected Subscription(Type myType, String fcpID) {
 			mID = UUID.randomUUID().toString();
 			mType = myType;
 			mFCP_ID = fcpID;
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void startupDatabaseIntegrityTest() throws Exception {
 			checkedActivate(1); // 1 is the maximum needed depth of all stuff we use in this function
 			
 			IfNull.thenThrow(mID, "mID");
 			UUID.fromString(mID); // Throws if invalid
 			
 			IfNull.thenThrow(mType, "mType");
 			
 			if(mType == Type.FCP)
 				IfNull.thenThrow(mFCP_ID, "mFCP_ID");
 			
 			if(mNextNotificationIndex < 0)
 				throw new IllegalStateException("mNextNotificationIndex==" + mNextNotificationIndex);
 		}
 		
 		/**
 		 * You must call {@link #initializeTransient} before using this!
 		 */
 		protected final SubscriptionManager getSubscriptionManager() {
 			return mWebOfTrust.getSubscriptionManager();
 		}
 		
 		/**
 		 * @return The UUID of this Subscription. Stored as String for db4o performance, but must be valid in terms of the UUID class.
 		 * @see #mID
 		 */
 		public final String getID() {
 			checkedActivate(1);
 			return mID;
 		}
 		
 		/**
 		 * @return The {@link Type} of this Subscription.
 		 * @see #mType
 		 */
 		public final Type getType() {
 			checkedActivate(1);
 			return mType;
 		}
 		
 		/**
 		 * @return An ID which associates this subscription with a FCP connection if the type is FCP.
 		 * @see #mFCP_ID
 		 */
 		public final String getFCP_ID() {
 			if(getType() != Type.FCP)
 				throw new UnsupportedOperationException("Type is not FCP:" + getType());
 			
 			checkedActivate(1);
 			return mFCP_ID;
 		}
 		
 		/**
 		 * Returns the next free index for a {@link Notification} in the queue of this Subscription
 		 * and stores this Subscription object without committing the transaction.
 		 * 
 		 * Schedules processing of the Notifications of the SubscriptionManger.
 		 */
 		protected final long takeFreeNotificationIndexWithoutCommit() {
 			checkedActivate(1);
 			final long index = mNextNotificationIndex++;
 			storeWithoutCommit();
 			getSubscriptionManager().scheduleNotificationProcessing();
 			return index;
 		}
 		
 		/**
 		 * ATTENTION: This does NOT delete the {@link Notification} objects associated with this Subscription!
 		 * Only use it if you delete them manually before!
 		 * 
 		 * - Deletes this {@link Subscription} from the database. Does not commit the transaction and does not take care of synchronization.
 		 */
 		@Override
 		protected void deleteWithoutCommit() {
 			throw new UnsupportedOperationException("Need a SubscriptionManager");
 		}
 		
 		/**
 		 * Deletes this Subscription and - using the passed in {@link SubscriptionManager} - also deletes all
 		 * queued {@link Notification}s of it. Does not commit the transaction.
 		 * 
 		 * @param manager The {@link SubscriptionManager} to which this Subscription belongs.
 		 */
 		protected void deleteWithoutCommit(final SubscriptionManager manager) {
 			for(final Notification notification : manager.getAllNotifications(this)) {
 				notification.deleteWithoutCommit();
 			}
 			super.deleteWithoutCommit();
 		}
 
 		/**
 		 * Takes the database lock to begin a transaction, stores this object and commits the transaction.
 		 * You must synchronize on the {@link SubscriptionManager} while calling this function.
 		 */
 		protected void storeAndCommit() {
 			synchronized(Persistent.transactionLock(mDB)) {
 				try {
 					storeWithoutCommit();
 					checkedCommit(this);
 				} catch(RuntimeException e) {
 					Persistent.checkedRollbackAndThrow(mDB, this, e);
 				}
 			}
 		}
 		
 		
 		/**
 		 * Called by this subscription when processing an {@link InitialSynchronizationNotification}.
 		 * 
 		 * Upon creation of a Subscription, such a notification is stored immediately, i.e. not triggered by an event.
 		 * When real events happen, we only want to send a "diff" between the last state of the database before the event happened and the new state.
 		 * For being able to only send a diff, the subscriber must know what the <i>initial</i> state of the database was.
 		 * And thats the purpose of this function: To send the full initial state of the database to the client.
 		 * 
 		 * For example, if a client subscribes to the list of identities, it must always receive a full list of all existing identities at first.
 		 * As new identities appear afterwards, the client can be kept up to date by sending each single new identity as it appears. 
 		 * 
 		 * @see InitialSynchronizationNotification
 		 */
 		protected abstract void synchronizeSubscriberByFCP() throws Exception;
 
 		/**
 		 * Called by this Subscription when the type of it is FCP and a {@link Notification} shall be sent via FCP. 
 		 * The implementation MUST throw a {@link RuntimeException} if the FCP message was not sent successfully: 
 		 * Subscriptions are supposed to be reliable, if transmitting a {@link Notification} fails it shall
 		 * be resent.
 		 * 
 		 * @param notification The {@link Notification} to send out via FCP.
 		 */
 		protected abstract void notifySubscriberByFCP(NotificationType notification) throws Exception;
 
 		/**
 		 * Sends out the notification queue for this Subscription, in sequence.
 		 * 
 		 * If a notification is sent successfully, it is deleted and the transaction is committed.
 		 * If sending a single notification fails, the transaction for the current Notification is rolled back
 		 * and an exception is thrown.
 		 * 
 		 * You have to synchronize on the WoT, the SubscriptionManager and the database lock before calling this
 		 * function!
 		 * You don't have to commit the transaction after calling this function.
 		 * 
 		 * @param manager The {@link SubscriptionManager} from which to query the {@link Notification}s of this Subscription.
 		 */
 		@SuppressWarnings("unchecked")
 		protected void sendNotifications(SubscriptionManager manager) {
 			switch(mType) {
 				case FCP:
 					for(final Notification notification : manager.getAllNotifications(this)) {
 						try {
 							try {
 								if(notification instanceof InitialSynchronizationNotification) {
 									synchronizeSubscriberByFCP();
 								} else {
 									notifySubscriberByFCP((NotificationType)notification);
 									notification.deleteWithoutCommit();
 								}
 							} catch(Exception e) {
 								// Disconnected etc.
 								throw new RuntimeException(e);
 							}
 							// If processing of a single notification fails, we do not want the previous notifications
 							// to be sent again when the failed notification is retried. Therefore, we commit after
 							// each processed notification but do not catch RuntimeExceptions here
 							
 							Persistent.checkedCommit(mDB, this);
 						} catch(RuntimeException e) {
 							Persistent.checkedRollbackAndThrow(mDB, this, e);
 						}
 					}
 					break;
 				default:
 					throw new UnsupportedOperationException("Unknown Type: " + mType);
 			}
 		}
 
 	}
 	
 	
 	/**
 	 * An object of type Notification is stored when an event happens to which a client is possibly subscribed.
 	 * The SubscriptionManager will wake up some time after that, pull all notifications from the database and process them.
 	 */
 	@SuppressWarnings("serial")
 	public static abstract class Notification extends Persistent {
 		
 		/**
 		 * The {@link Subscription} to which this Notification belongs
 		 */
 		@IndexedField
 		private final Subscription<? extends Notification> mSubscription;
 		
 		/**
 		 * The index of this Notification in the queue of its {@link Subscription}:
 		 * Notifications are supposed to be sent out in proper sequence, therefore we use incremental indices.
 		 */
 		@IndexedField
 		private final long mIndex;
 		
 		/**
 		 * Constructs a Notification in the queue of the given subscription.
 		 * Takes a free notification index from it with {@link Subscription#takeFreeNotificationIndexWithoutCommit}
 		 * 
 		 * @param mySubscription The {@link Subscription} to whose Notification queue this Notification belongs.
 		 */
 		protected Notification(final Subscription<? extends Notification> mySubscription) {
 			mSubscription = mySubscription;
 			mIndex = mySubscription.takeFreeNotificationIndexWithoutCommit();
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void startupDatabaseIntegrityTest() throws Exception {
 			checkedActivate(1); // 1 is the maximum needed depth of all stuff we use in this function
 			
 			IfNull.thenThrow(mSubscription);
 			
 			if(mIndex < 0)
 				throw new IllegalStateException("mIndex==" + mIndex);
 		}
 		
 	}
 	
 	/**
 	 * This {@link Notification} is stored as the first Notification for all types of {@link Subscription} which require an initial synchronization of the client.
 	 * 
 	 * Upon creation of a {@link Subscription}, such a notification is stored immediately, i.e. not triggered by an event.
 	 * When real events happen, we only want to send a "diff" between the last state of the database before the event happened and the new state.
 	 * For being able to only send a diff, the subscriber must know what the <i>initial</i> state of the database was.
 	 * And thats the purpose of this notification: To send the full initial state of the database to the client.
 	 * 
 	 * For example, if a client subscribes to the list of identities, it must always receive a full list of all existing identities at first.
 	 * As new identities appear afterwards, the client can be kept up to date by sending each single new identity as it appears.
 	 * 
 	 * @see Subscription#synchronizeSubscriberByFCP() The function which typically deploys this Notification.
 	 */
 	@SuppressWarnings("serial")
 	protected static class InitialSynchronizationNotification extends Notification {
 		
 		/**
 		 * @param mySubscription The {@link Subscription} to whose {@link Notification} queue this {@link Notification}  will belong.
 		 */
 		protected InitialSynchronizationNotification(Subscription<? extends Notification> mySubscription) {
 			super(mySubscription);
 		}
 		
 	}
 	
 	/**
 	 * This notification is issued when an {@link Identity} is added/deleted or its attributes change.
 	 * 
 	 * It provides a {@link Identity#clone()} of the identity:
 	 * - before the change via {@link IdentityChangedNotification#getOldIdentity()}
 	 * - and and after the change via ({@link IdentityChangedNotification#getNewIdentity()}
 	 * 
 	 * If one of the before/after values is null, this is because the identity was added/deleted.
 	 * If both are non-null, the identity was modified.
 	 * NOTICE: Modification can also mean that its class changed from {@link OwnIdentity} to {@link Identity} or vice versa!
 	 * 
 	 * NOTICE: Both Identity objects are not stored in the database and must not be stored there to prevent duplicates!
 	 * 
 	 * FIXME: The JavaDoc of this class should be used as inspiration for the JavaDoc of TrustChangedNotification / ScoreChangedNotification.
 	 * 
 	 * @see IdentitiesSubscription The type of {@link Subscription} which deploys this notification.
 	 */
 	@SuppressWarnings("serial")
 	protected static class IdentityChangedNotification extends Notification {
 		
 		/**
 		 * A serialized copy of the {@link Identity} before the change.
 		 * Null if the change was the creation of the identity.
 		 * If non-null its {@link Identity#getID()} must be equal to the one of {@link #mNewIdentity} if that member is non-null as well.
 		 * 
 		 * @see Identity#serialize()
 		 * @see #getOldIdentity() The public getter for this.
 		 */
 		final byte[] mOldIdentity;
 		
 		/**
 		 * A serialized copy of the {@link Identity } after the change.
 		 * Null if the change was the deletion of the identity.
 		 * If non-null its {@link Identity#getID()} must be equal to the one of {@link #mOldIdentity} if that member is non-null as well.
 		 * 
 		 * @see Identity#serialize()
 		 * @see #getNewIdentity() The public getter for this.
 		 */
 		final byte[] mNewIdentity;
 	
 		/**
 		 * @param mySubscription The {@link Subscription} to whose {@link Notification} queue this {@link Notification} belongs.
 		 * @param oldIdentity The version of the {@link Identity} before the change.
 		 * @param newIdentity The version of the {@link Identity} after the change.
 		 */
 		protected IdentityChangedNotification(final Subscription<? extends IdentityChangedNotification> mySubscription, 
 				final Identity oldIdentity, final Identity newIdentity) {
 			super(mySubscription);
 			mOldIdentity = (oldIdentity!=null ? oldIdentity.serialize() : null);
 			mNewIdentity = (newIdentity!=null ? newIdentity.serialize() : null);
 			
 			assert	(
 						(oldIdentity == null ^ newIdentity == null) ||
 						(oldIdentity != null && newIdentity != null && oldIdentity.getID().equals(newIdentity.getID()))
 					);
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void startupDatabaseIntegrityTest() throws Exception {
 			super.startupDatabaseIntegrityTest();
 			
 			checkedActivate(1); // 1 is the maximum needed depth of all stuff we use in this function
 			
 			if(mOldIdentity == null && mNewIdentity == null)
 				throw new NullPointerException("Only one of mOldIdentity and mNewIdentity may be null!");
 			
 			final Identity oldIdentity = mOldIdentity != null ? (Identity)Persistent.deserialize(mWebOfTrust, mOldIdentity) : null;
 			final Identity newIdentity = mNewIdentity != null ? (Identity)Persistent.deserialize(mWebOfTrust, mNewIdentity) : null;
 			
 			if(oldIdentity != null)
 				oldIdentity.startupDatabaseIntegrityTest();
 
 			if(newIdentity != null)
 				newIdentity.startupDatabaseIntegrityTest();
 
 			if(oldIdentity != null && newIdentity != null && !oldIdentity.getID().equals(newIdentity.getID()))
 				throw new IllegalStateException("mOldIdentity and mNewIdentity must have the same ID!");
 		}
 		
 		/**
 		 * @return The {@link Identity} before the change. 
 		 * @see #mOldIdentity The backend member variable of this getter.
 		 * @throws UnknownIdentityException If the change was the creation of the identity.
 		 */
 		protected final Identity getOldIdentity() throws UnknownIdentityException {
 			checkedActivate(1);
 			if(mOldIdentity == null)
 				throw new UnknownIdentityException("The identity did not exist before.");
 			return (Identity)Persistent.deserialize(mWebOfTrust, mOldIdentity);
 		}
 		
 		/**
 		 * @return The {@link Identity} after the change.
 		 * @see #mNewIdentity The backend member variable of this getter.
 		 * @throws UnknownIdentityException If the change was the deletion of the identity.
 		 */
 		protected final Identity getNewIdentity() throws UnknownIdentityException {
 			checkedActivate(1);
 			if(mNewIdentity == null)
 				throw new UnknownIdentityException("The was deleted.");
 			return (Identity)Persistent.deserialize(mWebOfTrust, mNewIdentity);
 		}
 
 	}
 	
 	/**
 	 * This notification is issued when a {@link Trust} is added/deleted or its attributes change.
 	 * 
 	 * FIXME: This should keep the ID of the trust instead of truster/trustee ID. Also, it should keep both the old and new ID, similar to {@link IdentityChangedNotification}
 	 * 
 	 * @see TrustsSubscription The type of {@link Subscription} which deploys this notification.
 	 */
 	@SuppressWarnings("serial")
 	protected static final class TrustChangedNotification extends Notification {
 		
 		/**
 		 * The ID of the truster of the changed {@link Trust}.
 		 * 
 		 * @see Identity#getID()
 		 * @see Trust#getTruster()
 		 * @see #getTrusterID()
 		 */
 		private final String mTrusterID;
 		
 		/**
 		 * The ID of the trustee of the changed {@link Trust}.
 		 * 
 		 * @see Identity#getID()
 		 * @see Trust#getTrustee()
 		 * @see #getTrusteeID()
 		 */
 		private final String mTrusteeID;
 		
 		/**
 		 * @param mySubscription The {@link Subscription} to whose {@link Notification} queue this {@link Notification} belongs.
 		 * @param myTrust The {@link Trust} which was added, removed or changed.
 		 */
 		protected TrustChangedNotification(final Subscription<TrustChangedNotification> mySubscription, Trust myTrust) {
 			super(mySubscription);
 			mTrusterID = myTrust.getTruster().getID();
 			mTrusteeID = myTrust.getTrustee().getID();
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void startupDatabaseIntegrityTest() throws Exception {
 			super.startupDatabaseIntegrityTest();
 			
 			checkedActivate(1); // 1 is the maximum needed depth of all stuff we use in this function
 			
 			IfNull.thenThrow(mTrusterID, "mTrusterID");
 			IfNull.thenThrow(mTrusteeID, "mTrusteeID");
 		}
 
 		/**
 		 * The ID of the truster of the changed {@link Trust}.
 		 * 
 		 * @see Identity#getID()
 		 * @see Trust#getTruster()
 		 * @see #mTrusterID
 		 */
 		protected final String getTrusterID() {
 			checkedActivate(1);
 			return mTrusterID;
 		}
 
 		/**
 		 * The ID of the trustee of the changed {@link Trust}.
 		 * 
 		 * @see Identity#getID()
 		 * @see Trust#getTrustee()
 		 * @see #mTrusteeID
 		 */
 		protected final String getTrusteeID() {
 			checkedActivate(1);
 			return mTrusteeID;
 		}
 		
 	}
 	
 	/**
 	 * This notification is issued when a score value is added/deleted or its attributes change.
 	 * 
 	 * FIXME: This should keep the ID of the trust instead of truster/trustee ID. Also, it should keep both the old and new ID, similar to {@link IdentityChangedNotification}
 	 * 
 	 * @see ScoresSubscription The type of {@link Subscription} which deploys this notification.
 	 */
 	@SuppressWarnings("serial")
 	protected static final class ScoreChangedNotification extends Notification {
 		
 		/**
 		 * The ID of the truster of the changed {@link Score}.
 		 * 
 		 * @see Identity#getID()
 		 * @see Score#getTruster()
 		 * @see #getTrusterID()
 		 */
 		private final String mTrusterID;
 		
 		/**
 		 * The ID of the trustee of the changed {@link Score}.
 		 * 
 		 * @see Identity#getID()
 		 * @see Score#getTrustee()
 		 * @see #getTrusteeID()
 		 */
 		private final String mTrusteeID;
 
 		/**
 		 * @param mySubscription The {@link Subscription} to whose {@link Notification} queue this {@link Notification} belongs.
 		 * @param myScore The {@link Score} which was added, deleted or changed.
 		 */
 		protected ScoreChangedNotification(final Subscription<ScoreChangedNotification> mySubscription, Score myScore) {
 			super(mySubscription);
 			mTrusterID = myScore.getTruster().getID();
 			mTrusteeID = myScore.getTrustee().getID();
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void startupDatabaseIntegrityTest() throws Exception {
 			super.startupDatabaseIntegrityTest();
 			
 			checkedActivate(1); // 1 is the maximum needed depth of all stuff we use in this function
 			
 			IfNull.thenThrow(mTrusterID, "mTrusterID");
 			IfNull.thenThrow(mTrusteeID, "mTrusteeID");
 		}
 		
 		/**
 		 * The ID of the truster of the changed {@link Score}.
 		 * 
 		 * @see Identity#getID()
 		 * @see Score#getTruster()
 		 * @see #mTrusterID
 		 */
 		protected final String getTrusterID() {
 			checkedActivate(1);
 			return mTrusterID;
 		}
 		
 		
 		/**
 		 * The ID of the trustee of the changed {@link Score}.
 		 * 
 		 * @see Identity#getID()
 		 * @see Score#getTrustee()
 		 * @see #mTrusteeID
 		 */
 		protected final String getTrusteeID() {
 			checkedActivate(1);
 			return mTrusteeID;
 		}
 	}
 
 	/**
 	 * A subscription to the set of all {@link Identity} and {@link OwnIdentity} instances.
 	 * If an identity gets added/deleted or if its attributes change the subscriber is notified by a {@link IdentityChangedNotification}.
 	 * 
 	 * @see IdentityChangedNotification The type of {@link Notification} which is deployed by this subscription.
 	 */
 	@SuppressWarnings("serial")
 	public static final class IdentitiesSubscription extends Subscription<IdentityChangedNotification> {
 
 		/**
 		 * @param fcpID See {@link Subscription#getFCP_ID()}. 
 		 */
 		protected IdentitiesSubscription(String fcpID) {
 			super(Subscription.Type.FCP, fcpID);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		protected void synchronizeSubscriberByFCP() throws Exception {
 			mWebOfTrust.getFCPInterface().sendAllIdentities(getFCP_ID());
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		protected void notifySubscriberByFCP(IdentityChangedNotification notification) throws Exception {
 			mWebOfTrust.getFCPInterface().sendIdentityChangedNotification(getFCP_ID(), notification.getOldIdentity(), notification.getNewIdentity());
 		}
 
 		/**
 		 * Stores a {@link IdentityChangedNotification} to the {@link Notification} queue of this {@link Subscription}.
 		 * 
 		 * @param identity The {@link Identity} whose attributes have changed.
 		 */
 		private void storeNotificationWithoutCommit(Identity oldIdentity, Identity newIdentity) {
 			final IdentityChangedNotification notification = new IdentityChangedNotification(this, oldIdentity, newIdentity);
 			notification.initializeTransient(mWebOfTrust);
 			notification.storeWithoutCommit();
 		}
 
 	}
 	
 	/**
 	 * A subscription to the set of all {@link Trust} instances.
 	 * If a trust gets added/deleted or if its attributes change the subscriber is notified by a {@link TrustChangedNotification}.
 	 * 
 	 * @see TrustChangedNotification The type of {@link Notification} which is deployed by this subscription.
 	 */
 	@SuppressWarnings("serial")
 	public static final class TrustsSubscription extends Subscription<TrustChangedNotification> {
 
 		/**
 		 * @param fcpID See {@link Subscription#getFCP_ID()}. 
 		 */
 		protected TrustsSubscription(String fcpID) {
 			super(Subscription.Type.FCP, fcpID);
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		protected void synchronizeSubscriberByFCP() throws Exception {
 			mWebOfTrust.getFCPInterface().sendAllTrustValues(getFCP_ID());
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		protected void notifySubscriberByFCP(TrustChangedNotification notification) throws Exception {
 			mWebOfTrust.getFCPInterface().sendTrustChangedNotification(getFCP_ID(), notification.getTrusterID(), notification.getTrusteeID());
 		}
 
 		/**
 		 * Stores a {@link TrustChangedNotification} to the {@link Notification} queue of this {@link Subscription}.
 		 * 
 		 * @param trust The {@link Trust} which was added, changed deleted.
 		 */
 		public void storeNotificationWithoutCommit(Trust trust) {
 			final TrustChangedNotification notification = new TrustChangedNotification(this, trust);
 			notification.initializeTransient(mWebOfTrust);
 			notification.storeWithoutCommit();
 		}
 
 	}
 	
 	/**
 	 * A subscription to the set of all {@link Score} instances.
 	 * If a score gets added/deleted or if its attributes change the subscriber is notified by a {@link ScoreChangedNotification}.
 	 * 
 	 * @see ScoreChangedNotification The type of {@link Notification} which is deployed by this subscription.
 	 */
 	@SuppressWarnings("serial")
 	public static final class ScoresSubscription extends Subscription<ScoreChangedNotification> {
 
 		/**
 		 * @param fcpID See {@link Subscription#getFCP_ID()}. 
 		 */
 		protected ScoresSubscription(String fcpID) {
 			super(Subscription.Type.FCP, fcpID);
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		protected void synchronizeSubscriberByFCP() throws Exception {
 			mWebOfTrust.getFCPInterface().sendAllScoreValues(getFCP_ID());
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		protected void notifySubscriberByFCP(ScoreChangedNotification notification) throws Exception {
 			mWebOfTrust.getFCPInterface().sendScoreChangedNotification(getFCP_ID(), notification.getTrusterID(), notification.getTrusteeID());
 		}
 
 		/**
 		 * Stores a {@link ScoreChangedNotification} to the {@link Notification} queue of this {@link Subscription}.
 		 * 
 		 * @param score The {@link Score} which was added, changed deleted.
 		 */
 		public void storeNotificationWithoutCommit(Score score) {
 			final ScoreChangedNotification notification = new ScoreChangedNotification(this, score);
 			notification.initializeTransient(mWebOfTrust);
 			notification.storeWithoutCommit();
 		}
 
 	}
 
 	
 	/**
 	 * After a {@link Notification} command is stored, we wait this amount of time before processing it.
 	 * This is to allow some coalescing when multiple notifications happen in a short interval.
 	 * This is usually the case as the import of trust lists often causes multiple changes. 
 	 */
 	private static final long PROCESS_NOTIFICATIONS_DELAY = 60 * 1000;
 	
 	
 	/**
 	 * The {@link WebOfTrust} to which this SubscriptionManager belongs.
 	 */
 	private final WebOfTrust mWoT;
 
 	/**
 	 * The database in which to store {@link Subscription} and {@link Notification} objects.
 	 * Same as <code>mWoT.getDatabase();</code>
 	 */
 	private final ExtObjectContainer mDB;
 
 	/**
 	 * The SubscriptionManager schedules execution of its notification deployment thread on this {@link TrivialTicker}.
 	 * The execution typically is scheduled after a delay of {@link #PROCESS_NOTIFICATIONS_DELAY}.
 	 * 
 	 * Is null until {@link #start()} was called.
 	 */
 	private TrivialTicker mTicker = null;
 	
 	
 	/**
 	 * Constructor both for regular in-node operation as well as operation in unit tests.
 	 * 
 	 * @param myWoT The {@link WebOfTrust} to which this SubscriptionManager belongs. Its {@link WebOfTrust#getPluginRespirator()} may return null in unit tests.
 	 */
 	public SubscriptionManager(WebOfTrust myWoT) {
 		mWoT = myWoT;
 		mDB = mWoT.getDatabase();
 	}
 
 	
 	/**
 	 * Thrown when a single client tries to file a {@link Subscription} of the same class of event {@link Notification} and the same {@link Subscription.Type} of notification deployment.
 	 * 
 	 * @see #throwIfSimilarSubscriptionExists
 	 */
 	@SuppressWarnings("serial")
 	public static final class SubscriptionExistsAlreadyException extends Exception {
 		public final Subscription<? extends Notification> existingSubscription;
 		
 		public SubscriptionExistsAlreadyException(Subscription<? extends Notification> existingSubscription) {
 			this.existingSubscription = existingSubscription;
 		}
 	}
 	
 	/**
 	 * Thrown by various functions which query the database for a certain {@link Subscription} if none exists matching the given filters.
 	 */
 	@SuppressWarnings("serial")
 	public static final class UnknownSubscriptionException extends Exception {
 		
 		/**
 		 * @param message A description of the filters which were set in the database query for the {@link Subscription}.
 		 */
 		public UnknownSubscriptionException(String message) {
 			super(message);
 		}
 	}
 	
 	/**
 	 * Throws when a single client tries to file a {@link Subscription} which matches the attributes of the given Subscription:
 	 * - The class of the Subscription and thereby the same class of event {@link Notification}
 	 * - The {@link Subscription.Type} of notification deployment.
 	 * - For FCP connections, the ID of the FCP connection. See {@link Subscription#getID()}.
 	 * 
 	 * Used to ensure that each client can only subscribe once to each type of event.
 	 * 
 	 * @param subscription The new subscription which the client is trying to create. The database is checked for an existing one with similar properties as specified above.
 	 * @throws SubscriptionExistsAlreadyException If a {@link Subscription} exists which matches the attributes of the given Subscription as specified in the description of the function.
 	 */
 	@SuppressWarnings("unchecked")
 	private synchronized void throwIfSimilarSubscriptionExists(final Subscription<? extends Notification> subscription) throws SubscriptionExistsAlreadyException {
 		switch(subscription.getType()) {
 			case FCP:
 				try {
 					throw new SubscriptionExistsAlreadyException(
 								getSubscription((Class<? extends Subscription<? extends Notification>>)subscription.getClass(), subscription.getFCP_ID())
 							);
 				} catch (UnknownSubscriptionException e) {
 					return;
 				}
 			default:
 				throw new UnsupportedOperationException("Unknown type: " + subscription.getType());
 		}
 	}
 	
 	/**
 	 * Creates an {@link InitialSynchronizationNotification} for the given subscription, stores it and the subscription and commits the transaction.
 	 * Takes care of all required synchronization.
 	 * Shall be used as back-end for all front-end functions for creating subscriptions.
 	 * 
 	 * @throws SubscriptionExistsAlreadyException Thrown if a subscription of the same type for the same client exists already. See {@link #throwIfSimilarSubscriptionExists(Subscription)}
 	 */
 	private synchronized void storeNewSubscriptionAndCommit(final Subscription<? extends Notification> subscription) throws SubscriptionExistsAlreadyException {
 		subscription.initializeTransient(mWoT);
 		throwIfSimilarSubscriptionExists(subscription);
 		
 		final InitialSynchronizationNotification notification = new InitialSynchronizationNotification(subscription);
 		notification.initializeTransient(mWoT);
 		notification.storeWithoutCommit();
 		subscription.storeAndCommit();
 	}
 	
 	/**
 	 * The client is notified when an identity is added, changed or deleted.
 	 * FIXME: Do we also notify if internal things such as edition change? Should we?
 	 * 
 	 * @param fcpID The identifier of the FCP connection of the client. Must be unique among all FCP connections!
 	 * @return The {@link IdentitiesSubscription} which is created by this function.
 	 * @see IdentityChangedNotification The type of {@link Notification} which is sent when an event happens.
 	 */
 	public IdentitiesSubscription subscribeToIdentities(String fcpID) throws SubscriptionExistsAlreadyException {
 		final IdentitiesSubscription subscription = new IdentitiesSubscription(fcpID);
 		storeNewSubscriptionAndCommit(subscription);
 		return subscription;
 	}
 	
 	/**
 	 * The client is notified when a trust value changes, is created or removed.
 	 * The client is NOT notified when the comment on a trust value changes.
 	 * 
 	 * @param fcpID The identifier of the FCP connection of the client. Must be unique among all FCP connections!
 	 * @return The {@link TrustsSubscription} which is created by this function.
 	 * @see TrustChangedNotification The type of {@link Notification} which is sent when an event happens.
 	 */
 	public TrustsSubscription subscribeToTrusts(String fcpID) throws SubscriptionExistsAlreadyException {
 		final TrustsSubscription subscription = new TrustsSubscription(fcpID);
 		storeNewSubscriptionAndCommit(subscription);
 		return subscription;
 	}
 	
 	/**
 	 * The client is notified when a score value changes, is created or removed.
 	 * 
 	 * @param fcpID The identifier of the FCP connection of the client. Must be unique among all FCP connections!
 	 * @return The {@link ScoresSubscription} which is created by this function.
 	 * @see ScoreChangedNotification The type of {@link Notification} which is sent when an event happens.
 	 */
 	public ScoresSubscription subscribeToScores(String fcpID) throws SubscriptionExistsAlreadyException {
 		final ScoresSubscription subscription = new ScoresSubscription(fcpID);
 		storeNewSubscriptionAndCommit(subscription);
 		return subscription;
 	}
 	
 	/**
 	 * Deletes the given {@link Subscription}.
 	 * 
 	 * @param subscriptionID See {@link Subscription#getID()}
 	 * @throws UnknownSubscriptionException If no subscription with the given ID exists.
 	 */
 	public void unsubscribe(String subscriptionID) throws UnknownSubscriptionException {
 		synchronized(mWoT) { // FIXME: Remove this synchronization when resolving the inner FIXME
 		synchronized(this) {
 		final Subscription<? extends Notification> subscription = getSubscription(subscriptionID);
 		synchronized(Persistent.transactionLock(mDB)) {
 			try {
 				// FIXME: This is debug code and removing it is a critical optimization:
 				// To make debugging easier, we also send an final InitialSynchronizationNotification when disconnecting a client
 				// instead of only sending one at the beginning of the connection.
 				// Sending an InitialSynchronizationNotification usually sends the full stored dataset to synchronize the client.
 				// The client can use it to check whether the state of WOT which he received through notifications was correct.
 				{
 					final InitialSynchronizationNotification notification = new InitialSynchronizationNotification(subscription);
 					notification.initializeTransient(mWoT);
 					notification.storeWithoutCommit();
 					subscription.sendNotifications(this);
 				}
 
 				subscription.deleteWithoutCommit(this);
 				Persistent.checkedCommit(mDB, this);
 			} catch(RuntimeException e) {
 				Persistent.checkedRollbackAndThrow(mDB, this, e);
 			}
 		}
 		}
 		}
 	}
 	
 	/**
 	 * Typically used at startup by {@link #deleteAllSubscriptions()} and {@link #run()}
 	 * 
 	 * @return All existing {@link Subscription}s.
 	 */
 	private ObjectSet<Subscription<? extends Notification>> getAllSubscriptions() {
 		final Query q = mDB.query();
 		q.constrain(Subscription.class);
 		return new Persistent.InitializingObjectSet<Subscription<? extends Notification>>(mWoT, q);
 	}
 	
 	/**
 	 * Get all {@link Subscription}s to a certain {@link Notification} type.
 	 * 
 	 * Typically used by the functions store*NotificationWithoutCommit() for storing the given {@link Notification} to the queues of all Subscriptions
 	 * which are subscribed to the type of the given notification.
 	 * 
 	 * @param clazz The type of {@link Notification} to filter by.
 	 * @return Get all {@link Subscription}s to a certain {@link Notification} type.
 	 */
 	private ObjectSet<? extends Subscription<? extends Notification>> getSubscriptions(final Class<? extends Subscription<? extends Notification>> clazz) {
 		final Query q = mDB.query();
 		q.constrain(clazz);
 		return new Persistent.InitializingObjectSet<Subscription<? extends Notification>>(mWoT, q);
 	}
 	
 	/**
 	 * @param id The unique identificator of the desired {@link Subscription}. See {@link Subscription#getID()}.
 	 * @return The {@link Subscription} with the given ID. Only one Subscription can exist for a single ID.
 	 * @throws UnknownSubscriptionException If no {@link Subscription} exists with the given ID.
 	 */
 	private Subscription<? extends Notification> getSubscription(final String id) throws UnknownSubscriptionException {
 		final Query q = mDB.query();
 		q.constrain(Subscription.class);
 		q.descend("mID").constrain(id);	
 		ObjectSet<Subscription<? extends Notification>> result = new Persistent.InitializingObjectSet<Subscription<? extends Notification>>(mWoT, q);
 		
 		switch(result.size()) {
 			case 1: return result.next();
 			case 0: throw new UnknownSubscriptionException(id);
 			default: throw new DuplicateObjectException(id);
 		}
 	}
 	
 	/**
 	 * Gets a  {@link Subscription} which matches the given parameters:
 	 * - the given class of Subscription and thereby event {@link Notification}
 	 * - the given FCP identificator, see {@link Subscription#getFCP_ID()}.
 	 * 
 	 * Only one {@link Subscription} which matches both of these can exist: Each FCP client can only subscribe once to a type of event.
 	 * 
 	 * Typically used by {@link #throwIfSimilarSubscriptionExists(Subscription)}.
 	 * 
 	 * @param clazz The class of the Subscription.
 	 * @param fcpID The identificator of the FCP connection. See {@link Subscription#getFCP_ID()}.
 	 * @return See description.
 	 * @throws UnknownSubscriptionException If no matching {@link Subscription} exists.
 	 */
 	private Subscription<? extends Notification> getSubscription(final Class<? extends Subscription<? extends Notification>> clazz, String fcpID) throws UnknownSubscriptionException {
 		final Query q = mDB.query();
 		q.constrain(clazz);
 		q.descend("mFCP_ID").constrain(fcpID);		
 		ObjectSet<Subscription<? extends Notification>> result = new Persistent.InitializingObjectSet<Subscription<? extends Notification>>(mWoT, q);
 		
 		switch(result.size()) {
 			case 1: return result.next();
 			case 0: throw new UnknownSubscriptionException(clazz.getSimpleName().toString() + " with fcpID: " + fcpID);
 			default: throw new DuplicateObjectException(clazz.getSimpleName().toString() + " with fcpID:" + fcpID);
 		}
 	}
 	
 	/**
 	 * Deletes all existing {@link Subscription} objects.
 	 * 
 	 * As a consequence, all {@link Notification} objects associated with the notification queues of the subscriptions become useless and are also deleted.
 	 * 
 	 * Typically used at {@link #start()} - we lose connection to all clients when restarting so their subscriptions are worthless.
 	 */
 	private synchronized final void deleteAllSubscriptions() {
 		Logger.normal(this, "Deleting all subscriptions...");
 		
 		synchronized(Persistent.transactionLock(mDB)) {
 			try {
 				for(Notification n : getAllNotifications()) {
 					n.deleteWithoutCommit();
 				}
 				
 				for(Subscription<? extends Notification> s : getAllSubscriptions()) {
 					s.deleteWithoutCommit();
 				}
 				Persistent.checkedCommit(mDB, this);
 			} catch(RuntimeException e) {
 				Persistent.checkedRollbackAndThrow(mDB, this, e);
 			}
 		}
 		
 		Logger.normal(this, "Finished deleting all subscriptions.");
 	}
 	
 	/**
 	 * Typically used by {@link #deleteAllSubscriptions()}.
 	 * 
 	 * @return All objects of class Notification which are stored in the database.
 	 */
 	private ObjectSet<? extends Notification> getAllNotifications() {
 		final Query q = mDB.query();
 		q.constrain(Notification.class);
 		return new Persistent.InitializingObjectSet<Notification>(mWoT, q);
 	}
 	
 	/**
 	 * Gets all {@link Notification} objects in the queue of the given {@link Subscription}.
 	 * They are ordered ascending by the time of when the event which triggered them happened.
 	 * 
 	 * Precisely, they are ordered by their {@link Notification#mIndex}.
 	 * 
 	 * Typically used for:
 	 * - Deploying the notification queue of a Subscription in {@link Subscription#sendNotifications(SubscriptionManager)}
 	 * - Deleting a subscription in {@link Subscription#deleteWithoutCommit(SubscriptionManager)}
 	 * 
 	 * @param subscription The {@link Subscription} of whose queue to return notifications from.
 	 * @return All {@link Notification}s on the queue of the subscription, ordered ascending by time of happening of their inducing event.
 	 */
 	private ObjectSet<? extends Notification> getAllNotifications(final Subscription<? extends Notification> subscription) {
 		final Query q = mDB.query();
 		q.constrain(Notification.class);
 		q.descend("mSubscription").constrain(subscription).identity();
 		q.descend("mIndex").orderAscending();
 		return new Persistent.InitializingObjectSet<Notification>(mWoT, q);
 	}
 	
 	/**
 	 * Interface for the core of WOT to deploy an {@link IdentityChangedNotification} to clients. 
 	 * 
 	 * Typically called when a {@link Identity} or {@link OwnIdentity} is added, deleted or its attributes are modified.
	 * TODO: List the changes which do not trigger a notification here. For example we don't trigger one upon new edition hints.
 	 * 
 	 * This function does not store a reference to the given identity object in the database, it only stores the ID.
 	 * You are safe to pass non-stored objects or objects which must not be stored.
 	 * 
 	 * You must synchronize on this {@link SubscriptionManager} and the {@link Persistent#transactionLock(ExtObjectContainer)} when calling this function!
 	 * FIXME: Check synchronization of callers.
 	 * 
 	 * @param oldIdentity A {@link Identity#clone()} of the {@link Identity} BEFORE the changes happened. In other words the old version of it.
 	 * @param newIdentity The new version of the {@link Identity} as stored in the database now. 
 	 */
 	protected void storeIdentityChangedNotificationWithoutCommit(final Identity oldIdentity, final Identity newIdentity) {
 		@SuppressWarnings("unchecked")
 		final ObjectSet<IdentitiesSubscription> subscriptions = (ObjectSet<IdentitiesSubscription>)getSubscriptions(IdentitiesSubscription.class);
 		
 		for(IdentitiesSubscription subscription : subscriptions) {
 			subscription.storeNotificationWithoutCommit(oldIdentity, newIdentity);
 		}
 	}
 	
 	/**
 	 * Interface for the core of WOT to deploy a {@link TrustChangedNotification} to clients. 
 	 * 
 	 * Typically called when a {@link Trust} is added, deleted or its attributes are modified.
 	 * 
 	 * This function does not store references to the passed objects in the database, it only stores their IDs.
 	 * You are safe to pass non-stored objects or objects which must not be stored.
 	 * 
 	 * You must synchronize on this {@link SubscriptionManager} and the {@link Persistent#transactionLock(ExtObjectContainer)} when calling this function!
 	 * FIXME: Check synchronization of callers.
 	 * 
 	 * @param oldTrust A {@link Trust#clone()} of the {@link Trust} BEFORE the changes happened. In other words the old version of it.
 	 * @param newTrust The new version of the {@link Trust} as stored in the database now.
 	 */
 	protected void storeTrustChangedNotificationWithoutCommit(final Trust oldTrust, final Trust newTrust) {
 		@SuppressWarnings("unchecked")
 		final ObjectSet<TrustsSubscription> subscriptions = (ObjectSet<TrustsSubscription>)getSubscriptions(TrustsSubscription.class);
 		
 		for(TrustsSubscription subscription : subscriptions) {
 			subscription.storeNotificationWithoutCommit(oldTrust);
 		}
 	}
 	
 	/**
 	 * Interface for the core of WOT to deploy a {@link ScoreChangedNotification} to clients. 
 	 * 
 	 * Typically called when a {@link Score} is added, deleted or its attributes are modified.
 	 * 
 	 * This function does not store references to the passed objects in the database, it only stores their IDs.
 	 * You are safe to pass non-stored objects or objects which must not be stored.
 	 * 
 	 * You must synchronize on this {@link SubscriptionManager} and the {@link Persistent#transactionLock(ExtObjectContainer)} when calling this function!
 	 * FIXME: Check synchronization of callers.
 	 * 
 	 * @param oldScore A {@link Score#clone()} of the {@link Score} BEFORE the changes happened. In other words the old version of it.
 	 * @param newScore The new version of the {@link Score} as stored in the database now.
 	 */
 	protected void storeScoreChangedNotificationWithoutCommit(final Score oldScore, final Score newScore) {
 		@SuppressWarnings("unchecked")
 		final ObjectSet<ScoresSubscription> subscriptions = (ObjectSet<ScoresSubscription>)getSubscriptions(ScoresSubscription.class);
 		
 		for(ScoresSubscription subscription : subscriptions) {
 			subscription.storeNotificationWithoutCommit(oldScore);
 		}
 	}
 
 	/**
 	 * Sends out the {@link Notification} queue of each {@link Subscription} to its clients.
 	 * 
 	 * Typically called by the Ticker {@link #mTicker} on a separate thread. This is triggered by {@link #scheduleNotificationProcessing()}
 	 * - the scheduling function should be called whenever a {@link Notification} is stored to the database.
 	 *  
 	 * If deploying the notifications for a subscription fails, this function is scheduled to be run again after some time.
 	 * If deploying for a certain subscription fails N times, the Subscription is deleted. FIXME: Actually implement this.
 	 * 
 	 * @see Subscription#sendNotifications(SubscriptionManager) This function is called on each subscription to deploy the {@link Notification} queue.
 	 */
 	public void run() {
 		synchronized(mWoT) {
 		synchronized(this) {
 			for(Subscription<? extends Notification> subscription : getAllSubscriptions()) {
 				try {
 					subscription.sendNotifications(this);
 					// Persistent.checkedCommit(mDB, this);	/* sendNotifications() does this already */
 				} catch(Exception e) {
 					Persistent.checkedRollback(mDB, this, e);
 					scheduleNotificationProcessing();
 				}
 			}
 		}
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	public int getPriority() {
 		return NativeThread.LOW_PRIORITY;
 	}
 	
 	/**
 	 * Schedules the {@link #run()} method to be executed after a delay of {@link #PROCESS_NOTIFICATIONS_DELAY}
 	 */
 	private void scheduleNotificationProcessing() {
 		if(mTicker != null)
 			mTicker.queueTimedJob(this, "WoT SubscriptionManager", PROCESS_NOTIFICATIONS_DELAY, false, true);
 		else
 			Logger.warning(this, "Cannot schedule notification processing: Ticker is null.");
 	}
 	
 
 	/**
 	 * Deletes all old subscriptions and enables subscription processing. 
 	 * 
 	 * You must call this before any subscriptions are created, so for example before FCP is available.
 	 * 
 	 * Does NOT work in unit tests - you must manually trigger subscription processing by calling {@link #run()} there.
 	 */
 	protected synchronized void start() {
 		deleteAllSubscriptions();
 		
 		final PluginRespirator respirator = mWoT.getPluginRespirator();
 		
 		if(respirator != null) { // We are connected to a node
 			mTicker = new TrivialTicker(respirator.getNode().executor);
 		} else { // We are inside of a unit test
 			mTicker = null;
 		}
 	}
 	
 	/**
 	 * Shuts down this SubscriptionManager by aborting all queued notification processing and waiting for running
 	 * processing to finish.
 	 */
 	protected synchronized void stop() {
 		Logger.normal(this, "Aborting all pending notifications");
 		
 		mTicker.shutdown();
 		
 		Logger.normal(this, "Stopped.");
 	}
 }
