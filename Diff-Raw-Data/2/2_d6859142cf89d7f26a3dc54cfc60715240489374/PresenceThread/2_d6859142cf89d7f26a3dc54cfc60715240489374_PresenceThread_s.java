 package microcontroller.threads;
 
 import java.util.LinkedList;
 import java.util.TimerTask;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Semaphore;
 
 import microcontroller.interfaces.Database;
 import microcontroller.interfaces.Presence;
 
 public class PresenceThread extends TimerTask {
 
 	protected Presence presence;
 	protected Database db;
 	protected BlockingQueue<String> messagesToPublish;
 	protected LinkedList<String> nearbyUsers;
 	protected LinkedList<String> staticNearbyUsers;
 	protected Semaphore nearbyUsersSemaphore;
 
 	public PresenceThread(Presence presence, Database db,
 			BlockingQueue<String> messagesToPublish,
 			LinkedList<String> nearbyUsers, Semaphore nearbyUsersSemaphore) {
 		super();
 		this.presence = presence;
 		this.db = db;
 		this.messagesToPublish = messagesToPublish;
 		this.nearbyUsers = nearbyUsers;
 		this.nearbyUsersSemaphore = nearbyUsersSemaphore;
 		this.staticNearbyUsers = new LinkedList<String>();
 		this.staticNearbyUsers.add("1CB0940951E7");
 	}
 
 	@Override
 	public void run() {
 		while (true) {
 			try {
 				System.out.println("Presence service is running");
 				LinkedList<String> previousNearbyUsers = new LinkedList<String>();
 				/*
 				 * Find out current devices in the room
 				 */
 				this.nearbyUsersSemaphore.acquire();
 				if (!this.nearbyUsers.isEmpty()) {
 					previousNearbyUsers.addAll(this.nearbyUsers);
 					this.nearbyUsers.clear();
 				}
 				this.presence.resetDevs();
 				this.presence.detectDevs();
 				this.nearbyUsers.addAll(this.presence.getDevs());
 				
 				for(String deviceId: staticNearbyUsers) {
 					if(!nearbyUsers.contains(deviceId)) {
 						this.nearbyUsers.add(deviceId);
 					}
 				}
 				/*
 				 * Which devices left the room?
 				 */
 				previousNearbyUsers.removeAll(this.nearbyUsers);
 
 				/*
 				 * TODO: Decide if this is necessary. Might make things ugly.
 				 */
 				/*
 				 * Publish the users that present in the previous sweep but not
 				 * in this one
 				 */
 				for (String userDeviceId : previousNearbyUsers) {
 					String twitterName = db.getTwitterNameFromDeviceId(userDeviceId);
 					db.logOutUserWithDeviceId(userDeviceId);
 					this.messagesToPublish.add(String.format("@%s left the room.", twitterName));
 				}
 				this.nearbyUsersSemaphore.release();
 				break;
 			} catch (InterruptedException e) {
 
 			}
 		}
 	}
 
 }
