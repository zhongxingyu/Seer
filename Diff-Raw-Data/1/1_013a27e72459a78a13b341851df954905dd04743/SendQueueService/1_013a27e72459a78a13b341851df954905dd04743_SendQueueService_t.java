 package org.fourdnest.androidclient.services;
 
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.Nest;
 import org.fourdnest.androidclient.NestManager;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 
 /**
  * A service that manages Eggs waiting to be sent to a Nest.
  * Eggs can be marked for immediate sending or queued in waiting
  * for user confirmation or cancellation. The queue of Eggs waiting
  * for confirmation cannot be reordered, but Eggs can be manually sent from it
  * out of sequence.
  */
 public class SendQueueService extends Service {
 	public static final String TAG = SendQueueService.class.getSimpleName();;
 	private SendQueueWorkerThread thread;
 	private ConcurrentLinkedQueue<Work> workQueue;
 	private ConcurrentLinkedQueue<Egg> waitingForConfirmation;
 	private NestManager nestManager;
 
 	/**
 	 * Creates the service, but does not start it.
 	 */
 	public SendQueueService(NestManager nestManager) {
 		this.workQueue = new ConcurrentLinkedQueue<Work>();
 		this.waitingForConfirmation = new ConcurrentLinkedQueue<Egg>();
 		this.thread = new SendQueueWorkerThread(this.workQueue);
 		this.nestManager = nestManager;
 	}	
 	
 	/**
 	 * Starts the service.
 	 */
 	public void start() {
 		this.thread.start();
 	}
 	/**
 	 * Stops the service
 	 */
 	public void stop() {
 		this.thread.dispose();
 	}
 	
 	/**
 	 * Queues an Egg for sending.
 	 * @param egg The Egg to be queued. May not be null.
 	 * @param autosend If true, the Egg will be sent immediately. If false,
 	 * it will be placed in a separate queue waiting user confirmation.
 	 */
 	public void queueEgg(Egg egg, boolean autosend) {
 		assert(egg != null);
 		// FIXME: write to database, or is it already done at this point?
 		this.workQueue.add(new QueuedEgg(egg, autosend));
 	}
 	/**
 	 * Removes an Egg that awaits user confirmation.
 	 * @param egg The Egg to be removed. May not be null.
 	 */
 	public void removeQueuedEgg(Egg egg) {
 		assert(egg != null);
 		this.workQueue.add(new ConfirmEggInQueue(egg, false));
 	}
 	/**
 	 * Sends one Egg that awaits user confirmation, out of sequence.
 	 * @param egg The Egg to be sent. May not be null.
 	 */
 	public void sendQueuedEgg(Egg egg) {
 		assert(egg != null);
 		this.workQueue.add(new ConfirmEggInQueue(egg, true));
 	}
 	/**
 	 * Sends all Eggs that await user confirmation.
 	 */
 	public void sendAllQueuedEggs() {
 		Egg egg;
 		// drain the waitingForConfirmation queue
 		while((egg = SendQueueService.this.waitingForConfirmation.poll()) != null) {
 			// put it back in the send queue, with autosend on
 			SendQueueService.this.queueEgg(egg, true);
 		}	
 	}
 	
 	private class QueuedEgg implements Work {
 		private Egg egg;
 		private boolean autosend;
 		public QueuedEgg(Egg egg, boolean autosend) {
 			this.egg = egg;
 			this.autosend = autosend;
 		}
 		public void doWork() {
 			if(this.autosend) {
 				Nest nest = SendQueueService.this.nestManager.getNest(
 					this.egg.getNestId()
 				);
 				if(nest != null) {
 					nest.getProtocol().sendEgg(egg);
 				} else {
 					Log.d(TAG, "Tried to send Egg to nonexisting nest " + this.egg.getNestId());
 				}
 			} else {
 				SendQueueService.this.waitingForConfirmation.add(this.egg);
 			}
 		}
 	}
 	
 	private class ConfirmEggInQueue implements Work {
 		private Egg egg;
 		private boolean send;
 		public ConfirmEggInQueue(Egg egg, boolean send) {
 			this.egg = egg;
 			this.send = send;
 		}
 		public void doWork() {
 			if(
 					SendQueueService.this.waitingForConfirmation.remove(this.egg) &&
 					this.send
 			) {
 				// put it back in the send queue, with autosend on
 				SendQueueService.this.queueEgg(this.egg, true);
 			}
 		}
 	}
 	
 	private static class SendQueueWorkerThread extends WorkerThread<Work> {
 		public SendQueueWorkerThread(
 				ConcurrentLinkedQueue<Work> queue
 		) {
 			super("SendQueue", queue);
 		}
 
 		@Override
 		protected void doPeriodically() {
 		}
 
 	}
 
 	public void setDelay(long delay) {
 		this.thread.setDelay(delay);
 	}
 	
 	/**
 	 * Null implementation.
 	 */
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 }
