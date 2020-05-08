 package ut.distcomp.playlist;
 
 import java.util.Arrays;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import ut.distcomp.playlist.TransactionState.STATE;
 
 /**
  * This class would be used to track the state of the current running
  * transaction in the non-coordinator processes.
  * 
  * @author gnanda
  *
  */
 public class Transaction implements Runnable {
 	public int DECISION_TIMEOUT;
	public int BUFFER_TIMEOUT;
 	
 	// Reference to the process starting this transaction.
 	Process process;
 	
 	// Message which is to be synchronized.
 	String command; 
 
 	// Next message to be processed.
 	Message message;
 	
 	boolean stateRequestReceived = false;
 	boolean stateRequestResponseReceived = false;
 	
 	// State of the current process. {RESTING, UNCERTAIN, COMMITABLE, COMMIT, ABORT}
 	STATE state = STATE.RESTING;
 	
 	/* 
 	 * XXXX FLAGS TO BE CONTROLLED FROM OUTSIDE XXXX
 	 */
 
 	// This variable is for telling whether a process wants to 
 	// accept this transaction or not.
 	public boolean decision = true;
 	
 	// This is to determine whether to send an abort decision to coordinator or not.
 	public boolean sendAbort = true;
 	
 	protected final Lock lock = new ReentrantLock();
 	protected final Condition nextMessageArrived = lock.newCondition();
 	
 	public Transaction(Process process, Message message) {
 		this.process = process;
 		this.command = message.payLoad;
 		this.message = message;
		this.BUFFER_TIMEOUT = 4000;
		this.DECISION_TIMEOUT = process.delay + this.BUFFER_TIMEOUT;
 	}
 	
 	@Override
 	public void run() {
 		lock.lock();
 		while(state != STATE.COMMIT && state != STATE.ABORT) {
 			
 			if (state == STATE.RESTING) {
 				// If we have come here, it means that we just received a VOTE-REQ.
 				// If we don't like the song, we will simply abort.
 				if (!decision) {
 					process.dtLogger.write(STATE.ABORT);
 					state = STATE.ABORT;
 					process.config.logger.warning("Transaction aborted. Not ready for this message.");
 					if(sendAbort) {
 						process.config.logger.info("Received: " + message.toString());
 						Message msg = new Message(process.processId, MessageType.NO, " ");
 						Process.waitTillDelay();
 						process.config.logger.info("Sending No.");
 						process.controller.sendMsg(process.coordinatorProcessNumber, msg.toString());
 					}
 					break; // STOP THE LOOP.
 				} else {
 					// Send coordinator a YES.
 					process.dtLogger.write(STATE.UNCERTAIN);
 					state = STATE.UNCERTAIN;
 					process.config.logger.info("Received: " + message.toString());
 					Message msg = new Message(process.processId, MessageType.YES, " ");
 					Process.waitTillDelay();
 					process.config.logger.info("Sending Yes.");
 					process.controller.sendMsg(process.coordinatorProcessNumber, msg.toString());
 					process.config.logger.info("Waiting to receive either PRE_COMMIT or ABORT.");
 					
 					// Timeout if all the process don't reply back with a Yes or No.
 					Thread th = new Thread() {
 						public void run(){
 							try {
 								Thread.sleep(DECISION_TIMEOUT);
 							} catch (InterruptedException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 							lock.lock();
 							if (state == STATE.UNCERTAIN) {
 								process.config.logger.warning("Did not receive either PRE_COMMIT or ABORT from coordinator. It must be dead.");
 								electCordinator();
 							}
 							lock.unlock();
 						}
 					};
 					th.start();
 				}
 			} else if (state == STATE.UNCERTAIN) {
 				if (message.type == MessageType.ABORT) {
 					stateRequestResponseReceived = true;
 					process.dtLogger.write(STATE.ABORT);
 					state = STATE.ABORT;
 					process.config.logger.info("Transaction aborted. Co-ordinator sent an abort." );
 					break; // STOP THE LOOP
 				}
 				else if (message.type == MessageType.PRE_COMMIT) {
 					stateRequestResponseReceived = true;
 					process.config.logger.info("Received: " + message.toString());
 					process.config.logger.info("Updated state to COMMITABLE.");
 					state = STATE.COMMITABLE;
 					
 					Message msg = new Message(process.processId, MessageType.ACK, " ");
 					Process.waitTillDelay();
 					process.config.logger.info("Sending Acknowledgment.");
 					process.controller.sendMsg(process.coordinatorProcessNumber, msg.toString());
 					
 					// Timeout if all the process don't reply back with a Yes or No.
 					Thread th = new Thread() {
 						public void run(){
 							try {
 								Thread.sleep(DECISION_TIMEOUT);
 							} catch (InterruptedException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 							lock.lock();
 							if (state == STATE.COMMITABLE) {
 								// RUN TERMINATION PROTOCOL.
 								process.config.logger.warning("Did not receive COMMIT from the coordinator. It must be dead.");
 								electCordinator();
 							}
 							lock.unlock();
 						}
 					};
 					th.start();
 				}
 				else {
 					process.config.logger.warning("Was expecting either an ABORT or PRE_COMMIT." +
 							"However, received a: " + message.type);
 					break;
 				}
 			} else if (state == STATE.COMMITABLE) {
 				process.config.logger.info("Received: " + message.toString());
 				if (message.type == MessageType.COMMIT) {
 					stateRequestResponseReceived = true;
 					process.dtLogger.write(STATE.COMMIT);
 					state = STATE.COMMIT;
 					process.config.logger.info("Transaction Committed.");
 					break; // STOP THE LOOP
 				} else {
 					process.config.logger.warning("Was expecting only a COMMIT message." +
 							"However, received a: " + message.type);
 					break;
 				}
 			}
 			
 			// Start waiting for the next message to come.
 			try {
 				nextMessageArrived.await();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		lock.unlock();
 	}
 
 	private void electCordinator() {
 		stateRequestReceived = false;
 		Integer[] keys = (Integer[]) process.upProcess.keySet().toArray(new Integer[0]);
 		Arrays.sort(keys);
 		
 		if (keys[0] > process.processId) {
 			keys[0] = process.processId;
 		}
 		
 		process.config.logger.info("Elected new coordinator: " + keys[0]);
 		
 		// Sending UR_SELECTED message to the new coordinator.
 		// Send a message to the new coordinator that he is the new coordinator.
 		// I would send the message to myself also, if I am the new coordinator.
 		Message msg = new Message(process.processId, MessageType.UR_SELECTED, command);
 		Process.waitTillDelay();
 		process.config.logger.info("Sending: " + msg + " to: " + keys[0]);
 		process.controller.sendMsg(keys[0], msg.toString());
 		
 		// If I am not the elected coordinator then update the new coordinator number.
 		if (keys[0] != process.processId) {
 			process.coordinatorProcessNumber = keys[0];
 			
 			// Start waiting for the new STATE_REQ message.
 			Thread th = new Thread() {
 				public void run() {
 					try {
 						Thread.sleep(DECISION_TIMEOUT);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					if (!stateRequestReceived) {
 						lock.lock();
 						process.config.logger.info("Going to reelect the cordinator because did not get any state Request.");
 						electCordinator();
 						lock.unlock();
 					}
 				}
 			};
 			
 			th.start();
 		}
 	}
 	
 	public void enforceStop() {
 		lock.lock();
 		
 		state = STATE.COMMIT;
 		nextMessageArrived.signal();
 		
 		lock.unlock();
 	}
 
 	public void update(Message message) {
 		lock.lock();
 		
 		this.message = message;
 		if (message.type == MessageType.STATE_REQ) {
 			stateRequestReceived = true;
 			process.coordinatorProcessNumber = message.process_id;
 			process.config.logger.info("Received: " + message.toString());
 			Process.waitTillDelay();
 			if (state == STATE.RESTING) {
 				state = STATE.ABORT;
 			}
 			Message response = new Message(process.processId, MessageType.STATE_VALUE, state.toString());
 			process.config.logger.info("Sending: " + response.toString());
 			stateRequestResponseReceived = false;
 			process.controller.sendMsg(message.process_id, response.toString());
 			
 			if (state == STATE.UNCERTAIN || state == STATE.COMMITABLE) {
 				Thread th = new Thread() {
 					public void run() {
 						try {
							// Increasing this time because Coordinator might have had to send to uncertain process also.
							Thread.sleep(DECISION_TIMEOUT * 2); 
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						if (stateRequestResponseReceived == false) {
 							lock.lock();
 							process.config.logger.info("Going to reelect the cordinator because no response is received after sending STATE.");
 							electCordinator();
 							lock.unlock();
 						}
 					}
 				};
 				th.start();
 			}
 		} else {
 			nextMessageArrived.signal();
 		}
 		
 		lock.unlock();
 	}
 
 }
