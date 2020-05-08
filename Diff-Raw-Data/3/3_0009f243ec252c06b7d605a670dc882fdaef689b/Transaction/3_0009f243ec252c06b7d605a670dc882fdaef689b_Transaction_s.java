 package ut.distcomp.playlist;
 
 import java.util.Arrays;
 import java.util.Hashtable;
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
 	
 	protected final Lock lock = new ReentrantLock();
 	protected final Condition nextMessageArrived = lock.newCondition();
 	
 	/* 
 	 * XXXX FLAGS TO BE CONTROLLED FROM OUTSIDE XXXX
 	 */
 	
 	// This variable is for telling whether a process wants to 
 	// accept this transaction or not.
 	public boolean decision = true;
 	
 	// This is to determine whether to send an abort decision to coordinator or not.
 	public boolean sendAbort = true;
 	
 	//hashtable to keep track of n messages from process p
 	Hashtable<Integer,Integer> deathAfter = new Hashtable<Integer, Integer>();
 	
 	public Transaction(Process process, Message message) {
 		this.process = process;
 		this.command = message.payLoad;
 		this.message = message;
 		this.BUFFER_TIMEOUT = 4000;
 		this.DECISION_TIMEOUT = process.delay + this.BUFFER_TIMEOUT;
 		deathAfter.put(Integer.parseInt(System.getProperty("DeathFromP")), Integer.parseInt(System.getProperty("DeathAfterN")));
 	}
 	
 	public STATE getState() {
 		if (state == STATE.COMMITABLE) {
 			return STATE.UNCERTAIN;
 		}
 		return this.state;
 	}
 	
 	@Override
 	public void run() {
 		lock.lock();
 		while(state != STATE.COMMIT && state != STATE.ABORT) {
 			
 			if (state == STATE.RESTING) {
 				// If we have come here, it means that we just received a VOTE-REQ.
 				// If we don't like the song, we will simply abort.
 				
 				// We are writing this to the log file so that we can know whether this process
 				// has to recover from this transaction or not.
 				// If you would not write this, when a new transaction will start, then you don't
 				// know that whether you are recovering or you are being started afresh.
 				process.dtLogger.write(STATE.RESTING, command);
 				if (!decision) {
 					process.dtLogger.write(STATE.ABORT, command);
 					state = STATE.ABORT;
 					process.notifyTransactionComplete();
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
 					process.dtLogger.write(STATE.UNCERTAIN, command);
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
 					process.dtLogger.write(STATE.ABORT, command);
 					state = STATE.ABORT;
 					process.notifyTransactionComplete();
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
 					process.dtLogger.write(STATE.COMMIT, command);
 					state = STATE.COMMIT;
 					process.config.logger.info("Transaction Committed.");
 					process.notifyTransactionComplete();
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
 		boolean nextChoosen = false;
 		
 		int nextCoordinator = (process.coordinatorProcessNumber + 1 ) % process.config.numProcesses;
 		while(!nextChoosen) {
 			if (nextCoordinator != process.processId) {
 				if (process.upProcess.keySet().contains(nextCoordinator)) {
 					nextChoosen = true;
 				} else {
 					nextCoordinator = (nextCoordinator + 1) % process.config.numProcesses;
 				}
 			} else {
 				nextChoosen = true;
 			}
 		}
 		
 		process.config.logger.info("Elected new coordinator: " + nextCoordinator);
 		
 		// Sending UR_SELECTED message to the new coordinator.
 		// Send a message to the new coordinator that he is the new coordinator.
 		// I would send the message to myself also, if I am the new coordinator.
 		Message msg = new Message(process.processId, MessageType.UR_SELECTED, command);
 		Process.waitTillDelay();
 		process.config.logger.info("Sending: " + msg + " to: " + nextCoordinator);
 		process.controller.sendMsg(nextCoordinator, msg.toString());
 		
 		// If I am not the elected coordinator then update the new coordinator number.
 		if (nextCoordinator != process.processId) {
 			process.coordinatorProcessNumber = nextCoordinator;
 			
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
 
 	public String getUpStates() {
 		return "";
 	}
 }
